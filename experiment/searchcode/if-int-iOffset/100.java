/*
 * The National Archives 2005-2006. All rights reserved. See Licence.txt for
 * full licence details. Developed by: Tessella Support Services plc 3
 * Vineyard Chambers Abingdon, OX14 3PX United Kingdom http://www.tessella.com
 * Tessella/NPD/4305 PRONOM 4 $Id: SubSequence.java,v 1.8 2006/03/13 15:15:29
 * linb Exp $ $Logger: SubSequence.java,v $ Revision 1.8 2006/03/13 15:15:29
 * linb Changed copyright holder from Crown Copyright to The National
 * Archives. Added reference to licence.txt Changed dates to 2005-2006
 * Revision 1.7 2006/02/13 10:29:40 gaur Fixed bug in searching a short file
 * for a byte sequence at a large offset from BOF Revision 1.6 2006/02/13
 * 09:26:16 gaur Fixed bug in searching files from EOF, after first STS round
 * Revision 1.5 2006/02/09 15:04:37 gaur Corrected formatting Revision 1.4
 * 2006/02/07 17:16:23 linb - Change fileReader to ByteReader in formal
 * parameters of methods - use new static constructors - Add detection of if a
 * filePath is a URL or not Revision 1.3 2006/02/07 11:30:04 gaur Added
 * support for endianness of signature $History: SubSequence.java $
 * ***************** Version 6 ***************** User: Walm Date: 29/09/05
 * Time: 9:16 Updated in $/PRONOM4/FFIT_SOURCE/signatureFile Bug fix in
 * response to JIRA issue PRON-29. changed startPosInFile to an array + some
 * changes to the way start position options are dealt with. *****************
 * Version 5 ***************** User: Walm Date: 17/05/05 Time: 12:47 Updated
 * in $/PRONOM4/FFIT_SOURCE/signatureFile added more error trapping
 * ***************** Version 4 ***************** User: Walm Date: 5/04/05
 * Time: 18:08 Updated in $/PRONOM4/FFIT_SOURCE/signatureFile review headers
 */
package uk.gov.nationalarchives.droid.signatureFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.nationalarchives.droid.base.SimpleElement;
import uk.gov.nationalarchives.droid.binFileReader.ByteReader;

/**
 * holds a subsequence for a byte sequence also contains most of the logic for
 * identifying files
 * 
 * @author Martin Waller
 * @version 4.0.0
 */
public class SubSequence extends SimpleElement {
	private static final Logger LOG = LoggerFactory
			.getLogger(SubSequence.class);
	static boolean showProgress = false;
	private boolean bigEndian = true;
	private byte[] byteSequence;

	private List<LeftFragment> leftFragments = new ArrayList<LeftFragment>();

	private int maxSeqOffset = 0;
	private int minFragLength;
	private int minSeqOffset = 0;
	private final List<List<SideFragment>> orderedLeftFragments = new ArrayList<List<SideFragment>>();
	private final List<List<SideFragment>> orderedRightFragments = new ArrayList<List<SideFragment>>();
	private ByteSequence parentByteSequence;
	private int parentSignature;
	private int position;
	private String reference;
	private List<RightFragment> rightFragments = new ArrayList<RightFragment>();
	private String sequence;
	// shiftFunction shift;
	private final long[] shiftFunction = new long[256];

	/* setters */
	public void addLeftFragment(final LeftFragment lf) {
		this.leftFragments.add(lf);
	}

	public void addRightFragment(final RightFragment lf) {
		this.rightFragments.add(lf);
	}

