/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Prototype.Popups;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JDialog;
import java.util.ArrayList;

/**
 *
 * @author Gregory
 */
public class NameForm extends javax.swing.JPanel {

    private ArrayList<String> names;
    private boolean Complete;
    private String result;
    private JDialog Parent;
    private boolean upper;

    /**
     * Creates new form NameForm
     */
    public NameForm(ArrayList<String> takennames, JDialog parent, String Message, boolean uppercaseonly) {
        upper = uppercaseonly;
        initComponents();
        names = takennames;
        Complete = false;
        result = "";
        Parent = parent;
        lblMessage.setVisible(true);
        lblError.setVisible(false);
        if (Message != null && !Message.isEmpty()) {
            lblMessage.setText(Message);

        }
        lblMessage.repaint();
        txtName.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                btnOkActionPerformed(e);
            }
        });
        this.txtName.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent evt) {
                char input = evt.getKeyChar();
                String text = txtName.getText();
                if ((!Character.isAlphabetic(input) && !Character.isDigit(input)&&(input!='_')) || (text.length() == 0 && Character.isDigit(input))||(text.length()==0&&(input=='_'))) {
                    evt.consume();
                } else if (upper) {
                    evt.setKeyChar(Character.toUpperCase(input));
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }
        });
    }

    public NameForm(ArrayList<String> takennames, JDialog parent, String Message, boolean uppercaseonly, String currentname) {
        initComponents();
        txtName.setText(currentname);
        upper = uppercaseonly;
        names = takennames;
        Complete = false;
        result = "";
        Parent = parent;
        lblMessage.setVisible(true);
        lblError.setVisible(false);
        if (Message != null && !Message.isEmpty()) {
            lblMessage.setText(Message);
        }
        lblMessage.repaint();
        txtName.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                btnOkActionPerformed(e);
            }
        });
        
        txtName.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent evt) {
                char input = evt.getKeyChar();
                String text = txtName.getText();
                if ((!Character.isAlphabetic(input) && !Character.isDigit(input)&&(input!='_')) || (text.length() == 0 && Character.isDigit(input))||(text.length()==0&&(input=='_'))) {
                    evt.consume();
                } else if (upper) {
                    evt.setKeyChar(Character.toUpperCase(input));
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }
        });
    }

    public String getresult() {
        if (Complete) {
            result = result.toUpperCase();
            return result;
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

        lblMessage = new javax.swing.JLabel();
        btnCancel = new javax.swing.JToggleButton();
        txtName = new javax.swing.JTextField();
        lblError = new javax.swing.JLabel();
        btnOk = new javax.swing.JButton();

        lblMessage.setText("Please enter a name:");
        lblMessage.setFocusable(false);

        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        lblError.setText("That name is already taken please enter a unique name.");

        btnOk.setText("Ok");
        btnOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOkActionPerformed(evt);
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
                        .addComponent(btnOk)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancel))
                    .addComponent(lblError)
                    .addComponent(lblMessage)
                    .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(31, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblMessage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblError, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancel)
                    .addComponent(btnOk))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        Complete = false;
        Parent.dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOkActionPerformed
        String text = txtName.getText();
        if (text.isEmpty()) {
            lblError.setText("Please enter a name.");
            lblError.setVisible(true);
            Parent.pack();
        } else if (names.contains(text)) {
            lblError.setText("That name is already taken please enter a unique name.");
            lblError.setVisible(true);
            Parent.pack();
        } else {
            result = text;
            Complete = true;
            Parent.dispose();
        }
    }//GEN-LAST:event_btnOkActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton btnCancel;
    private javax.swing.JButton btnOk;
    private javax.swing.JLabel lblError;
    private javax.swing.JLabel lblMessage;
    private javax.swing.JTextField txtName;
    // End of variables declaration//GEN-END:variables
}
