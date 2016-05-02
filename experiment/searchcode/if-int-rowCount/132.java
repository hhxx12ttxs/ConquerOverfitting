package taskschedulingsimgui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JPanel;

/**
 *
 * @author Ljubo Raicevic <rljubo90@gmail.com>
 */
public class Surface extends JPanel {
    
    int width;
    int lineHeight = 40;
    int timeUnitPixelRatio;
    int yStart = (int) Math.ceil(lineHeight / 2);
    int arrowLength = Math.round(lineHeight / 3);
    int instanceCompletedLength = Math.round(lineHeight / 3);
    int deadlineBreachRaduis = lineHeight / 2;
    HashMap<String, Task> tasks = new HashMap<>();
    int startRenderingFromTime;
    int endRenderingAtTime;
    boolean showDeadline;
  
    public Surface(File f, int pStart, int pEnd, int pWidth, boolean pShowDeadline) {
        TraceFileAnalyzer a = new TraceFileAnalyzer(this, f, pStart, pEnd);
        this.startRenderingFromTime = pStart;
        this.endRenderingAtTime = pEnd;
        this.showDeadline = pShowDeadline;
        this.width = pWidth;
        timeUnitPixelRatio = (int) Math.round(lineHeight / width);
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        doDrawing(g);
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(
                (endRenderingAtTime - startRenderingFromTime) * timeUnitPixelRatio, 
                2000);
    }
    
    // <editor-fold defaultstate="collapsed" desc="Methods for drawing shapes">
    private void drawTimeLines(Graphics2D g, int step) {
        int iCount = 0;
        int intervalLength = endRenderingAtTime - startRenderingFromTime;
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g.setColor(Color.lightGray);
        
        while (iCount < intervalLength) {
            g.drawString(
                    Integer.toString(iCount + startRenderingFromTime), 
                    iCount * timeUnitPixelRatio + 2, 
                    10);
            
            g.drawLine(
                    iCount * timeUnitPixelRatio, 
                    0, 
                    iCount * timeUnitPixelRatio, 
                    this.getPreferredSize().height);
            
            iCount += step;
        }
        
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }
    
    private void drawUpArrow(Graphics2D g, int pTime, int row) {
        int yRow = yStart + row * lineHeight * 2;
        int time = (pTime - startRenderingFromTime) * timeUnitPixelRatio;
        
        g.setColor(Color.blue);
        g.drawLine(time, yRow, time, yRow + lineHeight);
        g.drawLine(time, yRow, time - arrowLength, yRow + arrowLength);
        g.drawLine(time, yRow, time + arrowLength, yRow + arrowLength);
    }
    
    private void drawDownArrow(Graphics2D g, int pTime, int row) {
        int yRow = yStart + row * lineHeight * 2;
        int time = (pTime -startRenderingFromTime) * timeUnitPixelRatio;
        
        g.setColor(Color.red);
        g.drawLine(time, yRow, time, yRow + lineHeight);
        g.drawLine(time, yRow + lineHeight, time - arrowLength, 
                yRow + lineHeight - arrowLength);
        g.drawLine(time, yRow + lineHeight, time + arrowLength, 
                yRow + lineHeight - arrowLength);
    }
    
    private void drawDeadlineBreachSymbol(Graphics2D g, int pTime, int row) {
        int yRow = yStart + row * lineHeight * 2;
        int time = (pTime - startRenderingFromTime) * timeUnitPixelRatio;
        
        g.setColor(Color.red);
        g.fillOval(time - Math.round(deadlineBreachRaduis / 2), yRow, 
                deadlineBreachRaduis, deadlineBreachRaduis);
    }
    
    private void drawInstanceCompleted(Graphics2D g, int pTime, int row) {
        int yRow = yStart + row * lineHeight * 2 + Math.round(lineHeight / 2);
        int time = (pTime - startRenderingFromTime) * timeUnitPixelRatio;
        
        g.setColor(Color.BLACK);
        g.drawLine(time, yRow, time, yRow - instanceCompletedLength);
        g.drawLine(time, yRow - instanceCompletedLength, 
                time + instanceCompletedLength, 
                yRow - Math.round(instanceCompletedLength / 2));
        g.drawLine(time, yRow, time + instanceCompletedLength, 
                yRow - Math.round(instanceCompletedLength / 2));
    }
    
    private void drawActivationTimes(
            Graphics2D g, 
            ArrayList<Integer> activationTimes,
            int row) {
        
        for (Integer time : activationTimes) {
            drawUpArrow(g, time, row);
        }
    }
    
    private void drawDeadlines(
            Graphics2D g, 
            ArrayList<Integer> absoluteDeadlines,
            int row) {
        
        for (Integer time : absoluteDeadlines) {
            drawDownArrow(g, time, row);
        }
    }
    
    private void drawDeadlineBreaches(
            Graphics2D g, 
            ArrayList<Integer> deadlineBreaches,
            int row) {
        
        for (Integer time : deadlineBreaches) {
            if (time != -1) {
                drawDeadlineBreachSymbol(g, time, row);
            }
        }
    }
    
    private void drawExecutions(
            Graphics2D g, 
            ArrayList<ArrayList<Integer>> startTimes, 
            ArrayList<ArrayList<Integer>> endTimes,
            int row) {
        
        int yRow = yStart + row * lineHeight * 2;
        
        for (int iCount = 0; iCount < startTimes.size(); iCount++) {
            g.setColor(Color.pink);
            
            for (int jCount = 0; jCount < startTimes.get(iCount).size(); jCount++) {
                int start = startTimes.get(iCount).get(jCount);
                int lengthOfExecution = endTimes.get(iCount).get(jCount) - start;
                
                g.fillRect(
                        (startTimes.get(iCount).get(jCount) - startRenderingFromTime) * timeUnitPixelRatio + 1,
                        yRow + Math.round(lineHeight / 2), 
                        lengthOfExecution * timeUnitPixelRatio, 
                        (int) Math.ceil(lineHeight / 2) + 1);
            }
            
            drawInstanceCompleted(g, endTimes.get(iCount).get(endTimes.get(iCount).size() - 1), row);
        }
    }// </editor-fold> 
   
    private void doDrawing(Graphics g) {
        
        Graphics2D g2d = (Graphics2D) g;
        
        int rowCount = 0;
        
        for (Task t : tasks.values()) {
            ArrayList<Integer> a = t.getActivationTimes();
            ArrayList<Integer> d = t.getAbsoluteDeadlines();
            ArrayList<Integer> db = t.getDeadlineBreaches();
            ArrayList<ArrayList<Integer>> s = t.getStartTimes();
            ArrayList<ArrayList<Integer>> e = t.getEndTimes();
        
            drawTimeLines(g2d, (int)Math.ceil(width * 1.5));
            
            drawActivationTimes(g2d, a, rowCount);
            if (showDeadline) { 
                drawDeadlines(g2d, d, rowCount); 
            }
            drawDeadlineBreaches(g2d, db, rowCount);
            drawExecutions(g2d, s, e, rowCount);
            
            g2d.drawLine(
                    0, 
                    yStart + lineHeight + (rowCount * lineHeight) * 2, 
                    this.getPreferredSize().width, 
                    yStart + lineHeight + (rowCount * lineHeight) * 2);
            
            rowCount++;
        }
    }
}

