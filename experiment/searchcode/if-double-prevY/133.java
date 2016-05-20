package embedded.org.sgJoe.tools.planar;

import org.apache.log4j.Logger;

import javax.media.j3d.*;
import javax.vecmath.*;

import embedded.org.sgJoe.exception.SGPluginException;
import embedded.org.sgJoe.graphics.*;
import embedded.org.sgJoe.graphics.behaviors.*;
import org.sgJoe.logic.Session;
import embedded.org.sgJoe.plugin.*;
import embedded.org.sgJoe.tools.interfaces.*;

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
        
        if(action == toolForm.ACT_MOUSE_PRESSED) {
            
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
            
                instance.setSliderHandleVisibility(false);
                      
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
            
        }  else if(action == VToolForm.ACT_MOUSE_PLANARHANDLE_RELEASED ||
                   action == VToolForm.ACT_MOUSE_DRAGGED_HIT_OTHER_TOOL) {
            
            // --> System.out.println("toolForm.ACT_MOUSE_PLANARHANDLE_RELEASED");
            // ---> if(instance.getToolBaseTG() != null && instance != null) {
                // ---> if(instance.getSliderHandle() != null) {
                    // ---> if(instance.getSliderHandle().getParentBG() != null) {
                        // ----> important
                        // ----> instance.getToolBaseSWG().removeChild(instance.getSliderHandle().getParentBG()); 
                        instance.setSliderHandleVisibility(false);
                    // ---> }
                // ---> }
            // ---> }
            
                      
            BehaviorObserver observer = instance.getBhvObserver();

            vTool.setLGripPt(null);
            
            observer.update(vTool.getVirToolRef().getVToolOperatorsFormRef());            
            observer.update(vTool.getVirToolRef().getVUIToolFormRef()); 
            
        }  else if(action == toolForm.ACT_MOUSE_PLANARHANDLE_PRESSED) { 
            
            // --> System.out.println("toolForm.ACT_MOUSE_PLANARHANDLE_PRESSED");
            
            BehaviorObserver observer = instance.getBhvObserver();
            
//            if(instance.getSliderHandle() == null) {
//                SliderVToolHandle sliderHandle = new SliderVToolHandle(instance, VToolForm.ACT_MOUSE_TRANSLATE_XY);
//                BranchGroup bgHandle = new BranchGroup();
//                sliderHandle.setParentBG(bgHandle);
//                VToolFactory.setBGCapabilities(bgHandle);
//                
//                bgHandle.addChild(sliderHandle);
//                
//                editor.setCapabilities(bgHandle);
//                
//                // hanndle is set but not yet placed in scene subgraph
//                instance.setSliderHandle(sliderHandle);
//            
//            }
            
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
            
//            Transform3D trTGT3D = new Transform3D();
//            Transform3D handleT3D = new Transform3D();
//            
//            instance.adjustSliderHandlePosition();
//            
//            BoundingBox bounds = (BoundingBox) vTool.getBoundingBox();
//            Point3d lowerPt = new Point3d();
//            bounds.getLower(lowerPt);
//            handleT3D.setTranslation(new Vector3d(0.0, 0.0, -lowerPt.z));
//            instance.getTrTG().getTransform(trTGT3D);
//            trTGT3D.mul(handleT3D);            
//            
//            
//            // ---> instance.getSliderHandle().setTransform(toolT3D);
//            
//            instance.getSliderHandle().setTransform(trTGT3D);
//            
//            // ----> instance.getToolBaseTG().addChild(instance.getSliderHandle().getParent());
            
            instance.adjustSliderHandlePosition();
            
            instance.setSliderHandleVisibility(true);
            
//            instance.getToolBaseSWG().addChild(instance.getSliderHandle().getParent());
//            java.util.BitSet visibleNodes = new java.util.BitSet(instance.getToolBaseSWG().numChildren());
//            
//            visibleNodes.set(0);
//            visibleNodes.set(1);
//            
//            instance.getToolBaseSWG().setChildMask(visibleNodes);        
            
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


