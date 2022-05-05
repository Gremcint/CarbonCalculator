/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Prototype.TableBuilding;

import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.table.TableColumn;

import net.miginfocom.swing.MigLayout;
import org.jdom2.Element;

import Prototype.Popups.NameForm;
import Prototype.StaticClasses.Global;
import Prototype.StaticClasses.SQLHandling;
import Prototype.StaticClasses.XMLHandling;
import java.util.Collections;

/**
 *
 * @author Gregory
 */
public class TableBuilder extends javax.swing.JPanel implements Prototype.Main.SaveInterface {

    private String Schema;
    private String Mode;
    private HashMap<String, TableBuilderModel> modellist = new HashMap();
    private TableBuilderModel mainmodel;
    private JPanel mainpanel = new JPanel();
    private JPanel childpanel = new JPanel();
    private String currenttable;
    private HashMap<String, TableBuilderModel> deletedtables = new HashMap();

    //<editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     *
     * Creates new form TableManager
     */
    public TableBuilder() {
        super();
        initComponents();

    }
//</editor-fold>

    public void Startup() {
        modellist = new HashMap();
        mainpanel = new JPanel();
        childpanel = new JPanel();
        deletedtables = new HashMap();
        Schema = Global.getcurrentschema();
        Mode = Global.getMode() + "S";

        MigLayout mainlayout = new MigLayout("wrap 1");
        mainpanel.setLayout(mainlayout);
        ControlPanel.setLayout(new MigLayout());

        JButton btnAddNew = new JButton("New Table");
        btnAddNew.setText("New Table");
        mainpanel.add(btnAddNew, "width 100%");
        btnAddNew.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newtable();
            }
        });
        JButton btnRenameTable = new JButton("Rename Table");
        btnRenameTable.setText("Rename Table");
        mainpanel.add(btnRenameTable, "width 100%");
        btnRenameTable.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                renametable();
            }
        });
        JButton btnDelTable = new JButton("Delete Table");
        mainpanel.add(btnDelTable, "width 100%");
        btnDelTable.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deletetable();
            }
        });
        JButton btnEditCol = new JButton("Edit Columns");
        mainpanel.add(btnEditCol, "width 100%");
        btnEditCol.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editcolumns();
            }
        });

        JButton btnSave = new JButton("Save Changes");
        mainpanel.add(btnSave, "width 100%");
        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                savechanges();
            }
        });

        JButton btnRevert = new JButton("Revert to Last Save");
        mainpanel.add(btnRevert, "width 100%");
        btnRevert.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                revert();
            }
        });

        MigLayout childlayout = new MigLayout("Wrap 1");

        this.childpanel.setLayout(childlayout);
        JButton btnNewColumn = new JButton("New Column");
        childpanel.setSize(ControlPanel.getWidth(), ControlPanel.getHeight());
        childpanel.add(btnNewColumn, "width 100%");
        btnNewColumn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {

                newcolumn();
            }
        });
        JButton btnDelColumn = new JButton("Delete Column");
        childpanel.add(btnDelColumn, "width 100%");
        btnDelColumn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deletecolumn();
            }
        });
        JButton btnAddLink = new JButton("Add Link to Other Table");
        childpanel.add(btnAddLink, "width 100%");
        btnAddLink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addlink();
            }
        });
        JButton btnRenameColumn = new JButton("Rename Column");
        btnRenameColumn.setText("Rename Column");
        childpanel.add(btnRenameColumn, "width 100%");
        btnRenameColumn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                renamecolumn();
            }
        });
        JButton btnGoUp = new JButton("Back to Table List");
        childpanel.add(btnGoUp, "width 100%");
        btnGoUp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadmain();
            }
        });

        loadmain();
    } //Done

    private void loadmain() {//loads the panel to be the controls for handling the list of tables

        loadtablelist();

        ControlPanel.removeAll();
        ControlPanel.repaint();
        mainpanel.revalidate();
        mainpanel.repaint();
        ControlPanel.setLayout(new MigLayout());
        ControlPanel.add(mainpanel);

        ControlPanel.validate();

        TableList.setModel(mainmodel);

        this.currenttable = "*Main*";//I use stars in case the user named a table main
        ControlPanel.repaint();
        ControlPanel.revalidate();
        TableList.repaint();
        TableList.revalidate();
        TableColumn column = TableList.getColumnModel().getColumn(1);

        column.setCellEditor(new DefaultCellEditor(new javax.swing.JTextField()));

    } //Done

    private void loadtablelist() {
        ArrayList<TableChange> oldchanges = new ArrayList();
        if (mainmodel != null) {
            oldchanges.addAll(mainmodel.getchanges());
        }
        mainmodel = new TableBuilderModel("*Main*", "NEW TABLE", "DELETE TABLE");

        mainmodel.addchanges(oldchanges);

        mainmodel.addColumn("Table Name");
        mainmodel.addColumn("Columns");
        mainmodel.addColumn("Name Column");
        mainmodel.addColumn("Links");
        mainmodel.SetColumnEdit(0, false);
        mainmodel.SetColumnEdit(1, false);
        mainmodel.SetColumnEdit(2, false);
        mainmodel.SetColumnEdit(3, false);
        ArrayList<String> tablenames = new ArrayList(modellist.keySet());
        Collections.sort(tablenames);
        for (String tablename : tablenames) {
            TableBuilderModel current = modellist.get(tablename);
            ArrayList<String> newrow = new ArrayList();
            newrow.add(current.getcurrentname());
            newrow.add(String.valueOf(current.getRowCount()));
            newrow.add(current.getnamecolumn());
            String links = "";
            ArrayList<String> linklist = current.getlinks();
            for (String link : linklist) {
                links = links + link + ", ";
            }
            if (!links.isEmpty()) {
                links = links.substring(0, links.length() - 2);
            }
            newrow.add(links);
            mainmodel.addRow(newrow, false);
        }
        if (Global.getMode().equals(Global.PROJECTMODE)) {
            ArrayList<String> tables = SQLHandling.getlistoftables(Schema);
            Collections.sort(tables);
            for (String table : tables) {
                if (!modellist.containsKey(table) && !deletedtables.containsKey(table)) {
                    TableBuilderModel newmodel = new TableBuilderModel(table);
                    newmodel.addColumn("Column Name");
                    newmodel.addColumn("Data Type");
                    newmodel.addColumn("Notes");
                    newmodel.addColumn("Links To");

                    newmodel.SetColumnEdit(0, false);
                    newmodel.SetColumnEdit(1, true);
                    newmodel.SetColumnEdit(2, false);
                    newmodel.SetColumnEdit(3, false);
                    newmodel = getcolumns(table, newmodel);
                    modellist.put(table, newmodel);
                    ArrayList<String> commands = new ArrayList();
                    commands.add("ColCount");
                    commands.add("KeyListString");
                    String path = "PROJECTS," + Schema + ",TABLES," + table;
                    Element current = XMLHandling.getpath(path.split(","), Global.getxmlfilename());
                    HashMap tablevalues = SQLHandling.gettableinfo(table, Schema, commands);
                    HashMap<String, String> tablemap = Global.makecommandmap(current);
                    mainmodel.addRow(Arrays.asList(table, tablevalues.get("ColCount"), tablemap.get("NAMECOLUMN"), tablevalues.get("KeyListString")), false);
                }
            }
        } else if (Global.getMode().equals(Global.TEMPLATEMODE)) {
            String path = "TEMPLATES," + Schema + ",TABLES";
            Element tablelist = XMLHandling.getpath(path.split(","), Global.getxmlfilename());
            XMLHandling.sortchildren(tablelist);
            if (tablelist != null) {
                for (Element current : tablelist.getChildren()) {
                    if (!modellist.containsKey(current.getName()) && !deletedtables.containsKey(current.getName())) {
                        Element Columns = current.getChild("COLUMNS");
                        String KeyListString = "";
                        String table = current.getName();
                        TableBuilderModel newmodel = new TableBuilderModel(table);
                        newmodel.addColumn("Column Name");
                        newmodel.addColumn("Data Type");
                        newmodel.addColumn("Notes");
                        newmodel.addColumn("Links To");

                        newmodel.SetColumnEdit(0, false);
                        newmodel.SetColumnEdit(1, true);
                        newmodel.SetColumnEdit(2, false);
                        newmodel.SetColumnEdit(3, false);
                        newmodel = getcolumns(table, newmodel);
                        modellist.put(table, newmodel);
                        for (Element column : Columns.getChildren()) {
                            String columnstring = column.getAttributeValue("value");
                            HashMap<String, String> columnmap = Global.makecommandmap(columnstring);

                            if (columnmap.containsKey("TYPE") && columnmap.get("TYPE").equals("FOREIGN")) {

                                String link = columnmap.get("LINKTO");
                                if (link != null && !link.isEmpty()) {
                                    if (KeyListString.isEmpty()) {
                                        KeyListString = link;
                                    } else {
                                        KeyListString = KeyListString + ", " + link;
                                    }
                                }
                            }
                        }
                        String ColCount = String.valueOf(Columns.getChildren().size());
                        HashMap<String, String> tablemap = Global.makecommandmap(current);
                        mainmodel.addRow(Arrays.asList(table, ColCount, tablemap.get("NAMECOLUMN"), KeyListString), false);
                    }
                }
            }
        }
        if (mainmodel.getRowCount() == 0) {
            this.newtable();
        }
    } //Done

    private String tablenamepopup(String currentname) {
        ArrayList names = mainmodel.getColumn(0);
        if (currentname != null && !currentname.isEmpty()) {
            names.remove(currentname);
        }
        JDialog dialog = new JDialog();
        String message;
        if (names.isEmpty()) {
            message = "No tables found, please enter name of first table:";
        } else if (currentname != null && !currentname.isEmpty()) {
            message = "Please enter a new name for table " + currentname;
        } else {
            message = "Please enter a name for the table:";
        }
        NameForm nameform = new NameForm(names, dialog, message, true);
        dialog.add(nameform);
        dialog.setSize(380, 160);
        dialog.setTitle("Table Name");
        dialog.setModal(true);
        dialog.setVisible(true);
        String result = nameform.getresult();
        return result;
    }

    private String columnnamepopup(String currentname, String tablename) {
        TableBuilderModel model = modellist.get(tablename);
        ArrayList names = model.getColumn(0);
        if (currentname != null && !currentname.isEmpty()) {
            names.remove(currentname);
        }
        JDialog dialog = new JDialog();
        String message;
        if (currentname != null && !currentname.isEmpty()) {
            message = "Please enter a new name for column " + currentname;
        } else {
            message = "Please enter a name for the column:";
        }
        NameForm nameform = new NameForm(names, dialog, message, true);
        dialog.add(nameform);
        dialog.setSize(370, 150);
        dialog.setTitle("Column Name");
        dialog.setModal(true);
        dialog.setVisible(true);
        String result = nameform.getresult();
        return result;
    }

    private void newtable() {//procedure for adding a new table,

        String result = tablenamepopup(null);
        if (result != null && !result.isEmpty()) {
            mainmodel.addRow(Arrays.asList(result, 2, "NAME", ""), true);
            TableBuilderModel newmodel = new TableBuilderModel(result);
            newmodel.addRow(Arrays.asList(result + "_ID", "INTEGER", "Primary Index", ""), false);
            newmodel.addRow(Arrays.asList("NAME", "STRING", "Name Column", ""), false);
            newmodel.addColumn("Column Name");
            newmodel.addColumn("Data Type");
            newmodel.addColumn("Notes");
            newmodel.addColumn("Links To");

            newmodel.SetColumnEdit(0, false);
            newmodel.SetColumnEdit(1, true);
            newmodel.SetColumnEdit(2, false);
            newmodel.SetColumnEdit(3, false);
            this.modellist.put(result, newmodel);
        }
    } //Done

    private void renametable() {//procedure for adding a new table,
        int selection = TableList.getSelectedRow();
        if (selection >= 0) {
            String table = TableList.getValueAt(selection, 0).toString();
            String result = tablenamepopup(table);
            if (result != null && !result.isEmpty() && !table.equals(result)) {
                mainmodel.setValueAt(result, selection, 0);
                TableBuilderModel model = modellist.get(table);
                modellist.remove(table);
                modellist.put(result, model);
                for (TableBuilderModel currentmodel : modellist.values()) {
                    currentmodel.nameupdate(table, result);
                }
            }
        }
    } //Done

    private void renamecolumn() {//procedure for adding a new table,
        int selection = TableList.getSelectedRow();
        if (selection >= 0) {
            String column = TableList.getValueAt(selection, 0).toString();
            String result = columnnamepopup(column, currenttable);
            if (result != null && !result.isEmpty() && !column.equals(result)) {
                TableBuilderModel model = modellist.get(currenttable);
                model.setValueAt(result, selection, 0);
            }
        }
    } //Done

    private void deletetable() {
        int index = TableList.getSelectedRow();
        if (index != -1) {
            int choice = JOptionPane.showConfirmDialog(null, "Are you sure you wish to delete this table?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                String tablename = TableList.getValueAt(index, 0).toString();
                TableBuilderModel targetmodel = modellist.get(tablename);
                modellist.remove(tablename);
                mainmodel.deleteRow(index);
                deletedtables.put(tablename, targetmodel);
            }
        }
    } //Done

    private void savechanges() {
        //<editor-fold defaultstate="collapsed" desc="Template Mode">
        switch (Global.getMode()) {
            case Global.TEMPLATEMODE:
                //<editor-fold defaultstate="collapsed" desc="Main Model Changes">
                for (TableChange change : mainmodel.getchanges()) {
                    String action = change.getaction();

                    switch (action) {
                        case "DELETE TABLE":
                            String old = change.getoldvalue().toString();
                            String name = old.substring(old.indexOf(":") + 1, old.indexOf(";"));
                            TableBuilderModel deletedmodel = this.deletedtables.get(name);
                            String savedname = deletedmodel.getsavedname();
                            String delpath = Mode + "," + Schema + ",TABLES," + savedname;
                            XMLHandling.deletepath(delpath.split(","), Global.getxmlfilename());
                            break;

                        case "NEW TABLE":
                            String row = change.getnewvalue().toString();
                            String tablename = row.split(";")[0].split(":")[1];
                            Element tablexml = new Element(tablename);
                            Element columns = new Element("COLUMNS");
                            tablexml.addContent(columns);

                            TableBuilderModel current = modellist.get(tablename);
                            if (current == null) {
                                for (TableBuilderModel model : modellist.values()) {
                                    if (model.getsavedname().equals(tablename)) {
                                        current = model;
                                        break;
                                    }
                                }
                            }
                            ArrayList firstrow = current.getRow(0);
                            Element primary = new Element(firstrow.get(0).toString());
                            String tablestring = "PRIMARY:" + firstrow.get(0).toString();
                            primary.setAttribute("value", "TYPE:PRIMARY;");
                            columns.addContent(primary);

                            int rowcount = current.getRowCount();
                            for (int x = 1; x < rowcount; x++) {
                                ArrayList currentrow = current.getRow(x);
                                Element newcolumn = new Element(currentrow.get(0).toString());
                                if (currentrow.get(2).toString().equals("Name Column")) {
                                    tablestring += ";NAMECOLUMN:" + currentrow.get(0).toString();
                                    newcolumn.setAttribute("value", "TYPE:NAMECOLUMN;");
                                }
                                if (!currentrow.get(3).equals("")) {
                                    newcolumn.setAttribute("value", "TYPE:FOREIGN;LINKTO:" + currentrow.get(3).toString() + ";");
                                }
                                if (!newcolumn.hasAttributes()) {
                                    newcolumn.setAttribute("value", "TYPE:" + currentrow.get(1) + ";");
                                }
                                columns.addContent(newcolumn);
                            }
                            tablestring = tablestring.replace(";;", ";");
                            String newtablepath = Mode + "," + Schema;
                            ;
                            newtablepath = newtablepath + ",TABLES";
                            tablexml.setAttribute("value", tablestring);

                            XMLHandling.addpath(newtablepath.split(","), tablexml, Global.getxmlfilename(), true);
                            current.clearchangelist();
                            break;
                        case "CHANGE DATA":
                            String oldname = change.getoldvalue().toString();
                            String newname = change.getnewvalue().toString();
                            String rentablepath = Mode + "," + Schema + ",TABLES," + oldname;
                            XMLHandling.changeElementName(rentablepath.split(","), newname, Global.getxmlfilename());
                            break;
                    }
                }
                //</editor-fold>
                for (TableBuilderModel model : modellist.values()) {
                    ArrayList<TableChange> changes = model.getchanges();
                    String name = model.getcurrentname();
                    for (TableChange change : changes) {
                        String action = change.getaction();
                        String oldvalue = change.getoldvalue().toString();
                        String newvalue = change.getnewvalue().toString();
                        switch (action) {
                            case "NEW COLUMN":
                                HashMap<String, String> newcol = Global.makecommandmap(newvalue);
                                String colname = newcol.get("Column Name");
                                Element newcolumn = new Element(colname);
                                String datatype = newcol.get("Data Type");
                                String links = newcol.get("Links To");
                                if (!links.equals("")) {
                                    newcolumn.setAttribute("value", "TYPE:FOREIGN;LINKTO:" + links);
                                } else {
                                    newcolumn.setAttribute("value", "TYPE:" + datatype);
                                }
                                String newcolpath = Mode + "," + Schema + ",TABLES," + name + ",COLUMNS";
                                XMLHandling.addpath(newcolpath.split(","), newcolumn, Global.getxmlfilename(), true);
                                break;
                            case "DELETE COLUMN":
                                HashMap<String, String> oldcol = Global.makecommandmap(oldvalue);
                                String oldcolname = oldcol.get("Column Name");
                                String delcolpath = Mode + "," + Schema + ",TABLES," + name + ",COLUMNS," + oldcolname;
                                XMLHandling.deletepath(delcolpath.split(","), Global.getxmlfilename());

                                break;

                            case "CHANGE DATA":
                                int index = change.getcol();
                                if (index == 0) {
                                    XMLHandling.changeElementName(new String[]{Mode, Schema, "TABLES", name, "COLUMNS", oldvalue}, newvalue, Global.getxmlfilename());
                                } else if (index == 1) {
                                    String columnname = model.getValueAt(change.getrow(), 0).toString();

                                    String[] rencolpath = {Mode, Schema, "TABLES", name, "COLUMNS", columnname};

                                    Element currentcolumn = XMLHandling.getpath(rencolpath, Global.getxmlfilename());
                                    HashMap<String, String> columnmap = Global.makecommandmap(currentcolumn);
                                    columnmap.put("TYPE", newvalue);
                                    XMLHandling.setattribute(rencolpath, Global.getxmlfilename(), new org.jdom2.Attribute("value", Global.MakeCommandString(columnmap)));
                                }
                                break;
                        }
                    }
                    model.clearchangelist();
                }
                JOptionPane.showMessageDialog(null, "Changes Saved");
                //</editor-fold>
                break;
            case Global.PROJECTMODE:
                Connection c = null;
                Statement stmt;
                try {
                    c = Global.getConnectionPool().getConnection();
                    DatabaseMetaData metadata = c.getMetaData();
                    stmt = c.createStatement();
                    ArrayList<String> posttablequeries = new ArrayList();
                    //<editor-fold defaultstate="collapsed" desc="Main Model Changes">
                    for (TableChange change : mainmodel.getchanges()) {
                        String action = change.getaction();

                        switch (action) {
                            case "DELETE TABLE":
                                String old = change.getoldvalue().toString();
                                String name = old.substring(old.indexOf(":") + 1, old.indexOf(";"));
                                TableBuilderModel deletedmodel = this.deletedtables.get(name);
                                String savedname = deletedmodel.getsavedname();
                                ResultSet table = metadata.getTables(null, Schema, savedname, null);
                                if (table.next()) {
                                    ResultSet Keys = metadata.getExportedKeys(null, Schema, savedname);
                                    if (Keys.next()) {
                                        stmt.executeUpdate("DROP TABLE " + Schema + "." + savedname + " CASCADE;");
                                    } else {
                                        stmt.executeUpdate("DROP TABLE " + Schema + "." + savedname + ";");
                                    }
                                }
                                String path = Mode + "," + Schema + ",TABLES," + savedname;
                                XMLHandling.deletepath(path.split(","), Global.getxmlfilename());
                                break;
                            case "NEW TABLE":
                                String row = change.getnewvalue().toString();
                                String tablename = row.split(";")[0].split(":")[1];
                                Element tablexml = new Element(tablename);
                                Element columns = new Element("COLUMNS");
                                tablexml.addContent(columns);

                                TableBuilderModel current = modellist.get(tablename);
                                if (current == null) {
                                    for (TableBuilderModel model : modellist.values()) {
                                        if (model.getsavedname().equals(tablename)) {
                                            current = model;
                                            break;
                                        }
                                    }
                                }
                                ArrayList firstrow = current.getRow(0);
                                String tablestring = "CREATE TABLE IF NOT EXISTS " + Schema + "." + tablename + "(" + firstrow.get(0) + " " + firstrow.get(1) + " PRIMARY KEY AUTO_INCREMENT";
                                String primaryname = firstrow.get(0).toString();
                                Element primary = new Element(primaryname);
                                String attvalue = "PRIMARY:" + primaryname + ";";

                                primary.setAttribute("value", "TYPE:PRIMARY");
                                columns.addContent(primary);

                                int rowcount = current.getRowCount();
                                for (int x = 1; x < rowcount; x++) {
                                    ArrayList currentrow = current.getRow(x);
                                    Element newcolumn = new Element(currentrow.get(0).toString());
                                    if (currentrow.get(2).toString().equals("Name Column")) {
                                        attvalue = attvalue + "NAMECOLUMN:" + currentrow.get(0).toString() + ";";
                                        newcolumn.setAttribute("value", "TYPE:NAMECOLUMN");
                                    }
                                    if (!currentrow.get(3).equals("")) {
                                        String[] foreignpath = currentrow.get(3).toString().split(",", 2);
                                        posttablequeries.add("ALTER TABLE " + Schema + "." + tablename + " ADD FOREIGN KEY (" + currentrow.get(0).toString() + ") REFERENCES " + foreignpath[0] + "(" + foreignpath[1] + ")");
                                        newcolumn.setAttribute("value", "TYPE:FOREIGN;LINKTO:" + currentrow.get(3).toString() + "," + currentrow.get(0).toString());
                                    }
                                    if (!newcolumn.hasAttributes()) {
                                        newcolumn.setAttribute("value", "TYPE:" + currentrow.get(1));
                                    }
                                    columns.addContent(newcolumn);
                                    String type = currentrow.get(1).toString();
                                    if (type.equals("STRING")) {
                                        type = "CLOB";
                                    }
                                    tablestring = tablestring + ", " + currentrow.get(0).toString() + " " + type;
                                }
                                tablexml.setAttribute("value", attvalue);
                                tablestring = tablestring.replace(", )", ")");
                                if (!tablestring.endsWith(")")) {
                                    tablestring = tablestring + ")";
                                }
                                String newtablepath = Mode + "," + Schema + ",TABLES";

                                XMLHandling.addpath(newtablepath.split(","), tablexml, Global.getxmlfilename(), true);
                                stmt.executeUpdate(tablestring);
                                current.clearchangelist();
                                break;
                            case "CHANGE DATA":
                                String oldname = change.getoldvalue().toString();
                                String newname = change.getnewvalue().toString();
                                String rentablepath = Mode + "," + Schema + ",TABLES," + oldname;
                                XMLHandling.changeElementName(rentablepath.split(","), newname, Global.getxmlfilename());
                                stmt.executeUpdate("ALTER TABLE " + Schema + "." + oldname + " RENAME TO " + newname);
                                break;
                        }
                    }
                    mainmodel.clearchangelist();
                    //</editor-fold>
                    for (TableBuilderModel model : modellist.values()) {
                        ArrayList<TableChange> changes = model.getchanges();
                        String name = model.getcurrentname();
                        String Schemaname = Schema + "." + name;

                        for (TableChange change : changes) {
                            String action = change.getaction();
                            String oldvalue = change.getoldvalue().toString();
                            String newvalue = change.getnewvalue().toString();
                            switch (action) {
                                case "NEW COLUMN":
                                    HashMap<String, String> newcol = Global.makecommandmap(newvalue);
                                    String colname = newcol.get("Column Name");
                                    Element newcolumn = new Element(colname);
                                    String datatype = newcol.get("Data Type");
                                    String links = newcol.get("Links To");
                                    String query;
                                    if (datatype.equals("STRING")) {
                                        query = "ALTER TABLE " + Schemaname + " ADD " + colname + " CLOB";
                                    } else {
                                        query = "ALTER TABLE " + Schemaname + " ADD " + colname + " " + datatype;
                                    }
                                    stmt.execute(query);
                                    if (!links.equals("")) {
                                        String[] foreignpath = links.split(",", 2);

                                        query = "ALTER TABLE " + Schemaname + " ADD FOREIGN KEY (" + colname + ") REFERENCES " + Schema + "." + foreignpath[0] + "(" + foreignpath[1] + ")";
                                        stmt.execute(query);
                                        newcolumn.setAttribute("value", "TYPE:FOREIGN;LINKTO:" + links);
                                    } else {
                                        newcolumn.setAttribute("value", "TYPE:" + datatype);
                                    }
                                    String newcolpath = Mode + "," + Schema + ",TABLES," + name + ",COLUMNS";
                                    XMLHandling.addpath(newcolpath.split(","), newcolumn, Global.getxmlfilename(), true);
                                    break;
                                case "DELETE COLUMN":
                                    HashMap<String, String> oldcol = Global.makecommandmap(oldvalue);
                                    String oldcolname = oldcol.get("Column Name");
                                    String oldlinks = oldcol.get("Links To");
                                    String delquery;
                                    if (!oldlinks.equals("")) {
                                        delquery = "ALTER TABLE " + Schemaname + "DROP FOREIGN KEY " + oldcolname + "";
                                        stmt.execute(delquery);
                                    }

                                    delquery = "ALTER TABLE " + Schemaname + " DROP " + oldcolname;
                                    stmt.execute(delquery);
                                    String delcolpath = Mode + "," + Schema + ",TABLES," + name + ",COLUMNS," + oldcolname;
                                    XMLHandling.deletepath(delcolpath.split(","), Global.getxmlfilename());

                                    break;
                                case "CHANGE DATA":
                                    int index = change.getcol();
                                    String rencolpath = Mode + "," + Schema + ",TABLES," + name + ",COLUMNS,";
                                    if (index == 0) {
                                        stmt.executeUpdate("ALTER TABLE " + Schemaname + " ALTER COLUMN " + oldvalue + " RENAME TO " + newvalue);
                                        rencolpath = rencolpath + oldvalue;
                                        XMLHandling.changeElementName(rencolpath.split(","), newvalue, Global.getxmlfilename());
                                    } else if (index == 1) {
                                        String columnname = model.getValueAt(change.getrow(), 0).toString();
                                        stmt.executeUpdate("ALTER TABLE " + Schemaname + " ALTER COLUMN " + columnname + " " + newvalue);
                                        rencolpath = rencolpath + columnname;

                                        Element currentcolumn = XMLHandling.getpath(rencolpath.split(","), Global.getxmlfilename());
                                        HashMap<String, String> columnmap = Global.makecommandmap(currentcolumn);
                                        columnmap.put("TYPE", newvalue);
                                        XMLHandling.setattribute(rencolpath.split(","), Global.getxmlfilename(), new org.jdom2.Attribute("value", Global.MakeCommandString(columnmap)));
                                    }

                                    break;
                            }
                        }
                        model.clearchangelist();
                    }
                    for (String query : posttablequeries)//doing this prevents trying to create a foreign key before the table exists.
                    {
                        stmt.executeUpdate(query);
                    }
                    JOptionPane.showMessageDialog(null, "Changes Saved");
                } catch (Exception e) {
                    System.out.println(e.getMessage());

                } finally {
                    try {
                        if (c != null && !c.isClosed()) {
                            c.close();
                        }

                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
                break;
        }

    }

    private TableBuilderModel getcolumns(String Table, TableBuilderModel model) {
        switch (Global.getMode()) {
            case Global.PROJECTMODE:
                Connection c = null;
                try {
                    c = Global.getConnectionPool().getConnection();
                    DatabaseMetaData dm = c.getMetaData();
                    ArrayList<String> Columns = new ArrayList();
                    ResultSet columnlist = dm.getColumns(null, Schema, Table, null);
                    while (columnlist.next()) {
                        String columntype = columnlist.getString("TYPE_NAME");
                        String columnname = columnlist.getString("COLUMN_NAME");
                        switch (columntype) {
                            case "CLOB":
                                columntype = "STRING";
                                break;
                            case "DECIMAL":
                                columntype="DOUBLE";
                                break;
                        }
                        Columns.add(columnname);//track where the columns are in the list so we don't have to search during the next parts
                        model.addRow(Arrays.asList(columnname, columntype, "", ""), false);
                    }
                    ResultSet primary = dm.getPrimaryKeys("", Schema, Table);
                    while (primary.next()) {
                        String colname = primary.getString("COLUMN_NAME");
                        int index = Columns.indexOf(colname);
                        model.setValueAt("Primary Index", index, 2, false);
                    }
                    ResultSet foreign = dm.getImportedKeys("", Schema, Table);
                    while (foreign.next()) {
                        String colname = foreign.getString("FKCOLUMN_NAME");
                        String tablename = foreign.getString("PKTABLE_NAME");
                        int index = Columns.indexOf(colname);
                        model.setValueAt("Link Column", index, 2);
                        model.setValueAt(tablename, index, 3);
                    }

                    String[] currentpath = new String[]{"PROJECTS", Schema, "TABLES", Table};
                    Element current = XMLHandling.getpath(currentpath, Global.getxmlfilename());
                    if (current.getAttribute("value") != null && !current.getAttributeValue("value").equals("")) {
                        String tablestring = current.getAttributeValue("value");
                        HashMap<String, String> tablemap = Global.makecommandmap(tablestring);
                        String namecolumn = tablemap.get("NAMECOLUMN");
                        int nameindex = Columns.indexOf(namecolumn);
                        if (nameindex > -1) {
                            model.setValueAt("Name Column", nameindex, 2, false);
                        }
                    }
                } catch (Exception e) {
                    Global.Printmessage("SQLHandling.getlistoftables " + e.getClass().getName() + ": " + e.getMessage());
                } finally {
                    try {
                        if (c != null && !c.isClosed()) {
                            c.close();
                        }

                    } catch (Exception e) {
                        Global.Printmessage("SQLHandling.getlistoftables " + e.getClass().getName() + ": " + e.getMessage());
                    }
                }
                break;
            case Global.TEMPLATEMODE:
                Element current = XMLHandling.getpath(new String[]{"TEMPLATES", Schema, "TABLES", Table}, Global.getxmlfilename());

                HashMap<String, String> tablemap = Global.makecommandmap(current);
                String primarycolumn = tablemap.get("PRIMARY");
                String namecolumn = tablemap.get("NAMECOLUMN");
                Element ColumnsXML = current.getChild("COLUMNS");
                if (primarycolumn == null || ColumnsXML.getChild(primarycolumn) == null) {
                    primarycolumn = "";
                }
                if (namecolumn == null || ColumnsXML.getChild(namecolumn) == null) {
                    namecolumn = "";
                }
                for (Element column : ColumnsXML.getChildren()) {
                    String colname = column.getName();
                    HashMap<String, String> columnmap = Global.makecommandmap(column);
                    String type = "String";
                    if (columnmap.get("TYPE") != null && !columnmap.get("TYPE").isEmpty()) {
                        type = columnmap.get("TYPE");
                    }
                    if (colname.equals(primarycolumn)) {
                        model.addRow(Arrays.asList(colname, type, "Primary Index", ""), false);
                    } else if (colname.equals(namecolumn)) {
                        model.addRow(Arrays.asList(colname, type, "Name Column", ""), false);
                    } else if (type.equals("FOREIGN") && columnmap.containsKey("LINKTO") && !columnmap.get("LINKTO").isEmpty()) {
                        String tablename = columnmap.get("LINKTO");
                        tablename = tablename.replace("," + colname, "");
                        model.addRow(Arrays.asList(colname, type, "Link Column", tablename), false);
                    } else if (type.equals("FOREIGN")) {
                        model.addRow(Arrays.asList(colname, "STRING", "", ""), false);
                    } else if (primarycolumn.isEmpty() && type.equals("PRIMARY")) {
                        primarycolumn = colname;
                        model.addRow(Arrays.asList(colname, type, "Primary Index", ""), false);
                    } else if (namecolumn.isEmpty() && type.equals("NAMECOLUMN")) {
                        namecolumn = colname;
                        model.addRow(Arrays.asList(colname, type, "Name Column", ""), false);
                    } else {
                        model.addRow(Arrays.asList(colname, type, "", ""), false);
                    }
                }

                
                break;
        }
        return model;

    } //Done

    private void editcolumns() {
        int selection = TableList.getSelectedRow();
        if (selection >= 0) {
            String table = TableList.getValueAt(selection, 0).toString();
            if (!modellist.containsKey(table)) {
                TableBuilderModel newmodel = new TableBuilderModel(table);
                newmodel.addColumn("Column Name");
                newmodel.addColumn("Data Type");
                newmodel.addColumn("Notes");
                newmodel.addColumn("Links To");

                newmodel.SetColumnEdit(0, false);
                newmodel.SetColumnEdit(1, true);
                newmodel.SetColumnEdit(2, false);
                newmodel.SetColumnEdit(3, false);
                newmodel = getcolumns(table, newmodel);
                modellist.put(table, newmodel);
            }

            TableList.setModel(modellist.get(table));
            TableColumn tcolumn = TableList.getColumnModel().getColumn(1);
            String[] types = {"STRING", "INTEGER", "DOUBLE", "DATE"};
            JComboBox combobox = new JComboBox(types);
            tcolumn.setCellEditor(new DefaultCellEditor(combobox));
            ControlPanel.removeAll();
            ControlPanel.add(childpanel);
            ControlPanel.repaint();
            ControlPanel.revalidate();

            TableList.repaint();
            TableList.revalidate();

            currenttable = table;
        }
        //column,  data type, notes, 
    } //Done 

    private void revert() {
        modellist.clear();
        mainmodel.clearchangelist();
        mainmodel = null;
        loadmain();
    }//Done    

    private void newcolumn() {
        TableBuilderModel current = modellist.get(currenttable);
        String result = columnnamepopup(null, currenttable);
        if (result != null && !result.isEmpty()) {
            current.addRow(Arrays.asList(result, "STRING", "", ""), true);
            TableList.revalidate();
            TableList.repaint();
        }
    }

    private void deletecolumn() {
        TableBuilderModel current = (TableBuilderModel) this.TableList.getModel();
        int index = TableList.getSelectedRow();
        current.deleteRow(index);
        TableList.revalidate();
    }

    private void addlink() {
        ArrayList<String> TableNames = mainmodel.getColumn(0);
        TableBuilderModel current = modellist.get(currenttable);
        ArrayList<String> links = current.getColumn(3);
        for (String key : links) {
            if (!key.equals(""));
            {
                TableNames.remove(key);
            }
        }
        TableNames.remove(currenttable);
        String linkedtable = Prototype.Popups.TextForm.showoptiondialog(TableNames, "Please select referenced table.");
        if (linkedtable != null) {
            TableBuilderModel target = modellist.get(linkedtable);
            String primaryname = target.getprimarycolumn();
            current.addRow(Arrays.asList(primaryname, "INTEGER", "Link Column", linkedtable + "," + primaryname), true);
            TableList.revalidate();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel7 = new javax.swing.JPanel();
        TableScroll = new javax.swing.JScrollPane();
        TableList = new javax.swing.JTable();
        ControlPanel = new javax.swing.JPanel();

        setPreferredSize(new java.awt.Dimension(995, 694));

        TableList.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        TableList.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Table Name", "Columns", "Name Column", "Links"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class, java.lang.Object.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                true, false, true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        TableList.getTableHeader().setReorderingAllowed(false);
        TableScroll.setViewportView(TableList);

        ControlPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        ControlPanel.setPreferredSize(new java.awt.Dimension(164, 402));

        javax.swing.GroupLayout ControlPanelLayout = new javax.swing.GroupLayout(ControlPanel);
        ControlPanel.setLayout(ControlPanelLayout);
        ControlPanelLayout.setHorizontalGroup(
            ControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 240, Short.MAX_VALUE)
        );
        ControlPanelLayout.setVerticalGroup(
            ControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addComponent(ControlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(TableScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 710, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(26, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(TableScroll, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 628, Short.MAX_VALUE)
                    .addComponent(ControlPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 628, Short.MAX_VALUE))
                .addGap(0, 68, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 995, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 5, Short.MAX_VALUE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 5, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 709, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, 0)
                    .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ControlPanel;
    private javax.swing.JTable TableList;
    private javax.swing.JScrollPane TableScroll;
    private javax.swing.JPanel jPanel7;
    // End of variables declaration//GEN-END:variables

    @Override
    public boolean IsSaved() {
        for (TableBuilderModel currentmodel : modellist.values()) {
            if (currentmodel.haschanges()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int SaveCheck() {
        int answer = JOptionPane.showConfirmDialog(null, "Would you like to save changes before exiting?", "Save Changes", JOptionPane.YES_NO_CANCEL_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            savechanges();
        }
        return answer;
    }
}
