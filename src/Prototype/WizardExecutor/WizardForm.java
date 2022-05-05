/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Prototype.WizardExecutor;

import Prototype.StaticClasses.XMLHandling;
import Prototype.StaticClasses.Global;
import java.util.TreeMap;
import java.util.ArrayList;
import org.jdom2.Element;
import net.miginfocom.swing.MigLayout;
import java.util.HashMap;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JComponent;
import Prototype.StaticClasses.SQLHandling;
import Prototype.Main.MainPanel;
import java.awt.BorderLayout;
import java.sql.Connection;
import java.sql.Statement;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

/**
 *
 * @author User
 */
public class WizardForm extends javax.swing.JPanel implements Prototype.Main.SaveInterface {

    private HashMap<String, WizardTableNode> WizardTableList = new HashMap();
    private Element ProjectXML;
    private HashMap<String, String> Variables = new HashMap();
    private TreeMap<Integer, WizardStep> steps = new TreeMap();
    private int currentstep = 0;
    private String ProjectName;
    private Element Template;
    private MainPanel Home;
    private boolean newproject;
    private boolean checksave = false;
    private boolean forward = true;
    /**
     * scenario a: create new project from template with no wizards - auto
     * generate project using default equation - load into table editor tab
     * scenario b: create project from template with wizards - load only wizard
     * executor tab and ask what they want to do - load wizard or load default
     * settings - bounce back to main menu to load other tabs
     * scenario c:
     * existing project no wizards - message saying there's no wizards available
     * scenario d: existing project with wizards -message saying equations will
     * not change, just for data entry -show list of wizards to run -hide default
     * settings button
     */
    /**
     * Creates new form WizardForm
     */
    public WizardForm(MainPanel home, String projectname, Element template) {
        initComponents();
        Home = home;
        ProjectName = projectname;
        Template = template;
        ProjectXML = new Element(ProjectName);
        newproject = true;
        Element Wizardsource = template.getChild("WIZARDS");
        if (Wizardsource != null) {
            ArrayList<String> Wizards = XMLHandling.getchildlist(Wizardsource);
            if (!Wizards.isEmpty()) {
                lstWizards.setListData(Wizards.toArray());
                lstWizards.repaint();
            } else {
                NewNoWizard();
            }
        } else {
            NewNoWizard();
        }
    }

    public WizardForm(MainPanel home) {
        initComponents();
        Home = home;
        ProjectName = Global.getcurrentschema();
        ProjectXML = XMLHandling.getpath(new String[]{"PROJECTS", ProjectName}, Global.getxmlfilename());
        newproject = false;
        Element Wizardsource = ProjectXML.getChild("WIZARDS");
        if (Wizardsource != null) {
            ArrayList<String> Wizards = XMLHandling.getchildlist(Wizardsource);
            if (!Wizards.isEmpty()) {
                lstWizards.setListData(Wizards.toArray());
                lstWizards.repaint();
                lblMessage.setText("With existing projects the Wizards will not change the equations.");
            } else {
                NoWizards();
            }
        } else {
            NoWizards();
        }
        btnCancel.setEnabled(false);
    }

    //scenario a
    public void NewNoWizard() {
        createtables();
        NoWizardEquations();
        Home.loadproject(ProjectName);
    }

    public void NewWithWizard(String Wizard) {
        checksave = true;
        Element WizardXML = Template.getChild("WIZARDS");
        if (WizardXML != null) {
            WizardXML = WizardXML.getChild(Wizard);
            if (WizardXML != null) {
                createtables();
                loadvariables(WizardXML);
                LoadStepList(WizardXML);
                btnCancel.setEnabled(true);
                btnNext.setEnabled(true);
                btnPrevious.setEnabled(true);
                btnFinish.setEnabled(true);
                Integer firstkey = steps.firstKey();
                loadstep(steps.get(firstkey), true);
            }
        }
    }

    private void OldWithWizards(String Wizard) {
        Element WizardXML = XMLHandling.getpath(new String[]{"WIZARDS", Wizard}, ProjectXML);
        if (WizardXML != null) {
            loadvariables(WizardXML);
            LoadStepList(WizardXML);
            btnCancel.setEnabled(true);
            btnNext.setEnabled(true);
            btnPrevious.setEnabled(true);
            btnFinish.setEnabled(true);
            Integer firstkey = steps.firstKey();
            loadstep(steps.get(firstkey), true);
        }
    }

