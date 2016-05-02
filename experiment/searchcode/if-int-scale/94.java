package de.w33ky;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

public class MyRenderer {
	private int width, height, scale;
	
	public MyRenderer(int width, int height, int scale) {
		this.width = width;
		this.height = height;
		this.scale = scale;
		
		try {
			Display.setDisplayMode(new DisplayMode(this.width*scale,this.height*scale));
			Display.create();
		} catch(LWJGLException e) {
			e.printStackTrace();
			System.exit(0);
		}		
		
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, this.width*scale, 0, this.height*scale, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
	}
	
	public void update() {
		Display.update();
	}
	
	public void destroy() {
		Display.destroy();
	}
	
	public int get_res_width() {
		return width;
	}
	
	public int get_res_height() {
		return height;
	}
	
	public void render_sprite(MySprite sprite) {
		for (int x = 0 ; x < sprite.get_width(); x++) {
			for (int y = 0; y < sprite.get_height(); y++) {
				if (sprite.get_value(x, y) > -1) {
					float c = (float)sprite.get_value(x, y) / 255f;
					GL11.glColor3f(c,c,c);
					
					GL11.glBegin(GL11.GL_QUADS);
						GL11.glVertex2f(scale*x+scale*sprite.pos_x,scale*y+scale*sprite.pos_y);
						GL11.glVertex2f(scale*x+scale*sprite.pos_x+scale,scale*y+scale*sprite.pos_y);
						GL11.glVertex2f(scale*x+scale*sprite.pos_x+scale,scale*y+scale*sprite.pos_y+scale);
						GL11.glVertex2f(scale*x+scale*sprite.pos_x,scale*y+scale*sprite.pos_y+scale);
					GL11.glEnd();
				}
			}
		}
	}
	
	public void render_pixel(int pos_x, int pos_y) {
		GL11.glColor3f(1.0f,1.0f,1.0f);
		
		GL11.glBegin(GL11.GL_QUADS);
			GL11.glVertex2f(scale*pos_x,scale*pos_y);
			GL11.glVertex2f(scale*pos_x+scale,scale*pos_y);
			GL11.glVertex2f(scale*pos_x+scale,scale*pos_y+scale);
			GL11.glVertex2f(scale*pos_x,scale*pos_y+scale);
		GL11.glEnd();
	}
	
	public void render_pixel(int pos_x, int pos_y, MyColor color) {
		GL11.glColor3f(color.r,color.g,color.b);
		
		GL11.glBegin(GL11.GL_QUADS);
			GL11.glVertex2f(scale*pos_x,scale*pos_y);
			GL11.glVertex2f(scale*pos_x+scale,scale*pos_y);
			GL11.glVertex2f(scale*pos_x+scale,scale*pos_y+scale);
			GL11.glVertex2f(scale*pos_x,scale*pos_y+scale);
		GL11.glEnd();
	}
}

