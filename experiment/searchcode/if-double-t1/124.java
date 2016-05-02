/* Copyright (c) 2012 Jesper Öqvist <jesper@llbit.se>
 *
 * This file is part of Chunky.
 *
 * Chunky is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Chunky is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with Chunky.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.llbit.chunky.renderer.test;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import javax.swing.JFrame;

import se.llbit.chunky.main.Chunky;
import se.llbit.chunky.model.LeverModel;
import se.llbit.chunky.model.SignPostModel;
import se.llbit.chunky.model.TorchModel;
import se.llbit.chunky.model.WallSignModel;
import se.llbit.chunky.renderer.Camera;
import se.llbit.chunky.renderer.Refreshable;
import se.llbit.chunky.renderer.RenderableCanvas;
import se.llbit.chunky.renderer.Renderer;
import se.llbit.chunky.renderer.ui.Chunk3DView;
import se.llbit.chunky.renderer.ui.RenderCanvas;
import se.llbit.chunky.renderer.ui.ViewListener;
import se.llbit.chunky.resources.Texture;
import se.llbit.chunky.world.Block;
import se.llbit.math.AABB;
import se.llbit.math.Color;
import se.llbit.math.Matrix3d;
import se.llbit.math.Quad;
import se.llbit.math.Ray;
import se.llbit.math.Ray.RayPool;
import se.llbit.math.Triangle;
import se.llbit.math.Vector3d;
import se.llbit.math.Vector4d;
import se.llbit.util.VectorPool;

/**
 * Test renderer
 * @author Jesper Öqvist <jesper@llbit.se>
 */
