package cc.creativecomputing.xml;

import java.util.*;
import java.io.*;

/**
 * CCXMLElement is the basic class to work with xml. You can build a CCXMLElement and add 
 * attributes and children, or load it from a certain path using CCXMLIO. Text
 * is also handled as CCXMLElement, so if you want to get the text of an element
 * you have to call element.firstChild().text(). If you have a part where
 * XML nodes are inside text they separate the text into several elements, so for
 * example &quot;this is &lt;bold&gt;bold&lt;/bold&gt; ..&quot; would result in the following
 * element list: &quot;this is &quot;,&lt;bold&gt;,&quot;bold&quot;,&lt;/bold&gt;,&quot; ..&quot;
 * @see CCXMLIO
 */

public class CCXMLElement implements Iterable<CCXMLElement>{

	/**
	 * Holds the values and keys of the elements attributes.
	 */
	Map<String,String> _myAttributes;

	/**
	 * Vector keeping the children of this Element
	 */
	private List<CCXMLElement> _myChildren;

	/**
	 * true if this element is empty 
	 */
	private boolean _myIsEmpty = true;

	/**
	 * true if this element is a pcdata section
	 */
	public boolean _myIsPCData;

	public boolean cdata = false;

	/**
	 * Holds the parent of this Element
	 */
	private CCXMLElement _myParent;

	/**
	 * String holding the kind of the Element (the tagname)
	 */
	private String _myName;
	
	private int _myLine = -1;

	/**
	 * Initializes a new CCXMLElement with the given name, attributes and children.
	 * @param theName String, name of the element
	 * @param theAttributes Map, attributes for the element, with names and values
	 * @param theChildren List, the children of the element
	 * @param theIsPCData boolean, true if the element is a pcdata section
	 */
	private CCXMLElement(
		final String theName, 
		final Map<String,String> theAttributes, 
		final List<CCXMLElement> theChildren, 
		final boolean theIsPCData
	){
		_myName = theName;
		_myAttributes = theAttributes;
		_myChildren = theChildren;
		_myIsPCData = theIsPCData;
	}
	
	/**
	 * Returns the line of this element in the xml document. Can be used for debugging.
	 * TODO this needs to be implemented
	 * @return the line of this element in the xml document
	 */
	public int line() {
		return _myLine;
	}

	/**
	 * Initializes a new CCXMLElement with the given name.
	 * @param theName String, name of the element
	 * @param theIsPCData boolean, true if the element is a pcdata section
	 */
	public CCXMLElement(
		final String theName, 
		final boolean theIsPCData
	){
		this(theName, new HashMap<String,String>(), new ArrayList<CCXMLElement>(), theIsPCData);
	}

	/**
	 * Initializes a new CCXMLElement with the given name, attributes and children.
	 * @param theName String, name of the element
	 * @param theAttributes Map, attributes for the element, with names and values
	 * @param theChildren List, the children of the element
	 */
	public CCXMLElement(final String theName, final Map<String,String> theAttributes, final List<CCXMLElement> theChildren){
		this(theName, theAttributes, theChildren, false);
	}

	/**
	 * Initializes a CCXMLElement with the given name, but without children and attributes.
	 * @param theName String, name of the element
	 */

	public CCXMLElement(final String theName){
		this(theName, new HashMap<String,String>(), new ArrayList<CCXMLElement>());
	}

	/**
	 * Initializes a CCXMLElement with the given name and children.
	 * @param theName String, name of the element
	 * @param theChildren Vector, children of the element
	 */

	public CCXMLElement(final String theName, final List<CCXMLElement> theChildren){
		this(theName, new HashMap<String,String>(), theChildren);
	}

	/**
	 * Initializes a new CCXMLElement with the given name and attributes.
	 * @param theName String, name of the element
	 * @param theAttributes Map, attributes of the element, with names and values
	 */

	public CCXMLElement(final String theName, final Map<String,String> theAttributes){
		this(theName, theAttributes, new ArrayList<CCXMLElement>());
	}

	/**
	 * Checks if a Vector has Content
	 * @param toCheck Vector
	 * @return boolean
	 */
	private boolean has(List<CCXMLElement> toCheck){
		if (toCheck.isEmpty() || toCheck == null){
			return false;
		}else{
			return true;
		}
	}

