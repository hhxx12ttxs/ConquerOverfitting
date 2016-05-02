<<<<<<< HEAD
/*
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package javafx.scene.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point3D;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.shape.Shape;
import com.sun.javafx.Logging;
import com.sun.javafx.TempState;
import com.sun.javafx.binding.ExpressionHelper;
import com.sun.javafx.css.converters.BooleanConverter;
import com.sun.javafx.css.converters.InsetsConverter;
import com.sun.javafx.css.converters.ShapeConverter;
import com.sun.javafx.css.converters.SizeConverter;
import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.PickRay;
import com.sun.javafx.geom.Vec2d;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.scene.DirtyBits;
import com.sun.javafx.scene.input.PickResultChooser;
import com.sun.javafx.sg.PGNode;
import com.sun.javafx.sg.PGRegion;
import com.sun.javafx.tk.Toolkit;
import javafx.beans.value.ObservableValue;
import javafx.css.StyleOrigin;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.util.Callback;
import sun.util.logging.PlatformLogger;

/**
 * Region is the base class for all JavaFX Node-based UI Controls, and all layout containers.
 * It is a resizable Parent node which can be styled from CSS. It can have multiple backgrounds
 * and borders. It is designed to support as much of the CSS3 specification for backgrounds
 * and borders as is relevant to JavaFX.
 * The full specification is available at <a href="http://www.w3.org/TR/2012/CR-css3-background-20120724/">the W3C</a>.
 * <p/>
 * Every Region has its layout bounds, which are specified to be (0, 0, width, height). A Region might draw outside
 * these bounds. The content area of a Region is the area which is occupied for the layout of its children.
 * This area is, by default, the same as the layout bounds of the Region, but can be modified by either the
 * properties of a border (either with BorderStrokes or BorderImages), and by padding. The padding can
 * be negative, such that the content area of a Region might extend beyond the layout bounds of the Region,
 * but does not affect the layout bounds.
 * <p/>
 * A Region has a Background, and a Border, although either or both of these might be empty. The Background
 * of a Region is made up of zero or more BackgroundFills, and zero or more BackgroundImages. Likewise, the
 * border of a Region is defined by its Border, which is made up of zero or more BorderStrokes and
 * zero or more BorderImages. All BackgroundFills are drawn first, followed by BackgroundImages, BorderStrokes,
 * and finally BorderImages. The content is drawn above all backgrounds and borders. If a BorderImage is
 * present (and loaded all images properly), then no BorderStrokes are actually drawn, although they are
 * considered for computing the position of the content area (see the stroke width property of a BorderStroke).
 * These semantics are in line with the CSS 3 specification. The purpose of these semantics are to allow an
 * application to specify a fallback BorderStroke to be displayed in the case that an ImageStroke fails to
 * download or load.
 * <p/>
 * By default a Region appears as a Rectangle. A BackgroundFill radii might cause the Rectangle to appear rounded.
 * This affects not only making the visuals look like a rounded rectangle, but it also causes the picking behavior
 * of the Region to act like a rounded rectangle, such that locations outside the corner radii are ignored. A
 * Region can be made to use any shape, however, by specifing the {@code shape} property. If a shape is specified,
 * then all BackgroundFills, BackgroundImages, and BorderStrokes will be applied to the shape. BorderImages are
 * not used for Regions which have a shape specified.
 * <p/>
 * A Region with a shape
 * <p/>
 * Although the layout bounds of a Region are not influenced by any Border or Background, the content area
 * insets and the picking area of the Region are. The {@code insets} of the Region define the distance
 * between the edge of the layout bounds and the edge of the content area. For example, if the Region
 * layout bounds are (x=0, y=0, width=200, height=100), and the insets are (top=10, right=20, bottom=30, left=40),
 * then the content area bounds will be (x=40, y=10, width=140, height=60). A Region subclass which is laying
 * out its children should compute and honor these content area bounds.
 * <p/>
 * By default a Region inherits the layout behavior of its superclass, {@link Parent},
 * which means that it will resize any resizable child nodes to their preferred
 * size, but will not reposition them.  If an application needs more specific
 * layout behavior, then it should use one of the Region subclasses:
 * {@link StackPane}, {@link HBox}, {@link VBox}, {@link TilePane}, {@link FlowPane},
 * {@link BorderPane}, {@link GridPane}, or {@link AnchorPane}.
 * <p/>
 * To implement a more custom layout, a Region subclass must override
 * {@link #computePrefWidth(double) computePrefWidth}, {@link #computePrefHeight(double) computePrefHeight}, and
 * {@link #layoutChildren() layoutChildren}. Note that {@link #layoutChildren() layoutChildren} is called automatically
 * by the scene graph while executing a top-down layout pass and it should not be invoked directly by the
 * region subclass.
 * <p/>
 * Region subclasses which layout their children will position nodes by setting
 * {@link #setLayoutX(double) layoutX}/{@link #setLayoutY(double) layoutY} and do not alter
 * {@link #setTranslateX(double) translateX}/{@link #setTranslateY(double) translateY}, which are reserved for
 * adjustments and animation.
 * @since JavaFX 2.0
 */
public class Region extends Parent {

    /**
     * Sentinel value which can be passed to a region's
     * {@link #setMinWidth(double) setMinWidth},
     * {@link #setMinHeight(double) setMinHeight},
     * {@link #setMaxWidth(double) setMaxWidth} or
     * {@link #setMaxHeight(double) setMaxHeight}
     * methods to indicate that the preferred dimension should be used for that max and/or min constraint.
     */
    public static final double USE_PREF_SIZE = Double.NEGATIVE_INFINITY;

    /**
     * Sentinel value which can be passed to a region's
     * {@link #setMinWidth(double) setMinWidth},
     * {@link #setMinHeight(double) setMinHeight},
     * {@link #setPrefWidth(double) setPrefWidth},
     * {@link #setPrefHeight(double) setPrefHeight},
     * {@link #setMaxWidth(double) setMaxWidth},
     * {@link #setMaxHeight(double) setMaxHeight} methods
     * to reset the region's size constraint back to it's intrinsic size returned
     * by {@link #computeMinWidth(double) computeMinWidth}, {@link #computeMinHeight(double) computeMinHeight},
     * {@link #computePrefWidth(double) computePrefWidth}, {@link #computePrefHeight(double) computePrefHeight},
     * {@link #computeMaxWidth(double) computeMaxWidth}, or {@link #computeMaxHeight(double) computeMaxHeight}.
     */
    public static final double USE_COMPUTED_SIZE = -1;

    static Vec2d TEMP_VEC2D = new Vec2d();

    /***************************************************************************
     *                                                                         *
     * Static convenience methods for layout                                   *
     *                                                                         *
     **************************************************************************/

    /**
     * Computes the value based on the given min and max values. We encode in this
     * method the logic surrounding various edge cases, such as when the min is
     * specified as greater than the max, or the max less than the min, or a pref
     * value that exceeds either the max or min in their extremes.
     * <p/>
     * If the min is greater than the max, then we want to make sure the returned
     * value is the min. In other words, in such a case, the min becomes the only
     * acceptable return value.
     * <p/>
     * If the min and max values are well ordered, and the pref is less than the min
     * then the min is returned. Likewise, if the values are well ordered and the
     * pref is greater than the max, then the max is returned. If the pref lies
     * between the min and the max, then the pref is returned.
     *
     *
     * @param min The minimum bound
     * @param pref The value to be clamped between the min and max
     * @param max the maximum bound
     * @return the size bounded by min, pref, and max.
     */
    static double boundedSize(double min, double pref, double max) {
        double a = pref >= min ? pref : min;
        double b = min >= max ? min : max;
        return a <= b ? a : b;
    }

    double adjustWidthByMargin(double width, Insets margin) {
        if (margin == null || margin == Insets.EMPTY) {
            return width;
        }
        boolean isSnapToPixel = isSnapToPixel();
        return width - snapSpace(margin.getLeft(), isSnapToPixel) - snapSpace(margin.getRight(), isSnapToPixel);
    }

    double adjustHeightByMargin(double height, Insets margin) {
        if (margin == null || margin == Insets.EMPTY) {
            return height;
        }
        boolean isSnapToPixel = isSnapToPixel();
        return height - snapSpace(margin.getTop(), isSnapToPixel) - snapSpace(margin.getBottom(), isSnapToPixel);
    }

    /**
     * If snapToPixel is true, then the value is rounded using Math.round. Otherwise,
     * the value is simply returned. This method will surely be JIT'd under normal
     * circumstances, however on an interpreter it would be better to inline this
     * method. However the use of Math.round here, and Math.ceil in snapSize is
     * not obvious, and so for code maintenance this logic is pulled out into
     * a separate method.
     *
     * @param value The value that needs to be snapped
     * @param snapToPixel Whether to snap to pixel
     * @return value either as passed in or rounded based on snapToPixel
     */
    private static double snapSpace(double value, boolean snapToPixel) {
        return snapToPixel ? Math.round(value) : value;
    }

    /**
     * If snapToPixel is true, then the value is ceil'd using Math.ceil. Otherwise,
     * the value is simply returned.
     *
     * @param value The value that needs to be snapped
     * @param snapToPixel Whether to snap to pixel
     * @return value either as passed in or ceil'd based on snapToPixel
     */
    private static double snapSize(double value, boolean snapToPixel) {
        return snapToPixel ? Math.ceil(value) : value;
    }

    /**
     * If snapToPixel is true, then the value is rounded using Math.round. Otherwise,
     * the value is simply returned.
     *
     * @param value The value that needs to be snapped
     * @param snapToPixel Whether to snap to pixel
     * @return value either as passed in or rounded based on snapToPixel
     */
    private static double snapPosition(double value, boolean snapToPixel) {
        return snapToPixel ? Math.round(value) : value;
    }

    private static double snapPortion(double value, boolean snapToPixel) {
        if (snapToPixel) {
            return (value > 0 ? Math.max(1, Math.floor(value)) : Math.min(-1, Math.ceil(value)));
        }
        return value;
    }

    static double getMaxAreaBaselineOffset(List<Node> content, Callback<Node, Insets> margins) {
        double max = 0;
        for (int i = 0, maxPos = content.size(); i < maxPos; i++) {
            final Node node = content.get(i);
            final Insets margin = margins.call(node);
            final double topMargin = margin != null ? margin.getTop() : 0;
            final double position = topMargin + node.getBaselineOffset();
            max = max >= position ? max : position; // Math.max
        }
        return max;
    }

    static double getMaxBaselineOffset(List<Node> content) {
        double max = 0;
        for (int i = 0, maxPos = content.size(); i < maxPos; i++) {
            final Node node = content.get(i);
            final double baselineOffset = node.getBaselineOffset();
            max = max >= baselineOffset ? max : baselineOffset; // Math.max
        }
        return max;
    }

    static double computeXOffset(double width, double contentWidth, HPos hpos) {
        switch(hpos) {
            case LEFT:
                return 0;
            case CENTER:
                return (width - contentWidth) / 2;
            case RIGHT:
                return width - contentWidth;
            default:
                throw new AssertionError("Unhandled hPos");
        }
    }

    static double computeYOffset(double height, double contentHeight, VPos vpos) {
        switch(vpos) {
            case BASELINE:
            case TOP:
                return 0;
            case CENTER:
                return (height - contentHeight) / 2;
            case BOTTOM:
                return height - contentHeight;
            default:
                throw new AssertionError("Unhandled vPos");
        }
    }

    static double[] createDoubleArray(int length, double value) {
        double[] array = new double[length];
        for (int i = 0; i < length; i++) {
            array[i] = value;
        }
        return array;
    }


    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    /**
     * Creates a new Region with an empty Background and and empty Border. The
     * Region defaults to having pickOnBounds set to true, meaning that any pick
     * (mouse picking or touch picking etc) that occurs within the bounds in local
     * of the Region will return true, regardless of whether the Region is filled
     * or transparent.
     */
    public Region() {
        super();
        setPickOnBounds(true);
    }

    /***************************************************************************
     *                                                                         *
     * Region properties                                                       *
     *                                                                         *
     **************************************************************************/

    /**
     * Defines whether this region adjusts position, spacing, and size values of
     * its children to pixel boundaries. This defaults to true, which is generally
     * the expected behavior in order to have crisp user interfaces. A value of
     * false will allow for fractional alignment, which may lead to "fuzzy"
     * looking borders.
     */
    private BooleanProperty snapToPixel;
    /**
     * I'm using a super-lazy property pattern here, so as to only create the
     * property object when needed for listeners or when being set from CSS,
     * but also making sure that we only call requestParentLayout in the case
     * that the snapToPixel value has actually changed, whether set via the setter
     * or set via the property object.
     */
    private boolean _snapToPixel = true;
    public final boolean isSnapToPixel() { return _snapToPixel; }
    public final void setSnapToPixel(boolean value) {
        if (snapToPixel == null) {
            if (_snapToPixel != value) {
                _snapToPixel = value;
                updateSnappedInsets();
                requestParentLayout();
            }
        } else {
            snapToPixel.set(value);
        }
    }
    public final BooleanProperty snapToPixelProperty() {
        // Note: snapToPixel is virtually never set, and never listened to.
        // Because of this, it works reasonably well as a lazy property,
        // since this logic is just about never going to be called.
        if (snapToPixel == null) {
            snapToPixel = new StyleableBooleanProperty(_snapToPixel) {
                @Override public Object getBean() { return Region.this; }
                @Override public String getName() { return "snapToPixel"; }
                @Override public CssMetaData<Region, Boolean> getCssMetaData() {
                    return StyleableProperties.SNAP_TO_PIXEL;
                }
                @Override public void invalidated() {
                    boolean value = get();
                    if (_snapToPixel != value) {
                        _snapToPixel = value;
                        updateSnappedInsets();
                        requestParentLayout();
                    }
                }
            };
        }
        return snapToPixel;
    }

    /**
     * The top, right, bottom, and left padding around the region's content.
     * This space will be included in the calculation of the region's
     * minimum and preferred sizes. By default padding is Insets.EMPTY. Setting the
     * value to null should be avoided.
     */
    private ObjectProperty<Insets> padding = new StyleableObjectProperty<Insets>(Insets.EMPTY) {
        // Keep track of the last valid value for the sake of
        // rollback in case padding is set to null. Note that
        // Richard really does not like this pattern because
        // it essentially means that binding the padding property
        // is not possible since a binding expression could very
        // easily produce an intermediate null value.

        // Also note that because padding is set virtually everywhere via CSS, and CSS
        // requires a property object in order to set it, there is no benefit to having
        // lazy initialization here.

        private Insets lastValidValue = Insets.EMPTY;

        @Override public Object getBean() { return Region.this; }
        @Override public String getName() { return "padding"; }
        @Override public CssMetaData<Region, Insets> getCssMetaData() {
            return StyleableProperties.PADDING;
        }
        @Override public void invalidated() {
            final Insets newValue = get();
            if (newValue == null) {
                // rollback
                if (isBound()) {
                    unbind();
                }
                set(lastValidValue);
                throw new NullPointerException("cannot set padding to null");
            } else if (!newValue.equals(lastValidValue)) {
                lastValidValue = newValue;
                insets.fireValueChanged();
            }
        }
    };
    public final void setPadding(Insets value) { padding.set(value); }
    public final Insets getPadding() { return padding.get(); }
    public final ObjectProperty<Insets> paddingProperty() { return padding; }

    /**
     * The background of the Region, which is made up of zero or more BackgroundFills, and
     * zero or more BackgroundImages. It is possible for a Background to be empty, where it
     * has neither fills nor images, and is semantically equivalent to null.
     * @since JavaFX 8.0
     */
    private final ObjectProperty<Background> background = new StyleableObjectProperty<Background>(null) {
        private Background old = null;
        @Override public Object getBean() { return Region.this; }
        @Override public String getName() { return "background"; }
        @Override public CssMetaData<Region, Background> getCssMetaData() {
            return StyleableProperties.BACKGROUND;
        }

        @Override protected void invalidated() {
            final Background b = get();
            if(old != null ? !old.equals(b) : b != null) {
                // They are different! Both cannot be null
                if (old == null || b == null || !old.getOutsets().equals(b.getOutsets())) {
                    // We have determined that the outsets of these two different background
                    // objects is different, and therefore the bounds have changed.
                    impl_geomChanged();
                    insets.fireValueChanged();
                }
                // No matter what, the fill has changed, so we have to update it
                impl_markDirty(DirtyBits.SHAPE_FILL);
                old = b;
            }
        }
    };
    public final void setBackground(Background value) { background.set(value); }
    public final Background getBackground() { return background.get(); }
    public final ObjectProperty<Background> backgroundProperty() { return background; }

    /**
     * The border of the Region, which is made up of zero or more BorderStrokes, and
     * zero or more BorderImages. It is possible for a Border to be empty, where it
     * has neither strokes nor images, and is semantically equivalent to null.
     * @since JavaFX 8.0
     */
    private final ObjectProperty<Border> border = new StyleableObjectProperty<Border>(null) {
        private Border old = null;
        @Override public Object getBean() { return Region.this; }
        @Override public String getName() { return "border"; }
        @Override public CssMetaData<Region, Border> getCssMetaData() {
            return StyleableProperties.BORDER;
        }
        @Override protected void invalidated() {
            final Border b = get();
            if(old != null ? !old.equals(b) : b != null) {
                // They are different! Both cannot be null
                if (old == null || b == null || !old.getOutsets().equals(b.getOutsets())) {
                    // We have determined that the outsets of these two different border
                    // objects is different, and therefore the bounds have changed.
                    impl_geomChanged();
                    insets.fireValueChanged();
                }
                // No matter what, the fill has changed, so we have to update it
                impl_markDirty(DirtyBits.SHAPE_STROKE);
                old = b;
            }
        }
    };
    public final void setBorder(Border value) { border.set(value); }
    public final Border getBorder() { return border.get(); }
    public final ObjectProperty<Border> borderProperty() { return border; }

    /**
     * Defines the area of the region within which completely opaque pixels
     * are drawn. This is used for various performance optimizations.
     * The pixels within this area MUST BE fully opaque, or rendering
     * artifacts will result. It is the responsibility of the application, either
     * via code or via CSS, to ensure that the opaqueInsets is correct for
     * a Region based on the backgrounds and borders of that region. The values
     * for each of the insets must be real numbers, not NaN or Infinity. If
     * no known insets exist, then the opaqueInsets should be set to null.
     * @since JavaFX 8.0
     */
    public final ObjectProperty<Insets> opaqueInsetsProperty() {
        if (opaqueInsets == null) {
            opaqueInsets = new StyleableObjectProperty<Insets>() {
                @Override public Object getBean() { return Region.this; }
                @Override public String getName() { return "opaqueInsets"; }
                @Override public CssMetaData<Region, Insets> getCssMetaData() {
                    return StyleableProperties.OPAQUE_INSETS;
                }
                @Override protected void invalidated() {
                    // This causes the background to be updated, which
                    // is the code block where we also compute the opaque insets
                    // since updating the background is super fast even when
                    // nothing has changed.
                    impl_markDirty(DirtyBits.SHAPE_FILL);
                }
            };
        }
        return opaqueInsets;
    }
    private ObjectProperty<Insets> opaqueInsets;
    public final void setOpaqueInsets(Insets value) { opaqueInsetsProperty().set(value); }
    public final Insets getOpaqueInsets() { return opaqueInsets == null ? null : opaqueInsets.get(); }

    /**
     * The insets of the Region define the distance from the edge of the region (its layout bounds,
     * or (0, 0, width, height)) to the edge of the content area. All child nodes should be laid out
     * within the content area. The insets are computed based on the Border which has been specified,
     * if any, and also the padding.
     * @since JavaFX 8.0
     */
    private final InsetsProperty insets = new InsetsProperty();
    public final Insets getInsets() { return insets.get(); }
    public final ReadOnlyObjectProperty<Insets> insetsProperty() { return insets; }
    private final class InsetsProperty extends ReadOnlyObjectProperty<Insets> {
        private Insets cache = null;
        private ExpressionHelper<Insets> helper = null;

        @Override public Object getBean() { return Region.this; }
        @Override public String getName() { return "insets"; }

        @Override public void addListener(InvalidationListener listener) {
            helper = ExpressionHelper.addListener(helper, this, listener);
        }

        @Override public void removeListener(InvalidationListener listener) {
            helper = ExpressionHelper.removeListener(helper, listener);
        }

        @Override public void addListener(ChangeListener<? super Insets> listener) {
            helper = ExpressionHelper.addListener(helper, this, listener);
        }

        @Override public void removeListener(ChangeListener<? super Insets> listener) {
            helper = ExpressionHelper.removeListener(helper, listener);
        }

        void fireValueChanged() {
            cache = null;
            updateSnappedInsets();
            requestLayout();
            ExpressionHelper.fireValueChangedEvent(helper);
        }

        @Override public Insets get() {
            // If a shape is specified, then we don't really care whether there are any borders
            // specified, since borders of shapes do not contribute to the insets.
            if (_shape != null) return getPadding();

            // If there is no border or the border has no insets itself, then the only thing
            // affecting the insets is the padding, so we can just return it directly.
            final Border b = getBorder();
            if (b == null || Insets.EMPTY.equals(b.getInsets())) {
                return getPadding();
            }

            // There is a border with some non-zero insets and we do not have a _shape, so we need
            // to take the border's insets into account
            if (cache == null) {
                // Combine the padding and the border insets.
                // TODO note that negative border insets were being ignored, but
                // I'm not sure that that made sense or was reasonable, so I have
                // changed it so that we just do simple math.
                // TODO Stroke borders should NOT contribute to the insets. Ensure via tests.
                final Insets borderInsets = b.getInsets();
                final Insets paddingInsets = getPadding();
                cache = new Insets(
                        borderInsets.getTop() + paddingInsets.getTop(),
                        borderInsets.getRight() + paddingInsets.getRight(),
                        borderInsets.getBottom() + paddingInsets.getBottom(),
                        borderInsets.getLeft() + paddingInsets.getLeft()
                );
            }
            return cache;
        }
    };

    /**
     * cached results of snapped insets, this are used a lot during layout so makes sense
     * to keep fast access cached copies here.
     */
    private double snappedTopInset = 0;
    private double snappedRightInset = 0;
    private double snappedBottomInset = 0;
    private double snappedLeftInset = 0;

    /** Called to update the cached snapped insets */
    private void updateSnappedInsets() {
        final Insets insets = getInsets();
        if (_snapToPixel) {
            snappedTopInset = Math.ceil(insets.getTop());
            snappedRightInset = Math.ceil(insets.getRight());
            snappedBottomInset = Math.ceil(insets.getBottom());
            snappedLeftInset = Math.ceil(insets.getLeft());
        } else {
            snappedTopInset = insets.getTop();
            snappedRightInset = insets.getRight();
            snappedBottomInset = insets.getBottom();
            snappedLeftInset = insets.getLeft();
        }
    }

    /**
    * The width of this resizable node.  This property is set by the region's parent
    * during layout and may not be set by the application.  If an application
    * needs to explicitly control the size of a region, it should override its
    * preferred size range by setting the <code>minWidth</code>, <code>prefWidth</code>,
    * and <code>maxWidth</code> properties.
    */
    private ReadOnlyDoubleWrapper width;

    /**
     * Because the width is very often set and very often read but only sometimes
     * listened to, it is beneficial to use the super-lazy pattern property, where we
     * only inflate the property object when widthProperty() is explicitly invoked.
     */
    private double _width;

    // Note that it is OK for this method to be protected so long as the width
    // property is never bound. Only Region could do so because only Region has
    // access to a writable property for "width", but since there is now a protected
    // set method, it is impossible for Region to ever bind this property.
    protected void setWidth(double value) {
        if(width == null) {
            widthChanged(value);
        } else {
            width.set(value);
        }
    }

