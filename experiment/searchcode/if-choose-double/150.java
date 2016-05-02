/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shapes;

import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

/**
 *
 */
public class CoordsDisplay extends Pane{
    
    private boolean displayGrid = false;
    // private boolean isMetric = false;
    
    private int left;
    private int top;
    private int right;
    private int bottom;
    
    public CoordsDisplay(int left, int top, int right, int bottom, boolean displayGrid) {
        this.displayGrid = displayGrid;
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }
    
    public void init(){
        if(displayGrid) {
            double width = this.getWidth();
            double height = this.getHeight();
            double colWidth = 50;
            double rowHeight = 50;
            
            double mapWidth = right - left;
            double mapHeight = top - bottom;
            
            double rows = mapWidth/colWidth;
            double cols = mapHeight/rowHeight;
            
            double scale = width/mapWidth;
            
            Path grid = new Path();
            grid.setStrokeWidth(1);
            
            grid.setStroke(Color.BLACK);
            for(int i=0; i<rows; i++) {
                grid.getElements().add(new MoveTo(i*colWidth*scale, height));
                grid.getElements().add(new LineTo(i*colWidth*scale, 0));
            }
            
            for(int i=0; i<cols; i++) {
                grid.getElements().add(new MoveTo(0, i*rowHeight*scale));
                grid.getElements().add(new LineTo(width, i*rowHeight*scale));
            }
            
            this.getChildren().add(grid);
        }
    }
}

