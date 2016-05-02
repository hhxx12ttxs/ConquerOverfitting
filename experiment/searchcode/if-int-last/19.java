/*
 * UnicodeData.java
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2003 Mike Dillon
 * Copyright (C) 2012 Max Funk
 * Generated Portions Copyright (C) Unicode, Inc.
 *                    (see charactermap/unicode/LICENSE)
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

package charactermap.unicode;


import java.util.*;
import org.gjt.sp.util.StandardUtilities;


/**
 * A utility class providing access to data structures generated from the
 * Unicode Character Database (UCD). This class provides access to a list of
 * Unicode Blocks and provides a mapping between code points and Unicode
 * character descriptions.
 */
public final class UnicodeData
{
//{{{ Block handling
		/**
	 * A simple data type representing a Unicode Block.
	 */
	public static class Block
	{
		private String name;
		private int first;
		private int last;
		private int length;
		private boolean highBlock;

		/**
		 * Construct a new block for the internal block list.
		 *
		 * @param name the Unicode description for the block.
		 * @param first the first code point in the block.
		 * @param last the last code point in the block.
		 */
		protected Block(String name, int first, int last)
		{
			this.name = name;
			this.first = first;
			this.last = last;

			length = 1 + last - first;
			highBlock = last > 0xFFFF;
		}

		/**
		 * Returns the Unicode description for the block.
		 */
		public String getName()
		{
			return name;
		}

		/**
		 * Returns the first code point in this block as a Java char.
		 * This method is unsuitable for blocks outside of the Basic
		 * Multilingual Plane (Plane 0), i.e. for blocks where the code
		 * points are above the range of a Java char. If you might be
		 * working with such blocks, use <code>isHighBlock()</code> to
		 * determine if this method can be called safely.
		 *
		 * @see #isHighBlock()
		 * @throws java.lang.UnsupportedOperationException if the first
		 * code point is above the range of a Java char.
		 */
		public char getFirstChar()
		{
			if (highBlock)
			{
				throw new UnsupportedOperationException(
					"This block is above the range of a " +
					"Java char; use getFirstPoint()");
			}

			return (char)first;
		}

		/**
		 * Returns the first code point in this block.
		 */
		public int getFirstPoint()
		{
			return first;
		}

		/**
		 * Returns the last code point in this block as a Java char.
		 * This method is unsuitable for blocks outside of the Basic
		 * Multilingual Plane (Plane 0), i.e. for blocks where the code
		 * points are above the range of a Java char. If you might be
		 * working with such blocks, use <code>isHighBlock()</code> to
		 * determine if this method can be called safely.
		 *
		 * @see #isHighBlock()
		 * @throws java.lang.UnsupportedOperationException if the first
		 * code point is above the range of a Java char.
		 */
		public char getLastChar()
		{
			if (highBlock)
			{
				throw new UnsupportedOperationException(
					"This block is above the range of a " +
					"Java char; use getFirstPoint()");
			}

			return (char)last;
		}

		/**
		 * Returns the last code point in this block.
		 */
		public int getLastPoint()
		{
			return last;
		}

		/**
		 * Returns the number of code points in this block.
		 */
		public int length()
		{
			return length;
		}

		/**
		 * Returns whether this block is outside the Unicode Basic
		 * Multilingual Plane (Plane 0).
		 */
		public boolean isHighBlock()
		{
			return highBlock;
		}

		/**
		 * Returns the name of this block as its string representation.
		 */
		@Override
		public String toString()
		{
			return name;
		}
	}
//}}}

//{{{ Main class

	/**
	 * Returns the list of all defined Blocks from the Unicode Character
	 * Database.
	 */
	public static List<Block> getBlocks()
	{
		return Collections.unmodifiableList(UnicodeBlocks.getBlocks());
	}

	/**
	 * Returns the list of low defined Blocks from the Unicode Character
	 * Database.
	 */
	public static List<Block> getLowBlocks()
	{
		List<Block> lowBlockList = new ArrayList<Block>();
		for(Block I : getBlocks())
		{
			if (! I.isHighBlock())
				lowBlockList.add(I);
		}
		return Collections.unmodifiableList(lowBlockList);
	}

	/**
	 * Returns the list of all defined Blocks from the Unicode Character
	 * Database in alphabetic order.
	 */
	public static List<Block> getBlocksABC()
	{
		Block[] blockArrayABC  = new Block[getBlocks().size()];
		blockArrayABC = getBlocks().toArray(blockArrayABC);
		Arrays.sort(blockArrayABC,new StandardUtilities.StringCompare<Block>(true));
		return Collections.unmodifiableList(Arrays.asList(blockArrayABC));
	}

	/**
	 * Returns the list of low defined Blocks from the Unicode Character
	 * Database in alphabetic order.
	 */
	public static List<Block> getLowBlocksABC()
	{
		Block[] blockArrayABC  = new Block[getLowBlocks().size()];
		blockArrayABC = getLowBlocks().toArray(blockArrayABC);
		Arrays.sort(blockArrayABC,new StandardUtilities.StringCompare<Block>(true));
		return Collections.unmodifiableList(Arrays.asList(blockArrayABC));
	}

	/**
	 *  Returns the Unicode block for a given block name, or
	 *  <code>null</code> if there is no block with this name available.
	 */
	public static Block getBlock(String blockName)
	{
		Iterator<Block> I = getBlocks().iterator();
		Block B;
		while (I.hasNext()) {
			B = I.next();
			if (B.toString().equals(blockName))
				return B;
		}
		return null;
	}

	/** Remember last search result in getBlock(codepoint) */
	private static Block getBlockPrevious = UnicodeBlocks.get(0);

	/**
	 *  Returns the Unicode block of a character.
	 *  If no valid character is given, <code>null</code> is returned.
	 *
	 *  @param codePoint The codepoint of the character
	 */
	public static Block getBlock(int codePoint)
	{
		if (!Character.isValidCodePoint(codePoint)) return null;

		if ( (getBlockPrevious.getFirstPoint() <= codePoint)
			&& (codePoint <= getBlockPrevious.getLastPoint()) ) {
			return getBlockPrevious;
		}
		Iterator<Block> I = getBlocks().iterator();
		Block B;
		while (I.hasNext()) {
			B = I.next();
			if ( (B.getFirstPoint() <= codePoint)
				&& (codePoint <= B.getLastPoint()) ) {
				getBlockPrevious = B;
				return B;
			}
		}
		return null;
	}

	/** Remember last search result in getCharacterName(codePoint). **/
	private static int getCharacterNamePrevious = 0;

	/**
	 * Returns the Unicode character name for the specified code
	 * point, or <code>null</code> if there is no name available.
	 */
	public static String getCharacterName(int codePoint)
	{
		if (!Character.isValidCodePoint(codePoint)) return null;

		String name = UnicodeCharNames.get(new Integer(codePoint));

		boolean nameIsFirst = (name != null && name.endsWith("First>"));
		boolean nameIsLast = (name != null && name.endsWith("Last>"));

		// Return chars with standard name
		if (name != null && !nameIsFirst && !nameIsLast)
			return name;

		// Return chars named "..First>" and "..Last>".
		if (nameIsFirst)
			return (name.substring(1, name.trim().length() - 8)
				+ "-" + Integer.toHexString(codePoint).toUpperCase());
		if (nameIsLast)
			return (name.substring(1, name.trim().length() - 7)
				+ "-" + Integer.toHexString(codePoint).toUpperCase());

		// Return empty chars not from first-last block
		Block B = getBlock(codePoint);
		if (B == null) return null;
		name = UnicodeCharNames.get(new Integer(B.getFirstPoint()));
		if ((name == null) || (!name.endsWith("First>"))) return null;

		// Return chars between first and last
		int getCharLast;
		if ((B.getFirstPoint() <= getCharacterNamePrevious)
			&& (getCharacterNamePrevious <= B.getLastPoint())) {
			getCharLast = getCharacterNamePrevious;
		}
		else {
			getCharLast = B.getLastPoint();
			String tempName;
			while (getCharLast > B.getFirstPoint()) {
				tempName = UnicodeCharNames.get(new Integer(getCharLast));
				if ((tempName != null) && tempName.endsWith("Last>"))
					break;
				getCharLast--;
			}
			getCharacterNamePrevious = getCharLast;
		}
		if (codePoint < getCharLast)
			return (name.substring(1, name.trim().length() - 8)
				+ "-" + Integer.toHexString(codePoint).toUpperCase());

		// Char is behind last
		return null;
	}

	/** Remember previous search result of isDefined(codePoint) */
	private static int isDefinedPrevious = 0;

	/**
	 *  Character assignment in the data list.
	 *  A character is unassigned, if it has an empty name.
	 *  @param codePoint Codepoint of the Character
	 *  @return          Character unassigned
	 */
	public static boolean isDefined(int codePoint)
	{
		// The function is equivalent to getCharacterName == null
		// but rewritten to be faster

		if (!Character.isValidCodePoint(codePoint)) return false;

		String name = UnicodeCharNames.get(new Integer(codePoint));

		// names != null => Character is defined
		if (name != null) return true;

		// Empty chars not from first-last block => undefined
		Block B = getBlock(codePoint);
		if (B == null) return false;
		name = UnicodeCharNames.get(new Integer(B.getFirstPoint()));
		if ((name == null) || (!name.endsWith("First>"))) return false;

		// Chars between first and last => defined
		int getCharLast;
		if ((B.getFirstPoint() <= isDefinedPrevious)
			&& (isDefinedPrevious <= B.getLastPoint())) {
			getCharLast = isDefinedPrevious;
		}
		else {
			getCharLast = B.getLastPoint();
			String tempName;
			while (getCharLast > B.getFirstPoint()) {
				tempName = UnicodeCharNames.get(new Integer(getCharLast));
				if ((tempName != null) && tempName.endsWith("Last>"))
					break;
				getCharLast--;
			}
			isDefinedPrevious = getCharLast;
		}
		if (codePoint < getCharLast)
			return true;

		// Char is behind last => undefined
		return false;
	}

//}}}
}


//{{{ Unicode Block Data
/**
 * Class containing the Unicode Blocks imported from the
 * Unicode Database and auxiliary functions
 */
