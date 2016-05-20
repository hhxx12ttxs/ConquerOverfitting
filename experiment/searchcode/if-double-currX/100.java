package org.sgJoe.embedded.tools.sphere;

import javax.media.j3d.*;

import javax.vecmath.*;

import org.apache.log4j.Logger;
import org.sgJoe.embedded.exception.SGPluginException;
import org.sgJoe.embedded.graphics.SceneGraphEditor;
import org.sgJoe.embedded.graphics.behaviors.BehaviorObserver;
import org.sgJoe.embedded.logic.Session;
import org.sgJoe.embedded.plugin.Form;
import org.sgJoe.embedded.tools.VToolEventRegistry;
import org.sgJoe.embedded.tools.interfaces.*;
import org.sgJoe.embedded.utils.GraphicUtils;


/*
 * Descritpion for SphereVToolPlugin.java
 *
 *
 * @author   $ Author: Aleksandar Babic  $
 * @version  $ Revision:             0.1 $
 * @date     $ Date: April 28, 2006  9:37 PM  $
 */

public class SphereVToolPlugin extends VToolPlugin {
    
    private static Logger logger = Logger.getLogger(SphereVToolPlugin.class);
   
    private Vector3d translation = new Vector3d();
    private Transform3D currXform = new Transform3D();
    private Transform3D transformX = new Transform3D();    
    private Transform3D transformY = new Transform3D();
    
    
    public SphereVToolPlugin(VirTool virToolRef) {
        super(virToolRef);
    }

