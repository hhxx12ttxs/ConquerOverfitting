package perspectives.parallel_coordinates;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import perspectives.*;
import perspectives.two_d.Viewer2D;
import perspectives.util.Table;
import perspectives.util.TableData;

/**
 *
 * @author mershack
 */
public class ParallelCoordDrawer extends Viewer2D {

    private final int CANVAS_WIDTH = 580;
    private final int CANVAS_HEIGHT = 450;
    private final int OVAL_WIDTH = 6;
    private final int OVAL_HEIGHT = 6;
    private final int SMALL_OVAL_SIZE = 2;
    private final int MAX_RGB = 255;
    private final int NONE_SELECTED = -1;
    private int init_x;
    private int init_y;
    private int init_miny;
    private int init_maxy;
    private final int verticalFieldWidth = 4;
    private int separation;
    private final int minSeparation = 20;
    private int line_height;
    private SmallOvals topOvals[];
    private SmallOvals bottomOvals[];
    private SmallOvals dataOvals[][];
    private VerticalFields verticalFields[];
    private VerticalFieldDragged verticalFieldsDragged[];
    private Table tab;
    private Color vertLinesColor;
    private Color dataLinesColor;
    private Color dataLinesColors[];       //colors for the data lines
    private int alpha;                   //the alpha
    private int headerAngle = 0;   //the angle for the column header in case of rotation
    private String[][] data;        //the actual data
    private int rowCount;          //number of rows in the data
    private int colCount;         // the number of columns in the data
    private float[][] XY_coords;  //to hold the x, y coordinates of the data
    private float colMax[]; // maximum values for all the columns... for strings, it will be the count of distinct elements
    private float colMin[];  //minimum values for all the columns ... for strings, it will be 0;
    private final String DEFAULTSTRING = "missing";   //for missing strings
    private final int DEFAULTNUMERIC = 0;         //for missing nums
    private int selectedColumn;       //the id of a selected column
    private int pressedColumn;       //the id of a pressed column
    private int maxHeaderLength;     // the length of the longest column header
    boolean firstRender;  //flag to track the firstRender

    /**
     * Constructor and initialization
     */
    public ParallelCoordDrawer(String name, TableData tb) {
        super(name);
        tab = tb.getTable();
        init_x = 50;  //initial value for the coordinates
        init_y = 50;
        line_height = 300;
        init_miny = init_y - 20;   //Note the 20 is to allow the line to extend a little bit beyond the small horizontal line
        init_maxy = init_y + line_height + 20;
        colCount = tab.getColumnCount();
        rowCount = tab.getRowCount();
        data = new String[rowCount][colCount]; //initialize the data array to be of the size of the table.
        XY_coords = new float[rowCount][colCount];   //initialize the xy-coords array.
        colMax = new float[colCount];
        colMin = new float[colCount];
        verticalFields = new VerticalFields[colCount];
        verticalFieldsDragged = new VerticalFieldDragged[colCount];  // if a user drags mouse on some portion of the vertical field         
        topOvals = new SmallOvals[colCount];
        bottomOvals = new SmallOvals[colCount];
        dataOvals = new SmallOvals[rowCount][colCount];
        dataLinesColors = new Color[rowCount];  //to be used to save the color gradients for a specific column
        this.setTooltipDelay(100);
        vertLinesColor = new Color(0, 0, 0);  //set the initial color of the vertical lines to black
        dataLinesColor = new Color(0, 204, 204);
        alpha = 255;
        separation = 100;
        selectedColumn = 0;   //the first column will be selected.
        pressedColumn = -1;
        maxHeaderLength = separation;  //initialize the headerLength to the separation of the vertical lines.
        firstRender = true;
        /*initialize the verticalFieldsDragged Array*/
        for (int i = 0; i < colCount; i++) {
            verticalFieldsDragged[i] = new VerticalFieldDragged(0, 0, 0, 0);
        }
    }

    /**
     * render the parallel coordinates
     *
     * @param g the graphic2D object
     */
    @Override
    public void render(Graphics2D g) {
        //code to render the parallel coordinates
        if (firstRender) { //do this if it is the first render. We want this actions to be done only once
            this.setData();   //set the local data now but they will be in strings.
            this.setXYCoords();  //set the xy coordinates
            this.setMaxMin();
            this.setMaxHeaderLength(g);
            firstRender = false;   //set them to false

        }

        initializeCanvas(g);  //initialize the canvas
        renderDataLines(g);    // render the data lines

    }

