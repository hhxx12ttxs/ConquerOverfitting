/*
 * JEditTextArea.java - jEdit's text component
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 1999, 2000, 2001 Slava Pestov
 * Portions copyright (C) 2000 Ollie Rutherfurd
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.gjt.sp.jedit.textarea;

//{{{ Imports
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.text.Segment;
import javax.swing.undo.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.gjt.sp.jedit.buffer.*;
import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.jedit.syntax.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.util.Log;
//}}}

/**
 * jEdit's text component.
 *
 * @author Slava Pestov
 * @version $Id: JEditTextArea.java 3936 2001-12-21 07:02:14Z spestov $
 */
public class JEditTextArea extends JComponent
{
	//{{{ JEditTextArea constructor
	/**
	 * Creates a new JEditTextArea.
	 */
	public JEditTextArea(View view)
	{
		enableEvents(AWTEvent.FOCUS_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);

		this.view = view;

		//{{{ Initialize some misc. stuff
		selection = new Vector();
		renderer = new TextRenderer();
		painter = new TextAreaPainter(this);
		gutter = new Gutter(view,this);
		bufferHandler = new BufferChangeHandler();
		listenerList = new EventListenerList();
		caretEvent = new MutableCaretEvent();
		bracketLine = bracketPosition = -1;
		blink = true;
		lineSegment = new Segment();
		//}}}

		//{{{ Initialize the GUI
		setLayout(new ScrollLayout());
		add(LEFT,gutter);
		add(CENTER,painter);
		add(RIGHT,vertical = new JScrollBar(JScrollBar.VERTICAL));
		add(BOTTOM,horizontal = new JScrollBar(JScrollBar.HORIZONTAL));

		horizontal.setValues(0,0,0,0);
		//}}}

		//{{{ this ensures that the text area's look is slightly
		// more consistent with the rest of the metal l&f.
		// while it depends on not-so-well-documented portions
		// of Swing, it only affects appearance, so future
		// breakage shouldn't matter
		if(UIManager.getLookAndFeel() instanceof MetalLookAndFeel)
		{
			setBorder(new TextAreaBorder());
			vertical.putClientProperty("JScrollBar.isFreeStanding",
				Boolean.FALSE);
			horizontal.putClientProperty("JScrollBar.isFreeStanding",
				Boolean.FALSE);
			//horizontal.setBorder(null);
		}
		//}}}

		//{{{ Add some event listeners
		vertical.addAdjustmentListener(new AdjustHandler());
		horizontal.addAdjustmentListener(new AdjustHandler());
		painter.addComponentListener(new ComponentHandler());

		mouseHandler = new MouseHandler();
		painter.addMouseListener(mouseHandler);
		painter.addMouseMotionListener(mouseHandler);

		addFocusListener(new FocusHandler());
		//}}}

		// This doesn't seem very correct, but it fixes a problem
		// when setting the initial caret position for a buffer
		// (eg, from the recent file list)
		focusedComponent = this;
	} //}}}

	//{{{ Getters and setters

	//{{{ getPainter() method
	/**
	 * Returns the object responsible for painting this text area.
	 */
	public final TextAreaPainter getPainter()
	{
		return painter;
	} //}}}

	//{{{ getGutter() method
 	/**
	 * Returns the gutter to the left of the text area or null if the gutter
	 * is disabled
	 */
	public final Gutter getGutter()
	{
		return gutter;
	} //}}}

	//{{{ getTextRenderer() method
	/**
	 * Returns the text renderer instance.
	 * @since jEdit 3.2pre6
	 */
	public TextRenderer getTextRenderer()
	{
		return renderer;
	} //}}}

	//{{{ getFoldVisibilityManager() method
	/**
	 * Returns the fold visibility manager used by this text area.
	 * @since jEdit 4.0pre1
	 */
	public FoldVisibilityManager getFoldVisibilityManager()
	{
		return foldVisibilityManager;
	} //}}}

	//{{{ isCaretBlinkEnabled() method
	/**
	 * Returns true if the caret is blinking, false otherwise.
	 */
	public final boolean isCaretBlinkEnabled()
	{
		return caretBlinks;
	} //}}}

	//{{{ setCaretBlinkEnabled() method
	/**
	 * Toggles caret blinking.
	 * @param caretBlinks True if the caret should blink, false otherwise
	 */
	public void setCaretBlinkEnabled(boolean caretBlinks)
	{
		this.caretBlinks = caretBlinks;
		if(!caretBlinks)
			blink = false;

		if(buffer != null)
			invalidateLine(caretLine);
	} //}}}

	//{{{ getElectricScroll() method
	/**
	 * Returns the number of lines from the top and button of the
	 * text area that are always visible.
	 */
	public final int getElectricScroll()
	{
		return electricScroll;
	} //}}}

	//{{{ setElectricScroll() method
	/**
	 * Sets the number of lines from the top and bottom of the text
	 * area that are always visible
	 * @param electricScroll The number of lines always visible from
	 * the top or bottom
	 */
	public final void setElectricScroll(int electricScroll)
	{
		this.electricScroll = electricScroll;
	} //}}}

	//{{{ isMiddleMousePasteEnabled() method
	/**
	 * Returns if clicking the middle mouse button pastes the most
	 * recent selection (% register).
	 */
	public final boolean isMiddleMousePasteEnabled()
	{
		return middleMousePaste;
	} //}}}

	//{{{ setMiddleMousePasteEnabled() method
	/**
	 * Sets if clicking the middle mouse button pastes the most
	 * recent selection (% register).
	 * @param middleMousePaste A boolean flag
	 */
	public final void setMiddleMousePasteEnabled(boolean middleMousePaste)
	{
		this.middleMousePaste = middleMousePaste;
	} //}}}

	//{{{ getBuffer() method
	/**
	 * Returns the buffer this text area is editing.
	 */
	public final Buffer getBuffer()
	{
		return buffer;
	} //}}}

	//{{{ setBuffer() method
	/**
	 * Sets the buffer this text area is editing.
	 * @param buffer The buffer
	 */
	public void setBuffer(Buffer buffer)
	{
		if(this.buffer == buffer)
			return;

		try
		{
			bufferChanging = true;

			if(this.buffer != null)
			{
				this.buffer._releaseFoldVisibilityManager(foldVisibilityManager);
				this.buffer.removeBufferChangeListener(bufferHandler);
			}
			this.buffer = buffer;

			buffer.addBufferChangeListener(bufferHandler);
			bufferHandlerInstalled = true;

			foldVisibilityManager = buffer._getFoldVisibilityManager(this);

			setCaretPosition(0);

			// just in case, maybe not necessary?...
			physFirstLine = foldVisibilityManager.virtualToPhysical(0);

			painter.updateTabSize();

			for(int i = 0; i < lineWidths.length; i++)
			{
				lineWidths[i] = 0;
			}

			updateScrollBars();
			painter.repaint();
			gutter.repaint();
		}
		finally
		{
			bufferChanging = false;
		}
	} //}}}

	//{{{ isEditable() method
	/**
	 * Returns true if this text area is editable, false otherwise.
	 */
	public final boolean isEditable()
	{
		return buffer.isEditable();
	} //}}}

	//{{{ getRightClickPopup() method
	/**
	 * Returns the right click popup menu.
	 */
	public final JPopupMenu getRightClickPopup()
	{
		return popup;
	} //}}}

	//{{{ setRightClickPopup() method
	/**
	 * Sets the right click popup menu.
	 * @param popup The popup
	 */
	public final void setRightClickPopup(JPopupMenu popup)
	{
		this.popup = popup;
	} //}}}

	//}}}

	//{{{ Scrolling

	//{{{ getFirstLine() method
	/**
	 * Returns the line displayed at the text area's origin. This is
	 * a virtual, not a physical, line number.
	 */
	public final int getFirstLine()
	{
		return firstLine;
	} //}}}

	//{{{ setFirstLine() method
	/**
	 * Sets the line displayed at the text area's origin. This is
	 * a virtual, not a physical, line number.
	 */
	public void setFirstLine(int firstLine)
	{
		if(firstLine == this.firstLine)
			return;

		_setFirstLine(firstLine);

		view.synchroScrollVertical(this,firstLine);
	} //}}}

	//{{{ _setFirstLine() method
	public void _setFirstLine(int firstLine)
	{
		firstLine = Math.max(0,firstLine);
		this.firstLine = firstLine;

		physFirstLine = virtualToPhysical(firstLine);

		maxHorizontalScrollWidth = 0;

		// hack so that if we scroll and the matching bracket
		// comes into view, it is highlighted

		// 3.2pre9 update: I am commenting this out once again because
		// I have changed the location of the repaintAndScroll() call
		// in the BufferChangeHandler, so this is called before the caret
		// position is updated, which can be potentially tricky.

		//if(bracketPosition == -1)
		//	updateBracketHighlight();

		if(this.firstLine != vertical.getValue())
			updateScrollBars();

		painter.repaint();
		gutter.repaint();

		fireScrollEvent(true);
	} //}}}

