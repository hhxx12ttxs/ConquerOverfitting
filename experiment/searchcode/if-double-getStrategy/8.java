
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

package com.iver.cit.gvsig.fmap.layers;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.print.attribute.PrintRequestAttributeSet;

import org.cresques.cts.ICoordTrans;
import org.cresques.cts.IProjection;
import org.gvsig.tools.file.PathGenerator;

import com.hardcode.driverManager.Driver;
import com.hardcode.driverManager.DriverLoadException;
import com.hardcode.gdbms.driver.exceptions.InitializeDriverException;
import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.data.driver.DriverException;
import com.hardcode.gdbms.engine.instruction.FieldNotFoundException;
import com.hardcode.gdbms.engine.values.NullValue;
import com.hardcode.gdbms.engine.values.NumericValue;
import com.hardcode.gdbms.engine.values.StringValue;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.andami.messages.NotificationManager;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.exceptions.layers.CancelEditingLayerException;
import com.iver.cit.gvsig.exceptions.layers.LegendLayerException;
import com.iver.cit.gvsig.exceptions.layers.StartEditionLayerException;
import com.iver.cit.gvsig.exceptions.visitors.StartWriterVisitorException;
import com.iver.cit.gvsig.exceptions.visitors.VisitorException;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.fmap.core.FPoint2D;
import com.iver.cit.gvsig.fmap.core.GeneralPathX;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.symbols.IMarkerSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.ISymbol;
import com.iver.cit.gvsig.fmap.core.symbols.ITextSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.SimpleTextSymbol;
import com.iver.cit.gvsig.fmap.crs.CRSFactory;
import com.iver.cit.gvsig.fmap.drivers.BoundedShapes;
import com.iver.cit.gvsig.fmap.drivers.VectorialFileDriver;
import com.iver.cit.gvsig.fmap.edition.Annotation_EditableAdapter;
import com.iver.cit.gvsig.fmap.edition.EditionEvent;
import com.iver.cit.gvsig.fmap.edition.VectorialEditableAdapter;
import com.iver.cit.gvsig.fmap.operation.strategies.Annotation_Strategy;
import com.iver.cit.gvsig.fmap.operations.strategies.Strategy;
import com.iver.cit.gvsig.fmap.operations.strategies.StrategyManager;
import com.iver.cit.gvsig.fmap.rendering.Annotation_Legend;
import com.iver.cit.gvsig.fmap.rendering.IVectorLegend;
import com.iver.cit.gvsig.fmap.spatialindex.QuadtreeJts;
import com.iver.utiles.XMLEntity;
import com.iver.utiles.swing.threads.Cancellable;


/**
 * Annotation's layer.
 *
 * @author Vicente Caballero Navarro
 */
