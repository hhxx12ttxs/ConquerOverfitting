package perspectives.heatmap;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.util.ArrayList;

import perspectives.two_d.Viewer2D;
import perspectives.util.DistancedPoints;
import perspectives.util.Table;
import perspectives.util.TableData;

/**
 *
 * @author mershack
 */
public class HeatMapDrawer extends Viewer2D {

    private final int CANVAS_WIDTH = 600;
    private final int CANVAS_HEIGHT = 600;
    // private final int SINGLE_LINK = 1;
    //private final int COMP_LINK = 2;
    // private final int AVG_LINK = 3;    
    private boolean singleLinkType;
    private boolean compLinkType;
    private boolean avgLinkType;
    private Table tab;
    private int selectedColumn;
    private int colCount;
    private int rowCount;
    private float[][] XY_coords;     //to hold the x, y coordinates of the data
    private String[][] data;        //the actual data
    private boolean firstRender;
    private final String DEFAULTSTRING = "missing";   //for missing strings
    private final int DEFAULTNUMERIC = 0;         //for missing nums
    private float colMax[]; // maximum values for all the columns... for strings, it will be the count of distinct elements
    private float colMin[];  //minimum values for all the columns ... for strings, it will be 0;
    private int boxWidth;
    private int boxHeight;
    private Rectangles dataRects[][];
    private Color HighColor;
    private Color LowColor;
    private int headerAngle;   //the angle for the column header in case of rotation
    private int maxColHeadLength;
    private int maxRowHeadLength;
    private final int INIT_X = 30;
    private final int INIT_Y = 30;
    private ClusterNode clusterNodes[];
    private int clusterIds[];
    private float distMat[][];
    private int clusterCnt;
    private int clusterPos;
    private int dataPositions[];
    private Color selectedColor;
    private int selectedRow;
    private int selectedCol;
    private Color dataColors[][];
    private ArrayList<Color> scaleColors;

    public HeatMapDrawer(String name, TableData tb) {
        super(name);
        tab = tb.getTable();
        selectedColumn = 0;
        firstRender = true;
        colCount = tab.getColumnCount();
        rowCount = tab.getRowCount();
        colMax = new float[colCount];
        colMin = new float[colCount];
        XY_coords = new float[rowCount][colCount];   //initialize the xy-coords array.
        data = new String[rowCount][colCount];
        dataRects = new Rectangles[rowCount][colCount];

        boxWidth = CANVAS_WIDTH / colCount;
        //boxHeight = CANVAS_HEIGHT / rowCount;
        boxHeight = 12;
        HighColor = new Color(255, 255, 0);   //Initial High Color will be green

        clusterNodes = new ClusterNode[2 * rowCount - 1];  //the cluster nodes will be twice the rowcount. i.e. N initial clusters for the individual items, followed by N-1 clusters making one cluster.
        clusterIds = new int[rowCount];
        //distMatrix = new double [rowCount][];
        clusterCnt = 0;
        clusterPos = 0;
        headerAngle = 20;
        dataPositions = new int[rowCount];
        //make the singleLinkType the default for the clustering 
        singleLinkType = true;
        compLinkType = false;
        avgLinkType = false;

        scaleColors = new ArrayList<Color>();
        scaleColors.add(Color.red);
        scaleColors.add(Color.blue);
        selectedRow = -1;
        selectedCol = -1;
        selectedColor = new Color(255, 0, 0); //default selection color is red
        this.setTooltipDelay(100);  //tooltip delay set

        dataColors = new Color[rowCount][colCount];
        // ArrayList<Color> ac = new ArrayList<Color>();

    }

