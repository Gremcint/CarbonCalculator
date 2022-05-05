/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Prototype.WizardBuilding;

import Prototype.StaticClasses.Global;
import Prototype.StaticClasses.XMLHandling;
import org.jdom2.Element;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import javax.swing.table.DefaultTableModel;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JScrollPane;
import javax.swing.JDialog;
import java.awt.Dimension;
import java.util.Collections;
import java.util.Map;

/**
 *
 * @author Gregory
 */
public class TemplateBranch extends javax.swing.JPanel {

    private Element Table;
    private Element source;
    private Element condition;
    private DefaultTableModel formmodel;
    private DefaultTableModel resultmodel;
    private DefaultTableModel tablemodel;
    private WizardBuilder parent;
    private TreeMap<Integer, Element> lines = new TreeMap();
    private TreeMap<Integer, Element> results = new TreeMap();
    private Integer branchnumber;
    private ComboTable tblTable;
    private boolean loading = false;

    /**
     * Creates new form TemplateTab
     */
    public TemplateBranch(Integer BranchNumber, WizardBuilder Parent) {
        initComponents();
        tblTable = new ComboTable();
        parent = Parent;
        source = new Element("BRANCH" + BranchNumber);
        formmodel = (DefaultTableModel) this.tblForm.getModel();
        resultmodel = (DefaultTableModel) this.tblResults.getModel();
        tablemodel = new DefaultTableModel();
        tablemodel.addColumn("Equation Name");
        tablemodel.addColumn("Version");
        tblTable.setModel(tablemodel);
        tblTable.setRowHeight(28);
        tblTable.setShowGrid(true);
        tblTable.setRowMargin(0);
        TableListPanel.setLayout(new java.awt.BorderLayout());
        source.addContent(new Element("CONDITION"));
        source.addContent(new Element("FUNCTION"));
        source.getChild("FUNCTION").addContent(new Element("FORM"));
        source.getChild("FUNCTION").addContent(new Element("TABLE"));
        source.addContent("RESULTS");
        condition = source.getChild("CONDITION");
        branchnumber = BranchNumber;
        this.loadcondition();

        this.loadtablelist();
        loading = true;
        cboTable.setSelectedItem("");
        loading = false;
        JScrollPane scroll = new JScrollPane(tblTable);
        scroll.setPreferredSize(new Dimension(570, 168));
        TableListPanel.add(scroll);
        TableListPanel.repaint();
        TableListPanel.revalidate();
    }

