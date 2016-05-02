/**
 * Copyright (c) 2006-2008 MiniMe. Code released under The MIT/X Window System
 * License. Full license text can be found in license.txt
 */
package minime.ui.menu;

import javax.microedition.lcdui.Graphics;

import minime.Composite;
import minime.Drawable;
import minime.Image;
import minime.Logger;
import minime.Portability;
import minime.core.Event;
import minime.core.EventDispatcher;
import minime.core.Runtime;
import minime.gen.ImageRscId;
import minime.ui.ImagePanel;

/**
 * The class is to display a 3D menu screen.
 * 
 * @author The Flash.
 * 
 */
public class Carousel extends Menu {

	private static Logger LOG = Logger.getLogger("minime.ui.MenuScale");

	private static final int MOVE_ANIMATION = 1;

	private final static int ANIMATION_INTERVAL = 60; // how long time to reach
														// the next sprite
														// position
	private final static int FRAME_RATE = 10; // how many ms per frame

	private final static int offSetFromTop = -100;

	private int moveMaxNbrStep = ANIMATION_INTERVAL / FRAME_RATE;
	private boolean isMoving = false;

	private int moveStepCount = 0;

	private double stepTurnAngle;

	private int itemCount;

	private Carousel3D carousel;
	// the next selected item index
	private int nextSelectItemIndex;

	private ImagePanel iconLeft = new ImagePanel(
			ImageRscId.IM_ICON_TRIANGLE_LEFT);
	private ImagePanel iconRight = new ImagePanel(
			ImageRscId.IM_ICON_TRIANGLE_RIGHT);

	public Carousel(CarouselItem[] items) {
		carousel = new Carousel3D(items);
		createMenuItems(items);
	}

	private void createMenuItems(CarouselItem[] items) {
		composite = new Composite();
		for (int i = 0; i < items.length; i++)
			composite.addDrawable(items[i]);
	}

	protected void layoutImp() {
		LOG.debug("In layout method");
		carousel.layout();
		for (int i = 0; i < itemCount; i++) {
			Drawable d = composite.drawableAt(i);
			d.setPosition((getWidth() - d.getWidth()) / 2, offSetFromTop);
		}
		
//		System.out.println("Carousel.layoutImp(), size(" + size.width + "," + size.height +")");
//		System.out.println("Carousel.layoutImp(), iconLeft(0, " + 
//							(size.height - iconLeft.getHeight()) + 
//						    "), iconDown(" + 
//						    (size.width - iconRight.getWidth()) + "," + 
//						    (size.height - iconRight.getHeight()) +")");
		iconLeft.setPosition(0, size.height - iconLeft.getHeight());
		iconRight.setPosition(size.width - iconRight.getWidth(), 
							size.height - iconRight.getHeight());
	}
	
	public void renderImpl(Graphics gc) {
		LOG.debug("In render method");
		carousel.render(gc);
		iconLeft.render(gc);
		iconRight.render(gc);
	}

	/**
	 * override containsPoint
	 */
	public boolean containsPoint(int x, int y) {
		boolean contansUp = iconLeft.containsPoint(x - getLeft(), y - getTop());
		boolean containsDown = iconRight.containsPoint(x - getLeft(), y
				- getTop());
		return super.containsPoint(x, y) || contansUp || containsDown;
	}

	protected boolean onTimerEvent(Event evt) {
		boolean handled = false;

		if (evt.paramB == MOVE_ANIMATION) {
			moveStepCount++;
			Runtime.getInstance().repaint();
			carousel.move(evt.paramA, stepTurnAngle);
			LOG.debug("Move the carousel :" + moveStepCount);
			if (moveStepCount < moveMaxNbrStep) {
				EventDispatcher.getInstance().fire(
						Event.createEvent(Event.TIMER_EVENT, evt.paramA,
								evt.paramB, null, FRAME_RATE));
			} else {
				moveStepCount = 0;
				isMoving = false;
				if (evt.paramA == Carousel3D.CLOCK_WISE)
					selectedItemIndex = (selectedItemIndex + 1) % itemCount;
				else
					selectedItemIndex = (selectedItemIndex - 1 + itemCount)
							% itemCount;
			}
			
			handled = true;
		}
		return handled;
	}

