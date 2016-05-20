package org.sgJoe.embedded.tools.sample;

import org.sgJoe.embedded.logic.Session;
import org.apache.log4j.Logger;

import javax.media.j3d.*;
import javax.vecmath.*;
import org.sgJoe.embedded.exception.*;
import org.sgJoe.embedded.graphics.*;
import org.sgJoe.embedded.graphics.behaviors.*;
import org.sgJoe.embedded.plugin.*;
import org.sgJoe.embedded.tools.*;
import org.sgJoe.embedded.tools.interfaces.*;

/*
 * Descritpion for SampleVTool.java
 *
 *
 * @author   $ Author: Aleksandar Babic         $
 * @version  $ Revision:             0.1        $
 * @date     $ Date: April 2, 2006  9:25 PM     $
 */

public class SampleVToolPlugin extends VToolPlugin {
    
    private static Logger logger = Logger.getLogger(SampleVToolPlugin.class);
      
    private double x_factor = .01;
    private double y_factor = .01;
    private Vector3d translation = new Vector3d();
    private Transform3D currXform = new Transform3D();
    private Transform3D transformX = new Transform3D();
    
    public SampleVToolPlugin(VirTool virToolRef) {
        super(virToolRef);
    }
    
    public void performAction(Form form, SceneGraphEditor editor, Session session) throws SGPluginException {
        SampleVToolForm toolForm = (SampleVToolForm) form;
        
        View viewSource = toolForm.getViewSource();
        
        int prevX = toolForm.getPrevX();
        int prevY = toolForm.getPrevY();
        int currX = toolForm.getCurrX();
        int currY = toolForm.getCurrY();
        
        int action = toolForm.getAction();
        VirTool instance = toolForm.getVirToolRef();
        SampleVTool sg3dTool = (SampleVTool) instance.getVToolRef();
        
        BehaviorObserver observer = instance.getBhvObserver();
        SampleVUIToolForm guiform = (SampleVUIToolForm) sg3dTool.getVirToolRef().getVUIToolFormRef();
        SampleVToolOperatorsForm operatorsForm = (SampleVToolOperatorsForm) sg3dTool.getVirToolRef().getVToolOperatorsFormRef();
        String text = sg3dTool.getText();
        
        switch(action) {
            
            case VToolEventRegistry.ACT_MOUSE_PRESSED:

                text = text + "Mouse_Pressed for " + sg3dTool.getToolUID() + "\n";
                guiform.setText(text);
            
                sg3dTool.setText(text);
                observer.update(guiform);
                observer.update(operatorsForm);                
                break;
                
            case VToolForm.ACT_MOUSE_DRAGGED:

                text = text + "Mouse_Dragged for " + sg3dTool.getToolUID() + "\n";
                guiform.setText(text);
            
                sg3dTool.setText(text);
                observer.update(guiform);                    
                break;
                
            case VToolForm.ACT_ON_INSTANCE_DELETE:
          
                SampleVUIToolForm guiForm = (SampleVUIToolForm) sg3dTool.getVirToolRef().getVUIToolFormRef();
            
                editor.removeNode(instance.getToolBaseBG());
                editor.getVirToolStack().removeAll(instance);
                editor.getVirToolMap().remove(instance.getInstanceName());
                
                observer.toolRemoved(instance);
                observer.removeGUI(guiForm);
                observer.removeGUI(operatorsForm);  
                break;
                
            case SampleVToolForm.ACT_CHANGE_COORDINATE_SYSTEM:
                
                sg3dTool.setCoordinateSystem(toolForm.getCoordinateSystem());
                text = text + "coor sys " + toolForm.getCoordinateSystem() + " for " + sg3dTool.getToolUID() + "\n";
                guiform.setText(text);
            
                sg3dTool.setText(text);
                observer.update(guiform);   
                break;
                
            case VToolForm.ACT_MOUSE_OUT_OF_SCOPE:
                
                text = text + "out of object bounds for " + sg3dTool.getToolUID() + "\n";
                guiform.setText(text);
            
                sg3dTool.setText(text);
                observer.update(guiform);            
                break;
                
            case VToolForm.ACT_ACTIONBUTTON_PRESSED:
                
                text = text + "ActionButton Pressed for " + sg3dTool.getToolUID() + "\n";
                guiform.setText(text);
            
                sg3dTool.setText(text);
                observer.update(guiform);                            
                break;
                
            case VToolForm.ACT_MOUSE_RELEASED:
                text = text + "Mouse_Released for " + sg3dTool.getToolUID() + "\n";
                guiform.setText(text);
            
                sg3dTool.setText(text);
                observer.update(sg3dTool.getVirToolRef().getVToolOperatorsFormRef());
                observer.update(guiform);                            
                
            case VToolForm.ACT_MOUSE_TRANSLATE_X:
                
                switch(sg3dTool.getCoordinateSystem()) {
                    case SampleVTool.COORD_SYS_VIEW:
                    
                        Point3d currPt = new Point3d(currX, 0.0, 0.0);
                        Point3d prevPt = new Point3d(prevX, 0.0, 0.0);                    
                    
                        Transform3D loc2vworldTr3D = new Transform3D();
                        viewSource.getCanvas3D(0).getVworldToImagePlate(loc2vworldTr3D);

                        loc2vworldTr3D.transpose();

                        loc2vworldTr3D.transform(prevPt);
                        loc2vworldTr3D.transform(currPt);

                        sg3dTool.getVirToolRef().getToolBaseTG().getTransform(currXform);
                    
                        translation = new Vector3d();
                        translation.sub(currPt, prevPt);  
                        translation.sub(currPt, prevPt); 

                        if(translation.x != 0) {
                            translation.x *= x_factor * 10;
                        }
                        if(translation.y != 0) {
                            translation.y *= x_factor * 10;
                        }            
                        if(translation.z != 0) {
                            translation.z *= x_factor * 10;
                        }                        

                        transformX.set(translation);
                        currXform.mul(transformX, currXform);                    
                        break;
                    
                    case SampleVTool.COORD_SYS_WORLD:
                    
                        int dx = currX - prevX;   
                    
                        instance.getToolBaseTG().getTransform(currXform);
                    
                        translation = new Vector3d();
                    
                        translation.x = dx*x_factor;
                    
                        transformX.set(translation);
                        currXform.mul(transformX, currXform);
                                        
                        break;
                    default:
                        System.out.println("SampleVTool - NO DEFAULT BEHAVIOR");
                }            
   
                instance.getToolBaseTG().setTransform(currXform);    
                break;
                
            case VToolForm.ACT_MOUSE_TRANSLATE_Y:
                
                switch(sg3dTool.getCoordinateSystem()) {
                    case SampleVTool.COORD_SYS_VIEW:
                    
                        Point3d currPt = new Point3d(0.0, currY, 0.0);
                        Point3d prevPt = new Point3d(0.0, prevY, 0.0);                    
                        Transform3D loc2vworldTr3D = new Transform3D();
                        viewSource.getCanvas3D(0).getVworldToImagePlate(loc2vworldTr3D);

                        loc2vworldTr3D.transpose();

                        loc2vworldTr3D.transform(prevPt);
                        loc2vworldTr3D.transform(currPt);

                        instance.getToolBaseTG().getTransform(currXform);

                        translation = new Vector3d();
                        translation.sub(currPt, prevPt);  
                        if(translation.x != 0) {
                            translation.x *= -y_factor * 10;
                        }
                        if(translation.y != 0) {
                            translation.y *= -y_factor * 10;
                        }            
                        if(translation.z != 0) {
                            translation.z *= -y_factor *10;
                        }            

                        transformX.set(translation);
                        currXform.mul(transformX, currXform);                    
                        break;
                    
                    case SampleVTool.COORD_SYS_WORLD:
                    
                        int dy = currY - prevY;   
                    
                        sg3dTool.getVirToolRef().getToolBaseTG().getTransform(currXform);
                    
                        translation = new Vector3d();
                    
                        translation.y = -dy*y_factor;
                    
                        transformX.set(translation);
                        currXform.mul(transformX, currXform);
                                        
                        break;
                    default:
                        System.out.println("SampleVTool - NO DEFAULT BEHAVIOR");
                }

            
                instance.getToolBaseTG().setTransform(currXform);     
            
                if(instance.getVUIToolFormRef().isDirty()) {
                    observer.update(instance.getVUIToolFormRef());
                }
                if(instance.getVToolOperatorsFormRef().isDirty()) {
                    observer.update(instance.getVToolOperatorsFormRef());
                }
                break;
                
            case VToolForm.ACT_MOUSE_TRANSLATE_Z:
                
                switch(sg3dTool.getCoordinateSystem()) {
                    case SampleVTool.COORD_SYS_VIEW:
                    
                        Point3d currPt = new Point3d(0.0, 0.0, -currX); // + currY);
                        Point3d prevPt = new Point3d(0.0, 0.0, -prevX); // + prevY);                    
                    
                        Transform3D loc2vworldTr3D = new Transform3D();
                        viewSource.getCanvas3D(0).getVworldToImagePlate(loc2vworldTr3D);

                        loc2vworldTr3D.transpose();

                        loc2vworldTr3D.transform(prevPt);
                        loc2vworldTr3D.transform(currPt);

                        sg3dTool.getTransform(currXform);

                        translation = new Vector3d();
                        translation.sub(currPt, prevPt);  
                        translation.sub(currPt, prevPt); 

                        if(translation.x != 0) {
                            translation.x *= x_factor * 10;
                        }
                        if(translation.y != 0) {
                            translation.y *= x_factor * 10;
                        }            
                        if(translation.z != 0) {
                            translation.z *= x_factor * 10;
                        }                        

                        transformX.set(translation);
                        currXform.mul(transformX, currXform);                    
                        break;
                    
                    case SampleVTool.COORD_SYS_WORLD:
                    
                        int dx = currX - prevX;
                        int dy = currY - prevY;          
                    
                        sg3dTool.getTransform(currXform);
                    
                        translation = new Vector3d();
                    
                        translation.z = -dx*x_factor; // + dy*y_factor;
                    
                        transformX.set(translation);
                        currXform.mul(transformX, currXform);
                                        
                        break;
                        
                    default:
                        System.out.println("SampleVTool - NO DEFAULT BEHAVIOR");
                }            
   
                instance.getToolBaseTG().setTransform(currXform);     
            
                if(instance.getVUIToolFormRef().isDirty()) {
                    observer.update(instance.getVUIToolFormRef());
                }
                if(instance.getVToolOperatorsFormRef().isDirty()) {
                    observer.update(instance.getVToolOperatorsFormRef());
                }
                
            default:
                
                System.out.println("NO SUCH ACTION = " + action);
        }
    
    }
}

