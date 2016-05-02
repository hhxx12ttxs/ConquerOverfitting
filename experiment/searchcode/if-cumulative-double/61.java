/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package applet_algorithm;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.*/

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.io.File;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException; 
import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
/**
 *
 * @author Ervin Domazet & Mert Terzihan
 */
public class Algorithm {
    MyPoint rootPoint; // starting point of the algorithm
    String keywords; // keywords of the map
    int userID; // ID of the user that wants to schematize the map
    double mean_distance=0.0; 
    boolean preserveEast_West; // true if an advanced user selects east-west preservation
    boolean preserveNorth_South; // true if an advanced user selects north-south preservation
    boolean preserve_distance; // true if an advanced user selects distance preservation
    private double angleMultiple; // the value that the map will be schematize according to it
    double totalDistance=0;
    int totalEdges=0;
    
    //constructor
    Algorithm(MyPoint p, String k, int ID, Double _angleMultiple,boolean E_W,boolean N_S,boolean _distance) throws FileNotFoundException,IOException{
        rootPoint = p;
        keywords = k;
        userID = ID;
        preserveEast_West=E_W;
        preserveNorth_South=N_S;
        preserve_distance=_distance;
        angleMultiple = _angleMultiple;
        //parseConfigurationFromFile(ConfFile);
    }
    Algorithm(MyPoint p, String k, int ID) throws FileNotFoundException,IOException{
        rootPoint = p;
        keywords = k;
        userID = ID;
        preserveEast_West=false;
        preserveNorth_South=false;
        preserve_distance=false;
        angleMultiple =45.00;
        //parseConfigurationFromFile(ConfFile);
    }
 
    //getter-setters
    double getAngleMultiple(){
        return angleMultiple;
    }
    void setAngleMultiple(double a){
        angleMultiple = a;
    }
    
    boolean get_preserveEast_West(){
        return preserveEast_West;
    }
    boolean set_preserveEast_West(){
        return preserveEast_West;
    }
    boolean get_preserveNorth_South(){
        return preserveNorth_South;
    }
    boolean set_preserveNorth_South(){
        return preserveNorth_South;
    }
     boolean get_preserve_distance(){
        return preserve_distance;
    }
    boolean set_preserve_distance(){
        return preserve_distance;
    }
    
    //parsing configuration from a properties file(can be used)
    void parseConfigurationFromFile(String path) throws FileNotFoundException, IOException{
        Properties configuration;
        if(path != null){
            configuration = new Properties();
            FileInputStream InputStream = new FileInputStream(path);
            configuration.load(InputStream);
            InputStream.close();
            angleMultiple =Double.parseDouble(configuration.getProperty("angleMultiple"));
            preserveEast_West=Boolean.parseBoolean(configuration.getProperty("preserveEast_West"));
            preserveNorth_South=Boolean.parseBoolean(configuration.getProperty("preserveNorth_South"));
            preserve_distance=Boolean.parseBoolean(configuration.getProperty("preserve_distance"));
        }
        
       
    }
    
    //default configuration options especially for simple users
    void defaultConfiguration()throws FileNotFoundException,IOException{
        preserveEast_West=false;
        preserveNorth_South=false;
        preserve_distance=false;
        angleMultiple =45.00;
    }
    
    // this function is called if the user pushes the schematize button
    MyPoint Schematize(){
        MyPoint startPoint=rootPoint.outgoingPoints.get(0); // setting the starting point of the algorithm
        rootPoint.setY(rootPoint.outgoingPoints.get(0).getY());
        recursively_schematize(rootPoint, rootPoint.outgoingPoints.get(0)); // calling the schematization function that runs recursively
        rootPoint.setY(0);
        if(!preserve_distance){
            mean_distance(rootPoint.outgoingPoints.get(0)); // if an advanced user selects distance preservation, then the distances between every two neighbor points will be assigned to mean distance between all neighbor points
        }
        return rootPoint;    
    }
    void mean_distance(MyPoint start){
        int meanDistance=(int)(this.totalDistance/this.totalEdges); // calculating the mean distance between all points
        recursively_mean_distance(start,meanDistance); // recursive function that assignes magnitudes of all edges to the mean distance
    }
    
