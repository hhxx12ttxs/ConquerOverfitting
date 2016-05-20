/*
 * Copyright 2011, The gwtquery team.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.query.client;

import static com.google.gwt.query.client.plugins.QueuePlugin.Queue;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayMixed;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.BodyElement;
import com.google.gwt.dom.client.ButtonElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.OptionElement;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.HasCssName;
import com.google.gwt.dom.client.TextAreaElement;
import com.google.gwt.query.client.css.CSS;
import com.google.gwt.query.client.css.HasCssValue;
import com.google.gwt.query.client.css.TakesCssValue;
import com.google.gwt.query.client.css.TakesCssValue.CssSetter;
import com.google.gwt.query.client.impl.AttributeImpl;
import com.google.gwt.query.client.impl.DocumentStyleImpl;
import com.google.gwt.query.client.impl.SelectorEngine;
import com.google.gwt.query.client.js.JsCache;
import com.google.gwt.query.client.js.JsMap;
import com.google.gwt.query.client.js.JsNamedArray;
import com.google.gwt.query.client.js.JsNodeArray;
import com.google.gwt.query.client.js.JsObjectArray;
import com.google.gwt.query.client.js.JsRegexp;
import com.google.gwt.query.client.js.JsUtils;
import com.google.gwt.query.client.plugins.Effects;
import com.google.gwt.query.client.plugins.Events;
import com.google.gwt.query.client.plugins.Plugin;
import com.google.gwt.query.client.plugins.Widgets;
import com.google.gwt.query.client.plugins.ajax.Ajax;
import com.google.gwt.query.client.plugins.ajax.Ajax.Settings;
import com.google.gwt.query.client.plugins.effects.PropertiesAnimation.Easing;
import com.google.gwt.query.client.plugins.events.EventsListener;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.GqUi;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * GwtQuery is a GWT clone of the popular jQuery library.
 */
public class GQuery implements Lazy<GQuery, LazyGQuery> {

  private enum DomMan {
    AFTER, APPEND, BEFORE, PREPEND;
  }

  /**
   * A POJO used to store the top/left CSS positioning values of an element.
   */
  public static class Offset {
    public int left;
    public int top;

    public Offset(int left, int top) {
      this.left = left;
      this.top = top;
    }

    public Offset add(int left, int top) {
      return new Offset(this.left + left, this.top + top);
    }

    public String toString() {
      return top + "+" + left;
    }
  }

  /**
   * Class used internally to create DOM element from html snippet
   */
  private static class TagWrapper {
    public static final TagWrapper DEFAULT = new TagWrapper(0, "", "");
    private String postWrap;
    private String preWrap;
    private int wrapDepth;

    public TagWrapper(int wrapDepth, String preWrap, String postWrap) {
      this.wrapDepth = wrapDepth;
      this.postWrap = postWrap;
      this.preWrap = preWrap;
    }
  }

  /**
   * Implementation class to modify attributes.
   */
  protected static AttributeImpl attributeImpl;

  /**
   * The body element in the current page.
   */
  public static final BodyElement body = Document.get().getBody();

  /**
   * Object to store element data.
   */
  protected static JsCache dataCache = null;

  /**
   * The document element in the current page.
   */
  public static final Document document = Document.get();

  /**
   * Static reference Effects plugin
   */
  public static Class<Effects> Effects = com.google.gwt.query.client.plugins.Effects.Effects;

  /**
   * Implementation engine used for CSS selectors.
   */
  protected static SelectorEngine engine;

  /**
   * Static reference Events plugin
   */
  public static Class<Events> Events = com.google.gwt.query.client.plugins.Events.Events;

  /**
   * A static reference to the GQuery class.
   */
  public static Class<GQuery> GQUERY = GQuery.class;

  private static final String OLD_DATA_PREFIX = "old-";

  private static JsMap<Class<? extends GQuery>, Plugin<? extends GQuery>> plugins;

  // Sizzle POS regex : usefull in some methods
  // TODO: Share this static with SelectorEngineSizzle
  private static final String POS_REGEX =
      ":(nth|eq|gt|lt|first|last|even|odd)(?:\\((\\d*)\\))?(?=[^\\-]|$)";

  /**
   * Implementation class used for style manipulations.
   */
  private static DocumentStyleImpl styleImpl;

  private static JsRegexp tagNameRegex = new JsRegexp("<([\\w:]+)");

  /**
   * Static reference to the Widgets plugin
   */
  public static Class<Widgets> Widgets = com.google.gwt.query.client.plugins.Widgets.Widgets;

  /**
   * The window object.
   */
  public static final Element window = window();

  private static Element windowData = null;

  private static JsNamedArray<TagWrapper> wrapperMap;

  /**
   * Create an empty GQuery object.
   */
  public static GQuery $() {
    return new GQuery(JsNodeArray.create());
  }

  /**
   * Wrap a GQuery around an existing element.
   */
  public static GQuery $(Element element) {
    return new GQuery(element);
  }

  /**
   * Wrap a GQuery around an event's target element.
   */
  public static GQuery $(Event event) {
    return event == null ? $() : $((Element) event.getCurrentEventTarget().cast());
  }

  /**
   * Wrap a GQuery around the element of a Function callback.
   */
  public static GQuery $(Function f) {
    return $(f.getElement());
  }

  /**
   * Wrap a GQuery around an existing element, event, node or nodelist.
   */
  public static GQuery $(JavaScriptObject e) {
    return JsUtils.isWindow(e) ? GQuery.$(e.<Element> cast()) : JsUtils.isElement(e) ? GQuery.$(e
        .<Element> cast()) : JsUtils.isEvent(e) ? GQuery.$(e.<Event> cast()) : JsUtils
        .isNodeList(e) ? GQuery.$(e.<NodeList<Element>> cast()) : $();
  }

  /**
   * Create a new GQuery given a list of nodes, elements or widgets
   */
  public static GQuery $(List<?> nodesOrWidgets) {
    JsNodeArray elms = JsNodeArray.create();
    if (nodesOrWidgets != null) {
      for (Object o : nodesOrWidgets) {
        if (o instanceof Node) {
          elms.addNode((Node) o);
        } else if (o instanceof Widget) {
          elms.addNode(((Widget) o).getElement());
        }
      }
    }
    return new GQuery(elms);
  }

  /**
   * Wrap a GQuery around an existing node.
   */
  public static GQuery $(Node n) {
    return $((Element) n);
  }

  /**
   * Wrap a GQuery around existing Elements.
   */
  public static GQuery $(NodeList<Element> elms) {
    return new GQuery(elms);
  }

  /**
   * This function accepts a string containing a CSS selector which is then used to match a set of
   * elements, or it accepts raw HTML creating a GQuery element containing those elements. Xpath
   * selector is supported in browsers with native xpath engine.
   */
  public static GQuery $(String selectorOrHtml) {
    return $(selectorOrHtml, document);
  }

  /**
   * This function accepts a string containing a CSS selector which is then used to match a set of
   * elements, or it accepts raw HTML creating a GQuery element containing those elements. The
   * second parameter is is a class reference to a plugin to be used.
   * 
   * Xpath selector is supported in browsers with native xpath engine.
   */
  public static <T extends GQuery> T $(String selector, Class<T> plugin) {
    return $(selector, document, plugin);
  }

