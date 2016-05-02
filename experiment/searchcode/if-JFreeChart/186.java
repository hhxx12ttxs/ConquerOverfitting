/*
 * BEGIN_HEADER - DO NOT EDIT
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://open-esb.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://open-esb.dev.java.net/public/CDDLv1.0.html.
 * If applicable add the following below this CDDL HEADER,
 * with the fields enclosed by brackets "[]" replaced with
 * your own identifying information: Portions Copyright
 * [year] [name of copyright owner]
 */

/*
 * @(#)ChartListener.java
 * Copyright 2004-2007 Sun Microsystems, Inc. All Rights Reserved.
 *
 * END_HEADER - DO NOT EDIT
 */
package org.openesb.tools.extchart.jsf;

import org.openesb.tools.extchart.jfchart.ChartCreator;
import java.awt.image.BufferedImage;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
//import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Logger;


public class ChartListener implements PhaseListener {
        private static Logger mLogger = Logger.getLogger("org.openesb.tools.extchart.jsf.ChartListener");
	public final static String CHART_REQUEST = "chartcreatorrequest";
        public final static String CHART_ID_HOLDER = "chartid";

	public void afterPhase(PhaseEvent phaseEvent) {
                mLogger.info("After phase is called ");
		String rootId = phaseEvent.getFacesContext().getViewRoot().getViewId();
                mLogger.info("root id is " + rootId);
		if (rootId.indexOf(CHART_REQUEST) != -1) {
			handleChartRequest(phaseEvent);
		}
	}
	
	private void handleChartRequest(PhaseEvent phaseEvent) {
		FacesContext facesContext = phaseEvent.getFacesContext();
		ExternalContext externalContext = facesContext.getExternalContext();
		Map requestMap = externalContext.getRequestParameterMap();		
		Map sessionMap = externalContext.getSessionMap();
		String id = (String)requestMap.get("id");
                BufferedImage cImage = (BufferedImage) sessionMap.get(id);
		mLogger.info("handle request is called for id" + id);
                ChartCreator creator = new ChartCreator();
		try {
			if(externalContext.getResponse() instanceof HttpServletResponse) {
                            creator.writeBufferedChartToOutputStream(cImage,(HttpServletResponse)externalContext.getResponse());
				//writeChartWithServletResponse((HttpServletResponse)externalContext.getResponse(),chart, chartData);
                        } else { //if(externalContext.getResponse() instanceof RenderResponse)
				//writeChartWithPortletResponse((RenderResponse)externalContext.getResponse(), chart, chartData);
                        }
                        } catch (Exception e) {
			System.err.println(e.toString());
		} finally {
			emptySession(sessionMap, id);
			facesContext.responseComplete();
		}
	}

	public void beforePhase(PhaseEvent phaseEvent) {

	}

	public PhaseId getPhaseId() {
		return PhaseId.RESTORE_VIEW;
	}
	
	/*private void writeChartWithServletResponse(HttpServletResponse response, JFreeChart chart, ChartData chartData) throws IOException{
		OutputStream stream = response.getOutputStream();
		response.setContentType(ChartUtils.resolveContentType(chartData.getOutput()));
		writeChart(stream, chart, chartData);
	}
	
	private void writeChartWithPortletResponse(RenderResponse response, JFreeChart chart, ChartData chartData) throws IOException{
		OutputStream stream = response.getPortletOutputStream();
		response.setContentType(ChartUtils.resolveContentType(chartData.getOutput()));
		writeChart(stream, chart, chartData);
	}
	
	private void writeChart(OutputStream stream, JFreeChart chart, ChartData chartData) throws IOException{
		if(chartData.getOutput().equalsIgnoreCase("png"))
			ChartUtilities.writeChartAsPNG(stream, chart, chartData.getWidth(), chartData.getHeight());
		else if (chartData.getOutput().equalsIgnoreCase("jpeg"))
			ChartUtilities.writeChartAsJPEG(stream, chart, chartData.getWidth(), chartData.getHeight());
		
		stream.flush();
		stream.close();
	}
	*/
	private void emptySession(Map sessionMap, String id) {
		sessionMap.remove(id);
	}
}

