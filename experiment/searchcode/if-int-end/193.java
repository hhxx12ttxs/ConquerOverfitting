/*
 * Copyright 2008-2009 Adam Tacy <adam.tacy AT gmail.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.adamtacy.client.ui.effects.examples;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import org.adamtacy.client.ui.effects.Effect;
import org.adamtacy.client.ui.effects.RequiresDimensions;
import org.adamtacy.client.ui.effects.core.ChangeInterface;
import org.adamtacy.client.ui.effects.core.NChangeScalarAction;
import org.adamtacy.client.ui.effects.impl.css.StyleImplementation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;

public class Resize extends Effect implements RequiresDimensions {

	static Vector<String> triggerDOM = new Vector<String>(Arrays.asList("IMG", "TABLE", "DIV"));
	static Vector<String> fontSizes = new Vector<String>(Arrays.asList("xx-small", "x-small", "small", "medium", "large", "x-large", "xx-large"));

	static public void addToTriggerDOM(String newName) {
		triggerDOM.add(newName);
	}

	static public void addToTriggerDOM(Vector<String> newNames) {
		triggerDOM.addAll(newNames);
	}

	static public void setTriggerDOM(Vector<String> names) {
		triggerDOM = names;
	}

	HashMap<Element, Vector<ChangeInterface>> determinedEffects;

	/**
	 * Can only be used on one Element
	 */
	Element effectElement;

	protected Vector<Element> imgElements;

	int endX = 0;

	double endXRatio;

	int endY = 0;

	double endYRatio;

	double fontSizeScalingFactor = 1.2;

	HorizontalAlignmentConstant horizAlign = HasHorizontalAlignment.ALIGN_LEFT;

	int standardFontSize = 14;
	String standardFontUnit = "px";
	int standardIndex = 4;
	int startX = 100;

	double startXRatio;

	int startY = 100;

	double startYRatio;

	VerticalAlignmentConstant vertAlign = HasVerticalAlignment.ALIGN_TOP;

	/**
	 * Initialise effect and set up the basic set of DOM elements that resizing
	 * will be applied to If more elements are needed to be included, use the
	 * addToTriggerDOM(Vector<String> arg) method, or you can replace completely
	 * using the setTriggerDOM(Vector<String> arg) method.
	 */
	public Resize() {
		super();
	}

	public void logTriggers() {
		for (String trig : triggerDOM) GWT.log(trig);
	}

	public Resize(Element el) {
		this();
		addEffectElement(el);
	}

	public Resize(Element el, int start, int end) {
		this(start, end);
		addEffectElement(el);
	}

	public Resize(int start, int end) {
		this();
		startX = start;
		startY = start;
		endX = end;
		endY = end;
	}

	Vector<HandlerRegistration> loadHandlers = new Vector<HandlerRegistration>();
	int numberImages;

	protected void dealWithImages() {
		for (final Element img : this.imgElements) {
			final Image im = new Image(img.getAttribute("src"));
			im.setHeight("5px");
			final Label loading = new Label("Loading");
			loading.setPixelSize(img.getOffsetWidth(), img.getOffsetHeight());
			
			loadHandlers.add(im.addLoadHandler(new LoadHandler() {
				public void onLoad(LoadEvent event) {
					loading.setVisible(false);
					Show show = new Show(img);
					show.play();
					img.getStyle().setDisplay(Display.INLINE);
					RootPanel.get().remove(im);
					int nm = numberImages;
					loadHandlers.get(nm).removeHandler();
					numberImages++;
				}
			}));
			RootPanel.get().add(im, -500, -500);
			com.google.gwt.dom.client.Element e = img.getParentElement();
			e.insertFirst(loading.getElement());
			e.getStyle().setDisplay(Display.INLINE);
			img.getStyle().setDisplay(Display.NONE);
		}
	}

	public void init() {
		super.init();
		if (imgElements!=null)dealWithImages();
		super.init();
	}

	/**
	 * Use to check if components are being parsed correctly. Pass in the
	 * Element and check the log
	 * 
	 * @param currEl
	 */
	protected void testInternalEffectsPerElement(Element currEl) {

		// Recurse over all children
		NodeList<Node> nodes = currEl.getChildNodes();
		if (nodes != null) {
			for (int i = 0; i < nodes.getLength(); i++) {
				Node n = nodes.getItem(i);
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					testInternalEffectsPerElement((Element) nodes.getItem(i));
				}
			}
		}

		// Indicate whether element is handled or not (un-handled elements can be added using the addTrigger methods).
		String name = currEl.getNodeName();
		if (triggerDOM.contains(name)) GWT.log("Using element" + name);
		else GWT.log("NOT Using element" + name);
	}
	

	/**
	 * Creates all the various effects that are required to resize the currEl element
	 * @param currEl
	 */
	protected void createInternalEffectsPerElement(Element currEl) {

		// Recurse over all children that are DOM elements
		NodeList<Node> nodes = currEl.getChildNodes();
		if (nodes != null) {
			for (int i = 0; i < nodes.getLength(); i++) {
				Node n = nodes.getItem(i);
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					createInternalEffectsPerElement((Element) nodes.getItem(i));
				}
			}
		}

		// Find what the Element we are dealing with, i.e. IMG, TABLE, TR.....
		String name = currEl.getNodeName();
		
		// Is it one that we should handle ?
		if (triggerDOM.contains(name)) {
			
			Vector<ChangeInterface> elementEffects = new Vector<ChangeInterface>();

			
			// OK, it is, so let's build a vector of effects needed to resize this element.
			// We start by clearing our temporary vector
			elementEffects.clear();
			
			if (name.equals("IMG")) {
				if (imgElements == null)
					imgElements = new Vector<Element>();
				imgElements.add(currEl);
			}

			int startVal;
			int endVal;

			// Deal with Widths
			startVal = getStartWidth(currEl);
			endVal = getEndWidth(currEl);
			if (startVal != endVal) {
				NChangeScalarAction theWidthChange = new NChangeScalarAction(
						startVal + "px", endVal + "px");
				elementEffects.add(theWidthChange);
				theWidthChange.setUp(currEl, "width", 0);
			}

			// Deal with Heights
			startVal = getStartHeight(currEl);
			endVal = getEndHeight(currEl);
			if (startVal != endVal) {
				NChangeScalarAction theHeightChange = new NChangeScalarAction(
						startVal + "px", endVal + "px");
				elementEffects.add(theHeightChange);
				theHeightChange.setUp(currEl, "height", 0);
			}

			// Deal with Fonts
			String res = StyleImplementation.getComputedStyle(currEl, "fontSize");

			if (res != null) {
				// We have a font to play with
				String unit = StyleImplementation.getUnits(res);
				double value = -1;
				if (unit == null || unit.equals("")) {
					// Assume we're dealing with a named font-size, as there are
					// no units
					// so, find where this name is in our vector of font size
					// names
					int scale = fontSizes.indexOf(res);
					// If we found it, calculate the font size
					if (scale > -1) {
						value = (double) Math.round(Math.pow(
								fontSizeScalingFactor, (scale - standardIndex))
								* standardFontSize);
						unit = standardFontUnit;
					}
				} else {
					// Not a named font-size, so get the value
					value = (new Double(StyleImplementation.getValue(res, unit)))
							.doubleValue();
				}
				if (value > -1) {
					NChangeScalarAction theFontChange = new NChangeScalarAction(
							(int) (value * startXRatio) + unit,
							(int) (value * endXRatio) + unit);
					theFontChange.setUp(currEl, "fontSize", 0);
					elementEffects.add(theFontChange);
				} else {
					// Bail out of working this out since we can't
					GWT.log(
							"Don't know what to do with this font-size definition: "
									+ res, null);
				}
			}

			// Do we need to manage bounding box aspects, e.g. margin etc?  If so, handle them.
			if (managesMargin)
				elementEffects.addAll(getBoxChanges("margin", currEl));
			if (managesBorder)
				elementEffects.addAll(getBoxChanges("border", currEl));
			if (managesPadding)
				elementEffects.addAll(getBoxChanges("padding", currEl));

			// We're done, so copy all the elementEffects we have built into the main list of efects, and recurse back up the tree.
			determinedEffects.put(currEl, elementEffects);
		}
		//elementEffects = null;
	}

	protected Vector<NChangeScalarAction> getBoxChanges(String boxAspect,
			Element currEl) {
		Vector<NChangeScalarAction> temp2 = new Vector<NChangeScalarAction>();
		String end = "";

		if (boxAspect.equals("border"))
			end = "Width";

		int startVal;
		int endVal;

		startVal = getStartBoxHoriz(boxAspect + "Left" + end, currEl);
		endVal = getEndBoxHoriz(boxAspect + "Left" + end, currEl);
		if (startVal != endVal) {
			NChangeScalarAction marginLeft = new NChangeScalarAction(startVal
					+ "px", endVal + "px");
			marginLeft.setUp(currEl, boxAspect + "Left" + end, 0);
			temp2.add(marginLeft);
		}

		startVal = getStartBoxVert(boxAspect + "Bottom" + end, currEl);
		endVal = getEndBoxVert(boxAspect + "Bottom" + end, currEl);
		if (startVal != endVal) {
			NChangeScalarAction marginBottom = new NChangeScalarAction(startVal
					+ "px", endVal + "px");
			marginBottom.setUp(currEl, boxAspect + "Bottom" + end, 0);
			temp2.add(marginBottom);
		}

		startVal = getStartBoxHoriz(boxAspect + "Right" + end, currEl);
		endVal = getEndBoxHoriz(boxAspect + "Right" + end, currEl);
		if (startVal != endVal) {
			NChangeScalarAction marginRight = new NChangeScalarAction(startVal
					+ "px", endVal + "px");
			marginRight.setUp(currEl, boxAspect + "Right" + end, 0);
			temp2.add(marginRight);
		}

		startVal = getStartBoxVert(boxAspect + "Top" + end, currEl);
		endVal = getEndBoxVert(boxAspect + "Top" + end, currEl);
		if (startVal != endVal) {
			NChangeScalarAction marginTop = new NChangeScalarAction(startVal
					+ "px", endVal + "px");
			marginTop.setUp(currEl, boxAspect + "Top" + end, 0);
			temp2.add(marginTop);
		}
		return temp2;
	}

	// Do not manage margin, padding etc by default.
	boolean managesMargin = false;

	public boolean isManagesMargin() {
		return managesMargin;
	}

	public void setManagesMargin(boolean managesMargin) {
		this.managesMargin = managesMargin;
	}

	public boolean isManagesBorder() {
		return managesBorder;
	}

	public void setManagesBorder(boolean managesBorder) {
		this.managesBorder = managesBorder;
	}

	public boolean isManagesPadding() {
		return managesPadding;
	}

	public void setManagesPadding(boolean managesPadding) {
		this.managesPadding = managesPadding;
	}

	boolean managesBorder = false;
	boolean managesPadding = false;

	protected int getBoxSizeVert() {
		return 0;
	}

	protected int getBoxSizeHoriz() {
		return 0;
	}

	protected int getEndHeight(Element theWidget) {
		// int val = (int) ((theWidget.getOffsetHeight() - (2*getBoxSizeVert()))
		// * endYRatio);
		int val = (int) (Double.parseDouble(StyleImplementation
				.getValue(StyleImplementation.getComputedStyle(theWidget,
						"height"))) * endYRatio);
		return val;
	}

	protected int getEndBoxHoriz(String css, Element theWidget) {
		String val = StyleImplementation.getComputedStyle(theWidget, css);
		if (!val.equals("auto") && !val.equals("undefined")) {
			double dVal = Double.parseDouble(StyleImplementation.getValue(val));
			return (int) (dVal * endXRatio);
		} else {
			GWT.log("Unable to calculate style " + css + " for widget", null);
			return -1;
		}
	}

	protected int getStartBoxHoriz(String css, Element theWidget) {
		String val = StyleImplementation.getComputedStyle(theWidget, css);
		if (!val.equals("auto") && !val.equals("undefined")) {
			double dVal = Double.parseDouble(StyleImplementation.getValue(val));
			return (int) (dVal * startXRatio);
		} else {
			GWT.log("Unable to calculate style " + css + " for widget", null);
			return -1;
		}
	}

	protected int getEndBoxVert(String css, Element theWidget) {
		String val = StyleImplementation.getComputedStyle(theWidget, css);
		if (!val.equals("auto") && !val.equals("undefined")) {
			double dVal = Double.parseDouble(StyleImplementation.getValue(val));
			return (int) (dVal * endYRatio);
		} else {
			GWT.log("Unable to calculate style " + css + " for widget", null);
			return -1;
		}
	}

	protected int getStartBoxVert(String css, Element theWidget) {
		String val = StyleImplementation.getComputedStyle(theWidget, css);
		if (!val.equals("auto") && !val.equals("undefined")) {
			double dVal = Double.parseDouble(StyleImplementation.getValue(val));
			return (int) (dVal * startYRatio);
		} else {
			GWT.log("Unable to calculate style " + css + " for widget", null);
			return -1;
		}
	}

	protected int getEndLeft(Element theWidget) {
		if (horizAlign == HasHorizontalAlignment.ALIGN_RIGHT) {
			return (int) (((double) theWidget.getOffsetWidth() / 2 - theWidget
					.getOffsetWidth()
					* endXRatio) / 2);
		} else {
			return (int) ((theWidget.getOffsetWidth() - theWidget
					.getOffsetWidth()
					* endXRatio) / 4);
		}
	}

	protected int getEndTop(Element theWidget) {
		if (vertAlign == HasVerticalAlignment.ALIGN_BOTTOM) {
			return (int) ((double) theWidget.getOffsetHeight() / 2)
					- (int) (theWidget.getOffsetHeight() * endYRatio / 2);
		} else {
			return (int) ((double) theWidget.getOffsetHeight() / 4)
					- (int) (theWidget.getOffsetHeight() * endYRatio / 4);
		}
	}

	protected int getEndWidth(Element theWidget) {
		// int val = (int) ((theWidget.getOffsetWidth() - (2*getBoxSizeHoriz()))
		// * endYRatio);
		int val = (int) (Double.parseDouble(StyleImplementation
				.getValue(StyleImplementation.getComputedStyle(theWidget,
						"width"))) * endXRatio);
		return val;
	}

	protected int getStartHeight(Element theWidget) {
		// int val = (int) ((theWidget.getOffsetHeight() - (2*getBoxSizeVert()))
		// * startYRatio);
		int val = (int) (Double.parseDouble(StyleImplementation
				.getValue(StyleImplementation.getComputedStyle(theWidget,
						"height"))) * startYRatio);
		return val;
	}

	protected int getStartLeft(Element theWidget) {
		if (horizAlign == HasHorizontalAlignment.ALIGN_RIGHT) {
			return (int) ((theWidget.getOffsetWidth() - theWidget
					.getOffsetWidth()
					* startXRatio) / 2);
		} else {
			return (int) ((theWidget.getOffsetWidth() - theWidget
					.getOffsetWidth()
					* startXRatio) / 4);
		}
	}

	protected int getStartTop(Element theWidget) {
		if (vertAlign == HasVerticalAlignment.ALIGN_BOTTOM) {
			return (int) ((double) theWidget.getOffsetHeight() / 2)
					- (int) (theWidget.getOffsetHeight() * startYRatio / 2);
		} else {
			return (int) ((double) theWidget.getOffsetHeight() / 4)
					- (int) (theWidget.getOffsetHeight() * startYRatio / 4);
		}
	}

	protected int getStartWidth(Element theWidget) {
		// int val = (int) ((theWidget.getOffsetWidth() - (2*getBoxSizeHoriz()))
		// * startYRatio);
		int val = (int) (Double.parseDouble(StyleImplementation
				.getValue(StyleImplementation.getComputedStyle(theWidget,
						"width"))) * startXRatio);
		return val;

	}

	@Override
	protected void onUpdate(double progress) {
		super.onUpdate(progress);
		for (Element component : determinedEffects.keySet()) {
			for (ChangeInterface change : determinedEffects.get(component)) 
				change.performStep(component, "", progress);
		}
	}

	/**
	 * Set the ending height for the resize.
	 * 
	 * @param percentage
	 *            Ending height in percentage.
	 */
	public void setEndHeightPercentage(int percentage) {
		assert (percentage >= 0);
		endY = percentage;
	}

	/**
	 * Set Ending percentage for width and height
	 * 
	 * @param percentage
	 */
	public void setEndPercentage(int percentage) {
		setEndHeightPercentage(percentage);
		setEndWidthPercentage(percentage);
	}

	/**
	 * Set the ending width for the resize.
	 * 
	 * @param percentage
	 *            Ending width in percentage.
	 */
	public void setEndWidthPercentage(int percentage) {
		assert (percentage >= 0);
		endX = percentage;
	}

	public void setHorizAlignment(HorizontalAlignmentConstant newHorizAlign) {
		horizAlign = newHorizAlign;
	}

	/**
	 * Set the starting height for the resize.
	 * 
	 * @param percentage
	 *            Starting height in percentage.
	 */
	public void setStartHeightPercentage(int percentage) {
		assert (percentage >= 0);
		startY = percentage;
	}

	/**
	 * Set Starting percentage for width and height;
	 * 
	 * @param percentage
	 */
	public void setStartPercentage(int percentage) {
		setStartHeightPercentage(percentage);
		setStartWidthPercentage(percentage);
	}

	/**
	 * Set the starting width for the resize.
	 * 
	 * @param percentage
	 *            Starting width in percentage.
	 */
	public void setStartWidthPercentage(int percentage) {
		assert (percentage >= 0);
		startX = percentage;
	}

	@Override
	public void setUpEffect() {
		registerEffectElement();
		if (effectElement == null)
			effectElement = (Element) effectElements.get(0);

		startXRatio = (double) startX / 100;
		startYRatio = (double) startY / 100;
		endXRatio = (double) endX / 100;
		endYRatio = (double) endY / 100;

		if (determinedEffects == null)
			determinedEffects = new HashMap<Element, Vector<ChangeInterface>>();
		else
			determinedEffects.clear();

		Vector<ChangeInterface> temp2 = null;

		// Deal with top if not default to align_top
		if (vertAlign != HasVerticalAlignment.ALIGN_TOP) {
			if (temp2 == null)
				temp2 = new Vector<ChangeInterface>();
			NChangeScalarAction theTopChange = new NChangeScalarAction(
					getStartTop((Element) effectElement) + "px",
					getEndTop((Element) effectElement) + "px");
			theTopChange.setUp(effectElement, "top", 0);
			temp2.add(theTopChange);
		}
		if (horizAlign != HasHorizontalAlignment.ALIGN_LEFT) {
			if (temp2 == null)
				temp2 = new Vector<ChangeInterface>();
			NChangeScalarAction theLeftChange = new NChangeScalarAction(
					getStartLeft((Element) effectElement) + "px",
					getEndLeft((Element) effectElement) + "px");
			theLeftChange.setUp(effectElement, "left", 0);
			temp2.add(theLeftChange);
		}
		if (temp2 != null)
			determinedEffects.put((Element) effectElement, temp2);

		createInternalEffectsPerElement((Element) effectElement);

		this.isInitialised = true;
	}

	public void setVertAlignment(VerticalAlignmentConstant newVertAlign) {
		vertAlign = newVertAlign;
	}

	@Override
	public void tearDownEffect() {
		for (Element elem : determinedEffects.keySet()) tearDownEffect(elem);
		determinedEffects = null;
	}
	
	@Override
	public void tearDownEffect(com.google.gwt.dom.client.Element elem) {
		Vector<ChangeInterface> changes = determinedEffects.get(elem);
		for (ChangeInterface change : changes) {
			change.performStep(elem, "", 0);
		}
	}

	public String toString() {
		String toRet = "";
	    for (Element elem : determinedEffects.keySet()){
	        for (ChangeInterface change : determinedEffects.get(elem)){
	        	toRet += change.toString();
	        }
	    }
		return toRet;
	}
}