    public TemplateBranch(Element Source, WizardBuilder Parent, Integer Branchnumber) {
        initComponents();

        parent = Parent;
        source = Source;
        formmodel = (DefaultTableModel) this.tblForm.getModel();
        resultmodel = (DefaultTableModel) this.tblResults.getModel();
        tblTable = new ComboTable();
        tablemodel = new DefaultTableModel();

        tablemodel.addColumn("Equation Name");
        tablemodel.addColumn("Version");
        tblTable.setModel(tablemodel);
        tblTable.setRowHeight(28);
        tblTable.setShowGrid(true);
        tblTable.setRowMargin(0);
        condition = source.getChild("CONDITION");
        branchnumber = Branchnumber;
        this.loadcondition();
        this.loadtablelist();
        Element Function = source.getChild("FUNCTION");
        if (Function != null) {
            Element Form = Function.getChild("FORM");
            if (Form != null) {
                TreeMap<Integer, Element> templines = new TreeMap();
                for (Element line : Form.getChildren()) {
                    String name = line.getName();
                    if (name.startsWith("LINE") && Global.isInteger(name.substring(4))) {
                        Integer linenumber = Integer.parseInt(name.substring(4));
                        if (!templines.containsKey(linenumber) && linenumber > 0) {
                            templines.put(linenumber, line);
                        }
                    }
                }
                lines = new TreeMap();
                for (Integer key : templines.keySet()) {
                    int lineindex = lines.size();
                    lines.put(lineindex, templines.get(key));
                }
            }

            Element TableChoice = Function.getChild("TABLE");
            if (TableChoice != null) {
                HashMap<String, String> tablemap = Global.makecommandmap(TableChoice.getAttributeValue("value"));
                String tablename = tablemap.get("TABLE");
                if (tablename != null) {
                    HashMap<String, String> Versions = new HashMap();
                    cboTable.setSelectedItem(tablename);
                    Element Equations = TableChoice.getChild("EQUATIONS");
                    if (Equations != null) {
                        for (Element Equation : Equations.getChildren()) {
                            String Name = Equation.getName();
                            String Eqtext = Equation.getAttributeValue("value");
                            if (Eqtext != null) {
                                HashMap<String, String> EqMap = Global.makecommandmap(Eqtext);
                                String Version = EqMap.get("VERSION");
                                if (Version != null) {
                                    Versions.put(Name, Version);
                                }
                            }
                        }
                    }
                    for (int x = 0; x < tblTable.getModel().getRowCount(); x++) {
                        String name = tblTable.getValueAt(x, 0).toString();
                        String Version = Versions.get(name);
                        if (Version != null) {
                            tblTable.setValueAt(Version, x, 1);
                        }
                    }
                } else {
                    cboTable.setSelectedItem("");
                }
            }
        }
        this.loadfunctions();

        Element resultlist = source.getChild("RESULTS");
        if (resultlist != null) {
            TreeMap<Integer, Element> tempresults = new TreeMap();
            for (Element currentresult : resultlist.getChildren()) {
                String name = currentresult.getName();
                if (name.startsWith("RESULT") && Global.isInteger(name.substring(6))) {
                    Integer linenumber = Integer.parseInt(name.substring(4));
                    if (!tempresults.containsKey(linenumber) && linenumber > 0) {
                        tempresults.put(linenumber, currentresult);
                    }
                }
            }
            results = new TreeMap();
            for (Integer key : tempresults.keySet()) {
                int lineindex = results.size();
                results.put(lineindex, tempresults.get(key));
            }
        }
        this.loadresults();
        JScrollPane scroll = new JScrollPane(tblTable);
        scroll.setPreferredSize(new Dimension(570, 168));
        TableListPanel.setLayout(new java.awt.BorderLayout());

        TableListPanel.add(scroll);
        TableListPanel.repaint();
        TableListPanel.revalidate();

    }

    public ArrayList<String> checkusage(String Variable, String StepName, String BranchName) {
        //need to check condition, form and results
        ArrayList<String> Usages = new ArrayList();
        String conditiontext = condition.getAttributeValue("value");
        if (conditiontext != null && !conditiontext.isEmpty() && conditiontext.contains(Variable)) {
            ArrayList<String> parselist = new ArrayList();
            parselist.addAll(java.util.Arrays.asList("(", ")", "+", "-", "/", "*", "^", " ", "^", ",", "{", "}", "=", "<", ">", "!", "&&", "||"));
            for (String str : parselist) {
                conditiontext = conditiontext.replace(str, "`" + str + "`");
            }
            conditiontext = "`" + conditiontext + "`";
            if (conditiontext.contains("`" + Variable + "`")) {
                Usages.add(StepName + ": " + BranchName + ": Condition");
            }
        }

        for (Map.Entry<Integer, Element> line : lines.entrySet()) {
            Element linevalue = line.getValue();
            String current = linevalue.getAttributeValue("value");
            if (current != null && !current.isEmpty() && (current.contains("STORE:" + Variable + ";") || current.endsWith("STORE:" + Variable + ";"))) {
                Usages.add(StepName + ": " + BranchName + ": Form: Line " + line.getKey());
            }
        }
        for (Map.Entry<Integer, Element> result : results.entrySet()) {
            Element resultvalue = result.getValue();
            String current = resultvalue.getAttributeValue("value");
            if (current != null && !current.isEmpty() && (current.contains("VARIABLE:" + Variable + ";") || current.endsWith("VARIABLE:" + Variable + ";") || current.contains("VARIABLES," + Variable + ";") || current.endsWith("VARIABLES," + Variable))) {
                Usages.add(StepName + ": " + BranchName + ": Results: Line " + result.getKey());
            }
        }

        return Usages;

    }

