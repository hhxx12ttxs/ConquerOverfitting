/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.vjet.eclipse.internal.ui.text.completion;

import org.ebayopensource.vjet.eclipse.ui.VjetUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

public class TypeCompletionProposal extends CompletionProposal {
	private int needsPosition;
	private String typeName;

	public TypeCompletionProposal(String replacementString,
			int replacementOffset, int replacementLength, int cursorPosition, int needsPosition, String typeName) {
		super(replacementString, replacementOffset, replacementLength, cursorPosition);
		this.needsPosition = needsPosition;
		this.typeName = typeName;
	}

	public TypeCompletionProposal(String replacementString,
			int replacementOffset, int replacementLength, int cursorPosition, int needsPosition, String typeName,
			Image image, String displayString,
			IContextInformation contextInformation,
			String additionalProposalInfo) {
		super(replacementString, replacementOffset, replacementLength, cursorPosition,
				image, displayString, contextInformation, additionalProposalInfo);
		this.needsPosition = needsPosition;
		this.typeName = typeName;
	}
	
	@Override
	protected void postApply(IDocument document) {
		super.postApply(document);
		if (needsPosition == -1) {
			return;
		}
		try {
			performChange(getVjoEditor(), document, createTextChange(document));
		} catch (CoreException e) {
			VjetUIPlugin.log(e);
		}
	}
	
	
	protected TextChange createTextChange(IDocument document) throws CoreException {
		String name= getReplacementString();
		TextChange change;
		change= new DocumentChange(name, document);
		TextEdit rootEdit= new MultiTextEdit();
		change.setEdit(rootEdit);

		InsertEdit edit= new InsertEdit(needsPosition, typeName);
		rootEdit.addChild(edit);
		return change;
	}
		
	

}

