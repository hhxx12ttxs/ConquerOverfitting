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
import java.awt.Point;
import java.awt.Component;
import javax.swing.SwingUtilities;
import java.awt.event.*;

/** SelectionHandler handles the  selection events and the Slot
 * Context Menu (right click).
 * This is internally used by the weekview to communicate with its slots.
 */
class SelectionHandler extends MouseAdapter implements MouseMotionListener {
    Date start;
    Date end;
    boolean bPopupClicked = false;
    boolean bSelecting = false;
    public int selectionStart = -1;
    public int selectionEnd = -1;
    private int oldIndex = -1;
    private int oldSlotNr = -1;
    private int startSlot = -1;
    private int endSlot = -1;
    private int draggingSlot = -1;
    private int draggingIndex = -1;
    AbstractSwingCalendar m_wv;
    boolean periodSelection;
    
    public SelectionHandler(AbstractSwingCalendar wv) {
        m_wv = wv;
    }

    public void mouseClicked(MouseEvent evt) {
        if (evt.isPopupTrigger()) {
            bPopupClicked = true;
            slotPopup(evt);
        } else {
            /* We don't check click here
            if (SwingUtilities.isLeftMouseButton(evt))
                move(evt);
                */
        }
    }
    public void mousePressed(MouseEvent evt) {
        if (evt.isPopupTrigger()) {
            bPopupClicked = true;
                slotPopup(evt);
        } else {
            if (SwingUtilities.isLeftMouseButton(evt))
                move(evt);
        }
    }

    public void mouseReleased(MouseEvent evt) {
        if (evt.isPopupTrigger()) {
            bPopupClicked = true;
            slotPopup(evt);
        }
        if (SwingUtilities.isLeftMouseButton(evt) && !bPopupClicked)
            move(evt);
        bPopupClicked = false;
        bSelecting = false;
    }

    public void mouseDragged(MouseEvent evt) {
        if (SwingUtilities.isLeftMouseButton(evt) && !bPopupClicked)
            move(evt);
    }
    public void mouseMoved(MouseEvent evt) {
    }

    public void setPeriodSelection(boolean period) {
           periodSelection = period;
    }
    
    private void clearSelection() {
        for (int i=0;i<m_wv.getDayCount();i++)
            if (m_wv.getDay(i) != null)
                m_wv.getDay(i).unselectAll();
    }

    

    public void slotPopup(MouseEvent evt) {
        Point p = new Point(evt.getX(),evt.getY());
        DaySlot slot= (DaySlot)evt.getSource();
        if (start == null || end == null) {
            int index = slot.calcRow( evt.getY() );
            start = m_wv.createDate(slot, index, true);
            end = m_wv.createDate(slot, index, false);
            clearSelection();
            slot.select(index,index);
        }
        m_wv.fireSelectionPopup((Component)slot,p,start,end,m_wv.getSlotNr( slot ));
    }

    public void move(MouseEvent evt) {
        if (!m_wv.isEditable()) 
            return;
        DaySlot source = (DaySlot) evt.getSource();
        int slotNr = m_wv.calcSlotNr(
                source.getLocation().x + evt.getX()
                ,source.getLocation().y + evt.getY()
                );
        if (slotNr == -1)
            return;

        int selectedIndex = source.calcRow(evt.getY());

        if (!bSelecting) {
            clearSelection();
            bSelecting = true;
            selectionStart = selectedIndex;
            selectionEnd = selectedIndex;
            draggingSlot =slotNr;
            draggingIndex = selectedIndex;
            startSlot = slotNr;
            endSlot = slotNr;
        } else {
            if (slotNr == draggingSlot) {
                startSlot = endSlot = slotNr;
                if ( selectedIndex > draggingIndex ) {
                    selectionStart = draggingIndex;
                    selectionEnd = selectedIndex;
                } else if (selectedIndex < draggingIndex ){
                    selectionStart = selectedIndex;
                    selectionEnd = draggingIndex;
                }
            } else if (slotNr > draggingSlot) {
                startSlot = draggingSlot;
                selectionStart = draggingIndex;
                endSlot = slotNr;
                selectionEnd = selectedIndex;
            } else if (slotNr < draggingSlot) {
                startSlot = slotNr;
                selectionStart = selectedIndex;
                endSlot = draggingSlot;
                selectionEnd = draggingIndex;
            }
            if (selectedIndex == oldIndex && slotNr == oldSlotNr)
                return;
            int rowsPerDay = m_wv.getRowsPerDay();
            if (selectedIndex >= rowsPerDay-1)
                selectedIndex = rowsPerDay-1;
        }
        oldSlotNr = slotNr;
        oldIndex = selectedIndex;
        setSelection();
        m_wv.scrollTo(
                m_wv.getDay(slotNr).getLocation().x + evt.getX()
                ,m_wv.getDay(slotNr).getLocation().y + evt.getY()
        );
    }

