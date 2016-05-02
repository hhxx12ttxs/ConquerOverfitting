package worldwind.kml;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.IconRenderer;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.UserFacingIcon;
import gov.nasa.worldwind.render.WWIcon;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUtessellator;
import javax.media.opengl.glu.GLUtessellatorCallbackAdapter;

import worldwind.kml.model.AltitudeMode;
import worldwind.kml.model.KML3DModel;
import worldwind.kml.model.KMLColor;
import worldwind.kml.model.KMLCoord;
import worldwind.kml.model.KMLFile;
import worldwind.kml.model.KMLFolder;
import worldwind.kml.model.KMLGraphic;
import worldwind.kml.model.KMLLineString;
import worldwind.kml.model.KMLMultiGeometry;
import worldwind.kml.model.KMLObject;
import worldwind.kml.model.KMLPlacemark;
import worldwind.kml.model.KMLPoint;
import worldwind.kml.model.KMLPolygon;
import worldwind.kml.model.KMLStyle;

/**
 * Created by IntelliJ IDEA. User: tgleason Date: Sep 7, 2008 Time: 1:08:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class KMLLayer extends AbstractLayer {
	protected KMLFile kmlFile;
	private double tessDistance = 10;
	private double fudgeFactor = 1;
	private int displayList = -1;
	private double simplificationFactor = 0.1;
	private List<KMLPlacemark> lineStringsToDraw = new ArrayList<KMLPlacemark>();
	private List<KMLPlacemark> pointsToDraw = new ArrayList<KMLPlacemark>();
	private List<WWIcon> activeIcons = new ArrayList<WWIcon>();
	private IconRenderer iconRenderer = new IconRenderer();
	private boolean invalid = false;
	private KMLPlacemark selectedPoint = null;
	/** Current vertical exaggeration factor. */
	private double verticalExaggeration;

	public KMLLayer(KMLFile kmlFile) {
		this.kmlFile = kmlFile;
		this.verticalExaggeration = 1.0;
	}

	public boolean isInvalid() {
		return invalid;
	}

	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	/**
	 * Render the layer. Some notes: - The first time this is called, it will
	 * set up a OpenGL display list for most things. The others will be stored
	 * in other data structures - If the sector containing this layer is not on
	 * the screen, we'll drop out without drawing - If the size of the sector
	 * containing this layer is less than one pixel, we'll drop out - To draw:
	 * call the display list, then draw lines that are tesselated, then add
	 * icons to the draw context
	 * 
	 * @param dc
	 */
	protected void doRender(DrawContext dc) {
		setVerticalExaggeration(dc.getVerticalExaggeration());

		if (invalid) {
			invalid = false;
			dc.getGL().glDeleteLists(displayList, 1);
			displayList = -1;
			lineStringsToDraw.clear();
			pointsToDraw.clear();
		}

		if (displayList == -1) {
			System.out.println("Building display list...");
			displayList = dc.getGL().glGenLists(1);
			dc.getGL().glNewList(displayList, GL.GL_COMPILE);
			drawFolder(dc, kmlFile.getRootFolder());
			dc.getGL().glEndList();
			System.out.println("Done.");
		}

		if (!dc.getVisibleSector().intersects(kmlFile.getSector())) {
			return;
		}

		if (sizeInPixels(dc, kmlFile.getSector()) < 1) {
			return;
		}

		GL gl = dc.getGL();
		setupGL(dc, gl);

		// -- Draw most stuff
		gl.glCallList(displayList);

		// -- Draw tesselated lines
		for (KMLPlacemark kmlPlacemark : lineStringsToDraw) {
			KMLLineString lineString = (KMLLineString) kmlPlacemark
					.getGraphic();
			if (dc.getVisibleSector().intersects(lineString.getSector())) {
				drawLineString(dc, lineString, kmlPlacemark.getStyle());
			}
		}

		unsetupGL(gl);

		// -- Add point placemarks to draw list, if we're zoomed in far enough
		// -- Right now just supports "pushpin" icons. A lot to do here. Should
		// put the icon
		// -- list in a quadtree in case there are millions of them
		// -- NOTE: Drawing these icons seems to take a lot longer than it
		// should. I think the IconRendered needs some work
		activeIcons = new ArrayList<WWIcon>();
		for (KMLPlacemark kmlPlacemark : pointsToDraw) {
			KMLPoint kmlPoint = (KMLPoint) kmlPlacemark.getGraphic();
			// System.out.println("Intersects: " +
			// dc.getVisibleSector().intersects(kmlPoint.getSector()));
			if (dc.getVisibleSector().intersects(kmlPoint.getSector())
					&& sizeInPixels(dc, kmlPoint.getSector()) > 2) {
				UserFacingIcon icon = new UserFacingIcon("placemark.png",
						pointPosition(dc, kmlPoint));
				if (selectedPoint == kmlPlacemark) {
					icon.setHighlighted(true);
				}
				activeIcons.add(icon);
			}
		}
		iconRenderer.render(dc, activeIcons);

	}

	public void pick(DrawContext dc, Point point) {
		selectedPoint = null;

		Point flipped = new Point(point.x,
				(int) (dc.getView().getViewport().getHeight()) - point.y);

		for (KMLPlacemark kmlPlacemark : pointsToDraw) {
			KMLPoint kmlPoint = (KMLPoint) kmlPlacemark.getGraphic();
			if (dc.getVisibleSector().intersects(kmlPoint.getSector())
					&& sizeInPixels(dc, kmlPoint.getSector()) > 2) {
				if (distanceInPixels(dc, pointPosition(dc, kmlPoint), flipped) < 10) {
					selectedPoint = kmlPlacemark;

					int[] viewport = new int[4];
					java.nio.ByteBuffer pixel = com.sun.opengl.util.BufferUtil
							.newByteBuffer(3);
					GL gl = dc.getGL();
					gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
					gl.glReadPixels(dc.getPickPoint().x,
							viewport[3] - dc.getPickPoint().y, 1, 1, GL.GL_RGB,
							GL.GL_UNSIGNED_BYTE, pixel);

					Color topColor = new Color(pixel.get(0) & 0xff,
							pixel.get(1) & 0xff, pixel.get(2) & 0xff, 0);

					dc.getPickedObjects().clear();

					dc.addPickedObject(new PickedObject(topColor.getRGB(),
							kmlPlacemark, pointPosition(dc, kmlPoint), false));
					break;
				}
			}
		}
	}

	private Position pointPosition(DrawContext dc, KMLPoint kmlPoint) {
		Angle lat, lon;
		double height;

		lat = Angle.fromDegrees(kmlPoint.getCoord().getLat());
		lon = Angle.fromDegrees(kmlPoint.getCoord().getLon());

		switch (kmlPoint.getAltitudeMode()) {
		case KMLPoint.ABSOLUTE:
			height = kmlPoint.getCoord().getHeight();
			break;
		case KMLPoint.RELATIVE_TO_GROUND:
			height = kmlPoint.getCoord().getHeight()
					+ dc.getGlobe().getElevation(lat, lon);
			break;
		default:
			height = dc.getGlobe().getElevation(lat, lon);
		}

		return new Position(lat, lon, height);
	}

	private void setupGL(DrawContext dc, GL gl) {
		Vec4 cameraPosition = dc.getView().getEyePoint();

		gl.glPushAttrib(GL.GL_TEXTURE_BIT | GL.GL_ENABLE_BIT
				| GL.GL_CURRENT_BIT | GL.GL_LIGHTING_BIT | GL.GL_TRANSFORM_BIT);
		gl.glDisable(GL.GL_TEXTURE_2D);

		float[] lightPosition = { (float) (cameraPosition.x * 2),
				(float) (cameraPosition.y / 2), (float) (cameraPosition.z),
				0.0f };
		float[] lightDiffuse = { 0.4f, 0.4f, 0.4f, 1.0f };
		float[] lightAmbient = { 0.3f, 0.3f, 0.3f, 1.0f };
		float[] lightSpecular = { 0.5f, 0.5f, 0.5f, 1.0f };

		gl.glDisable(GL.GL_COLOR_MATERIAL);

		gl.glLightfv(GL.GL_LIGHT1, GL.GL_POSITION, lightPosition, 0);
		gl.glLightfv(GL.GL_LIGHT1, GL.GL_DIFFUSE, lightDiffuse, 0);
		gl.glLightfv(GL.GL_LIGHT1, GL.GL_AMBIENT, lightAmbient, 0);
		gl.glLightfv(GL.GL_LIGHT1, GL.GL_SPECULAR, lightSpecular, 0);

		gl.glDisable(GL.GL_LIGHT0);
		gl.glEnable(GL.GL_LIGHT1);
		gl.glEnable(GL.GL_LIGHTING);
		gl.glEnable(GL.GL_NORMALIZE);

		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPushMatrix();
	}

	private void unsetupGL(GL gl) {
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPopMatrix();

		gl.glDisable(GL.GL_LIGHT1);
		gl.glEnable(GL.GL_LIGHT0);
		gl.glDisable(GL.GL_LIGHTING);
		gl.glDisable(GL.GL_NORMALIZE);

		gl.glPopAttrib();
	}

	/**
	 * Draw a folder recursively. This is called once, the first time the file
	 * is displayed. It issues OpenGL commands to draw most of the objects.
	 * Those commands are stored in a Display List than can be replayed each
	 * other time the layer is displyed.
	 * 
	 * Some things aren't drawn now, since they need to be drawn each time
	 * differently. That includes lines that are tesselated. They need to be
	 * re-tesselated each frame depending on how much elevation detail is
	 * available. Also, icons are drawn post-render. All of these things are
	 * stored in other datastructures so they can be renedered separately every
	 * frame.
	 * 
	 * @param dc
	 * @param folder
	 */
	private void drawFolder(DrawContext dc, KMLFolder folder) {

		if (!folder.isVisible())
			return;

		Iterator<KMLObject> objIter = folder.getObjects().iterator();
		while (objIter.hasNext()) {
			KMLObject kmlObject = objIter.next();
			if (kmlObject instanceof KMLPlacemark) {
				KMLPlacemark placemark = (KMLPlacemark) kmlObject;

				if (!placemark.isVisible())
					continue;

				KMLGraphic graphic = placemark.getGraphic();
				if (graphic != null) {
					if (graphic instanceof KMLLineString) {
						KMLLineString lineString = (KMLLineString) graphic;
						if (lineString.isExtrude()) {
							drawExtrudedLineString(dc, (KMLLineString) graphic);
						} else {
							lineStringsToDraw.add(placemark);
						}
					} else if (graphic instanceof KMLPoint) {
						// paintPoint(context, (KMLPoint) graphic);
						pointsToDraw.add(placemark);
					} else if (graphic instanceof KMLPolygon) {
						drawPoly(dc, placemark.getStyle(), (KMLPolygon) graphic);
					} else if (graphic instanceof KMLMultiGeometry) {
						drawMultiGeometry(dc, placemark.getStyle(),
								(KMLMultiGeometry) graphic);
					} else if (graphic instanceof KML3DModel) {
						draw3DModel(dc, placemark.getStyle(),
								(KML3DModel) graphic);
					}
				}
			}
		}

		Iterator<KMLFolder> folders = folder.getChildFolders().iterator();
		while (folders.hasNext()) {
			KMLFolder kmlFolder = folders.next();
			drawFolder(dc, kmlFolder);
		}

	}

	private void draw3DModel(DrawContext dc, KMLStyle style, KML3DModel graphic) {
		Renderable mesh = graphic.getMesh();
		mesh.render(dc);
	}

	private void drawMultiGeometry(DrawContext dc, KMLStyle style,
			KMLMultiGeometry kmlMultiGeom) {
		for (KMLGraphic graphic : kmlMultiGeom.getGraphics()) {
			if (graphic != null) {
				if (graphic instanceof KMLLineString) {
					KMLLineString lineString = (KMLLineString) graphic;
					if (lineString.isExtrude()) {
						drawExtrudedLineString(dc, (KMLLineString) graphic);
					} else {
						// lineStringsToDraw.add(placemark);
					}
				} else if (graphic instanceof KMLPoint) {
					// paintPoint(context, (KMLPoint) graphic);
					// pointsToDraw.add(placemark);
				} else if (graphic instanceof KMLPolygon) {
					drawPoly(dc, style, (KMLPolygon) graphic);
				}
			}
		}
	}

	/**
	 * Right now, just draws a line. An extruded linestring should display a
	 * "fence-like" polygon.
	 * 
	 * @param dc
	 * @param lineString
	 */
	private void drawExtrudedLineString(DrawContext dc, KMLLineString lineString) {
		List<KMLCoord> coords = lineString.getCoords();

		if (lineString.isTessellate()) {
			coords = interpolateCoords(coords);
		}

		List<Vec4> points = getExact3dPointsForCoords(dc, coords,
				lineString.isAbsolute());

		dc.getGL().glLineWidth(3);
		dc.getGL().glDisable(GL.GL_LIGHTING);

		dc.getGL().glColor3f(1, 0, 0);
		dc.getGL().glBegin(GL.GL_LINE_STRIP);

		for (int i = 0; i < points.size(); i++) {
			Vec4 p = points.get(i);
			dc.getGL().glVertex3d(p.x, p.y, p.z);
		}

		dc.getGL().glEnd();
		dc.getGL().glEnable(GL.GL_LIGHTING);
	}

	/**
	 * Draw a regular linestring. It will simplify the line depending on the
	 * detail. This is done via a constant "simplificationFactor". It's a bit of
	 * a kludge, but seems to work reasonably well.
	 * 
	 * If it is a tesselated line, it will add detail to make sure it follows
	 * the terrain at the current view level.
	 * 
	 * @param dc
	 * @param lineString
	 */
	private void drawLineString(DrawContext dc, KMLLineString lineString,
			KMLStyle style) {
		double ppd = dc.getDrawableWidth()
				/ dc.getVisibleSector().getDeltaLonDegrees();
		double stepSize = simplificationFactor / ppd;

		List<KMLCoord> coords = lineString.getCoords();

		List<KMLCoord> interpCoords = new ArrayList<KMLCoord>();
		KMLCoord last = null;
		for (int i = 0; i < coords.size(); i++) {
			double dist = -1;

			if (last != null) {
				dist = last.dist(coords.get(i));
				if (dist < stepSize && i < (coords.size() - 1)) {
					continue;
				}
			}
			if (lineString.isTessellate() && dist > (stepSize * 2)) {
				double count = dist / (stepSize * 2);
				double dx = (last.getLon() - coords.get(i).getLon()) / count;
				double dy = (last.getLat() - coords.get(i).getLat()) / count;
				double dh = (last.getHeight() - coords.get(i).getHeight())
						/ count;

				for (double j = 1; j < count; j++) {
					KMLCoord coord = new KMLCoord(last.getLon() - dx * j,
							last.getLat() - dy * j, last.getHeight() - dh * j);
					interpCoords.add(coord);
				}
			}
			interpCoords.add(coords.get(i));
			last = coords.get(i);
		}

		List<Vec4> points = get3dPointsForCoords(dc, interpCoords,
				lineString.isAbsolute());

		// -- Style line

		KMLColor color = new KMLColor(); // TODO: use static
		float width = 3;

		if (style != null) {
			if (style.getLineStyle("color") != null) {
				color = (KMLColor) style.getLineStyle("color");
			}

			Float lineWidth = (Float) style.getLineStyle("width");
			if (lineWidth != null) {
				width = lineWidth.floatValue();
			}
		}

		dc.getGL().glLineWidth(width);
		dc.getGL().glDisable(GL.GL_LIGHTING);
		dc.getGL().glColor3f(color.red, color.green, color.blue);
		dc.getGL().glBegin(GL.GL_LINE_STRIP);

		for (int i = 0; i < points.size(); i++) {
			Vec4 p = points.get(i);
			dc.getGL().glVertex3d(p.x, p.y, p.z);
		}

		dc.getGL().glEnd();
		dc.getGL().glEnable(GL.GL_LIGHTING);
	}

	/**
	 * Tesselate a line segment.
	 * 
	 * @param coords
	 * @return
	 */
	private List<KMLCoord> interpolateCoords(List<KMLCoord> coords) {
		if (coords.size() < 2)
			return coords;

		List<KMLCoord> newCoords = new ArrayList<KMLCoord>(coords.size());
		newCoords.add(coords.get(0));

		KMLCoord last = coords.get(0);
		for (int i = 1; i < coords.size(); i++) {
			KMLCoord cur = coords.get(i);
			double dist = distance(last, cur);
			// System.out.println("Dist: " + dist);
			if (dist > (tessDistance * 2)) {
				double newPoints = dist / tessDistance;
				double dx = (last.getLon() - cur.getLon()) / newPoints;
				double dy = (last.getLat() - cur.getLat()) / newPoints;
				double dh = (last.getHeight() - coords.get(i).getHeight())
						/ newPoints;

				for (int j = 1; j < newPoints; j = j + 1) {
					KMLCoord coord = new KMLCoord(last.getLon() - dx * j,
							last.getLat() - dy * j, last.getHeight() - dh * j);
					newCoords.add(coord);
				}
			}
			newCoords.add(cur);
			last = cur;
		}
		return newCoords;
	}

	/**
	 * Draw a polygon. It may be extruded. It may have holes. Its altitude
	 * coordinates could be absolute or clamped to ground.
	 * 
	 * @param dc
	 * @param style
	 * @param kmlPolygon
	 */
	private void drawPoly(DrawContext dc, KMLStyle style, KMLPolygon kmlPolygon) {
		List<KMLCoord> coords = kmlPolygon.getOuter();

		boolean absoluteAltitude = AltitudeMode.Absolute.equals(kmlPolygon
				.getAltitudeMode());
		List<Vec4> outerTop = getExact3dPointsForCoords(dc, coords,
				absoluteAltitude);
		List<Vec4> outerBase = getExactGroundPointsForCoords(dc, coords, -1);
		List<Vec4> innerTop = null;
		List<Vec4> innerBase = null;

		if (kmlPolygon.getInner() != null) {
			List<KMLCoord> innerCoords = kmlPolygon.getInner();
			innerTop = getExact3dPointsForCoords(dc, innerCoords, false);
			innerBase = getExactGroundPointsForCoords(dc, innerCoords, -1);
		}

		KMLColor color = null;
		if (style != null) {
			color = (KMLColor) style.getPolyStyle("color");
		}

		if (color == null) {
			color = new KMLColor();
		}

		boolean drawOutline = true;
		if (style != null && style.getPolyStyle("outline") != null) {
			drawOutline = (Boolean) style.getPolyStyle("outline");
		}

		float colorComps[] = new float[] { color.red, color.green, color.blue,
				color.alpha };
		float white[] = new float[] { 1, 1, 1, 1 };
		float lightPosition[] = new float[] { 0, 0, 1, 0 };
		dc.getGL().glEnable(GL.GL_LIGHTING);
		dc.getGL().glEnable(GL.GL_LIGHT0);
		dc.getGL().glPushMatrix();
		dc.getGL().glLoadIdentity();
		dc.getGL().glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, lightPosition, 0);
		dc.getGL().glPopMatrix();
		dc.getGL().glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE,
				new float[] { 1, 1, 1, 1 }, 0);
		dc.getGL().glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, white, 0);
		dc.getGL().glMaterialfv(GL.GL_FRONT, GL.GL_DIFFUSE, colorComps, 0);
		dc.getGL().glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT,
				new float[] { 0, 0, 0, 1 }, 0);
		dc.getGL().glMaterialf(GL.GL_FRONT, GL.GL_SHININESS, 50);
		dc.getGL().glMaterialfv(GL.GL_FRONT, GL.GL_EMISSION,
				new float[] { 0, 0, 0, 1 }, 0);

		GLUtessellator tobj = dc.getGLU().gluNewTess();
		PloygonTessCallback callback = new PloygonTessCallback(dc);

		dc.getGLU().gluTessCallback(tobj, GLU.GLU_TESS_VERTEX, callback);
		dc.getGLU().gluTessCallback(tobj, GLU.GLU_TESS_BEGIN, callback);
		dc.getGLU().gluTessCallback(tobj, GLU.GLU_TESS_END, callback);
		dc.getGLU().gluTessCallback(tobj, GLU.GLU_TESS_ERROR, callback);

		// dc.getGL().glCullFace(GL.GL_BACK);
		// dc.getGL().glFrontFace(dc.getGL().GL_CCW);
		// dc.getGL().glCullFace(GL.GL_FRONT_AND_BACK);
		// dc.getGL().glEnable(dc.getGL().GL_CULL_FACE);
		dc.getGL().glEnable(GL.GL_BLEND);
		dc.getGL().glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		dc.getGL().glEnable(GL.GL_POLYGON_SMOOTH);
		// dc.getGL().glDisable(GL.GL_DEPTH_TEST);

		if (outerTop.size() > 2) {
			Vec4 p1 = outerTop.get(1).subtract3(outerTop.get(0)).normalize3();
			Vec4 p2 = outerTop.get(2).subtract3(outerTop.get(0)).normalize3();
			Vec4 normal = p1.cross3(p2).normalize3();
			dc.getGL().glNormal3d(normal.x, normal.y, normal.z);
		}

		dc.getGLU().gluTessBeginPolygon(tobj, null);

		// -- Outer contour
		dc.getGLU().gluTessBeginContour(tobj);
		for (int i = 0; i < outerTop.size(); i++) {
			Vec4 point = outerTop.get(i);
			double vals[] = point.toArray3(new double[3], 0);
			dc.getGLU().gluTessVertex(tobj, vals, 0, vals);
		}
		dc.getGLU().gluTessEndContour(tobj);

		// -- Inner contour
		if (innerTop != null) {
			dc.getGLU().gluTessBeginContour(tobj);
			for (int i = 0; i < innerTop.size(); i++) {
				Vec4 point = innerTop.get(i);
				double vals[] = point.toArray3(new double[3], 0);
				dc.getGLU().gluTessVertex(tobj, vals, 0, vals);
			}
			dc.getGLU().gluTessEndContour(tobj);
		}

		dc.getGLU().gluTessEndPolygon(tobj);

		if (kmlPolygon.isExtrude()) {
			for (int i = 0; i < outerTop.size(); i++) {
				dc.getGL().glBegin(GL.GL_POLYGON);

				Vec4 p1 = outerTop.get(i);
				Vec4 p2 = outerTop.get((i + 1) % outerTop.size());
				Vec4 p3 = outerBase.get((i + 1) % outerBase.size());
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
				for (int i = 0; i < innerTop.size(); i++) {
					dc.getGL().glBegin(GL.GL_POLYGON);

					Vec4 p1 = innerTop.get(i);
					Vec4 p2 = innerTop.get((i + 1) % innerTop.size());
					Vec4 p3 = innerBase.get((i + 1) % innerBase.size());
					Vec4 p4 = innerBase.get(i);

					dc.getGL().glVertex3d(p1.x, p1.y, p1.z);
					dc.getGL().glVertex3d(p2.x, p2.y, p2.z);
					dc.getGL().glVertex3d(p3.x, p3.y, p3.z);
					dc.getGL().glVertex3d(p4.x, p4.y, p4.z);

					dc.getGL().glEnd();
				}
			}
		}

		dc.getGL().glDisable(GL.GL_BLEND);
		dc.getGL().glDisable(GL.GL_POLYGON_SMOOTH);

		if (drawOutline) {
			dc.getGL().glColor3f(1, 1, 1);
			dc.getGL().glLineWidth(1.5f);
			dc.getGL().glBegin(GL.GL_LINES);
			dc.getGL().glEnable(GL.GL_LINE_SMOOTH);

			for (int i = 0; i < outerTop.size(); i++) {
				Vec4 vec = outerTop.get(i);
				Vec4 vec2 = outerTop.get((i + 1) % outerTop.size());
				Vec4 vec3 = outerBase.get(i);

				dc.getGL().glVertex3d(vec.x, vec.y, vec.z);
				dc.getGL().glVertex3d(vec2.x, vec2.y, vec2.z);

				if (kmlPolygon.isExtrude()) {
					dc.getGL().glVertex3d(vec.x, vec.y, vec.z);
					dc.getGL().glVertex3d(vec3.x, vec3.y, vec3.z);
				}
			}

			if (innerTop != null) {
				for (int i = 0; i < innerTop.size(); i++) {
					Vec4 vec = innerTop.get(i);
					Vec4 vec2 = innerTop.get((i + 1) % innerTop.size());
					Vec4 vec3 = innerBase.get(i);

					dc.getGL().glVertex3d(vec.x, vec.y, vec.z);
					dc.getGL().glVertex3d(vec2.x, vec2.y, vec2.z);

					if (kmlPolygon.isExtrude()) {
						dc.getGL().glVertex3d(vec.x, vec.y, vec.z);
						dc.getGL().glVertex3d(vec3.x, vec3.y, vec3.z);
					}
				}
			}

			dc.getGL().glEnd();
			dc.getGL().glDisable(GL.GL_LINE_SMOOTH);
		}

		dc.getGL().glEnable(GL.GL_LIGHTING);
	}

	/**
	 * Force the underlying code to load the most accurate value for the height
	 * of a point. This is called on setup for some points. If there are many,
	 * it may take some time to load them all.
	 * 
	 * @param dc
	 * @param coords
	 * @param fudge
	 * @return
	 */
	private List<Vec4> getExactGroundPointsForCoords(DrawContext dc,
			List<KMLCoord> coords, double fudge) {
		List<Vec4> points = new ArrayList<Vec4>(coords.size());

		for (KMLCoord coord : coords) {
			Vec4 point = getExact3dPointForLocation(dc, coord.getLat(),
					coord.getLon(), fudge);
			points.add(point);
		}

		return points;
	}

	private List<Vec4> get3dPointsForCoords(DrawContext dc,
			List<KMLCoord> coords, boolean absolute) {
		List<Vec4> points = new ArrayList<Vec4>(coords.size());

		if (absolute) {
			for (KMLCoord coord : coords) {
				Angle latAngle = Angle.fromDegrees(coord.getLat());
				Angle lonAngle = Angle.fromDegrees(coord.getLon());
				Vec4 point = dc.getGlobe().computePointFromPosition(latAngle,
						lonAngle, coord.getHeight());

				points.add(point);
			}
		} else {
			for (KMLCoord coord : coords) {
				Vec4 point = computeSurfacePoint(dc, coord, 1);
				points.add(point);
			}
		}

		return points;
	}

	private Vec4 computeSurfacePoint(DrawContext context, KMLCoord kmlCoord,
			double offset) {
		Angle lat = Angle.fromDegrees(kmlCoord.getLat());
		Angle lon = Angle.fromDegrees(kmlCoord.getLon());
		Vec4 point = context.getSurfaceGeometry().getSurfacePoint(lat, lon,
				offset);
		if (point != null)
			return point;

		// Point is outside the current sector geometry, so compute it from the
		// globe.
		return context.getGlobe().computePointFromPosition(lat, lon, offset);
	}

	private List<Vec4> getExact3dPointsForCoords(DrawContext dc,
			List<KMLCoord> coords, boolean absolute) {
		List<Vec4> points = new ArrayList<Vec4>(coords.size());

		if (absolute) {
			for (KMLCoord coord : coords) {
				Angle latAngle = Angle.fromDegrees(coord.getLat());
				Angle lonAngle = Angle.fromDegrees(coord.getLon());
				Vec4 point = dc.getGlobe().computePointFromPosition(latAngle,
						lonAngle, coord.getHeight() * verticalExaggeration);
				points.add(point);
			}
		} else {
			for (KMLCoord coord : coords) {
				Vec4 point = getExact3dPointForLocation(dc, coord.getLat(),
						coord.getLon(),
						Math.max(coord.getHeight(), fudgeFactor));
				points.add(point);
			}
		}

		return points;
	}

	private Vec4 getExact3dPointForLocation(DrawContext dc, double lat,
			double lon, double offset) {
		Double height = null;
		Angle latAngle = Angle.fromDegrees(lat);
		Angle lonAngle = Angle.fromDegrees(lon);

		while (height == null) {
			// FIXME getBestElevation() NWW 0.5 changed to getElevation() NWW
			// 0.6
			// is this behavior right ?
			height = dc.getGlobe().getElevation(latAngle, lonAngle);
			if (height == null) {
				System.out.print(".");
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					Thread.interrupted();
				}
			}
		}

		return dc.getGlobe().computePointFromPosition(latAngle, lonAngle,
				height + offset);
	}

	/**
	 * Guesstimate the size in pixels that a given sector would be on teh
	 * screen. This is useful to see whether we should draw something that would
	 * be too small to see.
	 * 
	 * @param dc
	 * @param sec
	 * @return
	 */
	private double sizeInPixels(DrawContext dc, Sector sec) {
		return Math.max(
				distanceInPixels(dc, sec.getMinLatitude(),
						sec.getMinLongitude(), sec.getMaxLatitude(),
						sec.getMaxLongitude()),
				distanceInPixels(dc, sec.getMinLatitude(),
						sec.getMaxLongitude(), sec.getMaxLatitude(),
						sec.getMinLongitude()));
	}

	private double distanceInPixels(DrawContext dc, Angle lat1, Angle lon1,
			Angle lat2, Angle lon2) {
		double model[] = new double[16];
		double proj[] = new double[16];
		int view[] = new int[4];
		dc.getGL().glGetDoublev(GL.GL_MODELVIEW_MATRIX, model, 0);
		dc.getGL().glGetDoublev(GL.GL_PROJECTION_MATRIX, proj, 0);
		dc.getGL().glGetIntegerv(GL.GL_VIEWPORT, view, 0);

		Vec4 p1 = dc.getGlobe().computePointFromPosition(
				new Position(lat1, lon1, 0));
		Vec4 p2 = dc.getGlobe().computePointFromPosition(
				new Position(lat2, lon2, 0));

		double p1S[] = new double[4];
		double p2S[] = new double[4];

		dc.getGLU().gluProject(p1.x, p1.y, p1.z, model, 0, proj, 0, view, 0,
				p1S, 0);
		dc.getGLU().gluProject(p2.x, p2.y, p2.z, model, 0, proj, 0, view, 0,
				p2S, 0);

		double p1x = p1S[0];
		double p1y = p1S[1];
		double p2x = p2S[0];
		double p2y = p2S[1];

		return Math.sqrt((p1x - p2x) * (p1x - p2x) + (p1y - p2y) * (p1y - p2y));
	}

	private double distanceInPixels(DrawContext dc, Position pos, Point p) {
		double model[] = new double[16];
		double proj[] = new double[16];
		int view[] = new int[4];
		dc.getGL().glGetDoublev(GL.GL_MODELVIEW_MATRIX, model, 0);
		dc.getGL().glGetDoublev(GL.GL_PROJECTION_MATRIX, proj, 0);
		dc.getGL().glGetIntegerv(GL.GL_VIEWPORT, view, 0);

		Vec4 p1 = dc.getGlobe().computePointFromPosition(pos);

		double p1S[] = new double[4];

		dc.getGLU().gluProject(p1.x, p1.y, p1.z, model, 0, proj, 0, view, 0,
				p1S, 0);

		double p1x = p1S[0];
		double p1y = p1S[1];
		double p2x = p.x;
		double p2y = p.y;

		// if (p1x>0 && p1x<1000 && p1y>0 && p1y<800)
		// System.out.println("Loc: " + p1x + ", " + p1y);

		return Math.sqrt((p1x - p2x) * (p1x - p2x) + (p1y - p2y) * (p1y - p2y));
	}

	private double distance(KMLCoord coord1, KMLCoord coord2) {
		LatLon ll1 = LatLon.fromDegrees(coord1.getLat(), coord1.getLon());
		LatLon ll2 = LatLon.fromDegrees(coord2.getLat(), coord2.getLon());
		return LatLon.ellipsoidalDistance(ll1, ll2, 6378137, 6356752.3); // consts
		// from
		// wikipedia
	}

	private void setVerticalExaggeration(double ve) {
		// if vertical exaggeration has changed, need to rebuild display lists
		if (ve != this.verticalExaggeration) {
			verticalExaggeration = ve;
			setInvalid(true);
		}
	}

	static class PloygonTessCallback extends GLUtessellatorCallbackAdapter {
		DrawContext dc;

		PloygonTessCallback(DrawContext dc) {
			this.dc = dc;
		}

		public void begin(int i) {
			dc.getGL().glBegin(i);
		}

		public void end() {
			dc.getGL().glEnd();
		}

		public void combine(double[] coords, Object[] data, float[] weight,
				Object[] outData) {
			double[] vertex = new double[6];
			int i;

			vertex[0] = coords[0];
			vertex[1] = coords[1];
			vertex[2] = coords[2];
			for (i = 3; i < 6/* 7OutOfBounds from C! */; i++)
				vertex[i] = weight[0] //
						* ((double[]) data[0])[i]
						+ weight[1]
						* ((double[]) data[1])[i]
						+ weight[2]
						* ((double[]) data[2])[i]
						+ weight[3]
						* ((double[]) data[3])[i];
			outData[0] = vertex;

		}

		public void vertex(Object o) {
			double vals[] = (double[]) o;
			dc.getGL().glVertex3dv(vals, 0);
		}

		public void error(int i) {
			System.out.println("Error: " + i);
		}
	}

}

