/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Prototype.StaticClasses;

import java.util.ArrayList;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.DriverManager;
import java.sql.DatabaseMetaData;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import org.jdom2.Element;
import java.io.File;

public class SQLHandling {

    public static void createschema(String schemaname) {
        Connection c = null;
        try {
            c = Global.getConnectionPool().getConnection();
            Statement stmt = c.createStatement();
            stmt.executeUpdate("CREATE SCHEMA " + schemaname);
        } catch (Exception e) {System.out.println(e.getMessage());
        int x=1;
        }
    }

    public static void createnewdb(String Filename) {
        File db = new File(Filename);
        db.delete();
        try {
            if (Filename.endsWith(".h2.db")) {
                Filename = Filename.substring(0, Filename.length() - 6);
            }
            Class.forName("org.h2.Driver");
            Connection conn = DriverManager.getConnection("jdbc:h2:" + Filename,"sa","");
            conn.close();
        } catch (Exception e) {
        } finally {
            try {
            } catch (Exception e) {
            }
        }
    }

    public static void deleteschema(String Schema) {
        ArrayList<String> tables = SQLHandling.getlistoftables(Schema);
        Connection c = null;
        try {
            if (tables != null && tables.size() > 0) {
                c = Global.getConnectionPool().getConnection();
                Statement stmt = c.createStatement();
                for (String table : tables) {
                    stmt.executeUpdate("DROP TABLE " + Schema + "." + table + " CASCADE;");
                }
                stmt.executeUpdate("DROP SCHEMA " + Schema);
            }
        } catch (Exception e) {
        }
    }

    public static ArrayList<String> getlistofschemas() {
        ArrayList<String> schemalist = new ArrayList();
        Connection c = null;
        try {
            c = Global.getConnectionPool().getConnection();
            DatabaseMetaData db = c.getMetaData();
            ResultSet Schemas = db.getSchemas();
            while (Schemas.next()) {
                schemalist.add(Schemas.getString("TABLE_SCHEM"));
            }

        } catch (Exception e) {
        } finally {
            try {
                if (c != null && !c.isClosed()) {
                    c.close();
                }

            } catch (Exception e) {
                Global.Printmessage("SQLHandling.getlistofschemas " + e.getClass().getName() + ": " + e.getMessage());
            }
        }
        schemalist.remove("PUBLIC");
        schemalist.remove("INFORMATION_SCHEMA");
        return schemalist;
    }

    public static ArrayList<String> getlistoftables() {
        ArrayList<String> tablelist = new ArrayList();
        Connection c = null;
        try {
            c = Global.getConnectionPool().getConnection();
            DatabaseMetaData db = c.getMetaData();
            ResultSet rs = db.getTables(null, Global.getcurrentschema(), null, new String[]{"TABLE"});
            while (rs.next()) {
                String temp = rs.getString(3);
                tablelist.add(temp);
            }

            c.close();
            return tablelist;

        } catch (Exception e) {
            Global.Printmessage("SQLHandling.getlistoftables " + e.getClass().getName() + ": " + e.getMessage());
        } finally {
            try {
                if (c != null && !c.isClosed()) {
                    c.close();
                }

            } catch (Exception e) {
                Global.Printmessage("SQLHandling.getlistoftables " + e.getClass().getName() + ": " + e.getMessage());
            }
            return tablelist;
        }
    }

    public static ArrayList<String> getlistoftables(String Schema) {
        ArrayList<String> tablelist = new ArrayList();
        Connection c = null;
        try {
            c = Global.getConnectionPool().getConnection();
            DatabaseMetaData db = c.getMetaData();

            ResultSet rs = db.getTables(null, Schema, null, new String[]{"TABLE"});
            while (rs.next()) {
                String temp = rs.getString(3);
                tablelist.add(temp);
            }

            c.close();
            return tablelist;

        } catch (Exception e) {
            Global.Printmessage("SQLHandling.getlistoftables " + e.getClass().getName() + ": " + e.getMessage());
        } finally {
            try {
                if (c != null && !c.isClosed()) {
                    c.close();
                }

            } catch (Exception e) {
                Global.Printmessage("SQLHandling.getlistoftables " + e.getClass().getName() + ": " + e.getMessage());
            }
            return tablelist;
        }
    }
    /*
     * Creates a list of tables for the given schema storing them under a parent element simply named TABLES
     */

