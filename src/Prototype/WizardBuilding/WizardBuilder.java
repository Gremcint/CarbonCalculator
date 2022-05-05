/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Prototype.WizardBuilding;

import Prototype.Popups.NameForm;
import Prototype.StaticClasses.Global;
import Prototype.StaticClasses.XMLHandling;
import java.util.HashMap;
import java.util.ArrayList;
import org.jdom2.Element;
import javax.swing.DefaultListModel;
import java.util.TreeMap;
import java.util.Map;
import Prototype.Popups.TextForm;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 *
 * @author gremcint
 */
public class WizardBuilder extends javax.swing.JPanel implements Prototype.Main.SaveInterface{

    private TreeMap<Integer, TemplateStep> Steps;
    private TreeMap<String, String> Variables;
    private String Schema;
    private Element currentwizard;
    private Integer currentstep;
    private String SavedName;
    private String mode;
    private String currentname;
    private DefaultListModel WizardModel;
    private DefaultListModel StepModel;
    private DefaultListModel ValueModel;

    /**
     * Creates new form WizardBuilder
     */
    public WizardBuilder() {
        initComponents();
        mode = Global.getMode();
    }

    public void Startup() {
        Schema = Global.getcurrentschema();
        pnlSteps.setEnabled(false);
        pnlValues.setEnabled(false);
        pnlBranches.setEnabled(false);
        btnRenameWizard.setEnabled(false);
        btnChangeWizard.setEnabled(false);
        btnDeleteWizard.setEnabled(false);
                Clear();

    }

    public void loadwizardlist() {
        WizardModel.clear();
        String[] Path = {mode + "S", Schema, "WIZARDS"};
        Element wizards = XMLHandling.getpath(Path, Global.getxmlfilename());
        if (wizards != null) {
            for (Element wizard : wizards.getChildren()) {
                String name = wizard.getName();
                WizardModel.addElement(name);
            }
        }
        lstWizards.setModel(WizardModel);
    }

    public void loadwizard(String wizardname) {
        loadwizardlist();
        Schema = Global.getcurrentschema();
        String[] Path = {Global.getMode()+"S", Schema, "WIZARDS", wizardname};

        currentwizard = XMLHandling.getpath(Path, Global.getxmlfilename());

        if (currentwizard == null) {
            currentwizard = new Element(wizardname);
        }
        Element steps = currentwizard.getChild("STEPS");
        TreeMap<Integer, Element> tempsteps = new TreeMap();
        if (steps != null) {
            for (Element step : steps.getChildren()) {
                String name = step.getName();

                if (name.startsWith("STEP") && Global.isInteger(name.substring(4))) {
                    Integer number = Integer.parseInt(name.substring(4));
                    if (!tempsteps.containsKey(number) && number > 0) {
                        tempsteps.put(number, step);
                    }
                }
            }
        }
        Steps = new TreeMap();

        for (Integer key : tempsteps.keySet()) {
            Element step = tempsteps.get(key);
            TemplateStep newstep = new TemplateStep(key, step, this);
            Steps.put(key, newstep);
        }
        LoadStepList();

        Variables = new TreeMap();
        Element variablelist = currentwizard.getChild("VARIABLES");
        if (variablelist != null) {
            for (Element variable : variablelist.getChildren()) {
                String name = variable.getName();
                String command = variable.getAttributeValue("value");
                HashMap<String, String> commandmap = Global.makecommandmap(command);
                String value = commandmap.get("DEFAULT");
                Variables.put(name, value);
            }
        }
        LoadVariableList();
        currentname = wizardname;
        lblWizard.setText(wizardname);
        pnlSteps.setEnabled(true);
        pnlValues.setEnabled(true);
        pnlBranches.setEnabled(true);
        if (!StepModel.isEmpty()) {
            loadstep(Steps.firstEntry().getValue());
        } else {
            newstep();
        }

        tabControls.setSelectedIndex(1);
    }

