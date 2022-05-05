/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Prototype.EquationBuilding;

import Prototype.StaticClasses.Global;
import org.jdom2.Element;
import javax.swing.DefaultListModel;
import javax.swing.DefaultComboBoxModel;
import java.util.HashMap;
import javax.swing.JOptionPane;
import javax.swing.JDialog;
import Prototype.StaticClasses.XMLHandling;
import javax.swing.JFormattedTextField;

/**
 *
 * @author Gregory
 */
public class PrevValue extends javax.swing.JPanel {

    private java.util.ArrayList<String> dates = new java.util.ArrayList();
    private java.util.ArrayList<String> strings = new java.util.ArrayList();
    private JDialog parent;
    private Element previous;
    private boolean complete;
    private DefaultListModel MatchModel = new DefaultListModel();

    /**
     * Creates new form CompareForm
     */
    
    public PrevValue(String TableName, String termname, JDialog Parent) {
        initComponents();
        txtDate = new JFormattedTextField(new java.text.SimpleDateFormat(Global.getDateFormat()));
        txtDate.repaint();
        txtDate.revalidate();
        lstMatch.setModel(MatchModel);
        String[] Path = {Global.getMode() + "S", Global.getcurrentschema(), "TABLES", TableName};
        Element Table = XMLHandling.getpath(Path, Global.getxmlfilename());
        complete = false;
        parent = Parent;
        previous = new Element(termname);
        cmbSort.setModel(new DefaultComboBoxModel());
        cmbValue.setModel(new DefaultComboBoxModel());
        if (Table == null) {
            JOptionPane.showMessageDialog(null, "Unable to find XML for " + TableName);
            complete = false;
            parent.dispose();
        } else {
            Element Columns = Table.getChild("COLUMNS");
            DefaultListModel model = new DefaultListModel();
            if (Columns != null) {
                for (Element column : Columns.getChildren()) {
                    HashMap<String, String> columnmap = Global.makecommandmap(column);
                    String type = columnmap.get("TYPE");
                    String name = column.getName();
                    switch (type) {
                        case "DATE":
                            dates.add(name);
                            model.addElement(name);
                            cmbSort.addItem(name);
                            break;
                        case "INTEGER":
                        case "DOUBLE":
                            cmbValue.addItem(name);
                            model.addElement(name);
                            cmbSort.addItem(name);
                            break;
                        case "STRING":
                            strings.add(name);
                            model.addElement(name);
                            cmbSort.addItem(name);
                            break;
                        default:
                            model.addElement(name);
                            cmbSort.addItem(name);
                            break;
                    }
                }
                Element Equations = Table.getChild("EQUATIONS");
                if (Equations != null) {
                    for (Element Equation : Equations.getChildren()) {
                        cmbValue.addItem(Equation.getName());
                        cmbSort.addItem(Equation.getName());
                    }
                }
            }
            lstAll.setModel(model);

        }
    }

