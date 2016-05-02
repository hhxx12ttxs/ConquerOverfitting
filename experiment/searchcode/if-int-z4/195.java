/*---

    iGeo - http://igeo.jp

    Copyright (c) 2002-2012 Satoru Sugihara

    This file is part of iGeo.

    iGeo is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as
    published by the Free Software Foundation, version 3.

    iGeo is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with iGeo.  If not, see <http://www.gnu.org/licenses/>.

---*/

package igeo;

import java.awt.*;
import java.util.*;
import java.io.*;

import java.lang.reflect.Array;

import igeo.gui.*;
import igeo.io.*;

/**
   A main interface to background process of iGeo.
   A single IG instance can be accessed through static methods although
   multiple IG instance also can be contained in the static variable iglist in case
   multiple object servers are needed or simultaneous execution of multiple applets. 
   One IG instance contains one IServer as object database and one IPanel as
   display window. The member variable of IPanel can be null when no display
   window is needed. 
   
   @see IServer
   @see IPanel
   
   @author Satoru Sugihara
   @version 0.7.6.2
*/
public class IG implements IServerI{
    
    public static int majorVersion(){ return 0; }
    public static int minorVersion(){ return 7; }
    public static int buildVersion(){ return 6; }
    public static int revisionVersion(){ return 2; }
    public static Calendar versionDate(){ return new GregorianCalendar(2012, 02, 25); }
    public static String version(){
	return String.valueOf(majorVersion())+"."+String.valueOf(minorVersion())+"."+
	    String.valueOf(buildVersion())+"."+String.valueOf(revisionVersion());
    }
    
    /************************************
     * static system variables
     ************************************/
    public static final Object lock = new Object();
    
    /** Processing Graphics using OpenGL to be put in size() method in Processing */
    public static final String GL = "igeo.p.PIGraphicsGL"; 
    
    /** Processing Graphics using P3D to be put in size() method in Processing; under development. do not use yet. */
    public static final String P3D = "igeo.p.PIGraphics3D";
    
    /** Processing Graphics using JAVA to be put in size() method in Processing; to be implemented */
    //public static final String JAVA = "igeo.p.PIGraphicsJava";
    
    /** multiple IG instances are stored in iglist and switched by IG static methods
	in case of applet execution or other occasion but handling of multiple IG
	instances and switching are not really tested. */
    protected static ArrayList<IG> iglist=null;
    protected static int currentId = -1;
    
    
    /************************************
     * static geometry variables
     ************************************/
    /** x-axis vector. do not modify the content. */
    public static final IVec xaxis = IVec.xaxis;
    /** y-axis vector. do not modify the content. */
    public static final IVec yaxis = IVec.yaxis;
    /** z-axis vector. do not modify the content. */
    public static final IVec zaxis = IVec.zaxis;
    /** origin vector. do not modify the content. */
    public static final IVec origin = IVec.origin;
    /** alias of x-axis vector */
    public static final IVec x = IVec.xaxis;
    /** alias of y-axis vector */
    public static final IVec y = IVec.yaxis;
    /** alias of z-axis vector */
    public static final IVec z = IVec.zaxis;
    /** alias of origin vector */
    public static final IVec o = IVec.origin;
    
    
    /************************************
     * object variables
     ************************************/
    /*protected*/ public IServer server;
    /*protected*/ public IPanel panel = null;
    
    /*protected*/ public String inputFile;
    /*protected*/ public String outputFile;
    
    /** base file path for file I/O */
    public String basePath = null; //".";

    /** wrapping inputs in different environment. replacing basePath. */
    public IInputWrapper inputWrapper=null;
    
    /* *
       initialize whole IG system with IServer and graphical components
       instance of IG should be held by IGPane
       
       @param
       owner: if owner contains IGPane, the instance of IG is kept by it.
              if not, IGPane is instantiated and the instance of IG is kept by it.
	      if the ownwer is null, IGPane and all other graphical components are not instantiated.
       
    */
    /*
    public static IG init(Container owner){
	if(iglist==null) iglist = new ArrayList<IG>();
	
	IG ig = null;
	if(owner != null){
	    IGPane p = findIGPane(owner);
	    if(p==null) p = new IGPane(owner);
	    ig = new IG(p);
	}
	else ig = new IG();
	
	iglist.add(ig);
	currentId = iglist.size()-1;
	
	return ig;
    }
    
    
    public static IGPane findIGPane(Container container){
	final int defaultSearchDepth = 0;
	return findIGPane(container, defaultSearchDepth);
    }
    
    public static IGPane findIGPane(Container container, int searchDepth){
	Component[] components = container.getComponents();
	for(Component c : components){
	    if(c instanceof IGPane) return (IGPane)c;
	    else if(searchDepth>0 && c instanceof Container){
		IGPane p = findIGPane((Container)c, searchDepth-1);
		if(p!=null) return p;
	    }
	}
	return null;
    }
    */
    
    
    /***********************************************************************
     * static methods
     ***********************************************************************/
    
    /**
       Initialize whole IG system in non-graphic mode.
    */
    public static IG init(){
	if(iglist==null) iglist = new ArrayList<IG>();
	IG ig = new IG();
	iglist.add(ig);
	currentId = iglist.size()-1;
	return ig;
    }
    
    /** Initialize whole IG system in graphic mode.
	Please instantiate IPanel beforehand.
    */
    public static IG init(IPanel owner){
	if(iglist==null) iglist = new ArrayList<IG>();
	IG ig = new IG(owner);
	iglist.add(ig);
	currentId = iglist.size()-1;
	return ig;
    }
    
    
    /** alias of cur() */
    public static IG current(){ return cur(); }
    
    /** Find the IG instance which is likely to be the current. */
    public static IG cur(){
	if(iglist==null || currentId<0 || currentId>=iglist.size()) return null;
	return iglist.get(currentId);
    }

    /** object to be used to lock in "synchronized" statement */
    public static IG defaultThread(){ return cur(); }
    
    /** object to be used to lock in "synchronized" statement */
    public static IDynamicServer dynamicThread(){
	IG ig = cur(); if(ig==null) return null; return ig.dynamicServer();
    }
    /** alias of dynamicThread() */
    public static IDynamicServer updateThread(){ return dynamicThread(); }
    
    
    public static void setCurrent(IG ig){
	int idx = iglist.indexOf(ig);
	if(idx>=0 && idx<iglist.size()) currentId = idx;
	else{ // not in the list
	    // add? really?
	    iglist.add(ig);
	    currentId = iglist.size()-1;
	}
	// default server for geometry creator
	ICurveCreator.server(ig);
	ISurfaceCreator.server(ig);
	IMeshCreator.server(ig);
    }
    
    public static void setCurrent(IPanel owner){
	for(int i=0; i<iglist.size(); i++){
	    if(iglist.get(i).panel == owner){
		currentId = i;

		// default server for geometry creator
		ICurveCreator.server(iglist.get(i));
		ISurfaceCreator.server(iglist.get(i));
		
		return;
	    }
	}
	IOut.err("no IG instance found for "+owner);
    }
    
    
    /** Find IG instance linked with the specified IPanel instance. */
    public static IG getIG(IPanel owner){
	for(IG ig : iglist) if(ig.panel == owner) return ig;
	return null;
    }
    
    public static boolean open(String file){
	IG ig = cur();
	if(ig==null) return false;
	return ig.openFile(file);
    }
    
    public static boolean save(String file){
	IG ig = cur();
	if(ig==null) return false;
	return ig.saveFile(file);
    }
    
    
    // dynamics methods
    /** set duration of dynamics update */
    public static void duration(int dur){ IG ig=cur(); if(ig!=null) ig.setDuration(dur); }
    /** get duration of dynamics update */
    public static int duration(){ IG ig=cur(); return ig==null?0:ig.getDuration(); }
    
    /** set current time count of dynamics update. recommeded not to chage time. */
    public static void time(int tm){ IG ig=cur(); if(ig!=null) ig.setTime(tm); }
    /** get current time count of dynamics update */
    public static int time(){ IG ig=cur(); return ig==null?-1:ig.getTime(); }
    
    /** pause dynamics update. */
    public static void pause(){ IG ig=cur(); if(ig!=null) ig.pauseDynamics(); }
    /** resume dynamics update. */
    public static void resume(){ IG ig=cur(); if(ig!=null) ig.resumeDynamics(); }
    
