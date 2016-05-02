/**
 * Copyright Sean Talbot 2010.
 * 
 * This file is part of idonmapper.
 * idonmapper is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * idonmapper is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with idonmapper.  If not, see <http://www.gnu.org/licenses/>. 
 */
package idonmapper;
import java.awt.*;
import javax.swing.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.*;
import java.util.regex.*;
import java.io.*;
import org.jdesktop.swingx.*;
import org.jdesktop.swingx.search.*;
import org.apache.xmlgraphics.java2d.ps.EPSDocumentGraphics2D;

/** 
 * HexPanel - coordinate-based Hexagonal grid. 
 * 
 * Idons are associated with Coord objects and then
 * the associated Hexagon is located on the map. This 
 * is then used as the basis for the Idon. 
 * 
 * The Hexagonal orientation is fixed so that one of its points is
 * 'pointing up'.
 */
public class HexPanel extends JPanel implements Scrollable
{
    private ArrayList<Hexagon>      cellList;
    private HashMap<Coord, Hexagon> coordMap;
    private HashMap<Coord, Idon>    idonMap;
    private ArrayList<Coord>        coordList;
    //private ArrayList<Arrow>        arrowList;
    
    public static final int INCREASE = 1, DECREASE = -1;
    
    private static final int TRIP = 3,
                             QUAD = 4;
    
    private int rows = 0, columns = 0;
    
    private static final int ANGLE = 30;
    
    private static final float HALF = 0.5f;
    private float StrokeWidth = 1.2f;
    
    /*
     * hexDist - Distance between the center of 2
     * hex cells. 
     */ 
    private double r, hexDist, hexSize;
    
    public static final int SCROLL_UNIT_INCR = 10, 
                            BLOCK_MULT = 3,                     
                            NOWHERE = -1;
    
    /*
     * A rectangle used by the user to click
     * and drag across cells to select
     * multiple idons
     */ 
    private Rectangle dragRect = null;
    
    /*	 
     *  These constants represent the maximum allowable sizes, 
     *  respectively, of:
     *  
     *  MAX_HEX_SIZE - the sideLength (vertex) of the Hexagons 
     * 					on the grid;
     * 	MAX_COLUMNS	 - the maximum allowable amount
     *  			   of columns on the grid;
     * 	MAX_ROWS	-the maximum allowable amount of rows on the grid;
     *   
     */  
    
    public static final double MAX_HEX_SIZE = 150.0, MIN_HEX_SIZE = 30.0,
                               PADDING = 15;
    public static final int MAX_COLUMNS = 200, MAX_ROWS = 200,
                            HEX_SIZE_INCR = 5;
    
    private double smallIdon, normalIdon, largeIdon;
    
    
    /*
     * HexPanel()
     * Create a HexPanel with the specified number of rows
     * and columns. 
     */
    public HexPanel(final double hexSize, final int rows, final int columns)
    {
        super(null, false);
        setIdonSizes();
        checkArgs(hexSize, rows, columns);    
        this.rows = rows;
        this.columns = columns;
        setHexSize(hexSize);
        setBackground(Color.white);
        
        //arrowList = new ArrayList<Arrow>();
    }
    
    private void setIdonSizes()
    {
        normalIdon = hexSize * Idon.NORMAL_MULT;
        smallIdon = hexSize * Idon.SMALL_MULT;
        largeIdon = hexSize * Idon.LARGE_MULT;
    }
    
    public double getNormalSize()
    {
        return normalIdon;
    }
    public double getSmallSize()
    {
        return smallIdon;
    }
    public double getLargeSize()
    {
        return largeIdon;
    }
    
    protected double getHexSize()
    {
        return hexSize;
    }
    
    protected boolean idonFitsCoord(Idon i, Coord c)
    {
        
        return false;
    }
    
    /**
     * Sets the vertex length of the hexagon cells in on
     * the HexPanel. Note that this method is only to be used 
     * when creating the hexPanel.
     */
    protected void setHexSize(double hexSize)
    {
        this.hexSize = hexSize;
        
        /*
         * hexDist - Distance between the center of 2
         * hex cells. 
         */ 
        hexDist = hexSize * Math.sqrt(3);
        r = Math.cos(ANGLE) * hexSize;
        setGridDimensions();    
        setIdonSizes();
    }
    
    /**
     * Returns the user-drag rectangle
     */ 
    protected Rectangle getDragRect()
    {
        return dragRect;
    }
    
    /**
     * Returns a set of the Idons on this
     * HexPanel. 
     * 
     * Used by the Controller module in the
     * process of saving the application
     * state to file.
     */ 
    protected HashSet<Idon> getIdons()
    {
        return new HashSet<Idon>(idonMap.values());
    }
    
    /**
     * Replaces the drag rectangle with @r
     */ 
    protected void setDragRect(Rectangle r)
    {
        dragRect = r;
    }
    
    
    private void checkArgs(double hexSize, int rows, int columns)
    {
        boolean bad = false;
        
        if(hexSize < 1 || rows < 10 || columns < 10)
        {
            bad = true;
        }
        else if(hexSize > MAX_HEX_SIZE || rows > MAX_ROWS 
                || columns > MAX_COLUMNS)
        {
            bad = true;   
        }
        
        if(bad)
        {
            throw new IllegalStateException(
                                "HexPanel received illegal variable(s): \n"
                                 + hexSize + ", " + rows + ", " + columns);
        }
    }
    
    /*
     * Sets the number of rows and columns of this
     * HexPanel. 
     */
    protected void setGridDimensions()
    {
        createHexagonCells();
        setPreferredSize(calculateMinimumSize());
    }       
    
    /**
     * Attempts to return the nearest unoccupied cell coordinate to the 
     * top left of JScrollPane's viewport
     */
    protected Coord getNearestUnoccupiedCell()
    {
        ArrayList<Coord> visEmpty = getVisibleUnoccupiedCells();
        
        if(!visEmpty.isEmpty())
        {
            return visEmpty.get(0);
        }
        return getUnoccupiedCell();
    }
    
     
    /**
     * Returns an ArrayList of  
     * all Idons on the HexPanel
     * that are currently selected 
     */ 
    protected ArrayList<Idon> getSelectedIdons()
    {
        ArrayList<Idon> sel = new ArrayList<Idon>();
           
        for(Idon i : idonMap.values())
        {
            if(i.getSelectedStatus() == true)
            {
                sel.add(i);               
            }
        }      
        return sel;
    } 
    
  
    
