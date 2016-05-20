/*
 * Copyright 2011 JBoss Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.drools.guvnor.client.widgets.decoratedgrid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import org.drools.guvnor.client.messages.Constants;
import org.drools.guvnor.client.resources.DecisionTableResources;
import org.drools.guvnor.client.resources.DecisionTableResources.DecisionTableStyle;
import org.drools.guvnor.client.widgets.decoratedgrid.CellValue.CellState;
import org.drools.guvnor.client.widgets.decoratedgrid.CellValue.GroupedCellValue;
import org.drools.guvnor.client.widgets.decoratedgrid.data.Coordinate;
import org.drools.guvnor.client.widgets.decoratedgrid.data.DynamicData;
import org.drools.guvnor.client.widgets.decoratedgrid.data.DynamicDataRow;
import org.drools.guvnor.client.widgets.decoratedgrid.data.GroupedDynamicDataRow;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.dom.client.TableSectionElement;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Widget;

/**
 * An abstract grid of data. Implementations can choose the orientation to
 * render "rows" and "columns" (e.g. some may transpose the normal meaning to
 * provide a horizontal implementation of normally vertical tabular data)
 */
public abstract class MergableGridWidget<T> extends Widget
    implements
    ValueUpdater<Object>,
    HasSelectedCellChangeHandlers,
    HasRowGroupingChangeHandlers {

    /**
     * Container for a details of a selected cell
     */
    public static class CellSelectionDetail {

        private Coordinate c;
        private int        offsetX;
        private int        offsetY;
        private int        height;
        private int        width;

        CellSelectionDetail(Coordinate c,
                            int offsetX,
                            int offsetY,
                            int height,
                            int width) {
            this.c = c;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.height = height;
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public int getOffsetX() {
            return offsetX;
        }

        public int getOffsetY() {
            return offsetY;
        }

        public int getWidth() {
            return width;
        }

        public Coordinate getCoordinate() {
            return c;
        }

    }

    // Enum to support keyboard navigation
    public enum MOVE_DIRECTION {
        LEFT, RIGHT, UP, DOWN, NONE
    }

    //GWT disable text selection in an HTMLTable. 
    //event.stopPropogation() doesn't prevent text selection
    private native static void disableTextSelectInternal(Element e,
                                                         boolean disable)/*-{
		if (disable) {
			e.ondrag = function() {
				return false;
			};
			e.onselectstart = function() {
				return false;
			};
			e.style.MozUserSelect = "none"
		} else {
			e.ondrag = null;
			e.onselectstart = null;
			e.style.MozUserSelect = "text"
		}
    }-*/;

    // Selections store the actual grid data selected (irrespective of
    // merged cells). So a merged cell spanning 2 rows is stored as 2
    // selections. Selections are ordered by row number so we can
    // iterate top to bottom.
    protected TreeSet<CellValue< ? extends Comparable< ? >>> selections                 = new TreeSet<CellValue< ? extends Comparable< ? >>>(
                                                                                                                                              new Comparator<CellValue< ? extends Comparable< ? >>>() {

                                                                                                                                                  public int compare(CellValue< ? extends Comparable< ? >> o1,
                                                                                                                                                                     CellValue< ? extends Comparable< ? >> o2) {
                                                                                                                                                      return o1.getPhysicalCoordinate().getRow()
                                                                                                                                                             - o2.getPhysicalCoordinate().getRow();
                                                                                                                                                  }

                                                                                                                                              } );

    // TABLE elements
    protected TableElement                                   table;

    protected TableSectionElement                            tbody;

    // Resources
    protected static final Constants                         messages                   = GWT.create( Constants.class );
    protected static final DecisionTableResources            resource                   = GWT.create( DecisionTableResources.class );
    protected static final DecisionTableStyle                style                      = resource.cellTableStyle();

    private static final ImageResource                       selectorGroupedCells       = resource.collapse();
    private static final ImageResource                       selectorUngroupedCells     = resource.expand();
    protected static final String                            selectorGroupedCellsHtml   = makeImageHtml( selectorGroupedCells );
    protected static final String                            selectorUngroupedCellsHtml = makeImageHtml( selectorUngroupedCells );

    private static String makeImageHtml(ImageResource image) {
        return AbstractImagePrototype.create( image ).getHTML();
    }

    // Data and columns to render
    protected List<DynamicColumn<T>> columns              = new ArrayList<DynamicColumn<T>>();
    protected DynamicData            data                 = new DynamicData();

    //Properties for multi-cell selection
    protected CellValue< ? >         rangeOriginCell;
    protected CellValue< ? >         rangeExtentCell;

    protected MOVE_DIRECTION         rangeDirection       = MOVE_DIRECTION.NONE;
    protected boolean                bDragOperationPrimed = false;

    /**
     * A grid of cells.
     */
    public MergableGridWidget() {
        style.ensureInjected();

        // Create some elements to contain the grid
        table = Document.get().createTableElement();
        tbody = Document.get().createTBodyElement();
        table.setClassName( style.cellTable() );
        table.setCellPadding( 0 );
        table.setCellSpacing( 0 );
        setElement( table );

        table.appendChild( tbody );

        // Events in which we're interested (note, if a Cell<?> appears not to
        // work I've probably forgotten some events. Might be a better way of
        // doing this, but I copied CellTable<?, ?>'s lead
        sinkEvents( Event.getTypeInt( "click" )
                    | Event.getTypeInt( "dblclick" )
                    | Event.getTypeInt( "mousedown" )
                    | Event.getTypeInt( "mouseup" )
                    | Event.getTypeInt( "mousemove" )
                    | Event.getTypeInt( "mouseout" )
                    | Event.getTypeInt( "change" )
                    | Event.getTypeInt( "keypress" )
                    | Event.getTypeInt( "keydown" ) );

        //Prevent text selection
        disableTextSelectInternal( table,
                                   true );

    }

    /**
     * Add a handler for RowGroupingChangeEvents
     */
    public HandlerRegistration addRowGroupingChangeHandler(RowGroupingChangeHandler handler) {
        return addHandler( handler,
                           RowGroupingChangeEvent.getType() );
    }

    /**
     * Add a handler for SelectedCellChangeEvents
     */
    public HandlerRegistration addSelectedCellChangeHandler(SelectedCellChangeHandler handler) {
        return addHandler( handler,
                           SelectedCellChangeEvent.getType() );
    }

    /**
     * Delete a column
     * 
     * @param column
     *            Column to delete
     * @param bRedraw
     *            Should grid be redrawn
     */
    public void deleteColumn(DynamicColumn<T> column,
                             boolean bRedraw) {

        //Find index of column
        int index = columns.indexOf( column );
        if ( index == -1 ) {
            throw new IllegalArgumentException(
                                                "Column not found in declared columns." );
        }

        //Expand any merged cells in colum
        boolean bRedrawSidebar = false;
        for ( int iRow = 0; iRow < data.size(); iRow++ ) {
            CellValue< ? > cv = data.get( iRow ).get( index );
            if ( cv.isGrouped() ) {
                removeModelGrouping( cv,
                                     false );
                bRedrawSidebar = true;
            }
        }

        // Clear any selections
        clearSelection();

        // Delete column from grid
        columns.remove( index );
        reindexColumns();

        data.deleteColumn( index );

        // Redraw
        if ( bRedraw ) {
            redraw();
            if ( bRedrawSidebar ) {
                RowGroupingChangeEvent.fire( this );
            }
        }

    }

    /**
     * Delete the given row. Partial redraw.
     * 
     * @param row
     */
    public void deleteRow(DynamicDataRow row) {

        //Find index of row
        int index = data.indexOf( row );
        if ( index == -1 ) {
            throw new IllegalArgumentException(
                                                "DynamicDataRow does not exist in table data." );
        }

        // Clear any selections
        clearSelection();

        //Delete row data
        data.deleteRow( index );

        // Partial redraw
        if ( !data.isMerged() ) {
            // Single row when not merged
            removeRowElement( index );
        } else {
            // Affected rows when merged
            removeRowElement( index );

            if ( data.size() > 0 ) {
                int minRedrawRow = findMinRedrawRow( index - 1 );
                int maxRedrawRow = findMaxRedrawRow( index - 1 ) + 1;
                if ( maxRedrawRow > data.size() - 1 ) {
                    maxRedrawRow = data.size() - 1;
                }
                redrawRows( minRedrawRow,
                                       maxRedrawRow );
            }
        }

    }

    /**
     * Return grid's columns
     * 
     * @return columns
     */
    public List<DynamicColumn<T>> getColumns() {
        return columns;
    }

    /**
     * Return grid's data. Grouping reflected in the UI will be collapsed in the
     * return value. Use of <code>getFlattenedData()</code> should be used in
     * preference if the ungrouped data is needed (e.g. when persisting the
     * model).
     * 
     * @return data
     */
    public DynamicData getData() {
        return data;
    }

    /**
     * Return an immutable list of selected cells
     * 
     * @return The selected cells
     */
    public List<CellValue< ? >> getSelectedCells() {
        return Collections.unmodifiableList( new ArrayList<CellValue< ? >>( this.selections ) );
    }

    /**
     * Insert a column before another
     * 
     * @param columnBefore
     *            The column before which the new column should be inserted
     * @param newColumn
     *            Column definition
     * @param columnData
     *            Data for column
     * @param bRedraw
     *            Should grid be redrawn
     */
    public void insertColumnBefore(DynamicColumn<T> columnBefore,
                                   DynamicColumn<T> newColumn,
                                   List<CellValue< ? extends Comparable< ? >>> columnData,
                                   boolean bRedraw) {

        if ( newColumn == null ) {
            throw new IllegalArgumentException( "newColumn cannot be null" );
        }
        if ( columnData == null ) {
            throw new IllegalArgumentException( "columnData cannot be null" );
        }
        if ( columnData.size() != data.size() ) {
            throw new IllegalArgumentException( "columnData contains a different number of rows to the grid" );
        }

        //Find index of new column
        int index = columns.size();
        if ( columnBefore != null ) {
            index = columns.indexOf( columnBefore );
            if ( index == -1 ) {
                throw new IllegalArgumentException(
                                                    "columnBefore does not exist in table data." );
            }
            index++;
        }

        // Clear any selections
        clearSelection();

        // Add column definition
        columns.add( index,
                     newColumn );
        reindexColumns();

        data.addColumn( index,
                        columnData,
                        newColumn.isVisible() );

        // Redraw
        if ( bRedraw ) {
            redrawColumns( index,
                           columns.size() - 1 );
        }

    }

    /**
     * Insert the given row before the provided index. Partial redraw.
     * 
     * @param rowBefore
     *            The row before which the new row should be inserted
     * @param rowData
     *            The row of data to insert
     */
    public DynamicDataRow insertRowBefore(DynamicDataRow rowBefore,
                                          List<CellValue< ? extends Comparable< ? >>> rowData) {

        if ( rowData == null ) {
            throw new IllegalArgumentException( "Row data cannot be null" );
        }
        if ( rowData.size() != columns.size() ) {
            throw new IllegalArgumentException( "rowData contains a different number of columns to the grid" );
        }

        //Find index of row
        int index = data.size();
        if ( rowBefore != null ) {
            index = data.indexOf( rowBefore );
            if ( index == -1 ) {
                throw new IllegalArgumentException(
                                                    "rowBefore does not exist in table data." );
            }
        }

        // Clear any selections
        clearSelection();

        // Find rows that need to be (re)drawn
        int minRedrawRow = index;
        int maxRedrawRow = index;
        if ( data.isMerged() ) {
            if ( index < data.size() ) {
                minRedrawRow = findMinRedrawRow( index );
                maxRedrawRow = findMaxRedrawRow( index ) + 1;
            } else {
                minRedrawRow = findMinRedrawRow( (index > 0 ? index - 1 : index) );
                maxRedrawRow = index;
            }
        }

        DynamicDataRow row = data.addRow( index,
                                          rowData );

        // Partial redraw
        if ( !data.isMerged() ) {
            // Only new row when not merged
            createRowElement( index,
                              row );
        } else {
            // Affected rows when merged
            createEmptyRowElement( index );
            redrawRows( minRedrawRow,
                        maxRedrawRow );
        }
        return row;

    }

    /**
     * Redraw the whole table
     */
    public abstract void redraw();

    /**
     * Redraw table column. Partial redraw
     * 
     * @param index
     *            Start column index (inclusive)
     */
    public abstract void redrawColumn(int index);

    /**
     * Redraw table columns. Partial redraw
     * 
     * @param startRedrawIndex
     *            Start column index (inclusive)
     * @param endRedrawIndex
     *            End column index (inclusive)
     */
    public abstract void redrawColumns(int startRedrawIndex,
                                       int endRedrawIndex);

    /**
     * Toggle the state of DecoratedGridWidget merging.
     * 
     * @return The state of merging after completing this call
     */
    public boolean toggleMerging() {
        if ( !data.isMerged() ) {
            clearSelection();
            data.setMerged( true );
            redraw();
        } else {
            clearSelection();
            data.setMerged( false );
            redraw();
            RowGroupingChangeEvent.fire( this );
        }
        return data.isMerged();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.google.gwt.cell.client.ValueUpdater#update(java.lang.Object)
     */
    public void update(Object value) {

        boolean bUngroupCells = false;
        Coordinate selection = selections.first().getCoordinate();

        //If selections span multiple cells, any of which are grouped we should ungroup them
        if ( selections.size() > 1 ) {
            for ( CellValue< ? extends Comparable< ? >> cell : selections ) {
                if ( cell instanceof GroupedCellValue ) {
                    bUngroupCells = true;
                    break;
                }
            }
        }

        // Update underlying data (update before ungrouping as selections would need to be expanded too)
        for ( CellValue< ? extends Comparable< ? >> cell : selections ) {
            Coordinate c = cell.getCoordinate();
            if ( !columns.get( c.getCol() ).isSystemControlled() ) {
                data.set( c,
                          value );
            }
            if ( value != null ) {
                cell.removeState( CellState.OTHERWISE );
            }
        }

        //Ungroup if applicable
        if ( bUngroupCells ) {
            for ( CellValue< ? extends Comparable< ? >> cell : selections ) {
                if ( cell instanceof GroupedCellValue ) {
                    removeModelGrouping( cell,
                                         true );
                }
            }
        }

        // Partial redraw
        int baseRowIndex = selections.first().getCoordinate().getRow();
        int minRedrawRow = findMinRedrawRow( baseRowIndex );
        int maxRedrawRow = findMaxRedrawRow( baseRowIndex );

        // When merged cells become unmerged (if their value is
        // cleared need to ensure the re-draw range is at least
        // as large as the selection range
        if ( maxRedrawRow < selections.last().getCoordinate().getRow() ) {
            maxRedrawRow = selections.last().getCoordinate().getRow();
        }
        redrawRows( minRedrawRow,
                    maxRedrawRow );

        //Re-select applicable cells, following change to merge
        startSelecting( selection );
    }

    //Apply grouping by collapsing applicable rows
    private void applyModelGrouping(CellValue< ? > startCell,
                                    boolean bRedraw) {

        data.applyModelGrouping( startCell );

        //Partial redraw
        if ( bRedraw ) {
            int startRowIndex = startCell.getCoordinate().getRow();
            GroupedDynamicDataRow groupedRow = (GroupedDynamicDataRow) data.get( startRowIndex );
            int minRedrawRow = findMinRedrawRow( startRowIndex - (startRowIndex > 0 ? 1 : 0) );
            int maxRedrawRow = findMaxRedrawRow( startRowIndex + (startRowIndex < data.size() - 1 ? 1 : 0) );
            for ( int iRow = 0; iRow < groupedRow.getChildRows().size() - 1; iRow++ ) {
                deleteRowElement( startRowIndex );
            }
            redrawRows( minRedrawRow,
                        maxRedrawRow );
            RowGroupingChangeEvent.fire( this );
        }

    }

    //Check whether two values are equal or both null
    private boolean equalOrNull(Object o1,
                                Object o2) {
        if ( o1 == null && o2 == null ) {
            return true;
        }
        if ( o1 != null && o2 == null ) {
            return false;
        }
        if ( o1 == null && o2 != null ) {
            return false;
        }
        return o1.equals( o2 );
    }

    // Given a base row find the maximum row that needs to be re-rendered based
    // upon each columns merged cells; where each merged cell passes through the
    // base row
    private int findMaxRedrawRow(int baseRowIndex) {

        if ( data.size() == 0 ) {
            return 0;
        }

        // These should never happen, but it's a safe-guard
        if ( baseRowIndex < 0 ) {
            baseRowIndex = 0;
        }
        if ( baseRowIndex > data.size() - 1 ) {
            baseRowIndex = data.size() - 1;
        }

        int maxRedrawRow = baseRowIndex;
        DynamicDataRow baseRow = data.get( baseRowIndex );
        for ( int iCol = 0; iCol < baseRow.size(); iCol++ ) {
            int iRow = baseRowIndex;
            CellValue< ? extends Comparable< ? >> cell = baseRow.get( iCol );
            while ( cell.getRowSpan() != 1
                    && iRow < data.size() - 1 ) {
                iRow++;
                DynamicDataRow row = data.get( iRow );
                cell = row.get( iCol );
            }
            maxRedrawRow = (iRow > maxRedrawRow ? iRow : maxRedrawRow);
        }
        return maxRedrawRow;
    }

    //Find the bottom coordinate of a merged cell
    private Coordinate findMergedCellExtent(Coordinate c) {
        if ( c.getRow() == data.size() - 1 ) {
            return c;
        }
        Coordinate nc = new Coordinate( c.getRow() + 1,
                                        c.getCol() );
        CellValue< ? > newCell = data.get( nc );
        while ( newCell.getRowSpan() == 0 && nc.getRow() < data.size() - 1 ) {
            nc = new Coordinate( nc.getRow() + 1,
                                     nc.getCol() );
            newCell = data.get( nc );
        }
        if ( newCell.getRowSpan() != 0 ) {
            nc = new Coordinate( nc.getRow() - 1,
                                     nc.getCol() );
        }
        return nc;
    }

    // Given a base row find the minimum row that needs to be re-rendered based
    // upon each columns merged cells; where each merged cell passes through the
    // base row
    private int findMinRedrawRow(int baseRowIndex) {

        if ( data.size() == 0 ) {
            return 0;
        }

        // These should never happen, but it's a safe-guard
        if ( baseRowIndex < 0 ) {
            baseRowIndex = 0;
        }
        if ( baseRowIndex > data.size() - 1 ) {
            baseRowIndex = data.size() - 1;
        }

        int minRedrawRow = baseRowIndex;
        DynamicDataRow baseRow = data.get( baseRowIndex );
        for ( int iCol = 0; iCol < baseRow.size(); iCol++ ) {
            int iRow = baseRowIndex;
            CellValue< ? extends Comparable< ? >> cell = baseRow.get( iCol );
            while ( cell.getRowSpan() != 1
                    && iRow > 0 ) {
                iRow--;
                DynamicDataRow row = data.get( iRow );
                cell = row.get( iCol );
            }
            minRedrawRow = (iRow < minRedrawRow ? iRow : minRedrawRow);
        }
        return minRedrawRow;
    }

    //Get the next cell when selection moves in the specified direction
    private Coordinate getNextCell(Coordinate c,
                                    MOVE_DIRECTION dir) {

        int step = 0;
        Coordinate nc = c;

        switch ( dir ) {
            case LEFT :

                // Move left
                step = c.getCol() > 0 ? 1 : 0;
                if ( step > 0 ) {
                    nc = new Coordinate( c.getRow(),
                                         c.getCol()
                                                 - step );

                    // Skip hidden columns
                    while ( nc.getCol() > 0
                            && !columns.get( nc.getCol() ).isVisible() ) {
                        nc = new Coordinate( c.getRow(),
                                             nc.getCol()
                                                     - step );
                    }

                    //Move to top of a merged cells
                    CellValue< ? > newCell = data.get( nc );
                    while ( newCell.getRowSpan() == 0 ) {
                        nc = new Coordinate( nc.getRow() - 1,
                                             nc.getCol() );
                        newCell = data.get( nc );
                    }

                }
                break;
            case RIGHT :

                // Move right
                step = c.getCol() < columns.size() - 1 ? 1 : 0;
                if ( step > 0 ) {
                    nc = new Coordinate( c.getRow(),
                                         c.getCol()
                                                 + step );

                    // Skip hidden columns
                    while ( nc.getCol() < columns.size() - 2
                            && !columns.get( nc.getCol() ).isVisible() ) {
                        nc = new Coordinate( c.getRow(),
                                             nc.getCol()
                                                     + step );
                    }

                    //Move to top of a merged cells
                    CellValue< ? > newCell = data.get( nc );
                    while ( newCell.getRowSpan() == 0 ) {
                        nc = new Coordinate( nc.getRow() - 1,
                                             nc.getCol() );
                        newCell = data.get( nc );
                    }

                }
                break;
            case UP :

                // Move up
                step = c.getRow() > 0 ? 1 : 0;
                if ( step > 0 ) {
                    nc = new Coordinate( c.getRow()
                                                 - step,
                                         c.getCol() );

                    //Move to top of a merged cells
                    CellValue< ? > newCell = data.get( nc );
                    while ( newCell.getRowSpan() == 0 ) {
                        nc = new Coordinate( nc.getRow() - step,
                                             nc.getCol() );
                        newCell = data.get( nc );
                    }

                }
                break;
            case DOWN :

                // Move down
                step = c.getRow() < data.size() - 1 ? 1 : 0;
                if ( step > 0 ) {
                    nc = new Coordinate( c.getRow()
                                                 + step,
                                         c.getCol() );

                    //Move to top of a merged cells
                    CellValue< ? > newCell = data.get( nc );
                    while ( newCell.getRowSpan() == 0 && nc.getRow() < data.size() - 1 ) {
                        nc = new Coordinate( nc.getRow() + step,
                                             nc.getCol() );
                        newCell = data.get( nc );
                    }
                    if ( newCell.getRowSpan() == 0 && nc.getRow() == data.size() - 1 ) {
                        nc = c;
                    }

                }
        }
        return nc;
    }

    // Re-index columns
    private void reindexColumns() {
        for ( int iCol = 0; iCol < columns.size(); iCol++ ) {
            DynamicColumn<T> col = columns.get( iCol );
            col.setColumnIndex( iCol );
        }
    }

    //Remove grouping by expanding applicable rows
    private void removeModelGrouping(CellValue< ? > startCell,
                                     boolean bRedraw) {

        List<DynamicDataRow> expandedRow = data.removeModelGrouping( startCell );

        //Partial redraw
        if ( bRedraw ) {
            int startRowIndex = startCell.getCoordinate().getRow();
            int minRedrawRow = findMinRedrawRow( startRowIndex - (startRowIndex > 0 ? 1 : 0) );
            int maxRedrawRow = findMaxRedrawRow( startRowIndex + (startRowIndex < data.size() - 2 ? 1 : 0) );
            for ( int iRow = 0; iRow < expandedRow.size() - 1; iRow++ ) {
                createEmptyRowElement( startRowIndex );

            }
            redrawRows( minRedrawRow,
                        maxRedrawRow );
            RowGroupingChangeEvent.fire( this );
        }

    }

    //Clear all selections
    protected void clearSelection() {
        // De-select any previously selected cells
        for ( CellValue< ? extends Comparable< ? >> cell : this.selections ) {
            cell.removeState( CellState.SELECTED );
            deselectCell( cell );
        }

        // Clear collection
        selections.clear();
        rangeDirection = MOVE_DIRECTION.NONE;
    }

    protected abstract void createEmptyRowElement(int index);

    protected abstract void createRowElement(int index,
                                             DynamicDataRow rowData);

    protected abstract void deleteRowElement(int index);

    //Check whether "Grouping" widget has been clicked
    protected boolean isGroupWidgetClicked(Event event,
                                           Element target) {
        String eventType = event.getType();
        if ( eventType.equals( "mousedown" ) ) {
            String tagName = target.getTagName();
            if ( "img".equalsIgnoreCase( tagName ) ) {
                return true;
            }
        }
        return false;
    }

    /**
     * Redraw table rows. Partial redraw
     * 
     * @param startRedrawIndex
     *            Start row index (inclusive)
     * @param endRedrawIndex
     *            End row index (inclusive)
     */
    protected abstract void redrawRows(int startRedrawIndex,
                                       int endRedrawIndex);

    protected abstract void removeRowElement(int index);

    /**
     * Remove styling indicating a selected state
     * 
     * @param cell
     */
    abstract void deselectCell(CellValue< ? extends Comparable< ? >> cell);

    /**
     * Extend selection from the first cell selected to the cell specified
     * 
     * @param end
     *            Extent of selection
     */
    void extendSelection(Coordinate end) {
        if ( rangeOriginCell == null ) {
            throw new IllegalArgumentException( "origin has not been set. Unable to extend selection" );
        }
        if ( end == null ) {
            throw new IllegalArgumentException( "end cannot be null" );
        }
        clearSelection();
        CellValue< ? > endCell = data.get( end );
        selectRange( rangeOriginCell,
                     endCell );
        if ( rangeOriginCell.getCoordinate().getRow() > endCell.getCoordinate().getRow() ) {
            rangeExtentCell = selections.first();
            rangeDirection = MOVE_DIRECTION.UP;
        } else {
            rangeExtentCell = selections.last();
            rangeDirection = MOVE_DIRECTION.DOWN;
        }
    }

    /**
     * Extend selection in the specified direction
     * 
     * @param dir
     *            Direction to extend the selection
     */
    void extendSelection(MOVE_DIRECTION dir) {
        if ( selections.size() > 0 ) {
            CellValue< ? > activeCell = (rangeExtentCell == null ? rangeOriginCell : rangeExtentCell);
            Coordinate nc = getNextCell( activeCell.getCoordinate(),
                                         dir );
            clearSelection();
            rangeDirection = dir;
            rangeExtentCell = data.get( nc );
            selectRange( rangeOriginCell,
                             rangeExtentCell );

        }
    }

    /**
     * Retrieve the extents of a cell
     * 
     * @param cv
     *            The cell for which to retrieve the extents
     * @return
     */
    CellSelectionDetail getSelectedCellExtents(CellValue< ? extends Comparable< ? >> cv) {

        if ( cv == null ) {
            throw new IllegalArgumentException( "cv cannot be null" );
        }

        // Cells in hidden columns do not have extents
        if ( !columns.get( cv.getCoordinate().getCol() )
                .isVisible() ) {
            return null;
        }

        Coordinate hc = cv.getHtmlCoordinate();
        TableRowElement tre = tbody.getRows().getItem( hc.getRow() )
                .<TableRowElement> cast();
        TableCellElement tce = tre.getCells().getItem( hc.getCol() )
                .<TableCellElement> cast();
        int offsetX = tce.getOffsetLeft();
        int offsetY = tce.getOffsetTop();
        int w = tce.getOffsetWidth();
        int h = tce.getOffsetHeight();
        CellSelectionDetail e = new CellSelectionDetail( cv.getCoordinate(),
                                                         offsetX,
                                                         offsetY,
                                                         h,
                                                         w );
        return e;
    }

    /**
     * Group a merged cell. If the cell is not merged across at least two rows
     * or the cell is not the top of the merged range no action is taken.
     * 
     * @param start
     *            Coordinate of top of merged group.
     */
    void groupCells(Coordinate start) {
        if ( start == null ) {
            throw new IllegalArgumentException( "start cannot be null" );
        }
        CellValue< ? > startCell = data.get( start );

        //Start cell needs to be top of a merged range
        if ( startCell.getRowSpan() <= 1 && !startCell.isGrouped() ) {
            return;
        }

        clearSelection();
        if ( startCell.isGrouped() ) {
            removeModelGrouping( startCell,
                                 true );
        } else {
            applyModelGrouping( startCell,
                                true );
        }

    }

    /**
     * Hide a column
     */
    abstract void hideColumn(int index);

    /**
     * Move the selected cell
     * 
     * @param dir
     *            Direction to move the selection
     */
    void moveSelection(MOVE_DIRECTION dir) {
        if ( selections.size() > 0 ) {
            CellValue< ? > activeCell = (rangeExtentCell == null ? rangeOriginCell : rangeExtentCell);
            Coordinate nc = getNextCell( activeCell.getCoordinate(),
                                         dir );
            startSelecting( nc );
            rangeDirection = dir;
        }
    }

    /**
     * Resize a column
     * 
     * @param col
     * @param width
     */
    abstract void resizeColumn(DynamicColumn< ? > col,
                                      int width);

    /**
     * Add styling to cell to indicate a selected state
     * 
     * @param cell
     */
    abstract void selectCell(CellValue< ? extends Comparable< ? >> cell);

    /**
     * Select a range of cells
     * 
     * @param startCell
     *            The first cell to select
     * @param endCell
     *            The last cell to select
     */
    void selectRange(CellValue< ? > startCell,
                             CellValue< ? > endCell) {
        int col = startCell.getCoordinate().getCol();

        //Ensure startCell precedes endCell
        if ( startCell.getCoordinate().getRow() > endCell.getCoordinate().getRow() ) {
            CellValue< ? > swap = startCell;
            startCell = endCell;
            endCell = swap;
        }

        //Ensure startCell is at the top of a merged cell
        while ( startCell.getRowSpan() == 0 ) {
            startCell = data.get( startCell.getCoordinate().getRow() - 1 ).get( col );
        }

        //Ensure endCell is at the bottom of a merged cell
        Coordinate nc = findMergedCellExtent( endCell.getCoordinate() );
        endCell = data.get( nc );

        //Select range
        for ( int iRow = startCell.getCoordinate().getRow(); iRow <= endCell.getCoordinate().getRow(); iRow++ ) {
            CellValue< ? > cell = data.get( iRow ).get( col );
            selections.add( cell );

            // Redraw selected cell
            cell.addState( CellState.SELECTED );
            selectCell( cell );
        }

        //Set extent of selected range according to the direction of selection
        switch ( rangeDirection ) {
            case DOWN :
                this.rangeExtentCell = this.selections.last();
                break;
            case UP :
                this.rangeExtentCell = this.selections.first();
                break;
        }
    }

    /**
     * Show a column
     */
    abstract void showColumn(int index);

    /**
     * Select a single cell. If the cell is merged the selection is extended to
     * include all merged cells.
     * 
     * @param start
     *            The physical coordinate of the cell
     */
    void startSelecting(Coordinate start) {
        if ( start == null ) {
            throw new IllegalArgumentException( "start cannot be null" );
        }

        //Raise event signalling change in selection 
        CellSelectionDetail ce = getSelectedCellExtents( data.get( start ) );
        SelectedCellChangeEvent.fire( this,
                                      ce );

        clearSelection();
        CellValue< ? > startCell = data.get( start );
        selectRange( startCell,
                     startCell );
        rangeOriginCell = startCell;
        rangeExtentCell = null;
    }

}