	/**
	 * Searches for the left fragments of this subsequence between the given
	 * byte positions in the file. Either returns the last byte taken up by
	 * the identified sequences or returns -2 if no match was found
	 * 
	 * @param targetFile
	 *            the binary file to be identified
	 * @param leftBytePos
	 *            left-most byte position of allowed search window on file
	 * @param rightBytePos
	 *            right-most byte position of allowed search window on file
	 * @param searchDirection
	 *            1 for a left to right search, -1 for right to left
	 * @param offsetRange
	 *            range of possible start positions in the direction of
	 *            searchDirection
	 * @param bigEndian
	 *            True iff our parent signature is big-endian
	 */
	private long[] bytePosForLeftFragments(final ByteReader targetFile,
			final long leftBytePos, final long rightBytePos,
			final int searchDirection, final int offsetRange,
			final boolean bigEndian) {
		final boolean leftFrag = true;
		long startPos = rightBytePos;
		int posLoopStart = 1;
		final int numFragPos = getNumFragmentPositions(leftFrag);
		if (searchDirection == 1) {
			startPos = leftBytePos;
			posLoopStart = numFragPos;
		}

		// now set up the array so that it can potentially hold all
		// possibilities
		int totalNumOptions = offsetRange + 1;
		for (int iFragPos = 1; iFragPos <= numFragPos; iFragPos++) {
			totalNumOptions = totalNumOptions
					* getNumAlternativeFragments(leftFrag, iFragPos);
		}
		final long[] markerPos = new long[totalNumOptions];
		for (int iOffset = 0; iOffset <= offsetRange; iOffset++) {
			markerPos[iOffset] = startPos + iOffset * searchDirection;
		}
		int numOptions = 1 + offsetRange;

		boolean seqNotFound = false;
		for (int iFragPos = posLoopStart; (!seqNotFound)
				&& (iFragPos <= numFragPos) && (iFragPos >= 1); iFragPos -= searchDirection) {
			final int numAltFrags = getNumAlternativeFragments(leftFrag,
					iFragPos);
			final long[] tempEndPos = new long[numAltFrags * numOptions]; // array
			// to
			// store
			// possible
			// end
			// positions
			// after
			// this
			// fragment
			// position
			// has
			// been
			// examined

			int numEndPos = 0;
			for (int iOption = 0; iOption < numOptions; iOption++) {
				// will now look for all matching alternative sequence at the
				// current end positions
				for (int iAlt = 0; iAlt < numAltFrags; iAlt++) {
					long tempFragEnd;
					if (searchDirection == 1) {
						tempFragEnd = endBytePosForSeqFrag(targetFile,
								markerPos[iOption], rightBytePos, true,
								searchDirection, iFragPos, iAlt, bigEndian);
					} else {
						tempFragEnd = endBytePosForSeqFrag(targetFile,
								leftBytePos, markerPos[iOption], true,
								searchDirection, iFragPos, iAlt, bigEndian);
					}
					if (tempFragEnd > -1L) { // amatch has been found
						tempEndPos[numEndPos] = tempFragEnd + searchDirection;
						numEndPos += 1;
					}
				}
			}
			if (numEndPos == 0) {
				seqNotFound = true;
			} else {
				numOptions = 0;
				for (int iOption = 0; iOption < numEndPos; iOption++) {
					// eliminate any repeated end positions
					boolean addEndPos = true;
					for (int iMarker = 0; iMarker < numOptions; iMarker++) {
						if (markerPos[iMarker] == tempEndPos[iOption]) {
							addEndPos = false;
							break;
						}
					}
					if (addEndPos) {
						markerPos[numOptions] = tempEndPos[iOption];
						numOptions++;
					}
				}
			}
		}

		// prepare array to be returned
		if (seqNotFound) {
			// no possible positions found, return 0 length array
			final long[] outArray = new long[0];
			return outArray;

		} else {
			// return ordered array of possibilities
			final long[] outArray = new long[numOptions];

			// convert values to negative temporarily so that reverse sort
			// order
			// can be obtained for a right to left search direction
			if (searchDirection < 0) {
				for (int iOption = 0; iOption < numOptions; iOption++) {
					markerPos[iOption] = -markerPos[iOption];
				}
			}

			// sort the values in the array
			Arrays.sort(markerPos, 0, numOptions);

			// convert values back to positive now that a reverse sort order
			// has
			// been obtained
			if (searchDirection < 0) {
				for (int iOption = 0; iOption < numOptions; iOption++) {
					markerPos[iOption] = -markerPos[iOption];
				}
			}

			// copy to a new array which has precisely the correct length
			System.arraycopy(markerPos, 0, outArray, 0, numOptions);

			// correct the value
			for (int iOption = 0; iOption < numOptions; iOption++) {
				outArray[iOption] -= searchDirection;
			}

			return outArray;
		}

	}

