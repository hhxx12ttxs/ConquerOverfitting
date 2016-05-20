package embedded.org.sgJoe.tools.sphere;

import javax.media.j3d.*;

import javax.vecmath.*;

import org.apache.log4j.Logger;
import embedded.org.sgJoe.exception.SGPluginException;
import embedded.org.sgJoe.graphics.SceneGraphEditor;
import embedded.org.sgJoe.graphics.behaviors.BehaviorObserver;
import org.sgJoe.logic.Session;
import embedded.org.sgJoe.plugin.Form;
import embedded.org.sgJoe.tools.interfaces.*;
import org.sgJoe.logic.Session;


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
        
        if(action == toolForm.ACT_MOUSE_PRESSED) {
            
            vTool.setLGripPt(null);
            
            instance.adjustRotateHandlePosition();
            
            instance.setRotateHandleVisibility(true);
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
            
        } else if(action == toolForm.ACT_MOUSE_DRAGGED) {
            
        } else if(action == toolForm.ACT_MOUSE_RELEASED ||
                    action == toolForm.ACT_MOUSE_CLICKED) {
            
            // --> System.out.println("toolForm.ACT_MOUSE_RELEASED");
            
            vTool.setLGripPt(null);
            
            instance.setRotateHandleVisibility(false);
            
//            java.util.BitSet visibleNodes = new java.util.BitSet(instance.getToolBaseSWG().numChildren());
//            visibleNodes.set(0);
//            instance.getToolBaseSWG().setChildMask(visibleNodes);                        
            
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
                        
//                vTool.getTransform(currXform);
//                transformX = new Transform3D();
//                transformX.set(aa);
//                currXform.mul(transformX, currXform);  
//                vTool.setTransform(currXform);                    
                
                instance.getRotTG().getTransform(currXform);
                transformX = new Transform3D();
                transformX.set(aa);
                currXform.mul(transformX, currXform);
                instance.getRotTG().setTransform(currXform);                                
           
            } else if( (oldGripPt != null && newGripPt == null) || (oldGripPt == null && newGripPt == null) ) {
               // -->  System.out.println("oldGripPt != null && newGripPt == null");
                int dx, dy;
                dx = currX - prevX;
                dy = currY - prevY;

                double x_angle = dy * 0.05;
                double y_angle = dx * 0.05;
                
                transformX = new Transform3D();
                transformY = new Transform3D();
                
//                vTool.getTransform(currXform);                
//                transformX.rotX(x_angle);
//                transformY.rotY(y_angle);                
//                currXform.mul(transformX, currXform);
//                currXform.mul(transformY, currXform);                
//                vTool.setTransform(currXform);  
                
                instance.getRotTG().getTransform(currXform);
                transformX.rotX(x_angle);
                transformY.rotY(y_angle);        
                currXform.mul(transformX, currXform);
                currXform.mul(transformY, currXform);         
                instance.getRotTG().setTransform(currXform);                      
            }           
           
            vTool.setLGripPt(newGripPt);
            
        } else if(action == toolForm.ACT_ON_INSTANCE_DELETE) {
            
            super.OnInstanceDelete(toolForm, editor);
            
        } else {
            
            System.out.println("[ " + action + " ] NO SUCH ACTION !!!!");
            
        }
        
    }
    
}

