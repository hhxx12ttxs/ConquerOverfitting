/*
	Thinlet GUI toolkit - www.thinlet.com
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

//package thinlet; //java
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

	//midp private transient Font font;
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
	private transient Image gradient; //java
	{
		setFont(new Font("SansSerif", Font.PLAIN, 12)); //java
		//midp setFont(Font.getDefaultFont());
		setColors(0xe6e6e6, 0x000000, 0xffffff,
			0x909090, 0xb0b0b0, 0xededed, 0xb9b9b9, 0x89899a, 0xc5c5dd); // f99237 eac16a // e68b2c ffc73c
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
	static {
		try {
			WHEEL_MASK = AWTEvent.class.getField("MOUSE_WHEEL_EVENT_MASK").getLong(null);
			MOUSE_WHEEL = MouseEvent.class.getField("MOUSE_WHEEL").getInt(null);
		} catch (Exception exc) { /* not 1.4 */ }
	}
	{
		if (MOUSE_WHEEL != 0) { // disable global focus-manager for this component in 1.4
			try {
				getClass().getMethod("setFocusTraversalKeysEnabled", new Class[] { Boolean.TYPE }).
					invoke(this, new Object[] { Boolean.FALSE });
			} catch (Exception exc) { /* never */ }
		}
		enableEvents(AWTEvent.COMPONENT_EVENT_MASK |
			AWTEvent.FOCUS_EVENT_MASK | AWTEvent.KEY_EVENT_MASK |
			AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | WHEEL_MASK);
	}
	//<java

	/**
	 *
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
		//java>
		int[] pix = new int[block * block];
		int r1 = c_bg.getRed(); int r2 = c_press.getRed();
		int g1 = c_bg.getGreen(); int g2 = c_press.getGreen();
		int b1 = c_bg.getBlue(); int b2 = c_press.getBlue();
		for (int i = 0; i < block; i++) {
			int r = r1 - (r1 - r2) * i / block;
			int g = g1 - (g1 - g2) * i / block;
			int b = b1 - (b1 - b2) * i / block;
			int color = (255 << 24) | (r << 16) | (g << 8) | b;
			for (int j = 0; j < block; j++) {
				pix[i * block + j] = color;
				//pix[j * block + i] = color;
			}
		}
		gradient = createImage(new MemoryImageSource(block, block, pix, 0, block));
		//<java
	}

	/**
	 *
	 */
	public void setFont(Font font) {
		block = getFontMetrics(font).getHeight(); //java
		super.setFont(font); //java
		//midp block = font.getHeight();
		//midp this.font = font;
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
			int caretx = 0; int carety = 0;
			FontMetrics fm = getFontMetrics(getFont()); //java
			int width = 0, height = 0;
			for (int i = 0, j = 0; j != -1; i = j + 1) {
				j = text.indexOf('\n', i);
				if (i != j) { // && i != text.length()
					String line = (j != -1) ? text.substring(i, j) : text.substring(i); //java
					width = Math.max(width, fm.stringWidth(line)); //java
					//midp width = font.substringWidth(text, i, ((j != -1) ? j : text.length()) - i);
				}
				if ((end >= i) && ((j == -1) || (end <= j))) {
					caretx = fm.stringWidth(text.substring(i, end)); //java
					//midp caretx = font.substringWidth(text, i, end - i);
					carety = height;
				}
				height += fm.getHeight();
			}
			layoutScrollPane(component, width + 2,
				height - fm.getLeading() + 2, 0, 0);
			scrollToVisible(component, caretx, carety,
				2, fm.getAscent() + fm.getDescent() + 2);
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
					int titleheight = getInteger(component, "titleheight", 0);
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
				} else if ((iclass == "combolist") || (iclass == "popupmenu")) {
						iclass = iclass; //compiler bug
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
		else if (("combolist" == classname) || ("popupmenu" == classname)) {
			boolean combo = ("combolist" == classname);
			int pw = 0; int ph = 0; int pxy = combo ? 0 : 1;
			for (Object item = get(get(component, combo ? "combobox" : "menu"),
					combo ? "choice" : "menu"); item != null; item = get(item, ":next")) {
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
	private Object popup(Object component, Object classname) {
		Object popup = null;
		int px = 0; int py = 0;
		if (("menubar" == classname) || ("popupmenu" == classname)) {
			Object popupmenu = get(component, "popupmenu");
			Object selected = get(component, "selected");
			if (popupmenu != null) {
				if (get(popupmenu, "menu") == selected) { return null; }
				set(popupmenu, "selected", null);
				set(popupmenu, "menu", null);
				removeItemImpl(content, "component", popupmenu);
				repaint(popupmenu);
				set(popupmenu, ":parent", null);
				set(component, "popupmenu", null);
				if (mouseinside == popupmenu) {
					findComponent(content, mousex, mousey);
					handleMouseEvent(mousex, mousex, 1, false, false, false, //java
						MouseEvent.MOUSE_ENTERED, mouseinside, insidepart); //java
				}
				popup(popupmenu, "popupmenu");
			}
			if ((selected == null) || (getClass(selected) != "menu")) { return null; }
			popup = createImpl("popupmenu");
			set(popup, "menu", selected);
			set(component, "popupmenu", popup);

			Rectangle bounds = getRectangle(selected, "bounds");
			if ("menubar" == classname) {
				px = bounds.x; py = bounds.y + bounds.height - 1;
			} else {
				px = bounds.x + getRectangle(component, "bounds").width - 4;
				py = bounds.y;
			}
		}
		else { //if ("combobox" == classname) {
			popup = createImpl("combolist");
			set(popup, "combobox", component);
			set(component, "combolist", popup);

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
			invoke(combobox, "action");
		}
		set(combolist, "combobox", null);
		set(combobox, "combolist", null);
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
				closeup(popupowner, get(popupowner, "combolist"), null);
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
		if ((classname == "tabbedpane") || (classname == "menubar") || (classname == "popupmenu")) {
			if (insidepart != null) {
				text = getString(insidepart, "tooltip", null);
			}
		}
		else if (classname == "combolist") {
			if (insidepart instanceof Object[]) {
				text = getString(insidepart, "tooltip", null);
			}
		}
		//list table tree
		if (text == null) { text = getString(mouseinside, "tooltip", null); }
			else { tooltipowner = insidepart; }
		if (text != null) {
			FontMetrics fm = getFontMetrics(getFont());
			int width = fm.stringWidth(text) + 4;
			int height = fm.getAscent() + fm.getDescent() + 4;
			if (tooltipowner == null) { tooltipowner = mouseinside; }
			setRectangle(tooltipowner, "tooltipbounds", mousex + 10, mousey + 10, width, height);
			repaint(mousex + 10, mousey + 10, width, height);
		}
	}

	/**
	 *
	 */
	private void hideTip() {
		if (tooltipowner != null) {
			Rectangle bounds = getRectangle(tooltipowner, "tooltipbounds");
			set(tooltipowner, "tooltipbounds", null);
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
		FontMetrics fm = getFontMetrics(getFont());
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
	 *
	 */
	public Dimension getPreferredSize() {
		return getPreferredSize(content);
	}

	/**
	 *
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
		if ("button" == classname) {
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
			FontMetrics fm = getFontMetrics(getFont()); //java
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
				setInteger(component, "titleheight", titleheight, 0);
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
				if ((iclass != "dialog") && (iclass != "popupmenu") &&
						(iclass != "combolist")) {
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
		FontMetrics fm = getFontMetrics(getFont());
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
			FontMetrics fm = getFontMetrics(getFont());
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
	 *
	 */
	public void update(Graphics g) {
		paint(g);
	}

	/**
	 *~
	 */
	public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
		if (infoflags == ImageObserver.ALLBITS) {
			validate(content);
			return super.imageUpdate(img, infoflags, x, y, width, height);
		}
		return true;
	}

	/**
	 *
	 */
	public void paint(Graphics g) { 
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
		else if ("button" == classname) {
			paintRect(g, 0, 0, bounds.width, bounds.height,
				enabled ? c_border : c_disable,
				enabled ? ((inside != pressed) ? c_hover :
					(pressed ? c_press : c_ctrl)) : c_bg, true, true, true, true);
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
					border, foreground, true, true, true, true);
			} else {
				g.setColor((foreground != c_ctrl) ? foreground : c_bg);
				g.fillOval(1, dy + 1, block - 3, block - 3); //java
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
					g.fillRect(3, dy + block - 9, 2, 6);
					g.drawLine(3, dy + block - 4, block - 4, dy + 3);
					g.drawLine(4, dy + block - 4, block - 4, dy + 4);
				} else {
					g.fillOval(5, dy + 5, block - 10, block - 10); //java
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
					'S', enabled, inside, pressed, "down", true, false, true, true);
			} else {
				paintRect(g, 0, 0, bounds.width, bounds.height,
					enabled ? c_border : c_disable,
					enabled ? ((inside != pressed) ? c_hover :
						(pressed ? c_press : c_ctrl)) : c_bg, true, true, true, true);
				paintContent(component, g, clipx, clipy, clipwidth, clipheight,
					2, 2, bounds.width - block - 4, bounds.height - 4,
					enabled ? c_text : c_disable, "left", false);
				paintArrow(g, bounds.width - block, 0, block, bounds.height, 'S');
				if (focus) {
					g.setColor(c_focus);
					g.drawRect(2, 2, bounds.width - block - 5, bounds.height - 5);
				}
			}
		}
		else if ("combolist" == classname) {
			Rectangle view = getRectangle(component, ":view");
			Rectangle viewport = getRectangle(component, ":port");
			g.setColor(c_border);
			g.drawRect(viewport.x, viewport.y, viewport.width - 1, viewport.height - 1);
			if (paintScrollPane(g, clipx, clipy, clipwidth, clipheight,
					bounds, view, viewport, enabled, inside, pressed)) {
				Object selected = get(component, "inside");
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
						armed ? c_select : c_bg, false, false, false, false);
					paintContent(choice, g, clipx, yfrom, clipwidth, yto - yfrom,
						r.x + 4, r.y + 2, bounds.width - 10, r.height - 4,
						getBoolean(choice, "enabled", true) ? c_text : c_disable, "left", false);
				}
				resetScrollPane(g, clipx, clipy, clipwidth, clipheight, view, viewport);
			}
			//paintRect(g, 0, 0, bounds.width, bounds.height,
			//	secondary1, c_ctrl, true, true, true, true);
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
				true, true, true, true);
			if (paintScrollPane(g, clipx, clipy, clipwidth, clipheight,
					bounds, view, viewport, enabled, inside, pressed)) {
				String text = getString(component, "text", "");
				int start = focus ? getInteger(component, "start", 0) : 0;
				int end = focus ? getInteger(component, "end", 0) : 0;
				int is = Math.min(start, end); int ie = Math.max(start, end);
				boolean wrap = getBoolean(component, "wrap", false);
				FontMetrics fm = g.getFontMetrics(); //java
				int fontascent = fm.getAscent(); int fontheight = fm.getHeight(); //java
				//midp int fontheight = fm.getHeight();
				int ascent = 1;
				int ly = clipy - viewport.y - 1;
				int yfrom = view.y + Math.max(0, ly);
				int yto = view.y + Math.min(viewport.height - 2, ly + clipheight);
				//g.setColor(Color.pink); g.fillRect(0, yfrom - 1, 75, 2); g.fillRect(0, yto - 1, 75, 2);

				boolean prevletter = false; int n = text.length(); char c = 0;
				for (int i = 0, j = -1, k = 0; k <= n; k++) { // j is the last space index (before k)
					if (yto <= ascent) { break; }
					if (wrap) {
						if (((k == n) || ((c = text.charAt(k)) == '\n') || (c == ' ')) &&
								(j  > i) && (fm.stringWidth(text.substring(i, k)) > viewport.width - 4)) {
							k--; // draw line to the begin of the current word (+ spaces) if it is out of width
						}
						else if ((k == n) || (c == '\n')) { // draw line to the text/line end
							j = k; prevletter = false;
						}
						else {
							if ((c == ' ') && (prevletter || (j > i))) { j = k; } // keep spaces starting the line
							prevletter = (c != ' ');
							continue;
						}
					}
					else {
						if ((k == n) || ((c = text.charAt(k)) == '\n')) { j = k; } else { continue; }
					}
					if (yfrom < ascent + fontheight) {
						String line = (j != -1) ? text.substring(i, j) : text.substring(i); //java
						if (focus && (is != ie) && (ie >= i) && ((j == -1) || (is <= j))) {
							int xs = (is < i) ? -1 : (((j != -1) && (is > j)) ? (view.width - 1) :
								fm.stringWidth(text.substring(i, is))); //java
								//midp font.substringWidth(text, i, is - i));
							int xe = ((j != -1) && (ie > j)) ? (view.width - 1) :
								fm.stringWidth(text.substring(i, ie)); //java
								//midp font.substringWidth(text, i, ie - i);
							g.setColor(c_select);
							g.fillRect(1 + xs, ascent, xe - xs, fontheight);
						}
						g.setColor(enabled ? c_text : c_disable);
						g.drawString(line, 1, ascent + fontascent); //java
						//midp g.drawSubstring(text, i, ((j != -1) ? j : text.length()) - i, 1, ascent, Graphics.LEFT | Graphics.TOP);
						if (focus && (end >= i) && ((j == -1) || (end <= j))) {
							int caret = fm.stringWidth(text.substring(i, end)); //java
							//midp int caret = font.substringWidth(text, i, end - i);
							g.setColor(c_focus);
							g.fillRect(caret, ascent, 1, fontheight);
						}
					}
					ascent += fontheight;
					i = j + 1;
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
					(placement == "right") || ((placement == "left") && !sel));
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
					(placement == "top"), (placement == "left"));
				paintRect(g, (placement == "left") ? last.width : 0,
					(placement == "top") ? last.height : 0,
					horizontal ? bounds.width : (bounds.width - last.width),
					horizontal ? (bounds.height - last.height) : bounds.height,
					enabled ? c_border : c_disable, c_bg,
					(placement != "top"), (placement != "left"),
					(placement != "bottom"), (placement != "right"));					
			}
			Object tabcontent = getItemImpl(component, "component", selected);
			if (tabcontent != null) {
				paint(g, clipx, clipy, clipwidth, clipheight, tabcontent, enabled);
			}
		}
		else if (("panel" == classname) || ("dialog" == classname)) {
			if ("dialog" == classname) {
				int titleheight = getInteger(component, "titleheight", 0);
				paintRect(g, 0, 0, bounds.width, 3 + titleheight,
					c_border, c_ctrl, true, true, false, true);
				paintRect(g, 0, 3 + titleheight, bounds.width, bounds.height - 3 - titleheight,
					c_border, c_press, false, true, true, true);
				paintContent(component, g, clipx, clipy, clipwidth, clipheight,
					3, 2, bounds.width - 6, titleheight, c_text, "left", false);
				paintRect(g, 3, 3 + titleheight, bounds.width - 6, bounds.height - 6 - titleheight,
					c_border, c_bg, true, true, true, true);
			} else {
				paintRect(g, 0, 0, bounds.width, bounds.height,
					c_border, c_bg, false, false, false, false);
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
			if (tooltipowner != null) {
				Rectangle r = getRectangle(tooltipowner, "tooltipbounds");
				paintRect(g, r.x, r.y, r.width, r.height,
					c_border, c_bg, true, true, true, true);
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
					'N', enabled, inside, pressed, "up", true, false, false, true);
			paintArrow(g, bounds.width - block, bounds.height / 2,
				block, bounds.height - (bounds.height / 2),
				'S', enabled, inside, pressed, "down", true, false, true, true);
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
				c_select, true, true, horizontal, !horizontal);
			paintRect(g, horizontal ? length : 0, horizontal ? 0 : length,
				horizontal ? (bounds.width - length) : bounds.width	,
				horizontal ? bounds.height : (bounds.height - length),
				enabled ? c_border : c_disable, c_bg, true, true, true, true);
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
				c_bg, true, true, horizontal, !horizontal);
			paintRect(g, horizontal ? length : 0, horizontal ? 0 : length,
				horizontal ? block : bounds.width, horizontal ? bounds.height : block,
				enabled ? c_border : c_disable,
				enabled ? c_ctrl : c_bg, true, true, true, true);
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
				c_bg, horizontal, !horizontal, true, true);
		}
		else if ("splitpane" == classname) {
			boolean horizontal = ("vertical" != get(component, "orientation"));
			int divider = getInteger(component, "divider", -1);
			paintRect(g, horizontal ? divider : 0, horizontal ? 0 : divider,
				horizontal ? 5 : bounds.width, horizontal ? bounds.height : 5,
				c_border, c_bg, false, false, false, false);
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
							true, true, false, lastcolumn);
						paintContent(column, g, clipx, clipy, clipwidth, clipheight,
							x + 2 - view.x, 1, width - 2,
							viewport.y - 2, enabled ? c_text : c_disable, "left", false);
					}
					i++; x += width;
				}
				if (drawheader) { g.setClip(clipx, clipy, clipwidth, clipheight); }
			}
			paintRect(g, viewport.x, viewport.y, viewport.width, viewport.height,
				enabled ? c_border : c_disable, c_textbg, true, true, true, true);
			if (paintScrollPane(g, clipx, clipy, clipwidth, clipheight, bounds,
					view, viewport, enabled, inside, pressed)) {
				Object lead = get(component, "lead");
				int ly = clipy - viewport.y - 1;
				int yfrom = view.y + Math.max(0, ly);
				int yto = view.y + Math.min(viewport.height - 2, ly + clipheight);
				for (Object item = get(component, ("list" == classname) ? "item" :
						(("table" == classname) ? "row" : "node")); item != null;) {
					Rectangle r = getRectangle(item, "bounds");
					if (lead == null) {
						set(component, "lead", lead = item); // draw first item focused when lead is null
					}
					if (yto <= r.y) { break; } // the clip bounds are above

					Object next = ("tree" == classname) ? get(item, "node") : null;
					boolean expanded = (next != null) &&
							getBoolean(item, "expanded", true);
					if (yfrom < r.y + r.height) { // the clip rectangle is not bellow the current item
						boolean selected = getBoolean(item, "selected", false);
						paintRect(g, 0, r.y, view.width, r.height,
							c_bg, selected ? c_select : c_textbg, false, false, true, false);
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
									itemenabled ? c_ctrl : c_bg, true, true, true, true);
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
			g.fillRect(0, 0, bounds.width, bounds.height);
		}
		else if ("menubar" == classname) {
			Object selected = get(component, "selected");
			int lastx = 0;
			for (Object menu = get(component, "menu");
					menu != null; menu = get(menu, ":next")) {
				Rectangle mb = getRectangle(menu, "bounds");
				if (clipx + clipwidth <= mb.x) { break; }
				if (clipx >= mb.x + mb.width) { continue; }
				boolean armed = (selected == menu);
				boolean hoover = (selected == null) && (insidepart == menu);
				paintRect(g, mb.x, 0, mb.width, bounds.height, enabled ? c_border : c_disable,
					enabled ? (armed ? c_select : (hoover ? c_hover : c_ctrl)) : c_bg,
					armed, armed, true, armed);
				paintContent(menu, g, clipx, clipy, clipwidth, clipheight,
					mb.x + 4, 1, mb.width, bounds.height,
					(enabled && getBoolean(menu, "enabled", true)) ? c_text : c_disable,
					"left", true);
				lastx = mb.x + mb.width;
			}
			paintRect(g, lastx, 0, bounds.width - lastx, bounds.height,
				enabled ? c_border : c_disable, enabled ? c_ctrl : c_bg,
				false, false, true, false);
		}
		else if ("popupmenu" == classname) {
			paintRect(g, 0, 0, bounds.width, bounds.height,
				c_border, c_bg, true, true, true, true);
			Object selected = get(component, "selected");
			for (Object menu = get(get(component, "menu"), "menu");
					menu != null; menu = get(menu, ":next")) {
				Rectangle r = getRectangle(menu, "bounds");
				if (clipy + clipheight <= r.y) { break; }
				if (clipy >= r.y + r.height) { continue; }
				String itemclass = getClass(menu);
				if (itemclass == "separator") {
					g.setColor(c_border);
					g.fillRect(r.x, r.y, bounds.width - 2, r.height);
				} else {
					boolean armed = (selected == menu);
					boolean menuenabled = getBoolean(menu, "enabled", true);
					paintRect(g, r.x, r.y, bounds.width - 2, r.height, c_border,
						armed ? c_select : c_bg, false, false, false, false);
					int tx = r.x;
					if (itemclass == "checkboxmenuitem") {
						tx += block + 3;
						boolean checked = getBoolean(menu, "selected", false);
						String group = getString(menu, "group", null);
						g.translate(r.x + 4, r.y + 2);
						g.setColor(menuenabled ? c_border : c_disable);
						if (group == null) {
							g.drawRect(1, 1, block - 3, block - 3);
						} else {
							g.drawOval(1, 1, block - 3, block - 3); //java
						}
						if (checked) {
							g.setColor(menuenabled ? c_text : c_disable);
							if (group == null) {
								g.fillRect(3, block - 9, 2, 6);
								g.drawLine(3, block - 4, block - 4, 3);
								g.drawLine(4, block - 4, block - 4, 4);
							} else {
								g.fillOval(5, 5, block - 10, block - 10); //java
								g.drawOval(4, 4, block - 9, block - 9); //java
							}
						}
						g.translate(-r.x - 4, -r.y - 2);
					}
					paintContent(menu, g, clipx, clipy, clipwidth, clipheight,
						tx + 4, r.y + 2, bounds.width - 10,
						r.height - 4, menuenabled ? c_text : c_disable, "left", true);
					if (itemclass == "menu") {
						paintArrow(g, r.x + bounds.width - block, r.y, block, r.height, 'E');
					}
				}
			}
		}
		//java>
		else if ("bean" == classname) {
				g.clipRect(0, 0, bounds.width, bounds.height);
				((Component) get(component, "bean")).paint(g);
				g.setClip(clipx, clipy, clipwidth, clipheight);
		}
		//<java
		else throw new IllegalArgumentException((String) classname);
		g.translate(-bounds.x, -bounds.y);
		clipx += bounds.x; clipy += bounds.y;
	}

	/**
	 *
	 */
	private void paintReverse(Graphics g,
			int clipx, int clipy, int clipwidth, int clipheight,
			Object component, boolean enabled) {
		if (component != null) {
			Rectangle bounds = getRectangle(component, "bounds");
			if ((clipx < bounds.x) ||
					(clipx + clipwidth > bounds.x + bounds.width) ||
					(clipy < bounds.y) ||
					(clipy + clipheight > bounds.y + bounds.height)) {
				paintReverse(g, clipx, clipy, clipwidth, clipheight,
					get(component, ":next"), enabled);
			}
			paint(g, clipx, clipy, clipwidth, clipheight, component, enabled);
		}
	}

	/**
	 *
	 */
	private void paintField(Graphics g,
			int clipx, int clipy, int clipwidth, int clipheight, Object component,
			int width, int height, boolean inside, boolean pressed,
			boolean focus, boolean enabled, boolean hidden, int left) {
		boolean editable = getBoolean(component, "editable", true);
		paintRect(g, 0, 0, width, height, enabled ? c_border : c_disable,
			editable ? c_textbg : c_bg, true, true, true, true);
		g.clipRect(1 + left, 1, width - left - 2, height - 2);

		String text = getString(component, "text", "");
		int offset = getInteger(component, "offset", 0);
		FontMetrics fm = g.getFontMetrics(); //java

		int caret = 0;
		if (focus) { 
			int start = getInteger(component, "start", 0); 
			int end = getInteger(component, "end", 0);
			caret = hidden ? (fm.charWidth('*') * end) :
				fm.stringWidth(text.substring(0, end)); //java
				//midp font.substringWidth(text, 0, end);
			if (start != end) {
				int is = hidden ? (fm.charWidth('*') * start) :
					fm.stringWidth(text.substring(0, start)); //java
					//midp font.substringWidth(text, 0, start);
				g.setColor(c_select);
				g.fillRect(2 + left - offset + Math.min(is, caret), 1,
					Math.abs(caret - is), height - 2);
			}
		}

		if (focus) {
			g.setColor(c_focus);
			g.fillRect(1 + left - offset + caret, 1, 1, height - 2);
		}

		g.setColor(enabled ? c_text : c_disable);
		int fx = 2 + left - offset;
		int fy = (height + fm.getAscent() - fm.getDescent()) / 2; //java
		//midp int fy = (height - font.getHeight()) / 2;
		if (hidden) {
			int fh = fm.charWidth('*');
			for (int i = text.length(); i > 0; i--) {
				g.drawString("*", fx, fy); //java
				//midp g.drawChar('*', fx, fy, Graphics.LEFT | Graphics.TOP);
				fx += fh;
			}
		} else {
			g.drawString(text, fx, fy); //java
			//midp g.drawString(text, fx, fy, Graphics.LEFT | Graphics.TOP);
		}
		g.setClip(clipx, clipy, clipwidth, clipheight);
	}

	/**
	 *
	 */
	private boolean paintScrollPane(Graphics g,
			int clipx, int clipy, int clipwidth, int clipheight, Rectangle bounds,
			Rectangle view, Rectangle viewport,
			boolean enabled, boolean inside, boolean pressed) {
		if ((viewport.y + viewport.height < bounds.height) &&
				(clipy + clipheight > viewport.y + viewport.height)) { // need horizontal
			int x = viewport.x;
			int y = viewport.y + viewport.height;
			int height = bounds.height - y;
			int button = Math.min(block, viewport.width / 2);
			int track = viewport.width - (2 * button); //max 10
			int knob = Math.min(track, Math.max(track * (viewport.width - 2) / view.width, 6));
			int decrease = view.x * (track - knob) /
				(view.width - viewport.width + 2);
			int increase = track - decrease - knob;
			paintArrow(g, x, y, button, height,
				'W', enabled, inside, pressed, "left", false, true, true, false);
			paintRect(g, x + button, y, decrease, height,
				enabled ? c_border : c_disable, c_bg, false, tru
