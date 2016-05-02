<<<<<<< HEAD
/*
 * Copyright (c) 2012, Metron, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Metron, Inc. nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL METRON, INC. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.metsci.glimpse.plot.timeline.event;

import static com.metsci.glimpse.plot.timeline.data.EventSelection.Location.Center;
import static com.metsci.glimpse.plot.timeline.data.EventSelection.Location.End;
import static com.metsci.glimpse.plot.timeline.data.EventSelection.Location.Icon;
import static com.metsci.glimpse.plot.timeline.data.EventSelection.Location.Label;
import static com.metsci.glimpse.plot.timeline.data.EventSelection.Location.Start;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.plot.timeline.data.EventSelection;
import com.metsci.glimpse.plot.timeline.data.EventSelection.Location;
import com.metsci.glimpse.util.units.time.TimeStamp;

/**
 * Helper class which maintains sorted Event data structures for {@code EventPlotInfo}.
 * 
 * @author ulman
 */
public class EventManager
{
    protected static final double BUFFER_MULTIPLIER = 2;
    protected static final double OVERLAP_HEURISTIC = 20.0;
    protected static final int PICK_BUFFER_PIXELS = 10;

    protected EventPlotInfo info;
    protected ReentrantLock lock;

    protected Map<Object, Event> eventMap;
    protected Map<Object, Row> rowMap;
    protected List<Row> rows;

    protected boolean aggregateNearbyEvents = false;
    protected int maxAggregateSize = 30;
    protected int maxAggregateGap = 5;

    protected boolean shouldStack = true;
    protected boolean isHorizontal = true;

    protected boolean visibleEventsDirty = true;
    protected double prevMin;
    protected double prevMax;

    protected class Row
    {
        int index;

        // all Events in the Row
        EventIntervalQuadTree events;

        // all visible Events in the Row (some Events may be aggregated)
        // will not be filled in if aggregation is not turned on (in that
        // case it is unneeded because the events map can be queried instead)
        EventIntervalQuadTree visibleAggregateEvents;

        // all visible Events (including aggregated events, if turned on)
        // sorted by starting timestamp
        List<Event> visibleEvents;

        public Row( int index )
        {
            this.index = index;
            this.visibleAggregateEvents = new EventIntervalQuadTree( );
            this.events = new EventIntervalQuadTree( );
        }

        public void addEvent( Event event )
        {
            this.events.add( event );
            rowMap.put( event.getId( ), this );
        }

        public void removeEvent( Event event )
        {
            this.events.remove( event );
            rowMap.remove( event.getId( ) );
        }

        public void calculateVisibleEvents( Axis1D axis, TimeStamp min, TimeStamp max )
        {
            if ( aggregateNearbyEvents )
            {
                calculateVisibleEventsAggregated( axis, min, max );
            }
            else
            {
                calculateVisibleEventsNormal( min, max );
            }
        }

        public void calculateVisibleEventsAggregated( Axis1D axis, TimeStamp min, TimeStamp max )
        {
            // calculate size of bin in system (time) units
            double ppv = axis.getPixelsPerValue( );
            double maxDuration = maxAggregateSize / ppv;
            double maxGap = maxAggregateGap / ppv;

            // expand the visible window slightly
            // since we only aggregate visible Events, we don't want weird
            // visual artifacts (aggregate groups appearing and disappearing)
            // as Events scroll off the screen
            TimeStamp expandedMin = min.subtract( maxDuration * BUFFER_MULTIPLIER );
            TimeStamp expandedMax = max.add( maxDuration * BUFFER_MULTIPLIER );

            List<Event> visible = calculateVisibleEventsNormal0( events, expandedMin, expandedMax );

            EventIntervalQuadTree events = new EventIntervalQuadTree( );

            Set<Event> children = new HashSet<Event>( );
            TimeStamp childrenMin = null;
            TimeStamp childrenMax = null;
            for ( Event event : visible )
            {
                // only aggregate small events
                boolean isDurationSmall = event.getDuration( ) < maxDuration;

                // only aggregate events with small gaps between them
                double gap = childrenMax == null ? 0 : childrenMax.durationBefore( event.getStartTime( ) );
                boolean isGapSmall = gap < maxGap;

                // if the gap is large, end the current aggregate group
                if ( !isGapSmall )
                {
                    addAggregateEvent( events, children, childrenMin, childrenMax, min, max );
                    children.clear( );
                    childrenMin = null;
                    childrenMax = null;
                }

                // if the event is small enough to be aggregated, add it to the child list
                if ( isDurationSmall )
                {
                    children.add( event );

                    // events are in start time order, so this will never change after being set
                    if ( childrenMin == null ) childrenMin = event.getStartTime( );

                    if ( childrenMax == null || childrenMax.isBefore( event.getEndTime( ) ) ) childrenMax = event.getEndTime( );
                }
                // otherwise just add it to the result map
                else
                {
                    if ( isVisible( event, min, max ) ) events.add( event );
                }
            }

            // add any remaining child events
            addAggregateEvent( events, children, childrenMin, childrenMax, min, max );

            this.visibleAggregateEvents = events;
            this.visibleEvents = calculateVisibleEventsNormal0( events.getAll( ) );
        }

        protected void addAggregateEvent( EventIntervalQuadTree events, Set<Event> children, TimeStamp childrenMin, TimeStamp childrenMax, TimeStamp min, TimeStamp max )
        {
            // if there is only one or zero events in the current group, just add a regular event
            if ( children.size( ) <= 1 )
            {
                for ( Event child : children )
                    if ( isVisible( child, min, max ) ) events.add( child );
            }
            // otherwise create an aggregate group and add it to the result map
            else
            {
                AggregateEvent aggregate = new AggregateEvent( children, childrenMin, childrenMax );

                if ( isVisible( aggregate, min, max ) ) events.add( aggregate );
            }
        }

        protected boolean isVisible( Event event, TimeStamp min, TimeStamp max )
        {
            return ! ( event.getEndTime( ).isBefore( min ) || event.getStartTime( ).isAfter( max ) );
        }

        protected List<Event> calculateVisibleEventsNormal0( EventIntervalQuadTree events, TimeStamp min, TimeStamp max )
        {
            return calculateVisibleEventsNormal0( events.get( min, true, max, true ) );
        }

        protected List<Event> calculateVisibleEventsNormal0( Collection<Event> visible )
        {
            ArrayList<Event> visible_start_sorted = new ArrayList<Event>( visible.size( ) );
            visible_start_sorted.addAll( visible );
            Collections.sort( visible_start_sorted, Event.getStartTimeComparator( ) );
            return visible_start_sorted;
        }

        public void calculateVisibleEventsNormal( TimeStamp min, TimeStamp max )
        {
            this.visibleEvents = calculateVisibleEventsNormal0( this.events, min, max );
        }