    public void renamevariable(String oldname, String newname) {
        //need to check condition, form and results

        String conditiontext = condition.getAttributeValue("value");
        if (conditiontext != null && !conditiontext.isEmpty() && conditiontext.contains(oldname)) {
            ArrayList<String> parselist = new ArrayList();
            parselist.addAll(java.util.Arrays.asList("(", ")", "+", "-", "/", "*", "^", " ", "^", ",", "{", "}", "=", "<", ">", "!", "&&", "||"));
            for (String str : parselist) {
                conditiontext = conditiontext.replace(str, "`" + str + "`");
            }
            conditiontext = "`" + conditiontext + "`";
            if (conditiontext.contains("`" + oldname + "`")) {
                conditiontext = conditiontext.replace("`" + oldname + "`", newname);
            }
            conditiontext = conditiontext.replace("`", "");
            condition.setAttribute("value", conditiontext);
        }
        TreeMap<Integer, Element> templines = new TreeMap();
        for (Map.Entry<Integer, Element> line : lines.entrySet()) {
            Element linevalue = line.getValue();
            String current = linevalue.getAttributeValue("value");
            if (!current.endsWith(";")) {
                current = current + ";";
            }
            if (current != null && !current.isEmpty() && (current.contains(":" + oldname + ";"))) {
                current = current.replace(":" + oldname + ";", ":" + newname + ";");
                linevalue.setAttribute("value", current);
                lines.remove(line.getKey());
                templines.put(line.getKey(), linevalue);
            }

        }
        lines.putAll(templines);
        TreeMap<Integer, Element> tempresults = new TreeMap();
        for (Map.Entry<Integer, Element> result : results.entrySet()) {
            Element resultvalue = result.getValue();
            String current = resultvalue.getAttributeValue("value");
            if (!current.endsWith(";")) {
                current = current + ";";
            }
            if (current != null && !current.isEmpty() && (current.contains(":" + oldname + ";") || current.contains("," + oldname + ";"))) {
                current = current.replace(":" + oldname + ";", ":" + newname + ";");
                current = current.replace("," + oldname + ";", "," + newname + ";");
                resultvalue.setAttribute("value", current);
                results.remove(result.getKey());
                tempresults.put(result.getKey(), resultvalue);
            }
        }
        results.putAll(tempresults);
    }

    public void deletevariable(String Variable) {
        //need to check condition, form and results

        String conditiontext = condition.getAttributeValue("value");
        if (conditiontext != null && !conditiontext.isEmpty() && conditiontext.contains(Variable)) {
            ArrayList<String> parselist = new ArrayList();
            parselist.addAll(java.util.Arrays.asList("(", ")", "+", "-", "/", "*", "^", " ", "^", ",", "{", "}", "=", "<", ">", "!", "&&", "||"));
            for (String str : parselist) {
                conditiontext = conditiontext.replace(str, "`" + str + "`");
            }
            conditiontext = "`" + conditiontext + "`";
            if (conditiontext.contains("`" + Variable + "`")) {
                conditiontext = conditiontext.replace("`" + Variable + "`", "`");
            }
            conditiontext = conditiontext.replace("`", "");
            condition.setAttribute("value", conditiontext);
        }
        TreeMap<Integer, Element> templines = new TreeMap();
        for (Map.Entry<Integer, Element> line : lines.entrySet()) {
            Element linevalue = line.getValue();
            String current = linevalue.getAttributeValue("value");
            if (!current.endsWith(";")) {
                current = current + ";";
            }
            if (current != null && !current.isEmpty() && (current.contains(":" + Variable + ";"))) {
                current = current.replace(":" + Variable + ";", ":;");
                linevalue.setAttribute("value", current);
                lines.remove(line.getKey());
                templines.put(line.getKey(), linevalue);
            }

        }
        lines.putAll(templines);

        TreeMap<Integer, Element> tempresults = new TreeMap();
        for (Map.Entry<Integer, Element> result : results.entrySet()) {
            Element resultvalue = result.getValue();
            String current = resultvalue.getAttributeValue("value");
            if (!current.endsWith(";")) {
                current = current + ";";
            }
            if (current != null && !current.isEmpty() && current.contains("VARIABLE:" + Variable + ";")) {
                results.remove(result.getKey());
            } else if (current != null && !current.isEmpty() && current.contains("VARIABLES," + Variable + ";")) {
                current = current.replace("VARIABLES," + Variable + ";", ";");
                resultvalue.setAttribute("value", current);
                results.remove(result.getKey());
                tempresults.put(result.getKey(), resultvalue);
            }
        }
        results.putAll(tempresults);
    }

