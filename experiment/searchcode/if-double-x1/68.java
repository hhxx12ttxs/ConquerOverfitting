package net.minecraft.src;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class PhysClothRender extends Render{
	
		static float x1 = 0.0F;
		static float x2 = 0.5F;
		static float y1 = 0.0F;
		static float y2 = 0.125F;
		
		static float sunAngle;
		//moon 0.25-0.75
		//sun 0.75- 0.25
		static PhysVector sunRay;
		
		static final Tessellator tessellator = Tessellator.instance;

		int count;
		public void renderCloth(PhysClothEntity cloth, double par2, double par4, double par6, float par9)
		{
			sunAngle = cloth.worldObj.getCelestialAngle(par9);
			sunRay = new PhysVector(Math.sin(sunAngle), Math.cos(sunAngle), 0);
			loadTexture("/item/cloth.png");
			GL11.glPushMatrix();
			
	        double rX = cloth.prevPosX + (cloth.posX - cloth.prevPosX) * (double)par9;
	        double rY = cloth.prevPosY + (cloth.posY - cloth.prevPosY) * (double)par9;
	        double rZ = cloth.prevPosZ + (cloth.posZ - cloth.prevPosZ) * (double)par9;
	        
			GL11.glTranslatef((float)(par2 - rX), (float)(par4 - rY), (float)(par6 - rZ));

			GL11.glEnable(GL11.GL_CULL_FACE);
			ArrayList<PhysClothPatch> patches = cloth.patches;
			tessellator.startDrawingQuads();

			GL11.glNormal3f(0.0F, 0.0F, 1F);
			for(int i = 0; i < patches.size(); i++)
			{
				count++;
				renderPatch(patches.get(i));
				if(count == 40)
				{
					tessellator.draw();
					tessellator.startDrawingQuads();
					count = 0;
				}
			}
			tessellator.draw();

			GL11.glPopMatrix();
		}
		
		public void renderPatch(PhysClothPatch patch)
		{
			PhysVector a = patch.bottomLeft.pos;
			PhysVector b = patch.topLeft.pos;
			PhysVector c = patch.topRight.pos;
			PhysVector d = patch.bottomRight.pos;
			
			PhysVector diag1 = c.minus(a);
			PhysVector diag2 = b.minus(d);
			PhysVector norm = diag1.cross(diag2);
			
			int bMult = (int)(255*(0.7 + Math.abs(0.3*norm.dot(sunRay)/(norm.length()*sunRay.length()))));
			
			tessellator.setColorRGBA(bMult, bMult, bMult, 255);
			
			double X1 = x1 + patch.colorIndexX*0.5F;
			double X2 = x2 + patch.colorIndexX*0.5F;
			double Y1 = y1 + patch.colorIndexY*0.125F;
			double Y2 = y2 + patch.colorIndexY*0.125F;
			
			tessellator.addVertexWithUV(a.X, a.Y, a.Z, X1, Y2);
			tessellator.addVertexWithUV(b.X, b.Y, b.Z, X1, Y1);
			tessellator.addVertexWithUV(c.X, c.Y, c.Z, X2, Y1);
			tessellator.addVertexWithUV(d.X, d.Y, d.Z, X2, Y2);
			
			tessellator.addVertexWithUV(d.X, d.Y, d.Z, X2, Y2);
			tessellator.addVertexWithUV(c.X, c.Y, c.Z, X2, Y1);
			tessellator.addVertexWithUV(b.X, b.Y, b.Z, X1, Y1);
			tessellator.addVertexWithUV(a.X, a.Y, a.Z, X1, Y2);
		}
		
		public void doRender(Entity par1Entity, double par2, double par4, double par6, float par8, float par9)
	    {
	        renderCloth((PhysClothEntity)par1Entity, par2, par4, par6, par9);
	    }

}

