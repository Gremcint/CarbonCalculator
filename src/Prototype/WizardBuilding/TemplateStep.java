/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Prototype.WizardBuilding;

import Prototype.StaticClasses.Global;
import java.util.ArrayList;
import java.util.TreeMap;
import org.jdom2.Element;
import java.util.Map;
import java.util.HashMap;

/**
 *
 * @author Gregory
 */
public class TemplateStep {

    private Element source;
    private String name;
    private Integer number;
    private TreeMap<Integer, TemplateBranch> branches;
    private WizardBuilder parent;
    private String defaultbranch;

    public void setdefaultbranch(String branchname) {
        defaultbranch = branchname;
    }

// public TemplateStep(Integer Number, String name, WizardBuilder Parent) {
//        
//    }
    
    public TemplateStep(Integer Number, Element Source, WizardBuilder Parent) {
        number = Number;
        String stepstring = Source.getAttributeValue("value");
        HashMap<String, String> stepmap = Global.makecommandmap(stepstring);
        name = stepmap.get("NAME");
        defaultbranch = stepmap.get("DEFAULT");
        source = Source;

        branches = new TreeMap();
        parent = Parent;
        this.makebranches();

        if (defaultbranch == null || defaultbranch.isEmpty()) {
            defaultbranch = branches.firstEntry().getValue().getName();
        }
    }

    public ArrayList<String> CheckUsage(String Variable) {
        ArrayList<String> Usages = new ArrayList();
        if (branches == null || branches.isEmpty()) {
            this.makebranches();
        }
        for (Map.Entry<Integer, TemplateBranch> branchentry : branches.entrySet()) {
            TemplateBranch branch = branchentry.getValue();
            Usages.addAll(branch.checkusage(Variable, name, branchentry.getKey().toString()));
        }
        return Usages;
    }

    public void deletevariable(String Variable) {
        if (branches == null || branches.isEmpty()) {
            this.makebranches();
        }
        for (Map.Entry<Integer, TemplateBranch> branchentry : branches.entrySet()) {
            TemplateBranch branch = branchentry.getValue();
            branch.deletevariable(Variable);
        }
    }

    public void renamevariable(String oldname, String newname) {
        if (branches == null || branches.isEmpty()) {
            this.makebranches();
        }
        for (Map.Entry<Integer, TemplateBranch> branchentry : branches.entrySet()) {
            TemplateBranch branch = branchentry.getValue();
            branch.renamevariable(oldname, newname);
        }
    }

    public Element save() {
        if (branches == null || branches.isEmpty()) {
            source.setName("STEP" + number);
            return source;
        }
        Element newstep = new Element("STEP" + number);
        newstep.setAttribute("value", "DEFAULT:" + defaultbranch + ";NAME:" + name);
        Element branchlist = new Element("BRANCHES");
        newstep.addContent(branchlist);
        for (Map.Entry<Integer, TemplateBranch> branchentry : branches.entrySet()) {
            branchlist.addContent(branchentry.getValue().Save());
        }
        return newstep;
    }

    public TemplateStep(Integer Number, String Name, WizardBuilder Parent) {
        number = Number;
        name = Name;
        branches = new TreeMap();
        source = new Element("STEP" + number);
        source.setAttribute("value", "NAME:" + Name);
        parent = Parent;
        this.makebranches();
        defaultbranch = branches.firstEntry().getValue().getName();
    }

    public TemplateStep duplicate(Integer newNumber) {
        Element newsource = source.clone();
        newsource.setName("STEP" + newNumber);
        return new TemplateStep(newNumber, newsource, parent);
    }

    public Element getSourceXML() {
        return source;
    }

    public Integer getnumber() {
        return number;
    }

    public String getname() {
        return name;
    }

    public void setname(String Name) {
        name = Name;
    }

    private void makebranches() {
        Element BranchlistXML = source.getChild("BRANCHES");
        if (BranchlistXML != null) {
            TreeMap<Integer, Element> tempbranches = new TreeMap();
            for (Element branch : BranchlistXML.getChildren()) {
                String branchname = branch.getName();
                if (branchname.startsWith("BRANCH") && Global.isInteger(branchname.substring(6))) {
                    Integer branchnumber = Integer.parseInt(branchname.substring(6));
                    if (!tempbranches.containsKey(branchnumber) && branchnumber > 0) {
                        tempbranches.put(branchnumber, branch);
                    }
                }
            }
            branches = new TreeMap();
            for (Integer key : tempbranches.keySet()) {
                TemplateBranch newbranch = new TemplateBranch(tempbranches.get(key), parent, key);
                branches.put(key, newbranch);
            }
        }
        if (branches == null) {
            branches = new TreeMap();
        }
        if (branches.isEmpty()) {
            branches.put(1, new TemplateBranch(1, parent));
        }

    }

    public TreeMap<Integer, TemplateBranch> getBranches() {
        if (branches == null || branches.isEmpty()) {
            this.makebranches();
        }
        return branches;
    }

    public String getdefaultbranch() {
        if (branches == null || branches.isEmpty()) {
            this.makebranches();
        }
        if (defaultbranch == null || defaultbranch.isEmpty()) {
            defaultbranch = branches.firstEntry().getValue().getName();
        }
        return defaultbranch;
    }

    public void setNumber(Integer Number) {
        number = Number;
    }

    public void AddBranch(Integer Branch, TemplateBranch NewBranch) {
        branches.put(Branch, NewBranch);
    }

    public void AddBranch() {
        Integer branchnumber = branches.lastKey() + 1;
        TemplateBranch NewBranch = new TemplateBranch(branchnumber, parent);
        branches.put(branchnumber, NewBranch);
    }

    public TemplateBranch DuplicateBranch(Integer index) {
        Integer newindex = branches.lastKey() + 1;
        TemplateBranch NewBranch = branches.get(index).clone(newindex);
        branches.put(newindex, NewBranch);
        return NewBranch;
    }

    public void RemoveBranch(Integer Branch) {
        TemplateBranch oldbranch = branches.get(Branch);
        String branchname = oldbranch.getName();
        branches.remove(Branch);
        if (branchname.equals(defaultbranch)) {
            defaultbranch = branches.firstEntry().getValue().getName();
        }

    }

    public TemplateBranch GetBranch(Integer Branch) {
        return branches.get(Branch);
    }

    @Override
    public String toString() {
        String value = "Step " + number + ": " + name;
        return value;
    }

    public int getbranchcount() {
        return branches.size();
    }
}
