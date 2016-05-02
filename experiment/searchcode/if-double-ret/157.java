package org.gwtopenmaps.openlayers.client.util;

import org.gwtopenmaps.openlayers.client.JFX;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Static methods for working with {@link JSObject} objects.
 * <p>
 * The getProperty/setProperty methods are convenience methods for reducing the
 * amount of JSNI code that is necessary for creating JSObject wrappers. Be
 * aware: getProperty methods can die horribly at runtime if property does not
 * exist.
 * </p>
 * ECMA-262 (Ecmascript = Javascript) defines an object as an unordered
 * collection of properties each of which contains a primitive value, object, or
 * function.
 * 
 * @author Edwin Commandeur - Atlis EJS
 * @author Alexander Solovets
 */
public class JSObjectImplJFX implements JSObjectHelper {

	public JSObject createObject() {
		return new JSObjectJFX((netscape.javascript.JSObject) JFX.getEngine()
				.executeScript("new Object()"));
	};

	public JSObject createFunction() {
		return new JSObjectJFX((netscape.javascript.JSObject) JFX.getEngine()
				.executeScript("new Function()"));
	};

	public JSObject createArray() {
		return new JSObjectJFX((netscape.javascript.JSObject) JFX.getEngine()
				.executeScript("new Array()"));
	};

	// TODO: implement setProperty getProperty methods
	// and remove setAttribute methods from ElementHelper that actually set
	// properies

	public void setProperty(JSObject object, String name, int value) {
		((JSObjectJFX) object).getJFXObject().setMember(name, value);
	};

	public int getPropertyAsInt(JSObject object, String name) {
		Object ret = ((JSObjectJFX) object).getJFXObject().getMember(name);
		return ret == null ? 0 : (Integer) ret;
	};

	public void setProperty(JSObject object, String name, String value) {
		((JSObjectJFX) object).getJFXObject().setMember(name, value);
	};

	public String getPropertyAsString(JSObject object, String name) {
		Object ret = ((JSObjectJFX) object).getJFXObject().getMember(name);
		return (String) ret;
	};

	public void setProperty(JSObject object, String name, boolean value) {
		((JSObjectJFX) object).getJFXObject().setMember(name, value);
	};

	public boolean getPropertyAsBoolean(JSObject object, String name) {
		Object ret = ((JSObjectJFX) object).getJFXObject().getMember(name);
		return ret == null ? false : (Boolean) ret;
	};

	public void setProperty(JSObject object, String name, float value) {
		((JSObjectJFX) object).getJFXObject().setMember(name, value);
	};

	public float getPropertyAsFloat(JSObject object, String name) {
		Object ret = ((JSObjectJFX) object).getJFXObject().getMember(name);
		return ret == null ? 0 : (Float) ret;
	};

	public void setProperty(JSObject object, String name, double value) {
		((JSObjectJFX) object).getJFXObject().setMember(name, value);
	};

	public double getPropertyAsDouble(JSObject object, String name) {
		Object ret = ((JSObjectJFX) object).getJFXObject().getMember(name);
		return ret == null ? 0 : (Double) ret;
	};

	public void setProperty(JSObject object, String name, JSObject value) {
		((JSObjectJFX) object).getJFXObject().setMember(name, value);
	};

	public JSObject getProperty(JSObject object, String name) {
		// FIXME correct?
		Object ret = ((JSObjectJFX) object).getJFXObject().getMember(name);
		return ret == null ? null : new JSObjectJFX(
				(netscape.javascript.JSObject) ret);
	};

	public void setProperty(JSObject object, String name, Element value) {
		((JSObjectJFX) object).getJFXObject().setMember(name, value);
	};

	public Element getPropertyAsDomElement(JSObject object, String name) {
		Object ret = ((JSObjectJFX) object).getJFXObject().getMember(name);
		return ret == null ? null : (Element) ret;
	};

	public String getPropertyNames(JSObject object) {
		Element e = (Element) ((JSObjectJFX) object).getJFXObject();
		NamedNodeMap attributes = e.getAttributes();
		StringBuilder ret = new StringBuilder();

		for (int i = 0; i < attributes.getLength(); i++) {
			Node item = attributes.item(i);
			String nodeName = item.getNodeName();

			ret.append(nodeName).append(",");
		}

		return ret.length() > 0 ? ret.substring(0, ret.length() - 2) : "";
	};

	public String getPropertyValues(JSObject object) {
		Element e = (Element) ((JSObjectJFX) object).getJFXObject();
		NamedNodeMap attributes = e.getAttributes();
		StringBuilder ret = new StringBuilder();

		for (int i = 0; i < attributes.getLength(); i++) {
			Node item = attributes.item(i);
			String nodeValue = item.getNodeValue();

			ret.append(nodeValue).append(",");
		}

		return ret.length() > 0 ? ret.substring(0, ret.length() - 2) : "";
	};

	public  boolean hasProperty(JSObject object, String name) {
        Element e = (Element) ((JSObjectJFX) object).getJFXObject();
		return e.hasAttribute(name);
    };

	/**
	 * Unset/Clear the property with the given name. Uses the javascript
	 * operator delete
	 * 
	 * @param object
	 *            The object in which the property exists
	 * @param name
	 *            The name of the property
	 */
	public void unsetProperty(JSObject object, String name) {
		((JSObjectJFX) object).getJFXObject().removeMember(name);
	};

	/**
	 * Utility method for creating arguments to functions that take an array of
	 * objects.
	 * 
	 * @param object
	 *            - an opaque handle on a JavaScript object
	 * @return object - an opaque handle on a JavaScript object that is surely
	 *         an Array
	 */
	public  JSObject ensureOpaqueArray(JSObject object) {
        return object;
    };

}

