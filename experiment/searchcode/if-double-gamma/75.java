/*
 * This file is part of FFractal, created by Guilhelm Savin and modified
 * by Bilyan Borisov.
 * 
 * FFractal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * FFractal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with FFractal.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2010
 * 	Guilhelm Savin
 * 
 * Copyright 2012
 *  Bilyan Borisov
 */
package org.ri2c.flame;

import java.awt.Color;

public interface ColorProvider
{
	Color getColor( double x, double y, double color,
			double frequency );

	public static class RandomColorProvider
		implements ColorProvider
	{
		java.util.Random random;

		public RandomColorProvider()
		{
			this.random = new java.util.Random();
		}

		@Override
		public Color getColor( double x, double y,
				double color, double frequency )
		{
			return new Color(
					random.nextFloat(),
					random.nextFloat(),
					random.nextFloat(),
					random.nextFloat());
		}
	}

	public static class Basic
		implements ColorProvider
	{
		double gamma;
		
		public Basic()
		{
			this( 2.5 ); //originally 3.0, edited by BBorisov - standart for LCD displays
		}
		
		public Basic( double gamma )
		{
			this.gamma = gamma;
		}
		
		@Override
		public Color getColor( double x, double y,
				double color, double frequency )
		{
			return new Color( ARGBTools.getARGBfromTSV( Math.max(10,(int) ( Math.pow(frequency,1/gamma) * 255 )), (float) color * 360.0f, 1, 1 ), true );
		
		}
	}
}
