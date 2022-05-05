/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Prototype.StaticClasses;

import Prototype.DataManaging.DatabaseTableModel;
import Prototype.DataManaging.DataTable;
import java.util.HashMap;
import java.util.ArrayList;
import org.jdom2.Element;

/**
 *
 * @author User
 */
public class TableHandling {

    //tables stored by their tablename
    static HashMap<String, DataTable> TableList = new HashMap();

    //<editor-fold defaultstate="collapsed" desc="Scale and Highlight">
    private static int Scale = 5;
    private static Boolean Highlight = false;
    
    public static int getScale(){
        return Scale;
    }
    
    public static void setScale(int Scale){
        TableHandling.Scale = Scale;
    }
    
    public static Boolean getHighlight()
    {
        return Highlight;
    }
//</editor-fold>
    
    public static void setHighlight(Boolean Highlight){
        TableHandling.Highlight = Highlight;
    }
    
    //<editor-fold defaultstate="collapsed" desc="Clock">
    static int clock = 0;

    public static void incrementclock() {
        clock++;
        if (clock > 2000000000)//doubt it'll reach that high ever by why take the chance.
        {
            clock = 0;
        }
    }

    public static int getclock() {
        return clock;
    }
//</editor-fold>

    public static void UpdateTable(String Tablename) {
        if (Global.getMode().equals(Global.PROJECTMODE)) {
            if (TableList.containsKey(Tablename)) {
                String Schema = Global.getcurrentschema();
                Element tablesource = XMLHandling.getpath(new String[]{"PROJECTS", Schema, "TABLES", Tablename}, Global.getxmlfilename());
                DataTable tablenode = TableList.get(Tablename);
                tablenode = new DataTable(Tablename, tablesource, Schema);
                TableList.put(Tablename, tablenode);
            }
        }
    }

    public static ArrayList<String> getlistoftables(String Schema) {
        switch (Global.getMode()) {
            case Global.PROJECTMODE: {
                ArrayList<String> tables = SQLHandling.getlistoftables(Schema);
                return tables;
            }
            case Global.TEMPLATEMODE: {
                String[] Path = {"TEMPLATES", Schema, "TABLES"};
                Element tableparent = XMLHandling.getpath(Path, Global.getxmlfilename());
                ArrayList<String> tables = new ArrayList();
                if (tableparent != null) {
                    for (Element table : tableparent.getChildren()) {
                        tables.add(table.getName());
                    }
                }
                return tables;
            }
        }

        return null;
    }

    public static void addDataTable(String tablename, DataTable table) {
        TableList.put(tablename, table);
    }

    public static boolean hasDataTable(String tablename) {
        return TableList.containsKey(tablename);
    }

    public static String getTableNameColumn(String tablename) {
        if (TableList.containsKey(tablename)) {
            DataTable node = TableList.get(tablename);
            return node.getNameColumn();
        }
        return null;
    }

    public static void loadtables() {
        TableHandling.TableList.clear();
        if (Global.getMode().equals(Global.PROJECTMODE)) {
            String Schema = Global.getcurrentschema();
            ArrayList<String> tables = SQLHandling.getlistoftables(Schema);
            for (String temptable : tables) {
                Element newtable = XMLHandling.getpath(new String[]{"PROJECTS", Schema, "TABLES", temptable}, Global.getxmlfilename());
                if (newtable != null) {
                    TableHandling.addDataTable(temptable, new DataTable(temptable, newtable, Schema));
                }
            }
        }
    }

    //get the different categories of values for adding the the equation builder interface.
    //code already exists in databasetablemodel need to write xml template equivalent
    public static HashMap<String, ArrayList<String>> getequationvalues(String tablename)
    {
        switch (Global.getMode()) {
            case Global.PROJECTMODE:
                DataTable node = TableList.get(tablename);
                if (node != null) {
                    return node.getEquationValues();
                }
                break;
            case Global.TEMPLATEMODE:
                Element table = XMLHandling.getpath(new String[]{"TEMPLATES", Global.getcurrentschema(), "TABLES", tablename}, Global.getxmlfilename());
                Element columns = table.getChild("COLUMNS");
                Element equationxml = table.getChild("EQUATIONS");
                ArrayList<String> primarykey = new ArrayList();
                ArrayList<String> numbers = new ArrayList();
                ArrayList<String> strings = new ArrayList();
                ArrayList<String> foreignkeys = new ArrayList();
                ArrayList<String> name = new ArrayList();
                ArrayList<String> equations = new ArrayList();
                ArrayList<String> dates = new ArrayList();
                ArrayList<String> all = new ArrayList();
                HashMap<String, ArrayList<String>> categories = new HashMap();

                for (Element column : columns.getChildren()) {
                    String columnname = column.getName();
                    all.add(columnname);
                    String columnstring = column.getAttributeValue("value");
                    if (columnstring != null) {
                        if (columnstring.contains("TYPE:PRIMARYKEY;") || columnstring.endsWith("TYPE:PRIMARYKEY")) {
                            primarykey.add(columnname);
                        }
                        if (columnstring.contains("TYPE:INTEGER;") || columnstring.endsWith("TYPE:INTEGER")) {
                            numbers.add(columnname);
                        }
                        if (columnstring.contains("TYPE:DOUBLE;") || columnstring.endsWith("TYPE:DOUBLE")) {
                            numbers.add(columnname);
                        }
                        if (columnstring.contains("TYPE:TEXT;") || columnstring.endsWith("TYPE:TEXT")) {
                            strings.add(columnname);
                        }
                        if (columnstring.contains("TYPE:DATE;") || columnstring.endsWith("TYPE:DATE")) {
                            dates.add(columnname);
                        }
                        if (columnstring.contains("TYPE:FOREIGNKEY;") || columnstring.endsWith("TYPE:FOREIGNKEY")) {
                            foreignkeys.add(columnname);
                        }
                    }
                }
                if (equationxml != null) {
                    for (Element equation : equationxml.getChildren()) {
                        String equationname = equation.getName();
                        equations.add(equationname);
                    }
                }
                String tablestring = table.getAttributeValue("value");
                HashMap<String, String> tablemap = Global.makecommandmap(tablestring);
                String namecolumn = tablemap.get("NAMECOLUMN");
                name.add(namecolumn);
                categories.put("PRIMARY", primarykey);
                categories.put("NUMBER", numbers);
                categories.put("STRING", strings);
                categories.put("FOREIGN", foreignkeys);
                categories.put("NAME", name);
                categories.put("DATE", dates);
                categories.put("EQUATION", equations);
                categories.put("ALL", all);
                return categories;
        }

        return new HashMap();
    }

