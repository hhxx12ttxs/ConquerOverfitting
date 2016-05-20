/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.vjo.tool.codecompletion.proposaldata.integration;

import java.util.List;

import org.eclipse.jdt.core.compiler.CharOperation;

import org.ebayopensource.dsf.jst.IJstMethod;
import org.ebayopensource.dsf.jst.IJstProperty;
import org.ebayopensource.dsf.jst.IJstType;
import org.ebayopensource.dsf.jst.declaration.JstArg;
import org.ebayopensource.vjo.tool.codecompletion.CodeCompletionUtils;
import org.ebayopensource.vjo.tool.codecompletion.StringUtils;

public class VjoEclipseCompletionProposalAdapter <IMAGE, CONTEXT_INFO>
	implements IVjoEclipseCompletionProposal<IMAGE, CONTEXT_INFO>{

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
	/** The additional info of this proposal. */
	protected String fAdditionalProposalInfo;
	
	/** The image to be displayed in the completion proposal popup. */
	protected IMAGE fImage;
	/** The context information of this proposal. */
	protected CONTEXT_INFO fContextInformation;

	protected static final char[] TRIGGER_METHOD = new char[] { ',', '.', ';' };
	protected static final char[] TRIGGER_METHOD_WITH_ARGS = new char[] { ',',
			';' };
	protected static final char[] TRIGGER_COMMON = new char[] {};

	/**
	 * The control creator.
	 */
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
	public VjoEclipseCompletionProposalAdapter(String replacementString, int replacementOffset,
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
	public VjoEclipseCompletionProposalAdapter(String replacementString, int replacementOffset,
			int replacementLength, int cursorPosition, IMAGE image,
			String displayString,
			CONTEXT_INFO contextInformation,
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
	 * @param displayString
	 *            the string to be displayed for the proposal
	 * @param additionalProposalInfo
	 *            the additional information associated with this proposal
	 */
	public VjoEclipseCompletionProposalAdapter(String replacementString, int replacementOffset,
			int replacementLength, int cursorPosition, IMAGE image,
			String displayString, CONTEXT_INFO contextInformation,
			String additionalProposalInfo, Object data) {
		assert replacementString != null;
		assert replacementOffset >= 0;
		assert replacementLength >= 0;
		assert cursorPosition >= 0;

		fReplacementString = replacementString;
		fReplacementOffset = replacementOffset;
		fReplacementLength = replacementLength;
		fCursorPosition = cursorPosition;
		fDisplayString = displayString;
		fImage = image;
		fContextInformation = contextInformation;
		fAdditionalProposalInfo = additionalProposalInfo;
		fData = data;
	}

	/*
	 * @see ICompletionProposal#getContextInformation()
	 */
	public CONTEXT_INFO getContextInformation() {
		return fContextInformation;
	}

	/*
	 * @see ICompletionProposal#getImage()
	 */
	public IMAGE getImage() {
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

	public int getContextInformationPosition() {
		return fReplacementOffset + fCursorPosition;
	}

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

}

