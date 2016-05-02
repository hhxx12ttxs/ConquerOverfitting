/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package scRisk.networks;

//import cyclic.GeneralizedPMedian;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import objects.Customer;
import objects.Facility;
import scRisk.optModels.interdiction.facility.rInterdictionMedianProblem;

/**
 *
 * @author hmedal
 */
public class PointsDataset {
    private ArrayList<Point> points;
    private double[][] distances;
    private double[] demands;

    public PointsDataset(String filename){
        readPointsFromFile(filename,true);
        calculateDistancesAndDemands();
    }

    public void readPointsFromFile(String filename,boolean hasHeader){
        points = new ArrayList<Point>();
        BufferedReader br;
            try {
                    br = new BufferedReader(new FileReader(filename));
                    String str = "";
                    if(hasHeader)
                        str = br.readLine();//has header
                    while(str!=null) {
                            str = br.readLine();
                            if(str!=null){
                                String[] nextLine = StringUtils.split(str, "\t");
                                //System.out.println(Arrays.toString(nextLine));
                                int demand = Integer.parseInt(nextLine[1]);
                                int xCoor = Integer.parseInt(nextLine[2]);
                                int yCoor = Integer.parseInt(nextLine[3]);
                                points.add(new Point(demand,xCoor,yCoor));
                            }
                    }
            } catch (FileNotFoundException e) {
                    e.printStackTrace();
            } catch (IOException e) {
                    e.printStackTrace();
            }
    }

    public void calculateDistancesAndDemands(){
        demands = new double[points.size()];
        distances = new double[points.size()][points.size()];
        //System.out.println(points);
        int i=0;
        int j=0;
        for(Point p1:points){
            for(Point p2:points){
                //System.out.println(i+" "+j);
                distances[i][j]=p1.getDistance(p2);
                j++;
            }
            demands[i]=p1.getDemand();
            i++;
            j=0;
        }
    }

    public double[] getDemands() {
        return demands;
    }

    public double[][] getDistances() {
        return distances;
    }

    public SupplyNetworkBipartiteDataset toSupplyNetworkBipartiteDataset(List<Integer> facilities){
            SupplyNetworkBipartiteDataset snb = new SupplyNetworkBipartiteDataset();
            List<Customer> customers = new ArrayList<Customer>();
            for(int i=0;i<distances.length;i++){
                customers.add(new Customer(i,""+i,demands[i]));
            }
            List<Facility> facilitiesList = new ArrayList<Facility>();
            for(int j:facilities){
                facilitiesList.add(new Facility(j,""+j,Double.POSITIVE_INFINITY,0.0));
            }
            snb.setCustomersListFromList(customers);
            //System.out.println(facilities);
            for(int i=0;i<distances.length;i++){
                for(int j=0;j<facilitiesList.size();j++){
                    int index = facilitiesList.get(j).getNumber()-1;
                    snb.setDistance(facilitiesList.get(j),customers.get(i),distances[i][index]);
                }
            }
            return snb;
    }

    public static void main(String[] args){
        PointsDataset pd = new PointsDataset("//ineg-file/shares/NTSCOE_SC_Risk/data/Swain1971.txt");
        //System.out.println(Arrays.deepToString(pd.getDistances()));
        //System.out.println(Arrays.toString(pd.getDemands()));
        double[] fixedCosts = new double[pd.getDistances().length];
        /*for(int p=5;p<=9;p+=2){
            GeneralizedPMedian pMed = new GeneralizedPMedian(pd.getDistances(),pd.getDemands(), fixedCosts, p);
            pMed.createModel();
            pMed.solve();
            List<Integer> medians= pMed.getMedians();
            double objValue = pMed.getObjectiveValue();
            pMed.end();
            SupplyNetworkBipartiteDataset snb = pd.toSupplyNetworkBipartiteDataset(medians);
            //System.out.println(snb);
            for(int r=2;r<=(p-2);r++){
                rInterdictionMedianProblem rInterdict = new rInterdictionMedianProblem(snb,r);
                rInterdict.createModel();
                rInterdict.solve();
                double rObj= rInterdict.getObjValue();
                System.out.println(p+"\t"+r+"\t"+objValue+"\t"+rObj+"\t"+medians+"\t"+rInterdict.getInterdictedFacilityNumbers());
                rInterdict.end();
            }
        }*/
        
    }

    private class Point{
        private double demand;
        private double x;
        private double y;

        public Point(double demand,double x,double y){
            this.demand=demand;
            this.x=x;
            this.y=y;
        }

        public double getDistance(Point p){
            return Math.sqrt(Math.pow(this.x-p.x,2)+Math.pow(this.y-p.y,2));
        }

        public double getDemand(){
            return demand;
        }

        public String toString(){
            return demand+" "+x+" "+y;
        }

    }
}

