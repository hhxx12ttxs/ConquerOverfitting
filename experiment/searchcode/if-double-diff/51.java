package com.googlecode.grtframework.sensor.gui;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JComponent;

import com.googlecode.grtframework.event.PotentiometerEvent;
import com.googlecode.grtframework.event.PotentiometerListener;
import com.googlecode.grtframework.sensor.IPotentiometer;
import com.googlecode.grtframework.vis.Mountable;
import com.googlecode.grtframework.vis.MountedPosition;
import com.googlecode.grtframework.vis.Select;

/**
 * A simulated Potentiometer that receives input from the graphical environment.
 * 
 * @author ajc
 * 
 */
public class SimPotentiometer implements IPotentiometer, MouseMotionListener,
		MouseListener, Mountable {

	private static final int MAX_SELECT_DISTANCE = 30;

	private Vector listeners;
	private double wiperAngle;

	private final MountedPosition position;

	// source
	private final JComponent jcomp;

	// selection
	private boolean selected;

	public SimPotentiometer(JComponent jcomp, MountedPosition position) {
		this.jcomp = jcomp;
		this.position = position;
		listeners = new Vector();
		selected = false;
	}

	@Override
	public void start() {
		jcomp.addMouseListener(this);
		jcomp.addMouseMotionListener(this);
	}

	private static double getAngle(Point sensor, Point dragged) {
		return Math.atan2((dragged.getY() - sensor.getY()),
				(dragged.getX() - sensor.getX()));
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		if (selected) {
			Point sensorPosition = new Point(getX(), getY());
			Point mouse = arg0.getPoint();
			// TODO multiple rotation simulation
			// double diff = prevAngle - getAngle(sensorPosition, mouse);
			// System.out.println(diff);
			// if (diff < 1) {
			// angle -= diff;
			// }
			// prevAngle = getAngle(sensorPosition, mouse);
			wiperAngle = getAngle(sensorPosition, mouse);
			notifyAngleChange(wiperAngle);

		}
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {

	}

	@Override
	public void mouseExited(MouseEvent arg0) {

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// toggle by the right click rule
		if (Select.RightClick.shouldToggleSelect(arg0,
				new Point(getX(), getY()))) {
			selected = !selected;
		}
	}

	@Override
	public void addPotentiometerListener(PotentiometerListener l) {
		listeners.add(l);
	}

	@Override
	public void removePotentiometerListener(PotentiometerListener l) {
		listeners.remove(l);
	}

	// TODO uncertain if we need to provide an angle
	public void notifyAngleChange(double angle) {
		PotentiometerEvent ev = new PotentiometerEvent(this, angle);
		for (Enumeration<PotentiometerListener> e = listeners.elements(); e
				.hasMoreElements();) {
			e.nextElement().rotationChanged(ev);
		}
	}

	// *********
	// MOUNTABLE
	// *********
	@Override
	public double getHeading() { // heading wrt the wiper
		return position.getHeading() + wiperAngle;
	}

	@Override
	public int getX() {
		return position.getX();
	}

	@Override
	public int getY() {
		return position.getY();
	}

}

