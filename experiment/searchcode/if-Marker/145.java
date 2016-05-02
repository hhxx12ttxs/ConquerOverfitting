/* gvSIG. Sistema de Informaci?n Geogr?fica de la Generalitat Valenciana
 *
 * Copyright (C) 2005 IVER T.I. and Generalitat Valenciana.
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
 * $Id: MarkerFill.java 14735 2007-10-18 16:01:39Z jdominguez $
 * $Log$
 * Revision 1.20  2007-09-17 09:21:45  jaume
 * refactored SymboSelector (added support for multishapedsymbol)
 *
 * Revision 1.19  2007/08/10 07:28:25  jaume
 * translations
 *
 * Revision 1.18  2007/08/09 10:39:04  jaume
 * first round of found bugs fixed
 *
 * Revision 1.17  2007/08/08 11:49:15  jaume
 * refactored to avoid provide more than one EditorTool
 *
 * Revision 1.15  2007/08/03 11:29:13  jaume
 * refactored AbstractTypeSymbolEditorPanel class name to AbastractTypeSymbolEditor
 *
 * Revision 1.14  2007/08/03 09:20:46  jaume
 * refactored class names
 *
 * Revision 1.13  2007/08/01 13:02:08  jaume
 * renamed methods
 *
 * Revision 1.12  2007/07/30 12:56:04  jaume
 * organize imports, java 5 code downgraded to 1.4 and added PictureFillSymbol
 *
 * Revision 1.11  2007/06/29 13:07:33  jaume
 * +PictureLineSymbol
 *
 * Revision 1.10  2007/05/28 13:34:28  jaume
 * *** empty log message ***
 *
 * Revision 1.9  2007/05/08 15:44:07  jaume
 * *** empty log message ***
 *
 * Revision 1.8  2007/04/27 12:10:17  jaume
 * *** empty log message ***
 *
 * Revision 1.7  2007/04/05 16:08:34  jaume
 * Styled labeling stuff
 *
 * Revision 1.6  2007/04/04 16:01:14  jaume
 * *** empty log message ***
 *
 * Revision 1.5  2007/03/28 16:44:08  jaume
 * *** empty log message ***
 *
 * Revision 1.4  2007/03/13 16:57:35  jaume
 * Added MultiVariable legend
 *
 * Revision 1.3  2007/03/09 11:25:00  jaume
 * Advanced symbology (start committing)
 *
 * Revision 1.1.2.3  2007/02/21 07:35:14  jaume
 * *** empty log message ***
 *
 * Revision 1.1.2.2  2007/02/08 15:43:05  jaume
 * some bug fixes in the editor and removed unnecessary imports
 *
 * Revision 1.1.2.1  2007/01/26 13:49:03  jaume
 * *** empty log message ***
 *
 * Revision 1.1  2007/01/16 11:52:11  jaume
 * *** empty log message ***
 *
 * Revision 1.8  2007/01/10 17:05:05  jaume
 * moved to FMap and gvSIG
 *
 * Revision 1.7  2006/11/13 09:15:23  jaume
 * javadoc and some clean-up
 *
 * Revision 1.6  2006/11/06 16:06:52  jaume
 * *** empty log message ***
 *
 * Revision 1.5  2006/11/02 17:19:28  jaume
 * *** empty log message ***
 *
 * Revision 1.4  2006/10/31 16:16:34  jaume
 * *** empty log message ***
 *
 * Revision 1.3  2006/10/30 19:30:35  jaume
 * *** empty log message ***
 *
 * Revision 1.2  2006/10/29 23:53:49  jaume
 * *** empty log message ***
 *
 * Revision 1.1  2006/10/27 12:41:09  jaume
 * GUI
 *
 *
 */