    public void NoWizards() {
        lstWizards.setEnabled(false);
        btnDefault.setEnabled(false);
        btnLoad.setEnabled(false);
        lblTop.setText("<html>This project currently has no Wizards available. <br/> You may use the Wizard Builder to create some.</html>");
    }

    
    private void LoadStepList(Element WizardXML) {
        Element steplist = WizardXML.getChild(("STEPS"));
        TreeMap<Integer, Element> tempsteps = new TreeMap();
        for (Element step : steplist.getChildren()) {
            String name = step.getName();
            if (name.startsWith("STEP") && Global.isInteger(name.substring(4))) {
                Integer number = Integer.parseInt(name.substring(4));
                if (!tempsteps.containsKey(number) && number > 0) {
                    tempsteps.put(number, step);
                }
            }
        }
        steps = new TreeMap();
        for (Integer key : tempsteps.keySet()) {
            Element step = tempsteps.get(key);
            WizardStep newstep = new WizardStep(step);
            steps.put(key, newstep);
            newstep.setnumber(key);
        }
        currentstep = steps.firstKey();
    }

    private void NoWizardEquations() {

        Element tables = Template.getChild("TABLES").clone();
        //the appropriate information about the columns is already loaded in the xml we just need to set the equations and save everything in the right spot.
        for (Element table : tables.getChildren()) {
            if (table.getChild("EQUATIONS") != null) {
                Element equationlist = table.getChild("EQUATIONS");
                Element newequationlist = new Element("EQUATIONS");
                for (Element equation : equationlist.getChildren()) {

                    String eqstring = equation.getAttributeValue("value");
                    HashMap<String, String> eqmap = Global.makecommandmap(eqstring);
                    if (eqmap == null || !eqmap.containsKey("DEFAULT")) {
                        Element versionlist = equation.getChild(("VERSIONS"));
                        TreeMap<Integer, Element> tempversions = new TreeMap();
                        for (Element version : versionlist.getChildren()) {
                            String name = version.getName();
                            if (name.startsWith("VERSION") && Global.isInteger(name.substring(7))) {
                                Integer number = Integer.parseInt(name.substring(7));
                                if (!tempversions.containsKey(number) && number > 0) {
                                    tempversions.put(number, version);
                                }
                            }
                        }
                        Element version = tempversions.firstEntry().getValue();
                        if (version != null) {
                            String vername = version.getName();
                            Element eqversion = versionlist.getChild(vername).detach();
                            eqversion.setName(equation.getName());
                            newequationlist.addContent(eqversion);
                        }
                    } else {
                        String vername = eqmap.get("DEFAULT");
                        Element eqversion = equation.getChild(vername);
                        if (eqversion != null) {
                            eqversion = eqversion.detach();
                            eqversion.setName(equation.getName());
                            newequationlist.addContent(eqversion);
                        }
                    }
                }
                table.removeChild("EQUATIONS");
                table.addContent(newequationlist);
            }
        }
        XMLHandling.addpath(new String[]{"PROJECTS", ProjectName}, tables, Global.getxmlfilename(), true);
    }