    /*
     check if the secondary table has a foreign key pointing to the primary table, returns columnname if yes null if no
     */
    public static String checkforeignkeys(String Primary, String Secondary) {
        Connection c = null;
        String link = null;
        try {//**run and make sure the primary and secondary are in the right order.
            c = Global.getConnectionPool().getConnection();
            DatabaseMetaData db = c.getMetaData();
            ResultSet keys = db.getImportedKeys(null, Global.getcurrentschema(), Secondary);
            while (keys.next()) {
                String Table = keys.getString("PKTABLE_NAME");
                if (Table.equals(Primary)) {
                    link = keys.getString("PKCOLUMN_NAME");
                    break;
                }
            }
            c.close();
            return link;
        } catch (Exception e) {
        } finally {
            try {
                if (c != null && !c.isClosed()) {
                    c.close();
                }

            } catch (Exception e) {
                Global.Printmessage("SQLHandling.getlistofschemas " + e.getClass().getName() + ": " + e.getMessage());
            }
        }
        return link;
    }

    public static HashMap<String, String> getlinkedtables(String tablename) {
        HashMap<String, String> results = new HashMap();
        Connection c = null;
        try {
            c = Global.getConnectionPool().getConnection();
            DatabaseMetaData db = c.getMetaData();
            ResultSet keys = db.getImportedKeys(null, Global.getcurrentschema(), tablename);
            while (keys.next()) {
                String Table = keys.getString("PKTABLE_NAME");
                String column = keys.getString("FKCOLUMN_NAME");
                results.put(column, Table);

            }
          
            c.close();
        } catch (Exception e) {
        } finally {
            try {
                if (c != null && !c.isClosed()) {
                    c.close();
                }

            } catch (Exception e) {
                Global.Printmessage("SQLHandling.getlistofschemas " + e.getClass().getName() + ": " + e.getMessage());
            }
        }
        return results;
    }

    public static Element getXMLlistoftables(String Schema) {
        Element tablelist = new Element("TABLES");
        Connection c = null;
        try {
            c = Global.getConnectionPool().getConnection();
            DatabaseMetaData db = c.getMetaData();
            ResultSet rs = db.getTables(null, Schema, null, new String[]{"TABLE"});
            while (rs.next()) {
                String temp = rs.getString(3);
                tablelist.addContent(new Element(temp));
            }

            c.close();
            return tablelist;

        } catch (Exception e) {
            Global.Printmessage("SQLHandling.getlistoftables " + e.getClass().getName() + ": " + e.getMessage());
        } finally {
            try {
                if (c != null && !c.isClosed()) {
                    c.close();
                }

            } catch (Exception e) {
                Global.Printmessage("SQLHandling.getlistoftables " + e.getClass().getName() + ": " + e.getMessage());
            }
            return tablelist;
        }
    }

