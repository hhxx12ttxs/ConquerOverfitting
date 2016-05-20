/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.vjet.eclipse.internal.ui.text.completion;

import java.util.ArrayList;
import java.util.List;

import org.ebayopensource.dsf.jst.IJstMethod;
import org.ebayopensource.vjet.eclipse.internal.ui.editor.EditorHighlightingSynchronizer;
import org.ebayopensource.vjet.eclipse.internal.ui.editor.VjoEditor;
import org.ebayopensource.vjet.eclipse.ui.VjetUIPlugin;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;

public class MethodCompletionProposal extends CompletionProposal {
	private IRegion fSelectedRegion = null;
	public MethodCompletionProposal(String replacementString,
			int replacementOffset, int replacementLength, int cursorPosition) {
		super(replacementString, replacementOffset, replacementLength, cursorPosition);
	}

	public MethodCompletionProposal(String replacementString,
			int replacementOffset, int replacementLength, int cursorPosition,
			Image image, String displayString,
			IContextInformation contextInformation,
			String additionalProposalInfo, IJstMethod method) {
		super(replacementString, replacementOffset, replacementLength, cursorPosition,
				image, displayString, contextInformation, additionalProposalInfo, method);
	}
	
	@Override
	public Point getSelection(IDocument document) {
		if (fSelectedRegion == null) {
			fSelectedRegion = getSelectedRegion(document);
		}
		if (fSelectedRegion == null) {
			return new Point(getReplacementOffset(), 0);
		}

		return new Point(fSelectedRegion.getOffset(), fSelectedRegion.getLength());
	}
	
	protected IRegion getSelectedRegion(IDocument document) {
		IRegion fSelectedRegion = null;
		int baseOffset= getReplacementOffset();
		String replacement= getReplacementString();
		int[][] argInfo = genArgInfo(replacement);
		if (argInfo != null && getTextViewer() != null) {
			try {
				LinkedModeModel model= new LinkedModeModel();
				for (int i= 0; i != argInfo.length; i++) {
					LinkedPositionGroup group= new LinkedPositionGroup();
					group.addPosition(new LinkedPosition(document, baseOffset + argInfo[i][0], argInfo[i][1], LinkedPositionGroup.NO_STOP));
					model.addGroup(group);
				}

				model.forceInstall();
				VjoEditor editor= getVjoEditor();
				if (editor != null) {
					model.addLinkingListener(new EditorHighlightingSynchronizer(editor));
				}

				LinkedModeUI ui= new EditorLinkedModeUI(model, getTextViewer());
				ui.setExitPosition(getTextViewer(), baseOffset + replacement.length(), 0, Integer.MAX_VALUE);
				ui.setExitPolicy(new ExitPolicy(')', document));
				ui.setDoContextInfo(true);
				ui.setCyclingMode(LinkedModeUI.CYCLE_WHEN_NO_PARENT);
				ui.enter();

				fSelectedRegion= ui.getSelectedRegion();

			} catch (BadLocationException e) {
				VjetUIPlugin.log(e);
			}
		} else {
			fSelectedRegion= new Region(baseOffset + replacement.length(), 0);
		}
		return fSelectedRegion;
	}
	

	@Override
	public int getContextInformationPosition() {
		if (fSelectedRegion != null){
			return fSelectedRegion.getOffset();
		} else {
			return super.getContextInformationPosition();
		}
	}

	private int[][] genArgInfo(String replaceString) {
		int begin = replaceString.indexOf("(");
		int end = replaceString.indexOf(")");
		if (end == -1 || begin == -1 || end < begin) {
			return null;
		}
		int offset = begin + 1;
		String argStr = replaceString.substring(offset, end);
		//Way 1
		List<Integer> list = new ArrayList<Integer>();
		int index = 0; 
		index = argStr.indexOf(",");
		while (index != -1) {
			list.add(index);
			index = argStr.indexOf(",", index  + 1);
		}
		int length = list.size();
		if (length == 0) {
			return new int[][]{{offset, argStr.length()}};
		} else {
			int[][] result = new int[length + 1][2];
			result[0][0] = offset;
			for (int i=0; i< length; i++) {
				int temp = list.get(i);
				result[i][1] = offset + temp - result[i][0];
				result[i+1][0] = offset + temp + 1; 
			}
			result[length][1] = offset + argStr.length() - result[length][0];
			return result;
			
		}
		
		//Way 2
//		String[] subs = argStr.split(",");
//		int[][] result  = new int[subs.length][2];
//		for (int i = 0; i<subs.length; i++) {
//			String sub = subs[i];
//			int begin1 = getBegin(sub);
//			offset = offset + begin1;
//			result[i][0] = offset;
//			result[i][1] = sub.trim().length();
//			offset = result[i][0] + result[i][1] + 1;
//		}
//		return result;
	}

	private int getBegin(String sub) {
		for (int i = 0; i<sub.length(); i++) {
			if (!Character.isWhitespace(sub.charAt(i))) {
				return i;
			}
		}
		return 0;
	}

}