	protected int pointerInX;
	protected int pointerInY;

	protected boolean onPointerPressed(int x, int y) {
		int vx = x - getLeft();
		int vy = y - getTop();

		pointerInX = x;
		pointerInY = y;

		if (isMoving) {
			return false;
		}

		if (iconLeft.containsPoint(vx, vy)) {
			return onKeyPressed(Event.createEvent(Event.KEY_DOWN_EVENT, 0,
					Portability.KEY_LEFT_GAME_ACTION));
		} else if (iconRight.containsPoint(vx, vy)) {
			return onKeyPressed(Event.createEvent(Event.KEY_DOWN_EVENT, 0,
					Portability.KEY_RIGHT_GAME_ACTION));
		}
		return true;
	}

	private boolean dragged = false;

	protected boolean onPointerDragged(int x, int y) {
		if (!containsPoint(x, y)) {
			return false;
		}

		if (isMoving) {
			return true;
		}

		int movDistance = x - pointerInX;
		int step = size.width / 5;
		if (Math.abs(movDistance) > step) {
			pointerInX = x;
			if (movDistance > 0) { // move to show next item
				return onKeyPressed(Event.createEvent(Event.KEY_DOWN_EVENT, 0,
						Portability.KEY_LEFT_GAME_ACTION));
			} else { // move to show previous item
				return onKeyPressed(Event.createEvent(Event.KEY_DOWN_EVENT, 0,
						Portability.KEY_RIGHT_GAME_ACTION));
			}
		}

		dragged = true;
		return true;
	}

	protected boolean onPointerReleased(int x, int y) {
		if (dragged) {
			dragged = false;
			return true;
		}
		
		Drawable d = (Drawable) carousel.imageProperties[selectedItemIndex].imagePanel;
		if (d.containsPoint(x - left, y - top))
			fireMenuEvent(Event.MENU_SELECTION_EVENT);
		
		return true;
	}

	protected boolean onKeyPressed(Event evt) {
		boolean handled = false;

		if (isMoving) {
			return true;
		}

		if (evt.paramB == Portability.KEY_RIGHT_GAME_ACTION) {
			isMoving = true;
			nextSelectItemIndex = (selectedItemIndex + 1) % itemCount;
			EventDispatcher.getInstance().fire(
					Event.createEvent(Event.TIMER_EVENT, Carousel3D.CLOCK_WISE,
							MOVE_ANIMATION, null, FRAME_RATE));
			EventDispatcher.getInstance().fire(
					Event.createEvent(Event.KEY_EVENT, nextSelectItemIndex,
							Portability.KEY_LEFT_GAME_ACTION));
			handled = true;
		} else if (evt.paramB == Portability.KEY_LEFT_GAME_ACTION) {
			isMoving = true;
			nextSelectItemIndex = (selectedItemIndex - 1 + itemCount)
					% itemCount;
			EventDispatcher.getInstance().fire(
					Event.createEvent(Event.TIMER_EVENT,
							Carousel3D.COUNTER_CLOCK_WISE, MOVE_ANIMATION,
							null, FRAME_RATE));
			EventDispatcher.getInstance().fire(
					Event.createEvent(Event.KEY_EVENT, nextSelectItemIndex,
							Portability.KEY_RIGHT_GAME_ACTION));
			handled = true;
		} else if ((evt.paramA == Portability.KEY_FIRE || evt.paramB == Portability.KEY_FIRE_GAME_ACTION)) {
			fireMenuEvent(Event.MENU_SELECTION_EVENT);
			handled = true;
		}
		
		if (handled)
			Runtime.getInstance().repaint();
		
		return handled;
	}
	
	protected boolean onKeyReleased(Event evt) {
		if (evt.paramB == Portability.KEY_RIGHT_GAME_ACTION ||
			evt.paramB == Portability.KEY_LEFT_GAME_ACTION ||
			evt.paramB == Portability.KEY_FIRE_GAME_ACTION) {
//			System.out.println("true!!!!!");
			return true;
		}
				
//		System.out.println("false!!!!!");		
		return false;
	}

	private class Carousel3D {
		public final static int CLOCK_WISE = 1;
		public final static int COUNTER_CLOCK_WISE = -1;

