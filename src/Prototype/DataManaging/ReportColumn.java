package Prototype.DataManaging;

public class ReportColumn {
    private String displayname;
    private String tablename;
    private String columnname;
   
    public ReportColumn(String Display, String Column, String Table)
    {
        displayname = Display;
        tablename = Table;
        columnname=Column;
    }
    
    public String getdisplay()
    {
        return displayname;
    }
   
    public String gettable()
    {
        return tablename;
    }
    
    public String getcolumn()
    {
        return columnname;
    }
   
    public void setcolumn(String name)
    {
        columnname=name;
    }
    public void settable(String name)
    {
        tablename=name;
    }
    public void setdisplay(String name)
    {
        displayname=name;
    }
   
    public String getpath()
    {
        return tablename+","+columnname;
    }
    
    public String ToString()
    {
        return displayname;
    }
}
