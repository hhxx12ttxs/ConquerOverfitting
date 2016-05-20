/*  
 * Copyright (c) 2009  Christian Riekoff <info@texone.org>  
 *  
 *  This file is free software: you may copy, redistribute and/or modify it  
 *  under the terms of the GNU General Public License as published by the  
 *  Free Software Foundation, either version 2 of the License, or (at your  
 *  option) any later version.  
 *  
 *  This file is distributed in the hope that it will be useful, but  
 *  WITHOUT ANY WARRANTY; without even the implied warranty of  
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU  
 *  General Public License for more details.  
 *  
 *  You should have received a copy of the GNU General Public License  
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.  
 *  
 * This file incorporates work covered by the following copyright and  
 * permission notice:  
 */
package cc.creativecomputing.graphics.font.text;

import java.util.ArrayList;
import java.util.List;

import cc.creativecomputing.graphics.CCDrawMode;
import cc.creativecomputing.graphics.CCGraphics;
import cc.creativecomputing.graphics.font.CCFont;
import cc.creativecomputing.math.CCAABoundingRectangle;
import cc.creativecomputing.math.CCMath;
import cc.creativecomputing.math.CCVector2f;
import cc.creativecomputing.math.CCVector3f;

/**
 * @author info
 * 
 */
public class CCText extends CCAbstractText<CCFont<?>>{
	
	/**
	 * Implement this class to react to text changes
	 * @author christianriekoff
	 *
	 */
	public static interface CCTextListener{
		/**
		 * This method is called by the text object, every time its text is changed
		 * @param theText
		 */
		public void onChangeText(CCText theText);
	}
	
	/**
	 * Class to hold the positions and index information for one line of text
	 * @author christianriekoff
	 *
	 */
	public class CCTextGridLine{
		private float _myY;
		private float[] _myX;
		private int[] _myFontCharIndices;
		private int _myIndex;
		private int _mySize;
		
		private int _myStart;
		private int _myEnd;
		
		private boolean _myHasLineBreak;
		
		/**
		 * Creates a new text line
		 * @param theIndex number of the text line
		 * @param theStart position of the the first letter in the text
		 * @param theEnd position of the last letter in the text
		 * @param theTextBuffer buffer holding the complete text as chars
		 * @param theX x position of the line
		 * @param theY y position of the line
		 */
		private CCTextGridLine(
			final int theIndex,
			final int theStart, final int theEnd, final char[] theTextBuffer,
			float theX, final float theY
		) {
			_mySize = theEnd - theStart;
			_myX = new float[_mySize + 1];
			_myFontCharIndices = new int[_mySize];
			
			_myStart = theStart;
			_myEnd = theEnd;
			
			_myIndex = theIndex;
			_myY = theY;
			
			CCTextAlign myTextAlign = _myTextAlign;
			if(myTextAlign == CCTextAlign.JUSTIFY && _myLineBreakMode == CCLineBreakMode.NONE)myTextAlign = CCTextAlign.LEFT;
			
			//reset x
			switch (_myTextAlign) {
			case CENTER:
				theX -= _myFont.width(theTextBuffer, theStart, theEnd) / 2f * _myTextSize;
				break;
			case RIGHT:
				theX -= _myFont.width(theTextBuffer, theStart, theEnd) * _myTextSize;
				break;
			}
			
			// place letters
			
			_myX[0] = theX;
			final float mySpace = (float)(_myWidth - _myFont.width(theTextBuffer, theStart, theEnd)* _myTextSize)/(theEnd - theStart - 1);
			int myLastIndex = -1;

			for (int index = theStart; index < theEnd; index++) {
				final int myArrayIndex = index - theStart;
				final char myChar = theTextBuffer[index];
				final int myIndex = _myFont.index(myChar);
					
				if(myTextAlign == CCTextAlign.JUSTIFY) {
					theX += mySpace;
				}else {
					theX += _myFont.kerning(myLastIndex, myIndex) * _myTextSize;
				}
					
				theX += _myFont.width(myIndex) * _myTextSize * _mySpacing;
				
				_myFontCharIndices[myArrayIndex] = myIndex;
				_myX[myArrayIndex + 1] = theX;
				myLastIndex = myIndex;
			}
			_myHasLineBreak = theTextBuffer[theEnd - 1] == '\n';
		}
		
		/**
		 * Returns the index of the first letter of this line in the text
		 * @return index of the first letter of this line in the text
		 */
		public int start() {
			return _myStart;
		}
		
		/**
		 * Returns the index of the last letter of this line in the text
		 * @return index of the last letter of this line in the text
		 */
		public int end() {
			return _myEnd;
		}
		
