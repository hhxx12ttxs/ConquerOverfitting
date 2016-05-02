package com.thoughtworks.hp.epromos;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import com.thoughtworks.hp.R;

@SuppressLint({ "ParserError", "ViewConstructor" })
public class ShoppingBasket extends ImageView implements DragSource, DropTarget {
	private long shoppingListID;

    public ShoppingBasket(Context context, long shoppingListID) {
		super(context);
		this.shoppingListID = shoppingListID;
	}

	public ShoppingBasket(Context context, AttributeSet attrs, long shoppingListID) {
		super(context, attrs);
		this.shoppingListID = shoppingListID;
	}

	public ShoppingBasket(Context context, AttributeSet attrs, int style, long shoppingListID) {
		super(context, attrs, style);
		this.shoppingListID = shoppingListID;
	}

	public boolean allowDrag() {
		return false;
	}

	public long getShoppingListID() {
		return this.shoppingListID;
	}

	public void setDragController(DragController dragger) {
	}

	@SuppressLint("ParserError")
	public void onDropCompleted(View target, boolean success) {
	}

	public void onDrop(DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo) {
		Log.e("Alert", "Added to Shopping List");

	}

	/**
	 * React to a dragged object entering the area of this DropSpot. Provide the
	 * user with some visual feedback.
	 */
	public void onDragEnter(DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo) {
		setImageDrawable(getResources().getDrawable(R.drawable.list_big));

	}

	/**
	 * React to something being dragged over the drop target.
	 */
	public void onDragOver(DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo) {
	}

	/**
	 * React to a drag
	 */
	public void onDragExit(DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo) {
		setImageDrawable(getResources().getDrawable(R.drawable.list));

	}

	/**
	 * Check if a drop action can occur at, or near, the requested location.
	 * This may be called repeatedly during a drag, so any calls should return
	 * quickly.
	 * 
	 * @param source
	 *            DragSource where the drag started
	 * @param x
	 *            X coordinate of the drop location
	 * @param y
	 *            Y coordinate of the drop location
	 * @param xOffset
	 *            Horizontal offset with the object being dragged where the
	 *            original touch happened
	 * @param yOffset
	 *            Vertical offset with the object being dragged where the
	 *            original touch happened
	 * @param dragView
	 *            The DragView that's being dragged around on screen.
	 * @param dragInfo
	 *            Data associated with the object being dragged
	 * @return True if the drop will be accepted, false otherwise.
	 */
	public boolean acceptDrop(DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo) {
		return true;
	}

	/**
	 * Estimate the surface area where this object would land if dropped at the
	 * given location.
	 * 
	 * @param source
	 *            DragSource where the drag started
	 * @param x
	 *            X coordinate of the drop location
	 * @param y
	 *            Y coordinate of the drop location
	 * @param xOffset
	 *            Horizontal offset with the object being dragged where the
	 *            original touch happened
	 * @param yOffset
	 *            Vertical offset with the object being dragged where the
	 *            original touch happened
	 * @param dragView
	 *            The DragView that's being dragged around on screen.
	 * @param dragInfo
	 *            Data associated with the object being dragged
	 * @param recycle
	 *            {@link Rect} object to be possibly recycled.
	 * @return Estimated area that would be occupied if object was dropped at
	 *         the given location. Should return null if no estimate is found,
	 *         or if this target doesn't provide estimations.
	 */
	public Rect estimateDropLocation(DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo, Rect recycle) {
		return null;
	}

	/**
	 * Return true if this cell is empty. If it is, it means that it will accept
	 * dropped views. It also means that there is nothing to drag.
	 * 
	 * @return boolean
	 */

	public boolean isEmpty() {
		return true;
	}

	/**
	 * Call this view's onClick listener. Return true if it was called. Clicks
	 * are ignored if the cell is empty.
	 * 
	 * @return boolean
	 */

	public boolean performClick() {
		return super.performClick();
	}

	/**
	 * Call this view's onLongClick listener. Return true if it was called.
	 * Clicks are ignored if the cell is empty.
	 * 
	 * @return boolean
	 */

	public boolean performLongClick() {

		return false;
	}

	/**
	 * Show a string on the screen via Toast if DragActivity.Debugging is true.
	 * 
	 * @param msg
	 *            String
	 * @return void
	 */

	public void toast(String msg) {
		if (!DragActivity.Debugging)
			return;
	}

}