	/**
	 * Use this method to check if the CCXMLElement is a text element. 
	 * @return boolean, true if the CCXMLElement is a PCDATA section.
	 * @shortdesc Checks if a CCXMLElement is a text element
	 * @see #text()
	 */
	public boolean isTextElement(){
		return _myIsPCData;
	}
	
	/**
	 * Use this function to check if the element is a cdata section.
	 * @return boolean, true if the CCXMLElement is a CDATA section
	 */
	public boolean isCDATASection(){
		return cdata;
	}

	/**
	 * @deprecated
	 * Use this method to get the name of a CCXMLElement. If the CCXMLElement is a 
	 * PCDATA section getElement() gives you its text.
	 * @return String, the name of the element or the text if it is a text element
	 * @shortdesc Use this method to get the name or text of an CCXMLElement. 
	 * @see #isTextElement ( )
	 */
	public String getName(){
		return _myName;
	}

	/**
	 * Returns the name of the XML object this is the name of the 
	 * tag that represents the element in the XML file. For example, TITLE is the elementName 
	 * of an HTML TITLE tag. If the XML object is a text element this method returns null.
	 * @return String the name of the element
	 * @shortdesc Returns the name of the element.
	 */
	public String name(){
		if (isTextElement())
			return null;
		return _myName;
	}

	/**
	 * If the XML object is a text element this method return the text of the element, otherwise
	 * it returns null.
	 * @return String, the text of a text element
	 * @shortdesc Returns the text of the element.
	 */
	public String text(){
		if (isTextElement())
			return _myName;
		return null;
	}
	
	/**
	 * Returns the text content of a XML element. If there is none available
	 * this methods returns an empty String.
	 * @return text content of an XML element.
	 */
	public String content() {
		return content("");
	}
	
	/**
	 * Returns the text content of a XML element. If there is none available
	 * this methods returns the given default string.
	 * @param theDefault default value in case there is no content
	 * @return text content of an XML element.
	 */
	public String content(final String theDefault) {
		try {
			return _myChildren.get(0).text();
		} catch (Exception e) {
			return theDefault;
		}
	}
	
	/**
	 * Returns the text content of a XML element as int value. 
	 * If there is none available or the content is no int value
	 * this methods returns the given default value.
	 * @param theDefaultValue in case that no content can be read
	 * @return content of the xml element as int value
	 */
	public int intContent(int theDefaultValue) {
		try {
			return Integer.parseInt(_myChildren.get(0).text());
		} catch (Exception e) {
			return theDefaultValue;
		}
	}
	
	/**
	 * Returns the text content of a XML element as int value. 
	 * If there is none available or the content is no int value
	 * this methods returns 0.
	 * @return content of the xml element as int value
	 */
	public int intContent() {
		return intContent(0);
	}
	
	/**
	 * Returns the text content of a XML element as float value. 
	 * If there is none available or the content is no float value
	 * this methods returns the given default value.
	 * @param theDefaultValue in case that no content can be read
	 * @return content of the xml element as float value
	 */
	public float floatContent(float theDefaultValue) {
		try {
			return Integer.parseInt(_myChildren.get(0).text());
		} catch (Exception e) {
			return theDefaultValue;
		}
	}
	
	/**
	 * Returns the text content of a XML element as float value. 
	 * If there is none available or the content is no float value
	 * this methods returns 0.
	 * @return content of the xml element as float value
	 */
	public float floatContent() {
		return floatContent(0);
	}
	
	/**
	 * Returns the text content of a XML element as boolean value. 
	 * If there is none available or the content is no boolean value
	 * this methods returns the given default value.
	 * @param theDefaultValue in case that no content can be read
	 * @return content of the xml element as boolean value
	 */
	public boolean booleanContent(boolean theDefaultValue) {
		try {
			return Boolean.parseBoolean(_myChildren.get(0).text());
		} catch (Exception e) {
			return theDefaultValue;
		}
	}
	
	/**
	 * Returns the text content of a XML element as boolean value. 
	 * If there is none available or the content is no float value
	 * this methods returns false.
	 * @return content of the xml element as boolean value
	 */
	public boolean booleanContent() {
		return booleanContent(false);
	}

