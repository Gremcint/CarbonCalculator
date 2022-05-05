/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Prototype.StaticClasses;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
//xml handling library
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.Attribute;
import java.util.ArrayList;
import java.util.List;
import org.jdom2.JDOMException;

/**
 *
 * @author User
 */
public class XMLHandling {
    //<editor-fold defaultstate="collapsed" desc="Create File">

    /*
     * This will create an XML file if it does not already exist, it will not
     * overwrite an existing file. It expects the full filename with the
     * extension.
     */
    public static boolean CreateFile(String Filename) {
        if (!Global.CheckForFile(Filename)) {
            try {
                Global.Printmessage(Filename + " does not exist, attempting to create new file.");
                Element root = new Element("ROOT");
                Document doc = new Document(root);
                XMLOutputter xmlOutput = new XMLOutputter();
                xmlOutput.setFormat(Format.getPrettyFormat());
                xmlOutput.output(doc, new FileWriter(Filename));
                Global.Printmessage("File " + Filename + " sucessfully created.");
                return true;
            } catch (IOException io) {
                Global.Printmessage("XML Create File" + io.getMessage());
                return false;
            }
        }
        return false;
    }

    public static boolean CreateFile(String Filename, Boolean Overwrite) {
        if (!Global.CheckForFile(Filename)) {
            try {
                Global.Printmessage(Filename + " does not exist, attempting to create new file.");
                Element root = new Element("ROOT");
                Document doc = new Document(root);
                doc.setRootElement(root);
                XMLOutputter xmlOutput = new XMLOutputter();
                xmlOutput.setFormat(Format.getPrettyFormat());
                xmlOutput.output(doc, new FileWriter(Filename));
                Global.Printmessage("File " + Filename + " sucessfully created.");
                return true;
            } catch (IOException io) {
                Global.Printmessage("XML Create File Overwrite 1" + io.getMessage());
                return false;
            }
        } else if (Overwrite) {
            try {
                Global.Printmessage(Filename + " exists, overwriting.");
                Element root = new Element("ROOT");
                Document doc = new Document(root);
                doc.setRootElement(root);
                XMLOutputter xmlOutput = new XMLOutputter();
                xmlOutput.setFormat(Format.getPrettyFormat());
                xmlOutput.output(doc, new FileWriter(Filename));
                Global.Printmessage("File " + Filename + " sucessfully overwritten.");
                return true;
            } catch (IOException io) {
                Global.Printmessage("XML Create File Overwrite 2" + io.getMessage());
                return false;
            }
        }
        Global.Printmessage(Filename + "already exists cannot/will not overwrite.");
        return false;

    }
    //</editor-fold>

    public ArrayList<String> getlistofchildren(Element source)
    {
        //this is a really simple procedure but it's one I keep having to do 
        //so might as well automate.
        ArrayList<String> names=new ArrayList();     
        for(Element current:source.getChildren())
        {
            names.add(current.getName());
        }
        return names;
    }
    
    static class xmlcomparator implements java.util.Comparator<Element>
    {
        @Override
        public int compare(Element e1,Element e2)
        {
            return e1.getName().compareTo(e2.getName());
        }
    }
    
    public static void sortchildren(Element source)
    {
        source.sortChildren(new xmlcomparator());
    }
    
    public static void checkduplicates(Element source) {

        ArrayList<String> Children = new ArrayList();
        ArrayList<String> Duplicates = new ArrayList();
        ArrayList<Element> duplicatexml = new ArrayList();

        for (Element currentchild : source.getChildren()) {
            String currentname = currentchild.getName();
            if (!Children.contains(currentname)) {
                Children.add(currentname);
            } else if (!Duplicates.contains(currentname)) {
                duplicatexml.addAll(currentchild.getChildren(currentname));
                Duplicates.add(currentname);
            }
        }
        for (String currentduplicate : Duplicates) {
            source.removeChildren(currentduplicate);
        }

    }

