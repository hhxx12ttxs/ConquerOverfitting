/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.VIVE.graphics.navigation;

/**
 *
 * @author Dusan
 */
import com.sun.j3d.exp.swing.JCanvas3D;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.pickfast.PickCanvas;
import com.sun.j3d.utils.picking.PickIntersection;
import com.sun.j3d.utils.picking.PickResult;
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.event.MouseEvent;

import java.util.ArrayList;
import java.util.Enumeration;

import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.BranchGroup;

import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Geometry;
import javax.media.j3d.Group;
import javax.media.j3d.Node;

import javax.media.j3d.PickConeSegment;
import javax.media.j3d.PickInfo;

import javax.media.j3d.Shape3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.Transform3D;

import javax.media.j3d.TransparencyAttributes;
import javax.media.j3d.View;
import javax.swing.SwingUtilities;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;

import javax.vecmath.Vector3d;
import org.sgJoe.graphics.event.EventDispatcher;
import org.sgJoe.graphics.event.SGEvent;
import org.sgJoe.graphics.event.view.SGEventView;
import org.sgJoe.graphics.event.view.ViewEventPublisher;

/**
 * Moves the View around a point of interest when the mouse is dragged with
 * a mouse button pressed.  Includes rotation, zoom, and translation
 * actions. Zooming can also be obtained by using mouse wheel.
 * <p>
 * The rotate action rotates the ViewPlatform around the point of interest
 * when the mouse is moved with the main mouse button pressed.  The
 * rotation is in the direction of the mouse movement, with a default
 * rotation of 0.01 radians for each pixel of mouse movement.
 * <p>
 * The zoom action moves the ViewPlatform closer to or further from the
 * point of interest when the mouse is moved with the middle mouse button
 * pressed (or Alt-main mouse button on systems without a middle mouse button).
 * The default zoom action is to translate the ViewPlatform 0.01 units for each
 * pixel of mouse movement.  Moving the mouse up moves the ViewPlatform closer,
 * moving the mouse down moves the ViewPlatform further away.
 * <p>
 * By default, the zoom action allows the ViewPlatform to move through
 * the center of rotation to orbit at a negative radius.
 * The <code>STOP_ZOOM</code> constructor flag will stop the ViewPlatform at
 * a minimum radius from the center.  The default minimum radius is 0.0
 * and can be set using the <code>setMinRadius</code> method.
 * <p>
 * The <code>PROPORTIONAL_ZOOM</code> constructor flag changes the zoom action
 * to move the ViewPlatform proportional to its distance from the center
 * of rotation.  For this mode, the default action is to move the ViewPlatform
 * by 1% of its distance from the center of rotation for each pixel of
 * mouse movement.
 * <p>
 * The translate action translates the ViewPlatform when the mouse is moved
 * with the right mouse button pressed (Shift-main mouse button on systems
 * without a right mouse button).  The translation is in the direction of the
 * mouse movement, with a default translation of 0.01 units for each pixel
 * of mouse movement.
 * <p>
 * The sensitivity of the actions can be scaled using the
 * <code>set</code><i>Action</i><code>Factor()</code> methods which scale
 * the default movement by the factor. The rotate and translate actions
 * have separate factors for x and y.
 * <p>
 * The actions can be reversed using the <code>REVERSE_</code><i>ACTION</i>
 * constructor flags.  The default action moves the ViewPlatform around the
 * objects in the scene.  The <code>REVERSE_</code><i>ACTION</i> flags can
 * make the objects in the scene appear to be moving in the direction
 * of the mouse movement.
 * <p>
 * The actions can be disabled by either using the
 * <code>DISABLE_</code><i>ACTION</i> constructor flags or the
 * <code>set</code><i>Action</i><code>Enable</code> methods.
 * <p>
 * The default center of rotation is (0, 0, 0) and can be set using the
 * <code>setRotationCenter()</code> method.
 *
 * @since Java 3D 1.2.1
 *
 */
public class OrbitBehaviorInterim extends ViewPlatformAWTBehaviorInterim implements ViewEventPublisher {

