/**
 * Copyright (c) 2006-2008 MiniMe. Code released under The MIT/X Window System
 * License. Full license text can be found in license.txt
 */
/**
 * Contains the drawable base class, font and image classes as resources, and UI facilites.
 */
package minime;

import javax.microedition.lcdui.*;

import minime.core.CanDisable;
import minime.core.Dimension;
import minime.core.ResourceManager;
import minime.core.CanSelect;
import minime.core.painter.BackgroundPainter;
import minime.core.painter.BasicFramePainter;
import minime.core.painter.FramePainter;
import minime.core.painter.Painter;
import minime.core.painter.SingleColorPainter;
import minime.core.painter.SingleColorRoundedPainter;

/**
 * An object that has the capability of being placed on the display. The
 * contents displayed and their interaction with the user are defined by
 * subclasses. Unless otherwise specified by a subclass, the default state of
 * newly created Displayable objects is as follows: it is visible on the
 * Display; the coordinates is (0,0) and the size is also 0; the background
 * color is transparent; it is not clipped.
 * 
 */
public abstract class Drawable {
	/** The status is not be selected but it's not disabled */
	public final static int NOT_SELECTED_ABLE = 0;

	/**
	 * Box-orientation constant used to specify the drawable's alignment inside
	 * a box.
	 */
	public static final int X_ALIGN_LEFT = 0;
	public static final int X_ALIGN_CENTER = 1;
	public static final int X_ALIGN_RIGHT = 2;

	public static final int Y_ALIGN_TOP = 0;
	public static final int Y_ALIGN_CENTER = 1;
	public static final int Y_ALIGN_BOTTOM = 2;

	/** Selected status */
	// public final static int SELECTED = 1;
	/** Disabled status */
	// public final static int DISABLED = 2;

	/** The x,y coordinates and size of the drawable object */
	protected int left;
	protected int top;

	protected Dimension size;
	/** the background color */
	protected BackgroundPainter bgPainter;
	/** the frame Painter */
	protected FramePainter framePainter;

	protected int frameColor = Portability.C_BLACK;
	protected int frameThickness = 1;
	protected boolean needFrame = false;

	/** associated resource ID (-1 if any) */
	protected int rscId;

	/** set to true when the drawable need to layout by layout method */
	private boolean needLayout;
	/** set to true when the layout is going on, set to false when finish */
	private boolean doingLayout;
	/** The status */
	// private int status;

	/** The visible attribute, if true the component will be drawn */
	protected boolean visible;
//	private boolean visible;

	/** The initial visible attribute when the drawable component is created */
	private boolean initVisible;

	/** the drawable object clip status */
	public boolean clip;
	protected Rectangle clipRectangle;
	protected String name = "Drawable";

	static final Logger LOG = Logger.getLogger("minime.Drawable");

	public Drawable() {
		this.left = 0;
		this.top = 0;
		size = new Dimension();

		this.visible = true;
		this.clip = false;
		this.clipRectangle = new Rectangle();
		this.bgPainter = new SingleColorPainter(Portability.TRANSPARENT);
		this.framePainter = new BasicFramePainter();
		rscId = ResourceManager.NONE_RESOURCE;
		needLayout = true;
		doingLayout = false;

	}
	
	public void setName(String newName) {
		this.name = newName;
	}
	
	/**
	 * debug use 
	 */
	public String toString() {
		return new String("[" + name + "]," + super.toString() + " (" + left + "," + top + "," + size.width + "," + size.height + ")\n");
	}