    /*
     add remove get element
     */
    public static void changeElementName(String[] path, String newname, String filename) {
        try {
            SAXBuilder builder = new SAXBuilder();
            File file = new File(filename);
            Document xmlnodes = (Document) builder.build(file);
            Element rootNode = xmlnodes.getRootElement();
            Element target = XMLHandling.getpath(path, rootNode);
            target.setName(newname);
            writetofile(xmlnodes, filename);
        } catch (JDOMException | IOException e) {
            Global.Printmessage("XML AddPath " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public static String getAttValue(String[] path, String attname, Element source) {
        Element target = getpath(path, source);
        if (target == null) {
            return null;
        }
        String value = target.getAttributeValue(attname);
        return value;
    }

    public static ArrayList<String> getchildlist(Element Source) {
        ArrayList<String> list = new ArrayList();
        List<Element> children = Source.getChildren();
        for (Element current : children) {
            list.add(current.getName());
        }
        return list;
    }

    public static boolean writetofile(Document newcontent, String filename) {
        try {
            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getPrettyFormat());
            xmlOutput.output(newcontent, new FileWriter(filename));
            return true;
        } catch (IOException io) {
            Global.Printmessage(io.getMessage());
            Global.Printmessage("XML Writetofile Unable to write to: " + filename);
            return false;
        }
    }

    public static void deletepath(String[] Path, String filename) {
        try {
            SAXBuilder builder = new SAXBuilder();
            if (!Global.CheckForFile(filename)) {
                return;
            }
            File file = new File(filename);
            Document xmlnodes = (Document) builder.build(file);
            Element rootNode = xmlnodes.getRootElement();
            Element currentNode = rootNode;
            for (String nodepath : Path) {
                if (currentNode.getName().equals(nodepath)) {
                } else if (currentNode.getChild(nodepath) == null) {
                    return;
                } else {
                    currentNode = currentNode.getChild(nodepath);
                }
            }
            Element ParentNode = currentNode.getParentElement();
            ParentNode.removeChild(currentNode.getName());
            writetofile(xmlnodes, Global.getxmlfilename());
        } catch (JDOMException | IOException e) {
            Global.Printmessage("XML AddPath " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public static void deleteAttribute(String[] Path, String filename, String attribute) {
        try {
            SAXBuilder builder = new SAXBuilder();
            if (!Global.CheckForFile(filename)) {
                return;
            }
            File file = new File(filename);
            Document xmlnodes = (Document) builder.build(file);
            Element rootNode = xmlnodes.getRootElement();
            Element currentNode = XMLHandling.getpath(Path, rootNode);
            if (currentNode == null) {
                return;
            }
            currentNode.removeAttribute(attribute);
            XMLHandling.writetofile(xmlnodes, filename);
        } catch (JDOMException | IOException e) {
            Global.Printmessage("XML AddPath " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public static void addtofile(Element newchild, String filename) {
        try {
            SAXBuilder builder = new SAXBuilder();
            if (!Global.CheckForFile(filename)) {
                XMLHandling.CreateFile(filename);
            }
            File file = new File(filename);
            Document xmlnodes = (Document) builder.build(file);
            Element rootNode = xmlnodes.getRootElement();
            rootNode.addContent(newchild);
            writetofile(xmlnodes, filename);
        } catch (JDOMException | IOException e) {
            Global.Printmessage("XML AddPath " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public static void addpath(String[] Path, Element newChild, String filename, boolean replace) {
        try {
            SAXBuilder builder = new SAXBuilder();
            if (!Global.CheckForFile(filename)) {
                XMLHandling.CreateFile(filename);
            }
            File file = new File(filename);
            Document xmlnodes = (Document) builder.build(file);
            Element rootNode = xmlnodes.getRootElement();
            Element currentNode = rootNode;
            for (String nodePath : Path) {
                if (currentNode.getName().equals(nodePath)) {
                } else if (currentNode.getChild(nodePath) == null) {
                    currentNode.addContent(new Element(nodePath));
                    currentNode = currentNode.getChild(nodePath);
                } else {
                    currentNode = currentNode.getChild(nodePath);
                }
            }

            if (currentNode.getChild(newChild.getName()) == null) {
                currentNode.addContent(newChild.clone());
            } else if (replace == true) {
                currentNode.removeChild(newChild.getName());
                currentNode.addContent(newChild.clone());
            }

            writetofile(xmlnodes, filename);

        } catch (JDOMException | IOException e) {
            Global.Printmessage("XML Add Path with child " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public static Element pathtoxml(String[] Path) {
        Element topnode = new Element(Path[0]);
        Element currentNode = topnode;
        for (int x = 1; x < Path.length; x++) {
            currentNode.addContent(new Element(Path[x]));
            currentNode = currentNode.getChild(Path[x]);
        }
        return topnode;
    }

    public static Element getpath(String[] Path, Element source) {
       if(source!=null){ Element current = source;
        for (String currentnode : Path) {
            current = current.getChild(currentnode);
            if (current == null) {
                return null;
            }
        }
        return current;
    }
       return null;
    }
    public static Element getpath(String[] Path, String filename) {
        try {
            SAXBuilder builder = new SAXBuilder();
            if (!Global.CheckForFile(filename)) {
                return null;
            }
            File file = new File(filename);
            Document xmlnodes = (Document) builder.build(file);
            Element rootNode = xmlnodes.getRootElement();
            Element currentNode = XMLHandling.getpath(Path, rootNode);
            return currentNode;
        } catch (JDOMException | IOException e) {
            Global.Printmessage("XML Get Path " + e.getClass().getName() + ": " + e.getMessage());
        }
        return null;
    }

    public static boolean haschild(Element source, String childname) {
        return source.getChild(childname) != null;
    }

    public static boolean hasattribute(Element source, String attributename) {
        return source.getAttributeValue(attributename) != null;
    }

    public static void setattribute(String[] Path, String filename, Attribute att) {
        try {
            SAXBuilder builder = new SAXBuilder();
            if (!Global.CheckForFile(filename)) {
                XMLHandling.CreateFile(filename);
            }
            File file = new File(filename);
            Document xmlnodes = (Document) builder.build(file);
            Element rootNode = xmlnodes.getRootElement();
            Element currentNode = rootNode;
            for (String nodepath : Path) {
                if (currentNode.getName().equals(nodepath)) {
                } else if (currentNode.getChild(nodepath) == null) {
                    currentNode.addContent(new Element(nodepath));
                    currentNode = currentNode.getChild(nodepath);
                } else {
                    currentNode = currentNode.getChild(nodepath);
                }
            }
            currentNode.setAttribute(att);
            writetofile(xmlnodes, filename);
        } catch (JDOMException | IOException e) {
            Global.Printmessage("XML AddPath " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

}
/*  

 public static void addpath(String[] Path, String filename, Document xmlnodes, boolean write) {
 Element rootNode = xmlnodes.getRootElement();
 Element currentNode = rootNode;
 for (int x = 0; x < Path.length; x++) {
 if (currentNode.getName().equals(Path[x])) {
 } else if (currentNode.getChild(Path[x]) == null) {
 currentNode.addContent(new Element(Path[x]));
 currentNode = currentNode.getChild(Path[x]);
 } else {
 currentNode = currentNode.getChild(Path[x]);
 }
 }
 if (write) {
 writetofile(xmlnodes, filename);
 }
 }
 */