    private void Clear() {
        if (Steps != null) {
            Steps.clear();
        }
        if (Variables != null) {
            Variables.clear();
        }
        currentwizard = null;
        currentstep = null;
        SavedName = null;
        currentname = null;
        StepModel = new DefaultListModel();
        ValueModel = new DefaultListModel();
        WizardModel = new DefaultListModel();
        lstWizards.setModel(WizardModel);
        lstSteps.setModel(StepModel);
        lstValues.setModel(ValueModel);
        lstWizards.repaint();
        lstSteps.repaint();
        lstValues.repaint();
        loadwizardlist();
        BranchTabs.removeAll();
        btnDeleteWizard.setEnabled(false);
        btnRenameWizard.setEnabled(false);
        lblCurrentStep.setText("None");
        lblWizard.setText("None");
    }

    private void newstep() {

        Integer number = 1;
        if (Steps.size() > 0) {
            number = Steps.lastKey() + 1;
        }
        TextForm form = new TextForm("Step Title");
        form.addlabel("Please Enter a title for this step.");
        form.addfield("Step Name", "");
        ArrayList<ArrayList<String>> results = form.show();
        if (results != null) {
            String name = results.get(0).get(2);
            if (name != null) {
                TemplateStep step = new TemplateStep(number, name, this);
                Steps.put(number, step);
            }
        }
        LoadStepList();
        loadstep(Steps.lastEntry().getValue());
    }

    private void loadstep(TemplateStep step) {
        TreeMap<Integer, TemplateBranch> branches = step.getBranches();
        this.cmbDefaultBranch.removeAllItems();
        currentstep = step.getnumber();
        if (branches != null) {
            BranchTabs.removeAll();
            for (Map.Entry<Integer, TemplateBranch> entry : branches.entrySet()) {
                TemplateBranch branch = entry.getValue();
                String branchname = branch.getName();
                cmbDefaultBranch.addItem(branchname);
                String defaultbranch = step.getdefaultbranch();
                cmbDefaultBranch.setSelectedItem(defaultbranch);
                this.BranchTabs.add(branch);
            }
            BranchTabs.setSelectedComponent(branches.firstEntry().getValue());
            cmbDefaultBranch.repaint();
            cmbDefaultBranch.revalidate();
            lblCurrentStep.setText(step.getname());
        }
    }

    private void LoadVariableList() {
        DefaultListModel listmodel = new DefaultListModel();
        for (String current : Variables.keySet()) {
            listmodel.addElement(current);
        }
        this.lstValues.setModel(listmodel);
        lstValues.repaint();
    }

    private void LoadStepList() {
        StepModel = new DefaultListModel();
        for (Integer key : Steps.keySet()) {
            StepModel.addElement(Steps.get(key));
        }
        lstSteps.setModel(StepModel);
        lstSteps.repaint();
    }

    private void LoadStepList(TemplateStep selected) {
        DefaultListModel stepmodel = new DefaultListModel();
        for (Integer key : Steps.keySet()) {
            stepmodel.addElement(Steps.get(key));
        }
        lstSteps.setModel(stepmodel);
        lstSteps.setSelectedValue(selected, true);
        lstSteps.repaint();
    }

    public ArrayList<String> getvariables() {
        return new ArrayList(Variables.keySet());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel3 = new javax.swing.JLabel();
        btnSaveAll = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        lblCurrentStep = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        lblWizard = new javax.swing.JLabel();
        tabControls = new javax.swing.JTabbedPane();
        pnlWizards = new javax.swing.JPanel();
        btnChangeWizard = new javax.swing.JButton();
        btnNewWizard = new javax.swing.JButton();
        btnDeleteWizard = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        lstWizards = new javax.swing.JList();
        btnRenameWizard = new javax.swing.JButton();
        pnlSteps = new javax.swing.JPanel();
        btnGoTo = new javax.swing.JButton();
        btnNewStep = new javax.swing.JButton();
        btnDuplicateStep = new javax.swing.JButton();
        btnMoveUp = new javax.swing.JButton();
        btnMoveDown = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();
        btnRenameStep = new javax.swing.JButton();
        btnNext = new javax.swing.JButton();
        btnPrev = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstSteps = new javax.swing.JList();
        pnlValues = new javax.swing.JPanel();
        btnAddValue = new javax.swing.JButton();
        btnDeleteValue = new javax.swing.JButton();
        btnCheck = new javax.swing.JButton();
        btnRenameValue = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        lstValues = new javax.swing.JList();
        pnlBranches = new javax.swing.JPanel();
        btnAddBranch = new javax.swing.JButton();
        btnDuplicateBranch = new javax.swing.JButton();
        btnDeleteBranch = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        cmbDefaultBranch = new javax.swing.JComboBox();
        BranchTabs = new javax.swing.JTabbedPane();

        jLabel3.setText("jLabel3");

        setPreferredSize(new java.awt.Dimension(995, 694));

        btnSaveAll.setText("Save Changes");
        btnSaveAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveAllActionPerformed(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        lblCurrentStep.setText("  ");
        lblCurrentStep.setToolTipText("");

        jLabel2.setText("Current Wizard:");

        jLabel4.setText("Current Step:");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblCurrentStep, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblWizard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addComponent(lblWizard, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(lblCurrentStep))
                .addContainerGap())
        );