public class Annotation_Layer extends FLyrVect {
	private Annotation_Mapping mapping = null;
	private int indexEditing = -1;
	private Annotation_Legend aLegend;
	private Strategy strategy = null;
	private IMarkerSymbol symbolPoint = SymbologyFactory.createDefaultMarkerSymbol();
	private final static AffineTransform ati=new AffineTransform();
	/**
	 * Crea un nuevo FLyrAnnotation.
	 */
	public Annotation_Layer() {
		super();
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param mapping DOCUMENT ME!
	 * @throws ReadDriverException
	 * @throws LegendLayerException
	 * @throws DriverException
	 * @throws FieldNotFoundException
	 */
	public void setMapping(Annotation_Mapping mapping) throws LegendLayerException, ReadDriverException {
		this.mapping = mapping;
		aLegend = new Annotation_Legend();
		setLegend();
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public Annotation_Mapping getAnnotatonMapping() {
		return mapping;
	}

	/**
	 * @throws ReadDriverException
	 * @throws InitializeDriverException
	 * @see com.iver.cit.gvsig.fmap.layers.LayerOperations#draw(java.awt.image.BufferedImage,
	 *      java.awt.Graphics2D, ISymbol)
	 */
	public void draw(BufferedImage image, Graphics2D g, ViewPort viewPort,
			Cancellable cancel, double scale) throws InitializeDriverException, ReadDriverException {
		if (isWithinScale(scale)) {
			// Las que solo tienen etiquetado sin pintar el shape,
			// no pasamos por ellas
			boolean bDrawShapes = true;

			if (bDrawShapes) {
				if (strategy == null) {
					strategy = new Annotation_Strategy(this);
				}
				g.setColor(Color.black);
				ReadableVectorial adapter = getSource();
				adapter.start();
				drawAnnotations(image,g,viewPort,cancel,null);
				adapter.stop();
			}

			if (getVirtualLayers() != null) {
				getVirtualLayers().draw(image, g, viewPort, cancel, scale);
			}
		}
	}
	private void drawAnnotations(BufferedImage image, Graphics2D g, ViewPort viewPort,
			Cancellable cancel,PrintRequestAttributeSet properties) {
		BufferedImage bi=new BufferedImage(viewPort.getImageWidth(),viewPort.getImageHeight(),BufferedImage.TYPE_INT_ARGB);
		Graphics2D gBi=(Graphics2D)bi.getGraphics();
		Point2D offset=viewPort.getOffset();
		gBi.translate(-offset.getX(),-offset.getY());
		Annotation_Legend l = (Annotation_Legend) getLegend();
		ITextSymbol sym = (ITextSymbol) l.getDefaultSymbol();

		try {
			ReadableVectorial source = getSource();
			// limit the labeling to the visible extent
			FBitSet bs = queryByRect(viewPort.getAdjustedExtent());

			SelectableDataSource recordSet = source.getRecordset();
			recordSet.start();
			FBitSet bitSet = recordSet.getSelection();
			Annotation_Mapping mapping = getAnnotatonMapping();
			int idHeightField = mapping.getColumnHeight();
			int idFontName = mapping.getColumnTypeFont();
			int idFontStyle = mapping.getColumnStyleFont();
			int idRotationField = mapping.getColumnRotate();
			int idFontColor = mapping.getColumnColor();
			int idTextField = mapping.getColumnText();

			double rotation = 0D;
			float size = sym.getFont().getSize();

			String fontName = "Dialog";
			int fontStyle = sym.getFont().getStyle();
			int fontColor = sym.getTextColor().getRGB();
			long t1 = System.currentTimeMillis();

			for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
				if (cancel.isCanceled()) {
					break;
				}

				Value[] vv = recordSet.getRow(i);

				if (idHeightField != -1) {
					// text size is defined in the table
					try {
						size = (float)(((NumericValue) vv[idHeightField]).doubleValue());
					} catch (ClassCastException ccEx) {
						if (!NullValue.class.equals(
								vv[idHeightField].getClass())) {
						}
						continue;
					}
				}

				if (idFontName != -1) {
					fontName = ((StringValue) vv[idFontName]).toString();
				}

				if (idFontStyle != -1) {
					fontStyle = ((NumericValue) vv[idFontStyle]).intValue();
				}

				if (idRotationField != -1) {
					// text rotation is defined in the table
					rotation = ((NumericValue) vv[idRotationField]).doubleValue();
					((SimpleTextSymbol)sym).setRotation(Math.toRadians(rotation));
				}

				if (idFontColor != -1) {
					// text rotation is defined in the table
					fontColor = ((NumericValue) vv[idFontColor]).intValue();
					sym.setTextColor(new Color(fontColor));
				}

				if (bitSet.get(i)) {
					sym = (ITextSymbol) sym.getSymbolForSelection();
				}

				sym.setFont(new Font(fontName, fontStyle, (int) size));
				source.start();
				IGeometry geom = source.getShape(i);
				source.stop();
				ICoordTrans ct = getCoordTrans();

				boolean bMustClone = false;

				if (geom == null) {
					return;
				}

				if (ct != null) {
					if (bMustClone) {
						geom = geom.cloneGeometry();
					}

					geom.reProject(ct);
				}
				geom.transform(viewPort.getAffineTransform());
				Shape shape=geom.getInternalShape();
				FPoint2D fpPixels=null;
				if (!(shape instanceof FPoint2D)) {
					Rectangle2D rP=shape.getBounds2D();
					fpPixels=new FPoint2D(rP.getX(),rP.getY());
				}else {
					fpPixels=(FPoint2D)shape;
				}
				int unit=-1;

				unit=l.getUnits();
				boolean draw=false;
				Rectangle2D r=getTextWrappingGeometryInPixels(unit,size,vv[idTextField].toString(),rotation,fontName, fontStyle,i,viewPort,geom).getBounds2D();
				Rectangle2D rPixels=new Rectangle2D.Double(r.getMinX(),r.getMinY(),r.getWidth(),r.getHeight());

				Point2D p=new Point2D.Double(fpPixels.getX(),fpPixels.getY());


				if (!isEditing() && l.isAvoidOverLapping()){
					p=getPoint(bi,rPixels,l.isDelOverLapping(), offset);
					if (p!=null){
						draw=true;
					}
				}else{
					if (!isEditing() && l.isDelOverLapping()){
						if (isOverlapping(bi,rPixels, offset)){
							draw=true;
						}
					}else{
						draw=true;
					}
				}
				if (l.isPointVisible()) {
					symbolPoint.draw(gBi,
							ati,
							fpPixels, cancel);
				}

				if (draw){
					//Si el tama?o de la fuente est? en unidades de mapa.
					if (unit!=-1) {
						((SimpleTextSymbol)l.getDefaultSymbol()).setFontSize(getTextHeightInPixels(unit, size, viewPort));
					}
					//Como ya hemos cambiado el tama?o a la fuente adapt?ndolo a p?xels pasamos units = -1 ?
					drawAnnotation(gBi,-1,this,l, p, vv[idTextField].toString(),viewPort,properties);
				}
			}
			gBi.translate(offset.getX(),offset.getY());
			g.drawImage(bi,(int)offset.getX(),(int)offset.getY(),null);
			System.err.println(System.currentTimeMillis()-t1+"millis");
			recordSet.stop();
		} catch (Exception e) {
			NotificationManager.addError(e); // TODO esto no puede estar aqu?, pertenece a una capa de abtracci?n superior
		}
	}
	private Point2D getPoint(BufferedImage bi, Rectangle2D rec, boolean b, Point2D offset) {
		Rectangle2D r=new Rectangle2D.Double(rec.getMinX(),rec.getMaxY(),rec.getWidth(),rec.getHeight());
		if (isOverlapping(bi,r, offset)){
			return new Point2D.Double(r.getX(),r.getY());
		}
		r.setFrame(rec.getMinX(),rec.getMinY(),rec.getWidth(),rec.getHeight());
		if (isOverlapping(bi,r, offset)){
			return new Point2D.Double(r.getX(),r.getY());
		}
		r.setFrame(rec.getMinX()-rec.getWidth(),rec.getMaxY(),rec.getWidth(),rec.getHeight());
		if (isOverlapping(bi,r, offset)){
			return new Point2D.Double(r.getX(),r.getY());
		}
		r.setFrame(rec.getMinX()-rec.getWidth(),rec.getMinY(),rec.getWidth(),rec.getHeight());
		if (isOverlapping(bi,r, offset)){
			return new Point2D.Double(r.getX(),r.getY());
		}
		r.setFrame(rec.getMinX()-rec.getWidth()/2,rec.getMaxY(),rec.getWidth(),rec.getHeight());
		if (isOverlapping(bi,r, offset)){
			return new Point2D.Double(r.getX(),r.getY());
		}
		r.setFrame(rec.getMinX()-rec.getWidth()/2,rec.getMinY(),rec.getWidth(),rec.getHeight());
		if (isOverlapping(bi,r, offset)){
			return new Point2D.Double(r.getX(),r.getY());
		}
		r.setFrame(rec.getMinX(),(rec.getMaxY()+rec.getMinY())/2,rec.getWidth(),rec.getHeight());
		if (isOverlapping(bi,r, offset)){
			return new Point2D.Double(r.getX(),r.getY());
		}
		if (!b){
			return new Point2D.Double(rec.getMinX(),rec.getMaxY());
		} else {
			return null;
		}
	}

	private boolean isOverlapping(BufferedImage bi, Rectangle2D rPixels, Point2D offset) {
		for (int i=(int)(rPixels.getMinX()-offset.getX());i<rPixels.getMaxX()-offset.getX();i++){
			for (int j=(int)(rPixels.getMinY()-offset.getY());j<rPixels.getMaxY()-offset.getY();j++){
				if (i<0 || j<0 || bi.getWidth()<i+1 || bi.getHeight()<j+1)
					continue;
				if (bi.getRGB(i,j)!=0){
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * @throws ReadDriverException
	 * @throws InitializeDriverException
	 * @throws ExpansionFileReadException
	 * @see com.iver.cit.gvsig.fmap.layers.LayerOperations#getFullExtent()
	 */
	public Rectangle2D getFullExtent() throws InitializeDriverException,
	ReadDriverException, ExpansionFileReadException {
		Rectangle2D rAux;
		getSource().start();
		rAux = getSource().getFullExtent();
		getSource().stop();

		// Si existe reproyecci?n, reproyectar el extent
		ICoordTrans ct = getCoordTrans();

		if (ct != null) {
			Point2D pt1 = new Point2D.Double(rAux.getMinX(), rAux.getMinY());
			Point2D pt2 = new Point2D.Double(rAux.getMaxX(), rAux.getMaxY());
			pt1 = ct.convert(pt1, null);
			pt2 = ct.convert(pt2, null);
			rAux = new Rectangle2D.Double();
			rAux.setFrameFromDiagonal(pt1, pt2);
		}

		return rAux;
	}

	/**
	 * @throws ReadDriverException
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#print(java.awt.Graphics2D,
	 *      com.iver.cit.gvsig.fmap.ViewPort,
	 *      com.iver.utiles.swing.threads.Cancellable)
	 */
	public void print(Graphics2D g, ViewPort viewPort, Cancellable cancel,
			double scale, PrintRequestAttributeSet properties) throws ReadDriverException{
		if (isVisible() && isWithinScale(scale)) {
			drawAnnotations(null,g, viewPort, cancel, properties);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.iver.cit.gvsig.fmap.layers.layerOperations.RandomVectorialData#queryByRect(java.awt.geom.Rectangle2D)
	 */
	public FBitSet queryByRect(Rectangle2D rect) throws ReadDriverException, VisitorException {
		Strategy s = StrategyManager.getStrategy(this);

		return s.queryByRect(rect);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param p DOCUMENT ME!
	 * @param tolerance DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 * @throws VisitorException
	 * @throws ReadDriverException
	 *
	 * @throws DriverException DOCUMENT ME!
	 */
	public FBitSet queryByPoint(Point2D p, double tolerance) throws ReadDriverException, VisitorException {
		Strategy s = StrategyManager.getStrategy(this);

		return s.queryByPoint(p, tolerance);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param g DOCUMENT ME!
	 * @param relationship DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 * @throws VisitorException
	 * @throws ReadDriverException
	 *
	 * @throws DriverException DOCUMENT ME!
	 * @throws VisitException DOCUMENT ME!
	 */
	public FBitSet queryByShape(IGeometry g, int relationship) throws ReadDriverException, VisitorException {
		Strategy s = StrategyManager.getStrategy(this);

		return s.queryByShape(g, relationship);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 *
	 * @throws XMLException
	 *
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#getProperties()
	 */
	public XMLEntity getXMLEntity() throws XMLException {
		XMLEntity xml = super.getXMLEntity();
		return xml;
	}

	/**
	 * @see com.iver.cit.gvsig.fmap.layers.FLyrDefault#setXMLEntity(com.iver.utiles.XMLEntity)
	 */
	public void setXMLEntity(XMLEntity xml) throws XMLException {
		IProjection proj = null;

		if (xml.contains("proj")) {
			proj = CRSFactory.getCRS(xml.getStringProperty("proj"));
		}

		if (xml.contains("file")) {
			Driver d;

			try {
				d = LayerFactory.getDM().getDriver(xml.getStringProperty(
						"driverName"));
			} catch (DriverLoadException e1) {
				throw new XMLException(e1);
			}

			FLyrVect lv = (FLyrVect) LayerFactory.createLayer(xml.getName(),
					(VectorialFileDriver) d,
					new File(PathGenerator.getInstance().getAbsolutePath(xml.getStringProperty("file"))), proj);

			try {
				this.setSource(lv.getSource());
				this.setRecordset(lv.getRecordset());
				this.setProjection(lv.getProjection());
				this.setLegend((IVectorLegend) lv.getLegend());


				Annotation_Mapping.addAnnotationMapping(this);
			} catch (ReadDriverException e) {
				throw new XMLException(e);
			} catch (LegendLayerException e) {
				throw new XMLException(e);
			}
		}
		super.setXMLEntity(xml);
//		if (this.getLabelingStrategy()==null){
//			boolean isInPixel=((Annotation_Legend)this.getLegend()).isFontSizeInPixels();
//			if (isInPixel){
//				AttrInTableLabelingStrategy labeling = new AttrInTableLabelingStrategy();
//				labeling.setUnit(-1);
//				setLabelingStrategy(labeling);
//			}else{
//				AttrInTableLabelingStrategy labeling = new AttrInTableLabelingStrategy();
//				labeling.setUnit(1);
//				setLabelingStrategy(labeling);
//			}
//		}
	}

	public Value getSymbolKey(int i) throws ReadDriverException {
		SelectableDataSource ds = getRecordset();
		String t = new String();
		Value val = ds.getFieldValue(i, mapping.getColumnText());
		t = val.toString();

		if (mapping.getColumnColor() != -1) {
			Value valColor = ds.getFieldValue(i, mapping.getColumnColor());
			t = t.concat(valColor.toString());
		}

		if (mapping.getColumnTypeFont() != -1) {
			Value valTypeFont = ds.getFieldValue(i, mapping.getColumnTypeFont());
			t = t.concat(valTypeFont.toString());
		}

		if (mapping.getColumnStyleFont() != -1) {
			Value valStyleFont = ds.getFieldValue(i,
					mapping.getColumnStyleFont());
			t = t.concat(valStyleFont.toString());
		}

		Value total = ValueFactory.createValue(t);

		return total;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.iver.cit.gvsig.fmap.layers.layerOperations.RandomVectorialData#createIndex()
	 */
	public void createSpatialIndex()  {
		// FJP: ESTO HABR? QUE CAMBIARLO. PARA LAS CAPAS SECUENCIALES, TENDREMOS
		// QUE ACCEDER CON UN WHILE NEXT. (O mejorar lo de los FeatureVisitor
		// para que acepten recorrer sin geometria, solo con rectangulos.
		// AZABALA: Como no tengo claro de donde se crean las capas de textos
		// el ?ndice espacial creado seguir? siendo el Quadtree en memoria
		// de JTS (QuadtreeJts es un adapter a nuestra api de indices)
		spatialIndex = new QuadtreeJts();

		ReadableVectorial va = getSource();
		ICoordTrans ct = getCoordTrans();
		BoundedShapes shapeBounds = (BoundedShapes) va.getDriver();

		try {
			va.start();

			for (int i = 0; i < va.getShapeCount(); i++) {
				Rectangle2D r = null;
				IGeometry geom = va.getShape(i);
				if (geom != null) {
					r = geom.getBounds2D();
				} else {
					r = shapeBounds.getShapeBounds(i);
				}

				// TODO: MIRAR COMO SE TRAGAR?A ESTO LO DE LAS REPROYECCIONES
				if (ct != null) {
					r = ct.convert(r);
				}

				if (r != null) {
					spatialIndex.insert(r, i);
				}
			} // for

			va.stop();
		} catch (ExpansionFileReadException e) {
			NotificationManager.addError(this.getName(),e);
		} catch (ReadDriverException e) {
			NotificationManager.addError(this.getName(),e);
		}
	}

	/**
	 * DOCUMENT ME!
	 * @throws ReadDriverException
	 *
	 * @throws DriverException DOCUMENT ME!
	 */
	public void setSelectedEditing() throws ReadDriverException {
		FBitSet bitSet = getRecordset().getSelection();

		if (bitSet.cardinality() == 0) {
			return;
		}

		indexEditing = bitSet.nextSetBit(0);
	}

	public void setInEdition(int i) {
		indexEditing = i;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public int getInEdition() {
		return indexEditing;
	}

	protected void setLegend() throws ReadDriverException, LegendLayerException {
		getSource().getRecordset().start();
		if (mapping.getColumnText()!=-1) {
			try {
				aLegend.setFieldName(getSource().getRecordset().getFieldName(mapping.getColumnText()));
				setLegend(aLegend);
				getSource().getRecordset().stop();
				return;
			}
			catch (IndexOutOfBoundsException ex) {
				// if mapping is -1, don't apply the legend
			}
		}
		throw new ReadDriverException(getSource().getDriver().getName(), new RuntimeException("This does not seem an annotation layer"));
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public Strategy getStrategy() {
		return strategy;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param b DOCUMENT ME!
	 * @throws StartEditionLayerException
	 *
	 * @throws EditionException DOCUMENT ME!
	 */
	public void setEditing(boolean b) throws StartEditionLayerException {
		super.setEditing(b);
		try {
			if (b) {
				Annotation_EditableAdapter aea = new Annotation_EditableAdapter(this);

				aea.setOriginalVectorialAdapter(((VectorialEditableAdapter) super.getSource()).getOriginalAdapter());

				((VectorialEditableAdapter) super.getSource()).cancelEdition(EditionEvent.GRAPHIC);
				aea.start();

				aea.setCoordTrans(getCoordTrans());
				// CHEMA
				aea.startEdition(EditionEvent.GRAPHIC);
				setSource(aea);
				getRecordset().setSelectionSupport(aea.getOriginalAdapter()
						.getRecordset()
						.getSelectionSupport());
				aea.addEditionListener(this);
			} else {
			}

		} catch (ReadDriverException e) {
			throw new StartEditionLayerException(this.getName(),e);
		} catch (CancelEditingLayerException e) {
			throw new StartEditionLayerException(this.getName(),e);
		} catch (StartWriterVisitorException e) {
			throw new StartEditionLayerException(this.getName(),e);
		}
		deleteSpatialIndex();
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param layer DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 * @throws ReadDriverException
	 *
	 * @throws DriverException DOCUMENT ME!
	 * @throws FieldNotFoundException DOCUMENT ME!
	 */
	public static Annotation_Layer createLayerFromVect(FLyrVect layer) throws ReadDriverException {
		Annotation_Layer la = new Annotation_Layer();
		FLyrVect lv=(FLyrVect)LayerFactory.createLayer(layer.getName(),layer.getSource().getDriver(),layer.getProjection());
		la.setSource(lv.getSource());
		la.setRecordset(lv.getRecordset());
		la.setProjection(layer.getProjection());
		la.getRecordset().setSelection(layer.getRecordset().getSelection());

		return la;
	}

	/**
	 * DOCUMENT ME!
	 */
	public void removingThisLayer() {
//		super.removingThisLayer();
		spatialIndex = null;
		aLegend = null;
		strategy = null;
		System.gc();
	}

	public double getTextHeightInPixels(int unit, double height,ViewPort vp){
		if (unit!=-1) {
			double[] distanceTrans2Meter = MapContext.getDistanceTrans2Meter();
			height = vp.fromMapDistance((int) (height)*distanceTrans2Meter[unit]/
					distanceTrans2Meter[vp.getMapUnits()]);
		}
		return height;
	}
	public IGeometry getTextWrappingGeometryInPixels(int unit,double height, String description,
			double rotation, String type, int style, int numReg,ViewPort vp, IGeometry geom) {

		Shape shapeP=geom.getInternalShape();
		FPoint2D p = null;
		if (!(shapeP instanceof FPoint2D)) {
			Rectangle2D rP=shapeP.getBounds2D();
			p=new FPoint2D(rP.getX(),rP.getY());
		}else {
			p=(FPoint2D)shapeP;
		}

		height = getTextHeightInPixels(unit, height, vp);

		AffineTransform tx = null;
		/* Para tama?os de fuente de letras excesivamente grandes obtenemos
		 * shapes con todas las coordenadas a 0, por eso limitamos el tama?o
		 * a 1000 y despu?s reescalamos el bounds.
		 */
		double scale = 1;
		if (height > 1000){
			scale = height/1000;
			height = 1000;
		}
		Font font = new Font(type, style,
				(int) (height));
		FontRenderContext frc = new FontRenderContext(tx,
				false, true);
		

		GlyphVector gv = font.createGlyphVector(frc, description);
		Shape shape = gv.getOutline();

		Rectangle2D rGeom=shape.getBounds2D();

		IGeometry geomResult = ShapeFactory.createPolygon2D(new GeneralPathX(rGeom));

		tx = AffineTransform.getTranslateInstance(0, -rGeom.getY());

		if (rotation != 0) {
			tx.preConcatenate(AffineTransform.getRotateInstance(Math.toRadians(rotation)));
		}

		if(scale != 1){
			tx.preConcatenate(AffineTransform.getScaleInstance(scale, scale));
		}
		
		/*
		 * Parche para arreglar la diferencia entre la altura de la letra y
		 * la altura del bounds que contiene la letra impresa.
		 */
		double boundsHeight = rGeom.getHeight();
		double fontScale = height/boundsHeight;
		tx.preConcatenate(AffineTransform.getScaleInstance(1, fontScale));
		//Fin del parche.
		
		tx.preConcatenate(AffineTransform.getTranslateInstance(p.getX(), p.getY()));
		geomResult.transform(tx);

		return geomResult;
	}

public FLayer cloneLayer() throws Exception {
		Annotation_Layer clonedLayer = new Annotation_Layer();
		clonedLayer.setSource(getSource());
		clonedLayer.setVisible(isVisible());
		clonedLayer.setISpatialIndex(getISpatialIndex());
		clonedLayer.setName(getName());
		clonedLayer.setCoordTrans(getCoordTrans());
		clonedLayer.setLegend((IVectorLegend)getLegend());
		clonedLayer.mapping=mapping;
		clonedLayer.aLegend=aLegend;
		return clonedLayer;
	}
	private static void drawAnnotation(Graphics2D g2,int unit,Annotation_Layer layer,
			Annotation_Legend legend,Point2D pAux, String text,ViewPort viewPort,PrintRequestAttributeSet properties) {
		SimpleTextSymbol textSymbol=(SimpleTextSymbol)legend.getDefaultSymbol();
		textSymbol.setText(text);
//		float x;
//		float y;
		// Las etiquetas que pongamos a nulo ser? porque no la queremos dibujar.
		// ?til para cuando queramos eliminar duplicados.
		if (text == null) {
			return;
		}


//FIXME: No parece que haga falta todo esto.

//		int size=textSymbol.getFont().getSize();
//		double resolutionPrinting=0;
//		if (unit!=-1){
//			size=viewPort.fromMapDistance(size*MapContext.getDistanceTrans2Meter()[unit]);
//		}

//		if (properties!=null){
//			PrintQuality resolution=(PrintQuality)properties.get(PrintQuality.class);
//			if (resolution.equals(PrintQuality.NORMAL)){
//				resolutionPrinting=300/72;
//			}else if (resolution.equals(PrintQuality.HIGH)){
//				resolutionPrinting=600/72;
//			}else if (resolution.equals(PrintQuality.DRAFT)){
//				resolutionPrinting=72/72;
//			}
//			size=(int)(size*resolutionPrinting);
//		}

		textSymbol.draw(g2,viewPort.getAffineTransform(),new FPoint2D(pAux.getX(),pAux.getY()), null);

	}
}

