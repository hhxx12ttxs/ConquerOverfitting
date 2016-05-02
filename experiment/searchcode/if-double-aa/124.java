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

import java.awt.*;
import org.octave.Matrix;

public class PatchObject extends GraphicObject
{
	private int[] faceCount;

	/* Properties */
	DoubleProperty __Index__;
	ArrayProperty Faces;
	ArrayProperty Vertices;
	ArrayProperty FaceVertexCData;
	RadioProperty CDataMapping;
	VectorProperty FaceVertexAlphaData;
	RadioProperty AlphaDataMapping;
	ColorProperty FaceColor;
	ColorProperty EdgeColor;
	RadioProperty FaceLighting;
	RadioProperty EdgeLighting;
	DoubleRadioProperty FaceAlpha;
	DoubleRadioProperty EdgeAlpha;
	LineStyleProperty LineStyle;
	DoubleProperty LineWidth;
	MarkerProperty Marker;
	DoubleProperty MarkerSize;
	DoubleProperty AmbientStrength;
	DoubleProperty DiffuseStrength;
	DoubleProperty SpecularStrength;
	DoubleProperty SpecularExponent;
	ArrayProperty VertexNormals;

	public PatchObject(HandleObject parent)
	{
		super(parent, "patch");

		Faces = new ArrayProperty(this, "Faces", new String[] {"double"}, 2, null);
		Vertices = new ArrayProperty(this, "Vertices", new String[] {"double"}, 2, null);
		FaceVertexCData = new ArrayProperty(this, "FaceVertexCData", new String[] {"double", "byte"}, 2, null);
		CDataMapping = new RadioProperty(this, "CDataMapping", new String[] {"direct", "scaled"}, "scaled");
		FaceVertexAlphaData = new VectorProperty(this, "FaceVertexAlphaData", -1, new double[0]);
		AlphaDataMapping = new RadioProperty(this, "AlphaDataMapping", new String[] {"none", "scaled", "direct"}, "scaled");
		FaceColor = new ColorProperty(this, "FaceColor", new String[] {"none", "flat", "interp"}, Color.black);
		EdgeColor = new ColorProperty(this, "EdgeColor", new String[] {"none", "flat", "interp"}, Color.black);
		FaceLighting = new RadioProperty(this, "FaceLighting", new String[] {"none", "flat", "gouraud", "phong"}, "none");
		EdgeLighting = new RadioProperty(this, "EdgeLighting", new String[] {"none", "flat", "gouraud", "phong"}, "none");
		FaceAlpha = new DoubleRadioProperty(this, "FaceAlpha", new String[] {"flat", "interp"}, 1.0);
		EdgeAlpha = new DoubleRadioProperty(this, "EdgeAlpha", new String[] {"flat", "interp"}, 1.0);
		LineStyle = new LineStyleProperty(this, "LineStyle", "-");
		LineWidth = new DoubleProperty(this, "LineWidth", 0.5);
		Marker = new MarkerProperty(this, "Marker", "none");
		MarkerSize = new DoubleProperty(this, "MarkerSize", 7.0);
		AmbientStrength = new DoubleProperty(this, "AmbientStrength", 0.3);
		DiffuseStrength = new DoubleProperty(this, "DiffuseStrength", 0.6);
		SpecularStrength = new DoubleProperty(this, "SpecularStrength", 0.9);
		SpecularExponent = new DoubleProperty(this, "SpecularExponent", 10);
		VertexNormals = new ArrayProperty(this, "VertexNormals", new String[] {"double"}, 2, null);
		__Index__ = new DoubleProperty(this, "__Index__", 0);
		__Index__.setVisible(false);

		ZLimInclude.reset(new Boolean(true));
		CLimInclude.reset(new Boolean(true));
		ALimInclude.reset(new Boolean(true));

		listen(Faces);
		listen(Vertices);
		listen(FaceVertexCData);
	}