	/**
	 * Searches for the right fragments of this subsequence between the given
	 * byte positions in the file. Either returns the last byte taken up by
	 * the identified sequences or returns -2 if no match was found
	 * 
	 * @param targetFile
	 *            the binary file to be identified
	 * @param leftBytePos
	 *            left-most byte position of allowed search window on file
	 * @param rightBytePos
	 *            right-most byte position of allowed search window on file
	 * @param searchDirection
	 *            1 for a left to right search, -1 for right to left
	 * @param offsetRange
	 *            range of possible start positions in the direction of
	 *            searchDirection
	 * @param bigEndian
	 *            True iff our parent signature is big-endian
	 */
	private long[] bytePosForRightFragments(final ByteReader targetFile,
			final long leftBytePos, final long rightBytePos,
			final int searchDirection, final int offsetRange,
			final boolean bigEndian) {
		final boolean leftFrag = false;
		long startPos = leftBytePos;
		int posLoopStart = 1;
		final int numFragPos = getNumFragmentPositions(leftFrag);
		if (searchDirection == -1) {
			startPos = rightBytePos;
			posLoopStart = numFragPos;
		}

		// now set up the array so that it can potentially hold all
		// possibilities
		int totalNumOptions = offsetRange + 1;
		for (int iFragPos = 1; iFragPos <= numFragPos; iFragPos++) {
			totalNumOptions = totalNumOptions
					* getNumAlternativeFragments(leftFrag, iFragPos);
		}
		final long[] markerPos = new long[totalNumOptions];
		for (int iOffset = 0; iOffset <= offsetRange; iOffset++) {
			markerPos[iOffset] = startPos + iOffset * searchDirection;
		}
		int numOptions = 1 + offsetRange;

		boolean seqNotFound = false;
		for (int iFragPos = posLoopStart; (!seqNotFound)
				&& (iFragPos <= numFragPos) && (iFragPos >= 1); iFragPos += searchDirection) {
			final int numAltFrags = getNumAlternativeFragments(leftFrag,
					iFragPos);
			final long[] tempEndPos = new long[numAltFrags * numOptions]; // array
			// to
			// store
			// possible
			// end
			// positions
			// after
			// this
			// fragment
			// position
			// has
			// been
			// examined
			int numEndPos = 0;
			for (int iOption = 0; iOption < numOptions; iOption++) {
				// will now look for all matching alternative sequence at the
				// current end positions
				for (int iAlt = 0; iAlt < numAltFrags; iAlt++) {
					long tempFragEnd;
					if (searchDirection == -1) {
						tempFragEnd = endBytePosForSeqFrag(targetFile,
								leftBytePos, markerPos[iOption], false,
								searchDirection, iFragPos, iAlt, bigEndian);
					} else {
						tempFragEnd = endBytePosForSeqFrag(targetFile,
								markerPos[iOption], rightBytePos, false,
								searchDirection, iFragPos, iAlt, bigEndian);
					}
					if (tempFragEnd > -1) { // amatch has been found
						tempEndPos[numEndPos] = tempFragEnd + searchDirection;
						numEndPos += 1;
					}
				}
			}
			if (numEndPos == 0) {
				seqNotFound = true;
			} else {
				numOptions = 0;
				for (int iOption = 0; iOption < numEndPos; iOption++) {
					// eliminate any repeated end positions
					boolean addEndPos = true;
					for (int iMarker = 0; iMarker < numOptions; iMarker++) {
						if (markerPos[iMarker] == tempEndPos[iOption]) {
							addEndPos = false;
							break;
						}
					}
					if (addEndPos) {
						markerPos[numOptions] = tempEndPos[iOption];
						numOptions++;
					}
				}
			}
		}

		// prepare array to be returned
		if (seqNotFound) {
			// no possible positions found, return 0 length array
			final long[] outArray = new long[0];
			return outArray;

		} else {
			// return ordered array of possibilities
			final long[] outArray = new long[numOptions];

			// convert values to negative temporarily so that reverse sort
			// order
			// can be obtained for a right to left search direction
			if (searchDirection < 0) {
				for (int iOption = 0; iOption < numOptions; iOption++) {
					markerPos[iOption] = -markerPos[iOption];
				}
			}

			// sort the values in the array
			Arrays.sort(markerPos, 0, numOptions);

			// convert values back to positive now that a reverse sort order
			// has
			// been obtained
			if (searchDirection < 0) {
				for (int iOption = 0; iOption < numOptions; iOption++) {
					markerPos[iOption] = -markerPos[iOption];
				}
			}

			// copy to a new array which has precisely the correct length
			System.arraycopy(markerPos, 0, outArray, 0, numOptions);

			// correct the value
			for (int iOption = 0; iOption < numOptions; iOption++) {
				outArray[iOption] -= searchDirection;
			}

			return outArray;
		}

	}

	/**
	 * searches for the specified fragment sequence between the leftmost and
	 * rightmost byte positions that are given. returns the end position of
	 * the found sequence or -1 if it is not found
	 * 
	 * @param targetFile
	 *            The file that is being reviewed for identification
	 * @param leftEndBytePos
	 *            leftmost position in file at which to search
	 * @param rightEndBytePos
	 *            rightmost postion in file at which to search
	 * @param leftFrag
	 *            flag to indicate whether looking at left or right fragments
	 * @param searchDirection
	 *            direction in which search is carried out (1 for left to
	 *            right, -1 for right to left)
	 * @param fragPos
	 *            position of left/right sequence fragment to use
	 * @param fragIndex
	 *            index of fragment within the position (where alternatives
	 *            exist)
	 * @param bigEndian
	 *            True iff out parent signature is big-endian
	 */
	private long endBytePosForSeqFrag(final ByteReader targetFile,
			final long leftEndBytePos, final long rightEndBytePos,
			final boolean leftFrag, final int searchDirection,
			final int fragPos, final int fragIndex, final boolean bigEndian) {
		long startPosInFile;
		long lastStartPosInFile;
		long endPosInFile = -1L;
		final long searchDirectionL = searchDirection;
		int numBytes;
		int minOffset;
		int maxOffset;

		// read in values
		numBytes = getFragment(leftFrag, fragPos, fragIndex).getNumBytes();
		if (leftFrag && (searchDirection == -1)) {
			minOffset = getFragment(leftFrag, fragPos, fragIndex)
					.getMinOffset();
			maxOffset = getFragment(leftFrag, fragPos, fragIndex)
					.getMaxOffset();
		} else if (!leftFrag && (searchDirection == 1)) {
			minOffset = getFragment(leftFrag, fragPos, fragIndex)
					.getMinOffset();
			maxOffset = getFragment(leftFrag, fragPos, fragIndex)
					.getMaxOffset();
		} else if (fragPos < getNumFragmentPositions(leftFrag)) {
			minOffset = getFragment(leftFrag, fragPos + 1, 0).getMinOffset();
			maxOffset = getFragment(leftFrag, fragPos + 1, 0).getMaxOffset();
		} else {
			minOffset = 0;
			maxOffset = 0;
		}

		// set up start and end positions for searches taking into account min
		// and max offsets
		if (searchDirection == -1) {
			startPosInFile = rightEndBytePos - minOffset;
			final long lastStartPosInFile1 = leftEndBytePos + numBytes - 1L;
			final long lastStartPosInFile2 = rightEndBytePos - maxOffset;
			lastStartPosInFile = (lastStartPosInFile1 < lastStartPosInFile2) ? lastStartPosInFile2
					: lastStartPosInFile1;
		} else {
			startPosInFile = leftEndBytePos + minOffset;
			final long lastStartPosInFile1 = rightEndBytePos - numBytes + 1L;
			final long lastStartPosInFile2 = leftEndBytePos + maxOffset;
			lastStartPosInFile = (lastStartPosInFile1 < lastStartPosInFile2) ? lastStartPosInFile1
					: lastStartPosInFile2;
		}

		// keep searching until either the sequence fragment is found or until
		// the end of the search area has been reached.
		// compare sequence with file contents directly at fileMarker position
		boolean subSeqFound = false;
		while ((!subSeqFound)
				&& ((searchDirectionL)
						* (lastStartPosInFile - startPosInFile) >= 0L)) {
			boolean missMatchFound = false;
			if (searchDirection == -1) {
			} else {
			}
			final SideFragment fragment = getFragment(leftFrag, fragPos,
					fragIndex);
			long tempFileMarker = startPosInFile;
			for (int i = (searchDirection == 1) ? 0 : fragment
					.getNumByteSeqSpecifiers() - 1; !missMatchFound
					&& (0 <= i) && (i < fragment.getNumByteSeqSpecifiers()); i += searchDirection) {
				missMatchFound = !fragment.getByteSeqSpecifier(i)
						.matchesByteSequence(targetFile, tempFileMarker,
								searchDirection, bigEndian);
				if (!missMatchFound) {
					tempFileMarker += searchDirection
							* fragment.getByteSeqSpecifier(i).getNumBytes();
				}
			}
			if (!missMatchFound) { // subsequence fragment was found in the
									// file
				subSeqFound = true;
				endPosInFile = tempFileMarker - searchDirectionL;
			} else {
				startPosInFile += searchDirectionL;
			}
		}
		return endPosInFile; // this is -1 unless subSeqFound = true
	}