package org.gvsig.symbology.gui.styling;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.gvsig.gui.beans.swing.GridBagLayoutPanel;
import org.gvsig.gui.beans.swing.JBlank;
import org.gvsig.gui.beans.swing.JButton;
import org.gvsig.gui.beans.swing.JIncrementalNumberField;
import org.gvsig.symbology.fmap.styles.SimpleMarkerFillPropertiesStyle;
import org.gvsig.symbology.fmap.symbols.MarkerFillSymbol;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.styles.IMarkerFillPropertiesStyle;
import com.iver.cit.gvsig.fmap.core.symbols.AbstractMarkerSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.ILineSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.IMarkerSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.IMultiLayerSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.ISymbol;
import com.iver.cit.gvsig.gui.panels.ColorChooserPanel;
import com.iver.cit.gvsig.gui.styling.AbstractTypeSymbolEditor;
import com.iver.cit.gvsig.gui.styling.EditorTool;
import com.iver.cit.gvsig.gui.styling.SymbolEditor;
import com.iver.cit.gvsig.gui.styling.SymbolSelector;
import com.iver.cit.gvsig.project.documents.view.legend.gui.ISymbolSelector;
import com.iver.cit.gvsig.project.documents.view.legend.gui.JSymbolPreviewButton;
/**
 * <b>MarkerFill</b> allows the user to store and modify the properties that fills a
 * polygon with a padding made of markers and an outline<p>
 * <p>
 * This functionality is carried out thanks to two tabs (marker fill and MarkerFillProperties)
 * which are included in the panel to edit the properities of a symbol (SymbolEditor)
 * how is explained in AbstractTypeSymbolEditor.<p>
 * <p>
 * The first tab (marker fill)permits the user to select the marker for the padding and
 * other options such as the color for the fill (<b>btnChooseMarker</b>),to select the
 * ouline (<b>btnOutline</b>)and the distribution (grid or random) of the marker inside
 * the padding (<b>rdGrid,rdRandom</b>).
 * <p>
 * The second tab is implementes as a MarkerFillProperties class and offers the possibilities
 * to change the separtion and the offset.
 *
 *
 *@see MarkerFillProperties
 *@see AbstractTypeSymbolEditor
 *@author jaume dominguez faus - jaume.dominguez@iver.es
 */
public class MarkerFill extends AbstractTypeSymbolEditor implements ActionListener,ChangeListener {
	private ArrayList<JPanel> tabs = new ArrayList<JPanel>();
	private ColorChooserPanel markerCC;
	private JButton btnChooseMarker;
	private MarkerFillProperties panelStyle = new MarkerFillProperties();
	private JRadioButton rdGrid;
	private JRadioButton rdRandom;
	private IMarkerSymbol marker = SymbologyFactory.createDefaultMarkerSymbol();

	private JIncrementalNumberField txtOutlineWidth;
	private JSymbolPreviewButton btnOutline;
	private JSlider sldOutlineTransparency;
	private int outlineAlpha = 255;
	private ILineSymbol outline;
	private JCheckBox useBorder;

	/**
	 * constructor method
	 * @param owner
	 */
	public MarkerFill(SymbolEditor owner) {
		super(owner);
		initialize();
	}

	/**
	 * Initializes the parameters that allows the user to fill the padding of
	 * a polygon with a style made of markers.To do it, two tabs are created (marker
	 * fill and MarkerFillProperties)inside the SymbolEditor panel with default values
	 * for the different attributes.
	 */
	private void initialize() {
//		GridLayout layout;
		JPanel myTab;
		// Marker fill tab

		{
			myTab = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
			myTab.setName(PluginServices.getText(this, "marker_fill"));

			GridBagLayoutPanel p = new GridBagLayoutPanel();
			JPanel aux = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 5));
			markerCC = new ColorChooserPanel(true);
			markerCC.setAlpha(255);
			markerCC.addActionListener(this);
			aux.add(markerCC);

