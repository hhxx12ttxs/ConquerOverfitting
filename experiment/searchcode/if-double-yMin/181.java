/*
 * Created on 19-oct-2006
 *
 * gvSIG. Sistema de Informaci?n Geogr?fica de la Generalitat Valenciana
 *
 * Copyright (C) 2004 IVER T.I. and Generalitat Valenciana.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 *
 * For more information, contact:
 *
 *  Generalitat Valenciana
 *   Conselleria d'Infraestructures i Transport
 *   Av. Blasco Ib??ez, 50
 *   46010 VALENCIA
 *   SPAIN
 *
 *      +34 963862235
 *   gvsig@gva.es
 *      www.gvsig.gva.es
 *
 *    or
 *
 *   IVER T.I. S.A
 *   Salamanca 50
 *   46005 Valencia
 *   Spain
 *
 *   +34 963163400
 *   dac@iver.es
 */
/* CVS MESSAGES:
*
* $Id: RouteReportPanel.java 9061 2006-11-28 13:14:38Z  $
* $Log$
* Revision 1.14  2006-11-20 08:44:55  fjp
* borrar tramos amarillos, seleccionar solo campos num?ricos y situar las barreras encima del tramo invalidado
*
* Revision 1.13  2006/11/14 16:12:01  fjp
* *** empty log message ***
*
* Revision 1.12  2006/11/08 16:48:20  fjp
* *** empty log message ***
*
* Revision 1.11  2006/11/06 17:19:02  fjp
* Depurando el aspecto
*
* Revision 1.10  2006/11/06 13:21:38  fjp
* detalles
*
* Revision 1.9  2006/10/27 10:17:27  fjp
* Correcto. Falta ponerlo bonito.
*
* Revision 1.8  2006/10/26 17:47:14  fjp
* previo a formato
*
* Revision 1.7  2006/10/26 11:42:42  fjp
* Ya pita, ya.
*
* Revision 1.6  2006/10/25 15:51:20  fjp
* por terminar lo de los giros
*
* Revision 1.5  2006/10/25 14:47:34  fjp
* *** empty log message ***
*
* Revision 1.4  2006/10/24 18:42:05  azabala
* *** empty log message ***
*
* Revision 1.3  2006/10/23 18:51:42  azabala
* *** empty log message ***
*
* Revision 1.2  2006/10/20 19:54:01  azabala
* *** empty log message ***
*
* Revision 1.1  2006/10/19 19:09:43  azabala
* *** empty log message ***
*
*
*/
package com.iver.cit.gvsig.graph.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;



import com.hardcode.gdbms.engine.values.DoubleValue;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.v02.FSymbol;
import com.iver.cit.gvsig.fmap.layers.GraphicLayer;
import com.iver.cit.gvsig.fmap.rendering.FGraphic;
import com.iver.cit.gvsig.graph.core.TurnUtil;
import com.iver.cit.gvsig.graph.solvers.Route;

public class RouteReportPanel extends JPanel implements IWindow{
	
	private final String START_IMAGE = "<img src=\"images/drapeau_depart.gif\">";
	private final String STOP_IMAGE = "<img src=\"images/drapeau_arrivee.gif\">";
	private final String LEFT_IMAGE = "<img src=\"images/turn-left.png\">";
	private final String RIGHT_IMAGE = "<img src=\"images/turn-right.png\">";
	private final String STRAIGHT_IMAGE = "<img src=\"images/gtk-go-up.png\">";
	
	private final String LINE_SEPARATOR = "<hr width =\"70%\">";
	
	private Route route;
	
	private JScrollPane scrollPanel;
	private JEditorPane htmlPanel;
	private WindowInfo viewInfo;
	
	
	private String htmlText;
	private MapControl mapControl;
	
	//Para poder poner duraci?n, etc.
	private String weightText = "Longitud:";
	
	//Distancias y costes acumulados (entre dos cambios de calles)
	private double acumuledLenght = 0d;
	private double acumuledWeight = 0d;
	
	private double totalLenght = 0d;
	private double totalWeight = 0d;
	
	
	
	private int numberOfStreets = 1;//partimos de 1 porque consideramos la salida
	private ArrayList tramesOfSameStreet = new ArrayList();
	
