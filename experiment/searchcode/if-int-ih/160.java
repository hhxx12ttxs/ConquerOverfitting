package org.dyno.visual.swing.widgets;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.net.URL;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;

import org.dyno.visual.swing.base.ContextCustomizerAdapter;
import org.dyno.visual.swing.base.JavaUtil;
import org.dyno.visual.swing.plugin.spi.WidgetAdapter;

@SuppressWarnings("serial")
public class JPopupMenuContextCustomizer extends ContextCustomizerAdapter {
	private static final String DROPDOWN_ICON = "/icons/dropdown.png";
	private static final String GRAY_DROPDOWN_ICON = "/icons/gray_dropdown.png";
	private static final String PULLUP_ICON = "/icons/pullup.png";
	private static final String GRAY_PULLUP_ICON = "/icons/gray_pullup.png";
	static java.awt.Image DROPDOWN_IMAGE;
	static java.awt.Image GRAY_DROPDOWN_IMAGE;
	static java.awt.Image PULLUP_IMAGE;
	static java.awt.Image GRAY_PULLUP_IMAGE;
	static JComponent DUMMY = new JComponent() {
	};
	static {
		URL url = ButtonGroupAdapter.class.getResource(DROPDOWN_ICON);
		DROPDOWN_IMAGE = Toolkit.getDefaultToolkit().getImage(url);
		url = ButtonGroupAdapter.class.getResource(PULLUP_ICON);
		PULLUP_IMAGE = Toolkit.getDefaultToolkit().getImage(url);
		url = ButtonGroupAdapter.class.getResource(GRAY_DROPDOWN_ICON);
		GRAY_DROPDOWN_IMAGE = Toolkit.getDefaultToolkit().getImage(url);
		url = ButtonGroupAdapter.class.getResource(GRAY_PULLUP_ICON);
		GRAY_PULLUP_IMAGE = Toolkit.getDefaultToolkit().getImage(url);
		MediaTracker mt = new MediaTracker(DUMMY);
		mt.addImage(DROPDOWN_IMAGE, 0);
		while (true) {
			try {
				mt.waitForAll();
			} catch (InterruptedException e) {
			}
			if (mt.checkID(0))
				break;
		}
	}

	@Override
	public void paintContext(Graphics g, WidgetAdapter rootAdapter) {
		List<Component> selected = rootAdapter.getSelection();
		if (selected != null && !selected.isEmpty()) {
			for (Component comp : selected) {
				if (comp instanceof JComponent) {
					JComponent jcomp = (JComponent) comp;
					JPopupMenu jpm = JavaUtil.getComponentPopupMenu(jcomp);
					if (jpm != null && WidgetAdapter.getWidgetAdapter(jpm) != null) {
						WidgetAdapter bAdapter = WidgetAdapter.getWidgetAdapter(comp);
						Point p = bAdapter.convertToGlobal(new Point(0, 0));
						Rectangle rect = comp.getBounds();
						rect.x = p.x;
						rect.y = p.y;
						String state = (String) bAdapter.getProperty("popup.state");
						if (state == null)
							state = "normal";
						Image image = state.equals("normal") ? 
								(jpm.isVisible() ? 
										GRAY_PULLUP_IMAGE : 
											GRAY_DROPDOWN_IMAGE) : 
									(jpm.isVisible() ? 
											PULLUP_IMAGE : 
												DROPDOWN_IMAGE);
						int iw = image.getWidth(DUMMY);
						int ih = image.getHeight(DUMMY);
						Point loc = new Point(rect.x + rect.width / 2 - iw, rect.y + rect.height / 2 - ih);
						g.drawImage(image, loc.x, loc.y, DUMMY);
						g.setColor(Color.lightGray);
						if (state.equals("up")) {
							g.draw3DRect(loc.x - 1, loc.y - 1, iw + 2, ih + 2, true);
						} else if (state.equals("down")) {
							g.draw3DRect(loc.x - 1, loc.y - 1, iw + 2, ih + 2, false);
						}
					}
				}
			}
		}

	}
}

