/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.coperius.desktopapplication2;

/**
 *
 * @author coperius
 */


import com.coperius.desktopapplication2.couchdb.couchUtils;
import com.fourspaces.couchdb.Database;
import com.fourspaces.couchdb.Document;
import com.fourspaces.couchdb.Session;
import com.fourspaces.couchdb.View;
import com.fourspaces.couchdb.ViewResults;
import java.util.*;
import java.sql.*;
import java.awt.*;
import javax.swing.table.*;
import javax.swing.*;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
//import org.postgresql.Driver;
import javax.swing.Timer;
import org.jdesktop.swingx.JXTable;

public class defTableModel extends AbstractTableModel {
    private static final long serialVersionUID = 1L;
    private ArrayList<String> columnNames = new ArrayList<String>();
    private ArrayList<Class> columnTypes = new ArrayList<Class>();
    private ArrayList<ArrayList<Object>> data = new ArrayList<ArrayList<Object>>();
    private Map hiddenColumns;
    private Map keyEvents;
    
    Connection con = null;
    Statement st = null;
    ResultSet rs = null;
    String url;
    String user;
    String password;
//    String query;
//    String condition;
    JXTable table;
    String doubleAction;
    Object parent;
    String view;
    String keys;

    
    public defTableModel() {

            try {
            //Class.forName("org.postgresql.Driver");
            //url = "jdbc:postgresql://localhost:5432/gate";                //your data
            //user = "postgres";                  //your data
            //password = "postgres";               //your data
            
            hiddenColumns = new HashMap();
            keyEvents = new HashMap();
            view = "";
            keys = null;
    
        }
        catch (Exception e)
            {

        }

    }


    public void addKeyEvent(int key,String funcName)
    {
      keyEvents.put(key, funcName);
    }

    public void setParent(Object parent) {
        this.parent = parent;
    }

    
    public void setDoubleAction(String doubleAction) {
        this.doubleAction = doubleAction;
    }

    

    public void setTable(JXTable table) {
        this.table = table;
    }


    public void setEnableSort()
    {
      if (table!=null)
      {
      TableSorter sorter = new TableSorter(this, table.getTableHeader());
      table.setModel(sorter);
      }
    }

 public void doubleClick()
    {

if (parent!=null && this.doubleAction!=null)
{
     Method method = null;
        try {
            method = parent.getClass().getMethod(this.doubleAction);
        } catch (NoSuchMethodException e) { /*  */ }

        // вызываем метод
        try {
            method.invoke(parent);
        } catch (IllegalAccessException e) {
            // не возникнет если следовать контракту публичных методов в action
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }
}

 }

public void setListeners()
    {
    
     if (table!=null)
     {
       table.addMouseListener(new tableMouseListener_org(table));
       table.addKeyListener(new KeyAdapter() {
         public void keyPressed(KeyEvent e)
             {
              if (keyEvents.containsKey(e.getKeyCode()))
              {
                if (parent!=null)
                {
                Method method = null;
                try {
                    method = parent.getClass().getMethod(keyEvents.get(e.getKeyCode()).toString());
                } catch (NoSuchMethodException ex) { /*  */ }

                // вызываем метод
                try {
                    method.invoke(parent);
                } catch (IllegalAccessException ex) {
                // не возникнет если следовать контракту публичных методов в action
                    throw new RuntimeException(ex);
                } catch (InvocationTargetException ex) {
                    throw new RuntimeException(ex.getCause());
                }
                }

              }
	     }
             });
     }

}

public void changeSource(String source)
    {
    changeSource(source,"");
}

public void changeSource(String source,String key)
{
 try
 {
//  Session s = new Session(couchUtils.IP,couchUtils.PORT);
//  Database db = s.getDatabase(couchUtils.BASE);
 if ((source==null) || (source.equals(""))) ;
 else view = source;

 if (key!=null) keys = key;
// {
//   if (key.equals("")) keys="";
//   else keys =key;
// }
 
//  if(!source.equals(""))view = source;

 if (!view.equals("")) 
   if(keys.equals(""))  setDataSource(view,null);
   else if (keys.equals("null")) setDataSource(view,"");
   else setDataSource(view,keys);

 }
 catch (Exception e)
 {
  e.printStackTrace();
  JOptionPane.showMessageDialog(null, e.getMessage());
 }
}


    public int getRowCount() {
        synchronized (data) {
            return data.size();
        }
    }

    public int getColumnCount() {
        return columnNames.size();
    }

    public Object getValueAt(int row, int col) {
        synchronized (data) {
            return data.get(row).get(col);
        }
    }

    public String getColumnName(int col) {
        return columnNames.get(col);
    }

    public Class getColumnClass(int col) {
        return columnTypes.get(col);
    }

    public boolean isCellEditable(int row, int col) {
        return false;
    }

    public void setValueAt(Object obj, int row, int col) {
        synchronized (data) {
            data.get(row).set(col, obj);
        }
    }

    /**
     * Core of the model. Initializes column names, types, data from ResultSet.
     *
     * @param rs ResultSet from which all information for model is token.
     * @throws SQLException
     * @throws ClassNotFoundException
     */

