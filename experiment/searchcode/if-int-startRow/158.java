package org.imogene.rcp.core.widget.table;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.imogene.rcp.core.wrapper.CoreMessages;




/**
 * Table navigation bar
 * @author Medes-IMPS
 */
public class TableNavigationBar extends Composite implements MouseListener, KeyListener {

	private PaginatedTable paginatedTable;
	
	private Button gotoFirst;
	private Button gotoNext;
	private Text pageNbText;
	private Button gotoPrev ;
	private Button gotoLast;	
	private Label pageTotalNbLabel;
	private Label rowStatusLabel;			
	
	private Color labelColor;

	
	/**
	 * Constructor
	 * @param entityListView EntityListPart of an entity
	 * @param toolkit FormToolkit
	 */
	public TableNavigationBar(PaginatedTable paginatedTable, FormToolkit toolkit) {
		super(paginatedTable.getContainerComposite(), SWT.NONE);
		
		this.paginatedTable = paginatedTable;
		
		this.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		
		GridLayout nblayout = new GridLayout(2,false);
		nblayout.marginWidth =0;
		nblayout.marginHeight =0;
		nblayout.horizontalSpacing = 0;
		nblayout.verticalSpacing = 0;
		this.setLayout(nblayout);		
		
		createColors(toolkit);
		createPageNavigationComposite(toolkit);
		createItemCountComposite(toolkit);
	}
	
	/**
	 * Create composite for the page navigation part
	 * of the navigation bar
	 */
	private void createPageNavigationComposite(FormToolkit toolkit){
		
		Composite pageNavComp = toolkit.createComposite(this);
		GridLayout pageNavComplayout = new GridLayout(8,false);
		pageNavComplayout.marginWidth =0;
		pageNavComplayout.marginHeight =0;
		pageNavComplayout.horizontalSpacing = 3;
		pageNavComplayout.verticalSpacing = 0;
		pageNavComp.setLayout(pageNavComplayout);	
		pageNavComp.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
		
		gotoFirst = toolkit.createButton(pageNavComp, "<<", SWT.PUSH);
		gotoFirst.addMouseListener(TableNavigationBar.this);
		gotoFirst.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
		gotoFirst.setEnabled(false);
		
		gotoPrev = toolkit.createButton(pageNavComp, "<", SWT.PUSH);
		gotoPrev.addMouseListener(TableNavigationBar.this);
		gotoPrev.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
		gotoPrev.setEnabled(false);
		
		Label pageNbLabel = toolkit.createLabel(pageNavComp, CoreMessages.getString("navbar_page"));
		pageNbLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
		pageNbLabel.setForeground(labelColor);
		
		pageNbText = toolkit.createText(pageNavComp,"");
		GridData pageNbTextLayoutData = new GridData(GridData.VERTICAL_ALIGN_CENTER);
		pageNbTextLayoutData.widthHint = 40;
		pageNbText.setLayoutData(pageNbTextLayoutData);
		pageNbText.addKeyListener(this);
		
		Label pageNbOfLabel = toolkit.createLabel(pageNavComp, CoreMessages.getString("navbar_of"));
		pageNbOfLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
		pageNbOfLabel.setForeground(labelColor);
		
		pageTotalNbLabel = toolkit.createLabel(pageNavComp, "");
		pageTotalNbLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
		pageTotalNbLabel.setForeground(labelColor);
		
		gotoNext = toolkit.createButton(pageNavComp, ">", SWT.PUSH);
		gotoNext.addMouseListener(TableNavigationBar.this);
		gotoNext.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
		
		gotoLast = toolkit.createButton(pageNavComp, ">>", SWT.PUSH);
		gotoLast.addMouseListener(TableNavigationBar.this);
		gotoLast.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
	}
	