    // recursive function that assignes magnitudes of all edges to the mean distance
    void recursively_mean_distance(MyPoint start, int mean){
        
        for(int i=0; i< start.outgoingPoints.size();i++){
                // get the next point that is connected with current point
                MyPoint next=start.outgoingPoints.get(i);
                int old_x=next.getX();
                int old_y=next.getY();
                int new_x;
                int new_y;
                int cumulative_change_x;
                int cumulative_change_y;
                // constuct a helper point , which is shadowed version next point, to x axis 
                MyPoint helper_point= new MyPoint(next.getX(),start.getY());
                
                double angle_between;
                //check for some specific conditions
                if(next.getX()==start.getX()){
                    angle_between=90.0;
                }
                else{
                    //calculate the angle between three points
                    angle_between=this.calculateAngle(next, start, helper_point);
                }
                // find the distance of the connection
                int distance_=(int)this.distance(start, next);
                /*check for different conditions, and find the new value of x and y,
                 so that the distance between this point and next point
                 equates to MEAN */
                if(start.getX()<=next.getX()){
                    if(start.getX()==next.getX()){
                        new_x=old_x;
                    }
                    else{
                        new_x=(int)(start.getX()+(Math.cos(Math.toRadians(angle_between))*mean));
                    }   
                    if(start.getY()<=next.getY()){
                        if(start.getY()==next.getY()){
                            new_y=old_y;
                        }
                        else{
                            new_y=(int)(start.getY()+(Math.sin(Math.toRadians(angle_between))*mean));
                        }
                        
                    }
                    else{
                        new_y=(int)(start.getY()-(Math.sin(Math.toRadians(angle_between))*mean));
                    }
                }
                else
                {
                    new_x=(int)(start.getX()-(Math.cos(Math.toRadians(angle_between))*mean));
                    if(start.getY()<=next.getY()){
                        if(start.getY()==next.getY()){
                           new_y=old_y;
                        }
                        else
                        {
                            new_y=(int)(start.getY()+(Math.sin(Math.toRadians(angle_between))*mean)); 
                        }
                    }
                    else
                    {
                           new_y=(int)(start.getY()-(Math.sin(Math.toRadians(angle_between))*mean));
                    }
                } 
                /* find the cumulative change(new-old),
                 *  which will be applied to all of the remaining 
                 * points that are directly or indirectly connected to the
                 * current point
                 */
                cumulative_change_x=new_x-old_x;
                cumulative_change_y=new_y-old_y;
                next.setX(new_x);
                next.setY(new_y);
                //reqursivelly apply the change
                apply_cummulative_effect(next,cumulative_change_x,cumulative_change_y);
                // reqursivelly continue the same iteration
                recursively_mean_distance(next,mean);
          
        }
    }
    /* This function applies a recursive cumulative change to all the points that 
     * are connected directly or independently to the start point
     * 
     * So a change in x and y axis will be reflected to all the "remaining" points connected
     * to that point
     */
    void apply_cummulative_effect(MyPoint start,int diff_x,int diff_y){
        for(int i=0;i<start.outgoingPoints.size();i++){
            MyPoint next=start.outgoingPoints.get(i);
            next.setX(next.getX()+diff_x);
            next.setY(next.getY()+diff_y);
            apply_cummulative_effect(next,diff_x,diff_y);
        }
    }
    
    void append_to_first(MyPoint p1,MyPoint p2,double distance)
    {
        p2.setY(p1.getY());
        p2.setX(p1.getX()+(int)distance);
    }
    
