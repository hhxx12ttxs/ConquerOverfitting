package com.oc.struts2;

import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.StrutsResultSupport;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import com.opensymphony.xwork2.ActionInvocation;
import com.oc.common.util.Assertion;

/**
 * 
 * @author nathanleewei
 * 
 */
public class JFreeChartResult extends StrutsResultSupport {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5673368580957455217L;

	private JFreeChart chart = null;

	private int height = 600;
	private int width = 800;

	protected void doExecute(String finalLocation, ActionInvocation invocation) throws Exception {

		if (null == chart) {
			chart = (JFreeChart) invocation.getStack().findValue("chart");
		}

		Assertion.notNull(chart, "chart not found");

		HttpServletResponse response = ServletActionContext.getResponse();
		OutputStream os = response.getOutputStream();
		ChartUtilities.writeChartAsPNG(os, chart, width, height);
		os.flush();
		// os.close();
	}

	/**
	 * @return the chart
	 */
	public JFreeChart getChart() {
		return chart;
	}

	/**
	 * @param chart
	 *            the chart to set
	 */
	public void setChart(JFreeChart chart) {
		this.chart = chart;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @param height
	 *            the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width
	 *            the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

}

