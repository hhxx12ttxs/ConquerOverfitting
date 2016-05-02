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
package org.gvsig.symbology.gui.styling.editortools;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JToggleButton;

import org.gvsig.symbology.fmap.styles.SimpleLabelStyle;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.core.styles.ILabelStyle;
import com.iver.cit.gvsig.fmap.core.styles.IStyle;
import com.iver.cit.gvsig.gui.styling.EditorTool;
import com.iver.cit.gvsig.gui.styling.StyleEditor;
import com.iver.cit.gvsig.gui.styling.StylePreviewer;
/**
 *
 * SimpleLabelStylePanTool.java
 *
 *
 * @author jaume dominguez faus - jaume.dominguez@iver.es Jan 7, 2008
 *
 */
public class LabelStylePanTool extends EditorTool {
	private int NO_SELECTION_ON_STYLE = -1;
	private int TEXT_FIELD_SELECTED = 0;
	private int MARKER_POINT_SELECTED = 1;

	private JToggleButton btnPan;
	private Cursor cursor;
	private Point pIni, pEnd;
	private ILabelStyle style;
	private int toolSelected = NO_SELECTION_ON_STYLE;
	private int textFieldSelected = NO_SELECTION_ON_STYLE;

	private Dimension bounds;
	private int ownerHgap,ownerVgap;

	public LabelStylePanTool(JComponent owner) {
		super(owner);
	}

	private void getBounds() {

		bounds = ((StyleEditor) owner).getStylePreviewer().getSize();
		ownerHgap =  ((StyleEditor) owner).getStylePreviewer().getHGap();
		ownerVgap = ((StyleEditor) owner).getStylePreviewer().getVGap();

	}

