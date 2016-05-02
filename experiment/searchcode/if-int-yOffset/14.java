/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package drawMethods;

import diagram.UMLInterface;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.util.ArrayList;

/**
 *
 * @author Ryan Paton rjp148
 */
public class InheritanceDrawing {
    
    /**
     * Enumeration for defining a directing.
     * Used for drawing arrows
     */
    public static enum Direction{
        NORTH,
        SOUTH,
        EAST,
        WEST
    }
    
    /**
     * Calculates and returns which direction that the lollipop should be
     * displayed.
     * @param lolPos    the position of the lollipop
     * @param clTL      the top left corner of the class
     * @param clBR      the bottom right corner of the class
     * @return          the Direction to display the lollipop
     */
    public static Direction getLollipopDrawDir(Point lolPos, Point clTL,
            Point clBR){
        
        Direction lollipopDir;
        
        if (lolPos.y <= clTL.y){
            lollipopDir = Direction.NORTH;
        }
        else if (lolPos.y >= clBR.y){
            lollipopDir = Direction.SOUTH;
        }
        else if (lolPos.x >= clBR.x){
            lollipopDir = Direction.EAST;
        }
        else{lollipopDir = Direction.WEST;}
        
        return lollipopDir;
    }
    
    
    /**
     * Same as getLollipopDrawDir but with a rectangle instead of two points
     * @param lolPos    the position of the lollipop
     * @param classBounds      The class boundry.
     * @return          the Direction to display the lollipop
     */
    public static Direction getLollipopDrawDir(Point lolPos,
            Rectangle classBounds){
        
        Point topLeft = new Point(classBounds.x, classBounds.y);
        
        Point bottomRight = new Point(classBounds.x + classBounds.width,
                classBounds.y + classBounds.height);
        
        return getLollipopDrawDir(lolPos, topLeft, bottomRight);
    }
    
    
    public static Point createOffsetPosition(Point srcPos, Rectangle bounds,
            Direction srcDir){
        Point result = new Point(srcPos);
        
        switch(srcDir){
            case NORTH:
                result.x -= 8 + bounds.width / 2;
                result.y -= 36 + bounds.height;
                break;
                
            case SOUTH:
                result.x -= 8 + bounds.width / 2;
                result.y += 36;
                break;
                
            case EAST:
                result.x += 36;
                result.y -= 8;
                break;
                
            default:
                result.x -= 36 + bounds.width;
                result.y -= 8;
        }
        
        return result;
    }
    
    public static void drawLollypop(Graphics g, UMLInterface itf){
        
        Point lollipopPos = itf.getPosition();
        Point classPosTopLeft = itf.getClassAttachedTo().getPosition();
        Point classPosBottomRight = new Point(classPosTopLeft.x +
                itf.getClassAttachedTo().getWidth(), classPosTopLeft.y +
                itf.getClassAttachedTo().getHeight());
        
        //Find the direction which the lollipop should be displayed.
        Direction lollipopDir = getLollipopDrawDir(lollipopPos, classPosTopLeft,
                classPosBottomRight);
        
        //Find the offsets for the lollipop
        int lineXOff, lineYOff;
        int circXOff, circYOff;
        switch(lollipopDir){
            case NORTH:
                lineXOff = 0;
                lineYOff = -20;
                circXOff = -8;
                circYOff = -36;
                break;
                
            case SOUTH:
                lineXOff = 0;
                lineYOff = 20;
                circXOff = -8;
                circYOff = 20;
                break;
                
            case EAST:
                lineXOff = 20;
                lineYOff = 0;
                circXOff = 20;
                circYOff = -8;
                break;
                
            default:
                lineXOff = -20;
                lineYOff = 0;
                circXOff = -36;
                circYOff = -8;
        }
        
        //Draw the lollipop
        g.setColor(Color.black);
        
        g.drawLine(lollipopPos.x, lollipopPos.y, lollipopPos.x + lineXOff,
                lollipopPos.y + lineYOff);
        g.drawOval(lollipopPos.x + circXOff, lollipopPos.y + circYOff, 16, 16);
        
    }
    
    public static ArrayList<Line2D.Double> drawConnectingLine(Graphics g, Point startPos,
            Point endPos, int yOffset){
    	ArrayList<Line2D.Double> lines = new ArrayList<Line2D.Double>();
        
        // Draw a line between start and end.
    	
    	//line 1
        g.drawLine(startPos.x, startPos.y, startPos.x,
                endPos.y + yOffset);
        lines.add(returnLine(startPos.x, startPos.y, startPos.x,
                endPos.y + yOffset));
        
        //line 2
        g.drawLine(startPos.x, endPos.y + yOffset, endPos.x,
                endPos.y + yOffset);
        lines.add(returnLine(startPos.x, endPos.y + yOffset, endPos.x,
                endPos.y + yOffset));
        
        //line 3
        g.drawLine(endPos.x, endPos.y + yOffset, endPos.x,
                endPos.y);
        lines.add(returnLine(endPos.x, endPos.y + yOffset, endPos.x,
                endPos.y));
        
        return lines;
    }
    
