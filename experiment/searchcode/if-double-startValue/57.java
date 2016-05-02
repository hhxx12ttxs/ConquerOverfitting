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
package com.metsci.glimpse.plot.timeline.listener;

import java.util.List;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.tagged.Tag;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.axis.tagged.TaggedAxisMouseListener1D;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.ModifierKey;
import com.metsci.glimpse.event.mouse.MouseButton;
import com.metsci.glimpse.plot.timeline.StackedTimePlot2D;

public class TimelineMouseListener1D extends TaggedAxisMouseListener1D
{
    protected boolean dragTogether = false;
    protected boolean onlyMoveCurrent = false;
    protected StackedTimePlot2D plot;
    protected boolean timeIsX;

    public TimelineMouseListener1D( StackedTimePlot2D plot )
    {
        super( 25 );

        this.timeIsX = plot.isTimeAxisHorizontal( );
        this.plot = plot;
    }

    public Tag getSelectedTag( )
    {
        return selectedTag;
    }

    @Override
    public void mousePressed( GlimpseMouseEvent e, Axis1D axis, boolean horizontal )
    {
        super.mousePressed( e, axis, horizontal );

        // right clicks toggle selection locking
        if ( this.allowSelectionLock && e.isButtonDown( MouseButton.Button3 ) )
        {
            if ( plot.isCurrentTimeLocked( ) || plot.isSelectionLocked( ) )
            {
                plot.setCurrentTimeLocked( false );
                plot.setSelectionLocked( false );
            }
            else
            {
                plot.setSelectionLocked( !plot.isSelectionLocked( ) );
            }
        }
    }

    @Override
    protected Tag getSelectedTag( TaggedAxis1D taggedAxis, List<Tag> tags, int mousePos, int maxPixelDist )
    {
        Tag minTag = taggedAxis.getTag( StackedTimePlot2D.MIN_TIME );
        Tag maxTag = taggedAxis.getTag( StackedTimePlot2D.MAX_TIME );
        Tag currentTag = taggedAxis.getTag( StackedTimePlot2D.CURRENT_TIME );

        dragTogether = false;

        double maxDistance = maxPixelDist / taggedAxis.getPixelsPerValue( );
        double mouseValue = taggedAxis.screenPixelToValue( mousePos );

        boolean closeToMin = Math.abs( mouseValue - minTag.getValue( ) ) < maxDistance;
        boolean closeToMax = Math.abs( mouseValue - maxTag.getValue( ) ) < maxDistance;

        Tag selectedTag = null;

        if ( closeToMin )
        {
            selectedTag = minTag;
        }
        else if ( closeToMax )
        {
            selectedTag = maxTag;
        }
        else if ( Math.abs( mouseValue - currentTag.getValue( ) ) < maxDistance )
        {
            selectedTag = currentTag;
        }

        return selectedTag;
    }

    @Override
    public void mouseMoved( GlimpseMouseEvent e, Axis1D axis, boolean horizontal )
    {
        TaggedAxis1D taggedAxis = ( TaggedAxis1D ) axis;

        if ( e.isKeyDown( ModifierKey.Ctrl ) && e.isButtonDown( MouseButton.Button1 ) && selectedTag != null )
        {
            tagDragged( e, taggedAxis, horizontal );
        }
        else if ( e.isButtonDown( MouseButton.Button1 ) && !plot.isCurrentTimeLocked( ) )
        {
            mouseDragged( e, taggedAxis, horizontal );
        }
        else if ( !plot.isSelectionLocked( ) && !plot.isCurrentTimeLocked( ) )
        {
            mouseHovered( e, taggedAxis, horizontal );
        }

        mouseMoved0( e, taggedAxis, horizontal );

        taggedAxis.validateTags( );
        taggedAxis.validate( );

        notifyTagsUpdated( taggedAxis );
    }
    
    protected void mouseMoved0( GlimpseMouseEvent e, TaggedAxis1D taggedAxis, boolean horizontal )
    {
        // subclasses can add additional mouseMoved behaviors here, do nothing by default
    }
    
    protected void tagDragged( GlimpseMouseEvent e, TaggedAxis1D taggedAxis, boolean horizontal )
    {
        anchor( taggedAxis, horizontal, e.getX( ), e.getY( ) );

        int mousePosPixels = getDim( horizontal, e.getX( ), taggedAxis.getSizePixels( ) - e.getY( ) );
        int panPixels = getDim( horizontal, anchorPixelsX, anchorPixelsY ) - mousePosPixels;
        double panValue = panPixels / taggedAxis.getPixelsPerValue( );
        double newTagValue = tagAnchor - panValue;

        this.selectedTag.setValue( newTagValue );
    }
    
    protected void mouseDragged( GlimpseMouseEvent e, TaggedAxis1D taggedAxis, boolean horizontal )
    {
        pan( taggedAxis, horizontal, e.getX( ), e.getY( ) );
    }
    
