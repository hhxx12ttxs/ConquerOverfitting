/*
 *      TomeGraph.java
 *      
 *      Copyright 2007 Ajish George <ajish@hocuslocus.com>
 *      
 *      This program is free software; you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation; either version 2 of the License, or
 *      (at your option) any later version.
 *      
 *      This program is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *      
 *      You should have received a copy of the GNU General Public License
 *      along with this program; if not, write to the Free Software
 *      Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 *      MA 02110-1301, USA.
 */
package org.omelogic.tome;


import edu.uci.ics.jung.graph.impl.UndirectedSparseVertex;
import java.lang.Cloneable;
import java.lang.CloneNotSupportedException;

public class TomeVertex extends UndirectedSparseVertex implements Cloneable
{
	private String id;
	private Double cc;
	private Double ad;
	private Double dd;
	
	public static String[] COLUMN_NAMES = { "ID", "ClustCoeff", "AvgPathLen", "Degree" };
	
	public TomeVertex( String name )
	{
		super();
		this.id = name;
		this.cc= null;
		this.ad = null;
		this.dd = null;
	}
	
	public void setClusteringCoefficient( Double coeff )
	{
		this.cc = coeff;
	}
	
	public void setAverageDistance( Double distance )
	{
		this.ad = distance;
	}
	
	public void setDegree( Double degree )
	{
		this.dd = degree;
	}
	
	public String getID()
	{
		return id;
	}
	
	public Double getClusteringCoefficient()
	{
		return cc;
	}
	
	public Double getAverageDistance()
	{
		return ad;
	}

	public Double getDegree()
	{
		return dd;
	}
	
	public Object[] getVertexRow()
	{
		Object[] row = { id, cc, ad, dd };
		return row;
	}
	
	
	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
		
	}

}

