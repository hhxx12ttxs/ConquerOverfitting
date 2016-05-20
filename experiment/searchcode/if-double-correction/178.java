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
 * (C) Copyright 2005, by EADS CRC
 * (C) Copyright 2007, by EADS France
 */

package org.jcae.viewer3d;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.j3d.*;
import javax.swing.JDialog;
import javax.swing.JTextPane;
import javax.vecmath.*;

import org.jcae.viewer3d.cad.ViewableCAD;
import org.jdesktop.j3d.utils.behaviors.vp.AxisBehavior;
import com.sun.j3d.utils.picking.PickResult;
import com.sun.j3d.utils.universe.PlatformGeometry;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.Viewer;
import com.sun.j3d.utils.universe.ViewingPlatform;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

/**
 * An AWT component wich display Viewable in a Java3D canvas.
 * This class is responsible for handling picking and refresh events.
 * The expected navigation behavior is the one of the OrbitBehavior of Java3D.
 * Multiple selection is available using ctrl+Left click.
 * @author Jerome Robert
 * @todo all methods must be implemented. public methods may be added.
 */
public class View extends Canvas3D implements PositionListener
{
	private static final float zFactorAbs=Float.parseFloat(System.getProperty("javax.media.j3d.zFactorAbs", "1.0f"));
	private static final float zFactorRel=Float.parseFloat(System.getProperty("javax.media.j3d.zFactorRel", "1.0f"));
	
	/** Cheat codes to change polygon offset on CAD */
	private class PAKeyListener extends KeyAdapter
	{
		float offset=zFactorAbs;
		float offsetRel=zFactorRel;
		@Override
		public void keyPressed(KeyEvent e)
		{
			boolean found=true;
			switch(e.getKeyChar())
			{
				case ']': offset = checkBounds(2.0f * offset, offset); break;
				case '[': offset = checkBounds(0.5f * offset, offset); break;
				case '}': offsetRel = checkBounds(2.0f * offsetRel, offsetRel); break;
				case '{': offsetRel = checkBounds(0.5f * offsetRel, offsetRel); break;
				case '@': offset=zFactorAbs; offsetRel=zFactorRel; break;
				default: found=false;
			}
			if(found)
			{
				ViewableCAD.polygonAttrFront.setPolygonOffset(offset);
				ViewableCAD.polygonAttrBack.setPolygonOffset(offset);
				ViewableCAD.polygonAttrNone.setPolygonOffset(offset);
				
				ViewableCAD.polygonAttrFront.setPolygonOffsetFactor(offsetRel);
				ViewableCAD.polygonAttrBack.setPolygonOffsetFactor(offsetRel);
				ViewableCAD.polygonAttrNone.setPolygonOffsetFactor(offsetRel);
				System.out.println("zFactorAbs: "+offset+" zFactorRel: "+offsetRel);
			}
		}
		public final float checkBounds(float x, float old)
		{
			if (x == 0.0f)
			{
				if (old > 0.0f)
					return Float.MIN_VALUE;
				else
					return - Float.MIN_VALUE;
			}
			else if (x == Float.POSITIVE_INFINITY)
				return Float.MAX_VALUE;
			else if (x == Float.NEGATIVE_INFINITY)
				return - Float.MAX_VALUE;
			return x;
		}
	}
	
	final public static float FrontClipDistanceFactor=0.005f;
	final public static float BackClipDistanceFactor=5f;
	
	static JTextPane textPane;
	private Switch originAxisSwitch=new Switch(Switch.CHILD_NONE);
	private Switch fixedAxisSwitch=new Switch(Switch.CHILD_NONE);
	private TransformGroup fixedAxisTransformGroup=new TransformGroup();
	private TransformGroup originAxisTransformGroup=new TransformGroup();
	private PickResult lastPickResult;
	
	private View navigationMaster;
	private List<PositionListener> positionListeners=Collections.synchronizedList(new ArrayList<PositionListener>());
	
	private transient BufferedImage snapshot;
	private transient Object snapshotLock=new Object();
	private transient boolean takeSnapshot;
	private transient ScreenshotListener screenshotListener;
	
	static private SimpleUniverse sharedUniverse;
	private SimpleUniverse universe;
	static private Map<Viewable, ViewSpecificGroup> viewableToViewSpecificGroup=Collections.synchronizedMap(new HashMap<Viewable, ViewSpecificGroup>());
	private ViewingPlatform viewingPlatform;
	private BranchGroup axisBranchGroup=new BranchGroup();
	private Viewable currentViewable;
	protected ViewBehavior orbit;
	private AxisBehavior axisBehavior;
	ModelClip modelClip;
	private boolean isModelClip=false;
	private BranchGroup widgetsBranchGroup;
	private BranchGroup unClipWidgetsBranchGroup;
	private ClipBox clipBox=null;
	private PrintWriter writer=null;
	private List<Runnable> postRenderers=new ArrayList<Runnable>();
	private boolean locked;
	private Cursor unlockedCursor;
	
	// This is to manage screen buffer capture
	private volatile Thread contextThread = null;
	
	// Color buffer
	private volatile ImageComponent2D imageComponent = null;
	private volatile Object waitScreenshot = new Object();
	private volatile boolean takeScreenshot = false;
	private volatile Point screenshotPosition = new Point();
	
	// Depth Buffer
	private volatile DepthComponentFloat depthComponent = null;
	private volatile Object waitDepthCapture = new Object();
	private volatile boolean takeDepthBuffer = false;
	private volatile Point depthCapturePosition = new Point();
    /**
     * From https://java3d.dev.java.net/issues/show_bug.cgi?id=89
     * Finds the preferred <code>GraphicsConfiguration</code> object
     * for the system.  This object can then be used to create the
     * Canvas3D objet for this system.
     * @param window the window in which the Canvas3D will reside 
     *
     * @return The best <code>GraphicsConfiguration</code> object for
     *  the system.
     */
    private static GraphicsConfiguration getPreferredConfiguration(Window window)
    {
    	if(window==null)
    		return SimpleUniverse.getPreferredConfiguration();
    	GraphicsDevice device = window.getGraphicsConfiguration().getDevice();
        GraphicsConfigTemplate3D template = new GraphicsConfigTemplate3D();
        String stereo;

        // Check if the user has set the Java 3D stereo option.
        // Getting the system properties causes appletviewer to fail with a
        //  security exception without a try/catch.

        stereo = (String) java.security.AccessController.doPrivileged(
           new java.security.PrivilegedAction() {
           public Object run() {
               return System.getProperty("j3d.stereo");
           }
        });

        // update template based on properties.
        if (stereo != null) {
            if (stereo.equals("REQUIRED"))
                template.setStereo(GraphicsConfigTemplate.REQUIRED);
            else if (stereo.equals("PREFERRED"))
                template.setStereo(GraphicsConfigTemplate.PREFERRED);
        }
        // Return the GraphicsConfiguration that best fits our needs.
        return device.getBestConfiguration(template);
    } 	
	
