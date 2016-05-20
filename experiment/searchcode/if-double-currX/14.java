package gov.moandor.android2048ai.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import gov.moandor.android2048ai.R;

public class Controller extends View {
    // the max distance of circle center from down position
    private static final int DISTANCE_CIRCLE = 150;
    private static final int CIRCLE_RADIUS = 60;
    
    // callback
    private ControllerCallBack mCallback;
    
    // records the x,y of down event
    private float oriX, oriY;
    
    // record the current x,y where user is pressing
    private float currX, currY;
    
    // the circle's actual position
    private float circleX, circleY;
    
    // is user pressing the controller
    private boolean isPressing;
    
    // direction icons
    private Bitmap arrow_up, arrow_left, arrow_right, arrow_down;
    
    private int arrow_icon_size;
    
    private boolean mIsActive = true;
    
    public Controller(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    
    public Controller(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public Controller(Context context) {
        super(context);
        init();
    }
    
    private void init() {
        arrow_down = BitmapFactory.decodeResource(getResources(), R.drawable.arrow_down);
        arrow_left = BitmapFactory.decodeResource(getResources(), R.drawable.arrow_left);
        arrow_right = BitmapFactory.decodeResource(getResources(), R.drawable.arrow_right);
        arrow_up = BitmapFactory.decodeResource(getResources(), R.drawable.arrow_up);
        arrow_icon_size = arrow_down.getHeight();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mIsActive) {
            return false;
        }
        switch (event.getAction()) {
        
        case MotionEvent.ACTION_DOWN:
            oriX = event.getX();
            oriY = event.getY();
            currX = event.getX();
            currY = event.getY();
            isPressing = true;
            break;
        
        case MotionEvent.ACTION_MOVE:
            currX = event.getX();
            currY = event.getY();
            break;
        
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            isPressing = false;
            
            if (currX != circleX || currY != circleY) {
                Direction availableArrow = null;
                if (currX - oriX > Math.abs(currY - oriY)) {
                    availableArrow = Direction.RIGHT;
                    
                } else if (oriX - currX > Math.abs(currY - oriY)) {
                    availableArrow = Direction.LEFT;
                    
                } else if (currY - oriY > Math.abs(oriX - currX)) {
                    availableArrow = Direction.DOWN;
                    
                } else if (oriY - currY > Math.abs(oriX - currX)) {
                    availableArrow = Direction.UP;
                }
                if (availableArrow != null) {
                    mCallback.onDirectionChosed(availableArrow);
                }
            }
            
            break;
        
        default:
            break;
        }
        invalidate();
        
        return true;
    }
    
    public void setCallback(ControllerCallBack mCallback) {
        this.mCallback = mCallback;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Log.i(TAG, "draw:" + isPressing);
        if (isPressing) {
            calcCirclePosition();
            Paint paint = new Paint();
            paint.setAlpha(50); // semitransparent
            
            Direction availableArrow = null;
            if (currX != circleX || currY != circleY) {
                if (currX - oriX > Math.abs(currY - oriY)) {
                    availableArrow = Direction.RIGHT;
                    
                } else if (oriX - currX > Math.abs(currY - oriY)) {
                    availableArrow = Direction.LEFT;
                    
                } else if (currY - oriY > Math.abs(oriX - currX)) {
                    availableArrow = Direction.DOWN;
                    
                } else if (oriY - currY > Math.abs(oriX - currX)) {
                    availableArrow = Direction.UP;
                }
                
            }
            if (availableArrow != Direction.UP) {
                canvas.drawBitmap(arrow_up, oriX - arrow_icon_size / 2, oriY - DISTANCE_CIRCLE - arrow_icon_size, paint);
                
            } else {
                canvas.drawBitmap(arrow_up, oriX - arrow_icon_size / 2, oriY - DISTANCE_CIRCLE - arrow_icon_size, null);
            }
            
            if (availableArrow != Direction.DOWN) {
                canvas.drawBitmap(arrow_down, oriX - arrow_icon_size / 2, oriY + DISTANCE_CIRCLE, paint);
            } else {
                canvas.drawBitmap(arrow_down, oriX - arrow_icon_size / 2, oriY + DISTANCE_CIRCLE, null);
            }
            if (availableArrow != Direction.LEFT) {
                canvas.drawBitmap(arrow_left, oriX - DISTANCE_CIRCLE - CIRCLE_RADIUS / 2 - arrow_icon_size, oriY
                        - arrow_icon_size / 2, paint);
            } else {
                canvas.drawBitmap(arrow_left, oriX - DISTANCE_CIRCLE - CIRCLE_RADIUS / 2 - arrow_icon_size, oriY
                        - arrow_icon_size / 2, null);
            }
            if (availableArrow != Direction.RIGHT) {
                canvas.drawBitmap(arrow_right, oriX + DISTANCE_CIRCLE + CIRCLE_RADIUS / 2, oriY - arrow_icon_size / 2,
                        paint);
            } else {
                canvas.drawBitmap(arrow_right, oriX + DISTANCE_CIRCLE + CIRCLE_RADIUS / 2, oriY - arrow_icon_size / 2,
                        null);
            }
            
            paint.setColor(Color.WHITE);
            paint.setAlpha(80);
            
            // draw circle
            canvas.drawCircle(circleX, circleY, CIRCLE_RADIUS, paint);
        }
        
    }
    
    /**
     * calculate the circle's position
     */
    private void calcCirclePosition() {
        float xDis = Math.abs(currX - oriX);
        float yDis = Math.abs(currY - oriY);
        
        if (Math.pow(xDis, 2) + Math.pow(yDis, 2) < Math.pow(DISTANCE_CIRCLE, 2)) {
            circleX = currX;
            circleY = currY;
            
        } else {
            float scale = yDis / xDis;
            double angle = Math.atan(scale);
            
            if (currX > oriX) {
                circleX = (float) (oriX + DISTANCE_CIRCLE * Math.cos(angle));
                
            } else {
                circleX = (float) (oriX - DISTANCE_CIRCLE * Math.cos(angle));
            }
            
            if (currY > oriY) {
                circleY = (float) (oriY + DISTANCE_CIRCLE * Math.sin(angle));
                
            } else {
                circleY = (float) (oriY - DISTANCE_CIRCLE * Math.sin(angle));
            }
            
        }
        
    }
    
    public void setIsActive(boolean isActive) {
        mIsActive = isActive;
    }
    
    public boolean isActive() {
        return mIsActive;
    }
    
    public interface ControllerCallBack {
        
        public void onDirectionChosed(Direction direction);
    }
    
    public enum Direction {
        LEFT, RIGHT, UP, DOWN;
    }
    
}

