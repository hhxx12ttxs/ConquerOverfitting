/*******************************************************************************
 * Raster.java
 *
 * Programmed By:
 *
 * David Finlayson, Ph.D.
 * Geologist
 * US Geological Survey
 * 400 Natural Bridges Drive
 * Santa Cruz, CA 95060
 * dfinlayson@usgs.gov
 *
 * DISCLAIMER
 *
 * This program and supporting information is furnished by the government of the
 * United States of America, and is accepted and used by the recipient with the
 * understanding that the United States government makes no warranties, express
 * or implied, concerning the accuracy, completeness, reliability, or suitability
 * of this program, of its constituent parts, or of any supporting data.
 *
 * The government of the United States of America shall be under no liability
 * whatsoever resulting from any use of this program. This program should not be
 * relied upon as the sole basis for solving a problem whose incorrect solution
 * could result in injury to person or property.
 *
 * This program is property of the government of the United States of America.
 * Therefore, the recipient further agrees not to assert proprietary rights
 * therein and not to represent this program to anyone as being other than a
 * government program.
 *
 * HISTORY:
 *
 * 2009-12-27 - Fixed a bug reading ESRI raster file headers when the values
 *              were vertically aligned (variable spaces between keyword and
 *              header value). Now the program expects that the header value
 *              is the last non-space token on the line.
 * 2009-11-25 - Fixed several bugs reading ESRI raster files.
 * 2009-11-02 - Fixed check for surfer blankValue to be value >= blankValue.
 *
 * 2009-08-27 - Added the ability to compare a raster to another raster for
 *              same size, dimensions and coordinate system (not values).
 *
 * 2009-02-03 - Changed floating point comparison of target == blankValue to
 *              Math.abs(target - blankValue) < 0.001 to try and prevent errors
 *              detecting the blankValue when it is set to a floating point
 *              number such as with SURFER_BLANKVALUE.
 ******************************************************************************/
 package gov.usgs.wr.raster;

import java.io.*;
import java.util.*;

/**
 * <p>A Raster is a rectangular region comprised of evenly spaced rows and columns.
 * The intersection of a row and column is called a grid node. Rows contain
 * grid nodes with the same Y coordinate, and columns contain grid nodes with
 * the same X coordinate.</p>
 * 
 * <p>The z values are stored in a 2D matrix of doubles in row-major order, with
 * the lowest row (minimum Y) first.</p>
 * 
 * <p>The abstract Raster class is modelled after the the Surfer 7 binary format
 * and is defined identically (some format documentation is lifted directly from
 * the Surfer 8 printed manual).</p>
 *
 * <p>Rasters are thread safe.</p>
 * 
 * @author David Finlayson <dfinlayson@usgs.gov>
 */
public class Raster
{
    /** Number of rows */
    private final int nRows;
    /** Number of columns */
    private final int nCols;
    /** The x lower-left coordinate value */
    private final double xLL;
    /** The y lower-left coordinate value */
    private final double yLL;
    /** The x cellsize */
    private final double xSize;
    /** They y cellsize */
    private final double ySize;
    /** Raster values */
    private final double values[][];
    /** The minimum z value */
    private double zMin;
    /** The maximum z value */
    private double zMax;
    /** Value representing blank or no data */
    private double blankValue;
    /** This flag is set to true when the z limits need to be recalculated */
    private boolean staleZLimits;
    /** Default blank value for Surfer formatted files */
    public static final double SURFER_BLANKVALUE = 1.70141e38;
    /** Default blank value for ESRI formatted files */
    public static final double ESRI_BLANKVALUE = -9999;

    /**
     * Create a new blank Raster with the given dimensions
     * @param nRows Number of rows in the raster
     * @param nCols Number of columns in the raster
     * @param xLLCorner the x-coordinate of the lower-left node
     * @param yLLCorner the y-coordinate of the lower-left node
     * @param xSize the x-dimension size or spacing between nodes
     * @param ySize the y-dimension size or spacing between nodes
     * @param getBlankValue the value used to represent no data.
     */
    public Raster(int nRows, int nCols, double xLLCorner, double yLLCorner,
            double xSize, double ySize, double blankValue)
    {
        if (nRows < 1 || nCols < 1 || xSize <= 0.0 || ySize <= 0) {
            throw new IllegalArgumentException("nRows: " + nRows + " nCols: " +
                    nCols + " xSize: " + xSize + " ySize: " + ySize);
        }

        this.nRows = nRows;
        this.nCols = nCols;
        this.xLL = xLLCorner;
        this.yLL = yLLCorner;
        this.xSize = xSize;
        this.ySize = ySize;
        this.blankValue = blankValue;
        this.values = new double[nRows][nCols];
        setAll(blankValue);
    }