    /** 
     * See https://java3d.dev.java.net/issues/show_bug.cgi?id=89
     * @deprecated Will cause a "java.lang.IllegalArgumentException: adding a
     * container to a container on a different GraphicsDevice" in dual screen
     * mode.
     */
	@Deprecated
	public View()
	{
		this(null, false, true);
	}
	
    /** 
     * See https://java3d.dev.java.net/issues/show_bug.cgi?id=89
     * @deprecated Will cause a "java.lang.IllegalArgumentException: adding a
     * container to a container on a different GraphicsDevice" in dual screen
     * mode.
     */
	@Deprecated
	public View(boolean offscreen)
	{
		this(null, offscreen, true);
	}
		
    /** 
     * See https://java3d.dev.java.net/issues/show_bug.cgi?id=89
     * @deprecated Will cause a "java.lang.IllegalArgumentException: adding a
     * container to a container on a different GraphicsDevice" in dual screen
     * mode. 
     */
	@Deprecated
	public View(boolean offscreen, boolean isSharedUniverse)
	{
		this(null, offscreen, isSharedUniverse);
	}

    public View(Window window)
	{
		this(window, false, true);
	}

	public View(Window window, boolean offscreen)
	{
		this(window, offscreen, true);
	}
		
	public View(Window window, boolean offscreen, boolean isSharedUniverse)
	{		
		super(getPreferredConfiguration(window), offscreen);
		if(offscreen)
		{
			getScreen3D().setPhysicalScreenWidth(0.0254/90.0 * 1600);
			getScreen3D().setPhysicalScreenHeight(0.0254/90.0 * 1200); 
		}

		if(isSharedUniverse)
		{
			if(sharedUniverse==null)
			{
				sharedUniverse=new SimpleUniverse(this);
				universe=View.sharedUniverse;
				viewingPlatform=universe.getViewingPlatform();			
			}
			else
			{
				universe=View.sharedUniverse;
				viewingPlatform=createViewingPlatform();
				sharedUniverse.getLocale().addBranchGraph(viewingPlatform);
			}
			
		}
		else
		{
			universe=new SimpleUniverse(this);
			viewingPlatform=universe.getViewingPlatform();			
		}

				
		PlatformGeometry platformGeometry = new PlatformGeometry();
		platformGeometry.addChild(createLights());
		platformGeometry.setCapability(Group.ALLOW_CHILDREN_WRITE);
		platformGeometry.setCapability(Group.ALLOW_CHILDREN_EXTEND);	
		viewingPlatform.setPlatformGeometry(platformGeometry);		
		orbit = new ViewBehavior(this);
		orbit.setCapability(Node.ALLOW_BOUNDS_WRITE);
		viewingPlatform.setViewPlatformBehavior(orbit);		
		
		originAxisSwitch.setCapability(Switch.ALLOW_SWITCH_READ);
		originAxisSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
		fixedAxisSwitch.setCapability(Switch.ALLOW_SWITCH_READ);
		fixedAxisSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
		fixedAxisTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		
		// Create the origin axis
		createAxis(originAxisTransformGroup);
		originAxisSwitch.addChild(originAxisTransformGroup);			
		ViewSpecificGroup vsp=new ViewSpecificGroup();				
		vsp.addView(getView());		
		vsp.addChild(originAxisSwitch);
		axisBranchGroup.addChild(vsp);
		universe.addBranchGraph(axisBranchGroup);
				
		createClipBranchGroup();
		createWidgetsBranchGroup();
		createUnClipWidgetsBranchGroup();
				
		createFixedAxis();
		
		getView().setFieldOfView(Math.PI/12);
		getView().setFrontClipPolicy(javax.media.j3d.View.PHYSICAL_EYE);
		getView().setBackClipPolicy(javax.media.j3d.View.PHYSICAL_EYE);
		
		zoomTo(0,0,0,1.0f);	
		addKeyListener(new PAKeyListener());
		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }	
		
	private void createFixedAxis()
	{
		final TransformGroup tg=new TransformGroup();
		tg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		Transform3D t3d=new Transform3D();
		t3d.set(new Vector3d(-4, -4, -40));
		createAxis(fixedAxisTransformGroup);		
		tg.setTransform(t3d);
		tg.addChild(fixedAxisTransformGroup);
		fixedAxisSwitch.addChild(tg);

		ViewSpecificGroup vsp=new ViewSpecificGroup();
		vsp.addView(getView());
		BranchGroup bg = new BranchGroup();
		vsp.addChild(fixedAxisSwitch);
		bg.addChild(vsp);
		axisBehavior = new AxisBehavior( fixedAxisTransformGroup,
			viewingPlatform.getViewPlatformTransform());
		bg.addChild(axisBehavior);
		addComponentListener(new ComponentAdapter()
		{
			private Transform3D myT3d=new Transform3D();
			@Override
			public void componentResized(ComponentEvent e)
			{
				if(getWidth()!=0)
				{
					tg.getTransform(myT3d);
					myT3d.set(new Vector3d(-4, -4*((float)getHeight())/getWidth(), -40));
					tg.setTransform(myT3d);
				}
			}
		});
		viewingPlatform.getPlatformGeometry().addChild(bg);
	}
	