	/**
	 * Returns a String Array with all attribute names of an Element. Use 
	 * getAttribute() to get the value for an attribute.
	 * @return Collection with the Attributes of an Element
	 * @shortdesc Returns a String Array with all attribute names of an Element.
	 * @see #attribute(String)
	 * @see #intAttribute(String)
	 * @see #floatAttribute(String)
	 * @see #hasAttributes()
	 * @see #hasAttribute(String)
	 * @see #countAttributes()
	 * @see #addAttribute(String, String)
	 */
	public Collection<String> attributes(){
		return _myAttributes.keySet();
	}

	/**
	 * Use attribute() to get the value of an attribute as a string. If your are
	 * sure, the value is an int or a float value you can also use intAttribute() or 
	 * floatAttribute() to get the numeric value without a cast.
	 * @param theKey String, the name of the attribute you want the value of
	 * @return String, the value to the given attribute
	 * @shortdesc Returns the value of a given attribute.
	 * @see #attributes()
	 * @see #intAttribute(String)
	 * @see #floatAttribute(String)
	 * @see #hasAttributes()
	 * @see #hasAttribute(String)
	 * @see #countAttributes()
	 * @see #addAttribute(String, String)
	 */
	public String attribute(final String theKey){
		return _myAttributes.get(theKey);
	}
	
	public String attribute(final String theKey, final String theDefaultValue){
		String result =  _myAttributes.get(theKey);
		if (result == null)
			return theDefaultValue;
		return result;
	}

	/**
	 * Use intAttribute() to get the value of an attribute as int value. You 
	 * can only use this method on attributes that are numeric, otherwise you get 
	 * a InvalidAttributeException. 
	 * @param key String, the name of the attribute you want the value of
	 * @return int, the value of the attribute
	 * @shortdesc Use intAttribute() to get the value of an attribute as int value.
	 * @see #attributes()
	 * @see #attribute(String)
	 * @see #hasAttribute(String)
	 * @see #hasAttributes()
	 * @see #hasAttribute(String)
	 * @see #countAttributes()
	 * @see #addAttribute(String, String)
	 */
	public int intAttribute(String key){
		String attributeValue =  _myAttributes.get(key);
		if (attributeValue == null)
			throw new CCXMLInvalidAttributeException(this._myName, key);
		try{
			return Integer.parseInt( _myAttributes.get(key));
		}catch (NumberFormatException e){
			throw new CCXMLInvalidAttributeException(this._myName, key, "int");
		}
	}
	
	public int intAttribute(String key, final int theDefaultValue){
		String attributeValue =  _myAttributes.get(key);
		if (attributeValue == null)
			return theDefaultValue;
		try{
			return Integer.parseInt( _myAttributes.get(key));
		}catch (NumberFormatException e){
			return theDefaultValue;
		}
	}
	
	/**
	 * Use getBooleanAttribute() to get the value of an attribute as boolean value. You 
	 * can only use this method on attributes that are boolean, otherwise you get 
	 * a InvalidAttributeException. 
	 * @param key String, the name of the attribute you want the value of
	 * @return the value of the attribute
	 * @shortdesc Use intAttribute() to get the value of an attribute as int value.
	 * @see #attributes()
	 * @see #attribute(String)
	 * @see #floatAttribute(String)
	 * @see #hasAttributes()
	 * @see #hasAttribute(String)
	 * @see #countAttributes()
	 * @see #addAttribute(String, String)
	 */
	public Boolean booleanAttribute(String key){
		String attributeValue =  _myAttributes.get(key);
		if (attributeValue == null)
			return null;
		try{
			return Boolean.parseBoolean( _myAttributes.get(key));
		}catch (NumberFormatException e){
			throw new CCXMLInvalidAttributeException(this._myName, key, "boolean");
		}
	}
	
	public boolean booleanAttribute(String key, final boolean theDefaultValue){
		String attributeValue =  _myAttributes.get(key);
		if (attributeValue == null)
			return theDefaultValue;
		try{
			return Boolean.parseBoolean( _myAttributes.get(key));
		}catch (NumberFormatException e){
			return theDefaultValue;
		}
	}