	//{{{ getVisibleLines() method
	/**
	 * Returns the number of lines visible in this text area.
	 */
	public final int getVisibleLines()
	{
		return visibleLines;
	} //}}}

	//{{{ getHorizontalOffset() method
	/**
	 * Returns the horizontal offset of drawn lines.
	 */
	public final int getHorizontalOffset()
	{
		return horizontalOffset;
	} //}}}

	//{{{ setHorizontalOffset() method
	/**
	 * Sets the horizontal offset of drawn lines. This can be used to
	 * implement horizontal scrolling.
	 * @param horizontalOffset offset The new horizontal offset
	 */
	public void setHorizontalOffset(int horizontalOffset)
	{
		if(horizontalOffset == this.horizontalOffset)
			return;
		_setHorizontalOffset(horizontalOffset);

		view.synchroScrollHorizontal(this,horizontalOffset);
	} //}}}

	//{{{ _setHorizontalOffset() method
	public void _setHorizontalOffset(int horizontalOffset)
	{
		this.horizontalOffset = horizontalOffset;
		if(horizontalOffset != horizontal.getValue())
			updateScrollBars();
		painter.repaint();

		fireScrollEvent(false);
	} //}}}

	//{{{ setOrigin() method
	/**
	 * @deprecated Use setFirstLine() and setHorizontalOffset() instead
	 */
	public boolean setOrigin(int firstLine, int horizontalOffset)
	{
		setFirstLine(firstLine);
		setHorizontalOffset(horizontalOffset);
		return true;
	} //}}}

	//{{{ updateScrollBars() method
	/**
	 * Updates the state of the scroll bars. This should be called
	 * if the number of lines in the buffer changes, or when the
	 * size of the text are changes.
	 */
	public void updateScrollBars()
	{
		if(vertical != null && visibleLines != 0)
		{
			// don't display stuff past the end of the buffer if
			// we can help it
			int lineCount = getVirtualLineCount();
			if(lineCount < firstLine + visibleLines)
			{
				// this will call updateScrollBars(), so
				// just return...
				int newFirstLine = Math.max(0,lineCount - visibleLines);
				if(newFirstLine != firstLine)
				{
					setFirstLine(newFirstLine);
					return;
				}
			}

			vertical.setValues(firstLine,visibleLines,0,lineCount);
			vertical.setUnitIncrement(2);
			vertical.setBlockIncrement(visibleLines);
		}

		int width = painter.getWidth();
		if(horizontal != null && width != 0)
		{
			maxHorizontalScrollWidth = 0;
			painter.repaint();

			horizontal.setUnitIncrement(painter.getFontMetrics()
				.charWidth('w'));
			horizontal.setBlockIncrement(width / 2);
		}
	} //}}}

	//{{{ scrollUpLine() method
	/**
	 * Scrolls up by one line.
	 * @since jEdit 2.7pre2
	 */
	public void scrollUpLine()
	{
		if(firstLine > 0)
			setFirstLine(firstLine-1);
		else
			getToolkit().beep();
	} //}}}

	//{{{ scrollUpPage() method
	/**
	 * Scrolls up by one page.
	 * @since jEdit 2.7pre2
	 */
	public void scrollUpPage()
	{
		if(firstLine > 0)
		{
			int newFirstLine = firstLine - visibleLines;
			setFirstLine(newFirstLine);
		}
		else
		{
			getToolkit().beep();
		}
	} //}}}

	//{{{ scrollDownLine() method
	/**
	 * Scrolls down by one line.
	 * @since jEdit 2.7pre2
	 */
	public void scrollDownLine()
	{
		int numLines = getVirtualLineCount();

		if(firstLine + visibleLines < numLines)
			setFirstLine(firstLine + 1);
		else
			getToolkit().beep();
	} //}}}

	//{{{ scrollDownPage() method
	/**
	 * Scrolls down by one page.
	 * @since jEdit 2.7pre2
	 */
	public void scrollDownPage()
	{
		int numLines = getVirtualLineCount();

		if(firstLine + visibleLines < numLines)
		{
			int newFirstLine = firstLine + visibleLines;
			setFirstLine(newFirstLine + visibleLines < numLines
				? newFirstLine : numLines - visibleLines);
		}
		else
		{
			getToolkit().beep();
		}
	} //}}}

	//{{{ scrollToCaret() method
	/**
	 * Ensures that the caret is visible by scrolling the text area if
	 * necessary.
	 * @param doElectricScroll If true, electric scrolling will be performed
	 */
	public void scrollToCaret(boolean doElectricScroll)
	{
		int offset = caret - getLineStartOffset(caretLine);
		int virtualCaretLine = physicalToVirtual(caretLine);

		// visibleLines == 0 before the component is realized
		// we can't do any proper scrolling then, so we have
		// this hack...
		if(visibleLines == 0)
		{
			setFirstLine(physicalToVirtual(
				Math.max(0,caretLine - electricScroll)));
			return;
		}

		int lineCount = getVirtualLineCount();

		int electricScroll;

		if(doElectricScroll && visibleLines > this.electricScroll * 2)
			electricScroll = this.electricScroll;
		else
			electricScroll = 0;

		boolean changed = false;

		int _firstLine = (firstLine == 0 ? 0 : firstLine + electricScroll);
		int _lastLine = firstLine + visibleLines - electricScroll;

		if(virtualCaretLine > _firstLine
			&& (virtualCaretLine < _lastLine
			|| firstLine + visibleLines >= lineCount))
		{
			// vertical scroll position is correct already
		}
		else if(_firstLine - virtualCaretLine > visibleLines
			|| virtualCaretLine - _lastLine > visibleLines)
		{
			int startLine, endLine;
			Selection s = getSelectionAtOffset(caret);
			if(s == null)
			{
				startLine = endLine = virtualCaretLine;
			}
			else
			{
				startLine = physicalToVirtual(s.startLine);
				endLine = physicalToVirtual(s.endLine);
			}

			if(endLine - startLine <= visibleLines)
				firstLine = (startLine + endLine - visibleLines) / 2;
			else
				firstLine = physicalToVirtual(caretLine) - visibleLines / 2;

			firstLine = Math.min(firstLine,getVirtualLineCount()
				- visibleLines);
			firstLine = Math.max(firstLine,0);

			changed = true;
		}
		else if(virtualCaretLine < _firstLine)
		{
			firstLine = Math.max(0,virtualCaretLine - electricScroll);

			changed = true;
		}
		else if(virtualCaretLine >= _lastLine)
		{
			firstLine = (virtualCaretLine - visibleLines)
				+ electricScroll + 1;
			if(firstLine >= getVirtualLineCount() - visibleLines)
				firstLine = getVirtualLineCount() - visibleLines;

			changed = true;
		}

		int x = offsetToX(caretLine,offset);
		int width = painter.getFontMetrics().charWidth('w');

		if(x < 0)
		{
			horizontalOffset = Math.min(0,horizontalOffset
				- x + width + 5);
			changed = true;
		}
		else if(x >= painter.getWidth() - width - 5)
		{
			horizontalOffset = horizontalOffset +
				(painter.getWidth() - x) - width - 5;
			changed = true;
		}

		if(changed)
		{
			if(firstLine < 0)
				firstLine = 0;

			physFirstLine = virtualToPhysical(firstLine);

			updateScrollBars();
			painter.repaint();
			gutter.repaint();

			view.synchroScrollVertical(this,firstLine);
			view.synchroScrollHorizontal(this,horizontalOffset);

			// fire events for both a horizontal and vertical scroll
			fireScrollEvent(true);
			fireScrollEvent(false);
		}
	} //}}}

	//{{{ addScrollListener() method
	/**
	 * Adds a scroll listener to this text area.
	 * @param listener The listener
	 * @since jEdit 3.2pre2
	 */
	public final void addScrollListener(ScrollListener listener)
	{
		listenerList.add(ScrollListener.class,listener);
	} //}}}

	//{{{ removeScrollListener() method
	/**
	 * Removes a scroll listener from this text area.
	 * @param listener The listener
	 * @since jEdit 3.2pre2
	 */
	public final void removeScrollListener(ScrollListener listener)
	{
		listenerList.remove(ScrollListener.class,listener);
	} //}}}

	//}}}

	//{{{ Offset conversion

	//{{{ lineToY() method
	/**
	 * Converts a line index to a y co-ordinate. This must be a virtual,
	 * not a physical, line number.
	 * @param line The line
	 */
	public int lineToY(int line)
	{
		FontMetrics fm = painter.getFontMetrics();
		return (line - firstLine) * fm.getHeight()
			- (fm.getLeading() + fm.getDescent());
	} //}}}

	//{{{ yToLine() method
	/**
	 * Converts a y co-ordinate to a virtual line index.
	 * @param y The y co-ordinate
	 */
	public int yToLine(int y)
	{
		FontMetrics fm = painter.getFontMetrics();
		int height = fm.getHeight();
		return Math.max(0,Math.min(getVirtualLineCount() - 1,
			y / height + firstLine));
	} //}}}