	NumberFormat nf = NumberFormat.getInstance();
	
	
	public RouteReportPanel(Route route, MapControl mapControl){
		super();
		setLayout(new BorderLayout());
		this.route = route;
		this.mapControl = mapControl;
		scrollPanel = new JScrollPane();
		htmlPanel = new JEditorPane();
		htmlPanel.setEditable(false);
		htmlPanel.setEditorKit(new HTMLEditorKit());
		
		nf.setMaximumFractionDigits(2);
		
		final MapControl map = mapControl;
		final Route routeTemp = route;
		final MapControl mapControlTemp = mapControl;
		htmlPanel.addHyperlinkListener(new HyperlinkListener(){
			ArrayList previousSelection;
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					Rectangle2D bounds = null; 
					StringTokenizer st = new StringTokenizer(e.getDescription(),",");
					System.err.println("E.GETDESCRIPTION():" + e.getDescription());
					ArrayList features = new ArrayList();
				     while (st.hasMoreTokens()) {
				    	 int indexOfFeature = Integer.parseInt(st.nextToken());
				    	 IFeature feature = (IFeature) routeTemp.getFeatureList().get(indexOfFeature);
				    	 if(bounds == null)
				    		 bounds = feature.getGeometry().getBounds2D();
				    	 else
				    		 bounds.add(feature.getGeometry().getBounds2D());
				    	 features.add(feature);
				     }
					if(bounds != null){
						bounds = expand(bounds);
						GraphicLayer graphicLayer = mapControlTemp.getMapContext().getGraphicsLayer();
						if(previousSelection != null){
							for(int i = 0; i < previousSelection.size(); i++){
								graphicLayer.removeGraphic((FGraphic) previousSelection.get(i));
							}
						}
						previousSelection = new ArrayList();
						FSymbol lineSymbol = new FSymbol(FShape.LINE, Color.YELLOW);
						lineSymbol.setStroke(new BasicStroke(3.0f));
						int idSymbolLine = graphicLayer.addSymbol(lineSymbol);
						for(int i = 0; i < features.size(); i++ ){
							IGeometry gAux = ((IFeature)features.get(i)).getGeometry();
							FGraphic graphic = new FGraphic(gAux, idSymbolLine);
							graphic.setTag("ROUTE");
							graphicLayer.addGraphic( graphic);
							previousSelection.add(graphic);
						}
						mapControlTemp.drawGraphics();
						map.getMapContext().getViewPort().setExtent(bounds);
					}	
			      }
			}});
		initialize();
		scrollPanel.setViewportView(htmlPanel);
		add(scrollPanel, BorderLayout.CENTER);
	}
	
	
	private Rectangle2D expand(Rectangle2D rect){
		double xmin = rect.getMinX();
		double ymin = rect.getMinY();
		double width = rect.getWidth();
		double height = rect.getHeight();
		
		xmin -= width ;
		ymin -= height ;
		double newWidth = width * 2;
		double newHeight = height * 2 ;
		return new Rectangle2D.Double(xmin, ymin, newWidth, newHeight);
	}
	
	
	public void setRoute(Route route){
		this.route = route;
		initialize();
	}
	
	private void initialize(){
		htmlText = "<head>";
		htmlText += "<style type='text/css'>";
		htmlText += "<!-- ";
		htmlText += "  .normal { ";
		htmlText += "	font-family: Arial, Helvetica, sans-serif;	font-size: 10px; font-style: normal; color: #333333;";
		htmlText += "}";
		htmlText += "  a { ";
		htmlText += "	font-family: Arial, Helvetica, sans-serif;	font-size: 10px; font-style: italic; font-weight: bold;";
		htmlText += "}";
		htmlText += "  h1 { ";
		htmlText += "	font-family: Arial, Helvetica, sans-serif;	font-size: 14px;";
		htmlText += "}";		
		htmlText += "  .distancia { ";
		htmlText += "	font-family: Arial, Helvetica, sans-serif;	font-size: 10px; color: #666666;";
		htmlText += "}";
		htmlText += "  .resumen { ";
		htmlText += "	font-family: Arial, Helvetica, sans-serif;	font-size: 12px; color: #333333;";
		htmlText += "}";
		htmlText += "  .left { ";
		htmlText += "	font-weight: bold; color: #990000;";
		htmlText += "}";
		htmlText += "  .right { ";
		htmlText += "	font-weight: bold; color: #0033FF;";
		htmlText += "}";
		
		htmlText += " -->";
		htmlText += "</style>";
		htmlText += "</head>";
		htmlText += "<body>";
		ArrayList features = route.getFeatureList();
		
		//Route is ordered from the the start to the end
		IFeature firstFeature = (IFeature) features.get(0);
		IFeature lastFeature = (IFeature) features.get(features.size() - 1);
		
		renderHeader(firstFeature, lastFeature);
		renderFirstStrech(firstFeature);
		
		IFeature previousFeature = firstFeature;
		for (int i = 1; i < features.size() ; i++) {
			IFeature feature = (IFeature) features.get(i);
			renderStrech(feature , previousFeature, i);
			previousFeature = feature;
		}
		
		//TODO
		//Invertir el FIRST y el LAST
		//Borrar el graphics resaltado cuando se resalte otro
		renderLastStretch((IFeature)features.get(features.size() -1), previousFeature);
		htmlText += "</body>";
		System.out.println(htmlText);
		htmlPanel.setText(htmlText);
		
	}
	
	
	
	
	
	private void renderHeader(IFeature firstFeature, IFeature lastFeature){
		String startName = firstFeature.getAttribute(Route.TEXT_INDEX).toString();
		String stopName = lastFeature.getAttribute(Route.TEXT_INDEX).toString();
		htmlText += "<h1>";
		htmlText += "Informe de Ruta:" + startName + "-" + stopName ;//TODO INTERNACIONALIZAR ESTO
		htmlText += "</h1><br>";
		htmlText += "<span class='resumen'>Salida desde: <b>";//TODO INTERNAC
		htmlText += startName;
		htmlText += "</b><br>";
		htmlText += "Llegada a:<b> ";//TODO INTERNAC
		htmlText += stopName + "</b><br>";
		
		//TODO METER LA LONGITUD TOTAL DEL TRAYECTO AQUI
		htmlText += "Longitud total del trayecto: <b>" + nf.format(getLengthOfRoute()) + "</b></span>";
		htmlText += LINE_SEPARATOR;
	}
	
	private double getLengthOfRoute() {
		double solution = 0d;
		ArrayList featureList = route.getFeatureList();
		for(int i = 0; i < featureList.size(); i++){
			IFeature feature = (IFeature) featureList.get(i);
			solution += ((DoubleValue)feature.getAttribute(Route.LENGTH_INDEX)).getValue();
		}
		return solution;
	}


	private void renderFirstStrech(IFeature feature){
		htmlText += "<table>";
		htmlText += "<tr>";
		htmlText += "<td width='40'>";
		htmlText += START_IMAGE;
		htmlText += "</td>";
		htmlText += "<td class='normal'>";
		htmlText += "1. Salir de:<b> ";//TODO INTERNAC
		htmlText += feature.getAttribute(Route.TEXT_INDEX);
		htmlText += "</b></td></tr>";
		htmlText += "<tr>"; 
		htmlText += "<td width='40'></td><td><a href=\""+0+"\">Ver sobre el mapa</a><td></tr>";
		htmlText += "</table>";
		htmlText += LINE_SEPARATOR;
		
		double length = ((DoubleValue)feature.getAttribute(Route.LENGTH_INDEX)).getValue();
		double weight = ((DoubleValue)feature.getAttribute(Route.WEIGHT_INDEX)).getValue();
		acumuledLenght += length;
		acumuledWeight += weight;
		
		totalLenght += length;
		totalWeight += weight;
		
		tramesOfSameStreet.add(new Integer(0));
	}
	
	private void renderStrech(IFeature feature, IFeature previousFeature, int index){
		String street1 =  previousFeature.getAttribute(Route.TEXT_INDEX).toString();
		String street2 = feature.getAttribute(Route.TEXT_INDEX).toString();
		boolean changeStreet = ! street1.equalsIgnoreCase(street2);
		double length = ((DoubleValue)feature.getAttribute(Route.LENGTH_INDEX)).getValue();
		double weight = ((DoubleValue)feature.getAttribute(Route.WEIGHT_INDEX)).getValue();
		
		String textoTramo = null;
		String imageTurn = null;
		
		if(changeStreet){
			numberOfStreets++;
			String prefix = "Contin?e por <b>" + street1 + "</b> durante  "+
			nf.format(acumuledLenght)+" y ";
			int direction = TurnUtil.getDirection(previousFeature, feature);
			
			if(direction == TurnUtil.GO_STRAIGH_ON){
				textoTramo = prefix + " prosiga por <b>"+ street2 + "</b>";
				imageTurn = STRAIGHT_IMAGE;
				
			}else if(direction == TurnUtil.TURN_LEFT){
				textoTramo = prefix += " gire a la <span class='left'><b>izquierda</b></span> por <b>" + street2 + "</b>";
				imageTurn = LEFT_IMAGE;
				
			}else if(direction == TurnUtil.TURN_RIGHT){
				textoTramo = prefix += " gire a la <span class='right'><b>derecha</b></span> por <b>" + street2 + "</b>";
				imageTurn = RIGHT_IMAGE;	
			}
			htmlText += "<table>";
			htmlText += "<tr>";
			htmlText += "<td width='40'>";
			htmlText += imageTurn;
			htmlText += "</td>";
			htmlText += "<td class='normal'>";
			htmlText += numberOfStreets+" "+textoTramo;//TODO INTERNAC
			htmlText += "</td></tr>";
			htmlText += "<tr>";
			htmlText += "<td with='40'></td><td class='distancia'>Distancia acumulada:"+nf.format(totalLenght)+"</td></tr>";
			
			if(!weightText.equalsIgnoreCase("Longitud:"))
			{
				htmlText += "<tr>";
				htmlText += "<td with='40'></td><td class='distancia'>Coste:"+nf.format(totalWeight)+"</td></tr>";
			}
			
			String features = "";
			for(int i = 0; i < tramesOfSameStreet.size() -1; i++){
				int featureIndex = ((Integer) tramesOfSameStreet.get(i)).intValue();
				features += featureIndex + ",";
			}
			
			features += ((Integer)tramesOfSameStreet.get(tramesOfSameStreet.size()-1)).intValue();
//			System.out.println("features = " + features);
			htmlText += "<tr><td with='40'></td><td><a href=\""+features+"\">Ver sobre el mapa</a><td></tr>";
			htmlText += "</table>";
			htmlText += LINE_SEPARATOR;
			
			acumuledLenght = length;
			acumuledWeight = weight;
			tramesOfSameStreet.clear();
	
			
		}else{
			acumuledLenght += length;
			acumuledWeight += weight;
		}
		
		tramesOfSameStreet.add(new Integer(index));
		totalLenght += length;
		totalWeight += weight;
		
	}
	
	
	
	private void renderLastStretch(IFeature feature, IFeature previousFeature){
		
//		double length = ((DoubleValue)feature.getAttribute(Route.LENGTH_INDEX)).getValue();
//		double weight = ((DoubleValue)feature.getAttribute(Route.WEIGHT_INDEX)).getValue();
//		
//		
//		totalLenght += length;
//		totalWeight += weight;
		
		htmlText += "<table>";
		htmlText += "<tr>";
		htmlText += "<td width='40'>";
		htmlText += STOP_IMAGE;
		htmlText += "</td>";
		htmlText += "<td>";
		htmlText += numberOfStreets+". Llegada: ";//TODO INTERNAC
		htmlText += feature.getAttribute(Route.TEXT_INDEX);
		htmlText += "</td></tr>";
		htmlText += "<tr><td with='40'></td><td>Longitud:"+nf.format(totalLenght)+"</td></tr>";
		
		if(!weightText.equalsIgnoreCase("Longitud:"))
			htmlText += "<tr><td with='40'></td><td class='distancia'>Coste:"+totalWeight+"</td></tr>";
		htmlText += "<tr><td with='40'></td><td><a href=\""+(route.getFeatureList().size()-1)+"\">Ver sobre el mapa</a><td></tr>";
		htmlText += "</table>";
	}

	public WindowInfo getWindowInfo() {
		if (viewInfo == null) {
			viewInfo = new WindowInfo(WindowInfo.MODELESSDIALOG | 
										WindowInfo.RESIZABLE | 
										WindowInfo.MAXIMIZABLE |
										WindowInfo.ICONIFIABLE | 
										WindowInfo.PALETTE);
			viewInfo.setTitle("Informe de la ruta calculada");//Internacionalizar esto
			viewInfo.setWidth(400);
			viewInfo.setHeight(350);
		}
		return viewInfo;
	}
	
	
	
	
	
}


