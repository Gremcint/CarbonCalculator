/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Prototype.EquationBuilding;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.Point;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import net.miginfocom.swing.MigLayout;
import org.jdom2.Element;
import Prototype.StaticClasses.DateTextField;
import Prototype.StaticClasses.Global;


/**
 *
 * @author User
 */
public class EQPanel extends JPanel implements EQInterface {

    public static final int newequation = 0;
    public static final int newversion = 1;
    public static final int existingequation = 2;
    private String versionnumber;
    private String tablename;
    private String currentname;
    private int mode;
    private String savedname;
    private String Path;
    private Element Equation;
    private ArrayList<JComponent> terms = new ArrayList();
    private boolean loading;
    private boolean saved;
    private EquationBuilder builder;
    private String XMLPath;
    private JComponent selected = null;
    private boolean inserting = false;
    private int insertionpoint;
    private Color bgcolor;
    private ArrayList<String> oplist = new ArrayList();

    //<editor-fold defaultstate="collapsed" desc="Constructors">
    public EQPanel(Element Equation, String name, String path, EquationBuilder Builder, String tablename, String XMLPath, int Mode, String version) {
        super();
        mode = Mode;
        versionnumber = version;
        builder = Builder;
        this.tablename = tablename;
        this.Equation = Equation;
        savedname = name;
        currentname = savedname;
        setLayout(new MigLayout());
        insertionpoint = -1;
        loading = false;
        Path = path;
        saved = true;
        loadequation();
        this.XMLPath = XMLPath;
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
                    selected.setBackground(bgcolor);
                    selected = null;
                    bgcolor = null;
                    builder.seteditenabled(false);
                }
            }
        });
    }

    public EQPanel(String Name, String path, EquationBuilder Builder, String tablename, String XMLPath, int Mode, String version) {
        super();
        mode = Mode;
        versionnumber = version;
        builder = Builder;
        insertionpoint = -1;

        this.tablename = tablename;
        this.Equation = new Element(Name);
        this.XMLPath = XMLPath;
        this.setLayout(new MigLayout());
        loading = false;
        Path = path;
        this.savedname = Name;
        currentname = savedname;
        saved = true;
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
                    selected.setBackground(bgcolor);
                    bgcolor = null;
                    selected = null;
                    builder.seteditenabled(false);
                }
            }
        });
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="gets">
    @Override
    public Container getParent() {
        Container parent = super.getParent();
        return parent;
    }

    public String geteqname() {
        return currentname;
    }

    public String getsavedeqname() {
        return savedname;
    }

    public String getversion() {
        return versionnumber;
    }

    public int getmode() {
        return mode;
    }

    public String getXMLpath() {
        return XMLPath;
    }
