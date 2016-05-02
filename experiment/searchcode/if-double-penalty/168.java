/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id: XMLTestCase.java 38 2006-06-26 18:39:10Z simon $ */

package cc.creativecomputing.gui.text;

import java.text.BreakIterator;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import cc.creativecomputing.graphics.font.CCFont;
import cc.creativecomputing.graphics.font.CCFontIO;
import cc.creativecomputing.gui.text.linebreaking.Box;
import cc.creativecomputing.gui.text.linebreaking.BoxPenaltyWText;
import cc.creativecomputing.gui.text.linebreaking.BoxWPointer;
import cc.creativecomputing.gui.text.linebreaking.BoxWText;
import cc.creativecomputing.gui.text.linebreaking.EOPPenalty;
import cc.creativecomputing.gui.text.linebreaking.EOPPenaltyImpl;
import cc.creativecomputing.gui.text.linebreaking.Element;
import cc.creativecomputing.gui.text.linebreaking.LineBreakingException;
import cc.creativecomputing.gui.text.linebreaking.MinOptMax;
import cc.creativecomputing.gui.text.linebreaking.Node;
import cc.creativecomputing.gui.text.linebreaking.Paragraph;
import cc.creativecomputing.gui.text.linebreaking.Penalty;
import cc.creativecomputing.gui.text.linebreaking.PenaltyWText;
import cc.creativecomputing.gui.text.linebreaking.Paragraph.WhiteSpaceTreatment;

/**
 * 
 */
public class CCTextBreaker{

	public enum TextAlign {
		START, CENTER, END, JUSTIFY, NAME, REFERENCE;
	};

	public static float RAGGED_STRETCH = 5;
	public static float NAME_CLEARANCE = 1.5f, NAME_CLEARANCE_STRETCH = 3f;
	public static float REFERENCE_CLEARANCE = 0, REFERENCE_CLEARANCE_STRETCH = 3f;
	
	private Map<TextAlign, MinOptMax> lbWidthsBefore, lbWidthsAfter;
	private Map<TextAlign, Integer> lastLineMaxEndValues, lastLineMaxStartValues;
	private Map<TextAlign, String> lbWidthsBeforeText, lbWidthsAfterText;
	
	private static String halfEOPString = "   ", EOPString = "      ";
	private static String startBAPString = "<", endBAPString = ">";
	private static String letterspaceWordBoundary = "+";

	private StringBuffer _myTextBufer;
	private Paragraph _myParagraph;
	
	private List<Insertion> _myInsertions;
	
	private TextAlign _myTextAlign = TextAlign.START;
	private TextAlign _myTextAlignLast = TextAlign.START;
	
	private MinOptMax lbWidthBefore, lbWidthAfter, spaceWidth;
	private int lbPenaltyValue;
	private MinOptMax lastLineAdditionalMaxStart;
	private int lastLineMaxEnd;
	private String lbWidthBeforeText, lbWidthAfterText;
	
	private WhiteSpaceTreatment _myWhiteSpaceTreatment = WhiteSpaceTreatment.SURROUNDING;
	private List<Float> _myRequiredLineLength = new ArrayList<Float>();
	private int _myTolerance = 40;
	private int _myLoseness = -5;
	private float _myRaggedStretch = RAGGED_STRETCH;

	protected CCHyphenator _myHyphenator;
	protected CCFont _myFont;

	/**
	 * Default constructor
	 */
	public CCTextBreaker(final CCHyphenator theHyphonetor, CCFont theFont) {
		_myHyphenator = theHyphonetor;
		_myFont = theFont;
		requiredLineLength(500);
		initLineBreakWidth(_myFont.width(" ") * _myFont.size());
	}
	
