/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Prototype.TableBuilding;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author User
 */
public class TableBuilderModel extends AbstractTableModel {

    /*
     * To change this template, choose Tools | Templates
     * and open the template in the editor.
     */
    private ArrayList<String> columnNames = new ArrayList();//list of columns in table
    private ArrayList<ArrayList> data = new ArrayList();//the actual data of the table itself
    private ArrayList<Integer> uneditablecolumns = new ArrayList();//which columns can't be edited
    private ArrayList<TableChange> changes = new ArrayList();
    private String CurrentName, SavedName;
    private String newrowtext = "NEW COLUMN";
    private String delrowtext = "DELETE COLUMN";

    public TableBuilderModel(String currentname) {
        super();
        CurrentName = currentname;
        SavedName = currentname;
    }

    public TableBuilderModel(String currentname, String NewRow, String DelRow) {
        super();
        CurrentName = currentname;
        SavedName = currentname;
        newrowtext = NewRow;
        delrowtext = DelRow;
    }

    // <editor-fold defaultstate="collapsed" desc=" Editable ">
    @Override
    public boolean isCellEditable(int row, int col) {
        ArrayList<String> types = new ArrayList();
        types.add("Primary Index");
        types.add("Name Column");
        types.add("Link Column");
        return !(uneditablecolumns.contains(col) || types.contains(data.get(row).get(2).toString()));
    }

    public void SetColumnEdit(int col, boolean edit) {
        if (!edit) {
            if (!uneditablecolumns.contains(col)) {
                uneditablecolumns.add(col);
            }
        } else {
            if (uneditablecolumns.contains(col)) {
                uneditablecolumns.remove((Integer) col);
            }
        }
    }
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Columns ">
    public void addColumn(String columnname) {
        columnNames.add(columnname);
        if (data != null) {
            for (int x = 0; x < data.size(); x++) {
                data.get(x).add(null);
            }
        }
        this.fireTableStructureChanged();
    }

    public void removeColumn(int col) {
        this.columnNames.remove(col);
    }

    @Override
    public int getColumnCount() {
        return columnNames.size();
    }

    @Override
    public String getColumnName(int col) {
        try {
            return columnNames.get(col);
        } catch (Exception e) {
            return null;
        }
    }

    public ArrayList getColumn(int index) {
        ArrayList column = new ArrayList();
        for (int x = 0; x < data.size(); x++) {
            column.add(data.get(x).get(index));
        }
        return column;
    }

    public String getnamecolumn() {
        for (ArrayList row : data) {
            if (row.get(2).toString().equals("Name Column")) {
                return row.get(0).toString();
            }
        }
        return null;
    }

    public String getprimarycolumn() {
        for (ArrayList row : data) {
            if (row.get(2).toString().equals("Primary Index")) {
                return row.get(0).toString();
            }
        }
        return null;
    }
// </editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Rows">
//    public void sortrows() {
//        ArrayList<ArrayList> foreignkeys = new ArrayList();
//        ArrayList primary = new ArrayList();
//        ArrayList namecolumn = new ArrayList();
//        ArrayList<ArrayList> othercolumns = new ArrayList();
//        for (ArrayList list : data) {
//            if (list.size() > 2) {
//                switch (list.get(1).toString()) {
//                    case "PRIMARY":
//                        primary = list;
//                        break;
//                    case "FOREIGN":
//                        foreignkeys.add(list);
//                        break;
//                    case "NAMECOLUMN":
//                        namecolumn = list;
//                        break;
//                    default:
//                        othercolumns.add(list);
//                        break;
//                }
//            }
//        }
//        //**figure out how to make a comparator to make it alphabetical.
//        data.clear();
//        data.add(primary);
//        data.add(namecolumn);
//        data.addAll(foreignkeys);
//        data.addAll(othercolumns);
//    }

    public ArrayList<String> getlinks() {
        ArrayList<String> links = new ArrayList();
        for (ArrayList row : data) {
            if (row.get(2).toString().equals("Link Column")) {
                links.add(row.get(0).toString());
            }
        }
        return links;
    }