    public void computeClusters() {
        //(DistancedPoints)tab; 
        clusterPos = 0;  //initialize the clusterPos 
        clusterCnt = rowCount; // clusterCnt
        float distance;
        int id1 = 0, id2 = 0, cnt = 0;
        while (cnt < rowCount - 1) { //the last cluster will be the root
            cnt++;

            distance = Float.MAX_VALUE;
            /* find the column and row of the smallest distance */
            for (int r = 1; r < distMat.length; r++) {
                for (int c = 0; c < r; c++) {

                    float temp = distMat[r][c];
                    if (temp < distance) {
                        distance = temp;
                        id1 = r;
                        id2 = c;
                    }
                }
            }
            //create a cluster with the distance, and the ids found to contain the smallest dist.
            int cid1 = clusterIds[id1];
            int cid2 = clusterIds[id2];
            int level = Math.max(clusterNodes[cid1].getLevel(), clusterNodes[cid2].getLevel()) + 1; //the maximum level among the 2 plus 1.
            int size = clusterNodes[cid1].getSize() + clusterNodes[cid2].getSize();
            int id = clusterCnt;

            /*compute the position for the clusters all the level 0 clusters will be given positions that reflect when they were found */

            if (clusterNodes[cid1].getLevel() == 0) {

                clusterNodes[cid1].setPos(clusterPos);   //the position will be in the midpoint of the box of that index

                dataPositions[clusterPos] = clusterNodes[cid1].getId(); //fix the data positions for that id                        
                clusterPos++;
            }

            if (clusterNodes[cid2].getLevel() == 0) {
                clusterNodes[cid2].setPos(clusterPos);    //the position will be in the midpoint of the box of that index

                dataPositions[clusterPos] = clusterNodes[cid2].getId();  //fix the datapositions for that id                        
                clusterPos++;

            }

            float newPos = (clusterNodes[cid1].getPos() + clusterNodes[cid2].getPos()) / 2;  //i.e. the midpoints of the two branches


            clusterNodes[clusterCnt] = new ClusterNode(id, level, cid1, cid2, distance, size, newPos);


            distMat = computeDistMat(id1, id2, cnt);

            //printDistMat();

            clusterCnt++;

        }

    }

    public void computeInitClusters() {

        /**
         * Initialize the 2D array for the distance matrice, create a
         * clusterNode object (leafs) for them
         */
        for (int i = 0; i < rowCount; i++) {
            /*create a leaf cluster node */
            clusterNodes[i] = new ClusterNode(i, 0, i, i, 0, 1, 0);
            /*initialize the clusterIds involved also */
            clusterIds[i] = i;
        }
        clusterCnt = rowCount;


    }

    public float[][] computeDistMat(int r, int c, int cnt) {
        int size = distMat.length - 1;
        int minIndex, maxIndex;
        float[][] distMat2 = new float[size][];

        /*recalculate all the distances for the minIndex (which will be used for the combined cluster)
         *  and move the last item in the table to maxIndex if it is not the last index
         *  the values in all the previous indexes will remain the same. */

        minIndex = Math.min(r, c);
        maxIndex = Math.max(r, c);

        for (int i = 1; i < size; i++) {
            distMat2[i] = new float[i];

            if (i == minIndex) {  //i.e. the row of the minIndex
                for (int j = 0; j < minIndex; j++) {
                    //find the distance that represents the linkage  type.
                    //For this example, I'm using singleLinkage
                    if (j != maxIndex) {  //we assuming the column of the maxIndex will be erased

                        if (singleLinkType) {  //the min distance between the two
                            distMat2[i][j] = Math.min(distMat[minIndex][j], distMat[maxIndex][j]);
                        } else if (compLinkType) { //the maximum distance between the two
                            distMat2[i][j] = Math.max(distMat[minIndex][j], distMat[maxIndex][j]);
                        } else if (avgLinkType) {
                            //to be completed later
                        } else {  //the default should be the singleLinkType
                            distMat2[i][j] = Math.min(distMat[minIndex][j], distMat[maxIndex][j]);
                        }
                    }

                }
            } else if (i != maxIndex) {

                for (int j = 0; j < i; j++) {

                    if (j == minIndex && i > minIndex) {//the column of the minIndex
                        //  System.out.println("min " + minIndex + " i " + i + " j " + j + "  max " + maxIndex);

                        if (maxIndex > i) {
                            if (singleLinkType) {
                                distMat2[i][j] = Math.min(distMat[i][minIndex], distMat[maxIndex][i]);
                            } else if (compLinkType) {
                                distMat2[i][j] = Math.max(distMat[i][minIndex], distMat[maxIndex][i]);
                            } else if (avgLinkType) {
                                //to be completed later
                            } else {//default is the same as the singleLinkType
                                distMat2[i][j] = Math.min(distMat[i][minIndex], distMat[maxIndex][i]);
                            }


                        } else {
                            if (singleLinkType) {
                                distMat2[i][j] = Math.min(distMat[i][minIndex], distMat[i][maxIndex]);
                            } else if (compLinkType) {
                                distMat2[i][j] = Math.max(distMat[i][minIndex], distMat[i][maxIndex]);
                            } else if (avgLinkType) {
                                //to be completed later
                            } else {//default is the singleLinkType
                                distMat2[i][j] = Math.min(distMat[i][minIndex], distMat[i][maxIndex]);
                            }

                        }
                    } else {
                        distMat2[i][j] = distMat[i][j];
                    }

                }
            }

        }

        /*Move the last item in distMat to distMat2..we assumming we will delete the row and column of maxIndex so put it there*/
        if (maxIndex < size) {  //if maxIndex is not already the last item in the distMat array

            for (int j = 0; j < maxIndex; j++) {//the row
                if (j == minIndex) {
                    distMat2[maxIndex][j] = Math.min(distMat[size][minIndex], distMat[size][maxIndex]);
                } else {
                    distMat2[maxIndex][j] = distMat[size][j];
                }
            }

            for (int j = maxIndex + 1; j < size; j++) {//fix the column, (check the reverse of the indexes) it will be the same.

                if (j == minIndex) {
                    distMat2[j][maxIndex] = Math.min(distMat[size][minIndex], distMat[size][maxIndex]);
                } else {
                    distMat2[j][maxIndex] = distMat[size][j];
                }
            }

            clusterIds[maxIndex] = clusterIds[size];

        }

        clusterIds[minIndex] = clusterCnt;

        return distMat2;
    }

