package embedded.org.sgJoe.tools.scale;

import javax.media.j3d.*;

import javax.vecmath.*;

import org.apache.log4j.Logger;
import embedded.org.sgJoe.exception.SGPluginException;
import embedded.org.sgJoe.graphics.SceneGraphEditor;
import embedded.org.sgJoe.graphics.behaviors.BehaviorObserver;
import org.sgJoe.logic.Session;
import embedded.org.sgJoe.plugin.Form;
import embedded.org.sgJoe.tools.interfaces.*;

/*
 * Descritpion for ScaleVToolPlugin.java
 *
 *
 * @author   $ Author: Aleksandar Babic  $
 * @version  $ Revision:             0.1 $
 * @date     $ Date: May 4, 2006  11:54 PM  $
 */

public class ScaleVToolPlugin extends VToolPlugin {
    
    private static Logger logger = Logger.getLogger(ScaleVToolPlugin.class);
   
    private Vector3d translation = new Vector3d();
    private Transform3D currXform = new Transform3D();
    private Transform3D transformX = new Transform3D();    
    private Transform3D transformY = new Transform3D();
        
    public ScaleVToolPlugin(VirTool virToolRef) {
        super(virToolRef);
    }

   public void performAction(Form form, SceneGraphEditor editor, Session session) throws SGPluginException {
        ScaleVToolForm toolForm = (ScaleVToolForm) form;
        
        View viewSource = toolForm.getViewSource();
        
        int prevX = toolForm.getPrevX();
        int prevY = toolForm.getPrevY();
        int currX = toolForm.getCurrX();
        int currY = toolForm.getCurrY();
        
        int action = toolForm.getAction();
        
        ScaleVirTool instance = (ScaleVirTool) toolForm.getVirToolRef();
        ScaleVTool vTool = (ScaleVTool) instance.getVToolRef();
        
        if(action == VToolForm.ACT_MOUSE_PRESSED) {
            BehaviorObserver observer = vTool.getVirToolRef().getBhvObserver();          
            
            observer.update(vTool.getVirToolRef().getVToolOperatorsFormRef());            
            observer.update(vTool.getVirToolRef().getVUIToolFormRef());             
        } else if(action == VToolForm.ACT_MOUSE_SCALETOUCHHANDLE_PRESSED) {//toolForm.ACT_MOUSE_PRESSED) {
        
            if(toolForm.getLGripPt().z < 0) {
                System.out.println("back plane");
                return;
            }
            
        BoundingSphere toolBounds = (BoundingSphere) vTool.getBounds();
        
        instance.getTrTG().getTransform(currXform);
        
        Transform3D temp = new Transform3D();
        double Z = Math.sqrt(Math.pow(toolBounds.getRadius(), 2) / 3.0);
        temp.setTranslation(new Vector3d(0.0, 0.0, Z + 0.05d));
        currXform.mul(temp);
        instance.getHFront().setTransform(currXform);
        
        instance.getTrTG().getTransform(currXform);
        
//        temp = new Transform3D();
//        temp.setTranslation(new Vector3d(0.0, 0.0, - Z - 0.001));
//        currXform.mul(temp);
//        instance.getHBack().setTransform(currXform);
        
        vTool.getTransform(currXform);
        Point3d fAnchor = new Point3d(instance.getAnchorPt()); // new Point3d(-1.0, -1.0, -1.0);
        currXform.transform(fAnchor);
        instance.getHFront().setAnchorPt(fAnchor);
        instance.getHBack().setAnchorPt(fAnchor);
        
            vTool.setLGripPt(null);
            java.util.BitSet visibleNodes = new java.util.BitSet(instance.getToolBaseSWG().numChildren());
            visibleNodes.set(0);
            visibleNodes.set(1);
            //visibleNodes.set(2);
            instance.getToolBaseSWG().setChildMask(visibleNodes);   
            
            BehaviorObserver observer = vTool.getVirToolRef().getBhvObserver();          
            
            observer.update(vTool.getVirToolRef().getVToolOperatorsFormRef());            
            observer.update(vTool.getVirToolRef().getVUIToolFormRef());             
        } else if(action == toolForm.ACT_MOUSE_DRAGGED) {
            
        } else if(action == VToolForm.ACT_MOUSE_SCALEHANDLE_PRESSED) {
            
        } else if(action == VToolForm.ACT_MOUSE_SCALEHANDLE_DRAGGED) {
            
            Point3d newGripPt = toolForm.getLGripPt();
            Point3d oldGripPt = vTool.getLGripPt();
            
            Point3d anchorPt = instance.getHFront().getAnchorPt();
            
            if(oldGripPt != null && newGripPt != null) {
                
                
                Vector3d newVec = new Vector3d();
                newVec.sub(newGripPt, anchorPt);
                
                Vector3d oldVec = new Vector3d();
                oldVec.sub(oldGripPt, anchorPt);

                /////////////////////////
                Transform3D currTrTG = new Transform3D();
                instance.getTrTG().getTransform(currTrTG);
                translation = new Vector3d(0.0, 0.0, 0.0);
                transformX = new Transform3D();
                
                if(instance.getScaleModus() == ScaleTouchHandle.MOD_SCALE_X) {
                    translation.x = (newGripPt.x - oldGripPt.x) / 2;
                } else if(instance.getScaleModus() == ScaleTouchHandle.MOD_SCALE_Y) {
                    translation.y = (newGripPt.y - oldGripPt.y) / 2;
                } else if(instance.getScaleModus() == ScaleTouchHandle.MOD_SCALE_XY) {
                    translation.x = (newGripPt.x - oldGripPt.x) / 2;
                    translation.y = (newGripPt.y - oldGripPt.y) / 2;
                }
                
                transformX.setTranslation(translation);
                currTrTG.mul(transformX, currTrTG);  
                 
                instance.getTrTG().setTransform(currTrTG); 
                
                //////////////////////////////////////////////////////////
 
                double scaleX = newVec.x / oldVec.x;
                double scaleY = newVec.y / oldVec.y;
             
                translation = new Vector3d(1.0, 1.0, 1.0);
                transformX = new Transform3D();
                
                if(instance.getScaleModus() == ScaleTouchHandle.MOD_SCALE_X) {
                    translation.x = scaleX;
                } else if(instance.getScaleModus() == ScaleTouchHandle.MOD_SCALE_Y) {
                    translation.y = scaleY;
                } else if(instance.getScaleModus() == ScaleTouchHandle.MOD_SCALE_XY) {
                    translation.x = scaleX;
                    translation.y = scaleY;
                } 
                
                vTool.getTransform(currXform);
                
                transformX.setScale(translation);
                currXform.mul(transformX, currXform);
                vTool.setTransform(currXform);      
                
                // adjust touchsensor position
                instance.getUppright().getTransform(currXform);
                transformX = new Transform3D();
                translation = new Vector3d(0.0, 0.0, 0.0);
                
                if(instance.getScaleModus() == ScaleTouchHandle.MOD_SCALE_X) {
                    translation.x = (newGripPt.x - oldGripPt.x) / 2;
                } else if(instance.getScaleModus() == ScaleTouchHandle.MOD_SCALE_Y) {
                    if(anchorPt.y > 0) {
                        translation.y = -(newGripPt.y - oldGripPt.y) / 2;
                    } else {
                        translation.y = (newGripPt.y - oldGripPt.y) / 2;
                    }                    
                } else if(instance.getScaleModus() == ScaleTouchHandle.MOD_SCALE_XY) {
                    translation.x = (newGripPt.x - oldGripPt.x) / 2;
                    if(anchorPt.y > 0) {
                        translation.y = -(newGripPt.y - oldGripPt.y) / 2;
                    } else {
                        translation.y = (newGripPt.y - oldGripPt.y) / 2;
                    }
                }             
                
                           
                instance.getUppright().getTransform(currXform);
                transformX.setTranslation(translation);
                currXform.mul(transformX);
                instance.getUppright().setTransform(currXform);
                
                ///////////////////////////////////////////////////
                
                instance.getMidright().getTransform(currXform);
                transformX = new Transform3D();
                translation = new Vector3d(0.0, 0.0, 0.0);
                if(instance.getScaleModus() == ScaleTouchHandle.MOD_SCALE_X ||
                        instance.getScaleModus() == ScaleTouchHandle.MOD_SCALE_XY) {
                    translation.x = (newGripPt.x - oldGripPt.x) / 2;
                } else if(instance.getScaleModus() == ScaleTouchHandle.MOD_SCALE_Y) {
                    //translation.y = (newGripPt.y - oldGripPt.y) / 2;;
                }
             
                instance.getMidright().getTransform(currXform);
                transformX.setTranslation(translation);
                currXform.mul(transformX);
                instance.getMidright().setTransform(currXform);
                
                ///////////////////////////////////////////////////
                
                instance.getDwnRight().getTransform(currXform);
                transformX = new Transform3D();
                translation = new Vector3d(0.0, 0.0, 0.0);
                
                if(instance.getScaleModus() == ScaleTouchHandle.MOD_SCALE_X) {
                    translation.x = (newGripPt.x - oldGripPt.x) / 2;
                } else if (instance.getScaleModus() == ScaleTouchHandle.MOD_SCALE_Y) {
                    if(anchorPt.y > 0) {
                        translation.y = (newGripPt.y - oldGripPt.y) / 2;
                    } else {
                        translation.y = -(newGripPt.y - oldGripPt.y) / 2;
                    }
                } else if(instance.getScaleModus() == ScaleTouchHandle.MOD_SCALE_XY) {
                    translation.x = (newGripPt.x - oldGripPt.x) / 2;
                    if(anchorPt.y > 0) {
                        translation.y = (newGripPt.y - oldGripPt.y) / 2;
                    } else {
                        translation.y = -(newGripPt.y - oldGripPt.y) / 2;
                    }
                    
                } 
             
                instance.getDwnRight().getTransform(currXform);
                transformX.setTranslation(translation);
                currXform.mul(transformX);
                instance.getDwnRight().setTransform(currXform);
                
                //////////////////////////////////////////////////////////////
                
                ///////////////////////////////////////////////////
                                
                instance.getMidUp().getTransform(currXform);
                transformX = new Transform3D();
                translation = new Vector3d(0.0, 0.0, 0.0);
                if(instance.getScaleModus() == ScaleTouchHandle.MOD_SCALE_X) {
                    //translation.x = (newGripPt.x - oldGripPt.x) / 2;
                } else if(instance.getScaleModus() == ScaleTouchHandle.MOD_SCALE_Y ||
                            instance.getScaleModus() == ScaleTouchHandle.MOD_SCALE_XY) {
                    if(anchorPt.y > 0) {
                        translation.y = -(newGripPt.y - oldGripPt.y) / 2;
                    } else {
                        translation.y = (newGripPt.y - oldGripPt.y) / 2;
                    }
                    
                }                
             
                instance.getMidUp().getTransform(currXform);
                transformX.setTranslation(translation);
                currXform.mul(transformX);
                instance.getMidUp().setTransform(currXform);
                
                ////////////////////////////////////////////////////////////////////
            } 
           
            vTool.setLGripPt(newGripPt);   
            
        } else if(action == VToolForm.ACT_MOUSE_RELEASED ||
                    action == VToolForm.ACT_MOUSE_SCALETOUCHHANDLE_RELEASED ||
                        action == VToolForm.ACT_MOUSE_SCALEHANDLE_RELEASED ||
                            action == VToolForm.ACT_MOUSE_DRAGGED_HIT_OTHER_TOOL) {
            
            vTool.setLGripPt(null);
            java.util.BitSet visibleNodes = new java.util.BitSet(instance.getToolBaseSWG().numChildren());
            visibleNodes.set(0);
            instance.getToolBaseSWG().setChildMask(visibleNodes);                        
            
        } else if(action == toolForm.ACT_ON_INSTANCE_DELETE) {
            
            BehaviorObserver observer = vTool.getVirToolRef().getBhvObserver();
                
            ScaleVUIToolForm guiForm = (ScaleVUIToolForm) vTool.getVirToolRef().getVUIToolFormRef();
            ScaleVToolOperatorsForm operatorsForm = (ScaleVToolOperatorsForm) vTool.getVirToolRef().getVToolOperatorsFormRef();
            
            editor.removeNode(instance.getToolBaseBG());
            editor.getVirToolStack().removeAll(instance);
            editor.getVirToolMap().remove(instance.getInstanceName());
                
            observer.toolRemoved(instance);
            observer.removeGUI(guiForm);
            observer.removeGUI(operatorsForm);              
            
        } else {
            
            System.out.println("[ " + action + " ] NO SUCH ACTION !!!!");
            
        }
        
    }
    
}


