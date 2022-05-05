       /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Prototype.EquationBuilding;

import Prototype.StaticClasses.DateTextField;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;
import org.jdom2.Element;
import Prototype.StaticClasses.Global;
import java.awt.BorderLayout;
import java.awt.Point;
import javax.swing.JScrollPane;
import java.awt.Color;
import javax.swing.SwingConstants;

/**
 *
 * @author User
 */
public class IFPanel extends javax.swing.JPanel implements EQInterface {

    private ArrayList<ArrayList<JComponent>> results = new ArrayList();
    private ArrayList<ArrayList<JComponent>> criteria = new ArrayList();
    private ArrayList<JComponent> defaultresult = new ArrayList();
    private int rownumber;
    private BuilderTerm Parent;
    private EquationBuilder builder;
    private String Name;
    private String tablename;
    private String Path;//the location of the table that this function is based in
    private boolean EQSelected = true;
    private Element Equation;
    private Boolean Saved;
    private boolean switching = false;
    private boolean loading = false;
    private boolean elseselected = true;
    private JComponent selected;
    private boolean inserting = false;
    private int insertionpoint = -1;
    private ArrayList<JComponent> insertionlist;
    private JSeparator divider = new JSeparator(SwingConstants.HORIZONTAL);
    private Color bgcolor;
    ArrayList<String> oplist = new ArrayList();
    private JPanel outpanel1;

    //Initializes the panel settings getting the information needed from the parent;
    public IFPanel(BuilderTerm Source, EquationBuilder Builder) {
        super();
        initComponents();
        Path = Source.getEQPath();
        outpanel1 = new JPanel();
        rownumber = 0;
        Parent = Source;
        lstIF.requestFocus();
        builder = Builder;
        tablename = Source.gettable();

        Name = Source.getName();
        Equation = Source.getSavedXML();
        outpanel1.setLayout(new MigLayout());

        loadequation();
        Saved = true;
        JScrollPane scpEq = new JScrollPane(outpanel1);
        OuterPanel.setLayout(new java.awt.BorderLayout());
        scpEq.setSize(outpanel1.getWidth(), OuterPanel.getHeight());
        OuterPanel.add(scpEq, BorderLayout.CENTER);
        OuterPanel.repaint();
        OuterPanel.revalidate();
        //<editor-fold defaultstate="collapsed" desc="mouse event">
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
//</editor-fold>
    }

    //fills the listbox with the different if statement clauses
    private void filllist() {
        DefaultListModel model = new DefaultListModel();
        int[] index = lstIF.getSelectedIndices();
        for (ArrayList<JComponent> criteria1 : criteria) {
            String text = "IF: ";
            ArrayList<JComponent> curcrit = criteria1;
            for (int y = 0; y < curcrit.size() && y < 3; y++) {
                if (curcrit.get(y) instanceof JButton) {
                    JButton tempbutton = (JButton) curcrit.get(y);
                    text += tempbutton.getText();
                } else if (curcrit.get(y) instanceof JTextField) {
                    JTextField tempfield = (JTextField) curcrit.get(y);
                    text += tempfield.getText();
                }
            }
            text += " THEN: ";
            ArrayList<JComponent> curresult = criteria1;
            for (int y = 0; y < curresult.size() && y < 3; y++) {
                if (curresult.get(y) instanceof JButton) {
                    JButton tempbutton = (JButton) curresult.get(y);
                    text += tempbutton.getText();
                } else if (curresult.get(y) instanceof JTextField) {
                    JTextField tempfield = (JTextField) curresult.get(y);
                    text += tempfield.getText();
                }
            }
            model.addElement(text);
        }
        String strElse = "ELSE: ";
        for (int y = 0; y < defaultresult.size() && y < 3; y++) {
            if (defaultresult.get(y) instanceof JButton) {
                JButton tempbutton = (JButton) defaultresult.get(y);
                strElse += tempbutton.getText();
            } else if (defaultresult.get(y) instanceof JTextField) {
                JTextField tempfield = (JTextField) defaultresult.get(y);
                strElse += tempfield.getText();
            }
        }
        model.addElement(strElse);
        lstIF.setModel(model);
        lstIF.setSelectedIndices(index);
        lstIF.repaint();
    }

