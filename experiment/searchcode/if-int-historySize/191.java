package com.androiddraw;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.androiddraw.geometry.ShapeDetector;

public class DrawView extends View
{
    private List<Point2D> points = new ArrayList<Point2D>(); 

    private Paint paint = new Paint();
    private final Path path = new Path();

    private static final float STROKE_WIDTH = 5f;
    private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;

    private float lastTouchX;
    private float lastTouchY;
    private final RectF dirtyRect = new RectF();

    private ShapeDetector shapeDetector = new ShapeDetector();
    private Activity activity;

    public DrawView(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);
        activity = (Activity)context;

        setFocusable(true);
        setFocusableInTouchMode(true);

        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(STROKE_WIDTH);
        paint.setColor(Color.DKGRAY);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setPathEffect(new CornerPathEffect(10));
    }

    public void clear()
    {
        final TextView textview = (TextView) activity.findViewById(R.id.bottomText);
        if (textview != null)
            textview.setText("");

        points.clear();
        path.reset();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        canvas.drawPath(path, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        float eventX = event.getX();
        float eventY = event.getY();

        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(eventX, eventY);
                return true;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
            {
                resetDirtyRect(eventX, eventY);

                int historySize = event.getHistorySize();
                for (int i = 0; i < historySize; ++i)
                {
                    float historicalX = event.getHistoricalX(i);
                    float historicalY = event.getHistoricalY(i);
                    expandDirtyRect(historicalX, historicalY);
                    path.lineTo(historicalX, historicalY);
                    points.add(new Point2D(historicalX, historicalY));
                }

                path.lineTo(eventX, eventY);

                invalidate(
                    (int) (dirtyRect.left - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                    (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

                lastTouchX = eventX;
                lastTouchY = eventY;

                if (event.getAction() == MotionEvent.ACTION_UP && !points.isEmpty())
                {
                    final TextView textview = (TextView) activity.findViewById(R.id.bottomText);
                    if (textview != null)
                    {
                        textview.setText("Shape: " + shapeDetector.CheckShapeType(points).toString());
                        points.clear();
                        return true;
                    }
                }

                points.add(new Point2D(eventX, eventY));
                return true;
            }
            default:
                return false;
        }
    }

    private void expandDirtyRect(float historicalX, float historicalY)
    {
        if (historicalX < dirtyRect.left)
            dirtyRect.left = historicalX;
        else if (historicalX > dirtyRect.right)
            dirtyRect.right = historicalX;

        if (historicalY < dirtyRect.top)
            dirtyRect.top = historicalY;
        else if (historicalY > dirtyRect.bottom)
            dirtyRect.bottom = historicalY;
    }

    private void resetDirtyRect(float eventX, float eventY)
    {
        dirtyRect.left   = Math.min(lastTouchX, eventX);
        dirtyRect.right  = Math.max(lastTouchX, eventX);
        dirtyRect.top    = Math.min(lastTouchY, eventY);
        dirtyRect.bottom = Math.max(lastTouchY, eventY);
    }
}