   public void performAction(Form form, SceneGraphEditor editor, Session session) throws SGPluginException {
        SphereVToolForm toolForm = (SphereVToolForm) form;
        
        View viewSource = toolForm.getViewSource();
        
        int prevX = toolForm.getPrevX();
        int prevY = toolForm.getPrevY();
        int currX = toolForm.getCurrX();
        int currY = toolForm.getCurrY();
        
        int action = toolForm.getAction();
        
        SphereVirTool instance = (SphereVirTool) toolForm.getVirToolRef();
        SphereVTool vTool = (SphereVTool) instance.getVToolRef();
        
        if(action == VToolEventRegistry.getInstance().getEventID("ACT_MOUSE_PRESSED").intValue()) {
            
            // set data for projection
            Transform3D toolTr = vTool.getLocal2world();
            Point3d local = new Point3d(toolForm.getLGripPt());
            
            GraphicUtils.Sphere3DParameters sParams = new GraphicUtils.Sphere3DParameters();
            sParams.origin = new Point3d(0.0, 0.0, 0.0);
            sParams.radius = 2.5;
            toolTr.transform(sParams.origin);
           
            GraphicUtils.Line3DParameters lParams = new GraphicUtils.Line3DParameters();
            lParams.origin = toolForm.getEyePosition();
            lParams.direction = toolForm.getLookingDirection();
            
            Point3d[] intersection = GraphicUtils.intersectLineAndSphere(lParams, sParams);
            
            if(null == intersection[0] && null == intersection[1]) {
                // no intersection
                vTool.setLGripPt(null);
                vTool.setVWGripPt(null);
            } else if(null != intersection[0] && null == intersection[1]) {
                // one intersection point - tangenta
                vTool.setVWGripPt(intersection[0]);
            } else {
                // two intersection points - i'm taking the closest one (simple case)
                if(lParams.origin.distance(intersection[0]) < lParams.origin.distance(intersection[1])) {
                    vTool.setVWGripPt(intersection[0]);
                } else {
                    vTool.setVWGripPt(intersection[1]);
                }
            }
                    
            
//            vTool.setLGripPt(null);
//            vTool.setVWGripPt(intersection[0]);

            
//            vTool.setLGripPt(null);
//            java.util.BitSet visibleNodes = new java.util.BitSet(instance.getToolBaseSWG().numChildren());
//            int childNum = instance.getToolBaseSWG().numChildren();
//            for(int i = 0; i < childNum ; ++i) {
//                visibleNodes.set(i);
//            }
//
//            instance.getToolBaseSWG().setChildMask(visibleNodes);   
            
            
            
            BehaviorObserver observer = vTool.getVirToolRef().getBhvObserver();          
            
            observer.update(vTool.getVirToolRef().getVToolOperatorsFormRef());            
            observer.update(vTool.getVirToolRef().getVUIToolFormRef());             
                                
            // --> System.out.println("toolForm.ACT_MOUSE_PRESSED");
            
            if(null != vTool.getCanvas3dPt()) {
                vTool.setPrevCanvas3dPt(vTool.getCanvas3dPt());
            }
            
        } 
//        else if(action == VToolEventRegistry.getInstance().getEventID("ACT_MOUSE_DRAGGED_HIT_OTHER_TOOL").intValue()) {
//            
//            // --> System.out.println("toolForm.ACT_MOUSE_DRAGGED");
//            
//            
//        } 
        else if(action == toolForm.ACT_MOUSE_DRAGGED ||
                action == VToolEventRegistry.getInstance().getEventID("ACT_MOUSE_DRAGGED_HIT_OTHER_TOOL").intValue()) {
            
            // --> System.out.println("toolForm.ACT_MOUSE_DRAGGED");
            Point3d oldPt = null;
            Point3d newPt = null;
            
                      
            if(null != vTool.getVWGripPt()) {
                oldPt = new Point3d(vTool.getVWGripPt());
            }
            
            //----
            Transform3D toolTr = vTool.getLocal2world();
                        
            GraphicUtils.Sphere3DParameters sParams = new GraphicUtils.Sphere3DParameters();
            sParams.origin = new Point3d(0.0, 0.0, 0.0);
            sParams.radius = 2.5;
            toolTr.transform(sParams.origin);
           
            GraphicUtils.Line3DParameters lParams = new GraphicUtils.Line3DParameters();
            lParams.origin = toolForm.getEyePosition();
            lParams.direction = toolForm.getLookingDirection();
            
            Point3d[] intersection = GraphicUtils.intersectLineAndSphere(lParams, sParams);
            
            if(null == intersection[0] && null == intersection[1]) {
                // no intersection
                vTool.setLGripPt(null);
                vTool.setVWGripPt(null);
                
            } else if(null != intersection[0] && null == intersection[1]) {
                // one intersection point - tangenta
                newPt = new Point3d(intersection[0]);
            } else {
                // two intersection points - i'm taking the closest one (simple case)
                if(lParams.origin.distance(intersection[0]) < lParams.origin.distance(intersection[1])) {
                    newPt = new Point3d(intersection[0]);
                } else {
                    newPt = new Point3d(intersection[1]);
                }
            }            
            //----
            
            Point3d oldGripPt = vTool.getVWGripPt();
                       
            if(oldPt != null && newPt != null) {

                Vector3d oldVector = new Vector3d(oldPt.x, oldPt.y, oldPt.z);
                Vector3d newVector = new Vector3d(newPt.x, newPt.y, newPt.z);    

                Vector3d axisVector = new Vector3d();
                axisVector.cross(oldVector, newVector);
                
                double angle = oldVector.angle(newVector);
                
                AxisAngle4d aa = new AxisAngle4d(axisVector, angle);
                
                // remember rotation axis ...
                
                vTool.setRotAxisVector(axisVector);
                axisVector.normalize();
                                       
                vTool.getTransform(currXform);
                transformX = new Transform3D();
                transformX.set(aa);
                currXform.mul(transformX, currXform);  
                vTool.setTransform(currXform);                    
           
            } 
            else if( (oldPt != null && newPt == null) || (oldPt == null && newPt != null) ) {
               // -->  System.out.println("oldGripPt != null && newGripPt == null");
                int dx, dy;
                dx = currX - prevX;
                dy = currY - prevY;

                double angle = 0.05;
                
                Vector3d axisViewerVector = new Vector3d();
                Vector3d oldViewerVector = 
                        new Vector3d(vTool.getCanvas3dPt().x, vTool.getCanvas3dPt().y, vTool.getCanvas3dPt().z);
                Vector3d newViewerVector = 
                        new Vector3d(vTool.getCanvas3dPt3().x, vTool.getCanvas3dPt3().y, vTool.getCanvas3dPt3().z);    
                
                axisViewerVector.cross(oldViewerVector, newViewerVector);
                
                
                Vector3d axisVector = new Vector3d(vTool.getRotAxisVector());

               if(axisViewerVector.dot(axisVector) > 0) {
                    if(dx < 0) {
                        angle = -1 * angle;
                    }
                } else {
                    if(dx > 0) {
                        angle = -1 * angle;
                    }       
                }
                
                AxisAngle4d aa = new AxisAngle4d(axisVector, angle);
                        
                vTool.getTransform(currXform);
                transformX = new Transform3D();
                transformX.set(aa);
                currXform.mul(transformX, currXform);  
                vTool.setTransform(currXform);                 
                
            } else if(oldPt == null && newPt == null){
                int dx, dy;
                dx = currX - prevX;
                dy = currY - prevY; 
                
                double angle = 0.05;
   
                Vector3d axisViewerVector = new Vector3d();
                Vector3d oldViewerVector = 
                        new Vector3d(vTool.getCanvas3dPt().x, vTool.getCanvas3dPt().y, vTool.getCanvas3dPt().z);
                Vector3d newViewerVector = 
                        new Vector3d(vTool.getCanvas3dPt3().x, vTool.getCanvas3dPt3().y, vTool.getCanvas3dPt3().z);    
                
                axisViewerVector.cross(oldViewerVector, newViewerVector);
                
                Vector3d axisVector = new Vector3d(vTool.getRotAxisVector());
                
                if(axisViewerVector.dot(axisVector) > 0) {
                    if(dx < 0) {
                        angle = -1 * angle;
                    }
                } else {
                    if(dx > 0) {
                        angle = -1 * angle;
                    }       
                }

                AxisAngle4d aa = new AxisAngle4d(axisVector, angle);
                        
                vTool.getTransform(currXform);
                transformX = new Transform3D();
                transformX.set(aa);
                currXform.mul(transformX, currXform);  
                vTool.setTransform(currXform);                
            }          
           
            vTool.setVWGripPt(newPt);
            
        } else if(action == toolForm.ACT_MOUSE_RELEASED ||
                    action == toolForm.ACT_MOUSE_CLICKED) {
            
            // --> System.out.println("toolForm.ACT_MOUSE_RELEASED");
            
            vTool.setLGripPt(null);
            vTool.setVWGripPt(null);
            java.util.BitSet visibleNodes = new java.util.BitSet(instance.getToolBaseSWG().numChildren());
            visibleNodes.set(0);
            instance.getToolBaseSWG().setChildMask(visibleNodes);                        
            
        } else if(action == toolForm.ACT_MOUSE_ROTATE) {
            
            // --> System.out.println("toolForm.ACT_MOUSE_ROTATE");
            
            Point3d newGripPt = toolForm.getLGripPt();
            Point3d oldGripPt = vTool.getLGripPt();
                       
            if(oldGripPt != null && newGripPt != null) {

            Vector3d oldVector = new Vector3d(oldGripPt.x, oldGripPt.y, oldGripPt.z);
            Vector3d newVector = new Vector3d(newGripPt.x, newGripPt.y, newGripPt.z);    
            
            Vector3d axisVector = new Vector3d();
            axisVector.cross(oldVector, newVector);
                
                double angle = oldVector.angle(newVector);
                
                AxisAngle4d aa = new AxisAngle4d(axisVector, angle);
                        
                vTool.getTransform(currXform);
                transformX = new Transform3D();
                transformX.set(aa);
                currXform.mul(transformX, currXform);  
                vTool.setTransform(currXform);                    
           
            } else if( (oldGripPt != null && newGripPt == null) || (oldGripPt == null && newGripPt == null) ) {
               // -->  System.out.println("oldGripPt != null && newGripPt == null");
                int dx, dy;
                dx = currX - prevX;
                dy = currY - prevY;

                double x_angle = dy * 0.05;
                double y_angle = dx * 0.05;
                
                transformX = new Transform3D();
                transformY = new Transform3D();
                
                vTool.getTransform(currXform);
                
                transformX.rotX(x_angle);
                transformY.rotY(y_angle);
                
                currXform.mul(transformX, currXform);
                currXform.mul(transformY, currXform);
                
                vTool.setTransform(currXform);  
            }           
           
            vTool.setLGripPt(newGripPt);
            
        } else if(action == toolForm.ACT_ON_INSTANCE_DELETE) {
            
            super.OnInstanceDelete(toolForm, editor);
            
        } else {
            
            System.out.println("[ " + action + " ] NO SUCH ACTION !!!!");
            
        }
        
    }
    
}