    protected void mouseHovered( GlimpseMouseEvent e, TaggedAxis1D taggedAxis, boolean horizontal )
    {
        Tag minTag = taggedAxis.getTag( StackedTimePlot2D.MIN_TIME );
        Tag maxTag = taggedAxis.getTag( StackedTimePlot2D.MAX_TIME );
        Tag currentTag = taggedAxis.getTag( StackedTimePlot2D.CURRENT_TIME );
        
        int mousePosPixels = getDim( horizontal, e.getX( ), taggedAxis.getSizePixels( ) - e.getY( ) );
        double mousePosValue = taggedAxis.screenPixelToValue( mousePosPixels );

        double minDiff = minTag.getValue( ) - currentTag.getValue( );
        double maxDiff = maxTag.getValue( ) - currentTag.getValue( );

        minTag.setValue( mousePosValue + minDiff );
        maxTag.setValue( mousePosValue + maxDiff );
        currentTag.setValue( mousePosValue );
    }
    
    @Override
    public void mouseWheelMoved( GlimpseMouseEvent e )
    {
        TaggedAxis1D taggedAxis = getTaggedAxis1D( e );

        if ( taggedAxis == null ) return;

        if ( e.isKeyDown( ModifierKey.Ctrl ) || e.isKeyDown( ModifierKey.Meta ) )
        {
            handleCtrlMouseWheel( e );
        }
        else
        {
            if ( timeIsX )
            {
                this.mouseWheelMoved( e, taggedAxis, true );
            }
            else
            {
                this.mouseWheelMoved( e, taggedAxis, false );
            }
        }

        taggedAxis.validateTags( );
        taggedAxis.validate( );

        notifyTagsUpdated( taggedAxis );
    }

    public void handleCtrlMouseWheel( GlimpseMouseEvent e )
    {
        TaggedAxis1D taggedAxis = getTaggedAxis1D( e );

        if ( taggedAxis == null ) return;
        
        int zoomIncrements = e.getWheelIncrement( );

        double newSelectionSize = calculateNewSelectionSize( taggedAxis, zoomIncrements );
        
        Tag minTag = taggedAxis.getTag( StackedTimePlot2D.MIN_TIME );
        Tag maxTag = taggedAxis.getTag( StackedTimePlot2D.MAX_TIME );
        Tag currentTag = taggedAxis.getTag( StackedTimePlot2D.CURRENT_TIME );
        
        double maxValue = maxTag.getValue( );
        
        minTag.setValue( maxValue - newSelectionSize );
        maxTag.setValue( maxValue );
        currentTag.setValue( maxValue );

        taggedAxis.validateTags( );
        taggedAxis.validate( );

        notifyTagsUpdated( minTag.getValue( ), maxTag.getValue( ), currentTag.getValue( ) );
    }
    
    protected double calculateNewSelectionSize( TaggedAxis1D taggedAxis, int zoomIncrements )
    {
        Tag minTag = taggedAxis.getTag( StackedTimePlot2D.MIN_TIME );
        Tag maxTag = taggedAxis.getTag( StackedTimePlot2D.MAX_TIME );

        double minValue = minTag.getValue( );
        double maxValue = maxTag.getValue( );
        double selectionSize = maxValue - minValue;

        double zoomPercentDbl = 1.0f;
        for ( int i = 0; i < Math.abs( zoomIncrements ); i++ )
        {
            zoomPercentDbl *= 1.0 + zoomConstant;
        }
        zoomPercentDbl = zoomIncrements > 0 ? 1.0 / zoomPercentDbl : zoomPercentDbl;
        double newSelectionSize = selectionSize * zoomPercentDbl;
        
        return newSelectionSize;
    }

    // TimelineMouseListener1D is used as a delegate for TimelineMouseListener2D, which
    // means it is sometimes passed GlimpseMouseEvents from GlimpseAxisLayout2D. This
    // method gets the correct Axis1D based on whether the time axis is X or Y for this timeline plot
    protected TaggedAxis1D getTaggedAxis1D( GlimpseMouseEvent e )
    {
        Axis1D axis = e.getAxis1D( );

        if ( axis != null )
        {
            return ( TaggedAxis1D ) e.getAxis1D( );
        }
        else
        {
            Axis2D axis2D = e.getAxis2D( );
            if ( axis2D == null ) return null;

            if ( timeIsX )
            {
                return ( TaggedAxis1D ) axis2D.getAxisX( );
            }
            else
            {
                return ( TaggedAxis1D ) axis2D.getAxisY( );
            }   
        }
    }

    protected void moveAllTags( TaggedAxis1D taggedAxis, double deltaTagValue )
    {
        for ( Tag tag : taggedAxis.getSortedTags( ) )
        {
            tag.setValue( tag.getValue( ) + deltaTagValue );
        }
    }

    protected void notifyTagsUpdated( TaggedAxis1D timeAxis )
    {
        List<Tag> tags = timeAxis.getSortedTags( );
        notifyTagsUpdated( tags.get( 0 ).getValue( ), tags.get( 2 ).getValue( ), tags.get( 1 ).getValue( ) );
    }

    protected void notifyTagsUpdated( double startValue, double endValue, double selectedValue )
    {
    }
}