        pnlWizards.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        btnChangeWizard.setText("Load Wizard");
        btnChangeWizard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChangeWizardActionPerformed(evt);
            }
        });

        btnNewWizard.setText("New Wizard");
        btnNewWizard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewWizardActionPerformed(evt);
            }
        });

        btnDeleteWizard.setText("Delete Wizard");
        btnDeleteWizard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteWizardActionPerformed(evt);
            }
        });

        lstWizards.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstWizardsValueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(lstWizards);

        btnRenameWizard.setText("Rename Wizard");
        btnRenameWizard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRenameWizardActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlWizardsLayout = new javax.swing.GroupLayout(pnlWizards);
        pnlWizards.setLayout(pnlWizardsLayout);
        pnlWizardsLayout.setHorizontalGroup(
            pnlWizardsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlWizardsLayout.createSequentialGroup()
                .addGroup(pnlWizardsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnChangeWizard, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnNewWizard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnDeleteWizard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnRenameWizard, javax.swing.GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        pnlWizardsLayout.setVerticalGroup(
            pnlWizardsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlWizardsLayout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addComponent(btnChangeWizard)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnNewWizard)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDeleteWizard)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRenameWizard))
            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 360, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        tabControls.addTab("Wizards", pnlWizards);

        pnlSteps.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        btnGoTo.setText("Go To Step");
        btnGoTo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGoToActionPerformed(evt);
            }
        });

        btnNewStep.setText("New Step");
        btnNewStep.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewStepActionPerformed(evt);
            }
        });

        btnDuplicateStep.setText("Duplicate Step");
        btnDuplicateStep.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDuplicateStepActionPerformed(evt);
            }
        });

        btnMoveUp.setText("Move Up");
        btnMoveUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoveUpActionPerformed(evt);
            }
        });

        btnMoveDown.setText("Move Down");
        btnMoveDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoveDownActionPerformed(evt);
            }
        });

        btnRemove.setText("Remove Step");
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveActionPerformed(evt);
            }
        });

        btnRenameStep.setText("Rename Step");
        btnRenameStep.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRenameStepActionPerformed(evt);
            }
        });

        btnNext.setText("Next Step");
        btnNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNextActionPerformed(evt);
            }
        });

        btnPrev.setText("Previous Step");
        btnPrev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrevActionPerformed(evt);
            }
        });

        jScrollPane1.setViewportView(lstSteps);

        javax.swing.GroupLayout pnlStepsLayout = new javax.swing.GroupLayout(pnlSteps);
        pnlSteps.setLayout(pnlStepsLayout);
        pnlStepsLayout.setHorizontalGroup(
            pnlStepsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStepsLayout.createSequentialGroup()
                .addGroup(pnlStepsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnGoTo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnNewStep, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnDuplicateStep, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnMoveUp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnMoveDown, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnRemove, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnNext, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnRenameStep, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnPrev, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE))
        );
        pnlStepsLayout.setVerticalGroup(
            pnlStepsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStepsLayout.createSequentialGroup()
                .addComponent(btnGoTo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnNewStep)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDuplicateStep)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnMoveUp)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnMoveDown)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRemove)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRenameStep)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnNext)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnPrev)
                .addGap(0, 78, Short.MAX_VALUE))
            .addComponent(jScrollPane1)
        );

        tabControls.addTab("Steps", pnlSteps);

        pnlValues.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        btnAddValue.setText("Add Value");
        btnAddValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddValueActionPerformed(evt);
            }
        });

        btnDeleteValue.setText("Delete Value");
        btnDeleteValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteValueActionPerformed(evt);
            }
        });

        btnCheck.setText("Check Usage");
        btnCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCheckActionPerformed(evt);
            }
        });

        btnRenameValue.setText("Edit Value");
        btnRenameValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRenameValueActionPerformed(evt);
            }
        });

        jScrollPane2.setViewportView(lstValues);

        javax.swing.GroupLayout pnlValuesLayout = new javax.swing.GroupLayout(pnlValues);
        pnlValues.setLayout(pnlValuesLayout);
        pnlValuesLayout.setHorizontalGroup(
            pnlValuesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlValuesLayout.createSequentialGroup()
                .addGroup(pnlValuesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnAddValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnRenameValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnCheck, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnDeleteValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE))
        );
        pnlValuesLayout.setVerticalGroup(
            pnlValuesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlValuesLayout.createSequentialGroup()
                .addComponent(btnAddValue)
                .addGap(7, 7, 7)
                .addComponent(btnDeleteValue)
                .addGap(7, 7, 7)
                .addComponent(btnCheck)
                .addGap(7, 7, 7)
                .addComponent(btnRenameValue)
                .addGap(0, 235, Short.MAX_VALUE))
            .addComponent(jScrollPane2)
        );

        tabControls.addTab("Values", pnlValues);

        pnlBranches.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        btnAddBranch.setText("Add Branch");
        btnAddBranch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddBranchActionPerformed(evt);
            }
        });

        btnDuplicateBranch.setText("Duplicate Branch");
        btnDuplicateBranch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDuplicateBranchActionPerformed(evt);
            }
        });

        btnDeleteBranch.setText("Delete Branch");
        btnDeleteBranch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteBranchActionPerformed(evt);
            }
        });

        jLabel1.setText("Default Branch:");

        cmbDefaultBranch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbDefaultBranchActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlBranchesLayout = new javax.swing.GroupLayout(pnlBranches);
        pnlBranches.setLayout(pnlBranchesLayout);
        pnlBranchesLayout.setHorizontalGroup(
            pnlBranchesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBranchesLayout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbDefaultBranch, 0, 144, Short.MAX_VALUE))
            .addGroup(pnlBranchesLayout.createSequentialGroup()
                .addGroup(pnlBranchesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(btnAddBranch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnDeleteBranch, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnDuplicateBranch, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        pnlBranchesLayout.setVerticalGroup(
            pnlBranchesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBranchesLayout.createSequentialGroup()
                .addComponent(btnAddBranch)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDuplicateBranch)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDeleteBranch)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlBranchesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cmbDefaultBranch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(239, Short.MAX_VALUE))
        );

        tabControls.addTab("Branches", pnlBranches);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(tabControls)
                    .addComponent(btnSaveAll)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(BranchTabs, javax.swing.GroupLayout.PREFERRED_SIZE, 687, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tabControls, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSaveAll))
                    .addComponent(BranchTabs, javax.swing.GroupLayout.PREFERRED_SIZE, 518, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(176, Short.MAX_VALUE))
        );

        tabControls.getAccessibleContext().setAccessibleName("Wizards");
    }// </editor-fold>//GEN-END:initComponents

    private void btnNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextActionPerformed
        Integer nextstep = Steps.ceilingKey(currentstep + 1);
        if (nextstep != null) {
            loadstep(Steps.get(nextstep));
        }
    }//GEN-LAST:event_btnNextActionPerformed

    private void btnPrevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrevActionPerformed
        Integer prevstep = Steps.floorKey(currentstep - 1);
        if (prevstep != null) {
            loadstep(Steps.get(prevstep));
        }
    }//GEN-LAST:event_btnPrevActionPerformed

    private void btnNewStepActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewStepActionPerformed
        newstep();
    }//GEN-LAST:event_btnNewStepActionPerformed

    private void btnGoToActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGoToActionPerformed
        int index = lstSteps.getSelectedIndex();
        if (index > -1) {
            TemplateStep step = (TemplateStep) lstSteps.getSelectedValue();
            loadstep(step);
        }
    }//GEN-LAST:event_btnGoToActionPerformed

    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed
        int index = lstSteps.getSelectedIndex();
        if (index > -1) {
            TemplateStep step = (TemplateStep) lstSteps.getSelectedValue();
            Integer Number = step.getnumber();
            Steps.remove(Number);
            LoadStepList();
            Integer newNumber = Steps.floorKey(Number);
            if (newNumber != null) {
                loadstep(Steps.get(newNumber));
            } else if (Steps.size() > 0) {
                loadstep(Steps.firstEntry().getValue());
            } else {
                BranchTabs.removeAll();
                newstep();
            }
        }
    }//GEN-LAST:event_btnRemoveActionPerformed

    private void btnAddValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddValueActionPerformed
        TextForm form = new TextForm("Variable Information");
        form.addfield("Variable Name:", "");
        form.addfield("Starting Value:", "");
        String varname;
        String defaultvalue;
        boolean loop = true;
        while (loop) {
            ArrayList<ArrayList<String>> results = form.show();
            if (results != null) {
                varname = results.get(0).get(2);
                if (varname != null && !varname.isEmpty()) {
                    defaultvalue = results.get(1).get(2);
                } else {
                    break;
                }
            } else {
                break;
            }
            if (Variables.containsKey(varname)) {
                form = new TextForm("Variable Information");
                form.addfield("Variable Name:", "");
                form.addfield("Starting Value:", defaultvalue);
                form.addlabel(varname + " is already taken please enter a unique name");
                loop = true;
            } else {
                Variables.put(varname, defaultvalue);
                LoadVariableList();

                loop = false;
            }
        }
    }//GEN-LAST:event_btnAddValueActionPerformed

    private void btnRenameStepActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRenameStepActionPerformed
        int index = lstSteps.getSelectedIndex();
        if (index > -1) {
            TextForm form = new TextForm("Please enter a new title for this step");
            form.addfield("Step Name", "");
            ArrayList<ArrayList<String>> results = form.show();
            if (results != null) {
                String name = results.get(0).get(2);
                if (name != null) {
                    TemplateStep step = (TemplateStep) lstSteps.getSelectedValue();
                    step.setname(name);
                    LoadStepList();
                }
            }
        }
    }//GEN-LAST:event_btnRenameStepActionPerformed

    private void btnDuplicateStepActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDuplicateStepActionPerformed
        int index = lstSteps.getSelectedIndex();
        if (index > -1) {
            TemplateStep step = (TemplateStep) lstSteps.getSelectedValue();
            Integer newstepnumber = Steps.lastKey() + 1;
            TemplateStep newstep = step.duplicate(newstepnumber);
            Steps.put(newstepnumber, newstep);
            LoadStepList();
            loadstep(newstep);
        }
    }//GEN-LAST:event_btnDuplicateStepActionPerformed

    private void btnMoveUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoveUpActionPerformed
        int index = lstSteps.getSelectedIndex();
        if (index > 0) {
            TemplateStep bottomstep = (TemplateStep) lstSteps.getSelectedValue();
            Integer bottom = bottomstep.getnumber();
            Integer top = Steps.floorKey(bottom - 1);
            TemplateStep topstep = Steps.get(top);
            bottomstep.setNumber(top);
            topstep.setNumber(bottom);
            Steps.put(top, bottomstep);
            Steps.put(bottom, topstep);
            LoadStepList(bottomstep);
        }
    }//GEN-LAST:event_btnMoveUpActionPerformed

    private void btnMoveDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoveDownActionPerformed
        int index = lstSteps.getSelectedIndex();
        if (index > -1 && Steps.size() > 1 && index < Steps.size() - 1) {
            TemplateStep topstep = (TemplateStep) lstSteps.getSelectedValue();
            Integer top = topstep.getnumber();
            Integer bottom = Steps.ceilingKey(top + 1);
            TemplateStep bottomstep = Steps.get(bottom);
            bottomstep.setNumber(top);
            topstep.setNumber(bottom);
            Steps.put(top, bottomstep);
            Steps.put(bottom, topstep);
            LoadStepList(topstep);
        }
    }//GEN-LAST:event_btnMoveDownActionPerformed

    private void btnDeleteValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteValueActionPerformed
        if (lstValues.getSelectedIndex() > -1) {
            ArrayList<String> usages = new ArrayList();
            String name = lstValues.getSelectedValue().toString();
            for (Map.Entry<Integer, TemplateStep> step : Steps.entrySet()) {
                TemplateStep current = step.getValue();
                usages = current.CheckUsage(name);
            }
            if (usages.size() > 0) {

                TextForm form = new TextForm("Usage Summary");
                form.addlist("Usages found:", usages);
                form.addlabel("Press OK to confirm deletion.");
                if (form.show() != null) {
                    for (Map.Entry<Integer, TemplateStep> step : Steps.entrySet()) {
                        step.getValue().deletevariable(name);
                    }
                }
            } else {
                
                    Variables.remove(name);
                
            }
        }
        LoadVariableList();
        loadstep(Steps.get(currentstep));
    }//GEN-LAST:event_btnDeleteValueActionPerformed

    private void btnSaveAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveAllActionPerformed
        Element StepsXML = new Element("STEPS");
        for (Map.Entry<Integer, TemplateStep> step : Steps.entrySet()) {
            TemplateStep current = step.getValue();
            StepsXML.addContent(current.save());
        }
        Element var = new Element("VARIABLES");
        for (String currentvar : Variables.keySet()) {
            Element varchild = new Element(currentvar);
            String value = Variables.get(currentvar);
            if (value == null) {
                value = "";
            }
            varchild.setAttribute("value", "DEFAULT:" + value);
            var.addContent(varchild);
        }
        if (SavedName == null) {
            SavedName = this.currentname;
        }
        String[] Path = {mode + "S", Schema, "WIZARDS", SavedName};
        if (!SavedName.equals(currentname)) {
            XMLHandling.changeElementName(Path, currentname, Global.getxmlfilename());
            Path[3] = currentname;
        }
        XMLHandling.addpath(Path, StepsXML, Global.getxmlfilename(), true);
        XMLHandling.addpath(Path, var, Global.getxmlfilename(), true);
        JOptionPane.showMessageDialog(null, "Changes Saved");
    }//GEN-LAST:event_btnSaveAllActionPerformed

    private void btnRenameValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRenameValueActionPerformed

        TextForm form = new TextForm("Variable Information");
        String varname = lstValues.getSelectedValue().toString();
        String defaultvalue = Variables.get(varname);

        form.addfield("Variable Name:", varname);
        form.addfield("Starting Value:", defaultvalue);
        String oldname = varname;
        boolean loop = true;
        while (loop) {
            ArrayList<ArrayList<String>> results = form.show();
            if (results != null) {
                varname = results.get(0).get(2);
                if (varname != null && !varname.isEmpty()) {
                    defaultvalue = results.get(1).get(2);
                } else {
                    break;
                }
            } else {
                break;
            }
            if (Variables.containsKey(varname) && !varname.equals(oldname)) {
                form = new TextForm("Variable Information");
                form.addfield("Variable Name:", oldname);
                form.addfield("Starting Value:", defaultvalue);
                form.addlabel(varname + " is already taken please enter a unique name");
                loop = true;
            } else {
                Variables.remove(oldname);
                Variables.put(varname, defaultvalue);
                LoadVariableList();
                loop = false;
            }
        }
    }//GEN-LAST:event_btnRenameValueActionPerformed

    private void btnAddBranchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddBranchActionPerformed
        TemplateStep step = Steps.get(currentstep);
        if (step != null) {
            step.AddBranch();
            this.loadstep(step);
        }
    }//GEN-LAST:event_btnAddBranchActionPerformed

    private void btnDeleteBranchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteBranchActionPerformed
        TemplateStep step = Steps.get(currentstep);
        if (step.getbranchcount() > 1) {
            if (BranchTabs.getSelectedComponent() instanceof TemplateBranch) {
                TemplateBranch branch = (TemplateBranch) BranchTabs.getSelectedComponent();
                Integer index = branch.getbranchnumber();
                step.RemoveBranch(index);
                loadstep(step);
            }
        }
    }//GEN-LAST:event_btnDeleteBranchActionPerformed

    private void btnDuplicateBranchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDuplicateBranchActionPerformed
        TemplateStep step = Steps.get(currentstep);
        if (BranchTabs.getSelectedComponent() instanceof TemplateBranch) {
            TemplateBranch branch = (TemplateBranch) BranchTabs.getSelectedComponent();
            Integer index = branch.getbranchnumber();
            TemplateBranch newbranch = step.DuplicateBranch(index);
            loadstep(step);
            BranchTabs.setSelectedComponent(newbranch);
        }
    }//GEN-LAST:event_btnDuplicateBranchActionPerformed

    private void cmbDefaultBranchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbDefaultBranchActionPerformed
        if (cmbDefaultBranch.getSelectedItem() != null) {
            String value = cmbDefaultBranch.getSelectedItem().toString();
            TemplateStep step = Steps.get(currentstep);
            step.setdefaultbranch(value);
        }
    }//GEN-LAST:event_cmbDefaultBranchActionPerformed

    private void btnCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCheckActionPerformed
        if (lstValues.getSelectedIndex() > -1) {
            ArrayList<String> usages = new ArrayList();
            String name = lstValues.getSelectedValue().toString();
            for (Map.Entry<Integer, TemplateStep> step : Steps.entrySet()) {
                TemplateStep current = step.getValue();
                usages = current.CheckUsage(name);
            }
            if (usages.size() > 0) {
                TextForm form = new TextForm("Usage Summary");
                form.addlist("Usages found:", usages);
                form.show();
            } else {
                TextForm form = new TextForm("Usage Summary");
                form.addlabel("No usage of variable found.");
                form.show();
            }
        }

    }//GEN-LAST:event_btnCheckActionPerformed

    private void btnNewWizardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewWizardActionPerformed
        DefaultListModel model = (DefaultListModel) lstWizards.getModel();
        ArrayList<String> names = new ArrayList();
        for (int curname = 0; curname < model.getSize(); curname++) {
            names.add(model.get(curname).toString());
        }
        JDialog dialog = new JDialog();
        String message = "Please enter a name for the new wizard:";
        NameForm nameform = new NameForm(names, dialog, message, true);
        dialog.add(nameform);
        dialog.setSize(370, 150);
        dialog.setTitle("Wizard Name");
        dialog.setModal(true);
        dialog.setVisible(true);

        String result = nameform.getresult();
        if (result != null) {
            Clear();
            loadwizard(result);
            WizardModel.addElement(result);
            lstWizards.repaint();
        }
    }//GEN-LAST:event_btnNewWizardActionPerformed

    private void btnDeleteWizardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteWizardActionPerformed
        if (lstWizards.getSelectedIndex() != -1) {
            int choice = JOptionPane.showConfirmDialog(null, "Are you sure you wish to delete this wizard? This cannot be undone.", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                String wizardname = lstWizards.getSelectedValue().toString();

                if (wizardname.equals(currentname)) {
                    XMLHandling.deletepath(new String[]{mode + "S", Schema, "WIZARDS", this.SavedName}, Global.getxmlfilename());
                    Clear();
                } else {
                    XMLHandling.deletepath(new String[]{mode + "S", Schema, "WIZARDS", wizardname}, Global.getxmlfilename());
                    loadwizardlist();
                }
            }
        }
    }//GEN-LAST:event_btnDeleteWizardActionPerformed

    private void btnChangeWizardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChangeWizardActionPerformed
        if (lstWizards.getSelectedIndex() != -1) {
            String wizardname = lstWizards.getSelectedValue().toString();
            if (!wizardname.equals(SavedName)) {
                loadwizard(wizardname);
                SavedName = wizardname;
            }
        }
    }//GEN-LAST:event_btnChangeWizardActionPerformed

    private void btnRenameWizardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRenameWizardActionPerformed
        if (lstWizards.getSelectedIndex() != -1) {

            String wizardname = lstWizards.getSelectedValue().toString();

            ArrayList<String> names = new ArrayList();
            String message;
            for (int name = 0; name < WizardModel.getSize(); name++) {
                names.add(WizardModel.get(name).toString());
            }
            if (!wizardname.equals(currentname)) {
                if (SavedName != null && !SavedName.equals(currentname)) {
                    names.add(SavedName);
                }
                message = "Please enter a new name for the wizard, this will be saved automatically:";
            }else
            {
                message = "Please enter a new name for the wizard:";
            }
            names.remove(wizardname);

            JDialog dialog = new JDialog();

            NameForm nameform = new NameForm(names, dialog, message, true);
            dialog.add(nameform);
            dialog.setSize(370, 150);
            dialog.setTitle("Wizard Name");
            dialog.setModal(true);
            dialog.setVisible(true);
            String result = nameform.getresult();
            if (result != null) {
                
                WizardModel.setElementAt(result, lstWizards.getSelectedIndex());
                lstWizards.repaint();
                if(wizardname.equals(currentname))
                {
                    lblWizard.setText(result);
                    currentname=result;
                }else{
                XMLHandling.changeElementName(new String[]{mode + "S", Schema, "WIZARDS", wizardname}, result, Global.getxmlfilename());
            }}
        
    }
    }//GEN-LAST:event_btnRenameWizardActionPerformed

    private void lstWizardsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstWizardsValueChanged
        int index = lstWizards.getSelectedIndex();
        if (index != -1) {
            btnChangeWizard.setEnabled(true);
            btnDeleteWizard.setEnabled(true);
            btnRenameWizard.setEnabled(true);
        } else {
            btnChangeWizard.setEnabled(false);
            btnDeleteWizard.setEnabled(false);
            btnRenameWizard.setEnabled(false);
        }
    }//GEN-LAST:event_lstWizardsValueChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane BranchTabs;
    private javax.swing.JButton btnAddBranch;
    private javax.swing.JButton btnAddValue;
    private javax.swing.JButton btnChangeWizard;
    private javax.swing.JButton btnCheck;
    private javax.swing.JButton btnDeleteBranch;
    private javax.swing.JButton btnDeleteValue;
    private javax.swing.JButton btnDeleteWizard;
    private javax.swing.JButton btnDuplicateBranch;
    private javax.swing.JButton btnDuplicateStep;
    private javax.swing.JButton btnGoTo;
    private javax.swing.JButton btnMoveDown;
    private javax.swing.JButton btnMoveUp;
    private javax.swing.JButton btnNewStep;
    private javax.swing.JButton btnNewWizard;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnPrev;
    private javax.swing.JButton btnRemove;
    private javax.swing.JButton btnRenameStep;
    private javax.swing.JButton btnRenameValue;
    private javax.swing.JButton btnRenameWizard;
    private javax.swing.JButton btnSaveAll;
    private javax.swing.JComboBox cmbDefaultBranch;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblCurrentStep;
    private javax.swing.JLabel lblWizard;
    private javax.swing.JList lstSteps;
    private javax.swing.JList lstValues;
    private javax.swing.JList lstWizards;
    private javax.swing.JPanel pnlBranches;
    private javax.swing.JPanel pnlSteps;
    private javax.swing.JPanel pnlValues;
    private javax.swing.JPanel pnlWizards;
    private javax.swing.JTabbedPane tabControls;
    // End of variables declaration//GEN-END:variables
 @Override
    public boolean IsSaved()
    {
        return true;
    }
   @Override 
    public int SaveCheck()
    {
        return JOptionPane.NO_OPTION;
                
    }
}
