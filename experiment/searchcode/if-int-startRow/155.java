package org.imogene.web.gwt.client.ui.field.paginatedList;

import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.imogene.gwt.widgets.client.dynaTable.DynaTableNLS;
import org.imogene.web.gwt.client.i18n.BaseNLS;
import org.imogene.web.gwt.client.ui.field.MainFieldsUtil;
import org.imogene.web.gwt.client.ui.field.paginatedList.ImogPaginatedListBoxDataProvider.RowDataAcceptor;
import org.imogene.web.gwt.common.entity.ImogBean;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * This class implements a paginated list of bean instances that are retrieved by using 
 * asynchronous call to the database.
 * @author MEDES-IMPS
 */
public class ImogPaginatedList extends Composite implements ClickHandler {


	/* listbox properties */
	protected List<ImogBean> values = new Vector<ImogBean>();
	protected ListBox box;
	private Image clearImage;
	private int itemByPage=10;
	
	/* pagination properties */
	private int startRow = 0;
	private NavBar navbar;
	private RowDataAcceptor acceptor = new RowDataAcceptorImpl();	
	private ImogPaginatedListBoxDataProvider provider;
	private MainFieldsUtil mainFieldsUtil;
	private TextBox valueFilter;	
	private PushButton filterButton;
	
	/* listeners */
	private HashSet<ClickHandler> listeners = new HashSet<ClickHandler>();


	
	/**
	 * Simple constructor
	 */
	public ImogPaginatedList(ImogPaginatedListBoxDataProvider provider, MainFieldsUtil mainFieldsUtil, boolean multipleSelect){
		super();
		this.provider = provider;		
		this.mainFieldsUtil = mainFieldsUtil;
		
		VerticalPanel vertical = new VerticalPanel();
		vertical.setSpacing(0);
		vertical.setStyleName("ImogListBox-PopupPanel-content");
		
		/* the list box */
		box  = new ListBox(multipleSelect);
		box.setVisibleItemCount(itemByPage);
		box.addClickHandler(this);
		box.setStyleName("ImogListBox-Listbox");
		vertical.add(box);
		
		/* the navigation panel */
		HorizontalPanel navbarPanel = new HorizontalPanel();
		navbarPanel.setSpacing(0);
		navbarPanel.setWidth("100%");
		
		/* the navigation bar */
		navbar = new NavBar();		
		navbarPanel.add(navbar);
		navbarPanel.setCellHorizontalAlignment(navbar, HasHorizontalAlignment.ALIGN_LEFT);
		
		/* the clear image*/	
		clearImage = new Image(GWT.getModuleBaseURL()+ "images/icon_clear.gif");
		clearImage.addClickHandler(this);	
		navbarPanel.add(clearImage);
		navbarPanel.setCellHorizontalAlignment(clearImage, HasHorizontalAlignment.ALIGN_CENTER);
		navbarPanel.setCellVerticalAlignment(clearImage, HasVerticalAlignment.ALIGN_MIDDLE);
		
		vertical.add(navbarPanel);		
		
		/* the text box for the full text filter */
		vertical.add(createFilterField());
		initWidget(vertical);
		//refresh();
	}
	
	
	
	/**
	 * Refresh the list by doing a request to the server
	 */
	public void fillList(){
		/* Disable buttons temporarily to stop 
		 the user from running off the end. */
		navbar.gotoFirst.setEnabled(false);
		navbar.gotoPrev.setEnabled(false);
		navbar.gotoNext.setEnabled(false);
		navbar.gotoLast.setEnabled(false);
		navbar.pageNb.setReadOnly(true);
		filterButton.setEnabled(false);
		clearImage.setVisible(false);
		navbar.isWaiting();
		provider.updateRowData(startRow, itemByPage, acceptor);		
	}
	
	/**
	 * Get the maximum number that 
	 * could by displayed in a page.
	 * @return The maximum number of items in a page
	 */
	public int getItemByPage() {
		return itemByPage;
	}

	/**
	 * Sets the maximum number of item that
	 * could be displayed in a page.
	 * @param itemByPage maximum number of item in page
	 */
	public void setItemByPage(int itemByPage) {
		this.itemByPage = itemByPage;
	}
	
	
	/**
	 * Gets the value of the listbox that is located at a given index
	 * @param i the index for which the value shall be retrieved
	 * @return the value of the listbox that is located at index i
	 */
/*	public ImogBean getValue(int i){
		if(i<0)
			return null;
		return values.get(i);
	}*/
	
