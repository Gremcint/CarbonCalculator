/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Prototype.StaticClasses;

import Prototype.DataManaging.EQValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.util.Collections;
import java.util.Comparator;
import java.util.Calendar;
import org.apache.commons.math3.distribution.TDistribution;

/**
 *
 * @author User
 */
public class MathHandling {

    //this is a list of all the different operations sorted by category, 
    //there's no reason they couldn't be in the same list but this is way is a lot easier to read on the human end
    private static ArrayList<String> constantlist = new ArrayList(Arrays.asList("CONSTANT:LN2", "CONSTANT:LN10", "CONSTANT:LOG10E", "CONSTANT:SQRT2", "CONSTANT:SQRT1_2", "CONSTANT:PI", "CONSTANT:E"));
    private static ArrayList<String> functionlist = new ArrayList(Arrays.asList("FUNCTION:CBRT", "FUNCTION:ROUNDUP", "FUNCTION:ROUNDDOWN", "FUNCTION:ROUNDOFF", "FUNCTION:ABSOLUTE", "FUNCTION:COS", "FUNCTION:SIN", "FUNCTION:TAN", "FUNCTION:ACOS", "FUNCTION:ASIN", "FUNCTION:ATAN", "FUNCTION:LN", "FUNCTION:LOG", "FUNCTION:SQRT", "FUNCTION:NEG", "FUNCTION:EXP"));
    private static ArrayList<String> operatorlist = new ArrayList(Arrays.asList("^", "/", "*", "-", "+"));
    private static ArrayList<String> comparatorlist = new ArrayList(Arrays.asList("==", "<=", ">=", "<", ">", "!="));
    private static ArrayList<String> boollist = new ArrayList(Arrays.asList("&&", "||", "!"));
    private static ArrayList<String> datelist = new ArrayList(Arrays.asList("FUNCTION:DAYOFMONTH", "FUNCTION:DAYOFWEEK", "FUNCTION:DAYOFYEAR", "FUNCTION:MONTH", "FUNCTION:YEAR"));

    public static EQValue PerformBasicOp(String Op, HashMap<String, String> Values) {
        String type = Values.get("*TYPE*");

        if (type == null || type.equals("NUMBER")) {
            Double sum = 0.0;
            Double Result;
            if (Values.isEmpty()) {
                return new EQValue("0", EQValue.EQ_Type_Number);
            }
            switch (Op) {
                case "COUNT":
                    double temp = Values.size();
                    Result = temp;
                    break;
                case "MEAN":
                    for (String key : Values.keySet()) {
                        String currentvalue = Values.get(key);
                        if (Global.isNumeric(currentvalue)) {
                            sum += Double.parseDouble(currentvalue);
                        }
                    }
                    Result = sum / Values.size();
                    break;
                case "SUM":
                    for (String key : Values.keySet()) {
                        String currentvalue = Values.get(key);
                        if (Global.isNumeric(currentvalue)) {
                            sum += Double.parseDouble(currentvalue);
                        }
                    }
                    Result = sum;
                    break;
                default:
                    Global.Printmessage("Unrecognized operation:" + Op);
                    return new EQValue("", "STRING", StatusList.EQ_Error_Unknown_Operation);
            }

            return new EQValue(Result.toString(), EQValue.EQ_Type_Number);
        } else if (type.equals("STRING")) {
            String Result;
            switch (Op) {
                case "COUNT":
                    double temp = Values.size();
                    Result = String.valueOf(temp);
                    break;
                default:
                    Global.Printmessage("Unable to perform operation on Strings:" + Op);
                    return new EQValue("", "STRING", "Unable to perform operation on Strings:" + Op);
            }

            return new EQValue(Result, "STRING");
        } else if (type.equals("DATE")) {
            String Result;
            switch (Op) {
                case "COUNT":
                    double temp = Values.size();
                    Result = String.valueOf(temp);
                    break;
                default:
                    Global.Printmessage("Unable to perform operation on Dates:" + Op);
                    return new EQValue("", "STRING", "Unable to perform operation on Dates:" + Op);
            }

            return new EQValue(Result, EQValue.EQ_Type_Date);
        }

        return new EQValue("", "STRING", "Unable to perform operation:" + Op);
    }