	public byte getByte(final int theIndex) {
		return this.byteSequence[theIndex];
	}

	public ByteSequence getByteSequence() {
		return this.parentByteSequence;
	}

	// TODO from UCDetector: Change visibility of Method
	// "SubSequence.getFragment(boolean,int,int)" to private
	public SideFragment getFragment(final boolean leftFrag, // NO_UCD
			final int thePosition, final int theIndex) {
		if (leftFrag) {
			return (SideFragment) ((ArrayList) this.orderedLeftFragments
					.get(thePosition - 1)).get(theIndex);
		} else {
			return (SideFragment) ((ArrayList) this.orderedRightFragments
					.get(thePosition - 1)).get(theIndex);
		}
	}

	/**
	 * Interpret the bytes in a file as an offset.
	 * <p>
	 * The next <code>indirectOffsetLength()</code> bytes after
	 * <code>indirectOffsetLocation()</code> are interpreted as an offset
	 * according to the endianness of the byte sequence.
	 * 
	 * @param targetFile
	 * @return
	 */
	private int getIndirectOffset(final ByteReader targetFile) {
		int offset = 0;
		long power = 1;

		long offsetLocation = getByteSequence().getIndirectOffsetLocation();

		if (getByteSequence().getReference().endsWith("EOFoffset")) {
			offsetLocation = targetFile.getNumBytes() - offsetLocation - 1;
		}

		final int offsetLength = getByteSequence().getIndirectOffsetLength();

		// In the case of indirect BOF or indirect EOF bytesequences,
		// We need to get read the file to get the offset.
		if (isBigEndian()) {
			for (int i = offsetLength - 1; i > -1; i--) {
				final Byte fileByte = targetFile.getByte(offsetLocation + i);
				int byteValue = fileByte.intValue();
				byteValue = (byteValue >= 0) ? byteValue : byteValue + 256;
				offset += power * byteValue;
				power *= 256;
			}
		} else {
			for (int i = 0; i < offsetLength; i++) {
				final Byte fileByte = targetFile.getByte(offsetLocation + i);
				int byteValue = fileByte.intValue();
				byteValue = (byteValue >= 0) ? byteValue : byteValue + 256;
				offset += power * byteValue;
				power *= 256;
			}
		}

		return offset;
	}

	public List<LeftFragment> getLeftFragments() {
		return this.leftFragments;
	}

	public int getMaxSeqOffset() {
		return this.maxSeqOffset;
	}

	public int getMinFragLength() {
		return this.minFragLength;
	}

	public int getMinSeqOffset() {
		return this.minSeqOffset;
	}

	// TODO from UCDetector: Change visibility of Method
	// "SubSequence.getNumAlternativeFragments(boolean,int)" to private
	public int getNumAlternativeFragments(final boolean leftFrag, // NO_UCD
			final int thePosition) {
		if (leftFrag) {
			return ((ArrayList) this.orderedLeftFragments
					.get(thePosition - 1)).size();
		} else {
			return ((ArrayList) this.orderedRightFragments
					.get(thePosition - 1)).size();
		}
	}

	public int getNumBytes() {
		return this.byteSequence.length;
	}