	//{{{ offsetToX() method
	/**
	 * Converts an offset in a line into an x co-ordinate.
	 * @param line The line
	 * @param offset The offset, from the start of the line
	 */
	public int offsetToX(int line, int offset)
	{
		Token tokens = buffer.markTokens(line).getFirstToken();

		getLineText(line,lineSegment);

		char[] text = lineSegment.array;
		int off = lineSegment.offset;

		float x = (float)horizontalOffset;

		Toolkit toolkit = painter.getToolkit();
		Font defaultFont = painter.getFont();
		SyntaxStyle[] styles = painter.getStyles();

		for(;;)
		{
			byte id = tokens.id;
			if(id == Token.END)
				return (int)x;

			Font font;
			if(id == Token.NULL)
				font = defaultFont;
			else
				font = styles[id].getFont();

			int len = tokens.length;

			if(offset < len)
			{
				return (int)(x + renderer.charsWidth(
					text,off,offset,font,x,painter));
			}
			else
			{
				x += renderer.charsWidth(
					text,off,len,font,x,painter);
				off += len;
				offset -= len;
			}

			tokens = tokens.next;
		}
	} //}}}

	//{{{ xToOffset() method
	/**
	 * Converts an x co-ordinate to an offset within a line.
	 * @param line The physical line index
	 * @param x The x co-ordinate
	 */
	public int xToOffset(int line, int x)
	{
		return xToOffset(line,x,true);
	} //}}}

	//{{{ xToOffset() method
	/**
	 * Converts an x co-ordinate to an offset within a line.
	 * @param line The physical line index
	 * @param x The x co-ordinate
	 * @param round Round up to next letter if past the middle of a letter?
	 * @since jEdit 3.2pre6
	 */
	public int xToOffset(int line, int x, boolean round)
	{
		Token tokens = buffer.markTokens(line).getFirstToken();

		getLineText(line,lineSegment);

		char[] text = lineSegment.array;
		int off = lineSegment.offset;

		Toolkit toolkit = painter.getToolkit();
		Font defaultFont = painter.getFont();
		SyntaxStyle[] styles = painter.getStyles();

		float[] widthArray = new float[] { horizontalOffset };

		for(;;)
		{
			byte id = tokens.id;
			if(id == Token.END)
				return lineSegment.count;

			Font font;
			if(id == Token.NULL)
				font = defaultFont;
			else
				font = styles[id].getFont();

			int len = tokens.length;

			int offset = renderer.xToOffset(text,off,len,font,x,
				painter,round,widthArray);

			if(offset != -1)
				return offset - lineSegment.offset;

			off += len;
			tokens = tokens.next;
		}
	} //}}}

	//{{{ xyToOffset() method
	/**
	 * Converts a point to an offset, from the start of the text.
	 * @param x The x co-ordinate of the point
	 * @param y The y co-ordinate of the point
	 */
	public int xyToOffset(int x, int y)
	{
		return xyToOffset(x,y,true);
	} //}}}

	//{{{ xyToOffset() method
	/**
	 * Converts a point to an offset, from the start of the text.
	 * @param x The x co-ordinate of the point
	 * @param y The y co-ordinate of the point
	 * @param round Round up to next letter if past the middle of a letter?
	 * @since jEdit 3.2pre6
	 */
	public int xyToOffset(int x, int y, boolean round)
	{
		FontMetrics fm = painter.getFontMetrics();
		int height = fm.getHeight();
		int line = y / height + firstLine;

		if(line < 0)
			return 0;
		else if(line >= getVirtualLineCount())
		{
			// WRONG!!!
			// return getBufferLength();
			return getLineEndOffset(virtualToPhysical(
				getVirtualLineCount() - 1)) - 1;
		}
		else
		{
			line = virtualToPhysical(line);
			return getLineStartOffset(line) + xToOffset(line,x);
		}
	} //}}}

	//}}}

	//{{{ Painting

	//{{{ invalidateLine() method
	/**
	 * Marks a line as needing a repaint.
	 * @param line The physical line to invalidate
	 */
	public void invalidateLine(int line)
	{
		if(line < physFirstLine)
			return;

		int lineCount = buffer.getLineCount();
		int virtualLineCount = foldVisibilityManager
			.getVirtualLineCount();

		if(line >= lineCount)
			line = (line - lineCount) + virtualLineCount;
		else
			line = foldVisibilityManager.physicalToVirtual(line);

		if(line > firstLine + visibleLines + 1)
			return;

		FontMetrics fm = painter.getFontMetrics();
		int y = lineToY(line) + fm.getDescent() + fm.getLeading();
		painter.repaint(0,y,painter.getWidth(),fm.getHeight());
		gutter.repaint(0,y,gutter.getWidth(),fm.getHeight());
	} //}}}

	//{{{ invalidateLineRange() method
	/**
	 * Marks a range of physical lines as needing a repaint.
	 * @param firstLine The first line to invalidate
	 * @param lastLine The last line to invalidate
	 */
	public void invalidateLineRange(int firstLine, int lastLine)
	{
		if(lastLine < firstLine)
		{
			int tmp = lastLine;
			lastLine = firstLine;
			firstLine = tmp;
		}

		if(lastLine < physFirstLine)
			return;

		int lineCount = buffer.getLineCount();
		int virtualLineCount = foldVisibilityManager
			.getVirtualLineCount();

		if(firstLine >= lineCount)
			firstLine = (firstLine - lineCount) + virtualLineCount;
		else
			firstLine = foldVisibilityManager.physicalToVirtual(firstLine);

		if(firstLine > firstLine + visibleLines + 1)
			return;

		if(lastLine >= lineCount)
			lastLine = (lastLine - lineCount) + virtualLineCount;
		else
			lastLine = foldVisibilityManager.physicalToVirtual(lastLine);

		invalidateVirtualLineRange(firstLine,lastLine);
	} //}}}

	//{{{ invalidateSelectedLines() method
	/**
	 * Repaints the lines containing the selection.
	 */
	public void invalidateSelectedLines()
	{
		// to hide line highlight if selections are being added later on
		invalidateLine(caretLine);

		for(int i = 0; i < selection.size(); i++)
		{
			Selection s = (Selection)selection.elementAt(i);
			invalidateLineRange(s.startLine,s.endLine);
		}
	} //}}}

	//{{{ invalidateVirtualLineRange() method
	/**
	 * Marks a range of virtual lines as needing a repaint.
	 * @param firstLine The first line to invalidate
	 * @param lastLine The last line to invalidate
	 */
	public void invalidateVirtualLineRange(int firstLine, int lastLine)
	{
		FontMetrics fm = painter.getFontMetrics();
		int y = lineToY(firstLine) + fm.getDescent() + fm.getLeading();
		int height = (lastLine - firstLine + 1) * fm.getHeight();
		painter.repaint(0,y,painter.getWidth(),height);
		gutter.repaint(0,y,gutter.getWidth(),height);
	} //}}}


	//}}}

	//{{{ Convenience methods

	//{{{ physicalToVirtual() method
	/**
	 * Converts a physical line number to a virtual line number.
	 * @param line A physical line index
	 * @since jEdit 4.0pre1
	 */
	public int physicalToVirtual(int line)
	{
		return foldVisibilityManager.physicalToVirtual(line);
	} //}}}

	//{{{ virtualToPhysical() method
	/**
	 * Converts a virtual line number to a physical line number.
	 * @param line A virtual line index
	 * @since jEdit 4.0pre1
	 */
	public int virtualToPhysical(int line)
	{
		return foldVisibilityManager.virtualToPhysical(line);
	} //}}}

	//{{{ getBufferLength() method
	/**
	 * Returns the length of the buffer.
	 */
	public final int getBufferLength()
	{
		return buffer.getLength();
	} //}}}

	//{{{ getLineCount() method
	/**
	 * Returns the number of physical lines in the buffer.
	 */
	public final int getLineCount()
	{
		return buffer.getLineCount();
	} //}}}

	//{{{ getVirtualLineCount() method
	/**
	 * Returns the number of virtual lines in the buffer.
	 */
	public final int getVirtualLineCount()
	{
		return foldVisibilityManager.getVirtualLineCount();
	} //}}}

	//{{{ getLineOfOffset() method
	/**
	 * Returns the line containing the specified offset.
	 * @param offset The offset
	 */
	public final int getLineOfOffset(int offset)
	{
		return buffer.getLineOfOffset(offset);
	} //}}}

	//{{{ getLineStartOffset() method
	/**
	 * Returns the start offset of the specified line.
	 * @param line The line
	 * @return The start offset of the specified line, or -1 if the line is
	 * invalid
	 */
	public int getLineStartOffset(int line)
	{
		return buffer.getLineStartOffset(line);
	} //}}}

	//{{{ getLineEndOffset() method
	/**
	 * Returns the end offset of the specified line.
	 * @param line The line
	 * @return The end offset of the specified line, or -1 if the line is
	 * invalid.
	 */
	public int getLineEndOffset(int line)
	{
		return buffer.getLineEndOffset(line);
	} //}}}

	//{{{ getLineLength() method
	/**
	 * Returns the length of the specified line.
	 * @param line The line
	 */
	public int getLineLength(int line)
	{
		return buffer.getLineLength(line);
	} //}}}

