package org.codehaus.groovy.eclipse.codeassist.completion;

import org.codehaus.groovy.ast.ModuleNode;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.contentassist.*;
import org.eclipse.swt.graphics.*;

public class GroovyProposal implements ICompletionProposal {

	protected String replacementString;
	protected int replacementOffset;
	protected int replacementLength;
	protected int cursorPosition;
	protected Image image;
	protected String displayString;
	protected IContextInformation contextInformation;
	protected String additionalProposalInfo;

	protected ModuleNode moduleNode;

	protected GroovyProposal() {
	}

	/**
	 * Creates a new completion proposal. All fields are initialized based on
	 * the provided information.
	 * 
	 * @param replacementString
	 *            the actual string to be inserted into the document
	 * @param replacementOffset
	 *            the offset of the text to be replaced
	 * @param replacementLength
	 *            the length of the text to be replaced
	 * @param cursorPosition
	 *            the position of the cursor following the insert relative to
	 *            replacementOffset
	 * @param image
	 *            the image to display for this proposal
	 * @param displayString
	 *            the string to be displayed for the proposal
	 * @param contextInformation
	 *            the context information associated with this proposal
	 * @param additionalProposalInfo
	 *            the additional information associated with this proposal
	 */
	public GroovyProposal(String replacementString, int replacementOffset,
			int replacementLength, int cursorPosition, Image image,
			String displayString, IContextInformation contextInformation,
			String additionalProposalInfo) {
		Assert.isNotNull(replacementString);
		Assert.isTrue(replacementOffset >= 0);
		Assert.isTrue(replacementLength >= 0);
		Assert.isTrue(cursorPosition >= 0);

		this.replacementString = replacementString;
		this.replacementOffset = replacementOffset;
		this.replacementLength = replacementLength;
		this.cursorPosition = cursorPosition;
		this.image = image;
		this.displayString = displayString;
		this.contextInformation = contextInformation;
		this.additionalProposalInfo = additionalProposalInfo;
	}

	public void apply(IDocument document) {
		try {
			document.replace(replacementOffset, replacementLength,
					replacementString);
		} catch (BadLocationException x) {
			// ignore
		}
	}

	public String getAdditionalProposalInfo() {
		return additionalProposalInfo;
	}

	public IContextInformation getContextInformation() {
		return contextInformation;
	}

	public String getDisplayString() {
		if (displayString != null)
			return displayString;
		return replacementString;
	}

	public Image getImage() {
		return image;
	}

	public Point getSelection(IDocument document) {
		return new Point(replacementOffset + cursorPosition, 0);
	}
}

