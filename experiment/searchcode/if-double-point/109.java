<<<<<<< HEAD
 /*
 * Copyright Robin Lindh Nilsson 2010
 */
package physics.entities;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;
import javax.swing.JPanel;
import physics.Vector;
import physics.Collision;
import physics.GravityInterpolatorRK4;

/**
 * Ett RigidBody-objekt är en grafisk representation av ett fysiskt objekt
 * i två dimensioner. Objektet implementerar "rigid body"-fysik
 * för gravitation och kollisioner, för att efterlikna de fysikaliska egenskaper
 * hos ett objekt som ej kan deformeras. 
 * <br/><br/>
 * Objektet skapas utifrån en BufferedImage (bild) som skickas till konstruktorn.
 * Objektet utformas av alla icke-transparenta pixlar i denna bild.
 * Objektets bredd och höjd (w,h) sätts till bildens dimensioner. Formen lagras
 * sedan i en boolean[w][h] för att underlätta och snabba upp läsning av pixeldata.
 * <br/><br/>
 * För att använda en RigidBody i en fysiksimulation, skapa en instans av RigidBody,
 * lägg till den till någon grafisk container, t.ex. en JFrame eller JPanel.
 * Se till att fysikobjektets update anropas varje simulations-steg.
 * Fysik-objektet kommer automatiskt ritas ut varje gång förälder-containern
 * ritas ut.
 * <br/><br/>
 * I den nuvarande implementationen av RigidBody kolliderar objekt
 * automatiskt med förälder-containerns kanter. Två rigid bodies
 * kolliderar inte med varandra, eftersom klassen än så länge bara har stöd
 * för kollision mellan en RigidBody och rektanglar.
 *
 * @author Robin Lindh Nilsson
 */
public class RigidBody extends JPanel {

	public static final double START_VEL_X = 0;
	public static final double START_VEL_Y = 0;
	public static int groundLevel = 500; //initieras av GamePanel

	public double density; //mass per pixel
	public Vector pos;
	public double rotation = 0;
	public double angularVel = 0;
	public Vector vel;
	public double collisionRadius; // Radie för kollisions-cirkeln som används för första kollisionskontroll
	public double damping = 0.5f; // Cofficient of Restitution
	public double gravity = 400;
	public double friction = 0.01f;
	private boolean isEmpty = true; //anger huruvida objektet innehåller några pixlar
	private boolean pixels[][]; //array with pixel data, true means there is a pixel at the coordinate
	private BufferedImage image; //en 'bild' som innehåller objektets form.
	private int w, h; //dimensions
	private Vector centerGravityLocal; //Center of Gravity
	private double mass;
	private double momentInertia; //moment of inertia
	private GravityInterpolatorRK4 interpolator;

	//används endast lokalt av splitImageIntoRigidBodies
	private static boolean[][] traversed;
	private static boolean[][] isTransparent;
	private static boolean[][] opaqueGroup;

	public RigidBody(BufferedImage image, double density, Vector position) {
		this(image, density);
		pos = position;
	}

	public RigidBody(BufferedImage image, double density) {
		this.image = image;
		this.density = density;
		updateShape();
		updateMass();
		updateMomentOfInertia();
		pos = new Vector(centerGravityLocal);
		vel = new Vector(START_VEL_X, START_VEL_Y);
		interpolator = new GravityInterpolatorRK4(this);

		setOpaque(false); // sätt panelen genomskinlig
		setOpaque(true);
		setBackground(Color.blue);
	}

	public RigidBody(RigidBody clone) {
		this(clone.image, clone.density, clone.pos);
	}

	/**
	 * Uppdaterar objektet motsvarande den förflutna tiden dt.
	 * Anropas varje frame i fysiksimulationen,
	 * eller så ofta som möjligt för en precis interpolation.
	 * @param dt förfluten tid sedan senaste anropet, i sekunder.
	 */
	public void update(double dt) {
		if (isEmpty) {
			return;
		}

		interpolator.update(dt);
		//rotation += angularVel * dt;
		if(rotation > Math.PI + 0.000001)
			rotation -= 2*Math.PI;
		if(rotation < -Math.PI - 0.000001)
			rotation += 2*Math.PI;
		
		handleCollisions();
	}