	private void createWidgetsBranchGroup(){
		widgetsBranchGroup=new BranchGroup();
		widgetsBranchGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);
		widgetsBranchGroup.setCapability(Node.ALLOW_BOUNDS_READ);
		widgetsBranchGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);
		widgetsBranchGroup.setCapability(Group.ALLOW_CHILDREN_READ);
		
		BranchGroup parent=new BranchGroup();
		parent.setCapability(Group.ALLOW_CHILDREN_EXTEND);
		parent.setCapability(BranchGroup.ALLOW_DETACH);
		parent.setCapability(Node.ALLOW_BOUNDS_READ);
		parent.setCapability(Group.ALLOW_CHILDREN_WRITE);			
		ViewSpecificGroup vsg=new ViewSpecificGroup();
		vsg.setCapability(ViewSpecificGroup.ALLOW_VIEW_WRITE);
		vsg.setCapability(ViewSpecificGroup.ALLOW_VIEW_READ);
		vsg.setCapability(Node.ALLOW_BOUNDS_READ);
		vsg.setUserData(parent);
		vsg.addChild(widgetsBranchGroup);
		parent.addChild(vsg);
		universe.getLocale().addBranchGraph(parent);
		vsg.addView(getView());
	}
	
	private void createUnClipWidgetsBranchGroup(){
		unClipWidgetsBranchGroup=new BranchGroup();
		unClipWidgetsBranchGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);
		unClipWidgetsBranchGroup.setCapability(Node.ALLOW_BOUNDS_READ);
		unClipWidgetsBranchGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);
		unClipWidgetsBranchGroup.setCapability(Group.ALLOW_CHILDREN_READ);
		
		BranchGroup parent=new BranchGroup();
		parent.setCapability(Group.ALLOW_CHILDREN_EXTEND);
		parent.setCapability(BranchGroup.ALLOW_DETACH);
		parent.setCapability(Node.ALLOW_BOUNDS_READ);
		parent.setCapability(Group.ALLOW_CHILDREN_WRITE);			
		ViewSpecificGroup vsg=new ViewSpecificGroup();
		vsg.setCapability(ViewSpecificGroup.ALLOW_VIEW_WRITE);
		vsg.setCapability(ViewSpecificGroup.ALLOW_VIEW_READ);
		vsg.setCapability(Node.ALLOW_BOUNDS_READ);
		vsg.setUserData(parent);
		vsg.addChild(unClipWidgetsBranchGroup);
		parent.addChild(vsg);
		universe.getLocale().addBranchGraph(parent);
		vsg.addView(getView());
	}
	
	private void createClipBranchGroup(){
		BranchGroup clipBranchGroup=new BranchGroup();
		clipBranchGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);
		clipBranchGroup.setCapability(Node.ALLOW_BOUNDS_READ);
		clipBranchGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);
		clipBranchGroup.setCapability(Group.ALLOW_CHILDREN_READ);
		
		modelClip=new ModelClip();
		modelClip.setEnables(new boolean[]{false, false, false, false, false, false});
		modelClip.setCapability(ModelClip.ALLOW_ENABLE_READ);
		modelClip.setCapability(ModelClip.ALLOW_ENABLE_WRITE);
		modelClip.setCapability(ModelClip.ALLOW_PLANE_READ);
		modelClip.setCapability(ModelClip.ALLOW_PLANE_WRITE);
		modelClip.setCapability(ModelClip.ALLOW_SCOPE_READ);
		modelClip.setCapability(ModelClip.ALLOW_SCOPE_WRITE);
		modelClip.setCapability(ModelClip.ALLOW_INFLUENCING_BOUNDS_WRITE);
		clipBranchGroup.addChild(modelClip);
		universe.getLocale().addBranchGraph(clipBranchGroup);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable
	{
		Viewable[] vs=getViewables();
		for(int i=0; i<vs.length; i++)
		{
			remove(vs[i]);
		}
		universe.getLocale().removeBranchGraph(axisBranchGroup);
		modelClip.removeAllScopes();
		universe.getLocale().removeBranchGraph((BranchGroup)modelClip.getParent());
		BranchGroup parent=(BranchGroup)widgetsBranchGroup.getParent().getParent();
		universe.getLocale().removeBranchGraph(parent);
		parent=(BranchGroup)unClipWidgetsBranchGroup.getParent().getParent();
		universe.getLocale().removeBranchGraph(parent);
	}
	
	/** set a PrintWriter for the viewer messages*/
	public void setPrintWriter(PrintWriter writer){
		this.writer=writer;
	}
	/** print a line in the PrintWriter*/
	public void println(String line){
		if(writer!=null)
		{
			writer.println(line);
			writer.flush();
		}
	}
	public TransformGroup getOriginAxisTransformGroup()
	{
		return originAxisTransformGroup;
	}
	/**
	 * @return
	 */
	private ViewingPlatform createViewingPlatform()
	{
		ViewingPlatform vp=new ViewingPlatform();
		vp.setUniverse(universe);
		Viewer viewer=new Viewer(this);
		viewer.setViewingPlatform(vp);
		return vp;		
	}

	/** return all the clipped groups in the view*/ 
	private Group[] getAllClipGroup(){
		int numGroup=0;
		Viewable[] viewable=getViewables();
		numGroup+=widgetsBranchGroup.numChildren();
		numGroup+=viewable.length;
		Group[] toReturn=new Group[numGroup];
		int ii=0;
		for(int i=0;i<widgetsBranchGroup.numChildren();i++){
			toReturn[ii++]=(Group) widgetsBranchGroup.getChild(i);
		}
		
		for(int i=0;i<viewable.length;i++){
			toReturn[ii++]=getBranchGroup(viewable[i]);
		}
		return toReturn;
	}
	
	/** Add a Viewable to the current view */
	public void add(Viewable viewable)
	{
		ViewSpecificGroup vsg=viewableToViewSpecificGroup.get(viewable);
		if(vsg==null)
		{
			Node node=viewable.getJ3DNode();
			BranchGroup parent=new BranchGroup();
			parent.setCapability(Group.ALLOW_CHILDREN_EXTEND);
			parent.setCapability(BranchGroup.ALLOW_DETACH);
			parent.setCapability(Node.ALLOW_BOUNDS_READ);
			parent.setCapability(Group.ALLOW_CHILDREN_WRITE);			
			vsg=new ViewSpecificGroup();
			vsg.setCapability(ViewSpecificGroup.ALLOW_VIEW_WRITE);
			vsg.setCapability(ViewSpecificGroup.ALLOW_VIEW_READ);
			vsg.setCapability(Node.ALLOW_BOUNDS_READ);
			vsg.setUserData(parent);
			viewableToViewSpecificGroup.put(viewable, vsg);
			vsg.addChild(node);
			parent.addChild(vsg);
			universe.getLocale().addBranchGraph(parent);
		}
		vsg.addView(getView());
		if(currentViewable==null)
			currentViewable=viewable;
		
		updateModelClipGroup();
	}

	/**
	 * Allow to had a custom branchgroup to the view
	 * @param branchGroup
	 */
	public void addBranchGroup(BranchGroup branchGroup)
	{
		branchGroup.setCapability(BranchGroup.ALLOW_DETACH);
		universe.getLocale().addBranchGraph(branchGroup);
	}

	/**
	 * add  a widget BranchGroup
	 * @param branchGroup
	 */
	public void addWidgetBranchGroup(BranchGroup branchGroup)
	{
		branchGroup.setCapability(BranchGroup.ALLOW_DETACH);
		widgetsBranchGroup.addChild(branchGroup);
		updateModelClipGroup();
	}
	
	
	/**
	 * remove the specified widget BranchGroup
	 * @param branchGroup
	 */
	public void removeWidgetBranchGroup(BranchGroup branchGroup){
		widgetsBranchGroup.removeChild(branchGroup);
		updateModelClipGroup();
	}
	
	/**
	 * add  a widget BranchGroup not clip by the clipModel
	 * @param branchGroup
	 */
	public void addUnClipWidgetBranchGroup(BranchGroup branchGroup)
	{
		branchGroup.setCapability(BranchGroup.ALLOW_DETACH);
		unClipWidgetsBranchGroup.addChild(branchGroup);
	}
	
	
	/**
	 * remove the specified widget BranchGroup not clip by the clipModel
	 * @param branchGroup
	 */
	public void removeUnClipWidgetBranchGroup(BranchGroup branchGroup){
		unClipWidgetsBranchGroup.removeChild(branchGroup);
	}
	
	
	private void setModelClip(ModelClip newModelClip){
		removeModelClip();
		updateModelClipGroup();
		for(int i=0;i<6;i++){
			if(newModelClip.getEnable(i)){
				Vector4d plane=new Vector4d();
				newModelClip.getPlane(i,plane);
				modelClip.setPlane(i,plane);
				modelClip.setEnable(i,true);
			}
		}
		modelClip.setInfluencingBounds(new BoundingSphere(new Point3d(),Double.MAX_VALUE));
		isModelClip=true;
	}
	
	/** update the group list of the modelclip*/
	private void updateModelClipGroup(){
		if(!isModelClip) return;
		modelClip.removeAllScopes();
		Group[] all=getAllClipGroup();
		for(int i=0;i<all.length;i++){
			modelClip.addScope(all[i]);
		}
	}
	
	private void initModelClipScope(ModelClip modelClip) {
		Viewable[] viewables=getViewables();
		for(int i=0;i<viewables.length;i++)
			modelClip.addScope(getBranchGroup(viewables[i]));
		
		modelClip.addScope(widgetsBranchGroup);
	}

	/**
	 * remove the ModelClip of the view
	 */
	public void removeModelClip(){
		modelClip.setEnables(new boolean[]{false, false, false, false, false, false});
		modelClip.removeAllScopes();
		isModelClip=false;
		
		if(clipBox!=null)
			removeUnClipWidgetBranchGroup(clipBox.getShape());
		clipBox=null;
	}
	
	/** create the modelclip with the specified planes and remove the previous modelclip
	 * Warning : all shared viewables will be clipped in other views !!
	 * */
	public void setClipPlanes(Vector4d[] planes){
		ModelClip mc=new ModelClip();
		mc.setEnables(new boolean[]{false, false, false, false, false, false});
		for(int ii=0;ii<planes.length;ii++){
			mc.setPlane(ii,planes[ii]);
			mc.setEnable(ii,true);
		}
	
		mc.setInfluencingBounds(new BoundingSphere(new Point3d(),Double.MAX_VALUE));
		setModelClip(mc);
	}
	
	/** create a clip Box and remove the previous modelclip
	 * Warning : all shared viewables will be clipped in other views !!
	 * */
	public void setClipBox(ClipBox box) {
		setClipPlanes(box.getClipBoxPlanes());
		addUnClipWidgetBranchGroup(box.getShape());
		clipBox=box;
	}
	/**returns true if the point is in the modelclip*/
	public boolean isInModelClip(Point3d pt){
		if(!isModelClip) return true;
		Vector4d plane=new Vector4d();
		double[] ptValues=new double[4];
		pt.get(ptValues);
		ptValues[3]=1;
		Vector4d p=new Vector4d(ptValues);

		for(int i=0;i<6;i++){
			if(!modelClip.getEnable(i)) continue;
			modelClip.getPlane(i,plane);
			if(plane.dot(p)>0) return false;
		}
		return true;
	}
	
	
	/**
	 * @param view
	 */
	public void addPositionListener(PositionListener listener)
	{
		positionListeners.add(listener);
	}
	
	private Node createAxis(TransformGroup transformGroup)
	{
		float[] f=new float[] {
			0, 0, 0,           1, 0, 0, // x line
			0.9f, 0.1f, 0,     1, 0, 0, // x arrow 1    
			0.9f, -0.1f, 0,    1, 0, 0, // x arrow 2    
			0.9f, 0, 0.1f,     1, 0, 0, // x arrow 3    
			0.9f, 0, -0.1f,    1, 0, 0, // x arrow 1    
			0, 0, 0,           0, 1, 0, // y line
			0.1f, 0.9f, 0,     0, 1, 0, // y arrow 1    
			-0.1f, 0.9f, 0,    0, 1, 0, // y arrow 2    
			0, 0.9f, 0.1f,     0, 1, 0, // y arrow 3    
			0, 0.9f, -0.1f,    0, 1, 0, // y arrow 4    
			0, 0, 0,           0, 0, 1, // z line
			0.1f, 0, 0.9f,     0, 0, 1, // z arrow 1    
			-0.1f, 0, 0.9f,    0, 0, 1, // z arrow 2    
			0, 0.1f, 0.9f,     0, 0, 1, // z arrow 3    
			0, -0.1f, 0.9f,    0, 0, 1 // z arrow 4    
		};
		LineArray la = new LineArray(f.length/3,
			GeometryArray.COORDINATES);
		la.setCoordinates(0, f);
		Appearance a = new Appearance();
		Color3f color=new Color3f(0.5f, 0.5f, 0.7f);
		ColoringAttributes ca = new ColoringAttributes(color,
			ColoringAttributes.FASTEST);
		a.setColoringAttributes(ca);
		Shape3D s3d = new Shape3D(la);
		s3d.setAppearance(a);
		transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		transformGroup.addChild(s3d);		
		transformGroup.addChild(new RasterTextLabel("x", Color.WHITE, 1.1f, 0, 0));
		transformGroup.addChild(new RasterTextLabel("y", Color.WHITE, 0, 1.1f, 0));
		transformGroup.addChild(new RasterTextLabel("z", Color.WHITE, 0, 0, 1.1f));
		transformGroup.setCapability(Node.ALLOW_BOUNDS_READ);
		return transformGroup;		
	}

	private Node createLights()
	{	
		Group toReturn = new Group();
		Color3f cl = new Color3f(1f, 1f, 1f);
		//Color3f cd = new Color3f(0.4f, 0.4f, 0.4f);
		BoundingSphere bs = new BoundingSphere(new Point3d(0,0,-10), Double.MAX_VALUE/2);
		DirectionalLight l1 = new DirectionalLight(cl, new Vector3f(0,0,1f));
		l1.setInfluencingBounds(bs);
		DirectionalLight l2 = new DirectionalLight(cl, new Vector3f(0,0,-1f));
		l2.setInfluencingBounds(bs);
		//Light l3 = new DirectionalLight(cd, new Vector3f(0,1f,0));
		l2.setInfluencingBounds(bs);
		toReturn.addChild(l1);
		toReturn.addChild(l2);
		//toReturn.addChild(l3);
		toReturn.addChild(new AmbientLight(new Color3f(0.7f, 0.7f, 0.7f)));
		return toReturn;
	}
	
	private void displayTransform3d(Transform3D transform)
	{
		if(textPane==null)
		{
			JDialog d=new JDialog();
			textPane=new JTextPane();
			d.setContentPane(textPane);
			d.setVisible(true);
		}	
		String cr=System.getProperty("line.separator");
		String s="scale="+transform.getScale()+cr;
		Matrix3f m=new Matrix3f();
		Vector3f v=new Vector3f();
		transform.get(m);
		transform.get(v);
		s+="rotation="+m+cr;
		s+="translation="+v+cr;
		textPane.setText(s);
	}

	protected void firePositionChanged()
	{
		Iterator<PositionListener> it=positionListeners.iterator();
		while(it.hasNext())
		{
			PositionListener p=it.next();
			p.positionChanged();
		}
	}
	
	/** Fit the view to show the specified viewable */
	public void fit(Viewable viewable)
	{
		BoundingSphere b= (BoundingSphere)getBranchGroup(viewable).getBounds();
		Point3d c=new Point3d();
		b.getCenter(c);
		zoomTo((float)c.x,(float)c.y,(float)c.z,(float)b.getRadius());
	}
	
	/** Fit the view to show all the Viewable */
	public void fitAll()
	{
		BoundingSphere bs=getBound();
		if(bs.getRadius()<=0)
			bs=new BoundingSphere();		
		Point3d c=new Point3d();
		bs.getCenter(c);
		zoomTo((float)c.x,(float)c.y,(float)c.z,(float)bs.getRadius());
	}
	
	/**
	 * This transforms from Normalized Eyes Coordinates (NEC) to Display Coordinates in AWT meanings (the z value is the same value of the zbuffer).
	 * You can go from virtual world coordinates to NEC with getVWorldProjection method and using left projection.
	 * @param point
	 * @param pointTransformed
	 */
	public void normalizedEyeCoordinateToViewportCoordinate(Tuple4d point, Tuple3d pointTransformed) {
		// Transform from [-1,1] to [0,1]
		
		if(point.y < -1. || point.y > 1.)
			throw new RuntimeException("PWET : " + point);
		
		pointTransformed.x = (point.x + 1.) * .5;
		pointTransformed.y = (point.y + 1.) * .5;
		pointTransformed.z = (-point.z + 1.) * .5;
		
		pointTransformed.x *= getWidth();
		pointTransformed.y *= getHeight();
		pointTransformed.y = getHeight() - pointTransformed.y;
	}
		
	/** restore the Front clip distance to see all the Viewable */
	public void restoreFrontClipDistance(){
		BoundingSphere bs=getBound();
		if(bs.getRadius()<=0)
			bs=new BoundingSphere();
		getView().setFrontClipDistance(FrontClipDistanceFactor*(float)bs.getRadius());
	}
	
	public void setFrontClipDistance(double d){
		getView().setFrontClipDistance(d);
	}
	
	public double getFrontClipDistance(){
		return getView().getFrontClipDistance();
	}
	
	public double getBackClipDistance(){
		return getView().getBackClipDistance();
	}
	
	public void setBackClipDistance(double d){
		getView().setBackClipDistance(d);
	}
	
	public BoundingSphere getBound()
	{
		Iterator<ViewSpecificGroup> it=viewableToViewSpecificGroup.values().iterator();
		ArrayList<Bounds> bounds=new ArrayList<Bounds>();		
		while(it.hasNext())
		{
			ViewSpecificGroup bg=it.next();
			if(bg.indexOfView(getView())!=-1)
			{
				Bounds b=bg.getBounds();
				bounds.add(b);
			}
		}
		
		if(isOriginAxisVisible())			
			bounds.add(originAxisTransformGroup.getBounds());
		
		BoundingSphere bs;
		if(!bounds.isEmpty())
		{
			bs=(BoundingSphere) bounds.get(0);
			bs.combine(bounds.toArray(new Bounds[bounds.size()]));
		}
		else
			bs=new BoundingSphere();
		return bs;
	}
	
	/**
	 * Get the cloned branchgroup of a viewable for this view.
	 * The viewables may used it to modify a branchgroup whithout rebuilding
	 * it entirely.
	 * @param view
	 * @return
	 * 
	 */
	protected BranchGroup getBranchGroup(Viewable viewable)
	{
		ViewSpecificGroup vsp=viewableToViewSpecificGroup.get(viewable);
		if(vsp==null) return null;
		return (BranchGroup)vsp.getUserData();
	}	
	
	/** Return viewables shown in this view */
	public Viewable[] getViewables()
	{
		ArrayList<Viewable> toReturn=new ArrayList<Viewable>();
		Iterator<Map.Entry<Viewable, ViewSpecificGroup>> it=viewableToViewSpecificGroup.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry<Viewable, ViewSpecificGroup> e=it.next();
			Viewable v=e.getKey();
			ViewSpecificGroup vsg=e.getValue();
			if(vsg.indexOfView(getView())!=-1)
			{
				toReturn.add(v);
			}
		}		
		return toReturn.toArray(new Viewable[toReturn.size()]);
	}
	
	public Viewable getCurrentViewable()
	{
		return currentViewable;
	}
	
	public ViewingPlatform getViewingPlatform()
	{
		return viewingPlatform;		
	}
	
	public void setCurrentViewable(Viewable v)
	{
		currentViewable=v;
	}
	
	/** Move the view to the specified position */
	public void move(Transform3D position)
	{
		viewingPlatform.getViewPlatformTransform().setTransform(position);
	}
	
	/**
	 * Implement PositionListener.
	 * This listener is fired when the navigation master move.
	 */
	public void positionChanged()
	{
		move(navigationMaster.where());
	}
    /**
     * Overloaded for to be able to take snapshots and draw overlays
     * (selection rectangle)
     * @see takeSnapshot 
     */
	@Override
	public void postSwap()
	{
		super.postSwap();
		
		contextThread = Thread.currentThread();
		
		try
		{
			if (takeSnapshot)
			{
				snapshot = getImage();			
				takeSnapshot = false;
				synchronized(snapshotLock)
				{
					snapshotLock.notifyAll();
				}
				
				if(screenshotListener!=null)
					screenshotListener.shot(snapshot);
			}
			
			if(takeScreenshot)
			{
				captureColorBuffer();
				takeScreenshot = false;
				synchronized(waitScreenshot)
				{
					waitScreenshot.notifyAll();
				}
			}
			
			if(takeDepthBuffer)
			{
				captureDepth();
				takeDepthBuffer = false;
				synchronized (waitDepthCapture)
				{
					waitDepthCapture.notifyAll();
				}				
				//System.out.println("ZBUFFER : " + Arrays.toString(depth));
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	/**
	 * Return an Image representing the current Canvas3D
	 * This method is not synchronized with the Java3D rendering.
	 * It used by takeSnapShopt method (which add synchronization),
	 * and for the rendering of the rectangle selection.
	 */
	protected BufferedImage getImage()
	{
		Dimension dim=getSize();
		GraphicsContext3D ctx = getGraphicsContext3D();
		Raster ras=new Raster();
		ras.setSize(dim);
		ras.setImage(new ImageComponent2D(ImageComponent.FORMAT_RGB,dim.width, dim.height));
		ctx.readRaster(ras);		
		// Now strip out the image info
		return ras.getImage().getImage();					
	}
	
	/**
	 * This is the new and simpliest method to take screenshot.
	 * Be careful : this method was not already tested.
	 * Warning : This methods works in all thread except in J3D behaviour threads.
	 * I don't know if it works in other J3D threads. I don't investigate much. Certainly
	 * a mutex that lock all the threads...
	 * @param x 
	 * @param y 
	 * @param width
	 * @param height
	 * @return
	 */
	public synchronized BufferedImage getScreenshot(int x, int y, int width, int height)
	{
		imageComponent = new ImageComponent2D(ImageComponent.FORMAT_RGB, width, height);
		screenshotPosition.x = x;
		screenshotPosition.y = y;
		
		if(Thread.currentThread().equals(contextThread))
		{
			captureColorBuffer();
		}
		else
		{
			takeScreenshot = true;
			
			// If we aren't in the swing thread go to it to send refresh event
			if(!SwingUtilities.isEventDispatchThread())
			{
				try
				{
					SwingUtilities.invokeAndWait(new Runnable()
					{

						public void run()
						{
							getView().repaint();
						}
					});
				} catch (InterruptedException ex)
				{
					Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
				} catch (InvocationTargetException ex)
				{
					Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
			else
				getView().repaint();
			
			try
			{
				// If the screenshot is not already done then wait
				if(takeScreenshot)
				{
					synchronized (waitScreenshot)
					{
						waitScreenshot.wait();
					}
				}
			} catch (InterruptedException ex)
			{
				Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		
		// Now strip out the image info
		return imageComponent.getImage();	
	
	}
	
	private void captureColorBuffer()
	{
		GraphicsContext3D ctx = getGraphicsContext3D();
		Raster ras=new Raster();
		
		ras.setSrcOffset(screenshotPosition);
		ras.setSize(new Dimension(imageComponent.getWidth(), imageComponent.getHeight()));
		ras.setImage(imageComponent);
		ctx.readRaster(ras);
		
		imageComponent = ras.getImage();
	}
	
	/**
	 * This method create a png file in the tempory directory containing the depth buffer.
	 * To see correctly the depth buffer a normalisation and scale color is maded.
	 * @param buffer
	 * @param width
	 * @param height
	 */
	private void debugDepthBuffer(float[] buffer, int width, int height)
	{
		System.out.println("width : " + width);
		System.out.println("height : " + height);
		System.out.println("buffer length : " + buffer.length);
		
		assert buffer.length == width * height : "buffer with not good length";
		//assert false;
		float zmin =Float.MAX_VALUE, zmax = Float.MIN_VALUE;
		
		for(float z : buffer)
		{
			zmin = Math.min(z, zmin);
			zmax = Math.max(z, zmax);
		}
		
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		for(int i = 0 ; i < depthComponent.getWidth() ; ++i)
		{
			for(int j = 0 ; j < depthComponent.getHeight() ; ++j)
			{//aZbuffer[(x - xMin)  + (xMax - xMin + 1) * (y - yMin)];
				float z = buffer[i + j * depthComponent.getWidth()];
				float zNormalized = (z-zmin) / (zmax - zmin);
				int c = (int)( zNormalized * 255.);
				assert c >=0;
				assert c < 256;
				int rgb = c << 8 | c << 16 | c;
				//System.out.println("C : " + c);
				//int rgb = c;
				image.setRGB(i, j, rgb);
			}
		}
		try
		{

			ImageIO.write(image, "PNG", File.createTempFile("pwet", "png"));
		} catch (IOException ex)
		{
			Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
		}		
	}
	/**
	 * This function works only with a patched java3d version.
	 * Contact a jCAE developper on the forum for more information.
	 * Warning : see warning of getScreenshot
	 * See RectangleSelection program for example of use.
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @return
	 */
	public synchronized float[] getDepthBuffer(int x, int y, int width, int height)
	{
		depthComponent = new DepthComponentFloat(width, height);
		depthCapturePosition.x = x;
		depthCapturePosition.y = y;
		
		if(Thread.currentThread().equals(contextThread))
		{
			captureDepth();
		}
		else
		{
			takeDepthBuffer = true;
			
			// If we aren't in the swing thread go to it to send refresh event
			if(!SwingUtilities.isEventDispatchThread())
			{
				try
				{
					SwingUtilities.invokeAndWait(new Runnable()
					{

						public void run()
						{
							getView().repaint();
						}
					});
				} catch (InterruptedException ex)
				{
					Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
				} catch (InvocationTargetException ex)
				{
					Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
			else
				getView().repaint();
			try
			{
				// If the screenshot is not already done then wait
				if(takeDepthBuffer)
				{
					synchronized (waitDepthCapture)
					{
						waitDepthCapture.wait();
					}
				}
			} catch (InterruptedException ex)
			{
				Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		
		float[] zbuffer = new float[depthComponent.getWidth() * depthComponent.getHeight()];
		depthComponent.getDepthData(zbuffer);
		
		// UNCOMMENT FOR DEBUGGING
		//debugDepthBuffer(zbuffer, depthComponent.getWidth() , depthComponent.getHeight());
		
		return zbuffer;
	}
	
	private void captureDepth()
	{
		GraphicsContext3D ctx = getGraphicsContext3D();
		depthComponent.setCapability(DepthComponent.ALLOW_DATA_READ);
		Raster ras=new Raster(new Point3f(0.0f, 0.0f, 0.0f),
        Raster.RASTER_DEPTH, depthCapturePosition.x, depthCapturePosition.y, depthComponent.getWidth(), depthComponent.getHeight(), null,
        depthComponent);
		ras.setCapability(Raster.ALLOW_DEPTH_COMPONENT_READ);
		ras.setCapability(Raster.ALLOW_IMAGE_READ);
		ras.setCapability(Raster.ALLOW_TYPE_READ);
		//ras.setImage(new ImageComponent2D(ImageComponent.FORMAT_RGB,dim.width, dim.height));
		
		ctx.readRaster(ras);
	}
	
	/** Remove a viewable from this view */
	public void remove(Viewable viewable)
	{	
		ViewSpecificGroup vsg=viewableToViewSpecificGroup.get(viewable);
		if(vsg!=null)
		{
			vsg.removeView(getView());			
			if(vsg.numViews()==0)
			{				
				universe.getLocale().removeBranchGraph(getBranchGroup(viewable));
				viewableToViewSpecificGroup.remove(viewable);
				vsg.removeAllChildren();
			}			
		}
		if(currentViewable==viewable)
		{
			Viewable[] vs=getViewables();
			if(vs.length>0)
				currentViewable=vs[0];
			else
				currentViewable=null;
		}
		
		updateModelClipGroup();
	}
	
	/** inform if the view contains the viewable*/
	public boolean contains(Viewable viewable){
		ViewSpecificGroup vsg=viewableToViewSpecificGroup.get(viewable);
		if(vsg==null) return false;
		
		Enumeration e=vsg.getAllViews();
		while(e.hasMoreElements())
			if(e.nextElement()==this.getView()) return true;
		
		return false;
	}
	
	/** Create a navigation link with the specified view.
	 * This will ensure that the current view show the same Position as the
	 * specified view.
	 * @param view the master view, null mean no master.
	 */
	public void setNavigationMaster(View view)
	{
		navigationMaster=view;
		view.addPositionListener(this);
	}	
	
	/**
	 * Set the list of Viewables to which picking events will be dispatched.
	 * The Viewable objects not in this list will not be pickable.
	 * @param viewables
	 */
	public void setPickableViewables(Collection viewables)
	{
		//TODO
	}
	
	/**
	 * Set the picking mode:
	 * <ul>
	 * <li>0, for single click picking</li>
	 * <li>1, for rectangular selection picking</li>
	 * <li>2, for polygonal selection picking</li>
	 * </ul>
	 * @param mode
	 */
	public void setPickingMode(short mode)
	{
		//TODO
	}

	/**
	 * Display 3D x-y-z cartesian, at the center of the view.
	 * These axis are visible whenever the 3D origin cannot be seen.
	 * Axis are identified with strings "x","y" and "z".
	 * The size of the axis do not depends on the zoom level.
	 * @param show
	 */
	public void setFixedAxisVisible(boolean show)
	{		
		if(show)
			fixedAxisSwitch.setWhichChild(0);
		else 
			fixedAxisSwitch.setWhichChild(Switch.CHILD_NONE);
	}
	/**
	 * Display 3D x-y-z cartesian at the 3D origin of the model.
	 * Axis are identified with strings "x","y" and "z".
	 * The size of the axis do not depends on the zoom level.
	 * @param show
	 */
	public void setOriginAxisVisible(boolean show)
	{
		if(show)
			originAxisSwitch.setWhichChild(0);
		else 
			originAxisSwitch.setWhichChild(Switch.CHILD_NONE);
	}
	
	public boolean isOriginAxisVisible()
	{
		return originAxisSwitch.getWhichChild()==0;		
	}
	
	/**
	 * Take a snapshot of the current view
	 * Do not use this for offscreen rendering. See "On-screen Rendering vs. Off-screen Rendering" in
	 * Canvas3D javadoc. This method should be wrapped in a SwingUtilities.invokeXXXX statements.
	 * @deprecated Use takeScreenshot Not thread safe. In some configuration a deadlock could
	 * occure.
	 */
	// snapshotLock.wait(); must not be run in an AWT thread because
	// it would block the AWT event thread. Then AWT would never notify the
	// J3D rendering thread and snapshotLock.wait() would never return
	
	@Deprecated
	public BufferedImage takeSnapshot()
	{
		takeSnapshot=true;
		synchronized(snapshotLock)
		{
			try
			{
				snapshotLock.wait();
			} catch(InterruptedException ex)
			{
				ex.printStackTrace();
				return new BufferedImage(0, 0, BufferedImage.TYPE_BYTE_INDEXED);
			}
		}
		return snapshot;
	}

	/**
	 * Take a snapshot of the current view
	 * Do not use this for offscreen rendering. See "On-screen Rendering vs. Off-screen Rendering" in
	 * Canvas3D javadoc. 
	 */	
	public void takeScreenshot(ScreenshotListener listener)
	{
		screenshotListener=listener;
		takeSnapshot=true;
		getView().repaint();		
	}
	
	/**
	 * Take a snapshot of the current view in Off-screen mode
	 * Do not use this for on-screen rendering. See "On-screen Rendering vs. Off-screen Rendering" in
	 * Canvas3D javadoc.	 
	 */
	public BufferedImage takeSnapshot(int w, int h)
	{		
		getScreen3D().setSize(w,h);
		BufferedImage image=new BufferedImage(w,h, ImageComponent.FORMAT_RGB);
		setOffScreenBuffer(new ImageComponent2D(ImageComponent.FORMAT_RGB, image));	
		renderOffScreenBuffer();
		waitForOffScreenRendering();
		return getOffScreenBuffer().getImage();
	}
	
	/** Return the current position of the view */
	public Transform3D where()
	{
		Transform3D t3d=new Transform3D();
		viewingPlatform.getViewPlatformTransform().getTransform(t3d);
		return t3d;
	}
		
	/** Modify the view to best see what is include a given sphere
	 * @param x x coordinate of the center of the sphere
	 * @param y y coordinate of the center of the sphere
	 * @param z z coordinate of the center of the sphere
	 * @param radius radius of the sphere
	 */
	public void zoomTo(float x, float y, float z, float radius)
	{	
		Point3d c=new Point3d(x,y,z);
		BoundingSphere b=new BoundingSphere(c,radius);
		orbit.setBounds(b);
		orbit.setRotationCenter(c);
		orbit.setZoomFactor(b.getRadius());
		orbit.setTransFactors(b.getRadius()/10,b.getRadius()/10);
		orbit.setRotFactors(0.5, 0.5);
		orbit.setSchedulingBounds(new BoundingSphere(c,b.getRadius()*100));
		axisBehavior.setSchedulingBounds(orbit.getSchedulingBounds());
		
		//getView().setWindowResizePolicy(javax.media.j3d.View.VIRTUAL_WORLD);				
		Transform3D t3d = new Transform3D();
		viewingPlatform.getViewPlatformTransform().getTransform(t3d);
		//calculate the translation vector for a identity rotation matrix
		float focal=(float) Math.tan(getView().getFieldOfView()/2);
		Vector3f correction=new Vector3f(0f, 0f, radius/focal);
		t3d.setTranslation(new Vector3f());	
		//rotate the translation vector
		t3d.transform(correction);
		correction.add(new Vector3f(x,y,z));
		t3d.setTranslation(correction);		
		viewingPlatform.getViewPlatformTransform().setTransform(t3d);
		
		double backClipDistance = radius*(1/focal+1);
		getView().setFrontClipDistance(backClipDistance/2000);
		getView().setBackClipDistance(backClipDistance);	
	}
	
	public double[] getRotationCenter()
	{
		Point3d p3d=new Point3d();
		orbit.getRotationCenter(p3d);
		double[] toReturn=new double[3];
		p3d.get(toReturn);
		return toReturn;
	}
	
	
	public void setRotationCenter(double x, double y, double z)
	{
		orbit.setRotationCenter(new Point3d(x, y, z));
	}
	
	/**
	 * Allow the user to specify the rotation center.
	 * The next click will be concidered as a rotation center
	 * redefinition.
	 */
	public void setChangeRotationCenter(boolean status)
	{
		this.requestFocus();
		((ViewBehavior)orbit).setChangeRotationCenter(true);
	}
	
	/** set the current mouse mode : see ViewBehavior*/
	public void setMouseMode(int mode){
		this.requestFocus();
		((ViewBehavior)orbit).setMouseMode(mode);
	}
	
	public int getMouseMode(){
		return ((ViewBehavior)orbit).getMouseMode();
	}
	
	public final static byte TOP	=0;
	public final static byte BOTTOM	=1;
	public final static byte LEFT	=2;
	public final static byte RIGHT	=3;
	public final static byte FRONT	=4;
	public final static byte BACK	=5;
	
	/**
	 * Change the view angle to a predefined one. 
	 * It could be TOP, BOTTOM, LEFT, RIGHT, FRONT, BACK.
	 */
	public void setOrientation(byte orientation)
	{
		Point3d eye=null;
		Vector3d up=new Vector3d(0,1,0);
		switch(orientation)
		{
		case TOP:
			eye=new Point3d(0, 1, 0);
			up=new Vector3d(0,0,-1);
			break;
		case BOTTOM:
			eye=new Point3d(0, -1, 0);
			up=new Vector3d(0,0,1);
			break;
		case LEFT:
			eye=new Point3d(-1, 0, 0);			
			break;
		case RIGHT:
			eye=new Point3d(1, 0, 0);
			break;
		case FRONT:
			eye=new Point3d(0, 0, 1);
			break;
		case BACK:
			eye=new Point3d(0, 0, -1);
			break;
		default:
			throw new IllegalArgumentException();
		}
		
		BoundingSphere bs=getBound();
		Transform3D t3d=new Transform3D();		
		Point3d center=new Point3d();
		bs.getCenter(center);
		float focal=(float) Math.tan(getView().getFieldOfView()/2);
		eye.scale(bs.getRadius()/focal);
		t3d.lookAt(eye, center, up);
		t3d.invert();
		move(t3d);
	}
	
	/** refresh the clip planes value*/
	protected void fireViewableChanged(Viewable viewable){
		if(!isModelClip) return;
		int planeCount=0;
		for(int i=0;i<6;i++){
			if(modelClip.getEnable(i))
				planeCount++;
		}
		
		Vector4d[] planes=new Vector4d[planeCount];
		int ii=0;
		for(int i=0;i<6;i++){
			if(modelClip.getEnable(i))
			{
				planes[ii]=new Vector4d();
				modelClip.getPlane(i,planes[ii]);
				ii++;
			}	
		}
		setClipPlanes(planes);
	}
	
	public static void viewableChanged(Viewable viewable){
		ViewSpecificGroup vsg=viewableToViewSpecificGroup.get(viewable);
		if(vsg!=null){
			Enumeration e=vsg.getAllViews();
			while(e.hasMoreElements()){
				Object o=e.nextElement();
				if( o instanceof javax.media.j3d.View){
					Enumeration ee=((javax.media.j3d.View)o).getAllCanvas3Ds();
					while(ee.hasMoreElements()){
						Object oo=ee.nextElement();
						if( oo instanceof View){
								((View)oo).fireViewableChanged(viewable);						
						}
					}
				}
			}
		}
	}
	
	public static void stopRenderer(Viewable viewable){
		ViewSpecificGroup vsg=viewableToViewSpecificGroup.get(viewable);
		if(vsg!=null){
			Enumeration e=vsg.getAllViews();
			while(e.hasMoreElements()){
				Object o=e.nextElement();
				if( o instanceof javax.media.j3d.View){
					Enumeration ee=((javax.media.j3d.View)o).getAllCanvas3Ds();
					while(ee.hasMoreElements()){
						Object oo=ee.nextElement();
						if( oo instanceof View){
								((View)oo).stopRenderer();						
						}
					}
				}
			}
		}
	}
	
	public static void startRenderer(Viewable viewable){
		ViewSpecificGroup vsg=viewableToViewSpecificGroup.get(viewable);
		if(vsg!=null){
			Enumeration e=vsg.getAllViews();
			while(e.hasMoreElements()){
				Object o=e.nextElement();
				if( o instanceof javax.media.j3d.View){
					Enumeration ee=((javax.media.j3d.View)o).getAllCanvas3Ds();
					while(ee.hasMoreElements()){
						Object oo=ee.nextElement();
						if( oo instanceof View){
								((View)oo).startRenderer();						
						}
					}
				}
			}
		}
	}
		
	/**
	 * Runnable to be call in the postRender method 
	 * @param runnable
	 * @todo think if it's good to set this "protected
	 * @todo think about synchronization (set this method synchronized
	 * or not as postRender is called by Java3D thread)
	 */
	protected void addPostRenderer(Runnable runnable)
	{
		postRenderers.add(runnable);
	}
	
	protected void removePostRenderer(Runnable runnable)
	{
		postRenderers.remove(runnable);
	}
	
	@Override
	final public void postRender()
	{
		for(int i=0; i<postRenderers.size(); i++)
		{
			Runnable r = postRenderers.get(i);
			r.run();
		}
	}
	
	/** Let this view ignore all input (mouse and keyboard) events */
	public void lock()
	{
		if(!locked)
		{
			locked = true;
			orbit.lock();
			unlockedCursor = getCursor();
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}
	}
	
	/** @see lock */
	public void unlock()
	{
		if(locked)
		{
			locked = false;
			orbit.unlock();
			setCursor(unlockedCursor);
		}
	}

	@Override
	protected void processKeyEvent(KeyEvent e)
	{
		if(!locked)
			super.processKeyEvent(e);
	}

	@Override
	protected void processMouseEvent(MouseEvent e)
	{
		if(!locked)
			super.processMouseEvent(e);
	}
}

