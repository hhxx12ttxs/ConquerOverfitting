<<<<<<< HEAD
/*
 * Copyright (c) 2011, 2013, Oracle and/or its affiliates. All rights reserved.
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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import com.sun.javafx.collections.TrackableObservableList;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.CssMetaData;
import com.sun.javafx.css.converters.BooleanConverter;
import com.sun.javafx.css.converters.EnumConverter;
import com.sun.javafx.css.converters.SizeConverter;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import static javafx.scene.layout.Priority.ALWAYS;
import static javafx.scene.layout.Priority.SOMETIMES;
import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;
import static javafx.scene.layout.Region.boundedSize;
import static javafx.scene.layout.Region.getMaxAreaBaselineOffset;
import javafx.util.Callback;



/**
 * GridPane lays out its children within a flexible grid of rows and columns.
 * If a border and/or padding is set, then its content will be layed out within
 * those insets.
 * <p>
 * A child may be placed anywhere within the grid and may span multiple
 * rows/columns.  Children may freely overlap within rows/columns and their
 * stacking order will be defined by the order of the gridpane's children list
 * (0th node in back, last node in front).
 * <p>
 * GridPane may be styled with backgrounds and borders using CSS.  See
 * {@link javafx.scene.layout.Region Region} superclass for details.</p>
 *
 * <h4>Grid Constraints</h4>
 * <p>
 * A child's placement within the grid is defined by it's layout constraints:
 * <p>
 * <table border="1">
 * <tr><th>Constraint</th><th>Type</th><th>Description</th></tr>
 * <tr><td>columnIndex</td><td>integer</td><td>column where child's layout area starts.</td></tr>
 * <tr><td>rowIndex</td><td>integer</td><td>row where child's layout area starts.</td></tr>
 * <tr><td>columnSpan</td><td>integer</td><td>the number of columns the child's layout area spans horizontally.</td></tr>
 * <tr><td>rowSpan</td><td>integer</td><td>the number of rows the child's layout area spans vertically.</td></tr>
 * </table>
 * <p>
 * If the row/column indices are not explicitly set, then the child will be placed
 * in the first row/column.  If row/column spans are not set, they will default to 1.
 * A child's placement constraints can be changed dynamically and the gridpane
 * will update accordingly.
 * <p>
 * The total number of rows/columns does not need to be specified up front as the
 * gridpane will automatically expand/contract the grid to accommodate the content.
 * <p>
 * To use the GridPane, an application needs to set the layout constraints on
 * the children and add those children to the gridpane instance.
 * Constraints are set on the children using static setter methods on the GridPane
 * class:
 * <pre><code>     GridPane gridpane = new GridPane();
 *
 *     // Set one constraint at a time...
 *     // Places the button at the first row and second column
 *     Button button = new Button();
 *     <b>GridPane.setRowIndex(button, 0);
 *     GridPane.setColumnIndex(button, 1);</b>
 *
 *     // or convenience methods set more than one constraint at once...
 *     Label label = new Label();
 *     <b>GridPane.setConstraints(label, 2, 0);</b> // column=2 row=0
 *
 *     // don't forget to add children to gridpane
 *     <b>gridpane.getChildren().addAll(button, label);</b>
 * </code></pre>
 *
 * Applications may also use convenience methods which combine the steps of
 * setting the constraints and adding the children:
 * <pre><code>
 *     GridPane gridpane = new GridPane();
 *     <b>gridpane.add(new Button(), 1, 0);</b> // column=1 row=0
 *     <b>gridpane.add(new Label(), 2, 0);</b>  // column=2 row=0
 * </code></pre>
 *
 *
 * <h4>Row/Column Sizing</h4>
 *
 * By default, rows and columns will be sized to fit their content;
 * a column will be wide enough to accommodate the widest child, a
 * row tall enough to fit the tallest child.However, if an application needs
 * to explicitly control the size of rows or columns, it may do so by adding
 * RowConstraints and ColumnConstraints objects to specify those metrics.
 * For example, to create a grid with two fixed-width columns:
 * <pre><code>
 *     GridPane gridpane = new GridPane();
 *     <b>gridpane.getColumnConstraints().add(new ColumnConstraints(100));</b> // column 0 is 100 wide
 *     <b>gridpane.getColumnConstraints().add(new ColumnConstraints(200));</b> // column 1 is 200 wide
 * </code></pre>
 * By default the gridpane will resize rows/columns to their preferred sizes (either
 * computed from content or fixed), even if the gridpane is resized larger than
 * its preferred size.   If an application needs a particular row or column to
 * grow if there is extra space, it may set its grow priority on the RowConstraints
 * or ColumnConstraints object.  For example:
 * <pre><code>
 *     GridPane gridpane = new GridPane();
 *     ColumnConstraints column1 = new ColumnConstraints(100,100,Double.MAX_VALUE);
 *     <b>column1.setHgrow(Priority.ALWAYS);</b>
 *     ColumnConstraints column2 = new ColumnConstraints(100);
 *     gridpane.getColumnConstraints().addAll(column1, column2); // first column gets any extra width
 * </code></pre>
 * <p>
 * Note: Nodes spanning multiple rows/columns will be also size to the preferred sizes.
 * The affected rows/columns are resized by the following priority: grow priorities, last row.
 * This is with respect to row/column constraints.
 *
 * <h4>Percentage Sizing</h4>
 *
 * Alternatively, RowConstraints and ColumnConstraints allow the size to be specified
 * as a percentage of gridpane's available space:
 * <pre><code>
 *     GridPane gridpane = new GridPane();
 *     ColumnConstraints column1 = new ColumnConstraints();
 *     <b>column1.setPercentWidth(50);</b>
 *     ColumnConstraints column2 = new ColumnConstraints();
 *     <b>column2.setPercentWidth(50);</b>
 *     gridpane.getColumnConstraints().addAll(column1, column2); // each get 50% of width
 * </code></pre>
 * If a percentage value is set on a row/column, then that value takes precedent and the
 * row/column's min, pref, max, and grow constraints will be ignored.
 * <p>
 * Note that if the sum of the widthPercent (or heightPercent) values total greater than 100, the values will
 * be treated as weights.  e.g.  if 3 columns are each given a widthPercent of 50,
 * then each will be allocated 1/3 of the gridpane's available width (50/(50+50+50)).
 *
 * <h4>Mixing Size Types</h4>
 *
 * An application may freely mix the size-types of rows/columns (computed from content, fixed,
 * or percentage).  The percentage rows/columns will always be allocated space first
 * based on their percentage of the gridpane's available space (size minus insets and gaps).
 * The remaining space will be allocated to rows/columns given their minimum, preferred,
 * and maximum sizes and grow priorities.
 *
 * <h4>Resizable Range</h4>
 * A gridpane's parent will resize the gridpane within the gridpane's resizable range
 * during layout.   By default the gridpane computes this range based on its content
 * and row/column constraints as outlined in the table below.
 * <p>
 * <table border="1">
 * <tr><td></td><th>width</th><th>height</th></tr>
 * <tr><th>minimum</th>
 * <td>left/right insets plus the sum of each column's min width.</td>
 * <td>top/bottom insets plus the sum of each row's min height.</td></tr>
 * <tr><th>preferred</th>
 * <td>left/right insets plus the sum of each column's pref width.</td>
 * <td>top/bottom insets plus the sum of each row's pref height.</td></tr>
 * <tr><th>maximum</th>
 * <td>Double.MAX_VALUE</td><td>Double.MAX_VALUE</td></tr>
 * </table>
 * <p>
 * A gridpane's unbounded maximum width and height are an indication to the parent that
 * it may be resized beyond its preferred size to fill whatever space is assigned
 * to it.
 * <p>
 * GridPane provides properties for setting the size range directly.  These
 * properties default to the sentinel value USE_COMPUTED_SIZE, however the
 * application may set them to other values as needed:
 * <pre><code>     <b>gridpane.setPrefSize(300, 300);</b>
 *     // never size the gridpane larger than its preferred size:
 *     <b>gridpane.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);</b>
 * </code></pre>
 * Applications may restore the computed values by setting these properties back
 * to USE_COMPUTED_SIZE.
 * <p>
 * GridPane does not clip its content by default, so it is possible that childrens'
 * bounds may extend outside its own bounds if a child's min size prevents it from
 * being fit within it space.</p>
 *
 * <h4>Optional Layout Constraints</h4>
 *
 * An application may set additional constraints on children to customize how the
 * child is sized and positioned within the layout area established by it's row/column
 * indices/spans:
 * <p>
 * <table border="1">
 * <tr><th>Constraint</th><th>Type</th><th>Description</th></tr>
 * <tr><td>halignment</td><td>javafx.geometry.HPos</td><td>The horizontal alignment of the child within its layout area.</td></tr>
 * <tr><td>valignment</td><td>javafx.geometry.VPos</td><td>The vertical alignment of the child within its layout area.</td></tr>
 * <tr><td>hgrow</td><td>javafx.scene.layout.Priority</td><td>The horizontal grow priority of the child.</td></tr>
 * <tr><td>vgrow</td><td>javafx.scene.layout.Priority</td><td>The vertical grow priority of the child.</td></tr>
 * <tr><td>margin</td><td>javafx.geometry.Insets</td><td>Margin space around the outside of the child.</td></tr>
 * </table>
 * <p>
 * By default the alignment of a child within its layout area is defined by the
 * alignment set for the row and column.  If an individual alignment constraint is
 * set on a child, that alignment will override the row/column alignment only
 * for that child.  Alignment of other children in the same row or column will
 * not be affected.
 * <p>
 * Grow priorities, on the other hand, can only be applied to entire rows or columns.
 * Therefore, if a grow priority constraint is set on a single child, it will be
 * used to compute the default grow priority of the encompassing row/column.  If
 * a grow priority is set directly on a RowConstraint or ColumnConstraint object,
 * it will override the value computed from content.
 *
 *
 * @since JavaFX 2.0
 */
public class GridPane extends Pane {

    /**
     * Sentinel value which may be set on a child's row/column span constraint to
     * indicate that it should span the remaining rows/columns.
     */
    public static final int REMAINING = Integer.MAX_VALUE;

    /********************************************************************
     *  BEGIN static methods
     ********************************************************************/
    private static final String MARGIN_CONSTRAINT = "gridpane-margin";
    private static final String HALIGNMENT_CONSTRAINT = "gridpane-halignment";
    private static final String VALIGNMENT_CONSTRAINT = "gridpane-valignment";
    private static final String HGROW_CONSTRAINT = "gridpane-hgrow";
    private static final String VGROW_CONSTRAINT = "gridpane-vgrow";
    private static final String ROW_INDEX_CONSTRAINT = "gridpane-row";
    private static final String COLUMN_INDEX_CONSTRAINT = "gridpane-column";
    private static final String ROW_SPAN_CONSTRAINT = "gridpane-row-span";
    private static final String COLUMN_SPAN_CONSTRAINT = "gridpane-column-span";
    private static final String FILL_WIDTH_CONSTRAINT = "gridpane-fill-width";
    private static final String FILL_HEIGHT_CONSTRAINT = "gridpane-fill-height";

    /**
     * Sets the row index for the child when contained by a gridpane
     * so that it will be positioned starting in that row of the gridpane.
     * If a gridpane child has no row index set, it will be positioned in the
     * first row.
     * Setting the value to null will remove the constraint.
     * @param child the child node of a gridpane
     * @param value the row index of the child
     */
    public static void setRowIndex(Node child, Integer value) {
        if (value != null && value < 0) {
            throw new IllegalArgumentException("rowIndex must be greater or equal to 0, but was "+value);
        }
        setConstraint(child, ROW_INDEX_CONSTRAINT, value);
    }

    /**
     * Returns the child's row index constraint if set.
     * @param child the child node of a gridpane
     * @return the row index for the child or null if no row index was set
     */
    public static Integer getRowIndex(Node child) {
        return (Integer)getConstraint(child, ROW_INDEX_CONSTRAINT);
    }

    /**
     * Sets the column index for the child when contained by a gridpane
     * so that it will be positioned starting in that column of the gridpane.
     * If a gridpane child has no column index set, it will be positioned in
     * the first column.
     * Setting the value to null will remove the constraint.
     * @param child the child node of a gridpane
     * @param value the column index of the child
     */
    public static void setColumnIndex(Node child, Integer value) {
        if (value != null && value < 0) {
            throw new IllegalArgumentException("columnIndex must be greater or equal to 0, but was "+value);
        }
        setConstraint(child, COLUMN_INDEX_CONSTRAINT, value);
    }

    /**
     * Returns the child's column index constraint if set.
     * @param child the child node of a gridpane
     * @return the column index for the child or null if no column index was set
     */
    public static Integer getColumnIndex(Node child) {
        return (Integer)getConstraint(child, COLUMN_INDEX_CONSTRAINT);
    }

    /**
     * Sets the row span for the child when contained by a gridpane
     * so that it will span that number of rows vertically.  This may be
     * set to REMAINING, which will cause the span to extend across all the remaining
     * rows.
     * <p>
     * If a gridpane child has no row span set, it will default to spanning one row.
     * Setting the value to null will remove the constraint.
     * @param child the child node of a gridpane
     * @param value the row span of the child
     */
    public static void setRowSpan(Node child, Integer value) {
        if (value != null && value < 1) {
            throw new IllegalArgumentException("rowSpan must be greater or equal to 1, but was "+value);
        }
        setConstraint(child, ROW_SPAN_CONSTRAINT, value);
    }

    /**
     * Returns the child's row-span constraint if set.
     * @param child the child node of a gridpane
     * @return the row span for the child or null if no row span was set
     */
    public static Integer getRowSpan(Node child) {
        return (Integer)getConstraint(child, ROW_SPAN_CONSTRAINT);
    }

    /**
     * Sets the column span for the child when contained by a gridpane
     * so that it will span that number of columns horizontally.   This may be
     * set to REMAINING, which will cause the span to extend across all the remaining
     * columns.
     * <p>
     * If a gridpane child has no column span set, it will default to spanning one column.
     * Setting the value to null will remove the constraint.
     * @param child the child node of a gridpane
     * @param value the column span of the child
     */
    public static void setColumnSpan(Node child, Integer value) {
        if (value != null && value < 1) {
            throw new IllegalArgumentException("columnSpan must be greater or equal to 1, but was "+value);
        }
        setConstraint(child, COLUMN_SPAN_CONSTRAINT, value);
    }

    /**
     * Returns the child's column-span constraint if set.
     * @param child the child node of a gridpane
     * @return the column span for the child or null if no column span was set
     */
    public static Integer getColumnSpan(Node child) {
        return (Integer)getConstraint(child, COLUMN_SPAN_CONSTRAINT);
    }

    /**
     * Sets the margin for the child when contained by a gridpane.
     * If set, the gridpane will lay it out with the margin space around it.
     * Setting the value to null will remove the constraint.
     * @param child the child node of a gridpane
     * @param value the margin of space around the child
     */
    public static void setMargin(Node child, Insets value) {
        setConstraint(child, MARGIN_CONSTRAINT, value);
    }

    /**
     * Returns the child's margin constraint if set.
     * @param child the child node of a gridpane
     * @return the margin for the child or null if no margin was set
     */
    public static Insets getMargin(Node child) {
        return (Insets)getConstraint(child, MARGIN_CONSTRAINT);
    }

    private Insets getEffectiveMargin(Node child) {
        final Insets margin = getMargin(child);
        if (isNodePositionedByBaseline(child)) {
            if (margin != null) {
                return new Insets(rowBaseline[getRowIndex(child)] - child.getBaselineOffset(),
                        margin.getRight(), margin.getBottom(), margin.getLeft());
            } else {
                return new Insets(rowBaseline[getRowIndex(child)] - child.getBaselineOffset(),
                        0, 0, 0);
            }
        } else {
            return margin;
        }
    }

    private static final Callback<Node, Insets> marginAccessor = new Callback<Node, Insets>() {
        public Insets call(Node n) {
            return getMargin(n);
        }
    };

    /**
     * Sets the horizontal alignment for the child when contained by a gridpane.
     * If set, will override the gridpane's default horizontal alignment.
     * Setting the value to null will remove the constraint.
     * @param child the child node of a gridpane
     * @param value the hozizontal alignment for the child
     */
    public static void setHalignment(Node child, HPos value) {
        setConstraint(child, HALIGNMENT_CONSTRAINT, value);
    }

    /**
     * Returns the child's halignment constraint if set.
     * @param child the child node of a gridpane
     * @return the horizontal alignment for the child or null if no alignment was set
     */
    public static HPos getHalignment(Node child) {
        return (HPos)getConstraint(child, HALIGNMENT_CONSTRAINT);
    }

    /**
     * Sets the vertical alignment for the child when contained by a gridpane.
     * If set, will override the gridpane's default vertical alignment.
     * Setting the value to null will remove the constraint.
     * @param child the child node of a gridpane
     * @param value the vertical alignment for the child
     */
    public static void setValignment(Node child, VPos value) {
        setConstraint(child, VALIGNMENT_CONSTRAINT, value);
    }

    /**
     * Returns the child's valignment constraint if set.
     * @param child the child node of a gridpane
     * @return the vertical alignment for the child or null if no alignment was set
     */
    public static VPos getValignment(Node child) {
        return (VPos)getConstraint(child, VALIGNMENT_CONSTRAINT);
    }

    /**
     * Sets the horizontal grow priority for the child when contained by a gridpane.
     * If set, the gridpane will use the priority to allocate the child additional
     * horizontal space if the gridpane is resized larger than it's preferred width.
     * Setting the value to null will remove the constraint.
     * @param child the child of a gridpane
     * @param value the horizontal grow priority for the child
     */
    public static void setHgrow(Node child, Priority value) {
        setConstraint(child, HGROW_CONSTRAINT, value);
    }

    /**
     * Returns the child's hgrow constraint if set.
     * @param child the child node of a gridpane
     * @return the horizontal grow priority for the child or null if no priority was set
     */
    public static Priority getHgrow(Node child) {
        return (Priority)getConstraint(child, HGROW_CONSTRAINT);
    }

    /**
     * Sets the vertical grow priority for the child when contained by a gridpane.
     * If set, the gridpane will use the priority to allocate the child additional
     * vertical space if the gridpane is resized larger than it's preferred height.
     * Setting the value to null will remove the constraint.
     * @param child the child of a gridpane
     * @param value the vertical grow priority for the child
     */
    public static void setVgrow(Node child, Priority value) {
        setConstraint(child, VGROW_CONSTRAINT, value);
    }

    /**
     * Returns the child's vgrow constraint if set.
     * @param child the child node of a gridpane
     * @return the vertical grow priority for the child or null if no priority was set
     */
    public static Priority getVgrow(Node child) {
        return (Priority)getConstraint(child, VGROW_CONSTRAINT);
    }

