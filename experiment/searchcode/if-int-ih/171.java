/*Thinlet GUI toolkit - www.thinlet.com
	Copyright (C) 2002 Robert Bajzat (robert.bajzat@thinlet.com)
	
	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License as published by the Free Software Foundation; either
	version 2.1 of the License, or (at your option) any later version.
	
	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	Lesser General Public License for more details.
	
	You should have received a copy of the GNU Lesser General Public
	License along with this library; if not, write to the Free Software
	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package alice.util.thinlet; //java
//midp package thinlet.midp;

import java.applet.*; //java
import java.awt.*; //java
import java.awt.datatransfer.*; //java
import java.awt.image.*; //java
import java.awt.event.*; //java
import java.lang.reflect.*; //java
import java.io.*;
import java.net.*; //java
import java.util.*;
//midp import javax.microedition.lcdui.*;
//midp import javax.microedition.midlet.*;

/**
 *
 */
public class Thinlet extends Container //java
	implements Runnable, Serializable { //java
//midp public class Thinlet extends Canvas implements CommandListener {

	//midp private static final Boolean TRUE = new Boolean(true);
	//midp private static final Boolean FALSE = new Boolean(false);

	private transient Font font;
	private transient Color c_bg;
	private transient Color c_text;
	private transient Color c_textbg;
	private transient Color c_border;
	private transient Color c_disable;
	private transient Color c_hover;
	private transient Color c_press;
	private transient Color c_focus;
	private transient Color c_select;
	private transient Color c_ctrl = null; //java
	//midp private transient Color c_ctrl;
	private transient int block;
	private transient Image hgradient, vgradient; //java
	{
		setFont(new Font("SansSerif", Font.PLAIN, 12)); //java
		//setFont((Font) getToolkit().getDesktopProperty("win.messagebox.font"));
		//midp setFont(Font.getDefaultFont());
		setColors(0xe6e6e6, 0x000000, 0xffffff,
			0x909090, 0xb0b0b0, 0xededed, 0xb9b9b9, 0x89899a, 0xc5c5dd);
	}

	private transient Thread timer;
	private transient long watchdelay;
	private transient long watch;
	private transient String clipboard;

	private Object content = createImpl("desktop");
	private transient Object mouseinside;
	private transient Object insidepart;
	private transient Object mousepressed;
	private transient Object pressedpart;
	private transient int referencex, referencey;
	private transient int mousex, mousey;
	private transient Object focusowner;
	private transient boolean focusinside; //midp { focusinside = true; }
	private transient Object popupowner;
	private transient Object tooltipowner;
	//private transient int pressedkey;

	//java>	
	private static final int DRAG_ENTERED = AWTEvent.RESERVED_ID_MAX + 1;
	private static final int DRAG_EXITED = AWTEvent.RESERVED_ID_MAX + 2;
	
	private static long WHEEL_MASK = 0;
	private static int MOUSE_WHEEL = 0;
	private static Method wheelrotation = null;
	//<java
	private static int evm = 0;
	//java>
	static {
		try {
			WHEEL_MASK = AWTEvent.class.getField("MOUSE_WHEEL_EVENT_MASK").getLong(null);
			MOUSE_WHEEL = MouseEvent.class.getField("MOUSE_WHEEL").getInt(null);
		} catch (Exception exc) { /* not 1.4 */ }
	}
	{
		// disable global focus-manager for this component in 1.4
		if (MOUSE_WHEEL != 0) {
			try {
				getClass().getMethod("setFocusTraversalKeysEnabled", new Class[] { Boolean.TYPE }).
					invoke(this, new Object[] { Boolean.FALSE });
			} catch (Exception exc) { /* never */ }
		}
		// set listeners flags
		enableEvents(AWTEvent.COMPONENT_EVENT_MASK |
			AWTEvent.FOCUS_EVENT_MASK | AWTEvent.KEY_EVENT_MASK |
			AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | WHEEL_MASK);
			// EVM has larger fillRect, fillOval, and drawImage(part), others are correct
			// contributed by Ibsen Ramos-Bonilla
			try {
				if (System.getProperty("java.vendor").indexOf("Insignia") != -1) { evm = -1; }
			} catch (Exception exc) { /* never */ }
	}
	//<java

	/**
	 * Sets the 9 colors used for components, and repaints the whole UI
	 *
	 * @param background the backround of panels (dialogs, desktops),
	 * and disabled controls, not editable texts, lines between list items
	 * (the default value if <i>#e6e6e6</i>)
	 * @param text for text, arrow foreground (<i>black</i> by default)
	 * @param textbackground the background of text components, and lists
	 * (<i>white</i> by default)
	 * @param border for outer in inner borders of enabled components
	 * (<i>#909090</i> by default)
	 * @param disable for text, border, arrow color in disabled components
	 * (<i>#b0b0b0</i> by default)
	 * @param hover indicates that the mouse is inside a button area
	 * (<i>#ededed</i> by default)
	 * @param press for pressed buttons,
	 * gradient image is calculated using the background and this press color
	 * (<i>#b9b9b9</i> by default)
	 * @param focus for text caret and rectagle color marking the focus owner
	 * (<i>#89899a</i> by default)
	 * @param select used as the background of selected text, and list items,
	 * and in slider (<i>#c5c5dd</i> by default)
	 */
	public void setColors(int background, int text, int textbackground,
			int border, int disable, int hover, int press,
			int focus, int select) {
		c_bg = new Color(background); c_text = new Color(text);
		c_textbg = new Color(textbackground); c_border = new Color(border);
		c_disable = new Color(disable); c_hover = new Color(hover);
		c_press = new Color(press); c_focus = new Color(focus);
		c_select = new Color(select);
		//midp c_ctrl = c_hover;
		hgradient = vgradient = null; //java
		repaint();
	}

	/**
	 * Sets the only one font used everywhere, and revalidates the whole UI.
	 * Scrollbar width/height, spinbox, and combobox button width,
	 * and slider size is the same as the font height
	 *
	 * @param font the default font is <i>SansSerif</i>, <i>plain</i>, and <i>12pt</i>
	 */
	public void setFont(Font font) {
		block = getFontMetrics(font).getHeight(); //java
		super.setFont(font); //java
		//midp block = font.getHeight();
		this.font = font;
		hgradient = vgradient = null; //java
		if (content != null) validate(content);
	}
	
	/**
	 *
	 */
	private void doLayout(Object component) {
		String classname = getClass(component);
		if ("combobox" == classname) {
			if (getBoolean(component, "editable", true)) {
				Image icon = getIcon(component, "icon", null);
				layoutField(component, block, false,
					(icon != null) ? icon.getWidth(this) : 0);
			} // set editable -> validate (overwrite textfield repaint)
			else {
				int selected = getInteger(component, "selected", -1);
				if (selected != -1) {
					Object choice = getItem(component, "choice", selected);
					set(component, "text", get(choice, "text"));
					set(component, "icon", get(choice, "icon"));
				}
			}
		}
		else if (("textfield" == classname) || ("passwordfield" == classname)) {
			layoutField(component, 0, ("passwordfield" == classname), 0);
		}
		else if ("textarea" == classname) {
			String text = getString(component, "text", "");
			int start = getInteger(component, "start", 0);
			if (start > text.length()) { setInteger(component, "start", start = text.length(), 0); }
			int end = getInteger(component, "end", 0);
			if (end > text.length()) { setInteger(component, "end", end = text.length(), 0); }
			
			boolean wrap = getBoolean(component, "wrap", false);
			char[] chars = null;
			if (wrap) {
				Rectangle bounds = getRectangle(component, "bounds");
				chars = getChars(component, text, true, bounds.width - 4, bounds.height);
				if (chars == null) { // need scrollbars
					chars = getChars(component, text, true, bounds.width - block - 4, 0);
				}
			}
			else {
				chars = getChars(component, text, false, 0, 0);
			}
			
			FontMetrics fm = getFontMetrics(font); //java
			int width = 0, height = 0;
			int caretx = 0; int carety = 0;
			for (int i = 0, j = 0; j <= chars.length; j++) {
				if ((j == chars.length) || (chars[j] == '\n')) {
					width = Math.max(width, fm.charsWidth(chars, i, j - i));
					if ((end >= i) && (end <= j)) {
						caretx = fm.charsWidth(chars, i, end - i);
						carety = height;
					}
					height += fm.getHeight();
					i = j + 1;
				}
			}
			layoutScrollPane(component, width + 2,
				height - fm.getLeading() + 2, 0, 0);
			scrollToVisible(component, caretx, carety,
				2, fm.getAscent() + fm.getDescent() + 2); //?
		} 
		else if ("tabbedpane" == classname) {
			Rectangle bounds = getRectangle(component, "bounds");
			String placement = getString(component, "placement", "top");
			boolean horizontal = ((placement == "top") || (placement == "bottom"));
			int tabd = 0;
			int tabsize = 0;
			for (Object comp = get(component, "tab");
					comp != null; comp = get(comp, ":next")) {
				Dimension d = getSize(comp, 8, 4, "left");
				setRectangle(comp, "bounds",
					horizontal ? tabd : 0, horizontal ? 0 : tabd, d.width, d.height);
				tabd += horizontal ? d.width : d.height;
				tabsize = Math.max(tabsize, horizontal ? d.height : d.width);
			}
			for (Object comp = get(component, "tab");
					comp != null; comp = get(comp, ":next")) {
				Rectangle r = getRectangle(comp, "bounds");
				if (horizontal) {
					if (placement == "bottom") { r.y = bounds.height - tabsize; }
					r.height = tabsize;
				} else {
					if (placement == "right") { r.x = bounds.width - tabsize; }
					r.width = tabsize;
				}
			}
			int cx = (placement == "left") ? (tabsize + 1) : 2;
			int cy = (placement == "top") ? (tabsize + 1) : 2;
			int cwidth = bounds.width - (horizontal ? 4 : (tabsize + 3));
			int cheight = bounds.height - (horizontal ? (tabsize + 3) : 4);
			for (Object comp = get(component, "component");
					comp != null; comp = get(comp, ":next")) {
				if (!getBoolean(comp, "visible", true)) { continue; }
				setRectangle(comp, "bounds", cx, cy, cwidth, cheight);
				doLayout(comp);
			}
		}
		else if (("panel" == classname) || (classname == "dialog")) {
			int gap = getInteger(component, "gap", 0);
			int[][] grid = getGrid(component, gap);
			if (grid != null) {
				int top = getInteger(component, "top", 0);
				int left = getInteger(component, "left", 0);
				int bottom = getInteger(component, "bottom", 0);
				int right = getInteger(component, "right", 0);
				if (classname == "dialog") {
					int titleheight = getInteger(component, ":titleheight", 0);
					top += 4 + titleheight; left += 4; bottom += 4; right += 4;
				}

				Rectangle bounds = getRectangle(component, "bounds");
				for (int i = 0; i < 2; i++) {
					int d = ((i == 0) ? (bounds.width - left - right) :
						(bounds.height - top - bottom)) -
						getSum(grid[i], 0, grid[i].length, gap, false);
					if (d != 0) {
						int w = getSum(grid[2 + i], 0, grid[2 + i].length, 0, false);
						if (w > 0) {
							for (int j = 0; j < grid[i].length; j++) {
								if (grid[2 + i][j] != 0) {
									grid[i][j] += d * grid[2 + i][j] / w;
								}
							}
						}
					}
				}

				int i = 0;
				for (Object comp = get(component, "component");
						comp != null; comp = get(comp, ":next")) {
					if (!getBoolean(comp, "visible", true)) { continue; }
					int ix = left + getSum(grid[0], 0, grid[4][i], gap, true);
					int iy = top + getSum(grid[1], 0, grid[5][i], gap, true);
					int iwidth = getSum(grid[0], grid[4][i], grid[6][i], gap, false);
					int iheight = getSum(grid[1], grid[5][i], grid[7][i], gap, false);
					String halign = getString(comp, "halign", "fill");
					String valign = getString(comp, "valign", "fill");
					if ((halign != "fill") || (valign != "fill")) {
						Dimension d = getPreferredSize(comp);
						if (halign != "fill") {
							int dw = Math.max(0, iwidth - d.width);
							if (halign == "center") { ix += dw / 2; }
								else if (halign == "right") { ix += dw; }
							iwidth -= dw;
						}
						if (valign != "fill") {
							int dh = Math.max(0, iheight - d.height);
							if (valign == "center") { iy += dh / 2; }
								else if (valign == "bottom") { iy += dh; }
							iheight -= dh;
						}
					}
					setRectangle(comp, "bounds", ix, iy, iwidth, iheight);
					doLayout(comp);
					i++;
				}
			}
		}
		else if ("desktop" == classname) {
			Rectangle bounds = getRectangle(component, "bounds");
			for (Object comp = get(component, "component");
					comp != null; comp = get(comp, ":next")) {
				String iclass = getClass(comp);
				if (iclass == "dialog") {
					Dimension d = getPreferredSize(comp);
					if (get(comp, "bounds") == null)
					setRectangle(comp, "bounds",
						Math.max(0, (bounds.width - d.width) / 2),
						Math.max(0, (bounds.height - d.height) / 2),
						Math.min(d.width, bounds.width), Math.min(d.height, bounds.height));
				} else if ((iclass == ":combolist") || (iclass == ":popup")) {
						//iclass = iclass; //compiler bug
				} else {
					setRectangle(comp, "bounds", 0, 0, bounds.width, bounds.height);
				}
				doLayout(comp);
			}
		}
		else if ("spinbox" == classname) {
			layoutField(component, block, false, 0);
		}
		else if ("splitpane" == classname) {
			Rectangle bounds = getRectangle(component, "bounds");
			boolean horizontal = ("vertical" != get(component, "orientation"));
			int divider = getInteger(component, "divider", -1);
			int maxdiv = (horizontal ? bounds.width : bounds.height) - 5;

			Object comp1 = get(component, "component");
			boolean visible1 = (comp1 != null) && getBoolean(comp1, "visible", true);
			if (divider == -1) {
				int d1 = 0;
				if (visible1) {
					Dimension d = getPreferredSize(comp1);
					d1 = horizontal ? d.width : d.height;
				}
				divider = Math.min(d1, maxdiv);
				setInteger(component, "divider", divider, -1);
			}
			else if (divider > maxdiv) {
				setInteger(component, "divider", divider = maxdiv, -1);
			}

			if (visible1) {
				setRectangle(comp1, "bounds", 0, 0, horizontal ? divider : bounds.width,
					horizontal ? bounds.height : divider);
				doLayout(comp1);
			}
			Object comp2 = (comp1 != null) ? get(comp1, ":next") : null;
			if ((comp2 != null) && getBoolean(comp2, "visible", true)) {
				setRectangle(comp2, "bounds", horizontal ? (divider + 5) : 0,
					horizontal ? 0 : (divider + 5),
					horizontal ? (bounds.width - 5 - divider) : bounds.width,
					horizontal ? bounds.height : (bounds.height - 5 - divider));
				doLayout(comp2);
			}
		} 
		else if (("list" == classname) ||
				("table" == classname) || ("tree" == classname)) {
			int width = 0;
			int columnheight = 0;
			if ("table" == classname) {
				for (Object column = get(component, "column");
						column != null; column = get(column, ":next")) {
					width += getInteger(column, "width", 80);
					Dimension d = getSize(column, 2, 2, "left");
					columnheight = Math.max(columnheight, d.height);
				}
			}
			String itemname = ("list" == classname) ? "item" :
				(("table" == classname) ? "row" : "node");
			int y = 0;
			int level = 0;
			for (Object item = get(component, itemname); item != null;) {
				int x = 0;
				int iwidth = 0; int iheight = 0;
				if ("table" == classname) {
					iwidth = width;
					for (Object cell = get(item, "cell");
							cell != null; cell = get(cell, ":next")) {
						Dimension d = getSize(cell, 2, 3, "left");
						iheight = Math.max(iheight, d.height);
					}
				}
				else {
					if ("tree" == classname) {
						x = (level + 1) * block;
					}
					Dimension d = getSize(item, 2, 3, "left");
					iwidth = d.width; iheight = d.height;
					width = Math.max(width, x + d.width);
				}
				setRectangle(item, "bounds", x, y, iwidth, iheight);
				y += iheight;
				if ("tree" == classname) {
					Object next = get(item, "node");
					if ((next != null) && getBoolean(item, "expanded", true)) {
						level++;
					} else {
						while (((next = get(item, ":next")) == null) && (level > 0)) {
							item = getParent(item);
							level--;
						}
					}
					item = next;
				} else {
					item = get(item, ":next");
				}
			}
			layoutScrollPane(component, width, y - 1, 0, columnheight);
		}
		else if ("menubar" == classname) { 
			Rectangle bounds = getRectangle(component, "bounds");
			int x = 0;
			for (Object menu = get(component, "menu");
					menu != null; menu = get(menu, ":next")) {
				Dimension d = getSize(menu, 8, 4, "left");
				setRectangle(menu, "bounds", x, 0, d.width, bounds.height);
				x += d.width;
			}
		}
		else if ((":combolist" == classname) || (":popup" == classname)) {
			boolean combo = (":combolist" == classname);
			int pw = 0; int ph = 0; int pxy = combo ? 0 : 1;
			Object pr = get(component, combo ? "combobox" : "menu");
			for (Object item = get(pr, combo ? "choice" : "menu");
					item != null; item = get(item, ":next")) {
				String itemclass = combo ? null : getClass(item);
				Dimension d = (itemclass == "separator") ? new Dimension(1, 1) :
					getSize(item, 8 , 4, "left");
				if (itemclass == "checkboxmenuitem") {
					d.width = d.width + block + 3;
					d.height = Math.max(block, d.height);
				}
				else if (itemclass == "menu") {
					d.width += block;
				}
				setRectangle(item, "bounds", pxy, pxy + ph, d.width, d.height);
				pw = Math.max(pw, d.width);
				ph += d.height;
			}
			Rectangle r = getRectangle(component, "bounds");
			r.width = pw + 2; r.height = ph + 2;
			if (combo) { r.width = Math.max(r.width, getRectangle(pr, "bounds").width); }
			if (combo) {
				Rectangle db = getRectangle(content, "bounds");
				if (r.y + ph + 2 > db.height) {
					r.width = pw + 2 + block;
					r.height = db.height - r.y;
				}
				else {
					r.height = Math.min(r.height, db.height - r.y);
				}
				r.width = Math.min(r.width, db.width - r.x);
				layoutScrollPane(component, pw, ph, 0, 0);//~
			}
		}
		//java>
		else if ("bean" == classname) {
				Rectangle r = getRectangle(component, "bounds");
				((Component) get(component, "bean")).setBounds(r);
		}
		//<java
	}

	/**
	 *
	 */
	private char[] getChars(Object component,
			String text, boolean wrap, int width, int height) {
		char[] chars = (char[]) get(component, ":text");
		if ((chars == null) || (chars.length != text.length())) {
			chars = text.toCharArray();
			set(component, ":text", chars);
		}
		else text.getChars(0, chars.length, chars, 0);
		
		if (wrap) {
			FontMetrics fm = getFontMetrics(font); //java
			int lines = (height - 4 + fm.getLeading()) / fm.getHeight();
			boolean prevletter = false; int n = chars.length; int linecount = 0;
			for (int i = 0, j = -1, k = 0; k <= n; k++) { // j is the last space index (before k)
				if (((k == n) || (chars[k] == '\n') || (chars[k] == ' ')) &&
						(j  > i) && (fm.charsWidth(chars, i, k - i) > width)) {
					chars[j] = '\n';
					k--; // draw line to the begin of the current word (+ spaces) if it is out of width
				}
				else if ((k == n) || (chars[k] == '\n')) { // draw line to the text/line end
					j = k; prevletter = false;
				}
				else {
					if ((chars[k] == ' ') && (prevletter || (j > i))) { j = k; } // keep spaces starting the line
					prevletter = (chars[k] != ' ');
					continue;
				}
				linecount++;
				if ((lines != 0) && (linecount == lines)) { return null; }
				i = j + 1;
			}
		}
		return chars;
	}
	
	/**
	 *
	 */
	/*private boolean wrap(char[] chars, int width, int lines) {
		FontMetrics fm = getFontMetrics(font);
		boolean prevletter = false; int n = chars.length; int linecount = 0;
		for (int i = 0, j = -1, k = 0; k <= n; k++) { // j is the last space index (before k)
			if (((k == n) || (chars[k] == '\n') || (chars[k] == ' ')) &&
					(j  > i) && (fm.charsWidth(chars, i, k - i) > width)) {
				chars[j] = '\t';
				k--; // draw line to the begin of the current word (+ spaces) if it is out of width
			}
			else if ((k == n) || (chars[k] == '\n')) { // draw line to the text/line end
				j = k; prevletter = false;
			}
			else {
				if (chars[k] == '\t') { chars[k] = ' '; }
				if ((chars[k] == ' ') && (prevletter || (j > i))) { j = k; } // keep spaces starting the line
				prevletter = (chars[k] != ' ');
				continue;
			}
			linecount++;
			if ((lines != 0) && (linecount == lines)) { return false; }
			i = j + 1;
		}
		return true;
	}*/

	/**
	 *
	 */
	private Object popup(Object component, Object classname) {
		Object popup = null;
		int px = 0; int py = 0;
		if (("menubar" == classname) || (":popup" == classname)) {
			Object popupmenu = get(component, ":popup");
			Object selected = get(component, "selected");
			if (popupmenu != null) {
				if (get(popupmenu, "menu") == selected) { return null; }
				set(popupmenu, "selected", null);
				set(popupmenu, "menu", null);
				removeItemImpl(content, "component", popupmenu);
				repaint(popupmenu);
				set(popupmenu, ":parent", null);
				set(component, ":popup", null);
				if (mouseinside == popupmenu) {
					findComponent(content, mousex, mousey);
					handleMouseEvent(mousex, mousex, 1, false, false, false, //java
						MouseEvent.MOUSE_ENTERED, mouseinside, insidepart); //java
				}
				popup(popupmenu, ":popup");
			}
			if ((selected == null) || (getClass(selected) != "menu")) { return null; }
			popup = createImpl(":popup");
			set(popup, "menu", selected);
			set(component, ":popup", popup);

			Rectangle bounds = getRectangle(selected, "bounds");
			if ("menubar" == classname) {
				px = bounds.x; py = bounds.y + bounds.height - 1;
			} else {
				px = bounds.x + getRectangle(component, "bounds").width - 4;
				py = bounds.y;
			}
		}
		else { //if ("combobox" == classname) {
			popup = createImpl(":combolist");
			set(popup, "combobox", component);
			set(component, ":combolist", popup);

			py = getRectangle(component, "bounds").height + 1;
		}
		if (("menubar" == classname) || ("combobox" == classname)) {
			popupowner = component;
		}
		insertItem(content, "component", popup, 0);
		set(popup, ":parent", content);
		while (component != content) {
			Rectangle r = getRectangle(component, "bounds");
			px += r.x; py += r.y;
			component = getParent(component);
		}
		setRectangle(popup, "bounds", px, py, 0, 0);
		doLayout(popup); repaint(popup);
		return popup;
	}

	/**
	 *
	 */
	private void closeup(Object combobox, Object combolist, Object item) {
		if ((item != null) && getBoolean(item, "enabled", true)) {
			String text = getString(item, "text", "");
			set(combobox, "text", text); // if editable
			setInteger(combobox, "start", text.length(), 0);
			setInteger(combobox, "end", 0, 0);
			set(combobox, "icon", get(item, "icon"));
			validate(combobox);
			setInteger(combobox, "selected", getIndex(combobox, "choice", item), -1);
			invoke(combobox, item, "action");
		}
		set(combolist, "combobox", null);
		set(combobox, ":combolist", null);
		removeItemImpl(content, "component", combolist);
		repaint(combolist);
		set(combolist, ":parent", null);
		popupowner = null;
		if (mouseinside == combolist) {
			findComponent(content, mousex, mousey);
			handleMouseEvent(mousex, mousex, 1, false, false, false, //java
				MouseEvent.MOUSE_ENTERED, mouseinside, insidepart); //java
		}
	}

	/**
	 *
	 */
	private void closeup(Object menubar) {
		set(menubar, "selected", null);
		popup(menubar, "menubar");
		repaint(menubar); // , selected
		popupowner = null;
	}

	/**
	 *
	 */
	private void closeup() {
		if (popupowner != null) {
			String classname = getClass(popupowner);
			if ("menubar" == classname) {
				closeup(popupowner);
			}
			else if ("combobox" == classname) {
				closeup(popupowner, get(popupowner, ":combolist"), null);
			}
		}
	}

	/**
	 *
	 */
	private void showTip() {
		String text = null;
		tooltipowner = null;
		String classname = getClass(mouseinside);
		if ((classname == "tabbedpane") || (classname == "menubar") || (classname == ":popup")) {
			if (insidepart != null) {
				text = getString(insidepart, "tooltip", null);
			}
		}
		else if (classname == ":combolist") {
			if (insidepart instanceof Object[]) {
				text = getString(insidepart, "tooltip", null);
			}
		}
		//list table tree
		if (text == null) { text = getString(mouseinside, "tooltip", null); }
			else { tooltipowner = insidepart; }
		if (text != null) {
			FontMetrics fm = getFontMetrics(font);
			int width = fm.stringWidth(text) + 4;
			int height = fm.getAscent() + fm.getDescent() + 4;
			if (tooltipowner == null) { tooltipowner = mouseinside; }
			Rectangle bounds = getRectangle(content, "bounds");
			int tx = Math.max(0, Math.min(mousex + 10, bounds.width - width));
			int ty = Math.max(0, Math.min(mousey + 10, bounds.height - height));
			setRectangle(tooltipowner, ":tooltipbounds", tx, ty, width, height);
			repaint(tx, ty, width, height);
		}
	}

	/**
	 *
	 */
	private void hideTip() {
		if (tooltipowner != null) {
			Rectangle bounds = getRectangle(tooltipowner, ":tooltipbounds");
			set(tooltipowner, ":tooltipbounds", null);
			tooltipowner = null;
			repaint(bounds.x, bounds.y, bounds.width, bounds.height);
		}
	}

	/**
	 *
	 */
	private void layoutField(Object component, int dw, boolean hidden, int left) {
		int width = getRectangle(component, "bounds").width - left -dw;
		String text = getString(component, "text", "");
		int start = getInteger(component, "start", 0);
		if (start > text.length()) { setInteger(component, "start", start = text.length(), 0); }
		int end = getInteger(component, "end", 0);
		if (end > text.length()) { setInteger(component, "end", end = text.length(), 0); }
		int offset = getInteger(component, "offset", 0);
		int off = offset;
		FontMetrics fm = getFontMetrics(font);
		int caret = hidden ? (fm.charWidth('*') * end) :
			fm.stringWidth(text.substring(0, end)); //java
			//midp font.substringWidth(text, 0, end);
		if (off > caret) {
			off = caret;
		}
		else if (off < caret - width + 4) {
			off = caret - width + 4;
		}
		off = Math.max(0, Math.min(off, (hidden ? (fm.charWidth('*') *
			text.length()) : fm.stringWidth(text)) - width + 4)); 
		if (off != offset) {
			setInteger(component, "offset", off, 0);
		}
	}

	/**
	 *
	 */
	private void layoutScrollPane(Object component,
			int contentwidth, int contentheight, int rowwidth, int columnheight) {
		Rectangle bounds = getRectangle(component, "bounds");
		boolean hneed = false; boolean vneed = false;
		if (contentwidth > bounds.width - rowwidth - 2) {
			hneed = true;
			vneed = (contentheight > bounds.height - columnheight - 2 - block);
		}
		if (vneed || (contentheight > bounds.height - columnheight - 2)) {
			vneed = true;
			hneed = hneed || (contentwidth > bounds.width - rowwidth - 2 - block);
		}
		int viewportwidth = bounds.width - rowwidth - (vneed ? block : 0);
		int viewportheight = bounds.height - columnheight - (hneed ? block : 0);
		setRectangle(component, ":port",
			rowwidth, columnheight, viewportwidth, viewportheight); //?rowwidth

		Rectangle view = getRectangle(component, ":view");
		setRectangle(component, ":view",
			(view != null) ? Math.max(0,
				Math.min(view.x, contentwidth - viewportwidth + 2)) : 0,
			(view != null) ? Math.max(0,
				Math.min(view.y, contentheight - viewportheight + 2)) : 0,
			Math.max(viewportwidth - 2, contentwidth),
			Math.max(viewportheight - 2, contentheight));
	}

	/**
	 *
	 */
	private void scrollToVisible(Object component,
			int x, int y, int width, int height) {
		Rectangle view = getRectangle(component, ":view");
		Rectangle port = getRectangle(component, ":port");
		int vx = Math.max(x + width - port.width + 2, Math.min(view.x, x));
		int vy = Math.max(y + height - port.height + 2, Math.min(view.y, y));
		if ((view.x != vx) || (view.y != vy)) {
			repaint(component); // horizontal | vertical
			view.x = vx; view.y = vy;
		}
	}
	
	/**
	 * Gets the preferred size of the root component
	 *
	 * @return a dimension object indicating the root component's preferred size 
	 */
	public Dimension getPreferredSize() {
		return getPreferredSize(content);
	}

	/**
	 *
	 * @throws java.lang.IllegalArgumentException
	 */
	private Dimension getPreferredSize(Object component) {
		int width = getInteger(component, "width", 0);
		int height = getInteger(component, "height", 0);
		if ((width > 0) && (height > 0)) {
			return new Dimension(width, height);
		}
		String classname = getClass(component);
		//System.out.println("classname: " + classname);
		if ("label" == classname) {
			return getSize(component, 0, 0, "left");
		} 
		if (("button" == classname) || ("togglebutton" == classname)) {
			return getSize(component, 12, 6, "center");
		} 
		if ("checkbox" == classname) {
			Dimension d = getSize(component, 0, 0, "left");
			d.width = d.width + block + 3;
			d.height = Math.max(block, d.height);
			return d;
		}
		if ("combobox" == classname) {
			if (getBoolean(component, "editable", true)) {
				Dimension size = getFieldSize(component);
				Image icon = getIcon(component, "icon", null);
				if (icon != null) {
					size.width += icon.getWidth(this);
					size.height = Math.max(size.height, icon.getHeight(this) + 2);
				}
				size.width += block;
				return size;
			} else {
				int selected = getInteger(component, "selected", -1);
				return getSize((selected != -1) ?
					getItemImpl(component, "choice", selected) :
					get(component, "choice"), 4 + block, 4, "left");
			}
		}
		if (("textfield" == classname) || ("passwordfield" == classname)) {
			return getFieldSize(component);
		}
		if ("textarea" == classname) {
			int columns = getInteger(component, "columns", 0);
			int rows = getInteger(component, "rows", 0); // 'e' -> 'm' ?
			FontMetrics fm = getFontMetrics(font); //java
			return new Dimension(
				((columns > 0) ? (columns * fm.charWidth('e') + 2) : 76) + 2 + block,
				((rows > 0) ? (rows * fm.getHeight() - fm.getLeading() + 2) : 76) + 2 + block);
		}
		if ("tabbedpane" == classname) {
			String placement = getString(component, "placement", "top");
			boolean horizontal = ((placement == "top") || (placement == "bottom"));
			int tabsize = 0;	
			int contentwidth = 0; int contentheight = 0;
			for (Object comp = get(component, "tab");
					comp != null; comp = get(comp, ":next")) {
				Dimension d = getSize(comp, 8, 4, "left");
				tabsize = Math.max(tabsize, horizontal ? d.height : d.width);
			}
			for (Object comp = get(component, "component");
					comp != null; comp = get(comp, ":next")) {
				if (!getBoolean(comp, "visible", true)) { continue; }
				Dimension d = getPreferredSize(comp);
				contentwidth = Math.max(contentwidth, d.width);
				contentheight = Math.max(contentheight, d.height);
			}
			return new Dimension(contentwidth + (horizontal ? 4 : (tabsize + 3)),
				contentheight + (horizontal ? (tabsize + 3) : 4));
		}
		if (("panel" == classname) || (classname == "dialog")) {
			Dimension size = new Dimension(
				getInteger(component, "left", 0) + getInteger(component, "right", 0),
				getInteger(component, "top", 0) + getInteger(component, "bottom", 0));
			if (classname == "dialog") {
				int titleheight = getSize(component, 0, 0, "left").height;
				setInteger(component, ":titleheight", titleheight, 0);
				size.width += 8; size.height += 8 + titleheight;
			}
			int gap = getInteger(component, "gap", 0);
			int[][] grid = getGrid(component, gap);
			if (grid != null) {
				size.width += getSum(grid[0], 0, grid[0].length, gap, false);
				size.height += getSum(grid[1], 0, grid[1].length, gap, false);
			}
			return size;
		}
		else if ("desktop" == classname) {
			Dimension size = new Dimension();
			for (Object comp = get(component, "component");
					comp != null; comp = get(comp, ":next")) {
				String iclass = getClass(comp);
				if ((iclass != "dialog") && (iclass != ":popup") &&
						(iclass != ":combolist")) {
					Dimension d = getPreferredSize(comp);
					size.width = Math.max(d.width, size.width);
					size.height = Math.max(d.height, size.height);
				}
			}
			return size;
		}
		if ("spinbox" == classname) {
			Dimension size = getFieldSize(component);
			size.width += block;
			return size;
		}
		if ("progressbar" == classname) {
			boolean horizontal = ("vertical" != get(component, "orientation"));
			return new Dimension(horizontal ? 76 : 6, horizontal ? 6 : 76);
		}
		if ("slider" == classname) {
			boolean horizontal = ("vertical" != get(component, "orientation"));
			return new Dimension(horizontal ? 76 : 10, horizontal ? 10 : 76);
		}
		if ("splitpane" == classname) {
			boolean horizontal = ("vertical" != get(component, "orientation"));
			Object comp1 = get(component, "component");
			Dimension size = ((comp1 == null) || !getBoolean(comp1, "visible", true)) ?
				new Dimension() : getPreferredSize(comp1);
			Object comp2 = get(comp1, ":next");
			if ((comp2 != null) && getBoolean(comp2, "visible", true)) {
				Dimension d = getPreferredSize(comp2);
				size.width = horizontal ? (size.width + d.width) :
					Math.max(size.width, d.width);
				size.height = horizontal ? Math.max(size.height, d.height) :
					(size.height + d.height);
			}
			if (horizontal) { size.width += 5; } else { size.height += 5; }
			return size;
		}
		if (("list" == classname) ||
				("table" == classname) || ("tree" == classname)) {
			return new Dimension(76 + 2 + block, 76 + 2 + block);
		}
		if ("separator" == classname) {
			return new Dimension(1, 1);
		}
		if ("menubar" == classname) { 
			Dimension size = new Dimension(0, 0);
			for (Object menu = get(component, "menu");
					menu != null; menu = get(menu, ":next")) {
				Dimension d = getSize(menu, 8, 4, "left");
				size.width += d.width;
				size.height = Math.max(size.height, d.height);
			}
			return size;
		}
		//java>
		if ("bean" == classname) {
				return ((Component) get(component, "bean")).getPreferredSize();
		}
		//<java
		throw new IllegalArgumentException((String) classname);
	}

	/**
	 *
	 */
	private int[][] getGrid(Object component, int gap) {
		int count = 0;
		for (Object comp = get(component, "component"); comp != null;
				comp = get(comp, ":next")) {
			if (getBoolean(comp, "visible", true)) { count++; }
		}
		if (count == 0) { return null; }
		int columns = getInteger(component, "columns", 0);
		int icols = (columns != 0) ? columns : count;
		int irows = (columns != 0) ? ((count + columns - 1) / columns) : 1;
		int[][] grid = {
			new int[icols], new int[irows], // columnwidths, rowheights
			new int[icols], new int[irows], // columnweights, rowweights
			new int[count], new int[count], // gridx, gridy
			new int[count], new int[count] }; // gridwidth, gridheight
		int[] columnheight = new int[icols];
		int[][] cache = null; // preferredwidth, height, columnweight, rowweight

		int i = 0; int x = 0; int y = 0;
		int nextsize = 0;
		for (Object comp = get(component, "component");
				comp != null; comp = get(comp, ":next")) {
			if (!getBoolean(comp, "visible", true)) { continue; }
			int colspan = ((columns != 0) && (columns < count)) ?
				Math.min(getInteger(comp, "colspan", 1), columns) : 1;
			int rowspan = (columns != 1) ? getInteger(comp, "rowspan", 1) : 1;
			
			for (int j = 0; j < colspan; j++) {
				if ((columns != 0) && (x + colspan > columns)) {
					x = 0; y++; j = -1;
				}
				else if (columnheight[x + j] > y) {
					x += (j + 1); j = -1;
				}
			}
			if (y + rowspan > grid[1].length) {
				int[] rowheights = new int[y + rowspan];
				System.arraycopy(grid[1], 0, rowheights, 0, grid[1].length);
				grid[1] = rowheights;
				int[] rowweights = new int[y + rowspan];
				System.arraycopy(grid[3], 0, rowweights, 0, grid[3].length);
				grid[3] = rowweights;
			}
			for (int j = 0; j < colspan; j++) {
				columnheight[x + j] = y + rowspan;
			}

			int weightx = getInteger(comp, "weightx", 0);
			int weighty = getInteger(comp, "weighty", 0);
			Dimension d = getPreferredSize(comp);

			if (colspan == 1) {
				grid[0][x] = Math.max(grid[0][x], d.width); // columnwidths
				grid[2][x] = Math.max(grid[2][x], weightx); // columnweights
			}
			else {
				if (cache == null) { cache = new int[4][count]; }
				cache[0][i] = d.width;
				cache[2][i] = weightx;
				if ((nextsize == 0) || (colspan < nextsize)) { nextsize = colspan; }
			}
			if (rowspan == 1) {
				grid[1][y] = Math.max(grid[1][y], d.height); // rowheights 
				grid[3][y] = Math.max(grid[3][y], weighty); // rowweights
			}
			else {
				if (cache == null) { cache = new int[4][count]; }
				cache[1][i] = d.height;
				cache[3][i] = weighty;
				if ((nextsize == 0) || (rowspan < nextsize)) { nextsize = rowspan; }
			}
			grid[4][i] = x; //gridx
			grid[5][i] = y; //gridy
			grid[6][i] = colspan; //gridwidth
			grid[7][i] = rowspan; //gridheight
			
			x += colspan;
			i++;
		}

		while (nextsize != 0) {
			int size = nextsize; nextsize = 0;
			for (int j = 0; j < 2; j++) { // horizontal, vertical
				for (int k = 0; k < count; k++) {
					if (grid[6 + j][k] == size) { // gridwidth, gridheight
						int gridpoint = grid[4 + j][k]; // gridx, gridy

						int weightdiff = cache[2 + j][k];
						for (int m = 0; (weightdiff > 0) && (m < size); m++) {
							weightdiff -= grid[2 + j][gridpoint + m];
						}
						if (weightdiff > 0) {
							int weightsum = cache[2 + j][k] - weightdiff;
							for (int m = 0; (weightsum > 0) && (m < size); m++) {
								int weight = grid[2 + j][gridpoint + m];
								if (weight > 0) {
									int weightinc = weight * weightdiff / weightsum;
									grid[2 + j][gridpoint + m] += weightinc;
									weightdiff -= weightinc;
									weightsum -= weightinc;
								}
							}
							grid[2 + j][gridpoint + size - 1] += weightdiff;
						}

						int sizediff = cache[j][k];
						int weightsum = 0;
						for (int m = 0; (sizediff > 0) && (m < size); m++) {
							sizediff -= grid[j][gridpoint + m];
							weightsum += grid[2 + j][gridpoint + m];
						}
						if (sizediff > 0) {
							for (int m = 0; (weightsum > 0) && (m < size); m++) {
								int weight = grid[2 + j][gridpoint + m];
								if (weight > 0) {
									int sizeinc = weight * sizediff / weightsum;
									grid[j][gridpoint + m] += sizeinc;
									sizediff -= sizeinc;
									weightsum -= weight;
								}
							}
							grid[j][gridpoint + size - 1] += sizediff;
						}
					}
					else if ((grid[6 + j][k] > size) &&
							((nextsize == 0) || (grid[6 + j][k] < nextsize))) {
						nextsize = grid[6 + j][k];
					}
				}
			}
		}
		return grid;
	}

	/**
	 *
	 */
	private int getSum(int[] values,
			int from, int length, int gap, boolean last) {
		if (length <= 0) { return 0; }
		int value = 0;
		for (int i = 0; i < length; i++) {
			value += values[from + i];
		}
		return value + (length - (last ? 0 : 1)) * gap;
	}

	/**
	 *
	 */
	private Dimension getFieldSize(Object component) {
		String text = getString(component, "text", "");
		int columns = getInteger(component, "columns", 0);
		FontMetrics fm = getFontMetrics(font);
		return new Dimension(((columns > 0) ?
			(columns * fm.charWidth('e')) : 76) + 4,
			fm.getAscent() + fm.getDescent() + 4); // fm.stringWidth(text)
	}

	/**
	 *
	 */
	private Dimension getSize(Object component,
			int dx, int dy, String defaultalignment) {
		String text = getString(component, "text", null);
		int tw = 0; int th = 0;
		if (text != null) {
			FontMetrics fm = getFontMetrics(font);
			tw = fm.stringWidth(text);
			th = fm.getAscent() + fm.getDescent();
		}
		Image icon = getIcon(component, "icon", null);
		int iw = 0; int ih = 0;
		if (icon != null) {
			iw = icon.getWidth(this);
			ih = icon.getHeight(this);
		}
		return new Dimension(tw + iw + dx, Math.max(th, ih) + dy);
	}
	//java>

	/**
	 * Invokes the paint method
	 */
	public void update(Graphics g) {
		paint(g);
	}

	/**
	 *
	 */
	/*public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
		if (infoflags == ImageObserver.ALLBITS) {
			validate(content);
		}
		System.out.println("imageUpdate " + x + ", " + y + ", " + width + ", " + height + " " +
			(((infoflags & ABORT) > 0) ? "ABORT " : "") + (((infoflags & ALLBITS) > 0) ? "ALLBITS " : "") +
			(((infoflags & ERROR) > 0) ? "ERROR " : "") + (((infoflags & FRAMEBITS) > 0) ? "FRAMEBITS " : "") +
			(((infoflags & HEIGHT) > 0) ? "HEIGHT " : "") + (((infoflags & PROPERTIES) > 0) ? "PROPERTIES " : "") +
			(((infoflags & SOMEBITS) > 0) ? "SOMEBITS " : "") + (((infoflags & WIDTH) > 0) ? "WIDTH " : ""));
		return super.imageUpdate(img, infoflags, x, y, width, height);
	}*/

	/**
	 * Paints the components inside the graphics clip area
	 */
	public void paint(Graphics g) {
		g.setFont(font);
		if (hgradient == null) {
			int[][] pix = new int[2][block * block];
			int r1 = c_bg.getRed(); int r2 = c_press.getRed();
			int g1 = c_bg.getGreen(); int g2 = c_press.getGreen();
			int b1 = c_bg.getBlue(); int b2 = c_press.getBlue();
			for (int i = 0; i < block; i++) {
				int cr = r1 - (r1 - r2) * i / block;
				int cg = g1 - (g1 - g2) * i / block;
				int cb = b1 - (b1 - b2) * i / block;
				int color = (255 << 24) | (cr << 16) | (cg << 8) | cb;
				for (int j = 0; j < block; j++) {
					pix[0][i * block + j] = color;
					pix[1][j * block + i] = color;
				}
			}
			hgradient = createImage(new MemoryImageSource(block, block, pix[0], 0, block));
			vgradient = createImage(new MemoryImageSource(block, block, pix[1], 0, block));
		}
	
		//((Graphics2D) g).setRenderingHint(
			//RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		//g.setColor(Color.orange);
		//g.fillRect(0, 0, getSize().width, getSize().height);
		//long time = System.currentTimeMillis();
		Rectangle clip = g.getClipBounds();
		///dg.setClip(r.x, r.y, r.width, r.height);
		paint(g, clip.x, clip.y, clip.width, clip.height, content, isEnabled());
		//System.out.println(System.currentTimeMillis() - time);
		///g.setClip(0, 0, getSize().width, getSize().height);
		//g.setColor(Color.red); g.drawRect(clip.x, clip.y, clip.width - 1, clip.height - 1);
	}

	//<java
	/*midp
	protected void paint(Graphics g) {
		paint(g, g.getClipX(), g.getClipY(),
			g.getClipWidth(), g.getClipHeight(), content, true);
	}

	protected void showNotify() {
		setRectangle(content, "bounds", 0, 0, getWidth(), getHeight());
		doLayout(content);
	}
	midp*/

	/**
	 *
	 * @throws java.lang.IllegalArgumentException
	 */
	private void paint(Graphics g,
			int clipx, int clipy, int clipwidth, int clipheight,
			Object component, boolean enabled) {
		if (!getBoolean(component, "visible", true)) { return; }
		Rectangle bounds = getRectangle(component, "bounds");
		if (bounds == null) { return; }
		if (bounds.width < 0) {
			bounds.width = Math.abs(bounds.width);
			doLayout(component);
		}
		if ((clipx + clipwidth < bounds.x) ||
				(clipx > bounds.x + bounds.width) ||
				(clipy + clipheight < bounds.y) ||
				(clipy > bounds.y + bounds.height)) {
			return;
		}
		clipx -= bounds.x; clipy -= bounds.y;
		String classname = getClass(component);
		boolean pressed = (mousepressed == component);
		boolean inside = (mouseinside == component) &&
			((mousepressed == null) || pressed);
		boolean focus = focusinside && (focusowner == component);
		enabled = getBoolean(component, "enabled", true); //enabled &&
		g.translate(bounds.x, bounds.y); 
		//g.setClip(0, 0, bounds.width, bounds.height);

		if ("label" == classname) {
			paintContent(component, g, clipx, clipy, clipwidth, clipheight,
				0, 0, bounds.width, bounds.height,
				enabled ? c_text : c_disable, "left", true);
		}
		else if (("button" == classname) || ("togglebutton" == classname)) {
			boolean toggled = ("togglebutton" == classname) && getBoolean(component, "selected", false);
			paintRect(g, 0, 0, bounds.width, bounds.height,
				enabled ? c_border : c_disable,
				enabled ? ((inside != pressed) ? c_hover : ((pressed || toggled) ? c_press : c_ctrl)) :
					(toggled ? c_press : c_bg), true, true, true, true, true);
			if (focus) {
				g.setColor(c_focus);
				g.drawRect(2, 2, bounds.width - 5, bounds.height - 5);
			}
			paintContent(component, g, clipx, clipy, clipwidth, clipheight,
				6, 3, bounds.width - 12, bounds.height - 6,
				enabled ? c_text : c_disable, "center", true);
		}
		else if ("checkbox" == classname) {
			boolean selected = getBoolean(component, "selected", false);
			String group = getString(component, "group", null);
			Color border = enabled ? c_border : c_disable;
			Color foreground = enabled ? ((inside != pressed) ? c_hover :
				(pressed ? c_press : c_ctrl)) : c_bg;
			int dy = (bounds.height - block + 2) / 2;
			if (group == null) {
				paintRect(g, 1, dy + 1, block - 2, block - 2,
					border, foreground, true, true, true, true, true);
			} else {
				g.setColor((foreground != c_ctrl) ? foreground : c_bg);
				g.fillOval(1, dy + 1, block - 3 + evm, block - 3 + evm); //java
				g.setColor(border);
				g.drawOval(1, dy + 1, block - 3, block - 3); //java
			}
			if (focus) {
				g.setColor(c_focus);
				if (group == null) {
					g.drawRect(3, dy + 3, block - 7, block - 7);
				} else {
					g.drawOval(3, dy + 3, block - 7, block - 7); //java
				}
			}
			if((!selected && inside && pressed) ||
					(selected && (!inside || !pressed))) {
				g.setColor(enabled ? c_text : c_disable);
				if (group == null) {
					g.fillRect(3, dy + block - 9, 2 + evm, 6 + evm);
					g.drawLine(3, dy + block - 4, block - 4, dy + 3);
					g.drawLine(4, dy + block - 4, block - 4, dy + 4);
				} else {
					g.fillOval(5, dy + 5, block - 10 + evm, block - 10 + evm); //java
					g.drawOval(4, dy + 4, block - 9, block - 9); //java
				}
			}
			paintContent(component, g, clipx, clipy, clipwidth, clipheight,
				block + 3, 0, bounds.width - block - 3, bounds.height,
				enabled ? c_text : c_disable, "left", true);
		}
		else if ("combobox" == classname) {
			if (getBoolean(component, "editable", true)) {
				Image icon = getIcon(component, "icon", null);
				int left = (icon != null) ? icon.getWidth(this) : 0;
				paintField(g, clipx, clipy, clipwidth, clipheight, component,
					bounds.width - block, bounds.height,
					inside, pressed, focus, enabled, false, left);
				if (icon != null) {
					g.drawImage(icon, 2, (bounds.height - icon.getHeight(this)) / 2, this); //java
					//midp g.drawImage(icon, 2, bounds.height / 2, Graphics.LEFT | Graphics.VCENTER);
				}
				paintArrow(g, bounds.width - block, 0, block, bounds.height,
					'S', enabled, inside, pressed, "down", true, false, true, true, true);
			} else {
				paintRect(g, 0, 0, bounds.width, bounds.height,
					enabled ? c_border : c_disable,
					enabled ? ((inside != pressed) ? c_hover :
						(pressed ? c_press : c_ctrl)) : c_bg, true, true, true, true, true);
				if (focus) {
					g.setColor(c_focus);
					g.drawRect(2, 2, bounds.width - block - 5, bounds.height - 5);
				}
				paintContent(component, g, clipx, clipy, clipwidth, clipheight,
					2, 2, bounds.width - block - 4, bounds.height - 4,
					enabled ? c_text : c_disable, "left", false);
				g.setColor(enabled ? c_text : c_disable);
				paintArrow(g, bounds.width - block, 0, block, bounds.height, 'S');
			}
		}
		else if (":combolist" == classname) {
			Rectangle view = getRectangle(component, ":view");
			Rectangle viewport = getRectangle(component, ":port");
			g.setColor(c_border);
			g.drawRect(viewport.x, viewport.y, viewport.width - 1, viewport.height - 1);
			if (paintScrollPane(g, clipx, clipy, clipwidth, clipheight,
					bounds, view, viewport, enabled, inside, pressed)) {
				Object selected = get(component, ":inside");
				int ly = clipy - viewport.y - 1;
				int yfrom = view.y + Math.max(0, ly);
				int yto = view.y + Math.min(viewport.height - 2, ly + clipheight);
				for (Object choice = get(get(component, "combobox"), "choice");
						choice != null; choice = get(choice, ":next")) {
					Rectangle r = getRectangle(choice, "bounds");
					if (yto <= r.y) { break; }
					if (yfrom >= r.y + r.height) { continue; }
					boolean armed = (selected == choice);
					paintRect(g, r.x, r.y, bounds.width - 2, r.height, c_border,
						armed ? c_select : c_bg, false, false, false, false, true);
					paintContent(choice, g, clipx, yfrom, clipwidth, yto - yfrom,
						r.x + 4, r.y + 2, bounds.width - 10, r.height - 4,
						getBoolean(choice, "enabled", true) ? c_text : c_disable, "left", false);
				}
				resetScrollPane(g, clipx, clipy, clipwidth, clipheight, view, viewport);
			}
		}
		else if (("textfield" == classname) || ("passwordfield" == classname)) {
			paintField(g, clipx, clipy, clipwidth, clipheight, component,
				bounds.width, bounds.height,
				inside, pressed, focus, enabled, ("passwordfield" == classname), 0);
		}
		else if ("textarea" == classname) {
			Rectangle view = getRectangle(component, ":view");
			Rectangle viewport = getRectangle(component, ":port");
			boolean editable = getBoolean(component, "editable", true);
			paintRect(g, viewport.x, viewport.y, viewport.width, viewport.height,
				enabled ? c_border : c_disable, editable ? c_textbg : c_bg,
				true, true, true, true, true);
			if (paintScrollPane(g, clipx, clipy, clipwidth, clipheight,
					bounds, view, viewport, enabled, inside, pressed)) {
				char[] chars = (char[]) get(component, ":text");
				int start = focus ? getInteger(component, "start", 0) : 0;
				int end = focus ? getInteger(component, "end", 0) : 0;
				int is = Math.min(start, end); int ie = Math.max(start, end);
				FontMetrics fm = g.getFontMetrics(); //java
				int fontascent = fm.getAscent(); int fontheight = fm.getHeight(); //java
				//midp int fontheight = fm.getHeight();
				int ascent = 1;
				int ly = clipy - viewport.y - 1;
				int yfrom = view.y + Math.max(0, ly);
				int yto = view.y + Math.min(viewport.height - 2, ly + clipheight);
				//g.setColor(Color.pink); g.fillRect(0, yfrom - 1, 75, 2); g.fillRect(0, yto - 1, 75, 2);
				
				for (int i = 0, j = 0; j <= chars.length; j++) {
					if ((j == chars.length) || (chars[j] == '\n')) {
						if (yto <= ascent) { break; } // the next lines are bellow paint rectangle
						if (yfrom < ascent + fontheight) { // this line is not above painting area
							if (focus && (is != ie) && (ie >= i) && (is <= j)) {
								int xs = (is < i) ? -1 : ((is > j) ? (view.width - 1) :
									fm.charsWidth(chars, i, is - i));
								int xe = ((j != -1) && (ie > j)) ? (view.width - 1) :
									fm.charsWidth(chars, i, ie - i);
								g.setColor(c_select);
								g.fillRect(1 + xs, ascent, xe - xs + evm, fontheight + evm);
							}
							g.setColor(enabled ? c_text : c_disable);
							g.drawChars(chars, i, j - i, 1, ascent + fontascent); //java
							//midp g.drawChars(chars, i, j - i, 1, ascent, Graphics.LEFT | Graphics.TOP);
							if (focus && (end >= i) && (end <= j)) {
								int caret = fm.charsWidth(chars, i, end - i);
								g.setColor(c_focus);
								g.fillRect(caret, ascent, 1 + evm, fontheight + evm);
							}
						}
						ascent += fontheight;
						i = j + 1;
					}
				}
				resetScrollPane(g, clipx, clipy, clipwidth, clipheight, view, viewport);
			}
		}
		else if ("tabbedpane" == classname) {
			int i = 0; Rectangle last = null;
			int selected = getInteger(component, "selected", 0);
			String placement = getString(component, "placement", "top");
			for (Object comp = get(component, "tab");
					comp != null; comp = get(comp, ":next")) {
				Rectangle r = getRectangle(comp, "bounds");
				boolean hover = !(selected == i) && inside &&
					(mousepressed == null) && (insidepart == comp);
				boolean sel = (selected == i);
				boolean tabenabled = enabled && getBoolean(comp, "enabled", true);
				paintRect(g, r.x, r.y, r.width, r.height,
					enabled ? c_border : c_disable,
					tabenabled ? (sel ? c_bg : (hover ? c_hover : c_ctrl)) : c_ctrl,
					(placement != "bottom") || !sel, (placement != "right") || !sel,
					(placement == "bottom") || ((placement == "top") && !sel),
					(placement == "right") || ((placement == "left") && !sel), true);
				if (focus && sel) {
					g.setColor(c_focus);
					g.drawRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4);
				}
				paintContent(comp, g, clipx, clipy, clipwidth, clipheight,
					r.x + 4, r.y + 2, r.width - 8, r.height - 4,
					tabenabled ? c_text : c_disable, "left", true);
				i++; last = r;
			}
			if (last != null) {
				boolean horizontal = ((placement == "top") || (placement == "bottom"));
				paintRect(g, horizontal ? (last.x + last.width) : last.x,
					horizontal ? last.y : (last.y + last.height),
					horizontal ? (bounds.width - last.x - last.width) : last.width,
					horizontal ? last.height : (bounds.height - last.y - last.height),
					enabled ? c_border : c_disable, c_bg,
					(placement != "top"), (placement != "left"),
					(placement == "top"), (placement == "left"), true);
				paintRect(g, (placement == "left") ? last.width : 0,
					(placement == "top") ? last.height : 0,
					horizontal ? bounds.width : (bounds.width - last.width),
					horizontal ? (bounds.height - last.height) : bounds.height,
					enabled ? c_border : c_disable, c_bg,
					(placement != "top"), (placement != "left"),
					(placement != "bottom"), (placement != "right"), true);					
			}
			Object tabcontent = getItemImpl(component, "component", selected);
			if (tabcontent != null) {
				paint(g, clipx, clipy, clipwidth, clipheight, tabcontent, enabled);
			}
		}
		else if (("panel" == classname) || ("dialog" == classname)) {
			if ("dialog" == classname) {
				int titleheight = getInteger(component, ":titleheight", 0);
				paintRect(g, 0, 0, bounds.width, 3 + titleheight,
					c_border, c_ctrl, true, true, false, true, true);
				paintRect(g, 0, 3 + titleheight, bounds.width, bounds.height - 3 - titleheight,
					c_border, c_press, false, true, true, true, true);
				paintContent(component, g, clipx, clipy, clipwidth, clipheight,
					3, 2, bounds.width - 6, titleheight, c_text, "left", false);
				paintRect(g, 3, 3 + titleheight, bounds.width - 6, bounds.height - 6 - titleheight,
					c_border, c_bg, true, true, true, true, true);
			} else {
				paintRect(g, 0, 0, bounds.width, bounds.height,
					c_border, c_bg, false, false, false, false, true);
				boolean border = getBoolean(component, "border", false);
				if (border) {
					g.setColor(enabled ? c_border : c_disable);
					g.drawRect(0, 0, bounds.width - 1, bounds.height - 1);
				}
			}
			for (Object comp = get(component, "component");
					comp != null; comp = get(comp, ":next")) {
				paint(g, clipx, clipy, clipwidth, clipheight, comp, enabled);
			}
		}
		else if ("desktop" == classname) {
			paintReverse(g, clipx, clipy, clipwidth, clipheight,
				get(component, "component"), enabled);
			//g.setColor(Color.red); if (clip != null) g.drawRect(clipx, clipy, clipwidth, clipheight);
			if ((tooltipowner != null) && (component == content)) {
				Rectangle r = getRectangle(tooltipowner, ":tooltipbounds");
				paintRect(g, r.x, r.y, r.width, r.height,
					c_border, c_bg, true, true, true, true, true);
				String text = getString(tooltipowner, "tooltip", null);
				g.setColor(c_text);
				g.drawString(text, r.x + 2, r.y + g.getFontMetrics().getAscent() + 2); //java
				//midp g.drawString(text, r.x + 2, r.y + (r.height - font.getHeight()) / 2, Graphics.LEFT | Graphics.TOP);
			}			
		}
		else if ("spinbox" == classname) {
			paintField(g, clipx, clipy, clipwidth, clipheight, component,
				bounds.width - block, bounds.height,
				inside, pressed, focus, enabled, false, 0);
			paintArrow(g, bounds.width - block, 0, block, bounds.height / 2,
					'N', enabled, inside, pressed, "up", true, false, false, true, true);
			paintArrow(g, bounds.width - block, bounds.height / 2,
				block, bounds.height - (bounds.height / 2),
				'S', enabled, inside, pressed, "down", true, false, true, true, true);
		}
		else if ("progressbar" == classname) {
			int minimum = getInteger(component, "minimum", 0);
			int maximum = getInteger(component, "maximum", 100);
			int value = getInteger(component, "value", 0);
			boolean horizontal = ("vertical" != get(component, "orientation"));
			int length = (value - minimum) *
				((horizontal ? bounds.width : bounds.height) - 1) / (maximum - minimum);
			paintRect(g, 0, 0, horizontal ? length : bounds.width,
				horizontal ? bounds.height : length, enabled ? c_border : c_disable,
				c_select, true, true, horizontal, !horizontal, true);
			paintRect(g, horizontal ? length : 0, horizontal ? 0 : length,
				horizontal ? (bounds.width - length) : bounds.width	,
				horizontal ? bounds.height : (bounds.height - length),
				enabled ? c_border : c_disable, c_bg, true, true, true, true, true);
		}
		else if ("slider" == classname) {
			int minimum = getInteger(component, "minimum", 0);
			int maximum = getInteger(component, "maximum", 100);
			int value = getInteger(component, "value", 0);
			boolean horizontal = ("vertical" != get(component, "orientation"));
			int length = (value - minimum) *
				((horizontal ? bounds.width : bounds.height) - block) /
				(maximum - minimum);
			paintRect(g, horizontal ? 0 : 3, horizontal ? 3 : 0,
				horizontal ? length : (bounds.width - 6),
				horizontal ? (bounds.height - 6) : length,
				enabled ? c_border : c_disable,
				c_bg, true, true, horizontal, !horizontal, true);
			paintRect(g, horizontal ? length : 0, horizontal ? 0 : length,
				horizontal ? block : bounds.width, horizontal ? bounds.height : block,
				enabled ? c_border : c_disable,
				enabled ? c_ctrl : c_bg, true, true, true, true, true);
			if (focus) {
				g.setColor(c_focus);
				g.drawRect(horizontal ? (length + 2) : 2, horizontal ? 2 : (length + 2),
					(horizontal ? block : bounds.width) - 5,
					(horizontal ? bounds.height : block) - 5);
				//g.drawRect(length + 1, 1, block - 3, bounds.height - 3);
			}
			paintRect(g, horizontal ? (block + length) : 3,
				horizontal ? 3 : (block + length),
				bounds.width - (horizontal ? (block + length) : 6),
				bounds.height - (horizontal ? 6 : (block + length)),
				enabled ? c_border : c_disable,
				c_bg, horizontal, !horizontal, true, true, true);
		}
		else if ("splitpane" == classname) {
			boolean horizontal = ("vertical" != get(component, "orientation"));
			int divider = getInteger(component, "divider", -1);
			paintRect(g, horizontal ? divider : 0, horizontal ? 0 : divider,
				horizontal ? 5 : bounds.width, horizontal ? bounds.height : 5,
				c_border, c_bg, false, false, false, false, true);
			g.setColor(enabled ? (focus ? c_focus : c_border) : c_disable);
			int xy = horizontal ? bounds.height : bounds.width;
			int xy1 = Math.max(0, xy / 2 - 12);
			int xy2 = Math.min(xy / 2 + 12, xy - 1);
			for (int i = divider + 1; i < divider + 4; i += 2) {
				if (horizontal) { g.drawLine(i, xy1, i, xy2); }
					else { g.drawLine(xy1, i, xy2, i); }
			}
			Object comp1 = get(component, "component");
			if (comp1 != null) {
				paint(g, clipx, clipy, clipwidth, clipheight, comp1, enabled);
				Object comp2 = get(comp1, ":next");
				if (comp2 != null) {
					paint(g, clipx, clipy, clipwidth, clipheight, comp2, enabled);
				}
			}
		}
		else if (("list" == classname) ||
				("table" == classname) || ("tree" == classname)) {
			Rectangle view = getRectangle(component, ":view");
			Rectangle viewport = getRectangle(component, ":port");
			int[] columnwidths = null;
			int lx = clipx - viewport.x - 1;
			int xfrom = view.x + Math.max(0, lx);
			int xto = view.x + Math.min(viewport.width - 2, lx + clipwidth);
			if ("table" == classname) {
				columnwidths = new int[getItemCountImpl(component, "column")];
				int i = 0; int x = 0; boolean drawheader = (clipy < viewport.y);
				if (drawheader) { g.setClip(viewport.x, 0, viewport.width, viewport.y); }
				for (Object column = get(component, "column");
						column != null; column = get(column, ":next")) {
					boolean lastcolumn = (i == columnwidths.length - 1);
					int width = getInteger(column, "width", 80);
					if (lastcolumn) { width = Math.max(width, viewport.width - x); }
					columnwidths[i] = width;
					if (drawheader && (xfrom < x + width) && (xto > x)) {
						paintRect(g, x - view.x, 0, width, viewport.y,
							enabled ? c_border : c_disable, enabled ? c_ctrl : c_bg,
							true, true, false, lastcolumn, true);
						paintContent(column, g, clipx, clipy, clipwidth, clipheight,
							x + 2 - view.x, 1, width - 2,
							viewport.y - 2, enabled ? c_text : c_disable, "left", false);
					}
					i++; x += width;
				}
				if (drawheader) { g.setClip(clipx, clipy, clipwidth, clipheight); }
			}
			paintRect(g, viewport.x, viewport.y, viewport.width, viewport.height,
				enabled ? c_border : c_disable, c_textbg, true, true, true, true, true);
			if (paintScrollPane(g, clipx, clipy, clipwidth, clipheight, bounds,
					view, viewport, enabled, inside, pressed)) {
				Object lead = get(component, ":lead");
				int ly = clipy - viewport.y - 1;
				int yfrom = view.y + Math.max(0, ly);
				int yto = view.y + Math.min(viewport.height - 2, ly + clipheight);
				for (Object item = get(component, ("list" == classname) ? "item" :
						(("table" == classname) ? "row" : "node")); item != null;) {
					Rectangle r = getRectangle(item, "bounds");
					if (lead == null) {
						set(component, ":lead", lead = item); // draw first item focused when lead is null
					}
					if (yto <= r.y) { break; } // the clip bounds are above

					Object next = ("tree" == classname) ? get(item, "node") : null;
					boolean expanded = (next != null) &&
							getBoolean(item, "expanded", true);
					if (yfrom < r.y + r.height) { // the clip rectangle is not bellow the current item
						boolean selected = getBoolean(item, "selected", false);
						paintRect(g, 0, r.y, view.width, r.height,
							c_bg, selected ? c_select : c_textbg, false, false, true, false, true);
						if (focus && (lead == item)) {
							g.setColor(c_focus);
							g.drawRect(0, r.y, view.width - 1, r.height - 2);
						}
						if ("table" == classname) {
							int x = 0; int i = 0;
							for (Object cell = get(item, "cell");
									cell != null; cell = get(cell, ":next")) {
								if (xto <= x) { break; }
								int iwidth = (i < columnwidths.length) ? columnwidths[i] : 80;
								if (xfrom < x + iwidth) {
									boolean cellenabled = enabled && getBoolean(cell, "enabled", true);
									paintContent(cell, g, xfrom, yfrom, xto - xfrom, yto - yfrom,
										r.x + x + 1, r.y + 1, iwidth - 2, r.height - 3,
										cellenabled ? c_text : c_disable, "left", false);
								}
								x += iwidth; i++;
							}
						} else {
							boolean itemenabled = enabled && getBoolean(item, "enabled", true);
							paintContent(item, g, xfrom, yfrom, xto - xfrom, yto - yfrom,
								r.x + 1, r.y + 1, view.width - r.x - 2,
								r.height - 3, itemenabled ? c_text : c_disable, "left", false);

							if (next != null) {
								int x = r.x - block / 2;
								int y = r.y + (r.height - 1) / 2;
								//g.drawRect(x - 4, y - 4, 8, 8);
								paintRect(g, x - 4, y - 4, 9, 9, itemenabled ? c_border : c_disable,
									itemenabled ? c_ctrl : c_bg, true, true, true, true, true);
								g.setColor(itemenabled ? c_text : c_disable);
								g.drawLine(x - 2, y, x + 2, y);
								if (!expanded) {
									g.drawLine(x, y - 2, x, y + 2);
								}
							}
						}
					}
					if ("tree" == classname) {
						if ((next == null) || !expanded) {
							while ((item != component) && ((next = get(item, ":next")) == null)) {
								item = getParent(item);
							}
						}
						item = next;
					} else {
						item = get(item, ":next");
					}
				}
				/*if (columnwidths != null) {
					g.setColor(c_bg);
					for (int i = 0, cx = -1; i < columnwidths.length - 1; i++) {
						cx += columnwidths[i];
						g.drawLine(cx, 0, cx, view.height);
					}
				}*/
				resetScrollPane(g, clipx, clipy, clipwidth, clipheight, view, viewport);
			}
		}
		else if ("separator" == classname) {
			g.setColor(enabled ? c_border : c_disable);
			g.fillRect(0, 0, bounds.width + evm, bounds.height + evm);
