package com.xblog.utils;

/*
 * The sessionScope bean with various utilities. Used primarily as a store for layout and config values
 */

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.DocumentCollection;
import lotus.domino.EmbeddedObject;
import lotus.domino.Item;
import lotus.domino.MIMEEntity;
import lotus.domino.NotesException;
import lotus.domino.RichTextItem;
import lotus.domino.View;
import lotus.domino.ViewEntry;
import lotus.domino.ViewEntryCollection;

import com.ibm.domino.xsp.module.nsf.NotesContext;
import com.ibm.xsp.designer.context.ServletXSPContext;
import com.ibm.xsp.designer.context.XSPUrl;
import com.xblog.renderkit.utils.RenderUtils;

public class XBlogUtils implements Serializable {

	private static final long serialVersionUID = 1L;
	private HashMap<String, Object> layoutValues;
	private HashMap<String, Object> configValues;
	private HashMap<String, String> translationValues;
	private HashMap<String, String> hotTagValues;
	private HashMap<String, String> hotTextValues;
	private boolean debug;
	public static final DebugToolbar dbar = new DebugToolbar();

	/**
	 * XBlogUtils managed bean. Used to get and set configuration and layout
	 * values This is a applicationScope bean
	 */
	public XBlogUtils() {
		try {
			if (xblogSetup()) {
				this.configValues = putConfigValues();
				this.layoutValues = putLayoutValues();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static XBlogUtils getCurrentInstance() {
		if (JSFUtil.getVariableValue("XBlogUtils") instanceof XBlogUtils) {
			return (XBlogUtils) JSFUtil.getVariableValue("XBlogUtils");
		} else {
			return null;
		}
	}

	/**
	 * Get a field value from the currently published configuration document
	 * 
	 * @param configItemValue
	 *            - The field name
	 * @param arrayIndex
	 *            - The value index, if not a multi-value field usually index 0
	 * @return - The requested value
	 */
	@SuppressWarnings("unchecked")
	public Object getConfigValue(String configItemValue, int arrayIndex) {
		configItemValue = configItemValue.toLowerCase();
		if (configValues != null && configValues.containsKey(configItemValue)) {
			if (configValues.get(configItemValue) instanceof Vector) {
				return ((Vector<Object>) configValues.get(configItemValue)).get(arrayIndex);
			} else {
				return configValues.get(configItemValue);
			}
		} else {
			return null;
		}
	}

	/**
	 * Return a vector if the field is a multi-value field
	 * 
	 * @param configItemValue
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Object getConfigValue(String configItemValue) {
		configItemValue = configItemValue.toLowerCase();
		if (configValues.containsKey(configItemValue)) {
			if (configValues.get(configItemValue) instanceof Vector) {
				return (configValues.get(configItemValue));
			} else {
				return configValues.get(configItemValue);
			}
		} else {
			return null;
		}
	}

	/**
	 * Get a field value from the currently published layout document
	 * 
	 * @param layoutItemValue
	 *            - The field name
	 * @param arrayIndex
	 *            - The value index, if not a multi-value field usually index 0
	 * @return - The requested value
	 */
	@SuppressWarnings("unchecked")
	public Object getLayoutValue(String layoutItemValue, int arrayIndex) {
		layoutItemValue = layoutItemValue.toLowerCase();
		if (layoutValues.containsKey(layoutItemValue)) {
			if (layoutValues.get(layoutItemValue) instanceof Vector) {
				return ((Vector<Object>) layoutValues.get(layoutItemValue)).get(arrayIndex);
			} else {
				return layoutValues.get(layoutItemValue);
			}
		} else {
			return null;
		}
	}

	/**
	 * Return a vector if the field is a multi-value field
	 * 
	 * @param layoutItemValue
	 * @return
	 */
	public Object getLayoutValue(String layoutItemValue) {
		try {
			Document layoutDoc = getLayoutDoc();
			if (layoutDoc.hasItem(layoutItemValue)) {
				Item layoutItem = layoutDoc.getFirstItem(layoutItemValue);
				return layoutItem.getValues();
			}
		} catch (NotesException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Get the currently published Layout Document
	 * 
	 * @return
	 */
	public Document getLayoutDoc() {
		Database db = NotesContext.getCurrent().getCurrentDatabase();
		View layoutView;
		try {
			layoutView = db.getView("layouts");
			Document layoutDoc = layoutView.getDocumentByKey(getConfigValue("layout", 0));
			return layoutDoc;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Get the currently published configuration document
	 * 
	 * @return
	 */
	public Document getConfigDoc() {
		Database db = NotesContext.getCurrent().getCurrentDatabase();
		View configView;
		try {
			configView = db.getView("configuration");
			Document configDoc = configView.getFirstDocument();
			return configDoc;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Since we can't store a NotesDocument in the applicationScope, we put all
	 * of our published Layout Document values here and store that in the
	 * application scope
	 * 
	 * @return - HashMap<String FieldName,Object ValueIndex>
	 * @throws NotesException
	 */
	@SuppressWarnings("unchecked")
	private HashMap<String, Object> putLayoutValues() throws NotesException {
		Document layoutDoc = getLayoutDoc();
		HashMap<String, Object> hm = new HashMap<String, Object>();
		if (layoutDoc != null) {
			Vector<Item> items = layoutDoc.getItems();
			Iterator<Item> itr = items.iterator();
			while (itr.hasNext()) {
				Item curItem = itr.next();
				if (curItem.getValues() == null) {
					hm.put(curItem.getName().toLowerCase(), "");
				} else {
					if (!curItem.getName().equalsIgnoreCase("$updatedby") && !curItem.getName().equalsIgnoreCase("$revisions")) {
						hm.put(curItem.getName().toLowerCase(), curItem.getValues());
					}
				}
			}
			if (hm.size() == 0) {
				hm = null;
			} else {
				hm.put("unid", layoutDoc.getUniversalID());
			}
			return hm;
		} else {
			return null;
		}
	}

	/**
	 * Since we can't store a NotesDocument in the applicationScope, we put all
	 * of our published Layout Document values here and store that in the
	 * application scope
	 * 
	 * @return - HashMap<String FieldName,Object ValueIndex>
	 * @throws NotesException
	 */
	@SuppressWarnings("unchecked")
	private HashMap<String, Object> putConfigValues() throws NotesException {
		Document configDoc = getConfigDoc();
		HashMap<String, Object> hm = new HashMap<String, Object>();
		if (configDoc != null) {
			Vector<Item> items = configDoc.getItems();
			Iterator<Item> itr = items.iterator();
			while (itr.hasNext()) {
				Item curItem = itr.next();
				if (curItem.getValues() == null) {
					hm.put(curItem.getName().toLowerCase(), "");
				} else {
					if (!curItem.getName().equalsIgnoreCase("$updatedby") && !curItem.getName().equalsIgnoreCase("$revisions")) {
						hm.put(curItem.getName().toLowerCase(), curItem.getValues());
					}
				}
			}
			if (hm.isEmpty()) {
				hm = null;
			} else {
				hm.put("unid", configDoc.getUniversalID());
			}
			return hm;
		} else {
			return null;
		}
	}

	/**
	 * Get the ConfigValues Map which contains values from the configuration
	 * document
	 */
	public HashMap<String, Object> getConfigValuesMap() {
		if (configValues != null) {
			return configValues;
		} else {
			return new HashMap<String, Object>();
		}
	}

	public HashMap<String, Object> getLayoutValuesMap() {
		return layoutValues;
	}

	public String getCurrentDocUNID() {
		try {
			Document currentDoc = JSFUtil.getCurrentDocument();
			if (currentDoc != null) {
				return currentDoc.getUniversalID();
			} else {
				return "noUNID";
			}
		} catch (NotesException e) {
			e.printStackTrace();
			return "noUNID";
		}
	}

	@Deprecated
	public String replaceTransTag(String tag) {
		if (tag == null) {
			return null;
		}
		try {
			View transView = NotesContext.getCurrent().getCurrentDatabase().getView("(luTranslationsTag)");
			ViewEntry ent = transView.getEntryByKey(getConfigValue("Language", 0) + tag);
			if (ent != null) {
				return ent.getColumnValues().get(1).toString();
			}
		} catch (NotesException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * getBlogUrl will return the full url to the NSF
	 */
	public String getBlogUrl() {
		ServletXSPContext thisContext = (ServletXSPContext) JSFUtil.getVariableValue("context");
		XSPUrl xspUrl = thisContext.getUrl();
		String fullUrl = xspUrl.toString();
		String blogUrl = fullUrl.substring(0, fullUrl.indexOf(".nsf") + 4);
		return blogUrl;
	}

	/**
	 * getCurrentUrl will return the current url
	 */
	public String getCurrentUrl() {
		try {
			ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
			HttpServletRequest request = (HttpServletRequest) extContext.getRequest();
			String fullUrl = request.getRequestURL().toString();
			if (request.getQueryString() != null) {
				fullUrl = fullUrl + "?" + request.getQueryString();
			}
			URL javaUrl = new URL(fullUrl);
			String currentUrl = javaUrl.toString();
			return currentUrl;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * getRelativeUrl will return a relative url to the nsf
	 */
	public String getRelativeUrl() {
		String url = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
		return url;
	}

	/**
	 * Not sure what this is used for but appears to return the current page
	 * name only
	 */
	public String getCurrentPageName() {
		String url = getCurrentUrl();
		String pageName = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf(".xsp"));
		Character firstChar = pageName.charAt(0);
		if (pageName.startsWith("vw") || pageName.startsWith("admin.xsp")) {
			return pageName;
		} else if (firstChar.isLowerCase(firstChar)) {
			return firstChar.toUpperCase(firstChar) + pageName.substring(1);
		} else {
			return pageName;
		}
	}

	/**
	 * Get just the Host Name
	 */
	public String getHostName() {
		ServletXSPContext thisContext = (ServletXSPContext) JSFUtil.getVariableValue("context");
		XSPUrl thisUrl = thisContext.getUrl();
		return thisUrl.getHost();
	}

	/**
	 * Is XBlog setup?
	 */
	public boolean xblogSetup() {
		try {
			if (getConfigDoc() == null) {
				return false;
			} else {
				if (getConfigDoc().getItemValueString("ConfigName") == null || getConfigDoc().getItemValueString("ConfigName").isEmpty()) {
					return false;
				} else {
					return true;
				}
			}
		} catch (NotesException e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * Accepts a vector and returns a string
	 */
	public String vectorToString(Vector convVector) {
		String result = "";
		for (Object valuePart : convVector) {
			if (valuePart != null) {
				result = result.concat(valuePart.toString());
			}
		}
		return result;
	}

	/**
	 * Is XBlog in Debug Mode?
	 */
	public boolean isDebug() {
		if (getConfigValue("Debug", 0).toString().equalsIgnoreCase("yes")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Determine the number of comments to display on the frontend. This value
	 * is determined by the number of actually visible comments. Meaning ones
	 * that aren't blocked or awaiting approval
	 * 
	 * @param unid
	 *            String - The unid of the post
	 * @return Integer
	 */
	public Integer getNumberComments(String unid) {
		try {
			Database db = NotesContext.getCurrent().getCurrentDatabase();
			Document doc = db.getDocumentByUNID(unid);
			Document respDoc = null;
			DocumentCollection respCol = doc.getResponses();
			if (respCol.getCount() > 0) {
				respDoc = respCol.getFirstDocument();
				int counter = 0;
				while (respDoc != null) {
					String blocked = "yes";
					String pending = respDoc.getItemValueString("PendingApproval");
					if (!respDoc.hasItem("Blocked")) {
						/*
						 * This is a fix for a bug introduced in 0.9.1a. By
						 * removing anti-samy we were not setting a default
						 * value for "Blocked". This functionality has been
						 * added back (setting a default value) in the
						 * PostComment class, but since we included it in a
						 * release, we need to ensure it doesn't cause an issue
						 * with any comments that may have been posted with that
						 * version.
						 */
						blocked = "no";
					} else {
						blocked = respDoc.getItemValueString("Blocked");
					}
					if (blocked.equalsIgnoreCase("no") && pending.equalsIgnoreCase("no")) {
						counter++;
					}
					respDoc = respCol.getNextDocument(respDoc);
				}
				return counter;
			}
		} catch (NotesException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public List<Map<String, String>> getAttachmentInfo(String unid) {
		List<Map<String, String>> attachments = new ArrayList<Map<String, String>>();
		try {
			Database db = JSFUtil.getCurrentDatabase();
			Document doc = db.getDocumentByUNID(unid);
			if (doc != null && doc.getEmbeddedObjects().isEmpty()) {
				RichTextItem attachmentItem = (RichTextItem) doc.getFirstItem("Attachments");
				if (attachmentItem != null) {
					if (!attachmentItem.getEmbeddedObjects().isEmpty()) {
						for (Object fileObj : attachmentItem.getEmbeddedObjects()) {
							Map<String, String> fileInfoMap = new HashMap<String, String>();
							EmbeddedObject file = (EmbeddedObject) fileObj;
							fileInfoMap.put("Name", file.getName());
							fileInfoMap.put("Size", ((Integer) file.getFileSize()).toString());
							attachments.add(fileInfoMap);
						}
					}
				}
			}
		} catch (NotesException e) {
			dbar.error("Error " + e.getMessage(), "XBlogUtils.getAttachmentInfo(String unid)");
			e.printStackTrace();
		}
		return attachments;
	}

	public String getRichTextMime(Item richTextItem) {
		MIMEEntity mimeContent;
		try {
			mimeContent = richTextItem.getMIMEEntity();
			return RenderUtils.replaceHotText(mimeContent.getContentAsText(), null);
		} catch (NotesException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * Map containing key: Hot Tag/Hot Text/Emoticon, value: value for that item
	 * 
	 * @return Map<String, String>
	 */
	public Map<String, String> getHotTagValues() {
		try {
			if (hotTagValues == null) {
				hotTagValues = new HashMap<String, String>();
				hotTagValues.putAll(getMapFromView("(luHotTags)", null, 0, 1));
				hotTagValues.putAll(getMapFromView("(luCustomHotTags)", null, 0, 1));
				hotTagValues.putAll(getMapFromView("(luEmoticon)", null, 0, 1));
			}
			return hotTagValues;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void setHotTagValues(HashMap<String, String> hotTagValues) {
		this.hotTagValues = hotTagValues;
	}

	/**
	 * Map containing key: Hot Text, value: value for that item
	 * 
	 * @return Map<String, String>
	 */
	public Map<String, String> getHotTextValues() {
		/*
		 * Had to include this by itself because we had some double replacement
		 * going on when replacing the content of a post
		 */
		if (hotTextValues == null) {
			hotTextValues = new HashMap<String, String>();
			hotTextValues.putAll(getMapFromView("(luHotText)", null, 0, 1));
		}
		return hotTextValues;
	}

	public void setHotTextValues(HashMap<String, String> hotTextValues) {
		this.hotTextValues = hotTextValues;
	}

	/**
	 * Map containing key: translation tag, value: value for that tag
	 * 
	 * @return Map<String, String>
	 */
	public Map<String, String> getTranslationValues() {
		try {
			if (translationValues == null) {
				translationValues = new HashMap<String, String>();
				String lang = getConfigValue("Language", 0).toString();
				translationValues.putAll(getMapFromView("(luTranslations)", lang, 1, 2));
			}
			return translationValues;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void setTranslationValues(HashMap<String, String> translationValues) {
		this.translationValues = translationValues;
	}

	/**
	 * Build a map from the passed viewName and columns
	 * 
	 * @param viewName
	 *            String - The name of the view to build the map from
	 * @param luKey
	 *            String - A lookup key, use null if you want all the
	 * @param keyCol
	 *            Integer - The column number (0 based) that will be the key of
	 *            the map entry
	 * @param valCol
	 *            Integer - The column number (0 based) that will be the value
	 *            of the map entry
	 * @return Map<String, String>
	 */
	private Map<String, String> getMapFromView(String viewName, String luKey, int keyCol, int valCol) {
		Map<String, String> returnMap = new HashMap<String, String>();
		try {
			View luView = NotesContext.getCurrent().getCurrentDatabase().getView(viewName);
			ViewEntryCollection entCol = null;
			if (luKey != null) {
				entCol = luView.getAllEntriesByKey(luKey);
			} else {
				entCol = luView.getAllEntries();
			}
			ViewEntry ent = entCol.getFirstEntry();
			while (ent != null) {
				String key = ent.getColumnValues().get(keyCol).toString();
				if (viewName.equalsIgnoreCase("(luHotTags)")) {
					returnMap.put(key, "");
				} else {
					Object viewVal = ent.getColumnValues().get(valCol);
					if (viewVal instanceof Vector) {
						viewVal = vectorToString((Vector) viewVal);
					}
					returnMap.put(key, viewVal.toString());
				}
				ViewEntry prevEnt = ent;
				ent = entCol.getNextEntry(ent);
				prevEnt.recycle();
			}
		} catch (NotesException e) {
			e.printStackTrace();
		}
		return returnMap;
	}
}

