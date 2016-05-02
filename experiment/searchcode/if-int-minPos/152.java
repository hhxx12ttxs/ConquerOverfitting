/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.kirauks.pixelrunner.scene.base;

import net.kirauks.pixelrunner.GameActivity;
import net.kirauks.pixelrunner.scene.base.element.IListElementTouchListener;
import net.kirauks.pixelrunner.scene.base.element.ListElement;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.entity.Entity;
import org.andengine.entity.IEntity;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.shape.Shape;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ScrollDetector;
import org.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.andengine.input.touch.detector.SurfaceScrollDetector;
import org.andengine.util.adt.list.SmartList;

/**
 *
 * @author Karl
 */
public abstract class BaseListMenuScene extends BaseMenuScene implements IScrollDetectorListener, IOnSceneTouchListener, IListElementTouchListener{
    private static final float FREQ_D = 50.0f;
    private static final float MAX_ACCEL = 5000;
    private static final float IMPULSE_ACCEL = 1000;
    private static final double FRICTION = 0.96f;
    
    private static final float START_Y = GameActivity.CAMERA_HEIGHT;
    private float wrapperHeight;
    private float maxY;
    
    private enum SlideState{
        WAIT, SCROLLING, MOMENTUM, DISABLE;
    }
    
    private TimerHandler thandle;
    private SlideState currentState;
    private double accel, accel1, accel0; //Moving avenage
    private float currentY;
    private IEntity listWrapper;
    private SurfaceScrollDetector scrollDetector;
    private long t0;
    
    private SmartList<ListElement> elements;
        
    @Override
    public void createScene(){
        super.createScene();
        
        this.elements = new SmartList<ListElement>();
        
        this.listWrapper = new Entity(0, START_Y);
        this.wrapperHeight = 0;
        this.attachChild(this.listWrapper);

        this.thandle = new TimerHandler(1.0f / FREQ_D, true, new ITimerCallback() {
            @Override
            public void onTimePassed(final TimerHandler pTimerHandler) {
                BaseListMenuScene.this.doSetPos();
            }
        });
        
        this.scrollDetector = new SurfaceScrollDetector(this);
        this.setOnSceneTouchListener(this);
        
        this.registerUpdateHandler(this.thandle);
        this.currentState = SlideState.WAIT;
    }
    
    public void addListElement(ListElement element, float margin){
        if(this.elements.isEmpty()){
            element.setPosition(element.getWidth()/2, -element.getHeight()/2 - margin);
            this.wrapperHeight += element.getWrappingHeight() + 2*margin;
        }
        else{
            ListElement last = this.elements.getLast();
            element.setPosition(element.getWidth()/2, last.getY() - last.getWrappingHeight() + last.getHeight()/2 - element.getHeight()/2 - margin);
            this.wrapperHeight += element.getWrappingHeight() + margin;
        }
        this.maxY = (this.wrapperHeight > GameActivity.CAMERA_HEIGHT) ? (this.wrapperHeight - GameActivity.CAMERA_HEIGHT) : 0;
        this.registerTouchArea(element);
        element.registerListElementTouchListener(this);
        this.listWrapper.attachChild(element);
        this.elements.add(element);
    }
    public void addListElement(ListElement element){
        this.addListElement(element, 0);
    }
    public IEntity getListWrapper(){
        return this.listWrapper;
    }
    
    public void impulseUp(){
        this.accel0 = this.accel1 = this.accel = IMPULSE_ACCEL;
    }
    public void impulseDown(){
        this.accel0 = this.accel1 = this.accel = -IMPULSE_ACCEL;
    }
    
    //IOnSceneTouchListener
    @Override
    public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
        switch(pSceneTouchEvent.getAction()) {
            case TouchEvent.ACTION_DOWN:
            case TouchEvent.ACTION_MOVE:
            case TouchEvent.ACTION_CANCEL:
                if (this.currentState == SlideState.DISABLE) {
                    return true;
                }
                if (this.currentState == SlideState.MOMENTUM) {
                    this.accel0 = this.accel1 = this.accel = 0;
                    this.currentState = SlideState.WAIT;
                }
                if (this.currentState == SlideState.SCROLLING){
                    this.currentState = SlideState.MOMENTUM;
                }
                this.scrollDetector.onTouchEvent(pSceneTouchEvent);
                return true;
            default:
                return false;
        }
    }

    //IScrollDetectorListener
    @Override
    public void onScrollStarted(ScrollDetector pScollDetector, int pPointerID, float pDistanceX, float pDistanceY) {
        this.t0 = System.currentTimeMillis();
        this.currentState = SlideState.SCROLLING;
    }
    @Override
    public void onScroll(ScrollDetector pScollDetector, int pPointerID, float pDistanceX, float pDistanceY) {
        long dt = System.currentTimeMillis() - this.t0;
        if (dt == 0) {
            return;
        }
        double s = pDistanceY / (double)dt * 1000.0;  // Pixel/Second
        this.accel = (this.accel0 + this.accel1 + s) / 3;
        this.accel0 = this.accel1;
        this.accel1 = this.accel;

        this.t0 = System.currentTimeMillis();
        this.currentState = SlideState.SCROLLING;
    }
    @Override
    public void onScrollFinished(ScrollDetector pScollDetector, int pPointerID, float pDistanceX, float pDistanceY) {
        this.currentState = SlideState.MOMENTUM;
    }
    
    protected synchronized void doSetPos() {
        if (this.accel == 0) {
            return;
        }

        if (this.currentY < 0) {
            this.currentY = 0;
            this.currentState = SlideState.WAIT;
            this.accel0 = this.accel1 = this.accel = 0;
        }
        if (this.currentY > this.maxY) {
            this.currentY = this.maxY;
            this.currentState = SlideState.WAIT;
            this.accel0 = this.accel1 = this.accel = 0;
        }
        this.listWrapper.setY(START_Y + this.currentY);
        this.onListMove(this.currentY, 0, this.maxY);
        
        if (this.accel < 0 && this.accel < -MAX_ACCEL) {
            this.accel0 = this.accel1 = this.accel = -MAX_ACCEL;
        }
        if (this.accel > 0 && this.accel > MAX_ACCEL) {
            this.accel0 = this.accel1 = this.accel = MAX_ACCEL;
        }

        double deltaY = accel / FREQ_D;
        if (deltaY >= -1 && deltaY <= 1) {
            this.currentState = SlideState.WAIT;
            this.accel0 = this.accel1 = this.accel = 0;
            return;
        }
        if (!Double.isNaN(deltaY) && !Double.isInfinite(deltaY)) {
            this.currentY -= deltaY;
        }
        this.accel = (this.accel * FRICTION);
    }
    protected abstract void onListMove(float newPos, float minPos, float maxPos);
    
    //IScrollPageElementTouchListener
    @Override
    public void onElementActionUp(ListElement element) {
        if(this.currentState != SlideState.SCROLLING){
            this.onElementAction(element);
        }
    }
    public abstract void onElementAction(ListElement element);
    
    @Override
    public void disposeScene() {
        super.disposeScene();
        for(Shape element : this.elements){
            element.detachSelf();
            element.dispose();
        }
    }
}