    /**
     * Returns an ArrayList of the coordinates all Idons on the HexPanel
     * that are currently selected 
     */ 
    protected ArrayList<Coord> getSelectedIdonsCoords()
    {
        ArrayList<Coord> coords = new ArrayList<Coord>();
        for(Idon i : idonMap.values())
        {
            if(i.getSelectedStatus())
            {
                coords.add(i.getCoord());
            }
        }
        return coords;
    }
    
    /**
     * Returns an arraylist of cells that overlap with
     * @rect on the Hex Panel
     */ 
    protected ArrayList<Idon> getOverlappingIdons(Rectangle rect)
    {
        if(rect == null)
        {
            throw new NullPointerException("getOverlappingCellCoord: "
                                           + " rectangle sent is null");
        }
        ArrayList<Idon> overlap = new ArrayList<Idon>();
        
        for(Map.Entry<Coord, Idon> e : idonMap.entrySet())
        {
            Coord c = e.getKey();
            Idon i = e.getValue();
            if(i.intersects(rect))
            {
                overlap.add(i);
            }
        }
        return overlap;
    }
    
    /**
     * Destroys the user-drag-rectangle
     */ 
    protected void destroyDragRect()
    {
        dragRect = null;
    }
    
    /*
     * Sets Idons found in the coordinates listed
     * in @newSelection, if present
     */ 
    protected void toggleSelectIdonsInList(ArrayList<Idon> newSelection)
    {
        for(Idon i : newSelection)
        {
            i.toggleSelectStatus();
        }
        return;
    }
    
    /*
     * Returns a reference to the Coord:Hexagon map.
     */ 
    protected HashMap<Coord, Hexagon> getCoordMap()
    {
        return coordMap;
    }
    
    
    /*
     * Returns a list containing the coordinates for
     * all empty hex cells on the hex panel adjacent to
     * Coord @c.
     */ 
    /*protected HashSet<Coord> getEmptyNeighbourCoords(Coord c)
    {
        HashSet<Coord> emptyNeighbours = new HashSet<Coord>();
        HashSet<Coord> neighbours = getNeighbourCoords(c);
        
        if(neighbours == null)
        {
            return null;
        }
        
        for(Coord cur : getNeighbourCoords(c))
        {
            if(!cellContainsIdon(cur))
            {
                emptyNeighbours.add(cur);
            }
        }
        return emptyNeighbours;
    }*/
    
    /**
     * Returns the middle Point of a Hexagon cell on the grid from 
     * a Coordinate, c.
     */ 
    public Point getCellPointFromCoord(Coord c)
    {
        if(c == null)
        {
            throw new NullPointerException();
        }
        final Hexagon hex = coordMap.get(c);
        
        if(hex == null)
        {
            throw new NullPointerException();
        }
        
        return new Point((int)hex.getXPos(), (int)hex.getYPos());
    }
    
  
    /*
     * Returns the coordinate on the grid exactly in the middle of the 
     * panel, so that the controller module can move the viewport to it 
     * at the beginning of the hex panel's creation 
     */ 
    protected Hexagon getMiddleHexagon()
    {
        // not exactly the middle, but it's close... :)
        int x = rows / 2;
        int y = columns / 2;
 
        for(Coord c : coordList)
        {
            if(c.x == x && c.y == y)
            {
                return getHexagonFromCoord(c);
            }   
        }
        return null;
    }    
    
    
    /**
     * Returns the coordinate to the nearest empty cell to @c, or @c 
     * itself, if it is empty. If the hashSet is not null, it will be
     * checked to see if the Coord has already been
     * examined.
     */ 
    protected Coord getNearestEmptyCellToCoord(Coord c,/*, int size,*/ 
                                               HashSet<Coord> exclude)
    {
        /*
        //System.out.println("getNearestEmptyCellToCoord: start: " + c);
        int small = 4, normal = 6, large = 14, useSize = 0;
                
        if(size == Idon.SIZE_SMALL)
        {
            useSize = small;
        }
        else if(size == Idon.SIZE_NORMAL)
        {
            useSize = normal;
        }
        else
        {
            useSize = large;
        }
                          
        ArrayList<Direction> path;
        
        Coord coord = null;*/
        
        // Look in each direction, record the distance to the first empty cell.
        // return the nearest one.
        
        int distance = 0;
        int nearestDist = 0;
        Coord nearest = null;

        for(final Direction d : Direction.values())
        {
            Coord currentPos = c;
            boolean foundEmpty = false;
            boolean leftGrid = false;
            distance = 0;
            
            while(/*currentPos != null && */!foundEmpty && !leftGrid)
            {
                // look in each cell in the current direction until we hit
                // an empty cell or we leave the grid.
                currentPos = Direction.directionToCoord(currentPos, d);
                
                if(currentPos == null)
                {
                    leftGrid = true;
                }
                
                //System.out.println("looking at " + currentPos);
                
                distance++;
                
                if(!exclude.contains(currentPos))
                {
                    final Point p = getCellPointFromCoord(currentPos);
                    
                    if(getIdonFromPoint(p) == null)
                    {
                        //System.out.println("found empty Coord: " + 
                        //                    currentPos);
                        if(nearest == null)
                        {
                            nearest = currentPos;
                            nearestDist = distance;
                        }
                        else
                        {
                            if(distance < nearestDist)
                            {
                                nearest = currentPos;
                                nearestDist = distance;
                            }
                        }
                        foundEmpty = true;
                    }
                    else
                    {
                        exclude.add(currentPos);
                    }
                }
            }
        }
        
        return nearest;
        
        /*
        Coord coord = null;
        for(Direction d : Direction.values())
        {
            path = addXDirections(d, useSize);
            coord = getCoordFromPath(c, path);
            
            if(coord != null && !exclude.contains(coord)) 
            {
                Point p = getCellPointFromCoord(coord);
                if(getIdonFromPoint(p) == null)
                {
                    System.out.println("\treturning " + coord); 
                    return coord;
                }
                else
                {
                    exclude.add(coord);
                }
            }
        }
        return getNearestEmptyCellToCoord(coord, size, exclude);*/
    }
    /*protected Coord getNearestEmptyCellToCoord(Coord c, HashSet<Coord>
                                               exclude)
    {  
        if(c == null)
        {
            return null;
        }
        
        System.out.println("getNearestEmptyCellToCoord: " + c);

        if(getIdonFromCoord(c) == null)
        {
            if(exclude != null)
            {
                if(!exclude.contains(c))
                {
                    return c;
                }
            }
            else
            {
                return c;
            }
        }
        HashSet<Coord> unoccupied = getEmptyNeighbourCoords(c);
        
        if(unoccupied.isEmpty())
        {
            System.out.println("coord " + c.toString() + " has no"
                                  + " empty neighbours!");
            /*
             * Coord c has no empty adjacent
             * cells, so look for the nearest 
             * empty cell to all of the
             */ 
             /*
             HashSet<Coord> neighbours = getNeighbourCoords(c);
             
             for(Coord curCoord : neighbours)
             {
                 return getNearestEmptyCellToCoord(curCoord, exclude);
             }
        }
        else
        {
            HashSet<Coord> emptyList = new HashSet<Coord>(unoccupied);
            
            for(Coord curCoord : emptyList)
            {
                return curCoord;
            }
        }
        
        return null;       
    }*/
    
