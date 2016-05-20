package Misc;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Shape;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.Billboard;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Font3D;
import javax.media.j3d.FontExtrusion;
import javax.media.j3d.LineArray;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Text3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.geometry.Sphere;

import datastructure.Config;

/**
 * Diese Klasse enthaelt viele Nuetzliche statische Methoden
 * Sie stellt Java3D Appearances und primitive Shapes zur verfuegung.
 * hier befindet sich unter anderem auch die Methode um einen Cylinder
 * zwischen zwei gegebene Punkte zu legen. Dies ist mittels Vector-
 * Mathematik realisiert.
 * @author Johannes
 *
 */
public class StaticTools {
	
	public static BoundingSphere defaultBounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);
	   
	static Appearance app_red;
	static Appearance app_blue;
	static Appearance app_green;
	static Appearance app_yellow;
	static Appearance app_standard;
	
	static float [] defaultCameraPos = {3,8,35};
	static int defaultCameraRotate = 180; // General Y Rotation in Degree

	static Config config;
	public static final String configFileName = "CSMCConf.xml";

	/*
	 * Geklaut ;)
	 */
	 public static BranchGroup ChartJ3DLink(Point3f a,Point3f b,float r)
	 {
		 float x1 =(float)a.x;
		 float y1 =(float)a.y;
		 float z1 =(float)a.z;
		 float x2 =(float)b.x;
		 float y2 =(float)b.y;
		 float z2 =(float)b.z;
		    //ChartJ3DLink starting at (x1,y1,z2) goint to (x2,y2,z2)
		 //   System.out.println("\nChartJ3DLink from ("+x1+","+y1+","+z1+") to ("+x2+","+y2+","+z2+")");

		    TransformGroup tg = new TransformGroup();
		    Transform3D transform = new Transform3D();

		    //Work out center...
		    float cx = (x1+x2) / 2;
		    float cy = (y1+y2) / 2;
		    float cz = (z1+z2) / 2;
		    Vector3f vector = new Vector3f(cx, cy, cz);
//		    System.out.println("Center: cx="+cx+" cy="+cy+" cz="+cz);
		    transform.setTranslation(vector);

		    //Work out the length
		    double dx = (double) (x2-x1);
		    double dy = (double) (y2-y1);
		    double dz = (double) (z2-z1);
		    //System.out.println("Distance: dx="+dx+" dy="+dy+" dz="+dz);
		    float s = (float) Math.sqrt(dx*dx + dy*dy + dz*dz);
		   // System.out.println("Length: s="+s);

		    //Work out rotation on Z-axis...
		    double rotZ=0;
		    if (dx==0){
		      rotZ=0;
		    }else if (dy==0){
		      //CHANGE THIS...
		      rotZ=Math.PI/2;//if this is negative, remove the -rotX below
		      if (dx<0){
		      //  System.out.println("Reversing rotZ (y=0)");
		        rotZ = -rotZ;
		      }
		    }else if (dx!=0){
		      rotZ=-Math.atan(Math.abs(dx)/Math.abs(dy));
		      if (dy<0){
		        //System.out.println("Altering rotZ (y<0)");
		        if (dx>0){
		          rotZ = Math.PI+rotZ;
		        }else{
		          rotZ = Math.PI*3-rotZ;
		        }
		      }else{
		        if (dx>0){
		          //System.out.println("Altering rotZ (y>0 x>0)");
		          rotZ=-rotZ;
		        }
		      }
		    }
		    rotZ=-rotZ;
		  // double degrees = rotZ * (180d/Math.PI);
		    //System.out.println("Z-Rot: rad="+ rotZ+" deg="+degrees);

		    //Work out rotation on X axis...
		    double rotX = Math.atan ( Math.abs(z2-cz) / (0.5*Math.sqrt(dx*dx+dy*dy)) );
		    rotX=-rotX;//If this goes, alter the CHANGE THIS above
		    if (z2-cz>0){
		      //CHANGE THIS...
		      //System.out.println("blah");
		      rotX = -rotX;
		    }
		    //rotX=Math.PI-rotX;
		   // degrees = rotX * (180d/Math.PI);
		    //System.out.println("X-Rot: rad="+ rotX+" deg="+degrees);
		    //rotX=0;

		    Transform3D rotationZ = new Transform3D();
		    Transform3D rotationX = new Transform3D();
		    rotationZ.rotZ(rotZ);
		    rotationX.rotX(rotX);

		    transform.mul(rotationZ);
		    transform.mul(rotationX);
		    tg.setTransform(transform);
		    Cylinder cylinder = new Cylinder(r,s,createAppearance());

		    tg.addChild(cylinder);
		    BranchGroup bg = new BranchGroup();
		    bg.setCapability(BranchGroup.ALLOW_DETACH);
		    bg.addChild(tg);
		    return (bg);
		  }
	 
	 
	 public static Transform3D getTransform(Point3f a,Point3f b)
	 {
		 if (a.equals(b))
			 return new Transform3D();
		 
		 float x1 =(float)a.x;
		 float y1 =(float)a.y;
		 float z1 =(float)a.z;
		 float x2 =(float)b.x;
		 float y2 =(float)b.y;
		 float z2 =(float)b.z;
		    //ChartJ3DLink starting at (x1,y1,z2) goint to (x2,y2,z2)
		 //   System.out.println("\nChartJ3DLink from ("+x1+","+y1+","+z1+") to ("+x2+","+y2+","+z2+")");

		    Transform3D transform = new Transform3D();

		    //Work out center...
		    float cx = (x1+x2) / 2;
		    float cy = (y1+y2) / 2;
		    float cz = (z1+z2) / 2;
		    Vector3f vector = new Vector3f(cx, cy, cz);
//		    System.out.println("Center: cx="+cx+" cy="+cy+" cz="+cz);
		    transform.setTranslation(vector);

		    //Work out the length
		    double dx = (double) (x2-x1);
		    double dy = (double) (y2-y1);
		    double dz = (double) (z2-z1);

		    //Work out rotation on Z-axis...
		    double rotZ=0;
		    if (dx==0){
		      rotZ=0;
		    }else if (dy==0){
		      //CHANGE THIS...
		      rotZ=Math.PI/2;//if this is negative, remove the -rotX below
		      if (dx<0){
		      //  System.out.println("Reversing rotZ (y=0)");
		        rotZ = -rotZ;
		      }
		    }else if (dx!=0){
		      rotZ=-Math.atan(Math.abs(dx)/Math.abs(dy));
		      if (dy<0){
		        //System.out.println("Altering rotZ (y<0)");
		        if (dx>0){
		          rotZ = Math.PI+rotZ;
		        }else{
		          rotZ = Math.PI*3-rotZ;
		        }
		      }else{
		        if (dx>0){
		          //System.out.println("Altering rotZ (y>0 x>0)");
		          rotZ=-rotZ;
		        }
		      }
		    }
		    rotZ=-rotZ;
		  // double degrees = rotZ * (180d/Math.PI);
		    //System.out.println("Z-Rot: rad="+ rotZ+" deg="+degrees);

		    //Work out rotation on X axis...
		    double rotX = Math.atan ( Math.abs(z2-cz) / (0.5*Math.sqrt(dx*dx+dy*dy)) );
		    rotX=-rotX;//If this goes, alter the CHANGE THIS above
		    if (z2-cz>0){
		      //CHANGE THIS...
		      //System.out.println("blah");
		      rotX = -rotX;
		    }
		    //rotX=Math.PI-rotX;
		   // degrees = rotX * (180d/Math.PI);
		    //System.out.println("X-Rot: rad="+ rotX+" deg="+degrees);
		    //rotX=0;

		    Transform3D rotationZ = new Transform3D();
		    Transform3D rotationX = new Transform3D();
		    rotationZ.rotZ(rotZ);
		    rotationX.rotX(rotX);

		    transform.mul(rotationZ);
		    transform.mul(rotationX);
		    return transform;
	 }
	 
	 
	 
	public static Node recktAngle(Point3f a,Point3f b,float r)
	{
		if (a != null && b != null && !a.equals(b))
		{
			BranchGroup bg = new BranchGroup();
			Point3f a2 = new Point3f(a);
			Point3f b2 = new Point3f(b);
			float z = b2.z;
			float x = a2.x;
			a2.x = b2.x;
			b2.z = a2.z;
			b2.x = x;
			a2.z = z;
			bg.addChild(ChartJ3DLink(a,a2,r));
			bg.addChild(ChartJ3DLink(a2,b,r));
			bg.addChild(ChartJ3DLink(b,b2,r));
			bg.addChild(ChartJ3DLink(a,b2,r));
			return bg;
			
		}
		return null;
	}
	
	public static BranchGroup line(Point3f a,Point3f b)
	{
		 // Plain line
	    Point3f[] plaPts = new Point3f[2];
	    plaPts[0] = new Point3f((float)a.x,
					    		(float)a.y,
					    		(float)a.z);
	    plaPts[1] = new Point3f((float)b.x,
					    		(float)b.y,
					    		(float)b.z);
	    
	    

	    LineArray pla = new LineArray(2, LineArray.COORDINATES);
	    pla.setCoordinates(0, plaPts);
	    Shape3D plShape = new Shape3D(pla, StaticTools.createAppearance());
	    BranchGroup lineGroup = new BranchGroup();
		lineGroup.addChild(plShape);
		lineGroup.setCapability(BranchGroup.ALLOW_DETACH);
		lineGroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		lineGroup.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
		lineGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		return lineGroup;
	}
	public static BranchGroup cylinder(Point3f a,Point3f b,float r)
	{
		//return line(a, b);
		/*
		BranchGroup bg = new BranchGroup();
		bg.setCapability(BranchGroup.ALLOW_DETACH);
		bg.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		bg.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
		bg.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		bg.setUserData("cyl");
		datastructure.Cylinder c = new datastructure.Cylinder(a, b, r);
		c.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
		bg.addChild(c);
		return bg;
		 * 
		 */
		return ChartJ3DLink(a,b,r);
	
	}
	public static BranchGroup cylinder(Point3f a,Point3f b,float r,Appearance app)
	{
		//return line(a, b);
		/*
		BranchGroup bg = new BranchGroup();
		bg.setCapability(BranchGroup.ALLOW_DETACH);
		bg.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		bg.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
		bg.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		bg.setUserData("cyl");
		datastructure.Cylinder c = new datastructure.Cylinder(a, b, r);
		c.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
		bg.addChild(c);
		return bg;
		 * 
		 */
		BranchGroup bg =ChartJ3DLink(a,b,r); 
		TransformGroup tg = (TransformGroup)bg.getChild(0);
		Cylinder c = (Cylinder) tg.getChild(0);
		c.setAppearance(app);
		return bg;
		
	}
	public static  BranchGroup createBillBoradText3D(String s,float scale)
	{
		BranchGroup bg  = new BranchGroup();
		bg.setCapability(BranchGroup.ALLOW_DETACH);
		Transform3D t = new Transform3D();
		t.setScale(scale);
		t.setTranslation(new Vector3d(0.5,0,0.5));
		
		TransformGroup tg  = new TransformGroup(t);
		tg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		bg.addChild(tg);
		double X1 = 0;
		double Y1 = 0;
		double X2 = 0.01;
		double Y2 = 0;
		Shape extrusionShape = new java.awt.geom.Line2D.Double(X1, Y1, X2, Y2);
		
		FontExtrusion fontEx = new FontExtrusion(extrusionShape);
		Font3D f3d = new Font3D(new Font("TimesRoman", Font.PLAIN, 1),fontEx);
		Text3D text  = new Text3D(f3d, s);
		//tg.addChild(new ColorCube(1));
		Shape3D shape = new Shape3D(text,createAppearance());
		tg.addChild(shape);	
		
		// check in biilboard
		Billboard billboard = new Billboard(tg, Billboard.ROTATE_ABOUT_POINT, new Point3f());
		billboard.setSchedulingBounds(defaultBounds);
		tg.addChild(billboard);
		
		return bg;
	}
	public static  BranchGroup createText3D(String s,float scale)
	{
		BranchGroup bg  = new BranchGroup();
		Transform3D t = new Transform3D();
		t.setScale(scale);
		t.setTranslation(new Vector3d(0.5,0,0.5));
		
		TransformGroup tg  = new TransformGroup(t);
		bg.addChild(tg);
		double X1 = 0;
		double Y1 = 0;
		double X2 = 0.01;
		double Y2 = 0;
		Shape extrusionShape = new java.awt.geom.Line2D.Double(X1, Y1, X2, Y2);
		
		FontExtrusion fontEx = new FontExtrusion(extrusionShape);
		Font3D f3d = new Font3D(new Font("TimesRoman", Font.PLAIN, 1),fontEx);
		Text3D text  = new Text3D(f3d, s);
		//tg.addChild(new ColorCube(1));
		Shape3D shape = new Shape3D(text,createAppearance());
		tg.addChild(shape);					 
		return bg;
	}
	
   public static  BranchGroup createText3D(String s)
    {
    	BranchGroup bg  = new BranchGroup();
    	Transform3D t = new Transform3D();
    	t.setScale(0.5);
    	t.setTranslation(new Vector3d(0.5,0,0.5));
    	
    	TransformGroup tg  = new TransformGroup(t);
    	bg.addChild(tg);
    	double X1 = 0;
    	double Y1 = 0;
    	double X2 = 0.01;
    	double Y2 = 0;
    	Shape extrusionShape = new java.awt.geom.Line2D.Double(X1, Y1, X2, Y2);
    	
    	FontExtrusion fontEx = new FontExtrusion(extrusionShape);
    	Font3D f3d = new Font3D(new Font("TimesRoman", Font.PLAIN, 1),fontEx);
    	Text3D text  = new Text3D(f3d, s);
    	//tg.addChild(new ColorCube(1));
    	Shape3D shape = new Shape3D(text,createAppearance());
    	tg.addChild(shape);					 
    	return bg;
    }
    
   
   public static TransformGroup createSphere(Point3f p,float size)
	{
		Transform3D t = new Transform3D();
		t.setTranslation(new Vector3f((float)p.x,
									(float)p.y,
									(float)	p.z));
		TransformGroup tg  = new TransformGroup(t);
		
		Sphere sphere  =new Sphere(size,StaticTools.createAppearance());
		sphere.setCapability(BranchGroup.ALLOW_PICKABLE_READ);
		sphere.setCapability(BranchGroup.ALLOW_PICKABLE_WRITE);
		
		tg.addChild(sphere);
		return tg;
		
	}
   
   public static TransformGroup createSphereWithText(Point3f p,float size,String s)
	{
		TransformGroup tg = StaticTools.createSphere(p, size);
		tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		tg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		Sphere sphere = (Sphere) tg.getChild(0);
		sphere.getShape().setUserData(s);
		tg.addChild(StaticTools.createText3D(s));
		
		// little color patching
		if (s.startsWith("R"))
			sphere.setAppearance(StaticTools.red());
		if (s.startsWith("L"))
			sphere.setAppearance(StaticTools.green());
		if (s.startsWith("O") || s.startsWith("X") || s.startsWith("Y")|| s.startsWith("Z"))
			sphere.setAppearance(StaticTools.yellow());
		
		return tg;	
	}
	
	static float[] nextRC()
	{
		float [] tuppelWhite = {0.1f,0.1f,0.1f};
		return tuppelWhite;
	}
	
    public static Appearance createAppearance() {
		if (app_standard == null )
		{
			app_standard = new Appearance();
			app_standard.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE);
			Color3f ambientColour = new Color3f();
			ambientColour.set(Color.BLACK);
			Color3f emissiveColour = new Color3f(0.0f, 0.1f, 0.0f);
			emissiveColour = new Color3f(nextRC());
			Color3f specularColour = new Color3f(1.0f, 1.0f, 1.0f);
			Color3f diffuseColour = new Color3f();
			diffuseColour.set(Color.BLACK);
			
			float shininess = 20.0f;
			app_standard.setMaterial(
					new Material(ambientColour,
							emissiveColour,
							diffuseColour,
							specularColour, 
							shininess)
			);
			
		}
	    return app_standard;
	}

	public static Appearance red() {
		if(app_red == null)
		{
			
			app_red = new Appearance();
			app_red.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE);
	    	Color3f ambientColour = new Color3f();
	    	ambientColour.set(Color.GRAY);
	    	Color3f emissiveColour = new Color3f(0.3f, 0.0f, 0.0f);
	    //	emissiveColour = new Color3f(nextRC());
	    	Color3f specularColour = new Color3f(1.0f, 0.3f, 0.3f);
	    	Color3f diffuseColour = new Color3f();
	    	diffuseColour.set(Color.GRAY);
	    	
	    	float shininess = 20.0f;
	    	app_red.setMaterial(
	    			new Material(ambientColour,
	    					emissiveColour,
	    					diffuseColour,
	    					specularColour, 
	    					shininess)
	    	);
		}
		return app_red;
	}

	public static Appearance green() {
    	if(app_green == null)
    	{
	         app_green = new Appearance();
	         app_green.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE);
	        Color3f ambientColour = new Color3f();
	        ambientColour.set(Color.GRAY);
	        Color3f emissiveColour = new Color3f(0.0f, 0.3f, 0.0f);
	    //    emissiveColour = new Color3f(nextRC());
	        Color3f specularColour = new Color3f(0.3f, 1.0f, 0.3f);
	        Color3f diffuseColour = new Color3f();
	        diffuseColour.set(Color.GRAY);
	
	        float shininess = 20.0f;
	        app_green.setMaterial(
	        		new Material(ambientColour,
		        				emissiveColour,
				                diffuseColour,
				                specularColour, 
				                shininess)
	        		);
    	}
        return app_green;
    }
	
    public static Appearance blue() {
    	if(app_blue == null)
    	{
    		app_blue = new Appearance();
    		app_blue.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE);
	    	Color3f ambientColour = new Color3f();
	    	ambientColour.set(Color.GRAY);
	    	Color3f emissiveColour = new Color3f(0.0f, 0.0f, 0.3f);
	    	//	emissiveColour = new Color3f(nextRC());
	    	Color3f specularColour = new Color3f(01.3f, 0.3f, 1.0f);
	    	Color3f diffuseColour = new Color3f();
	    	diffuseColour.set(Color.GRAY);
	    	
	    	float shininess = 20.0f;
	    	app_blue.setMaterial(
	    			new Material(ambientColour,
	    					emissiveColour,
	    					diffuseColour,
	    					specularColour, 
	    					shininess)
	    	);
    	}
    	return app_blue;
    }
    
    public static Appearance yellow() {
    	if(app_yellow == null)
    	{
    		app_yellow = new Appearance();
    		app_yellow.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE);
    		Color3f ambientColour = new Color3f();
    		ambientColour.set(Color.yellow);
    		Color3f emissiveColour = new Color3f(1.0f, 1.0f, 0.0f);
    		//	emissiveColour = new Color3f(nextRC());
    		Color3f specularColour = new Color3f(1.0f, 1.3f, 0.0f);
    		Color3f diffuseColour = new Color3f();
    		diffuseColour.set(Color.yellow);
    		
    		float shininess = 100.0f;
    		app_yellow.setMaterial(
    				new Material(ambientColour,
    						emissiveColour,
    						diffuseColour,
    						specularColour, 
    						shininess)
    		);
    	}
    	return app_yellow;
	}

    
    public static void makeBlendedTransparent(Appearance app, float tranpranecy)
    {
		if (!app.isLive())
			app.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE);
		TransparencyAttributes ta = new TransparencyAttributes();
		ta.setTransparency(tranpranecy);
		ta.setTransparencyMode(TransparencyAttributes.BLENDED);    
		app.setTransparencyAttributes(ta);
    }

	public static void lights(BranchGroup root)
	{
		 Color3f ambientLightColour = new Color3f(0.9f, 0.9f, 0.9f);
	        AmbientLight ambientLight = new AmbientLight(ambientLightColour);
	        ambientLight.setInfluencingBounds(defaultBounds);
	        Color3f directionLightColour = new Color3f(1.0f, 1.0f, 1.0f);
	        Vector3f directionLightDir = new Vector3f(-1.0f, -1.0f, -1.0f);
	        DirectionalLight directionLight = new DirectionalLight(directionLightColour, directionLightDir);
	        directionLight.setInfluencingBounds(defaultBounds);
	        root.addChild(ambientLight);
	        root.addChild(directionLight);
	        Background background =new Background(new Color3f(0.0f,0.1f,0.4f));
	        background.setApplicationBounds(defaultBounds);
	        root.addChild(background);
	}
	
	
	public static Transform3D getDefaultCameraPos() {
		Transform3D transform = new Transform3D();
		transform.setTranslation(new Vector3d(defaultCameraPos[0],defaultCameraPos[1],defaultCameraPos[2]));
		Transform3D transform2 = new Transform3D();
		transform2.rotY(Math.toRadians(defaultCameraRotate));
		transform2.invert();
		
		transform2.mul(transform);
		
		//u.getViewingPlatform().getViewPlatformTransform().setTransform(transform2);
		return transform2;
		/*
		 * Nice to have
		rootTransform.rotY(Math.toRadians(defaultCameraRotate));
		Transform3D transformX = new Transform3D();
		transformX.rotX(Math.toRadians(objectRotateX));
		rootTransform.mul(transformX);
		rootTransformtGroup.setTransform(rootTransform);
		 */
		//System.out.println(rootTransform);
//		System.out.println("Campos: "+campos[0] + ' ' +campos[1] + ' ' + campos[2] + ' ');
	}
	
	public static JFrame HidingFrame(Component c, String titel)
	{
		
		JFrame frame;
		frame = new JFrame(titel);
		frame.addWindowListener(new WindowAdapter() {
		      public void windowClosing(WindowEvent e) {
			        if (e.getSource().getClass() == JFrame.class)
			        {
			        	JFrame f = (JFrame) e.getSource();
			        	f.setVisible(false);
			        }
			  }
		});
		frame.setSize(500,500);
		frame.add(c);
		frame.setVisible(true);
		return frame;
	}
	
	public static File openDialog(final String filter, boolean isSaveDialog)
	{
		JFileChooser chooser  = new JFileChooser(".");
		FileFilter ff = new FileFilter() {
			@Override
			public String getDescription() {
				return "*." +filter;
			}
			@Override
			public boolean accept(File arg0) {
				if (arg0.getName().endsWith(filter) || arg0.isDirectory())
					return true;
				return false;
			}
		};
		chooser.setFileFilter(ff);
		
		if(isSaveDialog)
			chooser.showSaveDialog(null);
		else
			chooser.showOpenDialog(null);
		
		File file = chooser.getSelectedFile();
		if(isSaveDialog)
		{
			if (!file.getName().endsWith(filter))
				file = new File(file.getAbsolutePath() + "." +filter);
				
		}
		return file;
	}


	public static Config getConfig() {
		return config;
	}


	public static void setConfig(Config config) {
		StaticTools.config = config;
	}
	
	public static java.io.FileFilter fileFilterIsFile(final String endswith)
	{
		return new java.io.FileFilter() {
			public boolean accept(File pathname) {
				return (pathname.isFile() && pathname.getName().endsWith(endswith));
			}
		};
	}
}