		/**
		 * checks if the given index belongs to this line
		 * @param theIndex
		 * @return true if the given index is inside the line otherwise false
		 */
		public boolean isInside(int theIndex) {
			return theIndex >= _myStart && theIndex <= (_myHasLineBreak ? _myEnd -1 :_myEnd);
		}
		
		/**
		 * Number of letters in this line
		 * @return number of letters in this line 
		 */
		public int size() {
			return _mySize + (_myHasLineBreak ? 0 : 1);
		}
		
		/**
		 * Draws all chars of the line at the right positions
		 * @param g graphics object for drawing
		 */
		private void drawTextLine(CCGraphics g) {
			for (int i = 0; i < _myFontCharIndices.length; i++) {
				_myFont.drawChar(
					g, _myFontCharIndices[i], _myTextSize, 
					_myX[i] + _myPosition.x, 
					_myY + _myPosition.y + ascent(), 
					_myPosition.z
				);
			}
		}
		
		/**
		 * Draws the text grid 
		 * @param g graphics object for drawing
		 */
		private void drawGrid(CCGraphics g) {
			for(int i = 0; i < _myX.length;i++) {
				float myX = _myX[i]+ _myPosition.x;
				float myY = _myY + _myPosition.y + ascent();
				
				g.vertex(myX, myY);
				g.vertex(myX, myY - _myTextSize - descent());
			}
		}
		
		/**
		 * Helper to draw text highlighting in case of selection
		 * @param g graphics object for drawing
		 * @param theStartIndex index of the first selected letter
		 * @param theEndIndex index of the last selected letter
		 */
		private void drawHighlight(CCGraphics g, int theStartIndex, int theEndIndex) {
			if(theStartIndex > _myEnd)return;
			if(theEndIndex < _myStart)return;
			
			theStartIndex = CCMath.constrain(theStartIndex - _myStart, 0, _myX.length - 1);
			theEndIndex = CCMath.constrain(theEndIndex - _myStart, 0, _myX.length - 1);
			float myX1 = _myX[theStartIndex]+ _myPosition.x;
			float myX2 = _myX[theEndIndex]+ _myPosition.x;
			float myY = _myY + _myPosition.y + ascent();
			
			g.vertex(myX1, myY);
			g.vertex(myX1, myY - _myTextSize - descent());
			g.vertex(myX2, myY - _myTextSize - descent());
			g.vertex(myX2, myY);
		}
		
		/**
		 * Returns the letter index for the given 2d position
		 * @param thePosition
		 * @return letter index for the given 2d position
		 */
		private int gridIndex(CCVector2f thePosition) {
			float myY = _myY + _myPosition.y + ascent();
			
			if(
				_myIndex != 0 && thePosition.y > myY || 
				_myIndex != _myTextGrid._myGridLines.size() - 1 && thePosition.y < (myY - _myLeading)
			)return -1;
			
			return gridIndex(thePosition.x);
		}
		
		private int gridIndex(float theX) {
			for(int i = 0; i < _myX.length - 1;i++) {
				if(theX < (_myX[i] + _myX[i + 1]) / 2 + _myPosition.x)return i + _myStart;
			}
			
			return _myStart + size() - 1;
		}
		
		private float gridPos(int theIndex) {
			return _myX[theIndex - _myStart] + _myPosition.x;
		}
		
		float width() {
			return _myX[_myX.length - 1];
		}
	}
	
	/**
	 * This is a helper class to get the bounding boxes for every letter
	 * as grid. This can be used for text highlighting or selection. 
	 * @author christianriekoff
	 *
	 */
	public class CCTextGrid{
		
		private List<CCTextGridLine> _myGridLines = new ArrayList<CCTextGridLine>();
		
		public CCTextGrid() {
			
		}
		
		public void clear() {
			_myGridLines.clear();
		}
		
		CCTextGridLine createGridLine(
			final int theIndex,
			final int theStart, final int theStop, final char[] theTextBuffer,
			float theX, final float theY
		) {
			String myText = new String(theTextBuffer, theStart, theStop - theStart);
			if(myText.trim().equals(""))return null;
			
			CCTextGridLine myResult = new CCTextGridLine(theIndex, theStart, theStop, theTextBuffer, theX, theY);
			_myGridLines.add(myResult);
			return myResult;
		}
		
		public void drawText(CCGraphics g) {
			for(CCTextGridLine myGridLine:_myGridLines) {
				myGridLine.drawTextLine(g);
			}
		}
		
		public void drawGrid(CCGraphics g) {
			g.beginShape(CCDrawMode.LINES);
			for(CCTextGridLine myGridLine:_myGridLines) {
				myGridLine.drawGrid(g);
			}
			g.endShape();
		}
		