    public static ArrayList<Line2D.Double> drawInheritanceLine(Graphics g, Point childPoint,
            Point parentPoint, Dimension childSize, Dimension parentSize){
        
        Point lineStart = new Point(childPoint);
        Point lineEnd = new Point(parentPoint);
        Point arrowPosition = new Point(parentPoint);
        Direction dir;
                    
        // Calculate offset for line drawing.
        // the number 10 is in here to represent the hight of the
        // arrow, this needs to be changed to a constant.
        // If child is below parent.
        if (childPoint.y > parentPoint.y){
            dir = Direction.NORTH;
            arrowPosition.setLocation(parentPoint.x +
                    (parentSize.getWidth() / 2), parentPoint.y +
                    parentSize.getHeight());
            lineEnd.setLocation(arrowPosition.x, arrowPosition.y + 10);
        }
        // Otherwise child must be above
        else{
            dir = Direction.SOUTH;
            arrowPosition.setLocation(parentPoint.x +
                    (parentSize.getWidth() / 2), parentPoint.y);
            lineEnd.setLocation(arrowPosition.x, arrowPosition.y - 10);
        }
        
        // Set the starting point for the line to be drawn.
        lineStart.setLocation(childPoint.x + childSize.getWidth() /
                2, childPoint.y + childSize.getHeight() / 2);
        
        // Calculate a y offset.
        int yOffset;
        if (dir == Direction.NORTH){
            yOffset = 30;
        }
        else{
            yOffset = -30;
        }
        //TODO I swapped these around
        // draw the arrow head.
        drawArrowHead(g, arrowPosition, dir);
        // Draw a line between parent and child.
        return drawConnectingLine(g, lineStart, lineEnd, yOffset);
    }
    
    /**
     * Draws an arrow on the specified graphics at the specified position in
     * the specified direction.
     * @param g         The graphics instance to draw to.
     * @param position  The position of the arrow point.
     * @param dir       The direction of the arrow.
     */
    private static void drawArrowHead(Graphics g, Point position, Direction dir){
        
        // variables to get the arrow points.
        int xa = 0, xb = 0, ya = 0, yb = 0;
        
        // Calculate the positions.
        switch(dir)
        {
            case NORTH:
                xa = position.x - 8;
                xb = position.x + 8;
                ya = position.y + 10;
                yb = ya;
                break;
            case SOUTH:
                xa = position.x - 8;
                xb = position.x + 8;
                ya = position.y - 10;
                yb = ya;
                break;
            case EAST:
                xa = position.x - 10;
                xb = xa;
                ya = position.y - 8;
                yb = position.y + 8;
                break;
            case WEST:
                xa = position.x + 10;
                xb = xa;
                ya = position.y - 8;
                yb = position.y + 8;
        }
        
        // draw the arrow
        g.drawLine(position.x, position.y, xa, ya);
        g.drawLine(xa, ya, xb, yb);
        g.drawLine(xb, yb, position.x, position.y);
    }
    
    /**
     * Draws an arrow at a specific point, calculating which angle to draw it at
     * from the point of the arrow tail.
     * @param g         Graphics to draw to
     * @param arrowPos  The position of the point of the arrow
     * @param lineOrig  The position of the tail of the arrow
     */
    public static void drawRotatedArrowHead(Graphics g, Point arrowPos,
            Point lineOrig){
        
        Point line1End = new Point();
        Point line2End = new Point();
        int lineLen = 12;
        
        double arrowRads = Math.atan2(arrowPos.x - lineOrig.x, arrowPos.y -
                lineOrig.y);
        double line1Rads = arrowRads + 4.7 * Math.PI / 6;
        double line2Rads = arrowRads - 4.7 * Math.PI / 6;
        
        line1End.x = (int) (arrowPos.x + lineLen * Math.sin(line1Rads));
        line1End.y = (int) (arrowPos.y + lineLen * Math.cos(line1Rads));
        line2End.x = (int) (arrowPos.x + lineLen * Math.sin(line2Rads));
        line2End.y = (int) (arrowPos.y + lineLen * Math.cos(line2Rads));
        
        g.drawLine(arrowPos.x, arrowPos.y, line1End.x, line1End.y);
        g.drawLine(arrowPos.x, arrowPos.y, line2End.x, line2End.y);
    }
    
    public static Line2D.Double returnLine(int x, int y,int x1, int y1){
    	Point px = new Point(x,y);
    	Point py = new Point(x1,y1);
    	Line2D.Double line = new Line2D.Double(px,py);
    	return line;
    }
}