    public static ArrayList<String> getcolumnnames(String tablename) {
        switch (Global.getMode()) {
            case Global.PROJECTMODE:
                DataTable node = TableList.get(tablename);
                if (node != null) {
                    return node.getColumnNames();
                }
                break;
            case Global.TEMPLATEMODE:
                Element table = XMLHandling.getpath(new String[]{"TEMPLATES", Global.getcurrentschema(), "TABLES", tablename}, Global.getxmlfilename());
                Element columns = table.getChild("COLUMNS");
                Element equationxml = table.getChild("EQUATIONS");
                ArrayList<String> all = new ArrayList();
                if (columns != null) {
                    for (Element column : columns.getChildren()) {
                        String columnname = column.getName();
                        all.add(columnname);
                    }
                }
                if (equationxml != null) {
                    for (Element equation : equationxml.getChildren()) {
                        String equationname = equation.getName();
                        all.add(equationname);
                    }

                }
                return all;
        }

        return new ArrayList();
    }

    //check if secondary has a foreign key linking to secondary
    public static String checkforeignkeys(String Primary, String Secondary) {
        switch (Global.getMode()) {
            case Global.PROJECTMODE:
                return SQLHandling.checkforeignkeys(Primary, Secondary);
            case Global.TEMPLATEMODE:
                Element columns = XMLHandling.getpath(new String[]{"TEMPLATES", Global.getcurrentschema(), "TABLES", Secondary, "COLUMNS"}, Global.getxmlfilename());
                for (Element column : columns.getChildren()) {
                    String columnstring = column.getAttributeValue("value");
                    //so this code will work in almost all cases but if someone makes 2 very similar column names that both link to the same table it could mess up but the odds of that are super low
                    if (columnstring != null && columnstring.contains("LINKTO:" + Primary + ",")) {
                        return column.getName();
                    }
                }
                break;
        }
        return null;
    }

    //returns list of forein keys the tables share if they both link the same tables
    public static ArrayList<String> getsharedforeignkeys(String Primary, String Secondary) {
        ArrayList<String> keylist = new ArrayList();
        HashMap<String, String> primarycolumns = getlinkedtables(Primary);
        HashMap<String, String> secondarycolumns = getlinkedtables(Secondary);
        if (primarycolumns != null && secondarycolumns != null && primarycolumns.size() > 0 && secondarycolumns.size() > 0) {
            for (String key : secondarycolumns.keySet()) {
                if (primarycolumns.containsValue(secondarycolumns.get(key))) {
                    keylist.add(key);
                }
            }
        }
        return keylist;
    }

    //returns Key: column name Value: table name
    public static HashMap<String, String> getlinkedtables(String tablename) {
        switch (Global.getMode()) {
            case Global.PROJECTMODE:
                return SQLHandling.getlinkedtables(tablename);
            case Global.TEMPLATEMODE:
                HashMap<String, String> results = new HashMap();
                Element table = XMLHandling.getpath(new String[]{"TEMPLATES", Global.getcurrentschema(), "TABLES", tablename}, Global.getxmlfilename());
                Element columns = table.getChild("COLUMNS");
                if (columns != null) {
                    for (Element column : columns.getChildren()) {
                        String columnstring = column.getAttributeValue("value");
                        if (columnstring != null) {
                            if (columnstring.contains("TYPE:FOREIGN;") || columnstring.endsWith("TYPE:FOREIGN")) {
                                HashMap<String, String> columnmap = Global.makecommandmap(columnstring);
                                String target = columnmap.get("LINKTO");
                                if (target != null && !target.isEmpty()) {
                                    results.put(column.getName(), target);
                                }
                            }
                        }
                    }
                    return results;
                }
        }
        return null;
    }

    public static DataTable getTableNode(String tablename) {
        if (TableList != null && TableList.containsKey(tablename)) {
            return TableList.get(tablename);
        }
        return null;
    }

    public static DatabaseTableModel getDatabaseTableModel(String tablename) {
        DataTable node = TableList.get(tablename);
        if (node != null) {
            return node.getModel();
        }
        return null;
    }

    public static String ExportTable(javax.swing.JTable table, String name) {
        javax.swing.table.TableModel model = table.getModel();
        String output = "";
        int columns = model.getColumnCount();
        int rows = model.getRowCount();
        output = name + "\n";
        for (int col = 0; col < columns; col++) {
            output = output + model.getColumnName(col);
            if (col < columns - 1) {
                output = output + "\t";
            }
        }
        output = output + "\n";

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                output = output + model.getValueAt(row, col);
                if (col < columns - 1) {
                    output = output + "\t";
                }
            }
            output = output + "\n";
        }
        return output;
    }
}