	/**
	 * Use floatAttribute() to get the value of an attribute as float value. You 
	 * can only use this method on attributes that are numeric, otherwise you get 
	 * a InvalidAttributeException. 
	 * @param key String, the name of the attribute you want the value of
	 * @return float, the value of the attribute
	 * @shortdesc Use floatAttribute() to get the value of an attribute as float value.
	 * @see #attributes()
	 * @see #attribute(String)
	 * @see #intAttribute(String)
	 * @see #hasAttributes()
	 * @see #hasAttribute(String)
	 * @see #countAttributes()
	 * @see #addAttribute(String, String)
	 */
	public float floatAttribute(String key){
		String attributeValue = _myAttributes.get(key);
		if (attributeValue == null)
			throw new CCXMLInvalidAttributeException(this._myName, key);
		try{
			return Float.parseFloat(_myAttributes.get(key));
		}catch (NumberFormatException e){
			throw new CCXMLInvalidAttributeException(this._myName, key, "float",e);
		}
	}
	
	public float floatAttribute(String key, final float theDefaultValue){
		String attributeValue =  _myAttributes.get(key);
		if (attributeValue == null)
			return theDefaultValue;
		try{
			return Float.parseFloat( _myAttributes.get(key));
		}catch (NumberFormatException e){
			return theDefaultValue;
		}
	}

	/**
	 * Use doubleAttribute() to get the value of an attribute as double value. You 
	 * can only use this method on attributes that are numeric, otherwise you get 
	 * a InvalidAttributeException. 
	 * @param theKey String, the name of the attribute you want the value of
	 * @return double, the value of the attribute
	 * @see #attributes()
	 * @see #attribute(String)
	 * @see #intAttribute(String)
	 * @see #hasAttributes()
	 * @see #hasAttribute(String)
	 * @see #countAttributes()
	 * @see #addAttribute(String, String)
	 */
	public double doubleAttribute(String theKey){
		String attributeValue = _myAttributes.get(theKey);
		if (attributeValue == null)
			throw new CCXMLInvalidAttributeException(this._myName, theKey);
		try{
			return Double.parseDouble(_myAttributes.get(theKey));
		}catch (NumberFormatException e){
			throw new CCXMLInvalidAttributeException(this._myName, theKey, "float",e);
		}
	}
	
	public double doubleAttribute(String key, final double theDefaultValue){
		String attributeValue =  _myAttributes.get(key);
		if (attributeValue == null)
			return theDefaultValue;
		try{
			return Double.parseDouble( _myAttributes.get(key));
		}catch (NumberFormatException e){
			return theDefaultValue;
		}
	}

	/**
	 * Use this method to check if the CCXMLElement has attributes.
	 * @return boolean, true if the CCXMLElement has attributes
	 * @see #attributes()
	 * @see #attribute(String)
	 * @see #intAttribute(String)
	 * @see #hasAttribute(String)
	 * @see #hasAttribute(String)
	 * @see #countAttributes()
	 * @see #addAttribute(String, String)
	 */
	public boolean hasAttributes(){
		return !_myAttributes.isEmpty();
	}

	/**
	 * This method checks if the CCXMLElement has the given Attribute.
	 * @param key String, attribute you want to check
	 * @return boolean, true if the CCXMLElement has the given attribute
	 * @see #attributes()
	 * @see #attribute(String)
	 * @see #intAttribute(String)
	 * @see #hasAttribute(String)
	 * @see #hasAttributes()
	 * @see #countAttributes()
	 * @see #addAttribute(String, String)
	 */
	public boolean hasAttribute(String key){
		return _myAttributes.containsKey(key);
	}

	/**
	 * Use this method to count the attributes of a CCXMLElement.
	 * @return int, the number of attributes
	 * @see #attributes()
	 * @see #attribute(String)
	 * @see #intAttribute(String)
	 * @see #hasAttribute(String)
	 * @see #hasAttributes()
	 * @see #hasAttribute(String)
	 * @see #addAttribute(String, String)
	 */
	public int countAttributes(){
		return _myAttributes.size();

	}

	/**
	 * With addAttribute() you can add attributes to a CCXMLElement. The value
	 * of attribute can be a String a float or an int. 
	 * @param key String, name of the attribute
	 * @param value String, int or float: value of the attribute
	 * @shortdesc With addAttribute() you can add attributes to a CCXMLElement.
	 * @see #attributes()
	 * @see #attribute(String)
	 * @see #intAttribute(String)
	 * @see #floatAttribute(String)
	 * @see #hasAttributes()
	 * @see #hasAttribute(String)
	 * @see #countAttributes()
	 */
	public void addAttribute(String key, String value){
		if (isTextElement())
			throw new CCXMLInvalidAttributeException(key);
		_myAttributes.put(key, value);
	}

