/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Prototype.WizardExecutor;

//<editor-fold defaultstate="collapsed" desc="Imports">
import Prototype.StaticClasses.Global;
import Prototype.StaticClasses.MathHandling;
import Prototype.StaticClasses.XMLHandling;
import java.util.HashMap;
import java.util.TreeMap;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;
import org.jdom2.Element;
//</editor-fold>

/**
 *
 * @author User
 */
public class WizardStep {

    private Element table;
    private TreeMap<String, JComponent> forminputs = new TreeMap();
    private Element source;
    private JPanel panel;
    private TreeMap<Integer, Element> branches;
    private Element currentbranch;
    private int number;
    private boolean skip = false;

    public WizardStep(Element Source) {
        source = Source;
        Element branchlist = source.getChild(("BRANCHES"));
        TreeMap<Integer, Element> tempbranches = new TreeMap();
        for (Element branch : branchlist.getChildren()) {
            String name = branch.getName();
            if (name.startsWith("BRANCH") && Global.isInteger(name.substring(6))) {
                Integer num = Integer.parseInt(name.substring(6));
                if (!tempbranches.containsKey(num) && num > 0) {
                    tempbranches.put(num, branch);
                }
            }
        }
        branches = new TreeMap();
        for (Integer key : tempbranches.keySet()) {
            Element newbranch = tempbranches.get(key);
            branches.put(key, newbranch);
        }
        String command = Source.getAttributeValue("value");
        HashMap<String, String> map = Global.makecommandmap(command);
        String defaultvalue = map.get("DEFAULT");
        Element defaultxml = branchlist.getChild(defaultvalue);
        branches.put(0, defaultxml);
    }

    public void loadbranch(Element Branch) {
        currentbranch = Branch;
        table = XMLHandling.getpath(new String[]{"FUNCTION", "TABLE"}, Branch);

        Element form = XMLHandling.getpath(new String[]{"FUNCTION", "FORM"}, Branch);
        if (form != null) {
            panel = new JPanel();
            MigLayout layout = new MigLayout();
            panel.setLayout(layout);
            TreeMap<Integer, Element> templines = new TreeMap();
            for (Element line : form.getChildren()) {
                String name = line.getName();
                if (name.startsWith("LINE") && Global.isInteger(name.substring(4))) {
                    Integer linenumber = Integer.parseInt(name.substring(4));
                    if (!templines.containsKey(linenumber) && linenumber > 0) {
                        templines.put(linenumber, line);
                    }
                }
            }
            if (templines.isEmpty()) {
                skip = true;
            } else {
                for (Integer key : templines.keySet()) {
                    Element currentline = templines.get(key);
                    HashMap<String, String> linemap = Global.makecommandmap(currentline.getAttributeValue("value"));
                    String caption = linemap.get("CAPTION");
                    String type = linemap.get("TYPE");
                    String defvalue = linemap.get("DEFAULT");
                    String store = linemap.get("STORE");
                    if (caption != null && !caption.isEmpty()) {
                        JLabel label = new JLabel();
                        label.setText(caption);
                        panel.add(label);
                    }
                    switch (type) {
                        case "PARAGRAPH":
                            String text = currentline.getChildText("TEXT");
                            if (text != null && !text.isEmpty()) {
                                JLabel paragraph = new JLabel();
                                paragraph.setText(text);
                                panel.add(paragraph, "wrap");
                            }
                            break;
                        case "TEXTINPUT":
                            JTextField textfield = new JTextField();
                            if (defvalue != null && !defvalue.isEmpty()) {
                                textfield.setText(defvalue);
                            }
                            panel.add(textfield, "wrap");
                            if (store != null && !store.isEmpty()) {
                                forminputs.put(store, textfield);
                            }
                            break;
                        case "LIST":
                            TreeMap<Integer, String> listitems = new TreeMap();
                            for (Element listitem : currentline.getChildren()) {
                                String name = listitem.getName();
                                if (name.startsWith("ITEM") && Global.isInteger(name.substring(4))) {
                                    Integer itemnumber = Integer.parseInt(name.substring(4));
                                    if (!listitems.containsKey(itemnumber) && itemnumber >= 0) {
                                        listitems.put(itemnumber, listitem.getText());
                                    }
                                }
                            }
                            String[] items = listitems.values().toArray(new String[listitems.keySet().size()]);
                            JComboBox box = new JComboBox(items);
                            box.insertItemAt("", 0);
                            box.setSelectedIndex(0);
                            if (defvalue != null && !defvalue.isEmpty()) {
                                box.setSelectedItem(defvalue);
                            }
                            if (store != null && !store.isEmpty()) {
                                forminputs.put(store, box);
                            }
                            panel.add(box, "wrap");
                            break;
                    }
                }
            }
        }
    }

    public void choosebranch(HashMap<String, Prototype.DataManaging.EQValue> Variables) {
        Integer branchchoice = 0;
        for (Integer index : branches.keySet()) {
            if (index > 0) {
                Element condition = branches.get(index).getChild("CONDITION");
                if (condition != null) {
                    String equation = condition.getAttributeValue("value");
                    if (equation != null && !equation.isEmpty()) {
                        HashMap<String, String> classes = new HashMap();
                        for (String key : Variables.keySet()) {
                            classes.put(key, "String");
                        }
                        java.util.ArrayList<String> terms = Global.parseformula(equation);
                        Element TERMS = condition.getChild("TERMS");
                        for (String currentterm : terms) {
                            if (TERMS != null) {
                                Element termxml = TERMS.getChild(currentterm.substring(4));
                                if (currentterm.startsWith("TXT:") && termxml != null) {
                                    Variables.put(currentterm.substring(4), new Prototype.DataManaging.EQValue(termxml.getText(), "STRING"));
                                } else if (currentterm.startsWith("DATE:") && termxml != null) {
                                    Variables.put(currentterm.substring(5), new Prototype.DataManaging.EQValue(termxml.getText(), "DATE"));
                                }
                            }
                        }
                        if (MathHandling.Solve(equation, Variables, null).GetValue().equals("true")) {
                            branchchoice = index;
                            break;
                        }
                    }
                }
            }
        }
        Element branch = branches.get(branchchoice);
        loadbranch(branch);
    }

    public String gettablename() {
        if (table != null) {
            String commands = table.getAttributeValue("value");
            if (commands != null && !commands.isEmpty()) {
                HashMap<String, String> map = Global.makecommandmap(commands);
                String name = map.get("TABLE");
                if (name != null && !name.isEmpty()) {
                    return name;
                }
            }
        }
        return null;

    }

    public Element gettableequations() {
        if (table != null) {
            return table.getChild("Equations");
        }
        return null;
    }

    public Element getresults() {
        if (currentbranch != null) {
            Element results = currentbranch.getChild("RESULTS");
            return results;
        }
        return null;

    }

    public TreeMap<String, JComponent> getinputs() {
        return forminputs;
    }

    public boolean CheckSkip() {
        return skip;
    }

    public JPanel getpanel() {
        return panel;
    }

    public Element getbranch() {
        if (currentbranch != null) {
            return currentbranch;
        }
        return null;
    }

    public int getnumber() {
        return number;
    }

    public void setnumber(int Number) {
        number = Number;
    }
}
