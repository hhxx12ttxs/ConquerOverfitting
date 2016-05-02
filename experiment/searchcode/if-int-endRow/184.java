/*--------------------------------------------------------------------------*
 | Copyright (C) 2006  Christopher Kohlhaas                                 |
 |                                                                          |
 | This program is free software; you can redistribute it and/or modify     |
 | it under the terms of the GNU General Public License as published by the |
 | Free Software Foundation. A copy of the license has been included with   |
 | these distribution in the COPYING file, if not go to www.fsf.org         |
 |                                                                          |
 | As a special exception, you are granted the permissions to link this     |
 | program with every library, which license fulfills the Open Source       |
 | Definition as published by the Open Source Initiative (OSI).             |
 *--------------------------------------------------------------------------*/
package org.rapla.components.calendarview.swing;

import java.util.Date;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;

/** DraggingHandler coordinates the drag events from the Block-Components
 * between the different MultiSlots of a weekview.
 */
class DraggingHandler {
    int draggingPointOffset = 0;
    DaySlot oldSlot;
    int oldY = 0;
    int oldHeight = 0;
    AbstractSwingCalendar m_cv;
    Date start = null;
    Date newStart = null;
    Date end = null;
    Date newEnd = null;
    int resizeDirection;
    boolean bMoving;
    boolean bResizing;
    boolean supportsResizing;
    
    public DraggingHandler(AbstractSwingCalendar wv, boolean supportsResizing) {
        this.supportsResizing = supportsResizing;  
        m_cv = wv;
    }
    
    public boolean supportsResizing() {
        return supportsResizing;    
    }

    public void blockPopup(SwingBlock block,Point p) {
        m_cv.fireBlockPopup(block,p);
    }

    public void blockEdit(SwingBlock block,Point p) {
        m_cv.fireBlockEdit(block,p);
    }

    public void mouseReleased(DaySlot slot, SwingBlock block, MouseEvent evt) {
        if ( isDragging() )
           stopDragging(slot, block, evt);
    }
    
    public void blockBorderPressed(DaySlot slot,SwingBlock block,MouseEvent evt, int direction) {
        if (!bResizing && supportsResizing ) {
            this.resizeDirection = direction;
            startResize( slot, block, evt);
        }
    }

    public boolean isDragging() {
        return bResizing || bMoving;
    }
    
    public void mouseDragged(DaySlot slot,SwingBlock block,MouseEvent evt) {
        if ( bResizing )
             startResize( slot, block, evt );
        else 
             startMoving( slot, block, evt );
    }
    
    private void dragging(DaySlot slot,SwingBlock block,int _x,int _y,boolean bDragging) {
        // 1. Calculate slot
        DaySlot newSlot = null;
        if ( bResizing ) {
            newSlot = slot; 
        } else {
            int slotNr = m_cv.calcSlotNr(
                slot.getLocation().x + _x
                , slot.getLocation().y + _y);
            newSlot = m_cv.getDay( slotNr );
            if (newSlot == null)
                return;
        }

        // 2. Calculate new x relative to slot
        int y = _y;
        int rowSize = newSlot.getRowSize();
        int xslot = 0;
        int height = block.getView().getHeight();
        xslot = newSlot.calcSlot( slot.getLocation().x + _x - newSlot.getLocation().x );
        if ( bResizing ) {
            if ( resizeDirection == 1) {
                y = block.getView().getLocation().y;
                //  we must trim the endRow
                int endrow = (_y - 3) / rowSize + 1;
                endrow = Math.max( newSlot.calcRow(y) + 2, endrow);
                height = endrow * rowSize - y;
                if ( bDragging ) { 
                    start = block.getStart();
                    end =  m_cv.createDate( newSlot, endrow, true);
                    //System.out.println ( end );
                }
            } else if (resizeDirection == -1){
                //  we must trim y
                y = (y  / rowSize) * rowSize;
                y = Math.min ( block.getView().getLocation().y + block.getView().getHeight() - rowSize, y );
                height = block.getView().getLocation().y  + block.getView().getHeight() - y;
                if ( bDragging ) { 
                    start = m_cv.createDate( newSlot, newSlot.calcRow( y ) , false);
                    end = block.getEnd();
                    //System.out.println ( start);
                }
            }
        } else if (bMoving){ 
            // we must trim y
            y = (y / rowSize) * rowSize ;
            if ( bDragging ) {
                start = m_cv.createDate( newSlot, newSlot.calcRow( y ) + 1, true);
                //System.out.println ( "Slot " + newSlot + " Date " + start);
            }
        }
        if (oldSlot != null && oldSlot != newSlot)
            oldSlot.paintDraggingGrid(xslot, y, height, block, oldY, oldHeight, false);

        newSlot.paintDraggingGrid(xslot, y, height, block, oldY, oldHeight, bDragging);
        oldSlot = newSlot;
        oldY = y;
        oldHeight = height;
    }
    
