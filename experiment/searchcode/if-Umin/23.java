package net.firesquaredcore.client.gui.helper;

import net.firesquaredcore.helper.Helper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

public class TexturedQuadDrawer implements IQuadDrawer
{
	private boolean hasWarned = false;
	
	float zLevel = 0;
	ResourceLocation texture;
	final Tessellator tessellator = Tessellator.instance;
	final TextureManager texMan = Minecraft.getMinecraft().getTextureManager();
	static final float scale = 0.00390625F;
	public final float u0,v0,u1,v1,u2,v2,u3,v3;
	public int width, height;
	public TexturedQuadDrawer(ResourceLocation texture, float uMin, float uMax, float vMin, float vMax)
	{
		u3 = u0 = uMin * scale;
		v1 = v0 = vMax * scale;
		u1 = u2 = uMax * scale;
		v2 = v3 = vMin * scale;
		this.texture = texture;
	}
	public TexturedQuadDrawer(ResourceLocation texture, float uMin, float uMax, float vMin, float vMax, int width, int height)
	{
		this(texture, uMin, uMax, vMin, vMax);
		this.width = width;
		this.height = height;
	}
	public TexturedQuadDrawer(ResourceLocation texture, Vector2f uv0, Vector2f uv1, Vector2f uv2, Vector2f uv3)
	{
		u0 = uv0.x;
		v0 = uv0.y;
		u1 = uv1.x;
		v1 = uv1.y;
		u2 = uv2.x;
		v2 = uv2.y;
		u3 = uv3.x;
		v3 = uv3.y;
		this.texture = texture;
	}

	@Override
	public void draw(int x,int y)
	{
		draw(x, y, width, height);
	}

	@Override
	public void draw(int x, int y, int width, int height)
	{
		if (texture == null && !hasWarned)
		{
			Helper.getLogger().error("Missing texture!");
			hasWarned = true;
		}
		texMan.bindTexture(texture);
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(x, 			y + height,     zLevel, u0, v0);
		tessellator.addVertexWithUV(x + width,	y + height,     zLevel, u1, v1);
		tessellator.addVertexWithUV(x + width,  y,              zLevel, u2, v2);
		tessellator.addVertexWithUV(x,          y,              zLevel, u3, v3);
		tessellator.draw();
	}

	@Override
	public void draw(int x, int y, int width, int height, float rotation)
	{
		if (texture == null && !hasWarned)
		{
			Helper.getLogger().error("Missing texture!");
			hasWarned = true;
		}
		texMan.bindTexture(texture);
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(x, 			y + height,     zLevel, u0, v0);
		tessellator.addVertexWithUV(x + width,	y + height,     zLevel, u1, v1);
		tessellator.addVertexWithUV(x + width,  y,              zLevel, u2, v2);
		tessellator.addVertexWithUV(x,          y,              zLevel, u3, v3);
		GL11.glRotatef(rotation, 0, 0, 1);
		tessellator.draw();
		GL11.glRotatef(-rotation, 0, 0, 1);
	}

	@Override
	public float getZLayer()
	{
		return zLevel;
	}

	@Override
	public TexturedQuadDrawer setWH(int width, int height)
	{
		this.width = width;
		this.height = height;
		return this;
	}

	@Override
	public IQuadDrawer setZLayer(float z)
	{
		zLevel = z;
		return this;
	}
	@Override
	public int getWidth()
	{
		return width;
	}
	@Override
	public int getHeight()
	{
		return height;
	}
}

