/*
 * Created on 19-oct-2005
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
package com.iver.cit.gvsig.fmap.drivers.dxf.write;

import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.Vector;

import org.cresques.cts.IProjection;
import org.cresques.geo.Ellipsoid;
import org.cresques.geo.Point3D;
import org.cresques.geo.UtmZone;
import org.cresques.io.DxfFile;
import org.cresques.io.DxfGroup;
import org.cresques.io.DxfGroupVector;
import org.cresques.px.dxf.DxfEntityMaker;

import com.iver.cit.gvsig.fmap.core.FGeometry;
import com.iver.cit.gvsig.fmap.core.FPoint2D;
import com.iver.cit.gvsig.fmap.core.FPoint3D;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IGeometry;

public class DxfGisWriter {
    private DxfFile.EntityFactory entityMaker;
    private DxfFile dxfFile;
    private IProjection proj;

    public DxfGisWriter() {}

    public void write(IGeometry[] geometries, File file) throws Exception {
        proj = UtmZone.getProjection(Ellipsoid.hayford, 30, UtmZone.NORTH);
        // NOTA: La proyecci?n no se usa absolutamente para nada (al menos
        // por ahora). Las entidades se escribir?n con las coordenadas con
        // las que se crean.
        entityMaker = new DxfEntityMaker(proj);
        int handle = 40; // Revisar porqu? es 40.
        int k=0;
        boolean dxf3DFile = false;
        for (int i=0;i<geometries.length;i++) {
        	IGeometry geom = geometries[i];
            if (geom.getGeometryType()==FShape.POINT) {
                createPoint2D(handle, k, geom);
                k++;
            } else if (geom.getGeometryType()==(FShape.POINT | FShape.Z)) {
                dxf3DFile = true;
                createPoint3D(handle, k, geom);
                k++;
            } else if (geom.getGeometryType()==FShape.LINE) {
                createLwPolyline2D(handle, k, geom);
                k++;
            } else if (geom.getGeometryType()==(FShape.LINE | FShape.Z)) {
                dxf3DFile = true;
                k = createPolyline3D(handle, k, geom);
            } else if (geom.getGeometryType()==FShape.POLYGON) {
                createPolygon2D(handle, k, geom);
                k++;
            } else if (geom.getGeometryType()==(FShape.POLYGON | FShape.Z)) {
                dxf3DFile = true;
                k = createPolygon3D(handle, k, geom);
            } else {
                System.out.println("IGeometry not supported yet");
                k++;
            }
        }
        dxfFile = new DxfFile(proj, file.getAbsolutePath(), entityMaker);
        dxfFile.setCadFlag(true);
        if (dxf3DFile) dxfFile.setDxf3DFlag(true);
        dxfFile.save(file.getAbsolutePath());
    }

	/**
	 * @param handle
	 * @param k
	 * @param geom
	 * @return
	 * @throws Exception
	 */
	private int createPolygon3D(int handle, int k, IGeometry geom) throws Exception {
		DxfGroupVector polv = new DxfGroupVector();
		DxfGroup polylineLayer = new DxfGroup(8, "default");
		polv.add(polylineLayer);
		DxfGroup handleGroup = new DxfGroup();
		handleGroup.setCode(5);
		handleGroup.setData(new Integer(handle+k).toString());
		polv.add(handleGroup);
		Vector vpoints = new Vector();
		PathIterator theIterator = geom.getPathIterator(null); //polyLine.getPathIterator(null, flatness);
		double[] theData = new double[6];
		double[] velev = ((FGeometry)geom).getZs();
		while (!theIterator.isDone()) {
		    int theType = theIterator.currentSegment(theData);
		    switch (theType) {
		        case PathIterator.SEG_MOVETO:
		            vpoints.add(new FPoint2D(theData[0], theData[1]));
		            break;
		        case PathIterator.SEG_LINETO:
		            vpoints.add(new FPoint2D(theData[0], theData[1]));
		            break;
		    }
		    theIterator.next();
		}
		if (constantElevation(velev)) {
		    DxfGroup polylineFlag = new DxfGroup();
		    polylineFlag.setCode(70);
		    polylineFlag.setData(new Integer(1));
		    polv.add(polylineFlag);
		    DxfGroup elevation = new DxfGroup();
		    elevation.setCode(38);
		    elevation.setData(new Double(velev[0]));
		    polv.add(elevation);
		    for (int j=0;j<vpoints.size();j++) {
		        DxfGroup xvertex = new DxfGroup();
		        xvertex.setCode(10);
		        xvertex.setData(new Double(((FPoint2D)vpoints.get(j)).getX()));
		        DxfGroup yvertex = new DxfGroup();
		        yvertex.setCode(20);
		        yvertex.setData(new Double(((FPoint2D)vpoints.get(j)).getY()));
		        polv.add(xvertex);
		        polv.add(yvertex);
		    }
		    entityMaker.createLwPolyline(polv);
		    k++;
		} else {
		    DxfGroup polylineFlag = new DxfGroup();
		    polylineFlag.setCode(70);
		    polylineFlag.setData(new Integer(9));
		    polv.add(polylineFlag);
		    DxfGroup xgroup = new DxfGroup();
		    xgroup.setCode(10);
		    xgroup.setData(new Double(0.0));
		    polv.add(xgroup);
		    DxfGroup ygroup = new DxfGroup();
		    ygroup.setCode(20);
		    ygroup.setData(new Double(0.0));
		    polv.add(ygroup);
		    DxfGroup elevation = new DxfGroup();
		    elevation.setCode(30);
		    elevation.setData(new Double(0.0));
		    polv.add(elevation);
		    DxfGroup subclassMarker = new DxfGroup(100, "AcDb3dPolyline");
		    polv.add(subclassMarker);
		    entityMaker.createPolyline(polv);
		    k++;
		    for (int j=0;j<vpoints.size();j++) {
		        DxfGroupVector verv = new DxfGroupVector();
		        DxfGroup entityType = new DxfGroup(0, "VERTEX");
		        verv.add(entityType);
		        DxfGroup generalSubclassMarker = new DxfGroup(100, "AcDbEntity");
		        verv.add(generalSubclassMarker);
		        DxfGroup layerName = new DxfGroup(8, "default");
		        verv.add(layerName);
		        DxfGroup vertexSubclassMarker = new DxfGroup(100, "AcDbVertex");
		        verv.add(vertexSubclassMarker);
		        //DxfGroup vertex3DSubclassMarker = new DxfGroup(100, "AcDb3dPolylineVertex");
		        //verv.add(vertex3DSubclassMarker);
		        DxfGroup xvertex = new DxfGroup();
		        xvertex.setCode(10);
		        xvertex.setData(new Double(((FPoint2D)vpoints.get(j)).getX()));
		        DxfGroup yvertex = new DxfGroup();
		        yvertex.setCode(20);
		        yvertex.setData(new Double(((FPoint2D)vpoints.get(j)).getY()));
		        DxfGroup zvertex = new DxfGroup();
		        zvertex.setCode(30);
		        zvertex.setData(new Double(velev[j]));
		        verv.add(xvertex);
		        verv.add(yvertex);
		        verv.add(zvertex);
		        entityMaker.addVertex(verv);
		        k++;
		    }
		    DxfGroupVector seqv = new DxfGroupVector();
		    DxfGroup entityType = new DxfGroup(0, "SEQEND");
		    seqv.add(entityType);
		    //DxfGroup handle = new DxfGroup();
		    //elevation.setCode(5);
		    //elevation.setData(new Integer(getHandle()));
		    //seqv.add(handle);
		    DxfGroup generalSubclassMarker = new DxfGroup(100, "AcDbEntity");
		    seqv.add(generalSubclassMarker);
		    DxfGroup layerName = new DxfGroup(8, "default");
		    seqv.add(layerName);
		    DxfGroup handleSeqGroup = new DxfGroup();
		    handleSeqGroup.setCode(5);
		    handleSeqGroup.setData(new Integer(handle+k).toString());
		    seqv.add(handleSeqGroup);
		    entityMaker.endSeq();
		    k++;
		}
		return k;
	}

	/**
	 * @param handle
	 * @param k
	 * @param geom
	 * @throws Exception
	 */
	private void createPolygon2D(int handle, int k, IGeometry geom) throws Exception {
		DxfGroupVector polv = new DxfGroupVector();
		DxfGroup polylineLayer = new DxfGroup(8, "default");
		polv.add(polylineLayer);
		DxfGroup handleGroup = new DxfGroup();
		handleGroup.setCode(5);
		handleGroup.setData(new Integer(handle+k).toString());
		polv.add(handleGroup);
		DxfGroup polylineFlag = new DxfGroup();
		polylineFlag.setCode(70);
		polylineFlag.setData(new Integer(1));
		polv.add(polylineFlag);

		Vector vpoints = new Vector();
		PathIterator theIterator = geom.getPathIterator(null); //polyLine.getPathIterator(null, flatness);
		double[] theData = new double[6];
		while (!theIterator.isDone()) {
		    int theType = theIterator.currentSegment(theData);
		    switch (theType) {
		        case PathIterator.SEG_MOVETO:
		            vpoints.add(new FPoint2D(theData[0], theData[1]));
		            break;
		        case PathIterator.SEG_LINETO:
		            vpoints.add(new FPoint2D(theData[0], theData[1]));
		            break;
		    }
		    theIterator.next();
		}
		for (int j=0;j<vpoints.size();j++) {
		    DxfGroup xvertex = new DxfGroup();
		    xvertex.setCode(10);
		    xvertex.setData(new Double(((FPoint2D)vpoints.get(j)).getX()));
		    DxfGroup yvertex = new DxfGroup();
		    yvertex.setCode(20);
		    yvertex.setData(new Double(((FPoint2D)vpoints.get(j)).getY()));
		    polv.add(xvertex);
		    polv.add(yvertex);
		}
		entityMaker.createLwPolyline(polv);
	}

	/**
	 * @param handle
	 * @param k
	 * @param geom
	 * @return
	 * @throws Exception
	 */
	private int createPolyline3D(int handle, int k, IGeometry geom) throws Exception {
		DxfGroupVector polv = new DxfGroupVector();
		DxfGroup polylineLayer = new DxfGroup(8, "default");
		polv.add(polylineLayer);
		DxfGroup handleGroup = new DxfGroup();
		handleGroup.setCode(5);
		handleGroup.setData(new Integer(handle+k).toString());
		polv.add(handleGroup);
		Vector vpoints = new Vector();
		PathIterator theIterator = geom.getPathIterator(null); //polyLine.getPathIterator(null, flatness);
		double[] theData = new double[6];
		double[] velev = ((FGeometry)geom).getZs();
		while (!theIterator.isDone()) {
		    int theType = theIterator.currentSegment(theData);
		    switch (theType) {
		        case PathIterator.SEG_MOVETO:
		            vpoints.add(new FPoint2D(theData[0], theData[1]));
		            break;
		        case PathIterator.SEG_LINETO:
		            vpoints.add(new FPoint2D(theData[0], theData[1]));
		            break;
		    }
		    theIterator.next();
		}
		if (constantElevation(velev)) {
		    DxfGroup polylineFlag = new DxfGroup();
		    polylineFlag.setCode(70);
		    polylineFlag.setData(new Integer(0));
		    polv.add(polylineFlag);
		    DxfGroup elevation = new DxfGroup();
		    elevation.setCode(38);
		    elevation.setData(new Double(velev[0]));
		    polv.add(elevation);
		    for (int j=0;j<vpoints.size();j++) {
		        DxfGroup xvertex = new DxfGroup();
		        xvertex.setCode(10);
		        xvertex.setData(new Double(((FPoint2D)vpoints.get(j)).getX()));
		        DxfGroup yvertex = new DxfGroup();
		        yvertex.setCode(20);
		        yvertex.setData(new Double(((FPoint2D)vpoints.get(j)).getY()));
		        polv.add(xvertex);
		        polv.add(yvertex);
		    }
		    entityMaker.createLwPolyline(polv);
		    k++;
		} else {
		    DxfGroup polylineFlag = new DxfGroup();
		    polylineFlag.setCode(70);
		    polylineFlag.setData(new Integer(8));
		    polv.add(polylineFlag);
		    DxfGroup xgroup = new DxfGroup();
		    xgroup.setCode(10);
		    xgroup.setData(new Double(0.0));
		    polv.add(xgroup);
		    DxfGroup ygroup = new DxfGroup();
		    ygroup.setCode(20);
		    ygroup.setData(new Double(0.0));
		    polv.add(ygroup);
		    DxfGroup elevation = new DxfGroup();
		    elevation.setCode(30);
		    elevation.setData(new Double(0.0));
		    polv.add(elevation);
		    DxfGroup subclassMarker = new DxfGroup(100, "AcDb3dPolyline");
		    polv.add(subclassMarker);
		    entityMaker.createPolyline(polv);
		    k++;
		    for (int j=0;j<vpoints.size();j++) {
		        DxfGroupVector verv = new DxfGroupVector();
		        DxfGroup entityType = new DxfGroup(0, "VERTEX");
		        verv.add(entityType);
		        DxfGroup generalSubclassMarker = new DxfGroup(100, "AcDbEntity");
		        verv.add(generalSubclassMarker);
		        DxfGroup layerName = new DxfGroup(8, "default");
		        verv.add(layerName);
		        DxfGroup vertexSubclassMarker = new DxfGroup(100, "AcDbVertex");
		        verv.add(vertexSubclassMarker);
		        //DxfGroup vertex3DSubclassMarker = new DxfGroup(100, "AcDb3dPolylineVertex");
		        //verv.add(vertex3DSubclassMarker);
		        DxfGroup xvertex = new DxfGroup();
		        xvertex.setCode(10);
		        xvertex.setData(new Double(((FPoint2D)vpoints.get(j)).getX()));
		        DxfGroup yvertex = new DxfGroup();
		        yvertex.setCode(20);
		        yvertex.setData(new Double(((FPoint2D)vpoints.get(j)).getY()));
		        DxfGroup zvertex = new DxfGroup();
		        zvertex.setCode(30);
		        zvertex.setData(new Double(velev[j]));
		        verv.add(xvertex);
		        verv.add(yvertex);
		        verv.add(zvertex);
		        entityMaker.addVertex(verv);
		        k++;
		    }
		    DxfGroupVector seqv = new DxfGroupVector();
		    DxfGroup entityType = new DxfGroup(0, "SEQEND");
		    seqv.add(entityType);
		    //DxfGroup handle = new DxfGroup();
		    //elevation.setCode(5);
		    //elevation.setData(new Integer(getHandle()));
		    //seqv.add(handle);
		    DxfGroup generalSubclassMarker = new DxfGroup(100, "AcDbEntity");
		    seqv.add(generalSubclassMarker);
		    DxfGroup layerName = new DxfGroup(8, "default");
		    seqv.add(layerName);
		    DxfGroup handleSeqGroup = new DxfGroup();
		    handleSeqGroup.setCode(5);
		    handleSeqGroup.setData(new Integer(handle+k).toString());
		    seqv.add(handleSeqGroup);
		    entityMaker.endSeq();
		    k++;
		}
		return k;
	}

	/**
	 * @param handle
	 * @param k
	 * @param geom
	 * @throws Exception
	 */
	private void createLwPolyline2D(int handle, int k, IGeometry geom) throws Exception {
		DxfGroupVector polv = new DxfGroupVector();
		DxfGroup polylineLayer = new DxfGroup(8, "default");
		polv.add(polylineLayer);
		DxfGroup handleGroup = new DxfGroup();
		handleGroup.setCode(5);
		handleGroup.setData(new Integer(handle+k).toString());
		polv.add(handleGroup);
		Vector vpoints = new Vector();
		PathIterator theIterator = geom.getPathIterator(null); //polyLine.getPathIterator(null, flatness);
		double[] theData = new double[6];
		while (!theIterator.isDone()) {
		    int theType = theIterator.currentSegment(theData);
		    switch (theType) {
		        case PathIterator.SEG_MOVETO:
		            vpoints.add(new FPoint2D(theData[0], theData[1]));
		            break;
		        case PathIterator.SEG_LINETO:
		            vpoints.add(new FPoint2D(theData[0], theData[1]));
		            break;
		    }
		    theIterator.next();
		}
		DxfGroup polylineFlag = new DxfGroup();
		polylineFlag.setCode(70);
		polylineFlag.setData(new Integer(0));
		polv.add(polylineFlag);
		for (int j=0;j<vpoints.size();j++) {
		    DxfGroup xvertex = new DxfGroup();
		    xvertex.setCode(10);
		    xvertex.setData(new Double(((FPoint2D)vpoints.get(j)).getX()));
		    DxfGroup yvertex = new DxfGroup();
		    yvertex.setCode(20);
		    yvertex.setData(new Double(((FPoint2D)vpoints.get(j)).getY()));
		    polv.add(xvertex);
		    polv.add(yvertex);
		}
		entityMaker.createLwPolyline(polv);
	}

	/**
	 * @param handle
	 * @param k
	 * @param geom
	 * @throws Exception
	 */
	private void createPoint3D(int handle, int k, IGeometry geom) throws Exception {
		FPoint3D point = new FPoint3D(0,0,0);
		double[] pointCoords = new double[6];
		PathIterator pointIt = geom.getPathIterator(null);
		while (!pointIt.isDone()) {
		    pointIt.currentSegment(pointCoords);
		    point = new FPoint3D(pointCoords[0], pointCoords[1], pointCoords[2]);
		    pointIt.next();
		}
		Point3D pto = new Point3D(point.getX(), point.getY(), point.getZs()[0]);
		DxfGroup pointLayer = new DxfGroup(8, "default");
		DxfGroup handleGroup = new DxfGroup();
		handleGroup.setCode(5);
		handleGroup.setData(new Integer(handle+k).toString());
		DxfGroup px = new DxfGroup();
		DxfGroup py = new DxfGroup();
		DxfGroup pz = new DxfGroup();
		px.setCode(10);
		px.setData(new Double(pto.getX()));
		py.setCode(20);
		py.setData(new Double(pto.getY()));
		pz.setCode(30);
		pz.setData(new Double(pto.getZ()));
		DxfGroupVector pv = new DxfGroupVector();
		pv.add(pointLayer);
		pv.add(handleGroup);
		pv.add(px);
		pv.add(py);
		pv.add(pz);
		entityMaker.createPoint(pv);
	}

	/**
	 * @param handle
	 * @param k
	 * @param geom
	 * @throws Exception
	 */
	private void createPoint2D(int handle, int k, IGeometry geom) throws Exception {
		FPoint2D point = new FPoint2D(0,0);
		double[] pointCoords = new double[6];
		PathIterator pointIt = geom.getPathIterator(null);
		while (!pointIt.isDone()) {
		    pointIt.currentSegment(pointCoords);
		    point = new FPoint2D(pointCoords[0], pointCoords[1]);
		    pointIt.next();
		}
		Point2D pto = new Point2D.Double(point.getX(), point.getY());
		DxfGroup pointLayer = new DxfGroup(8, "default");
		DxfGroup handleGroup = new DxfGroup();
		handleGroup.setCode(5);
		handleGroup.setData(new Integer(handle+k).toString());
		DxfGroup px = new DxfGroup();
		DxfGroup py = new DxfGroup();
		DxfGroup pz = new DxfGroup();
		px.setCode(10);
		px.setData(new Double(pto.getX()));
		py.setCode(20);
		py.setData(new Double(pto.getY()));
		pz.setCode(30);
		// POINT del DXF tiene cota. Le asigno cero arbitrariamente.
		pz.setData(new Double(0.0));
		DxfGroupVector pv = new DxfGroupVector();
		pv.add(pointLayer);
		pv.add(handleGroup);
		pv.add(px);
		pv.add(py);
		pv.add(pz);
		entityMaker.createPoint(pv);
	}

    private boolean constantElevation(double[] velev) {
        boolean constant = true;
        for (int i=0;i<velev.length;i++) {
            for (int j=0;j<velev.length;j++) {
                if (j>i) {
                    if (velev[i]!=velev[j]) {
                        constant = false;
                        break;
                    }
                }
            }
            break;
        }
        return constant;
    }
}