    public static HashMap gettableinfo(String Table, String Schema, ArrayList<String> commands) {

        HashMap info = new HashMap();

        Statement stmt = null;
        Connection c = null;
        try {
            c = Global.getConnectionPool().getConnection();
            stmt = c.createStatement();
            ResultSet results;
            results = stmt.executeQuery("SELECT * FROM " + Schema + "." + Table);
            ResultSetMetaData resultsdata = results.getMetaData();//metadata about this specific table
            DatabaseMetaData dm = c.getMetaData();
            if (commands.contains("ColCount")) {
                info.put("ColCount", resultsdata.getColumnCount());
            }
            if (commands.contains("KeyCount") || commands.contains("KeyList") || commands.contains("KeyListString")) {
                ResultSet dmrs = dm.getImportedKeys(null, Schema, Table);
                ArrayList<String> FKeys = new ArrayList();
                String keystring = "";
                while (dmrs.next()) {
                    String currentkey = dmrs.getString("FKCOLUMN_NAME");
                    FKeys.add(currentkey);
                    keystring += currentkey + ", ";
                }
                if(!keystring.isEmpty())
                {
                keystring = keystring.substring(0, keystring.length() - 2);
                }
                if (commands.contains("KeyCount")) {
                    info.put("KeyCount", FKeys.size());
                }
                if (commands.contains("KeyList")) {
                    info.put("KeyList", FKeys);
                }
                if (commands.contains("KeyListString")) {
                    info.put("KeyListString", keystring);
                }
            }

            if (commands.contains("ColList")) {
                ArrayList<String> columnlist = new ArrayList();
                for (int x = 1; x <= resultsdata.getColumnCount(); x++) {
                    columnlist.add(resultsdata.getColumnName(x));//creates a list of columns in the table
                }
                info.put("ColList", columnlist);
            }
            if (commands.contains("Primary")) {
                ResultSet primary = dm.getPrimaryKeys("", Schema, Table);

                while (primary.next()) {
                    String pkey = primary.getString(4);
                    info.put("Primary", pkey);
                }
            }

        } catch (Exception e) {
            Global.Printmessage("SQLHandling.getlistoftables " + e.getClass().getName() + ": " + e.getMessage());
        } finally {
            try {
                if (c != null && !c.isClosed()) {
                    c.close();
                }

            } catch (Exception e) {
                Global.Printmessage("SQLHandling.getlistoftables " + e.getClass().getName() + ": " + e.getMessage());
            }
            return info;
        }
    }

    public static ArrayList<String> GetColumnList(String Table, String Schema) {
        Statement stmt = null;
        Connection c = null;
        ArrayList<String> columnlist = new ArrayList();
        try {
            c = Global.getConnectionPool().getConnection();
            stmt = c.createStatement();
            ResultSet results;
            results = stmt.executeQuery("SELECT * FROM " + Schema + "." + Table);
            ResultSetMetaData resultsdata = results.getMetaData();//metadata about this specific table

            for (int x = 1; x <= resultsdata.getColumnCount(); x++) {
                columnlist.add(resultsdata.getColumnName(x));//creates a list of columns in the table
            }
        } catch (Exception e) {
            Global.Printmessage("DataNode.GenerateModel " + e.getClass().getName() + ": " + e.getMessage());

        } finally {
            try {
                if (stmt != null && !stmt.isClosed()) {
                    stmt.close();
                }
                if (c != null && !c.isClosed()) {
                    c.close();
                }
            } catch (Exception e) {
                Global.Printmessage("DataNode.GenerateModel " + e.getClass().getName() + ": " + e.getMessage());
            }
        }
        return columnlist;
    }

    public static Element GetXMLColumnList(Element Table, String Schema) {
        Statement stmt = null;
        Connection c = null;
        try {
            c = Global.getConnectionPool().getConnection();
            stmt = c.createStatement();
            ResultSet results;
            results = stmt.executeQuery("SELECT * FROM " + Schema + "." + Table.getName());
            ResultSetMetaData resultsdata = results.getMetaData();//metadata about this specific table

            for (int x = 1; x <= resultsdata.getColumnCount(); x++) {
                Table.addContent(new Element(resultsdata.getColumnName(x)));//creates a list of columns in the table
            }
        } catch (Exception e) {
            Global.Printmessage("DataNode.GenerateModel " + e.getClass().getName() + ": " + e.getMessage());

        } finally {
            try {
                if (stmt != null && !stmt.isClosed()) {
                    stmt.close();
                }
                if (c != null && !c.isClosed()) {
                    c.close();
                }
            } catch (Exception e) {
                Global.Printmessage("DataNode.GenerateModel " + e.getClass().getName() + ": " + e.getMessage());
            }
        }
        return Table;
    }
}