    /**
     * Load a Surfer GS ASCII formatted file into a Raster object
     * @param filename name of GS ASCII file
     * @throws RasterException if the input file is not a Surfer GS ASCII raster.
     */
    public static Raster loadSurferASCII(File filename) throws IOException,
            RasterException
    {
        // Open file stream
        BufferedReader in = new BufferedReader(
                new FileReader(filename));

        // Check file identification string
        String id = in.readLine();
        if (!id.equals("DSAA")) {
            throw new RasterException(RasterException.INCOMPATIBLE_RASTER);
        }

        // Read the rest of the header
        String[] tokens = in.readLine().split("\\s");
        int nx = Integer.parseInt(tokens[0]);
        int ny = Integer.parseInt(tokens[1]);

        tokens = in.readLine().split("\\s");
        double xlo = Double.parseDouble(tokens[0]);
        double xhi = Double.parseDouble(tokens[1]);

        tokens = in.readLine().split("\\s");
        double ylo = Double.parseDouble(tokens[0]);
        double yhi = Double.parseDouble(tokens[1]);

        // skip over the z limits (we calculate them later)
        in.readLine();

        // Store the header
        int nCols = nx;
        int nRows = ny;
        double xLLCorner = xlo;
        double yLLCorner = ylo;
        double xSize = (xhi - xlo) / (double)(nx - 1);
        double ySize = (yhi - ylo) / (double)(ny - 1);
        double blankValue = Raster.SURFER_BLANKVALUE;
        Raster r = new Raster(nRows, nCols, xLLCorner, yLLCorner, xSize, ySize,
                blankValue);

        // Load the data from the file. You need a little buffer mechanism here
        //(I used a Queue) to accomidate the common situation where the number
        // of nodes stored in a line of the input file does not equal the number
        // of columns in a row of data. The idea is to fill the nodes in the 2D
        // array from the Queue, if the Queue is empty, stop and fill it again.
        Queue<Double> rowOfData = new LinkedList<Double>();
        for (int row = 0; row < nRows; row++) {
            for (int col = 0; col < nCols; col++) {

                // Check if we need to re-fill the data queue
                if (rowOfData.isEmpty()) {

                    // Read until we get data (skip over blank lines)
                    tokens = in.readLine().split("\\s");
                    while (tokens.length == 1 && tokens[0].equals("")) {
                        tokens = in.readLine().split("\\s");
                    }

                    // Add the data to the data queue
                    for (String node : tokens) {
                        rowOfData.offer(Double.parseDouble(node));
                    }
                }

                // Pull one node value from the data stack for each (row, col)
                r.set(row, col, rowOfData.remove());
            }
        }

        // close the file
        in.close();

        return r;
    }

    private static double readNextToken(String token, BufferedReader in) throws
            IOException
    {
        String[] tokens = in.readLine().split("\\s");
        if (!tokens[0].equalsIgnoreCase(token)) {
            throw new RasterException(RasterException.INCOMPATIBLE_RASTER);
        } else {
            double value = 0.0;
            try {
                value = Double.parseDouble(tokens[tokens.length - 1]);
            } catch(NumberFormatException e) {
                System.err.println("Raster.jar: error: Error reading header token " + token);
                System.exit(1);
            }
            return value;
        }
    }