    public PrevValue(String TableName, Element Previous, JDialog Parent) {
        initComponents();
        txtDate = new JFormattedTextField(new java.text.SimpleDateFormat(Global.getDateFormat()));
        txtDate.repaint();
        txtDate.revalidate();
        lstMatch.setModel(MatchModel);
        String[] Path = {Global.getMode() + "S", Global.getcurrentschema(), "TABLES", TableName};
        Element Table = XMLHandling.getpath(Path, Global.getxmlfilename());
        complete = false;
        parent = Parent;
        previous = Previous;
        cmbSort.setModel(new DefaultComboBoxModel());
        cmbValue.setModel(new DefaultComboBoxModel());
        if (Table == null) {
            JOptionPane.showMessageDialog(null, "Unable to find XML for " + TableName);
            complete = false;
            parent.dispose();
        } else {
            Element Columns = Table.getChild("COLUMNS");
            DefaultListModel model = new DefaultListModel();
            for (Element column : Columns.getChildren()) {
                HashMap<String, String> columnmap = Global.makecommandmap(column);
                String type = columnmap.get("TYPE");
                String name = column.getName();
                switch (type) {
                    case "DATE":
                        dates.add(name);
                        model.addElement(name);
                        cmbSort.addItem(name);
                        break;
                    case "INTEGER":
                    case "DOUBLE":
                        cmbValue.addItem(name);
                        model.addElement(name);
                        cmbSort.addItem(name);
                        break;
                    case "STRING":
                        strings.add(name);
                        model.addElement(name);
                        cmbSort.addItem(name);
                        break;
                    default:
                        model.addElement(name);
                        cmbSort.addItem(name);
                        break;
                }
            }
            Element Equations = Table.getChild("EQUATIONS");
            if (Equations != null) {
                for (Element Equation : Equations.getChildren()) {
                    cmbValue.addItem(Equation.getName());
                    cmbSort.addItem(Equation.getName());
                }
            }
            lstAll.setModel(model);
            String compstring = Previous.getAttributeValue("value");
            HashMap<String, String> prevmap = Global.makecommandmap(compstring);
            if (prevmap.containsKey("VALUE") && !prevmap.get("VALUE").isEmpty()) {
                String Value = prevmap.get("VALUE");
                cmbValue.setSelectedItem(Value);
            }

            if (prevmap.containsKey("ORDERBY") && !prevmap.get("ORDERBY").isEmpty()) {
                String Sort = prevmap.get("ORDERBY");
                cmbSort.setSelectedItem(Sort);
            }
            String defvalue = prevmap.get("DEFAULT");
            if (defvalue != null) {
                if (Global.isNumeric(defvalue)) {
                    txtDefault.setText(defvalue);
                } else if (defvalue.equals("DATE")) {
                    String value = XMLHandling.getAttValue(new String[]{"TERMS", "DATE"}, "value", previous);
                    if (value != null) {
                        txtDate.setText(defvalue.substring(5));
                    }
                } else if (defvalue.equals("STRING")) {
                    String value = XMLHandling.getAttValue(new String[]{"TERMS", "STRING"}, "value", previous);
                    if (value != null) {
                        txtString.setText(defvalue.substring(5));
                    }
                }
            }
            if (prevmap.containsKey("MATCH") && !prevmap.get("MATCH").isEmpty()) {
                String[] matches = prevmap.get("MATCH").split("&&");
                DefaultListModel matchlist = new DefaultListModel();
                for (String match : matches) {
                    if (model.contains(match)) {
                        matchlist.addElement(match);
                    }
                }
                lstMatch.setModel(matchlist);
            }
            if (prevmap.get("ORDER") != null && prevmap.get("ORDER").equals("DESCENDING")) {
                this.rdoDescending.setSelected(true);
            } else {
                this.rdoAscending.setSelected(true);
            }
        }
    }