   private static long counter = 0;
   private long myID = -1;
   private EventDispatcher dispatcher;
   private View viewRef;
   public static boolean markerVisible = false;
   private TransformGroup markerScalableTransform = new TransformGroup();
   private BranchGroup markerBrGroup = new BranchGroup();
   private Shape3D markerShape = new Shape3D();
   private Color3f markerColor = new Color3f(1.8f, 0.1f, 0.1f);
   private float markerDiamater = 0.01f;
   private TransformGroup objectTransform = new TransformGroup();
   private NavigationChildBranchGroup childBranchGroup = new NavigationChildBranchGroup();
   private BranchGroup parentBranchGroup = new BranchGroup();
   private Transform3D locToVWorld = new Transform3D();
   private Point3d rotationPoint = new Point3d();
   private Transform3D longditudeTransform = new Transform3D();
   private Transform3D latitudeTransform = new Transform3D();
   private Transform3D rotateTransform = new Transform3D();
   private Transform3D storeReferencePoint = new Transform3D();
   // needed for integrateTransforms but don't want to new every time
   private Transform3D temp1 = new Transform3D();
   private Transform3D temp2 = new Transform3D();
   private Vector3d tempvec1 = new Vector3d();
   private Vector3d tempvec2 = new Vector3d();
   private Transform3D translation = new Transform3D();
   private Vector3d transVector = new Vector3d();
   private Vector3d distanceVector = new Vector3d();
   private Vector3d centerVector = new Vector3d();
   private Vector3d invertCenterVector = new Vector3d();
   private double longditude = 0.0;
   private double latitude = 0.0;
//    private double rollAngle = 0.0;
   private double startDistanceFromCenter = 20.0;
   private double distanceFromCenter = 20.0;
//    private final double MAX_MOUSE_ANGLE = Math.toRadians( 3 );
//    private final double ZOOM_FACTOR = 1.0;
   private Point3d rotationCenter = new Point3d();
   private Matrix3d rotMatrix = new Matrix3d();
   private Transform3D currentXfm = new Transform3D();
   private Transform3D targetTGState = new Transform3D();
   private PickCanvas pickCanvas;
   private int mouseX = 0;
   private int mouseY = 0;
   private double rotXFactor = 1.0;
   private double rotYFactor = 1.0;
   private double transXFactor = 1.0;
   private double transYFactor = 1.0;
   private double zoomFactor = 1.0;
   private double xtrans = 0.0;
   private double ytrans = 0.0;
   private double ztrans = 0.0;
   private boolean zoomEnabled = true;
   private boolean rotateEnabled = true;
   private boolean translateEnabled = true;
   private boolean reverseRotate = false;
   private boolean reverseTrans = false;
   private boolean reverseZoom = false;
   private boolean stopZoom = false;
   private boolean proportionalZoom = false;
   private boolean proportionalTranslate = true;
   private double minRadius = 1.0;
   private int leftButton = ROTATE;
   private int rightButton = TRANSLATE;
   private int middleButton = ZOOM;
   // the factor to be applied to wheel zooming so that it does not
   // look much different with mouse movement zooming.
   // This is a totally subjective factor.
   private float wheelZoomFactor = 50.0f;
   /**
    * Constructor flag to reverse the rotate behavior
    */
   public static final int REVERSE_ROTATE = 0x010;
   /**
    * Constructor flag to reverse the translate behavior
    */
   public static final int REVERSE_TRANSLATE = 0x020;
   /**
    * Constructor flag to reverse the zoom behavior
    */
   public static final int REVERSE_ZOOM = 0x040;
   /**
    * Constructor flag to reverse all the behaviors
    */
   public static final int REVERSE_ALL = (REVERSE_ROTATE | REVERSE_TRANSLATE | REVERSE_ZOOM);
   /**
    * Constructor flag that indicates zoom should stop when it reaches
    * the minimum orbit radius set by setMinRadius().  The minimus
    * radius default is 0.0.
    */
   public static final int STOP_ZOOM = 0x100;
   /**
    * Constructor flag to disable rotate
    */
   public static final int DISABLE_ROTATE = 0x200;
   /**
    * Constructor flag to disable translate
    */
   public static final int DISABLE_TRANSLATE = 0x400;
   /**
    * Constructor flag to disable zoom
    */
   public static final int DISABLE_ZOOM = 0x800;
   /**
    * Constructor flag to use proportional zoom, which determines
    * how much you zoom based on view's distance from the center of
    * rotation.  The percentage of distance that the viewer zooms
    * is determined by the zoom factor.
    */
   public static final int PROPORTIONAL_ZOOM = 0x1000;
   /**
    * Used to set the fuction for a mouse button to Rotate
    */
   private static final int ROTATE = 0;
   /**
    * Used to set the function for a mouse button to Translate
    */
   private static final int TRANSLATE = 1;
   /**
    * Used to set the function for a mouse button to Zoom
    */
   private static final int ZOOM = 2;
   private static final double NOMINAL_ZOOM_FACTOR = .01;
   private static final double NOMINAL_PZOOM_FACTOR = 1.0;
   private static final double NOMINAL_ROT_FACTOR = .01;
   private static final double NOMINAL_TRANS_FACTOR = .01;
   private double rotXMul = NOMINAL_ROT_FACTOR * rotXFactor;
   private double rotYMul = NOMINAL_ROT_FACTOR * rotYFactor;
   private double transXMul = NOMINAL_TRANS_FACTOR * transXFactor;
   private double transYMul = NOMINAL_TRANS_FACTOR * transYFactor;
   private double zoomMul = NOMINAL_ZOOM_FACTOR * zoomFactor;

   /**
    * Constructs a OrbitBehaviorInterim with a null source of Component,
    * a null target of TransformGroup, and no constructor flags.
    *
    */
   public OrbitBehaviorInterim() {
      super(MOUSE_LISTENER | MOUSE_MOTION_LISTENER | MOUSE_WHEEL_LISTENER);
   }

   /**
    * Constructs a OrbitBehaviorInterim with a null source of Component,
    * null target of TransformGroup, and specified constructor flags.
    *
    * @param flags The option flags
    */
   public OrbitBehaviorInterim(int flags) {
      super(MOUSE_LISTENER | MOUSE_MOTION_LISTENER | MOUSE_WHEEL_LISTENER | flags);

      // since 1.1
      updateFlags(flags);
   }

   /**
    * Constructs a OrbitBehaviorInterim with a specified source of Component,
    * a specified target of TransformGroup, and no constructor flags.
    *
    * @param c The Component to add the behavior to
    * @param tg The target TransformGroup for this behavior
    * @throws NullPointerException If the Component or TransformGroup are null.
    */
   public OrbitBehaviorInterim(Component c, TransformGroup tg) {
      this(c, tg, 0);
   }

   /**
    * Constructs a OrbitBehaviorInterim with a specified source of Component, a
    * specified target of TransformGroup, and specified constructor flags.
    *
    * @param c The Component to add the behavior to
    * @param tg The target TransformGroup for this behavior
    * @param flags The option flags
    * @throws NullPointerException If the Component or TransformGroup are null.
    */
   public OrbitBehaviorInterim(Component c, TransformGroup tg, int flags) {

      super(MOUSE_LISTENER | MOUSE_MOTION_LISTENER | MOUSE_WHEEL_LISTENER | flags);

      this.myID = ++counter;
      this.setPDispatcher(EventDispatcher.getDispatcher()); // mora biti ispred setviewingtransformgroup
      if (c == null) {
         throw new NullPointerException("Component is null !!");
      }
      if (tg == null) {
         throw new NullPointerException("TransformGroup is null !!");
      }

      // since 1.1
      updateFlags(flags);

      setAWTComponent(c);
      setViewingTransformGroup(tg);
      setDefaultView();

      pickCanvas = new PickCanvas(((JCanvas3D) c).getOffscreenCanvas3D(), tg.getLocale());
      pickCanvas.setMode(PickInfo.PICK_GEOMETRY);
      pickCanvas.setFlags(PickInfo.NODE | PickInfo.CLOSEST_INTERSECTION_POINT);
      pickCanvas.setTolerance(4.0f);
      childBranchGroup.setCapability(BranchGroup.ALLOW_DETACH);
      childBranchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
      childBranchGroup.setPickable(false);

      Appearance izgled = new Appearance();
      TransparencyAttributes ta = new TransparencyAttributes();
      //  ta.setTransparencyMode (ta.SCREEN_DOOR);
      ta.setTransparency(0.5f);
      izgled.setTransparencyAttributes(ta);
      ColoringAttributes boja = new ColoringAttributes(markerColor, 1);
      izgled.setColoringAttributes(boja);
      Sphere sfera = new Sphere(markerDiamater);
      Geometry geom = sfera.getShape().getGeometry();

      markerShape.addGeometry(geom);
      markerShape.setAppearance(izgled);
      markerShape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
      markerShape.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
      markerScalableTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
      markerScalableTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
      markerScalableTransform.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
      markerScalableTransform.addChild(markerShape);
      markerBrGroup.addChild(markerScalableTransform);
      markerBrGroup.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
      markerBrGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
      setMarkerVisibility();
   }

