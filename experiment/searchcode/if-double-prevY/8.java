package net.minecraft.src;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class PhysRopeRender extends Render{

	static float f = 0.0F;
	static float f1 = 1.0F;
	static final float f2 = 0;
	static final float f3 = 0.25F;
	static final double thickness = .05D;
	static final Tessellator tessellator = Tessellator.instance;
	double x = 0;
	double y = 0;
	double z = 0;
	double prevX = 0;
	double prevY = 0;
	double prevZ = 0;
	
	double ptx;
	double pty;
	double ptz;

	int count = 0;
	long total = 0;
	
	public void renderRope(PhysRopeEntity rope, double par2, double par4, double par6, float par9, PhysVector camPos)
	{
		if(rope.tail == null)
		{
			System.out.println("Warning: Undeleted Rope Rendering");
			ArrayList<Entity> r = new ArrayList<Entity>();
			r.add(rope);
			rope.worldObj.unloadEntities(r);
			return;
		}
		long a = System.nanoTime();
		loadTexture("/item/rope.png");
		GL11.glPushMatrix();
		
        double rX = rope.prevPosX + (rope.posX - rope.prevPosX) * (double)par9;
        double rY = rope.prevPosY + (rope.posY - rope.prevPosY) * (double)par9;
        double rZ = rope.prevPosZ + (rope.posZ - rope.prevPosZ) * (double)par9;
        
		GL11.glTranslatef((float)(par2 - rX), (float)(par4 - rY), (float)(par6 - rZ));

		int count = 0;
		tessellator.startDrawingQuads();

		PhysRopeJoint current = rope.tail;
		
		PhysVector startPos = current.pos;
		prevX = startPos.X;
		prevY = startPos.Y;
		prevZ = startPos.Z;

		current = current.nextJoint;
		
		while(current != null)
		{
			count++;
			renderRopeJoint(current, camPos);
			if(count == 80)
			{
				tessellator.draw();
				tessellator.startDrawingQuads();
				count = 0;
			}

			current = current.nextJoint;
		}
		tessellator.draw();
		x = 0;
		y = 0;
		z = 0;
		
		GL11.glPopMatrix();
		f = 0.0f;
		f1 = 0.0f;
	}
	
	public void renderRopeJoint(PhysRopeJoint joint, PhysVector camPos)
	{
		x = joint.pos.X;
		y = joint.pos.Y;
		z = joint.pos.Z;
		
		PhysVector segment = joint.prevJoint.pos.minus(joint.pos);
		
		PhysVector look = joint.pos.minus(camPos);

		PhysVector width = look.cross(segment);
		
		width.multiply(thickness/width.length());
		
		double tx = -width.X;
		double ty = -width.Y;
		double tz = -width.Z;
		
		double t = -joint.twist + joint.prevJoint.twist;
		if(t/(2*Math.PI) < -0.9)
		{
			f = f1 + 1.0F - 0.9f; 
		}
		else
		{
			f = f1 + 1.0F + (float)(t/(2*Math.PI)); 
		}
		
		GL11.glNormal3f(0.0F, 0.0F, 1F);

		tessellator.addVertexWithUV(-tx + x, 0 + y - ty, 0.0D + z - tz, f, f3);
		tessellator.addVertexWithUV(-ptx + prevX, prevY - pty, 0.0D + prevZ - ptz, f1, f3);
		tessellator.addVertexWithUV(ptx + prevX, prevY + pty, 0.0D + prevZ + ptz, f1, f2);
		tessellator.addVertexWithUV(tx + x, 0 + y + ty, 0.0D + z + tz, f, f2);

		prevX = x;
		prevY = y;
		prevZ = z;
		
		ptx = tx;
		pty = ty;
		ptz = tz;
		
		f1 = f;
	}
	
	public void doRender(Entity par1Entity, double par2, double par4, double par6, float par8, float par9)
    {
		EntityPlayer player = PhysManagerMC.mc().thePlayer;
		
		PhysVector offs = PhysManagerMC.cameraOffset;
		
        double cX = player.prevPosX + (player.posX - player.prevPosX) * (double)par9 - offs.X;
        double cY = player.prevPosY + (player.posY - player.prevPosY) * (double)par9 - offs.Y;
        double cZ = player.prevPosZ + (player.posZ - player.prevPosZ) * (double)par9 - offs.Z;
		
        renderRope((PhysRopeEntity)par1Entity, par2, par4, par6, par9, new PhysVector(cX, cY, cZ));
    }
}

