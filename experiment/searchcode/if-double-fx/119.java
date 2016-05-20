package javatest.game;

import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javatest.game.entities.Entity;
import javatest.game.entities.Exhaust;

/**
 * Handles user's mouse input.
 * 
 * @author Viliam Simko
 */
public class WorldInput implements MouseMotionListener, MouseListener {

	World world;

	public WorldInput(World world, Window window) {
		this.world = world;
		window.addMouseListener(this);
		window.addMouseMotionListener(this);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	Entity draggedEntity;
	int dragOffsetX;
	int dragOffsetY;

	private void emitDecoration(int x, int y, int entityRadius,
		double decorationSpeed, int numDecor) {
		double angle = 2 * Math.PI / numDecor;
		for (int i = 0; i < numDecor; i++) {
			double fx = Math.cos(i * angle);
			double fy = Math.sin(i * angle);
			world
					.addEntity(new Exhaust(world, x + fx * entityRadius, y + fy
							* entityRadius, fx * decorationSpeed, fy
							* decorationSpeed));
		}
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		for (Entity entity : world.getEntities()) {
			if (entity.isClicked(e.getX(), e.getY())) {
				emitDecoration((int) entity.x, (int) entity.y, entity
						.getEntityRadius(), 3, 7);
				draggedEntity = entity;
				dragOffsetX = (int) entity.x - e.getX();
				dragOffsetY = (int) entity.y - e.getY();
				entity.vx = 0;
				entity.vy = 0;
				return;
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (draggedEntity == null)
			return;
		
		emitDecoration((int) draggedEntity.x, (int) draggedEntity.y,
				draggedEntity.getEntityRadius(), -0.1, 11);
		
		updateMouseAcceleration(e.getX(), e.getY());
		draggedEntity.vx = mouseAccelX;
		draggedEntity.vy = mouseAccelY;
		
		draggedEntity = null;
	}

	double mouseAccelX;
	double mouseAccelY;
	double prevMouseX;
	double prevMouseY;
	
	private void updateMouseAcceleration(double mouseX, double mouseY) {
		mouseAccelX = mouseAccelX * 0.8 + (mouseX - prevMouseX) * 0.1;
		mouseAccelY = mouseAccelY * 0.8 + (mouseY - prevMouseY) * 0.1;
		prevMouseX = mouseX;
		prevMouseY = mouseY;
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		updateMouseAcceleration(e.getX(), e.getY());
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (draggedEntity != null) {
			draggedEntity.x = e.getX() + dragOffsetX;
			draggedEntity.y = e.getY() + dragOffsetY;
		}
		updateMouseAcceleration(e.getX(), e.getY());
	}
}

