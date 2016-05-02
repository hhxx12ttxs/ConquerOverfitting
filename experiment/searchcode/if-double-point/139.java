<<<<<<< HEAD
package org.rsbot.script.task;

import org.rsbot.script.callback.MouseCallback;
import org.rsbot.script.methods.MethodContext;
import org.rsbot.script.methods.Methods;
import org.rsbot.script.wrappers.RSTarget;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MouseTask extends AbstractTask {
	private final RSTarget target;
	private final MethodContext ctx;
	private final MouseCallback callback;
	protected final List<ForceModifier> forceModifiers = new ArrayList<ForceModifier>(5);
	protected final Vector2D velocity = new Vector2D();
	private boolean running = true;

	public MouseTask(RSTarget target, MouseCallback callback, MethodContext ctx) {
		this.target = target;
		this.callback = callback;
		this.ctx = ctx;
	}

	public void run() {
		initForceModifiers();
		while (running) {
			Point p = target.getPoint();
			if (p.x == -1 || p.y == -1) {
				try {
					Thread.sleep(random(50, 250));
				} catch (InterruptedException ignored) {
				}
				continue;
			}

			if (target.contains(ctx.client.getMouse().getX(), ctx.client.getMouse().getY()) && callback.performAction()) {
				break;
			}
			double deltaTime = Methods.random(8D, 10D) / 1000D;
			Vector2D force = new Vector2D();
			for (ForceModifier modifier : forceModifiers) {
				Vector2D f = modifier.apply(deltaTime, p);
				if (f == null) {
					continue;
				}
				force.add(f);
			}

			if (Double.isNaN(force.xUnits) || Double.isNaN(force.yUnits)) {
				return;
			}
			velocity.add(force.multiply(deltaTime));

			Vector2D deltaPosition = velocity.multiply(deltaTime);
			if (deltaPosition.xUnits != 0 && deltaPosition.yUnits != 0) {
				int x = ctx.client.getMouse().getX() + (int) deltaPosition.xUnits;
				int y = ctx.client.getMouse().getY() + (int) deltaPosition.yUnits;
				if (!ctx.client.getCanvas().contains(x, y)) {
					switch (ctx.inputManager.side) {
						case 1:
							x = 1;
							y = random(0, ctx.client.getCanvas().getHeight());
							break;
						case 2:
							x = random(0, ctx.client.getCanvas().getWidth());
							y = ctx.client.getCanvas().getHeight() + 1;
							break;
						case 3:
							x = ctx.client.getCanvas().getWidth() + 1;
							y = random(0, ctx.client.getCanvas().getHeight());
							break;
						case 4:
							x = random(0, ctx.client.getCanvas().getWidth());
							y = 1;
							break;
					}
				}
				ctx.mouse.hop(x, y);
			}

			try {
				Thread.sleep((long) (deltaTime * 1000));
			} catch (InterruptedException e) {
				return;
			}
		}
		running = false;
	}

	public boolean isRunning() {
		return running;
	}

	public void stop() {
		running = false;
	}

	public void initForceModifiers() {
		forceModifiers.add(new ForceModifier() {
			// TARGET GRAVITY
			public Vector2D apply(double deltaTime, Point pTarget) {
				Vector2D force = new Vector2D();

				Vector2D toTarget = new Vector2D();
				toTarget.xUnits = pTarget.x - ctx.client.getMouse().getX();
				toTarget.yUnits = pTarget.y - ctx.client.getMouse().getY();
				if (toTarget.xUnits == 0 && toTarget.yUnits == 0) {
					return null;
				}

				double alpha = toTarget.getAngle();
				double acc = random(1500, 2000);
				force.xUnits = Math.cos(alpha) * acc;
				force.yUnits = Math.sin(alpha) * acc;

				return force;
			}
		});

		forceModifiers.add(new ForceModifier() {
			// "friction"
			public Vector2D apply(double deltaTime, Point pTarget) {
				return velocity.multiply(-1);
			}
		});

		forceModifiers.add(new ForceModifier() {

			private int offset = random(300, 500);
			private double offsetAngle = -1;

			// Offset
			public Vector2D apply(double deltaTime, Point pTarget) {
				if (offsetAngle == -1) {
					offsetAngle = Methods.random(-Math.PI, Math.PI);
				}
				Vector2D toTarget = new Vector2D();
				toTarget.xUnits = pTarget.x - ctx.client.getMouse().getX();
				toTarget.yUnits = pTarget.y - ctx.client.getMouse().getY();
				if (offset > 0 && toTarget.getLength() > random(25, 55)) {
					Vector2D force = new Vector2D();
					force.xUnits = Math.cos(offsetAngle) * offset;
					force.yUnits = Math.sin(offsetAngle) * offset;
					offset -= random(0, 6);
					return force;
				}
				return null;
			}
		});

		forceModifiers.add(new ForceModifier() {
			// correction when close
			public Vector2D apply(double deltaTime, Point pTarget) {
				Vector2D toTarget = new Vector2D();
				toTarget.xUnits = pTarget.x - ctx.client.getMouse().getX();
				toTarget.yUnits = pTarget.y - ctx.client.getMouse().getY();
				double length = toTarget.getLength();
				if (length < random(75, 125)) {
					Vector2D force = new Vector2D();

					double speed = velocity.getLength();
					double rh = speed * speed;
					double s = toTarget.xUnits * toTarget.xUnits + toTarget.yUnits * toTarget.yUnits;
					if (s == 0) {
						return null;
					}
					double f = rh / s;
					f = Math.sqrt(f);
					Vector2D adjustedToTarget = toTarget.multiply(f);

					force.xUnits = (adjustedToTarget.xUnits - velocity.xUnits) / (deltaTime);
					force.yUnits = (adjustedToTarget.yUnits - velocity.yUnits) / (deltaTime);

					double v = 4D / length;
					if (v < 1D) {
						force = force.multiply(v);
					}
					if (length < 10) {
						force = force.multiply(0.5D);
					}
					return force;
				}
				return null;
			}
		});

		forceModifiers.add(new ForceModifier() {
			// correction when close
			public Vector2D apply(double deltaTime, Point pTarget) {
				int mouseX = ctx.client.getMouse().getX();
				int mouseY = ctx.client.getMouse().getY();
				//if(mouseX > pTarget.x-2 && mouseX < pTarget.x+2 && mouseY > pTarget.y-2 && mouseY < pTarget.y+2){
				if (mouseX == pTarget.x && mouseY == pTarget.y) {
					velocity.xUnits = 0;
					velocity.yUnits = 0;
				}
				return null;
			}
		});
	}

	/**
	 * Returns a linearly distributed pseudorandom integer.
	 *
	 * @param min The inclusive lower bound.
	 * @param max The exclusive upper bound.
	 * @return Random integer min <= n < max.
	 */
	public int random(final int min, final int max) {
		return min + (max == min ? 0 : ctx.random.nextInt(max - min));
	}

	interface ForceModifier {
		public Vector2D apply(double deltaTime, Point pTarget);
	}

	class Vector2D {
		public double xUnits;
		public double yUnits;

		public Vector2D sum(Vector2D vector) {
			Vector2D out = new Vector2D();
			out.xUnits = xUnits + vector.xUnits;
			out.yUnits = xUnits + vector.yUnits;
			return out;
		}

		public void add(Vector2D vector) {
			xUnits += vector.xUnits;
			yUnits += vector.yUnits;
		}

		public Vector2D multiply(double factor) {
			Vector2D out = new Vector2D();
			out.xUnits = xUnits * factor;
			out.yUnits = yUnits * factor;
			return out;
		}

		public double getLength() {
			return Math.sqrt(xUnits * xUnits + yUnits * yUnits);
		}

		public double getAngle() {
			return Math.atan2(yUnits, xUnits);
		}
	}
=======
/*
 * Copyright (C) 2012 CyberAgent
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.co.cyberagent.android.gpuimage;

import android.graphics.Point;
import android.graphics.PointF;
import android.opengl.GLES20;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class GPUImageToneCurveFilter extends GPUImageFilter {
    public static final String TONE_CURVE_FRAGMENT_SHADER = "" +
            " varying highp vec2 textureCoordinate;\n" +
            " uniform sampler2D inputImageTexture;\n" +
            " uniform sampler2D toneCurveTexture;\n" +
            "\n" +
            " void main()\n" +
            " {\n" +
            "     lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "     lowp float redCurveValue = texture2D(toneCurveTexture, vec2(textureColor.r, 0.0)).r;\n" +
            "     lowp float greenCurveValue = texture2D(toneCurveTexture, vec2(textureColor.g, 0.0)).g;\n" +
            "     lowp float blueCurveValue = texture2D(toneCurveTexture, vec2(textureColor.b, 0.0)).b;\n" +
            "\n" +
            "     gl_FragColor = vec4(redCurveValue, greenCurveValue, blueCurveValue, textureColor.a);\n" +
            " }";

    private int[] mToneCurveTexture = new int[]{OpenGlUtils.NO_TEXTURE};
    private int mToneCurveTextureUniformLocation;

    private PointF[] mRgbCompositeControlPoints;
    private PointF[] mRedControlPoints;
    private PointF[] mGreenControlPoints;
    private PointF[] mBlueControlPoints;

    private ArrayList<Float> mRgbCompositeCurve;
    private ArrayList<Float> mRedCurve;
    private ArrayList<Float> mGreenCurve;
    private ArrayList<Float> mBlueCurve;


    public GPUImageToneCurveFilter() {
        super(NO_FILTER_VERTEX_SHADER, TONE_CURVE_FRAGMENT_SHADER);

        PointF[] defaultCurvePoints = new PointF[]{new PointF(0.0f, 0.0f), new PointF(0.5f, 0.5f), new PointF(1.0f, 1.0f)};
        mRgbCompositeControlPoints = defaultCurvePoints;
        mRedControlPoints = defaultCurvePoints;
        mGreenControlPoints = defaultCurvePoints;
        mBlueControlPoints = defaultCurvePoints;
    }

    @Override
    public void onInit() {
        super.onInit();
        mToneCurveTextureUniformLocation = GLES20.glGetUniformLocation(getProgram(), "toneCurveTexture");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
        GLES20.glGenTextures(1, mToneCurveTexture, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mToneCurveTexture[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    }

    @Override
    public void onInitialized() {
        super.onInitialized();
        setRgbCompositeControlPoints(mRgbCompositeControlPoints);
        setRedControlPoints(mRedControlPoints);
        setGreenControlPoints(mGreenControlPoints);
        setBlueControlPoints(mBlueControlPoints);
    }

    @Override
    protected void onDrawArraysPre() {
        if (mToneCurveTexture[0] != OpenGlUtils.NO_TEXTURE) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mToneCurveTexture[0]);
            GLES20.glUniform1i(mToneCurveTextureUniformLocation, 3);
        }
    }

    public void setFromCurveFileInputStream(InputStream input) {
        try {
            int version = readShort(input);
            int totalCurves = readShort(input);

            ArrayList<PointF[]> curves = new ArrayList<PointF[]>(totalCurves);
            float pointRate = 1.0f / 255;

            for (int i = 0; i < totalCurves; i++) {
                // 2 bytes, Count of points in the curve (short integer from 2...19)
                short pointCount = readShort(input);

                PointF[] points = new PointF[pointCount];

                // point count * 4
                // Curve points. Each curve point is a pair of short integers where
                // the first number is the output value (vertical coordinate on the
                // Curves dialog graph) and the second is the input value. All coordinates have range 0 to 255.
                for (int j = 0; j < pointCount; j++) {
                    short y = readShort(input);
                    short x = readShort(input);

                    points[j] = new PointF(x * pointRate, y * pointRate);
                }

                curves.add(points);
            }
            input.close();

            mRgbCompositeControlPoints = curves.get(0);
            mRedControlPoints = curves.get(1);
            mGreenControlPoints = curves.get(2);
            mBlueControlPoints = curves.get(3);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private short readShort(InputStream input) throws IOException {
        return (short) (input.read() << 8 | input.read());
    }

    public void setRgbCompositeControlPoints(PointF[] points) {
        mRgbCompositeControlPoints = points;
        mRgbCompositeCurve = createSplineCurve(mRgbCompositeControlPoints);
        updateToneCurveTexture();
    }

    public void setRedControlPoints(PointF[] points) {
        mRedControlPoints = points;
        mRedCurve = createSplineCurve(mRedControlPoints);
        updateToneCurveTexture();
    }

    public void setGreenControlPoints(PointF[] points) {
        mGreenControlPoints = points;
        mGreenCurve = createSplineCurve(mGreenControlPoints);
        updateToneCurveTexture();
    }

    public void setBlueControlPoints(PointF[] points) {
        mBlueControlPoints = points;
        mBlueCurve = createSplineCurve(mBlueControlPoints);
        updateToneCurveTexture();
    }

    private void updateToneCurveTexture() {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mToneCurveTexture[0]);

                if ((mRedCurve.size() >= 256) && (mGreenCurve.size() >= 256) && (mBlueCurve.size() >= 256) && (mRgbCompositeCurve.size() >= 256)) {
                    byte[] toneCurveByteArray = new byte[256 * 4];
                    for (int currentCurveIndex = 0; currentCurveIndex < 256; currentCurveIndex++) {
                        // BGRA for upload to texture
                        toneCurveByteArray[currentCurveIndex * 4 + 2] = (byte) ((int) Math.min(Math.max(currentCurveIndex + mBlueCurve.get(currentCurveIndex) + mRgbCompositeCurve.get(currentCurveIndex), 0), 255) & 0xff);
                        toneCurveByteArray[currentCurveIndex * 4 + 1] = (byte) ((int) Math.min(Math.max(currentCurveIndex + mGreenCurve.get(currentCurveIndex) + mRgbCompositeCurve.get(currentCurveIndex), 0), 255) & 0xff);
                        toneCurveByteArray[currentCurveIndex * 4] = (byte) ((int) Math.min(Math.max(currentCurveIndex + mRedCurve.get(currentCurveIndex) + mRgbCompositeCurve.get(currentCurveIndex), 0), 255) & 0xff);
                        toneCurveByteArray[currentCurveIndex * 4 + 3] = (byte) (255 & 0xff);
                    }

                    GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 256 /*width*/, 1 /*height*/, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, ByteBuffer.wrap(toneCurveByteArray));
                }