    private void loadstep(WizardStep step, boolean updatebranch) {
        boolean skip = false;
        JPanel DisplayPanel = new JPanel();
        JScrollPane scpDisplay = new JScrollPane(DisplayPanel);
        DisplayPanel.setLayout(new MigLayout());
        pnlMain.removeAll();
        pnlMain.setLayout(new BorderLayout());
        pnlMain.add(scpDisplay, BorderLayout.CENTER);
        pnlMain.add(scpDisplay);

        if (updatebranch) {
            HashMap<String, Prototype.DataManaging.EQValue> Var = new HashMap();
            for (String key : Variables.keySet()) {
                Var.put(key, new Prototype.DataManaging.EQValue(Variables.get(key), "STRING"));
            }
            step.choosebranch(Var);
        }
        String tablename = step.gettablename();

        if (tablename != null && !tablename.isEmpty()) {
            //<editor-fold defaultstate="collapsed" desc="Table">
            WizardTableNode table;
            if (hasWizardTable(tablename)) {
                table = getWizardTable(tablename);
            } else {
                Element tablesource;
                if (newproject) {
                    tablesource = XMLHandling.getpath(new String[]{"TABLES", tablename}, Template);
                } else {
                    tablesource = XMLHandling.getpath(new String[]{"TABLES", tablename}, ProjectXML);
                }
                table = new WizardTableNode(this, ProjectName + "." + tablename, tablesource);
                addWizardTable(tablename, table);
            }
            Element equations = step.gettableequations();
            HashMap<String, String> equationversions = new HashMap();
            if (equations != null) {

                for (Element equation : equations.getChildren()) {
                    String name = equation.getName();
                    String commandstring = equation.getAttributeValue("value");
                    HashMap<String, String> commandmap = Global.makecommandmap(commandstring);
                    String version = commandmap.get("VERSION");
                    equationversions.put(name, version);
                }
            }
            table.setequations(equationversions);
            JPanel leftpanel = table.getpanel();
            DisplayPanel.add(leftpanel, "wrap");//, "dock west");

//</editor-fold>
        } else {
            skip = true;
        }
        currentstep = step.getnumber();
        if (step.CheckSkip()&&skip==true) {
            if(forward){
                LoadNextStep();
            }else{
                LoadPrevStep();
            }
        } else {
            JPanel rightpanel = step.getpanel();
            DisplayPanel.add(rightpanel);//, "dock west");
            pnlMain.revalidate();
            DisplayPanel.revalidate();
            scpDisplay.revalidate();
        }
    }

    private void loadvariables(Element WizardXML) {
        Element variablelist = WizardXML.getChild("VARIABLES");
        for (Element variable : variablelist.getChildren()) {
            String name = variable.getName();
            String command = variable.getAttributeValue("value");
            HashMap<String, String> commandmap = Global.makecommandmap(command);
            String value = commandmap.get("DEFAULT");
            Variables.put(name, value);
        }
    }

    private void LoadHome() {
        checksave = false;
        Variables.clear();
        steps.clear();
        currentstep = 0;
        btnCancel.setEnabled(false);
        btnPrevious.setEnabled(false);
        btnNext.setEnabled(false);
        btnFinish.setEnabled(false);
        pnlMain.removeAll();
        pnlMain.add(this.pnlMenu);
        pnlMain.revalidate();
        pnlMenu.repaint();
    }

    private void LoadNextStep() {
        forward = true;
        Integer next = currentstep + 1;
        next = steps.ceilingKey(next);
        if (next == null) {
            JOptionPane.showMessageDialog(null, "There are no further steps, please press finish when done or cancel to undo and delete the project so far.");
        } else {
            WizardStep step = steps.get(currentstep);
            TreeMap<String, JComponent> inputs = step.getinputs();
            for (String key : inputs.keySet()) {
                if (Variables.containsKey(key)) {
                    JComponent component = inputs.get(key);
                    if (component instanceof JTextField) {
                        JTextField field = (JTextField) component;
                        Variables.put(key, field.getText());
                    } else if (component instanceof JComboBox) {
                        JComboBox box = (JComboBox) component;
                        Variables.put(key, box.getSelectedItem().toString());
                    }
                }
            }
            Element results = step.getresults();

            TreeMap<Integer, Element> tempresults = new TreeMap();
            for (Element resultline : results.getChildren()) {
                String name = resultline.getName();
                if (name.startsWith("RESULT") && Global.isInteger(name.substring(6))) {
                    Integer number = Integer.parseInt(name.substring(6));
                    if (!tempresults.containsKey(number) && number > 0) {
                        tempresults.put(number, resultline);
                    }
                }
            }

            for (Integer key : tempresults.keySet()) {
                Element result = tempresults.get(key);

                HashMap<String, String> resultmap = Global.makecommandmap(result.getAttributeValue("value"));
                String variable = resultmap.get("VARIABLE");
                if (Variables.containsKey(variable)) {

                    String setto = resultmap.get("SETTO");
                    if (setto != null && setto.equals("TEXT")) {
                        String text = result.getChildText("TEXT");
                        if (text != null) {
                            Variables.put(variable, text);
                        }
                    } else if (setto != null && setto.startsWith("VARIABLES,")) {
                        String[] Path = setto.split(",", 2);
                        if (Variables.containsKey(Path[1])) {
                            Variables.put(variable, Variables.get(Path[1]));
                        }
                    }
                }
            }

            WizardStep nextstep = steps.get(next);
            currentstep = next;
            loadstep(nextstep, true);
        }
    }