   // since 1.1
   private void updateFlags(int flags) {
      if ((flags & DISABLE_ROTATE) != 0) {
         rotateEnabled = false;
      }
      if ((flags & DISABLE_ZOOM) != 0) {
         zoomEnabled = false;
      }
      if ((flags & DISABLE_TRANSLATE) != 0) {
         translateEnabled = false;
      }
      if ((flags & REVERSE_TRANSLATE) != 0) {
         reverseTrans = true;
      }
      if ((flags & REVERSE_ROTATE) != 0) {
         reverseRotate = true;
      }
      if ((flags & REVERSE_ZOOM) != 0) {
         reverseZoom = true;
      }
      if ((flags & STOP_ZOOM) != 0) {
         stopZoom = true;
      }
      if ((flags & PROPORTIONAL_ZOOM) != 0) {
         proportionalZoom = true;
         zoomMul = NOMINAL_PZOOM_FACTOR * zoomFactor;
      }
   }

   /**
    * Sets the Component used to listen for mouse,
    * mouse motion, and mouse wheel events.
    * If a subclass overrides this method, it must
    * call super.setViewingTransformGroup(tg).<p>
    *
    * @param c the source Component for this behavior
    */
   @Override
   public void setAWTComponent(Component c) {
      super.setAWTComponent(c);

      if (component != null && targetTG != null) {
         resetView();
         integrateTransforms();
      }
   }

   /**
    * Sets the ViewPlatform's TransformGroup for this behavior.
    * If a subclass overrides this method, it must
    * call super.setViewingTransformGroup(tg).<p>
    *
    * @param tg the target TransformGroup for this behavior
    */
   @Override
   public void setViewingTransformGroup(TransformGroup tg) {
      super.setViewingTransformGroup(tg);

      if (component != null && targetTG != null) {
         resetView();
         integrateTransforms();
      }
   }

   protected synchronized void processAWTEvents(final AWTEvent[] events) {
      motion = false;

      for (int i = 0; i < events.length; i++) {
         if (events[i] instanceof MouseEvent) {
            processMouseEvent((MouseEvent) events[i]);
         }
      }
      getPDispatcher().publish(this, SGEvent.EVT_VIEW);
      ScaleMarkerToDistance();
   }

   void processMouseEvent(final MouseEvent evt) {

      if (evt.getID() == MouseEvent.MOUSE_PRESSED) {
         mouseX = evt.getX();
         mouseY = evt.getY();
         motion = true;
      } else if (evt.getID() == MouseEvent.MOUSE_DRAGGED) {
         int xchange = evt.getX() - mouseX;
         int ychange = evt.getY() - mouseY;
         // rotate
         if (rotate(evt)) {
            if (reverseRotate) {
               longditude -= xchange * rotXMul;
               latitude -= ychange * rotYMul;
            } else {
               longditude += xchange * rotXMul;
               latitude += ychange * rotYMul;
            }
         } // translate
         else if (translate(evt)) {
            if (proportionalTranslate) {
                  if (reverseTrans) {
                  xtrans -= xchange * transXMul*distanceFromCenter/20;
                  ytrans += ychange * transYMul*distanceFromCenter/20;
               } else {
                  xtrans += xchange * transXMul*distanceFromCenter/20;
                  ytrans -= ychange * transYMul*distanceFromCenter/20;
               }
            } else{
                 if (reverseTrans) {
                  xtrans -= xchange * transXMul;
                  ytrans += ychange * transYMul;
               } else {
                  xtrans += xchange * transXMul;
                  ytrans -= ychange * transYMul;
               }
            }
            
         } // zoom
         else if (zoom(evt)) {
            doZoomOperations(ychange);
         }
         mouseX = evt.getX();
         mouseY = evt.getY();
         motion = true;
      } else if (evt.getID() == MouseEvent.MOUSE_RELEASED) {
      } else if (evt.getID() == MouseEvent.MOUSE_WHEEL) {
         if (zoom(evt)) {
            // if zooming is done through mouse wheel,
            // the amount of increments the wheel changed,
            // multiplied with wheelZoomFactor is used,
            // so that zooming speed looks natural compared to mouse movement zoom.
            if (evt instanceof java.awt.event.MouseWheelEvent) {
               // I/O differenciation is made between
               // java.awt.event.MouseWheelEvent.WHEEL_UNIT_SCROLL or
               // java.awt.event.MouseWheelEvent.WHEEL_BLOCK_SCROLL so
               // that behavior remains stable and not dependent on OS settings.
               // If getWheelRotation() was used for calculating the zoom,
               // the zooming speed could act differently on different platforms,
               // if, for example, the user sets his mouse wheel to jump 10 lines
               // or a block.
               int zoom = ((int) (((java.awt.event.MouseWheelEvent) evt).getWheelRotation() * wheelZoomFactor));
               doZoomOperations(zoom);
               motion = true;
            }
         }
      }
   }

   /*extraction of the zoom algorithms so that there is no code duplication or source 'uglyfication'.
    */
   private void doZoomOperations(int ychange) {
      if (proportionalZoom) {
         if (reverseZoom) {
            if ((distanceFromCenter -
                    (zoomMul * ychange * distanceFromCenter / 100.0)) >
                    minRadius) {
               distanceFromCenter -= (zoomMul * ychange * distanceFromCenter / 100.0);
            } else {
               distanceFromCenter = minRadius;
            }
         } else {
            if ((distanceFromCenter +
                    (zoomMul * ychange * distanceFromCenter / 100.0)) > minRadius) {
               distanceFromCenter += (zoomMul * ychange * distanceFromCenter / 100.0);
            } else {
               distanceFromCenter = minRadius;
            }
         }
      } else {
         if (stopZoom) {
            if (reverseZoom) {
               if ((distanceFromCenter - ychange * zoomMul) > minRadius) {
                  distanceFromCenter -= ychange * zoomMul;
               } else {
                  distanceFromCenter = minRadius;
               }
            } else {
               if ((distanceFromCenter + ychange * zoomMul) > minRadius) {
                  distanceFromCenter += ychange * zoomMul;
               } else {
                  distanceFromCenter = minRadius;
               }
            }
         } else {
            if (reverseZoom) {
               distanceFromCenter -= ychange * zoomMul;
            } else {
               distanceFromCenter += ychange * zoomMul;
            }
         }
      }
      //     System.out.println(distanceFromCenter);

   }