@SuppressWarnings("unused")
public class TestRenderer extends Thread implements ViewListener,
	Renderer, Refreshable, ImageObserver {

	private Chunk3DView view;
	private BufferedImage buffer;
	private BufferedImage backBuffer;
	private final int width;
	private final int height;
	private final VectorPool vectorPool = new VectorPool();
	private final RayPool rayPool = new RayPool();
	private final Camera camera;
	private boolean refresh = true;
	private Vector3d camPos = new Vector3d();
	private Matrix3d rot = new Matrix3d();
	private Matrix3d tmpRot = new Matrix3d();
	private double distance = 1.5;
	
	private static final Texture[] tex = {
		new Texture("east"),
		new Texture("west"),
		new Texture("north"),
		new Texture("south"),
	};
	
	private final Quad[] quads = {
			new Quad(new Vector3d(1, 0, 0), new Vector3d(1, 0, 1), new Vector3d(1, 1, 0), new Vector4d(0, 1, 0, 1)),
			new Quad(new Vector3d(0, 0, 1), new Vector3d(0, 0, 0), new Vector3d(0, 1, 1), new Vector4d(0, 1, 0, 1)),
			new Quad(new Vector3d(0, 0, 0), new Vector3d(1, 0, 0), new Vector3d(0, 1, 0), new Vector4d(0, 1, 0, 1)),
			new Quad(new Vector3d(1, 0, 1), new Vector3d(0, 0, 1), new Vector3d(1, 1, 1), new Vector4d(0, 1, 0, 1)),
	};
	
	private TestModel testModel = new TestModel();

	/**
	 * Constructor
	 * @param parent 
	 */
	public TestRenderer(JFrame parent) {
		width = RenderableCanvas.DEFAULT_WIDTH;
		height = RenderableCanvas.DEFAULT_HEIGHT;
		buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		backBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		camera = new Camera(this);
		camera.setPosition(new Vector3d(.5, .5, 2));
		camera.setView(-Math.PI/2, -Math.PI/2);
		
		view = new Chunk3DView(this, parent);
		view.setRenderer(this);
		view.setVisible(true);
	}
	
	@Override
	public void run() {
		
		try {
			while (!isInterrupted()) {
				
				testModel.setUp();
				waitRefresh();
				
				synchronized (backBuffer) {
					raytrace();
					
					// flip buffers
					BufferedImage tmp = backBuffer;
					backBuffer = buffer;
					buffer = tmp;
				}
				
				view.getCanvas().repaint();
				
			}
		} catch (InterruptedException e) {
		}
	}

	/**
	 * Raytrace one frame.
	 */
	private void raytrace() {
		
		double aspect = width / (double) height;
		
		Ray ray = rayPool.get();
		
		camPos.set(0, 0, distance);
		tmpRot.rotX(-camera.getPitch() - Math.PI / 2);
		rot.rotY(camera.getYaw() + Math.PI / 2);
		rot.mul(tmpRot);
		rot.transform(camPos);
		camPos.add(.5, .5, .5);
		
		for (int x = 0; x < width; ++x) {
			
			double rayx = camera.fovTan * aspect *
					(.5 - ((double) x) / width);
			
			for (int y = 0; y < height; ++y) {
				
				ray.setDefault();
				ray.d.set(camera.fovTan *
						(-.5 + ((double) y) / height),
						-1, rayx);
				ray.d.normalize();
				camera.transform(ray.d);
				
				ray.x.set(camPos);
				raytrace(ray);
					
				ray.color.x = Math.min(1, Math.sqrt(ray.color.x));
				ray.color.y = Math.min(1, Math.sqrt(ray.color.y));
				ray.color.z = Math.min(1, Math.sqrt(ray.color.z));
				buffer.setRGB(x, y, Color.getRGB(ray.color));
			}
		}
	}
	
	private void raytrace(Ray ray) {
		int level = 0;
		double tNear = Double.NEGATIVE_INFINITY;
		double tFar = Double.POSITIVE_INFINITY;
		double t1, t2;
		Vector3d d = ray.d;
		Vector3d o = ray.x;
		
		if (d.x != 0) {
			t1 = -o.x / d.x;
			t2 = ((1<<level) - o.x) / d.x;
			
			if (t1 > t2) {
				double t = t1;
				t1 = t2;
				t2 = t;
			}
			
			if (t1 > tNear) tNear = t1;
			if (t2 < tFar) tFar = t2;
		}
		
		if (d.y != 0) {
			t1 = -o.y / d.y;
			t2 = ((1<<level) - o.y) / d.y;
			
			if (t1 > t2) {
				double t = t1;
				t1 = t2;
				t2 = t;
			}
			
			if (t1 > tNear) tNear = t1;
			if (t2 < tFar) tFar = t2;
		}
		
		if (d.z != 0) {
			t1 = -o.z / d.z;
			t2 = ((1<<level) - o.z) / d.z;
			
			if (t1 > t2) {
				double t = t1;
				t1 = t2;
				t2 = t;
			}
			
			if (t1 > tNear) tNear = t1;
			if (t2 < tFar) tFar = t2;
		}
		
		ray.color.set(1, 1, 1, 1);
		
		if (tNear <= tFar && tFar >= 0) {
			ray.x.scaleAdd(tNear, d, ray.x);
			ray.distance += tNear;
			
			ray.t = Double.POSITIVE_INFINITY;
			for (int i = 0; i < quads.length; ++i) {
				if (quads[i].intersect(ray)) {
					ray.t = ray.tNear;
					tex[i].getColor(ray);
				}
			}
			
			ray.t = Double.POSITIVE_INFINITY;
			testModel.intersect(ray);
		}
	}

	@Override
	public void onStrafeLeft() {
        camera.strafeLeft(.5);
        refresh();
	}
	
	@Override
	public void onStrafeRight() {
        camera.strafeRight(.5);
        refresh();
	}

	@Override
	public void onMoveForward() {
		distance -= .1;
		distance = Math.max(.1, distance);
		refresh();
	}

	@Override
	public void onMoveBackward() {
		distance += .1;
		distance = Math.max(.1, distance);
		refresh();
	}

	@Override
	public void onMoveForwardFar() {
		// do nothing
	}

	@Override
	public void onMoveBackwardFar() {
        // do nothing
	}

	@Override
	public void onMoveUp() {
        camera.moveUp(.5);
        refresh();
	}

	@Override
	public void onMoveDown() {
        camera.moveDown(.5);
        refresh();
	}

	@Override
	public void onMouseDragged(int dx, int dy) {
        camera.rotateView(
                - (Math.PI / 250) * dx,
                (Math.PI / 250) * dy);
        refresh();
	}

	@Override
	public void setViewVisible(boolean visible) {
		if (!visible) {
			interrupt();
			view.dispose();
		}
	}

	@Override
	public void zoom(int diff) {
		distance += .1 * diff;
		distance = Math.max(.1, distance);
		refresh();
	}

	@Override
	public void drawBufferedImage(Graphics g, int width, int height) {
		synchronized (backBuffer) {
			g.drawImage(backBuffer, 0, 0, width, height, null);
		}
	}

	@Override
	public void setBufferFinalization(boolean flag) {
		// do nothing
	}

	@Override
	public synchronized void refresh() {
		refresh = true;
		notifyAll();
	}
	
	private synchronized void waitRefresh() throws InterruptedException {
		while (!refresh)
			wait();
		refresh = false;
	}

	@Override
	public boolean imageUpdate(Image img, int infoflags, int x, int y,
			int width, int height) {
		// TODO Auto-generated method stub
		return false;
	}
}

