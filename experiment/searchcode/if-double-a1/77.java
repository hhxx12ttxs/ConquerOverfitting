package de.winterberg.android.sandbox.sample3;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * @author Benjamin Winterberg
 */
public class Sample3View extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "Sample3";

    public static final int GREEN = Color.rgb(22, 245, 156);
    public static final int BLUE = Color.rgb(22, 167, 245);

    public static final float MARGIN = 5;
    public static final float STROKE_WIDTH = 5;
    public static final float CIRCLE_RADIUS = 20;


    class SurfaceThread extends Thread {
        private static final double PHYS_VELOCITY_START = 200d;      // pixel per seconds
        private static final double PHYS_VELOCITY_LOSS = 25d;        // per second

        private final Context context;
        private final SurfaceHolder surfaceHolder;

        private Paint boundsPaint;
        private Paint ballPaint;

        private boolean running = false;

        private int surfaceWidth;
        private int surfaceHeight;

        private RectF bounds;
        private PointF position;

        private long lastTimestamp;

        private double degree;
        private double velocity;


        SurfaceThread(Context context, SurfaceHolder surfaceHolder) {
            super();
            this.context = context;
            this.surfaceHolder = surfaceHolder;
            initPaints();
        }

        private void initPaints() {
            boundsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            boundsPaint.setColor(BLUE);
            boundsPaint.setStyle(Paint.Style.STROKE);
            boundsPaint.setStrokeWidth(STROKE_WIDTH);

            ballPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            ballPaint.setColor(GREEN);
            ballPaint.setStyle(Paint.Style.STROKE);
            ballPaint.setStrokeWidth(STROKE_WIDTH);
        }

        @Override
        public void run() {
            Log.d(TAG, "surface thread running");
            while (running) {
                Canvas canvas = null;
                try {
                    canvas = surfaceHolder.lockCanvas();
                    synchronized (surfaceHolder) {
                        updatePhysics();
                        doDraw(canvas);
                    }
                } finally {
                    if (canvas != null)
                        surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
            Log.d(TAG, "surface thread stopped");
        }

        private void updatePhysics() {
            long now = System.currentTimeMillis();
            long duration = now - lastTimestamp;
            lastTimestamp = now;

            // use center as starting position
            if (position == null) {
                position = new PointF(bounds.centerX(), bounds.centerY());
                velocity = PHYS_VELOCITY_START;
                degree = (7 / 4d) * Math.PI;
                return;
            }

            // do nothing without velocity
            if (velocity == 0f)
                return;

            // choke velocity until it reaches zero
            double loss = (PHYS_VELOCITY_LOSS / 1000d) * duration;
            velocity = velocity - loss;
            if (velocity <= 0) {
                velocity = 0d;
                return;
            }

            double distance = (duration / 1000d) * velocity;

            Log.d(TAG, "distance=" + distance);

            // calculate possible collision with bounds
            double dx = distance * Math.cos(degree);
            double dy = distance * Math.sin(degree);
            PointF collision = collision(dx, dy);

            if (collision == null) {
                position = new PointF(position.x + (float) dx, position.y + (float) dy);
                return;
            }

            Log.d(TAG, "COLLISION: x=" + collision.x + "; y=" + collision.y);

            // prevent collision
            position = collision;

            Path path = new Path();
            path.addRect(bounds, Path.Direction.CW);




            // calculate distance to bounds
//            double xBounds = bounds.right - position.x;
//            double distanceToBounds = xBounds / Math.cos(degree);
//
////            Log.d(TAG, "distance="+ distance + "; distanceToBounds=" + distanceToBounds);
//
//            // enough distance to bounds
//            if (distanceToBounds > distance) {
//                float x = (float) (distance * Math.cos(degree));
//                float y = (float) (distance * Math.sin(degree));
//                position = new PointF(position.x + x, position.y + y);
//                return;
//            }

            // distance to bounds is to small
            // calculate bouncing
//            distance -= distanceToBounds;
//            degree += 2 * Math.PI;
//            degree = -degree;

//            float x = (float) (distance * Math.cos(degree));
//            float y = (float) (distance * Math.sin(degree));
//
//            double yBounds = Math.sqrt(xBounds * xBounds - distanceToBounds * distanceToBounds);
//            double xCollision = position.x + xBounds;
//            double yCollision = position.y + yBounds;
//            PointF collisionPoint = new PointF((float) xCollision, (float) yCollision);
//
//            position = new PointF(collisionPoint.x + x, collisionPoint.y + y);
        }

        private PointF collision(double dx, double dy) {
            double x1 = position.x;
            double y1 = position.y;
            double x2 = x1 + dx;
            double y2 = y1 - dy;


            // f(x) = m*x + t
            double m = dy / dx;
            double t = y1 / (m * x1);

            double x, y;

            // left
            x = MARGIN;
            y = m * x + t;
            if (between(x, x1, x2) && between(y, y1, y2))
                return new PointF((float) x, (float) y);

            // right
            x = bounds.width() + MARGIN + STROKE_WIDTH;
            y = m * x + t;
//            Log.d(TAG, "right: x=" + x + "; y=" + y);
            if (between(x, x1, x2) && between(y, y1, y2))
                return new PointF((float) x, (float) y);

            // top
            y = MARGIN;
            x = (y - t) / m;
            if (between(x, x1, x2) && between(y, y1, y2))
                return new PointF((float) x, (float) y);

            // bottom
            y = bounds.height() + MARGIN;
            x = (y - t) / m;
            if (between(x, x1, x2) && between(y, y1, y2))
                return new PointF((float) x, (float) y);

            return null;
        }

        private boolean between(double a, double a1, double a2) {
            return (a >= a1 && a <= a2) || (a >= a2 && a <= a1);
        }

        private void doDraw(Canvas canvas) {
            clearScreen(canvas);
            drawBackground(canvas);
            drawBall(canvas);
        }

        private void drawBall(Canvas canvas) {
            Path circle = new Path();
            circle.addCircle(position.x, position.y, CIRCLE_RADIUS, Path.Direction.CW);
            canvas.drawPath(circle, ballPaint);
        }

        private void clearScreen(Canvas canvas) {
            canvas.drawColor(Color.BLACK);
        }

        private void drawBackground(Canvas canvas) {
            Rect bounds = canvas.getClipBounds();
            RectF rectF = new RectF(MARGIN, MARGIN, bounds.right - MARGIN, bounds.bottom - MARGIN);
            canvas.drawRoundRect(rectF, 15, 15, boundsPaint);
        }

        public void setRunning(boolean running) {
            this.running = running;
        }

        public void setSurfaceSize(int width, int height) {
            synchronized (surfaceHolder) {
                // size
                this.surfaceWidth = width;
                this.surfaceHeight = height;

                // bounds
                float left = MARGIN + STROKE_WIDTH + CIRCLE_RADIUS;
                float top = MARGIN + STROKE_WIDTH + CIRCLE_RADIUS;
                float right = surfaceWidth - MARGIN - STROKE_WIDTH - CIRCLE_RADIUS;
                float bottom = surfaceHeight - MARGIN - STROKE_WIDTH - CIRCLE_RADIUS;
                bounds = new RectF(left, top, right, bottom);
            }
        }
    }


    private Context context;

    private SurfaceThread thread;


    public Sample3View(Context context, AttributeSet attrs) {
        super(context, attrs);

        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        this.context = context;
        this.thread = new SurfaceThread(context, holder);
    }

    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged");
        thread.setSurfaceSize(width, height);
    }

    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "surfaceCreated");
        thread.setRunning(true);
        thread.start();
    }

    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "surfaceDestroyed");
        boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
                // nothing
            }
        }
    }
}