	/**
	 * Gets the lisbox selected value
	 * @return the selected value of the listbox
	 */
/*	public ImogBean getValue() {		
		return getValue(box.getSelectedIndex());
	}*/
	
	/**
	 * Gets the display value of the listbox selected value
	 * @return the display value of the listbox selected value
	 */
/*	public String getDisplayValue() {
		return box.getItemText(box.getSelectedIndex());
	}*/
	
	public ListBox getListBox() {
		return box;
	}
	
	public Image getClearImage() {
		return clearImage;
	}
	
	/**
	 * Creates the Widget that permits to enter data for the full text filter.
	 * @return the widget
	 */
	private Widget createFilterField() {
		
		Grid grid = new Grid(1, 2);
		
		/* text box to fix the filter */
		valueFilter = new TextBox();
		valueFilter.addKeyPressHandler(new KeyPressHandler() {
		
			public void onKeyPress(KeyPressEvent event) {
				
				if (event.getCharCode() == (char) KeyCodes.KEY_ENTER) {
					provider.fullTextSearch(valueFilter.getText());
					startRow=0;
					fillList();										
				}	
			}
		});			
		valueFilter.setReadOnly(false);		
		grid.setWidget(0, 0, valueFilter);
		valueFilter.setStyleName("Filter-TextBox");
		
		/* button filter */
		filterButton = new PushButton(BaseNLS.constants().button_search());
		filterButton.setStyleName("ImogListBox-Button-Search");
		filterButton.addClickHandler(this);
		grid.setWidget(0, 1, filterButton);
				
		return grid;
	}
	

	/*
	 * (non-Javadoc)
	 * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
	 */
	public void onClick(ClickEvent event) {
		if(event.getSource().equals(filterButton)){			
			provider.fullTextSearch(valueFilter.getText());
			startRow=0;
			fillList();
		}
		if(event.getSource().equals(box)){
			for(ClickHandler listener:listeners){
				listener.onClick(event);
			}
		}
		if(event.getSource().equals(clearImage)){
			box.setSelectedIndex(-1);			
			for(ClickHandler listener:listeners){
				listener.onClick(event);
			}
		}
	}		

	/**
	 * Adds a click listener, notified 
	 * when an item is selected in the list box.
	 * @param listener The listener to add
	 */
	public void addClickListener(ClickHandler listener){
		listeners.add(listener);
	}
	
	/**
	 * Removes a click listener
	 * @param listener The listener to remove
	 */
	public void removeClickListener(ClickHandler listener){
		listeners.remove(listener);
	}



	/** INTERNAL CLASS **
	 * Private class that creates a navigation bar.
	 */
	private class NavBar extends Composite implements ClickHandler, KeyPressHandler {
		
		public final HorizontalPanel bar = new HorizontalPanel();
		public final PushButton gotoFirst = new PushButton("<<", this);
		public final PushButton gotoNext = new PushButton(">", this);
		public final PushButton gotoPrev = new PushButton("<", this);
		public final PushButton gotoLast = new PushButton(">>", this);
		public final TextBox pageNb = new TextBox();
		public final Label pageTotalNb = new Label();
		public int rowTotalNb = 0;	// total number of rows without pagination
		public final HorizontalPanel pagePanel = new HorizontalPanel();	
		