    /**
     * Load an ESRI ASCII Grid formatted file into a Raster object
     * @param filename name of ESRI ASCII Grid file
     * @throws RasterException if the input file is not a ESRI Grid ASCII raster.
     */
    public static Raster loadESRIASCII(File filename) throws IOException,
            RasterException
    {
        // Open file stream
        BufferedReader in = new BufferedReader(new FileReader(filename));

        // Read the rest of the header
        int nCols = (int)readNextToken("NCOLS", in);
        int nRows = (int)readNextToken("NROWS", in);
        double xLLCorner = readNextToken("XLLCORNER", in);
        double yLLCorner = readNextToken("YLLCORNER", in);
        double cellsize = readNextToken("CELLSIZE", in);
        double blankValue = readNextToken("NODATA_VALUE", in);

        double xSize = cellsize;
        double ySize = cellsize;

        // Go from grid-registration to node-registration
        xLLCorner += (cellsize * 0.5);
        yLLCorner += (cellsize * 0.5);

        // Create a new Raster
        Raster r = new Raster(nRows, nCols, xLLCorner, yLLCorner, xSize, ySize,
                Raster.SURFER_BLANKVALUE);

        // Load the data node-by-node while reading the file line-by-line
        Queue<Double> rowOfData = new LinkedList<Double>();
        for (int row = nRows - 1; row >= 0; row--) {
            for (int col = 0; col < nCols; col++) {
                // Load the data queue a line-at-a-time
                if (rowOfData.isEmpty()) {

                    // Read until we get data (skip over blank lines)
                    String[] tokens = in.readLine().split("\\s");
                    while (tokens.length == 1 && tokens[0].equals("")) {
                        tokens = in.readLine().split("\\s");
                    }

                    // Add the data to the data queue
                    for (String node : tokens) {
                        rowOfData.offer(Double.parseDouble(node));
                    }
                }

                // Pull one node value from the data stack for each (row, col)
                double value = rowOfData.remove();

                // convert to Surfer blankValue convention
                if (value == blankValue) {
                    value = Raster.SURFER_BLANKVALUE;
                }
                r.set(row, col, value);
            }
        }

        // close the file
        in.close();
        return r;
    }

    /**
     * Get the number of rows in the raster.
     * @return the number of rows.
     */
    public int nRows()
    {
        return nRows;
    }

    /**
     * Get the number of columns in the raster.
     * @return the number of columns.
     */
    public int nCols()
    {
        return nCols;
    }

    /**
     * Get the x coordinate of the lower-left node of the raster
     * @return coordinate value of the lower-left node
     */
    public double xLLCorner()
    {
        return xLL;
    }

    /**
     * Get the y coordinate value of the lower-left node of the raster
     * @return coordinate value of the lower-left node.
     */
    public double yLLCorner()
    {
        return yLL;
    }

    /**
     * Get the size (spacing) between neighboring nodes along the x axis.
     * @return x size between nodes
     */
    public double xSize()
    {
        return xSize;
    }

    /**
     * Get the y size (spacing) between neighboring nodes along the y axis.
     * @return y size between nodes.
     */
    public double ySize()
    {
        return ySize;
    }

    /**
     * Get the minimum z value in the raster.
     * @return the minimum z value.
     */
    public double zMin()
    {
        if (staleZLimits) {
            updateZLimits();
        }
        return zMin;
    }

    /**
     * Get the maximum z value in the raster
     * @return the maximum z value
     */
    public double zMax()
    {
        if (staleZLimits) {
            updateZLimits();
        }
        return zMax;
    }

    /**
     * Get the blank or NODATA value of the raster.
     * @return the blank value.
     */
    public double getBlankValue()
    {
        return blankValue;
    }

    /**
     * Set the blank or NODATA value of the raster.
     * @param getBlankValue the blank value
     */
    public void setBlankValue(double blankValue)
    {
        this.blankValue = blankValue;
        staleZLimits = true;
    }

    /**
     * Set all nodes in the raster to a constant value
     * @param value the value
     */
    public void setAll(double value)
    {
        assert (0 < nRows);
        assert (0 < nCols);

        for (int row = 0; row < nRows; row++) {
            for (int col = 0; col < nCols; col++) {
                values[row][col] = value;
            }
        }
        staleZLimits = true;
    }

    /**
     * Set the value of the node at (row, col).
     * @param row the row number
     * @param col the column number
     * @param value the value
     */
    public void set(int row, int col, double value)
    {
        // throws an IndexOutOfBounds error if row or col is invalid,
        // this seems appropriate for the set method.
        values[row][col] = value;
        staleZLimits = true;
    }

    /**
     * Set the node at (row, col) to be blank
     * @param row
     * @param col
     */
    public void setBlank(int row, int col)
    {
        values[row][col] = blankValue;
        staleZLimits = true;
    }

    /**
     * Get the value of the node at (row, col).
     * @param row the row number
     * @param col the column number
     * @return the value of the node at (row, col)
     */
    public double get(int row, int col)
    {
        // throws an IndexOutOfBounds exception if row or col is invalid
        // this seems appropriate for the get method, too.
        return values[row][col];
    }