	@Override
	public void paintComponent(Graphics g) {
		paintAtCoordinate(g, new Point((int)pos.x, (int)pos.y));
	}

	public void paintAtCoordinate(Graphics g, Point coord) {

		
		if (isEmpty) {
			return;
		}
		Graphics2D g2d = (Graphics2D) g;
		AffineTransform original = g2d.getTransform();
		//transformera till rätt koordinatsystem
		g2d.translate(coord.x, coord.y);
		g2d.rotate(-rotation, 0, 0);
		g2d.drawImage(image, (int) -centerGravityLocal.x, (int) -centerGravityLocal.y, this);
		g2d.setColor(Color.green);
		g2d.fillOval(-2, -2, 4, 4);
		g2d.setTransform(original);
	}

	/**
	 * Beräknar och uppdaterar objektets höjd och bredd, och skapar en matris
	 * med pixeldata för objektet, utifrån nuvarande BufferedImage-objekt.
	 */
	public final void updateShape() {
		w = image.getWidth();
		h = image.getHeight();

		pixels = new boolean[w][h];

		int n = 0;
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				int rgb = image.getRGB(i, j);
				if (rgb == 0) { //om vit
					//transparent
					pixels[i][j] = false;
				} else {
					//non-transparent
					pixels[i][j] = true;
					n++;
				}
			}
		}

		isEmpty = n == 0;

		updateMass();
		updateMomentOfInertia();
	}

	/**
	 * Beräknar och uppdaterar objektets massa och tyngdpunkt,
	 * baserat på nuvarande pixeldata och densitet.
	 * Om objektet inte innehåller några fyllda pixlar sätts
	 * massan till noll, och tyngdpunkten till null.
	 */
	public final void updateMass() {
		centerGravityLocal = new Vector();

		int n = 0;

		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				if (pixels[i][j]) {
					n++;
					centerGravityLocal.add(i, j);
				}
			}
		}

		if (n == 0) {
			centerGravityLocal = null;
			mass = 0;
		} else {
			centerGravityLocal.divide(n);
			mass = n * density;
		}
	}

	/**
	 * Beräknar och uppdaterar moment of inertia,
	 * baserat på nuvarande pixeldata, densitet och tyngdpunkt.
	 * Beräknar också radien för kollisionscirkeln i samma metod, av effektivitetsskäl.
	 * Om objektet inte innehåller några fyllda pixlar sätts kollisionsradien
	 * och moment of inertia till 0.
	 */
	public final void updateMomentOfInertia() {
		double sum = 0;
		int n = 0;
		collisionRadius = 0;

		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				if (pixels[i][j]) {
					n++;
					Vector delta = Vector.difference(centerGravityLocal, new Vector(i, j));
					double distance = delta.length();
					sum += distance * distance;
					if (distance > collisionRadius) {
						collisionRadius = distance;
					}
				}
			}
		}

		if (n == 0) {
			momentInertia = 0;
			collisionRadius = 0;
		} else {
			momentInertia = sum / n * mass;
		}
	}

	/**
	 * Applicerar en impuls på objektet.
	 * Impulsen ger upphov till en ändring i hastigheten.
	 * Om impulsens angreppspunkt är skiljd från tyngdpunkten uppstår även
	 * en förändring i vinkelhastigheten.<br/>
	 * Genom att göra en interpolation över många frames,
	 * och varje frame applicera en impuls F * dt, där dt är tiden sedan föregående frame,
	 * fås resultatet av att kraften F verkar under en längre tid och orsakar acceleration.
	 * Ju kortare intervall man interpolerar med, desto bättre precision får simulationen.
	 *
	 * @param impulse Impulsen som appliceras, som en 2D-vektor
	 * @param origin Impulsens angreppspunkt, uttryckt i koordinatsystemet
	 * för containern som innehåller detta objekt.
	 */
	public void applyImpulse(Vector impulse, Vector origin) {
		if (isEmpty) {
			return;
		}
		if (impulse.length() == 0) {
			return;
		}

		//rotation axis
		Vector r = Vector.difference(origin, pos);

		//calculate torque (tao = r x F)
		double torque = Vector.cross(r, impulse);
		//apply angular acceleration (tao = I * alpha)
		angularVel += torque / momentInertia;

		//apply normal acceleration (F = m*a)
		vel.add(impulse.x / mass, impulse.y / mass);
	}

	/**
	 * Detekterar och hanterar alla kollisioner för detta RigidBody-objekt.
	 * Bör anropas varje frame i simulationen, eller så ofta som möjligt för bästa precision.
	 */
	protected void handleCollisions() {
		if (isEmpty) {
			return;
		}

		//marknivå
		handleCollision(Collision.checkCollision(this, new Rectangle(-50, groundLevel, 100000, 50)));


	}

	/**
	 * Hanterar en enskild kollision.
	 * Om null skickas in sker ingen kollision.
	 * @param col Kollisionsdata
	 */
	protected void handleCollision(Collision col) {
		if (isEmpty) {
			return;
		}

		if (col != null) {
			Vector tangent = new Vector(-col.normal.y, col.normal.x);
			Vector r = new Vector(col.point).subtract(pos);
			Vector pointVel = getPointVelocity(col.point);
			double tangentVel = Vector.dot(pointVel, tangent);


			//flytta ut objekten från varandra, ut ur kollisionsläget
			pos.add(new Vector(col.normal).multiply(col.overlap));

			double tmp = Vector.cross(r, col.normal);
			double impulseAbs = -(1 + damping) * Vector.dot(pointVel, col.normal) / (1 / mass + tmp * tmp / momentInertia);
			Vector impulse = new Vector(col.normal).multiply(impulseAbs);



			//friction force = my * m * g
			double frictionForce = gravity * mass * friction * -Math.signum(tangentVel);

			//beräkna den kraft som skulle krävas för att ge kollisionspunkten hastigheten noll längs tangenten
			double rScalar = r.length();
			double frictionForceToStop = (-tangentVel) / (1/mass + rScalar*rScalar/momentInertia);

			//friktionskraft kan inte vara större än att objektet stannar längs tangenten,
			//så om den beräknade friktionen är större än vad som krävs för att
			//"äta upp" hela punktens hastighet,
			//anpassa istället friktionen så att den nya tangenthastigheten blir noll
			if(Math.abs(frictionForce) > Math.abs(frictionForceToStop)) {
				//det som krävs för att tangenthastigheten ska bli noll
				frictionForce = frictionForceToStop;
			}

			//applicera friktionskraften längs tangenten med hjälp av F = m * dv
			impulse.add(new Vector(tangent).multiply(frictionForce));

			applyImpulse(impulse, col.point);
		}
	}



	/** Delar upp bilddatan i flera RigidBody-objekt, genom att gruppera alla icke-transparenta pixlar
	 * avgränsade av transparenta pixlar. Varje separat sammanhängande grupp av
	 * icketransparenta pixlar blir ett nytt RigidBody-objekt.
	 * @param imageData en BufferedImage med pixeldata. Alla icketransparenta pixlar representerar materia.
	 * @return En ArrayList med alla nya RigidBody-objekt.
	 * Varje objekt representeras av en boolsk matris med true för fyllda pixlar.
	 */
	public static ArrayList<boolean[][]> splitImageIntoRigidBodies(BufferedImage imageData) {
		int width = imageData.getWidth();
		int height = imageData.getHeight();
		traversed = new boolean[width][height];
		isTransparent = new boolean[width][height];

		ArrayList<boolean[][]> returnValue = new ArrayList<boolean[][]>();

		//läs över bilddatan i en boolsk matris för effektivare läsning
		//fyll traversed med false i samma loop pga effektivitet
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				isTransparent[i][j] = imageData.getRGB(i, j) == -1;
				traversed[i][j] = false;
			}
		}

		int wingCounter = 0;
		//leta efter fyllda pixlar
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (traversed[i][j]) {
					continue;
				}
				if (isTransparent[i][j]) {
					traversed[i][j] = true;
				} else {
					//när en fylld pixel påträffas scannas bilden
					//rekursivt efter intilliggande fyllda pixlar
					opaqueGroup = new boolean[width][height];
					findOpaqueGroup(i, j);

					//loopa igenom punkterna en första gång
					//för att hitta hur stor matrisen behöver vara
					int maxX = Integer.MIN_VALUE;
					int maxY = Integer.MIN_VALUE;
					for (int x = 0; x < width; x++) {
						for (int y = 0; y < height; y++) {
							if (opaqueGroup[x][y]) {
								if (x > maxX) {
									maxX = x;
								}
								if (y > maxY) {
									maxY = y;
								}
							}
						}
					}
					//pixeldatan lagrar true för alla fyllda pixlar, false för genomskinliga
					boolean[][] pixelData = new boolean[maxX + 1][maxY + 1];
					for (boolean[] column : pixelData) {
						Arrays.fill(column, false);
					}

					//loopa igenom punkterna en andra gång och lägg till dem i matrisen
					for (int x = 0; x < width; x++) {
						for (int y = 0; y < height; y++) {
							if (opaqueGroup[x][y]) {
								pixelData[x][y] = true;
							}
						}
					}
					returnValue.add(pixelData);
				}
			}
		}

		return returnValue;
	}

	private static void findOpaqueGroup(int x, int y) {

		Stack<Point> checkPoints = new Stack<Point>();
		checkPoints.add(new Point(x,y));

		while(!checkPoints.empty()) {
			Point point = checkPoints.pop();
			x = point.x;
			y = point.y;

			//skydda mot array index out of bounds
			if (x < 0 || y < 0 || x >= traversed.length || y >= traversed[0].length) {
				continue;
			}

			if (traversed[x][y]) {
				continue;
			}

			traversed[x][y] = true;

			if (!isTransparent[x][y]) {
				opaqueGroup[x][y] = true;
				checkPoints.add(new Point(x + 1, y));
				checkPoints.add(new Point(x - 1, y));
				checkPoints.add(new Point(x, y + 1));
				checkPoints.add(new Point(x, y - 1));
			}
		}
	}

	/**
	 * Beräknar punktens hastighet, med hänsyn till objektets rotationshastighet
	 * @param point punktens världskoordinater
	 * @return en vektor med punktens hastighet
	 */
	public Vector getPointVelocity(Vector point) {
		return calcPointVelocity(point, pos, vel, angularVel);
	}

	public static Vector calcPointVelocity(Vector point, Vector centerMass, Vector velocity, double angularVelocity) {
		Vector r = new Vector(point).subtract(centerMass);
		Vector rPerpendicular = new Vector(r.y, -r.x);
		Vector pointVelocity = new Vector(rPerpendicular).multiply(angularVelocity).add(velocity);
		return pointVelocity;
	}

	/**
	 * checks if this object contains the point, which is if the pixel is not transparent
	 * Does not take rotation into account!
	 * @param The point in world coordinates
	 * @return boolean indicating if the point is a part of this physics object
	 */
	public boolean containsPointNoRot(Point globalPoint) {

		boolean returnVal;
		try {
			int x = (int) (pos.x - centerGravityLocal.x + globalPoint.x);
			int y = (int) (pos.y - centerGravityLocal.y + globalPoint.y);
			returnVal = pixels[x][y];
		} catch(ArrayIndexOutOfBoundsException ex) {
			returnVal = false;
		}
		return returnVal;
	}

	public boolean isEmpty() {
		return isEmpty;
	}

	public double getMass() {
		return mass;
	}

	public double getMomentInertia() {
		return momentInertia;
	}

