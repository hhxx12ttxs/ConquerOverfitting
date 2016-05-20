package eclihx.ui.internal.ui.editors.hx.indent;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;

/**
 * Indentation strategy for haxe comments and haxe documentation comments.
 * 
 * Strongly based on @see org.eclipse.jdt.internal.ui.text.javadoc.JavaDocAutoIndentStrategy.
 */
public class HaxeDocAutoIndentStrategy implements IAutoEditStrategy {

	/*
	 * Copied from {@see
	 * DefaultIndentLineAutoEditStrategy#findEndOfWhiteSpace()}
	 */
	protected int findEndOfWhiteSpace(IDocument document, int offset, int end)
			throws BadLocationException {
		while (offset < end) {
			char c = document.getChar(offset);
			if (c != ' ' && c != '\t') {
				return offset;
			}
			offset++;
		}
		return end;
	}
	
	/**
	 * Copies the indentation of the previous line and adds a star. If the
	 * Javadoc just started on this line add standard method tags and close the
	 * Javadoc.
	 * 
	 * @param d the document to work on
	 * @param c the command to deal with
	 */
	private void indentAfterNewLine(IDocument d, DocumentCommand c) {

		int offset = c.offset;
		if (offset == -1 || d.getLength() == 0)
			return;

		try {
			int p = (offset == d.getLength() ? offset - 1 : offset);
			IRegion lineRegion = d.getLineInformationOfOffset(p);

			int lineOffset = lineRegion.getOffset();
			int firstNonWS = findEndOfWhiteSpace(d, lineOffset, offset);
			Assert.isTrue(firstNonWS >= lineOffset, "indentation must not be negative"); //$NON-NLS-1$

			StringBuffer buf = new StringBuffer(c.text);
			IRegion prefix = findPrefixRange(d, lineRegion);
			String indentation = d.get(prefix.getOffset(), prefix.getLength());
			int lengthToAdd = Math.min(offset - prefix.getOffset(),
					prefix.getLength());

			buf.append(indentation.substring(0, lengthToAdd));

			if (firstNonWS < offset) {
				if (d.getChar(firstNonWS) == '/') {
					// Javadoc started on this line
					buf.append(" * "); //$NON-NLS-1$
					
					if (isNewComment(d, offset)) {
						c.shiftsCaret = false;
						c.caretOffset = c.offset + buf.length();
						String lineDelimiter = TextUtilities.getDefaultLineDelimiter(d);

						int eolOffset = lineOffset + lineRegion.getLength();
						int replacementLength = eolOffset - p;
						String restOfLine = d.get(p, replacementLength);
						String endTag = lineDelimiter + indentation + " */"; //$NON-NLS-1$

						c.length = replacementLength;
						buf.append(restOfLine);
						buf.append(endTag);
					}
				}
			}

			// move the caret behind the prefix, even if we do not have to
			// insert it.
			if (lengthToAdd < prefix.getLength())
				c.caretOffset = offset + prefix.getLength() - lengthToAdd;
			c.text = buf.toString();

		} catch (BadLocationException excp) {
			// stop work
		}
	}

	/**
	 * Returns the range of the haxe doc prefix on the given line in
	 * <code>document</code>. The prefix greedily matches the following regex
	 * pattern: <code>\w*\*\w*</code>, that is, any number of whitespace
	 * characters, followed by an asterisk ('*'), followed by any number of
	 * whitespace characters.
	 * 
	 * @param document the document to which <code>line</code> refers
	 * @param line the line from which to extract the prefix range
	 * @return an <code>IRegion</code> describing the range of the prefix on the
	 *         given line
	 * @throws BadLocationException if accessing the document fails
	 */
	private IRegion findPrefixRange(IDocument document, IRegion line) throws BadLocationException {
		int lineOffset = line.getOffset();
		int lineEnd = lineOffset + line.getLength();
		
		int indentEnd = findEndOfWhiteSpace(document, lineOffset, lineEnd);
		if (indentEnd < lineEnd && document.getChar(indentEnd) == '*') {
			indentEnd++;
			while (indentEnd < lineEnd && document.getChar(indentEnd) == ' ') {
				indentEnd++;
			}
		}
		
		return new Region(lineOffset, indentEnd - lineOffset);
	}

	/**
	 * Guesses if the command operates within a newly created Javadoc comment or
	 * not. If in doubt, it will assume that the Javadoc is new.
	 * 
	 * @param document the document
	 * @param commandOffset the command offset
	 * @return <code>true</code> if the comment should be closed,
	 *         <code>false</code> if not
	 */
	private boolean isNewComment(IDocument document, int commandOffset) {

		try {
			int lineIndex = document.getLineOfOffset(commandOffset) + 1;
			if (lineIndex >= document.getNumberOfLines()) {
				return true;
			}

			IRegion line = document.getLineInformation(lineIndex);
			ITypedRegion partition = document.getPartition(commandOffset);
					
			int partitionEnd = partition.getOffset() + partition.getLength();
			if (line.getOffset() >= partitionEnd) {
				return false;
			}

			if (document.getLength() == partitionEnd) {
				// partition goes to end of document - probably a new comment
				return true; 
			}

			String comment = document.get(partition.getOffset(),
					partition.getLength());
			if (comment.indexOf("/*", 2) != -1) { //$NON-NLS-1$
				// enclosed another comment -> probably a new comment
				return true;
			}

			return false;

		} catch (BadLocationException e) {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.text.IAutoEditStrategy#customizeDocumentCommand(org
	 * .eclipse.jface.text.IDocument, org.eclipse.jface.text.DocumentCommand)
	 */
	@Override
	public void customizeDocumentCommand(IDocument document,
			DocumentCommand command) {
		if (command.length == 0
				&& command.text != null
				&& TextUtilities.endsWith(document.getLegalLineDelimiters(),
						command.text) != -1) {

			indentAfterNewLine(document, command);
		}
	}

}