    public void refresh() {
        this.loadcondition();
        this.loadfunctions();
        this.loadtablelist();
        this.loadtable();
        this.loadresults();
    }

    public Element Save() {
        Element branch = new Element(source.getName());
        branch.addContent(condition.detach());
        Element function = new Element("FUNCTION");

        Element form = new Element("FORM");
        for (Map.Entry<Integer, Element> line : this.lines.entrySet()) {
            Integer number = line.getKey();
            Element linevalue = line.getValue();
            linevalue.setName("LINE" + number);
            form.addContent(linevalue.detach());
        }
        function.addContent(form);
        branch.addContent(function);
        Element resultlist = new Element("RESULTS");
        for (Map.Entry<Integer, Element> result : this.results.entrySet()) {
            Integer number = result.getKey();
            Element linevalue = result.getValue();
            linevalue.setName("RESULT" + number);
            resultlist.addContent(linevalue.detach());
        }
        branch.addContent(resultlist);

        String tablename = this.cboTable.getSelectedItem().toString();
        if (!tablename.isEmpty()) {
            Element table = new Element("TABLE");
            table.setAttribute("value", "TABLE:" + tablename);
            Element equations = new Element("EQUATIONS");
            table.addContent(equations);
            for (int x = 0; x < tablemodel.getRowCount(); x++) {
                String eqname = tablemodel.getValueAt(x, 0).toString();
                String version = tablemodel.getValueAt(x, 1).toString();
                Element currenteq = new Element(eqname);
                currenteq.setAttribute("value", "VERSION:" + version);
                equations.addContent(currenteq);
            }
            function.addContent(table);
        }
        return branch;
    }

    private void loadcondition() {
        if (condition != null && condition.getAttributeValue("value") != null) {
            String convalue = condition.getAttributeValue("value");
            if (convalue.contains("TXT:")) {
                String[] eqterms = convalue.split("`");
                convalue = convalue.replace("`", "");
                Element TERMS = condition.getChild("TERMS");
                if (TERMS != null) {
                    for (String term : eqterms) {
                        if (term.startsWith("TXT:")) {
                            String termname = term.substring(0, 4);
                            Element termxml = TERMS.getChild(termname);
                            if (termxml != null) {
                                convalue = convalue.replaceAll(term, "'" + termxml.getText() + "'");
                            }
                        }
                    }

                }
            } else {
                convalue = convalue.replace("`", "");

            }
            this.lblCondition.setText(convalue);
        } else {
            lblCondition.setText("Click Edit to add a condition");
        }
    }

