package maicliant.render;

import org.lwjgl.opengl.GL11;

import maicliant.gui.util.GL11Assist;
import maicliant.main.MaiWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.MathHelper;
import net.minecraft.src.Tessellator;


public class BoundingBox
{

	public static void drawOutlinedBoundingBox(AxisAlignedBB a)
	{
		double x = a.minX;
		double y = a.minY;
		double z = a.minZ;
		double x2 = a.maxX;
		double y2 = a.maxY;
		double z2 = a.maxZ;
		
	    GL11Assist.defaultsOn();
	    
	    float timechange = MathHelper.cos((float)Minecraft.getSystemTime() / 255.0F);
	    GL11.glColor4f(0f,0f,0f,0.4f); 
	    drawBox(a);
	    
	    float r,g,b;
	    r = (timechange * 0) + 0;
	    g = (timechange * 0.9f) + 0.9f;
	    b = (timechange * 1f) + 1f;
	    
	    r = checkVal(r);
	    g = checkVal(g);
	    b = checkVal(b);
	    
	    GL11.glColor4f(r,g,b,0.5f);

		double[] l1 = 
		{
			x,y,z,
			x,y,z2,
			x,y2,z2,
			x,y2,z,
			x,y,z,
		};
		
		double[] l2 = 
		{
			x2,y,z,
			x2,y,z2,
			x2,y2,z2,
			x2,y2,z,
			x2,y,z,
		};
		
		double[] l3 =
		{
			x,y,z,
			x2,y,z		
		};
		
		double[] l4 =
		{
			x,y,z2,
			x2,y,z2
		};
		
		double[] l5 =
		{
			x,y2,z,
			x2,y2,z		
		};
		
		double[] l6 =
		{
			x,y2,z2,
			x2,y2,z2
		};
		
			float l =(timechange*2.0f)+2.0f;
			l = checkVal(l)+0.5f;
			GL11Assist.drawLines3d(l1, l);
			GL11Assist.drawLines3d(l2, l);
			GL11Assist.drawLines3d(l3, l);
			GL11Assist.drawLines3d(l4, l);
			GL11Assist.drawLines3d(l5, l);
			GL11Assist.drawLines3d(l6, l);
	
	   GL11Assist.defaultsOff();
	}
	
	private static float checkVal(float f)
	{
		if(f >= 255)return 254f;
		if(f <= 0)return 0.1f;
		return f;
	}
	
	private static float checkValReturnSelf(float f, float f1)
	{
		float f2 = f;
		if(f > 255 || f < 0.05f)f2 = f > 255? 254f:f1;
		return f2;
	}

	public static void drawStaticOutlinedBoundingBox(AxisAlignedBB a)
	{
		 int c = 0x80FF4D00;
		 GL11Assist.defaultsOn();
		 GL11Assist.setColor4F(c);
		 drawBox(a);
		 GL11Assist.defaultsOff();
	}
	