    /**
     * Finds the Coords of where any sized neighbour Idons may be, from
     * an Idon starting at c, of size idonSize. 
     */ 
   /* protected HashSet<Coord> getNeighbourCoords(Coord c, int idonSize)
    {
        
        HashSet<Coord> neighbourCoords = new HashSet<Coord>();
        
                
        for(Direction d : Direction.values())
        {     
            switch(idonSize)
            {
                case Idon.SIZE_SMALL:
                    neighbourCoords.addAll(getNeighbourCoordsOfSmall(c, d));
                    
                break;
                case Idon.SIZE_NORMAL:
                
                break;
                
                case Idon.SIZE_LARGE:
                
                
                break;   
            }
        }
        return neighbourCoords;
    }
    */
    /*private HashSet<Coord> getNeighbourCoordsOfSmall(Coord c, Direction d,
                                                int size)
    {
        int smallToSmall = 3;
        int smallToNorm = 1;
        int bigPath1 = 3;
        int bigPath2 = 4;
        
        ArrayList<Direction> path;
        HashSet<Coord> neighbourCoords = new HashSet<Coord>();
        
        switch(size)
        {
            case Idon.SIZE_SMALL:
                // look for same size 
                path = addXDirections(d, smallToSmall);
                Coord tmpCoord = addCoordToSetFromPath(c, path, neighbourCoords);
            break;
            
        
        path = addXDirections(d, smallToNorm);
        
        // Next, we need to find the Coord to the left 
        // and the right of tmpCoord, so create 2 paths
        // from the previous one, each going separate ways
        
        ArrayList<Direction> pathLeft = new ArrayList<Direction>(path);
        ArrayList<Direction> pathRight = new ArrayList<Direction>(path);
        
        pathLeft.add(d.turnLeft());
        pathRight.add(d.turnRight());
        
        Coord left = addCoordToSetFromPath(tmpCoord, pathLeft, neighbourCoords);
        Coord right = addCoordToSetFromPath(tmpCoord, pathRight, neighbourCoords);
        
        // Finally, we need to find the Coords fo large neighbours of
        // the small Idon. This is trickier, as each side has 4
        // possible locations for neighbours
        
        // Reuse 'tmpCoord' (which is still 3 cells away from the centre
        // of our small Idon in the current direction, d)
        
        ArrayList<Direction> path1, path2, path3, path4;
        
        
        path1 = addXDirections(d, smallToSmall);
        
        Direction turn = d.turnRight();
        // Add 3 jumps to the middle of possible large Idon 1
        path1.add(turn);
        path1.add(turn);
        path1.add(turn);
        
        // Use the path to add the Coords
        addCoordToSetFromPath(tmpCoord, path1, neighbourCoords);
        
        // Next possible large Idon. This is 4 cells along from tmpCoord,
        // then one to the right
        path2 = addXDirections(d, bigPath2);
        path2.add(d.turnRight());
        
        // Use the path to add the Coords
        addCoordToSetFromPath(tmpCoord, path2, neighbourCoords);
        
        // Third possible large Idon. This is four along from tmpCoord
        // (same as path2) then one to the left 
        path3 = addXDirections(d, bigPath2);
        path3.add(d.turnLeft());
        
        // Use the path to add the Coords
        addCoordToSetFromPath(tmpCoord, path3, neighbourCoords);
        
        // Path 4 - Last possible Idon coord. 3 along from tmpCoord in
        // Direction d, then 3 to the left
        
        path4 = addXDirections(d, bigPath1);
        turn = d.turnLeft();
        
        // Add 3 jumps to the middle of possible large Idon 1
        path4.add(turn);
        path4.add(turn);
        path4.add(turn);
        
        addCoordToSetFromPath(tmpCoord, path4, neighbourCoords);

        
        return neighbourCoords;
    }
*/
    