    /** start dynamics update. if IConfig.autoStart is true, this should not be used. */
    public static void start(){ IG ig=cur(); if(ig!=null) ig.startDynamics(); }
    /** stop dynamics update. recommended not to use this because stopping should be done by setting duration. */
    public static void stop(){ IG ig=cur(); if(ig!=null) ig.stopDynamics(); }
    
    
    /** setting update rate time interval in second */
    public static void updateRate(double second){ IConfig.updateRate=second; }
    /** alias of updateRate() */
    public static void updateSpeed(double second){ updateRate(second); }
    /** alias of updateRate() */
    public static void rate(double second){ updateRate(second); }
    /** alias of updateRate() */
    public static void speed(double second){ updateRate(second); }
    
    /** getting update rate time interval in second */
    public static double updateRate(){ return IConfig.updateRate; }
    /** alias of updateRate() */
    public static double updateSpeed(){ return updateRate(); }
    /** alias of updateRate() */
    public static double rate(){ return updateRate(); }
    /** alias of updateRate() */
    public static double speed(){ return updateRate(); }
    
    
    /** getting unit of current IG server */
    public static IUnit unit(){
	IG ig=cur(); if(ig!=null) return ig.server().unit(); 
	return null;
    }
    /** setting unit of current IG server */
    public static void unit(IUnit u){
	IG ig=cur(); if(ig!=null) ig.server().unit(u); 
    }
    /** setting unit of current IG server */
    public static void unit(String unitName){
	IG ig=cur(); if(ig!=null) ig.server().unit(unitName); 
    }
    
    
    /** to set the name first and save later (likely by key event) */
    public static void outputFile(String filename){
	IG ig = cur();
	if(ig!=null) ig.setOutputFile(filename);
    }
    public static String outputFile(){
	IG ig = cur();
	if(ig==null) return null;
	return ig.getOutputFile();
    }
    public static void inputFile(String filename){
	IG ig = cur();
	if(ig!=null) ig.setInputFile(filename);
    }
    public static String inputFile(){
	IG ig = cur();
	if(ig==null) return null;
	return ig.getInputFile();
    }


    /** get all points in the current server */
    public static IPoint[] points(){
	IG ig = cur(); return ig==null?null:ig.getPoints();
    }
    /** get all points in the current server; alias */
    public static IPoint[] pts(){ return points(); }
    
    /** get all curves in the current server */
    public static ICurve[] curves(){
	IG ig = cur(); return ig==null?null:ig.getCurves();
    }
    /** get all curves in the current server; alias */
    public static ICurve[] crvs(){ return curves(); }
    
    /** get all surfaces in the current server */
    public static ISurface[] surfaces(){
	IG ig = cur(); return ig==null?null:ig.getSurfaces();
    }
    /** get all surfaces in the current server; alias */
    public static ISurface[] srfs(){ return surfaces(); }
    
    /** get all meshes in the current server */
    public static IMesh[] meshes(){
	IG ig = cur(); return ig==null?null:ig.getMeshes();
    }
    /** get all breps in the current server */
    public static IBrep[] breps(){
	IG ig = cur(); return ig==null?null:ig.getBreps();
    }
    /** get all breps in the current server */
    public static IGeometry[] geometries(){
	IG ig = cur(); return ig==null?null:ig.getGeometries();
    }
    /** get all breps in the current server */
    public static IGeometry[] geos(){ return geometries(); }
    
    /** get all objects of the specified class in the current server */
    public static IObject[] objects(Class cls){
	IG ig = cur(); return ig==null?null:ig.getObjects(cls);
    }
    /** get all objects of the specified class in the current server; alias */
    public static IObject[] objs(Class cls){ return objects(); }
    
    /** get all objects in the current server */
    public static IObject[] objects(){
	IG ig = cur(); return ig==null?null:ig.getObjects();
    }
    /** get all objects in the current server; alias */
    public static IObject[] objs(){ return objects(); }
    
    /** get a point in the current server */
    public static IPoint point(int i){
	IG ig = cur(); return ig==null?null:ig.getPoint(i);
    }
    /** get a point in the current server; alias */
    public static IPoint pt(int i){ return point(i); }
    
    /** get a curve in the current server */
    public static ICurve curve(int i){
	IG ig = cur(); return ig==null?null:ig.getCurve(i);
    }
    /** get a curve in the current server; alias */
    public static ICurve crv(int i){ return curve(i); }
    
    /** get a surface in the current server */
    public static ISurface surface(int i){
	IG ig = cur(); return ig==null?null:ig.getSurface(i);
    }
    /** get a surface in the current server; alias */
    public static ISurface srf(int i){ return surface(i); }
    
    /** get a mesh in the current server */
    public static IMesh mesh(int i){
	IG ig = cur(); return ig==null?null:ig.getMesh(i);
    }
    
    /** get a brep in the current server */
    public static IBrep brep(int i){
	IG ig = cur(); return ig==null?null:ig.getBrep(i);
    }

    /** get a geometry in the current server */
    public static IGeometry geometry(int i){
	IG ig = cur(); return ig==null?null:ig.getGeometry(i);
    }
    /** get a geometry in the current server */
    public static IGeometry geo(int i){ return geometry(i); }
    
    /** get a object of the specified class in the current server */
    public static IObject object(Class cls, int i){
	IG ig = cur(); return ig==null?null:ig.getObject(cls,i);
    }
    /** get a object of the specified class in the current server; alias */
    public static IObject obj(Class cls, int i){ return object(cls,i); }
    
    /** get a object in the current server */
    public static IObject object(int i){
	IG ig = cur(); return ig==null?null:ig.getObject(i);
    }
    /** get a object in the current server; alias */
    public static IObject obj(int i){ return object(i); }
    
    /** number of points in the current server */
    public static int pointNum(){
	IG ig = cur(); return ig==null?0:ig.getPointNum();
    }
    /** number of points in the current server; alias */
    public static int ptNum(){ return pointNum(); }
    
    /** number of curves in the current server */
    public static int curveNum(){
	IG ig = cur(); return ig==null?0:ig.getCurveNum();
    }
    /** number of curves in the current server; alias */
    public static int crvNum(){ return curveNum(); }
    
    /** number of surfaces in the current server */
    public static int surfaceNum(){
	IG ig = cur(); return ig==null?0:ig.getSurfaceNum();
    }
    /** number of surfaces in the current server; alias */
    public static int srfNum(){ return surfaceNum(); }
    
    /** number of meshes in the current server */
    public static int meshNum(){
	IG ig = cur(); return ig==null?0:ig.getMeshNum();
    }
    /** number of breps in the current server */
    public static int brepNum(){
	IG ig = cur(); return ig==null?0:ig.getBrepNum();
    }
    /** number of geometries in the cubrrent server */
    public static int geometryNum(){
	IG ig = cur(); return ig==null?0:ig.getGeometryNum();
    }
    /** alias of geometryNum() */
    public static int geoNum(){ return geometryNum(); }
    
    /** number of objects of the specified class in the current server */
    public static int objectNum(Class cls){
	IG ig = cur(); return ig==null?0:ig.getObjectNum(cls);
    }
    /** number of objects of the specified class in the current server; alias */
    public static int objNum(Class cls){ return objectNum(cls); }
    
    /** number of objects in the current server */
    public static int objectNum(){
	IG ig = cur(); return ig==null?0:ig.getObjectNum();
    }
    /** number of objects in the current server; alias */
    public static int objNum(){ return objectNum(); }
    
    
    
    public static ILayer layer(String layerName){
	IG ig = cur();
	if(ig==null) return null;
	return ig.getLayer(layerName);
    }
    public static ILayer layer(int i){
	IG ig = cur();
	if(ig==null) return null;
	return ig.getLayer(i);
    }
    public static ILayer[] layers(){
	IG ig = cur();
	if(ig==null) return null;
	return ig.getAllLayers();
    }
    public static void delLayer(String layerName){
	IG ig = cur();
	if(ig==null) return;
	ig.deleteLayer(layerName);
    }
    public static int layerNum(){
	IG ig = cur();
	if(ig==null) return 0;
	return ig.getLayerNum();
    }
    
