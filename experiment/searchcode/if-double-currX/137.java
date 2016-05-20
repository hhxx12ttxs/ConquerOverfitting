package org.sgJoe.embedded.tools.planar;

import com.sun.j3d.utils.geometry.Box;
import org.apache.log4j.Logger;

import com.sun.j3d.utils.geometry.ColorCube;
import javax.media.j3d.*;
import javax.vecmath.*;

import org.sgJoe.embedded.exception.SGPluginException;
import org.sgJoe.embedded.graphics.*;
import org.sgJoe.embedded.graphics.*;
import org.sgJoe.embedded.graphics.behaviors.*;
import org.sgJoe.embedded.logic.Session;
import org.sgJoe.embedded.plugin.*;
import org.sgJoe.embedded.tools.*;
import org.sgJoe.embedded.tools.decorators.*;
import org.sgJoe.embedded.tools.interfaces.*;
import org.sgJoe.embedded.utils.GraphicUtils;

/*
 * Descritpion for PlanarVToolPlugin.java
 *
 *
 * @author   $ Author: Aleksandar Babic         $
 * @version  $ Revision:             0.1        $
 * @date     $ Date: April 19, 2006  9:49 AM    $
 */

public class PlanarVToolPlugin extends VToolPlugin {
    
    private static Logger logger = Logger.getLogger(PlanarVToolPlugin.class);

    private double x_factor = .5;
    private double y_factor = .03;
    private Vector3d translation = new Vector3d();
    private Transform3D currXform = new Transform3D();
    private Transform3D transformX = new Transform3D();    
    
    private BranchGroup handleBG = null;
    
    public PlanarVToolPlugin(VirTool virToolRef) {
        super(virToolRef);
    }
    

