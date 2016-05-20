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
import org.ebayopensource.dsf.jst.IJstNode;
import org.ebayopensource.dsf.jst.IJstProperty;
import org.ebayopensource.dsf.jst.declaration.JstModifiers;
import org.ebayopensource.vjet.eclipse.ui.VjetUIPlugin;
import org.ebayopensource.vjo.tool.codecompletion.IVjoCcProposalData;
import org.ebayopensource.vjo.tool.codecompletion.VjoCcCtx;
import org.ebayopensource.vjo.tool.codecompletion.proposaldata.AbstractVjoProposalPresenter;
import org.ebayopensource.vjo.tool.codecompletion.proposaldata.integration.IVjoEclipseCompletionProposal;
import org.ebayopensource.vjo.tool.codecompletion.proposaldata.integration.IVjoEclipseContextInformationFactory;
import org.ebayopensource.vjo.tool.codecompletion.proposaldata.integration.IVjoMethodCompletionProposalFactory;
import org.ebayopensource.vjo.tool.codecompletion.proposaldata.integration.IVjoProposalAditionalInfoGeneratorAdapter;
import org.ebayopensource.vjo.tool.codecompletion.proposaldata.integration.IVjoProposalLabelProviderAdapter;
import org.ebayopensource.vjo.tool.codecompletion.proposaldata.integration.IVjoProposalLabelUtilAdapter;
import org.ebayopensource.vjo.tool.codecompletion.proposaldata.integration.IVjoTypeCompletionProposalFactory;
import org.ebayopensource.vjo.tool.codecompletion.proposaldata.integration.VjoProposalEclipsePresenterAdapter;
import org.eclipse.dltk.mod.javascript.internal.ui.text.JsPreferenceInterpreter;
import org.eclipse.dltk.mod.ui.templates.IScriptTemplateIndenter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * 
 * 
 * 
 * TODO, should be called before VjoCompetionEngine String token =
 * completion.getToken(); this.setSourceRange(position - token.length(),
 * position);
 */
public class VjoProposalEclipsePresenter extends AbstractVjoProposalPresenter {

	private static final IVjoProposalLabelUtilAdapter<IDocument, IScriptTemplateIndenter> LABEL_UTIL = new IVjoProposalLabelUtilAdapter<IDocument, IScriptTemplateIndenter>() {

		@Override
		public String calculateIndent(IDocument document, int offset) {
			return VjoProposalLabelUtil.calculateIndent(document, offset);
		}

		@Override
		public String evaluateIndent(String str, IDocument document,
				int replaceOffset) {
			return VjoProposalLabelUtil.evaluateIndent(str, document,
					replaceOffset);
		}

		@Override
		public int getDltkModifyFlag(JstModifiers modifiers) {
			return VjoProposalLabelUtil.getDltkModifyFlag(modifiers);
		}

		@Override
		public IScriptTemplateIndenter getIndenter() {
			return VjoProposalLabelUtil.getIndenter();
		}

		@Override
		public JstModifiers getModifiers(IJstNode node) {
			return VjoProposalLabelUtil.getModifiers(node);
		}

		@Override
		public int getVjoModifierForImage(JstModifiers modifiers) {
			return VjoProposalLabelUtil.getVjoModifierForImage(modifiers);
		}

		@Override
		public int translateModifers(int jstModifer) {
			return VjoProposalLabelUtil.translateModifers(jstModifer);
		}
	};
	private static final IVjoProposalLabelProviderAdapter<ImageDescriptor, Image> LABEL_PROVIDER = new IVjoProposalLabelProviderAdapter<ImageDescriptor, Image>() {

		@Override
		public Image getMethodImage(int flags) {
			return VjoProposalLabelProvider.getMethodImage(flags);
		}

		@Override
		public Image getScriptImage(IJstNode node) {
			return VjoProposalLabelProvider.getScriptImage(node);
		}

		@Override
		public ImageDescriptor getScriptImageDescriptor(IJstNode node) {
			return VjoProposalLabelProvider.getScriptImageDescriptor(node);
		}

		@Override
		public Image getTypeImageDescriptor(int flags, boolean useLightIcons) {
			return VjoProposalLabelProvider.getTypeImageDescriptor(flags,
					useLightIcons);
		}
	};
	private static final IVjoProposalAditionalInfoGeneratorAdapter ADDITIONAL_INFO_GENERATOR = new IVjoProposalAditionalInfoGeneratorAdapter() {

		@Override
		public boolean isBrowserNoneNode(IJstNode node) {
			return VjoProposalAditionalInfoGenerator.isBrowserNoneNode(node);
		}

		@Override
		public String getModifierListStr(JstModifiers jstModifiers) {
			return VjoProposalAditionalInfoGenerator
					.getModifierListStr(jstModifiers);
		}

		@Override
		public String getElementBriefDesc(IJstMethod method) {
			return VjoProposalAditionalInfoGenerator
					.getElementBriefDesc(method);
		}

		@Override
		public String getElementBriefDesc(IJstProperty property) {
			return VjoProposalAditionalInfoGenerator
					.getElementBriefDesc(property);
		}

		@Override
		public String getAdditionalPropesalInfo(IJstNode node) {
			return VjoProposalAditionalInfoGenerator
					.getAdditionalPropesalInfo(node);
		}
	};

