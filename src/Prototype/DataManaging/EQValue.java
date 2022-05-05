package Prototype.DataManaging;

import Prototype.StaticClasses.StatusList;
import java.sql.Date;
/**
 * This class is used to store data for working with equation values.
 * It has 4 parts, the value, the type of data (date, string, number)
 * a Boolean value to say if the equation was successful or not and
 * an error message if unsuccessful.
 *
 * @author Home
 */
public class EQValue {

    private String Value; //The actual data value
    private String Status;//whether or not the equation succeeded
    private String Type;//the type of data
  
    public static String EQ_Type_Number ="NUMBER";
    public static String EQ_Type_Date ="DATE";
    public static String EQ_Type_String ="STRING";
    public static String EQ_Type_If_String ="IFSTRING";
    
    
    public EQValue(String value, String type) {
        Value = value;
        Type = type;
        Status = StatusList.EQ_Normal;
    }
    
    public EQValue(String value, String type, String status) {
        Value = value;
        Type = type;
        Status = status;
    }
    
    public EQValue(String value, Class type) {
        Value = value;
        String classtype="";
        if (type != String.class) if (type == Integer.class || type == Double.class) {
            classtype = "NUMBER";
        } else if (type== Date.class) {
            classtype = "DATE";
        } else {
            classtype = "STRING";
        }
        Type = classtype;
        Status = StatusList.EQ_Normal;
    }
    
    public String GetValue() {
        return Value;
    }

    public String GetStatus() {
        return Status;
    }

    public String GetType() {
        return Type;
    }

    public void SetValue(String value) {
        Value = value;
    }

    public void SetStatus(String status) {
        Status = status;
    }

    public void SetType(String type) {
        Type = type;
    }

    public boolean GetComplete()
    {
        return Status.equals(StatusList.EQ_Normal);
    }
    
    @Override
    public String toString() {
        if (Status.equals(StatusList.EQ_Normal)) {
            return Value;
        }
        return Status;
    }
}