    private void createtables() {

        ArrayList<String> tablenames = new ArrayList();
        ArrayList<String> foreignkeystrings = new ArrayList();
        ArrayList<String> tablestrings = new ArrayList();
        Element tablelist = Template.getChild("TABLES");
        SQLHandling.createschema(ProjectName);

        for (Element table : tablelist.getChildren()) {
            String name = table.getName();
            if (!tablenames.contains(name)) {
                tablenames.add(name);
                String primary = "";
                Element columnlist = table.getChild("COLUMNS");
                ArrayList<String> columnstrings = new ArrayList();
                for (Element currentcolumn : columnlist.getChildren()) {
                    String columnname = currentcolumn.getName();
                    String commandstring = currentcolumn.getAttributeValue("value");
                    HashMap<String, String> columnmap = Global.makecommandmap(commandstring);
                    String type = columnmap.get("TYPE");
                    String ColumnString = columnname;
                    switch (type) {
                        case "STRING":
                            ColumnString = ColumnString + " CLOB, ";
                            break;
                        case "DATE":
                            ColumnString = ColumnString + " DATE, ";
                            break;
                        case "DOUBLE":
                            ColumnString = ColumnString + " DECIMAL, ";
                            break;
                        case "INTEGER":
                            ColumnString = ColumnString + " INTEGER, ";
                            break;
                        case "PRIMARY":
                            ColumnString = ColumnString + " INTEGER AUTO_INCREMENT, ";
                            primary = columnname;
                            break;
                        case "FOREIGN":
                            ColumnString = ColumnString + " INTEGER, ";
                            String link = columnmap.get("LINKTO");
                            String[] path = link.split(",", 2);
                            foreignkeystrings.add("ALTER TABLE " + ProjectName + "." + name + " ADD FOREIGN KEY (" + columnname + ") REFERENCES " + ProjectName + "." + path[0] + "(" + path[1] + ")");
                            break;
                        case "NAMECOLUMN":
                            ColumnString = ColumnString + " CLOB, ";
                            break;
                        default:
                            ColumnString = ColumnString + " CLOB, ";
                            break;
                    }
                    columnstrings.add(ColumnString);
                }
                String tablestring = "CREATE TABLE IF NOT EXISTS " + ProjectName + "." + name + "(";
                for (String curstring : columnstrings) {
                    tablestring = tablestring + curstring;
                }
                tablestring = tablestring + "PRIMARY KEY (" + primary + "));";
                tablestrings.add(tablestring);
            }
        }
        Connection c = null;
        Statement stmt = null;
        try {
            c = Global.getConnectionPool().getConnection();
            stmt = c.createStatement();
            for (String createtable : tablestrings) {
                stmt.executeUpdate(createtable);
            }
            for (String addforeign : foreignkeystrings) {
                stmt.execute(addforeign);
            }

            stmt.close();
            c.close();
            Global.Printmessage("Tables Created Successfully.");
        } catch (Exception e) {
            Global.Printmessage("create db " + e.getClass().getName() + ": " + e.getMessage());

        } finally {
            try {
                if (stmt != null && !stmt.isClosed()) {
                    stmt.close();
                }
                if (c != null && !c.isClosed()) {
                    c.close();
                }
            } catch (Exception e) {
                Global.Printmessage("create db " + e.getClass().getName() + ": " + e.getMessage());
            }
        }
    }

