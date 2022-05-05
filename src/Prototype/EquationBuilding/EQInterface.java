/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Prototype.EquationBuilding;

import org.jdom2.Element;
import java.awt.Container;
import java.awt.Point;

/**
 *
 * @author Gregory
 */
public interface EQInterface {

    public void addsymbol(String symbol);

    public void addnumber(String number);

    public void addnumber();

    public void addtext();

    public void addtext(String text);

    public void addterm(EQButton currentbutton);

    public void addterm(OPButton currentbutton);

    public void addop(String OP);

    public void addop(String OP, Element Term);

    public void adddate(java.sql.Date date);
    
    public void adddate();
    
    public Element Save();

    public boolean CheckSave();

    public Container getParent();

    public void moveleft();

    public void moveright();

    public void delete();

    public void backspace();

    public void editop();
    
    public void loadparent();
 
    public void dropControl();
    
    public void endinsert();
    
    public Point getmousedrop();
    
    public void duplicate();
    
    public void loadvalues();
}