   /**
    * Reset the orientation and distance of this behavior to the current
    * values in the ViewPlatform Transform Group
    */
   protected void resetView() {
      Vector3d centerToView = new Vector3d();

      targetTG.getTransform(targetTransform);

      targetTransform.get(rotMatrix, transVector);
      centerToView.sub(transVector, rotationCenter);
      distanceFromCenter = centerToView.length();
      startDistanceFromCenter = distanceFromCenter;

      targetTransform.get(rotMatrix);
      rotateTransform.set(rotMatrix);

      // compute the initial x/y/z offset
      temp1.set(centerToView);
      rotateTransform.invert();
      rotateTransform.mul(temp1);
      rotateTransform.get(centerToView);
      xtrans = centerToView.x;
      ytrans = centerToView.y;
      ztrans = centerToView.z;

      // reset rotMatrix
      rotateTransform.set(rotMatrix);
   }

   protected synchronized void integrateTransforms() {
      // Check if the transform has been changed by another
      // behavior
      targetTG.getTransform(currentXfm);
      if (!targetTransform.equals(currentXfm)) {
         resetView();
      }

      longditudeTransform.rotY(longditude);
      latitudeTransform.rotX(latitude);
      rotateTransform.mul(rotateTransform, latitudeTransform);
      rotateTransform.mul(rotateTransform, longditudeTransform);

      distanceVector.z = distanceFromCenter - startDistanceFromCenter;

      temp1.set(distanceVector);
      temp1.mul(rotateTransform, temp1);

      // want to look at rotationCenter
      transVector.x = rotationCenter.x + xtrans;
      transVector.y = rotationCenter.y + ytrans;
      transVector.z = rotationCenter.z + ztrans;

      translation.set(transVector);
      targetTransform.mul(temp1, translation);

      // handle rotationCenter
      temp1.set(centerVector);
      temp1.mul(targetTransform);

      invertCenterVector.x = -centerVector.x;
      invertCenterVector.y = -centerVector.y;
      invertCenterVector.z = -centerVector.z;

      temp2.set(invertCenterVector);
      targetTransform.mul(temp1, temp2);

      targetTG.setTransform(targetTransform);

      // reset yaw and pitch angles
      longditude = 0.0;
      latitude = 0.0;
   }

   @Override
   public void goHome() {
      if (targetTG != null) {
         targetTG.setTransform(homeTransform);
         resetView();
      }
   }

   @Override
   public void goHome(boolean aroundHomeCenter) {
      if (aroundHomeCenter) {
         if (targetTG != null) {
            setRotationCenter(homeRotCenter, false);
            targetTG.setTransform(homeTransform);
            resetView();
         }
      } else {
         this.goHome();
      }
   }

   /**
    * Sets the center around which the View rotates.
    * If ViewPlatform's TransformGroup isn't set the center is updated,
    * but rotation may not behave as expected.
    * The default center is (0,0,0).
    * @param center The Point3d to set the center of rotation to
    */
   public void setRotationCenter(Point3d center) {
      setRotationCenter(center, false);
   }

   /**
    * Sets the center around which the View rotates and moves the View to this center if desired.
    * If ViewPlatform's TransformGroup isn't set only the center is updated,
    * but rotation may not behave as expected.
    * The default center is (0,0,0).
    * @param center The Point3d to set the center of rotation to
    * @param lookAtRotCenter if true the View is moved to the rotation center, otherwise the View is unchanged
    * @since Version 1.1
    */
   public void setRotationCenter(Point3d center, boolean lookAtRotCenter) {
      rotationCenter.x = center.x;
      rotationCenter.y = center.y;
      rotationCenter.z = center.z;
      centerVector.set(rotationCenter);

      if (targetTG != null) {
         resetView();
         if (lookAtRotCenter) {
            xtrans = 0;
            ytrans = 0;
         }
         integrateTransforms();
      }
   }

   /**
    * Moves the View to the current rotation center.
    * This method has no effect if ViewPlatform's TransformGroup isn't set.
    * @since Version 1.1
    */
   public void lookAtRotationCenter() {
      if (targetTG != null) {
         resetView();
         xtrans = 0;
         ytrans = 0;
         integrateTransforms();
      }
   }

   /**
    * Places the value of the center around which the View rotates
    * into the Point3d.
    * @param center The Point3d
    */
   public void getRotationCenter(Point3d center) {
      center.x = rotationCenter.x;
      center.y = rotationCenter.y;
      center.z = rotationCenter.z;
   }

   /**
    * Returns distance from center of rotation
    */
   public double getDistanceFromCenter() {
      return distanceFromCenter;
   }

   // TODO
   // Need to add key factors for Rotate, Translate and Zoom
   // Method calls should just update MAX_KEY_ANGLE, KEY_TRANSLATE and
   // KEY_ZOOM
   //
   // Methods also need to correctly set sign of variables depending on
   // the Reverse settings.
   /**
    * Sets the rotation x and y factors.  The factors are used to determine
    * how many radians to rotate the view for each pixel of mouse movement.
    * The view is rotated factor * 0.01 radians for each pixel of mouse
    * movement.  The default factor is 1.0.
    * @param xfactor The x movement multiplier
    * @param yfactor The y movement multiplier
    **/
   public synchronized void setRotFactors(double xfactor, double yfactor) {
      rotXFactor = xfactor;
      rotYFactor = yfactor;
      rotXMul = NOMINAL_ROT_FACTOR * xfactor;
      rotYMul = NOMINAL_ROT_FACTOR * yfactor;
   }

   /**
    * Sets the rotation x factor.  The factors are used to determine
    * how many radians to rotate the view for each pixel of mouse movement.
    * The view is rotated factor * 0.01 radians for each pixel of mouse
    * movement.  The default factor is 1.0.
    * @param xfactor The x movement multiplier
    **/
   public synchronized void setRotXFactor(double xfactor) {
      rotXFactor = xfactor;
      rotXMul = NOMINAL_ROT_FACTOR * xfactor;
   }

   /**
    * Sets the rotation y factor.  The factors are used to determine
    * how many radians to rotate the view for each pixel of mouse movement.
    * The view is rotated factor * 0.01 radians for each pixel of mouse
    * movement.  The default factor is 1.0.
    * @param yfactor The y movement multiplier
    **/
   public synchronized void setRotYFactor(double yfactor) {
      rotYFactor = yfactor;
      rotYMul = NOMINAL_ROT_FACTOR * yfactor;
   }

   /**
    * Sets the translation x and y factors.  The factors are used to determine
    * how many units to translate the view for each pixel of mouse movement.
    * The view is translated factor * 0.01 units for each pixel of mouse
    * movement.  The default factor is 1.0.
    * @param xfactor The x movement multiplier
    * @param yfactor The y movement multiplier
    **/
   public synchronized void setTransFactors(double xfactor, double yfactor) {
      transXFactor = xfactor;
      transYFactor = yfactor;
      transXMul = NOMINAL_TRANS_FACTOR * xfactor;
      transYMul = NOMINAL_TRANS_FACTOR * yfactor;
   }