    // the function that schematizes the given map recursively taking three neighbor points in order
    void recursively_schematize(MyPoint p1,MyPoint p2){
        //if(!preserveEast_West & !preserveNorth_South){
            for(int i=0;i<p2.outgoingPoints.size();i++){ // for every neighbor point of point 2
                MyPoint p3=p2.outgoingPoints.get(i); // setting one of the neighbor points of point 2 to point 3, so that we can have a combination of 3 neighbor points
                if(p3.point_id!=p1.point_id){ // the third point should be different than point 1, since we have two ways of edges
                    double angle=calculateAngle(p1,p2,p3); // calculation of the angle between three points, we will change this angle
                    double actual_angle=angle; // original angle
                    double remainder;
                    remainder=angle % angleMultiple; // the amount that is the difference between original angle and any multiple of the angle multiple that is chosen by the user or the default one
                    int division = (int)(angle / angleMultiple);
                    if((remainder!=0.0) || (division != 0.0)){ // if the angle is not already schematized
                        if(remainder > (angleMultiple/2)){ // if the angle is closer to the angle (division+1)*angleMultiple
                            //asagi dogru!
                            //angle=(int)((angle/angleMultiple) * angleMultiple )-remainder;
                            angle=angleMultiple-remainder;//add the remaining angle to the original angle so that the angle is schematized
                            movePoint(p1,p2, p3, angle,actual_angle);
                        }
                        else{
                            //angle yukari dogru gidecek
                            angle=-remainder;//subtract the remaining angle from the original angle so that the angle is schematized
                            //angle=(int)(((angle/angleMultiple)-1) * angleMultiple )-remainder;
                            movePoint(p1,p2, p3,angle,actual_angle);
                        }
                    }
                    else if((remainder!= 0.0) && (division == 0.0)){
                        angle = angleMultiple-remainder;
                        movePoint(p1, p2, p3, angle, actual_angle);
                    }
                    
                    if(!this.preserve_distance){ // if the preserve distance option is selected, then the magnitudes of all edges should be added together to calculate mean distance 
                        this.totalDistance=totalDistance+distance(p2,p3);
                        this.totalEdges++;
                    }
                    recursively_schematize(p2,p3); // calling the recursively schematization function for point 2 and point 3 to proceed with the schematization of the whole map
                }
                else // if the point 3 is point 1, in other words the angle is 0
                    {
                       p2.outgoingPoints.remove(i); //to prevent from infinite loop
                       i--;
                    }
            }
        //}
        
        
    }
    double calculateAngle(MyPoint p1, MyPoint p2, MyPoint p3){ // the angle calculation is done according to the cosine law
        double a = Math.sqrt(Math.pow((p1.getX()-p2.getX()), 2) + Math.pow((p1.getY()-p2.getY()), 2));
        double b = Math.sqrt(Math.pow((p3.getX()-p2.getX()), 2) + Math.pow((p3.getY()-p2.getY()), 2));
        double c = Math.sqrt(Math.pow((p3.getX()-p1.getX()), 2) + Math.pow((p3.getY()-p1.getY()), 2));
        double angle = Math.toDegrees(Math.acos((Math.pow(a,2)+Math.pow(b,2)- Math.pow(c,2))/(2.0*a*b)));
        return angle;
    }
    