  /**
   * This function accepts a string containing a CSS selector which is then used to match a set of
   * elements, or it accepts raw HTML creating a GQuery element containing those elements. The
   * second parameter is the context to use for the selector, or the document where the new elements
   * will be created.
   * 
   * Xpath selector is supported in browsers with native xpath engine.
   */
  public static GQuery $(String selectorOrHtml, Node ctx) {
    String selector = null;
    if (selectorOrHtml == null || (selector = selectorOrHtml.trim()).length() == 0) {
      return $();
    }
    if (selector.startsWith("<")) {
      return innerHtml(selectorOrHtml, JsUtils.getOwnerDocument(ctx));
    }
    return new GQuery().select(selectorOrHtml, ctx);
  }

  /**
   * This function accepts a string containing a CSS selector which is then used to match a set of
   * elements, or it accepts raw HTML creating a GQuery element containing those elements. The
   * second parameter is the context to use for the selector. The third parameter is the class
   * plugin to use.
   * 
   * Xpath selector is supported in browsers with native xpath engine.
   */
  @SuppressWarnings("unchecked")
  public static <T extends GQuery> T $(String selector, Node context, Class<T> plugin) {
    try {
      if (plugins != null) {
        T gquery = (T) plugins.get(plugin).init(new GQuery().select(selector, context));
        return gquery;
      }
      throw new RuntimeException("No plugin for class " + plugin);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * This function accepts a string containing a CSS selector which is then used to match a set of
   * elements, or it accepts raw HTML creating a GQuery element containing those elements. The
   * second parameter is the context to use for the selector, or the document where the new elements
   * will be created.
   * 
   * Xpath selector is supported in browsers with native xpath engine.
   */
  public static GQuery $(String selectorOrHtml, Widget context) {
    return $(selectorOrHtml, context.getElement());
  }

  /**
   * This function accepts a string containing a CSS selector which is then used to match a set of
   * elements, or it accepts raw HTML creating a GQuery element containing those elements. The
   * second parameter is the context to use for the selector. The third parameter is the class
   * plugin to use.
   * 
   * Xpath selector is supported in browsers with native xpath engine.
   */
  public static <T extends GQuery> T $(String selector, Widget context, Class<T> plugin) {
    return $(selector, context.getElement(), plugin);
  }

  /**
   * wraps a GQuery or a plugin object
   */
  public static <T extends GQuery> T $(T gq) {
    return gq;
  }

  /**
   * Wrap a GQuery around one widget or an array of existing ones.
   */
  public static GQuery $(Widget... widgets) {
    return $(Arrays.asList(widgets));
  }

  /**
   * Wrap a JSON object.
   */
  public static Properties $$(String properties) {
    return Properties.create(properties);
  }

  /**
   * Perform an ajax request to the server.
   */
  public static void ajax(Properties p) {
    ajax(p);
  }

  /**
   * Perform an ajax request to the server.
   */
  public static void ajax(Settings settings) {
    Ajax.ajax(settings);
  }

  /**
   * Perform an ajax request to the server.
   */
  public static void ajax(String url, Settings settings) {
    Ajax.ajax(url, settings);
  }

  @SuppressWarnings("unchecked")
  protected static GQuery cleanHtmlString(String elem, Document doc) {

    String tag = tagNameRegex.exec(elem).get(1);

    if (tag == null) {
      throw new RuntimeException("HTML snippet doesn't contain any tag");
    }

    if (wrapperMap == null) {
      initWrapperMap();
    }

    TagWrapper wrapper = wrapperMap.get(tag.toLowerCase());

    if (wrapper == null) {
      wrapper = TagWrapper.DEFAULT;
    }

    // TODO: fix IE link tag serialization
    // TODO: fix IE <script> tag
    Element div = doc.createDivElement();
    div.setInnerHTML(wrapper.preWrap + elem.trim() + wrapper.postWrap);
    Node n = div;
    int depth = wrapper.wrapDepth;
    while (depth-- != 0) {
      n = n.getLastChild();
    }
    // TODO: add fixes for IE TBODY issue
    return $((NodeList<Element>) n.getChildNodes().cast());
  }

  /**
   * Return true if the element b is contained in a.
   */
  public static boolean contains(Element a, Element b) {
    return engine.contains(a, b);
  }

  /**
   * Get the element data matching the key.
   */
  public static Object data(Element e, String key) {
    return GQuery.data(e, key, null);
  }

  protected static <S> Object data(Element item, String name, S value) {
    if (dataCache == null) {
      windowData = JavaScriptObject.createObject().cast();
      dataCache = JavaScriptObject.createObject().cast();
    }
    item = item == window || item.getNodeName() == null ? windowData : item;
    if (item == null) {
      return value;
    }
    int id = item.hashCode();
    if (name != null && !dataCache.exists(id)) {
      dataCache.put(id, JsCache.createObject().cast());
    }

    JsCache d = dataCache.getCache(id);
    if (name != null && value != null) {
      d.put(name, value);
    }
    return name != null ? d.get(name) : id;
  }

  /**
   * Execute a function around each object
   */
  public static void each(JsArrayMixed objects, Function f) {
    for (int i = 0, l = objects.length(); i < l; i++) {
      f.f(i, objects.getObject(i));
    }
  }

  /**
   * Execute a function around each object
   */
  public static <T> void each(List<T> objects, Function f) {
    for (int i = 0, l = objects.size(); i < l; i++) {
      f.f(i, objects.get(i));
    }
  }

  /**
   * Execute a function around each object
   */
  public static <T> void each(T[] objects, Function f) {
    for (int i = 0, l = objects.length; i < l; i++) {
      f.f(i, objects[i]);
    }
  }

  /**
   * Perform an ajax request to the server using GET.
   */
  public static void get(String url, Properties data, final Function onSuccess) {
    Ajax.get(url, data, onSuccess);
  }

  /**
   * We will use the fact as GWT use the widget itself as EventListener ! If no Widget associated
   * with the element, this method returns null.
   */
  protected static Widget getAssociatedWidget(Element e) {
    try {
      EventListener listener = DOM.getEventListener((com.google.gwt.user.client.Element) e);
      // No listener attached to the element, so no widget exist for this element
      if (listener == null) {
        return null;
      }
      if (listener instanceof Widget) {
        // GWT uses the widget as event listener
        return (Widget) listener;
      } else if (listener instanceof EventsListener) {
        // GQuery replaces the gwt event listener and save it
        EventsListener gQueryListener = (EventsListener) listener;
        if (gQueryListener.getOriginalEventListener() != null
            && gQueryListener.getOriginalEventListener() instanceof Widget) {
          return (Widget) gQueryListener.getOriginalEventListener();
        }
      }
    } catch (Exception e2) {
      // Some times this code could raise an exception.
      // We do not want GQuery to fail, but in dev-move we log the error.
      e2.printStackTrace();
    }
    return null;
  }

  private static AttributeImpl getAttributeImpl() {
    if (attributeImpl == null) {
      attributeImpl = GWT.create(AttributeImpl.class);
    }
    return attributeImpl;
  }

  /**
   * Perform an ajax request to the server using POST and parsing the json response.
   */
  public static void getJSON(String url, Properties data, final Function onSuccess) {
    Ajax.getJSON(url, data, onSuccess);
  }

  /**
   * Perform an ajax request to the server using scripts tags and parsing the json response. The
   * request is not subject to the same origin policy restrictions.
   * 
   * Server side should accept a parameter to specify the callback funcion name, and it must return
   * a valid json object wrapped this callback function.
   * 
   * Example:
   * 
   * <pre>
    Client code:
    getJSONP("http://server.exampe.com/getData.php",$$("myCallback:'?', otherParameter='whatever'"), 
      new Function(){ public void f() {
        Properties p = getDataProperties();
        alert(p.getStr("k1");
    }});
   
    Server response:
    myCallback({"k1":"v1", "k2":"v2"});
   </pre>
   * 
   */
  public static void getJSONP(String url, Properties data, final Function onSuccess) {
    Ajax.getJSONP(url, data, onSuccess);
  }

  protected static DocumentStyleImpl getStyleImpl() {
    if (styleImpl == null) {
      styleImpl = GWT.create(DocumentStyleImpl.class);
    }
    return styleImpl;
  }

  /**
   * Return only the set of objects with match the predicate.
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] grep(T[] objects, Predicate f) {
    ArrayList<Object> ret = new ArrayList<Object>();
    for (int i = 0, l = objects.length; i < l; i++) {
      if (f.f(objects[i], i)) {
        ret.add(objects[i]);
      }
    }
    return (T[]) ret.toArray(new Object[0]);
  }

  private static boolean hasClass(Element e, String clz) {
    return e.getClassName().matches("(^|.*\\s)" + clz + "(\\s.*|$)");
  }

  private static void initWrapperMap() {

    TagWrapper tableWrapper = new TagWrapper(1, "<table>", "</table>");
    TagWrapper selectWrapper = new TagWrapper(1, "<select multiple=\"multiple\">", "</select>");
    TagWrapper trWrapper = new TagWrapper(3, "<table><tbody><tr>", "</tr></tbody></table>");

    wrapperMap = JsNamedArray.create();
    wrapperMap.put("option", selectWrapper);
    wrapperMap.put("optgroup", selectWrapper);
    wrapperMap.put("legend", new TagWrapper(1, "<fieldset>", "</fieldset>"));
    wrapperMap.put("thead", tableWrapper);
    wrapperMap.put("tbody", tableWrapper);
    wrapperMap.put("tfoot", tableWrapper);
    wrapperMap.put("colgroup", tableWrapper);
    wrapperMap.put("caption", tableWrapper);
    wrapperMap.put("tr", new TagWrapper(2, "<table><tbody>", "</tbody></table>"));
    wrapperMap.put("td", trWrapper);
    wrapperMap.put("th", trWrapper);
    wrapperMap.put("col", new TagWrapper(2, "<table><tbody></tbody><colgroup>",
        "</colgroup></table>"));
    wrapperMap.put("area", new TagWrapper(1, "<map>", "</map>"));

  }

  private static GQuery innerHtml(String html, Document doc) {
    return $(cleanHtmlString(html, doc));
  }

  protected static String[] jsArrayToString(JsArrayString array) {
    if (GWT.isScript()) {
      return jsArrayToString0(array);
    } else {
      String result[] = new String[array.length()];
      for (int i = 0, l = result.length; i < l; i++) {
        result[i] = array.get(i);
      }
      return result;
    }
  }

  private static native String[] jsArrayToString0(JsArrayString array) /*-{
		return array;
  }-*/;

  /**
   * Return a lazy version of the GQuery interface. Lazy function calls are simply queued up and not
   * executed immediately.
   */
  public static LazyGQuery<?> lazy() {
    return $().createLazy();
  }

  /**
   * Perform an ajax request to the server using POST.
   */
  public static void post(String url, Properties data, final Function onSuccess) {
    Ajax.post(url, data, onSuccess);
  }

  public static <T extends GQuery> Class<T> registerPlugin(Class<T> plugin, Plugin<T> pluginFactory) {
    if (plugins == null) {
      plugins = JsMap.createObject().cast();
    }

    plugins.put(plugin, pluginFactory);
    return plugin;
  }

  private static native void scrollIntoViewImpl(Node n) /*-{
		if (n)
			n.scrollIntoView()
  }-*/;

  private static native void setElementValue(Element e, String value) /*-{
		e.value = value;
  }-*/;

  private static native Element window() /*-{
		return $wnd;
  }-*/;

  protected Node currentContext;

  protected String currentSelector;
  /**
   * Immutable array of matched elements, modify this using setArray
   */
  private Element[] elements = new Element[0];

  /**
   * The nodeList of matched elements, modify this using setArray
   */
  // TODO: remove this and use elements, change return type of get()
  private NodeList<Element> nodeList = JavaScriptObject.createArray().cast();

  private GQuery previousObject;

  private GQuery() {
  }

  private GQuery(Element element) {
    this(JsNodeArray.create(element));
  }

  protected GQuery(GQuery gq) {
    this(gq == null ? null : gq.get());
    currentSelector = gq.getSelector();
    currentContext = gq.getContext();
  }

  private GQuery(JsNodeArray nodes) {
    this(nodes.<NodeList<Element>> cast());
  }

  private GQuery(NodeList<Element> list) {
    setArray(list);
  }

  /**
   * Add elements to the set of matched elements if they are not included yet.
   * 
   * It construct a new GQuery object and does not modify the original ones.
   * 
   * It also update the selector appending the new one.
   */
  public GQuery add(GQuery elementsToAdd) {
    return pushStack(JsUtils.copyNodeList(nodeList, elementsToAdd.nodeList, true)
        .<JsNodeArray> cast(), "add", getSelector() + "," + elementsToAdd.getSelector());
  }

  /**
   * Add elements to the set of matched elements if they are not included yet.
   */
  public GQuery add(String selector) {
    return add($(selector));
  }

  /**
   * Adds the specified classes to each matched element.
   */
  public GQuery addClass(String... classes) {
    for (Element e : elements) {
      // issue 81 : ensure that the element is an Element node.
      if (Element.is(e)) {
        for (String clz : classes) {
          e.addClassName(clz);
        }
      }
    }
    return this;
  }

  /**
   * Insert content after each of the matched elements. The elements must already be inserted into
   * the document (you can't insert an element after another if it's not in the page).
   */
  public GQuery after(GQuery query) {
    return domManip(query, DomMan.AFTER);
  }

  /**
   * Insert content after each of the matched elements. The elements must already be inserted into
   * the document (you can't insert an element after another if it's not in the page).
   */
  public GQuery after(Node n) {
    return domManip($(n), DomMan.AFTER);
  }

  /**
   * Insert content after each of the matched elements. The elements must already be inserted into
   * the document (you can't insert an element after another if it's not in the page).
   */
  public GQuery after(String html) {
    return domManip(html, DomMan.AFTER);
  }

  private void allNextSiblingElements(Element firstChildElement, JsNodeArray result, Element elem,
      GQuery until, String filterSelector) {

    while (firstChildElement != null) {

      if (until != null && until.index(firstChildElement) != -1) {
        return;
      }

      if (firstChildElement != elem
          && (filterSelector == null || $(firstChildElement).is(filterSelector))) {
        result.addNode(firstChildElement);
      }
      firstChildElement = firstChildElement.getNextSiblingElement();
    }
  }

  private void allPreviousSiblingElements(Element firstChildElement, JsNodeArray result,
      GQuery until, String filterSelector) {
    while (firstChildElement != null) {
      if (until != null && until.index(firstChildElement) != -1) {
        return;
      }

      if (filterSelector == null || $(firstChildElement).is(filterSelector)) {
        result.addNode(firstChildElement);
      }

      firstChildElement = getPreviousSiblingElement(firstChildElement);
    }
  }

  /**
   * Add the previous selection to the current selection. Useful for traversing elements, and then
   * adding something that was matched before the last traversal.
   */
  public GQuery andSelf() {
    return add(previousObject);
  }

  /**
   * 
   * The animate() method allows you to create animation effects on any numeric Attribute, CSS
   * property, or color CSS property.
   * 
   * Concerning to numeric properties, values are treated as a number of pixels unless otherwise
   * specified. The units em and % can be specified where applicable.
   * 
   * By default animate considers css properties, if you wanted to animate element attributes you
   * should to prepend the symbol dollar to the attribute name.
   * 
   * Example:
   * 
   * <pre class="code">
   *  //move the element from its original position to left:500px for 500ms
   *  $("#foo").animate("left:'500'");
   *  // Change the width attribute of a table
   *  $("table").animate("$width:'500'"), 400, Easing.LINEAR);
   * </pre>
   * 
   * In addition to numeric values, each property can take the strings 'show', 'hide', and 'toggle'.
   * These shortcuts allow for custom hiding and showing animations that take into account the
   * display type of the element. Animated properties can also be relative. If a value is supplied
   * with a leading += or -= sequence of characters, then the target value is computed by adding or
   * subtracting the given number from the current value of the property.
   * 
   * Example:
   * 
   * <pre class="code">
   *  //move the element from its original position to 500px to the left for 500ms and
   *  // change the background color of the element at the end of the animation
   *  $("#foo").animate("left:'+=500'", new Function(){
   *                  
   *                 public void f(Element e){
   *                   $(e).css(CSS.BACKGROUND_COLOR.with(RGBColor.RED);
   *                 }
   *                 
   *              });
   * </pre>
   * 
   * The duration of the animation is 500ms.
   * 
   * For color css properties, values can be specified via hexadecimal or rgb or literal values.
   * 
   * Example:
   * 
   * <pre class="code">
   *  $("#foo").animate("backgroundColor:'red', color:'#ffffff', borderColor:'rgb(129, 0, 70)'");
   * </pre>
   * 
   * @param prop the property to animate : "cssName:'value'"
   * @param funcs an array of {@link Function} called once the animation is complete
   */
  public GQuery animate(Object stringOrProperties, Function... funcs) {
    return as(Effects).animate(stringOrProperties, funcs);
  }

  /**
   * The animate() method allows you to create animation effects on any numeric Attribute, CSS
   * property, or color CSS property.
   * 
   * Concerning to numeric properties, values are treated as a number of pixels unless otherwise
   * specified. The units em and % can be specified where applicable.
   * 
   * By default animate considers css properties, if you wanted to animate element attributes you
   * should to prepend the symbol dollar to the attribute name.
   * 
   * Example:
   * 
   * <pre class="code">
   *  //move the element from its original position to the position top:500px and left:500px for 400ms.
   *  //use a swing easing function for the transition
   *  $("#foo").animate(Properties.create("{top:'500px',left:'500px'}"), 400, Easing.SWING);
   *  // Change the width and border attributes of a table
   *  $("table").animate(Properties.create("{$width: '500', $border: '10'}"), 400, Easing.LINEAR);
   * </pre>
   * 
   * In addition to numeric values, each property can take the strings 'show', 'hide', and 'toggle'.
   * These shortcuts allow for custom hiding and showing animations that take into account the
   * display type of the element. Animated properties can also be relative. If a value is supplied
   * with a leading += or -= sequence of characters, then the target value is computed by adding or
   * subtracting the given number from the current value of the property.
   * 
   * Example:
   * 
   * <pre class="code">
   *  //move the element from its original position to 500px to the left and 5OOpx down for 400ms.
   *  //use a swing easing function for the transition
   *  $("#foo").animate(Properties.create("{top:'+=500px',left:'+=500px'}"), 400, Easing.SWING);
   * </pre>
   * 
   * For color css properties, values can be specified via hexadecimal or rgb or literal values.
   * 
   * Example:
   * 
   * <pre class="code">
   *  $("#foo").animate("backgroundColor:'red', color:'#ffffff', borderColor:'rgb(129, 0, 70)'"), 400, Easing.SWING);
   * </pre>
   * 
   * @param stringOrProperties a String or a {@link Properties} object containing css properties to
   *          animate.
   * @param funcs an array of {@link Function} called once the animation is complete
   * @param duration the duration in milliseconds of the animation
   * @param easing the easing function to use for the transition
   */
  public GQuery animate(Object stringOrProperties, int duration, Easing easing, Function... funcs) {
    return as(Effects).animate(stringOrProperties, duration, easing, funcs);
  }

  /**
   * The animate() method allows you to create animation effects on any numeric Attribute, CSS
   * properties, or color CSS property.
   * 
   * Concerning to numeric property, values are treated as a number of pixels unless otherwise
   * specified. The units em and % can be specified where applicable.
   * 
   * By default animate considers css properties, if you wanted to animate element attributes you
   * should to prepend the symbol dollar to the attribute name.
   * 
   * Example:
   * 
   * <pre class="code">
   *  //move the element from its original position to left:500px for 2s
   *  $("#foo").animate("left:'500px'", 2000);
   *  // Change the width attribute of a table
   *  $("table").animate("$width:'500'"), 400);
   * </pre>
   * 
   * In addition to numeric values, each property can take the strings 'show', 'hide', and 'toggle'.
   * These shortcuts allow for custom hiding and showing animations that take into account the
   * display type of the element. Animated properties can also be relative. If a value is supplied
   * with a leading += or -= sequence of characters, then the target value is computed by adding or
   * subtracting the given number from the current value of the property.
   * 
   * Example:
   * 
   * <pre class="code">
   *  //move the element from its original position to 500px to the left for 1000ms and
   *  // change the background color of the element at the end of the animation
   *  $("#foo").animate("left:'+=500'", 1000, new Function(){
   *     public void f(Element e){
   *       $(e).css(CSS.BACKGROUND_COLOR.with(RGBColor.RED);
   *     }
   *  });
   * </pre>
   * 
   * 
   * For color css properties, values can be specified via hexadecimal or rgb or literal values.
   * 
   * Example:
   * 
   * <pre class="code">
   *  $("#foo").animate("backgroundColor:'red', color:'#ffffff', borderColor:'rgb(129, 0, 70)', 1000");
   * </pre>
   * 
   * 
   * @param prop the property to animate : "cssName:'value'"
   * @param funcs an array of {@link Function} called once the animation is complete
   * @param duration the duration in milliseconds of the animation
   */
  public GQuery animate(Object stringOrProperties, int duration, Function... funcs) {
    return as(Effects).animate(stringOrProperties, duration, funcs);
  }

  /**
   * Append content to the inside of every matched element. This operation is similar to doing an
   * appendChild to all the specified elements, adding them into the document.
   */
  public GQuery append(GQuery query) {
    return domManip(query, DomMan.APPEND);
  }

  /**
   * Append content to the inside of every matched element. This operation is similar to doing an
   * appendChild to all the specified elements, adding them into the document.
   */
  public GQuery append(Node n) {
    return domManip($(n), DomMan.APPEND);
  }

  /**
   * Append content to the inside of every matched element. This operation is similar to doing an
   * appendChild to all the specified elements, adding them into the document.
   */
  public GQuery append(String html) {
    return domManip(html, DomMan.APPEND);
  }

  /**
   * All of the matched set of elements will be inserted at the end of the element(s) specified by
   * the parameter other.
   * 
   * The operation $(A).appendTo(B) is, essentially, the reverse of doing a regular $(A).append(B),
   * instead of appending B to A, you're appending A to B.
   */
  public GQuery appendTo(GQuery other) {
    other.append(this);
    return this;
  }

  /**
   * All of the matched set of elements will be inserted at the end of the element(s) specified by
   * the parameter other.
   * 
   * The operation $(A).appendTo(B) is, essentially, the reverse of doing a regular $(A).append(B),
   * instead of appending B to A, you're appending A to B.
   */
  public GQuery appendTo(Node n) {
    GQuery a = $(n);
    GQuery b = this;
    a.append(b);
    return this;
  }

  /**
   * All of the matched set of elements will be inserted at the end of the element(s) specified by
   * the parameter other.
   * 
   * The operation $(A).appendTo(B) is, essentially, the reverse of doing a regular $(A).append(B),
   * instead of appending B to A, you're appending A to B.
   */
  public GQuery appendTo(String html) {
    $(html).append(this);
    return this;
  }

  /**
   * Convert to Plugin interface provided by Class literal.
   */
  @SuppressWarnings("unchecked")
  public <T extends GQuery> T as(Class<T> plugin) {
    // GQuery is not a plugin for itself
    if (plugin == GQUERY) {
      return (T) $(this);
    } else if (plugins != null) {

      Plugin<?> p = plugins.get(plugin);
      if (p != null) {
        return (T) p.init(this);
      }
    }
    throw new RuntimeException("No plugin registered for class " + plugin.getName());
  }

  /**
   * Set a key/value object as properties to all matched elements.
   * 
   * Example: $("img").attr(new Properties("src: 'test.jpg', alt: 'Test Image'"))
   */
  public GQuery attr(Properties properties) {
    for (String name : properties.keys()) {
      attr(name, properties.getStr(name));
    }
    return this;
  }

  /**
   * Access a property on the first matched element. This method makes it easy to retrieve a
   * property value from the first matched element. If the element does not have an attribute with
   * such a name, empty string is returned. Attributes include title, alt, src, href, width, style,
   * etc.
   */
  public String attr(String name) {
    return isEmpty() ? "" : get(0).getAttribute(name);
  }

  /**
   * Set a single property to a computed value, on all matched elements.
   */
  public GQuery attr(String key, Function closure) {
    int i = 0;
    for (Element e : elements) {
      Object val = closure.f(e.<com.google.gwt.dom.client.Element> cast(), i++);
      $(e).attr(key, val);
    }
    return this;
  }

  /**
   * Set a single property to a value, on all matched elements.
   */
  public GQuery attr(String key, Object value) {
    assert key != null : "key cannot be null";
    assert !"$H".equalsIgnoreCase(key) : "$H is a GWT reserved attribute. Changing its value will break your application.";

    getAttributeImpl().setAttribute(this, key, value);

    return this;
  }

  /**
   * Insert content before each of the matched elements. The elements must already be inserted into
   * the document (you can't insert an element before another if it's not in the page).
   */
  public GQuery before(GQuery query) {
    return domManip(query, DomMan.BEFORE);
  }

  /**
   * Insert content before each of the matched elements. The elements must already be inserted into
   * the document (you can't insert an element before another if it's not in the page).
   */
  public GQuery before(Node n) {
    return domManip($(n), DomMan.BEFORE);
  }

  /**
   * Insert content before each of the matched elements. The elements must already be inserted into
   * the document (you can't insert an element before another if it's not in the page).
   */
  public GQuery before(String html) {
    return domManip(html, DomMan.BEFORE);
  }

  /**
   * Binds a set of handlers to a particular Event for each matched element.
   * 
   * The event handlers are passed as Functions that you can use to prevent default behavior. To
   * stop both default action and event bubbling, the function event handler has to return false.
   * 
   * You can pass an additional Object data to your Function as the second parameter
   * 
   */
  public GQuery bind(int eventbits, final Object data, final Function... funcs) {
    return as(Events).bind(eventbits, data, funcs);
  }
  
  /**
   * Binds a set of handlers to a particular Event for each matched element.
   * 
   * The event handlers are passed as Functions that you can use to prevent default behavior. To
   * stop both default action and event bubbling, the function event handler has to return false.
   * 
   * 
   */
  public GQuery bind(int eventbits, final Function... funcs) {
    return as(Events).bind(eventbits, null, funcs);
  }

  /**
   * Binds a set of handlers to a particular Event for each matched element.
   * 
   * The event handlers are passed as Functions that you can use to prevent default behavior. To
   * stop both default action and event bubbling, the function event handler has to return false.
   * 
   * You can pass an additional Object data to your Function as the second parameter
   * 
   */
  public GQuery bind(String eventType, final Object data, final Function... funcs) {
    return as(Events).bind(eventType, data, funcs);
  }

  /**
   * Binds a set of handlers to a particular Event for each matched element.
   * 
   * The event handlers are passed as Functions that you can use to prevent default behavior. To
   * stop both default action and event bubbling, the function event handler has to return false.
   * 
   * 
   */
  public GQuery bind(String eventType, final Function... funcs) {
    return as(Events).bind(eventType, null, funcs);
  }

  /**
   * Bind Handlers or fire Events for each matched element.
   */
  private GQuery bindOrFire(int eventbits, final Object data, final Function... funcs) {
    if (funcs.length == 0) {
      return trigger(eventbits);
    } else {
      return bind(eventbits, data, funcs);
    }
  }

  /**
   * Bind a set of functions to the blur event of each matched element. Or trigger the blur event if
   * no functions are provided.
   */
  public GQuery blur(Function... f) {
    bindOrFire(Event.ONBLUR, null, f);
    if (!isEmpty() && f.length == 0) {
      get(0).blur();
    }
    return this;
  }

  /**
   * Bind a set of functions to the change event of each matched element. Or trigger the event if no
   * functions are provided.
   */
  public GQuery change(Function... f) {
    return bindOrFire(Event.ONCHANGE, null, f);
  }

  /**
   * Get a set of elements containing all of the unique immediate children of each of the matched
   * set of elements. Also note: while parents() will look at all ancestors, children() will only
   * consider immediate child elements.
   */
  public GQuery children() {
    JsNodeArray result = JsNodeArray.create();
    for (Element e : elements) {
      allNextSiblingElements(e.getFirstChildElement(), result, null, null, null);
    }
    return new GQuery(unique(result));
  }

  /**
   * Get a set of elements containing all of the unique children of each of the matched set of
   * elements. This set is filtered with the expressions that will cause only elements matching any
   * of the selectors to be collected.
   */
  public GQuery children(String... filters) {
    return children().filter(filters);
  }

  private void cleanGQData(Element... elements) {
    for (Element el : elements) {
      try {
        EventsListener.clean(el);
        removeData(el, null);
      } catch (Exception e) {
        // If for some reason event/data removal fails, do not break the app,
        // just log the error in dev-mode
        // e.g.: this happens when removing iframes which are no fully loaded.
        e.printStackTrace();
      }
    }
  }

  /**
   * Remove from the Effects queue all {@link Function} that have not yet been run.
   */
  public GQuery clearQueue() {
    return as(Queue).clearQueue();
  }

  /**
   * Remove from the queue all {@link Function} that have not yet been run.
   */
  public GQuery clearQueue(String queueName) {
    return as(Queue).clearQueue(queueName);
  }

  /**
   * Bind a set of functions to the click event of each matched element. Or trigger the event if no
   * functions are provided.
   */
  public GQuery click(Function... f) {
    return bindOrFire(Event.ONCLICK, null, f);
  }

  /**
   * Clone matched DOM Elements and select the clones. This is useful for moving copies of the
   * elements to another location in the DOM.
   */
  public GQuery clone() {
    JsNodeArray result = JsNodeArray.create();
    for (Element e : elements) {
      result.addNode(e.cloneNode(true));
    }
    GQuery ret = new GQuery(result);
    ret.currentContext = currentContext;
    ret.currentSelector = currentSelector;
    return ret;
  }

  /**
   * Get the first ancestor element that matches the selector (for each matched element), beginning
   * at the current element and progressing up through the DOM tree.
   * 
   * @param selector
   * @return
   */
  public GQuery closest(String selector) {
    return closest(selector, null);
  }

  /**
   * Get the first ancestor element that matches the selector (for each matched element), beginning
   * at the current element and progressing up through the DOM tree until reach the
   * <code>context</code> node.
   * 
   * If no context is passed in then the context of the gQuery object will be used instead.
   * 
   */
  public GQuery closest(String selector, Node context) {
    assert selector != null;

    if (context == null) {
      context = currentContext;
    }

    GQuery pos = selector.matches(POS_REGEX) ? $(selector, context) : null;
    JsNodeArray result = JsNodeArray.create();

    for (Element e : elements) {
      Element current = e;
      while (current != null && current.getOwnerDocument() != null && current != context) {
        boolean match = pos != null ? pos.index(current) > -1 : $(current).is(selector);
        if (match) {
          result.addNode(current);
          break;
        } else {
          current = current.getParentElement();
        }
      }
    }

    return $(unique(result));

  }

  /**
   * Returns a {@link Map} object as key a selector and as value the list of ancestor elements
   * matching this selectors, beginning at the first matched element and progressing up through the
   * DOM. This method allows retrieving the list of ancestors matching many selectors by traversing
   * the DOM only one time.
   * 
   * @param selector
   * @return
   */
  public JsNamedArray<NodeList<Element>> closest(String[] selectors) {
    return closest(selectors, null);
  }

  /**
   * Returns a {@link Map} object as key a selector and as value the list of ancestor elements
   * matching this selectors, beginning at the first matched element and progressing up through the
   * DOM until reach the <code>context</code> node.. This method allows retrieving the list of
   * ancestors matching many selectors by traversing the DOM only one time.
   * 
   * @param selector
   * @return
   */
  public JsNamedArray<NodeList<Element>> closest(String[] selectors, Node context) {
    JsNamedArray<NodeList<Element>> results = JsNamedArray.create();

    if (context == null) {
      context = currentContext;
    }

    Element first = get(0);
    if (first != null && selectors != null && selectors.length > 0) {
      JsNamedArray<GQuery> matches = JsNamedArray.create();
      for (String selector : selectors) {
        if (!matches.exists(selector)) {
          matches.put(selector, selector.matches(POS_REGEX) ? $(selector, context) : null);
        }
      }

      Element current = first;
      while (current != null && current.getOwnerDocument() != null && current != context) {
        // for each selector, check if the current element match it.
        for (String selector : matches.keys()) {

          GQuery pos = matches.get(selector);
          boolean match = pos != null ? pos.index(current) > -1 : $(current).is(selector);

          if (match) {
            JsNodeArray elementsMatchingSelector = results.get(selector).cast();
            if (elementsMatchingSelector == null) {
              elementsMatchingSelector = JsNodeArray.create();
              results.put(selector, elementsMatchingSelector);
            }
            elementsMatchingSelector.addNode(current);
          }
        }

        current = current.getParentElement();
      }
    }
    return results;
  }

  /**
   * Filter the set of elements to those that contain the specified text.
   */
  public GQuery contains(String text) {
    JsNodeArray array = JsNodeArray.create();
    for (Element e : elements) {
      if ($(e).text().contains(text)) {
        array.addNode(e);
      }
    }
    return $(array);
  }

  /**
   * Find all the child nodes inside the matched elements (including text nodes), or the content
   * document, if the element is an iframe.
   */
  public GQuery contents() {
    JsNodeArray result = JsNodeArray.create();
    for (Element e : elements) {
      if (JsUtils.isWindow(e) || "iframe".equalsIgnoreCase(e.getTagName())) {
        result.addNode(getStyleImpl().getContentDocument(e));
      } else {
        NodeList<Node> children = e.getChildNodes();
        for (int i = 0, l = children.getLength(); i < l; i++) {
          result.addNode(children.getItem(i));
        }
      }
    }
    return new GQuery(unique(result));
  }

  public LazyGQuery<?> createLazy() {
    return GWT.create(GQuery.class);
  }

  /**
   * Set CSS a single style property on every matched element using type-safe enumerations.
   * 
   * The best way to use this method (i.e. to generate a CssSetter) is to take the desired css
   * property defined in {@link CSS} class and call the {@link TakesCssValue#with(HasCssName)}
   * method on it.
   * 
   * 
   * ex :
   * 
   * <pre class="code">
   * $("#myDiv").css(CSS.TOP.with(Length.cm(15)));
   * $("#myDiv").css(CSS.BACKGROUND.with(RGBColor.SILVER, ImageValue.url(""),
   *               BackgroundRepeat.NO_REPEAT, BackgroundAttachment.FIXED,
   *               BackgroundPosition.CENTER));
   * $("#myDiv").css(CSS.BACKGROUND_ATTACHMENT.with(BackgroundAttachment.FIXED));
   * 
   * </pre>
   * 
   */
  public GQuery css(CssSetter... cssSetter) {
    for (Element e : elements) {
      for (CssSetter s : cssSetter) {
        s.applyCss(e);
      }
    }
    return this;
  }

  /**
   * Return a style property on the first matched element using type-safe enumerations.
   * 
   * Ex : $("#myId").css(CSS.BACKGROUND_COLOR);
   */
  public String css(HasCssValue property) {
    return css(property, true);
  }

  /**
   * Return a style property on the first matched element using type-safe enumerations.
   * 
   * The parameter force has a special meaning here: - When force is false, returns the value of the
   * css property defined in the style attribute of the element. - Otherwise it returns the real
   * computed value.
   * 
   * For instance if you define 'display=none' not in the element style but in the css stylesheet,
   * it returns an empty string unless you pass the parameter force=true.
   * 
   * Ex : $("#myId").css(CSS.WIDTH, true);
   */
  public String css(HasCssValue property, boolean force) {
    return css(property.getCssName(), force);
  }

  /**
   * Set a key/value object as style properties to all matched elements. This serves as the best way
   * to set a large number of style properties on all matched elements. You can use either js maps
   * or pure css syntax.
   * 
   * Example:
   * 
   * <pre class="code">
   *  $(".item").css(Properties.create("color: 'red', background:'blue'"))
   *  $(".item").css(Properties.create("color: red; background: blue;"))
   * </pre>
   */
  public GQuery css(Properties properties) {
    for (String property : properties.keys()) {
      css(property, properties.getStr(property));
    }
    return this;
  }

  /**
   * Return a style property on the first matched element.
   */
  public String css(String name) {
    return css(name, true);
  }

  /**
   * Return a style property on the first matched element.
   * 
   * The parameter force has a special meaning here:
   * <ul>
   * <li>When force is false, returns the value of the css property defined in the style attribute
   * of the element.
   * <li>Otherwise it returns the real computed value.
   * </ul>
   * 
   * For instance if you don't define 'display=none'in the element style but in the css stylesheet,
   * it returns an empty string unless you pass the parameter force=true.
   */
  public String css(String name, boolean force) {
    return isEmpty() ? "" : getStyleImpl().curCSS(get(0), name, force);
  }

  /**
   * Set a single style property to a value, on all matched elements.
   * 
   */
  public GQuery css(String prop, String val) {
    for (Element e : elements) {
      getStyleImpl().setStyleProperty(e, prop, val);
    }
    return this;
  }

  /**
   * Set CSS a single style property on every matched element using type-safe enumerations. This
   * method allows you to set manually the value or set <i>inherit</i> value
   * 
   * ex :
   * 
   * <pre class="code">
   * $(#myId).css(CSS.TEXT_DECORATION, CSS.INHERIT);
   * </pre>
   */
  public GQuery css(TakesCssValue<?> cssProperty, String value) {
    return css(cssProperty.getCssName(), value);
  }

  /**
   * Returns the numeric value of a css property.
   */
  public double cur(String prop) {
    return cur(prop, false);
  }

  /**
   * Returns the numeric value of a css property.
   * 
   * The parameter force has a special meaning: - When force is false, returns the value of the css
   * property defined in the set of style attributes. - When true returns the real computed value.
   */
  public double cur(String prop, boolean force) {
    return isEmpty() ? 0 : getStyleImpl().cur(get(0), prop, force);
  }

  /**
   * Returns value at named data store for the element, as set by data(name, value).
   */
  public Object data(String name) {
    return isEmpty() ? null : data(get(0), name, null);
  }

  /**
   * Returns value at named data store for the element, as set by data(name, value) with desired
   * return type.
   * 
   * @param clz return type class literal
   */
  @SuppressWarnings("unchecked")
  public <T> T data(String name, Class<T> clz) {
    return isEmpty() ? null : (T) data(get(0), name, null);
  }

  /**
   * Stores the value in the named spot with desired return type.
   */
  public GQuery data(String name, Object value) {
    for (Element e : elements()) {
      data(e, name, value);
    }
    return this;
  }

  /**
   * Bind a set of functions to the dblclick event of each matched element. Or trigger the event if
   * no functions are provided.
   */
  public GQuery dblclick(Function... f) {
    return bindOrFire(Event.ONDBLCLICK, null, f);
  }

  /**
   * Insert a delay (in ms) in the GQuery queue, and optionally execute one o more functions if
   * provided when the delay finishes. It uses the effects queue namespace, so you can stack any of
   * the methods in the effects plugin.
   * 
   * Example:
   * 
   * <pre class="code">
   * $("#foo").slideUp(300)
   *          .delay(800)
   *          .fadeIn(400); 
   * </pre>
   * 
   * When this statement is executed, the element slides up for 300 milliseconds and then pauses for
   * 800 milliseconds before fading in for 400 milliseconds. Aditionally after those 800
   * milliseconds the element color is set to red.
   * 
   * NOTE that this methods affects only methods which uses the queue like effects. So the following
   * example is wrong:
   * 
   * <pre>
   * $("#foo").css(CSS.COLOR.with(RGBColor.RED)).delay(800).css(CSS.COLOR.with(RGBColor.BLACK)); 
   * </pre>
   * 
   * The code above will not insert a delay of 800 ms between the css() calls ! For this kind of
   * behavior, you should execute these methods puting them in inline functions passed as argument
   * to the delay() method, or adding them to the queue.
   * 
   * <pre>
   * $("#foo").css(CSS.COLOR.with(RGBColor.RED)).delay(800, lazy().css(CSS.COLOR.with(RGBColor.BLACK)).done()); 
   * $("#foo").css(CSS.COLOR.with(RGBColor.RED)).delay(800).queue(lazy().css(CSS.COLOR.with(RGBColor.BLACK)).dequeue().done()); 
   * </pre>
   */
  public GQuery delay(int milliseconds, Function... f) {
    return as(Queue).delay(milliseconds, f);
  }

  /**
   * Insert a delay (in ms) in the queue identified by the <code>queueName</code> parameter, and
   * optionally execute one o more functions if provided when the delay finishes.
   * 
   * If <code>queueName</code> is null or equats to 'fx', the delay will be inserted to the Effects
   * queue.
   * 
   * Example :
   * 
   * <pre class="code">
   * $("#foo").queue("colorQueue", lazy().css(CSS.COLOR.with(RGBColor.RED)).dequeue("colorQueue").done())
   *          .delay(800, "colorQueue")
   *          .queue("colorQueue", lazy().css(CSS.COLOR.with(RGBColor.BLACK)).dequeue("colorQueue").done()); 
   * </pre>
   * 
   * When this statement is executed, the text color of the element changes to red and then wait for
   * 800 milliseconds before changes the text color to black.
   * 
   */
  public GQuery delay(int milliseconds, String queueName, Function... f) {
    return as(Queue).delay(milliseconds, queueName, f);
  }

  /**
   * Attach <code>handlers</code> to one or more events for all elements that match the
   * <code>selector</code>, now or in the future, based on a specific set of root elements.
   * 
   * Example:
   * 
   * <pre>
   * $("table").delegate("td", Event.ONCLICK, new Function(){
   *  public void f(Element e){
   *  $(e).css(CSS.BACKGROUND_COLOR.with(RGBColor.RED));
   *  }
   * });
   * </pre>
   * 
   * This code above add an handler on click event on all cell (the existing oneand the future cell)
   * of all table. This code is equivalent to :
   * 
   * <pre>
   * $("table").each(new Function(){
   *  public void f(Element table){
   *   $("td", table).live(Event.ONCLICK, new Function(){
   *      public void f(Element e){
   *      $(e).css(CSS.BACKGROUND_COLOR.with(RGBColor.RED));
   *    }
   *  }
   * });
   *
   * </pre>
   * 
   * You can attach the handlers to many events by using the '|' operator ex:
   * 
   * <pre>
   *  $("div.main").delegate(".subMain", Event.ONCLICK | Event.ONDBLCLICK, new Function(){...});
   * </pre>
   */
  public GQuery delegate(String selector, int eventbits, Function... handlers) {
    return delegate(selector, eventbits, null, handlers);
  }

  /**
   * Attach <code>handlers</code> to one or more events for all elements that match the
   * <code>selector</code>, now or in the future, based on a specific set of root elements. The
   * <code>data</code> parameter allows us to pass data to the handler.
   * 
   * Example:
   * 
   * <pre>
   * $("table").delegate("td", "click", new Function(){
   *  public void f(Element e){
   *  $(e).css(CSS.BACKGROUND_COLOR.with(RGBColor.RED));
   *  }
   * });
   * </pre>
   * 
   * This code above add an handler on click event on all cell (the existing oneand the future cell)
   * of all table. This code is equivalent to :
   * 
   * <pre>
   * $("table").each(new Function(){
   *  public void f(Element table){
   *   $("td", table).live("click", new Function(){
   *      public void f(Element e){
   *      $(e).css(CSS.BACKGROUND_COLOR.with(RGBColor.RED));
   *    }
   *  }
   * });
   *
   * </pre>
   * 
   * You can pass attach the handlers to many events by using the '|' operator ex:
   * 
   * <pre>
   *  $("div.main").delegate(".subMain", Event.ONCLICK | Event.ONDBLCLICK, new Function(){...});
   * </pre>
   */
  public GQuery delegate(String selector, int eventbits, Object data, Function... handlers) {

    for (Element e : elements) {
      $(selector, e).live(eventbits, data, handlers);
    }

    return this;
  }

  /**
   * Attach <code>handlers</code> to one or more events for all elements that match the
   * <code>selector</code>, now or in the future, based on a specific set of root elements.
   * 
   * Example:
   * 
   * <pre>
   * $("table").delegate("td", "click", new Function(){
   *  public void f(Element e){
   *  $(e).css(CSS.BACKGROUND_COLOR.with(RGBColor.RED));
   *  }
   * });
   * </pre>
   * 
   * This code above add an handler on click event on all cell (the existing oneand the future cell)
   * of all table. This code is equivalent to :
   * 
   * <pre>
   * $("table").each(new Function(){
   *  public void f(Element table){
   *   $("td", table).live("click", new Function(){
   *      public void f(Element e){
   *      $(e).css(CSS.BACKGROUND_COLOR.with(RGBColor.RED));
   *    }
   *  }
   * });
   *
   * </pre>
   * 
   * You can pass attach the handlers to many events by specifying a String with espaced event type.
   * ex:
   * 
   * <pre>
   *  $("div.main").delegate(".subMain", "click dblclick", new Function(){...});
   * </pre>
   * 
   * </pre>
   */
  public GQuery delegate(String selector, String eventType, Function... handlers) {
    return delegate(selector, eventType, null, handlers);
  }

  /**
   * Attach <code>handlers</code> to one or more events for all elements that match the
   * <code>selector</code>, now or in the future, based on a specific set of root elements.
   * 
   * Example:
   * 
   * <pre>
   * $("table").delegate("td", "click", new Function(){
   *  public void f(Element e){
   *  $(e).css(CSS.BACKGROUND_COLOR.with(RGBColor.RED));
   *  }
   * });
   * </pre>
   * 
   * This code above add an handler on click event on all cell (the existing oneand the future cell)
   * of all table. This code is equivalent to :
   * 
   * <pre>
   * $("table").each(new Function(){
   *  public void f(Element table){
   *   $("td", table).live("click", new Function(){
   *      public void f(Element e){
   *      $(e).css(CSS.BACKGROUND_COLOR.with(RGBColor.RED));
   *    }
   *  }
   * });
   *
   * You can pass attach the handlers to many events by specifying a String with espaced event type.
   * ex:
   * 
   * <pre>
   *  $("div.main").delegate(".subMain", "click dblclick", new Function(){...});
   * </pre>
   * 
   * </pre>
   */
  public GQuery delegate(String selector, String eventType, Object data, Function... handlers) {
    for (Element e : elements) {
      $(selector, e).live(eventType, data, handlers);
    }

    return this;
  }

  /**
   * Execute the next function on the Effects queue for the matched elements. This method is usefull
   * to tell when a function you add in the Effects queue is ended and so the next function in the
   * queue can start.
   * 
   * Note: you should be sure to call dequeue() in all functions of a queue chain, otherwise the
   * queue execution will be stopped.
   */
  public GQuery dequeue() {
    return as(Queue).dequeue();
  }

  /**
   * Execute the next function on the queue named as queueName for the matched elements. This method
   * is usefull to tell when a function you add in the Effects queue is ended and so the next
   * function in the queue can start.
   */
  public GQuery dequeue(String queueName) {
    return as(Queue).dequeue(queueName);
  }

  /**
   * Detach all matched elements from the DOM. This method is the same than {@link #remove()} method
   * except all data and event handlers are not remove from the element. This method is useful when
   * removed elements are to be reinserted into the DOM at a later time.
   */
  public GQuery detach() {
    return remove(null, false);
  }

  /**
   * Detach from the DOM all matched elements filtered by the <code>filter</code>.. This method is
   * the same than {@link #remove(String)} method except all data and event handlers are not remove
   * from the element. This method is useful when removed elements are to be reinserted into the DOM
   * at a later time.
   */
  public GQuery detach(String filter) {
    return remove(filter, false);
  }

  /**
   * Remove all event handlers previously attached using {@link #live(String, Function)}. In order
   * for this method to function correctly, the selector used with it must match exactly the
   * selector initially used with {@link #live(String, Function)}
   */
  public GQuery die() {
    return die(0);
  }

  /**
   * Remove an event handlers previously attached using {@link #live(int, Function)} In order for
   * this method to function correctly, the selector used with it must match exactly the selector
   * initially used with {@link #live(int, Function)}
   */
  public GQuery die(int eventbits) {
    return as(Events).die(eventbits);
  }

  /**
   * Remove an event handlers previously attached using {@link #live(String, Function)} In order for
   * this method to function correctly, the selector used with it must match exactly the selector
   * initially used with {@link #live(String, Function)}
   */
  public GQuery die(String eventName) {
    return as(Events).die(eventName);
  }

  private GQuery domManip(GQuery g, DomMan type, Element... elms) {
    JsNodeArray newNodes = JsNodeArray.create();
    if (elms.length == 0) {
      elms = elements;
    }
    for (int i = 0, l = elms.length; i < l; i++) {
      Element e = elms[i];
      if (e.getNodeType() == Node.DOCUMENT_NODE) {
        e = e.<Document> cast().getBody();
      }
      for (int j = 0, size = g.size(); j < size; j++) {
        // Widget w = getAssociatedWidget(g.get(j));
        // GqUi.detachWidget(w);

        Node n = g.get(j);
        // If an element selected is inserted elsewhere, it will be moved into the target (not
        // cloned).
        // If there is more than one target element, however, cloned copies of the inserted element
        // will be created for each target after the first
        if (i > 0) {
          n = n.cloneNode(true);
        }
        switch (type) {
          case PREPEND:
            newNodes.addNode(e.insertBefore(n, e.getFirstChild()));
            break;
          case APPEND:
            newNodes.addNode(e.appendChild(n));
            break;
          case AFTER:
            newNodes.addNode(e.getParentNode().insertBefore(n, e.getNextSibling()));
            break;
          case BEFORE:
            newNodes.addNode(e.getParentNode().insertBefore(n, e));
            break;
        }
        EventsListener.rebind(n.<Element> cast());
        // GqUi.attachWidget(w);
      }
    }
    // TODO: newNodes.size() > g.size() makes testRebind fail
    if (newNodes.size() >= g.size()) {
      g.setArray(newNodes);
    }
    return this;
  }

  // TODO: this should be handled by the other domManip method
  private GQuery domManip(String htmlString, DomMan type) {
    JsMap<Document, GQuery> cache = JsMap.createObject().cast();
    for (Element e : elements) {
      Document d = JsUtils.getOwnerDocument(e);
      GQuery g = cache.get(d);
      if (g == null) {
        g = cleanHtmlString(htmlString, d);
        cache.put(d, g);
      }
      domManip(g.clone(), type, e);
    }
    return this;
  }

  /**
   * Run one or more Functions over each element o
