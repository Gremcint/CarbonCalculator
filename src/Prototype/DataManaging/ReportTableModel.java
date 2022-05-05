package Prototype.DataManaging;

//<editor-fold defaultstate="collapsed" desc="Imports">
import Prototype.StaticClasses.Global;
import java.util.HashMap;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;
import org.jdom2.Element;
//</editor-fold>

public class ReportTableModel extends AbstractTableModel {

    private ArrayList<String> columnNames = new ArrayList();//list of columns in table
    private ArrayList<Class> columnClasses = new ArrayList(); //list of classes of columns
    private ArrayList<java.util.List> data = new ArrayList();//the actual data of the table itself
    private ArrayList<Integer> equations = new ArrayList();
    private String datatablename;//the real name of the table in the database
    private Element report;
    private HashMap<String, HashMap<String, String>> columninfo = new HashMap();

//    private HashMap<String, String> columnpaths;
    //<editor-fold defaultstate="collapsed" desc="Constructors">
    public ReportTableModel(Element Report) {
        report = Report;
        HashMap<String, String> commandmap = Global.makecommandmap(report);
        datatablename = commandmap.get("TABLE");
        getColumns();
        gatherData();
        renameColumns();
    }
//</editor-fold>

    private void renameColumns() {//this procedure takes the columns and changes them from the database name to the display name 
        for (int index = 0; index < columnNames.size(); index++) {
            String currentname = columnNames.get(index);
            HashMap<String, String> currentcolumn = columninfo.get(currentname);
            if (currentcolumn != null) {
                String pathsplit[] = currentname.split(",");
                DatabaseTableModel model =Prototype.StaticClasses.TableHandling.getDatabaseTableModel(pathsplit[0]);
                if(model!=null&&model.isEquation(pathsplit[1]))//this section here adds a list of equations in the final report so that the report viewer can check for them to format them
                {
                    equations.add(index);
                }
                if (currentcolumn.containsKey("DISPLAY") && !currentcolumn.get("DISPLAY").isEmpty()) {
                    columnNames.set(index, currentcolumn.get("DISPLAY"));
                }
            }
        }
    }