		public void drawHeighlight(CCGraphics g, int theStart, int theEnd) {
			if(theEnd < theStart) {
				int myTmp = theEnd;
				theEnd = theStart;
				theStart = myTmp;
			}
			
			g.beginShape(CCDrawMode.QUADS);
			for(CCTextGridLine myLine:_myGridLines) {
				myLine.drawHighlight(g, theStart, theEnd);
			}
			g.endShape();
		}
		
		public int gridIndex(CCVector2f thePosition) {
			
			for(CCTextGridLine myLine:_myGridLines) {
				int myLineX = myLine.gridIndex(thePosition);
				if(myLineX < 0) {
					continue;
				}
				return myLineX;
			}
			
			return 0;
		}
		
		public int upperIndex(float theX, int theIndex) {
			int myLine = gridLine(theIndex);
			
			if(myLine > 0)myLine--;
			int myResult = _myGridLines.get(myLine).gridIndex(theX);
			return myResult;
		}
		
		public int lowerIndex(float theX, int theIndex) {
			int myLine = gridLine(theIndex);
			if(myLine < _myGridLines.size() - 1)myLine++;
			return _myGridLines.get(myLine).gridIndex(theX);
		}
		
		public int gridLine(int theIndex) {
			for(CCTextGridLine myLine:_myGridLines) {
				if(myLine.isInside(theIndex)) {
					return myLine._myIndex;
				}
			}
			return 0;
		}
		
		public CCVector2f gridPosition(int theIndex) {
			CCVector2f myResult = new CCVector2f(0,0);
			
			for(CCTextGridLine myLine:_myGridLines) {
				if(myLine.isInside(theIndex)) {
					myResult.x = myLine.gridPos(theIndex);
					myResult.y = myLine._myY + _myPosition.y + ascent();
					return myResult;
				}
			}
			
			return myResult;
		}
	}

	/**
	 * save the text to display
	 */
	protected String _myText;
	
	/**
	 * rectangle to save the bounding box around the text
	 */
	private CCAABoundingRectangle _myBoundingRectangle;

	protected char[] _myTextBuffer;

	/**
	 * store the current text width
	 */
	private float _myWidth = 0;

	/**
	 * store the current text height
	 */
	protected float _myHeight = 0;
	
	private float _myBlockWidth = 0;
	private float _myBlockHeight = 0;

	protected CCVector3f _myPosition = new CCVector3f();
	
	private CCTextGrid _myTextGrid = new CCTextGrid();
	
	private CCLineBreakMode _myLineBreakMode = CCLineBreakMode.NONE;
	private CCLineBreaking _myLineBreaking = new CCLineBreaking();
	
	private List<CCTextListener> _myTextListeners;

	/**
	 * Create a new Text object with the given Font
	 * 
	 * @param theFont
	 */
	public CCText(final CCFont<?> theFont) {
		super(theFont);
		_myBoundingRectangle = new CCAABoundingRectangle();
		text(" ");
	}
	
	/**
	 * 
	 * @param theListener
	 */
	public void addListener(CCTextListener theListener) {
		if(_myTextListeners == null) {
			_myTextListeners = new ArrayList<CCTextListener>();
		}
		_myTextListeners.add(theListener);
	}
	
	/**
	 * 
	 * @param theListener
	 */
	public void removeListener(CCTextListener theListener) {
		if(_myTextListeners == null) return;
		_myTextListeners.remove(theListener);
	}
	
	private void callListeners() {
		if(_myTextListeners == null) return;
		
		for(CCTextListener myListener:new ArrayList<CCTextListener>(_myTextListeners)) {
			myListener.onChangeText(this);
		}
	}

	/**
	 * Set the text to display
	 * 
	 * @param theText
	 */
	public void text(final String theText) {
		_myText = theText;
		_myTextBuffer = theText.toCharArray();
		_myLineBreaking.breakText(this);
		callListeners();
	}
	
	public void lineBreak(CCLineBreakMode theLineBreak) {
		_myLineBreakMode = theLineBreak;
		switch(theLineBreak) {
		case NONE:
			_myLineBreaking = new CCLineBreaking();
			break;
		case BLOCK:
			_myLineBreaking = new CCFirstFitFirst();
			break;
		}
		_myLineBreaking.breakText(this);
		callListeners();
	}
	
	public CCLineBreakMode lineBreak() {
		return _myLineBreakMode;
	}
	
	public void text(Number theNumber) {
		text(theNumber.toString());
	}
	
	public void text(final int theText) {
		text(Integer.toString(theText));
	}
	
	public void text(final char theChar) {
		text(Character.toString(theChar));
	}
	
	public void text(final float theText) {
		text(Float.toString(theText));
	}
	
	public void text(final double theText) {
		text(Double.toString(theText));
	}

	/**
	 * Returns the current text.
	 * @return the current text.
	 */
	public String text() {
		return _myText;
	}
	