    //moving the point 3 according to the calculation in the recursively_schematize function
    void movePoint(MyPoint p1,MyPoint p2, MyPoint p3, double _angle,double actual_angle){
        boolean above = false; // for north-south preservation
        if(p2.getY()<p3.getY()){
            above = true;
        }
        boolean right = false; // for east-west preservation
        if(p2.getX()<p3.getX()){ 
            right = true;
        }
        if(angleMultiple==(double)180){ // if the angle multiple is 180
            double distance_p2_p3=this.distance(p2, p3);
            append_to_first(p2,p3,distance_p2_p3); // to prevent points to overlap
        }
        else{
            double y3_prime; // the value of the point's y-coordinate if the three points lie on the same line
            boolean y3_yukarda_kalir; // whether the y3_prime is located above p3's y-coordinate value or not
            if((p2.getX()-p1.getX())!=0){ 
                y3_prime=p2.getY() + (int)( ((p3.getX()-p2.getX())*(p2.getY()-p1.getY()))/ (p2.getX()-p1.getX())   );
            }
            else
            {
                if(p3.getX()>p2.getX()){
                    y3_prime=p3.getY()+1;
                }
                else
                    y3_prime=p3.getY()-1;
            }

            if (y3_prime >= p3.getY()){
                y3_yukarda_kalir=true;
            }
            else
                y3_yukarda_kalir=false;
            double dist_p2_p3 = Math.sqrt(Math.pow((p2.getX()-p3.getX()), 2) + Math.pow((p2.getY()-p3.getY()), 2)); // the distance between point 2 and 3

            if(y3_yukarda_kalir && actual_angle >=90){ // if the y3_prime is greater than p3's y and original angle is greater than 90
                /* calculate the new coordinates of the Point 3, so that it
                 * suits the given angle multiple restriction
                 */
                MyPoint pnew=new MyPoint(p3.getX(),p2.getY());
                double angle=calculateAngle(pnew,p2,p3);
                double new_angle;
                if( pnew.getY()>p3.getY()){
                    new_angle=angle-_angle;
                }
                else
                    new_angle=_angle+angle;

                /*int tmp1=Math.abs((int)(dist_p2_p3*Math.cos(Math.toRadians(new_angle))));
                int tmp2=Math.abs((int)(dist_p2_p3*Math.sin(Math.toRadians(new_angle))));*/
                
                int tmp1=(int)(dist_p2_p3*Math.cos(Math.toRadians(new_angle)));
                int tmp2=(int)(dist_p2_p3*Math.sin(Math.toRadians(new_angle)));

                //p3.setX(p2.getX()+ tmp1 );
                if( pnew.getX()> p2.getX()){
                    p3.setX(p2.getX()+ tmp1 );
                }
                else{
                    p3.setX(p2.getX()- tmp1 );
                }
                if( pnew.getY()> p3.getY()){
                    p3.setY(p2.getY()- tmp2 );
                }
                else{
                    p3.setY(p2.getY()+ tmp2 );
                }
                /*if( pnew.getX()> p2.getX() && new_angle > 0.0){
                    p3.setX(p2.getX()+ tmp1 );
                }
                else if ( pnew.getX()> p2.getX() && new_angle < 0.0){
                    p3.setX(p2.getX()- tmp1 );
                }
                else if( pnew.getX()< p2.getX() && new_angle > 0.0){
                    p3.setX(p2.getX()- tmp1 );
                }
                else
                    p3.setX(p2.getX()+ tmp1 );
                
                if( pnew.getY()> p3.getY() && new_angle > 0.0){
                    p3.setY(p2.getY()- tmp2 );
                }
                else if ( pnew.getY()> p3.getY() && new_angle < 0.0){
                    p3.setY(p2.getY()+ tmp2 );
                }
                else if( pnew.getY()< p3.getY() && new_angle > 0.0){
                    p3.setY(p2.getY()+ tmp2 );
                }
                else
                    p3.setY(p2.getY()- tmp2 );*/
            }
            else if((y3_yukarda_kalir) && actual_angle <90){ // if the y3_prime is greater than p3's y and original angle is smaller than 90
                /* calculate the new coordinates of the Point 3, so that it
                 * suits the given angle multiple restriction
                 */
                MyPoint pnew=new MyPoint(p2.getX(),p3.getY());
                double angle=calculateAngle(pnew,p2,p3);
                double new_angle;
                if(pnew.getX()>p3.getX()){
                    new_angle=angle-_angle;
                }
                else
                    new_angle=angle+_angle;

                /*int tmp1=Math.abs((int)(dist_p2_p3*Math.sin(Math.toRadians(new_angle))));
                int tmp2=Math.abs((int)(dist_p2_p3*Math.cos(Math.toRadians(new_angle))));*/
                
                int tmp1=(int)(dist_p2_p3*Math.sin(Math.toRadians(new_angle)));
                int tmp2=(int)(dist_p2_p3*Math.cos(Math.toRadians(new_angle)));
                //p3.setY(p2.getY()- tmp2 );
                if( pnew.getY()> p2.getY()){
                    p3.setY(p2.getY()+ tmp2 );
                }
                else{
                    p3.setY(p2.getY()- tmp2 );
                }
                /*if( pnew.getY()> p2.getY() && new_angle > 0.0){
                    p3.setY(p2.getY()+ tmp2 );
                }
                else if ( pnew.getY()> p2.getY() && new_angle < 0.0){
                    p3.setY(p2.getY()- tmp2 );
                }
                else if( pnew.getY()< p2.getY() && new_angle > 0.0){
                    p3.setY(p2.getY()- tmp2 );
                }
                else
                    p3.setY(p2.getY()+ tmp2 );*/
                
                if(pnew.getX()>p3.getX()){
                    p3.setX(p2.getX()- tmp1 );
                }
                else {
                    p3.setX(p2.getX()+ tmp1 );
                }
                /*if(pnew.getX()>p3.getX() && new_angle >0.0){
                    p3.setX(p2.getX()- tmp1 );
                }
                else if(pnew.getX()>p3.getX() && new_angle <0.0){
                    p3.setX(p2.getX()+ tmp1 );
                }
                else if(pnew.getX()<p3.getX() && new_angle >0.0){
                    p3.setX(p2.getX()+ tmp1 );
                }
                else 
                {
                    p3.setX(p2.getX()- tmp1 );
                }*/
            }
            else if(!(y3_yukarda_kalir) && actual_angle <90){ // if the y3_prime is smaller than p3's y and original angle is smaller than 90
                /* calculate the new coordinates of the Point 3, so that it
                 * suits the given angle multiple restriction
                 */
                MyPoint pnew=new MyPoint(p2.getX(),p3.getY());
                double angle=calculateAngle(pnew,p2,p3);
                double new_angle;
                if( pnew.getX()<p3.getX()){
                    new_angle=angle+_angle;
                }
                else
                    new_angle=angle-_angle;
                /*int tmp1=Math.abs((int)(dist_p2_p3*Math.sin(Math.toRadians(new_angle))));
                int tmp2=Math.abs((int)(dist_p2_p3*Math.cos(Math.toRadians(new_angle))));*/
                int tmp1=(int)(dist_p2_p3*Math.sin(Math.toRadians(new_angle)));
                int tmp2=(int)(dist_p2_p3*Math.cos(Math.toRadians(new_angle)));
                /*if(new_angle<=90.0){
                    p3.setY(p2.getY()+ tmp2 );
                }
                else
                    p3.setY(p2.getY()- tmp2 );
                */
                if( pnew.getY()> p2.getY()){
                    p3.setY(p2.getY()+ tmp2 );
                }
                else{
                    p3.setY(p2.getY()- tmp2 );
                }
                if(pnew.getX()>p3.getX()){
                    p3.setX(p2.getX()- tmp1 );
                }
                else if(pnew.getX()>p3.getX()){
                    p3.setX(p2.getX()+ tmp1 );
                }
                
                /*if( pnew.getY()> p2.getY() && new_angle > 0.0){
                    p3.setY(p2.getY()+ tmp2 );
                }
                else if ( pnew.getY()> p2.getY() && new_angle < 0.0){
                    p3.setY(p2.getY()- tmp2 );
                }
                else if( pnew.getY()< p2.getY() && new_angle > 0.0){
                    p3.setY(p2.getY()- tmp2 );
                }
                else
                    p3.setY(p2.getY()+ tmp2 );

                if(pnew.getX()>p3.getX() && new_angle >0.0){
                    p3.setX(p2.getX()- tmp1 );
                }
                else if(pnew.getX()>p3.getX() && new_angle <0.0){
                    p3.setX(p2.getX()+ tmp1 );
                }
                else if(pnew.getX()<p3.getX() && new_angle >0.0){
                    p3.setX(p2.getX()+ tmp1 );
                }
                else 
                {
                    p3.setX(p2.getX()- tmp1 );
                }*/       
            }
            else if(!(y3_yukarda_kalir) && actual_angle >=90) // if the y3_prime is smaller than p3's y and original angle is greater than 90
            {   /* calculate the new coordinates of the Point 3, so that it
                 * suits the given angle multiple restriction
                 */
                MyPoint pnew=new MyPoint(p3.getX(),p2.getY());
                double angle=calculateAngle(pnew,p2,p3);
                double new_angle;
                if( pnew.getY()>p3.getY()){
                    new_angle=angle+_angle;
                }
                else
                    new_angle=angle-_angle;
                /*int tmp1=Math.abs((int)(dist_p2_p3*Math.cos(Math.toRadians(new_angle))));
                int tmp2=Math.abs((int)(dist_p2_p3*Math.sin(Math.toRadians(new_angle))));*/
                int tmp1=(int)(dist_p2_p3*Math.cos(Math.toRadians(new_angle)));
                int tmp2=(int)(dist_p2_p3*Math.sin(Math.toRadians(new_angle)));
                //p3.setX(p2.getX()+ tmp1 );
                if( pnew.getX()> p2.getX()){
                    p3.setX(p2.getX()+ tmp1 );
                }
                else {
                    p3.setX(p2.getX()- tmp1 );
                }
                if( pnew.getY()> p3.getY()){
                    p3.setY(p2.getY()- tmp2 );
                }
                else {
                    p3.setY(p2.getY()+ tmp2 );
                }
                /*
                if( pnew.getX()> p2.getX() && new_angle > 0.0){
                    p3.setX(p2.getX()+ tmp1 );
                }
                else if ( pnew.getX()> p2.getX() && new_angle < 0.0){
                    p3.setX(p2.getX()- tmp1 );
                }
                else if( pnew.getX()< p2.getX() && new_angle > 0.0){
                    p3.setX(p2.getX()- tmp1 );
                }
                else
                    p3.setX(p2.getX()+ tmp1 );
                
                if( pnew.getY()> p3.getY() && new_angle > 0.0){
                    p3.setY(p2.getY()- tmp2 );
                }
                else if ( pnew.getY()> p3.getY() && new_angle < 0.0){
                    p3.setY(p2.getY()+ tmp2 );
                }
                else if( pnew.getY()< p3.getY() && new_angle > 0.0){
                    p3.setY(p2.getY()+ tmp2 );
                }
                else
                    p3.setY(p2.getY()- tmp2 );*/
            }
            if(preserveEast_West){ // if preserve east-west alignment is selected, reflection of the point 3 will be taken to preserve east-west alignment
               boolean temp = false;
                if(p2.getX()<p3.getX()){ // if the east-west alingment is preserved
                    temp = true;
                }
                if(temp != right){ // if the east-west alingment is not preserved, then the reflection of point 3 will be taken
                    double angle = 100;
                    if(p1.getX() != p3.getX()){
                       angle  = (p1.getY()-p3.getY())/(p1.getX()-p3.getX()); // calculating the angle between point 1 and 3
                    }
                    if(angle > 1){ // the reflection of p3 will be taken according to the line that passes through p1 and p2
                        double a = Double.MAX_VALUE;
                        if(p1.getX() != p2.getX()){
                            a = (p1.getY()-p2.getY())/(p1.getX()-p2.getX()); // angle between point 1 and 2
                        }
                        else if(p1.getY()<p2.getY()){
                            a = -1*a;
                        }
                        double b = p1.getY()-(a*p1.getX());
                        double newA = 1/a;
                        double newB = p3.getY()-(newA*p3.getX());
                        double t = Math.pow(a,2)-1;
                        double newY = Double.MAX_VALUE;
                        if(t != 0){
                            newY = (((t+1)*b)-newB)/t;
                        }
                        double newX = (newY-b)/a;
                        int dist = (int)(2*newX-p3.getX());
                        if(dist >= 500){ // if the point is out of the borders of the map
                            dist = 499;
                        }
                        else if(dist <= 0){ // if the point is out of the borders of the map
                            dist = 6;
                        }
                        p3.setX(dist);
                        dist = (int)(2*newY-p3.getY());
                        if(dist >= 500){ // if the point is out of the borders of the map
                            dist = 499;
                        }
                        else if(dist <= 0){ // if the point is out of the borders of the map
                            dist = 6;
                        }
                        p3.setY(dist);
                    }
                    else if(angle < 1){ // the reflection of p3 will be taken according to the line that is perpendicular to the line that passes through p1 and p2
                        double a = Double.MAX_VALUE;
                        if(p1.getX() != p2.getX()){
                            a = (p1.getY()-p2.getY())/(p1.getX()-p2.getX());
                        }
                        else if(p1.getY()<p2.getY()){
                            a = -1*a;
                        }
                        double b = p3.getY()-(a*p3.getX());
                        double newA = 1/a;
                        double newB = p2.getY()-(newA*p2.getX());
                        double t = Math.pow(a,2)-1;
                        double newY = Double.MAX_VALUE;
                        if(t != 0){
                            newY = (((t+1)*b)-newB)/t;
                        }
                        double newX = (newY-b)/a;
                        int dist = (int)(2*newX-p3.getX());
                        if(dist >= 500){ // if the point is out of the borders of the map
                            dist = 499;
                        }
                        else if(dist <= 0){ // if the point is out of the borders of the map
                            dist = 6;
                        }
                        p3.setX(dist);
                        dist = (int)(2*newY-p3.getY());
                        if(dist >= 500){ // if the point is out of the borders of the map
                            dist = 499;
                        }
                        else if(dist <= 0){ // if the point is out of the borders of the map
                            dist = 6;
                        }
                        p3.setY(dist);
                    }
                } 
            }
            if(preserveNorth_South){ // if preserve north-south alignment is selected, reflection of the point 3 will be taken to preserve north-south alignment
                boolean temp = false;
                if(p2.getY()<p3.getY()){
                    temp = true; // if the p3 is above p2
                }
                if(temp != above){ // if the preservation is spoiled, reflection of the point 3 will be taken
                    double angle = 100;
                    if(p1.getY()<p3.getY()){
                        angle = -100;
                    }
                    if(p1.getX() != p3.getX()){
                       angle  = (p1.getY()-p3.getY())/(p1.getX()-p3.getX()); // calculating the angle
                    }
                    if(angle  > 0){ // reflection of p3 will be taken according to the line that passes through p1 and p2
                        double a = Double.MAX_VALUE;
                        if(p1.getX() != p2.getX()){
                             a = (p1.getY()-p2.getY())/(p1.getX()-p2.getX());
                        }
                        else if(p1.getY()<p2.getY()){
                            a = -1*a;
                        }
                        double b = p1.getY()-(a*p1.getX());
                        double newA = 1/a;
                        double newB = p3.getY()-(newA*p3.getX());
                        double t = Math.pow(a,2)-1;
                        double newY = Double.MAX_VALUE;
                        if(t != 0){
                            newY = (((t+1)*b)-newB)/t;
                        }
                        double newX = (newY-b)/a;
                        int dist = (int)(2*newX-p3.getX());
                        if(dist >= 500){ // if the point is out of the borders of the map
                            dist = 499;
                        }
                        else if(dist <= 0){ // if the point is out of the borders of the map
                            dist = 6;
                        }
                        p3.setX(dist);
                        dist = (int)(2*newY-p3.getY());
                       if(dist >= 500){ // if the point is out of the borders of the map
                            dist = 499;
                        }
                        else if(dist <= 0){ // if the point is out of the borders of the map
                            dist = 6;
                        }
                        p3.setY(dist);
                    }
                    else if(angle < 0){ // reflection of p3 will be taken according to the line that is perpendicular to the line that passes through p1 and p2
                        double a = Double.MAX_VALUE;
                        if(p1.getX() != p2.getX()){
                            a = (p1.getY()-p2.getY())/(p1.getX()-p2.getX());
                        }
                        else if(p1.getY()<p2.getY()){
                            a = -1*a;
                        }
                        double b = p3.getY()-(a*p3.getX());
                        double newA = 1/a;
                        double newB = p2.getY()-(newA*p2.getX());
                        double t = Math.pow(a,2)-1;
                        double newY = Double.MAX_VALUE;
                        if(t != 0){
                            newY = (((t+1)*b)-newB)/t;
                        }
                        double newX = (newY-b)/a;
                        int dist = (int)(2*newX-p3.getX());
                        if(dist >= 500){ // if the point is out of the borders of the map
                            dist = 499;
                        }
                        else if(dist <= 0){ // if the point is out of the borders of the map
                            dist = 6;
                        }
                        p3.setX(dist);
                        dist = (int)(2*newY-p3.getY());
                        if(dist >= 500){ // if the point is out of the borders of the map
                            dist = 499;
                        }
                        else if(dist <= 0){ // if the point is out of the borders of the map
                            dist = 6;
                        }
                        p3.setY(dist);
                    }
                }
            }
        }
    }