		public final static int ZOOM_IN = 1;
		public final static int ZOOM_OUT = 2;

		private final static double X_RADIUS = 110;
		private final static double Y_RADIUS = 40;

		private final static double SCALE_RADIO = 0.8;
		
		private double angleMinimum = -190;
		private double angleStep = 40;
		private double angleMaximum;
		private double centerAngle;
		
		private double nextScaleRate;

		private int centerXPos;
		private int centerYPos;
		private double prospectiveCoordinate;
//		private int[] imgRscIds;
		private Image[] imgs;
		//the default selected menu index
		private static final int defaultSelectedIndex=0;

		private class ImageProperty {
			private double angle;
			private double scaleRate;
			private int index;

			private double x;
			private double y;
			private int left;
			private int top;
			private Image image;
			private ImagePanel imagePanel;

			public ImageProperty(int index, Image image) {
				this.image = image;
				this.index = index;
				reCalcAngle(true);
			}

			public void reCalcAngle(boolean isfirstCalculate) {
				angle = calcAngle(index);
				LOG.debug("The angle of the image " + index + " is " + angle);
				double randian = angle / 180 * Math.PI;
				x = Math.cos(randian) * X_RADIUS;
				y = Math.sin(randian) * Y_RADIUS;
				if (isfirstCalculate) {
					if (index == selectedItemIndex)
						scaleRate = 1.0;
					else
						scaleRate = Math.abs((y - prospectiveCoordinate)
								/ (-Y_RADIUS - prospectiveCoordinate))
								* SCALE_RADIO;
				} else {
					if (index == nextSelectItemIndex)
						scaleRate = calcScaleRate(ZOOM_OUT);
					else if (index == selectedItemIndex)
						scaleRate = calcScaleRate(ZOOM_IN);
					else
						scaleRate = Math.abs((y - prospectiveCoordinate)
								/ (-Y_RADIUS - prospectiveCoordinate))
								* SCALE_RADIO;
				}
				double width = image.getWidth() * scaleRate;
				double height = image.getHeight() * scaleRate;
				if (imagePanel == null)
					// scale image
					imagePanel = new ImagePanel(image.scaleBresenham(
							(int) width, (int) height, true));
				else
					imagePanel.setImage(image.scaleBresenham((int) width,
							(int) height, true));
				LOG.debug("The position of the image x:" + x + " y:" + y);
				left = (int) calcXPos(x, width);
				top = (int) calcYPos(y, height);
				imagePanel.setPosition(left, top);
				LOG.debug("The position of the image left:" + left + " top:"
						+ top);

			}
		}

		private ImageProperty[] imageProperties;

		private Carousel3D(CarouselItem[] items) {
			itemCount = items.length;
//			moveMaxNbrStep = ANIMATION_INTERVAL / FRAME_RATE;
			stepTurnAngle = angleStep / moveMaxNbrStep;
			angleMaximum = angleMinimum + angleStep * itemCount;
			centerAngle = -90;
			selectedItemIndex = 0;
			adjustMenuParam();
			selectedItemIndex=defaultSelectedIndex;
			stepTurnAngle=angleStep/moveMaxNbrStep;
			angleMaximum=angleMinimum+angleStep*itemCount;
			imageProperties = new ImageProperty[itemCount];
//			imgRscIds = new int[itemCount];
			imgs = new Image[itemCount];
			for (int i = 0; i < itemCount; i++)
//				imgRscIds[i] = items[i].getImgRscId();
				imgs[i] = items[i].getIcon();
		}
		
		/**
		 * Adjust the menu parameters by the number of the menu items
		 * Only one menu item:   Should not be scrolled and can only be selected
		 *      two menu items:  One is selected in the center of the menu, and the other is not visible
		 *      three menu items:One is on the left side, and the second is on the right side, and the third 
		 *      				 is in the center of the menu. 
		 *      four or more menu items:
		 *      				 ...
		 */
		protected void adjustMenuParam() {
			if (itemCount == 1) {
				angleMinimum = -190;
				angleStep = 200;
			} else if (itemCount == 2) {
				angleMinimum = -190;
				angleStep = 90;
			} else if (itemCount == 3) {
				angleMinimum = -190;
				angleStep = 70;
			} else if (itemCount == 4) {
				angleMinimum = -190;
				angleStep = 50;
			} else if (itemCount >= 5) {
				angleMinimum = -190;
				angleStep = 40;
			}
		}

