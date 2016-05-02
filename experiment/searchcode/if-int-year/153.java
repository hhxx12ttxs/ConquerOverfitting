/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package STAnalysis;

import Analysis.spatialPopulation;
import AnoiModel.AnoiData;
import STModel.OutlierInfo;
import STModel.RegionDataRetrieval;
import java.lang.String;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Smrita
 */
public class AllSpatialOutliers {

    String[] months = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DECM"};
    String anoiDbTable;
    String regionId;
    String startMonth,endMonth;
         
    ArrayList<OutlierInfo> info = new ArrayList();
    RegionDataRetrieval rdr;
    AnoiData ad;
//    StAnomaliesSequencer sequencer=new StAnomaliesSequencer();
//    ArrayList<ArrayList<OutlierInfo>> sequencedSpatialAnomalies=new ArrayList<ArrayList<OutlierInfo>>();
  
    
    
    int edgeflag=16;
    int year;
    int startYear;
    int endYear;
    int currentYearFlag;
    int startMonthFlag,endMonthFlag;
    int counter;
    int breadth,length;
    
    public AllSpatialOutliers(String startMonthh,String endMonthh,int startYearr,int endYearr,String regionid) throws SQLException
    {
          startYear=startYearr;
          endYear=endYearr;
          startMonth=startMonthh;
          endMonth=endMonthh;
          anoiDbTable="anoi_"+startYear;
          counter=endYear-startYear+1;
          year=startYear;
          currentYearFlag=year;
          System.out.println("region id"+regionid);
          regionId=regionid;
          ad=new AnoiData(anoiDbTable);
          rdr=new RegionDataRetrieval("regions");
          breadth=rdr.getBreadth(regionId);
          length=rdr.getLength(regionId);
          edgeflag=length;
          System.out.println("breadth length\t"+breadth+"\t"+length);
    }

    public void setRegionId(String region_id) {
        regionId = region_id;
    }

    public ArrayList<OutlierInfo> getAllSpOutliers() throws SQLException {
        Map spOutliers = new HashMap();
       
         while(counter!=0)
          {
                for(int i=0;i<12;i++)
                {
                     if(startMonth==months[i])
                         startMonthFlag=i;
                     
                     if(endMonth==months[i])
                         endMonthFlag=i;
                }
                
                
                if(currentYearFlag!=year)
                {
                       System.out.println("changed year in all spatial outliers");
                       anoiDbTable="anoi_"+year;
                       currentYearFlag=year;
                       ad=new AnoiData(anoiDbTable);
                }

        for (int cols = 0; cols < 12; cols++) 
        {
            if(year==startYear&&cols<startMonthFlag)
                        {
                              System.out.println("skipping loop when \tstart year\t"+startYear+"\tmonth\t"+months[cols]);
                              continue;
                        }
                        
            if(year==endYear&&cols>endMonthFlag)
                        {
                              System.out.println("breaking loop when \t end year\t"+endYear+"\tmonth\t"+months[cols]);
                              break;
                        }
                       spOutliers = ad.getAnoiValue(regionId, months[cols]);
          
              
                int sn;
                Set s = spOutliers.entrySet();
               
                Iterator it = s.iterator();

                while (it.hasNext())
                {
                    OutlierInfo soi = new OutlierInfo();
                    Map.Entry m = (Map.Entry) it.next();
                    String key = (String) m.getKey();
                    sn = ad.getSerialNo(key);
                   // if (sn % spopn.length == 0 || sn % spopn.length == 1 || sn < spopn.length * 2 || sn > spopn.breath * (spopn.length - 2))//data filtration
                      //   continue
                   
                        if(sn%edgeflag==0||sn%edgeflag==1||sn<edgeflag*2||sn>edgeflag*(breadth-2)||sn%edgeflag==2||sn%edgeflag==(edgeflag-1))
                        continue;

                       
                        soi.setId(key);
                        soi.setMonth(months[cols]);
                        soi.setYear(year);
                        soi.setSerialId(sn);
                        info.add(soi);
                       // System.out.println("soi:\t"+soi.id+"\t"+soi.month+"\t"+soi.year);
                }
          
           }
                        year=year+1;
                        counter--; 
       }
       System.out.println("info size"+info.size());
     //  sequencedSpatialAnomalies=sequencer.sequenceStAnomalies(info);
       
       System.out.println("sequenced Outliers");
       ArrayList<ArrayList<String>> bigList=new ArrayList<ArrayList<String>>();
       
//       for(int i=0;i<sequencedSpatialAnomalies.size();i++)
//       {   
//            ArrayList<OutlierInfo> sAnomalies=new ArrayList();
//            ArrayList<String> id = new ArrayList<String>();
//             
//            
//            sAnomalies=sequencedSpatialAnomalies.get(i);
//            OutlierInfo outlier=new OutlierInfo();
//            outlier=sAnomalies.get(i);
//                     id.add(outlier.id.toString());
//                     System.out.println(outlier.id); 
//                     bigList.add(id);
//                    
//            for(int j=0;j<sAnomalies.size();j++)
//            {   
//               OutlierInfo outlier1=new OutlierInfo();
//               outlier1=sAnomalies.get(j);
//               String yEar=outlier1.year+"";
//              //System.out.println(outlier1.id+"\t"+outlier1.month+"\t"+outlier1.year);
//               
//            ArrayList<String> time_stamp=new ArrayList<String>();
//               time_stamp.add(outlier1.month.toString());
//               time_stamp.add(yEar);
//               bigList.add(time_stamp);
//               
//            }
//            
//            
//            
//            //System.out.println(outlier.id+"\t"+outlier.month+"\t"+outlier.year);
//       }
//        System.out.println(bigList);
        

        
        
       
       return info;
       
      
       
    }
         
    
    public static void main(String[] args) throws SQLException
    {
             ArrayList<OutlierInfo> infoo = new ArrayList();
             AllSpatialOutliers aso=new AllSpatialOutliers("FEB","OCT",2002,2003,"27");
             
             infoo=aso.getAllSpOutliers();
             
             //System.out.println("id\tyear\tmonth");
             for(int i=0;i<infoo.size();i++)
             {
               OutlierInfo g = new OutlierInfo();
               g=infoo.get(i);
               //System.out.println(g.serialId+"\t"+g.id+"\t"+g.year+"\t"+g.month);
             }
    }
}