	//{{{ getText() method
	/**
	 * Returns the specified substring of the buffer.
	 * @param start The start offset
	 * @param len The length of the substring
	 * @return The substring
	 */
	public final String getText(int start, int len)
	{
		return buffer.getText(start,len);
	} //}}}

	//{{{ getText() method
	/**
	 * Copies the specified substring of the buffer into a segment.
	 * @param start The start offset
	 * @param len The length of the substring
	 * @param segment The segment
	 */
	public final void getText(int start, int len, Segment segment)
	{
		buffer.getText(start,len,segment);
	} //}}}

	//{{{ getLineText() method
	/**
	 * Returns the text on the specified line.
	 * @param lineIndex The line
	 * @return The text, or null if the line is invalid
	 */
	public final String getLineText(int lineIndex)
	{
		return buffer.getLineText(lineIndex);
	} //}}}

	//{{{ getLineText() method
	/**
	 * Copies the text on the specified line into a segment. If the line
	 * is invalid, the segment will contain a null string.
	 * @param lineIndex The line
	 */
	public final void getLineText(int lineIndex, Segment segment)
	{
		buffer.getLineText(lineIndex,segment);
	} //}}}

	//{{{ getText() method
	/**
	 * Returns the entire text of this text area.
	 */
	public String getText()
	{
		return buffer.getText(0,buffer.getLength());
	} //}}}

	//{{{ setText() method
	/**
	 * Sets the entire text of this text area.
	 */
	public void setText(String text)
	{
		try
		{
			buffer.beginCompoundEdit();
			buffer.remove(0,buffer.getLength());
			buffer.insert(0,text);
		}
		finally
		{
			buffer.endCompoundEdit();
		}
	} //}}}

	//}}}

	//{{{ Selection

	//{{{ selectAll() method
	/**
	 * Selects all text in the buffer.
	 */
	public final void selectAll()
	{
		setSelection(new Selection.Range(0,buffer.getLength()));
		moveCaretPosition(buffer.getLength(),true);
	} //}}}

	//{{{ selectLine() method
	/**
	 * Selects the current line.
	 * @since jEdit 2.7pre2
	 */
	public void selectLine()
	{
		int caretLine = getCaretLine();
		int start = getLineStartOffset(caretLine);
		int end = getLineEndOffset(caretLine) - 1;
		Selection s = new Selection.Range(start,end);
		if(multi)
			addToSelection(s);
		else
			setSelection(s);
		moveCaretPosition(end);
	} //}}}

	//{{{ selectParagraph() method
	/**
	 * Selects the paragraph at the caret position.
	 * @since jEdit 2.7pre2
	 */
	public void selectParagraph()
	{
		int caretLine = getCaretLine();

		if(getLineLength(caretLine) == 0)
		{
			view.getToolkit().beep();
			return;
		}

		int start = caretLine;
		int end = caretLine;

		while(start >= 0)
		{
			if(getLineLength(start) == 0)
				break;
			else
				start--;
		}

		while(end < getLineCount())
		{
			if(getLineLength(end) == 0)
				break;
			else
				end++;
		}

		int selectionStart = getLineStartOffset(start + 1);
		int selectionEnd = getLineEndOffset(end - 1) - 1;
		Selection s = new Selection.Range(selectionStart,selectionEnd);
		if(multi)
			addToSelection(s);
		else
			setSelection(s);
		moveCaretPosition(selectionEnd);
	} //}}}

	//{{{ selectWord() method
	/**
	 * Selects the word at the caret position.
	 * @since jEdit 2.7pre2
	 */
	public void selectWord()
	{
		int line = getCaretLine();
		int lineStart = getLineStartOffset(line);
		int offset = getCaretPosition() - lineStart;

		if(getLineLength(line) == 0)
			return;

		String lineText = getLineText(line);
		String noWordSep = buffer.getStringProperty("noWordSep");

		if(offset == getLineLength(line))
			offset--;

		int wordStart = TextUtilities.findWordStart(lineText,offset,noWordSep);
		int wordEnd = TextUtilities.findWordEnd(lineText,offset+1,noWordSep);

		Selection s = new Selection.Range(lineStart + wordStart,
			lineStart + wordEnd);
		if(multi)
			addToSelection(s);
		else
			setSelection(s);
		moveCaretPosition(lineStart + wordEnd);
	} //}}}

	//{{{ selectToMatchingBracket() method
	/**
	 * Selects from the bracket at the caret position to the corresponding
	 * bracket.
	 * @since jEdit 4.0pre2
	 */
	public void selectToMatchingBracket()
	{
		int bracket = TextUtilities.findMatchingBracket(buffer,caretLine,
			Math.max(0,caret - buffer.getLineStartOffset(caretLine) - 1));

		if(bracket != -1)
		{
			Selection s;

			if(bracket < caret)
				s = new Selection.Range(bracket + 1,caret - 1);
			else
				s = new Selection.Range(caret,bracket);
			if(multi)
				addToSelection(s);
			else
				setSelection(s);
			return;
		}
	} //}}}

	//{{{ selectBlock() method
	/**
	 * Selects the code block surrounding the caret.
	 * @since jEdit 2.7pre2
	 */
	public void selectBlock()
	{
		String openBrackets = "([{";
		String closeBrackets = ")]}";

		Selection s = getSelectionAtOffset(caret);
		int start, end;
		if(s == null)
			start = end = caret;
		else
		{
			start = s.start;
			end = s.end;
		}

		String text = getText(0,buffer.getLength());

		// Scan backwards, trying to find a bracket
		int count = 1;
		char openBracket = '\0';
		char closeBracket = '\0';

		// We can't do the backward scan if start == 0
		if(start == 0)
		{
			view.getToolkit().beep();
			return;
		}

backward_scan:	while(--start > 0)
		{
			char c = text.charAt(start);
			int index = openBrackets.indexOf(c);
			if(index != -1)
			{
				if(--count == 0)
				{
					openBracket = c;
					closeBracket = closeBrackets.charAt(index);
					break backward_scan;
				}
			}
			else if(closeBrackets.indexOf(c) != -1)
				count++;
		}

		// Reset count
		count = 1;

		// Scan forward, matching that bracket
		if(openBracket == '\0')
		{
			getToolkit().beep();
			return;
		}
		else
		{
forward_scan:		do
			{
				char c = text.charAt(end);
				if(c == closeBracket)
				{
					if(--count == 0)
					{
						end++;
						break forward_scan;
					}
				}
				else if(c == openBracket)
					count++;
			}
			while(++end < buffer.getLength());
		}

		s = new Selection.Range(start,end);
		if(multi)
			addToSelection(s);
		else
			setSelection(s);
		moveCaretPosition(end);
	} //}}}

	//{{{ invertSelection() method
	/**
	 * Inverts the selection.
	 * @since jEdit 4.0pre1
	 */
	public final void invertSelection()
	{
		Selection[] newSelection = new Selection[selection.size() + 1];
		int lastOffset = 0;
		for(int i = 0; i < selection.size(); i++)
		{
			Selection s = (Selection)selection.elementAt(i);
			newSelection[i] = new Selection.Range(lastOffset,
				s.getStart());
			lastOffset = s.getEnd();
		}
		newSelection[selection.size()] = new Selection.Range(
			lastOffset,buffer.getLength());
		setSelection(newSelection);
	} //}}}

	//{{{ getSelectionCount() method
	/**
	 * Returns the number of selections. This is primarily for use by the
	 * the status bar.
	 * @since jEdit 3.2pre2
	 */
	public int getSelectionCount()
	{
		return selection.size();
	} //}}}

	//{{{ getSelection() method
	/**
	 * Returns the current selection.
	 * @since jEdit 3.2pre1
	 */
	public Selection[] getSelection()
	{
		Selection[] sel = new Selection[selection.size()];
		selection.copyInto(sel);
		return sel;
	} //}}}

	//{{{ selectNone() method
	/**
	 * Deselects everything.
	 */
	public void selectNone()
	{
		setSelection((Selection)null);
	} //}}}

	//{{{ setSelection() method
	/**
	 * Sets the selection.
	 * @param selection The new selection
	 * since jEdit 3.2pre1
	 */
	public void setSelection(Selection[] selection)
	{
		// invalidate the old selection
		invalidateSelectedLines();

		this.selection.removeAllElements();

		if(selection != null)
		{
			for(int i = 0; i < selection.length; i++)
				_addToSelection(selection[i]);
		}

		fireCaretEvent();
	} //}}}

	//{{{ setSelection() method
	/**
	 * Sets the selection.
	 * @param selection The new selection
	 * since jEdit 3.2pre1
	 */
	public void setSelection(Selection selection)
	{
		invalidateSelectedLines();
		this.selection.removeAllElements();

		if(selection != null)
			_addToSelection(selection);

		fireCaretEvent();
	} //}}}

	//{{{ addToSelection() method
	/**
	 * Adds to the selection.
	 * @param selection The new selection
	 * since jEdit 3.2pre1
	 */
	public void addToSelection(Selection[] selection)
	{
		if(selection != null)
		{
			for(int i = 0; i < selection.length; i++)
				_addToSelection(selection[i]);
		}

		// to hide current line highlight
		invalidateLine(caretLine);

		fireCaretEvent();
	} //}}}