    // <editor-fold defaultstate="collapsed" desc="addterm, addsymbol, addnumber ">
    @Override
    public void addsymbol(String symbol) {
        Saved = false;
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
                Point target = outpanel1.getMousePosition();
                JComponent source = (JComponent) evt.getSource();
                movecontrol(source, target);
            }
        });
        insertcontrol(currentbutton);
        printequation();
    }

    @Override
    public void addnumber(String number) {
        Saved = false;
        JFormattedTextField Number = new JFormattedTextField(NumberFormat.getInstance());
        Number.setText(number);
        Number.setColumns(5);
        Number.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                Point target = outpanel1.getMousePosition();
                JComponent source = (JComponent) evt.getSource();
                movecontrol(source, target);
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
        insertcontrol(Number);
        printequation();
    }

    @Override
    public void addnumber() {
        Saved = false;
        JFormattedTextField Number = new JFormattedTextField(NumberFormat.getInstance());
        Number.setText("0");
        Number.setColumns(5);

        Number.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                Point target = outpanel1.getMousePosition();
                JComponent source = (JComponent) evt.getSource();
                movecontrol(source, target);
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
        insertcontrol(Number);
        printequation();
    }

    @Override
    public void addtext() {
        Saved = false;
        JTextField Text = new JTextField("", 20);

        Text.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                Point target = outpanel1.getMousePosition();
                JComponent source = (JComponent) evt.getSource();
                movecontrol(source, target);
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

    @Override
    public void addtext(String text) {
        Saved = false;
        JTextField Text = new JTextField(text, 20);

        Text.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                Point target = outpanel1.getMousePosition();
                JComponent source = (JComponent) evt.getSource();
                movecontrol(source, target);
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

    @Override
    public void addterm(EQButton currentbutton) {
        Saved = false;
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
                Point target = outpanel1.getMousePosition();
                JComponent source = (JComponent) evt.getSource();
                movecontrol(source, target);
            }
        });
        insertcontrol(currentbutton);
        printequation();
    }

    @Override
    public void addterm(OPButton currentbutton) {
        Saved = false;
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
                Point target = outpanel1.getMousePosition();
                JComponent source = (JComponent) evt.getSource();
                movecontrol(source, target);
            }
        });
        insertcontrol(currentbutton);
        printequation();
    }

    @Override
    public void adddate(java.sql.Date date) {
        DateTextField Datefield = new DateTextField();
        Datefield.setPreferredSize(new Dimension(100, Datefield.getHeight()));
        Datefield.setText(date.toString());

        Datefield.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                Point target = outpanel1.getMousePosition();
                JComponent source = (JComponent) evt.getSource();
                movecontrol(source, target);
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

        Saved = false;
        insertcontrol(Datefield);
        printequation();
    }

    @Override
    public void adddate() {
        DateTextField Datefield = new DateTextField();
        Datefield.setPreferredSize(new Dimension(100, Datefield.getHeight()));
        
        Datefield.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                Point target = outpanel1.getMousePosition();
                JComponent source = (JComponent) evt.getSource();
                movecontrol(source, target);
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
        Saved = false;
        printequation();
    }

    private void insertcontrol(JComponent currentcontrol) {
        if (inserting) {
            if (elseselected) {
                defaultresult.add(insertionpoint, currentcontrol);
            } else if (EQSelected) {
                criteria.get(rownumber).add(insertionpoint, currentcontrol);
            } else {
                results.get(rownumber).add(insertionpoint, currentcontrol);
            }
        } else {
            if (elseselected) {
                defaultresult.add(currentcontrol);
            } else if (EQSelected) {
                criteria.get(rownumber).add(currentcontrol);
            } else {
                results.get(rownumber).add(currentcontrol);
            }
        }
    }
