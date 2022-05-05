/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Prototype.ReportBuilding;

import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JDialog;
import javax.swing.JTable;
import org.jdom2.Element;
import Prototype.Popups.NameForm;
import Prototype.StaticClasses.Global;
import Prototype.StaticClasses.TableHandling;
import Prototype.StaticClasses.XMLHandling;
import Prototype.WizardBuilding.ConditionEditor;
import java.awt.event.ActionListener;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.tree.DefaultMutableTreeNode;
import net.miginfocom.swing.MigLayout;
import javax.swing.JTree;
import javax.swing.JLabel;

/**
 *
 * @author HP Owner
 */
public class ReportBuilder extends javax.swing.JPanel implements Prototype.Main.SaveInterface{

    private String Mode;
    private String Schema;
    private String CurrentTable;
    private HashMap<String, ArrayList<String>> Columns;
    private String savedname = "";
    private String currentname = "";
    private Element tablexml;
    private JTable table;
    private JTable reportlist;
    private ReportDemoModel tablemodel;
    private Element condition;
    private boolean mainlist = true;
    private ArrayList<JButton> ReportControls = new ArrayList();
    private JTree treTables;
    private boolean saved = true;
    private JLabel lblReport = new JLabel();
    private JLabel lblTable = new JLabel();
    private JLabel lblCondition = new JLabel();
    private JButton btnLoadReport;
    private JScrollPane treepane;

    public void Startup() {
        Columns = new HashMap();
        Mode = Global.getMode();
        Schema = Global.getcurrentschema();
        TableHandling.loadtables();
        condition = new Element("CONDITION");
        ControlPanel.removeAll();
        ControlPanel.setLayout(new MigLayout("wrap 2, width 200,ins 0"));

//<editor-fold defaultstate="collapsed" desc="Controls Above Tree">
        JButton btnNewReport = new JButton("New Report");
        ControlPanel.add(btnNewReport, "width 100%");

        btnNewReport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newreport();
            }
        });

        btnLoadReport = new JButton("Load Report");
        ControlPanel.add(btnLoadReport, "width 100%");
        btnLoadReport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (mainlist) {
                    int selection = reportlist.getSelectedRow();
                    if (selection > -1) {
                        String reportname = reportlist.getValueAt(selection, 0).toString();
                        loadreport(reportname);
                    }
                }
            }
        });

        JButton btnDelReport = new JButton("Delete Report");
        ControlPanel.add(btnDelReport, "width 100%");
        btnDelReport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (mainlist) {
                    int selection = reportlist.getSelectedRow();
                    if (selection > -1) {
                        String reportname = reportlist.getValueAt(selection, 0).toString();
                        delete(reportname);
                    }
                } else {
                    delete(savedname);
                }
                loadmain();

            }
        });

        //controls for the individual reports
        JButton btnChangeReport = new JButton("Change Report");
        ControlPanel.add(btnChangeReport, "width 100%");
        ReportControls.add(btnChangeReport);
        btnChangeReport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadmain();
            }
        });

        
        JButton btnSave = new JButton("Save Changes");
        ControlPanel.add(btnSave, "width 100%");
        ReportControls.add(btnSave);
        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Save();
            }
        });

        JButton btnRename = new JButton("Rename Report");
        ControlPanel.add(btnRename, "width 100%");
        ReportControls.add(btnRename);
        btnRename.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setname();
            }
        });
JButton btnEditCondition = new JButton("Edit Row Restrictions");
        ControlPanel.add(btnEditCondition, "width 100%, span, wrap");
        ReportControls.add(btnEditCondition);
        btnEditCondition.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EditCondition();
            }
        });

//</editor-fold>
        treTables = new JTree();
        treepane = new JScrollPane(treTables);
        ControlPanel.add(treepane, "width 100%, height 250, span, wrap");
        javax.swing.tree.DefaultTreeModel model = (javax.swing.tree.DefaultTreeModel) treTables.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        root.removeAllChildren();
        root.setUserObject("Tables");
        model.reload(root);
        treTables.getSelectionModel().setSelectionMode(javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION);