	//{{{ addToSelection() method
	/**
	 * Adds to the selection.
	 * @param selection The new selection
	 * since jEdit 3.2pre1
	 */
	public void addToSelection(Selection selection)
	{
		_addToSelection(selection);

		// to hide current line highlight
		invalidateLine(caretLine);

		fireCaretEvent();
	} //}}}

	//{{{ getSelectionAtOffset() method
	/**
	 * Returns the selection containing the specific offset, or null
	 * if there is no selection at that offset.
	 * @param offset The offset
	 * @since jEdit 3.2pre1
	 */
	public Selection getSelectionAtOffset(int offset)
	{
		if(selection != null)
		{
			for(int i = 0; i < selection.size(); i++)
			{
				Selection s = (Selection)selection.elementAt(i);
				if(offset >= s.start && offset <= s.end)
					return s;
			}
		}

		return null;
	} //}}}

	//{{{ removeFromSelection() method
	/**
	 * Deactivates the specified selection.
	 * @param s The selection
	 * @since jEdit 3.2pre1
	 */
	public void removeFromSelection(Selection sel)
	{
		selection.removeElement(sel);
		invalidateLineRange(sel.startLine,sel.endLine);

		// to hide current line highlight
		invalidateLine(caretLine);

		fireCaretEvent();
	} //}}}

	//{{{ removeFromSelection() method
	/**
	 * Deactivates the selection at the specified offset. If there is
	 * no selection at that offset, does nothing.
	 * @param offset The offset
	 * @since jEdit 3.2pre1
	 */
	public void removeFromSelection(int offset)
	{
		Selection sel = getSelectionAtOffset(offset);
		if(sel == null)
			return;

		selection.removeElement(sel);
		invalidateLineRange(sel.startLine,sel.endLine);

		// to hide current line highlight
		invalidateLine(caretLine);

		fireCaretEvent();
	} //}}}

	//{{{ resizeSelection() method
	/**
	 * Resizes the selection at the specified offset, or creates a new
	 * one if there is no selection at the specified offset. This is a
	 * utility method that is mainly useful in the mouse event handler
	 * because it handles the case of end being before offset gracefully
	 * (unlike the rest of the selection API).
	 * @param offset The offset
	 * @param end The new selection end
	 * @param rect Make the selection rectangular?
	 * @since jEdit 3.2pre1
	 */
	public void resizeSelection(int offset, int end, boolean rect)
	{
		Selection s = getSelectionAtOffset(offset);
		if(s != null)
		{
			invalidateLineRange(s.startLine,s.endLine);
			selection.removeElement(s);
		}

		if(end < offset)
		{
			int tmp = offset;
			offset = end;
			end = tmp;
		}

		Selection newSel;
		if(rect)
			newSel = new Selection.Rect(offset,end);
		else
			newSel = new Selection.Range(offset,end);

		_addToSelection(newSel);
		fireCaretEvent();
	} //}}}

	//{{{ extendSelection() method
	/**
	 * Extends the selection at the specified offset, or creates a new
	 * one if there is no selection at the specified offset. This is
	 * different from resizing in that the new chunk is added to the
	 * selection in question, instead of replacing it.
	 * @param offset The offset
	 * @param end The new selection end
	 * @param rect Make the selection rectangular?
	 * @since jEdit 3.2pre1
	 */
	public void extendSelection(int offset, int end)
	{
		Selection s = getSelectionAtOffset(offset);
		if(s != null)
		{
			invalidateLineRange(s.startLine,s.endLine);
			selection.removeElement(s);

			if(offset == s.start)
			{
				offset = end;
				end = s.end;
			}
			else if(offset == s.end)
			{
				offset = s.start;
			}
		}

		if(end < offset)
		{
			int tmp = end;
			end = offset;
			offset = tmp;
		}

		_addToSelection(new Selection.Range(offset,end));
		fireCaretEvent();
	} //}}}

	//{{{ getSelectedText() method
	/**
	 * Returns the text in the specified selection.
	 * @param s The selection
	 * @since jEdit 3.2pre1
	 */
	public String getSelectedText(Selection s)
	{
		StringBuffer buf = new StringBuffer();
		getSelectedText(s,buf);
		return buf.toString();
	} //}}}

	//{{{ getSelectedText() method
	/**
	 * Returns the text in all active selections.
	 * @param separator The string to insert between each text chunk
	 * (for example, a newline)
	 * @since jEdit 3.2pre1
	 */
	public String getSelectedText(String separator)
	{
		if(selection.size() == 0)
			return null;

		StringBuffer buf = new StringBuffer();
		for(int i = 0; i < selection.size(); i++)
		{
			if(i != 0)
				buf.append(separator);

			getSelectedText((Selection)selection.elementAt(i),buf);
		}

		return buf.toString();
	} //}}}

	//{{{ getSelectedText() method
	/**
	 * Returns the text in all active selections, with a newline
	 * between each text chunk.
	 */
	public String getSelectedText()
	{
		return getSelectedText("\n");
	} //}}}

	//{{{ setSelectedText() method
	/**
	 * Replaces the selection with the specified text.
	 * @param s The selection
	 * @param selectedText The new text
	 * @since jEdit 3.2pre1
	 */
	public void setSelectedText(Selection s, String selectedText)
	{
		if(!isEditable())
		{
			throw new InternalError("Text component"
				+ " read only");
		}

		try
		{
			buffer.beginCompoundEdit();

			if(s instanceof Selection.Rect)
			{
				int start = s.start - getLineStartOffset(s.startLine);
				int end = s.end - getLineStartOffset(s.endLine);

				// Certain rectangles satisfy this condition...
				if(end < start)
				{
					int tmp = end;
					end = start;
					start = tmp;
				}

				int lastNewline = 0;
				int currNewline = 0;

				for(int i = s.startLine; i <= s.endLine; i++)
				{
					int lineStart = getLineStartOffset(i);
					int lineEnd = getLineEndOffset(i) - 1;
					int rectStart = Math.min(lineEnd,lineStart + start);

					buffer.remove(rectStart,Math.min(lineEnd - rectStart,
						end - start));

					if(selectedText == null)
						continue;

					currNewline = selectedText.indexOf('\n',lastNewline);
					if(currNewline == -1)
						currNewline = selectedText.length();

					buffer.insert(rectStart,selectedText
						.substring(lastNewline,currNewline));

					lastNewline = Math.min(selectedText.length(),
						currNewline + 1);
				}

				if(selectedText != null &&
					currNewline != selectedText.length())
				{
					int offset = getLineEndOffset(s.endLine) - 1;
					buffer.insert(offset,"\n");
					buffer.insert(offset + 1,selectedText
						.substring(currNewline + 1));
				}
			}
			else
			{
				buffer.remove(s.start,s.end - s.start);
				if(selectedText != null && selectedText.length() != 0)
				{
					buffer.insert(s.start,selectedText);
				}
			}
		}
		// No matter what happends... stops us from leaving buffer
		// in a bad state
		finally
		{
			buffer.endCompoundEdit();
		}

		// no no no!!!!
		//selectNone();
	} //}}}

	//{{{ setSelectedText() method
	/**
	 * Replaces the selection at the caret with the specified text.
	 * If there is no selection at the caret, the text is inserted at
	 * the caret position.
	 */
	public void setSelectedText(String selectedText)
	{
		if(!isEditable())
		{
			throw new InternalError("Text component"
				+ " read only");
		}

		Selection[] selection = getSelection();
		if(selection.length == 0)
		{
			// for compatibility with older jEdit versions
			buffer.insert(caret,selectedText);
		}
		else
		{
			try
			{
				buffer.beginCompoundEdit();

				for(int i = 0; i < selection.length; i++)
				{
					setSelectedText(selection[i],selectedText);
				}
			}
			finally
			{
				buffer.endCompoundEdit();
			}
		}

		selectNone();
	} //}}}

	//{{{ getSelectedLines() method
	/**
	 * Returns an array of all line numbers that contain a selection.
	 * This array will also include the line number containing the
	 * caret, for convinience.
	 * @since jEdit 3.2pre1
	 */
	public int[] getSelectedLines()
	{
		if(selection.size() == 0)
			return new int[] { caretLine };

		Integer line;

		Hashtable hash = new Hashtable();
		for(int i = 0; i < selection.size(); i++)
		{
			Selection s = (Selection)selection.elementAt(i);
			int endLine = (s.end == getLineStartOffset(s.endLine)
				? s.endLine - 1
				: s.endLine);

			for(int j = s.startLine; j <= endLine; j++)
			{
				line = new Integer(j);
				hash.put(line,line);
			}
		}

		int[] returnValue = new int[hash.size()];
		int i = 0;

		Enumeration keys = hash.keys();
		while(keys.hasMoreElements())
		{
			line = (Integer)keys.nextElement();
			returnValue[i++] = line.intValue();
		}

		quicksort(returnValue,0,returnValue.length - 1);

		return returnValue;
	} //}}}