    private void Finish() {
        checksave = false;
        boolean loopcomplete = false;
        if (newproject) {
            while (!loopcomplete) {
                if (currentstep == steps.lastKey()) {
                    loopcomplete = true;
                } else {
                    LoadNextStep();
                }
            }

            Element tables = Template.getChild("TABLES").clone();
            //the appropriate information about the columns is already loaded in the xml we just need to set the equations and save everything in the right spot.
            for (Element table : tables.getChildren()) {
                String tablename = table.getName();
                Element equationlist = table.getChild("EQUATIONS");
                WizardTableNode currentnode = getWizardTable(tablename);
                table.removeChild("EQUATIONS");
                Element newequations = new Element("EQUATIONS");
                if (equationlist != null && currentnode != null) {
                    HashMap<String, String> equations = currentnode.getequations();
                    for (String Equationname : equations.keySet()) {
                        Element equation = equationlist.getChild(Equationname);
                        String version = equations.get(Equationname);
                        Element eqversion = equation.getChild(version).detach();
                        eqversion.setName(Equationname);
                        newequations.addContent(eqversion);
                    }
                } else if (equationlist != null && currentnode == null) {
                    for (Element equation : equationlist.getChildren()) {
                        String Equationname = equation.getName();
                        String eqstring = equation.getAttributeValue("value");
                        HashMap<String, String> eqmap = Global.makecommandmap(eqstring);
                        if (eqmap == null || !eqmap.containsKey("DEFAULT")) {
                            Element versionlist = equation.getChild(("VERSIONS"));
                            TreeMap<Integer, Element> tempversions = new TreeMap();
                            for (Element version : versionlist.getChildren()) {
                                String name = version.getName();
                                if (name.startsWith("VERSION") && Global.isInteger(name.substring(7))) {
                                    Integer number = Integer.parseInt(name.substring(7));
                                    if (!tempversions.containsKey(number) && number > 0) {
                                        tempversions.put(number, version);
                                    }
                                }
                            }
                            Element version = tempversions.firstEntry().getValue();
                            if (version != null) {
                                String vername = version.getName();
                                Element eqversion = versionlist.getChild(vername).detach();
                                eqversion.setName(Equationname);
                                newequations.addContent(eqversion);

                            }
                        } else {
                            String vername = eqmap.get("DEFAULT");
                            Element eqversion = equation.getChild(vername);
                            if (eqversion != null) {
                                eqversion = eqversion.detach();
                                eqversion.setName(Equationname);
                                newequations.addContent(eqversion);

                            }
                        }
                    }
                    table.addContent(newequations);
                }
            }
            XMLHandling.addpath(new String[]{"PROJECTS", ProjectName}, tables, Global.getxmlfilename(), true);
            newproject = false;
            Home.WizardComplete();
        }
        LoadHome();
    }

    private void Cancel() {
        checksave = false;
        if (newproject) {
            ArrayList<String> Tables = SQLHandling.getlistoftables(ProjectName);
            Connection c = null;
            Statement stmt = null;
            try {
                c = Global.getConnectionPool().getConnection();
                stmt = c.createStatement();
                for (String Table : Tables) {
                    stmt.executeUpdate("ALTER TABLE " + ProjectName + "." + Table + " SET REFERENTIAL_INTEGRITY FALSE");
                }
                for (String Table : Tables) {
                    stmt.executeUpdate("DROP TABLE " + ProjectName + "." + Table + ";");
                }
                stmt.executeUpdate("DROP SCHEMA " + ProjectName);
                stmt.close();
                c.close();
                Global.Printmessage("Tables Deleted Successfully.");
            } catch (Exception e) {
                Global.Printmessage("create db " + e.getClass().getName() + ": " + e.getMessage());

            } finally {
                try {
                    if (stmt != null && !stmt.isClosed()) {
                        stmt.close();
                    }
                    if (c != null && !c.isClosed()) {
                        c.close();
                    }
                } catch (Exception e) {
                    Global.Printmessage("create db " + e.getClass().getName() + ": " + e.getMessage());
                }
            }
        }
        LoadHome();
    }

    private void LoadPrevStep() {
        forward = false;
        Integer previous = currentstep - 1;
        previous = steps.floorKey(previous);
        if (previous != null) {
            loadstep(steps.get(previous), false);
        }
    }

    public void addWizardTable(String tablename, WizardTableNode table) {
        WizardTableList.put(tablename, table);
    }

    public WizardTableNode getWizardTable(String tablename) {
        return WizardTableList.get(tablename);
    }

    public boolean hasWizardTable(String tablename) {
        return WizardTableList.containsKey(tablename);
    }

