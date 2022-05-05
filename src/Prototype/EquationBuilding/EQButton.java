/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Prototype.EquationBuilding;

import org.jdom2.Element;
import javax.swing.JButton;

public class EQButton extends JButton {

    private String EqText;
    private Element value;

    //<editor-fold defaultstate="collapsed" desc="Constructors">   
    public EQButton(String text, String term, String Value) {
        super(text);
        EqText = term;
        value = new Element(EqText);
        value.setAttribute("value", Value);
    }

    public EQButton(String text, String term) {
        super(text);
        EqText = term;
    }

    public EQButton(String text, String term, Element Value) {
        super(text);
        EqText = term;
        value = Value;
    }
//</editor-fold>

    
    
    public EQButton Clone()  {
            EQButton newbutton = new EQButton(this.getText(), EqText, value);
            return newbutton;
    }

    //<editor-fold defaultstate="collapsed" desc="Gets">
    public Element getvalue() {
        return value;
    }
    public String getvaluetext()
    {
        if(value!=null)
        {
            return value.getAttributeValue("value");
        }
        return null;
    }

    public String getEqText() {
        return EqText;
    }
//</editor-fold>

    public boolean HasValue() {
        return value != null;
    }
}