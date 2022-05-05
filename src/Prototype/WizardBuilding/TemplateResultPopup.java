/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Prototype.WizardBuilding;

import Prototype.StaticClasses.Global;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import org.jdom2.Element;

/**
 *
 * @author User
 */
public class TemplateResultPopup extends javax.swing.JPanel {

    boolean complete;
    JDialog parent;
    Element Result;

    /**
     * Creates new form TemplateResultPopup
     */
    public TemplateResultPopup(JDialog Parent, ArrayList<String> variables, Element CurrentResult) {
        initComponents();

        Result = CurrentResult;
        HashMap<String, String> resultmap = Global.makecommandmap(CurrentResult.getAttributeValue("value"));
        DefaultComboBoxModel EqualsModel = new DefaultComboBoxModel(variables.toArray());
        DefaultComboBoxModel VariablesModel = new DefaultComboBoxModel(variables.toArray());
        parent = Parent;
        cboEquals.setModel(EqualsModel);
        cboVariables.setModel(VariablesModel);
        String variable = resultmap.get("VARIABLE");
        if (VariablesModel.getIndexOf(variable) > -1) {
            cboVariables.setSelectedItem(variable);
        }
        String setto = resultmap.get("SETTO");
        if (setto != null && setto.equals("TEXT")) {
            String text = Result.getChildText("TEXT");
            if (text != null) {
                txtValue.setText(text);
                rdoText.setSelected(true);
            }
        } else if (setto.startsWith("VARIABLES,")) {
            String[] Path = setto.split(",", 2);
            if (Path.length > 1 && EqualsModel.getIndexOf(Path[1]) != -1) {
                cboEquals.setSelectedItem(Path[1]);
            }
        }
        complete = false;
    }

    public TemplateResultPopup(JDialog Parent, ArrayList<String> variables, int resultnumber) {
        initComponents();
        Result = new Element("RESULT" + resultnumber);
        parent = Parent;
        cboEquals.setModel(new DefaultComboBoxModel(variables.toArray()));
        cboVariables.setModel(new DefaultComboBoxModel(variables.toArray()));
        complete = false;
    }

    public Element getresult() {
        if (complete) {
            return Result;
        }
        return null;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Group = new javax.swing.ButtonGroup();
        rdoText = new javax.swing.JRadioButton();
        rdoVariable = new javax.swing.JRadioButton();
        txtValue = new javax.swing.JTextField();
        cboEquals = new javax.swing.JComboBox();
        btnOk = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        cboVariables = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();

        rdoText.setSelected(true);
        rdoText.setText("Input Specific Text:");

        rdoVariable.setText("Set equal to another variable:");

        cboEquals.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        btnOk.setText("Ok");
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

        cboVariables.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel1.setText("Variable:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(rdoVariable)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(cboEquals, 0, 133, Short.MAX_VALUE))
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(rdoText)
                                .addComponent(jLabel1))
                            .addGap(62, 62, 62)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(cboVariables, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(txtValue, javax.swing.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnOk, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancel)))
                .addContainerGap(27, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cboVariables, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rdoText)
                    .addComponent(txtValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rdoVariable)
                    .addComponent(cboEquals, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnOk)
                    .addComponent(btnCancel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOkActionPerformed
        complete = true;
        String name = Result.getName();
        Result = new Element(name);
        if (rdoText.isSelected()) {
            Result.setAttribute("value", "SETTO:TEXT;VARIABLE:" + cboVariables.getSelectedItem().toString());
            Result.addContent(new Element("TEXT"));
            Result.getChild("TEXT").addContent(txtValue.getText());
        } else {
            Result.setAttribute("value", "SETTO:VARIABLES," + cboEquals.getSelectedItem().toString() + ";VARIABLE:" + cboVariables.getSelectedItem().toString());
        }
        parent.dispose();
    }//GEN-LAST:event_btnOkActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        complete = false;
        parent.dispose();
    }//GEN-LAST:event_btnCancelActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup Group;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnOk;
    private javax.swing.JComboBox cboEquals;
    private javax.swing.JComboBox cboVariables;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JRadioButton rdoText;
    private javax.swing.JRadioButton rdoVariable;
    private javax.swing.JTextField txtValue;
    // End of variables declaration//GEN-END:variables
}
