/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
package org.ebayopensource.vjet.eclipse.internal.ui.text.completion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ebayopensource.dsf.jst.BaseJstNode;
import org.ebayopensource.dsf.jst.IJstMethod;
import org.ebayopensource.dsf.jst.IJstProperty;
import org.ebayopensource.dsf.jst.IJstType;
import org.ebayopensource.dsf.jst.declaration.JstArg;
import org.ebayopensource.dsf.jst.declaration.JstModifiers;
import org.ebayopensource.vjet.eclipse.ui.VjetUIPlugin;
import org.ebayopensource.vjo.tool.typespace.TypeSpaceMgr;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dltk.mod.core.DLTKCore;
import org.eclipse.dltk.mod.core.IModelElement;
import org.eclipse.dltk.mod.internal.core.Member;
import org.eclipse.dltk.mod.internal.core.VjoSourceModule;
import org.eclipse.dltk.mod.internal.core.VjoSourceType;
import org.eclipse.dltk.mod.javascript.scriptdoc.JavaDoc2HTMLTextReader;
import org.eclipse.dltk.mod.ui.text.completion.HTMLPrinter;
import org.eclipse.dltk.mod.ui.text.completion.ScriptCompletionProposal;
import org.eclipse.dltk.mod.ui.text.completion.TypeProposalInfo;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

