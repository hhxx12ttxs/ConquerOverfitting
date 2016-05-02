/* 
 * Copyright (c) 2014, Universitat Pompeu Fabra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright 
 *   notice, this list of conditions and the following disclaimer in the 
 *   documentation and/or other materials provided with the distribution.
 * - Neither the name of the Universitat Pompeu Fabra nor the names 
 *   of its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED 
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY 
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * @author Pancraรง Villalonga <panviflo@gmail.com>
 * @date   January 30, 2014 
 */

package com.edu.los.dumper.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToolBar;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.edu.los.dumper.JDConstants;
import com.edu.los.dumper.statistics.continuous.JDPacketContStatisticsTaker;
import com.edu.los.service.module.base.data.BaseData;

@SuppressWarnings("serial")
public class JDContinuousStatFrame extends JDStatFrame implements ActionListener
{
	JFreeChart chart;
	ChartPanel chartPanel;
	JCheckBoxMenuItem plotCheckBoxPopup[];
	
	XYPlot plot;
	JDPacketContStatisticsTaker staker;
	long initialSec = 0, lastSec=0;
    XYSeriesCollection dataset;
	
	public static JDContinuousStatFrame openWindow(List<BaseData> packets, JDPacketContStatisticsTaker staker) {
		JDContinuousStatFrame frame = new JDContinuousStatFrame(packets, staker);
		frame.setVisible(true);
		return frame;
	}
	