   public void performAction(Form form, SceneGraphEditor editor, Session session) throws SGPluginException {
        PlanarVToolForm toolForm = (PlanarVToolForm) form;
        
        View viewSource = toolForm.getViewSource();
        
        int prevX = toolForm.getPrevX();
        int prevY = toolForm.getPrevY();
        int currX = toolForm.getCurrX();
        int currY = toolForm.getCurrY();
        
        int action = toolForm.getAction();
        
        PlanarVirTool instance = (PlanarVirTool) toolForm.getVirToolRef();
        PlanarVTool vTool = (PlanarVTool) instance.getVToolRef();
        
        if(action == VToolEventRegistry.getInstance().getEventID("ACT_MOUSE_PRESSED").intValue()) {
            
            /////////////////////////////
            //* projection test
            ///////////////////////////
      
            // projection plane
                   
            Transform3D toolTr = vTool.getLocal2world();
            Point3d local = new Point3d(toolForm.getLGripPt());
            
            // local points that comprise plane are:
            Point3d planePt1 = new Point3d(-1, 1, 1);
            Point3d planePt2 = new Point3d(-1, -1, 1);
            Point3d planePt3 = new Point3d(1, -1, 1);
            
            // apply world transform
            if(local.z == -1.0){ 
                planePt1 = new Point3d(0, 0, -1);
                planePt2 = new Point3d(1, 0, -1);
                planePt3 = new Point3d(0, 1, -1);                      
            } else if (local.x == -1.0) {
                planePt1 = new Point3d(-1, 0, 0);
                planePt2 = new Point3d(-1, 1, 0);
                planePt3 = new Point3d(-1, 0, 1);                
            } else if (local.x == 1.0) {
                planePt1 = new Point3d(1, 0, 0);
                planePt2 = new Point3d(1, 1, 0);
                planePt3 = new Point3d(1, 0, 1);                          
            } else if (local.y == 1.0) {
                planePt1 = new Point3d(0, 1, 0);
                planePt2 = new Point3d(1, 1, 0);
                planePt3 = new Point3d(0, 1, 1);                          
            } else if (local.y == -1.0) {
                planePt1 = new Point3d(0, -1, 0);
                planePt2 = new Point3d(0, -1, 1); 
                planePt3 = new Point3d(1, -1, 0);
            }            
            
            vTool.setPlaneLocalPt1(new Point3d(planePt1));
            vTool.setPlaneLocalPt2(new Point3d(planePt2));
            vTool.setPlaneLocalPt3(new Point3d(planePt3));
            
            toolTr.transform(planePt1);
            toolTr.transform(planePt2);
            toolTr.transform(planePt3);
            
            GraphicUtils.Plane3DParameters pParams = 
                    GraphicUtils.calcPlaneParameters(planePt1, planePt2, planePt3);
            
            GraphicUtils.Line3DParameters lParams = new GraphicUtils.Line3DParameters();
            lParams.origin = toolForm.getEyePosition();
            lParams.direction = toolForm.getLookingDirection();
            
            Point3d intersection = GraphicUtils.intersectLineAndPlane(lParams, pParams, -1);
            
            //vTool.setLGripPt(null);
            vTool.setVWGripPt(intersection);
            
            BehaviorObserver observer = vTool.getVirToolRef().getBhvObserver();          
            
            observer.update(vTool.getVirToolRef().getVToolOperatorsFormRef());            
            observer.update(vTool.getVirToolRef().getVUIToolFormRef()); 
            
        } else if(action == VToolForm.ACT_MOUSE_DRAGGED ||
                  action == VToolEventRegistry.getInstance().getEventID("ACT_MOUSE_DRAGGED_HIT_OTHER_TOOL").intValue()) {
            Point3d prev = vTool.getVWGripPt();
            //Point3d local = toolForm.getLGripPt();
            
            //if(local == null || (local.x == 0.0 && local.y == 0 && local.z == 0))
            //    return;
            
            Transform3D toolTr = vTool.getLocal2world();
            
            //get points that were rememberd for on pressed operation
            Point3d planePt1 = new Point3d(vTool.getPlaneLocalPt1());
            Point3d planePt2 = new Point3d(vTool.getPlaneLocalPt2());
            Point3d planePt3 = new Point3d(vTool.getPlaneLocalPt3());           

            // apply world transform
            
            toolTr.transform(planePt1);
            toolTr.transform(planePt2);
            toolTr.transform(planePt3);
            
            GraphicUtils.Plane3DParameters pParams = 
                    GraphicUtils.calcPlaneParameters(planePt1, planePt2, planePt3);
            
            GraphicUtils.Line3DParameters lParams = new GraphicUtils.Line3DParameters();
            lParams.origin = toolForm.getEyePosition();
            lParams.direction = toolForm.getLookingDirection();
            
            Point3d next = GraphicUtils.intersectLineAndPlane(lParams, pParams, -1);
            
            //---
            translation = new Vector3d();
            transformX = new Transform3D();
            translation.sub(next, prev);

            instance.getTrTG().getTransform(currXform);
            transformX.set(translation);
            currXform.mul(transformX, currXform);  
            instance.getTrTG().setTransform(currXform);

            vTool.setVWGripPt(next);
            
        } else if(action == toolForm.ACT_MOUSE_TRANSLATE_XY) {
            
            Point3d newGripPt = toolForm.getLGripPt();
            Point3d oldGripPt = vTool.getLGripPt();
                       
            if(oldGripPt != null && newGripPt != null) {
  

                translation = new Vector3d();
                transformX = new Transform3D();
                translation.sub(newGripPt, oldGripPt);
                
                instance.getTrTG().getTransform(currXform);
                transformX.set(translation);
                currXform.mul(transformX, currXform);  
                instance.getTrTG().setTransform(currXform);           
               
// very important                
//                vTool.getTransform(currXform);
//                transformX.set(translation);
//                currXform.mul(transformX, currXform);  
//                vTool.setTransform(currXform);                    
// ------------------------------------------------------------------------------               
            } else if(newGripPt == null) {
                // --> System.out.println("toolForm.ACT_MOUSE_PLANARHANDLE_RELEASED");
            
                instance.getToolBaseTG().removeChild(instance.getSliderHandle().getParentBG());
                      
                // --> BehaviorObserver observer = instance.getBhvObserver();

                vTool.setLGripPt(null);
            
                // --> observer.update(vTool.getVirToolRef().getVToolOperatorsFormRef());            
                // --> observer.update(vTool.getVirToolRef().getVUIToolFormRef());                 
            }
            
            vTool.setLGripPt(newGripPt);
                 
        } else if(action == toolForm.ACT_MOUSE_RELEASED) {
           
            BehaviorObserver observer = vTool.getVirToolRef().getBhvObserver();

            vTool.setLGripPt(null);
            
            observer.update(vTool.getVirToolRef().getVToolOperatorsFormRef());            
            observer.update(vTool.getVirToolRef().getVUIToolFormRef());            
            
        }  else if(action == VToolEventRegistry.getInstance().getEventID("ACT_MOUSE_PLANARHANDLE_RELEASED").intValue()) {
            
            // --> System.out.println("toolForm.ACT_MOUSE_PLANARHANDLE_RELEASED");
            if(instance.getToolBaseTG() != null && instance != null) {
                if(instance.getSliderHandle() != null) {
                    if(instance.getSliderHandle().getParentBG() != null) {
                        // ----> important
                        instance.getToolBaseSWG().removeChild(instance.getSliderHandle().getParentBG());                        
                    }
                }
            }
            
                      
            BehaviorObserver observer = instance.getBhvObserver();

            vTool.setLGripPt(null);
            
            observer.update(vTool.getVirToolRef().getVToolOperatorsFormRef());            
            observer.update(vTool.getVirToolRef().getVUIToolFormRef()); 
            
        }  else if(action == VToolEventRegistry.getInstance().getEventID("ACT_MOUSE_PLANARHANDLE_PRESSED").intValue()) { 
            
            // --> System.out.println("toolForm.ACT_MOUSE_PLANARHANDLE_PRESSED");
            
            BehaviorObserver observer = instance.getBhvObserver();
            
            if(instance.getSliderHandle() == null) {
                SliderVToolHandle sliderHandle = new SliderVToolHandle(instance, VToolForm.ACT_MOUSE_TRANSLATE_XY);
                BranchGroup bgHandle = new BranchGroup();
                sliderHandle.setParentBG(bgHandle);
                VToolFactory.setBGCapabilities(bgHandle);
                
                bgHandle.addChild(sliderHandle);
                
                editor.setCapabilities(bgHandle);
                
                // hanndle is set but not yet placed in scene subgraph
                instance.setSliderHandle(sliderHandle);
            
            }
            
//very important            
//            Transform3D toolT3D = new Transform3D();
//            Transform3D handleT3D = new Transform3D();
//            BoundingBox bounds = (BoundingBox) vTool.getBoundingBox();
//            Point3d lowerPt = new Point3d();
//            bounds.getLower(lowerPt);
//            handleT3D.setTranslation(new Vector3d(0.0, 0.0, -lowerPt.z));
//            vTool.getTransform(toolT3D);
//            toolT3D.mul(handleT3D);
//------------------------------------------------------------------------------            
            
            Transform3D trTGT3D = new Transform3D();
            Transform3D handleT3D = new Transform3D();
            // --> Transform3D  newSliderPos = instance.calcTranslateSliderPos();
            BoundingBox bounds = (BoundingBox) vTool.getBoundingBox();
            Point3d lowerPt = new Point3d();
            bounds.getLower(lowerPt);
            handleT3D.setTranslation(new Vector3d(0.0, 0.0, -lowerPt.z));
            instance.getTrTG().getTransform(trTGT3D);
            trTGT3D.mul(handleT3D);            
            
            
            // ---> instance.getSliderHandle().setTransform(toolT3D);
            
            instance.getSliderHandle().setTransform(trTGT3D);
            
            // ----> instance.getToolBaseTG().addChild(instance.getSliderHandle().getParent());
            
            instance.getToolBaseSWG().addChild(instance.getSliderHandle().getParent());
            java.util.BitSet visibleNodes = new java.util.BitSet(instance.getToolBaseSWG().numChildren());
            
            visibleNodes.set(0);
            visibleNodes.set(1);
            
            instance.getToolBaseSWG().setChildMask(visibleNodes);        
            
            vTool.setLGripPt(null);            
           
            observer.update(vTool.getVirToolRef().getVToolOperatorsFormRef());            
            observer.update(vTool.getVirToolRef().getVUIToolFormRef());   
            
        } else if(action == toolForm.ACT_ON_INSTANCE_DELETE) {
            BehaviorObserver observer = vTool.getVirToolRef().getBhvObserver();

            PlanarVUIToolForm guiForm = (PlanarVUIToolForm) vTool.getVirToolRef().getVUIToolFormRef();
            PlanarVToolOperatorsForm operatorsForm = (PlanarVToolOperatorsForm) vTool.getVirToolRef().getVToolOperatorsFormRef();
            
            editor.removeNode(instance.getToolBaseBG());
            editor.getVirToolStack().removeAll(instance);
            editor.getVirToolMap().remove(instance.getInstanceName());
                
            observer.toolRemoved(instance);
            observer.removeGUI(guiForm);
            observer.removeGUI(operatorsForm);  
                
        } else if(action == toolForm.ACT_NONE){
            
        } else {
            System.out.println("[PlanarVToolPlugin] [ " + action + " ] NO SUCH ACTION !!!!");
        }
        
    }    
}


