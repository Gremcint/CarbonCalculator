package Prototype.DataManaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import Prototype.StaticClasses.Global;
import java.sql.SQLException;

public class DatabaseTableModel extends AbstractTableModel {

    private int clock;//this integer is for when the table was last updated if it's being loaded and it's out of data it'll be refreshed
    private ArrayList<String> columnNames = new ArrayList();//list of columns in table
    private ArrayList<Integer> uneditable = new ArrayList();//which columns can't be edited
    private ArrayList<List> data = new ArrayList();//the actual data of the table itself
    private ArrayList<String> foreign = new ArrayList();//list of columns that are foreign keys
    private ArrayList<String> equationlist = new ArrayList();
    private ArrayList<Class> columnsclasses = new ArrayList();
    private String path;//the chain of xml parent elements to reach this
    private String namecolumn;
    private String primary;//the primary identifier of the table
    private String datatablename;//the real name of the table in the database
    private HashMap<String, String> relationships = new HashMap();//

    //<editor-fold defaultstate="collapsed" desc="Constructors">
    public DatabaseTableModel(String Primary, String Tablename) {
        primary = Primary;
        datatablename = Tablename;
        clock = Prototype.StaticClasses.TableHandling.getclock();
    }
//</editor-fold>

    //gets the row number using the primary key
    public Integer getRowNumberFromID(Object key) {
        int index = getPrimaryIndex();
        for (int row = 0; row < data.size(); row++) {
            if (data.get(row).get(index).equals(key)) {
                return row;
            }
        }
        return null;
    }

    //<editor-fold defaultstate="collapsed" desc="Path">
    public void setPath(String Path) {
        path = Path;
    }

    public String getPath() {
        return path;
    }
//</editor-fold>

    public int getClock() {
        return clock;
    }

    //prevents editing of certain columns, mainly the primary key and the equation results
    @Override
    public boolean isCellEditable(int row, int col) {
        return !(uneditable.contains(col) || equationlist.contains(columnNames.get(col)));
    }

    //<editor-fold defaultstate="collapsed" desc="Foreign and Primary Keys">
    public int getPrimaryIndex() {
        return getcolumnindex(primary);
    }

    public String getPrimary() {
        return primary;
    }

    public boolean isforeignkey(String key) {
        return foreign.contains(key);
    }

    //column name, path it references
    public void addforeignkey(String Foreign, String Path) {
        foreign.add(Foreign);
        relationships.put(Foreign, Path);
    }

//</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Columns">
    public void setnamecolumn(String column) {
        namecolumn = column;
    }

    public boolean hascolumn(String name) {
        return columnNames.contains(name);
    }

    //returns the location of a specific column based on the supplied name. column names should be unique.
    public int getcolumnindex(String ColumnName) {
        for (int columnindex = 0; columnindex < this.getColumnCount(); columnindex++) {
            if (ColumnName.equals(this.getColumnName(columnindex))) {
                return columnindex;
            }
        }
        return -1;
    }

    //sets whether or not a specific column is editable.
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

    //adds a column to the table, will be empty of data
    public void addColumn(String columnname, Class columnclass) {
        try {
            columnNames.add(columnname);
            columnsclasses.add(columnclass);
            if (data != null) {
                for (List currentrow : data) {
                    currentrow.add(null);
                }
            }
            this.fireTableStructureChanged();
        } catch (Exception e) {
            Global.Printmessage("DBTM addcolumn " + e.getClass() + ":" + e.getMessage());
        }
    }

    @Override
    public int getColumnCount() {
        return columnNames.size();
    }

    //gets a specific column's data paired with the primary key for each row.
    public HashMap<Integer, Object> getcolumnwithkeys(String Column) {
        HashMap<Integer, Object> column = new HashMap();
        int index = getcolumnindex(Column);
        int primaryindex = getPrimaryIndex();
        if (index == -1) {
            return null;
        }
        if (foreign.contains(Column)) {
            for (List row : data) {
                String value = row.get(index).toString();
                if (value != null && !value.isEmpty() && value.contains(":")) {
                    value = value.substring(0, value.indexOf(":"));
                }
                Integer ID = (Integer)row.get(primaryindex);
                column.put(ID, value);
            }
        } else {
            for (List row : data) {
                Integer ID = (Integer)row.get(primaryindex);
                column.put(ID, row.get(index).toString());
            }
        }
        return column;
    }