    /**
     * Convert a y coordinate to a row number
     * @param y the y coordinate
     * @return the row number
     */
    private int yToRow(double y)
    {
        assert (yLL <= y);
        assert (0 < ySize);
        return (int)Math.floor((y - yLL) / ySize);
    }

    /**
     * Convert an x coordinate to a column number.
     * @param x the x coordinate
     * @return the column number
     */
    private int xToCol(double x)
    {
        assert (xLL <= x);
        assert (0 < xSize);
        return (int)Math.floor((x - xLL) / xSize);
    }

    /**
     * Set the value of node nearest to coordinates (x, y).
     * @param x the x coordinate
     * @param y the y coordinate
     * @param value the value
     */
    public void setNearest(double x, double y, double value)
    {
        int col = xToCol(x);
        int row = yToRow(y);
        set(row, col, value);
    }

    /**
     * Get the value of the node nearest to coordinate (x, y).
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the value nearest to x, y in the grid
     */
    public double getNearest(double x, double y)
    {
        int col = xToCol(x);
        int row = yToRow(y);
        return get(row, col);
    }

    /**
     * Interpolate the raster value at (x, y) by using bilinear interpolation of 
     * the four nearest nodes surrounding the point.
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the interpolated value at (x, y)
     */
    public double getBilinear(double x, double y)
    {
        // Convert point (x, y) to (row, col) with fractional parts.
        double col = (x - xLL) / xSize;
        double row = (y - yLL) / ySize;

        // Calcualte the (row, col) of the 4 nodes surrounding point (x, y)
        int x1_col = (int)Math.floor(col);
        int y1_row = (int)Math.floor(row);
        int x2_col = (int)Math.ceil(col);
        int y2_row = (int)Math.ceil(row);

        // Find the values of the 4 nodes surrounding point (x, y)
        // this will throw a RasterException if x,y is not withing the
        // raster bounds.
        double Q11 = get(y1_row, x1_col); // value of lower left node
        double Q12 = get(y2_row, x1_col); // value of upper left node
        double Q22 = get(y2_row, x2_col); // value of upper right node
        double Q21 = get(y1_row, x2_col); // value of lower right node

        // Interpolate the raster value at point (x, y)
        double result;
        if (Q11 == blankValue || Q12 == blankValue || Q22 == blankValue ||
                Q21 == blankValue) {
            // If any node is blank, the result it blank
            result = blankValue;
        } else {

            // Find the coordinates of the 4 nodes surrounding (x, y)
            double x1 = xLL + (x1_col * xSize);
            double y1 = yLL + (y1_row * ySize);
            double x2 = xLL + (x2_col * xSize);
            double y2 = yLL + (y2_row * ySize);

            // Linearly interpolate in the x-direction
            double R1;
            double R2;
            if (x == x1) {
                R1 = Q11;
                R2 = Q12;
            } else if (x == x2) {
                R1 = Q21;
                R2 = Q22;
            } else {
                R1 = (x2 - x) / (x2 - x1) * Q11 + (x - x1) / (x2 - x1) * Q21;
                R2 = (x2 - x) / (x2 - x1) * Q12 + (x - x1) / (x2 - x1) * Q22;
            }

            // Linearly interpolate in the y-direction
            if (y == y1) {
                result = R1;
            } else if (y == y2) {
                result = R2;
            } else {
                result = (y2 - y) / (y2 - y1) * R1 + (y - y1) / (y2 - y1) * R2;
            }
        }
        return (result);
    }

    /**
     * Recalculate the minimum and maximum z values in the raster
     */
    private void updateZLimits()
    {
        zMin = Double.MAX_VALUE;
        zMax = Double.MIN_VALUE;
        for (int row = 0; row < nRows; row++) {
            for (int col = 0; col < nCols; col++) {
                double value = values[row][col];
                if (value >= blankValue) {
                    continue;
                } else {
                    zMin = Math.min(zMin, value);
                    zMax = Math.max(zMax, value);
                }
            }
        }
        staleZLimits = false;
    }

