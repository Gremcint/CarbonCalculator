/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Prototype.WizardExecutor;

//<editor-fold defaultstate="collapsed" desc="Imports">
import java.awt.event.ActionListener;
import java.sql.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.*;
import net.miginfocom.swing.MigLayout;
import org.jdom2.Element;
import Prototype.Popups.TextForm;
import Prototype.StaticClasses.Global;
import java.awt.Color;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;

//</editor-fold>
public class WizardTableNode {

    private Element source;
    private WizardTableModel model;
    private HashMap<String, String> Equations = new HashMap();
    private JPanel panel;
    private JSpinner Rows;
    private JTable table = new JTable();
    private String namecolumn;
    private String tablename;
    private WizardForm home;

    //<editor-fold defaultstate="collapsed" desc="Constructor">
    public WizardTableNode(WizardForm Home, String TableName, Element Source) {
        home = Home;
        source = Source;
        tablename = TableName;
        table = new JTable();
        String sourcestring = source.getAttributeValue("value");
        HashMap<String, String> sourcemap = Global.makecommandmap(sourcestring);
        namecolumn = sourcemap.get("NAMECOLUMN");
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="gets">
    private JTable getTable() {
        model = getModel();
        table.setModel(model);//just to be sure this isn't called out of order
        return table;
    }

    public String getTableName() {
        return tablename;
    }

    public HashMap<String, String> getequations() {
        return Equations;
    }

    public String getNameColumn() {
        namecolumn = model.getnamecolumn();
        return namecolumn;
    }

    public HashMap getdata(HashMap<String, String> CommandMap) {
        this.getModel();
        HashMap results = model.getdata(CommandMap);
        return results;
    }

    public HashMap<String, String> getrelationships() {
        if (model == null) {
            GenerateModel();
        }
        return model.getrelationships();
    }

    public HashMap<String, ArrayList> getkeyvalues() {
        ArrayList<String> keys = model.getallforeignkeys();
        HashMap<String, ArrayList> values = new HashMap();
        for (String key : keys) {
            values.put(key, model.getColumn(key));
        }
        return values;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="sets">
    public void setTableName(String TableName) {
        tablename = TableName;
    }

    public void setequations(HashMap<String, String> newvalues) {
        Equations.clear();
        if (newvalues != null) {
            Equations.putAll(newvalues);
        }
    }
    //</editor-fold>

    public void GenerateModel() {
        model = new WizardTableModel();
        Statement stmt = null;
        Statement stmt2 = null;
        Connection c = null;
        model.settablename(tablename);
        table.setModel(model);

        try {
            c = Global.getConnectionPool().getConnection();
            HashMap<String, HashMap<String, String>> fkeys = new HashMap();
            stmt = c.createStatement();
            ResultSet results;

            results = stmt.executeQuery("SELECT * FROM " + tablename);

            ResultSetMetaData resultsdata = results.getMetaData();//metadata about this specific table
            List<String> columnlist = new ArrayList();
            DatabaseMetaData db = c.getMetaData();//metadata about the entire database
            String[] tablearray = tablename.split("\\.");
            ResultSet rstemp = db.getPrimaryKeys(null, tablearray[0], tablearray[1]);
            String primary = "";
            while (rstemp.next()) {
                primary = rstemp.getString("COLUMN_NAME");
            }
            model.setprimary(primary);
            ResultSet keys = db.getImportedKeys(null, tablearray[0], tablearray[1]);//creates a result set of information on the foreginkeys for this table

            for (int x = 1; x <= resultsdata.getColumnCount(); x++) {
                columnlist.add(resultsdata.getColumnName(x));//creates a list of columns in the table
                String classname = resultsdata.getColumnClassName(x);
                switch (classname) {
                    case "java.sql.Clob":
                        model.addColumn(columnlist.get(x - 1), String.class);
                        break;
                    case "java.lang.Integer":
                        model.addColumn(columnlist.get(x - 1), Integer.class);
                        break;
                    case "java.lang.Double":
                        model.addColumn(columnlist.get(x - 1), Double.class);
                        break;
                    case "java.sql.Date":
                        model.addColumn(columnlist.get(x - 1), Date.class);
                        table.setDefaultEditor(Date.class, new DefaultCellEditor(new Prototype.StaticClasses.DateTextField()));
                        break;
                    default:
                        model.addColumn(columnlist.get(x - 1));
                        break;
                }

            }
            while (keys.next()) {//go through list of foreign keys one by one
                if (columnlist.contains(keys.getString("FKCOLUMN_NAME"))) {
                    String pkey = keys.getString("PKCOLUMN_NAME");//get the name of the linked table
                    String ptablename = keys.getString("PKTABLE_NAME");//get the primary key of the linked table
                    model.addforeignkey(keys.getString("FKCOLUMN_NAME"), "TABLES," + ptablename);

                    stmt2 = c.createStatement();
                    ResultSet ptable = stmt2.executeQuery("SELECT " + pkey + ", " + namecolumn + " FROM " + Global.getcurrentschema() + "." + ptablename + ";");
                    //puts together a list of values for the foreign key to use a dropdown selection later
                    HashMap<String, String> temp = new HashMap();
                    while (ptable.next()) {
                        temp.put(ptable.getString(pkey), ptable.getString(pkey) + ": " + ptable.getString(namecolumn));
                    }
                    fkeys.put(keys.getString("FKCOLUMN_NAME"), temp);
                    stmt2.close();
                }
            }

            while (results.next()) {
                List<Object> rowdata = new ArrayList();
                HashMap fkey = new HashMap();
                for (int x = 1; x <= resultsdata.getColumnCount(); x++) {
                    String colclass = resultsdata.getColumnClassName(x);
                    if (fkeys.containsKey(resultsdata.getColumnName(x))) {
                        colclass = "FOREIGNKEY";
                        fkey = fkeys.get(resultsdata.getColumnName(x));
                    }
                    switch (colclass) {
                        case "java.sql.Clob":
                            rowdata.add(results.getString(x));
                            break;
                        case "java.lang.Integer":
                            Integer tempint = results.getInt(x);
                            if (results.wasNull()) {
                                tempint = null;
                            }
                            rowdata.add(tempint);
                            break;
                        case "java.lang.Double":
                            Double tempfloat = results.getDouble(x);
                            if (results.wasNull()) {
                                tempfloat = null;
                            }
                            rowdata.add(tempfloat);
                            break;
                        case "FOREIGNKEY":
                            String tempstring = results.getString(x);
                            if (fkey.containsKey(tempstring)) {
                                rowdata.add(fkey.get(tempstring));
                            } else if (tempstring == null) {
                                rowdata.add("");
                            } else {
                                rowdata.add(tempstring);
                            }
                            break;
                        default:
                            rowdata.add(results.getObject(x));
                            break;
                    }
                }
                model.addRow(rowdata);
                model.SetColumnEdit(0, false);
            }

            model.setnamecolumn(this.namecolumn);

            table.getModel().addTableModelListener(new TableModelListener() {
                @Override
                public void tableChanged(TableModelEvent e) {
                    JTable table = getTable();
                    table.setModel(getModel());
                }
            });

            for (int x = 0; x < table.getColumnCount(); x++) {
                String columnname = table.getColumnName(x);

                if (fkeys.containsKey(columnname)) {
                    TableColumn tcolumn = table.getColumnModel().getColumn(x);

                    ArrayList<String> listofkeys = new ArrayList(fkeys.get(columnname).values());
                    Collections.sort(listofkeys);
                    JComboBox combobox = new JComboBox(listofkeys.toArray());
                    combobox.insertItemAt("", 0);
                    tcolumn.setCellEditor(new DefaultCellEditor(combobox));
                }
            }

            stmt.close();
            table.setCellSelectionEnabled(true);
            ListSelectionModel cellSelectionModel = table.getSelectionModel();
            cellSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.getColumnModel().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.getColumnModel().setColumnSelectionAllowed(true);

        } catch (Exception e) {
            Global.Printmessage("WizardTableModel.GenerateModel " + e.getClass().getName() + ": " + e.getMessage());
            Global.Printmessage(this.tablename);
        } finally {
            try {
                if (stmt != null && !stmt.isClosed()) {
                    stmt.close();
                }
                if (stmt2 != null && !stmt2.isClosed()) {
                    stmt2.close();
                }
                if (c != null && !c.isClosed()) {
                    c.close();
                }
            } catch (Exception e) {
                Global.Printmessage("DataNode.GenerateModel " + e.getClass().getName() + ": " + e.getMessage());
                Global.Printmessage(this.tablename);
            }
        }
    }

    public JPanel getpanel() {
        panel = new JPanel();
        MigLayout layout = new MigLayout();
        panel.setLayout(layout);
        getTable();
        table.setRowHeight(28);
        table.setRowMargin(0);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setAutoCreateColumnsFromModel(false);

        for (int col = 0; col < table.getColumnCount(); col++) {
            table.getColumnModel().getColumn(col).setPreferredWidth(120);
        }
        JScrollPane pane = new JScrollPane(table);
        table.setShowGrid(true);
        String[] tableinfo = tablename.split("\\.");
        if (tableinfo.length > 1) {
            panel.add(new JLabel("Current Project: " + tableinfo[0]), "wrap, span");
            panel.add(new JLabel("Current Table: " + tableinfo[1]), "wrap, span");
        } else {
            panel.add(new JLabel("Current Table: " + tablename), "wrap, span");
        }
        panel.add(pane, "dock east");

        Rows = new JSpinner();
        SpinnerNumberModel numbermodel = new SpinnerNumberModel();
        numbermodel.setMaximum(100);
        numbermodel.setMinimum(1);
        numbermodel.setStepSize(1);
        numbermodel.setValue(1);
        Rows.setModel(numbermodel);

        JButton AddRows = new JButton();
        AddRows.setText("Add Rows:");
        AddRows.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                int number = (int) Rows.getValue();
                for (int x = 0; x < number; x++) {
                    model.addBlankRow();
                }
                table.setModel(model);
                table.revalidate();
            }
        });
        panel.add(AddRows, "width 140");
        panel.add(Rows, "width 100, wrap");
        JButton DeleteRow = new JButton();
        DeleteRow.setText("Delete Row");
        DeleteRow.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {

                int index = table.getSelectedRow();
                if (index > -1) {
                    model.RemoveRow(index);
                    table.setModel(model);
                    table.repaint();
                    table.revalidate();
                }
            }
        });
        panel.add(DeleteRow, "width 140,wrap");

        JButton AddKeys = new JButton();
        AddKeys.setText("Add Values");
        AddKeys.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                int column = table.getSelectedColumn();
                String columnname = model.getColumnName(column);
                String linkedtable = model.getlinkedtable(columnname);
                if (linkedtable != null && !linkedtable.isEmpty()) {
                    TextForm form = new TextForm("Add Values");
                    form.addmultilinefield("Please put each value to add on a separate line. Corresponding rows will be added to " + linkedtable + " for each new value", "");
                    ArrayList<ArrayList<String>> result = form.show();
                    if (result != null) {
                        String resultstring = result.get((0)).get(2);
                        String[] newvalues = resultstring.split("\\n");
                        WizardTableNode target = home.getWizardTable(linkedtable);
                        target.addNameColumnValues(newvalues);
                        table.setModel(model);
                        table.repaint();
                        table.revalidate();
                    }
                }
            }
        });

        panel.add(AddKeys, "width 140,wrap");

        JLabel label = new JLabel("<html>The columns with dropdown lists<br/>"
                                + "pull their values from other tables.<br/>"
                                + "To add to those tables click the Add Values<br/>"
                                + "button with the column selected.</html>");
        label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        panel.add(label, "wrap, span");
        return panel;
    }

    private WizardTableModel getModel() {
        GenerateModel();

        return model;
    }

    public void addNameColumnValues(String[] values) {
        for (String value : values) {
            model.addNameColumnValue(value);
        }
    }

    static class DateRenderer extends DefaultTableCellRenderer {

        java.text.DateFormat formatter;

        public DateRenderer() {
            super();
        }

        public void setValue(Object value) {
            if (formatter == null) {
                formatter = java.text.DateFormat.getDateInstance();
            }
            setText((value == null) ? "" : formatter.format(value));
        }
    }
}
