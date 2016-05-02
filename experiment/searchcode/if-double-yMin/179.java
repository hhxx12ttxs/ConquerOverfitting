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
 * $Id: RouteReportPanel.java 34304 2010-12-22 11:00:05Z fpenarrubia $
 * $Log$
 * Revision 1.15  2007-09-07 11:29:47  fjp
 * Casi compila. Falta arreglar lo de FArrowSymbol y retocar el graphiclist de FMap.
 *
 * Revision 1.14  2006/11/20 08:44:55  fjp
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
package org.gvsig.graph.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.awt.print.PrinterException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.cresques.cts.IProjection;
import org.gvsig.graph.core.DocumentRenderer;
import org.gvsig.graph.core.TurnUtil;
import org.gvsig.graph.solvers.Route;
import org.gvsig.graph.solvers.RouteMemoryDriver;
import org.gvsig.gui.beans.swing.JButton;

import com.hardcode.gdbms.engine.values.DoubleValue;
import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.addlayer.AddLayerDialog;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.symbols.ILineSymbol;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.GraphicLayer;
import com.iver.cit.gvsig.fmap.layers.LayerFactory;
import com.iver.cit.gvsig.fmap.rendering.FGraphic;

public class RouteReportPanel extends JPanel implements IWindow {

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

	private StringBuilder htmlText;
	private MapControl mapControl;

	// Para poder poner duraci?n, etc.
	private String weightText = "Longitud:";

	// Distancias y costes acumulados (entre dos cambios de calles)
	private double acumuledLenght = 0d;
	private double acumuledWeight = 0d;

	private double totalLenght = 0d;
	private double totalWeight = 0d;

	private int numberOfStreets = 1;// partimos de 1 porque consideramos la
									// salida
	private ArrayList tramesOfSameStreet = new ArrayList();

	NumberFormat nf = NumberFormat.getInstance();

