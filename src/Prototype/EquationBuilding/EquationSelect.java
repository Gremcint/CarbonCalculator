/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Prototype.EquationBuilding;

import Prototype.StaticClasses.Global;
import Prototype.StaticClasses.XMLHandling;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jdom2.Element;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 *
 * @author Gregory
 */
public class EquationSelect extends javax.swing.JPanel {

    /**
     * Creates new form EquationSelect
     */
    private String mode;
    private String Schema;
    private Element chosenTable;
    private Element chosenEquation;
    private Element chosenVersion;
    private String result = "CANCEL";
    private JDialog Parent;

    /**
     * Creates new form EquationSelect
     *
     * @param Parent: The JDialog that houses the selection window.
     */
    public EquationSelect(JDialog Parent) {
        initComponents();
        this.Parent = Parent;
        mode = Global.getMode();
        Schema = Global.getcurrentschema();
        btnOk.setEnabled(false);
        btnNewEq.setEnabled(false);
        btnNewVer.setEnabled(false);

        if (mode.equals(Global.PROJECTMODE)) {
            DefaultListModel model = new DefaultListModel();
            model.addElement("Not Applicable");
            VersionList.setModel(model);
            VersionList.setEnabled(false);

        }
        LoadTables();
        TableList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                Object choice = TableList.getSelectedValue();
                if (choice != null) {
                    switch (Global.getMode()) {
                        case Global.PROJECTMODE: {
                            chosenTable = XMLHandling.getpath(new String[]{"PROJECTS", Schema, "TABLES", choice.toString()}, Global.getxmlfilename());
                            chosenEquation = null;
                            btnDelEquation.setEnabled(false);
                            btnDelVersion.setEnabled(false);
                            break;
                        }
                        case Global.TEMPLATEMODE: {
                            chosenTable = XMLHandling.getpath(new String[]{"TEMPLATES", Schema, "TABLES", choice.toString()}, Global.getxmlfilename());
                            VersionList.clearSelection();
                            VersionList.setModel(new DefaultListModel());
                            chosenEquation = null;
                            chosenVersion = null;
                            btnNewVer.setEnabled(false);
                            btnDelEquation.setEnabled(false);
                            btnDelVersion.setEnabled(false);
                            break;
                        }
                    }
                    EquationList.clearSelection();
                    EquationList.setModel(new DefaultListModel());
                    LoadEquations();
                    btnNewEq.setEnabled(true);
                } else {
                    btnNewEq.setEnabled(false);
                    btnNewVer.setEnabled(false);
                }
                btnOk.setEnabled(false);
            }
        });

        EquationList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                Object choice = EquationList.getSelectedValue();
                if (choice != null) {
                    chosenEquation = XMLHandling.getpath(new String[]{"EQUATIONS", choice.toString()}, chosenTable);
                    switch (mode) {
                        case Global.PROJECTMODE:
                            btnOk.setEnabled(true);
                            btnNewEq.setEnabled(true);
                            btnDelEquation.setEnabled(true);
                            btnDelVersion.setEnabled(false);
                            break;
                        case Global.TEMPLATEMODE:
                            VersionList.clearSelection();
                            chosenVersion = null;
                            btnNewVer.setEnabled(true);
                            btnOk.setEnabled(false);
                            VersionList.removeAll();
                            LoadVersions();
                            btnDelEquation.setEnabled(true);
                            btnDelVersion.setEnabled(false);
                            break;
                    }
                } else {
                    VersionList.clearSelection();
                    chosenVersion = null;
                    btnNewVer.setEnabled(false);
                    btnOk.setEnabled(false);
                    VersionList.removeAll();
                }
            }
        });

        VersionList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                Object choice = VersionList.getSelectedValue();
                if (choice != null) {

                    chosenVersion = chosenEquation.getChild("VERSIONS").getChild(choice.toString());
                    btnNewVer.setEnabled(true);
                    btnOk.setEnabled(true);
                    btnDelEquation.setEnabled(true);
                    btnDelVersion.setEnabled(true);
                } else {
                    btnOk.setEnabled(false);
                }
            }
        });
    }

    private void LoadTables() {
        switch (Global.getMode()) {
            case Global.PROJECTMODE:
                ArrayList<String> Tables = Prototype.StaticClasses.SQLHandling.getlistoftables(Schema);
                if (Tables.isEmpty()) {
                } else {
                    TableList.setListData(Tables.toArray());
                }
                break;
            case Global.TEMPLATEMODE:
                Element project = XMLHandling.getpath(new String[]{"TEMPLATES", Schema, "TABLES"}, Global.getxmlfilename());
                ArrayList<String> Tablenames = new ArrayList();
                for (Element current : project.getChildren()) {
                    Tablenames.add(current.getName());
                }
                TableList.setListData(Tablenames.toArray());
                break;
        }
    }

    private void LoadEquations() {
        Element Eq = chosenTable.getChild("EQUATIONS");
        if (Eq != null) {
            ArrayList<String> equations = new ArrayList();
            for (Element child : Eq.getChildren()) {
                equations.add(child.getName());
            }
            EquationList.setListData(equations.toArray());
            EquationList.repaint();
            VersionList.repaint();
            EquationList.revalidate();
            VersionList.revalidate();
        }
    }

    private void LoadVersions() {
        Element Ver = chosenEquation.getChild("VERSIONS");
        if (Ver != null) {
            ArrayList<String> versions = new ArrayList();
            for (Element child : Ver.getChildren()) {
                versions.add(child.getName());
            }
            VersionList.setListData(versions.toArray());
            VersionList.repaint();
        }
    }

    public Element getTable() {
        return chosenTable;
    }

    public String getequation() {
        if (chosenEquation == null) {
            return null;
        }
        return chosenEquation.getName();
    }

    public String getVersion() {
        if (chosenVersion == null) {
            return null;
        }
        return chosenVersion.getName();
    }

    public String getresult() {
        return result;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        TableList = new javax.swing.JList();
        EquationList = new javax.swing.JList();
        VersionList = new javax.swing.JList();
        btnNewVer = new javax.swing.JButton();
        btnNewEq = new javax.swing.JButton();
        btnOk = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        btnDelEquation = new javax.swing.JButton();
        btnDelVersion = new javax.swing.JButton();

        setMaximumSize(new java.awt.Dimension(483, 366));
        setMinimumSize(new java.awt.Dimension(483, 366));

        TableList.setModel(new DefaultListModel());
        TableList.setMaximumSize(new java.awt.Dimension(145, 295));
        TableList.setMinimumSize(new java.awt.Dimension(145, 295));
        TableList.setPreferredSize(new java.awt.Dimension(145, 295));

        EquationList.setModel(new DefaultListModel());
        EquationList.setMaximumSize(new java.awt.Dimension(145, 295));
        EquationList.setMinimumSize(new java.awt.Dimension(145, 295));
        EquationList.setPreferredSize(new java.awt.Dimension(145, 295));

        VersionList.setModel(new DefaultListModel());
        VersionList.setMaximumSize(new java.awt.Dimension(145, 295));
        VersionList.setMinimumSize(new java.awt.Dimension(145, 295));
        VersionList.setPreferredSize(new java.awt.Dimension(145, 295));

        btnNewVer.setText("New Version");
        btnNewVer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewVerActionPerformed(evt);
            }
        });

        btnNewEq.setText("New Equation");
        btnNewEq.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewEqActionPerformed(evt);
            }
        });

        btnOk.setText("OK");
        btnOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOkActionPerformed(evt);
            }
        });

        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        btnDelEquation.setText("Delete Equation");
        btnDelEquation.setEnabled(false);
        btnDelEquation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelEquationActionPerformed(evt);
            }
        });

        btnDelVersion.setText("Delete Version");
        btnDelVersion.setEnabled(false);
        btnDelVersion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelVersionActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnOk, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnNewEq, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(btnDelEquation, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnNewVer, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnDelVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(TableList, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(EquationList, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(VersionList, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 30, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TableList, javax.swing.GroupLayout.PREFERRED_SIZE, 298, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(EquationList, javax.swing.GroupLayout.PREFERRED_SIZE, 298, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(VersionList, javax.swing.GroupLayout.PREFERRED_SIZE, 298, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnOk)
                    .addComponent(btnCancel)
                    .addComponent(btnNewEq)
                    .addComponent(btnNewVer))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnDelEquation)
                    .addComponent(btnDelVersion))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnNewVerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewVerActionPerformed
        result = "NEW VERSION";
        Parent.dispose();
    }//GEN-LAST:event_btnNewVerActionPerformed

    private void btnNewEqActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewEqActionPerformed
        result = "NEW EQUATION";
        Parent.dispose();
    }//GEN-LAST:event_btnNewEqActionPerformed

    private void btnOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOkActionPerformed
        result = "OLD EQUATION";
        Parent.dispose();
    }//GEN-LAST:event_btnOkActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        result = "CANCEL";
        Parent.dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnDelEquationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelEquationActionPerformed
        int index = EquationList.getSelectedIndex();
        if (index != -1) {
            String Equationname = EquationList.getSelectedValue().toString();
            int choice = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete the " + Equationname + " Equation?", "Delete Equation", JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                String Tablename = TableList.getSelectedValue().toString();
                XMLHandling.deletepath(new String[]{Global.getMode() + "S", Schema, "TABLES", Tablename, "EQUATIONS", Equationname}, Tablename);
                LoadEquations();
            }
        }
    }//GEN-LAST:event_btnDelEquationActionPerformed

    private void btnDelVersionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelVersionActionPerformed
        int index = VersionList.getSelectedIndex();
        if (index != -1) {
            String Versionname = VersionList.getSelectedValue().toString();
            String Equationname = EquationList.getSelectedValue().toString();
            int choice = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete " + Versionname + " of the " + Equationname + " Equation?", "Delete Equation", JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                String Tablename = TableList.getSelectedValue().toString();
                XMLHandling.deletepath(new String[]{Global.getMode() + "S", Schema, "TABLES", Tablename, "EQUATIONS", Equationname, "VERSIONS", Versionname}, Tablename);
                LoadVersions();
            }
        }
    }//GEN-LAST:event_btnDelVersionActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList EquationList;
    private javax.swing.JList TableList;
    private javax.swing.JList VersionList;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnDelEquation;
    private javax.swing.JButton btnDelVersion;
    private javax.swing.JButton btnNewEq;
    private javax.swing.JButton btnNewVer;
    private javax.swing.JButton btnOk;
    // End of variables declaration//GEN-END:variables
}
