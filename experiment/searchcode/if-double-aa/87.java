/*
 * jhandles 
 *
 * Copyright (C) 2007 Michael Goffioul 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 *
 */

package org.octave.graphics;

import java.util.Map;
import java.util.HashMap;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.font.TextAttribute;
import java.awt.Container;
import java.awt.Component;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.UIManager;

public class Utils
{
	public static void crossProduct(double ax, double ay, double az, double bx, double by, double bz, double[] res)
	{
		crossProduct(ax, ay, az, bx, by, bz, res, 0, 1);
	}

	public static void crossProduct(double ax, double ay, double az, double bx, double by, double bz,
			double[] res, int offset, int ldr)
	{
		res[offset+0*ldr] += (ay*bz-az*by);
		res[offset+1*ldr] += (az*bx-ax*bz);
		res[offset+2*ldr] += (ax*by-ay*bx);
	}

	public static void printCpuTime()
	{
		System.out.println(java.lang.management.ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime());
	}

	public static double[][] getAlphaData(ArrayProperty adata, RadioProperty mapping, AxesObject axes)
	{
		if (adata.getNDims() != 2)
			return null;

		if (mapping.is("none"))
		{
			if (adata.isType("double"))
				return adata.asDoubleMatrix();
		}
		else if (mapping.is("direct"))
		{
			double[] amap = axes.getFigure().Alphamap.getArray();
			if (adata.isType("double"))
			{
				double[][] aa = adata.asDoubleMatrix();
				double[][] res = new double[aa.length][aa[0].length];

				for (int i=0; i<res.length; i++)
					for (int j=0; j<res[i].length; j++)
						res[i][j] = amap[(int)Math.min(Math.max(1, aa[i][j]), amap.length)-1];
				return res;
			}
			else if (adata.isType("integer"))
			{
				int[][] aa = adata.asIntMatrix();
				double[][] res = new double[aa.length][aa[0].length];
				
				for (int i=0; i<res.length; i++)
					for (int j=0; j<res[i].length; j++)
						res[i][j] = amap[Math.min(Math.max(0, aa[i][j]), amap.length-1)];
				return res;
			}
		}
		else if (mapping.is("scaled"))
		{
			double[] amap = axes.getFigure().Alphamap.getArray();
			double[] alim = axes.ALim.getArray();

			if (adata.isType("double"))
			{
				double[][] aa = adata.asDoubleMatrix();
				double[][] res = new double[aa.length][aa[0].length];

				for (int i=0; i<aa.length; i++)
					for (int j=0; j<aa[0].length; j++)
					{
						double s = (aa[i][j]-alim[0])/(alim[1]-alim[0]);
						res[i][j] = amap[(int)Math.round((amap.length-1)*s)];
					}
				return res;
			}
		}

		return null;
	}
	
	public static Font getFont(StringProperty FontName, DoubleProperty FontSize,
		RadioProperty FontUnits, RadioProperty FontAngle, RadioProperty FontWeight, double h)
	{
		Map map = new HashMap();

		map.put(TextAttribute.FAMILY, FontName.toString());
		map.put(TextAttribute.POSTURE,
			FontAngle.is("normal") ? TextAttribute.POSTURE_REGULAR : TextAttribute.POSTURE_OBLIQUE);
		map.put(TextAttribute.WEIGHT,
			FontWeight.is("normal") ? TextAttribute.WEIGHT_REGULAR :
			FontWeight.is("light") ? TextAttribute.WEIGHT_LIGHT :
			FontWeight.is("demi") ? TextAttribute.WEIGHT_SEMIBOLD : TextAttribute.WEIGHT_BOLD);
		float fs = 12;
		if (FontUnits.is("points"))
			fs = FontSize.floatValue();
		else if (FontUnits.is("normalized"))
			fs = (float)(FontSize.doubleValue()*h);
		else if (FontUnits.is("inches"))
			fs = FontSize.floatValue()*72;
		else if (FontUnits.is("centimeters"))
			fs = (FontSize.floatValue()/2.54f)*72;
		else
			System.out.println("Warning: ignoring FontUnits (" + FontUnits.getValue() + ")");
		map.put(TextAttribute.SIZE, new Float(fs));
		
		return new Font(map);
	}

	public static double[] convertPosition(double[] pos, String fromUnits, String toUnits, Component parent)
	{
		double[] p = null;
		boolean isContainer = (parent instanceof Container);

		if (fromUnits.equalsIgnoreCase("pixels"))
			p = (double[])pos.clone();
		else if (fromUnits.equalsIgnoreCase("normalized"))
		{
			Insets ir = (isContainer ? ((Container)parent).getInsets() : new Insets(0, 0, 0, 0));
			Rectangle r = parent.getBounds();
			int w = r.width-ir.left-ir.right, h = r.height-ir.top-ir.bottom;

			p = new double[] {pos[0]*w+1, pos[1]*h+1, pos[2]*w, pos[3]*h};
		}
		else if (fromUnits.equalsIgnoreCase("characters"))
		{
			FontMetrics fm = parent.getFontMetrics(Font.decode(""));
			int w = fm.charWidth('x'), h = fm.getHeight();
			
			p = new double[] {pos[0]*w+1, pos[1]*h+1, pos[2]*w, pos[3]*h};
		}

		if (!toUnits.equalsIgnoreCase("pixels"))
		{
			if (toUnits.equalsIgnoreCase("normalized"))
			{
				Insets ir = (isContainer ? ((Container)parent).getInsets() : new Insets(0, 0, 0, 0));
				Rectangle r = parent.getBounds();
				int w = r.width-ir.left-ir.right, h = r.height-ir.top-ir.bottom;

				p[0] = (p[0]-1)/w;
				p[1] = (p[1]-1)/h;
				p[2] /= w;
				p[3] /= h;
			}
			else if (toUnits.equalsIgnoreCase("characters"))
			{
				FontMetrics fm = parent.getFontMetrics(Font.decode(""));
				int w = fm.charWidth('x'), h = fm.getHeight();

				p[0] = (p[0]-1)/w;
				p[1] = (p[1]-1)/h;
				p[2] /= w;
				p[3] /= h;
			}
		}

		return p;
	}

	private static Color getUIColor(String name, Color def)
	{
		/* make sure LookAndFeel is initialized */
		RootObject.getInstance();

		try { return (Color)UIManager.get(name); }
		catch (Exception e) { return def; }
	}

	public static Color getBackgroundColor()
	{
		return getUIColor("control", Color.lightGray);
	}

	public static Color getShadowColor()
	{
		return getUIColor("controlShadow", Color.gray);
	}

	public static Color getHighlightColor()
	{
		return getUIColor("controlLtHighlight", Color.white);
	}
}