final class UnicodeBlocks
{
	/*
	 * Below is a list of Unicode Blocks generated from the UCD file
	 * Blocks.txt using the parse_unicode_data.pl script. All blocks below
	 * a cutoff value defined in the script are included in the generated
	 * code.
	 *
	 * Do not edit these definitions by hand.
	 */


// BEGIN GENERATED CODE: Blocks.txt, cutoff=0x10FFFF
private static final List<UnicodeData.Block> blocks = Arrays.asList(new UnicodeData.Block[] {
	new UnicodeData.Block("Basic Latin", 0x0000, 0x007F),
	new UnicodeData.Block("Latin-1 Supplement", 0x0080, 0x00FF),
	new UnicodeData.Block("Latin Extended-A", 0x0100, 0x017F),
	new UnicodeData.Block("Latin Extended-B", 0x0180, 0x024F),
	new UnicodeData.Block("IPA Extensions", 0x0250, 0x02AF),
	new UnicodeData.Block("Spacing Modifier Letters", 0x02B0, 0x02FF),
	new UnicodeData.Block("Combining Diacritical Marks", 0x0300, 0x036F),
	new UnicodeData.Block("Greek and Coptic", 0x0370, 0x03FF),
	new UnicodeData.Block("Cyrillic", 0x0400, 0x04FF),
	new UnicodeData.Block("Cyrillic Supplement", 0x0500, 0x052F),
	new UnicodeData.Block("Armenian", 0x0530, 0x058F),
	new UnicodeData.Block("Hebrew", 0x0590, 0x05FF),
	new UnicodeData.Block("Arabic", 0x0600, 0x06FF),
	new UnicodeData.Block("Syriac", 0x0700, 0x074F),
	new UnicodeData.Block("Arabic Supplement", 0x0750, 0x077F),
	new UnicodeData.Block("Thaana", 0x0780, 0x07BF),
	new UnicodeData.Block("NKo", 0x07C0, 0x07FF),
	new UnicodeData.Block("Samaritan", 0x0800, 0x083F),
	new UnicodeData.Block("Mandaic", 0x0840, 0x085F),
	new UnicodeData.Block("Arabic Extended-A", 0x08A0, 0x08FF),
	new UnicodeData.Block("Devanagari", 0x0900, 0x097F),
	new UnicodeData.Block("Bengali", 0x0980, 0x09FF),
	new UnicodeData.Block("Gurmukhi", 0x0A00, 0x0A7F),
	new UnicodeData.Block("Gujarati", 0x0A80, 0x0AFF),
	new UnicodeData.Block("Oriya", 0x0B00, 0x0B7F),
	new UnicodeData.Block("Tamil", 0x0B80, 0x0BFF),
	new UnicodeData.Block("Telugu", 0x0C00, 0x0C7F),
	new UnicodeData.Block("Kannada", 0x0C80, 0x0CFF),
	new UnicodeData.Block("Malayalam", 0x0D00, 0x0D7F),
	new UnicodeData.Block("Sinhala", 0x0D80, 0x0DFF),
	new UnicodeData.Block("Thai", 0x0E00, 0x0E7F),
	new UnicodeData.Block("Lao", 0x0E80, 0x0EFF),
	new UnicodeData.Block("Tibetan", 0x0F00, 0x0FFF),
	new UnicodeData.Block("Myanmar", 0x1000, 0x109F),
	new UnicodeData.Block("Georgian", 0x10A0, 0x10FF),
	new UnicodeData.Block("Hangul Jamo", 0x1100, 0x11FF),
	new UnicodeData.Block("Ethiopic", 0x1200, 0x137F),
	new UnicodeData.Block("Ethiopic Supplement", 0x1380, 0x139F),
	new UnicodeData.Block("Cherokee", 0x13A0, 0x13FF),
	new UnicodeData.Block("Unified Canadian Aboriginal Syllabics", 0x1400, 0x167F),
	new UnicodeData.Block("Ogham", 0x1680, 0x169F),
	new UnicodeData.Block("Runic", 0x16A0, 0x16FF),
	new UnicodeData.Block("Tagalog", 0x1700, 0x171F),
	new UnicodeData.Block("Hanunoo", 0x1720, 0x173F),
	new UnicodeData.Block("Buhid", 0x1740, 0x175F),
	new UnicodeData.Block("Tagbanwa", 0x1760, 0x177F),
	new UnicodeData.Block("Khmer", 0x1780, 0x17FF),
	new UnicodeData.Block("Mongolian", 0x1800, 0x18AF),
	new UnicodeData.Block("Unified Canadian Aboriginal Syllabics Extended", 0x18B0, 0x18FF),
	new UnicodeData.Block("Limbu", 0x1900, 0x194F),
	new UnicodeData.Block("Tai Le", 0x1950, 0x197F),
	new UnicodeData.Block("New Tai Lue", 0x1980, 0x19DF),
	new UnicodeData.Block("Khmer Symbols", 0x19E0, 0x19FF),
	new UnicodeData.Block("Buginese", 0x1A00, 0x1A1F),
	new UnicodeData.Block("Tai Tham", 0x1A20, 0x1AAF),
	new UnicodeData.Block("Balinese", 0x1B00, 0x1B7F),
	new UnicodeData.Block("Sundanese", 0x1B80, 0x1BBF),
	new UnicodeData.Block("Batak", 0x1BC0, 0x1BFF),
	new UnicodeData.Block("Lepcha", 0x1C00, 0x1C4F),
	new UnicodeData.Block("Ol Chiki", 0x1C50, 0x1C7F),
	new UnicodeData.Block("Sundanese Supplement", 0x1CC0, 0x1CCF),
	new UnicodeData.Block("Vedic Extensions", 0x1CD0, 0x1CFF),
	new UnicodeData.Block("Phonetic Extensions", 0x1D00, 0x1D7F),
	new UnicodeData.Block("Phonetic Extensions Supplement", 0x1D80, 0x1DBF),
	new UnicodeData.Block("Combining Diacritical Marks Supplement", 0x1DC0, 0x1DFF),
	new UnicodeData.Block("Latin Extended Additional", 0x1E00, 0x1EFF),
	new UnicodeData.Block("Greek Extended", 0x1F00, 0x1FFF),
	new UnicodeData.Block("General Punctuation", 0x2000, 0x206F),
	new UnicodeData.Block("Superscripts and Subscripts", 0x2070, 0x209F),
	new UnicodeData.Block("Currency Symbols", 0x20A0, 0x20CF),
	new UnicodeData.Block("Combining Diacritical Marks for Symbols", 0x20D0, 0x20FF),
	new UnicodeData.Block("Letterlike Symbols", 0x2100, 0x214F),
	new UnicodeData.Block("Number Forms", 0x2150, 0x218F),
	new UnicodeData.Block("Arrows", 0x2190, 0x21FF),
	new UnicodeData.Block("Mathematical Operators", 0x2200, 0x22FF),
	new UnicodeData.Block("Miscellaneous Technical", 0x2300, 0x23FF),
	new UnicodeData.Block("Control Pictures", 0x2400, 0x243F),
	new UnicodeData.Block("Optical Character Recognition", 0x2440, 0x245F),
	new UnicodeData.Block("Enclosed Alphanumerics", 0x2460, 0x24FF),
	new UnicodeData.Block("Box Drawing", 0x2500, 0x257F),
	new UnicodeData.Block("Block Elements", 0x2580, 0x259F),
	new UnicodeData.Block("Geometric Shapes", 0x25A0, 0x25FF),
	new UnicodeData.Block("Miscellaneous Symbols", 0x2600, 0x26FF),
	new UnicodeData.Block("Dingbats", 0x2700, 0x27BF),
	new UnicodeData.Block("Miscellaneous Mathematical Symbols-A", 0x27C0, 0x27EF),
	new UnicodeData.Block("Supplemental Arrows-A", 0x27F0, 0x27FF),
	new UnicodeData.Block("Braille Patterns", 0x2800, 0x28FF),
	new UnicodeData.Block("Supplemental Arrows-B", 0x2900, 0x297F),
	new UnicodeData.Block("Miscellaneous Mathematical Symbols-B", 0x2980, 0x29FF),
	new UnicodeData.Block("Supplemental Mathematical Operators", 0x2A00, 0x2AFF),
	new UnicodeData.Block("Miscellaneous Symbols and Arrows", 0x2B00, 0x2BFF),
	new UnicodeData.Block("Glagolitic", 0x2C00, 0x2C5F),
	new UnicodeData.Block("Latin Extended-C", 0x2C60, 0x2C7F),
	new UnicodeData.Block("Coptic", 0x2C80, 0x2CFF),
	new UnicodeData.Block("Georgian Supplement", 0x2D00, 0x2D2F),
	new UnicodeData.Block("Tifinagh", 0x2D30, 0x2D7F),
	new UnicodeData.Block("Ethiopic Extended", 0x2D80, 0x2DDF),
	new UnicodeData.Block("Cyrillic Extended-A", 0x2DE0, 0x2DFF),
	new UnicodeData.Block("Supplemental Punctuation", 0x2E00, 0x2E7F),
	new UnicodeData.Block("CJK Radicals Supplement", 0x2E80, 0x2EFF),
	new UnicodeData.Block("Kangxi Radicals", 0x2F00, 0x2FDF),
	new UnicodeData.Block("Ideographic Description Characters", 0x2FF0, 0x2FFF),
	new UnicodeData.Block("CJK Symbols and Punctuation", 0x3000, 0x303F),
	new UnicodeData.Block("Hiragana", 0x3040, 0x309F),
	new UnicodeData.Block("Katakana", 0x30A0, 0x30FF),
	new UnicodeData.Block("Bopomofo", 0x3100, 0x312F),
	new UnicodeData.Block("Hangul Compatibility Jamo", 0x3130, 0x318F),
	new UnicodeData.Block("Kanbun", 0x3190, 0x319F),
	new UnicodeData.Block("Bopomofo Extended", 0x31A0, 0x31BF),
	new UnicodeData.Block("CJK Strokes", 0x31C0, 0x31EF),
	new UnicodeData.Block("Katakana Phonetic Extensions", 0x31F0, 0x31FF),
	new UnicodeData.Block("Enclosed CJK Letters and Months", 0x3200, 0x32FF),
	new UnicodeData.Block("CJK Compatibility", 0x3300, 0x33FF),
	new UnicodeData.Block("CJK Unified Ideographs Extension A", 0x3400, 0x4DBF),
	new UnicodeData.Block("Yijing Hexagram Symbols", 0x4DC0, 0x4DFF),
	new UnicodeData.Block("CJK Unified Ideographs", 0x4E00, 0x9FFF),
	new UnicodeData.Block("Yi Syllables", 0xA000, 0xA48F),
	new UnicodeData.Block("Yi Radicals", 0xA490, 0xA4CF),
	new UnicodeData.Block("Lisu", 0xA4D0, 0xA4FF),
	new UnicodeData.Block("Vai", 0xA500, 0xA63F),
	new UnicodeData.Block("Cyrillic Extended-B", 0xA640, 0xA69F),
	new UnicodeData.Block("Bamum", 0xA6A0, 0xA6FF),
	new UnicodeData.Block("Modifier Tone Letters", 0xA700, 0xA71F),
	new UnicodeData.Block("Latin Extended-D", 0xA720, 0xA7FF),
	new UnicodeData.Block("Syloti Nagri", 0xA800, 0xA82F),
	new UnicodeData.Block("Common Indic Number Forms", 0xA830, 0xA83F),
	new UnicodeData.Block("Phags-pa", 0xA840, 0xA87F),
	new UnicodeData.Block("Saurashtra", 0xA880, 0xA8DF),
	new UnicodeData.Block("Devanagari Extended", 0xA8E0, 0xA8FF),
	new UnicodeData.Block("Kayah Li", 0xA900, 0xA92F),
	new UnicodeData.Block("Rejang", 0xA930, 0xA95F),
	new UnicodeData.Block("Hangul Jamo Extended-A", 0xA960, 0xA97F),
	new UnicodeData.Block("Javanese", 0xA980, 0xA9DF),
	new UnicodeData.Block("Cham", 0xAA00, 0xAA5F),
	new UnicodeData.Block("Myanmar Extended-A", 0xAA60, 0xAA7F),
	new UnicodeData.Block("Tai Viet", 0xAA80, 0xAADF),
	new UnicodeData.Block("Meetei Mayek Extensions", 0xAAE0, 0xAAFF),
	new UnicodeData.Block("Ethiopic Extended-A", 0xAB00, 0xAB2F),
	new UnicodeData.Block("Meetei Mayek", 0xABC0, 0xABFF),
	new UnicodeData.Block("Hangul Syllables", 0xAC00, 0xD7AF),
	new UnicodeData.Block("Hangul Jamo Extended-B", 0xD7B0, 0xD7FF),
	new UnicodeData.Block("High Surrogates", 0xD800, 0xDB7F),
	new UnicodeData.Block("High Private Use Surrogates", 0xDB80, 0xDBFF),
	new UnicodeData.Block("Low Surrogates", 0xDC00, 0xDFFF),
	new UnicodeData.Block("Private Use Area", 0xE000, 0xF8FF),
	new UnicodeData.Block("CJK Compatibility Ideographs", 0xF900, 0xFAFF),
	new UnicodeData.Block("Alphabetic Presentation Forms", 0xFB00, 0xFB4F),
	new UnicodeData.Block("Arabic Presentation Forms-A", 0xFB50, 0xFDFF),
	new UnicodeData.Block("Variation Selectors", 0xFE00, 0xFE0F),
	new UnicodeData.Block("Vertical Forms", 0xFE10, 0xFE1F),
	new UnicodeData.Block("Combining Half Marks", 0xFE20, 0xFE2F),
	new UnicodeData.Block("CJK Compatibility Forms", 0xFE30, 0xFE4F),
	new UnicodeData.Block("Small Form Variants", 0xFE50, 0xFE6F),
	new UnicodeData.Block("Arabic Presentation Forms-B", 0xFE70, 0xFEFF),
	new UnicodeData.Block("Halfwidth and Fullwidth Forms", 0xFF00, 0xFFEF),
	new UnicodeData.Block("Specials", 0xFFF0, 0xFFFF),
	new UnicodeData.Block("Linear B Syllabary", 0x10000, 0x1007F),
	new UnicodeData.Block("Linear B Ideograms", 0x10080, 0x100FF),
	new UnicodeData.Block("Aegean Numbers", 0x10100, 0x1013F),
	new UnicodeData.Block("Ancient Greek Numbers", 0x10140, 0x1018F),
	new UnicodeData.Block("Ancient Symbols", 0x10190, 0x101CF),
	new UnicodeData.Block("Phaistos Disc", 0x101D0, 0x101FF),
	new UnicodeData.Block("Lycian", 0x10280, 0x1029F),
	new UnicodeData.Block("Carian", 0x102A0, 0x102DF),
	new UnicodeData.Block("Old Italic", 0x10300, 0x1032F),
	new UnicodeData.Block("Gothic", 0x10330, 0x1034F),
	new UnicodeData.Block("Ugaritic", 0x10380, 0x1039F),
	new UnicodeData.Block("Old Persian", 0x103A0, 0x103DF),
	new UnicodeData.Block("Deseret", 0x10400, 0x1044F),
	new UnicodeData.Block("Shavian", 0x10450, 0x1047F),
	new UnicodeData.Block("Osmanya", 0x10480, 0x104AF),
	new UnicodeData.Block("Cypriot Syllabary", 0x10800, 0x1083F),
	new UnicodeData.Block("Imperial Aramaic", 0x10840, 0x1085F),
	new UnicodeData.Block("Phoenician", 0x10900, 0x1091F),
	new UnicodeData.Block("Lydian", 0x10920, 0x1093F),
	new UnicodeData.Block("Meroitic Hieroglyphs", 0x10980, 0x1099F),
	new UnicodeData.Block("Meroitic Cursive", 0x109A0, 0x109FF),
	new UnicodeData.Block("Kharoshthi", 0x10A00, 0x10A5F),
	new UnicodeData.Block("Old South Arabian", 0x10A60, 0x10A7F),
	new UnicodeData.Block("Avestan", 0x10B00, 0x10B3F),
	new UnicodeData.Block("Inscriptional Parthian", 0x10B40, 0x10B5F),
	new UnicodeData.Block("Inscriptional Pahlavi", 0x10B60, 0x10B7F),
	new UnicodeData.Block("Old Turkic", 0x10C00, 0x10C4F),
	new UnicodeData.Block("Rumi Numeral Symbols", 0x10E60, 0x10E7F),
	new UnicodeData.Block("Brahmi", 0x11000, 0x1107F),
	new UnicodeData.Block("Kaithi", 0x11080, 0x110CF),
	new UnicodeData.Block("Sora Sompeng", 0x110D0, 0x110FF),
	new UnicodeData.Block("Chakma", 0x11100, 0x1114F),
	new UnicodeData.Block("Sharada", 0x11180, 0x111DF),
	new UnicodeData.Block("Takri", 0x11680, 0x116CF),
	new UnicodeData.Block("Cuneiform", 0x12000, 0x123FF),
	new UnicodeData.Block("Cuneiform Numbers and Punctuation", 0x12400, 0x1247F),
	new UnicodeData.Block("Egyptian Hieroglyphs", 0x13000, 0x1342F),
	new UnicodeData.Block("Bamum Supplement", 0x16800, 0x16A3F),
	new UnicodeData.Block("Miao", 0x16F00, 0x16F9F),
	new UnicodeData.Block("Kana Supplement", 0x1B000, 0x1B0FF),
	new UnicodeData.Block("Byzantine Musical Symbols", 0x1D000, 0x1D0FF),
	new UnicodeData.Block("Musical Symbols", 0x1D100, 0x1D1FF),
	new UnicodeData.Block("Ancient Greek Musical Notation", 0x1D200, 0x1D24F),
	new UnicodeData.Block("Tai Xuan Jing Symbols", 0x1D300, 0x1D35F),
	new UnicodeData.Block("Counting Rod Numerals", 0x1D360, 0x1D37F),
	new UnicodeData.Block("Mathematical Alphanumeric Symbols", 0x1D400, 0x1D7FF),
	new UnicodeData.Block("Arabic Mathematical Alphabetic Symbols", 0x1EE00, 0x1EEFF),
	new UnicodeData.Block("Mahjong Tiles", 0x1F000, 0x1F02F),
	new UnicodeData.Block("Domino Tiles", 0x1F030, 0x1F09F),
	new UnicodeData.Block("Playing Cards", 0x1F0A0, 0x1F0FF),
	new UnicodeData.Block("Enclosed Alphanumeric Supplement", 0x1F100, 0x1F1FF),
	new UnicodeData.Block("Enclosed Ideographic Supplement", 0x1F200, 0x1F2FF),
	new UnicodeData.Block("Miscellaneous Symbols And Pictographs", 0x1F300, 0x1F5FF),
	new UnicodeData.Block("Emoticons", 0x1F600, 0x1F64F),
	new UnicodeData.Block("Transport And Map Symbols", 0x1F680, 0x1F6FF),
	new UnicodeData.Block("Alchemical Symbols", 0x1F700, 0x1F77F),
	new UnicodeData.Block("CJK Unified Ideographs Extension B", 0x20000, 0x2A6DF),
	new UnicodeData.Block("CJK Unified Ideographs Extension C", 0x2A700, 0x2B73F),
	new UnicodeData.Block("CJK Unified Ideographs Extension D", 0x2B740, 0x2B81F),
	new UnicodeData.Block("CJK Compatibility Ideographs Supplement", 0x2F800, 0x2FA1F),
	new UnicodeData.Block("Tags", 0xE0000, 0xE007F),
	new UnicodeData.Block("Variation Selectors Supplement", 0xE0100, 0xE01EF),
	new UnicodeData.Block("Supplementary Private Use Area-A", 0xF0000, 0xFFFFF),
	new UnicodeData.Block("Supplementary Private Use Area-B", 0x100000, 0x10FFFF),
});
// END GENERATED CODE