	public static void drawSearchSelectionBox(AxisAlignedBB a, boolean cansee)
	{
	    	double x = a.minX;
	    	double y = a.minY;
	    	double z = a.minZ;
	    	
	    	double x2 = a.maxX;
	    	double y2 = a.maxY;
	    	double z2 = a.maxZ;
	    	
	       GL11Assist.defaultsOn();
	       if(!cansee)GL11.glDisable(GL11.GL_DEPTH_TEST);
			
	       float timechange = MathHelper.sin((float)Minecraft.getSystemTime() / 255.0F);
	       int c = 0xFF2E0151;
	       float[] c1 = GL11Assist.getRGBA(c);
	       float rh = c1[0];
	       float gh = c1[1];
	       float bh = c1[2];
	       float r,g,b;
	       r = (timechange * (0.1f+rh)) + rh;
	       g = (timechange * gh) + gh;
	       b = (timechange * (0.8f+bh)) + bh;
	       r = checkValReturnSelf(r,rh);
	       g = checkValReturnSelf(g,gh);
	       b = checkValReturnSelf(b,bh);
	       GL11.glColor4f(r,g,b,0.4F); 
	       drawBox(a);
	       c = 0xFF33008A;
	       c1 = GL11Assist.getRGBA(c);
		   r = (timechange * rh) + rh;
		   g = (timechange * gh) + gh;
		   b = (timechange * bh) + bh;
		   r = checkVal(r);
		   g = checkVal(g);
		   b = checkVal(b);
		   GL11.glColor4f(r,g,b,0.6f);

			double[] l1 = 
			{
				x,y,z,
				x,y,z2,
				x,y2,z2,
				x,y2,z,
				x,y,z,
			};
			
			double[] l2 = 
			{
				x2,y,z,
				x2,y,z2,
				x2,y2,z2,
				x2,y2,z,
				x2,y,z,
			};
			
			double[] l3 =
			{
				x,y,z,
				x2,y,z		
			};
			
			double[] l4 =
			{
				x,y,z2,
				x2,y,z2
			};
			
			double[] l5 =
			{
				x,y2,z,
				x2,y2,z		
			};
			
			double[] l6 =
			{
				x,y2,z2,
				x2,y2,z2
			};

			GL11Assist.drawLines3D(l1,2.0f);
			GL11Assist.drawLines3D(l2,2.0f);
			GL11Assist.drawLines3D(l3,2.0f);
			GL11Assist.drawLines3D(l4,2.0f);
			GL11Assist.drawLines3D(l5,2.0f);
			GL11Assist.drawLines3D(l6,2.0f);
			if(!cansee)GL11.glEnable(GL11.GL_DEPTH_TEST);	
			GL11Assist.defaultsOff();
	}
	
	public static void drawEspBox(AxisAlignedBB a, int lineColor, int colorTop, int colorBot){
		double x = a.minX;
		double y = a.minY;
		double z = a.minZ;
		
		double x2 = a.maxX;
		double y2 = a.maxY;
		double z2 = a.maxZ;
		GL11Assist.defaultsOn();
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glDisable(GL11.GL_LIGHTING);
		int lightmap = MaiWrapper.getMc().entityRenderer.lightmapTexture;
		MaiWrapper.getMc().entityRenderer.disableLightmap(0);
		drawGradientBox( a, colorTop, colorBot);
		double[] l1 = 
			{
				x,y,z,
				x,y,z2,
				x,y2,z2,
				x,y2,z,
				x,y,z,
			};
			
			double[] l2 = 
			{
				x2,y,z,
				x2,y,z2,
				x2,y2,z2,
				x2,y2,z,
				x2,y,z,
			};
			
			double[] l3 =
			{
				x,y,z,
				x2,y,z		
			};
			
			double[] l4 =
			{
				x,y,z2,
				x2,y,z2
			};
			
			double[] l5 =
			{
				x,y2,z,
				x2,y2,z		
			};
			
			double[] l6 =
			{
				x,y2,z2,
				x2,y2,z2
			};
			GL11Assist.setColor4F(lineColor);
			GL11Assist.drawLines3D(l1,2.0f);
			GL11Assist.drawLines3D(l2,2.0f);
			GL11Assist.drawLines3D(l3,2.0f);
			GL11Assist.drawLines3D(l4,2.0f);
			GL11Assist.drawLines3D(l5,2.0f);
			GL11Assist.drawLines3D(l6,2.0f);
			GL11.glShadeModel(GL11.GL_FLAT);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glEnable(GL11.GL_LIGHTING);
			MaiWrapper.getMc().entityRenderer.enableLightmap(0);
			GL11Assist.defaultsOff();
	}
	