	/**
	 * Create composite for the item counting part
	 * of the navigation bar
	 */
	private void createItemCountComposite(FormToolkit toolkit){
		
		Composite itemCountComp = toolkit.createComposite(this);
		GridLayout itemCountComplayout = new GridLayout(1,false);
		itemCountComplayout.marginWidth =0;
		itemCountComplayout.marginHeight =0;
		itemCountComplayout.horizontalSpacing = 0;
		itemCountComplayout.verticalSpacing = 0;
		itemCountComp.setLayout(itemCountComplayout);	
		itemCountComp.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.HORIZONTAL_ALIGN_END | GridData.FILL_HORIZONTAL));
		
		rowStatusLabel = toolkit.createLabel(itemCountComp, "");
		rowStatusLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
		rowStatusLabel.setForeground(labelColor);		
	}
	

	@Override
	public void mouseUp(MouseEvent e) {
		
		int startRow = paginatedTable.getStartRow();
		int maxRows = paginatedTable.getMaxRows();
		int totalNbOfRows = paginatedTable.getTotalNbOfRows();
		
		if (e.widget.equals(gotoFirst)) {
			gotoFirst.setFocus();
			startRow = 0;
		}
		else if (e.widget.equals(gotoNext)) {
			gotoNext.setFocus();
			startRow = startRow + maxRows;
		}
		else if (e.widget.equals(gotoPrev)) {
			gotoPrev.setFocus();
			startRow = startRow - maxRows;
			if (startRow < 0) {
				startRow = 0;
			}
		}
		else if (e.widget.equals(gotoLast)) {
			gotoLast.setFocus();
			int pageNb = Integer.valueOf(pageTotalNbLabel.getText());
			startRow = totalNbOfRows - (maxRows - ((pageNb*maxRows)-totalNbOfRows));
		}	
		else
			startRow = 0;
		
		paginatedTable.setStartRow(startRow);
		paginatedTable.refreshTable();
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		
		int startRow = paginatedTable.getStartRow();
		int maxRows = paginatedTable.getMaxRows();
		
		if (e.widget.equals(pageNbText)) {		
			
			char key = e.character;
			int keyCode = e.keyCode;
			
			// user can only press 0 to 9 keys, delete, backspace and return keys
			if ( ('0' <= key && key <= '9') || keyCode == SWT.CR || keyCode ==SWT.DEL || keyCode ==SWT.BS) {
				
				if (keyCode == SWT.CR) {	
					// validate entry
					int pageNum = Integer.valueOf(pageNbText.getText());
					int pageNb = Integer.valueOf(pageTotalNbLabel.getText());							
					if (pageNum<=pageNb) {							
						int rowPerPage = maxRows;							
						int newStartRow = ((pageNum-1)*rowPerPage);
						if (newStartRow!=startRow){
							startRow = newStartRow;						
							paginatedTable.setStartRow(startRow);
							paginatedTable.refreshTable();								
						}						
					}			
				}				
			}
			else
				e.doit = false;
		}		
	}
	
	
	/**
	 * 
	 * @param toolkit
	 */
	private void createColors(FormToolkit toolkit) {
		labelColor = toolkit.getColors().getColor(IFormColors.TITLE);
	}
	
	/**
	 * Set the table row status text
	 * @param text status text
	 */
	public void setRowStatusText(String text) {
		
		int totalNbOfRows = paginatedTable.getTotalNbOfRows();
		
		StringBuffer buffer = new StringBuffer();
		buffer.append(CoreMessages.getString("navbar_items") + " ");
		buffer.append(text);
		buffer.append(" " + CoreMessages.getString("navbar_of") + " ");
		buffer.append(totalNbOfRows);
		
		rowStatusLabel.setText(buffer.toString());
	}
	
	/**
	 * Set the table total nb of pages
	 * @param text status text
	 */
	public void setTotalNbOfPageText(String text) {		
		pageTotalNbLabel.setText(text);
	}
	
	/**
	 * Set the table page number
	 * @param text status text
	 */
	public void setPageNbText(String text) {		
		pageNbText.setText(text);
	}	
	
	/**
	 * Enables GoToFirst Button
	 * @param enabled true if button enabled
	 */
	public void enableGoToFirstButton(boolean enabled) {
		gotoFirst.setEnabled(enabled);
	}
	
	/**
	 * Enables GoToNext Button
	 * @param enabled true if button enabled
	 */
	public void enableGoToNextButton(boolean enabled) {
		gotoNext.setEnabled(enabled);
	}
	
	/**
	 * Enables GoToLast Button
	 * @param enabled true if button enabled
	 */
	public void enableGoToLastButton(boolean enabled) {
		gotoLast.setEnabled(enabled);
	}
	
	/**
	 * Enables GoToPrev Button
	 * @param enabled true if button enabled
	 */
	public void enableGoToPrevButton(boolean enabled) {
		gotoPrev.setEnabled(enabled);
	}


	@Override
	public void keyReleased(KeyEvent e) {
	}
	@Override
	public void mouseDoubleClick(MouseEvent e) {
	}
	@Override
	public void mouseDown(MouseEvent e) {
	}
}