    public static void focus(){
	IG ig = cur();
	if(ig==null) return;
	ig.focusView();
    }
    
    
    public static boolean isGL(){
	IG ig = cur();
	if(ig==null){
	    IOut.err("no IG found");
	    return true; // GL is default
	}
	if(ig.server().graphicServer()==null){
	    IOut.err("no graphic server found");
	    return true; // GL is default
	}
	return ig.server().graphicServer().isGL();
    }
    public static void graphicMode(IGraphicMode mode){
	IG ig = cur(); if(ig==null) return;
	ig.server().setGraphicMode(mode);
    }
    
    /** set wireframe graphic mode */
    public static void wireframe(){
	IGraphicMode.GraphicType gtype = IGraphicMode.GraphicType.J2D;
	if(isGL()) gtype = IGraphicMode.GraphicType.GL;
	graphicMode(new IGraphicMode(gtype,false,true,false));
    }
    /** alias of wireframe() */
    public static void wire(){ wireframe(); }
    
    /** set fill graphic mode */
    public static void fill(){
	IGraphicMode.GraphicType gtype = IGraphicMode.GraphicType.J2D;
	if(isGL()) gtype = IGraphicMode.GraphicType.GL;
	graphicMode(new IGraphicMode(gtype,true,false,false));
    }
    
    /** set fill+wireframe graphic mode */
    public static void fillWithWireframe(){ wireframeFill(); }
    /** set fill+wireframe graphic mode */
    public static void fillWireframe(){ wireframeFill(); }
    /** set fill+wireframe graphic mode */
    public static void fillWire(){ wireframeFill(); }
    /** set fill+wireframe graphic mode */
    public static void wireframeFill(){
	IGraphicMode.GraphicType gtype = IGraphicMode.GraphicType.J2D;
	if(isGL()) gtype = IGraphicMode.GraphicType.GL;
	graphicMode(new IGraphicMode(gtype,true,true,false));
    }
    /** set fill+wireframe graphic mode */
    public static void wireFill(){ wireframeFill(); }
    
    /** set transparent fill graphic mode */
    public static void transparentFill(){ transparent(); }
    /** set transparent fill graphic mode */
    public static void transFill(){ transparent(); }
    /** set transparent fill graphic mode */
    public static void transparent(){
	IGraphicMode.GraphicType gtype = IGraphicMode.GraphicType.J2D;
	if(isGL()) gtype = IGraphicMode.GraphicType.GL;
	graphicMode(new IGraphicMode(gtype,true,false,true));
    }
    /** alias of transparent() */
    public static void trans(){ transparent(); }
    
    /** set transparent fill+wireframe graphic mode */
    public static void transparentFillWithWireframe(){ wireframeTransparent(); }
    /** set transparent fill+wireframe graphic mode */
    public static void transparentWireframe(){ wireframeTransparent(); }
    /** set transparent fill+wireframe graphic mode */
    public static void wireframeTransparent(){
	IGraphicMode.GraphicType gtype = IGraphicMode.GraphicType.J2D;
	if(isGL()) gtype = IGraphicMode.GraphicType.GL;
	graphicMode(new IGraphicMode(gtype,true,true,true));
    }
    /** alias of wireframeTransparent() */
    public static void wireTrans(){ wireframeTransparent(); }
    /** alias of wireframeTransparent() */
    public static void transWire(){ wireframeTransparent(); }
    
    
    public static void noGraphic(){
	IGraphicMode.GraphicType gtype = IGraphicMode.GraphicType.J2D;
	if(isGL()) gtype = IGraphicMode.GraphicType.GL;
	graphicMode(new IGraphicMode(gtype,false,false,false));
    }
    
    
    
    public static IView view(int paneIndex){
	IG ig = cur(); if(ig==null) return null;
	if(ig==null || ig.panel==null || ig.panel.panes==null ||
	   ig.panel.panes.size() <= paneIndex || paneIndex<0 ){ return null; }
	if(ig.panel instanceof IScreenTogglePanel){
	    ((IScreenTogglePanel)(ig.panel)).enableFullScreen(ig.panel.panes.get(paneIndex));
	}
	return ig.panel.panes.get(paneIndex).getView();
    }
    
    
    /** put the specified pane on the full screen inside the window if the panel is IGridPanel with 2x2 grid */
    public static IPane gridPane(int xindex, int yindex){
	IG ig = cur(); if(ig==null) return null;
	if(ig.panel!=null && ig.panel instanceof IGridPanel){
	    IGridPanel gpanel = (IGridPanel)ig.panel;
	    if(xindex>=0 && xindex < gpanel.gridPanes.length &&
	       yindex>=0 && yindex < gpanel.gridPanes[xindex].length ){
		IPane pane = gpanel.gridPanes[xindex][yindex];
		gpanel.enableFullScreen(pane);
		return pane;
	    }
	}
	return null;
    }
    
    /** put top pane on the full screen inside the window if the panel is IGridPanel */
    public static IPane topPane(){ return gridPane(0,0); }
    
    /** bottom pane is identical with top pane in IGridPanel */
    public static IPane bottomPane(){ return topPane(); }
    
    /** put perspective pane on the full screen inside the window if the panel is IGridPanel */
    public static IPane perspectivePane(){ return gridPane(1,0); }
    
    /** axonometric pane is identical with perspective pane in IGridPanel */
    public static IPane axonometricPane(){ return perspectivePane(); }
    
    /** put front pane on the full screen inside the window if the panel is IGridPanel */
    public static IPane frontPane(){ return gridPane(0,1); }
    
    /** back pane is identical with front pane in IGridPanel */
    public static IPane backPane(){ return frontPane(); }
    
    /** put right pane on the full screen inside the window if the panel is IGridPanel */
    public static IPane rightPane(){ return gridPane(1,1); }
    
    /** left pane is identical with front pane in IGridPanel */
    public static IPane leftPane(){ return rightPane(); }
    
    
    
    /** put top view on the full screen inside the window */
    public static void top(){
	IPane pane = topPane();
	if(pane!=null){
	    pane.getView().setTop();
	    pane.focus(); // added 20120615
	}
    }
    public static void top(double x, double y){
	IPane pane = topPane();
	if(pane!=null){ pane.getView().setTop(x,y); }
    }
    public static void top(double x, double y, double z){
	IPane pane = topPane();
	if(pane!=null){ pane.getView().setTop(x,y,z); }
    }
    public static void top(double x, double y, double z, double axonRatio){
	IPane pane = topPane();
	if(pane!=null){ pane.getView().setTop(x,y,z,axonRatio); }
    }
    public static void topView(){ top(); }
    public static void topView(double x, double y){ top(x,y); }
    public static void topView(double x, double y, double z){ top(x,y,z); }
    public static void topView(double x, double y, double z, double axonRatio){
	top(x,y,z,axonRatio);
    }
    