	/**
	 * Renders the drawable object. If the visible attribute is false, it
	 * doesn't render the drawable. If clip attribute is true, it just renders
	 * the drawable area.
	 * 
	 * @param gc
	 *            the Graphics object to be used for rendering the Drawable
	 */
	public final void render(Graphics gc) {
		if (!isVisible()) {
			return;
		}

		// layout the component
		layout();

		Rectangle clipToRestore = new Rectangle();

		// clip if needed
		if (clip) {
			GraphicsUtil.getClip(gc, clipToRestore);
			GraphicsUtil.clip(gc, clipRectangle);
			LOG.debug("Clip " + this + "(" + getLeft() + ", " + getTop() + ", "
					+ size.width + ", " + size.height);
		}

		// translate the coordinate
		gc.translate(left, top);

		// rendering
		LOG.debug("Rendering " + this + "(" + getLeft() + ", " + getTop()
				+ ", " + size.width + ", " + size.height);
		drawBackground(gc);

		// check type of rendering
		if (this instanceof CanSelect && ((CanSelect) this).isSelected()) {
			((CanSelect) this).renderImplSelected(gc);
		} else if (this instanceof CanDisable
				&& !((CanDisable) this).isEnable()) {
			((CanDisable) this).renderImplDisable(gc);
		} else
			// basic rendering
			renderImpl(gc);
		drawFrame(gc);
		gc.translate(-left, -top);

		// restore clip area
		if (clip) {
			GraphicsUtil.setClip(gc, clipToRestore);
			LOG.debug("Restore clip:" + this + "(" + clipRectangle.x + ", "
					+ clipRectangle.y + ", " + clipRectangle.w + ", "
					+ clipRectangle.h);
		}
	}

	public void setClipRectangle(Rectangle rect) {
		clipRectangle = rect;
	}

	public final void layout() {
		if (!doingLayout && needLayout && isVisible()) {
			LOG.debug("perform layout:" + this);
			// set doingLayout in order to avoid recursive call to layout()
			// through getWidth and getHeight
			doingLayout = true;
			layoutImp();
			// layout is done, clear the flags
			clrNeedLayout();
			doingLayout = false;
		}
	}

	protected void layoutImp() {
//		clrNeedLayout();
	}

	public void setNeedLayout() {
		if (doingLayout == false)
			needLayout = true;
			if (parent != null) {
				parent.setNeedLayout();
			}
	}

	public void clrNeedLayout() {
		needLayout = false;
	}

	protected void drawBackground(Graphics gc) {
		bgPainter.paint(gc, 0, 0, size.width, size.height);
	}

	protected void drawFrame(Graphics gc) {
		framePainter.setFrameThicknessAndColor(frameThickness, frameColor);
		if (needFrame && framePainter != null) {
			framePainter.paint(gc, 0, 0, size.width - 1, size.height - 1);
		}
	}
	
	/**
	 * Get the Drawable's frame thickness
	 * 
	 * @return the Drawable's frame thickness
	 */
	public int getFrameThickness(){
		return frameThickness;
	}
	
	/**
	 * Get the Drawable's frame color
	 * 
	 * @return the Drawable's frame color
	 */
	public int getFrameColor(){
		return frameColor;
	}

	/**
	 * Renders the Drawable implementation.The operation will be defined by
	 * subclasses.
	 * 
	 * @param gc
	 *            the Graphics object to be used for rendering the Drawable
	 */
	public abstract void renderImpl(Graphics gc);

	public void setWidth(int w) {
		// update the width if necessary
		if (w != size.width) {
			setNeedLayout();
			size.setWidth(w);
		}
	}

	public void setHeight(int h) {
		// update the height if necessary
		if (h != size.height) {
			setNeedLayout();
			size.setHeight(h);
		}
	}

	// do auto layout to make sure Width & Height are set
	public int getWidth() {
		if (needLayout) { // decrease the invoking time of layout
			layout();
		}
		
		return size.width;
	}

	public int getWidthWithoutLayout() {
		return size.width;
	}

	public int getHeight() {
		if (needLayout) { // decrease the invoking time of layout
			layout();
		}
		return size.height;
	}

	public void setSize(Dimension d) {
		if (!size.equals(d)) {
			setNeedLayout();
		}
		size.setSize(d);
	}

	public Dimension getSize() {
		if (needLayout) { // decrease the invoking time of layout
			layout();
		}
		return new Dimension(size);
	}

	public int getRscId() {
		return rscId;
	}

	public void setRscId(int rscId) {
		this.rscId = rscId;
	}