    private void widthChanged(double value) {
        // It is possible that somebody sets the width of the region to a value which
        // it previously held. If this is the case, we want to avoid excessive layouts.
        // Note that I have biased this for layout over binding, because the widthProperty
        // is now going to recompute the width eagerly. The cost of excessive and
        // unnecessary bounds changes, however, is relatively high.
        if (value != _width) {
            _width = value;
            boundingBox = null;
            impl_layoutBoundsChanged();
            impl_geomChanged();
            impl_markDirty(DirtyBits.NODE_GEOMETRY);
            setNeedsLayout(true);
            requestParentLayout();
        }
    }

    public final double getWidth() { return width == null ? _width : width.get(); }

    public final ReadOnlyDoubleProperty widthProperty() {
        if (width == null) {
            width = new ReadOnlyDoubleWrapper(_width) {
                @Override protected void invalidated() { widthChanged(get()); }
                @Override public Object getBean() { return Region.this; }
                @Override public String getName() { return "width"; }
            };
        }
        return width.getReadOnlyProperty();
    }

    /**
     * The height of this resizable node.  This property is set by the region's parent
     * during layout and may not be set by the application.  If an application
     * needs to explicitly control the size of a region, it should override its
     * preferred size range by setting the <code>minHeight</code>, <code>prefHeight</code>,
     * and <code>maxHeight</code> properties.
     */
    private ReadOnlyDoubleWrapper height;

    /**
     * Because the height is very often set and very often read but only sometimes
     * listened to, it is beneficial to use the super-lazy pattern property, where we
     * only inflate the property object when heightProperty() is explicitly invoked.
     */
    private double _height;

    // Note that it is OK for this method to be protected so long as the height
    // property is never bound. Only Region could do so because only Region has
    // access to a writable property for "height", but since there is now a protected
    // set method, it is impossible for Region to ever bind this property.
    protected void setHeight(double value) {
        if (height == null) {
            heightChanged(value);
        } else {
            height.set(value);
        }
    }

    private void heightChanged(double value) {
        if (_height != value) {
            _height = value;
            // It is possible that somebody sets the height of the region to a value which
            // it previously held. If this is the case, we want to avoid excessive layouts.
            // Note that I have biased this for layout over binding, because the heightProperty
            // is now going to recompute the height eagerly. The cost of excessive and
            // unnecessary bounds changes, however, is relatively high.
            boundingBox = null;
            // Note: although impl_geomChanged will usually also invalidate the
            // layout bounds, that is not the case for Regions, and both must
            // be called separately.
            impl_geomChanged();
            impl_layoutBoundsChanged();
            // We use "NODE_GEOMETRY" to mean that the bounds have changed and
            // need to be sync'd with the render tree
            impl_markDirty(DirtyBits.NODE_GEOMETRY);
            // Change of the height (or width) won't change the preferred size.
            // So we don't need to flush the cache. We should however mark this node
            // as needs layout to be internally layouted.
            setNeedsLayout(true);
            // This call is only needed when this was not called from the parent during the layout.
            // Otherwise it would flush the cache of the parent, which is not necessary
            requestParentLayout();
        }
    }

    public final double getHeight() { return height == null ? _height : height.get(); }

    public final ReadOnlyDoubleProperty heightProperty() {
        if (height == null) {
            height = new ReadOnlyDoubleWrapper(_height) {
                @Override protected void invalidated() { heightChanged(get()); }
                @Override public Object getBean() { return Region.this; }
                @Override public String getName() { return "height"; }
            };
        }
        return height.getReadOnlyProperty();
    }

    /**
     * This class is reused for the min, pref, and max properties since
     * they all performed the same function (to call requestParentLayout).
     */
    private final class MinPrefMaxProperty extends StyleableDoubleProperty {
        private final String name;
        private final CssMetaData<? extends Styleable, Number> cssMetaData;

        MinPrefMaxProperty(String name, double initialValue, CssMetaData<? extends Styleable, Number> cssMetaData) {
            super(initialValue);
            this.name = name;
            this.cssMetaData = cssMetaData;
        }

        @Override public void invalidated() { requestParentLayout(); }
        @Override public Object getBean() { return Region.this; }
        @Override public String getName() { return name; }

        @Override
        public CssMetaData<? extends Styleable, Number> getCssMetaData() {
            return cssMetaData;
        }
    }

    /**
     * Property for overriding the region's computed minimum width.
     * This should only be set if the region's internally computed minimum width
     * doesn't meet the application's layout needs.
     * <p>
     * Defaults to the <code>USE_COMPUTED_SIZE</code> flag, which means that
     * <code>minWidth(forHeight)</code> will return the region's internally
     * computed minimum width.
     * <p>
     * Setting this value to the <code>USE_PREF_SIZE</code> flag will cause
     * <code>minWidth(forHeight)</code> to return the region's preferred width,
     * enabling applications to easily restrict the resizability of the region.
     */
    private DoubleProperty minWidth;
    private double _minWidth = USE_COMPUTED_SIZE;
    public final void setMinWidth(double value) {
        if (minWidth == null) {
            _minWidth = value;
            requestParentLayout();
        } else {
            minWidth.set(value);
        }
    }
    public final double getMinWidth() { return minWidth == null ? _minWidth : minWidth.get(); }
    public final DoubleProperty minWidthProperty() {
        if (minWidth == null) minWidth = new MinPrefMaxProperty("minWidth", _minWidth, StyleableProperties.MIN_WIDTH);
        return minWidth;
    }

    /**
     * Property for overriding the region's computed minimum height.
     * This should only be set if the region's internally computed minimum height
     * doesn't meet the application's layout needs.
     * <p>
     * Defaults to the <code>USE_COMPUTED_SIZE</code> flag, which means that
     * <code>minHeight(forWidth)</code> will return the region's internally
     * computed minimum height.
     * <p>
     * Setting this value to the <code>USE_PREF_SIZE</code> flag will cause
     * <code>minHeight(forWidth)</code> to return the region's preferred height,
     * enabling applications to easily restrict the resizability of the region.
     *
     */
    private DoubleProperty minHeight;
    private double _minHeight = USE_COMPUTED_SIZE;
    public final void setMinHeight(double value) {
        if (minHeight == null) {
            _minHeight = value;
            requestParentLayout();
        } else {
            minHeight.set(value);
        }
    }
    public final double getMinHeight() { return minHeight == null ? _minHeight : minHeight.get(); }
    public final DoubleProperty minHeightProperty() {
        if (minHeight == null) minHeight = new MinPrefMaxProperty("minHeight", _minHeight, StyleableProperties.MIN_HEIGHT);
        return minHeight;
    }

    /**
     * Convenience method for overriding the region's computed minimum width and height.
     * This should only be called if the region's internally computed minimum size
     * doesn't meet the application's layout needs.
     *
     * @see #setMinWidth
     * @see #setMinHeight
     * @param minWidth  the override value for minimum width
     * @param minHeight the override value for minimum height
     */
    public void setMinSize(double minWidth, double minHeight) {
        setMinWidth(minWidth);
        setMinHeight(minHeight);
    }

    /**
     * Property for overriding the region's computed preferred width.
     * This should only be set if the region's internally computed preferred width
     * doesn't meet the application's layout needs.
     * <p>
     * Defaults to the <code>USE_COMPUTED_SIZE</code> flag, which means that
     * <code>getPrefWidth(forHeight)</code> will return the region's internally
     * computed preferred width.
     */
    private DoubleProperty prefWidth;
    private double _prefWidth = USE_COMPUTED_SIZE;
    public final void setPrefWidth(double value) {
        if (prefWidth == null) {
            _prefWidth = value;
            requestParentLayout();
        } else {
            prefWidth.set(value);
        }
    }
    public final double getPrefWidth() { return prefWidth == null ? _prefWidth : prefWidth.get(); }
    public final DoubleProperty prefWidthProperty() {
        if (prefWidth == null) prefWidth = new MinPrefMaxProperty("prefWidth", _prefWidth, StyleableProperties.PREF_WIDTH);
        return prefWidth;
    }

    /**
     * Property for overriding the region's computed preferred height.
     * This should only be set if the region's internally computed preferred height
     * doesn't meet the application's layout needs.
     * <p>
     * Defaults to the <code>USE_COMPUTED_SIZE</code> flag, which means that
     * <code>getPrefHeight(forWidth)</code> will return the region's internally
     * computed preferred width.
     */
    private DoubleProperty prefHeight;
    private double _prefHeight = USE_COMPUTED_SIZE;
    public final void setPrefHeight(double value) {
        if (prefHeight == null) {
            _prefHeight = value;
            requestParentLayout();
        } else {
            prefHeight.set(value);
        }
    }
    public final double getPrefHeight() { return prefHeight == null ? _prefHeight : prefHeight.get(); }
    public final DoubleProperty prefHeightProperty() {
        if (prefHeight == null) prefHeight = new MinPrefMaxProperty("prefHeight", _prefHeight, StyleableProperties.PREF_HEIGHT);
        return prefHeight;
    }

    /**
     * Convenience method for overriding the region's computed preferred width and height.
     * This should only be called if the region's internally computed preferred size
     * doesn't meet the application's layout needs.
     *
     * @see #setPrefWidth
     * @see #setPrefHeight
     * @param prefWidth the override value for preferred width
     * @param prefHeight the override value for preferred height
     */
    public void setPrefSize(double prefWidth, double prefHeight) {
        setPrefWidth(prefWidth);
        setPrefHeight(prefHeight);
    }

    /**
     * Property for overriding the region's computed maximum width.
     * This should only be set if the region's internally computed maximum width
     * doesn't meet the application's layout needs.
     * <p>
     * Defaults to the <code>USE_COMPUTED_SIZE</code> flag, which means that
     * <code>getMaxWidth(forHeight)</code> will return the region's internally
     * computed maximum width.
     * <p>
     * Setting this value to the <code>USE_PREF_SIZE</code> flag will cause
     * <code>getMaxWidth(forHeight)</code> to return the region's preferred width,
     * enabling applications to easily restrict the resizability of the region.
     */
    private DoubleProperty maxWidth;
    private double _maxWidth = USE_COMPUTED_SIZE;
    public final void setMaxWidth(double value) {
        if (maxWidth == null) {
            _maxWidth = value;
            requestParentLayout();
        } else {
            maxWidth.set(value);
        }
    }
    public final double getMaxWidth() { return maxWidth == null ? _maxWidth : maxWidth.get(); }
    public final DoubleProperty maxWidthProperty() {
        if (maxWidth == null) maxWidth = new MinPrefMaxProperty("maxWidth", _maxWidth, StyleableProperties.MAX_WIDTH);
        return maxWidth;
    }

    /**
     * Property for overriding the region's computed maximum height.
     * This should only be set if the region's internally computed maximum height
     * doesn't meet the application's layout needs.
     * <p>
     * Defaults to the <code>USE_COMPUTED_SIZE</code> flag, which means that
     * <code>getMaxHeight(forWidth)</code> will return the region's internally
     * computed maximum height.
     * <p>
     * Setting this value to the <code>USE_PREF_SIZE</code> flag will cause
     * <code>getMaxHeight(forWidth)</code> to return the region's preferred height,
     * enabling applications to easily restrict the resizability of the region.
     */
    private DoubleProperty maxHeight;
    private double _maxHeight = USE_COMPUTED_SIZE;
    public final void setMaxHeight(double value) {
        if (maxHeight == null) {
            _maxHeight = value;
            requestParentLayout();
        } else {
            maxHeight.set(value);
        }
    }
    public final double getMaxHeight() { return maxHeight == null ? _maxHeight : maxHeight.get(); }
    public final DoubleProperty maxHeightProperty() {
        if (maxHeight == null) maxHeight = new MinPrefMaxProperty("maxHeight", _maxHeight, StyleableProperties.MAX_HEIGHT);
        return maxHeight;
    }

    /**
     * Convenience method for overriding the region's computed maximum width and height.
     * This should only be called if the region's internally computed maximum size
     * doesn't meet the application's layout needs.
     *
     * @see #setMaxWidth
     * @see #setMaxHeight
     * @param maxWidth  the override value for maximum width
     * @param maxHeight the override value for maximum height
     */
    public void setMaxSize(double maxWidth, double maxHeight) {
        setMaxWidth(maxWidth);
        setMaxHeight(maxHeight);
    }

    /**
     * When specified, the {@code shape} will cause the region to be
     * rendered as the specified shape rather than as a rounded rectangle.
     * When null, the Region is rendered as a rounded rectangle. When rendered
     * as a Shape, any Background is used to fill the shape, although any
     * background insets are ignored as are background radii. Any BorderStrokes
     * defined are used for stroking the shape. Any BorderImages are ignored.
     *
     * @default null
     * @css shape SVG shape string
     * @since JavaFX 8.0
     */
    private ObjectProperty<Shape> shape = null;
    private Shape _shape;
    public final Shape getShape() { return shape == null ? _shape : shape.get(); }
    public final void setShape(Shape value) { shapeProperty().set(value); }
    public final ObjectProperty<Shape> shapeProperty() {
        if (shape == null) {
            shape = new ShapeProperty();
        }
        return shape;
    }

    /**
     * An implementation for the ShapeProperty. This is also a ShapeChangeListener.
     */
    private final class ShapeProperty extends StyleableObjectProperty<Shape> implements Runnable {
        @Override public Object getBean() { return Region.this; }
        @Override public String getName() { return "shape"; }
        @Override public CssMetaData<Region, Shape> getCssMetaData() {
            return StyleableProperties.SHAPE;
        }
        @Override protected void invalidated() {
            final Shape value = get();
            if (_shape != value) {
                // The shape has changed. We need to add/remove listeners
                if (_shape != null) _shape.impl_setShapeChangeListener(null);
                if (value != null) value.impl_setShapeChangeListener(this);
                // Invalidate the bounds and such
                run();
                if (_shape == null || value == null) {
                    // It either was null before, or is null now. In either case,
                    // the result of the insets computation will have changed, and
                    // we therefore need to fire that the insets value may have changed.
                    insets.fireValueChanged();
                }
                // Update our reference to the old shape
                _shape = value;
            }
        }

        @Override public void run() {
            impl_geomChanged();
            impl_markDirty(DirtyBits.REGION_SHAPE);
        }
    };

    /**
     * Specifies whether the shape, if defined, is scaled to match the size of the Region.
     * {@code true} means the shape is scaled to fit the size of the Region, {@code false}
     * means the shape is at its source size, its positioning depends on the value of
     * {@code centerShape}.
     *
     * @default true
     * @css shape-size      true | false
     * @since JavaFX 8.0
     */
    private BooleanProperty scaleShape = null;
    public final void setScaleShape(boolean value) { scaleShapeProperty().set(value); }
    public final boolean isScaleShape() { return scaleShape == null ? true : scaleShape.get(); }
    public final BooleanProperty scaleShapeProperty() {
        if (scaleShape == null) {
            scaleShape = new StyleableBooleanProperty(true) {
                @Override public Object getBean() { return Region.this; }
                @Override public String getName() { return "scaleShape"; }
                @Override public CssMetaData<Region, Boolean> getCssMetaData() {
                    return StyleableProperties.SCALE_SHAPE;
                }
                @Override public void invalidated() {
                    impl_geomChanged();
                    impl_markDirty(DirtyBits.REGION_SHAPE);
                }
            };
        }
        return scaleShape;
    }

    /**
     * Defines whether the shape is centered within the Region's width or height.
     * {@code true} means the shape centered within the Region's width and height,
     * {@code false} means the shape is positioned at its source position.
     *
     * @default true
     * @css position-shape      true | false
     * @since JavaFX 8.0
     */
    private BooleanProperty centerShape = null;
    public final void setCenterShape(boolean value) { centerShapeProperty().set(value); }
    public final boolean isCenterShape() { return centerShape == null ? true : centerShape.get(); }
    public final BooleanProperty centerShapeProperty() {
        if (centerShape == null) {
            centerShape = new StyleableBooleanProperty(true) {
                @Override public Object getBean() { return Region.this; }
                @Override public String getName() { return "centerShape"; }
                @Override public CssMetaData<Region, Boolean> getCssMetaData() {
                    return StyleableProperties.POSITION_SHAPE;
                }
                @Override public void invalidated() {
                    impl_geomChanged();
                    impl_markDirty(DirtyBits.REGION_SHAPE);
                }
            };
        }
        return centerShape;
    }

    /**
     * Defines a hint to the system indicating that the Shape used to define the region's
     * background is stable and would benefit from caching.
     *
     * @default true
     * @css -fx-cache-shape      true | false
     * @since JavaFX 8.0
     */
    private BooleanProperty cacheShape = null;
    public final void setCacheShape(boolean value) { cacheShapeProperty().set(value); }
    public final boolean isCacheShape() { return cacheShape == null ? true : cacheShape.get(); }
    public final BooleanProperty cacheShapeProperty() {
        if (cacheShape == null) {
            cacheShape = new StyleableBooleanProperty(true) {
                @Override public Object getBean() { return Region.this; }
                @Override public String getName() { return "cacheShape"; }
                @Override public CssMetaData<Region, Boolean> getCssMetaData() {
                    return StyleableProperties.CACHE_SHAPE;
                }
            };
        }
        return cacheShape;
    }

    /***************************************************************************
     *                                                                         *
     * Layout                                                                  *
     *                                                                         *
     **************************************************************************/

    /**
     * Returns <code>true</code> since all Regions are resizable.
     * @return whether this node can be resized by its parent during layout
     */
    @Override public boolean isResizable() {
        return true;
    }

    /**
     * Invoked by the region's parent during layout to set the region's
     * width and height.  <b>Applications should not invoke this method directly</b>.
     * If an application needs to directly set the size of the region, it should
     * override its size constraints by calling <code>setMinSize()</code>,
     *  <code>setPrefSize()</code>, or <code>setMaxSize()</code> and it's parent
     * will honor those overrides during layout.
     *
     * @param width the target layout bounds width
     * @param height the target layout bounds height
     */
    @Override public void resize(double width, double height) {
        setWidth(width);
        setHeight(height);
        PlatformLogger logger = Logging.getLayoutLogger();
        if (logger.isLoggable(PlatformLogger.FINER)) {
            logger.finer(this.toString() + " resized to " + width + " x " + height);
        }
    }

    /**
     * Called during layout to determine the minimum width for this node.
     * Returns the value from <code>computeMinWidth(forHeight)</code> unless
     * the application overrode the minimum width by setting the minWidth property.
     *
     * @see #setMinWidth(double)
     * @return the minimum width that this node should be resized to during layout
     */
    @Override public final double minWidth(double height) {
        double override = getMinWidth();
        if (override == USE_COMPUTED_SIZE) {
            return super.minWidth(height);
        } else if (override == USE_PREF_SIZE) {
            return prefWidth(height);
        }
        return override;
    }

    /**
     * Called during layout to determine the minimum height for this node.
     * Returns the value from <code>computeMinHeight(forWidth)</code> unless
     * the application overrode the minimum height by setting the minHeight property.
     *
     * @see #setMinHeight
     * @return the minimum height that this node should be resized to during layout
     */
    @Override public final double minHeight(double width) {
        double override = getMinHeight();
        if (override == USE_COMPUTED_SIZE) {
            return super.minHeight(width);
        } else if (override == USE_PREF_SIZE) {
            return prefHeight(width);
        }
        return override;
    }

    /**
     * Called during layout to determine the preferred width for this node.
     * Returns the value from <code>computePrefWidth(forHeight)</code> unless
     * the application overrode the preferred width by setting the prefWidth property.
     *
     * @see #setPrefWidth
     * @return the preferred width that this node should be resized to during layout
     */
    @Override public final double prefWidth(double height) {
        double override = getPrefWidth();
        if (override == USE_COMPUTED_SIZE) {
            return super.prefWidth(height);
        }
        return override;
    }

    /**
     * Called during layout to determine the preferred height for this node.
     * Returns the value from <code>computePrefHeight(forWidth)</code> unless
     * the application overrode the preferred height by setting the prefHeight property.
     *
     * @see #setPrefHeight
     * @return the preferred height that this node should be resized to during layout
     */
    @Override public final double prefHeight(double width) {
        double override = getPrefHeight();
        if (override == USE_COMPUTED_SIZE) {
            return super.prefHeight(width);
        }
        return override;
    }

    /**
     * Called during layout to determine the maximum width for this node.
     * Returns the value from <code>computeMaxWidth(forHeight)</code> unless
     * the application overrode the maximum width by setting the maxWidth property.
     *
     * @see #setMaxWidth
     * @return the maximum width that this node should be resized to during layout
     */
    @Override public final double maxWidth(double height) {
        double override = getMaxWidth();
        if (override == USE_COMPUTED_SIZE) {
            return computeMaxWidth(height);
        } else if (override == USE_PREF_SIZE) {
            return prefWidth(height);
        }
        return override;
    }

    /**
     * Called during layout to determine the maximum height for this node.
     * Returns the value from <code>computeMaxHeight(forWidth)</code> unless
     * the application overrode the maximum height by setting the maxHeight property.
     *
     * @see #setMaxHeight
     * @return the maximum height that this node should be resized to during layout
     */
    @Override public final double maxHeight(double width) {
        double override = getMaxHeight();
        if (override == USE_COMPUTED_SIZE) {
            return computeMaxHeight(width);
        } else if (override == USE_PREF_SIZE) {
            return prefHeight(width);
        }
        return override;
    }

    /**
     * Computes the minimum width of this region.
     * Returns the sum of the left and right insets by default.
     * region subclasses should override this method to return an appropriate
     * value based on their content and layout strategy.  If the subclass
     * doesn't have a VERTICAL content bias, then the height parameter can be
     * ignored.
     *
     * @return the computed minimum width of this region
     */
    @Override protected double computeMinWidth(double height) {
        return getInsets().getLeft() + getInsets().getRight();
    }

    /**
     * Computes the minimum height of this region.
     * Returns the sum of the top and bottom insets by default.
     * Region subclasses should override this method to return an appropriate
     * value based on their content and layout strategy.  If the subclass
     * doesn't have a HORIZONTAL content bias, then the width parameter can be
     * ignored.
     *
     * @return the computed minimum height for this region
     */
    @Override protected double computeMinHeight(double width) {
        return getInsets().getTop() + getInsets().getBottom();
    }

    /**
     * Computes the preferred width of this region for the given height.
     * Region subclasses should override this method to return an appropriate
     * value based on their content and layout strategy.  If the subclass
     * doesn't have a VERTICAL content bias, then the height parameter can be
     * ignored.
     *
     * @return the computed preferred width for this region
     */
    @Override protected double computePrefWidth(double height) {
        final double w = super.computePrefWidth(height);
        return getInsets().getLeft() + w + getInsets().getRight();
    }

    /**
     * Computes the preferred height of this region for the given width;
     * Region subclasses should override this method to return an appropriate
     * value based on their content and layout strategy.  If the subclass
     * doesn't have a HORIZONTAL content bias, then the width parameter can be
     * ignored.
     *
     * @return the computed preferred height for this region
     */
    @Override protected double computePrefHeight(double width) {
        final double h = super.computePrefHeight(width);
        return getInsets().getTop() + h + getInsets().getBottom();
    }

    /**
     * Computes the maximum width for this region.
     * Returns Double.MAX_VALUE by default.
     * Region subclasses may override this method to return an different
     * value based on their content and layout strategy.  If the subclass
     * doesn't have a VERTICAL content bias, then the height parameter can be
     * ignored.
     *
     * @return the computed maximum width for this region
     */
    protected double computeMaxWidth(double height) {
        return Double.MAX_VALUE;
    }

    /**
     * Computes the maximum height of this region.
     * Returns Double.MAX_VALUE by default.
     * Region subclasses may override this method to return a different
     * value based on their content and layout strategy.  If the subclass
     * doesn't have a HORIZONTAL content bias, then the width parameter can be
     * ignored.
     *
     * @return the computed maximum height for this region
     */
    protected double computeMaxHeight(double width) {
        return Double.MAX_VALUE;
    }

    /**
     * If this region's snapToPixel property is true, returns a value rounded
     * to the nearest pixel, else returns the same value.
     * @param value the space value to be snapped
     * @return value rounded to nearest pixel
     */
    protected double snapSpace(double value) {
        return snapSpace(value, isSnapToPixel());
    }