    public void printClusterIds() {
        for (int i = 0; i < clusterIds.length; i++) {
            System.out.print("   " + clusterIds[i]);
        }
    }

    public void printClusterNodes() {
        System.out.println("\n......................................................");
        for (int i = 0; i < clusterNodes.length; i++) {
            System.out.println("cluster Id: " + clusterNodes[i].getId() + "\t Level: " + clusterNodes[i].getLevel() + "\t Size: " + clusterNodes[i].getSize()
                    + "\t LeftId: " + clusterNodes[i].getLeftId() + "\tRightId: " + clusterNodes[i].getRightId() + "\t Distance: " + clusterNodes[i].getDistance()
                    + "\tPos: " + clusterNodes[i].getPos());
        }
    }

    public void computeInitDistMat() {
        distMat = new float[rowCount][];
        /*initialize the dist-matrix */

        for (int i = 1; i < rowCount; i++) {
            distMat[i] = new float[i];

            for (int j = 0; j < i; j++) {
                distMat[i][j] = ((DistancedPoints) tab).getDistance(i, j);
            }
        }
    }

    public void setSingleLinkType(boolean slt) {
        singleLinkType = slt;
    }

    public void setCompLinkType(boolean clt) {
        compLinkType = clt;
    }

    public void setAvgLinkType(boolean alt) {
        avgLinkType = alt;
    }

    /*Print the distMatrix */
    public void printDistMat() {
        //  if(distMat.length > 1)
        for (int i = 1; i < distMat.length; i++) {
            for (int j = 0; j < i; j++) {
                System.out.print("  " + distMat[i][j]);
            }
            System.out.println();
        }

    }

