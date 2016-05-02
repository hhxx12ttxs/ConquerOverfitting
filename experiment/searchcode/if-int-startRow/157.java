package org.imogene.rcp.core.widget;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.imogene.common.data.Synchronizable;
import org.imogene.common.data.handler.EntityHandler;
import org.imogene.rcp.core.ImogPlugin;
import org.imogene.rcp.core.view.IEntityForm;
import org.imogene.rcp.core.widget.table.PaginatedTable;
import org.imogene.rcp.core.widget.table.SynchronizableTableContentProvider;
import org.imogene.rcp.core.widget.table.TableNavigationBar;
import org.imogene.rcp.core.wrapper.CoreMessages;

/**
 * Widget to display a relation field with cardinality = 1
 * as a paginated list
 * @author Medes-IMPS
 */
public class RelationPaginatedCombo extends Composite {
	
	private Logger logger = Logger.getLogger("org.imogene.rcp.core.widget.RelationPaginatedCombo");

	/* paginated combo */
	private String selectedId;
	private Text selectedEntityText;
	private Label openListLabel;
	private Image openListIcon = ImogPlugin.getImageDescriptor("icons/downarrow.png").createImage();
	private PaginatedListPopUp paginatedList;
	
	/* view entity button */
	private Label openEntityLabel;
	private Image openEntityIcon = ImogPlugin.getImageDescriptor("icons/info.png").createImage();
	
	/* add entity button */
	private Label addLabel;
	private Image addIcon;
	
	private EntityHandler handler;
	private String formID = null;
	private FormToolkit toolkit;
	
	
	
	
	/**
	 * Creates the Paginated combo widget and corresponding
	 * action buttons
	 * @param parent parent composite
	 * @param toolkit FormToolkit
	 * @param className related entity class name
	 * @param formId related entity form Id
	 */
	public RelationPaginatedCombo(Composite parent, FormToolkit toolkit, String className, String formId, String sortField, boolean sortOrder){
		super(parent, SWT.NONE);
		this.formID = formId;
		this.toolkit = toolkit;
		
		this.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		
		handler = ImogPlugin.getDefault().getDataHandlerManager().getHandler(className);

		GridLayout layout = new GridLayout(3, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 2;
		layout.verticalSpacing = 0;
		setLayout(layout);
		
		/* paginated combo */
		
		Composite comboListComp = toolkit.createComposite(RelationPaginatedCombo.this, SWT.NONE);
		GridLayout comboLayout = new GridLayout(2, false);
		comboLayout.marginWidth = 0;
		comboLayout.marginHeight = 0;
		comboLayout.horizontalSpacing = 0;
		comboLayout.verticalSpacing = 0;
		comboListComp.setLayout(comboLayout);
		comboListComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER));
		