	private void initLineBreakWidth(final float theSpaceWidth){
		MinOptMax stretch = new MinOptMax(0, 0, RAGGED_STRETCH * theSpaceWidth);
		MinOptMax halfstretch = MinOptMax.multiply(stretch, 0.5);
		MinOptMax zero = new MinOptMax();
		MinOptMax nameBefore = new MinOptMax(
			NAME_CLEARANCE * theSpaceWidth, 
			NAME_CLEARANCE * theSpaceWidth, 
			NAME_CLEARANCE * theSpaceWidth + NAME_CLEARANCE_STRETCH * theSpaceWidth
		);
		MinOptMax referenceAfter = new MinOptMax(
			REFERENCE_CLEARANCE * theSpaceWidth, 
			REFERENCE_CLEARANCE * theSpaceWidth, 
			REFERENCE_CLEARANCE * theSpaceWidth + REFERENCE_CLEARANCE_STRETCH * theSpaceWidth
		);
		
		String stretchString = "    ";
		String halfstretchString = "  ";
		String nameBeforeString = "  ";
		String referenceAfterString = " ";

		lbWidthsBefore = new EnumMap<TextAlign, MinOptMax>(TextAlign.class);
		lbWidthsBefore.put(TextAlign.START, stretch);
		lbWidthsBefore.put(TextAlign.CENTER, halfstretch);
		lbWidthsBefore.put(TextAlign.END, zero);
		lbWidthsBefore.put(TextAlign.JUSTIFY, zero);
		lbWidthsBefore.put(TextAlign.NAME, nameBefore);
		lbWidthsBefore.put(TextAlign.REFERENCE, zero);

		lbWidthsAfter = new EnumMap<TextAlign, MinOptMax>(TextAlign.class);
		lbWidthsAfter.put(TextAlign.START, zero);
		lbWidthsAfter.put(TextAlign.CENTER, halfstretch);
		lbWidthsAfter.put(TextAlign.END, stretch);
		lbWidthsAfter.put(TextAlign.JUSTIFY, zero);
		lbWidthsAfter.put(TextAlign.NAME, zero);
		lbWidthsAfter.put(TextAlign.REFERENCE, referenceAfter);

		lbWidthsBeforeText = new EnumMap<TextAlign, String>(TextAlign.class);
		lbWidthsBeforeText.put(TextAlign.START, stretchString);
		lbWidthsBeforeText.put(TextAlign.CENTER, halfstretchString);
		lbWidthsBeforeText.put(TextAlign.END, "");
		lbWidthsBeforeText.put(TextAlign.JUSTIFY, "");
		lbWidthsBeforeText.put(TextAlign.NAME, nameBeforeString);
		lbWidthsBeforeText.put(TextAlign.REFERENCE, "");

		lbWidthsAfterText = new EnumMap<TextAlign, String>(TextAlign.class);
		lbWidthsAfterText.put(TextAlign.START, "");
		lbWidthsAfterText.put(TextAlign.CENTER, halfstretchString);
		lbWidthsAfterText.put(TextAlign.END, stretchString);
		lbWidthsAfterText.put(TextAlign.JUSTIFY, "");
		lbWidthsAfterText.put(TextAlign.NAME, "");
		lbWidthsAfterText.put(TextAlign.REFERENCE, referenceAfterString);

		lastLineMaxEndValues = new EnumMap<TextAlign, Integer>(TextAlign.class);
		lastLineMaxEndValues.put(TextAlign.START, Integer.MAX_VALUE);
		lastLineMaxEndValues.put(TextAlign.CENTER, Integer.MAX_VALUE);
		lastLineMaxEndValues.put(TextAlign.END, 0);
		lastLineMaxEndValues.put(TextAlign.JUSTIFY, 0);
		lastLineMaxEndValues.put(TextAlign.NAME, 0);
		lastLineMaxEndValues.put(TextAlign.REFERENCE, 0);

		lastLineMaxStartValues = new EnumMap<TextAlign, Integer>(TextAlign.class);
		lastLineMaxStartValues.put(TextAlign.START, 0);
		lastLineMaxStartValues.put(TextAlign.CENTER, Integer.MAX_VALUE);
		lastLineMaxStartValues.put(TextAlign.END, Integer.MAX_VALUE);
		lastLineMaxStartValues.put(TextAlign.JUSTIFY, 0);
		lastLineMaxStartValues.put(TextAlign.NAME, 0);
		lastLineMaxStartValues.put(TextAlign.REFERENCE, 0);
	}
	
