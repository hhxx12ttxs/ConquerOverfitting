/*
 * The MIT License
 *
 * Copyright 2011 kosuke.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package shoganai.graph;

import java.awt.Rectangle;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.nio.FloatBuffer;
import shoganai.graph.GraphModel.PlotData;

/**
 * 2D scaling transform that perform a mapping from graph coordinates
 * to screen coordinates. Used by GraphCanvas class. 
 * @author kosuke
 */
public abstract class Transformer2D {

    /**
     * Tuple class represents the range of graph coordinates.
     */
    public final class Range {

        double minX, minY, maxX, maxY;

        Range (double minX, double minY, double maxX, double maxY) {
            set(minX, minY, maxX, maxY);
        }
        
        void set(double minX, double minY, double maxX, double maxY) {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }        
    }
    
    public static final String PROP_BOUNDS = "bounds";
    public static final String PROP_RANGE = "range";
    protected Rectangle bounds = new Rectangle(0, 0, 10, 10);
    protected Range range = new Range(0, 0, 10, 10);
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /**
     * Sets the screen bounding rectangle.
     * @param x
     * @param y
     * @param width
     * @param height 
     */
    public void setBounds(int x, int y, int width, int height) {
        assert x >= 0 && y >= 0 && width >= 0 && height >= 0;
        Rectangle oldValue = this.bounds;
        this.bounds = new Rectangle(x, y, width, height);
        pcs.firePropertyChange(PROP_BOUNDS, oldValue, bounds);
    }