    private void setSelection() {

        int startRow = selectionStart;
        int endRow = m_wv.getRowsPerDay() -1;
        
        
        for (int i=0;i<startSlot;i++) {
            if (m_wv.getDay(i) != null) {
                m_wv.getDay(i).unselectAll();
            }
        }
        
     
       if ( periodSelection ) {
           if ( selectionStart > selectionEnd ) {
           startRow = selectionEnd;
           endRow = selectionStart;
           } else {
           startRow = selectionStart;
           endRow = selectionEnd;
           }
           int startSlotDay = startSlot % 7;
           int endSlotDay = endSlot % 7;
           int startDayOfWeek = Math.min(startSlotDay, endSlotDay);
           int endDayOfWeek = Math.max(startSlotDay, endSlotDay);
           int maxRow = m_wv.getRowsPerDay();
           int realEndSlot = (endSlot/7)*7 + endDayOfWeek + 1; 
           // System.out.println("selectionStart=" + selectionStart + " selectionEnd=" + selectionEnd + " startslot=" + startSlot + " endSlot=" + endSlot + " maxrow=" + maxRow) ;
           for (int i=(startSlot/7)*7;i<=realEndSlot;i++) {
           if (m_wv.getDay(i) == null)
               continue;
           int dayOfWeek = i % 7;
           if ( dayOfWeek < startDayOfWeek
                || dayOfWeek > endDayOfWeek
                )
               m_wv.getDay(i).unselectAll();
           else if ( dayOfWeek > startDayOfWeek
                 && dayOfWeek < endDayOfWeek)
               m_wv.getDay(i).select(0, 10000); // XXX Excoffier
           else if ( dayOfWeek == startDayOfWeek
                 && dayOfWeek == endDayOfWeek)
               m_wv.getDay(i).select(startRow,endRow);
           else if ( dayOfWeek == startDayOfWeek
                 && dayOfWeek < endDayOfWeek)
               m_wv.getDay(i).select((startRow+7*maxRow) % maxRow,10000); // XXX
           else if ( dayOfWeek > startDayOfWeek
                 && dayOfWeek == endDayOfWeek)
               m_wv.getDay(i).select(0, endRow % maxRow);
           else
               System.out.println("Should raise an error"); // XXX
           }
           for (int i=realEndSlot;i<m_wv.getDayCount();i++) {
           if (m_wv.getDay(i) != null)
               m_wv.getDay(i).unselectAll();
           }
    
       } else {
           for (int i=startSlot;i<=endSlot;i++) {
           if (i > startSlot)
               startRow = 0;
           if (i == endSlot)
               endRow = selectionEnd;
           if (m_wv.getDay(i) != null)
               m_wv.getDay(i).select(startRow,endRow);
           }
           startRow = selectionStart ;
           endRow = selectionEnd ;
           for (int i=endSlot+1;i<m_wv.getDayCount();i++) {
           if (m_wv.getDay(i) != null)
               m_wv.getDay(i).unselectAll();
           }
       }

      start = m_wv.createDate(m_wv.getDay(startSlot),startRow, true);
      end = m_wv.createDate(m_wv.getDay(endSlot),endRow, false);
      m_wv.fireSelectionChanged(start,end);
    }

}