	public void addAttribute(String key, int value){
		addAttribute(key, value + "");
	}

	public void addAttribute(String key, long value){
		addAttribute(key, value + "");
	}

	public void addAttribute(String key, float value){
		addAttribute(key, value + "");
	}

	public void addAttribute(String key, double value){
		addAttribute(key, value + "");
	}

	public void addAttribute(String key, boolean value){
		addAttribute(key, value + "");
	}

	/**
	 * With parent() you can get the parent of a CCXMLElement. If the 
	 * CCXMLElement is the root element it returns null.
	 * @return CCXMLElement, the parent of the CCXMLElement or null 
	 * if the CCXMLElement is the root element
	 * @shortdesc With getParent() you can get the parent of a CCXMLElement.
	 * @see #addChild(CCXMLElement)
	 * @see #countChildren()
	 * @see #child(String)
	 * @see #children()
	 * @see #hasChildren()
	 */
	public CCXMLElement parent(){
		return _myParent;
	}

	/**
	 * Use children() to get an array with all children of an element. 
	 * Each element in the array is a reference to an XML object that represents 
	 * a child element.
	 * @return a list of elements
	 * @shortdesc Returns an Array with all the children of an element.
	 * @see #addChild(CCXMLElement)
	 * @see #countChildren()
	 * @see #child(int)
	 * @see #parent()
	 * @see #hasChildren()
	 */
	public List<CCXMLElement> children(){
		return _myChildren;
	}

	/**
	 * Evaluates the specified XML element and references the first child in the 
	 * parent element's child list or null if the element does not 
	 * have children.
	 * @return CCXMLElement, the first child element
	 * @shortdesc Returns the first child of the element.
	 */
	public CCXMLElement firstChild(){
		if (hasChildren())
			return child(0);
		else
			return null;
	}

	/**
	 * Returns the last child in the element's child list or null if the element does 
	 * not have children.
	 * @return CCXMLElement, the last child of the element
	 * @shortdesc Returns the last child of the element.
	 */
	public CCXMLElement lastChild(){
		if (hasChildren())
			return child(countChildren() - 1);
		else
			return null;
	}

	/**
	 * Returns the next sibling in the parent elements's child list or null if the node does 
	 * not have a next sibling element.
	 * @return CCXMLElement, the next sibling of the element
	 * @shortdesc Returns the next sibling of the element.
	 */
	public CCXMLElement nextSibling(){
		if (_myParent == null)
			return null;

		final int index = _myParent._myChildren.indexOf(this);

		if (index < _myParent.countChildren() - 1){
			return _myParent.child(index + 1);
		}

		return null;
	}

	/**
	 * Returns the previous sibling in the parent node's child list or null if the node does 
	 * not have a previous sibling node.
	 * @return CCXMLElement, the previous sibling of the element.
	 * @shortdesc Returns the previous sibling of the element.
	 */
	public CCXMLElement previousSibling(){
		if (_myParent == null)
			return null;

		final int index = _myParent._myChildren.indexOf(this);

		if (index > 0){
			return _myParent.child(index - 1);
		}

		return null;
	}

	/**
	 * Use child() to get a certain child element of a CCXMLElement. 
	 * With countAllChildren() you get the number of all children.
	 * @param theIndex int, number of the child
	 * @return CCXMLElement, the child
	 * @shortdesc Use getChild() to get a certain child element of a CCXMLElement.
	 * @see #addChild(CCXMLElement)
	 * @see #countChildren()
	 * @see #children()
	 * @see #parent()
	 * @see #hasChildren()
	 */
	public CCXMLElement child(final int theIndex){
		return _myChildren.get(theIndex);
	}

	/**
	 * Specifies whether or not the XML object has child nodes.
	 * @return boolean, true if the specified CCXMLElement has one or more child nodes; otherwise false.
	 * @see #addChild(CCXMLElement)
	 * @see #countChildren()
	 * @see #child(String)
	 * @see #children()
	 * @see #parent()
	 */
	public boolean hasChildren(){
		return has(_myChildren);
	}

