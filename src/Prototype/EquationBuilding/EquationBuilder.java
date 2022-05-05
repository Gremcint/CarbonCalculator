/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Prototype.EquationBuilding;

import Prototype.StaticClasses.Global;
import Prototype.StaticClasses.XMLHandling;
import Prototype.StaticClasses.TableHandling;
import Prototype.Popups.NameForm;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.HeadlessException;
import java.util.ArrayList;
import java.util.HashMap;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JDialog;
import javax.swing.JScrollPane;

import org.jdom2.Element;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author User
 */
public class EquationBuilder extends javax.swing.JPanel implements Prototype.Main.SaveInterface {

    private ArrayList<JButton> ifList = new ArrayList();
    private boolean initialized = false;
    private EQPanel basepanel;
    private JButton btnEditOp;
    private String currentEqSaved;
    private String currentTable;
    private String currentVersion;
    private String Schema;
    private Element currentTableXML;
    private EQInterface activepanel;

    //<editor-fold defaultstate="collapsed" desc="Start Up">
    public EquationBuilder() {
        Schema = Global.getcurrentschema();
        initComponents();
    }

    public EQPanel getbase() {
        return basepanel;
    }

    private boolean ChooseEquation() {
        javax.swing.JDialog dialog = new JDialog();
        EquationSelect chooser = new EquationSelect(dialog);
        dialog.setTitle("Please select an Equation or Table.");
        dialog.add(chooser);
        dialog.pack();
        dialog.setModal(true);
        dialog.setVisible(true);

        String result = chooser.getresult();
        if (result.equals("CANCEL")) {
            return false;
        } else {
            currentTableXML = chooser.getTable();
            currentEqSaved = chooser.getequation();
            if (Global.getMode().equals(Global.TEMPLATEMODE)) {
                currentVersion = chooser.getVersion();
            } else {
                currentVersion = null;
            }
        }
        loadequation(currentTableXML, result, currentEqSaved, currentVersion);
        return true;
    }

    public void Startup() {
        try {
            for (JButton button : ifList) {
                button.setEnabled(false);
            }
            TableHandling.loadtables();
            LoadControls();
        } catch (Exception e) {
            Global.Printmessage("Equation Builder Startup " + e.getClass() + ":" + e.getMessage());
        }
        ChooseEquation();
    }

//</editor-fold>
    public EQInterface GetActivePanel() {
        return activepanel;
    }

    public JPanel getmainpanel() {
        return this.pnlEquationDisplay;
    }

    public void SetActivePanel(EQInterface panel) {
        activepanel = panel;
    }

