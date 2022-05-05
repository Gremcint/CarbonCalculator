/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Prototype.TableBuilding;

/*
 new table
 delete table
 rename table

 add column
 remove column
 move columne
 rename column
 change name column
 add link
 remove link
 change column type
 delete equation
 */
/**
 *
 * @author Gregory
 */
public class TableChange {

    private String action;
    private int column, row;
    private Object newvalue, oldvalue;

    public TableChange(String action, Object oldvalue, Object newvalue, int row, int col) {
        this.action = action;
        this.newvalue = newvalue;
        this.oldvalue = oldvalue;
        this.column = col;
        this.row = row;
    }

    public String getaction() {
        return action;
    }

    public int getrow() {
        return row;
    }

    public int getcol() {
        return column;
    }

    public Object getoldvalue() {
        return oldvalue;
    }

    public Object getnewvalue() {
        return newvalue;
    }
}