	/* getters */
	// TODO from UCDetector: Change visibility of Method
	// "SubSequence.getNumFragmentPositions(boolean)" to private
	public int getNumFragmentPositions(final boolean leftFrag) { // NO_UCD
		if (leftFrag) {
			return this.orderedLeftFragments.size();
		} else {
			return this.orderedRightFragments.size();
		}
	}

	/**
	 * Get the id of the internal signature that this sequence belongs to
	 * 
	 * @return
	 */
	public int getParentSignature() {
		return this.parentSignature;
	}

	public int getPosition() {
		return this.position;
	}

	// TODO from UCDetector: Change visibility of Method
	// "SubSequence.getRawLeftFragment(int)" to private
	public LeftFragment getRawLeftFragment(final int theIndex) { // NO_UCD
		return this.leftFragments.get(theIndex);
	}

	// TODO from UCDetector: Change visibility of Method
	// "SubSequence.getRawRightFragment(int)" to private
	public RightFragment getRawRightFragment(final int theIndex) { // NO_UCD
		return this.rightFragments.get(theIndex);
	}

	public List<RightFragment> getRightFragments() {
		return this.rightFragments;
	}

	public String getSequence() {
		return this.sequence;
	}

	public long getShift(final byte theByteValue) {
		// this.ShiftFunction is a long[256] array
		return this.shiftFunction[theByteValue + 128];
	}

	public boolean isBigEndian() {
		return this.bigEndian;
	}

	/**
	 * is this a BOF sub squence If this subsequence does not match the we can
	 * reject the entire signature
	 * 
	 * @return boolean
	 */
	public boolean isBOF() {
		return (this.reference.equalsIgnoreCase("BOFoffset") && (this.position == 1));
	}

	/**
	 * is this a EOF sub squence If this subsequence does not match the we can
	 * reject the entire signature
	 * 
	 * @return boolean
	 */
	public boolean isEOF() {
		return (this.reference.equalsIgnoreCase("EOFoffset") && (this.position == 1));
	}

	/**
	 * Searches for this subsequence after the current file marker position in
	 * the file. Moves the file marker to the end of this subsequence.
	 * 
	 * @param targetFile
	 *            the binary file to be identified
	 * @param reverseOrder
	 *            true if file is being searched from right to left
	 * @param bigEndian
	 *            True iff our parent signature is big-endian
	 */
	protected boolean isFoundAfterFileMarker(final ByteReader targetFile,
			final boolean reverseOrder, final boolean bigEndian) {
		boolean subSeqFound = false;
		try {
			final long fileSize = targetFile.getNumBytes() - 1;
			final int searchDirection = reverseOrder ? -1 : 1;
			// get the current file marker
			long startPosInFile = targetFile.getFileMarker();
			// Add the minimum offset before start of sequence and update the
			// file
			// marker accordingly
			startPosInFile = startPosInFile
					+ (searchDirection * getMinSeqOffset());
			if (fileSize < startPosInFile - 1) {
				// We're looking for a sequence of bytes at an offset which is
				// longer than the file itself
				return false;
			}
			targetFile.setFileMarker(startPosInFile);
			// start searching for main sequence after the minimum length of
			// the
			// relevant fragments
			startPosInFile = startPosInFile
					+ (searchDirection * getMinFragLength());
			final int numSeqBytes = getNumBytes();

			boolean missMatchFound;
			final int byteLoopStart = reverseOrder ? numSeqBytes - 1 : 0;
			final int byteLoopEnd = reverseOrder ? 0 : numSeqBytes - 1;

			try {
				while (!subSeqFound) {

					// compare sequence with file contents directly at
					// fileMarker
					// position
					missMatchFound = false;

					// Start by checking the last byte in the window on the
					// file.
					// If this byte is different from the last byte in the
					// subsequence,
					// Then we may shift the window according to the value of
					// this
					// byte.
					// In practice, this saves us from unnecessarily checking
					// file
					// bytes to calculate the shift.
					final byte lastByte = targetFile.getByte(startPosInFile
							+ byteLoopEnd);
					if (this.byteSequence[byteLoopEnd] != lastByte) {
						startPosInFile += (this.shiftFunction[128 + lastByte] - 1);
						if ((startPosInFile < 0L)
								|| (startPosInFile > fileSize)) {
							break;
						}
					} else {
						// If the last bytes don't match, then check the rest.
						for (int iByte = byteLoopStart; (!missMatchFound)
								&& (iByte <= numSeqBytes - 1) && (iByte >= 0); iByte += searchDirection) {
							missMatchFound = (this.byteSequence[iByte] != targetFile
									.getByte(startPosInFile + iByte
											- byteLoopStart));
						}
						if (!missMatchFound) { // subsequence was found at
												// position
							// fileMarker in the file
							// Now search for fragments between original
							// fileMarker
							// and startPosInFile
							if (reverseOrder) {
								final long[] rightFragEndArray = bytePosForRightFragments(
										targetFile, startPosInFile + 1,
										targetFile.getFileMarker(), 1, 0,
										bigEndian);
								if (rightFragEndArray.length == 0) {
									missMatchFound = true;
								} else {
									long leftFragEnd;
									final long[] leftFragEndArray = bytePosForLeftFragments(
											targetFile, 0, startPosInFile
													- numSeqBytes, -1, 0,
											bigEndian);
									if (leftFragEndArray.length == 0) {
										missMatchFound = true;
									} else {
										leftFragEnd = leftFragEndArray[0];
										targetFile
												.setFileMarker(leftFragEnd - 1L);
										subSeqFound = true;
									}
								}

							} else { // search is in forward direction
								final long[] leftFragEndArray = bytePosForLeftFragments(
										targetFile,
										targetFile.getFileMarker(),
										startPosInFile - 1L, -1, 0, bigEndian);
								if (leftFragEndArray.length == 0) {
									missMatchFound = true;
								} else {
									long rightFragEnd;
									final long[] rightFragEndArray = bytePosForRightFragments(
											targetFile, startPosInFile
													+ numSeqBytes,
											targetFile.getNumBytes() - 1L, 1,
											0, bigEndian);
									if (rightFragEndArray.length == 0) {
										missMatchFound = true;
									} else {
										rightFragEnd = rightFragEndArray[0];
										targetFile
												.setFileMarker(rightFragEnd + 1L);
										subSeqFound = true;
									}
								}
							}
						}

						if (missMatchFound) {
							// If a mismatch is found, then shift the window
							// by a
							// shift calculated from the value
							// of the file byte occuring one place after the
							// window
							// position.
							startPosInFile += this.shiftFunction[128 + targetFile
									.getByte(startPosInFile
											+ (searchDirection * numSeqBytes))];
							if ((startPosInFile < 0L)
									|| (startPosInFile > fileSize)) {
								break;
							}
						}
					}
				}
			} catch (final IndexOutOfBoundsException e) {
				// This only happens when the end of the file is reached.
				// This exception is allowed to be thrown to avoid repeatedly
				// checking if the index is valid
				// and to hence improve the performace of DROID
			}
		} catch (final IndexOutOfBoundsException e) {
			// This is thrown if targetFile is a URLByteReader, the embedded
			// HeapByteBuffer will check for each access
			// and throw java.lang.IndexOutOfBoundsException if we are on or
			// past the limit
		}
		return subSeqFound;
	}