=======
/*
 * Project Info:  http://jcae.sourceforge.net
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 *
 * (C) Copyright 2007, by EADS France
 */

package org.jcae.viewer3d.cad.occ;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import org.jcae.opencascade.jni.*;
import org.jcae.viewer3d.cad.CADDomainAdaptor;
import org.jcae.viewer3d.cad.DefaultFaceMesh;
import org.jcae.viewer3d.cad.FaceMesh;

/**
 * Meshing parameters were taken from Opencascade sources. See
 * <ul>
 *  <li>src/AIS/AIS_TexturedShape.cxx</li>
 *  <li>src/AIS/AIS_Shape.cxx</li>
 *  <li>src/Prs3d/Prs3d_Drawer.cxx</li>
 *  <li>src/BRepMesh/BRepMesh.cxx</li>
 *  <li>inc/BRepMesh_IncrementalMesh.hxx</li>  
 * </ul>
 * @author Jerome Robert
 *
 */
public class OCCFaceDomain extends CADDomainAdaptor
{
	private static int meshIter=3;
	private ArrayList<FaceMesh> faceMeshes;
	private Color[] facesColors;
	private static boolean debug=false;
	public OCCFaceDomain(TopoDS_Shape shape)
	{	
		TopExp_Explorer explorer = new TopExp_Explorer();
		TopLoc_Location loc = new TopLoc_Location();
		faceMeshes=new ArrayList<FaceMesh>();
				
		for (explorer.init(shape, TopAbs_ShapeEnum.FACE); explorer.more(); explorer.next())
		{						
			TopoDS_Shape s = explorer.current();
			if (!(s instanceof TopoDS_Face)) continue; // should not happen!
			TopoDS_Face face = (TopoDS_Face)s;
			Poly_Triangulation pt = BRep_Tool.triangulation(face,loc);
			
			float error=0.001f*getMaxBound(s)*4;
			//float error=0.0001f;
			int iter=0;
			while((pt==null)&(iter<meshIter)){
				new BRepMesh_IncrementalMesh(face,error, false);
				//new BRepMesh_IncrementalMesh(face,error, true);
				pt = BRep_Tool.triangulation(face,loc);				
				error/=10;
				iter++;
			}
						
			/*if(pt==null)
			{
				//System.err.println("Meshing "+face);
				//BRepMesh.mesh(face, boundingBoxDeflection);				
				new BRepMesh_IncrementalMesh(face, 0.1, true);
				pt = BRep_Tool.triangulation(face,loc);
			}	*/		
			
			if (pt==null)
			{
				System.err.println("Triangulation failed for face "+face+". Trying other mesh parameters.");
				faceMeshes.add(new DefaultFaceMesh(new float[0], new int[0]));
				continue;
				/*BRepMesh_IncrementalMesh mesh = new BRepMesh_IncrementalMesh();
				mesh.setAngle(angle);
				mesh.setRatio(ratio);
				mesh.update(face);
				pt = BRep_Tool.triangulation(face,loc);
				if (pt==null)
				{
					System.err.println("Cannot triangulate face "+face);
					continue;
				}*/
			}
			else if(debug){
				System.out.println("Triangulation succed for face "+face);
			}
			
			
			double[] dnodes = pt.nodes();
			final int[] itriangles = pt.triangles();						
			/*double[] point=new double[3];
			double[] meshNormal=computeMeshNormal(dnodes, itriangles, point);
			double[] cadNormal=getNormal(point, face);
			
			System.out.println("meshNormal "+meshNormal[0]+" "+meshNormal[1]+" "+meshNormal[2]);
			System.out.println("cadNormal "+cadNormal[0]+" "+cadNormal[1]+" "+cadNormal[2]);
			
			if(scalarProduct(meshNormal, cadNormal)<0)
			{				
				System.out.println("reverseMesh");
				reverseMesh(itriangles);
			}*/
			if(face.orientation()==TopAbs_Orientation.REVERSED)
			{
				reverseMesh(itriangles);
			}
			

			final float[] fnodes=new float[dnodes.length];			
			
			if(loc.isIdentity())
			{
				for(int i=0; i<dnodes.length; i++)
				{
					fnodes[i]=(float) dnodes[i];
				}				
			}
			else
				transformMesh(loc, dnodes, fnodes);
				
			faceMeshes.add(new DefaultFaceMesh(fnodes, itriangles));
		}
	}
	