    @Override
    public String getColumnName(int col) {
        try {
            return columnNames.get(col);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Class getColumnClass(int c) {
        return columnsclasses.get(c);
    }

    //gives a list of the values in a column
    public ArrayList getColumn(String name) {
        ArrayList column = new ArrayList();
        int index = this.getcolumnindex(name);
        for (List data1 : data) {
            column.add(data1.get(index));
        }
        return column;
    }

    //compiles a hashmap of each column with the key being the name of the column
    public HashMap<String, java.util.TreeMap<Integer, Object>> getColumns(ArrayList<String> Names) {
        HashMap<Integer, String> indexes = new HashMap();
        HashMap<String, java.util.TreeMap<Integer, Object>> Results = new HashMap();
        int primindex = getPrimaryIndex();
        for (String name : Names) {
            indexes.put(getcolumnindex(name), name);
            Results.put(name, new java.util.TreeMap<Integer, Object>());
        }

        for (List data1 : data) {
            Integer rowprim = (Integer)data1.get(primindex);
            for (Integer index : indexes.keySet()) {
                Object value = data1.get(index);
                String name = indexes.get(index);
                if (isforeignkey(name)) {
                    String valuestring = value.toString();
                    if(!valuestring.isEmpty()&&valuestring.contains(":")){
                    valuestring = valuestring.substring(0, valuestring.indexOf(":"));
                    value = valuestring;}
                }
                Results.get(indexes.get(index)).put(rowprim, value);
            }
        }
        return Results;
    }
    
    public boolean isEquation(int index)
    {
        return equationlist.contains(columnNames.get(index));
    }
    public boolean isEquation(String name)
    {
        return equationlist.contains(name);
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Rows">
    public void addRow(List rowData) {
        data.add(new ArrayList(rowData));
        this.fireTableRowsInserted(data.size() - 1, data.size() - 1);
    }

    //deletes a row from the display table and the actual database
    public void RemoveRow(int index) {
        if (datatablename != null && primary != null) {
            int idcolumn = getPrimaryIndex();
            Object id = getValueAt(index, idcolumn);
            java.sql.Statement stmt = null;
            java.sql.Connection c = null;
            try {
                c = Global.getConnectionPool().getConnection();
                stmt = c.createStatement();
                stmt.executeUpdate("DELETE FROM " + Global.getcurrentschema() + "." + datatablename + " WHERE " + primary + " = '" + id.toString() + "';");
                Global.Printmessage("Row Deleted.");
                data.remove(index);
                fireTableRowsDeleted(index, index);
                Prototype.StaticClasses.TableHandling.incrementclock();
            } catch (SQLException e) {
                Global.Printmessage("TableModel.RemoveRow" + e.getClass().getName() + ": " + e.getMessage());
            } finally {
                try {
                    if (stmt != null && !stmt.isClosed()) {
                        stmt.close();
                    }
                    if (c != null && !c.isClosed()) {
                        c.close();
                    }
                } catch (SQLException e) {
                    Global.Printmessage("TableModel.RemoveRow" + e.getClass().getName() + ": " + e.getMessage());
                }
            }
        }
    }

    //will add a blank row with a generated primary key into the table that the user can then fill in
    public void addBlankRow() {
        try {
            String schema = Global.getcurrentschema();
            String row = "INSERT INTO " + schema + "." + datatablename + "(";
            for (int columnindex = 0; columnindex < getColumnCount(); columnindex++) {
                String columnname = getColumnName(columnindex);
                if (!columnname.equals(primary) && !equationlist.contains(columnname)) {
                    row = row + columnname + ",";
                }
            }
            row = row.substring(0, row.length() - 1) + ") VALUES(";
            for (int columnindex = 1; columnindex < this.getColumnCount() - 1 - equationlist.size(); columnindex++) {
                row = row + "null,";
            }
            row = row + "null);";
            if (datatablename != null && primary != null) {
                java.sql.Statement stmt = null;//referred to later so without this there is an error
                java.sql.Statement stmt2;
                java.sql.Connection c = null;
                try {
                    c = Global.getConnectionPool().getConnection();
                    stmt = c.createStatement();
                    stmt2 = c.createStatement();
                    stmt.executeUpdate(row, java.sql.Statement.RETURN_GENERATED_KEYS);
                    java.sql.ResultSet rs = stmt.getGeneratedKeys();
                    int pkey = 0;
                    while (rs.next()) {
                        pkey = rs.getInt(1);
                    }
                    java.sql.ResultSet newrow = stmt2.executeQuery("SELECT * FROM " + schema + "." + datatablename + " WHERE " + primary + " = " + pkey);
                    java.sql.ResultSetMetaData newrowdata = newrow.getMetaData();

                    ArrayList newrowarray = new ArrayList();
                    while (newrow.next()) {
                        for (int columnindex = 1; columnindex <= newrowdata.getColumnCount(); columnindex++) {
                            String colclass = newrowdata.getColumnClassName(columnindex);
                            if (foreign.contains(newrowdata.getColumnName(columnindex))) {
                                colclass = "FOEIGNKEY";
                            }
                            switch (colclass) {
                                case "java.sql.Clob":
                                    newrowarray.add(newrow.getString(columnindex));
                                    break;
                                case "java.lang.Integer":
                                    Integer tempint = newrow.getInt(columnindex);
                                    if (newrow.wasNull()) {
                                        tempint = null;
                                    }
                                    newrowarray.add(tempint);
                                    break;

                                case "java.lang.Float":
                                    Float tempfloat = newrow.getFloat(columnindex);
                                    if (newrow.wasNull()) {
                                        tempfloat = null;
                                    }
                                    newrowarray.add(tempfloat);
                                    break;
                                case "java.sql.Date":
                                    java.sql.Date tempdate = newrow.getDate(columnindex);
                                    if (newrow.wasNull()) {
                                        tempdate = null;
                                    }
                                    newrowarray.add(tempdate);
                                    break;
                                case "FOREIGNKEY":
                                    newrowarray.add("");
                                    break;
                                default:
                                    newrowarray.add(newrow.getObject(columnindex));
                                    break;
                            }
                        }
                    }
                    for (String eq : equationlist) {
                        newrowarray.add(null);
                    }
                    this.addRow(newrowarray);
                } catch (SQLException e) {
                    Global.Printmessage("TableModel.addRow" + e.getClass().getName() + ": " + e.getMessage());

                } finally {
                    try {
                        if (stmt != null && !stmt.isClosed()) {
                            stmt.close();
                        }
                        if (c != null && !c.isClosed()) {
                            c.close();
                        }
                    } catch (SQLException e) {
                        Global.Printmessage("TableModel.addblankRow" + e.getClass().getName() + ": " + e.getMessage());
                    }

                }
            }
            Prototype.StaticClasses.TableHandling.incrementclock();
        } catch (Exception e) {
            Global.Printmessage("DBTM add blank row " + e.getClass() + ":" + e.getMessage());
        }
        this.fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    //this code takes creates a hashmap of each row where the key is the column name and the value is the data
    public HashMap<String, EQValue> getEQRowMap(int index, Boolean Parsekeys) {

        if (this.getRowCount() <= index) {
            return null;
        }
        HashMap<String, EQValue> results = new HashMap();
        try {
            for (int columnindex = 0; columnindex < columnNames.size(); columnindex++) {
                String currentcolumn = columnNames.get(columnindex);
                EQValue currentvalue = getEQValueAt(index, columnindex);
                if (isforeignkey(currentcolumn) && Parsekeys && currentvalue != null && !currentvalue.toString().equals("")) {
                    String oldvalue = currentvalue.GetValue();
                    if (oldvalue != null && !oldvalue.isEmpty()) {
                        currentvalue.SetValue(oldvalue.substring(0, oldvalue.indexOf(":")));
                    }
                }
                results.put(currentcolumn, currentvalue);
            }
        } catch (Exception e) {
            Global.Printmessage("DBTM getrowmap " + e.getClass() + ":" + e.getMessage());
        }
        return results;
    }

    public HashMap getrowmap(int index) {
        if (this.getRowCount() <= index) {
            return null;
        }
        HashMap results = new HashMap();
        try {
            for (int columnindex = 0; columnindex < columnNames.size(); columnindex++) {
                String currentcolumn = columnNames.get(columnindex);
                Object currentvalue = this.getValueAt(index, columnindex);
                results.put(currentcolumn, currentvalue);
            }
        } catch (Exception e) {
            Global.Printmessage("DBTM getrowmap " + e.getClass() + ":" + e.getMessage());
        }
        return results;
    }

    public HashMap<String, Class> getcolumnclasses(ArrayList<String> Columns, boolean tableprefix) {
        HashMap<String, Class> classes = new HashMap();
        for (String Column : Columns) {
            int index = getcolumnindex(Column);
            Class classtype = columnsclasses.get(index);
            if (tableprefix) {
                classes.put(datatablename + "," + columnNames.get(index), classtype);
            } else {
                classes.put(columnNames.get(index), classtype);
            }
        }
        return classes;
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Data">
    public ArrayList<String> getcolumnnames() {
        return columnNames;
    }
//returns a list of the various columns by which data type they are
    public HashMap<String, ArrayList<String>> getcategories() {
        ArrayList<String> primarykey = new ArrayList();
        ArrayList<String> numbers = new ArrayList();
        ArrayList<String> strings = new ArrayList();
        ArrayList<String> foreignkeys = new ArrayList();
        ArrayList<String> name = new ArrayList();
        ArrayList<String> dates = new ArrayList();
        ArrayList<String> equations = new ArrayList();
        ArrayList<String> empty = new ArrayList();
        HashMap<String, ArrayList<String>> categories = new HashMap();
        ArrayList<String> All = new ArrayList();//this is being made because just adding the columnnames array means it can be changed accidentally
        All.addAll(columnNames);
        categories.put("ALL", All);
        try {
            for (String currentcolumn : columnNames) {
                int index = this.getcolumnindex(currentcolumn);
                Class columnclass = this.getColumnClass(index);

                if (currentcolumn.equals(primary)) {
                    primarykey.add(currentcolumn);
                }
                if (currentcolumn.equals(namecolumn)) {
                    name.add(currentcolumn);
                }
                if (columnclass == int.class || columnclass == double.class || columnclass == float.class) {
                    numbers.add(currentcolumn);
                }
                if (columnclass == java.sql.Date.class) {
                    dates.add(currentcolumn);
                }
                if (columnclass == Integer.class || columnclass == Double.class || columnclass == Float.class) {
                    numbers.add(currentcolumn);
                }
                if (equationlist.contains(currentcolumn)) {
                    equations.add(currentcolumn);
                }
                if ((!equationlist.contains(currentcolumn)) && columnclass == String.class) {
                    strings.add(currentcolumn);
                }
                if (foreign.contains(currentcolumn)) {
                    foreignkeys.add(currentcolumn);
                }
                if (columnclass == Object.class) {
                    empty.add(currentcolumn);
                }
            }
        } catch (Exception e) {
            Global.Printmessage("DBTM getcategories " + e.getClass() + ":" + e.getMessage());
        }
        categories.put("PRIMARY", primarykey);
        categories.put("NUMBER", numbers);
        categories.put("STRING", strings);
        categories.put("DATE", dates);
        categories.put("FOREIGN", foreignkeys);
        categories.put("NAME", name);
        categories.put("EQUATION", equations);
        categories.put("EMPTY", empty);
        return categories;
    }

    /*
     ok so this procedure is only run under specific circumstances. when calculating an equation and it references a column that is in another table
     it will run this to get the values for that column. this is not used for values in the table where the equation is
     the only IF it should ever receive is limiting it by the values of a foreign key so that it can be grouped together for ops.
    
     what it returns is a hashmap consisting of the entire column (or the ones matching the if statement) simply Primary ID->Row Value
    
     */
    public HashMap<String, EQValue> getdata(HashMap<String, String> CommandMap, org.jdom2.Element TERMS) {
        HashMap<String, EQValue> results = new HashMap();
        try {
            if (CommandMap.containsKey("VALUE") && hascolumn(CommandMap.get("VALUE"))) {
                int primaryindex = getPrimaryIndex();
                int dataindex = getcolumnindex(CommandMap.get("VALUE"));
                if (dataindex == -1 || primaryindex == -1) {
                    return null;
                }
                if (!CommandMap.containsKey("IF")) {
                    for (int columnrow = 0; columnrow < this.getRowCount(); columnrow++) {
                        String a = getValueAt(columnrow, primaryindex).toString();
                        Object b = getValueAt(columnrow, dataindex);
                        if (getColumnName(dataindex).equals(namecolumn)) {

                            String[] tempsplit = b.toString().split(",");
                            b = tempsplit[0];
                            results.put(a, new EQValue(b.toString(), EQValue.EQ_Type_Number));
                        } else {
                            results.put(a, getEQValueAt(columnrow, dataindex));
                        }
                    }
                } else {
                    String Filter = CommandMap.get("IF");
                    for (int columnrow = 0; columnrow < this.getRowCount(); columnrow++) {
                        HashMap<String, EQValue> currentrow = getEQRowMap(columnrow, true);
                        for (org.jdom2.Element currentterm : TERMS.getChildren()) {
                            String name = currentterm.getName();
                            if (Filter.contains("TXT:" + name) && !currentrow.containsKey(name)) {
                                currentrow.put(name, new EQValue(currentterm.getText(), "STRING"));
                            } else if (Filter.contains("DATE:" + name) && !currentrow.containsKey(name)) {
                                currentrow.put(name, new EQValue(currentterm.getAttributeValue("value"), EQValue.EQ_Type_Date));
                            }
                        }
                        if (Filter.isEmpty() || Prototype.StaticClasses.MathHandling.Solve(Filter, currentrow, null).GetValue().equals("true")) {
                            results.put(getValueAt(columnrow, primaryindex).toString(), getEQValueAt(columnrow, dataindex));
                        }
                    }
                }
            }
        } catch (Exception e) {
            Global.Printmessage("DBTM getdata " + e.getClass() + ":" + e.getMessage());
        }
        return results;
    }

    public EQValue getEQValueAt(int row, int col) {
        Object value = getValueAt(row, col);
        String type;
        if (getColumnClass(col).equals(Integer.class) || getColumnClass(col).equals(Double.class)) {
            type = EQValue.EQ_Type_Number;
        } else if (getColumnClass(col).equals(java.sql.Date.class)) {
            type = EQValue.EQ_Type_Date;
        } else if (equationlist.contains(getColumnName(col))&&Global.isNumeric(value.toString()))
        {
        type = EQValue.EQ_Type_Number;    
        }else{
            type = EQValue.EQ_Type_String;
        }
        if (value != null) {
            return new EQValue(value.toString(), type);
        }
        return new EQValue(null, type);
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (row > -1 && col > -1) {
            try {
                return data.get(row).get(col);
            } catch (Exception e) {
                Global.Printmessage("DBTM getvalueat " + row + "," + col + "," + e.getClass() + ":" + e.getMessage());
            }
        }
        return null;
    }

    @Override
    public void setValueAt(Object Value, int row, int col) {
            data.get(row).set(col, Value);
            if (datatablename != null && primary != null) {
                java.sql.Statement stmt = null;
                java.sql.Connection c = null;
                try {
                    c = Global.getConnectionPool().getConnection();
                    stmt = c.createStatement();
                    if(Value==null)
                    {
                    Value = "null";
                    }

                    if (!Value.equals("null") && foreign.contains(columnNames.get(col))) {
                        String[] temp = Value.toString().split(":");
                        Value = temp[0];
                        if (Value == "") {
                            Value = "null";
                        }
                    } else if (!Value.equals("null") && (getValueAt(row, col).getClass() == String.class) || getValueAt(row, col).getClass() == java.sql.Date.class) {
                        Value = "'" + Value + "'";
                    }
                    String id = this.getValueAt(row, 0).toString();
                    stmt.executeUpdate("UPDATE " + Global.getcurrentschema() + "." + datatablename + " SET " + columnNames.get(col) + " = " + Value + " WHERE " + primary + " = " + id + ";");
                } catch (SQLException e) {
                    Global.Printmessage("TableModel.UpdateRow " + e.getClass().getName() + ": " + e.getMessage());

                } finally {
                    try {
                        if (stmt != null && !stmt.isClosed()) {
                            stmt.close();
                        }
                        if (c != null && !c.isClosed()) {
                            c.close();
                        }
                    } catch (SQLException e) {
                        Global.Printmessage("TableModel.updaterow " + e.getClass().getName() + ": " + e.getMessage());
                    }
                }
            }
            Prototype.StaticClasses.TableHandling.incrementclock();
            this.fireTableDataChanged();       
    }

    public void addequationresults(HashMap<String, EQValue> values, String name) {
        try {
            if (!columnNames.contains(name)) {
                this.addColumn(name, String.class);
                equationlist.remove(name);
                equationlist.add(name);
            } else if (!equationlist.contains(name)) {
                Global.Printmessage("Not Equation type Column");
                return;
            }
            int index = this.getcolumnindex(name);
            int primaryindex = this.getPrimaryIndex();
            if (index != -1 && primaryindex != -1) {
                for (int columnrow = 0; columnrow < this.getRowCount(); columnrow++) {
                    EQValue currentvalue;
                    currentvalue = values.get(getValueAt(columnrow, primaryindex).toString());
                    if (currentvalue == null) {
                        data.get(columnrow).set(index, null);
                    } else if (currentvalue.GetComplete()) {
                        data.get(columnrow).set(index, currentvalue.GetValue());
                    } else {
                        data.get(columnrow).set(index, currentvalue.GetStatus());
                    }
                }
                this.fireTableDataChanged();
            }
        } catch (Exception e) {
            Global.Printmessage("DBTM addequationresults " + e.getClass() + ":" + e.getMessage());
        }
    }

//</editor-fold>
}