    /*
     * Returns the necessary Coordinates to find neighbours of an Idon
     * at Coord c that has a size of @idonSize.
     */ 
    /*protected HashSet<Coord> getNeighbourCoords(Coord c, int idonSize)
    {
        HashSet<Coord> posNeigh = new HashSet<Coord>();
        ArrayList<Direction> path = new ArrayList<Direction>();
        Coord coord;
        int smallSizeSteps = 3;
        int normalSizeSteps = 3;
        int largeSizeSteps = 7;
        
        // Create the path for each of the possible adjacent Idons and
        // add to the possible neighbours set       
        for(Direction d : Direction.values())
        {
            switch(idonSize)
            {
                case Idon.SIZE_SMALL:
                
                    // 2 positions in each direction to find any size
                    // neighbours.
                    path = addXDirections(d, smallSizeSteps);
                    addCoordToSetFromPath(c, path, posNeigh);
                break;
                case Idon.SIZE_NORMAL:
                
                    // For normally sized Idons, we just look 4 cells
                    // along
                    path = addXDirections(d, normalSizeSteps);
                    Coord edge = addCoordToSetFromPath(c, path, posNeigh);
                    getNormalIdonOffsetCoords(d, edge, posNeigh);
                break;
                case Idon.SIZE_LARGE:
                    // For large Idons it is slightly more complex. 
                    
                    // We need to find 3 Coordinates for each Direction,
                    // the first of which is simply 7 cells along
                    
                    path = addXDirections(d, largeSizeSteps);                                       
                    
                    // Record the return Coord as a reference 
                    Coord diagonal = addCoordToSetFromPath(c, path,
                                                           posNeigh);

                    // The two Coordinates we want are both two cells
                    // away, symmetrically, left and right, and we can
                    // use @diagonal as a reference cell.
                    getLargeIdonOffsetCoords(d, diagonal, 
                                                     posNeigh);
                    
                break; 
            }
        }
        return posNeigh;  
    }*/
    
    private ArrayList<Direction> addXDirections(Direction d, int count)
    {
        ArrayList<Direction> dirs = new ArrayList<Direction>();
        for(int i = 0; i < count ; i ++)
        {
            dirs.add(d);
        }
        return dirs;
    }

    /*
     * Utility method for adding the final two neighbouring Coords
     * for normal size Idons (the ones offset symmetrically from 
     * Direction d) to the possible neighbours HashSet.
     */
    private void getNormalIdonOffsetCoords(Direction d, Coord outside, 
                                            HashSet<Coord> posNeigh)
    {
        // Right side
        Direction turn = d.turnRight();
        Coord tmpCoord = Direction.directionToCoord(outside, turn);
        
        if(tmpCoord != null)
        {
            posNeigh.add(tmpCoord);
        } 
        
        // left side
        turn = d.turnLeft();
        tmpCoord = Direction.directionToCoord(outside, turn);
        
        if(tmpCoord != null)
        {
            posNeigh.add(tmpCoord);
        } 
        
        return;
    } 
    
    /*
     * Utility method for adding the final two neighbouring Coords
     * for large Idons (the ones offset symmetrically from Direction d)  
     * to the possible neighbours HashSet.
     */ 
    private void getLargeIdonOffsetCoords(Direction d, Coord 
                                                diagonal, HashSet<Coord>
                                                posNeigh)
    {
        // Right side
        Direction turn = d.turnRight();
        Coord tmpCoord = Direction.directionToCoord(diagonal, turn);
        turn = turn.turnRight();
        tmpCoord = Direction.directionToCoord(tmpCoord, turn);
        
        if(tmpCoord != null)
        {
            posNeigh.add(tmpCoord);
        }   
        
        // Left side
        turn = d.turnLeft();
        tmpCoord = Direction.directionToCoord(diagonal, turn);
        turn = turn.turnLeft();
        tmpCoord = Direction.directionToCoord(tmpCoord, turn);
        
        if(tmpCoord != null)
        {
            posNeigh.add(tmpCoord);
        }   
    }
    
    
    /*
     * Utility method to find cell Coords by following a path
     * of Directions from a start Coord. Coords at the end of the path
     * are added to the specified HashSet.
     * 
     * Returns the resulting Coord at the end of the path (or null).
     */ 
    private Coord addCoordToSetFromPath(Coord start, 
                                        ArrayList<Direction> path,
                                        HashSet<Coord> posNeigh)
    {
        Coord c = getCoordFromPath(start, path); 
        
        if(c != null)
        {
            posNeigh.add(c);
        }    
        return c;
    }
    
   
    
    /*
     * Returns a list of coordinates for all cells (empty or
     * otherwise) that are adjacent to @c.
     */ 
    /*
     * old version, before varying size hexagons:
     * protected HashSet<Coord> getNeighbourCoords(Coord c)
    {
        if(c == null)
        {
            return null;
        }
        System.out.println("getNeighbourCoords: " + c);
        HashSet<Coord> neighbours = new HashSet<Coord>();
        
        for(Direction d : Direction.values())
        {
            Coord adj = d.directionToCoord(c, d);

            /*
             * Only add to the list if the Coord actually
             * exists on the hex panel
             */ 
            /*
            if(isFoundInCoordList(adj))
            {
                neighbours.add(adj);           
            }
        }
        return neighbours;
    }*/
    
    /*
     * Returns true if a coordinate with the
     * same contents as @c exists in the 
     * hex panel's coordinate list
     */ 
    private boolean isFoundInCoordList(Coord c)
    {
        if(c == null)
        {
            return false;
        }
        for(Coord currCoord : coordList)
        {
            if(c.x == currCoord.x && c.y == currCoord.y)
            {
                return true;
            }
        }
        return false;
    }
    
    /** 
     * Returns the Coord for an unoccupied cell inside (or near to) the 
     * the JScrollPane's viewport, or null if none are found.
     */
    protected ArrayList<Coord> getVisibleUnoccupiedCells()
    {
        /*
         * First grab a rectangle of the visible area on the
         * hex panel
         */ 
        Rectangle vis = visibleArea();
        
        if(vis == null)
        {
            throw new IllegalStateException(
                               "getVisibleUnoccupiedCells: no visible" +
                               " area available!");
        }
        
        ArrayList<Coord> visEmpty = new ArrayList<Coord>();
        
        /*
         * Next examine all those hexagons on the
         * panel which intersect the visible area
         */         
        for(Hexagon h : cellList)
        {
            if(h.isEnclosedBy(vis))
            {
                 Coord c = h.getCoord();
       
                 if(!cellContainsIdon(c))
                 {              
                     visEmpty.add(c);
                 }
            }
        } 
        return visEmpty;
    }
        
    protected Idon getIdonFromPoint(Point p)
    {
        for(Idon i : idonMap.values())
        {
            if(i.getShape().contains(p))
            {
                return i;
            }
        }
        return null;
    }
    