	/**
	 * Searches for this subsequence at the start of the current file. Moves
	 * the file marker to the end of this subsequence.
	 * 
	 * @param targetFile
	 *            the binary file to be identified
	 * @param reverseOrder
	 *            true if file is being searched from right to left
	 * @param bigEndian
	 *            True iff our parent signature is big-endian
	 */
	protected boolean isFoundAtStartOfFile(final ByteReader targetFile,
			final boolean reverseOrder, final boolean bigEndian) {

		try {
			final int searchDirection = reverseOrder ? -1 : 1;
			int minSeqOffset = getMinSeqOffset();
			int maxSeqOffset = getMaxSeqOffset();

			// Get any indirect offset
			if (this.reference.startsWith("Indirect")) {
				try {
					final int indirectOffset = getIndirectOffset(targetFile);
					minSeqOffset += indirectOffset;
					maxSeqOffset += indirectOffset;
				} catch (final Exception e) {
					// If an exception is thrown, we can assume that the file
					// did not match the indirect offset
					// eg. the indirect offset found could be too large to be
					// held in an int type
					return false;
				}
			}

			long[] startPosInFile = new long[1];
			startPosInFile[0] = reverseOrder ? targetFile.getNumBytes()
					- minSeqOffset - 1 : minSeqOffset;
			boolean subseqFound = true;
			boolean leftFrag = true;

			if (reverseOrder) {
				leftFrag = false;
			}

			// match intial fragment
			if (reverseOrder) {
				startPosInFile = bytePosForRightFragments(targetFile, 0,
						startPosInFile[0], -1, (maxSeqOffset - minSeqOffset),
						bigEndian);
			} else {
				startPosInFile = bytePosForLeftFragments(targetFile,
						startPosInFile[0], targetFile.getNumBytes() - 1, 1,
						(maxSeqOffset - minSeqOffset), bigEndian);
			}
			int numOptions = startPosInFile.length;
			if (numOptions == 0) {
				subseqFound = false;
			} else {
				for (int i = 0; i < numOptions; i++) {
					startPosInFile[i] += searchDirection;
				}
			}

			// match main sequence
			if (subseqFound) {

				// move startPosInFile according to min offset of last
				// fragment
				// looked at
				int minOffset = 0;
				int maxOffset = 0;
				if (getNumFragmentPositions(leftFrag) > 0) {
					minOffset = getFragment(leftFrag, 1, 0).getMinOffset();
					maxOffset = getFragment(leftFrag, 1, 0).getMaxOffset();
					for (int i = 0; i < numOptions; i++) {
						startPosInFile[i] += (minOffset * searchDirection);
					}
				}

				// add new possible values for startPosInFile to allow for
				// difference between maxOffset and minOffset
				final int offsetRange = maxOffset - minOffset;
				if (offsetRange > 0) {
					final long[] newStartPosInFile = new long[numOptions
							* (offsetRange + 1)];
					for (int i = 0; i <= offsetRange; i++) {
						for (int j = 0; j < numOptions; j++) {
							newStartPosInFile[j + i * numOptions] = startPosInFile[j]
									+ (i * searchDirection);
						}
					}
					Arrays.sort(newStartPosInFile);
					int newNumOptions = 1;
					for (int i = 1; i < numOptions * (offsetRange + 1); i++) {
						if (newStartPosInFile[i] > newStartPosInFile[newNumOptions - 1]) {
							newStartPosInFile[newNumOptions] = newStartPosInFile[i];
							newNumOptions++;
						}
					}
					// now copy these back to the startPosInFile array (sorted
					// in searchDirection)
					numOptions = newNumOptions;
					if (searchDirection > 1) {
						System.arraycopy(newStartPosInFile, 0,
								startPosInFile, 0, numOptions);
					} else {
						// reverse order copy
						for (int i = 0; i < numOptions; i++) {
							startPosInFile[i] = newStartPosInFile[numOptions
									- 1 - i];
						}
					}

				}

				// check that the end of the file is not going to be reached
				final int numSeqBytes = getNumBytes();
				final long numBytesInFile = targetFile.getNumBytes();
				if (reverseOrder) {
					// cutoff if startPosInFile is too close to start of file
					for (int i = 0; i < numOptions; i++) {
						if (startPosInFile[i] < (numSeqBytes - 1L)) {
							numOptions = i;
						}
					}
				} else {
					// cutoff if startPosInFile is too close to end of file
					for (int i = 0; i < numOptions; i++) {
						if (startPosInFile[i] > (numBytesInFile - numSeqBytes)) {
							numOptions = i;
						}
					}
				}

				for (int iOption = 0; iOption < numOptions; iOption++) {
					// compare sequence with file contents directly at
					// fileMarker position
					final int byteLoopStart = reverseOrder ? numSeqBytes - 1
							: 0;
					long tempFileMarker = startPosInFile[iOption];
					boolean provSeqMatch = true;

					// check whether the file and signature sequences match
					for (int iByte = byteLoopStart; (provSeqMatch)
							&& (iByte <= numSeqBytes - 1) && (iByte >= 0); iByte += searchDirection) {
						provSeqMatch = (this.byteSequence[iByte] == targetFile
								.getByte(tempFileMarker));
						tempFileMarker += searchDirection;
					}

					if (!provSeqMatch) {
						// no match
						startPosInFile[iOption] = -2L;
					} else {
						// success: a match was found - update the
						// startPosInFile
						startPosInFile[iOption] = tempFileMarker;
					}
				}

				// check the startPosInFile array: remove -2 values, reorder
				// and
				// remove duplicates
				Arrays.sort(startPosInFile, 0, numOptions);
				int newNumOptions = 0;
				final long[] newStartPosInFile = new long[numOptions];
				if (numOptions > 0) {
					if (startPosInFile[0] >= -1L) {
						newStartPosInFile[0] = startPosInFile[0];
						newNumOptions = 1;
					}
				}
				for (int i = 1; i < numOptions; i++) {
					if (startPosInFile[i] > startPosInFile[i - 1]) {
						newStartPosInFile[newNumOptions] = startPosInFile[i];
						newNumOptions++;
					}
				}

				if (newNumOptions == 0) {
					subseqFound = false;
				} else {
					numOptions = newNumOptions;
					if (searchDirection < 0) {
						// for right to left search direction, reorder in
						// reverse
						for (int iOption = 0; iOption < numOptions; iOption++) {
							startPosInFile[iOption] = newStartPosInFile[numOptions
									- 1 - iOption];
						}
					} else {
						// for left to right search direction, copy over as is
						System.arraycopy(newStartPosInFile, 0,
								startPosInFile, 0, numOptions);
					}
				}
			}

			// match remaining sequence fragment
			long newValueStartPosInFile = 0L;
			if (subseqFound) {

				long[] newArrayStartPosInFile;
				if (reverseOrder) {
					int i = 0;
					subseqFound = false;
					while ((i < numOptions) && !subseqFound) {
						newArrayStartPosInFile = bytePosForLeftFragments(
								targetFile, 0L, startPosInFile[i], -1, 0,
								bigEndian);
						if (newArrayStartPosInFile.length == 0) {
							subseqFound = false;
						} else {
							subseqFound = true;
							newValueStartPosInFile = newArrayStartPosInFile[0] - 1L; // take
							// away
							// -1???
						}
						i++;
					}
				} else {
					int i = 0;
					subseqFound = false;
					while ((i < numOptions) && !subseqFound) {
						newArrayStartPosInFile = bytePosForRightFragments(
								targetFile, startPosInFile[i],
								targetFile.getNumBytes() - 1L, 1, 0,
								bigEndian);
						if (newArrayStartPosInFile.length == 0) {
							subseqFound = false;
						} else {
							subseqFound = true;
							newValueStartPosInFile = newArrayStartPosInFile[0] + 1L; // take
							// away
							// +1????
						}
						i++;
					}
				}
			}

			// update the file marker
			if (subseqFound) {
				targetFile.setFileMarker(newValueStartPosInFile);
			}

			return subseqFound;
		} catch (final IndexOutOfBoundsException e) {
			// If an indirect offset points to a place that is after the end
			// of
			// the file,
			// Then this exception is thrown and it can be assumed that the
			// signature is not compliant
			return false;
		}

	}