        public Collection<Event> getOverlappingEvents( Event event )
        {
            return this.events.get( event.getStartTime( ), false, event.getEndTime( ), false );
        }

        public Collection<Event> getNearestVisibleEvents( TimeStamp timeStart, TimeStamp timeEnd )
        {
            if ( aggregateNearbyEvents )
            {
                return this.visibleAggregateEvents.get( timeStart, timeEnd );
            }
            else
            {
                return this.events.get( timeStart, timeEnd );
            }
        }

        public boolean isEmpty( )
        {
            return this.events.isEmpty( );
        }

        public int size( )
        {
            return this.events.size( );
        }

        public int getIndex( )
        {
            return this.index;
        }

        public void setIndex( int index )
        {
            this.index = index;
        }
    }

    public EventManager( EventPlotInfo info )
    {
        this.info = info;

        this.lock = info.getStackedPlot( ).getLock( );

        this.rows = new ArrayList<Row>( );
        this.eventMap = new HashMap<Object, Event>( );
        this.rowMap = new HashMap<Object, Row>( );

        this.isHorizontal = info.getStackedTimePlot( ).isTimeAxisHorizontal( );
    }

    public void lock( )
    {
        this.lock.lock( );
    }

    public void unlock( )
    {
        this.lock.unlock( );
    }

    public List<Row> getRows( )
    {
        return Collections.unmodifiableList( rows );
    }

    /**
     * @see #setStackOverlappingEvents(boolean)
     */
    public boolean isStackOverlappingEvents( )
    {
        return this.shouldStack;
    }

    /**
     * If true, Events will be automatically placed into rows in order to
     * avoid overlap. Any row requested by {@link Event#setFixedRow(int)} will
     * be ignored.
     */
    public void setStackOverlappingEvents( boolean stack )
    {
        this.shouldStack = stack;
        this.validate( );
    }

    /**
     * @see #setMaxAggregatedGroupSize(int)
     */
    public int getMaxAggregatedGroupSize( )
    {
        return this.maxAggregateSize;
    }

    /**
     * Sets the maximum pixel size above which an Event will not be aggregated
     * with nearby Events (in order to reduce visual clutter).
     * 
     * @see #setAggregateNearbyEvents(boolean)
     */
    public void setMaxAggregatedGroupSize( int size )
    {
        this.maxAggregateSize = size;
        this.validate( );
    }

    public int getMaxAggregatedEventGapSize( )
    {
        return this.maxAggregateGap;
    }

    /**
     * Sets the maximum pixel distance between adjacent events above which
     * events will not be aggregated into a single Event (in order to reduce
     * visual clutter).
     * 
     * @param size
     */
    public void setMaxAggregatedEventGapSize( int size )
    {
        this.maxAggregateGap = size;
        this.validate( );
    }

    /**
     * @see #setAggregateNearbyEvents(boolean)
     */
    public boolean isAggregateNearbyEvents( )
    {
        return this.aggregateNearbyEvents;
    }

    /**
     * If true, nearby events in the same row will be combined into one
     * event to reduce visual clutter.
     */
    public void setAggregateNearbyEvents( boolean aggregate )
    {
        this.aggregateNearbyEvents = aggregate;
        this.validate( );
    }