	public Cursor getCursor() {
		if (cursor == null) {
			ImageIcon cursorImage = new ImageIcon(PluginServices.getIconTheme().get("hand-icon").getImage());
			cursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImage.getImage(),
					new Point(cursorImage.getIconWidth()/2,
							cursorImage.getIconHeight()/2), //16, 16), "");
							"");

		}
		return cursor;
	}

	public void mousePressed(MouseEvent e) {
		getBounds();
		Point2D marker = style.getMarkerPoint();
		pIni = new Point((int) (marker.getX()*(bounds.width-ownerHgap)),(int) (marker.getY()*(bounds.height-ownerVgap)));
		pEnd = e.getPoint();

		Point selectedPoint = new Point((int) (pEnd.getX()-ownerHgap),(int) (pEnd.getY()-ownerVgap));
		if((Math.abs(pIni.x - selectedPoint.x) < ownerHgap ) && (Math.abs(pIni.y - selectedPoint.y) < ownerVgap )){
			toolSelected = MARKER_POINT_SELECTED;
			return;
		}

		Point2D selPoint = screenPointToLabelPoint(pEnd);

		for (int i = style.getTextBounds().length - 1; i >= 0 ; i--) {
			Rectangle2D rectangle = style.getTextBounds()[i];
			if((selPoint.getX() >= rectangle.getMinX()) &&
					(selPoint.getX() <= rectangle.getMaxX()) &&
					(selPoint.getY() >= rectangle.getMinY())&&
					(selPoint.getY() <= rectangle.getMaxY())){
				toolSelected = TEXT_FIELD_SELECTED;
				textFieldSelected = i;
				pIni = pEnd;
				return;
			}
		}
	}

	public void mouseReleased(MouseEvent e) {

		toolSelected = NO_SELECTION_ON_STYLE;
		textFieldSelected = NO_SELECTION_ON_STYLE;

	}

	public void mouseDragged(MouseEvent e) {
		pEnd = e.getPoint();
		if((pEnd.getX() > bounds.width - ownerHgap) || ( pEnd.getY() > bounds.getHeight() - ownerVgap) ||
				(pEnd.getX() < ownerHgap) || ( pEnd.getY() < ownerVgap))
			return;

		Point2D labelEndPoint = screenPointToLabelPoint(pEnd);

		if(toolSelected == TEXT_FIELD_SELECTED){
			if(textFieldSelected != NO_SELECTION_ON_STYLE){

				Rectangle2D rectangle = style.getTextBounds()[textFieldSelected];

				Point2D labelIniPoint = screenPointToLabelPoint(pIni);
				Point maxPoint = new Point((int)bounds.getWidth(), (int)bounds.getHeight());
				Point2D labelMaxPoint = screenPointToLabelPoint(maxPoint);

				Point2D labelZero = screenPointToLabelPoint(new Point(0,0));
				double x = rectangle.getX() + labelEndPoint.getX()-labelIniPoint.getX();
				if (x<labelZero.getX()){ x=labelZero.getX(); }
				if ((x+rectangle.getWidth())>=labelMaxPoint.getX()) { x =  labelMaxPoint.getX()-rectangle.getWidth();}

				double y = rectangle.getY() + labelEndPoint.getY()-labelIniPoint.getY();
				if (y<labelZero.getY()){ y=labelZero.getY(); }
				if ((y+rectangle.getHeight())>=labelMaxPoint.getY()) { y =  labelMaxPoint.getY()-rectangle.getHeight();}

				rectangle.setRect(x,
						y,
						rectangle.getWidth(),
						rectangle.getHeight());
				style.setTextFieldArea(textFieldSelected,rectangle);
				pIni = pEnd;
			}
		}
		else if (toolSelected == MARKER_POINT_SELECTED){
			double xOffset = (pEnd.getX())/( bounds.getWidth()- ownerHgap );
			double yOffset = (pEnd.getY())/( bounds.getHeight()- ownerVgap );

			if(xOffset > 1) xOffset = 1;
			if(xOffset < 0) xOffset = 0;
			if(yOffset > 1) yOffset = 1;
			if(yOffset < 0) yOffset = 0;

			Point2D marker = style.getMarkerPoint();
			marker.setLocation(xOffset, yOffset);
		}
		owner.repaint();
	}

	private Point2D screenPointToLabelPoint(Point pIni) {
		//FIXME: Esto es un parche, habr?a que cambiar la API de los estilos y simbolos
		//pero mientras tanto
		int minx = pIni.x;
		int miny = pIni.y;

		StylePreviewer sp = ((StyleEditor) owner).getStylePreviewer();
		Dimension bounds = sp.getSize();

		IStyle style = sp.getStyle();
		Dimension backgroundBounds = null;
		if (style instanceof SimpleLabelStyle){
			backgroundBounds = ((SimpleLabelStyle)style).getSize();
		}
		Point2D p;
		if (backgroundBounds == null){
			p = new Point2D.Double(
					pIni.x/(bounds.getWidth()-sp.getHGap()/2),
					pIni.y/(bounds.getHeight()-sp.getVGap()/2));
		} else {
			double xOffset = 0;
			double yOffset = 0;
			double scale = 1;
			if (backgroundBounds.getWidth()>backgroundBounds.getHeight()) {
				scale = (bounds.getWidth()-sp.getHGap())/backgroundBounds.getWidth();
				yOffset = 0.5*(bounds.getHeight()-sp.getVGap() - backgroundBounds.getHeight()*scale);
			} else {
				scale = (bounds.getHeight()-sp.getVGap())/backgroundBounds.getHeight();
				xOffset = 0.5*(bounds.getWidth()-sp.getHGap() - backgroundBounds.getWidth()*scale);
			}
			p = new Point2D.Double(
					((minx-(sp.getHGap()/2)-xOffset)/scale)/backgroundBounds.getWidth(),
					((miny-(sp.getVGap()/2)-yOffset)/scale)/backgroundBounds.getHeight());
		}
		return p;
	}

	@Override
	public AbstractButton getButton() {
		return getBtnPan();
	}

	private JToggleButton getBtnPan() {
		if (btnPan == null) {
			btnPan = new JToggleButton(PluginServices.getIconTheme().get("hand-icon"));
			btnPan.setToolTipText(PluginServices.getText(this, "offset_label"));
			btnPan.setPreferredSize(EditorTool.SMALL_BTN_SIZE);
			btnPan.setSize(EditorTool.SMALL_BTN_SIZE);
		}

		return btnPan;
	}

	@Override
	public String getID() {
		return "1";
	}

	@Override
	public boolean isSuitableFor(Object obj) {
		return obj instanceof ILabelStyle;
	}

	@Override
	public void setModel(Object objectToBeEdited) {
		style = (ILabelStyle) objectToBeEdited;
	}
}