	public void whiteSpaceTreatment(final WhiteSpaceTreatment theWhiteSpaceTreatment){
		_myWhiteSpaceTreatment = theWhiteSpaceTreatment;
	}
	
	public void textBoxSize(final float theSize) {
		requiredLineLength(theSize);
	}
	
	public void requiredLineLength(final float theRequiredLineLength){
		_myRequiredLineLength.clear();
		_myRequiredLineLength.add(theRequiredLineLength);
	}
	
	public void tolerance(final int theTolerance){
		_myTolerance = theTolerance;
	}
	
	public void loseness(final int theLoseness){
		_myLoseness = theLoseness;
	}
	
	public void textAlign(final TextAlign theTextAlign){
		_myTextAlign = theTextAlign;
	}
	
	public void textAlignLast(final TextAlign theTextAlign){
		_myTextAlignLast = theTextAlign;
	}
	
	public void raggedStretch(final int theRaggedStretch){
		_myRaggedStretch = theRaggedStretch;
	}
	
	public List<String> breakText(final String theParagraph){
		return breakText(theParagraph,0);
	}
	
	public List<String> breakText(final String theParagraph, final int theMimumWordSplitLength){
		_myParagraph = new Paragraph(_myWhiteSpaceTreatment, _myRequiredLineLength, _myTolerance, _myLoseness);
		
		if (_myRaggedStretch == RAGGED_STRETCH) {
			lbWidthBefore = lbWidthsBefore.get(_myTextAlign);
			lbWidthAfter = lbWidthsAfter.get(_myTextAlign);
		} else {
			double factor = (double) _myRaggedStretch / (double) RAGGED_STRETCH;
			lbWidthBefore = MinOptMax.multiply(lbWidthsBefore.get(_myTextAlign), factor);
			lbWidthAfter = MinOptMax.multiply(lbWidthsAfter.get(_myTextAlign), factor);
		}
		lbWidthBeforeText = lbWidthsBeforeText.get(_myTextAlign);
		lbWidthAfterText = lbWidthsAfterText.get(_myTextAlign);
		lbPenaltyValue = 0;
		if (lbWidthAfter.isNonZero()) {
			_myParagraph.elements().add(addTextAlign(new PenaltyWText("", "")));
		}
//		if (lbWidthBefore.isNonZero() || lbWidthAfter.isNonZero()) {
			spaceWidth = new MinOptMax(_myFont.width(" ")*_myFont.size());
//		} else {
//			spaceWidth = fixedSpace;
//		}

		lastLineMaxEnd = lastLineMaxEndValues.get(_myTextAlignLast);
		lastLineAdditionalMaxStart = MinOptMax.decr(new MinOptMax(0, 0, lastLineMaxStartValues.get(_myTextAlignLast)), new MinOptMax(0, 0, lbWidthAfter.getMax()));
	
		_myTextBufer = new StringBuffer(theParagraph);
		_myInsertions = new ArrayList<Insertion>();
		addTextToParagraph(true, theMimumWordSplitLength);
		
		_myParagraph.elements().remove(_myParagraph.elements().size() - 1);
		
		EOPPenalty penalty = new EOPPenaltyImpl(new MinOptMax(0, 0, lastLineMaxEnd));
		penalty.setLastLineAdditionalMaxStart(lastLineAdditionalMaxStart);
		
		_myParagraph.elements().add(penalty);
		
		
		try {
			return linebreakPara();
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			System.out.println("# FOUND NO LINEBRAKS");
//			System.out.println("# LANGUAGE:" + _myHyphenator.language());
//			System.out.println("# TEXT    :" + theParagraph);
			return breakTextFitFirst(theParagraph);
		}
	}
	