	public void validate()
	{
		updateMinMax();
		updateFaceCount();
		if (VertexNormals.getDim(0) == 0)
			VertexNormals.reset(computeNormals());
		super.validate();
	}

	private void updateMinMax()
	{
		double xmin, xmax, ymin, ymax, zmin, zmax, cmin, cmax;
		double xmin2, xmax2, ymin2, ymax2, zmin2, zmax2;
		double[][] v = Vertices.asDoubleMatrix();

		xmin = ymin = zmin = Double.POSITIVE_INFINITY;
		xmax = ymax = zmax = Double.NEGATIVE_INFINITY;
		xmin2 = ymin2 = zmin2 = Double.POSITIVE_INFINITY;
		xmax2 = ymax2 = zmax2 = Double.MIN_VALUE;

		if (v != null && v.length > 0)
		{
			for (int i=0; i<v.length; i++)
			{
				if (v[i][0] < xmin) xmin = v[i][0];
				if (v[i][0] > xmax) xmax = v[i][0];
				if (v[i][0] > 0)
				{
					if (v[i][0] < xmin2) xmin2 = v[i][0];
					if (v[i][0] > xmax2) xmax2 = v[i][0];
				}
				if (v[i][1] < ymin) ymin = v[i][1];
				if (v[i][1] > ymax) ymax = v[i][1];
				if (v[i][1] > 0)
				{
					if (v[i][1] < ymin2) ymin2 = v[i][1];
					if (v[i][1] > ymax2) ymax2 = v[i][1];
				}
				if (v[i][2] < zmin) zmin = v[i][2];
				if (v[i][2] > zmax) zmax = v[i][2];
				if (v[i][2] > 0)
				{
					if (v[i][2] < zmin2) zmin2 = v[i][2];
					if (v[i][2] > zmax2) zmax2 = v[i][2];
				}
			}
		}

		XLim.set(new double[] {xmin, xmax, xmin2, xmax2}, true);
		YLim.set(new double[] {ymin, ymax, ymin2, ymax2}, true);
		ZLim.set(new double[] {zmin, zmax, zmin2, zmax2}, true);

		if (FaceVertexCData.getDim(1) == 1 && CDataMapping.is("scaled"))
		{
			double[] cdata = FaceVertexCData.asDoubleVector();

			if (cdata != null && cdata.length > 0)
			{
				cmin = cmax = cdata[0];
				for (int i=1; i<cdata.length; i++)
				{
					if (cdata[i] < cmin) cmin = cdata[i];
					else if (cdata[i] > cmax) cmax = cdata[i];
				}
			}
			else
			{
				cmin = Double.POSITIVE_INFINITY;
				cmax = Double.NEGATIVE_INFINITY;
			}
		}
		else
		{
			cmin = Double.POSITIVE_INFINITY;
			cmax = Double.NEGATIVE_INFINITY;
		}

		CLim.set(new double[] {cmin, cmax}, true);
	}

	private boolean checkConsistency()
	{
		int nf = Faces.getDim(0), nv = Vertices.getDim(0), nfv = FaceVertexCData.getDim(0);

		if (nf == 0 || nv == 0)
			return false;

		if (!FaceColor.isSet() && !FaceColor.is("none"))
		{
			if (FaceColor.is("flat") && nfv != nf && nfv != nv)
			{
				System.err.println("Warning: Color data must be given per-face");
				return false;
			}
			else if (FaceColor.is("interp") && nfv != nv)
			{
				System.err.println("Warning: Color data must be given per-vertex");
				return false;
			}
		}

		if (!EdgeColor.isSet() && !EdgeColor.is("none"))
		{
			if (nfv != nv)
			{
				System.err.println("Warning: Color data must be given per-vertex");
				return false;
			}
		}

		return true;
	}

	private void updateFaceCount()
	{
		double[][] f = Faces.asDoubleMatrix();

		if (f != null)
		{
			faceCount = new int[f.length];
			for (int i=0; i<f.length; i++)
				for (int j=0; j<f[i].length; j++)
					if (Double.isNaN(f[i][j]))
						break;
					else
						faceCount[i]++;
		}
	}

