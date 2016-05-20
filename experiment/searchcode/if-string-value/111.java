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

public final class ShadowStyle extends Style {

	public final static String PARAMETER_distance = "distance";
	public final static String PARAMETER_angle = "angle";
	public final static String PARAMETER_color = "color";
	public final static String PARAMETER_alpha = "alpha";
	public final static String PARAMETER_blurX = "blurX";
	public final static String PARAMETER_blurY = "blurY";
	public final static String PARAMETER_strength = "strength";
	public final static String PARAMETER_quality = "quality";
	
	public ShadowStyle(String styleName) {
		super(styleName, "shadow");
	}
	/**
	 * The angle of the shadow. Valid values are 0 to 360Ë&#x161;. The default value 
	 * is 45
	 */
	public void setAnlge(String value) {
		setAttribute(PARAMETER_angle, value);
	}
	/**
	 * The offset distance for the shadow (in pixels). The default value is 4
	 */
	public void setDistance(String value) {
		setAttribute(PARAMETER_distance, value);
	}
	/**
	 * The color of the shadow in hex code (without #). The default value 
	 * is CCCCCC
	 */
	public void setColor(String value) {
		setAttribute(PARAMETER_color, value);
	}
	/**
	 * The alpha transparency value for the shadow color. Valid values are 0 
	 * to 100. For example, 25 sets a transparency value of 25%.
	 */
	public void setAlpha(String value) {
		setAttribute(PARAMETER_alpha, value);
	}
	/**
	 * The amount of horizontal blur. Valid values are 0 to 255. The default
	 * value is 4. Values that are a power of 2 (such as 2, 4, 8, 16 and 32) 
	 * are optimized to render more quickly than other values.
	 */
	public void setBlurX(String value) {
		setAttribute(PARAMETER_blurX, value);
	}
	/**
	 * The amount of vertical blur. Valid values are 0 to 255. The default 
	 * value is 4. Values that are a power of 2 (such as 2, 4, 8, 16 and 32) 
	 * are optimized to render more quickly than other values.
	 */
	public void setBlurY(String value) {
		setAttribute(PARAMETER_blurY, value);
	}
	/**
	 * The strength of the imprint or spread. The higher the value, the more 
	 * color is imprinted and the stronger the contrast between the shadow and 
	 * the background. Valid values are from 0 to 255. The default is 1.
	 */
	public void setStrength(String value) {
		setAttribute(PARAMETER_strength, value);
	}
	/**
	 * The number of times to apply the shadow effect. Valid values are 0 to 
	 * 15. The default value is 1, which is equivalent to low quality. A value 
	 * of 2 is medium quality, and a value of 3 is high quality. Shadow with 
	 * lower values are rendered quicker
	 */
	public void setQuality(String value) {
		setAttribute(PARAMETER_quality, value);
	}
}