    /**
     * ************************************************************************************
     * Initialize the canvas, and render all the vertical lines of the parallel
     * coordinates
     *
     * @param g - the graphic2D object
     * ************************************************************************************
     */
    public void initializeCanvas(Graphics2D g) {

        //set the color gradients using the selected column default is the first column i.e. 0)
        this.setLineColors();

        int x = init_x, y = init_y, miny = init_miny, maxy = init_maxy;

        // set background of the canvas to white
        g.setBackground(new Color(255, 255, 255));


        g.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        g.setStroke(new BasicStroke(0.3f));   //set the thickness of the stroke
        // render all vertical lines
        for (int i = 0; i < colCount; i++) {
            g.setColor(vertLinesColor); //set the color
            // render vertical lines line_height pixels high
            g.drawLine(x, miny, x, maxy);
            //draw small horizontal lines to mark the dimensions of the data on the vertical lines
            g.drawLine(x - 3, y, x + 3, y);
            g.drawLine(x - 3, y + line_height, x + 3, y + line_height);

            //render 2 vertical lines on the sides of the main vertical lines to form boxes around them.
            g.setColor(new Color(170, 150, 140, 100));
            g.drawLine(x - 2, miny, x - 2, maxy);
            g.drawLine(x + 2, miny, x + 2, maxy);

            /**************  Printing the Headings *************************/
            // set color used to print the headings
            g.setColor(new Color(0, 0, 0));
            
            
            AffineTransform orig = g.getTransform();//get the current transform

            double rotateRadian = 0, offset = 0, labelX, halfRotAngle = 0;

            rotateRadian = (headerAngle) * Math.PI / 180;  //radians = degrees * pi/180
            halfRotAngle = (headerAngle / 2.0) * Math.PI / 180;
            //compute the offset
            offset = 2 * Math.sin((halfRotAngle)) * (x-10);   // 2 * SOH to find the base of the isocelles triangle found 

            labelX = (x-10);  //the 5 is to move the text a 5pixels to the left when it is rotated
            
            offset = Math.abs(offset);
            //rotateRadian = angle * Math.PI/180  ;  //radians = degrees * pi/180
            g.rotate(-rotateRadian);
           
            double xtrans = 0, ytrans = 0;
            double xrad = 0;
            /*find the distance of the x after the rotation. : i.e. find the distance where the base of the isoceles triangle
             *  formed during the rotation forms a right-angle with the header of the rotated figure
             */
            int isoAngle = (180 - headerAngle) / 2;
            xrad = (isoAngle) * Math.PI / 180;
            ytrans = Math.sin(xrad) * offset;

            xtrans = Math.sqrt(Math.pow(offset, 2) - Math.pow(ytrans, 2));
            labelX = (x-10) - xtrans;  // (x-5) because we want the label to start a few pixels before the vertical line
            
            g.drawString(tab.getColumnName(i), (int) labelX, (int) ((miny - 10) + ytrans));   //write the header on top of the line. Note the 10 is because we wanted the text to be 30 pixels above the vertical line.
            //after writing the rotated text, continue with the normal rotation.
            g.setTransform(orig);  
            
            /* Set the field dimensions. these dimensions represent the box around the vertical lines*/
            verticalFields[i] = new VerticalFields(x - (verticalFieldWidth / 2), y - 20, line_height + 40, verticalFieldWidth);

            /*Filling of selection*/
            if (verticalFieldsDragged[i].getDragged()) { //if the column has been dragged, then fill the drag area
                verticalFieldsDragged[i].setX(verticalFields[i].getX());
                verticalFieldsDragged[i].setFill(true);
                verticalFieldsDragged[i].draw(g);
            } else if (i == selectedColumn) {  //if the whole column has been selected then fill the whole column
                verticalFields[i].setFill(true);
                verticalFields[i].draw(g);
            }

            x += separation;

        }
        //render top and bottom ovals at the endpoints of the vertical lines
        x = init_x;

        for (int i = 0; i < colCount; i++) {
            bottomOvals[i] = new SmallOvals(x - OVAL_WIDTH / 2, y + line_height + 20, OVAL_WIDTH, OVAL_HEIGHT);
            topOvals[i] = new SmallOvals(x - OVAL_WIDTH / 2, y - 20 - OVAL_HEIGHT, OVAL_WIDTH, OVAL_HEIGHT);

            topOvals[i].setLabel(tab.getColumnName(i));
            bottomOvals[i].setLabel(tab.getColumnName(i));
            topOvals[i].draw(g);
            bottomOvals[i].draw(g);

            x += separation;    //the horizontal separation of the vertical lines.
        }

    }