    // a function to calculate the distance between given two points
    double distance(MyPoint p1, MyPoint p2){
        return Math.sqrt(Math.pow((p1.getX()-p2.getX()), 2) + Math.pow((p1.getY()-p2.getY()), 2));
    } 
    
    // a function to generate a map from a KML file (this can be used further in the project)
    private void ParseAndStorePoints_from_KMLFile(String KML_File){ 
           try{
                DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                Document doc = docBuilder.parse (new File(KML_File));
                NodeList listOfPlacemarks = doc.getElementsByTagName("Placemark");
                int totalPlacemarks = listOfPlacemarks.getLength();
                System.out.println("Total no of Placemark : " + totalPlacemarks);
                for(int s=0; s<listOfPlacemarks.getLength() ; s++){
                    Node firstPlacemarkNode = listOfPlacemarks.item(s);               
                    if(firstPlacemarkNode.getNodeType() == Node.ELEMENT_NODE){
                        Element element = (Element)firstPlacemarkNode;
                        String name_ = getTagValue("name", element);
                        String description_ = getTagValue("description", element);
                        String [] points_= getTagValue("coordinates", element).split(",");
                       // MyPoint p1=new MyPoint(Integer.parseInt(points_[0]),Integer.parseInt(points_[1]),description_);
                       // this.points.add(p1);               
                    }

                }      
            }catch (SAXParseException err) {
            System.out.println ("** Parsing error" + ", line " 
                 + err.getLineNumber () + ", uri " + err.getSystemId ());
            System.out.println(" " + err.getMessage ());

            }catch (SAXException e) {
            Exception x = e.getException ();
            ((x == null) ? e : x).printStackTrace ();

            }catch (Throwable t) {
            t.printStackTrace ();
            }
    }
    
