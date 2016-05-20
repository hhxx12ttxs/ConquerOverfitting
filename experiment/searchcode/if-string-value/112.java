/**
 * Copyright (c) 2008 Raise Partner
 * 22, av. Doyen Louis Weil,
 * 38000 Grenoble, France
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Contact: sebastien@chassande.fr
 */
package com.raisepartner.chartfusion.api.styles;

public final class BevelStyle extends Style {

	public final static String PARAMETER_distance = "distance";
	public final static String PARAMETER_angle = "angle";
	public final static String PARAMETER_shadowColor = "shadowColor";
	public final static String PARAMETER_shadowAlpha = "shadowAlpha";
	public final static String PARAMETER_highlightColor = "highlightColor";
	public final static String PARAMETER_highlightAlpha = "highlightAlpha";
	public final static String PARAMETER_blurX = "blurX";
	public final static String PARAMETER_blurY = "blurY";
	public final static String PARAMETER_strength = "strength";
	public final static String PARAMETER_quality = "quality";
	
	public BevelStyle(String styleName) {
		super(styleName, "bevel");
	}
	/**
	 * The offset distance of the bevel. Valid values are in pixels 
	 * (floating point). The default value is 4.
	 */
	public void setDistance(String value) {
		setAttribute(PARAMETER_distance, value);
	}
	/**
	 * The angle of the bevel. Valid values are from 0 to 360 degrees. The 
	 * default value is 45
	 */
	public void setAngle(String value) {
		setAttribute(PARAMETER_angle, value);
	}
	/**
	 * The shadow color of the bevel. Valid values are in hexadecimal format 
	 * RRGGBB (without #). The default value is 000000
	 */
	public void setShadowColor(String value) {
		setAttribute(PARAMETER_shadowColor, value);
	}
	/**
	 * The alpha transparency value of the shadow color. This value is 
	 * specified as a normalized value from 0 to 100. For example, 25 set a 
	 * transparency value of 25%. The default value is 50.
	 */
	public void setShadowAlpha(String value) {
		setAttribute(PARAMETER_shadowAlpha, value);
	}
	/**
	 * The highlight color of the bevel. Valid values are in hexadecimal format 
	 * RRGGBB (without #). The default value is FFFFFF
	 */
	public void setHighlightColor(String value) {
		setAttribute(PARAMETER_highlightColor, value);
	}
	/**
	 * The alpha transparency value of the highlight color. The value is 
	 * specified as a normalized value from 0 to 100. For example, 25 sets a 
	 * transparency value of 25%. The default value is 50.
	 */
	public void setHighlightAlpha(String value) {
		setAttribute(PARAMETER_highlightAlpha, value);
	}
	/**
	 * The amount of horizontal blur in pixels. Valid values are from 0 to 255 
	 * (floating point). The default value is 4. Values that are a power of 
	 * 2 (such as 2, 4, 8, 16, and 32) are optimized to render more quickly 
	 * than other values
	 */
	public void setBlurX(String value) {
		setAttribute(PARAMETER_blurX, value);
	}
	/**
	 * The amount of vertical blur in pixels. Valid values are from 0 to 255 
	 * (floating point). The default value is 4. Values that are a power of 
	 * 2 (such as 2, 4, 8, 16, and 32) are optimized to render more quickly 
	 * than other values
	 */
	public void setBlurY(String value) {
		setAttribute(PARAMETER_blurY, value);
	}
	/**
	 * The strength of the imprint or spread. Valid values are from 0 to 255. 
	 * The larger the value, the more color is imprinted and the stronger the 
	 * contrast between the bevel and the background. The default value is 1
	 */
	public void setStrength(String value) {
		setAttribute(PARAMETER_strength, value);
	}
	/**
	 * The number of times to apply the filter. The default value is 1, which 
	 * is equivalent to low quality. A value of 2 is medium quality, and a 
	 * value of 3 is high quality. Filters with lower values are rendered 
	 * more quickly
	 */
	public void setQuality(String value) {
		setAttribute(PARAMETER_quality, value);
	}
}

