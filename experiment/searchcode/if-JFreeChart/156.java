/*
 * Copyright 2011 Keith Flanagan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/*
 * NotificationMessageListPanel.java
 *
 * Created on Aug 12, 2011, 2:23:24 PM
 */
package uk.org.microbase.ui.graphs;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.date.DateUtilities;
import uk.org.microbase.dist.processes.DistributedProcessList;
import uk.org.microbase.dist.processes.ExecutionResult;
import uk.org.microbase.dist.processes.ExecutionResultLog;
import uk.org.microbase.dist.processes.ProcessListException;
import uk.org.microbase.notification.data.Message;
import uk.org.microbase.runtime.ClientRuntime;
import uk.org.microbase.ui.admin.ViewMessageDialog;

/**
 *
 * @author Keith Flanagan
 */
public class ExecutionCompletionGraphPanel
    extends javax.swing.JPanel
{
  private static final Logger l = 
      Logger.getLogger(ExecutionCompletionGraphPanel.class.getName());
  
  private ClientRuntime runtime;
  
  /** Creates new form NotificationMessageListPanel */
  public ExecutionCompletionGraphPanel()
  {
    initComponents();
//    JFreeChart chart = testData();
//    ChartPanel chartP = new ChartPanel(chart);
    chartPanel.add(new JLabel(
        "To render a graph, choose from the options above"), BorderLayout.CENTER);
  }
  
  public void setRuntime(ClientRuntime runtime)
  {
    this.runtime = runtime;
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        refreshButton = new javax.swing.JButton();
        chartPanel = new javax.swing.JPanel();

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        refreshButton.setText("Refresh");
        refreshButton.setFocusable(false);
        refreshButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        refreshButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(refreshButton);

        chartPanel.setLayout(new java.awt.BorderLayout());

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jToolBar1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
                    .add(chartPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(jToolBar1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chartPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

  private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_refreshButtonActionPerformed
  {//GEN-HEADEREND:event_refreshButtonActionPerformed
    chartPanel.removeAll();
    chartPanel.add(new JLabel("Please wait... obtaining data..."), BorderLayout.CENTER);
    
    Thread t = new Thread()
    {
      @Override
      public void run()
      {
        try
        {
          //generate chart data
          //final XYSeriesCollection dataset = generateChartData();
          final JFreeChart chart = generateChartData();
          
          SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run()
            {
              System.out.println("Displaying chart");
              displayChart(chart);
              revalidate();
            }
          });
          
        }
        catch(final Exception e)
        {
          SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run()
            {
              e.printStackTrace();
              chartPanel.removeAll();
              chartPanel.add(new JLabel(
                  "Failed to query database: "+e.getMessage()),
                  BorderLayout.CENTER);
              revalidate();
            }
          });
        }
      }
    };
    t.setDaemon(true);
    t.start();
  }//GEN-LAST:event_refreshButtonActionPerformed

  private JFreeChart generateChartData()
      throws ProcessListException
  {
//    DistributedProcessList procList = runtime.getProcessList();
    ExecutionResultLog resultLog = runtime.getExecutionResultLog();
    
    int numDataPoints = -1; //50;
    long desiredIntervalMs = 1000 * 60;
    
    final XYSeries procStartSeries = new XYSeries("Processes started", false, true);
    final XYSeries workCompletedSeries = new XYSeries("Work completed", false, true);
    //Find first/end times.
    long firstMs = Math.min(resultLog.getFirstProcessStartTimestamp(),
                            resultLog.getFirstWorkCompletedTimestamp());
    long lastMs = Math.max(resultLog.getLastProcessStartTimestamp(),
                           resultLog.getLastWorkCompletedTimestamp());
    
    System.out.println("First: "+new Date(firstMs)+", last: "+new Date(lastMs));
    
    long durationMs = lastMs - firstMs;
    //Choose between interval based on time frame, or max data points
    long intervalMs = desiredIntervalMs;
    if (numDataPoints > 0)
    {
      intervalMs = durationMs / numDataPoints;
    }

    
    if (durationMs == 0 || intervalMs == 0)
    {
      throw new ProcessListException("Not enough data!");
    }
    
    /*
     * Query each data point required, starting a little before and ending a
     * little after the requested range.
     */
    for (long timestampMs = firstMs - intervalMs; 
        timestampMs <= lastMs + intervalMs; 
        timestampMs += intervalMs)
    {
      int procStartCount = resultLog.
          countResultsBetweenProcStartTimestamps(timestampMs, timestampMs+intervalMs);
      procStartSeries.add(timestampMs, procStartCount);
      //System.out.println("Querying interval: "+new Date(timestampMs) + " to "+new Date(timestampMs+intervalMs)+", count: "+procStartCount);
      
      int workCompletedCount = resultLog.
          countResultsBetweenWorkCompletedTimestamps(timestampMs, timestampMs+intervalMs);
      workCompletedSeries.add(timestampMs, workCompletedCount);
    }

    
    /*
     * Add all series to a single collection
     */
    final XYSeriesCollection dataset = new XYSeriesCollection();
    dataset.addSeries(procStartSeries);
    dataset.addSeries(workCompletedSeries);
    
    /*
     * Create the chart
     */
    double intervalMins = intervalMs / 1000d / 60d;
    final String title = "Message processing task start/completion rate";
    final String xAxisLabel = "Time";
    final String yAxisLabel = "Number of process starts / end events per "+intervalMins+" mins";

    final JFreeChart chart = ChartFactory.createXYStepChart(
        title,
        xAxisLabel, yAxisLabel,
        dataset,
        PlotOrientation.VERTICAL,
        true,   // legend
        true,   // tooltips
        false   // urls
    );
    return chart;
  }
  
  private void displayChart(JFreeChart chart)
  {
    // Display properties
    chart.setBackgroundPaint(new Color(216, 216, 216));
    final XYPlot plot = chart.getXYPlot();
    plot.getRenderer().setSeriesStroke(0, new BasicStroke(2.0f));
    plot.getRenderer().setSeriesStroke(1, new BasicStroke(2.0f));
    
    ChartPanel chartWrapper = new ChartPanel(chart);
    chartPanel.removeAll();
    chartPanel.add(chartWrapper, BorderLayout.CENTER);
  }

  
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel chartPanel;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JButton refreshButton;
    // End of variables declaration//GEN-END:variables
}