	public List<String> breakTextFitFirst(final String theText) {
		final List<String> myResult = new ArrayList<String>();
		final float mySpaceWidth = _myFont.spaceWidth() * _myFont.size();
		final float myTextWidth = _myRequiredLineLength.get(0);
		float myRunningX = 0; 

		final int myStringLength = theText.length();
		
		char[] _myTextBuffer = new char[myStringLength + 10];
		theText.getChars(0, myStringLength, _myTextBuffer, 0);

		int wordStart = 0;
		int wordStop = 0;
		int lineStart = 0;
		int index = 0;
		
		while (index < myStringLength){
			if ((_myTextBuffer[index] == ' ') || (index == myStringLength - 1)){
				// boundary of a word
				float wordWidth = _myFont.width(_myTextBuffer, wordStart, index) * _myFont.size();

				if (myRunningX + wordWidth > myTextWidth){
					if (myRunningX == 0){ // boxX1) {
						// if this is the first word, and its width is
						// greater than the width of the text box,
						// then break the word where at the max width,
						// and send the rest of the word to the next line.
						do{
							index--;
							if (index == wordStart){
								// not a single char will fit on this line. screw 'em.
								return myResult;
							}
							wordWidth = _myFont.width(_myTextBuffer, wordStart, index) * _myFont.size();
						}while (wordWidth > myTextWidth);
						myResult.add(new String(_myTextBuffer, lineStart, index - lineStart));
					}else{
						// next word is too big, output current line
						// and advance to the next line
						myResult.add(new String(_myTextBuffer, lineStart, wordStop - lineStart));
						// only increment index if a word wasn't broken inside the
						// do/while loop above.. also, this is a while() loop too,
						// because multiple spaces don't count for shit when they're
						// at the end of a line like this.

						index = wordStop; // back that ass up
						while ((index < myStringLength) && (_myTextBuffer[index] == ' ')){
							index++;
						}
					}
					lineStart = index;
					wordStart = index;
					wordStop = index;
					myRunningX = 0;
				}else{
					myRunningX += wordWidth + mySpaceWidth;
					// on to the next word
					wordStop = index;
					wordStart = index + 1;
				}
			}else if (_myTextBuffer[index] == '\n'){
				if (lineStart != index){ // if line is not empty
					myResult.add(new String(_myTextBuffer, lineStart, index - lineStart));
				}
				lineStart = index + 1;
				wordStart = lineStart;
				myRunningX = 0;
			}
			index++;
		}
		if ((lineStart < myStringLength) && (lineStart != index)){
			myResult.add(new String(_myTextBuffer, lineStart, index - lineStart));
		}
		return myResult;
	}