    private void startMoving(DaySlot slot,SwingBlock block,MouseEvent evt) {   
        if (!bMoving) {
            draggingPointOffset = evt.getY();
            if (block.isMovable()) {
                bMoving = true;
                slot.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            } else {
                bMoving = false;
                slot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                return;
            }
        }
        if ( block == null)
            return;
        int x = evt.getX() + slot.getX(block.getView());
        int y = evt.getY() + block.getView().getLocation().y;

        scrollTo( slot, x, y);
        // Correct y with the draggingPointOffset
        y = evt.getY() - draggingPointOffset + block.getView().getLocation().y + slot.getRowSize()/2;
        dragging( slot, block, x, y, bMoving);
    }
    
    private void startResize(DaySlot slot,SwingBlock block, MouseEvent evt) {
        int x = evt.getX() + slot.getX(block.getView());
        int y = evt.getY() + block.getView().getLocation().y;
        if (!bResizing) {
            if (block.isMovable() && (   ( resizeDirection == -1 && block.isStartResizable() ) 
                                                 || ( resizeDirection == 1 && block.isEndResizable()))) {
                bResizing = true;
            } else {
                bResizing = false;
                return;
            }
        }
        if ( block == null)
            return;
        scrollTo( slot, x, y);
        dragging( slot, block, x, y, bResizing);
    }
    
    private void stopDragging(DaySlot slot, SwingBlock block,MouseEvent evt) {
        slot.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        if ( block == null) {
        	return ;
        }
        
        if (!m_cv.isEditable()) 
            return;
        try {
            int x = evt.getX() + slot.getX( block.getView() );
            int y = evt.getY() - draggingPointOffset + block.getView().getLocation().y + slot.getRowSize()/2;
            dragging(slot,block,x,y,false);
            Point upperLeft = m_cv.getScrollPane().getViewport().getViewPosition();
            Point newPoint = new Point(slot.getLocation().x + x -upperLeft.x
                    ,y-upperLeft.y);
            if ( bMoving ) {
                // Has the block moved
                //System.out.println("Moved to " + newStart + " - " + newEnd);
                if ( !start.equals( block.getStart() ) ) {
                    m_cv.fireMoved(block, newPoint, start);
                }
            }
            if ( bResizing ) {
                // System.out.println("Resized to " + start + " - " + end);
                if ( !( start.equals( block.getStart() )  && end.equals( block.getEnd()) )) {
                    m_cv.fireResized(block, newPoint, start, end);
                }
            }
        } finally {
            bResizing = false;
            bMoving = false;
            start = null; 
            end = null; 
        }
    }    
    
    // Begin scrolling when hitting the upper or lower border while
    // dragging or selecting.
    private void scrollTo(DaySlot slot,int x,int y) {
        // 1. Transfer p.x relative to jCenter
        m_cv.scrollTo(slot.getLocation().x + x, slot.getLocation().y + y);
    }

}