public class VjoCompletionProposal extends ScriptCompletionProposal implements
		ICompletionProposalExtension4 {
	private boolean m_isAutoInsert;

	private ImportRewriter needsRewriter = new ImportRewriter();

	private Object extraInfo;

	public VjoCompletionProposal(String replacementString,
			int replacementOffset, int replacementLength, Image image,
			String displayString, int relevance) {
		super(replacementString, replacementOffset, replacementLength, image,
				displayString, relevance);
		setContextInformation(null);
		// ProposalInfo proposalInfo = new ProposalInfo(null);
		// this.setProposalInfo(proposalInfo);
	}

	public VjoCompletionProposal(String replacementString,
			int replacementOffset, int replacementLength, Image image,
			String displayString, int relevance, boolean isInDoc) {
		super(replacementString, replacementOffset, replacementLength, image,
				displayString, relevance, isInDoc);
		setContextInformation(null);
		// ProposalInfo proposalInfo = new ProposalInfo(null);
		// this.setProposalInfo(proposalInfo);
	}

	@Override
	protected boolean isSmartTrigger(char trigger) {
		if (trigger == '$') {
			return true;
		}
		return false;
	}

	@Override
	protected boolean isValidPrefix(String prefix) {
		if ((getProposalInfo() instanceof TypeProposalInfo)
				&& prefix.contains(".")) {
			return isPrefix(prefix.substring(prefix.lastIndexOf('.') + 1),
					getDisplayString());
		}
		return isPrefix(prefix, getDisplayString());
	}

	public void setAutoInsert(boolean isAutoInsert) {
		m_isAutoInsert = isAutoInsert;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension4#isAutoInsertable()
	 */
	public boolean isAutoInsertable() {
		return m_isAutoInsert;
	}

	@Override
	public Object getAdditionalProposalInfo(IProgressMonitor monitor) {

		IModelElement element = getModelElement();

		String info = getAdditionalPropesalInfo(element, getCSSStyles());

		return info;
	}

	private static final String PERS_FOLDER = "org.ebayopensource.vjet.eclipse.ui"; //$NON-NLS-1$
	private static final String PERS_FILENAME = "additionalCompletionTemplate.html"; //$NON-NLS-1$

	/**
	 * @param baseUrl
	 * @param create
	 */
	private static void generateTempFolder(URL baseUrl, boolean create) {
		if (baseUrl == null) {
			return;
		}

		try {
			// make sure the directory exists
			URL url = new URL(baseUrl, PERS_FOLDER);
			File dir = new File(url.getFile());
			if (!dir.exists() && create) {
				dir.mkdir();
			}

		} catch (IOException e) {
		}
	}

	public static String getAdditionalPropesalInfo(IModelElement element,
			String css) {
		String info = null;

		if (element != null) {
			VjoSourceModule module = null;
			if (element instanceof Member) {
				try {
					if (element.getParent() instanceof VjoSourceModule) {
						module = (VjoSourceModule) element.getParent();
					} else if (element.getParent().getParent() instanceof VjoSourceModule) {
						module = (VjoSourceModule) element.getParent()
								.getParent();
					}

				} catch (Exception e) {
					DLTKCore.warn(e.toString(), e);
					return null;
				}
			}
			if (module == null) {
				return info;
			}
			String name = element.getElementName();
			TypeSpaceMgr mgr = TypeSpaceMgr.getInstance();
			IJstType type = mgr.findType(module.getTypeName());
			if (type == null) {
				return "";
			}
			IJstProperty property = type.getProperty(name);
			IJstMethod method = type.getMethod(name);

			List allSupportedExplorers = new ArrayList();

			String briefInfo = "";
			if (property != null && property.getDoc() != null) {
				info = property.getDoc().getComment();
				briefInfo = getElementBriefDesc(property);
				fillAllSupportedExplorer(property, allSupportedExplorers);
			} else {
				if ((method != null) && (method.getDoc() != null)) {
					info = method.getDoc().getComment();
					briefInfo = getElementBriefDesc(method);
					fillAllSupportedExplorer(method, allSupportedExplorers);
				}
			}

			if (info != null) {

				try {
					copyHtmlTemplateFromBundleToTemp();
					copyIconsFromBundleToTemp();

					// Replace the invalid character.
					info = info.replace("/", "");
					info = info.replace("*", "");

					Location configLoc = Platform.getConfigurationLocation();

					URL url;
					File persFile = null;
					try {
						url = new URL(configLoc.getURL(), PERS_FOLDER);
						File dir = new File(url.getFile());

						url = new URL(dir.toURL(), PERS_FILENAME);
						persFile = new File(url.getFile());

						if (persFile != null) {
							FileReader readerSuc = new FileReader(persFile);
							JavaDoc2HTMLTextReader reader2 = new JavaDoc2HTMLTextReader(
									readerSuc);
							String wholeInfo = getString(reader2);

							wholeInfo = wholeInfo.replace(
									"<%=browserSupported%>",
									getSupportedTypesStirng(
											allSupportedExplorers,
											"BrowserType."));

							String domLevel = getSupportedTypesStirng(
									allSupportedExplorers, "DomLevel.");
							if (domLevel.length() > 0) {
								wholeInfo = wholeInfo.replace("<%=DOMLevel%>",
										"<b>DOM level</b><br>"
												+ getSupportedTypesStirng(
														allSupportedExplorers,
														"DomLevel.") + "<br>");
							}
							wholeInfo = wholeInfo
									.replace(
											"<%=MinJSVersion%>",
											translateCharacterToNumber(getSupportedTypesStirng(
													allSupportedExplorers,
													"JsVersion.")));
							wholeInfo = wholeInfo.replace("<%=briefInfo%>",
									briefInfo);
							wholeInfo = wholeInfo.replace("<%=images%>",
									getIcon(allSupportedExplorers));
							wholeInfo = wholeInfo.replace("<%=description%>",
									info);

							// Add the css and HTML tags at the beginning and
							// end.
							StringBuffer buffer = new StringBuffer();
							HTMLPrinter.insertPageProlog(buffer, 0, css);
							buffer.append(wholeInfo);
							HTMLPrinter.addPageEpilog(buffer);
							wholeInfo = buffer.toString();

							return wholeInfo;
						}
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return info;
	}

	private static String getSupportedTypesStirng(List supportedTypes,
			String type) {
		String imageLine = "";

		String comma = "";
		int i = 0;

		for (Object oneType : supportedTypes) {
			if (((String) oneType).startsWith(type)) {
				if (i != 0) {
					comma = ", ";
				}
				imageLine += comma
						+ ((String) oneType).substring(type.length());
				i++;
			}

		}
		boolean noBrowserInfo = noBrowserInfo(supportedTypes);
		if (noBrowserInfo && "BrowserType.".equals(type)) {
			//imageLine = "FIREFOX,IE,OPERA,SAFARI,CHROME";
		}

		return imageLine;
	}

	/**
	 * @param supportedTypes
	 * @return
	 */
	private static String getIcon(List supportedTypes) {
		String imageLine = "";

		String path = "";
		try {
			URL url = new URL(Platform.getConfigurationLocation().getURL(),
					PERS_FOLDER);
			path = url.getPath();
			path = path.substring(1);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		boolean noBrowserInfo = noBrowserInfo(supportedTypes);

		if (getDescOfSupport(supportedTypes, "FIREFOX").trim().length() > 0
				|| noBrowserInfo) {
			imageLine += "<img aligh=\"right\" src=\""
					+ path
					+ "/firefox.gif\" height=\"25\" width=\"25\" border=\"0\" title=\""
					+ getDescOfSupport(supportedTypes, "MOZILLA") + "\">";
		}
		if (getDescOfSupport(supportedTypes, "IE").trim().length() > 0
				|| noBrowserInfo) {
			imageLine += "<img aligh=\"right\" src=\""
					+ path
					+ "/ie.gif\" height=\"25\" width=\"25\" border=\"0\" title=\""
					+ getDescOfSupport(supportedTypes, "IE") + "\">";
		}
		if (getDescOfSupport(supportedTypes, "OPERA").trim().length() > 0
				|| noBrowserInfo) {
			imageLine += "<img aligh=\"right\" src=\""
					+ path
					+ "/opera.gif\" height=\"25\" width=\"25\" border=\"0\" title=\""
					+ getDescOfSupport(supportedTypes, "OPERA") + "\">";
		}
		if (getDescOfSupport(supportedTypes, "SAFARI").trim().length() > 0
				|| noBrowserInfo) {
			imageLine += "<img aligh=\"right\" src=\""
					+ path
					+ "/safari.gif\" height=\"25\" width=\"25\" border=\"0\" title=\""
					+ getDescOfSupport(supportedTypes, "SAFARI") + "\">";
		}
		if (getDescOfSupport(supportedTypes, "CHROME").trim().length() > 0
				|| noBrowserInfo) {
			imageLine += "<img aligh=\"right\" src=\""
					+ path
					+ "/chrome.gif\" height=\"25\" width=\"25\" border=\"0\" title=\""
					+ getDescOfSupport(supportedTypes, "CHROME") + "\">";
		}

		return imageLine;
	}

	/**
	 * @param supportedTypes
	 * @return
	 */
	private static boolean noBrowserInfo(List supportedTypes) {
		boolean noBrowserInfo = true;
		for (Object type : supportedTypes) {
			if (((String) type).contains("BrowserType.")) {
				noBrowserInfo = false;
				break;
			}
		}
		return noBrowserInfo;
	}

	/**
	 * @param jstElement
	 * @return
	 */
	private static List fillAllSupportedExplorer(Object jstElement,
			List allSupportedExplorers) {

		if (jstElement instanceof IJstProperty) {
			List annotations = ((IJstProperty) jstElement).getAnnotations();

			getExplorerInfoFromAnnotation(allSupportedExplorers, annotations);

			if (annotations.size() == 0) {
				fillAllSupportedExplorer(((IJstProperty) jstElement)
						.getOwnerType(), allSupportedExplorers);
			}
		} else if (jstElement instanceof IJstMethod) {
			List annotations = ((IJstMethod) jstElement).getAnnotations();

			getExplorerInfoFromAnnotation(allSupportedExplorers, annotations);

			if (annotations.size() == 0) {
				fillAllSupportedExplorer(((IJstMethod) jstElement)
						.getOwnerType(), allSupportedExplorers);
			}
		} else if (jstElement instanceof IJstType) {
			List annotations = ((IJstType) jstElement).getAnnotations();
			getExplorerInfoFromAnnotation(allSupportedExplorers, annotations);
		}
		return allSupportedExplorers;
	}

	/**
	 * @param allSupportedExplorers
	 * @param annotations
	 */
	private static void getExplorerInfoFromAnnotation(
			List allSupportedExplorers, List annotations) {
		VjoProposalAditionalInfoGenerator.getExplorerInfoFromAnnotation(allSupportedExplorers, annotations);
	}

	/**
	 * Copy the html template to temporary folder.
	 */
	private static void copyHtmlTemplateFromBundleToTemp() {
		String[] htmlFiles = new String[] { PERS_FILENAME };
		for (String file : htmlFiles) {
			copyFileFromBundleToTemp("templates", file);
		}
	}

	/**
	 * Copy current icons to temporary folder.
	 */
	private static void copyIconsFromBundleToTemp() {
		String[] icons = new String[] { "chrome.gif", "firefox.gif", "ie.gif",
				"opera.gif", "safari.gif" };
		for (String icon : icons) {
			copyFileFromBundleToTemp("icons", icon);
		}
	}

	/**
	 * Copy specified icon to temporary folder.
	 * 
	 * @param folderName
	 *            TODO
	 */
	private static void copyFileFromBundleToTemp(String folderName,
			String fileName) {
		try {
			Location configLoc = Platform.getConfigurationLocation();

			// First check whether we have the temporary folder.
			generateTempFolder(new URL(configLoc.getURL(), PERS_FOLDER), true);

			URL url = new URL(configLoc.getURL(), PERS_FOLDER + "//" + fileName);
			File persFile = new File(url.getFile());
			if (persFile.exists()) {
				return;
			} else {
				persFile.createNewFile();
			}

			InputStream stream = VjetUIPlugin.getDefault().getBundle()
					.getEntry("//" + folderName + "//" + fileName).openStream();
			byte[] bs = new byte[stream.available()];
			stream.read(bs);

			FileOutputStream fop = new FileOutputStream(persFile);

			if (persFile.exists()) {
				fop.write(bs);
				fop.flush();
				fop.close();
			}

		} catch (IOException e) {
		}
	}

	/**
	 * @param supportedTypes
	 * @param type
	 * @return
	 */
	private static String getDescOfSupport(List<String> supportedTypes,
			String type) {
		for (String everyType : supportedTypes) {
			if (everyType.contains(type)) {
				return everyType;
			}
		}
		return "";
	}

	/**
	 * @param desc
	 * @return
	 */
	private static String translateCharacterToNumber(String desc) {
		if (desc != null) {
			String[] numbers = { "_ZERO", "_ONE", "_TWO", "_THREE", "_FOUR",
					"_FIVE", "_SIX", "_SEVEN", "_EIGHT", "_NINE" };
			for (int i = 0; i < numbers.length; i++) {
				if (desc.contains(numbers[i])) {
					desc = desc.replace(numbers[i], String.valueOf(i));
				}
			}
			if (desc.contains("_DOT")) {
				desc = desc.replace("_DOT", ".");
			}
		}
		return desc;
	}

	public static String getElementBriefDesc(IJstProperty property) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(property.getOwnerType().getName() + "\n");
		// buffer.append("<dl><dt>");
		buffer.append(getModifierListStr(property.getModifiers()) + " <b>");
		buffer.append(property.getName() + "</b> ");
		// buffer.append("</dt><dd></dd></dl>");

		return buffer.toString();
	}

	public static String getModifierListStr(JstModifiers jstModifiers) {
		List<BaseJstNode> list = jstModifiers.getChildren();
		if (list == null || list.isEmpty()) {
			return "";
		}
		StringBuffer buffer = new StringBuffer();
		Iterator<BaseJstNode> it = list.iterator();
		while (it.hasNext()) {
			BaseJstNode node = it.next();
			buffer.append("");
		}
		return buffer.toString();
	}

	public static String getElementBriefDesc(IJstMethod method) {
		StringBuffer buffer = new StringBuffer();
		// buffer.append("<dl><dt>");
		buffer.append(getModifierListStr(method.getModifiers()) + " "
				+ method.getRtnType().getName() + " <b>");
		buffer.append(method.getName() + "</b> ");
		// buffer.append("</dt></dl>");
		buffer.append("(" + combineParameters(method) + ")");
		return buffer.toString();
	}

	/**
	 * @param method
	 * @return
	 */
	private static String combineParameters(IJstMethod method) {
		String par = "";
		if (method != null) {
			List<JstArg> args = method.getArgs();
			String comma = "";
			int i = 0;
			for (JstArg arg : args) {
				if (i != 0) {
					comma = ", ";
				}
				par += comma + arg.getType().getSimpleName() + " "
						+ arg.getName();
				i++;
			}
		}
		return par;
	}

	/**
	 * Gets the reader content as a String
	 */
	private static String getString(Reader reader) {
		StringBuffer buf = new StringBuffer();
		char[] buffer = new char[1024];
		int count;
		try {
			while ((count = reader.read(buffer)) != -1)
				buf.append(buffer, 0, count);
		} catch (IOException e) {
			try {
				reader.close();
			} catch (IOException e1) {
			}
			return null;
		}
		try {
			reader.close();
		} catch (IOException e) {
		}
		return buf.toString();
	}

	@Override
	public void apply(IDocument document, char trigger, int offset) {
		super.apply(document, trigger, offset);
		if (isTypeProposal()) {
			VjoSourceType type = (VjoSourceType) getModelElement();
			IJstType jstType = (IJstType) extraInfo;
			MultiTextEdit edit = needsRewriter.rewrite(type, jstType);
			applyNeeds(document, edit);
			// super.apply(document);
		}
	}

	private void applyNeeds(IDocument document, MultiTextEdit edit) {
		int oldLen = document.getLength();
		try {
			edit.apply(document, TextEdit.UPDATE_REGIONS);
			setReplacementOffset(getReplacementOffset() + document.getLength()
					- oldLen);
		} catch (Exception e) {
			DLTKCore.error(e.getMessage(), e);
		}
	}

	private boolean isTypeProposal() {
		return getModelElement() instanceof VjoSourceType
				&& extraInfo instanceof IJstType;
	}

	public void setExtraInfo(Object extraInfo) {
		this.extraInfo = extraInfo;
	}
}