    /**
     * Returns true if two rasters have the same extent, cell dimensions and
     * coordinate system.
     * @param r a raster to compare with this raster
     * @return true if the rasters are compatible
     */
    public boolean sameButValues(Raster r)
    {
        if (nRows == r.nRows()
                && nCols == r.nCols()
                && xLL == r.xLLCorner()
                && yLL == r.yLLCorner()
                && xSize == r.xSize()
                && ySize == r.ySize()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Store a raster to file in Surfer GS ASCII format. Blanked nodes will
     * automatically be converted to Surfer's default blank value.
     * @param filename name of file to save.
     */
    public void saveSurferASCII(File filename) throws IOException
    {
        // Open a file stream
        BufferedWriter out = new BufferedWriter(new FileWriter(filename));

        // Calculate the raster header
        updateZLimits();
        int nx = nCols;
        int ny = nRows;
        double xlo = xLL;
        double ylo = yLL;
        double xhi = (xSize * (nx - 1)) + xlo;
        double yhi = (ySize * (ny - 1)) + ylo;
        double zlo = zMin;
        double zhi = zMax;

        // Write the header to the file
        StringBuilder sb = new StringBuilder();
        sb.append("DSAA");
        sb.append("\r\n");

        sb.append(Integer.toString(nx));
        sb.append(" ");
        sb.append(Integer.toString(ny));
        sb.append("\r\n");

        sb.append(Double.toString(xlo));
        sb.append(" ");
        sb.append(Double.toString(xhi));
        sb.append("\r\n");

        sb.append(Double.toString(ylo));
        sb.append(" ");
        sb.append(Double.toString(yhi));
        sb.append("\r\n");

        sb.append(Double.toString(zlo));
        sb.append(" ");
        sb.append(Double.toString(zhi));
        sb.append("\r\n");

        out.write(sb.toString());

        // Write the data section
        for (int row = 0; row < nRows; row++) {
            sb = new StringBuilder();
            for (int col = 0; col < nCols; col++) {
                double value = values[row][col];
                if (value >= blankValue) {
                    sb.append(Double.toString(SURFER_BLANKVALUE));
                } else {
                    sb.append(Double.toString(value));
                }
                sb.append(" ");
            }
            sb.append("\r\n");
            out.write(sb.toString());
        }

        // close the file
        out.close();
    }

    /**
     * Save a Raster as an ESRI ASCII Grid file. Note that the xSize and ySize
     * must be identical for a valid conversion.
     * @param filename ESRI ASCII Grid filename
     * @throws java.io.IOException if underlying stream throws an exception
     * @throws IllegalStateException if the Raster is incompatible with ESRI ASCII Grid format.
     */
    public void saveESRIASCII(File filename) throws IOException
    {
        // Check that this raster can be a valid ESRI ASCII Grid
        if (xSize != ySize) {
            throw new IllegalStateException("xSize: " + xSize + " ySize: " +
                    ySize);
        }

        // Open a file output stream
        BufferedWriter out = new BufferedWriter(new FileWriter(filename));

        // Create a string buffer to increase string I/O performance
        StringBuilder sb = new StringBuilder();
        String nl = System.getProperty("line.separator");

        // Create the header
        updateZLimits();

        double xllcorner = xLL - (xSize * 0.5);
        double yllcorner = yLL - (ySize * 0.5);
        double cellsize = xSize;
        double nodata_value = blankValue;

        // Write the header
        sb.append("NCOLS ");
        sb.append(Integer.toString(nCols));
        sb.append(nl);

        sb.append("NROWS ");
        sb.append(Integer.toString(nRows));
        sb.append(nl);

        sb.append("XLLCORNER ");
        sb.append(Double.toString(xllcorner));
        sb.append(nl);

        sb.append("YLLCORNER ");
        sb.append(Double.toString(yllcorner));
        sb.append(nl);

        sb.append("CELLSIZE ");
        sb.append(Double.toString(cellsize));
        sb.append(nl);

        sb.append("NODATA_VALUE ");
        sb.append(Double.toString(nodata_value));
        sb.append(nl);

        out.write(sb.toString());

        // Write the data section
        for (int row = nRows - 1; row >= 0; row--) {
            sb = new StringBuilder();
            for (int col = 0; col < nCols; col++) {
                sb.append(Double.toString(values[row][col]));
                sb.append(" ");
            }
            sb.append(nl);
            out.write(sb.toString());
        }

        // close the file
        out.close();
    }

    @Override
    public String toString()
    {
        return "Raster [nRows: " + nRows + " nCols: " + nCols + "]";
    }
}