	/**
	 * constructs the abstract representation of the accumulated text and adds it to the paragraph;
	 * 
	 * @param lastChunk true if this is the last chunk of text of the paragraph
	 */
	private void addTextToParagraph(boolean lastChunk, final int theMimumWordSplitLength) {
		// transform linefeed into space (our only linefeed treatment)
		for (int linefeedIndex = _myTextBufer.indexOf("\n", 0); linefeedIndex != -1; linefeedIndex = _myTextBufer.indexOf("\n", linefeedIndex)) {
			_myTextBufer.setCharAt(linefeedIndex, ' ');
		}
		BreakIterator iter = BreakIterator.getLineInstance();
		int mySize = _myTextBufer.length() / 4;
		for(int i = 0; i < mySize;i++){
		_myTextBufer.append("     ");
		}
		String textString = _myTextBufer.toString();
//		System.out.println("BEFORE REPLACE:"+textString);
		textString = textString.replaceAll("-", "_");
		textString = textString.replaceAll("???", "_");
		textString = textString.replaceAll("???", "_");
//		System.out.println("AFTER REPLACE:"+textString);
		
		textString = _myHyphenator.hyphenateText(textString,theMimumWordSplitLength);
//		System.out.println("AFTER HYPH:"+textString);
//
//		System.out.println(textString);
		iter.setText(textString);
		CharacterIterator charIter = new StringCharacterIterator(textString);// iter.getText().clone();
		int done;
		if (lastChunk) {
			done = BreakIterator.DONE;
		} else {
			done = iter.last();
		}
		// loop over linebreak opportunities
		int start = iter.first();
		for (int end = iter.next(); end != done; end = iter.next()) {
			// this is the start index of an insertion;
			// activate the insertion and insert it.
			Box box = getBAPBoxHere(start, true);
			if (box != null) {
				_myParagraph.elements().add(box);
			}
			// loop over characters of the chunk between linebreak opportunities
			int startItem = start;
			char nextChar = charIter.setIndex(start);
			boolean itemIsSpace = (nextChar == ' ');
			for (nextChar = charIter.next(); charIter.getIndex() != end; nextChar = charIter.next()) {
				boolean nextItemIsSpace = (nextChar == ' ');
				int nextStartItem = charIter.getIndex();
				// change of character type, suppressible or not;
				// or this is the index of a pending insertion;
				// create a box for the preceding part
				if (nextItemIsSpace != itemIsSpace || hasBAPBoxHere(nextStartItem)) {
					int numChars = textString.codePointCount(startItem, nextStartItem);
					if (!itemIsSpace) {
						MinOptMax letterSpace = getLetterSpaceHere(startItem);
						String myString = textString.substring(startItem, nextStartItem);
						
						MinOptMax charWidth = new MinOptMax(_myFont.width(myString) * _myFont.size());
						BoxWPointer charBox = new BoxWPointer(charWidth, textString, startItem, nextStartItem);
						if (letterSpace != null) {
							charWidth.add(MinOptMax.multiply(letterSpace, numChars - 1));
							charBox.setLetterSpacing(true);
							Box halfLS = new BoxWText(MinOptMax.multiply(letterSpace, 0.5), true, false, letterspaceWordBoundary);
							_myParagraph.elements().add(halfLS);
							_myParagraph.elements().add(charBox);
							_myParagraph.elements().add(halfLS);
						} else {
							_myParagraph.elements().add(charBox);
						}
					} else {
						MinOptMax mom;
						if (numChars == 1) {
							mom = spaceWidth;
						} else {
							mom = MinOptMax.multiply(spaceWidth, numChars);
						}
						_myParagraph.elements().add(new BoxWPointer(mom, true, false, textString, startItem, nextStartItem));
					}
					// insert the pending insertion element
					box = getBAPBoxHere(nextStartItem);
					if (box != null) {
						_myParagraph.elements().add(box);
					}
					itemIsSpace = nextItemIsSpace;
					startItem = nextStartItem;
				}
			} // end of loop over characters of the chunk between linebreak
				// opportunities
			// inspect the last character of the chunk
			int endItem = end;
			int penaltyValue = lbPenaltyValue;
			MinOptMax hyphenwidth = null;
			int lastChar = textString.codePointBefore(end);
			// soft hyphen
			if (lastChar == (int)HYPH) {
				--endItem;
				penaltyValue += 50;
				hyphenwidth = new MinOptMax(_myFont.width("-") * _myFont.size());
			}
			// hyphen
			else if (lastChar == (int)'+') {
				penaltyValue += 50;
				hyphenwidth = new MinOptMax();
			}
			// zero-width space
			else if (nextChar == 0x200B) {
				--endItem;
			}
			
			startItem = Math.min(startItem, _myTextBufer.length()-1);
			endItem = Math.min(endItem, _myTextBufer.length()-1);
			int numChars = _myTextBufer.codePointCount(startItem, endItem);
			if (!itemIsSpace) {
				String myString = textString.substring(startItem, endItem);
				_myParagraph.elements().add(new BoxWPointer(new MinOptMax(_myFont.width(myString) * _myFont.size()), textString, startItem, endItem));
			} else {
				MinOptMax mom;
				if (numChars == 1) {
					mom = spaceWidth;
				} else {
					mom = MinOptMax.multiply(spaceWidth, numChars);
				}
				_myParagraph.elements().add(new BoxWPointer(mom, true, false, textString, startItem, endItem));
			}
			// if this is the end index of an insertion, get it
			box = getBAPBoxHere(end, false);
			Penalty penalty = null;
			// otherwise, if there is a BAP penalty, get it
			if (box == null) {
				penalty = getBAPPenaltyHere(end, penaltyValue, hyphenwidth);
			}
			// otherwise a hyphenated or non-hyphenated penalty
			if (penalty == null) {
				if (hyphenwidth != null) {
					// if this is the end index of an insertion and a soft
					// hyphen,
					// change the box into a BoxPenalty
					if (box != null && hyphenwidth.isNonZero()) {
						penalty = new BoxPenaltyWText(penaltyValue, MinOptMax.add(hyphenwidth, box.getWidth()), new MinOptMax(), true, "-" + box.toString(), "", box.getWidth(),
								box.isSuppressible(), box.isBP(), box.toString());
						box = null;
					} else {
						penalty = new PenaltyWText(50, hyphenwidth, new MinOptMax(), true, !hyphenwidth.isNonZero() ? "" : "-", "");
					}
				} else {
					penalty = new PenaltyWText(penaltyValue, "", "");
				}
			}
			addTextAlign(penalty);
			if (box != null) {
				_myParagraph.elements().add(box);
			}
			_myParagraph.elements().add(penalty);
			start = end;
		} // end of loop over linebreak opportunities
		// remove the part of the text that we added to the paragraph
		if (lastChunk) {
			_myTextBufer = null;
			// error if insertions is not empty
		} else {
			_myTextBufer.delete(0, start);
			reindexInsertions(start, 0);
		}
	}

