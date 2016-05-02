/*
 * GWT-Ext Widget Library
 * Copyright 2007 - 2008, GWT-Ext LLC., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
 
package com.gwtext.client.core;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.gwtext.client.animation.Easing;
import com.gwtext.client.util.KeyMap;
import com.gwtext.client.util.KeyMapConfig;
import com.gwtext.client.widgets.event.KeyListener;

/**
 * Represents a base Element in the DOM.
 *
 * @author Sanjiv Jivan
 */
public class BaseElement extends JsObject implements Fx {

    protected BaseElement() {
    }

    public BaseElement(JavaScriptObject jsObj) {
        super(jsObj);
    }

    /**
     * Adds a CSS class to the element. Duplicate classes are automatically filtered out.
     *
     * @param className the CSS class to add
     * @return this
     */
    public native BaseElement addClass(String className)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.addClass(className);
        return this;
    }-*/;

    /**
     * Adds CSS classes to the element. Duplicate classes are automatically filtered out.
     *
     * @param classNames an array of CSS classes
     * @return this
     */
    public native BaseElement addClass(String[] classNames)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.addClass(@com.gwtext.client.util.JavaScriptObjectHelper::convertToJavaScriptArray([Ljava/lang/Object;)(classNames));
        return this;
    }-*/;

    /**
     * Sets up event handlers to add and remove a css class when the mouse is down and then up on this element (a click effect).
     *
     * @param className the CSS class to add
     * @return this
     */
    public native BaseElement addClassOnClick(String className)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.addClassOnClick(className);
        return this;
    }-*/;

    /**
     * Sets up event handlers to add and remove a css class when this element has the focus
     *
     * @param className the CSS class to add
     * @return this
     */
    public native BaseElement addClassOnFocus(String className)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.addClassOnFocus(className);
        return this;
    }-*/;

    public native BaseElement addClassOnOver(String className)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.addClassOnOver(className);
        return this;
    }-*/;

    /**
     * Sets up event handlers to add and remove a css class when the mouse is over this element.
     *
     * @param className      the CSS class to add
     * @param preventFlicker if set to true, it prevents flickering by filtering mouseout events for children elements
     * @return this
     */
    public native BaseElement addClassOnOver(String className, boolean preventFlicker)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.addClassOnOver(className, preventFlicker);
        return this;
    }-*/;

    /**
     * Convenience method for constructing a KeyMap.
     *
     * @param keyCode  the numeric key code
     * @param listener the key listener
     * @return the KeyMap created
     */
    public native KeyMap addKeyListener(int keyCode, KeyListener listener)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var km = elem.addKeyListener(keyCode, function(key, event) {
                var e = @com.gwtext.client.core.EventObject::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(event);
                listener.@com.gwtext.client.widgets.event.KeyListener::onKey(ILcom/gwtext/client/core/EventObject;)(key, e);
            });
        return @com.gwtext.client.util.KeyMap::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(km);
    }-*/;

    /**
     * Convenience method for constructing a KeyMap.
     *
     * @param keyCodes array of key codes
     * @param listener the key listener
     * @return the KeyMap created
     */
    public native KeyMap addKeyListener(int[] keyCodes, KeyListener listener)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var keyCodesJS = @com.gwtext.client.util.JavaScriptObjectHelper::convertToJavaScriptArray([I)(keyCodes);
        var km = elem.addKeyListener(keyCodesJS, function(key, event) {
                var e = @com.gwtext.client.core.EventObject::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(event);
                listener.@com.gwtext.client.widgets.event.KeyListener::onKey(ILcom/gwtext/client/core/EventObject;)(key, e);
            });
        return @com.gwtext.client.util.KeyMap::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(km);
    }-*/;

    /**
     * Convenience method for constructing a KeyMap.
     *
     * @param keys     a string with the keys to listen for
     * @param listener the key listener
     * @return the KeyMap created
     */
    public native KeyMap addKeyListener(String keys, KeyListener listener)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var km = elem.addKeyListener(keys, function(key, event) {
                var e = @com.gwtext.client.core.EventObject::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(event);
                listener.@com.gwtext.client.widgets.event.KeyListener::onKey(ILcom/gwtext/client/core/EventObject;)(key, e);
            });
        return @com.gwtext.client.util.KeyMap::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(km);
    }-*/;

    /**
     * Convenience method for constructing a KeyMap.
     *
     * @param config the key map config
     * @return the KeyMap created
     */
    public native KeyMap addKeyMap(KeyMapConfig config)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var configJS = config.@com.gwtext.client.core.JsObject::getJsObj()();
        var km = elem.addKeyMap(configJS);
        return @com.gwtext.client.util.KeyMap::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(km);
    }-*/;

    /**
     * Appends an event handler.
     *
     * @param eventName the type of event to append
     * @param cb        the event callback
     */
    public native void addListener(String eventName, EventCallback cb) /*-{
        var el = this.@com.gwtext.client.core.JsObject::getJsObj()();
        el.addListener(eventName, function(event) {
                var e = (event === undefined || event == null) ? null : @com.gwtext.client.core.EventObject::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(event);
                cb.@com.gwtext.client.core.EventCallback::execute(Lcom/gwtext/client/core/EventObject;)(e);
            }
        );
    }-*/;

    /**
     * Appends an event handler.
     *
     * @param eventName the type of event to append
     * @param cb        the event callback
     * @param config    the listener config
     */
    public native void addListener(String eventName, EventCallback cb, ListenerConfig config) /*-{
        var el = this.@com.gwtext.client.core.JsObject::getJsObj()();
        el.addListener(eventName, function(event) {
                var e = (event === undefined || event == null) ? null : @com.gwtext.client.core.EventObject::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(event);
                cb.@com.gwtext.client.core.EventCallback::execute(Lcom/gwtext/client/core/EventObject;)(e);
            },
            null,
            config.@com.gwtext.client.core.JsObject::jsObj
        );
    }-*/;

    /**
     * Aligns this element with another element relative to the specified anchor points. If the other element is the document it aligns it to the viewport.
     *
     * The anchorPosition value is separated by a dash, the first value is used as the element's anchor point, and the second value is used as the target's anchor point.
     * In addition to the anchor points, the position parameter also supports the "?" character.  If "?" is passed at the end of the position string, the element will
     * attempt to align as specified, but the position will be adjusted to constrain to the viewport if necessary.  Note that the element being aligned might be swapped
     * to align to a different position than that specified in order to enforce the viewport constraints.
     * Following are all of the supported anchor positions:
     *
     * <pre >Value  Description
     * -----  -----------------------------
     * tl     The top left corner (default)
     * t      The center of the top edge
     * tr     The top right corner
     * l      The center of the left edge
     * c      In the center of the element
     * r      The center of the right edge
     * bl     The bottom left corner
     * b      The center of the bottom edge
     * br     The bottom right corner</pre>
     *
     * Example Usage:
     * <pre ><code ><i>
     * // align el to other-el using the <b>default</b> positioning (<em >"tl-bl"</em>, non-constrained)</i>
     * el.alignTo(<em id="ext-gen1086">"other-el"</em>);
     * <i id="ext-gen1079">// align the top left corner of el <b>with</b> the top right corner of other-el (constrained to viewport)</i>
     * el.alignTo(<em id="ext-gen1085">"other-el"</em>, <em id="ext-gen1097">"tr?"</em>);
     *
     * <i id="ext-gen1080">// align the bottom right corner of el <b>with</b> the center left edge of other-el</i>
     * el.alignTo(<em id="ext-gen1084">"other-el"</em>, <em id="ext-gen1098">"br-l?"</em>);
     * <i id="ext-gen1083">// align the center of el <b>with</b> the bottom left corner of other-el and</i>
     * <i id="ext-gen1082">// adjust the x position by -6 pixels (and the y position by 0)</i>
     * el.alignTo(<em>"other-el"</em>, <em id="ext-gen1099">"c-bl"</em>, new int[]{-6, 0});</code></pre>
     *
     * @param id             the element to align to
     * @param anchorPosition the element's anchor point
     * @return this
     */
    public BaseElement alignTo(final String id, final String anchorPosition) {
        DeferredCommand.addCommand(new Command() {
            public void execute() {
                doAlignTo(id, anchorPosition);
            }
        });
        return this;
    }

    private native BaseElement doAlignTo(String id, String anchorPosition)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.alignTo(id, anchorPosition);
        return this;
    }-*/;

    /**
     * Aligns this element with another element relative to the specified anchor points. If the other element is the document it aligns it to the viewport.
     *
     * The anchorPosition value is separated by a dash, the first value is used as the element's anchor point, and the second value is used as the target's anchor point.
     * In addition to the anchor points, the position parameter also supports the "?" character.  If "?" is passed at the end of the position string, the element will
     * attempt to align as specified, but the position will be adjusted to constrain to the viewport if necessary.  Note that the element being aligned might be swapped
     * to align to a different position than that specified in order to enforce the viewport constraints.
     * Following are all of the supported anchor positions:
     *
     * <pre >Value  Description
     * -----  -----------------------------
     * tl     The top left corner (default)
     * t      The center of the top edge
     * tr     The top right corner
     * l      The center of the left edge
     * c      In the center of the element
     * r      The center of the right edge
     * bl     The bottom left corner
     * b      The center of the bottom edge
     * br     The bottom right corner</pre>
     *
     * Example Usage:
     * <pre ><code ><i>
     * // align el to other-el using the <b>default</b> positioning (<em >"tl-bl"</em>, non-constrained)</i>
     * el.alignTo(<em id="ext-gen1086">"other-el"</em>);
     * <i id="ext-gen1079">// align the top left corner of el <b>with</b> the top right corner of other-el (constrained to viewport)</i>
     * el.alignTo(<em id="ext-gen1085">"other-el"</em>, <em id="ext-gen1097">"tr?"</em>);
     *
     * <i id="ext-gen1080">// align the bottom right corner of el <b>with</b> the center left edge of other-el</i>
     * el.alignTo(<em id="ext-gen1084">"other-el"</em>, <em id="ext-gen1098">"br-l?"</em>);
     * <i id="ext-gen1083">// align the center of el <b>with</b> the bottom left corner of other-el and</i>
     * <i id="ext-gen1082">// adjust the x position by -6 pixels (and the y position by 0)</i>
     * el.alignTo(<em>"other-el"</em>, <em id="ext-gen1099">"c-bl"</em>, new int[]{-6, 0});</code></pre>
     *
     * @param id             the element to align to
     * @param anchorPosition the element's anchor point
     * @param offsetXY       offset the positioning by [x, y]
     * @param animate        true for the default animation
     * @return this
     */
    public native BaseElement alignTo(String id, String anchorPosition, int[] offsetXY, boolean animate)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var offsetJS = @com.gwtext.client.util.JavaScriptObjectHelper::convertToJavaScriptArray([I)(offsetXY);
        elem.alignTo(id, anchorPosition, offsetJS, animate);
        return this;
    }-*/;

    /**
     * Aligns this element with another element relative to the specified anchor points. If the other element is the document it aligns it to the viewport.
     *
     * @param id             the element to align to
     * @param anchorPosition the element's anchor point
     * @return this
     */
    public native BaseElement anchorTo(String id, String anchorPosition)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.anchorTo(id, anchorPosition);
        return this;
    }-*/;

    //todo fix ext inconsistency
    /**
     * Aligns this element with another element relative to the specified anchor points. If the other element is the document it aligns it to the viewport.
     *
     * @param id             the element to align to
     * @param anchorPosition the element's anchor point
     * @param offsetXY       offset the positioning by [x, y]
     * @param animate        true for the default animation
     * @param bufferDelay    buffer delay
     * @return this
     */
    public native BaseElement anchorTo(String id, String anchorPosition, int[] offsetXY, boolean animate, int bufferDelay)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var offsetJS = @com.gwtext.client.util.JavaScriptObjectHelper::convertToJavaScriptArray([I)(offsetXY);
        elem.anchorTo(id, anchorPosition, offsetJS, animate, bufferDelay);
        return this;
    }-*/;

    /**
     * Perform animation on this element. For example :
     * <code><pre>
     * GenericConfig animArgs = new GenericConfig();
     * GenericConfig widthArgs = new GenericConfig();
     * widthArgs.setProperty("from", 600);
     * widthArgs.setProperty("to", 0);
     * animArgs.setProperty("width", widthArgs);
     * el.animate(animArgs);
     * </pre></code>
     *
     * @param args animation control args
     * @return this
     */
    public native BaseElement animate(GenericConfig args)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var argsJS = args == null ? null : args.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.animate(argsJS);
        return this;
    }-*/;

    /**
     * Perform animation on this element.
     *
     * @param args       animation control args
     * @param duration   how long the animation lasts in seconds (defaults to .35)
     * @param onComplete function to call when animation completes
     * @param easing     Easing method to use (defaults to easeOut) easeOut. See http://developer.yahoo.com/yui/docs/YAHOO.util.Easing.html
     * @param animType   'run' is the default. Can also be 'color', 'motion', or 'scroll'
     * @return this
     */
    public native BaseElement animate(GenericConfig args, float duration, Function onComplete, Easing easing, String animType)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var argsJS = args == null ? null : args.@com.gwtext.client.core.JsObject::getJsObj()();
        var easingJS = easing.@com.gwtext.client.animation.Easing::getMethod();
        elem.animate(argsJS, duration, onComplete == null ? null : function() {
            onComplete.@com.gwtext.client.core.Function::execute()();
        }, easingJS, animType);
        return this;
    }-*/;

    /**
     * More flexible version of {@link #setStyle} for setting style properties.
     *
     * @param style a style specification string, e.g. "width:100px"
     * @return this
     */
    public native BaseElement applyStyles(String style) /*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.applyStyles(style);
        return this;
    }-*/;

    /**
     * Measures the element's content height and updates height to match. Note: this function uses setTimeout so the new height may not be available immediately.
     *
     * @return this
     */
    public native BaseElement autoHeight() /*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.autoHeight();
        return this;
    }-*/;

    //todo fix ext inconsisteny
    /**
     * Measures the element's content height and updates height to match. Note: this function uses setTimeout so the new height may not be available immediately.
     *
     * @param animate    animate the transition (defaults to false)
     * @param duration   length of the animation in seconds (defaults to .35)
     * @param onComplete Function to call when animation completes
     * @param easing Easing method to use (defaults to easeOut)
     * @return this
     */
    public native BaseElement autoHeight(boolean animate, float duration, Function onComplete, Easing easing)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var easingJS = easing.@com.gwtext.client.animation.Easing::getMethod();
        elem.autoHeight(animate, duration, function() {
            onComplete.@com.gwtext.client.core.Function::execute()();
        }, easingJS);
        return this;
    }-*/;

    /**
     * Removes worthless text nodes.
     */
    public native void clean() /*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.clean();
    }-*/;

    /**
     * Removes worthless text nodes.
     *
     * @param forceClean by default the element keeps track if it has been cleaned already so you can call this over and over. However, if you update the element and need to force a reclean, you can pass true.
     */
    public native void clean(boolean forceClean) /*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.clean(forceClean);
    }-*/;

    /**
     * Clears any opacity settings from this element. Required in some cases for IE.
     *
     * @return this
     */
    public native BaseElement clearOpacity() /*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.clearOpacity();
        return this;
    }-*/;

    /**
     * Store the current overflow setting and clip overflow on the element - use unclip to remove.
     *
     * @return this
     */
    public native BaseElement clip() /*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.clip();
        return this;
    }-*/;

    /**
     * Creates an iframe shim for this element to keep selects and other windowed objects from showing through.
     *
     * @return the new shim element
     */
    public native ExtElement createShim() /*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var el = elem.shim();
        return @com.gwtext.client.core.ExtElement::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(el);
    }-*/;

    /**
     * Convenience method for setVisibilityMode(Element.DISPLAY)
     *
     * @return this
     */
    public native BaseElement enableDisplayMode() /*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.enableDisplayMode();
        return this;
    }-*/;

    /**
     * Convenience method for setVisibilityMode(Element.DISPLAY)
     *
     * @param display what to set display to when visible
     * @return this
     */
    public native BaseElement enableDisplayMode(String display) /*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.enableDisplayMode(display);
        return this;
    }-*/;

    /**
     * Hide this element - Uses display mode to determine whether to use "display" or "visibility". See {@link #setVisible}.
     *
     * @return this
     */
    public native BaseElement hide()/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.hide();
        return this;
    }-*/;

    /**
     * Hide this element - Uses display mode to determine whether to use "display" or "visibility". See {@link #setVisible}.
     *
     * @param animate true for the default animation
     * @return this
     */
    public native BaseElement hide(boolean animate)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.hide(animate);
        return this;
    }-*/;

    /**
     * Hide this element - Uses display mode to determine whether to use "display" or "visibility". See {@link #setVisible}.
     *
     * @param animateConfig the animation config
     * @return this
     */
    public native BaseElement hide(AnimationConfig animateConfig)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var animateConfigJS = animateConfig.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.hide(animateConfigJS);
        return this;
    }-*/;

    /**
     * Move this element relative to its current position.
     *
     * @param direction the direction
     * @param distance  how far to move the element in pixels
     * @return this
     */
    public native BaseElement move(Direction direction, int distance)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var directionJS = direction.@com.gwtext.client.core.Direction::getDirection()();
        elem.move(directionJS, distance);
        return this;
    }-*/;

    /**
     * Move this element relative to its current position.
     *
     * @param direction the direction
     * @param distance  how far to move the element in pixels
     * @param animate   true for the default animation
     * @return this
     */
    public native BaseElement move(Direction direction, int distance, boolean animate)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var directionJS = direction.@com.gwtext.client.core.Direction::getDirection()();
        elem.move(directionJS, distance, animate);
        return this;
    }-*/;

    /**
     * Move this element relative to its current position.
     *
     * @param direction     the direction
     * @param distance      how far to move the element in pixels
     * @param animateConfig the animation config
     * @return this
     */
    public native BaseElement move(Direction direction, int distance, AnimationConfig animateConfig)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var animateConfigJS = animateConfig.@com.gwtext.client.core.JsObject::getJsObj()();
        var directionJS = direction.@com.gwtext.client.core.Direction::getDirection()();
        elem.move(directionJS, distance, animateConfigJS);
        return this;
    }-*/;

    /**
     * Sets the position of the element in page coordinates, regardless of how the element is positioned. the element must be part of the DOM tree to have page coordinates (display:none or elements not appended return false).
     *
     * @param x X value for new position (coordinates are page-based)
     * @param y Y value for new position (coordinates are page-based)
     * @return this
     */
    public native BaseElement moveTo(int x, int y)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.moveTo(x, y);
        return this;
    }-*/;

    /**
     * Sets the position of the element in page coordinates, regardless of how the element is positioned. the element must be part of the DOM tree to have page coordinates (display:none or elements not appended return false).
     *
     * @param x       X value for new position (coordinates are page-based)
     * @param y       Y value for new position (coordinates are page-based)
     * @param animate true to animate
     * @return this
     */
    public native BaseElement moveTo(int x, int y, boolean animate)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.moveTo(x, y, animate);
        return this;
    }-*/;

    /**
     * Sets the position of the element in page coordinates, regardless of how the element is positioned. the element must be part of the DOM tree to have page coordinates (display:none or elements not appended return false).
     *
     * @param x             X value for new position (coordinates are page-based)
     * @param y             Y value for new position (coordinates are page-based)
     * @param animateConfig the animcation config
     * @return this
     */
    public native BaseElement moveTo(int x, int y, AnimationConfig animateConfig)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var animateConfigJS = animateConfig.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.moveTo(x, y, animateConfigJS);
        return this;
    }-*/;

    /**
     * Adds a CSS class to this element and removes the same class(es) from all siblings.
     *
     * @param className the CSS class to add
     * @return this
     */
    public native BaseElement radioClass(String className)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.radioClass(className);
        return this;
    }-*/;

    /**
     * Adds one or more CSS classes to this element and removes the same class(es) from all siblings.
     *
     * @param classNames the CSS classes to add
     * @return this
     */
    public native BaseElement radioClass(String[] classNames)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var classNamesJS = @com.gwtext.client.util.JavaScriptObjectHelper::convertToJavaScriptArray([Ljava/lang/Object;)(classNames);
        elem.radioClass(classNamesJS);
        return this;
    }-*/;

    /**
     * Removes this element from the DOM and deletes it from the cache.
     */
    public native void remove()/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.remove();
    }-*/;

    /**
     * Removes all previous added listeners from this element.
     */
    public native void removeAllListeners()/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.removeAllListeners();
    }-*/;

    /**
     * Removes the CSS classes from the element.
     *
     * @param className the CSS class to remove
     * @return this
     */
    public native BaseElement removeClass(String className)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.removeClass(className);
        return this;
    }-*/;

    /**
     * Removes the CSS classes from the element.
     *
     * @param classNames the CSS classes to remove
     * @return this
     */
    public native BaseElement removeClass(String[] classNames)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var classNamesJS = @com.gwtext.client.util.JavaScriptObjectHelper::convertToJavaScriptArray([Ljava/lang/Object;)(classNames);
        elem.removeClass(classNamesJS);
        return this;
    }-*/;

    /**
     * Forces the browser to repaint this element.
     *
     * @return this
     */
    public native BaseElement repaint()/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.repaint();
        return this;
    }-*/;

    /**
     * Replaces a CSS class on the element with another. If the old name does not exist, the new name will simply be added.
     *
     * @param oldClassName the CSS class to replace
     * @param newClassName the replacement CSS class
     * @return this
     */
    public native BaseElement replaceClass(String oldClassName, String newClassName)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.replaceClass(oldClassName, newClassName);
        return this;
    }-*/;

    /**
     * Scrolls this element the specified direction. Does bounds checking to make sure the scroll is within this element's scrollable range.
     *
     * @param direction the direction
     * @param distance  how far to scroll the element in pixels
     * @param animate   true to animate
     * @return true if a scroll was triggered or false if the element was scrolled as far as it could go.
     */
    public native boolean scroll(Direction direction, int distance, boolean animate)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var directionJS = direction.@com.gwtext.client.core.Direction::getDirection()();
        var ret =  elem.scroll(directionJS, distance, animate);
        return ret === undefined || ret == null ? false : ret;
    }-*/;

    /**
     * Scrolls this element the specified direction. Does bounds checking to make sure the scroll is within this element's scrollable range.
     *
     * @param direction     the direction
     * @param distance      how far to scroll the element in pixels
     * @param animateConfig the animation config
     * @return true if a scroll was triggered or false if the element was scrolled as far as it could go.
     */
    public native boolean scroll(Direction direction, int distance, AnimationConfig animateConfig)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var animateConfigJS = animateConfig.@com.gwtext.client.core.JsObject::getJsObj()();
        var directionJS = direction.@com.gwtext.client.core.Direction::getDirection()();
        var ret = elem.scroll(directionJS, distance, animateConfigJS);
        return ret === undefined || ret == null ? false : ret;
    }-*/;

    /**
     * Scrolls this element the specified scroll point. It does NOT do bounds checking so if you scroll to a weird value it will try to do it. For auto bounds checking, use scroll().
     *
     * @param side    Either "left" for scrollLeft values or "top" for scrollTop values.
     * @param value   the new scroll value
     * @param animate true for the default animation
     * @return this
     */
    public native BaseElement scrollTo(String side, int value, boolean animate)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.scrollTo(side, value, animate);
        return this;
    }-*/;

    /**
     * Scrolls this element the specified scroll point. It does NOT do bounds checking so if you scroll to a weird value it will try to do it. For auto bounds checking, use scroll().
     *
     * @param side          Either "left" for scrollLeft values or "top" for scrollTop values.
     * @param value         the new scroll value
     * @param animateConfig the animation config
     * @return this
     */
    public native BaseElement scrollTo(String side, int value, AnimationConfig animateConfig)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var animateConfigJS = animateConfig.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.scrollTo(side, value, animateConfigJS);
        return this;
    }-*/;


    /**
     * Sets the element's CSS bottom style.
     *
     * @param bottom the bottom CSS property value
     * @return this
     */
    public native BaseElement setBottom(String bottom)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.setBottom(bottom);
        return this;
    }-*/;

    /**
     * Sets the element's position and size in one shot.
     *
     * @param x      X value for new position (coordinates are page-based)
     * @param y      Y value for new position (coordinates are page-based)
     * @param width  the new width
     * @param height the new height
     * @return this
     */
    public native BaseElement setBounds(int x, int y, int width, int height)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.setBounds(x, y, width, height);
        return this;
    }-*/;

    /**
     * Sets the element's position and size in one shot. If animation is true then width, height, x and y will be animated concurrently.
     *
     * @param x       X value for new position (coordinates are page-based)
     * @param y       Y value for new position (coordinates are page-based)
     * @param width   the new width
     * @param height  the new height
     * @param animate true to animate
     * @return this
     */
    public native BaseElement setBounds(int x, int y, int width, int height, boolean animate)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.setBounds(x, y, width, height, animate);
        return this;
    }-*/;

    /**
     * Sets the element's position and size in one shot. If animation is true then width, height, x and y will be animated concurrently.
     *
     * @param x             X value for new position (coordinates are page-based)
     * @param y             Y value for new position (coordinates are page-based)
     * @param width         the new width
     * @param height        the new height
     * @param animateConfig the  animation config
     * @return this
     */
    public native BaseElement setBounds(int x, int y, int width, int height, AnimationConfig animateConfig)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var animateConfigJS = animateConfig.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.setBounds(x, y, width, height, animateConfigJS);
        return this;
    }-*/;

    /**
     * Sets the element's box. Use getBox() on another element to get a box obj.
     *
     * @param box the box to fill {x, y, width, height}
     * @return this
     */
    public native BaseElement setBox(Box box)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var boxJS = box.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.setBox(boxJS);
        return this;
    }-*/;

    /**
     * Sets the element's box. Use getBox() on another element to get a box obj. If animate is true then width, height, x and y will be animated concurrently.
     *
     * @param box     the box to fill {x, y, width, height}
     * @param adjust  Whether to adjust for box-model issues automatically
     * @param animate true for the default animation
     * @return this
     */
    public native BaseElement setBox(Box box, boolean adjust, boolean animate)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var boxJS = box.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.setBox(boxJS, adjust, animate);
        return this;
    }-*/;

    /**
     * Sets the element's box. Use getBox() on another element to get a box obj. If animate is true then width, height, x and y will be animated concurrently.
     *
     * @param box           the box to fill {x, y, width, height}
     * @param adjust        Whether to adjust for box-model issues automatically
     * @param animateConfig the animation config
     * @return this
     */
    public native BaseElement setBox(Box box, boolean adjust, AnimationConfig animateConfig)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var boxJS = box.@com.gwtext.client.core.JsObject::getJsObj()();
        var animateConfigJS = animateConfig.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.setBox(boxJS, adjust, animateConfigJS);
        return this;
    }-*/;

    /**
     * Sets the CSS display property. Uses originalDisplay if the specified value is a boolean true.
     *
     * @param value Boolean value to display the element using its default display
     * @return this
     */
    public native BaseElement setDisplayed(boolean value)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.setDisplayed(value);
        return this;
    }-*/;

    /**
     * Set the height of the element.
     *
     * @param height  the new height
     * @param animate true to animate
     * @return this
     */
    public native BaseElement setHeight(int height, boolean animate)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.setHeight(height, animate);
        return this;
    }-*/;

    /**
     * Set the height of the element.
     *
     * @param height        the new height
     * @param animateConfig the animation config
     * @return this
     */
    public native BaseElement setHeight(int height, AnimationConfig animateConfig)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var animateConfigJS = animateConfig.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.setHeight(height, animateConfigJS);
        return this;
    }-*/;

    /**
     * Sets the element's left position directly using CSS style (instead of setX).
     *
     * @param left the left CSS property value
     * @return this
     */
    public native BaseElement setLeft(String left)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.setLeft(left);
        return this;
    }-*/;

    /**
     * Quick set left and top adding default units.
     *
     * @param left the left CSS property value
     * @param top  the top CSS property value
     * @return this
     */
    public native BaseElement setLeftTop(String left, String top)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.setLeft(left);
        return this;
    }-*/;

    /**
     * Sets the position of the element in page coordinates, regardless of how the element is positioned. the element must be part of the DOM tree to have page coordinates (display:none or elements not appended return false).
     *
     * @param x       X value for new position (coordinates are page-based)
     * @param y       Y value for new position (coordinates are page-based)
     * @param animate true to animate
     * @return this
     */
    public native BaseElement setLocation(int x, int y, boolean animate)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.setLocation(x, y, animate);
        return this;
    }-*/;

    /**
     * Sets the position of the element in page coordinates, regardless of how the element is positioned. the element must be part of the DOM tree to have page coordinates (display:none or elements not appended return false).
     *
     * @param x             X value for new position (coordinates are page-based)
     * @param y             Y value for new position (coordinates are page-based)
     * @param animateConfig the animation config
     * @return this
     */
    public native BaseElement setLocation(int x, int y, AnimationConfig animateConfig)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var animateConfigJS = animateConfig.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.setLocation(x, y, animateConfigJS);
        return this;
    }-*/;

    /**
     * Set the opacity of the element.
     *
     * @param opacity the new opacity. 0 = transparent, .5 = 50% visibile, 1 = fully visible, etc
     * @param animate true to animate
     * @return this
     */
    public native BaseElement setOpacity(float opacity, boolean animate)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.setOpacity(opacity, animate);
        return this;
    }-*/;

    /**
     * Set the opacity of the element.
     *
     * @param opacity       the new opacity. 0 = transparent, .5 = 50% visibile, 1 = fully visible, etc
     * @param animateConfig the animation config
     * @return this
     */
    public native BaseElement setOpacity(float opacity, AnimationConfig animateConfig)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var animateConfigJS = animateConfig.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.setOpacity(opacity, animateConfigJS);
        return this;
    }-*/;

    /**
     * Sets the element's position and size the the specified region. If animation is true then width, height, x and y will be animated concurrently.
     *
     * @param region  the region to fill
     * @param animate true to animate
     * @return this
     */
    public native BaseElement setRegion(Region region, boolean animate)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var regionJS = region.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.setRegion(regionJS, animate);
        return this;
    }-*/;

    /**
     * Sets the element's position and size the the specified region. If animation is true then width, height, x and y will be animated concurrently.
     *
     * @param region        the region to fill
     * @param animateConfig the animation config
     * @return this
     */
    public native BaseElement setRegion(Region region, AnimationConfig animateConfig)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var regionJS = region.@com.gwtext.client.core.JsObject::getJsObj()();
        var animateConfigJS = animateConfig.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.setRegion(regionJS, animateConfigJS);
        return this;
    }-*/;

    /**
     * Sets the element's CSS right style.
     *
     * @param right the right CSS property value
     * @return this
     */
    public native BaseElement setRight(String right)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.setRight(right);
        return this;
    }-*/;

    /**
     * Set the size of the element. If animation is true, both width an height will be animated concurrently.
     *
     * @param width   the new width
     * @param height  the new height
     * @param animate true to animate
     * @return this
     */
    public native BaseElement setSize(int width, int height, boolean animate)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.setSize(width, height, animate);
        return this;
    }-*/;

    /**
     * Set the size of the element. If animation is true, both width an height will be animated concurrently.
     *
     * @param width         the new width
     * @param height        the new height
     * @param animateConfig the animation config
     * @return this
     */
    public native BaseElement setSize(int width, int height, AnimationConfig animateConfig)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var animateConfigJS = animateConfig.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.setSize(width, height, animateConfigJS);
        return this;
    }-*/;

    /**
     * Wrapper for setting style properties.
     *
     * @param style the style property to be set
     * @param value the value to apply to the given property
     * @return this
     */
    public native BaseElement setStyle(String style, String value)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.setStyle(style, value);
        return this;
    }-*/;

    /**
     * Wrapper for setting style properties.
     *
     * @param styles the style property to be set
     * @return this
     */
    public native BaseElement setStyles(GenericConfig styles)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var stylesJS = styles.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.setStyle(stylesJS);
        return this;
    }-*/;

    /**
     * Sets the element's top position directly using CSS style (instead of setY).
     *
     * @param top the top CSS property value
     * @return this
     */
    public native BaseElement setTop(String top)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.setTop(top);
        return this;
    }-*/;

    /**
     * Sets the element's visibility mode. When setVisible() is called it will use this to determine whether to set the visibility or the display property.
     *
     * @param useVisibleProperty true to use VISIBILITY, false for DISPLAY
     * @return this
     */
    public native BaseElement setVisibilityMode(boolean useVisibleProperty)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.setVisibilityMode(useVisibleProperty ? $wnd.Ext.Element.VISIBILITY: $wnd.Ext.Element.DISPLAY);
        return this;
    }-*/;

    /**
     * Sets the visibility of the element. If the visibilityMode is set to Element.DISPLAY, it will use the display
     * property to hide the element, otherwise it uses visibility. the default is to hide and show using the visibility property.
     *
     * @param visible Whether the element is visible
     * @return this
     */
    public BaseElement setVisible(boolean visible) {
        setVisible(visible, false);
        return this;
    }

    /**
     * Sets the visibility of the element. If the visibilityMode is set to Element.DISPLAY, it will use the display
     * property to hide the element, otherwise it uses visibility. the default is to hide and show using the visibility property.
     *
     * @param visible Whether the element is visible
     * @param animate true to animate
     * @return this
     */
    public native BaseElement setVisible(boolean visible, boolean animate)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.setVisible(visible, animate);
        return this;
    }-*/;

    /**
     * Sets the visibility of the element. If the visibilityMode is set to Element.DISPLAY, it will use the display
     * property to hide the element, otherwise it uses visibility. the default is to hide and show using the visibility property.
     *
     * @param visible       Whether the element is visible
     * @param animateConfig the animation config
     * @return this
     */
    public native BaseElement setVisible(boolean visible, AnimationConfig animateConfig)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var animateConfigJS = animateConfig.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.setVisible(visible, animateConfigJS);
        return this;
    }-*/;

    /**
     * Set the width of the element.
     *
     * @param width   the new width
     * @param animate true to animate
     * @return this
     */
    public native BaseElement setWidth(int width, boolean animate)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.setWidth(width, animate);
        return this;
    }-*/;

    /**
     * Set the width of the element.
     *
     * @param width   the new width
     * @param animate true to animate
     * @return this
     */
    public native BaseElement setWidth(String width, boolean animate)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.setWidth(width, animate);
        return this;
    }-*/;

    /**
     * Set the width of the element.
     *
     * @param width         the new width
     * @param animateConfig the animation config
     * @return this
     */
    public native BaseElement setWidth(int width, AnimationConfig animateConfig)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var animateConfigJS = animateConfig.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.setWidth(width, animateConfigJS);
        return this;
    }-*/;

    /**
     * Sets the X position of the element based on page coordinates. Element must be part of the DOM tree to have page coordinates (display:none or elements not appended return false).
     *
     * @param x       X position of the element
     * @param animate true to animate
     * @return this
     */
    public native BaseElement setX(int x, boolean animate)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.setX(x, animate);
        return this;
    }-*/;

    /**
     * Sets the X position of the element based on page coordinates. Element must be part of the DOM tree to have page coordinates (display:none or elements not appended return false).
     *
     * @param x             X position of the element
     * @param animateConfig the animation config
     * @return this
     */
    public native BaseElement setX(int x, AnimationConfig animateConfig)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var animateConfigJS = animateConfig.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.setX(x, animateConfigJS);
        return this;
    }-*/;

    /**
     * Sets the position of the element in page coordinates, regardless of how the element is positioned. the element must be part of the DOM tree to have page coordinates
     * (display:none or elements not appended return false).
     *
     * @param x       X position of the element
     * @param y       Y position of the element
     * @param animate true to animate
     * @return this
     */
    public native BaseElement setXY(int x, int y, boolean animate)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.setXY([x, y], animate);
        return this;
    }-*/;

    /**
     * Sets the position of the element in page coordinates, regardless of how the element is positioned. the element must be part of the DOM tree to have page coordinates
     * (display:none or elements not appended return false).
     *
     * @param x             X position of the element
     * @param y             Y position of the element
     * @param animateConfig the animation config
     * @return this
     */
    public native BaseElement setXY(int x, int y, AnimationConfig animateConfig)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var animateConfigJS = animateConfig.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.setXY([x, y], animateConfigJS);
        return this;
    }-*/;

    /**
     * Sets the Y position of the element based on page coordinates. Element must be part of the DOM tree to have page
     * coordinates (display:none or elements not appended return false).
     *
     * @param y       Y position of the element
     * @param animate true to animate
     * @return this
     */
    public native BaseElement setY(int y, boolean animate)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.setY(y, animate);
        return this;
    }-*/;

    /**
     * Sets the Y position of the element based on page coordinates. Element must be part of the DOM tree to have page
     * coordinates (display:none or elements not appended return false).
     *
     * @param y             Y position of the element
     * @param animateConfig the animation config
     * @return this
     */
    public native BaseElement setY(int y, AnimationConfig animateConfig)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var animateConfigJS = animateConfig.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.setY(y, animateConfigJS);
        return this;
    }-*/;

    /**
     * Show this element - Uses display mode to determine whether to use "display" or "visibility". See {@link #setVisible}.
     *
     * @param animate true to animate
     * @return this
     */
    public native BaseElement show(boolean animate)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.show(animate);
        return this;
    }-*/;

    /**
     * Show this element - Uses display mode to determine whether to use "display" or "visibility". See {@link #setVisible}.
     *
     * @param animateConfig the animation config
     * @return this
     */
    public native BaseElement show(AnimationConfig animateConfig)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var animateConfigJS = animateConfig.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.show(animateConfigJS);
        return this;
    }-*/;

    /**
     * Stops the specified event from bubbling and optionally prevents the default action.
     *
     * @param eventName the event name
     * @return this
     */
    public native BaseElement swallowEvent(String eventName)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.swallowEvent(eventName);
        return this;
    }-*/;

    /**
     * Stops the specified event from bubbling and optionally prevents the default action.
     *
     * @param eventName      the event name
     * @param preventDefault true to prevent the default action too
     * @return this
     */
    public native BaseElement swallowEvent(String eventName, boolean preventDefault)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.swallowEvent(eventName, preventDefault);
        return this;
    }-*/;

    /**
     * Toggles the element's visibility or display, depending on visibility mode.
     *
     * @param animate true to animate
     * @return this
     */
    public native BaseElement toggle(boolean animate)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.toggle(animate);
        return this;
    }-*/;

    /**
     * Toggles the element's visibility or display, depending on visibility mode.
     *
     * @param animateConfig the animation config
     * @return this
     */
    public native BaseElement toggle(AnimationConfig animateConfig)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var animateConfigJS = animateConfig.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.toggle(animateConfigJS);
        return this;
    }-*/;

    /**
     * Toggles the specified CSS class on this element (removes it if it already exists, otherwise adds it).
     *
     * @param className the CSS class to toggle
     * @return this
     */
    public native BaseElement toggleClass(String className)/*-{
        var elem = this.@com.gwtext.client.core.JsObject::getJsObj()();
        elem.toggleClass(className);
        return this;
    }-*/;

    /**
     * Disables text selection for this element (normalized across browsers).
     */
    public native void unselectable() /*-{
        var el = this.@com.gwtext.client.core.JsObject::getJsObj()();
        el.unselectable();
    }-*/;

    /**
     * Update the innerHTML of this element.
     *
     * @param html the new HTML
     */
    public void update(String html) {
        update(html, false);
    }

    /**
     * Update the innerHTML of this element, optionally searching for and processing scripts.
     *
     * @param html        the new HTML
     * @param loadScripts true to look for  and process scripts
     */
    public native void update(String html, boolean loadScripts) /*-{
        var el = this.@com.gwtext.client.core.JsObject::getJsObj()();
        el.update(html, loadScripts);
    }-*/;

    /**
     * Creates and wraps this element with another element.
     *
     * @param config DomHelper element config object for the wrapper element
     * @return this
     */
    public native Element wrap(DomConfig config) /*-{
        var el = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var configJS = config.@com.gwtext.client.core.DomConfig::getJsObject()();
        return el.wrap(configJS, true);
    }-*/;

    //Ext Fx API's
    public native Fx fadeIn() /*-{
        var el = this.@com.gwtext.client.core.JsObject::getJsObj()();
        el.fadeIn();
        return this;
    }-*/;

    public native Fx fadeIn(FxConfig config) /*-{
        var el = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var configJS = config.@com.gwtext.client.core.JsObject::getJsObj()();
        el.fadeIn(configJS);
        return this;
    }-*/;

    public native Fx fadeOut() /*-{
        var el = this.@com.gwtext.client.core.JsObject::getJsObj()();
        el.fadeOut();
        return this;
    }-*/;

    public native Fx fadeOut(FxConfig config) /*-{
        var el = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var configJS = config.@com.gwtext.client.core.JsObject::getJsObj()();
        el.fadeOut(configJS);
    return this;
    }-*/;

    public native Fx frame() /*-{
        var el = this.@com.gwtext.client.core.JsObject::getJsObj()();
        el.frame();
        return this;
    }-*/;

    public native Fx frame(String color, int count, FxConfig config) /*-{
        var el = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var configJS = config.@com.gwtext.client.core.JsObject::getJsObj()();
        el.frame(color, count, configJS);
        return this;
    }-*/;

    public native Fx ghost() /*-{
        var el = this.@com.gwtext.client.core.JsObject::getJsObj()();
        el.ghost();
        return this;
    }-*/;

    public native Fx ghost(String anchorPosition, FxConfig config) /*-{
        var el = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var configJS = config.@com.gwtext.client.core.JsObject::getJsObj()();
        el.ghost(anchorPosition, configJS);
        return this;
    }-*/;

    public native boolean hasActiveFx() /*-{
        var el = this.@com.gwtext.client.core.JsObject::getJsObj()();
        return el.hasActiveFx();
    }-*/;

    public native boolean hasFxBlock() /*-{
        var el = this.@com.gwtext.client.core.JsObject::getJsObj()();
        return el.hasFxBlock();
    }-*/;

    public native Fx highlight() /*-{
        var el = this.@com.gwtext.client.core.JsObject::getJsObj()();
        el.highlight();
        return this;
    }-*/;

    public native Fx highlight(String color, FxConfig config) /*-{
        var el = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var configJS = config.@com.gwtext.client.core.JsObject::getJsObj()();
        el.highlight(color, configJS);
        return this;
    }-*/;

    public native Fx highlight(String color, String attr, String endColor, FxConfig config) /*-{
        var el = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var configJS = config.@com.gwtext.client.core.JsObject::getJsObj()();
        if(attr != null) {
            configJS['attr'] = attr;
        }
        if(endColor != null) {
            configJS['endColor'] = endColor;
        }
        el.highlight(color, configJS);
        return this;
    }-*/;

    public native Fx pause(int seconds) /*-{
        var el = this.@com.gwtext.client.core.JsObject::getJsObj()();
        el.pause(seconds);
        return this;
    }-*/;

    public native Fx puff() /*-{
        var el = this.@com.gwtext.client.core.JsObject::getJsObj()();
        el.puff();
        return this;
    }-*/;

    public native Fx puff(boolean remove, FxConfig config) /*-{
        var el = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var configJS = config.@com.gwtext.client.core.JsObject::getJsObj()();
        configJS['remove'] = remove;
        el.puff(configJS);
        return this;
    }-*/;

    public native Fx scale(int width, int height) /*-{
        var el = this.@com.gwtext.client.core.JsObject::getJsObj()();
        el.scale(width <=0 ? null : width, height <= 0 ? null : height);
        return this;
    }-*/;

    public native Fx scale(int width, int height, FxConfig config) /*-{
        var el = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var configJS = config.@com.gwtext.client.core.JsObject::getJsObj()();
        el.scale(width <=0 ? null : width, height <= 0 ? null : height, configJS);
        return this;
    }-*/;

    public native Fx sequenceFx() /*-{
        var el = this.@com.gwtext.client.core.JsObject::getJsObj()();
        el.sequenceFx();
        return this;
    }-*/;

    public native Fx shift(int x, int y, int width, int height, FxConfig config) /*-{
        var el = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var configJS = config.@com.gwtext.client.core.JsObject::getJsObj()();
        if(x > 0) configJS['x'] = x;
        if(y > 0) configJS['y'] = y;
        if(width > 0) configJS['width'] = width;
        if(height > 0) configJS['height'] = height;
        el.shift(configJS);
        return this;
    }-*/;

    public native Fx slideIn() /*-{
        var el = this.@com.gwtext.client.core.JsObject::getJsObj()();
        el.slideIn();
        return this;
    }-*/;

    public native Fx slideIn(String anchorPosition, FxConfig config) /*-{
        var el = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var configJS = config.@com.gwtext.client.core.JsObject::getJsObj()();
        el.slideIn(anchorPosition, configJS);
        return this;
    }-*/;

    public native Fx slideOut() /*-{
        var el = this.@com.gwtext.client.core.JsObject::getJsObj()();
        el.slideOut();
        return this;
    }-*/;

    public native Fx slideOut(boolean remove, String anchorPosition, FxConfig config) /*-{
        var el = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var configJS = config.@com.gwtext.client.core.JsObject::getJsObj()();
        configJS['remove'] = remove;
        el.slideOut(anchorPosition, configJS);
        return this;
    }-*/;

    public native Fx stopFx() /*-{
        var el = this.@com.gwtext.client.core.JsObject::getJsObj()();
        el.stopFx();
        return this;
    }-*/;

    public native Fx switchOff() /*-{
        var el = this.@com.gwtext.client.core.JsObject::getJsObj()();
        el.switchOff();
        return this;
    }-*/;

    public native Fx switchOff(boolean remove, FxConfig config) /*-{
        var el = this.@com.gwtext.client.core.JsObject::getJsObj()();
        var configJS = config.@com.gwtext.client.core.JsObject::getJsObj()();
        configJS['remove'] = remove;
        el.switchOff(configJS);
        return this;
    }-*/;

    public native Fx syncFx() /*-{
        var el = this.@com.gwtext.client.core.JsObject::getJsObj()();
        el.syncFx();
        return this;
    }-*/;
}

