/* gvSIG. Sistema de Informaci?n Geogr?fica de la Generalitat Valenciana
 *
 * Copyright (C) 2006 Prodevelop and Generalitat Valenciana.
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
 *   Generalitat Valenciana
 *   Conselleria d'Infraestructures i Transport
 *   Av. Blasco Ib??ez, 50
 *   46010 VALENCIA
 *   SPAIN
 *
 *   +34 963862235
 *   gvsig@gva.es
 *   www.gvsig.gva.es
 *
 *    or
 *
 *   Prodevelop Integraci?n de Tecnolog?as SL
 *   Conde Salvatierra de ?lava , 34-10
 *   46004 Valencia
 *   Spain
 *
 *   +34 963 510 612
 *   +34 963 510 968
 *   gis@prodevelop.es
 *   http://www.prodevelop.es
 */
package es.prodevelop.cit.gvsig.fmap.drivers.jdbc.oracle;

import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import oracle.sql.ARRAY;
import oracle.sql.Datum;
import oracle.sql.NUMBER;
import oracle.sql.STRUCT;
import oracle.sql.StructDescriptor;

import org.apache.log4j.Logger;

import com.hardcode.gdbms.engine.spatial.fmap.FShapeGeneralPathX;
import com.iver.cit.gvsig.fmap.core.FCircle2D;
import com.iver.cit.gvsig.fmap.core.FGeometryCollection;
import com.iver.cit.gvsig.fmap.core.FMultiPoint2D;
import com.iver.cit.gvsig.fmap.core.FMultipoint3D;
import com.iver.cit.gvsig.fmap.core.FNullGeometry;
import com.iver.cit.gvsig.fmap.core.FPoint2D;
import com.iver.cit.gvsig.fmap.core.FPoint3D;
import com.iver.cit.gvsig.fmap.core.FPolygon2D;
import com.iver.cit.gvsig.fmap.core.FPolygon3D;
import com.iver.cit.gvsig.fmap.core.FPolyline2D;
import com.iver.cit.gvsig.fmap.core.FPolyline3D;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.FShape3D;
import com.iver.cit.gvsig.fmap.core.GeneralPathX;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.IGeometry3D;
import com.iver.cit.gvsig.fmap.core.IGeometryM;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.iver.cit.gvsig.fmap.core.v02.FConverter;
import com.iver.cit.gvsig.fmap.drivers.ConnectionJDBC;
import com.iver.cit.gvsig.fmap.drivers.DBLayerDefinition;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.drivers.IConnection;
import com.iver.cit.gvsig.fmap.edition.UtilFunctions;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.algorithm.RobustCGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateArrays;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;


/**
 * Utility class with static methods.
 *
 * @author jldominguez
 *
 */
public class OracleSpatialUtils {
    private static Logger logger = Logger.getLogger(OracleSpatialUtils.class.getName());
    private static double FLATNESS = 0.8;
    private static GeometryFactory geomFactory = new GeometryFactory();
    private static final double IRRELEVANT_DISTANCE = 0.00000001;
    private static Random rnd = new Random();
    private static DecimalFormat df = new DecimalFormat();
    private static DecimalFormatSymbols dfs = new DecimalFormatSymbols();
    public static final int ORACLE_GTYPE_UNKNOWN = 0;
    public static final int ORACLE_GTYPE_POINT = 1;
    public static final int ORACLE_GTYPE_LINE = 2;
    public static final int ORACLE_GTYPE_POLYGON = 3;
    public static final int ORACLE_GTYPE_COLLECTION = 4;
    public static final int ORACLE_GTYPE_MULTIPOINT = 5;
    public static final int ORACLE_GTYPE_MULTILINE = 6;
    public static final int ORACLE_GTYPE_MULTIPOLYGON = 7;

    public static final int ORACLE_GTYPE_COMPLEX_VOIDED_OR_NORMAL_POLYGON = 3;
    public static final int ORACLE_GTYPE_COMPLEX_COMPOUND_LINE = 4;
    public static final int ORACLE_GTYPE_COMPLEX_COMPOUND_POLYGON = 5;
    
    // req by wolfgang qual sept 2009
    public static final String ORACLE_GEOM_METADATA_TOLERANCE = "0.0005";
    
	public static final int FETCH_BLOCK_SIZE_BYTES = 10 * 1000 * 1000;
	public static final int FETCH_BLOCK_MAX = 10000;


    /**
     * COnstructs a geometry from a file that contains a vertex per line:
     *
     * x1 y1 z1
     * x2 y2 z2
     * ...
     *
     * @param filepath vertices text file path
     * @param polygon whether it is a polygon or not
     * @return the created geometry
     */
    public static IGeometry readGeometry3D(URL filepath, boolean polygon) {
        GeneralPathX resp = new GeneralPathX();
        File file = new File(filepath.getFile());
        ArrayList z = new ArrayList();

        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            double[] coords = new double[3];

            boolean move = true;

            String line = br.readLine();

            while (line != null) {
                coords = parseLine(line);

                if (line.length() == 0) {
                    move = true;
                }
                else {
                    if (move) {
                        resp.moveTo(coords[0], coords[1]);
                        z.add(new Double(coords[2]));
                    }
                    else {
                        resp.lineTo(coords[0], coords[1]);
                        z.add(new Double(coords[2]));
                    }

                    move = false;
                }

                line = br.readLine();
            }
        }
        catch (Exception ex) {
        	logger.error("While creating GeneralPathX: " +
                ex.getMessage());

            return null;
        }

        double[] zz = new double[z.size()];

        for (int i = 0; i < z.size(); i++) {
            zz[i] = ((Double) z.get(i)).doubleValue();
        }