    /**
     * ***********************************************************
     * Render the dataLines that connect the vertical lines
     *
     * @param g - the graphics2D object
     * **********************************************************
     */
    public void renderDataLines(Graphics2D g) {
        float curpoint, nextpoint;
        int x, y;
        /*Draw the lines of the data  */
        double curmax, nextmax;
        g.setStroke(new BasicStroke(0.2f));

        for (int i = 0; i < rowCount; i++) {
            x = init_x;
            y = init_y;

            for (int j = 0; j < colCount - 1; j++) {

                dataLinesColor = dataLinesColors[i];
                g.setColor(new Color(dataLinesColor.getRed(), dataLinesColor.getGreen(), dataLinesColor.getBlue(), dataLinesColor.getAlpha()));

                // g.setColor(dataLinesColors[i],alpha);
                //set the point on the current line
                curmax = colMax[j] - colMin[j];
                //to
                curpoint = (float) ((line_height / curmax) * XY_coords[i][j]);
                //set the point on the next line
                nextmax = colMax[j + 1] - colMin[j + 1];
                //set
                nextpoint = (float) ((line_height / nextmax) * XY_coords[i][j + 1]);
                //  }

                g.draw(new Line2D.Float(x, Math.abs((line_height + y) - curpoint), x + separation, Math.abs((line_height + y) - nextpoint)));
                // g.drawLine(x, Math.abs((y + curpoint) - line_height), x + separation, Math.abs((y + nextpoint)-line_height));

                //Draw the small ovals for the data
                dataOvals[i][j] = new SmallOvals(x - SMALL_OVAL_SIZE / 2, (int) Math.abs((line_height + y) - curpoint - 1), SMALL_OVAL_SIZE, SMALL_OVAL_SIZE);
                dataOvals[i][j + 1] = new SmallOvals(x + separation - SMALL_OVAL_SIZE / 2, (int) Math.abs((line_height + y) - nextpoint - 1), SMALL_OVAL_SIZE, SMALL_OVAL_SIZE);

                //set the labels for the dataovals
                dataOvals[i][j].setLabel(data[i][j]);

                dataOvals[i][j + 1].setLabel(data[i][j + 1]);

                // distance between vertial lines
                x += separation;

            }

        }

    }

    /**
     * Set the max and minimum values for all the columns
     */
    public void setMaxMin() {
        //For String items, the lowest value will be 0 whiles their highest value will be the count of the items.

        String[] strvals;  //this will be used to save the distinct elements in the column

        for (int i = 0; i < colCount; i++) {

            strvals = DistinctColVal(i);  //get the distinct values in the column
            colMax[i] = strvals.length;
            colMin[i] = 0;

        }
    }

    /**
     * This function will set the xy-coordinates for the data items
     */
    public void setXYCoords() {

        String[] distinct;
        for (int i = 0; i < colCount; i++) {

            //find the distinct values and assign them the int values 0,1, 2, etc.
            distinct = DistinctColVal(i);
            //set the index of the distinct item in the array to the value of the distinct value
            for (int k = 0; k < distinct.length; k++) {
                for (int m = 0; m < rowCount; m++) {
                    if (data[m][i].equalsIgnoreCase(distinct[k])) {
                        XY_coords[m][i] = k + 1;   //the index of the XY coordinate the distinct values will be from 1 to length of distinct
                    }
                }
            }

        }
    }