    public void addRow(List rowData, boolean addchange) {
        data.add(new ArrayList(rowData));
        int index = data.size() - 1;
        this.fireTableRowsInserted(index, index);
        String rowmap = "";
        for (int x = 0; x < this.getColumnCount(); x++)//need to get column name so can't use foreach
        {
            String name = this.getColumnName(x);
            rowmap = rowmap + name + ":" + rowData.get(x) + ";";
        }
        if (addchange) {
            this.changes.add(new TableChange(newrowtext, "", rowmap, index, -1));
        }
    }

    public void deleteRow(int index) {
        String rowmap = "";
        for (int x = 0; x < this.getColumnCount(); x++)//need to get column name so can't use foreach
        {
            String name = this.getColumnName(x);
            rowmap = rowmap + name + ":" + this.getValueAt(index, x) + ";";
        }
        this.changes.add(new TableChange(delrowtext, rowmap, "", index, -1));
        data.remove(index);
        this.fireTableRowsDeleted(index, index);
    }

    public void deleteallRows(boolean backup) {
        if (backup) {
            ArrayList<ArrayList> olddata = (ArrayList<ArrayList>) data.clone();
            this.changes.add(new TableChange("DelAllRows", olddata, "", -1, -1));
        }
        data.clear();
    }

    public ArrayList getRow(int index) {
        return data.get(index);
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    public HashMap getrowmap(int index) {
        if (this.getRowCount() <= index || index < 0) {
            return null;
        }

        HashMap results = new HashMap();
        for (int x = 0; x < columnNames.size(); x++) {
            String currentcolumn = columnNames.get(x);
            Object currentvalue = this.getValueAt(index, x);
            results.put(currentcolumn, currentvalue);
        }
        return results;
    }
    //</editor-fold>

    @Override
    public Object getValueAt(int row, int col) {
        return data.get(row).get(col);
    }

    @Override
    public void setValueAt(Object Value, int row, int col) {

        Object OldValue = data.get(row).get(col);
        data.get(row).set(col, Value);
        TableChange change = new TableChange("CHANGE DATA", OldValue, Value, row, col);
        changes.add(change);
        this.fireTableDataChanged();
    }

    public void setValueAt(Object Value, int row, int col, boolean addchange) {

        Object OldValue = data.get(row).get(col);
        data.get(row).set(col, Value);
        if (addchange) {
            TableChange change = new TableChange("CHANGE DATA", OldValue, Value, row, col);
            changes.add(change);
        }
        this.fireTableDataChanged();
    }

    public boolean columncontains(Object Value, int col) {
        for (ArrayList list : data) {
            if (list.get(col).equals(Value)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<TableChange> getchanges() {
        return changes;
    }
    
    public boolean haschanges()
    {
        return changes.size()>0;
    }

    public void clearchangelist() {
        changes.clear();
    }

    public void nameupdate(String OldName, String NewName) {
        if (this.columncontains(OldName, 3)) {
            for (ArrayList row : data) {
                if (row.get(3).equals(OldName)) {
                    row.set(3, NewName);
                }
            }
        }
        if (CurrentName.equals(OldName)) {
            CurrentName = NewName;
        }
    }

    public void saved() {
        SavedName = CurrentName;
        this.clearchangelist();
    }

    public ArrayList getcolumnvalues(int index) {
        ArrayList values = new ArrayList();
        for (ArrayList row : data) {
            values.add(row.get(index));
        }

        return values;
    }

    public String getsavedname() {
        return SavedName;
    }

    public String getcurrentname() {
        return CurrentName;
    }

    public TableChange getlastchange() {
        return changes.get(changes.size() - 1);
    }

    public void removechangesbytype(String type) {
        ArrayList<TableChange> newlist = new ArrayList();
        for (TableChange current : changes) {
            if (!current.getaction().equals(type)) {
                newlist.add(current);
            }
        }
        changes.clear();
        changes = newlist;
    }

    public void addchanges(ArrayList<TableChange> newchanges) {
        changes.addAll(newchanges);
    }
}
