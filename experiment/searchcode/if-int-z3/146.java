package plia.core.scene;

import plia.core.scene.geometry.Mesh;
import plia.core.scene.shading.Heightmap;
import plia.core.scene.shading.Texture2D;

public class StaticTerrain extends Terrain
{
	private int segment;
	
	private Mesh mesh;
	
	public StaticTerrain(Texture2D heightmap, int maxHeight, int scale, int segment)
	{
		super(heightmap, maxHeight, scale);
		
		this.segment = segment;
		createMesh();
	}

	private void createMesh()
	{
		int length = (segment+1);
		int s, t;
		
		float[] vertices = new float[length*length*3];
		int[] indices = new int[segment*segment*6];
		
//		float segSize = (float)getTerrainScale() / (float)this.segment;
		
		int seg6 = segment*6;

		for (int v = 0; v < length; v++)
		{
			for (int u = 0; u < length; u++)
			{
				int index = ((v*length)+u);
				int vertex_index = index*3;
				
				float hu = (float)u / length;
				float hv = (float)v / length;

				vertices[vertex_index] = hu * getTerrainScale();
				vertices[vertex_index+1] = hv * getTerrainScale();
				
				Texture2D heightmap = getHeightmap();
				float hw = (heightmap.getWidth());
				float hh = (heightmap.getHeight());
				
				float hs = (hu * hw);
				float ht = (hv * hh);
				
				int ihS = (int) hs;
				int ihT = (int) ht;
				
				int u00 = (int) Math.min(hw, Math.max(0, ihS-1));
				int u01 = (int) Math.min(hw, Math.max(0, ihS));
//				int u02 = (int) Math.min(hw, Math.max(0, ihS+1));
				
				int v00 = (int) Math.min(hh, Math.max(0, ihT-1));
				int v01 = (int) Math.min(hh, Math.max(0, ihT));
//				int v02 = (int) Math.min(hh, Math.max(0, ihT+1));
				
				float z0 = Heightmap.getHeightFromPixel(heightmap, u00, v00);
				float z1 = Heightmap.getHeightFromPixel(heightmap, u01, v00);
//				float z2 = Heightmap.getHeightFromPixel(heightmap, u02, v00);
				
				float z3 = Heightmap.getHeightFromPixel(heightmap, u00, v01);
				float z4 = Heightmap.getHeightFromPixel(heightmap, u01, v01);
//				float z5 = Heightmap.getHeightFromPixel(heightmap, u02, v01);
//				
//				float z6 = Heightmap.getHeightFromPixel(heightmap, u00, v02);
//				float z7 = Heightmap.getHeightFromPixel(heightmap, u01, v02);
//				float z8 = Heightmap.getHeightFromPixel(heightmap, u02, v02);
				
				float h0 = (z0 + z1 + z3 + z4) / 4f;
//				float h1 = (z1 + z2 + z4 + z5) / 4f;
//				float h2 = (z4 + z5 + z7 + z8) / 4f;
//				float h3 = (z3 + z4 + z6 + z7) / 4f;
				
				vertices[vertex_index+2] = h0 * (float)getTerrainMaxHeight(); //Heightmap.getHeightFromPixel(getHeightmap(), hu, hv) * (float)getTerrainMaxHeight();

				if(v < segment && u < segment)
				{
					t = ((v+1)*(length))+u;
					s = index;

					int indices_index = ((v*seg6)+(u*6));

					indices[indices_index] = s;
					indices[indices_index+1] = t;
					indices[indices_index+2] = t+1;
					
					indices[indices_index+3] = s;
					indices[indices_index+4] = t+1;
					indices[indices_index+5] = s+1;
				}
			}
		}

		mesh = new Mesh(vertices, indices);
	}
	
	public Mesh getMesh()
	{
		return mesh;
	}
	
	public int getSegment()
	{
		return segment;
	}
}