	//{{{ showSelectLineRangeDialog() method
	/**
	 * Displays the 'select line range' dialog box, and selects the
	 * specified range of lines.
	 * @since jEdit 2.7pre2
	 */
	public void showSelectLineRangeDialog()
	{
		new SelectLineRange(view);
	} //}}}

	//}}}

	//{{{ Old selection API

	//{{{ getSelectionStart() method
	/**
	 * @deprecated Instead, obtain a Selection instance using
	 * any means, and call its <code>getStart()</code> method
	 */
	public final int getSelectionStart()
	{
		if(selection.size() != 1)
			return caret;

		return ((Selection)selection.elementAt(0)).getStart();
	} //}}}

	//{{{ getSelectionStart() method
	/**
	 * @deprecated Instead, obtain a Selection instance using
	 * any means, and call its <code>getStart(int)</code> method
	 */
	public int getSelectionStart(int line)
	{
		if(selection.size() != 1)
			return caret;

		return ((Selection)selection.elementAt(0)).getStart(
			buffer,line);
	} //}}}

	//{{{ getSelectionStartLine() method
	/**
	 * @deprecated Instead, obtain a Selection instance using
	 * any means, and call its <code>getStartLine()</code> method
	 */
	public final int getSelectionStartLine()
	{
		if(selection.size() != 1)
			return caret;

		return ((Selection)selection.elementAt(0)).getStartLine();
	} //}}}

	//{{{ setSelectionStart() method
	/**
	 * @deprecated Do not use.
	 */
	public final void setSelectionStart(int selectionStart)
	{
		select(selectionStart,getSelectionEnd(),true);
	} //}}}

	//{{{ getSelectionEnd() method
	/**
	 * @deprecated Instead, obtain a Selection instance using
	 * any means, and call its <code>getEnd()</code> method
	 */
	public final int getSelectionEnd()
	{
		if(selection.size() != 1)
			return caret;

		return ((Selection)selection.elementAt(0)).getEnd();
	} //}}}

	//{{{ getSelectionEnd() method
	/**
	 * @deprecated Instead, obtain a Selection instance using
	 * any means, and call its <code>getEnd(int)</code> method
	 */
	public int getSelectionEnd(int line)
	{
		if(selection.size() != 1)
			return caret;

		return ((Selection)selection.elementAt(0)).getEnd(
			buffer,line);
	} //}}}

	//{{{ getSelectionEndLine() method
	/**
	 * @deprecated Instead, obtain a Selection instance using
	 * any means, and call its <code>getEndLine()</code> method
	 */
	public final int getSelectionEndLine()
	{
		if(selection.size() != 1)
			return caret;

		return ((Selection)selection.elementAt(0)).getEndLine();
	} //}}}

	//{{{ setSelectionEnd() method
	/**
	 * @deprecated Do not use.
	 */
	public final void setSelectionEnd(int selectionEnd)
	{
		select(getSelectionStart(),selectionEnd,true);
	} //}}}

	//{{{ getMarkPosition() method
	/**
	 * @deprecated Do not use.
	 */
	public final int getMarkPosition()
	{
		Selection s = getSelectionAtOffset(caret);
		if(s == null)
			return caret;

		if(s.start == caret)
			return s.end;
		else if(s.end == caret)
			return s.start;
		else
			return caret;
	} //}}}

	//{{{ getMarkLine() method
	/**
	 * @deprecated Do not use.
	 */
	public final int getMarkLine()
	{
		if(selection.size() != 1)
			return caretLine;

		Selection s = (Selection)selection.elementAt(0);
		if(s.start == caret)
			return s.endLine;
		else if(s.end == caret)
			return s.startLine;
		else
			return caretLine;
	} //}}}

	//{{{ select() method
	/**
	 * @deprecated Instead, call either <code>addToSelection()</code>,
	 * or <code>setSelection()</code> with a new Selection instance.
	 */
	public void select(int start, int end)
	{
		select(start,end,true);
	} //}}}

	//{{{ select() method
	/**
	 * @deprecated Instead, call either <code>addToSelection()</code>,
	 * or <code>setSelection()</code> with a new Selection instance.
	 */
	public void select(int start, int end, boolean doElectricScroll)
	{
		selectNone();

		int newStart, newEnd;
		if(start < end)
		{
			newStart = start;
			newEnd = end;
		}
		else
		{
			newStart = end;
			newEnd = start;
		}

		setSelection(new Selection.Range(newStart,newEnd));
		moveCaretPosition(end,doElectricScroll);
	} //}}}

	//{{{ isSelectionRectangular() method
	/**
	 * @deprecated Instead, check if the appropriate Selection
	 * is an instance of the Selection.Rect class.
	 */
	public boolean isSelectionRectangular()
	{
		Selection s = getSelectionAtOffset(caret);
		if(s == null)
			return false;
		else
			return (s instanceof Selection.Rect);
	} //}}}

	//}}}

	//{{{ Caret

	//{{{ blinkCaret() method
	/**
	 * Blinks the caret.
	 */
	public final void blinkCaret()
	{
		if(caretBlinks)
		{
			blink = !blink;
			invalidateLine(caretLine);
		}
		else
			blink = true;
	} //}}}

	//{{{ centerCaret() method
	/**
	 * Centers the caret on the screen.
	 * @since jEdit 2.7pre2
	 */
	public void centerCaret()
	{
		int gotoLine = virtualToPhysical(firstLine + visibleLines / 2);

		if(gotoLine < 0 || gotoLine >= getLineCount())
		{
			getToolkit().beep();
			return;
		}

		setCaretPosition(getLineStartOffset(gotoLine));
	} //}}}

	//{{{ setCaretPosition() method
	/**
	 * Sets the caret position and deactivates the selection.
	 * @param caret The caret position
	 */
	public void setCaretPosition(int newCaret)
	{
		invalidateSelectedLines();
		selection.removeAllElements();
		moveCaretPosition(newCaret,true);
	} //}}}

	//{{{ setCaretPosition() method
	/**
	 * Sets the caret position and deactivates the selection.
	 * @param caret The caret position
	 * @param doElectricScroll Do electric scrolling?
	 */
	public void setCaretPosition(int newCaret, boolean doElectricScroll)
	{
		invalidateSelectedLines();
		selection.removeAllElements();
		moveCaretPosition(newCaret,doElectricScroll);
	} //}}}

	//{{{ moveCaretPosition() method
	/**
	 * Sets the caret position without deactivating the selection.
	 * @param caret The caret position
	 */
	public void moveCaretPosition(int newCaret)
	{
		moveCaretPosition(newCaret,true);
	} //}}}

	//{{{ moveCaretPosition() method
	/**
	 * Sets the caret position without deactivating the selection.
	 * @param caret The caret position
	 * @param doElectricScroll Do electric scrolling?
	 */
	public void moveCaretPosition(int newCaret, boolean doElectricScroll)
	{
		if(newCaret < 0 || newCaret > buffer.getLength())
		{
			throw new IllegalArgumentException("caret out of bounds: "
				+ newCaret);
		}

		// When the user is typing, etc, we don't want the caret
		// to blink
		blink = true;
		caretTimer.restart();

		if(caret == newCaret)
		{
			// so that C+y <marker>, for example, will return
			// to the saved location even if the caret was
			// never moved but the user scrolled instead
			scrollToCaret(doElectricScroll);
			return;
		}

		int newCaretLine = getLineOfOffset(newCaret);

		magicCaret = -1;

		if(!foldVisibilityManager.isLineVisible(newCaretLine))
		{
			if(foldVisibilityManager.isNarrowed())
			{
				int collapseFolds = buffer.getIntegerProperty(
					"collapseFolds",0);
				if(collapseFolds != 0)
					foldVisibilityManager.expandFolds(collapseFolds);
				else
					foldVisibilityManager.expandAllFolds();
			}
			else
				foldVisibilityManager.expandFold(newCaretLine,false);
		}

		invalidateLineRange(caretLine,newCaretLine);

		caret = newCaret;
		caretLine = newCaretLine;

		if(focusedComponent == this)
			scrollToCaret(doElectricScroll);

		updateBracketHighlight();

		fireCaretEvent();
	} //}}}

	//{{{ getCaretPosition() method
	/**
	 * Returns the caret position.
	 */
	public int getCaretPosition()
	{
		return caret;
	} //}}}

	//{{{ getCaretLine() method
	/**
	 * Returns the line number containing the caret.
	 */
	public int getCaretLine()
	{
		return caretLine;
	} //}}}

	//{{{ getMagicCaretPosition() method
	/**
	 * Returns the `magic' caret position. This can be used to preserve
	 * the column position when moving up and down lines.
	 */
	public final int getMagicCaretPosition()
	{
		if(magicCaret == -1)
		{
			magicCaret = offsetToX(caretLine,caret
				- getLineStartOffset(caretLine));
		}

		return magicCaret;
	} //}}}

	//{{{ setMagicCaretPosition() method
	/**
	 * Sets the `magic' caret position. This can be used to preserve
	 * the column position when moving up and down lines.
	 * @param magicCaret The magic caret position
	 */
	public final void setMagicCaretPosition(int magicCaret)
	{
		this.magicCaret = magicCaret;
	} //}}}

