package org.sgJoe.tools.interfaces;

import javax.media.j3d.View;
import javax.vecmath.Point3d;
import org.apache.log4j.Logger;
import org.sgJoe.graphics.behaviors.BehaviorObserver;
import org.sgJoe.logic.ActionErrors;
import org.sgJoe.logic.Session;
import org.sgJoe.plugin.Form;


/*
 * Descritpion for VToolForm.java
 *
 *
 * @author   $ Author: Aleksandar Babic         $
 * @version  $ Revision:             0.1        $
 * @date     $ Date: April 8, 2006  11:34 AM    $
 */

public abstract class VToolForm  extends Form {
    
    protected VirTool               virToolRef = null;
    protected int                   toolAction = ACT_NONE;
    
    protected Point3d               lGripPt = null;
    protected Point3d               vwGripPt = null;
    protected double                viewingDistance = 0.0;
    
    protected int                   prevX;
    protected int                   prevY;
    protected int                   currX;
    protected int                   currY;
    
    protected View                  viewSource = null;

    public final static int ACT_OFFSET = 10000,
                            ACT_NONE = ACT_OFFSET + 0,
            
                            // Mouse related
                            ACT_MOUSE_PRESSED = ACT_OFFSET + 1,
                            ACT_MOUSE_CLICKED = ACT_OFFSET + 2,
                            ACT_MOUSE_RELEASED = ACT_OFFSET + 3,
                            ACT_MOUSE_DRAGGED = ACT_OFFSET + 4,   
                            ACT_MOUSE_TRANSLATE_X = ACT_OFFSET + 5,
                            ACT_MOUSE_TRANSLATE_Y = ACT_OFFSET + 6,
                            ACT_MOUSE_TRANSLATE_Z = ACT_OFFSET + 7,
                            ACT_MOUSE_TRANSLATE_XY = ACT_OFFSET + 8,
            
                            ACT_MOUSE_ROTATE = ACT_OFFSET + 9,
            
                            // GUI Tool related
                            ACT_ACTIONBUTTON_PRESSED = ACT_OFFSET + 100,
                            
                            // Framework related
                            ACT_ON_INSTANCE_DELETE = ACT_OFFSET + 201,
                            ACT_MOUSE_OUT_OF_SCOPE = ACT_OFFSET + 202,
    
                            // Handle related
                            ACT_MOUSE_PLANARHANDLE_PRESSED = ACT_OFFSET + 301,
                            ACT_MOUSE_PLANARHANDLE_CLICKED = ACT_OFFSET + 302,
                            ACT_MOUSE_PLANARHANDLE_DRAGGED = ACT_OFFSET + 303,
                            ACT_MOUSE_PLANARHANDLE_RELEASED = ACT_OFFSET + 304,
    
                            ACT_MOUSE_SCALEHANDLE_PRESSED = ACT_OFFSET + 304,
                            ACT_MOUSE_SCALEHANDLE_DRAGGED = ACT_OFFSET + 305,
                            ACT_MOUSE_SCALEHANDLE_RELEASED = ACT_OFFSET + 306,
                            
                            ACT_MOUSE_SCALETOUCHHANDLE_PRESSED = ACT_OFFSET + 307,
                            ACT_MOUSE_SCALETOUCHHANDLE_DRAGGED = ACT_OFFSET + 308,
                            ACT_MOUSE_SCALETOUCHHANDLE_RELEASED = ACT_OFFSET + 309,
            
                            ACT_MOUSE_ROTATE_HANDLE_PRESSED = ACT_OFFSET + 311,
                            ACT_MOUSE_ROTATE_HANDLE_CLICKED = ACT_OFFSET + 312,
                            ACT_MOUSE_ROTATE_HANDLE_RELEASED = ACT_OFFSET + 313,
                            
                            ACT_MOUSE_POSITION_HANDLE_PRESSED = ACT_OFFSET + 314,
                            ACT_MOUSE_POSITION_HANDLE_CLICKED = ACT_OFFSET + 315,
                            ACT_MOUSE_POSITION_HANDLE_RELEASED = ACT_OFFSET + 316,
            
                            ACT_MOUSE_PLANAR_HANDLE_PRESSED = ACT_OFFSET + 317,
                            ACT_MOUSE_PLANAR_HANDLE_CLICKED = ACT_OFFSET + 318,
                            ACT_MOUSE_PLANAR_HANDLE_RELEASED = ACT_OFFSET + 319,
    