	void transformMesh(TopLoc_Location loc, double[] src, float[] dst)
	{
		double[] matrix=new double[16];
		loc.transformation().getValues(matrix);
		Matrix4d m4d=new Matrix4d(matrix);
		Point3d p3d=new Point3d();
		for(int i=0; i<src.length; i+=3)
		{
			p3d.x=src[i+0];
			p3d.y=src[i+1];
			p3d.z=src[i+2];
			m4d.transform(p3d);
			dst[i+0]=(float) p3d.x;
			dst[i+1]=(float) p3d.y;
			dst[i+2]=(float) p3d.z;
		}		
	}
	
	/**
	 * Compute the bounding box of the shape and
	 * return the maximum bound value
	 * @param shape
	 * @return
	 */
	private static float getMaxBound(TopoDS_Shape shape){
		Bnd_Box box = new Bnd_Box(); 
		BRepBndLib.add(shape,box);
		double[] bbox = box.get();
		double minBoundingBox=
			Math.max(Math.max(bbox[3]-bbox[0], bbox[4]-bbox[1]), bbox[5]-bbox[2]);
		return (float)minBoundingBox;
	}
	
	/**
	 * Create a Colored Shape
	 * @param shape
	 * @param facesColors
	 */
	public OCCFaceDomain(TopoDS_Shape shape,Color[] facesColors){
		this(shape);
		this.facesColors=facesColors;
	}
	
