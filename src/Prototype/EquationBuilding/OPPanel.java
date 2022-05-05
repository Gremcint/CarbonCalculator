/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Prototype.EquationBuilding;

import Prototype.StaticClasses.DateTextField;
import java.awt.Color;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import net.miginfocom.swing.MigLayout;
import org.jdom2.Element;
import Prototype.StaticClasses.Global;
import java.awt.Point;
import javax.swing.JScrollPane;

/**
 * I decided to go with an interface rather than inheriting from EQpanel as I
 * would have to rewrite every procedure anyways but still need some ability to
 * communicate between classes
 *
 * @author User
 */
public class OPPanel extends JPanel implements EQInterface {

    private javax.swing.JComboBox valuebox;
    private String Path;
    private int RowSelected = 0;
    private BuilderTerm Parent;
    private Element Equation;
    private ArrayList<JComponent> terms = new ArrayList();
    private ArrayList<JComponent> criteria = new ArrayList();
    private ArrayList<JComponent> order = new ArrayList();
    private boolean loading = false;
    private boolean saved;
    private EquationBuilder builder;
    private String tablename;
    private JComponent selected;
    private boolean inserting = false;
    private int insertionpoint = -1;
    private String groupby = "";
    private ArrayList<JComponent> insertionlist;
    private JSeparator Topdivider = new JSeparator(SwingConstants.HORIZONTAL);
    private Color bgcolor;
    private JSeparator Bottomdivider = new JSeparator(SwingConstants.HORIZONTAL);
    private ArrayList<String> oplist = new ArrayList();
    private String opmode;
    private String countcolumn;
    private ArrayList<String> valuelist;
    private ArrayList<String> links;

