/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Prototype.WizardBuilding;

import Prototype.EquationBuilding.EQButton;
import Prototype.StaticClasses.Global;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import java.util.HashMap;
import net.miginfocom.swing.MigLayout;
import org.jdom2.Element;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.util.Collections;
import javax.swing.JScrollPane;

/**
 *
 * @author Gregory
 */
public class ConditionEditor extends javax.swing.JPanel {

    private ArrayList<String> values = new ArrayList();
    private JDialog parent;
    private Element equation;
    private ArrayList<JComponent> terms = new ArrayList();
    boolean loading;
    boolean complete = false;
    private boolean inserting = false;
    private int insertionpoint = -1;
    private HashMap<String, ArrayList<String>> tablecolumns = new HashMap();
    private String mode;
    private JPanel pnlcolumns;
    private JComponent selected = null;
    private JPanel EqPanel;
    private Color bgcolor;

    public ConditionEditor(JDialog Parent, ArrayList<String> Values) {
        this.initComponents();
        mode = "single";
        values = Values;
        parent = Parent;
        loadvalues();
        equation = new Element("CONDITION");
        EqPanel = new JPanel();
        EqPanel.setLayout(new MigLayout());
        EqPanel.addMouseListener(new MouseListener() {
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
                }
            }
        });
        EqPanel.setSize(EqPanel.getPreferredSize());//I don't know why this works but it does, it lets the panel expand with new controls
        pnlOuter.setLayout(new BorderLayout());
        pnlOuter.add(new JScrollPane(EqPanel), BorderLayout.CENTER);
        pnlOuter.repaint();
        pnlOuter.revalidate();
        loadequation();
        LoadControls();
    }

    public ConditionEditor(JDialog Parent, HashMap<String, ArrayList<String>> TableColumns) {
        this.initComponents();
        mode = "tables";
        tablecolumns = TableColumns;
        parent = Parent;
        this.loadvalues();
        equation = new Element("CONDITION");
        EqPanel = new JPanel();
        EqPanel.setLayout(new MigLayout());
        EqPanel.addMouseListener(new MouseListener() {
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
                }
            }
        });
        EqPanel.setSize(EqPanel.getPreferredSize());//I don't know why this works but it does, it lets the panel expand with new controls
        pnlOuter.setLayout(new BorderLayout());
        pnlOuter.add(new JScrollPane(EqPanel), BorderLayout.CENTER);
        pnlOuter.repaint();
        pnlOuter.revalidate();
        loadequation();
        LoadControls();
    }

    public ConditionEditor(JDialog Parent, ArrayList<String> Values, Element Equation) {
        this.initComponents();
        values = Values;
        parent = Parent;
        mode = "single";
        this.loadvalues();
        equation = Equation;
        EqPanel = new JPanel();
        EqPanel.setLayout(new MigLayout());
        EqPanel.addMouseListener(new MouseListener() {
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
                }
            }
        });
        EqPanel.setSize(EqPanel.getPreferredSize());//I don't know why this works but it does, it lets the panel expand with new controls
        pnlOuter.setLayout(new BorderLayout());
        pnlOuter.add(new JScrollPane(EqPanel), BorderLayout.CENTER);
        pnlOuter.repaint();
        pnlOuter.revalidate();;
        loadequation();
        LoadControls();
    }

    public ConditionEditor(JDialog Parent, HashMap<String, ArrayList<String>> TableColumns, Element Equation) {
        this.initComponents();
        tablecolumns = TableColumns;
        parent = Parent;
        mode = "tables";
        loadvalues();
        equation = Equation;
        EqPanel = new JPanel();
        EqPanel.setLayout(new MigLayout());
        EqPanel.addMouseListener(new MouseListener() {
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
                }
            }
        });
        EqPanel.setSize(EqPanel.getPreferredSize());//I don't know why this works but it does, it lets the panel expand with new controls
        pnlOuter.setLayout(new BorderLayout());
        pnlOuter.add(new JScrollPane(EqPanel), BorderLayout.CENTER);
        pnlOuter.repaint();
        pnlOuter.revalidate();
        loadequation();
        LoadControls();
    }

    private void LoadTermMenu() {

        JButton btnDelTerm = new JButton();
        btnDelTerm.setText("Delete");
        btnDelTerm.setToolTipText("Deletes the selected term.");
        btnDelTerm.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delete();
            }
        });
        pnlControls.add(btnDelTerm, "width 120, left");

        JButton btnBackSpace = new JButton();
        btnBackSpace.setText("Backspace");
        btnBackSpace.setToolTipText("Deletes the last term in the equation.");
        btnBackSpace.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backspace();
            }
        });
        pnlControls.add(btnBackSpace, "width 120, left");

        JButton btnLeft = new JButton();
        btnLeft.setText("Left");
        btnLeft.setToolTipText("Moves selected term to the left.");
        btnLeft.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveleft();
            }
        });
        pnlControls.add(btnLeft, "width 120, left");

        JButton btnRight = new JButton();
        btnRight.setText("Right");
        btnRight.setToolTipText("Moves selected term to the right.");
        btnRight.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveright();
            }
        });
        pnlControls.add(btnRight, "width 120, left");
        JButton btnOk = new JButton();
        btnOk.setText("Ok");
        btnOk.setToolTipText("Closes the window and saves the equation.");
        btnOk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ok();
            }
        });
        pnlControls.add(btnOk, "width 120, left");
        JButton btnCancel = new JButton();
        btnCancel.setText("Cancel");
        btnCancel.setToolTipText("Closes the window without saving changes.");
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                complete = false;
                parent.dispose();
            }
        });
        pnlControls.add(btnCancel, "width 120, left");
    }

    //<editor-fold defaultstate="collapsed" desc="loading">
    private void loadequation() {
        loading = true;//this is used while the equation is being loaded so that
        //the print equation procedure isn't called for every term 
        Element currentequation = equation.clone();
        ArrayList<String> parselist = new ArrayList();
        parselist.addAll(java.util.Arrays.asList("(", ")", "+", "-", "/", "*", "^", " ", "^", ",", "{", "}", "==", "<", ">", "!", "&&", "||"));
        String equationtext = currentequation.getAttributeValue("value");
        if (equationtext != null && !equationtext.isEmpty()) {
            String newformula = equationtext;//this code is used to get the terms where we need to hunt for data
            //this will strip out negative signs but as this is not where the math occurs and the values will
            //be subbed back into original equation it won't matter (-x becomes x, we search for x find x=3 sub
            //x back into original and get -3

            for (String str : parselist) {
                newformula = newformula.replace(str, "`" + str + "`");
            }

            String[] temp = newformula.split("`");//having removed all operations and text values we now get the terms needed to be found
            ArrayList<String> termlist = new ArrayList();
            for (String tempterm : temp) {
                if (tempterm != null && !tempterm.isEmpty()) {
                    termlist.add(tempterm);
                }
            }
            Element TERMS = currentequation.getChild("TERMS");
            for (int x = 0; x < termlist.size(); x++) {
                String currentterm = termlist.get(x);
                if (parselist.contains(currentterm)) {//creates buttons for the mathematical symbols
                    /*ok this is a big if statement sequence but it is
                     * checking for the following things to make sure negative numbers
                     * happen properly
                     * A. is the minus sign the first character and is the next term numeric
                     * B. is it a minus sign followed by a number and preceded by another math symbol(x+-y vs x-y)
                     * c. is it the final character
                     */
                    if (currentterm.equals("-")) {
                        if (x < termlist.size() - 1) {
                            String nextterm = termlist.get(x);
                            if (Global.isNumeric(nextterm)) {
                                if (x == 0 || parselist.contains(termlist.get(x - 1))) {
                                    nextterm = currentterm + nextterm;
                                    addnumber(nextterm);
                                    x++;//skip the next term as we just added it here
                                }
                            }
                        }
                    } else {
                        EQButton newbutton = new EQButton(currentterm, currentterm);
                        addterm(newbutton);
                    }
                } else if (Global.isNumeric(currentterm)) {
                    addnumber(currentterm);

                } else if (currentterm.startsWith("TXT:")) {
                    if (TERMS != null && TERMS.getChild(currentterm.substring(4)) != null) {
                        String value = TERMS.getChild(currentterm.substring(4)).getText();
                        addtext(value);
                    }
                } else if (currentterm.startsWith("DATE:")) {
                    
                    if (TERMS != null && TERMS.getChild(currentterm.substring(5)) != null) {
                        String value = TERMS.getChild(currentterm.substring(5)).getAttributeValue("value");
                        adddate(java.sql.Date.valueOf(value));
                    }
                } else if (values.contains(currentterm)) {

                        JButton newbutton = new JButton(currentterm);
                        addterm(newbutton);

                    }else {
                    String[] path = currentterm.split("\\.");
                    if(path.length>1&&tablecolumns.containsKey(path[0])&&TERMS!=null&&TERMS.getChild(currentterm)!=null)
                    {
                        
                            EQButton newbutton = new EQButton(currentterm, currentterm, TERMS.getChild(currentterm).clone());
                            addterm(newbutton);
                    }
                    
                }
                
            }
        }
        loading = false;
        printequation();
    }

    private void loadvalues() {
        switch (mode) {
            case "single":
                if (values.size() > 0) {
                    pnlValues.setLayout(new MigLayout("wrap 2, width 180, height 300"));
                }
                for (String Text : values) {
                    JButton newbutton = new JButton(Text);
                    newbutton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            JButton button = (JButton) evt.getSource();
                            String text = button.getText();
                            addterm(new JButton(text));
                        }
                    });
                    pnlValues.add(newbutton, "width 70");
                }
                break;
            case "tables":
                ArrayList<String> tables = new ArrayList(tablecolumns.keySet());
                JComboBox tablelist = new JComboBox(tables.toArray());
                pnlValues.setLayout(new MigLayout("wrap 2"));
                pnlValues.add(new JLabel("Tables:"));
                pnlValues.add(tablelist);
                pnlcolumns = new JPanel();
                pnlcolumns.setLayout(new MigLayout("wrap 1, width 180, height 300"));
                pnlValues.add(pnlcolumns, "span");
                tablelist.insertItemAt("", 0);
                tablelist.setSelectedIndex(0);

                tablelist.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        pnlcolumns.removeAll();
                        JComboBox tableselect = (JComboBox) e.getSource();
                        String selection = tableselect.getSelectedItem().toString();
                        if (!selection.equals("")) {
                            ArrayList<String> columns = tablecolumns.get(selection);
                            for (String column : columns) {
                                String value = ("TABLE:" + selection + ";VALUE:" + column);
                                EQButton newbutton = new EQButton(selection + "." + column, selection + "." + column, value);
                                newbutton.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                                        EQButton button = (EQButton) evt.getSource();
                                        addterm(button.Clone());
                                    }
                                });
                                pnlcolumns.add(newbutton, "width 175");
                            }
                            pnlcolumns.revalidate();
                            pnlcolumns.repaint();
                        }
                    }
                });
                break;
        }
        pnlValues.repaint();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Adding buttons">
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
            }
        });
        currentbutton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                Point target = EqPanel.getMousePosition();
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
        printequation();
    }

    public void addnumber(String number) {
        JFormattedTextField Number = new JFormattedTextField(NumberFormat.getInstance());
        Number.setText(number);
        Number.setColumns(5);
        Number.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JFormattedTextField field = (JFormattedTextField) evt.getSource();
                if (selected != null) {
                    selected.setBackground(bgcolor);
                }
                bgcolor = field.getBackground();
                field.setBackground(Color.CYAN);
                selected = field;
            }
        });
        Number.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                Point target = EqPanel.getMousePosition();
                JComponent source = (JComponent) evt.getSource();
                movecontrol(source, target);
                endinsert();
            }
        });
        if (inserting) {
            terms.add(insertionpoint, Number);
        } else {
            terms.add(Number);
        }
        printequation();

    }

    public void addnumber() {
        JFormattedTextField Number = new JFormattedTextField(NumberFormat.getInstance());
        Number.setText("0");
        Number.setColumns(5);
        Number.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JFormattedTextField field = (JFormattedTextField) evt.getSource();
                if (selected != null) {
                    selected.setBackground(bgcolor);
                }
                bgcolor = field.getBackground();
                field.setBackground(Color.CYAN);
                selected = field;
            }
        });
        Number.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                Point target = EqPanel.getMousePosition();
                JComponent source = (JComponent) evt.getSource();
                movecontrol(source, target);
                endinsert();
            }
        });
        if (inserting) {
            terms.add(insertionpoint, Number);
        } else {
            terms.add(Number);
        }
        printequation();
    }

    public void adddate(java.sql.Date date) {
        JFormattedTextField Datefield = new JFormattedTextField(new java.text.SimpleDateFormat(Global.getDateFormat()));
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

            }
        });
        Datefield.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                Point target = EqPanel.getMousePosition();
                JComponent source = (JComponent) evt.getSource();
                movecontrol(source, target);
                endinsert();
            }
        });
        if (inserting) {
            terms.add(insertionpoint, Datefield);
        } else {
            terms.add(Datefield);
        }
        printequation();
    }

    public void adddate() {
        JFormattedTextField Datefield = new JFormattedTextField(new java.text.SimpleDateFormat(Global.getDateFormat()));
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
            }
        });
        Datefield.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                Point target = EqPanel.getMousePosition();
                JComponent source = (JComponent) evt.getSource();
                movecontrol(source, target);
                endinsert();
            }
        });
        if (inserting) {
            terms.add(insertionpoint, Datefield);
        } else {
            terms.add(Datefield);
        }
        printequation();
    }

    public void addtext() {
        JTextField Text = new JTextField("", 13);
        if (inserting) {
            terms.add(insertionpoint, Text);
        } else {
            terms.add(Text);
        }
        Text.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JTextField field = (JTextField) evt.getSource();
                if (selected != null) {
                    selected.setBackground(bgcolor);
                }
                bgcolor = field.getBackground();
                field.setBackground(Color.CYAN);
                selected = field;
            }
        });
        Text.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                Point target = EqPanel.getMousePosition();
                JComponent source = (JComponent) evt.getSource();
                movecontrol(source, target);
                endinsert();
            }
        });
        printequation();
    }

    public void addtext(String text) {
        JTextField Text = new JTextField(text, 13);
        Text.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JTextField field = (JTextField) evt.getSource();
                if (selected != null) {
                    selected.setBackground(bgcolor);
                }
                bgcolor = field.getBackground();
                field.setBackground(Color.CYAN);
                selected = field;
            }
        });
        if (inserting) {
            terms.add(insertionpoint, Text);
        } else {
            terms.add(Text);
        }
        Text.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                Point target = EqPanel.getMousePosition();
                JComponent source = (JComponent) evt.getSource();
                movecontrol(source, target);
                endinsert();
            }
        });
        printequation();
    }

    public void addterm(JButton currentbutton) {
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
            }
        });
        currentbutton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                Point target = EqPanel.getMousePosition();
                JComponent source = (JComponent) evt.getSource();
                movecontrol(source, target);
                endinsert();
            }
        });
        printequation();
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Load Controls">
    private void LoadControls() {
        pnlControls.setLayout(new MigLayout("wrap 1"));
        LoadTermMenu();
        pnlBasic.setLayout(new MigLayout("wrap 4"));
        LoadBasic();
        pnlIf.setLayout(new MigLayout("wrap 2"));
        LoadIf();
        pnlConstants.setLayout(new MigLayout("wrap 2"));
        LoadConstants();
        pnlTrig.setLayout(new MigLayout("wrap 2"));
        LoadTrig();
    }

    private void LoadBasic() {
        String BasicAddString = "width 55, left, height 55";
        JButton btnMultiply = new JButton();
        btnMultiply.setText("*");
        btnMultiply.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addsymbol("*");
            }
        });
        AddMouseEvent(btnMultiply);
        pnlBasic.add(btnMultiply, BasicAddString);

        JButton btnDivide = new JButton();
        btnDivide.setText("/");
        btnDivide.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addsymbol("/");
            }
        });
        AddMouseEvent(btnDivide);
        pnlBasic.add(btnDivide, BasicAddString);

        JButton btnAddition = new JButton();
        btnAddition.setText("+");
        btnAddition.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addsymbol("+");
            }
        });
        AddMouseEvent(btnAddition);
        pnlBasic.add(btnAddition, BasicAddString);

        JButton btnSubtraction = new JButton();
        btnSubtraction.setText("-");
        btnSubtraction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addsymbol("-");
            }
        });
        AddMouseEvent(btnSubtraction);
        pnlBasic.add(btnSubtraction, BasicAddString);
        
        JButton btnLeftBR = new JButton();
        btnLeftBR.setText("(");
        btnLeftBR.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addsymbol("(");
            }
        });
        AddMouseEvent(btnLeftBR);
        pnlBasic.add(btnLeftBR, BasicAddString);

        JButton btnLn = new JButton();
        btnLn.setText("Ln");
        btnLn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQButton newbutton = new EQButton("Ln", "FUNCTION:LN");
                addterm(newbutton);
                addsymbol("(");
            }
        });
        AddMouseEvent(btnLn);
        pnlBasic.add(btnLn, BasicAddString);
        
        JButton btnLog = new JButton();
        btnLog.setText("Log");
        btnLog.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQButton newbutton = new EQButton("Log", "FUNCTION:LOG");
                addterm(newbutton);
                addsymbol("(");
            }
        });
        AddMouseEvent(btnLog);
        pnlBasic.add(btnLog, BasicAddString);

        JButton btnRightBR = new JButton();
        btnRightBR.setText(")");
        btnRightBR.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addsymbol(")");
            }
        });
        AddMouseEvent(btnRightBR);
        pnlBasic.add(btnRightBR, BasicAddString);

        
        
        JButton btnSquare = new JButton();
        btnSquare.setText("<html>X<sup>2</sup></html>");
        btnSquare.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addsymbol("^");
                addsymbol("(");
                addnumber("2");
                addsymbol(")");
            }
        });
        AddMouseEvent(btnSquare);
        pnlBasic.add(btnSquare, BasicAddString);

        JButton btnSQRoot = new JButton();
        btnSQRoot.setText("<html>&#8730</html>");
        btnSQRoot.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQButton newbutton = new EQButton("<html>&#8730</html>", "FUNCTION:SQRT");
                addterm(newbutton);
                addsymbol("(");
            }
        });
        AddMouseEvent(btnSQRoot);
        pnlBasic.add(btnSQRoot, BasicAddString);

        JButton btnxto3 = new JButton();
        btnxto3.setText("<html>X<sup>3</sup></html>");
        btnxto3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addsymbol("^");
                addsymbol("(");
                addnumber("3");
                addsymbol(")");
            }
        });
        AddMouseEvent(btnxto3);
        pnlBasic.add(btnxto3, BasicAddString);

        JButton btnCBRoot = new JButton();
        btnCBRoot.setText("<html><sup>3</sup>&#8730</html>");
        btnCBRoot.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQButton newbutton = new EQButton("<html><sup>3</sup>&#8730</html>" + "(", "FUNCTION:CBRT");
                addterm(newbutton);
            }
        });
        AddMouseEvent(btnCBRoot);
        pnlBasic.add(btnCBRoot, BasicAddString);

        JButton btnInvert = new JButton();
        btnInvert.setText("1/X");
        btnInvert.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addnumber("1");
                addsymbol("/");
                addsymbol("(");
            }
        });
        AddMouseEvent(btnInvert);
        pnlBasic.add(btnInvert, BasicAddString);

        JButton btnXtoY = new JButton();
        btnXtoY.setText("<html>x<sup>y</sup></html>");
        btnXtoY.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addsymbol("^");
                addsymbol("(");
            }
        });
        AddMouseEvent(btnXtoY);
        pnlBasic.add(btnXtoY, BasicAddString);

        JButton btnXRoot = new JButton();
        btnXRoot.setText("<html><sup>X</sup>&#8730</html>");
        btnXRoot.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQButton newbutton = new EQButton("<html><sup>X</sup>&#8730</html>" + "(", "FUNCTION:ROOT");
                addterm(newbutton);
            }
        });
        AddMouseEvent(btnXRoot);
        pnlBasic.add(btnXRoot, BasicAddString);

        JButton btn10X = new JButton();
        btn10X.setText("<html>10<sup>x</sup></html>");
        btn10X.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addnumber("10");
                addsymbol("^");
                addsymbol("(");
            }
        });
        AddMouseEvent(btn10X);
        pnlBasic.add(btn10X, BasicAddString);

        JButton btnEXP = new JButton();
        btnEXP.setText("EXP");
        btnEXP.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQButton newbutton = new EQButton("EXP", "FUNCTION:EXP");
                addterm(newbutton);
                addsymbol("(");
            }
        });
        AddMouseEvent(btnEXP);
        pnlBasic.add(btnEXP, "width 55,height 55, left");

        JButton btnNumber = new JButton();
        btnNumber.setText("Insert Number");
        btnNumber.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addnumber();
            }
        });
        AddMouseEvent(btnNumber);
        pnlBasic.add(btnNumber, "span 3,height 55, width 180, left");
    }

     private void LoadIf() {
        String IfAddString = "width 80, left";
        JButton btnAnd = new JButton();
        btnAnd.setText("AND");
        btnAnd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQButton newbutton = new EQButton("AND", "&amp;&amp;");
                addterm(newbutton);
            }
        });
        pnlIf.add(btnAnd, IfAddString);
        AddMouseEvent(btnAnd);

        JButton btnOr = new JButton();
        btnOr.setText("OR");
        btnOr.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQButton newbutton = new EQButton("OR", "||");
                addterm(newbutton);
            }
        });
        pnlIf.add(btnOr, IfAddString);
        AddMouseEvent(btnOr);

        JButton btnNot = new JButton();
        btnNot.setText("NOT");
        btnNot.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQButton newbutton = new EQButton("NOT", "!");
                addterm(newbutton);
            }
        });
        pnlIf.add(btnNot, IfAddString);
        AddMouseEvent(btnNot);

        JButton btnEqual = new JButton();
        btnEqual.setText("EQUALS");
        btnEqual.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQButton newbutton = new EQButton("==", "==");
                addterm(newbutton);
            }
        });
        pnlIf.add(btnEqual, IfAddString);
        AddMouseEvent(btnEqual);

        JButton btnLess = new JButton();
        btnLess.setText("<");
        btnLess.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQButton newbutton = new EQButton("<", "<");
                addterm(newbutton);
            }
        });
        pnlIf.add(btnLess, IfAddString);
        AddMouseEvent(btnLess);

        JButton btnGreater = new JButton();
        btnGreater.setText(">");
        btnGreater.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQButton newbutton = new EQButton(">", ">");
                addterm(newbutton);
            }
        });
        pnlIf.add(btnGreater, IfAddString);
        AddMouseEvent(btnGreater);

        JButton btnLessEq = new JButton();
        btnLessEq.setText("<=");
        btnLessEq.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQButton newbutton = new EQButton("<=", "<=");
                addterm(newbutton);
            }
        });
        pnlIf.add(btnLessEq, IfAddString);
        AddMouseEvent(btnLessEq);

        JButton btnGreaterEq = new JButton();
        btnGreaterEq.setText(">=");
        btnGreaterEq.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQButton newbutton = new EQButton(">=", ">=");
                addterm(newbutton);
            }
        });
        pnlIf.add(btnGreaterEq, IfAddString);
        AddMouseEvent(btnGreaterEq);

        JButton btnText = new JButton();
        btnText.setText("Text");
        btnText.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addtext();
            }
        });
        pnlIf.add(btnText, IfAddString);
        AddMouseEvent(btnText);

    }

    private void LoadConstants() {
        
        JButton btnLn2 = new JButton();
        btnLn2.setText("LN2");
        btnLn2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQButton newbutton = new EQButton("LN2", "CONSTANT:LN2");
                addterm(newbutton);
            }
        });
        AddMouseEvent(btnLn2);
        pnlConstants.add(btnLn2, "width 120, left");

        JButton btnLn10 = new JButton();
        btnLn10.setText("LN10");
        btnLn10.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQButton newbutton = new EQButton("LN10", "CONSTANT:LN10");
                addterm(newbutton);
            }
        });
        AddMouseEvent(btnLn10);
        pnlConstants.add(btnLn10, "width 120, left");

        JButton btnSqrt2 = new JButton();
        btnSqrt2.setText("<html>&#8730(2)</html>");
        btnSqrt2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQButton newbutton = new EQButton("<html>&#8730(2)</html>", "CONSTANT:SQRT2");
                addterm(newbutton);
            }
        });
        AddMouseEvent(btnSqrt2);
        pnlConstants.add(btnSqrt2, "width 120, left");

        JButton btnSqrtHalf = new JButton();
        btnSqrtHalf.setText("<html>&#8730(1/2)</html>");
        btnSqrtHalf.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQButton newbutton = new EQButton("<html>&#8730(1/2)</html>", "CONSTANT:SQRT1_2");
                addterm(newbutton);
            }
        });
        AddMouseEvent(btnSqrtHalf);
        pnlConstants.add(btnSqrtHalf, "width 120, left");

        JButton btnPi = new JButton();
        btnPi.setText("<html>&#960</html>");
        btnPi.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQButton newbutton = new EQButton("<html>&#960</html>", "CONSTANT:PI");
                //newbutton.setFont(source.getFont());
                addterm(newbutton);
            }
        });
        AddMouseEvent(btnPi);
        pnlConstants.add(btnPi, "width 120, left");

        JButton btnE = new JButton();
        btnE.setText("e");
        btnE.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQButton newbutton = new EQButton("e", "CONSTANT:E");
                addterm(newbutton);
            }
        });
        AddMouseEvent(btnE);
        pnlConstants.add(btnE, "width 120, left");

        JButton btnLog10e = new JButton();
        btnLog10e.setText("<html>LOG<sub>10</sub>E</html>");
        btnLog10e.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQButton newbutton = new EQButton("<html>LOG<sub>10</sub>E</html>", "CONSTANT:LOG10E");
                addterm(newbutton);
            }
        });
        AddMouseEvent(btnLog10e);
        pnlConstants.add(btnLog10e, "width 120, left, span 2");

    }

    private void LoadTrig() {
        String TrigAddString = "width 80, left";
        JButton btnCOS = new JButton();
        btnCOS.setText("COS");
        btnCOS.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQButton newbutton = new EQButton("COS", "FUNCTION:COS");
                addterm(newbutton);
                addsymbol("(");
            }
        });
        AddMouseEvent(btnCOS);
        pnlTrig.add(btnCOS, TrigAddString);

        JButton btnACOS = new JButton();
        btnACOS.setText("<html>COS<sup>-1</sup></html>");
        btnACOS.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQButton newbutton = new EQButton("<html>COS<sup>-1</sup></html>", "FUNCTION:ACOS");
                addterm(newbutton);
                addsymbol("(");
            }
        });
        AddMouseEvent(btnACOS);
        pnlTrig.add(btnACOS, TrigAddString);

        JButton btnSIN = new JButton();
        btnSIN.setText("SIN");
        btnSIN.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQButton newbutton = new EQButton("SIN", "FUNCTION:SIN");
                addterm(newbutton);
                addsymbol("(");
            }
        });
        AddMouseEvent(btnSIN);
        pnlTrig.add(btnSIN, TrigAddString);

        JButton btnASIN = new JButton();
        btnASIN.setText("<html>SIN<sup>-1</sup></html>");
        btnASIN.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQButton newbutton = new EQButton("<html>SIN<sup>-1</sup></html>", "FUNCTION:ASIN");
                addterm(newbutton);
                addsymbol("(");
            }
        });
        AddMouseEvent(btnASIN);
        pnlTrig.add(btnASIN, TrigAddString);

        JButton btnTAN = new JButton();
        btnTAN.setText("TAN");
        btnTAN.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQButton newbutton = new EQButton("TAN", "FUNCTION:TAN");
                addterm(newbutton);
                addsymbol("(");
            }
        });
        AddMouseEvent(btnTAN);
        pnlTrig.add(btnTAN, TrigAddString);

        JButton btnATAN = new JButton();
        btnATAN.setText("<html>TAN<sup>-1</sup></html>");
        btnATAN.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQButton newbutton = new EQButton("<html>TAN<sup>-1</sup></html>", "FUNCTION:ATAN");
                addterm(newbutton);
                addsymbol("(");
            }
        });
        AddMouseEvent(btnATAN);

        pnlTrig.add(btnATAN, TrigAddString);
    }

    private void AddMouseEvent(JButton button) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (EqPanel.getMousePosition() != null) {
                    dropControl();
                    JButton source = (JButton) evt.getSource();
                    source.doClick();
                    endinsert();
                }
            }
        });
    }
