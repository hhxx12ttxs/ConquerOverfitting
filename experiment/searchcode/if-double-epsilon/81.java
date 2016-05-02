/* jCAE stand for Java Computer Aided Engineering. Features are : Small CAD
   modeler, Finite element mesher, Plugin architecture.

    Copyright (C) 2007, by EADS France

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
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */

package org.jcae.mesh.amibe.ds;

import java.util.HashMap;
import java.util.Map;

/**
 * Mesh parameters.
 */
public class MeshParameters
{
	// By convention, Mesh2D.init() will set edgeLength to patch diagonal
	// if size has not been set.
	private double edgeLength = 0.0;
	private double deflection = -1.0;
	private boolean relativeDeflection;
	private boolean isotropic;
	private double epsilon;
	private boolean cumulativeEpsilon;

	public MeshParameters()
	{
		this(new HashMap<String, String>());
	}

	public MeshParameters(final Map<String, String> options)
	{
		// First process system properties
		String relativeDeflectionProperty = System.getProperty("org.jcae.mesh.amibe.ds.Metric3D.relativeDeflection");
		if (relativeDeflectionProperty == null)
		{
			relativeDeflectionProperty = "true";
			System.setProperty("org.jcae.mesh.amibe.ds.Metric3D.relativeDeflection", relativeDeflectionProperty);
		}
		relativeDeflection = relativeDeflectionProperty.equals("true");

		String cumulativeEpsilonProperty = System.getProperty("org.jcae.mesh.amibe.ds.Mesh.cumulativeEpsilon");
		if (cumulativeEpsilonProperty == null)
		{
			cumulativeEpsilonProperty = "false";
			System.setProperty("org.jcae.mesh.amibe.ds.Mesh.cumulativeEpsilon", cumulativeEpsilonProperty);
		}
		cumulativeEpsilon = cumulativeEpsilonProperty.equals("true");

		String epsilonProperty = System.getProperty("org.jcae.mesh.amibe.ds.Mesh.epsilon");
		if (epsilonProperty == null)
		{
			epsilonProperty = "0.0";
			System.setProperty("org.jcae.mesh.amibe.ds.Mesh.epsilon", epsilonProperty);
		}
		epsilon = Double.valueOf(epsilonProperty).doubleValue();

		String isotropicProperty = System.getProperty("org.jcae.mesh.Mesher.isotropic");
		if (isotropicProperty == null)
		{
			isotropicProperty = "true";
			System.setProperty("org.jcae.mesh.Mesher.isotropic", isotropicProperty);
		}
		isotropic = isotropicProperty.equals("true");

		// Next process arguments
		for (final Map.Entry<String, String> opt: options.entrySet())
		{
			final String key = opt.getKey();
			final String val = opt.getValue();
			if (key.equals("size"))
				edgeLength = Double.valueOf(val).doubleValue();
			else if (key.equals("deflection"))
				deflection = Double.valueOf(val).doubleValue();
			else if (key.equals("relativeDeflection"))
				relativeDeflection = Boolean.valueOf(val).booleanValue();
			else if (key.equals("isotropic"))
				isotropic = Boolean.valueOf(val).booleanValue();
			else if (key.equals("epsilon"))
				epsilon = Double.valueOf(val).doubleValue();
			else if (key.equals("cumulativeEpsilon"))
				cumulativeEpsilon = Boolean.valueOf(val).booleanValue();
			else
				throw new RuntimeException("Unknown option: "+key);
		}
	}

	public final double getLength()
	{
		return edgeLength;
	}

	public final void setLength(double e)
	{
		edgeLength = e;
	}

	public final double getEpsilon()
	{
		return epsilon;
	}

	public final void setEpsilon(double e)
	{
		epsilon = e;
	}

	public final double getDeflection()
	{
		return deflection;
	}

	public final boolean isIsotropic()
	{
		return isotropic;
	}

	public final boolean hasDeflection()
	{
		return deflection > 0.0;
	}

	public final boolean hasRelativeDeflection()
	{
		return relativeDeflection;
	}

	public final boolean hasCumulativeEpsilon()
	{
		return cumulativeEpsilon;
	}

	public final void scaleTolerance(double scale)
	{
		epsilon *= scale;
	}
	
}

