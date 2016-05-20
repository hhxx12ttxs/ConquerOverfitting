/*
 *	AnimatorRunnable.java -  Part of BrowserChooser 0.2
 *
 *	OpenSource code and fragments by avocado systems, Marcus Fuhrmeister
 *
 *	Redistribution and use in source and binary forms, with or without
 *	modification, are permitted provided that the following conditions
 *	are met:
 *  
 *		* Redistribution of source code must retain the above OpenSource comment,
 *		this list of conditions and the following disclaimer.
 * 
 *		* Redistribution in binary form must reproduce the above OpenSource comment,
 *		this list of conditions and the following disclaimer in the
 *		documentation and/or other materials provided with the distribution.
 * 
 *	Neither the name of 'avocaod systems, Marcus Fuhrmeister' or the
 *	names of contributors may be used to endorse or promote products derived
 *	from this software without specific prior written permission.
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *	Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA. 
 *
 *	@project	BrowserChooser 0.2
 *	@package	de.fuhrmeister.browserchooser.animation
 *	@file		AnimatorRunnable.java
 *
 */
package de.fuhrmeister.browserchooser.animation;

import java.awt.Component;
import java.awt.peer.ComponentPeer;

import javax.swing.JFrame;

/**
 * 
 * @date 10.12.2009
 * @author Marcus Fuhrmeister
 * @version
 */

public class RAnimatorSize implements Runnable {

	private int delay;
	private int speed;
	private JFrame window;
	private int width;
	private int step;

	public RAnimatorSize(JFrame window, final int width, final int step) {
		this.window = window;
		this.width = width;
		this.step = step;
	}

	public void setDelay(final int delay) {
		this.delay = delay;
	}

	public void setSpeed(final int speed) {
		this.speed = speed;
	}

	public void run() {
		Animator.isAnimating = true;
		synchronized (this) {
			try {
				this.wait(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		//BrowserShortcutView label = new BrowserShortcutView(null, 1, width);
		//label.setBackground(Color.black);
		//window.add(label, window.getContentPane().getComponentCount()-1);
//		if (step <= 0) window.setSize(200, window.getHeight());
		for (int counter = 1; counter <= width;) {
			//window.setSize(window.getWidth() + this.step, window.getHeight());
			//int xCoordinate = (counter % (this.step*2) == 0) ? window.getX()- this.step : window.getX();
//			window.setLocation(xCoordinate, window.getY());
//			window.setSize(window.getWidth() + this.step, window.getHeight());
			//window.getContentPane().getComponent(window.getContentPane().getComponentCount()-1).setSize(counter, width);
//			window.setBounds(xCoordinate, window.getY(), window.getWidth() + this.step, window.getHeight());
		//	Component c = window;
			//c.getPeer().setBounds(xCoordinate, window.getY(), window.getWidth() + this.step, window.getHeight(), ComponentPeer.DEFAULT_OPERATION);
			synchronized (this) {
				try {
					this.wait(speed);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			counter+= Math.abs(this.step);
			//System.out.println(window.getContentPane().getComponent(window.getContentPane().getComponentCount()-1).getSize());
		}
		Animator.isAnimating = false;
	}

}