	//{{{ addCaretListener() method
	/**
	 * Adds a caret change listener to this text area.
	 * @param listener The listener
	 */
	public final void addCaretListener(CaretListener listener)
	{
		listenerList.add(CaretListener.class,listener);
	} //}}}

	//{{{ removeCaretListener() method
	/**
	 * Removes a caret change listener from this text area.
	 * @param listener The listener
	 */
	public final void removeCaretListener(CaretListener listener)
	{
		listenerList.remove(CaretListener.class,listener);
	} //}}}

	//{{{ getBracketPosition() method
	/**
	 * Returns the position of the highlighted bracket (the bracket
	 * matching the one before the caret)
	 */
	public final int getBracketPosition()
	{
		return bracketPosition;
	} //}}}

	//{{{ getBracketLine() method
	/**
	 * Returns the line of the highlighted bracket (the bracket
	 * matching the one before the caret)
	 */
	public final int getBracketLine()
	{
		return bracketLine;
	} //}}}

	//{{{ goToNextBracket() method
	/**
	 * Moves the caret to the next closing bracket.
	 * @since jEdit 2.7pre2.
	 */
	public void goToNextBracket(boolean select)
	{
		String text = getText(caret,buffer.getLength() - caret - 1);

		int newCaret = -1;

loop:		for(int i = 0; i < text.length(); i++)
		{
			switch(text.charAt(i))
			{
			case ')': case ']': case '}':
				newCaret = caret + i + 1;
				break loop;
			}
		}

		if(newCaret == -1)
			getToolkit().beep();
		else
		{
			if(select)
				extendSelection(caret,newCaret);
			else if(!multi)
				selectNone();
			moveCaretPosition(newCaret);
		}
	} //}}}

	//{{{ goToNextCharacter() method
	/**
	 * Moves the caret to the next character.
	 * @since jEdit 2.7pre2.
	 */
	public void goToNextCharacter(boolean select)
	{
		if(!select && selection.size() != 0)
		{
			Selection s = getSelectionAtOffset(caret);
			if(s != null)
			{
				if(multi)
				{
					if(caret != s.end)
					{
						moveCaretPosition(s.end);
						return;
					}
				}
				else
				{
					setCaretPosition(s.end);
					return;
				}
			}
		}

		if(caret == buffer.getLength())
			getToolkit().beep();

		int newCaret;

		if(caret == getLineEndOffset(caretLine) - 1)
		{
			int line = foldVisibilityManager.getNextVisibleLine(caretLine);
			if(line == -1)
			{
				getToolkit().beep();
				return;
			}

			newCaret = getLineStartOffset(line);
		}
		else
			newCaret = caret + 1;

		if(select)
			extendSelection(caret,newCaret);
		else if(!multi)
			selectNone();

		moveCaretPosition(newCaret);
	} //}}}

	//{{{ goToNextLine() method
	/**
	 * Movse the caret to the next line.
	 * @since jEdit 2.7pre2
	 */
	public void goToNextLine(boolean select)
	{
		int caret = getCaretPosition();
		int line = getCaretLine();

		int magic = getMagicCaretPosition();

		int nextLine = foldVisibilityManager.getNextVisibleLine(line);

		if(nextLine == -1)
		{
			getToolkit().beep();
			return;
		}

		int newCaret = getLineStartOffset(nextLine)
			+ xToOffset(nextLine,magic + 1);
		if(select)
			extendSelection(caret,newCaret);
		else if(!multi)
			selectNone();

		moveCaretPosition(newCaret);
		setMagicCaretPosition(magic);
	} //}}}

	//{{{ goToNextMarker() method
	/**
	 * Moves the caret to the next marker.
	 * @since jEdit 2.7pre2
	 */
	public void goToNextMarker(boolean select)
	{
		Vector markers = buffer.getMarkers();
		Marker marker = null;

		for(int i = 0; i < markers.size(); i++)
		{
			Marker _marker = (Marker)markers.elementAt(i);
			if(_marker.getPosition() > caret)
			{
				marker = _marker;
				break;
			}
		}

		if(marker == null)
			getToolkit().beep();
		else
		{
			if(select)
				extendSelection(caret,marker.getPosition());
			else if(!multi)
				selectNone();
			moveCaretPosition(marker.getPosition());
		}
	} //}}}

	//{{{ goToNextPage() method
	/**
	 * Moves the caret to the next screenful.
	 * @since jEdit 2.7pre2.
	 */
	public void goToNextPage(boolean select)
	{
		int lineCount = getVirtualLineCount();

		int magic = getMagicCaretPosition();

		if(firstLine + visibleLines * 2 >= lineCount - 1)
			setFirstLine(lineCount - visibleLines);
		else
			setFirstLine(firstLine + visibleLines);

		int newLine = virtualToPhysical(Math.min(lineCount - 1,
			physicalToVirtual(caretLine) + visibleLines));
		int newCaret = getLineStartOffset(newLine)
			+ xToOffset(newLine,magic + 1);

		if(select)
			extendSelection(caret,newCaret);
		else if(!multi)
			selectNone();
		moveCaretPosition(newCaret);

		setMagicCaretPosition(magic);
	} //}}}

	//{{{ goToNextParagraph() method
	/**
	 * Moves the caret to the start of the next paragraph.
	 * @since jEdit 2.7pre2
	 */
	public void goToNextParagraph(boolean select)
	{
		int lineNo = getCaretLine();

		int newCaret = getBufferLength();

		boolean foundBlank = false;

loop:		for(int i = lineNo + 1; i < getLineCount(); i++)
		{
			if(!foldVisibilityManager.isLineVisible(i))
				continue;

			getLineText(i,lineSegment);

			for(int j = 0; j < lineSegment.count; j++)
			{
				switch(lineSegment.array[lineSegment.offset + j])
				{
				case ' ':
				case '\t':
					break;
				default:
					if(foundBlank)
					{
						newCaret = getLineStartOffset(i);
						break loop;
					}
					else
						continue loop;
				}
			}

			foundBlank = true;
		}

		if(select)
			extendSelection(caret,newCaret);
		else if(!multi)
			selectNone();
		moveCaretPosition(newCaret);
	} //}}}

	//{{{ goToNextWord() method
	/**
	 * Moves the caret to the start of the next word.
	 * @since jEdit 2.7pre2
	 */
	public void goToNextWord(boolean select)
	{
		int lineStart = getLineStartOffset(caretLine);
		int newCaret = caret - lineStart;
		String lineText = getLineText(caretLine);

		if(newCaret == lineText.length())
		{
			int nextLine = foldVisibilityManager.getNextVisibleLine(caretLine);
			if(nextLine == -1)
			{
				getToolkit().beep();
				return;
			}

			newCaret = getLineStartOffset(nextLine);
		}
		else
		{
			String noWordSep = buffer.getStringProperty("noWordSep");
			newCaret = TextUtilities.findWordEnd(lineText,newCaret + 1,noWordSep,true)
				+ lineStart;
		}

		if(select)
			extendSelection(caret,newCaret);
		else if(!multi)
			selectNone();
		moveCaretPosition(newCaret);
	} //}}}

	//{{{ goToPrevBracket() method
	/**
	 * Moves the caret to the previous bracket.
	 * @since jEdit 2.7pre2
	 */
	public void goToPrevBracket(boolean select)
	{
		String text = getText(0,caret);

		int newCaret = -1;

loop:		for(int i = getCaretPosition() - 1; i >= 0; i--)
		{
			switch(text.charAt(i))
			{
			case '(': case '[': case '{':
				newCaret = i;
				break loop;
			}
		}

		if(newCaret == -1)
			getToolkit().beep();
		else
		{
			if(select)
				extendSelection(caret,newCaret);
			else if(!multi)
				selectNone();
			moveCaretPosition(newCaret);
		}
	} //}}}

	//{{{ goToPrevCharacter() method
	/**
	 * Moves the caret to the previous character.
	 * @since jEdit 2.7pre2.
	 */
	public void goToPrevCharacter(boolean select)
	{
		if(!select && selection.size() != 0)
		{
			Selection s = getSelectionAtOffset(caret);
			if(s != null)
			{
				if(multi)
				{
					if(caret != s.start)
					{
						moveCaretPosition(s.start);
						return;
					}
				}
				else
				{
					setCaretPosition(s.start);
					return;
				}
			}
		}

		int newCaret;

		if(caret == getLineStartOffset(caretLine))
		{
			int line = foldVisibilityManager.getPrevVisibleLine(caretLine);
			if(line == -1)
			{
				getToolkit().beep();
				return;
			}
			newCaret = getLineEndOffset(line) - 1;
		}
		else
			newCaret = caret - 1;

		if(select)
			extendSelection(caret,newCaret);
		else if(!multi)
			selectNone();
		moveCaretPosition(newCaret);
	} //}}}

