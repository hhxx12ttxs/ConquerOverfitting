package happywallrobot;

import basicrobotcontrol.MainControl;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class FloorMonitor extends JPanel implements Runnable {

    private final double GRIDSIZE = 0.3;    //30cm
    private final int DISPLAYGRIDSIZE = 20;
    MainControl mc;
    int gridSizeX, gridSizeY;
    double segMinX, segMinY;
    boolean[][] floorGrid;
    private int integralCleanedGrids = 0;
    private int cleanedGrids = 0;
    private JTextField text;

    // -------------------------------------------------------------------------
    public FloorMonitor(MainControl mc) {
        this.mc = mc;
        createGrid();
        setPreferredSize(new Dimension(gridSizeX * DISPLAYGRIDSIZE, gridSizeY * DISPLAYGRIDSIZE));
        displayGrid();
    }

    // -------------------------------------------------------------------------
    // create floor grid from robot map
    private void createGrid() {
        double[] minMax = getSegmentMinMax(mc.robot.map.getMapData());
        segMinX = minMax[0];
        segMinY = minMax[1];
        double extentX = minMax[2] - minMax[0];
        double extentY = minMax[3] - minMax[1];
        gridSizeX = (int) (extentX / GRIDSIZE) + 1;
        gridSizeY = (int) (extentY / GRIDSIZE) + 1;
        floorGrid = new boolean[gridSizeX][gridSizeY];
    }

    // -------------------------------------------------------------------------
    private double[] getSegmentMinMax(float[][] segmentMap) {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (float[] s : segmentMap) {
            if (s[0] < minX) {
                minX = s[0];
            }
            if (s[2] < minX) {
                minX = s[2];
            }
            if (s[1] < minY) {
                minY = s[1];
            }
            if (s[3] < minY) {
                minY = s[3];
            }
            if (s[0] > maxX) {
                maxX = s[0];
            }
            if (s[2] > maxX) {
                maxX = s[2];
            }
            if (s[1] > maxY) {
                maxY = s[1];
            }
            if (s[3] > maxY) {
                maxY = s[3];
            }
        }
        double[] minMax = {minX, minY, maxX, maxY};
        return (minMax);
    }

    // -------------------------------------------------------------------------
    private int[] segToGridCoord(double sx, double sy) {
        int[] g = {(int) Math.round((sx - segMinX) / GRIDSIZE), (int) Math.round((sy - segMinY) / GRIDSIZE)};
        return (g);
    }

    private double[] gridToSegCoord(int gx, int gy) {
        double[] s = {gx * GRIDSIZE + segMinX, gy * GRIDSIZE + segMinY};
        return (s);
    }

    // -------------------------------------------------------------------------
    private void displayGrid() {
        JFrame f = new JFrame("Floor Grid");
        f.setLayout(new BorderLayout());
        f.add(this,"Center");
        text = new JTextField();
        f.add(text,"South");
        f.pack();
        f.setVisible(true);
    }

    // -------------------------------------------------------------------------
    public void paintComponent(Graphics g) {
        cleanedGrids = 0;
        g.setColor(Color.black);
        g.fillRect(0, 0, getWidth(), getHeight());
        for (int x = 0; x < gridSizeX; x++) {
            for (int y = 0; y < gridSizeY; y++) {
                boolean clean = floorGrid[x][y];
                Color c = (clean ? Color.blue : new Color(100, 30, 30));
                cleanedGrids = (clean ? cleanedGrids + 1 : cleanedGrids);
                g.setColor(c);
                g.fillRect(x * DISPLAYGRIDSIZE + 1, this.getHeight() - y * DISPLAYGRIDSIZE + 1, DISPLAYGRIDSIZE - 1, DISPLAYGRIDSIZE - 1);
            }
        }
    }

    // -------------------------------------------------------------------------
    public void run() {
        long SLEEPYTIME = 50;
        long lastPrintUpdate = 0;
        int evalCounter=1;
        while (true) {
            long startTime = System.currentTimeMillis();

            // check robot position, change grid accordingly
            double[] mapPose = mc.robot.map.coordsRobotToMap(mc.robot.getPose());
            int[] gridIndex = segToGridCoord(mapPose[0], mapPose[1]);
            floorGrid[gridIndex[0]][gridIndex[1]] = true;

            // printint + evaluation: every second
            if ((startTime - lastPrintUpdate) > 1000) {
                lastPrintUpdate = startTime;
                this.repaint();

                // evaluation
                integralCleanedGrids += cleanedGrids;
                double totalPerc = (double)cleanedGrids/gridSizeX/gridSizeY;
                double avgPerc = (double)integralCleanedGrids/gridSizeX/gridSizeY/evalCounter;
                evalCounter++;
                text.setText("Time: "+evalCounter+"\tTotal: "+totalPerc*100+ "\tScore: "+avgPerc*100);
            }

            // timing
            long currentTime = System.currentTimeMillis();
            long pauseTime = Math.max(1, SLEEPYTIME - (currentTime - startTime));
            try {
                Thread.sleep(pauseTime);
            } catch (InterruptedException ex) {
            }
        }
    }
}