    public OPPanel(BuilderTerm Source, EquationBuilder Builder) {
        super();

        Path = Source.getEQPath();
        builder = Builder;
        saved = true;
        groupby = Source.getlink();
        if (groupby == null) {
            groupby = "";
        }
        links = Source.getlinkcolumns();
        links.add(0, "All matching rows");
        Parent = Source;
        Equation = Source.getSource();
        tablename = Source.gettable();
        loadequation();
        this.addMouseListener(new MouseListener() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (selected != null) {
                    selected.setBorder(null);
                    selected.updateUI();
                    selected.setBackground(bgcolor);
                    bgcolor = null;
                    selected = null;
                    builder.seteditenabled(false);
                }
            }
        });
    }


    /*
     * this code checks whether or not the current tab is saved and asks the user 
     * if they wish to save. for use when closing the tab.
     * yes, no, null means close the tab, if they chose yes it already saved
     * if it's cancel then don't close the tab.
     */
    private void loadequation() {
        loading = true;
        opmode = "";
        HashMap<String, String> eqmap = Global.makecommandmap(Equation.getAttributeValue("value"));
        Element currentequation = Equation.clone();
        String groupvalue = eqmap.get("GROUPBY");
        if (groupvalue != null && !groupvalue.isEmpty()) {
            groupby = groupvalue;
        }
        String equationtext = eqmap.get("EQ");

        if (eqmap.containsKey("OP") && eqmap.get("OP").equals("COUNT")) {
            opmode = "COUNT";
            countcolumn = eqmap.get("VALUE");
            HashMap<String, ArrayList<String>> tempmap = Prototype.StaticClasses.TableHandling.getequationvalues(tablename);
            valuelist = new ArrayList();
            valuelist.addAll(tempmap.get("ALL"));
            valuelist.remove(builder.getbase().getsavedeqname());
            valuelist.remove(builder.getbase().geteqname());

        } else {
            if (equationtext != null && !equationtext.isEmpty()) {
                ArrayList<String> termlist = Global.separateformula(equationtext);
                Element Subterms = currentequation.getChild("TERMS");
                processterms(equationtext, termlist, Subterms);
            }
        }
        if (eqmap.containsKey("OP") && (eqmap.get("OP").equals("HIGHEST") || eqmap.get("OP").equals("LOWEST"))) {
            opmode = "HIGHLOW";
        }
        String iftext = eqmap.get("IF");
        RowSelected = 1;
        if (iftext != null && !iftext.isEmpty()) {
            ArrayList<String> termlist = Global.separateformula(iftext);
            Element Subterms = currentequation.getChild("TERMS");
            processterms(iftext, termlist, Subterms);
        }
        if (opmode.equals("HIGHLOW")) {
            String ordertext = eqmap.get("ORDERBY");
            RowSelected = 2;
            if (ordertext != null && !ordertext.isEmpty()) {
                ArrayList<String> termlist = Global.separateformula(ordertext);
                Element Subterms = currentequation.getChild("TERMS");
                processterms(ordertext, termlist, Subterms);
            }
        }
        if (opmode.equals("COUNT")) {
            RowSelected = 1;
        } else {
            RowSelected = 0;
        }
        loadvalues();
        loading = false;
        printequation();
    }

    @Override
    public void loadvalues() {
        builder.loadvalues(tablename, false);
    }

    private void processterms(String equationtext, ArrayList<String> termlist, Element Subterms) {
        ArrayList<String> parselist = Global.getParseList();

        for (int x = 0; x < termlist.size(); x++) {
            String currentterm = termlist.get(x);
            if (parselist.contains(currentterm)) {
                if (currentterm.equals("-")) {
                    if (x < termlist.size() - 1) {
                        String nextterm = termlist.get(x + 1);
                        if (Global.isNumeric(nextterm) && (x == 0 || parselist.contains(termlist.get(x - 1)))) {
                            nextterm = currentterm + nextterm;
                            addnumber(nextterm);
                            x++;//skip the next term as we just added it here
                        } else {
                            EQButton newbutton = new EQButton(currentterm, currentterm);
                            addterm(newbutton);
                        }
                    }
                } else {
                    EQButton newbutton = new EQButton(currentterm, currentterm);
                    addterm(newbutton);
                }
            } else if (Global.isNumeric(currentterm)) {
                addnumber(currentterm);

            } else if (currentterm.startsWith("TXT:")) {
                if (Subterms != null && Subterms.getChild(currentterm.substring(4)) != null) {
                    String value = Subterms.getChild(currentterm).getAttributeValue("value");
                    addtext(value);
                }
            } else if (currentterm.startsWith("DATE:")) {
                if (Subterms != null && Subterms.getChild(currentterm.substring(4)) != null) {
                    String value = Subterms.getChild(currentterm).getAttributeValue("value");
                    adddate(java.sql.Date.valueOf(value));
                }
            } else if (currentterm.startsWith("FUNCTION:")) {
                String[] currentfunction = currentterm.split(":");
                EQButton newbutton;
                switch (currentfunction[1]) {
                    case "DAYOFWEEK":
                        newbutton = new EQButton("Get Day of Week", "FUNCTION:DAYOFWEEK");
                        addterm(newbutton);
                        break;
                    case "DAYOFMONTH":
                        newbutton = new EQButton("Get Day of Month", "FUNCTION:DAYOFMONTH");
                        addterm(newbutton);
                        break;
                    case "DAYOFYEAR":
                        newbutton = new EQButton("Get Day of Year", "FUNCTION:DAYOFYEAR");
                        addterm(newbutton);
                        break;
                    case "YEAR":
                        newbutton = new EQButton("Get Year", "FUNCTION:YEAR");
                        addterm(newbutton);
                        break;
                    case "MONTH":
                        newbutton = new EQButton("Get Month", "FUNCTION:MONTH");
                        addterm(newbutton);
                        break;
                    case "SQRT":
                        newbutton = new EQButton("<html>&#8730</html>", "FUNCTION:SQRT");
                        this.addterm(newbutton);
                        break;
                    case "CBRT":
                        newbutton = new EQButton("<html><sup>3</sup>&#8730</html>", "FUNCTION:SQRT");
                        this.addterm(newbutton);
                        break;
                    case "COS":
                        newbutton = new EQButton("COS", "FUNCTION:COS");
                        this.addterm(newbutton);
                        break;
                    case "SIN":
                        newbutton = new EQButton("SIN", "FUNCTION:SIN");
                        this.addterm(newbutton);
                        break;
                    case "TAN":
                        newbutton = new EQButton("TAN", "FUNCTION:TAN");
                        this.addterm(newbutton);
                        break;
                    case "ACOS":
                        newbutton = new EQButton("<html>COS<sup>-1</sup></html>", "FUNCTION:ACOS");
                        this.addterm(newbutton);
                        break;
                    case "ASIN":
                        newbutton = new EQButton("<html>ASIN<sup>-1</sup></html>", "FUNCTION:ASIN");
                        this.addterm(newbutton);
                        break;
                    case "ATAN":
                        newbutton = new EQButton("<html>ATAN<sup>-1</sup></html>", "FUNCTION:ATAN");
                        this.addterm(newbutton);
                        break;
                    case "EXP":
                        newbutton = new EQButton("EXP", "FUNCTION:EXP");
                        this.addterm(newbutton);
                        break;
                    case "LN":
                        newbutton = new EQButton("Ln", "FUNCTION:LN");
                        this.addterm(newbutton);
                        break;
                    case "LOG":
                        newbutton = new EQButton("Log", "FUNCTION:LOG");
                        this.addterm(newbutton);
                        break;
                    case "ROUNDOFF":
                        newbutton = new EQButton("Round", "FUNCTION:ROUNDOFF");
                        this.addterm(newbutton);
                        break;
                    case "ROUND DOWN":
                    case "ROUNDDOWN":
                        newbutton = new EQButton("Round Down", "FUNCTION:ROUNDDOWN");
                        this.addterm(newbutton);
                        break;
                    case "ROUND UP":
                    case "ROUNDUP":
                        newbutton = new EQButton("Round Up", "FUNCTION:ROUNDUP");
                        this.addterm(newbutton);
                        break;
                    case "ABSOLUTE":
                        newbutton = new EQButton("Absolute", "FUNCTION:ABSOLUTE");
                        this.addterm(newbutton);
                        break;
                }

            } else if (currentterm.startsWith("CONSTANT:")) {
                String[] currentfunction = currentterm.split(":");
                EQButton newbutton;
                switch (currentfunction[1]) {
                    case "LOG10E":
                        newbutton = new EQButton("Log10E", "CONSTANT:LOG10E");
                        this.addterm(newbutton);
                        break;
                    case "LN2":
                        newbutton = new EQButton("Ln2", "CONSTANT:LN2");
                        this.addterm(newbutton);
                        break;
                    case "LN10":
                        newbutton = new EQButton("Ln10", "CONSTANT:LN10");
                        this.addterm(newbutton);
                        break;
                    case "SQRT2":
                        newbutton = new EQButton("<html>&#8730(2)</html>", "CONSTANT:SQRT2");
                        this.addterm(newbutton);
                        break;
                    case "SQRT1_2":
                        newbutton = new EQButton("<html>&#8730(1/2)</html>", "CONSTANT:SQRT1_2");
                        this.addterm(newbutton);
                        break;
                    case "PI":
                        newbutton = new EQButton("<html>&#960</html>", "CONSTANT:PI");
                        this.addterm(newbutton);
                        break;
                    case "E":
                        newbutton = new EQButton("E", "CONSTANT:E");
                        this.addterm(newbutton);
                        break;
                }
            } else {
                if (Subterms != null && Subterms.getChild(currentterm) != null) {
                    if (equationtext.contains("OP:" + currentterm)) {
                        Element opterm = Subterms.getChild(currentterm);
                        HashMap<String, String> opmap = Global.makecommandmap(opterm);

                        addop(opmap.get("OP"), Subterms.getChild(currentterm));
                    } else if (equationtext.contains("EVAL:" + currentterm) && Subterms.getChild(currentterm) != null) {
                        addop("EVAL", Subterms.getChild(currentterm));
                    } else {
                        EQButton newbutton = new EQButton(currentterm, currentterm, Subterms.getChild(currentterm).clone());
                        addterm(newbutton);
                    }
                }
            }
        }
    }

    //this creates the xml from the current equation on screen
    @Override
    public Element Save() {
        saved = true;
        Element equation = new Element(Parent.getName());
        equation.addContent(new Element("TERMS"));
        String value = "PATH:TABLES," + tablename + ";";

        int textcount = 1;
        int datecount = 1;
        Element TERMS = equation.getChild("TERMS");
        if (opmode.equals("COUNT")) {
            value = value + "VALUE:" + valuebox.getSelectedItem().toString();
        } else {
            value = value + "EQ:";
            for (JComponent component : terms) {
                if (component.getClass() == JButton.class) {
                    JButton button = (JButton) component;
                    value = value + "`" + button.getText();
                } else if (component.getClass() == EQButton.class) {
                    EQButton tempbutton = (EQButton) component;
                    value = value + "`" + tempbutton.getEqText();
                    if (tempbutton.HasValue()) {
                        Element newelement = tempbutton.getvalue().clone();
                        if (TERMS.getChild(newelement.getName()) == null) {
                            TERMS.addContent(newelement);
                        }
                    }
                } else if (component.getClass() == JFormattedTextField.class) {
                    JFormattedTextField textfield = (JFormattedTextField) component;
                    String text = textfield.getText();
                    if (Global.isDate(text)) {
                        value = value + "`" + "DATE:DATE" + datecount;
                        Element date = new Element("DATE" + datecount);
                        datecount++;
                        date.setAttribute("value", text);
                        TERMS.addContent(date);
                    } else {
                        value = value + "`" + text;
                    }

                } else if (component.getClass() == JTextField.class) {
                    JTextField tempbox = (JTextField) component;
                    value = value + "`" + "TXT:TEXT" + textcount;
                    Element text = new Element("TEXT" + textcount);
                    text.setAttribute("value", tempbox.getText());
                    textcount++;
                    TERMS.addContent(text);
                } else if (component.getClass() == OPButton.class) {
                    OPButton current = (OPButton) component;
                    BuilderTerm currentterm = current.getterm();
                    value = value + "`" + current.getText();
                    Element newelement = currentterm.saveXML().clone();
                    if (TERMS.getChild(newelement.getName()) == null) {
                        TERMS.addContent(newelement);
                    }
                }
            }
        }

        String condition = "IF:";
        for (JComponent component : criteria) {
            if (component.getClass() == JButton.class) {
                JButton button = (JButton) component;
                condition = condition + "`" + button.getText();
            } else if (component.getClass() == EQButton.class) {
                EQButton tempbutton = (EQButton) component;
                condition = condition + "`" + tempbutton.getEqText();
                if (tempbutton.HasValue()) {
                    Element newelement = tempbutton.getvalue().clone();
                    if (TERMS.getChild(newelement.getName()) == null) {
                        TERMS.addContent(newelement);
                    }
                }
            } else if (component.getClass() == JFormattedTextField.class) {
                JFormattedTextField textfield = (JFormattedTextField) component;
                String text = textfield.getText();
                if (Global.isDate(text)) {
                    condition = condition + "`" + "DATE:DATE" + datecount;
                    Element date = new Element("DATE" + datecount);
                    datecount++;
                    date.setAttribute("value", text);
                    TERMS.addContent(date);
                } else {
                    condition = condition + "`" + text;
                }
            } else if (component.getClass() == JTextField.class) {
                JTextField tempbox = (JTextField) component;
                condition = condition + "`" + "TXT:TEXT" + textcount;
                Element text = new Element("TEXT" + textcount);
                text.setAttribute("value", tempbox.getText());
                TERMS.addContent(text);
                textcount++;
            } else if (component.getClass() == OPButton.class) {
                OPButton current = (OPButton) component;
                BuilderTerm currentterm = current.getterm();
                condition = condition + "`" + current.getText();
                Element newelement = currentterm.saveXML().clone();
                if (TERMS.getChild(newelement.getName()) == null) {
                    TERMS.addContent(newelement);
                }
            }
        }
        String orderby = "";

        if (opmode.equals("HIGHLOW")) {
            orderby = ";ORDERBY:";
            for (JComponent component : order) {
                if (component.getClass() == EQButton.class) {
                    EQButton tempbutton = (EQButton) component;
                    orderby = orderby + "`" + tempbutton.getEqText();
                    if (tempbutton.HasValue()) {
                        Element newelement = tempbutton.getvalue().clone();
                        if (TERMS.getChild(newelement.getName()) == null) {
                            TERMS.addContent(newelement);
                        }
                    }
                }
            }
        }

        String eqstring = value + ";" + condition + orderby + ";OP:" + Parent.GetCommand("OP") + ";GROUPBY:" + groupby;
        equation.setAttribute("value", eqstring);
        return equation;

    }

    private void printequation() {
        try {
            oplist.clear();
            if (!loading) {
                this.removeAll();

                this.setLayout(new MigLayout());
                JLabel TableLabel = new JLabel();
                TableLabel.setText("Path: " + Path);
                this.add(TableLabel, "span");

                JSeparator topseparator = new JSeparator(SwingConstants.HORIZONTAL);
                topseparator.setBackground(Color.black);
                topseparator.setForeground(Color.black);
                this.add(topseparator, "newline, wrap, span");
                ButtonGroup group = new ButtonGroup();
                JRadioButton rdoValue = new JRadioButton("Value: ");
                JRadioButton rdoCriteria = new JRadioButton("Criteria: ");
                JRadioButton rdoOrder = new JRadioButton("Order By: ");
                if (!opmode.equals("COUNT")) {
                    rdoValue.addActionListener(new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            RowSelected = 0;
                            builder.loadvalues(tablename, false);
                        }
                    });
                    rdoCriteria.addActionListener(new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            RowSelected = 1;
                            builder.loadvalues(tablename, true);

                        }
                    });
                    rdoOrder.addActionListener(new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            RowSelected = 2;
                            builder.loadvalues(tablename, true);

                        }
                    });
                    group.add(rdoValue);
                    group.add(rdoCriteria);
                    group.add(rdoOrder);
                    add(rdoValue);

                    for (JComponent currentterm : terms) {
                        currentterm.setEnabled(true);
                        this.add(currentterm);
                        if (currentterm.getClass() == OPButton.class) {
                            OPButton temp = (OPButton) currentterm;
                            oplist.add(temp.getterm().getName());
                        }
                    }
                } else {
                    builder.loadvalues(tablename, true);
                    valuebox = new javax.swing.JComboBox();
                    add(new JLabel("Selected the table column to be counted: "));
                    javax.swing.DefaultComboBoxModel model = new javax.swing.DefaultComboBoxModel(valuelist.toArray());
                    valuebox.setModel(model);
                    if (countcolumn != null && model.getIndexOf(countcolumn) >= 0) {
                        valuebox.setSelectedItem(countcolumn);
                    } else {
                        valuebox.setSelectedIndex(0);
                    }
                    add(valuebox);
                }
                Topdivider = new JSeparator(SwingConstants.HORIZONTAL);
                this.add(Topdivider, "newline, wrap, span");
                if (!opmode.equals("COUNT")) {
                    add(rdoCriteria);
                } else {
                    add(new JLabel("Criteria: "));
                }
                for (JComponent currentterm : criteria) {
                    currentterm.setEnabled(true);
                    add(currentterm);
                    if (currentterm.getClass() == OPButton.class) {
                        OPButton temp = (OPButton) currentterm;
                        oplist.add(temp.getterm().getName());
                    }
                }
                if (opmode.equals("HIGHLOW")) {
                    Bottomdivider = new JSeparator(SwingConstants.HORIZONTAL);
                    add(Bottomdivider, "newline, wrap, span");
                    add(rdoOrder);

                    for (JComponent currentterm : order) {
                        currentterm.setEnabled(true);
                        add(currentterm);
                        if (currentterm.getClass() == OPButton.class) {
                            OPButton temp = (OPButton) currentterm;
                            oplist.add(temp.getterm().getName());
                        }
                    }
                }

                if (RowSelected == 0) {
                    rdoValue.setSelected(true);
                    rdoCriteria.setSelected(false);
                    rdoOrder.setSelected(false);
                } else if (RowSelected == 1) {
                    rdoValue.setSelected(false);
                    rdoCriteria.setSelected(true);
                    rdoOrder.setSelected(false);
                } else {
                    rdoValue.setSelected(false);
                    rdoCriteria.setSelected(false);
                    rdoOrder.setSelected(true);
                }
                builder.seteditenabled(false);
                JSeparator bottomseparator = new JSeparator(SwingConstants.HORIZONTAL);
                if (links != null && links.size() > 0) {

                    bottomseparator.setBackground(Color.black);
                    bottomseparator.setForeground(Color.black);
                    this.add(bottomseparator, "newline, wrap, span");

                    JLabel grouping = new JLabel("Group values by: ");
                    this.add(grouping);
                    javax.swing.JComboBox grouplist = new javax.swing.JComboBox(links.toArray());
                    if (groupby != null && !groupby.isEmpty()) {
                        grouplist.setSelectedItem(groupby);
                    }
                    if (Parent.uselink()) {
                        grouplist.setSelectedItem(groupby);
                    }
                    grouplist.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            javax.swing.JComboBox grouptemp = (javax.swing.JComboBox) e.getSource();
                            if (grouptemp.getSelectedIndex() == 0) {
                                groupby = "";
                            } else {
                                groupby = grouptemp.getSelectedItem().toString();
                            }
                        }
                    });
                    this.add(grouplist, "span,wrap");
                }

                this.setSize(this.getPreferredSize());//I don't know why this works but it does, it lets the panel expand with new controls
                if (this.getWidth() < 900) {
                    topseparator.setPreferredSize(new Dimension(900, topseparator.getHeight()));
                    bottomseparator.setPreferredSize(new Dimension(900, bottomseparator.getHeight()));
                    Topdivider.setPreferredSize(new Dimension(900, Topdivider.getHeight()));
                    Bottomdivider.setPreferredSize(new Dimension(900, Bottomdivider.getHeight()));

                } else {
                    topseparator.setPreferredSize(new Dimension(this.getWidth(), topseparator.getHeight()));
                    bottomseparator.setPreferredSize(new Dimension(this.getWidth(), bottomseparator.getHeight()));
                    Topdivider.setPreferredSize(new Dimension(this.getWidth(), Topdivider.getHeight()));
                    Bottomdivider.setPreferredSize(new Dimension(this.getWidth(), Bottomdivider.getHeight()));
                }
                if (opmode.equals("HIGHLOW")) {
                    this.add(new JLabel("Add terms from the table to determine sorting, will go left to right, if none will sort by the value of the equation result."), "span");
                }
                this.repaint();
                this.revalidate();

            }

        } catch (Exception e) {
            Global.Printmessage("OPPanel.Printequation " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    @Override
    public void addsymbol(String symbol) {
        if (RowSelected != 2) {
            saved = false;
            JButton currentbutton = new JButton(symbol);
            currentbutton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    JButton button = (JButton) evt.getSource();
                    if (selected != null) {
                        selected.setBackground(bgcolor);
                    }
                    bgcolor = button.getBackground();
                    button.setBackground(Color.CYAN);
                    selected = button;

                    builder.seteditenabled(false);
                }
            });
            currentbutton.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseReleased(java.awt.event.MouseEvent evt) {
                    Point target = getMousePosition();
                    JComponent source = (JComponent) evt.getSource();
                    movecontrol(source, target);
                    endinsert();
                }
            });
            insertcontrol(currentbutton);
            printequation();
        }
    }

    private void insertcontrol(JComponent currentcontrol) {
        if (inserting) {
            if (RowSelected == 0) {
                terms.add(insertionpoint, currentcontrol);
            } else if (RowSelected == 1) {
                criteria.add(insertionpoint, currentcontrol);
            } else {
                if (currentcontrol.getClass().equals(EQButton.class)) {
                    EQButton temp = (EQButton) currentcontrol;
                    if (temp.getEqText() != null && temp.getvaluetext().contains("PATH:")) {
                        order.add(insertionpoint, currentcontrol);
                    }
                }
            }
        } else {
            if (RowSelected == 0) {
                terms.add(currentcontrol);
            } else if (RowSelected == 1) {
                criteria.add(currentcontrol);
            } else {
                if (currentcontrol.getClass().equals(EQButton.class)) {
                    EQButton temp = (EQButton) currentcontrol;
                    if (temp.getEqText() != null && temp.getvaluetext().contains("PATH:")) {
                        order.add(currentcontrol);
                    }
                }
            }
        }
    }

    @Override
    public void addnumber(String number) {
        if (RowSelected != 2) {
            saved = false;
            JFormattedTextField Number = new JFormattedTextField(NumberFormat.getInstance());

            Number.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseReleased(java.awt.event.MouseEvent evt) {
                    Point target = getMousePosition();
                    JComponent source = (JComponent) evt.getSource();
                    movecontrol(source, target);
                    endinsert();
                }

                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    JFormattedTextField Text = (JFormattedTextField) evt.getSource();
                    if (selected != null) {
                        selected.setBackground(bgcolor);
                    }
                    bgcolor = Text.getBackground();
                    Text.setBackground(Color.CYAN);
                    selected = Text;

                    builder.seteditenabled(false);

                }
            });
            Number.setText(number);
            Number.setColumns(5);
            insertcontrol(Number);
            printequation();
        }
    }

    @Override
    public void addnumber() {
        if (RowSelected != 2) {
            saved = false;
            JFormattedTextField Number = new JFormattedTextField(NumberFormat.getInstance());

            Number.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseReleased(java.awt.event.MouseEvent evt) {
                    Point target = getMousePosition();
                    JComponent source = (JComponent) evt.getSource();
                    movecontrol(source, target);
                    endinsert();
                }

                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    JFormattedTextField Text = (JFormattedTextField) evt.getSource();
                    if (selected != null) {
                        selected.setBackground(bgcolor);
                    }
                    bgcolor = Text.getBackground();
                    Text.setBackground(Color.CYAN);
                    selected = Text;

                    builder.seteditenabled(false);

                }
            });
            Number.setText("0");
            Number.setColumns(5);
            insertcontrol(Number);
            printequation();
        }
    }

    @Override
    public void addterm(OPButton currentbutton) {
        saved = false;

        currentbutton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {

                JButton button = (JButton) evt.getSource();
                if (selected != null) {
                    selected.setBackground(bgcolor);
                }
                bgcolor = button.getBackground();
                button.setBackground(Color.CYAN);
                selected = button;

                builder.seteditenabled(true);
            }
        });
        currentbutton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                Point target = getMousePosition();
                JComponent source = (JComponent) evt.getSource();
                movecontrol(source, target);
                endinsert();
            }
        });
        insertcontrol(currentbutton);
        printequation();
    }

    @Override
    public void duplicate() {
        if (selected != null) {
            if (selected.getClass().equals(JFormattedTextField.class)) {
                JFormattedTextField field = (JFormattedTextField) selected;
                JFormattedTextField newfield = new JFormattedTextField(field.getFormatter());
                newfield.setText(field.getText());
                newfield.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseReleased(java.awt.event.MouseEvent evt) {
                        Point target = getMousePosition();
                        JComponent source = (JComponent) evt.getSource();
                        movecontrol(source, target);
                        endinsert();
                    }

                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent evt) {

                        JFormattedTextField field = (JFormattedTextField) evt.getSource();
                        if (selected != null) {
                            selected.setBackground(bgcolor);
                        }
                        bgcolor = field.getBackground();
                        field.setBackground(Color.CYAN);
                        selected = field;

                        builder.seteditenabled(false);
                    }
                });

                insertcontrol(newfield);
                saved = false;
                printequation();
            } else if (selected.getClass().equals(JTextField.class)) {
                JTextField field = (JTextField) selected;
                addtext(field.getText());
            } else if (selected.getClass().equals(JButton.class)) {
                JButton button = (JButton) selected;
                addsymbol(button.getText());
            } else if (selected.getClass().equals(OPButton.class)) {
                OPButton button = (OPButton) selected;
                addterm(button.getterm().createbutton());

            } else if (selected.getClass().equals(EQButton.class)) {
                EQButton button = (EQButton) selected;
                addterm(button.Clone());
            }
        }
    }

    @Override
    public void addterm(EQButton currentbutton) {

        saved = false;
        currentbutton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JButton button = (JButton) evt.getSource();
                if (selected != null) {
                    selected.setBackground(bgcolor);
                }
                bgcolor = button.getBackground();
                button.setBackground(Color.CYAN);
                selected = button;

                builder.seteditenabled(false);
            }
        });
        currentbutton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                Point target = getMousePosition();
                JComponent source = (JComponent) evt.getSource();
                movecontrol(source, target);
                endinsert();
            }
        });
        insertcontrol(currentbutton);
        printequation();
    }

    @Override
    public void adddate(java.sql.Date date) {
        if (RowSelected != 2) {
            DateTextField Datefield = new DateTextField();
        Datefield.setPreferredSize(new Dimension(100, Datefield.getHeight()));
        Datefield.setText(date.toString());
            Datefield.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    JFormattedTextField field = (JFormattedTextField) evt.getSource();
                    if (selected != null) {
                        selected.setBackground(bgcolor);
                    }
                    bgcolor = field.getBackground();
                    field.setBackground(Color.CYAN);
                    selected = field;

                    builder.seteditenabled(false);
                }
            });
            Datefield.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseReleased(java.awt.event.MouseEvent evt) {
                    Point target = getMousePosition();
                    JComponent source = (JComponent) evt.getSource();
                    movecontrol(source, target);
                    endinsert();
                }
            });

            saved = false;
            insertcontrol(Datefield);
            printequation();
        }
    }

    @Override
    public void adddate() {
        if (RowSelected != 2) {
            DateTextField Datefield = new DateTextField();
        Datefield.setPreferredSize(new Dimension(100, Datefield.getHeight()));
        
            Datefield.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseReleased(java.awt.event.MouseEvent evt) {
                    Point target = getMousePosition();
                    JComponent source = (JComponent) evt.getSource();
                    movecontrol(source, target);
                    endinsert();
                }

                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    JFormattedTextField Text = (JFormattedTextField) evt.getSource();
                    if (selected != null) {
                        selected.setBackground(bgcolor);
                    }
                    bgcolor = Text.getBackground();
                    Text.setBackground(Color.CYAN);
                    selected = Text;

                    builder.seteditenabled(false);

                }
            });
            insertcontrol(Datefield);
            saved = false;
            printequation();
        }
    }

    //<editor-fold defaultstate="collapsed" desc="addop">
    @Override
    public void addop(String OP) {
        if (OP != null && RowSelected != 2) {
            saved = false;
            int x = 1;
            while ((Equation.getChild("TERMS") != null && Equation.getChild("TERMS").getChild(OP + x) != null) || oplist.contains(OP + x)) {
                x++;
            }
            String name = OP + x;
            BuilderTerm newterm;
            JPanel pnlEquationDisplay = builder.getmainpanel();
            JScrollPane scpEq;
            pnlEquationDisplay.removeAll();
            switch (OP) {
                case "IF":
                    newterm = new BuilderTerm("EVAL:" + name, OP, this.Path + "," + name, this.Path, name, tablename, tablename, this);//this is the basepanel class only used for the main equation so therefore it will have a different structure
                    addterm(newterm.createbutton());
                    IFPanel newpanel = new IFPanel(newterm, builder);
                    newterm.setpanel(newpanel);
                    scpEq = new JScrollPane(newpanel);

                    pnlEquationDisplay.add(scpEq);
                    pnlEquationDisplay.repaint();
                    pnlEquationDisplay.revalidate();
                    builder.SetActivePanel(newpanel);
                    break;
                case "PREVIOUS":
                    Element compare;
                    JDialog dialog = new JDialog();
                    PrevValue panel = new PrevValue(tablename, name, dialog);
                    dialog.setTitle("Previous Value Selector");

                    dialog.add(panel);
                    dialog.pack();
                    dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
                    dialog.setVisible(true);
                    compare = panel.getXML();
                    if (compare != null) {
                        //String text, Element Value, String eqpath, String name, String parenttable, String table
                        newterm = new BuilderTerm("OP:" + name, compare, this.Path + "," + name, name, tablename, tablename, this);
                        addterm(newterm.createbutton());
                    }
                    reloadpanel();

                    break;
                case "TVAL":
                    Element tval;
                    JDialog tvaldialog = new JDialog();
                    TValuePopUp tvpop = new TValuePopUp(1, tvaldialog, name);
                    tvaldialog.add(tvpop);
                    tvaldialog.setTitle("Please Select Confidence Level");

                    tvaldialog.pack();
                    tvaldialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
                    tvaldialog.setVisible(true);
                    if (tvpop.getcomplete()) {
                        tval = tvpop.getcl();
                        //String text, Element Value, String eqpath, String name, String parenttable, String table
                        newterm = new BuilderTerm("FUNCTION:TVAL:" + name, tval, this.Path + "," + name, name, tablename, tablename, this);
                        addterm(newterm.createbutton());
                        addsymbol("(");
                    }
                    reloadpanel();
                    break;
                default:
                    String tablechoice = Prototype.Popups.TextForm.choosetabledialog(Global.getcurrentschema());
                    if(tablechoice!=null){newterm = new BuilderTerm("OP:" + name, OP, this.Path + "," + name, this.Path, name, tablename, tablechoice, this);
                    addterm(newterm.createbutton());
                    OPPanel newoppanel = new OPPanel(newterm, builder);
                    newterm.setpanel(newoppanel);
                    scpEq = new JScrollPane(newoppanel);
                    pnlEquationDisplay.add(scpEq);
                    pnlEquationDisplay.repaint();
                    pnlEquationDisplay.revalidate();
                    builder.SetActivePanel(newoppanel);
                    }
                    break;
            }
        }
    }

    public void reloadpanel() {
        //for some reason the tvalue and the previous functions wouldn't refresh
        //the panel afterwards and it would be bugged. this code reloads the panel
        //entirely and seems to fix that.
        JPanel pnlEquationDisplay = builder.getmainpanel();
        JScrollPane scpEq;
        pnlEquationDisplay.removeAll();

        scpEq = new JScrollPane(this);
        pnlEquationDisplay.add(scpEq);
        pnlEquationDisplay.repaint();
        pnlEquationDisplay.revalidate();
    }

    @Override
    public void addop(String OP, Element Term) {
        if (OP != null && RowSelected != 2) {
            saved = false;
            HashMap<String, String> commandmap = Global.makecommandmap(Term.getAttributeValue("value"));

            String name = Term.getName();
            BuilderTerm newterm;
            switch (OP) {
                case "EVAL":
                case "IF":
                    newterm = new BuilderTerm("EVAL:" + name, Term, this.Path + "," + name, name, tablename, tablename, this);//this is the basepanel class only used for the main equation so therefore it will have a different structure
                    addterm(newterm.createbutton());
                    break;
                case "TVAL":
                    newterm = new BuilderTerm("FUNCTION:TVAL:" + name, Term, this.Path + "," + name, name, tablename, tablename, this);
                    addterm(newterm.createbutton());
                    break;
                default:
                    if (commandmap.containsKey("PATH") && !commandmap.get("PATH").isEmpty()) {
                        String[] EQPathsplit = commandmap.get("PATH").split(",");
                        String table = EQPathsplit[EQPathsplit.length - 1];
                        newterm = new BuilderTerm("OP:" + name, Term, this.Path + "," + name, name, tablename, table, this);
                    } else {
                        newterm = new BuilderTerm("OP:" + name, Term, this.Path + "," + name, name, tablename, tablename, this);
                    }
                    addterm(newterm.createbutton());
                    break;
            }
            if (commandmap != null && commandmap.containsKey("GROUPBY") && commandmap.get("GROUPBY").equals(newterm.getlink())) {
                newterm.setlinkstate(true);
            }
        }
    }