//</editor-fold>

    public void seteqname(String name) {
        currentname = name;
        if (XMLPath != null) {
            XMLPath = XMLPath.substring(0, XMLPath.lastIndexOf(",") + 1) + name;
        }
        Equation.setName(name);
    }

    public void setSave(Boolean save) {
        saved = save;
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

    private void loadequation() {
        try {
            loading = true;//this is used while the equation is being loaded so that
            //the print equation procedure isn't called for every term 
            Element currentequation = Equation.clone();
            ArrayList<String> parselist = new ArrayList();
            parselist.addAll(Global.getParseList());
            String equationtext = currentequation.getAttributeValue("value");
            if (equationtext != null && !equationtext.isEmpty()) {
                ArrayList<String> termlist = Global.separateformula(equationtext);
                Element TERMS = currentequation.getChild("TERMS");
                for (int x = 0; x < termlist.size(); x++) {
                    String currentterm = termlist.get(x);
                    if (parselist.contains(currentterm)) {//creates buttons for the mathematical symbols
                        //<editor-fold defaultstate="collapsed" desc="parselist">
                        /*ok this is a big if statement sequence but it is
                         * checking for the following things to make sure negative numbers
                         * happen properly
                         * A. is the minus sign the first character and is the next term numeric
                         * B. is it a minus sign followed by a number and preceded by another math symbol(x+-y vs x-y)
                         * c. is it the final character
                         */
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
//</editor-fold>
                    } else if (Global.isNumeric(currentterm)) {
                        addnumber(currentterm);

                    } else if (currentterm.startsWith("TXT:")) {
                        if (TERMS != null && TERMS.getChild(currentterm.substring(4)) != null) {
                            String value = TERMS.getChild(currentterm).getAttributeValue("value");
                            addtext(value);
                        }
                    } else if (currentterm.startsWith("DATE:")) {
                        if (TERMS != null && TERMS.getChild(currentterm.substring(5)) != null) {
                            String value = TERMS.getChild(currentterm).getAttributeValue("value");
                            adddate(java.sql.Date.valueOf(value));
                        }
                    } else if (currentterm.startsWith("FUNCTION:")) {
                        //<editor-fold defaultstate="collapsed" desc="Functions">
                        String[] currentfunction = currentterm.split(":");
                        EQButton newbutton;
                        switch (currentfunction[1]) {
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
                                addterm(newbutton);
                                break;
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
//</editor-fold>
                    } else if (currentterm.startsWith("CONSTANT:")) {
                        //<editor-fold defaultstate="collapsed" desc="Constants">
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
//</editor-fold>
                    } else //<editor-fold defaultstate="collapsed" desc="Else">
                    if (TERMS != null && TERMS.getChild(currentterm) != null) {
                        if (equationtext.contains("OP:" + currentterm)) {
                            Element opterm = TERMS.getChild(currentterm);
                            HashMap<String, String> opmap = Global.makecommandmap(opterm);

                            addop(opmap.get("OP"), TERMS.getChild(currentterm));
                        } else if (equationtext.contains("EVAL:" + currentterm) && TERMS.getChild(currentterm) != null) {
                            addop("EVAL", TERMS.getChild(currentterm));
                        } else {
                            EQButton newbutton = new EQButton(currentterm, currentterm, TERMS.getChild(currentterm).clone());
                            addterm(newbutton);
                        }
                    } //</editor-fold>
                }
            }
            builder.loadvalues(tablename, false);
            loading = false;
            printequation();

        } catch (Exception e) {
            Global.Printmessage("Load Equation EQPanel:" + e.getMessage());
        }
    }

    private void printequation() {
        try {
            oplist.clear();
            if (!loading) {
                this.removeAll();
                String label = "Path: " + Path + "   Name: " + currentname;
                JLabel NameLabel = new JLabel();
                NameLabel.setText(label);
                this.add(NameLabel, "wrap,span");

                JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
                separator.setBackground(Color.BLACK);
                separator.setForeground(Color.BLACK);
                this.add(separator, "wrap,span");//, width 98%");//strange expanding behaviour with the width at 99 or 100

                for (JComponent currentterm : terms) {
                    currentterm.setEnabled(true);
                    this.add(currentterm);
                    if (currentterm.getClass() == OPButton.class) {
                        OPButton temp = (OPButton) currentterm;
                        oplist.add(temp.getterm().getName());
                    }
                }

                this.setSize(this.getPreferredSize());//I don't know why this works but it does, it lets the panel expand with new controls
                if (this.getWidth() < 900) {
                    separator.setPreferredSize(new Dimension(900, separator.getHeight()));
                } else {
                    separator.setPreferredSize(new Dimension(this.getWidth(), separator.getHeight()));
                }
                this.repaint();
                this.revalidate();
            }
        } catch (Exception e) {
            Global.Printmessage("Eqpanel print Equation " + e.getClass() + ":" + e.getMessage());
        }
    }

    //<editor-fold defaultstate="collapsed" desc="save equation">
    @Override
    public Element Save() {
        saved = true;
        String Equationname = currentname;
        mode = EQPanel.existingequation;
        savedname = currentname;
        Equationname = Equationname.replace(" ", "_");
        Element equation = new Element(Equationname);
        equation.addContent(new Element("TERMS"));
        Element TERMS = equation.getChild("TERMS");
        int textcount = 1;
        int datecount = 1;
        String value = "";
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
                textcount++;
                text.setAttribute("value", tempbox.getText());
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
        equation.setAttribute("value", value);
        return equation;
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
            return true;
        }
        return false;
    }
//</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="addterm, addsymbol, addnumber ">
    @Override
    public void addsymbol(String symbol) {
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
        if (inserting) {
            terms.add(insertionpoint, currentbutton);
        } else {
            terms.add(currentbutton);
        }
        saved = false;
        printequation();
    }

    @Override
    public void addnumber(String number) {
        JFormattedTextField Number = new JFormattedTextField(NumberFormat.getInstance());
        Number.setText(number);
        Number.setColumns(5);
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
        if (inserting) {
            terms.add(insertionpoint, Number);
        } else {
            terms.add(Number);
        }
        saved = false;
        printequation();
    }

    @Override
    public void addnumber() {
        JFormattedTextField Number = new JFormattedTextField(NumberFormat.getInstance());
        Number.setText("0");
        Number.setColumns(5);

        Number.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                Point target = getMousePosition();
                JComponent source = (JComponent) evt.getSource();
                movecontrol(source, target);
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
        if (inserting) {
            terms.add(insertionpoint, Number);
        } else {
            terms.add(Number);
        }
        saved = false;
        printequation();
    }

    @Override
    public void addtext() {
        JTextField Text = new JTextField("", 20);
        if (inserting) {
            terms.add(insertionpoint, Text);
        } else {
            terms.add(Text);
        }

        Text.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                JComponent source = (JComponent) evt.getSource();
                Point target = getMousePosition();
                movecontrol(source, target);
                endinsert();
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {

                JTextField field = (JTextField) evt.getSource();
                if (selected != null) {
                    selected.setBackground(bgcolor);
                }
                bgcolor = field.getBackground();
                field.setBackground(Color.CYAN);
                selected = field;

                builder.seteditenabled(false);
            }
        });
        saved = false;
        printequation();
    }

    @Override
    public void addtext(String text) {
        JTextField Text = new JTextField(text, 20);

        if (inserting) {
            terms.add(insertionpoint, Text);
        } else {
            terms.add(Text);
        }
        Text.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                JComponent source = (JComponent) evt.getSource();
                Point target = getMousePosition();
                movecontrol(source, target);
                endinsert();
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {

                JTextField field = (JTextField) evt.getSource();
                if (selected != null) {
                    selected.setBackground(bgcolor);
                }
                bgcolor = field.getBackground();
                field.setBackground(Color.CYAN);
                selected = field;

                builder.seteditenabled(false);
            }
        });
        saved = false;
        printequation();
    }

    @Override
    public void addterm(EQButton currentbutton) {
        if (inserting) {
            terms.add(insertionpoint, currentbutton);
        } else {
            terms.add(currentbutton);
        }
        currentbutton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQButton button = (EQButton) evt.getSource();
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
        saved = false;
        printequation();
    }

    @Override
    public void addterm(OPButton currentbutton) {
        if (inserting) {
            terms.add(insertionpoint, currentbutton);
        } else {
            terms.add(currentbutton);
        }
        currentbutton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OPButton button = (OPButton) evt.getSource();
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
        saved = false;
        printequation();
    }

    @Override
    public void adddate(java.sql.Date date) {
        JFormattedTextField Datefield = new JFormattedTextField(new java.text.SimpleDateFormat(Global.getDateFormat()));
        Datefield.setText(date.toString());
        Datefield.setPreferredSize(new Dimension(100, Datefield.getHeight()));
        Datefield.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                JComponent source = (JComponent) evt.getSource();
                Point target = getMousePosition();
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
        if (inserting) {
            terms.add(insertionpoint, Datefield);
        } else {
            terms.add(Datefield);
        }
        saved = false;
        printequation();
    }

    @Override
    public void adddate() {
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
        if (inserting) {
            terms.add(insertionpoint, Datefield);
        } else {
            terms.add(Datefield);
        }
        saved = false;
        printequation();
    }