    private void loadtablelist() {
        loading = true;
        String Schema = Global.getcurrentschema();
        if (Global.getMode().equals(Global.PROJECTMODE)) {
            Schema = "PROJECTS," + Schema;
        } else {
            Schema = "TEMPLATES," + Schema;
        }
        String Pathstring = Schema + ",TABLES";
        String Path[] = Pathstring.split(",");
        Element tables = XMLHandling.getpath(Path, Global.getxmlfilename());
        DefaultComboBoxModel combomodel = (DefaultComboBoxModel) cboTable.getModel();
        int cboindex = cboTable.getSelectedIndex();
        String currentchoice = null;
        if (cboindex > -1) {
            currentchoice = cboTable.getSelectedItem().toString();
        }
        combomodel.removeAllElements();
        combomodel.addElement("");
        for (Element current : tables.getChildren()) {
            combomodel.addElement(current.getName());
        }
        cboTable.setModel(combomodel);
        if (currentchoice != null && combomodel.getIndexOf(currentchoice) > -1) {
            cboTable.setSelectedItem(currentchoice);
        } else {
            cboTable.setSelectedItem("");
        }
        loading = false;
    }

    private void loadtable() {
        try {
            tblTable.removealldropdowns();
            int cbochoice = cboTable.getSelectedIndex();

            tablemodel = new DefaultTableModel();
            tablemodel.addColumn("Equation Name");
            tablemodel.addColumn("Version");
            tblTable.setModel(tablemodel);
            if (cbochoice > -1) {
                String TableChoice = cboTable.getSelectedItem().toString();
                if (!TableChoice.isEmpty()) {
                    String Schema = Global.getcurrentschema();
                    if (Global.getMode().equals(Global.PROJECTMODE)) {
                        Schema = "PROJECTS," + Schema;
                    } else {
                        Schema = "TEMPLATES," + Schema;
                    }
                    String path = Schema + ",TABLES," + TableChoice;
                    Table = XMLHandling.getpath(path.split(","), Global.getxmlfilename());

                    Element Equations = Table.getChild("EQUATIONS");
                    if (Equations != null) {
                        if (Global.getMode().equals(Global.TEMPLATEMODE)) {
                            for (Element currenteq : Equations.getChildren()) {
                                String name = currenteq.getName();
                                ArrayList<String> versions = new ArrayList();
                                Element Versions = currenteq.getChild("VERSIONS");
                                for (Element currentver : Versions.getChildren()) {
                                    String versionname = currentver.getName();
                                    if (versionname.startsWith("VERSION") && Global.isInteger(versionname.substring(7)) && !versions.contains(versionname)) {
                                        versions.add(versionname);
                                    }
                                }
                                Collections.sort(versions);
                                JComboBox newbox = new JComboBox(versions.toArray());

                                String[] Path = {"FUNCTION", "TABLE", "EQUATIONS", name};
                                Element eqVersion = XMLHandling.getpath(Path, source);
                                String[] row = {name, ""};

                                if (eqVersion != null) {
                                    HashMap<String, String> eqMap = Global.makecommandmap(eqVersion.getAttributeValue("value"));
                                    String version = eqMap.get("VERSION");
                                    if (version != null) {
                                        row[1] = version;
                                    }
                                } else {
                                    row[1] = newbox.getSelectedItem().toString();
                                }

                                int rownumber = tablemodel.getRowCount();
                                this.tablemodel.addRow(row);
                                tblTable.adddropdown(rownumber, 1, newbox);
                            }

                            tblTable.setColumnSelectionAllowed(true);
                            tblTable.repaint();
                            tblTable.revalidate();
                        }
                        else{
                            for (Element currenteq : Equations.getChildren()) {
                                String name = currenteq.getName();
                                
                                JComboBox newbox = new JComboBox(new String[]{"Not Applicable"});

                                String[] row = {name, ""};
                                    row[1] = newbox.getSelectedItem().toString();
                                int rownumber = tablemodel.getRowCount();
                                this.tablemodel.addRow(row);
                                tblTable.adddropdown(rownumber, 1, newbox);
                            }

                            tblTable.setColumnSelectionAllowed(true);
                            tblTable.repaint();
                            tblTable.revalidate();
                        }
                    }
                }
            } else {
                cboTable.setSelectedItem("");
            }
        } catch (Exception e) {
            Global.Printmessage("Branch Load Table " + e.getClass() + ":" + e.getMessage());
        }
    }

