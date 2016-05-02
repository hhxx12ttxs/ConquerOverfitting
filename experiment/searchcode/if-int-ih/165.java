package plugins.adufour.activemeshes.producers;

import icy.sequence.Sequence;
import icy.type.collection.array.Array1DUtil;

import javax.vecmath.Point3d;

import plugins.adufour.activemeshes.mesh.Mesh;
import plugins.adufour.activemeshes.mesh.MeshException;
import plugins.adufour.connectedcomponents.ConnectedComponent;

public class MarchingTetrahedra extends MeshProducer
{
	private final double[]	data1D;
	private final int		width, height, depth;
	private final double	isoValue;
	private final int		gridSize;
	private final Point3d	imageResolution;
	
	public MarchingTetrahedra(Sequence s, int t, int c, double isoValue, double gridSizeInVoxels, Point3d imageResolution, boolean useVTK)
	{
		super(new Mesh(0, 0, gridSizeInVoxels * imageResolution.x, useVTK));
		
		// get a copy of the volume to triangulate
		Object raw = s.getDataCopyXYZ(t, c);
		this.data1D = Array1DUtil.arrayToDoubleArray(raw, false);
		this.width = s.getSizeX();
		this.height = s.getSizeY();
		this.depth = s.getSizeZ();
		this.isoValue = isoValue;
		this.gridSize = (int) Math.round(gridSizeInVoxels);
		this.imageResolution = imageResolution;
	}
	
	public MarchingTetrahedra(ConnectedComponent object, double gridSizeInVoxels, Point3d imageResolution, boolean useVTK)
	{
		super(new Mesh(0, 0, gridSizeInVoxels * imageResolution.x, useVTK));
		
		// get a copy of the volume to triangulate
		Sequence s = object.toSequence(); // FIXME SLOW IN MULTI-THREAD DUE TO PERSISTANCE !
		this.data1D = Array1DUtil.arrayToDoubleArray(s.getDataCopyXYZ(0, 0), false);
		this.width = s.getSizeX();
		this.height = s.getSizeY();
		this.depth = s.getSizeZ();
		this.isoValue = 0.5;
		this.gridSize = (int) Math.round(gridSizeInVoxels);
		this.imageResolution = imageResolution;
	}
	