	private Matrix computeNormals()
	{
		double[][] f = Faces.asDoubleMatrix();
		double[][] v = Vertices.asDoubleMatrix();

		if (f == null || v == null || f.length == 0 || v.length == 0)
			return null;

		int nv = v.length;
		double[][] n = new double[nv][3];
		double[] vCount = new double[v.length];
		int vIndex;

		for (int i=0; i<f.length; i++)
		{
			if (faceCount[i] <= 2)
				continue;
			double[] v1 = v[(int)f[i][0]-1], vp = v[(int)f[i][1]-1], vc = v[(int)f[i][2]-1];
			Utils.crossProduct(
				v1[0]-vp[0], v1[1]-vp[1], v1[2]-vp[2],
				vc[0]-vp[0], vc[1]-vp[1], vc[2]-vp[2],
				n[(int)f[i][1]-1]);
			Utils.crossProduct(
				v1[0]-vp[0], v1[1]-vp[1], v1[2]-vp[2],
				vc[0]-vp[0], vc[1]-vp[1], vc[2]-vp[2],
				n[(int)f[i][0]-1]);
			vCount[(int)f[i][0]-1]++;
			vCount[(int)f[i][1]-1]++;
			for (int j=2; j<faceCount[i]; j++)
			{
				vIndex = (int)(f[i][j]-1);
				vc = v[vIndex];
				Utils.crossProduct(
					vp[0]-vc[0], vp[1]-vc[1], vp[2]-vc[2],
					v1[0]-vc[0], v1[1]-vc[1], v1[2]-vc[2],
					n[vIndex]);
				vCount[vIndex]++;
				vp = vc;
			}
		}
		for (int i=0; i<v.length; i++)
		{
			if (vCount[i] > 0)
			{
				n[i][0] /= vCount[i];
				n[i][1] /= vCount[i];
				n[i][2] /= vCount[i];
			}
		}

		return new Matrix(n);
	}

	double[][] getCData()
	{
		if (FaceVertexCData.getDim(1) == 3)
			/* true colors */
			return FaceVertexCData.asDoubleMatrix();
		else if (FaceVertexCData.getDim(1) == 1)
			/* indexed colors */
			return getAxes().convertCData(FaceVertexCData.asDoubleVector(), CDataMapping.getValue());
		else
			return null;
	}

	double[] getAlphaData()
	{
		if (AlphaDataMapping.is("none"))
			return FaceVertexAlphaData.getArray();
		else if (AlphaDataMapping.is("direct"))
		{
			double[] amap = getAxes().getFigure().Alphamap.getArray();
			double[] aa = FaceVertexAlphaData.getArray();
			double[] res = new double[aa.length];

			for (int i=0; i<res.length; i++)
				res[i] = amap[(int)Math.min(Math.max(1, aa[i]), amap.length)-1];
			return res;
		}
		else if (AlphaDataMapping.is("scaled"))
		{
			double[] amap = getAxes().getFigure().Alphamap.getArray();
			double[] aa = FaceVertexAlphaData.getArray();
			double[] alim = getAxes().ALim.getArray();
			double[] res = new double[aa.length];

			for (int i=0; i<aa.length; i++)
			{
				double s = (aa[i]-alim[0])/(alim[1]-alim[0]);
				res[i] = amap[(int)Math.round((amap.length-1)*s)];
			}
			return res;
		}

		return null;
	}

	int[] getFaceCount()
	{
		return faceCount;
	}

	public void draw(Renderer r)
	{
		if (checkConsistency())
		{
			r.draw(this);
		}
	}

	public void propertyChanged(Property p) throws PropertyException
	{
		super.propertyChanged(p);

		if (p == Faces)
			updateFaceCount();
		else if (p == Vertices || p == FaceVertexCData)
			updateMinMax();

		if (p == Faces || p == Vertices)
			VertexNormals.set(computeNormals());
	}
}