    // a function to generate a map from a text file
     private void parseTextFile(String path) throws ParserConfigurationException, IOException, SAXException{
        File XmlFile = new File(path);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(XmlFile);
        doc.getDocumentElement().normalize();
 
        NodeList nList = doc.getElementsByTagName("Edge");
 
        for (int i = 0; i<nList.getLength(); i++) {
 
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
 
                Element element = (Element) nNode;
                int p1X = Integer.parseInt(getTagValue("Point1_X", element));
                int p1Y = Integer.parseInt(getTagValue("Point1_Y", element));
                int p2X = Integer.parseInt(getTagValue("Point2_X", element));
                int p2Y = Integer.parseInt(getTagValue("Point2_Y", element));
                String color = getTagValue("Color", element);
            }
        }
                
        nList = doc.getElementsByTagName("Point");
        for (int i = 0; i<nList.getLength(); i++) {
 
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
 
                Element element = (Element) nNode;
                int xCoor = Integer.parseInt(getTagValue("Point_X", element));
                int yCoor = Integer.parseInt(getTagValue("Point_Y", element));
                String desc = getTagValue("Description", element);
            }
        }
        
        nList = doc.getElementsByTagName("Map");
        for (int i = 0; i<nList.getLength(); i++) {
 
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
 
                Element element = (Element) nNode;
                String mapDesc = getTagValue("Map_Description", element);
            }
        }
    }
    
    // to export information from a given tag in an XML. This is used in function parseTextFile
    private static String getTagValue(String sTag, Element eElement) {
        NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
        Node nValue = (Node) nlList.item(0);
        return nValue.getNodeValue();
    }   
}
