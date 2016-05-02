
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

package com.iver.cit.gvsig.fmap.edition;

import java.awt.Shape;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.List;

import org.cresques.cts.ICoordTrans;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.values.NullValue;
import com.hardcode.gdbms.engine.values.NumericValue;
import com.hardcode.gdbms.engine.values.StringValue;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileWriteException;
import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.fmap.core.DefaultFeature;
import com.iver.cit.gvsig.fmap.core.FPoint2D;
import com.iver.cit.gvsig.fmap.core.Handler;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.IRow;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.iver.cit.gvsig.fmap.drivers.DriverIOException;
import com.iver.cit.gvsig.fmap.layers.Annotation_Layer;
import com.iver.cit.gvsig.fmap.layers.Annotation_Mapping;
import com.iver.cit.gvsig.fmap.rendering.Annotation_Legend;
import com.iver.utiles.StringUtilities;
import com.iver.utiles.XMLEntity;


/**
 * Annotation Editable Adapter
 *
 * @author Vicente Caballero Navarro
 */
public class Annotation_EditableAdapter extends VectorialEditableAdapter {
	private static String DEFAULT_ANNOTATION_TEXT = "default_annotation_text";

	private static String DEFAULT_ANNOTATION_TYPEFONT = "default_annotation_typefont";

	private static String DEFAULT_ANNOTATION_ROTATE = "default_annotation_rotate";

	private static String DEFAULT_ANNOTATION_STYLEFONT = "default_annotation_stylefont";

	private static String DEFAULT_ANNOTATION_HEIGHT = "default_annotation_height";

	private static String DEFAULT_ANNOTATION_COLOR = "default_annotation_color";



	private Annotation_Mapping mapping;

    private Annotation_Layer lyrAnnotation;

     public int doAddRow(IRow feat, int sourceType) throws ExpansionFileWriteException, ReadDriverException {
		int position = super.doAddRow(feat, sourceType);
		PluginServices ps = PluginServices
				.getPluginServices("com.iver.cit.gvsig.annotation");
		XMLEntity xml = ps.getPersistentXML();
		String newFID=getNewFID();
		boolean cancel = fireBeforeRowAdded(sourceType,newFID);
		if (cancel)
			return -1;
		Value[] values = feat.getAttributes();
		if (!(values[0] instanceof NullValue &&
				values[1] instanceof NullValue &&
				values[2] instanceof NullValue &&
				values[3] instanceof NullValue &&
				values[4] instanceof NullValue &&
				values[5] instanceof NullValue)) {
			IGeometry geom;
			geom=((DefaultFeature)feat).getGeometry();
			Shape shape=geom.getInternalShape();
			Handler[] handlers=geom.getHandlers(IGeometry.SELECTHANDLER);
			Point2D h0=handlers[0].getPoint();
			if (!(shape instanceof FPoint2D)) {
				Point2D h1=handlers[1].getPoint();
				double rotation=Math.toDegrees(UtilFunctions.getAngle(h0,h1));
				values[mapping.getColumnRotate()] = ValueFactory.createValue(rotation);
			}
			geom = ShapeFactory.createPoint2D(h0.getX(),h0.getY());
			((DefaultFeature)feat).setGeometry(geom);
			return position;
		}

		int intColor = Annotation_Mapping.DEFAULTCOLOR;
		String text = Annotation_Mapping.DEFAULTTEXT;
		String type = Annotation_Mapping.DEFAULTTYPEFONT;
		int style = Annotation_Mapping.DEFAULTSTYLEFONT;
		int height = Annotation_Mapping.DEFAULTHEIGHT;
		int rotate = Annotation_Mapping.DEFAULTROTATE;

		if (xml.contains(DEFAULT_ANNOTATION_COLOR)){
			intColor = StringUtilities.string2Color(xml.getStringProperty(DEFAULT_ANNOTATION_COLOR)).getRGB();
			text = xml.getStringProperty(DEFAULT_ANNOTATION_TEXT);
			type = xml.getStringProperty(DEFAULT_ANNOTATION_TYPEFONT);
			style = xml.getIntProperty(DEFAULT_ANNOTATION_STYLEFONT);
			height = xml.getIntProperty(DEFAULT_ANNOTATION_HEIGHT);
			rotate = xml.getIntProperty(DEFAULT_ANNOTATION_ROTATE);
		}
		values[mapping.getColumnText()] = ValueFactory.createValue(text);
		values[mapping.getColumnTypeFont()] = ValueFactory.createValue(type);
		values[mapping.getColumnStyleFont()] = ValueFactory.createValue(style);
		values[mapping.getColumnHeight()] = ValueFactory.createValue(height);
		values[mapping.getColumnRotate()] = ValueFactory.createValue(rotate);
		values[mapping.getColumnColor()] = ValueFactory.createValue(intColor);
//		IGeometry geom;
//		ViewPort vp=lyrAnnotation.getMapContext().getViewPort();
//		geom = lyrAnnotation.getTextWrappingGeometry(height, text, rotate,
//					position,vp);
//		feat = new DefaultFeature(geom, values, feat.getID());
		return position;
	}

