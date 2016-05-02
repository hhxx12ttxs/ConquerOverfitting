/*
 * jEdit - Programmer's Text Editor
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright Â 2010, 2011 Matthieu Casanova
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package gatchan.highlight.color;

import java.awt.*;

/**
 * A color token.
 * It contains the color and the offset within the line where it is.
 *
 * @author Matthieu Casanova
 */
public class ColorToken
{
	private final int start;
	private final int end;

	private final Color color;

	public ColorToken(int start, int end, String hexa)
	{
		this.start = start;
		this.end = end;
		color = Color.decode(hexa);
	}

	public int getStart()
	{
		return start;
	}

	public int getEnd()
	{
		return end;
	}

	public Color getColor()
	{
		return color;
	}

	@Override
	public String toString()
	{
		return "ColorToken[" + color + ',' + start + ',' + end + ']';
	}
}