	private static void drawGradientBox(AxisAlignedBB a, int colorTop, int colorBot)
	{
		double x = a.minX;
		double y = a.minY;
		double z = a.minZ;
		
		double x2 = a.maxX;
		double y2 = a.maxY;
		double z2 = a.maxZ;
		Tessellator t = Tessellator.instance;

		float[]
		rgba1 = GL11Assist.getRGBA(colorTop),
		rgba2 = GL11Assist.getRGBA(colorBot);
		
		//Northern Face
		t.startDrawingQuads();
		t.setColorRGBA_F(rgba1[0], rgba1[1], rgba1[2], rgba1[3]);
		t.addVertex(x2, y, z);
		t.addVertex(x, y, z);
		t.setColorRGBA_F(rgba2[0], rgba2[1], rgba2[2], rgba2[3]);
		t.addVertex(x, y2, z);
		t.addVertex(x2, y2, z);
		t.draw();
		
		//Southern Face
		t.startDrawingQuads();
		t.setColorRGBA_F(rgba1[0], rgba1[1], rgba1[2], rgba1[3]);
		t.addVertex(x, y, z2);
		t.addVertex(x2, y, z2);
		t.setColorRGBA_F(rgba2[0], rgba2[1], rgba2[2], rgba2[3]);
		t.addVertex(x2, y2, z2);
		t.addVertex(x, y2, z2);
		t.draw();
		
		//Western Face
		t.startDrawingQuads();
		t.setColorRGBA_F(rgba1[0], rgba1[1], rgba1[2], rgba1[3]);
		t.addVertex(x, y, z);
		t.addVertex(x, y, z2);
		t.setColorRGBA_F(rgba2[0], rgba2[1], rgba2[2], rgba2[3]);
		t.addVertex(x, y2, z2);
		t.addVertex(x, y2, z);
		t.draw();
		
		//Eastern Face
		t.startDrawingQuads();
		t.setColorRGBA_F(rgba1[0], rgba1[1], rgba1[2], rgba1[3]);
		t.addVertex(x2, y, z2);
		t.addVertex(x2, y, z);
		t.setColorRGBA_F(rgba2[0], rgba2[1], rgba2[2], rgba2[3]);
		t.addVertex(x2, y2, z);
		t.addVertex(x2, y2, z2);
		t.draw();
		
		//Top Face
		t.startDrawingQuads();
		t.setColorRGBA_F(rgba2[0], rgba2[1], rgba2[2], rgba2[3]);
		t.addVertex(x, y2, z);
		t.addVertex(x, y2, z2);
		t.addVertex(x2, y2, z2);
		t.addVertex(x2, y2, z);
		t.draw();
		
		//Bottom Face
		t.startDrawingQuads();
		t.setColorRGBA_F(rgba1[0], rgba1[1], rgba1[2], rgba1[3]);
		t.addVertex(x, y, z);
		t.addVertex(x2, y, z);
		t.addVertex(x2, y, z2);
		t.addVertex(x, y, z2);
		t.draw();

	}
	
	private static void drawBox(AxisAlignedBB a)
	{
		double x = a.minX;
		double y = a.minY;
		double z = a.minZ;
		
		double x2 = a.maxX;
		double y2 = a.maxY;
		double z2 = a.maxZ;
		Tessellator t = Tessellator.instance;
	    
		//Northern Face
		t.startDrawingQuads();
		t.addVertex(x, y, z);
		t.addVertex(x, y2, z);
		t.addVertex(x2, y2, z);
		t.addVertex(x2, y, z);
		t.draw();
		
		//Southern Face
		t.startDrawingQuads();
		t.addVertex(x2, y, z2);
		t.addVertex(x2, y2, z2);
		t.addVertex(x, y2, z2);
		t.addVertex(x, y, z2);
		t.draw();
		
		//Western Face
		t.startDrawingQuads();
		t.addVertex(x, y, z2);
		t.addVertex(x, y2, z2);
		t.addVertex(x, y2, z);
		t.addVertex(x, y, z);
		t.draw();
		
		//Eastern Face
		t.startDrawingQuads();
		t.addVertex(x2, y, z);
		t.addVertex(x2, y2, z);
		t.addVertex(x2, y2, z2);
		t.addVertex(x2, y, z2);
		t.draw();
		
		//Top Face
		t.startDrawingQuads();
		t.addVertex(x, y2, z);
		t.addVertex(x, y2, z2);
		t.addVertex(x2, y2, z2);
		t.addVertex(x2, y2, z);
		t.draw();
		
		//Bottom Face
		t.startDrawingQuads();
		t.addVertex(x, y, z);
		t.addVertex(x2, y, z);
		t.addVertex(x2, y, z2);
		t.addVertex(x, y, z2);
		t.draw();
	}

}

