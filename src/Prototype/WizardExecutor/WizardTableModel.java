package Prototype.WizardExecutor;

//<editor-fold defaultstate="collapsed" desc="Imports">
import Prototype.DataManaging.EQValue;
import Prototype.StaticClasses.Global;
import Prototype.StaticClasses.MathHandling;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;
//</editor-fold>

public class WizardTableModel extends AbstractTableModel {

    private ArrayList<Class> columnsclasses = new ArrayList();
    private ArrayList<String> columnNames = new ArrayList();//list of columns in table
    private ArrayList<Integer> uneditable = new ArrayList();//which columns can't be edited
    private ArrayList<List> data = new ArrayList();//the actual data of the table itself
    private ArrayList<String> foreign = new ArrayList();//list of columns that are foreign keys
    private String namecolumn;
    private String primary;//the primary identifier of the table
    private String tablename;//the real name of the table in the database
    private HashMap<String, String> relationships = new HashMap();//

    @Override
    public boolean isCellEditable(int row, int col) {
        return !uneditable.contains(col);
    }

    //<editor-fold defaultstate="collapsed" desc="Foreign and Primary Keys">
    

    public void setprimary(String Primary) {
        primary = Primary;
    }   

    public void settablename(String name) {
        tablename = name;
    }


    public void addforeignkey(String Foreign, String Path) {
        foreign.add(Foreign);
        relationships.put(Foreign, Path);
    }

    public String getlinkedtable(String Columnname) {
        if (relationships.containsKey(Columnname)) {
            return relationships.get(Columnname);
        }
        return null;
    }


    public ArrayList getallforeignkeys() {
        ArrayList temp = (ArrayList) foreign.clone();
        return temp;
    }

