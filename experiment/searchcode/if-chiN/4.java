package com.blogspot.the3cube.beefree.ui;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.wb.swt.SWTResourceManager;

import com.blogspot.the3cube.beefree.logic.CommandHandler;
import com.blogspot.the3cube.beefree.storage.Storage;
import com.blogspot.the3cube.beefree.util.Blockout;
import com.blogspot.the3cube.beefree.util.DisplayCLI;
import com.blogspot.the3cube.beefree.util.Logger;
import com.blogspot.the3cube.beefree.util.Task;
import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.IntellitypeListener;
import com.melloware.jintellitype.JIntellitype;

/**
 * The graphical user interface for BeeFree.<br>
 * 
 * @author The3Cube
 * @since v0.2
 * @version v0.2
 * 
 */
public class BeeFreeGUI extends Composite implements UserInterface, Observer,
		Listener, KeyListener, MouseListener, HotkeyListener,
		IntellitypeListener {
	
	private static final String CLASSNAME = "BeeFreeGUI";
	private static final Color PALE_VIOLET_RED = SWTResourceManager.getColor(255, 130, 171);
	private static final Color WHITE = SWTResourceManager.getColor(255, 255, 255);
	
	private static enum TABS { TASKS, COMPLETED, BLOCKOUTS };

	private final FormToolkit toolkit_ = new FormToolkit(Display.getCurrent());
	
	private boolean isJIntellitypeEnabled_ = false;
	
	private int fontSize_ = 8;
	
	private TabFolder tabFolder_;
	
	private TabItem tbtmTasks_;
	private TabItem tbtmCompleted_;
	private TabItem tbtmBlockouts_;
	
	private Table tableTasks_;
	private Table tableBlockouts_;
	private Table tableCompletedTasks_;

	private Vector<Task> previousTasks_;
	private Vector<Blockout> previousBlockouts_;
	
	private Text textCmdPrompt_;
	private Text textOutput_;

	private Button btnEnter_;

	private Image grayTick_;
	private Image redCross_;

	private CommandHandler handler_;

	/**
	 * Create the composite.<br>
	 * See {@link org.eclipse.swt.widgets.Composite#Composite(Composite, int)}<br>
	 * 
	 * @param parent 	the parent of this composite.
	 * @param style 	the style to use for this composite.
	 */
	public BeeFreeGUI(Composite parent, int style) {
		super(parent, SWT.NO_FOCUS);
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				toolkit_.dispose();
				if (isJIntellitypeEnabled_) {
					jIntelExit();
				}
			}
		});
		toolkit_.setBorderStyle(SWT.NULL);
		toolkit_.adapt(this);
		toolkit_.paintBordersFor(this);
		setLayout(new GridLayout(4, false));
		addKeyListener(this);
		setBackgroundMode(SWT.INHERIT_DEFAULT);

		Label lblBeefree = new Label(this, SWT.NONE);
		
		final Image headingIcon = new Image(getDisplay(), "dist\\img\\heading.png");
		lblBeefree.setImage(headingIcon);
		
		toolkit_.adapt(lblBeefree, true, true);
		
		Label lblBeefreeName = new Label(this, SWT.NONE);
		lblBeefreeName.setFont(SWTResourceManager.getFont("Segoe UI", 14, SWT.BOLD));
		lblBeefreeName.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, false, false, 1, 1));
		toolkit_.adapt(lblBeefreeName, true, true);
		lblBeefreeName.setText("BeeFree");
		
		Label lblCaption = new Label(this, SWT.NONE);
		lblCaption.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1));
		toolkit_.adapt(lblCaption, true, true);
		lblCaption.setText("use BeeFree and be free ...");
		new Label(this, SWT.NONE);

		Composite composite = new Composite(this, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, true, true,
				4, 1);
		gd_composite.widthHint = 579;
		gd_composite.heightHint = 311;
		composite.setLayoutData(gd_composite);
		composite.setBackgroundMode(SWT.INHERIT_DEFAULT);
		toolkit_.adapt(composite);
		toolkit_.paintBordersFor(composite);

		ScrolledComposite scrolledComposite = new ScrolledComposite(composite,
				SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		GridData gd_scrolledComposite = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_scrolledComposite.heightHint = 121;
		scrolledComposite.setLayoutData(gd_scrolledComposite);
		scrolledComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);
		toolkit_.adapt(scrolledComposite);
		toolkit_.paintBordersFor(scrolledComposite);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		tabFolder_ = new TabFolder(scrolledComposite, SWT.NONE);
		tabFolder_.setBackgroundMode(SWT.INHERIT_DEFAULT);
		toolkit_.adapt(tabFolder_);
		toolkit_.paintBordersFor(tabFolder_);
		
		tbtmTasks_ = new TabItem(tabFolder_, SWT.NONE);
		tbtmTasks_.setText("Tasks");

		tableTasks_ = new Table(tabFolder_, SWT.BORDER | SWT.HIDE_SELECTION
				| SWT.VIRTUAL);
		tableTasks_.setLinesVisible(true);
		tbtmTasks_.setControl(tableTasks_);
		toolkit_.paintBordersFor(tableTasks_);
		tableTasks_.setHeaderVisible(true);
		tableTasks_.addListener(SWT.Selection, this);
		tableTasks_.setFont(SWTResourceManager.getFont("Consolas", fontSize_,
				SWT.NORMAL));

		TableColumn tblclmnComplete = new TableColumn(tableTasks_, SWT.NONE);
		tblclmnComplete.setText("done");
		tblclmnComplete.setWidth(40);

		final TableColumn tblclmnTaskids = new TableColumn(tableTasks_, SWT.NONE);
		tblclmnTaskids.setWidth(25);
		tblclmnTaskids.setText("id");

		final TableColumn tblclmnTaskname = new TableColumn(tableTasks_,
				SWT.NONE);
		tblclmnTaskname.setWidth(190);
		tblclmnTaskname.setText("name");

		final TableColumn tblclmnFrom = new TableColumn(tableTasks_, SWT.NONE);
		tblclmnFrom.setWidth(140);
		tblclmnFrom.setText("from");

		final TableColumn tblclmnTo = new TableColumn(tableTasks_, SWT.NONE);
		tblclmnTo.setWidth(140);
		tblclmnTo.setText("to");

		final TableColumn tblclmnLabels = new TableColumn(tableTasks_, SWT.NONE);
		tblclmnLabels.setWidth(70);
		tblclmnLabels.setText("labels");

		tbtmCompleted_ = new TabItem(tabFolder_, SWT.NONE);
		tbtmCompleted_.setText("Completed");

		tableCompletedTasks_ = new Table(tabFolder_, SWT.BORDER
				| SWT.HIDE_SELECTION | SWT.VIRTUAL);
		tbtmCompleted_.setControl(tableCompletedTasks_);
		toolkit_.paintBordersFor(tableCompletedTasks_);
		tableCompletedTasks_.setHeaderVisible(true);
		tableCompletedTasks_.setLinesVisible(true);
		tableCompletedTasks_.addListener(SWT.Selection, this);
		tableCompletedTasks_.setFont(SWTResourceManager.getFont("Consolas", fontSize_,
				SWT.NORMAL));

		TableColumn tableColumn = new TableColumn(tableCompletedTasks_, SWT.NONE);
		tableColumn.setWidth(40);
		tableColumn.setText("del");

		TableColumn tableCompletedTaskids = new TableColumn(
				tableCompletedTasks_, SWT.NONE);
		tableCompletedTaskids.setWidth(25);
		tableCompletedTaskids.setText("id");

		TableColumn tableCompletedTaskname = new TableColumn(
				tableCompletedTasks_, SWT.NONE);
		tableCompletedTaskname.setWidth(190);
		tableCompletedTaskname.setText("name");

		TableColumn tableCompletedFrom = new TableColumn(tableCompletedTasks_,
				SWT.NONE);
		tableCompletedFrom.setWidth(140);
		tableCompletedFrom.setText("from");

		TableColumn tableCompletedto = new TableColumn(tableCompletedTasks_,
				SWT.NONE);
		tableCompletedto.setWidth(140);
		tableCompletedto.setText("to");

		TableColumn tblclmnCompletedLabels = new TableColumn(
				tableCompletedTasks_, SWT.NONE);
		tblclmnCompletedLabels.setWidth(70);
		tblclmnCompletedLabels.setText("labels");

		tbtmBlockouts_ = new TabItem(tabFolder_, SWT.NONE);
		tbtmBlockouts_.setText("Blockouts");

		tableBlockouts_ = new Table(tabFolder_, SWT.BORDER);
		tbtmBlockouts_.setControl(tableBlockouts_);
		toolkit_.paintBordersFor(tableBlockouts_);
		tableBlockouts_.setHeaderVisible(true);
		tableBlockouts_.setLinesVisible(true);
		tableBlockouts_.addListener(SWT.Selection, this);
		tableBlockouts_.setFont(SWTResourceManager.getFont("Consolas", fontSize_,
				SWT.NORMAL));

		TableColumn tblclmnDelete_1 = new TableColumn(tableBlockouts_, SWT.NONE);
		tblclmnDelete_1.setWidth(50);
		tblclmnDelete_1.setText("del");

		TableColumn tblclmnName = new TableColumn(tableBlockouts_, SWT.NONE);
		tblclmnName.setWidth(200);
		tblclmnName.setText("name");

		TableColumn tblclmnFrom_1 = new TableColumn(tableBlockouts_, SWT.NONE);
		tblclmnFrom_1.setWidth(100);
		tblclmnFrom_1.setText("from");

		TableColumn tblclmnTo_1 = new TableColumn(tableBlockouts_, SWT.NONE);
		tblclmnTo_1.setWidth(100);
		tblclmnTo_1.setText("to");
		scrolledComposite.setContent(tabFolder_);
		scrolledComposite.setMinSize(tabFolder_.computeSize(SWT.DEFAULT,
				SWT.DEFAULT));

		textOutput_ = new Text(composite, SWT.READ_ONLY | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		GridData gd_textOutput_ = new GridData(SWT.FILL, SWT.FILL, true, false,
				1, 1);
		gd_textOutput_.heightHint = 44;
		gd_textOutput_.widthHint = 605;
		textOutput_.setLayoutData(gd_textOutput_);
		textOutput_.setFont(SWTResourceManager.getFont("Consolas", fontSize_,
				SWT.NORMAL));
		textOutput_.setTabs(textOutput_.getTabs() + 2);
		toolkit_.adapt(textOutput_, true, true);

		textCmdPrompt_ = new Text(this, SWT.NONE);
		textCmdPrompt_.addKeyListener(this);
		GridData gd_textCmdPrompt_ = new GridData(SWT.FILL, SWT.CENTER, false,
				false, 3, 1);
		gd_textCmdPrompt_.minimumWidth = 600;
		gd_textCmdPrompt_.grabExcessHorizontalSpace = true;
		textCmdPrompt_.setLayoutData(gd_textCmdPrompt_);
		textCmdPrompt_.forceFocus();
		toolkit_.adapt(textCmdPrompt_, true, true);

		btnEnter_ = toolkit_.createButton(this, "enter", SWT.NONE);
		btnEnter_.addMouseListener(this);
		btnEnter_.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false,
				1, 1));
		setTabList(new Control[] { textCmdPrompt_, btnEnter_ });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.blogspot.the3cube.beefree.ui.UserInterface#run()
	 */
	@Override
	public void run() {
		final String METHODNAME = "run";
		try {
			grayTick_ = new Image(getDisplay(), "dist/img/green_tick.png");
			redCross_ = new Image(getDisplay(), "dist/img/red_cross.png");	
		} catch (Exception e) {
			Logger.getInstance().error(CLASSNAME, METHODNAME, "unable to declare image");
		}
		
		handler_ = new CommandHandler();

		Storage storage = handler_.getStorage_();
		storage.addBlockoutContainerObserver(this);
		storage.addTaskContainerObserver(this);

		previousBlockouts_ = storage.getBlockouts_();
		previousTasks_ = storage.getTasks_();
		
		updateView();

		try {
			jIntelInit();
			isJIntellitypeEnabled_ = true;
		} catch (Exception e) {
			isJIntellitypeEnabled_ = false;
			Logger.getInstance().error(CLASSNAME, METHODNAME,
					"JIntellitype is not enabled, " + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.blogspot.the3cube.beefree.ui.Observer#updateData()
	 */
	@Override
	public void updateView() {
		removeAll();

		Storage storage = handler_.getStorage_();
		assert storage != null;
		
		TableItem updatedTask = addItemToTaskTable(storage.getTasks_());
		TableItem updatedBlockout = addItemToBlockoutTable(storage.getBlockouts_());

		if (updatedTask != null) {
			if (updatedTask.getParent() == tableTasks_) {
				focusTab(TABS.TASKS);
				tableTasks_.showItem(updatedTask);
			} else {
				focusTab(TABS.COMPLETED);
				tableCompletedTasks_.showItem(updatedTask);
			}
		} else if (updatedBlockout != null) {
			focusTab(TABS.BLOCKOUTS);
			tableBlockouts_.showItem(updatedBlockout);
		}
		
	}
	
	/**
	 * this method will add custom vector for task and blockout.<br>
	 * 
	 * @programmer 			Chua Jie Sheng
	 * @start 				2 November 2011
	 * @finish 				2 November 2011
	 * 
	 * @reviewer 			Chin Gui Pei
	 * @reviewdate 			2 November 2011
	 * 
	 * @since 				v0.2
	 * 
	 * @param tasks			the task to be displayed.
	 * @param blockouts		the blockouts to be displayed.
	 */
	private void updateView(Vector<Task> tasks, Vector<Blockout> blockouts) {
		removeAll();

		TableItem updatedTask = addItemToTaskTable(tasks);
		TableItem updatedBlockout = addItemToBlockoutTable(blockouts);

		if (updatedTask != null) {
			if (updatedTask.getParent() == tableTasks_) {
				focusTab(TABS.TASKS);
				tableTasks_.showItem(updatedTask);
			} else {
				focusTab(TABS.COMPLETED);
				tableCompletedTasks_.showItem(updatedTask);
			}
		} else if (updatedBlockout != null) {
			focusTab(TABS.BLOCKOUTS);
			tableBlockouts_.showItem(updatedBlockout);
		} else {
			if (tableTasks_.getItemCount() > 0) {
				focusTab(TABS.TASKS);
			} else if (tableCompletedTasks_.getItemCount() > 0) {
				focusTab(TABS.COMPLETED);
			} else {
				focusTab(TABS.BLOCKOUTS);
			}
		}
	}

	/**
	 * this method will remove all item from all the tables.<br>
	 * 
	 * @programmer 	Chua Jie Sheng
	 * @start 		19 October 2011
	 * @finish 		23 October 2011
	 * 
	 * @reviewer 	The3Cube
	 * @reviewdate 	25 October 2011
	 * 
	 * @since 		v0.2
	 */
	private void removeAll() {
		assert tableTasks_ != null;
		assert tableCompletedTasks_ != null;
		assert tableBlockouts_ != null;

		tableTasks_.deselectAll();
		tableTasks_.removeAll();
		tableCompletedTasks_.deselectAll();
		tableCompletedTasks_.removeAll();
		tableBlockouts_.deselectAll();
		tableBlockouts_.removeAll();
	}

	/**
	 * this method all item to task and complete tables.<br>
	 * 
	 * @programmer 	Chua Jie Sheng
	 * @start 		19 October 2011
	 * @finish 		23 October 2011
	 * 
	 * @reviewer 	The3Cube
	 * @reviewdate 	25 October 2011
	 * 
	 * @since 		v0.2
	 * 
	 * @param tasks	the vector to be added to the table.
	 * @return 		the table item which was first updated.
	 */
	private TableItem addItemToTaskTable(Vector<Task> tasks) {
		assert tasks != null;

		Vector<Task> currentTasks = new Vector<Task>();
		assert currentTasks != null;
		
		TableItem updatedTaskRow = null;
		
		for (int i = 0; i < tasks.size(); i++) {
			Task t = tasks.get(i);
			t = t.clone();
			currentTasks.add(t);

			boolean isTaskChanged = compareToOldTaskRecords(t);
			
			TableItem row = null;
			if (t.containsLabel(Task.COMPLETED_LABEL)) {
				row = new TableItem(tableCompletedTasks_, SWT.CENTER);
			} else {
				row = new TableItem(tableTasks_, SWT.CENTER);
			}
			
			if (isTaskChanged) {
				row.setBackground(PALE_VIOLET_RED);
				if (updatedTaskRow == null) {
					updatedTaskRow = row;
				}
				row.setBackground(0, WHITE);
			}

			String[] rowItem = generateItemForRow(t);
			row.setText(rowItem);

			row.setData("taskId", t.getTaskId_());

			if (t.containsLabel(Task.COMPLETED_LABEL)) {
				row.setImage(0, redCross_);
			} else {
				row.setImage(0, grayTick_);
			}
		}
		
		previousTasks_ = currentTasks;
		return updatedTaskRow;
	}

	/**
	 * this method check the old record vector to see if the task changed.<br>
	 * 
	 * @programmer 	Chua Jie Sheng
	 * @start 		2 November 2011
	 * @finish 		2 November 2011
	 * 
	 * @reviewer 	Chin Gui Pei
	 * @reviewdate 	2 November 2011
	 * 
	 * @since 		v0.2
	 * 
	 * @param t		the task to checked.
	 * @return		true if is changed or not found.
	 */
	private boolean compareToOldTaskRecords(Task t) {
		boolean isTaskChanged = true;
		
		for (int i = 0; i < previousTasks_.size(); i++) {
			Task checkWith = previousTasks_.get(i);
			boolean sameTask = false;
			
			if (t.equals(checkWith)) {
				sameTask = true;
			}
			
			if (sameTask) {
				String currentCSV = t.toCSV();
				String previousCSV = checkWith.toCSV();
				
				if (currentCSV.equals(previousCSV)) {
					isTaskChanged = false;
				} else {
					isTaskChanged = true;
				}
			}
		}
		
		return isTaskChanged;
	}

	/**
	 * this method generate item for TableItem.<br>
	 * 
	 * @programmer 	Chua Jie Sheng
	 * @start 		2 November 2011
	 * @finish 		2 November 2011
	 * 
	 * @reviewer 	Chin Gui Pei
	 * @reviewdate 	2 November 2011
	 * 
	 * @since 		v0.2
	 * 
	 * @param t		the task to generate from.
	 * @return		a string array of for TableItem.
	 */
	private String[] generateItemForRow(Task t) {
		String taskId = t.getTaskId_() + "";
		String taskName = t.getTaskName_();
		String start = "";
		if (t.getStartFrom_() != null) {
			start += t.getStartFrom_();

			if (t.getStartTime_() != null) {
				start += ", " + t.getStartTime_();
			}
		}
		String finish = "";
		if (t.getFinishBy_() != null) {
			finish += t.getFinishBy_();

			if (t.getFinishTime_() != null) {
				finish += ", " + t.getFinishTime_();
			}
		}
		String label = "";
		Vector<com.blogspot.the3cube.beefree.util.Label> labels = t
				.getLabels_();
		for (com.blogspot.the3cube.beefree.util.Label l : labels) {
			label += l.getLabelName_() + ";";
		}

		String[] rowItem = { "", taskId, taskName, start, finish, label };
		return rowItem;
	}

	/**
	 * this method all item to blockout table.<br>
	 * 
	 * @programmer 			Chua Jie Sheng
	 * @start 				19 October 2011
	 * @finish 				23 October 2011
	 * 
	 * @reviewer 			The3Cube
	 * @reviewdate 			25 October 2011
	 * 
	 * @since 				v0.2
	 * 
	 * @param blockouts		the vector to added to the table.
	 * @return				the table item which was first updated.
	 */
	private TableItem addItemToBlockoutTable(Vector<Blockout> blockouts) {
		assert blockouts != null;
		
		Vector<Blockout> currentBlockouts = new Vector<Blockout>();
		assert currentBlockouts != null;
		
		TableItem updateBlockoutRow = null;
		
		for (int i = 0; i < blockouts.size(); i++) {
			Blockout b = blockouts.get(i);
			b = b.clone();
			
			currentBlockouts.add(b);

			boolean isBlockoutChanged = compareToOldBlockoutRecords(b);
			
			TableItem row = new TableItem(tableBlockouts_, SWT.CENTER);

			if (isBlockoutChanged) {
				row.setBackground(PALE_VIOLET_RED);
				if (updateBlockoutRow == null) {
					updateBlockoutRow = row;
				}
				row.setBackground(0, WHITE);
			}
			
			String blockoutName = b.getBlockoutName_();
			String from = b.getStartBlockout_() + "";
			String to = b.getEndBlockout_() + "";

			String[] rowItem = { "", blockoutName, from, to };

			row.setData("blockoutId", b.getBlockoutId_());
			row.setText(rowItem);
			row.setImage(0, redCross_);
		}
		previousBlockouts_ = currentBlockouts;
		return updateBlockoutRow;
	}

	/**
	 * this method check the old record vector to see if the blockout changed.<br>
	 * 
	 * @programmer 	Chua Jie Sheng
	 * @start 		2 November 2011
	 * @finish 		2 November 2011
	 * 
	 * @reviewer 	Chin Gui Pei
	 * @reviewdate 	2 November 2011
	 * 
	 * @since 		v0.2
	 * 
	 * @param b		the blockout to checked.
	 * @return		true if is changed or not found.
	 */
	private boolean compareToOldBlockoutRecords(Blockout b) {
		boolean isBlockoutChanged = true;
		
		for (int i = 0; i < previousBlockouts_.size(); i++) {
			Blockout checkWith = previousBlockouts_.get(i);
			boolean sameBlockout = false;
			
			if (b.equals(checkWith)) {
				sameBlockout = true;
			}
			
			if (sameBlockout) {
				String currentCSV = b.toCSV();
				String previousCSV = checkWith.toCSV();
				
				if (currentCSV.equals(previousCSV)) {
					isBlockoutChanged = false;
				} else {
					isBlockoutChanged = true;
				}
			}
		}
		
		return isBlockoutChanged;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.
	 * Event)
	 */
	@Override
	public void handleEvent(Event arg0) {
		assert arg0 != null;

		completeTaskIfSelected();
		deleteTaskIfSelected();
		deleteBlockoutIfSelected();
	}

	/**
	 * this method complete task if there is task selected.<br>
	 * 
	 * @programmer 	Chua Jie Sheng
	 * @start 		19 October 2011
	 * @finish 		23 October 2011
	 * 
	 * @reviewer 	The3Cube
	 * @reviewdate 	25 October 2011
	 * 
	 * @since 		v0.2
	 */
	private void completeTaskIfSelected() {
		assert tableTasks_ != null;

		TableItem[] completeTask = tableTasks_.getSelection();

		for (TableItem task : completeTask) {
			String cmd = "complete " + task.getData("taskId");
			com.blogspot.the3cube.beefree.util.Display display = handler_
					.doCmd(cmd);
			display(display);
		}
	}

	/**
	 * this method delete task if there is task selected.<br>
	 * 
	 * @programmer 	Chua Jie Sheng
	 * @start 		19 October 2011
	 * @finish 		23 October 2011
	 * 
	 * @reviewer 	The3Cube
	 * @reviewdate 	25 October 2011
	 * 
	 * @since 		v0.2
	 */
	private void deleteTaskIfSelected() {
		assert tableCompletedTasks_ != null;

		TableItem[] deleteTask = tableCompletedTasks_.getSelection();

		for (TableItem task : deleteTask) {
			String cmd = "delete " + task.getData("taskId");
			com.blogspot.the3cube.beefree.util.Display display = handler_
					.doCmd(cmd);
			display(display);
		}
	}

	/**
	 * this method delete blockout if there is blockout selected.<br>
	 * 
	 * @programmer 	Chua Jie Sheng
	 * @start 		19 October 2011
	 * @finish 		23 October 2011
	 * 
	 * @reviewer 	The3Cube
	 * @reviewdate 	25 October 2011
	 * 
	 * @since 		v0.2
	 */
	private void deleteBlockoutIfSelected() {
		assert tableBlockouts_ != null;

		TableItem[] deleteBlockout = tableBlockouts_.getSelection();

		for (TableItem blockout : deleteBlockout) {
			String cmd = "blockout remove " + blockout.getData("blockoutId");
			com.blogspot.the3cube.beefree.util.Display display = handler_
					.doCmd(cmd);
			display(display);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.
	 * KeyEvent)
	 */
	@Override
	public void keyPressed(KeyEvent arg0) {
		if (arg0.stateMask == SWT.CTRL) {
			if (arg0.character == '-') {
				reduceFontSize();
			}
			if (arg0.character == '=') {
				increaseFontSize();
			}
		}
	}

	/**
	 * this method reduce the font size of the gui.<br>
	 * 
	 * @programmer 	Chua Jie Sheng
	 * @start 		30 October 2011
	 * @finish 		30 October 2011
	 * 
	 * @reviewer 	Chin Gui Pei	
	 * @reviewdate 	30 October 2011
	 * 
	 * @since 		v0.2
	 */
	private void reduceFontSize() {
		Font font;
		
		if (fontSize_ > 8) {
			font = SWTResourceManager.getFont("Consolas", --fontSize_, 
					SWT.NORMAL);
			
			tableBlockouts_.setFont(font);
			tableCompletedTasks_.setFont(font);
			tableTasks_.setFont(font);
			
			textOutput_.setFont(font);
			textCmdPrompt_.setFont(font);		
			
			btnEnter_.setFont(font);	
		}
		
	}

	/**
	 * this method increase the font size of the gui.<br>
	 * 
	 * @programmer 	Chua Jie Sheng
	 * @start 		30 October 2011
	 * @finish 		30 October 2011
	 * 
	 * @reviewer 	Chin Gui Pei	
	 * @reviewdate 	30 October 2011
	 * 
	 * @since 		v0.2
	 */
	private void increaseFontSize() {
		Font font;
		
		if (fontSize_ < 30) {
			font = SWTResourceManager.getFont("Consolas", ++fontSize_, 
					SWT.NORMAL);
			
			tableBlockouts_.setFont(font);
			tableCompletedTasks_.setFont(font);
			tableTasks_.setFont(font);
			
			textOutput_.setFont(font);
			textCmdPrompt_.setFont(font);			
			
			btnEnter_.setFont(font);		
		}
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events
	 * .KeyEvent)
	 */
	@Override
	public void keyReleased(KeyEvent arg0) {
		assert arg0 != null;

		boolean fromCmdPrompt = arg0.getSource() == textCmdPrompt_;
		if (fromCmdPrompt) {
			if (arg0.character == SWT.CR) {
				runCmdFromPrompt();
			}
			if (arg0.character == SWT.ESC) {
				textCmdPrompt_.setText(new String());
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt
	 * .events.MouseEvent)
	 */
	@Override
	public void mouseDoubleClick(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events
	 * .MouseEvent)
	 */
	@Override
	public void mouseDown(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.
	 * MouseEvent)
	 */
	@Override
	public void mouseUp(MouseEvent arg0) {
		assert arg0 != null;
		
		if (arg0.getSource() == btnEnter_) {
			runCmdFromPrompt();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.melloware.jintellitype.IntellitypeListener#onIntellitype(int)
	 */
	@Override
	public void onIntellitype(int command) {
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.melloware.jintellitype.HotkeyListener#onHotKey(int)
	 */
	@Override
	public void onHotKey(int identifier) {
		if (identifier == 1) {
			scheduleExecutionAdd();
		} else if (identifier == 2) {
			focusShell();
		} else if (identifier == 11) {
			focusTab(TABS.TASKS);
		} else if (identifier == 12) {
			focusTab(TABS.COMPLETED);
		} else if (identifier == 13) {
			focusTab(TABS.BLOCKOUTS);
		}
	}

	/**
	 * this method will schedule the execution of adding the task from
	 * clipboard.<br>
	 * 
	 * @programmer 	Chua Jie Sheng
	 * @start 		23 October 2011
	 * @finish 		23 October 2011
	 * 
	 * @reviewer 	The3Cube
	 * @reviewdate 	25 October 2011
	 * 
	 * @since 		v0.2
	 */
	private void scheduleExecutionAdd() {
		this.getDisplay().syncExec(new Runnable() {
			public void run() {
				String clipboard = getClipboard();
				if (clipboard != null) {
					handler_.doCmd("add " + clipboard);
				}
			}
		});
	}
	
	/**
	 * this method force the shell to be focus.<br>
	 * 
	 * @programmer 	Chua Jie Sheng
	 * @start 		23 October 2011
	 * @finish 		23 October 2011
	 * 
	 * @reviewer 	The3Cube
	 * @reviewdate 	25 October 2011
	 * 
	 * @since 		v0.2
	 */
	private void focusShell() {
		this.getDisplay().syncExec(new Runnable() {
			public void run() {
				getShell().setMinimized(false);
				getShell().setActive();	
				textCmdPrompt_.forceFocus();
			}
		});
	}
	
	/**
	 * this method focus the specific tabs.<br>
	 * 
	 * @programmer 	Chua Jie Sheng
	 * @start 		23 October 2011
	 * @finish 		23 October 2011
	 * 
	 * @reviewer 	The3Cube
	 * @reviewdate 	25 October 2011
	 * 
	 * @since 		v0.2
	 * 
	 * @param tab	the tabs to be focus.
	 */
	private void focusTab(final TABS tab) {
		this.getDisplay().syncExec(new Runnable() {
			public void run() {
				if (tab == TABS.TASKS) {
					tabFolder_.setSelection(tbtmTasks_);
				} else if (tab == TABS.COMPLETED) {
					tabFolder_.setSelection(tbtmCompleted_);
				} else if (tab == TABS.BLOCKOUTS) {
					tabFolder_.setSelection(tbtmBlockouts_);
				}
			}
		});
	}

	/**
	 * this method initalise the JIntellitype handler.<br>
	 * 
	 * @programmer 	Chua Jie Sheng
	 * @start 		23 October 2011
	 * @finish 		23 October 2011
	 * 
	 * @reviewer 	The3Cube
	 * @reviewdate 	25 October 2011
	 * 
	 * @since 		v0.2
	 */
	private void jIntelInit() throws Exception {
		final String METHODNAME = "jIntelInit";

		String jvm = System.getProperty("java.vm.name");

		if (jvm.contains("64-Bit")) {
			JIntellitype.setLibraryLocation("dist\\lib\\JIntellitype64.dll");
			Logger.getInstance().debug(CLASSNAME, METHODNAME,
					"loaded JIntellitype64 on" + jvm);
		} else {
			JIntellitype.setLibraryLocation("dist\\lib\\JIntellitype.dll");
			Logger.getInstance().debug(CLASSNAME, METHODNAME,
					"loaded JIntellitype on" + jvm);
		}

		// Initialize JIntellitype
		JIntellitype.getInstance();

		if (JIntellitype.checkInstanceAlreadyRunning("JIntellitype")) {
			System.err
					.println("An instance of this application is already running");
			System.exit(1);
		}

		// Assign global hotkeys
		JIntellitype.getInstance().registerHotKey(1,
				JIntellitype.MOD_WIN, (int) 'A');
		JIntellitype.getInstance().registerHotKey(2,
				JIntellitype.MOD_WIN, (int) 'Q');
		JIntellitype.getInstance().registerHotKey(11,
				JIntellitype.MOD_CONTROL, (int) '1');
		JIntellitype.getInstance().registerHotKey(12,
				JIntellitype.MOD_CONTROL, (int) '2');
		JIntellitype.getInstance().registerHotKey(13,
				JIntellitype.MOD_CONTROL, (int) '3');

		// assign this class to be a HotKeyListener
		JIntellitype.getInstance().addHotKeyListener(this);

		// assign this class to be a IntellitypeListener
		JIntellitype.getInstance().addIntellitypeListener(this);
	}

	/**
	 * this method perform the exit event and clean up the JIntellitype related
	 * resources.<br>
	 * 
	 * @programmer 	Chua Jie Sheng
	 * @start 		19 October 2011
	 * @finish 		23 October 2011
	 * 
	 * @reviewer 	The3Cube
	 * @reviewdate 	25 October 2011
	 * 
	 * @since 		v0.2
	 */
	private void jIntelExit() {
		// To unregister them just call unregisterHotKey with the unique
		// identifier
		JIntellitype.getInstance().unregisterHotKey(1);
		JIntellitype.getInstance().unregisterHotKey(2);
		JIntellitype.getInstance().unregisterHotKey(11);
		JIntellitype.getInstance().unregisterHotKey(12);
		JIntellitype.getInstance().unregisterHotKey(13);
		JIntellitype.getInstance().cleanUp();
	}

	/**
	 * this method retrieve the clipboard in string flavor.<br>
	 * 
	 * @programmer 	Chua Jie Sheng
	 * @start 		19 October 2011
	 * @finish 		23 October 2011
	 * 
	 * @reviewer 	The3Cube
	 * @reviewdate 	25 October 2011
	 * 
	 * @since 		v0.2
	 * @return 		the data inside the clipboard in string.
	 */
	private static String getClipboard() {
		Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard()
				.getContents(null);
		String text = "";
		try {
			if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				text = (String) t.getTransferData(DataFlavor.stringFlavor);

				return text.trim();
			}
		} catch (Exception e) {
			text = null;
		}
		return text;
	}

	/**
	 * run command currently in command prompt.<br>
	 * 
	 * @programmer 	Chua Jie Sheng
	 * @start 		18 October 2011
	 * @finish 		18 October 2011
	 * 
	 * @reviewer 	The3Cube
	 * @reviewdate 	25 October 2011
	 * 
	 * @since 		v0.2
	 */
	private void runCmdFromPrompt() {
		assert handler_ != null;

		String cmd = textCmdPrompt_.getText();

		cmd = cmd.trim();
		cmd = cmd.replace(',', ' ');
		
		boolean isSearch = false;
		isSearch = cmd.matches("[f|s]{1}.*");
		isSearch = isSearch || cmd.matches("[di|ls]{2}.*");
		
		if (isSearch) {
			boolean isValidCommand = false;
			isValidCommand = cmd.indexOf(" ") > 0;

			Vector<Task> tasks = null;
			Vector<Blockout> blockouts = null;
			
			if (isValidCommand) {
				String searchFor = cmd.substring(cmd.indexOf(" ") + 1);
				searchFor = searchFor.trim();
				searchFor = searchFor.toLowerCase();
				
				tasks = searchTasks(searchFor);
				assert tasks != null;
				
				blockouts = searchBlockouts(searchFor);
				assert blockouts != null;
			}
			
			Storage storage = handler_.getStorage_();
			if (tasks == null) {
				tasks = storage.getTasks_();
			}
			if (blockouts == null) {
				blockouts = storage.getBlockouts_();
			}
			
			updateView(tasks, blockouts);
			
		} else {
			com.blogspot.the3cube.beefree.util.Display display = null;
			try {
				display = handler_.doCmd(cmd);
			} catch (Exception e) {
				String errorMsg = "The command you entered is invalid. Please refer to \"help [command]\".";
				display = new DisplayCLI(errorMsg);
			}
			display(display);
		}
		textCmdPrompt_.setText(new String());
		
	}

	/**
	 * this method search for task with the search string.<br>
	 * 
	 * @programmer 		Chua Jie Sheng
	 * @start 			2 November 2011
	 * @finish 			2 November 2011
	 * 
	 * @reviewer 		Chin Gui Pei
	 * @reviewdate 		2 November 2011
	 * 
	 * @since 			v0.2
	 * 
	 * @param searchFor	the string to search for.
	 * @return			the vector containing what is found.
	 */
	private Vector<Task> searchTasks(String searchFor) {
		assert handler_ != null;

		Storage storage = handler_.getStorage_();
		assert storage != null;

		Vector<Task> tasks = storage.getTasks_();
		assert tasks != null;

		Vector<Task> result = new Vector<Task>();

		for (int i = 0; i < tasks.size(); i++) {
			Task t = tasks.get(i);

			boolean matched = false;

			String taskName = t.getTaskName_();
			taskName = taskName.toLowerCase();
			
			Vector<com.blogspot.the3cube.beefree.util.Label> labels = t
					.getLabels_();

			matched = taskName.indexOf(searchFor) > -1;
			
			if (!matched) {
				for (int j = 0; j < labels.size(); j++) {
					com.blogspot.the3cube.beefree.util.Label l = labels.get(j);
					
					String labelName = l.getLabelName_();
					labelName = labelName.toLowerCase();
					
					matched = labelName.indexOf(searchFor) > -1;
				}
			}

			if (matched) {
				result.add(t.clone());
			}
		}

		return result;
	}

	/**
	 * this method search for blockout with the search string.<br>
	 * 
	 * @programmer 		Chua Jie Sheng
	 * @start 			2 November 2011
	 * @finish 			2 November 2011
	 * 
	 * @reviewer 		Chin Gui Pei
	 * @reviewdate 		2 November 2011
	 * 
	 * @since 			v0.2
	 * 
	 * @param searchFor	the string to search for.
	 * @return			the vector containing what is found.
	 */
	private Vector<Blockout> searchBlockouts(String searchFor) {
		assert handler_ != null;

		Storage storage = handler_.getStorage_();
		assert storage != null;

		Vector<Blockout> blockouts = storage.getBlockouts_();
		assert blockouts != null;

		Vector<Blockout> result = new Vector<Blockout>();

		for (int i = 0; i < blockouts.size(); i++) {
			Blockout b = blockouts.get(i);

			String blockoutName = b.getBlockoutName_();
			blockoutName = blockoutName.toLowerCase();

			boolean matched = blockoutName.indexOf(searchFor) > -1;

			if (matched) {
				result.add(b.clone());
			}
		}

		return result;
	}

	/**
	 * display the string from display.<br>
	 * 
	 * @programmer 		Chua Jie Sheng
	 * @start 			18 October 2011
	 * @finish 			18 October 2011
	 * 
	 * @reviewer 		The3Cube
	 * @reviewdate 		25 October 2011
	 * 
	 * @since 			v0.2
	 * 
	 * @param display 	the display to be display to the output.
	 */
	private void display(com.blogspot.the3cube.beefree.util.Display display) {
		assert textOutput_ != null;

		if (display != null) {
			if (display instanceof DisplayCLI) {
				DisplayCLI cli = (DisplayCLI) display;
				textOutput_.setText(cli.getData_().trim());
			}
		}
	}
}

