package slimevoid.infection.blocks;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.tan;
import static java.lang.Math.toRadians;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_FRONT;
import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11.GL_LEQUAL;
import static org.lwjgl.opengl.GL11.GL_LIGHTING;
import static org.lwjgl.opengl.GL11.GL_LINE;
import static org.lwjgl.opengl.GL11.GL_LINE_SMOOTH;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_SMOOTH;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_COLOR;
import static org.lwjgl.opengl.GL11.GL_TEXTURE;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_FAN;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glDepthFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glNormal3d;
import static org.lwjgl.opengl.GL11.glNormal3f;
import static org.lwjgl.opengl.GL11.glPolygonMode;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScaled;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glShadeModel;
import static org.lwjgl.opengl.GL11.glTexCoord2d;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glTranslated;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glVertex3d;
import static org.lwjgl.opengl.GL11.glVertex3f;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.src.ModLoader;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public class RenderSpawnBlock extends TileEntitySpecialRenderer {

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float frameDelta) {
		buildProgress = System.currentTimeMillis() % 6000 / 5000F;
		buildProgress = 1;
		if(buildProgress > 1) buildProgress = 1;
		glPushMatrix();
		glTranslated(x + .5, y, z + .5);
		glPushMatrix();
		double barsShift = .3;
		float barsRot = 15;
		for(int i = 0; i < 3; i ++) {
			glRotatef(120, 0, 1, 0);
			glTranslated(barsShift, buildProgress * .5 - .5, 0);
			glRotatef(+barsRot, 0, 0, 1);
			renderBar();
			glRotatef(-barsRot, 0, 0, 1);
			glTranslated(-barsShift,  -(buildProgress * .5 - .5), 0);
		}
		glPopMatrix();
		renderBaseHalo(barsShift, barsRot);
		renderGem();
		glPopMatrix();
	}
	
	private void renderBaseHalo(double barsShift, float barsRot) {
		glDisable(GL_CULL_FACE);
        glEnable(GL_BLEND);
        
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_TEXTURE_2D);
        drawBaseHalo(barsShift, barsRot);
        glEnable(GL_TEXTURE_2D);
        
        glDisable(GL_BLEND);
        glDepthFunc(GL_LEQUAL);
		
		glEnable(GL_CULL_FACE);
	}

	private void drawBaseHalo(double barsShift, float barsRot) {
		RenderHelper.enableStandardItemLighting();
		glShadeModel(GL_SMOOTH);
		glBegin(GL_TRIANGLE_FAN);
			double sY = sin(toRadians(90 - barsRot)) * .5 - (barsShift - cos(toRadians(90 - barsRot)) * .5) / tan(toRadians(90 - barsRot));
			glTexCoord2d(0, 0);
			glColor4f(1, 0, 0, .1F);
			glVertex3d(0, sY, 0);
			int subs = 48;
			double r = .3;
			glColor4f(.3F, .3F, .3F, .5F);
			for(int i = 0; i < subs; i ++) {
				double a = -i * PI * 2 / (subs - 1);
				double x = cos(a) * r;
				double z = sin(a) * r;
				double nw = cos(toRadians(barsRot));
				double nx = nw * cos(a);
				double nz = nw * sin(a);
				double ny = sin(toRadians(barsRot));
				glNormal3d(nx, ny, nz);
				glTexCoord2d(cos(a), sin(a));
				glVertex3d(x, sY + r * tan(toRadians(barsRot)), z);
			}
		glEnd();
	}

	private void renderBar() {
		glDisable(GL_LIGHTING);
		Minecraft mc = ModLoader.getMinecraftInstance();
		float f  = 1 / 16F;
		float f1 = 8 / 16F;
		Vector3f p0 = new Vector3f(-f, 0, -f);
		Vector3f p1 = new Vector3f(+f, 0, -f);
		Vector3f p2 = new Vector3f(+f, 0, +f);
		Vector3f p3 = new Vector3f(-f, 0, +f);
		Vector3f p4 = new Vector3f(-f, f1, -f);
		Vector3f p5 = new Vector3f(+f, f1, -f);
		Vector3f p6 = new Vector3f(+f, f1, +f);
		Vector3f p7 = new Vector3f(-f, f1, +f);
		
		int tex = Block.stoneBrick.getBlockTextureFromSideAndMetadata(0, 3);
		int u = tex % 16;
		int v = tex / 16;
		
		float f2 = .5F - f;
		float f3 = .5F + f;
		Vector2f t0 = new Vector2f((u + f2) / 16F, (v + f2) / 16F);
		Vector2f t1 = new Vector2f((u + f3) / 16F, (v + f2) / 16F);
		Vector2f t2 = new Vector2f((u + f3) / 16F, (v + f3) / 16F);
		Vector2f t3 = new Vector2f((u + f2) / 16F, (v + f3) / 16F);
		float f4 = 0;
		float f5 = f1;
		Vector2f t4 = new Vector2f((u + f2) / 16F, (v + f4) / 16F);
		Vector2f t5 = new Vector2f((u + f2) / 16F, (v + f5) / 16F);
		Vector2f t6 = new Vector2f((u + f3) / 16F, (v + f5) / 16F);
		Vector2f t7 = new Vector2f((u + f3) / 16F, (v + f4) / 16F);
		
		float f6 = (f * 2) / 16F;
		
		mc.renderEngine.bindTexture(mc.renderEngine.getTexture("/terrain.png"));
		glBegin(GL_QUADS);
			texCord(t0); addVert(p0);
			texCord(t1); addVert(p1);
			texCord(t2); addVert(p2);
			texCord(t3); addVert(p3);
			
			texCord(t4); addVert(p0);
			texCord(t5); addVert(p4);
			texCord(t6); addVert(p5);
			texCord(t7); addVert(p1);
			
			t4.translate(f6, 0);
			t5.translate(f6, 0);
			t6.translate(f6, 0);
			t7.translate(f6, 0);
			
			texCord(t4); addVert(p1);
			texCord(t5); addVert(p5);
			texCord(t6); addVert(p6);
			texCord(t7); addVert(p2);
			
			t4.translate(f6, 0);
			t5.translate(f6, 0);
			t6.translate(f6, 0);
			t7.translate(f6, 0);
			
			texCord(t4); addVert(p2);
			texCord(t5); addVert(p6);
			texCord(t6); addVert(p7);
			texCord(t7); addVert(p3);
			
			t4.translate(f6, 0);
			t5.translate(f6, 0);
			t6.translate(f6, 0);
			t7.translate(f6, 0);
			
			texCord(t4); addVert(p3);
			texCord(t5); addVert(p7);
			texCord(t6); addVert(p4);
			texCord(t7); addVert(p0);
			
			texCord(t0); addVert(p7);
			texCord(t1); addVert(p6);
			texCord(t2); addVert(p5);
			texCord(t3); addVert(p4);
		glEnd();
	}
	
	private void renderGem() {
		glPushMatrix();
		
		mc.renderEngine.bindTexture(mc.renderEngine.getTexture("/rubis.png"));
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glColor4f(.8F, .8F, .8F, 1F);
		double yMove = sin((System.currentTimeMillis() % (PI * 2000)) / (1000)) * -.075 + .1;
		glTranslated(0, yMove, 0);
		RenderHelper.enableStandardItemLighting();
		glRotatef(360 * (System.currentTimeMillis() % 8000) / 8000F, 0, 1, 0);
		drawGem(false);
		
		glPushMatrix();
			glEnable(GL_LINE_SMOOTH);
			glPolygonMode(GL_FRONT, GL_LINE);
			glDisable(GL_BLEND);
			glColor4f(0.4f, 0.2f, 0.2f, 0.4f);
			glLineWidth(1.2f);
			glScalef(1.001f, 1.001f, 1.001f);
			drawGem(false);
			glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
			glEnable(GL_BLEND);
		glPopMatrix();
		
		glDisable(GL_LIGHTING);
		glEnable(GL_TEXTURE_2D);
		
		glTranslated(0, h0, 0);
		glScaled(1.1, 1.1, 1.1);
		glTranslated(0, -h0, 0);
		
		mc.renderEngine.bindTexture(mc.renderEngine.getTexture("%blur%/misc/glint.png"));
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_COLOR, GL_ONE);
        
        float light = 1F;
        glColor4f(1F * light, 0.5F * light, .5F * light, .8F);
        
        glMatrixMode(GL_TEXTURE);
        glPushMatrix();
	        float scale = .1F;
	        glScalef(scale, scale, scale);
	        float f9 = ((System.currentTimeMillis() % 3000L) / 3000F) * 8F;
	        glTranslatef(f9, 0.0F, 0.0F);
	        glRotatef(-50F, 0.0F, 0.0F, 1.0F);
	        glMatrixMode(GL_MODELVIEW);
	        drawGem(true);
	        glMatrixMode(GL_TEXTURE);
        glPopMatrix();
        
        glPushMatrix();
	        glScalef(scale, scale, scale);
	        f9 = ((System.currentTimeMillis() % 4873L) / 4873F) * 8F;
	        glTranslatef(-f9, 0.0F, 0.0F);
	        glRotatef(10F, 0.0F, 0.0F, 1.0F);
	        glMatrixMode(GL_MODELVIEW);
	        drawGem(true);
	        glMatrixMode(GL_TEXTURE);
        glPopMatrix();
        
        glMatrixMode(GL_MODELVIEW);
        glDisable(GL_BLEND);
        glDepthFunc(GL_LEQUAL);
        
        glPopMatrix();
	}
	
	private void drawGem(boolean halo) {
		Vector3f p0 = new Vector3f(0, h, 0);
		Vector3f p1 = new Vector3f(-w0, h0, -w0);
		Vector3f p2 = new Vector3f(+w0, h0, -w0);
		Vector3f p3 = new Vector3f(+w0, h0, +w0);
		Vector3f p4 = new Vector3f(-w0, h0, +w0);
		
		Vector3f p5 = new Vector3f(-w1, h1, -w1);
		Vector3f p6 = new Vector3f(+w1, h1, -w1);
		Vector3f p7 = new Vector3f(+w1, h1, +w1);
		Vector3f p8 = new Vector3f(-w1, h1, +w1);
		
		float f0 = .15F / .7F;
		float f1 = .55F / .7F;
		
		Vector2f t0 = new Vector2f(.5F, .5F);
		Vector2f t5 = new Vector2f(f1, f0);
		Vector2f t6 = new Vector2f(f1, f1);
		Vector2f t7 = new Vector2f(f0, f1);
		Vector2f t8 = new Vector2f(f0, f0);
		Vector2f t1 = new Vector2f(1, 0);
		Vector2f t2 = new Vector2f(1, 1);
		Vector2f t3 = new Vector2f(0, 1);
		Vector2f t4 = new Vector2f(0, 0);
		
		glBegin(GL_TRIANGLES);
			glNormal3f(0, -ny0, -nx0);
			texCord(t0); addVert(p0);
			texCord(t1); addVert(p1);
			texCord(t2); addVert(p2);
			glNormal3f(+nx0, -ny0, 0);
			texCord(t0); addVert(p0);
			texCord(t2); addVert(p2);
			texCord(t3); addVert(p3);
			glNormal3f(0, -ny0, +nx0);
			texCord(t0); addVert(p0);
			texCord(t3); addVert(p3);
			texCord(t4); addVert(p4);
			glNormal3f(-nx0, -ny0, 0);
			texCord(t0); addVert(p0);
			texCord(t4); addVert(p4);
			texCord(t1); addVert(p1);
		glEnd();
		glBegin(GL_QUADS);
			glNormal3f(0, +ny1, -nx1);
			texCord(t1); addVert(p1);
			texCord(t5); addVert(p5);
			texCord(t6); addVert(p6);
			texCord(t2); addVert(p2);
			
			glNormal3f(+nx1, +ny1, 0);
			texCord(t2); addVert(p2);
			texCord(t6); addVert(p6);
			texCord(t7); addVert(p7);
			texCord(t3); addVert(p3);
			
			glNormal3f(0, +ny1, +nx1);
			texCord(t3); addVert(p3);
			texCord(t7); addVert(p7);
			texCord(t8); addVert(p8);
			texCord(t4); addVert(p4);
			
			glNormal3f(-nx1, +ny1, 0);
			texCord(t4); addVert(p4);
			texCord(t8); addVert(p8);
			texCord(t5); addVert(p5);
			texCord(t1); addVert(p1);
			
			glNormal3f(0, 1, 0);
			texCord(t8); addVert(p8);
			texCord(t7); addVert(p7);
			texCord(t6); addVert(p6);
			texCord(t5); addVert(p5);
		glEnd();
	}
	
	private void addVert(Vector3f vec) {
		glVertex3f(vec.x, vec.y, vec.z);
	}
	
	private void texCord(Vector2f vec) {
		glTexCoord2f(vec.x, vec.y);
	}
	
	private final float h = .5F;
	private final float w0 = .35F;
	private final float h0 = .85F;
	private final float nx0 = (h0 - h) / w0;
	private final float ny0 = w0 / (h0 - h);
	private final float w1 = .2F;
	private final float h1 = 1;
	private final float nx1 = (h1 - h0) / (w0 - w1);
	private final float ny1 = (w0 - w1) / (h1 - h0);
	
	private float buildProgress;
	
	private final Minecraft mc = ModLoader.getMinecraftInstance();
}