    /** put bottom view on the full screen inside the window */
    public static void bottom(){
	IPane pane = bottomPane();
	if(pane!=null){
	    pane.getView().setBottom();
	    pane.focus(); // added 20120615
	}
    }
    public static void bottom(double x, double y){
	IPane pane = bottomPane();
	if(pane!=null){ pane.getView().setBottom(x,y); }
    }
    public static void bottom(double x, double y, double z){
	IPane pane = bottomPane();
	if(pane!=null){ pane.getView().setBottom(x,y,z); }
    }
    public static void bottom(double x, double y, double z, double axonRatio){
	IPane pane = bottomPane();
	if(pane!=null){ pane.getView().setBottom(x,y,z,axonRatio); }
    }
    public static void bottomView(){ bottom(); }
    public static void bottomView(double x, double y){ bottom(x,y); }
    public static void bottomView(double x, double y, double z){ bottom(x,y,z); }
    public static void bottomView(double x, double y, double z, double axonRatio){ bottom(x,y,z,axonRatio); }
    
    
    /** put perspective view on the full screen inside the window */
    public static void perspective(){
	IPane pane = perspectivePane();
	if(pane!=null){
	    pane.getView().setPerspective();
	    pane.focus(); // added 20120615
	}
    }
    public static void perspective(double x, double y, double z){
	IPane pane = perspectivePane();
	if(pane!=null){ pane.getView().setPerspective(x,y,z); }
    }
    public static void perspective(double x, double y, double z,
				   double yaw, double pitch){
	IPane pane = perspectivePane();
	if(pane!=null){ pane.getView().setPerspective(x,y,z,yaw,pitch); }
    }
    public static void perspectiveView(){ perspective(); }
    public static void perspectiveView(double x, double y, double z){ perspective(x,y,z); }
    public static void perspectiveView(double x, double y, double z,
				       double yaw, double pitch){ perspective(x,y,z,yaw,pitch); }
    public static void pers(){ perspective(); }
    public static void pers(double x, double y, double z){ perspective(x,y,z); }
    public static void pers(double x, double y, double z, double yaw, double pitch){ perspective(x,y,z,yaw,pitch); }
    
    
    /** put perspective view on the full screen inside the window */
    public static void perspective(double perspectiveAngle){
	IPane pane = perspectivePane();
	if(pane!=null){
	    pane.getView().setPerspective(perspectiveAngle);
	}
    }
    public static void perspective(double x, double y, double z,
				   double perspectiveAngle){
	IPane pane = perspectivePane();
	if(pane!=null){
	    pane.getView().setPerspective(x,y,z,perspectiveAngle);
	}
    }
    public static void perspective(double x, double y, double z,
				   double yaw, double pitch,
				   double perspectiveAngle){
	IPane pane = perspectivePane();
	if(pane!=null){
	    pane.getView().setPerspective(x,y,z,yaw,pitch,perspectiveAngle);
	}
    }
    public static void perspectiveView(double perspectiveAngle){ perspective(perspectiveAngle); }
    public static void perspectiveView(double x, double y, double z,
				       double perspectiveAngle){
	perspective(x,y,z,perspectiveAngle);
    }
    public static void perspectiveView(double x, double y, double z,
				       double yaw, double pitch,
				       double perspectiveAngle){
	perspective(x,y,z,yaw,pitch,perspectiveAngle);
    }
    public static void pers(double perspectiveAngle){ perspective(perspectiveAngle); }
    public static void pers(double x, double y, double z, double perspectiveAngle){
	perspective(x,y,z,perspectiveAngle);
    }
    public static void pers(double x, double y, double z, double yaw, double pitch, double perspectiveAngle){
	perspective(x,y,z,yaw,pitch,perspectiveAngle);
    }
    
    /** put axonometric view on the full screen inside the window */
    public static void axonometric(){
	IPane pane = axonometricPane();
	if(pane!=null){
	    pane.getView().setAxonometric();
	    pane.focus(); // added 20120615
	}
    }
    public static void axonometric(double x, double y, double z){
	IPane pane = axonometricPane();
	if(pane!=null){ pane.getView().setAxonometric(x,y,z); }
    }
    public static void axonometric(double x, double y, double z, double axonRatio){
	IPane pane = axonometricPane();
	if(pane!=null){ pane.getView().setAxonometric(x,y,z,axonRatio); }
    }
    public static void axonometric(double x, double y, double z, double yaw, double pitch){
	IPane pane = axonometricPane();
	if(pane!=null){ pane.getView().setAxonometric(x,y,z,yaw,pitch); }
    }
    public static void axonometric(double x, double y, double z, double yaw, double pitch, double axonRatio){
	IPane pane = axonometricPane();
	if(pane!=null){ pane.getView().setAxonometric(x,y,z,yaw,pitch,axonRatio); }
    }
    public static void axonometricView(){ axonometric(); }
    public static void axonometricView(double x, double y, double z){
	axonometric(x,y,z);
    }
    public static void axonometricView(double x, double y, double z, double axonRatio){
	axonometric(x,y,z,axonRatio);
    }
    public static void axonometricView(double x, double y, double z,
				       double yaw, double pitch){
	axonometric(x,y,z,yaw,pitch);
    }
    public static void axonometricView(double x, double y, double z,
				       double yaw, double pitch, double axonRatio){
	axonometric(x,y,z,yaw,pitch,axonRatio);
    }
    public static void axon(){ axonometric(); }
    public static void axon(double x, double y, double z){
	axonometric(x,y,z);
    }
    public static void axon(double x, double y, double z, double axonRatio){
	axonometric(x,y,z,axonRatio);
    }
    public static void axon(double x, double y, double z, double yaw, double pitch){
	axonometric(x,y,z,yaw,pitch);
    }
    public static void axon(double x, double y, double z, double yaw, double pitch, double axonRatio){
	axonometric(x,y,z,yaw,pitch,axonRatio);
    }
    
    
    /** put front view on the full screen inside the window */
    public static void front(){
	IPane pane = frontPane();
	if(pane!=null){
	    pane.getView().setFront();
	    pane.focus(); // added 20120615
	}
    }
    public static void front(double x, double z){
	IPane pane = frontPane();
	if(pane!=null){ pane.getView().setFront(x,z); }
    }
    public static void front(double x, double y, double z){
	IPane pane = frontPane();
	if(pane!=null){ pane.getView().setFront(x,y,z); }
    }
    public static void front(double x, double y, double z, double axonRatio){
	IPane pane = frontPane();
	if(pane!=null){ pane.getView().setFront(x,y,z,axonRatio); }
    }
    public static void frontView(){ front(); }
    public static void frontView(double x, double z){ front(x,z); }
    public static void frontView(double x, double y, double z){ front(x,y,z); }
    public static void frontView(double x, double y, double z, double axonRatio){ front(x,y,z,axonRatio); }
    
    /** put back view on the full screen inside the window */
    public static void back(){
	IPane pane = backPane();
	if(pane!=null){
	    pane.getView().setBack();
	    pane.focus(); // added 20120615
	}
    }
    public static void back(double x, double z){
	IPane pane = backPane();
	if(pane!=null){ pane.getView().setBack(x,z); }
    }
    public static void back(double x, double y, double z){
	IPane pane = backPane();
	if(pane!=null){ pane.getView().setBack(x,y,z); }
    }
    public static void back(double x, double y, double z, double axonRatio){
	IPane pane = backPane();
	if(pane!=null){ pane.getView().setBack(x,y,z,axonRatio); }
    }
    public static void backView(){ back(); }
    public static void backView(double x, double z){ back(x,z); }
    public static void backView(double x, double y, double z){ back(x,y,z); }
    public static void backView(double x, double y, double z, double axonRatio){ back(x,y,z,axonRatio); }
    
    /** put right view on the full screen inside the window */
    public static void right(){
	IPane pane = rightPane();
	if(pane!=null){
	    pane.getView().setRight();
	    pane.focus(); // added 20120615
	}
    }
    public static void right(double y, double z){
	IPane pane = rightPane();
	if(pane!=null){ pane.getView().setRight(y, z); }
    }
    public static void right(double x, double y, double z){
	IPane pane = rightPane();
	if(pane!=null){ pane.getView().setRight(x, y, z); }
    }
    public static void right(double x, double y, double z, double axonRatio){
	IPane pane = rightPane();
	if(pane!=null){ pane.getView().setRight(x, y, z, axonRatio); }
    }
    public static void rightView(){ right(); }
    public static void rightView(double y, double z){ right(y,z); }
    public static void rightView(double x, double y, double z){ right(x,y,z); }
    public static void rightView(double x, double y, double z, double axonRatio){ right(x,y,z,axonRatio); }
    
    /** put left view on the full screen inside the window */
    public static void left(){
	IPane pane = leftPane();
	if(pane!=null){
	    pane.getView().setLeft();
	    pane.focus(); // added 20120615
	}
    }
    public static void left(double y, double z){
	IPane pane = leftPane();
	if(pane!=null){ pane.getView().setLeft(y,z); }
    }
    public static void left(double x, double y, double z){
	IPane pane = leftPane();
	if(pane!=null){ pane.getView().setLeft(x,y,z); }
    }
    public static void left(double x, double y, double z, double axonRatio){
	IPane pane = leftPane();
	if(pane!=null){ pane.getView().setLeft(x,y,z,axonRatio); }
    }
    public static void leftView(){ left(); }
    public static void leftView(double y, double z){ left(y,z); }
    public static void leftView(double x, double y, double z){ left(x,y,z); }
    public static void leftView(double x, double y, double z, double axonRatio){ left(x,y,z,axonRatio); }
    
    
    /****************************
     * background color
     ***************************/
    
    //public static void setBG(Color c){}
    //public static void setBG(Color c1, Color c2){}
    //public static void setBG(Color c1, Color c2, Color c3, Color c4){}
    //public static void setBG(Image img){}
    
    public static void bg(Color c1, Color c2, Color c3, Color c4){
	IG ig = cur(); if(ig==null) return;
	ig.server().bg(c1,c2,c3,c4);
    }
    