   /**
    * Sets the translation x factor.  The factors are used to determine
    * how many units to translate the view for each pixel of mouse movement.
    * The view is translated factor * 0.01 units for each pixel of mouse
    * movement.  The default factor is 1.0.
    * @param xfactor The x movement multiplier
    **/
   public synchronized void setTransXFactor(double xfactor) {
      transXFactor = xfactor;
      transXMul = NOMINAL_TRANS_FACTOR * xfactor;
   }

   /**
    * Sets the translation y factor.  The factors are used to determine
    * how many units to translate the view for each pixel of mouse movement.
    * The view is translated factor * 0.01 units for each pixel of mouse
    * movement.  The default factor is 1.0.
    * @param yfactor The y movement multiplier
    **/
   public synchronized void setTransYFactor(double yfactor) {
      transYFactor = yfactor;
      transYMul = NOMINAL_TRANS_FACTOR * yfactor;
   }

   /**
    * Sets the zoom factor.  The factor is used to determine how many
    * units to zoom the view for each pixel of mouse movement.
    * The view is zoomed factor * 0.01 units for each pixel of mouse
    * movement.  For proportional zoom, the view is zoomed factor * 1%
    * of the distance from the center of rotation for each pixel of
    * mouse movement.  The default factor is 1.0.
    * @param zfactor The movement multiplier
    */
   public synchronized void setZoomFactor(double zfactor) {
      zoomFactor = zfactor;
      if (proportionalZoom) {
         zoomMul = NOMINAL_PZOOM_FACTOR * zfactor;
      } else {
         zoomMul = NOMINAL_ZOOM_FACTOR * zfactor;
      }
   }

   /**
    * Returns the x rotation movement multiplier
    * @return The movement multiplier for x rotation
    */
   public double getRotXFactor() {
      return rotXFactor;
   }

   /**
    * Returns the y rotation movement multiplier
    * @return The movement multiplier for y rotation
    */
   public double getRotYFactor() {
      return rotYFactor;
   }

   /**
    * Returns the x translation movement multiplier
    * @return The movement multiplier for x translation
    */
   public double getTransXFactor() {
      return transXFactor;
   }

   /**
    * Returns the y translation movement multiplier
    * @return The movement multiplier for y translation
    */
   public double getTransYFactor() {
      return transYFactor;
   }

   /**
    * Returns the zoom movement multiplier
    * @return The movement multiplier for zoom
    */
   public double getZoomFactor() {
      return zoomFactor;
   }

   /**
    * Enables or disables rotation.  The default is true.
    * @param enabled true or false to enable or disable rotate
    */
   public synchronized void setRotateEnable(boolean enabled) {
      rotateEnabled = enabled;
   }

   /**
    * Enables or disables zoom. The default is true.
    * @param enabled true or false to enable or disable zoom
    */
   public synchronized void setZoomEnable(boolean enabled) {
      zoomEnabled = enabled;
   }

   /**
    * Enables or disables translate. The default is true.
    * @param enabled true or false to enable or disable translate
    */
   public synchronized void setTranslateEnable(boolean enabled) {
      translateEnabled = enabled;
   }

   /**
    * Retrieves the state of rotate enabled
    * @return the rotate enable state
    */
   public boolean getRotateEnable() {
      return rotateEnabled;
   }

   /**
    * Retrieves the state of zoom enabled
    * @return the zoom enable state
    */
   public boolean getZoomEnable() {
      return zoomEnabled;
   }

   /**
    * Retrieves the state of translate enabled
    * @return the translate enable state
    */
   public boolean getTranslateEnable() {
      return translateEnabled;
   }

   boolean rotate(MouseEvent evt) {
      if (rotateEnabled) {
         if ((leftButton == ROTATE) &&
                 (!evt.isAltDown() && !evt.isMetaDown())) {
            return true;
         }
         if ((middleButton == ROTATE) &&
                 (evt.isAltDown() && !evt.isMetaDown())) {
            return true;
         }
         if ((rightButton == ROTATE) &&
                 (!evt.isAltDown() && evt.isMetaDown())) {
            return true;
         }
      }
      return false;
   }

   boolean zoom(MouseEvent evt) {
      if (zoomEnabled) {
         if (evt instanceof java.awt.event.MouseWheelEvent) {
            return true;
         }
         if ((leftButton == ZOOM) &&
                 (!evt.isAltDown() && !evt.isMetaDown())) {
            return true;
         }
         if ((middleButton == ZOOM) &&
                 (evt.isAltDown() && !evt.isMetaDown())) {
            return true;
         }
         if ((rightButton == ZOOM) &&
                 (!evt.isAltDown() && evt.isMetaDown())) {
            return true;
         }
      }
      return false;
   }

   boolean translate(MouseEvent evt) {
      if (translateEnabled) {
         if ((leftButton == TRANSLATE) &&
                 (!evt.isAltDown() && !evt.isMetaDown())) {
            return true;
         }
         if ((middleButton == TRANSLATE) &&
                 (evt.isAltDown() && !evt.isMetaDown())) {
            return true;
         }
         if ((rightButton == TRANSLATE) &&
                 (!evt.isAltDown() && evt.isMetaDown())) {
            return true;
         }
      }
      return false;
   }

   /**
    * Sets the minimum radius for the OrbitBehavior.  The zoom will
    * stop at this distance from the center of rotation.  The default
    * is 0.0.  The minimum will have no affect if the STOP_ZOOM constructor
    * flag is not set.
    * @param r the minimum radius
    * @exception IllegalArgumentException if the radius is less than 0.0
    */
   public synchronized void setMinRadius(double r) {
      if (r < 0.0) {
         throw new IllegalArgumentException("OrbitBehavior MinRadius < 0.0 !!");
      }
      minRadius = r;
   }

   /**
    * Returns the minimum orbit radius.  The zoom will stop at this distance
    * from the center of rotation if the STOP_ZOOM constructor flag is set.
    * @return the minimum radius
    */
   public double getMinRadius() {
      return minRadius;
   }

   /**
    * Set reverse translate behavior.  The default is false.
    * @param state if true, reverse translate behavior
    * @since Java 3D 1.3
    */
   public void setReverseTranslate(boolean state) {
      reverseTrans = state;
   }

   /**
    * Set reverse rotate behavior.  The default is false.
    * @param state if true, reverse rotate behavior
    * @since Java 3D 1.3
    */
   public void setReverseRotate(boolean state) {
      reverseRotate = state;
   }