	private void onChangeText() {
		
	}
	
	public char delete(int theIndex) {
		if(theIndex < 0)return ' ';
		char myDeletedChar = _myText.charAt(theIndex);
		String myNewText = _myText.substring(0,theIndex);
		myNewText = myNewText.concat(_myText.substring(theIndex + 1));
		_myText = myNewText;
		_myLineBreaking.breakText(this);
		callListeners();
		return myDeletedChar;
	}
	
	public void append(int theIndex, String theString) {
		String myNewText = _myText.substring(0,theIndex+1);
		myNewText = myNewText + theString;
		myNewText = myNewText.concat(_myText.substring(theIndex+1));
		_myText = myNewText;
		_myLineBreaking.breakText(this);
		callListeners();
	}
	
	public void delete(int theStartIndex, int theEndIndex) {
		if(theEndIndex < theStartIndex) {
			int myTemp = theEndIndex;
			theEndIndex = theStartIndex;
			theStartIndex = myTemp;
		}
		String myNewText = _myText.substring(0,theStartIndex+1);
		myNewText = myNewText.concat(_myText.substring(theEndIndex + 1));
		_myText = myNewText;
		_myLineBreaking.breakText(this);
		callListeners();
	}
	
	public String text(int theStartIndex, int theEndIndex) {
		if(theEndIndex < theStartIndex) {
			int myTemp = theEndIndex;
			theEndIndex = theStartIndex;
			theStartIndex = myTemp;
		}
		return _myText.substring(theStartIndex + 1,theEndIndex + 1);
	}
	
	public void dimension(final float theWidth, final float theHeight) {
		width(theWidth);
		height(theHeight);
	}

	/**
	 * Return the width of a line of text. If the text has multiple lines, this returns the length of the longest line.
	 * Note this is only recalculated once you have changed the text.
	 * 
	 * @param theString
	 * @return
	 */
	public float width() {
		return _myWidth;
	}
	
	void changeWidth(float theWidth) {
		_myWidth = theWidth;
	}
	
	public void width(float theWidth) {
		_myWidth = theWidth;
		_myBlockWidth = theWidth;
	}
	
	public float height() {
		return _myHeight;
	}
	
	void changeHeight(float theHeight) {
		_myHeight = theHeight;
	}
	
	public void height(float theHeight) {
		_myHeight = theHeight;
		_myBlockHeight = theHeight;
	}
	
	float blockHeight() {
		return _myBlockHeight;
	}
	
	float blockWidth() {
		return _myBlockWidth;
	}
	
	/**
	 * Returns the bounding rectangle around the text.
	 * @return the bounding rectangle around the text.
	 */
	public CCAABoundingRectangle boundingBox(){
		_myBoundingRectangle.min().x = _myPosition.x();
		_myBoundingRectangle.min().y = _myPosition.y() + ascent() - _myHeight;
		_myBoundingRectangle.width(_myWidth);
		_myBoundingRectangle.height(_myHeight);
		return _myBoundingRectangle;
	}
	
	public CCTextGrid textGrid() {
		return _myTextGrid;
	}
	
	/**
	 * Sets the position of the text to the given coordinates.
	 * @param theX new x position for the text
	 * @param theY new y position for the text
	 */
	public void position(final float theX, final float theY) {
		_myPosition.set(theX, theY, 0);
	}
	
	/**
	 * Sets the position of the text to the given vector.
	 * @param thePosition the new position of the text
	 */
	public void position(final CCVector2f thePosition){
		_myPosition.set(thePosition);
	}
	
	/**
	 * Sets the position of the text to the given coordinates.
	 * @param theX new x position for the text
	 * @param theY new y position for the text
	 */
	public void position(final float theX, final float theY, float theZ) {
		_myPosition.set(theX, theY, theZ);
	}
	
	/**
	 * Sets the 3D position of the text to the given vector.
	 * @param thePosition the new position of the text
	 */
	public void position(final CCVector3f thePosition){
		_myPosition.set(thePosition);
	}

	/**
	 * Returns a reference to the position of the text.
	 * @return reference to the position of the text
	 */
	public CCVector3f position() {
		return _myPosition;
	}
	
	public CCVector2f position(int theIndex) {
		return _myTextGrid.gridPosition(theIndex);
	}
	
	/**
	 * Draws the text
	 */
	public void draw(CCGraphics g) {
		_myFont.beginText(g);
		_myTextGrid.drawText(g);
		_myFont.endText(g);
	}

	@Override
	public CCText clone() {
		final CCText _myResult = new CCText(_myFont);
		_myResult._myPosition = _myPosition.clone();
		_myResult.text(_myText);
		_myResult.align(_myTextAlign);
		return _myResult;
	}
}