    /**
     * Returns the Idon enclosing the Point2D.Double p, or null.
     */ 
    protected Idon getIdonFromPoint(Point2D.Double p)
    {
        for(Idon i : idonMap.values())
        {
            if(i.getShape().contains(p))
            {
                return i;
            }
        }
        return null;
    }
    
    /**
     * Changes the size of the Idons specified, where change
     * represents INCREASE or DECREASE
     */ 
    public ArrayList<Coord> alterIdonSize(Collection<Coord> idonCoords, int change)
    {
        ArrayList<Coord> changed = new ArrayList<Coord>();

        if(change != INCREASE && change != DECREASE)
        {
            throw new IllegalArgumentException("alterIdonSize: wrong " 
                                                + "arg");
        }
        for(Coord c : idonCoords)
        {
            Idon i = getIdonFromCoord(c);
            if(change == INCREASE)
            {
                if(i.increaseSize())
                {
                    changed.add(c);
                }
            }
            else if(change == DECREASE)
            {
                if(i.decreaseSize())
                {
                    changed.add(c);
                }
            }
        }
        repaint();
        return changed;        
    }
   /* public ArrayList<Idon> alterIdonSize(Collection<Idon> idons, int change)
    {
        
        
        ArrayList<Idon> changed = new ArrayList<Idon>();

        if(change != INCREASE && change != DECREASE)
        {
            throw new IllegalArgumentException("alterIdonSize: wrong " 
                                                + "arg");
        }
        for(Idon i : idons)
        {
            if(change == INCREASE)
            {
                if(i.increaseSize())
                {
                    changed.add(i);
                }
            }
            else if(change == DECREASE)
            {
                if(i.decreaseSize())
                {
                    changed.add(i);
                }
            }
        }
        repaint();
        System.out.println("repainted");
        return changed;
        
    }* */
    
    /** 
     * Returns the coordinates of the first unoccupied cell on the grid,
     * starting at the top left and working horizontally 
     */
    public Coord getUnoccupiedCell()
    {
        for(Coord c : coordList)
        {
            if(!cellContainsIdon(c))
            {
                Point p = getCellPointFromCoord(c);
                if(getIdonFromPoint(p) == null)
                {
                    return c;
                }
            }
        }
        throw new IllegalStateException("No empty cells left!");
    }
    
    /**
     * Return the Hexagon at the specified Coordinate.
     */
    protected Hexagon getHexagonFromCoord(Coord c)
    {
        try
        {
            Coord d = getCoordFromList(c.x, c.y);
            
            Hexagon h = coordMap.get(d);
            return h;
        }
        catch(NullPointerException e)
        {
            System.out.println("getHexagonFromCoord returned null!");
            return null;
        }
    }
    
    /*
     * Creates the hex cells on the hex panel.
     * 
     * Note the boolean to tell the method
     * whether or not to destroy existing map
     * data.
     */ 
    private void createHexagonCells()
    {
        cellList = new ArrayList<Hexagon>(rows*columns);
        coordList = new ArrayList<Coord>(rows*columns);
        coordMap = new HashMap<Coord, Hexagon>();        
        idonMap = new HashMap<Coord, Idon>();
        
        int incrOdd = 1;
        int incrEven = 3;
        
        for(int col = 1; col <= columns; ++col)
        {
            if(isOdd(col))
            {
                createRow(col, hexDist/2, hexSize*incrOdd);
                incrOdd += TRIP;
            }
            else
            {
                createRow(col, hexDist, ((hexSize/2) * (col + incrEven)));
                incrEven += QUAD;
            }
        }
        repaint();
    }
    
    /*
     * Set and return a @Dimension containing the minimum size the panel
     * must be in order to display all of the hex cells, according to the
     * rows and columns.
     */
    public Dimension calculateMinimumSize()
    {
        double x, y = 0;
        x = (hexDist * rows) + (hexDist/2);
        y = (hexDist * columns);
        y -= (hexDist * ((double)columns / 8));
        
        Dimension min = new Dimension((int)(x + PADDING), (int)y);
        return min;
    }
    
    /*
     * Create a row of hexagonal cells that will later be
     * drawn onto the HexPanel. 
     * 
     * Deals with odd and even rows separately
     */
    private void createRow(int column, double xPos, double yPos)
    {
        int incr = 1;
        
        for(int x = 1; x <= rows; ++x)
        {
            Coord coord = new Coord(x, column);
            Hexagon h = new Hexagon(hexSize, (xPos * incr), yPos,
                                                            coord);
            coordMap.put(coord, h);
            coordList.add(coord);
            cellList.add(h);
            
            if(isOdd(column))
            {
                incr += 2;
            }
            else
            {       
                ++incr;
            }
        }
    }
    
    private boolean isOdd(int n)
    {
        if(n % 2 != 0 && n >= 0)
        {
            return true;
        }
        return false;
    }
    
    /*
     * Returns true if the MouseEvent @e featured
     * button number @n
     */ 
    private boolean isButton(MouseEvent e, int n)
    {
        if(e.getButton() == n)
        {
            return true;
        }
        return false;
    }
    
    
    /**
     * Returns a mapping of the neighbouring Idons to a specified
     * Idon, along with their Coordinates.
     */ 
    /*
     * This views cells as 'neighbours' if they share a line. This
     * is worked out by checking the centre point of various cells 
     * surrounding the given Idon Coord, to see if they contain another
     * idon.
     * 
     * In order to *find* the Coords, the program needs to know the 
     * directions to them, which this method supplies. 
     */ 
    /*public HashSet<Idon> getNeighbours(Idon idon)
    {
        HashSet<Idon> neigh = new HashSet<Idon>();
        
        for(Coord c : getNeighbourCoords(idon.getCoord(), idon.getSize()))
        { 
            //Point p = getCellPointFromCoord(c);           
            //Idon i = getIdonFromPoint(p);
            
            Idon i = getIdonFromCoord(c);
            
            if(i != null)
            {
                neigh.add(i);
            }
        }
        return neigh;
    } */
    