    public static void background(Color c1, Color c2, Color c3, Color c4){ bg(c1,c2,c3,c4); }
    
    public static void bg(Color c){ bg(c,c,c,c); }
    public static void background(Color c){ bg(c); }
    
    public static void bg(int r1, int g1, int b1,
			  int r2, int g2, int b2,
			  int r3, int g3, int b3,
			  int r4, int g4, int b4){
	bg(IGraphicObject.getColor(r1,g1,b1), IGraphicObject.getColor(r2,g2,b2),
	   IGraphicObject.getColor(r3,g3,b3), IGraphicObject.getColor(r4,g4,b4));
    }
    public static void background(int r1, int g1, int b1,
				  int r2, int g2, int b2,
				  int r3, int g3, int b3,
				  int r4, int g4, int b4){
	bg(r1,g1,b1,r2,g2,b2,r3,g3,b3,r4,b4,g4);
    }
    
    public static void bg(int r, int g, int b){
	bg(IGraphicObject.getColor(r,g,b));
    }
    public static void background(int r, int g, int b){ bg(r,g,b); }
    
    public static void bg(int gray1, int gray2, int gray3, int gray4){
	bg(IGraphicObject.getColor(gray1), IGraphicObject.getColor(gray2),
	   IGraphicObject.getColor(gray3), IGraphicObject.getColor(gray4));
    }
    public static void background(int gray1, int gray2, int gray3, int gray4){
	bg(gray1,gray2,gray3,gray4);
    }
    
    public static void bg(int gray){ bg(IGraphicObject.getColor(gray)); }
    
    public static void background(int gray){ bg(gray); }
    
    
    
    public static void bg(float r1, float g1, float b1,
			  float r2, float g2, float b2,
			  float r3, float g3, float b3,
			  float r4, float g4, float b4){
	bg(IGraphicObject.getColor(r1,g1,b1), IGraphicObject.getColor(r2,g2,b2),
	   IGraphicObject.getColor(r3,g3,b3), IGraphicObject.getColor(r4,g4,b4));
    }
    public static void background(float r1, float g1, float b1,
				  float r2, float g2, float b2,
				  float r3, float g3, float b3,
				  float r4, float g4, float b4){
	bg(r1,g1,b1,r2,g2,b2,r3,g3,b3,r4,b4,g4);
    }
    
    public static void bg(float r, float g, float b){
	bg(IGraphicObject.getColor(r,g,b));
    }
    
    public static void background(float r, float g, float b){ bg(r,g,b); }
    
    public static void bg(float gray1, float gray2, float gray3, float gray4){
	bg(IGraphicObject.getColor(gray1), IGraphicObject.getColor(gray2),
	   IGraphicObject.getColor(gray3), IGraphicObject.getColor(gray4));
    }
    public static void background(float gray1, float gray2, float gray3, float gray4){
	bg(gray1,gray2,gray3,gray4);
    }
    
    public static void bg(float gray){ bg(IGraphicObject.getColor(gray)); }
    
    public static void background(float gray){ bg(gray); }
    
    
    
    public static void bg(double r1, double g1, double b1,
			  double r2, double g2, double b2,
			  double r3, double g3, double b3,
			  double r4, double g4, double b4){
	bg(IGraphicObject.getColor((float)r1,(float)g1,(float)b1),
	   IGraphicObject.getColor((float)r2,(float)g2,(float)b2),
	   IGraphicObject.getColor((float)r3,(float)g3,(float)b3),
	   IGraphicObject.getColor((float)r4,(float)g4,(float)b4));
    }
    public static void background(double r1, double g1, double b1,
				  double r2, double g2, double b2,
				  double r3, double g3, double b3,
				  double r4, double g4, double b4){
	bg(r1,g1,b1,r2,g2,b2,r3,g3,b3,r4,b4,g4);
    }
    
    public static void bg(double r, double g, double b){
	bg(IGraphicObject.getColor((float)r,(float)g,(float)b));
    }
    
    public static void background(double r, double g, double b){ bg(r,g,b); }
    
    public static void bg(double gray1, double gray2, double gray3, double gray4){
	bg(IGraphicObject.getColor((float)gray1), IGraphicObject.getColor((float)gray2),
	   IGraphicObject.getColor((float)gray3), IGraphicObject.getColor((float)gray4));
    }
    public static void background(double gray1, double gray2, double gray3, double gray4){
	bg(gray1,gray2,gray3,gray4);
    }
    
    public static void bg(double gray){ bg(IGraphicObject.getColor((float)gray)); }
    public static void background(double gray){ bg(gray); }
    
    
    
    /** Print method.
	This is a wrapper of IOut.p(), which is 
	also a wrapper of System.out.println() in most part.
    */
    public static void p(Object obj){ IOut.printlnWithOffset(obj,1); }
    public static void p(){ IOut.printlnWithOffset(1); }
    public static void enabePrintPrefix(){ IOut.enablePrefix(); }
    public static void disablePrintPrefix(){ IOut.disablePrefix(); }
    
    /** Error print method.
	This is a wrapper of IOut.err()
    */
    public static void err(Object obj){ IOut.errWithOffset(obj,1); }
    public static void err(){ IOut.errWithOffset(1); }
    public static void enabeErrorPrefix(){ IOut.enablePrefix(); }
    public static void disableErrorPrefix(){ IOut.disablePrefix(); }
    
    /** change the debug level of IOut */
    public static void debugLevel(int level){ IOut.debugLevel(level); }
    public static int debugLevel(){ return IOut.debugLevel(); }
    
    
    /*************************************************************************
     * object methods
     *************************************************************************/
    
    // anybody would want this in public?
    protected IG(){
	server = new IServer(this);
    }
    
    protected IG(IPanel p){
	server = new IServer(this, p);
	panel = p; // 
	p.setIG(this);
    }
    
    public boolean openFile(String file){
	boolean retval = false;
	if(inputWrapper!=null){ retval = IIO.open(file,this,inputWrapper); }
	else{
	    File f = new File(file);
	    if(!f.isAbsolute() && basePath!=null) file = basePath + File.separator + file;
	    retval = IIO.open(file,this);
	}
	server.updateState(); // update server status
	inputFile = file;
	focusView();
	return retval;
    }
    
    public boolean saveFile(String file){
	File f = new File(file);
	if(!f.isAbsolute() && basePath!=null){
	    file = basePath + File.separator + file;
	    File baseDir = new File(basePath);
	    if(!baseDir.isDirectory()){
		IOut.debug(20, "creating directory"+baseDir.toString());
		if(!baseDir.mkdir()){
		    IOut.err("failed to create directory: "+baseDir.toString());
		}
	    }
	}
	return IIO.save(file,this);
    }
    
    public boolean save(){
	if(outputFile==null){
	    IOut.err("output filename is not set. not saved");
	    return false;
	}
	return saveFile(outputFile);
    }
    
    public void setInputFile(String filename){ inputFile=filename; }
    public void setOutputFile(String filename){ outputFile=filename; }
    public String getInputFile(){ return inputFile; }
    public String getOutputFile(){ return outputFile; }
    
    public String getBasePath(){ return basePath; }
    public String setBasePath(String path){ return basePath=path; }
    
    public void setInputWrapper(IInputWrapper wrapper){ inputWrapper = wrapper; }
    
    public ILayer getLayer(String layerName){ return server.getLayer(layerName); }
    public ILayer getLayer(int i){ return server.getLayer(i); }
    public ILayer[] getAllLayers(){ return server.getAllLayers(); }
    public void deleteLayer(String layerName){ server.deleteLayer(layerName); }
    public int getLayerNum(){ return server.layerNum(); }
    
    public IPoint[] getPoints(){ return server.points(); }
    public ICurve[] getCurves(){ return server.curves(); }
    public ISurface[] getSurfaces(){ return server.surfaces(); }
    public IMesh[] getMeshes(){ return server.meshes(); }
    public IBrep[] getBreps(){ return server.breps(); }
    public IGeometry[] getGeometries(){ return server.geometries(); }
    public IObject[] getObjects(Class cls){ return server.objects(cls); }
    public IObject[] getObjects(){ return server.objects(); }
    
    public IPoint getPoint(int i){ return server.point(i); }
    public ICurve getCurve(int i){ return server.curve(i); }
    public ISurface getSurface(int i){ return server.surface(i); }
    public IMesh getMesh(int i){ return server.mesh(i); }
    public IBrep getBrep(int i){ return server.brep(i); }
    public IGeometry getGeometry(int i){ return server.geometry(i); }
    public IObject getObject(Class cls,int i){ return server.object(cls,i); }
    public IObject getObject(int i){ return server.object(i); }
    