   /**
    * Set reverse zoom behavior.  The default is false.
    * @param state if true, reverse zoom behavior
    * @since Java 3D 1.3
    */
   public void setReverseZoom(boolean state) {
      reverseZoom = state;
   }

   /**
    * Set proportional zoom behavior.  The default is false.
    * @param state if true, use proportional zoom behavior
    * @since Java 3D 1.3
    */
   public synchronized void setProportionalZoom(boolean state) {
      proportionalZoom = state;

      if (state) {
         zoomMul = NOMINAL_PZOOM_FACTOR * zoomFactor;
      } else {
         zoomMul = NOMINAL_ZOOM_FACTOR * zoomFactor;
      }
   }

   /**
    * Set proportional translate behavior.  The default is false.
    * @param state if true, use proportional translate behavior
    * @since Java 3D 1.3
    */
   public synchronized void setProportionalTranlate(boolean state)
   {
      proportionalTranslate = state;
   }

   @Override
   public void mouseClicked(MouseEvent e) {

      if (e.isShiftDown() && e.isAltDown()) {
         changeRotationCenterSurface(e, false);   // izbor novog centra bez pomeranja pogleda
         System.out.println("centar rotacije na povrsini");
         return;
      }

      if (e.isShiftDown()) {                     // sa pomeranjem pogleda
         changeRotationCenterSurface(e, true);
         System.out.println("centar rotacije na povrsini");
         return;
      }

      if (e.isControlDown() && e.isAltDown()) {
         changeRotationCenterMiddle(e, false);
         System.out.println("centar rotacije u centru objekta");
         return;
      }

      if (e.isControlDown()) {
         changeRotationCenterMiddle(e, true);
         System.out.println("centar rotacije u centru objekta");
         return;
      }

      int clicks = e.getClickCount();
      if (clicks == 2 && SwingUtilities.isLeftMouseButton(e)) {

         // translacija unapred
         translateForwardProportonal();
         return;
      }
      if (clicks == 2 && SwingUtilities.isRightMouseButton(e)) {

         // translacija unazad
         translateBackwardProportonal();
         return;
      }
   }

   private void changeRotationCenterMiddle(MouseEvent e, boolean lookAt) {
      pickCanvas.setShapeLocation(e.getX(), e.getY());
      PickInfo pickInfo = pickCanvas.pickClosest();
      if (pickInfo == null) {
         System.out.println("pickinfo je nula");
      }
      if (pickInfo != null) {

         childBranchGroup.detach();
         childBranchGroup.removeAllChildren();

         Node pickedNode = pickInfo.getNode();

         pickedNode.getLocalToVworld(locToVWorld);
         locToVWorld.get(tempvec2);
         rotationPoint.set(tempvec2.x, tempvec2.y, tempvec2.z);


         while (!(pickedNode instanceof BranchGroup)) //idemo kroz graf do paren branchgroup
         {
            pickedNode = pickedNode.getParent();
         }

         pickedNode.getLocalToVworld(temp1);   // u temp1 je sada world transformacija parent branchgroup

         temp1.get(tempvec1);    // uzimamo translacionu komponentu i smestamo je u tempvec1
         tempvec1.negate();      // invert zato sto nam ustvari treba oduzimanje
         tempvec2.add(tempvec1);

         temp1.set(tempvec2);    // sada je u temp1 translacija od parent branchgroup do izabrane tacke,
         // i tu treba iscrtati referenti objekat

         objectTransform.setTransform(temp1);

         markerBrGroup.detach(); //za slucaj da vec ima roditelja
         objectTransform.addChild(markerBrGroup);
         childBranchGroup.addChild(objectTransform);

         parentBranchGroup = (BranchGroup) pickedNode;
         parentBranchGroup.addChild(childBranchGroup);

         if (lookAt) {
            lookAtCenter(rotationPoint);
         } else {
            setRotationCenter(rotationPoint, false);
         }

      }

   }

   private void changeRotationCenterSurface(MouseEvent e, boolean lookAt) {
      pickCanvas.setShapeLocation(e.getX(), e.getY());
      PickInfo pickInfo = pickCanvas.pickClosest();

      System.out.println(e.getX() + " " + e.getY());
      if (pickInfo == null) {
         System.out.println("pickinfo je nula");
      }

      if (pickInfo != null) {
         childBranchGroup.detach();
         childBranchGroup.removeAllChildren();
         Node pickedNode = pickInfo.getNode();

         pickedNode.getLocalToVworld(locToVWorld); // uzimamo world koordinate izabranog cvora

         rotationPoint = pickInfo.getClosestIntersectionPoint();
         locToVWorld.transform(rotationPoint); // rotationpoint sada sadrzi world koordinate izabrane tacke
         tempvec2.set(rotationPoint.x, rotationPoint.y, rotationPoint.z); // sada je u tempvec2 world translaciona komponenta izabrane tacke

         // referentni objekat rotacije, za sada plava providna loptica


         while (!(pickedNode instanceof BranchGroup)) {
            pickedNode = pickedNode.getParent();
         }

         pickedNode.getLocalToVworld(temp1);   // u temp1 je sada world transformacija parent branchgroup

         temp1.get(tempvec1);    // uzimamo translacionu komponentu i smestamo je u tempvec1
         tempvec1.negate();      // invert zato sto nam ustvari treba oduzimanje
         tempvec2.add(tempvec1);

         temp1.set(tempvec2);    // sada je u temp1 translacija od parent branchgroup do izabrane tacke,
         // i tu treba iscrtati referenti objekat

         objectTransform.setTransform(temp1);
         markerBrGroup.detach();
         objectTransform.addChild(markerBrGroup);
         childBranchGroup.addChild(objectTransform);

         parentBranchGroup = (BranchGroup) pickedNode;
         parentBranchGroup.addChild(childBranchGroup);

         if (lookAt) {
            lookAtCenter(rotationPoint);
         } else {
            setRotationCenter(rotationPoint, false);
         }
         ScaleMarkerToDistance();
      }

   }