//</editor-fold>

    @Override
    public void addtext(String text) {
        if (RowSelected != 2) {
            saved = false;
            JTextField Text = new JTextField(text, 20);

            Text.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseReleased(java.awt.event.MouseEvent evt) {
                    Point target = getMousePosition();
                    JComponent source = (JComponent) evt.getSource();
                    movecontrol(source, target);
                    endinsert();
                }

                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    JTextField Text = (JTextField) evt.getSource();
                    if (selected != null) {
                        selected.setBackground(bgcolor);
                    }
                    bgcolor = Text.getBackground();
                    Text.setBackground(Color.CYAN);
                    selected = Text;

                    builder.seteditenabled(false);

                }
            });
            insertcontrol(Text);
            printequation();
        }
    }

    @Override
    public void addtext() {
        if (RowSelected != 2) {
            saved = false;
            JTextField Text = new JTextField("", 20);

            Text.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseReleased(java.awt.event.MouseEvent evt) {
                    Point target = getMousePosition();
                    JComponent source = (JComponent) evt.getSource();
                    movecontrol(source, target);
                    endinsert();
                }

                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    JTextField Text = (JTextField) evt.getSource();
                    if (selected != null) {
                        selected.setBackground(bgcolor);
                    }
                    bgcolor = Text.getBackground();
                    Text.setBackground(Color.CYAN);
                    selected = Text;

                    builder.seteditenabled(false);

                }
            });
            insertcontrol(Text);
            printequation();
        }
    }

    @Override
    public boolean CheckSave() {
        if (saved) {
            for (JComponent component : terms) {
                if (component.getClass() == OPButton.class) {
                    OPButton current = (OPButton) component;
                    BuilderTerm currentterm = current.getterm();
                    if (currentterm.CheckSave() == false) {
                        return false;
                    }
                }
            }
            for (JComponent component : criteria) {
                if (component.getClass() == OPButton.class) {
                    OPButton current = (OPButton) component;
                    BuilderTerm currentterm = current.getterm();
                    if (currentterm.CheckSave() == false) {
                        return false;
                    }
                }
            }

            return true;
        }
        return false;
    }

    @Override
    public void backspace() {
        if (RowSelected == 0) {
            if (terms.size() > 0) {
                terms.remove(terms.size() - 1);
                saved = false;
                printequation();
            }
        } else if (RowSelected == 1) {
            if (criteria.size() > 0) {
                criteria.remove(criteria.size() - 1);
                saved = false;
                printequation();
            }
        } else {
            if (order.size() > 0) {
                order.remove(order.size() - 1);
                saved = false;
                printequation();
            }
        }
    }

    @Override
    public void delete() {
        if (selected != null) {
            if (terms.contains(selected)) {
                terms.remove(selected);
                bgcolor = null;
                selected = null;
                saved = false;
                printequation();
            } else if (criteria.contains(selected)) {
                criteria.remove(selected);
                bgcolor = null;
                selected = null;
                saved = false;
                printequation();
            }
        } else if (order.contains(selected)) {
            order.remove(selected);
            bgcolor = null;
            selected = null;
            saved = false;
            printequation();
            builder.seteditenabled(false);
        }
    }

    @Override
    public void moveleft() {
        if (selected != null) {
            if (terms.contains(selected)) {
                int index = terms.indexOf(selected);
                if (index > 1) {
                    Collections.swap(terms, index, index - 1);
                    saved = false;
                    printequation();
                }
            } else if (criteria.contains(selected)) {
                int index = criteria.indexOf(selected);
                if (index > 1) {
                    Collections.swap(criteria, index, index - 1);
                    saved = false;
                    printequation();
                }
            } else if (order.contains(selected)) {
                int index = order.indexOf(selected);
                if (index > 1) {
                    Collections.swap(order, index, index - 1);
                    saved = false;
                    printequation();
                }
            }
        }
    }

    @Override
    public void moveright() {
        if (selected != null) {
            if (terms.contains(selected)) {
                int index = terms.indexOf(selected);
                if (index > terms.size() - 1) {
                    Collections.swap(terms, index, index + 1);
                    saved = false;
                    printequation();
                }
            } else if (criteria.contains(selected)) {
                int index = criteria.indexOf(selected);
                if (index > criteria.size() - 1) {
                    Collections.swap(criteria, index, index + 1);
                    saved = false;
                    printequation();
                }
            } else if (order.contains(selected)) {
                int index = order.indexOf(selected);
                if (index > order.size() - 1) {
                    Collections.swap(order, index, index + 1);
                    saved = false;
                    printequation();
                }
            }
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Tab Change">
    @Override
    public void loadparent() {
        JPanel pnlEquationDisplay = builder.getmainpanel();
        JScrollPane scpEq;
        pnlEquationDisplay.removeAll();
        JPanel newpanel = Parent.getparentpanel();

        scpEq = new JScrollPane(newpanel);
        pnlEquationDisplay.add(scpEq);
        pnlEquationDisplay.repaint();
        pnlEquationDisplay.revalidate();
        builder.SetActivePanel((EQInterface) newpanel);
        builder.GetActivePanel().loadvalues();
        printequation();
    }

    public void changepanel(BuilderTerm term) {
        JPanel newpanel = (JPanel) term.getpanel();
        JPanel pnlEquationDisplay = builder.getmainpanel();
        JScrollPane scpEq;
        pnlEquationDisplay.removeAll();
        if (newpanel == null) {
            HashMap<String, String> commands = term.GetCommandMap();
            if (commands.containsKey("OP")) {
                OPPanel oppanel = new OPPanel(term, builder);
                scpEq = new JScrollPane(oppanel);
                pnlEquationDisplay.add(scpEq);
                pnlEquationDisplay.repaint();
                pnlEquationDisplay.revalidate();
                builder.SetActivePanel(oppanel);
                term.setpanel(oppanel);
                builder.SetActivePanel(oppanel);
            } else if (commands.containsKey("EVAL")) {
                IFPanel oppanel = new IFPanel(term, builder);
                scpEq = new JScrollPane(oppanel);
                pnlEquationDisplay.add(scpEq);
                pnlEquationDisplay.repaint();
                pnlEquationDisplay.revalidate();
                builder.SetActivePanel(oppanel);
                term.setpanel(oppanel);
                builder.SetActivePanel(oppanel);
            }
        } else {
            scpEq = new JScrollPane(newpanel);
            pnlEquationDisplay.add(scpEq);
            pnlEquationDisplay.repaint();
            pnlEquationDisplay.revalidate();

            builder.SetActivePanel((EQInterface) newpanel);
        }
    }

    @Override
    public void editop() {
        if (selected.getClass() == OPButton.class) {
            OPButton currentbutton = (OPButton) selected;

            BuilderTerm term = currentbutton.getterm();
            String Op = term.GetCommand("OP");
            if (Op != null && Op.equals("TVAL")) {
                Element tval;
                JDialog tvaldialog = new JDialog();
                int cl = 1;
                String clvalue = term.GetCommand("CONFIDENCE");
                if (clvalue != null && Global.isNumeric(clvalue)) {
                    cl = Integer.valueOf(clvalue);
                }
                TValuePopUp tvpop = new TValuePopUp(cl, tvaldialog, term.getName());
                tvaldialog.add(tvpop);
                tvaldialog.setTitle("Please Select Confidence Level");

                tvaldialog.pack();

                tvaldialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
                tvaldialog.setVisible(true);
                if (tvpop.getcomplete()) {
                    tval = tvpop.getcl();
                    term.updatecommandmap(tval);
                }
            } else if (Op != null && Op.equals("PREVIOUS")) {
                JDialog dialog = new JDialog();
                Element Prev = term.getSavedXML();
                PrevValue panel = new PrevValue(tablename, Prev, dialog);
                dialog.setTitle("Previous Value Selector");

                dialog.add(panel);
                dialog.pack();
                dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
                dialog.setVisible(true);
                Prev = panel.getXML();
                if (Prev != null) {
                    term.updatecommandmap(Prev);
                }
            } else {
                changepanel(term);
            }
        }
    }

//</editor-fold>
    @Override
    public void endinsert() {
        inserting = false;
        insertionlist = null;
        insertionpoint = -1;
    }

    @Override
    public void dropControl() {
        Point target = this.getMousePosition();
        if (target != null) {
            inserting = true;

            int topdividingheight = Topdivider.getY();

            if (opmode.equals("COUNT")) {
                insertionlist = criteria;
            } else {
                if (target.getY() <= topdividingheight) {
                    insertionlist = terms;
                } else if (!opmode.equals("HIGHLOW") || target.getY() <= Bottomdivider.getY()) {
                    insertionlist = criteria;
                } else {
                    insertionlist = order;
                }
            }

            if (insertionlist.size() > 0) {
                JComponent targetcomponent = (JComponent) getComponentAt(target);

                if (targetcomponent != null && insertionlist.contains(targetcomponent)) {
                    double targetx = targetcomponent.getLocation().getX();
                    int index = insertionlist.indexOf(targetcomponent);
                    double midpoint = targetx + targetcomponent.getWidth() / 2;
                    if (target.getX() > midpoint) {
                        insertionpoint = index + 1;
                    } else {
                        insertionpoint = index;
                    }
                } else {
                    JComponent firstcontrol = insertionlist.get(0);
                    double top = firstcontrol.getLocation().getY() - 20;
                    double bottom = top + 40 + firstcontrol.getHeight();
                    if (top < 0) {
                        top = 0;
                    }
                    double targety = target.getY();
                    if (targety >= top && targety <= bottom) {
                        double targetx = target.getX();
                        boolean found = false;

                        JComponent lastterm = insertionlist.get(insertionlist.size() - 1);
                        int farx = lastterm.getX() + lastterm.getWidth();
                        if (targetx > farx) {
                            found = true;
                            insertionpoint = insertionlist.size();
                        } else {
                            for (JComponent currentterm : insertionlist) {
                                if (targetx < currentterm.getLocation().getX()) {
                                    found = true;
                                    insertionpoint = insertionlist.indexOf(currentterm);
                                    break;
                                }
                            }
                        }
                        inserting = found;
                    } else {
                        inserting = false;
                    }
                }
            }
        } else {
            inserting = false;
        }
    }

    private void movecontrol(JComponent control, Point target) {
        JComponent targetcomp = (JComponent) this.getComponentAt(target);
        if (!targetcomp.equals(control)) {
            dropControl();
            if (insertionpoint >= insertionlist.size() || insertionpoint == -1) {
                insertionlist.remove(control);
                insertionlist.add(control);
            } else if (insertionpoint > insertionlist.indexOf(control) + 1) {
                insertionlist.remove(control);
                insertionlist.add(insertionpoint - 1, control);
            } else if (insertionpoint < insertionlist.indexOf(control)) {
                insertionlist.remove(control);
                insertionlist.add(insertionpoint, control);
            }
            printequation();
            endinsert();
        }
    }

    @Override
    public Point getmousedrop() {
        return this.getMousePosition();
    }
}
