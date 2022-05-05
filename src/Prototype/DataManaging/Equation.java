package Prototype.DataManaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import org.jdom2.Element;
import Prototype.StaticClasses.Global;
import Prototype.StaticClasses.MathHandling;
import Prototype.StaticClasses.StatusList;

/**
 *
 * @author User
 */
public class Equation {

    protected ArrayList<String> listofterms = new ArrayList();//the different terms in the equation (refers to the variables not to the numbers)
    protected DatabaseTableModel hometable;
    protected Element source;
    protected String equationstring, equationname;//general information on the equation
    protected String path;//the path the equation is stored on
    protected HashMap<String, HashMap<String, String>> AllCommandMaps = new HashMap();//stores the commandmap for every term
    protected HashMap<String, HashMap<String, HashMap<String, String>>> EvalTerms = new HashMap();
    protected HashMap tvalues = new HashMap();
    private HashMap<String, String> OpMap = new HashMap();
    private boolean opmode = false;

    //<editor-fold defaultstate="collapsed" desc="Constructors">
    public Equation(Element Source, DatabaseTableModel home) {
        try {
            opmode = false;
            source = Source;
            hometable = home;
            equationname = source.getName();
            equationstring = source.getAttributeValue("value");

            if (source.getChild("TERMS") != null) {
                Element termsource = source.getChild("TERMS");
                
                java.util.List<Element> termlist = termsource.getChildren();
                for (Element currentelement : termlist) {
                    //handle if statements first.
                    if (equationstring.contains("EVAL:" + currentelement.getName())) {
                        //<editor-fold defaultstate="collapsed" desc="If Statement">
                        HashMap<String, HashMap<String, String>> evalbranches = new HashMap();
                        String children = "";
                        for (Element currentchild : currentelement.getChildren()) {
                            String currentname = currentchild.getName();
                            evalbranches.put(currentname, Global.makecommandmap(currentchild.getAttributeValue("value")));
                            if (currentname.equals("IF")) {
                                children = "IF," + children;
                            } else if (!currentname.equals("DEFAULT")) {
                                children = children + "," + currentname;
                            }
                        }
                        if (currentelement.getChild("DEFAULT") != null) {
                            children = children + ",ELSE";
                        }
                        EvalTerms.put(currentelement.getName(), evalbranches);
                        HashMap<String, String> temp = new HashMap();
                        temp.put("PATH", hometable.getPath());
                        temp.put("EVAL", "IF");
                        temp.put("VALUE", children);
                        AllCommandMaps.put(currentelement.getName(), temp);
//</editor-fold>
                    } else {
                        HashMap<String, String> currentmap = Global.makecommandmap(currentelement.getAttributeValue("value"));
                        AllCommandMaps.put(currentelement.getName(), currentmap);
                    }
                }
            }
            listofterms = Global.parseformula(equationstring);
        } catch (Exception e) {
            Global.Printmessage("Equation Constructor error 1");
        }
    }