//        Buffer pixels!
//        GLES20.glTexImage2D(int target,
//            int level,
//            int internalformat,
//            int width,
//            int height,
//            int border,
//            int format,
//            int type,
//            java.nio.Buffer pixels);
            }
        });
    }

    private ArrayList<Float> createSplineCurve(PointF[] points) {
        if (points == null || points.length <= 0) {
            return null;
        }

        // Sort the array
        PointF[] pointsSorted = points.clone();
        Arrays.sort(pointsSorted, new Comparator<PointF>() {
            @Override
            public int compare(PointF point1, PointF point2) {
                if (point1.x < point2.x) {
                    return -1;
                } else if (point1.x > point2.x) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        // Convert from (0, 1) to (0, 255).
        Point[] convertedPoints = new Point[pointsSorted.length];
        for (int i = 0; i < points.length; i++) {
            PointF point = pointsSorted[i];
            convertedPoints[i] = new Point((int) (point.x * 255), (int) (point.y * 255));
        }

        ArrayList<Point> splinePoints = createSplineCurve2(convertedPoints);

        // If we have a first point like (0.3, 0) we'll be missing some points at the beginning
        // that should be 0.
        Point firstSplinePoint = splinePoints.get(0);
        if (firstSplinePoint.x > 0) {
            for (int i = firstSplinePoint.x; i >= 0; i--) {
                splinePoints.add(0, new Point(i, 0));
            }
        }

        // Insert points similarly at the end, if necessary.
        Point lastSplinePoint = splinePoints.get(splinePoints.size() - 1);
        if (lastSplinePoint.x < 255) {
            for (int i = lastSplinePoint.x + 1; i <= 255; i++) {
                splinePoints.add(new Point(i, 255));
            }
        }

        // Prepare the spline points.
        ArrayList<Float> preparedSplinePoints = new ArrayList<Float>(splinePoints.size());
        for (Point newPoint : splinePoints) {
            Point origPoint = new Point(newPoint.x, newPoint.x);

            float distance = (float) Math.sqrt(Math.pow((origPoint.x - newPoint.x), 2.0) + Math.pow((origPoint.y - newPoint.y), 2.0));

            if (origPoint.y > newPoint.y) {
                distance = -distance;
            }

            preparedSplinePoints.add(distance);
        }

        return preparedSplinePoints;
    }

    private ArrayList<Point> createSplineCurve2(Point[] points) {
        ArrayList<Double> sdA = createSecondDerivative(points);

        // Is [points count] equal to [sdA count]?
//    int n = [points count];
        int n = sdA.size();
        if (n < 1) {
            return null;
        }
        double sd[] = new double[n];

        // From NSMutableArray to sd[n];
        for (int i = 0; i < n; i++) {
            sd[i] = sdA.get(i);
        }


        ArrayList<Point> output = new ArrayList<Point>(n + 1);

        for (int i = 0; i < n - 1; i++) {
            Point cur = points[i];
            Point next = points[i + 1];

            for (int x = cur.x; x < next.x; x++) {
                double t = (double) (x - cur.x) / (next.x - cur.x);

                double a = 1 - t;
                double b = t;
                double h = next.x - cur.x;

                double y = a * cur.y + b * next.y + (h * h / 6) * ((a * a * a - a) * sd[i] + (b * b * b - b) * sd[i + 1]);

                if (y > 255.0) {
                    y = 255.0;
                } else if (y < 0.0) {
                    y = 0.0;
                }

                output.add(new Point(x, (int) Math.round(y)));
            }
        }

        // If the last point is (255, 255) it doesn't get added.
        if (output.size() == 255) {
            output.add(points[points.length - 1]);
        }
        return output;
    }

    private ArrayList<Double> createSecondDerivative(Point[] points) {
        int n = points.length;
        if (n <= 1) {
            return null;
        }

        double matrix[][] = new double[n][3];
        double result[] = new double[n];
        matrix[0][1] = 1;
        // What about matrix[0][1] and matrix[0][0]? Assuming 0 for now (Brad L.)
        matrix[0][0] = 0;
        matrix[0][2] = 0;

        for (int i = 1; i < n - 1; i++) {
            Point P1 = points[i - 1];
            Point P2 = points[i];
            Point P3 = points[i + 1];

            matrix[i][0] = (double) (P2.x - P1.x) / 6;
            matrix[i][1] = (double) (P3.x - P1.x) / 3;
            matrix[i][2] = (double) (P3.x - P2.x) / 6;
            result[i] = (double) (P3.y - P2.y) / (P3.x - P2.x) - (double) (P2.y - P1.y) / (P2.x - P1.x);
        }

        // What about result[0] and result[n-1]? Assuming 0 for now (Brad L.)
        result[0] = 0;
        result[n - 1] = 0;

        matrix[n - 1][1] = 1;
        // What about matrix[n-1][0] and matrix[n-1][2]? For now, assuming they are 0 (Brad L.)
        matrix[n - 1][0] = 0;
        matrix[n - 1][2] = 0;

        // solving pass1 (up->down)
        for (int i = 1; i < n; i++) {
            double k = matrix[i][0] / matrix[i - 1][1];
            matrix[i][1] -= k * matrix[i - 1][2];
            matrix[i][0] = 0;
            result[i] -= k * result[i - 1];
        }
        // solving pass2 (down->up)
        for (int i = n - 2; i >= 0; i--) {
            double k = matrix[i][2] / matrix[i + 1][1];
            matrix[i][1] -= k * matrix[i + 1][0];
            matrix[i][2] = 0;
            result[i] -= k * result[i + 1];
        }

        ArrayList<Double> output = new ArrayList<Double>(n);
        for (int i = 0; i < n; i++) output.add(result[i] / matrix[i][1]);

        return output;
    }
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