	@Override
	public void run()
	{
	    mesh.topology.beginUpdate(false);
	    
		// pre-calculate some stuff
		int sliceSize = width * height;
		
		int gridSizeX = gridSize;
		int gridSizeY = gridSize;
		int gridSizeZ = Math.max(1, (int) Math.round(gridSize * imageResolution.x / imageResolution.z));
		
		double gridWidth = imageResolution.x * gridSizeX;
		double gridHeight = imageResolution.y * gridSizeY;
		double gridDepth = imageResolution.z * gridSizeZ;
		
		int gridWidthOffset = gridSizeX;
		int gridHeightOffset = gridSizeY * width;
		int gridDepthOffset = gridSizeZ * sliceSize;
		
		// erase first slices in all directions to ensure closed contours
		
		// along X
		for (int o = sliceSize; o < data1D.length; o += sliceSize)
			java.util.Arrays.fill(data1D, o, o + width, 0.0);
		
		// along Y
		for (int o = 0; o < data1D.length; o += width)
			data1D[o] = 0.0;
		
		// along Z
		java.util.Arrays.fill(data1D, 0, sliceSize, 0.0);
		
		int i, j, k;
		int iA, iB, iC, iD, iE, iF, iG, iH;
		Point3d vA = new Point3d();
		Point3d vB = new Point3d();
		Point3d vC = new Point3d();
		Point3d vD = new Point3d();
		Point3d vE = new Point3d();
		Point3d vF = new Point3d();
		Point3d vG = new Point3d();
		Point3d vH = new Point3d();
		double valA, valB, valC, valD, valE, valF, valG, valH;
		boolean outInX, outInY, outInZ;
		
		for (k = 0; k < depth; k += gridSizeZ)
		{
			outInZ = (k >= depth - gridSizeZ);
			
			vA.z = vB.z = vE.z = vF.z = gridDepth * (k / gridSizeZ);
			vC.z = vD.z = vG.z = vH.z = vA.z + gridDepth;
			
			for (j = 0; j < height; j += gridSizeY)
			{
				outInY = (j >= height - gridSizeY);
				
				vA.y = vB.y = vC.y = vD.y = gridHeight * (j / gridSizeY);
				vE.y = vF.y = vG.y = vH.y = vA.y + gridHeight;
				
				for (i = 0; i < width; i += gridSizeX)
				{
					outInX = (i >= width - gridSizeX);
					
					vA.x = vE.x = vH.x = vD.x = gridWidth * (i / gridSizeX);
					vB.x = vF.x = vG.x = vC.x = vA.x + gridWidth;
					
					iA = i + j * width + k * sliceSize;
					iB = iA + gridWidthOffset;
					iC = iB + gridDepthOffset;
					iD = iA + gridDepthOffset;
					iE = iA + gridHeightOffset;
					iF = iE + gridWidthOffset;
					iG = iF + gridDepthOffset;
					iH = iE + gridDepthOffset;
					
					valA = data1D[iA];
					valB = outInX ? 0 : data1D[iB];
					valC = outInX || outInZ ? 0 : data1D[iC];
					valD = outInZ ? 0 : data1D[iD];
					valE = outInY ? 0 : data1D[iE];
					valF = outInX || outInY ? 0 : data1D[iF];
					valG = outInX || outInY || outInZ ? 0 : data1D[iG];
					valH = outInY || outInZ ? 0 : data1D[iH];
					
					double sum = valA + valB + valC + valD + valE + valF + valG + valH;
					if (sum == 0 || sum == 8) continue;
					
					// extract the triangles from all 6 tetrahedra in the
					// current cube
					extractiso(isoValue, vC, valC, vH, valH, vD, valD, vB, valB);
					extractiso(isoValue, vA, valA, vH, valH, vB, valB, vD, valD);
					extractiso(isoValue, vG, valG, vB, valB, vH, valH, vC, valC);
					extractiso(isoValue, vA, valA, vB, valB, vH, valH, vE, valE);
					extractiso(isoValue, vB, valB, vE, valE, vF, valF, vH, valH);
					extractiso(isoValue, vB, valB, vH, valH, vF, valF, vG, valG);
				}
			}
		}
		mesh.topology.endUpdate();
	}
	