		/**
		 * Simple constructor
		 */
		public NavBar() {

			initWidget(bar);
			bar.setStyleName("ImogListBox-navbar");	
			
			// Reward Buttons
			HorizontalPanel buttonsFirst = new HorizontalPanel();
			buttonsFirst.setSpacing(2);
			gotoFirst.setStyleName("DynaTable-navbar-button");
			gotoPrev.setStyleName("DynaTable-navbar-button");
			buttonsFirst.add(gotoFirst);
			buttonsFirst.add(gotoPrev);
			
			// Page status
			HorizontalPanel pagesStatus = new HorizontalPanel();	

			Label pageLabel = new Label(DynaTableNLS.constants().navbar_page());
			pageLabel.setStyleName("DynaTable-status-text");
			pagesStatus.add(pageLabel);		
			pageNb.setText("0");
			pageNb.setStyleName("DynaTable-status-textbox");
			pageNb.setVisibleLength(4);
			pageNb.addKeyPressHandler(this);
			pagesStatus.add(pageNb);				
			Label pageOf = new Label(DynaTableNLS.constants().navbar_of());
			pageOf.setStyleName("DynaTable-status-text");
			pagesStatus.add(pageOf);			
			pageTotalNb.setText("0");
			pageTotalNb.setStyleName("DynaTable-status-text");
			pagesStatus.add(pageTotalNb);
									
			// Forward Buttons
			HorizontalPanel buttonsLast = new HorizontalPanel();
			buttonsLast.setSpacing(2);
			gotoNext.setStyleName("DynaTable-navbar-button");
			gotoLast.setStyleName("DynaTable-navbar-button");
			buttonsLast.add(gotoNext);
			buttonsLast.add(gotoLast);						
									
			pagePanel.add(buttonsFirst);
			pagePanel.add(pagesStatus);
			pagePanel.add(buttonsLast);			
			bar.add(pagePanel);
			
			pagePanel.setCellVerticalAlignment(buttonsFirst, HasVerticalAlignment.ALIGN_MIDDLE);
			pagePanel.setCellVerticalAlignment(buttonsLast, HasVerticalAlignment.ALIGN_MIDDLE);
			pagePanel.setCellVerticalAlignment(pagesStatus, HasVerticalAlignment.ALIGN_MIDDLE);			
			buttonsFirst.setCellVerticalAlignment(gotoFirst, HasVerticalAlignment.ALIGN_MIDDLE);
			buttonsFirst.setCellVerticalAlignment(gotoPrev, HasVerticalAlignment.ALIGN_MIDDLE);		
			pagesStatus.setCellVerticalAlignment(pageLabel, HasVerticalAlignment.ALIGN_MIDDLE);
			pagesStatus.setCellVerticalAlignment(pageNb, HasVerticalAlignment.ALIGN_MIDDLE);
			pagesStatus.setCellVerticalAlignment(pageOf, HasVerticalAlignment.ALIGN_MIDDLE);
			pagesStatus.setCellVerticalAlignment(pageTotalNb, HasVerticalAlignment.ALIGN_MIDDLE);		
			buttonsLast.setCellVerticalAlignment(gotoNext, HasVerticalAlignment.ALIGN_MIDDLE);
			buttonsLast.setCellVerticalAlignment(gotoLast, HasVerticalAlignment.ALIGN_MIDDLE);
			
			bar.setCellHorizontalAlignment(pagePanel, HasHorizontalAlignment.ALIGN_LEFT);
			bar.setCellVerticalAlignment(pagePanel, HasVerticalAlignment.ALIGN_TOP);		

			// Initialize prev & first button to disabled.
			gotoPrev.setEnabled(false);
			gotoFirst.setEnabled(false);		
		}
		
		/*
		 * (non-Javadoc)
		 * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
		 */
		public void onClick(ClickEvent event) {			
			if (event.getSource() == gotoNext) {
				startRow += itemByPage;
				fillList();
			} else if (event.getSource() == gotoPrev) {
				startRow -= itemByPage;
				if (startRow < 0) {
					startRow = 0;
				}
				fillList();
			} else if (event.getSource() == gotoFirst) {
				startRow = 0;
				fillList();
			} else if (event.getSource() == gotoLast) {
				int pageNb = Integer.valueOf(pageTotalNb.getText());
				startRow = rowTotalNb - (itemByPage - ((pageNb*itemByPage)-rowTotalNb));
				fillList();
			}
		}
		
		
		

