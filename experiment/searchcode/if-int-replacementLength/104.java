/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.vjet.eclipse.internal.ui.text.completion;

import java.util.List;

import org.ebayopensource.dsf.jst.IJstMethod;
import org.ebayopensource.dsf.jst.IJstProperty;
import org.ebayopensource.dsf.jst.IJstType;
import org.ebayopensource.dsf.jst.declaration.JstArg;
import org.ebayopensource.vjet.eclipse.internal.ui.editor.VjoEditor;
import org.ebayopensource.vjet.eclipse.ui.VjetUIPlugin;
import org.ebayopensource.vjo.tool.codecompletion.CodeCompletionUtils;
import org.ebayopensource.vjo.tool.codecompletion.StringUtils;
import org.ebayopensource.vjo.tool.codecompletion.proposaldata.integration.IVjoEclipseCompletionProposal;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dltk.mod.compiler.CharOperation;
import org.eclipse.dltk.mod.internal.ui.BrowserInformationControl;
import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRewriteTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI.ExitFlags;
import org.eclipse.jface.text.link.LinkedModeUI.IExitPolicy;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.IUndoManager;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;

public class CompletionProposal implements ICompletionProposal,
		ICompletionProposalExtension, ICompletionProposalExtension2,
		ICompletionProposalExtension3,
		IVjoEclipseCompletionProposal<Image, IContextInformation>{

	/** The string to be displayed in the completion proposal popup. */
	protected String fDisplayString;
	/** The replacement string. */
	protected String fReplacementString;
	/** The replacement offset. */
	protected int fReplacementOffset;
	/** The replacement length. */
	protected int fReplacementLength;
	/** The cursor position after this proposal has been applied. */
	protected int fCursorPosition;
	/** The image to be displayed in the completion proposal popup. */
	protected Image fImage;
	/** The context information of this proposal. */
	protected IContextInformation fContextInformation;
	/** The additional info of this proposal. */
	protected String fAdditionalProposalInfo;

	protected static final char[] TRIGGER_METHOD = new char[] { ',', '.', ';' };
	protected static final char[] TRIGGER_METHOD_WITH_ARGS = new char[] { ',',
			';' };
	protected static final char[] TRIGGER_COMMON = new char[] {};

	/**
	 * The control creator.
	 */
	private IInformationControlCreator fCreator;
	private ITextViewer fTextViewer;
	protected Object fData;

	/**
	 * Creates a new completion proposal based on the provided information. The
	 * replacement string is considered being the display string too. All
	 * remaining fields are set to <code>null</code>.
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
	 */
	public CompletionProposal(String replacementString, int replacementOffset,
			int replacementLength, int cursorPosition) {
		this(replacementString, replacementOffset, replacementLength,
				cursorPosition, null, null, null, null);
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
	public CompletionProposal(String replacementString, int replacementOffset,
			int replacementLength, int cursorPosition, Image image,
			String displayString, IContextInformation contextInformation,
			String additionalProposalInfo) {
		this(replacementString, replacementOffset, replacementLength,
				cursorPosition, image, displayString, contextInformation,
				additionalProposalInfo, null);
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
	public CompletionProposal(String replacementString, int replacementOffset,
			int replacementLength, int cursorPosition, Image image,
			String displayString, IContextInformation contextInformation,
			String additionalProposalInfo, Object data) {
		Assert.isNotNull(replacementString);
		Assert.isTrue(replacementOffset >= 0);
		Assert.isTrue(replacementLength >= 0);
		Assert.isTrue(cursorPosition >= 0);

		fReplacementString = replacementString;
		fReplacementOffset = replacementOffset;
		fReplacementLength = replacementLength;
		fCursorPosition = cursorPosition;
		fImage = image;
		fDisplayString = displayString;
		fContextInformation = contextInformation;
		fAdditionalProposalInfo = additionalProposalInfo;
		fData = data;
	}

	/*
	 * @see ICompletionProposal#apply(IDocument)
	 */
	public void apply(IDocument document) {
		try {
			preApply(document);
			document.replace(fReplacementOffset, fReplacementLength,
					fReplacementString);
			postApply(document);
		} catch (BadLocationException x) {
			// ignore
		}
	}

	/**
	 * Will be called before apply operation.
	 * 
	 * @param document
	 */
	protected void preApply(IDocument document) {

	}

	/*
	 * @see ICompletionProposal#getSelection(IDocument)
	 */
	public Point getSelection(IDocument document) {
		return new Point(fReplacementOffset + fCursorPosition, 0);
	}

	/*
	 * @see ICompletionProposal#getContextInformation()
	 */
	public IContextInformation getContextInformation() {
		return fContextInformation;
	}

	/*
	 * @see ICompletionProposal#getImage()
	 */
	public Image getImage() {
		return fImage;
	}

	/*
	 * @see ICompletionProposal#getDisplayString()
	 */
	public String getDisplayString() {
		if (fDisplayString != null)
			return fDisplayString;
		return fReplacementString;
	}

	/*
	 * @see ICompletionProposal#getAdditionalProposalInfo()
	 */
	public String getAdditionalProposalInfo() {
		return fAdditionalProposalInfo;
	}

	public void apply(ITextViewer viewer, char trigger, int stateMask,
			int offset) {
		this.fTextViewer = viewer;
		String strigger = String.valueOf(trigger);
		if (!StringUtils.isBlankOrEmpty(strigger)) {
			fReplacementString = fReplacementString + strigger;
			fCursorPosition = fCursorPosition + 1;
		}
		// Add annotation at the end of the expression
		if (trigger == ';') {
			appendComment();
		}
		apply(viewer.getDocument());

	}

	protected void appendComment() {
		String typeComment = "";
		if (fData instanceof IJstProperty) {
			IJstProperty property = (IJstProperty) fData;
			IJstType type = property.getType();
			if (type != null) {
				typeComment = type.getSimpleName();
			}
		} else if (fData instanceof IJstMethod) {
			IJstMethod method = (IJstMethod) fData;
			if (CodeCompletionUtils.hasReturnValue(method)) {
				typeComment = CodeCompletionUtils.getReturnType(method);
			}
		}
		if (!StringUtils.isBlankOrEmpty(typeComment)) {
			typeComment = "//<" + typeComment;
			fReplacementString = fReplacementString + "" + typeComment;
			fCursorPosition = fCursorPosition + typeComment.length();
		}
	}

	public void selected(ITextViewer viewer, boolean smartToggle) {

	}

	public void unselected(ITextViewer viewer) {

	}

	public boolean validate(IDocument document, int offset, DocumentEvent event) {
		if (offset < fReplacementOffset)
			return false;

		boolean validated = isPrefix(getPrefix(document, offset),
				getReplacementString());
		if (!validated) {
			validated = isPrefix(getPrefix(document, offset),
					getDisplayString());
		}

		if (validated && event != null) {
			// adapt replacement range to document change
			int delta = (event.fText == null ? 0 : event.fText.length())
					- event.fLength;
			final int newLength = Math.max(fReplacementLength + delta, 0);
			this.fReplacementLength = newLength;
		}

		return validated;
	}

	/**
	 * Returns the text in <code>document</code> from
	 * {@link #getReplacementOffset()} to <code>offset</code>. Returns the
	 * empty string if <code>offset</code> is before the replacement offset or
	 * if an exception occurs when accessing the document.
	 * 
	 * 
	 */
	protected String getPrefix(IDocument document, int offset) {
		try {
			int length = offset - fReplacementOffset;
			if (length > 0)
				return document.get(fReplacementOffset, length);
		} catch (BadLocationException x) {
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Case insensitive comparison of <code>prefix</code> with the start of
	 * <code>string</code>. Returns <code>false</code> if
	 * <code>prefix</code> is longer than <code>string</code>
	 * 
	 * 
	 */
	protected boolean isPrefix(String prefix, String string) {
		if (prefix == null || string == null
				|| prefix.length() > string.length())
			return false;
		String start = string.substring(0, prefix.length());
		return start.equalsIgnoreCase(prefix)
				|| CharOperation.camelCaseMatch(prefix.toCharArray(), string
						.toCharArray());
	}

	public IInformationControlCreator getInformationControlCreator() {
		if (fCreator == null) {
			fCreator = new AbstractReusableInformationControlCreator() {

				public IInformationControl doCreateInformationControl(
						Shell parent) {
					return new BrowserInformationControl(parent, SWT.NO_TRIM
							| SWT.TOOL, SWT.NONE, null);
				}
			};
		}
		return fCreator;
		// return null;
	}

	public int getPrefixCompletionStart(IDocument document, int completionOffset) {
		return fReplacementOffset;
	}

	public CharSequence getPrefixCompletionText(IDocument document,
			int completionOffset) {
		return null;
	}

	protected void postApply(IDocument document) {

	}

	public String getReplacementString() {
		return fReplacementString;
	}

	public int getReplacementOffset() {
		return fReplacementOffset;
	}

	public int getReplacementLength() {
		return fReplacementLength;
	}

	public int getCursorPosition() {
		return fCursorPosition;
	}

	protected IInformationControlCreator getCreator() {
		return fCreator;
	}

	protected static final class ExitPolicy implements IExitPolicy {

		final char fExitCharacter;
		private final IDocument fDocument;

		public ExitPolicy(char exitCharacter, IDocument document) {
			fExitCharacter = exitCharacter;
			fDocument = document;
		}

		public ExitFlags doExit(LinkedModeModel environment, VerifyEvent event,
				int offset, int length) {

			if (event.character == fExitCharacter) {
				if (environment.anyPositionContains(offset))
					return new ExitFlags(ILinkedModeListener.UPDATE_CARET,
							false);
				else
					return new ExitFlags(ILinkedModeListener.UPDATE_CARET, true);
			}

			switch (event.character) {
			case ';':
				return new ExitFlags(ILinkedModeListener.NONE, true);
			case SWT.CR:
				// when entering an anonymous class as a parameter, we don't
				// want
				// to jump after the parenthesis when return is pressed
				if (offset > 0) {
					try {
						if (fDocument.getChar(offset - 1) == '{')
							return new ExitFlags(ILinkedModeListener.EXIT_ALL,
									true);
					} catch (BadLocationException e) {
					}
				}
				return null;
			default:
				return null;
			}
		}

	}

	protected VjoEditor getVjoEditor() {
		return VjetUIPlugin.getVjoEditor();
	}

	protected ITextViewer getTextViewer() {
		return fTextViewer;
	}

	protected void performChange(IEditorPart activeEditor, IDocument document,
			Change change) throws CoreException {
		if (change == null) {
			return;
		}
		IRewriteTarget rewriteTarget = null;
		try {
			if (change != null) {
				if (document != null) {
					LinkedModeModel.closeAllModels(document);
				}
				if (activeEditor != null) {
					rewriteTarget = (IRewriteTarget) activeEditor
							.getAdapter(IRewriteTarget.class);
					if (rewriteTarget != null) {
						rewriteTarget.beginCompoundChange();
					}
				}

				change.initializeValidationData(new NullProgressMonitor());
				IUndoManager manager = RefactoringCore.getUndoManager();
				manager.aboutToPerformChange(change);
				org.eclipse.ltk.core.refactoring.Change undoChange = change
						.perform(new NullProgressMonitor());
				manager.changePerformed(change, true);
				if (undoChange != null) {
					undoChange
							.initializeValidationData(new NullProgressMonitor());
					manager.addUndo(getReplacementString(), undoChange);
				}
			}
		} finally {
			if (rewriteTarget != null) {
				rewriteTarget.endCompoundChange();
			}

			if (change != null) {
				change.dispose();
			}
		}
	}

	protected TextChange createTextChange(IDocument document)
			throws CoreException {
		return null;
	}

	@Override
	public void apply(IDocument document, char trigger, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getContextInformationPosition() {
		return fReplacementOffset + fCursorPosition;
	}

	@Override
	public char[] getTriggerCharacters() {
		if (fData == null) {
			return TRIGGER_COMMON;
		} else if (fData instanceof IJstProperty) {
			return TRIGGER_METHOD;
		} else if (fData instanceof IJstMethod) {
			IJstMethod method = (IJstMethod) fData;
			List<JstArg> args = method.getArgs();
			if (args == null || args.isEmpty()) {
				return TRIGGER_METHOD;
			} else {
				return TRIGGER_METHOD_WITH_ARGS;
			}
		} else {
			return TRIGGER_COMMON;
		}
	}

	@Override
	public boolean isValidFor(IDocument document, int offset) {
		return false;
	}

}