//<editor-fold defaultstate="collapsed" desc="Controls Below Tree">
        JButton btnAddColumn = new JButton("Add Column");
        ControlPanel.add(btnAddColumn, "width 100%, span, wrap");
        ReportControls.add(btnAddColumn);
        btnAddColumn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (tablemodel != null) {
                    if (treTables.getLastSelectedPathComponent() != null) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) treTables.getLastSelectedPathComponent();
                        if (node.isLeaf() && node.getLevel() == 2) {
                            DefaultMutableTreeNode parentnode = (DefaultMutableTreeNode) node.getParent();
                            String Column = node.toString();
                            String Table = parentnode.toString();
                            tablemodel.addRow(Arrays.asList(Column, Table, ""));
                            saved = false;
                        }
                    }
                }
            }
        });

        JButton btnRemoveColumn = new JButton("Remove Column");
        ControlPanel.add(btnRemoveColumn, "width 100%, span, wrap");
        ReportControls.add(btnRemoveColumn);
        btnRemoveColumn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (tablemodel != null) {
                    saved = false;
                    tablemodel.removeRow(table.getSelectedRow());
                }
            }
        });

        JButton btnMoveLeft = new JButton("Move Column Left");
        ControlPanel.add(btnMoveLeft, "width 100%, span, wrap");
        ReportControls.add(btnMoveLeft);
        btnMoveLeft.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (tablemodel != null) {
                    int index = table.getSelectedRow();

                    if (tablemodel.moveleft(index)) {
                        table.setRowSelectionInterval(index - 1, index - 1);
                        saved = false;
                    }

                }
            }
        });

        JButton btnMoveRight = new JButton("Move Column Right");
        ControlPanel.add(btnMoveRight, "width 100%, span, wrap");
        ReportControls.add(btnMoveRight);
        btnMoveRight.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (tablemodel != null) {
                    int index = table.getSelectedRow();
                    if (tablemodel.moveright(index)) {
                        table.setRowSelectionInterval(index + 1, index + 1);
                        saved = false;
                    }
                }
            }
        });