    /**
     * set a local copy of the data. And convert the data items to strings since
     * they current have object data type.
     */
    public void setData() {  //this will get the data from the default Table Model and set it to the local data.

        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                data[i][j] = tab.getValueAt(i, j).toString();    //convert all the data to Strings

                //normalize data
                if (data[i][j].trim().isEmpty()) {
                    data[i][j] = DEFAULTSTRING;
                }

            }

        }
    }

    /**
     * generates the color gradients to be used for the lines based on a
     * selected column
     *
     * @param colid : the selected column id.
     */
    public void setLineColors() {

        int colid = selectedColumn;   //the selected column. the initial value for selected column is 0

        if (colid >= 0) {
            /*varying the color of the lines */
            for (int i = 0; i < rowCount; i++) {
                //find the percentage value of the coordinate to be used as offset
                float offset = (colMax[colid] - XY_coords[i][colid]) / colMax[colid];

                float red, green, blue;

                red = dataLinesColor.getRed();
                green = dataLinesColor.getGreen();
                blue = dataLinesColor.getBlue();

                /*The color gradient combination will range between red and blue. Green will remain constant 
                 * NB: 
                 */
                dataLinesColors[i] = new Color((int) Math.abs((red - (offset * MAX_RGB))),
                        (int) Math.abs(((green))),
                        (int) Math.abs((blue - (offset * MAX_RGB))),
                        alpha);
            }
        } else {  //check the dragged coordinates and draw those lines

            colid = pressedColumn;   //the selected column. the initial value for selected column is 0

            //if no column is selected, we will select the first column by default for the data line colouring

            if (colid < 0) { //do nothing

                return; //no need to do colors           

            }


            /*varying the color of the lines */
            for (int i = 0; i < rowCount; i++) {
                float r, g, b;

                r = dataLinesColor.getRed();
                g = dataLinesColor.getGreen();
                b = dataLinesColor.getBlue();

                float offset = (colMax[colid] - XY_coords[i][colid]) / colMax[colid];

                if (dataWithinDragged(i, colid) == true) {
                    //if ((curpoint >= dragY) && (curpoint <= (dragY + dragHeight))) {
                    dataLinesColors[i] = new Color((int) Math.abs((r - (offset * MAX_RGB))),
                            (int) Math.abs(((g))),
                            (int) Math.abs((b - (offset * MAX_RGB))),
                            255);    //note the alpha of this one will be opaque */

                } else {
                    dataLinesColors[i] = new Color((int) Math.abs((r - (offset * MAX_RGB))),
                            (int) Math.abs(((g))),
                            (int) Math.abs((b - (offset * MAX_RGB))),
                            5);  //note the alpha of this one will be close to transparent
                }
            }
        }
    }

    /**
     * print the XY_coords: for debugging purposes
     */
    public void printXYcoords() {
        System.out.println("The XY-coords are: ");
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                System.out.print(" " + XY_coords[i][j]);
            }
            System.out.println();
        }
    }

    /**
     * print the Maxs: for debugging purposes
     */
    public void printMaxs() {

        System.out.println("The Maximums are: ");
        for (int j = 0; j < colCount; j++) {
            System.out.print(" " + colMax[j]);
        }
    }

    /**
     * method to determine the unique items in the column.
     *
     * @param columnid - the specific column
     * @return a String array that contains the unique items sorted.
     */
    public String[] DistinctColVal(int columnid) {

        String[] distinctColVals;

        String columnList[] = new String[rowCount];

        for (int i = 0; i < rowCount; i++) {
            columnList[i] = data[i][columnid];
        }
        //convert the Strings to a Set array, which can subsequently have the unique items.
        Set<String> strset = new HashSet<String>(Arrays.asList(columnList));

        distinctColVals = strset.toArray(new String[0]);  //convert the set to an array

        //* Now sort the distinct elements */
        Arrays.sort(distinctColVals);      //sort the items in the array.

        return distinctColVals;
    }

    /**
     * Determine whether the mouse is on the top header ovals
     *
     * @param x the x coordinate of the mouse pointer
     * @param y the y coordinate of the mouse pointer
     * @return the respective header oval or null
     */
    public SmallOvals mouseOnTopOval(int x, int y) {

        //check if the top ovals have already been set
        for (int i = 0; i < colCount; i++) {

            if (topOvals[i] != null) {
                if ((x >= topOvals[i].getX()) && (x <= topOvals[i].getX() + topOvals[i].getW())
                        && (y >= topOvals[i].getY()) && (y <= (topOvals[i].getY() + topOvals[i].getH()))) {

                    return topOvals[i];
                }

            }
        }

        return null;

    }

    /**
     * determine the maxHeader Length
     *
     * @param g
     */
    public void setMaxHeaderLength(Graphics2D g) {

        //get the maximum
        //we will use the font metric to determine that


        FontMetrics fm = g.getFontMetrics(g.getFont()); // or another font
        int strw = 0;

        for (int i = 0; i < colCount; i++) {
            if (fm.stringWidth(tab.getColumnName(i)) > strw)
                strw = fm.stringWidth(tab.getColumnName(i));
        }

        maxHeaderLength = strw;   //the highest length for the header string

    }
    
    
   public void setHeaderAngle(int angle) {
        if (angle < 360 && angle > 0) {  //the angle has to be between 0 and 360
            headerAngle = angle;
        }

    }

    /**
     * Determine the index if mouse is on top oval
     *
     * @param x the x coordinate of the mouse
     * @param y the y coordinate of the mouse
     * @return the respective index or -1
     */
    public int mouseOnTopOvalIndex(int x, int y) {

        //check if the top ovals have already been set
        for (int i = 0; i < colCount; i++) {

            if (topOvals[i] != null) {
                if ((x >= topOvals[i].getX()) && (x <= topOvals[i].getX() + topOvals[i].getW())
                        && (y >= topOvals[i].getY()) && (y <= (topOvals[i].getY() + topOvals[i].getH()))) {

                    return i;
                }

            }
        }
        return -1;
    }

    /**
     * Determine the index if mouse is on bottom oval
     *
     * @param x the x coordinate of the mouse pointer
     * @param y the y coordinate of the mouse pointer
     * @return the respective index or -1
     */
    public int mouseOnBottomOvalIndex(int x, int y) {
        for (int i = 0; i < colCount; i++) {
            if (bottomOvals[i] != null) {
                if ((x >= bottomOvals[i].getX()) && (x <= bottomOvals[i].getX() + bottomOvals[i].getW())
                        && (y >= bottomOvals[i].getY()) && (y <= (bottomOvals[i].getY() + bottomOvals[i].getH()))) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * set the selected column
     *
     * @param sc the id of the selected column
     */
    public void setSelectedColumn(int sc) {
        selectedColumn = sc;
    }

    /**
     * return the selected column
     *
     * @return
     */
    public int getSelectedColumn() {
        return selectedColumn;
    }

    /**
     * Determine whether mouse is on the bottom oval
     *
     * @param x - the x coordinate of the mouse pointer
     * @param y - the y coordinate of the mouse pointer
     * @return the small oval object or null
     */
    public SmallOvals mouseOnBottomOval(int x, int y) {

        for (int i = 0; i < colCount; i++) {
            if (bottomOvals[i] != null) {
                if ((x >= bottomOvals[i].getX()) && (x <= bottomOvals[i].getX() + bottomOvals[i].getW())
                        && (y >= bottomOvals[i].getY()) && (y <= (bottomOvals[i].getY() + bottomOvals[i].getH()))) {
                    return bottomOvals[i];
                }
            }
        }
        return null;
    }

    /**
     * Determine whether mouse is on the data ovals
     *
     * @param x -the x coordinate of the mouse pointer
     * @param y - the y coordinate of the mouse pointer
     * @return the respective smalldovals object or null
     */
    public SmallOvals mouseonDataOvals(int x, int y) {
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                if (dataOvals[i][j] != null) {
                    if ((x >= dataOvals[i][j].getX()) && (x <= dataOvals[i][j].getX() + dataOvals[i][j].getW())
                            && (y >= dataOvals[i][j].getY()) && (y <= (dataOvals[i][j].getY() + dataOvals[i][j].getH()))) {
                        return dataOvals[i][j];
                    }
                }
            }
        }
        return null;
    }

    /**
     * determine whether data lies within the dragged area
     *
     * @param rowid - the rowid of the data
     * @param colid - the column of the data
     * @return true or false
     */
    public boolean dataWithinDragged(int rowid, int colid) {

        int mindragY, maxdragY;
        mindragY = verticalFieldsDragged[colid].getY();
        maxdragY = verticalFieldsDragged[colid].getY() + verticalFieldsDragged[colid].getH();

        if (mindragY <= dataOvals[rowid][colid].getY() && (maxdragY >= (dataOvals[rowid][colid].getY() + dataOvals[rowid][colid].getH()))) {
            return true;
        }

        return false;
    }

    /**
     * set the vertical lines color
     *
     * @param c the respective color object
     */
    public void setVerticalLinesColor(Color c) {
        vertLinesColor = c;
    }

    /**
     * set the separation of the vertical lines
     *
     * @param separation the size of the separation
     */
    public void setVerticalLinesSeparation(int separation) {

        if (separation < minSeparation) {  //ensure that separation is not less than the minimum
            this.separation = minSeparation;
        } else {
            this.separation = separation;
        }
    }

    /**
     * Set the height of the vertical lines
     *
     * @param height the respective height
     */
    public void setVerticalLinesHeight(int height) {
        line_height = height;
        init_maxy = init_y + line_height + 20;
    }

    /**
     * Set the beginning color for the dataLines.
     *
     * @param c - the respective color
     */
    public void setDataLinesColor(Color c) {
        dataLinesColor = c;
    }

    /**
     * Set the alpha for the datalines
     *
     * @param alpha - the respective alpha
     */
    public void setDataLinesAlpha(int alpha) {
        this.alpha = alpha;

    }

    /**
     * determine the index if mouse is on any of the vertical boxes
     *
     * @param x the x-coordinate of the mouse
     * @param y the y-coordinate of the mouse
     * @return the index of the vertical box or -1
     */
    public int mouseOnVerticalBoxIndex(int x, int y) {
        for (int i = 0; i < colCount; i++) {

            if (verticalFields[i] != null) {
                if ((x >= verticalFields[i].getX()) && (x <= verticalFields[i].getX() + verticalFields[i].getW())
                        && (y >= verticalFields[i].getY()) && (y <= (verticalFields[i].getY() + verticalFields[i].getH()))) {
                    return i;
                }
            }
        }

        return -1;
    }

    /**
     * set the tool tip if mouse moved on any of the header ovals or the data
     * ovals
     *
     * @param x x-coordinate of the mouse
     * @param y y-coordinate of the mouse
     * @return
     */
    @Override
    public boolean mousemoved(int x, int y) {

        this.setToolTipText("");

        SmallOvals headeroval;
        SmallOvals dataoval;

        if (((headeroval = mouseOnTopOval(x, y)) != null)) {  //check if mouse on top ovals

            this.setToolTipText(headeroval.getLabel());

            return true;

        } else if ((headeroval = mouseOnBottomOval(x, y)) != null) { //check if mouse on bottom ovals

            this.setToolTipText(headeroval.getLabel());

            return true;
        } else if ((dataoval = mouseonDataOvals(x, y)) != null) {
            this.setToolTipText(dataoval.getLabel());
            return true;
        }

        return false;
    }

    /**
     * if mouse is pressed on any of the header fields, select the whole column,
     * if it was pressed within a vertical box, mark it as the beginning of
     * dragging
     *
     * @param x x-coordinate of the mouse
     * @param y y-coordinate of the mouse
     * @param button
     * @return true or false
     */
    @Override
    public boolean mousepressed(int x, int y, int button) {
        int index;
        //check if mouse is on top header oval or bottom header oval
        if (((index = mouseOnTopOvalIndex(x, y)) >= 0) || ((index = mouseOnBottomOvalIndex(x, y)) >= 0)) {
            //select the whole column
            setSelectedColumn(index);
            //reset the drags for all the columns;
            resetAllColumnDrags();
            return true;
        } else if ((index = mouseOnVerticalBoxIndex(x, y)) >= 0) { //mouse on the verticalfield, set the x and y
            verticalFieldsDragged[index].setX(verticalFields[index].getX());
            verticalFieldsDragged[index].setDragBeginY(y);
            verticalFieldsDragged[index].setW(verticalFieldWidth);
            //set the current height to zero            
            verticalFieldsDragged[index].setH(0);
            //set the pressedColumn
            setPressedColumn(index);
            return true;
        }
        return false;
    }

    /**
     * reset the dragging to false for any dragged column
     */
    public void resetAllColumnDrags() {
        for (int i = 0; i < colCount; i++) {
            verticalFieldsDragged[i].setDragged(false);
        }
    }

    /**
     * reset the previous dragged column to false
     */
    public void resetPreviousColumnDrag() {
        for (int i = 0; i < colCount; i++) {

            if (i != pressedColumn) {
                verticalFieldsDragged[i].setDragged(false);
            }

        }
    }

    /**
     * set the column pressed
     *
     * @param colid the id of the pressed column
     */
    public void setPressedColumn(int colid) {
        pressedColumn = colid;
    }

    /**
     * Get the column which has beganDragging set to true.
     *
     * @return
     */
    public int getBeganDraggingIndex() {
        for (int i = 0; i < colCount; i++) {

            if (verticalFieldsDragged[i] != null) {
                if (verticalFieldsDragged[i].getBeganDragging() == true) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * if any column is pressed, then we will create a filled box for the
     * dragging the field box will start from the y starting of the beginning
     * point to the last y point the mouse is left
     *
     * @param currentX the current x of the drag
     * @param currentY the current y of the drag
     * @param oldX
     * @param oldY
     * @return
     */
    @Override
    public boolean mousedragged(int currentX, int currentY, int oldX, int oldY) {
        //checked if the beginning of the dragging is within a specific vertical field.
        //if it is, then we will create a filled box, from the y starting of the beginning point to the last y point the mouse is left

        if (pressedColumn >= 0) {  // check if pressing has began on a column

            setSelectedColumn(NONE_SELECTED);  //unset the selected column

            //reset the previous drag
            verticalFieldsDragged[pressedColumn].setBeganDragging(true);
            resetPreviousColumnDrag();

            /* make sure the currentY does not go beyond the boundary of the line */
            if (currentY > (init_y + line_height)) {  //if it is greater than the highest point for data
                currentY = init_y + line_height;
            } else if (currentY < init_y) {  //if it is less than the lowest point for data
                currentY = init_y;
            }

            //set the height of the rectangle.
            //compute the current height of the column. Note it will be currentY and the beginning of the dragging of the Y.
            int height = (currentY - verticalFieldsDragged[pressedColumn].getDragBeginY());

            if (height < 0) { //if drawing up
                height = Math.abs(height); //find the positive value of the height                    
                int newY = verticalFieldsDragged[pressedColumn].getDragBeginY() - height;
                //set the new Y
                verticalFieldsDragged[pressedColumn].setY(newY);
                //System.out.println("The height is " + height);
            }

            verticalFieldsDragged[pressedColumn].setH(height);

            verticalFieldsDragged[pressedColumn].setDragged(true);
            //remove the tooltip when dragging is on
            this.setToolTipText("");
            return true;
        }
        return false;
    }

    /**
     * if mouse was pressed on a column and began_dragging of the column is true
     * then set the last height of the dragging
     *
     * @param x x-coordinate of the mouse pointer
     * @param y y-coordinate of the mouse pointer
     * @param button
     * @return true or false
     */
    @Override
    public boolean mousereleased(int x, int y, int button) {
        //int index;
        //check if mouse was pressed on a column and began_dragging of the column is true
        if (pressedColumn >= 0) {
            if (verticalFieldsDragged[pressedColumn].getBeganDragging() == true) {
                /* make sure the y does not go beyond the boundary of the line */
                if (y > (init_y + line_height)) {  //if it is greater than the highest point for data
                    y = init_y + line_height;
                } else if (y < init_y) {  //if it is less than the lowest point for data
                    y = init_y;
                }

                int height = (y - verticalFieldsDragged[pressedColumn].getDragBeginY());

                if (height < 0) {
                    height = Math.abs(height); //find the positive value of the height

                    int newY = verticalFieldsDragged[pressedColumn].getDragBeginY() - height;
                    //set the new Y
                    verticalFieldsDragged[pressedColumn].setY(newY);
                }

                verticalFieldsDragged[pressedColumn].setH(height);

                verticalFieldsDragged[pressedColumn].setBeganDragging(false);

                verticalFieldsDragged[pressedColumn].setDragged(true);
                setPressedColumn(NONE_SELECTED);  //reset the pressed column

                return true;

            }
        }

        return false;
    }

    public void simulate() {
    }
}