    /**
     * If this region's snapToPixel property is true, returns a value ceiled
     * to the nearest pixel, else returns the same value.
     * @param value the size value to be snapped
     * @return value ceiled to nearest pixel
     */
    protected double snapSize(double value) {
        return snapSize(value, isSnapToPixel());
    }

    /**
     * If this region's snapToPixel property is true, returns a value rounded
     * to the nearest pixel, else returns the same value.
     * @param value the position value to be snapped
     * @return value rounded to nearest pixel
     */
    protected double snapPosition(double value) {
        return snapPosition(value, isSnapToPixel());
    }

    double snapPortion(double value) {
        return snapPortion(value, isSnapToPixel());
    }


    /**
     * Utility method to get the top inset which includes padding and border
     * inset. Then snapped to whole pixels if isSnapToPixel() is true.
     *
     * @since JavaFX 8.0
     * @return Rounded up insets top
     */
    public final double snappedTopInset() {
        return snappedTopInset;
    }

    /**
     * Utility method to get the bottom inset which includes padding and border
     * inset. Then snapped to whole pixels if isSnapToPixel() is true.
     *
     * @since JavaFX 8.0
     * @return Rounded up insets bottom
     */
    public final double snappedBottomInset() {
        return snappedBottomInset;
    }

    /**
     * Utility method to get the left inset which includes padding and border
     * inset. Then snapped to whole pixels if isSnapToPixel() is true.
     *
     * @since JavaFX 8.0
     * @return Rounded up insets left
     */
    public final double snappedLeftInset() {
        return snappedLeftInset;
    }

    /**
     * Utility method to get the right inset which includes padding and border
     * inset. Then snapped to whole pixels if isSnapToPixel() is true.
     *
     * @since JavaFX 8.0
     * @return Rounded up insets right
     */
    public final double snappedRightInset() {
        return snappedRightInset;
    }


    double computeChildMinAreaWidth(Node child, Insets margin) {
        return computeChildMinAreaWidth(child, margin, -1);
    }

    double computeChildMinAreaWidth(Node child, Insets margin, double height) {
        final boolean snap = isSnapToPixel();
        double left = margin != null? snapSpace(margin.getLeft(), snap) : 0;
        double right = margin != null? snapSpace(margin.getRight(), snap) : 0;
        double alt = -1;
        if (child.getContentBias() == Orientation.VERTICAL) { // width depends on height
            alt = snapSize(height != -1? boundedSize(child.minHeight(-1), height, child.maxHeight(-1)) :
                                         child.maxHeight(-1));
        }
        return left + snapSize(child.minWidth(alt)) + right;
    }

    double computeChildMinAreaHeight(Node child, Insets margin) {
        return computeChildMinAreaHeight(child, margin, -1);
    }

    double computeChildMinAreaHeight(Node child, Insets margin, double width) {
        final boolean snap = isSnapToPixel();
        double top = margin != null? snapSpace(margin.getTop(), snap) : 0;
        double bottom = margin != null? snapSpace(margin.getBottom(), snap) : 0;
        double alt = -1;
        if (child.getContentBias() == Orientation.HORIZONTAL) { // height depends on width
            alt = snapSize(width != -1? boundedSize(child.minWidth(-1), width, child.maxWidth(-1)) :
                                        child.maxWidth(-1));
        }
        return top + snapSize(child.minHeight(alt)) + bottom;
    }

    double computeChildPrefAreaWidth(Node child, Insets margin) {
        return computeChildPrefAreaWidth(child, margin, -1);
    }

    double computeChildPrefAreaWidth(Node child, Insets margin, double height) {
        final boolean snap = isSnapToPixel();
        double top = margin != null? snapSpace(margin.getTop(), snap) : 0;
        double bottom = margin != null? snapSpace(margin.getBottom(), snap) : 0;
        double left = margin != null? snapSpace(margin.getLeft(), snap) : 0;
        double right = margin != null? snapSpace(margin.getRight(), snap) : 0;
        double alt = -1;
        if (child.getContentBias() == Orientation.VERTICAL) { // width depends on height
            alt = snapSize(boundedSize(
                    child.minHeight(-1), height != -1? height - top - bottom :
                           child.prefHeight(-1), child.maxHeight(-1)));
        }
        return left + snapSize(boundedSize(child.minWidth(alt), child.prefWidth(alt), child.maxWidth(alt))) + right;
    }

    double computeChildPrefAreaHeight(Node child, Insets margin) {
        return computeChildPrefAreaHeight(child, margin, -1);
    }

    double computeChildPrefAreaHeight(Node child, Insets margin, double width) {
        final boolean snap = isSnapToPixel();
        double top = margin != null? snapSpace(margin.getTop(), snap) : 0;
        double bottom = margin != null? snapSpace(margin.getBottom(), snap) : 0;
        double left = margin != null? snapSpace(margin.getLeft(), snap) : 0;
        double right = margin != null? snapSpace(margin.getRight(), snap) : 0;
        double alt = -1;
        if (child.getContentBias() == Orientation.HORIZONTAL) { // height depends on width
            alt = snapSize(boundedSize(
                    child.minWidth(-1), width != -1? width - left - right :
                           child.prefWidth(-1), child.maxWidth(-1)));
        }
        return top + snapSize(boundedSize(child.minHeight(alt), child.prefHeight(alt), child.maxHeight(alt))) + bottom;
    }

    double computeChildMaxAreaWidth(Node child, Insets margin, double height) {
        double max = child.maxWidth(-1);
        if (max == Double.MAX_VALUE) {
            return max;
        }
        final boolean snap = isSnapToPixel();
        double left = margin != null? snapSpace(margin.getLeft(), snap) : 0;
        double right = margin != null? snapSpace(margin.getRight(), snap) : 0;
        double alt = -1;
        if (child.getContentBias() == Orientation.VERTICAL) { // width depends on height
            alt = snapSize(height != -1? boundedSize(child.minHeight(-1), height, child.maxHeight(-1)) :
                child.minHeight(-1));
            max = child.maxWidth(alt);
        }
        // if min > max, min wins, so still need to call boundedSize()
        return left + snapSize(boundedSize(child.minWidth(alt), max, child.maxWidth(alt))) + right;
    }

    double computeChildMaxAreaHeight(Node child, Insets margin, double width) {
        double max = child.maxHeight(-1);
        if (max == Double.MAX_VALUE) {
            return max;
        }

        final boolean snap = isSnapToPixel();
        double top = margin != null? snapSpace(margin.getTop(), snap) : 0;
        double bottom = margin != null? snapSpace(margin.getBottom(), snap) : 0;
        double alt = -1;
        if (child.getContentBias() == Orientation.HORIZONTAL) { // height depends on width
            alt = snapSize(width != -1? boundedSize(child.minWidth(-1), width, child.maxWidth(-1)) :
                child.minWidth(-1));
            max = child.maxHeight(alt);
        }
        // if min > max, min wins, so still need to call boundedSize()
        return top + snapSize(boundedSize(child.minHeight(alt), max, child.maxHeight(alt))) + bottom;
    }

    /* Max of children's minimum area widths */

    double computeMaxMinAreaWidth(List<Node> children, Callback<Node, Insets> margins, HPos halignment /* ignored for now */) {
        return getMaxAreaWidth(children, margins, new double[] { -1 }, true);
    }

    double computeMaxMinAreaWidth(List<Node> children, Callback<Node, Insets> margins, HPos halignment /* ignored for now */, double height) {
        return getMaxAreaWidth(children, margins, new double[] { height }, true);
    }

    double computeMaxMinAreaWidth(List<Node> children, Callback<Node, Insets> childMargins, double childHeights[], HPos halignment /* ignored for now */) {
        return getMaxAreaWidth(children, childMargins, childHeights, true);
    }

    /* Max of children's minimum area heights */

    double computeMaxMinAreaHeight(List<Node>children, Callback<Node, Insets> margins, VPos valignment) {
        return getMaxAreaHeight(children, margins, null, valignment, true);
    }

    double computeMaxMinAreaHeight(List<Node>children, Callback<Node, Insets> margins, VPos valignment, double width) {
        return getMaxAreaHeight(children, margins, new double[] { width }, valignment, true);
    }

    double computeMaxMinAreaHeight(List<Node>children, Callback<Node, Insets> childMargins, double childWidths[], VPos valignment) {
        return getMaxAreaHeight(children, childMargins, childWidths, valignment, true);
    }

    /* Max of children's pref area widths */

    double computeMaxPrefAreaWidth(List<Node>children, Callback<Node, Insets> margins, HPos halignment /* ignored for now */) {
        return getMaxAreaWidth(children, margins, new double[] { -1 }, false);
    }

    double computeMaxPrefAreaWidth(List<Node>children, Callback<Node, Insets> margins, double height, HPos halignment /* ignored for now */) {
        return getMaxAreaWidth(children, margins, new double[] { height }, false);
    }

    double computeMaxPrefAreaWidth(List<Node>children, Callback<Node, Insets> childMargins, double childHeights[], HPos halignment /* ignored for now */) {
        return getMaxAreaWidth(children, childMargins, childHeights, false);
    }

    /* Max of children's pref area heights */

    double computeMaxPrefAreaHeight(List<Node>children, Callback<Node, Insets> margins, VPos valignment) {
        return getMaxAreaHeight(children, margins, null, valignment, false);
    }

    double computeMaxPrefAreaHeight(List<Node>children, Callback<Node, Insets> margins, double width, VPos valignment) {
        return getMaxAreaHeight(children, margins, new double[] { width }, valignment, false);
    }

    double computeMaxPrefAreaHeight(List<Node>children, Callback<Node, Insets> childMargins, double childWidths[], VPos valignment) {
        return getMaxAreaHeight(children, childMargins, childWidths, valignment, false);
    }

    /**
     * Returns the size of a Node that should be placed in an area of the specified size,
     * bounded in it's min/max size, respecting bias.
     *
     * @param node the node
     * @param areaWidth the width of the bounding area where the node is going to be placed
     * @param areaHeight the height of the bounding area where the node is going to be placed
     * @param fillWidth if Node should try to fill the area width
     * @param fillHeight if Node should try to fill the area height
     * @param result Vec2d object for the result or null if new one should be created
     * @return Vec2d object with width(x parameter) and height (y parameter)
     */
    static Vec2d boundedNodeSizeWithBias(Node node, double areaWidth, double areaHeight,
            boolean fillWidth, boolean fillHeight, Vec2d result) {
        if (result == null) {
            result = new Vec2d();
        }

        Orientation bias = node.getContentBias();

        double childWidth = 0;
        double childHeight = 0;

        if (bias == null) {
            childWidth = boundedSize(
                    node.minWidth(-1), fillWidth ? areaWidth
                    : Math.min(areaWidth, node.prefWidth(-1)),
                    node.maxWidth(-1));
            childHeight = boundedSize(
                    node.minHeight(-1), fillHeight ? areaHeight
                    : Math.min(areaHeight, node.prefHeight(-1)),
                    node.maxHeight(-1));

        } else if (bias == Orientation.HORIZONTAL) {
            childWidth = boundedSize(
                    node.minWidth(-1), fillWidth ? areaWidth
                    : Math.min(areaWidth, node.prefWidth(-1)),
                    node.maxWidth(-1));
            childHeight = boundedSize(
                    node.minHeight(childWidth), fillHeight ? areaHeight
                    : Math.min(areaHeight, node.prefHeight(childWidth)),
                    node.maxHeight(childWidth));

        } else { // bias == VERTICAL
            childHeight = boundedSize(
                    node.minHeight(-1), fillHeight ? areaHeight
                    : Math.min(areaHeight, node.prefHeight(-1)),
                    node.maxHeight(-1));
            childWidth = boundedSize(
                    node.minWidth(childHeight), fillWidth ? areaWidth
                    : Math.min(areaWidth, node.prefWidth(childHeight)),
                    node.maxWidth(childHeight));
        }

        result.set(childWidth, childHeight);
        return result;
    }

    /* utility method for computing the max of children's min or pref heights, taking into account baseline alignment */
    private double getMaxAreaHeight(List<Node> children, Callback<Node,Insets> childMargins,  double childWidths[], VPos valignment, boolean minimum) {
        final double singleChildWidth = childWidths == null ? -1 : childWidths.length == 1 ? childWidths[0] : Double.NaN;
        if (valignment == VPos.BASELINE) {
            double maxAbove = 0;
            double maxBelow = 0;
            for (int i = 0, maxPos = children.size(); i < maxPos; i++) {
                final Node child = children.get(i);
                final double baseline = child.getBaselineOffset();
                Insets margin = childMargins.call(child);
                final double top = margin != null? snapSpace(margin.getTop()) : 0;
                final double bottom = margin != null? snapSpace(margin.getBottom()) : 0;
                final double childWidth = Double.isNaN(singleChildWidth) ? childWidths[i] : singleChildWidth;
                maxAbove = Math.max(maxAbove, baseline + top);
                maxBelow = Math.max(maxBelow,
                        snapSpace(minimum?snapSize(child.minHeight(childWidth)) : snapSize(child.prefHeight(childWidth))) -
                        baseline + bottom);
            }
            return maxAbove + maxBelow; //remind(aim): ceil this value?
        } else {
            double max = 0;
            for (int i = 0, maxPos = children.size(); i < maxPos; i++) {
                final Node child = children.get(i);
                Insets margin = childMargins.call(child);
                final double childWidth = Double.isNaN(singleChildWidth) ? childWidths[i] : singleChildWidth;
                max = Math.max(max, minimum?
                    computeChildMinAreaHeight(child, margin, childWidth) :
                        computeChildPrefAreaHeight(child, margin, childWidth));
            }
            return max;
        }
    }

    /* utility method for computing the max of children's min or pref width, horizontal alignment is ignored for now */
    private double getMaxAreaWidth(List<javafx.scene.Node> children, Callback<Node, Insets> childMargins, double childHeights[], boolean minimum) {
        final double lastChildHeight = childHeights.length > 0 ? childHeights[childHeights.length - 1] : 0;
        double max = 0;
        for (int i = 0, maxPos = children.size(); i < maxPos; i++) {
            final Node child = children.get(i);
            final Insets margin = childMargins.call(child);
            final double childHeight = i < childHeights.length ? childHeights[i] : lastChildHeight;
            max = Math.max(max, minimum?
                computeChildMinAreaWidth(children.get(i), margin, childHeight) :
                    computeChildPrefAreaWidth(child, margin, childHeight));
        }
        return max;
    }

    /**
     * Utility method which positions the child within an area of this
     * region defined by {@code areaX}, {@code areaY}, {@code areaWidth} x {@code areaHeight},
     * with a baseline offset relative to that area.
     * <p>
     * This function does <i>not</i> resize the node and uses the node's layout bounds
     * width and height to determine how it should be positioned within the area.
     * <p>
     * If the vertical alignment is {@code VPos.BASELINE} then it
     * will position the node so that its own baseline aligns with the passed in
     * {@code baselineOffset},  otherwise the baseline parameter is ignored.
     * <p>
     * If {@code snapToPixel} is {@code true} for this region, then the x/y position
     * values will be rounded to their nearest pixel boundaries.
     *
     * @param child the child being positioned within this region
     * @param areaX the horizontal offset of the layout area relative to this region
     * @param areaY the vertical offset of the layout area relative to this region
     * @param areaWidth  the width of the layout area
     * @param areaHeight the height of the layout area
     * @param areaBaselineOffset the baseline offset to be used if VPos is BASELINE
     * @param halignment the horizontal alignment for the child within the area
     * @param valignment the vertical alignment for the child within the area
     *
     */
    protected void positionInArea(Node child, double areaX, double areaY, double areaWidth, double areaHeight,
                               double areaBaselineOffset, HPos halignment, VPos valignment) {
        positionInArea(child, areaX, areaY, areaWidth, areaHeight, areaBaselineOffset,
                Insets.EMPTY, halignment, valignment, isSnapToPixel());
    }

    /**
     * Utility method which positions the child within an area of this
     * region defined by {@code areaX}, {@code areaY}, {@code areaWidth} x {@code areaHeight},
     * with a baseline offset relative to that area.
     * <p>
     * This function does <i>not</i> resize the node and uses the node's layout bounds
     * width and height to determine how it should be positioned within the area.
     * <p>
     * If the vertical alignment is {@code VPos.BASELINE} then it
     * will position the node so that its own baseline aligns with the passed in
     * {@code baselineOffset},  otherwise the baseline parameter is ignored.
     * <p>
     * If {@code snapToPixel} is {@code true} for this region, then the x/y position
     * values will be rounded to their nearest pixel boundaries.
     * <p>
     * If {@code margin} is non-null, then that space will be allocated around the
     * child within the layout area.  margin may be null.
     *
     * @param child the child being positioned within this region
     * @param areaX the horizontal offset of the layout area relative to this region
     * @param areaY the vertical offset of the layout area relative to this region
     * @param areaWidth  the width of the layout area
     * @param areaHeight the height of the layout area
     * @param areaBaselineOffset the baseline offset to be used if VPos is BASELINE
     * @param margin the margin of space to be allocated around the child
     * @param halignment the horizontal alignment for the child within the area
     * @param valignment the vertical alignment for the child within the area
     *
     * @since JavaFX 8.0
     */
    public static void positionInArea(Node child, double areaX, double areaY, double areaWidth, double areaHeight,
                               double areaBaselineOffset, Insets margin, HPos halignment, VPos valignment, boolean isSnapToPixel) {
        Insets childMargin = margin != null? margin : Insets.EMPTY;

        position(child, areaX, areaY, areaWidth, areaHeight, areaBaselineOffset,
                snapSpace(childMargin.getTop(), isSnapToPixel),
                snapSpace(childMargin.getRight(), isSnapToPixel),
                snapSpace(childMargin.getBottom(), isSnapToPixel),
                snapSpace(childMargin.getLeft(), isSnapToPixel),
                halignment, valignment, isSnapToPixel);
    }

    /**
     * Utility method which lays out the child within an area of this
     * region defined by {@code areaX}, {@code areaY}, {@code areaWidth} x {@code areaHeight},
     * with a baseline offset relative to that area.
     * <p>
     * If the child is resizable, this method will resize it to fill the specified
     * area unless the node's maximum size prevents it.  If the node's maximum
     * size preference is less than the area size, the maximum size will be used.
     * If node's maximum is greater than the area size, then the node will be
     * resized to fit within the area, unless its minimum size prevents it.
     * <p>
     * If the child has a non-null contentBias, then this method will use it when
     * resizing the child.  If the contentBias is horizontal, it will set its width
     * first to the area's width (up to the child's max width limit) and then pass
     * that value to compute the child's height.  If child's contentBias is vertical,
     * then it will set its height to the area height (up to child's max height limit)
     * and pass that height to compute the child's width.  If the child's contentBias
     * is null, then it's width and height have no dependencies on each other.
     * <p>
     * If the child is not resizable (Shape, Group, etc) then it will only be
     * positioned and not resized.
     * <p>
     * If the child's resulting size differs from the area's size (either
     * because it was not resizable or it's sizing preferences prevented it), then
     * this function will align the node relative to the area using horizontal and
     * vertical alignment values.
     * If valignment is {@code VPos.BASELINE} then the node's baseline will be aligned
     * with the area baseline offset parameter, otherwise the baseline parameter
     * is ignored.
     * <p>
     * If {@code snapToPixel} is {@code true} for this region, then the resulting x,y
     * values will be rounded to their nearest pixel boundaries and the
     * width/height values will be ceiled to the next pixel boundary.
     *
     * @param child the child being positioned within this region
     * @param areaX the horizontal offset of the layout area relative to this region
     * @param areaY the vertical offset of the layout area relative to this region
     * @param areaWidth  the width of the layout area
     * @param areaHeight the height of the layout area
     * @param areaBaselineOffset the baseline offset to be used if VPos is BASELINE
     * @param halignment the horizontal alignment for the child within the area
     * @param valignment the vertical alignment for the child within the area
     *
     */
    protected void layoutInArea(Node child, double areaX, double areaY,
                               double areaWidth, double areaHeight,
                               double areaBaselineOffset,
                               HPos halignment, VPos valignment) {
        layoutInArea(child, areaX, areaY, areaWidth, areaHeight, areaBaselineOffset,
                Insets.EMPTY, halignment, valignment);
    }

    /**
     * Utility method which lays out the child within an area of this
     * region defined by {@code areaX}, {@code areaY}, {@code areaWidth} x {@code areaHeight},
     * with a baseline offset relative to that area.
     * <p>
     * If the child is resizable, this method will resize it to fill the specified
     * area unless the node's maximum size prevents it.  If the node's maximum
     * size preference is less than the area size, the maximum size will be used.
     * If node's maximum is greater than the area size, then the node will be
     * resized to fit within the area, unless its minimum size prevents it.
     * <p>
     * If the child has a non-null contentBias, then this method will use it when
     * resizing the child.  If the contentBias is horizontal, it will set its width
     * first to the area's width (up to the child's max width limit) and then pass
     * that value to compute the child's height.  If child's contentBias is vertical,
     * then it will set its height to the area height (up to child's max height limit)
     * and pass that height to compute the child's width.  If the child's contentBias
     * is null, then it's width and height have no dependencies on each other.
     * <p>
     * If the child is not resizable (Shape, Group, etc) then it will only be
     * positioned and not resized.
     * <p>
     * If the child's resulting size differs from the area's size (either
     * because it was not resizable or it's sizing preferences prevented it), then
     * this function will align the node relative to the area using horizontal and
     * vertical alignment values.
     * If valignment is {@code VPos.BASELINE} then the node's baseline will be aligned
     * with the area baseline offset parameter, otherwise the baseline parameter
     * is ignored.
     * <p>
     * If {@code margin} is non-null, then that space will be allocated around the
     * child within the layout area.  margin may be null.
     * <p>
     * If {@code snapToPixel} is {@code true} for this region, then the resulting x,y
     * values will be rounded to their nearest pixel boundaries and the
     * width/height values will be ceiled to the next pixel boundary.
     *
     * @param child the child being positioned within this region
     * @param areaX the horizontal offset of the layout area relative to this region
     * @param areaY the vertical offset of the layout area relative to this region
     * @param areaWidth  the width of the layout area
     * @param areaHeight the height of the layout area
     * @param areaBaselineOffset the baseline offset to be used if VPos is BASELINE
     * @param margin the margin of space to be allocated around the child
     * @param halignment the horizontal alignment for the child within the area
     * @param valignment the vertical alignment for the child within the area
     */
    protected void layoutInArea(Node child, double areaX, double areaY,
                               double areaWidth, double areaHeight,
                               double areaBaselineOffset,
                               Insets margin,
                               HPos halignment, VPos valignment) {
        layoutInArea(child, areaX, areaY, areaWidth, areaHeight,
                areaBaselineOffset, margin, true, true, halignment, valignment);
    }