    /**
     * Sets the region of graph that is mapped to the corresponding bounds on 
     * the screen.
     * @param minX
     * @param minY
     * @param maxX
     * @param maxY 
     */
    public void setRange(double minX, double minY, double maxX, double maxY) {
        assert minX < maxX && minY < maxY;
        Rectangle2D.Double oldValue =
                new Rectangle2D.Double(range.minX, range.minY,
                range.maxX, range.maxY);
        range.set(minX, minY, maxX, maxY);
        pcs.firePropertyChange(PROP_RANGE, oldValue, range);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public abstract double toViewX(double x);

    public abstract double toViewY(double y);

    public abstract double toModelX(double x);

    public abstract double toModelY(double y);

    public abstract Path2D.Float createPath(PlotData data);

    /**
     * 
     */
    public static class Linear extends Transformer2D {

        @Override
        public double toViewX(double x) {
            final double w = bounds.width - 1;
            return (x - range.minX) * w / (range.maxX - range.minX);
        }

        @Override
        public double toViewY(double y) {
            final double h = bounds.height - 1;
            return h - (y - range.minY) * h / (range.maxY - range.minY);
        }

        @Override
        public double toModelX(double x) {
            final double w = bounds.width - 1;
            return (x - toViewX(0.0)) * (range.maxX - range.minX) / w;
        }

        @Override
        public double toModelY(double y) {
            double h = (double) (bounds.height - 1);
            return (toViewY(0.0) - y) * (range.maxY - range.minY) / h;
        }

        @Override
        public Path2D.Float createPath(PlotData data) {
            if (data.isIndexed()) {
                //TODO: implement
                throw new UnsupportedOperationException();
            } else {
                FloatBuffer bufX = data.getBufferX();
                FloatBuffer bufY = data.getBufferY();
                if (null != bufX && null != bufY) {
                    if (!bufX.hasArray() || !bufY.hasArray()) {
                        throw new UnsupportedOperationException();
                    } else {
                        return createPath(bufX.array(), bufY.array(), data.size());
                    }
                } else {
                    //TODO: implement
                    throw new UnsupportedOperationException();
                }
            }
        }

        private Path2D.Float createPath(float[] xx, float[] yy, int size) {
            final double minX = range.minX;
            final double minY = range.minY;
            final double maxX = range.maxX;
            final double maxY = range.maxY;
            final double W = maxX - minX;
            final double H = maxY - minY;
            final double wx = (bounds.width - 1) / W;
            final double hy = (bounds.height - 1) / H;

            Path2D.Float path = new Path2D.Float();
            int start = -1;
            for (int i = 1; i < size; ++i) {
                if (xx[i] > minX) {
                    start = i - 1;
                    break;
                }
            }
            if (start < 0) {
                return path;
            }

            int end = xx.length - 1;
            for (int i = start; i < size; ++i) {
                if (xx[i] > maxX) {
                    end = i;
                    break;
                }
            }
            if (end < 0) {
                return path;
            }

            int prevX = (int) ((xx[start] - minX) * wx);
            int prevY = (int) (bounds.height - (yy[start] - minY) * hy);
            int maximumY = prevY;
            int minimumY = prevY;

            path.moveTo(prevX, minimumY);
            for (int i = start; i <= end; ++i) {
                int x = (int) ((xx[i] - minX) * wx);
                int y = (int) (bounds.height - (yy[i] - minY) * hy);

                //TODO: int
                if (prevX != x) {
                    path.lineTo(prevX, minimumY);
                    path.lineTo(prevX, maximumY);
                    path.lineTo(prevX, prevY);
                    path.lineTo(x, y);

                    minimumY = y;
                    maximumY = y;
                } else {
                    if (y < minimumY) {
                        minimumY = y;
                    } else if (y > maximumY) {
                        maximumY = y;
                    }
                }
                prevX = x;
                prevY = y;
            }

            return path;
        }
    }

    /**
     * 
     */
    public static class Log10Y extends Transformer2D {

        @Override
        public double toViewX(double x) {
            final double w = bounds.width - 1;
            return (x - range.minX) * w / (range.maxX - range.minX);
        }

        @Override
        public double toViewY(double y) {
            double h = (double) (bounds.height - 1);
            return h - h * (Math.log10(y / range.minY)) / Math.log10(range.maxY / range.minY);
        }

        @Override
        public double toModelX(double x) {
            final double w = bounds.width - 1;
            return (x - toViewX(0.0)) * (range.maxX - range.minX) / w;
        }

        @Override
        public double toModelY(double y) {
            double h = (bounds.height - 1);
            return Math.pow(10.0, (toViewY(0.0) - y) * (range.maxY - range.minY) / h);
        }

        @Override
        public Path2D.Float createPath(PlotData data) {
            if (data.isIndexed()) {
                //TODO: implement
                throw new UnsupportedOperationException();
            } else {
                FloatBuffer bufX = data.getBufferX();
                FloatBuffer bufY = data.getBufferY();
                if (null != bufX && null != bufY) {
                    if (!bufX.hasArray() || !bufY.hasArray()) {
                        throw new UnsupportedOperationException();
                    } else {
                        return createPath(bufX.array(), bufY.array(), data.size());
                    }
                } else {
                    //TODO: implement
                    throw new UnsupportedOperationException();
                }
            }
        }

        private Path2D.Float createPath(float[] xx, float[] yy, int size) {
            final double minX = range.minX;
            final double minY = range.minY;
            final double maxX = range.maxX;
            final double maxY = range.maxY;
            final double W = maxX - minX;
            final double H = Math.log10(maxY / minY);
            final double wx = (bounds.width - 1) / W;
            final double hy = (bounds.height - 1) / H;

            Path2D.Float path = new Path2D.Float();
            int start = -1;
            for (int i = 1; i < size; ++i) {
                if (xx[i] > minX) {
                    start = i - 1;
                    break;
                }
            }
            if (start < 0) {
                return path;
            }

            int end = xx.length - 1;
            for (int i = start; i < size; ++i) {
                if (xx[i] > maxX) {
                    end = i;
                    break;
                }
            }
            if (end < 0) {
                return path;
            }

            int prevX = (int) ((xx[start] - minX) * wx);
            int prevY = (int) (bounds.height - (Math.log10(yy[start] / minY)) * hy);
            int maximumY = prevY;
            int minimumY = prevY;

            path.moveTo(prevX, minimumY);
            for (int i = start; i <= end; ++i) {
                int x = (int) ((xx[i] - minX) * wx);
                int y = (int) (bounds.height - (Math.log10(yy[i] / minY)) * hy);

                //TODO: int
                if (prevX != x) {
                    path.lineTo(prevX, minimumY);
                    path.lineTo(prevX, maximumY);
                    path.lineTo(prevX, prevY);
                    path.lineTo(x, y);

                    minimumY = y;
                    maximumY = y;
                } else {
                    if (y < minimumY) {
                        minimumY = y;
                    } else if (y > maximumY) {
                        maximumY = y;
                    }
                }
                prevX = x;
                prevY = y;
            }

            return path;
        }
    }
}