    private void getColumns() {
        java.util.TreeMap<Integer, Element> columnsort = new java.util.TreeMap();
        Element columns = report.getChild("COLUMNS");
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
            HashMap<String, String> map = Global.makecommandmap(column);
            map.put("XMLNAME", column.getName());
            String columnname;
            String path[] = map.get("PATH").split(",");
            columnname = path[1] + "," + map.get("VALUE");

            columninfo.put(columnname, map);
            columnNames.add(columnname);//this is to line up the columns later and it will be replaced with the display name at the end
        }
    }

    private void gatherData() {
        HashMap<String, ArrayList<String>> tablecolumns = new HashMap();
        //the next 2 loops are just to get a list of columns we need to fetch and group them by table
        for (String columntag : columninfo.keySet()) {
            //<editor-fold defaultstate="collapsed" desc="fetch column names">
            HashMap<String, String> currentcolumn = columninfo.get(columntag);
            String path[] = currentcolumn.get("PATH").split(",");

            String table = path[1];
            String columnname = currentcolumn.get("VALUE");
            if (tablecolumns.containsKey(table)) {
                ArrayList<String> currenttable = tablecolumns.get(table);
                if (!currenttable.contains(columnname)) {
                    currenttable.add(columnname);
                }
            } else {
                tablecolumns.put(table, new ArrayList(java.util.Arrays.asList(columnname)));
            }
//</editor-fold>
        }
        Element condition = report.getChild("CONDITION");
        Element terms = Prototype.StaticClasses.XMLHandling.getpath(new String[]{"CONDITION", "TERMS"}, report);
        String equation = "";
        HashMap<String, String> EquationTermNames = new HashMap();
        if (condition != null && condition.getAttributeValue("value") != null) {
            equation = condition.getAttributeValue("value");
        }
        if (terms != null) {
            for (Element Term : terms.getChildren()) {
                if (equation != null && !equation.contains("TXT:" + Term.getName()) && !equation.contains("DATE:" + Term.getName())) {
                    HashMap<String, String> map = Global.makecommandmap(Term);
                    String table = map.get("TABLE");
                    String columnname = map.get("VALUE");
                    EquationTermNames.put(table + "," + columnname, Term.getName());
                    if (tablecolumns.containsKey(table)) {
                        ArrayList<String> currenttable = tablecolumns.get(table);
                        if (!currenttable.contains(columnname)) {
                            currenttable.add(columnname);
                        }
                    } else {
                        tablecolumns.put(table, new ArrayList(java.util.Arrays.asList(columnname)));
                    }
                }
            }
        }

        HashMap<String, StorageTable> tabledata = new HashMap();
        for (String tablename : tablecolumns.keySet()) {
            DatabaseTableModel model = Prototype.StaticClasses.TableHandling.getDatabaseTableModel(tablename);
            HashMap<String, java.util.TreeMap<Integer, Object>> currenttabledata = model.getColumns(tablecolumns.get(tablename));
            HashMap<String, Class> Columnclasses = model.getcolumnclasses(tablecolumns.get(tablename), true);

            StorageTable currentstoragetable = new StorageTable();
            for (String key : currenttabledata.keySet()) {
                currentstoragetable.addcolumn(tablename + "," + key, tablename, key, currenttabledata.get(key));
            }
            currentstoragetable.setcolumnclasses(Columnclasses);
            currentstoragetable.setname(tablename);
            tabledata.put(tablename, currentstoragetable);
        }
        //the program later is looking for when merging strata into plots it wants the linking path to be plots,column name not strata,columnname
        HashMap<String, String> linkedtables = Prototype.StaticClasses.TableHandling.getlinkedtables(datatablename);
        for (String column : linkedtables.keySet()) {
            ArrayList<String> chain = new ArrayList();
            String currenttable = linkedtables.get(column);
            chain.add(datatablename + "," + column);
            if (tabledata.containsKey(currenttable)) {
                ArrayList<String> chainlist = (ArrayList<String>) chain.clone();
                java.util.Collections.reverse(chainlist);
                tabledata.get(currenttable).setchain((ArrayList) chainlist);
            }
            gettablechains(currenttable, tabledata, chain, tablecolumns);
        }

        //strata is giving strata,strata_id in the chain when it should be plot,strata_id
        ArrayList<StorageTable> sortedtables = new ArrayList(tabledata.values());
        java.util.Collections.sort(sortedtables);
        for (StorageTable currenttable : sortedtables) {
            ArrayList<String> currentchain = currenttable.getchain();
            for (String chainlink : currentchain) {
                String[] chainpath = chainlink.split(",");
                String NextTable = chainpath[0];
                if (tabledata.containsKey(NextTable)) {
                    tabledata.get(NextTable).mergetable(currenttable, chainlink);
                    break;
                } else {
                    currenttable.changetable(NextTable, chainlink);
                }
            }
        }
        StorageTable gathereddata = sortedtables.get(sortedtables.size() - 1);
        HashMap<String, Class> tempclasslist = gathereddata.getcolumnclasses();
        for (String column : columnNames) {
            columnClasses.add(tempclasslist.get(column));
        }

        if (equation != null && !equation.isEmpty() && sortedtables.size() > 0) {
            //<editor-fold defaultstate="collapsed" desc="Condition">
            gathereddata.setEquationNames(EquationTermNames);
            ArrayList<Integer> keys = gathereddata.getkeys();
            for (Integer key : keys) {

                HashMap<String, EQValue> rowmap = gathereddata.getEQrowmapnameswap(key);
                EQValue result;

                result = Prototype.StaticClasses.MathHandling.Solve(equation, rowmap, null);
                if (result.GetValue().equals("true")) {
                    ArrayList rowdata = new ArrayList();
                    for (String currentcolumn : columnNames) {
                        int columnnumber = columnNames.indexOf(currentcolumn);
                        Class columnclass = columnClasses.get(columnnumber);
                        try {
                            String currentvalue = rowmap.get(currentcolumn).GetValue();
                            if (columnclass == Integer.class) {
                                rowdata.add(Integer.parseInt(currentvalue));
                            } else if (columnclass == Double.class) {
                                rowdata.add(Double.parseDouble(currentvalue));
                            } else if (columnclass == java.util.Date.class) {
                                rowdata.add(new java.text.SimpleDateFormat("yyyy/MM/dd").parse(currentvalue));
                            } else {
                                rowdata.add(currentvalue);
                            }
                        } catch (NumberFormatException | java.text.ParseException e) {
                            rowdata.add(rowmap.get(currentcolumn));

                        }

                    }
                    addRow(rowdata);
                }
            }
//</editor-fold>
        } else if (sortedtables.size() > 0) {
            ArrayList<Integer> keys = gathereddata.getkeys();
            for (Integer key : keys) {

                HashMap<String, EQValue> rowmap = gathereddata.getEQrowmap(key);
                ArrayList rowdata = new ArrayList();
                for (String currentcolumn : columnNames) {
                    int columnnumber = columnNames.indexOf(currentcolumn);
                    Class columnclass = columnClasses.get(columnnumber);
                    try {
                        String currentvalue = rowmap.get(currentcolumn).GetValue();
                        if (columnclass == Integer.class) {
                            rowdata.add(Integer.parseInt(currentvalue));
                        } else if (columnclass == Double.class) {
                            rowdata.add(Double.parseDouble(currentvalue));
                        } else if (columnclass == java.util.Date.class) {
                            rowdata.add(new java.text.SimpleDateFormat("yyyy/MM/dd").parse(currentvalue));
                        } else {
                            rowdata.add(currentvalue);
                        }
                    } catch (NumberFormatException | java.text.ParseException e) {
                        rowdata.add(rowmap.get(currentcolumn));

                    }

                }
                addRow(rowdata);
            }
        }
    }

    //this procedure is putting together a list of columns and tables laying out how each table connects to the base table.
    private void gettablechains(String starttable, HashMap<String, StorageTable> tabledata, ArrayList<String> chain, HashMap<String, ArrayList<String>> tablecolumns) {
        HashMap<String, String> linkedtables = Prototype.StaticClasses.TableHandling.getlinkedtables(starttable);
        for (String column : linkedtables.keySet()) {
            String currenttable = linkedtables.get(column);
            chain.add(starttable + "," + column);
            StorageTable table = tabledata.get(currenttable);
            if (table != null && (table.getchain().size() > chain.size() || table.getchain().isEmpty())) {
                ArrayList<String> chainlist = (ArrayList<String>) chain.clone();
                java.util.Collections.reverse(chainlist);
                table.setchain(chainlist);
                gettablechains(currenttable, tabledata, chain, tablecolumns);
            }
            if (!tabledata.containsKey(currenttable)) {
                gettablechains(currenttable, tabledata, chain, tablecolumns);
            }
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
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

    @Override
    public Class getColumnClass(int c) {
        if (columnClasses.size() < c || columnClasses.get(c) == null) {
            return String.class;
        }
        return columnClasses.get(c);
    }

    //<editor-fold defaultstate="collapsed" desc="Rows">
    public void addRow(java.util.List rowData) {

        data.add(new ArrayList(rowData));
        this.fireTableRowsInserted(data.size() - 1, data.size() - 1);
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public Object getValueAt(int row, int col) {
        try {
            return data.get(row).get(col);
        } catch (Exception e) {
            Global.Printmessage("RTM getvalueat " + row + "," + col + "," + e.getClass() + ":" + e.getMessage());
        }
        return null;
    }

    @Override
    public void setValueAt(Object Value, int row, int col) {

        data.get(row).set(col, Value);

        this.fireTableDataChanged();
    }
    
    public boolean isEquation(int index)
    {
        return equations.contains(index);
    }
}