    public int getPointNum(){ return server.pointNum(); }
    public int getCurveNum(){ return server.curveNum(); }
    public int getSurfaceNum(){ return server.surfaceNum(); }
    public int getMeshNum(){ return server.meshNum(); }
    public int getBrepNum(){ return server.brepNum(); }
    public int getGeometryNum(){ return server.geometryNum(); }
    public int getObjectNum(Class cls){ return server.objectNum(cls); }
    public int getObjectNum(){ return server.objectNum(); }
    
    
    public void focusView(){
	if(panel!=null) panel.focus(); // focus on all pane
    }
    
    public IServer server(){ return server; }
    public IDynamicServer dynamicServer(){ return server.dynamicServer(); }
    
    // dynamics
    public void setDuration(int dur){ server.duration(dur); }
    public int getDuration(){ return server.duration(); }
    
    public void setTime(int tm){ server.time(tm); }
    public int getTime(){ return server.time(); }
    
    public void pauseDynamics(){ server.pause(); }
    public void resumeDynamics(){ server.resume(); }
    
    public void startDynamics(){ server.start(); }
    public void stopDynamics(){ server.stop(); }
    
    //public boolean isDynamicsRunning(){ server.
    
    
    //public void draw(IGraphics g){ server.draw(g); }
    
    //public IGPane pane(){ return pane; }
    //public IPanel panel(){ return panel; }
    
    //public void delete(){
    public void clear(){ server.clear(); }
    
    
    
    /*********************************************************************
     * Static Geometry Operations
     ********************************************************************/
    
    /** point creation */
    public static IPoint point(IVecI v){ return pt(v); }
    public static IPoint point(IVec v){ return pt(v); }
    public static IPoint point(double x, double y, double z){ return pt(x,y,z); }
    public static IPoint point(double x, double y){ return pt(x,y); }
        
    /** point creation shorter name */
    public static IPoint pt(IVecI v){ return new IPoint(v); }
    public static IPoint pt(IVec v){ return new IPoint(v); }
    public static IPoint pt(double x, double y, double z){ return new IPoint(x,y,z); }
    public static IPoint pt(double x, double y){ return new IPoint(x,y); }
    
    
    public static ICurve curve(IVecI[] cpts, int degree, double[] knots, double ustart, double uend){
	return ICurveCreator.curve(cpts,degree,knots,ustart,uend);
    }
    
    public static ICurve curve(IVecI[] cpts, int degree, double[] knots){
	return ICurveCreator.curve(cpts,degree,knots);
    }
    
    public static ICurve curve(IVecI[] cpts, int degree){
	return ICurveCreator.curve(cpts,degree);
    }
    
    public static ICurve curve(IVecI[] cpts){
	return ICurveCreator.curve(cpts);
    }
    
    public static ICurve curve(IVecI[] cpts, int degree, boolean close){
	return ICurveCreator.curve(cpts,degree,close);
    }
    public static ICurve curve(IVecI[] cpts, boolean close){
	return ICurveCreator.curve(cpts,close);
    }
    public static ICurve curve(IVecI pt1, IVecI pt2){
	return ICurveCreator.curve(pt1,pt2);
    }
    /** this creates a line between a same point */
    public static ICurve curve(IVecI pt){ return ICurveCreator.curve(pt); }
    
    
    public static ICurve curve(double x1, double y1, double z1, double x2, double y2, double z2){
	return ICurveCreator.curve(x1,y1,z1,x2,y2,z2);
    }
    public static ICurve curve(double[][] xyzValues){
	return ICurveCreator.curve(xyzValues);
    }    
    public static ICurve curve(double[][] xyzValues, int degree){
	return ICurveCreator.curve(xyzValues,degree);
    }
    public static ICurve curve(double[][] xyzValues, boolean close){
	return ICurveCreator.curve(xyzValues,close);
    }
    public static ICurve curve(double[][] xyzValues, int degree, boolean close){
	return ICurveCreator.curve(xyzValues,degree,close);
    }
    public static ICurve curve(ICurveI crv){
	return ICurveCreator.curve(crv);
    }


    /***********
     * curve short name : crv
     **********/
    
    public static ICurve crv(IVecI[] cpts, int degree, double[] knots, double ustart, double uend){
	return curve(cpts,degree,knots,ustart,uend);
    }
    public static ICurve crv(IVecI[] cpts, int degree, double[] knots){
	return curve(cpts,degree,knots);
    }
    public static ICurve crv(IVecI[] cpts, int degree){
	return curve(cpts,degree);
    }
    public static ICurve crv(IVecI[] cpts){ return curve(cpts); }
    
    public static ICurve crv(IVecI[] cpts, int degree, boolean close){
	return curve(cpts,degree,close);
    }
    public static ICurve crv(IVecI[] cpts, boolean close){
	return curve(cpts,close);
    }
    public static ICurve crv(IVecI pt1, IVecI pt2){ return curve(pt1,pt2); }
    /** this creates a line between a same point */
    public static ICurve crv(IVecI pt){ return curve(pt); }
    public static ICurve crv(double x1, double y1, double z1, double x2, double y2, double z2){
	return curve(x1,y1,z1,x2,y2,z2);
    }
    public static ICurve crv(double[][] xyzValues){ return curve(xyzValues); }
    public static ICurve crv(double[][] xyzValues, int degree){
	return curve(xyzValues,degree);
    }
    public static ICurve crv(double[][] xyzValues, boolean close){
	return curve(xyzValues,close);
    }
    public static ICurve crv(double[][] xyzValues, int degree, boolean close){
	return curve(xyzValues,degree,close);
    }
    public static ICurve crv(ICurveI crv){ return curve(crv); }
    
    

    /***********
     * line : type of curve.
     **********/
    