    public HashMap<String, String> getrelationships() {
        return relationships;
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Columns">
    public void setnamecolumn(String column) {
        namecolumn = column;
    }

    public String getnamecolumn() {
        return namecolumn;
    }

    public int getindexfromname(String Name) {
        for (int x = 0; x < columnNames.size(); x++) {
            if (columnNames.get(x).equals(Name)) {
                return x;
            }
        }
        return -1;
    }

    public int getcolumnindex(String ColumnName) {
        for (int x = 0; x < this.getColumnCount(); x++) {

            if (ColumnName.equals(this.getColumnName(x))) {
                return x;
            }
        }
        return -1;
    }

    public void SetColumnEdit(int col, boolean edit) {
        if (!edit) {
            if (!uneditable.contains(col)) {
                uneditable.add(col);
            }
        } else {
            if (uneditable.contains(col)) {
                uneditable.remove((Integer) col);
            }
        }
    }

    @Override
    public Class getColumnClass(int c) {
        return columnsclasses.get(c);
    }

    public void addColumn(String columnname) {
        columnNames.add(columnname);
        columnsclasses.add(String.class);
        if (data != null) {
            for (List data1 : data) {
                data1.add(null);
            }
        }
        fireTableStructureChanged();
    }

    public void addColumn(String columnname, Class columnclass) {
        columnNames.add(columnname);
        columnsclasses.add(columnclass);
        if (data != null) {
            for (List data1 : data) {
                data1.add(null);
            }
        }
        fireTableStructureChanged();
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

    public ArrayList getColumn(String columnname) {
        ArrayList column = new ArrayList();
        int index = getcolumnindex(columnname);
        for (int x = 0; x < data.size(); x++) {
            column.add(data.get(x).get(index));
        }
        return column;
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Rows">
    public void addRow(List rowData) {
        data.add(new ArrayList(rowData));
        this.fireTableRowsInserted(data.size() - 1, data.size() - 1);
    }

    public void RemoveRow(int index) {
        if (tablename != null && primary != null) {
            int idcolumn = 0;
            for (int x = 0; x < this.getColumnCount(); x++) {
                if (columnNames.get(index).equals(primary)) {
                    idcolumn = x;
                    x = this.getColumnCount();
                }
            }
            Object id = this.getValueAt(index, idcolumn);
            Statement stmt = null;
            Connection c = null;
            try {
                c = Global.getConnectionPool().getConnection();
                stmt = c.createStatement();
                stmt.executeUpdate("DELETE FROM " + tablename + " WHERE " + primary + " = '" + id.toString() + "';");
                Global.Printmessage("Row Deleted.");
                data.remove(index);
        fireTableRowsDeleted(index, index);
            } catch (Exception e) {
                Global.Printmessage("TableModel.RemoveRow" + e.getClass().getName() + ": " + e.getMessage());
            } finally {
                try {
                    if (stmt != null && !stmt.isClosed()) {
                        stmt.close();
                    }
                    if (c != null && !c.isClosed()) {
                        c.close();
                    }
                } catch (Exception e) {
                    Global.Printmessage("TableModel.RemoveRow" + e.getClass().getName() + ": " + e.getMessage());
                }
            }
        }
    }

    public void addNameColumnValue(String value) {
        String row = "INSERT INTO " + tablename + "(";
        int nameindex = columnNames.indexOf(namecolumn);
        for (int x = 0; x < this.getColumnCount() - 1; x++) {
            row = row + this.columnNames.get(x) + ",";

        }
        row = row + this.columnNames.get(this.getColumnCount() - 1) + ") VALUES(null,";
        for (int x = 0; x < this.getColumnCount() - 1; x++) {
            if (nameindex == x) {
                row = row + value;
            } else {
                row = row + "null,";
            }
        }
        row = row.substring(0, row.length() - 2) + ");";
        if (tablename != null && primary != null) {
            Statement stmt = null;
            Statement stmt2 = null;
            Connection c = null;
            try {
                c = Global.getConnectionPool().getConnection();
                stmt = c.createStatement();
                stmt2 = c.createStatement();
                stmt.executeUpdate(row, Statement.RETURN_GENERATED_KEYS);
                ResultSet rs = stmt.getGeneratedKeys();
                int pkey = 0;
                while (rs.next()) {
                    pkey = rs.getInt(1);
                }
                ResultSet newrow = stmt2.executeQuery("SELECT * FROM " + tablename + " WHERE " + primary + " = " + pkey);
                ResultSetMetaData newrowdata = newrow.getMetaData();

                ArrayList newrowarray = new ArrayList();
                while (newrow.next()) {
                    for (int x = 1; x <= newrowdata.getColumnCount(); x++) {
                        String colclass = newrowdata.getColumnClassName(x);
                        if (foreign.contains(newrowdata.getColumnName(x))) {
                            colclass = "foreign key";
                        }
                        switch (colclass) {
                            case "java.sql.Clob":
                                newrowarray.add(newrow.getString(x));
                                break;
                            case "java.lang.Integer":
                                Integer tempint = newrow.getInt(x);
                                if (newrow.wasNull()) {
                                    tempint = null;
                                }
                                newrowarray.add(tempint);
                                break;

                            case "java.lang.Float":
                                Float tempfloat = newrow.getFloat(x);
                                if (newrow.wasNull()) {
                                    tempfloat = null;
                                }
                                newrowarray.add(tempfloat);
                                break;
                            case "foreign key":
                                newrowarray.add("");
                                break;
                            default:
                                newrowarray.add(newrow.getObject(x));
                                break;
                        }
                    }
                }

                this.addRow(newrowarray);
            } catch (Exception e) {
                Global.Printmessage("TableModel.addRow" + e.getClass().getName() + ": " + e.getMessage());

            } finally {
                try {
                    if (stmt != null && !stmt.isClosed()) {
                        stmt.close();
                    }
                    if (c != null && !c.isClosed()) {
                        c.close();
                    }
                } catch (Exception e) {
                    Global.Printmessage("TableModel.addRow" + e.getClass().getName() + ": " + e.getMessage());
                }

            }
        }

    }

    public void addBlankRow() {
        String row = "INSERT INTO " + tablename + "(";
        for (int x = 0; x < this.getColumnCount() - 1; x++) {
            row = row + this.columnNames.get(x) + ",";
        }
        row = row + this.columnNames.get(this.getColumnCount() - 1) + ") VALUES(";
        for (int x = 0; x < this.getColumnCount() - 1; x++) {
            row = row + "null,";
        }
        row = row + "null);";
        if (tablename != null && primary != null) {
            Statement stmt = null;
            Statement stmt2 = null;
            Connection c = null;
            try {
                c = Global.getConnectionPool().getConnection();
                stmt = c.createStatement();
                stmt2 = c.createStatement();
                stmt.executeUpdate(row, Statement.RETURN_GENERATED_KEYS);
                ResultSet rs = stmt.getGeneratedKeys();
                int pkey = 0;
                while (rs.next()) {
                    pkey = rs.getInt(1);
                }
                ResultSet newrow = stmt2.executeQuery("SELECT * FROM " + tablename + " WHERE " + primary + " = " + pkey);
                ResultSetMetaData newrowdata = newrow.getMetaData();

                ArrayList newrowarray = new ArrayList();
                while (newrow.next()) {
                    for (int x = 1; x <= newrowdata.getColumnCount(); x++) {
                        String colclass = newrowdata.getColumnClassName(x);
                        if (foreign.contains(newrowdata.getColumnName(x))) {
                            colclass = "FOREIGNKEY";
                        }
                        switch (colclass) {
                            case "java.sql.Clob":
                                newrowarray.add(newrow.getString(x));
                                break;
                            case "java.lang.Integer":
                                Integer tempint = newrow.getInt(x);
                                if (newrow.wasNull()) {
                                    tempint = null;
                                }
                                newrowarray.add(tempint);
                                break;

                            case "java.lang.Double":
                                Double tempfloat = newrow.getDouble(x);
                                if (newrow.wasNull()) {
                                    tempfloat = null;
                                }
                                newrowarray.add(tempfloat);
                                break;
                            case "FOREIGNKEY":
                                newrowarray.add("");
                                break;
                            default:
                                newrowarray.add(newrow.getObject(x));
                                break;
                        }
                    }
                }

                this.addRow(newrowarray);
            } catch (Exception e) {
                Global.Printmessage("TableModel.addRow" + e.getClass().getName() + ": " + e.getMessage());

            } finally {
                try {
                    if (stmt != null && !stmt.isClosed()) {
                        stmt.close();
                    }
                    if (c != null && !c.isClosed()) {
                        c.close();
                    }
                } catch (Exception e) {
                    Global.Printmessage("TableModel.addRow" + e.getClass().getName() + ": " + e.getMessage());
                }

            }
        }
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    public HashMap<String, EQValue> getEQrowmap(int index) {
        if (this.getRowCount() <= index) {
            return null;
        }
        HashMap<String, EQValue> results = new HashMap();
        for (int x = 0; x < columnNames.size(); x++) {
            String currentcolumn = columnNames.get(x);
            EQValue currentvalue = getEQValueAt(index, x);

            results.put(currentcolumn, currentvalue);
        }
        return results;
    }
//</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Data">
    public HashMap getdata(HashMap<String, String> CommandMap) {
        HashMap<String, EQValue> results = new HashMap();
        if (CommandMap.containsKey("VALUE") && columnNames.contains(CommandMap.get("VALUE"))) {
            int primaryindex = getindexfromname(primary);
            int dataindex = this.getindexfromname(CommandMap.get("VALUE"));
            if (dataindex == -1 || primaryindex == -1) {
                return null;
            }
            if (!CommandMap.containsKey("IF")) {
                for (int x = 0; x < this.getRowCount(); x++) {
                    results.put(getValueAt(x, primaryindex).toString(), getEQValueAt(x, dataindex));
                }
            } else {
                String Filter = CommandMap.get("IF");
                
                for (int x = 0; x < this.getRowCount(); x++) {
                    HashMap<String, EQValue> currentrow = getEQrowmap(x);

                    if (MathHandling.Solve(Filter, currentrow,null).equals("true")) {
                        results.put(getValueAt(x, primaryindex).toString(), getEQValueAt(x, dataindex));
                    }
                }
            }
        }
        return results;
    }

    public EQValue getEQValueAt(int row, int col)
{
    Object value = getValueAt(row,col);
    String type;
    if(getColumnClass(col).equals(Integer.class)&&getColumnClass(col).equals(Double.class))
    {
        type = "NUMBER";
    }
    else if(getColumnClass(col).equals(Date.class))
    {
        type = "DATE";
    }
    else {
        type = "STRING";
    }
    if(value!=null)
    {
        return new EQValue(value.toString(),type);
    }
    return new EQValue(null,type);
}
    
    @Override
    public Object getValueAt(int row, int col) {
        return data.get(row).get(col);
    }

    @Override
    public void setValueAt(Object Value, int row, int col) {

        data.get(row).set(col, Value);
        if (tablename != null && primary != null) {
            Statement stmt = null;
            Connection c = null;
            try {
                c = Global.getConnectionPool().getConnection();
                stmt = c.createStatement();
                if (foreign.contains(columnNames.get(col))) {
                    String[] temp = Value.toString().split(":");
                    Value = temp[0];
                    if (Value == "") {
                        Value = "null";

                    }
                } else if (columnsclasses.get(col)  == String.class||columnsclasses.get(col) == Date.class)
                {
                    Value = "'" + Value + "'";
                }
                String id = this.getValueAt(row, 0).toString();
                stmt.executeUpdate("UPDATE " + tablename + " SET " + this.columnNames.get(col) + " = " + Value + " WHERE " + primary + " = " + id + ";");
            } catch (Exception e) {
                Global.Printmessage("TableModel.UpdateRow " + e.getClass().getName() + ": " + e.getMessage());

            } finally {
                try {
                    if (stmt != null && !stmt.isClosed()) {
                        stmt.close();
                    }
                    if (c != null && !c.isClosed()) {
                        c.close();
                    }
                } catch (Exception e) {
                    Global.Printmessage("TableModel.updaterow " + e.getClass().getName() + ": " + e.getMessage());
                }
            }

        }
        this.fireTableDataChanged();
    }
//</editor-fold>
}