    public void setDataSource(String source,String key) throws  ClassNotFoundException {
        columnNames.clear();
        columnTypes.clear();
        data.clear();
     Session s = new Session(couchUtils.IP,couchUtils.PORT);
     Database db = s.getDatabase(couchUtils.BASE);
     View v = new View("desktop/fields"); //TODO
     v.setKey(java.net.URLEncoder.encode("\""+source+"\""));
     ViewResults rs = db.view(v);

     View v2 = new View("desktop/types"); //TODO
     v2.setKey(java.net.URLEncoder.encode("\""+source+"\""));
     ViewResults rs2 = db.view(v2);

     if ((rs==null) || (rs.isEmpty())) return;
     if (rs.getResults().isEmpty()) return;
     if ((rs2==null) || (rs2.isEmpty())) return;
     if (rs2.getResults().isEmpty()) return;

//     java.util.List<Document> docs = rs.getResults();
     if (rs.getResults().size()<1) return;
//     Document doc = rs.getResults().get(0);
     Object[] names =  rs.getResults().get(0).getJSONArray("value").toArray();
     int columnCount = names.length; //  rsmd.getColumnCount();

//     java.util.List<Document> docs = rs.getResults();
     if (rs2.getResults().size()<1) return;
//     Document doc = rs.getResults().get(0);
     Object[] types =  rs2.getResults().get(0).getJSONArray("value").toArray();
     int columnCount2 = types.length; //  rsmd.getColumnCount();
        //for (int i = 0; i < columnCount; i++) {

     if (columnCount!=columnCount2) return;

     ArrayList<String> columnIds = new ArrayList<String>();

        for (int i=0;i<columnCount;i++)
        {
            String[] sp = names[i].toString().split(":");
            columnNames.add(sp[0]);
            columnIds.add(sp[1]);
            columnTypes.add(Class.forName(types[i].toString()));
        }
        fireTableStructureChanged();


     View v3 = new View("desktop/"+source); //TODO

     if (key!=null) v3.setKey(java.net.URLEncoder.encode("\""+key+"\""));
     ViewResults rs3 = db.view(v3);

     if ((rs3==null) || (rs3.isEmpty())) return;
     if (rs3.getResults().isEmpty()) return;

     java.util.List<Document> docs = rs3.getResults();
     if (rs3.getResults().size()<1) return;
     Document doc = rs3.getResults().get(0);

        for(int j=0;j<docs.size();j++)
        {
            doc = docs.get(j);
            ArrayList rowData = new ArrayList();
            for (int i = 0; i < columnCount; i++) {
                if (doc.getJSONObject("value").containsKey(columnIds.get(i)))
                {
                  if (columnTypes.get(i) == String.class)
                    rowData.add(doc.getJSONObject("value").getString(columnIds.get(i)));
                  else
                    rowData.add(doc.getJSONObject("value").get(columnIds.get(i)));
                }
                else rowData.add(null);
            }
            synchronized (data) {
                data.add(rowData);
                this.fireTableRowsInserted(data.size() - 1, data.size() - 1);
            }
        }


    }
//    public void setDataSource(ViewResults rs) throws SQLException, ClassNotFoundException {
//        //ResultSetMetaData rsmd = rs.getMetaData();
//        columnNames.clear();
//        columnTypes.clear();
//        data.clear();
//        if ((rs==null) || (rs.isEmpty())) return;
//        if (rs.getResults().isEmpty()) return;
//        java.util.List<Document> docs = rs.getResults();
//        if (docs.size()<1) return;
//
//        Document doc = docs.get(0);
//        int columnCount = docs.get(0).getJSONObject("value").size(); //  rsmd.getColumnCount();
//        //for (int i = 0; i < columnCount; i++) {
//
//        Object[] keya=doc.getJSONObject("value").keySet().toArray();
//        for (int i=0;i<keya.length;i++)
//        {
//            String cname= keya[i].toString();
//            columnNames.add(cname);
//            Class type = Class.forName(doc.getJSONObject("value").get(cname).getClass().getName());
//            columnTypes.add(type);
//            //doc.keys();
//        }
//        fireTableStructureChanged();
//
//        for(int j=0;j<docs.size();j++)
//        {
//            doc = docs.get(j);
//            ArrayList rowData = new ArrayList();
//            for (int i = 0; i < columnCount; i++) {
//                if (columnTypes.get(i) == String.class)
//                    rowData.add(doc.getJSONObject("value").getString(columnNames.get(i)));
//                else
//                    rowData.add(doc.getJSONObject("value").get(columnNames.get(i)));
//            }
//            synchronized (data) {
//                data.add(rowData);
//                this.fireTableRowsInserted(data.size() - 1, data.size() - 1);
//            }
//        }
//    }

   public  void hideColumn(int index){
     if (table==null) return;
     
     if(!hiddenColumns.containsKey(index))
     {
      hiddenColumns.put(index, 0);
     }
     hideColumns();
  }

   protected void hideColumns()
    {
    for (Object element : hiddenColumns.keySet()) { 
        table.getColumnModel().getColumn((Integer) element).setMaxWidth(0);
	table.getColumnModel().getColumn((Integer) element).setMinWidth(0);
	table.getTableHeader().getColumnModel().getColumn((Integer) element).setMaxWidth(0);
	table.getTableHeader().getColumnModel().getColumn((Integer) element).setMinWidth(0);

    }
    
    }
   




}