	private void extractiso(double isoval, Point3d v1, double c1, Point3d v2, double c2, Point3d v3, double c3, Point3d v4, double c4) throws MeshException
	{
		int flag = 0;
		
		double d1, d2, d3, d4;
		
		d1 = c1 - isoval;
		d2 = c2 - isoval;
		d3 = c3 - isoval;
		d4 = c4 - isoval;
		
		if (d1 < 0.0f)
			flag |= 1;
		else if (d1 > 0.0f) flag |= 2;
		
		if (d2 < 0.0f)
			flag |= 4;
		else if (d2 > 0.0f) flag |= 8;
		
		if (d3 < 0.0f)
			flag |= 16;
		else if (d3 > 0.0f) flag |= 32;
		
		if (d4 < 0.0f)
			flag |= 64;
		else if (d4 > 0.0f) flag |= 128;
		
		switch (flag)
		{
		// isoval=c for three vertices
			case 1:
				extractiso1A(v1, v2, v3, v4);
			break;
			case 2:
				extractiso1A(v1, v2, v4, v3);
			break;
			case 4:
				extractiso1A(v2, v1, v4, v3);
			break;
			case 8:
				extractiso1A(v2, v1, v3, v4);
			break;
			case 16:
				extractiso1A(v3, v1, v2, v4);
			break;
			case 32:
				extractiso1A(v3, v1, v4, v2);
			break;
			case 64:
				extractiso1A(v4, v1, v3, v2);
			break;
			case 128:
				extractiso1A(v4, v1, v2, v3);
			break;
			
			// isoval=c for two vertices
			case 1 + 8:
				extractiso1B(v1, v2, v3, v4);
			break;
			case 2 + 4:
				extractiso1B(v1, v2, v4, v3);
			break;
			case 1 + 32:
				extractiso1B(v1, v3, v4, v2);
			break;
			case 2 + 16:
				extractiso1B(v1, v3, v2, v4);
			break;
			case 1 + 128:
				extractiso1B(v1, v4, v2, v3);
			break;
			case 2 + 64:
				extractiso1B(v1, v4, v3, v2);
			break;
			case 4 + 32:
				extractiso1B(v2, v3, v1, v4);
			break;
			case 8 + 16:
				extractiso1B(v2, v3, v4, v1);
			break;
			case 16 + 128:
				extractiso1B(v3, v4, v1, v2);
			break;
			case 32 + 64:
				extractiso1B(v3, v4, v2, v1);
			break;
			case 8 + 64:
				extractiso1B(v2, v4, v1, v3);
			break;
			case 4 + 128:
				extractiso1B(v2, v4, v3, v1);
			break;
			
			// isoval=c for one vertex
			case 4 + 32 + 64:
				extractiso1C(v4, v3, v2, v1);
			break;
			case 8 + 16 + 128:
				extractiso1C(v2, v3, v4, v1);
			break;
			case 4 + 16 + 128:
				extractiso1C(v2, v4, v3, v1);
			break;
			case 8 + 32 + 64:
				extractiso1C(v3, v4, v2, v1);
			break;
			case 8 + 16 + 64:
				extractiso1C(v3, v2, v4, v1);
			break;
			case 4 + 32 + 128:
				extractiso1C(v4, v2, v3, v1);
			break;
			case 1 + 32 + 64:
				extractiso1C(v1, v3, v4, v2);
			break;
			case 2 + 16 + 128:
				extractiso1C(v4, v3, v1, v2);
			break;
			case 1 + 16 + 128:
				extractiso1C(v3, v4, v1, v2);
			break;
			case 2 + 32 + 64:
				extractiso1C(v1, v4, v3, v2);
			break;
			case 2 + 16 + 64:
				extractiso1C(v4, v1, v3, v2);
			break;
			case 1 + 32 + 128:
				extractiso1C(v3, v1, v4, v2);
			break;
			case 1 + 8 + 64:
				extractiso1C(v4, v2, v1, v3);
			break;
			case 2 + 4 + 128:
				extractiso1C(v1, v2, v4, v3);
			break;
			case 1 + 4 + 128:
				extractiso1C(v1, v4, v2, v3);
			break;
			case 2 + 8 + 64:
				extractiso1C(v2, v4, v1, v3);
			break;
			case 2 + 4 + 64:
				extractiso1C(v2, v1, v4, v3);
			break;
			case 1 + 8 + 128:
				extractiso1C(v4, v1, v2, v3);
			break;
			case 1 + 8 + 16:
				extractiso1C(v1, v2, v3, v4);
			break;
			case 2 + 4 + 32:
				extractiso1C(v3, v2, v1, v4);
			break;
			case 1 + 4 + 32:
				extractiso1C(v2, v3, v1, v4);
			break;
			case 2 + 8 + 16:
				extractiso1C(v1, v3, v2, v4);
			break;
			case 2 + 4 + 16:
				extractiso1C(v3, v1, v2, v4);
			break;
			case 1 + 8 + 32:
				extractiso1C(v2, v1, v3, v4);
			break;
			
			// 1st case: isoval<c for one and isoval>c for other three vertices
			// 2nd case: isoval>c for one and isoval<c for other three vertices
			case 1 + 8 + 32 + 128:
				extractiso1D(v1, v2, v3, v4);
			break;
			case 2 + 4 + 16 + 64:
				extractiso1D(v1, v2, v4, v3);
			break;
			case 2 + 4 + 32 + 128:
				extractiso1D(v2, v1, v4, v3);
			break;
			case 1 + 8 + 16 + 64:
				extractiso1D(v2, v1, v3, v4);
			break;
			case 2 + 8 + 16 + 128:
				extractiso1D(v3, v1, v2, v4);
			break;
			case 1 + 4 + 32 + 64:
				extractiso1D(v3, v1, v4, v2);
			break;
			case 2 + 8 + 32 + 64:
				extractiso1D(v4, v1, v3, v2);
			break;
			case 1 + 4 + 16 + 128:
				extractiso1D(v4, v1, v2, v3);
			break;
			
			// 1st case: isoval<c for two and isoval>c for other two vertices
			// 2nd case: isoval>c for two and isoval<c for other two vertices
			case 1 + 4 + 32 + 128:
				extractiso2(v1, v2, v3, v4);
			break;
			case 2 + 8 + 16 + 64:
				extractiso2(v1, v2, v4, v3);
			break;
			case 1 + 8 + 16 + 128:
				extractiso2(v1, v3, v4, v2);
			break;
			case 2 + 4 + 32 + 64:
				extractiso2(v1, v3, v2, v4);
			break;
			case 2 + 4 + 16 + 128:
				extractiso2(v2, v3, v1, v4);
			break;
			case 1 + 8 + 32 + 64:
				extractiso2(v2, v3, v4, v1);
			break;
		}
	}
	
