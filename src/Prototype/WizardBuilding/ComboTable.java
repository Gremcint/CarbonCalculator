/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Prototype.WizardBuilding;

import javax.swing.JTable;
import javax.swing.JComboBox;
import java.util.HashMap;
import javax.swing.table.TableCellEditor;
import javax.swing.DefaultCellEditor;

/**
 *
 * @author Gregory
 */
public class ComboTable extends JTable {

    HashMap<String, DefaultCellEditor> dropdowns = new HashMap();
    public void adddropdown(int row, int col, JComboBox dropdown) {
        dropdowns.put(row+","+col, new DefaultCellEditor(dropdown));
    }
    public void removedropdown(int row, int col)
    {
        dropdowns.remove(row+","+col);
    }
    
    public void removealldropdowns()
    {
        dropdowns.clear();
    }
    
    
    @Override
    public TableCellEditor getCellEditor(int row, int col)
    {
        if(dropdowns.containsKey(row+","+col))
        {
            return dropdowns.get(row+","+col);
        }
        return super.getCellEditor(row,col);
    }
}
