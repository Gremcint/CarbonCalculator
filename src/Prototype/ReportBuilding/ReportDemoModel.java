/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Prototype.ReportBuilding;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.TreeMap;
import javax.swing.table.AbstractTableModel;
import Prototype.StaticClasses.Global;
import java.util.Arrays;
import org.jdom2.Element;

/**
 *
 * @author Gregory
 */
public class ReportDemoModel extends AbstractTableModel {

    private ArrayList<List> data = new ArrayList();//the actual data of the table itself
    private ArrayList<String> columnNames = new ArrayList();

    public ReportDemoModel() {
        columnNames.add("Column Name");
        columnNames.add("Table");
        columnNames.add("Display Name");
    }

    public ReportDemoModel(Element columns) {
        columnNames.add("Column Name");
        columnNames.add("Table");
        columnNames.add("Display Name");

        TreeMap<Integer, Element> columnsort = new TreeMap();
        if (columns != null) {
            for (Element term : columns.getChildren()) {
                String name = term.getName();
                if (name.startsWith("COLUMN") && Global.isInteger(name.substring(6))) {
                    Integer number = Integer.parseInt(name.substring(6));
                    if (!columnsort.containsKey(number) && number > 0) {
                        columnsort.put(number, term);
                    }
                }
            }
        }
        for (Element column : columnsort.values()) {
            String commandstring = column.getAttributeValue("value");
            HashMap<String, String> map = Global.makecommandmap(commandstring);
            String columnname = map.get("VALUE");
            
            String tablename = map.get("PATH");
            tablename = tablename.replace("TABLES,","");
            if (columnname != null && tablename != null) {
                data.add(Arrays.asList(columnname, tablename, map.get("DISPLAY")));
            }
        }
        if (data.size() > 1) {
            this.fireTableRowsInserted(0, data.size() - 1);
        }
    }

    public void addRow(List rowData) {
        data.add(new ArrayList(rowData));
        this.fireTableRowsInserted(data.size() - 1, data.size() - 1);
    }

    public void removeRow(int selectedrow) {
        if (selectedrow > -1) {
            data.remove(selectedrow);
        }
        this.fireTableRowsDeleted(selectedrow, selectedrow);
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return col == 2;
    }

    @Override
    public int getColumnCount() {
        return columnNames.size();
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public Object getValueAt(int row, int col) {
        return data.get(row).get(col);
    }

    @Override
    public void setValueAt(Object Value, int row, int col) {

        data.get(row).set(col, Value);
    }

    public Element save() {
        Element list = new Element("COLUMNS");
        Integer columnnumber = 1;
        for (List row : data) {
            String tablename = row.get(1).toString();
            String columnname = row.get(0).toString();
            String display = row.get(2).toString();
            String value = "VALUE:" + columnname + ";PATH:TABLES,"+ tablename + ";DISPLAY:" + display + ";";
            Element newColumn = new Element("COLUMN" + columnnumber);
            columnnumber++;
            newColumn.setAttribute("value", value);
            list.addContent(newColumn);
        }
        return list;
    }

    public boolean moveleft(int selection) {
        if (selection > 0 && data.size() > 1) {
            List leftdata = data.get(selection - 1);
            data.remove(selection - 1);
            data.add(selection, leftdata);
            this.fireTableDataChanged();
            return true;
        }
        return false;
    }

    public boolean moveright(int selection) {
        if (selection > -1 && selection < data.size() - 1 && data.size() > 1) {
            List leftdata = data.get(selection);
            data.remove(selection);
            data.add(selection + 1, leftdata);
            this.fireTableDataChanged();
            return true;
        }
        return false;
    }

    @Override
    public String getColumnName(int col) {
        try {
            return columnNames.get(col);
        } catch (Exception e) {
            return null;
        }
    }

}
