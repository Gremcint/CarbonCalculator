/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Prototype.DataManaging;

import Prototype.StaticClasses.Global;
import Prototype.StaticClasses.TableHandling;

public class TableEditor extends javax.swing.JPanel implements Prototype.Main.SaveInterface {

    private String Schema;
    private DataTable node;
    private String currenttable;
    private String currentreport;
    private javax.swing.JTable table = new javax.swing.JTable();

    public void Startup() {
        try {
            btnAdd.setEnabled(false);
            btnAdd5.setEnabled(false);
            btnDelete.setEnabled(false);
            btnCopy.setEnabled(false);
            btnExportCurrent.setEnabled(false);
            Schema = Global.getcurrentschema();
            TableHandling.loadtables();
            loadtablelist();
            loadreports();
        } catch (Exception e) {
            Global.Printmessage("Table Editor startup " + e.getClass() + ":" + e.getMessage());
        }
    }

    private void loadreports() {
        org.jdom2.Element Reports = Prototype.StaticClasses.XMLHandling.getpath(new String[]{"PROJECTS", Schema, "REPORTS"}, Global.getxmlfilename());
        if (Reports != null) {
            java.util.ArrayList<String> equations = new java.util.ArrayList();
            for (org.jdom2.Element child : Reports.getChildren()) {
                String reportname = child.getName();
                equations.add(reportname);

            }
            lstReports.setListData(equations.toArray());
        }
    }

    private void loadtablelist() {
        try {
            java.util.ArrayList<String> tables = TableHandling.getlistoftables(Schema);
            javax.swing.DefaultListModel model = new javax.swing.DefaultListModel();
            for (String temptable : tables) {
                model.addElement(temptable);
            }
            lstTables.setModel(model);
        } catch (Exception e) {
            Global.Printmessage("startup loadtablelist " + e.getClass() + ":" + e.getMessage());
        }
    }

    //this is the procedure that will load the table of course
    private void loadtable(String Name) {
        try {
            //this gets the table to be displayed, if it hasn't been prepared
            //or is out of date the model will be generated
            node = TableHandling.getTableNode(Name);
            table = node.getTable();

            //sets up the formatting for the table and sets up a scroll pane
            table.setRowHeight(28);
            table.setRowMargin(0);
            table.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
            table.setAutoCreateColumnsFromModel(false);
            
            DecimalRounder rounder = new DecimalRounder();
            rounder.setHorizontalAlignment(javax.swing.JLabel.RIGHT);
                    
            
            javax.swing.table.DefaultTableCellRenderer EQRenderer = new javax.swing.table.DefaultTableCellRenderer();
            EQRenderer.setHorizontalAlignment(javax.swing.JLabel.RIGHT);
            if (TableHandling.getHighlight()) {
                EQRenderer.setBackground(java.awt.Color.LIGHT_GRAY);
            }
            javax.swing.table.DefaultTableCellRenderer KeyRenderer = new javax.swing.table.DefaultTableCellRenderer();
            KeyRenderer.setHorizontalAlignment(javax.swing.JLabel.LEFT);
            
            DatabaseTableModel model = node.getModel();
            for (int col = 0; col < table.getColumnCount(); col++) {
                table.getColumnModel().getColumn(col).setPreferredWidth(120);
                if (model.isEquation(col)) {
                    table.getColumnModel().getColumn(col).setCellRenderer(EQRenderer);
                } else if (model.getColumnClass(col).equals(Double.class)) {
                    table.getColumnModel().getColumn(col).setCellRenderer(rounder);
                }else if (model.isforeignkey(model.getColumnName(col)))
                {
                    table.getColumnModel().getColumn(col).setCellRenderer(KeyRenderer);
                }
            }
            javax.swing.JScrollPane pane = new javax.swing.JScrollPane(table);
            table.setShowGrid(true);
            TablePanel.setLayout(new java.awt.BorderLayout());
            TablePanel.removeAll();
            TablePanel.add(pane, java.awt.BorderLayout.CENTER);

            TablePanel.repaint();
            TablePanel.revalidate();
            table.repaint();
            table.revalidate();
            table.setAutoCreateRowSorter(true);
        } catch (Exception e) {
            Global.Printmessage("tableeditor loadtable " + e.getClass() + ":" + e.getMessage());
        }
    }