    /**
     * Attempts to locate a dropping point for an Idon of specified size 
     * near to the Coord c, assuming that Coord c 
     */ 
    /*protected Coord getNearbyEmptyCoord(Coord c, int size, 
                                        HashSet<Coord> coordsTried)
    {
        
        ArrayList<Direction> path;
        
        for(Direction d : Direction.values())
        {
            switch(size)
            {
                case Idon.SIZE_SMALL:
                    
                     path addXDirections(       
                break;
                
                case Idon.SIZE_NORMAL:
                
                
                break;
                
                
                case Idon.SIZE_LARGE:
                
                
                break;
            }
        }        
    }*/

       
    /**
     * Returns a mapping of the Idons surrounding a particular Idon
     * and the Directions in which they occur.
     **/
    /*protected HashMap<Direction, Idon> getNeighbours(Idon idon)
    {
        Coord current = idon.getCoord();
        
                
        HashMap<Direction, Idon> map = new HashMap<Direction, Idon>();
        
        
        
        
        for(Direction d : Direction.values())
        {
            Coord c = Direction.directionToCoord(current, d);
            if(containsCoord(c))
            {
                Idon i = getIdonFromCoord(c);
                if(i != null)
                {
                    map.put(d, i);
                }
            }            
        }
        return map;
    }*/
    
    
    
    /*
     * Returns the Coordinate matching @x and @y
     */ 
    protected Coord getCoordFromList(int x, int y)
    {
        for(Coord c : coordList)
        {
            if(c.x == x && c.y == y)
            {
                return c;
            }
        }
        return null;
    }
    
    /**
     * Recursively adds all the idons linked to idon @i on the grid
     * to the Idon HashSet.
     * 
     * @param i The idon to use as the start.
     * 
     * @param linked The destination for linked Idons.
     */
    /*protected void getAllLinked(Idon i, HashSet<Idon> linked)
    {
        if(linked.contains(i))
        {
            return;
        }
        linked.add(i);
        
        for(Idon idon : getNeighbours(i))
        {
            getAllLinked(idon, linked);
        }
        
        //for(Idon idon : getNeighbours(i).values())
        //{
          //  getAllLinked(idon, linked);
        //}
        return;
    }*/
    
    
    /* 
     * Returns true if the specified Idon is within any of the collections inside 
     * collection @all.
     */
    private boolean collectionsContainsIdon(HashSet<HashSet<Idon>> all, 
                                            Idon idon)
    {
        if(all.isEmpty())
        {
            return false;
        }
        
        Iterator<HashSet<Idon>> iter = all.iterator();
        
        while(iter.hasNext())
        {
            HashSet<Idon> hm = iter.next();
            if(hm.contains(idon))
            {
                return true;
            }
        }
        
        return false;
    }
    
    /*public void printNeighbours(Idon i)
    {
        //HashMap<Direction, Idon> neighbours = getNeighbours(i);
        System.out.println(i + "'s neighbours:");
        
        for(Idon idon : getNeighbours(i))
        {
            System.out.print("\t" + idon + ",");
        }
        System.out.println();
        
        /*for(Map.Entry<Direction, Idon> e : neighbours.entrySet())
        {
            Direction d = e.getKey();
            Idon idon = e.getValue();
            System.out.println("\tdirection " + d + ": " + idon);
        }
    }*/
    
    /*
     * Returns the Idon found at Coord @c, or null.
     */ 
    protected Idon getIdonFromCoord(Coord c)
    {
        if(c == null)
        {
            throw new NullPointerException("getIidonFromCoord: coord" 
                                            + " is null!");
        }
        
        Coord coord = getCoordFromList(c.x, c.y); 
        
        if(coord == null)
        {
            throw new NullPointerException();
        }
        
        return idonMap.get(coord);
    }
    
    /*
     * Returns true if the coordMap 
     * Coord contains @c.
     */ 
    protected boolean containsCoord(Coord c)
    {
        for(Map.Entry<Coord, Hexagon> e : coordMap.entrySet())
        {
            Coord coord = e.getKey();
            if(coord.equals(c))
            {
                return true;
            }
        }
        return false;
    }
    
    /*
     * If MouseEvent @e is inside a hex cell, return it,
     * or else return null
     */ 
    private Hexagon getCellFromPoint(Point p)
    {
        ListIterator<Hexagon> i = cellList.listIterator();
        
        while(i.hasNext())
        {
            Hexagon cell = i.next();
            
            if(cell.containsPoint(p))
            {
                return cell;
            }
        }
        return null;
    }
    
    
    /**
     * Returns the coord for the 
     * hex cell at Point p, or null
     * if there isn't one.
     * 
     * @param p The point to locate in
     * a Hexagon Cell
     * 
     * @return The Coord associated with
     * the Hexagon Cell.
     */ 
    protected Coord getCoordFromPoint(Point p)
    {
        Hexagon h = getCellFromPoint(p);
        if(h == null)
        {
            return null;
        }
        for(Map.Entry<Coord, Hexagon> e : coordMap.entrySet())
        {
            Coord c = e.getKey();
            Hexagon hex = e.getValue();
            
            if(hex.equals(h))
            {
                return c;
            }
        }
        return null;
    }
    
    /**
     * Utility method to output the contents of the panel to a 
     * postscript file.
     */ 
    public void paintToPostScript(EPSDocumentGraphics2D g2d)
    {
        setUpGraphics2D(g2d);
        paintIdons(g2d);
    }

    /*
     * Paints the Idons and Hex cells if they intersect 
     * the scrollpane's viewport.
     */
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2 = setUpGraphics(g);
       