    public TemplateBranch clone(Integer newbranchnumber) {
        Element newxml = this.Save();
        TemplateBranch newbranch = new TemplateBranch(newxml, this.parent, newbranchnumber);
        return newbranch;
    }

    public Integer getbranchnumber() {
        return branchnumber;
    }

    private void loadfunctions() {
        for (int row = formmodel.getRowCount() - 1; row >= 0; row--) {
            formmodel.removeRow(row);
        }
        Integer linenumber = 1;
        TreeMap<Integer, Element> templines = new TreeMap();
        while (lines.size() > 0) {
            Element currentline = lines.get(lines.firstKey());
            currentline.setName("LINE" + linenumber);
            lines.remove(lines.firstKey());
            templines.put(linenumber, currentline);
            linenumber++;
            HashMap<String, String> linemap = Global.makecommandmap(currentline.getAttributeValue("value"));
            String caption = linemap.get("CAPTION");
            if (caption == null) {
                caption = "";
            }

            String type = linemap.get("TYPE");
            if (type == null) {
                type = "";
            }

            String defaultvalue = linemap.get("DEFAULT");
            if (defaultvalue == null) {
                defaultvalue = "";
            }

            String Store = linemap.get("STORE");
            if (Store == null) {
                Store = "";
            }
            String[] linedata = {caption, defaultvalue, type, Store};
            formmodel.addRow(linedata);
        }
        lines = templines;
        tblForm.repaint();
    }

    private void loadresults() {

        for (int row = resultmodel.getRowCount() - 1; row >= 0; row--) {
            resultmodel.removeRow(row);
        }

        for (Integer currentresult : results.keySet()) {
            HashMap<String, String> resultmap = Global.makecommandmap(results.get(currentresult).getAttributeValue("value"));

            String variable = resultmap.get("VARIABLE");
            String setto = resultmap.get("SETTO");
            String value = "";
            if (setto == null) {
                value = "Not found";
            } else if (setto.equals("TEXT")) {
                value = "TEXT:" + results.get(currentresult).getChildText("TEXT");
            } else if (setto.startsWith("VARIABLES,")) {
                String[] Path = setto.split(",", 2);
                if (Path.length > 1) {
                    value = Path[1];
                } else {
                    value = "Not found";
                }
            }
            String[] row = {variable, value};
            resultmodel.addRow(row);
            tblResults.repaint();
        }
    }