	JDContinuousStatFrame(List<BaseData> packets, JDPacketContStatisticsTaker staker) {
		super(staker.getName());
		this.staker = staker;
		dataset = new XYSeriesCollection();
				
		// Create Chart
		chart = createChart();
		chartPanel = new ChartPanel(chart);
	    chartPanel.setDisplayToolTips( true );
		chartPanel.setMouseWheelEnabled(true);
		
		// Plot Menu
		JMenu plotMenu = new JMenu("Plots");
		plotCheckBoxPopup = new JCheckBoxMenuItem[staker.getLabels().length];
		for(int i = 0; i < plotCheckBoxPopup.length; i++ ) {
			plotCheckBoxPopup[i] = new JCheckBoxMenuItem(staker.getLabels()[i], true);
			plotCheckBoxPopup[i].setActionCommand("Series" + i);
			plotCheckBoxPopup[i].addActionListener(this);
			plotMenu.add(plotCheckBoxPopup[i]);
		}
		menuBar.add(plotMenu);
		
		// Axis Trace Menu
		JMenu traceMenu = new JMenu("Traces");
		ButtonGroup traceGroup = new ButtonGroup();
		JRadioButtonMenuItem traceDisableMenu = new JRadioButtonMenuItem("Disable");
		traceDisableMenu.setActionCommand("Disable");
		traceDisableMenu.addActionListener(this);
		traceDisableMenu.setSelected(true);
		traceGroup.add(traceDisableMenu);
		traceMenu.add(traceDisableMenu);
		JRadioButtonMenuItem traceEnableMenu = new JRadioButtonMenuItem("Enable");
		traceEnableMenu.setActionCommand("Enable");
		traceEnableMenu.addActionListener(this);
		traceGroup.add(traceEnableMenu);
		traceMenu.add(traceEnableMenu);
		menuBar.add(traceMenu);
				
		// Create Toolbar
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		
		JButton autoZoomMenu = new JButton(new ImageIcon(
				this.getClass().getResource(JDConstants.ICON_PATH + "auto_zoom.png")));	
		autoZoomMenu.setActionCommand("AutoZoom");
		autoZoomMenu.setToolTipText("Auto Zoom");
		autoZoomMenu.addActionListener(this);
		toolbar.add(autoZoomMenu);
		
		JButton zoomInMenu = new JButton(new ImageIcon(
				this.getClass().getResource(JDConstants.ICON_PATH + "zoom_in.png")));	
		zoomInMenu.setActionCommand("ZoomIn");
		zoomInMenu.setToolTipText("Zoom In");
		zoomInMenu.addActionListener(this);
		toolbar.add(zoomInMenu);
		
		JButton zoomOutMenu = new JButton(new ImageIcon(
				this.getClass().getResource(JDConstants.ICON_PATH + "zoom_out.png")));	
		zoomOutMenu.setActionCommand("ZoomOut");
		zoomOutMenu.setToolTipText("Zoom Out");
		zoomOutMenu.addActionListener(this);
		toolbar.add(zoomOutMenu);
		
		for (int i = 0; i < staker.getLabels().length; i++) {
	    	XYSeries data= new XYSeries(staker.getLabels()[i]);
	    	dataset.addSeries(data);
	    	
	    	//XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
	    	//renderer.setSeriesShape(0, ShapeUtilities.createDiamond(3));
	    	//renderer.setSeriesShapesVisible(0, true);	
	    	//renderer.setBaseToolTipGenerator(new XYToolTipGenerator() { 
			//	public String generateToolTip(XYDataset dataset, int series, int item) { 
			//		//double x = dataset.getXValue(series, item); 
			//		double y = dataset.getYValue(series, item); 
			//		return (String.valueOf(y) + "mV");					
			//	} 
		    //});
			//this.plot.setRenderer(i, renderer);
			
	    	this.plot.setRenderer(i, new StandardXYItemRenderer());    	
	    }	
	    
		getContentPane().add(chartPanel, BorderLayout.CENTER);
		getContentPane().add(toolbar, BorderLayout.NORTH);
        setSize(500, 400);
        
        // Add existing packets to the XYplot.
        if(!packets.isEmpty())
        {
        	Iterator<BaseData> iter = packets.iterator();
        	while (iter.hasNext()) {
        		this.addPacket(iter.next());
        	}	
        }
    }
	
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() instanceof JButton || evt.getSource() instanceof JMenuItem) {
			
			String cmd = evt.getActionCommand();

			if (cmd.equals("Disable")) {
                chartPanel.setHorizontalAxisTrace(false);
                chartPanel.setVerticalAxisTrace(false);
                chartPanel.repaint();
			} 
			else if (cmd.equals("Enable")) {
                chartPanel.setHorizontalAxisTrace(true);
                chartPanel.setVerticalAxisTrace(true);
                chartPanel.repaint();				
			}			
			else if (cmd.startsWith("Series")) {
				int series = Integer.parseInt(cmd.substring(6));
				if (plotCheckBoxPopup[series].isSelected()) {
					plot.getRenderer().setSeriesVisible(series, true);
				}
				else {
					plot.getRenderer().setSeriesVisible(series, false);
				}
			} 
			else if (cmd.equals("AutoZoom")) {
				chartPanel.restoreAutoBounds();
				chartPanel.repaint();				
			}
			else if (cmd.equals("ZoomIn")) {
				chartPanel.zoomInBoth(2, 2);
			}
			else if (cmd.equals("ZoomOut")) {
				chartPanel.zoomOutBoth(2, 2);
			}
		}	
	}
	
	public void addPacket(BaseData p) {
		if (initialSec == 0) {
			initialSec = new Date().getTime();
		}
		long timeOfPacket = new Date().getTime() - initialSec;

		if (lastSec != timeOfPacket) {
			for (int i = 0; i < dataset.getSeriesCount(); i++) {
				// Plot last second values
				dataset.getSeries(i).addOrUpdate(lastSec, staker.getValues(0)[i]); 
			}
		}
		lastSec = timeOfPacket;
		
		// Update Average
		staker.addPacket(p);		
	}
	
	public void clear() {
		lastSec = 0;
		initialSec = 0;
		for (int i = 0; i < dataset.getSeriesCount(); i++) {
			dataset.getSeries(i).clear();
		}
	}

	void fireUpdate() {

	}
	
	private JFreeChart createChart() {
        final JFreeChart result = ChartFactory.createScatterPlot(
            "",
            staker.getTypes()[0],
            staker.getTypes()[1],
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );
        plot = result.getXYPlot();
         
        ValueAxis domain = plot.getDomainAxis();
        domain.setAutoRange(true);
        domain.setVerticalTickLabels(false);
        ValueAxis range = plot.getRangeAxis();
        range.setAutoRange(true);       
       
        return result;
    }	
}