	/**
	 * Empty default constructor. The class should be called only with its
	 * static methods.
	 */
	UnicodeBlocks()
	{
	}

	/**
	 Returns the Unicode Block List
	 */
	static List<UnicodeData.Block>  getBlocks()
	{
		return blocks;
	}

	/**
	 Returns an element from the List with the specified index
	 */
	static UnicodeData.Block get(int index)
	{
		return blocks.get(index);
	}
}
//}}}


//{{{ Unicode Name Data
/**
 * Class containing the Character Names imported from the
 * Unicode Database and auxiliary functions
 */
final class UnicodeCharNames
{
	/*
	 * Below is a list of Unicode character descriptions generated from the
	 * UCD file UnicodeData.txt using the parse_unicode_data.pl script. All
	 * code points below a cutoff value defined in the script are included
	 * in the generated code.
	 *
	 * Do not edit these definitions by hand.
	 */

// BEGIN GENERATED CODE: UnicodeData.txt, cutoff=0x10FFFF
private static final int actualSize = 24428;
private static final String[] characterNames = new String[1114110];

private static void loadCharacterNames0()
{
	characterNames[0x0000] = "<control> (NULL)";
	characterNames[0x0001] = "<control> (START OF HEADING)";
	characterNames[0x0002] = "<control> (START OF TEXT)";
	characterNames[0x0003] = "<control> (END OF TEXT)";
	characterNames[0x0004] = "<control> (END OF TRANSMISSION)";
	characterNames[0x0005] = "<control> (ENQUIRY)";
	characterNames[0x0006] = "<control> (ACKNOWLEDGE)";
	characterNames[0x0007] = "<control> (BELL)";
	characterNames[0x0008] = "<control> (BACKSPACE)";
	characterNames[0x0009] = "<control> (CHARACTER TABULATION)";
	characterNames[0x000A] = "<control> (LINE FEED (LF))";
	characterNames[0x000B] = "<control> (LINE TABULATION)";
	characterNames[0x000C] = "<control> (FORM FEED (FF))";
	characterNames[0x000D] = "<control> (CARRIAGE RETURN (CR))";
	characterNames[0x000E] = "<control> (SHIFT OUT)";
	characterNames[0x000F] = "<control> (SHIFT IN)";
	characterNames[0x0010] = "<control> (DATA LINK ESCAPE)";
	characterNames[0x0011] = "<control> (DEVICE CONTROL ONE)";
	characterNames[0x0012] = "<control> (DEVICE CONTROL TWO)";
	characterNames[0x0013] = "<control> (DEVICE CONTROL THREE)";
	characterNames[0x0014] = "<control> (DEVICE CONTROL FOUR)";
	characterNames[0x0015] = "<control> (NEGATIVE ACKNOWLEDGE)";
	characterNames[0x0016] = "<control> (SYNCHRONOUS IDLE)";
	characterNames[0x0017] = "<control> (END OF TRANSMISSION BLOCK)";
	characterNames[0x0018] = "<control> (CANCEL)";
	characterNames[0x0019] = "<control> (END OF MEDIUM)";
	characterNames[0x001A] = "<control> (SUBSTITUTE)";
	characterNames[0x001B] = "<control> (ESCAPE)";
	characterNames[0x001C] = "<control> (INFORMATION SEPARATOR FOUR)";
	characterNames[0x001D] = "<control> (INFORMATION SEPARATOR THREE)";
	characterNames[0x001E] = "<control> (INFORMATION SEPARATOR TWO)";
	characterNames[0x001F] = "<control> (INFORMATION SEPARATOR ONE)";
	characterNames[0x0020] = "SPACE";
	characterNames[0x0021] = "EXCLAMATION MARK";
	characterNames[0x0022] = "QUOTATION MARK";
	characterNames[0x0023] = "NUMBER SIGN";
	characterNames[0x0024] = "DOLLAR SIGN";
	characterNames[0x0025] = "PERCENT SIGN";
	characterNames[0x0026] = "AMPERSAND";
	characterNames[0x0027] = "APOSTROPHE";
	characterNames[0x0028] = "LEFT PARENTHESIS";
	characterNames[0x0029] = "RIGHT PARENTHESIS";
	characterNames[0x002A] = "ASTERISK";
	characterNames[0x002B] = "PLUS SIGN";
	characterNames[0x002C] = "COMMA";
	characterNames[0x002D] = "HYPHEN-MINUS";
	characterNames[0x002E] = "FULL STOP";
	characterNames[0x002F] = "SOLIDUS";
	characterNames[0x0030] = "DIGIT ZERO";
	characterNames[0x0031] = "DIGIT ONE";
	characterNames[0x0032] = "DIGIT TWO";
	characterNames[0x0033] = "DIGIT THREE";
	characterNames[0x0034] = "DIGIT FOUR";
	characterNames[0x0035] = "DIGIT FIVE";
	characterNames[0x0036] = "DIGIT SIX";
	characterNames[0x0037] = "DIGIT SEVEN";
	characterNames[0x0038] = "DIGIT EIGHT";
	characterNames[0x0039] = "DIGIT NINE";
	characterNames[0x003A] = "COLON";
	characterNames[0x003B] = "SEMICOLON";
	characterNames[0x003C] = "LESS-THAN SIGN";
	characterNames[0x003D] = "EQUALS SIGN";
	characterNames[0x003E] = "GREATER-THAN SIGN";
	characterNames[0x003F] = "QUESTION MARK";
	characterNames[0x0040] = "COMMERCIAL AT";
	characterNames[0x0041] = "LATIN CAPITAL LETTER A";
	characterNames[0x0042] = "LATIN CAPITAL LETTER B";
	characterNames[0x0043] = "LATIN CAPITAL LETTER C";
	characterNames[0x0044] = "LATIN CAPITAL LETTER D";
	characterNames[0x0045] = "LATIN CAPITAL LETTER E";
	characterNames[0x0046] = "LATIN CAPITAL LETTER F";
	characterNames[0x0047] = "LATIN CAPITAL LETTER G";
	characterNames[0x0048] = "LATIN CAPITAL LETTER H";
	characterNames[0x0049] = "LATIN CAPITAL LETTER I";
	characterNames[0x004A] = "LATIN CAPITAL LETTER J";
	characterNames[0x004B] = "LATIN CAPITAL LETTER K";
	characterNames[0x004C] = "LATIN CAPITAL LETTER L";
	characterNames[0x004D] = "LATIN CAPITAL LETTER M";
	characterNames[0x004E] = "LATIN CAPITAL LETTER N";
	characterNames[0x004F] = "LATIN CAPITAL LETTER O";
	characterNames[0x0050] = "LATIN CAPITAL LETTER P";
	characterNames[0x0051] = "LATIN CAPITAL LETTER Q";
	characterNames[0x0052] = "LATIN CAPITAL LETTER R";
	characterNames[0x0053] = "LATIN CAPITAL LETTER S";
	characterNames[0x0054] = "LATIN CAPITAL LETTER T";
	characterNames[0x0055] = "LATIN CAPITAL LETTER U";
	characterNames[0x0056] = "LATIN CAPITAL LETTER V";
	characterNames[0x0057] = "LATIN CAPITAL LETTER W";
	characterNames[0x0058] = "LATIN CAPITAL LETTER X";
	characterNames[0x0059] = "LATIN CAPITAL LETTER Y";
	characterNames[0x005A] = "LATIN CAPITAL LETTER Z";
	characterNames[0x005B] = "LEFT SQUARE BRACKET";
	characterNames[0x005C] = "REVERSE SOLIDUS";
	characterNames[0x005D] = "RIGHT SQUARE BRACKET";
	characterNames[0x005E] = "CIRCUMFLEX ACCENT";
	characterNames[0x005F] = "LOW LINE";
	characterNames[0x0060] = "GRAVE ACCENT";
	characterNames[0x0061] = "LATIN SMALL LETTER A";
	characterNames[0x0062] = "LATIN SMALL LETTER B";
	characterNames[0x0063] = "LATIN SMALL LETTER C";
	characterNames[0x0064] = "LATIN SMALL LETTER D";
	characterNames[0x0065] = "LATIN SMALL LETTER E";
	characterNames[0x0066] = "LATIN SMALL LETTER F";
	characterNames[0x0067] = "LATIN SMALL LETTER G";
	characterNames[0x0068] = "LATIN SMALL LETTER H";
	characterNames[0x0069] = "LATIN SMALL LETTER I";
	characterNames[0x006A] = "LATIN SMALL LETTER J";
	characterNames[0x006B] = "LATIN SMALL LETTER K";
	characterNames[0x006C] = "LATIN SMALL LETTER L";
	characterNames[0x006D] = "LATIN SMALL LETTER M";
	characterNames[0x006E] = "LATIN SMALL LETTER N";
	characterNames[0x006F] = "LATIN SMALL LETTER O";
	characterNames[0x0070] = "LATIN SMALL LETTER P";
	characterNames[0x0071] = "LATIN SMALL LETTER Q";
	characterNames[0x0072] = "LATIN SMALL LETTER R";
	characterNames[0x0073] = "LATIN SMALL LETTER S";
	characterNames[0x0074] = "LATIN SMALL LETTER T";
	characterNames[0x0075] = "LATIN SMALL LETTER U";
	characterNames[0x0076] = "LATIN SMALL LETTER V";
	characterNames[0x0077] = "LATIN SMALL LETTER W";
	characterNames[0x0078] = "LATIN SMALL LETTER X";
	characterNames[0x0079] = "LATIN SMALL LETTER Y";
	characterNames[0x007A] = "LATIN SMALL LETTER Z";
	characterNames[0x007B] = "LEFT CURLY BRACKET";
	characterNames[0x007C] = "VERTICAL LINE";
	characterNames[0x007D] = "RIGHT CURLY BRACKET";
	characterNames[0x007E] = "TILDE";
	characterNames[0x007F] = "<control> (DELETE)";
	characterNames[0x0080] = "<control>";
	characterNames[0x0081] = "<control>";
	characterNames[0x0082] = "<control> (BREAK PERMITTED HERE)";
	characterNames[0x0083] = "<control> (NO BREAK HERE)";
	characterNames[0x0084] = "<control>";
	characterNames[0x0085] = "<control> (NEXT LINE (NEL))";
	characterNames[0x0086] = "<control> (START OF SELECTED AREA)";
	characterNames[0x0087] = "<control> (END OF SELECTED AREA)";
	characterNames[0x0088] = "<control> (CHARACTER TABULATION SET)";
	characterNames[0x0089] = "<control> (CHARACTER TABULATION WITH JUSTIFICATION)";
	characterNames[0x008A] = "<control> (LINE TABULATION SET)";
	characterNames[0x008B] = "<control> (PARTIAL LINE FORWARD)";
	characterNames[0x008C] = "<control> (PARTIAL LINE BACKWARD)";
	characterNames[0x008D] = "<control> (REVERSE LINE FEED)";
	characterNames[0x008E] = "<control> (SINGLE SHIFT TWO)";
	characterNames[0x008F] = "<control> (SINGLE SHIFT THREE)";
	characterNames[0x0090] = "<control> (DEVICE CONTROL STRING)";
	characterNames[0x0091] = "<control> (PRIVATE USE ONE)";
	characterNames[0x0092] = "<control> (PRIVATE USE TWO)";
	characterNames[0x0093] = "<control> (SET TRANSMIT STATE)";
	characterNames[0x0094] = "<control> (CANCEL CHARACTER)";
	characterNames[0x0095] = "<control> (MESSAGE WAITING)";
	characterNames[0x0096] = "<control> (START OF GUARDED AREA)";
	characterNames[0x0097] = "<control> (END OF GUARDED AREA)";
	characterNames[0x0098] = "<control> (START OF STRING)";
	characterNames[0x0099] = "<control>";
	characterNames[0x009A] = "<control> (SINGLE CHARACTER INTRODUCER)";
	characterNames[0x009B] = "<control> (CONTROL SEQUENCE INTRODUCER)";
	characterNames[0x009C] = "<control> (STRING TERMINATOR)";
	characterNames[0x009D] = "<control> (OPERATING SYSTEM COMMAND)";
	characterNames[0x009E] = "<control> (PRIVACY MESSAGE)";
	characterNames[0x009F] = "<control> (APPLICATION PROGRAM COMMAND)";
	characterNames[0x00A0] = "NO-BREAK SPACE";
	characterNames[0x00A1] = "INVERTED EXCLAMATION MARK";
	characterNames[0x00A2] = "CENT SIGN";
	characterNames[0x00A3] = "POUND SIGN";
	characterNames[0x00A4] = "CURRENCY SIGN";
	characterNames[0x00A5] = "YEN SIGN";
	characterNames[0x00A6] = "BROKEN BAR";
	characterNames[0x00A7] = "SECTION SIGN";
	characterNames[0x00A8] = "DIAERESIS";
	characterNames[0x00A9] = "COPYRIGHT SIGN";
	characterNames[0x00AA] = "FEMININE ORDINAL INDICATOR";
	characterNames[0x00AB] = "LEFT-POINTING DOUBLE ANGLE QUOTATION MARK";
	characterNames[0x00AC] = "NOT SIGN";
	characterNames[0x00AD] = "SOFT HYPHEN";
	characterNames[0x00AE] = "REGISTERED SIGN";
	characterNames[0x00AF] = "MACRON";
	characterNames[0x00B0] = "DEGREE SIGN";
	characterNames[0x00B1] = "PLUS-MINUS SIGN";
	characterNames[0x00B2] = "SUPERSCRIPT TWO";
	characterNames[0x00B3] = "SUPERSCRIPT THREE";
	characterNames[0x00B4] = "ACUTE ACCENT";
	characterNames[0x00B5] = "MICRO SIGN";
	characterNames[0x00B6] = "PILCROW SIGN";
	characterNames[0x00B7] = "MIDDLE DOT";
	characterNames[0x00B8] = "CEDILLA";
	characterNames[0x00B9] = "SUPERSCRIPT ONE";
	characterNames[0x00BA] = "MASCULINE ORDINAL INDICATOR";
	characterNames[0x00BB] = "RIGHT-POINTING DOUBLE ANGLE QUOTATION MARK";
	characterNames[0x00BC] = "VULGAR FRACTION ONE QUARTER";
	characterNames[0x00BD] = "VULGAR FRACTION ONE HALF";
	characterNames[0x00BE] = "VULGAR FRACTION THREE QUARTERS";
	characterNames[0x00BF] = "INVERTED QUESTION MARK";
	characterNames[0x00C0] = "LATIN CAPITAL LETTER A WITH GRAVE";
	characterNames[0x00C1] = "LATIN CAPITAL LETTER A WITH ACUTE";
	characterNames[0x00C2] = "LATIN CAPITAL LETTER A WITH CIRCUMFLEX";
	characterNames[0x00C3] = "LATIN CAPITAL LETTER A WITH TILDE";
	characterNames[0x00C4] = "LATIN CAPITAL LETTER A WITH DIAERESIS";
	characterNames[0x00C5] = "LATIN CAPITAL LETTER A WITH RING ABOVE";
	characterNames[0x00C6] = "LATIN CAPITAL LETTER AE";
	characterNames[0x00C7] = "LATIN CAPITAL LETTER C WITH CEDILLA";
	characterNames[0x00C8] = "LATIN CAPITAL LETTER E WITH GRAVE";
	characterNames[0x00C9] = "LATIN CAPITAL LETTER E WITH ACUTE";
	characterNames[0x00CA] = "LATIN CAPITAL LETTER E WITH CIRCUMFLEX";
	characterNames[0x00CB] = "LATIN CAPITAL LETTER E WITH DIAERESIS";
	characterNames[0x00CC] = "LATIN CAPITAL LETTER I WITH GRAVE";
	characterNames[0x00CD] = "LATIN CAPITAL LETTER I WITH ACUTE";
	characterNames[0x00CE] = "LATIN CAPITAL LETTER I WITH CIRCUMFLEX";
	characterNames[0x00CF] = "LATIN CAPITAL LETTER I WITH DIAERESIS";
	characterNames[0x00D0] = "LATIN CAPITAL LETTER ETH";
	characterNames[0x00D1] = "LATIN CAPITAL LETTER N WITH TILDE";
	characterNames[0x00D2] = "LATIN CAPITAL LETTER O WITH GRAVE";
	characterNames[0x00D3] = "LATIN CAPITAL LETTER O WITH ACUTE";
	characterNames[0x00D4] = "LATIN CAPITAL LETTER O WITH CIRCUMFLEX";
	characterNames[0x00D5] = "LATIN CAPITAL LETTER O WITH TILDE";
	characterNames[0x00D6] = "LATIN CAPITAL LETTER O WITH DIAERESIS";
	characterNames[0x00D7] = "MULTIPLICATION SIGN";
	characterNames[0x00D8] = "LATIN CAPITAL LETTER O WITH STROKE";
	characterNames[0x00D9] = "LATIN CAPITAL LETTER U WITH GRAVE";
	characterNames[0x00DA] = "LATIN CAPITAL LETTER U WITH ACUTE";
	characterNames[0x00DB] = "LATIN CAPITAL LETTER U WITH CIRCUMFLEX";
	characterNames[0x00DC] = "LATIN CAPITAL LETTER U WITH DIAERESIS";
	characterNames[0x00DD] = "LATIN CAPITAL LETTER Y WITH ACUTE";
	characterNames[0x00DE] = "LATIN CAPITAL LETTER THORN";
	characterNames[0x00DF] = "LATIN SMALL LETTER SHARP S";
	characterNames[0x00E0] = "LATIN SMALL LETTER A WITH GRAVE";
	characterNames[0x00E1] = "LATIN SMALL LETTER A WITH ACUTE";
	characterNames[0x00E2] = "LATIN SMALL LETTER A WITH CIRCUMFLEX";
	characterNames[0x00E3] = "LATIN SMALL LETTER A WITH TILDE";
	characterNames[0x00E4] = "LATIN SMALL LETTER A WITH DIAERESIS";
	characterNames[0x00E5] = "LATIN SMALL LETTER A WITH RING ABOVE";
	characterNames[0x00E6] = "LATIN SMALL LETTER AE";
	characterNames[0x00E7] = "LATIN SMALL LETTER C WITH CEDILLA";
	characterNames[0x00E8] = "LATIN SMALL LETTER E WITH GRAVE";
	characterNames[0x00E9] = "LATIN SMALL LETTER E WITH ACUTE";
	characterNames[0x00EA] = "LATIN SMALL LETTER E WITH CIRCUMFLEX";
	characterNames[0x00EB] = "LATIN SMALL LETTER E WITH DIAERESIS";
	characterNames[0x00EC] = "LATIN SMALL LETTER I WITH GRAVE";
	characterNames[0x00ED] = "LATIN SMALL LETTER I WITH ACUTE";
	characterNames[0x00EE] = "LATIN SMALL LETTER I WITH CIRCUMFLEX";
	characterNames[0x00EF] = "LATIN SMALL LETTER I WITH DIAERESIS";
	characterNames[0x00F0] = "LATIN SMALL LETTER ETH";
	characterNames[0x00F1] = "LATIN SMALL LETTER N WITH TILDE";
	characterNames[0x00F2] = "LATIN SMALL LETTER O WITH GRAVE";
	characterNames[0x00F3] = "LATIN SMALL LETTER O WITH ACUTE";
	characterNames[0x00F4] = "LATIN SMALL LETTER O WITH CIRCUMFLEX";
	characterNames[0x00F5] = "LATIN SMALL LETTER O WITH TILDE";
	characterNames[0x00F6] = "LATIN SMALL LETTER O WITH DIAERESIS";
	characterNames[0x00F7] = "DIVISION SIGN";
	characterNames[0x00F8] = "LATIN SMALL LETTER O WITH STROKE";
	characterNames[0x00F9] = "LATIN SMALL LETTER U WITH GRAVE";
	characterNames[0x00FA] = "LATIN SMALL LETTER U WITH ACUTE";
	characterNames[0x00FB] = "LATIN SMALL LETTER U WITH CIRCUMFLEX";
	characterNames[0x00FC] = "LATIN SMALL LETTER U WITH DIAERESIS";
	characterNames[0x00FD] = "LATIN SMALL LETTER Y WITH ACUTE";
	characterNames[0x00FE] = "LATIN SMALL LETTER THORN";
	characterNames[0x00FF] = "LATIN SMALL LETTER Y WITH DIAERESIS";
	characterNames[0x0100] = "LATIN CAPITAL LETTER A WITH MACRON";
	characterNames[0x0101] = "LATIN SMALL LETTER A WITH MACRON";
	characterNames[0x0102] = "LATIN CAPITAL LETTER A WITH BREVE";
	characterNames[0x0103] = "LATIN SMALL LETTER A WITH BREVE";
	characterNames[0x0104] = "LATIN CAPITAL LETTER A WITH OGONEK";
	characterNames[0x0105] = "LATIN SMALL LETTER A WITH OGONEK";
	characterNames[0x0106] = "LATIN CAPITAL LETTER C WITH ACUTE";
	characterNames[0x0107] = "LATIN SMALL LETTER C WITH ACUTE";
	characterNames[0x0108] = "LATIN CAPITAL LETTER C WITH CIRCUMFLEX";
	characterNames[0x0109] = "LATIN SMALL LETTER C WITH CIRCUMFLEX";
	characterNames[0x010A] = "LATIN CAPITAL LETTER C WITH DOT ABOVE";
	characterNames[0x010B] = "LATIN SMALL LETTER C WITH DOT ABOVE";
	characterNames[0x010C] = "LATIN CAPITAL LETTER C WITH CARON";
	characterNames[0x010D] = "LATIN SMALL LETTER C WITH CARON";
	characterNames[0x010E] = "LATIN CAPITAL LETTER D WITH CARON";
	characterNames[0x010F] = "LATIN SMALL LETTER D WITH CARON";
	characterNames[0x0110] = "LATIN CAPITAL LETTER D WITH STROKE";
	characterNames[0x0111] = "LATIN SMALL LETTER D WITH STROKE";
	characterNames[0x0112] = "LATIN CAPITAL LETTER E WITH MACRON";
	characterNames[0x0113] = "LATIN SMALL LETTER E WITH MACRON";
	characterNames[0x0114] = "LATIN CAPITAL LETTER E WITH BREVE";
	characterNames[0x0115] = "LATIN SMALL LETTER E WITH BREVE";
	characterNames[0x0116] = "LATIN CAPITAL LETTER E WITH DOT ABOVE";
	characterNames[0x0117] = "LATIN SMALL LETTER E WITH DOT ABOVE";
	characterNames[0x0118] = "LATIN CAPITAL LETTER E WITH OGONEK";
	characterNames[0x0119] = "LATIN SMALL LETTER E WITH OGONEK";
	characterNames[0x011A] = "LATIN CAPITAL LETTER E WITH CARON";
	characterNames[0x011B] = "LATIN SMALL LETTER E WITH CARON";
	characterNames[0x011C] = "LATIN CAPITAL LETTER G WITH CIRCUMFLEX";
	characterNames[0x011D] = "LATIN SMALL LETTER G WITH CIRCUMFLEX";
	characterNames[0x011E] = "LATIN CAPITAL LETTER G WITH BREVE";
	characterNames[0x011F] = "LATIN SMALL LETTER G WITH BREVE";
	characterNames[0x0120] = "LATIN CAPITAL LETTER G WITH DOT ABOVE";
	characterNames[0x0121] = "LATIN SMALL LETTER G WITH DOT ABOVE";
	characterNames[0x0122] = "LATIN CAPITAL LETTER G WITH CEDILLA";
	characterNames[0x0123] = "LATIN SMALL LETTER G WITH CEDILLA";
	characterNames[0x0124] = "LATIN CAPITAL LETTER H WITH CIRCUMFLEX";
	characterNames[0x0125] = "LATIN SMALL LETTER H WITH CIRCUMFLEX";
	characterNames[0x0126] = "LATIN CAPITAL LETTER H WITH STROKE";
	characterNames[0x0127] = "LATIN SMALL LETTER H WITH STROKE";
	characterNames[0x0128] = "LATIN CAPITAL LETTER I WITH TILDE";
	characterNames[0x0129] = "LATIN SMALL LETTER I WITH TILDE";
	characterNames[0x012A] = "LATIN CAPITAL LETTER I WITH MACRON";
	characterNames[0x012B] = "LATIN SMALL LETTER I WITH MACRON";
	characterNames[0x012C] = "LATIN CAPITAL LETTER I WITH BREVE";
	characterNames[0x012D] = "LATIN SMALL LETTER I WITH BREVE";
	characterNames[0x012E] = "LATIN CAPITAL LETTER I WITH OGONEK";
	characterNames[0x012F] = "LATIN SMALL LETTER I WITH OGONEK";
	characterNames[0x0130] = "LATIN CAPITAL LETTER I WITH DOT ABOVE";
	characterNames[0x0131] = "LATIN SMALL LETTER DOTLESS I";
	characterNames[0x0132] = "LATIN CAPITAL LIGATURE IJ";
	characterNames[0x0133] = "LATIN SMALL LIGATURE IJ";
	characterNames[0x0134] = "LATIN CAPITAL LETTER J WITH CIRCUMFLEX";
	characterNames[0x0135] = "LATIN SMALL LETTER J WITH CIRCUMFLEX";
	characterNames[0x0136] = "LATIN CAPITAL LETTER K WITH CEDILLA";
	characterNames[0x0137] = "LATIN SMALL LETTER K WITH CEDILLA";
	characterNames[0x0138] = "LATIN SMALL LETTER KRA";
	characterNames[0x0139] = "LATIN CAPITAL LETTER L WITH ACUTE";
	characterNames[0x013A] = "LATIN SMALL LETTER L WITH ACUTE";
	characterNames[0x013B] = "LATIN CAPITAL LETTER L WITH CEDILLA";
	characterNames[0x013C] = "LATIN SMALL LETTER L WITH CEDILLA";
	characterNames[0x013D] = "LATIN CAPITAL LETTER L WITH CARON";
	characterNames[0x013E] = "LATIN SMALL LETTER L WITH CARON";
	characterNames[0x013F] = "LATIN CAPITAL LETTER L WITH MIDDLE DOT";
	characterNames[0x0140] = "LATIN SMALL LETTER L WITH MIDDLE DOT";
	characterNames[0x0141] = "LATIN CAPITAL LETTER L WITH STROKE";
	characterNames[0x0142] = "LATIN SMALL LETTER L WITH STROKE";
	characterNames[0x0143] = "LATIN CAPITAL LETTER N WITH ACUTE";
	characterNames[0x0144] = "LATIN SMALL LETTER N WITH ACUTE";
	characterNames[0x0145] = "LATIN CAPITAL LETTER N WITH CEDILLA";
	characterNames[0x0146] = "LATIN SMALL LETTER N WITH CEDILLA";
	characterNames[0x0147] = "LATIN CAPITAL LETTER N WITH CARON";
	characterNames[0x0148] = "LATIN SMALL LETTER N WITH CARON";
	characterNames[0x0149] = "LATIN SMALL LETTER N PRECEDED BY APOSTROPHE";
	characterNames[0x014A] = "LATIN CAPITAL LETTER ENG";
	characterNames[0x014B] = "LATIN SMALL LETTER ENG";
	characterNames[0x014C] = "LATIN CAPITAL LETTER O WITH MACRON";
	characterNames[0x014D] = "LATIN SMALL LETTER O WITH MACRON";
	characterNames[0x014E] = "LATIN CAPITAL LETTER O WITH BREVE";
	characterNames[0x014F] = "LATIN SMALL LETTER O WITH BREVE";
	characterNames[0x0150] = "LATIN CAPITAL LETTER O WITH DOUBLE ACUTE";
	characterNames[0x0151] = "LATIN SMALL LETTER O WITH DOUBLE ACUTE";
	characterNames[0x0152] = "LATIN CAPITAL LIGATURE OE";
	characterNames[0x0153] = "LATIN SMALL LIGATURE OE";
	characterNames[0x0154] = "LATIN CAPITAL LETTER R WITH ACUTE";
	characterNames[0x0155] = "LATIN SMALL LETTER R WITH ACUTE";
	characterNames[0x0156] = "LATIN CAPITAL LETTER R WITH CEDILLA";
	characterNames[0x0157] = "LATIN SMALL LETTER R WITH CEDILLA";
	characterNames[0x0158] = "LATIN CAPITAL LETTER R WITH CARON";
	characterNames[0x0159] = "LATIN SMALL LETTER R WITH CARON";
	characterNames[0x015A] = "LATIN CAPITAL LETTER S WITH ACUTE";
	characterNames[0x015B] = "LATIN SMALL LETTER S WITH ACUTE";
	characterNames[0x015C] = "LATIN CAPITAL LETTER S WITH CIRCUMFLEX";
	characterNames[0x015D] = "LATIN SMALL LETTER S WITH CIRCUMFLEX";
	characterNames[0x015E] = "LATIN CAPITAL LETTER S WITH CEDILLA";
	characterNames[0x015F] = "LATIN SMALL LETTER S WITH CEDILLA";
	characterNames[0x0160] = "LATIN CAPITAL LETTER S WITH CARON";
	characterNames[0x0161] = "LATIN SMALL LETTER S WITH CARON";
	characterNames[0x0162] = "LATIN CAPITAL LETTER T WITH CEDILLA";
	characterNames[0x0163] = "LATIN SMALL LETTER T WITH CEDILLA";
	characterNames[0x0164] = "LATIN CAPITAL LETTER T WITH CARON";
	characterNames[0x0165] = "LATIN SMALL LETTER T WITH CARON";
	characterNames[0x0166] = "LATIN CAPITAL LETTER T WITH STROKE";
	characterNames[0x0167] = "LATIN SMALL LETTER T WITH STROKE";
	characterNames[0x0168] = "LATIN CAPITAL LETTER U WITH TILDE";
	characterNames[0x0169] = "LATIN SMALL LETTER U WITH TILDE";
	characterNames[0x016A] = "LATIN CAPITAL LETTER U WITH MACRON";
	characterNames[0x016B] = "LATIN SMALL LETTER U WITH MACRON";
	characterNames[0x016C] = "LATIN CAPITAL LETTER U WITH BREVE";
	characterNames[0x016D] = "LATIN SMALL LETTER U WITH BREVE";
	characterNames[0x016E] = "LATIN CAPITAL LETTER U WITH RING ABOVE";
	characterNames[0x016F] = "LATIN SMALL LETTER U WITH RING ABOVE";
	characterNames[0x0170] = "LATIN CAPITAL LETTER U WITH DOUBLE ACUTE";
	characterNames[0x0171] = "LATIN SMALL LETTER U WITH DOUBLE ACUTE";
	characterNames[0x0172] = "LATIN CAPITAL LETTER U WITH OGONEK";
	characterNames[0x0173] = "LATIN SMALL LETTER U WITH OGONEK";
	characterNames[0x0174] = "LATIN CAPITAL LETTER W WITH CIRCUMFLEX";
	characterNames[0x0175] = "LATIN SMALL LETTER W WITH CIRCUMFLEX";
	characterNames[0x0176] = "LATIN CAPITAL LETTER Y WITH CIRCUMFLEX";
	characterNames[0x0177] = "LATIN SMALL LETTER Y WITH CIRCUMFLEX";
	characterNames[0x0178] = "LATIN CAPITAL LETTER Y WITH DIAERESIS";
	characterNames[0x0179] = "LATIN CAPITAL LETTER Z WITH ACUTE";
	characterNames[0x017A] = "LATIN SMALL LETTER Z WITH ACUTE";
	characterNames[0x017B] = "LATIN CAPITAL LETTER Z WITH DOT ABOVE";
	characterNames[0x017C] = "LATIN SMALL LETTER Z WITH DOT ABOVE";
	characterNames[0x017D] = "LATIN CAPITAL LETTER Z WITH CARON";
	characterNames[0x017E] = "LATIN SMALL LETTER Z WITH CARON";
	characterNames[0x017F] = "LATIN SMALL LETTER LONG S";
	characterNames[0x0180] = "LATIN SMALL LETTER B WITH STROKE";
	characterNames[0x0181] = "LATIN CAPITAL LETTER B WITH HOOK";
	characterNames[0x0182] = "LATIN CAPITAL LETTER B WITH TOPBAR";
	characterNames[0x0183] = "LATIN SMALL LETTER B WITH TOPBAR";
	characterNames[0x0184] = "LATIN CAPITAL LETTER TONE SIX";
	characterNames[0x0185] = "LATIN SMALL LETTER TONE SIX";
	characterNames[0x0186] = "LATIN CAPITAL LETTER OPEN O";
	characterNames[0x0187] = "LATIN CAPITAL LETTER C WITH HOOK";
	characterNames[0x0188] = "LATIN SMALL LETTER C WITH HOOK";
	characterNames[0x0189] = "LATIN CAPITAL LETTER AFRICAN D";
	characterNames[0x018A] = "LATIN CAPITAL LETTER D WITH HOOK";
	characterNames[0x018B] = "LATIN CAPITAL LETTER D WITH TOPBAR";
	characterNames[0x018C] = "LATIN SMALL LETTER D WITH TOPBAR";
	characterNames[0x018D] = "LATIN SMALL LETTER TURNED DELTA";
	characterNames[0x018E] = "LATIN CAPITAL LETTER REVERSED E";
	characterNames[0x018F] = "LATIN CAPITAL LETTER SCHWA";
	characterNames[0x0190] = "LATIN CAPITAL LETTER OPEN E";
	characterNames[0x0191] = "LATIN CAPITAL LETTER F WITH HOOK";
	characterNames[0x0192] = "LATIN SMALL LETTER F WITH HOOK";
	characterNames[0x0193] = "LATIN CAPITAL LETTER G WITH HOOK";
	characterNames[0x0194] = "LATIN CAPITAL LETTER GAMMA";
	characterNames[0x0195] = "LATIN SMALL LETTER HV";
	characterNames[0x0196] = "LATIN CAPITAL LETTER IOTA";
	characterNames[0x0197] = "LATIN CAPITAL LETTER I WITH STROKE";
	characterNames[0x0198] = "LATIN CAPITAL LETTER K WITH HOOK";
	characterNames[0x0199] = "LATIN SMALL LETTER K WITH HOOK";
	characterNames[0x019A] = "LATIN SMALL LETTER L WITH BAR";
	characterNames[0x019B] = "LATIN SMALL LETTER LAMBDA WITH STROKE";
	characterNames[0x019C] = "LATIN CAPITAL LETTER TURNED M";
	characterNames[0x019D] = "LATIN CAPITAL LETTER N WITH LEFT HOOK";
	characterNames[0x019E] = "LATIN SMALL LETTER N WITH LONG RIGHT LEG";
	characterNames[0x019F] = "LATIN CAPITAL LETTER O WITH MIDDLE TILDE";
	characterNames[0x01A0] = "LATIN CAPITAL LETTER O WITH HORN";
	characterNames[0x01A1] = "LATIN SMALL LETTER O WITH HORN";
	characterNames[0x01A2] = "LATIN CAPITAL LETTER OI";
	characterNames[0x01A3] = "LATIN SMALL LETTER OI";
	characterNames[0x01A4] = "LATIN CAPITAL LETTER P WITH HOOK";
	characterNames[0x01A5] = "LATIN SMALL LETTER P WITH HOOK";
	characterNames[0x01A6] = "LATIN LETTER YR";
	characterNames[0x01A7] = "LATIN CAPITAL LETTER TONE TWO";
	characterNames[0x01A8] = "LATIN SMALL LETTER TONE TWO";
	characterNames[0x01A9] = "LATIN CAPITAL LETTER ESH";
	characterNames[0x01AA] = "LATIN LETTER REVERSED ESH LOOP";
	characterNames[0x01AB] = "LATIN SMALL LETTER T WITH PALATAL HOOK";
	characterNames[0x01AC] = "LATIN CAPITAL LETTER T WITH HOOK";
	characterNames[0x01AD] = "LATIN SMALL LETTER T WITH HOOK";
	characterNames[0x01AE] = "LATIN CAPITAL LETTER T WITH RETROFLEX HOOK";
	characterNames[0x01AF] = "LATIN CAPITAL LETTER U WITH HORN";
	characterNames[0x01B0] = "LATIN SMALL LETTER U WITH HORN";
	characterNames[0x01B1] = "LATIN CAPITAL LETTER UPSILON";
	characterNames[0x01B2] = "LATIN CAPITAL LETTER V WITH HOOK";
	characterNames[0x01B3] = "LATIN CAPITAL LETTER Y WITH HOOK";
	characterNames[0x01B4] = "LATIN SMALL LETTER Y WITH HOOK";
	characterNames[0x01B5] = "LATIN CAPITAL LETTER Z WITH STROKE";
	characterNames[0x01B6] = "LATIN SMALL LETTER Z WITH STROKE";
	characterNames[0x01B7] = "LATIN CAPITAL LETTER EZH";
	characterNames[0x01B8] = "LATIN CAPITAL LETTER EZH REVERSED";
	characterNames[0x01B9] = "LATIN SMALL LETTER EZH REVERSED";
	characterNames[0x01BA] = "LATIN SMALL LETTER EZH WITH TAIL";
	characterNames[0x01BB] = "LATIN LETTER TWO WITH STROKE";
	characterNames[0x01BC] = "LATIN CAPITAL LETTER TONE FIVE";
	characterNames[0x01BD] = "LATIN SMALL LETTER TONE FIVE";
	characterNames[0x01BE] = "LATIN LETTER INVERTED GLOTTAL STOP WITH STROKE";
	characterNames[0x01BF] = "LATIN LETTER WYNN";
	characterNames[0x01C0] = "LATIN LETTER DENTAL CLICK";
	characterNames[0x01C1] = "LATIN LETTER LATERAL CLICK";
	characterNames[0x01C2] = "LATIN LETTER ALVEOLAR CLICK";
	characterNames[0x01C3] = "LATIN LETTER RETROFLEX CLICK";
	characterNames[0x01C4] = "LATIN CAPITAL LETTER DZ WITH CARON";
	characterNames[0x01C5] = "LATIN CAPITAL LETTER D WITH SMALL LETTER Z WITH CARON";
	characterNames[0x01C6] = "LATIN SMALL LETTER DZ WITH CARON";
	characterNames[0x01C7] = "LATIN CAPITAL LETTER LJ";
	characterNames[0x01C8] = "LATIN CAPITAL LETTER L WITH SMALL LETTER J";
	characterNames[0x01C9] = "LATIN SMALL LETTER LJ";
	characterNames[0x01CA] = "LATIN CAPITAL LETTER NJ";
	characterNames[0x01CB] = "LATIN CAPITAL LETTER N WITH SMALL LETTER J";
	characterNames[0x01CC] = "LATIN SMALL LETTER NJ";
	characterNames[0x01CD] = "LATIN CAPITAL LETTER A WITH CARON";
	characterNames[0x01CE] = "LATIN SMALL LETTER A WITH CARON";
	characterNames[0x01CF] = "LATIN CAPITAL LETTER I WITH CARON";
	characterNames[0x01D0] = "LATIN SMALL LETTER I WITH CARON";
	characterNames[0x01D1] = "LATIN CAPITAL LETTER O WITH CARON";
	characterNames[0x01D2] = "LATIN SMALL LETTER O WITH CARON";
	characterNames[0x01D3] = "LATIN CAPITAL LETTER U WITH CARON";
	characterNames[0x01D4] = "LATIN SMALL LETTER U WITH CARON";
	characterNames[0x01D5] = "LATIN CAPITAL LETTER U WITH DIAERESIS AND MACRON";
	characterNames[0x01D6] = "LATIN SMALL LETTER U WITH DIAERESIS AND MACRON";
	characterNames[0x01D7] = "LATIN CAPITAL LETTER U WITH DIAERESIS AND ACUTE";
	characterNames[0x01D8] = "LATIN SMALL LETTER U WITH DIAERESIS AND ACUTE";
	characterNames[0x01D9] = "LATIN CAPITAL LETTER U WITH DIAERESIS AND CARON";
	characterNames[0x01DA] = "LATIN SMALL LETTER U WITH DIAERESIS AND CARON";
	characterNames[0x01DB] = "LATIN CAPITAL LETTER U WITH DIAERESIS AND GRAVE";
	characterNames[0x01DC] = "LATIN SMALL LETTER U WITH DIAERESIS AND GRAVE";
	characterNames[0x01DD] = "LATIN SMALL LETTER TURNED E";
	characterNames[0x01DE] = "LATIN CAPITAL LETTER A WITH DIAERESIS AND MACRON";
	characterNames[0x01DF] = "LATIN SMALL LETTER A WITH DIAERESIS AND MACRON";
	characterNames[0x01E0] = "LATIN CAPITAL LETTER A WITH DOT ABOVE AND MACRON";
	characterNames[0x01E1] = "LATIN SMALL LETTER A WITH DOT ABOVE AND MACRON";
	characterNames[0x01E2] = "LATIN CAPITAL LETTER AE WITH MACRON";
	characterNames[0x01E3] = "LATIN SMALL LETTER AE WITH MACRON";
	characterNames[0x01E4] = "LATIN CAPITAL LETTER G WITH STROKE";
	characterNames[0x01E5] = "LATIN SMALL LETTER G WITH STROKE";
	characterNames[0x01E6] = "LATIN CAPITAL LETTER G WITH CARON";
	characterNames[0x01E7] = "LATIN SMALL LETTER G WITH CARON";
	characterNames[0x01E8] = "LATIN CAPITAL LETTER K WITH CARON";
	characterNames[0x01E9] = "LATIN SMALL LETTER K WITH CARON";
	characterNames[0x01EA] = "LATIN CAPITAL LETTER O WITH OGONEK";
	characterNames[0x01EB] = "LATIN SMALL LETTER O WITH OGONEK";
	characterNames[0x01EC] = "LATIN CAPITAL LETTER O WITH OGONEK AND MACRON";
	characterNames[0x01ED] = "LATIN SMALL LETTER O WITH OGONEK AND MACRON";
	characterNames[0x01EE] = "LATIN CAPITAL LETTER EZH WITH CARON";
	characterNames[0x01EF] = "LATIN SMALL LETTER EZH WITH CARON";
	characterNames[0x01F0] = "LATIN SMALL LETTER J WITH CARON";
	characterNames[0x01F1] = "LATIN CAPITAL LETTER DZ";
	characterNames[0x01F2] = "LATIN CAPITAL LETTER D WITH SMALL LETTER Z";
	characterNames[0x01F3] = "LATIN SMALL LETTER DZ";
	characterNames[0x01F4] = "LATIN CAPITAL LETTER G WITH ACUTE";
	characterNames[0x01F5] = "LATIN SMALL LETTER G WITH ACUTE";
	characterNames[0x01F6] = "LATIN CAPITAL LETTER HWAIR";
	characterNames[0x01F7] = "LATIN CAPITAL LETTER WYNN";
	characterNames[0x01F8] = "LATIN CAPITAL LETTER N WITH GRAVE";
	characterNames[0x01F9] = "LATIN SMALL LETTER N WITH GRAVE";
	characterNames[0x01FA] = "LATIN CAPITAL LETTER A WITH RING ABOVE AND ACUTE";
	characterNames[0x01FB] = "LATIN SMALL LETTER A WITH RING ABOVE AND ACUTE";
	characterNames[0x01FC] = "LATIN CAPITAL LETTER AE WITH ACUTE";
	characterNames[0x01FD] = "LATIN SMALL LETTER AE WITH ACUTE";
	characterNames[0x01FE] = "LATIN CAPITAL LETTER O WITH STROKE AND ACUTE";
	characterNames[0x01FF] = "LATIN SMALL LETTER O WITH STROKE AND ACUTE";
	characterNames[0x0200] = "LATIN CAPITAL LETTER A WITH DOUBLE GRAVE";
	characterNames[0x0201] = "LATIN SMALL LETTER A WITH DOUBLE GRAVE";
	characterNames[0x0202] = "LATIN CAPITAL LETTER A WITH INVERTED BREVE";
	characterNames[0x0203] = "LATIN SMALL LETTER A WITH INVERTED BREVE";
	characterNames[0x0204] = "LATIN CAPITAL LETTER E WITH DOUBLE GRAVE";
	characterNames[0x0205] = "LATIN SMALL LETTER E WITH DOUBLE GRAVE";
	characterNames[0x0206] = "LATIN CAPITAL LETTER E WITH INVERTED BREVE";
	characterNames[0x0207] = "LATIN SMALL LETTER E WITH INVERTED BREVE";
	characterNames[0x0208] = "LATIN CAPITAL LETTER I WITH DOUBLE GRAVE";
	characterNames[0x0209] = "LATIN SMALL LETTER I WITH DOUBLE GRAVE";
	characterNames[0x020A] = "LATIN CAPITAL LETTER I WITH INVERTED BREVE";
	characterNames[0x020B] = "LATIN SMALL LETTER I WITH INVERTED BREVE";
	characterNames[0x020C] = "LATIN CAPITAL LETTER O WITH DOUBLE GRAVE";
	characterNames[0x020D] = "LATIN SMALL LETTER O WITH DOUBLE GRAVE";
	characterNames[0x020E] = "LATIN CAPITAL LETTER O WITH INVERTED BREVE";
	characterNames[0x020F] = "LATIN SMALL LETTER O WITH INVERTED BREVE";
	characterNames[0x0210] = "LATIN CAPITAL LETTER R WITH DOUBLE GRAVE";
	characterNames[0x0211] = "LATIN SMALL LETTER R WITH DOUBLE GRAVE";
	characterNames[0x0212] = "LATIN CAPITAL LETTER R WITH INVERTED BREVE";
	characterNames[0x0213] = "LATIN SMALL LETTER R WITH INVERTED BREVE";
	characterNames[0x0214] = "LATIN CAPITAL LETTER U WITH DOUBLE GRAVE";
	characterNames[0x0215] = "LATIN SMALL LETTER U WITH DOUBLE GRAVE";
	characterNames[0x0216] = "LATIN CAPITAL LETTER U WITH INVERTED BREVE";
	characterNames[0x0217] = "LATIN SMALL LETTER U WITH INVERTED BREVE";
	characterNames[0x0218] = "LATIN CAPITAL LETTER S WITH COMMA BELOW";
	characterNames[0x0219] = "LATIN SMALL LETTER S WITH COMMA BELOW";
	characterNames[0x021A] = "LATIN CAPITAL LETTER T WITH COMMA BELOW";
	characterNames[0x021B] = "LATIN SMALL LETTER T WITH COMMA BELOW";
	characterNames[0x021C] = "LATIN CAPITAL LETTER YOGH";
	characterNames[0x021D] = "LATIN SMALL LETTER YOGH";
	characterNames[0x021E] = "LATIN CAPITAL LETTER H WITH CARON";
	characterNames[0x021F] = "LATIN SMALL LETTER H WITH CARON";
	characterNames[0x0220] = "LATIN CAPITAL LETTER N WITH LONG RIGHT LEG";
	characterNames[0x0221] = "LATIN SMALL LETTER D WITH CURL";
	characterNames[0x0222] = "LATIN CAPITAL LETTER OU";
	characterNames[0x0223] = "LATIN SMALL LETTER OU";
	characterNames[0x0224] = "LATIN CAPITAL LETTER Z WITH HOOK";
	characterNames[0x0225] = "LATIN SMALL LETTER Z WITH HOOK";
	characterNames[0x0226] = "LATIN CAPITAL LETTER A WITH DOT ABOVE";
	characterNames[0x0227] = "LATIN SMALL LETTER A WITH DOT ABOVE";
	characterNames[0x0228] = "LATIN CAPITAL LETTER E WITH CEDILLA";
	characterNames[0x0229] = "LATIN SMALL LETTER E WITH CEDILLA";
	characterNames[0x022A] = "LATIN CAPITAL LETTER O WITH DIAERESIS AND MACRON";
	characterNames[0x022B] = "LATIN SMALL LETTER O WITH DIAERESIS AND MACRON";
	characterNames[0x022C] = "LATIN CAPITAL LETTER O WITH TILDE AND MACRON";
	characterNames[0x022D] = "LATIN SMALL LETTER O WITH TILDE AND MACRON";
	characterNames[0x022E] = "LATIN CAPITAL LETTER O WITH DOT ABOVE";
	characterNames[0x022F] = "LATIN SMALL LETTER O WITH DOT ABOVE";
	characterNames[0x0230] = "LATIN CAPITAL LETTER O WITH DOT ABOVE AND MACRON";
	characterNames[0x0231] = "LATIN SMALL LETTER O WITH DOT ABOVE AND MACRON";
	characterNames[0x0232] = "LATIN CAPITAL LETTER Y WITH MACRON";
	characterNames[0x0233] = "LATIN SMALL LETTER Y WITH MACRON";
	characterNames[0x0234] = "LATIN SMALL LETTER L WITH CURL";
	characterNames[0x0235] = "LATIN SMALL LETTER N WITH CURL";
	characterNames[0x0236] = "LATIN SMALL LETTER T WITH CURL";
	characterNames[0x0237] = "LATIN SMALL LETTER DOTLESS J";
	characterNames[0x0238] = "LATIN SMALL LETTER DB DIGRAPH";
	characterNames[0x0239] = "LATIN SMALL LETTER QP DIGRAPH";
	characterNames[0x023A] = "LATIN CAPITAL LETTER A WITH STROKE";
	characterNames[0x023B] = "LATIN CAPITAL LETTER C WITH STROKE";
	characterNames[0x023C] = "LATIN SMALL LETTER C WITH STROKE";
	characterNames[0x023D] = "LATIN CAPITAL LETTER L WITH BAR";
	characterNames[0x023E] = "LATIN CAPITAL LETTER T WITH DIAGONAL STROKE";
	characterNames[0x023F] = "LATIN SMALL LETTER S WITH SWASH TAIL";
	characterNames[0x0240] = "LATIN SMALL LETTER Z WITH SWASH TAIL";
	characterNames[0x0241] = "LATIN CAPITAL LETTER GLOTTAL STOP";
	characterNames[0x0242] = "LATIN SMALL LETTER GLOTTAL STOP";
	characterNames[0x0243] = "LATIN CAPITAL LETTER B WITH STROKE";
	characterNames[0x0244] = "LATIN CAPITAL LETTER U BAR";
	characterNames[0x0245] = "LATIN CAPITAL LETTER TURNED V";
	characterNames[0x0246] = "LATIN CAPITAL LETTER E WITH STROKE";
	characterNames[0x0247] = "LATIN SMALL LETTER E WITH STROKE";
	characterNames[0x0248] = "LATIN CAPITAL LETTER J WITH STROKE";
	characterNames[0x0249] = "LATIN SMALL LETTER J WITH STROKE";
	characterNames[0x024A] = "LATIN CAPITAL LETTER SMALL Q WITH HOOK TAIL";
	characterNames[0x024B] = "LATIN SMALL LETTER Q WITH HOOK TAIL";
	characterNames[0x024C] = "LATIN CAPITAL LETTER R WITH STROKE";
	characterNames[0x024D] = "LATIN SMALL LETTER R WITH STROKE";
	characterNames[0x024E] = "LATIN CAPITAL LETTER Y WITH STROKE";
	characterNames[0x024F] = "LATIN SMALL LETTER Y WITH STROKE";
	characterNames[0x0250] = "LATIN SMALL LETTER TURNED A";
	characterNames[0x0251] = "LATIN SMALL LETTER ALPHA";
	characterNames[0x0252] = "LATIN SMALL LETTER TURNED ALPHA";
	characterNames[0x0253] = "LATIN SMALL LETTER B WITH HOOK";
	characterNames[0x0254] = "LATIN SMALL LETTER OPEN O";
	characterNames[0x0255] = "LATIN SMALL LETTER C WITH CURL";
	characterNames[0x0256] = "LATIN SMALL LETTER D WITH TAIL";
	characterNames[0x0257] = "LATIN SMALL LETTER D WITH HOOK";
	characterNames[0x0258] = "LATIN SMALL LETTER REVERSED E";
	characterNames[0x0259] = "LATIN SMALL LETTER SCHWA";
	characterNames[0x025A] = "LATIN SMALL LETTER SCHWA WITH HOOK";
	characterNames[0x025B] = "LATIN SMALL LETTER OPEN E";
	characterNames[0x025C] = "LATIN SMALL LETTER REVERSED OPEN E";
	characterNames[0x025D] = "LATIN SMALL LETTER REVERSED OPEN E WITH HOOK";
	characterNames[0x025E] = "LATIN SMALL LETTER CLOSED REVERSED OPEN E";
	characterNames[0x025F] = "LATIN SMALL LETTER DOTLESS J WITH STROKE";
	characterNames[0x0260] = "LATIN SMALL LETTER G WITH HOOK";
	characterNames[0x0261] = "LATIN SMALL LETTER SCRIPT G";
	characterNames[0x0262] = "LATIN LETTER SMALL CAPITAL G";
	characterNames[0x0263] = "LATIN SMALL LETTER GAMMA";
	characterNames[0x0264] = "LATIN SMALL LETTER RAMS HORN";
	characterNames[0x0265] = "LATIN SMALL LETTER TURNED H";
	characterNames[0x0266] = "LATIN SMALL LETTER H WITH HOOK";
	characterNames[0x0267] = "LATIN SMALL LETTER HENG WITH HOOK";
	characterNames[0x0268] = "LATIN SMALL LETTER I WITH STROKE";
	characterNames[0x0269] = "LATIN SMALL LETTER IOTA";
	characterNames[0x026A] = "LATIN LETTER SMALL CAPITAL I";
	characterNames[0x026B] = "LATIN SMALL LETTER L WITH MIDDLE TILDE";
	characterNames[0x026C] = "LATIN SMALL LETTER L WITH BELT";
	characterNames[0x026D] = "LATIN SMALL LETTER L WITH RETROFLEX HOOK";
	characterNames[0x026E] = "LATIN SMALL LETTER LEZH";
	characterNames[0x026F] = "LATIN SMALL LETTER TURNED M";
	characterNames[0x0270] = "LATIN SMALL LETTER TURNED M WITH LONG LEG";
	characterNames[0x0271] = "LATIN SMALL LETTER M WITH HOOK";
	characterNames[0x0272] = "LATIN SMALL LETTER N WITH LEFT HOOK";
	characterNames[0x0273] = "LATIN SMALL LETTER N WITH RETROFLEX HOOK";
	characterNames[0x0274] = "LATIN LETTER SMALL CAPITAL N";
	characterNames[0x0275] = "LATIN SMALL LETTER BARRED O";
	characterNames[0x0276] = "LATIN LETTER SMALL CAPITAL OE";
	characterNames[0x0277] = "LATIN SMALL LETTER CLOSED OMEGA";
	characterNames[0x0278] = "LATIN SMALL LETTER PHI";
	characterNames[0x0279] = "LATIN SMALL LETTER TURNED R";
	characterNames[0x027A] = "LATIN SMALL LETTER TURNED R WITH LONG LEG";
	characterNames[0x027B] = "LATIN SMALL LETTER TURNED R WITH HOOK";
	characterNames[0x027C] = "LATIN SMALL LETTER R WITH LONG LEG";
	characterNames[0x027D] = "LATIN SMALL LETTER R WITH TAIL";
	characterNames[0x027E] = "LATIN SMALL LETTER R WITH FISHHOOK";
	characterNames[0x027F] = "LATIN SMALL LETTER REVERSED R WITH FISHHOOK";
	characterNames[0x0280] = "LATIN LETTER SMALL CAPITAL R";
	characterNames[0x0281] = "LATIN LETTER SMALL CAPITAL INVERTED R";
	characterNames[0x0282] = "LATIN SMALL LETTER S WITH HOOK";
	characterNames[0x0283] = "LATIN SMALL LETTER ESH";
	characterNames[0x0284] = "LATIN SMALL LETTER DOTLESS J WITH STROKE AND HOOK";
	characterNames[0x0285] = "LATIN SMALL LETTER SQUAT REVERSED ESH";
	characterNames[0x0286] = "LATIN SMALL LETTER ESH WITH CURL";
	characterNames[0x0287] = "LATIN SMALL LETTER TURNED T";
	characterNames[0x0288] = "LATIN SMALL LETTER T WITH RETROFLEX HOOK";
	characterNames[0x0289] = "LATIN SMALL LETTER U BAR";
	characterNames[0x028A] = "LATIN SMALL LETTER UPSILON";
	characterNames[0x028B] = "LATIN SMALL LETTER V WITH HOOK";
	characterNames[0x028C] = "LATIN SMALL LETTER TURNED V";
	characterNames[0x028D] = "LATIN SMALL LETTER TURN
