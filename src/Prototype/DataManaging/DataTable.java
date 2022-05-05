package Prototype.DataManaging;

import Prototype.StaticClasses.Global;
import Prototype.StaticClasses.TableHandling;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JTable;


/*
This is basically a management class for the data tables.
handles getting the data between them, searching for tables by name
and generating their models.
It's carryover from a previous method of organization. In a rebuild this 
would be merged with the databasetablemodel class and the tablehandling static class
*/
public class DataTable {

    private ArrayList<Equation> Equationlist = new ArrayList(); //list of equations in the table
    private DatabaseTableModel dtmTableModel;//the model for the table that holds the data
    private org.jdom2.Element XMLSource;//the xml source for the table
    private JTable tblDisplayTable = new JTable();//the actual display table
    private String strNameColumn;//the column of the table that stores the name for each record
    private String strSchema;//the software uses database schema to separate different projects in the same file
    private String strTableName;//the name of the table

    public DataTable(String TableName, org.jdom2.Element Source, String Schema) {
        try {
            XMLSource = Source;
            strTableName = TableName;
            tblDisplayTable = new JTable();
            this.strSchema = Schema;
            //this code takes the instructions stored in the XML and chops it up to be able to get the namecolumn information
            String sourcestring = XMLSource.getAttributeValue("value");
            HashMap<String, String> sourcemap = Global.makecommandmap(sourcestring);
            strNameColumn = sourcemap.get("NAMECOLUMN");
        } catch (Exception e) {
            Global.Printmessage("Data Tree Node Constructor " + e.getClass() + ":" + e.getMessage());
        }

    }

    //<editor-fold defaultstate="collapsed" desc="Rows">
    public void AddBlankRow() {
        dtmTableModel.addBlankRow();
    }

