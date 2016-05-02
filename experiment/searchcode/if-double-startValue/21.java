package train.book.chart;

import java.text.Format;
import java.util.ArrayList;

import train.book.Const;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;

public class LineChart extends View {
	private static final int OFFSET_LEFT = 10;
	private static final int OFFSET_RIGHT = 10;
	private static final int OFFSET_TOP = 10;
	private static final int OFFSET_BOTTOM = 10;
	private static final int EXTRA_MARGIN = 50;
	private static final int SMALL_TEXT_SIZE = 14;
	private static final int LARGE_TEXT_SIZE = 26;
	private static final int LINE_SIZE = 3;
	public static final int BACKGROUND_COLOR = Color.WHITE;
	private Paint axisPaint;
	private Paint linePaint;
	private Paint axisLabelPaint;
	private Paint titlePaint;
	private ArrayList<ChartData> data2 = new ArrayList<ChartData>();
	private ArrayList<AxisLabel> xLabels = new ArrayList<AxisLabel>();
	private ArrayList<AxisLabel> yLabels = new ArrayList<AxisLabel>();
	private int fullWidth;
	private int fullHeight;
	private int areaHeight;
	private boolean processed = false;
	private Format xFormat = Const.one_decimal_format;
	private Format yFormat = Const.one_decimal_format;
	private String title = "";

	public enum PointType {
		CIRCLE, SQUARE, TRIANGLE;
	}