    public void drawClusters(Graphics2D g) {
        //recompute clusterPositions
        computeClusterPositions();
        int x = INIT_X;
        x = maxRowHeadLength + 3;
        // int y = INIT_Y;  //reset the x and y coordiantes  

        int y = INIT_Y + (boxHeight * rowCount) + 100;
        int indx;

        g.setStroke(new BasicStroke(0.2f));   //set the thickness of the stroke
        /**
         * Draw Rectangles for each of the data record *
         */
        for (int i = 0; i < rowCount; i++) {

            indx = dataPositions[i];

            /* Render the row label*/
            g.setColor(new Color(0, 0, 0));
            x = maxRowHeadLength + 3 - getRowLabelLength(g, indx);

            g.drawString(tab.getRowName(indx), x, y + (boxHeight));

            x = maxRowHeadLength + 3;  //make sure the longest word can be written before the actual heatmap       

            // System.out.println("x is " + x);
            for (int j = 0; j < colCount; j++) {
                //fill the rectangle with the respective color;

                g.setColor(dataColors[indx][j]);
                g.fillRect(x, y, boxWidth, boxHeight);
                //draw the rectangle with black border line
                //g.setColor(new Color(0, 0, 0));
                //g.drawRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
                g.drawRect(x, y, boxWidth, boxHeight);

                //set the coordinates of the rectangle to be used later.
                dataRects[i][j] = new Rectangles(x, y, boxWidth, boxHeight, tab.getValueAt(indx, j).toString(), indx, j);

                x = x + boxWidth;
            }

            y = y + boxHeight;
        }

        /*Write the Column headers  */
        x = maxRowHeadLength + 3;
        y = INIT_Y;
        int newYoffset = (boxHeight * rowCount) + 100;
        g.setColor(new Color(0, 0, 0));
        for (int i = 0; i < colCount; i++) {
            AffineTransform orig = g.getTransform();  //get the current transform
            // int angle= 0;
            double rotateRadian = 0, offset = 0, labelX, halfRotAngle = 0;

            // if ((maxHeaderLength + 5) > separation) {
            rotateRadian = (headerAngle) * Math.PI / 180;  //radians = degrees * pi/180
            halfRotAngle = (headerAngle / 2.0) * Math.PI / 180;
            //compute the offset
            offset = 2 * Math.sin((halfRotAngle)) * (x);   // 2 * SOH to find the base of the isocelles triangle found 

            labelX = x;  //the 15 is to move the text a 15pixels to the left when it is rotated


            offset = Math.abs(offset);
            //rotateRadian = angle * Math.PI/180  ;  //radians = degrees * pi/180
            g.rotate(-rotateRadian);
            //g.translate(INIT_X,INIT_Y);

            double xtrans = 0, ytrans = 0;
            double xrad = 0;
            /*find the distance of the x after the rotation. : i.e. find the distance where the base of the isoceles triangl
             *  formed during the rotation forms a right-angle with the header of the rotated figure
             */
            int isoAngle = (180 - headerAngle) / 2;
            xrad = (isoAngle) * Math.PI / 180;
            ytrans = Math.sin(xrad) * offset;

            xtrans = Math.sqrt(Math.pow(offset, 2) - Math.pow(ytrans, 2));
            labelX = x - xtrans;
            //y = INIT_Y - maxColHeadLength;
            // g.drawString(tab.getColumnName(i), (int) labelX, (int) ((y - 10) + ytrans));   //write the header on top of the line. Note the 30 is because we wanted the text to be 30 pixels above the vertical line.

            //after writing the rotated text, continue with the normal rotation.
            g.setTransform(orig);

            x = x + boxWidth;

        }


        /**
         * Draw the lines of the clusters now
         */
        y = INIT_Y + (boxHeight * rowCount) + 100;
        float lpos;
        float rpos;
        float cdist;
        for (int i = rowCount; i < clusterNodes.length; i++) {
            cdist = clusterNodes[i].getDistance();

            lpos = clusterNodes[clusterNodes[i].getLeftId()].getCoord();
            rpos = clusterNodes[clusterNodes[i].getRightId()].getCoord();

            if (clusterNodes[i].getLevel() == 1) {  //draw when the level is 1
                g.draw(new Line2D.Float(x, y + lpos, x + cdist, y + lpos));  //draw the first leg of the cluster

                g.draw(new Line2D.Float(x, y + rpos, x + cdist, y + rpos)); //draw the second leg of the cluster

                //join the two legs of the cluster
                g.draw(new Line2D.Float(x + cdist, y + lpos, x + cdist, y + rpos));

            }

        }

    }