		/*
		 * (non-Javadoc)
		 * @see com.google.gwt.event.dom.client.KeyPressHandler#onKeyPress(com.google.gwt.event.dom.client.KeyPressEvent)
		 */
		public void onKeyPress(KeyPressEvent event) {
			
			if (event.getSource() == pageNb) {
				
				if ((!Character.isDigit(event.getCharCode())) 
						//&& (keyCode != (char) KEY_TAB)
						&& (event.getCharCode() != (char) KeyCodes.KEY_BACKSPACE)
						&& (event.getCharCode() != (char) KeyCodes.KEY_DELETE) 
						&& (event.getCharCode() != (char) KeyCodes.KEY_ENTER) 
						//&& (keyCode != (char) KEY_HOME) 
						&& (event.getCharCode() != (char) KeyCodes.KEY_END)
						&& (event.getCharCode() != (char) KeyCodes.KEY_LEFT) 
						//&& (keyCode != (char) KEY_UP)
						//&& (keyCode != (char) KEY_DOWN) 
						&& (event.getCharCode() != (char) KeyCodes.KEY_RIGHT)) {

					((TextBox)event.getSource()).cancelKey();					
				}
				else {
					if (event.getCharCode() == (char) KeyCodes.KEY_ENTER) {
						
						int pageNum = Integer.valueOf(pageNb.getText());
						int pageNb = Integer.valueOf(pageTotalNb.getText());							
						if (pageNum<=pageNb) {							
							int rowPerPage = itemByPage;							
							int newStartRow = ((pageNum-1)*rowPerPage);
							if (newStartRow!=startRow){
								startRow = newStartRow;						
								fillList();									
							}						
						}					
					}					
				}
			}			
		}
		
		public void isWaiting() {
			Image img = new Image(GWT.getModuleBaseURL()+ "images/loading.gif");
			pagePanel.add(img);
			pagePanel.setCellVerticalAlignment(img, HasVerticalAlignment.ALIGN_MIDDLE);			
		}
		
		public void stopWaiting() {
			pagePanel.remove(3);
		}		
	}
	
	/** INTERNAL CLASS **
	 * Implementation of a row data acceptor that populates the 
	 * list box with main fields display of the the entities 
	 */
	private class RowDataAcceptorImpl implements RowDataAcceptor{
		
		/*
		 * (non-Javadoc)
		 * @see org.imogene.web.gwt.client.ui.field.paginatedList.ImogenePaginatedListBoxDataProvider.RowDataAcceptor#accept(int, org.imogene.web.gwt.common.entity.ImogBean[], int)
		 */
		public void accept(int startRow, ImogBean[] array, int totalNbOfRows) {
			
			if(array!=null) {
				
				int srcRowCount = array.length;
				box.clear();		
				values.clear();
				for(int i=0; i<srcRowCount; i++){
					box.addItem(mainFieldsUtil.getDisplayValue(array[i]), array[i].getId());
					values.add(array[i]);
				}
				
				boolean isLastPage = false;
				if(srcRowCount<itemByPage) isLastPage = true;
				
				/* Update navbar information */
				int totalNbOfPages = 0;			
				if ((totalNbOfRows % itemByPage)==0)
					totalNbOfPages = totalNbOfRows / itemByPage;
				else
					totalNbOfPages = totalNbOfRows / itemByPage + 1;	
				
				int currentPageNb = 0;			
				if ((startRow % itemByPage)==0)
					currentPageNb = (startRow / itemByPage) + 1;
				else
					currentPageNb = (startRow / itemByPage) + 2;		
							
				navbar.rowTotalNb= totalNbOfRows;
				navbar.pageTotalNb.setText(String.valueOf(totalNbOfPages));
				navbar.pageNb.setText(String.valueOf(currentPageNb));	
				navbar.pageNb.setReadOnly(false);


				/* Synchronize the navigation buttons. */				
				navbar.gotoFirst.setEnabled(startRow > 0);
				navbar.gotoPrev.setEnabled(startRow > 0);
				navbar.gotoNext.setEnabled(!isLastPage);
				navbar.gotoLast.setEnabled(!isLastPage);	
				filterButton.setEnabled(true);
				clearImage.setVisible(true);
			}
			navbar.stopWaiting();
		}

		/*
		 * (non-Javadoc)
		 * @see org.imogene.web.gwt.client.ui.field.paginatedList.ImogenePaginatedListBoxDataProvider.RowDataAcceptor#failed(java.lang.Throwable)
		 */
		public void failed(Throwable caught) {
			navbar.stopWaiting();
		}		
		
		/*
		 * (non-Javadoc)
		 * @see org.imogene.web.gwt.client.ui.field.paginatedList.ImogPaginatedListBoxDataProvider.RowDataAcceptor#acceptEmpty()
		 */
		public void acceptEmpty() {
			box.clear();
			navbar.stopWaiting();
		}
	}
	
}

