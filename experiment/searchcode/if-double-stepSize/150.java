package worldwind.kml;

import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.IconRenderer;
import gov.nasa.worldwind.render.UserFacingIcon;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import worldwind.kml.model.*;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLUquadric;
import javax.media.opengl.glu.GLUtessellatorCallbackAdapter;
import javax.media.opengl.glu.GLUtessellator;
import javax.media.opengl.glu.GLU;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: tgleason
 * Date: Sep 2, 2008
 * Time: 8:30:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class KML3dLayer extends AbstractLayer {

    KMLFile kmlFile;
    int count = 0;
    double simplificationFactor = 0.1;
    IconRenderer iconRenderer = new IconRenderer();

    Map pointCache = new HashMap();
    Map<KMLPolygon, PolyCacheItem> polyCache = new HashMap<KMLPolygon, PolyCacheItem>();

    static final int HEIGHT_ABSOULTE = 0;
    static final int HEIGHT_RELATIVE = 1;
    static final int HEIGHT_TESS = 2;


    public KML3dLayer(KMLFile kmlFile) {
        this.kmlFile = kmlFile;
    }

    int polys = 0;
    int cachedPolys = 0;

    protected void doRender(DrawContext dc) {

        

//        System.out.println("Draw: " + dc.getVisibleSector() + " width: " + dc.getDrawableWidth() + " height: " + dc.getDrawableHeight());
        double ppd = dc.getDrawableWidth() / dc.getVisibleSector().getDeltaLonDegrees();
//        System.out.println("Pixels per Degree: " + ppd);

        polys = 0;
        cachedPolys = 0;

        KMLFolder folder = kmlFile.getRootFolder();

        paintKMLFolder(dc, folder);

        //System.out.printf("Painted %d cached of %d polys\n", cachedPolys, polys);
    }


    private void paintKMLFolder(DrawContext context, KMLFolder folder) {
        Iterator<KMLObject> objIter = folder.getObjects().iterator();
        while (objIter.hasNext()) {
            KMLObject kmlObject = objIter.next();
            if (kmlObject instanceof KMLPlacemark) {
                KMLPlacemark placemark = (KMLPlacemark) kmlObject;
                KMLGraphic graphic = placemark.getGraphic();
                if (graphic != null) {
                    if (graphic.getSector().intersects(context.getVisibleSector())) {
                        if (graphic instanceof KMLLineString) {
                            paintLineString(context, (KMLLineString) graphic);
                        } else if (graphic instanceof KMLPoint) {
                            paintPoint(context, (KMLPoint) graphic);
                        } else if (graphic instanceof KMLPolygon) {
                            paintPoly(context, placemark.getStyle(), (KMLPolygon)graphic);
                        }
                    }

                }
            }
        }

        Iterator<KMLFolder> folders = folder.getChildFolders().iterator();
        while (folders.hasNext()) {
            KMLFolder kmlFolder = folders.next();
            paintKMLFolder(context, kmlFolder);
        }
    }

    private void paintPoly(DrawContext dc, KMLStyle style,  KMLPolygon kmlPolygon) {


        double scale = calcScaleForSector(dc, kmlPolygon.getSector());

        if (scale < 0.5) {
            //Too small to be visible
            return;
        }

        polys++;

        PolyCacheItem cacheItem = polyCache.get(kmlPolygon);

        if (cacheItem != null && !cacheItem.fullScale && (scale > (cacheItem.scale*3) || scale < (cacheItem.scale/3) )) {
            polyCache.remove(kmlPolygon);
            dc.getGL().glDeleteLists(cacheItem.displayList, 1);
            cacheItem = null;
            //System.out.println("Purging cached item");
        }

        if (cacheItem == null) {
            cacheItem = new PolyCacheItem();
            cacheItem.scale = scale;
            int n = dc.getGL().glGenLists(1);
            dc.getGL().glNewList(n, GL.GL_COMPILE);
            cacheItem.fullScale = doPaintPoly(dc, style, kmlPolygon);
            dc.getGL().glEndList();
            cacheItem.displayList = n;
            polyCache.put(kmlPolygon, cacheItem);
        } else {
            cachedPolys++;
        }


        dc.getGL().glCallList(cacheItem.displayList);


    }

    private boolean doPaintPoly(DrawContext dc, KMLStyle style, KMLPolygon kmlPolygon) {
        List<KMLCoord> coords = kmlPolygon.getOuter();
        List<Vec4> outerTop = getTransformedPoints(dc, kmlPolygon, coords, 25, HEIGHT_RELATIVE, false);
        List<Vec4> outerBase = getTransformedPoints(dc, kmlPolygon, coords, -10, HEIGHT_TESS, false);
        List<Vec4> innerTop = null;
        List<Vec4> innerBase = null;
        boolean fullScale = (coords.size() == outerTop.size());

        //if (fullScale)
        //    System.out.println("Full scale");

        if (kmlPolygon.getInner() != null) {
            List<KMLCoord> innerCoords = kmlPolygon.getInner();
            innerTop = getTransformedPoints(dc, kmlPolygon, innerCoords, 25, HEIGHT_RELATIVE, false);
            innerBase = getTransformedPoints(dc, kmlPolygon, innerCoords, -10, HEIGHT_TESS, false);
        }


        KMLColor color = null;
        if (style != null) {
            color = (KMLColor)style.getPolyStyle("color");
        }

        if (color == null) {
            color = new KMLColor();
        }

        boolean drawOutline = true;
//        if (style != null && style.getPolyStyle("outline") != null) {
//            drawOutline = (Boolean) style.getPolyStyle("outline");
//        }

        this.begin(dc);
        {

            //dc.getGL().glDisable(GL.GL_LIGHTING);
            //dc.getGL().glColor4d(color.red, color.green, color.blue, color.alpha);
            //Material.RED.apply(dc.getGL(), GL.GL_FRONT);
            //Material.RED.apply(dc.getGL(), GL.GL_BACK);

            float colorComps[] = new float[]{color.red, color.green, color.blue, color.alpha};
            float white[] = new float[]{1,1,1,1};
            dc.getGL().glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, white, 0);
            dc.getGL().glMaterialfv(GL.GL_FRONT, GL.GL_DIFFUSE, colorComps, 0);
            dc.getGL().glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT, colorComps, 0);
            dc.getGL().glMaterialf(GL.GL_FRONT, GL.GL_SHININESS, 50);
            dc.getGL().glMaterialfv(GL.GL_FRONT, GL.GL_EMISSION, colorComps, 0);

//            dc.getGL().glMaterialfv(GL.GL_BACK, GL.GL_SPECULAR, white, 0);
//            dc.getGL().glMaterialfv(GL.GL_BACK, GL.GL_DIFFUSE, white, 0);
//            dc.getGL().glMaterialfv(GL.GL_BACK, GL.GL_AMBIENT, white, 0);
//            dc.getGL().glMaterialf(GL.GL_BACK, GL.GL_SHININESS, 50);
//            dc.getGL().glMaterialfv(GL.GL_BACK, GL.GL_EMISSION, white, 0);


            GLUtessellator tobj = dc.getGLU().gluNewTess();
            PloygonTessCallback callback = new PloygonTessCallback(dc);

            dc.getGLU().gluTessCallback(tobj, GLU.GLU_TESS_VERTEX, callback);
            dc.getGLU().gluTessCallback(tobj, GLU.GLU_TESS_BEGIN, callback);
            dc.getGLU().gluTessCallback(tobj, GLU.GLU_TESS_END, callback);
            dc.getGLU().gluTessCallback(tobj, GLU.GLU_TESS_ERROR, callback);


            dc.getGL().glCullFace(GL.GL_BACK);
            dc.getGL().glEnable(GL.GL_BLEND);
            dc.getGL().glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
            dc.getGL().glEnable(GL.GL_POLYGON_SMOOTH);
            //dc.getGL().glDisable(GL.GL_DEPTH_TEST);

            if (outerTop.size() > 2) {
                Vec4 p1 = outerTop.get(1).subtract3(outerTop.get(0)).normalize3();
                Vec4 p2 = outerTop.get(2).subtract3(outerTop.get(0)).normalize3();
                Vec4 normal = p1.cross3(p2).normalize3();
                dc.getGL().glNormal3d(normal.x, normal.y, normal.z);
            }

            dc.getGLU().gluTessBeginPolygon(tobj, null);

            //-- Outer contour
            dc.getGLU().gluTessBeginContour(tobj);
            for (int i=0; i<outerTop.size(); i++) {
                Vec4 point = outerTop.get(i);
                double vals[] = point.toArray3(new double[3], 0);
                dc.getGLU().gluTessVertex(tobj, vals, 0, vals);
            }
            dc.getGLU().gluTessEndContour(tobj);

            //--  Inner contour
            if (innerTop != null) {
                dc.getGLU().gluTessBeginContour(tobj);
                for (int i=0; i<innerTop.size(); i++) {
                    Vec4 point = innerTop.get(i);
                    double vals[] = point.toArray3(new double[3], 0);
                    dc.getGLU().gluTessVertex(tobj, vals, 0, vals);
                }
                dc.getGLU().gluTessEndContour(tobj);
            }


            dc.getGLU().gluTessEndPolygon(tobj);


            for (int i=0; i<outerTop.size(); i++) {
                dc.getGL().glBegin(GL.GL_POLYGON);

                Vec4 p1 = outerTop.get(i);
                Vec4 p2 = outerTop.get((i+1)%outerTop.size());
                Vec4 p3 = outerBase.get((i+1)%outerBase.size());
                Vec4 p4 = outerBase.get(i);

                Vec4 pp1 = p2.subtract3(p1).normalize3();
                Vec4 pp2 = p3.subtract3(p1).normalize3();
                Vec4 normal = pp2.cross3(pp1).normalize3();
                dc.getGL().glNormal3d(normal.x, normal.y, normal.z);

                dc.getGL().glVertex3d(p1.x, p1.y, p1.z);
                dc.getGL().glVertex3d(p2.x, p2.y, p2.z);
                dc.getGL().glVertex3d(p3.x, p3.y, p3.z);
                dc.getGL().glVertex3d(p4.x, p4.y, p4.z);

                dc.getGL().glEnd();
            }

            if (innerTop != null) {
                for (int i=0; i<innerTop.size(); i++) {
                    dc.getGL().glBegin(GL.GL_POLYGON);

                    Vec4 p1 = innerTop.get(i);
                    Vec4 p2 = innerTop.get((i+1)%innerTop.size());
                    Vec4 p3 = innerBase.get((i+1)%innerBase.size());
                    Vec4 p4 = innerBase.get(i);

                    dc.getGL().glVertex3d(p1.x, p1.y, p1.z);
                    dc.getGL().glVertex3d(p2.x, p2.y, p2.z);
                    dc.getGL().glVertex3d(p3.x, p3.y, p3.z);
                    dc.getGL().glVertex3d(p4.x, p4.y, p4.z);

                    dc.getGL().glEnd();
                }
            }

            dc.getGL().glDisable(GL.GL_BLEND);
            dc.getGL().glDisable(GL.GL_POLYGON_SMOOTH);


            if (drawOutline) {
                dc.getGL().glColor3f(1,1,1);
                dc.getGL().glLineWidth(1.5f);
                dc.getGL().glBegin(GL.GL_LINES);
                dc.getGL().glEnable(GL.GL_LINE_SMOOTH);

                for (int i=0; i<outerTop.size(); i++) {
                    Vec4 vec = outerTop.get(i);
                    Vec4 vec2 = outerTop.get((i+1)%outerTop.size());
                    Vec4 vec3 = outerBase.get(i);

                    dc.getGL().glVertex3d(vec.x, vec.y, vec.z);
                    dc.getGL().glVertex3d(vec2.x, vec2.y, vec2.z);

                    dc.getGL().glVertex3d(vec.x, vec.y, vec.z);
                    dc.getGL().glVertex3d(vec3.x, vec3.y, vec3.z);
                }

                if (innerTop != null) {
                    for (int i=0; i<innerTop.size(); i++) {
                        Vec4 vec = innerTop.get(i);
                        Vec4 vec2 = innerTop.get((i+1)%innerTop.size());
                        Vec4 vec3 = innerBase.get(i);

                        dc.getGL().glVertex3d(vec.x, vec.y, vec.z);
                        dc.getGL().glVertex3d(vec2.x, vec2.y, vec2.z);

                        dc.getGL().glVertex3d(vec.x, vec.y, vec.z);
                        dc.getGL().glVertex3d(vec3.x, vec3.y, vec3.z);
                    }
                }

                dc.getGL().glEnd();
                dc.getGL().glDisable(GL.GL_LINE_SMOOTH);
            }

            dc.getGL().glEnable(GL.GL_LIGHTING);
        }
        this.end(dc);

        return fullScale;
    }

    private void paintPoint(DrawContext dc, KMLPoint kmlPoint) {


        UserFacingIcon icon = new UserFacingIcon("Thumbtack.png",
                new Position(Angle.fromDegrees(kmlPoint.getCoord().getLat()), Angle.fromDegrees(kmlPoint.getCoord().getLon()), 0));
        ArrayList icons = new ArrayList();
        icons.add(icon);
        iconRenderer.render(dc, icons);


        /*
        Vec4 point = computeSurfacePoint(dc, kmlPoint.getCoord(), 50);
        Vec4 point2 = computeSurfacePoint(dc, kmlPoint.getCoord(), -10);

        double dist = calcScaleAtPoint(dc, point);
        //System.out.println("Dist: " + dist);

        double size = 10000/dist;
        //point = computeSurfacePoint(dc, kmlPoint.getCoord(), size);


        this.begin(dc);
        {
            Material.RED.apply(dc.getGL(), GL.GL_FRONT);

            dc.getView().pushReferenceCenter(dc, point);

            GLUquadric quadric = dc.getGLU().gluNewQuadric();
            dc.getGLU().gluSphere(quadric, size, 12, 12);
            dc.getGLU().gluDeleteQuadric(quadric);

            dc.getView().popReferenceCenter(dc);

            dc.getGL().glDisable(GL.GL_LIGHTING);

            dc.getGL().glColor3f(1,1,1);
            dc.getGL().glLineWidth(3);
            dc.getGL().glBegin(GL.GL_LINES);

            dc.getGL().glVertex3d(point.x, point.y, point.z);
            dc.getGL().glVertex3d(point2.x, point2.y, point2.z);

            dc.getGL().glEnd();
            dc.getGL().glEnable(GL.GL_LIGHTING);
        }
        this.end(dc);
        */
    }

    private double calcScaleForSector (DrawContext dc, Sector sector) {
        double model[] = new double[16];
        double proj[] =  new double[16];
        int view[] = new int[4];

        Vec4 upperLeft = computeSurfacePoint(dc, sector.getMinLatitude(), sector.getMinLongitude(), 0);
        Vec4 upperRight = computeSurfacePoint(dc, sector.getMinLatitude(), sector.getMaxLongitude(), 0);
        Vec4 lowerLeft = computeSurfacePoint(dc, sector.getMaxLatitude(), sector.getMinLongitude(), 0);
        Vec4 lowerRight = computeSurfacePoint(dc, sector.getMaxLatitude(), sector.getMaxLongitude(), 0);

        double upperLeftS[] = new double[4];
        double upperRightS[] = new double[4];
        double lowerLeftS[] = new double[4];
        double lowerRightS[] = new double[4];

        dc.getGL().glGetDoublev(GL.GL_MODELVIEW_MATRIX, model, 0);
        dc.getGL().glGetDoublev(GL.GL_PROJECTION_MATRIX, proj, 0);
        dc.getGL().glGetIntegerv(GL.GL_VIEWPORT, view, 0);

        dc.getGLU().gluProject(upperLeft.x, upperLeft.y, upperLeft.z, model, 0, proj, 0, view, 0, upperLeftS, 0);
        dc.getGLU().gluProject(upperRight.x, upperRight.y, upperRight.z, model, 0, proj, 0, view, 0, upperRightS, 0);
        dc.getGLU().gluProject(lowerLeft.x, lowerLeft.y, lowerLeft.z, model, 0, proj, 0, view, 0, lowerLeftS, 0);
        dc.getGLU().gluProject(lowerRight.x, lowerRight.y, lowerRight.z, model, 0, proj, 0, view, 0, lowerRightS, 0);

        double ulx = upperLeftS[0];
        double uly = upperLeftS[1];
        double urx = upperRightS[0];
        double ury = upperRightS[1];
        double llx = lowerLeftS[0];
        double lly = lowerLeftS[1];
        double lrx = lowerRightS[0];
        double lry = lowerRightS[1];

        double dist1 = Math.sqrt((ulx-lrx)*(ulx-lrx) + (uly-lry)*(uly-lry));
        double dist2 = Math.sqrt((urx-llx)*(urx-llx) + (ury-lly)*(ury-lly));

        return Math.max(dist1,  dist2);
    }

    private double calcScaleAtPoint(DrawContext dc, Vec4 point) {
        double model[] = new double[16];
        double proj[] =  new double[16];
        int view[] = new int[4];
        double out1[] = new double[4];
        double out2[] = new double[4];
        double out3[] = new double[4];
        double out4[] = new double[4];

        dc.getGL().glGetDoublev(GL.GL_MODELVIEW_MATRIX, model, 0);
        dc.getGL().glGetDoublev(GL.GL_PROJECTION_MATRIX, proj, 0);
        dc.getGL().glGetIntegerv(GL.GL_VIEWPORT, view, 0);


        dc.getGLU().gluProject(point.x, point.y, point.z, model, 0, proj, 0, view, 0, out1, 0);
        dc.getGLU().gluProject(point.x+1000, point.y, point.z, model, 0, proj, 0, view, 0, out2, 0);
        dc.getGLU().gluProject(point.x, point.y+1000, point.z, model, 0, proj, 0, view, 0, out3, 0);
        dc.getGLU().gluProject(point.x, point.y, point.z+1000, model, 0, proj, 0, view, 0, out4, 0);

        double x1 = out1[0];
        double y1 = out1[1];
        double x2 = out2[0];
        double y2 = out2[1];
        double x3 = out3[0];
        double y3 = out3[1];
        double x4 = out4[0];
        double y4 = out4[1];

//        System.out.printf("1: %f %f %f\n", x1, y1, x1);
//        System.out.printf("2: %f %f %f\n", x2, y2, x2);

        double dist = Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
        double dist2 = Math.sqrt((x3-x1)*(x3-x1) + (y3-y1)*(y3-y1));
        double dist3 = Math.sqrt((x4-x1)*(x4-x1) + (y4-y1)*(y4-y1));
        return Math.max(dist, Math.max(dist2, dist3));
    }

    private void paintLineString(DrawContext dc, KMLLineString kmlLineString) {
        List<KMLCoord> coords = kmlLineString.getCoords();
        List<Vec4> points;

        int style = HEIGHT_RELATIVE;
        if (kmlLineString.isAbsolute()) {
            style = HEIGHT_ABSOULTE;
        }

        points = getTransformedPoints(dc, kmlLineString, coords, 10, style, kmlLineString.isTessellate());

        this.begin(dc);
        {
            //System.out.println("Elevation: " + getElevation());

            dc.getGL().glLineWidth(3);
            dc.getGL().glDisable(GL.GL_LIGHTING);
            dc.getGL().glColor3f(1,0,0);
            dc.getGL().glBegin(GL.GL_LINES);

            Vec4 p1 = points.get(0);
            for (int i = 1; i < points.size(); i++)
            {
                Vec4 p2 = points.get(i);
 //               this.pipeShape.render(dc, p1, p2, newRadius);
                dc.getGL().glVertex3d(p1.x, p1.y, p1.z);
                dc.getGL().glVertex3d(p2.x, p2.y, p2.z);
                p1 = p2;
            }

            dc.getGL().glEnd();
            dc.getGL().glEnable(GL.GL_LIGHTING);

        }
        this.end(dc);
    }

    private List<Vec4> getTransformedPoints(DrawContext dc, Object obj, List<KMLCoord> coords, double height, int mode, boolean tess) {
//        CacheWrapper cw = (CacheWrapper) pointCache.get(obj);
        double ppd = dc.getDrawableWidth() / dc.getVisibleSector().getDeltaLonDegrees();

//        if (cw != null) {
//            double ratio = ppd/cw.ppd;
//            count++;
//            if (ratio > 2 || ratio < 0.5 || (count%31) == 0) {
//                cw = null;
//            } else {
//                return (List<Vec4>) cw.val;
//            }
//        }

        double stepSize = simplificationFactor/ppd;

        List<Vec4> points = null;
        points = new ArrayList<Vec4>();
        KMLCoord last = null;
        for (int i=0; i<coords.size(); i++) {
            double dist = -1;

            if (last != null) {
                dist = last.dist(coords.get(i));
                if (dist < stepSize && i < (coords.size()-1)) {
                    continue;
                }
            }

//            double offset;
//            switch (mode) {
//                case HEIGHT_RELATIVE:
//                    offset = coords.get(i).getHeight();
//                    break;
//                case HEIGHT_TESS:
//                default:
//                    offset = height;
//
//            }

//            System.out.println("Stepsize: " + stepSize);
//            System.out.println("Dist: " + dist);

            if (tess && dist > (stepSize*2)) {
                double count = dist/(stepSize*2);
                double dx = (last.getLon() - coords.get(i).getLon())/count;
                double dy = (last.getLat() - coords.get(i).getLat())/count;
                double dh = (last.getHeight() - coords.get(i).getHeight())/count;


                for (double j=1; j<count; j++) {
                    KMLCoord coord = new KMLCoord(
                            last.getLon() - dx*j,
                            last.getLat() - dy*j,
                            last.getHeight() - dh*j);
                    //System.out.printf("Point %f: %f %f\n", j, coord.getLon(), coord.getLat());
                    addComputedPoint(dc, coord, mode, height, points);
                }
            }

            addComputedPoint(dc, coords.get(i), mode, height, points);
//            points.add(computeSurfacePoint(dc, coords.get(i), offset));
            last = coords.get(i);
        }
        //System.out.println("From: " + coords.size() + " to " + points.size());

//        cw = new CacheWrapper();
//        cw.ppd = ppd;
//        cw.val = points;

        //pointCache.put(obj, cw);
//        System.out.println("Total points: " + points.size());

        return points;
    }

    private void addComputedPoint(DrawContext dc, KMLCoord coord, int mode, double height, List<Vec4> points) {
        if (mode == HEIGHT_ABSOULTE) {
            points.add(computePointFromPosition(dc, coord));
        } else if (mode == HEIGHT_RELATIVE) {
            points.add(computeSurfacePoint(dc, coord, Math.max(coord.getHeight(), 1)));
        } else {
            points.add(computeSurfacePoint(dc, coord, height));
        }
    }


    private Vec4 computeSurfacePoint(DrawContext context, KMLCoord kmlCoord, double offset) {
        Angle lat = Angle.fromDegrees(kmlCoord.getLat());
        Angle lon = Angle.fromDegrees(kmlCoord.getLon());
        Vec4 point = context.getSurfaceGeometry().getSurfacePoint(lat, lon, offset);
        if (point != null)
            return point;

        // Point is outside the current sector geometry, so compute it from the globe.
        return context.getGlobe().computePointFromPosition(lat, lon, offset);
    }

    private Vec4 computeSurfacePoint(DrawContext context, Angle lat, Angle lon, double offset) {
        Vec4 point = context.getSurfaceGeometry().getSurfacePoint(lat, lon, offset);
        if (point != null)
            return point;

        // Point is outside the current sector geometry, so compute it from the globe.
        return context.getGlobe().computePointFromPosition(lat, lon, offset);
    }

    private Vec4 computePointFromPosition(DrawContext context, KMLCoord kmlCoord) {
        Angle lat = Angle.fromDegrees(kmlCoord.getLat());
        Angle lon = Angle.fromDegrees(kmlCoord.getLon());
        // Point is outside the current sector geometry, so compute it from the globe.
        return context.getGlobe().computePointFromPosition(lat, lon, kmlCoord.getHeight());
    }


    protected void begin(DrawContext dc)
    {
        GL gl = dc.getGL();
        Vec4 cameraPosition = dc.getView().getEyePoint();

//        if (dc.isPickingMode())
//        {
//            this.pickSupport.beginPicking(dc);
//
//            gl.glPushAttrib(GL.GL_ENABLE_BIT | GL.GL_CURRENT_BIT | GL.GL_TRANSFORM_BIT);
//            gl.glDisable(GL.GL_TEXTURE_2D);
//            gl.glDisable(GL.GL_COLOR_MATERIAL);
//        }
//        else
//        {
            gl.glPushAttrib(
                GL.GL_TEXTURE_BIT | GL.GL_ENABLE_BIT | GL.GL_CURRENT_BIT | GL.GL_LIGHTING_BIT | GL.GL_TRANSFORM_BIT);
            gl.glDisable(GL.GL_TEXTURE_2D);

            float[] lightPosition =
                {(float) (cameraPosition.x * 2), (float) (cameraPosition.y / 2), (float) (cameraPosition.z), 0.0f};
//        float[] lightDiffuse = {1.0f, 1.0f, 1.0f, 1.0f};
//        float[] lightAmbient = {1.0f, 1.0f, 1.0f, 1.0f};
//        float[] lightSpecular = {1.0f, 1.0f, 1.0f, 1.0f};
        float[] lightDiffuse = {0.4f, 0.4f, 0.4f, 1.0f};
        float[] lightAmbient = {0.3f, 0.3f, 0.3f, 1.0f};
        float[] lightSpecular = {0.5f, 0.5f, 0.5f, 1.0f};

            gl.glDisable(GL.GL_COLOR_MATERIAL);

            gl.glLightfv(GL.GL_LIGHT1, GL.GL_POSITION, lightPosition, 0);
            gl.glLightfv(GL.GL_LIGHT1, GL.GL_DIFFUSE, lightDiffuse, 0);
            gl.glLightfv(GL.GL_LIGHT1, GL.GL_AMBIENT, lightAmbient, 0);
            gl.glLightfv(GL.GL_LIGHT1, GL.GL_SPECULAR, lightSpecular, 0);

            gl.glDisable(GL.GL_LIGHT0);
            gl.glEnable(GL.GL_LIGHT1);
            gl.glEnable(GL.GL_LIGHTING);
            gl.glEnable(GL.GL_NORMALIZE);
//        }

        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPushMatrix();
    }

    protected void end(DrawContext dc)
    {
        GL gl = dc.getGL();

        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPopMatrix();

//        if (dc.isPickingMode())
//        {
//            this.pickSupport.endPicking(dc);
//        }
//        else
//        {
            gl.glDisable(GL.GL_LIGHT1);
            gl.glEnable(GL.GL_LIGHT0);
            gl.glDisable(GL.GL_LIGHTING);
            gl.glDisable(GL.GL_NORMALIZE);
//        }

        gl.glPopAttrib();
    }

    static class CacheWrapper {
        double ppd;
        Object val;
    }

    public static class PolyCacheItem {
        int displayList;
        double scale;
        boolean fullScale = false;
    }

    static class PloygonTessCallback extends GLUtessellatorCallbackAdapter {
        DrawContext dc;

        PloygonTessCallback(DrawContext dc) {
            this.dc = dc;
        }

        public void begin(int i) {
            //System.out.println("Begin: " + i);
            dc.getGL().glBegin(i);
        }

        public void end() {
            //System.out.println("End");
            dc.getGL().glEnd();
        }

        public void combine(double[] coords, Object[] data, float[] weight, Object[] outData) {
            //System.out.println("CombineL " + doubles.length);
            double[] vertex = new double[6];
            int i;

            vertex[0] = coords[0];
            vertex[1] = coords[1];
            vertex[2] = coords[2];
            for (i = 3; i < 6/* 7OutOfBounds from C! */; i++)
                vertex[i] = weight[0] //
                        * ((double[]) data[0])[i] + weight[1]
                        * ((double[]) data[1])[i] + weight[2]
                        * ((double[]) data[2])[i] + weight[3]
                        * ((double[]) data[3])[i];
            outData[0] = vertex;

        }

        public void vertex(Object o) {
            //System.out.println("Vertex: " + ((double[])o).length);
            double vals[] = (double[]) o;
            dc.getGL().glVertex3dv(vals, 0);
        }

        public void error(int i) {
            System.out.println("Error: " + i);
        }
    }
}