	/**
	 * @param index the index in the text
	 * @return true if there is a BAP box at this index
	 */
	private boolean hasBAPBoxHere(int index) {
		if (_myInsertions.size() == 0) {
			return false;
		}
		Insertion insertion = _myInsertions.get(0);
		return (index == insertion.startIndex || index == insertion.endIndex) && insertion.BAPwidth != null;
	}

	/**
	 * @param index the index in the text
	 * @return the BAP box at this index, or null if there is none
	 */
	private Box getBAPBoxHere(int index) {
		if (_myInsertions.size() == 0) {
			return null;
		}
		Insertion insertion = _myInsertions.get(0);
		if (index == insertion.startIndex) {
			return insertion.getBAPBox(index);
		} else if (index == insertion.endIndex) {
			_myInsertions.remove(0);
			return insertion.getBAPBox(index);
		} else {
			return null;
		}
	}

	/**
	 * @param index
	 *            the index in the text
	 * @param start
	 *            true if a start box is required, false if an end box
	 * @return the BAP box at this index, or null if there is none
	 */
	private Box getBAPBoxHere(int index, boolean start) {
		if (_myInsertions.size() == 0) {
			return null;
		}
		Insertion insertion = _myInsertions.get(0);
		if (start && index == insertion.startIndex) {
			return insertion.getBAPBox(index);
		} else if (!start && index == insertion.endIndex) {
			_myInsertions.remove(0);
			return insertion.getBAPBox(index);
		} else {
			return null;
		}
	}

	/**
	 * @param index the index in the text
	 * @param penalty the penalty value
	 * @return the BAP penalty at this index, or null if there is none
	 */
	private PenaltyWText getBAPPenaltyHere(int index, int penalty, MinOptMax hyphenwidth) {
		if (_myInsertions.size() == 0) {
			return null;
		}
		Insertion insertion = _myInsertions.get(0);
		if (insertion.isActive(index) && !insertion.BAPdiscard && index != insertion.endIndex) {
			return insertion.getBAPPenalty(index, penalty, hyphenwidth);
		} else {
			return null;
		}
	}

	/**
	 * @param index the index in the text
	 * @return the letter space length at this index, or null if there is none
	 */
	private MinOptMax getLetterSpaceHere(int index) {
		if (_myInsertions.size() == 0) {
			return null;
		}
		Insertion insertion = _myInsertions.get(0);
		if (insertion.isActive(index)) {
			return insertion.letterSpace;
		} else {
			return null;
		}
	}

	/**
	 * recompute the indexes of the insertions after the text StringBuffer has
	 * been shortened
	 * 
	 * @param before
	 *            text length before shortening
	 * @param after
	 *            text length after shortening
	 */
	private void reindexInsertions(int before, int after) {
		int decrement = before - after;
		for (Insertion i : _myInsertions) {
			// error if index becomes negative
			i.startIndex -= decrement;
			if (i.endIndex >= 0) {
				i.endIndex -= decrement;
			}
		}
	}