    /**
     * Sets the horizontal fill policy for the child when contained by a gridpane.
     * If set, the gridpane will use the policy to determine whether node
     * should be expanded to fill the column or kept to it's preferred width.
     * Setting the value to null will remove the constraint.
     * If not value is specified for the node nor for the column, the default value is true.
     * @param child the child node of a gridpane
     * @param value the horizontal fill policy or null for unset
     * @since JavaFX 8.0
     */
    public static void setFillWidth(Node child, Boolean value) {
        setConstraint(child, FILL_WIDTH_CONSTRAINT, value);
    }

    /**
     * Returns the child's horizontal fill policy if set
     * @param child the child node of a gridpane
     * @return the horizontal fill policy for the child or null if no policy was set
     * @since JavaFX 8.0
     */
    public static Boolean isFillWidth(Node child) {
        return (Boolean) getConstraint(child, FILL_WIDTH_CONSTRAINT);
    }

    /**
     * Sets the vertical fill policy for the child when contained by a gridpane.
     * If set, the gridpane will use the policy to determine whether node
     * should be expanded to fill the row or kept to it's preferred height.
     * Setting the value to null will remove the constraint.
     * If not value is specified for the node nor for the row, the default value is true.
     * @param child the child node of a gridpane
     * @param value the vertical fill policy or null for unset
     * @since JavaFX 8.0
     */
    public static void setFillHeight(Node child, Boolean value) {
        setConstraint(child, FILL_HEIGHT_CONSTRAINT, value);
    }

    /**
     * Returns the child's vertical fill policy if set
     * @param child the child node of a gridpane
     * @return the vertical fill policy for the child or null if no policy was set
     * @since JavaFX 8.0
     */
    public static Boolean isFillHeight(Node child) {
        return (Boolean) getConstraint(child, FILL_HEIGHT_CONSTRAINT);
    }

    /**
     * Sets the column,row indeces for the child when contained in a gridpane.
     * @param child the child node of a gridpane
     * @param columnIndex the column index position for the child
     * @param rowIndex the row index position for the child
     */
    public static void setConstraints(Node child, int columnIndex, int rowIndex) {
        setRowIndex(child, rowIndex);
        setColumnIndex(child, columnIndex);
    }

    /**
     * Sets the column, row, column-span, and row-span value for the child when
     * contained in a gridpane.
     * @param child the child node of a gridpane
     * @param columnIndex the column index position for the child
     * @param rowIndex the row index position for the child
     * @param columnspan the number of columns the child should span
     * @param rowspan the number of rows the child should span
     */
    public static void setConstraints(Node child, int columnIndex, int rowIndex, int columnspan, int rowspan) {
        setRowIndex(child, rowIndex);
        setColumnIndex(child, columnIndex);
        setRowSpan(child, rowspan);
        setColumnSpan(child, columnspan);
    }

    /**
     * Sets the grid position, spans, and alignment for the child when contained in a gridpane.
     * @param child the child node of a gridpane
     * @param columnIndex the column index position for the child
     * @param rowIndex the row index position for the child
     * @param columnspan the number of columns the child should span
     * @param rowspan the number of rows the child should span
     * @param halignment the horizontal alignment of the child
     * @param valignment the vertical alignment of the child
     */
    public static void setConstraints(Node child, int columnIndex, int rowIndex, int columnspan, int rowspan,
            HPos halignment, VPos valignment) {
        setRowIndex(child, rowIndex);
        setColumnIndex(child, columnIndex);
        setRowSpan(child, rowspan);
        setColumnSpan(child, columnspan);
        setHalignment(child, halignment);
        setValignment(child, valignment);
    }

    /**
     * Sets the grid position, spans, and alignment for the child when contained in a gridpane.
     * @param child the child node of a gridpane
     * @param columnIndex the column index position for the child
     * @param rowIndex the row index position for the child
     * @param columnspan the number of columns the child should span
     * @param rowspan the number of rows the child should span
     * @param halignment the horizontal alignment of the child
     * @param valignment the vertical alignment of the child
     * @param hgrow the horizontal grow priority of the child
     * @param vgrow the vertical grow priority of the child
     */
    public static void setConstraints(Node child, int columnIndex, int rowIndex, int columnspan, int rowspan,
            HPos halignment, VPos valignment, Priority hgrow, Priority vgrow) {
        setRowIndex(child, rowIndex);
        setColumnIndex(child, columnIndex);
        setRowSpan(child, rowspan);
        setColumnSpan(child, columnspan);
        setHalignment(child, halignment);
        setValignment(child, valignment);
        setHgrow(child, hgrow);
        setVgrow(child, vgrow);
    }

    /**
     * Sets the grid position, spans, alignment, grow priorities, and margin for
     * the child when contained in a gridpane.
     * @param child the child node of a gridpane
     * @param columnIndex the column index position for the child
     * @param rowIndex the row index position for the child
     * @param columnspan the number of columns the child should span
     * @param rowspan the number of rows the child should span
     * @param halignment the horizontal alignment of the child
     * @param valignment the vertical alignment of the child
     * @param hgrow the horizontal grow priority of the child
     * @param vgrow the vertical grow priority of the child
     * @param margin the margin of space around the child
     */
    public static void setConstraints(Node child, int columnIndex, int rowIndex, int columnspan, int rowspan,
            HPos halignment, VPos valignment, Priority hgrow, Priority vgrow, Insets margin) {
        setRowIndex(child, rowIndex);
        setColumnIndex(child, columnIndex);
        setRowSpan(child, rowspan);
        setColumnSpan(child, columnspan);
        setHalignment(child, halignment);
        setValignment(child, valignment);
        setHgrow(child, hgrow);
        setVgrow(child, vgrow);
        setMargin(child, margin);
    }

    /**
     * Removes all gridpane constraints from the child node.
     * @param child the child node
     */
    public static void clearConstraints(Node child) {
        setRowIndex(child, null);
        setColumnIndex(child, null);
        setRowSpan(child, null);
        setColumnSpan(child, null);
        setHalignment(child, null);
        setValignment(child, null);
        setHgrow(child, null);
        setVgrow(child, null);
        setMargin(child, null);
    }


    private static final Color GRID_LINE_COLOR = Color.rgb(30, 30, 30);
    private static final double GRID_LINE_DASH = 3;

    static void createRow(int rowIndex, int columnIndex, Node... nodes) {
        for (int i = 0; i < nodes.length; i++) {
            setConstraints(nodes[i], columnIndex + i, rowIndex);
        }
    }

    static void createColumn(int columnIndex, int rowIndex, Node... nodes) {
        for (int i = 0; i < nodes.length; i++) {
            setConstraints(nodes[i], columnIndex, rowIndex + i);
        }
    }

    static int getNodeRowIndex(Node node) {
        Integer rowIndex = getRowIndex(node);
        return rowIndex != null? rowIndex : 0;
    }

    private static int getNodeRowSpan(Node node) {
        Integer rowspan = getRowSpan(node);
        return rowspan != null? rowspan : 1;
    }

    static int getNodeRowEnd(Node node) {
        int rowSpan = getNodeRowSpan(node);
        return rowSpan != REMAINING? getNodeRowIndex(node) + rowSpan - 1 : REMAINING;
    }

    static int getNodeColumnIndex(Node node) {
        Integer columnIndex = getColumnIndex(node);
        return columnIndex != null? columnIndex : 0;
    }

    private static int getNodeColumnSpan(Node node) {
        Integer colspan = getColumnSpan(node);
        return colspan != null? colspan : 1;
    }

    static int getNodeColumnEnd(Node node) {
        int columnSpan = getNodeColumnSpan(node);
        return columnSpan != REMAINING? getNodeColumnIndex(node) + columnSpan - 1 : REMAINING;
    }

    private static Priority getNodeHgrow(Node node) {
        Priority hgrow = getHgrow(node);
        return hgrow != null? hgrow : Priority.NEVER;
    }

    private static Priority getNodeVgrow(Node node) {
        Priority vgrow = getVgrow(node);
        return vgrow != null? vgrow : Priority.NEVER;
    }

    private static Priority[] createPriorityArray(int length, Priority value) {
        Priority[] array = new Priority[length];
        Arrays.fill(array, value);
        return array;
    }

    /********************************************************************
     *  END static methods
     ********************************************************************/