		selectedEntityText = toolkit.createText(comboListComp, null);
		selectedEntityText.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.FILL_HORIZONTAL));
		selectedEntityText.setEditable(false);
		
		openListLabel = toolkit.createLabel(comboListComp, null);
		openListLabel.setImage(openListIcon);
		openListLabel.setToolTipText(CoreMessages.getString("tooltip_open_combo"));
		openListLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		openListLabel.addMouseListener(new MouseAdapter(){
		   @Override
		   public void mouseUp(MouseEvent e) {    
			   showComboPaginatedListList();
		   }
		});
		
		paginatedList = new PaginatedListPopUp(toolkit, comboListComp.getShell(), sortField, sortOrder, this);

		/* action buttons */
		
		openEntityLabel = toolkit.createLabel(RelationPaginatedCombo.this, null);
		openEntityLabel.setImage(openEntityIcon);
		openEntityLabel.setToolTipText(CoreMessages.getString("tooltip_view"));
		openEntityLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
		openEntityLabel.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseUp(MouseEvent e) {
				if(formID != null && getSelected()!=null){
					try {
						IWorkbenchPage page = ImogPlugin.getDefault()
								.getWorkbench().getActiveWorkbenchWindow()
								.getActivePage();
						IViewPart lview = page.showView(formID, getSelected()
								.getId(), IWorkbenchPage.VIEW_ACTIVATE);
						((IEntityForm) lview).setInput(getSelected());
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		});
		hideLabelButton(openEntityLabel, true);		

	}
	
	/**
	 * Show the paginated list under the text
	 * widget that displays the selected entity
	 */
	private void showComboPaginatedListList() {	   
		   paginatedList.getPaginatedListComposite().refreshTable();
		   paginatedList.open();
	}
	
	/**
	 * Get the selected entity
	 * @return the selected entity
	 */
	public Synchronizable getSelected(){
		if (selectedId!=null)
			return handler.loadEntity(selectedId, null);
		else
			return null;
	}
	
	/**
	 * Nothing selected
	 */
	public void nothingSelected() {
		selectedId = null;
		selectedEntityText.setText("");
		if (formID!=null) {
			hideLabelButton(openEntityLabel, true);
			//this.layout(true, true);
		}
	}
	
	/**
	 * Select the item that match the specified entity
	 * @param entity the entity selected
	 */
	public void select(Synchronizable entity){
		if (entity!=null) {
			selectedId = entity.getId();
			selectedEntityText.setText(entity.getDisplayValue());
			if (formID!=null) {
				hideLabelButton(openEntityLabel, false);
				//this.layout(true, true);
			}
		}
	}
	
	/**
	 * Select the item that match this entity id
	 */
	public void select(String id){
		if (id != null) {
			Synchronizable entity = handler.loadEntity(selectedId, null);
			select(entity);
		}
	}

	
	@Override
	public void dispose() {
		openEntityIcon.dispose();
		openListIcon.dispose();
		if (addLabel!=null)
			addIcon.dispose();
		super.dispose();
	}
	
	/**
	 * Hides a label button
	 * @param label the button to hide
	 * @param hide true if the button should be hidden
	 */
	public void hideLabelButton(Label label, boolean hide) {		
		label.setVisible(!hide);
		//((GridData)label.getLayoutData()).exclude = hide;
	}
	

	 @Override
	 public boolean isFocusControl() {  
		 return (super.isFocusControl());
	 }

	@Override
	public void setEnabled(boolean enabled) {
		hideLabelButton(openListLabel, !enabled);
		if (addLabel!=null)
			hideLabelButton(addLabel, !enabled);
		//this.layout(true, true);
	}
	
	
	/**
	 * Add a button to be able to create a related entity
	 * @param listener
	 */
	public void addCreateRelationEntityButton(MouseListener listener) {
		
		addLabel = toolkit.createLabel(RelationPaginatedCombo.this, null);
		addIcon = ImogPlugin.getImageDescriptor("icons/edit_add.png").createImage();
		addLabel.setImage(addIcon);
		addLabel.setToolTipText(CoreMessages.getString("tooltip_create_related"));
		addLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
		
		addLabel.addMouseListener(listener);				
	}
	
	
	