    public Equation(Element Source, DatabaseTableModel home, HashMap<String, String> Opmap) {
        try {
            opmode = true;
            source = Source;
            hometable = home;
            equationname = source.getName();
            equationstring = source.getAttributeValue("value");
            if (equationstring == null) {
                equationstring = "";
            }
            OpMap = Opmap;

            if (source.getChild("TERMS") != null) {
                Element termsource = source.getChild("TERMS");
                java.util.List<Element> termlist = termsource.getChildren();
                for (Element currentelement : termlist) {
                    //handle if statements first.
                    if (equationstring.contains("EVAL:" + currentelement.getName())) {
                        //<editor-fold defaultstate="collapsed" desc="If Statement">
                        HashMap<String, HashMap<String, String>> evalbranches = new HashMap();
                        String children = "";
                        for (Element currentchild : currentelement.getChildren()) {
                            String currentname = currentchild.getName();
                            evalbranches.put(currentname, Global.makecommandmap(currentchild.getAttributeValue("value")));
                            if (currentname.equals("IF")) {
                                children = "IF," + children;
                            } else if (!currentname.equals("DEFAULT")) {
                                children = children + "," + currentname;
                            }
                        }
                        if (currentelement.getChild("DEFAULT") != null) {
                            children = children + ",ELSE";
                        }
                        EvalTerms.put(currentelement.getName(), evalbranches);
                        HashMap<String, String> temp = new HashMap();
                        temp.put("PATH", hometable.getPath());
                        temp.put("EVAL", "IF");
                        temp.put("VALUE", children);
                        AllCommandMaps.put(currentelement.getName(), temp);
//</editor-fold>
                    } else {
                        HashMap<String, String> currentmap = Global.makecommandmap(currentelement.getAttributeValue("value"));
                        AllCommandMaps.put(currentelement.getName(), currentmap);
                    }
                }
            }
            listofterms = Global.parseformula(equationstring);
            if (Opmap.containsKey("IF")) {

                listofterms.addAll(Global.parseformula(Opmap.get("IF")));
            }
            if (Opmap.containsKey("MATCH")) {
                listofterms.addAll(Global.parseformula(Opmap.get("MATCH")));
            }
            if (Opmap.containsKey("ORDERBY")) {
                listofterms.addAll(Global.parseformula(Opmap.get("ORDERBY")));
            }
        } catch (Exception e) {
            Global.Printmessage("Equation Constructor error 1");
        }
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Gets">
    public String getname() {
        return equationname;
    }

    public ArrayList<String> getlistofterms() {
        return listofterms;
    }
//</editor-fold>

    public HashMap<String, String> getcommandmap(String Term) {
        if (Term.contains(",")) {
            String[] termpath = Term.split(",");
            try {
                return EvalTerms.get(termpath[0]).get(termpath[1]);
            } catch (Exception e) {
                return null;
            }
        }
        return AllCommandMaps.get(Term);
    }

    private TreeMap<String, HashMap<String, String>> makeevalmap(Element evalsource) {
        TreeMap<String, HashMap<String, String>> evalbranches = new TreeMap();
        for (Element currentchild : evalsource.getChildren()) {
            evalbranches.put(currentchild.getName(), Global.makecommandmap(currentchild.getAttributeValue("value")));
        }
        return evalbranches;
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Gather Data">
    protected HashMap<String, HashMap<String, EQValue>> gatherdata(ArrayList<String> termslist) {
        HashMap<String, DatabaseTableModel> Locations = new HashMap();//tracks nodes we've already searched for to save time
        HashMap<String, HashMap<String, EQValue>> newvalues = new HashMap();
        path = hometable.getPath();
        try {
            if (!AllCommandMaps.isEmpty()) {
                for (String term : termslist) {
                    DatabaseTableModel currentmodel;
                    HashMap<String, String> Commandmap = getcommandmap(term);
                    //if it's not an if term, if we have the path and if it's an op or not in the hometable.
                    if (Commandmap != null && !Commandmap.containsKey("EVAL") && (Commandmap.containsKey("PATH") && ((!Commandmap.get("PATH").equals(path) || Commandmap.containsKey("OP"))))) {
                        String PathString = Commandmap.get("PATH");
                        String[] PathArray = PathString.split(",");
                        String tablename = PathArray[PathArray.length - 1];
                        if (Locations.containsKey(PathString)) {
                            currentmodel = Locations.get(tablename);
                        } else {
                            currentmodel = Prototype.StaticClasses.TableHandling.getDatabaseTableModel(tablename);
                            if (currentmodel != null) {
                                Locations.put(tablename, currentmodel);
                            }
                        }
                        //right now we want to retrieve the entire column if there's no operation so the
                        //equation doesn't have to search every table row for each row of the hometable which
                        //would greatly increase the run time, instead it get's one column in a hashmap that it
                        //uses as needed
                        if (currentmodel != null) {
                            HashMap currentvalues = currentmodel.getdata(Commandmap, source.getChild("TERMS"));
                            newvalues.put(term, currentvalues);
                        }
                    }
                }
            }
            return newvalues;
        } catch (Exception e) {
            Global.Printmessage("Equation.GatherData ArrayList");
            Global.Printmessage(path + " " + equationname);
            return null;
        }
    }

    /*
     so this gathers the values for the tables that that the hometable references either through foreign key or an op
     the return format is key = column name value = result hashmap
     result hashmap = key= match/group value or all and value = the actual result used in the equation
     */
    protected HashMap<String, HashMap<String, EQValue>> gatherdata(Element TERMS) {
        HashMap<String, DatabaseTableModel> Locations = new HashMap();//tracks nodes we've already searched for to save time
        HashMap<String, HashMap<String, EQValue>> newvalues = new HashMap();
        path = hometable.getPath();
        try {
            for (Element term : TERMS.getChildren()) {
                DatabaseTableModel currentmodel;
                HashMap<String, String> Commandmap = Global.makecommandmap(term.getAttributeValue("value"));
                if (!Commandmap.containsKey("EVAL") && (Commandmap.containsKey("PATH") && ((!Commandmap.get("PATH").equals(path) || Commandmap.containsKey("OP"))))) {
                    String PathString = Commandmap.get("PATH");
                    if (Locations.containsKey(PathString)) {
                        currentmodel = Locations.get(PathString);
                    } else {
                        PathString = PathString.substring(7);
                        currentmodel = Prototype.StaticClasses.TableHandling.getDatabaseTableModel(PathString);
                        if (currentmodel != null) {
                            Locations.put(PathString, currentmodel);
                        }
                    }
                    //right now we want to retrieve the entire column if there's no operation so the
                    //equation doesn't have to search every table row for each row of the hometable which
                    //would greatly increase the run time, instead it get's one column in a hashmap that it
                    //uses as needed
                    if (currentmodel != null) {
                        HashMap<String, EQValue> currentvalues = currentmodel.getdata(Commandmap, source.getChild("TERMS"));
                        newvalues.put(term.getName(), currentvalues);
                    }
                }
            }
            return newvalues;
        } catch (Exception e) {
            Global.Printmessage("Equation.GatherData Element");
            Global.Printmessage(path + " " + equationname);
            return null;
        }
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Evaluate">
    //this returns a hashmap with the primary key as the key and the equation result as the value
    protected HashMap<String, EQValue> calculatevalues(String neweq, HashMap<String, HashMap<String, EQValue>> Values, HashMap<String, HashMap<String, Element>> IfValues) {
        HashMap<String, EQValue> results = new HashMap();
        ArrayList<String> terms = Global.parseformula(neweq);

        try {
            for (int x = 0; x < hometable.getRowCount(); x++) {//for each row of the table
                String errortext = "";
                String cureqstring = neweq;
                HashMap<String, EQValue> currentrow = hometable.getEQRowMap(x, true);//creates a map of columnname>value for that row
                String primcol = hometable.getPrimary();
                String primid = currentrow.get(primcol).GetValue();

                for (String currentterm : terms) {//for each term in the equation
                    EQValue currentvalue = null;
                    HashMap<String, String> currentmap = getcommandmap(currentterm);
                    //if this term is in the hometable we get the information here directly
                    if (Values.containsKey(currentterm) && !(currentmap.containsKey("PATH") && path.equals(Global.getcurrentschema() + "," + currentmap.get("PATH")) && currentmap.containsKey("VALUE"))) {//if we fetched the information from outside the table already
                        //<editor-fold defaultstate="collapsed" desc="comment">
                        /*
                         so this is getting the values out of the value hashmap. 
                         option a: linked to other table and paired using the match value. getting the value of the foreign key for that table
                         option b: performed operation and pairing up the results using the groupby value to match a foreign key
                         option c: performed operation and using the same result for all values.
                         */
                        String key = "MATCH";
                        if (currentmap.containsKey("OP") && currentmap.containsKey("GROUPBY"))//if the term is an operation we use the groupby value instead
                        {
                            key = "GROUPBY";
                        }
                        if (Values.get(currentterm).containsKey("*ALL*")) {
                            currentvalue = Values.get(currentterm).get("*ALL*");
                            if (!"NUMBER".equals(currentvalue.GetType())) {
                                currentrow.put(currentterm, currentvalue);
                            }
                        } else if (currentmap.containsKey(key) && !currentmap.get(key).equals("")) {
                            String Column = currentmap.get(key);
                            String ColumnValue = currentrow.get(Column).GetValue();
                            if (hometable.isforeignkey(Column)) {
                                String[] valsplit = ColumnValue.split(":");
                                ColumnValue = valsplit[0];
                            }
                            if (Values.get(currentterm).containsKey(ColumnValue)) {
                                currentvalue = Values.get(currentterm).get(ColumnValue);
                                if (!"NUMBER".equals(currentvalue.GetType())) {
                                    currentrow.put(currentterm, currentvalue);
                                }
                            }
                        } else if (Values.get(currentterm).containsKey(primid) && Values.get(currentterm).get(primid) != null) {
                            currentvalue = Values.get(currentterm).get(primid);
                            if (!"NUMBER".equals(currentvalue.GetType())) {
                                currentrow.put(currentterm, currentvalue);
                            }
                        } else {
                            errortext = "missing " + key + " instruction in: " + currentterm;
                        }
                        //</editor-fold>
                    } else if (currentmap.containsKey("PATH") && path.equals(Global.getcurrentschema() + "," + currentmap.get("PATH"))) {//if the term is in the hometable
                        //<editor-fold defaultstate="collapsed" desc="comment">

                        if (currentmap.containsKey("IF") && !currentmap.get("IF").equals("")) {
                            Element TERMS;

                            TERMS = source.getChild("TERMS");
                            String Filter = currentmap.get("IF");
                            if (TERMS != null) {
                                for (Element txtterm : TERMS.getChildren()) {
                                    String name = txtterm.getName();
                                    if (Filter.contains("TXT:" + name) && !currentrow.containsKey(name)) {
                                        currentrow.put(name, new EQValue(txtterm.getAttributeValue("value"), "STRING"));
                                    } else if (Filter.contains("DATE:" + name) && !currentrow.containsKey(name)) {
                                        currentrow.put(name, new EQValue(txtterm.getAttributeValue("value"), "DATE"));
                                    }
                                }
                            }
//                            if (!MathHandling.Solve(Filter, currentrow, classes, tvalues).equals("true")) {
//                                errortext = "skip";
//                                break;
//                            }
                        }

                        String valuename = currentmap.get("VALUE");
                        EQValue currenteqval = currentrow.get(valuename);
                        if (currenteqval == null) {
                            currentvalue = new EQValue("", "STRING", StatusList.EQ_Error_Missing_Data);
                        } else {
                            currentvalue = currentrow.get(valuename);
                        }
                        //if the information is not in the hometable then we should have gathered it already
                        //</editor-fold>
                    } else if (IfValues.containsKey(currentterm)) {
                        //<editor-fold defaultstate="collapsed" desc="comment">
                        int column = hometable.getPrimaryIndex();

                        Element current = IfValues.get(currentterm).get(String.valueOf(hometable.getValueAt(x, column)));
                        if (current != null && current.getAttribute("value") != null) {
                            String ifvalue = current.getAttributeValue("value");
                            for (Element currentchild : current.getChild("TERMS").getChildren()) {
                                String termname = currentchild.getName();
                                if (!currentrow.containsKey(termname)) {
                                    if (ifvalue.contains("TXT:" + termname)) {
                                        currentrow.put(currentchild.getName(), new EQValue(currentchild.getText(), "STRING"));
                                    } else if (ifvalue.contains("DATE:" + termname)) {
                                        currentrow.put(currentchild.getName(), new EQValue(currentchild.getAttributeValue("value"), "DATE"));
                                    }
                                }
                            }
                            currentvalue = new EQValue(ifvalue, "IFSTRING");
                        } else {
                            currentvalue = new EQValue("", "STRING", StatusList.EQ_Error_Value_Not_Found);
                        }
                        //</editor-fold>
                    } else {
                        currentvalue = new EQValue("", "STRING", StatusList.EQ_Error_Value_Not_Found);
                    }
                    if (!cureqstring.startsWith("`")) {
                        cureqstring = "`" + cureqstring;
                    }
                    if (currentvalue != null && currentvalue.GetStatus().equals(StatusList.EQ_Normal) && (currentvalue.GetType().equals("NUMBER") || currentvalue.GetType().equals("IFSTRING"))) {
                        cureqstring = cureqstring.replace("`" + currentterm, "`" + currentvalue);
                    }
                }

                if (!errortext.equals("skip")) {
                    int index = hometable.getPrimaryIndex();
                    String id = hometable.getValueAt(x, index).toString();

                    EQValue result;
                    if (!cureqstring.contains("null") && !cureqstring.contains("missing data") && !cureqstring.contains("Value not found")) {
                        result = MathHandling.Solve(cureqstring, currentrow, tvalues);

                    } else if (cureqstring.contains("Value not found")) {
                        result = new EQValue("", "", StatusList.EQ_Error_Value_Not_Found);
                    } else {
                        result = new EQValue("", "", StatusList.EQ_Error_Missing_Data);
                    }
                    results.put(id, result);
                }
            }
            return results;
        } catch (Exception e) {
            Global.Printmessage("Equation.CalculateValues code failed");
            Global.Printmessage(e.toString());
            Global.Printmessage(path + " " + equationname);
            return results;
        }
    }

    protected HashMap<String, Element> Evaluate(String term, HashMap<String, HashMap<String, EQValue>> Values) {
        TreeMap<String, HashMap<String, String>> EvalMap = makeevalmap(source.getChild("TERMS").getChild(term).getChild("TERMS"));
        HashMap<String, String> CommandMap = this.getcommandmap(term);
        HashMap<String, Element> results = new HashMap();
        Element termsource = source.getChild("TERMS").getChild(term);
        if (CommandMap.containsKey("EVAL")) {
            String command = CommandMap.get("EVAL");
            if (command != null && command.equals("IF")) {
                for (int x = 0; x < hometable.getRowCount(); x++) {
                    try {
                        HashMap<String, EQValue> currentrow = hometable.getEQRowMap(x, true);//creates a map of columnname>value for that row
                        Element result = evalif(EvalMap, currentrow, termsource);
                        results.put(currentrow.get(hometable.getPrimary()).toString(), result);
                    } catch (Exception e) {
                        Global.Printmessage("Equation.SolveFormula Evaluate");
                        Global.Printmessage(path + " " + equationname + " " + term + " ROW:" + x);
                    }
                }
            }
        }
        return results;
    }

    public Element evalif(TreeMap<String, HashMap<String, String>> EvalMap, HashMap<String, EQValue> row, Element termsource) {
        String resultkey = "DEFAULT";
        for (String key : EvalMap.keySet()) {
            if (key.startsWith("COND") && Global.isInteger(key.substring(4))) {
                try {
                    Element childterms = Prototype.StaticClasses.XMLHandling.getpath(new String[]{"TERMS", key, "TERMS"}, termsource);
                    HashMap<String, HashMap<String, EQValue>> Values = new HashMap();
                    String currentstatement = EvalMap.get(key).get("IF");
                    ArrayList<String> terms = Global.parseformula(currentstatement);

                    //<editor-fold defaultstate="collapsed" desc="Subequations">
                    while (currentstatement.contains("EVAL:")) {
                        for (String currentterm : terms) {
                            if (currentstatement.contains("EVAL:" + currentterm)) {
                                Element currentelement = termsource.getChild("TERMS").getChild(currentterm);
                                TreeMap<String, HashMap<String, String>> newevalmap = makeevalmap(termsource);
                                Element evalresult = evalif(newevalmap, row, currentelement);
                                if (evalresult != null && evalresult.getAttribute("value") != null) {
                                    String evalresultstring = evalresult.getAttributeValue("value");
                                    currentstatement = currentstatement.replace("EVAL:" + currentterm, evalresultstring);
                                    childterms.addContent(evalresult.getChildren());
                                }
                            }
                        }
                    }

                    while (currentstatement.contains("OP:")) {
                        //example: (n*OP:SUM1{Math.pow(b,2)}-Math.pow(OP:SUM2{b},2))/(n*(n-1))
                        for (String currentterm : terms) {
                            if (currentstatement.contains("OP:" + currentterm)) {
                                Element currentelement = termsource.getChild("TERMS").getChild(currentterm).clone();
                                HashMap<String, EQValue> subresults = solvesubformula(currentelement);
                                Values.put(currentterm, subresults);
                                currentstatement = currentstatement.replace("OP:" + currentterm, currentterm);
                            }
                        }
                    }
//</editor-fold>

                    if (childterms != null) {
                        Values.putAll(gatherdata(childterms));
                    }
                    String ID = hometable.getPrimary();
                    HashMap<String, EQValue> rowvalues = new HashMap();
                    for (String currentterm : terms) {
                        if (Values.containsKey(currentterm) && row.containsKey(ID) && Values.get(currentterm).get(row.get(ID).GetValue()) != null) {
                            EQValue IDvalue;
                            IDvalue = Values.get(currentterm).get(row.get(ID).GetValue());
                            rowvalues.put(currentterm, IDvalue);
                        } else if (row.containsKey(currentterm) && row.get(currentterm) != null) {
                            rowvalues.put(currentterm, row.get(currentterm));
                        }
                    }
                    if (MathHandling.Solve(currentstatement, rowvalues, tvalues).GetValue().equals("true")) {
                        resultkey = key;
                        break;

                    }
                } catch (Exception e) {
                    Global.Printmessage("Equation.evalif Subequation");
                    Global.Printmessage(path + " " + equationname + " " + key);
                }
            }
        }
        HashMap<String, String> resultmap = EvalMap.get(resultkey);
        try {
            HashMap<String, HashMap> Values = new HashMap();
            String currentstatement = resultmap.get("RESULT");
            ArrayList<String> terms = Global.parseformula(currentstatement);
            Element childterms = Prototype.StaticClasses.XMLHandling.getpath(new String[]{"TERMS", resultkey, "TERMS"}, termsource);

            while (currentstatement.contains("EVAL:")) {
                for (String currentterm : terms) {
                    if (currentstatement.contains("EVAL:" + currentterm)) {
                        Element currentelement = termsource.getChild("TERMS").getChild(currentterm);
                        TreeMap<String, HashMap<String, String>> newevalmap = makeevalmap(termsource);
                        Element evalresult = evalif(newevalmap, row, currentelement);
                        if (evalresult != null && evalresult.getAttribute("value") != null) {
                            String evalresultstring = evalresult.getAttributeValue("value");
                            currentstatement = currentstatement.replace("EVAL:" + currentterm, evalresultstring);
                            childterms.addContent(evalresult.getChildren());
                        }
                    }
                }
            }

            while (currentstatement.contains("OP:")) {
                //example: (n*OP:SUM1{Math.pow(b,2)}-Math.pow(OP:SUM2{b},2))/(n*(n-1))
                for (String currentterm : terms) {
                    if (currentstatement.contains("OP:" + currentterm)) {
                        Element currentelement = childterms.getChild(currentterm).clone();
                        HashMap<String, EQValue> subresults = solvesubformula(currentelement);
                        String currentresult;
                        currentresult = subresults.get(row.get(hometable.getPrimary()).GetValue()).GetValue();
                        Values.put(currentterm, subresults);
                        currentstatement = currentstatement.replace("OP:" + currentterm, currentresult);
                    }
                }
            }

            terms = Global.parseformula(currentstatement);
            if (childterms != null) {
                Values.putAll(gatherdata(childterms));

                Element Result = new Element("RESULT");
                Element ResultTerms = new Element("TERMS");
                for (String currentterm : terms) {
                    if (childterms.getChild(currentterm) != null && ResultTerms.getChild(currentterm) == null) {
                        ResultTerms.addContent(childterms.getChild(currentterm).clone());
                    }
                }
                Result.addContent(ResultTerms);
                Result.setAttribute("value", currentstatement);
                return Result;
            }
        } catch (Exception e) {
            Global.Printmessage("Equation.evalif Subequation");
            Global.Printmessage(path + " " + equationname + " " + resultkey);
        }
        return null;
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Solves">
    public HashMap<String, EQValue> solveformula() {
        HashMap<String, EQValue> results;
        HashMap<String, HashMap<String, Element>> IfValues = new HashMap();
        HashMap<String, HashMap<String, EQValue>> Values = new HashMap();
        String neweq = equationstring;
        ArrayList<String> remainingterms = (ArrayList<String>) listofterms.clone();
        //remove equation results from list of terms for gatherdata
        ArrayList<String> parselist = Global.getParseList();

        for (String currentterm : listofterms) {
            if (parselist.contains(currentterm) || currentterm.startsWith("FUNCTION:") || currentterm.startsWith("CONSTANT")) {
                remainingterms.remove(currentterm);
            }
            //<editor-fold defaultstate="collapsed" desc="TValue">
            if (currentterm.startsWith("FUNCTION:TVAL:")) {
                String termname = currentterm.substring(14);
                if (AllCommandMaps.containsKey(termname)) {
                    tvalues.put(termname, AllCommandMaps.get(termname));
                }

            }
//</editor-fold>
        }
        //EQSolve 2 here is where we handle any operations as they get treated like subequations
        //<editor-fold defaultstate="collapsed" desc="OP">
        while (neweq.contains("OP:")) {
            //example: (n*OP:SUM1{Math.pow(b,2)}-Math.pow(OP:SUM2{b},2))/(n*(n-1))

            for (String currentterm : listofterms) {
                try {
                    if (neweq.contains("OP:" + currentterm)) {
                        Element currentelement = source.getChild("TERMS").getChild(currentterm).clone();
                        HashMap<String, EQValue> subresults = solvesubformula(currentelement);

                        Values.put(currentterm, subresults);
                        neweq = neweq.replace("OP:" + currentterm, currentterm);
                        remainingterms.remove(currentterm);
                    }
                } catch (Exception e) {
                    Global.Printmessage("Equation.SolveFormula Subequation");
                    Global.Printmessage(path + " " + equationname + " " + currentterm);
                }
            }
        }
//</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="EVAL">
        while (neweq.contains("EVAL:")) {
            for (String currentterm : listofterms) {
                try {
                    if (neweq.contains("EVAL:" + currentterm)) {
                        HashMap<String, Element> subresults = Evaluate(currentterm, Values);
                        String optext = "EVAL:" + currentterm;
                        IfValues.put(currentterm, subresults);
                        neweq = neweq.replace(optext, currentterm);
                        remainingterms.remove(currentterm);
                    }
                } catch (Exception e) {
                    Global.Printmessage("Equation.SolveFormula Evaluation");
                    Global.Printmessage(path + " " + equationname + " " + currentterm);
                }
            }
        }
//</editor-fold>

        Values.putAll(gatherdata(remainingterms));

        results = calculatevalues(neweq, Values, IfValues);
        return results;
    }

    //this procedure takes operations like sum or highest and solves what's in the brackets for that operation to be done later.
    protected HashMap<String, EQValue> solvesubformula(Element Termsource) {
        HashMap<String, Boolean> locations = new HashMap();
        HashMap<String, EQValue> results = new HashMap();
        HashMap<String, String> opMap = Global.makecommandmap(Termsource.getAttributeValue("value"));
        HashMap<String, HashMap<String, String>> NewCommandMaps = new HashMap();

        //code of the subequation
        String subformula = opMap.get("EQ");
        String ifcode = opMap.get("IF");
        String matchcode = opMap.get("MATCH");
        String orderby = opMap.get("ORDERBY");
        String Op = opMap.get("OP");
        //list of terms
        Element oldterms = Termsource.getChild("TERMS");

        if (Op.equals("COUNT") && oldterms != null) {

            subformula = opMap.get("VALUE");

            String grouptext = opMap.get("GROUPBY");
            if (grouptext != null & oldterms.getChild(grouptext) == null) {
                Element groupxml = new Element(grouptext);
                groupxml.setAttribute("value", "PATH:" + opMap.get("PATH") + ";VALUE:" + grouptext + ";");
                oldterms.addContent(groupxml);
            }
            String valuetext = opMap.get("VALUE");
            if (valuetext != null && oldterms.getChild(valuetext) == null) {
                Element valuexml = new Element(valuetext);
                valuexml.setAttribute("value", "PATH:" + opMap.get("PATH") + ";VALUE:" + valuetext + ";");
                oldterms.addContent(valuexml);
            }
            Termsource.removeChild("TERMS");
            Termsource.addContent(oldterms);
        }
        ArrayList<String> subtermlist = Global.parseformula(subformula);
        if (ifcode != null && !ifcode.isEmpty()) {
            subtermlist.addAll(Global.parseformula(ifcode));
        }
        if (orderby != null && !orderby.isEmpty()) {
            subtermlist.addAll(Global.parseformula(orderby));
        }
        if (matchcode != null && !matchcode.isEmpty()) {
            subtermlist.addAll(Global.parseformula(matchcode));
        }

        ArrayList groupby = new ArrayList();
        DatabaseTableModel subhome = new DatabaseTableModel(null, null);

        try {
            if (!opMap.containsKey("MATCH") && !opMap.containsKey("GROUPBY") && !Op.equals("PREVIOUS")) {
                groupby.add("*ALL*");
            } else {
                groupby = hometable.getColumn(hometable.getPrimary());
            }

            if (opMap.get("PATH") != null && !opMap.get("PATH").isEmpty()) {
                String[] pathsplit = opMap.get("PATH").split(",");
                String tablename = pathsplit[pathsplit.length - 1];
                subhome = Prototype.StaticClasses.TableHandling.getDatabaseTableModel(tablename);
                if (subhome.hascolumn(opMap.get("GROUPBY"))) {
                    locations.put(opMap.get("PATH"), true);
                } else {
                    locations.put(opMap.get("PATH"), false);
                }
            }
        } catch (Exception e) {
            Global.Printmessage("Equation.SolveFormula solveSubformula part 1");
            Global.Printmessage(path + " " + equationname);
        }

        if (oldterms != null) {
            for (String subterm : subtermlist) {
                if (oldterms.getChild(subterm) != null) {
                    NewCommandMaps.put(subterm, Global.makecommandmap(oldterms.getChild(subterm).getAttributeValue("value")));
                }
            }
        }
        //for each group of values (may be all one group)
        for (Object value : groupby) {
            Element newterms = new Element("TERMS");

            if (Termsource.getChild("TERMS") != null) {
                for (String subterm : subtermlist) {
                    if (Termsource.getChild("TERMS").getChild(subterm) != null) {
                        try {
                            //get hometable from path, get location of each relevant term, if that location has the groupby column then add the if statement.
                            Element newterm = Termsource.getChild("TERMS").getChild(subterm).clone();
                            HashMap<String, String> currentmap = (HashMap<String, String>) NewCommandMaps.get(subterm).clone();
                            if (!subformula.contains("OP:" + subterm) && !subformula.contains("EVAL:" + subterm)) {
                                String currentpath = currentmap.get("PATH");
                                //so if we haven't fetched the location previously we get it now, done this way so we don't do searches more than we need to
                                if (!locations.containsKey(currentpath)) {
                                    String[] pathsplit = currentpath.split(",");
                                    String tablename = pathsplit[1];
                                    DatabaseTableModel currenttable = Prototype.StaticClasses.TableHandling.getDatabaseTableModel(tablename);
                                    if (opMap.containsKey("GROUPBY") && currenttable != null && currenttable.hascolumn(opMap.get("GROUPBY"))) {
                                        locations.put(currentpath, true);
                                    } else {
                                        locations.put(currentpath, false);
                                    }

                                }

                                //currently this adds filtering to match the foreign key in the tbale so that it groups the values.
                                if (locations.get(currentpath) != null && locations.get(currentpath) && !value.equals("*ALL*")) {
                                    if (currentmap.containsKey("IF") && currentmap.get("IF") != null && !currentmap.get("IF").equals("")) {
                                        String ifvalue = "(" + currentmap.get("IF") + ")&&" + hometable.getPrimary() + "==" + value.toString();
                                        currentmap.put("IF", ifvalue);
                                    } else {
                                        currentmap.put("IF", hometable.getPrimary() + "==" + value.toString());
                                    }
                                }
                                newterm.setAttribute("value", Global.MakeCommandString(currentmap));
                                newterms.addContent(newterm);
                            } else {
                                newterms.addContent(newterm);
                            }
                        } catch (Exception e) {
                            Global.Printmessage("Equation.solvesubformula solvesubformula");
                            Global.Printmessage(path + " " + equationname + " " + subterm + " " + value);
                        }
                    }
                }
            }
            Element newequation = new Element(equationname);
            if (opMap.containsKey("EQ") && opMap.get("EQ") != null) {
                newequation.setAttribute("value", opMap.get("EQ"));
            }else if(Op.equals("COUNT"))
            {
                newequation.setAttribute("value", opMap.get("VALUE"));
            }

            newequation.addContent(newterms);
            if (opMap.get("OP").equals("PREVIOUS")) {
                opMap.put("ID", value.toString());
                subhome = hometable;
            }
            //here is where the actual operation is performed once we've got the values to total/average etc
            Equation subequation = new Equation(newequation, subhome, opMap);//this line of code is the end result of the subformula, we've taken it and made it match an equation and now we solve it
            //then the next step is to do the op
            EQValue result = subequation.SolveOP();

            results.put(value.toString(), result);

        }
        return results;
    }
    //</editor-fold>

    //this procedure is used to solve operations like sum and mean.
    public EQValue SolveOP() {
        if (opmode) {
            String Op = OpMap.get("OP");
            //<editor-fold defaultstate="collapsed" desc="Prep">
            HashMap<String, HashMap<String, Element>> IfValues = new HashMap();
            HashMap<String, HashMap<String, EQValue>> Values = new HashMap();
            String neweq = equationstring;
            ArrayList<String> remainingterms = (ArrayList<String>) listofterms.clone();
            //remove equation results from list of terms for gatherdata
            ArrayList<String> parselist = Global.getParseList();

            for (String currentterm : listofterms) {
                if (parselist.contains(currentterm) || currentterm.startsWith("FUNCTION:") || currentterm.startsWith("CONSTANT")) {
                    remainingterms.remove(currentterm);
                }
                //<editor-fold defaultstate="collapsed" desc="TValue">
                if (currentterm.startsWith("FUNCTION:TVAL:")) {
                    String termname = currentterm.substring(14);
                    if (AllCommandMaps.containsKey(termname)) {
                        tvalues.put(termname, AllCommandMaps.get(termname));
                    }

                }
//</editor-fold>
            }
            //EQSolve 2 here is where we handle any operations as they get treated like subequations
            //<editor-fold defaultstate="collapsed" desc="OP">
            while (neweq.contains("OP:")) {
                //example: (n*OP:SUM1{Math.pow(b,2)}-Math.pow(OP:SUM2{b},2))/(n*(n-1))

                for (String currentterm : listofterms) {
                    try {
                        if (neweq.contains("OP:" + currentterm)) {
                            Element currentelement = source.getChild("TERMS").getChild(currentterm).clone();
                            HashMap<String, EQValue> subresults = solvesubformula(currentelement);

                            Values.put(currentterm, subresults);
                            neweq = neweq.replace("OP:" + currentterm, currentterm);
                            remainingterms.remove(currentterm);
                        }
                    } catch (Exception e) {
                        Global.Printmessage("Equation.SolveFormula Subequation");
                        Global.Printmessage(path + " " + equationname + " " + currentterm);
                    }
                }
            }
//</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="EVAL">
            while (neweq.contains("EVAL:")) {
                for (String currentterm : listofterms) {
                    try {
                        if (neweq.contains("EVAL:" + currentterm)) {
                            HashMap<String, Element> subresults = Evaluate(currentterm, Values);
                            String optext = "EVAL:" + currentterm;
                            IfValues.put(currentterm, subresults);
                            neweq = neweq.replace(optext, currentterm);
                            remainingterms.remove(currentterm);
                        }
                    } catch (Exception e) {
                        Global.Printmessage("Equation.SolveFormula Evaluation");
                        Global.Printmessage(path + " " + equationname + " " + currentterm);
                    }
                }
            }
//</editor-fold>

            Values.putAll(gatherdata(remainingterms));
            ArrayList<String> newlist = Global.parseformula(neweq);
//</editor-fold>
            String previd;
            HashMap<String, Object> matchvalues = new HashMap();
            if (Op.equals("PREVIOUS")) //<editor-fold defaultstate="collapsed" desc="Previous Operation">
            {
                previd = OpMap.get("ID");
                String match = OpMap.get("MATCH");
                if (match == null) {
                    match = "";
                }
                Integer rownumber = hometable.getRowNumberFromID(previd);
                if (rownumber != null) {
                    HashMap idrow = hometable.getrowmap(rownumber);
                    for (String matchvalue : match.split("&&")) {
                        if (Values.containsKey(matchvalue)) {
                            matchvalues.put(matchvalue, Values.get(matchvalue).get(previd));
                        } else {
                            matchvalues.put(matchvalue, idrow.get(matchvalue));
                        }
                    }
                }
            }
//</editor-fold>

            ArrayList<ArrayList<Comparable>> oprows = new ArrayList();
            HashMap<String, String> opvalues = new HashMap();
            try {
                String primname = hometable.getPrimary();
                for (int x = 0; x < hometable.getRowCount(); x++) {//for each row of the table
                    String errortext = "";
                    String cureqstring = neweq;
                    HashMap<String, EQValue> currentrow = hometable.getEQRowMap(x, true);//creates a map of columnname>value for that row
                    String rowid = currentrow.get(primname).GetValue();
                    for (String currentterm : newlist) {
                        String currentvalue = "null";
                        HashMap<String, String> currentmap = this.getcommandmap(currentterm);
                        //if this term is in the hometable we get the information here directly
                        if (currentmap.containsKey("PATH") && path.equals(Global.getcurrentschema() + "," + currentmap.get("PATH")))//if the term is in the hometable
                        //<editor-fold defaultstate="collapsed" desc="Hometable columns">
                        {
                            if (currentmap.containsKey("IF") && !currentmap.get("IF").equals("")) {
                                String currentpath = "PROJECTS," + Global.getcurrentschema() + "," + currentmap.get("PATH") + ",EQUATIONS," + currentmap.get("VALUE") + ",TERMS";
                                Element TERMS = Prototype.StaticClasses.XMLHandling.getpath(currentpath.split(","), Global.getxmlfilename());

                                String Filter = currentmap.get("IF");
                                if (TERMS != null) {
                                    for (Element txtterm : TERMS.getChildren()) {
                                        String name = txtterm.getName();
                                        if (Filter.contains("TXT:" + name) && !currentrow.containsKey(name)) {
                                            currentrow.put(name, new EQValue(txtterm.getText(), "STRING"));
                                        } else if (Filter.contains("DATE:" + name) && !currentrow.containsKey(name)) {
                                            currentrow.put(name, new EQValue(txtterm.getAttributeValue("value"), "DATE"));
                                        }
                                    }
                                }
                                if (Op.equals("PREVIOUS")) {
                                    for (String column : matchvalues.keySet()) {
                                        if (!currentrow.containsKey(column)) {
                                            errortext = "skip";
                                            break;
                                        }
                                        Object value = currentrow.get(column);
                                        if (!(matchvalues.get(column) == null && value == null) || !(matchvalues.get(column).equals(value))) {
                                            errortext = "skip";
                                            break;
                                        }
                                    }
                                } else if (!MathHandling.Solve(Filter, currentrow, tvalues).GetValue().equals("true")) {
                                    errortext = "skip";
                                    break;
                                }
                            }
//</editor-fold>

                            String valuename = currentmap.get("VALUE");
                            if (currentrow.get(valuename) == null) {
                                currentvalue = "null";
                            } else {
                                currentvalue = currentrow.get(valuename).GetValue();
                            }
                            //<editor-fold defaultstate="collapsed" desc="comment">

                            //if the information is not in the hometable then we should have gathered it already
                        } else if (Values.containsKey(currentterm)) {//if we fetched the information from outside the table already
                            /*
                             so this is getting the values out of the value hashmap. 
                             option a: linked to other table and paired using the match value. getting the value of the foreign key for that table
                             option b: performed operation and pairing up the results using the groupby value to match a foreign key
                             option c: performed operation and using the same result for all values.
                             */
                            String key = "MATCH";
                            if (currentmap.containsKey("OP") && currentmap.containsKey("GROUPBY"))//if the term is an operation we use the groupby value instead
                            {
                                key = "GROUPBY";
                            }
                            if (Values.get(currentterm).containsKey("*ALL*")) {
                                currentvalue = Values.get(currentterm).get("*ALL*").GetValue();
                            } else if (currentmap.containsKey(key) && !currentmap.get(key).equals("")) {
                                String Column = currentmap.get(key);
                                String ColumnValue = currentrow.get(Column).GetValue();
                                if (hometable.isforeignkey(Column)) {
                                    String[] valsplit = ColumnValue.split(":");
                                    ColumnValue = valsplit[0];
                                }
                                if (Values.get(currentterm).containsKey(ColumnValue)) {
                                    currentvalue = Values.get(currentterm).get(ColumnValue).GetValue();
                                }
                            } else {
                                errortext = "missing " + key + " instruction in: " + currentterm;
                            }
//</editor-fold>
                        } else if (IfValues.containsKey(currentterm)) {
                            //<editor-fold defaultstate="collapsed" desc="comment">
                            int column = hometable.getPrimaryIndex();

                            Element current = IfValues.get(currentterm).get(String.valueOf(hometable.getValueAt(x, column)));
                            if (current != null && current.getAttribute("value") != null) {
                                String ifvalue = current.getAttributeValue("value");
                                currentvalue = ifvalue;

                                for (Element currentchild : current.getChild("TERMS").getChildren()) {
                                    String termname = currentchild.getName();
                                    if (!currentrow.containsKey(termname)) {
                                        if (currentvalue.contains("TXT:" + termname)) {
                                            currentrow.put(currentchild.getName(), new EQValue(currentchild.getText(), "STRING"));
                                        } else if (currentvalue.contains("DATE:" + termname)) {
                                            currentrow.put(currentchild.getName(), new EQValue(currentchild.getAttributeValue("value"), "DATE"));
                                        }
                                    }
                                }

                            } else {
                                currentvalue = "`Value not found";
                            }
//</editor-fold>
                        } else {
                            currentvalue = "`Value not found";
                        }
                        if (!cureqstring.startsWith("`")) {
                            cureqstring = "`" + cureqstring;
                        }
                        cureqstring = cureqstring.replace("`" + currentterm, "`" + currentvalue);
                    }

                    if (!errortext.equals("skip")) {
                        int index = hometable.getPrimaryIndex();
                        String id = hometable.getValueAt(x, index).toString();

                        String result;
                        String resultclass;
                        if (!cureqstring.contains("null") && !cureqstring.contains("missing data") && !cureqstring.contains("Value not found")) {
                            if (Op.equals("PREVIOUS")) {
                                String column = OpMap.get("VALUE");
                                result = currentrow.get(column).GetValue();
                                resultclass = currentrow.get(column).GetType();

                            } else {
                                EQValue resulteq = MathHandling.Solve(cureqstring, currentrow, tvalues);
                                result = resulteq.GetValue();
                                resultclass = resulteq.GetType();
                            }
                            switch (Op) {
                                case "HIGHEST":
                                case "LOWEST":
                                    //<editor-fold defaultstate="collapsed" desc="high, low">
                                    String orderby = OpMap.get("ORDERBY");
                                    ArrayList gridrow = new ArrayList();
                                    if (orderby != null && !orderby.isEmpty()) {
                                        String[] sorting = orderby.split("`");
                                        for (String currentcolumn : sorting) {
                                            if (!currentcolumn.isEmpty() && Values.containsKey(currentcolumn)) {
                                                EQValue EQV = Values.get(currentcolumn).get(rowid);
                                                if (EQV != null && EQV.GetComplete()) {
                                                    String value = EQV.GetValue();
                                                    String type = EQV.GetType();
                                                    switch (type) {
                                                        case "STRING":
                                                            gridrow.add(value);
                                                            break;
                                                        case "NUMBER":
                                                            if (Global.isNumeric(value)) {
                                                                gridrow.add(Double.valueOf(value));
                                                            }
                                                            break;
                                                        case "DATE":
                                                            if (Global.isDate(value)) {
                                                                gridrow.add(java.sql.Date.valueOf(value));
                                                            }
                                                            break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    switch (resultclass) {
                                        case "STRING":
                                            gridrow.add(result);
                                            break;
                                        case "NUMBER":
                                            if (Global.isNumeric(result)) {
                                                gridrow.add(Double.valueOf(result));
                                            } else if (result == null) {
                                                gridrow.add(null);
                                            }
                                            break;
                                        case "DATE":
                                            if (Global.isDate(result)) {
                                                gridrow.add(java.sql.Date.valueOf(result));
                                            }
                                            if (result == null) {
                                                gridrow.add(null);
                                            }
                                            break;
                                    }

                                    oprows.add(gridrow);
//</editor-fold>
                                    break;
                                case "PREVIOUS":
                                    //<editor-fold defaultstate="collapsed" desc="previous">
                                    String orderby2 = OpMap.get("ORDERBY");
                                    if (orderby2 != null && !orderby2.isEmpty()) {
                                        ArrayList prevgridrow = new ArrayList();
                                        String[] sorting = orderby2.split("`");
                                        for (String currentcolumn : sorting) {
                                            EQValue currentvalue = currentrow.get(currentcolumn);
                                            String valuestring = currentvalue.GetValue();
                                            switch (currentvalue.GetType()) {
                                                case "STRING":
                                                    prevgridrow.add(valuestring);
                                                    break;
                                                case "NUMBER":
                                                    if (Global.isNumeric(valuestring)) {
                                                        prevgridrow.add(Double.valueOf(valuestring));
                                                    } else if (result == null) {
                                                        prevgridrow.add(null);
                                                    }
                                                    break;
                                                case "DATE":
                                                    if (Global.isDate(valuestring)) {
                                                        prevgridrow.add(java.sql.Date.valueOf(valuestring));
                                                    } else if (result == null) {
                                                        prevgridrow.add(null);
                                                    }
                                                    break;
                                            }
                                        }
                                        if (Global.isNumeric(id)) {
                                            prevgridrow.add(Double.valueOf(id));
                                        } else {
                                            prevgridrow.add(id);
                                        }

                                        switch (resultclass) {
                                            case "STRING":
                                                prevgridrow.add(result);
                                                break;
                                            case "NUMBER":
                                                if (Global.isNumeric(result)) {
                                                    prevgridrow.add(Double.valueOf(result));
                                                } else if (result == null) {
                                                    prevgridrow.add(null);
                                                }
                                                break;
                                            case "DATE":
                                                if (Global.isDate(result)) {
                                                    prevgridrow.add(java.sql.Date.valueOf(result));
                                                } else if (result == null) {
                                                    prevgridrow.add(null);
                                                }
                                                break;
                                        }
                                        oprows.add(prevgridrow);
                                        /*this creates a grid where each row goes sorting columne>id>value 
                                         we want to get the value of the column based on that sorting but that could result in
                                         multiple valid answers so sorting by ID at least creates predictable results.
                                         */
                                    }
//</editor-fold>
                                    break;
                                case "SUM":
                                case "AVERAGE":
                                case "MEAN":
                                case "COUNT":
                                    opvalues.put(id, result);
                                    break;
                            }
                        }
                    }
                }
                switch (Op) {
                    case "HIGHEST":
                    case "LOWEST":
                        return MathHandling.FetchHighLow(oprows, Op);
                    case "PREVIOUS":
                        EQValue EQResult = MathHandling.FetchPrevNext(oprows, Integer.valueOf(OpMap.get("ID")), OpMap.get("ORDER"));
                        if (!EQResult.GetComplete() && EQResult.GetStatus() != null && EQResult.GetStatus().equals(StatusList.EQ_Error_No_Prev_Value)) {
                            String Defvalue = OpMap.get("DEFAULT");
                            if (Defvalue == null) {
                                EQResult.SetStatus("ERROR:Missing Default Value Instruction");
                            } else if (Defvalue.equals("SAME")) {
                                EQResult.SetStatus(StatusList.EQ_Normal);
                            } else if (Global.isNumeric(Defvalue)) {
                                EQResult.SetStatus(StatusList.EQ_Normal);
                                EQResult.SetType("NUMBER");
                                EQResult.SetValue(Defvalue);
                            } else if (Defvalue.equals("STRING") || Defvalue.equals("DATE")) {
                                String value = Prototype.StaticClasses.XMLHandling.getAttValue(new String[]{"TERMS", Defvalue}, primname, source);
                                if (value != null) {
                                    EQResult.SetStatus(StatusList.EQ_Normal);
                                    EQResult.SetType(Defvalue);
                                    EQResult.SetValue(value);
                                } else {
                                    EQResult.SetStatus("Error: No Default Value Found");
                                }
                            }
                        }

                        return EQResult;
                    default:
                        return MathHandling.PerformBasicOp(Op, opvalues);
                }

            } catch (NumberFormatException e) {
                Global.Printmessage("Equation.CalculateValues code failed");
                Global.Printmessage(e.toString());
                Global.Printmessage(path + " " + equationname);
            }
        }
        return null;
    }
}