	private Penalty addTextAlign(Penalty penalty) {
		if (lbWidthBefore.isNonZero()) {
			penalty.getWidthBefore().add(lbWidthBefore);
			if (penalty instanceof PenaltyWText) {
				PenaltyWText penaltyWText = (PenaltyWText) penalty;
				penaltyWText.setTextBefore(penaltyWText.getTextBefore() + lbWidthBeforeText);
			}
		}
		if (lbWidthAfter.isNonZero()) {
			penalty.getWidthAfter().add(lbWidthAfter);
			if (penalty instanceof PenaltyWText) {
				PenaltyWText penaltyWText = (PenaltyWText) penalty;
				penaltyWText.setTextAfter(lbWidthAfterText + penaltyWText.getTextAfter());
			}
		}

		return penalty;
	}

	private static float ss(MinOptMax length, double adjRatio) {
		if (adjRatio == 0) {
			return length.getOpt();
		} else if (adjRatio >= 0) {
			return length.getOpt() + (int) (adjRatio * (length.getStretch()));
		} else {
			return length.getOpt() + (int) (adjRatio * (length.getShrink()));
		}
	}

	private static float ss(MinOptMax end, MinOptMax begin, double adjRatio) {
		MinOptMax length = new MinOptMax(end);
		length.decr(begin);
		return ss(length, adjRatio);
	}

	public List<String> linebreakPara() {
		final List<String> myResult = new ArrayList<String>();
		Node lbNode = null;
		try {
			lbNode = _myParagraph.doLineBreaking();
		} catch (LineBreakingException e) {
			System.err.println("Error in linebreaking");
			e.printStackTrace();
			System.exit(1);
		}
		if (lbNode == null) {
			throw new RuntimeException("No feasible linebreaks found");
		}
		List<Node> lineBreakNodes = new ArrayList<Node>();
		while (lbNode != null) {
			lineBreakNodes.add(0, lbNode);
			lbNode = lbNode.getPrevious();
		}

		Node prevLineBreakNode = lineBreakNodes.get(0);
		int prevLBPos = prevLineBreakNode.getLBPos();
		Penalty prevLineBreak = (Penalty) _myParagraph.elements().get(prevLBPos);

		for (Node lineBreakNode : lineBreakNodes.subList(1, lineBreakNodes.size())) {
			int lbPos = lineBreakNode.getLBPos();
			Penalty lineBreak = (Penalty) _myParagraph.elements().get(lbPos);
			int startPos = prevLineBreakNode.getLineAfterStartPos();
			int endPos = lineBreakNode.getLineBeforeEndPos();
			double adjRatio = lineBreakNode.getAdjRatio();

			int lineLength = 0, lineLength2 = 0;
			MinOptMax w;
			StringBuffer s = new StringBuffer();

			// width after of previous line break at prevLBPos
			w = prevLineBreak.getWidthAfter();
			if (w.isNonZero()) {
				float l = ss(w, adjRatio);
				if (l != 0) {
					s.append(prevLineBreak.toStringAfter());
					lineLength += l;
					lineLength2 += l;
				}
				// print last line alignment
				else if (lbPos == _myParagraph.elements().size() - 1 && w.hasInfiniteStretch()) {
					if (_myTextAlignLast == TextAlign.CENTER) {
						s.append(halfEOPString);
					} else {
						s.append(EOPString);
					}
				}
			}

			// border/padding after linebreak from prevLBPos + 1 up to startPos
			// (not inclusive)
			for (Element e : _myParagraph.elements().subList(prevLBPos + 1, startPos)) {
				if (e.isBP()) {
					float l = ss(e.getWidth(), adjRatio);
					if (l != 0) {
						s.append(e);
						lineLength += l;
					}
				}
			}
			lineLength2 += ss(prevLineBreakNode.getBPWidthAfter(), adjRatio);

			// the line elements from startPos up to endPos (not inclusive)
			for (Element e : _myParagraph.elements().subList(startPos, endPos)) {
				w = e.getWidth();
				if (w.isNonZero()) {
					float l = ss(w, adjRatio);
					if (l != 0) {
						s.append(e);
						lineLength += l;
					}
				}
			}
			lineLength2 += ss(lineBreakNode.getTotalBoxWidthBefore(), prevLineBreakNode.getTotalBoxWidthAfter(), adjRatio);

			// border/padding before linebreak from endPos up to lbPos (not
			// inclusive)
			for (Element e : _myParagraph.elements().subList(endPos, lbPos)) {
				if (e.isBP()) {
					float l = ss(e.getWidth(), adjRatio);
					if (l != 0) {
						s.append(e);
						lineLength += l;
					}
				}
			}
			lineLength2 += ss(lineBreakNode.getBPWidthBefore(), adjRatio);

			// penalty at the line break at lbPos
			w = lineBreak.getWidthBefore();
			if (w.isNonZero()) {
				float l = ss(w, adjRatio);
				if (l != 0) {
					s.append(lineBreak.toStringBefore());
					lineLength += l;
					lineLength2 += l;
				}
				// print last line alignment
				else if (lbPos == _myParagraph.elements().size() - 1 && w.hasInfiniteStretch()) {
					if (_myTextAlignLast == TextAlign.CENTER) {
						s.append(halfEOPString);
					} else {
						s.append(EOPString);
					}
				}
			}
			
			myResult.add(s.toString());//.replace('_', '-'));
			// next line
			prevLineBreakNode = lineBreakNode;
			prevLineBreak = lineBreak;
			prevLBPos = lbPos;
		}

		return myResult;
	}