	/**
	 * With countChildren() you get the number of children of a CCXMLElement.
	 * @return int, the number of children
	 * @see #addChild(CCXMLElement)
	 * @see #child(String)
	 * @see #children()
	 * @see #parent()
	 * @see #hasChildren()
	 */
	public int countChildren(){
		return _myChildren.size();
	}

	/**
	 * Adds the specified node to the XML element's child list. This method
	 * operates directly on the element referenced by the childElement parameter; it
	 * does not append a copy of the element. If the element to be added already
	 * exists in another tree structure, appending the element to the new
	 * location will remove it from its current location. If the childElement
	 * parameter refers to a element that already exists in another XML tree
	 * structure, the appended child element is placed in the new tree structure
	 * after it is removed from its existing parent element.
	 * 
	 * @param element CCXMLElement, element you want to add as child
	 * @shortdesc Adds the specified node to the XML element's child list.
	 * @see #addChild(CCXMLElement)
	 * @see #countChildren()
	 * @see #children()
	 * @see #parent()
	 * @see #hasChildren()
	 */
	public void addChild(CCXMLElement element){
		_myIsEmpty = false;
		element._myParent = this;
		_myChildren.add(element);
	}

	/**
	 * @param element CCXMLElement, element you want to add as child
	 * @param position int, position where you want to insert the element
	 */
	public void addChild(CCXMLElement element, int position){
		_myIsEmpty = false;
		element._myParent = this;
		_myChildren.add(position, element);
	}
	
	/**
	 * Shortcut to add an xml node with text content. Note that the
	 * created element is automatically added as child to node.
	 * @param theElementName name of the element to create
	 * @return the create element
	 */
	public CCXMLElement createChild(String theElementName){
		_myIsEmpty = false;
		CCXMLElement myElement = new CCXMLElement(theElementName);
		addChild(myElement);
		return myElement;
	}
	
	/**
	 * Shortcut to add an xml node with text content. Note that the
	 * created element is automatically added as child to node.
	 * @param theElementName name of the element to create
	 * @param theContent content to put into the element
	 * @return the create element
	 */
	public CCXMLElement createChild(String theElementName, String theContent){
		_myIsEmpty = false;
		CCXMLElement myElement = new CCXMLElement(theElementName);
		myElement.addText(theContent);
		addChild(myElement);
		return myElement;
	}
	
	public CCXMLElement createChild(String theElementName, int theContent){
		return createChild(theElementName, "" + theContent);
	}
	
	public CCXMLElement createChild(String theElementName, float theContent){
		return createChild(theElementName, "" + theContent);
	}
	
	public CCXMLElement createChild(String theElementName, double theContent){
		return createChild(theElementName, "" + theContent);
	}
	
	public CCXMLElement createChild(String theElementName, boolean theContent){
		return createChild(theElementName, "" + theContent);
	}
	
	
	public void addText(final String theText){
		addChild(new CCXMLElement (theText,true));
	}

	/**
	 * Removes the specified XML element from its parent. Also deletes all descendants of the element.
	 * @param childNumber int, the number of the child to remove
	 * @shortdesc Removes the specified XML element from its parent.
	 * @see #addChild(CCXMLElement)
	 * @see #countChildren()
	 */
	public void removeChild(int childNumber){
		_myChildren.remove(childNumber);
		_myIsEmpty = _myChildren.size() == 0;
	}

	/**
	 * Use getDepth to get the maximum depth of an Element to one of its leaves.
	 * @return int, the maximum depth of an Element to one of its leaves
	 * @see countAllChildren ( )
	 * @see #countAttributes()
	 * @see countChildren ( )
	 */
	public int getDepth(){
		int result = 0;
		for (CCXMLElement myChild:_myChildren){
			result = Math.max(result, myChild.getDepth());
		}
		return 1 + result;
	}

	/**
	 * This method returns the number of all nodes of a CCXMLElement.
	 * @return int, the number of all descendants of an Element
	 * @see #parent()
	 * @see #getDepth()
	 * @see #countAttributes()
	 * @see #countChildren()
	 */
	public int countAllChildren(){
		int result = 0;
		for (CCXMLElement myChild:_myChildren){
			result += myChild.countAllChildren();
		}
		return 1 + result;
	}