    public void computeClusterPositions() {
        float pos, coord;
        /* Note: the first N items in clusterNodes are of level 0, the other levels follows after that.
         * So compute the positions of the level 0 items, the other levels will be relative to the Level 0 items
         * The index locations of the leaves of every index of the array, will be less than that particular index
         *   
         */

        for (int i = 0; i < clusterNodes.length; i++) {  //to ensure the clusters work with the dynamic resizing of the boxHeights

            if (clusterNodes[i].getLevel() == 0) {
                pos = clusterNodes[i].getPos();
                coord = (pos * boxHeight) + boxHeight / 2;
                clusterNodes[i].setCoord(coord);
            } else {
                coord = clusterNodes[clusterNodes[i].getLeftId()].getCoord()
                        + clusterNodes[clusterNodes[i].getRightId()].getCoord();

                clusterNodes[i].setPos(coord);
            }
        }


    }

    public void setHeaderAngle(int angle) {

        System.out.println("Header Angle is " + angle);
        if (angle < 360 && angle > 0) {  //the angle has to be between 0 and 360
            headerAngle = angle;
        }

    }

    public void computeDataColors() {
        //get the sorted data of a column
        //based on the size of Scale colors, divide the data values according to the colors
        String[] distinct;

        Color colDataColors[] = new Color[rowCount];
        int rangeSize;

        int colorIndex;
        Color color1, color2;

        //rangeSize = rangeSize+1;
        colorIndex = 0;
        int r, grn, b, rem, rdf, grndf, bdf;

        for (int i = 0; i < colCount; i++) {
            //find the distinct values and assign them the int values 0,1, 2, etc.
            distinct = DistinctColVal(i);

            rangeSize = distinct.length / (scaleColors.size() - 1);

            if (rangeSize == 0) {
                rangeSize = 1;
            }

            color1 = scaleColors.get(0);
            color2 = scaleColors.get(1);

            colDataColors[0] = color1;
            colorIndex = 1;

            for (int j = 1; j < distinct.length; j++) {

                rem = (j + 1) % (rangeSize);

                if (rem == 0) { //the second color becomes color 1 and increase the colorIndex for the next color
                    color1 = color2;
                    colDataColors[j] = color1;
                    //colorIndex++;

                    if (colorIndex == scaleColors.size() - 2) { //find the last range, the last range may not be equal to the others
                        rangeSize = distinct.length - (colorIndex) * (rangeSize);
                    }
                    if (colorIndex < scaleColors.size() - 1) {
                        colorIndex++;
                        color2 = scaleColors.get(colorIndex);
                    }

                } else {
                    rdf = ((color2.getRed() - color1.getRed()) / (rangeSize));
                    grndf = ((color2.getGreen() - color1.getGreen()) / (rangeSize));
                    bdf = ((color2.getBlue() - color1.getBlue()) / (rangeSize));

                    r = (color1.getRed() + rdf * rem);
                    grn = (color1.getGreen() + grndf * rem);
                    b = (color1.getBlue() + bdf * rem);

                    colDataColors[j] = new Color(r, grn, b);
                }
            }


            //set the index of the distinct item in the array to the value of the distinct value
            for (int k = 0; k < distinct.length; k++) {

                for (int m = 0; m < rowCount; m++) {
                    if (data[m][i].equalsIgnoreCase(distinct[k])) {
                        XY_coords[m][i] = k + 1;   //the index of the XY coordinate the distinct values will be from 1 to length of distinct
                        dataColors[m][i] = colDataColors[k];
                    }
                }

            }

        }


    }

    public void render(Graphics2D g) {

        if (firstRender) { //do this if it is the first render. We want this actions to be done only once
            this.setData();   //set the local data now but they will be in strings.
            this.setXYCoords();  //set the xy coordinates
            this.setMaxMin();
            this.setMaxColHeaderLength(g);
            this.setMaxRowHeaderLength(g);
            firstRender = false;   //set them to false
            computeInitDistMat();
            computeInitClusters();
            computeClusters();
            printDistMat();
            computeDataColors();
            // printClusterIds();
            //printClusterNodes();
            // drawClusters(g);
        }



        renderHeatMap(g);
        // drawClusters(g);
    }