    public static EQValue Solve(String ifstring, HashMap<String, EQValue> rowvalues, HashMap<String, HashMap<String, String>> tvalues) {

        //<editor-fold defaultstate="collapsed" desc="Stack">
        //this is where we sort the values into the stack and by this point the 
        //strings and dates need to be in the hashmaps with matching keys in the equation.
        Stack Operators = new Stack();
        ArrayList<String> Formula = new ArrayList();
        ArrayList<String> SplitEquation = Global.separateformula(ifstring);
        for (int index = 0; index < SplitEquation.size(); index++) {
            String term = SplitEquation.get(index);
            if (term != null && !term.equals("")) {
                //<editor-fold defaultstate="collapsed" desc="Prefixes">
                if (term.startsWith("DATE:") && rowvalues.containsKey(term.substring(5))) {
                    term = term.substring(5);
                } else if (term.startsWith("TXT:") && rowvalues.containsKey(term.substring(4))) {
                    term = term.substring(4);
                } else if (term.equals("-") && (index == 0 || (SplitEquation.get(index - 1).equals("(")) || (operatorlist.contains(SplitEquation.get(index - 1))))) {
                    term = "MATH:NEG";
                } else if (rowvalues.containsKey(term) && rowvalues.get(term).GetType().equals(EQValue.EQ_Type_Number)) {
                    term = rowvalues.get(term).GetValue();
                }
//</editor-fold>
                //if it's a number, a constant or in the values, basically if it's a value and not an operation put it on the stack
                if (rowvalues.containsKey(term) && rowvalues.get(term).GetType().equals(EQValue.EQ_Type_Number)) {
                    Formula.add(rowvalues.get(term).GetValue());
                } else if (Global.isNumeric(term) || constantlist.contains(term) || rowvalues.containsKey(term)) {
                    Formula.add(term);
                    if (!Operators.empty() && functionlist.contains(Operators.peek().toString())) {
                        Formula.add(Operators.pop().toString());
                    }
                } else if (Operators.empty() || (Operators.peek().toString().equals("(") && !term.equals(")")) || term.equals("(")) {
                    Operators.push(term);
                } else if (term.equals(")")) {
                    //<editor-fold defaultstate="collapsed" desc="close bracket">
//                    do {
//                        Formula.add(Operators.pop().toString());
//                    } while (!Operators.empty() && !Operators.peek().equals("("));
                    while (!Operators.empty() && !Operators.peek().equals("(")) {
                        Formula.add(Operators.pop().toString());
                    }
                    if (!Operators.isEmpty() && Operators.peek().equals("(") && !Formula.get(Formula.size() - 1).equals("(")) {
                        Operators.pop();
                    }
                    if (!Formula.isEmpty() && Formula.contains("(")) {
                        Formula.remove("(");
                    }
                    while (!Operators.empty() && functionlist.contains(Operators.peek().toString())) {
                        Formula.add(Operators.pop().toString());
                    }
//</editor-fold>
                } else if (functionlist.contains(term) || term.equals("^")) {
                    Operators.push(term);
                } else if (term.equals("/") || term.equals("*")) {
                    while (!Operators.empty() && (!Operators.peek().equals("+") || !Operators.peek().equals("-") || !Operators.peek().equals("("))) {
                        Formula.add(Operators.pop().toString());
                    }
                    Operators.push(term);
                } else if (comparatorlist.contains(term)) {
                    while (!Operators.empty() && !comparatorlist.contains(Operators.peek().toString()) && !boollist.contains(Operators.peek().toString()) && !Operators.peek().equals("(")) {
                        Formula.add(Operators.pop().toString());
                    }
                    Operators.push(term);
                } else if (boollist.contains(term)) {
                    while (!Operators.empty() && (!boollist.contains(Operators.peek().toString()) || !Operators.peek().equals("("))) {
                        Formula.add(Operators.pop().toString());
                    }
                    Operators.push(term);
                } else {
                    while (!Operators.empty() && !Operators.peek().equals("(") && !boollist.contains(Operators.peek().toString())) {
                        Formula.add(Operators.pop().toString());
                    }
                    Operators.push(term);
                }
            }
        }
        while (!Operators.empty()) {
            Formula.add(Operators.pop().toString());
        }
//</editor-fold>

        /*
         this is the second part where the actual operations are performed. 
         at this point the values should be either directly in the equation or
         stored in eqvalues with the key being the same name as what's in the equation
         */
        String error = "";
        loop:
        for (int index = 0; index < Formula.size(); index++) {
            String current = Formula.get(index);
            if (current.equals("MATH:NEG")) {
                //<editor-fold defaultstate="collapsed" desc="Negative">
                if (index > 0) {
                    String formulavalue = Formula.get(index - 1);
                    if (rowvalues.containsKey(formulavalue)) {
                        error = StatusList.EQ_Error_Unable_String_Date;
                        break;
                    } else if (Global.isNumeric(formulavalue)) {
                        BigDecimal value = new BigDecimal(Formula.get(index - 1));
                        value = value.negate();
                        Formula.set(index - 1, value.toString());
                        Formula.remove(index);
                        index = 0;
                    } else {
                        error = StatusList.EQ_Error_Improper_Equation;
                        break;
                    }
                }
//</editor-fold>
            } else if (operatorlist.contains(current)) {
                //<editor-fold defaultstate="collapsed" desc="Operators">
                if (index > 1) {
                    String leftvalue = Formula.get(index - 2);
                    String rightvalue = Formula.get(index - 1);

                    if (!rowvalues.containsKey(leftvalue) && !rowvalues.containsKey(rightvalue) && Global.isNumeric(leftvalue) && Global.isNumeric(rightvalue)) {

                        BigDecimal left = new BigDecimal(leftvalue);
                        BigDecimal right = new BigDecimal(rightvalue);
                        BigDecimal result;

                        switch (current) {
                            case "^":
                                result = new BigDecimal(Math.pow(left.doubleValue(), right.doubleValue()));
                                break;
                            case "/":
                                if (right.compareTo(BigDecimal.ZERO) == 0) {
                                    error = StatusList.EQ_Error_Divide_By_Zero;
                                    break loop;
                                } else {
                                    result = left.divide(right, 10, RoundingMode.HALF_UP);
                                }
                                break;
                            case "*":
                                result = left.multiply(right);
                                break;
                            case "-":
                                result = left.subtract(right);
                                break;
                            case "+":
                                result = left.add(right);
                                break;
                            default:
                                error = StatusList.EQ_Error_Unknown_Operator;
                                break loop;
                        }

                        result = result.setScale(10, RoundingMode.HALF_UP).stripTrailingZeros();
                        Formula.set(index, result.toPlainString());
                        Formula.remove(index - 1);
                        Formula.remove(index - 2);
                        index = 0;
                    } else if (current.equals("-") && rowvalues.get(leftvalue).GetType().equals(EQValue.EQ_Type_Date) && rowvalues.get(rightvalue).GetType().equals(EQValue.EQ_Type_Date)) {

                        String leftdatestring = rowvalues.get(leftvalue).GetValue();
                        String rightdatestring = rowvalues.get(rightvalue).GetValue();
                        if (Global.isDate(leftdatestring) && Global.isDate(rightdatestring)) {
                            Date leftdate = Date.valueOf(leftdatestring);
                            Date rightdate = Date.valueOf(rightdatestring);

                            long time1 = leftdate.getTime();
                            long time2 = rightdate.getTime();
                            long time3 = time1 - time2;
                            time3 = time3 / 24;
                            time3 = time3 / 60;
                            time3 = time3 / 60;
                            time3 = time3 / 1000;// this gives the result in days. if the scale needs to be altered change it here.
                            BigDecimal result = new BigDecimal(time3);
                            result = result.setScale(10, RoundingMode.HALF_UP).stripTrailingZeros();
                            Formula.set(index, result.toPlainString());
                            Formula.remove(index - 1);
                            Formula.remove(index - 2);
                            index = 0;
                        } else {
                            error = StatusList.EQ_Error_Only_Subtract_Date;
                            break;
                        }
                    } else {
                        error = StatusList.EQ_Error_Unable_String_Date;
                    }
//                    break;
                } else {
                    error = StatusList.EQ_Error_Missing_Operand;
                    break;
                }
//</editor-fold>
                //<editor-fold defaultstate="collapsed" desc="Functions">
            } else if (current.startsWith("TVAL:")) {
                BigDecimal result;
                String formulavalue = Formula.get(index - 1);
                if (tvalues.containsKey(formulavalue) && tvalues.get(formulavalue) != null && tvalues.get(formulavalue).containsKey("CONFIDENCE")) {
                    String constring = tvalues.get(formulavalue).get("CONFIDENCE");

                    if (Global.isNumeric(formulavalue) && Global.isNumeric(constring)) {
                        Double df = Double.parseDouble(formulavalue);
                        df = df / 100;
                        result = new BigDecimal((new TDistribution(df)).inverseCumulativeProbability(0.5 + Double.parseDouble(constring) / 2));
                        result = result.setScale(10, RoundingMode.HALF_UP).stripTrailingZeros();
                        Formula.set(index, result.toPlainString());
                        Formula.remove(index - 1);
                    } else {
                        error = StatusList.EQ_Error_Improper_TValue;
                        break;
                    }
                } else {
                    error = StatusList.EQ_Error_Missing_TValue;
                    break;
                }
            } else if (functionlist.contains(current) || datelist.contains(current)) {
                if (index > 0) {
                    BigDecimal result;
                    String formulavalue = Formula.get(index - 1);
                    if (datelist.contains(current) && rowvalues.containsKey(formulavalue) && rowvalues.get(formulavalue).GetType().equals(EQValue.EQ_Type_Date)) {
                        String datevalue = rowvalues.get(formulavalue).GetValue();
                        if (Global.isDate(datevalue)) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(Date.valueOf(datevalue));
                            switch (current) {
                                case "FUNCTION:DAYOFWEEK":
                                    result = new BigDecimal(calendar.get(Calendar.DAY_OF_WEEK));
                                    break;
                                case "FUNCTION:DAYOFMONTH":
                                    result = new BigDecimal(calendar.get(Calendar.DAY_OF_MONTH));
                                    break;
                                case "FUNCTION:DAYOFYEAR":
                                    result = new BigDecimal(calendar.get(Calendar.DAY_OF_YEAR));
                                    break;
                                case "FUNCTION:YEAR":
                                    result = new BigDecimal(calendar.get(Calendar.YEAR));
                                    break;
                                case "FUNCTION:MONTH":
                                    result = new BigDecimal(calendar.get(Calendar.MONTH));
                                    break;
                                default:
                                    error = StatusList.EQ_Error_Unable_Date;
                                    break loop;
                            }
                        } else {
                            error = StatusList.EQ_Error_Only_Date;
                            break;
                        }
                    } else if (rowvalues.containsKey(current)) {
                        error = StatusList.EQ_Error_Unable_String_Date;
                        break;
                    } else if (datelist.contains(current)) {
                        error = StatusList.EQ_Error_Only_Date;
                        break;
                    } else {
                        BigDecimal left = new BigDecimal(formulavalue);

                        switch (current) {
                            case "FUNCTION:ROUNDUP":
                                result = left.setScale(0, RoundingMode.UP);
                                break;
                            case "FUNCTION:ROUNDDOWN":
                                result = left.setScale(0, RoundingMode.DOWN);
                                break;
                            case "FUNCTION:ROUND":
                            case "FUNCTION:ROUNDOFF":
                                result = left.setScale(0, RoundingMode.HALF_UP);
                                break;
                            case "FUNCTION:ABSOLUTE":
                                result = left.abs();
                                break;
                            case "FUNCTION:COS":
                                result = new BigDecimal(Math.cos(left.doubleValue()));
                                break;
                            case "FUNCTION:SIN":
                                result = new BigDecimal(Math.sin(left.doubleValue()));
                                break;
                            case "FUNCTION:TAN":
                                result = new BigDecimal(Math.tan(left.doubleValue()));
                                break;
                            case "FUNCTION:ACOS":
                                result = new BigDecimal(Math.acos(left.doubleValue()));
                                break;
                            case "FUNCTION:ASIN":
                                result = new BigDecimal(Math.asin(left.doubleValue()));
                                break;
                            case "FUNCTION:ATAN":
                                result = new BigDecimal(Math.atan(left.doubleValue()));
                                break;
                            case "FUNCTION:LN":
                                result = new BigDecimal(Math.log(left.doubleValue()));
                                break;
                            case "FUNCTION:LOG":
                                result = new BigDecimal(Math.log10(left.doubleValue()));
                                break;
                            case "FUNCTION:SQRT":
                                result = new BigDecimal(Math.sqrt(left.doubleValue()));
                                break;
                            case "FUNCTION:CBRT":
                                result = new BigDecimal(Math.cbrt(left.doubleValue()));
                                break;
                            case "FUNCTION:NEG":
                                result = left.multiply(new BigDecimal(-1));
                                break;
                            case "FUNCTION:EXP":
                                result = new BigDecimal(Math.exp(left.doubleValue()));
                                break;
                            default:
                                error = StatusList.EQ_Error_Unknown_Function;
                                break loop;
                        }

                    }
                    result = result.setScale(10, RoundingMode.HALF_UP).stripTrailingZeros();
                    Formula.set(index, result.toPlainString());
                    Formula.remove(index - 1);
                } else {
                    error = StatusList.EQ_Error_Missing_Operand;
                    break;
                }
//</editor-fold>
            } else if (constantlist.contains(current)) {
                //<editor-fold defaultstate="collapsed" desc="Constants">
                BigDecimal result;
                switch (current) {
                    case "CONSTANT:LN2":
                        result = new BigDecimal(Math.log(2));
                        break;
                    case "CONSTANT:LN10":
                        result = new BigDecimal(Math.log(10));
                        break;
                    case "CONSTANT:LOG10E":
                        result = new BigDecimal(Math.log10(Math.E));
                        break;
                    case "CONSTANT:SQRT2":
                        result = new BigDecimal(Math.sqrt(2));
                        break;
                    case "CONSTANT:SQRT1_2":
                        result = new BigDecimal(Math.sqrt(.5));
                        break;
                    case "CONSTANT:PI":
                        result = new BigDecimal(Math.PI);
                        break;
                    case "CONSTANT:E":
                        result = new BigDecimal(Math.E);
                        break;
                    default:
                        error = StatusList.EQ_Error_Unknown_Constant;
                        break loop;
//</editor-fold>
                    }
                result = result.setScale(10, RoundingMode.HALF_UP).stripTrailingZeros();
                Formula.set(index, result.toPlainString());

            } else if (comparatorlist.contains(current)) {
                //<editor-fold defaultstate="collapsed" desc="Comparators">
                if (index > 1) {
                    String left = Formula.get(index - 2);
                    String right = Formula.get(index - 1);
                    int comparison = 200;
                    if (Global.isNumeric(right) && Global.isNumeric(left)) {
                        BigDecimal leftnum = new BigDecimal(left);
                        BigDecimal rightnum = new BigDecimal(right);
                        comparison = leftnum.compareTo(rightnum);
                    } else if (rowvalues.containsKey(left) && rowvalues.containsKey(right)) {
                        EQValue lefteq = rowvalues.get(left);
                        EQValue righteq = rowvalues.get(right);
                        if (lefteq.GetType().equals(EQValue.EQ_Type_Date) && righteq.GetType().equals(EQValue.EQ_Type_Date)) {
                            if (Global.isDate(righteq.GetValue()) && Global.isDate(lefteq.GetValue())) {
                                Date leftdate = Date.valueOf(lefteq.GetValue());
                                Date rightdate = Date.valueOf(righteq.GetValue());
                                if (leftdate == null || rightdate == null) {
                                    error = StatusList.EQ_Error_Missing_Data;
                                    break;
                                }
                                comparison = leftdate.compareTo(rightdate);
                            }
                        } else if (lefteq.GetType().equals("STRING") && righteq.GetType().equals("STRING")) {
                            comparison = lefteq.GetValue().compareTo(righteq.GetValue());
                        } else {
                            error = StatusList.EQ_Error_Unable_Compare_Data_Types;
                            break;
                        }
                    }
                    String result;
                    switch (current) {
                        //if (highdate.compareTo(currentvalue) < 0) {
                        //DateA.compareto(DateB) returns negative if DateB is a later date than A.
                        case "==":
                            if (comparison == 0) {
                                result = "true";
                            } else {
                                result = "false";
                            }
                            break;
                        case "<=":
                            if (comparison <= 0) {
                                result = "true";
                            } else {
                                result = "false";
                            }
                            break;
                        case ">=":
                            if (comparison >= 0) {
                                result = "true";
                            } else {
                                result = "false";
                            }
                            break;
                        case "<":
                            if (comparison < 0) {
                                result = "true";
                            } else {
                                result = "false";
                            }
                            break;
                        case ">":
                            if (comparison > 0) {
                                result = "true";
                            } else {
                                result = "false";
                            }
                            break;
                        case "!=":
                            if (comparison != 0) {
                                result = "true";
                            } else {
                                result = "false";
                            }
                            break;
                        default:
                            error = StatusList.EQ_Error_Unknown_Comparator;
                            break loop;
                    }

                    Formula.set(index, result);
                    Formula.remove(index - 1);
                    Formula.remove(index - 2);
                    index = 0;

                } else {
                    error = StatusList.EQ_Error_Missing_Operand;
                    break;
                }
                //</editor-fold>
            } else if (current.equals("!")) {
                //<editor-fold defaultstate="collapsed" desc="Not">

                if (index > 0) {
                    String left = Formula.get(index - 1);
                    String result;
                    switch (left) {
                        case "true":
                            result = "false";
                            break;
                        case "false":
                            result = "true";
                            break;
                        default:
                            error = StatusList.EQ_Error_Not_True_False;
                            break loop;
                    }
                    Formula.set(index, result);
                    Formula.remove(index - 1);
                    index = 0;
                } else {
                    error = StatusList.EQ_Error_Missing_Operand;
                    break;
                }
                //</editor-fold>
            } else if (current.equals("&&")) {
                //<editor-fold defaultstate="collapsed" desc="And">
                if (index > 1) {
                    String left = Formula.get(index - 1);
                    String right = Formula.get(index - 2);
                    String result;
                    if ((left.equals("true") || left.equals("false")) && (right.equals("true") || right.equals("false"))) {
                        if (left.equals("true") && right.equals("true")) {
                            result = "true";
                        } else {
                            result = "false";
                        }
                    } else {
                        error = StatusList.EQ_Error_Not_True_False;
                        break loop;
                    }
                    Formula.set(index, result);
                    Formula.remove(index - 1);
                    Formula.remove(index - 2);
                    index = 0;
                } else {
                    error = StatusList.EQ_Error_Missing_Operand;
                    break;
                }
                //</editor-fold>
            } else if (current.equals("||")) {
                //<editor-fold defaultstate="collapsed" desc="Or">
                if (index > 1) {
                    String left = Formula.get(index - 1);
                    String right = Formula.get(index - 2);
                    String result;
                    if ((left.equals("true") || left.equals("false")) && (right.equals("true") || right.equals("false"))) {
                        if (left.equals("true") || right.equals("true")) {
                            result = "true";
                        } else {
                            result = "false";
                        }
                    } else {
                        error = StatusList.EQ_Error_Not_True_False;
                        break loop;
                    }
                    Formula.set(index, result);
                    Formula.remove(index - 1);
                    Formula.remove(index - 2);
                    index = 0;
                } else {
                    error = StatusList.EQ_Error_Missing_Operand;
                    break;
                }
                //</editor-fold>
            }
        }
        if (Formula.size() > 1) {
            error = StatusList.EQ_Error_Improper_Equation;
        }
        if (error.isEmpty() && Formula.size() == 1) {
            String finalresult = Formula.get(0);
            if (rowvalues.containsKey(finalresult)) {
                EQValue finalEQVal = new EQValue(rowvalues.get(finalresult).GetValue(), rowvalues.get(finalresult).GetType());
                return finalEQVal;
            } else if (finalresult.equals("true") || finalresult.equals("false")) {
                return new EQValue(finalresult, "STRING");
            } else if (Global.isNumeric(finalresult)) {
                BigDecimal roundedresult = new BigDecimal(finalresult);
                roundedresult = roundedresult.setScale(TableHandling.getScale(), RoundingMode.HALF_UP).stripTrailingZeros();//this takes the numberic result and rounds according to the set scale.
                return new EQValue(roundedresult.toPlainString(), EQValue.EQ_Type_Number);
            }
        }
        return new EQValue("", "", error);
    }

    //<editor-fold defaultstate="collapsed" desc="Grid Sorting">
    //sorts a grid of values left to right and returns the last column
    // in the highest or lowest. this is currently only used in one spot but having it here
    //keeps all the calculations together and makes it easier to use elsewhere if necessary
    public static EQValue FetchHighLow(ArrayList<ArrayList<Comparable>> data, String Op) {
        if (!data.isEmpty()) {
            Collections.sort(data, gridsort);
            Object result = null;
            switch (Op) {
                case "LOWEST":
                    result = data.get(0).get(data.get(0).size() - 1);//return the second last column as it'll be the result value of that row's calculation
                    break;
                case "HIGHEST":
                    int index = data.size() - 1;
                    result = data.get(index).get(data.get(index).size() - 1);//return the second last column as it'll be the result value of that row's calculation
                    break;
            }
            if (result != null) {

                if (result.getClass() == Double.class || result.getClass() == Integer.class) {
                    return new EQValue(result.toString(), EQValue.EQ_Type_Number);
                } else if (result.getClass() == Date.class) {
                    return new EQValue(result.toString(), EQValue.EQ_Type_Date);
                }
                return new EQValue(result.toString(), "STRING");
            }
        }
        return null;
    }

    //some equations require comparing to the previous value of the same measurement, so this code is to accomodate that
    //it sorts the data in order filtering out the data that doesn't match the criteria and then returns the previous or next value
    public static EQValue FetchPrevNext(ArrayList<ArrayList<Comparable>> data, Integer id, String direction) {
        //data has the format of Sorting Column->ID Column->Value Column
        if (direction == null) {
            return null;
        }
        boolean complete = true;
        Object result = null;

        if (!data.isEmpty()) {
            Collections.sort(data, gridsort);
            for (int x = 0; x < data.size(); x++) {
                ArrayList<Comparable> currentrow = data.get(x);
                if (currentrow.size() < 2) {
                    return new EQValue("", "STRING", "missing data");
                }
                Comparable currentrowid = currentrow.get(currentrow.size() - 2);
                if (currentrowid.equals(id.doubleValue())) {
                    if ((direction.equals("ASCENDING") && x == data.size() - 1)) {
                        result = currentrow.get(currentrow.size() - 1);
                        complete = false;
                    } else if (direction.equals("DESCENDING") && x == 0) {
                        result = currentrow.get(currentrow.size() - 1);
                        complete = false;
                    } else if (direction.equals("DESCENDING")) {
                        result = data.get(x - 1).get(data.get(x - 1).size() - 1);
                        if (result == null) {
                            result = currentrow.get(currentrow.size() - 1);
                            complete = false;
                        }
                    } else if (direction.equals("ASCENDING")) {
                        result = data.get(x + 1).get(data.get(x + 1).size() - 1);
                        if (result == null) {
                            result = currentrow.get(currentrow.size() - 1);
                            complete = false;
                        }
                    }
                }
            }
            if (result != null) {
                EQValue EQResult;
                if (result.getClass() == Double.class || result.getClass() == Integer.class) {
                    EQResult = new EQValue(result.toString(), EQValue.EQ_Type_Number);
                } else if (result.getClass() == Date.class) {
                    EQResult = new EQValue(result.toString(), EQValue.EQ_Type_Date);
                } else {
                    EQResult = new EQValue(result.toString(), "STRING");
                }
                if (!complete) {
                    EQResult.SetStatus(StatusList.EQ_Error_No_Prev_Value);
                    //this will result in both knowing that there was not a
                    //previous value but also having the value of reference row
                    //right on hand if needed for the same value option.
                }
                return EQResult;
            }
        }
        return new EQValue("", "STRING", StatusList.EQ_Error_Operation_Failed);
    }

    final static Comparator<ArrayList<Comparable>> gridsort = new Comparator<ArrayList<Comparable>>() {
        @Override
        public int compare(ArrayList<Comparable> left, ArrayList<Comparable> right) {
            int length;
            if (left.size() > right.size()) {
                length = left.size();
            } else {
                length = right.size();
            }
            int result = 0;
            for (int currentrow = 0; currentrow < length; currentrow++) {
                if (currentrow >= right.size()) {
                    return 1;
                }
                if (currentrow >= left.size()) {
                    return -1;
                }
                Comparable currentleft = left.get(currentrow);
                Comparable currentright = right.get(currentrow);
                if (currentleft != null && currentright != null) {
                    result = currentleft.compareTo(currentright);
                    if (result != 0) {
                        return result;
                    }
                }
                if (currentleft == null && currentright != null) {
                    return -1;
                }
                if (currentright == null && currentleft != null) {
                    return 1;
                }
            }
            return result;
        }
    };
//</editor-fold>
}
