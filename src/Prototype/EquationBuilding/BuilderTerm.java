/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Prototype.EquationBuilding;

import java.util.HashMap;
import javax.swing.JPanel;
import org.jdom2.Element;
import Prototype.StaticClasses.Global;
import Prototype.StaticClasses.TableHandling;

/**
 *
 * @author Gregory
 */
public class BuilderTerm {

    private HashMap<String, String> CommandMap;
    private Element Terms;
    private EQInterface Panel;
    private String EQPath;
    private String Name;
    private String LinkColumn;
    private java.util.ArrayList<String> Linklist;
    private boolean Link = false;
    private String Table;
    private String btnText;
    private Element source;
    private JPanel ParentPanel;

    //<editor-fold defaultstate="collapsed" desc="Constructors">
    public BuilderTerm(String text, Element Value, String eqpath, String name, String parenttable, String table, JPanel ParentPanel) {
        btnText = text;
        String xmlvalue = Value.getAttributeValue("value");
        EQPath = eqpath + "," + text;

        if (xmlvalue == null && text.startsWith("EVAL")) {
            xmlvalue = "EVAL:IF;PATH:" + eqpath;
        }
        CommandMap = Global.makecommandmap(xmlvalue);
        Terms = Value.getChild("TERMS");
        Name = name;
        Table = table;
        Linklist = TableHandling.getsharedforeignkeys(parenttable, table);
        String foreignkey = TableHandling.checkforeignkeys(parenttable, table);
        if (foreignkey != null && !foreignkey.isEmpty() && !Linklist.contains(foreignkey)) {
            Linklist.add(foreignkey);
        }

        if (CommandMap != null && CommandMap.containsKey("GROUPBY") && Linklist.contains(CommandMap.get("GROUPBY"))) {
            LinkColumn = CommandMap.get("GROUPBY");
        }

        this.ParentPanel = ParentPanel;
        source = Value;
    }

    public BuilderTerm(String text, String op, String eqpath, String path, String name, String parenttable, String table, JPanel ParentPanel) {
        btnText = text;
        CommandMap = new HashMap();
        CommandMap.put("PATH", path);
        if (op.equals("IF")) {
            CommandMap.put("EVAL", op);
        } else {
            CommandMap.put("OP", op);
        }
        Terms = new Element("TERMS");
        EQPath = eqpath;
        Name = name;
        Table = table;
        Linklist = TableHandling.getsharedforeignkeys(parenttable, table);
        String foreignkey = TableHandling.checkforeignkeys(parenttable, table);
        if (foreignkey != null && !foreignkey.isEmpty() && !Linklist.contains(foreignkey)) {
            Linklist.add(foreignkey);
        }
        if (CommandMap != null && CommandMap.containsKey("GROUPBY") && Linklist.contains(CommandMap.get("GROUPBY"))) {
            LinkColumn = CommandMap.get("GROUPBY");
        }
        this.ParentPanel = ParentPanel;
        source = new Element(name);
        source.setAttribute("value", Global.MakeCommandString(CommandMap));
        source.addContent(Terms);
    }
//</editor-fold>

    public JPanel getparentpanel() {
        return ParentPanel;
    }

    public boolean CheckSave() {
        if (Panel != null) {
            return Panel.CheckSave();
        }
        return true;
    }

    public boolean uselink() {
        return Link;
    }

    public Element getSource() {
        return source;
    }

    public void setlinkstate(boolean link) {
        Link = link;
    }

    public String getlink() {
        return LinkColumn;
    }

    public java.util.ArrayList<String> getlinkcolumns() {
        return Linklist;
    }

    public HashMap<String, String> GetCommandMap() {
        return CommandMap;
    }

    public void updatecommandmap(Element newvalue) {
        CommandMap = Global.makecommandmap(newvalue);
    }

    public String GetCommand(String Command) {
        return CommandMap.get(Command);
    }

    public String getEQPath() {
        return EQPath;
    }

    public void setpanel(EQInterface panel) {
        Panel = panel;
    }

    public EQInterface getpanel() {
        return Panel;
    }

    public Element saveXML() {
        if (this.Panel == null) {
            Element value = new Element(Name);
            String attribute = Global.MakeCommandString(CommandMap);
            value.setAttribute("value", attribute);
            if (Terms != null) {
                Terms = Terms.detach();
                value.addContent(Terms);
            }
            return value;
        } else {
            return Panel.Save();
        }
    }

    public Element getSavedXML() {
        Element value = new Element(Name);
        String attribute = Global.MakeCommandString(CommandMap);
        value.setAttribute("value", attribute);
        if (Terms != null) {
            Terms = Terms.detach();
            value.addContent(Terms);
        }
        return value;

    }

    public OPButton createbutton() {
        OPButton newbutton = new OPButton(btnText, this);
        return newbutton;
    }

    public String gettable() {
        return this.Table;
    }

    public String getName() {
        return Name;
    }
}