    public String getWizardTableNameColumn(String tablename) {
        if (WizardTableList.containsKey(tablename)) {
            WizardTableNode node = WizardTableList.get(tablename);
            return node.getNameColumn();
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

        btnCancel = new javax.swing.JButton();
        btnPrevious = new javax.swing.JButton();
        btnNext = new javax.swing.JButton();
        btnFinish = new javax.swing.JButton();
        pnlMain = new javax.swing.JPanel();
        pnlMenu = new javax.swing.JPanel();
        lblTop = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstWizards = new javax.swing.JList();
        btnLoad = new javax.swing.JButton();
        btnDefault = new javax.swing.JButton();
        lblMessage = new javax.swing.JLabel();

        setMaximumSize(new java.awt.Dimension(1000, 650));
        setMinimumSize(new java.awt.Dimension(1000, 650));
        setPreferredSize(new java.awt.Dimension(1000, 650));

        btnCancel.setText("Cancel");
        btnCancel.setEnabled(false);
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        btnPrevious.setText("Previous");
        btnPrevious.setEnabled(false);
        btnPrevious.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPreviousActionPerformed(evt);
            }
        });

        btnNext.setText("Next");
        btnNext.setEnabled(false);
        btnNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNextActionPerformed(evt);
            }
        });

        btnFinish.setText("Finish");
        btnFinish.setEnabled(false);
        btnFinish.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFinishActionPerformed(evt);
            }
        });

        lblTop.setText("Available Wizards");

        jScrollPane1.setViewportView(lstWizards);

        btnLoad.setText("Load Wizard");
        btnLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoadActionPerformed(evt);
            }
        });

        btnDefault.setText("Use Default Settings");
        btnDefault.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDefaultActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlMenuLayout = new javax.swing.GroupLayout(pnlMenu);
        pnlMenu.setLayout(pnlMenuLayout);
        pnlMenuLayout.setHorizontalGroup(
            pnlMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMenuLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTop)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(pnlMenuLayout.createSequentialGroup()
                .addGroup(pnlMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblMessage)
                    .addComponent(btnLoad, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1)
                    .addComponent(btnDefault, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 742, Short.MAX_VALUE))
        );
        pnlMenuLayout.setVerticalGroup(
            pnlMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMenuLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTop)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnLoad, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDefault)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblMessage)
                .addContainerGap(241, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlMenu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        pnlMainLayout.setVerticalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlMenu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(btnCancel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnPrevious)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnNext)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnFinish)
                .addGap(0, 663, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnlMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancel)
                    .addComponent(btnPrevious)
                    .addComponent(btnNext)
                    .addComponent(btnFinish))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        Cancel();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnPreviousActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPreviousActionPerformed
        LoadPrevStep();
    }//GEN-LAST:event_btnPreviousActionPerformed

    private void btnNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextActionPerformed
        LoadNextStep();
    }//GEN-LAST:event_btnNextActionPerformed

    private void btnFinishActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFinishActionPerformed
        Finish();
    }//GEN-LAST:event_btnFinishActionPerformed

    private void btnDefaultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDefaultActionPerformed
        NewNoWizard();
    }//GEN-LAST:event_btnDefaultActionPerformed

    private void btnLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoadActionPerformed
        int index = lstWizards.getSelectedIndex();
        if (index > -1) {
            String Wizard = lstWizards.getSelectedValue().toString();
            if (newproject) {
                NewWithWizard(Wizard);
            } else {
                OldWithWizards(Wizard);
            }
        }
    }//GEN-LAST:event_btnLoadActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnDefault;
    private javax.swing.JButton btnFinish;
    private javax.swing.JButton btnLoad;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnPrevious;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblMessage;
    private javax.swing.JLabel lblTop;
    private javax.swing.JList lstWizards;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JPanel pnlMenu;
    // End of variables declaration//GEN-END:variables

    @Override
    public boolean IsSaved() {
        return checksave;
    }

    @Override
    public int SaveCheck() {
        if (checksave) {
            int answer = JOptionPane.showConfirmDialog(null, "Would you like this project to be created before exiting?", "Save Changes", JOptionPane.YES_NO_CANCEL_OPTION);
            if (answer == JOptionPane.YES_OPTION) {
                Finish();
            } else if (answer == JOptionPane.NO_OPTION) {
                Cancel();
            }
            return answer;
        }
        return JOptionPane.NO_OPTION;
    }
}