    public void renderHeatMap(Graphics2D g) {
        int x = INIT_X, y = INIT_Y;
        g.setStroke(new BasicStroke(0.2f));   //set the thickness of the stroke
        /**
         * Draw Rectangles for each of the data record *
         */
        for (int i = 0; i < rowCount; i++) {
            x = INIT_X;
            /* Render the row label*/

            //if the row has been selected, color it with the selected color else color it with black
            if (i == selectedRow) {
                //System.out.println("******** " + i);
                g.setColor(selectedColor);
            } else {
                g.setColor(new Color(0, 0, 0));
            }

            // System.out.println("The maxRowHeadLength is " + maxRowHeadLength);



            x = maxRowHeadLength - 3 - getRowLabelLength(g, i);

            //  System.out.println("X is now " + x + " and the world size is " + getRowLabelLength(g, i));
            g.drawString(tab.getRowName(i), x, y + (boxHeight));

            x = maxRowHeadLength + 3;  //make sure the longest word can be written before the actual heatmap

            // System.out.println("x is " + x);
            for (int j = 0; j < colCount; j++) {

                // g.setColor(computeValueColor(colMax[j], XY_coords[i][j]));

                g.setColor(dataColors[i][j]);
                g.fillRect(x, y, boxWidth, boxHeight);
                //draw the rectangle with black border line
                //g.setColor(new Color(0, 0, 0));
                //g.drawRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
                g.drawRect(x, y, boxWidth, boxHeight);

                //set the coordinates of the rectangle to be used later.
                dataRects[i][j] = new Rectangles(x, y, boxWidth, boxHeight, data[i][j], i, j);

                x = x + boxWidth;
            }
            y = y + boxHeight;
        }

        /*Write the Column headers  */
        x = maxRowHeadLength + 3;
        y = INIT_Y;  //reset the x and y coordiantes        


        for (int i = 0; i < colCount; i++) {
            AffineTransform orig = g.getTransform();//get the current transform

            // int angle= 0;
            double rotateRadian = 0, offset = 0, labelX, halfRotAngle = 0;

            // if ((maxHeaderLength + 5) > separation) {

            rotateRadian = (headerAngle) * Math.PI / 180;  //radians = degrees * pi/180
            halfRotAngle = (headerAngle / 2.0) * Math.PI / 180;
            //compute the offset
            offset = 2 * Math.sin((halfRotAngle)) * (x);   // 2 * SOH to find the base of the isocelles triangle found 

            labelX = x;  //the 15 is to move the text a 15pixels to the left when it is rotated


            offset = Math.abs(offset);
            //rotateRadian = angle * Math.PI/180  ;  //radians = degrees * pi/180
            g.rotate(-rotateRadian);
            //g.translate(INIT_X,INIT_Y);

            double xtrans = 0, ytrans = 0;
            double xrad = 0;
            /*find the distance of the x after the rotation. : i.e. find the distance where the base of the isoceles triangl
             *  formed during the rotation forms a right-angle with the header of the rotated figure
             */
            int isoAngle = (180 - headerAngle) / 2;
            xrad = (isoAngle) * Math.PI / 180;
            ytrans = Math.sin(xrad) * offset;

            xtrans = Math.sqrt(Math.pow(offset, 2) - Math.pow(ytrans, 2));
            labelX = x - xtrans;
            //y = INIT_Y - maxColHeadLength;

            if (i == selectedCol) {
                g.setColor(selectedColor);
            } else {
                g.setColor(new Color(0, 0, 0));
            }

            g.drawString(tab.getColumnName(i), (int) labelX, (int) ((y - 10) + ytrans));   //write the header on top of the line. Note the 30 is because we wanted the text to be 30 pixels above the vertical line.
            //g.translate((int)labelX, (int) (y - 10) + offset);
            //g.rotate(-rotateRadian,(int)labelX, (int) ((y - 5) + offset));      //perform the rotating to be used only for drawing the header text
            //after writing the rotated text, continue with the normal rotation.
            g.setTransform(orig);
            x = x + boxWidth;
            // y = y + boxHeight;

            //System.out.println("The offset is: " + offset + " and x is: " + x);
        }
        //draw the color scale
        drawColorScale(g);
    }

