package edu.bridgeport.cartesianpoints;

/**
 * A class for handling points on a Cartesian grid.
 * @author Mohammad Typaldos [mohammad at reliablerabbit.com]
 */
public class Point {
    /**
     * The x coordinate of the point
     */
    private int xCord;
    /**
     * The y coordinate of the point
     */
    private int yCord;
    
    /**
     * Makes a new point on the origin
     */
    public Point(){
        xCord = yCord = 0;
    }
    
    /**
     * Makes a point at the coordinates passed into the constructor.
     * Uses setPoint internally
     * @param setXCord The X coordinate
     * @param setYCord The y coordinate
     * @see Point#setPoint(int, int)
     */
    public Point(int setXCord, int setYCord){
        setPoint(setXCord, setYCord);
    }
    
    /**
     * @return the x coordinate of the point
     */
    public int getX(){
        return xCord;
    }
    
    /**
     * @return the y coordinate of the point
     */
    public int getY(){
        return yCord;
    }
    
    /**
     * Sets the x coordinate of the point
     * @param newXCord The new X coordinate 
     */
    public void setX(int newXCord){
        xCord = newXCord;
    }
    
    /**
     * Sets the y coordinate of the point
     * @param newYCord The new y coordinate
     */
    public void setY(int newYCord){
        yCord = newYCord;
    }
    
    /**
     * Set the new coordinates
     * @param newXCord New x coordinate
     * @param newYCord New y coordinate
     */
    public void setPoint(int newXCord, int newYCord){
        setX(newXCord);
        setY(newYCord);
    }
    
    /**
     * @return returns a string, such as [x, y]
     */
    @Override
    public String toString(){
        return String.format("[%d, %d]", getX(), getY());
    }
    
    /**
     * Uses the distance formula to calculate the distance between two points
     * @param point The point that you want to know the distance between
     * @return positive double of the distance between the two points.
     */
    public double distanceBetween(Point point){
        // d = |sqrt( (x2 - x1)^2 + (y2 - y1)^2 )|
        double distance;
        
        // (x2 - x1)^2
        distance = Math.pow(point.getX() - getX(), 2);
        // + (y2 - y1)^2
        distance += Math.pow(point.getY() - getY(), 2);
        // absolute value of the square root
        distance = Math.abs(Math.sqrt(distance));
                
        return distance;
    }
    
    /**
     * Uses the midpoint formula to create a new point that is between the two points
     * @param point The end point of the line.
     * @return A point object that resides in the middle of the line.
     */
    public Point midpointBetween(Point point){
        // x = (x1 + x2) / 2
        // y = (y1 + y2) / 2
        int x = (getX() + point.getX()) / 2;
        int y = (getY() + point.getY()) / 2;
        
        return new Point(x, y);
    }
    
    /**
     * Tells if points are equal to each other. Checks their X and Y values
     * @param object The object to compare
     */
    @Override
    public boolean equals(Object object){
        if(this == object) return true; // same object?
        
        if(object.getClass() == Point.class){
            Point point = (Point) object; // now can cast so have access to class methods
            return(
                    getX() == point.getX() &&
                    getY() == point.getY()
                 ); // same points?
            
        } else if(object.getClass() == Point3D.class){
            // if 3D point, let its equals method handle the check
            Point3D point = (Point3D) object;
            return(point.equals(this));
        } else {
            return false;
        }
    }

    @Override
    /**
     * Provide a unique int based on the data of this object.
     * Objects with the same data should have the same hashCode()
     */
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + this.xCord;
        hash = 29 * hash + this.yCord;
        return hash;
    }
}