    @Override
    public String getName() {
        return "BRANCH" + branchnumber;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        lblCondition = new javax.swing.JLabel();
        btnEditCon = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        cboTable = new javax.swing.JComboBox();
        TableListPanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tblResults = new javax.swing.JTable();
        btnAddValue = new javax.swing.JButton();
        btnEditResult = new javax.swing.JButton();
        btnRemoveValue = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblForm = new javax.swing.JTable();
        btnAddLine = new javax.swing.JButton();
        btnDeleteLine = new javax.swing.JButton();
        btnSettings = new javax.swing.JButton();
        btnMoveDown = new javax.swing.JButton();
        btnMoveUp = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Condition", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP));

        lblCondition.setText("jLabel2");

        btnEditCon.setText("Edit");
        btnEditCon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditConActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblCondition)
                    .addComponent(btnEditCon, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblCondition)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEditCon)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel1.setText("Table:");

        cboTable.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cboTable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboTableActionPerformed(evt);
            }
        });

        TableListPanel.setMaximumSize(new java.awt.Dimension(553, 168));
        TableListPanel.setMinimumSize(new java.awt.Dimension(553, 168));

        javax.swing.GroupLayout TableListPanelLayout = new javax.swing.GroupLayout(TableListPanel);
        TableListPanel.setLayout(TableListPanelLayout);
        TableListPanelLayout.setHorizontalGroup(
            TableListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        TableListPanelLayout.setVerticalGroup(
            TableListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 321, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(TableListPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 515, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cboTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cboTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(TableListPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Table", jPanel2);

        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel4.setText("Results:");

        jScrollPane4.setPreferredSize(new java.awt.Dimension(245, 124));

        tblResults.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Value", "Equals"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane4.setViewportView(tblResults);

        btnAddValue.setText("Add Value");
        btnAddValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddValueActionPerformed(evt);
            }
        });

        btnEditResult.setText("Edit Result");
        btnEditResult.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditResultActionPerformed(evt);
            }
        });

        btnRemoveValue.setText("Remove Value");
        btnRemoveValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveValueActionPerformed(evt);
            }
        });

        jScrollPane3.setPreferredSize(new java.awt.Dimension(245, 124));

        tblForm.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Caption", "Default Value", "Input Type", "Store Result In"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(tblForm);

        btnAddLine.setText("Add Line");
        btnAddLine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddLineActionPerformed(evt);
            }
        });

        btnDeleteLine.setText("Delete Line");
        btnDeleteLine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteLineActionPerformed(evt);
            }
        });

        btnSettings.setText("Edit Settings");
        btnSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSettingsActionPerformed(evt);
            }
        });

        btnMoveDown.setText("Move Down");
        btnMoveDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoveDownActionPerformed(evt);
            }
        });

        btnMoveUp.setText("Move Up");
        btnMoveUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoveUpActionPerformed(evt);
            }
        });

        jLabel2.setText("Input Form:");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(btnRemoveValue, javax.swing.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE)
                                .addComponent(btnEditResult, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnMoveUp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnMoveDown, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnSettings, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnDeleteLine, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnAddLine, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(btnAddValue, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel2))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(btnAddLine)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDeleteLine)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSettings)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnMoveDown, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnMoveUp, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(btnAddValue)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRemoveValue, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnEditResult))
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(27, 27, 27))
        );

        jTabbedPane1.addTab("Values and Results", jPanel3);

        jLabel3.setText("To have the program skip a step create a branch with no table or input lines.");

        jLabel5.setText("Anything in the results section will still be executed though.");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jTabbedPane1)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(jLabel3))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 422, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addContainerGap(43, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnEditConActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditConActionPerformed
        ArrayList<String> variables = parent.getvariables();
        JDialog dialog = new JDialog();
        ConditionEditor newpanel;
        if (condition != null) {
            newpanel = new ConditionEditor(dialog, variables, condition);
        } else {
            newpanel = new ConditionEditor(dialog, variables);
        }

        dialog.add(newpanel);
        dialog.setModal(true);
        dialog.setTitle("Set Condition for " + getName());
        dialog.pack();
        dialog.setVisible(true);
        Element Condition = newpanel.getcondition();
        if (Condition != null) {
            condition = Condition;
            loadcondition();
        }
    }//GEN-LAST:event_btnEditConActionPerformed

    private void btnDeleteLineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteLineActionPerformed
        int index = this.tblForm.getSelectedRow();
        if (index >= 0) {
            index++;
            lines.remove(index);
            loadfunctions();
        }
    }//GEN-LAST:event_btnDeleteLineActionPerformed

    private void btnAddValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddValueActionPerformed
        JDialog dialog = new JDialog();
        int linenumber = 1;

        if (results.size() > 0) {
            linenumber = results.lastKey() + 1;
        }
        TemplateResultPopup newpopup = new TemplateResultPopup(dialog, parent.getvariables(), linenumber);

        dialog.add(newpopup);
        dialog.setModal(true);
        dialog.setSize(400, 400);
        dialog.setVisible(true);
        Element Result = newpopup.getresult();
        if (Result != null) {
            results.put(linenumber, Result);
        }
        this.loadresults();
    }//GEN-LAST:event_btnAddValueActionPerformed

    private void btnAddLineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddLineActionPerformed
        int index = 1;
        if (lines.size() > 0) {
            index = lines.lastKey() + 1;
        }
        JDialog dialog = new JDialog();
        TemplateFormPopup popup = new TemplateFormPopup(dialog, parent.getvariables(), index);
        dialog.add(popup);
        dialog.pack();
        dialog.setModal(true);
        dialog.setVisible(true);
        Element newline = popup.getLine();
        if (newline != null) {
            lines.put(index, newline);
        }
        loadfunctions();
        dialog.dispose();
    }//GEN-LAST:event_btnAddLineActionPerformed

    private void btnMoveDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoveDownActionPerformed
        int index = tblForm.getSelectedRow();
        if (index >= 0 && index < formmodel.getRowCount() - 1) {
            index++;//table uses 0 based but map uses 1 based
            Element top = lines.get(index);
            Element bottom = lines.get(index + 1);
            lines.put(index, bottom);
            lines.put(index + 1, top);
            loadfunctions();
        }
    }//GEN-LAST:event_btnMoveDownActionPerformed

    private void btnMoveUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoveUpActionPerformed
        int index = tblForm.getSelectedRow();
        if (index > 0) {
            index++;//table uses 0 based but map uses 1 based
            Element top = lines.get(index - 1);
            Element bottom = lines.get(index);
            lines.put(index - 1, bottom);
            lines.put(index, top);
            loadfunctions();
        }
    }//GEN-LAST:event_btnMoveUpActionPerformed

    private void btnRemoveValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveValueActionPerformed
        int index = tblResults.getSelectedRow();
        if (index >= 0) {
            results.remove(index);
        }
        this.loadresults();
    }//GEN-LAST:event_btnRemoveValueActionPerformed

    private void cboTableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboTableActionPerformed
        if (!loading) {
            loadtable();//being called when branch is created multiple times
        }
    }//GEN-LAST:event_cboTableActionPerformed

    private void btnEditResultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditResultActionPerformed
        JDialog dialog = new JDialog();
        int linenumber = tblResults.getSelectedRow();
        if (linenumber > -1) {
            Element current = results.get(linenumber);
            if (current != null) {
                TemplateResultPopup newpopup = new TemplateResultPopup(dialog, parent.getvariables(), current);
                dialog.add(newpopup);
                dialog.setModal(true);
                dialog.setSize(400, 400);
                dialog.setVisible(true);
                Element Result = newpopup.getresult();
                if (Result != null) {
                    results.put(linenumber, Result);
                }
                this.loadresults();
            }
        }
    }//GEN-LAST:event_btnEditResultActionPerformed

    private void btnSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSettingsActionPerformed
        int index = tblForm.getSelectedRow();
        if (index != -1) {
            Element line = this.lines.get(index + 1);
            JDialog dialog = new JDialog();
            TemplateFormPopup popup = new TemplateFormPopup(dialog, parent.getvariables(), line);
            dialog.add(popup);
            dialog.pack();
            dialog.setModal(true);
            dialog.setVisible(true);
            Element newline = popup.getLine();
            if (newline != null) {
                lines.put(index + 1, newline);
            }
            loadfunctions();
            dialog.dispose();
        }
    }//GEN-LAST:event_btnSettingsActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel TableListPanel;
    private javax.swing.JButton btnAddLine;
    private javax.swing.JButton btnAddValue;
    private javax.swing.JButton btnDeleteLine;
    private javax.swing.JButton btnEditCon;
    private javax.swing.JButton btnEditResult;
    private javax.swing.JButton btnMoveDown;
    private javax.swing.JButton btnMoveUp;
    private javax.swing.JButton btnRemoveValue;
    private javax.swing.JButton btnSettings;
    private javax.swing.JComboBox cboTable;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel lblCondition;
    private javax.swing.JTable tblForm;
    private javax.swing.JTable tblResults;
    // End of variables declaration//GEN-END:variables
}