/*  
 * -----------------------------------------
 *              PRIVATE CLASSES
 * 	----------------------------------------
 */
	
	
	/**
	 * Popup window to display the paginated list
	 * under the selected entity text widget
	 * @author Medes-IMPS
	 */
	private class PaginatedListPopUp {
		
		Shell  shell;
		PaginatedListComposite paginatedListComp;
		Control nextFocus;
		
		public PaginatedListPopUp(FormToolkit toolkit, Shell parent, String sortField, boolean sortOrder, Control nextFocus) {
			this(toolkit, parent, sortField, sortOrder);
			this.nextFocus = nextFocus;
		}
		
		public PaginatedListPopUp(FormToolkit toolkit, Shell parent, String sortField, boolean sortOrder) {
			
			shell = new Shell(parent, SWT.ON_TOP);
			GridLayout compLayout = new GridLayout(1,false);
			compLayout.marginHeight = 0;
			compLayout.marginWidth = 0;
			compLayout.verticalSpacing = 0;
			compLayout.horizontalSpacing = 0;
			shell.setLayout(compLayout);			
			
			// close dialog if user selects outside of the shell
			shell.addListener(SWT.Deactivate, new Listener() {
				public void handleEvent(Event e){	
					shell.setVisible(false);
				}
			});

			paginatedListComp = new PaginatedListComposite(toolkit, shell, sortField, sortOrder);
			GridData paginatedListCompData = new GridData(GridData.FILL_HORIZONTAL);
			paginatedListCompData.widthHint = selectedEntityText.getSize().x + openListLabel.getSize().x + 2 ;	
			paginatedListComp.setLayoutData(paginatedListCompData);
		}
		
		/**
		* Opens the Popup window
		*/
		public void open() {

			Point pointSize = selectedEntityText.getSize();
			Point pointLoc = selectedEntityText.toDisplay(0, 0);
			shell.setBounds(pointLoc.x - 2, pointLoc.y + pointSize.y - 3, pointSize.x, 100);
			shell.open();

			Display display = shell.getDisplay();
			while (!shell.isDisposed() && shell.isVisible()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
		}
		
		/**
		 * 
		 */
		public void close () {			
			shell.setVisible(false);
			if(nextFocus!=null)
				nextFocus.setFocus();
			//MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Debug", "paginated shell closed");
		}
		
		/**
		 * 
		 * @return
		 */
		public PaginatedListComposite getPaginatedListComposite() {
			return paginatedListComp;
		}
		
	}
	
	
	
	/**
	 * Paginated list
	 * @author Medes-IMPS
	 */
	private class PaginatedListComposite extends Composite implements PaginatedTable {
		
		/* Table */
		private TableViewer tableViewer;
		private TableNavigationBar navbar;

		private int maxRows = 10;
		private int startRow = 0;
		private String sortProperty;
		private boolean sortOrder = true;
		private int totalNbOfRows = 0;
		
		
		public PaginatedListComposite(FormToolkit toolkit, Composite parent, String sortField, boolean sortOrder) {
			super(parent, SWT.NONE);
			this.sortProperty = sortField;
			this.sortOrder = sortOrder;
			
			if (toolkit == null)
				toolkit = new FormToolkit(parent.getDisplay());
		
			GridLayout viewLayout = new GridLayout(1, false);
			viewLayout.marginWidth =2;
			viewLayout.marginHeight = 2;
			viewLayout.horizontalSpacing = 0;
			viewLayout.verticalSpacing = 0;
			this.setLayout(viewLayout);
			
			this.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
			
			/* table initialization */
			Table table = toolkit.createTable(PaginatedListComposite.this, SWT.FULL_SELECTION);
			table.setLinesVisible(false);
			table.setHeaderVisible(false);
			GridData tableGridData = new GridData(GridData.FILL_HORIZONTAL);
			tableGridData.heightHint = 50;
			table.setLayoutData(tableGridData);
	
			/* table viewer initialization */
			tableViewer = new TableViewer(table);
			// to remove selection when clicked outside selectable row
			tableViewer.getTable().addMouseListener(new MouseAdapter() {
				public void mouseDown(MouseEvent e) {
					if (tableViewer.getTable().getItem(new Point(e.x, e.y)) == null) {
						tableViewer.setSelection(new StructuredSelection());
					}
				}
			});
			tableViewer.setContentProvider(new SynchronizableTableContentProvider());
			tableViewer.addDoubleClickListener(new IDoubleClickListener() {
				public void doubleClick(DoubleClickEvent event) {
					selectEntity(event);
				}
			});
	
			/* table column definition */	
			final TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
			column.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					if (element instanceof Synchronizable) {
						String value = ((Synchronizable) element).getDisplayValue();
						if (value != null)
							return value;
					}
					return new String();
				}
			});
			
			/* navigation bar initialization */
			navbar = new TableNavigationBar(this, toolkit);
			navbar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
			/* table data filling */
			//updateTableData(startRow, maxRows, sortProperty, sortOrder);
			//packColumns();		

		}
		
		
		/**
		 * Open the Entity selected in the table.
		 * @param event the open event
		 */
		private void selectEntity(DoubleClickEvent event) {
			if (event.getSelection() instanceof IStructuredSelection) {
				Object selectedObject = ((IStructuredSelection) event.getSelection()).getFirstElement();
				try {
					if (selectedObject instanceof Synchronizable) {
						Synchronizable entity = (Synchronizable)selectedObject;
						select(entity);
						paginatedList.close();
					}
				} catch (Exception ex) {
					logger.error(ex.getMessage());
				}
			}
		}
		
		/**
		 * Updates the table entity list content
		 * @param startRow page start row
		 * @param maxRows page max number of rows
		 * @param sortProperty property used for entity list sorting
		 * @param sortOrder true if ascending
		 */
		private void updateTableData(final int startRow, final int maxRows,
				String sortProperty, Boolean sortOrder) {

			this.startRow = startRow;
			List<Synchronizable> entities = null;

			/* get data 
			if (searchCriterions != null) {
				entities = (List<Synchronizable>) handler.loadEntities(startRow,
						maxRows, sortProperty, sortOrder, null);
				totalNbOfRows = handler.countAll();
			} else {*/
			
				try {
					entities = (List<Synchronizable>) handler.loadEntities(startRow, maxRows, sortProperty, sortOrder, null);
				} catch (Exception ex) {
					ex.printStackTrace();
					logger.error(ex.getMessage());
				}
				totalNbOfRows = handler.countAll();
			//}

			if (entities != null) {

				int srcRowCount = entities.size();

				/* push data into table */
				tableViewer.setInput(entities);

				/* update the navigation bar information */
				int totalNbOfPages = 0;
				if ((totalNbOfRows % maxRows) == 0)
					totalNbOfPages = totalNbOfRows / maxRows;
				else
					totalNbOfPages = totalNbOfRows / maxRows + 1;
				if (totalNbOfPages==0)
					totalNbOfPages=1;

				int currentPageNb = 0;
				if ((startRow % maxRows) == 0)
					currentPageNb = (startRow / maxRows) + 1;
				else
					currentPageNb = (startRow / maxRows) + 2;

				navbar.setTotalNbOfPageText(String.valueOf(totalNbOfPages));
				navbar.setPageNbText(String.valueOf(currentPageNb));

				if (startRow == 0 && srcRowCount == 0)
					navbar.setRowStatusText("0 - 0");
				else
					navbar.setRowStatusText((startRow + 1) + " - "
							+ (startRow + srcRowCount));

				/* synchronize the navigation bar buttons */
				boolean isLastPage = false;
				if (currentPageNb == totalNbOfPages)
					isLastPage = true;
				navbar.enableGoToFirstButton(startRow > 0);
				navbar.enableGoToPrevButton(startRow > 0);
				navbar.enableGoToNextButton(!isLastPage);
				navbar.enableGoToLastButton(!isLastPage);

				navbar.layout(true, true);
			}
		}
		
		

		/**
		 * Packs the columns
		 */
		public void packColumns() {
			for (TableColumn column : tableViewer.getTable().getColumns()) {
				column.pack();
			}
		}

		@Override
		public boolean setFocus() {
			return tableViewer.getTable().setFocus();
		}
		
		/**
		 * Refresh the table content
		 */
		public void refreshTable() {
			updateTableData(startRow, maxRows, sortProperty, sortOrder);
			packColumns();
		}

		public int getMaxRows() {
			return maxRows;
		}

		public int getStartRow() {
			return startRow;
		}

		public int getTotalNbOfRows() {
			return totalNbOfRows;
		}

		public void setStartRow(int row) {
			startRow = row;
		}
		
		public Composite getContainerComposite() {
			return this;
		}
		
	}
		
}

