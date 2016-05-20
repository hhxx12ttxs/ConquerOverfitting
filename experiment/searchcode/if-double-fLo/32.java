/*******************************************************************************
 * Copyright (c) 2006 ... 2009 ekkehard gentz
 * http://ekkehard.org, http://ekkes-corner.org, http://open-erp-ware.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Based on work of compeople AG and Eclipse Riena project
 * http://eclipse.org/riena
 *******************************************************************************/
package org.redview.lnf;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.BundleContext;
import org.redview.lnf.internal.LnfHandler;
import org.redview.lnf.internal.SharedColors;
import org.redview.lnf.internal.SharedFonts;

/**
 * The activator class controls the plug-in life cycle
 * 
 * THIS CLASS IS ONLY A TEMPLATE FOR FLO
 * 
 * viel spass
 * 
 * ekke
 * 
 */
public class LnfPlugin extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.redview.lnf"; //$NON-NLS-1$

	// The shared instance
	private static LnfPlugin plugin;

	private final LnfHandler lnfHandler = new LnfHandler();

	// Helper class for shared colors
	private static SharedColors sharedColors;

	// Helper class for shared fonts
	private static SharedFonts sharedFonts;

	private List<LnfChangedListener> lnfListener = new ArrayList<LnfChangedListener>();

	/**
	 * The constructor
	 */
	public LnfPlugin() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if (sharedColors != null) {
			sharedColors.dispose();
			sharedColors = null;
		}
		if (sharedFonts != null) {
			sharedFonts.dispose();
			sharedFonts = null;
		}
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static LnfPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the color by the given key from the lnf. Note, a LnfKey must be
	 * passed.
	 * 
	 * @param key
	 * @return
	 */
	public Color getLnfColor(String key) {
		if (key != null && key.contains(SharedColors.RBG_COLOR_DELIMITER)) {
			return getColor(Display.getDefault(), key);
		}
		return lnfHandler.getColor(key);
	}

	/**
	 * Notifies the plugin, that the property of
	 * 
	 * @param proerty
	 */
	public void notifyLnfChanged(LnfChangedListener.Property property) {
		notifyLnfListener(property);
	}

	/**
	 * Returns the font by the given key from the lnf. Note, a LnfKey must be
	 * passed.
	 * 
	 * @param key
	 * @return
	 */
	public Font getLnfFont(String key) {
		return lnfHandler.getFont(key);
	}

	/**
	 * Returns the font for the given key. The passed properties height and
	 * style will be applied to the font. <br>
	 * 
	 * @param key
	 * @param height
	 * @param style
	 * @return the font to which this map maps the specified key with differing
	 *         height and style, or <code>null</code> if the map contains no
	 *         mapping for this lnfKeyConstants key.
	 * 
	 * 
	 */
	// TODO change this to riena lnf
	public Font getLnfFont(String key, int height, int style) {
		if (sharedFonts == null) {
			sharedFonts = new SharedFonts(Display.getDefault());
		}
		return sharedFonts.getFont(key, height, style);
	}

	/**
	 * Returns the Integer by the given key from the lnf. Note, a LnfKey must be
	 * passed.
	 * 
	 * @param key
	 * @return
	 */
	public Integer getLnfInt(String key) {
		return lnfHandler.getInt(key);
	}

	/**
	 * Returns the Double by the given key from the lnf. Note, a LnfKey must be
	 * passed.
	 * 
	 * @param key
	 * @return
	 */
	public Double getLnfDouble(String key) {
		return lnfHandler.getDouble(key);
	}

	/**
	 * Converts the given pixel to the operating system representation.
	 * 
	 * @param pixel
	 * @return
	 */
	public static int toOsPixels(int pixel) {
		double transformFactor = getDefault().getLnfDouble(
				RedviewLnfKeyConstants.REDVIEW_SETTING_OS_TRANSFORM_FACTOR);
		return (int) (pixel * transformFactor);
	}

	/**
	 * Return a "shared" color.
	 * 
	 */
	public static synchronized Color getColor(Display display, String colorKey) {
		Assert.isNotNull(display);
		if (sharedColors == null) {
			sharedColors = new SharedColors(display);
		}
		return sharedColors.getSharedColor(colorKey);
	}

	/**
	 * Return a "shared" color.
	 */
	public static synchronized Color getColor(Display display, RGB colorKey) {
		Assert.isNotNull(display);
		if (sharedColors == null) {
			sharedColors = new SharedColors(display);
		}
		return sharedColors.getSharedColor(colorKey);
	}

	/**
	 * Return a "shared" font.
	 */
	public static synchronized Font getFont(Display display, String fontKey,
			int size, int style) {
		Assert.isNotNull(display);
		if (sharedFonts == null) {
			sharedFonts = new SharedFonts(display);
		}
		return sharedFonts.getFont(fontKey, size, style);
	}

	/**
	 * Adds a {@link LnfChangedListener} to the list of listeners.
	 * 
	 * @param listener
	 */
	public void addLnfListener(LnfChangedListener listener) {
		lnfListener.add(listener);
	}

	/**
	 * Removes a {@link LnfChangedListener} from the list of listeners.
	 * 
	 * @param listener
	 */
	public void removeLnfListener(LnfChangedListener listener) {
		lnfListener.remove(listener);
	}

	/**
	 * Notifies the {@link LnfChangedListener} of a property change.
	 * 
	 * @param property
	 */
	void notifyLnfListener(LnfChangedListener.Property property) {
		for (LnfChangedListener listener : lnfListener) {
			listener.lnfChanged(property);
		}
	}

}

