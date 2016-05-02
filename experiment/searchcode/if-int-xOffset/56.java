package com.thoughtworks.hp.epromos;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.thoughtworks.hp.R;
import com.thoughtworks.hp.datastore.ProductTable;
import com.thoughtworks.hp.epromos.EPromoFactory.EPromo;
import com.thoughtworks.hp.models.Product;

@SuppressLint("ParserError")
public class ImageCell extends ImageView implements DragSource, DropTarget {
	public boolean mEmpty = true;
	public int mCellNumber = -1;
	public TableLayout tableView;
	public EPromo promotionBeingDisplayed;

	public ImageCell(Context context) {
		super(context);
	}

	public ImageCell(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ImageCell(Context context, AttributeSet attrs, int style) {
		super(context, attrs, style);
	}

	public boolean allowDrag() {
		return !mEmpty;
	}

	public void setDragController(DragController dragger) {
		// Do nothing. We do not need to know the controller object.
	}

	public void onDropCompleted(View target, boolean success) {
		// If the drop succeeds, the image has moved elsewhere.
		// So clear the image cell.
		if (success) {
			showDialog("Deal Added", "Your deal was successfully added to your shopping list");

		}
	}

	private void showDialog(String title, String details) {
		Dialog dialog = new Dialog(this.getContext());
		dialog.setContentView(R.layout.promodialog);
		ImageView image = (ImageView) dialog.findViewById(R.id.imageIndialog);
		image.setBackgroundDrawable(getContext().getResources().getDrawable(
				this.promotionBeingDisplayed.getPromotionImage()));
		TextView text = (TextView) dialog.findViewById(R.id.TextView01);
		ProductTable table = new ProductTable();
		Product product = table.findByBarcodeId(promotionBeingDisplayed.getProductID());
		String productDescription = promotionBeingDisplayed.getProductID();
		if (product != null) {
			productDescription = product.getName();
		}
		text.setText(productDescription);

		if (details != null && !details.trim().equals("")) {
			TextView extrDesc = (TextView) dialog.findViewById(R.id.extraDesc);
			extrDesc.setText(details);
		}

		dialog.setTitle(title);
		dialog.setCancelable(true);
		dialog.show();
	}

	public void onDrop(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo) {
		// Mark the cell so it is no longer empty.
		mEmpty = false;
		int bg = mEmpty ? R.color.cell_empty : R.color.cell_filled;
		setBackgroundResource(bg);

		// The view being dragged does not actually change its parent and switch
		// over to the ImageCell.
		// What we do is copy the drawable from the source view.
		ImageView sourceView = (ImageView) source;
		Drawable d = sourceView.getDrawable();
		if (d != null) {
			this.setImageDrawable(d);
		}
	}

	/**
	 * React to a dragged object entering the area of this DropSpot. Provide the
	 * user with some visual feedback.
	 */
	public void onDragEnter(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo) {
		int bg = mEmpty ? R.color.cell_empty_hover : R.color.cell_filled_hover;
		setBackgroundResource(bg);
	}

	/**
	 * React to something being dragged over the drop target.
	 */
	public void onDragOver(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo) {
	}

	/**
	 * React to a drag
	 */
	public void onDragExit(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo) {
		int bg = mEmpty ? R.color.cell_empty : R.color.cell_filled;
		setBackgroundResource(bg);
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
	public boolean acceptDrop(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo) {
		// An ImageCell accepts a drop if it is empty and if it is part of a
		// grid.
		// A free-standing ImageCell does not accept drops.
		return mEmpty && (mCellNumber >= 0);
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
	public Rect estimateDropLocation(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo, Rect recycle) {
		return null;
	}

	/**
 */
	// Other Methods

	/**
	 * Return true if this cell is empty. If it is, it means that it will accept
	 * dropped views. It also means that there is nothing to drag.
	 * 
	 * @return boolean
	 */

	public boolean isEmpty() {
		return mEmpty;
	}

	/**
	 * Call this view's onClick listener. Return true if it was called. Clicks
	 * are ignored if the cell is empty.
	 * 
	 * @return boolean
	 */

	public boolean performClick() {
		if (!mEmpty) {
			showDialog("Details", promotionBeingDisplayed.getDescription());
			return super.performClick();
		}
		return false;
	}

	/**
	 * Call this view's onLongClick listener. Return true if it was called.
	 * Clicks are ignored if the cell is empty.
	 * 
	 * @return boolean
	 */

	public boolean performLongClick() {
		if (!mEmpty) {
			return super.performLongClick();
		}
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