        /*
         * Painting the grid of hex cells
         * eats up quite a bit of processing
         * power, so it has been turned off.
         * 
         * Uncomment the following line to
         * display it again.
         */ 
        paintGrid(g2); 
        paintIdons(g2);
     // paintArrows(g2);
        drawDragRect(g2);
        return;
    }
    
    /*public void paintArrows(Graphics2D g)
    {
        for(Arrow arrow : arrowList)
        {
            arrow.draw(g);
        }
    }*/
    /**
     * Painting method that allows the SVG exporter to grab the Idon 
     * graphics.
     */ 
    protected void paintSVG(Graphics2D g)
    {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                           RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(StrokeWidth));
        
        paintIdons(g);
    }
    
    /**
     * Draws the @dragRect on the hex panel
     */ 
    private void drawDragRect(Graphics2D g2)
    {
        if(dragRect == null)
        {
            return;
        }
        g2.setStroke(new BasicStroke(StrokeWidth));
        g2.setPaint(new Color(0, 0, 0)); 
        g2.draw(dragRect);
        return;
    }
    
    /*
     * Returns a rectangle delimiting the visible area of
     * this HexPanel according to the JScrollPane's viewport.
     */
    protected Rectangle visibleArea()
    {
        return Controller.getHexScroller().getViewport().getViewRect();
    }
    
    /*
     * Paints the Hexagonal grid (not the Idons) cells using the 
     * JXPanel's Graphics2D object .
     *
     * For greater efficiency, Hexagon cells are drawn only if 
     * their bounding Rectangles intersect 
     * the Controller's current JViewport rectangle. 
     */
    private void paintGrid(final Graphics2D g2)
    {
        final Rectangle visible = visibleArea();
        
        for(final Coord c : coordList)
        {
            final Hexagon h = coordMap.get(c);
            if(h.intersects(visible))
            {
                h.draw(g2);
            }
        }
    }
    
    /**
     * Paints the Idons that are visible, according to 
     * the visibleArea() Rectangle.
     */
     /*
      * Note that Idons are sorted into separate lists
      * and drawn in different stages according to
      * their properties:
      * 
      * 1) Normal, stationary, non-selected
      * 2) Selected Idons
      * 3) Idons being dragged
      * 
      * This gives a 'z-order' layering appearance, so
      * (for example) stationary Idons will
      *  appear behind any being dragged.
      */ 
    private void paintIdons(Graphics2D g2)
    {
        Rectangle visible = visibleArea();
        
        ArrayList<Idon> selected = new ArrayList<Idon>();
        ArrayList<Idon> dragged = new ArrayList<Idon>();
         
        for(Idon i : idonMap.values())
        {
            /*
             * Find those Idons intersecting the
             * visible area
             */ 
            if(i.intersects(visible))
            {
                if(i.getDragState() == true)
                {   
                    dragged.add(i);               
                }
                else if(i.getSelectedStatus() == true)
                {
                    selected.add(i);
                }
                else
                {
                    /*
                     * Draw normal (unselected, non-moving) first
                     */ 
                    i.draw(g2);
                }
            }
        }
        
        /*
         * Draw selected Idons next 
         */
        Iterator<Idon> it = selected.iterator();
        while(it.hasNext())
        {
            Idon i = it.next();
            i.draw(g2);
        }
        
        /*
         * Finally draw Idon[s] being dragged on top
         */ 
        it = dragged.iterator();
        while(it.hasNext())
        {
            Idon i = it.next();
            i.draw(g2);
        }
        return;
    }
    
    public boolean overlapsExistingIdon(Area a)
    {
        return false;
    }
    
    private Graphics2D setUpGraphics(Graphics g)
    {
        Graphics2D g2 = (Graphics2D)g;
        setUpGraphics2D(g2);
        return g2;
    }
    
    private void setUpGraphics2D(Graphics2D g2)
    {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(StrokeWidth));
        g2.setPaint(new Color(0, 0, 0)); 
    }
    
    /**
     * Returns true if the cell at Coord @c
     * contains an Idon.
     */
    public boolean cellContainsIdon(Coord c)
    {
        if(idonMap.containsKey(getCoordFromList(c.x, c.y)))
        {
            return true;
        }
        return false;
    }
    
    /**
     * Returns a collection of Coords of each visible
     * hexagon on the hex panel. Note that this includes
     * both empty and non-empty idons
     */ 
    protected HashSet<Coord> getVisibleHexagonCoords()
    {
        Rectangle visible = visibleArea();
        HashSet<Coord> visCoords = new HashSet<Coord>();
        
        for(Coord c : coordList)
        {
            Hexagon h = getHexagonFromCoord(c);
            
            if(h.intersects(visible))
            {
                visCoords.add(c);
            }
        }
        return visCoords;
    }
    
    /**
     * Conveniance method for adding 
     * a new Idon which does not include
     * a Color argument. The default
     * Color is used instead.
     */ 
    protected void addIdon(Coord c, String s, int size)
    {
        Color col = Controller.getColorFromString("default");
        this.addIdon(c, s, col, size);
    }
    
    /**
     * Associates a hexagon cell with an idon.
     */
    protected Idon addIdon(Coord c, String s, Color col, int size)
    {   
        /*
         * replace the coord with the hexPanel's
         * actual coord
         */ 
        c = getCoordFromList(c.x, c.y);
        
        
        Hexagon h = getHexagonFromCoord(c); 
          
        
        
        double idonSize = calculateIdonSize(h.getSideLength(), size);
        //Idon i = new Idon(h, s, col, size);       
        Idon i = new Idon(idonSize, h.getXPos(), h.getYPos(), 
                          c, s, col, size);
        
        idonMap.put(c, i);
        
        /*
         * Check that the Idon is visible to the user
         */ 
         if(!i.isEnclosedBy(visibleArea()))
         {
             /*
              * Scroll the hexScroller so that the
              * idon is in view
              */ 
              scrollRectToVisible(i.getShape().getBounds());
         }
         return i;
    }
    
    public double calculateIdonSize(double hexSize, int size)
    {
        switch(size)
        {
            case Idon.SIZE_SMALL:
                return hexSize * Idon.SMALL_MULT;
            case Idon.SIZE_NORMAL:
                return hexSize * Idon.NORMAL_MULT;
            case Idon.SIZE_LARGE:
                return hexSize * Idon.LARGE_MULT;
        }
        
        throw new IllegalArgumentException();        
    }
    
    
    /**
     * Returns true if the HexPanel contains an Idon
     * with the String @s.
     */
    public boolean containsIdea(String s)
    {            
        for(Map.Entry<Coord, Idon> e : idonMap.entrySet())
        {
            if(s.equals(e.getValue().getIdea()))
            {
                return true;    
            }
        }
        return false;
    }
    
    /*
     * Print out the idons as a list (for debugging
     * purposes)
     */ 
    public void printIdons()
    {
        System.out.println("    IDONS:");
        for(Map.Entry<Coord, Idon> e : idonMap.entrySet())
        {
            Coord c = e.getKey();
            Idon i = e.getValue();
            System.out.println(c + "--->   " + i);
        }
    }
        
    /** 
     * Remove a hexagon/idon association
     * from the hashmap.
     */
    protected void removeIdon(Coord c)
    {
        /*System.out.println("removing " + idonMap.get(c) 
            + " from " + c);*/
        idonMap.remove(c);
        repaint();
    }
    
    
    
    private int getIdonCount()
    {
        return idonMap.size();
    }
    
    private void printOutHashSet(HashSet<?> h)
    {
        Iterator it = h.iterator();
        while(it.hasNext())
        {
            System.out.print(it.next().toString() + ", ");
        }
        System.out.println();
    }
    
    /**
     * Conveniance method to find out if a Hexagon contains an Idon.
     * <p>
     * @return True if c has no Idon associated with it.
     * @param c The Coord to look up.
     */
    public boolean isEmpty(Coord c)
    {
        if(getIdonFromCoord(getCoordFromList(c.x, c.y)) == null)
        {
            return true;
        }
        return false;
    } 
    
    /**
     * Deselects any selected Idons 
     */ 
    protected void deselectIdons()
    {
        for(Idon i : idonMap.values())
        {
            if(i.getSelectedStatus() == true)
            {
                i.deselect();
            }
        }
        repaint();
    }
    
    /**
     * Returns a HashMap of the Idons and Coordinates of their new positions 
     * in the dropPoints map, if they can be slotted into the panel from
     * specific point. 
     * 
     * If any Point is outside the grid or inside a cell that is found 
     * to be occupied, it is added to the 'exclude' HashSet, and null is 
     * returned. This is used so that no Coord is queried twice.
     */ 
    public HashMap<Idon, Coord> getPlacementMap(HashMap<Idon, 
                                                Point> dropPoints,
                                                HashSet<Coord> exclude)
    {
        HashMap<Idon, Coord> dropCoords = new HashMap<Idon, Coord>(
                                                     dropPoints.size());    
        for(Map.Entry<Idon, Point> e : dropPoints.entrySet())
        {
            Idon i = e.getKey();
            Point p = e.getValue();
            
            Coord c = getCoordFromPoint(p);   
            
            if(c == null)
            {
                // current point was outside of the HexPanel cells
                // so we cannot use these Points
                return null;
            }
            
            // We hit a Coord, so check if we also hit an Idon
            Idon idon = getIdonFromPoint(p);
            
            // If there is no Idon at Coord c, or the Idon at Coord c is
            // in the collection being dragged, the Coord is usable.
            
            if(idon == null || dropPoints.containsKey(idon))
            {
                dropCoords.put(i, c);
            }
            else
            {
                // Otherwise, add the Coord to the exclusion list
                // and leave now
                exclude.add(c);
                return null;
            }
        }
        
        System.out.println("got placement map");
        return dropCoords;
    }
       
    /**
     * Returns the Coord found by following a path from a specified
     * Coord on the HexPanel, or otherwise null.
     */ 
    public Coord getCoordFromPath(Coord start, ArrayList<Direction> path)
    {    
        if(path.size() == 0)
        {
            return start;
        }
        Coord c = start;
        
        for(Direction d : path)
        {
            c = d.directionToCoord(c, d);
            
            if(c == null)
            {
                return null;   
            }
        }
        return c;
    }
    
    /**
     * Scrollable implementation method. 
     */ 
    public Dimension getPreferredScrollableViewportSize()
    {
        return getPreferredSize();
    }
    
    /**
     * Scrollable implementation method. 
     */ 
    public int getScrollableBlockIncrement(Rectangle visibleRect, 
                                    int orientation, int direction)
    {   
        return SCROLL_UNIT_INCR * 2;//* BLOCK_MULT;
    }
    public boolean getScrollableTracksViewportHeight()
    {
        return false;
    }
    public boolean getScrollableTracksViewportWidth() 
    {
        return false;
    }
    
    public int getScrollableUnitIncrement(Rectangle visibleRect, 
                                          int orientation, int direction) 
    {
        return SCROLL_UNIT_INCR;
    }
    
   /**
    * Search for Idons with the given String.
    * 
    * @param query The idea to find in the Idons 
    * 
    * @param start The position to start in the Idon list.
    * 
    * @return The position in the sorted Idon list that matches the
    * query, or a negative int on fail.
    */ 
    public Coord searchForIdon(String query, Coord lastFound, boolean rev)
    {
        /*
         * Create a sorted list of Coordinates that contain Idons
         */ 
        TreeSet<Coord> sortedCoords = new TreeSet<Coord>(idonMap.keySet()); 
        Object[] coordArray = sortedCoords.toArray();
        if(!rev)
        {
            for(int i = 0; i < sortedCoords.size(); i++)
            {
                Coord c = (Coord)coordArray[i];
                if(idonMatchesPattern(query, c, lastFound))
                {
                    return c;
                }
            }
        }
        else if(rev)
        {
            for(int i = sortedCoords.size()-1; i >= 0; i--)
            {
                Coord c = (Coord)coordArray[i];
                if(idonMatchesPattern(query, c, lastFound))
                {
                    return c;
                }
            }
        }
        return null;
    } 
    
    /*
     * Utility method used by both forward and backward Idon searches.
     */ 
    private boolean idonMatchesPattern(String str, Coord c, Coord lastFound)
    {
        Idon idon = getIdonFromCoord(c);
        String idea = idon.getIdea();
        
        if(idea != null)
        {
            //System.out.println("comparing " + str.toLowerCase() 
              //                  + " with " + idea.toLowerCase());
            if(idea.toLowerCase().startsWith(str.toLowerCase()))
            {       
                if(lastFound == null)
                {
                   return true;
                } 
                else
                {
                    if(!c.equals(lastFound))
                    {
                        return true;
                    }
                }         
            }
        }
        return false;
    }
}