	/**
	 * Set a painter to draw the component background
	 * 
	 * @param p
	 *            , the background painter
	 */
	public void setBackgroundPainter(BackgroundPainter p) {
		bgPainter = p;
	}

	/**
	 *@deprecated
	 *@see setBackgroundPainter
	 */
	public void setBgColor(int bgColor) {
		this.bgPainter = new SingleColorPainter(bgColor);
	}

	public void activate() {
		return;
	}

	public void deactivate() {
		return;
	}

	/**
	 * Sets the Drawable position
	 * 
	 * @param left
	 *            the x coordinate of Drawable
	 * @param top
	 *            the x coordinate of Drawable
	 */
	public void setPosition(int left, int top) {
		this.setLeft(left);
		this.setTop(top);
	}

	public void setLeft(int left) {
		this.left = left;
	}

	public int getLeft() {
		return left;
	}

	public void setTop(int top) {
		this.top = top;
	}

	public int getTop() {
		return top;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
		setNeedLayout();
	}

	public boolean isVisible() {
		return visible;
	}

	public boolean isInitVisible() {
		return initVisible;
	}

	public void setInitVisible(boolean initVisible) {
		this.initVisible = initVisible;
	}

	/**
	 * Used to check if this drawable has a composite in it, like Menu/MenuItem
	 * Drawables that have a composite should override this method and return
	 * true
	 */
	public boolean hasComposite() {
		return false;
	}

	/**
	 * Utility method to determine if the given point lies within the Component
	 * 
	 * @param x
	 *            the "x" coordinate of the point
	 * @param y
	 *            the "y" coordinate of the point
	 * @return true if the coordinate lies in the bounds of this layer
	 */
	public boolean containsPoint(int x, int y) {
		boolean contains = x >= getLeft() && x <= (getLeft() + getWidth())
				&& y >= getTop() && y <= getTop() + getHeight();
		return contains;
	}

	/**
	 * Set the background painter of this item. If set to null, no frame will be
	 * painted
	 * 
	 * @param painter
	 */
	public void setFramePainter(BasicFramePainter painter) {
		framePainter = painter;
	}

	/**
	 * Set the frame color.
	 * 
	 * @param color
	 */
	public void setFrameColor(int color) {
		this.frameColor = color; 
	}

	/**
	 * Set frame thickness.
	 * 
	 * @param thick
	 */
	public void setFrameThickness(int thick) {
		this.frameThickness = thick;
		if (needFrame)
			setNeedLayout();
	}

	/**
	 * Set the Drawable framed or not.
	 * @param needFrame
	 */
	public void setFramed(boolean needFrame) {
		this.needFrame = needFrame;
		setNeedLayout();
	}
	
    /**
     * Sets the Label frame thickness and color.
     * 
     * @param frameThickness
     *            the frame thickness
     * @param frameColor
     *            the color for frame
     */
    public void setFrameThicknessAndColor(int frameThickness, int frameColor)
    {
        this.frameThickness = frameThickness;
        this.frameColor = frameColor;
		if (needFrame)
			setNeedLayout();
    }
	
	/**
	 * @deprecated please use setFrame instead
	 * Set if Drawable needs frame.
	 * @param needFrame
	 */
	public void setNeedFrame(boolean needFrame) {
		this.needFrame = needFrame;
	}
	
	/**
	 * Get if Drawable has frame.
	 * @return
	 *     --- true, has frame;false, does not.
	 */
	public boolean isFramed()
	{
		return this.needFrame;
	}
	
	public Drawable getParent() {
		return parent;
	}
	
	public void setParent(Drawable parent) {
		this.parent = parent;
	}
	
    /** 
     * This method is recursive, it trace back the drawable tree until the root
     */
    public Drawable getRootComposite() {
    	Drawable parent = getParent();
    	
    	if (parent != null) { // it's not root yet, go on
    		parent = parent.getRootComposite();
    		return parent;
    	} else {
    		return this;
    	}
    }
    
	private Drawable parent = null;
}