	public RouteReportPanel(Route route, MapControl mapControl) {
		super();
		setLayout(new BorderLayout());
		this.route = route;
		this.mapControl = mapControl;
		scrollPanel = new JScrollPane();
		htmlPanel = new JEditorPane();
		htmlPanel.setEditable(false);
		HTMLEditorKit kit = new HTMLEditorKit();
		// {
		// public ViewFactory getViewFactory()
		// {
		// return new HTMLFactory()
		// {
		// public View create(Element elem)
		// {
		// View view = super.create(elem);
		//
		// if (view instanceof ImageView)
		// {
		// ((javax.swing.text.html.ImageView) view).setLoadsSynchronously(true);
		// }
		// return view;
		// }
		// };
		// }
		// };
		htmlPanel.setEditorKit(kit);

		nf.setMaximumFractionDigits(2);

		final MapControl map = mapControl;
		final Route routeTemp = route;
		final MapControl mapControlTemp = mapControl;
		htmlPanel.addHyperlinkListener(new HyperlinkListener() {
			ArrayList previousSelection;

			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					Rectangle2D bounds = null;
					StringTokenizer st = new StringTokenizer(
							e.getDescription(), ",");
					System.err.println("E.GETDESCRIPTION():"
							+ e.getDescription());
					ArrayList features = new ArrayList();
					while (st.hasMoreTokens()) {
						int indexOfFeature = Integer.parseInt(st.nextToken());
						IFeature feature = (IFeature) routeTemp
								.getFeatureList().get(indexOfFeature);
						if (bounds == null)
							bounds = feature.getGeometry().getBounds2D();
						else
							bounds.add(feature.getGeometry().getBounds2D());
						features.add(feature);
					}
					if (bounds != null) {
						bounds = expand(bounds);
						GraphicLayer graphicLayer = mapControlTemp
								.getMapContext().getGraphicsLayer();
						if (previousSelection != null) {
							for (int i = 0; i < previousSelection.size(); i++) {
								graphicLayer
										.removeGraphic((FGraphic) previousSelection
												.get(i));
							}
						}
						previousSelection = new ArrayList();

						ILineSymbol lineSymbol = SymbologyFactory
								.createDefaultLineSymbol();
						lineSymbol.setLineColor(Color.YELLOW);
						lineSymbol.setLineWidth(3.0f);
						int idSymbolLine = graphicLayer.addSymbol(lineSymbol);
						for (int i = 0; i < features.size(); i++) {
							IGeometry gAux = ((IFeature) features.get(i))
									.getGeometry();
							FGraphic graphic = new FGraphic(gAux, idSymbolLine);
							graphic.setTag("ROUTE");
							graphicLayer.addGraphic(graphic);
							previousSelection.add(graphic);
						}
						mapControlTemp.drawGraphics();
						map.getMapContext().getViewPort().setExtent(bounds);
					}
				}
			}
		});
		initialize();
		scrollPanel.setViewportView(htmlPanel);
		add(scrollPanel, BorderLayout.CENTER);
		JPanel south = new JPanel();
		JButton btnPrint = new JButton(_T("Print"));
		btnPrint.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				printReport();
			}

		});
		JButton btnExport = new JButton(_T("Export"));
		btnExport.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				exportRoute();
			}

		});
		
		south.add(btnPrint);
		south.add(btnExport);
		add(south, BorderLayout.SOUTH);
	}

	protected void exportRoute() {
		RouteMemoryDriver driver = new RouteMemoryDriver(route.getFeatureList());
		IProjection projection = AddLayerDialog.getLastProjection();
		FLyrVect routeLayer = (FLyrVect) LayerFactory.createLayer("Route",
				driver, projection);

		FormatSelectionPanel selectionPanel = new FormatSelectionPanel(PluginServices.getText(null,
		"Seleccione_un_formato_para_guardar_la_ruta"));
		PluginServices.getMDIManager().addWindow(selectionPanel);

		String format = selectionPanel.getSelectedFormat();
		com.iver.cit.gvsig.ExportTo export = new com.iver.cit.gvsig.ExportTo();
		MapContext context = mapControl.getMapContext();
			if (format.equalsIgnoreCase("SHP")) {
				export.saveToShp(context, routeLayer);
			} else if (format.equalsIgnoreCase("DXF")) {
				export.saveToDxf(context, routeLayer);
//			} else if (format.equalsIgnoreCase("GML")) {
//				export.saveToGml(context, routeLayer);
			} else if (format.equalsIgnoreCase("POSTGIS")) {
				export.saveToPostGIS(context, routeLayer);
			}

		
	}

	/**
	 * Prints report (html)
	 */
	protected void printReport() {
		try {
			// FJP: Esto ser?a lo ideal, pero solo funciona con el jre 1.6
			// htmlPanel.print();
			
			// Usamos una clase externa por compatibilidad con jre1.5, pero tiene menos calidad la salida.
			DocumentRenderer docRenderer = new DocumentRenderer();
			docRenderer.print(htmlPanel);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog((Component) PluginServices.getMDIManager().getActiveWindow(),
					e.getLocalizedMessage());
		}
	}

	private Rectangle2D expand(Rectangle2D rect) {
		double xmin = rect.getMinX();
		double ymin = rect.getMinY();
		double width = rect.getWidth();
		double height = rect.getHeight();

		xmin -= width;
		ymin -= height;
		double newWidth = width * 2;
		double newHeight = height * 2;
		return new Rectangle2D.Double(xmin, ymin, newWidth, newHeight);
	}

	public void setRoute(Route route) {
		this.route = route;
		initialize();
	}

	private void initialize() {
		htmlText = new StringBuilder("<head>");
		htmlText.append("<style type='text/css'>");
		htmlText.append("<!-- ");
		htmlText.append("  .normal { ");
		htmlText
				.append("	font-family: Arial, Helvetica, sans-serif;	font-size: 9px; font-style: normal; color: #333333;");
		htmlText.append("}");
		htmlText.append("  a { ");
		htmlText
				.append("	font-family: Arial, Helvetica, sans-serif;	font-size: 9px; font-style: italic; font-weight: bold;");
		htmlText.append("}");
		htmlText.append("  h1 { ");
		htmlText
				.append("	font-family: Arial, Helvetica, sans-serif;	font-size: 12px;");
		htmlText.append("}");
		htmlText.append("  .distancia { ");
		htmlText
				.append("	font-family: Arial, Helvetica, sans-serif;	font-size: 9px; color: #666666;");
		htmlText.append("}");
		htmlText.append("  .resumen { ");
		htmlText
				.append("	font-family: Arial, Helvetica, sans-serif;	font-size: 10px; color: #333333;");
		htmlText.append("}");
		htmlText.append("  .left { ");
		htmlText.append("	font-weight: bold; color: #990000;");
		htmlText.append("}");
		htmlText.append("  .right { ");
		htmlText.append("	font-weight: bold; color: #0033FF;");
		htmlText.append("}");

		htmlText.append(" -->");
		htmlText.append("</style>");
		htmlText.append("</head>");
		htmlText.append("<body>");
		ArrayList features = route.getFeatureList();

		// Route is ordered from the the start to the end
		IFeature firstFeature = (IFeature) features.get(0);
		IFeature lastFeature = (IFeature) features.get(features.size() - 1);

		renderHeader(firstFeature, lastFeature);
		renderFirstStrech(firstFeature);

		IFeature previousFeature = firstFeature;
		for (int i = 1; i < features.size(); i++) {
			IFeature feature = (IFeature) features.get(i);
			renderStrech(feature, previousFeature, i);
			previousFeature = feature;
		}

		// TODO
		// Invertir el FIRST y el LAST
		// Borrar el graphics resaltado cuando se resalte otro
		renderLastStretch((IFeature) features.get(features.size() - 1),
				previousFeature);
		htmlText.append("</body>");
		// System.out.println(htmlText);
		htmlPanel.setText(htmlText.toString());

	}

	private void renderHeader(IFeature firstFeature, IFeature lastFeature) {
		String startName = firstFeature.getAttribute(Route.TEXT_INDEX)
				.toString();
		String stopName = lastFeature.getAttribute(Route.TEXT_INDEX).toString();
		htmlText.append("<h1>");
		htmlText.append(_T("Route_report") + ":" + startName + "-" + stopName);
		htmlText.append("</h1><br>");
		htmlText.append("<span class='resumen'>" + _T("Start_from") + ": <b>");
		htmlText.append(startName);
		htmlText.append("</b><br>");
		htmlText.append(_T("Arrival_to") + ":<b> ");
		htmlText.append(stopName + "</b><br>");

		htmlText.append(_T("Total_length") + ": <b>"
				+ nf.format(getLengthOfRoute()) + "</b></span>");
		htmlText.append(LINE_SEPARATOR);
	}

	private double getLengthOfRoute() {
		double solution = 0d;
		ArrayList featureList = route.getFeatureList();
		for (int i = 0; i < featureList.size(); i++) {
			IFeature feature = (IFeature) featureList.get(i);
			solution += ((DoubleValue) feature.getAttribute(Route.LENGTH_INDEX))
					.getValue();
		}
		return solution;
	}

	private void renderFirstStrech(IFeature feature) {
		htmlText.append("<table>");
		htmlText.append("<tr>");
		htmlText.append("<td width='40'>");
		htmlText.append(START_IMAGE);
		htmlText.append("</td>");
		htmlText.append("<td class='normal'>");
		htmlText.append("1. " + _T("Start_from") + ":<b> ");
		htmlText.append(feature.getAttribute(Route.TEXT_INDEX));
		htmlText.append("</b></td></tr>");
		htmlText.append("<tr>");
		htmlText.append("<td width='40'></td><td><a href=\"" + 0 + "\">"
				+ _T("Show_in_map") + "</a><td></tr>");
		htmlText.append("</table>");
		htmlText.append(LINE_SEPARATOR);

		double length = ((DoubleValue) feature.getAttribute(Route.LENGTH_INDEX))
				.getValue();
		double weight = ((DoubleValue) feature.getAttribute(Route.WEIGHT_INDEX))
				.getValue();
		acumuledLenght += length;
		acumuledWeight += weight;

		totalLenght += length;
		totalWeight += weight;

		tramesOfSameStreet.add(new Integer(0));
	}

	private String _T(String str) {
		return PluginServices.getText(this, str);
	}

	private void renderStrech(IFeature feature, IFeature previousFeature,
			int index) {
		String street1 = previousFeature.getAttribute(Route.TEXT_INDEX)
				.toString();
		String street2 = feature.getAttribute(Route.TEXT_INDEX).toString();
		boolean changeStreet = !street1.equalsIgnoreCase(street2);
		double length = ((DoubleValue) feature.getAttribute(Route.LENGTH_INDEX))
				.getValue();
		double weight = ((DoubleValue) feature.getAttribute(Route.WEIGHT_INDEX))
				.getValue();

		String textoTramo = null;
		String imageTurn = null;

		if (changeStreet) {
			numberOfStreets++;
			String prefix = _T("follow") + " <b>" + street1 + "</b> "
					+ _T("during") + " " + nf.format(acumuledLenght) + " "
					+ _T("and");
			int direction = TurnUtil.getDirection(previousFeature, feature);

			if (direction == TurnUtil.GO_STRAIGH_ON) {
				textoTramo = prefix + " " + _T("continue_by") + " <b> "
						+ street2 + "</b>";
				imageTurn = STRAIGHT_IMAGE;

			} else if (direction == TurnUtil.TURN_LEFT) {
				textoTramo = prefix + " " + _T("turn") + " "
						+ "<span class='left'><b> " + _T("left")
						+ "</b></span> " + _T("by_turn") + " <b>" + street2
						+ "</b> ";
				imageTurn = LEFT_IMAGE;

			} else if (direction == TurnUtil.TURN_RIGHT) {
				textoTramo = prefix + " " + _T("turn")
						+ " <span class='right'><b> " + _T("right")
						+ " </b></span> " + _T("by_turn") + " <b>" + street2
						+ "</b>";
				imageTurn = RIGHT_IMAGE;
			}
			htmlText.append("<table>");
			htmlText.append("<tr>");
			htmlText.append("<td width='40'>");
			htmlText.append(imageTurn);
			htmlText.append("</td>");
			htmlText.append("<td class='normal'>");
			htmlText.append(numberOfStreets + " " + textoTramo);// TODO INTERNAC
			htmlText.append("</td></tr>");
			htmlText.append("<tr>");
			htmlText.append("<td with='40'></td><td class='distancia'>"
					+ _T("Accumulated_distance") + ":" + nf.format(totalLenght)
					+ "</td></tr>");

			if (!weightText.equalsIgnoreCase("Longitud:")) {
				htmlText.append("<tr>");
				htmlText.append("<td with='40'></td><td class='distancia'>"
						+ _T("cost") + ":" + nf.format(totalWeight)
						+ "</td></tr>");
			}

			String features = "";
			for (int i = 0; i < tramesOfSameStreet.size() - 1; i++) {
				int featureIndex = ((Integer) tramesOfSameStreet.get(i))
						.intValue();
				features += featureIndex + ",";
			}

			features += ((Integer) tramesOfSameStreet.get(tramesOfSameStreet
					.size() - 1)).intValue();
			// System.out.println("features = " + features);
			htmlText.append("<tr><td with='40'></td><td><a href=\"" + features
					+ "\">" + _T("Show_in_map") + "</a><td></tr>");
			htmlText.append("</table>");
			htmlText.append(LINE_SEPARATOR);

			acumuledLenght = length;
			acumuledWeight = weight;
			tramesOfSameStreet.clear();

		} else {
			acumuledLenght += length;
			acumuledWeight += weight;
		}

		tramesOfSameStreet.add(new Integer(index));
		totalLenght += length;
		totalWeight += weight;

	}

	private void renderLastStretch(IFeature feature, IFeature previousFeature) {

		// double length =
		// ((DoubleValue)feature.getAttribute(Route.LENGTH_INDEX)).getValue();
		// double weight =
		// ((DoubleValue)feature.getAttribute(Route.WEIGHT_INDEX)).getValue();
		//		
		//		
		// totalLenght += length;
		// totalWeight += weight;

		htmlText.append("<table>");
		htmlText.append("<tr>");
		htmlText.append("<td width='40'>");
		htmlText.append(STOP_IMAGE);
		htmlText.append("</td>");
		htmlText.append("<td class='resumen'>");
		htmlText.append(numberOfStreets + ". " + _T("Arrival_to") + ": ");
		htmlText.append(feature.getAttribute(Route.TEXT_INDEX));
		htmlText.append("</td></tr>");
		htmlText.append("<tr><td with='40'></td><td class='resumen'>"
				+ _T("Accumulated_distance") + ":" + nf.format(totalLenght)
				+ "</td></tr>");

		if (!weightText.equalsIgnoreCase("Longitud:"))
			htmlText.append("<tr><td with='40'></td><td class='resumen'>Coste:"
					+ totalWeight + "</td></tr>");
		htmlText.append("<tr><td with='40'></td><td><a href=\""
				+ (route.getFeatureList().size() - 1) + "\">"
				+ _T("Show_in_map") + "</a><td></tr>");
		htmlText.append("</table>");
	}

	public WindowInfo getWindowInfo() {
		if (viewInfo == null) {
			viewInfo = new WindowInfo(WindowInfo.MODELESSDIALOG
					| WindowInfo.RESIZABLE | WindowInfo.MAXIMIZABLE
					| WindowInfo.ICONIFIABLE | WindowInfo.PALETTE);
			viewInfo.setTitle(_T("route_report_title"));// Internacionalizar
														// esto
			viewInfo.setWidth(400);
			viewInfo.setHeight(350);
		}
		return viewInfo;
	}

	public Object getWindowProfile() {
		return WindowInfo.TOOL_PROFILE;
	}

}