    /**
     * Utility method which lays out the child within an area of this
     * region defined by {@code areaX}, {@code areaY}, {@code areaWidth} x {@code areaHeight},
     * with a baseline offset relative to that area.
     * <p>
     * If the child is resizable, this method will use {@code fillWidth} and {@code fillHeight}
     * to determine whether to resize it to fill the area or keep the child at its
     * preferred dimension.  If fillWidth/fillHeight are true, then this method
     * will only resize the child up to its max size limits.  If the node's maximum
     * size preference is less than the area size, the maximum size will be used.
     * If node's maximum is greater than the area size, then the node will be
     * resized to fit within the area, unless its minimum size prevents it.
     * <p>
     * If the child has a non-null contentBias, then this method will use it when
     * resizing the child.  If the contentBias is horizontal, it will set its width
     * first and then pass that value to compute the child's height.  If child's
     * contentBias is vertical, then it will set its height first
     * and pass that value to compute the child's width.  If the child's contentBias
     * is null, then it's width and height have no dependencies on each other.
     * <p>
     * If the child is not resizable (Shape, Group, etc) then it will only be
     * positioned and not resized.
     * <p>
     * If the child's resulting size differs from the area's size (either
     * because it was not resizable or it's sizing preferences prevented it), then
     * this function will align the node relative to the area using horizontal and
     * vertical alignment values.
     * If valignment is {@code VPos.BASELINE} then the node's baseline will be aligned
     * with the area baseline offset parameter, otherwise the baseline parameter
     * is ignored.
     * <p>
     * If {@code margin} is non-null, then that space will be allocated around the
     * child within the layout area.  margin may be null.
     * <p>
     * If {@code snapToPixel} is {@code true} for this region, then the resulting x,y
     * values will be rounded to their nearest pixel boundaries and the
     * width/height values will be ceiled to the next pixel boundary.
     *
     * @param child the child being positioned within this region
     * @param areaX the horizontal offset of the layout area relative to this region
     * @param areaY the vertical offset of the layout area relative to this region
     * @param areaWidth  the width of the layout area
     * @param areaHeight the height of the layout area
     * @param areaBaselineOffset the baseline offset to be used if VPos is BASELINE
     * @param margin the margin of space to be allocated around the child
     * @param fillWidth whether or not the child should be resized to fill the area width or kept to its preferred width
     * @param fillHeight whether or not the child should e resized to fill the area height or kept to its preferred height
     * @param halignment the horizontal alignment for the child within the area
     * @param valignment the vertical alignment for the child within the area
     */
    protected void layoutInArea(Node child, double areaX, double areaY,
                               double areaWidth, double areaHeight,
                               double areaBaselineOffset,
                               Insets margin, boolean fillWidth, boolean fillHeight,
                               HPos halignment, VPos valignment) {
        layoutInArea(child, areaX, areaY, areaWidth, areaHeight, areaBaselineOffset, margin, fillWidth, fillHeight, halignment, valignment, isSnapToPixel());
    }

    public static void layoutInArea(Node child, double areaX, double areaY,
                               double areaWidth, double areaHeight,
                               double areaBaselineOffset,
                               Insets margin, boolean fillWidth, boolean fillHeight,
                               HPos halignment, VPos valignment, boolean isSnapToPixel) {

        Insets childMargin = margin != null ? margin : Insets.EMPTY;
        double top = snapSpace(childMargin.getTop(), isSnapToPixel);
        double bottom = snapSpace(childMargin.getBottom(), isSnapToPixel);
        double left = snapSpace(childMargin.getLeft(), isSnapToPixel);
        double right = snapSpace(childMargin.getRight(), isSnapToPixel);

        if (child.isResizable()) {
            Vec2d size = boundedNodeSizeWithBias(child, areaWidth - left - right, areaHeight - top - bottom,
                    fillWidth, fillHeight, TEMP_VEC2D);
            child.resize(snapSize(size.x, isSnapToPixel),snapSize(size.y, isSnapToPixel));
        }
        position(child, areaX, areaY, areaWidth, areaHeight, areaBaselineOffset,
                top, right, bottom, left, halignment, valignment, isSnapToPixel);
    }

    private static void position(Node child, double areaX, double areaY, double areaWidth, double areaHeight,
                          double areaBaselineOffset,
                          double topMargin, double rightMargin, double bottomMargin, double leftMargin,
                          HPos hpos, VPos vpos, boolean isSnapToPixel) {
        final double xoffset = leftMargin + computeXOffset(areaWidth - leftMargin - rightMargin,
                                                     child.getLayoutBounds().getWidth(), hpos);
        final double yoffset = topMargin +
                      (vpos == VPos.BASELINE?
                          areaBaselineOffset - child.getBaselineOffset() :
                          computeYOffset(areaHeight - topMargin - bottomMargin,
                                         child.getLayoutBounds().getHeight(), vpos));
        final double x = snapPosition(areaX + xoffset, isSnapToPixel);
        final double y = snapPosition(areaY + yoffset, isSnapToPixel);

        child.relocate(x,y);
    }

     /**************************************************************************
     *                                                                         *
     * PG Implementation                                                       *
     *                                                                         *
     **************************************************************************/

    /** @treatAsPrivate */
    @Override public void impl_updatePG() {
        super.impl_updatePG();
        if (_shape != null) _shape.impl_syncPGNode();
        PGRegion pg = (PGRegion) impl_getPGNode();

        final boolean sizeChanged = impl_isDirty(DirtyBits.NODE_GEOMETRY);
        if (sizeChanged) {
            pg.setSize((float)getWidth(), (float)getHeight());
        }

        // NOTE: The order here is very important. There is logic in NGRegion which determines
        // whether we can cache an image representing this region, and for this to work correctly,
        // the shape must be specified before the background which is before the border.
        final boolean shapeChanged = impl_isDirty(DirtyBits.REGION_SHAPE);
        if (shapeChanged) {
            pg.updateShape(_shape, isScaleShape(), isCenterShape(), isCacheShape());
        }

        final boolean backgroundChanged = impl_isDirty(DirtyBits.SHAPE_FILL);
        final Background bg = getBackground();
        if (backgroundChanged) {
            pg.updateBackground(bg);
        }

        if (impl_isDirty(DirtyBits.SHAPE_STROKE)) {
            pg.updateBorder(getBorder());
        }

        if (sizeChanged || backgroundChanged || shapeChanged) {
            // TODO Make sure there is a test ensuring that stroke borders do not contribute to insets or opaque insets
            // If the background is determined by a shape, then we don't care (for now) what the opaque insets
            // of the Background are. If the developer specified opaque insets, we will use them, otherwise
            // we will make sure the opaque insets are cleared
            final Insets i = getOpaqueInsets();
            if (_shape != null) {
                if (i != null) {
                    pg.setOpaqueInsets((float) i.getTop(), (float) i.getRight(),
                                       (float) i.getBottom(), (float) i.getLeft());
                } else {
                    pg.setOpaqueInsets(Float.NaN, Float.NaN, Float.NaN, Float.NaN);
                }
            } else if (bg == null || bg.isEmpty() || !bg.hasOpaqueFill) {
                // If the background is null or empty or has no opaque fills, then we will forbear
                // computing the opaque insets, and just send null down.
                pg.setOpaqueInsets(Float.NaN, Float.NaN, Float.NaN, Float.NaN);
            } else if (i == null) {
                // There are no opaqueInsets specified by the developer (the most common case), so
                // I will have to compute them.
                // TODO If I could determine whether an Image were opaque, I could also compute
                // the opaqueInsets for a BackgroundImage and BorderImage.
                final double[] trbl = new double[4];
                bg.computeOpaqueInsets(getWidth(), getHeight(), trbl);
                pg.setOpaqueInsets((float) trbl[0], (float) trbl[1], (float) trbl[2], (float) trbl[3]);
            } else {
                // TODO For now, I'm just going to honor the opaqueInsets. Really I would want
                // to check to see if the computed opaqueTop, right, etc on the Background is
                // broader than the supplied opaque insets and adjust accordingly, but for now
                // I won't bother.
                pg.setOpaqueInsets((float) i.getTop(), (float) i.getRight(),
                                   (float) i.getBottom(), (float) i.getLeft());
            }
        }
    }

    /** @treatAsPrivate */
    @Override public PGNode impl_createPGNode() {
        return Toolkit.getToolkit().createPGRegion();
    }