   public void guessCenterOfRotation() {

      targetTG.getTransform(temp1);
      Point3d defaultPoint = new Point3d(0, 0, -20);
      Point3d endPoint = new Point3d(0, 0, -40);
      Point3d startPoint = new Point3d(0, 0, -0.04);
      Vector3d startPointVec = new Vector3d();

      temp1.transform(startPoint);
      temp1.transform(endPoint);
      temp1.transform(defaultPoint);


      PickConeSegment cone = new PickConeSegment(startPoint, endPoint, Math.PI / 2);

      PickInfo izabraniObjekti[] = targetTG.getLocale().pickAllSorted(PickInfo.PICK_GEOMETRY, PickInfo.NODE, cone);

      boolean nadjenCentar = false;
      Point3d lwr = new Point3d();
      Point3d upr = new Point3d();
      double maxSurface = 0;
      double surface;
      BoundingBox pickedBox = new BoundingBox();
      Node pickedNode = null;
      Node node = null;
      double dx, dy, dz;
      if (izabraniObjekti != null) {
         for (int i = 0; i < izabraniObjekti.length; i++) {
            node = izabraniObjekti[i].getNode();
            pickedBox = (BoundingBox) node.getBounds();
            pickedBox.getLower(lwr);
            pickedBox.getUpper(upr);
            dx = Math.abs(upr.x - lwr.x);
            dy = Math.abs(upr.y - lwr.y);
            dz = Math.abs(upr.z - lwr.z);
            surface = 2 * (dx * dy + dx * dz + dz * dy);

            if (surface > maxSurface) {

               pickedNode = node;
               maxSurface = surface;
               nadjenCentar = true;
            }
         }
      }

      if (nadjenCentar) {
         // postavi centar u izabranom objektu
         childBranchGroup.detach();
         childBranchGroup.removeAllChildren();

         pickedNode.getLocalToVworld(locToVWorld);
         locToVWorld.get(tempvec2);
         rotationPoint.set(tempvec2.x, tempvec2.y, tempvec2.z);


         while (!(pickedNode instanceof BranchGroup)) //idemo kroz graf do paren branchgroup
         {
            pickedNode = pickedNode.getParent();
         }

         pickedNode.getLocalToVworld(temp1);   // u temp1 je sada world transformacija parent branchgroup

         temp1.get(tempvec1);    // uzimamo translacionu komponentu i smestamo je u tempvec1
         tempvec1.negate();      // invert zato sto nam ustvari treba oduzimanje
         tempvec2.add(tempvec1);

         temp1.set(tempvec2);    // sada je u temp1 translacija od parent branchgroup do izabrane tacke,
         // i tu treba iscrtati referenti objekat

         objectTransform.setTransform(temp1);

         markerBrGroup.detach(); //za slucaj da vec ima roditelja
         objectTransform.addChild(markerBrGroup);
         childBranchGroup.addChild(objectTransform);

         parentBranchGroup = (BranchGroup) pickedNode;
         parentBranchGroup.addChild(childBranchGroup);
         setRotationCenter(rotationPoint, false);
      } else {
         // postavi centar ispred kamere
         Enumeration enume = targetTG.getLocale().getAllBranchGraphs();
         parentBranchGroup = (BranchGroup) enume.nextElement();
         childBranchGroup.detach();
         childBranchGroup.removeAllChildren();

         tempvec1.x = defaultPoint.x;
         tempvec1.y = defaultPoint.y;
         tempvec1.z = defaultPoint.z;
         temp1.set(tempvec1);
         objectTransform.setTransform(temp1);
         markerBrGroup.detach();
         objectTransform.addChild(markerBrGroup);
         childBranchGroup.addChild(objectTransform);

         parentBranchGroup.addChild(childBranchGroup);
         setRotationCenter(defaultPoint, false);
      }
      ScaleMarkerToDistance();
   }

   private void translateForwardProportonal() {
      int x;
      double pom = distanceFromCenter;
      if (pom > 10) {                    // sto smo blize centru rotacije to
         x = 30;                        // je i manji skok unapred
      } else if (pom > 5) {
         x = 50;
      } else if (pom > 2) {
         x = 60;
      } else {
         x = 70;
      }
      distanceFromCenter = distanceFromCenter * x / 100;
      if (distanceFromCenter < minRadius) {
         distanceFromCenter = pom;
      }
      ScaleMarkerToDistance();
      integrateTransforms();
   }

   private void translateBackwardProportonal() {
      double x;
      double pom = distanceFromCenter;
      if (pom < 1) {                    // sto smo blize centru rotacije to
         x = 10;                        // je veci skok unazad
      } else if (pom < 3) {
         x = 4;
      } else if (pom < 6) {
         x = 3;
      } else {
         x = 2;
      }
      distanceFromCenter = distanceFromCenter * x;
      ScaleMarkerToDistance();
      integrateTransforms();
   }

   @Override
   public void setEnable(boolean state) {
      super.setEnable(state);

      if (state) {
         for (Enumeration e = targetTG.getLocale().getAllBranchGraphs(); e.hasMoreElements();) {
            traverseDeleteNavigationChild((Group) e.nextElement());
         }

         if (childBranchGroup.getParent() == null) {    // da li je child zakacen za parent


            if (childBranchGroup.numChildren() > 0) {
               parentBranchGroup.addChild(childBranchGroup);          // ako je marker shape zakacen za child BG
               Node pom = childBranchGroup.getChild(0);
               // vec znamo da smo dobili TransformGroup
               pom = ((TransformGroup) pom).getChild(0);
               pom.getLocalToVworld(temp1);

               temp1.get(tempvec1);
               rotationPoint.x = tempvec1.x;
               rotationPoint.y = tempvec1.y;
               rotationPoint.z = tempvec1.z;

               setRotationCenter(rotationPoint, false);
               setMarkerVisibility();
               ScaleMarkerToDistance();
            }

            // ako trenutno ne postoji marker
            // parentBG nema roditelja ili mu je roditelj obrisan
            if (parentBranchGroup.getParent() == null || !(parentBranchGroup.getParent().isLive())) {
               Object ppom;

               Enumeration enume = targetTG.getLocale().getAllBranchGraphs();
               parentBranchGroup = (BranchGroup) enume.nextElement();
               childBranchGroup.detach();
               childBranchGroup.removeAllChildren();
               tempvec1.x = rotationCenter.x;
               tempvec1.y = rotationCenter.y;
               tempvec1.z = rotationCenter.z;
               temp1.set(tempvec1);
               objectTransform.setTransform(temp1);
               markerBrGroup.detach();
               objectTransform.addChild(markerBrGroup);
               childBranchGroup.addChild(objectTransform);

               parentBranchGroup.addChild(childBranchGroup);
               setMarkerVisibility();
               ScaleMarkerToDistance();
            }

         }
            getPDispatcher().publish(this, SGEvent.EVT_VIEW);
      } else {
         // brisemo indikator centra rotacije
         childBranchGroup.detach();
      }

   }