    public void validate( )
    {
        lock.lock( );
        try
        {
            this.rebuildRows0( );
            this.visibleEventsDirty = true;
            this.info.updateSize( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public int getRowCount( )
    {
        lock.lock( );
        try
        {
            return Math.max( 1, this.rows.size( ) );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void setRow( Object eventId, int rowIndex )
    {
        lock.lock( );
        try
        {
            Event event = getEvent( eventId );
            if ( event == null ) return;

            int oldRowIndex = getRow( eventId );
            Row oldRow = rows.get( oldRowIndex );
            if ( oldRow != null ) oldRow.removeEvent( event );

            ensureRows0( rowIndex );
            Row newRow = rows.get( rowIndex );
            newRow.addEvent( event );

            // row was set manually so don't automatically
            // adjust the other rows to avoid overlap

            this.visibleEventsDirty = true;
            this.info.updateSize( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public int getRow( Object eventId )
    {
        lock.lock( );
        try
        {
            Row row = rowMap.get( eventId );
            if ( row != null )
            {
                return row.getIndex( );
            }
            else
            {
                return 0;
            }
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void addEvent( Event event )
    {
        if ( event == null ) return;

        lock.lock( );
        try
        {
            // remove the event if it already exists
            this.removeEvent( event.getId( ) );

            this.eventMap.put( event.getId( ), event );
            this.addEvent0( event );
            this.visibleEventsDirty = true;
            this.info.updateSize( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public Event removeEvent( Object id )
    {
        lock.lock( );
        try
        {
            Event event = this.eventMap.remove( id );

            if ( event != null )
            {
                this.removeEvent0( event );
                this.visibleEventsDirty = true;
                this.info.updateSize( );
            }

            return event;
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void removeAllEvents( )
    {
        lock.lock( );
        try
        {
            for ( Event event : this.eventMap.values( ) )
            {
                event.setEventPlotInfo( null );
            }

            this.eventMap.clear( );
            this.rowMap.clear( );
            this.rows.clear( );

            this.visibleEventsDirty = true;
            this.info.updateSize( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void moveEvent( Event event, TimeStamp newStartTime, TimeStamp newEndTime )
    {
        lock.lock( );
        try
        {

            Event eventOld = Event.createDummyEvent( event );

            Row oldRow = rowMap.get( event.getId( ) );
            if ( oldRow == null ) return;

            if ( event.isFixedRow( ) )
            {
                // update the event times (its row will stay the same)
                // remove and add it to update start/end time indexes
                oldRow.removeEvent( event );
                event.setTimes0( newStartTime, newEndTime );
                oldRow.addEvent( event );

                // displace the events this event has shifted on to
                displaceEvents0( oldRow, event );
            }
            else
            {
                // remove the event from its old row
                oldRow.removeEvent( event );

                // update the event times
                event.setTimes0( newStartTime, newEndTime );

                // add the moved version of the event back in
                // (which might land it on a different row if it
                //  has been moved over top of another event)
                addEvent0( event );
            }

            // now shift events to fill the space left by moving the event
            shiftEvents0( eventOld, oldRow );
            clearEmptyRows0( );

            this.visibleEventsDirty = true;
            this.info.updateSize( );

        }
        finally
        {
            lock.unlock( );
        }
    }

    public Set<Event> getEvents( )
    {
        lock.lock( );
        try
        {
            return Collections.unmodifiableSet( new HashSet<Event>( this.eventMap.values( ) ) );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public Event getEvent( Object id )
    {
        lock.lock( );
        try
        {
            return this.eventMap.get( id );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public Set<EventSelection> getNearestEvents( GlimpseMouseEvent e )
    {
        lock.lock( );
        try
        {
            Row row = getNearestRow( e );

            if ( row != null )
            {
                Axis1D axis = e.getAxis1D( );
                double value = isHorizontal ? e.getAxisCoordinatesX( ) : e.getAxisCoordinatesY( );
                double buffer = PICK_BUFFER_PIXELS / axis.getPixelsPerValue( );

                Epoch epoch = info.getStackedTimePlot( ).getEpoch( );

                TimeStamp time = epoch.toTimeStamp( value );
                TimeStamp timeStart = epoch.toTimeStamp( value - buffer );
                TimeStamp timeEnd = epoch.toTimeStamp( value + buffer );

                Collection<Event> events = row.getNearestVisibleEvents( timeStart, timeEnd );
                Set<EventSelection> eventSelections = createEventSelection( axis, events, time );
                return eventSelections;
            }

            return Collections.emptySet( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    // find the event which minimizes: abs(clickPos-eventEnd)+abs(clickPos-eventStart)
    // this is a heuristic for the single event "closest" to the click position
    // we don't want to require that the click be inside the event because we want
    // to make selection of instantaneous events possible
    // (but if the click *is* inside an event, it gets priority)
    public EventSelection getNearestEvent( Set<EventSelection> events, GlimpseMouseEvent e )
    {
        lock.lock( );
        try
        {
            Epoch epoch = info.getStackedTimePlot( ).getEpoch( );
            double value = isHorizontal ? e.getAxisCoordinatesX( ) : e.getAxisCoordinatesY( );
            TimeStamp time = epoch.toTimeStamp( value );

            double bestDist = Double.MAX_VALUE;
            EventSelection bestEvent = null;

            for ( EventSelection s : events )
            {
                Event event = s.getEvent( );

                if ( event.contains( time ) )
                {
                    return s;
                }
                else
                {
                    double dist = distance0( event, time );
                    if ( bestEvent == null || dist < bestDist )
                    {
                        bestDist = dist;
                        bestEvent = s;
                    }
                }
            }

            return bestEvent;
        }
        finally
        {
            lock.unlock( );
        }
    }

    public EventSelection getNearestEvent( GlimpseMouseEvent e )
    {
        return getNearestEvent( getNearestEvents( e ), e );
    }

    // heuristic distance measure for use in getNearestEvent( )
    protected double distance0( Event event, TimeStamp time )
    {
        double startDiff = Math.abs( time.durationAfter( event.getStartTime( ) ) );
        double endDiff = Math.abs( time.durationAfter( event.getEndTime( ) ) );
        return Math.min( startDiff, endDiff );
    }

    // must be called while holding lock
    protected Row getNearestRow( GlimpseMouseEvent e )
    {
        int value = isHorizontal ? e.getY( ) : e.getTargetStack( ).getBounds( ).getWidth( ) - e.getX( );

        int rowIndex = ( int ) Math.floor( value / ( double ) ( info.getRowSize( ) + info.getEventPadding( ) ) );
        rowIndex = info.getRowCount( ) - 1 - rowIndex;

        if ( rowIndex >= 0 && rowIndex < rows.size( ) )
        {
            return rows.get( rowIndex );
        }

        return null;
    }

    public void calculateVisibleEvents( Axis1D axis )
    {
        lock.lock( );
        try
        {
            if ( visibleEventsDirty || axis.getMin( ) != prevMin || axis.getMax( ) != prevMax )
            {
                calculateVisibleEvents( axis.getMin( ), axis.getMax( ) );
            }
        }
        finally
        {
            lock.unlock( );
        }
    }

    // must be called while holding lock
    private Set<EventSelection> createEventSelection( Axis1D axis, Collection<Event> events, TimeStamp clickTime )
    {
        Set<EventSelection> set = new HashSet<EventSelection>( );

        for ( Event event : events )
        {
            set.add( createEventSelection( axis, event, clickTime ) );
        }

        return set;
    }

    // must be called while holding lock
    private EventSelection createEventSelection( Axis1D axis, Event event, TimeStamp t )
    {
        double buffer = PICK_BUFFER_PIXELS / axis.getPixelsPerValue( );

        TimeStamp t1 = t.subtract( buffer );
        TimeStamp t2 = t.add( buffer );

        TimeStamp e1 = event.getStartTime( );
        TimeStamp e2 = event.getEndTime( );

        EnumSet<Location> locations = EnumSet.noneOf( Location.class );

        boolean start = t2.isAfterOrEquals( e1 ) && t1.isBeforeOrEquals( e1 );
        boolean end = t2.isAfterOrEquals( e2 ) && t1.isBeforeOrEquals( e2 );

        TimeStamp i1 = event.getIconStartTime( );
        TimeStamp i2 = event.getIconEndTime( );
        boolean icon = event.isIconVisible( ) && i1 != null && i2 != null && t.isAfterOrEquals( i1 ) && t.isBeforeOrEquals( i2 );

        TimeStamp l1 = event.getLabelStartTime( );
        TimeStamp l2 = event.getLabelEndTime( );
        boolean text = event.isLabelVisible( ) && l1 != null && l2 != null && t.isAfterOrEquals( l1 ) && t.isBeforeOrEquals( l2 );

        if ( text ) locations.add( Label );
        if ( icon ) locations.add( Icon );
        if ( start ) locations.add( Start );
        if ( end ) locations.add( End );
        if ( ( !start && !end ) || ( start && end ) ) locations.add( Center );

        return new EventSelection( event, locations );
    }

    // must be called while holding lock
    private void rebuildRows0( )
    {
        rows.clear( );
        rowMap.clear( );

        for ( Event event : eventMap.values( ) )
        {
            addEvent0( event );
        }
    }

    // must be called while holding lock
    private void ensureRows0( int requestedIndex )
    {
        int currentRowCount = rows.size( );
        while ( requestedIndex >= currentRowCount )
        {
            rows.add( new Row( currentRowCount++ ) );
        }
    }

    // must be called while holding lock
    private void displaceEvents0( Row row, Event event )
    {
        Set<Event> overlapEvents = new HashSet<Event>( row.getOverlappingEvents( event ) );
        for ( Event overlapEvent : overlapEvents )
        {
            displaceEvent0( overlapEvent );
        }
    }

    // must be called while holding lock
    // move an event which has been overlapped by another event
    private void displaceEvent0( Event oldEvent )
    {
        if ( !oldEvent.isFixedRow( ) )
        {
            Row oldRow = rowMap.get( oldEvent.getId( ) );
            oldRow.removeEvent( oldEvent );
            addEvent0( oldEvent );
            shiftEvents0( oldEvent, oldRow );
        }
        else
        {
            // if the displaced event requested the row it is in, don't move it
        }
    }

    // must be called while holding lock
    private void clearEmptyRows0( )
    {
        // clear empty rows until we find a non-empty one
        for ( int i = rows.size( ) - 1; i >= 0; i-- )
        {
            if ( rows.get( i ).isEmpty( ) )
            {
                rows.remove( i );
            }
            else
            {
                break;
            }
        }
    }

    // must be called while holding lock
    private void removeEvent0( Event event )
    {
        // remove the event then determine if other events should be
        // shifted down to fill its place
        eventMap.remove( event.getId( ) );

        Row row = rowMap.remove( event.getId( ) );
        if ( row == null ) return;
        row.removeEvent( event );

        shiftEvents0( event, row );
        clearEmptyRows0( );
    }

    // must be called while holding lock
    private void shiftEvents0( Event event, Row toRow )
    {
        // determine if the removal of this event allows others to shift down
        int size = rows.size( );
        for ( int i = size - 1; i > toRow.index; i-- )
        {
            Row fromRow = rows.get( i );

            // check to see if any of these candidates can be moved down to
            // fill the spot in toRow left by the deleted event
            HashSet<Event> events = new HashSet<Event>( fromRow.getOverlappingEvents( event ) );
            for ( Event e : events )
                moveEventIfRoom0( e, fromRow, toRow );
        }
    }

    // must be called while holding lock
    private void moveEventIfRoom0( Event event, Row fromRow, Row toRow )
    {
        // move the event if there is room for it and it hasn't explicitly requested its current row
        if ( !event.isFixedRow( ) && toRow.getOverlappingEvents( event ).isEmpty( ) )
        {
            fromRow.removeEvent( event );
            toRow.addEvent( event );
            shiftEvents0( event, fromRow );
        }
    }

    // must be called while holding lock
    private Row addEvent0( Event event )
    {
        Row row = null;
        if ( shouldStack && !event.isFixedRow( ) )
        {
            row = getRowWithLeastOverlaps( event );

            // put the event into the non-overlapping spot we've found for it
            row.addEvent( event );
        }
        else
        {
            // the requested row index must be less than the maximum row count and greater than or equal to 0
            int requestedRow = Math.min( Math.max( 0, event.getFixedRow( ) ), info.getRowMaxCount( ) - 1 );
            ensureRows0( requestedRow );
            row = rows.get( requestedRow );

            row.addEvent( event );

            // this spot might overlap with other events, move them out of the way
            if ( shouldStack )
            {
                displaceEvents0( row, event );
            }
        }

        return row;
    }

    // must be called while holding lock
    //
    // If plot.getMaxRowCount() is large, we'll always be able to simply
    // make a new row (which will have no overlaps. If we're constrained
    // regarding the number of rows we can create, we may have to accept
    // some overlaps.
    private Row getRowWithLeastOverlaps( Event event )
    {
        int size = rows.size( );
        int max = info.getRowMaxCount( );

        double leastTime = Double.POSITIVE_INFINITY;
        Row leastRow = null;

        for ( int i = 0; i < size; i++ )
        {
            Row candidate = rows.get( i );

            double overlapTime = getTotalOverlapTime( candidate, event );

            if ( overlapTime < leastTime )
            {
                leastTime = overlapTime;
                leastRow = candidate;
            }
        }

        // if we didn't find an empty row, and there's room to make
        // a new row, then make a new row, which will have 0 overlap
        if ( leastTime != 0.0 && size < max )
        {
            leastTime = 0;
            leastRow = new Row( size );
            rows.add( leastRow );
        }

        return leastRow;
    }

    // must be called while holding lock
    private double getTotalOverlapTime( Row candidate, Event event )
    {
        double totalOverlap = 0;

        //XXX Heuristic: we want overlaps with very small events (in the
        // limit we have 0 duration events) to count for something, so
        // we make the minimum time penalty for any overlap be 1/20th
        // of the total duration of either event
        double minOverlap1 = event.getDuration( ) / OVERLAP_HEURISTIC;

        Collection<Event> events = candidate.getOverlappingEvents( event );
        for ( Event overlapEvent : events )
        {
            double minOverlap = Math.max( minOverlap1, overlapEvent.getDuration( ) / OVERLAP_HEURISTIC );
            double overlap = event.getOverlapTime( overlapEvent );

            totalOverlap += Math.max( minOverlap, overlap );
        }

        return totalOverlap;
    }

    // must be called while holding lock
    private void calculateVisibleEvents( double min, double max )
    {
        Epoch epoch = info.getStackedTimePlot( ).getEpoch( );
        TaggedAxis1D axis = info.getStackedTimePlot( ).getTimeAxis( );

        for ( Row row : rows )
        {
            row.calculateVisibleEvents( axis, epoch.toTimeStamp( min ), epoch.toTimeStamp( max ) );
        }

        this.visibleEventsDirty = false;
    }
}

=======
package redis.clients.jedis;

import static redis.clients.jedis.Protocol.toByteArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.util.SafeEncoder;

public class Pipeline extends Queable {
	
    private MultiResponseBuilder currentMulti;
    
    private class MultiResponseBuilder extends Builder<List<Object>>{
    	private List<Response<?>> responses = new ArrayList<Response<?>>();
		
		@Override
		public List<Object> build(Object data) {
			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>)data;
			List<Object> values = new ArrayList<Object>();
			
			if(list.size() != responses.size()){
				throw new JedisDataException("Expected data size " + responses.size() + " but was " + list.size());
			}
			
			for(int i=0;i<list.size();i++){
				Response<?> response = responses.get(i);
				response.set(list.get(i));
				values.add(response.get());
			}
			return values;
		}

		public void addResponse(Response<?> response){
			responses.add(response);
		}
    }

    @Override
    protected <T> Response<T> getResponse(Builder<T> builder) {
    	if(currentMulti != null){
    		super.getResponse(BuilderFactory.STRING); //Expected QUEUED
    		
    		Response<T> lr = new Response<T>(builder);
    		currentMulti.addResponse(lr);
    		return lr;
    	}
    	else{
    		return super.getResponse(builder);
    	}
    }
	
    private Client client;
    
    public void setClient(Client client) {
        this.client = client;
    }

    /**
     * Syncronize pipeline by reading all responses. This operation close the
     * pipeline. In order to get return values from pipelined commands, capture
     * the different Response<?> of the commands you execute.
     */
    public void sync() {
        List<Object> unformatted = client.getAll();
        for (Object o : unformatted) {
            generateResponse(o);
        }
    }

    /**
     * Syncronize pipeline by reading all responses. This operation close the
     * pipeline. Whenever possible try to avoid using this version and use
     * Pipeline.sync() as it won't go through all the responses and generate the
     * right response type (usually it is a waste of time).
     * 
     * @return A list of all the responses in the order you executed them.
     * @see sync
     */
    public List<Object> syncAndReturnAll() {
        List<Object> unformatted = client.getAll();
        List<Object> formatted = new ArrayList<Object>();
        
        for (Object o : unformatted) {
            try {
            	formatted.add(generateResponse(o).get());
            } catch (JedisDataException e) {
                formatted.add(e);
            }
        }
        return formatted;
    }

    public Response<Long> append(String key, String value) {
        client.append(key, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> append(byte[] key, byte[] value) {
        client.append(key, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<List<String>> blpop(String... args) {
        client.blpop(args);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<List<String>> blpop(byte[]... args) {
        client.blpop(args);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<List<String>> brpop(String... args) {
        client.brpop(args);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<List<String>> brpop(byte[]... args) {
        client.brpop(args);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<Long> decr(String key) {
        client.decr(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> decr(byte[] key) {
        client.decr(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> decrBy(String key, long integer) {
        client.decrBy(key, integer);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> decrBy(byte[] key, long integer) {
        client.decrBy(key, integer);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> del(String... keys) {
        client.del(keys);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> del(byte[]... keys) {
        client.del(keys);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> echo(String string) {
        client.echo(string);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> echo(byte[] string) {
        client.echo(string);
        return getResponse(BuilderFactory.STRING);
    }
    
    /**
     * Submits a script for pipelined execution
     * @param script The text of the lua script to execute
     * @param keyCount The numbers of keys submitted
     * @param params The keys and/or arguments submitted to the script
     * @return a response containing the return value[s] of the script in a list
     */
    public Response<List<Object>> eval(String script, int keyCount, String... params) {
        client.eval(script, keyCount, params);       
        return new Response<List<Object>>(new MultiResponseBuilder());
    }
   
    public Object eval(String script) {
        client.eval(script, 0);       
        return getEvalResult();
    }
   
    public Object eval(String script, List<String> keys, List<String> args) {
        return eval(script, keys.size(), getParams(keys, args));
    }
   
    public Object evalsha(String sha1, int keyCount, String... params) {
        client.evalsha(sha1, keyCount, params);       
        return getEvalResult();
    }
   
    public Object evalsha(String sha1) {
        client.evalsha(sha1, 0);       
        return getEvalResult();
    }
   
    public Object evalsha(String sha1, List<String> keys, List<String> args) {
        return evalsha(sha1, keys.size(), getParams(keys, args));
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

    public Response<Boolean> exists(String key) {
        client.exists(key);
        return getResponse(BuilderFactory.BOOLEAN);
    }

    public Response<Boolean> exists(byte[] key) {
        client.exists(key);
        return getResponse(BuilderFactory.BOOLEAN);
    }

    public Response<Long> expire(String key, int seconds) {
        client.expire(key, seconds);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> expire(byte[] key, int seconds) {
        client.expire(key, seconds);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> expireAt(String key, long unixTime) {
        client.expireAt(key, unixTime);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> expireAt(byte[] key, long unixTime) {
        client.expireAt(key, unixTime);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> get(String key) {
        client.get(key);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<byte[]> get(byte[] key) {
        client.get(key);
        return getResponse(BuilderFactory.BYTE_ARRAY);
    }

    public Response<Boolean> getbit(String key, long offset) {
        client.getbit(key, offset);
        return getResponse(BuilderFactory.BOOLEAN);
    }

    public Response<String> getrange(String key, long startOffset,
            long endOffset) {
        client.getrange(key, startOffset, endOffset);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> getSet(String key, String value) {
        client.getSet(key, value);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<byte[]> getSet(byte[] key, byte[] value) {
        client.getSet(key, value);
        return getResponse(BuilderFactory.BYTE_ARRAY);
    }

    public Response<Long> hdel(String key, String field) {
        client.hdel(key, field);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> hdel(byte[] key, byte[] field) {
        client.hdel(key, field);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Boolean> hexists(String key, String field) {
        client.hexists(key, field);
        return getResponse(BuilderFactory.BOOLEAN);
    }

    public Response<Boolean> hexists(byte[] key, byte[] field) {
        client.hexists(key, field);
        return getResponse(BuilderFactory.BOOLEAN);
    }

    public Response<String> hget(String key, String field) {
        client.hget(key, field);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> hget(byte[] key, byte[] field) {
        client.hget(key, field);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Map<String, String>> hgetAll(String key) {
        client.hgetAll(key);
        return getResponse(BuilderFactory.STRING_MAP);
    }

    public Response<Map<String, String>> hgetAll(byte[] key) {
        client.hgetAll(key);
        return getResponse(BuilderFactory.STRING_MAP);
    }

    public Response<Long> hincrBy(String key, String field, long value) {
        client.hincrBy(key, field, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> hincrBy(byte[] key, byte[] field, long value) {
        client.hincrBy(key, field, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Set<String>> hkeys(String key) {
        client.hkeys(key);
        return getResponse(BuilderFactory.STRING_SET);
    }

    public Response<Set<String>> hkeys(byte[] key) {
        client.hkeys(key);
        return getResponse(BuilderFactory.STRING_SET);
    }

    public Response<Long> hlen(String key) {
        client.hlen(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> hlen(byte[] key) {
        client.hlen(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<List<String>> hmget(String key, String... fields) {
        client.hmget(key, fields);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<List<String>> hmget(byte[] key, byte[]... fields) {
        client.hmget(key, fields);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<String> hmset(String key, Map<String, String> hash) {
        client.hmset(key, hash);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> hmset(byte[] key, Map<byte[], byte[]> hash) {
        client.hmset(key, hash);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Long> hset(String key, String field, String value) {
        client.hset(key, field, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> hset(byte[] key, byte[] field, byte[] value) {
        client.hset(key, field, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> hsetnx(String key, String field, String value) {
        client.hsetnx(key, field, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> hsetnx(byte[] key, byte[] field, byte[] value) {
        client.hsetnx(key, field, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<List<String>> hvals(String key) {
        client.hvals(key);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<List<String>> hvals(byte[] key) {
        client.hvals(key);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<Long> incr(String key) {
        client.incr(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> incr(byte[] key) {
        client.incr(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> incrBy(String key, long integer) {
        client.incrBy(key, integer);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> incrBy(byte[] key, long integer) {
        client.incrBy(key, integer);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Set<String>> keys(String pattern) {
        client.keys(pattern);
        return getResponse(BuilderFactory.STRING_SET);
    }

    public Response<Set<String>> keys(byte[] pattern) {
        client.keys(pattern);
        return getResponse(BuilderFactory.STRING_SET);
    }

    public Response<String> lindex(String key, int index) {
        client.lindex(key, index);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> lindex(byte[] key, int index) {
        client.lindex(key, index);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Long> linsert(String key, LIST_POSITION where,
            String pivot, String value) {
        client.linsert(key, where, pivot, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> linsert(byte[] key, LIST_POSITION where,
            byte[] pivot, byte[] value) {
        client.linsert(key, where, pivot, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> llen(String key) {
        client.llen(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> llen(byte[] key) {
        client.llen(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> lpop(String key) {
        client.lpop(key);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> lpop(byte[] key) {
        client.lpop(key);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Long> lpush(String key, String string) {
        client.lpush(key, string);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> lpush(byte[] key, byte[] string) {
        client.lpush(key, string);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> lpushx(String key, String string) {
        client.lpushx(key, string);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> lpushx(byte[] key, byte[] bytes) {
        client.lpushx(key, bytes);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<List<String>> lrange(String key, long start, long end) {
        client.lrange(key, start, end);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<List<String>> lrange(byte[] key, long start, long end) {
        client.lrange(key, start, end);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<Long> lrem(String key, long count, String value) {
        client.lrem(key, count, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> lrem(byte[] key, long count, byte[] value) {
        client.lrem(key, count, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> lset(String key, long index, String value) {
        client.lset(key, index, value);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> lset(byte[] key, long index, byte[] value) {
        client.lset(key, index, value);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> ltrim(String key, long start, long end) {
        client.ltrim(key, start, end);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> ltrim(byte[] key, long start, long end) {
        client.ltrim(key, start, end);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<List<String>> mget(String... keys) {
        client.mget(keys);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<List<String>> mget(byte[]... keys) {
        client.mget(keys);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<Long> move(String key, int dbIndex) {
        client.move(key, dbIndex);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> move(byte[] key, int dbIndex) {
        client.move(key, dbIndex);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> mset(String... keysvalues) {
        client.mset(keysvalues);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> mset(byte[]... keysvalues) {
        client.mset(keysvalues);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Long> msetnx(String... keysvalues) {
        client.msetnx(keysvalues);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> msetnx(byte[]... keysvalues) {
        client.msetnx(keysvalues);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> persist(String key) {
        client.persist(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> persist(byte[] key) {
        client.persist(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> rename(String oldkey, String newkey) {
        client.rename(oldkey, newkey);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> rename(byte[] oldkey, byte[] newkey) {
        client.rename(oldkey, newkey);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Long> renamenx(String oldkey, String newkey) {
        client.renamenx(oldkey, newkey);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> renamenx(byte[] oldkey, byte[] newkey) {
        client.renamenx(oldkey, newkey);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> rpop(String key) {
        client.rpop(key);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> rpop(byte[] key) {
        client.rpop(key);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> rpoplpush(String srckey, String dstkey) {
        client.rpoplpush(srckey, dstkey);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> rpoplpush(byte[] srckey, byte[] dstkey) {
        client.rpoplpush(srckey, dstkey);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Long> rpush(String key, String string) {
        client.rpush(key, string);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> rpush(byte[] key, byte[] string) {
        client.rpush(key, string);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> rpushx(String key, String string) {
        client.rpushx(key, string);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> rpushx(byte[] key, byte[] string) {
        client.rpushx(key, string);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> sadd(String key, String member) {
        client.sadd(key, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> sadd(byte[] key, byte[] member) {
        client.sadd(key, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> scard(String key) {
        client.scard(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> scard(byte[] key) {
        client.scard(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Set<String>> sdiff(String... keys) {
        client.sdiff(keys);
        return getResponse(BuilderFactory.STRING_SET);
    }

    public Response<Set<String>> sdiff(byte[]... keys) {
        client.sdiff(keys);
        return getResponse(BuilderFactory.STRING_SET);
    }

    public Response<Long> sdiffstore(String dstkey, String... keys) {
        client.sdiffstore(dstkey, keys);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> sdiffstore(byte[] dstkey, byte[]... keys) {
        client.sdiffstore(dstkey, keys);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> set(String key, String value) {
        client.set(key, value);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> set(byte[] key, byte[] value) {
        client.set(key, value);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Boolean> setbit(String key, long offset, boolean value) {
        client.setbit(key, offset, value);
        return getResponse(BuilderFactory.BOOLEAN);
    }

    public Response<String> setex(String key, int seconds, String value) {
        client.setex(key, seconds, value);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> setex(byte[] key, int seconds, byte[] value) {
        client.setex(key, seconds, value);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Long> setnx(String key, String value) {
        client.setnx(key, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> setnx(byte[] key, byte[] value) {
        client.setnx(key, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> setrange(String key, long offset, String value) {
        client.setrange(key, offset, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Set<String>> sinter(String... keys) {
        client.sinter(keys);
        return getResponse(BuilderFactory.STRING_SET);
    }

    public Response<Set<String>> sinter(byte[]... keys) {
        client.sinter(keys);
        return getResponse(BuilderFactory.STRING_SET);
    }

    public Response<Long> sinterstore(String dstkey, String... keys) {
        client.sinterstore(dstkey, keys);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> sinterstore(byte[] dstkey, byte[]... keys) {
        client.sinterstore(dstkey, keys);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Boolean> sismember(String key, String member) {
        client.sismember(key, member);
        return getResponse(BuilderFactory.BOOLEAN);
    }

    public Response<Boolean> sismember(byte[] key, byte[] member) {
        client.sismember(key, member);
        return getResponse(BuilderFactory.BOOLEAN);
    }

    public Response<Set<String>> smembers(String key) {
        client.smembers(key);
        return getResponse(BuilderFactory.STRING_SET);
    }

    public Response<Set<String>> smembers(byte[] key) {
        client.smembers(key);
        return getResponse(BuilderFactory.STRING_SET);
    }

    public Response<Long> smove(String srckey, String dstkey, String member) {
        client.smove(srckey, dstkey, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> smove(byte[] srckey, byte[] dstkey, byte[] member) {
        client.smove(srckey, dstkey, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> sort(String key) {
        client.sort(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> sort(byte[] key) {
        client.sort(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<List<String>> sort(String key,
            SortingParams sortingParameters) {
        client.sort(key, sortingParameters);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<List<String>> sort(byte[] key,
            SortingParams sortingParameters) {
        client.sort(key, sortingParameters);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<List<String>> sort(String key,
            SortingParams sortingParameters, String dstkey) {
        client.sort(key, sortingParameters, dstkey);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<List<String>> sort(byte[] key,
            SortingParams sortingParameters, byte[] dstkey) {
        client.sort(key, sortingParameters, dstkey);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<List<String>> sort(String key, String dstkey) {
        client.sort(key, dstkey);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<List<String>> sort(byte[] key, byte[] dstkey) {
        client.sort(key, dstkey);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<String> spop(String key) {
        client.spop(key);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> spop(byte[] key) {
        client.spop(key);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> srandmember(String key) {
        client.srandmember(key);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> srandmember(byte[] key) {
        client.srandmember(key);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Long> srem(String key, String member) {
        client.srem(key, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> srem(byte[] key, byte[] member) {
        client.srem(key, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> strlen(String key) {
        client.strlen(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> strlen(byte[] key) {
        client.strlen(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> substr(String key, int start, int end) {
        client.substr(key, start, end);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> substr(byte[] key, int start, int end) {
        client.substr(key, start, end);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Set<String>> sunion(String... keys) {
        client.sunion(keys);
        return getResponse(BuilderFactory.STRING_SET);
    }

    public Response<Set<String>> sunion(byte[]... keys) {
        client.sunion(keys);
        return getResponse(BuilderFactory.STRING_SET);
    }

    public Response<Long> sunionstore(String dstkey, String... keys) {
        client.sunionstore(dstkey, keys);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> sunionstore(byte[] dstkey, byte[]... keys) {
        client.sunionstore(dstkey, keys);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> ttl(String key) {
        client.ttl(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> ttl(byte[] key) {
        client.ttl(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> type(String key) {
        client.type(key);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> type(byte[] key) {
        client.type(key);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> watch(String... keys) {
        client.watch(keys);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> watch(byte[]... keys) {
        client.watch(keys);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Long> zadd(String key, double score, String member) {
        client.zadd(key, score, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zadd(byte[] key, double score, byte[] member) {
        client.zadd(key, score, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zcard(String key) {
        client.zcard(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zcard(byte[] key) {
        client.zcard(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zcount(String key, double min, double max) {
        client.zcount(key, min, max);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zcount(byte[] key, double min, double max) {
        client.zcount(key, toByteArray(min), toByteArray(max));
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Double> zincrby(String key, double score, String member) {
        client.zincrby(key, score, member);
        return getResponse(BuilderFactory.DOUBLE);
    }

    public Response<Double> zincrby(byte[] key, double score, byte[] member) {
        client.zincrby(key, score, member);
        return getResponse(BuilderFactory.DOUBLE);
    }

    public Response<Long> zinterstore(String dstkey, String... sets) {
        client.zinterstore(dstkey, sets);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zinterstore(byte[] dstkey, byte[]... sets) {
        client.zinterstore(dstkey, sets);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zinterstore(String dstkey, ZParams params,
            String... sets) {
        client.zinterstore(dstkey, params, sets);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zinterstore(byte[] dstkey, ZParams params,
            byte[]... sets) {
        client.zinterstore(dstkey, params, sets);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Set<String>> zrange(String key, int start, int end) {
        client.zrange(key, start, end);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<String>> zrange(byte[] key, int start, int end) {
        client.zrange(key, start, end);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<String>> zrangeByScore(String key, double min,
            double max) {
        client.zrangeByScore(key, min, max);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<String>> zrangeByScore(byte[] key, double min,
            double max) {
        return zrangeByScore(key, toByteArray(min), toByteArray(max));
    }
    
    public Response<Set<String>> zrangeByScore(String key, String min,
            String max) {
        client.zrangeByScore(key, min, max);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<String>> zrangeByScore(byte[] key, byte[] min,
            byte[] max) {
        client.zrangeByScore(key, min, max);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<String>> zrangeByScore(String key, double min,
            double max, int offset, int count) {
        client.zrangeByScore(key, min, max, offset, count);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<String>> zrangeByScore(byte[] key, double min,
            double max, int offset, int count) {
        return zrangeByScore(key, toByteArray(min), toByteArray(max), offset, count);
    }
    
    public Response<Set<String>> zrangeByScore(byte[] key, byte[] min,
    		byte[] max, int offset, int count) {
        client.zrangeByScore(key, min, max, offset, count);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<Tuple>> zrangeByScoreWithScores(String key, double min,
            double max) {
        client.zrangeByScoreWithScores(key, min, max);
        return getResponse(BuilderFactory.TUPLE_ZSET);
    }

    public Response<Set<Tuple>> zrangeByScoreWithScores(byte[] key, double min,
            double max) {
        return zrangeByScoreWithScores(key, toByteArray(min), toByteArray(max));
    }
    
    public Response<Set<Tuple>> zrangeByScoreWithScores(byte[] key, byte[] min,
    		byte[] max) {
        client.zrangeByScoreWithScores(key, min, max);
        return getResponse(BuilderFactory.TUPLE_ZSET);
    }

    public Response<Set<Tuple>> zrangeByScoreWithScores(String key, double min,
            double max, int offset, int count) {
        client.zrangeByScoreWithScores(key, min, max, offset, count);
        return getResponse(BuilderFactory.TUPLE_ZSET);
    }

    public Response<Set<Tuple>> zrangeByScoreWithScores(byte[] key, double min,
            double max, int offset, int count) {
        client.zrangeByScoreWithScores(key, toByteArray(min), toByteArray(max), offset, count);
        return getResponse(BuilderFactory.TUPLE_ZSET);
    }
    
    public Response<Set<Tuple>> zrangeByScoreWithScores(byte[] key, byte[] min,
    		byte[] max, int offset, int count) {
        client.zrangeByScoreWithScores(key, min, max, offset, count);
        return getResponse(BuilderFactory.TUPLE_ZSET);
    }

    public Response<Set<String>> zrevrangeByScore(String key, double max,
            double min) {
        client.zrevrangeByScore(key, max, min);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<String>> zrevrangeByScore(byte[] key, double max,
            double min) {
        client.zrevrangeByScore(key, toByteArray(max), toByteArray(min));
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<String>> zrevrangeByScore(String key, String max,
            String min) {
        client.zrevrangeByScore(key, max, min);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<String>> zrevrangeByScore(byte[] key, byte[] max,
            byte[] min) {
        client.zrevrangeByScore(key, max, min);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<String>> zrevrangeByScore(String key, double max,
            double min, int offset, int count) {
        client.zrevrangeByScore(key, max, min, offset, count);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<String>> zrevrangeByScore(byte[] key, double max,
            double min, int offset, int count) {
        client.zrevrangeByScore(key, toByteArray(max), toByteArray(min), offset, count);
        return getResponse(BuilderFactory.STRING_ZSET);
    }
    
    public Response<Set<String>> zrevrangeByScore(byte[] key, byte[] max,
    		byte[] min, int offset, int count) {
        client.zrevrangeByScore(key, max, min, offset, count);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<Tuple>> zrevrangeByScoreWithScores(String key,
            double max, double min) {
        client.zrevrangeByScoreWithScores(key, max, min);
        return getResponse(BuilderFactory.TUPLE_ZSET);
    }

    public Response<Set<Tuple>> zrevrangeByScoreWithScores(byte[] key,
            double max, double min) {
        client.zrevrangeByScoreWithScores(key, toByteArray(max), toByteArray(min));
        return getResponse(BuilderFactory.TUPLE_ZSET);
    }
    
    public Response<Set<Tuple>> zrevrangeByScoreWithScores(byte[] key,
    		byte[] max, byte[] min) {
        client.zrevrangeByScoreWithScores(key, max, min);
        return getResponse(BuilderFactory.TUPLE_ZSET);
    }

    public Response<Set<Tuple>> zrevrangeByScoreWithScores(String key,
            double max, double min, int offset, int count) {
        client.zrevrangeByScoreWithScores(key, max, min, offset, count);
        return getResponse(BuilderFactory.TUPLE_ZSET);
    }

    public Response<Set<Tuple>> zrevrangeByScoreWithScores(byte[] key,
            double max, double min, int offset, int count) {
        client.zrevrangeByScoreWithScores(key, toByteArray(max), toByteArray(min), offset, count);
        return getResponse(BuilderFactory.TUPLE_ZSET);
    }
    
    public Response<Set<Tuple>> zrevrangeByScoreWithScores(byte[] key,
    		byte[] max, byte[] min, int offset, int count) {
        client.zrevrangeByScoreWithScores(key, max, min, offset, count);
        return getResponse(BuilderFactory.TUPLE_ZSET);
    }

    public Response<Set<Tuple>> zrangeWithScores(String key, int start, int end) {
        client.zrangeWithScores(key, start, end);
        return getResponse(BuilderFactory.TUPLE_ZSET);
    }

    public Response<Set<Tuple>> zrangeWithScores(byte[] key, int start, int end) {
        client.zrangeWithScores(key, start, end);
        return getResponse(BuilderFactory.TUPLE_ZSET);
    }

    public Response<Long> zrank(String key, String member) {
        client.zrank(key, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zrank(byte[] key, byte[] member) {
        client.zrank(key, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zrem(String key, String member) {
        client.zrem(key, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zrem(byte[] key, byte[] member) {
        client.zrem(key, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zremrangeByRank(String key, int start, int end) {
        client.zremrangeByRank(key, start, end);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zremrangeByRank(byte[] key, int start, int end) {
        client.zremrangeByRank(key, start, end);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zremrangeByScore(String key, double start, double end) {
        client.zremrangeByScore(key, start, end);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zremrangeByScore(byte[] key, double start, double end) {
        client.zremrangeByScore(key, toByteArray(start), toByteArray(end));
        return getResponse(BuilderFactory.LONG);
    }
    
    public Response<Long> zremrangeByScore(byte[] key, byte[] start, byte[] end) {
        client.zremrangeByScore(key, start, end);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Set<String>> zrevrange(String key, int start, int end) {
        client.zrevrange(key, start, end);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<String>> zrevrange(byte[] key, int start, int end) {
        client.zrevrange(key, start, end);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<Tuple>> zrevrangeWithScores(String key, int start,
            int end) {
        client.zrevrangeWithScores(key, start, end);
        return getResponse(BuilderFactory.TUPLE_ZSET);
    }

    public Response<Set<Tuple>> zrevrangeWithScores(byte[] key, int start,
            int end) {
        client.zrevrangeWithScores(key, start, end);
        return getResponse(BuilderFactory.TUPLE_ZSET);
    }

    public Response<Long> zrevrank(String key, String member) {
        client.zrevrank(key, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zrevrank(byte[] key, byte[] member) {
        client.zrevrank(key, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Double> zscore(String key, String member) {
        client.zscore(key, member);
        return getResponse(BuilderFactory.DOUBLE);
    }

    public Response<Double> zscore(byte[] key, byte[] member) {
        client.zscore(key, member);
        return getResponse(BuilderFactory.DOUBLE);
    }

    public Response<Long> zunionstore(String dstkey, String... sets) {
        client.zunionstore(dstkey, sets);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zunionstore(byte[] dstkey, byte[]... sets) {
        client.zunionstore(dstkey, sets);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zunionstore(String dstkey, ZParams params,
            String... sets) {
        client.zunionstore(dstkey, params, sets);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zunionstore(byte[] dstkey, ZParams params,
            byte[]... sets) {
        client.zunionstore(dstkey, params, sets);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> bgrewriteaof() {
        client.bgrewriteaof();
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> bgsave() {
        client.bgsave();
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> configGet(String pattern) {
        client.configGet(pattern);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> configSet(String parameter, String value) {
        client.configSet(parameter, value);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> brpoplpush(String source, String destination,
            int timeout) {
        client.brpoplpush(source, destination, timeout);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> brpoplpush(byte[] source, byte[] destination,
            int timeout) {
        client.brpoplpush(source, destination, timeout);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> configResetStat() {
        client.configResetStat();
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> save() {
        client.save();
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Long> lastsave() {
        client.lastsave();
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> discard() {
        client.discard();
        return getResponse(BuilderFactory.STRING);
    }

    public Response<List<Object>> exec() {
        client.exec();
        Response<List<Object>> response = super.getResponse(currentMulti);
        currentMulti = null;
        return response;
    }

    public void multi() {
        client.multi();
        getResponse(BuilderFactory.STRING); //Expecting OK
        currentMulti = new MultiResponseBuilder();
    }

    public Response<Long> publish(String channel, String message) {
        client.publish(channel, message);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> publish(byte[] channel, byte[] message) {
        client.publish(channel, message);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> flushDB() {
        client.flushDB();
        return getResponse(BuilderFactory.STRING);
    }
    
    public Response<String> flushAll() {
        client.flushAll();
        return getResponse(BuilderFactory.STRING);
    }
    
    public Response<String> info() {
        client.info();
        return getResponse(BuilderFactory.STRING);
    }
    
    public Response<Long> dbSize() {
        client.dbSize();
        return getResponse(BuilderFactory.LONG);
    }
    
    public Response<String> shutdown() {
        client.shutdown();
        return getResponse(BuilderFactory.STRING);
    }
    
    public Response<String> ping() {
        client.ping();
        return getResponse(BuilderFactory.STRING);
    }
    
    public Response<String> randomKey() {
        client.randomKey();
        return getResponse(BuilderFactory.STRING);
    }   
    
    public Response<String> select(int index){
    	client.select(index);
    	return getResponse(BuilderFactory.STRING);
    }    
}
>>>>>>> 76aa07461566a5976980e6696204781271955163