// </editor-fold>

    //<editor-fold defaultstate="collapsed" desc="addop">
    @Override
    public void addop(String OP) {
        if (OP != null) {
            Saved = false;
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
                case "EVAL":
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
                    tvaldialog.pack();
                tvaldialog.setTitle("Please Select Confidence Level");

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

    @Override
    public void addop(String OP, Element Term) {
        if (OP != null) {
            Saved = false;
            HashMap<String, String> commandmap = Global.makecommandmap(Term.getAttributeValue("value"));

            String[] pathsplit = Path.split(",");
            String hometable = pathsplit[pathsplit.length - 1];
            String name = Term.getName();
            BuilderTerm newterm;
            switch (OP) {
                case "EVAL":
                case "IF":
                    newterm = new BuilderTerm("EVAL:" + name, Term, this.Path + "," + name, name, hometable, hometable, this);//this is the basepanel class only used for the main equation so therefore it will have a different structure
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

    private void reloadpanel() {
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
                tvaldialog.pack();
                tvaldialog.setTitle("Please Select Confidence Level");

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
//</editor-fold>

    private void loadequation() {
        loading = true;
        TreeMap<Integer, Element> Branches = new TreeMap();
        Element TERMS = Equation.getChild("TERMS");
        for (Element term : TERMS.getChildren()) {
            String name = term.getName();
            if (name.startsWith("COND") && Global.isInteger(name.substring(4))) {
                Integer number = Integer.parseInt(name.substring(4));
                if (!Branches.containsKey(number) && number >= 0) {
                    Branches.put(number, term);
                }
            }
        }

        criteria.clear();
        results.clear();
        elseselected = false;
        for (Integer key : Branches.keySet()) {
            Element currentrow = Branches.get(key);
            loadbranch(currentrow);
        }
        Element defaultvalue = TERMS.getChild("DEFAULT");
        elseselected = true;
        if (defaultvalue != null) {
            loadbranch(defaultvalue);
        }
        EQSelected = false;
        EQSelected = true;

        builder.loadvalues(tablename, true);
        loading = false;
        printequation();
        filllist();
    }

    @Override
    public Element Save() {
        Saved = true;
        Element equation = new Element(Name);
        equation.addContent(new Element("TERMS"));

        for (int x = 0; x < criteria.size(); x++) {
            int textcount = 1;
            int datecount = 1;
            Element currentif = new Element("COND" + x);
            currentif.addContent(new Element("TERMS"));
            equation.getChild("TERMS").addContent(currentif);
            ArrayList<JComponent> currentrow = criteria.get(x);
            String condition = "IF:";
            Element TERMS = currentif.getChild("TERMS");
            for (JComponent component : currentrow) {
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

            String value = ";RESULT:";
            ArrayList<JComponent> result = results.get(x);
            for (JComponent component : result) {
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
            currentif.setAttribute("value", condition + value);
        }
        Element elseresult = new Element("DEFAULT");
        int textcount = 1;
        int datecount = 1;
        elseresult.addContent(new Element("TERMS"));
        equation.getChild("TERMS").addContent(elseresult);

        String result = "RESULT:";
        Element TERMS = elseresult.getChild("TERMS");
        for (JComponent component : this.defaultresult) {
            if (component.getClass() == JButton.class) {
                JButton button = (JButton) component;
                result = result + "`" + button.getText();
            } else if (component.getClass() == EQButton.class) {
                EQButton tempbutton = (EQButton) component;
                result = result + "`" + tempbutton.getEqText();
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
                    result = result + "`" + "DATE:DATE" + datecount;
                    Element date = new Element("DATE" + datecount);
                    datecount++;
                    date.setAttribute("value", text);
                    TERMS.addContent(date);

                } else {
                    result = result + "`" + text;
                }
            } else if (component.getClass() == JTextField.class) {
                JTextField tempbox = (JTextField) component;
                result = result + "`" + "TXT:TEXT" + textcount;
                Element text = new Element("TEXT" + textcount);
                text.setAttribute("value", tempbox.getText());
                textcount++;
                TERMS.addContent(text);
            } else if (component.getClass() == OPButton.class) {
                OPButton current = (OPButton) component;
                BuilderTerm currentterm = current.getterm();
                result = result + "`" + current.getText();
                Element newelement = currentterm.saveXML().clone();
                if (TERMS.getChild(newelement.getName()) == null) {
                    TERMS.addContent(newelement);
                }
            }
        }
        elseresult.setAttribute("value", result);
        return equation;

    }

    private void loadbranch(Element branch) {
        HashMap<String, String> eqmap = Global.makecommandmap(branch.getAttributeValue("value"));
        EQSelected = true;
        if (!branch.getName().equals("DEFAULT")) {
            rownumber = criteria.size();
            criteria.add(new ArrayList<JComponent>());
            Element currentequation = branch.clone();
            String iftext = eqmap.get("IF");
            //<editor-fold defaultstate="collapsed" desc="IF">
            if (iftext != null && !iftext.isEmpty()) {
                ArrayList<String> termlist = Global.separateformula(iftext);
                Element Subterms = currentequation.getChild("TERMS");
                processterms(iftext, termlist, Subterms);

            }
//</editor-fold>
        }
        String resulttext = eqmap.get("RESULT");
        results.add(new ArrayList<JComponent>());
        EQSelected = false;
        //<editor-fold defaultstate="collapsed" desc="RESULT">
        if (resulttext != null && !resulttext.isEmpty()) {
            ArrayList<String> termlist = Global.separateformula(resulttext);

            Element Subterms = branch.getChild("TERMS");
            processterms(resulttext, termlist, Subterms);

        }
//</editor-fold>
    }

    private void printequation() {
        if (!loading) {
            outpanel1.removeAll();
            oplist.clear();
            if (rownumber > -1) {
                if (!elseselected) {
                    ButtonGroup group = new ButtonGroup();
                    JRadioButton rdoCriteria = new JRadioButton("IF: ");
                    rdoCriteria.addActionListener(new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            EQSelected = true;
                            builder.loadvalues(tablename, true);
                        }
                    });
                    JRadioButton rdoResult = new JRadioButton("Then: ");
                    rdoResult.addActionListener(new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            EQSelected = false;
                            builder.loadvalues(tablename, false);
                        }
                    });
                    group.add(rdoCriteria);
                    group.add(rdoResult);
                    outpanel1.add(rdoCriteria);

                    if (!criteria.isEmpty()) {
                        for (JComponent currentterm : criteria.get(rownumber)) {
                            currentterm.setEnabled(true);

                            outpanel1.add(currentterm,"split");
                            if (currentterm.getClass() == OPButton.class) {
                                OPButton temp = (OPButton) currentterm;
                                oplist.add(temp.getterm().getName());
                            }
                        }
                    }

                    divider = new JSeparator(SwingConstants.HORIZONTAL);
                    outpanel1.add(divider, "newline, wrap,span");
                    outpanel1.add(rdoResult);

                    if (!results.isEmpty()) {
                        for (JComponent currentterm : results.get(rownumber)) {
                            currentterm.setEnabled(true);

                            outpanel1.add(currentterm,"split");
                            if (currentterm.getClass() == OPButton.class) {
                                OPButton temp = (OPButton) currentterm;
                                oplist.add(temp.getterm().getName());
                            }
                        }
                    }
                    if (this.EQSelected) {
                        rdoCriteria.setSelected(true);
                        rdoResult.setSelected(false);
                    } else {
                        rdoCriteria.setSelected(false);
                        rdoResult.setSelected(true);
                    }
                } else {
                    JLabel newlabel = new JLabel("Else: ");
                    outpanel1.add(newlabel);

                    if (!defaultresult.isEmpty()) {

                        for (JComponent currentterm : defaultresult) {
                            currentterm.setEnabled(true);

                            outpanel1.add(currentterm,"split");

                        }

                    }
                }
            }
            this.setSize(this.getPreferredSize());//I don't know why this works but it does, it lets the panel expand with new controls
            if (this.getWidth() < 900) {
                divider.setPreferredSize(new Dimension(900, divider.getHeight()));
            } else {
                divider.setPreferredSize(new Dimension(this.getWidth(), divider.getHeight()));
            }
            this.repaint();
            this.revalidate();
            Saved = false;
        }
    }

    @Override
    public boolean CheckSave() {
        if (Saved) {
            for (ArrayList<JComponent> terms : this.criteria) {
                for (JComponent component : terms) {
                    if (component.getClass() == OPButton.class) {
                        OPButton current = (OPButton) component;
                        BuilderTerm currentterm = current.getterm();
                        if (currentterm.CheckSave() == false) {
                            return false;
                        }
                    }
                }
            }
            for (ArrayList<JComponent> terms : this.results) {
                for (JComponent component : terms) {
                    if (component.getClass() == OPButton.class) {
                        OPButton current = (OPButton) component;
                        BuilderTerm currentterm = current.getterm();
                        if (currentterm.CheckSave() == false) {
                            return false;
                        }
                    }
                }
            }
            for (JComponent component : this.defaultresult) {
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
        if (elseselected) {
            if (defaultresult.size() > 0) {
                defaultresult.remove(defaultresult.size() - 1);
            }
        } else if (EQSelected) {
            if (criteria.get(rownumber).size() > 0) {
                criteria.get(rownumber).remove(criteria.get(rownumber).size() - 1);
            }
        } else {
            if (results.get(rownumber).size() > 0) {
                results.get(rownumber).remove(results.get(rownumber).size() - 1);
            }
        }
        Saved = false;
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
                        Point target = outpanel1.getMousePosition();
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

                insertcontrol(newfield);
                Saved = false;
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

    @Override
    public void delete() {
        if (selected != null) {
            if (elseselected) {
                if (defaultresult.contains(selected)) {
                    defaultresult.remove(selected);
                    selected = null;
                    bgcolor = null;
                    Saved = false;
                    printequation();
                }
            } else {
                if (results.get(rownumber).contains(selected)) {
                    results.get(rownumber).remove(selected);
                    selected = null;
                    bgcolor = null;
                    Saved = false;
                    printequation();
                } else if (criteria.get(rownumber).contains(selected)) {
                    criteria.get(rownumber).remove(selected);
                    selected = null;
                    bgcolor = null;
                    Saved = false;
                    printequation();
                }
            }
            builder.seteditenabled(false);
        }
    }

    @Override
    public void moveleft() {
        if (selected != null) {
            if (elseselected) {
                if (defaultresult.contains(selected)) {
                    int index = defaultresult.indexOf(selected);
                    if (index > 1) {
                        Collections.swap(defaultresult, index, index - 1);
                        Saved = false;
                        printequation();
                    }
                }
            } else {
                if (results.get(rownumber).contains(selected)) {
                    int index = results.get(rownumber).indexOf(selected);
                    if (index > 1) {
                        Collections.swap(results.get(rownumber), index, index - 1);
                        Saved = false;
                        printequation();
                    }
                } else if (criteria.get(rownumber).contains(selected)) {
                    int index = criteria.get(rownumber).indexOf(selected);
                    if (index > 1) {
                        Collections.swap(criteria.get(rownumber), index, index - 1);
                        Saved = false;
                        printequation();
                    }
                }
            }
        }
    }

    @Override
    public void loadvalues() {
        builder.loadvalues(tablename, true);
    }

    @Override
    public void moveright() {
        if (selected != null) {
            if (elseselected) {
                if (defaultresult.contains(selected)) {
                    int index = defaultresult.indexOf(selected);
                    if (index < defaultresult.size() - 1) {
                        Collections.swap(defaultresult, index, index + 1);
                        Saved = false;
                        printequation();
                    }
                }
            } else {
                if (results.get(rownumber).contains(selected)) {
                    int index = results.get(rownumber).indexOf(selected);
                    if (index > results.get(rownumber).size() - 1) {
                        Collections.swap(results.get(rownumber), index, index + 1);
                        Saved = false;
                        printequation();
                    }
                } else if (criteria.get(rownumber).contains(selected)) {
                    int index = criteria.get(rownumber).indexOf(selected);
                    if (index > criteria.get(rownumber).size() - 1) {
                        Collections.swap(criteria.get(rownumber), index, index + 1);
                        Saved = false;
                        printequation();
                    }
                }
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnUp = new javax.swing.JButton();
        btnAdd = new javax.swing.JButton();
        btnDown = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstIF = new javax.swing.JList();
        OuterPanel = new javax.swing.JPanel();

        setPreferredSize(new java.awt.Dimension(971, 179));

        btnUp.setText("Move Up");
        btnUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpActionPerformed(evt);
            }
        });

        btnAdd.setText("Add");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnDown.setText("Move Down");
        btnDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDownActionPerformed(evt);
            }
        });

        btnDelete.setText("Delete");
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        jScrollPane1.setPreferredSize(new java.awt.Dimension(180, 170));

        lstIF.setPreferredSize(new java.awt.Dimension(180, 170));
        lstIF.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstIF_valueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(lstIF);

        javax.swing.GroupLayout OuterPanelLayout = new javax.swing.GroupLayout(OuterPanel);
        OuterPanel.setLayout(OuterPanelLayout);
        OuterPanelLayout.setHorizontalGroup(
            OuterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 636, Short.MAX_VALUE)
        );
        OuterPanelLayout.setVerticalGroup(
            OuterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnDown, javax.swing.GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE)
                            .addComponent(btnUp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(7, 7, 7))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(OuterPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(OuterPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnUp)
                        .addGap(5, 5, 5)
                        .addComponent(btnDown)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAdd)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDelete)
                        .addGap(60, 60, 60)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpActionPerformed
        int index = lstIF.getSelectedIndex();
        if (index > 0 && index < lstIF.getModel().getSize() - 1) {
            Collections.swap(criteria, index, index - 1);
            Collections.swap(results, index, index - 1);
            Saved = false;
            filllist();
        }
    }//GEN-LAST:event_btnUpActionPerformed

    private void btnDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDownActionPerformed
        int index = lstIF.getSelectedIndex();
        if (index < lstIF.getModel().getSize() - 2) {
            Collections.swap(criteria, index, index + 1);
            Collections.swap(results, index, index + 1);
            Saved = false;
            filllist();
        }
    }//GEN-LAST:event_btnDownActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        Element blank = new Element("COND" + (lstIF.getModel().getSize() - 2));
        blank.setAttribute("value", "IF: ;RESULT: ");
        criteria.add(new ArrayList<JComponent>());
        results.add(new ArrayList<JComponent>());
        Saved = false;
        filllist();
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        int index = lstIF.getSelectedIndex();
        if (index < lstIF.getModel().getSize() - 1) {
            criteria.remove(index);
            results.remove(index);
            Saved = false;
            filllist();
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void lstIF_valueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstIF_valueChanged
        if (!evt.getValueIsAdjusting()&&!switching) {
switching =true;
            rownumber = lstIF.getSelectedIndex();
            elseselected = rownumber == lstIF.getModel().getSize() - 1;
            printequation();
            if (lstIF.getSelectedIndex() > -1) {
                btnDown.setEnabled(true);
                btnUp.setEnabled(true);
                btnDelete.setEnabled(true);
            } else {
                btnDown.setEnabled(false);
                btnUp.setEnabled(false);
                btnDelete.setEnabled(false);
            }
            filllist();
            switching = false;
        }
    }//GEN-LAST:event_lstIF_valueChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel OuterPanel;
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnDown;
    private javax.swing.JButton btnUp;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList lstIF;
    // End of variables declaration//GEN-END:variables

    @Override
    public void endinsert() {
        inserting = false;
        insertionlist = null;
        insertionpoint = -1;
    }

    @Override
    public void dropControl() {
        Point target = outpanel1.getMousePosition();
        if (target != null) {
            inserting = true;
            if (this.elseselected) {
                insertionlist = defaultresult;
            } else {
                int dividingheight = divider.getY();
                if (target.getY() <= dividingheight) {
                    insertionlist = criteria.get(rownumber);
                } else {
                    insertionlist = results.get(rownumber);
                }
            }

            if (insertionlist.size() > 0) {
                JComponent targetcomponent = (JComponent) outpanel1.getComponentAt(target);

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
        JComponent targetcomp = (JComponent) outpanel1.getComponentAt(target);
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
        }
        endinsert();

    }

    @Override
    public Point getmousedrop() {
        return this.getMousePosition();
    }
}
