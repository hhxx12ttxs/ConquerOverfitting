
package clusterer;
import java.math.*;
import java.util.*;
/**
 *
 * @author winslowfarrell
 */
public class Cluster {
    int ID;     //id (of cluster)
    Point centroid = new Point();
    Double energy = 0.0;
    //LinkedList <Point> points = new LinkedList <Point>(); not working: params?
    ArrayList <Point> points = new ArrayList <Point>();
       // should change to LinkedList<point> data
 
    public Cluster() {
    }
        
    public double calculateCentroid() {
        double xDouble, yDouble;
        double dist = 0;
        Point oldCentroid = new Point(centroid.x, centroid.y);

        EUDistance calculator = new EUDistance();
        int arraySize = points.size();
        if (arraySize != 0) {

            xDouble = 0.0;

            yDouble = 0.0;
            for (Point point : points) {
                xDouble += point.x;
                yDouble += point.y;
            }
            centroid.x = xDouble / arraySize;
            centroid.y = yDouble / arraySize;
            dist = calculator.doDistance(oldCentroid, centroid);
        }
        return dist;
    }
    //setter
    public void setCentroid(Point newCentroid){
        centroid = newCentroid;
    }
    //getter
    public Point getCentroid(){
        return(centroid);
    }
// method to add a point: addPoint(Point p)
 public void addPoint(Point newpoint) {
        points.add(newpoint);
    }
 
 public void removePoint(Point oldpoint) {
        points.remove(oldpoint);
    }
 
 public void reAssignPoints() {
     //do again: assign points and calculate centroid
     // return number of reassignments
 }
 public double update(){
     double distance = calculateCentroid();
        energy = getEnergy();
        return distance;
    }
 

 public double getEnergy() { 
        double e = 0.0;     
        EUDistance euDistance = new EUDistance();
        
        for (Point point : points) {
            e += euDistance.doDistance(point, centroid);
        }       
        return(e);
     //energy is sum of energy (distances?) of that cluster 
 }
 
 public void print() {
        System.out.println("Centroid Coords are now " + centroid.x + " " + centroid.y);
        System.out.println("Cluster number: "+ ID);
        System.out.println("Centroid is: " + centroid);
    }    
}




