/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Prototype.Main;

import Prototype.StaticClasses.Global;
import Prototype.StaticClasses.XMLHandling;
import java.io.File;
import Prototype.StaticClasses.SQLHandling;
import java.util.ArrayList;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.jdom2.Element;
import javax.swing.DefaultListModel;
import org.h2.jdbcx.JdbcConnectionPool;
import Prototype.Popups.NameForm;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 *
 * @author User
 */
public class StartMenu extends javax.swing.JPanel implements SaveInterface{

    /**
     * Creates new form StartMenu
     */
    private MainPanel panel;
    private Preferences prefs;
    private String xmlfile;
    private String dbfile;

    /**
     * Creates new form StartFinalForReal
     */
    public StartMenu(MainPanel Panel) {
        initComponents();
        prefs = Preferences.userRoot().node(this.getClass().getName());
        xmlfile = prefs.get("xmlfile", "Settings.xml");
        dbfile = prefs.get("dbfile", "Projects.h2.db");
        panel = Panel;
        loadfiles();
    }

    private void loadfiles() {
        String dbfilename = dbfile;
        if (dbfilename != null && dbfilename.endsWith(".h2.db")) {
            dbfilename = dbfilename.substring(0, dbfilename.length() - 6);
        }
        this.lstTemplates.setEnabled(true);
        this.lstProjects.setEnabled(true);
        this.lstXMLOrphans.setEnabled(true);
        this.lstDBOrphans.setEnabled(true);

        boolean xmlexists = Global.CheckForFile(xmlfile);
        boolean dbexists = Global.CheckForFile(dbfile);
        DefaultListModel xmlmodel = new DefaultListModel();
        DefaultListModel dbmodel = new DefaultListModel();
        DefaultListModel dborphan = new DefaultListModel();
        DefaultListModel xmlorphan = new DefaultListModel();
        this.lblSettings.setText("File Not Found");
        this.lblProjects.setText("File Not Found");
        if (xmlexists && dbexists) {
            btnBlankTemplate.setEnabled(true);
            btnEditProject.setEnabled(true);
            btnDeleteTemplate.setEnabled(true);
            btnDupTemplate.setEnabled(true);
            btnNewBlankProject.setEnabled(true);
            btnEditTemplate.setEnabled(true);
            btnDeleteProject.setEnabled(true);
            btnNewFromTemplate.setEnabled(true);
            JdbcConnectionPool cp = Global.getConnectionPool();
            if (cp != null) {
                cp.dispose();
            }
            cp = JdbcConnectionPool.create("jdbc:h2:" + dbfilename, "sa", "");//creates database connection pool
            Global.setConnectionPool(cp);
            Global.setxmlfilename(xmlfile);
            Global.setdbfilename(dbfile);
            prefs.put("xmlfile", xmlfile);
            prefs.put("dbfile", dbfile);

            lblProjects.setText(dbfile);
            lblSettings.setText(xmlfile);
            Element Templates = XMLHandling.getpath(new String[]{"TEMPLATES"}, xmlfile);
            Element Projects = XMLHandling.getpath(new String[]{"PROJECTS"}, xmlfile);
            ArrayList<String> schemas = SQLHandling.getlistofschemas();
            ArrayList<String> projectlist = new ArrayList();
            if (Templates != null) {
                for (Element template : Templates.getChildren()) {
                    String name = template.getName();
                    xmlmodel.addElement(name);
                }
            }
            if (Projects != null) {

                for (Element project : Projects.getChildren()) {
                    String name = project.getName();
                    projectlist.add(name);
                }
            }
            for (String proj : projectlist) {
                if (schemas.contains(proj)) {
                    schemas.remove(proj);
                    dbmodel.addElement(proj);
                } else {
                    xmlorphan.addElement(proj);
                }
            }
            for (String schema : schemas) {
                dborphan.addElement(schema);
            }
        }
        if (!xmlexists && dbexists) {

            JdbcConnectionPool cp = Global.getConnectionPool();
            if (cp != null) {
                cp.dispose();
            }
            cp = JdbcConnectionPool.create("jdbc:h2:" + dbfilename, "sa", "");//creates database connection pool
            Global.setConnectionPool(cp);
            Global.setdbfilename(dbfile);
            Global.setxmlfilename(null);
            prefs.put("dbfile", dbfile);

            lblProjects.setText(dbfile);
            ArrayList<String> schemas = SQLHandling.getlistofschemas();
            for (String schema : schemas) {
                dborphan.addElement(schema);
            }
        }
        if (xmlexists && !dbexists) {

            Global.setdbfilename(null);
            Global.setxmlfilename(xmlfile);
            prefs.put("xmlfile", xmlfile);
            lblSettings.setText(xmlfile);
            Element Templates = XMLHandling.getpath(new String[]{"TEMPLATES"}, xmlfile);
            Element Projects = XMLHandling.getpath(new String[]{"PROJECTS"}, xmlfile);
            if (Templates != null) {
                for (Element template : Templates.getChildren()) {
                    String name = template.getName();
                    xmlmodel.addElement(name);
                }
            }
            if (Projects != null) {
                for (Element project : Projects.getChildren()) {
                    String name = project.getName();
                    xmlorphan.addElement(name);
                }
            }
        }
        if (!dbexists || !xmlexists) {
            btnBlankTemplate.setEnabled(false);
            btnEditProject.setEnabled(false);
            btnDeleteTemplate.setEnabled(false);
            btnDupTemplate.setEnabled(false);
            btnNewBlankProject.setEnabled(false);
            btnEditTemplate.setEnabled(false);
            btnDeleteProject.setEnabled(false);
            btnNewFromTemplate.setEnabled(false);
        }
        if (xmlmodel.getSize() == 0) {
            xmlmodel.addElement("None Found");
            this.lstTemplates.setEnabled(false);
        }
        if (dbmodel.getSize() == 0) {
            dbmodel.addElement("None Found");
            this.lstProjects.setEnabled(false);
        }
        if (dborphan.getSize() == 0) {
            dborphan.addElement("None Found");
            this.lstDBOrphans.setEnabled(false);
        }
        if (xmlorphan.getSize() == 0) {
            xmlorphan.addElement("None Found");
            this.lstXMLOrphans.setEnabled(false);
        }
        lstXMLOrphans.setModel(xmlorphan);
        lstDBOrphans.setModel(dborphan);
        lstProjects.setModel(dbmodel);
        lstTemplates.setModel(xmlmodel);

        this.lstDBOrphans.repaint();
        this.lstProjects.repaint();
        this.lstDBOrphans.repaint();
        this.lstXMLOrphans.repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane3 = new javax.swing.JScrollPane();
        lstXMLOrphans = new javax.swing.JList();
        btnDeleteProject = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        btnDeleteTemplate = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        lstDBOrphans = new javax.swing.JList();
        btnDupTemplate = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        btnNewFromTemplate = new javax.swing.JButton();
        btnChooseXML = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        lstTemplates = new javax.swing.JList();
        btnEditTemplate = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        btnBlankTemplate = new javax.swing.JButton();
        lblSettings = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstProjects = new javax.swing.JList();
        lblProjects = new javax.swing.JLabel();
        btnChooseSQL = new javax.swing.JButton();
        btnEditProject = new javax.swing.JButton();
        btnNewXML = new javax.swing.JButton();
        btnNewDB = new javax.swing.JButton();
        btnNewBlankProject = new javax.swing.JButton();
        btnRenTemp = new javax.swing.JButton();

        setMaximumSize(new java.awt.Dimension(851, 450));
        setMinimumSize(new java.awt.Dimension(851, 450));
        setPreferredSize(new java.awt.Dimension(851, 450));

        jScrollPane3.setMaximumSize(new java.awt.Dimension(240, 160));
        jScrollPane3.setMinimumSize(new java.awt.Dimension(240, 160));
        jScrollPane3.setOpaque(false);
        jScrollPane3.setPreferredSize(new java.awt.Dimension(240, 160));

        lstXMLOrphans.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstXMLOrphans.setMaximumSize(new java.awt.Dimension(1000, 1000));
        jScrollPane3.setViewportView(lstXMLOrphans);

        btnDeleteProject.setText("Delete Project");
        btnDeleteProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteProjectActionPerformed(evt);
            }
        });

        jLabel2.setText("Database File (.db)");

        btnDeleteTemplate.setText("Delete Template");
        btnDeleteTemplate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteTemplateActionPerformed(evt);
            }
        });

        jLabel3.setText("Active Projects Found:");

        jLabel5.setText("Projects found in XML File with no matching data:");

        jLabel6.setText("Possible Projects found in data file:");

        jScrollPane4.setMaximumSize(new java.awt.Dimension(240, 160));
        jScrollPane4.setMinimumSize(new java.awt.Dimension(240, 160));
        jScrollPane4.setPreferredSize(new java.awt.Dimension(240, 160));

        lstDBOrphans.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstDBOrphans.setMaximumSize(new java.awt.Dimension(1000, 1000));
        jScrollPane4.setViewportView(lstDBOrphans);

        btnDupTemplate.setText("Duplicate Template");
        btnDupTemplate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDupTemplateActionPerformed(evt);
            }
        });

        jLabel1.setText("Settings File (.xml)");

        btnNewFromTemplate.setText("New Project from Template");
        btnNewFromTemplate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewFromTemplateActionPerformed(evt);
            }
        });

        btnChooseXML.setText("Change File");
        btnChooseXML.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChooseXMLActionPerformed(evt);
            }
        });

        jScrollPane2.setMaximumSize(new java.awt.Dimension(240, 160));
        jScrollPane2.setMinimumSize(new java.awt.Dimension(240, 160));
        jScrollPane2.setName(""); // NOI18N
        jScrollPane2.setPreferredSize(new java.awt.Dimension(240, 160));

        lstTemplates.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstTemplates.setMaximumSize(new java.awt.Dimension(1000, 1000));
        jScrollPane2.setViewportView(lstTemplates);

        btnEditTemplate.setText("Edit Template");
        btnEditTemplate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditTemplateActionPerformed(evt);
            }
        });

        jLabel4.setText("Templates Found:");

        btnBlankTemplate.setText("New Blank Template");
        btnBlankTemplate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBlankTemplateActionPerformed(evt);
            }
        });

        lblSettings.setText("Settings.xml");

        jScrollPane1.setMaximumSize(new java.awt.Dimension(240, 160));
        jScrollPane1.setMinimumSize(new java.awt.Dimension(240, 160));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(240, 160));

        lstProjects.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstProjects.setMaximumSize(new java.awt.Dimension(1000, 1000));
        jScrollPane1.setViewportView(lstProjects);

        lblProjects.setText("project.db.h2.db");

        btnChooseSQL.setText("Change File");
        btnChooseSQL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChooseSQLActionPerformed(evt);
            }
        });

        btnEditProject.setText("Edit Project");
        btnEditProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditProjectActionPerformed(evt);
            }
        });

        btnNewXML.setText("Create New");
        btnNewXML.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewXMLActionPerformed(evt);
            }
        });

        btnNewDB.setText("Create New");
        btnNewDB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewDBActionPerformed(evt);
            }
        });

        btnNewBlankProject.setText("New Blank Project");
        btnNewBlankProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewBlankProjectActionPerformed(evt);
            }
        });

        btnRenTemp.setText("Rename Template");
        btnRenTemp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRenTempActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblSettings)
                    .addComponent(lblProjects)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(230, 230, 230)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(btnChooseSQL, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnChooseXML, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btnNewXML)
                                    .addComponent(btnNewDB)))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addGap(6, 6, 6)
                                    .addComponent(btnDeleteProject, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createSequentialGroup()
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(btnNewBlankProject, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnEditProject, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(btnEditTemplate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btnNewFromTemplate, javax.swing.GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE)
                                    .addComponent(btnDupTemplate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btnBlankTemplate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btnRenTemp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btnDeleteTemplate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(101, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnChooseXML)
                    .addComponent(lblSettings)
                    .addComponent(btnNewXML))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnChooseSQL)
                        .addComponent(btnNewDB))
                    .addComponent(lblProjects))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnEditProject)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnNewBlankProject)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDeleteProject))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnEditTemplate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnBlankTemplate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDupTemplate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnNewFromTemplate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRenTemp)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDeleteTemplate))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(26, 26, 26))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnDeleteTemplateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteTemplateActionPerformed
        int index = lstTemplates.getSelectedIndex();
        if (index > -1) {
            String templatename = lstTemplates.getSelectedValue().toString();
            int choice = JOptionPane.showConfirmDialog(null, "Are you sure you wish to delete " + templatename + "? This cannot be undone.", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                XMLHandling.deletepath(new String[]{"TEMPLATES", templatename}, Global.getxmlfilename());
            }
        }
        this.loadfiles();
    }//GEN-LAST:event_btnDeleteTemplateActionPerformed

    private void btnDupTemplateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDupTemplateActionPerformed
        int index = lstTemplates.getSelectedIndex();
        if (index > -1) {
            String templatename = lstTemplates.getSelectedValue().toString();
            JDialog dialog = new JDialog();
            DefaultListModel model = (DefaultListModel) lstProjects.getModel();
            ArrayList<String> names = new ArrayList();
            for (Object name : model.toArray()) {
                names.add(name.toString());
            }
            NameForm form = new NameForm(names, dialog, "Please enter a name for the template", true);
            dialog.add(form);
            dialog.setModal(true);
            dialog.setVisible(true);
            String newtemplatename = form.getresult();
            if (newtemplatename != null && !newtemplatename.isEmpty()) {
                Element template = XMLHandling.getpath(new String[]{"TEMPLATES", templatename}, Global.getxmlfilename());
                Element newtemplate = template.clone();
                newtemplate.setName(newtemplatename);
                XMLHandling.addpath(new String[]{"TEMPLATES"}, newtemplate, Global.getxmlfilename(), true);
                panel.loadtemplate(newtemplatename);
            }

        }
    }//GEN-LAST:event_btnDupTemplateActionPerformed

    private void btnNewFromTemplateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewFromTemplateActionPerformed
        int index = lstTemplates.getSelectedIndex();
        if (index > -1) {
            String templatename = lstTemplates.getSelectedValue().toString();
            JDialog dialog = new JDialog();
            DefaultListModel model = (DefaultListModel) lstProjects.getModel();
            ArrayList<String> names = new ArrayList();
            for (Object name : model.toArray()) {
                names.add(name.toString());
            }
            model = (DefaultListModel) lstDBOrphans.getModel();
            for (Object name : model.toArray()) {
                names.add(name.toString());
            }
            model = (DefaultListModel) lstXMLOrphans.getModel();
            for (Object name : model.toArray()) {
                names.add(name.toString());
            }
            names.add("PUBLIC");
            names.add("INFORMATION_SCHEMA");
            NameForm form = new NameForm(names, dialog, "Please enter a name for the project", true);
            dialog.add(form);
            dialog.setTitle("Project Name");
            dialog.pack();
            dialog.setModal(true);
            dialog.setVisible(true);
            String projectname = form.getresult();
            if (projectname != null && !projectname.isEmpty()) {
                Element template = XMLHandling.getpath(new String[]{"TEMPLATES", templatename}, xmlfile);
                if (template != null) {
                    panel.NewFromTemplate(template, projectname);
                }
            }
        }
    }//GEN-LAST:event_btnNewFromTemplateActionPerformed

    private void btnChooseXMLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChooseXMLActionPerformed
        JFileChooser c = new JFileChooser();
        c.setCurrentDirectory(new File(xmlfile));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("XML File", "xml");
        c.setAcceptAllFileFilterUsed(false);
        c.setFileFilter(filter);
        int rVal = c.showOpenDialog(this);

        if (rVal == JFileChooser.APPROVE_OPTION) {
            try {
                String filepath = c.getSelectedFile().getCanonicalPath();

                xmlfile = filepath;
                lblSettings.setText(xmlfile);
                Global.setxmlfilename(xmlfile);
                this.loadfiles();
                prefs.put("xmlfile", xmlfile);
            } catch (Exception e) {
            }
        }
        if (rVal == JFileChooser.CANCEL_OPTION) {
        }
    }//GEN-LAST:event_btnChooseXMLActionPerformed

    private void btnEditTemplateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditTemplateActionPerformed
        int index = lstTemplates.getSelectedIndex();
        if (index > -1) {
            String templatename = lstTemplates.getSelectedValue().toString();
            panel.loadtemplate(templatename);
        }
    }//GEN-LAST:event_btnEditTemplateActionPerformed

    private void btnBlankTemplateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBlankTemplateActionPerformed
        JDialog dialog = new JDialog();
        DefaultListModel model = (DefaultListModel) lstTemplates.getModel();
        ArrayList<String> names = new ArrayList();
        dialog.setTitle("Template Name");
        for (Object name : model.toArray()) {
            names.add(name.toString());
        }
        NameForm form = new NameForm(names, dialog, "Please enter a name for the template:", true);
        dialog.add(form);
        dialog.setSize(370, 150);
        dialog.setModal(true);
        dialog.setVisible(true);
        String templatename = form.getresult();
        if (templatename != null && !templatename.isEmpty()) {
            Element newtemplate = new Element(templatename);
            newtemplate.addContent(new Element("TABLES"));
            newtemplate.addContent(new Element("WIZARD"));
            XMLHandling.addpath(new String[]{"TEMPLATES"}, newtemplate, Global.getxmlfilename(), true);
            panel.loadtemplate(templatename);
        }
    }//GEN-LAST:event_btnBlankTemplateActionPerformed

    private void btnChooseSQLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChooseSQLActionPerformed
        JFileChooser c = new JFileChooser();
        // Demonstrate "Open" dialog:
        c.setCurrentDirectory(new File(dbfile));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Database File", "db");
        c.setAcceptAllFileFilterUsed(false);
        c.setFileFilter(filter);
        int rVal = c.showOpenDialog(this);
        if (rVal == JFileChooser.APPROVE_OPTION) {
            try {
                String filepath = c.getSelectedFile().getCanonicalPath();

                dbfile = filepath;
                lblProjects.setText(dbfile);
                Global.setdbfilename(dbfile);
                prefs.put("dbfile", dbfile);
                this.loadfiles();
            } catch (Exception e) {
            }
        }
        if (rVal == JFileChooser.CANCEL_OPTION) {
        }
    }//GEN-LAST:event_btnChooseSQLActionPerformed

    private void btnNewXMLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewXMLActionPerformed
        JFileChooser c = new JFileChooser();
        // Demonstrate "Open" dialog:
        c.setCurrentDirectory(new File(xmlfile));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("XML File", "xml");
        c.setAcceptAllFileFilterUsed(false);
        c.setFileFilter(filter);
        int rVal = c.showSaveDialog(this);

        if (rVal == JFileChooser.APPROVE_OPTION) {
            String filepath;
            try {
                filepath = c.getSelectedFile().getCanonicalPath();
                if (!filepath.endsWith(".xml")) {
                    filepath = filepath + ".xml";
                }

                lblSettings.setText(filepath);
                xmlfile = filepath;

                XMLHandling.CreateFile(xmlfile);
                XMLHandling.addpath(new String[]{}, new Element("TEMPLATES"), xmlfile, true);
                XMLHandling.addpath(new String[]{}, new Element("PROJECTS"), xmlfile, true);
                Global.setxmlfilename(xmlfile);
                prefs.put("xmlfile", xmlfile);
                this.loadfiles();

            } catch (Exception e) {
            }

        }
        if (rVal == JFileChooser.CANCEL_OPTION) {
        }
    }//GEN-LAST:event_btnNewXMLActionPerformed

    private void btnNewDBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewDBActionPerformed
        JFileChooser c = new JFileChooser();
        c.setCurrentDirectory(new File(dbfile));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Database File", "h2.db");
        c.setAcceptAllFileFilterUsed(false);
        c.setFileFilter(filter);
        int rVal = c.showSaveDialog(this);

        if (rVal == JFileChooser.APPROVE_OPTION) {
            String filepath;
            try {
                filepath = c.getSelectedFile().getCanonicalPath();
                if (!filepath.endsWith(".h2.db")) {
                    filepath = filepath + ".h2.db";
                }
                dbfile = filepath;
                SQLHandling.createnewdb(dbfile);
                lblProjects.setText(dbfile);
                Global.setdbfilename(dbfile);
                this.loadfiles();
                prefs.put("dbfile", dbfile);
            } catch (Exception e) {
            }
        }
        if (rVal == JFileChooser.CANCEL_OPTION) {
        }
    }//GEN-LAST:event_btnNewDBActionPerformed

    private void btnRenTempActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRenTempActionPerformed
        int index = lstTemplates.getSelectedIndex();
        if (index > -1) {
            String oldtemplatename = lstTemplates.getSelectedValue().toString();
            JDialog dialog = new JDialog();
            DefaultListModel model = (DefaultListModel) lstTemplates.getModel();
            ArrayList<String> names = new ArrayList();
            dialog.setTitle("Template Name");
            for (Object name : model.toArray()) {
                names.add(name.toString());
            }
            NameForm form = new NameForm(names, dialog, "Please enter a name for the template:", true);
            dialog.add(form);
            dialog.setSize(370, 150);
            dialog.setModal(true);
            dialog.setVisible(true);
            String templatename = form.getresult();
            if (templatename != null && !templatename.isEmpty()) {
                XMLHandling.changeElementName(new String[]{"TEMPLATES", oldtemplatename}, templatename, xmlfile);
            }
        }
    }//GEN-LAST:event_btnRenTempActionPerformed

    private void btnDeleteProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteProjectActionPerformed
        int index = lstProjects.getSelectedIndex();
        if (index > -1) {
            String projectname = lstProjects.getSelectedValue().toString();
            int choice = JOptionPane.showConfirmDialog(null, "Are you sure you wish to delete " + projectname + "? This cannot be undone.", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                SQLHandling.deleteschema(projectname);
                XMLHandling.deletepath(new String[]{"PROJECTS", projectname}, Global.getxmlfilename());
            }
        }
        this.loadfiles();
    }//GEN-LAST:event_btnDeleteProjectActionPerformed

    private void btnEditProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditProjectActionPerformed
        int index = lstProjects.getSelectedIndex();
        if (index > -1) {
            String projectname = lstProjects.getSelectedValue().toString();
            panel.loadproject(projectname);
        }
    }//GEN-LAST:event_btnEditProjectActionPerformed

    private void btnNewBlankProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewBlankProjectActionPerformed
        JDialog dialog = new JDialog();
        DefaultListModel model = (DefaultListModel) lstProjects.getModel();
        ArrayList<String> names = new ArrayList();
        for (Object name : model.toArray()) {
            names.add(name.toString());
        }
        model = (DefaultListModel) lstDBOrphans.getModel();
        for (Object name : model.toArray()) {
            names.add(name.toString());
        }
        model = (DefaultListModel) lstXMLOrphans.getModel();
        for (Object name : model.toArray()) {
            names.add(name.toString());
        }
        names.add("PUBLIC");
        names.add("INFORMATION_SCHEMA");
        NameForm form = new NameForm(names, dialog, "Please enter a name for the project", true);
        dialog.add(form);
        dialog.setSize(370, 150);
        dialog.setTitle("Project Name");
        dialog.setModal(true);
        dialog.setVisible(true);
        String projectname = form.getresult();
        if (projectname != null && !projectname.isEmpty()) {
            Element newproj = new Element(projectname);
            XMLHandling.addpath(new String[]{"PROJECTS"}, newproj, Global.getxmlfilename(), true);
            SQLHandling.createschema(projectname);

            panel.loadproject(projectname);
        }
    }//GEN-LAST:event_btnNewBlankProjectActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBlankTemplate;
    private javax.swing.JButton btnChooseSQL;
    private javax.swing.JButton btnChooseXML;
    private javax.swing.JButton btnDeleteProject;
    private javax.swing.JButton btnDeleteTemplate;
    private javax.swing.JButton btnDupTemplate;
    private javax.swing.JButton btnEditProject;
    private javax.swing.JButton btnEditTemplate;
    private javax.swing.JButton btnNewBlankProject;
    private javax.swing.JButton btnNewDB;
    private javax.swing.JButton btnNewFromTemplate;
    private javax.swing.JButton btnNewXML;
    private javax.swing.JButton btnRenTemp;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JLabel lblProjects;
    private javax.swing.JLabel lblSettings;
    private javax.swing.JList lstDBOrphans;
    private javax.swing.JList lstProjects;
    private javax.swing.JList lstTemplates;
    private javax.swing.JList lstXMLOrphans;
    // End of variables declaration//GEN-END:variables

    @Override
    public boolean IsSaved() {
        return true;
    }

    @Override
    public int SaveCheck() {
        return JOptionPane.YES_OPTION;
    }
}