    public void drawColorScale(Graphics2D g) {
        //draw the scale based on the highest to the lowest color for the row item
        //the scale will be the next to the actual heat map

        int y = INIT_Y + (boxHeight * rowCount) + 30; //draw scale below the box

        int x = maxRowHeadLength - 3 - getRowLabelLength(g, "Scale   1 ");

        g.setColor(new Color(0, 0, 0));

        g.drawString("Scale   1 ", x, y + (boxHeight));
        //int x = INIT_X + maxRowHeadLength ;

        int scaleSize = colCount;

        x = maxRowHeadLength + 3;

        /**
         * Compute the colors
         */
        int colorIndex;
        Color color, color1, color2;

        Color colScale[] = new Color[scaleSize];
        int rangeSize = scaleSize / (scaleColors.size() - 1);

        if (rangeSize == 0) {
            rangeSize = 1;
        }

        colorIndex = 0;
        int r, grn, b, rem, rdf, grndf, bdf;

        color1 = scaleColors.get(0);
        color2 = scaleColors.get(1);
        colScale[0] = color1;
        colorIndex = 1;


        for (int i = 1; i < scaleSize; i++) {

            rem = (i + 1) % (rangeSize);

            if (rem == 0) { //the second color becomes color 1 and increase the colorIndex for the next color
                color1 = color2;
                colScale[i] = color1;

                if (colorIndex == scaleColors.size() - 2) { //if the last range, the last range may not be equal to the others
                    rangeSize = scaleSize - (colorIndex) * (rangeSize);
                }

                if (colorIndex < scaleColors.size() - 1) {
                    colorIndex++;
                    color2 = scaleColors.get(colorIndex);
                }

            } else {

                rdf = ((color2.getRed() - color1.getRed()) / (rangeSize));
                grndf = ((color2.getGreen() - color1.getGreen()) / (rangeSize));
                bdf = ((color2.getBlue() - color1.getBlue()) / (rangeSize));

                r = (color1.getRed() + rdf * rem);
                grn = (color1.getGreen() + grndf * rem);
                b = (color1.getBlue() + bdf * rem);
                colScale[i] = new Color(r, grn, b);
            }
        }

        for (int i = 1; i <= scaleSize; i++) {

            g.setColor(colScale[i - 1]);
            g.fillRect(x, y, boxWidth, boxHeight);
            g.drawRect(x, y, boxWidth, boxHeight);

            x = x + boxWidth;
        }

        g.setColor(new Color(0, 0, 0));
        g.drawString(" " + colCount, x + 3, y + (boxHeight));


    }

    protected int getBoxWidth() {
        return boxWidth;
    }

    protected int getBoxHeight() {
        return boxHeight;

    }

    protected void setBoxWidth(int w) {
        if (w > 1) {  //we make sure the width is at least 2
            boxWidth = w;
        }
    }

    protected void setBoxHeight(int h) {
        if (h > 1) {  //we make sure the height is at least 2;
            boxHeight = h;
        }
    }

    protected void setHighColor(Color c) {
        HighColor = c;
    }

    protected void setLowColor(Color c) {
        LowColor = c;
    }

