/**
 * The Logic Lab
 * @author jpk Aug 28, 2007
 */
package com.tll.client.ui.listing;

import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.tll.client.listing.IListingHandler;
import com.tll.client.listing.IListingOperator;
import com.tll.client.listing.ListingEvent;

/**
 * ListingWidget - Base class for all listing {@link Widget}s in the app.
 * @author jpk
 * @param <R> The row data type.
 * @param <T> the table widget type
 */
public class ListingWidget<R, T extends ListingTable<R>> extends Composite implements Focusable, KeyDownHandler, IListingHandler<R> {

	protected final String listingElementName;

	/**
	 * The listing table.
	 */
	protected final T table;

	/**
	 * Displayed in place of the table when no data rows exist.
	 */
	private Widget noDataRowsWidget;

	/**
	 * The listing navigation bar.
	 */
	protected final ListingNavBar<R> navBar;

	/**
	 * The main "listing" panel containing all widgets comprising this widget.
	 */
	protected final FocusPanel focusPanel = new FocusPanel();

	/**
	 * Wrapped around the listing table enabling vertical scrolling.
	 */
	protected final ScrollPanel portal = new ScrollPanel();

	protected final FlowPanel tableViewPanel = new FlowPanel();
	
	/**
	 * The listing operator
	 */
	private IListingOperator<R> operator;

	/**
	 * The optional row popup.
	 */
	// protected RowContextPopup rowPopup;
	
	/**
	 * Listing and RPC event registrations. 
	 */
	private HandlerRegistration hrListing, hrRpc;

	/**
	 * Constructor
	 * @param listingElementName 
	 * @param table listing table widget
	 * @param navBar optional nav bar
	 */
	public ListingWidget(String listingElementName, T table, ListingNavBar<R> navBar) {
		super();
		
		this.listingElementName = listingElementName;

		tableViewPanel.setStyleName(ListingStyles.css().tableView());

		// portal
		portal.setStyleName(ListingStyles.css().portal());
		tableViewPanel.add(portal);

		// table
		portal.add(table);
		focusPanel.addKeyDownHandler(this);
		this.table = table;

		// generate nav bar
		this.navBar = navBar;
		if(navBar != null) {
			tableViewPanel.add(navBar.getWidget());
		}

		// TODO fix row delegate wiring in listing widget
		// row delegate?
//		final IRowOptionsDelegate rod = getRowOptionsHandler();
//		if(rod != null) rowPopup = new RowContextPopup(2000, table, rod);

		focusPanel.add(tableViewPanel);

		initWidget(focusPanel);
	}

	/**
	 * Sets the operator which is delegated to on behalf of this Widget for
	 * performing listing ops.
	 * @param operator The listing operator
	 */
	public final void setOperator(IListingOperator<R> operator) {
		if(operator == null) throw new IllegalArgumentException();
		if(this.operator != null) {
			// un-bind existing
			assert hrListing != null && hrRpc != null;
			hrListing.removeHandler();
			hrListing = null;
			hrRpc.removeHandler();
			hrRpc = null;
		}
		this.operator = operator;
		operator.setTarget(this);
		this.table.setListingOperator(operator);
		if(navBar != null) navBar.setListingOperator(operator);
		
		// needed to get notified of listing events the operator will fire
		assert hrListing == null && hrRpc == null;
		hrListing = addHandler(this, ListingEvent.TYPE);
	}

	/**
	 * @return The listing operator.
	 */
	public final IListingOperator<R> getOperator() {
		return operator;
	}

	/**
	 * @return The number of rows <em>shown</em> in the listing.
	 */
	public final int getNumRows() {
		return table.getRowCount();
	}

	/**
	 * Physically adds a row.
	 * @param rowData The row data to add
	 */
	public final void addRow(R rowData) {
		table.addRow(rowData);
		if(navBar != null) navBar.increment();
		handleTableVisibility();
	}

	/**
	 * Updates a row at the given rowIndex.
	 * @param rowIndex 0-based row index
	 * @param rowData
	 */
	public final void updateRow(int rowIndex, R rowData) {
		table.updateRow(rowIndex, rowData);
	}

	/**
	 * Physically deletes a row from the listing.
	 * @param rowIndex the 0-based index
	 */
	public final void deleteRow(int rowIndex) {
		table.deleteRow(rowIndex);
		if(navBar != null) navBar.decrement();
		handleTableVisibility();
	}

	/**
	 * Marks a row as deleted or un-deleted.
	 * @param rowIndex the 0-based index
	 * @param markDeleted true/false
	 */
	public final void markRowDeleted(int rowIndex, boolean markDeleted) {
		table.markRowDeleted(rowIndex, markDeleted);
	}

	public final boolean isRowMarkedDeleted(int rowIndex) {
		return table.isRowMarkedDeleted(rowIndex);
	}

	@Override
	public final int getTabIndex() {
		return focusPanel.getTabIndex();
	}

	@Override
	public final void setAccessKey(char key) {
		focusPanel.setAccessKey(key);
	}

	@Override
	public final void setFocus(boolean focused) {
		focusPanel.setFocus(focused);
	}

	@Override
	public final void setTabIndex(int index) {
		focusPanel.setTabIndex(index);
	}

	@Override
	public void onKeyDown(KeyDownEvent event) {
		delegateEvent(table, event);
	}

	public final void setPortalHeight(String height) {
		portal.setHeight(height);
	}
	
	public final void setPortalWidth(String width) {
		portal.setWidth(width);
	}
	
	protected Widget createNoDataRowsWidget() {
		return new Label("Currently, no " + listingElementName + "s exist.");
	}
	
	private void handleTableVisibility() {
		// handle no data rows case
		boolean noDataRows = table.getRowCount() <= 1;
		portal.setVisible(!noDataRows);
		if(noDataRows && noDataRowsWidget == null) {
			// no data rows widget
			noDataRowsWidget = createNoDataRowsWidget();
			noDataRowsWidget.setStyleName(ListingStyles.css().nodata());
			noDataRowsWidget.setVisible(false);
			tableViewPanel.insert(noDataRowsWidget, navBar == null ? 0 : 1);
		}
		if(noDataRowsWidget != null) noDataRowsWidget.setVisible(noDataRows);
	}

	@Override
	public final void onListingEvent(ListingEvent<R> event) {
		table.onListingEvent(event);
		if(navBar != null) navBar.onListingEvent(event);
		handleTableVisibility();
	}
}

