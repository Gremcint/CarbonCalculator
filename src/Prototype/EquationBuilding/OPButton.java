/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Prototype.EquationBuilding;

import javax.swing.JButton;

public class OPButton extends JButton {

    private BuilderTerm ParentTerm;

    //<editor-fold defaultstate="collapsed" desc="Constructors">
    public OPButton(String text, BuilderTerm Parent) {
        super(text);
        this.ParentTerm = Parent;
    }
//</editor-fold>
    
    public BuilderTerm getterm() {
        return ParentTerm;
    } 
}