// </editor-fold>
   
    //<editor-fold defaultstate="collapsed" desc="Tab Change">
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
            } else if (commands.containsKey("EVAL")) {
                IFPanel oppanel = new IFPanel(term, builder);
                scpEq = new JScrollPane(oppanel);
                pnlEquationDisplay.add(scpEq);
                pnlEquationDisplay.repaint();
                pnlEquationDisplay.revalidate();
                builder.SetActivePanel(oppanel);
                term.setpanel(oppanel);
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
                tvaldialog.setTitle("Please Select Confidence Level");
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

    @Override
    public void loadparent() {
//this panel never actually has a parent panel but the interface requires this to be here
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="addop">
    @Override
    public void addop(String OP) {
        if (OP != null) {
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
                case "EVAL":
                    newterm = new BuilderTerm("EVAL:" + name, OP, this.Path + "," + name, XMLPath, name, tablename, tablename, this);//this is the basepanel class only used for the main equation so therefore it will have a different structure
                    addterm(newterm.createbutton());
                    IFPanel newpanel = new IFPanel(newterm, builder);
                    scpEq = new JScrollPane(newpanel);
                    pnlEquationDisplay.add(scpEq);
                    pnlEquationDisplay.repaint();
                    pnlEquationDisplay.revalidate();
                    builder.SetActivePanel(newpanel);
                    newterm.setpanel(newpanel);
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
                   if(tablechoice!=null){ newterm = new BuilderTerm("OP:" + name, OP, this.Path + "," + name, XMLPath, name, tablename, tablechoice, this);
                    addterm(newterm.createbutton());
                    OPPanel newoppanel = new OPPanel(newterm, builder);
                    newterm.setpanel(newoppanel);
                    scpEq = new JScrollPane(newoppanel);
                    pnlEquationDisplay.add(scpEq);
                    pnlEquationDisplay.repaint();
                    pnlEquationDisplay.revalidate();
                    newoppanel.setName(name);
                    builder.SetActivePanel(newoppanel);}
                    break;
            }
        }
    }

    @Override
    public void addop(String OP, Element Term) {
        if (OP != null) {
            HashMap<String, String> commandmap = Global.makecommandmap(Term.getAttributeValue("value"));
            saved = false;
            String name = Term.getName();
            BuilderTerm newterm;
            switch (OP) {
                case "IF":
                case "EVAL":
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
    public void loadvalues() {
        builder.loadvalues(tablename, false);
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

                terms.add(newfield);
                saved = false;
                printequation();
            } else if (selected.getClass().equals(JTextField.class)) {
                JTextField field = (JTextField) selected;
                addtext(field.getText());
            } else if (selected.getClass().equals(JButton.class)) {
                JButton button = (JButton) selected;
                this.addsymbol(button.getText());
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
    public void backspace() {
        if (terms.size() > 0) {
            terms.remove(terms.size() - 1);
            saved = false;
            printequation();
        }
    }

    @Override
    public void delete() {
        if (selected != null && terms.contains(selected)) {
            terms.remove(selected);
            bgcolor = null;
            selected = null;
            saved = false;
            builder.seteditenabled(false);
            printequation();
        }
    }

    @Override
    public void moveleft() {
        if (selected != null && terms.contains(selected)) {
            int index = terms.indexOf(selected);
            if (index > 0) {
                Collections.swap(terms, index, index - 1);
                saved = false;
                printequation();
            }
        }
    }

    @Override
    public void moveright() {
        if (selected != null && terms.contains(selected)) {
            int index = terms.indexOf(selected);
            if (index < terms.size() - 1) {
                Collections.swap(terms, index, index + 1);
                saved = false;
                printequation();
            }
        }

    }

    @Override
    public void endinsert() {
        inserting = false;
        insertionpoint = -1;
    }

    @Override
    public void dropControl() {
        Point target = this.getMousePosition();
        if (target != null) {
            inserting = true;
            if (terms.size() > 0) {
                JComponent targetcomponent = (JComponent) getComponentAt(target);
//if it was dropped on a component instead of the panel
                if (targetcomponent != null && terms.contains(targetcomponent)) {
                    double targetx = targetcomponent.getLocation().getX();
                    int index = terms.indexOf(targetcomponent);
                    double midpoint = targetx + targetcomponent.getWidth() / 2;
                    if (target.getX() > midpoint) {
                        insertionpoint = index + 1;
                    } else {
                        insertionpoint = index;
                    }
                } else {
                    JComponent firstcontrol = terms.get(0);
                    double top = firstcontrol.getLocation().getY() - 20;
                    double bottom = top + 40 + firstcontrol.getHeight();
                    if (top < 0) {
                        top = 0;
                    }
                    double targety = target.getY();
                    //if we're within a certain height of the equation line
                    if (targety >= top && targety <= bottom) {
                        double targetx = target.getX();
                        boolean found = false;
                        JComponent lastterm = terms.get(terms.size() - 1);
                        int farx = lastterm.getX() + lastterm.getWidth();
                        if (targetx > farx) {
                            found = true;
                            insertionpoint = terms.size();
                        } else {
                            for (JComponent currentterm : terms) {
                                if (targetx < currentterm.getLocation().getX()) {
                                    found = true;
                                    insertionpoint = terms.indexOf(currentterm);
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
            if (insertionpoint >= terms.size() || insertionpoint == -1) {
                terms.remove(control);
                terms.add(control);
            } else if (insertionpoint > terms.indexOf(control) + 1) {
                terms.remove(control);
                terms.add(insertionpoint - 1, control);
            } else if (insertionpoint < terms.indexOf(control)) {
                terms.remove(control);
                terms.add(insertionpoint, control);
            }
            printequation();
        }
        endinsert();
    }

    @Override
    public Point getmousedrop() {
        return this.getMousePosition();
    }
}