			p.addComponent(PluginServices.getText(this, "color")+":", aux);
			btnChooseMarker = new JButton(PluginServices.getText(this, "choose_marker"));
			btnChooseMarker.addActionListener(this);
			aux = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 5));
			aux.add(btnChooseMarker);
			p.addComponent("", aux);

			ButtonGroup group = new ButtonGroup();
			rdGrid = new JRadioButton(PluginServices.getText(this, "grid"));
			rdGrid.addActionListener(this);
			rdRandom = new JRadioButton(PluginServices.getText(this, "random"));
			rdRandom.addActionListener(this);
			group.add(rdGrid);
			group.add(rdRandom);

			aux = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 5));
			aux.add(rdGrid);
			aux.add(rdRandom);
			rdGrid.setSelected(true);
			p.addComponent("", aux);


			JPanel myTab2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5,5));
			GridBagLayoutPanel aux3 = new GridBagLayoutPanel();

			JPanel aux2 = new JPanel();
			btnOutline = new JSymbolPreviewButton(FShape.LINE);
			btnOutline.setPreferredSize(new Dimension(100, 35));
			aux2.add(btnOutline);

			aux3.addComponent(new JBlank(10, 10));
			useBorder = new JCheckBox(PluginServices.getText(this, "use_outline"));
			aux3.addComponent(useBorder, aux2);
			aux3.addComponent(new JBlank(10, 10));

			sldOutlineTransparency = new JSlider();
			sldOutlineTransparency.setValue(100);
			aux3.addComponent(PluginServices.getText(this, "outline")+":",
					aux2);
			aux3.addComponent(PluginServices.getText(this, "outline_opacity")+":", sldOutlineTransparency);
			txtOutlineWidth = new JIncrementalNumberField("", 25, 0, Double.MAX_VALUE, 1);
			aux3.addComponent(PluginServices.getText(this, "outline_width")+":", txtOutlineWidth);
			myTab2.add(aux3);

			p.addComponent("", myTab2);
			myTab.add(p);

			useBorder.addActionListener(this);
			btnOutline.addActionListener(this);
			txtOutlineWidth.addActionListener(this);
			sldOutlineTransparency.addChangeListener(this);

		}
		tabs.add(myTab);

		// Fill properties tab
		tabs.add(panelStyle);
		panelStyle.addActionListener(this);
	}

	public void refreshControls(ISymbol layer) {
		if (layer == null) {
			System.err.println(getClass().getName()+":: should be unreachable code");
			// set defaults
			markerCC.setColor(Color.BLACK);
			rdGrid.setSelected(true);
			rdRandom.setSelected(false);
		} else {

			MarkerFillSymbol mfs = (MarkerFillSymbol) layer;
			int fillStyle = mfs.getMarkerFillProperties().getFillStyle();
			marker = mfs.getMarker();
			rdGrid.setSelected(fillStyle == SimpleMarkerFillPropertiesStyle.GRID_FILL);
			rdRandom.setSelected(fillStyle == SimpleMarkerFillPropertiesStyle.RANDOM_FILL);
			panelStyle.setModel(mfs.getMarkerFillProperties());
			markerCC.setColor(marker.getColor());

			//outline
			sldOutlineTransparency.removeChangeListener(this);

			outline=mfs.getOutline();
			btnOutline.setSymbol(outline);
			useBorder.setSelected(mfs.hasOutline());

			if (outline != null) {
				outlineAlpha = outline.getAlpha();
				sldOutlineTransparency.setValue((int)((outlineAlpha/255D)*100));
				txtOutlineWidth.setDouble(outline.getLineWidth());
			} else {
				sldOutlineTransparency.setValue(100);
			}

			sldOutlineTransparency.addChangeListener(this);



		}
	}

	public String getName() {
		return PluginServices.getText(this, "marker_fill_symbol");
	}

	public JPanel[] getTabs() {
		return (JPanel[]) tabs.toArray(new JPanel[0]);
	}

	public void actionPerformed(ActionEvent e) {

		JComponent comp = (JComponent) e.getSource();
		if (comp.equals(btnChooseMarker)) {
			ISymbolSelector symSelect = SymbolSelector.createSymbolSelector(marker, FShape.POINT);
			PluginServices.getMDIManager().addWindow(symSelect);
			marker = (AbstractMarkerSymbol) symSelect.getSelectedObject();

			if (marker == null) return;

		}

		if (!(marker instanceof IMultiLayerSymbol)) {
			marker.setColor(markerCC.getColor());
		}

		if (comp.equals(btnOutline)) {
			ISymbol sym = btnOutline.getSymbol();
			if (sym instanceof ILineSymbol) {
				ILineSymbol outline = (ILineSymbol) sym;
				if (outline != null)
					txtOutlineWidth.setDouble(outline.getLineWidth());
				}

		}

		fireSymbolChangedEvent();
	}

	public Class getSymbolClass() {
		return MarkerFillSymbol.class;
	}

	public ISymbol getLayer() {
		MarkerFillSymbol mfs = new MarkerFillSymbol();
		IMarkerFillPropertiesStyle prop = panelStyle.getMarkerFillProperties();
		prop.setFillStyle(rdGrid.isSelected() ?
				IMarkerFillPropertiesStyle.GRID_FILL : IMarkerFillPropertiesStyle.RANDOM_FILL);

		IMarkerSymbol myMarker = (IMarkerSymbol ) SymbologyFactory.createSymbolFromXML(marker.getXMLEntity(), "theMarker");

		mfs.setMarker(myMarker);
		mfs.setMarkerFillProperties(prop);

		mfs.setHasOutline(useBorder.isSelected());
		outline = (ILineSymbol) btnOutline.getSymbol();

		if (outline!=null) {
			outline.setLineWidth(txtOutlineWidth.getDouble());
			outline.setAlpha(outlineAlpha);
		}

		mfs.setOutline(outline);

		return mfs;
	}

	public EditorTool[] getEditorTools() {
		return null;
	}

	public void stateChanged(ChangeEvent e) {
		Object s = e.getSource();

		if (s.equals(sldOutlineTransparency)) {
			outlineAlpha = (int) (255*(sldOutlineTransparency.getValue()/100.0));
		}

		outline = (ILineSymbol) btnOutline.getSymbol();
		fireSymbolChangedEvent();

	}
}