    private void loadreport(String Name) {
        try {
            org.jdom2.Element report = Prototype.StaticClasses.XMLHandling.getpath(new String[]{"PROJECTS", Schema, "REPORTS", Name}, Global.getxmlfilename());
            ReportTableModel model = new ReportTableModel(report);
            table = new javax.swing.JTable(model);
            table.setRowHeight(28); 
            table.setRowMargin(0);
            table.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
            table.setAutoCreateColumnsFromModel(false);
            DecimalRounder rounder = new DecimalRounder();
            rounder.setHorizontalAlignment(javax.swing.JLabel.RIGHT);
                    
            
            javax.swing.table.DefaultTableCellRenderer EQRenderer = new javax.swing.table.DefaultTableCellRenderer();
            EQRenderer.setHorizontalAlignment(javax.swing.JLabel.RIGHT);
            
//            javax.swing.table.DefaultTableCellRenderer KeyRenderer = new javax.swing.table.DefaultTableCellRenderer();
//            KeyRenderer.setHorizontalAlignment(javax.swing.JLabel.LEFT);
            
            for (int col = 0; col < table.getColumnCount(); col++) {
                table.getColumnModel().getColumn(col).setPreferredWidth(120);
                if(model.getColumnClass(col).equals(Double.class))
                {
                    table.getColumnModel().getColumn(col).setCellRenderer(rounder);
                }else if(model.isEquation(col))
                {
                    table.getColumnModel().getColumn(col).setCellRenderer(EQRenderer);
                }
            }
            javax.swing.JScrollPane pane = new javax.swing.JScrollPane(table);
            table.setShowGrid(true);
            TablePanel.removeAll();
            TablePanel.setLayout(new java.awt.BorderLayout());

            TablePanel.add(pane, java.awt.BorderLayout.CENTER);

            TablePanel.repaint();
            TablePanel.revalidate();
            table.repaint();
            table.revalidate();
            table.setAutoCreateRowSorter(true);
        } catch (Exception e) {
            Global.Printmessage("tableeditor loadtable " + e.getClass() + ":" + e.getMessage());
        }
    }

    /**
     * Creates new form TableBuilder
     */
    public TableEditor() {
        super();
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnAdd = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        TablePanel = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        ListPane = new javax.swing.JScrollPane();
        lstTables = new javax.swing.JList();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstReports = new javax.swing.JList();
        btnExportCurrent = new javax.swing.JButton();
        btnCopy = new javax.swing.JButton();
        btnAdd5 = new javax.swing.JButton();
        cbxHighlight = new javax.swing.JCheckBox();
        spnScale = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(800, 676));