	public LineChart(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public LineChart(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public LineChart(Context context) {
		super(context);
		init();
	}

	private static ArrayList<Pair<Double, Integer>> linspace(double startValue,
			double endValue, int startPos, int endPos, int n) {
		ArrayList<Pair<Double, Integer>> res = new ArrayList<Pair<Double, Integer>>(
				n);
		if (n > 0) {
			// start values
			res.add(new Pair<Double, Integer>(startValue, startPos));

			if (n > 1) {
				double n1 = n - 1;
				double stepValue = (endValue - startValue) / n1;
				double stepPos = (double) (endPos - startPos) / n1;
				for (int i = 1; i < n1; i++) {
					res.add(new Pair<Double, Integer>(startValue + i
							* stepValue, (int) (startPos + i * stepPos)));
				}

				// end values
				res.add(new Pair<Double, Integer>(endValue, endPos));
			}
		}
		return res;
	}

	public void setFormat(Format xFormat, Format yFormat) {
		if (xFormat != null) {
			this.xFormat = xFormat;
		}
		if (yFormat != null) {
			this.yFormat = yFormat;
		}
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void clearData() {
		data2.clear();
	}

	public void addData(int color, ArrayList<PointF> data) {
		this.data2.add(new ChartData(color, data));
		processed = false;
		invalidate();
	}

	private void init() {
		axisPaint = new Paint();
		axisPaint.setStrokeWidth(2);
		axisPaint.setColor(Color.GRAY);

		linePaint = new Paint();
		linePaint.setAntiAlias(true);
		linePaint.setStrokeWidth(LINE_SIZE);

		axisLabelPaint = new Paint();
		axisLabelPaint.setAntiAlias(true);
		axisLabelPaint.setTextSize(SMALL_TEXT_SIZE);

		titlePaint = new Paint();
		titlePaint.setAntiAlias(true);
		titlePaint.setTextSize(LARGE_TEXT_SIZE);
	}

	private void processData() {
		float minX = Float.POSITIVE_INFINITY;
		float minY = Float.POSITIVE_INFINITY;
		float maxX = Float.NEGATIVE_INFINITY;
		float maxY = Float.NEGATIVE_INFINITY;

		int pointCount = 0;
		for (ChartData d : data2) {
			for (PointF p : d.originalData) {
				minX = Math.min(minX, p.x);
				minY = Math.min(minY, p.y);
				maxX = Math.max(maxX, p.x);
				maxY = Math.max(maxY, p.y);
				pointCount++;
			}
		}

		// Default size is 0 to 1 when all points are 0 or no points at all
		if (maxX == 0 && minX == 0 || pointCount == 0) {
			minX = 0;
			maxX = 1;
		}
		if (maxY == 0 && minY == 0 || pointCount == 0) {
			minY = 0;
			maxY = 1;
		}

		fullHeight = getHeight();
		fullWidth = getWidth();

		areaHeight = Math.max(0, fullHeight - 2 * EXTRA_MARGIN);
		int areaWidth = Math.max(0, fullWidth - 2 * EXTRA_MARGIN);

		double spanX = Math.abs(maxX - minX);
		double spanY = Math.abs(maxY - minY);
		double xUnitLength = 0;
		double yUnitLength = 0;
		if (spanX != 0) {
			xUnitLength = (areaWidth - OFFSET_LEFT - OFFSET_RIGHT) / spanX;
		}
		if (spanY != 0) {
			yUnitLength = (areaHeight - OFFSET_TOP - OFFSET_BOTTOM) / spanY;
		}

		double xTranslateFactor = EXTRA_MARGIN - (minX * xUnitLength)
				+ OFFSET_LEFT;
		double yTranslateFactor = EXTRA_MARGIN + areaHeight
				+ (minY * yUnitLength) - OFFSET_BOTTOM;

		// Calculate labels and units etc
		xLabels.clear();
		yLabels.clear();

		// x-values
		int xNums = 0;
		if (xUnitLength != 0) { // Only one value of all x-values are the same
			xNums = areaWidth / ((SMALL_TEXT_SIZE * 10));
		}
		ArrayList<Pair<Double, Integer>> xVals = linspace(minX, maxX,
				OFFSET_LEFT + EXTRA_MARGIN, EXTRA_MARGIN + areaWidth
						- OFFSET_LEFT, xNums + 1);

		int yPos = areaHeight + EXTRA_MARGIN - OFFSET_TOP;
		for (Pair<Double, Integer> p : xVals) {
			xLabels.add(new AxisLabel(xFormat.format(p.first), p.second, yPos));
		}

		// y-values
		int yNums = 0;
		if (yUnitLength != 0) { // Only one value of all y-values are the same
			yNums = areaHeight / ((SMALL_TEXT_SIZE * 10));
		}
		ArrayList<Pair<Double, Integer>> yVals = linspace(minY, maxY,
				areaHeight + EXTRA_MARGIN - OFFSET_TOP, EXTRA_MARGIN
						+ OFFSET_TOP, yNums + 1);

		int xPos = OFFSET_LEFT + EXTRA_MARGIN;
		for (Pair<Double, Integer> p : yVals) {
			yLabels.add(new AxisLabel(yFormat.format(p.first), xPos, p.second));
		}

		// Transform the data.
		for (ChartData d : data2) {
			d.convertedData.clear();
			for (PointF p : d.originalData) {
				PointF p2 = new PointF(
						(float) (xTranslateFactor + (p.x * xUnitLength)),
						(float) (yTranslateFactor - (p.y * yUnitLength)));
				d.convertedData.add(p2);
			}
		}

		processed = true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// White background
		canvas.drawColor(BACKGROUND_COLOR);

		// Draw title
		canvas.drawText(title, OFFSET_LEFT, OFFSET_TOP + LARGE_TEXT_SIZE,
				titlePaint);

		if (data2.size() == 0) {
			return;
		} else if (!processed) {
			processData();
		}

		// Draw the axis
		canvas.drawLine(OFFSET_LEFT + EXTRA_MARGIN, areaHeight + EXTRA_MARGIN
				- OFFSET_TOP, fullWidth - OFFSET_RIGHT - EXTRA_MARGIN,
				areaHeight + EXTRA_MARGIN - OFFSET_TOP, axisPaint);
		// y
		canvas.drawLine(OFFSET_LEFT + EXTRA_MARGIN, areaHeight + EXTRA_MARGIN
				- OFFSET_TOP, OFFSET_LEFT + EXTRA_MARGIN, OFFSET_TOP
				+ EXTRA_MARGIN, axisPaint);

		// Draw the labels
		for (AxisLabel l : xLabels) {
			drawXLabel(l, canvas);
		}
		for (AxisLabel l : yLabels) {
			drawYLabel(l, canvas);
		}

		// Draw the lines/points
		for (ChartData d : data2) {
			PointF prev = null;
			linePaint.setColor(d.color);
			for (PointF p : d.convertedData) {
				if (prev != null) {
					canvas.drawLine(prev.x, prev.y, p.x, p.y, linePaint);
				}
				canvas.drawCircle(p.x, p.y, LINE_SIZE, linePaint);
				// canvas.drawRect(p.x - LINE_SIZE, p.y - LINE_SIZE, p.x
				// + LINE_SIZE, p.y + LINE_SIZE, linePaint);
				// Path trianglePath = new Path();
				// trianglePath.moveTo(p.x - POINT_SIZE, p.y + POINT_SIZE - 2);
				// trianglePath.lineTo(p.x, p.y - POINT_SIZE - 2);
				// trianglePath.lineTo(p.x + POINT_SIZE, p.y + POINT_SIZE - 2);
				// trianglePath.lineTo(p.x - POINT_SIZE, p.y + POINT_SIZE - 2);
				// canvas.drawPath(trianglePath, pointPaint);
				prev = p;
			}
		}

		// Let it update
		// invalidate();
	}

	private void drawXLabel(AxisLabel l, Canvas c) {
		c.drawLine(l.p.x, l.p.y, l.p.x, l.p.y + OFFSET_TOP, axisPaint);
		if (data2.size() != 0) {
			c.drawText(l.text, l.p.x - axisLabelPaint.measureText(l.text) / 2
					+ 2, l.p.y + SMALL_TEXT_SIZE + OFFSET_TOP, axisLabelPaint);
		}
	}

	private void drawYLabel(AxisLabel l, Canvas c) {
		c.drawLine(l.p.x, l.p.y, l.p.x - OFFSET_LEFT, l.p.y, axisPaint);
		if (data2.size() != 0) {
			c.drawText(l.text, l.p.x - EXTRA_MARGIN, l.p.y + SMALL_TEXT_SIZE
					/ 2 - 2, axisLabelPaint);
		}
	}

	private class AxisLabel {
		private PointF p;
		private String text;

		public AxisLabel(String text, float x, float y) {
			this.text = text;
			p = new PointF(x, y);
		}
	}

	private class ChartData {
		private int color;
		private ArrayList<PointF> originalData;
		private ArrayList<PointF> convertedData = new ArrayList<PointF>();

		public ChartData(int color, ArrayList<PointF> data) {
			this.color = color;
			this.originalData = data;
		}
	}
}