	private static final IVjoEclipseContextInformationFactory<Image, IContextInformation> CONTEXT_INFORMATION_FACTORY = new IVjoEclipseContextInformationFactory<Image, IContextInformation>() {

		@Override
		public IContextInformation buildContextInfo(
				String contextDisplayString, String informationDisplayString) {
			return new ContextInformation(contextDisplayString,
					informationDisplayString);
		}

		@Override
		public IContextInformation buildContextInfo(Image image,
				String contextDisplayString, String informationDisplayString) {
			return new ContextInformation(image, contextDisplayString,
					informationDisplayString);
		}
	};

	private static final IVjoMethodCompletionProposalFactory<Image, IContextInformation> METHOD_COMPLETION_PROPOSAL_FACTORY = new IVjoMethodCompletionProposalFactory<Image, IContextInformation>() {

		@Override
		public IVjoEclipseCompletionProposal<Image, IContextInformation> createMethodCompletionProposal(
				String replacementString, int replacementOffset,
				int replacementLength, int cursorPosition) {
			return new MethodCompletionProposal(replacementString,
					replacementOffset, replacementLength, cursorPosition);
		}

		@Override
		public IVjoEclipseCompletionProposal<Image, IContextInformation> createMethodCompletionProposal(
				String replacementString, int replacementOffset,
				int replacementLength, int cursorPosition, Image image,
				String displayString, IContextInformation contextInformation,
				String additionalProposalInfo, IJstMethod method) {
			return new MethodCompletionProposal(replacementString,
					replacementOffset, replacementLength, cursorPosition,
					image, displayString, contextInformation,
					additionalProposalInfo, method);
		}
	};

	private static final IVjoTypeCompletionProposalFactory<Image, IContextInformation> TYPE_COMPLETION_PROPOSAL_FACTORY = new IVjoTypeCompletionProposalFactory<Image, IContextInformation>() {

		@Override
		public IVjoEclipseCompletionProposal<Image, IContextInformation> createTypeCompletionProposal(
				String replacementString, int replacementOffset,
				int replacementLength, int cursorPosition, int needsPosition,
				String typeName) {
			return new TypeCompletionProposal(replacementString,
					replacementOffset, replacementLength, cursorPosition,
					needsPosition, typeName);
		}

		@Override
		public IVjoEclipseCompletionProposal<Image, IContextInformation> createTypeCompletionProposal(
				String replacementString, int replacementOffset,
				int replacementLength, int cursorPosition, int needsPosition,
				String typeName, Image image, String displayString,
				IContextInformation contextInformation,
				String additionalProposalInfo) {
			return new TypeCompletionProposal(replacementString,
					replacementOffset, replacementLength, cursorPosition,
					needsPosition, typeName, image, displayString,
					contextInformation, additionalProposalInfo);
		}
	};
	