	/**
	 * Returns a list of child elements that have the supplied node name.
	 * @param theNodeName String
	 * @return list of child elements that have the supplied node name
	 */
	public List<CCXMLElement> children(final String theNodeName){
		List<CCXMLElement> result = new ArrayList<CCXMLElement>();
		for (final CCXMLElement myChild:_myChildren){
//			if (!myChild.isTextElement()){
//				result.addAll(myChild.children(theNodeName));
//			}
			if (myChild._myName.equals(theNodeName)){
				result.add(myChild);
			}
		}
		return result;
	}

	/**
	 * Returns the first child node with the given node name. If there
	 * is no such child node the method returns null.
	 * @param theNodeName String
	 * @return CCXMLElement
	 */
	public CCXMLElement child(final String theNodeName){
		for (CCXMLElement myChild:_myChildren){
			if (myChild._myName.equals(theNodeName)){
				return myChild;
			}
		}
		return null;
	}
	
	public CCXMLElement child(final String theNodeName, final String theAttribute, final String theValue){
		for (CCXMLElement myChild:_myChildren){
			if (myChild._myName.equals(theNodeName) && myChild.hasAttribute(theAttribute) && myChild.attribute(theAttribute).equals(theValue)){
				return myChild;
			}
		}
		return null;
	}

	/**
	 * Use toString to get the String representation of a CCXMLElement. The 
	 * method gives you the start tag with the name and its attributes, or its text if 
	 * it is a PCDATA section.
	 * @return String, String representation of the CCXMLElement
	 * @shortdesc Use toString to get the String representation of a CCXMLElement.
	 * @see #printElementTree()
	 * @see #getName()
	 * @see #isTextElement()
	 */
	public String toString(){
		if (isTextElement()){
			if (this.cdata){
				final StringBuffer result = new StringBuffer();
				result.append("<![CDATA[");
				result.append(name());
				result.append("]]>");
				return result.toString();
			}else{
				String result = _myName;
				result = result.replaceAll("&", "&amp;");
				result = result.replaceAll("<", "&lt;");
				result = result.replaceAll(">", "&gt;");
				result = result.replaceAll("\"", "&quot;");
				result = result.replaceAll("'", "&apos;");
				return result;
			}
		}
		final StringBuffer result = new StringBuffer();
		result.append("<");
		result.append(_myName);
		for (String key:_myAttributes.keySet()){
			result.append(" ");
			result.append(key);
			result.append("=\"");
			result.append(_myAttributes.get(key));
			result.append("\"");
		}
		if (_myIsEmpty)
			result.append("/>");
		else
			result.append(">");
		return result.toString();
	}

	/**
	 * Use this method for a simple trace of the XML structure, 
	 * beginning at a certain CCXMLElement.
	 * @shortdesc Prints out the XML  content of the element.
	 * @see #toString()
	 */
	public void printElementTree(){
		printElementTree(" ");
	}

	/**
	 * @param dist String, String for formating the output
	 */
	public void printElementTree(String dist){
		printElementTree("", dist);
	}

	/**
	 * @param start String, String to put before the element
	 */
	void printElementTree(String start, String dist){
		System.out.println(start + this);
		for (int i = 0; i < _myChildren.size(); i++){
			((CCXMLElement) _myChildren.get(i)).printElementTree(start + dist, dist);
		}
		if (!_myIsEmpty){
			System.out.println(start + "</" + _myName + ">");
		}
	}

	/**
	 * Prints the tree of this Element with the given distance
	 * 
	 * @param dist String
	 * @param output PrintWriter
	 */
	void printElementTree(PrintWriter output, String dist){
		printElementTree(output, "", dist);
	}

	/**
	 * Prints the tree of this Element with the given distance and start string.
	 * @param start String
	 * @param dist String
	 * @param output PrintWriter
	 */
	void printElementTree(PrintWriter output, String start, String dist){
		output.println(start + this);
		for (int i = 0; i < _myChildren.size(); i++){
			((CCXMLElement) _myChildren.get(i)).printElementTree(output, start + dist, dist);
		}
		if (!_myIsEmpty){
			output.println(start + "</" + _myName + ">");
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<CCXMLElement> iterator() {
		return _myChildren.iterator();
	}

}