//</editor-fold>

        loadmain();

        ControlPanel.repaint();
        ControlPanel.revalidate();
    }

    private void SetControls(boolean enabled) {
        for (JButton button : ReportControls) {
            button.setEnabled(enabled);
        }
        btnLoadReport.setEnabled(!enabled);
        treepane.revalidate();
    }

    private void loadmain() {
        boolean change = true;
        if (!mainlist && !saved) {
            int answer = JOptionPane.showConfirmDialog(null, "Would you like to save changes before switching reports?", "Save Changes", JOptionPane.YES_NO_CANCEL_OPTION);
            if (answer != JOptionPane.CANCEL_OPTION) {
                if (answer == JOptionPane.YES_OPTION) {
                    Save();
                }
                saved = true;
            } else {
                change = false;
            }
        }
        if (change) {
            reportlist = new JTable();
            reportlist.setRowHeight(28);
            reportlist.setRowMargin(0);
            reportlist.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            JScrollPane pane = new JScrollPane(reportlist);
            reportlist.setShowGrid(true);
            javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel();
            model.addColumn("Report Name");
            model.addColumn("Base Table");
            model.addColumn("Number of Columns");
            Element Reports = XMLHandling.getpath(new String[]{Global.getMode() + "S", Schema, "REPORTS"}, Global.getxmlfilename());

            if (Reports != null) {
                for (Element child : Reports.getChildren()) {
                    String reportname = child.getName();
                    String tablename = child.getAttributeValue("value");
                    int columncount = child.getChild("COLUMNS").getChildren().size();
                    model.addRow(new Object[]{reportname, tablename, columncount});
                }
            }
            reportlist.setModel(model);
            TablePanel.removeAll();
            TablePanel.setLayout(new MigLayout("Wrap 1"));
            TablePanel.add(pane, "width 100%");
            TablePanel.repaint();
            TablePanel.revalidate();
            savedname = "";
            currentname = "";
            tablexml = null;
            SetControls(false);
            javax.swing.tree.DefaultTreeModel treemodel = (javax.swing.tree.DefaultTreeModel) treTables.getModel();
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) treemodel.getRoot();
            root.removeAllChildren();
            root.setUserObject("Tables");
            treemodel.reload(root);
            treTables.setModel(treemodel);
            Columns.clear();
            pane.repaint();
            pane.revalidate();
            mainlist = true;

        }
    }

    private void newreport() {
        String hometable = Prototype.Popups.TextForm.choosetabledialog(Schema);
        if(hometable!=null)
        {Element Table = Prototype.StaticClasses.XMLHandling.getpath(new String[]{Global.getMode() + "S", Schema, "TABLES", hometable}, Global.getxmlfilename());
        if (Table != null) {
            setname();
            CurrentTable = Table.getName();
            table = new JTable();
            tablemodel = new ReportDemoModel();
            table.setModel(tablemodel);
            table.setRowHeight(28);
            table.setRowMargin(0);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            JScrollPane pane = new JScrollPane(table);
            table.setShowGrid(true);
            TablePanel.setLayout(new MigLayout("wrap 1"));
            TablePanel.removeAll();
            lblReport.setText("Current Report: " + currentname);
            lblTable.setText("Current Table: " + CurrentTable);
            lblCondition.setText("Current Row Restriction: ");
            TablePanel.add(lblReport, "width 100%");
            TablePanel.add(lblTable, "width 100%");
            TablePanel.add(lblCondition, "width 100%");

            TablePanel.add(pane, "width 100%");
            TablePanel.repaint();
            TablePanel.revalidate();
            table.repaint();
            table.revalidate();
            tablexml = Table;
            getColumns();
            loadtree();
            condition = new Element("CONDITION");
            saved = false;
            SetControls(true);
            mainlist = false;
        }
    }
    }
    private void loadreport(String ReportName) {
        Element reportxml = XMLHandling.getpath(new String[]{Global.getMode() + "S", Schema, "REPORTS", ReportName}, Global.getxmlfilename());

        savedname = reportxml.getName();
        currentname = savedname;
        HashMap<String, String> map = Global.makecommandmap(reportxml);
        String tablename = map.get("TABLE");
        tablexml = XMLHandling.getpath(new String[]{Mode + "S", Schema, "TABLES", tablename}, Global.getxmlfilename());
        CurrentTable = tablexml.getName();
        getColumns();

        Element tablecolumns = reportxml.getChild("COLUMNS");
        if (tablecolumns == null) {
            tablemodel = new ReportDemoModel();
        } else {
            tablemodel = new ReportDemoModel(tablecolumns);
        }

        table = new JTable();
        table.setModel(tablemodel);
        table.setRowHeight(28);
        table.setRowMargin(0);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane pane = new JScrollPane(table);
        table.setShowGrid(true);
        TablePanel.setLayout(new MigLayout("wrap 1"));
        TablePanel.removeAll();

        lblReport.setText("Current Report: " + currentname);
        lblTable.setText("Current Table: " + CurrentTable);
        String conditionstring = "";
        if (reportxml.getChild("CONDITION") != null) {
            condition = reportxml.getChild("CONDITION");
            if (condition.getAttributeValue("value") != null) {
                conditionstring = condition.getAttributeValue("value");
                conditionstring = conditionstring.replace("`", "");
            }
        } else {
            condition = new Element("CONDITION");
        }

        lblCondition.setText(
                "Current Row Restriction: " + conditionstring);
        TablePanel.add(lblReport,
                "width 100%");
        TablePanel.add(lblTable,
                "width 100%");
        TablePanel.add(lblCondition,
                "width 100%");

        TablePanel.add(pane,
                "width 100%");

        TablePanel.repaint();

        TablePanel.revalidate();

        table.repaint();

        table.revalidate();

        loadtree();

        SetControls(
                true);
        mainlist = false;

    }

    private void loadtree() {
        if (Columns != null && !Columns.isEmpty()) {
            javax.swing.tree.DefaultTreeModel model = (javax.swing.tree.DefaultTreeModel) treTables.getModel();
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
            root.setUserObject("Tables");
            root.removeAllChildren();
            for (String tablename : Columns.keySet()) {
                DefaultMutableTreeNode tablenode = new DefaultMutableTreeNode(tablename);
                for (String column : Columns.get(tablename)) {
                    DefaultMutableTreeNode columnnode = new DefaultMutableTreeNode(column);
                    tablenode.add(columnnode);
                }
                root.add(tablenode);
            }
            treTables.getSelectionModel().setSelectionMode(javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION);

            model.reload(root);
        }
    }

    private void setname() {
        ArrayList<String> takennames = new ArrayList();
        Element reports = XMLHandling.getpath(new String[]{Schema, "REPORTS"}, Global.getxmlfilename());
        if (reports != null) {
            for (Element report : reports.getChildren()) {
                takennames.add(report.getName());
            }
        }
        takennames.remove(savedname);
        JDialog dialog = new JDialog();
        NameForm form = new NameForm(takennames, dialog, "Please select a new name for the report.", true, currentname);
        dialog.add(form);
        dialog.pack();
        dialog.validate();
        dialog.setModal(true);
        dialog.setVisible(true);
        currentname = form.getresult();
        lblReport.setText("Current Report: " + currentname);
    }

    private void getColumns() {
        Columns.clear();
        ArrayList<String> currentcolumns = TableHandling.getcolumnnames(CurrentTable);
        Columns.put(CurrentTable, currentcolumns);
        HashMap<String, String> tablelinks = TableHandling.getlinkedtables(CurrentTable);
        for (String columnname : tablelinks.keySet()) {

            String tablename = tablelinks.get(columnname);
            if (tablename.contains(",")) {
                tablename = tablename.substring(0, tablename.indexOf(","));
            }
            if (!Columns.containsKey(tablename)) {
                getColumns(tablename);
            }
        }

    }

    private void getColumns(String Table) {
        if (!Columns.containsKey(Table)) {
            ArrayList<String> currentcolumns = TableHandling.getcolumnnames(Table);
            Columns.put(Table, currentcolumns);
            HashMap<String, String> tablelinks = TableHandling.getlinkedtables(Table);
            for (String columnname : tablelinks.keySet()) {
                String tablename = tablelinks.get(columnname);
                if (tablename.contains(",")) {
                    tablename = tablename.substring(0, tablename.indexOf(","));
                }

                if (!Columns.containsKey(tablename)) {
                    getColumns(tablename);
                }
            }
        }
    }

    private void delete(String ReportName) {
        int answer = JOptionPane.showConfirmDialog(null, "Are you sure you wish to delete this report?", "Delete", JOptionPane.YES_NO_CANCEL_OPTION);
        if (answer != JOptionPane.CANCEL_OPTION) {
            if (answer == JOptionPane.YES_OPTION) {
                saved = true;
                XMLHandling.deletepath(new String[]{Global.getMode() + "S", Schema, "REPORTS", ReportName}, Global.getxmlfilename());
                loadmain();
            }
        }
    }

    private void Save() {
        Element reportfinal = new Element(currentname);
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        reportfinal.addContent(condition.detach());
        reportfinal.addContent(tablemodel.save().detach());
        reportfinal.setAttribute("value", "TABLE:" + CurrentTable);
        if (!savedname.equals(currentname)) {
            XMLHandling.deletepath(new String[]{Mode + "S", Schema, "REPORTS", savedname}, Global.getxmlfilename());
        }
        XMLHandling.addpath(new String[]{Mode + "S", Schema, "REPORTS"}, reportfinal, Global.getxmlfilename(), true);
        JOptionPane.showMessageDialog(null, "Report Saved.");
        saved = true;
    }

    private void EditCondition() {
        if (currentname != null) {
            JDialog dialog = new JDialog();
            ConditionEditor newpanel;
            if (condition != null) {
                newpanel = new ConditionEditor(dialog, Columns, condition);
            } else {
                newpanel = new ConditionEditor(dialog, Columns);
            }

            dialog.setTitle("Edit Row Restriction.");
            dialog.add(newpanel);
            dialog.setModal(true);
            dialog.pack();
            dialog.setVisible(true);
            Element Condition = newpanel.getcondition();
            if (Condition != null) {
                condition = Condition;
            }
            String conditionstring = condition.getAttributeValue("value");
            conditionstring = conditionstring.replace("`", "");
            lblCondition.setText("Current Row Restriction: " + conditionstring);

        }
    }

    /**
     * Creates new form ReportBuilder
     */
    public ReportBuilder() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        TablePanel = new javax.swing.JPanel();
        ControlPanel = new javax.swing.JPanel();

        setMaximumSize(new java.awt.Dimension(980, 591));
        setMinimumSize(new java.awt.Dimension(980, 591));
        setPreferredSize(new java.awt.Dimension(980, 591));

        TablePanel.setMaximumSize(new java.awt.Dimension(575, 591));
        TablePanel.setMinimumSize(new java.awt.Dimension(575, 591));
        TablePanel.setName(""); // NOI18N

        javax.swing.GroupLayout TablePanelLayout = new javax.swing.GroupLayout(TablePanel);
        TablePanel.setLayout(TablePanelLayout);
        TablePanelLayout.setHorizontalGroup(
            TablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 740, Short.MAX_VALUE)
        );
        TablePanelLayout.setVerticalGroup(
            TablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 622, Short.MAX_VALUE)
        );

        ControlPanel.setMaximumSize(new java.awt.Dimension(228, 591));
        ControlPanel.setMinimumSize(new java.awt.Dimension(228, 591));

        javax.swing.GroupLayout ControlPanelLayout = new javax.swing.GroupLayout(ControlPanel);
        ControlPanel.setLayout(ControlPanelLayout);
        ControlPanelLayout.setHorizontalGroup(
            ControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 228, Short.MAX_VALUE)
        );
        ControlPanelLayout.setVerticalGroup(
            ControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 591, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(ControlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(TablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(TablePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ControlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ControlPanel;
    private javax.swing.JPanel TablePanel;
    // End of variables declaration//GEN-END:variables

@Override
    public boolean IsSaved() {
        return saved;
    }

    @Override
    public int SaveCheck() {
        int answer = JOptionPane.showConfirmDialog(null, "Would you like to save changes before exiting?", "Save Changes", JOptionPane.YES_NO_CANCEL_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            Save();
        }
        return answer;
    }
}