    public Color computeValueColor(float colmax, float value) {
        int r, g, b;


        //color will range between green and red with green being the highest and 
        //red being the lowest.


        g = (int) (HighColor.getGreen() * (value / colmax));
        r = (int) (HighColor.getRed() * (value) / colmax);
        b = (int) (HighColor.getBlue() * (value) / colmax);

        Color c = new Color(r, g, b);

        return c;

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
     * determine the maxHeader Length
     *
     * @param g
     */
    public void setMaxColHeaderLength(Graphics2D g) {

        //get the maximum
        //we will use the font metric to determine that


        FontMetrics fm = g.getFontMetrics(g.getFont()); // or another font
        int strw = 0;

        for (int i = 0; i < colCount; i++) {
            if (fm.stringWidth(tab.getColumnName(i)) > strw);
            strw = fm.stringWidth(tab.getColumnName(i));
        }

        maxColHeadLength = strw;   //the highest length for the header string

    }

    /**
     * determine the maxHeader Length
     *
     * @param g
     */
    public void setMaxRowHeaderLength(Graphics2D g) {

        FontMetrics fm = g.getFontMetrics(g.getFont()); // or another font
        int strw = 0;
        int ind = 0;
        String word = "Gibberish";

        for (int i = 0; i < rowCount; i++) {
            if (fm.stringWidth(tab.getRowName(i)) > strw) {
                strw = fm.stringWidth(tab.getRowName(i));
                word = tab.getRowName(i);
                ind = i;
            }
        }

        maxRowHeadLength = strw;   //the highest length for the header string

        // System.out.println("The HIGHEST WORD LENTH IS " + word);

    }

    public int getRowLabelLength(Graphics2D g, int indx) {
        int len;

        FontMetrics fm = g.getFontMetrics(g.getFont());
        len = fm.stringWidth(tab.getRowName(indx));

        if (indx == 0) {
            //System.out.println("The first Word is " + tab.getRowName(indx) );
        }

        return len;
    }

    public int getRowLabelLength(Graphics2D g, String str) {
        int len;

        FontMetrics fm = g.getFontMetrics(g.getFont());
        len = fm.stringWidth(str);

        return len;   //
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

    public void setSelectedColor(Color c) {
        selectedColor = c;
    }

    public void simulate() {
    }

    /**
     * **********************************************************
     * Determine whether mouse is on any of the data rectangles
     *
     * @param x -the x coordinate of the mouse pointer
     * @param y - the y coordinate of the mouse pointer
     * @return respective label or an empty String
     * **********************************************************
     */
    /* public String mouseonDataRect(int x, int y) {
     int x1, x2, y1, y2;
     for (int i = 0; i < rowCount; i++) {
     for (int j = 0; j < colCount; j++) {
     x1 = dataRects[i][j].getX();
     x2 = x1 + boxWidth;
     y1 = dataRects[i][j].getY();
     y2 = y1 + boxHeight;

     if ((x >= x1) && (x <= x2) && ((y >= y1) && (y <= y2))) {
     return dataRects[i][j].getLabel();
     }
     }
     }
     return "";
     } */
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
        String tip, rowname, colname;
        //check if mouse is on a data Rectangle
        Rectangles rect = mouseOnDataRect(x, y);

        if ((rect != null)) { //if it is not an empty string
            rowname = tab.getRowName(rect.getRow()).trim();
            colname = tab.getColumnName(rect.getCol()).trim();

            tip = rowname + ",   " + colname + " = " + rect.getLabel();
//            /System.out.println("The tooltip is "+tip);
            this.setToolTipText(tip);

            
            
            return true;
        }
        return false;
    }

    public Rectangles mouseOnDataRect(int x, int y) {
        int x1, x2, y1, y2;
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                x1 = dataRects[i][j].getX();
                x2 = x1 + boxWidth;
                y1 = dataRects[i][j].getY();
                y2 = y1 + boxHeight;

                if ((x >= x1) && (x <= x2) && ((y >= y1) && (y <= y2))) {
                    return dataRects[i][j];
                }
            }
        }
        return null;
    }

    /**
     * if mouse is pressed on any of the dataRectangle fields, color the header
     * and the rowname as well as the box. if it was pressed within a vertical
     * box, mark it as the beginning of dragging
     *
     * @param x x-coordinate of the mouse
     * @param y y-coordinate of the mouse
     * @param button
     * @return true or false
     */
    @Override
    public boolean mousepressed(int x, int y, int button) {
        //check if mouse is on data rectangle 
        Rectangles rect = mouseOnDataRect(x, y);

        if ((rect != null)) {
            selectedRow = rect.getRow();
            selectedCol = rect.getCol();
            return true;
        } else {//set the selected col and row to negative values
            selectedRow = -1;
            selectedCol = -1;
        }

        return false;

    }

    public void setScaleColors(ArrayList<Color> colors) {
        scaleColors = colors;

        computeDataColors();

    }
}