	/**
	 * @param itriangles
	 */
	static private void reverseMesh(int[] itriangles)
	{
		int tmp;
		for(int i=0; i<itriangles.length; i+=3)
		{
			tmp=itriangles[i];
			itriangles[i]=itriangles[i+1];
			itriangles[i+1]=tmp;
		}
	}

	static private double scalarProduct(double[] v1, double[] v2)
	{
		return v1[0]*v2[0]+v1[1]*v2[1]+v1[2]*v2[2];
	}
	/**
	 * 
	 * @param nodes the nodes of the mesh
	 * @param trias the triangles of the mesh
	 * @param point double[3] array which will receive the point were
	 * the normal is computed
	 * @return The normal a the point <code>point</code>
	 */
	static private double[] computeMeshNormal(double[] nodes, int[] trias, double[] point)
	{
		double[] toReturn=new double[3];
		int index=(trias.length/6)*3;
		double[] v1=new double[3];
		double[] v2=new double[3];
		
		v1[0]=nodes[trias[index]*3]-nodes[trias[index+1]*3];
		v1[1]=nodes[trias[index]*3+1]-nodes[trias[index+1]*3+1];
		v1[2]=nodes[trias[index]*3+2]-nodes[trias[index+1]*3+2];

		v2[0]=nodes[trias[index+2]*3]-nodes[trias[index+1]*3];
		v2[1]=nodes[trias[index+2]*3+1]-nodes[trias[index+1]*3+1];
		v2[2]=nodes[trias[index+2]*3+2]-nodes[trias[index+1]*3+2];
		
		//compute center of the triangle
		point[0]=nodes[trias[index]*3]+nodes[trias[index]*3+1]+nodes[trias[index]*3+2];
		point[1]=nodes[trias[index+1]*3]+nodes[trias[index+1]*3+1]+nodes[trias[index+1]*3+2];
		point[2]=nodes[trias[index+2]*3]+nodes[trias[index+2]*3+1]+nodes[trias[index+2]*3+2];
		
		/*System.out.println("v1 "+v1[0]+" "+v1[1]+" "+v1[2]);
		System.out.println("v2 "+v2[0]+" "+v2[1]+" "+v2[2]);
		System.out.println("point "+point[0]+" "+point[1]+" "+point[2]);*/
		//compute the normal
		toReturn[0]=(v1[1]*v2[2])-(v1[2]*v2[1]);
		toReturn[1]=(v2[0]*v1[2])-(v2[2]*v1[0]);
		toReturn[2]=(v1[0]*v2[1])-(v1[1]*v2[0]);
		
		return toReturn;
	}
	
	static private double[] getNormal(double[] point, TopoDS_Face face)
	{
		Geom_Surface gs=BRep_Tool.surface(face);
		GeomLProp_SLProps myLprop = new GeomLProp_SLProps(2, 0.0001);
		myLprop.setSurface(gs);
		GeomAPI_ProjectPointOnSurf pof=new GeomAPI_ProjectPointOnSurf(point, gs);
		double[] uv=new double[2];
		pof.lowerDistanceParameters(uv);
		myLprop.setParameters(uv[0], uv[1]);
		double[] toReturn=myLprop.normal();
		if(face.orientation()==TopAbs_Orientation.REVERSED)
		{
			toReturn[0]*=-1;
			toReturn[1]*=-1;
			toReturn[2]*=-1;
		}
		return toReturn;
	}
	
	/* (non-Javadoc)
	 * @see org.jcae.viewer3d.cad.CADDomainAdaptor#getFaceIterator()
	 */
	@Override
	public Iterator<FaceMesh> getFaceIterator()
	{
		return faceMeshes.iterator();
	}
	/**
	 * Return the Color of the ith Face of the iterator 
	 * @param i
	 * @return
	 */
	public Color getFaceColor(int i){
		if(facesColors==null) return null;
		return facesColors[i];
	}
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

