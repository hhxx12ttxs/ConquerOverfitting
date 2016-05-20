package org.sgJoe.tools.planar;

import com.sun.j3d.utils.geometry.Box;
import org.apache.log4j.Logger;

import com.sun.j3d.utils.geometry.ColorCube;
import javax.media.j3d.*;
import javax.vecmath.*;

import org.sgJoe.exception.SGPluginException;
import org.sgJoe.graphics.*;
import org.sgJoe.graphics.*;
import org.sgJoe.graphics.behaviors.*;
import org.sgJoe.graphics.event.SGEvent;
import org.sgJoe.logic.Session;
import org.sgJoe.plugin.*;
import org.sgJoe.tools.*;
import org.sgJoe.tools.decorators.*;
import org.sgJoe.tools.interfaces.*;

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
        System.out.println("Action: [ " + action + " ] ");
        
        PlanarVirTool instance = (PlanarVirTool) toolForm.getVirToolRef();
        PlanarVTool vTool = (PlanarVTool) instance.getVToolRef();
        
        if(action == toolForm.ACT_MOUSE_PRESSED) {

//            System.out.println("Mouse pressed LPT: " + lGripPt);
//
//            Transform3D lvw = new Transform3D();
//            vTool.getLocalToVworld(lvw);
//            lvw.transform(lGripPt);
//            
//            System.out.println("Mouse Pressed L -> VW: " + lGripPt);        
//            
//            System.out.println("Mouse pressed VWPT: " + vwGripPt);
//            
//            lvw.invert();
//            lvw.transform(vwGripPt);
//            
//            System.out.println("Mouse pressed VW -> L: " + vwGripPt);
//            
//            Transform3D lvwView = new Transform3D();
//            viewSource.getCanvas3D(0).getVworldToImagePlate(lvwView);
//
//            Point3d viewPt = new Point3d(currX, currY, 0.0);
//            
//            lvwView.transform(viewPt);
//            System.out.println("Mouse pressed VIEW VW: " + viewPt);
//            
//            lvw.transform(viewPt);
//            System.out.println("Mouse pressed VIEW -> L: " + viewPt);
            
            //vTool.setLGripPt(toolForm.getLGripPt());

            vTool.setLGripPt(null);
            
            BehaviorObserver observer = vTool.getVirToolRef().getBhvObserver();          
            
            observer.update(vTool.getVirToolRef().getVToolOperatorsFormRef());            
            observer.update(vTool.getVirToolRef().getVUIToolFormRef()); 
            
        } else if(action == toolForm.ACT_MOUSE_TRANSLATE_XY) {
            
            Point3d newGripPt = toolForm.getLGripPt();
            Point3d oldGripPt = vTool.getLGripPt();
                       
            if(oldGripPt != null && newGripPt != null) {
                //int dX = currX - prevX;
                //int dY = currY - prevY;

                translation = new Vector3d();
                transformX = new Transform3D();
                translation.sub(newGripPt, oldGripPt);
                
                vTool.getTransform(currXform);
                transformX.set(translation);
                currXform.mul(transformX, currXform);  
                vTool.setTransform(currXform);                    
               
            } else if(newGripPt == null) {
                // --> System.out.println("toolForm.ACT_MOUSE_PLANARHANDLE_RELEASED");
            
                instance.getToolBaseTG().removeChild(instance.getSliderHandle().getParentBG());
                      
                // --> BehaviorObserver observer = instance.getBhvObserver();

                vTool.setLGripPt(null);
            
                // --> observer.update(vTool.getVirToolRef().getVToolOperatorsFormRef());            
                // --> observer.update(vTool.getVirToolRef().getVUIToolFormRef());                 
            }
            
             //laki
               ((PlanarVTool)getVirToolRef().getVToolRef()).getPDispatcher()
                     .publish((PlanarVTool)getVirToolRef().getVToolRef(),SGEvent.EVT_MOVE_TOOL);
            
            vTool.setLGripPt(newGripPt);
                 
        } else if(action == toolForm.ACT_MOUSE_RELEASED) {
           
            BehaviorObserver observer = vTool.getVirToolRef().getBhvObserver();

            vTool.setLGripPt(null);
            
            observer.update(vTool.getVirToolRef().getVToolOperatorsFormRef());            
            observer.update(vTool.getVirToolRef().getVUIToolFormRef());            
            
        }  else if(action == toolForm.ACT_MOUSE_PLANARHANDLE_RELEASED) {
            
            // --> System.out.println("toolForm.ACT_MOUSE_PLANARHANDLE_RELEASED");
            
            instance.getToolBaseTG().removeChild(instance.getSliderHandle().getParentBG());
                      
            BehaviorObserver observer = instance.getBhvObserver();

            vTool.setLGripPt(null);
            
            observer.update(vTool.getVirToolRef().getVToolOperatorsFormRef());            
            observer.update(vTool.getVirToolRef().getVUIToolFormRef()); 
            
             //laki
               ((PlanarVTool)getVirToolRef().getVToolRef()).getPDispatcher()
                     .publish((PlanarVTool)getVirToolRef().getVToolRef(),SGEvent.EVT_STOP_MOVE_TOOL);
              
            
          
            
        }  else if(action == toolForm.ACT_MOUSE_PLANARHANDLE_PRESSED) { 
            
            //laki
          // if(vTool.getLGripPt()!=null && vTool.getVWGripPt()!=null)
            ((PlanarVTool)getVirToolRef().getVToolRef()).getPDispatcher()
                     .publish((PlanarVTool)getVirToolRef().getVToolRef(),SGEvent.EVT_START_MOVE_TOOL);
            
            // --> System.out.println("toolForm.ACT_MOUSE_PLANARHANDLE_PRESSED");
            
            BehaviorObserver observer = instance.getBhvObserver();
            
            if(instance.getSliderHandle() == null) {
                SliderVToolHandle sliderHandle = new SliderVToolHandle(instance, VToolForm.ACT_MOUSE_TRANSLATE_XY);
                BranchGroup bgHandle = new BranchGroup();
                sliderHandle.setParentBG(bgHandle);
                VToolFactory.setBGCapabilities(bgHandle);
                
                bgHandle.addChild(sliderHandle);
                
                editor.setCapabilities(bgHandle);
                
                instance.setSliderHandle(sliderHandle);
            
            }
            Transform3D toolT3D = new Transform3D();
            Transform3D handleT3D = new Transform3D();
            BoundingBox bounds = (BoundingBox) vTool.getBoundingBox();
            Point3d lowerPt = new Point3d();
            bounds.getLower(lowerPt);
            handleT3D.setTranslation(new Vector3d(0.0, 0.0, -lowerPt.z));
            vTool.getTransform(toolT3D);
            toolT3D.mul(handleT3D);
            
            instance.getSliderHandle().setTransform(toolT3D);
            
            instance.getToolBaseTG().addChild(instance.getSliderHandle().getParent());
            
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
                
            observer.removeGUI(instance);  
                
        } else if(action == toolForm.ACT_NONE){
            
        } else {
            System.out.println("[PlanarVToolPlugin] [ " + action + " ] NO SUCH ACTION !!!!");
        }
        
    }    
}