        if (polygon) {
            return ShapeFactory.createPolygon3D(resp, zz);
        }
        else {
            return ShapeFactory.createPolyline3D(resp, zz);
        }
    }

    private static double[] parseLine(String line) {
        String[] sep = line.split(" ");
        double[] resp = new double[3];

        for (int i = 0; i < 3; i++)
            resp[i] = 0.0;

        try {
            resp[0] = Double.parseDouble(sep[0]);
        }
        catch (Exception ex) {
        }

        if (sep.length > 1) {
            try {
                resp[1] = Double.parseDouble(sep[1]);
            }
            catch (Exception ex) {
            }

            if (sep.length > 2) {
                try {
                    resp[2] = Double.parseDouble(sep[2]);
                }
                catch (Exception ex) {
                }
            }
        }

        return resp;
    }

    /**
     * Utility method to convert a gvSIG FShape into a oracle struct
     *
     * @param fshp the FShape object
     * @param c the connection
     * @param srid the SRS (oarcle code)
     * @param agu_b whether to check holes validity
     * @param hasSrid whether the SRS is non-NULL
     * @return a oracle struct representing the geometry
     *
     * @throws SQLException
     */
    public static STRUCT fShapeToSTRUCT(Object fshp, IConnection c, int srid,
        boolean agu_b, boolean hasSrid, boolean is_geodet) throws SQLException {
        boolean three = false;

        if (fshp instanceof FShape3D) {
            three = true;
        }

        STRUCT resp = null;

        if (fshp instanceof FMultiPoint2D) {
            resp = multiPoint2DToStruct((FMultiPoint2D) fshp, c, srid, hasSrid);

            return resp;
        }

        if (!(fshp instanceof FShape)) {
            logger.error("Unknown geometry: " + fshp.toString());

            return null;
        }

        if (fshp instanceof FPoint2D) { // point 2/3d

            // resp = pointToWKT((FPoint2D) fshp, three);
            Coordinate p = getSingleCoordinate((FPoint2D) fshp);
            resp = getMultiPointAsStruct(p, srid, three, c, hasSrid);
        }
        else {
            if (fshp instanceof FPolygon2D) { // polygon 2/3d

                if ((fshp instanceof FCircle2D) && (!is_geodet)) {
                    resp = getCircleAsStruct((FCircle2D) fshp, srid, c, hasSrid);
                } else {
                    // also FEllipse2D
                    resp = getMultiPolygonAsStruct((FShape) fshp, srid, three,
                            c, agu_b, hasSrid, is_geodet);

                    // ArrayList polys = getPolygonsEasily(fshp);
                    // resp = getMultiPolygonAsStruct(polys, srid, three, c);
                }
            }
            else { // line 2/3d

                ArrayList _lines = getLineStrings((FShape) fshp, is_geodet);
                resp = getMultiLineAsStruct(_lines, srid, three, c, hasSrid);
            }
        }

        return resp;
    }

    private static STRUCT multiPoint2DToStruct(FMultiPoint2D mp2d,
        IConnection c, int srid, boolean hasSrid) throws SQLException {
        int np = mp2d.getNumPoints();
        boolean threed = (mp2d instanceof FMultipoint3D);
        int gtype = 2005;
        int dim = 2;
        FMultipoint3D mp3d = null;

        if (threed) {
            gtype = 3005;
            dim = 3;
            mp3d = (FMultipoint3D) mp2d;
        }

        NUMBER[] indices = new NUMBER[3];
        indices[0] = new NUMBER(1);
        indices[1] = new NUMBER(1);
        indices[2] = new NUMBER(np);

        NUMBER[] ords = new NUMBER[dim * np];

        for (int i = 0; i < np; i++) {
            ords[dim * i] = new NUMBER(mp2d.getPoint(i).getX());
            ords[(dim * i) + 1] = new NUMBER(mp2d.getPoint(i).getY());

            if (threed) {
                ords[(dim * i) + 2] = new NUMBER(mp3d.getZs()[i]);
            }
        }

        STRUCT resp;
        StructDescriptor dsc = StructDescriptor.createDescriptor("MDSYS.SDO_GEOMETRY",
        		((ConnectionJDBC)c).getConnection());
        Object[] obj = new Object[5];
        obj[0] = new NUMBER(gtype);

        if (hasSrid) {
            obj[1] = new NUMBER(srid);
        }
        else { // , boolean hasSrid
            obj[1] = null;
        }

        obj[2] = null;
        obj[3] = indices;
        obj[4] = ords;
        resp = new STRUCT(dsc, ((ConnectionJDBC)c).getConnection(), obj);

        return resp;
    }

    private static STRUCT getCircleAsStruct(FCircle2D fcirc, int srid,
        IConnection _conn, boolean hasSrid) throws SQLException {
        int geotype = 2003;
        NUMBER[] indices = new NUMBER[3];
        indices[0] = new NUMBER(1);
        indices[1] = new NUMBER(1003);
        indices[2] = new NUMBER(4);

        NUMBER[] ords = new NUMBER[6];
        Coordinate[] three_points = getThreePointsOfCircumference(fcirc.getCenter(),
                fcirc.getRadio());

        for (int i = 0; i < three_points.length; i++) {
            ords[i * 2] = new NUMBER(three_points[i].x);
            ords[(i * 2) + 1] = new NUMBER(three_points[i].y);
        }

        STRUCT resp;
        StructDescriptor dsc = StructDescriptor.createDescriptor("MDSYS.SDO_GEOMETRY",
        		((ConnectionJDBC)_conn).getConnection());
        Object[] obj = new Object[5];
        obj[0] = new NUMBER(geotype);

        if (hasSrid) {
            obj[1] = new NUMBER(srid);
        }
        else {
            obj[1] = null;
        }

        obj[2] = null;
        obj[3] = indices;
        obj[4] = ords;
        resp = new STRUCT(dsc, ((ConnectionJDBC)_conn).getConnection(), obj);

        return resp;
    }

    private static Coordinate[] getThreePointsOfCircumference(Point2D cntr,
        double radius) {
        Coordinate[] resp = new Coordinate[3];
        double x;
        double y;
        double alpha = 0;

        for (int i = 0; i < 3; i++) {
            alpha = (i * 120.0 * Math.PI) / 180.0;
            x = cntr.getX() + (radius * Math.cos(alpha));
            y = cntr.getY() + (radius * Math.sin(alpha));
            resp[i] = new Coordinate(x, y);
        }

        return resp;
    }

    private static Coordinate getSingleCoordinate(FPoint2D p2d) {
        // TODO Auto-generated method stub
        Coordinate resp = new Coordinate();
        resp.x = p2d.getX();
        resp.y = p2d.getY();

        if (p2d instanceof FPoint3D) {
            resp.z = ((FPoint3D) p2d).getZs()[0];
        }

        return resp;
    }

    private static ArrayList ensureSensibleLineString(ArrayList cc) {
        if (cc.size() == 2) {
            if (sameCoordinate((Coordinate) cc.get(0),
                        (Coordinate) cc.get(cc.size() - 1))) {
                ArrayList resp = new ArrayList();
                resp.add(cc.get(0));

                Coordinate newc = new Coordinate((Coordinate) cc.get(0));
                newc.x = newc.x + IRRELEVANT_DISTANCE;
                resp.add(newc);

                return resp;
            }
        }

        return cc;
    }

    private static boolean sameCoordinate(Coordinate c1, Coordinate c2) {
        if (c1.x != c2.x) {
            return false;
        }

        if (c1.y != c2.y) {
            return false;
        }

        return true;
    }

    private static ArrayList getClosedRelevantPolygon(ArrayList cc) {
        if (cc.size() == 2) {
            return getMinClosedCoords((Coordinate) cc.get(0));
        }

        if (cc.size() == 3) {
            if (sameCoordinate((Coordinate) cc.get(0), (Coordinate) cc.get(1))) {
                return getMinClosedCoords((Coordinate) cc.get(0));
            }

            if (sameCoordinate((Coordinate) cc.get(0), (Coordinate) cc.get(2))) {
                return getMinClosedCoords((Coordinate) cc.get(0));
            }

            if (sameCoordinate((Coordinate) cc.get(1), (Coordinate) cc.get(2))) {
                return getMinClosedCoords((Coordinate) cc.get(1));
            }

            cc.add(cc.get(0));

            return cc;
        }

        if (!sameCoordinate((Coordinate) cc.get(0),
                    (Coordinate) cc.get(cc.size() - 1))) {
            cc.add(cc.get(0));
        }

        return cc;
    }

    private static ArrayList getMinClosedCoords(Coordinate c) {
        ArrayList resp = new ArrayList();
        resp.add(c);

        Coordinate nc = new Coordinate(c);
        nc.x = nc.x + IRRELEVANT_DISTANCE;
        resp.add(nc);

        Coordinate nc2 = new Coordinate(nc);
        nc2.y = nc2.y + IRRELEVANT_DISTANCE;
        resp.add(nc2);

        resp.add(new Coordinate(c));

        return resp;
    }

    private static LinearRing getMinLinearRing(Coordinate c) {
        Coordinate[] p = new Coordinate[4];
        p[0] = c;

        Coordinate nc = new Coordinate(c);
        nc.x = nc.x + IRRELEVANT_DISTANCE;

        Coordinate nc2 = new Coordinate(nc);
        nc2.y = nc2.y - IRRELEVANT_DISTANCE;
        p[1] = nc;
        p[2] = nc2;
        p[3] = new Coordinate(c);

        CoordinateArraySequence cs = new CoordinateArraySequence(p);
        LinearRing ls = new LinearRing(cs, geomFactory);

        return ls;
    }

    private static double[] getMinLinearRingZ() {
        double[] resp = new double[4];

        for (int i = 0; i < 4; i++)
            resp[i] = 0.0;

        return resp;
    }

    private static boolean pointInList(Coordinate testPoint,
        Coordinate[] pointList) {
        int t;
        int numpoints;
        Coordinate p;

        numpoints = Array.getLength(pointList);

        for (t = 0; t < numpoints; t++) {
            p = pointList[t];

            if ((testPoint.x == p.x) && (testPoint.y == p.y) &&
                    ((testPoint.z == p.z) || (!(testPoint.z == testPoint.z))) //nan test; x!=x iff x is nan
            ) {
                return true;
            }
        }

        return false;
    }

    private static ArrayList getPolygonsEasily(FShape mpolygon, boolean isgeo) {
        boolean threed = false;

        if (mpolygon instanceof FPolygon3D) {
            threed = true;
        }

        int start_ind = 0;
        int end_ind = 0;
        int ind = 0;
        int new_size;
        ArrayList arrayCoords = null;
        ArrayList resp = new ArrayList();
        Coordinate[] points = null;
        int theType = -99;
        double[] theData = new double[6];
        Coordinate onlyCoord = null;
        int numParts = 0;

        PathIterator theIterator = mpolygon.getPathIterator(null,
        		isgeo ? (FLATNESS / 150000.0) : FLATNESS);
        

        while (!theIterator.isDone()) {
            //while not done
            theType = theIterator.currentSegment(theData);

            if (onlyCoord == null) {
                onlyCoord = new Coordinate();
                onlyCoord.x = theData[0];
                onlyCoord.y = theData[1];
            }

            switch (theType) {
            case PathIterator.SEG_MOVETO:

                if (arrayCoords == null) {
                    arrayCoords = new ArrayList();
                }
                else {
                    end_ind = ind - 1;

                    arrayCoords = getClosedRelevantPolygon(arrayCoords);
                    new_size = arrayCoords.size();

                    if (arrayCoords != null) {
                        points = CoordinateArrays.toCoordinateArray(arrayCoords);

                        try {
                            LinearRing aux = geomFactory.createLinearRing(points);
                            double[] z = null;

                            if (threed) {
                                z = getZ((FPolygon3D) mpolygon, start_ind,
                                        end_ind, new_size);
                            }

                            LineString3D ring = new LineString3D(aux, z);

                            if (CGAlgorithms.isCCW(points)) {
                                resp.add(ring);
                            }
                            else {
                                resp.add(ring.createReverse());
                            }
                        }
                        catch (Exception e) {
                        	logger.error("Topology exception: " +
                                e.getMessage());

                            return null;
                        }
                    }

                    arrayCoords = new ArrayList();

                    start_ind = ind;
                }

                numParts++;

                arrayCoords.add(new Coordinate(theData[0], theData[1]));
                ind++;

                break;

            case PathIterator.SEG_LINETO:
                arrayCoords.add(new Coordinate(theData[0], theData[1]));
                ind++;

                break;

            case PathIterator.SEG_QUADTO:
            	logger.info("SEG_QUADTO Not supported here");
                arrayCoords.add(new Coordinate(theData[0], theData[1]));
                arrayCoords.add(new Coordinate(theData[2], theData[3]));
                ind++;
                ind++;

                break;

            case PathIterator.SEG_CUBICTO:
            	logger.info("SEG_CUBICTO Not supported here");
                arrayCoords.add(new Coordinate(theData[0], theData[1]));
                arrayCoords.add(new Coordinate(theData[2], theData[3]));
                arrayCoords.add(new Coordinate(theData[4], theData[5]));
                ind++;
                ind++;
                ind++;

                break;

            case PathIterator.SEG_CLOSE:

                // Coordinate firstCoord = (Coordinate) arrayCoords.get(0);
                // arrayCoords.add(new Coordinate(firstCoord.x, firstCoord.y));
                break;
            } //end switch

            theIterator.next();
        } //end while loop

        end_ind = ind - 1;

        // null shape:
        if (arrayCoords == null) {
            arrayCoords = new ArrayList();

            Coordinate _c = new Coordinate(0, 0, 0);
            arrayCoords.add(new Coordinate(_c));
            arrayCoords.add(new Coordinate(_c));
        }

        // --------------------------------------------
        arrayCoords = getClosedRelevantPolygon(arrayCoords);
        new_size = arrayCoords.size();

        if (arrayCoords != null) {
            points = CoordinateArrays.toCoordinateArray(arrayCoords);

            try {
                LinearRing aux = geomFactory.createLinearRing(points);
                double[] z = null;

                if (threed) {
                    z = getZ((FPolygon3D) mpolygon, start_ind, end_ind, new_size);
                }

                LineString3D ring = new LineString3D(aux, z);

                if (CGAlgorithms.isCCW(points)) {
                    resp.add(ring);
                }
                else {
                    resp.add(ring.createReverse());
                }
            }
            catch (Exception e) {
            	logger.error("Topology exception: " + e.getMessage());

                return null;
            }
        }

        if (resp.size() == 0) {
            resp.add(new LineString3D(getMinLinearRing(onlyCoord),
                    getMinLinearRingZ()));
        }

        return resp;
    }

    /**
     * Utility method to reverse an array of doubles.
     *
     * @param _z an array of doubles to be reversed.
     *
     * @return the reversed array of doubles
     */
    public static double[] reverseArray(double[] _z) {
        int size = _z.length;
        double[] resp = new double[size];

        for (int i = 0; i < size; i++) {
            resp[i] = _z[size - 1 - i];
        }

        return resp;
    }

    /**
     * Utility method to reverse an array of coordinates
     *
     * @param _z an array of coordinaes to be reversed.
     *
     * @return the reversed array of coordinates
     */
    public static Coordinate[] reverseCoordinateArray(Coordinate[] _z) {
        int size = _z.length;
        Coordinate[] resp = new Coordinate[size];

        for (int i = 0; i < size; i++) {
            resp[i] = _z[size - 1 - i];
        }

        return resp;
    }

    private static double[] getZ(FShape3D p3d, int _str, int _end, int size) {
        double[] resp = new double[size];
        double[] allz = p3d.getZs();

        for (int i = _str; ((i <= _end) && ((i - _str) < size)); i++) {
            resp[i - _str] = allz[i];
        }

        if ((_end - _str + 1) < size) {
            double repe = allz[_end];

            for (int i = (_end - _str + 1); i < size; i++) {
                resp[i] = repe;
            }
        }

        return resp;
    }

    private static ArrayList getLineStrings(FShape mlines, boolean isgeo) {
        boolean threed = false;

        if (mlines instanceof FPolyline3D) {
            threed = true;
        }

        int start_ind = 0;
        int end_ind = 0;
        int ind = 0;
        int new_size = 0;

        LineString3D lin;

        ArrayList arrayLines = new ArrayList();
        PathIterator theIterator = mlines.getPathIterator(null, isgeo ? (FLATNESS/150000.0) : FLATNESS);
        int theType = -99;
        double[] theData = new double[6];
        ArrayList arrayCoords = null;
        int numParts = 0;

        while (!theIterator.isDone()) {
            //while not done
            theType = theIterator.currentSegment(theData);

            switch (theType) {
            case PathIterator.SEG_MOVETO:

                if (arrayCoords == null) {
                    arrayCoords = new ArrayList();
                }
                else {
                    end_ind = ind - 1;
                    arrayCoords = ensureSensibleLineString(arrayCoords);
                    new_size = arrayCoords.size();

                    LineString aux = geomFactory.createLineString(CoordinateArrays.toCoordinateArray(
                                arrayCoords));
                    double[] z = null;

                    if (threed) {
                        z = getZ((FPolyline3D) mlines, start_ind, end_ind,
                                new_size);
                    }

                    lin = new LineString3D(aux, z);
                    arrayLines.add(lin);
                    arrayCoords = new ArrayList();

                    start_ind = ind;
                }

                numParts++;
                arrayCoords.add(new Coordinate(theData[0], theData[1]));

                break;

            case PathIterator.SEG_LINETO:
                arrayCoords.add(new Coordinate(theData[0], theData[1]));

                break;

            case PathIterator.SEG_QUADTO:
            	logger.info("Not supported here: SEG_QUADTO");

                break;

            case PathIterator.SEG_CUBICTO:
            	logger.info("Not supported here: SEG_CUBICTO");

                break;

            case PathIterator.SEG_CLOSE:

                Coordinate firstCoord = (Coordinate) arrayCoords.get(0);
                arrayCoords.add(new Coordinate(firstCoord.x, firstCoord.y));

                break;
            } //end switch

            theIterator.next();
            ind++;
        } //end while loop

        arrayCoords = ensureSensibleLineString(arrayCoords);
        new_size = arrayCoords.size();

        LineString aux = geomFactory.createLineString(CoordinateArrays.toCoordinateArray(
                    arrayCoords));
        double[] z = null;

        if (threed) {
            z = getZ((FPolyline3D) mlines, start_ind, end_ind, new_size);
        }

        lin = new LineString3D(aux, z);
        arrayLines.add(lin);

        return arrayLines;
    }

    private static String lineStringToWKT(LineString3D ls, boolean threed) {
        String resp = "(";
        Coordinate[] cc = ls.getLs().getCoordinates();
        double[] z = ls.getZc();
        int size = cc.length;

        if (threed) {
            for (int i = 0; i < size; i++) {
                resp = resp + cc[i].x + " " + cc[i].y + " " + z[i] + ", ";
            }

            resp = resp.substring(0, resp.length() - 2);
            resp = resp + ")";
        }
        else {
            for (int i = 0; i < size; i++) {
                resp = resp + cc[i].x + " " + cc[i].y + ", ";
            }

            resp = resp.substring(0, resp.length() - 2);
            resp = resp + ")";
        }

        return resp;
    }

    private static String multiLineStringToWKT(ArrayList ml, boolean threed) {
        String resp = "MULTILINESTRING(";

        for (int i = 0; i < ml.size(); i++) {
            LineString3D ls = (LineString3D) ml.get(i);
            resp = resp + lineStringToWKT(ls, threed) + ", ";
        }

        resp = resp.substring(0, resp.length() - 2) + ")";

        return resp;
    }

    private static String polygonsToWKT(ArrayList pols, boolean threed) {
        String resp = "MULTIPOLYGON(";
        LineString3D ls = null;

        for (int i = 0; i < pols.size(); i++) {
            ls = (LineString3D) pols.get(i);
            resp = resp + "(" + lineStringToWKT(ls, threed) + "), ";
        }

        resp = resp.substring(0, resp.length() - 2) + ")";

        return resp;
    }

    private static String shellAndHolesToWKT(LineString3D shell,
        ArrayList holes, boolean threed) {
        String resp = "(";
        resp = resp + lineStringToWKT(shell, threed);

        if (holes.size() > 0) {
            for (int i = 0; i < holes.size(); i++) {
                LineString3D ls = (LineString3D) holes.get(i);
                resp = resp + ", " + lineStringToWKT(ls, threed);
            }
        }

        resp = resp + ")";

        return resp;
    }

    private static String multiPolygonToWKT(ArrayList shells, ArrayList hFs,
        boolean threed) {
        String resp = "MULTIPOLYGON(";
        LineString3D ls = null;
        ArrayList holes;

        for (int i = 0; i < shells.size(); i++) {
            ls = (LineString3D) shells.get(i);
            holes = (ArrayList) hFs.get(i);
            resp = resp + shellAndHolesToWKT(ls, holes, threed) + ", ";
        }

        resp = resp.substring(0, resp.length() - 2) + ")";

        return resp;
    }

    private static String pointToWKT(FPoint2D point, boolean threed) {
        String resp = "POINT(" + point.getX() + " " + point.getY();

        if ((threed) && (point instanceof FPoint3D)) {
            resp = resp + " " + ((FPoint3D) point).getZs()[0];
        }

        resp = resp + ")";

        return resp;
    }

    private static int twoDIndexToDimsIndex(int n, int d) {
        return ((d * (n - 1)) / 2) + 1;
    }

    private static ARRAY setSubelementsToDim(ARRAY old, int d)
        throws SQLException {
        Datum[] infos = (Datum[]) old.getOracleArray();

        for (int i = 3; i < infos.length; i = i + 3) {
            int oldind = infos[i].intValue();
            oldind = twoDIndexToDimsIndex(oldind, d);
            infos[i] = new NUMBER(oldind);

            //
            oldind = infos[i + 1].intValue();
            infos[i + 1] = new NUMBER(infos[1].intValue());
        }

        ARRAY resp = new ARRAY(old.getDescriptor(), old.getOracleConnection(),
                infos);

        return resp;
    }

    private static boolean isPointInsideLineString(Coordinate p, LineString ls) {
        Envelope env = ls.getEnvelopeInternal();

        if (!env.contains(p)) {
            return false;
        }

        return CGAlgorithms.isPointInRing(p, ls.getCoordinates());
    }

    private static boolean lineString3DIsContainedBy(LineString3D contained,
        LineString3D container) {
        int samples = 10;
        LineString _in = contained.getLs();
        LineString _out = container.getLs();
        Coordinate[] inc = _in.getCoordinates();
        Coordinate aux;
        int size = inc.length;

        if (size <= 10) {
            for (int i = 0; i < size; i++) {
                aux = inc[i];

                if (!isPointInsideLineString(aux, _out)) {
                    return false;
                }
            }

            return true;
        }
        else {
            for (int i = 0; i < samples; i++) {
                aux = inc[rnd.nextInt(size)];

                if (!isPointInsideLineString(aux, _out)) {
                    return false;
                }
            }

            return true;
        }
    }

    private static STRUCT getMultiPolygonAsStruct(ArrayList pols, int srid,
        boolean threed, IConnection _conn, boolean agu_bien, boolean hasSrid)
        throws SQLException {
        int size = pols.size();
        int geotype = 2007;
        int dim = 2;
        int acum = 0;

        if (threed) {
            geotype = 3007;
            dim = 3;
        }

        NUMBER[] indices = new NUMBER[3 * size];

        for (int i = 0; i < size; i++) {
            indices[3 * i] = new NUMBER(acum + 1);
            indices[(3 * i) + 1] = new NUMBER(1003);
            indices[(3 * i) + 2] = new NUMBER(1);
            acum = acum +
                (dim * ((LineString3D) pols.get(i)).getLs().getNumPoints());
        }

        int _ind = 0;
        NUMBER[] ords = new NUMBER[acum];

        for (int i = 0; i < size; i++) {
            LineString3D ls = (LineString3D) pols.get(i);
            int num_p = ls.getLs().getNumPoints();

            for (int j = 0; j < num_p; j++) {
                ords[_ind] = new NUMBER(ls.getLs().getCoordinateN(j).x);
                ords[_ind + 1] = new NUMBER(ls.getLs().getCoordinateN(j).y);

                if (threed) {
                    ords[_ind + 2] = new NUMBER(ls.getZc()[j]);
                }

                _ind = _ind + dim;
            }
        }

        STRUCT resp;
        StructDescriptor dsc = StructDescriptor.createDescriptor("MDSYS.SDO_GEOMETRY",
        		((ConnectionJDBC)_conn).getConnection());
        Object[] obj = new Object[5];
        obj[0] = new NUMBER(geotype);

        if (hasSrid) {
            obj[1] = new NUMBER(srid);
        }
        else {
            obj[1] = null;
        }

        obj[2] = null;
        obj[3] = indices;
        obj[4] = ords;
        resp = new STRUCT(dsc, ((ConnectionJDBC)_conn).getConnection(), obj);

        return resp;
    }

    private static STRUCT getMultiLineAsStruct(ArrayList lines, int srid,
        boolean threed, IConnection _conn, boolean hasSrid)
        throws SQLException {
        /*
        if (lines.size() == 1) {
                return getOneLineStringAsStruct((LineString3D) lines.get(0), srid, threed, _conn);
        }
        */
        int size = lines.size();
        int geotype = 2006;
        int dim = 2;
        int acum = 0;

        if (threed) {
            geotype = 3006;
            dim = 3;
        }

        NUMBER[] indices = new NUMBER[3 * size];

        for (int i = 0; i < size; i++) {
            indices[3 * i] = new NUMBER(acum + 1);
            indices[(3 * i) + 1] = new NUMBER(2);
            indices[(3 * i) + 2] = new NUMBER(1);
            acum = acum +
                (dim * ((LineString3D) lines.get(i)).getLs().getNumPoints());
        }

        int _ind = 0;
        NUMBER[] ords = new NUMBER[acum];

        for (int i = 0; i < size; i++) {
            LineString3D ls = (LineString3D) lines.get(i);
            int num_p = ls.getLs().getNumPoints();

            for (int j = 0; j < num_p; j++) {
                ords[_ind] = new NUMBER(ls.getLs().getCoordinateN(j).x);
                ords[_ind + 1] = new NUMBER(ls.getLs().getCoordinateN(j).y);

                if (threed) {
                    ords[_ind + 2] = new NUMBER(ls.getZc()[j]);
                }

                _ind = _ind + dim;
            }
        }

        STRUCT resp;
        StructDescriptor dsc = StructDescriptor.createDescriptor("MDSYS.SDO_GEOMETRY",
        		((ConnectionJDBC)_conn).getConnection());
        Object[] obj = new Object[5];
        obj[0] = new NUMBER(geotype);

        if (hasSrid) {
            obj[1] = new NUMBER(srid);
        }
        else {
            obj[1] = null;
        }

        obj[2] = null;
        obj[3] = indices;
        obj[4] = ords;
        resp = new STRUCT(dsc,((ConnectionJDBC)_conn).getConnection(), obj);

        return resp;
    }

    private static STRUCT getMultiPointAsStruct(Coordinate pnt, int srid,
        boolean threed, IConnection _conn, boolean hasSrid)
        throws SQLException {
        int geotype = 2001;
        int dim = 2;

        if (threed) {
            geotype = 3001;
            dim = 3;
        }

        Object[] ords = new Object[3];
        ords[0] = new NUMBER(pnt.x);
        ords[1] = new NUMBER(pnt.y);
        ords[2] = (dim == 3) ? new NUMBER(pnt.z) : null; // ole ole y ole

        StructDescriptor ord_dsc = StructDescriptor.createDescriptor("MDSYS.SDO_POINT_TYPE",
        		((ConnectionJDBC)_conn).getConnection());
        STRUCT ords_st = new STRUCT(ord_dsc, ((ConnectionJDBC)_conn).getConnection(), ords);

        STRUCT resp;

        StructDescriptor dsc = StructDescriptor.createDescriptor("MDSYS.SDO_GEOMETRY",
        		((ConnectionJDBC)_conn).getConnection());
        Object[] obj = new Object[5];

        obj[0] = new NUMBER(geotype);

        if (hasSrid) {
            obj[1] = new NUMBER(srid);
        }
        else {
            obj[1] = null;
        }

        obj[2] = ords_st;
        obj[3] = null;
        obj[4] = null;
        resp = new STRUCT(dsc, ((ConnectionJDBC)_conn).getConnection(), obj);

        return resp;
    }

    /**
     * Utility method to compute a circle's center and radius from three given points.
     *
     * @param points three points of a circumference
     * @return a 2-item array with the circumference's center (Point2D) and radius (Double)
     */
    public static Object[] getCenterAndRadiousOfCirc(Point2D[] points) {
        Object[] resp = new Object[2];
        resp[0] = new Point2D.Double(0, 0);
        resp[1] = new Double(0);

        double m11;
        double m12;
        double m13;
        double m14;

        if (points.length != 3) {
            logger.error("Needs 3 points (found " + points.length +
                ") - circle cannot be computed.");

            // not a circle
            return resp;
        }

        double[][] a = new double[3][3];

        for (int i = 0; i < 3; i++) { // find minor 11
            a[i][0] = points[i].getX();
            a[i][1] = points[i].getY();
            a[i][2] = 1;
        }

        m11 = determinant(a, 3);

        for (int i = 0; i < 3; i++) { // find minor 12
            a[i][0] = (points[i].getX() * points[i].getX()) +
                (points[i].getY() * points[i].getY());
            a[i][1] = points[i].getY();
            a[i][2] = 1;
        }

        m12 = determinant(a, 3);

        for (int i = 0; i < 3; i++) // find minor 13
         {
            a[i][0] = (points[i].getX() * points[i].getX()) +
                (points[i].getY() * points[i].getY());
            a[i][1] = points[i].getX();
            a[i][2] = 1;
        }

        m13 = determinant(a, 3);

        for (int i = 0; i < 3; i++) { // find minor 14
            a[i][0] = (points[i].getX() * points[i].getX()) +
                (points[i].getY() * points[i].getY());
            a[i][1] = points[i].getX();
            a[i][2] = points[i].getY();
        }

        m14 = determinant(a, 3);

        Double resp_radius = new Double(0);
        Point2D resp_center = new Point2D.Double(0, 0);

        if (m11 == 0) {
            logger.error("Three points aligned - circle cannot be computed."); // not a circle
        }
        else {
            double x = (0.5 * m12) / m11;
            double y = (-0.5 * m13) / m11;
            resp_center.setLocation(x, y);
            resp_radius = new Double(Math.sqrt((x * x) + (y * y) + (m14 / m11)));
            resp[0] = resp_center;
            resp[1] = resp_radius;
        }

        return resp;
    }

    /**
     * Utility method to compute a matrix determinant
     * @param a the matrix
     * @param n matrix size
     * @return the matrix's determinant
     */
    public static double determinant(double[][] a, int n) {
        double resp = 0;
        double[][] m = new double[3][3];

        if (n == 2) { // terminate recursion
            resp = (a[0][0] * a[1][1]) - (a[1][0] * a[0][1]);
        }
        else {
            resp = 0;

            for (int j1 = 0; j1 < n; j1++) { // do each column

                for (int i = 1; i < n; i++) { // create minor

                    int j2 = 0;

                    for (int j = 0; j < n; j++) {
                        if (j == j1) {
                            continue;
                        }

                        m[i - 1][j2] = a[i][j];
                        j2++;
                    }
                }

                // sum (+/-)cofactor * minor
                resp = resp +
                    (Math.pow(-1.0, j1) * a[0][j1] * determinant(m, n - 1));
            }
        }

        return resp;
    }

    private static int getSmallestContainerExcept(LineString3D ls,
        ArrayList list, int self) {
        int resp = -1;
        ArrayList provList = new ArrayList();

        int size = list.size();

        for (int i = 0; i < self; i++) {
            if (lineString3DIsContainedBy(ls, (LineString3D) list.get(i))) {
                provList.add(new Integer(i));
            }
        }

        for (int i = (self + 1); i < size; i++) {
            if (lineString3DIsContainedBy(ls, (LineString3D) list.get(i))) {
                provList.add(new Integer(i));
            }
        }

        if (provList.size() == 0) {
            // logger.debug("LineString is not contained by any other ls.");
        }
        else {
            if (provList.size() == 1) {
                resp = ((Integer) provList.get(0)).intValue();
            }
            else {
                if (provList.size() == 2) {
                    int ind_1 = ((Integer) provList.get(0)).intValue();
                    int ind_2 = ((Integer) provList.get(1)).intValue();
                    LineString3D ls1 = (LineString3D) list.get(ind_1);
                    LineString3D ls2 = (LineString3D) list.get(ind_2);

                    if (lineString3DIsContainedBy(ls1, ls2)) {
                        resp = ind_1;
                    }
                    else {
                        resp = ind_2;
                    }
                }
                else {
                    // not so deep, sorry!
                    // it's going to be a shell: resp = -1;
                }
            }
        }

        return resp;
    }

    private static int[] getIndicesOfShells(int[] containings) {
        ArrayList resp = new ArrayList();

        for (int i = 0; i < containings.length; i++) {
            if (containings[i] == -1) {
                resp.add(new Integer(i));
            }
        }

        int size = resp.size();
        int[] _resp = new int[size];

        for (int i = 0; i < size; i++) {
            _resp[i] = ((Integer) resp.get(i)).intValue();
        }

        return _resp;
    }

    private static int[] getIndicesOfHoles(int[] containings, int[] shells) {
        ArrayList resp = new ArrayList();

        for (int i = 0; i < containings.length; i++) {
            int cont_by = containings[i];

            if ((cont_by != -1) && (isOneOf(cont_by, shells))) {
                resp.add(new Integer(i));
            }
        }

        int size = resp.size();
        int[] _resp = new int[size];

        for (int i = 0; i < size; i++) {
            _resp[i] = ((Integer) resp.get(i)).intValue();
        }

        return _resp;
    }

    private static int[] getFinalContainings(int[] containings, int[] holes) {
        ArrayList resp = new ArrayList();

        for (int i = 0; i < containings.length; i++) {
            int cont_by = containings[i];

            if (isOneOf(cont_by, holes)) {
                resp.add(new Integer(-1));
            }
            else {
                resp.add(new Integer(cont_by));
            }
        }

        int size = resp.size();
        int[] _resp = new int[size];

        for (int i = 0; i < size; i++) {
            _resp[i] = ((Integer) resp.get(i)).intValue();
        }

        return _resp;
    }

    private static ArrayList getHolesOf(int ind, int[] final_contn,
        ArrayList all) {
        ArrayList resp_ind = new ArrayList();

        for (int i = 0; i < final_contn.length; i++) {
            if (final_contn[i] == ind) {
                resp_ind.add(new Integer(i));
            }
        }

        ArrayList resp = new ArrayList();

        for (int i = 0; i < resp_ind.size(); i++) {
            Integer aux = (Integer) resp_ind.get(i);
            resp.add(all.get(aux.intValue()));
        }

        return resp;
    }

    private static ArrayList getShellsIn(int[] final_contn, ArrayList all) {
        ArrayList resp_ind = new ArrayList();

        for (int i = 0; i < final_contn.length; i++) {
            if (final_contn[i] == -1) {
                resp_ind.add(new Integer(i));
            }
        }

        ArrayList resp = new ArrayList();

        for (int i = 0; i < resp_ind.size(); i++) {
            Integer aux = (Integer) resp_ind.get(i);
            resp.add(all.get(aux.intValue()));
        }

        return resp;
    }

    /**
     * This method tries to guess who is a shell and who is a hole from a set of
     * linestrings.
     *
     * @param all_ls a set of linestrings to be checked.
     *
     * @return a 2-item array. the first is an arraylist of linestrings thought to be shells.
     * the second is an array of arraylists containing the holes of each shell found in the
     * first item
     *
     */
    public static Object[] getHolesForShells(ArrayList all_ls) {
        int no_of_ls = all_ls.size();
        int[] containedby = new int[no_of_ls];
        int[] shells;
        int[] holes;
        int[] final_cont;

        for (int i = 0; i < no_of_ls; i++) {
            LineString3D ls_aux = (LineString3D) all_ls.get(i);
            containedby[i] = getSmallestContainerExcept(ls_aux, all_ls, i);
        }

        shells = getIndicesOfShells(containedby);
        holes = getIndicesOfHoles(containedby, shells);
        final_cont = getFinalContainings(containedby, holes);

        // true shells:
        shells = getIndicesOfShells(final_cont);

        ArrayList resp_shells = new ArrayList();
        ArrayList resp_holes_for_shells = new ArrayList();
        ArrayList aux_holes;

        for (int i = 0; i < shells.length; i++) {
            resp_shells.add(all_ls.get(shells[i]));
            aux_holes = getHolesOf(i, final_cont, all_ls);
            resp_holes_for_shells.add(aux_holes);
        }

        Object[] _resp = new Object[2];
        _resp[0] = resp_shells;
        _resp[1] = resp_holes_for_shells;

        return _resp;
    }

    private static int getTotalSize(ArrayList listOfLists) {
        int resp = 0;

        for (int i = 0; i < listOfLists.size(); i++) {
            resp = resp + ((ArrayList) listOfLists.get(i)).size();
        }

        return resp;
    }

    // private static STRUCT // private static ArrayList getPolygonsEasily(FShape mpolygon) {
    private static STRUCT getMultiPolygonAsStruct(FShape mpol, int srid,
        boolean threed, IConnection _conn, boolean agu_bien, boolean hasSrid, boolean isgeo)
        throws SQLException {
        ArrayList all_ls = getPolygonsEasily(mpol, isgeo);
        Object[] hs = getHolesForShells(all_ls);
        ArrayList sh = (ArrayList) hs[0];
        ArrayList _ho = (ArrayList) hs[1];
        ArrayList ho = reverseHoles(_ho);

        return getMultiPolygonAsStruct(sh, ho, srid, threed, _conn, agu_bien, hasSrid);

    }

    private static ArrayList reverseHoles(ArrayList hh) {
        ArrayList resp = new ArrayList();

        for (int i = 0; i < hh.size(); i++) {
            ArrayList item = (ArrayList) hh.get(i);
            ArrayList newitem = new ArrayList();

            for (int j = 0; j < item.size(); j++) {
                LineString3D ls = (LineString3D) item.get(j);
                newitem.add(ls.createReverse());
            }

            resp.add(newitem);
        }

        return resp;
    }

    private static STRUCT getMultiPolygonAsStruct(ArrayList shells,
        ArrayList holes, int srid, boolean threed, IConnection _conn,
        boolean explicito, boolean hasSrid) throws SQLException {
        int t = 1003;

        if (explicito) {
            t = 2003;
        }

        int size = shells.size() + getTotalSize(holes);
        int geotype = 2003;
        if (size > 1) geotype = 2007;

        int dim = 2;

        if (threed) {
            geotype = geotype + 1000;
            dim = 3;
        }

        NUMBER[] indices = new NUMBER[3 * size];

        int acum = 0;
        int start_ind = 0;

        for (int i = 0; i < shells.size(); i++) {
            indices[start_ind] = new NUMBER(acum + 1);
            indices[start_ind + 1] = new NUMBER(1003);
            indices[start_ind + 2] = new NUMBER(1);
            start_ind = start_ind + 3;
            acum = acum +
                (dim * ((LineString3D) shells.get(i)).getLs().getNumPoints());

            ArrayList item_holes = (ArrayList) holes.get(i);

            for (int j = 0; j < item_holes.size(); j++) {
                indices[start_ind] = new NUMBER(acum + 1);
                indices[start_ind + 1] = new NUMBER(t); // 1003
                indices[start_ind + 2] = new NUMBER(1);
                start_ind = start_ind + 3;
                acum = acum +
                    (dim * ((LineString3D) item_holes.get(j)).getLs()
                            .getNumPoints());
            }
        }

        int _ind = 0;
        NUMBER[] ords = new NUMBER[acum];

        for (int i = 0; i < shells.size(); i++) {
            // --------------------------------
            LineString3D ls = (LineString3D) shells.get(i);
            int num_p = ls.getLs().getNumPoints();

            for (int j = 0; j < num_p; j++) {
                ords[_ind] = new NUMBER(ls.getLs().getCoordinateN(j).x);
                ords[_ind + 1] = new NUMBER(ls.getLs().getCoordinateN(j).y);

                if (threed) {
                    ords[_ind + 2] = new NUMBER(ls.getZc()[j]);
                }

                _ind = _ind + dim;
            }

            // -------------------------------
            ArrayList item_holes = (ArrayList) holes.get(i);

            for (int j = 0; j < item_holes.size(); j++) {
                ls = (LineString3D) item_holes.get(j);
                num_p = ls.getLs().getNumPoints();

                for (int k = 0; k < num_p; k++) {
                    ords[_ind] = new NUMBER(ls.getLs().getCoordinateN(k).x);
                    ords[_ind + 1] = new NUMBER(ls.getLs().getCoordinateN(k).y);

                    if (threed) {
                        ords[_ind + 2] = new NUMBER(ls.getZc()[k]);
                    }

                    _ind = _ind + dim;
                }
            }
        }

        STRUCT resp;
        StructDescriptor dsc = StructDescriptor.createDescriptor("MDSYS.SDO_GEOMETRY",
        		((ConnectionJDBC)_conn).getConnection());
        Object[] obj = new Object[5];
        obj[0] = new NUMBER(geotype);

        if (hasSrid) {
            obj[1] = new NUMBER(srid);
        }
        else {
            obj[1] = null;
        }

        obj[2] = null;
        obj[3] = indices;
        obj[4] = ords;

        // String ind_str = printArray(indices);
        // String ord_str = printArray(ords);
        // obj = getTestPolygon();
        resp = new STRUCT(dsc, ((ConnectionJDBC)_conn).getConnection(), obj);

        return resp;
    }

    private static Object[] getTestPolygon(boolean b) throws SQLException {
    	
        Object[] resp = new Object[5];
        resp[0] = new NUMBER(3005);
       	resp[1] = null;

       	NUMBER[] _ind = new NUMBER[6];
       	_ind[0] = new NUMBER(1.0);
       	_ind[1] = new NUMBER(2005.0);
       	_ind[2] = new NUMBER(1.0);
       	_ind[3] = new NUMBER(1.0);
       	_ind[4] = new NUMBER(2.0);
       	_ind[5] = new NUMBER(2.0);

       	NUMBER[] _ord = new NUMBER[33];
       	_ord[0] = new NUMBER(4478106.584);
       	_ord[1] = new NUMBER(5345524.3355);
       	_ord[2] = new NUMBER(0.0); 
       	_ord[3] = new NUMBER(4478103.55781412);
       	_ord[4] = new NUMBER(5345520.13869913);
       	_ord[5] = new NUMBER(0.0); 
       	_ord[6] = new NUMBER(4478094.627);
       	_ord[7] = new NUMBER(5345517.0485);
       	_ord[8] = new NUMBER(0.0 );
       	_ord[9] = new NUMBER(4478088.31210806);
       	_ord[10] = new NUMBER(5345521.30429374);
       	_ord[11] = new NUMBER(0.0 );
       	_ord[12] = new NUMBER(4478087.661);
       	_ord[13] = new NUMBER(5345528.8915);
       	_ord[14] = new NUMBER(0.0 );
       	_ord[15] = new NUMBER(4478093.20816592);
       	_ord[16] = new NUMBER(5345537.22495391);
       	_ord[17] = new NUMBER(0.0 );
       	_ord[18] = new NUMBER(4478103.219);
       	_ord[19] = new NUMBER(5345537.2515);
       	_ord[20] = new NUMBER(0.0 );
       	_ord[21] = new NUMBER(4478107.61580098);
       	_ord[22] = new NUMBER(5345529.07024585);
       	_ord[23] = new NUMBER(0.0 );
       	_ord[24] = new NUMBER(4478107.585);
       	_ord[25] = new NUMBER(5345528.688);
       	_ord[26] = new NUMBER(0.0 );
       	_ord[27] = new NUMBER(4478107.28586386);
       	_ord[28] = new NUMBER(5345526.46542887);
       	_ord[29] = new NUMBER(0.0 );
       	_ord[30] = new NUMBER(4478106.584);
       	_ord[31] = new NUMBER(5345524.3355);
       	_ord[32] = new NUMBER(0.0);
       	
        resp[2] = null;
        resp[3] = _ind;
        resp[4] = _ord;
    	
		return resp;
	}

	public static String printArray(NUMBER[] array) {
        String resp = "[ ";

        for (int i = 0; i < array.length; i++) {
            resp = resp + " " + array[i].doubleValue() + " , ";
        }

        resp = resp.substring(0, resp.length() - 2) + "]";

        return resp;
    }

    private static boolean isOneOf(int ind, int[] list) {
        for (int i = 0; i < list.length; i++) {
            if (list[i] == ind) {
                return true;
            }
        }

        return false;
    }

    /**
     * This method appends the geometries from a geometry collection in one STRUCT.
     *
     * @param co the geometry collection
     * @param _forced_type a type that has to be used as the struct's main type
     * @param _conn the connection
     * @param _o_srid the geometry's SRS (oracle code)
     * @param withSrid whether the SRS is non-NULL
     * @param agu_bien whether to check holes' validity
     * @param _isGeoCS whether the SRS is geodetic
     * @return the STRUCT with the appended geometries
     */
    public static STRUCT appendGeometriesInStruct(FGeometryCollection co,
        int _forced_type, IConnection _conn, String _o_srid, boolean withSrid,
        boolean agu_bien, boolean _isGeoCS) {
        IGeometry[] geoms = co.getGeometries();
        int size = geoms.length;
        STRUCT[] sts = new STRUCT[size];

        for (int i = 0; i < size; i++) {
            sts[i] = OracleSpatialDriver.iGeometryToSTRUCT(geoms[i],
                    _forced_type, _conn, _o_srid, withSrid, agu_bien, _isGeoCS);
        }

        if (size == 1) {
            return sts[0];
        }

        STRUCT aux = sts[0];

        for (int i = 1; i < size; i++) {
            aux = appendStructs(aux, sts[i], _conn);
        }

        return aux;
    }

    private static STRUCT appendStructs(STRUCT st1, STRUCT st2, IConnection _conn) {
        try {
            ARRAY _ords = (ARRAY) st1.getOracleAttributes()[4];
            int length_of_head_ords = _ords.getOracleArray().length;

            NUMBER gtype = new NUMBER(4 +
                    (((NUMBER) st1.getOracleAttributes()[0]).intValue() / 1000));
            NUMBER srid = (NUMBER) st1.getOracleAttributes()[1];
            NUMBER middle = (NUMBER) st1.getOracleAttributes()[2];

            ARRAY info1 = (ARRAY) st1.getOracleAttributes()[3];
            ARRAY info2 = (ARRAY) st2.getOracleAttributes()[3];
            ARRAY ords1 = (ARRAY) st1.getOracleAttributes()[4];
            ARRAY ords2 = (ARRAY) st2.getOracleAttributes()[4];

            Datum[] info = appendDatumArrays(info1.getOracleArray(),
                    info2.getOracleArray(), length_of_head_ords);

            Datum[] ords = appendDatumArrays(ords1.getOracleArray(),
                    ords2.getOracleArray(), 0);

            StructDescriptor dsc = st1.getDescriptor();

            Object[] atts = new Object[5];
            atts[0] = gtype;
            atts[1] = srid;
            atts[2] = middle;
            atts[3] = info;
            atts[4] = ords;

            STRUCT resp = new STRUCT(dsc, ((ConnectionJDBC)_conn).getConnection(), atts);

            return resp;
        }
        catch (SQLException sqle) {
            logger.error("While appending structs: " + sqle.getMessage(), sqle);
        }

        return null;
    }

    private static Datum[] appendDatumArrays(Datum[] head, Datum[] tail,
        int offset) {
        int head_l = head.length;
        int tail_l = tail.length;
        Datum[] resp = new Datum[head_l + tail_l];

        for (int i = 0; i < head_l; i++)
            resp[i] = head[i];

        if (offset == 0) {
            for (int i = 0; i < tail_l; i++)
                resp[head_l + i] = tail[i];
        }
        else {
            try {
                for (int i = 0; i < tail_l; i++) {
                    if ((i % 3) == 0) {
                        resp[head_l + i] = new NUMBER(tail[i].intValue() +
                                offset);
                    }
                    else {
                        resp[head_l + i] = tail[i];
                    }
                }
            }
            catch (SQLException se) {
                logger.error("Unexpected error: " + se.getMessage());
            }
        }

        return resp;
    }

    /**
     * Utility method to get an ineteger as a formatted string.
     *
     * @param n the integer
     * @return the formatted string
     */
    public static String getFormattedInteger(int n) {
        df.setGroupingUsed(true);
        df.setGroupingSize(3);
        dfs.setGroupingSeparator('.');
        df.setDecimalFormatSymbols(dfs);

        return df.format(n);
    }

    /**
     * Tells whether these arrays belong to a rectangle polygon.
     *
     * @param info the struct's element info array
     * @param ords the struct's coordinate array
     * @return true if it is a rectangle polygon. false otherwise.
     */
    public static boolean polStructIsRectStruct(ARRAY info, ARRAY ords) {
        try {
            int[] infos = info.getIntArray();

            return ((infos[2] == 3) && (infos.length == 3));
        }
        catch (SQLException se) {
            logger.error("While ckecking rectangle: " + se.getMessage(), se);
        }

        return false;
    }

    /**
     * Utility method to deal with oracle info arrays.
     */
    public static ARRAY getDevelopedInfoArray(ARRAY info) {
        ARRAY _resp = null;

        try {
            Datum[] resp = new Datum[3];
            Datum[] in = info.getOracleArray();
            resp[0] = in[0];
            resp[1] = in[1];
            resp[2] = new NUMBER(1);
            _resp = new ARRAY(info.getDescriptor(),
                    info.getInternalConnection(), resp);
        }
        catch (SQLException se) {
            logger.error("While creating ARRAY: " + se.getMessage(), se);
        }

        return _resp;
    }

    /**
     * Utility method to deal with oracle coordinate arrays.
     */
	public static ARRAY getDevelopedOrdsArray(ARRAY ords, int dim) {
		ARRAY _resp = null;

		try {
			int n = 5*2; // dim = 2, ignored dim, 3d makes no sense
			Datum[] resp = new Datum[n];
			Datum[] corners = ords.getOracleArray();

			for (int i=0; i<5; i++) {
				resp[i*2] = ((i==2) || (i==3)) ? corners[dim] : corners[0];
			}
			for (int i=0; i<5; i++) {
				resp[1+i*2] = ((i==1) || (i==2)) ? corners[dim+1] : corners[1];
			}
			_resp = new ARRAY(ords.getDescriptor(), ords
					.getInternalConnection(), resp);
		} catch (SQLException se) {
			logger.error("While creating ARRAY: " + se.getMessage(), se);
		}

		return _resp;
	}

    /**
     * utility method to convert a STRUCT into a GeneralPathX
     * @param aux the struct's datum array
     * @return the ExtendedGeneralPathX instance created
     */
    public static ExtendedGeneralPathX structToGPX(Datum[] aux) {
    	
    	ExtendedGeneralPathX resp = new ExtendedGeneralPathX();
        ARRAY infoARRAY = null;
        ARRAY ordsARRAY = null;
        Datum[] info_array = null;
        Datum[] ords_array = null;
        int info_array_size = 0;
        int[] start_ind;
        int[] end_ind;
        
        int dims = 0;
        boolean next_must_do_first = true;

        try {
            infoARRAY = (ARRAY) aux[3];
            ordsARRAY = (ARRAY) aux[4];

            dims = ((NUMBER) aux[0]).intValue() / 1000;

            if (dims == 0) {
                dims = 2;
            }

            if (polStructIsRectStruct(infoARRAY, ordsARRAY)) {
                infoARRAY = getDevelopedInfoArray(infoARRAY);
                ordsARRAY = getDevelopedOrdsArray(ordsARRAY, dims);
                dims = 2;
            }

            info_array = (Datum[]) infoARRAY.getOracleArray();
            ords_array = (Datum[]) ordsARRAY.getOracleArray();
            info_array_size = info_array.length / 3;

            int last_index = ords_array.length - dims + 1;

            // set indices:
            start_ind = new int[info_array_size];
            end_ind = new int[info_array_size];

            for (int i = 0; i < info_array_size; i++)
                start_ind[i] = ((NUMBER) info_array[3 * i]).intValue();

            for (int i = 0; i < (info_array_size - 1); i++)
                end_ind[i] = start_ind[i + 1] - 1;

            end_ind[info_array_size - 1] = last_index;

            int lineType = PathIterator.SEG_LINETO;

            if (end_ind[0] == 0) { // collection of paths

                for (int i = 1; i < info_array_size; i++) {
                    lineType = getLineToType(info_array, i);

                    // -----------------------
                    if (end_ind[i] == (start_ind[i] - 1))
                    	lineType = PathIterator.SEG_MOVETO;
                    // -----------------------

                    next_must_do_first = addOrdsToGPX(resp, start_ind[i] - 1,
                            end_ind[i] - 1, ords_array, dims, lineType,
                            (i == 1) || (lineType == PathIterator.SEG_MOVETO),
                            next_must_do_first);
                }
            } else {
            	
                // standard case, do the moveto always
                for (int i = 0; i < info_array_size; i++) {
                    lineType = getLineToType(info_array, i);
                    addOrdsToGPX(resp, start_ind[i] - 1, end_ind[i] - 1,
                        ords_array, dims, lineType, true, true);
                }
            }
        }
        catch (SQLException se) {
            logger.error("While creating GPX: " + se.getMessage(), se);
        }

        return resp;
    }

    private static int getLineToType(Datum[] infos, int i) {
        int resp = PathIterator.SEG_LINETO;

        try {
            if (((NUMBER) infos[(3 * i) + 2]).intValue() == 2) {
                resp = PathIterator.SEG_QUADTO;
            }
        }
        catch (SQLException e) {
            logger.error("While getting line-to type: " + e.getMessage() +
                " (returned SEG_LINETO)");
        }

        return resp;
    }

    private static boolean addOrdsToGPX(ExtendedGeneralPathX gpx, int zero_based_start,
        int zero_based_include_end, Datum[] ords, int d, int ltype,
        boolean do_the_move, boolean must_do_first) {
        int length = ords.length;
        boolean return_following_must_do_first = true;

        double x = ((NUMBER) ords[zero_based_start]).doubleValue();
        double y = ((NUMBER) ords[zero_based_start + 1]).doubleValue();

        if (must_do_first) {
            if (do_the_move) {
                gpx.moveTo(x, y);
            }
            else {
                gpx.lineTo(x, y);
            }
        }

        int ind = 1;

        int size = ((zero_based_include_end - zero_based_start) / d) + 1;
        int indx;
        int indx2;

        if (ltype == PathIterator.SEG_QUADTO) { // (interpretation = 2)

            double x2;
            double y2;

            while (ind < size) {
                indx = zero_based_start + (ind * d);
                x = ((NUMBER) ords[indx]).doubleValue();
                y = ((NUMBER) ords[indx + 1]).doubleValue();

                indx2 = zero_based_start + ((ind + 1) * d);

                if (indx >= length) {
                    indx2 = zero_based_start;
                }

                x2 = ((NUMBER) ords[indx2]).doubleValue();
                y2 = ((NUMBER) ords[indx2 + 1]).doubleValue();
                if (false) { // System.currentTimeMillis() % 2 == 0) {
                    gpx.quadTo(x, y, x2, y2);
                } else {
                    arcTo(gpx, x, y, x2, y2, 20);
                }
                gpx.setLinearized(true);
                ind++;
                ind++;
            }

            return_following_must_do_first = false;
        }
        else { // PathIterator.SEG_LINETO (interpretation = 1)

            while (ind < size) {
                indx = zero_based_start + (ind * d);
                x = ((NUMBER) ords[indx]).doubleValue();
                y = ((NUMBER) ords[indx + 1]).doubleValue();
                gpx.lineTo(x, y);
                ind++;
            }
        }

        return return_following_must_do_first;
    }

    private static void arcTo(
    		GeneralPathX gpx,
    		double x, double y,
			double x2, double y2,
			int ints) {
    	
    	Point2D p1 = gpx.getCurrentPoint();
    	// gpx.lineTo(p1.getX(), p1.getY());
    	
    	Point2D p2 = new Point2D.Double(x,y);
    	Point2D p3 = new Point2D.Double(x2,y2);
    	Point2D cent = UtilFunctions.getCenter(p1, p2, p3);
    	double r = p1.distance(cent);
		double angle_ini = UtilFunctions.getAngle(cent, p1);
		double angle_fin = UtilFunctions.getAngle(cent, p3);
		
		Coordinate[] coords = new Coordinate[4];
		coords[0] = new Coordinate(p1.getX(), p1.getY());
		coords[1] = new Coordinate(p2.getX(), p2.getY());
		coords[2] = new Coordinate(p3.getX(), p3.getY());
		coords[3] = new Coordinate(p1.getX(), p1.getY());

		double extent = 0;
		if (CGAlgorithms.isCCW(coords)) {
			if (angle_fin > angle_ini) {
				extent = angle_fin - angle_ini;
			} else {
				extent = 2 * Math.PI - (angle_ini - angle_fin);
			}
		} else {
			if (angle_fin > angle_ini) {
				extent = -(2 * Math.PI - (angle_fin - angle_ini));
			} else {
				extent = -(angle_ini - angle_fin);
			}
		}
		double anginc = extent / 20;
    	for (int i=0; i<ints; i++) {
    		gpx.lineTo(
    				cent.getX() + r * Math.cos(angle_ini+i*anginc),
    				cent.getY() + r * Math.sin(angle_ini+i*anginc));
    	}
		gpx.lineTo(x2,y2);
	}

	/**
     * Utility method. Gets FShape type from oracle geometry type.
     * @param otype
     * @return FShape type
     */
    public static int oracleGTypeToFShapeType(int otype) {
        switch (otype) {
        case ORACLE_GTYPE_UNKNOWN:
            return FShape.NULL;

        case ORACLE_GTYPE_POINT:
        case ORACLE_GTYPE_MULTIPOINT:
            return FShape.POINT;

        case ORACLE_GTYPE_LINE:
        case ORACLE_GTYPE_MULTILINE:
            return FShape.LINE;

        case ORACLE_GTYPE_POLYGON:
        case ORACLE_GTYPE_MULTIPOLYGON:
            return FShape.POLYGON;

        case ORACLE_GTYPE_COLLECTION:
            return FShape.MULTI;
        }

        logger.warn("Unknown oracle geometry type: " + otype);

        return FShape.NULL;
    }

    /**
     * Utility method to get struct's type.
     * @param the_data the struct's datum array
     * @return the struct type
     */
    public static int getStructType(Datum[] the_data) {
        int resp = -1;

        try {
            resp = ((NUMBER) the_data[0]).intValue() % 1000;
        }
        catch (SQLException se) {
            logger.error("Error: " + se.getMessage(), se);
        }

        return resp;
    }

    /**
     * Utility method to get struct's SRID.
     * @param the_data the struct's datum array
     * @return the struct0's SRID
     */
    public static int getStructSRID(Datum[] the_data) {
        int resp = -1;

        try {
            resp = ((NUMBER) the_data[1]).intValue();
        }
        catch (SQLException se) {
            logger.error("Error: " + se.getMessage(), se);
        }

        return resp;
    }

    /**
     * Utility method to find out if  a struct is a circle.
     *
     * @param the_data the struct's datum array
     * @return whether it is a circle
     */
    public static boolean isCircle(Datum[] the_data) {
        int[] info = null;

        try {
            info = ((ARRAY) the_data[3]).getIntArray();
        }
        catch (SQLException se) {
            logger.error("While cheking circle: " + se.getMessage(), se);

            return false;
        }

        if (info == null) {
            return false;
        }

        boolean resp = ((info.length == 3) && (info[2] == 4));

        return resp;
    }

    /**
     * Gets the struct's dimension size.
     * @param st the struct
     * @return the structs dimension
     */
    public static int getStructDimensions(STRUCT st) {
        int resp = -1;

        try {
            resp = ((NUMBER) st.getOracleAttributes()[0]).intValue() / 1000;
        }
        catch (SQLException se) {
            logger.error("Error: " + se.getMessage(), se);
        }

        if (resp < 2) {
            resp = 2;
        }

        return resp;
    }

    /**
     * Gets a struct's coordinates array.
     * @param the_data the struct's datum array
     * @return the coordinates array
     */
    public static double[] getOrds(Datum[] the_data) {
        double[] resp = null;

        try {
            ARRAY aux = (ARRAY) the_data[4];

            if (aux == null) {
                return null;
            }

            resp = aux.getDoubleArray();
        }
        catch (SQLException se) {
            logger.error("While getting ordinates: " + se.getMessage(), se);
        }

        return resp;
    }

    /**
     * Utility method to create a struct with the given data.
     * @param type struct type
     * @param srid coordinate system
     * @param info element info array
     * @param ords coordinates array
     * @param conn connection
     * @return the created struct
     */
    public static STRUCT createStruct(NUMBER type, NUMBER srid, Datum[] info,
        Datum[] ords, Connection conn) {
        try {
            StructDescriptor dsc = StructDescriptor.createDescriptor("MDSYS.SDO_GEOMETRY",
                    conn);
            Object[] obj = new Object[5];
            obj[0] = type;
            obj[1] = srid;
            obj[2] = null;
            obj[3] = info;
            obj[4] = ords;

            return new STRUCT(dsc, conn, obj);
        }
        catch (SQLException se) {
            logger.error("While creating STRUCT: " + se.getMessage(), se);
        }

        return null;
    }

    public static String getDimInfoAsString(ARRAY dim_info) {
    	String resp = "DIMENSIONS: ";

        if (dim_info == null) {
            return "NULL" + "\n";
        }
        else {
        	try {
				Datum[] da = dim_info.getOracleArray();
				int size = da.length;
				resp = resp + size + "\n";
				for (int i = 0; i < size; i++) {
					STRUCT dim_itemx = (STRUCT) da[i];
					Object[] dim_desc = dim_itemx.getAttributes();
					resp = resp + "DIMENSION " + i + ": " + ", NAME: "
							+ dim_desc[0].toString() + ", MIN: "
							+ dim_desc[1].toString() + ", MAX: "
							+ dim_desc[2].toString() + ", TOL: "
							+ dim_desc[3].toString();
					if (i != (size -1)) {
						resp = resp + "\n";
					}
				}
			} catch (Exception ex) {
				return "ERROR: " + ex.getMessage() + "\n";
			}
        }
        return resp;
    }

    public static STRUCT reprojectGeometry(IConnection conn, STRUCT fromStruct, String toSrid) {

    	String qry = "SELECT SDO_CS.TRANSFORM( ?, " + toSrid + ") FROM DUAL";
    	STRUCT resp = null;

    	try {
			PreparedStatement _st = ((ConnectionJDBC)conn).getConnection().prepareStatement(qry);
			_st.setObject(1, fromStruct);
			ResultSet _rs = _st.executeQuery();

			if (_rs.next()) {
				resp = (STRUCT) _rs.getObject(1);
			} else {
				logger.error("While executing reprojection: empty resultset (?)");
				return fromStruct;
			}
		} catch (Exception ex) {
			logger.error("While reprojecting: " + ex.getMessage());
			return fromStruct;
		}

        if (resp == null) {
        	return fromStruct;
        } else {
        	return resp;
        }
    }
    
    
    public static void printStruct(STRUCT st) {
    	
        logger.debug("----------------------------------------------");
        logger.debug("-- 16 FEBRERO 2009 ---------------------------");
        logger.debug("----------------------------------------------");

        try {
            Object[] att = st.getAttributes();
            int l = att.length;

            for (int i = 0; i < l; i++) {
            	if (att[i] != null) {
            		if (att[i] instanceof ARRAY) {
            			ARRAY arr = (ARRAY) att[i];
            			logger.debug("ATT " + i + ": ");
            			printARRAY(arr);
            		} else {
            			logger.debug("ATT " + i + ": " + att[i].toString());
            		}
                    logger.debug("----------------------------------------------");
            	}
            }
        }
        catch (Exception ex) {
        	logger.debug("-- Error: " + ex.getMessage());
        }

    }

	private static void printARRAY(ARRAY arr) throws Exception {
		
		int[] intarr = arr.getIntArray();
		if (intarr == null) {
			float[] floarr = arr.getFloatArray();
			if (floarr == null) {
				logger.debug("INT NULL y FLOAT NULL (?)");
			} else {
				int len = floarr.length;
				for (int i=0; i<len; i++) {
					if (Math.min(i, (len - i)) < 20) {
						logger.debug("" + floarr[i]);
					}
				}
			}
			
		} else {
			int len = intarr.length;
			for (int i=0; i<len; i++) {
				if (Math.min(i, (len - i)) < 20) {
					logger.debug("" + intarr[i]);
				}
			}
		}
	}

    /**
     * Utility method. Gets FShape type from oracle geometry type.
     * @param otype
     * @return FShape type
     */
    public static int oracleGTypeToFShapeType(int full_otype, boolean complex) {
    	
    	int resp = FShape.NULL;
    	int simpl_otype = full_otype % 1000;
    	
    	if (complex) {
    		
    		switch (simpl_otype) {
    		case ORACLE_GTYPE_COMPLEX_VOIDED_OR_NORMAL_POLYGON:
    		case ORACLE_GTYPE_COMPLEX_COMPOUND_POLYGON:
    			resp = FShape.POLYGON;
    			break;
    		case ORACLE_GTYPE_COMPLEX_COMPOUND_LINE:
    			resp = FShape.LINE;
    			break;
    		default:
    			return oracleGTypeToFShapeType(full_otype, false);
    		}

    	} else {

    		// =========== not complex =================
            switch (simpl_otype) {
            case ORACLE_GTYPE_UNKNOWN:
            	resp = FShape.NULL;
                break;

            case ORACLE_GTYPE_POINT:
            case ORACLE_GTYPE_MULTIPOINT:
            	resp = FShape.POINT;
                break;

            case ORACLE_GTYPE_LINE:
            case ORACLE_GTYPE_MULTILINE:
            	resp = FShape.LINE;
                break;

            case ORACLE_GTYPE_POLYGON:
            case ORACLE_GTYPE_MULTIPOLYGON:
            	resp = FShape.POLYGON;
                break;

            case ORACLE_GTYPE_COLLECTION:
            	resp = FShape.MULTI;
                break;
            }
    		// =========== not complex =================
    	}
    	if (resp == FShape.NULL) {
    		logger.error("Unknown oracle geometry type: " + full_otype);
    	}
        return resp;
    }
    
	public static void removeStructFields(DBLayerDefinition def, String[] arr) {
		
		FieldDescription[] flds = def.getFieldsDesc();
		ArrayList aux = new ArrayList();
		
		for (int i=0; i<flds.length; i++) {
			if (!isOneOfThese(flds[i].getFieldName(), arr)) {
				aux.add(flds[i]);
			}
		}
		
		FieldDescription[] flds_new =
			(FieldDescription[]) aux.toArray(new FieldDescription[0]);
		def.setFieldsDesc(flds_new);
	}
	
	private static boolean isOneOfThese(String name, String[] arr) {

		for (int i=0; i<arr.length; i++) {
				if (arr[i].compareToIgnoreCase(name) == 0) return true; 
			}
		return false;
	}
	
	public static void setUpperCase(DBLayerDefinition def) {
		String aux = def.getCatalogName();
		if (aux != null) def.setCatalogName(aux.toUpperCase());

		aux = def.getSchema();
		if (aux != null) def.setSchema(aux.toUpperCase());
	}
	
    public static boolean hasSeveralGeometryTypes(ArrayList tt, boolean are_dims) {
        if (tt.size() == 0) {
            return false;
        }

        HashMap m = new HashMap();

        for (int i = 0; i < tt.size(); i++) {
            Integer integ = (Integer) tt.get(i);
            int val = integ.intValue();

            if ((val == 4) && (!are_dims)) {
                return true;
            }

            m.put("" + (val % 4), "a type");
        }

        Iterator iter = m.keySet().iterator();
        iter.next();

        return iter.hasNext();
    }

    public static void showMemory() {
        Runtime r = Runtime.getRuntime();
        long mem = r.totalMemory() - r.freeMemory();
        logger.info("Total memory : " + mem);
    }
    

    
    private static double[] getIndDoublesModule(double[] input, int ind, int n) {
        int size = input.length / n;
        double[] resp = new double[size];

        for (int i = 0; i < size; i++) {
            resp[i] = input[(i * n) + ind];
        }

        return resp;
    }
    
    private static double[] getIndBigDecimalModule(double[] input, int ind, int n) {
        int size = input.length / n;
        double[] resp = new double[size];

        for (int i = 0; i < size; i++) {
            resp[i] = input[(i * n) + ind];
        }

        return resp;
    }
    
    public static IGeometry getFMapGeometryMultipolygon(Datum[] the_data, int dim) {
        IGeometry ig = null;

        if (OracleSpatialUtils.isCircle(the_data)) {
            ig = getCircleFromStruct(the_data);
        }
        else {
        	ExtendedGeneralPathX gpx = OracleSpatialUtils.structToGPX(the_data);

            if (dim == 2) {
                ig = ShapeFactory.createPolygon2D(gpx);
            }
            else {
                double[] ords = null;

                try {
                    ords = ((ARRAY) the_data[4]).getDoubleArray();
                } catch (SQLException se) {
                    logger.error("While getting ordinates: " + se.getMessage(), se);
                }

                double[] z = null;
                
                if (gpx.isLinearized()) {
                	int count = countCoords(gpx);
                	z = new double[count];
                	logger.warn("Linearized a 3D GPX, z[i] = 0");
                } else {
                	z = getIndBigDecimalModule(ords, 2, dim);
                }
                
                ig = ShapeFactory.createPolygon3D(gpx, z);
            }
        }

        return ig;
    }
    
    private static IGeometry getCircleFromStruct(Datum[] the_data) {
        double[] threep = null;

        try {
            threep = ((ARRAY) the_data[4]).getDoubleArray();
        }
        catch (SQLException se) {
            logger.error("While getting ords from struct: " + se.getMessage(),
                se);

            return new FNullGeometry();
        }

        Point2D[] three = new Point2D.Double[3];
        three[0] = new Point2D.Double(threep[0], threep[1]);
        three[1] = new Point2D.Double(threep[2], threep[3]);
        three[2] = new Point2D.Double(threep[4], threep[5]);

        Object[] cent_rad = OracleSpatialUtils.getCenterAndRadiousOfCirc(three);

        Point2D cent = (Point2D) cent_rad[0];
        double radius = ((Double) cent_rad[1]).doubleValue();

        IGeometry circ = ShapeFactory.createCircle(cent, radius);

        return circ;
    }
    
    public static IGeometry getFMapGeometryMultiLineString(Datum[] the_data, int dim) {
    	ExtendedGeneralPathX gpx = OracleSpatialUtils.structToGPX(the_data);
        IGeometry ig = null;
        double[] ords = null;

        if (dim == 2) {
            ig = ShapeFactory.createPolyline2D(gpx);
        }
        else {
            ords = OracleSpatialUtils.getOrds(the_data);

            double[] z = null;
            
            if (gpx.isLinearized()) {
            	int count = countCoords(gpx);
            	z = new double[count];
            	logger.warn("Linearized a 3D GPX, z[i] = 0");
            } else {
            	z = getIndBigDecimalModule(ords, 2, dim);
            }
            ig = ShapeFactory.createPolyline3D(gpx, z);
        }

        return ig;
    }

    
    private static int countCoords(GeneralPathX gpx) {
    	
    	int resp = 0;
    	PathIterator piter = gpx.getPathIterator(null);
    	while (!piter.isDone()) {
    		piter.next();
    		resp++;
    	}
		return resp;
	}

	public static IGeometry getFMapGeometryPoint(Datum[] the_data, int dim) {
        double[] ords = OracleSpatialUtils.getOrds(the_data);

        if (ords == null) { // sdo_point

            return getFMapGeometrySdoPoint(the_data, dim);
        }

        IGeometry ig = null;
        int total_size = ords.length;
        int no_po = total_size / dim;
        double[] x = new double[no_po];
        double[] y = new double[no_po];
        double[] z = new double[no_po];

        for (int i = 0; i < no_po; i++) {
            x[i] = ords[i * dim]; // pp[i].getX();
            y[i] = ords[(i * dim) + 1];

            if (dim >= 3) {
                z[i] = ords[(i * dim) + 2];
            }
        }

        if (dim == 2) {
            if (no_po == 1) {
                ig = ShapeFactory.createPoint2D(x[0], y[0]);
            }
            else {
                ig = ShapeFactory.createMultipoint2D(x, y);
            }
        }
        else {
            if (no_po == 1) {
                ig = ShapeFactory.createPoint3D(x[0], y[0], z[0]);
            }
            else {
                ig = ShapeFactory.createMultipoint3D(x, y, z);
            }
        }

        return ig;
    }
    
    private static IGeometry getFMapGeometrySdoPoint(Datum[] the_data, int d) {
        double x = 0;
        double y = 0;
        double z = 0;

        try {
            Datum[] aux = ((STRUCT) the_data[2]).getOracleAttributes();
            x = ((NUMBER) aux[0]).doubleValue();
            y = ((NUMBER) aux[1]).doubleValue();

            if (d > 2) {
            	if (aux[2] == null) {
            		z = 0;
            	} else {
            		z = ((NUMBER) aux[2]).doubleValue();
            	}
            }
        }
        catch (SQLException se) {
            logger.error("While getting sdo point ordinates: " +
                se.getMessage(), se);
        }

        IGeometry ig = null;

        if (d == 2) {
            ig = ShapeFactory.createPoint2D(x, y);
        }
        else {
            ig = ShapeFactory.createPoint3D(x, y, z);
        }

        return ig;
    }

    public static final int COLLECTION_VALUE_NOT_COLLECTION = 0;
    public static final int COLLECTION_VALUE_YES_COLLECTION = 1;
    public static final int COLLECTION_VALUE_MERGE_COLLECTION_IN_POLYGON = 2;
    
    public static int isCollection(Datum[] the_data) {
        int[] info = null;

        try {
            ARRAY aux = (ARRAY) the_data[3];

            if (aux == null) {
                return COLLECTION_VALUE_NOT_COLLECTION;
            }

            info = aux.getIntArray();
        }
        catch (SQLException se) {
            logger.error("While checking collection: " + se.getMessage());
            return COLLECTION_VALUE_NOT_COLLECTION;
        }

        if (info == null) {
            return COLLECTION_VALUE_NOT_COLLECTION; // sdo_point
        }

        int size = info.length / 3;

        if (size == 1) {
            return COLLECTION_VALUE_NOT_COLLECTION;
        }

        if (size == 2) {
        	if (((info[1] == 1003) && (info[2] == 3 || info[2] == 4))
        			|| ((info[4] == 1003 || info[4] == 2003) && (info[5] == 3 || info[5] == 4))) {
        		return COLLECTION_VALUE_MERGE_COLLECTION_IN_POLYGON;
        	} else {
        		boolean aux = ((info[1] % 1000) != (info[4] % 1000))
                && ( ! ((info[1] == 1005) && (info[4] == 2)) );
        		if (aux) {
                    return COLLECTION_VALUE_YES_COLLECTION;
        		} else {
                    return COLLECTION_VALUE_NOT_COLLECTION;
        		}
        	}
        }
        
        // ======================================== check no rects whsn size > 2
        for (int i=0; i<size; i++) { // outer pol
        	if ((info[i*3+1] == 1003) && (info[i*3+2] == 3 || info[i*3+2] == 4)) {
        		return COLLECTION_VALUE_MERGE_COLLECTION_IN_POLYGON; 
        	}
        }
        for (int i=1; i<size; i++) { // inner pol
        	if ((info[i*3+1] == 2003) && (info[i*3+2] == 3 || info[i*3+2] == 4)) {
        		return COLLECTION_VALUE_MERGE_COLLECTION_IN_POLYGON; 
        	}
        }
        // =================================== 

        int _first = info[1] % 1000;
        int second = info[4] % 1000;
        int item = 0;
        
        for (int i = 2; i < size; i++) {
        	item = info[(i * 3) + 1] % 1000;
            if ((item != second) &&
            		( ! ((item == 5) && (second == 2)) ) && 
            		( ! ((item == 2) && (second == 5)) )
            		) {
                return COLLECTION_VALUE_YES_COLLECTION;
            }
        }

        return COLLECTION_VALUE_NOT_COLLECTION;
    }

    
    public static Datum[] updateIndexes(Datum[] info) {
        int size = info.length / 3;
        NUMBER[] resp = new NUMBER[3 * size];

        try {
            int rest = info[0].intValue() - 1;

            for (int i = 0; i < size; i++) {
                resp[3 * i] = new NUMBER(info[3 * i].intValue() - rest);
                resp[(3 * i) + 1] = new NUMBER(info[(3 * i) + 1].intValue());
                resp[(3 * i) + 2] = new NUMBER(info[(3 * i) + 2].intValue());
            }
        }
        catch (SQLException se) {
            logger.error("Unexpected error: " + se.getMessage());
        }

        return resp;
    }
    
    public static double[] getSubSet(double[] all, int first_inc, int last_inc) {
        double[] resp = new double[last_inc - first_inc + 1];

        for (int i = first_inc; i <= last_inc; i++) {
            resp[i - first_inc] = all[i];
        }

        return resp;
    }
    
    public static Object[] getOrdOfGroups(Datum[] all, Object[] groups) throws SQLException {
        Object[] resp = new Object[groups.length];

        if (resp.length == 1) {
            resp[0] = all;

            return resp;
        }

        int ind = 0;
        Datum[] aux = (Datum[]) groups[1];
        int _end = aux[0].intValue() - 2;
        Datum[] ord_aux = getSubSet(all, 0, _end);

        int _start = _end + 1;
        resp[ind] = ord_aux;
        ind++;

        for (int i = 2; i < groups.length; i++) {
            aux = (Datum[]) groups[i];
            _end = aux[0].intValue() - 2;
            ord_aux = getSubSet(all, _start, _end);
            resp[ind] = ord_aux;
            ind++;
            _start = _end + 1;
        }

        // last
        _end = all.length - 1;
        ord_aux = getSubSet(all, _start, _end);
        resp[groups.length - 1] = ord_aux;

        return resp;
    }
    
    
    
    public static Object[] getOrdOfGroups(double[] all, Object[] groups) {
        Object[] resp = new Object[groups.length];

        if (resp.length == 1) {
            resp[0] = all;

            return resp;
        }

        int ind = 0;
        int[] aux = (int[]) groups[1];
        int _end = aux[0] - 2;
        double[] ord_aux = getSubSet(all, 0, _end);

        int _start = _end + 1;
        resp[ind] = ord_aux;
        ind++;

        for (int i = 2; i < groups.length; i++) {
            aux = (int[]) groups[i];
            _end = aux[0] - 2;
            ord_aux = getSubSet(all, _start, _end);
            resp[ind] = ord_aux;
            ind++;
            _start = _end + 1;
        }

        // last
        _end = all.length - 1;
        ord_aux = getSubSet(all, _start, _end);
        resp[groups.length - 1] = ord_aux;

        return resp;
    }
    
    
    public static Object[] groupByElement(int[] all_elem) {
        ArrayList resp = new ArrayList();

        int size = all_elem.length / 3;

        int[] aux = getNthGroupOfThree(all_elem, 0);

        int[] newaux;
        int i = 1;

        while (i < size) {
            newaux = getNthGroupOfThree(all_elem, i);

            if (newaux[0] == aux[0]) {
                // aux[2] says how many components
                for (int j = 0; j < aux[2]; j++) {
                    aux = appendIntArrays(aux,
                            getNthGroupOfThree(all_elem, j + i));
                }

                resp.add(aux);
                i = i + aux[2];
                aux = getNthGroupOfThree(all_elem, i);
            }
            else {
                if (newaux[1] == 2003) {
                    aux = appendIntArrays(aux, newaux);
                }
                else {
                    resp.add(aux);
                    aux = getNthGroupOfThree(all_elem, i);
                }
            }

            i++;
        }

        resp.add(aux);

        return resp.toArray();
    }
    
    public static boolean isSimpleCollectionOfLines(Datum[] all_elem) {
    	
    	try {
        	int size = all_elem.length;
        	if (all_elem[1].intValue() != 4) return false;
        	int size3 = size / 3;
        	
        	for (int i=1; i<size3; i++) {
        		if (all_elem[3 * i + 1].intValue() != 2) return false; 
        	}
        	return true;
        	
    	} catch (SQLException ex) {
    		logger.error("While is simple line collection: " + ex.getMessage());
    	}
    	
    	return false;
    }
    
    public static Datum[] removeThreeFirst(Datum[] elem) {
    	int sz = elem.length;
    	Datum[] resp = new Datum[sz - 3];
    	for (int i=3; i<sz; i++) resp[i - 3] = elem[i];
    	return resp;
    }
    
    public static Object[] groupByElement(Datum[] all_elem) {
    	
    	if (isSimpleCollectionOfLines(all_elem)) {
    		Object[] r = new Object[1];
    		r[0] = removeThreeFirst(all_elem);
    		return r;
    	}
    	
        ArrayList resp = new ArrayList();

        int size = all_elem.length / 3;

        Datum[] aux = getNthGroupOfThree(all_elem, 0);

        Datum[] newaux;
        int i = 1;
        boolean add_last_time = true;

        try {
            while (i < size) {
                newaux = getNthGroupOfThree(all_elem, i);

                if (newaux[0].intValue() == aux[0].intValue()) {
                    // aux[2] says how many components
                    for (int j = 0; j < ((NUMBER) aux[2]).intValue(); j++) {
                        aux = appendDatArrays(aux,
                                getNthGroupOfThree(all_elem, j + i));
                    }

                    resp.add(aux);
                    i = i + ((NUMBER) aux[2]).intValue();
                    if (i < size) { // in some cases (line collection, 4)
                    	aux = getNthGroupOfThree(all_elem, i);
                    } else {
                    	add_last_time = false;
                    }
                }
                else {
                	
                    resp.add(aux);
                    aux = getNthGroupOfThree(all_elem, i);
                    // no complex subelements expected
                    /*
                    if (((NUMBER) newaux[1]).intValue() == 1003
                    		|| ((NUMBER) newaux[1]).intValue() == 2003) {
                        resp.add(aux);
                        aux = getNthGroupOfThree(all_elem, i);
                    } else {
                        aux = appendDatArrays(aux, newaux);
                    }
                    */
                }

                i++;
            }
        }
        catch (SQLException se) { 
            logger.error("Unexpected error: " + se.getMessage());
        }

        if (add_last_time) {
        	resp.add(aux);
        }

        return resp.toArray();
    }
    
    
    
    public static Geometry shapeToGeometry(Shape shp) {
        if (shp == null) {
            return null;
        }

        int type = FShape.POLYGON;

        if ((shp instanceof FPolyline2D) && (!(shp instanceof FPolygon2D))) {
            type = FShape.LINE;
        }

        if (shp instanceof FPoint2D) {
            type = FShape.POINT;
        }

        if (shp instanceof FMultiPoint2D) {
            type = FShape.MULTIPOINT;
        }

        GeneralPathX wagp = new GeneralPathX(shp);
        FShapeGeneralPathX fwagp = new FShapeGeneralPathX(wagp, type);

        return FConverter.java2d_to_jts(fwagp);
    }
    
    public static STRUCT rectangleToStruct(Rectangle2D r, boolean hasSrid,
            boolean isView, boolean _isGeogCS, String _oracleSRID, IConnection __conn) {
            Point2D c1 = new Point2D.Double(r.getMinX(), r.getMinY());
            Point2D c2 = new Point2D.Double(r.getMaxX(), r.getMaxY());

            if ((_isGeogCS) && (isView)) {
            	// absurd cases for geodetic (user chose wrong epsg when loading the table) 
            	if (r.getMinX() >= 180) {
                	c1.setLocation(179, 0);
                	c2.setLocation(180, 1);
            	}
            	
            	if (r.getMaxX() <= -180) {
                	c1.setLocation(-180, 0);
                	c2.setLocation(-179, 1);
            	}
            	
            	if (r.getMinY() >= 90) {
                	c1.setLocation(0, 89);
                	c2.setLocation(1, 90);
            	}
            	
            	if (r.getMaxY() <= -90) {
                	c1.setLocation(0, -90);
                	c2.setLocation(1, -89);
            	}
            }

            STRUCT resp = null;

            try {
                // System.out.println("ABIERTA: " + (!conn.isClosed()));
                // resp = structCreator.toSTRUCT(rect_wkt.getBytes(), conn);
                // Object[] old_obj = resp.getAttributes();
                int size = 5;
                Object[] new_obj = new Object[size];

                // for (int i=0; i<size; i++) new_obj[i] = old_obj[i];
                new_obj[0] = new NUMBER(2003);

                if (hasSrid) {
                    new_obj[1] = new NUMBER(_oracleSRID);
                }
                else {
                    new_obj[1] = null;
                }

                new_obj[2] = null;

                NUMBER[] elem_info = new NUMBER[3];
                elem_info[0] = new NUMBER(1);
                elem_info[1] = new NUMBER(1003);
                elem_info[2] = new NUMBER(3);
                new_obj[3] = elem_info;

                NUMBER[] ords = null;
                ords = new NUMBER[4];
                ords[0] = new NUMBER(c1.getX());
                ords[1] = new NUMBER(c1.getY());
                ords[2] = new NUMBER(c2.getX());
                ords[3] = new NUMBER(c2.getY());
                new_obj[4] = ords;

                // StructDescriptor dsc = StructDescriptor.createDescriptor("STRUCT", conn);
                StructDescriptor dsc = StructDescriptor.createDescriptor("MDSYS.SDO_GEOMETRY",
                		((ConnectionJDBC)__conn).getConnection());

                resp = new STRUCT(dsc,((ConnectionJDBC)__conn).getConnection(), new_obj);
            }
            catch (Exception ex) {
                logger.error("Error while creating rect struct: " +
                    ex.getMessage(), ex);
            }

            return resp;
        }
    
    
    public static Rectangle2D doIntersect(Rectangle2D r1, Rectangle2D r2) {
        if (r1.getMaxX() <= r2.getMinX()) {
            return null;
        }

        if (r2.getMaxX() <= r1.getMinX()) {
            return null;
        }

        if (r1.getMaxY() <= r2.getMinY()) {
            return null;
        }

        if (r2.getMaxY() <= r1.getMinY()) {
            return null;
        }

        double minx = Math.max(r1.getMinX(), r2.getMinX());
        double miny = Math.max(r1.getMinY(), r2.getMinY());
        double maxx = Math.min(r1.getMaxX(), r2.getMaxX());
        double maxy = Math.min(r1.getMaxY(), r2.getMaxY());

        double w = maxx - minx;
        double h = maxy - miny;

        return new Rectangle2D.Double(minx, miny, w, h);
    }
    
    
    /**
     * Utility method to find out if a coordinate system is geodetic or not.
     *
     * @param oracleSRID2 the coordinate system's oracle code
     * @param thas whether the table has a coordinate system set.
     * if not, the method returns false.
     * @return whether the coordinate system is geodetic or not.
     */
    public static boolean getIsGCS(String oracleSRID2, boolean thas) {

        if (!thas) return false;
        if (oracleSRID2 == null) return false;
        
        int ora_cs = 0;

        try {
            ora_cs = Integer.parseInt(oracleSRID2);
        }
        catch (Exception ex) {
            return false;
        }

        if (((ora_cs >= 8000) && (ora_cs <= 8999)) || (ora_cs == 524288)) {
            return true;
        } else {
        	return false;
        }
    }
    
    
    public static int getShapeTypeOfStruct(STRUCT sample) throws SQLException {

        int code = ((NUMBER) sample.getOracleAttributes()[0]).intValue();
        
        int type_part = code % 10;
        int dim_part = code / 1000;

        int z_added = 0;
        if (dim_part == 3) {
        	z_added = FShape.Z;
        } else {
            if (dim_part == 4) {
            	z_added = FShape.Z | FShape.M;
            }
        }

        switch (type_part) {
        case 1:
            return z_added + FShape.POINT;

        case 2:
            return z_added + FShape.LINE;

        case 3:
            return z_added + FShape.POLYGON;

        case 4:
            return z_added + FShape.MULTI;

        case 5:
            return z_added + FShape.MULTIPOINT;

        case 6:
            return z_added + FShape.LINE;

        case 7:
            return z_added + FShape.POLYGON;
        }

        logger.error("Unknown geometry type: " + code);

        return FShape.NULL;
    }
    
    public static IGeometry NULL_GEOM = new FNullGeometry();
    

    
    
    public static int[] updateIndexes(int[] info) {
        int size = info.length / 3;
        int[] resp = new int[3 * size];
        int rest = info[0] - 1;

        for (int i = 0; i < size; i++) {
            resp[3 * i] = info[3 * i] - rest;
            resp[(3 * i) + 1] = info[(3 * i) + 1];
            resp[(3 * i) + 2] = info[(3 * i) + 2];
        }

        return resp;
    }
    
    

    public static int[] appendIntArrays(int[] head, int[] tail) {
        int[] resp = new int[head.length + tail.length];
        int hsize = head.length;

        for (int i = 0; i < hsize; i++) {
            resp[i] = head[i];
        }

        for (int i = 0; i < tail.length; i++) {
            resp[hsize + i] = tail[i];
        }

        return resp;
    }
    
    
    public static Datum[] appendDatArrays(Datum[] head, Datum[] tail) {
        Datum[] resp = new Datum[head.length + tail.length];
        int hsize = head.length;

        for (int i = 0; i < hsize; i++) {
            resp[i] = head[i];
        }

        for (int i = 0; i < tail.length; i++) {
            resp[hsize + i] = tail[i];
        }

        return resp;
    }

    public static int[] getNthGroupOfThree(int[] list, int n) {
        int[] resp = new int[3];
        resp[0] = list[3 * n];
        resp[1] = list[(3 * n) + 1];
        resp[2] = list[(3 * n) + 2];

        return resp;
    }

    public static Datum[] getNthGroupOfThree(Datum[] list, int n) {
        Datum[] resp = new Datum[3];
        resp[0] = list[3 * n];
        resp[1] = list[(3 * n) + 1];
        resp[2] = list[(3 * n) + 2];

        return resp;
    }

    public static Datum[] getSubSet(Datum[] all, int first_inc, int last_inc) {
        Datum[] resp = new Datum[last_inc - first_inc + 1];

        for (int i = first_inc; i <= last_inc; i++) {
            resp[i - first_inc] = all[i];
        }

        return resp;
    }
    
    
    public static int maxSizeForFieldType(int _type) {
        switch (_type) {
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
            return OracleSpatialDriver.VARCHAR2_MAX_SIZE;
        }

        return -1;
    }
    
    
    
    public static String EXPONENTIAL_INDICES_CONDITION = null;
    
    static {
    	
    	String sb = "";
    	int i=0;
    	sb = "(rownum = 1)";
    	for (i=2; i<20; i++) {
    		sb = "(" + sb + " OR (rownum = " + i + "))";
    	}
    	int cnt = 0;
    	float aux = 1;
    	while (cnt < 35) {
    		aux = aux * 1.5f;
    		i = 20 + Math.round(aux);
    		sb = "(" + sb + " OR (rownum = " + i + "))";
    		cnt++;
    	}
		aux = aux * 1.5f;
		i = 20 + Math.round(aux);
		sb = "(" + sb + " OR (rownum = " + i + "))";
		EXPONENTIAL_INDICES_CONDITION = sb;
    }

	public static IGeometry makeLinear(FPolygon2D shp) {
		
		
		if (shp instanceof FPolygon3D) {
			
			double[] z = ((FPolygon3D) shp).getZs();
			PathIterator piter = shp.getPathIterator(null);
			GeneralPathX gpx = new GeneralPathX();
			gpx.append(piter, false);
			return ShapeFactory.createPolyline3D(gpx, z);
		} else {
			PathIterator piter = shp.getPathIterator(null);
			GeneralPathX gpx = new GeneralPathX();
			gpx.append(piter, false);
			return ShapeFactory.createPolyline2D(gpx);
		}
	}

	public static IGeometry mergePolygons(FGeometryCollection gco) {
		GeneralPathX gpx = new GeneralPathX();
		
		IGeometry[] gg = gco.getGeometries();
		int cnt = gg.length;
		IGeometry ig2d;
		for (int i=0; i<cnt; i++) {
			ig2d = gg[i];
			gpx.append(ig2d.getPathIterator(null), false);
		}
		return ShapeFactory.createPolygon2D(gpx);
	}

	public static int estimateGoodFetchSize(ResultSetMetaData md) { 
		
		int bytesum = 100;
		try {
			int sz = md.getColumnCount();
			int inc = 0;
			for (int i=1; i<=sz; i++) {
				if (md.getColumnType(i) == Types.BLOB) {
					bytesum = bytesum + 1000; // blob 5k
				} else {
					inc = Math.max(10, md.getColumnDisplaySize(i));
					if (inc <= OracleSpatialDriver.VARCHAR2_MAX_SIZE) {
						bytesum = bytesum + inc;
					}
				}
			}
			// TODO Auto-generated method stub
		} catch (SQLException se) {
			logger.warn("Error while getting field sizes: " + se.getMessage());
			logger.warn("Used: row size = 5000 bytes.");
			bytesum = 5000;
		}
		
		int resp = FETCH_BLOCK_SIZE_BYTES / bytesum;
		resp = Math.max(50, resp);
		resp = Math.min(resp, FETCH_BLOCK_MAX);
		
		return resp;
	}
    
	

}