    public static ICurve line(IVecI pt1, IVecI pt2){ return curve(pt1,pt2); }
    /** this creates a line between a same point */
    public static ICurve line(IVecI pt){ return curve(pt); }
    public static ICurve line(double x1, double y1, double z1, double x2, double y2, double z2){
	return curve(x1,y1,z1,x2,y2,z2);
    }
    
    
    /************
     * rectangle
     ***********/
    public static ICurve rect(IVecI corner, double xwidth, double yheight){
	return ICurveCreator.rect(corner,xwidth,yheight);
    }
    public static ICurve rect(IVecI corner, IVecI width, IVecI height){
	return ICurveCreator.rect(corner,width,height);
    }
    public static ICurve rect(double x, double y, double z, double xwidth, double yheight){
	return ICurveCreator.rect(x,y,z,xwidth,yheight);
    }
    /************
     * circle
     ***********/
    public static ICircle circle(IVecI center, IVecI normal, IDoubleI radius){
	return ICurveCreator.circle(center,normal,radius);
    }
    public static ICircle circle(IVecI center, IVecI normal, double radius){
        return ICurveCreator.circle(center,normal,radius);
    }
    public static ICircle circle(IVecI center, IDoubleI radius){
	return ICurveCreator.circle(center,radius);
    }
    public static ICircle circle(IVecI center, double radius){
        return ICurveCreator.circle(center,radius);
    }
    public static ICircle circle(double x, double y, double z, double radius){
        return ICurveCreator.circle(x,y,z,radius);
    }
    public static ICircle circle(IVecI center, IVecI normal, IDoubleI xradius, IDoubleI yradius){
        return ICurveCreator.circle(center,normal,xradius,yradius);
    }
    public static ICircle circle(IVecI center, IVecI normal, double xradius, double yradius){
	return ICurveCreator.circle(center,normal,xradius,yradius);
    }
    public static ICircle circle(IVecI center, IDoubleI xradius, IDoubleI yradius){
        return ICurveCreator.circle(center,xradius,yradius);
    }
    public static ICircle circle(IVecI center, double xradius, double yradius){
	return ICurveCreator.circle(center,xradius,yradius);
    }
    public static ICircle circle(double x, double y, double z, double xradius, double yradius){
	return ICurveCreator.circle(x,y,z,xradius,yradius);
    }
    public static ICircle circle(IVecI center, IVecI normal, IVecI rollDir, double radius){
	return ICurveCreator.circle(center,normal,rollDir,radius);
    }
    public static ICircle circle(IVecI center, IVecI normal, IVecI rollDir, IDoubleI radius){
	return ICurveCreator.circle(center,normal,rollDir,radius);
    }
    public static ICircle circle(IVecI center, IVecI normal, IVecI rollDir, double xradius, double yradius){
	return ICurveCreator.circle(center,normal,rollDir,xradius,yradius);
    }
    public static ICircle circle(IVecI center, IVecI normal, IVecI rollDir, IDoubleI xradius, IDoubleI yradius){
        return ICurveCreator.circle(center,normal,rollDir,xradius,yradius);
    }
    public static ICircle circle(IVecI center, IVecI xradiusVec, IVecI yradiusVec){
        return ICurveCreator.circle(center,xradiusVec,yradiusVec);
    }
    public static ICircle circle(IVecI center, IVecI normal, IDoubleI radius, boolean approx){
        return ICurveCreator.circle(center,normal,radius,approx);
    }
    public static ICircle circle(IVecI center, IVecI normal, double radius, boolean approx){
	return ICurveCreator.circle(center,normal,radius,approx);
    }
    public static ICircle circle(IVecI center, IDoubleI radius, boolean approx){
	return ICurveCreator.circle(center,radius,approx);
    }
    public static ICircle circle(IVecI center, double radius, boolean approx){
	return ICurveCreator.circle(center,radius,approx);
    }
    public static ICircle circle(double x, double y, double z, double radius, boolean approx){
	return ICurveCreator.circle(x,y,z,radius,approx);
    }
    public static ICircle circle(IVecI center, IVecI normal, double xradius, double yradius, boolean approx){
	return ICurveCreator.circle(center,normal,xradius,yradius,approx);
    }
    public static ICircle circle(IVecI center, IVecI normal, IDoubleI xradius, IDoubleI yradius, boolean approx){
	return ICurveCreator.circle(center,normal,xradius,yradius,approx);
    }
    public static ICircle circle(IVecI center, double xradius, double yradius, boolean approx){
	return ICurveCreator.circle(center,xradius,yradius,approx);
    }
    public static ICircle circle(IVecI center, IDoubleI xradius, IDoubleI yradius, boolean approx){
	return ICurveCreator.circle(center,xradius,yradius,approx);
    }
    public static ICircle circle(double x, double y, double z, double xradius, double yradius, boolean approx){
	return ICurveCreator.circle(x,y,z,xradius,yradius,approx);
    }
    public static ICircle circle(IVecI center, IVecI normal, IVecI rollDir, double radius, boolean approx){
        return ICurveCreator.circle(center,normal,rollDir,radius,approx);
    }
    public static ICircle circle(IVecI center, IVecI normal, IVecI rollDir, IDoubleI radius, boolean approx){
	return ICurveCreator.circle(center,normal,rollDir,radius,approx);
    }
    
    public static ICircle circle(IVecI center, IVecI normal, IVecI rollDir, double xradius, double yradius, boolean approx){
	return ICurveCreator.circle(center,normal,rollDir,xradius,yradius,approx);
    }
    
    public static ICircle circle(IVecI center, IVecI normal, IVecI rollDir, IDoubleI xradius, IDoubleI yradius, boolean approx){
	return ICurveCreator.circle(center,normal,rollDir,xradius,yradius,approx);
    }
    
    public static ICircle circle(IVecI center, IVecI xradiusVec, IVecI yradiusVec, boolean approx){
	return ICurveCreator.circle(center,xradiusVec,yradiusVec,approx);
    }
    
    
    /************
     * ellipse (alias of some of circle)
     ***********/
    public static ICircle ellipse(IVecI center, IVecI xradiusVec, IVecI yradiusVec){
	return ICurveCreator.ellipse(center,xradiusVec,yradiusVec);
    }
    public static ICircle ellipse(IVecI center, IDoubleI xradius, IDoubleI yradius){
        return ICurveCreator.ellipse(center,xradius,yradius);
    }
    public static ICircle ellipse(IVecI center, double xradius, double yradius){
	return ICurveCreator.ellipse(center,xradius,yradius);
    }
    public static ICircle ellipse(double x, double y, double z, double xradius, double yradius){
	return ICurveCreator.ellipse(x,y,z,xradius,yradius);
    }
    
    
    /************
     * arc
     ***********/
    public static IArc arc(IVecI center, IVecI normal, IVecI startPt, double angle){
        return ICurveCreator.arc(center,normal,startPt,angle);
    }
    public static IArc arc(IVecI center, IVecI normal, IVecI startPt, IDoubleI angle){
	return ICurveCreator.arc(center,normal,startPt,angle);
    }
    public static IArc arc(IVecI center, IVecI startPt, double angle){
	return ICurveCreator.arc(center,startPt,angle);
    }
    public static IArc arc(IVecI center, IVecI startPt, IDoubleI angle){
	return ICurveCreator.arc(center,startPt,angle);
    }
    public static IArc arc(double x, double y, double z, double startX, double startY, double startZ, double angle){
	return ICurveCreator.arc(x,y,z,startX,startY,startZ,angle);
    }
    public static IArc arc(IVecI center, IVecI startPt, IVecI endPt, IBoolI flipArcSide){
	return ICurveCreator.arc(center,startPt,endPt,flipArcSide);
    }
    public static IArc arc(IVecI center, IVecI startPt, IVecI endPt, boolean flipArcSide){
	return ICurveCreator.arc(center,startPt,endPt,flipArcSide);
    }
    public static IArc arc(IVecI center, IVecI startPt, IVecI midPt, IVecI endPt, IVecI normal){
	return ICurveCreator.arc(center,startPt,midPt,endPt,normal);
    }
    
    
    /************
     * offset curve
     ***********/
    public static ICurve offset(ICurveI curve, double width, IVecI planeNormal){
	return ICurveCreator.offset(curve,width,planeNormal);
    }
    
    public static ICurve offset(ICurveI curve, IDoubleI width, IVecI planeNormal){
	return ICurveCreator.offset(curve,width,planeNormal);
    }
    
    public static ICurve offset(ICurveI curve, double width){
	return ICurveCreator.offset(curve,width);
    }
    
    public static ICurve offset(ICurveI curve, IDoubleI width){
	return ICurveCreator.offset(curve,width);
    }
    
    
    /************
     * offset points
     ***********/
    
    public static IVec[] offset(IVec[] pts, double width, IVecI planeNormal){
	return IVec.offset(pts,width,planeNormal);
    }    
    public static IVec[] offset(IVec[] pts, double width, IVecI planeNormal, boolean close){
	return IVec.offset(pts,width,planeNormal,close);
    }
    
    public static IVecI[] offset(IVecI[] pts, double width, IVecI planeNormal, boolean close){
        return IVec.offset(pts,width,planeNormal,close);
    }
    public static IVecI[] offset(IVecI[] pts, double width, IVecI planeNormal){
	return IVec.offset(pts,width,planeNormal);
    }
    public static IVecI[] offset(IVecI[] pts, IDoubleI width, IVecI planeNormal, boolean close){
	return IVec.offset(pts,width,planeNormal,close);
    }
    public static IVecI[] offset(IVecI[] pts, IDoubleI width, IVecI planeNormal){
	return IVec.offset(pts,width,planeNormal);
    }
    
    public static IVecI[] offset(IVecI[] pts, IVecI[] normal, double width){
	return IVec.offset(pts,normal,width);
    }
    public static IVecI[] offset(IVecI[] pts, IVecI[] normal, IDoubleI width){
        return IVec.offset(pts, normal, width);
    }
    public static IVec[] offset(IVec[] pts, double width){
	return IVec.offset(pts,width);
    }
    public static IVec[] offset(IVec[] pts, double width, boolean close){
	return IVec.offset(pts,width,close);
    }
    public static IVecI[] offset(IVecI[] pts, double width, boolean close){
	return IVec.offset(pts,width,close);
    }
    public static IVecI[] offset(IVecI[] pts, double width){
	return IVec.offset(pts,width);
    }       
    public static IVecI[] offset(IVecI[] pts, IDoubleI width, boolean close){
	return IVec.offset(pts,width,close);
    }
    public static IVecI[] offset(IVecI[] pts, IDoubleI width){
	return IVec.offset(pts,width);
    }
    
    
    
    /*****************************************************************
     * surfaces
     *****************************************************************/
    