		/**
		 * @param index
		 *            : the index of the image
		 * @return the angle of the image range[-angleMinimum,angleMaximum]
		 */
		private double calcAngle(int index) {
//			double result = ((centerAngle - angleMinimum) + (index - 2)
//					* angleStep + (angleMaximum - angleMinimum))
//					% (angleMaximum - angleMinimum) + angleMinimum;
			
			double result = ((centerAngle - angleMinimum)
					+ (index - defaultSelectedIndex) * angleStep + (angleMaximum - angleMinimum))
					% (angleMaximum - angleMinimum) + angleMinimum;
			return result;
		}

		// private double calcScaleRate(int type)
		// {
		// //The scale rate function parameter y=sqrt(kx+b)
		// //x: the moving step count y: the scaleRate value
		// //(only for the selected item and the next selected item)
		// double k;
		// double b;
		// double result;
		// if(type==ZOOM_OUT)
		// {
		// k=(1.0-nextScaleRate*nextScaleRate)/moveMaxNbrStep;
		// b=nextScaleRate*nextScaleRate;
		// result=Math.sqrt(k*moveStepCount+b);
		// }
		// else
		// {
		// k=(nextScaleRate*nextScaleRate-1)/moveMaxNbrStep;
		// b=1.0;
		// result=Math.sqrt(k*moveStepCount+b);
		// }
		// return result;
		//			
		// }

		private double calcScaleRate(int type) {
			double d;
			double result;
			if (type == ZOOM_OUT) {
				d = (1.0 - nextScaleRate) / moveMaxNbrStep;
				result = nextScaleRate + moveStepCount * d;
			} else {
				d = (nextScaleRate - 1.0) / moveMaxNbrStep;
				result = 1.0 + moveStepCount * d;
			}
			return result;
		}

		private void layout() {
			centerXPos = getWidth() >> 1;
			centerYPos = getHeight() >> 1;
			prospectiveCoordinate = Y_RADIUS;
//			System.out.println("itemCount=" + itemCount + ",imgs.size=" + imgs.length + ",imageProperties.size=" + imageProperties.length);
			for (int i = 0; i < itemCount; i++)
				imageProperties[i] = new ImageProperty(i, imgs[i]);
			nextScaleRate = imageProperties[(selectedItemIndex + 1) % itemCount].scaleRate;
		}

		public void render(Graphics gc) {
			LOG.debug("In Carousel render method!");
			for (int i = 0; i < itemCount; i++) {
				if (imageProperties[i].y < 0)
					imageProperties[i].imagePanel.render(gc);
			}
		}

		private void move(int direction, double angleStep) {
			if (direction == COUNTER_CLOCK_WISE) {
				centerAngle = ((centerAngle - angleMinimum) + angleStep + (angleMaximum - angleMinimum))
						% (angleMaximum - angleMinimum) + angleMinimum;
			} else {
				centerAngle = ((centerAngle - angleMinimum) - angleStep + (angleMaximum - angleMinimum))
						% (angleMaximum - angleMinimum) + angleMinimum;
			}
			setInitialPosition();
		}

		private void setInitialPosition() {
			for (int i = 0; i < itemCount; i++)
				imageProperties[i].reCalcAngle(false);
		}
		
//		private void setInitialPosition(int direction) {
//			if (direction == COUNTER_CLOCK_WISE) {
//				for (int i = 0; i < itemCount; i++) {
//					if (i == itemCount - 1 ) {
//						imageProperties[i].reCalcAngle(false);
//					} else {
//						imageProperties[i] = imageProperties[i+1];
//					}
//				}
//			} else {
//				
//			}
//		}

		private int calcXPos(double xCoordinate, double width) {
			return (int) (xCoordinate + centerXPos - width / 2);
		}

		private int calcYPos(double yCoordinate, double height) {
			return (int) (centerYPos - yCoordinate - height / 2);
		}

	}

}