    public Element getXML() {
        if (complete) {
            return previous;
        } else {
            return null;
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

        AscvDesc = new javax.swing.ButtonGroup();
        jFormattedTextField1 = new javax.swing.JFormattedTextField();
        SamevsDef = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstAll = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        lstMatch = new javax.swing.JList();
        btnAdd = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        cmbValue = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        cmbSort = new javax.swing.JComboBox();
        rdoAscending = new javax.swing.JRadioButton();
        rdoDescending = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        btnOK = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        txtDefault = new javax.swing.JFormattedTextField();
        jLabel5 = new javax.swing.JLabel();
        txtDate = new javax.swing.JFormattedTextField();
        jLabel6 = new javax.swing.JLabel();
        rdoSame = new javax.swing.JRadioButton();
        rdoDefault = new javax.swing.JRadioButton();
        jLabel7 = new javax.swing.JLabel();
        txtString = new javax.swing.JTextField();

        jFormattedTextField1.setText("jFormattedTextField1");

        jScrollPane1.setViewportView(lstAll);

        jScrollPane2.setViewportView(lstMatch);

        btnAdd.setText(">>");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnRemove.setText("<<");
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveActionPerformed(evt);
            }
        });

        jLabel1.setText("Value:");

        cmbValue.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel4.setText("Sort by:");

        cmbSort.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        AscvDesc.add(rdoAscending);
        rdoAscending.setSelected(true);
        rdoAscending.setText("Ascending");

        AscvDesc.add(rdoDescending);
        rdoDescending.setText("Descending");

        jLabel2.setText("Which Columns to match:");

        btnOK.setText("OK");
        btnOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOKActionPerformed(evt);
            }
        });

        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        jLabel3.setText("Default Value: Number:");

        txtDefault.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat(""))));
        txtDefault.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtDefault.setText("0");
        txtDefault.setToolTipText("");

        jLabel5.setText("Date:");

        txtDate.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.DateFormatter(new java.text.SimpleDateFormat("yyyy-MM-dd"))));

        jLabel6.setText("If no previous value found:");

        SamevsDef.add(rdoSame);
        rdoSame.setSelected(true);
        rdoSame.setText("Use value from current row.");

        SamevsDef.add(rdoDefault);
        rdoDefault.setText("Use default value.");

        jLabel7.setText("Text:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel5)
                        .addGroup(layout.createSequentialGroup()
                            .addGap(5, 5, 5)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel2)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(btnAdd)
                                        .addComponent(btnRemove))
                                    .addGap(34, 34, 34)
                                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(btnOK)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(btnCancel))))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(rdoAscending)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(rdoDescending))
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel4)
                                .addComponent(jLabel1))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(cmbValue, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(cmbSort, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addComponent(jLabel6)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(rdoSame)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rdoDefault))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel7))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtDate)
                            .addComponent(txtDefault, javax.swing.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
                            .addComponent(txtString))))
                .addContainerGap(38, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cmbValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(cmbSort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rdoAscending)
                    .addComponent(rdoDescending))
                .addGap(3, 3, 3)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rdoSame)
                    .addComponent(rdoDefault))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtDefault, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(txtDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(txtString, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addComponent(btnAdd)
                        .addGap(18, 18, 18)
                        .addComponent(btnRemove))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane2)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnOK)
                    .addComponent(btnCancel))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        int index = lstAll.getSelectedIndex();
        if (index >= 0 && !MatchModel.contains(lstAll.getSelectedValue().toString())) {
            MatchModel.addElement(lstAll.getSelectedValue().toString());
            lstMatch.repaint();
            lstMatch.revalidate();
        }
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed
        int index = lstMatch.getSelectedIndex();
        if (index >= 0) {
            MatchModel.remove(index);
        }
    }//GEN-LAST:event_btnRemoveActionPerformed

    private void btnOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOKActionPerformed
        String value = cmbValue.getSelectedItem().toString();
        String sort = cmbSort.getSelectedItem().toString();
        String name = previous.getName();
        previous = new Element(name);
        if (MatchModel.contains(value)) {
            JOptionPane.showMessageDialog(null, "The selected value cannot be the same in the columns to match, please correct to finish saving.");
        } else {
            String order;
            if (this.rdoAscending.isSelected()) {
                order = "ASCENDING";
            } else {
                order = "DESCENDING";
            }
            String XMLString;
            XMLString = "OP:PREVIOUS;VALUE:" + value + ";ORDERBY:" + sort + ";ORDER:" + order + ";";
            if (rdoSame.isSelected()) {
                XMLString = XMLString + "DEFAULT:SAME;";
            } else if (dates.contains(value)) {
                XMLString = XMLString + "DEFAULT:DATE;";
                previous.addContent(new Element("TERMS"));
                previous.getChild("TERMS").addContent("DATE").setAttribute("value", txtDate.getText());
            } else if (strings.contains(value)) {
                XMLString = XMLString + "DEFAULT:STRING;";
                previous.addContent(new Element("TERMS"));
                previous.getChild("TERMS").addContent("STRING").addContent(txtString.getText());
            } else {
                XMLString = XMLString + "DEFAULT:" + txtDefault.getText() + ";";
            }
            if (MatchModel.getSize() > 0) {
                XMLString = XMLString + "MATCH:" + MatchModel.getElementAt(0);
            }
            for (int x = 1; x < MatchModel.getSize(); x++) {
                XMLString = XMLString + "&&" + MatchModel.getElementAt(x);
            }
            previous.setAttribute("value", XMLString);
            complete = true;
            parent.dispose();
        }

    }//GEN-LAST:event_btnOKActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        complete = false;
        parent.dispose();
    }//GEN-LAST:event_btnCancelActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup AscvDesc;
    private javax.swing.ButtonGroup SamevsDef;
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnOK;
    private javax.swing.JButton btnRemove;
    private javax.swing.JComboBox cmbSort;
    private javax.swing.JComboBox cmbValue;
    private javax.swing.JFormattedTextField jFormattedTextField1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JList lstAll;
    private javax.swing.JList lstMatch;
    private javax.swing.JRadioButton rdoAscending;
    private javax.swing.JRadioButton rdoDefault;
    private javax.swing.JRadioButton rdoDescending;
    private javax.swing.JRadioButton rdoSame;
    private javax.swing.JFormattedTextField txtDate;
    private javax.swing.JFormattedTextField txtDefault;
    private javax.swing.JTextField txtString;
    // End of variables declaration//GEN-END:variables
}
