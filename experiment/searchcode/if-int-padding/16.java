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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.util.List;
import javax.swing.Renderer;

/**
 *
 * @author kosuke
 */
public interface GraphLayoutManager extends LayoutManager2 {

    public GraphPaneLayoutInfo getLayoutInfo(Graph cp);
}

class DefaultGraphLayoutManager implements GraphLayoutManager {

    private GraphPaneLayoutInfo cpr;

    @Override
    public void addLayoutComponent(Component cmpnt, Object o) {
        //DO nothing
    }

    @Override
    public void addLayoutComponent(String string, Component cmpnt) {
        //DO nothing
    }

    @Override
    public float getLayoutAlignmentX(Container cntnr) {
        return 0;
    }

    @Override
    public float getLayoutAlignmentY(Container cntnr) {
        return 0;
    }

    @Override
    public void invalidateLayout(Container cntnr) {
        //clear cache.
        cpr = null;
    }

    @Override
    public void removeLayoutComponent(Component cmpnt) {
        //Do nothing
    }

    @Override
    public GraphPaneLayoutInfo getLayoutInfo(Graph cp) {
        if (null == cpr) {
            if (cp.getLayoutType() == Graph.LayoutType.CANVAS) {
                GraphCanvas canvas = cp.getCanvas();
                if (null == canvas) {
                    return null; //TODO: throw exception.
                }
                final int parentWidth = cp.getWidth();
                final int parentHeight = cp.getHeight();

                final Renderer r = cp.getLabelRenderer();
                final Transformer2D trans = canvas.getTransformer();
                final Dimension dimCanvas = canvas.getPreferredSize();
                final Dimension dimZero = new Dimension(0, 0);

                int labelXHeight = 0;
                int labelXMarginLeft = 0;
                int labelXMarginRight = 0;
                int labelYWidth = 0;
                int labelYMarginTop = 0;
                int labelYMarginBottom = 0;
                Dimension dimTitleX = dimZero;
                Dimension dimTitleY = dimZero;
                Dimension dimTitle = dimZero;

                if (cp.isLabelXVisible()) {
                    List<Axis.Entry> marksX = canvas.getAxisX().getMarkList();
                    labelXHeight = getLabelXHeight(marksX, r);
                    final Axis.Entry first = marksX.get(0);
                    final Axis.Entry last = marksX.get(marksX.size() - 1);
                    final Dimension dimFirstLabelX = getLabelDimension(first.v, r);
                    final Dimension dimLastLabelX = getLabelDimension(last.v, r);
//                    final int offsetFirst = (int) trans.toViewX(first.k.doubleValue());
//                    final int offsetLast = (int) trans.toViewX(last.k.doubleValue());
//                    labelXMarginLeft = Math.min(0, - dimFirstLabelX.width / 2 + offsetFirst);
//                    labelXMarginRight = dimLastLabelX.width / 2 + offsetLast;
                    labelXMarginLeft = dimFirstLabelX.width / 2;
                    labelXMarginRight = dimLastLabelX.width / 2;
                }

                if (cp.isLabelYVisible()) {
                    List<Axis.Entry> marksY = canvas.getAxisY().getMarkList();
                    labelYWidth = getLabelYWidth(marksY, r);
                    final Dimension dimFirstLabelY =
                            getLabelDimension(marksY.get(0).v, r);
                    final Dimension dimLastLabelY =
                            getLabelDimension(marksY.get(marksY.size() - 1).v, r);
                    labelYMarginBottom = dimFirstLabelY.height / 4; //TODO: 2 instead
                    labelYMarginTop = dimLastLabelY.height / 2;
                }

                if (null != cp.getTitleY() && cp.getTitleY().isVisible()) {
                    final Dimension dim = cp.getTitleY().getPreferredSize();
                    //Rotate
                    dimTitleY = new Dimension(dim.height, dim.width);
                }

                if (null != cp.getTitleX() && cp.getTitleX().isVisible()) {
                    dimTitleX = cp.getTitleX().getPreferredSize();
                }

                if (null != cp.getTitle() && cp.getTitle().isVisible()) {
                    dimTitle = cp.getTitle().getPreferredSize();
                }
                final int padding = 1;
                final int canvasOffsetX = Math.max(labelXMarginLeft, labelYWidth + padding);
                final int canvasOffsetY = labelYMarginTop;

                final int mainWidth = canvasOffsetX + dimCanvas.width + labelXMarginRight;
                final int mainHeight = canvasOffsetY + dimCanvas.height + labelYMarginBottom
                        + padding + labelXHeight;
                final int width = dimTitleY.width + padding + mainWidth;
                final int height = dimTitle.height + padding + mainHeight
                        + padding + dimTitleX.height;

                final Rectangle zeroRect = new Rectangle(0, 0, 0, 0);
                Rectangle titleRect = null != cp.getTitle() && cp.getTitle().isVisible()
                        ? new Rectangle(0, 0, width, dimTitle.height)
                        : zeroRect;
                Rectangle titleYRect = null != cp.getTitleY() && cp.getTitleY().isVisible()
                        ? new Rectangle(0, titleRect.height + padding, dimTitleY.width, mainHeight)
                        : zeroRect;
                Rectangle mainRect = new Rectangle(titleYRect.width + padding,
                        titleRect.height + padding, mainWidth, mainHeight);
                Rectangle titleXRect = null != cp.getTitleX() && cp.getTitleX().isVisible()
                        ? new Rectangle(mainRect.x,
                        mainRect.y + mainRect.height + padding,
                        mainWidth, dimTitleX.height)
                        : zeroRect;
                Rectangle canvasRect = new Rectangle(mainRect.x + canvasOffsetX,
                        mainRect.y + canvasOffsetY, canvas.getPreferredSize().width,
                        canvas.getPreferredSize().height);
                Rectangle labelYRect = cp.isLabelYVisible()
                        ? new Rectangle(mainRect.x, mainRect.y, labelYWidth,
                        canvasRect.height + labelYMarginTop + labelYMarginBottom)
                        : zeroRect;
                Rectangle labelXRect = cp.isLabelXVisible()
                        ? new Rectangle(canvasRect.x - labelXMarginLeft,
                        canvasRect.y + canvasRect.height + labelYMarginBottom + padding,
                        canvasRect.width + labelXMarginLeft + labelXMarginRight,
                        labelXHeight)
                        : zeroRect;

                this.cpr = new GraphPaneLayoutInfo.Builder(width, height).
                        title(titleRect).titleY(titleYRect).titleX(titleXRect).
                        main(mainRect).canvas(canvasRect).labelY(labelYRect).
                        labelX(labelXRect).create();
            } else {
                throw new UnsupportedOperationException();

            }
        }
        return cpr;
    }