        btnAdd.setText("Add Row");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnDelete.setText("Delete Row");
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout TablePanelLayout = new javax.swing.GroupLayout(TablePanel);
        TablePanel.setLayout(TablePanelLayout);
        TablePanelLayout.setHorizontalGroup(
            TablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 590, Short.MAX_VALUE)
        );
        TablePanelLayout.setVerticalGroup(
            TablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        lstTables.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstTablesValueChanged(evt);
            }
        });
        ListPane.setViewportView(lstTables);

        jTabbedPane1.addTab("Tables", ListPane);

        lstReports.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstReportsValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(lstReports);

        jTabbedPane1.addTab("Reports", jScrollPane1);

        btnExportCurrent.setText("Export Current Table");
        btnExportCurrent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportCurrentActionPerformed(evt);
            }
        });

        btnCopy.setText("Copy Table to Clipboard");
        btnCopy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCopyActionPerformed(evt);
            }
        });

        btnAdd5.setText("Add 5 Rows");
        btnAdd5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAdd5ActionPerformed(evt);
            }
        });

        cbxHighlight.setText("Highlight Equation Values");
        cbxHighlight.setToolTipText("");
        cbxHighlight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxHighlightActionPerformed(evt);
            }
        });

        spnScale.setModel(new javax.swing.SpinnerNumberModel(5, 0, 10, 1));
        spnScale.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnScaleStateChanged(evt);
            }
        });

        jLabel1.setText("Decimal Places:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(btnAdd5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnCopy, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnAdd, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnDelete, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnExportCurrent, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jLabel1)
                            .addGap(18, 18, 18)
                            .addComponent(spnScale))
                        .addComponent(cbxHighlight)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(TablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 298, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnAdd)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnAdd5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDelete)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCopy)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnExportCurrent)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbxHighlight)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spnScale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 186, Short.MAX_VALUE))
            .addComponent(TablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void lstTablesValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstTablesValueChanged
        try {
            if (evt.getValueIsAdjusting()) {
                lstReports.clearSelection();
                int index = lstTables.getSelectedIndex();
                if (index > -1) {
                    currentreport = null;
                    lstReports.setSelectedIndex(-1);
                    btnAdd.setEnabled(true);
                    btnAdd5.setEnabled(true);
                    btnDelete.setEnabled(true);
                    btnCopy.setEnabled(true);
                    btnExportCurrent.setEnabled(true);
                    String selection = lstTables.getSelectedValue().toString();
                    if (selection != null && !selection.equals(this.currenttable)) {
                        currenttable = selection;
                        loadtable(currenttable);
                    }
                } else {
                    btnAdd.setEnabled(false);
                    btnAdd5.setEnabled(false);
                    btnDelete.setEnabled(false);
                }
            }
        } catch (Exception e) {
            Global.Printmessage("tableeditor change data " + e.getClass() + ":" + e.getMessage());
        }

    }//GEN-LAST:event_lstTablesValueChanged

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        node.AddBlankRow();
        table.revalidate();
        table.repaint();

    }//GEN-LAST:event_btnAddActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        int index = table.getSelectedRow();
        if (index > -1) {
            node.RemoveRow(index);
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void lstReportsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstReportsValueChanged
        try {
            lstTables.clearSelection();
            if (evt.getValueIsAdjusting()) {
                int index = lstReports.getSelectedIndex();
                if (index > -1) {
                    currenttable = null;
                    lstTables.setSelectedIndex(-1);
                    btnAdd.setEnabled(false);
                    btnAdd5.setEnabled(false);
                    btnDelete.setEnabled(false);
                    btnCopy.setEnabled(true);
                    btnExportCurrent.setEnabled(true);
                    String selection = lstReports.getSelectedValue().toString();
                    if (selection != null && !selection.equals(this.currentreport)) {
                        currentreport = selection;
                        loadreport(currentreport);
                    }
                } else {
                    btnAdd.setEnabled(false);
                    btnAdd5.setEnabled(false);
                    btnDelete.setEnabled(false);
                }
            }
        } catch (Exception e) {
            Global.Printmessage("tableeditor change data " + e.getClass() + ":" + e.getMessage());
        }
    }//GEN-LAST:event_lstReportsValueChanged

    private void btnExportCurrentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportCurrentActionPerformed
        javax.swing.JFileChooser exportcurrent = new javax.swing.JFileChooser();
        javax.swing.filechooser.FileNameExtensionFilter filter = new javax.swing.filechooser.FileNameExtensionFilter("Tab Separated Value [.tsv]", "tsv");
        exportcurrent.setAcceptAllFileFilterUsed(false);
        exportcurrent.setFileFilter(filter);
        int rVal = exportcurrent.showSaveDialog(this);
        String currentexport;
        if (currenttable == null) {
            currentexport = currentreport;
        } else {
            currentexport = currenttable;
        }
        String tabletext = TableHandling.ExportTable(table, currentexport);
        if (rVal == javax.swing.JFileChooser.APPROVE_OPTION) {
            try {
                String filepath = exportcurrent.getSelectedFile().getCanonicalPath();
                if (!filepath.endsWith(".tsv")) {
                    filepath = filepath + ".tsv";
                }
                java.io.FileWriter writer = new java.io.FileWriter(filepath, false);
                writer.write(tabletext);
                writer.close();
                javax.swing.JOptionPane.showMessageDialog(null, "File Created.");
            } catch (Exception e) {
            }
        }
        if (rVal == javax.swing.JFileChooser.CANCEL_OPTION) {
        }
    }//GEN-LAST:event_btnExportCurrentActionPerformed

    private void btnCopyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCopyActionPerformed
        String current;
        if (currenttable == null) {
            current = currentreport;
        } else {
            current = currenttable;
        }
        String copy = TableHandling.ExportTable(table, current);
        java.awt.datatransfer.StringSelection selection = new java.awt.datatransfer.StringSelection(copy);
        java.awt.datatransfer.Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, null);
        javax.swing.JOptionPane.showMessageDialog(null, "Table Copied.");
    }//GEN-LAST:event_btnCopyActionPerformed

    private void btnAdd5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAdd5ActionPerformed
        node.AddBlankRow();
        node.AddBlankRow();
        node.AddBlankRow();
        node.AddBlankRow();
        node.AddBlankRow();
        table.revalidate();
        table.repaint();
    }//GEN-LAST:event_btnAdd5ActionPerformed

    private void cbxHighlightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxHighlightActionPerformed
        Boolean Highlight = cbxHighlight.isSelected();
        TableHandling.setHighlight(Highlight);
        if(node!=null&&table!=null){
        loadtable(currenttable);
        }
    }//GEN-LAST:event_cbxHighlightActionPerformed

    private void spnScaleStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnScaleStateChanged
        int Scale = (int) spnScale.getValue();
        TableHandling.setScale(Scale);
        if(node!=null&&table!=null){
        loadtable(currenttable);
        }
    }//GEN-LAST:event_spnScaleStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane ListPane;
    private javax.swing.JPanel TablePanel;
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnAdd5;
    private javax.swing.JButton btnCopy;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnExportCurrent;
    private javax.swing.JCheckBox cbxHighlight;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JList lstReports;
    private javax.swing.JList lstTables;
    private javax.swing.JSpinner spnScale;
    // End of variables declaration//GEN-END:variables

    @Override
    public boolean IsSaved() {
        return true;
    }

    @Override
    public int SaveCheck() {
        return javax.swing.JOptionPane.YES_OPTION;
    }

    static class DecimalRounder extends javax.swing.table.DefaultTableCellRenderer {

        private static final java.text.DecimalFormat formatter = new java.text.DecimalFormat();

        @Override
        public void setHorizontalAlignment(int Alignment) {
            super.setHorizontalAlignment(Alignment);
        }

        @Override
        public java.awt.Component getTableCellRendererComponent(
                javax.swing.JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            formatter.setMaximumFractionDigits(TableHandling.getScale());
            value = formatter.format((Number) value);

            return super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
        }
    }
}
