/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Prototype.Main;

import Prototype.StaticClasses.Global;
import Prototype.DataManaging.TableEditor;
import Prototype.EquationBuilding.EquationBuilder;
import Prototype.TableBuilding.TableBuilder;
import Prototype.WizardBuilding.WizardBuilder;
import Prototype.ReportBuilding.ReportBuilder;
import Prototype.WizardExecutor.WizardForm;
import org.h2.jdbcx.JdbcConnectionPool;
import javax.swing.JPanel;

/**
 *
 * @author Gregory
 */
public class MainPanel extends javax.swing.JFrame {

    private static SaveInterface currenttab;

    /**
     * Creates new form MainPanel
     */
    public MainPanel() {
        javax.swing.UIManager.getLookAndFeelDefaults().put("defaultFont", new java.awt.Font("Dialog", java.awt.Font.BOLD, 14));
        initComponents();
    }

    public void loadtemplate(String templatename) {
        Global.setMode(Global.TEMPLATEMODE);
        Global.setcurrentschema(templatename);
        MainTabs.removeAll();
        TableBuilder tablebuilder = new TableBuilder();
        MainTabs.add("Table Builder", tablebuilder);
        EquationBuilder equationbuilder = new EquationBuilder();
        MainTabs.add("Equation Builder", equationbuilder);
        WizardBuilder dialoguemanager = new WizardBuilder();
        MainTabs.add("Wizard Builder", dialoguemanager);
        ReportBuilder reportbuilder = new ReportBuilder();
        MainTabs.add("Report Builder", reportbuilder);
    }

    public void NewFromTemplate(org.jdom2.Element Template, String ProjectName) {
        Global.setMode(Global.PROJECTMODE);
        Global.setcurrentschema(ProjectName);
        MainTabs.removeAll();
        WizardForm form = new WizardForm(this, ProjectName, Template);
        MainTabs.add("Wizards", form);
    }

    public void WizardComplete() {
        TableBuilder tablebuilder = new TableBuilder();
        this.MainTabs.add("Table Builder", tablebuilder);
        TableEditor tableeditor = new TableEditor();
        this.MainTabs.add("Data Editor", tableeditor);
        EquationBuilder equationbuilder = new EquationBuilder();
        this.MainTabs.add("Equation Builder", equationbuilder);
        WizardBuilder dialoguemanager = new WizardBuilder();
        MainTabs.add("Wizard Builder", dialoguemanager);
        ReportBuilder reportbuilder = new ReportBuilder();
        MainTabs.add("Report Builder", reportbuilder);
    }

    public void loadproject(String projectname) {
        Global.setMode(Global.PROJECTMODE);
        Global.setcurrentschema(projectname);
        MainTabs.removeAll();
        TableBuilder tablebuilder = new TableBuilder();
        this.MainTabs.add("Table Builder", tablebuilder);
        TableEditor tableeditor = new TableEditor();
        this.MainTabs.add("Data Editor", tableeditor);
        EquationBuilder equationbuilder = new EquationBuilder();
        this.MainTabs.add("Equation Builder", equationbuilder);
        WizardBuilder dialoguemanager = new WizardBuilder();
        MainTabs.add("Wizard Builder", dialoguemanager);
        ReportBuilder reportbuilder = new ReportBuilder();
        MainTabs.add("Report Builder", reportbuilder);
        WizardForm form = new WizardForm(this);
        MainTabs.add("Wizards", form);
    }

    private void MainMenu() {

        StartMenu menu = new StartMenu(this);
        MainTabs.removeAll();
        MainTabs.add("Start Menu", menu);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        MainTabs = new javax.swing.JTabbedPane();
        MainMenuBar = new javax.swing.JMenuBar();
        mnuMain = new javax.swing.JMenu();
        mitMainMenu = new javax.swing.JMenuItem();
        mnuExit = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Carbon Calculator Prototype");
        setMinimumSize(new java.awt.Dimension(1050, 680));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        MainTabs.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        MainTabs.setMaximumSize(new java.awt.Dimension(1000, 480));
        MainTabs.setMinimumSize(new java.awt.Dimension(1000, 480));
        MainTabs.setPreferredSize(new java.awt.Dimension(1000, 480));
        MainTabs.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                MainTabsStateChanged(evt);
            }
        });

        mnuMain.setText("Main");

        mitMainMenu.setText("Main Menu");
        mitMainMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mitMainMenuActionPerformed(evt);
            }
        });
        mnuMain.add(mitMainMenu);

        mnuExit.setText("Exit");
        mnuExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuExitActionPerformed(evt);
            }
        });
        mnuMain.add(mnuExit);

        MainMenuBar.add(mnuMain);

        setJMenuBar(MainMenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(MainTabs, javax.swing.GroupLayout.DEFAULT_SIZE, 1035, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(MainTabs, javax.swing.GroupLayout.PREFERRED_SIZE, 600, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        MainMenu();
    }//GEN-LAST:event_formWindowOpened

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing

        JdbcConnectionPool cp = Global.getConnectionPool();//closes the database connection to save space
        if (cp != null) {
            cp.dispose();
        }
    }//GEN-LAST:event_formWindowClosing

    private void MainTabsStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_MainTabsStateChanged
        JPanel tab = (JPanel) MainTabs.getSelectedComponent();
        currenttab = (SaveInterface) tab;
        if (tab instanceof TableBuilder) {
            TableBuilder builder = (TableBuilder) tab;
            builder.Startup();
        } else if (tab instanceof EquationBuilder) {
            EquationBuilder eqbuilder = (EquationBuilder) tab;
            eqbuilder.Startup();
        } else if (tab instanceof WizardBuilder) {
            WizardBuilder manager = (WizardBuilder) tab;
            manager.Startup();
        } else if (tab instanceof TableEditor) {
            TableEditor editor = (TableEditor) tab;
            editor.Startup();
        } else if (tab instanceof ReportBuilder) {
            ReportBuilder editor = (ReportBuilder) tab;
            editor.Startup();
        }
    }//GEN-LAST:event_MainTabsStateChanged

    private void mnuExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuExitActionPerformed
        if (currenttab == null || currenttab.IsSaved()) {
            System.exit(0);
        } else {
            if (currenttab.SaveCheck() != javax.swing.JOptionPane.CANCEL_OPTION) {
                System.exit(0);
            }
        }
    }//GEN-LAST:event_mnuExitActionPerformed

    private void mitMainMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mitMainMenuActionPerformed
        MainMenu();
    }//GEN-LAST:event_mitMainMenuActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        final MainPanel panel = new MainPanel();

        panel.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (currenttab == null || currenttab.IsSaved()) {
                    System.exit(0);
                } else {
                    if (currenttab.SaveCheck() != javax.swing.JOptionPane.CANCEL_OPTION) {
                        System.exit(0);
                    }
                }
            }
        });
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                panel.setVisible(true);
            }
        });

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuBar MainMenuBar;
    private javax.swing.JTabbedPane MainTabs;
    private javax.swing.JMenuItem mitMainMenu;
    private javax.swing.JMenuItem mnuExit;
    private javax.swing.JMenu mnuMain;
    // End of variables declaration//GEN-END:variables
}
