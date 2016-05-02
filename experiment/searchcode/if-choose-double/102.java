/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package AnoiModel;

import Model.Connector;
import com.mysql.jdbc.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author oscine
 */
public class AnoiData {
    private String databaseTable;
    Connector connector;
    Statement st;
    
    public  AnoiData(String databaseTableName) throws SQLException
    {  
    this.databaseTable=databaseTableName;    
    connector=new Connector();
    st=(Statement)connector.returnStatement();
    }
    
    
    public Map getAnoiValue(String RegionId,String Month) throws SQLException
    { Map anoiResult = new HashMap();
     String query="SELECT "+databaseTable+".id,"+Month+" FROM `"+databaseTable+"` WHERE RegionId="+RegionId;   
    System.out.println(query);
     ResultSet rs=st.executeQuery(query);
     float sum=0;
     int count=0;
     while(rs.next()){
         if(new Double(rs.getString(Month))>0)
         {
         System.out.println(rs.getString("id")+":"+rs.getString(Month));
     anoiResult.put(rs.getString("id"),rs.getString(Month));
    sum+=new Double(rs.getString(Month)); 
    count++;//System.out.println(tempValue);
         }
    }
     System.out.println("mean : " +sum/count+"count : "+count);
    return filterAnoi(anoiResult,sum/count,2);    
    }
    
    
   public Map filterAnoi(Map IdAnois,float mean,float percentConfidence){   
   Map resultAnoi=new HashMap();
   float confidence= (float) ((100-percentConfidence)*mean*2/100);
   for (Object key: IdAnois.keySet()) {
      if(new Float(IdAnois.get(key).toString())>confidence)
  resultAnoi.put(key,Math.ceil((new Float(IdAnois.get(key).toString()))*500000));
  }
   return resultAnoi;
   }
    
 public int getSerialNo(String id) throws SQLException
    {
        int sn =0;
        String query="SELECT * FROM `"+databaseTable+"` WHERE id="+id; 
        ResultSet rs=st.executeQuery(query);
        while(rs.next())
        {
            sn=rs.getInt("SerialId");
        }
        
        return sn;
    }
    
  
     
//      public double getAnoiData(int sn,String month) throws SQLException
//    {        
//             String query="SELECT * FROM `"+databaseTable+"` WHERE SerialId="+sn;  
//             ResultSet rs=st.executeQuery(query);
//             while(rs.next())
//             {
//                 anoiValue=rs.getDouble(month);
//             }
//            return anoiValue;     
//   
//    }
      
       public double getAnoiData(int sn,String month,String RegionId) throws SQLException
    {        double anoiValue = 0;
             String query="SELECT * FROM `"+databaseTable+"` WHERE SerialId="+sn+" AND RegionId="+RegionId;  
             ResultSet rs=st.executeQuery(query);
             while(rs.next())
             {
                 anoiValue=rs.getDouble(month);
             }
            return anoiValue;   
    }

    public double getAnoiData(String id, String month) throws SQLException {
        double anoiValue = 0;
        String query="SELECT "+month + " FROM "+this.databaseTable+" WHERE id ="+id;  
             ResultSet rs=st.executeQuery(query);
             while(rs.next())
             {
                 anoiValue=rs.getDouble(month);
             }
            return anoiValue;   
         }
      
    }
