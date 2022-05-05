package Prototype.DataManaging;

import java.util.ArrayList;
import java.util.HashMap;

public class StorageTable implements Comparable<StorageTable> {

    private ArrayList<ReportColumn> columns = new ArrayList();
    private HashMap<Integer, ArrayList> rows = new HashMap();
    private ArrayList<String> chain = new ArrayList();
    private String name;
    private HashMap<String, Class> columnclasses = new HashMap();
    private HashMap<String,String> EquationTermNames = new HashMap();
    
    public void setEquationNames(HashMap<String,String> names)
    {
        EquationTermNames = names;
    }

    public HashMap<String, EQValue> getEQrowmap(Integer key) {
        ArrayList currentrow = rows.get(key);
        HashMap<String, EQValue> rowmap = new HashMap();
        for (int x = 0; x < currentrow.size(); x++) {
            rowmap.put(columns.get(x).getpath(), new EQValue(currentrow.get(x).toString(), columnclasses.get(columns.get(x).getdisplay())));
        }
        return rowmap;
    }

    public HashMap<String, EQValue> getEQrowmapnameswap(Integer key) {
        ArrayList currentrow = rows.get(key);
        HashMap<String, EQValue> rowmap = new HashMap();
        for (int x = 0; x < currentrow.size(); x++) {
            String columntag =columns.get(x).getpath();
            String columnref=EquationTermNames.get(columntag);
            if(columnref!=null&&!columnref.isEmpty())
            {
                columntag = EquationTermNames.get(columntag);
            }
            rowmap.put(columntag, new EQValue(currentrow.get(x).toString(), columnclasses.get(columns.get(x).getdisplay())));
        }
        return rowmap;
    }

    public void setname(String Name) {
        name = Name;
    }

    @Override
    public int compareTo(StorageTable othertable) {
        return Integer.compare(othertable.getchain().size(), chain.size());
    }

    public void setcolumnclasses(HashMap<String, Class> classes) {
        columnclasses.putAll(classes);
    }

    public HashMap<String, Class> getcolumnclasses() {
        return columnclasses;
    }

    public void setchain(ArrayList<String> Chain) {
        chain = Chain;
    }

    public ArrayList<String> getchain() {
        return chain;
    }

    public void addcolumn(String display, String table, String column, java.util.TreeMap<Integer, Object> columnvalues) {
        columns.add(new ReportColumn(display, column, table));

        for (Integer key : rows.keySet()) {
            Object value = columnvalues.get(key);
            columnvalues.remove(key);
            rows.get(key).add(value.toString());
        }

        for (Integer key : columnvalues.keySet()) {
            ArrayList<String> row = new ArrayList();
            for (ReportColumn currentcolumn : columns) {
                String columnname = currentcolumn.getdisplay();
                if (columnname.equals(display)) {
                    row.add(columnvalues.get(key).toString());
                } else {
                    row.add(null);
                }
            }
            rows.put(key, row);
        }
    }

    public ArrayList<Integer> getkeys() {
        ArrayList<Integer> keys = new ArrayList(rows.keySet());
        java.util.Collections.sort(keys);
        return keys;
    }

    public ArrayList<ReportColumn> getcolumns() {
        return columns;
    }

    public void changetable(String newtablename, String columnname) {
        DatabaseTableModel tempmodel = Prototype.StaticClasses.TableHandling.getDatabaseTableModel(newtablename);
        columnname = columnname.replace(newtablename + ",", "");
        HashMap<Integer, Object> column = tempmodel.getcolumnwithkeys(columnname);
        HashMap<Integer, ArrayList> newrows = new HashMap();
        for (Integer primarykey : column.keySet()) {
            String foreignkey = column.get(primarykey).toString();
            if (foreignkey != null) {
                ArrayList currentrow = rows.get(foreignkey);
                newrows.put(primarykey, currentrow);
            }
        }
        rows.clear();
        rows = newrows;
    }

    public ArrayList getrow(String primarykey) {
        if (Prototype.StaticClasses.Global.isInteger(primarykey) && rows.containsKey(Integer.parseInt(primarykey)))//this is a hack, originally it was stored as strings and I changed it to integers to get the sorting right
        {
            return rows.get(Integer.parseInt(primarykey));
        }
        return null;
    }

    public void mergetable(StorageTable sourcetable, String columnname) {
        DatabaseTableModel tempmodel = Prototype.StaticClasses.TableHandling.getDatabaseTableModel(name);
        columnname = columnname.replace(name + ",", "");
        HashMap<Integer, Object> column = tempmodel.getcolumnwithkeys(columnname);
        for (Integer primarykey : column.keySet()) {
            String foreignkey = column.get(primarykey).toString();
            if (foreignkey != null) {
                ArrayList currentrow = rows.get(primarykey);
                ArrayList newrowdata = sourcetable.getrow(foreignkey);
                if (newrowdata != null) {
                    currentrow.addAll(newrowdata);
                    rows.put(primarykey, currentrow);
                }
            }
        }
        columns.addAll(sourcetable.getcolumns());
        columnclasses.putAll(sourcetable.getcolumnclasses());
    }
}