	/**
	 * An object for a span, attached to the text by its start and end indexes.
	 * It can contain a BAPwidth and BAPdiscard, and/or a letterSpace
	 */
	public static class Insertion {

		private MinOptMax BAPwidth = null, letterSpace = null;
		private int startIndex, endIndex = -1;
		private boolean BAPdiscard;

		/**
		 * Constructor with the start index
		 * 
		 * @param startIndex
		 *            the start index
		 */
		private Insertion(int startIndex) {
			this.startIndex = startIndex;
		}

		/**
		 * @param index
		 *            the index in the text
		 * @return whether we are in this span
		 */
		private boolean isActive(int index) {
			return startIndex <= index && (endIndex < 0 || index < endIndex);
		}

		/**
		 * @param index
		 *            the index in the text
		 * @return the BAP box at this index, or null if there is none
		 */
		private BoxWText getBAPBox(int index) {
			if (BAPwidth != null) {
				return new BoxWText(BAPwidth, false, true, index == startIndex ? startBAPString : endBAPString);
			} else {
				return null;
			}
		}

		/**
		 * @param index
		 *            the index in the text
		 * @param penalty
		 *            the penalty value
		 * @return the BAP penalty at this index, or null if there is none
		 */
		private PenaltyWText getBAPPenalty(int index, int penalty, MinOptMax hyphenwidth) {
			if (BAPwidth != null) {
				if (hyphenwidth == null) {
					return new PenaltyWText(penalty, BAPwidth, BAPwidth, false, endBAPString, startBAPString);
				} else {
					return new PenaltyWText(penalty, MinOptMax.add(BAPwidth, hyphenwidth), BAPwidth, true, (hyphenwidth.getOpt() == 0 ? "" : "-") + endBAPString, startBAPString);
				}
			} else {
				return null;
			}
		}

	}
	
	static char HYPH = '-';

	/**
	 * @param args
	 *            the XML file name
	 */
	public static void main(String[] args) throws Exception {
		CCTextBreaker testCase = new CCTextBreaker(new CCHyphenator("de"),CCFontIO.createVectorFont("Arial", 20));
		List<String> myStrings = testCase.breakText(
			"Gl&#x;h-birnen ver-brauchen sehr viel Energie. L&#x160;sst man sie " +
			"t&#x160;glich zwei Stunden brennen, macht die Nutzung &#x;bers Jahr " +
			"gerechnet 99 % ihres &#x161;kologischen Rucksacks aus. Als Verbraucher " +
			"kann man dieses Gewicht einfach verringern, indem man Energiesparlampen " +
			"verwendet und das Licht immer ausknipst, wenn man es nicht braucht. " +
			"Strom aus alternativen Energiequellen verbraucht au§erdem weniger " +
			"Ressourcen als z. B. Strom aus Kohle."
		);
		
		for(String myString:myStrings){
			System.out.println(myString);
		}
	}

}