	/**
	 * Re-orders the left and right sequence fragments in increasing position
	 * order this method must be after the signature file has been parsed and
	 * before running any file identifications
	 */
	protected void prepareSeqFragments() {

		/* Left fragments */
		// Determine the number of fragment subsequences there are
		int numFrags = 0;
		for (int i = 0; i < this.leftFragments.size(); i++) {
			final int currentPosition = getRawLeftFragment(i).getPosition();
			if (currentPosition > numFrags) {
				numFrags = currentPosition;
			}
		}

		// initialise all necessary fragment lists (one for each position)
		for (int i = 0; i < numFrags; i++) { // loop through fragment
												// positions
			final List<SideFragment> alternativeFragments = new ArrayList<SideFragment>();
			this.orderedLeftFragments.add(alternativeFragments);
		}

		// Add fragments to new structure
		for (int i = 0; i < this.leftFragments.size(); i++) { // loop through
			// all fragments
			final int currentPosition = getRawLeftFragment(i).getPosition();
			this.orderedLeftFragments.get(currentPosition - 1).add(
					getRawLeftFragment(i));
		}

		// clear out unecessary info
		this.leftFragments = null;

		/* Right fragments */
		// Determine the number of fragment subsequences there are
		numFrags = 0;
		for (int i = 0; i < this.rightFragments.size(); i++) {
			final int currentPosition = getRawRightFragment(i).getPosition();
			if (currentPosition > numFrags) {
				numFrags = currentPosition;
			}
		}

		// initialise all necessary fragment lists (one for each position)
		for (int i = 0; i < numFrags; i++) { // loop through fragment
												// positions
			final List<SideFragment> alternativeFragments = new ArrayList<SideFragment>();
			this.orderedRightFragments.add(alternativeFragments);
		}

		// Add fragments to new structure
		for (int i = 0; i < this.rightFragments.size(); i++) { // loop through
			// all fragments
			final int currentPosition = getRawRightFragment(i).getPosition();
			this.orderedRightFragments.get(currentPosition - 1).add(
					getRawRightFragment(i));
		}

		// clear out unecessary info
		this.rightFragments = null;

	}