    public Annotation_EditableAdapter(Annotation_Layer lyrAnnotation) {
        super();
        this.mapping = lyrAnnotation.getAnnotatonMapping();
        this.lyrAnnotation = lyrAnnotation;
    }

    public IRowEdited[] getFeatures(Rectangle2D r, String strEPSG) throws ReadDriverException, ExpansionFileReadException{
        // En esta clase suponemos random access.
        // Luego tendremos otra clase que sea VectorialEditableDBAdapter
        // que reescribir? este m?todo.
//        Envelope e = FConverter.convertRectangle2DtoEnvelope(r);
        List l = fmapSpatialIndex.query(r);
        IRowEdited[] feats = new IRowEdited[l.size()];

        try {
            for (int index = 0; index < l.size(); index++) {
                Integer i = (Integer) l.get(index);
                int inverse = getInversedIndex(i.intValue());
                feats[index] = getRowAnnotation(inverse);
            }
        } catch (DriverIOException ex) {
        	 throw new ReadDriverException(lyrAnnotation.getName(),ex);
        }

        return feats;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.iver.cit.gvsig.fmap.edition.IEditableSource#getRow(int)
     */
    public IRowEdited getRowAnnotation(int index) throws DriverIOException, ExpansionFileReadException, ReadDriverException {
        int calculatedIndex = getCalculatedIndex(index);
        Integer integer = new Integer(calculatedIndex);

        // Si no est? en el fichero de expansi?n
        DefaultRowEdited edRow = null;
        ViewPort vp=lyrAnnotation.getMapContext().getViewPort();
       	IFeature f = null;
        if (!relations.containsKey(integer)) {
            f = ova.getFeature(calculatedIndex);
        } else {
            int num = ((Integer) relations.get(integer)).intValue();
            IRowEdited aux = expansionFile.getRow(num);
            f = (IFeature) aux.getLinkedRow().cloneRow();
        }
        String s = f.getID();
        Annotation_Mapping mapping = lyrAnnotation.getAnnotatonMapping();
        NumericValue vRotation = (NumericValue) f.getAttribute(mapping.getColumnRotate());
        NumericValue vHeight = (NumericValue) f.getAttribute(mapping.getColumnHeight());
        NumericValue vStyle = (NumericValue) f.getAttribute(mapping.getColumnStyleFont());
        StringValue vType = (StringValue) f.getAttribute(mapping.getColumnTypeFont());
        Value vText = f.getAttribute(mapping.getColumnText());
        start();
        IGeometry geom = getShape(index);
        stop();
        ICoordTrans ct = lyrAnnotation.getCoordTrans();
        boolean bMustClone = false;
        if (ct != null) {
            if (bMustClone) {
                geom = geom.cloneGeometry();
            }
            geom.reProject(ct);
        }
        geom.transform(vp.getAffineTransform());
        int unit=-1;
        if (lyrAnnotation.getLegend() instanceof Annotation_Legend) {
        	unit = ((Annotation_Legend)lyrAnnotation.getLegend()).getUnits();
        }
//        if (lyrAnnotation.getLabelingStrategy()!=null)
//        	unit=((AttrInTableLabelingStrategy)lyrAnnotation.getLabelingStrategy()).getUnit();

        IGeometry geom1 = lyrAnnotation.getTextWrappingGeometryInPixels(unit,vHeight.floatValue(), //*FConstant.FONT_HEIGHT_SCALE_FACTOR,
               vText.toString(), vRotation.doubleValue(),vType.getValue(),vStyle.intValue(), index, vp,geom);
      	try {
			geom1.transform(vp.getAffineTransform().createInverse());
		} catch (NoninvertibleTransformException e) {
			 throw new ReadDriverException(lyrAnnotation.getName(),e);
		}
      	f = new DefaultFeature(geom1, f.getAttributes(), s);
        edRow = new DefaultRowEdited(f,
               DefaultRowEdited.STATUS_ORIGINAL, index);

        return edRow;

    }
	/**
	 * Si se intenta modificar una geometr?a original de la capa en edici?n se
	 * a?ade al fichero de expansi?n y se registra la posici?n en la que se
	 * a?adi?. Si se intenta modificar una geometria que se encuentra en el
	 * fichero de expansi?n (por ser nueva o original pero modificada) se invoca
	 * el m?todo modifyGeometry y se actualiza el ?ndice de la geometria en el
	 * fichero.
	 *
	 * @param calculatedIndex
	 *            DOCUMENT ME!
	 * @param feat
	 *            DOCUMENT ME!
	 *
	 * @return position inside ExpansionFile
	 * @throws ReadDriverException
	 * @throws ExpansionFileWriteException
	 * @throws ExpansionFileReadException
	 *
	 * @throws IOException
	 * @throws DriverIOException
	 */
	public int doModifyRow(int calculatedIndex, IRow feat, int sourceType) throws ReadDriverException, ExpansionFileWriteException, ExpansionFileReadException {
		boolean cancel = fireBeforeModifyRow(feat, calculatedIndex, sourceType);
		if (cancel)
			return -1;
		int posAnteriorInExpansionFile = -1;
		Integer integer = new Integer(calculatedIndex);

		IFeature featAnt = null;
		IGeometry geom;
		Value[] values=feat.getAttributes();
		geom=((DefaultFeature)feat).getGeometry();
		Shape shape=geom.getInternalShape();
		Handler[] handlers=geom.getHandlers(IGeometry.SELECTHANDLER);
		Point2D h0=handlers[0].getPoint();
		if (!(shape instanceof FPoint2D)) {
			Point2D h1=handlers[1].getPoint();
			double rotation=-Math.toDegrees(UtilFunctions.getAngle(h0,h1));
			boolean rotate=false;
			if (((NumericValue)values[mapping.getColumnRotate()]).intValue() != (int)rotation ) {
				values[mapping.getColumnRotate()] = ValueFactory.createValue(rotation);
				rotate=true;
			}

			Point2D h2=handlers[2].getPoint();
			if (!rotate) {
				double height=Math.sqrt(Math.pow(h2.getX()-h1.getX(),2D)+Math.pow(h2.getY()-h1.getY(),2D));
				boolean isInPixels=((Annotation_Legend)lyrAnnotation.getLegend()).isFontSizeInPixels();
				int annotationUnits = ((Annotation_Legend)lyrAnnotation.getLegend()).getUnits();
				int mapUnits = lyrAnnotation.getMapContext().getViewPort().getMapUnits();
				double[] trans2Meter = lyrAnnotation.getMapContext().getDistanceTrans2Meter();
				double distance=height;
				if (isInPixels) {
					distance=lyrAnnotation.getMapContext().getViewPort().fromMapDistance(distance);
				} else {
					distance=distance
					* trans2Meter[mapUnits]
					/ trans2Meter[annotationUnits];
				}
				values[mapping.getColumnHeight()] = ValueFactory.createValue(distance);
			}
			Point2D h3=handlers[3].getPoint();
//			geom = ShapeFactory.createPoint2D(h3.getX(),h3.getY());
			geom = ShapeFactory.createPoint2D(h0.getX(),h0.getY());
		}else {
			geom = ShapeFactory.createPoint2D(h0.getX(),h0.getY());
		}

		feat = new DefaultFeature(geom, values, feat.getID());
		if (!relations.containsKey(integer)) {
			int newPosition = expansionFile.addRow(feat,
					IRowEdited.STATUS_MODIFIED, actualIndexFields);
			relations.put(integer, new Integer(newPosition));

			if (sourceType == EditionEvent.GRAPHIC) {
				// Se actualiza el ?ndice espacial
				featAnt = (DefaultFeature) (ova.getFeature(calculatedIndex));
				IGeometry g = featAnt.getGeometry();
				Rectangle2D rAnt = g.getBounds2D();
				Rectangle2D r = ((IFeature) feat).getGeometry().getBounds2D();
				this.fmapSpatialIndex.delete(rAnt, new Integer(calculatedIndex));
				this.fmapSpatialIndex.insert(r, new Integer(calculatedIndex));
			}
		} else {
			// Obtenemos el ?ndice en el fichero de expansi?n
			int num = ((Integer) relations.get(integer)).intValue();
			posAnteriorInExpansionFile = num;

			// Obtenemos la geometr?a para actualiza el ?ndice
			// espacialposteriormente
			featAnt = (IFeature) expansionFile.getRow(num).getLinkedRow();

			/*
			 * Se modifica la geometr?a y nos guardamos el ?ndice dentro del
			 * fichero de expansi?n en el que se encuentra la geometr?a
			 * modificada
			 */
			num = expansionFile.modifyRow(num, feat, actualIndexFields);

			/*
			 * Actualiza la relaci?n del ?ndice de la geometr?a al ?ndice en el
			 * fichero de expansi?n.
			 */
			relations.put(integer, new Integer(num));
			if (sourceType == EditionEvent.GRAPHIC) {
				// Se modifica el ?ndice espacial
				Rectangle2D rAnt = featAnt.getGeometry().getBounds2D();
				Rectangle2D r = ((IFeature) feat).getGeometry().getBounds2D();
				this.fmapSpatialIndex.delete(rAnt, new Integer(calculatedIndex));
				this.fmapSpatialIndex.insert(r, new Integer(calculatedIndex));
			}
		}
		isFullExtentDirty = true;
		fireAfterModifyRow(calculatedIndex, sourceType);
		return posAnteriorInExpansionFile;
	}

}