    @Override
    public Dimension maximumLayoutSize(Container cntnr) {
        if (cntnr instanceof Graph) {
            Graph cp = (Graph) cntnr;
            if (cp.getLayoutType() == Graph.LayoutType.CANVAS) {
                GraphPaneLayoutInfo region = getLayoutInfo(cp);
                return new Dimension(region.width, region.height);
            } else {
                throw new UnsupportedOperationException();
            }
        }
        throw new UnsupportedOperationException();
    }

    // Graph.getPreferredSize() -> 
    @Override
    public Dimension preferredLayoutSize(Container cntnr) {
        if (cntnr instanceof Graph) {
            Graph cp = (Graph) cntnr;
            if (cp.getLayoutType() == Graph.LayoutType.CANVAS) {
                GraphPaneLayoutInfo region = getLayoutInfo(cp);
                return new Dimension(region.width, region.height);
            } else {
                throw new UnsupportedOperationException();
            }
        }
        throw new UnsupportedOperationException();
    }

    // Graph.getMinimumSize() -> 
    @Override
    public Dimension minimumLayoutSize(Container cntnr) {
        if (cntnr instanceof Graph) {
            Graph cp = (Graph) cntnr;
            if (cp.getLayoutType() == Graph.LayoutType.CANVAS) {
                GraphPaneLayoutInfo region = getLayoutInfo(cp);
                return new Dimension(region.width, region.height);
            } else {
                throw new UnsupportedOperationException();
            }
        }
        throw new UnsupportedOperationException();
    }

    /**
     * Configure the layout, calling GraphCanvas.setBounds(). Prepares 
     * GraphPaneLayoutInfo as well.
     * @param canvasPane 
     */
    @Override
    public void layoutContainer(Container parent) {
        if (parent instanceof Graph) {
            Graph cp = (Graph) parent;
            GraphPaneLayoutInfo region = getLayoutInfo(cp);
            // main region
            cp.getCanvas().setBounds(region.canvasRect);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private int getLabelXHeight(List<Axis.Entry> marks, Renderer r) {
        int maxHeight = 0;
        for (Axis.Entry e : marks) {
            int h = getLabelDimension(e.v, r).height;
            if (maxHeight < h) {
                maxHeight = h;
            }
        }
        return maxHeight;
    }

    private int getLabelYWidth(List<Axis.Entry> marks, Renderer r) {
        int maxWidth = 0;
        for (Axis.Entry e : marks) {
            int w = getLabelDimension(e.v, r).width;
            if (maxWidth < w) {
                maxWidth = w;
            }
        }
        return maxWidth;
    }

    private Dimension getLabelDimension(String s, Renderer r) {
        r.setValue(s, true);
        Component c = r.getComponent();
        return c.getPreferredSize();
    }
}