                            // light related
                            ACT_LIGHT_ENABLE = ACT_OFFSET + 401,
                            ACT_LIGHT_COLOR_CHANGE = ACT_OFFSET + 402,
                            ACT_LIGHT_POSITION_CHANGE = ACT_OFFSET + 403,
                            ACT_LIGHT_DIRECTION_CHANGE = ACT_OFFSET + 404,
                            ACT_LIGHT_ATTENUATION_CHANGE = ACT_OFFSET + 405,
                            ACT_LIGHT_SPREAD_ANGLE_CHANGE = ACT_OFFSET + 406,
                            ACT_LIGHT_CONCENTRATION_CHANGE = ACT_OFFSET + 407,
            
                            // collision 
                            ACT_MOUSE_DRAGGED_HIT_OTHER_TOOL = ACT_OFFSET + 407,
//IGOR
                            ACT_MOUSE_GUIDEHANDLE_PRESSED = ACT_OFFSET + 504,
                            ACT_MOUSE_GUIDEHANDLE_DRAGGED =  ACT_OFFSET + 505,
                            ACT_MOUSE_GUIDEHANDLE_RELEASED =  ACT_OFFSET + 506,
                            //IGOR
                            //Turn rotation shpere on/off
                            ACT_MOUSE_SWITCH_SPHERE_ON_PRESSED =  ACT_OFFSET + 507,
                            ACT_MOUSE_SWITCH_SPHERE_OFF_PRESSED = ACT_OFFSET + 508,
                            //IGOR
                            ACT_MOUSE_TRANSLATE_TOUCHHANDLE_PRESSED = ACT_OFFSET + 509,
                            ACT_MOUSE_TRANSLATE_TOUCHHANDLE_DRAGGED = ACT_OFFSET + 510,
                            ACT_MOUSE_TRANSLATE_TOUCHHANDLE_RELEASED = ACT_OFFSET + 511,
                            //IGOR
                            ACT_MOUSE_VTOOL_PRESSED = ACT_OFFSET + 512,
                            ACT_MOUSE_VTOOL_DRAGGED =  ACT_OFFSET + 513,
                            ACT_MOUSE_VTOOL_RELEASED =  ACT_OFFSET + 514,
                            ACT_MOUSE_VTOOL_CLICKED =  ACT_OFFSET + 514;

    protected String toolName;                           

    public VToolForm(VirTool virToolRef) {
        super();
        this.virToolRef = virToolRef;
    }
    
    public ActionErrors validate(Session session) {
        return null;
    }

    public void reset(Session session) {
        lGripPt = null;
        vwGripPt = null;
        viewingDistance = 0.0;
        prevX = prevY = currX = currY = 0;
        this.viewSource = null;    
    }

    public int getPrevX() {
        return prevX;
    }

    public void setPrevX(int prevX) {
        this.prevX = prevX;
    }

    public int getPrevY() {
        return prevY;
    }

    public void setPrevY(int prevY) {
        this.prevY = prevY;
    }

    public int getCurrX() {
        return currX;
    }

    public void setCurrX(int currX) {
        this.currX = currX;
    }

    public int getCurrY() {
        return currY;
    }

    public void setCurrY(int currY) {
        this.currY = currY;
    }

//    public void setBehaviorObserverRef(BehaviorObserver behaviorObserver) {
//        this.behaviorObserverRef = behaviorObserver;
//    }
//    public BehaviorObserver getBehaviorObserverRef() {
//        return behaviorObserverRef;
//    }
   
    public void setAction(int toolAction) {
        this.toolAction = toolAction;
    }
    public int getAction() {
        return toolAction;
    }

    public void onVToolDelete(VTool vTool) {
        //check if current reference is same
        // if not escape
//        if(vToolRef != null && vToolRef.equals(vTool)) {
//            _onVToolDelete();
//        }
    }
    
    private void _onVToolDelete() {
        // --> behaviorObserverRef = null;
        // --> vToolRef = null;
        // --> setVToolOperatorsFormRef(null);
        toolAction = ACT_NONE;  
        prevX = prevY = currX = currY = 0;        
    }

    public void setToolName(String string) {
        this.toolName = string;
    }

    public View getViewSource() {
        return viewSource;
    }

    public void setViewSource(View viewSource) {
        this.viewSource = viewSource;
    }

    public Point3d getLGripPt() {
        return lGripPt;
    }

    public void setLGripPt(Point3d lGripPt) {
        this.lGripPt = lGripPt;
    }

    public double getViewingDistance() {
        return viewingDistance;
    }

    public void setViewingDistance(double viewingDistance) {
        this.viewingDistance = viewingDistance;
    }

    public Point3d getVWGripPt() {
        return vwGripPt;
    }

    public void setVWGripPt(Point3d vwGripPt) {
        this.vwGripPt = vwGripPt;
    }

    public VirTool getVirToolRef() {
        return virToolRef;
    }

    public void setVirToolRef(VirTool virToolRef) {
        this.virToolRef = virToolRef;
    }
}
