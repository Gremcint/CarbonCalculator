/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Prototype.StaticClasses;

import java.io.File;
import java.sql.Date;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import org.h2.jdbcx.JdbcConnectionPool;
import org.jdom2.Element;

/**
 *
 * @author User
 */
public class Global {

    public static final String TEMPLATEMODE = "TEMPLATE";
    public static final String PROJECTMODE = "PROJECT";
    
    private static JdbcConnectionPool cp;
    private static String XMLFile;
    private static String DBFile;
    private static final ArrayList<String> parselist = new ArrayList(Arrays.asList("(", ")", "+", "-", "/", "*", "^", " ", "Math.pow", ",", "{", "}", "OP:", "==", "<", ">",">=","<=", "!", "&&", "||", "EVAL:", "Math.ceil", "Math.LN2", "Math.LN10", "Math.LOG10E", "Math.SQRT2", "Math.SQRT1_2", "Math.PI", "Math.E", "Math.floor", "Math.round", "Math.abs", "Math.cos", "Math.sin", "Math.tan", "Math.acos", "Math.asin", "Math.atan", "Math.ln", "Math.pow", "Math.sqrt", "<html>&#8730</html>", "<html>&#8730</html>"));
    private static String currentschema;
    private static String ProgramMode;
    private static final String DateFormat = "yyyy-MM-dd";
    
    
    public static String getDateFormat() {
        return DateFormat;
    }

    public static void setMode(String Mode) {
        ProgramMode = Mode;
    }

    public static String getMode() {
        return ProgramMode;
    }

    public static ArrayList<String> getParseList() {
        return parselist;
    }

    public static void setcurrentschema(String schema) {
        currentschema = schema;
    }

    public static String getcurrentschema() {
        if (currentschema != null) {
            return currentschema;
        }
        return null;
    }

  

    public static void setxmlfilename(String filename) {
        XMLFile = filename;
    }

    public static String getxmlfilename() {
        if (XMLFile != null) {
            return XMLFile;
        }
        return null;
    }

    public static void setdbfilename(String filename) {
        DBFile = filename;
    }

    public static String getdbfilename() {
        if (DBFile != null) {
            return DBFile;
        }
        return null;
    }

    public static void setConnectionPool(JdbcConnectionPool CP) {
        cp = CP;
    }

    public static JdbcConnectionPool getConnectionPool() {
        return cp;
    }

    public static boolean CheckForFile(String Filename) {
        File file = new File(Filename);
        return file.exists();
    }

    public static void Printmessage(String Text) {
        System.out.println(Text);
    }

    public static boolean isNumeric(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isDate(String value) {
        try {
            Date.valueOf(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static HashMap<String, String> makecommandmap(Element source) {
        String mapstring = source.getAttributeValue("value");
        if (mapstring != null && !mapstring.isEmpty()) {
            return makecommandmap(mapstring);
        }
        return null;
    }

    public static HashMap<String, String> makecommandmap(String source) {
        
        if (source != null && !source.isEmpty()) {
            source = source.replaceAll("&amp;", "&");
            source = source.replaceAll("&gt;", ">");
            source = source.replaceAll("&lt;", "<");
            String termsections[] = source.split(";");
            HashMap<String, String> Commandmap = new HashMap();
            for (int y = 0; y < termsections.length; y++) {
                if (termsections[y].contains(":")) {
                    String[] temp = termsections[y].split(":", 2);
                    Commandmap.put(temp[0], temp[1]);
                } else if (!termsections[y].isEmpty()) {
                    Commandmap.put("NOCOMMAND" + y, termsections[y]);
                }
            }
            return Commandmap;
        }
        return null;
    }


    public static String MakeCommandString(HashMap<String, String> commandmap) {
        String map = "";
        for (String key : commandmap.keySet()) {
            map = key + ":" + commandmap.get(key) + ";" + map;
        }

        return map;
    }

    public static ArrayList<String> parseformula(String formula) {
        if (formula == null || formula.equals("")) {
            return new ArrayList();
        }
        String newformula = formula;//this code is used to get the terms where we need to hunt for data
        //this will strip out negative signs but as this is not where the math occurs and the values will
        //be subbed back into original equation it won't matter (-x becomes x, we search for x find x=3 sub
        //x back into original and get -3
        newformula = newformula.replace("OP:", "");
        newformula = newformula.replace("EVAL:", "");
        if (!newformula.contains("`")) {
            for (String str : parselist) {
                newformula = newformula.replace(str, "`");
            }
        }
        String[] temp = newformula.split("`");
        ArrayList<String> returnlist = new ArrayList();
        for (String temp1 : temp) {
            if (temp1 != null && !temp1.equals("") && !Global.isNumeric(temp1) && !returnlist.contains(temp1) && !parselist.contains(temp1) && !temp1.startsWith("FUNCTION:") && !temp1.startsWith("CONSTANT:")) {
                returnlist.add(temp1);
            }
        }
        return returnlist;
    }

    public static ArrayList<String> separateformula(String formula) {
        String newformula = formula;
        newformula = newformula.replace("OP:", "");
        newformula = newformula.replace("EVAL:", "");
        if (!newformula.contains("`")) {
            for (String str : parselist) {
                newformula = newformula.replace(str, "`" + str + "`");
            }
        }
        String[] temp = newformula.split("`");//having removed all operations and text values we now get the terms needed to be found
        ArrayList<String> termlist = new ArrayList();
        for (String current : temp) {
            if (current != null && !current.equals("")) {
                termlist.add(current);
            }
        }
        return termlist;
    }
    
    
}