    private void AddMouseEvent(JButton button) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (activepanel != null && activepanel.getmousedrop() != null) {
                    activepanel.dropControl();
                    JButton source = (JButton) evt.getSource();
                    source.doClick();
                    activepanel.endinsert();
                }
            }
        });
    }

    //<editor-fold defaultstate="collapsed" desc="Load Controls">
    private void LoadControls() {
        if (!initialized) {
            pnlEQMenu.setLayout(new MigLayout("wrap 1"));
            LoadEQMenu();
            LoadTermMenu();
            pnlBasic.setLayout(new MigLayout("wrap 4"));
            LoadBasic();
            pnlIf.setLayout(new MigLayout("wrap 2"));
            LoadIf();
            pnlConstants.setLayout(new MigLayout("wrap 1"));
            LoadConstants();
            pnlTrig.setLayout(new MigLayout("wrap 2"));
            LoadTrig();
            pnlAdvanced.setLayout(new MigLayout("wrap 2"));
            LoadAdvanced();
            pnlDates.setLayout(new MigLayout("wrap 2"));
            LoadDates();
            initialized = true;
        }
    }

    private void LoadEQMenu() {
        String eqmenustring = "width 140, left";
        JButton btnNewEq = new JButton();
        btnNewEq.setText("Change");
        btnNewEq.setToolTipText("Choose a different Equation or Version to load.");
        btnNewEq.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    if (basepanel == null || basepanel.CheckSave()) {
                        ChooseEquation();
                    } else {
                        int answer = JOptionPane.showConfirmDialog(null, "Would you like to save changes before switching equations?", "Save Changes", JOptionPane.YES_NO_CANCEL_OPTION);
                        if (answer != JOptionPane.CANCEL_OPTION) {
                            if (answer == JOptionPane.YES_OPTION) {
                                save();
                            }
                            ChooseEquation();
                        }
                    }
                } catch (Exception e) {
                    Global.Printmessage("Equation Builder choose equation button" + e.getClass() + ":" + e.getMessage());
                }
            }
        });
        pnlEQMenu.add(btnNewEq, eqmenustring);

        JButton btnRename = new JButton();
        btnRename.setText("Rename");
        btnRename.setToolTipText("Rename the current Equation, requires Save to be permanent.");
        btnRename.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    ArrayList<String> usednames = new ArrayList();
                    String currentname = basepanel.geteqname();
                    if (currentTableXML.getChild("EQUATIONS") != null) {
                        for (Element eq : currentTableXML.getChild("EQUATIONS").getChildren()) {
                            usednames.add(eq.getName());
                        }
                    }
                    if (currentTableXML.getChild("COLUMNS") != null) {
                        for (Element col : currentTableXML.getChild("COLUMNS").getChildren()) {
                            usednames.add(col.getName());
                        }
                    }

                    String neweq = geteqname(currentname, usednames, "Please enter a new name for the equation.");

                    if (neweq != null && !neweq.isEmpty()) {
                        basepanel.seteqname(neweq);
                    }
                } catch (Exception e) {
                    Global.Printmessage("Equation Builder rename button" + e.getClass() + ":" + e.getMessage());
                }
            }
        });
        pnlEQMenu.add(btnRename, eqmenustring);

        JButton btnReload = new JButton();
        btnReload.setText("Reload");
        btnReload.setToolTipText("Reloads the current equation from the most recent save.");
        btnReload.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                int panelmode = basepanel.getmode();
                String savedname = basepanel.getsavedeqname();
                String command = "OLD EQUATION";
                if (panelmode == EQPanel.existingequation) {
                    command = "OLD EQUATION";
                } else if (panelmode == EQPanel.newequation) {
                    command = "NEW EQUATION";
                } else if (panelmode == EQPanel.newversion) {
                    command = "NEW VERSION";
                }
                String version = basepanel.getversion();
                loadequation(currentTableXML, command, savedname, version);
            }
        });
        pnlEQMenu.add(btnReload, eqmenustring);

        JButton btnSave = new JButton();
        btnSave.setText("Save");
        btnSave.setToolTipText("Saves the current Equation.");
        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                save();
            }
        });
        pnlEQMenu.add(btnSave, eqmenustring);

        JButton btnDelete = new JButton();
        btnDelete.setText("Delete Equation");
        btnDelete.setToolTipText("Deletes the current Equation, this cannot be undone.");
        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (basepanel != null) {
                    int choice = JOptionPane.showConfirmDialog(null, "Are you sure you wish to delete this equation? This cannot be undone.", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                    if (choice == JOptionPane.YES_OPTION) {

                        String path;
                        if (Global.getMode().equals(Global.PROJECTMODE)) {
                            path = basepanel.getXMLpath();
                        } else {
                            path = "TEMPLATES," + Schema + ",TABLES," + currentTable + ",EQUATIONS," + currentEqSaved;
                        }
                        XMLHandling.deletepath(path.split(","), Global.getxmlfilename());
                        Clear();
                        ChooseEquation();
                    }
                }
            }
        });
        pnlEQMenu.add(btnDelete, eqmenustring);
        JButton btnDeletever = new JButton();
        btnDeletever.setText("Delete Version");
        btnDeletever.setToolTipText("Deletes the current Version, this cannot be undone.");
        btnDeletever.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (basepanel != null) {
                    int choice = JOptionPane.showConfirmDialog(null, "Are you sure you wish to delete this version? This cannot be undone.", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                    if (choice == JOptionPane.YES_OPTION) {

                        String path = basepanel.getXMLpath();
                        XMLHandling.deletepath(path.split(","), Global.getxmlfilename());
                        Clear();
                        ChooseEquation();
                    }
                }
            }
        });
        pnlEQMenu.add(btnDelete, eqmenustring);

    }

    private void LoadTermMenu() {
        JPanel TermMenu = new JPanel();
        TermMenu.setLayout(new MigLayout("wrap 1"));
        String termstring = "width 140, left";
        btnEditOp = new JButton();
        btnEditOp.setText("Edit");
        btnEditOp.setToolTipText("Edit the subequation of the selected term.");
        btnEditOp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQInterface current = GetActivePanel();
                current.editop();
            }
        });
        TermMenu.add(btnEditOp, termstring);

        JButton btnParent = new JButton();
        btnParent.setText("Parent");
        btnParent.setToolTipText("Takes you back up to the parent operation/equation if applicable.");
        btnParent.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQInterface current = GetActivePanel();
                current.loadparent();
            }
        });
        TermMenu.add(btnParent, termstring);

        JButton btnDelTerm = new JButton();
        btnDelTerm.setText("Delete");
        btnDelTerm.setToolTipText("Deletes the selected term.");
        btnDelTerm.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQInterface current = GetActivePanel();
                current.delete();
            }
        });
        TermMenu.add(btnDelTerm, termstring);

        JButton btnBackSpace = new JButton();
        btnBackSpace.setText("Backspace");
        btnBackSpace.setToolTipText("Deletes the last term in the equation.");
        btnBackSpace.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQInterface current = GetActivePanel();
                current.backspace();
            }
        });
        TermMenu.add(btnBackSpace, termstring);

        JButton btnLeft = new JButton();
        btnLeft.setText("Left");
        btnLeft.setToolTipText("Moves selected term to the left.");
        btnLeft.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQInterface current = GetActivePanel();
                current.moveleft();
            }
        });
        TermMenu.add(btnLeft, termstring);

        JButton btnRight = new JButton();
        btnRight.setText("Right");
        btnRight.setToolTipText("Moves selected term to the right.");
        btnRight.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQInterface current = GetActivePanel();
                current.moveright();
            }
        });
        TermMenu.add(btnRight, termstring);

        JButton btnDup = new JButton();
        btnDup.setText("Duplicate Term");
        btnDup.setToolTipText("Creates a copy of the selected term.");
        btnDup.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQInterface current = GetActivePanel();
                current.duplicate();
            }
        });
        TermMenu.add(btnDup, termstring);
        JScrollPane TermScroll = new JScrollPane(TermMenu);
        pnlTermMenu.setLayout(new BorderLayout());
        pnlTermMenu.add(TermScroll, BorderLayout.CENTER);
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
        ifList.clear();
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
        ifList.add(btnAnd);

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
        ifList.add(btnOr);

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
        ifList.add(btnNot);

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
        ifList.add(btnEqual);

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
        ifList.add(btnLess);

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
        ifList.add(btnGreater);

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
        ifList.add(btnLessEq);

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
        ifList.add(btnGreaterEq);

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
        ifList.add(btnText);

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

    private void LoadAdvanced() {
        String AdvancedAddString = "width 180, left";
        JButton btnCount = new JButton();
        btnCount.setText("COUNT()");
        btnCount.setToolTipText("Inserts an operation that counts the number of entries matching the given criteria.");
        btnCount.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addop("COUNT");
            }
        });
        AddMouseEvent(btnCount);
        pnlAdvanced.add(btnCount, AdvancedAddString);

        JButton btnTVal = new JButton();

        btnTVal.setText("T-VALUE()");
        btnTVal.setToolTipText("Inserts a function that gives the 2 sided TValue.");
        btnTVal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addop("TVAL");
            }
        });
        AddMouseEvent(btnTVal);
        pnlAdvanced.add(btnTVal, AdvancedAddString);

        JButton btnRndUp = new JButton();
        btnRndUp.setText("ROUND UP()");
        btnRndUp.setToolTipText("Will round up the value within the brackets.");
        btnRndUp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQButton newbutton = new EQButton("Round Up", "FUNCTION:ROUNDUP");
                addterm(newbutton);
                addsymbol("(");
            }
        });
        AddMouseEvent(btnRndUp);
        pnlAdvanced.add(btnRndUp, AdvancedAddString);

        JButton btnSum = new JButton();
        btnSum.setText("SUM()");
        btnSum.setToolTipText("Inserts an operation that calculates the total value of entries matching the given criteria.");
        btnSum.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addop("SUM");
            }
        });
        AddMouseEvent(btnSum);
        pnlAdvanced.add(btnSum, AdvancedAddString);

        JButton btnRndDown = new JButton();
        btnRndDown.setText("ROUND DOWN()");
        btnRndDown.setToolTipText("Will round down the value within the brackets.");
        btnRndDown.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQButton newbutton = new EQButton("Round Down", "FUNCTION:ROUNDDOWN");
                addterm(newbutton);
                addsymbol("(");
            }
        });
        AddMouseEvent(btnRndDown);
        pnlAdvanced.add(btnRndDown, AdvancedAddString);

        JButton btnMean = new JButton();
        btnMean.setText("MEAN()");
        btnMean.setToolTipText("Inserts an operation that calculates the mean value of entries matching the given criteria.");
        btnMean.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addop("MEAN");
            }
        });
        AddMouseEvent(btnMean);
        pnlAdvanced.add(btnMean, AdvancedAddString);

        JButton btnRndOff = new JButton();
        btnRndOff.setText("ROUND OFF()");
        btnRndOff.setToolTipText("Will round off the value within the brackets.");
        btnRndOff.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQButton newbutton = new EQButton("Round Off", "FUNCTION:ROUNDOFF");
                addterm(newbutton);
                addsymbol("(");
            }
        });
        AddMouseEvent(btnRndOff);
        pnlAdvanced.add(btnRndOff, AdvancedAddString);

        JButton btnIf = new JButton();
        btnIf.setText("IF()");
        btnIf.setToolTipText("Inserts an operation that lets you create an IF ELSE sequence allowing for dynamic equations basic on data.");
        btnIf.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addop("IF");
            }
        });
        AddMouseEvent(btnIf);
        pnlAdvanced.add(btnIf, AdvancedAddString);

        JButton btnAbs = new JButton();
        btnAbs.setText("ABSOLUTE()");
        btnAbs.setToolTipText("Returns the absolute value of the contents of the brackets.");
        btnAbs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQButton newbutton = new EQButton("Absolute", "FUNCTION:ABSOLUTE");
                addterm(newbutton);
                addsymbol("(");
            }
        });
        AddMouseEvent(btnAbs);
        pnlAdvanced.add(btnAbs, AdvancedAddString);

        JButton btnHighest = new JButton();
        btnHighest.setText("HIGHEST()");
        btnHighest.setToolTipText("Inserts an operation that produces the highest value of entries matching the given criteria.");
        btnHighest.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addop("HIGHEST");
            }
        });
        AddMouseEvent(btnHighest);
        pnlAdvanced.add(btnHighest, AdvancedAddString);

        JButton btnLowest = new JButton();
        btnLowest.setText("LOWEST()");
        btnLowest.setToolTipText("Inserts an operation that produces the lowest value of entries matching the given criteria.");
        btnLowest.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addop("LOWEST");
            }
        });
        AddMouseEvent(btnLowest);
        pnlAdvanced.add(btnLowest, AdvancedAddString);

        JButton btnCompare = new JButton();
        btnCompare.setText("PREVIOUS VALUE()");
        btnCompare.setToolTipText("Inserts an operation that will allow comparison between 2 successive values in the same column.");
        btnCompare.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addop("PREVIOUS");
            }
        });
        AddMouseEvent(btnCompare);
        pnlAdvanced.add(btnCompare, AdvancedAddString);

    }

    private void LoadDates() {
        String DateAddString = "width 180, left";

        JButton btnDate = new JButton();
        btnDate.setText("INSERT DATE");
        btnDate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                adddate();
            }
        });
        AddMouseEvent(btnDate);
        pnlDates.add(btnDate, DateAddString);

        JButton btnDayOfWeek = new JButton();
        btnDayOfWeek.setText("GET DAY OF WEEK()");
        btnDayOfWeek.setToolTipText("Adds a function to get the day value from a date.");
        btnDayOfWeek.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQButton newbutton = new EQButton("Get Day of Week", "FUNCTION:DAYOFWEEK");
                addterm(newbutton);
                addsymbol("(");
            }
        });
        AddMouseEvent(btnDayOfWeek);
        pnlDates.add(btnDayOfWeek, DateAddString);

        JButton btnGetMonth = new JButton();
        btnGetMonth.setText("GET MONTH()");
        btnGetMonth.setToolTipText("Adds a function to get the month value from a date.");
        btnGetMonth.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQButton newbutton = new EQButton("Get Month", "FUNCTION:MONTH");
                addterm(newbutton);
                addsymbol("(");
            }
        });
        AddMouseEvent(btnGetMonth);
        pnlDates.add(btnGetMonth, DateAddString);

        JButton btnDayOfMonth = new JButton();
        btnDayOfMonth.setText("GET DAY OF MONTH()");
        btnDayOfMonth.setToolTipText("Adds a function to get the day value from a date.");
        btnDayOfMonth.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQButton newbutton = new EQButton("Get Day of Month", "FUNCTION:DAYOFMONTH");
                addterm(newbutton);
                addsymbol("(");
            }
        });
        AddMouseEvent(btnDayOfMonth);
        pnlDates.add(btnDayOfMonth, DateAddString);

        JButton btnGetYear = new JButton();
        btnGetYear.setText("GET YEAR()");
        btnGetYear.setToolTipText("Adds a function to get the year value from a date.");
        btnGetYear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQButton newbutton = new EQButton("Get Year", "FUNCTION:YEAR");
                addterm(newbutton);
                addsymbol("(");
            }
        });
        AddMouseEvent(btnGetYear);
        pnlDates.add(btnGetYear, DateAddString);

        JButton btnDayOfYear = new JButton();
        btnDayOfYear.setText("GET DAY OF YEAR()");
        btnDayOfYear.setToolTipText("Adds a function to get the day value from a date.");
        btnDayOfYear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EQButton newbutton = new EQButton("Get Day of Year", "FUNCTION:DAYOFYEAR");
                addterm(newbutton);
                addsymbol("(");
            }
        });
        AddMouseEvent(btnDayOfYear);
        pnlDates.add(btnDayOfYear, DateAddString);

    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="loading and switching equations">
    private void loadequation(Element TableXML, String command, String neweqname, String version) {
        try {
            boolean loaded = false;
            String neweq = "";
            Clear();
            String tablename = TableXML.getName();

            switch (Global.getMode()) {
                case Global.PROJECTMODE:
                    // <editor-fold defaultstate="collapsed" desc=" Project ">
                    String path = "PROJECTS," + Schema + ",TABLES," + tablename + ",EQUATIONS,";
                    if (command.equals("NEW EQUATION")) {
                        //<editor-fold defaultstate="collapsed" desc="New Equation">
                        ArrayList<String> equationnames = new ArrayList();
                        if (TableXML.getChild("EQUATIONS") != null) {
                            for (Element currentchild : TableXML.getChild("EQUATIONS").getChildren()) {
                                equationnames.add(currentchild.getName());
                            }
                        }
                        int suffix = 1;
                        while (equationnames.contains("NEW_EQUATION" + suffix)) {
                            suffix++;
                        }

                        neweq = "NEW_EQUATION" + suffix;

                        neweq = geteqname(neweq, equationnames, "Please enter a name for the equation.");

                        if (neweq != null && !neweq.isEmpty()) {
                            basepanel = new EQPanel(neweq, path, this, tablename, path, EQPanel.newequation, "VERSION0");
                            loaded = true;
                        }
//</editor-fold>
                    } else {
                        //<editor-fold defaultstate="collapsed" desc="old equation">
                        path = path + neweqname;
                        Element equationxml = XMLHandling.getpath(new String[]{"EQUATIONS", neweqname}, TableXML);
                        basepanel = new EQPanel(equationxml, neweqname, path, this, tablename, path, EQPanel.existingequation, "VERSION0");
                        loaded = true;
                        neweq = equationxml.getName();
//</editor-fold>
                    }

// </editor-fold>
                    break;
                case Global.TEMPLATEMODE:
                    //<editor-fold defaultstate="collapsed" desc="Template">
                    switch (command) {
                        case "NEW EQUATION": {
                            String templatepath = "TEMPLATES," + Schema + ",TABLES," + tablename + ",";
                            ArrayList<String> equationnames = new ArrayList();
                            if (TableXML.getChild("EQUATIONS") != null) {
                                for (Element currentchild : TableXML.getChild("EQUATIONS").getChildren()) {
                                    equationnames.add(currentchild.getName());
                                }
                            }
                            int suffix = 1;
                            while (equationnames.contains("NEW_EQUATION" + suffix)) {
                                suffix++;
                            }
                            neweq = "NEW_EQUATION" + suffix;
                            neweq = geteqname(neweq, equationnames, "Please enter a name for the equation.");
                            if (neweq != null && !neweq.isEmpty()) {
                                basepanel = new EQPanel(neweq, templatepath + neweq + ",VERSION1", this, tablename, templatepath + "EQUATIONS,", EQPanel.newequation, "VERSION1");
                                loaded = true;
                            }
                            break;
                        }
                        case "NEW VERSION": {
                            neweq = neweqname;

                            String versionpath = "TEMPLATES," + Schema + ",TABLES," + tablename + "," + neweqname + ",";
                            ArrayList<String> versionnames = new ArrayList();
                            Element versions = XMLHandling.getpath(new String[]{"EQUATIONS", neweqname, "VERSIONS"}, TableXML);
                            if (versions != null) {
                                for (Element currentversion : versions.getChildren()) {
                                    versionnames.add(currentversion.getName());
                                }
                            }
                            int suffix = 1;
                            while (versionnames.contains("VERSION" + suffix)) {
                                suffix++;
                            }

                            basepanel = new EQPanel(neweq, versionpath + neweq + ",VERSION" + suffix, this, tablename, versionpath + "EQUATIONS,", EQPanel.newversion, "VERSION" + suffix);
                            loaded = true;
                            break;
                        }
                        default:
                            neweq = neweqname;
                            String existingpath = "TEMPLATES," + Schema + ",TABLES," + tablename + ",EQUATIONS," + neweqname + ",VERSIONS";
                            String fetchpath = "EQUATIONS," + neweqname + ",VERSIONS," + version;
                            String displaypath = tablename + "," + fetchpath;
                            Element equationxml = XMLHandling.getpath(fetchpath.split(","), TableXML);
                            basepanel = new EQPanel(equationxml, neweqname, displaypath, this, tablename, existingpath, EQPanel.existingequation, version);
                            loaded = true;
                            break;
                    }
//</editor-fold>
                    break;
            }
            if (loaded) {
                JScrollPane scpEq = new JScrollPane(basepanel);
                pnlEquationDisplay.setLayout(new BorderLayout());
                pnlEquationDisplay.add(scpEq, BorderLayout.CENTER);
                pnlEquationDisplay.repaint();
                pnlEquationDisplay.revalidate();
                SetActivePanel(basepanel);
                basepanel.setSave(true);
                currentEqSaved = neweq;
                if (version != null && Global.getMode().equals(Global.TEMPLATEMODE)) {
                    currentVersion = version;
                }

                currentTable = tablename;
                currentTableXML = TableXML;
            }
        } catch (Exception e) {
            Global.Printmessage("Equation Builder Load Equation " + e.getClass() + ":" + e.getMessage());
        }
    }

    public void loadvalues(String tablename, boolean ifmode) {
        try {
            for (JButton button : ifList) {
                button.setEnabled(ifmode);
            }
            TableTabs.removeAll();
            JPanel HomePanel = new JPanel();
            HomePanel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
            HashMap<String, ArrayList<String>> categories = TableHandling.getequationvalues(tablename);
            ArrayList<String> Names = new ArrayList();
            if (ifmode) {
                if (categories.containsKey("ALL")) {
                    Names.addAll(categories.get("ALL"));
                }

            } else {
                if (categories.containsKey("EQUATION")) {
                    Names.addAll(categories.get("EQUATION"));
                }
                if (categories.containsKey("NUMBER")) {
                    Names.addAll(categories.get("NUMBER"));
                }
                if (categories.containsKey("DATE")) {
                    Names.addAll(categories.get("DATE"));
                }
            }
            if (tablename.equals(currentTable)) {
                Names.remove(currentEqSaved);
            }
            HomePanel.setLayout(new MigLayout("wrap 1, width 180"));
            String Path = "TABLES," + tablename;
            for (String Name : Names) {
                String value = ("PATH:" + Path + ";VALUE:" + Name);
                EQButton newbutton = new EQButton(Name, Name, value);
                newbutton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        EQButton button = (EQButton) evt.getSource();
                        addterm(button.Clone());
                    }
                });
                HomePanel.add(newbutton, "width 200");
            }

            TableTabs.addTab(tablename, new JScrollPane(HomePanel));
            HomePanel.repaint();
            HashMap<String, String> links = TableHandling.getlinkedtables(tablename);

            for (String key : links.keySet()) {
                String panelname = links.get(key);
                JPanel newPanel = new JPanel();
                newPanel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
                String[] tempname = panelname.split(",");
                panelname = tempname[0];
           
                TableTabs.addTab(panelname, new JScrollPane(newPanel));
                HashMap<String, ArrayList<String>> LinkedCategories = TableHandling.getequationvalues(panelname);
                ArrayList<String> LinkedValues = new ArrayList();
                if (ifmode) {
                    if (LinkedCategories.containsKey("ALL")) {
                        LinkedValues.addAll(LinkedCategories.get("ALL"));
                    }
                    if (categories.containsKey("EQUATION")) {
                        LinkedValues.addAll(categories.get("EQUATION"));
                    }
                } else {
                    if (LinkedCategories.containsKey("EQUATION")) {
                        LinkedValues.addAll(LinkedCategories.get("EQUATION"));
                    }
                    if (LinkedCategories.containsKey("NUMBER")) {
                        LinkedValues.addAll(LinkedCategories.get("NUMBER"));
                    }
                    if (categories.containsKey("DATE")) {
                        LinkedValues.addAll(categories.get("DATE"));
                    }
                }
                newPanel.setLayout(new MigLayout("wrap 1, width 180"));
                for (String Text : LinkedValues) {

                    String panelpath = "TABLES," + panelname;
                    String textvalue = panelname + "." + Text;
                    String value = ("PATH:" + panelpath + ";VALUE:" + Text + ";MATCH:" + key);
                    EQButton newbutton = new EQButton(textvalue, textvalue, value);//**double check if button text loads weirdly
                    newbutton.addActionListener(new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            EQButton button = (EQButton) evt.getSource();
                            addterm(button.Clone());
                        }
                    });
                    newPanel.add(newbutton, "width 200");
                }
                newPanel.repaint();
            }
            TableTabs.repaint();
        } catch (Exception e) {
            Global.Printmessage("Equation Builder Load Values " + e.getClass() + ":" + e.getMessage());
        }
    }

    private String geteqname(String neweq, ArrayList<String> usednames, String message) {
        try {
            JDialog dialog = new JDialog();
            NameForm form = new NameForm(usednames, dialog, message, true, neweq);
            dialog.add(form);
            dialog.setTitle("Equation Name");
            dialog.pack();
            dialog.validate();
            dialog.setModal(true);
            dialog.setVisible(true);
            neweq = form.getresult();
            return neweq;
        } catch (Exception e) {
            Global.Printmessage("Equation Builder Geteqname " + e.getClass() + ":" + e.getMessage());
        }
        return null;
    }

    private void Clear() {
        pnlEquationDisplay.removeAll();
        basepanel = null;
        TableTabs.removeAll();
    }

    
    private void save() {
        try {
            int mode = basepanel.getmode();
            String savedname = basepanel.getsavedeqname();
            String currentname = basepanel.geteqname();
            Element equation = basepanel.Save();
            String pathstring = Global.getMode() + "S," + Schema + ",TABLES," + currentTable + ",EQUATIONS";
            String savedpath = pathstring + "," + savedname;
            if (mode != EQPanel.newequation && !currentname.equals(savedname)) {
                XMLHandling.changeElementName(savedpath.split(","), currentname, Global.getxmlfilename());
            }
            if (Global.getMode().equals(Global.TEMPLATEMODE)) {
                pathstring = pathstring + "," + currentname + ",VERSIONS";
                currentVersion = basepanel.getversion();
                equation.setName(currentVersion);
            }
            XMLHandling.addpath(pathstring.split(","), equation, Global.getxmlfilename(), true);
            TableHandling.incrementclock();
            JOptionPane.showMessageDialog(null, "Equation Saved.");

            currentEqSaved = currentname;
        } catch (HeadlessException e) {
            Global.Printmessage("Save Equation Equation Builder:" + e.getMessage());
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Adding buttons">
    private void addsymbol(String symbol) {

        EQInterface active = GetActivePanel();
        if (active != null) {
            active.addsymbol(symbol);
        }
    }

    private void adddate() {
        EQInterface active = GetActivePanel();
        if (active != null) {
            active.adddate();
        }
    }

    private void addnumber() {
        EQInterface active = GetActivePanel();
        if (active != null) {
            active.addnumber();
        }
    }

    private void addnumber(String number) {
        EQInterface active = GetActivePanel();
        if (active != null) {
            active.addnumber(number);

        }
    }

    private void addtext() {
        EQInterface active = GetActivePanel();
        if (active != null) {
            active.addtext();
        }
    }

    private void addterm(EQButton currentbutton) {
        EQInterface active = GetActivePanel();
        if (active != null) {
            active.addterm(currentbutton);
        }
    }

    public void addop(String Op) {
        EQInterface active = GetActivePanel();
        if (active != null) {
            active.addop(Op);
        }
    }
    //</editor-fold>

    public void seteditenabled(boolean setting) {
        btnEditOp.setEnabled(setting);
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
        jTabbedPane1 = new javax.swing.JTabbedPane();
        pnlEQMenu = new javax.swing.JPanel();
        pnlTermMenu = new javax.swing.JPanel();
        tabControls = new javax.swing.JTabbedPane();
        pnlBasic = new javax.swing.JPanel();
        pnlConstants = new javax.swing.JPanel();
        pnlTrig = new javax.swing.JPanel();
        pnlAdvanced = new javax.swing.JPanel();
        pnlIf = new javax.swing.JPanel();
        pnlDates = new javax.swing.JPanel();
        pnlEquationDisplay = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        TableTabs = new javax.swing.JTabbedPane();

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setPreferredSize(new java.awt.Dimension(995, 694));

        jTabbedPane1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jTabbedPane1.setMaximumSize(new java.awt.Dimension(138, 290));
        jTabbedPane1.setMinimumSize(new java.awt.Dimension(138, 290));
        jTabbedPane1.setPreferredSize(new java.awt.Dimension(138, 400));

        pnlEQMenu.setPreferredSize(new java.awt.Dimension(131, 240));

        javax.swing.GroupLayout pnlEQMenuLayout = new javax.swing.GroupLayout(pnlEQMenu);
        pnlEQMenu.setLayout(pnlEQMenuLayout);
        pnlEQMenuLayout.setHorizontalGroup(
            pnlEQMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 240, Short.MAX_VALUE)
        );
        pnlEQMenuLayout.setVerticalGroup(
            pnlEQMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 270, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Equation Controls", pnlEQMenu);

        pnlTermMenu.setPreferredSize(new java.awt.Dimension(131, 240));

        javax.swing.GroupLayout pnlTermMenuLayout = new javax.swing.GroupLayout(pnlTermMenu);
        pnlTermMenu.setLayout(pnlTermMenuLayout);
        pnlTermMenuLayout.setHorizontalGroup(
            pnlTermMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 240, Short.MAX_VALUE)
        );
        pnlTermMenuLayout.setVerticalGroup(
            pnlTermMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 270, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Term Controls", pnlTermMenu);

        tabControls.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        tabControls.setMaximumSize(new java.awt.Dimension(433, 400));
        tabControls.setMinimumSize(new java.awt.Dimension(433, 400));
        tabControls.setPreferredSize(new java.awt.Dimension(433, 400));

        pnlBasic.setPreferredSize(new java.awt.Dimension(150, 240));

        javax.swing.GroupLayout pnlBasicLayout = new javax.swing.GroupLayout(pnlBasic);
        pnlBasic.setLayout(pnlBasicLayout);
        pnlBasicLayout.setHorizontalGroup(
            pnlBasicLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 426, Short.MAX_VALUE)
        );
        pnlBasicLayout.setVerticalGroup(
            pnlBasicLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 251, Short.MAX_VALUE)
        );

        tabControls.addTab("Basic", pnlBasic);

        pnlConstants.setPreferredSize(new java.awt.Dimension(150, 240));

        javax.swing.GroupLayout pnlConstantsLayout = new javax.swing.GroupLayout(pnlConstants);
        pnlConstants.setLayout(pnlConstantsLayout);
        pnlConstantsLayout.setHorizontalGroup(
            pnlConstantsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 426, Short.MAX_VALUE)
        );
        pnlConstantsLayout.setVerticalGroup(
            pnlConstantsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 251, Short.MAX_VALUE)
        );

        tabControls.addTab("Constants", pnlConstants);

        pnlTrig.setPreferredSize(new java.awt.Dimension(150, 240));

        javax.swing.GroupLayout pnlTrigLayout = new javax.swing.GroupLayout(pnlTrig);
        pnlTrig.setLayout(pnlTrigLayout);
        pnlTrigLayout.setHorizontalGroup(
            pnlTrigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 426, Short.MAX_VALUE)
        );
        pnlTrigLayout.setVerticalGroup(
            pnlTrigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 251, Short.MAX_VALUE)
        );

        tabControls.addTab("Trigonometric", pnlTrig);

        pnlAdvanced.setPreferredSize(new java.awt.Dimension(180, 484));

        javax.swing.GroupLayout pnlAdvancedLayout = new javax.swing.GroupLayout(pnlAdvanced);
        pnlAdvanced.setLayout(pnlAdvancedLayout);
        pnlAdvancedLayout.setHorizontalGroup(
            pnlAdvancedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 426, Short.MAX_VALUE)
        );
        pnlAdvancedLayout.setVerticalGroup(
            pnlAdvancedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 251, Short.MAX_VALUE)
        );

        tabControls.addTab("Advanced", pnlAdvanced);

        pnlIf.setPreferredSize(new java.awt.Dimension(150, 240));

        javax.swing.GroupLayout pnlIfLayout = new javax.swing.GroupLayout(pnlIf);
        pnlIf.setLayout(pnlIfLayout);
        pnlIfLayout.setHorizontalGroup(
            pnlIfLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 426, Short.MAX_VALUE)
        );
        pnlIfLayout.setVerticalGroup(
            pnlIfLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 251, Short.MAX_VALUE)
        );

        tabControls.addTab("Comparators", pnlIf);

        javax.swing.GroupLayout pnlDatesLayout = new javax.swing.GroupLayout(pnlDates);
        pnlDates.setLayout(pnlDatesLayout);
        pnlDatesLayout.setHorizontalGroup(
            pnlDatesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 426, Short.MAX_VALUE)
        );
        pnlDatesLayout.setVerticalGroup(
            pnlDatesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 251, Short.MAX_VALUE)
        );

        tabControls.addTab("Dates", pnlDates);

        pnlEquationDisplay.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        pnlEquationDisplay.setMaximumSize(new java.awt.Dimension(972, 222));
        pnlEquationDisplay.setMinimumSize(new java.awt.Dimension(972, 222));
        pnlEquationDisplay.setPreferredSize(new java.awt.Dimension(972, 222));

        javax.swing.GroupLayout pnlEquationDisplayLayout = new javax.swing.GroupLayout(pnlEquationDisplay);
        pnlEquationDisplay.setLayout(pnlEquationDisplayLayout);
        pnlEquationDisplayLayout.setHorizontalGroup(
            pnlEquationDisplayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        pnlEquationDisplayLayout.setVerticalGroup(
            pnlEquationDisplayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        TableTabs.setMaximumSize(new java.awt.Dimension(227, 200));
        TableTabs.setMinimumSize(new java.awt.Dimension(227, 200));
        TableTabs.setName(""); // NOI18N
        TableTabs.setOpaque(true);
        TableTabs.setPreferredSize(new java.awt.Dimension(227, 200));
        jScrollPane1.setViewportView(TableTabs);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 247, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tabControls, javax.swing.GroupLayout.PREFERRED_SIZE, 433, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 274, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 17, Short.MAX_VALUE))
                    .addComponent(pnlEquationDisplay, javax.swing.GroupLayout.DEFAULT_SIZE, 983, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnlEquationDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(tabControls, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(176, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane TableTabs;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPanel pnlAdvanced;
    private javax.swing.JPanel pnlBasic;
    private javax.swing.JPanel pnlConstants;
    private javax.swing.JPanel pnlDates;
    private javax.swing.JPanel pnlEQMenu;
    private javax.swing.JPanel pnlEquationDisplay;
    private javax.swing.JPanel pnlIf;
    private javax.swing.JPanel pnlTermMenu;
    private javax.swing.JPanel pnlTrig;
    private javax.swing.JTabbedPane tabControls;
    // End of variables declaration//GEN-END:variables

    @Override
    public boolean IsSaved() {
        return basepanel == null || basepanel.CheckSave();
    }

    @Override
    public int SaveCheck() {
        int answer = JOptionPane.showConfirmDialog(null, "Would you like to save changes before exiting?", "Save Changes", JOptionPane.YES_NO_CANCEL_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            save();
        }
        return answer;
    }
}