	private void extractiso1A(Point3d v1, Point3d v2, Point3d v3, Point3d v4) throws MeshException
	{
		int p1 = mesh.topology.addVertex(v2);
		int p2 = mesh.topology.addVertex(v3);
		int p3 = mesh.topology.addVertex(v4);
		
		mesh.topology.addFace(p1, p2, p3);
	}
	
	private void extractiso1B(Point3d v1, Point3d v2, Point3d v3, Point3d v4) throws MeshException
	{
		Point3d v = new Point3d();
		
		v.interpolate(v1, v2, 0.5);
		int p1 = mesh.topology.addVertex(v);
		
		int p2 = mesh.topology.addVertex(v3);
		
		int p3 = mesh.topology.addVertex(v4);
		
		mesh.topology.addFace(p1, p2, p3);
	}
	
	private void extractiso1C(Point3d v1, Point3d v2, Point3d v3, Point3d v4) throws MeshException
	{
		Point3d v = new Point3d();
		
		v.interpolate(v1, v2, 0.5);
		int p1 = mesh.topology.addVertex(v);
		
		v.interpolate(v2, v3, 0.5);
		int p2 = mesh.topology.addVertex(v);
		
		int p3 = mesh.topology.addVertex(v4);
		
		mesh.topology.addFace(p1, p2, p3);
	}
	
	private void extractiso1D(Point3d v1, Point3d v2, Point3d v3, Point3d v4) throws MeshException
	{
		Point3d v = new Point3d();
		
		v.interpolate(v1, v2, 0.5);
		int p1 = mesh.topology.addVertex(v);
		
		v.interpolate(v1, v3, 0.5);
		int p2 = mesh.topology.addVertex(v);
		
		v.interpolate(v1, v4, 0.5);
		int p3 = mesh.topology.addVertex(v);
		
		mesh.topology.addFace(p1, p2, p3);
	}
	
	private void extractiso2(Point3d v1, Point3d v2, Point3d v3, Point3d v4) throws MeshException
	{
		Point3d v = new Point3d();
		
		v.interpolate(v1, v3, 0.5);
		int p1 = mesh.topology.addVertex(v);
		
		v.interpolate(v1, v4, 0.5);
		int p2 = mesh.topology.addVertex(v);
		
		v.interpolate(v2, v3, 0.5);
		int p3 = mesh.topology.addVertex(v);
		
		v.interpolate(v2, v4, 0.5);
		int p4 = mesh.topology.addVertex(v);
		
		mesh.topology.addFace(p1, p2, p4);
		mesh.topology.addFace(p1, p4, p3);
	}
}

