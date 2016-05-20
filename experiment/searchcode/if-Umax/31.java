package com.arhangeldim.graphics;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;

public class Bsplain extends Curve {

	private static final int ORDER = 4;
	protected List<PointF> divides = new ArrayList<PointF>();
	protected List<PointF> polyline = new ArrayList<PointF>();
		
	@Override
	public void drawCurve(Canvas canvas, Paint paint) {
		if (controlPoints.size() < 5)
			return;
		else {
			polyline.clear();
			polyline.add(controlPoints.get(0));
			polyline.add(controlPoints.get(1));
			for (int i = 1; i <= getDividePointsCount(ORDER, controlPoints.size()); i++)
				deBure(ORDER, i, buildKnots(ORDER, controlPoints.size()), canvas, paint);
			polyline.add(controlPoints.get(controlPoints.size() - 2));
			polyline.add(controlPoints.get(controlPoints.size() - 1));
			parsePolyline(canvas, paint);
			
		}
	}

	private int getDividePointsCount(int order, int pointsCount) {
		return pointsCount - order;
	}
	
	private int[] buildKnots(int order, int pointsCount) {
		int n = pointsCount - 1;
		int umax = n - order + 2;
		int length = umax + 1 + 2 * (order - 1);
		int[] knots = new int[length];
		for (int i = 0; i < order; i++) {
			knots[i] = 0;
			knots[length - 1 - i] = umax;
		}
		for (int i = order, j = 1; i < length - order; i++, j++) {
			knots[i] = j;
		}
		return knots;
	}
	
	public PointF deBure(int order, int u, int[] knots, Canvas canvas, Paint paint) {
		int l = 0;
		/* Search for right position in knot vector */
		for (int i = 0; i < knots.length; i++) {
			if (u == knots[i]) {
				l = i;
				break;
			}
		}
		PointF[] A = new PointF[order];
		for (int i = 0; i < order; i++) {
			PointF p2d = controlPoints.get(l - order + 1 + i);
			A[i] = new PointF(p2d.x, p2d.y);
		}

		/* r < order */
		for (int r = 1; r < order - 1; r++) {
			for (int j = order - 1; j >= r; j--) {
				int i = l - order + j + 1;
				float d1 = u - knots[i];
				float d2 = knots[i + order - r] - u;
				A[j].x = (d1 * A[j].x + d2 * A[j - 1].x) / (d1 + d2);
				A[j].y = (d1 * A[j].y + d2 * A[j - 1].y) / (d1 + d2);
			}
			
			if (drawDividingPoints && canvas != null && r == 2) {
				for (int p = 1; p <= order - 1; p++) {
					polyline.add(A[p]);
				}
				RectF r2d = new RectF(A[order - 2].x - 2, A[order - 2].y - 2, A[order - 2].x + 2, A[order - 2].y + 2);
				canvas.drawRect(r2d, paint);
				
			}
		}
		return new PointF(A[order - 1].x, A[order - 1].y);
	}
	
	private void parsePolyline(Canvas canvas, Paint paint) {
		for (int i = 0; i <= getDividePointsCount(ORDER, controlPoints.size()); i++) {
			divides.clear();
			bezierDivider(
					polyline.get(0 + i * 3).x,
					polyline.get(0 + i * 3).y,
					polyline.get(1 + i * 3).x,
					polyline.get(1 + i * 3).y,
					polyline.get(2 + i * 3).x,
					polyline.get(2 + i * 3).y,
					polyline.get(3 + i * 3).x,
					polyline.get(3 + i * 3).y);
			paint.setColor(Color.DKGRAY);
			drawCurveByPoints(canvas, divides, paint);
		}
	}
	
	private void recursiveBezier(float x1, float y1, float x2, float y2,
								 float x3, float y3, float x4, float y4) {
		float x12  = (x1 + x2) / 2;
		float y12  = (y1 + y2) / 2;
		float x23  = (x2 + x3) / 2;
		float y23  = (y2 + y3) / 2;
		float x34  = (x3 + x4) / 2;
		float y34  = (y3 + y4) / 2;
		float x123  = (x12 + x23) / 2;
		float y123  = (y12 + y23) / 2;
		float x234  = (x23 + x34) / 2;
		float y234  = (y23 + y34) / 2;
		float x1234 = (x123 + x234) / 2;
		float y1234 = (y123 + y234) / 2;

		float dx = x4 - x1;
		float dy = y4 - y1;

		float d2 = Math.abs(((x2 - x4) * dy - (y2 - y4) * dx));
		float d3 = Math.abs(((x3 - x4) * dy - (y3 - y4) * dx));

		if((d2 + d3)*(d2 + d3) < 0.25 * (dx*dx + dy*dy))
		{
			divides.add(new PointF(x1234, y1234));
			return;
		}

		recursiveBezier(x1, y1, x12, y12, x123, y123, x1234, y1234);
		recursiveBezier(x1234, y1234, x234, y234, x34, y34, x4, y4);

	}


	private void bezierDivider(float x1, float y1, float x2, float y2,
							   float x3, float y3, float x4, float y4) {
		divides.add(new PointF(x1, y1));
		recursiveBezier(x1, y1, x2, y2, x3, y3, x4, y4);
		divides.add(new PointF(x4, y4));
	}
}