	@Override
	public void setAttributeValue(final String name, final String value) {
		if (name.equals("Position")) {
			setPosition(Integer.parseInt(value));
		} else if (name.equals("SubSeqMinOffset")) {
			setMinSeqOffset(Integer.parseInt(value));
		} else if (name.equals("SubSeqMaxOffset")) {
			setMaxSeqOffset(Integer.parseInt(value));
		} else if (name.equals("MinFragLength")) {
			setMinFragLength(Integer.parseInt(value));
		} else {
			final String theCMDMessage = "WARNING: Unknown XML attribute "
					+ name + " found for " + getElementName() + " ";
			LOG.warn(theCMDMessage);
		}
	}

	public void setBigEndian(final boolean bigEndian) {
		this.bigEndian = bigEndian;
	}

	public void setByteSequence(final ByteSequence byteSequence) {
		this.parentByteSequence = byteSequence;
	}

	public void setDefaultShift(final String theValue) {
		for (int i = 0; i < 256; i++) {
			this.shiftFunction[i] = Long.parseLong(theValue);
		}
	}

	public void setMaxSeqOffset(final int theOffset) {
		this.maxSeqOffset = theOffset;
		if (this.maxSeqOffset < this.minSeqOffset) {
			this.maxSeqOffset = this.minSeqOffset;
		}
	}

	public void setMinFragLength(final int theLength) {
		this.minFragLength = theLength;
	}

	public void setMinSeqOffset(final int theOffset) {
		this.minSeqOffset = theOffset;
		if (this.maxSeqOffset < this.minSeqOffset) {
			this.maxSeqOffset = this.minSeqOffset;
		}
	}

	public void setParentSignature(final int parentSignature) {
		this.parentSignature = parentSignature;
	}

	public void setPosition(final int position) {
		this.position = position;
	}

	public void setReference(final String reference) {
		this.reference = reference;
	}

	public void setSequence(final String seq) {
		this.sequence = seq;
		final int seqLength = seq.length() / 2;
		if (2 * seqLength != seq.length()) {
			System.out
					.println("A problem - sequence of odd length was found: "
							+ seq);
		}
		this.byteSequence = new byte[seqLength];
		for (int i = 0; i < seqLength; i++) {
			final int byteVal = Integer.parseInt(
					seq.substring(2 * i, 2 * (i + 1)), 16);
			this.byteSequence[i] = (byteVal > Byte.MAX_VALUE) ? (byte) (byteVal - 256)
					: (byte) byteVal;
		}
	}

	public void setShift(final Shift theShift) {
		final int theShiftByte = theShift.getShiftByte();
		if ((theShiftByte >= 0) && (theShiftByte < 128)) {
			this.shiftFunction[theShiftByte + 128] = theShift.getShiftValue();
		} else if ((theShiftByte >= 128) && (theShiftByte < 256)) {
			this.shiftFunction[theShiftByte - 128] = theShift.getShiftValue();
		}
	}

	@Override
	public String toString() {
		return this.position + " seq=<" + this.sequence + ">" + "LLL"
				+ this.orderedLeftFragments + "LLL" + "RRR"
				+ this.orderedRightFragments + "RRR";
	}
}

