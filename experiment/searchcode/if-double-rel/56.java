/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.dsf.html.dom;

import org.w3c.dom.DOMException;

import org.ebayopensource.dsf.common.event.AbortDsfEventProcessingException;
import org.ebayopensource.dsf.common.event.DsfEvent;
import org.ebayopensource.dsf.common.node.IDNodeRelationshipVerifier;
import org.ebayopensource.dsf.common.node.visitor.IDNodeVisitor;
import org.ebayopensource.dsf.css.CssClassConstant;
import org.ebayopensource.dsf.css.CssIdConstant;
import org.ebayopensource.dsf.css.dom.ICssStyleDeclaration;
import org.ebayopensource.dsf.dom.DNode;
import org.ebayopensource.dsf.dom.support.DNamespace;
import org.ebayopensource.dsf.html.events.EventType;
import org.ebayopensource.dsf.html.events.ISimpleJsEventHandler;
import org.ebayopensource.dsf.html.js.IJsFunc;
import com.ebay.kernel.util.Z;

/**
* http://www.w3.org/TR/REC-html40/struct/links.html#edef-LINK
* <p>
* Example:
* <pre>
* <HEAD>
* <TITLE>The manual in English</TITLE>
* <LINK title="The manual in Dutch"
*       type="text/html"
*       rel="alternate"
*       hreflang="nl" 
*       href="http://someplace.com/manual/dutch.html">
* <LINK title="The manual in Portuguese"
*       type="text/html"
*       rel="alternate"
*       hreflang="pt" 
*       href="http://someplace.com/manual/portuguese.html">
* <LINK title="The manual in Arabic"
*       type="text/html"
*       rel="alternate"
*       charset="ISO-8859-6"
*       hreflang="ar" 
*       href="http://someplace.com/manual/arabic.html">
* <LINK lang="fr" title="La documentation en Fran&ccedil;ais"
*       type="text/html"
*       rel="alternate"
*       hreflang="fr"
*       href="http://someplace.com/manual/french.html">
* </HEAD>
* </pre>
*/
public class DLink extends BaseAttrsHtmlElement 
//	implements IDRepeatableHeadElement, IDStrict, IAttrs, 
//		/* HTML 5.0 */ IMetadataContent
{
	private static final long serialVersionUID = 4050763784366929202L;

	// For target.  Target can accept a user-defined value as well.
	/** "_blank" */
	public static final String TARGET_BLANK = "_blank" ;
	/** "_self" */
	public static final String TARGET_SELF = "_self" ;	
	/** "_parent" */
	public static final String TARGET_PARENT = "_parent" ;
	/** "_top" */
	public static final String TARGET_TOP = "_top" ;

	// For rel.
	/** "Alternate" */
	public static final String REL_ALTERNATE = "Alternate" ;
	/** "Stylesheet" */
	public static final String REL_STYLESHEET = "Stylesheet" ;
	/** "Start" */
	public static final String REL_START = "Start" ;
	/** "Next" */
	public static final String REL_NEXT = "Next" ;
	/** "Prev" */
	public static final String REL_PREV = "Prev" ;
	/** "Contents" */
	public static final String REL_CONTENTS = "Contents" ;
	/** "Index" */
	public static final String REL_INDEX = "Index" ;
	/** "Glossary" */
	public static final String REL_GLOSSARY = "Glossary" ;
	/** "Copyright" */
	public static final String REL_COPYRIGHT = "Copyright" ;
	/** "Chapter" */
	public static final String REL_CHAPTER = "Chapter" ;
	/** "Section" */
	public static final String REL_SECTION = "Section" ;
	/** "Subsection" */
	public static final String REL_SUBSECTION = "Subsection" ;
	/** "Appendix" */
	public static final String REL_APPENDIX = "Appendix" ;
	/** "Help" */
	public static final String REL_HELP = "Help" ;
	/** "Bookmark" */
	public static final String REL_BOOKMARK = "Bookmark" ;
	
	// For media.
	/** "all" */
	public static final String MEDIA_ALL = "all" ;
	/** "aural" */
	public static final String MEDIA_AURAL = "aural" ;
	/** "braille" */
	public static final String MEDIA_BRAILLE = "braille" ;
	/** "handheld" */
	public static final String MEDIA_HANDHELD = "handheld" ;
	/** "print" */
	public static final String MEDIA_PRINT = "print" ;
	/** "projection" */
	public static final String MEDIA_PROJECTION = "projection" ;
	/** "screen" */
	public static final String MEDIA_SCREEN = "screen" ;
	/** "tty" */
	public static final String MEDIA_TTY = "tty" ;
	/** "tv" */
	public static final String MEDIA_TV = "tv" ;

	//
	// Constructor(s)
	//
	public DLink() {
		super(HtmlTypeEnum.LINK);
	}
	
	public DLink(final DHtmlDocument doc) {
		super(doc, HtmlTypeEnum.LINK);
	}
	
	public DLink(final String jif) {
		this() ;
		jif(jif) ;
	}
	
	public static DLink newCssStyleSheetReference(final String src) {
		final DLink link = new DLink() ;
		link.setHtmlRel("stylesheet") ;
		link.setHtmlType("text/css") ;
		link.setHtmlHref(src) ;	
		return link ;
	}
	
	//
	// Framework
	//
	@Override
	public HtmlTypeEnum htmlType() {
		return HtmlTypeEnum.LINK ;
	}
		
	//
	// HTML Attributes
	//	
//	public boolean getHtmlDisabled() { // TODO: Not in Spec
//		return getBinary("disabled");
//	}
//
//	public DLink setHtmlDisabled(boolean disabled) { // TODO: Not in Spec
//		setAttribute("disabled", disabled);
//		return this ;
//	}

	/**
	 * @deprecated - Not supported in HTML 5.0
	 */
	public String getHtmlCharset() {
		return getHtmlAttribute(EHtmlAttr.charset);
	}
	/**
	 * @deprecated - Not supported in HTML 5.0
	 */
	public DLink setHtmlCharset(final String charset) {
		setHtmlAttribute(EHtmlAttr.charset, charset);
		return this ;
	}

	/**
	 * The destination of the link(s) is given by the href attribute, which must 
	 * be present and must contain a valid URL. If the href attribute is absent, 
	 * then the element does not define a link.
	 */
	public String getHtmlHref() {
		return getHtmlAttribute(EHtmlAttr.href);
	}
	/**
	 * The destination of the link(s) is given by the href attribute, which must 
	 * be present and must contain a valid URL. If the href attribute is absent, 
	 * then the element does not define a link.
	 */
	public DLink setHtmlHref(final String href) {
		setHtmlAttribute(EHtmlAttr.href, href);
		return this ;
	}

	public String getHtmlHreflang() {
		return getHtmlAttribute(EHtmlAttr.lang);
	}
	public DLink setHtmlHreflang(final String hreflang) {
		setHtmlAttribute(EHtmlAttr.hreflang, hreflang);
		return this ;
	}

	public String getHtmlMedia() {
		return getHtmlAttribute(EHtmlAttr.media);
	}
	public DLink setHtmlMedia(final String media) {
		setHtmlAttribute(EHtmlAttr.media, media);
		return this ;
	}

	/**
	 * The types of link indicated (the relationships) are given by the value of 
	 * the rel  attribute, which must be present, and must have a value that is 
	 * a set of space-separated tokens. The allowed values and their meanings are 
	 * defined in a later section. If the rel  attribute is absent, or if the 
	 * values used are not allowed according to the definitions in this 
	 * specification, then the element does not define a link.
	 */
	public String getHtmlRel() {
		return getHtmlAttribute(EHtmlAttr.rel);
	}
	/**
	 * The types of link indicated (the relationships) are given by the value of 
	 * the rel  attribute, which must be present, and must have a value that is 
	 * a set of space-separated tokens. The allowed values and their meanings are 
	 * defined in a later section. If the rel  attribute is absent, or if the 
	 * values used are not allowed according to the definitions in this 
	 * specification, then the element does not define a link.
	 */
	public DLink setHtmlRel(final String rel) {
		setHtmlAttribute(EHtmlAttr.rel, rel);
		return this ;
	}

	/**
	 * @deprecated - Not supported in HTML 5.0
	 */
	public String getHtmlRev() {
		return getHtmlAttribute(EHtmlAttr.rev);
	}
	/**
	 * @deprecated - Not supported in HTML 5.0
	 */
	public DLink setHtmlRev(final String rev) {
		setHtmlAttribute(EHtmlAttr.rev, rev);
		return this ;
	}

	/**
	 * @deprecated - Not supported in HTML 5.0
	 */
	public String getHtmlTarget() {
		return getHtmlAttribute(EHtmlAttr.target);
	}
	/**
	 * @deprecated - Not supported in HTML 5.0
	 */
	public DLink setHtmlTarget(final String target) {
		setHtmlAttribute(EHtmlAttr.target, target);
		return this ;
	}

	public String getHtmlType() {
		return getHtmlAttribute(EHtmlAttr.type);
	}
	public DLink setHtmlType(final String type) {
		setHtmlAttribute(EHtmlAttr.type, type);
		return this ;
	}
	
	public boolean getHtmlDisabled() {
		String disabled = getHtmlAttribute(EHtmlAttr.disabled);
		return "true".equalsIgnoreCase(disabled) ? true : false;
	}
	public DLink setHtmlDisabled(boolean disabled) {
		setHtmlAttribute(EHtmlAttr.disabled, disabled);
		return this ;
	}
	
	// Not an int
	public String getHtmlSizes() {  // HTML 5.0
		return getHtmlAttribute(EHtmlAttr.sizes);
	}
	public DLink setHtmlSizes(final String sizes) {  // HTML 5.0
		setHtmlAttribute(EHtmlAttr.sizes, sizes);
		return this ;
	}
	
	//
	// Overrides from Object
	//
	@Override
	public String toString() {
		return super.toString() +
		Z.fmt("charset", getHtmlCharset()) + 
		Z.fmt("disabled", "" + getHtmlDisabled()) +
		Z.fmt("href", getHtmlHref()) +
		Z.fmt("hreflang", getHtmlHreflang()) +
		Z.fmt("media", "" + getHtmlMedia()) +
		Z.fmt("rel", "" + getHtmlRel()) +
		Z.fmt("rev", "" + getHtmlRev()) +
		Z.fmt("target", "" + getHtmlTarget()) +
		Z.fmt("type", "" + getHtmlType())  +
		Z.fmt("sizes", "" + getHtmlSizes()) ;
	}
	
	//
	// Overrides from DElement
	//
	/**
	 * Shorthand of appendChild(Node) but takes a DNode arg.
	 * Returns "this" DNode vs. the added child - this is nice for
	 * cascade style programming. 
	 * <code>
	 * node.add(anotherNode).addRaw("&nbsp;") ;
	 * vs.
	 * node.add(anotherNode);
	 * node.addRaw("&nbsp;");
	 * @param newChild node to be appended.  Throws DOMException if value is null.
	 * @return this
	 * @throws DOMException
	 */
	@Override
	public DLink add(final DNode newChild) throws DOMException {
		super.add(newChild) ;
		return this ;
	}
	
	/**
	 * Shorthand for add(new DText(value))
	 * <br><code>
	 * ex: node.add("Address")
	 * </code>
	 * @param value to be added as a DText node.  Throws DOMException if value is null.
	 * @return this
	 * @throws DOMException
	 */
	@Override
	public DLink add(final String value) throws DOMException {
		super.add(value) ;
		return this ;
	}
	
	/**
	 * Shorthand for add(new DRawString(value))
	 * <br>
	 * The value will be emitted as is without any escaping.
	 * <br>
	 * ex: node.addRaw("&npbsp;")
	 * @param  value to be added without any escaping. Throws DOMException if value is null.
	 */
	@Override
	public DLink addRaw(final String value) throws DOMException {
		super.addRaw(value) ;
		return this ;
	}
	
	/**
	 * This double dispatch approach provides the control point for the node
	 * to have customized behavior.
	 */
	@Override
	public DLink dsfAccept(final IDNodeVisitor visitor) {
		super.dsfAccept(visitor) ;
		return this;
	}
	
	/**
	 * Broadcasts the event to any registered IDsfEventListner's.
	 * The listeners are broadcast to in the order they were maintained.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public DLink dsfBroadcast(final DsfEvent event) // must not be null
		throws AbortDsfEventProcessingException
	{
		super.dsfBroadcast(event) ;
		return this;
	}
	
	/**
	 * Set the relationship verifier for this instance
	 * <br>
	 * The verifier can be used to assert a newly added attribute, child, facet 
	 * or parent.
	 */
	@Override
	public DLink setDsfRelationshipVerifier(
		final IDNodeRelationshipVerifier relationshipVerifier)
	{
		super.setDsfRelationshipVerifier(relationshipVerifier) ;
		return this;
	}
	
	@Override
	public DLink cloned() {
		return (DLink)super.cloned() ;
	}
	
    /**
     * set namespace for this node.
     * update the nodename based on the given namespace
     * @param namespace
     * @return
     */
    @Override
    public DLink setDsfNamespace(DNamespace namespace){
    	super.setDsfNamespace(namespace) ;
    	return this ;
    }
	
	//
	// Overrides from BaseHtmlElement
	//
	/**
	 * The accesskey attribute's value is used by the user agent as a guide for 
	 * creating a keyboard shortcut that activates or focuses the element.
	 */
	@Override
	public DLink setHtmlAccessKey(final String accessKey) {
		super.setHtmlAccessKey(accessKey) ;
		return this ;
	}
		
	/**
	 * set class name, overwrite current class(es)
	 */
	@Override
	public DLink setHtmlClassName(final String className) {
		super.setHtmlClassName(className) ;
		return this ;
	}
	@Override
	public DLink setHtmlClassName(final CssClassConstant ccc) {
		super.setHtmlClassName(ccc) ;
		return this ;
	}

	/**
	 * The contenteditable  attribute is an enumerated attribute whose keywords 
	 * are the empty string, true, and false. The empty string and the true keyword 
	 * map to the true state. The false keyword maps to the false state. In 
	 * addition, there is a third state, the inherit state, which is the missing 
	 * value default (and the invalid value default).
	 */
	@Override
	public DLink setHtmlContentEditable(final String editable) {
		super.setHtmlContentEditable(editable) ;
		return this ;
	}
	
	/**
	 * The contextmenu  attribute gives the element's context menu. The value 
	 * must be the ID of a menu element in the DOM. If the node that would be 
	 * obtained by the invoking the getElementById() method using the attribute's 
	 * value as the only argument is null or not a menu element, then the element 
	 * has no assigned context menu. Otherwise, the element's assigned context 
	 * menu is the element so identified.
	 */
	@Override
	public DLink setHtmlContextMenu(final String contextMenu) {
		super.setHtmlContextMenu(contextMenu) ;
		return this ;
	}
	
	/**
	 * The dir attribute specifies the element's text directionality. The attribute 
	 * is an enumerated attribute with the keyword ltr mapping to the state ltr, 
	 * and the keyword rtl  mapping to the state rtl. The attribute has no defaults.
	 */
	@Override
	public DLink setHtmlDir(final String dir) {
		super.setHtmlDir(dir) ;
		return this ;
	}
	
	/**
	 * The draggable attribute is an enumerated attribute. It has three states. 
	 * The first state is true and it has the keyword true. The second state is 
	 * false and it has the keyword false. The third state is auto; it has no 
	 * keywords but it is the missing value default.
	 */
	@Override
	public DLink setHtmlDraggable(final String draggable) {  // true, false, auto
		super.setHtmlDraggable(draggable) ;
		return this ;
	}
	/**
	 * The draggable attribute is an enumerated attribute. It has three states. 
	 * The first state is true and it has the keyword true. The second state is 
	 * false and it has the keyword false. The third state is auto; it has no 
	 * keywords but it is the missing value default.
	 */
	@Override
	public DLink setHtmlDraggable(final boolean draggable) {  // true, false
		super.setHtmlDraggable(draggable) ;
		return this ;
	}
	
	/** 
	 * This attribute is a boolean attribute. When specified on an element, it 
	 * indicates that the element is not yet, or is no longer, relevant. User 
	 * agents should not render elements that have the hidden attribute specified.
	 */
	@Override
	public DLink setHtmlHidden(final String hidden) {
		super.setHtmlHidden(hidden);
		return this ;
	}
	/** 
	 * This attribute is a boolean attribute. When specified on an element, it 
	 * indicates that the element is not yet, or is no longer, relevant. User 
	 * agents should not render elements that have the hidden attribute specified.
	 */
	@Override
	public DLink setHtmlHidden(final boolean hidden) {
		super.setHtmlHidden(hidden);
		return this ;
	}
	
	/**
	 * The id attribute represents its element's unique identifier. The value must 
	 * be unique in the element's home subtree and must contain at least one character. 
	 * The value must not contain any space characters
	 */
	@Override
	public DLink setHtmlId(String id) {
		super.setHtmlId(id) ;
		return this ;
	}
	/**
	 * The id attribute represents its element's unique identifier. The value must 
	 * be unique in the element's home subtree and must contain at least one character. 
	 * The value must not contain any space characters
	 */	
	@Override
	public DLink setHtmlId(CssIdConstant id) {
		super.setHtmlId(id) ;
		return this ;
	}
	
	/**
	 * An element with the item attribute specified creates a new item, a group 
	 * of name-value pairs.  The attribute, if specified, must have a value that 
	 * is an unordered set of unique space-separated tokens representing the 
	 * types (if any) of the item.
	 */
	@Override
	public DLink setHtmlItem(final String item) {
		super.setHtmlItem(item) ;
		return this ;
	}
	
	/**
	 * An element with the itemprop  attribute specified adds one or more name-value 
	 * pairs to its corresponding item. The itemprop attribute, if specified, must 
	 * have a value that is an unordered set of unique space-separated tokens 
	 * representing the names of the name-value pairs that it adds. The attribute's 
	 * value must have at least one token.
	 */
	@Override
	public DLink setHtmlItemProp(final String itemProp) {
		super.setHtmlItemProp(itemProp) ;
		return this ;
	}	
	
	/**
	 * The lang attribute (in no namespace) specifies the primary language for 
	 * the element's contents and for any of the element's attributes that contain 
	 * text. Its value must be a valid BCP 47 language code, or the empty string.
	 */
	@Override
	public DLink setHtmlLang(final String lang) {
		super.setHtmlLang(lang) ;
		return this ;
	}
	
	/**
	 * The spellcheck  attribute is an enumerated attribute whose keywords are 
	 * the empty string, true and false. The empty string and the true keyword 
	 * map to the true state. The false keyword maps to the false state. In 
	 * addition, there is a third state, the default state, which is the missing 
	 * value default (and the invalid value default).
	 */
	@Override
	public DLink setHtmlSpellCheck(final String spellCheck) {
		super.setHtmlSpellCheck(spellCheck);
		return this ;
	}
	/**
	 * The spellcheck  attribute is an enumerated attribute whose keywords are 
	 * the empty string, true and false. The empty string and the true keyword 
	 * map to the true state. The false keyword maps to the false state. In 
	 * addition, there is a third state, the default state, which is the missing 
	 * value default (and the invalid value default).
	 */
	@Override
	public DLink setHtmlSpellCheck(final boolean spellCheck) {
		super.setHtmlSpellCheck(spellCheck) ;
		return this ;
	}	

	@Override
	public DLink setHtmlStyleAsString(final String styleString) {
		super.setHtmlStyleAsString(styleString) ;
		return this;
	}
	/** Set the style.
	 * This will make a copy of the contents, so further changes to the
	 * style object will not be reflected.
	 */
	@Override
	public DLink setHtmlStyle(final ICssStyleDeclaration style) {
		super.setHtmlStyle(style) ;
		return this;
	}	
	
	/**
	 * The subject  attribute may be specified on any HTML element to associate 
	 * the element with an element with an item attribute. If the subject 
	 * attribute is specified, the attribute's value must be the ID of an element 
	 * with an item attribute, in the same Document as the element with the 
	 * subject attribute.
	 */
	@Override
	public DLink setHtmlSubject(final String subject) {
		super.setHtmlSubject(subject) ;
		return this ;
	}	
	
	/**
	 * The tabindex content attribute specifies whether the element is focusable, 
	 * whether it can be reached using sequential focus navigation, and the relative 
	 * order of the element for the purposes of sequential focus navigation. The 
	 * name "tab index" comes from the common use of the "tab" key to navigate 
	 * through the focusable elements. The term "tabbing" refers to moving forward 
	 * through the focusable elements that can be reached using sequential focus 
	 * navigation.
	 */
	@Override
	public DLink setHtmlTabIndex(final String tabIndex) {  // HTML 5.0
		super.setHtmlTabIndex(tabIndex) ;
		return this ;
	}
	/**
	 * The tabindex content attribute specifies whether the element is focusable, 
	 * whether it can be reached using sequential focus navigation, and the relative 
	 * order of the element for the purposes of sequential focus navigation. The 
	 * name "tab index" comes from the common use of the "tab" key to navigate 
	 * through the focusable elements. The term "tabbing" refers to moving forward 
	 * through the focusable elements that can be reached using sequential focus 
	 * navigation.
	 */
	@Override
	public DLink setHtmlTabIndex(final int tabIndex) {  // HTML 5.0
		super.setHtmlTabIndex(tabIndex) ;
		return this ;
	}
	
	/**
	 * The title attribute represents advisory information for the element, such 
	 * as would be appropriate for a tooltip. On a link, this could be the title 
	 * or a description of the target resource; on an image, it could be the image 
	 * credit or a description of the image; on a paragraph, it could be a footnote 
	 * or commentary on the text; on a citation, it could be further information 
	 * about the source; and so forth. The value is text.
	 * <p>If this attribute is omitted from an element, then it implies that the 
	 * title attribute of the nearest ancestor HTML element with a title attribute 
	 * set is also relevant to this element.
	 */
	@Override
	public DLink setHtmlTitle(final String title) {
		super.setHtmlTitle(title) ;
		return this ;
	}
	
	//
	// HTML 5.0 API - Events
	//
	/**
	 * The user agent stops fetching the media data before it is completely downloaded.
	 */
	@Override
	public DLink setHtmlOnAbort(final String script) {
		super.setHtmlOnAbort(script) ;
		return this ;
	}
	
	/**
	 * The onblur event occurs when an object loses focus.
	 * not supported on BODY
	 */
	@Override
	public DLink setHtmlOnBlur(final String onBlur) {
		super.setHtmlOnBlur(onBlur) ;
		return this ;
	}
	
	/**
	 * The user agent can resume playback of the media data, but estimates that 
	 * if playback were to be started now, the media resource could not be rendered 
	 * at the current playback rate up to its end without having to stop for 
	 * further buffering of content.
	 */
	@Override
	public DLink setHtmlOnCanPlay(final String script) {
		super.setHtmlOnCanPlay(script) ;
		return this ;
	}
	
	/**
	 *  The user agent estimates that if playback were to be started now, the 
	 *  media resource could be rendered at the current playback rate all the way 
	 *  to its end without having to stop for further buffering. 
	 */
	@Override
	public DLink setHtmlOnCanPlayThrough(final String script) {
		super.setHtmlOnCanPlayThrough(script) ;
		return this ;
	}
	
	// onchange
	@Override
	public DLink setHtmlOnChange(final String script) {
		super.setHtmlOnChange(script) ;
		return this ;
	}
	
	// onclick
	@Override
	public DLink setHtmlOnClick(final String script) {
		super.setHtmlOnClick(script) ;
		return this ;
	}

	// oncontextmenu
	@Override
	public DLink setHtmlOnContextMenu(final String script) {
		super.setHtmlOnContextMenu(script) ;
		return this ;
	}
	
	// ondblclick
	@Override
	public DLink setHtmlOnDblClick(final String script) {
		super.setHtmlOnDblClick(script) ;
		return this ;
	}
	
	// ondrag
	@Override
	public DLink setHtmlOnDrag(final String script) {
		super.setHtmlOnDrag(script) ;
		return this ;
	}
	
	// ondragend
	@Override
	public DLink setHtmlOnDragEnd(final String script) {
		super.setHtmlOnDragEnd(script) ;
		return this ;
	}
	
	// ondragenter
	@Override
	public DLink setHtmlOnDragEnter(final String script) {
		super.setHtmlOnDragEnter(script) ;
		return this ;
	}
	
	// ondragleave
	@Override
	public DLink setHtmlOnDragLeave(final String script) {
		super.setHtmlOnDragLeave(script) ;
		return this ;
	}
	
	// ondragover
	@Override
	public DLink setHtmlOnDragOver(final String script) {
		super.setHtmlOnDragOver(script) ;
		return this ;
	}
	
	// ondragstart
	@Override
	public DLink setHtmlOnDragStart(final String script) {
		super.setHtmlOnDragStart(script) ;
		return this ;
	}
	
	// ondrop
	@Override
	public DLink setHtmlOnDrop(final String script) {
		super.setHtmlOnDrop(script) ;
		return this ;
	}
	
	// ondurationchange
	@Override
	public DLink setHtmlOnDurationChange(final String script) {
		super.setHtmlOnDurationChange(script) ;
		return this ;
	}
	
	/**
	 *  A media element whose networkState was previously not in the 
	 *  NETWORK_EMPTY state has just switched to that state (either because of a
	 *  fatal error during load that's about to be reported, or because the 
	 *  load() method was invoked while the resource selection algorithm was 
	 *  already running, in which case it is fired synchronously during the 
	 *  load() method call). 
	 */
	@Override
	public DLink setHtmlOnEmptied(final String script) {
		super.setHtmlOnEmptied(script) ;
		return this ;
	}
	
	/**
	 * Playback has stopped because the end of the media resource was reached. 
	 */
	@Override
	public DLink setHtmlOnEnded(final String script) {
		super.setHtmlOnEnded(script) ;
		return this ;
	}
	
	/**
	 * An error occurs while fetching the media data. 
	 * not supported on BODY
	 */
	@Override
	public DLink setHtmlOnError(final String script) {
		super.setHtmlOnError(script) ;
		return this ;
	}

	/**
	 * onfocus - not supported on BODY
	 */
	@Override
	public DLink setHtmlOnFocus(final String script) {
		super.setHtmlOnFocus(script) ;
		return this ;
	}
	
	/**
	 * onformchange
	 */
	@Override
	public DLink setHtmlOnFormChange(final String script) {
		super.setHtmlOnFormChange(script) ;
		return this ;
	}
	
	/**
	 * onforminput
	 */
	@Override
	public DLink setHtmlOnFormInput(final String script) {
		super.setHtmlOnFormInput(script) ;
		return this ;
	}
	
	/**
	 * oninput
	 */
	@Override
	public DLink setHtmlOnInput(final String script) {
		super.setHtmlOnInput(script) ;
		return this ;
	}
	
	/**
	 * oninvalid
	 */
	@Override
	public DLink setHtmlOnInvalid(final String script) {
		super.setHtmlOnInvalid(script) ;
		return this ;
	}
	
	/**
	 * onkeydown
	 */
	@Override
	public DLink setHtmlOnKeyDown(final String script) {
		super.setHtmlOnKeyDown(script) ;
		return this ;
	}

	/**
	 * onkeypress
	 */
	@Override
	public DLink setHtmlOnKeyPress(final String script) {
		super.setHtmlOnKeyPress(script) ;
		return this ;
	}
	
	/**
	 * onkeyup
	 */
	@Override
    public DLink setHtmlOnKeyUp(final String script) {
    	super.setHtmlOnKeyUp(script);
    	return this ;
	}
    
	/**
	 * onload
	 */
	@Override
    public DLink setHtmlOnLoad(final String script) {
    	super.setHtmlOnLoad(script) ;
    	return this ;
	}
    
	/**
	 * onloadeddata
	 */
	@Override
    public DLink setHtmlOnLoadedData(final String script) {
    	super.setHtmlOnLoadedData(script) ;
    	return this ;
	}   
    
	/**
	 * onloadedmetadata
	 */
	@Override
    public DLink setHtmlOnLoadedMetadata(final String script) {
    	super.setHtmlOnLoadedMetadata(script) ;
    	return this ;
	}  
	
	/**
	 * onloadstart
	 */
	@Override
    public DLink setHtmlOnLoadStart(final String script) {
    	super.setHtmlOnLoadStart(script) ;
    	return this ;
	} 
    
	/**
	 * onmousedown
	 */
	@Override
	public DLink setHtmlOnMouseDown(final String script){
		super.setHtmlOnMouseDown(script) ;
		return this ;
	}
	
	/**
	 * onmousemove
	 */
	@Override
	public DLink setHtmlOnMouseMove(final String script) {
		super.setHtmlOnMouseMove(script) ;
		return this ;
	}
	
	/**
	 * onmouseout
	 */
	@Override
	public DLink setHtmlOnMouseOut(final String script) {
		super.setHtmlOnMouseOut(script) ;
		return this ;
	}
	
	/**
	 * onmouseover
	 */
	@Override
	public DLink setHtmlOnMouseOver(final String script) {
		super.setHtmlOnMouseOver(script) ;
		return this ;
	}
	
	/**
	 * onmouseup
	 */
	@Override
	public DLink setHtmlOnMouseUp(final String script) {
		super.setHtmlOnMouseUp(script) ;
		return this ;
	}
    
	/**
	 * onmousewheel
	 */
	@Override
	public DLink setHtmlOnMouseWheel(final String script) {
		super.setHtmlOnMouseWheel(script) ;
		return this ;
	}
    
	/**
	 * onpause
	 */
	@Override
	public DLink setHtmlOnPause(final String script) {
		super.setHtmlOnPause(script) ;
		return this ;
	}  
    
	/**
	 * onplay
	 */
	@Override
	public DLink setHtmlOnPlay(final String script) {
		super.setHtmlOnPlay(script) ;
		return this ;
	}
    
	/**
	 * onplaying
	 */
	@Override
	public DLink setHtmlOnPlaying(final String script) {
		super.setHtmlOnPlaying(script) ;
		return this ;
	}
	
	/**
	 * onprogress
	 */
	@Override
	public DLink setHtmlOnProgress(final String script) {
		super.setHtmlOnProgress(script) ;
		return this ;
	}
    
	/**
	 * onratechange
	 */
	@Override
	public DLink setHtmlOnRateChange(final String script) {
		super.setHtmlOnRateChange(script) ;
		return this ;
	}
    
	/**
	 * onreadystatechange
	 */
	@Override
	public DLink setHtmlOnReadyStateChange(final String script) {
		super.setHtmlOnReadyStateChange(script) ;
		return this ;
	}

	/**
	 * onscroll
	 */
	@Override
	public DLink setHtmlOnScroll(final String script) {
		super.setHtmlOnScroll(script) ;
		return this ;
	}
	
	/**
	 * onseeked
	 */
	@Override
	public DLink setHtmlOnSeeked(final String script) {
		super.setHtmlOnSeeked(script) ;
		return this ;
	}
    
	/**
	 * onseeking
	 */
	@Override
	public DLink setHtmlOnSeeking(final String script) {
		super.setHtmlOnSeeking(script) ;
		return this ;
	}
	
	/**
	 * onselect
	 */
	@Override
	public DLink setHtmlOnSelect(final String script) {
		super.setHtmlOnSelect(script) ;
		return this ;
	}
	
	/**
	 * onshow
	 */
	@Override
	public DLink setHtmlOnShow(final String script) {
		super.setHtmlOnShow(script) ;
		return this ;
	}
	
	/**
	 * onstalled
	 */
	@Override
	public DLink setHtmlOnStalled(final String script) {
		super.setHtmlOnStalled(script) ;
		return this ;
	}
	
	/**
	 * onsubmit
	 */
	@Override
	public DLink setHtmlOnSubmit(final String script) {
		super.setHtmlOnSubmit(script) ;
		return this ;
	}
	
	/**
	 * onsuspend
	 */
	@Override
	public DLink setHtmlOnSuspend(final String script) {
		super.setHtmlOnSuspend(script) ;
		return this ;
	}
	
	/**
	 * ontimeupdate
	 */
	@Override
	public DLink setHtmlOnTimeUpdate(final String script) {
		super.setHtmlOnTimeUpdate(script) ;
		return this ;
	}
	
	/**
	 * onvolumechange
	 */
	@Override
	public DLink setHtmlOnVolumeChange(final String script) {
		super.setHtmlOnVolumeChange(script) ;
		return this ;
	}
	
	/**
	 * onwaiting
	 */
	@Override
	public DLink setHtmlOnWaiting(final String script) {
		super.setHtmlOnWaiting(script) ;
		return this ;
	}
	
	//
	// Framework - Event Wiring
	//
	@Override
	public DLink add(
		final EventType eventType, 
		final ISimpleJsEventHandler handler)
	{
		super.add(eventType, handler) ;
		return this ;
	}
	
	@Override
	public DLink add(
		final EventType eventType, 
		final IJsFunc func)
	{
		super.add(eventType, func) ;
		return this ;
	}
	
	@Override
	public DLink add(
		final EventType eventType, 
		final String jsText)
	{
		super.add(eventType, jsText) ;
		return this ;
	}
	
//	@Override
//	public DLink add(final IDomActiveListener listener){
//		super.add(listener) ;
//		return this ;
//	}
//	
//	@Override
//	public DLink add(
//		final EventType eventType, final IDomActiveListener listener)
//	{
//		super.add(eventType, listener) ;
//		return this ;
//	}
//	
//	@Override
//	public DLink removeListener(
//		final EventType eventType, final IDomActiveListener listener)
//	{
//		super.removeListener(eventType, listener) ;
//		return this ;
//	}
	
	//
	// Helpers
	//
	@Override
	public DLink addBr() {
		super.addBr() ;
		return this ;
	}
	
	@Override
	public DLink addBr(final int howMany){
		super.addBr(howMany) ;
		return this ;
	}

	/**
	 * Adds a class to the end, does not overwrite, and the classes are space
	 * delimited.
	 */
	@Override
	public DLink addHtmlClassName(final String className) {
		super.addHtmlClassName(className) ;
		return this ;
	}
	
	@Override
	public DLink addHtmlClassName(final CssClassConstant ccc) {
		super.addHtmlClassName(ccc) ;
		return this ;
	}
	
	@Override
	public DLink jif(final String jif) { 
		super.jif(jif) ;
		return this ;
	}
	
	//
	// Child Hooks (none)
	//
}