    /**
     * @treatAsPrivate implementation detail
     * @deprecated This is an internal API that is not intended for use and will be removed in the next version
     */
    @Deprecated
    @Override protected boolean impl_computeContains(double localX, double localY) {
        // NOTE: This method only gets called if a quick check of bounds has already
        // occurred, so there is no need to test against bound again. We know that the
        // point (localX, localY) falls within the bounds of this node, now we need
        // to determine if it falls within the geometry of this node.
        // Also note that because Region defaults pickOnBounds to true, this code is
        // not usually executed. It will only be executed if pickOnBounds is set to false.

        final double x2 = getWidth();
        final double y2 = getHeight();
        // Figure out what the maximum possible radius value is.
        final double maxRadius = Math.min(x2 / 2.0, y2 / 2.0);

        // First check the shape. Shape could be impacted by scaleShape & positionShape properties.
        // This is going to be ugly! The problem is that basically all the scale / position operations
        // have to be implemented here in Region, whereas right now they are all implemented in
        // NGRegion. Drat. Basically I can't implement this properly until I have a way to get the
        // geometry backing an arbitrary FX shape. For example, in this case I need an NGShape peer
        // of this shape so that I can resize it as appropriate for these picking tests.
        // Lacking that, for now, I will simply check the shape (so that picking works for pie charts)
        // Bug is filed as RT-27775.
        if (_shape != null) {
            return _shape.contains(localX, localY);
        }

        // OK, there was no background shape, so I'm going to work on the principle of
        // nested rounded rectangles. We'll start by checking the backgrounds. The
        // first background which passes the test is good enough for us!
        final Background background = getBackground();
        if (background != null) {
            final List<BackgroundFill> fills = background.getFills();
            for (int i = 0, max = fills.size(); i < max; i++) {
                final BackgroundFill bgFill = fills.get(i);
                if (contains(localX, localY, 0, 0, x2, y2, bgFill.getInsets(), bgFill.getRadii(), maxRadius)) {
                    return true;
                }
            }
        }

        // If we are here then either there were no background fills or there were no background
        // fills which contained the point, and the region is not defined by a shape.
        final Border border = getBorder();
        if (border != null) {
            // Check all the stroke borders first. If the pick occurs on any stroke border
            // then we consider the contains test to have passed. Semantically we will treat a Region
            // with a border as if it were a rectangle with a stroke but no fill.
            final List<BorderStroke> strokes = border.getStrokes();
            for (int i=0, max=strokes.size(); i<max; i++) {
                final BorderStroke strokeBorder = strokes.get(i);
                if (contains(localX, localY, 0, 0, x2, y2, strokeBorder.getWidths(), false, strokeBorder.getInsets(),
                             strokeBorder.getRadii(), maxRadius)) {
                    return true;
                }
            }

            // Check the image borders. We treat the image border as though it is opaque.
            final List<BorderImage> images = border.getImages();
            for (int i = 0, max = images.size(); i < max; i++) {
                final BorderImage borderImage = images.get(i);
                if (contains(localX, localY, 0, 0, x2, y2, borderImage.getWidths(), borderImage.isFilled(),
                             borderImage.getInsets(), CornerRadii.EMPTY, maxRadius)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Basically we will perform two contains tests. For a point to be on the stroke, it must
     * be within the outermost edge of the stroke, but outside the innermost edge of the stroke.
     * Unless it is filled, in which case it is really just a normal contains test.
     *
     * @param px        The x position of the point to test
     * @param py        The y position of the point to test
     * @param x1        The x1 position of the bounds to test
     * @param y1        The y1 position of the bounds to test
     * @param x2        The x2 position of the bounds to test
     * @param y2        The y2 position of the bounds to test
     * @param widths    The widths of the stroke on each side
     * @param filled    Whether the area is filled or is just stroked
     * @param insets    The insets to apply to (x1,y1)-(x2,y2) to get the final bounds to test
     * @param rad       The corner radii to test with. Must not be null.
     * @param maxRadius The maximum possible radius value
     * @return True if (px, py) is within the stroke, taking into account insets and corner radii.
     */
    private boolean contains(final double px, final double py,
                             final double x1, final double y1, final double x2, final double y2,
                             BorderWidths widths, boolean filled,
                             final Insets insets, final CornerRadii rad, final double maxRadius) {
        if (filled) {
            if (contains(px, py, x1, y1, x2, y2, insets, rad, maxRadius)) {
                return true;
            }
        } else {
            boolean insideOuterEdge = contains(px, py, x1, y1, x2, y2, insets, rad, maxRadius);
            if (insideOuterEdge) {
                boolean outsideInnerEdge = !contains(px, py,
                    x1 + (widths.isLeftAsPercentage() ? getWidth() * widths.getLeft() : widths.getLeft()),
                    y1 + (widths.isTopAsPercentage() ? getHeight() * widths.getTop() : widths.getTop()),
                    x2 - (widths.isRightAsPercentage() ? getWidth() * widths.getRight() : widths.getRight()),
                    y2 - (widths.isBottomAsPercentage() ? getHeight() * widths.getBottom() : widths.getBottom()),
                    insets, rad, maxRadius);
                if (outsideInnerEdge) return true;
            }
        }
        return false;
    }

    /**
     * Determines whether the point (px, py) is contained within the the bounds (x1, y1)-(x2, y2),
     * after taking into account the insets and the corner radii.
     *
     * @param px        The x position of the point to test
     * @param py        The y position of the point to test
     * @param x1        The x1 position of the bounds to test
     * @param y1        The y1 position of the bounds to test
     * @param x2        The x2 position of the bounds to test
     * @param y2        The y2 position of the bounds to test
     * @param insets    The insets to apply to (x1,y1)-(x2,y2) to get the final bounds to test
     * @param rad       The corner radii to test with. Must not be null.
     * @param maxRadius The maximum possible radius value
     * @return True if (px, py) is within the bounds, taking into account insets and corner radii.
     */
    private boolean contains(final double px, final double py,
                             final double x1, final double y1, final double x2, final double y2,
                             final Insets insets, CornerRadii rad, final double maxRadius) {
        // These four values are the x0, y0, x1, y1 bounding box after
        // having taken into account the insets of this particular
        // background fill.
        final double rrx0 = x1 + insets.getLeft();
        final double rry0 = y1 + insets.getTop();
        final double rrx1 = x2 - insets.getRight();
        final double rry1 = y2 - insets.getBottom();

        // Adjust based on whether it is % based radii
        rad = normalize(rad);

        // Check for trivial rejection - point is inside bounding rectangle
        if (px >= rrx0 && py >= rry0 && px <= rrx1 && py <= rry1) {
            // The point was within the index bounding box. Now we need to analyze the
            // corner radii to see if the point lies within the corners or not. If the
            // point is within a corner then we reject this one.
            final double tlhr = Math.min(rad.getTopLeftHorizontalRadius(), maxRadius);
            if (rad.isUniform() && tlhr == 0) {
                // This is a simple square! Since we know the point is already within
                // the insets of this fill, we can simply return true.
                return true;
            } else {
                final double tlvr = Math.min(rad.getTopLeftVerticalRadius(), maxRadius);
                final double trhr = Math.min(rad.getTopRightHorizontalRadius(), maxRadius);
                final double trvr = Math.min(rad.getTopRightVerticalRadius(), maxRadius);
                final double blhr = Math.min(rad.getBottomLeftHorizontalRadius(), maxRadius);
                final double blvr = Math.min(rad.getBottomLeftVerticalRadius(), maxRadius);
                final double brhr = Math.min(rad.getBottomRightHorizontalRadius(), maxRadius);
                final double brvr = Math.min(rad.getBottomRightVerticalRadius(), maxRadius);

                // The four corners can each be described as a quarter of an ellipse
                double centerX, centerY, a, b;

                if (px <= rrx0 + tlhr && py <= rry0 + tlvr) {
                    // Point is in the top left corner
                    centerX = rrx0 + tlhr;
                    centerY = rry0 + tlvr;
                    a = tlhr;
                    b = tlvr;
                } else if (px >= rrx1 - trhr && py <= rry0 + trvr) {
                    // Point is in the top right corner
                    centerX = rrx1 - trhr;
                    centerY = rry0 + trvr;
                    a = trhr;
                    b = trvr;
                } else if (px >= rrx1 - brhr && py >= rry1 - brvr) {
                    // Point is in the bottom right corner
                    centerX = rrx1 - brhr;
                    centerY = rry1 - brvr;
                    a = brhr;
                    b = brvr;
                } else if (px <= rrx0 + blhr && py >= rry1 - blvr) {
                    // Point is in the bottom left corner
                    centerX = rrx0 + blhr;
                    centerY = rry1 - blvr;
                    a = blhr;
                    b = blvr;
                } else {
                    // The point must have been in the solid body someplace
                    return true;
                }

                double x = px - centerX;
                double y = py - centerY;
                double result = ((x*x)/(a*a) + (y*y)/(b*b));
                // The .0000001 is fudge to help in cases where double arithmetic isn't quite right
                if (result - .0000001 <= 1) return true;
            }
        }
        return false;
    }

    /**
     * Direct copy of a method in NGRegion. If NGRegion were part of the core graphics module (coming!)
     * then this method could be removed.
     *
     * @param radii    The radii.
     * @return Normalized radii.
     */
    private CornerRadii normalize(CornerRadii radii) {
        final double width = getWidth();
        final double height = getHeight();
        final double tlvr = radii.isTopLeftVerticalRadiusAsPercentage() ? height * radii.getTopLeftVerticalRadius() : radii.getTopLeftVerticalRadius();
        final double tlhr = radii.isTopLeftHorizontalRadiusAsPercentage() ? width * radii.getTopLeftHorizontalRadius() : radii.getTopLeftHorizontalRadius();
        final double trvr = radii.isTopRightVerticalRadiusAsPercentage() ? height * radii.getTopRightVerticalRadius() : radii.getTopRightVerticalRadius();
        final double trhr = radii.isTopRightHorizontalRadiusAsPercentage() ? width * radii.getTopRightHorizontalRadius() : radii.getTopRightHorizontalRadius();
        final double brvr = radii.isBottomRightVerticalRadiusAsPercentage() ? height * radii.getBottomRightVerticalRadius() : radii.getBottomRightVerticalRadius();
        final double brhr = radii.isBottomRightHorizontalRadiusAsPercentage() ? width * radii.getBottomRightHorizontalRadius() : radii.getBottomRightHorizontalRadius();
        final double blvr = radii.isBottomLeftVerticalRadiusAsPercentage() ? height * radii.getBottomLeftVerticalRadius() : radii.getBottomLeftVerticalRadius();
        final double blhr = radii.isBottomLeftHorizontalRadiusAsPercentage() ? width * radii.getBottomLeftHorizontalRadius() : radii.getBottomLeftHorizontalRadius();
        return new CornerRadii(tlhr, tlvr, trvr, trhr, brhr, brvr, blvr, blhr, false, false, false, false, false, false, false, false);
    }

    /**
     * Some skins relying on this
     * @treatAsPrivate implementation detail
     * @deprecated This is an internal API that is not intended for use and will be removed in the next version
     */
    @Deprecated
    @Override protected void impl_pickNodeLocal(PickRay pickRay, PickResultChooser result) {

        double boundsDistance = impl_intersectsBounds(pickRay);

        if (!Double.isNaN(boundsDistance)) {
            ObservableList<Node> children = getChildren();
            for (int i = children.size()-1; i >= 0; i--) {
                children.get(i).impl_pickNode(pickRay, result);
                if (result.isClosed()) {
                    return;
                }
            }

            impl_intersects(pickRay, result);
        }
    }

    private Bounds boundingBox;

    /**
     * The layout bounds of this region: {@code 0, 0  width x height}
     *
     * @treatAsPrivate implementation detail
     * @deprecated This is an internal API that is not intended for use and will be removed in the next version
     */
    @Deprecated
    @Override protected final Bounds impl_computeLayoutBounds() {
        if (boundingBox == null) {
            // we reuse the bounding box if the width and height haven't changed.
            boundingBox = new BoundingBox(0, 0, 0, getWidth(), getHeight(), 0);
        }
        return boundingBox;
    }

    /**
     * @treatAsPrivate implementation detail
     * @deprecated This is an internal API that is not intended for use and will be removed in the next version
     */
    @Deprecated
    @Override final protected void impl_notifyLayoutBoundsChanged() {
        // override Node's default behavior of having a geometric bounds change
        // trigger a change in layoutBounds. For Resizable nodes, layoutBounds
        // is unrelated to geometric bounds.
    }

    /**
     * @treatAsPrivate implementation detail
     * @deprecated This is an internal API that is not intended for use and will be removed in the next version
     */
    @Deprecated
    @Override public BaseBounds impl_computeGeomBounds(BaseBounds bounds, BaseTransform tx) {
        // Unlike Group, a Region has its own intrinsic geometric bounds, even if it has no children.
        // The bounds of the Region must take into account any backgrounds and borders and how
        // they are used to draw the Region. The geom bounds must always take into account
        // all pixels drawn (because the geom bounds forms the basis of the dirty regions).
        // Note that the layout bounds of a Region is not based on the geom bounds.

        // Define some variables to hold the top-left and bottom-right corners of the bounds
        double bx1 = 0;
        double by1 = 0;
        double bx2 = getWidth();
        double by2 = getHeight();

        // If the shape is defined, then the top-left and bottom-right corner positions
        // need to be redefined
        if (_shape != null && isScaleShape() == false) {
            final Bounds layoutBounds = _shape.getLayoutBounds();
            final double shapeWidth = layoutBounds.getWidth();
            final double shapeHeight = layoutBounds.getHeight();
            if (isCenterShape()) {
                bx1 = (bx2 - shapeWidth) / 2;
                by1 = (by2 - shapeHeight) / 2;
                bx2 = bx1 + shapeWidth;
                by2 = by1 + shapeHeight;
            } else {
                bx1 = layoutBounds.getMinX();
                by1 = layoutBounds.getMinY();
                bx2 = layoutBounds.getMaxX();
                by2 = layoutBounds.getMaxY();
            }
        }

        // Expand the bounds to include the outsets from the background and border.
        // The outsets are the opposite of insets -- a measure of distance from the
        // edge of the Region outward. The outsets cannot, however, be negative.
        final Background background = getBackground();
        final Border border = getBorder();
        final Insets backgroundOutsets = background == null ? Insets.EMPTY : background.getOutsets();
        final Insets borderOutsets = border == null ? Insets.EMPTY : border.getOutsets();
        bx1 -= Math.max(backgroundOutsets.getLeft(), borderOutsets.getLeft());
        by1 -= Math.max(backgroundOutsets.getTop(), borderOutsets.getTop());
        bx2 += Math.max(backgroundOutsets.getRight(), borderOutsets.getRight());
        by2 += Math.max(backgroundOutsets.getBottom(), borderOutsets.getBottom());

        // NOTE: Okay to call impl_computeGeomBounds with tx even in the 3D case
        // since Parent.impl_computeGeomBounds does handle 3D correctly.
        BaseBounds cb = super.impl_computeGeomBounds(bounds, tx);
        /*
         * This is a work around for RT-7680. Parent returns invalid bounds from
         * impl_computeGeomBounds when it has no children or if all its children
         * have invalid bounds. If RT-7680 were fixed, then we could omit this
         * first branch of the if and only use the else since the correct value
         * would be computed.
         */
        if (cb.isEmpty()) {
            // There are no children bounds, so
            bounds = bounds.deriveWithNewBounds(
                    (float)bx1, (float)by1, 0.0f,
                    (float)bx2, (float)by2, 0.0f);
            bounds = tx.transform(bounds, bounds);
            return bounds;
        } else {
            // Union with children's bounds
            BaseBounds tempBounds = TempState.getInstance().bounds;
            tempBounds = tempBounds.deriveWithNewBounds(
                    (float)bx1, (float)by1, 0.0f,
                    (float)bx2, (float)by2, 0.0f);
            BaseBounds bb = tx.transform(tempBounds, tempBounds);
            cb = cb.deriveWithUnion(bb);
            return cb;
        }
    }

    /***************************************************************************
     *                                                                         *
     * CSS                                                                     *
     *                                                                         *
     **************************************************************************/

     /**
      * Super-lazy instantiation pattern from Bill Pugh.
      * @treatAsPrivate implementation detail
      */
     private static class StyleableProperties {
         private static final CssMetaData<Region,Insets> PADDING =
             new CssMetaData<Region,Insets>("-fx-padding",
                 InsetsConverter.getInstance(), Insets.EMPTY) {

            @Override public boolean isSettable(Region node) {
                return node.padding == null || !node.padding.isBound();
            }

            @Override public StyleableProperty<Insets> getStyleableProperty(Region node) {
                return (StyleableProperty<Insets>)node.paddingProperty();
            }
         };

         private static final CssMetaData<Region,Insets> OPAQUE_INSETS =
                 new CssMetaData<Region,Insets>("-fx-opaque-insets",
                         InsetsConverter.getInstance(), null) {

                     @Override
                     public boolean isSettable(Region node) {
                         return node.opaqueInsets == null || !node.opaqueInsets.isBound();
                     }

                     @Override
                     public StyleableProperty<Insets> getStyleableProperty(Region node) {
                         return (StyleableProperty<Insets>)node.opaqueInsetsProperty();
                     }

                 };

         private static final CssMetaData<Region,Background> BACKGROUND =
             new CssMetaData<Region,Background>("-fx-region-background",
                 BackgroundConverter.INSTANCE,
                 null,
                 false,
                 Background.getClassCssMetaData()) {

            @Override public boolean isSettable(Region node) {
                return !node.background.isBound();
            }

            @Override public StyleableProperty<Background> getStyleableProperty(Region node) {
                return (StyleableProperty<Background>)node.background;
            }
         };

         private static final CssMetaData<Region,Border> BORDER =
             new CssMetaData<Region,Border>("-fx-region-border",
                     BorderConverter.getInstance(),
                     null,
                     false,
                     Border.getClassCssMetaData()) {

                 @Override public boolean isSettable(Region node) {
                     return !node.background.isBound();
                 }

                 @Override public StyleableProperty<Border> getStyleableProperty(Region node) {
                     return (StyleableProperty<Border>)node.border;
                 }
             };

         private static final CssMetaData<Region,Shape> SHAPE =
             new CssMetaData<Region,Shape>("-fx-shape",
                 ShapeConverter.getInstance()) {

            @Override public boolean isSettable(Region node) {
                // isSettable depends on node.shape, not node.shapeContent
                return node.shape == null || !node.shape.isBound();
            }

            @Override public StyleableProperty<Shape> getStyleableProperty(Region node) {
                return (StyleableProperty<Shape>)node.shapeProperty();
            }
         };

         private static final CssMetaData<Region, Boolean> SCALE_SHAPE =
             new CssMetaData<Region,Boolean>("-fx-scale-shape",
                 BooleanConverter.getInstance(), Boolean.TRUE){

            @Override public boolean isSettable(Region node) {
                return node.scaleShape == null || !node.scaleShape.isBound();
            }

            @Override public StyleableProperty<Boolean> getStyleableProperty(Region node) {
                return (StyleableProperty<Boolean>)node.scaleShapeProperty();
            }
        };

         private static final CssMetaData<Region,Boolean> POSITION_SHAPE =
             new CssMetaData<Region,Boolean>("-fx-position-shape",
                 BooleanConverter.getInstance(), Boolean.TRUE){

            @Override public boolean isSettable(Region node) {
                return node.centerShape == null || !node.centerShape.isBound();
            }

            @Override public StyleableProperty<Boolean> getStyleableProperty(Region node) {
                return (StyleableProperty<Boolean>)node.centerShapeProperty();
            }
        };

         private static final CssMetaData<Region,Boolean> CACHE_SHAPE =
             new CssMetaData<Region,Boolean>("-fx-cache-shape",
                 BooleanConverter.getInstance(), Boolean.TRUE){

            @Override public boolean isSettable(Region node) {
                return node.cacheShape == null || !node.cacheShape.isBound();
            }

            @Override public StyleableProperty<Boolean> getStyleableProperty(Region node) {
                return (StyleableProperty<Boolean>)node.cacheShapeProperty();
            }
        };

         private static final CssMetaData<Region, Boolean> SNAP_TO_PIXEL =
             new CssMetaData<Region,Boolean>("-fx-snap-to-pixel",
                 BooleanConverter.getInstance(), Boolean.TRUE){

            @Override public boolean isSettable(Region node) {
                return node.snapToPixel == null ||
                        !node.snapToPixel.isBound();
            }

            @Override public StyleableProperty<Boolean> getStyleableProperty(Region node) {
                return (StyleableProperty<Boolean>)node.snapToPixelProperty();
            }
        };

         private static final CssMetaData<Region, Number> MIN_HEIGHT =
             new CssMetaData<Region,Number>("-fx-min-height",
                 SizeConverter.getInstance(), USE_COMPUTED_SIZE){

            @Override public boolean isSettable(Region node) {
                return node.minHeight == null ||
                        !node.minHeight.isBound();
            }

            @Override public StyleableProperty<Number> getStyleableProperty(Region node) {
                return (StyleableProperty<Number>)node.minHeightProperty();
            }
        };

         private static final CssMetaData<Region, Number> PREF_HEIGHT =
             new CssMetaData<Region,Number>("-fx-pref-height",
                 SizeConverter.getInstance(), USE_COMPUTED_SIZE){

            @Override public boolean isSettable(Region node) {
                return node.prefHeight == null ||
                        !node.prefHeight.isBound();
            }

            @Override public StyleableProperty<Number> getStyleableProperty(Region node) {
                return (StyleableProperty<Number>)node.prefHeightProperty();
            }
        };

         private static final CssMetaData<Region, Number> MAX_HEIGHT =
             new CssMetaData<Region,Number>("-fx-max-height",
                 SizeConverter.getInstance(), USE_COMPUTED_SIZE){

            @Override public boolean isSettable(Region node) {
                return node.maxHeight == null ||
                        !node.maxHeight.isBound();
            }

            @Override public StyleableProperty<Number> getStyleableProperty(Region node) {
                return (StyleableProperty<Number>)node.maxHeightProperty();
            }
        };

         private static final CssMetaData<Region, Number> MIN_WIDTH =
             new CssMetaData<Region,Number>("-fx-min-width",
                 SizeConverter.getInstance(), USE_COMPUTED_SIZE){

            @Override public boolean isSettable(Region node) {
                return node.minWidth == null ||
                        !node.minWidth.isBound();
            }

            @Override public StyleableProperty<Number> getStyleableProperty(Region node) {
                return (StyleableProperty<Number>)node.minWidthProperty();
            }
        };

         private static final CssMetaData<Region, Number> PREF_WIDTH =
             new CssMetaData<Region,Number>("-fx-pref-width",
                 SizeConverter.getInstance(), USE_COMPUTED_SIZE){

            @Override public boolean isSettable(Region node) {
                return node.prefWidth == null ||
                        !node.prefWidth.isBound();
            }

            @Override public StyleableProperty<Number> getStyleableProperty(Region node) {
                return (StyleableProperty<Number>)node.prefWidthProperty();
            }
        };

         private static final CssMetaData<Region, Number> MAX_WIDTH =
             new CssMetaData<Region,Number>("-fx-max-width",
                 SizeConverter.getInstance(), USE_COMPUTED_SIZE){

            @Override public boolean isSettable(Region node) {
                return node.maxWidth == null ||
                        !node.maxWidth.isBound();
            }

            @Override public StyleableProperty<Number> getStyleableProperty(Region node) {
                return (StyleableProperty<Number>)node.maxWidthProperty();
            }
        };

         private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
         static {

            final List<CssMetaData<? extends Styleable, ?>> styleables =
                new ArrayList<CssMetaData<? extends Styleable, ?>>(Parent.getClassCssMetaData());
            styleables.add(PADDING);
            styleables.add(BACKGROUND);
            styleables.add(BORDER);
            styleables.add(OPAQUE_INSETS);
            styleables.add(SHAPE);
            styleables.add(SCALE_SHAPE);
            styleables.add(POSITION_SHAPE);
            styleables.add(SNAP_TO_PIXEL);
            styleables.add(MIN_WIDTH);
            styleables.add(PREF_WIDTH);
            styleables.add(MAX_WIDTH);
            styleables.add(MIN_HEIGHT);
            styleables.add(PREF_HEIGHT);
            styleables.add(MAX_HEIGHT);
            STYLEABLES = Collections.unmodifiableList(styleables);
         }
    }

    /**
     * @return The CssMetaData associated with this class, which may include the
     * CssMetaData of its super classes.
     * @since JavaFX 8.0
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /**
     * {@inheritDoc}
     *
     * @since JavaFX 8.0
     */


    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return getClassCssMetaData();
    }

=======
package redis.clients.jedis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.util.SafeEncoder;
import redis.clients.util.Slowlog;

public class Jedis extends BinaryJedis implements JedisCommands {
    public Jedis(final String host) {
	super(host);
    }

    public Jedis(final String host, final int port) {
	super(host, port);
    }

    public Jedis(final String host, final int port, final int timeout) {
	super(host, port, timeout);
    }

    public Jedis(JedisShardInfo shardInfo) {
	super(shardInfo);
    }

    public String ping() {
	checkIsInMulti();
	client.ping();
	return client.getStatusCodeReply();
    }

    /**
     * Set the string value as value of the key. The string can't be longer than
     * 1073741824 bytes (1 GB).
     * <p>
     * Time complexity: O(1)
     * 
     * @param key
     * @param value
     * @return Status code reply
     */
    public String set(final String key, String value) {
	checkIsInMulti();
	client.set(key, value);
	return client.getStatusCodeReply();
    }

    /**
     * Get the value of the specified key. If the key does not exist the special
     * value 'nil' is returned. If the value stored at key is not a string an
     * error is returned because GET can only handle string values.
     * <p>
     * Time complexity: O(1)
     * 
     * @param key
     * @return Bulk reply
     */
    public String get(final String key) {
	checkIsInMulti();
	client.sendCommand(Protocol.Command.GET, key);
	return client.getBulkReply();
    }

    /**
     * Ask the server to silently close the connection.
     */

    public String quit() {
	checkIsInMulti();
	client.quit();
	return client.getStatusCodeReply();
    }

    /**
     * Test if the specified key exists. The command returns "1" if the key
     * exists, otherwise "0" is returned. Note that even keys set with an empty
     * string as value will return "1".
     * 
     * Time complexity: O(1)
     * 
     * @param key
     * @return Boolean reply, true if the key exists, otherwise false
     */
    public Boolean exists(final String key) {
	checkIsInMulti();
	client.exists(key);
	return client.getIntegerReply() == 1;
    }

    /**
     * Remove the specified keys. If a given key does not exist no operation is
     * performed for this key. The command returns the number of keys removed.
     * 
     * Time complexity: O(1)
     * 
     * @param keys
     * @return Integer reply, specifically: an integer greater than 0 if one or
     *         more keys were removed 0 if none of the specified key existed
     */
    public Long del(final String... keys) {
	checkIsInMulti();
	client.del(keys);
	return client.getIntegerReply();
    }

    /**
     * Return the type of the value stored at key in form of a string. The type
     * can be one of "none", "string", "list", "set". "none" is returned if the
     * key does not exist.
     * 
     * Time complexity: O(1)
     * 
     * @param key
     * @return Status code reply, specifically: "none" if the key does not exist
     *         "string" if the key contains a String value "list" if the key
     *         contains a List value "set" if the key contains a Set value
     *         "zset" if the key contains a Sorted Set value "hash" if the key
     *         contains a Hash value
     */
    public String type(final String key) {
	checkIsInMulti();
	client.type(key);
	return client.getStatusCodeReply();
    }

    /**
     * Delete all the keys of the currently selected DB. This command never
     * fails.
     * 
     * @return Status code reply
     */

    public String flushDB() {
	checkIsInMulti();
	client.flushDB();
	return client.getStatusCodeReply();
    }

    /**
     * Returns all the keys matching the glob-style pattern as space separated
     * strings. For example if you have in the database the keys "foo" and
     * "foobar" the command "KEYS foo*" will return "foo foobar".
     * <p>
     * Note that while the time complexity for this operation is O(n) the
     * constant times are pretty low. For example Redis running on an entry
     * level laptop can scan a 1 million keys database in 40 milliseconds.
     * <b>Still it's better to consider this one of the slow commands that may
     * ruin the DB performance if not used with care.</b>
     * <p>
     * In other words this command is intended only for debugging and special
     * operations like creating a script to change the DB schema. Don't use it
     * in your normal code. Use Redis Sets in order to group together a subset
     * of objects.
     * <p>
     * Glob style patterns examples:
     * <ul>
     * <li>h?llo will match hello hallo hhllo
     * <li>h*llo will match hllo heeeello
     * <li>h[ae]llo will match hello and hallo, but not hillo
     * </ul>
     * <p>
     * Use \ to escape special chars if you want to match them verbatim.
     * <p>
     * Time complexity: O(n) (with n being the number of keys in the DB, and
     * assuming keys and pattern of limited length)
     * 
     * @param pattern
     * @return Multi bulk reply
     */
    public Set<String> keys(final String pattern) {
	checkIsInMulti();
	client.keys(pattern);
	return BuilderFactory.STRING_SET
		.build(client.getBinaryMultiBulkReply());
    }

    /**
     * Return a randomly selected key from the currently selected DB.
     * <p>
     * Time complexity: O(1)
     * 
     * @return Singe line reply, specifically the randomly selected key or an
     *         empty string is the database is empty
     */
    public String randomKey() {
	checkIsInMulti();
	client.randomKey();
	return client.getBulkReply();
    }

    /**
     * Atomically renames the key oldkey to newkey. If the source and
     * destination name are the same an error is returned. If newkey already
     * exists it is overwritten.
     * <p>
     * Time complexity: O(1)
     * 
     * @param oldkey
     * @param newkey
     * @return Status code repy
     */
    public String rename(final String oldkey, final String newkey) {
	checkIsInMulti();
	client.rename(oldkey, newkey);
	return client.getStatusCodeReply();
    }

    /**
     * Rename oldkey into newkey but fails if the destination key newkey already
     * exists.
     * <p>
     * Time complexity: O(1)
     * 
     * @param oldkey
     * @param newkey
     * @return Integer reply, specifically: 1 if the key was renamed 0 if the
     *         target key already exist
     */
    public Long renamenx(final String oldkey, final String newkey) {
	checkIsInMulti();
	client.renamenx(oldkey, newkey);
	return client.getIntegerReply();
    }

    /**
     * Set a timeout on the specified key. After the timeout the key will be
     * automatically deleted by the server. A key with an associated timeout is
     * said to be volatile in Redis terminology.
     * <p>
     * Voltile keys are stored on disk like the other keys, the timeout is
     * persistent too like all the other aspects of the dataset. Saving a
     * dataset containing expires and stopping the server does not stop the flow
     * of time as Redis stores on disk the time when the key will no longer be
     * available as Unix time, and not the remaining seconds.
     * <p>
     * Since Redis 2.1.3 you can update the value of the timeout of a key
     * already having an expire set. It is also possible to undo the expire at
     * all turning the key into a normal key using the {@link #persist(String)
     * PERSIST} command.
     * <p>
     * Time complexity: O(1)
     * 
     * @see <ahref="http://code.google.com/p/redis/wiki/ExpireCommand">ExpireCommand</a>
     * 
     * @param key
     * @param seconds
     * @return Integer reply, specifically: 1: the timeout was set. 0: the
     *         timeout was not set since the key already has an associated
     *         timeout (this may happen only in Redis versions < 2.1.3, Redis >=
     *         2.1.3 will happily update the timeout), or the key does not
     *         exist.
     */
    public Long expire(final String key, final int seconds) {
	checkIsInMulti();
	client.expire(key, seconds);
	return client.getIntegerReply();
    }

    /**
     * EXPIREAT works exctly like {@link #expire(String, int) EXPIRE} but
     * instead to get the number of seconds representing the Time To Live of the
     * key as a second argument (that is a relative way of specifing the TTL),
     * it takes an absolute one in the form of a UNIX timestamp (Number of
     * seconds elapsed since 1 Gen 1970).
     * <p>
     * EXPIREAT was introduced in order to implement the Append Only File
     * persistence mode so that EXPIRE commands are automatically translated
     * into EXPIREAT commands for the append only file. Of course EXPIREAT can
     * also used by programmers that need a way to simply specify that a given
     * key should expire at a given time in the future.
     * <p>
     * Since Redis 2.1.3 you can update the value of the timeout of a key
     * already having an expire set. It is also possible to undo the expire at
     * all turning the key into a normal key using the {@link #persist(String)
     * PERSIST} command.
     * <p>
     * Time complexity: O(1)
     * 
     * @see <ahref="http://code.google.com/p/redis/wiki/ExpireCommand">ExpireCommand</a>
     * 
     * @param key
     * @param unixTime
     * @return Integer reply, specifically: 1: the timeout was set. 0: the
     *         timeout was not set since the key already has an associated
     *         timeout (this may happen only in Redis versions < 2.1.3, Redis >=
     *         2.1.3 will happily update the timeout), or the key does not
     *         exist.
     */
    public Long expireAt(final String key, final long unixTime) {
	checkIsInMulti();
	client.expireAt(key, unixTime);
	return client.getIntegerReply();
    }

    /**
     * The TTL command returns the remaining time to live in seconds of a key
     * that has an {@link #expire(String, int) EXPIRE} set. This introspection
     * capability allows a Redis client to check how many seconds a given key
     * will continue to be part of the dataset.
     * 
     * @param key
     * @return Integer reply, returns the remaining time to live in seconds of a
     *         key that has an EXPIRE. If the Key does not exists or does not
     *         have an associated expire, -1 is returned.
     */
    public Long ttl(final String key) {
	checkIsInMulti();
	client.ttl(key);
	return client.getIntegerReply();
    }

    /**
     * Select the DB with having the specified zero-based numeric index. For
     * default every new client connection is automatically selected to DB 0.
     * 
     * @param index
     * @return Status code reply
     */

    public String select(final int index) {
	checkIsInMulti();
	client.select(index);
	return client.getStatusCodeReply();
    }

    /**
     * Move the specified key from the currently selected DB to the specified
     * destination DB. Note that this command returns 1 only if the key was
     * successfully moved, and 0 if the target key was already there or if the
     * source key was not found at all, so it is possible to use MOVE as a
     * locking primitive.
     * 
     * @param key
     * @param dbIndex
     * @return Integer reply, specifically: 1 if the key was moved 0 if the key
     *         was not moved because already present on the target DB or was not
     *         found in the current DB.
     */
    public Long move(final String key, final int dbIndex) {
	checkIsInMulti();
	client.move(key, dbIndex);
	return client.getIntegerReply();
    }

    /**
     * Delete all the keys of all the existing databases, not just the currently
     * selected one. This command never fails.
     * 
     * @return Status code reply
     */

    public String flushAll() {
	checkIsInMulti();
	client.flushAll();
	return client.getStatusCodeReply();
    }

    /**
     * GETSET is an atomic set this value and return the old value command. Set
     * key to the string value and return the old value stored at key. The
     * string can't be longer than 1073741824 bytes (1 GB).
     * <p>
     * Time complexity: O(1)
     * 
     * @param key
     * @param value
     * @return Bulk reply
     */
    public String getSet(final String key, final String value) {
	checkIsInMulti();
	client.getSet(key, value);
	return client.getBulkReply();
    }

    /**
     * Get the values of all the specified keys. If one or more keys dont exist
     * or is not of type String, a 'nil' value is returned instead of the value
     * of the specified key, but the operation never fails.
     * <p>
     * Time complexity: O(1) for every key
     * 
     * @param keys
     * @return Multi bulk reply
     */
    public List<String> mget(final String... keys) {
	checkIsInMulti();
	client.mget(keys);
	return client.getMultiBulkReply();
    }

    /**
     * SETNX works exactly like {@link #set(String, String) SET} with the only
     * difference that if the key already exists no operation is performed.
     * SETNX actually means "SET if Not eXists".
     * <p>
     * Time complexity: O(1)
     * 
     * @param key
     * @param value
     * @return Integer reply, specifically: 1 if the key was set 0 if the key
     *         was not set
     */
    public Long setnx(final String key, final String value) {
	checkIsInMulti();
	client.setnx(key, value);
	return client.getIntegerReply();
    }

    /**
     * The command is exactly equivalent to the following group of commands:
     * {@link #set(String, String) SET} + {@link #expire(String, int) EXPIRE}.
     * The operation is atomic.
     * <p>
     * Time complexity: O(1)
     * 
     * @param key
     * @param seconds
     * @param value
     * @return Status code reply
     */
    public String setex(final String key, final int seconds, final String value) {
	checkIsInMulti();
	client.setex(key, seconds, value);
	return client.getStatusCodeReply();
    }

    /**
     * Set the the respective keys to the respective values. MSET will replace
     * old values with new values, while {@link #msetnx(String...) MSETNX} will
     * not perform any operation at all even if just a single key already
     * exists.
     * <p>
     * Because of this semantic MSETNX can be used in order to set different
     * keys representing different fields of an unique logic object in a way
     * that ensures that either all the fields or none at all are set.
     * <p>
     * Both MSET and MSETNX are atomic operations. This means that for instance
     * if the keys A and B are modified, another client talking to Redis can
     * either see the changes to both A and B at once, or no modification at
     * all.
     * 
     * @see #msetnx(String...)
     * 
     * @param keysvalues
     * @return Status code reply Basically +OK as MSET can't fail
     */
    public String mset(final String... keysvalues) {
	checkIsInMulti();
	client.mset(keysvalues);
	return client.getStatusCodeReply();
    }

    /**
     * Set the the respective keys to the respective values.
     * {@link #mset(String...) MSET} will replace old values with new values,
     * while MSETNX will not perform any operation at all even if just a single
     * key already exists.
     * <p>
     * Because of this semantic MSETNX can be used in order to set different
     * keys representing different fields of an unique logic object in a way
     * that ensures that either all the fields or none at all are set.
     * <p>
     * Both MSET and MSETNX are atomic operations. This means that for instance
     * if the keys A and B are modified, another client talking to Redis can
     * either see the changes to both A and B at once, or no modification at
     * all.
     * 
     * @see #mset(String...)
     * 
     * @param keysvalues
     * @return Integer reply, specifically: 1 if the all the keys were set 0 if
     *         no key was set (at least one key already existed)
     */
    public Long msetnx(final String... keysvalues) {
	checkIsInMulti();
	client.msetnx(keysvalues);
	return client.getIntegerReply();
    }

    /**
     * IDECRBY work just like {@link #decr(String) INCR} but instead to
     * decrement by 1 the decrement is integer.
     * <p>
     * INCR commands are limited to 64 bit signed integers.
     * <p>
     * Note: this is actually a string operation, that is, in Redis there are
     * not "integer" types. Simply the string stored at the key is parsed as a
     * base 10 64 bit signed integer, incremented, and then converted back as a
     * string.
     * <p>
     * Time complexity: O(1)
     * 
     * @see #incr(String)
     * @see #decr(String)
     * @see #incrBy(String, long)
     * 
     * @param key
     * @param integer
     * @return Integer reply, this commands will reply with the new value of key
     *         after the increment.
     */
    public Long decrBy(final String key, final long integer) {
	checkIsInMulti();
	client.decrBy(key, integer);
	return client.getIntegerReply();
    }

    /**
     * Decrement the number stored at key by one. If the key does not exist or
     * contains a value of a wrong type, set the key to the value of "0" before
     * to perform the decrement operation.
     * <p>
     * INCR commands are limited to 64 bit signed integers.
     * <p>
     * Note: this is actually a string operation, that is, in Redis there are
     * not "integer" types. Simply the string stored at the key is parsed as a
     * base 10 64 bit signed integer, incremented, and then converted back as a
     * string.
     * <p>
     * Time complexity: O(1)
     * 
     * @see #incr(String)
     * @see #incrBy(String, long)
     * @see #decrBy(String, long)
     * 
     * @param key
     * @return Integer reply, this commands will reply with the new value of key
     *         after the increment.
     */
    public Long decr(final String key) {
	checkIsInMulti();
	client.decr(key);
	return client.getIntegerReply();
    }

    /**
     * INCRBY work just like {@link #incr(String) INCR} but instead to increment
     * by 1 the increment is integer.
     * <p>
     * INCR commands are limited to 64 bit signed integers.
     * <p>
     * Note: this is actually a string operation, that is, in Redis there are
     * not "integer" types. Simply the string stored at the key is parsed as a
     * base 10 64 bit signed integer, incremented, and then converted back as a
     * string.
     * <p>
     * Time complexity: O(1)
     * 
     * @see #incr(String)
     * @see #decr(String)
     * @see #decrBy(String, long)
     * 
     * @param key
     * @param integer
     * @return Integer reply, this commands will reply with the new value of key
     *         after the increment.
     */
    public Long incrBy(final String key, final long integer) {
	checkIsInMulti();
	client.incrBy(key, integer);
	return client.getIntegerReply();
    }

    /**
     * Increment the number stored at key by one. If the key does not exist or
     * contains a value of a wrong type, set the key to the value of "0" before
     * to perform the increment operation.
     * <p>
     * INCR commands are limited to 64 bit signed integers.
     * <p>
     * Note: this is actually a string operation, that is, in Redis there are
     * not "integer" types. Simply the string stored at the key is parsed as a
     * base 10 64 bit signed integer, incremented, and then converted back as a
     * string.
     * <p>
     * Time complexity: O(1)
     * 
     * @see #incrBy(String, long)
     * @see #decr(String)
     * @see #decrBy(String, long)
     * 
     * @param key
     * @return Integer reply, this commands will reply with the new value of key
     *         after the increment.
     */
    public Long incr(final String key) {
	checkIsInMulti();
	client.incr(key);
	return client.getIntegerReply();
    }

    /**
     * If the key already exists and is a string, this command appends the
     * provided value at the end of the string. If the key does not exist it is
     * created and set as an empty string, so APPEND will be very similar to SET
     * in this special case.
     * <p>
     * Time complexity: O(1). The amortized time complexity is O(1) assuming the
     * appended value is small and the already present value is of any size,
     * since the dynamic string library used by Redis will double the free space
     * available on every reallocation.
     * 
     * @param key
     * @param value
     * @return Integer reply, specifically the total length of the string after
     *         the append operation.
     */
    public Long append(final String key, final String value) {
	checkIsInMulti();
	client.append(key, value);
	return client.getIntegerReply();
    }

    /**
     * Return a subset of the string from offset start to offset end (both
     * offsets are inclusive). Negative offsets can be used in order to provide
     * an offset starting from the end of the string. So -1 means the last char,
     * -2 the penultimate and so forth.
     * <p>
     * The function handles out of range requests without raising an error, but
     * just limiting the resulting range to the actual length of the string.
     * <p>
     * Time complexity: O(start+n) (with start being the start index and n the
     * total length of the requested range). Note that the lookup part of this
     * command is O(1) so for small strings this is actually an O(1) command.
     * 
     * @param key
     * @param start
     * @param end
     * @return Bulk reply
     */
    public String substr(final String key, final int start, final int end) {
	checkIsInMulti();
	client.substr(key, start, end);
	return client.getBulkReply();
    }

    /**
     * 
     * Set the specified hash field to the specified value.
     * <p>
     * If key does not exist, a new key holding a hash is created.
     * <p>
     * <b>Time complexity:</b> O(1)
     * 
     * @param key
     * @param field
     * @param value
     * @return If the field already exists, and the HSET just produced an update
     *         of the value, 0 is returned, otherwise if a new field is created
     *         1 is returned.
     */
    public Long hset(final String key, final String field, final String value) {
	checkIsInMulti();
	client.hset(key, field, value);
	return client.getIntegerReply();
    }

    /**
     * If key holds a hash, retrieve the value associated to the specified
     * field.
     * <p>
     * If the field is not found or the key does not exist, a special 'nil'
     * value is returned.
     * <p>
     * <b>Time complexity:</b> O(1)
     * 
     * @param key
     * @param field
     * @return Bulk reply
     */
    public String hget(final String key, final String field) {
	checkIsInMulti();
	client.hget(key, field);
	return client.getBulkReply();
    }

    /**
     * 
     * Set the specified hash field to the specified value if the field not
     * exists. <b>Time complexity:</b> O(1)
     * 
     * @param key
     * @param field
     * @param value
     * @return If the field already exists, 0 is returned, otherwise if a new
     *         field is created 1 is returned.
     */
    public Long hsetnx(final String key, final String field, final String value) {
	checkIsInMulti();
	client.hsetnx(key, field, value);
	return client.getIntegerReply();
    }

    /**
     * Set the respective fields to the respective values. HMSET replaces old
     * values with new values.
     * <p>
     * If key does not exist, a new key holding a hash is created.
     * <p>
     * <b>Time complexity:</b> O(N) (with N being the number of fields)
     * 
     * @param key
     * @param hash
     * @return Return OK or Exception if hash is empty
     */
    public String hmset(final String key, final Map<String, String> hash) {
	checkIsInMulti();
	client.hmset(key, hash);
	return client.getStatusCodeReply();
    }

    /**
     * Retrieve the values associated to the specified fields.
     * <p>
     * If some of the specified fields do not exist, nil values are returned.
     * Non existing keys are considered like empty hashes.
     * <p>
     * <b>Time complexity:</b> O(N) (with N being the number of fields)
     * 
     * @param key
     * @param fields
     * @return Multi Bulk Reply specifically a list of all the values associated
     *         with the specified fields, in the same order of the request.
     */
    public List<String> hmget(final String key, final String... fields) {
	checkIsInMulti();
	client.hmget(key, fields);
	return client.getMultiBulkReply();
    }

    /**
     * Increment the number stored at field in the hash at key by value. If key
     * does not exist, a new key holding a hash is created. If field does not
     * exist or holds a string, the value is set to 0 before applying the
     * operation. Since the value argument is signed you can use this command to
     * perform both increments and decrements.
     * <p>
     * The range of values supported by HINCRBY is limited to 64 bit signed
     * integers.
     * <p>
     * <b>Time complexity:</b> O(1)
     * 
     * @param key
     * @param field
     * @param value
     * @return Integer reply The new value at field after the increment
     *         operation.
     */
    public Long hincrBy(final String key, final String field, final long value) {
	checkIsInMulti();
	client.hincrBy(key, field, value);
	return client.getIntegerReply();
    }

    /**
     * Test for existence of a specified field in a hash.
     * 
     * <b>Time complexity:</b> O(1)
     * 
     * @param key
     * @param field
     * @return Return 1 if the hash stored at key contains the specified field.
     *         Return 0 if the key is not found or the field is not present.
     */
    public Boolean hexists(final String key, final String field) {
	checkIsInMulti();
	client.hexists(key, field);
	return client.getIntegerReply() == 1;
    }

    /**
     * Remove the specified field from an hash stored at key.
     * <p>
     * <b>Time complexity:</b> O(1)
     * 
     * @param key
     * @param fields
     * @return If the field was present in the hash it is deleted and 1 is
     *         returned, otherwise 0 is returned and no operation is performed.
     */
    public Long hdel(final String key, final String... fields) {
	checkIsInMulti();
	client.hdel(key, fields);
	return client.getIntegerReply();
    }

    /**
     * Return the number of items in a hash.
     * <p>
     * <b>Time complexity:</b> O(1)
     * 
     * @param key
     * @return The number of entries (fields) contained in the hash stored at
     *         key. If the specified key does not exist, 0 is returned assuming
     *         an empty hash.
     */
    public Long hlen(final String key) {
	checkIsInMulti();
	client.hlen(key);
	return client.getIntegerReply();
    }

    /**
     * Return all the fields in a hash.
     * <p>
     * <b>Time complexity:</b> O(N), where N is the total number of entries
     * 
     * @param key
     * @return All the fields names contained into a hash.
     */
    public Set<String> hkeys(final String key) {
	checkIsInMulti();
	client.hkeys(key);
	return BuilderFactory.STRING_SET
		.build(client.getBinaryMultiBulkReply());
    }

    /**
     * Return all the values in a hash.
     * <p>
     * <b>Time complexity:</b> O(N), where N is the total number of entries
     * 
     * @param key
     * @return All the fields values contained into a hash.
     */
    public List<String> hvals(final String key) {
	checkIsInMulti();
	client.hvals(key);
	final List<String> lresult = client.getMultiBulkReply();
	return lresult;
    }

    /**
     * Return all the fields and associated values in a hash.
     * <p>
     * <b>Time complexity:</b> O(N), where N is the total number of entries
     * 
     * @param key
     * @return All the fields and values contained into a hash.
     */
    public Map<String, String> hgetAll(final String key) {
	checkIsInMulti();
	client.hgetAll(key);
	return BuilderFactory.STRING_MAP
		.build(client.getBinaryMultiBulkReply());
    }

    /**
     * Add the string value to the head (LPUSH) or tail (RPUSH) of the list
     * stored at key. If the key does not exist an empty list is created just
     * before the append operation. If the key exists but is not a List an error
     * is returned.
     * <p>
     * Time complexity: O(1)
     * 
     * @see Jedis#lpush(String, String)
     * 
     * @param key
     * @param strings
     * @return Integer reply, specifically, the number of elements inside the
     *         list after the push operation.
     */
    public Long rpush(final String key, final String... strings) {
	checkIsInMulti();
	client.rpush(key, strings);
	return client.getIntegerReply();
    }

    /**
     * Add the string value to the head (LPUSH) or tail (RPUSH) of the list
     * stored at key. If the key does not exist an empty list is created just
     * before the append operation. If the key exists but is not a List an error
     * is returned.
     * <p>
     * Time complexity: O(1)
     * 
     * @see Jedis#rpush(String, String)
     * 
     * @param key
     * @param strings
     * @return Integer reply, specifically, the number of elements inside the
     *         list after the push operation.
     */
    public Long lpush(final String key, final String... strings) {
	checkIsInMulti();
	client.lpush(key, strings);
	return client.getIntegerReply();
    }

    /**
     * Return the length of the list stored at the specified key. If the key
     * does not exist zero is returned (the same behaviour as for empty lists).
     * If the value stored at key is not a list an error is returned.
     * <p>
     * Time complexity: O(1)
     * 
     * @param key
     * @return The length of the list.
     */
    public Long llen(final String key) {
	checkIsInMulti();
	client.llen(key);
	return client.getIntegerReply();
    }

    /**
     * Return the specified elements of the list stored at the specified key.
     * Start and end are zero-based indexes. 0 is the first element of the list
     * (the list head), 1 the next element and so on.
     * <p>
     * For example LRANGE foobar 0 2 will return the first three elements of the
     * list.
     * <p>
     * start and end can also be negative numbers indicating offsets from the
     * end of the list. For example -1 is the last element of the list, -2 the
     * penultimate element and so on.
     * <p>
     * <b>Consistency with range functions in various programming languages</b>
     * <p>
     * Note that if you have a list of numbers from 0 to 100, LRANGE 0 10 will
     * return 11 elements, that is, rightmost item is included. This may or may
     * not be consistent with behavior of range-related functions in your
     * programming language of choice (think Ruby's Range.new, Array#slice or
     * Python's range() function).
     * <p>
     * LRANGE behavior is consistent with one of Tcl.
     * <p>
     * <b>Out-of-range indexes</b>
     * <p>
     * Indexes out of range will not produce an error: if start is over the end
     * of the list, or start > end, an empty list is returned. If end is over
     * the end of the list Redis will threat it just like the last element of
     * the list.
     * <p>
     * Time complexity: O(start+n) (with n being the length of the range and
     * start being the start offset)
     * 
     * @param key
     * @param start
     * @param end
     * @return Multi bulk reply, specifically a list of elements in the
     *         specified range.
     */
    public List<String> lrange(final String key, final long start,
	    final long end) {
	checkIsInMulti();
	client.lrange(key, start, end);
	return client.getMultiBulkReply();
    }

    /**
     * Trim an existing list so that it will contain only the specified range of
     * elements specified. Start and end are zero-based indexes. 0 is the first
     * element of the list (the list head), 1 the next element and so on.
     * <p>
     * For example LTRIM foobar 0 2 will modify the list stored at foobar key so
     * that only the first three elements of the list will remain.
     * <p>
     * start and end can also be negative numbers indicating offsets from the
     * end of the list. For example -1 is the last element of the list, -2 the
     * penultimate element and so on.
     * <p>
     * Indexes out of range will not produce an error: if start is over the end
     * of the list, or start > end, an empty list is left as value. If end over
     * the end of the list Redis will threat it just like the last element of
     * the list.
     * <p>
     * Hint: the obvious use of LTRIM is together with LPUSH/RPUSH. For example:
     * <p>
     * {@code lpush("mylist", "someelement"); ltrim("mylist", 0, 99); * }
     * <p>
     * The above two commands will push elements in the list taking care that
     * the list will not grow without limits. This is very useful when using
     * Redis to store logs for example. It is important to note that when used
     * in this way LTRIM is an O(1) operation because in the average case just
     * one element is removed from the tail of the list.
     * <p>
     * Time complexity: O(n) (with n being len of list - len of range)
     * 
     * @param key
     * @param start
     * @param end
     * @return Status code reply
     */
    public String ltrim(final String key, final long start, final long end) {
	checkIsInMulti();
	client.ltrim(key, start, end);
	return client.getStatusCodeReply();
    }

    /**
     * Return the specified element of the list stored at the specified key. 0
     * is the first element, 1 the second and so on. Negative indexes are
     * supported, for example -1 is the last element, -2 the penultimate and so
     * on.
     * <p>
     * If the value stored at key is not of list type an error is returned. If
     * the index is out of range a 'nil' reply is returned.
     * <p>
     * Note that even if the average time complexity is O(n) asking for the
     * first or the last element of the list is O(1).
     * <p>
     * Time complexity: O(n) (with n being the length of the list)
     * 
     * @param key
     * @param index
     * @return Bulk reply, specifically the requested element
     */
    public String lindex(final String key, final long index) {
	checkIsInMulti();
	client.lindex(key, index);
	return client.getBulkReply();
    }

    /**
     * Set a new value as the element at index position of the List at key.
     * <p>
     * Out of range indexes will generate an error.
     * <p>
     * Similarly to other list commands accepting indexes, the index can be
     * negative to access elements starting from the end of the list. So -1 is
     * the last element, -2 is the penultimate, and so forth.
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(N) (with N being the length of the list), setting the first or last
     * elements of the list is O(1).
     * 
     * @see #lindex(String, long)
     * 
     * @param key
     * @param index
     * @param value
     * @return Status code reply
     */
    public String lset(final String key, final long index, final String value) {
	checkIsInMulti();
	client.lset(key, index, value);
	return client.getStatusCodeReply();
    }

    /**
     * Remove the first count occurrences of the value element from the list. If
     * count is zero all the elements are removed. If count is negative elements
     * are removed from tail to head, instead to go from head to tail that is
     * the normal behaviour. So for example LREM with count -2 and hello as
     * value to remove against the list (a,b,c,hello,x,hello,hello) will lave
     * the list (a,b,c,hello,x). The number of removed elements is returned as
     * an integer, see below for more information about the returned value. Note
     * that non existing keys are considered like empty lists by LREM, so LREM
     * against non existing keys will always return 0.
     * <p>
     * Time complexity: O(N) (with N being the length of the list)
     * 
     * @param key
     * @param count
     * @param value
     * @return Integer Reply, specifically: The number of removed elements if
     *         the operation succeeded
     */
    public Long lrem(final String key, final long count, final String value) {
	checkIsInMulti();
	client.lrem(key, count, value);
	return client.getIntegerReply();
    }

    /**
     * Atomically return and remove the first (LPOP) or last (RPOP) element of
     * the list. For example if the list contains the elements "a","b","c" LPOP
     * will return "a" and the list will become "b","c".
     * <p>
     * If the key does not exist or the list is already empty the special value
     * 'nil' is returned.
     * 
     * @see #rpop(String)
     * 
     * @param key
     * @return Bulk reply
     */
    public String lpop(final String key) {
	checkIsInMulti();
	client.lpop(key);
	return client.getBulkReply();
    }

    /**
     * Atomically return and remove the first (LPOP) or last (RPOP) element of
     * the list. For example if the list contains the elements "a","b","c" LPOP
     * will return "a" and the list will become "b","c".
     * <p>
     * If the key does not exist or the list is already empty the special value
     * 'nil' is returned.
     * 
     * @see #lpop(String)
     * 
     * @param key
     * @return Bulk reply
     */
    public String rpop(final String key) {
	checkIsInMulti();
	client.rpop(key);
	return client.getBulkReply();
    }

    /**
     * Atomically return and remove the last (tail) element of the srckey list,
     * and push the element as the first (head) element of the dstkey list. For
     * example if the source list contains the elements "a","b","c" and the
     * destination list contains the elements "foo","bar" after an RPOPLPUSH
     * command the content of the two lists will be "a","b" and "c","foo","bar".
     * <p>
     * If the key does not exist or the list is already empty the special value
     * 'nil' is returned. If the srckey and dstkey are the same the operation is
     * equivalent to removing the last element from the list and pusing it as
     * first element of the list, so it's a "list rotation" command.
     * <p>
     * Time complexity: O(1)
     * 
     * @param srckey
     * @param dstkey
     * @return Bulk reply
     */
    public String rpoplpush(final String srckey, final String dstkey) {
	checkIsInMulti();
	client.rpoplpush(srckey, dstkey);
	return client.getBulkReply();
    }

    /**
     * Add the specified member to the set value stored at key. If member is
     * already a member of the set no operation is performed. If key does not
     * exist a new set with the specified member as sole member is created. If
     * the key exists but does not hold a set value an error is returned.
     * <p>
     * Time complexity O(1)
     * 
     * @param key
     * @param members
     * @return Integer reply, specifically: 1 if the new element was added 0 if
     *         the element was already a member of the set
     */
    public Long sadd(final String key, final String... members) {
	checkIsInMulti();
	client.sadd(key, members);
	return client.getIntegerReply();
    }

    /**
     * Return all the members (elements) of the set value stored at key. This is
     * just syntax glue for {@link #sinter(String...) SINTER}.
     * <p>
     * Time complexity O(N)
     * 
     * @param key
     * @return Multi bulk reply
     */
    public Set<String> smembers(final String key) {
	checkIsInMulti();
	client.smembers(key);
	final List<String> members = client.getMultiBulkReply();
	return new HashSet<String>(members);
    }

    /**
     * Remove the specified member from the set value stored at key. If member
     * was not a member of the set no operation is performed. If key does not
     * hold a set value an error is returned.
     * <p>
     * Time complexity O(1)
     * 
     * @param key
     * @param members
     * @return Integer reply, specifically: 1 if the new element was removed 0
     *         if the new element was not a member of the set
     */
    public Long srem(final String key, final String... members) {
	checkIsInMulti();
	client.srem(key, members);
	return client.getIntegerReply();
    }

    /**
     * Remove a random element from a Set returning it as return value. If the
     * Set is empty or the key does not exist, a nil object is returned.
     * <p>
     * The {@link #srandmember(String)} command does a similar work but the
     * returned element is not removed from the Set.
     * <p>
     * Time complexity O(1)
     * 
     * @param key
     * @return Bulk reply
     */
    public String spop(final String key) {
	checkIsInMulti();
	client.spop(key);
	return client.getBulkReply();
    }

    /**
     * Move the specifided member from the set at srckey to the set at dstkey.
     * This operation is atomic, in every given moment the element will appear
     * to be in the source or destination set for accessing clients.
     * <p>
     * If the source set does not exist or does not contain the specified
     * element no operation is performed and zero is returned, otherwise the
     * element is removed from the source set and added to the destination set.
     * On success one is returned, even if the element was already present in
     * the destination set.
     * <p>
     * An error is raised if the source or destination keys contain a non Set
     * value.
     * <p>
     * Time complexity O(1)
     * 
     * @param srckey
     * @param dstkey
     * @param member
     * @return Integer reply, specifically: 1 if the element was moved 0 if the
     *         element was not found on the first set and no operation was
     *         performed
     */
    public Long smove(final String srckey, final String dstkey,
	    final String member) {
	checkIsInMulti();
	client.smove(srckey, dstkey, member);
	return client.getIntegerReply();
    }

    /**
     * Return the set cardinality (number of elements). If the key does not
     * exist 0 is returned, like for empty sets.
     * 
     * @param key
     * @return Integer reply, specifically: the cardinality (number of elements)
     *         of the set as an integer.
     */
    public Long scard(final String key) {
	checkIsInMulti();
	client.scard(key);
	return client.getIntegerReply();
    }

    /**
     * Return 1 if member is a member of the set stored at key, otherwise 0 is
     * returned.
     * <p>
     * Time complexity O(1)
     * 
     * @param key
     * @param member
     * @return Integer reply, specifically: 1 if the element is a member of the
     *         set 0 if the element is not a member of the set OR if the key
     *         does not exist
     */
    public Boolean sismember(final String key, final String member) {
	checkIsInMulti();
	client.sismember(key, member);
	return client.getIntegerReply() == 1;
    }

    /**
     * Return the members of a set resulting from the intersection of all the
     * sets hold at the specified keys. Like in
     * {@link #lrange(String, long, long) LRANGE} the result is sent to the
     * client as a multi-bulk reply (see the protocol specification for more
     * information). If just a single key is specified, then this command
     * produces the same result as {@link #smembers(String) SMEMBERS}. Actually
     * SMEMBERS is just syntax sugar for SINTER.
     * <p>
     * Non existing keys are considered like empty sets, so if one of the keys
     * is missing an empty set is returned (since the intersection with an empty
     * set always is an empty set).
     * <p>
     * Time complexity O(N*M) worst case where N is the cardinality of the
     * smallest set and M the number of sets
     * 
     * @param keys
     * @return Multi bulk reply, specifically the list of common elements.
     */
    public Set<String> sinter(final String... keys) {
	checkIsInMulti();
	client.sinter(keys);
	final List<String> members = client.getMultiBulkReply();
	return new HashSet<String>(members);
    }

    /**
     * This commnad works exactly like {@link #sinter(String...) SINTER} but
     * instead of being returned the resulting set is sotred as dstkey.
     * <p>
     * Time complexity O(N*M) worst case where N is the cardinality of the
     * smallest set and M the number of sets
     * 
     * @param dstkey
     * @param keys
     * @return Status code reply
     */
    public Long sinterstore(final String dstkey, final String... keys) {
	checkIsInMulti();
	client.sinterstore(dstkey, keys);
	return client.getIntegerReply();
    }

    /**
     * Return the members of a set resulting from the union of all the sets hold
     * at the specified keys. Like in {@link #lrange(String, long, long) LRANGE}
     * the result is sent to the client as a multi-bulk reply (see the protocol
     * specification for more information). If just a single key is specified,
     * then this command produces the same result as {@link #smembers(String)
     * SMEMBERS}.
     * <p>
     * Non existing keys are considered like empty sets.
     * <p>
     * Time complexity O(N) where N is the total number of elements in all the
     * provided sets
     * 
     * @param keys
     * @return Multi bulk reply, specifically the list of common elements.
     */
    public Set<String> sunion(final String... keys) {
	checkIsInMulti();
	client.sunion(keys);
	final List<String> members = client.getMultiBulkReply();
	return new HashSet<String>(members);
    }

    /**
     * This command works exactly like {@link #sunion(String...) SUNION} but
     * instead of being returned the resulting set is stored as dstkey. Any
     * existing value in dstkey will be over-written.
     * <p>
     * Time complexity O(N) where N is the total number of elements in all the
     * provided sets
     * 
     * @param dstkey
     * @param keys
     * @return Status code reply
     */
    public Long sunionstore(final String dstkey, final String... keys) {
	checkIsInMulti();
	client.sunionstore(dstkey, keys);
	return client.getIntegerReply();
    }

    /**
     * Return the difference between the Set stored at key1 and all the Sets
     * key2, ..., keyN
     * <p>
     * <b>Example:</b>
     * 
     * <pre>
     * key1 = [x, a, b, c]
     * key2 = [c]
     * key3 = [a, d]
     * SDIFF key1,key2,key3 => [x, b]
     * </pre>
     * 
     * Non existing keys are considered like empty sets.
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(N) with N being the total number of elements of all the sets
     * 
     * @param keys
     * @return Return the members of a set resulting from the difference between
     *         the first set provided and all the successive sets.
     */
    public Set<String> sdiff(final String... keys) {
	checkIsInMulti();
	client.sdiff(keys);
	return BuilderFactory.STRING_SET
		.build(client.getBinaryMultiBulkReply());
    }

    /**
     * This command works exactly like {@link #sdiff(String...) SDIFF} but
     * instead of being returned the resulting set is stored in dstkey.
     * 
     * @param dstkey
     * @param keys
     * @return Status code reply
     */
    public Long sdiffstore(final String dstkey, final String... keys) {
	checkIsInMulti();
	client.sdiffstore(dstkey, keys);
	return client.getIntegerReply();
    }

    /**
     * Return a random element from a Set, without removing the element. If the
     * Set is empty or the key does not exist, a nil object is returned.
     * <p>
     * The SPOP command does a similar work but the returned element is popped
     * (removed) from the Set.
     * <p>
     * Time complexity O(1)
     * 
     * @param key
     * @return Bulk reply
     */
    public String srandmember(final String key) {
	checkIsInMulti();
	client.srandmember(key);
	return client.getBulkReply();
    }

    /**
     * Add the specified member having the specifeid score to the sorted set
     * stored at key. If member is already a member of the sorted set the score
     * is updated, and the element reinserted in the right position to ensure
     * sorting. If key does not exist a new sorted set with the specified member
     * as sole member is crated. If the key exists but does not hold a sorted
     * set value an error is returned.
     * <p>
     * The score value can be the string representation of a double precision
     * floating point number.
     * <p>
     * Time complexity O(log(N)) with N being the number of elements in the
     * sorted set
     * 
     * @param key
     * @param score
     * @param member
     * @return Integer reply, specifically: 1 if the new element was added 0 if
     *         the element was already a member of the sorted set and the score
     *         was updated
     */
    public Long zadd(final String key, final double score, final String member) {
	checkIsInMulti();
	client.zadd(key, score, member);
	return client.getIntegerReply();
    }

    public Long zadd(final String key, final Map<Double, String> scoreMembers) {
	checkIsInMulti();
	client.zadd(key, scoreMembers);
	return client.getIntegerReply();
    }

    public Set<String> zrange(final String key, final long start, final long end) {
	checkIsInMulti();
	client.zrange(key, start, end);
	final List<String> members = client.getMultiBulkReply();
	return new LinkedHashSet<String>(members);
    }

    /**
     * Remove the specified member from the sorted set value stored at key. If
     * member was not a member of the set no operation is performed. If key does
     * not not hold a set value an error is returned.
     * <p>
     * Time complexity O(log(N)) with N being the number of elements in the
     * sorted set
     * 
     * 
     * 
     * @param key
     * @param members
     * @return Integer reply, specifically: 1 if the new element was removed 0
     *         if the new element was not a member of the set
     */
    public Long zrem(final String key, final String... members) {
	checkIsInMulti();
	client.zrem(key, members);
	return client.getIntegerReply();
    }

    /**
     * If member already exists in the sorted set adds the increment to its
     * score and updates the position of the element in the sorted set
     * accordingly. If member does not already exist in the sorted set it is
     * added with increment as score (that is, like if the previous score was
     * virtually zero). If key does not exist a new sorted set with the
     * specified member as sole member is crated. If the key exists but does not
     * hold a sorted set value an error is returned.
     * <p>
     * The score value can be the string representation of a double precision
     * floating point number. It's possible to provide a negative value to
     * perform a decrement.
     * <p>
     * For an introduction to sorted sets check the Introduction to Redis data
     * types page.
     * <p>
     * Time complexity O(log(N)) with N being the number of elements in the
     * sorted set
     * 
     * @param key
     * @param score
     * @param member
     * @return The new score
     */
    public Double zincrby(final String key, final double score,
	    final String member) {
	checkIsInMulti();
	client.zincrby(key, score, member);
	String newscore = client.getBulkReply();
	return Double.valueOf(newscore);
    }

    /**
     * Return the rank (or index) or member in the sorted set at key, with
     * scores being ordered from low to high.
     * <p>
     * When the given member does not exist in the sorted set, the special value
     * 'nil' is returned. The returned rank (or index) of the member is 0-based
     * for both commands.
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(log(N))
     * 
     * @see #zrevrank(String, String)
     * 
     * @param key
     * @param member
     * @return Integer reply or a nil bulk reply, specifically: the rank of the
     *         element as an integer reply if the element exists. A nil bulk
     *         reply if there is no such element.
     */
    public Long zrank(final String key, final String member) {
	checkIsInMulti();
	client.zrank(key, member);
	return client.getIntegerReply();
    }

    /**
     * Return the rank (or index) or member in the sorted set at key, with
     * scores being ordered from high to low.
     * <p>
     * When the given member does not exist in the sorted set, the special value
     * 'nil' is returned. The returned rank (or index) of the member is 0-based
     * for both commands.
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(log(N))
     * 
     * @see #zrank(String, String)
     * 
     * @param key
     * @param member
     * @return Integer reply or a nil bulk reply, specifically: the rank of the
     *         element as an integer reply if the element exists. A nil bulk
     *         reply if there is no such element.
     */
    public Long zrevrank(final String key, final String member) {
	checkIsInMulti();
	client.zrevrank(key, member);
	return client.getIntegerReply();
    }

    public Set<String> zrevrange(final String key, final long start,
	    final long end) {
	checkIsInMulti();
	client.zrevrange(key, start, end);
	final List<String> members = client.getMultiBulkReply();
	return new LinkedHashSet<String>(members);
    }

    public Set<Tuple> zrangeWithScores(final String key, final long start,
	    final long end) {
	checkIsInMulti();
	client.zrangeWithScores(key, start, end);
	Set<Tuple> set = getTupledSet();
	return set;
    }

    public Set<Tuple> zrevrangeWithScores(final String key, final long start,
	    final long end) {
	checkIsInMulti();
	client.zrevrangeWithScores(key, start, end);
	Set<Tuple> set = getTupledSet();
	return set;
    }

    /**
     * Return the sorted set cardinality (number of elements). If the key does
     * not exist 0 is returned, like for empty sorted sets.
     * <p>
     * Time complexity O(1)
     * 
     * @param key
     * @return the cardinality (number of elements) of the set as an integer.
     */
    public Long zcard(final String key) {
	checkIsInMulti();
	client.zcard(key);
	return client.getIntegerReply();
    }

    /**
     * Return the score of the specified element of the sorted set at key. If
     * the specified element does not exist in the sorted set, or the key does
     * not exist at all, a special 'nil' value is returned.
     * <p>
     * <b>Time complexity:</b> O(1)
     * 
     * @param key
     * @param member
     * @return the score
     */
    public Double zscore(final String key, final String member) {
	checkIsInMulti();
	client.zscore(key, member);
	final String score = client.getBulkReply();
	return (score != null ? new Double(score) : null);
    }

    public String watch(final String... keys) {
	client.watch(keys);
	return client.getStatusCodeReply();
    }

    /**
     * Sort a Set or a List.
     * <p>
     * Sort the elements contained in the List, Set, or Sorted Set value at key.
     * By default sorting is numeric with elements being compared as double
     * precision floating point numbers. This is the simplest form of SORT.
     * 
     * @see #sort(String, String)
     * @see #sort(String, SortingParams)
     * @see #sort(String, SortingParams, String)
     * 
     * 
     * @param key
     * @return Assuming the Set/List at key contains a list of numbers, the
     *         return value will be the list of numbers ordered from the
     *         smallest to the biggest number.
     */
    public List<String> sort(final String key) {
	checkIsInMulti();
	client.sort(key);
	return client.getMultiBulkReply();
    }

    /**
     * Sort a Set or a List accordingly to the specified parameters.
     * <p>
     * <b>examples:</b>
     * <p>
     * Given are the following sets and key/values:
     * 
     * <pre>
     * x = [1, 2, 3]
     * y = [a, b, c]
     * 
     * k1 = z
     * k2 = y
     * k3 = x
     * 
     * w1 = 9
     * w2 = 8
     * w3 = 7
     * </pre>
     * 
     * Sort Order:
     * 
     * <pre>
     * sort(x) or sort(x, sp.asc())
     * -> [1, 2, 3]
     * 
     * sort(x, sp.desc())
     * -> [3, 2, 1]
     * 
     * sort(y)
     * -> [c, a, b]
     * 
     * sort(y, sp.alpha())
     * -> [a, b, c]
     * 
     * sort(y, sp.alpha().desc())
     * -> [c, a, b]
     * </pre>
     * 
     * Limit (e.g. for Pagination):
     * 
     * <pre>
     * sort(x, sp.limit(0, 2))
     * -> [1, 2]
     * 
     * sort(y, sp.alpha().desc().limit(1, 2))
     * -> [b, a]
     * </pre>
     * 
     * Sorting by external keys:
     * 
     * <pre>
     * sort(x, sb.by(w*))
     * -> [3, 2, 1]
     * 
     * sort(x, sb.by(w*).desc())
     * -> [1, 2, 3]
     * </pre>
     * 
     * Getting external keys:
     * 
     * <pre>
     * sort(x, sp.by(w*).get(k*))
     * -> [x, y, z]
     * 
     * sort(x, sp.by(w*).get(#).get(k*))
     * -> [3, x, 2, y, 1, z]
     * </pre>
     * 
     * @see #sort(String)
     * @see #sort(String, SortingParams, String)
     * 
     * @param key
     * @param sortingParameters
     * @return a list of sorted elements.
     */
    public List<String> sort(final String key,
	    final SortingParams sortingParameters) {
	checkIsInMulti();
	client.sort(key, sortingParameters);
	return client.getMultiBulkReply();
    }

    /**
     * BLPOP (and BRPOP) is a blocking list pop primitive. You can see this
     * commands as blocking versions of LPOP and RPOP able to block if the
     * specified keys don't exist or contain empty lists.
     * <p>
     * The following is a description of the exact semantic. We describe BLPOP
     * but the two commands are identical, the only difference is that BLPOP
     * pops the element from the left (head) of the list, and BRPOP pops from
     * the right (tail).
     * <p>
     * <b>Non blocking behavior</b>
     * <p>
     * When BLPOP is called, if at least one of the specified keys contain a non
     * empty list, an element is popped from the head of the list and returned
     * to the caller together with the name of the key (BLPOP returns a two
     * elements array, the first element is the key, the second the popped
     * value).
     * <p>
     * Keys are scanned from left to right, so for instance if you issue BLPOP
     * list1 list2 list3 0 against a dataset where list1 does not exist but
     * list2 and list3 contain non empty lists, BLPOP guarantees to return an
     * element from the list stored at list2 (since it is the first non empty
     * list starting from the left).
     * <p>
     * <b>Blocking behavior</b>
     * <p>
     * If none of the specified keys exist or contain non empty lists, BLPOP
     * blocks until some other client performs a LPUSH or an RPUSH operation
     * against one of the lists.
     * <p>
     * Once new data is present on one of the lists, the client finally returns
     * with the name of the key unblocking it and the popped value.
     * <p>
     * When blocking, if a non-zero timeout is specified, the client will
     * unblock returning a nil special value if the specified amount of seconds
     * passed without a push operation against at least one of the specified
     * keys.
     * <p>
     * The timeout argument is interpreted as an integer value. A timeout of
     * zero means instead to block forever.
     * <p>
     * <b>Multiple clients blocking for the same keys</b>
     * <p>
     * Multiple clients can block for the same key. They are put into a queue,
     * so the first to be served will be the one that started to wait earlier,
     * in a first-blpopping first-served fashion.
     * <p>
     * <b>blocking POP inside a MULTI/EXEC transaction</b>
     * <p>
     * BLPOP and BRPOP can be used with pipelining (sending multiple commands
     * and reading the replies in batch), but it does not make sense to use
     * BLPOP or BRPOP inside a MULTI/EXEC block (a Redis transaction).
     * <p>
     * The behavior of BLPOP inside MULTI/EXEC when the list is empty is to
     * return a multi-bulk nil reply, exactly what happens when the timeout is
     * reached. If you like science fiction, think at it like if inside
     * MULTI/EXEC the time will flow at infinite speed :)
     * <p>
     * Time complexity: O(1)
     * 
     * @see #brpop(int, String...)
     * 
     * @param timeout
     * @param keys
     * @return BLPOP returns a two-elements array via a multi bulk reply in
     *         order to return both the unblocking key and the popped value.
     *         <p>
     *         When a non-zero timeout is specified, and the BLPOP operation
     *         timed out, the return value is a nil multi bulk reply. Most
     *         client values will return false or nil accordingly to the
     *         programming language used.
     */
    public List<String> blpop(final int timeout, final String... keys) {
	checkIsInMulti();
	List<String> args = new ArrayList<String>();
	for (String arg : keys) {
	    args.add(arg);
	}
	args.add(String.valueOf(timeout));

	client.blpop(args.toArray(new String[args.size()]));
	client.setTimeoutInfinite();
	final List<String> multiBulkReply = client.getMultiBulkReply();
	client.rollbackTimeout();
	return multiBulkReply;
    }

    /**
     * Sort a Set or a List accordingly to the specified parameters and store
     * the result at dstkey.
     * 
     * @see #sort(String, SortingParams)
     * @see #sort(String)
     * @see #sort(String, String)
     * 
     * @param key
     * @param sortingParameters
     * @param dstkey
     * @return The number of elements of the list at dstkey.
     */
    public Long sort(final String key, final SortingParams sortingParameters,
	    final String dstkey) {
	checkIsInMulti();
	client.sort(key, sortingParameters, dstkey);
	return client.getIntegerReply();
    }

    /**
     * Sort a Set or a List and Store the Result at dstkey.
     * <p>
     * Sort the elements contained in the List, Set, or Sorted Set value at key
     * and store the result at dstkey. By default sorting is numeric with
     * elements being compared as double precision floating point numbers. This
     * is the simplest form of SORT.
     * 
     * @see #sort(String)
     * @see #sort(String, SortingParams)
     * @see #sort(String, SortingParams, String)
     * 
     * @param key
     * @param dstkey
     * @return The number of elements of the list at dstkey.
     */
    public Long sort(final String key, final String dstkey) {
	checkIsInMulti();
	client.sort(key, dstkey);
	return client.getIntegerReply();
    }

    /**
     * BLPOP (and BRPOP) is a blocking list pop primitive. You can see this
     * commands as blocking versions of LPOP and RPOP able to block if the
     * specified keys don't exist or contain empty lists.
     * <p>
     * The following is a description of the exact semantic. We describe BLPOP
     * but the two commands are identical, the only difference is that BLPOP
     * pops the element from the left (head) of the list, and BRPOP pops from
     * the right (tail).
     * <p>
     * <b>Non blocking behavior</b>
     * <p>
     * When BLPOP is called, if at least one of the specified keys contain a non
     * empty list, an element is popped from the head of the list and returned
     * to the caller together with the name of the key (BLPOP returns a two
     * elements array, the first element is the key, the second the popped
     * value).
     * <p>
     * Keys are scanned from left to right, so for instance if you issue BLPOP
     * list1 list2 list3 0 against a dataset where list1 does not exist but
     * list2 and list3 contain non empty lists, BLPOP guarantees to return an
     * element from the list stored at list2 (since it is the first non empty
     * list starting from the left).
     * <p>
     * <b>Blocking behavior</b>
     * <p>
     * If none of the specified keys exist or contain non empty lists, BLPOP
     * blocks until some other client performs a LPUSH or an RPUSH operation
     * against one of the lists.
     * <p>
     * Once new data is present on one of the lists, the client finally returns
     * with the name of the key unblocking it and the popped value.
     * <p>
     * When blocking, if a non-zero timeout is specified, the client will
     * unblock returning a nil special value if the specified amount of seconds
     * passed without a push operation against at least one of the specified
     * keys.
     * <p>
     * The timeout argument is interpreted as an integer value. A timeout of
     * zero means instead to block forever.
     * <p>
     * <b>Multiple clients blocking for the same keys</b>
     * <p>
     * Multiple clients can block for the same key. They are put into a queue,
     * so the first to be served will be the one that started to wait earlier,
     * in a first-blpopping first-served fashion.
     * <p>
     * <b>blocking POP inside a MULTI/EXEC transaction</b>
     * <p>
     * BLPOP and BRPOP can be used with pipelining (sending multiple commands
     * and reading the replies in batch), but it does not make sense to use
     * BLPOP or BRPOP inside a MULTI/EXEC block (a Redis transaction).
     * <p>
     * The behavior of BLPOP inside MULTI/EXEC when the list is empty is to
     * return a multi-bulk nil reply, exactly what happens when the timeout is
     * reached. If you like science fiction, think at it like if inside
     * MULTI/EXEC the time will flow at infinite speed :)
     * <p>
     * Time complexity: O(1)
     * 
     * @see #blpop(int, String...)
     * 
     * @param timeout
     * @param keys
     * @return BLPOP returns a two-elements array via a multi bulk reply in
     *         order to return both the unblocking key and the popped value.
     *         <p>
     *         When a non-zero timeout is specified, and the BLPOP operation
     *         timed out, the return value is a nil multi bulk reply. Most
     *         client values will return false or nil accordingly to the
     *         programming language used.
     */
    public List<String> brpop(final int timeout, final String... keys) {
	checkIsInMulti();
	List<String> args = new ArrayList<String>();
	for (String arg : keys) {
	    args.add(arg);
	}
	args.add(String.valueOf(timeout));

	client.brpop(args.toArray(new String[args.size()]));
	client.setTimeoutInfinite();
	List<String> multiBulkReply = client.getMultiBulkReply();
	client.rollbackTimeout();

	return multiBulkReply;
    }

    /**
     * Request for authentication in a password protected Redis server. A Redis
     * server can be instructed to require a password before to allow clients to
     * issue commands. This is done using the requirepass directive in the Redis
     * configuration file. If the password given by the client is correct the
     * server replies with an OK status code reply and starts accepting commands
     * from the client. Otherwise an error is returned and the clients needs to
     * try a new password. Note that for the high performance nature of Redis it
     * is possible to try a lot of passwords in parallel in very short time, so
     * make sure to generate a strong and very long password so that this attack
     * is infeasible.
     * 
     * @param password
     * @return Status code reply
     */

    public String auth(final String password) {
	checkIsInMulti();
	client.auth(password);
	return client.getStatusCodeReply();
    }

    public void subscribe(JedisPubSub jedisPubSub, String... channels) {
	checkIsInMulti();
	connect();
	client.setTimeoutInfinite();
	jedisPubSub.proceed(client, channels);
	client.rollbackTimeout();
    }

    public Long publish(String channel, String message) {
	checkIsInMulti();
	client.publish(channel, message);
	return client.getIntegerReply();
    }

    public void psubscribe(JedisPubSub jedisPubSub, String... patterns) {
	checkIsInMulti();
	connect();
	client.setTimeoutInfinite();
	jedisPubSub.proceedWithPatterns(client, patterns);
	client.rollbackTimeout();
    }

    public Long zcount(final String key, final double min, final double max) {
	checkIsInMulti();
	client.zcount(key, min, max);
	return client.getIntegerReply();
    }

    public Long zcount(final String key, final String min, final String max) {
	checkIsInMulti();
	client.zcount(key, min, max);
	return client.getIntegerReply();
    }

    /**
     * Return the all the elements in the sorted set at key with a score between
     * min and max (including elements with score equal to min or max).
     * <p>
     * The elements having the same score are returned sorted lexicographically
     * as ASCII strings (this follows from a property of Redis sorted sets and
     * does not involve further computation).
     * <p>
     * Using the optional
     * {@link #zrangeByScore(String, double, double, int, int) LIMIT} it's
     * possible to get only a range of the matching elements in an SQL-alike
     * way. Note that if offset is large the commands needs to traverse the list
     * for offset elements and this adds up to the O(M) figure.
     * <p>
     * The {@link #zcount(String, double, double) ZCOUNT} command is similar to
     * {@link #zrangeByScore(String, double, double) ZRANGEBYSCORE} but instead
     * of returning the actual elements in the specified interval, it just
     * returns the number of matching elements.
     * <p>
     * <b>Exclusive intervals and infinity</b>
     * <p>
     * min and max can be -inf and +inf, so that you are not required to know
     * what's the greatest or smallest element in order to take, for instance,
     * elements "up to a given value".
     * <p>
     * Also while the interval is for default closed (inclusive) it's possible
     * to specify open intervals prefixing the score with a "(" character, so
     * for instance:
     * <p>
     * {@code ZRANGEBYSCORE zset (1.3 5}
     * <p>
     * Will return all the values with score > 1.3 and <= 5, while for instance:
     * <p>
     * {@code ZRANGEBYSCORE zset (5 (10}
     * <p>
     * Will return all the values with score > 5 and < 10 (5 and 10 excluded).
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(log(N))+O(M) with N being the number of elements in the sorted set and
     * M the number of elements returned by the command, so if M is constant
     * (for instance you always ask for the first ten elements with LIMIT) you
     * can consider it O(log(N))
     * 
     * @see #zrangeByScore(String, double, double)
     * @see #zrangeByScore(String, double, double, int, int)
     * @see #zrangeByScoreWithScores(String, double, double)
     * @see #zrangeByScoreWithScores(String, String, String)
     * @see #zrangeByScoreWithScores(String, double, double, int, int)
     * @see #zcount(String, double, double)
     * 
     * @param key
     * @param min
     * @param max
     * @return Multi bulk reply specifically a list of elements in the specified
     *         score range.
     */
    public Set<String> zrangeByScore(final String key, final double min,
	    final double max) {
	checkIsInMulti();
	client.zrangeByScore(key, min, max);
	return new LinkedHashSet<String>(client.getMultiBulkReply());
    }

    public Set<String> zrangeByScore(final String key, final String min,
	    final String max) {
	checkIsInMulti();
	client.zrangeByScore(key, min, max);
	return new LinkedHashSet<String>(client.getMultiBulkReply());
    }

    /**
     * Return the all the elements in the sorted set at key with a score between
     * min and max (including elements with score equal to min or max).
     * <p>
     * The elements having the same score are returned sorted lexicographically
     * as ASCII strings (this follows from a property of Redis sorted sets and
     * does not involve further computation).
     * <p>
     * Using the optional
     * {@link #zrangeByScore(String, double, double, int, int) LIMIT} it's
     * possible to get only a range of the matching elements in an SQL-alike
     * way. Note that if offset is large the commands needs to traverse the list
     * for offset elements and this adds up to the O(M) figure.
     * <p>
     * The {@link #zcount(String, double, double) ZCOUNT} command is similar to
     * {@link #zrangeByScore(String, double, double) ZRANGEBYSCORE} but instead
     * of returning the actual elements in the specified interval, it just
     * returns the number of matching elements.
     * <p>
     * <b>Exclusive intervals and infinity</b>
     * <p>
     * min and max can be -inf and +inf, so that you are not required to know
     * what's the greatest or smallest element in order to take, for instance,
     * elements "up to a given value".
     * <p>
     * Also while the interval is for default closed (inclusive) it's possible
     * to specify open intervals prefixing the score with a "(" character, so
     * for instance:
     * <p>
     * {@code ZRANGEBYSCORE zset (1.3 5}
     * <p>
     * Will return all the values with score > 1.3 and <= 5, while for instance:
     * <p>
     * {@code ZRANGEBYSCORE zset (5 (10}
     * <p>
     * Will return all the values with score > 5 and < 10 (5 and 10 excluded).
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(log(N))+O(M) with N being the number of elements in the sorted set and
     * M the number of elements returned by the command, so if M is constant
     * (for instance you always ask for the first ten elements with LIMIT) you
     * can consider it O(log(N))
     * 
     * @see #zrangeByScore(String, double, double)
     * @see #zrangeByScore(String, double, double, int, int)
     * @see #zrangeByScoreWithScores(String, double, double)
     * @see #zrangeByScoreWithScores(String, double, double, int, int)
     * @see #zcount(String, double, double)
     * 
     * @param key
     * @param min
     * @param max
     * @return Multi bulk reply specifically a list of elements in the specified
     *         score range.
     */
    public Set<String> zrangeByScore(final String key, final double min,
	    final double max, final int offset, final int count) {
	checkIsInMulti();
	client.zrangeByScore(key, min, max, offset, count);
	return new LinkedHashSet<String>(client.getMultiBulkReply());
    }

    public Set<String> zrangeByScore(final String key, final String min,
	    final String max, final int offset, final int count) {
	checkIsInMulti();
	client.zrangeByScore(key, min, max, offset, count);
	return new LinkedHashSet<String>(client.getMultiBulkReply());
    }

    /**
     * Return the all the elements in the sorted set at key with a score between
     * min and max (including elements with score equal to min or max).
     * <p>
     * The elements having the same score are returned sorted lexicographically
     * as ASCII strings (this follows from a property of Redis sorted sets and
     * does not involve further computation).
     * <p>
     * Using the optional
     * {@link #zrangeByScore(String, double, double, int, int) LIMIT} it's
     * possible to get only a range of the matching elements in an SQL-alike
     * way. Note that if offset is large the commands needs to traverse the list
     * for offset elements and this adds up to the O(M) figure.
     * <p>
     * The {@link #zcount(String, double, double) ZCOUNT} command is similar to
     * {@link #zrangeByScore(String, double, double) ZRANGEBYSCORE} but instead
     * of returning the actual elements in the specified interval, it just
     * returns the number of matching elements.
     * <p>
     * <b>Exclusive intervals and infinity</b>
     * <p>
     * min and max can be -inf and +inf, so that you are not required to know
     * what's the greatest or smallest element in order to take, for instance,
     * elements "up to a given value".
     * <p>
     * Also while the interval is for default closed (inclusive) it's possible
     * to specify open intervals prefixing the score with a "(" character, so
     * for instance:
     * <p>
     * {@code ZRANGEBYSCORE zset (1.3 5}
     * <p>
     * Will return all the values with score > 1.3 and <= 5, while for instance:
     * <p>
     * {@code ZRANGEBYSCORE zset (5 (10}
     * <p>
     * Will return all the values with score > 5 and < 10 (5 and 10 excluded).
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(log(N))+O(M) with N being the number of elements in the sorted set and
     * M the number of elements returned by the command, so if M is constant
     * (for instance you always ask for the first ten elements with LIMIT) you
     * can consider it O(log(N))
     * 
     * @see #zrangeByScore(String, double, double)
     * @see #zrangeByScore(String, double, double, int, int)
     * @see #zrangeByScoreWithScores(String, double, double)
     * @see #zrangeByScoreWithScores(String, double, double, int, int)
     * @see #zcount(String, double, double)
     * 
     * @param key
     * @param min
     * @param max
     * @return Multi bulk reply specifically a list of elements in the specified
     *         score range.
     */
    public Set<Tuple> zrangeByScoreWithScores(final String key,
	    final double min, final double max) {
	checkIsInMulti();
	client.zrangeByScoreWithScores(key, min, max);
	Set<Tuple> set = getTupledSet();
	return set;
    }

    public Set<Tuple> zrangeByScoreWithScores(final String key,
	    final String min, final String max) {
	checkIsInMulti();
	client.zrangeByScoreWithScores(key, min, max);
	Set<Tuple> set = getTupledSet();
	return set;
    }

    /**
     * Return the all the elements in the sorted set at key with a score between
     * min and max (including elements with score equal to min or max).
     * <p>
     * The elements having the same score are returned sorted lexicographically
     * as ASCII strings (this follows from a property of Redis sorted sets and
     * does not involve further computation).
     * <p>
     * Using the optional
     * {@link #zrangeByScore(String, double, double, int, int) LIMIT} it's
     * possible to get only a range of the matching elements in an SQL-alike
     * way. Note that if offset is large the commands needs to traverse the list
     * for offset elements and this adds up to the O(M) figure.
     * <p>
     * The {@link #zcount(String, double, double) ZCOUNT} command is similar to
     * {@link #zrangeByScore(String, double, double) ZRANGEBYSCORE} but instead
     * of returning the actual elements in the specified interval, it just
     * returns the number of matching elements.
     * <p>
     * <b>Exclusive intervals and infinity</b>
     * <p>
     * min and max can be -inf and +inf, so that you are not required to know
     * what's the greatest or smallest element in order to take, for instance,
     * elements "up to a given value".
     * <p>
     * Also while the interval is for default closed (inclusive) it's possible
     * to specify open intervals prefixing the score with a "(" character, so
     * for instance:
     * <p>
     * {@code ZRANGEBYSCORE zset (1.3 5}
     * <p>
     * Will return all the values with score > 1.3 and <= 5, while for instance:
     * <p>
     * {@code ZRANGEBYSCORE zset (5 (10}
     * <p>
     * Will return all the values with score > 5 and < 10 (5 and 10 excluded).
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(log(N))+O(M) with N being the number of elements in the sorted set and
     * M the number of elements returned by the command, so if M is constant
     * (for instance you always ask for the first ten elements with LIMIT) you
     * can consider it O(log(N))
     * 
     * @see #zrangeByScore(String, double, double)
     * @see #zrangeByScore(String, double, double, int, int)
     * @see #zrangeByScoreWithScores(String, double, double)
     * @see #zrangeByScoreWithScores(String, double, double, int, int)
     * @see #zcount(String, double, double)
     * 
     * @param key
     * @param min
     * @param max
     * @return Multi bulk reply specifically a list of elements in the specified
     *         score range.
     */
    public Set<Tuple> zrangeByScoreWithScores(final String key,
	    final double min, final double max, final int offset,
	    final int count) {
	checkIsInMulti();
	client.zrangeByScoreWithScores(key, min, max, offset, count);
	Set<Tuple> set = getTupledSet();
	return set;
    }

    public Set<Tuple> zrangeByScoreWithScores(final String key,
	    final String min, final String max, final int offset,
	    final int count) {
	checkIsInMulti();
	client.zrangeByScoreWithScores(key, min, max, offset, count);
	Set<Tuple> set = getTupledSet();
	return set;
    }

    private Set<Tuple> getTupledSet() {
	checkIsInMulti();
	List<String> membersWithScores = client.getMultiBulkReply();
	Set<Tuple> set = new LinkedHashSet<Tuple>();
	Iterator<String> iterator = membersWithScores.iterator();
	while (iterator.hasNext()) {
	    set.add(new Tuple(iterator.next(), Double.valueOf(iterator.next())));
	}
	return set;
    }

    public Set<String> zrevrangeByScore(final String key, final double max,
	    final double min) {
	checkIsInMulti();
	client.zrevrangeByScore(key, max, min);
	return new LinkedHashSet<String>(client.getMultiBulkReply());
    }

    public Set<String> zrevrangeByScore(final String key, final String max,
	    final String min) {
	checkIsInMulti();
	client.zrevrangeByScore(key, max, min);
	return new LinkedHashSet<String>(client.getMultiBulkReply());
    }

    public Set<String> zrevrangeByScore(final String key, final double max,
	    final double min, final int offset, final int count) {
	checkIsInMulti();
	client.zrevrangeByScore(key, max, min, offset, count);
	return new LinkedHashSet<String>(client.getMultiBulkReply());
    }

    public Set<Tuple> zrevrangeByScoreWithScores(final String key,
	    final double max, final double min) {
	checkIsInMulti();
	client.zrevrangeByScoreWithScores(key, max, min);
	Set<Tuple> set = getTupledSet();
	return set;
    }

    public Set<Tuple> zrevrangeByScoreWithScores(final String key,
	    final double max, final double min, final int offset,
	    final int count) {
	checkIsInMulti();
	client.zrevrangeByScoreWithScores(key, max, min, offset, count);
	Set<Tuple> set = getTupledSet();
	return set;
    }

    public Set<Tuple> zrevrangeByScoreWithScores(final String key,
	    final String max, final String min, final int offset,
	    final int count) {
	checkIsInMulti();
	client.zrevrangeByScoreWithScores(key, max, min, offset, count);
	Set<Tuple> set = getTupledSet();
	return set;
    }

    public Set<String> zrevrangeByScore(final String key, final String max,
	    final String min, final int offset, final int count) {
	checkIsInMulti();
	client.zrevrangeByScore(key, max, min, offset, count);
	return new LinkedHashSet<String>(client.getMultiBulkReply());
    }

    public Set<Tuple> zrevrangeByScoreWithScores(final String key,
	    final String max, final String min) {
	checkIsInMulti();
	client.zrevrangeByScoreWithScores(key, max, min);
	Set<Tuple> set = getTupledSet();
	return set;
    }

    /**
     * Remove all elements in the sorted set at key with rank between start and
     * end. Start and end are 0-based with rank 0 being the element with the
     * lowest score. Both start and end can be negative numbers, where they
     * indicate offsets starting at the element with the highest rank. For
     * example: -1 is the element with the highest score, -2 the element with
     * the second highest score and so forth.
     * <p>
     * <b>Time complexity:</b> O(log(N))+O(M) with N being the number of
     * elements in the sorted set and M the number of elements removed by the
     * operation
     * 
     */
    public Long zremrangeByRank(final String key, final long start,
	    final long end) {
	checkIsInMulti();
	client.zremrangeByRank(key, start, end);
	return client.getIntegerReply();
    }

    /**
     * Remove all the elements in the sorted set at key with a score between min
     * and max (including elements with score equal to min or max).
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(log(N))+O(M) with N being the number of elements in the sorted set and
     * M the number of elements removed by the operation
     * 
     * @param key
     * @param start
     * @param end
     * @return Integer reply, specifically the number of elements removed.
     */
    public Long zremrangeByScore(final String key, final double start,
	    final double end) {
	checkIsInMulti();
	client.zremrangeByScore(key, start, end);
	return client.getIntegerReply();
    }

    public Long zremrangeByScore(final String key, final String start,
	    final String end) {
	checkIsInMulti();
	client.zremrangeByScore(key, start, end);
	return client.getIntegerReply();
    }

    /**
     * Creates a union or intersection of N sorted sets given by keys k1 through
     * kN, and stores it at dstkey. It is mandatory to provide the number of
     * input keys N, before passing the input keys and the other (optional)
     * arguments.
     * <p>
     * As the terms imply, the {@link #zinterstore(String, String...)
     * ZINTERSTORE} command requires an element to be present in each of the
     * given inputs to be inserted in the result. The
     * {@link #zunionstore(String, String...) ZUNIONSTORE} command inserts all
     * elements across all inputs.
     * <p>
     * Using the WEIGHTS option, it is possible to add weight to each input
     * sorted set. This means that the score of each element in the sorted set
     * is first multiplied by this weight before being passed to the
     * aggregation. When this option is not given, all weights default to 1.
     * <p>
     * With the AGGREGATE option, it's possible to specify how the results of
     * the union or intersection are aggregated. This option defaults to SUM,
     * where the score of an element is summed across the inputs where it
     * exists. When this option is set to be either MIN or MAX, the resulting
     * set will contain the minimum or maximum score of an element across the
     * inputs where it exists.
     * <p>
     * <b>Time complexity:</b> O(N) + O(M log(M)) with N being the sum of the
     * sizes of the input sorted sets, and M being the number of elements in the
     * resulting sorted set
     * 
     * @see #zunionstore(String, String...)
     * @see #zunionstore(String, ZParams, String...)
     * @see #zinterstore(String, String...)
     * @see #zinterstore(String, ZParams, String...)
     * 
     * @param dstkey
     * @param sets
     * @return Integer reply, specifically the number of elements in the sorted
     *         set at dstkey
     */
    public Long zunionstore(final String dstkey, final String... sets) {
	checkIsInMulti();
	client.zunionstore(dstkey, sets);
	return client.getIntegerReply();
    }

    /**
     * Creates a union or intersection of N sorted sets given by keys k1 through
     * kN, and stores it at dstkey. It is mandatory to provide the number of
     * input keys N, before passing the input keys and the other (optional)
     * arguments.
     * <p>
     * As the terms imply, the {@link #zinterstore(String, String...)
     * ZINTERSTORE} command requires an element to be present in each of the
     * given inputs to be inserted in the result. The
     * {@link #zunionstore(String, String...) ZUNIONSTORE} command inserts all
     * elements across all inputs.
     * <p>
     * Using the WEIGHTS option, it is possible to add weight to each input
     * sorted set. This means that the score of each element in the sorted set
     * is first multiplied by this weight before being passed to the
     * aggregation. When this option is not given, all weights default to 1.
     * <p>
     * With the AGGREGATE option, it's possible to specify how the results of
     * the union or intersection are aggregated. This option defaults to SUM,
     * where the score of an element is summed across the inputs where it
     * exists. When this option is set to be either MIN or MAX, the resulting
     * set will contain the minimum or maximum score of an element across the
     * inputs where it exists.
     * <p>
     * <b>Time complexity:</b> O(N) + O(M log(M)) with N being the sum of the
     * sizes of the input sorted sets, and M being the number of elements in the
     * resulting sorted set
     * 
     * @see #zunionstore(String, String...)
     * @see #zunionstore(String, ZParams, String...)
     * @see #zinterstore(String, String...)
     * @see #zinterstore(String, ZParams, String...)
     * 
     * @param dstkey
     * @param sets
     * @param params
     * @return Integer reply, specifically the number of elements in the sorted
     *         set at dstkey
     */
    public Long zunionstore(final String dstkey, final ZParams params,
	    final String... sets) {
	checkIsInMulti();
	client.zunionstore(dstkey, params, sets);
	return client.getIntegerReply();
    }

    /**
     * Creates a union or intersection of N sorted sets given by keys k1 through
     * kN, and stores it at dstkey. It is mandatory to provide the number of
     * input keys N, before passing the input keys and the other (optional)
     * arguments.
     * <p>
     * As the terms imply, the {@link #zinterstore(String, String...)
     * ZINTERSTORE} command requires an element to be present in each of the
     * given inputs to be inserted in the result. The
     * {@link #zunionstore(String, String...) ZUNIONSTORE} command inserts all
     * elements across all inputs.
     * <p>
     * Using the WEIGHTS option, it is possible to add weight to each input
     * sorted set. This means that the score of each element in the sorted set
     * is first multiplied by this weight before being passed to the
     * aggregation. When this option is not given, all weights default to 1.
     * <p>
     * With the AGGREGATE option, it's possible to specify how the results of
     * the union or intersection are aggregated. This option defaults to SUM,
     * where the score of an element is summed across the inputs where it
     * exists. When this option is set to be either MIN or MAX, the resulting
     * set will contain the minimum or maximum score of an element across the
     * inputs where it exists.
     * <p>
     * <b>Time complexity:</b> O(N) + O(M log(M)) with N being the sum of the
     * sizes of the input sorted sets, and M being the number of elements in the
     * resulting sorted set
     * 
     * @see #zunionstore(String, String...)
     * @see #zunionstore(String, ZParams, String...)
     * @see #zinterstore(String, String...)
     * @see #zinterstore(String, ZParams, String...)
     * 
     * @param dstkey
     * @param sets
     * @return Integer reply, specifically the number of elements in the sorted
     *         set at dstkey
     */
    public Long zinterstore(final String dstkey, final String... sets) {
	checkIsInMulti();
	client.zinterstore(dstkey, sets);
	return client.getIntegerReply();
    }

    /**
     * Creates a union or intersection of N sorted sets given by keys k1 through
     * kN, and stores it at dstkey. It is mandatory to provide the number of
     * input keys N, before passing the input keys and the other (optional)
     * arguments.
     * <p>
     * As the terms imply, the {@link #zinterstore(String, String...)
     * ZINTERSTORE} command requires an element to be present in each of the
     * given inputs to be inserted in the result. The
     * {@link #zunionstore(String, String...) ZUNIONSTORE} command inserts all
     * elements across all inputs.
     * <p>
     * Using the WEIGHTS option, it is possible to add weight to each input
     * sorted set. This means that the score of each element in the sorted set
     * is first multiplied by this weight before being passed to the
     * aggregation. When this option is not given, all weights default to 1.
     * <p>
     * With the AGGREGATE option, it's possible to specify how the results of
     * the union or intersection are aggregated. This option defaults to SUM,
     * where the score of an element is summed across the inputs where it
     * exists. When this option is set to be either MIN or MAX, the resulting
     * set will contain the minimum or maximum score of an element across the
     * inputs where it exists.
     * <p>
     * <b>Time complexity:</b> O(N) + O(M log(M)) with N being the sum of the
     * sizes of the input sorted sets, and M being the number of elements in the
     * resulting sorted set
     * 
     * @see #zunionstore(String, String...)
     * @see #zunionstore(String, ZParams, String...)
     * @see #zinterstore(String, String...)
     * @see #zinterstore(String, ZParams, String...)
     * 
     * @param dstkey
     * @param sets
     * @param params
     * @return Integer reply, specifically the number of elements in the sorted
     *         set at dstkey
     */
    public Long zinterstore(final String dstkey, final ZParams params,
	    final String... sets) {
	checkIsInMulti();
	client.zinterstore(dstkey, params, sets);
	return client.getIntegerReply();
    }

    public Long strlen(final String key) {
	client.strlen(key);
	return client.getIntegerReply();
    }

    public Long lpushx(final String key, final String string) {
	client.lpushx(key, string);
	return client.getIntegerReply();
    }

    /**
     * Undo a {@link #expire(String, int) expire} at turning the expire key into
     * a normal key.
     * <p>
     * Time complexity: O(1)
     * 
     * @param key
     * @return Integer reply, specifically: 1: the key is now persist. 0: the
     *         key is not persist (only happens when key not set).
     */
    public Long persist(final String key) {
	client.persist(key);
	return client.getIntegerReply();
    }

    public Long rpushx(final String key, final String string) {
	client.rpushx(key, string);
	return client.getIntegerReply();
    }

    public String echo(final String string) {
	client.echo(string);
	return client.getBulkReply();
    }

    public Long linsert(final String key, final LIST_POSITION where,
	    final String pivot, final String value) {
	client.linsert(key, where, pivot, value);
	return client.getIntegerReply();
    }

    /**
     * Pop a value from a list, push it to another list and return it; or block
     * until one is available
     * 
     * @param source
     * @param destination
     * @param timeout
     * @return the element
     */
    public String brpoplpush(String source, String destination, int timeout) {
	client.brpoplpush(source, destination, timeout);
	client.setTimeoutInfinite();
	String reply = client.getBulkReply();
	client.rollbackTimeout();
	return reply;
    }

    /**
     * Sets or clears the bit at offset in the string value stored at key
     * 
     * @param key
     * @param offset
     * @param value
     * @return
     */
    public Boolean setbit(String key, long offset, boolean value) {
	client.setbit(key, offset, value);
	return client.getIntegerReply() == 1;
    }

    /**
     * Returns the bit value at offset in the string value stored at key
     * 
     * @param key
     * @param offset
     * @return
     */
    public Boolean getbit(String key, long offset) {
	client.getbit(key, offset);
	return client.getIntegerReply() == 1;
    }

    public Long setrange(String key, long offset, String value) {
	client.setrange(key, offset, value);
	return client.getIntegerReply();
    }

    public String getrange(String key, long startOffset, long endOffset) {
	client.getrange(key, startOffset, endOffset);
	return client.getBulkReply();
    }

    /**
     * Retrieve the configuration of a running Redis server. Not all the
     * configuration parameters are supported.
     * <p>
     * CONFIG GET returns the current configuration parameters. This sub command
     * only accepts a single argument, that is glob style pattern. All the
     * configuration parameters matching this parameter are reported as a list
     * of key-value pairs.
     * <p>
     * <b>Example:</b>
     * 
     * <pre>
     * $ redis-cli config get '*'
     * 1. "dbfilename"
     * 2. "dump.rdb"
     * 3. "requirepass"
     * 4. (nil)
     * 5. "masterauth"
     * 6. (nil)
     * 7. "maxmemory"
     * 8. "0\n"
     * 9. "appendfsync"
     * 10. "everysec"
     * 11. "save"
     * 12. "3600 1 300 100 60 10000"
     * 
     * $ redis-cli config get 'm*'
     * 1. "masterauth"
     * 2. (nil)
     * 3. "maxmemory"
     * 4. "0\n"
     * </pre>
     * 
     * @param pattern
     * @return Bulk reply.
     */
    public List<String> configGet(final String pattern) {
	client.configGet(pattern);
	return client.getMultiBulkReply();
    }

    /**
     * Alter the configuration of a running Redis server. Not all the
     * configuration parameters are supported.
     * <p>
     * The list of configuration parameters supported by CONFIG SET can be
     * obtained issuing a {@link #configGet(String) CONFIG GET *} command.
     * <p>
     * The configuration set using CONFIG SET is immediately loaded by the Redis
     * server that will start acting as specified starting from the next
     * command.
     * <p>
     * 
     * <b>Parameters value format</b>
     * <p>
     * The value of the configuration parameter is the same as the one of the
     * same parameter in the Redis configuration file, with the following
     * exceptions:
     * <p>
     * <ul>
     * <li>The save paramter is a list of space-separated integers. Every pair
     * of integers specify the time and number of changes limit to trigger a
     * save. For instance the command CONFIG SET save "3600 10 60 10000" will
     * configure the server to issue a background saving of the RDB file every
     * 3600 seconds if there are at least 10 changes in the dataset, and every
     * 60 seconds if there are at least 10000 changes. To completely disable
     * automatic snapshots just set the parameter as an empty string.
     * <li>All the integer parameters representing memory are returned and
     * accepted only using bytes as unit.
     * </ul>
     * 
     * @param parameter
     * @param value
     * @return Status code reply
     */
    public String configSet(final String parameter, final String value) {
	client.configSet(parameter, value);
	return client.getStatusCodeReply();
    }

    public Object eval(String script, int keyCount, String... params) {
	client.setTimeoutInfinite();
	client.eval(script, keyCount, params);

	return getEvalResult();
    }

    private String[] getParams(List<String> keys, List<String> args) {
	int keyCount = keys.size();
	int argCount = args.size();

	String[] params = new String[keyCount + args.size()];

	for (int i = 0; i < keyCount; i++)
	    params[i] = keys.get(i);

	for (int i = 0; i < argCount; i++)
	    params[keyCount + i] = args.get(i);

	return params;
    }

    public Object eval(String script, List<String> keys, List<String> args) {
	return eval(script, keys.size(), getParams(keys, args));
    }

    public Object eval(String script) {
	return eval(script, 0);
    }

    public Object evalsha(String script) {
	return evalsha(script, 0);
    }

    private Object getEvalResult() {
	Object result = client.getOne();

	if (result instanceof byte[])
	    return SafeEncoder.encode((byte[]) result);

	if (result instanceof List<?>) {
	    List<?> list = (List<?>) result;
	    List<String> listResult = new ArrayList<String>(list.size());
	    for (Object bin : list)
		listResult.add(SafeEncoder.encode((byte[]) bin));

	    return listResult;
	}

	return result;
    }

    public Object evalsha(String sha1, List<String> keys, List<String> args) {
	return evalsha(sha1, keys.size(), getParams(keys, args));
    }

    public Object evalsha(String sha1, int keyCount, String... params) {
	checkIsInMulti();
	client.evalsha(sha1, keyCount, params);

	return getEvalResult();
    }

    public Boolean scriptExists(String sha1) {
	String[] a = new String[1];
	a[0] = sha1;
	return scriptExists(a).get(0);
    }

    public List<Boolean> scriptExists(String... sha1) {
	client.scriptExists(sha1);
	List<Long> result = client.getIntegerMultiBulkReply();
	List<Boolean> exists = new ArrayList<Boolean>();

	for (Long value : result)
	    exists.add(value == 1);

	return exists;
    }

    public String scriptLoad(String script) {
	client.scriptLoad(script);
	return client.getBulkReply();
    }

    public List<Slowlog> slowlogGet() {
	client.slowlogGet();
	return Slowlog.from(client.getObjectMultiBulkReply());
    }

    public List<Slowlog> slowlogGet(long entries) {
	client.slowlogGet(entries);
	return Slowlog.from(client.getObjectMultiBulkReply());
    }

    public Long objectRefcount(String string) {
	client.objectRefcount(string);
	return client.getIntegerReply();
    }

    public String objectEncoding(String string) {
	client.objectEncoding(string);
	return client.getBulkReply();
    }

    public Long objectIdletime(String string) {
	client.objectIdletime(string);
	return client.getIntegerReply();
    }
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