    public void RemoveRow(int index) {
        dtmTableModel.RemoveRow(index);
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Columns">
    public ArrayList<String> getColumnNames() {
        if (dtmTableModel == null || dtmTableModel.getClock() != TableHandling.getclock()) {
            this.GenerateModel();
        }
        return dtmTableModel.getcolumnnames();
    }

    public String getNameColumn() {
        return strNameColumn;
    }
//</editor-fold>

    public JTable getTable() {
        if (dtmTableModel == null || dtmTableModel.getClock() != TableHandling.getclock()) {
            GenerateModel();
        }
        return tblDisplayTable;
    }

    /*
     this function returns a list of the different columns in the table organized into different groups
     based on their data types.
     */
    public HashMap<String, ArrayList<String>> getEquationValues() {
        if (dtmTableModel == null || dtmTableModel.getClock() != TableHandling.getclock()) {
            GenerateModel();
        }
        return dtmTableModel.getcategories();
    }

    //this code uses the data in the table to generate the results of all the equations and insert them into the table
    private void getEquationResults() {
        try {
            //create a list to track equations that have been solved
            ArrayList<String> solved = new ArrayList();
            //create a list of equations to solve
            ArrayList<String> EquationNames = new ArrayList();
            for (Equation eq : Equationlist) {
                EquationNames.add(eq.getname());
            }
            /*basically loop until all the equations have been solved and go through them one by one
            note there is a known flaw here, no testing to prevent an infinite loop if equations reference each other back and forth. 
            this is a proof of concept project to demonstrate that the math and system can work and that part was skipped 
            this would definitely be something to deal with if an actual version of this software was to be made.
            */
            while (solved.size() < Equationlist.size()) {
                for (Equation eq : Equationlist) {
                    if (!solved.contains(eq.getname())) {//if already solved then skip
                        boolean solvetest = false;
                        ArrayList<String> listofterms = eq.getlistofterms();
                        
                        /*this section is checking if any of the terms used in this equation are 
                        A. unsolved and need to be calculated first
                        B. a more complext operation such as a summation or if statement that then needs to be solved first
                        if that is the case then the loop is setup to solve those first and come back to this
                        */
                        for (String currentterm : listofterms) {
                            if (EquationNames.contains(currentterm) && !solved.contains(currentterm)) {
                                break;
                            }
                            HashMap<String, String> commandmap;
                            if (currentterm.contains("OP:")) {
                                String[] temp = currentterm.split(":");
                                currentterm = temp[1];
                            }
                            if (currentterm.contains("EVAL:")) {
                                String[] temp = currentterm.split(":");
                                currentterm = temp[1];
                            }
                            commandmap = eq.getcommandmap(currentterm);
                            solvetest = true;
                            //is the value local and is it in the list of unsolved equations?
                            if (commandmap != null && commandmap.containsKey("PATH") && commandmap.get("PATH").equals(dtmTableModel.getPath()) && commandmap.containsKey("VALUE") && !solved.contains(commandmap.get("VALUE"))) {
                                break;
                            }

                        }
                        //If we're good to go then solve the equations and add the results to the table and the equation name to the list.
                        if (solvetest) {
                            HashMap<String, EQValue> results = eq.solveformula();
                            dtmTableModel.addequationresults(results, eq.getname());
                            solved.add(eq.getname());
                        }
                    }
                }
            }
        } catch (Exception e) {
            Global.Printmessage("DTN get equations results " + e.getClass() + ":" + e.getMessage());
        }
    }

    //checks if the model exists and is up to date. if it isn't then it makes one and either way returns it
    public DatabaseTableModel getModel() {
        if (dtmTableModel == null || dtmTableModel.getClock() != TableHandling.getclock()) {
            GenerateModel();
        }
        return dtmTableModel;
    }

    //This is the code that generates the table model and arranges the data for displaying to the user
    private void GenerateModel() {
        //prep initial variables
        Equationlist.clear();
        java.sql.Statement stmtData = null;
        java.sql.Statement stmtKeys = null;
        java.sql.Connection connection = null;
        
        //this gets the primary key for the table
        ArrayList<String> commands = new ArrayList();
        commands.add("Primary");
        HashMap tableinfo = Prototype.StaticClasses.SQLHandling.gettableinfo(strTableName, strSchema, commands);
        String Primary = tableinfo.get("Primary").toString();
        
        try {
            connection = Global.getConnectionPool().getConnection();
            //the foreign keys for the table.
            HashMap<String, HashMap<String, String>> fkeys = new HashMap();
            stmtData = connection.createStatement();
            ResultSet results;
            //get all the rows from the table
            if (Primary != null) {
                results = stmtData.executeQuery("SELECT * FROM " + strSchema + "." + strTableName + " ORDER BY " + Primary);
            } else {
                results = stmtData.executeQuery("SELECT * FROM " + strSchema + "." + strTableName);
            }
            //so this code gets all the raw data from the database for the table as well as the primary and foreign keys
            java.sql.ResultSetMetaData resultsdata = results.getMetaData();//metadata about this specific table
            ArrayList<String> columnlist = new ArrayList();
            java.sql.DatabaseMetaData db = connection.getMetaData();//metadata about the entire database
            ResultSet keys = db.getImportedKeys(null, strSchema, strTableName);//creates a result set of information on the foreginkeys for this table
            ResultSet rstemp = db.getPrimaryKeys(null, strSchema, strTableName);

            String primary = "";

            while (rstemp.next()) {
                primary = rstemp.getString("COLUMN_NAME");
            }
            //this is where we start building the table model. takes the info from the database puts it into the table
            dtmTableModel = new DatabaseTableModel(primary, strTableName);
            for (int columnindex = 1; columnindex <= resultsdata.getColumnCount(); columnindex++) {
                columnlist.add(resultsdata.getColumnName(columnindex));//creates a list of columns in the table
                String classname = resultsdata.getColumnClassName(columnindex);
                switch (classname) {
                    case "java.sql.Clob":
                        dtmTableModel.addColumn(columnlist.get(columnindex - 1), String.class);
                        break;
                    case "java.lang.Integer":
                        dtmTableModel.addColumn(columnlist.get(columnindex - 1), Integer.class);
                        break;
                    case "java.lang.Float":
                        dtmTableModel.addColumn(columnlist.get(columnindex - 1), Float.class);
                        break;
                    case "java.lang.Double":
                        dtmTableModel.addColumn(columnlist.get(columnindex - 1), Double.class);
                        break;
                    case "java.sql.Date":
                        dtmTableModel.addColumn(columnlist.get(columnindex - 1), java.sql.Date.class);
                        tblDisplayTable.setDefaultEditor(java.sql.Date.class, new javax.swing.DefaultCellEditor(new Prototype.StaticClasses.DateTextField()));
                        break;
                    default:
                        dtmTableModel.addColumn(columnlist.get(columnindex - 1), String.class);
                        break;
                }
            }

            while (keys.next()) {//go through list of foreign keys one by one
                if (columnlist.contains(keys.getString("FKCOLUMN_NAME"))) {
                    String pkey = keys.getString("PKCOLUMN_NAME");//get the name of the linked table
                    String ptablename = keys.getString("PKTABLE_NAME");//get the primary key of the linked table
                    dtmTableModel.addforeignkey(keys.getString("FKCOLUMN_NAME"), "TABLES," + ptablename);

                    String ncolumn = TableHandling.getTableNameColumn(ptablename);

                    stmtKeys = connection.createStatement();
                    ResultSet ptable = stmtKeys.executeQuery("SELECT " + pkey + ", " + ncolumn + " FROM " + strSchema + "." + ptablename + ";");
                    //puts together a list of values for the foreign key to use a dropdown selection later
                    HashMap<String, String> temp = new HashMap();
                    while (ptable.next()) {
                        temp.put(ptable.getString(pkey), ptable.getString(pkey) + ": " + ptable.getString(ncolumn));
                    }
                    fkeys.put(keys.getString("FKCOLUMN_NAME"), temp);
                    stmtKeys.close();
                }
            }

            //this goes through the results row by row and compiles the table
            while (results.next()) {
                ArrayList<Object> rowdata = new ArrayList();
                HashMap fkey = new HashMap();
                for (int columnindex = 1; columnindex <= resultsdata.getColumnCount(); columnindex++) {
                    String colclass = resultsdata.getColumnClassName(columnindex);
                    if (fkeys.containsKey(resultsdata.getColumnName(columnindex))) {
                        colclass = "foreign key";
                        fkey = fkeys.get(resultsdata.getColumnName(columnindex));
                    }
                    switch (colclass) {
                        case "java.sql.Clob":
                            rowdata.add(results.getString(columnindex));
                            break;
                        case "java.sql.Date":
                            rowdata.add(results.getDate(columnindex));
                            break;
                        case "java.lang.Integer":
                            Integer tempint = results.getInt(columnindex);
                            if (results.wasNull()) {
                                tempint = null;
                            }
                            rowdata.add(tempint);
                            break;
                        case "java.lang.Float":
                            Float tempfloat = results.getFloat(columnindex);
                            if (results.wasNull()) {
                                tempfloat = null;
                            }
                            rowdata.add(tempfloat);
                            break;
                        case "foreign key":
                            String tempstring = results.getString(columnindex);
                            if (fkey.containsKey(tempstring)) {
                                rowdata.add(fkey.get(tempstring));
                            } else if (tempstring == null) {
                                rowdata.add("");
                            } else {
                                rowdata.add(tempstring);
                            }
                            break;
                        default:
                            rowdata.add(results.getObject(columnindex));
                            break;
                    }
                }
                dtmTableModel.addRow(rowdata);
            }
            dtmTableModel.SetColumnEdit(0, false);//prevents people from editing the primary key column

            //now that the base data has been gathered and put in the table the program starts to solve the equations
            org.jdom2.Element Equations = XMLSource.getChild("EQUATIONS");
            dtmTableModel.setnamecolumn(this.strNameColumn);
            dtmTableModel.setPath(Global.getcurrentschema() + ",TABLES," + strTableName);

            if (Equations != null) {
                for (org.jdom2.Element currentEq : Equations.getChildren()) {
                    Equationlist.add(new Equation(currentEq, dtmTableModel));
                }
            }
            tblDisplayTable.setModel(dtmTableModel);

            getEquationResults();
            //then finally updates the display of the table
            tblDisplayTable.getModel().addTableModelListener(new javax.swing.event.TableModelListener() {
                @Override
                public void tableChanged(javax.swing.event.TableModelEvent e) {
                    JTable table = getTable();
                    table.setModel(getModel());
                }
            });

            //this code makes it so that any foreign key column is handled with a dropdown list of values from the referenced table
            for (int columnindex = 0; columnindex < tblDisplayTable.getColumnCount(); columnindex++) {
                String columnname;
                columnname = tblDisplayTable.getColumnName(columnindex);
                if (fkeys.containsKey(columnname)) {
                    javax.swing.table.TableColumn tcolumn = tblDisplayTable.getColumnModel().getColumn(columnindex);
                    ArrayList<String> listofkeys = new ArrayList(fkeys.get(columnname).values());
                    java.util.Collections.sort(listofkeys);
                    javax.swing.JComboBox combobox = new javax.swing.JComboBox(listofkeys.toArray());
                    combobox.insertItemAt("", 0);
                    tcolumn.setCellEditor(new javax.swing.DefaultCellEditor(combobox));
                }
            }

            stmtData.close();
            //sets the selection mode for the table.
            tblDisplayTable.setCellSelectionEnabled(true);
            javax.swing.ListSelectionModel cellSelectionModel = tblDisplayTable.getSelectionModel();
            cellSelectionModel.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
            tblDisplayTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
            tblDisplayTable.getColumnModel().setColumnSelectionAllowed(true);
        } catch (SQLException e) {
            Global.Printmessage("DataNode.GenerateModel " + e.getClass().getName() + ": " + e.getMessage());
            Global.Printmessage(this.strTableName);
        } finally {
            //makes sure to close the database connections whether everything worked or not.
            try {
                if (stmtData != null && !stmtData.isClosed()) {
                    stmtData.close();
                }
                if (stmtKeys != null && !stmtKeys.isClosed()) {
                    stmtKeys.close();
                }
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                Global.Printmessage("DataTreeNode.GenerateModel " + e.getClass().getName() + ": " + e.getMessage());
                Global.Printmessage(this.strTableName);
            }
        }
    }
}