   private void traverseDeleteNavigationChild(Group brg) {

      Object ppom;
      for (Enumeration e = brg.getAllChildren(); e.hasMoreElements();) {
         ppom = e.nextElement();
         if (ppom instanceof NavigationChildBranchGroup) {
            ((BranchGroup) ppom).detach();
         } else if (ppom instanceof Group) {
            traverseDeleteNavigationChild((Group) ppom);
         }

      }
   }

   public void lookAtCenter(Point3d center) {
      rotationCenter.x = center.x;
      rotationCenter.y = center.y;
      rotationCenter.z = center.z;
      centerVector.set(rotationCenter);

      targetTG.getTransform(temp1); //TG containing the rotation

      tempvec1.set(0, 1, 0);
      temp1.transform(tempvec1);
      Point3d pomPoint = new Point3d();
      temp1.get(tempvec2);
      pomPoint.x = tempvec2.x;
      pomPoint.y = tempvec2.y;
      pomPoint.z = tempvec2.z;
      temp1.lookAt(pomPoint, rotationCenter, tempvec1);
      temp1.invert();
      targetTG.setTransform(temp1);


      resetView();
      integrateTransforms();

   }

   public void ScaleMarkerToDistance() {
      markerScalableTransform.getTransform(temp1);
      temp1.setScale(distanceFromCenter);
      markerScalableTransform.setTransform(temp1);
   }

   public void setDefaultView() {
      tempvec1.set(0.0, 0.0, 20.0);
      temp1.set(tempvec1);
      targetTG.setTransform(temp1);
   }

   public void SetRotationCenterMarker(PickResult result, boolean isCtrl, boolean isAlt) { // da bi se marker menjao iz druge klase

      if (isCtrl) {
         childBranchGroup.detach();
         childBranchGroup.removeAllChildren();

         Node pickedNode = result.getNode(PickResult.SHAPE3D);
         PickIntersection pickInter = result.getIntersection(0);

         pickedNode.getLocalToVworld(locToVWorld);
         locToVWorld.get(tempvec2);
         rotationPoint.set(tempvec2.x, tempvec2.y, tempvec2.z);


         while (!(pickedNode instanceof BranchGroup)) //idemo kroz graf do paren branchgroup
         {
            pickedNode = pickedNode.getParent();
         }

         pickedNode.getLocalToVworld(temp1);   // u temp1 je sada world transformacija parent branchgroup

         temp1.get(tempvec1);    // uzimamo translacionu komponentu i smestamo je u tempvec1
         tempvec1.negate();      // invert zato sto nam ustvari treba oduzimanje
         tempvec2.add(tempvec1);

         temp1.set(tempvec2);    // sada je u temp1 translacija od parent branchgroup do izabrane tacke,
         // i tu treba iscrtati referenti objekat

         objectTransform.setTransform(temp1);

         markerBrGroup.detach(); //za slucaj da vec ima roditelja
         objectTransform.addChild(markerBrGroup);
         childBranchGroup.addChild(objectTransform);

         parentBranchGroup = (BranchGroup) pickedNode;
         parentBranchGroup.addChild(childBranchGroup);

         if (!isAlt) {
            lookAtCenter(rotationPoint);
         } else {
            setRotationCenter(rotationPoint, false);
         }
      } else {
         childBranchGroup.detach();
         childBranchGroup.removeAllChildren();

         Node pickedNode = result.getNode(PickResult.SHAPE3D);
         PickIntersection pickInter = result.getIntersection(0);


         rotationPoint = pickInter.getPointCoordinates();
         pickedNode.getLocalToVworld(locToVWorld);
         locToVWorld.transform(rotationPoint);
         tempvec2.set(rotationPoint.x, rotationPoint.y, rotationPoint.z);

         while (!(pickedNode instanceof BranchGroup)) {
            pickedNode = pickedNode.getParent();
         }

         pickedNode.getLocalToVworld(temp1);   // u temp1 je sada world transformacija parent branchgroup

         temp1.get(tempvec1);    // uzimamo translacionu komponentu i smestamo je u tempvec1
         tempvec1.negate();      // invert zato sto nam ustvari treba oduzimanje
         tempvec2.add(tempvec1);

         temp1.set(tempvec2);    // sada je u temp1 translacija od parent branchgroup do izabrane tacke,
         // i tu treba iscrtati referenti objekat

         objectTransform.setTransform(temp1);
         markerBrGroup.detach();
         objectTransform.addChild(markerBrGroup);
         childBranchGroup.addChild(objectTransform);

         parentBranchGroup = (BranchGroup) pickedNode;
         parentBranchGroup.addChild(childBranchGroup);

         if (!isAlt) {
            lookAtCenter(rotationPoint);
         } else {
            setRotationCenter(rotationPoint, false);
         }
         ScaleMarkerToDistance();
      }
   }

   public void setMarkerVisibility() {

      if (markerVisible) {
         Appearance izgled = new Appearance();
         TransparencyAttributes ta = new TransparencyAttributes();
         //   ta.setTransparencyMode (ta.SCREEN_DOOR);
         ColoringAttributes boja = new ColoringAttributes(markerColor, 1);
         ta.setTransparency(0.5f);
         izgled.setColoringAttributes(boja);
         izgled.setTransparencyAttributes(ta);
         markerShape.setAppearance(izgled);
      } else {
         Appearance izgled = new Appearance();
         TransparencyAttributes ta = new TransparencyAttributes();
         ta.setTransparencyMode(ta.SCREEN_DOOR);
         ta.setTransparency(1f);
         ColoringAttributes boja = new ColoringAttributes(markerColor, 1);
         izgled.setColoringAttributes(boja);
         izgled.setTransparencyAttributes(ta);
         markerShape.setAppearance(izgled);
      }
   }

   public void setView(View view) {
      this.viewRef = view;
   }

   public void setPDispatcher(EventDispatcher dispatcher) {
      this.dispatcher = dispatcher;
   }

   public EventDispatcher getPDispatcher() {
      return dispatcher;
   }

   public void onPRegister(Long evtUID) {
   }

   public ArrayList getEventUIDs4Publish() {
      ArrayList evtList = new ArrayList();
      evtList.add(SGEvent.EVT_VIEW);
      return evtList;
   }

   public void onPublish(SGEvent event) {
   }

   public SGEvent createEvent(Long evtUID) {
      if (evtUID == SGEvent.EVT_VIEW) {
         return new SGEventView(this, viewRef);
      } else {
         return null;
      }
   }

   public Long getPublisherUID() {
      return new Long(myID);
   }

   public String getPublisherName() {
      return "OrbitBehaviorInterim";
   }

   public boolean isPTransparent(Long evtUID) {
      return false;
   }

   public void setPTransparent(boolean bTransparent, Long evtUID) {
   }
}