    public static ISurface surface(IVecI[][] cpts, int udegree, int vdegree,
				   double[] uknots, double[] vknots,
				   double ustart, double uend, double vstart, double vend){
	return ISurfaceCreator.surface(cpts,udegree,vdegree,uknots,vknots,
				       ustart,uend,vstart,vend);
    }
    
    public static ISurface surface(IVecI[][] cpts, int udegree, int vdegree,
				   double[] uknots, double[] vknots){
	return ISurfaceCreator.surface(cpts,udegree,vdegree,uknots,vknots);
    }
    
    public static ISurface surface(IVecI[][] cpts, int udegree, int vdegree){
        return ISurfaceCreator.surface(cpts,udegree,vdegree);
    }
    
    public static ISurface surface(IVecI[][] cpts){
	return ISurfaceCreator.surface(cpts);
    }
    
    public static ISurface surface(IVecI[][] cpts, int udegree, int vdegree,
				   boolean closeU, boolean closeV){
	return ISurfaceCreator.surface(cpts,udegree,vdegree,closeU,closeV);
    }
    
    public static ISurface surface(IVecI[][] cpts, int udegree, int vdegree,
				   boolean closeU, double[] vk){
	return ISurfaceCreator.surface(cpts,udegree,vdegree,closeU,vk);
    }
    
    public static ISurface surface(IVecI[][] cpts, int udegree, int vdegree,
				   double[] uk, boolean closeV){
	return ISurfaceCreator.surface(cpts,udegree,vdegree,uk,closeV);
    }
    
    public static ISurface surface(IVecI[][] cpts, boolean closeU, boolean closeV){
	return ISurfaceCreator.surface(cpts,closeU,closeV);
    }
    
    public static ISurface surface(IVecI pt1, IVecI pt2, IVecI pt3, IVecI pt4){
	return ISurfaceCreator.surface(pt1,pt2,pt3,pt4);
    }
    
    public static ISurface surface(IVecI pt1, IVecI pt2, IVecI pt3){
	return ISurfaceCreator.surface(pt1,pt2,pt3);
    }
    
    public static ISurface surface(double x1, double y1, double z1,
				   double x2, double y2, double z2,
				   double x3, double y3, double z3,
				   double x4, double y4, double z4){
	return ISurfaceCreator.surface(x1,y1,z1,x2,y2,z2,x3,y3,z3,x4,y4,z4);
    }
    
    public static ISurface surface(double x1, double y1, double z1,
				   double x2, double y2, double z2,
				   double x3, double y3, double z3){
	return ISurfaceCreator.surface(x1,y1,z1,x2,y2,z2,x3,y3,z3);
    }
    
    public static ISurface surface(double[][][] xyzValues){
	return ISurfaceCreator.surface(xyzValues);
    }
    
    public static ISurface surface(double[][][] xyzValues, int udeg, int vdeg){
	return ISurfaceCreator.surface(xyzValues,udeg,vdeg);
    }
    
    public static ISurface surface(double[][][] xyzValues, boolean closeU, boolean closeV){
	return ISurfaceCreator.surface(xyzValues,closeU,closeV);
    }
    
    public static ISurface surface(double[][][] xyzValues, int udeg, int vdeg, boolean closeU, boolean closeV){
	return ISurfaceCreator.surface(xyzValues,udeg,vdeg,closeU,closeV);
    }
    
    public static ISurface surface(ISurfaceI srf){
	return ISurfaceCreator.surface(srf);
    }
    
    // planar surface with trim
    public static ISurface surface(ICurveI trimCurve){
	return ISurfaceCreator.surface(trimCurve);
    }
    public static ISurface surface(ICurveI outerTrimCurve, ICurveI[] innerTrimCurves){
	return ISurfaceCreator.surface(outerTrimCurve, innerTrimCurves);
    }
    public static ISurface surface(ICurveI outerTrimCurve, ICurveI innerTrimCurve){
	return ISurfaceCreator.surface(outerTrimCurve, innerTrimCurve);
    }
    public static ISurface surface(ICurveI[] trimCurves){
	return ISurfaceCreator.surface(trimCurves);
    }
    public static ISurface surface(IVecI[] trimCrvPts){
	return ISurfaceCreator.surface(trimCrvPts);
    }
    public static ISurface surface(IVecI[] trimCrvPts, int trimCrvDeg){
	return ISurfaceCreator.surface(trimCrvPts,trimCrvDeg);
    }
    public static ISurface surface(IVecI[] trimCrvPts, int trimCrvDeg, double[] trimCrvKnots){
	return ISurfaceCreator.surface(trimCrvPts,trimCrvDeg,trimCrvKnots);
    }
    
    

    
    /*****************************************************************
     * srf : short name of surfaces
     *****************************************************************/
    
    public static ISurface srf(IVecI[][] cpts, int udegree, int vdegree,
			       double[] uknots, double[] vknots,
			       double ustart, double uend, double vstart, double vend){
	return surface(cpts,udegree,vdegree,uknots,vknots,ustart,uend,vstart,vend);
    }
    
    public static ISurface srf(IVecI[][] cpts, int udegree, int vdegree,
			       double[] uknots, double[] vknots){
	return surface(cpts,udegree,vdegree,uknots,vknots);
    }
    
    public static ISurface srf(IVecI[][] cpts, int udegree, int vdegree){
        return surface(cpts,udegree,vdegree);
    }
    
    public static ISurface srf(IVecI[][] cpts){ return surface(cpts); }
    
    public static ISurface srf(IVecI[][] cpts, int udegree, int vdegree,
			       boolean closeU, boolean closeV){
	return surface(cpts,udegree,vdegree,closeU,closeV);
    }
    
    public static ISurface srf(IVecI[][] cpts, int udegree, int vdegree,
			       boolean closeU, double[] vk){
	return surface(cpts,udegree,vdegree,closeU,vk);
    }
    
    public static ISurface srf(IVecI[][] cpts, int udegree, int vdegree,
			       double[] uk, boolean closeV){
	return surface(cpts,udegree,vdegree,uk,closeV);
    }
    
    public static ISurface srf(IVecI[][] cpts, boolean closeU, boolean closeV){
	return surface(cpts,closeU,closeV);
    }
    
    public static ISurface srf(IVecI pt1, IVecI pt2, IVecI pt3, IVecI pt4){
	return surface(pt1,pt2,pt3,pt4);
    }
    
    public static ISurface srf(IVecI pt1, IVecI pt2, IVecI pt3){
	return surface(pt1,pt2,pt3);
    }
    
    public static ISurface srf(double x1, double y1, double z1,
			       double x2, double y2, double z2,
			       double x3, double y3, double z3,
			       double x4, double y4, double z4){
	return surface(x1,y1,z1,x2,y2,z2,x3,y3,z3,x4,y4,z4);
    }
    
    public static ISurface srf(double x1, double y1, double z1,
			       double x2, double y2, double z2,
			       double x3, double y3, double z3){
	return surface(x1,y1,z1,x2,y2,z2,x3,y3,z3);
    }
    
    public static ISurface srf(double[][][] xyzValues){ return surface(xyzValues); }
    
    public static ISurface srf(double[][][] xyzValues, int udeg, int vdeg){
	return surface(xyzValues,udeg,vdeg);
    }
    
    public static ISurface srf(double[][][] xyzValues, boolean closeU, boolean closeV){
	return surface(xyzValues,closeU,closeV);
    }
    
    public static ISurface srf(double[][][] xyzValues, int udeg, int vdeg, boolean closeU, boolean closeV){
	return surface(xyzValues,udeg,vdeg,closeU,closeV);
    }
    
    public static ISurface srf(ISurfaceI srf){ return surface(srf); }
    
    
    /** planar surface with trim */
    public static ISurface srf(ICurveI trimCurve){ return surface(trimCurve); }
    public static ISurface srf(ICurveI outerTrimCurve, ICurveI[] innerTrimCurves){
	return surface(outerTrimCurve, innerTrimCurves);
    }
    public static ISurface srf(ICurveI outerTrimCurve, ICurveI innerTrimCurve){
	return surface(outerTrimCurve, innerTrimCurve);
    }
    public static ISurface srf(ICurveI[] trimCurves){ return surface(trimCurves); }
    public static ISurface srf(IVecI[] trimCrvPts){ return surface(trimCrvPts); }
    public static ISur
