/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package STAnalysis;

import STModel.Neighbours;
import STModel.OutlierInfo;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 *
 * @author Smrita
 */
public class SpTempOutliers {

    static int currentYear;
    int maxYear = 2008;
    int minYear = 2004;
    int startYear;
    int endYear;
    int rowCount;
    int year; 
    double corr_2;
    double corr_1;
    double corr;
    double corr1;    
    String[] months = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DECM"};
    String startMonth;
    String endMonth;
    String regionId;
    AllSpatialOutliers so;
    Neighbours nbrs;
    ArrayList<OutlierInfo> spOutlierInfo = new ArrayList();
    ArrayList<OutlierInfo> stOutlierInfo = new ArrayList();
    
    
    
    static StAnomaliesSequencer sequencer=new StAnomaliesSequencer();
    static ArrayList<ArrayList<OutlierInfo>> sequencedSTAnomalies=new ArrayList<ArrayList<OutlierInfo>>();
    
  
    

   
    public SpTempOutliers(int year1, int year2, String startmnth, String endmnth, String regionid) throws SQLException 
    {
        startYear = year1;
        currentYear = startYear;
        endYear = year2;
        startMonth = startmnth;
        endMonth = endmnth;
        regionId = regionid;
        so = new AllSpatialOutliers(startMonth, endMonth, startYear, endYear, regionId);
        nbrs = new Neighbours(regionId);
        nbrs.initializeNeighbours(startYear);
     
    }
    

    public void setMinMaxYears(int min, int max)
    {
        minYear = min;
        maxYear = max;
    }

    public  ArrayList<ArrayList<String>> getAllspTempOutliers() throws SQLException {
         
        ArrayList<ArrayList<String>> stAnomalies=new ArrayList<ArrayList<String>>();
        int mnthindx = 0;
        spOutlierInfo = so.getAllSpOutliers();
        System.out.println("spOutliers size  " + spOutlierInfo.size());

        for (int j = 0; j < spOutlierInfo.size(); j++) {
            OutlierInfo out = new OutlierInfo();
            out = spOutlierInfo.get(j);
            System.out.println(out.serialId + "\t" + out.month + "\t" + out.year);
        }
        for (int i = 0; i < spOutlierInfo.size(); i++)
        {
            int count = i + 1;
            System.out.println("sn\t" + count);
            OutlierInfo soi = new OutlierInfo();
            soi = spOutlierInfo.get(i);
            if (soi.year < minYear || soi.year > maxYear||(soi.year==minYear&&soi.month=="JAN")||(soi.year==maxYear&&soi.month=="NOV")||(soi.year==maxYear&&soi.month=="DECM"))
            {
                continue;
            }

            if (soi.year != currentYear)
            {
                System.out.println("===============entered if soi.year!currentYear===================");
                nbrs.initializeNeighbours(soi.year);
                currentYear = soi.year;
            }
            
            System.out.println("soi.year  " + soi.year);
            nbrs.setNeighbours(soi.serialId, soi.month);
            System.out.println("sn\tid\t month\n" +soi.serialId+"\t"+ soi.id + "\t" + soi.month);
            compareNeighbours(nbrs);

            if (corr < 0) {
                if (corr1 > 0.5 && corr_1 < 0) {
                    System.out.println("============stoutlier=============");
                    stOutlierInfo.add(soi);
                } else if (corr_1 > 0.5 && corr1 < 0) {
                    System.out.println("===============stoutlier=============");
                    for (int ind = 0; ind < 12; ind++) {
                        if (months[ind] == soi.month) {
                            mnthindx = ind;
                        }
                    }
                    OutlierInfo soy = new OutlierInfo();
                       if(soi.month=="DECM")
                       {   soy.setMonth("JAN");
                           int yearrr=soi.year+1;
                           soy.setYear(yearrr);
                       }
                       else
                       {   String mnth = months[mnthindx + 1];
                           soy.setMonth(mnth);
                           soy.setYear(soi.year);
                        }
                    
                    soy.setSerialId(soi.serialId);
                    soy.setId(soi.id);
                   
                   
                    stOutlierInfo.add(soy);

                }
            }
        }
        sequencer.sequenceStAnomalies(stOutlierInfo);
        stAnomalies=sequencer.getSequencedAnomalies();
        
        return stAnomalies;
    }

    private void compareNeighbours(Neighbours nbr) {
        corr = getPearsonCorrelation(nbr.nbr1TimeStampAhead, nbr.nbrInThisTimeStamp);
        corr_1 = getPearsonCorrelation(nbr.nbrInThisTimeStamp, nbr.nbr1TimeStampBack);
        corr1 = getPearsonCorrelation(nbr.nbr2TimeStampAhead, nbr.nbr1TimeStampAhead);
        System.out.println("corr\tcorr_1\tcorr1\t" + corr + "\t" + corr_1 + "\t" + corr1);
    }

    public static double getPearsonCorrelation(double[] scores1, double[] scores2) {

        double result = 0;
        double sum_sq_x = 0;
        double sum_sq_y = 0;
        double sum_coproduct = 0;
        double mean_x = scores1[0];
        double mean_y = scores2[0];

        for (int i = 2; i < scores1.length + 1; i++) {
            double sweep = Double.valueOf(i - 1) / i;
            double delta_x = scores1[i - 1] - mean_x;
            double delta_y = scores2[i - 1] - mean_y;
            sum_sq_x += delta_x * delta_x * sweep;
            sum_sq_y += delta_y * delta_y * sweep;
            sum_coproduct += delta_x * delta_y * sweep;
            mean_x += delta_x / i;
            mean_y += delta_y / i;
        }
        double pop_sd_x = (double) Math.sqrt(sum_sq_x / scores1.length);
        double pop_sd_y = (double) Math.sqrt(sum_sq_y / scores1.length);
        double cov_x_y = sum_coproduct / scores1.length;


        result = cov_x_y / (pop_sd_x * pop_sd_y);
        return result;
    }

    public static void main(String[] args) throws SQLException {
        SpTempOutliers sto = new SpTempOutliers(2001, 2004, "APR", "APR", "27");// input 
        ArrayList<OutlierInfo> result = new ArrayList();
        ArrayList<ArrayList<String>> stanomalies= sto.getAllspTempOutliers();// output
        System.out.println(stanomalies);
 }
}

