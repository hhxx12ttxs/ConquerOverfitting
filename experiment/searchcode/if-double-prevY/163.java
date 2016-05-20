package org.sgJoe.tools.lights.directional;

import javax.media.j3d.*;

import javax.vecmath.*;

import org.apache.log4j.Logger;
import org.sgJoe.exception.SGPluginException;
import org.sgJoe.graphics.SceneGraphEditor;
import org.sgJoe.graphics.behaviors.BehaviorObserver;
import org.sgJoe.graphics.light.LightShape;
import org.sgJoe.logic.Session;
import org.sgJoe.plugin.Form;
import org.sgJoe.tools.interfaces.*;
import org.sgJoe.tools.lights.*;

/*
 * Descritpion for DirLightVToolPlugin.java
 *
 *
 * @author   $ Author: Aleksandar Babic  $
 * @version  $ Revision:             0.1 $
 * @date     $ Date: June 4, 2006  3:23 PM  $
 */

public class DirLightVToolPlugin extends LightVToolPlugin {
    
    private static Logger logger = Logger.getLogger(DirLightVToolPlugin.class);
   
    public DirLightVToolPlugin(VirTool virToolRef) {
        super(virToolRef);
    }

   public void performAction(Form form, SceneGraphEditor editor, Session session) throws SGPluginException {
        LightVToolForm toolForm = (LightVToolForm) form;
        
        View viewSource = toolForm.getViewSource();
        
        int prevX = toolForm.getPrevX();
        int prevY = toolForm.getPrevY();
        int currX = toolForm.getCurrX();
        int currY = toolForm.getCurrY();
        
        int action = toolForm.getAction();
        
        LightVirTool instance = (LightVirTool) toolForm.getVirToolRef();
        LightVTool vTool = (LightVTool)instance.getVToolRef();
        
        if(action == VToolForm.ACT_MOUSE_CLICKED) {
            
            Transform3D temp = new Transform3D();
            vTool.setLGripPt(null);
            
            java.util.BitSet visibleNodes = new java.util.BitSet(instance.getToolBaseSWG().numChildren());
            
            visibleNodes.set(0);
            vTool.getTransform(temp);
            instance.getRotHandle().setTransform(temp);
            visibleNodes.set(1);
            instance.getToolBaseSWG().setChildMask(visibleNodes); 
            
            BehaviorObserver observer = instance.getBhvObserver();
          
            LightVUIToolForm guiform = (LightVUIToolForm) instance.getVUIToolFormRef();
            LightVToolOperatorsForm operatorsForm = (LightVToolOperatorsForm) instance.getVToolOperatorsFormRef();
            
            guiform.update();

            observer.update(guiform);
            observer.update(operatorsForm);            
            
        } else if(action == VToolForm.ACT_MOUSE_ROTATE_HANDLE_CLICKED) {
            
            Transform3D temp = new Transform3D();
            Transform3D curr = new Transform3D();
            
            vTool.setLGripPt(null);
            
            java.util.BitSet visibleNodes = new java.util.BitSet(instance.getToolBaseSWG().numChildren());
            
            visibleNodes.set(0);
            
                    translation = new Vector3d(0.0, 0.0, 0.0);
                    transformX.setTranslation(translation);
                    instance.getFrontHandle().getRotTG().getTransform(temp);
                    temp.mul(transformX, temp);
                    instance.getFrontHandle().getRotTG().setTransform(temp);
                    
                    vTool.getRotTG().getTransform(temp);     
                    
                    instance.getFrontHandle().getRotTG().setTransform(temp);                    

                    vTool.getTransform(curr);
                
                    instance.getFrontHandle().setTransform(curr);
                    
                    visibleNodes.set(2);
                    
            instance.getToolBaseSWG().setChildMask(visibleNodes); 
            
            BehaviorObserver observer = instance.getBhvObserver();
          
            LightVUIToolForm guiform = (LightVUIToolForm) instance.getVUIToolFormRef();
            LightVToolOperatorsForm operatorsForm = (LightVToolOperatorsForm) instance.getVToolOperatorsFormRef();
            
            guiform.update();

            observer.update(guiform);
            observer.update(operatorsForm);
            
        }
        else if(action == VToolForm.ACT_MOUSE_POSITION_HANDLE_PRESSED) {
            if(toolForm.getLGripPt().z < 0) {
                return;
            }
            // --> System.out.println("VToolForm.ACT_MOUSE_POSITION_HANDLE_PRESSED");
            Transform3D temp = new Transform3D();
            Transform3D curr = new Transform3D();
            
            vTool.setLGripPt(null);
            
            java.util.BitSet visibleNodes = new java.util.BitSet(instance.getToolBaseSWG().numChildren());
            
            visibleNodes.set(0);
            
                    translation = new Vector3d(0.0, 0.0, 0.0);
                    transformX.setTranslation(translation);
                    instance.getFrontHandle().getRotTG().getTransform(temp);
                    temp.mul(transformX, temp);
                    instance.getSliderHandle().getRotTG().setTransform(temp);
                    
                    vTool.getRotTG().getTransform(temp);     
                    
                    instance.getSliderHandle().getRotTG().setTransform(temp);                    

                    vTool.getTransform(curr);
                
                    instance.getSliderHandle().setTransform(curr);
                    
                    visibleNodes.set(3);
                    
            instance.getToolBaseSWG().setChildMask(visibleNodes); 
            
            BehaviorObserver observer = instance.getBhvObserver();
          
            LightVUIToolForm guiform = (LightVUIToolForm) instance.getVUIToolFormRef();
            LightVToolOperatorsForm operatorsForm = (LightVToolOperatorsForm) instance.getVToolOperatorsFormRef();
            
            guiform.update();

            observer.update(guiform);
            observer.update(operatorsForm);            
        } 
        else if(action == VToolForm.ACT_MOUSE_POSITION_HANDLE_RELEASED ||
                action == VToolForm.ACT_MOUSE_POSITION_HANDLE_CLICKED) {          
            vTool.setLGripPt(null);
            vTool.setVWGripPt(null);
        }
        else if (action == VToolForm.ACT_MOUSE_ROTATE_HANDLE_RELEASED) {     
            vTool.setLGripPt(null);
            vTool.setVWGripPt(null);
        } 
        else if(action == VToolForm.ACT_MOUSE_PLANAR_HANDLE_RELEASED || 
                    action == VToolForm.ACT_MOUSE_PLANAR_HANDLE_CLICKED || 
                        action == VToolForm.ACT_MOUSE_DRAGGED_HIT_OTHER_TOOL) {
            
            vTool.setLGripPt(null);
            vTool.setVWGripPt(null);
            
            java.util.BitSet visibleNodes = new java.util.BitSet(instance.getToolBaseSWG().numChildren());
            visibleNodes.set(0);
            instance.getToolBaseSWG().setChildMask(visibleNodes);             
        }
        else if(action == toolForm.ACT_MOUSE_TRANSLATE_XY) {
            
            Point3d newGripPt = toolForm.getLGripPt();
            Point3d oldGripPt = vTool.getLGripPt();
                       
            Point3d newVWPt = toolForm.getVWGripPt();
            Point3d oldVWPt = vTool.getVWGripPt();
            
            if(oldGripPt != null && newGripPt != null) {
                
                translation = new Vector3d();
                transformX = new Transform3D();
                
                // --> translation.sub(newGripPt, oldGripPt);
                translation.sub(newVWPt, oldVWPt);
                
                vTool.getTransform(currXform);
                transformX.set(translation);
                currXform.mul(transformX, currXform);  
                vTool.setTransform(currXform);                    
               
                BehaviorObserver observer = vTool.getVirToolRef().getBhvObserver();
                
                LightVUIToolForm guiform = (LightVUIToolForm) instance.getVUIToolFormRef();
                LightVToolOperatorsForm operatorsForm = (LightVToolOperatorsForm) instance.getVToolOperatorsFormRef();
            
                guiform.update();

                observer.update(guiform);
                observer.update(operatorsForm);  
                
            } else if(newGripPt == null) {
 
                vTool.setLGripPt(null);
            
            }
            
            vTool.setLGripPt(newGripPt);
            vTool.setVWGripPt(newVWPt);
            
                 
        } 
        else if(action == toolForm.ACT_MOUSE_RELEASED) {
  
            vTool.setLGripPt(null);
            vTool.setVWGripPt(null);
            
            vTool.setLGripPt(null);
            java.util.BitSet visibleNodes = new java.util.BitSet(instance.getToolBaseSWG().numChildren());
            visibleNodes.set(0);
            instance.getToolBaseSWG().setChildMask(visibleNodes);         
            
                BehaviorObserver observer = vTool.getVirToolRef().getBhvObserver();
                
                LightVUIToolForm guiform = (LightVUIToolForm) instance.getVUIToolFormRef();
                LightVToolOperatorsForm operatorsForm = (LightVToolOperatorsForm) instance.getVToolOperatorsFormRef();
            
                guiform.update();

                observer.update(guiform);
                observer.update(operatorsForm);              
            
        } else if(action == toolForm.ACT_MOUSE_ROTATE) {
            
            Point3d newGripPt = toolForm.getLGripPt();
            Point3d oldGripPt = vTool.getLGripPt();
                       
            if(oldGripPt != null && newGripPt != null) {
                
            Vector3d oldVector = new Vector3d(oldGripPt.x, oldGripPt.y, oldGripPt.z);
            Vector3d newVector = new Vector3d(newGripPt.x, newGripPt.y, newGripPt.z);    
            
            Vector3d axisVector = new Vector3d();
            axisVector.cross(oldVector, newVector);
                
                double angle = oldVector.angle(newVector);
                
                AxisAngle4d aa = new AxisAngle4d(axisVector, angle);
                        
                vTool.getRotTG().getTransform(currXform);
                transformX = new Transform3D();
                transformX.set(aa);
                currXform.mul(transformX, currXform);  
                vTool.getRotTG().setTransform(currXform);     
                
                BehaviorObserver observer = vTool.getVirToolRef().getBhvObserver();
                
                LightVUIToolForm guiform = (LightVUIToolForm) instance.getVUIToolFormRef();
                LightVToolOperatorsForm operatorsForm = (LightVToolOperatorsForm) instance.getVToolOperatorsFormRef();
            
                guiform.update();

                observer.update(guiform);
                observer.update(operatorsForm);                  
           
            } 
           
            vTool.setLGripPt(newGripPt);
            
        } else if(action == toolForm.ACT_ON_INSTANCE_DELETE) {
            
            OnInstanceDelete(toolForm, editor);
            
        } else if(action == VToolForm.ACT_LIGHT_COLOR_CHANGE) {
            
            Light light = vTool.getLight();
            LightShape lightShape = vTool.getLightShape();
            Material lightShapeMaterial = lightShape.getAppearance().getMaterial();
            
            Color3f color = toolForm.getColor();
            
            if(color != null) {
                light.setColor(color);
                lightShapeMaterial = lightShape.getAppearance().getMaterial();
                if(color.x > 0.9f && color.y > 0.9f && color.z > 0.9f) {
                    lightShapeMaterial.setEmissiveColor(grey);
                } else {
                    lightShapeMaterial.setEmissiveColor(color);
                }
            }
            
        } else if(action == VToolForm.ACT_LIGHT_DIRECTION_CHANGE) {
  
            DirLightVToolForm vtForm = (DirLightVToolForm) toolForm;

            Vector3f oldVector = vTool.getDirection();
            
            Vector3f newVector = new Vector3f(vtForm.getDirX(), vtForm.getDirY(), vtForm.getDirZ());    

            Vector3f axisVector = new Vector3f();
            axisVector.cross(oldVector, newVector);

            float angle = oldVector.angle(newVector);

            AxisAngle4f aa = new AxisAngle4f(axisVector, angle);

            vTool.getRotTG().getTransform(currXform);
            transformX = new Transform3D();
            transformX.set(aa);
            currXform.mul(transformX, currXform);  
            vTool.getRotTG().setTransform(currXform);     

            BehaviorObserver observer = vTool.getVirToolRef().getBhvObserver();

            LightVUIToolForm guiform = (LightVUIToolForm) instance.getVUIToolFormRef();
            LightVToolOperatorsForm operatorsForm = (LightVToolOperatorsForm) instance.getVToolOperatorsFormRef();

            //guiform.update();

            //observer.update(guiform);
            //observer.update(operatorsForm);                  
           
        } else if(action == VToolForm.ACT_LIGHT_ENABLE) {
            
            Light light = vTool.getLight();
            LightShape lightShape = vTool.getLightShape();
            Material lightShapeMaterial = lightShape.getAppearance().getMaterial();
            
            light.setEnable(toolForm.isLightEnabled());
            light.getColor(currentColor);
            if(toolForm.isLightEnabled()) {
                lightShapeMaterial.setEmissiveColor(currentColor);
            } else {
                lightShapeMaterial.setEmissiveColor(black);
            }            
        } else if(action == VToolForm.ACT_LIGHT_POSITION_CHANGE) {
            
            Vector3f lightPos = 
                    new Vector3f(toolForm.getPosX(), toolForm.getPosY(), toolForm.getPosZ());
            
            vTool.setPosition(lightPos);     
            
            Transform3D temp = new Transform3D();
            Transform3D curr = new Transform3D();
            // don't foprget to adjust handles - some may be visible////
            vTool.getTransform(temp);
            instance.getRotHandle().setTransform(temp);
            
            translation = new Vector3d(0.0, 0.0, 0.0);
            transformX.setTranslation(translation);
            instance.getFrontHandle().getRotTG().getTransform(temp);
            temp.mul(transformX, temp);
            instance.getFrontHandle().getRotTG().setTransform(temp);
                    
            vTool.getRotTG().getTransform(temp);     
                    
            instance.getFrontHandle().getRotTG().setTransform(temp);                    

            vTool.getTransform(curr);
                
            instance.getFrontHandle().setTransform(curr);            
            
        } else {
            
            System.out.println("[ " + action + " ] NO SUCH ACTION !!!!");
            
        }
        
    }
    
}



