/*
Copyright (C) 2013 by Florian SIMON

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */
package org.jew.swing.progressbar;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.renderable.RenderableImage;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Icon;
import javax.swing.JComponent;

@SuppressWarnings("serial")
public class CircularProgressBar extends JComponent {

	public static final int CLOCKWISE = 1;
	public static final int ANTICLOCKWISE = -1;

	protected final int refreshPeriod = 25;

	protected boolean indeterminate;

	public void setIndeterminate(final boolean indeterminate) {
		this.indeterminate = indeterminate;
	}

	public boolean isIndeterminate() {
		return indeterminate;
	}

	protected Icon icon;

	public void setIcon(Icon icon) {
		this.icon = icon;
	}

	public Icon getIcon() {
		return icon;
	}

	protected int rotationSpeed;

	public void setRotationSpeed(int rotationSpeed) {
		this.rotationSpeed = rotationSpeed;
	}

	public int getRotationSpeed() {
		return rotationSpeed;
	}

	protected int rotationDirection;

	public void setRotationDirection(int rotationDirection) {
		this.rotationDirection = rotationDirection;
	}

	public int getRotationDirection() {
		return rotationDirection;
	}

	protected Color textColor;

	public void setTextColor(Color textColor) {
		this.textColor = textColor;
	}

	public Color getTextColor() {
		return textColor;
	}

	protected double minimum;

	public void setMinimum(double minimum) {
		this.minimum = minimum;
	}

	public double getMinimum() {
		return minimum;
	}

	protected double maximum;

	public void setMaximum(double maximum) {
		this.maximum = maximum;
	}

	public double getMaximum() {
		return maximum;
	}

	protected double value;

	public void setValue(double value) {
		this.value = value;
	}

	public double getValue() {
		return value;
	}

	protected double rotationAngle;

	public double getRotationAngle(){
		return this.rotationAngle;
	}

	protected Timer timer;
	protected TimerTask task;
	protected boolean isRunning;

	/*
	 * Circular ProgressBar Creator
	 */
	public CircularProgressBar() {
		this.setOpaque(false);
		this.textColor = Color.BLACK;
		this.rotationSpeed = 1000;
		this.rotationDirection = CLOCKWISE;
		this.indeterminate = true;
		this.isRunning = false;
		this.minimum = 0;
		this.maximum = 0;
		this.value = 0;
		this.timer = new Timer();
		this.task = this.createTimerTask();

		//		this.setOpaque(true);

		this.setUI(new Windows7LikeUI());
	}


	@Override
	public void paint(final Graphics g) {

		if(this.isRunning == false){
			this.rotationAngle = 0;
			this.timer.schedule(this.task, 0, this.refreshPeriod);
			this.isRunning = true;
		}

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		if(indeterminate){
			paintIndeterminate(g2d);
		}else{
			paintDetermintate(g2d);
		}		
	}

	protected void paintDetermintate(final Graphics2D g2d) {
		// TODO Auto-generated method stub		
	}

	protected void paintIndeterminate(final Graphics2D g2d) {
		this.ui.paint(g2d, this);		
	}

	private TimerTask createTimerTask() {
		return new TimerTask() {
			@Override
			public void run() {
				if(isShowing() == false){
					timer.cancel();
					timer.purge();
					isRunning = false;
				}

				rotationAngle += (Math.PI * 2) / (rotationSpeed / refreshPeriod) * rotationDirection;

				repaint();
			}
		};
	}
}

