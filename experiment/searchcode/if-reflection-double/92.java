/*
 * JEditTextArea.java - jEdit's text component
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 1999, 2005 Slava Pestov
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
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TooManyListenersException;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.text.Segment;
import org.gjt.sp.jedit.Abbrevs;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.Debug;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.Macros;
import org.gjt.sp.jedit.MiscUtilities;
import org.gjt.sp.jedit.TextUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.buffer.*;
import org.gjt.sp.jedit.gui.InputHandler;
import org.gjt.sp.jedit.syntax.*;
import org.gjt.sp.util.Log;


//}}}

/**
 * jEdit's text component.<p>
 *
 * Unlike most other text editors, the selection API permits selection and
 * concurrent manipulation of multiple, non-contiguous regions of text.
 * Methods in this class that deal with selecting text rely upon classes derived
 * the {@link Selection} class.
 *
 * @author Slava Pestov
 * @author John Gellene (API documentation)
 * @version $Id: JEditTextArea.java 5325 2005-12-31 22:03:39Z ezust $
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
		selectionManager = new SelectionManager(this);
		chunkCache = new ChunkCache(this);
		painter = new TextAreaPainter(this);
		repaintMgr = new FastRepaintManager(this,painter);
		gutter = new Gutter(view,this);
		listenerList = new EventListenerList();
		caretEvent = new MutableCaretEvent();
		blink = true;
		lineSegment = new Segment();
		offsetXY = new Point();
		structureMatchers = new LinkedList();
		structureMatchers.add(new StructureMatcher.BracketMatcher());
		//}}}

		//{{{ Initialize the GUI
		setLayout(new ScrollLayout());
		add(ScrollLayout.CENTER,painter);
		add(ScrollLayout.LEFT,gutter);

		// some plugins add stuff in a "right-hand" gutter
		verticalBox = new Box(BoxLayout.X_AXIS);
		verticalBox.add(vertical = new JScrollBar(JScrollBar.VERTICAL));
		vertical.setRequestFocusEnabled(false);
		add(ScrollLayout.RIGHT,verticalBox);
		add(ScrollLayout.BOTTOM,
			horizontal = new JScrollBar(JScrollBar.HORIZONTAL));
		horizontal.setRequestFocusEnabled(false);

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

		mouseHandler = new MouseHandler(this);
		painter.addMouseListener(mouseHandler);
		painter.addMouseMotionListener(mouseHandler);

		addFocusListener(new FocusHandler());
		addMouseWheelListener(new MouseWheelHandler());

		/* Drag and drop */
		setTransferHandler(new TextAreaTransferHandler());
		try
		{
			getDropTarget().addDropTargetListener(
				new TextAreaDropHandler(this));
		}
		catch(TooManyListenersException e)
		{
			Log.log(Log.ERROR,this,e);
		} //}}}

		// This doesn't seem very correct, but it fixes a problem
		// when setting the initial caret position for a buffer
		// (eg, from the recent file list)
		focusedComponent = this;

		popupEnabled = true;
	} //}}}

	public String toString() {
		ArrayList sl = new ArrayList();
		sl .add("caret: " + caret);
		sl.add("caretLine: " + caretLine );
		sl.add("caretScreenLine: " + caretScreenLine);
		sl.add("electricScroll: " + electricScroll);
		sl.add("horizontalOffset: " + horizontalOffset);
		sl.add("magicCaret: " + magicCaret);
		sl.add("offsetXY" + offsetXY.toString());
		sl.add("oldCaretLine: " + oldCaretLine);
		sl.add("screenLastLine: " + screenLastLine);
		sl.add("visibleLines: " + visibleLines);
		sl.add("firstPhysicalLine: " + getFirstPhysicalLine());
		sl.add("physLastLine: " + physLastLine);
		return TextUtilities.join(sl, "\n");
	}
	
	
	//{{{ dispose() method
	/**
	 * Plugins and macros should not call this method.
	 * @since jEdit 4.2pre1
	 */
	public void dispose()
	{
		DisplayManager.textAreaDisposed(this);
	} //}}}

	//{{{ Getters and setters

	//{{{ getView() method
	/**
	 * Returns this text area's view.
	 * @since jEdit 4.2pre5
	 */
	public View getView()
	{
		return view;
	} //}}}

	//{{{ getInputHandler() method
	/**
	 * @since jEdit 4.3pre1
	 */
	public InputHandler getInputHandler()
	{
		return view.getInputHandler();
	} //}}}

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

	//{{{ getDisplayManager() method
	/**
	 * @return the display manager used by this text area.
	 * @since jEdit 4.2pre1
	 */
	public DisplayManager getDisplayManager()
	{
		return displayManager;
	} //}}}

	//{{{ isCaretBlinkEnabled() method
	/**
	 * @return true if the caret is blinking, false otherwise.
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
	 * @return the minimum distance (in number of lines)
	 * from the caret to the nearest edge of the screen 
	 * (top or bottom edge).
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

	//{{{ isQuickCopyEnabled() method
	/**
	 * Returns if clicking the middle mouse button pastes the most
	 * recent selection (% register), and if Control-dragging inserts
	 * the selection at the caret.
	 */
	public final boolean isQuickCopyEnabled()
	{
		return quickCopy;
	} //}}}

	//{{{ setQuickCopyEnabled() method
	/**
	 * Sets if clicking the middle mouse button pastes the most
	 * recent selection (% register), and if Control-dragging inserts
	 * the selection at the caret.
	 * @param quickCopy A boolean flag
	 */
	public final void setQuickCopyEnabled(boolean quickCopy)
	{
		this.quickCopy = quickCopy;
	} //}}}

	//{{{ getBuffer() method
	/**
	 * Returns the buffer this text area is editing.
	 */
	public final JEditBuffer getBuffer()
	{
		return buffer;
	} //}}}

	//{{{ setBuffer() method
	/**
	 * Sets the buffer this text area is editing. Do not call this method -
	 * use {@link org.gjt.sp.jedit.EditPane#setBuffer(Buffer)} instead.
	 * @param buffer The buffer
	 */
	public void setBuffer(JEditBuffer buffer)
	{
		if(this.buffer == buffer)
			return;

		try
		{
			bufferChanging = true;

			if(this.buffer != null)
			{
				// dubious?
				//setFirstLine(0);

				if(!buffer.isLoading())
					selectNone();
				caretLine = caret = caretScreenLine = 0;
				match = null;
			}
			this.buffer = buffer;

			chunkCache.setBuffer(buffer);
			repaintMgr.setFastScroll(false);
			propertiesChanged();

			if(displayManager != null)
			{
				DisplayManager.releaseDisplayManager(
					displayManager);
			}

			displayManager = DisplayManager.getDisplayManager(
				buffer,this);

			displayManager.init();

			if(buffer.isLoading())
				updateScrollBar();

			repaint();

			fireScrollEvent(true);
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

	//{{{ isDragInProgress() method
	/**
	 * Drag and drop of text in jEdit is implementing using jEdit 1.4 APIs,
	 * however since jEdit must run with Java 1.3, this class only has the
	 * necessary support to call a hook method via reflection. This method
	 * is called by the {@link org.gjt.sp.jedit.Java14} class to signal that
	 * a drag is in progress.
	 * @since jEdit 4.2pre5
	 */
	public boolean isDragInProgress()
	{
		return dndInProgress;
	} //}}}

	//{{{ setDragInProgress() method
	/**
	 * Drag and drop of text in jEdit is implementing using jEdit 1.4 APIs,
	 * however since jEdit must run with Java 1.3, this class only has the
	 * necessary support to call a hook method via reflection. This method
	 * is called by the {@link org.gjt.sp.jedit.Java14} class to signal that
	 * a drag is in progress.
	 * @since jEdit 4.2pre5
	 */
	public void setDragInProgress(boolean dndInProgress)
	{
		this.dndInProgress = dndInProgress;
	} //}}}

	//{{{ isDragEnabled() method
	/**
	 * Returns if drag and drop of text is enabled.
	 * @since jEdit 4.2pre5
	 */
	public boolean isDragEnabled()
	{
		return dndEnabled;
	} //}}}

	//{{{ setDragEnabled() method
	/**
	 * Sets if drag and drop of text is enabled.
	 * @since jEdit 4.2pre5
	 */
	public void setDragEnabled(boolean dndEnabled)
	{
		this.dndEnabled = dndEnabled;
	} //}}}

	//{{{ getJoinNonWordChars() method
	/**
	 * If set, double clicking will join non-word characters to form one "word".
	 * @since jEdit 4.3pre2
	 */
	public boolean getJoinNonWordChars()
	{
		return joinNonWordChars;
	} //}}}
	
	//{{{ setJoinNonWordChars() method
	/**
	 * If set, double clicking will join non-word characters to form one "word".
	 * @since jEdit 4.3pre2
	 */
	public void setJoinNonWordChars(boolean joinNonWordChars)
	{
		this.joinNonWordChars = joinNonWordChars;
	} //}}}
	
	//}}}

	//{{{ Scrolling

	//{{{ getFirstLine() method
	/**
	 * Returns the vertical scroll bar position.
	 * @since jEdit 4.2pre1
	 */
	public final int getFirstLine()
	{
		return displayManager.firstLine.scrollLine
			+ displayManager.firstLine.skew;
	} //}}}

	//{{{ setFirstLine() method
	/**
	 * Sets the vertical scroll bar position
	 *
	 * @param firstLine The scroll bar position
	 */
	public void setFirstLine(int firstLine)
	{
		//{{{ ensure we don't have empty space at the bottom or top, etc
		int max = displayManager.getScrollLineCount() - visibleLines
			+ (lastLinePartial ? 1 : 0);
		if(firstLine > max)
			firstLine = max;
		if(firstLine < 0)
			firstLine = 0;
		//}}}

		if(Debug.SCROLL_DEBUG)
		{
			Log.log(Log.DEBUG,this,"setFirstLine() from "
				+ getFirstLine() + " to " + firstLine);
		}

		int oldFirstLine = getFirstLine();
		if(firstLine == oldFirstLine)
			return;

		displayManager.setFirstLine(oldFirstLine,firstLine);

		repaint();

		fireScrollEvent(true);
	} //}}}

	//{{{ getFirstPhysicalLine() method
	/**
	 * Returns the first visible physical line index.
	 * @since jEdit 4.0pre4
	 */
	public final int getFirstPhysicalLine()
	{
		return displayManager.firstLine.physicalLine;
	} //}}}

	//{{{ setFirstPhysicalLine() method
	/**
	 * Sets the vertical scroll bar position.
	 * @param physFirstLine The first physical line to display
	 * @since jEdit 4.2pre1
	 */
	public void setFirstPhysicalLine(int physFirstLine)
	{
		setFirstPhysicalLine(physFirstLine,0);
	} //}}}

	//{{{ setFirstPhysicalLine() method
	/**
	 * Sets the vertical scroll bar position.
	 * @param physFirstLine The first physical line to display
	 * @param skew A local screen line delta
	 * @since jEdit 4.2pre1
	 */
	public void setFirstPhysicalLine(int physFirstLine, int skew)
	{
		if(Debug.SCROLL_DEBUG)
		{
			Log.log(Log.DEBUG,this,"setFirstPhysicalLine("
				+ physFirstLine + "," + skew + ")");
		}

		int amount = (physFirstLine - displayManager.firstLine.physicalLine);

		displayManager.setFirstPhysicalLine(amount,skew);

		repaint();

		fireScrollEvent(true);
	} //}}}

	//{{{ getLastPhysicalLine() method
	/**
	 * Returns the last visible physical line index.
	 * @since jEdit 4.0pre4
	 */
	public final int getLastPhysicalLine()
	{
		return physLastLine;
	} //}}}

	//{{{ getLastScreenLine() method
	/**
	 * @since jEdit 4.3pre1
	 */
	public int getLastScreenLine()
	{
		return screenLastLine;
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
		if(horizontalOffset > 0)
			horizontalOffset = 0;

		if(horizontalOffset == this.horizontalOffset)
			return;

		this.horizontalOffset = horizontalOffset;
		painter.repaint();

		fireScrollEvent(false);
	} //}}}

	//{{{ scrollUpLine() method
	/**
	 * Scrolls up by one line.
	 * @since jEdit 2.7pre2
	 */
	public void scrollUpLine()
	{
		setFirstLine(getFirstLine() - 1);
	} //}}}

	//{{{ scrollUpPage() method
	/**
	 * Scrolls up by one page.
	 * @since jEdit 2.7pre2
	 */
	public void scrollUpPage()
	{
		setFirstLine(getFirstLine() - getVisibleLines()
			+ (lastLinePartial ? 1 : 0));
	} //}}}

	//{{{ scrollDownLine() method
	/**
	 * Scrolls down by one line.
	 * @since jEdit 2.7pre2
	 */
	public void scrollDownLine()
	{
		setFirstLine(getFirstLine() + 1);
	} //}}}

	//{{{ scrollDownPage() method
	/**
	 * Scrolls down by one page.
	 * @since jEdit 2.7pre2
	 */
	public void scrollDownPage()
	{
		setFirstLine(getFirstLine() + getVisibleLines()
			- (lastLinePartial ? 1 : 0));
	} //}}}

	//{{{ scrollToCaret() method
	/**
	 * Ensures that the caret is visible by scrolling the text area if
	 * necessary.
	 * @param doElectricScroll If true, electric scrolling will be performed
	 */
	public void scrollToCaret(boolean doElectricScroll)
	{
		scrollTo(caretLine,caret - buffer.getLineStartOffset(caretLine),
			doElectricScroll);
	} //}}}

	//{{{ scrollTo() method
	/**
	 * Ensures that the specified location in the buffer is visible.
	 * @param offset The offset from the start of the buffer
	 * @param doElectricScroll If true, electric scrolling will be performed
	 * @since jEdit 4.2pre3
	 */
	public void scrollTo(int offset, boolean doElectricScroll)
	{
		int line = buffer.getLineOfOffset(offset);
		scrollTo(line,offset - buffer.getLineStartOffset(line),
			doElectricScroll);
	} //}}}

	//{{{ scrollTo() method
	/**
	 * Ensures that the specified location in the buffer is visible.
	 * @param line The line number
	 * @param offset The offset from the start of the line
	 * @param doElectricScroll If true, electric scrolling will be performed
	 * @since jEdit 4.0pre6
	 */
	public void scrollTo(int line, int offset, boolean doElectricScroll)
	{
		if(Debug.SCROLL_TO_DEBUG)
			Log.log(Log.DEBUG,this,"scrollTo(), lineCount="
				+ getLineCount());

		//{{{ Get ready
		int extraEndVirt;
		int lineLength = buffer.getLineLength(line);
		if(offset > lineLength)
		{
			extraEndVirt = charWidth * (offset - lineLength);
			offset = lineLength;
		}
		else
			extraEndVirt = 0;

		int _electricScroll = (doElectricScroll
			&& visibleLines - 1 > electricScroll * 2
			? electricScroll : 0); //}}}

		if(visibleLines <= 1)
		{
			if(Debug.SCROLL_TO_DEBUG)
				Log.log(Log.DEBUG,this,"visibleLines <= 0");
			setFirstPhysicalLine(line,_electricScroll);
			return;
		}

		//{{{ Scroll vertically
		int screenLine = chunkCache.getScreenLineOfOffset(line,offset);
		int visibleLines = getVisibleLines();
		if(screenLine == -1)
		{
			if(Debug.SCROLL_TO_DEBUG)
				Log.log(Log.DEBUG,this,"screenLine == -1");
			ChunkCache.LineInfo[] infos = chunkCache
				.getLineInfosForPhysicalLine(line);
			int subregion = chunkCache.getSubregionOfOffset(
				offset,infos);
			int prevLine = displayManager.getPrevVisibleLine(getFirstPhysicalLine());
			int nextLine = displayManager.getNextVisibleLine(getLastPhysicalLine());
			if(line == getFirstPhysicalLine())
			{
				if(Debug.SCROLL_TO_DEBUG)
					Log.log(Log.DEBUG,this,line + " == " + getFirstPhysicalLine());
				setFirstPhysicalLine(line,subregion
					- _electricScroll);
			}
			else if(line == prevLine)
			{
				if(Debug.SCROLL_TO_DEBUG)
					Log.log(Log.DEBUG,this,line + " == " + prevLine);
				setFirstPhysicalLine(prevLine,subregion
					- _electricScroll);
			}
			else if(line == getLastPhysicalLine())
			{
				if(Debug.SCROLL_TO_DEBUG)
					Log.log(Log.DEBUG,this,line + " == " + getLastPhysicalLine());
				setFirstPhysicalLine(line,
					subregion + _electricScroll
					- visibleLines
					+ (lastLinePartial ? 2 : 1));
			}
			else if(line == nextLine)
			{
				if(Debug.SCROLL_TO_DEBUG)
					Log.log(Log.DEBUG,this,line + " == " + nextLine);
				setFirstPhysicalLine(nextLine,
					subregion + electricScroll
					- visibleLines
					+ (lastLinePartial ? 2 : 1));
			}
			else
			{
				if(Debug.SCROLL_TO_DEBUG)
				{
					Log.log(Log.DEBUG,this,"neither");
					Log.log(Log.DEBUG,this,"Last physical line is " + getLastPhysicalLine());
				}
				setFirstPhysicalLine(line,subregion
					- visibleLines / 2);
				if(Debug.SCROLL_TO_DEBUG)
				{
					Log.log(Log.DEBUG,this,"Last physical line is " + getLastPhysicalLine());
				}
			}
		}
		else if(screenLine < _electricScroll)
		{
			if(Debug.SCROLL_TO_DEBUG)
				Log.log(Log.DEBUG,this,"electric up");
			setFirstLine(getFirstLine() - _electricScroll + screenLine);
		}
		else if(screenLine > visibleLines - _electricScroll
			- (lastLinePartial ? 2 : 1))
		{
			if(Debug.SCROLL_TO_DEBUG)
				Log.log(Log.DEBUG,this,"electric down");
			setFirstLine(getFirstLine() + _electricScroll - visibleLines + screenLine + (lastLinePartial ? 2 : 1));
		} //}}}

		//{{{ Scroll horizontally
		if(!displayManager.isLineVisible(line))
			return;

		Point point = offsetToXY(line,offset,offsetXY);
		if(point == null)
		// FIXME - we need to reset the state of this window so that it has the right
		// dimensions again. 
		{
			
			Log.log(Log.ERROR,this,"BUG: screenLine=" + screenLine
				+ ",visibleLines=" + visibleLines
				+ ",physicalLine=" + line
				+ ",offset=" + offset
				+ ",firstPhysicalLine=" + getFirstPhysicalLine()
				+ ",lastPhysicalLine=" + getLastPhysicalLine());

		}
		point.x += extraEndVirt;

		if(point.x < 0)
		{
			setHorizontalOffset(horizontalOffset
				- point.x + charWidth + 5);
		}
		else if(point.x >= painter.getWidth() - charWidth - 5)
		{
			setHorizontalOffset(horizontalOffset +
				(painter.getWidth() - point.x)
				- charWidth - 5);
		} //}}}
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

	//{{{ Screen line stuff

	//{{{ getPhysicalLineOfScreenLine() method
	/**
	 * Returns the physical line number that contains the specified screen
	 * line.
	 * @param screenLine The screen line
	 * @since jEdit 4.0pre6
	 */
	public int getPhysicalLineOfScreenLine(int screenLine)
	{
		return chunkCache.getLineInfo(screenLine).physicalLine;
	} //}}}

	//{{{ getScreenLineOfOffset() method
	/**
	 * Returns the screen (wrapped) line containing the specified offset.
	 * Returns -1 if the line is not currently visible on the screen.
	 * @param offset The offset
	 * @since jEdit 4.0pre4
	 */
	public int getScreenLineOfOffset(int offset)
	{
		int line = buffer.getLineOfOffset(offset);
		offset -= buffer.getLineStartOffset(line);
		return chunkCache.getScreenLineOfOffset(line,offset);
	} //}}}

	//{{{ getScreenLineStartOffset() method
	/**
	 * Returns the start offset of the specified screen (wrapped) line.
	 * @param line The line
	 * @since jEdit 4.0pre4
	 */
	public int getScreenLineStartOffset(int line)
	{
		ChunkCache.LineInfo lineInfo = chunkCache.getLineInfo(line);
		if(lineInfo.physicalLine == -1)
			return -1;

		return buffer.getLineStartOffset(lineInfo.physicalLine)
			+ lineInfo.offset;
	} //}}}

	//{{{ getScreenLineEndOffset() method
	/**
	 * Returns the end offset of the specified screen (wrapped) line.
	 * @param line The line
	 * @since jEdit 4.0pre4
	 */
	public int getScreenLineEndOffset(int line)
	{
		ChunkCache.LineInfo lineInfo = chunkCache.getLineInfo(line);
		if(lineInfo.physicalLine == -1)
			return -1;

		return buffer.getLineStartOffset(lineInfo.physicalLine)
			+ lineInfo.offset + lineInfo.length;
	} //}}}

	//}}}

	//{{{ Offset conversion

	//{{{ xyToOffset() method
	/**
	 * Converts a point to an offset.
	 * Note that unlike in previous jEdit versions, this method now returns
	 * -1 if the y co-ordinate is out of bounds.
	 *
	 * @param x The x co-ordinate of the point
	 * @param y The y co-ordinate of the point
	 */
	public int xyToOffset(int x, int y)
	{
		return xyToOffset(x,y,true);
	} //}}}

	//{{{ xyToOffset() method
	/**
	 * Converts a point to an offset.
	 * Note that unlike in previous jEdit versions, this method now returns
	 * -1 if the y co-ordinate is out of bounds.
	 *
	 * @param x The x co-ordinate of the point
	 * @param y The y co-ordinate of the point
	 * @param round Round up to next letter if past the middle of a letter?
	 * @since jEdit 3.2pre6
	 */
	public int xyToOffset(int x, int y, boolean round)
	{
		FontMetrics fm = painter.getFontMetrics();
		int height = fm.getHeight();
		int line = y / height;

		if(line < 0 || line >= visibleLines)
			return -1;

		return xToScreenLineOffset(line,x,round);
	} //}}}

	//{{{ xToScreenLineOffset() method
	/**
	 * Converts a point in a given screen line to an offset.
	 * Note that unlike in previous jEdit versions, this method now returns
	 * -1 if the y co-ordinate is out of bounds.
	 *
	 * @param x The x co-ordinate of the point
	 * @param screenLine The screen line
	 * @param round Round up to next letter if past the middle of a letter?
	 * @since jEdit 3.2pre6
	 */
	public int xToScreenLineOffset(int screenLine, int x, boolean round)
	{
		ChunkCache.LineInfo lineInfo = chunkCache.getLineInfo(screenLine);
		if(lineInfo.physicalLine == -1)
		{
			return getLineEndOffset(displayManager
				.getLastVisibleLine()) - 1;
		}
		else
		{
			int offset = Chunk.xToOffset(lineInfo.chunks,
				x - horizontalOffset,round);
			if(offset == -1 || offset == lineInfo.offset + lineInfo.length)
				offset = lineInfo.offset + lineInfo.length - 1;

			return getLineStartOffset(lineInfo.physicalLine) + offset;
		}
	} //}}}

	//{{{ offsetToXY() method
	/**
	 * Converts an offset into a point in the text area painter's
	 * co-ordinate space.
	 * @param offset The offset
	 * @return The location of the offset on screen, or <code>null</code>
	 * if the specified offset is not visible
	 */
	public Point offsetToXY(int offset)
	{
		int line = buffer.getLineOfOffset(offset);
		offset -= buffer.getLineStartOffset(line);
		Point retVal = new Point();
		return offsetToXY(line,offset,retVal);
	} //}}}

	//{{{ offsetToXY() method
	/**
	 * Converts an offset into a point in the text area painter's
	 * co-ordinate space.
	 * @param line The line
	 * @param offset The offset
	 * @return The location of the offset on screen, or <code>null</code>
	 * if the specified offset is not visible
	 */
	public Point offsetToXY(int line, int offset)
	{
		return offsetToXY(line,offset,new Point());
	} //}}}

	//{{{ offsetToXY() method
	/**
	 * Converts a line,offset pair into an x,y (pixel) point relative to the 
	 * upper left corner (0,0) of the text area.
	 * 
	 * @param line The physical line number (from top of document)
	 * @param offset The offset in characters, from the start of the line
	 * @param retVal The point to store the return value in
	 * @return <code>retVal</code> for convenience, or <code>null</code>
	 * if the specified offset is not visible
	 * @since jEdit 4.0pre4
	 */
	public Point offsetToXY(int line, int offset, Point retVal)
	{
		if(!displayManager.isLineVisible(line))
			return null;
		int screenLine = chunkCache.getScreenLineOfOffset(line,offset);
		if(screenLine == -1)
			return null;

		FontMetrics fm = painter.getFontMetrics();

		retVal.y = screenLine * fm.getHeight();

		ChunkCache.LineInfo info = chunkCache.getLineInfo(screenLine);

		retVal.x = (int)(horizontalOffset + Chunk.offsetToX(
			info.chunks,offset));

		return retVal;
	} //}}}

	//}}}

	//{{{ Painting

	//{{{ invalidateScreenLineRange() method
	/**
	 * Marks a range of screen lines as needing a repaint.
	 * @param start The first line
	 * @param end The last line
	 * @since jEdit 4.0pre4
	 */
	public void invalidateScreenLineRange(int start, int end)
	{
		if(buffer.isLoading())
			return;

		if(start > end)
		{
			int tmp = end;
			end = start;
			start = tmp;
		}

		if(chunkCache.needFullRepaint())
			end = visibleLines;

		FontMetrics fm = painter.getFontMetrics();
		int y = start * fm.getHeight();
		int height = (end - start + 1) * fm.getHeight();
		painter.repaint(0,y,painter.getWidth(),height);
		gutter.repaint(0,y,gutter.getWidth(),height);
	} //}}}

	//{{{ invalidateLine() method
	/**
	 * Marks a line as needing a repaint.
	 * @param line The physical line to invalidate
	 */
	public void invalidateLine(int line)
	{
		if(!isShowing()
			|| buffer.isLoading()
			|| line < getFirstPhysicalLine()
			|| line > physLastLine
			|| !displayManager.isLineVisible(line))
			return;

		int startLine = -1;
		int endLine = -1;

		for(int i = 0; i < visibleLines; i++)
		{
			ChunkCache.LineInfo info = chunkCache.getLineInfo(i);

			if((info.physicalLine >= line || info.physicalLine == -1)
				&& startLine == -1)
			{
				startLine = i;
			}

			if((info.physicalLine >= line && info.lastSubregion)
				|| info.physicalLine == -1)
			{
				endLine = i;
				break;
			}
		}

		if(chunkCache.needFullRepaint() || endLine == -1)
			endLine = visibleLines;

		invalidateScreenLineRange(startLine,endLine);
	} //}}}

	//{{{ invalidateLineRange() method
	/**
	 * Marks a range of physical lines as needing a repaint.
	 * @param start The first line to invalidate
	 * @param end The last line to invalidate
	 */
	public void invalidateLineRange(int start, int end)
	{
		if(!isShowing() || buffer.isLoading())
			return;

		if(end < start)
		{
			int tmp = end;
			end = start;
			start = tmp;
		}

		if(end < getFirstPhysicalLine() || start > getLastPhysicalLine())
			return;

		int startScreenLine = -1;
		int endScreenLine = -1;

		for(int i = 0; i < visibleLines; i++)
		{
			ChunkCache.LineInfo info = chunkCache.getLineInfo(i);

			if((info.physicalLine >= start || info.physicalLine == -1)
				&& startScreenLine == -1)
			{
				startScreenLine = i;
			}

			if((info.physicalLine >= end && info.lastSubregion)
				|| info.physicalLine == -1)
			{
				endScreenLine = i;
				break;
			}
		}

		if(startScreenLine == -1)
			startScreenLine = 0;

		if(chunkCache.needFullRepaint() || endScreenLine == -1)
			endScreenLine = visibleLines;

		invalidateScreenLineRange(startScreenLine,endScreenLine);
	} //}}}

	//}}}

	//{{{ Convenience methods

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
	 * Selects all text in the buffer. Preserves the scroll position.
	 */
	public final void selectAll()
	{
		int firstLine = getFirstLine();
		int horizOffset = getHorizontalOffset();

		setSelection(new Selection.Range(0,buffer.getLength()));
		moveCaretPosition(buffer.getLength(),true);

		setFirstLine(firstLine);
		setHorizontalOffset(horizOffset);
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
			getToolkit().beep();
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
	 * Selects from the bracket at the specified position to the
	 * corresponding bracket.
	 * @since jEdit 4.2pre1
	 */
	public Selection selectToMatchingBracket(int position,
		boolean quickCopy)
	{
		int positionLine = buffer.getLineOfOffset(position);
		int lineOffset = position - buffer.getLineStartOffset(positionLine);
		if(getLineLength(positionLine) != 0)
		{
			int bracket = TextUtilities.findMatchingBracket(buffer,
				positionLine,Math.max(0,lineOffset - 1));

			if(bracket != -1)
			{
				Selection s;

				if(bracket < position)
				{
					if(!quickCopy)
						moveCaretPosition(position,false);
					s = new Selection.Range(bracket,position);
				}
				else
				{
					if(!quickCopy)
						moveCaretPosition(bracket + 1,false);
					s = new Selection.Range(position - 1,bracket + 1);
				}

				if(!multi && !quickCopy)
					selectNone();

				addToSelection(s);
				return s;
			}
		}

		return null;
	} //}}}

	//{{{ selectToMatchingBracket() method
	/**
	 * Selects from the bracket at the caret position to the corresponding
	 * bracket.
	 * @since jEdit 4.0pre2
	 */
	public void selectToMatchingBracket()
	{
		selectToMatchingBracket(caret,false);
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
			getToolkit().beep();
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

	//{{{ lineInStructureScope() method
	/**
	 * Returns if the specified line is contained in the currently
	 * matched structure's scope.
	 * @since jEdit 4.2pre3
	 */
	public boolean lineInStructureScope(int line)
	{
		if(match == null)
			return false;

		if(match.startLine < caretLine)
			return (line >= match.startLine && line <= caretLine);
		else
			return (line <= match.endLine && line >= caretLine);
	} //}}}

	//{{{ invertSelection() method
	/**
	 * Inverts the selection.
	 * @since jEdit 4.0pre1
	 */
	public final void invertSelection()
	{
		selectionManager.invertSelection();
	} //}}}

	//{{{ getSelectionCount() method
	/**
	 * Returns the number of selections. This can be used to test
	 * for the existence of selections.
	 * @since jEdit 3.2pre2
	 */
	public int getSelectionCount()
	{
		return selectionManager.getSelectionCount();
	} //}}}

	//{{{ getSelection() method
	/**
	 * Returns the current selection.
	 * @since jEdit 3.2pre1
	 */
	public Selection[] getSelection()
	{
		return selectionManager.getSelection();
	} //}}}

	//{{{ getSelectionIterator() method
	/**
	 * Returns the current selection.
	 * @since jEdit 4.3pre1
	 */
	public Iterator getSelectionIterator()
	{
		return selectionManager.selection.iterator();
	} //}}}

	//{{{ getSelection() method
	/**
	 * Returns the selection with the specified index. This must be
	 * between 0 and the return value of <code>getSelectionCount()</code>.
	 * @since jEdit 4.3pre1
	 */
	public Selection getSelection(int index)
	{
		return (Selection)selectionManager.selection.get(index);
	} //}}}

	//{{{ selectNone() method
	/**
	 * Deselects everything.
	 */
	public void selectNone()
	{
		invalidateSelectedLines();
		setSelection((Selection)null);
	} //}}}

	//{{{ setSelection() method
	/**
	 * Sets the selection. Nested and overlapping selections are merged
	 * where possible. Null elements of the array are ignored.
	 * @param selection The new selection
	 * since jEdit 3.2pre1
	 */
	public void setSelection(Selection[] selection)
	{
		// invalidate the old selection
		invalidateSelectedLines();
		selectionManager.setSelection(selection);
		finishCaretUpdate(caretLine,NO_SCROLL,true);
	} //}}}

	//{{{ setSelection() method
	/**
	 * Sets the selection. Nested and overlapping selections are merged
	 * where possible.
	 * @param selection The new selection
	 * since jEdit 3.2pre1
	 */
	public void setSelection(Selection selection)
	{
		invalidateSelectedLines();
		selectionManager.setSelection(selection);
		finishCaretUpdate(caretLine,NO_SCROLL,true);
	} //}}}

	//{{{ addToSelection() method
	/**
	 * Adds to the selection. Nested and overlapping selections are merged
	 * where possible.
	 * @param selection The new selection
	 * since jEdit 3.2pre1
	 */
	public void addToSelection(Selection[] selection)
	{
		invalidateSelectedLines();
		selectionManager.addToSelection(selection);
		finishCaretUpdate(caretLine,NO_SCROLL,true);
	} //}}}

	//{{{ addToSelection() method
	/**
	 * Adds to the selection. Nested and overlapping selections are merged
	 * where possible.
	 * @param selection The new selection
	 * since jEdit 3.2pre1
	 */
	public void addToSelection(Selection selection)
	{
		invalidateSelectedLines();
		selectionManager.addToSelection(selection);
		finishCaretUpdate(caretLine,NO_SCROLL,true);
	} //}}}

	//{{{ getSelectionAtOffset() method
	/**
	 * Returns the selection containing the specific offset, or <code>null</code>
	 * if there is no selection at that offset.
	 * @param offset The offset
	 * @since jEdit 3.2pre1
	 */
	public Selection getSelectionAtOffset(int offset)
	{
		return selectionManager.getSelectionAtOffset(offset);
	} //}}}

	//{{{ removeFromSelection() method
	/**
	 * Deactivates the specified selection.
	 * @param sel The selection
	 * @since jEdit 3.2pre1
	 */
	public void removeFromSelection(Selection sel)
	{
		invalidateSelectedLines();
		selectionManager.removeFromSelection(sel);
		finishCaretUpdate(caretLine,NO_SCROLL,true);
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

		invalidateSelectedLines();
		selectionManager.removeFromSelection(sel);
		finishCaretUpdate(caretLine,NO_SCROLL,true);
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
	 * @param extraEndVirt Only for rectangular selections - specifies how
	 * far it extends into virtual space.
	 * @param rect Make the selection rectangular?
	 * @since jEdit 3.2pre1
	 */
	public void resizeSelection(int offset, int end, int extraEndVirt,
		boolean rect)
	{
		Selection s = selectionManager.getSelectionAtOffset(offset);
		if(s != null)
		{
			invalidateLineRange(s.startLine,s.endLine);
			selectionManager.removeFromSelection(s);
		}

		selectionManager.resizeSelection(offset,end,extraEndVirt,rect);
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
	 * @since jEdit 3.2pre1
	 */
	public void extendSelection(int offset, int end)
	{
		extendSelection(offset,end,0,0);
	} //}}}

	//{{{ extendSelection() method
	/**
	 * Extends the selection at the specified offset, or creates a new
	 * one if there is no selection at the specified offset. This is
	 * different from resizing in that the new chunk is added to the
	 * selection in question, instead of replacing it.
	 * @param offset The offset
	 * @param end The new selection end
	 * @param extraStartVirt Extra virtual space at the start
	 * @param extraEndVirt Extra virtual space at the end
	 * @since jEdit 4.2pre1
	 */
	public void extendSelection(int offset, int end,
		int extraStartVirt, int extraEndVirt)
	{
		Selection s = getSelectionAtOffset(offset);
		if(s != null)
		{
			invalidateLineRange(s.startLine,s.endLine);
			selectionManager.removeFromSelection(s);

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

		if(rectangularSelectionMode)
		{
			s = new Selection.Rect(offset,end);
			((Selection.Rect)s).extraStartVirt = extraStartVirt;
			((Selection.Rect)s).extraEndVirt = extraEndVirt;
		}
		else
			s = new Selection.Range(offset,end);

		selectionManager.addToSelection(s);
		fireCaretEvent();

		if(rectangularSelectionMode && extraEndVirt != 0)
		{
			int line = getLineOfOffset(end);
			scrollTo(line,getLineLength(line) + extraEndVirt,false);
		}
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
		s.getText(buffer,buf);
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
		Selection[] sel = selectionManager.getSelection();
		if(sel.length == 0)
			return null;

		StringBuffer buf = new StringBuffer();
		for(int i = 0; i < sel.length; i++)
		{
			if(i != 0)
				buf.append(separator);

			sel[i].getText(buffer,buf);
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

			moveCaretPosition(s.setText(buffer,selectedText));
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
		int newCaret = replaceSelection(selectedText);
		if(newCaret != -1)
			moveCaretPosition(newCaret);
		selectNone();
	} //}}}

	//{{{ setSelectedText() method
	/**
	 * Replaces the selection at the caret with the specified text.
	 * If there is no selection at the caret, the text is inserted at
	 * the caret position.
	 * @param selectedText The new selection
	 * @param moveCaret Move caret to insertion location if necessary
	 * @since jEdit 4.2pre5
	 */
	public void setSelectedText(String selectedText, boolean moveCaret)
	{
		int newCaret = replaceSelection(selectedText);
		if(moveCaret && newCaret != -1)
			moveCaretPosition(newCaret);
		selectNone();
	} //}}}

	//{{{ replaceSelection() method
	/**
	 * Set the selection, but does not deactivate it, and does not move the
	 * caret.
	 *
	 * Please use {@link #setSelectedText(String)} instead.
	 *
	 * @param selectedText The new selection
	 * @return The new caret position
	 * @since 4.3pre1
	 */
	public int replaceSelection(String selectedText)
	{
		if(!isEditable())
			throw new RuntimeException("Text component read only");

		int newCaret = -1;

		if(getSelectionCount() == 0)
		{
			// for compatibility with older jEdit versions
			buffer.insert(caret,selectedText);
		}
		else
		{
			try
			{

				buffer.beginCompoundEdit();

				Selection[] selection = getSelection();
				for(int i = 0; i < selection.length; i++)
					newCaret = selection[i].setText(buffer,selectedText);
			}
			finally
			{
				buffer.endCompoundEdit();
			}
		}

		return newCaret;
	} //}}}

	//{{{ getSelectedLines() method
	/**
	 * Returns a sorted array of line numbers on which a selection or
	 * selections are present.<p>
	 *
	 * This method is the most convenient way to iterate through selected
	 * lines in a buffer. The line numbers in the array returned by this
	 * method can be passed as a parameter to such methods as
	 * {@link org.gjt.sp.jedit.Buffer#getLineText(int)}.
	 *
	 * @since jEdit 3.2pre1
	 */
	public int[] getSelectedLines()
	{
		if(selectionManager.getSelectionCount() == 0)
			return new int[] { caretLine };

		return selectionManager.getSelectedLines();
	} //}}}

	//}}}

	//{{{ Caret

	//{{{ caretAutoScroll() method
	/**
	 * Return if change in buffer should scroll this text area.
	 * @since jEdit 4.3pre2
	 */
	public boolean caretAutoScroll()
	{
		return (focusedComponent == this);
	} //}}}

	//{{{ addStructureMatcher() method
	/**
	 * Adds a structure matcher.
	 * @since jEdit 4.2pre3
	 */
	public void addStructureMatcher(StructureMatcher matcher)
	{
		structureMatchers.add(matcher);
	} //}}}

	//{{{ removeStructureMatcher() method
	/**
	 * Removes a structure matcher.
	 * @since jEdit 4.2pre3
	 */
	public void removeStructureMatcher(StructureMatcher matcher)
	{
		structureMatchers.remove(matcher);
	} //}}}

	//{{{ getStructureMatchStart() method
	/**
	 * Returns the structure element (bracket, or XML tag, etc) matching the
	 * one before the caret.
	 * @since jEdit 4.2pre3
	 */
	public StructureMatcher.Match getStructureMatch()
	{
		return match;
	} //}}}

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
		int offset = getScreenLineStartOffset(visibleLines / 2);
		if(offset == -1)
			getToolkit().beep();
		else
			setCaretPosition(offset);
	} //}}}

	//{{{ setCaretPosition() method
	/**
	 * Sets the caret position and deactivates the selection.
	 * @param newCaret The caret position
	 */
	public void setCaretPosition(int newCaret)
	{
		selectNone();
		moveCaretPosition(newCaret,true);
	} //}}}

	//{{{ setCaretPosition() method
	/**
	 * Sets the caret position and deactivates the selection.
	 * @param newCaret The caret position
	 * @param doElectricScroll Do electric scrolling?
	 */
	public void setCaretPosition(int newCaret, boolean doElectricScroll)
	{
		selectNone();
		moveCaretPosition(newCaret,doElectricScroll);
	} //}}}

	//{{{ moveCaretPosition() method
	/**
	 * Sets the caret position without deactivating the selection.
	 * @param newCaret The caret position
	 */
	public void moveCaretPosition(int newCaret)
	{
		moveCaretPosition(newCaret,true);
	} //}}}

	//{{{ moveCaretPosition() method
	/**
	 * Sets the caret position without deactivating the selection.
	 * @param newCaret The caret position
	 * @param doElectricScroll Do electric scrolling?
	 */
	public void moveCaretPosition(int newCaret, boolean doElectricScroll)
	{
		moveCaretPosition(newCaret,doElectricScroll ? ELECTRIC_SCROLL
			: NORMAL_SCROLL);
	} //}}}

	//{{{ moveCaretPosition() method
	public static int NO_SCROLL = 0;
	public static int NORMAL_SCROLL = 1;
	public static int ELECTRIC_SCROLL = 2;
	/**
	 * Sets the caret position without deactivating the selection.
	 * @param newCaret The caret position
	 * @param scrollMode The scroll mode (NO_SCROLL, NORMAL_SCROLL, or
	 * ELECTRIC_SCROLL).
	 * @since jEdit 4.2pre1
	 */
	public void moveCaretPosition(int newCaret, int scrollMode)
	{
		if(newCaret < 0 || newCaret > buffer.getLength())
		{
			throw new IllegalArgumentException("caret out of bounds: "
				+ newCaret);
		}

		int oldCaretLine = caretLine;

		if(caret == newCaret)
			finishCaretUpdate(oldCaretLine,scrollMode,false);
		else
		{
			caret = newCaret;
			caretLine = getLineOfOffset(newCaret);

			magicCaret = -1;

			finishCaretUpdate(oldCaretLine,scrollMode,true);
		}
	} //}}}

	//{{{ getCaretPosition() method
	/**
	 * Returns a zero-based index of the caret position.
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
	 * Returns an internal position used to keep the caret in one
	 * column while moving around lines of varying lengths.
	 * @since jEdit 4.2pre1
	 */
	public int getMagicCaretPosition()
	{
		if(magicCaret == -1)
		{
			magicCaret = chunkCache.subregionOffsetToX(
				caretLine,caret - getLineStartOffset(caretLine));
		}

		return magicCaret;
	} //}}}

	//{{{ setMagicCaretPosition() method
	/**
	 * Sets the `magic' caret position. This can be used to preserve
	 * the column position when moving up and down lines.
	 * @param magicCaret The magic caret position
	 * @since jEdit 4.2pre1
	 */
	public void setMagicCaretPosition(int magicCaret)
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

	//{{{ goToNextBracket() method
	/**
	 * Moves the caret to the next closing bracket.
	 * @since jEdit 2.7pre2.
	 */
	public void goToNextBracket(boolean select)
	{
		int newCaret = -1;

		if(caret != buffer.getLength())
		{
			String text = getText(caret,buffer.getLength()
				- caret - 1);

loop:			for(int i = 0; i < text.length(); i++)
			{
				switch(text.charAt(i))
				{
				case ')': case ']': case '}':
					newCaret = caret + i + 1;
					break loop;
				}
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
		Selection s = getSelectionAtOffset(caret);

		if(!select && s instanceof Selection.Range)
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

		int extraStartVirt, extraEndVirt;
		if(s instanceof Selection.Rect)
		{
			extraStartVirt = ((Selection.Rect)s).extraStartVirt;
			extraEndVirt = ((Selection.Rect)s).extraEndVirt;
		}
		else
		{
			extraStartVirt = 0;
			extraEndVirt = 0;
		}

		int newCaret = caret;

		if(caret == buffer.getLength())
		{
			if(select && (rectangularSelectionMode || s instanceof Selection.Rect))
			{
				if(s != null && caret == s.start)
					extraStartVirt++;
				else
					extraEndVirt++;
			}
			else
			{
				getToolkit().beep();
				return;
			}
		}
		else if(caret == getLineEndOffset(caretLine) - 1)
		{
			if(select && (rectangularSelectionMode || s instanceof Selection.Rect))
			{
				if(s != null && caret == s.start)
					extraStartVirt++;
				else
					extraEndVirt++;
			}
			else
			{
				int line = displayManager.getNextVisibleLine(caretLine);
				if(line == -1)
				{
					getToolkit().beep();
					return;
				}
				else
					newCaret = getLineStartOffset(line);
			}
		}
		else
			newCaret = caret + 1;

		if(select)
			extendSelection(caret,newCaret,extraStartVirt,extraEndVirt);
		else if(!multi)
			selectNone();

		moveCaretPosition(newCaret);
	} //}}}

	//{{{ goToNextLine() method
	/**
	 * Move the caret to the next line.
	 * @since jEdit 2.7pre2
	 */
	public void goToNextLine(boolean select)
	{
		Selection s = getSelectionAtOffset(caret);
		boolean rectSelect = (s == null ? rectangularSelectionMode
			: s instanceof Selection.Rect);
		int magic = getMagicCaretPosition();
		int newCaret = chunkCache.getBelowPosition(caretLine,
			caret - buffer.getLineStartOffset(caretLine),magic + 1,
			rectSelect && select);
		if(newCaret == -1)
		{
			int end = getLineEndOffset(caretLine) - 1;
			if(caret == end)
			{
				getToolkit().beep();
				return;
			}
			else
				newCaret = end;
		}

		if(select)
		{
			RectParams params = getRectParams(caret,newCaret);
			int extraStartVirt;
			int extraEndVirt;
			if(params == null)
			{
				extraStartVirt = 0;
				extraEndVirt = 0;
			}
			else
			{
				extraStartVirt = params.extraStartVirt;
				extraEndVirt = params.extraEndVirt;
				newCaret = params.newCaret;
			}
			extendSelection(caret,newCaret,extraStartVirt,extraEndVirt);
		}
		else if(!multi)
			selectNone();

		moveCaretPosition(newCaret);

		setMagicCaretPosition(magic);
	} //}}}

	//{{{ goToNextPage() method
	/**
	 * Moves the caret to the next screenful.
	 * @since jEdit 2.7pre2.
	 */
	public void goToNextPage(boolean select)
	{
		scrollToCaret(false);
		int magic = getMagicCaretPosition();
		if(caretLine < displayManager.getFirstVisibleLine())
		{
			caretLine = displayManager.getNextVisibleLine(
				caretLine);
		}

		int newCaret;

		if(getFirstLine() + getVisibleLines() >= displayManager
			.getScrollLineCount())
		{
			int lastVisibleLine = displayManager
				.getLastVisibleLine();
			newCaret = getLineEndOffset(lastVisibleLine) - 1;
		}
		else
		{
			int caretScreenLine = getScreenLineOfOffset(caret);

			scrollDownPage();

			newCaret = xToScreenLineOffset(caretScreenLine,
				magic,true);
		}

		if(select)
			extendSelection(caret,newCaret);
		else if(!multi)
			selectNone();

		moveCaretPosition(newCaret,false);

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
			if(!displayManager.isLineVisible(i))
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
	 * Note that if the "view.eatWhitespace" boolean propery is false,
	 * this method moves the caret to the end of the current word instead.
	 * @since jEdit 2.7pre2
	 */
	public void goToNextWord(boolean select)
	{
		goToNextWord(select,false);
	} //}}}

	//{{{ goToNextWord() method
	/**
	 * Moves the caret to the start of the next word.
	 * @since jEdit 4.1pre5
	 */
	public void goToNextWord(boolean select, boolean eatWhitespace)
	{
		int lineStart = getLineStartOffset(caretLine);
		int newCaret = caret - lineStart;
		String lineText = getLineText(caretLine);

		if(newCaret == lineText.length())
		{
			int nextLine = displayManager.getNextVisibleLine(caretLine);
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
			newCaret = TextUtilities.findWordEnd(lineText,
				newCaret + 1,noWordSep,true,eatWhitespace);

			newCaret += lineStart;
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
		Selection s = getSelectionAtOffset(caret);

		if(caret == 0)
		{
			getToolkit().beep();
			return;
		}

		if(!select && s instanceof Selection.Range)
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

		int extraStartVirt = 0;
		int extraEndVirt = 0;
		int newCaret = caret;

		if(select && caret == getLineEndOffset(caretLine) - 1)
		{
			if(s instanceof Selection.Rect)
			{
				extraStartVirt = ((Selection.Rect)s).extraStartVirt;
				extraEndVirt = ((Selection.Rect)s).extraEndVirt;
				if(caret == s.start)
				{
					if(extraStartVirt == 0)
						newCaret = caret - 1;
					else
						extraStartVirt--;
				}
				else
				{
					if(extraEndVirt == 0)
						newCaret = caret - 1;
					else
						extraEndVirt--;
				}
			}
			else
				newCaret = caret - 1;
		}
		else if(caret == getLineStartOffset(caretLine))
		{
			int line = displayManager.getPrevVisibleLine(caretLine);
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
			extendSelection(caret,newCaret,extraStartVirt,extraEndVirt);
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
		Selection s = getSelectionAtOffset(caret);
		boolean rectSelect = (s == null ? rectangularSelectionMode
			: s instanceof Selection.Rect);
		int magic = getMagicCaretPosition();

		int newCaret = chunkCache.getAbovePosition(caretLine,
			caret - buffer.getLineStartOffset(caretLine),magic + 1,
			rectSelect && select);
		if(newCaret == -1)
		{
			int start = getLineStartOffset(caretLine);
			if(caret == start)
			{
				getToolkit().beep();
				return;
			}
			else
				newCaret = start;
		}

		if(select)
		{
			RectParams params = getRectParams(caret,newCaret);
			int extraStartVirt;
			int extraEndVirt;
			if(params == null)
			{
				extraStartVirt = 0;
				extraEndVirt = 0;
			}
			else
			{
				extraStartVirt = params.extraStartVirt;
				extraEndVirt = params.extraEndVirt;
				newCaret = params.newCaret;
			}
			extendSelection(caret,newCaret,extraStartVirt,extraEndVirt);
		}
		else if(!multi)
			selectNone();

		moveCaretPosition(newCaret);

		setMagicCaretPosition(magic);
	} //}}}

	//{{{ goToPrevPage() method
	/**
	 * Moves the caret to the previous screenful.
	 * @since jEdit 2.7pre2
	 */
	public void goToPrevPage(boolean select)
	{
		scrollToCaret(false);
		int magic = getMagicCaretPosition();

		if(caretLine < displayManager.getFirstVisibleLine())
		{
			caretLine = displayManager.getNextVisibleLine(
				caretLine);
		}

		int newCaret;

		if(getFirstLine() == 0)
		{
			int firstVisibleLine = displayManager
				.getFirstVisibleLine();
			newCaret = getLineStartOffset(firstVisibleLine);
		}
		else
		{
			int caretScreenLine = getScreenLineOfOffset(caret);

			scrollUpPage();

			newCaret = xToScreenLineOffset(caretScreenLine,
				magic,true);
		}

		if(select)
			extendSelection(caret,newCaret);
		else if(!multi)
			selectNone();
		moveCaretPosition(newCaret,false);

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
			if(!displayManager.isLineVisible(i))
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
		goToPrevWord(select,false);
	} //}}}

	//{{{ goToPrevWord() method
	/**
	 * Moves the caret to the start of the previous word.
	 * @since jEdit 4.1pre5
	 */
	public void goToPrevWord(boolean select, boolean eatWhitespace)
	{
		int lineStart = getLineStartOffset(caretLine);
		int newCaret = caret - lineStart;
		String lineText = getLineText(caretLine);

		if(newCaret == 0)
