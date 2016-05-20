package edu.bridgeport.cartesianpoints;
import java.util.ArrayList;
import com.reliablerabbit.prettyprint.PrettyPrint;

/**
 * A class to allow plotting of points, then displaying it in an ASCII format.
 * @author Mohammad Typaldos [mohammad at reliablerabbit.com]
 */
public class Graph implements GraphInterface {
    /**
     * The list of points to plot.
     */
    private ArrayList<Point>  points      = new ArrayList();
    /**
     * A way to cache the points by using their toString() method
     */
    private ArrayList<String> pointsCache = new ArrayList();
    
    /**
     * The smallest X value, used to know what X value to start at
     */
    private Integer smallestX = 0;
    /**
     * The largest X value, used to know what X value to stop at.
     */
    private Integer largestX  = 0;
    /**
     * The smallest Y value, used to know what Y value to stop at (top-down).
     */
    private Integer smallestY = 0;
    /**
     * The largest Y value, used to know what Y value to start at (top-down).
     */
    private Integer largestY  = 0;
    /**
     * The amount of blank plots that should be on the side of the graph
     */
    private static final int padding = 1;
    /**
     * The code to reset the terminal colors to their default value
     */
    private static final String resetColor = "\033[0m";
    
    /**
     * @return An array list of the plotted points
     */
    public ArrayList<Point> getPoints(){
        return points;
    }
    
    /**
     * Adds a point to the graph by using addPoint(new Point(x, y));
     * @param x The X coordinate
     * @param y The Y coordinate
     * @see Graph#addPoint(edu.bridgeport.cartesianpoints.Point)
     */
    public void addPoint(int x, int y){
        addPoint(new Point(x,y));
    }
    
    /**
     * Adds a point to the graph. Also does some internal caching to help make toString() faster
     * @param point The Point to plot
     */
    public void addPoint(Point point){
        points.add(point);
        
        int x = point.getX();
        int y = point.getY();
        pointsCache.add(String.format("%d%d", point.getX(), point.getY()));
        // Not using toString since it depends on a certain format
        // then changing the format too to use less space, though it doesn't truely affect the application
        //pointsCache.add(point.toString());
        
        if(x > largestX){
            largestX  = x;
        } else if(smallestX > x){
            smallestX = x;
        }
        
        if(y > largestY){
            largestY  = y;
        } else if(smallestY > y){
            smallestY = y;
        }
    }
    
    /**
     * Unplot a point if it exists
     * @param point The point to unplot
     */
    public void removePoint(Point point){
        points.remove(point);
        pointsCache.remove(point.toString());
    }
    
    /**
     * @see Graph#textualGraph() 
     * @return The graph in an ASCII format
     */
    @Override
    public String toString(){
        return textualGraph();
    }
    
    /**
     * Creates an ASCII graph and attempts to use color to make the reading easier.
     * @return A formated String that contains the graph
     */
    public String textualGraph(){
        String   renderedGraph = "";
        
        int longestX, y;
        longestX = smallestX.toString().length();
        y = largestX.toString().length() + 1; // variable name is irelevent here
        // + 1 because of positive sign
        if(y > longestX){
            longestX = y;
        }
        
        int longestY;
        longestY = Integer.valueOf(smallestY - padding).toString().length();
               y = Integer.valueOf(largestY + padding).toString().length() + 1;
        if(y > longestY){
            longestY = y;
        }
        
        y = largestY + padding;
        do {
            if(y % 2 == 0){
                renderedGraph += "\033[1;35m";
            }
            renderedGraph += String.format("%+"+longestY+"d", y) + resetColor + " ";
            int x = smallestX - padding;
            do {
                int iteration = 0;
                boolean added = false;
                
                // the following if statements saves .2 seconds per 5 set of points (total application with 10 points)
                if(pointsCache.contains(String.format("%d%d", x, y))){
                    // loop the points list
                    while(points.size() > iteration){
                        // if the point matches what we're looking at
                        if(points.get(iteration).equals(new Point(x,y))){
                            renderedGraph += "\033[1;31m*" + resetColor;
                            added = true;
                            break;
                        }
                        iteration++;
                    }
                }
                
                if(!added && x == 0 && y == 0){
                    renderedGraph += "\033[1;35mO" + resetColor;
                } else if(!added && y == 0){
                    renderedGraph += "\033[1;35m-" + resetColor;
                } else if(!added && x == 0){
                    renderedGraph += "\033[1;35m|" + resetColor;
                } else if(!added){
                    renderedGraph += "\033[1;34m." + resetColor;
                }
                x++;
            } while(largestX + padding >= x);
            y--;
            renderedGraph += "\n";
        } while(y + padding >= smallestY);
        
        
        // add numbers to bottom in a vertical alignment
        String[] verticalNumbers = new String[longestX];
        Integer i = 0;
        while(longestX > i){
            if(verticalNumbers[i] == null){
                verticalNumbers[i] = PrettyPrint.generateRepeatingString(longestY + padding, ' ');
            }
            i++;
        }
        
        // add the vertical numbers
        int x = largestX + padding;
        i = smallestX - padding;
        while(x + padding > i){
            String number = String.format("%+-" + longestX + "d", i);
            int iteration = 0;
            while(number.length() > iteration){
                String color;
                if(i % 2 == 0){
                    color = "\033[1;35m";
                } else {
                    color = "";
                }
                verticalNumbers[iteration] += color + number.charAt(iteration) + resetColor;
                iteration++;
            }
            i++;
        }
        
        // add the String[]
        renderedGraph += "\n";
        i = 0;
        while(verticalNumbers.length > i){
            renderedGraph += verticalNumbers[i] + "\n";
            i++;
        }
        
        return renderedGraph;
    }
    
    @Override
    public boolean equals(Object object){
        if(object.getClass() != Graph.class) return false;
        Graph oposing = (Graph) object;
        return this.getPoints().hashCode() == oposing.getPoints().hashCode();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (this.points != null ? this.points.hashCode() : 0);
        hash = 29 * hash + (this.pointsCache != null ? this.pointsCache.hashCode() : 0);
        return hash;
    }
}

