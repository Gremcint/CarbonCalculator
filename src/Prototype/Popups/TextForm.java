/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Prototype.Popups;

import Prototype.StaticClasses.Global;
import Prototype.StaticClasses.TableHandling;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JComboBox;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.JCheckBox;
import javax.swing.JTextArea;
import net.miginfocom.swing.MigLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import javax.swing.JList;

/**
 *
 * @author Gregory
 */
public class TextForm {

    Integer namekeys = 0;
    JPanel panel;
    ArrayList<ArrayList<String>> fields;
    HashMap<String, ArrayList<String>> lists;
    HashMap<Integer, ArrayList<String>> namelists;
    HashMap inputs;
    String title;

    public TextForm(String Title) {
        panel = new JPanel(new MigLayout("Wrap 2"));
        fields = new ArrayList();
        inputs = new HashMap();
        namelists = new HashMap();
        title = Title;
    }

    public static String choosetabledialog(String Schema) {
        ArrayList<String> tables = TableHandling.getlistoftables(Schema);
        return showoptiondialog(tables, "Please Select a Table:");
    }

    public static String showoptiondialog(ArrayList<String> options, String Title) {
        try {
            JPanel optionpanel = new JPanel();

            optionpanel.setLayout(new MigLayout("wrap 5"));
            ButtonGroup buttongroup = new ButtonGroup();
            ArrayList<JRadioButton> buttons = new ArrayList();
            for (int x = 0; x < options.size(); x++) {
                JRadioButton currentbutton = new JRadioButton(options.get(x));
                if (x == 0) {
                    currentbutton.setSelected(true);
                }
                buttongroup.add(currentbutton);
                optionpanel.add(currentbutton);
                buttons.add(currentbutton);
            }

           int result = JOptionPane.showOptionDialog(null, optionpanel, Title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.OK_CANCEL_OPTION, null, null, null);

           if(result==JOptionPane.OK_OPTION)
           {
            String selected = "";
            for (JRadioButton currentbutton : buttons) {
                if (currentbutton.isSelected()) {
                    selected = currentbutton.getText();
                }
            }
            return selected;
           }
           return null;
        } catch (Exception e) {
            Global.Printmessage("Failed to display option dialog");
            Global.Printmessage(Title);
            Global.Printmessage(e.getMessage());
            return null;
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Adds">
    public void addlabel(String text) {
        ArrayList<String> newfield = new ArrayList();
        newfield.add("Label");
        newfield.add(text);
        fields.add(newfield);
    }

    public void addlist(String Label, ArrayList<String> lines) {
        String listname = "list" + (lists.size());
        ArrayList<String> newfield = new ArrayList();
        newfield.add("List");
        newfield.add(Label);
        newfield.add(listname);
        fields.add(newfield);
        lists.put(listname, lines);
    }

    public void addfield(String text, String defaultvalue) {
        ArrayList<String> newfield = new ArrayList();
        newfield.add("TextField");
        newfield.add(text);
        if (defaultvalue != null) {
            newfield.add(defaultvalue);
        } else {
            newfield.add("");
        }
        fields.add(newfield);
    }

    public void addnamefield(String text, String defaultvalue, ArrayList<String> Names) {
        ArrayList<String> newfield = new ArrayList();
        newfield.add("NameField");
        newfield.add(text);

        if (defaultvalue != null) {
            newfield.add(defaultvalue);
        } else {
            newfield.add("");
        }
        newfield.add(namekeys.toString());
        namelists.put(namekeys, Names);
        namekeys++;
        fields.add(newfield);
    }

    public void addmultilinefield(String text, String defaultvalue) {
        ArrayList<String> newfield = new ArrayList();
        newfield.add("TextArea");
        newfield.add(text);
        if (defaultvalue != null) {
            newfield.add(defaultvalue);
        } else {
            newfield.add("");
        }
        fields.add(newfield);
    }

    public void addDropDown(String Text, ArrayList<String> options) {
        ArrayList<String> newfield = new ArrayList();
        newfield.add(Text);
        newfield.add("ComboBox");
        newfield.addAll(options);
        fields.add(newfield);
    }

    public void addOptionList(String Text, ArrayList<String> options, Boolean multiselect) {
        ArrayList<String> newfield = new ArrayList();

        if (multiselect) {
            newfield.add("CheckBoxes");
        } else {
            newfield.add("RadioButtons");
        }
        newfield.add(Text);
        newfield.addAll(options);
        fields.add(newfield);
    }
    //</editor-fold>

    public ArrayList<ArrayList<String>> show() {
        panel = new JPanel(new MigLayout("Wrap 2"));
        int inputctr = 0;
        boolean setfocus = false;
        for (ArrayList<String> field : fields) {
            JLabel label = new JLabel("<html>" + field.get(1) + "<html>");
            switch (field.get(0)) {
                case "NameField":
                    JTextField txtName = new JTextField(field.get(2));
                    txtName.setPreferredSize(new Dimension(100, 22));
                    if(!setfocus)
                    {
                        txtName.requestFocus();
                    }
                    panel.add(label);
                    panel.add(txtName);
                    inputs.put(inputctr, txtName);
                    txtName.addKeyListener(new KeyListener() {
                        @Override
                        public void keyTyped(KeyEvent evt) {
                            char input = evt.getKeyChar();
                            JTextField txtName = (JTextField) evt.getSource();
                            String text = txtName.getText();
                            if ((!Character.isAlphabetic(input) && !Character.isDigit(input) && (input != '_')) || (text.length() == 0 && Character.isDigit(input)) || (text.length() == 0 && (input == '_'))) {
                                evt.consume();
                            } else {
                                evt.setKeyChar(Character.toUpperCase(input));
                            }
                        }

                        @Override
                        public void keyReleased(KeyEvent e) {
                        }

                        @Override
                        public void keyPressed(KeyEvent e) {
                        }
                    });
                    break;

                case "Label":
                    panel.add(label, "span");
                    break;
                case "TextField":
                    JTextField textfield = new JTextField(field.get(2));
                    textfield.setPreferredSize(new Dimension(100, 22));
                    if(!setfocus)
                    {
                        textfield.requestFocus();
                    }
                    panel.add(label);
                    panel.add(textfield);
                    inputs.put(inputctr, textfield);
                    break;
                case "List":
                    String listname = field.get(2);
                    ArrayList<String> list = lists.get(listname);
                    JList listbox = new JList(list.toArray());
                    panel.add(label, "span");
                    panel.add(listbox, "span");
                    break;
                case "TextArea":
                    JTextArea textarea = new JTextArea(field.get(2));
                    if(!setfocus)
                    {
                        textarea.requestFocus();
                    }
                    panel.add(label, "span");
                    panel.add(textarea, "span");
                    textarea.setPreferredSize(new Dimension(200, 600));
                    inputs.put(inputctr, textarea);
                    break;
                case "ComboBox":
                    ArrayList<String> combooptions = (ArrayList<String>) field.clone();
                    combooptions.remove(0);
                    combooptions.remove(0);
                    JComboBox combobox = new JComboBox(combooptions.toArray());
                    panel.add(label);
                    panel.add(combobox);
                    break;
                case "CheckBoxes":
                    ArrayList<String> checkoptions = (ArrayList<String>) field.clone();
                    checkoptions.remove(0);
                    checkoptions.remove(0);
                    JScrollPane scrollpane = new JScrollPane();
                    MigLayout layout = new MigLayout("wrap 2");
                    scrollpane.setLayout(layout);
                    ArrayList<JCheckBox> boxes = new ArrayList();
                    panel.add(label);
                    panel.add(scrollpane, "newline span 2 3");
                    for (String option : checkoptions) {
                        JCheckBox newbox = new JCheckBox(option);
                        scrollpane.add(newbox);
                        boxes.add(newbox);
                    }
                    inputs.put(inputctr, boxes);
                    break;
                case "RadioButtons":
                    ArrayList<String> radiooptions = (ArrayList<String>) field.clone();
                    radiooptions.remove(0);
                    radiooptions.remove(0);
                    ButtonGroup group = new ButtonGroup();
                    JScrollPane scrollpane2 = new JScrollPane();
                    MigLayout layout2 = new MigLayout("wrap 2");
                    scrollpane2.setLayout(layout2);
                    panel.add(label);
                    panel.add(scrollpane2, "newline span 2 3");
                    for (String option : radiooptions) {
                        JRadioButton button = new JRadioButton(option);
                        button.setActionCommand(option);
                        scrollpane2.add(button);
                        group.add(button);
                    }

                    inputs.put(inputctr, group);
                    break;
            }
            inputctr++;
        }
        panel.setPreferredSize(panel.getPreferredSize());
        int choice = JOptionPane.showConfirmDialog(null, panel, title,
                JOptionPane.OK_CANCEL_OPTION);
        if (choice == JOptionPane.OK_OPTION) {
            ArrayList<ArrayList<String>> results = new ArrayList();
            for (Object number : inputs.keySet()) {
                int key = (int) number;
                Object field = inputs.get(key);
                if (field instanceof JTextField) {
                    JTextField current = (JTextField) field;

                    fields.get(key).set(2, current.getText());
                    results.add(fields.get(key));
                } else if (field instanceof JComboBox) {
                    JComboBox current = (JComboBox) field;
                    String type = fields.get(key).get(0);
                    String text = fields.get(key).get(1);
                    fields.get(key).clear();
                    fields.get(key).add(type);
                    fields.get(key).add(text);
                    fields.get(key).add(current.getSelectedItem().toString());
                    results.add(fields.get(key));

                } else if (field instanceof ButtonGroup) {
                    ButtonGroup current = (ButtonGroup) field;
                    String type = fields.get(key).get(0);
                    String text = fields.get(key).get(1);
                    fields.get(key).clear();
                    fields.get(key).add(type);
                    fields.get(key).add(text);
                    fields.get(key).add(current.getSelection().getActionCommand());
                    results.add(fields.get(key));

                } else if (field instanceof ArrayList) {
                    ArrayList<JCheckBox> current = (ArrayList<JCheckBox>) field;
                    String type = fields.get(key).get(0);
                    String text = fields.get(key).get(1);
                    fields.get(key).clear();
                    fields.get(key).add(type);
                    fields.get(key).add(text);
                    for (JCheckBox box : current) {
                        if (box.isSelected()) {
                            fields.get(key).add(box.getText());
                        }
                    }
                    results.add(fields.get(key));

                } else if (field instanceof JTextArea) {
                    JTextArea current = (JTextArea) field;
                    fields.get(key).set(2, current.getText());
                    results.add(fields.get(key));

                }
            }
            return results;
        } else {
            return null;
        }
    }
}