//</editor-fold>

    private void printequation() {
        if (!loading) {
            EqPanel.removeAll();
            for (JComponent currentterm : terms) {
                EqPanel.add(currentterm);
            }
            EqPanel.setSize(EqPanel.getPreferredSize());
            EqPanel.repaint();
            EqPanel.revalidate();
        }
    }

    public Element getcondition() {
        if (complete) {
            return equation;
        }
        return null;

    }

    public void endinsert() {
        inserting = false;
        insertionpoint = -1;
    }

    public void dropControl() {
        Point target = EqPanel.getMousePosition();
        if (target != null) {
            inserting = true;
            if (terms.size() > 0) {
                JComponent targetcomponent = (JComponent) EqPanel.getComponentAt(target);

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
if(target!=null)
{
        JComponent targetcomp = (JComponent) EqPanel.getComponentAt(target);
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
    }}

    private void ok() {
        equation = new Element("CONDITION");
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
                String formattedtext = textfield.getText();
                if (Global.isDate(formattedtext)) {
                    
                    value = value + "`" + "DATE:DATE" + datecount;
                    Element date = new Element("DATE" + datecount);
                    datecount++;
                    date.setAttribute("value", formattedtext);
                    TERMS.addContent(date);
                }else if(Global.isNumeric(formattedtext))
                {
                    value=value+"`"+formattedtext;
                }
            } else if (component.getClass() == JTextField.class) {
                JTextField tempbox = (JTextField) component;
                value = value + "`" + "TXT:TEXT" + textcount;
                Element text = new Element("TEXT" + textcount);
                text.addContent(tempbox.getText());
                TERMS.addContent(text);
            }

        }
        equation.setAttribute("value", value);
        complete = true;
        parent.dispose();
    }

    private void backspace() {
        if (terms.size() > 0) {
            terms.remove(terms.size() - 1);
            printequation();
        }
    }

    private void delete() {
        if (selected != null && terms.contains(selected)) {
            terms.remove(selected);
            bgcolor = null;
            selected = null;
            printequation();
        }
    }

    private void moveleft() {
        if (selected != null && terms.contains(selected)) {
            int index = terms.indexOf(selected);
            if (index > 0) {
                Collections.swap(terms, index, index - 1);
                printequation();
            }
        }
    }

    private void moveright() {
        if (selected != null && terms.contains(selected)) {
            int index = terms.indexOf(selected);
            if (index < terms.size() - 1) {
                Collections.swap(terms, index, index + 1);
                printequation();
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

        jTabbedPane1 = new javax.swing.JTabbedPane();
        pnlBasic = new javax.swing.JPanel();
        pnlTrig = new javax.swing.JPanel();
        pnlIf = new javax.swing.JPanel();
        pnlConstants = new javax.swing.JPanel();
        pnlControls = new javax.swing.JPanel();
        pnlOuter = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        pnlValues = new javax.swing.JPanel();

        setMaximumSize(new java.awt.Dimension(730, 550));
        setMinimumSize(new java.awt.Dimension(730, 550));
        setName(""); // NOI18N
        setPreferredSize(new java.awt.Dimension(730, 550));

        jTabbedPane1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout pnlBasicLayout = new javax.swing.GroupLayout(pnlBasic);
        pnlBasic.setLayout(pnlBasicLayout);
        pnlBasicLayout.setHorizontalGroup(
            pnlBasicLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 308, Short.MAX_VALUE)
        );
        pnlBasicLayout.setVerticalGroup(
            pnlBasicLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 321, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Basic", pnlBasic);

        javax.swing.GroupLayout pnlTrigLayout = new javax.swing.GroupLayout(pnlTrig);
        pnlTrig.setLayout(pnlTrigLayout);
        pnlTrigLayout.setHorizontalGroup(
            pnlTrigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 308, Short.MAX_VALUE)
        );
        pnlTrigLayout.setVerticalGroup(
            pnlTrigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 321, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Trig", pnlTrig);

        javax.swing.GroupLayout pnlIfLayout = new javax.swing.GroupLayout(pnlIf);
        pnlIf.setLayout(pnlIfLayout);
        pnlIfLayout.setHorizontalGroup(
            pnlIfLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 308, Short.MAX_VALUE)
        );
        pnlIfLayout.setVerticalGroup(
            pnlIfLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 321, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("IF", pnlIf);

        javax.swing.GroupLayout pnlConstantsLayout = new javax.swing.GroupLayout(pnlConstants);
        pnlConstants.setLayout(pnlConstantsLayout);
        pnlConstantsLayout.setHorizontalGroup(
            pnlConstantsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 308, Short.MAX_VALUE)
        );
        pnlConstantsLayout.setVerticalGroup(
            pnlConstantsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 321, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Constants", pnlConstants);

        pnlControls.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Controls", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));

        javax.swing.GroupLayout pnlControlsLayout = new javax.swing.GroupLayout(pnlControls);
        pnlControls.setLayout(pnlControlsLayout);
        pnlControlsLayout.setHorizontalGroup(
            pnlControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 183, Short.MAX_VALUE)
        );
        pnlControlsLayout.setVerticalGroup(
            pnlControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        pnlOuter.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        pnlOuter.setMaximumSize(new java.awt.Dimension(765, 177));
        pnlOuter.setMinimumSize(new java.awt.Dimension(765, 177));
        pnlOuter.setPreferredSize(new java.awt.Dimension(765, 177));

        javax.swing.GroupLayout pnlOuterLayout = new javax.swing.GroupLayout(pnlOuter);
        pnlOuter.setLayout(pnlOuterLayout);
        pnlOuterLayout.setHorizontalGroup(
            pnlOuterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        pnlOuterLayout.setVerticalGroup(
            pnlOuterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 175, Short.MAX_VALUE)
        );

        pnlValues.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Values", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        pnlValues.setMaximumSize(new java.awt.Dimension(261, 404));
        pnlValues.setMinimumSize(new java.awt.Dimension(261, 404));
        pnlValues.setName(""); // NOI18N

        javax.swing.GroupLayout pnlValuesLayout = new javax.swing.GroupLayout(pnlValues);
        pnlValues.setLayout(pnlValuesLayout);
        pnlValuesLayout.setHorizontalGroup(
            pnlValuesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        pnlValuesLayout.setVerticalGroup(
            pnlValuesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jScrollPane1.setViewportView(pnlValues);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(pnlOuter, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pnlControls, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 315, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(34, 34, 34)))
                .addGap(27, 27, 27))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnlOuter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jTabbedPane1)
                            .addComponent(pnlControls, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 16, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1)
                        .addContainerGap())))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPanel pnlBasic;
    private javax.swing.JPanel pnlConstants;
    private javax.swing.JPanel pnlControls;
    private javax.swing.JPanel pnlIf;
    private javax.swing.JPanel pnlOuter;
    private javax.swing.JPanel pnlTrig;
    private javax.swing.JPanel pnlValues;
    // End of variables declaration//GEN-END:variables

}