    /**
     * Creates a GridPane layout with hgap/vgap = 0 and TOP_LEFT alignment.
     */
    public GridPane() {
        super();
        getChildren().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable o) {
                requestLayout();
            }
        });
    }

    /**
     * The width of the horizontal gaps between columns.
     */
    public final DoubleProperty hgapProperty() {
        if (hgap == null) {
            hgap = new StyleableDoubleProperty(0) {
                @Override
                public void invalidated() {
                    requestLayout();
                }

                @Override
                public CssMetaData<GridPane, Number> getCssMetaData() {
                    return StyleableProperties.HGAP;
                }

                @Override
                public Object getBean() {
                    return GridPane.this;
                }

                @Override
                public String getName() {
                    return "hgap";
                }
            };
        }
        return hgap;
    }

    private DoubleProperty hgap;
    public final void setHgap(double value) { hgapProperty().set(value); }
    public final double getHgap() { return hgap == null ? 0 : hgap.get(); }

    /**
     * The height of the vertical gaps between rows.
     */
    public final DoubleProperty vgapProperty() {
        if (vgap == null) {
            vgap = new StyleableDoubleProperty(0) {
                @Override
                public void invalidated() {
                    requestLayout();
                }

                @Override
                public CssMetaData<GridPane, Number> getCssMetaData() {
                    return StyleableProperties.VGAP;
                }

                @Override
                public Object getBean() {
                    return GridPane.this;
                }

                @Override
                public String getName() {
                    return "vgap";
                }
            };
        }
        return vgap;
    }

    private DoubleProperty vgap;
    public final void setVgap(double value) { vgapProperty().set(value); }
    public final double getVgap() { return vgap == null ? 0 : vgap.get(); }

    /**
     * The alignment of of the grid within the gridpane's width and height.
     */
    public final ObjectProperty<Pos> alignmentProperty() {
        if (alignment == null) {
            alignment = new StyleableObjectProperty<Pos>(Pos.TOP_LEFT) {
                @Override
                public void invalidated() {
                    requestLayout();
                }

                @Override
                public CssMetaData<GridPane, Pos> getCssMetaData() {
                    return StyleableProperties.ALIGNMENT;
                }

                @Override
                public Object getBean() {
                    return GridPane.this;
                }

                @Override
                public String getName() {
                    return "alignment";
                }
            };
        }
        return alignment;
    }

    private ObjectProperty<Pos> alignment;
    public final void setAlignment(Pos value) {
        alignmentProperty().set(value);
    }
    public final Pos getAlignment() {
        return alignment == null ? Pos.TOP_LEFT : alignment.get();
    }
    private Pos getAlignmentInternal() {
        Pos localPos = getAlignment();
        return localPos == null ? Pos.TOP_LEFT : localPos;
    }

    /**
     * For debug purposes only: controls whether lines are displayed to show the gridpane's rows and columns.
     * Default is <code>false</code>.
     */
    public final BooleanProperty gridLinesVisibleProperty() {
        if (gridLinesVisible == null) {
            gridLinesVisible = new StyleableBooleanProperty() {
                @Override
                protected void invalidated() {
                    if (get()) {
                        gridLines = new Group();
                        gridLines.setManaged(false);
                        getChildren().add(gridLines);
                    } else {
                        getChildren().remove(gridLines);
                        gridLines = null;
                    }
                    requestLayout();
                }

                @Override
                public CssMetaData<GridPane, Boolean> getCssMetaData() {
                    return StyleableProperties.GRID_LINES_VISIBLE;
                }

                @Override
                public Object getBean() {
                    return GridPane.this;
                }

                @Override
                public String getName() {
                    return "gridLinesVisible";
                }
            };
        }
        return gridLinesVisible;
    }

    private BooleanProperty gridLinesVisible;
    public final void setGridLinesVisible(boolean value) { gridLinesVisibleProperty().set(value); }
    public final boolean isGridLinesVisible() { return gridLinesVisible == null ? false : gridLinesVisible.get(); }

    /**
     * RowConstraints instances can be added to explicitly control individual row
     * sizing and layout behavior.
     * If not set, row sizing and layout behavior will be computed based on content.
     *
     */
    private final ObservableList<RowConstraints> rowConstraints = new TrackableObservableList<RowConstraints>() {
        @Override
        protected void onChanged(Change<RowConstraints> c) {
            while (c.next()) {
                for (RowConstraints constraints : c.getRemoved()) {
                    if (constraints != null && !rowConstraints.contains(constraints)) {
                        constraints.remove(GridPane.this);
                    }
                }
                for (RowConstraints constraints : c.getAddedSubList()) {
                    if (constraints != null) {
                        constraints.add(GridPane.this);
                    }
                }
            }
            requestLayout();
        }
    };

    /**
     * Returns list of row constraints. Row constraints can be added to
     * explicitly control individual row sizing and layout behavior.
     * If not set, row sizing and layout behavior is computed based on content.
     *
     * Index in the ObservableList denotes the row number, so the row constraint for the first row
     * is at the position of 0.
     */
    public final ObservableList<RowConstraints> getRowConstraints() { return rowConstraints; }
    /**
     * ColumnConstraints instances can be added to explicitly control individual column
     * sizing and layout behavior.
     * If not set, column sizing and layout behavior will be computed based on content.
     */
    private final ObservableList<ColumnConstraints> columnConstraints = new TrackableObservableList<ColumnConstraints>() {
        @Override
        protected void onChanged(Change<ColumnConstraints> c) {
            while(c.next()) {
                for (ColumnConstraints constraints : c.getRemoved()) {
                    if (constraints != null && !columnConstraints.contains(constraints)) {
                        constraints.remove(GridPane.this);
                    }
                }
                for (ColumnConstraints constraints : c.getAddedSubList()) {
                    if (constraints != null) {
                        constraints.add(GridPane.this);
                    }
                }
            }
            requestLayout();
        }
    };

    /**
     * Returns list of column constraints. Column constraints can be added to
     * explicitly control individual column sizing and layout behavior.
     * If not set, column sizing and layout behavior is computed based on content.
     *
     * Index in the ObservableList denotes the column number, so the column constraint for the first column
     * is at the position of 0.
     */
    public final ObservableList<ColumnConstraints> getColumnConstraints() { return columnConstraints; }

    /**
     * Adds a child to the gridpane at the specified column,row position.
     * This convenience method will set the gridpane column and row constraints
     * on the child.
     * @param child the node being added to the gridpane
     * @param columnIndex the column index position for the child within the gridpane, counting from 0
     * @param rowIndex the row index position for the child within the gridpane, counting from 0
     */
    public void add(Node child, int columnIndex, int rowIndex) {
        setConstraints(child, columnIndex, rowIndex);
        getChildren().add(child);
    }

    /**
     * Adds a child to the gridpane at the specified column,row position and spans.
     * This convenience method will set the gridpane column, row, and span constraints
     * on the child.
     * @param child the node being added to the gridpane
     * @param columnIndex the column index position for the child within the gridpane, counting from 0
     * @param rowIndex the row index position for the child within the gridpane, counting from 0
     * @param colspan the number of columns the child's layout area should span
     * @param rowspan the number of rows the child's layout area should span
     */
    public void add(Node child, int columnIndex, int rowIndex, int colspan, int rowspan) {
        setConstraints(child, columnIndex, rowIndex, colspan, rowspan);
        getChildren().add(child);
    }

    /**
     * Convenience method for placing the specified nodes sequentially in a given
     * row of the gridpane.    If the row already contains nodes the specified nodes
     * will be appended to the row.  For example, the first node will be positioned at [column,row],
     * the second at [column+1,row], etc.   This method will set the appropriate gridpane
     * row/column constraints on the nodes as well as add the nodes to the gridpane's
     * children sequence.
     *
     * @param rowIndex the row index position for the children within the gridpane
     * @param children the nodes to be added as a row in the gridpane
     */
    public void addRow(int rowIndex, Node... children) {
        int columnIndex = 0;
        final List<Node> managed = getManagedChildren();
        for (int i = 0, size = managed.size(); i < size; i++) {
            Node child = managed.get(i);
            if (rowIndex == getNodeRowIndex(child)) {
                int index = getNodeColumnIndex(child);
                int end = getNodeColumnEnd(child);
                columnIndex = Math.max(columnIndex, (end != REMAINING? end : index) + 1);
            }
        }
        createRow(rowIndex, columnIndex, children);
        getChildren().addAll(children);
    }

    /**
     * Convenience method for placing the specified nodes sequentially in a given
     * column of the gridpane.    If the column already contains nodes the specified nodes
     * will be appended to the column.  For example, the first node will be positioned at [column, row],
     * the second at [column, row+1], etc.   This method will set the appropriate gridpane
     * row/column constraints on the nodes as well as add the nodes to the gridpane's
     * children sequence.
     *
     * @param columnIndex the column index position for the children within the gridpane
     * @param children the nodes to be added as a column in the gridpane
     */
    public void addColumn(int columnIndex, Node... children)  {
        int rowIndex = 0;
        final List<Node> managed = getManagedChildren();
        for (int i = 0, size = managed.size(); i < size; i++) {
            Node child = managed.get(i);
            if (columnIndex == getNodeColumnIndex(child)) {
                int index = getNodeRowIndex(child);
                int end = getNodeRowEnd(child);
                rowIndex = Math.max(rowIndex, (end != REMAINING? end : index) + 1);
            }
        }
        createColumn(columnIndex, rowIndex, children);
        getChildren().addAll(children);
    }

    private Group gridLines;
    private Orientation bias;

    private double[] rowPercentHeight;
    private double rowPercentTotal = 0;

    private CompositeSize rowMinHeight;
    private CompositeSize rowPrefHeight;
    private CompositeSize  rowMaxHeight;
    private double[] rowBaseline;
    private Priority[] rowGrow;

    private double[] columnPercentWidth;
    private double columnPercentTotal = 0;

    private CompositeSize columnMinWidth;
    private CompositeSize columnPrefWidth;
    private CompositeSize columnMaxWidth;
    private Priority[] columnGrow;

    private boolean metricsDirty = true;

    // This is set to true while in layoutChildren and set false on the conclusion.
    // It is used to decide whether to update metricsDirty in requestLayout().
    private boolean performingLayout = false;

    private int numRows;
    private int numColumns;

    private int getNumberOfRows() {
        computeGridMetrics();
        return numRows;
    }

    private int getNumberOfColumns() {
        computeGridMetrics();
        return numColumns;
    }

    private boolean isNodePositionedByBaseline(Node n){
        return (getRowValignment(getNodeRowIndex(n)) == VPos.BASELINE && getValignment(n) == null)
                || getValignment(n) == VPos.BASELINE;
    }

    private void computeGridMetrics() {
        if (metricsDirty) {
            numRows = rowConstraints.size();
            numColumns = columnConstraints.size();
            final List<Node> managed = getManagedChildren();
            for (int i = 0, size = managed.size(); i < size; i++) {
                Node child = managed.get(i);
                int rowIndex = getNodeRowIndex(child);
                int columnIndex = getNodeColumnIndex(child);
                int rowEnd = getNodeRowEnd(child);
                int columnEnd = getNodeColumnEnd(child);
                numRows = Math.max(numRows, (rowEnd != REMAINING ? rowEnd : rowIndex) + 1);
                numColumns = Math.max(numColumns, (columnEnd != REMAINING ? columnEnd : columnIndex) + 1);
            }
            rowPercentHeight = createDoubleArray(numRows, -1);
            rowPercentTotal = 0;
            columnPercentWidth = createDoubleArray(numColumns, -1);
            columnPercentTotal = 0;
            columnGrow = createPriorityArray(numColumns, Priority.NEVER);
            rowGrow = createPriorityArray(numRows, Priority.NEVER);
            rowBaseline = createDoubleArray(numRows, -1);
            List<Node> baselineNodes = new ArrayList<>(numColumns);
            for (int i = 0, sz = Math.min(numRows, rowConstraints.size()); i < sz; ++i) {
                final RowConstraints rc = rowConstraints.get(i);
                double percentHeight = rc.getPercentHeight();
                Priority vGrow = rc.getVgrow();
                if (percentHeight >= 0)
                    rowPercentHeight[i] = percentHeight;
                if (vGrow != null)
                    rowGrow[i] = vGrow;

                for (int j = 0, size = managed.size(); j < size; j++) {
                    Node n = managed.get(j);
                    if (getNodeRowIndex(n) == i && isNodePositionedByBaseline(n)) {
                        baselineNodes.add(n);
                    }
                }
                rowBaseline[i] = getMaxAreaBaselineOffset(baselineNodes, marginAccessor);
                baselineNodes.clear();

            }
            for (int i = 0, sz = Math.min(numColumns, columnConstraints.size()); i < sz; ++i) {
                final ColumnConstraints cc = columnConstraints.get(i);
                double percentWidth = cc.getPercentWidth();
                Priority hGrow = cc.getHgrow();
                if (percentWidth >= 0)
                    columnPercentWidth[i] = percentWidth;
                if (hGrow != null)
                    columnGrow[i] = hGrow;
            }

            for (int i = 0, size = managed.size(); i < size; i++) {
                Node child = managed.get(i);
                if (getNodeColumnSpan(child) == 1) {
                    Priority hg = getNodeHgrow(child);
                    int idx = getNodeColumnIndex(child);
                    columnGrow[idx] = Priority.max(columnGrow[idx], hg);
                }
                if (getNodeRowSpan(child) == 1) {
                    Priority vg = getNodeVgrow(child);
                    int idx = getNodeRowIndex(child);
                    rowGrow[idx] = Priority.max(rowGrow[idx], vg);
                }
            }

            for (int i = 0; i < rowPercentHeight.length; i++) {
                if (rowPercentHeight[i] > 0) {
                    rowPercentTotal += rowPercentHeight[i];
                }
            }
            if (rowPercentTotal > 100) {
                double weight = 100 / rowPercentTotal;
                for (int i = 0; i < rowPercentHeight.length; i++) {
                    if (rowPercentHeight[i] > 0) {
                        rowPercentHeight[i] *= weight;
                    }
                }
                rowPercentTotal = 100;
            }
            for (int i = 0; i < columnPercentWidth.length; i++) {
                if (columnPercentWidth[i] > 0) {
                    columnPercentTotal += columnPercentWidth[i];
                }
            }
            if (columnPercentTotal > 100) {
                double weight = 100 / columnPercentTotal;
                for (int i = 0; i < columnPercentWidth.length; i++) {
                    if (columnPercentWidth[i] > 0) {
                        columnPercentWidth[i] *= weight;
                    }
                }
                columnPercentTotal = 100;
            }

            for (int i = 0; i < managed.size(); ++i) {
                final Orientation b = managed.get(i).getContentBias();
                if (b != null) {
                    bias = b;
                    break;
                }
            }

            metricsDirty = false;
        }
    }

    @Override protected double computeMinWidth(double height) {
        computeGridMetrics();
        final double[] heights = height == -1 ? null : computeHeightsToFit(height).asArray();

        return snapSpace(getInsets().getLeft()) +
               computeMinWidths(heights).computeTotalWithMultiSize() +
               snapSpace(getInsets().getRight());

    }

    @Override protected double computeMinHeight(double width) {
        computeGridMetrics();
        final double[] widths = width == -1 ? null : computeWidthsToFit(width).asArray();

        return snapSpace(getInsets().getTop()) +
               computeMinHeights(widths).computeTotalWithMultiSize() +
               snapSpace(getInsets().getBottom());
    }

    @Override protected double computePrefWidth(double height) {
        computeGridMetrics();
        final double[] heights = height == -1 ? null : computeHeightsToFit(height).asArray();

        return snapSpace(getInsets().getLeft()) +
               computePrefWidths(heights).computeTotalWithMultiSize() +
               snapSpace(getInsets().getRight());
    }

    @Override protected double computePrefHeight(double width) {
        computeGridMetrics();
        final double[] widths = width == -1 ? null : computeWidthsToFit(width).asArray();

        return snapSpace(getInsets().getTop()) +
               computePrefHeights(widths).computeTotalWithMultiSize() +
               snapSpace(getInsets().getBottom());
    }

    private VPos getRowValignment(int rowIndex) {
        if (rowIndex < getRowConstraints().size()) {
            RowConstraints constraints = getRowConstraints().get(rowIndex);
            if (constraints.getValignment() != null) {
                return constraints.getValignment();
            }
        }
        return VPos.CENTER;
    }

    private HPos getColumnHalignment(int columnIndex) {
        if (columnIndex < getColumnConstraints().size()) {
            ColumnConstraints constraints = getColumnConstraints().get(columnIndex);
            if (constraints.getHalignment() != null) {
                return constraints.getHalignment();
            }
        }
        return HPos.LEFT;
    }

    private double getColumnMinWidth(int columnIndex) {
        if (columnIndex < getColumnConstraints().size()) {
            ColumnConstraints constraints = getColumnConstraints().get(columnIndex);
            return constraints.getMinWidth();

        }
        return USE_COMPUTED_SIZE;
    }

    private double getRowMinHeight(int rowIndex) {
        if (rowIndex < getRowConstraints().size()) {
            RowConstraints constraints = getRowConstraints().get(rowIndex);
            return constraints.getMinHeight();
        }
        return USE_COMPUTED_SIZE;
    }

    private double getColumnMaxWidth(int columnIndex) {
        if (columnIndex < getColumnConstraints().size()) {
            ColumnConstraints constraints = getColumnConstraints().get(columnIndex);
            return constraints.getMaxWidth();

        }
        return USE_COMPUTED_SIZE;
    }

    private double getColumnPrefWidth(int columnIndex) {
        if (columnIndex < getColumnConstraints().size()) {
            ColumnConstraints constraints = getColumnConstraints().get(columnIndex);
            return constraints.getPrefWidth();

        }
        return USE_COMPUTED_SIZE;
    }

    private double getRowPrefHeight(int rowIndex) {
        if (rowIndex < getRowConstraints().size()) {
            RowConstraints constraints = getRowConstraints().get(rowIndex);
            return constraints.getPrefHeight();

        }
        return USE_COMPUTED_SIZE;
    }

    private double getRowMaxHeight(int rowIndex) {
        if (rowIndex < getRowConstraints().size()) {
            RowConstraints constraints = getRowConstraints().get(rowIndex);
            return constraints.getMaxHeight();
        }
        return USE_COMPUTED_SIZE;
    }

    private boolean shouldRowFillHeight(int rowIndex) {
        if (rowIndex < getRowConstraints().size()) {
            return getRowConstraints().get(rowIndex).isFillHeight();
        }
        return true;
    }

    private boolean shouldColumnFillWidth(int columnIndex) {
        if (columnIndex < getColumnConstraints().size()) {
            return getColumnConstraints().get(columnIndex).isFillWidth();
        }
        return true;
    }

    private double getTotalWidthOfNodeColumns(Node child, double[] widths) {
        if (getNodeColumnSpan(child) == 1) {
            return widths[getNodeColumnIndex(child)];
        } else {
            double total = 0;
            for (int i = getNodeColumnIndex(child), last = getNodeColumnEndConvertRemaining(child); i <= last; ++i) {
                total += widths[i];
            }
            return total;
        }
    }

    private CompositeSize computeMaxHeights() {
        if (rowMaxHeight == null) {
            rowMaxHeight = createCompositeRows();
            final ObservableList<RowConstraints> rowConstr = getRowConstraints();
            CompositeSize prefHeights = null;
            for (int i = 0; i < rowConstr.size(); ++i) {
                final RowConstraints curConstraint = rowConstr.get(i);
                double maxRowHeight = snapSize(curConstraint.getMaxHeight());
                if (maxRowHeight == USE_PREF_SIZE) {
                    if (prefHeights == null) {
                        prefHeights = computePrefHeights(null);
                    }
                    rowMaxHeight.setPresetSize(i, prefHeights.getSize(i));
                } else if (maxRowHeight != USE_COMPUTED_SIZE) {
                    final double min = snapSize(curConstraint.getMinHeight());
                    if (min >= 0 ) {
                        rowMaxHeight.setPresetSize(i, boundedSize(min, maxRowHeight, maxRowHeight));
                    } else {
                        rowMaxHeight.setPresetSize(i, maxRowHeight);
                    }
                }
            }
            List<Node> managed = getManagedChildren();
            for (int i = 0, size = managed.size(); i < size; i++) {
                Node child = managed.get(i);
                int start = getNodeRowIndex(child);
                int end = getNodeRowEndConvertRemaining(child);
                double childMaxAreaHeight = computeChildMaxAreaHeight(child, getEffectiveMargin(child), -1);
                if (start == end && !rowMaxHeight.isPreset(start)) {
                    rowMaxHeight.setMaxSize(start, childMaxAreaHeight);
                } else if (start != end){
                    rowMaxHeight.setMaxMultiSize(start, end + 1, childMaxAreaHeight);
                }
            }
        }
        return rowMaxHeight;
    }

    private CompositeSize computePrefHeights(double[] widths) {
        CompositeSize result;
        if (widths == null) {
            if (rowPrefHeight != null) {
                return rowPrefHeight;
            }
            rowPrefHeight = createCompositeRows();
            result = rowPrefHeight;
        } else {
            result = createCompositeRows();
        }

        final ObservableList<RowConstraints> rowConstr = getRowConstraints();
        for (int i = 0; i < rowConstr.size(); ++i) {
            final RowConstraints curConstraint = rowConstr.get(i);
            double prefRowHeight = snapSize(curConstraint.getPrefHeight());
            if (prefRowHeight != USE_COMPUTED_SIZE) {
                final double min = snapSize(curConstraint.getMinHeight());
                final double max = snapSize(curConstraint.getMaxHeight());
                if (min >= 0 || max >= 0) {
                    result.setPresetSize(i, boundedSize(min < 0 ? 0 : min,
                            prefRowHeight,
                            max < 0 ? Double.POSITIVE_INFINITY : max));
                } else {
                    result.setPresetSize(i, prefRowHeight);
                }
            }
        }
        List<Node> managed = getManagedChildren();
        for (int i = 0, size = managed.size(); i < size; i++) {
            Node child = managed.get(i);
            int start = getNodeRowIndex(child);
            int end = getNodeRowEndConvertRemaining(child);
            double childPrefAreaHeight = computeChildPrefAreaHeight(child, getEffectiveMargin(child),
                    widths == null ? -1 : getTotalWidthOfNodeColumns(child, widths));
            if (start == end && !result.isPreset(start)) {
                double min = getRowMinHeight(start);
                double max = getRowMaxHeight(start);
                result.setMaxSize(start, boundedSize(min < 0 ? 0 : min, childPrefAreaHeight, max < 0 ? Double.MAX_VALUE : max));
            } else if (start != end){
                result.setMaxMultiSize(start, end + 1, childPrefAreaHeight);
            }
        }
        return result;
    }

    private CompositeSize computeMinHeights(double[] widths) {
        CompositeSize result;
        if (widths == null) {
            if (rowMinHeight != null) {
                return rowMinHeight;
            }
            rowMinHeight = createCompositeRows();
            result = rowMinHeight;
        } else {
            result = createCompositeRows();
        }

        final ObservableList<RowConstraints> rowConstr = getRowConstraints();
        CompositeSize prefHeights = null;
        for (int i = 0; i < rowConstr.size(); ++i) {
            double minRowHeight = snapSize(rowConstr.get(i).getMinHeight());
            if (minRowHeight == USE_PREF_SIZE) {
                if (prefHeights == null) {
                    prefHeights = computePrefHeights(widths);
                }
                result.setPresetSize(i, prefHeights.getSize(i));
            } else if (minRowHeight != USE_COMPUTED_SIZE) {
                result.setPresetSize(i, minRowHeight);
            }
        }
        List<Node> managed = getManagedChildren();
        for (int i = 0, size = managed.size(); i < size; i++) {
            Node child = managed.get(i);
            int start = getNodeRowIndex(child);
            int end = getNodeRowEndConvertRemaining(child);
            double childMinAreaHeight = computeChildMinAreaHeight(child, getEffectiveMargin(child),
                             widths == null ? -1 : getTotalWidthOfNodeColumns(child, widths));
            if (start == end && !result.isPreset(start)) {
                result.setMaxSize(start, childMinAreaHeight);
            } else if (start != end){
                result.setMaxMultiSize(start, end + 1, childMinAreaHeight);
            }
        }
        return result;
    }

    private double getTotalHeightOfNodeRows(Node child, double[] heights) {
        if (getNodeRowSpan(child) == 1) {
            return heights[getNodeRowIndex(child)];
        } else {
            double total = 0;
            for (int i = getNodeRowIndex(child), last = getNodeRowEndConvertRemaining(child); i <= last; ++i) {
                total += heights[i];
            }
            return total;
        }
    }

    private CompositeSize computeMaxWidths() {
        if (columnMaxWidth == null) {
            columnMaxWidth = createCompositeColumns();
            final ObservableList<ColumnConstraints> columnConstr = getColumnConstraints();
            CompositeSize prefWidths = null;
            for (int i = 0; i < columnConstr.size(); ++i) {
                final ColumnConstraints curConstraint = columnConstr.get(i);
                double maxColumnWidth = snapSize(curConstraint.getMaxWidth());
                if (maxColumnWidth == USE_PREF_SIZE) {
                    if (prefWidths == null) {
                        prefWidths = computePrefWidths(null);
                    }
                    columnMaxWidth.setPresetSize(i, prefWidths.getSize(i));
                } else if (maxColumnWidth != USE_COMPUTED_SIZE) {
                    final double min = snapSize(curConstraint.getMinWidth());
                    if (min >= 0) {
                        columnMaxWidth.setPresetSize(i, boundedSize(min, maxColumnWidth, maxColumnWidth));
                    } else {
                        columnMaxWidth.setPresetSize(i, maxColumnWidth);
                    }
                }
            }
            List<Node> managed = getManagedChildren();
            for (int i = 0, size = managed.size(); i < size; i++) {
                Node child = managed.get(i);
                int start = getNodeColumnIndex(child);
                int end = getNodeColumnEndConvertRemaining(child);
                if (start == end && !columnMaxWidth.isPreset(start)) {
                    columnMaxWidth.setMaxSize(start, computeChildMaxAreaWidth(child, getEffectiveMargin(child), -1));
                } else if (start != end){
                    columnMaxWidth.setMaxMultiSize(start, end + 1, computeChildMaxAreaWidth(child, getEffectiveMargin(child), -1));
                }
            }
        }
        return columnMaxWidth;
    }

    private CompositeSize computePrefWidths(double[] heights) {
        CompositeSize result;
        if (heights == null) {
            if (columnPrefWidth != null) {
                return columnPrefWidth;
            }
            columnPrefWidth = createCompositeColumns();
            result = columnPrefWidth;
        } else {
            result = createCompositeColumns();
        }

        final ObservableList<ColumnConstraints> columnConstr = getColumnConstraints();
        for (int i = 0; i < columnConstr.size(); ++i) {
            final ColumnConstraints curConstraint = columnConstr.get(i);
            double prefColumnWidth = snapSize(curConstraint.getPrefWidth());
            if (prefColumnWidth != USE_COMPUTED_SIZE) {
                final double min = snapSize(curConstraint.getMinWidth());
                final double max = snapSize(curConstraint.getMaxWidth());
                if (min >= 0 || max >= 0) {
                    result.setPresetSize(i, boundedSize(min < 0 ? 0 : min,
                            prefColumnWidth,
                            max < 0 ? Double.POSITIVE_INFINITY : max));
                } else {
                    result.setPresetSize(i, prefColumnWidth);
                }
            }
        }
        List<Node> managed = getManagedChildren();
        for (int i = 0, size = managed.size(); i < size; i++) {
            Node child = managed.get(i);
            int start = getNodeColumnIndex(child);
            int end = getNodeColumnEndConvertRemaining(child);
            if (start == end && !result.isPreset(start)) {
                double min = getColumnMinWidth(start);
                double max = getColumnMaxWidth(start);
                result.setMaxSize(start, boundedSize(min < 0 ? 0 : min, computeChildPrefAreaWidth(child, getEffectiveMargin(child),
                        heights == null ? -1 : getTotalHeightOfNodeRows(child, heights)), max < 0 ? Double.MAX_VALUE : max));
            } else if (start != end) {
                result.setMaxMultiSize(start, end + 1, computeChildPrefAreaWidth(child, getEffectiveMargin(child),
                        heights == null ? -1 : getTotalHeightOfNodeRows(child, heights)));
            }
        }
        return result;
    }

    private CompositeSize computeMinWidths(double[] heights) {
        CompositeSize result;
        if (heights == null) {
            if (columnMinWidth != null) {
                return columnMinWidth;
            }
            columnMinWidth = createCompositeColumns();
            result = columnMinWidth;
        } else {
            result = createCompositeColumns();
        }

        final ObservableList<ColumnConstraints> columnConstr = getColumnConstraints();
        CompositeSize prefWidths = null;
        for (int i = 0; i < columnConstr.size(); ++i) {
            double minColumnWidth = snapSize(columnConstr.get(i).getMinWidth());
            if (minColumnWidth == USE_PREF_SIZE) {
                if (prefWidths == null) {
                    prefWidths = computePrefWidths(heights);
                }
                result.setPresetSize(i, prefWidths.getSize(i));
            } else if (minColumnWidth != USE_COMPUTED_SIZE) {
                result.setPresetSize(i, minColumnWidth);
            }
        }
        List<Node> managed = getManagedChildren();
        for (int i = 0, size = managed.size(); i < size; i++) {
            Node child = managed.get(i);
            int start = getNodeColumnIndex(child);
            int end = getNodeColumnEndConvertRemaining(child);
            if (start == end && !result.isPreset(start)) {
                result.setMaxSize(start, computeChildMinAreaWidth(child, getEffectiveMargin(child),
                        heights == null ? -1 : getTotalHeightOfNodeRows(child, heights)));
            } else if (start != end){
                result.setMaxMultiSize(start, end + 1, computeChildMinAreaWidth(child, getEffectiveMargin(child),
                        heights == null ? -1 : getTotalHeightOfNodeRows(child, heights)));
            }
        }
        return result;
    }

    private CompositeSize computeHeightsToFit(double height) {
        assert(height != -1);
        final CompositeSize heights;
        if (rowPercentTotal == 100) {
            // all rows defined by percentage, no need to compute pref heights
            heights = createCompositeRows();
        } else {
            heights = (CompositeSize) computePrefHeights(null).clone();
        }
        adjustRowHeights(heights, height);
        return heights;
    }

    private CompositeSize computeWidthsToFit(double width) {
        assert(width != -1);
        final CompositeSize widths;
        if (columnPercentTotal == 100) {
            // all columns defined by percentage, no need to compute pref widths
            widths = createCompositeColumns();
        } else {
            widths = (CompositeSize) computePrefWidths(null).clone();
        }
        adjustColumnWidths(widths, width);
        return widths;
    }

    /**
     *
     * @return null unless one of its children has a content bias.
     */
    @Override public Orientation getContentBias() {
        computeGridMetrics();
        return bias;
    }

    @Override public void requestLayout() {
        // RT-18878: Do not update metrics dirty if we are performing layout.
        // If metricsDirty is set true during a layout pass the next call to computeGridMetrics()
        // will clear all the cell bounds resulting in out of date info until the
        // next layout pass.
        if (performingLayout) {
            return;
        }
        metricsDirty = true;
        bias = null;
        rowGrow = null;
        rowMinHeight = rowPrefHeight = rowMaxHeight = null;
        columnGrow = null;
        columnMinWidth = columnPrefWidth = columnMaxWidth = null;
        super.requestLayout();
    }

    @Override protected void layoutChildren() {
        performingLayout = true;
        final double snaphgap = snapSpace(getHgap());
        final double snapvgap = snapSpace(getVgap());
        final double top = snapSpace(getInsets().getTop());
        final double bottom = snapSpace(getInsets().getBottom());
        final double left = snapSpace(getInsets().getLeft());
        final double right = snapSpace(getInsets().getRight());

        final double width = getWidth();
        final double height = getHeight();
        final double contentHeight = height - top - bottom;
        final double contentWidth = width - left - right;
        double columnTotal;
        double rowTotal;
        computeGridMetrics();

        Orientation contentBias = getContentBias();
        CompositeSize heights;
        CompositeSize widths;
        if (contentBias == null) {
            heights = (CompositeSize) computePrefHeights(null).clone();
            widths = (CompositeSize) computePrefWidths(null).clone();
            rowTotal = adjustRowHeights(heights, height);
            columnTotal = adjustColumnWidths(widths, width);
        } else if (contentBias == Orientation.HORIZONTAL) {
            widths = (CompositeSize) computePrefWidths(null).clone();
            columnTotal = adjustColumnWidths(widths, width);
            heights = computePrefHeights(widths.asArray());
            rowTotal = adjustRowHeights(heights, height);
        } else {
            heights = (CompositeSize) computePrefHeights(null).clone();
            rowTotal = adjustRowHeights(heights, height);
            widths = computePrefWidths(heights.asArray());
            columnTotal = adjustColumnWidths(widths, width);
        }

        final double x = left + computeXOffset(contentWidth, columnTotal, getAlignmentInternal().getHpos());
        final double y = top + computeYOffset(contentHeight, rowTotal, getAlignmentInternal().getVpos());
        final List<Node> managed = getManagedChildren();
        for (int i = 0, size = managed.size(); i < size; i++) {
            Node child = managed.get(i);
            int rowIndex = getNodeRowIndex(child);
            int columnIndex = getNodeColumnIndex(child);
            int colspan = getNodeColumnSpan(child);
            if (colspan == REMAINING) {
                colspan = widths.getLength() - columnIndex;
            }
            int rowspan = getNodeRowSpan(child);
            if (rowspan == REMAINING) {
                rowspan = heights.getLength() - rowIndex;
            }
            double areaX = x;
            for (int j = 0; j < columnIndex; j++) {
                areaX += widths.getSize(j) + snaphgap;
            }
            double areaY = y;
            for (int j = 0; j < rowIndex; j++) {
                areaY += heights.getSize(j) + snapvgap;
            }
            double areaW = widths.getSize(columnIndex);
            for (int j = 2; j <= colspan; j++) {
                areaW += widths.getSize(columnIndex+j-1) + snaphgap;
            }
            double areaH = heights.getSize(rowIndex);
            for (int j = 2; j <= rowspan; j++) {
                areaH += heights.getSize(rowIndex+j-1) + snapvgap;
            }

            HPos halign = getHalignment(child);
            VPos valign = getValignment(child);
            Boolean fillWidth = isFillWidth(child);
            Boolean fillHeight = isFillHeight(child);

            if (halign == null) {
                halign = getColumnHalignment(columnIndex);
            }
            if (valign == null) {
                valign = getRowValignment(rowIndex);
            }
            if (fillWidth == null) {
                fillWidth = shouldColumnFillWidth(columnIndex);
            }
            if (fillHeight == null) {
                fillHeight = shouldRowFillHeight(rowIndex);
            }

            Insets margin = getMargin(child);
            if (margin != null && valign == VPos.BASELINE) {
                // The top margin has already added to rowBaseline[] in computeRowMetric()
                // we do not need to add it again in layoutInArea.
                margin = new Insets(0, margin.getRight(), margin.getBottom(), margin.getLeft());
            }
            //System.out.println("layoutNode("+child.toString()+" row/span="+rowIndex+"/"+rowspan+" col/span="+columnIndex+"/"+colspan+" area="+areaX+","+areaY+" "+areaW+"x"+areaH+""+" rowBaseline="+rowBaseline[rowIndex]);
            layoutInArea(child, areaX, areaY, areaW, areaH, rowBaseline[rowIndex],
                    margin,
                    fillWidth, fillHeight && valign != VPos.BASELINE,
                    halign, valign);
        }
        layoutGridLines(widths, heights, x, y, rowTotal, columnTotal);
        currentHeights = heights;
        currentWidths = widths;
        performingLayout = false;
    }

    private double adjustRowHeights(final CompositeSize heights, double height) {
        assert(height != -1);
        final double snapvgap = snapSpace(getVgap());
        final double top = snapSpace(getInsets().getTop());
        final double bottom = snapSpace(getInsets().getBottom());
        final double vgaps = snapvgap * (getNumberOfRows() - 1);
        final double contentHeight = height - top - bottom;

        // if there are percentage rows, give them their percentages first
        if (rowPercentTotal > 0) {
            for (int i = 0; i < rowPercentHeight.length; i++) {
                if (rowPercentHeight[i] >= 0) {
                    final double size = (contentHeight - vgaps) * (rowPercentHeight[i]/100);
                    heights.setSize(i, size);
                }
            }
        }
        double rowTotal = heights.computeTotal();
        if (rowPercentTotal < 100) {
            double heightAvailable = height - top - bottom - rowTotal;
            // now that both fixed and percentage rows have been computed, divy up any surplus or deficit
            if (heightAvailable != 0) {
                // maybe grow or shrink row heights
                double remaining = growToMultiSpanPreferredHeights(heights, heightAvailable);
                remaining = growOrShrinkRowHeights(heights, Priority.ALWAYS, remaining);
                remaining = growOrShrinkRowHeights(heights, Priority.SOMETIMES, remaining);
                rowTotal += (heightAvailable - remaining);
            }
        }

        return rowTotal;
    }

    private double growToMultiSpanPreferredHeights(CompositeSize heights, double extraHeight) {
        if (extraHeight <= 0) {
            return extraHeight;
        }

        Set<Integer> rowsAlways = new TreeSet<>();
        Set<Integer> rowsSometimes = new TreeSet<>();
        Set<Integer> lastRows = new TreeSet<>();
        for (Entry<Interval, Double> ms : heights.multiSizes()) {
            final Interval interval = ms.getKey();
            for (int i = interval.begin; i < interval.end; ++i) {
                if (rowPercentHeight[i] < 0) {
                    switch (rowGrow[i]) {
                        case ALWAYS:
                            rowsAlways.add(i);
                            break;
                        case SOMETIMES:
                            rowsSometimes.add(i);
                            break;
                    }
                }
            }
            if (rowPercentHeight[interval.end - 1] < 0) {
                lastRows.add(interval.end - 1);
            }
        }

        double remaining = extraHeight;

        while (rowsAlways.size() > 0 && remaining > rowsAlways.size()) {
            double rowPortion = Math.floor(remaining / rowsAlways.size());
            for (Iterator<Integer> it = rowsAlways.iterator(); it.hasNext();) {
                int i = it.next();
                double maxOfRow = getRowMaxHeight(i);
                double prefOfRow = getRowPrefHeight(i);
                double actualPortion = rowPortion;

                for (Entry<Interval, Double> ms : heights.multiSizes()) {
                    final Interval interval = ms.getKey();
                    if (interval.contains(i)) {
                        int intervalRows = 0;
                        for (int j = interval.begin; j < interval.end; ++j) {
                            if (rowsAlways.contains(j)) {
                                intervalRows++;
                            }
                        }
                        double curLength = heights.computeTotal(interval.begin, interval.end);
                        actualPortion = Math.min(Math.floor((ms.getValue() - curLength) / intervalRows),
                                actualPortion);
                    }
                }

                final double current = heights.getSize(i);
                double bounded = maxOfRow >= 0 ? boundedSize(0, current + actualPortion, maxOfRow) :
                        maxOfRow == USE_PREF_SIZE && prefOfRow > 0 ? boundedSize(0, current + actualPortion, prefOfRow) :
                        current + actualPortion;
                final double portionUsed = bounded - current;
                remaining -= portionUsed;
                if (portionUsed != actualPortion || portionUsed == 0) {
                    it.remove();
                }
                heights.setSize(i, bounded);
            }
        }

        while (rowsSometimes.size() > 0 && remaining > rowsSometimes.size()) {
            double colPortion = Math.floor(remaining / rowsSometimes.size());
            for (Iterator<Integer> it = rowsSometimes.iterator(); it.hasNext();) {
                int i = it.next();
                double maxOfRow = getRowMaxHeight(i);
                double prefOfRow = getRowPrefHeight(i);
                double actualPortion = colPortion;

                for (Entry<Interval, Double> ms : heights.multiSizes()) {
                    final Interval interval = ms.getKey();
                    if (interval.contains(i)) {
                        int intervalRows = 0;
                        for (int j = interval.begin; j < interval.end; ++j) {
                            if (rowsSometimes.contains(j)) {
                                intervalRows++;
                            }
                        }
                        double curLength = heights.computeTotal(interval.begin, interval.end);
                        actualPortion = Math.min(Math.floor((ms.getValue() - curLength) / intervalRows),
                                actualPortion);
                    }
                }

                final double current = heights.getSize(i);
                double bounded = maxOfRow >= 0 ? boundedSize(0, current + actualPortion, maxOfRow) :
                        maxOfRow == USE_PREF_SIZE && prefOfRow > 0 ? boundedSize(0, current + actualPortion, prefOfRow) :
                        current + actualPortion;
                final double portionUsed = bounded - current;
                remaining -= portionUsed;
                if (portionUsed != actualPortion || portionUsed == 0) {
                    it.remove();
                }
                heights.setSize(i, bounded);
            }
        }


        while (lastRows.size() > 0 && remaining > lastRows.size()) {
            double colPortion = Math.floor(remaining / lastRows.size());
            for (Iterator<Integer> it = lastRows.iterator(); it.hasNext();) {
                int i = it.next();
                double maxOfRow = getRowMaxHeight(i);
                double prefOfRow = getRowPrefHeight(i);
                double actualPortion = colPortion;

                for (Entry<Interval, Double> ms : heights.multiSizes()) {
                    final Interval interval = ms.getKey();
                    if (interval.end - 1 == i) {
                        double curLength = heights.computeTotal(interval.begin, interval.end);
                        actualPortion = Math.min(ms.getValue() - curLength,
                                actualPortion);
                    }
                }

                final double current = heights.getSize(i);
                double bounded = maxOfRow >= 0 ? boundedSize(0, current + actualPortion, maxOfRow) :
                        maxOfRow == USE_PREF_SIZE && prefOfRow > 0 ? boundedSize(0, current + actualPortion, prefOfRow) :
                        current + actualPortion;
                final double portionUsed = bounded - current;
                remaining -= portionUsed;
                if (portionUsed != actualPortion || portionUsed == 0) {
                    it.remove();
                }
                heights.setSize(i, bounded);
            }
        }
        return remaining;
    }

    private double growOrShrinkRowHeights(CompositeSize heights, Priority priority, double extraHeight) {
        final boolean shrinking = extraHeight < 0;
        List<Integer> adjusting = new ArrayList<>();

        for (int i = 0; i < rowGrow.length; i++) {
            if (rowPercentHeight[i] < 0 && (shrinking || rowGrow[i] == priority)) {
                adjusting.add(i);
            }
        }

        double available = extraHeight; // will be negative in shrinking case
        boolean handleRemainder = false;
        double portion = 0;

        // RT-25684: We have to be careful that when subtracting change
        // that we don't jump right past 0 - this leads to an infinite
        // loop
        final boolean wasPositive = available >= 0.0;
        boolean isPositive = wasPositive;

        CompositeSize limitSize = shrinking? computeMinHeights(null) :
                            computeMaxHeights();
        while (available != 0 && wasPositive == isPositive && adjusting.size() > 0) {
            if (!handleRemainder) {
                portion = available > 0 ? Math.floor(available / adjusting.size()) :
                        Math.ceil(available / adjusting.size()); // negative in shrinking case
            }
            if (portion != 0) {
                for (Iterator<Integer> i = adjusting.iterator(); i.hasNext();) {
                    final int index = i.next();
                    final double limit = snapSpace(limitSize.getProportionalSize(index))
                            - heights.getSize(index); // negative in shrinking case
                    final double change = Math.abs(limit) <= Math.abs(portion)? limit : portion;
                    heights.addSize(index, change);
                    available -= change;
                    isPositive = available >= 0.0;
                    if (Math.abs(change) < Math.abs(portion)) {
                        i.remove();
                    }
                    if (available == 0) {
                        break;
                    }
                }
             } else {
                // Handle the remainder
                portion = (int)(available) % adjusting.size();
                if (portion == 0) {
                    break;
                } else {
                    // We have a remainder evenly distribute it.
                    portion = shrinking ? -1 : 1;
                    handleRemainder = true;
                }
            }
        }

        return available; // might be negative in shrinking case
    }

    private double adjustColumnWidths(final CompositeSize widths, double width) {
        assert(width != -1);
        final double snaphgap = snapSpace(getHgap());
        final double left = snapSpace(getInsets().getLeft());
        final double right = snapSpace(getInsets().getRight());
        final double hgaps = snaphgap * (getNumberOfColumns() - 1);
        final double contentWidth = width - left - right;

        // if there are percentage rows, give them their percentages first
        if (columnPercentTotal > 0) {
            for (int i = 0; i < columnPercentWidth.length; i++) {
                if (columnPercentWidth[i] >= 0) {
                    final double size = (contentWidth - hgaps) * (columnPercentWidth[i]/100);
                    widths.setSize(i, size);
                }
            }
        }

        double columnTotal = widths.computeTotal();
        if (columnPercentTotal < 100) {
            double widthAvailable = width - left - right - columnTotal;
            // now that both fixed and percentage rows have been computed, divy up any surplus or deficit
            if (widthAvailable != 0) {
                // maybe grow or shrink row heights
                double remaining = growToMultiSpanPreferredWidths(widths, widthAvailable);
                remaining = growOrShrinkColumnWidths(widths, Priority.ALWAYS, remaining);
                remaining = growOrShrinkColumnWidths(widths, Priority.SOMETIMES, remaining);
                columnTotal += (widthAvailable - remaining);
            }
        }
        return columnTotal;
    }

    private double growToMultiSpanPreferredWidths(CompositeSize widths, double extraWidth) {
        if (extraWidth <= 0) {
            return extraWidth;
        }

        Set<Integer> columnsAlways = new TreeSet<>();
        Set<Integer> columnsSometimes = new TreeSet<>();
        Set<Integer> lastColumns = new TreeSet<>();
        for (Entry<Interval, Double> ms : widths.multiSizes()) {
            final Interval interval = ms.getKey();
            for (int i = interval.begin; i < interval.end; ++i) {
                if (columnPercentWidth[i] < 0) {
                    switch (columnGrow[i]) {
                        case ALWAYS:
                            columnsAlways.add(i);
                            break;
                        case SOMETIMES:
                            columnsSometimes.add(i);
                            break;
                    }
                }
            }
            if (columnPercentWidth[interval.end - 1] < 0) {
                lastColumns.add(interval.end - 1);
            }
        }

        double remaining = extraWidth;

        while (columnsAlways.size() > 0 && remaining > columnsAlways.size()) {
            double colPortion = Math.floor(remaining / columnsAlways.size());
            for (Iterator<Integer> it = columnsAlways.iterator(); it.hasNext();) {
                int i = it.next();
                double maxOfColumn = getColumnMaxWidth(i);
                double prefOfColumn = getColumnPrefWidth(i);
                double actualPortion = colPortion;

                for (Entry<Interval, Double> ms : widths.multiSizes()) {
                    final Interval interval = ms.getKey();
                    if (interval.contains(i)) {
                        int intervalColumns = 0;
                        for (int j = interval.begin; j < interval.end; ++j) {
                            if (columnsAlways.contains(j)) {
                                intervalColumns++;
                            }
                        }
                        double curLength = widths.computeTotal(interval.begin, interval.end);
                        actualPortion = Math.min(Math.floor((ms.getValue() - curLength) / intervalColumns),
                                actualPortion);
                    }
                }

                final double current = widths.getSize(i);
                double bounded = maxOfColumn >= 0 ? boundedSize(0, current + actualPortion, maxOfColumn) :
                        maxOfColumn == USE_PREF_SIZE && prefOfColumn > 0 ? boundedSize(0, current + actualPortion, prefOfColumn) :
                        current + actualPortion;
                final double portionUsed = bounded - current;
                remaining -= portionUsed;
                if (portionUsed != actualPortion || portionUsed == 0) {
                    it.remove();
                }
                widths.setSize(i, bounded);
            }
        }

        while (columnsSometimes.size() > 0 && remaining > columnsSometimes.size()) {
            double colPortion = Math.floor(remaining / columnsSometimes.size());
            for (Iterator<Integer> it = columnsSometimes.iterator(); it.hasNext();) {
                int i = it.next();
                double maxOfColumn = getColumnMaxWidth(i);
                double prefOfColumn = getColumnPrefWidth(i);
                double actualPortion = colPortion;

                for (Entry<Interval, Double> ms : widths.multiSizes()) {
                    final Interval interval = ms.getKey();
                    if (interval.contains(i)) {
                        int intervalColumns = 0;
                        for (int j = interval.begin; j < interval.end; ++j) {
                            if (columnsSometimes.contains(j)) {
                                intervalColumns++;
                            }
                        }
                        double curLength = widths.computeTotal(interval.begin, interval.end);
                        actualPortion = Math.min(Math.floor((ms.getValue() - curLength) / intervalColumns),
                                actualPortion);
                    }
                }

                final double current = widths.getSize(i);
                double bounded = maxOfColumn >= 0 ? boundedSize(0, current + actualPortion, maxOfColumn) :
                        maxOfColumn == USE_PREF_SIZE && prefOfColumn > 0 ? boundedSize(0, current + actualPortion, prefOfColumn) :
                        current + actualPortion;
                final double portionUsed = bounded - current;
                remaining -= portionUsed;
                if (portionUsed != actualPortion || portionUsed == 0) {
                    it.remove();
                }
                widths.setSize(i, bounded);
            }
        }


        while (lastColumns.size() > 0 && remaining > lastColumns.size()) {
            double colPortion = Math.floor(remaining / lastColumns.size());
            for (Iterator<Integer> it = lastColumns.iterator(); it.hasNext();) {
                int i = it.next();
                double maxOfColumn = getColumnMaxWidth(i);
                double prefOfColumn = getColumnPrefWidth(i);
                double actualPortion = colPortion;

                for (Entry<Interval, Double> ms : widths.multiSizes()) {
                    final Interval interval = ms.getKey();
                    if (interval.end - 1 == i) {
                        double curLength = widths.computeTotal(interval.begin, interval.end);
                        actualPortion = Math.min(ms.getValue() - curLength,
                                actualPortion);
                    }
                }

                final double current = widths.getSize(i);
                double bounded = maxOfColumn >= 0 ? boundedSize(0, current + actualPortion, maxOfColumn) :
                        maxOfColumn == USE_PREF_SIZE && prefOfColumn > 0 ? boundedSize(0, current + actualPortion, prefOfColumn) :
                        current + actualPortion;
                final double portionUsed = bounded - current;
                remaining -= portionUsed;
                if (portionUsed != actualPortion || portionUsed == 0) {
                    it.remove();
                }
                widths.setSize(i, bounded);
            }
        }
        return remaining;
    }

    private double growOrShrinkColumnWidths(CompositeSize widths, Priority priority, double extraWidth) {
        if (extraWidth == 0) {
            return 0;
        }
        final boolean shrinking = extraWidth < 0;
        List<Integer> adjusting = new ArrayList<>();

        for (int i = 0; i < columnGrow.length; i++) {
            if (columnPercentWidth[i] < 0 && (shrinking || columnGrow[i] == priority)) {
                adjusting.add(i);
            }
        }

        double available = extraWidth; // will be negative in shrinking case
        boolean handleRemainder = false;
        double portion = 0;

        // RT-25684: We have to be careful that when subtracting change
        // that we don't jump right past 0 - this leads to an infinite
        // loop
        final boolean wasPositive = available >= 0.0;
        boolean isPositive = wasPositive;

        CompositeSize limitSize = shrinking? computeMinWidths(null) :
                            computeMaxWidths();
        while (available != 0 && wasPositive == isPositive && adjusting.size() > 0) {
            if (!handleRemainder) {
                portion = available > 0 ? Math.floor(available / adjusting.size()) :
                        Math.ceil(available / adjusting.size()); // negative in shrinking case
            }
            if (portion != 0) {
                for (Iterator<Integer> i = adjusting.iterator(); i.hasNext();) {
                    final int index = i.next();
                    final double limit = snapSpace(limitSize.getProportionalSize(index))
                            - widths.getSize(index); // negative in shrinking case
                    final double change = Math.abs(limit) <= Math.abs(portion)? limit : portion;
                    widths.addSize(index, change);
                    available -= change;
                    isPositive = available >= 0.0;
                    if (Math.abs(change) < Math.abs(portion)) {
                        i.remove();
                    }
                    if (available == 0) {
                        break;
                    }
                }
            } else {
                // Handle the remainder
                portion = (int)(available) % adjusting.size();
                if (portion == 0) {
                    break;
                } else {
                    // We have a remainder evenly distribute it.
                    portion = shrinking ? -1 : 1;
                    handleRemainder = true;
                }
            }
        }

        return available; // might be negative in shrinking case
    }

    private void layoutGridLines(CompositeSize columnWidths, CompositeSize rowHeights, double x, double y, double columnHeight, double rowWidth) {
        if (!isGridLinesVisible()) {
            return;
        }
        if (!gridLines.getChildren().isEmpty()) {
            gridLines.getChildren().clear();
        }
        double hgap = snapSpace(getHgap());
        double vgap = snapSpace(getVgap());

        // create vertical lines
        double linex = x;
        double liney = y;
        for (int i = 0; i <= columnWidths.getLength(); i++) {
             gridLines.getChildren().add(createGridLine(linex, liney, linex, liney + columnHeight));
             if (i > 0 && i < columnWidths.getLength() && getHgap() != 0) {
                 linex += getHgap();
                 gridLines.getChildren().add(createGridLine(linex, liney, linex, liney + columnHeight));
             }
             if (i < columnWidths.getLength()) {
                 linex += columnWidths.getSize(i);
             }
        }
        // create horizontal lines
        linex = x;
        for (int i = 0; i <= rowHeights.getLength(); i++) {
            gridLines.getChildren().add(createGridLine(linex, liney, linex + rowWidth, liney));
            if (i > 0 && i < rowHeights.getLength() && getVgap() != 0) {
                liney += getVgap();
                gridLines.getChildren().add(createGridLine(linex, liney, linex + rowWidth, liney));
            }
            if (i < rowHeights.getLength()) {
                liney += rowHeights.getSize(i);
            }
        }
    }

    private Line createGridLine(double startX, double startY, double endX, double endY) {
         Line line = new Line();
         line.setStartX(startX);
         line.setStartY(startY);
         line.setEndX(endX);
         line.setEndY(endY);
         line.setStroke(GRID_LINE_COLOR);
         line.setStrokeDashOffset(GRID_LINE_DASH);

         return line;
    }

    /**
     * Returns a string representation of this {@code GridPane} object.
     * @return a string representation of this {@code GridPane} object.
     */
    @Override public String toString() {
        return "Grid hgap="+getHgap()+", vgap="+getVgap()+", alignment="+getAlignment();
    }

    private CompositeSize createCompositeRows() {
        return new CompositeSize(getNumberOfRows(), rowPercentHeight, rowPercentTotal,
                snapSpace(getVgap()));
    }

    private CompositeSize createCompositeColumns() {
        return new CompositeSize(getNumberOfColumns(), columnPercentWidth, columnPercentTotal,
                snapSpace(getHgap()));
    }

    private int getNodeRowEndConvertRemaining(Node child) {
        int rowSpan = getNodeRowSpan(child);
        return rowSpan != REMAINING? getNodeRowIndex(child) + rowSpan - 1 : getNumberOfRows() - 1;
    }

    private int getNodeColumnEndConvertRemaining(Node child) {
        int columnSpan = getNodeColumnSpan(child);
        return columnSpan != REMAINING? getNodeColumnIndex(child) + columnSpan - 1 : getNumberOfColumns() - 1;
    }


    // This methods are inteded to be used by GridPaneDesignInfo
    private CompositeSize currentHeights;
    private CompositeSize currentWidths;

    double[][] getGrid() {
        if (currentHeights == null || currentWidths == null) {
            return null;
        }
        return new double[][] {currentWidths.asArray(), currentHeights.asArray()};
    }

    /***************************************************************************
     *                                                                         *
     *                         Stylesheet Handling                             *
     *                                                                         *
     **************************************************************************/

      /**
      * Super-lazy instantiation pattern from Bill Pugh.
      * @treatAsPrivate implementation detail
      */
     private static class StyleableProperties {

         private static final CssMetaData<GridPane,Boolean> GRID_LINES_VISIBLE =
             new CssMetaData<GridPane,Boolean>("-fx-grid-lines-visible",
                 BooleanConverter.getInstance(), Boolean.FALSE) {

            @Override
            public boolean isSettable(GridPane node) {
                return node.gridLinesVisible == null ||
                        !node.gridLinesVisible.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(GridPane node) {
                return (StyleableProperty<Boolean>)node.gridLinesVisibleProperty();
            }
         };

         private static final CssMetaData<GridPane,Number> HGAP =
             new CssMetaData<GridPane,Number>("-fx-hgap",
                 SizeConverter.getInstance(), 0.0){

            @Override
            public boolean isSettable(GridPane node) {
                return node.hgap == null || !node.hgap.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(GridPane node) {
                return (StyleableProperty<Number>)node.hgapProperty();
            }

         };

         private static final CssMetaData<GridPane,Pos> ALIGNMENT =
             new CssMetaData<GridPane,Pos>("-fx-alignment",
                 new EnumConverter<Pos>(Pos.class), Pos.TOP_LEFT) {

            @Override
            public boolean isSettable(GridPane node) {
                return node.alignment == null || !node.alignment.isBound();
            }

            @Override
            public StyleableProperty<Pos> getStyleableProperty(GridPane node) {
                return (StyleableProperty<Pos>)node.alignmentProperty();
            }

         };

         private static final CssMetaData<GridPane,Number> VGAP =
             new CssMetaData<GridPane,Number>("-fx-vgap",
                 SizeConverter.getInstance(), 0.0){

            @Override
            public boolean isSettable(GridPane node) {
                return node.vgap == null || !node.vgap.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(GridPane node) {
                return (StyleableProperty<Number>)node.vgapProperty();
            }

         };

         private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
         static {

            final List<CssMetaData<? extends Styleable, ?>> styleables =
                    new ArrayList<CssMetaData<? extends Styleable, ?>>(Region.getClassCssMetaData());
            styleables.add(GRID_LINES_VISIBLE);
            styleables.add(HGAP);
            styleables.add(ALIGNMENT);
            styleables.add(VGAP);

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

    private static final class Interval implements Comparable<Interval> {

        public final int begin;
        public final int end;

        public Interval(int begin, int end) {
            this.begin = begin;
            this.end = end;
        }

        @Override
        public int compareTo(Interval o) {
            return begin != o.begin ? begin - o.begin : end - o.end;
        }

        private boolean contains(int position) {
            return begin <= position && position < end;
        }

        private int size() {
            return end - begin;
        }

    }

    private static final class CompositeSize implements Cloneable {

        // These variables will be modified during the computations
        double singleSizes[];
        private SortedMap<Interval, Double> multiSizes;
        private BitSet preset;

        // Preset metrics for this dimension
        private final double fixedPercent[];
        private final double totalFixedPercent;
        private final double gap;

        public CompositeSize(int capacity, double fixedPercent[], double totalFixedPercent, double gap) {
            singleSizes = new double[capacity];
            Arrays.fill(singleSizes, 0);

            this.fixedPercent = fixedPercent;
            this.totalFixedPercent = totalFixedPercent;
            this.gap = gap;
        }

        private void setSize(int position, double size) {
            singleSizes[position] = size;
        }

        private void setPresetSize(int position, double size) {
            setSize(position, size);
            if (preset == null) {
                preset = new BitSet(singleSizes.length);
            }
            preset.set(position);
        }

        private boolean isPreset(int position) {
            if (preset == null) {
                return false;
            }
            return preset.get(position);
        }

        private void addSize(int position, double change) {
            singleSizes[position] = singleSizes[position] + change;
        }

        private double getSize(int position) {
            return singleSizes[position];
        }

        private void setMaxSize(int position, double size) {
            singleSizes[position] = Math.max(singleSizes[position], size);
        }

        private void setMultiSize(int startPosition, int endPosition, double size) {
            if (multiSizes == null) {
                multiSizes = new TreeMap<>();
            }
            Interval i = new Interval(startPosition, endPosition);
            multiSizes.put(i, size);
        }

        private Iterable<Entry<Interval, Double>> multiSizes() {
            if (multiSizes == null) {
                return Collections.EMPTY_LIST;
            }
            return multiSizes.entrySet();
        }

        private void setMaxMultiSize(int startPosition, int endPosition, double size) {
            if (multiSizes == null) {
                multiSizes = new TreeMap<>();
            }
            Interval i = new Interval(startPosition, endPosition);
            Double sz = multiSizes.get(i);
            if (sz == null) {
                multiSizes.put(i, size);
            } else {
                multiSizes.put(i, Math.max(size, sz));
            }
        }

        private double getProportionalSize(int position) {
            double result = singleSizes[position];
            if (!isPreset(position) && multiSizes != null) {
                for (Interval i : multiSizes.keySet()) {
                    if (i.contains(position)) {
                        double segment = multiSizes.get(i) / i.size();
                        double propSize = segment;
                        for (int j = i.begin; j < i.end; ++j) {
                            if (j != position) {
                                if (singleSizes[j] > segment) {
                                    propSize += singleSizes[j] - segment;
                                }
                            }
                        }
                        result = Math.max(result, propSize);
                    }
                }
            }
            return result;
        }

        private double computeTotal(final int from, final int to) {
            double total = gap * (to - from - 1);
            for (int i = from; i < to; ++i) {
                total += singleSizes[i];
            }
            return total;
        }

        private double computeTotal() {
            return computeTotal(0, singleSizes.length);
        }

        private boolean allPreset(int begin, int end) {
            if (preset == null) {
                return false;
            }
            for (int i = begin; i < end; ++i) {
                if (!preset.get(i)) {
                    return false;
                }
            }
            return true;
        }

        private double computeTotalWithMultiSize() {
            double total = computeTotal();
            if (multiSizes != null) {
                for (Entry<Interval, Double> e: multiSizes.entrySet()) {
                    final Interval i = e.getKey();
                    if (!allPreset(i.begin, i.end)) {
                        double subTotal = computeTotal(i.begin, i.end);
                        if (e.getValue() > subTotal) {
                            total += e.getValue() - subTotal;
                        }
                    }
                }
            }
            if (totalFixedPercent > 0) {
                double totalFixed = 0;
                for (int i = 0; i < fixedPercent.length; ++i) {
                    if (fixedPercent[i] != -1) {
                        totalFixed += singleSizes[i];
                        total = Math.max(total, singleSizes[i] * (100 / fixedPercent[i]));
                    }
                }
                if (totalFixedPercent < 100) {
                    total = Math.max(total, (total - totalFixed) * 100 / (100 - totalFixedPercent));
                }
            }
            return total;
        }

        private int getLength() {
            return singleSizes.length;
        }

        @Override
        protected Object clone() {
            try {
            CompositeSize clone = (CompositeSize) super.clone();
            clone.singleSizes = clone.singleSizes.clone();
            if (multiSizes != null)
                clone.multiSizes = new TreeMap<>(clone.multiSizes);
            return clone;
            } catch (CloneNotSupportedException ex) {
                throw new RuntimeException(ex);
            }
        }

        private double[] asArray() {
            return singleSizes;
        }

=======
package org.cpsolver.studentsct;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;


import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.assignment.DefaultSingleAssignment;
import org.cpsolver.ifs.heuristics.BacktrackNeighbourSelection;
import org.cpsolver.ifs.model.Neighbour;
import org.cpsolver.ifs.solution.Solution;
import org.cpsolver.ifs.solution.SolutionListener;
import org.cpsolver.ifs.solver.ParallelSolver;
import org.cpsolver.ifs.solver.Solver;
import org.cpsolver.ifs.solver.SolverListener;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.JProf;
import org.cpsolver.ifs.util.Progress;
import org.cpsolver.ifs.util.ProgressWriter;
import org.cpsolver.ifs.util.ToolBox;
import org.cpsolver.studentsct.check.CourseLimitCheck;
import org.cpsolver.studentsct.check.InevitableStudentConflicts;
import org.cpsolver.studentsct.check.OverlapCheck;
import org.cpsolver.studentsct.check.SectionLimitCheck;
import org.cpsolver.studentsct.extension.DistanceConflict;
import org.cpsolver.studentsct.extension.TimeOverlapsCounter;
import org.cpsolver.studentsct.filter.CombinedStudentFilter;
import org.cpsolver.studentsct.filter.FreshmanStudentFilter;
import org.cpsolver.studentsct.filter.RandomStudentFilter;
import org.cpsolver.studentsct.filter.ReverseStudentFilter;
import org.cpsolver.studentsct.filter.StudentFilter;
import org.cpsolver.studentsct.heuristics.StudentSctNeighbourSelection;
import org.cpsolver.studentsct.heuristics.selection.BranchBoundSelection;
import org.cpsolver.studentsct.heuristics.selection.OnlineSelection;
import org.cpsolver.studentsct.heuristics.selection.SwapStudentSelection;
import org.cpsolver.studentsct.heuristics.selection.BranchBoundSelection.BranchBoundNeighbour;
import org.cpsolver.studentsct.heuristics.studentord.StudentOrder;
import org.cpsolver.studentsct.heuristics.studentord.StudentRandomOrder;
import org.cpsolver.studentsct.model.AcademicAreaCode;
import org.cpsolver.studentsct.model.Course;
import org.cpsolver.studentsct.model.CourseRequest;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.Offering;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.Student;
import org.cpsolver.studentsct.report.CourseConflictTable;
import org.cpsolver.studentsct.report.DistanceConflictTable;
import org.cpsolver.studentsct.report.SectionConflictTable;
import org.cpsolver.studentsct.report.TimeOverlapConflictTable;
import org.cpsolver.studentsct.report.UnbalancedSectionsTable;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

/**
 * A main class for running of the student sectioning solver from command line. <br>
 * <br>
 * Usage:<br>
 * java -Xmx1024m -jar studentsct-1.1.jar config.properties [input_file]
 * [output_folder] [batch|online|simple]<br>
 * <br>
 * Modes:<br>
 * &nbsp;&nbsp;batch ... batch sectioning mode (default mode -- IFS solver with
 * {@link StudentSctNeighbourSelection} is used)<br>
 * &nbsp;&nbsp;online ... online sectioning mode (students are sectioned one by
 * one, sectioning info (expected/held space) is used)<br>
 * &nbsp;&nbsp;simple ... simple sectioning mode (students are sectioned one by
 * one, sectioning info is not used)<br>
 * See http://www.unitime.org for example configuration files and benchmark data
 * sets.<br>
 * <br>
 * 
 * The test does the following steps:
 * <ul>
 * <li>Provided property file is loaded (see {@link DataProperties}).
 * <li>Output folder is created (General.Output property) and logging is setup
 * (using log4j).
 * <li>Input data are loaded from the given XML file (calling
 * {@link StudentSectioningXMLLoader#load()}).
 * <li>Solver is executed (see {@link Solver}).
 * <li>Resultant solution is saved to an XML file (calling
 * {@link StudentSectioningXMLSaver#save()}.
 * </ul>
 * Also, a log and some reports (e.g., {@link CourseConflictTable} and
 * {@link DistanceConflictTable}) are created in the output folder.
 * 
 * <br>
 * <br>
 * Parameters:
 * <table border='1' summary='Related Solver Parameters'>
 * <tr>
 * <th>Parameter</th>
 * <th>Type</th>
 * <th>Comment</th>
 * </tr>
 * <tr>
 * <td>Test.LastLikeCourseDemands</td>
 * <td>{@link String}</td>
 * <td>Load last-like course demands from the given XML file (in the format that
 * is being used for last like course demand table in the timetabling
 * application)</td>
 * </tr>
 * <tr>
 * <td>Test.StudentInfos</td>
 * <td>{@link String}</td>
 * <td>Load last-like course demands from the given XML file (in the format that
 * is being used for last like course demand table in the timetabling
 * application)</td>
 * </tr>
 * <tr>
 * <td>Test.CrsReq</td>
 * <td>{@link String}</td>
 * <td>Load student requests from the given semi-colon separated list files (in
 * the format that is being used by the old MSF system)</td>
 * </tr>
 * <tr>
 * <td>Test.EtrChk</td>
 * <td>{@link String}</td>
 * <td>Load student information (academic area, classification, major, minor)
 * from the given semi-colon separated list files (in the format that is being
 * used by the old MSF system)</td>
 * </tr>
 * <tr>
 * <td>Sectioning.UseStudentPreferencePenalties</td>
 * <td>{@link Boolean}</td>
 * <td>If true, {@link StudentPreferencePenalties} are used (applicable only for
 * online sectioning)</td>
 * </tr>
 * <tr>
 * <td>Test.StudentOrder</td>
 * <td>{@link String}</td>
 * <td>A class that is used for ordering of students (must be an interface of
 * {@link StudentOrder}, default is {@link StudentRandomOrder}, not applicable
 * only for batch sectioning)</td>
 * </tr>
 * <tr>
 * <td>Test.CombineStudents</td>
 * <td>{@link File}</td>
 * <td>If provided, students are combined from the input file (last-like
 * students) and the provided file (real students). Real non-freshmen students
 * are taken from real data, last-like data are loaded on top of the real data
 * (all students, but weighted to occupy only the remaining space).</td>
 * </tr>
 * <tr>
 * <td>Test.CombineStudentsLastLike</td>
 * <td>{@link File}</td>
 * <td>If provided (together with Test.CombineStudents), students are combined
 * from the this file (last-like students) and Test.CombineStudents file (real
 * students). Real non-freshmen students are taken from real data, last-like
 * data are loaded on top of the real data (all students, but weighted to occupy
 * only the remaining space).</td>
 * </tr>
 * <tr>
 * <td>Test.CombineAcceptProb</td>
 * <td>{@link Double}</td>
 * <td>Used in combining students, probability of a non-freshmen real student to
 * be taken into the combined file (default is 1.0 -- all real non-freshmen
 * students are taken).</td>
 * </tr>
 * <tr>
 * <td>Test.FixPriorities</td>
 * <td>{@link Boolean}</td>
 * <td>If true, course/free time request priorities are corrected (to go from
 * zero, without holes or duplicates).</td>
 * </tr>
 * <tr>
 * <td>Test.ExtraStudents</td>
 * <td>{@link File}</td>
 * <td>If provided, students are loaded from the given file on top of the
 * students loaded from the ordinary input file (students with the same id are
 * skipped).</td>
 * </tr>
 * </table>
 * <br>
 * <br>
 * 
 * @version StudentSct 1.3 (Student Sectioning)<br>
 *          Copyright (C) 2007 - 2014 Tomas Muller<br>
 *          <a href="mailto:muller@unitime.org">muller@unitime.org</a><br>
 *          <a href="http://muller.unitime.org">http://muller.unitime.org</a><br>
 * <br>
 *          This library is free software; you can redistribute it and/or modify
 *          it under the terms of the GNU Lesser General Public License as
 *          published by the Free Software Foundation; either version 3 of the
 *          License, or (at your option) any later version. <br>
 * <br>
 *          This library is distributed in the hope that it will be useful, but
 *          WITHOUT ANY WARRANTY; without even the implied warranty of
 *          MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *          Lesser General Public License for more details. <br>
 * <br>
 *          You should have received a copy of the GNU Lesser General Public
 *          License along with this library; if not see
 *          <a href='http://www.gnu.org/licenses/'>http://www.gnu.org/licenses/</a>.
 */

public class Test {
    private static org.apache.log4j.Logger sLog = org.apache.log4j.Logger.getLogger(Test.class);
    private static java.text.SimpleDateFormat sDateFormat = new java.text.SimpleDateFormat("yyMMdd_HHmmss",
            java.util.Locale.US);
    private static DecimalFormat sDF = new DecimalFormat("0.000");

    /** Load student sectioning model 
     * @param cfg solver configuration
     * @return loaded solution
     **/
    public static Solution<Request, Enrollment> load(DataProperties cfg) {
        StudentSectioningModel model = null;
        Assignment<Request, Enrollment> assignment = null;
        try {
            if (cfg.getProperty("Test.CombineStudents") == null) {
                model = new StudentSectioningModel(cfg);
                assignment = new DefaultSingleAssignment<Request, Enrollment>();
                new StudentSectioningXMLLoader(model, assignment).load();
            } else {
                Solution<Request, Enrollment> solution = combineStudents(cfg,
                        new File(cfg.getProperty("Test.CombineStudentsLastLike", cfg.getProperty("General.Input", "." + File.separator + "solution.xml"))),
                        new File(cfg.getProperty("Test.CombineStudents")));
                model = (StudentSectioningModel)solution.getModel();
                assignment = solution.getAssignment();
            }
            if (cfg.getProperty("Test.ExtraStudents") != null) {
                StudentSectioningXMLLoader extra = new StudentSectioningXMLLoader(model, assignment);
                extra.setInputFile(new File(cfg.getProperty("Test.ExtraStudents")));
                extra.setLoadOfferings(false);
                extra.setLoadStudents(true);
                extra.setStudentFilter(new ExtraStudentFilter(model));
                extra.load();
            }
            if (cfg.getProperty("Test.LastLikeCourseDemands") != null)
                loadLastLikeCourseDemandsXml(model, new File(cfg.getProperty("Test.LastLikeCourseDemands")));
            if (cfg.getProperty("Test.StudentInfos") != null)
                loadStudentInfoXml(model, new File(cfg.getProperty("Test.StudentInfos")));
            if (cfg.getProperty("Test.CrsReq") != null)
                loadCrsReqFiles(model, cfg.getProperty("Test.CrsReq"));
        } catch (Exception e) {
            sLog.error("Unable to load model, reason: " + e.getMessage(), e);
            return null;
        }
        if (cfg.getPropertyBoolean("Debug.DistanceConflict", false))
            DistanceConflict.sDebug = true;
        if (cfg.getPropertyBoolean("Debug.BranchBoundSelection", false))
            BranchBoundSelection.sDebug = true;
        if (cfg.getPropertyBoolean("Debug.SwapStudentsSelection", false))
            SwapStudentSelection.sDebug = true;
        if (cfg.getPropertyBoolean("Debug.TimeOverlaps", false))
            TimeOverlapsCounter.sDebug = true;
        if (cfg.getProperty("CourseRequest.SameTimePrecise") != null)
            CourseRequest.sSameTimePrecise = cfg.getPropertyBoolean("CourseRequest.SameTimePrecise", false);
        Logger.getLogger(BacktrackNeighbourSelection.class).setLevel(
                cfg.getPropertyBoolean("Debug.BacktrackNeighbourSelection", false) ? Level.DEBUG : Level.INFO);
        if (cfg.getPropertyBoolean("Test.FixPriorities", false))
            fixPriorities(model);
        return new Solution<Request, Enrollment>(model, assignment);
    }

    /** Batch sectioning test 
     * @param cfg solver configuration
     * @return resultant solution
     **/
    public static Solution<Request, Enrollment> batchSectioning(DataProperties cfg) {
        Solution<Request, Enrollment> solution = load(cfg);
        if (solution == null)
            return null;
        StudentSectioningModel model = (StudentSectioningModel)solution.getModel();

        if (cfg.getPropertyBoolean("Test.ComputeSectioningInfo", true))
            model.clearOnlineSectioningInfos();
        
        Progress.getInstance(model).addProgressListener(new ProgressWriter(System.out));

        solve(solution, cfg);

        return solution;
    }

    /** Online sectioning test 
     * @param cfg solver configuration
     * @return resultant solution
     * @throws Exception thrown when the sectioning fails
     **/
    public static Solution<Request, Enrollment> onlineSectioning(DataProperties cfg) throws Exception {
        Solution<Request, Enrollment> solution = load(cfg);
        if (solution == null)
            return null;
        StudentSectioningModel model = (StudentSectioningModel)solution.getModel();
        Assignment<Request, Enrollment> assignment = solution.getAssignment();

        solution.addSolutionListener(new TestSolutionListener());
        double startTime = JProf.currentTimeSec();

        Solver<Request, Enrollment> solver = new Solver<Request, Enrollment>(cfg);
        solver.setInitalSolution(solution);
        solver.initSolver();

        OnlineSelection onlineSelection = new OnlineSelection(cfg);
        onlineSelection.init(solver);

        double totalPenalty = 0, minPenalty = 0, maxPenalty = 0;
        double minAvEnrlPenalty = 0, maxAvEnrlPenalty = 0;
        double totalPrefPenalty = 0, minPrefPenalty = 0, maxPrefPenalty = 0;
        double minAvEnrlPrefPenalty = 0, maxAvEnrlPrefPenalty = 0;
        int nrChoices = 0, nrEnrollments = 0, nrCourseRequests = 0;
        int chChoices = 0, chCourseRequests = 0, chStudents = 0;

        int choiceLimit = model.getProperties().getPropertyInt("Test.ChoicesLimit", -1);

        File outDir = new File(model.getProperties().getProperty("General.Output", "."));
        outDir.mkdirs();
        PrintWriter pw = new PrintWriter(new FileWriter(new File(outDir, "choices.csv")));

        List<Student> students = model.getStudents();
        try {
            @SuppressWarnings("rawtypes")
            Class studentOrdClass = Class.forName(model.getProperties().getProperty("Test.StudentOrder", StudentRandomOrder.class.getName()));
            @SuppressWarnings("unchecked")
            StudentOrder studentOrd = (StudentOrder) studentOrdClass.getConstructor(new Class[] { DataProperties.class }).newInstance(new Object[] { model.getProperties() });
            students = studentOrd.order(model.getStudents());
        } catch (Exception e) {
            sLog.error("Unable to reorder students, reason: " + e.getMessage(), e);
        }
        
        ShutdownHook hook = new ShutdownHook(solver);
        Runtime.getRuntime().addShutdownHook(hook);

        for (Student student : students) {
            if (student.nrAssignedRequests(assignment) > 0)
                continue; // skip students with assigned courses (i.e., students
                          // already assigned by a batch sectioning process)
            sLog.info("Sectioning student: " + student);

            BranchBoundSelection.Selection selection = onlineSelection.getSelection(assignment, student);
            BranchBoundNeighbour neighbour = selection.select();
            if (neighbour != null) {
                StudentPreferencePenalties penalties = null;
                if (selection instanceof OnlineSelection.EpsilonSelection) {
                    OnlineSelection.EpsilonSelection epsSelection = (OnlineSelection.EpsilonSelection) selection;
                    penalties = epsSelection.getPenalties();
                    for (int i = 0; i < neighbour.getAssignment().length; i++) {
                        Request r = student.getRequests().get(i);
                        if (r instanceof CourseRequest) {
                            nrCourseRequests++;
                            chCourseRequests++;
                            int chChoicesThisRq = 0;
                            CourseRequest request = (CourseRequest) r;
                            for (Enrollment x : request.getAvaiableEnrollments(assignment)) {
                                nrEnrollments++;
                                if (epsSelection.isAllowed(i, x)) {
                                    nrChoices++;
                                    if (choiceLimit <= 0 || chChoicesThisRq < choiceLimit) {
                                        chChoices++;
                                        chChoicesThisRq++;
                                    }
                                }
                            }
                        }
                    }
                    chStudents++;
                    if (chStudents == 100) {
                        pw.println(sDF.format(((double) chChoices) / chCourseRequests));
                        pw.flush();
                        chStudents = 0;
                        chChoices = 0;
                        chCourseRequests = 0;
                    }
                }
                for (int i = 0; i < neighbour.getAssignment().length; i++) {
                    if (neighbour.getAssignment()[i] == null)
                        continue;
                    Enrollment enrollment = neighbour.getAssignment()[i];
                    if (enrollment.getRequest() instanceof CourseRequest) {
                        CourseRequest request = (CourseRequest) enrollment.getRequest();
                        double[] avEnrlMinMax = getMinMaxAvailableEnrollmentPenalty(assignment, request);
                        minAvEnrlPenalty += avEnrlMinMax[0];
                        maxAvEnrlPenalty += avEnrlMinMax[1];
                        totalPenalty += enrollment.getPenalty();
                        minPenalty += request.getMinPenalty();
                        maxPenalty += request.getMaxPenalty();
                        if (penalties != null) {
                            double[] avEnrlPrefMinMax = penalties.getMinMaxAvailableEnrollmentPenalty(assignment, enrollment.getRequest());
                            minAvEnrlPrefPenalty += avEnrlPrefMinMax[0];
                            maxAvEnrlPrefPenalty += avEnrlPrefMinMax[1];
                            totalPrefPenalty += penalties.getPenalty(enrollment);
                            minPrefPenalty += penalties.getMinPenalty(enrollment.getRequest());
                            maxPrefPenalty += penalties.getMaxPenalty(enrollment.getRequest());
                        }
                    }
                }
                neighbour.assign(assignment, solution.getIteration());
                sLog.info("Student " + student + " enrolls into " + neighbour);
                onlineSelection.updateSpace(assignment, student);
            } else {
                sLog.warn("No solution found.");
            }
            solution.update(JProf.currentTimeSec() - startTime);
        }

        if (chCourseRequests > 0)
            pw.println(sDF.format(((double) chChoices) / chCourseRequests));

        pw.flush();
        pw.close();
        
        HashMap<String, String> extra = new HashMap<String, String>();
        sLog.info("Overall penalty is " + getPerc(totalPenalty, minPenalty, maxPenalty) + "% ("
                + sDF.format(totalPenalty) + "/" + sDF.format(minPenalty) + ".." + sDF.format(maxPenalty) + ")");
        extra.put("Overall penalty", getPerc(totalPenalty, minPenalty, maxPenalty) + "% (" + sDF.format(totalPenalty)
                + "/" + sDF.format(minPenalty) + ".." + sDF.format(maxPenalty) + ")");
        extra.put("Overall available enrollment penalty", getPerc(totalPenalty, minAvEnrlPenalty, maxAvEnrlPenalty)
                + "% (" + sDF.format(totalPenalty) + "/" + sDF.format(minAvEnrlPenalty) + ".." + sDF.format(maxAvEnrlPenalty) + ")");
        if (onlineSelection.isUseStudentPrefPenalties()) {
            sLog.info("Overall preference penalty is " + getPerc(totalPrefPenalty, minPrefPenalty, maxPrefPenalty)
                    + "% (" + sDF.format(totalPrefPenalty) + "/" + sDF.format(minPrefPenalty) + ".." + sDF.format(maxPrefPenalty) + ")");
            extra.put("Overall preference penalty", getPerc(totalPrefPenalty, minPrefPenalty, maxPrefPenalty) + "% ("
                    + sDF.format(totalPrefPenalty) + "/" + sDF.format(minPrefPenalty) + ".." + sDF.format(maxPrefPenalty) + ")");
            extra.put("Overall preference available enrollment penalty", getPerc(totalPrefPenalty,
                    minAvEnrlPrefPenalty, maxAvEnrlPrefPenalty)
                    + "% (" + sDF.format(totalPrefPenalty) + "/" + sDF.format(minAvEnrlPrefPenalty) + ".." + sDF.format(maxAvEnrlPrefPenalty) + ")");
            extra.put("Average number of choices", sDF.format(((double) nrChoices) / nrCourseRequests) + " ("
                    + nrChoices + "/" + nrCourseRequests + ")");
            extra.put("Average number of enrollments", sDF.format(((double) nrEnrollments) / nrCourseRequests) + " ("
                    + nrEnrollments + "/" + nrCourseRequests + ")");
        }
        hook.setExtra(extra);

        return solution;
    }

    /**
     * Minimum and maximum enrollment penalty, i.e.,
     * {@link Enrollment#getPenalty()} of all enrollments
     * @param request a course request
     * @return minimum and maximum of the enrollment penalty
     */
    public static double[] getMinMaxEnrollmentPenalty(CourseRequest request) {
        List<Enrollment> enrollments = request.values();
        if (enrollments.isEmpty())
            return new double[] { 0, 0 };
        double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
        for (Enrollment enrollment : enrollments) {
            double penalty = enrollment.getPenalty();
            min = Math.min(min, penalty);
            max = Math.max(max, penalty);
        }
        return new double[] { min, max };
    }

    /**
     * Minimum and maximum available enrollment penalty, i.e.,
     * {@link Enrollment#getPenalty()} of all available enrollments
     * @param assignment current assignment
     * @param request a course request
     * @return minimum and maximum of the available enrollment penalty
     */
    public static double[] getMinMaxAvailableEnrollmentPenalty(Assignment<Request, Enrollment> assignment, CourseRequest request) {
        List<Enrollment> enrollments = request.getAvaiableEnrollments(assignment);
        if (enrollments.isEmpty())
            return new double[] { 0, 0 };
        double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
        for (Enrollment enrollment : enrollments) {
            double penalty = enrollment.getPenalty();
            min = Math.min(min, penalty);
            max = Math.max(max, penalty);
        }
        return new double[] { min, max };
    }

    /**
     * Compute percentage
     * 
     * @param value
     *            current value
     * @param min
     *            minimal bound
     * @param max
     *            maximal bound
     * @return (value-min)/(max-min)
     */
    public static String getPerc(double value, double min, double max) {
        if (max == min)
            return sDF.format(100.0);
        return sDF.format(100.0 - 100.0 * (value - min) / (max - min));
    }

    /**
     * Print some information about the solution
     * 
     * @param solution
     *            given solution
     * @param computeTables
     *            true, if reports {@link CourseConflictTable} and
     *            {@link DistanceConflictTable} are to be computed as well
     * @param computeSectInfos
     *            true, if online sectioning infou is to be computed as well
     *            (see
     *            {@link StudentSectioningModel#computeOnlineSectioningInfos(Assignment)})
     * @param runChecks
     *            true, if checks {@link OverlapCheck} and
     *            {@link SectionLimitCheck} are to be performed as well
     */
    public static void printInfo(Solution<Request, Enrollment> solution, boolean computeTables, boolean computeSectInfos, boolean runChecks) {
        StudentSectioningModel model = (StudentSectioningModel) solution.getModel();

        if (computeTables) {
            if (solution.getModel().assignedVariables(solution.getAssignment()).size() > 0) {
                try {
                    File outDir = new File(model.getProperties().getProperty("General.Output", "."));
                    outDir.mkdirs();
                    CourseConflictTable cct = new CourseConflictTable((StudentSectioningModel) solution.getModel());
                    cct.createTable(solution.getAssignment(), true, false).save(new File(outDir, "conflicts-lastlike.csv"));
                    cct.createTable(solution.getAssignment(), false, true).save(new File(outDir, "conflicts-real.csv"));

                    DistanceConflictTable dct = new DistanceConflictTable((StudentSectioningModel) solution.getModel());
                    dct.createTable(solution.getAssignment(), true, false).save(new File(outDir, "distances-lastlike.csv"));
                    dct.createTable(solution.getAssignment(), false, true).save(new File(outDir, "distances-real.csv"));
                    
                    SectionConflictTable sct = new SectionConflictTable((StudentSectioningModel) solution.getModel(), SectionConflictTable.Type.OVERLAPS);
                    sct.createTable(solution.getAssignment(), true, false).save(new File(outDir, "time-conflicts-lastlike.csv"));
                    sct.createTable(solution.getAssignment(), false, true).save(new File(outDir, "time-conflicts-real.csv"));
                    
                    SectionConflictTable ust = new SectionConflictTable((StudentSectioningModel) solution.getModel(), SectionConflictTable.Type.UNAVAILABILITIES);
                    ust.createTable(solution.getAssignment(), true, false).save(new File(outDir, "availability-conflicts-lastlike.csv"));
                    ust.createTable(solution.getAssignment(), false, true).save(new File(outDir, "availability-conflicts-real.csv"));
                    
                    SectionConflictTable ct = new SectionConflictTable((StudentSectioningModel) solution.getModel(), SectionConflictTable.Type.OVERLAPS_AND_UNAVAILABILITIES);
                    ct.createTable(solution.getAssignment(), true, false).save(new File(outDir, "section-conflicts-lastlike.csv"));
                    ct.createTable(solution.getAssignment(), false, true).save(new File(outDir, "section-conflicts-real.csv"));
                    
                    UnbalancedSectionsTable ubt = new UnbalancedSectionsTable((StudentSectioningModel) solution.getModel());
                    ubt.createTable(solution.getAssignment(), true, false).save(new File(outDir, "unbalanced-lastlike.csv"));
                    ubt.createTable(solution.getAssignment(), false, true).save(new File(outDir, "unbalanced-real.csv"));
                    
                    TimeOverlapConflictTable toc = new TimeOverlapConflictTable((StudentSectioningModel) solution.getModel());
                    toc.createTable(solution.getAssignment(), true, false).save(new File(outDir, "time-overlaps-lastlike.csv"));
                    toc.createTable(solution.getAssignment(), false, true).save(new File(outDir, "time-overlaps-real.csv"));
                } catch (IOException e) {
                    sLog.error(e.getMessage(), e);
                }
            }

            solution.saveBest();
        }

        if (computeSectInfos)
            model.computeOnlineSectioningInfos(solution.getAssignment());

        if (runChecks) {
            try {
                if (model.getProperties().getPropertyBoolean("Test.InevitableStudentConflictsCheck", false)) {
                    InevitableStudentConflicts ch = new InevitableStudentConflicts(model);
                    if (!ch.check(solution.getAssignment()))
                        ch.getCSVFile().save(
                                new File(new File(model.getProperties().getProperty("General.Output", ".")),
                                        "inevitable-conflicts.csv"));
                }
            } catch (IOException e) {
                sLog.error(e.getMessage(), e);
            }
            new OverlapCheck(model).check(solution.getAssignment());
            new SectionLimitCheck(model).check(solution.getAssignment());
            try {
                CourseLimitCheck ch = new CourseLimitCheck(model);
                if (!ch.check())
                    ch.getCSVFile().save(
                            new File(new File(model.getProperties().getProperty("General.Output", ".")),
                                    "course-limits.csv"));
            } catch (IOException e) {
                sLog.error(e.getMessage(), e);
            }
        }

        sLog.info("Best solution found after " + solution.getBestTime() + " seconds (" + solution.getBestIteration()
                + " iterations).");
        sLog.info("Info: " + ToolBox.dict2string(solution.getExtendedInfo(), 2));
    }

    /** Solve the student sectioning problem using IFS solver 
     * @param solution current solution
     * @param cfg solver configuration
     * @return resultant solution
     **/
    public static Solution<Request, Enrollment> solve(Solution<Request, Enrollment> solution, DataProperties cfg) {
        int nrSolvers = cfg.getPropertyInt("Parallel.NrSolvers", 1);
        Solver<Request, Enrollment> solver = (nrSolvers == 1 ? new Solver<Request, Enrollment>(cfg) : new ParallelSolver<Request, Enrollment>(cfg));
        solver.setInitalSolution(solution);
        if (cfg.getPropertyBoolean("Test.Verbose", false)) {
            solver.addSolverListener(new SolverListener<Request, Enrollment>() {
                @Override
                public boolean variableSelected(Assignment<Request, Enrollment> assignment, long iteration, Request variable) {
                    return true;
                }

                @Override
                public boolean valueSelected(Assignment<Request, Enrollment> assignment, long iteration, Request variable, Enrollment value) {
                    return true;
                }

                @Override
                public boolean neighbourSelected(Assignment<Request, Enrollment> assignment, long iteration, Neighbour<Request, Enrollment> neighbour) {
                    sLog.debug("Select[" + iteration + "]: " + neighbour);
                    return true;
                }

                @Override
                public void neighbourFailed(Assignment<Request, Enrollment> assignment, long iteration, Neighbour<Request, Enrollment> neighbour) {
                    sLog.debug("Failed[" + iteration + "]: " + neighbour);
                }
            });
        }
        solution.addSolutionListener(new TestSolutionListener());
        
        Runtime.getRuntime().addShutdownHook(new ShutdownHook(solver));

        solver.start();
        try {
            solver.getSolverThread().join();
        } catch (InterruptedException e) {
        }

        return solution;
    }

    /**
     * Compute last-like student weight for the given course
     * 
     * @param course
     *            given course
     * @param real
     *            number of real students for the course
     * @param lastLike
     *            number of last-like students for the course
     * @return weight of a student request for the given course
     */
    public static double getLastLikeStudentWeight(Course course, int real, int lastLike) {
        int projected = course.getProjected();
        int limit = course.getLimit();
        if (course.getLimit() < 0) {
            sLog.debug("  -- Course " + course.getName() + " is unlimited.");
            return 1.0;
        }
        if (projected <= 0) {
            sLog.warn("  -- No projected demand for course " + course.getName() + ", using course limit (" + limit
                    + ")");
            projected = limit;
        } else if (limit < projected) {
            sLog.warn("  -- Projected number of students is over course limit for course " + course.getName() + " ("
                    + Math.round(projected) + ">" + limit + ")");
            projected = limit;
        }
        if (lastLike == 0) {
            sLog.warn("  -- No last like info for course " + course.getName());
            return 1.0;
        }
        double weight = ((double) Math.max(0, projected - real)) / lastLike;
        sLog.debug("  -- last like student weight for " + course.getName() + " is " + weight + " (lastLike=" + lastLike
                + ", real=" + real + ", projected=" + projected + ")");
        return weight;
    }

    /**
     * Load last-like students from an XML file (the one that is used to load
     * last like course demands table in the timetabling application)
     * @param model problem model
     * @param xml an XML file
     */
    public static void loadLastLikeCourseDemandsXml(StudentSectioningModel model, File xml) {
        try {
            Document document = (new SAXReader()).read(xml);
            Element root = document.getRootElement();
            HashMap<Course, List<Request>> requests = new HashMap<Course, List<Request>>();
            long reqId = 0;
            for (Iterator<?> i = root.elementIterator("student"); i.hasNext();) {
                Element studentEl = (Element) i.next();
                Student student = new Student(Long.parseLong(studentEl.attributeValue("externalId")));
                student.setDummy(true);
                int priority = 0;
                HashSet<Course> reqCourses = new HashSet<Course>();
                for (Iterator<?> j = studentEl.elementIterator("studentCourse"); j.hasNext();) {
                    Element courseEl = (Element) j.next();
                    String subjectArea = courseEl.attributeValue("subject");
                    String courseNbr = courseEl.attributeValue("courseNumber");
                    Course course = null;
                    offerings: for (Offering offering : model.getOfferings()) {
                        for (Course c : offering.getCourses()) {
                            if (c.getSubjectArea().equals(subjectArea) && c.getCourseNumber().equals(courseNbr)) {
                                course = c;
                                break offerings;
                            }
                        }
                    }
                    if (course == null && courseNbr.charAt(courseNbr.length() - 1) >= 'A'
                            && courseNbr.charAt(courseNbr.length() - 1) <= 'Z') {
                        String courseNbrNoSfx = courseNbr.substring(0, courseNbr.length() - 1);
                        offerings: for (Offering offering : model.getOfferings()) {
                            for (Course c : offering.getCourses()) {
                                if (c.getSubjectArea().equals(subjectArea)
                                        && c.getCourseNumber().equals(courseNbrNoSfx)) {
                                    course = c;
                                    break offerings;
                                }
                            }
                        }
                    }
                    if (course == null) {
                        sLog.warn("Course " + subjectArea + " " + courseNbr + " not found.");
                    } else {
                        if (!reqCourses.add(course)) {
                            sLog.warn("Course " + subjectArea + " " + courseNbr + " already requested.");
                        } else {
                            List<Course> courses = new ArrayList<Course>(1);
                            courses.add(course);
                            CourseRequest request = new CourseRequest(reqId++, priority++, false, student, courses, false, null);
                            List<Request> requestsThisCourse = requests.get(course);
                            if (requestsThisCourse == null) {
                                requestsThisCourse = new ArrayList<Request>();
                                requests.put(course, requestsThisCourse);
                            }
                            requestsThisCourse.add(request);
                        }
                    }
                }
                if (!student.getRequests().isEmpty())
                    model.addStudent(student);
            }
            for (Map.Entry<Course, List<Request>> entry : requests.entrySet()) {
                Course course = entry.getKey();
                List<Request> requestsThisCourse = entry.getValue();
                double weight = getLastLikeStudentWeight(course, 0, requestsThisCourse.size());
                for (Request request : requestsThisCourse) {
                    request.setWeight(weight);
                }
            }
        } catch (Exception e) {
            sLog.error(e.getMessage(), e);
        }
    }

    /**
     * Load course request from the given files (in the format being used by the
     * old MSF system)
     * 
     * @param model
     *            student sectioning model (with offerings loaded)
     * @param files
     *            semi-colon separated list of files to be loaded
     */
    public static void loadCrsReqFiles(StudentSectioningModel model, String files) {
        try {
            boolean lastLike = model.getProperties().getPropertyBoolean("Test.CrsReqIsLastLike", true);
            boolean shuffleIds = model.getProperties().getPropertyBoolean("Test.CrsReqShuffleStudentIds", true);
            boolean tryWithoutSuffix = model.getProperties().getPropertyBoolean("Test.CrsReqTryWithoutSuffix", false);
            HashMap<Long, Student> students = new HashMap<Long, Student>();
            long reqId = 0;
            for (StringTokenizer stk = new StringTokenizer(files, ";"); stk.hasMoreTokens();) {
                String file = stk.nextToken();
                sLog.debug("Loading " + file + " ...");
                BufferedReader in = new BufferedReader(new FileReader(file));
                String line;
                int lineIndex = 0;
                while ((line = in.readLine()) != null) {
                    lineIndex++;
                    if (line.length() <= 150)
                        continue;
                    char code = line.charAt(13);
                    if (code == 'H' || code == 'T')
                        continue; // skip header and tail
                    long studentId = Long.parseLong(line.substring(14, 23));
                    Student student = students.get(new Long(studentId));
                    if (student == null) {
                        student = new Student(studentId);
                        if (lastLike)
                            student.setDummy(true);
                        students.put(new Long(studentId), student);
                        sLog.debug("  -- loading student " + studentId + " ...");
                    } else
                        sLog.debug("  -- updating student " + studentId + " ...");
                    line = line.substring(150);
                    while (line.length() >= 20) {
                        String subjectArea = line.substring(0, 4).trim();
                        String courseNbr = line.substring(4, 8).trim();
                        if (subjectArea.length() == 0 || courseNbr.length() == 0) {
                            line = line.substring(20);
                            continue;
                        }
                        /*
                         * // UNUSED String instrSel = line.substring(8,10);
                         * //ZZ - Remove previous instructor selection char
                         * reqPDiv = line.charAt(10); //P - Personal preference;
                         * C - Conflict resolution; //0 - (Zero) used by program
                         * only, for change requests to reschedule division //
                         * (used to reschedule canceled division) String reqDiv
                         * = line.substring(11,13); //00 - Reschedule division
                         * String reqSect = line.substring(13,15); //Contains
                         * designator for designator-required courses String
                         * credit = line.substring(15,19); char nameRaise =
                         * line.charAt(19); //N - Name raise
                         */
                        char action = line.charAt(19); // A - Add; D - Drop; C -
                                                       // Change
                        sLog.debug("    -- requesting " + subjectArea + " " + courseNbr + " (action:" + action
                                + ") ...");
                        Course course = null;
                        offerings: for (Offering offering : model.getOfferings()) {
                            for (Course c : offering.getCourses()) {
                                if (c.getSubjectArea().equals(subjectArea) && c.getCourseNumber().equals(courseNbr)) {
                                    course = c;
                                    break offerings;
                                }
                            }
                        }
                        if (course == null && tryWithoutSuffix && courseNbr.charAt(courseNbr.length() - 1) >= 'A'
                                && courseNbr.charAt(courseNbr.length() - 1) <= 'Z') {
                            String courseNbrNoSfx = courseNbr.substring(0, courseNbr.length() - 1);
                            offerings: for (Offering offering : model.getOfferings()) {
                                for (Course c : offering.getCourses()) {
                                    if (c.getSubjectArea().equals(subjectArea)
                                            && c.getCourseNumber().equals(courseNbrNoSfx)) {
                                        course = c;
                                        break offerings;
                                    }
                                }
                            }
                        }
                        if (course == null) {
                            if (courseNbr.charAt(courseNbr.length() - 1) >= 'A'
                                    && courseNbr.charAt(courseNbr.length() - 1) <= 'Z') {
                            } else {
                                sLog.warn("      -- course " + subjectArea + " " + courseNbr + " not found (file "
                                        + file + ", line " + lineIndex + ")");
                            }
                        } else {
                            CourseRequest courseRequest = null;
                            for (Request request : student.getRequests()) {
                                if (request instanceof CourseRequest
                                        && ((CourseRequest) request).getCourses().contains(course)) {
                                    courseRequest = (CourseRequest) request;
                                    break;
                                }
                            }
                            if (action == 'A') {
                                if (courseRequest == null) {
                                    List<Course> courses = new ArrayList<Course>(1);
                                    courses.add(course);
                                    courseRequest = new CourseRequest(reqId++, student.getRequests().size(), false, student, courses, false, null);
                                } else {
                                    sLog.warn("      -- request for course " + course + " is already present");
                                }
                            } else if (action == 'D') {
                                if (courseRequest == null) {
                                    sLog.warn("      -- request for course " + course
                                            + " is not present -- cannot be dropped");
                                } else {
                                    student.getRequests().remove(courseRequest);
                                }
                            } else if (action == 'C') {
                                if (courseRequest == null) {
                                    sLog.warn("      -- request for course " + course
                                            + " is not present -- cannot be changed");
                                } else {
                                    // ?
                                }
                            } else {
                                sLog.warn("      -- unknown action " + action);
                            }
                        }
                        line = line.substring(20);
                    }
                }
                in.close();
            }
            HashMap<Course, List<Request>> requests = new HashMap<Course, List<Request>>();
            Set<Long> studentIds = new HashSet<Long>();
            for (Student student: students.values()) {
                if (!student.getRequests().isEmpty())
                    model.addStudent(student);
                if (shuffleIds) {
                    long newId = -1;
                    while (true) {
                        newId = 1 + (long) (999999999L * Math.random());
                        if (studentIds.add(new Long(newId)))
                            break;
                    }
                    student.setId(newId);
                }
                if (student.isDummy()) {
                    for (Request request : student.getRequests()) {
                        if (request instanceof CourseRequest) {
                            Course course = ((CourseRequest) request).getCourses().get(0);
                            List<Request> requestsThisCourse = requests.get(course);
                            if (requestsThisCourse == null) {
                                requestsThisCourse = new ArrayList<Request>();
                                requests.put(course, requestsThisCourse);
                            }
                            requestsThisCourse.add(request);
                        }
                    }
                }
            }
            Collections.sort(model.getStudents(), new Comparator<Student>() {
                @Override
                public int compare(Student o1, Student o2) {
                    return Double.compare(o1.getId(), o2.getId());
                }
            });
            for (Map.Entry<Course, List<Request>> entry : requests.entrySet()) {
                Course course = entry.getKey();
                List<Request> requestsThisCourse = entry.getValue();
                double weight = getLastLikeStudentWeight(course, 0, requestsThisCourse.size());
                for (Request request : requestsThisCourse) {
                    request.setWeight(weight);
                }
            }
            if (model.getProperties().getProperty("Test.EtrChk") != null) {
                for (StringTokenizer stk = new StringTokenizer(model.getProperties().getProperty("Test.EtrChk"), ";"); stk
                        .hasMoreTokens();) {
                    String file = stk.nextToken();
                    sLog.debug("Loading " + file + " ...");
                    BufferedReader in = new BufferedReader(new FileReader(file));
                    String line;
                    while ((line = in.readLine()) != null) {
                        if (line.length() < 55)
                            continue;
                        char code = line.charAt(12);
                        if (code == 'H' || code == 'T')
                            continue; // skip header and tail
                        if (code == 'D' || code == 'K')
                            continue; // skip delete nad cancel
                        long studentId = Long.parseLong(line.substring(2, 11));
                        Student student = students.get(new Long(studentId));
                        if (student == null) {
                            sLog.info("  -- student " + studentId + " not found");
                            continue;
                        }
                        sLog.info("  -- reading student " + studentId);
                        String area = line.substring(15, 18).trim();
                        if (area.length() == 0)
                            continue;
                        String clasf = line.substring(18, 20).trim();
                        String major = line.substring(21, 24).trim();
                        String minor = line.substring(24, 27).trim();
                        student.getAcademicAreaClasiffications().clear();
                        student.getMajors().clear();
                        student.getMinors().clear();
                        student.getAcademicAreaClasiffications().add(new AcademicAreaCode(area, clasf));
                        if (major.length() > 0)
                            student.getMajors().add(new AcademicAreaCode(area, major));
                        if (minor.length() > 0)
                            student.getMinors().add(new AcademicAreaCode(area, minor));
                    }
                }
            }
            int without = 0;
            for (Student student: students.values()) {
                if (student.getAcademicAreaClasiffications().isEmpty())
                    without++;
            }
            fixPriorities(model);
            sLog.info("Students without academic area: " + without);
        } catch (Exception e) {
            sLog.error(e.getMessage(), e);
        }
    }

    public static void fixPriorities(StudentSectioningModel model) {
        for (Student student : model.getStudents()) {
            Collections.sort(student.getRequests(), new Comparator<Request>() {
                @Override
                public int compare(Request r1, Request r2) {
                    int cmp = Double.compare(r1.getPriority(), r2.getPriority());
                    if (cmp != 0)
                        return cmp;
                    return Double.compare(r1.getId(), r2.getId());
                }
            });
            int priority = 0;
            for (Request request : student.getRequests()) {
                if (priority != request.getPriority()) {
                    sLog.debug("Change priority of " + request + " to " + priority);
                    request.setPriority(priority);
                }
            }
        }
    }

    /** Load student infos from a given XML file. 
     * @param model problem model
     * @param xml an XML file
     **/
    public static void loadStudentInfoXml(StudentSectioningModel model, File xml) {
        try {
            sLog.info("Loading student infos from " + xml);
            Document document = (new SAXReader()).read(xml);
            Element root = document.getRootElement();
            HashMap<Long, Student> studentTable = new HashMap<Long, Student>();
            for (Student student : model.getStudents()) {
                studentTable.put(new Long(student.getId()), student);
            }
            for (Iterator<?> i = root.elementIterator("student"); i.hasNext();) {
                Element studentEl = (Element) i.next();
                Student student = studentTable.get(Long.valueOf(studentEl.attributeValue("externalId")));
                if (student == null) {
                    sLog.debug(" -- student " + studentEl.attributeValue("externalId") + " not found");
                    continue;
                }
                sLog.debug(" -- loading info for student " + student);
                student.getAcademicAreaClasiffications().clear();
                if (studentEl.element("studentAcadAreaClass") != null)
                    for (Iterator<?> j = studentEl.element("studentAcadAreaClass").elementIterator("acadAreaClass"); j
                            .hasNext();) {
                        Element studentAcadAreaClassElement = (Element) j.next();
                        student.getAcademicAreaClasiffications().add(
                                new AcademicAreaCode(studentAcadAreaClassElement.attributeValue("academicArea"),
                                        studentAcadAreaClassElement.attributeValue("academicClass")));
                    }
                sLog.debug("   -- acad areas classifs " + student.getAcademicAreaClasiffications());
                student.getMajors().clear();
                if (studentEl.element("studentMajors") != null)
                    for (Iterator<?> j = studentEl.element("studentMajors").elementIterator("major"); j.hasNext();) {
                        Element studentMajorElement = (Element) j.next();
                        student.getMajors().add(
                                new AcademicAreaCode(studentMajorElement.attributeValue("academicArea"),
                                        studentMajorElement.attributeValue("code")));
                    }
                sLog.debug("   -- majors " + student.getMajors());
                student.getMinors().clear();
                if (studentEl.element("studentMinors") != null)
                    for (Iterator<?> j = studentEl.element("studentMinors").elementIterator("minor"); j.hasNext();) {
                        Element studentMinorElement = (Element) j.next();
                        student.getMinors().add(
                                new AcademicAreaCode(studentMinorElement.attributeValue("academicArea", ""),
                                        studentMinorElement.attributeValue("code", "")));
                    }
                sLog.debug("   -- minors " + student.getMinors());
            }
        } catch (Exception e) {
            sLog.error(e.getMessage(), e);
        }
    }

    /** Save solution info as XML 
     * @param solution current solution
     * @param extra solution extra info
     * @param file file to write
     **/
    public static void saveInfoToXML(Solution<Request, Enrollment> solution, Map<String, String> extra, File file) {
        FileOutputStream fos = null;
        try {
            Document document = DocumentHelper.createDocument();
            document.addComment("Solution Info");

            Element root = document.addElement("info");
            TreeSet<Map.Entry<String, String>> entrySet = new TreeSet<Map.Entry<String, String>>(
                    new Comparator<Map.Entry<String, String>>() {
                        @Override
                        public int compare(Map.Entry<String, String> e1, Map.Entry<String, String> e2) {
                            return e1.getKey().compareTo(e2.getKey());
                        }
                    });
            entrySet.addAll(solution.getExtendedInfo().entrySet());
            if (extra != null)
                entrySet.addAll(extra.entrySet());
            for (Map.Entry<String, String> entry : entrySet) {
                root.addElement("property").addAttribute("name", entry.getKey()).setText(entry.getValue());
            }

            fos = new FileOutputStream(file);
            (new XMLWriter(fos, OutputFormat.createPrettyPrint())).write(document);
            fos.flush();
            fos.close();
            fos = null;
        } catch (Exception e) {
            sLog.error("Unable to save info, reason: " + e.getMessage(), e);
        } finally {
            try {
                if (fos != null)
                    fos.close();
            } catch (IOException e) {
            }
        }
    }

    private static void fixWeights(StudentSectioningModel model) {
        HashMap<Course, Integer> lastLike = new HashMap<Course, Integer>();
        HashMap<Course, Integer> real = new HashMap<Course, Integer>();
        HashSet<Long> lastLikeIds = new HashSet<Long>();
        HashSet<Long> realIds = new HashSet<Long>();
        for (Student student : model.getStudents()) {
            if (student.isDummy()) {
                if (!lastLikeIds.add(new Long(student.getId()))) {
                    sLog.error("Two last-like student with id " + student.getId());
                }
            } else {
                if (!realIds.add(new Long(student.getId()))) {
                    sLog.error("Two real student with id " + student.getId());
                }
            }
            for (Request request : student.getRequests()) {
                if (request instanceof CourseRequest) {
                    CourseRequest courseRequest = (CourseRequest) request;
                    Course course = courseRequest.getCourses().get(0);
                    Integer cnt = (student.isDummy() ? lastLike : real).get(course);
                    (student.isDummy() ? lastLike : real).put(course, new Integer(
                            (cnt == null ? 0 : cnt.intValue()) + 1));
                }
            }
        }
        for (Student student : new ArrayList<Student>(model.getStudents())) {
            if (student.isDummy() && realIds.contains(new Long(student.getId()))) {
                sLog.warn("There is both last-like and real student with id " + student.getId());
                long newId = -1;
                while (true) {
                    newId = 1 + (long) (999999999L * Math.random());
                    if (!realIds.contains(new Long(newId)) && !lastLikeIds.contains(new Long(newId)))
                        break;
                }
                lastLikeIds.remove(new Long(student.getId()));
                lastLikeIds.add(new Long(newId));
                student.setId(newId);
                sLog.warn("  -- last-like student id changed to " + student.getId());
            }
            for (Request request : new ArrayList<Request>(student.getRequests())) {
                if (!student.isDummy()) {
                    request.setWeight(1.0);
                    continue;
                }
                if (request instanceof CourseRequest) {
                    CourseRequest courseRequest = (CourseRequest) request;
                    Course course = courseRequest.getCourses().get(0);
                    Integer lastLikeCnt = lastLike.get(course);
                    Integer realCnt = real.get(course);
                    courseRequest.setWeight(getLastLikeStudentWeight(course, realCnt == null ? 0 : realCnt.intValue(),
                            lastLikeCnt == null ? 0 : lastLikeCnt.intValue()));
                } else
                    request.setWeight(1.0);
                if (request.getWeight() <= 0.0) {
                    model.removeVariable(request);
                    student.getRequests().remove(request);
                }
            }
            if (student.getRequests().isEmpty()) {
                model.getStudents().remove(student);
            }
        }
    }

    /** Combine students from the provided two files 
     * @param cfg solver configuration
     * @param lastLikeStudentData a file containing last-like student data
     * @param realStudentData a file containing real student data
     * @return combined solution
     **/
    public static Solution<Request, Enrollment> combineStudents(DataProperties cfg, File lastLikeStudentData, File realStudentData) {
        try {
            RandomStudentFilter rnd = new RandomStudentFilter(1.0);

            StudentSectioningModel model = null;
            Assignment<Request, Enrollment> assignment = new DefaultSingleAssignment<Request, Enrollment>();

            for (StringTokenizer stk = new StringTokenizer(cfg.getProperty("Test.CombineAcceptProb", "1.0"), ","); stk.hasMoreTokens();) {
                double acceptProb = Double.parseDouble(stk.nextToken());
                sLog.info("Test.CombineAcceptProb=" + acceptProb);
                rnd.setProbability(acceptProb);

                StudentFilter batchFilter = new CombinedStudentFilter(new ReverseStudentFilter(
                        new FreshmanStudentFilter()), rnd, CombinedStudentFilter.OP_AND);

                model = new StudentSectioningModel(cfg);
                StudentSectioningXMLLoader loader = new StudentSectioningXMLLoader(model, assignment);
                loader.setLoadStudents(false);
                loader.load();

                StudentSectioningXMLLoader lastLikeLoader = new StudentSectioningXMLLoader(model, assignment);
                lastLikeLoader.setInputFile(lastLikeStudentData);
                lastLikeLoader.setLoadOfferings(false);
                lastLikeLoader.setLoadStudents(true);
                lastLikeLoader.load();

                StudentSectioningXMLLoader realLoader = new StudentSectioningXMLLoader(model, assignment);
                realLoader.setInputFile(realStudentData);
                realLoader.setLoadOfferings(false);
                realLoader.setLoadStudents(true);
                realLoader.setStudentFilter(batchFilter);
                realLoader.load();

                fixWeights(model);

                fixPriorities(model);

                Solver<Request, Enrollment> solver = new Solver<Request, Enrollment>(model.getProperties());
                solver.setInitalSolution(model);
                new StudentSectioningXMLSaver(solver).save(new File(new File(model.getProperties().getProperty(
                        "General.Output", ".")), "solution-r" + ((int) (100.0 * acceptProb)) + ".xml"));

            }

            return model == null ? null : new Solution<Request, Enrollment>(model, assignment);

        } catch (Exception e) {
            sLog.error("Unable to combine students, reason: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Setup log4j logging
     * 
     * @param logFile  log file
     */
    public static void setupLogging(File logFile) {
        Logger root = Logger.getRootLogger();
        ConsoleAppender console = new ConsoleAppender(new PatternLayout("[%t] %m%n"));
        console.setThreshold(Level.INFO);
        root.addAppender(console);
        if (logFile != null) {
            try {
                FileAppender file = new FileAppender(new PatternLayout("%d{dd-MMM-yy HH:mm:ss.SSS} [%t] %-5p %c{2}> %m%n"), logFile.getPath(), false);
                file.setThreshold(Level.DEBUG);
                root.addAppender(file);
            } catch (IOException e) {
                sLog.fatal("Unable to configure logging, reason: " + e.getMessage(), e);
            }
        }
    }

    /** Main 
     * @param args program arguments
     **/
    public static void main(String[] args) {
        try {
            DataProperties cfg = new DataProperties();
            cfg.setProperty("Termination.Class", "org.cpsolver.ifs.termination.GeneralTerminationCondition");
            cfg.setProperty("Termination.StopWhenComplete", "true");
            cfg.setProperty("Termination.TimeOut", "600");
            cfg.setProperty("Comparator.Class", "org.cpsolver.ifs.solution.GeneralSolutionComparator");
            cfg.setProperty("Value.Class", "org.cpsolver.studentsct.heuristics.EnrollmentSelection");// org.cpsolver.ifs.heuristics.GeneralValueSelection
            cfg.setProperty("Value.WeightConflicts", "1.0");
            cfg.setProperty("Value.WeightNrAssignments", "0.0");
            cfg.setProperty("Variable.Class", "org.cpsolver.ifs.heuristics.GeneralVariableSelection");
            cfg.setProperty("Neighbour.Class", "org.cpsolver.studentsct.heuristics.StudentSctNeighbourSelection");
            cfg.setProperty("General.SaveBestUnassigned", "0");
            cfg.setProperty("Extensions.Classes",
                    "org.cpsolver.ifs.extension.ConflictStatistics;org.cpsolver.studentsct.extension.DistanceConflict" +
                    ";org.cpsolver.studentsct.extension.TimeOverlapsCounter");
            cfg.setProperty("Data.Initiative", "puWestLafayetteTrdtn");
            cfg.setProperty("Data.Term", "Fal");
            cfg.setProperty("Data.Year", "2007");
            cfg.setProperty("General.Input", "pu-sectll-fal07-s.xml");
            if (args.length >= 1) {
                cfg.load(new FileInputStream(args[0]));
            }
            cfg.putAll(System.getProperties());

            if (args.length >= 2) {
                cfg.setProperty("General.Input", args[1]);
            }

            File outDir = null;
            if (args.length >= 3) {
                outDir = new File(args[2], sDateFormat.format(new Date()));
            } else if (cfg.getProperty("General.Output") != null) {
                outDir = new File(cfg.getProperty("General.Output", "."), sDateFormat.format(new Date()));
            } else {
                outDir = new File(System.getProperty("user.home", ".") + File.separator + "Sectioning-Test" + File.separator + (sDateFormat.format(new Date())));
            }
            outDir.mkdirs();
            setupLogging(new File(outDir, "debug.log"));
            cfg.setProperty("General.Output", outDir.getAbsolutePath());

            if (args.length >= 4 && "online".equals(args[3])) {
                onlineSectioning(cfg);
            } else if (args.length >= 4 && "simple".equals(args[3])) {
                cfg.setProperty("Sectioning.UseOnlinePenalties", "false");
                onlineSectioning(cfg);
            } else {
                batchSectioning(cfg);
            }
        } catch (Exception e) {
            sLog.error(e.getMessage(), e);
            e.printStackTrace();
        }
    }

    public static class ExtraStudentFilter implements StudentFilter {
        HashSet<Long> iIds = new HashSet<Long>();

        public ExtraStudentFilter(StudentSectioningModel model) {
            for (Student student : model.getStudents()) {
                iIds.add(new Long(student.getId()));
            }
        }

        @Override
        public boolean accept(Student student) {
            return !iIds.contains(new Long(student.getId()));
        }
    }

    public static class TestSolutionListener implements SolutionListener<Request, Enrollment> {
        @Override
        public void solutionUpdated(Solution<Request, Enrollment> solution) {
            StudentSectioningModel m = (StudentSectioningModel) solution.getModel();
            if (m.getTimeOverlaps() != null && TimeOverlapsCounter.sDebug)
                m.getTimeOverlaps().checkTotalNrConflicts(solution.getAssignment());
            if (m.getDistanceConflict() != null && DistanceConflict.sDebug)
                m.getDistanceConflict().checkAllConflicts(solution.getAssignment());
        }

        @Override
        public void getInfo(Solution<Request, Enrollment> solution, Map<String, String> info) {
        }

        @Override
        public void getInfo(Solution<Request, Enrollment> solution, Map<String, String> info, Collection<Request> variables) {
        }

        @Override
        public void bestCleared(Solution<Request, Enrollment> solution) {
        }

        @Override
        public void bestSaved(Solution<Request, Enrollment> solution) {
            sLog.info("**BEST** " + ((StudentSectioningModel)solution.getModel()).toString(solution.getAssignment()) + ", TM:" + sDF.format(solution.getTime() / 3600.0) + "h" +
                    (solution.getFailedIterations() > 0 ? ", F:" + sDF.format(100.0 * solution.getFailedIterations() / solution.getIteration()) + "%" : ""));
        }

        @Override
        public void bestRestored(Solution<Request, Enrollment> solution) {
        }
    }
    
    private static class ShutdownHook extends Thread {
        Solver<Request, Enrollment> iSolver = null;
        Map<String, String> iExtra = null;

        private ShutdownHook(Solver<Request, Enrollment> solver) {
            setName("ShutdownHook");
            iSolver = solver;
        }
        
        void setExtra(Map<String, String> extra) { iExtra = extra; }
        
        @Override
        public void run() {
            try {
                if (iSolver.isRunning()) iSolver.stopSolver();
                Solution<Request, Enrollment> solution = iSolver.lastSolution();
                solution.restoreBest();
                DataProperties cfg = iSolver.getProperties();
                
                printInfo(solution,
                        cfg.getPropertyBoolean("Test.CreateReports", true),
                        cfg.getPropertyBoolean("Test.ComputeSectioningInfo", true),
                        cfg.getPropertyBoolean("Test.RunChecks", true));

                try {
                    new StudentSectioningXMLSaver(iSolver).save(new File(new File(cfg.getProperty("General.Output", ".")), "solution.xml"));
                } catch (Exception e) {
                    sLog.error("Unable to save solution, reason: " + e.getMessage(), e);
                }
                
                saveInfoToXML(solution, iExtra, new File(new File(cfg.getProperty("General.Output", ".")), "info.xml"));
                
                Progress.removeInstance(solution.getModel());
            } catch (Throwable t) {
                sLog.error("Test failed.", t);
            }
        }
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

}