	//{{{ goToPrevLine() method
	/**
	 * Moves the caret to the previous line.
	 * @since jEdit 2.7pre2
	 */
	public void goToPrevLine(boolean select)
	{
		int magic = getMagicCaretPosition();

		int prevLine = foldVisibilityManager.getPrevVisibleLine(caretLine);
		if(prevLine == -1)
		{
			getToolkit().beep();
			return;
		}

		int newCaret = getLineStartOffset(prevLine) + xToOffset(prevLine,magic + 1);
		if(select)
			extendSelection(caret,newCaret);
		else if(!multi)
			selectNone();

		moveCaretPosition(newCaret);
		setMagicCaretPosition(magic);
	} //}}}

	//{{{ goToPrevMarker() method
	/**
	 * Moves the caret to the previous marker.
	 * @since jEdit 2.7pre2
	 */
	public void goToPrevMarker(boolean select)
	{
		Vector markers = buffer.getMarkers();
		Marker marker = null;
		for(int i = markers.size() - 1; i >= 0; i--)
		{
			Marker _marker = (Marker)markers.elementAt(i);
			if(_marker.getPosition() < caret)
			{
				marker = _marker;
				break;
			}
		}

		if(marker == null)
			getToolkit().beep();
		else
		{
			if(select)
				extendSelection(caret,marker.getPosition());
			else if(!multi)
				selectNone();
			moveCaretPosition(marker.getPosition());
		}
	} //}}}

	//{{{ goToPrevPage() method
	/**
	 * Moves the caret to the previous screenful.
	 * @since jEdit 2.7pre2
	 */
	public void goToPrevPage(boolean select)
	{
		if(firstLine < visibleLines)
			setFirstLine(0);
		else
			setFirstLine(firstLine - visibleLines);

		int magic = getMagicCaretPosition();

		int newLine = virtualToPhysical(Math.max(0,
			physicalToVirtual(caretLine) - visibleLines));
		int newCaret = getLineStartOffset(newLine)
			+ xToOffset(newLine,magic + 1);

		if(select)
			extendSelection(caret,newCaret);
		else if(!multi)
			selectNone();

		moveCaretPosition(newCaret);
		setMagicCaretPosition(magic);
	} //}}}

	//{{{ goToPrevParagraph() method
	/**
	 * Moves the caret to the start of the previous paragraph.
	 * @since jEdit 2.7pre2
	 */
	public void goToPrevParagraph(boolean select)
	{
		int lineNo = caretLine;
		int newCaret = 0;

		boolean foundBlank = false;

loop:		for(int i = lineNo - 1; i >= 0; i--)
		{
			if(!foldVisibilityManager.isLineVisible(i))
				continue;

			getLineText(i,lineSegment);

			for(int j = 0; j < lineSegment.count; j++)
			{
				switch(lineSegment.array[lineSegment.offset + j])
				{
				case ' ':
				case '\t':
					break;
				default:
					if(foundBlank)
					{
						newCaret = getLineEndOffset(i) - 1;
						break loop;
					}
					else
						continue loop;
				}
			}

			foundBlank = true;
		}

		if(select)
			extendSelection(caret,newCaret);
		else if(!multi)
			selectNone();
		moveCaretPosition(newCaret);
	} //}}}

	//{{{ goToPrevWord() method
	/**
	 * Moves the caret to the start of the previous word.
	 * @since jEdit 2.7pre2
	 */
	public void goToPrevWord(boolean select)
	{
		int lineStart = getLineStartOffset(caretLine);
		int newCaret = caret - lineStart;
		String lineText = getLineText(caretLine);

		if(newCaret == 0)
		{
			if(lineStart == 0)
			{
				view.getToolkit().beep();
				return;
			}
			else
			{
				int prevLine = foldVisibilityManager.getPrevVisibleLine(caretLine);
				if(prevLine == -1)
				{
					getToolkit().beep();
					return;
				}

				newCaret = getLineEndOffset(prevLine) - 1;
			}
		}
		else
		{
			String noWordSep = buffer.getStringProperty("noWordSep");
			newCaret = TextUtilities.findWordStart(lineText,newCaret - 1,noWordSep,true)
				+ lineStart;
		}

		if(select)
			extendSelection(caret,newCaret);
		else if(!multi)
			selectNone();
		moveCaretPosition(newCaret);
	} //}}}

	//{{{ smartHome() method
	/**
	 * On subsequent invocations, first moves the caret to the first
	 * non-whitespace character of the line, then the beginning of the
	 * line, then to the first visible line.
	 * @since jEdit 2.7pre2
	 */
	public void smartHome(boolean select)
	{
		if(!jEdit.getBooleanProperty("view.homeEnd"))
			goToStartOfLine(select);
		else
		{
			switch(view.getInputHandler().getLastActionCount())
			{
			case 1:
				goToStartOfWhiteSpace(select);
				break;
			case 2:
				goToStartOfLine(select);
				break;
			default: //case 3:
				goToFirstVisibleLine(select);
				break;
			}
		}
	} //}}}

	//{{{ smartEnd() method
	/**
	 * On subsequent invocations, first moves the caret to the last
	 * non-whitespace character of the line, then the end of the
	 * line, then to the last visible line.
	 * @since jEdit 2.7pre2
	 */
	public void smartEnd(boolean select)
	{
		if(!jEdit.getBooleanProperty("view.homeEnd"))
			goToEndOfLine(select);
		else
		{
			switch(view.getInputHandler().getLastActionCount())
			{
			case 1:
				goToEndOfWhiteSpace(select);
				break;
			case 2:
				goToEndOfLine(select);
				break;
			default: //case 3:
				goToLastVisibleLine(select);
				break;
			}
		}
	} //}}}

	//{{{ goToStartOfLine() method
	/**
	 * Moves the caret to the beginning of the current line.
	 * @since jEdit 2.7pre2
	 */
	public void goToStartOfLine(boolean select)
	{
		// do this here, for weird reasons
		Macros.Recorder recorder = view.getMacroRecorder();
		if(recorder != null)
			recorder.record("textArea.goToStartOfLine(" + select + ");");

		Selection s = getSelectionAtOffset(caret);
		int line = (s == null ? caretLine : s.startLine);
		int newCaret = getLineStartOffset(line);
		if(select)
			extendSelection(caret,newCaret);
		else if(!multi)
			selectNone();
		moveCaretPosition(newCaret);
	} //}}}

	//{{{ goToEndOfLine() method
	/**
	 * Moves the caret to the end of the current line.
	 * @since jEdit 2.7pre2
	 */
	public void goToEndOfLine(boolean select)
	{
		// do this here, for weird reasons
		Macros.Recorder recorder = view.getMacroRecorder();
		if(recorder != null)
			recorder.record("textArea.goToEndOfLine(" + select + ");");

		Selection s = getSelectionAtOffset(caret);
		int line = (s == null ? caretLine : s.endLine);
		int newCaret = getLineEndOffset(line) - 1;
		if(select)
			extendSelection(caret,newCaret);
		else if(!multi)
			selectNone();
		moveCaretPosition(newCaret);

		// so that end followed by up arrow will always put caret at
		// the end of the previous line, for example
		setMagicCaretPosition(Integer.MAX_VALUE);
	} //}}}

	//{{{ goToStartOfWhiteSpace() method
	/**
	 * Moves the caret to the first non-whitespace character of the current
	 * line.
	 * @since jEdit 2.7pre2
	 */
	public void goToStartOfWhiteSpace(boolean select)
	{
		// do this here, for weird reasons
		Macros.Recorder recorder = view.getMacroRecorder();
		if(recorder != null)
			recorder.record("textArea.goToStartOfWhiteSpace(" + select + ");");

		Selection s = getSelectionAtOffset(caret);
		int line = (s == null ? caretLine : s.startLine);

		int firstIndent = MiscUtilities.getLeadingWhiteSpace(getLineText(line));
		int firstOfLine = getLineStartOffset(line);

		firstIndent = firstOfLine + firstIndent;
		if(firstIndent == getLineEndOffset(line) - 1)
			firstIndent = firstOfLine;

		if(select)
			extendSelection(caret,firstIndent);
		else if(!multi)
			selectNone();
		moveCaretPosition(firstIndent);
	} //}}}

	//{{{ goToEndOfWhiteSpace() method
	/**
	 * Moves the caret to the last non-whitespace character of the current
	 * line.
	 * @since jEdit 2.7pre2
	 */
	public void goToEndOfWhiteSpace(boolean select)
	{
		// do this here, for weird reasons
		Macros.Recorder recorder = view.getMacroRecorder();
		if(recorder != null)
			recorder.record("textArea.goToEndOfWhiteSpace(" + select + ");");

		Selection s = getSelectionAtOffset(caret);
		int line = (s == null ? caretLine : s.endLine);

		int lastIndent = MiscUtilities.getTrailingWhiteSpace(getLineText(line));
		int lastOfLine = getLineEndOffset(line) - 1;

		lastIndent = lastOfLine - lastIndent;
		if(lastIndent == getLineStartOffset(line))
			lastIndent = lastOfLine;

		if(select)
			extendSelection(caret,lastIndent);
		else if(!multi)
			selectNone();
		moveCaretPosition(lastInden