	private final VjoProposalEclipsePresenterAdapter<IDocument, Point, ImageDescriptor, Image, IContextInformation, IScriptTemplateIndenter> m_presenterAdapter;
	
	public VjoProposalEclipsePresenter(VjoCcCtx ctx, int offset,
			Point selectedRange, IDocument document) {
		m_presenterAdapter = new VjoProposalEclipsePresenterAdapter<IDocument, Point, ImageDescriptor, Image, IContextInformation, IScriptTemplateIndenter>(ctx, 
				offset, 
				selectedRange, 
				document,
				ADDITIONAL_INFO_GENERATOR,
				LABEL_PROVIDER,
				LABEL_UTIL,
				CONTEXT_INFORMATION_FACTORY,
				METHOD_COMPLETION_PROPOSAL_FACTORY,
				TYPE_COMPLETION_PROPOSAL_FACTORY);
		
		init();
	}

	protected void init() {
		String token = m_presenterAdapter.getVjoCcCtx().getActingToken();
		if (token == null) {
			token = "";
		}
		// common token
		String replacedToken = m_presenterAdapter.getVjoCcCtx().getReplacedToken();
		if (m_presenterAdapter.getDocument() != null) {
			m_presenterAdapter.setLineSeperator(TextUtilities.getDefaultLineDelimiter(m_presenterAdapter.getDocument()));
		}
		if (m_presenterAdapter.getSelectedRange() == null || m_presenterAdapter.getSelectedRange().y == 0) {
			m_presenterAdapter.setReplaceLength(replacedToken.length());
		} else {
			int temp = m_presenterAdapter.getSelectedRange().x - m_presenterAdapter.getOffset() + m_presenterAdapter.getSelectedRange().y;
			m_presenterAdapter.setReplaceLength(replacedToken.length() + temp);
		}
		m_presenterAdapter.setReplaceOffset(m_presenterAdapter.getOffset() - token.length());
		// package token
		String replacedPToken = m_presenterAdapter.getVjoCcCtx().getReplacedPackageToken();
		if (m_presenterAdapter.getSelectedRange() == null || m_presenterAdapter.getSelectedRange().y == 0) {
			m_presenterAdapter.setReplacePLength(replacedPToken.length());
		} else {
			int temp = m_presenterAdapter.getSelectedRange().x - m_presenterAdapter.getOffset() + m_presenterAdapter.getSelectedRange().y;
			m_presenterAdapter.setReplacePLength(replacedPToken.length() + temp);
		}
		m_presenterAdapter.setReplacePOffset(m_presenterAdapter.getOffset() - replacedPToken.length());
	
		JsPreferenceInterpreter prefs = new JsPreferenceInterpreter(
				VjetUIPlugin.getDefault().getPreferenceStore());
		
		m_presenterAdapter.setIndent(prefs.getIndent());
	
	}
	
	public List<ICompletionProposal> doPresenter(List<IVjoCcProposalData> datas) {

		final List<? extends IVjoEclipseCompletionProposal<Image, IContextInformation>> prepares = m_presenterAdapter.doPresenter(datas);
		final List<ICompletionProposal> adaptedProposals = new ArrayList<ICompletionProposal>(prepares.size()); 
		
		for(IVjoEclipseCompletionProposal<Image, IContextInformation> proposal : prepares){
			if(proposal instanceof MethodCompletionProposal){
				adaptedProposals.add((MethodCompletionProposal)proposal);
			}
			else{
				final ICompletionProposal adaptedProposal = 
					new CompletionProposal(proposal.getReplacementString(), 
							proposal.getReplacementOffset(), proposal.getReplacementLength(), proposal.getCursorPosition(), proposal.getImage(), proposal.getDisplayString(), proposal.getContextInformation(), proposal.getAdditionalProposalInfo(), null);
				adaptedProposals.add(adaptedProposal);
			}
		}
		
		return adaptedProposals;
	}
}

