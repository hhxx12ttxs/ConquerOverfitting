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

import java.<error descr="Cannot resolve symbol 'applet'">applet</error>.*; //java
import java.awt.*; //java
import java.awt.<error descr="Cannot resolve symbol 'datatransfer'">datatransfer</error>.*; //java
import java.awt.<error descr="Cannot resolve symbol 'image'">image</error>.*; //java
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
	private transient <error descr="Cannot resolve symbol 'Color'">Color</error> c_bg;
	private transient <error descr="Cannot resolve symbol 'Color'">Color</error> c_text;
	private transient <error descr="Cannot resolve symbol 'Color'">Color</error> c_textbg;
	private transient <error descr="Cannot resolve symbol 'Color'">Color</error> c_border;
	private transient <error descr="Cannot resolve symbol 'Color'">Color</error> c_disable;
	private transient <error descr="Cannot resolve symbol 'Color'">Color</error> c_hover;
	private transient <error descr="Cannot resolve symbol 'Color'">Color</error> c_press;
	private transient <error descr="Cannot resolve symbol 'Color'">Color</error> c_focus;
	private transient <error descr="Cannot resolve symbol 'Color'">Color</error> c_select;
	private transient <error descr="Cannot resolve symbol 'Color'">Color</error> c_ctrl = null; //java
	//midp private transient Color c_ctrl;
	private transient int block;
	private transient <error descr="Cannot resolve symbol 'Image'">Image</error> gradient; //java
	{
		setFont(new <error descr="Cannot resolve symbol 'Font'">Font</error>("SansSerif", <error descr="Cannot resolve symbol 'Font'">Font</error>.PLAIN, 12)); //java
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
	private static final int DRAG_ENTERED = <error descr="Cannot resolve symbol 'AWTEvent'">AWTEvent</error>.RESERVED_ID_MAX + 1;
	private static final int DRAG_EXITED = <error descr="Cannot resolve symbol 'AWTEvent'">AWTEvent</error>.RESERVED_ID_MAX + 2;

	private static long WHEEL_MASK = 0;
	private static int MOUSE_WHEEL = 0;
	private static <error descr="Cannot resolve symbol 'Method'">Method</error> wheelrotation = null;
	static {
		try {
			WHEEL_MASK = <error descr="Cannot resolve symbol 'AWTEvent'">AWTEvent</error>.class.getField("MOUSE_WHEEL_EVENT_MASK").getLong(null);
			MOUSE_WHEEL = <error descr="Cannot resolve symbol 'MouseEvent'">MouseEvent</error>.class.getField("MOUSE_WHEEL").getInt(null);
		} catch (Exception exc) { /* not 1.4 */ }
	}
	{
		if (MOUSE_WHEEL != 0) { // disable global focus-manager for this component in 1.4
			try {
				getClass().getMethod("setFocusTraversalKeysEnabled", new Class[] { Boolean.TYPE }).
					<error descr="Cannot resolve method 'invoke(Thinlet, java.lang.Object[])'">invoke</error>(this, new Object[] { Boolean.FALSE });
			} catch (Exception exc) { /* never */ }
		}
		enableEvents(<error descr="Cannot resolve symbol 'AWTEvent'">AWTEvent</error>.COMPONENT_EVENT_MASK |
			<error descr="Cannot resolve symbol 'AWTEvent'">AWTEvent</error>.FOCUS_EVENT_MASK | <error descr="Cannot resolve symbol 'AWTEvent'">AWTEvent</error>.KEY_EVENT_MASK |
			<error descr="Cannot resolve symbol 'AWTEvent'">AWTEvent</error>.MOUSE_EVENT_MASK | <error descr="Cannot resolve symbol 'AWTEvent'">AWTEvent</error>.MOUSE_MOTION_EVENT_MASK | WHEEL_MASK);
	}
	//<java

	/**
	 *
	 */
	public void setColors(int background, int text, int textbackground,
			int border, int disable, int hover, int press,
			int focus, int select) {
		c_bg = new <error descr="Cannot resolve symbol 'Color'">Color</error>(background); c_text = new <error descr="Cannot resolve symbol 'Color'">Color</error>(text);
		c_textbg = new <error descr="Cannot resolve symbol 'Color'">Color</error>(textbackground); c_border = new <error descr="Cannot resolve symbol 'Color'">Color</error>(border);
		c_disable = new <error descr="Cannot resolve symbol 'Color'">Color</error>(disable); c_hover = new <error descr="Cannot resolve symbol 'Color'">Color</error>(hover);
		c_press = new <error descr="Cannot resolve symbol 'Color'">Color</error>(press); c_focus = new <error descr="Cannot resolve symbol 'Color'">Color</error>(focus);
		c_select = new <error descr="Cannot resolve symbol 'Color'">Color</error>(select);
		//midp c_ctrl = c_hover;
		//java>
		int[] pix = new int[block * block];
		int r1 = c_bg.<error descr="Cannot resolve method 'getRed()'">getRed</error>(); int r2 = c_press.<error descr="Cannot resolve method 'getRed()'">getRed</error>();
		int g1 = c_bg.<error descr="Cannot resolve method 'getGreen()'">getGreen</error>(); int g2 = c_press.<error descr="Cannot resolve method 'getGreen()'">getGreen</error>();
		int b1 = c_bg.<error descr="Cannot resolve method 'getBlue()'">getBlue</error>(); int b2 = c_press.<error descr="Cannot resolve method 'getBlue()'">getBlue</error>();
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
		gradient = createImage(new <error descr="Cannot resolve symbol 'MemoryImageSource'">MemoryImageSource</error>(block, block, pix, 0, block));
		//<java
	}

	/**
	 *
	 */
	public void setFont(<error descr="Cannot resolve symbol 'Font'">Font</error> font) {
		block = getFontMetrics(font).<error descr="Cannot resolve method 'getHeight()'">getHeight</error>(); //java
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
				<error descr="Cannot resolve symbol 'Image'">Image</error> icon = getIcon(component, "icon", null);
				layoutField(component, block, false,
					(icon != null) ? icon.<error descr="Cannot resolve method 'getWidth(Thinlet)'">getWidth</error>(this) : 0);
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
			<error descr="Cannot resolve symbol 'FontMetrics'">FontMetrics</error> fm = getFontMetrics(getFont()); //java
			int width = 0, height = 0;
			for (int i = 0, j = 0; j != -1; i = j + 1) {
				j = text.indexOf('\n', i);
				if (i != j) { // && i != text.length()
					String line = (j != -1) ? text.substring(i, j) : text.substring(i); //java
					width = Math.max<error descr="Cannot resolve method 'max(int, ?)'">(width, fm.<error descr="Cannot resolve method 'stringWidth(java.lang.String)'">stringWidth</error>(line))</error>; //java
					//midp width = font.substringWidth(text, i, ((j != -1) ? j : text.length()) - i);
				}
				if ((end >= i) && ((j == -1) || (end <= j))) {
					caretx = fm.<error descr="Cannot resolve method 'stringWidth(java.lang.String)'">stringWidth</error>(text.substring(i, end)); //java
					//midp caretx = font.substringWidth(text, i, end - i);
					carety = height;
				}
				height += fm.<error descr="Cannot resolve method 'getHeight()'">getHeight</error>();
			}
			layoutScrollPane(component, width + 2,
				height - fm.<error descr="Cannot resolve method 'getLeading()'">getLeading</error>() + 2, 0, 0);
			scrollToVisible(component, caretx, carety,
				2, fm.<error descr="Cannot resolve method 'getAscent()'">getAscent</error>() + fm.<error descr="Cannot resolve method 'getDescent()'">getDescent</error>() + 2);
		}
		else if ("tabbedpane" == classname) {
			<error descr="Cannot resolve symbol 'Rectangle'">Rectangle</error> bounds = getRectangle(component, "bounds");
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
				<error descr="Cannot resolve symbol 'Rectangle'">Rectangle</error> r = getRectangle(comp, "bounds");
				if (horizontal) {
					if (placement == "bottom") { r.<error descr="Cannot resolve symbol 'y'">y</error> = bounds.<error descr="Cannot resolve symbol 'height'">height</error> - tabsize; }
					r.<error descr="Cannot resolve symbol 'height'">height</error> = tabsize;
				} else {
					if (placement == "right") { r.<error descr="Cannot resolve symbol 'x'">x</error> = bounds.<error descr="Cannot resolve symbol 'width'">width</error> - tabsize; }
					r.<error descr="Cannot resolve symbol 'width'">width</error> = tabsize;
				}
			}
			int cx = (placement == "left") ? (tabsize + 1) : 2;
			int cy = (placement == "top") ? (tabsize + 1) : 2;
			int cwidth = bounds.<error descr="Cannot resolve symbol 'width'">width</error> - (horizontal ? 4 : (tabsize + 3));
			int cheight = bounds.<error descr="Cannot resolve symbol 'height'">height</error> - (horizontal ? (tabsize + 3) : 4);
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

				<error descr="Cannot resolve symbol 'Rectangle'">Rectangle</error> bounds = getRectangle(component, "bounds");
				for (int i = 0; i < 2; i++) {
					int d = ((i == 0) ? (bounds.<error descr="Cannot resolve symbol 'width'">width</error> - left - right) :
						(bounds.<error descr="Cannot resolve symbol 'height'">height</error> - top - bottom)) -
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
			<error descr="Cannot resolve symbol 'Rectangle'">Rectangle</error> bounds = getRectangle(component, "bounds");
			for (Object comp = get(component, "component");
					comp != null; comp = get(comp, ":next")) {
				String iclass = getClass(comp);
				if (iclass == "dialog") {
					Dimension d = getPreferredSize(comp);
					if (get(comp, "bounds") == null)
					setRectangle(comp, "bounds",
						Math.max<error descr="Cannot resolve method 'max(int, ?)'">(0, (bounds.<error descr="Cannot resolve symbol 'width'">width</error> - d.width) / 2)</error>,
						Math.max<error descr="Cannot resolve method 'max(int, ?)'">(0, (bounds.<error descr="Cannot resolve symbol 'height'">height</error> - d.height) / 2)</error>,
						Math.min<error descr="Cannot resolve method 'min(int, ?)'">(d.width, bounds.<error descr="Cannot resolve symbol 'width'">width</error>)</error>, Math.min<error descr="Cannot resolve method 'min(int, ?)'">(d.height, bounds.<error descr="Cannot resolve symbol 'height'">height</error>)</error>);
				} else if ((iclass == "combolist") || (iclass == "popupmenu")) {
						iclass = iclass; //compiler bug
				} else {
					setRectangle(comp, "bounds", 0, 0, bounds.<error descr="Cannot resolve symbol 'width'">width</error>, bounds.<error descr="Cannot resolve symbol 'height'">height</error>);
				}
				doLayout(comp);
			}
		}
		else if ("spinbox" == classname) {
			layoutField(component, block, false, 0);
		}
		else if ("splitpane" == classname) {
			<error descr="Cannot resolve symbol 'Rectangle'">Rectangle</error> bounds = getRectangle(component, "bounds");
			boolean horizontal = ("vertical" != get(component, "orientation"));
			int divider = getInteger(component, "divider", -1);
			int maxdiv = (horizontal ? bounds.<error descr="Cannot resolve symbol 'width'">width</error> : bounds.<error descr="Cannot resolve symbol 'height'">height</error>) - 5;

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
				setRectangle(comp1, "bounds", 0, 0, horizontal ? divider : bounds.<error descr="Cannot resolve symbol 'width'">width</error>,
					horizontal ? bounds.<error descr="Cannot resolve symbol 'height'">height</error> : divider);
				doLayout(comp1);
			}
			Object comp2 = (comp1 != null) ? get(comp1, ":next") : null;
			if ((comp2 != null) && getBoolean(comp2, "visible", true)) {
				setRectangle(comp2, "bounds", horizontal ? (divider + 5) : 0,
					horizontal ? 0 : (divider + 5),
					horizontal ? (bounds.<error descr="Cannot resolve symbol 'width'">width</error> - 5 - divider) : bounds.<error descr="Cannot resolve symbol 'width'">width</error>,
					horizontal ? bounds.<error descr="Cannot resolve symbol 'height'">height</error> : (bounds.<error descr="Cannot resolve symbol 'height'">height</error> - 5 - divider));
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
			<error descr="Cannot resolve symbol 'Rectangle'">Rectangle</error> bounds = getRectangle(component, "bounds");
			int x = 0;
			for (Object menu = get(component, "menu");
					menu != null; menu = get(menu, ":next")) {
				Dimension d = getSize(menu, 8, 4, "left");
				setRectangle(menu, "bounds", x, 0, d.width, bounds.<error descr="Cannot resolve symbol 'height'">height</error>);
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
			<error descr="Cannot resolve symbol 'Rectangle'">Rectangle</error> r = getRectangle(component, "bounds");
			r.<error descr="Cannot resolve symbol 'width'">width</error> = pw + 2; r.<error descr="Cannot resolve symbol 'height'">height</error> = ph + 2;
			if (combo) {
				<error descr="Cannot resolve symbol 'Rectangle'">Rectangle</error> db = getRectangle(content, "bounds");
				if (r.<error descr="Cannot resolve symbol 'y'">y</error> + ph + 2 > db.<error descr="Cannot resolve symbol 'height'">height</error>) {
					r.<error descr="Cannot resolve symbol 'width'">width</error> = pw + 2 + block;
					r.<error descr="Cannot resolve symbol 'height'">height</error> = db.<error descr="Cannot resolve symbol 'height'">height</error> - r.<error descr="Cannot resolve symbol 'y'">y</error>;
				}
				else {
					r.<error descr="Cannot resolve symbol 'height'">height</error> = Math.min<error descr="Cannot resolve method 'min(?, ?)'">(r.<error descr="Cannot resolve symbol 'height'">height</error>, db.<error descr="Cannot resolve symbol 'height'">height</error> - r.<error descr="Cannot resolve symbol 'y'">y</error>)</error>;
				}
				r.<error descr="Cannot resolve symbol 'width'">width</error> = Math.min<error descr="Cannot resolve method 'min(?, ?)'">(r.<error descr="Cannot resolve symbol 'width'">width</error>, db.<error descr="Cannot resolve symbol 'width'">width</error> - r.<error descr="Cannot resolve symbol 'x'">x</error>)</error>;
				layoutScrollPane(component, pw, ph, 0, 0);//~
			}
		}
		//java>
		else if ("bean" == classname) {
				<error descr="Cannot resolve symbol 'Rectangle'">Rectangle</error> r = getRectangle(component, "bounds");
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
						<error descr="Cannot resolve symbol 'MouseEvent'">MouseEvent</error>.MOUSE_ENTERED, mouseinside, insidepart); //java
				}
				popup(popupmenu, "popupmenu");
			}
			if ((selected == null) || (getClass(selected) != "menu")) { return null; }
			popup = createImpl("popupmenu");
			set(popup, "menu", selected);
			set(component, "popupmenu", popup);

			<error descr="Cannot resolve symbol 'Rectangle'">Rectangle</error> bounds = getRectangle(selected, "bounds");
			if ("menubar" == classname) {
				px = bounds.<error descr="Cannot resolve symbol 'x'">x</error>; py = bounds.<error descr="Cannot resolve symbol 'y'">y</error> + bounds.<error descr="Cannot resolve symbol 'height'">height</error> - 1;
			} else {
				px = bounds.<error descr="Cannot resolve symbol 'x'">x</error> + getRectangle(component, "bounds").<error descr="Cannot resolve symbol 'width'">width</error> - 4;
				py = bounds.<error descr="Cannot resolve symbol 'y'">y</error>;
			}
		}
		else { //if ("combobox" == classname) {
			popup = createImpl("combolist");
			set(popup, "combobox", component);
			set(component, "combolist", popup);

			py = getRectangle(component, "bounds").<error descr="Cannot resolve symbol 'height'">height</error> + 1;
		}
		if (("menubar" == classname) || ("combobox" == classname)) {
			popupowner = component;
		}
		insertItem(content, "component", popup, 0);
		set(popup, ":parent", content);
		while (component != content) {
			<error descr="Cannot resolve symbol 'Rectangle'">Rectangle</error> r = getRectangle(component, "bounds");
			px += r.<error descr="Cannot resolve symbol 'x'">x</error>; py += r.<error descr="Cannot resolve symbol 'y'">y</error>;
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
				<error descr="Cannot resolve symbol 'MouseEvent'">MouseEvent</error>.MOUSE_ENTERED, mouseinside, insidepart); //java
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
			<error descr="Cannot resolve symbol 'FontMetrics'">FontMetrics</error> fm = getFontMetrics(getFont());
			int width = fm.<error descr="Cannot resolve method 'stringWidth(java.lang.String)'">stringWidth</error>(text) + 4;
			int height = fm.<error descr="Cannot resolve method 'getAscent()'">getAscent</error>() + fm.<error descr="Cannot resolve method 'getDescent()'">getDescent</error>() + 4;
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
			<error descr="Cannot resolve symbol 'Rectangle'">Rectangle</error> bounds = getRectangle(tooltipowner, "tooltipbounds");
			set(tooltipowner, "tooltipbounds", null);
			tooltipowner = null;
			repaint(bounds.<error descr="Cannot resolve symbol 'x'">x</error>, bounds.<error descr="Cannot resolve symbol 'y'">y</error>, bounds.<error descr="Cannot resolve symbol 'width'">width</error>, bounds.<error descr="Cannot resolve symbol 'height'">height</error>);
		}
	}

	/**
	 *
	 */
	private void layoutField(Object component, int dw, boolean hidden, int left) {
		int width = getRectangle(component, "bounds").<error descr="Cannot resolve symbol 'width'">width</error> - left -dw;
		String text = getString(component, "text", "");
		int start = getInteger(component, "start", 0);
		if (start > text.length()) { setInteger(component, "start", start = text.length(), 0); }
		int end = getInteger(component, "end", 0);
		if (end > text.length()) { setInteger(component, "end", end = text.length(), 0); }
		int offset = getInteger(component, "offset", 0);
		int off = offset;
		<error descr="Cannot resolve symbol 'FontMetrics'">FontMetrics</error> fm = getFontMetrics(getFont());
		int caret = hidden ? (fm.<error descr="Cannot resolve method 'charWidth(char)'">charWidth</error>('*') * end) :
			fm.<error descr="Cannot resolve method 'stringWidth(java.lang.String)'">stringWidth</error>(text.substring(0, end)); //java
			//midp font.substringWidth(text, 0, end);
		if (off > caret) {
			off = caret;
		}
		else if (off < caret - width + 4) {
			off = caret - width + 4;
		}
		off = Math.max<error descr="Cannot resolve method 'max(int, ?)'">(0, Math.min<error descr="Cannot resolve method 'min(int, ?)'">(off, (hidden ? (fm.<error descr="Cannot resolve method 'charWidth(char)'">charWidth</error>('*') *
			text.length()) : fm.<error descr="Cannot resolve method 'stringWidth(java.lang.String)'">stringWidth</error>(text)) - width + 4)</error>)</error>;
		if (off != offset) {
			setInteger(component, "offset", off, 0);
		}
	}

	/**
	 *
	 */
	private void layoutScrollPane(Object component,
			int contentwidth, int contentheight, int rowwidth, int columnheight) {
		<error descr="Cannot resolve symbol 'Rectangle'">Rectangle</error> bounds = getRectangle(component, "bounds");
		boolean hneed = false; boolean vneed = false;
		if (contentwidth > bounds.<error descr="Cannot resolve symbol 'width'">width</error> - rowwidth - 2) {
			hneed = true;
			vneed = (contentheight > bounds.<error descr="Cannot resolve symbol 'height'">height</error> - columnheight - 2 - block);
		}
		if (vneed || (contentheight > bounds.<error descr="Cannot resolve symbol 'height'">height</error> - columnheight - 2)) {
			vneed = true;
			hneed = hneed || (contentwidth > bounds.<error descr="Cannot resolve symbol 'width'">width</error> - rowwidth - 2 - block);
		}
		int viewportwidth = bounds.<error descr="Cannot resolve symbol 'width'">width</error> - rowwidth - (vneed ? block : 0);
		int viewportheight = bounds.<error descr="Cannot resolve symbol 'height'">height</error> - columnheight - (hneed ? block : 0);
		setRectangle(component, ":port",
			rowwidth, columnheight, viewportwidth, viewportheight); //?rowwidth

		<error descr="Cannot resolve symbol 'Rectangle'">Rectangle</error> view = getRectangle(component, ":view");
		setRectangle(component, ":view",
			(view != null) ? Math.max<error descr="Cannot resolve method 'max(int, ?)'">(0,
				Math.min<error descr="Cannot resolve method 'min(?, int)'">(view.<error descr="Cannot resolve symbol 'x'">x</error>, contentwidth - viewportwidth + 2)</error>)</error> : 0,
			(view != null) ? Math.max<error descr="Cannot resolve method 'max(int, ?)'">(0,
				Math.min<error descr="Cannot resolve method 'min(?, int)'">(view.<error descr="Cannot resolve symbol 'y'">y</error>, contentheight - viewportheight + 2)</error>)</error> : 0,
			Math.max(viewportwidth - 2, contentwidth),
			Math.max(viewportheight - 2, contentheight));
	}

	/**
	 *
	 */
	private void scrollToVisible(Object component,
			int x, int y, int width, int height) {
		<error descr="Cannot resolve symbol 'Rectangle'">Rectangle</error> view = getRectangle(component, ":view");
		<error descr="Cannot resolve symbol 'Rectangle'">Rectangle</error> port = getRectangle(component, ":port");
		int vx = Math.max<error descr="Cannot resolve method 'max(?, ?)'">(x + width - port.<error descr="Cannot resolve symbol 'width'">width</error> + 2, Math.min<error descr="Cannot resolve method 'min(?, int)'">(view.<error descr="Cannot resolve symbol 'x'">x</error>, x)</error>)</error>;
		int vy = Math.max<error descr="Cannot resolve method 'max(?, ?)'">(y + height - port.<error descr="Cannot resolve symbol 'height'">height</error> + 2, Math.min<error descr="Cannot resolve method 'min(?, int)'">(view.<error descr="Cannot resolve symbol 'y'">y</error>, y)</error>)</error>;
		if ((view.<error descr="Cannot resolve symbol 'x'">x</error> != vx) || (view.<error descr="Cannot resolve symbol 'y'">y</error> != vy)) {
			repaint(component); // horizontal | vertical
			view.<error descr="Cannot resolve symbol 'x'">x</error> = vx; view.<error descr="Cannot resolve symbol 'y'">y</error> = vy;
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
				<error descr="Cannot resolve symbol 'Image'">Image</error> icon = getIcon(component, "icon", null);
				if (icon != null) {
					size.width += icon.<error descr="Cannot resolve method 'getWidth(Thinlet)'">getWidth</error>(this);
					size.height = Math.max<error descr="Cannot resolve method 'max(int, ?)'">(size.height, icon.<error descr="Cannot resolve method 'getHeight(Thinlet)'">getHeight</error>(this) + 2)</error>;
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
			<error descr="Cannot resolve symbol 'FontMetrics'">FontMetrics</error> fm = getFontMetrics(getFont()); //java
			return new Dimension(
				((columns > 0) ? (columns * fm.<error descr="Cannot resolve method 'charWidth(char)'">charWidth</error>('e') + 2) : 76) + 2 + block,
				((rows > 0) ? (rows * fm.<error descr="Cannot resolve method 'getHeight()'">getHeight</error>() - fm.<error descr="Cannot resolve method 'getLeading()'">getLeading</error>() + 2) : 76) + 2 + block);
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
		<error descr="Cannot resolve symbol 'FontMetrics'">FontMetrics</error> fm = getFontMetrics(getFont());
		return new Dimension(((columns > 0) ?
			(columns * fm.<error descr="Cannot resolve method 'charWidth(char)'">charWidth</error>('e')) : 76) + 4,
			fm.<error descr="Cannot resolve method 'getAscent()'">getAscent</error>() + fm.<error descr="Cannot resolve method 'getDescent()'">getDescent</error>() + 4); // fm.stringWidth(text)
	}

	/**
	 *
	 */
	private Dimension getSize(Object component,
			int dx, int dy, String defaultalignment) {
		String text = getString(component, "text", null);
		int tw = 0; int th = 0;
		if (text != null) {
			<error descr="Cannot resolve symbol 'FontMetrics'">FontMetrics</error> fm = getFontMetrics(getFont());
			tw = fm.<error descr="Cannot resolve method 'stringWidth(java.lang.String)'">stringWidth</error>(text);
			th = fm.<error descr="Cannot resolve method 'getAscent()'">getAscent</error>() + fm.<error descr="Cannot resolve method 'getDescent()'">getDescent</error>();
		}
		<error descr="Cannot resolve symbol 'Image'">Image</error> icon = getIcon(component, "icon", null);
		int iw = 0; int ih = 0;
		if (icon != null) {
			iw = icon.<error descr="Cannot resolve method 'getWidth(Thinlet)'">getWidth</error>(this);
			ih = icon.<error descr="Cannot resolve method 'getHeight(Thinlet)'">getHeight</error>(this);
		}
		return new Dimension(tw + iw + dx, Math.max(th, ih) + dy);
	}
	//java>

	/**
	 *
	 */
	public void update(<error descr="Cannot resolve symbol 'Graphics'">Graphics</error> g) {
		paint(g);
	}

	/**
	 *~
	 */
	public boolean imageUpdate(<error descr="Cannot resolve symbol 'Image'">Image</error> img, int infoflags, int x, int y, int width, int height) {
		if (infoflags == <error descr="Cannot resolve symbol 'ImageObserver'">ImageObserver</error>.ALLBITS) {
			validate(content);
			return super.imageUpdate(img, infoflags, x, y, width, height);
		}
		return true;
	}

	/**
	 *
	 */
	public void paint(<error descr="Cannot resolve symbol 'Graphics'">Graphics</error> g) {
		//g.setColor(Color.orange);
		//g.fillRect(0, 0, getSize().width, getSize().height);
		//long time = System.currentTimeMillis();
		<error descr="Cannot resolve symbol 'Rectangle'">Rectangle</error> clip = g.<error descr="Cannot resolve method 'getClipBounds()'">getClipBounds</error>();
		///dg.setClip(r.x, r.y, r.width, r.height);
		paint(g, clip.<error descr="Cannot resolve symbol 'x'">x</error>, clip.<error descr="Cannot resolve symbol 'y'">y</error>, clip.<error descr="Cannot resolve symbol 'width'">width</error>, clip.<error descr="Cannot resolve symbol 'height'">height</error>, content, isEnabled());
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
	private void paint(<error descr="Cannot resolve symbol 'Graphics'">Graphics</error> g,
			int clipx, int clipy, int clipwidth, int clipheight,
			Object component, boolean enabled) {
		if (!getBoolean(component, "visible", true)) { return; }
		<error descr="Cannot resolve symbol 'Rectangle'">Rectangle</error> bounds = getRectangle(component, "bounds");
		if (bounds == null) { return; }
		if (bounds.<error descr="Cannot resolve symbol 'width'">width</error> < 0) {
			bounds.<error descr="Cannot resolve symbol 'width'">width</error> = Math.abs<error descr="Cannot resolve method 'abs(?)'">(bounds.<error descr="Cannot resolve symbol 'width'">width</error>)</error>;
			doLayout(component);
		}
		if ((clipx + clipwidth < bounds.<error descr="Cannot resolve symbol 'x'">x</error>) ||
				(clipx > bounds.<error descr="Cannot resolve symbol 'x'">x</error> + bounds.<error descr="Cannot resolve symbol 'width'">width</error>) ||
				(clipy + clipheight < bounds.<error descr="Cannot resolve symbol 'y'">y</error>) ||
				(clipy > bounds.<error descr="Cannot resolve symbol 'y'">y</error> + bounds.<error descr="Cannot resolve symbol 'height'">height</error>)) {
			return;
		}
		clipx -= bounds.<error descr="Cannot resolve symbol 'x'">x</error>; clipy -= bounds.<error descr="Cannot resolve symbol 'y'">y</error>;
		String classname = getClass(component);
		boolean pressed = (mousepressed == component);
		boolean inside = (mouseinside == component) &&
			((mousepressed == null) || pressed);
		boolean focus = focusinside && (focusowner == component);
		enabled = getBoolean(component, "enabled", true); //enabled &&
		g.<error descr="Cannot resolve method 'translate(?, ?)'">translate</error>(bounds.<error descr="Cannot resolve symbol 'x'">x</error>, bounds.<error descr="Cannot resolve symbol 'y'">y</error>);
		//g.setClip(0, 0, bounds.width, bounds.height);

		if ("label" == classname) {
			paintContent(component, g, clipx, clipy, clipwidth, clipheight,
				0, 0, bounds.<error descr="Cannot resolve symbol 'width'">width</error>, bounds.<error descr="Cannot resolve symbol 'height'">height</error>,
				enabled ? c_text : c_disable, "left", true);
		}
		else if ("button" == classname) {
			paintRect(g, 0, 0, bounds.<error descr="Cannot resolve symbol 'width'">width</error>, bounds.<error descr="Cannot resolve symbol 'height'">height</error>,
				enabled ? c_border : c_disable,
				enabled ? ((inside != pressed) ? c_hover :
					(pressed ? c_press : c_ctrl)) : c_bg, true, true, true, true);
			if (focus) {
				g.<error descr="Cannot resolve method 'setColor(Color)'">setColor</error>(c_focus);
				g.<error descr="Cannot resolve method 'drawRect(int, int, ?, ?)'">drawRect</error>(2, 2, bounds.<error descr="Cannot resolve symbol 'width'">width</error> - 5, bounds.<error descr="Cannot resolve symbol 'height'">height</error> - 5);
			}
			paintContent(component, g, clipx, clipy, clipwidth, clipheight,
				6, 3, bounds.<error descr="Cannot resolve symbol 'width'">width</error> - 12, bounds.<error descr="Cannot resolve symbol 'height'">height</error> - 6,
				enabled ? c_text : c_disable, "center", true);
		}
		else if ("checkbox" == classname) {
			boolean selected = getBoolean(component, "selected", false);
			String group = getString(component, "group", null);
			<error descr="Cannot resolve symbol 'Color'">Color</error> border = enabled ? c_border : c_disable;
			<error descr="Cannot resolve symbol 'Color'">Color</error> foreground = enabled ? ((inside != pressed) ? c_hover :
				(pressed ? c_press : c_ctrl)) : c_bg;
			int dy = (bounds.<error descr="Cannot resolve symbol 'height'">height</error> - block + 2) / 2;
			if (group == null) {
				paintRect(g, 1, dy + 1, block - 2, block - 2,
					border, foreground, true, true, true, true);
			} else {
				g.<error descr="Cannot resolve method 'setColor(Color)'">setColor</error>((foreground != c_ctrl) ? foreground : c_bg);
				g.<error descr="Cannot resolve method 'fillOval(int, int, int, int)'">fillOval</error>(1, dy + 1, block - 3, block - 3); //java
				g.<error descr="Cannot resolve method 'setColor(Color)'">setColor</error>(border);
				g.<error descr="Cannot resolve method 'drawOval(int, int, int, int)'">drawOval</error>(1, dy + 1, block - 3, block - 3); //java
			}
			if (focus) {
				g.<error descr="Cannot resolve method 'setColor(Color)'">setColor</error>(c_focus);
				if (group == null) {
					g.<error descr="Cannot resolve method 'drawRect(int, int, int, int)'">drawRect</error>(3, dy + 3, block - 7, block - 7);
				} else {
					g.<error descr="Cannot resolve method 'drawOval(int, int, int, int)'">drawOval</error>(3, dy + 3, block - 7, block - 7); //java
				}
			}
			if((!selected && inside && pressed) ||
					(selected && (!inside || !pressed))) {
				g.<error descr="Cannot resolve method 'setColor(Color)'">setColor</error>(enabled ? c_text : c_disable);
				if (group == null) {
					g.<error descr="Cannot resolve method 'fillRect(int, int, int, int)'">fillRect</error>(3, dy + block - 9, 2, 6);
					g.<error descr="Cannot resolve method 'drawLine(int, int, int, int)'">drawLine</error>(3, dy + block - 4, block - 4, dy + 3);
					g.<error descr="Cannot resolve method 'drawLine(int, int, int, int)'">drawLine</error>(4, dy + block - 4, block - 4, dy + 4);
				} else {
					g.<error descr="Cannot resolve method 'fillOval(int, int, int, int)'">fillOval</error>(5, dy + 5, block - 10, block - 10); //java
					g.<error descr="Cannot resolve method 'drawOval(int, int, int, int)'">drawOval</error>(4, dy + 4, block - 9, block - 9); //java
				}
			}
			paintContent(component, g, clipx, clipy, clipwidth, clipheight,
				block + 3, 0, bounds.<error descr="Cannot resolve symbol 'width'">width</error> - block - 3, bounds.<error descr="Cannot resolve symbol 'height'">height</error>,
				enabled ? c_text : c_disable, "left", true);
		}
		else if ("combobox" == classname) {
			if (getBoolean(component, "editable", true)) {
				<error descr="Cannot resolve symbol 'Image'">Image</error> icon = getIcon(component, "icon", null);
				int left = (icon != null) ? icon.<error descr="Cannot resolve method 'getWidth(Thinlet)'">getWidth</error>(this) : 0;
				paintField(g, clipx, clipy, clipwidth, clipheight, component,
					bounds.<error descr="Cannot resolve symbol 'width'">width</error> - block, bounds.<error descr="Cannot resolve symbol 'height'">height</error>,
					inside, pressed, focus, enabled, false, left);
				if (icon != null) {
					g.<error descr="Cannot resolve method 'drawImage(Image, int, ?, Thinlet)'">drawImage</error>(icon, 2, (bounds.<error descr="Cannot resolve symbol 'height'">height</error> - icon.<error descr="Cannot resolve method 'getHeight(Thinlet)'">getHeight</error>(this)) / 2, this); //java
					//midp g.drawImage(icon, 2, bounds.height / 2, Graphics.LEFT | Graphics.VCENTER);
				}
				paintArrow(g, bounds.<error descr="Cannot resolve symbol 'width'">width</error> - block, 0, block, bounds.<error descr="Cannot resolve symbol 'height'">height</error>,
					'S', enabled, inside, pressed, "down", true, false, true, true);
			} else {
				paintRect(g, 0, 0, bounds.<error descr="Cannot resolve symbol 'width'">width</error>, bounds.<error descr="Cannot resolve symbol 'height'">height</error>,
					enabled ? c_border : c_disable,
					enabled ? ((inside != pressed) ? c_hover :
						(pressed ? c_press : c_ctrl)) : c_bg, true, true, true, true);
				paintContent(component, g, clipx, clipy, clipwidth, clipheight,
					2, 2, bounds.<error descr="Cannot resolve symbol 'width'">width</error> - block - 4, bounds.<error descr="Cannot resolve symbol 'height'">height</error> - 4,
					enabled ? c_text : c_disable, "left", false);
				paintArrow(g, bounds.<error descr="Cannot resolve symbol 'width'">width</error> - block, 0, block, bounds.<error descr="Cannot resolve symbol 'height'">height</error>, 'S');
				if (focus) {
					g.<error descr="Cannot resolve method 'setColor(Color)'">setColor</error>(c_focus);
					g.<error descr="Cannot resolve method 'drawRect(int, int, ?, ?)'">drawRect</error>(2, 2, bounds.<error descr="Cannot resolve symbol 'width'">width</error> - block - 5, bounds.<error descr="Cannot resolve symbol 'height'">height</error> - 5);
				}
			}
		}
		else if ("combolist" == classname) {
			<error descr="Cannot resolve symbol 'Rectangle'">Rectangle</error> view = getRectangle(component, ":view");
			<error descr="Cannot resolve symbol 'Rectangle'">Rectangle</error> viewport = getRectangle(component, ":port");
			g.<error descr="Cannot resolve method 'setColor(Color)'">setColor</error>(c_border);
			g.<error descr="Cannot resolve method 'drawRect(?, ?, ?, ?)'">drawRect</error>(viewport.<error descr="Cannot resolve symbol 'x'">x</error>, viewport.<error descr="Cannot resolve symbol 'y'">y</error>, viewport.<error descr="Cannot resolve symbol 'width'">width</error> - 1, viewport.<error descr="Cannot resolve symbol 'height'">height</error> - 1);
			if (paintScrollPane(g, clipx, clipy, clipwidth, clipheight,
					bounds, view, viewport, enabled, inside, pressed)) {
				Object selected = get(component, "inside");
				int ly = clipy - viewport.<error descr="Cannot resolve symbol 'y'">y</error> - 1;
				int yfrom = view.<error descr="Cannot resolve symbol 'y'">y</error> + Math.max(0, ly);
				int yto = view.<error descr="Cannot resolve symbol 'y'">y</error> + Math.min<error descr="Cannot resolve method 'min(?, int)'">(viewport.<error descr="Cannot resolve symbol 'height'">height</error> - 2, ly + clipheight)</error>;
				for (Object choice = get(get(component, "combobox"), "choice");
						choice != null; choice = get(choice, ":next")) {
					<error descr="Cannot resolve symbol 'Rectangle'">Rectangle</error> r = getRectangle(choice, "bounds");
					if (yto <= r.<error descr="Cannot resolve symbol 'y'">y</error>) { break; }
					if (yfrom >= r.<error descr="Cannot resolve symbol 'y'">y</error> + r.<error descr="Cannot resolve symbol 'height'">height</error>) { continue; }
					boolean armed = (selected == choice);
					paintRect(g, r.<error descr="Cannot resolve symbol 'x'">x</error>, r.<error descr="Cannot resolve symbol 'y'">y</error>, bounds.<error descr="Cannot resolve symbol 'width'">width</error> - 2, r.<error descr="Cannot resolve symbol 'height'">height</error>, c_border,
						armed ? c_select : c_bg, false, false, false, false);
					paintContent(choice, g, clipx, yfrom, clipwidth, yto - yfrom,
						r.<error descr="Cannot resolve symbol 'x'">x</error> + 4, r.<error descr="Cannot resolve symbol 'y'">y</error> + 2, bounds.<error descr="Cannot resolve symbol 'width'">width</error> - 10, r.<error descr="Cannot resolve symbol 'height'">height</error> - 4,
						getBoolean(choice, "enabled", true) ? c_text : c_disable, "left", false);
				}
				resetScrollPane(g, clipx, clipy, clipwidth, clipheight, view, viewport);
			}
			//paintRect(g, 0, 0, bounds.width, bounds.height,
			//	secondary1, c_ctrl, true, true, true, true);
		}
		else if (("textfield" == classname) || ("passwordfield" == classname)) {
			paintField(g, clipx, clipy, clipwidth, clipheight, component,
				bounds.<error descr="Cannot resolve symbol 'width'">width</error>, bounds.<error descr="Cannot resolve symbol 'height'">height</error>,
				inside, pressed, focus, enabled, ("passwordfield" == classname), 0);
		}
		else if ("textarea" == classname) {
			<error descr="Cannot resolve symbol 'Rectangle'">Rectangle</error> view = getRectangle(component, ":view");
			<error descr="Cannot resolve symbol 'Rectangle'">Rectangle</error> viewport = getRectangle(component, ":port");
			boolean editable = getBoolean(component, "editable", true);
			paintRect(g, viewport.<error descr="Cannot resolve symbol 'x'">x</error>, viewport.<error descr="Cannot resolve symbol 'y'">y</error>, viewport.<error descr="Cannot resolve symbol 'width'">width</error>, viewport.<error descr="Cannot resolve symbol 'height'">height</error>,
				enabled ? c_border : c_disable, editable ? c_textbg : c_bg,
				true, true, true, true);
			if (paintScrollPane(g, clipx, clipy, clipwidth, clipheight,
					bounds, view, viewport, enabled, inside, pressed)) {
				String text = getString(component, "text", "");
				int start = focus ? getInteger(component, "start", 0) : 0;
				int end = focus ? getInteger(component, "end", 0) : 0;
				int is = Math.min(start, end); int ie = Math.max(start, end);
				boolean wrap = getBoolean(component, "wrap", false);
				<error descr="Cannot resolve symbol 'FontMetrics'">FontMetrics</error> fm = g.<error descr="Cannot resolve method 'getFontMetrics()'">getFontMetrics</error>(); //java
				int fontascent = fm.<error descr="Cannot resolve method 'getAscent()'">getAscent</error>(); int fontheight = fm.<error descr="Cannot resolve method 'getHeight()'">getHeight</error>(); //java
				//midp int fontheight = fm.getHeight();
				int ascent = 1;
				int ly = clipy - viewport.<error descr="Cannot resolve symbol 'y'">y</error> - 1;
				int yfrom = view.<error descr="Cannot resolve symbol 'y'">y</error> + Math.max(0, ly);
				int yto = view.<error descr="Cannot resolve symbol 'y'">y</error> + Math.min<error descr="Cannot resolve method 'min(?, int)'">(viewport.<error descr="Cannot resolve symbol 'height'">height</error> - 2, ly + clipheight)</error>;
				//g.setColor(Color.pink); g.fillRect(0, yfrom - 1, 75, 2); g.fillRect(0, yto - 1, 75, 2);

				boolean prevletter = false; int n = text.length(); char c = 0;
				for (int i = 0, j = -1, k = 0; k <= n; k++) { // j is the last space index (before k)
					if (yto <= ascent) { break; }
					if (wrap) {
						if (((k == n) || ((c = text.charAt(k)) == '\n') || (c == ' ')) &&
								(j  > i) && (fm.<error descr="Cannot resolve method 'stringWidth(java.lang.String)'">stringWidth</error>(text.substring(i, k)) > viewport.<error descr="Cannot resolve symbol 'width'">width</error> - 4)) {
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
							int xs = (is < i) ? -1 : (((j != -1) && (is > j)) ? (view.<error descr="Cannot resolve symbol 'width'">width</error> - 1) :
								fm.<error descr="Cannot resolve method 'stringWidth(java.lang.String)'">stringWidth</error>(text.substring(i, is))); //java
								//midp font.substringWidth(text, i, is - i));
							int xe = ((j != -1) && (ie > j)) ? (view.<error descr="Cannot resolve symbol 'width'">width</error> - 1) :
								fm.<error descr="Cannot resolve method 'stringWidth(java.lang.String)'">stringWidth</error>(text.substring(i, ie)); //java
								//midp font.substringWidth(text, i, ie - i);
							g.<error descr="Cannot resolve method 'setColor(Color)'">setColor</error>(c_select);
							g.<error descr="Cannot resolve method 'fillRect(int, int, int, int)'">fillRect</error>(1 + xs, ascent, xe - xs, fontheight);
						}
						g.<error descr="Cannot resolve method 'setColor(Color)'">setColor</error>(enabled ? c_text : c_disable);
						g.<error descr="Cannot resolve method 'drawString(java.lang.String, int, int)'">drawString</error>(line, 1, ascent + fontascent); //java
						//midp g.drawSubstring(text, i, ((j != -1) ? j : text.length()) - i, 1, ascent, Graphics.LEFT | Graphics.TOP);
						if (focus && (end >= i) && ((j == -1) || (end <= j))) {
							int caret = fm.<error descr="Cannot resolve method 'stringWidth(java.lang.String)'">stringWidth</error>(text.substring(i, end)); //java
							//midp int caret = font.substringWidth(text, i, end - i);
							g.<error descr="Cannot resolve method 'setColor(Color)'">setColor</error>(c_focus);
							g.<error descr="Cannot resolve method 'fillRect(int, int, int, int)'">fillRect</error>(caret, ascent, 1, fontheight);
						}
					}
					ascent += fontheight;
					i = j + 1;
				}
				resetScrollPane(g, clipx, clipy, clipwidth, clipheight, view, viewport);
			}
		}
		else if ("tabbedpane" == classname) {
			int i = 0; <error descr="Cannot resolve symbol 'Rectangle'">Rectangle</error> last = null;
			int selected = getInteger(component, "selected", 0);
			String placement = getString(component, "placement", "top");
			for (Object comp = get(component, "tab");
					comp != null; comp = get(comp, ":next")) {
				<error descr="Cannot resolve symbol 'Rectangle'">Rectangle</error> r = getRectangle(comp, "bounds");
				boolean hover = !(selected == i) && inside &&
					(mousepressed == null) && (insidepart == comp);
				boolean sel = (selected == i);
				boolean tabenabled = enabled && getBoolean(comp, "enabled", true);
				paintRect(g, r.<error descr="Cannot resolve symbol 'x'">x</error>, r.<error descr="Cannot resolve symbol 'y'">y</error>, r.<error descr="Cannot resolve symbol 'width'">width</error>, r.<error descr="Cannot resolve symbol 'height'">height</error>,
					enabled ? c_border : c_disable,
					tabenabled ? (sel ? c_bg : (hover ? c_hover : c_ctrl)) : c_ctrl,
					(placement != "bottom") || !sel, (placement != "right") || !sel,
					(placement == "bottom") || ((placement == "top") && !sel),
					(placement == "right") || ((placement == "left") && !sel));
				if (focus && sel) {
					g.<error descr="Cannot resolve method 'setColor(Color)'">setColor</error>(c_focus);
					g.<error descr="Cannot resolve method 'drawRect(?, ?, ?, ?)'">drawRect</error>(r.<error descr="Cannot resolve symbol 'x'">x</error> + 2, r.<error de
