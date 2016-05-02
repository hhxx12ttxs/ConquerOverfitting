/*
 * License GNU LGPL
 * Copyright (C) 2012 Amrullah <amrullah@panemu.com>.
 */
package com.panemu.tiwulfx.table;

import com.panemu.tiwulfx.common.ObjectExposer;
import com.panemu.tiwulfx.common.TableCriteria;
import com.panemu.tiwulfx.common.TableData;
import com.panemu.tiwulfx.common.TiwulFXUtil;
import com.panemu.tiwulfx.dialog.MessageDialog;
import com.panemu.tiwulfx.dialog.MessageDialogBuilder;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBuilder;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.control.ToolBarBuilder;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.apache.commons.beanutils.PropertyUtils;

/**
 *
 * @author amrullah
 */
public class TableControl<T> extends VBox {

	private Button btnAdd;
	private Button btnEdit;
	private Button btnDelete;
	private Button btnFirstPage;
	private Button btnLastPage;
	private Button btnNextPage;
	private Button btnPrevPage;
	private Button btnReload;
	private Button btnSave;
	private Button btnExport;
	private ComboBox<Integer> cmbPage;
	private Label lblRowIndex;
	private Label lblTotalRow;
	private TableView<T> tblView = new TableView<>();
	private Region spacer;
	private HBox paginationBox;
	private ToolBar toolbar;
	private StackPane footer;
	private TableController<T> controller;
	private SimpleIntegerProperty startIndex = new SimpleIntegerProperty(0);
	private StartIndexChangeListener startIndexChangeListener = new StartIndexChangeListener();
	private InvalidationListener sortTypeChangeListener = new SortTypeChangeListener();
	private List<ObjectExposer> lstExposer = new ArrayList<>();
	private ReadOnlyObjectWrapper<Mode> mode = new ReadOnlyObjectWrapper<>(null);
	private long totalRows = 0;
	private Integer page = 1;
	private ObservableList<T> lstChangedRow = FXCollections.observableArrayList();
	private Class<T> recordClass;
	private boolean fitColumnAfterReload = false;
	private List<TableCriteria> lstCriteria = new ArrayList<>();
	private boolean reloadOnCriteriaChange = true;
	private boolean directEdit = false;
	private List<EditCommitListener> lstEditCommitListener = new ArrayList<>();
	private final ObservableList<TableColumn<T, ?>> columns = tblView.getColumns();

	public static enum Mode {

		INSERT, EDIT, READ
	}

	/**
	 * UI component in TableControl which their visibility could be manipulated
	 *
	 * @see #setVisibleComponents(boolean,
	 * com.panemu.tiwulfx.table.TableControl.Component[])
	 *
	 */
	public static enum Component {

		BUTTON_RELOAD, BUTTON_INSERT, BUTTON_EDIT, BUTTON_SAVE, BUTTON_DELETE,
		BUTTON_EXPORT, BUTTON_PAGINATION, TOOLBAR, FOOTER;
	}

	public TableControl(Class<T> recordClass) {
		this.recordClass = recordClass;
		initControls();

		tblView.getSortOrder().addListener(new ListChangeListener<TableColumn<T, ?>>() {
			@Override
			public void onChanged(ListChangeListener.Change<? extends TableColumn<T, ?>> change) {
				reload();
			}
		});

		tblView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<T>() {
			@Override
			public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
				List<T> lst = tblView.getSelectionModel().getSelectedItems();
				if (!lst.isEmpty()) {
					for (ObjectExposer exposer : lstExposer) {
						exposer.setObjectToDisplay(lst.get(lst.size() - 1));
					}
				}
			}
		});

//        btnExport.disableProperty().bind(mode.isNotEqualTo(Mode.READ));
		btnAdd.disableProperty().bind(mode.isEqualTo(Mode.EDIT));
		btnEdit.disableProperty().bind(mode.isNotEqualTo(Mode.READ));
		btnSave.disableProperty().bind(mode.isEqualTo(Mode.READ));
		btnDelete.disableProperty().bind(new BooleanBinding() {
			{
				super.bind(mode, tblView.getSelectionModel().selectedItemProperty(), lstChangedRow);
			}

			@Override
			protected boolean computeValue() {
				if ((mode.get() == Mode.INSERT && lstChangedRow.size() < 2) || tblView.getSelectionModel().selectedItemProperty().get() == null || mode.get() == Mode.EDIT) {
					return true;
				}
				return false;
			}
		});
		tblView.editableProperty().bind(mode.isNotEqualTo(Mode.READ));
		tblView.getSelectionModel().cellSelectionEnabledProperty().bind(tblView.editableProperty());
		mode.addListener(new ChangeListener<Mode>() {
			@Override
			public void changed(ObservableValue<? extends Mode> ov, Mode t, Mode t1) {
				if (t1 == Mode.READ) {
					directEdit = false;
				}
			}
		});



		tblView.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
				lblRowIndex.setText(TiwulFXUtil.getLiteral("row.param", (page * maxResult.get() + t1.intValue() + 1)));
			}
		});

		tblView.getFocusModel().focusedCellProperty().addListener(new ChangeListener<TablePosition>() {
			@Override
			public void changed(ObservableValue<? extends TablePosition> observable, TablePosition oldValue, TablePosition newValue) {
				if (tblView.isEditable() && directEdit && agileEditing.get()) {
					tblView.edit(newValue.getRow(), newValue.getTableColumn());
				}
			}
		});

		tblView.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.ESCAPE) {
					directEdit = false;
				} else if (event.getCode() == KeyCode.ENTER && mode.get() == Mode.READ) {
					getController().doubleClick(getSelectionModel().getSelectedItem());
				}
			}
		});

		/**
		 * Define policy for TAB key press
		 */
		tblView.addEventFilter(KeyEvent.KEY_PRESSED, tableKeyListener);
		/**
		 * In INSERT mode, only inserted row that is focusable
		 */
		tblView.getFocusModel().focusedCellProperty().addListener(tableFocusListener);

		cm = new ContextMenu();
		cm.setAutoHide(true);
		tblView.setOnMouseReleased(tableRightClickListener);
		setToolTips();

		/**
		 * create custom row factory that can intercept double click on grid row
		 */
		tblView.setRowFactory(new Callback<TableView<T>, TableRow<T>>() {
			@Override
			public TableRow<T> call(TableView<T> param) {
				return new TableRowControl(TableControl.this);
			}
		});

		columns.addListener(new ListChangeListener<TableColumn<T, ?>>() {
			@Override
			public void onChanged(ListChangeListener.Change<? extends TableColumn<T, ?>> change) {
				while (change.next()) {
					if (change.wasAdded()) {
						for (TableColumn<T, ?> column : change.getAddedSubList()) {
							initColumn(column);
						}
					}
					lastColumnIndex = getLeafColumns().size() - 1;
				}
			}
		});
	}
	
	private int lastColumnIndex = 0;

	public final ObservableList<TableColumn<T, ?>> getColumns() {
		return columns;
	}

	public ObservableList<T> getChangedRecords() {
		return lstChangedRow;
	}

	private void initControls() {

		btnAdd = buildButton("add.png");
		btnDelete = buildButton("delete.png");
		btnEdit = buildButton("edit.png");
		btnExport = buildButton("export.png");
		btnReload = buildButton("reload.png");
		btnSave = buildButton("save.png");

		btnFirstPage = ButtonBuilder.create()
				.text("<<")
				.onAction(paginationHandler)
				.disable(true)
				.focusTraversable(false)
				.styleClass("pill-button", "pill-button-left")
				.build();
		btnPrevPage = ButtonBuilder.create()
				.text("<")
				.onAction(paginationHandler)
				.disable(true)
				.focusTraversable(false)
				.styleClass("pill-button", "pill-button-center")
				.build();
		btnNextPage = ButtonBuilder.create()
				.text(">")
				.onAction(paginationHandler)
				.disable(true)
				.focusTraversable(false)
				.styleClass("pill-button", "pill-button-center")
				.build();
		btnLastPage = ButtonBuilder.create()
				.text(">>")
				.onAction(paginationHandler)
				.disable(true)
				.focusTraversable(false)
				.styleClass("pill-button", "pill-button-right")
				.build();
		cmbPage = ComboBoxBuilder.<Integer>create()
				.editable(true)
				.onAction(paginationHandler)
				.focusTraversable(false)
				.disable(true)
				.styleClass("combo-page")
				.build();

		paginationBox = HBoxBuilder.create()
				.alignment(Pos.CENTER)
				.children(btnFirstPage, btnPrevPage, cmbPage, btnNextPage, btnLastPage)
				.build();

		spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		toolbar = ToolBarBuilder.create()
				.styleClass("table-toolbar")
				.items(btnReload, btnAdd, btnEdit, btnSave, btnDelete, btnExport, spacer, paginationBox)
				.build();

		footer = new StackPane();
		footer.getStyleClass().add("table-footer");
		lblRowIndex = new Label();
		lblTotalRow = new Label();
		MenuButton menuButton = new TableControlMenu(this);
		menuButton.setGraphic(new ImageView(new Image(TableControl.class.getResourceAsStream("/images/" + "conf.png"))));
		StackPane.setAlignment(lblRowIndex, Pos.CENTER_LEFT);
		StackPane.setAlignment(lblTotalRow, Pos.CENTER);
		StackPane.setAlignment(menuButton, Pos.CENTER_RIGHT);
		footer.getChildren().addAll(lblRowIndex, lblTotalRow, menuButton);
		VBox.setVgrow(tblView, Priority.ALWAYS);
		getChildren().addAll(toolbar, tblView, footer);

	}

	private Button buildButton(String imageName) {
		return ButtonBuilder.create()
				.graphic(new ImageView(new Image(TableControl.class.getResourceAsStream("/images/" + imageName))))
				.styleClass("flat-button")
				.onAction(buttonHandler)
				.build();
	}
	private EventHandler<ActionEvent> buttonHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			if (event.getSource() == btnAdd) {
				insert();
			} else if (event.getSource() == btnDelete) {
				delete();
			} else if (event.getSource() == btnEdit) {
				edit();
			} else if (event.getSource() == btnExport) {
				export();
			} else if (event.getSource() == btnReload) {
				reload();
			} else if (event.getSource() == btnSave) {
				save();
			}
		}
	};
	private EventHandler<ActionEvent> paginationHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			if (event.getSource() == btnFirstPage) {
				reloadFirstPage();
			} else if (event.getSource() == btnPrevPage) {
				prevPageFired(event);
			} else if (event.getSource() == btnNextPage) {
				nextPageFired(event);
			} else if (event.getSource() == btnLastPage) {
				lastPageFired(event);
			} else if (event.getSource() == cmbPage) {
				pageChangeFired(event);
			}
		}
	};

	/**
	 * Set selection mode
	 *
	 * @see javafx.scene.control.SelectionMode
	 * @param mode
	 */
	public void setSelectionMode(SelectionMode mode) {
		tblView.getSelectionModel().setSelectionMode(mode);
	}

	private void setToolTips() {
		TiwulFXUtil.setToolTip(btnAdd, "add.record");
		TiwulFXUtil.setToolTip(btnDelete, "delete.record");
		TiwulFXUtil.setToolTip(btnEdit, "edit.record");
		TiwulFXUtil.setToolTip(btnFirstPage, "go.to.first.page");
		TiwulFXUtil.setToolTip(btnLastPage, "go.to.last.page");
		TiwulFXUtil.setToolTip(btnNextPage, "go.to.next.page");
		TiwulFXUtil.setToolTip(btnPrevPage, "go.to.prev.page");
		TiwulFXUtil.setToolTip(btnReload, "reload.records");
		TiwulFXUtil.setToolTip(btnExport, "export.records");
		TiwulFXUtil.setToolTip(btnSave, "save.record");
	}
	private BooleanProperty agileEditing = new SimpleBooleanProperty(false);

	public void setAgileEditing(boolean agileEditing) {
		this.agileEditing.set(agileEditing);
	}

	public boolean isAgileEditing() {
		return agileEditing.get();
	}

	public BooleanProperty agileEditingProperty() {
		return agileEditing;
	}
	/**
	 * Move focus to the next cell if user pressing TAB and the mode is
	 * EDIT/INSERT
	 */
	private EventHandler<KeyEvent> tableKeyListener = new EventHandler<KeyEvent>() {
		@Override
		public void handle(KeyEvent event) {
			if (mode.get() == Mode.READ) {
				return;
			}
			if (event.getCode() == KeyCode.TAB) {
				if (event.isShiftDown()) {
					if (tblView.getSelectionModel().getSelectedCells().get(0).getColumn() == 0) {
						List<TableColumn<T,?>> leafColumns = getLeafColumns();
						tblView.getSelectionModel().select(tblView.getSelectionModel().getSelectedIndex() - 1, leafColumns.get(leafColumns.size() - 1));
					} else {
						tblView.getSelectionModel().selectLeftCell();
					}
				} else {
					if (tblView.getSelectionModel().getSelectedCells().get(0).getColumn() == lastColumnIndex) {
						tblView.getSelectionModel().select(tblView.getSelectionModel().getSelectedIndex() + 1, tblView.getColumns().get(0));
					} else {
						tblView.getSelectionModel().selectRightCell();
					}
				}
				horizontalScroller.run();
				event.consume();
			} else if (event.getCode() == KeyCode.ENTER && !event.isControlDown() && !event.isAltDown() && !event.isShiftDown()) {
				if (agileEditing.get()) {
					if (directEdit) {
						//TODO warning, using tblView.lookup is fragile
						showRow(tblView.getSelectionModel().getSelectedIndex() + 1);
						if (tblView.getSelectionModel().getSelectedIndex() == tblView.getItems().size() - 1) {
							//it will trigger cell's commit edit for the most bottom row
							tblView.getSelectionModel().selectAboveCell();
						}
						tblView.getSelectionModel().selectBelowCell();
						event.consume();
					} else {
						directEdit = true;
					}
				}
			} else if (event.getCode() == KeyCode.V && event.isControlDown()) {
				if (!isACellInEditing()) {
					paste();
					event.consume();
				}

			}
		}
	};

	private boolean isACellInEditing() {
		return tblView.getEditingCell() != null && tblView.getEditingCell().getRow() > -1;
	}

	public void showRow(int index) {
		//TODO warning, using tblView.lookup is fragile
		VirtualFlow node = (VirtualFlow) tblView.lookup("VirtualFlow");
		node.show(index);
	}

	/**
     * Mark record as changed. It will only add the record to the changed record list
	 * if the record doesn't exist in the list. Avoid adding record to {@link #getChangedRecords()}
	 * to avoid adding the same record multiple times.
     * @param record
     */
	public void markAsChanged(T record) {
		if (!lstChangedRow.contains(record)) {
			lstChangedRow.add(record);
		}
	}

	/**
	 * Paste text on clipboard. Doesn't work on READ mode.
	 */
	public void paste() {
		if (mode.get() == Mode.READ) {
			return;
		}
		final Clipboard clipboard = Clipboard.getSystemClipboard();
		if (clipboard.hasString()) {
			final String text = clipboard.getString();
			if (text != null) {
				List<TablePosition> cells = tblView.getSelectionModel().getSelectedCells();
				if (cells.isEmpty()) {
					return;
				}
				TablePosition cell = cells.get(0);
				List<TableColumn<T, ?>> lstColumn = getLeafColumns();
				TableColumn startColumn = null;
				for (TableColumn clm : lstColumn) {
					if (clm instanceof BaseColumn && clm == cell.getTableColumn()) {
						startColumn = (BaseColumn) clm;
					}
				}
				if (startColumn == null) {
					return;
				}
				int rowIndex = cell.getRow();
				String[] arrString = text.split("\n");
				boolean stopPasting = false;
				for (String line : arrString) {
					if (stopPasting) {
						break;
					}
					T item = null;
					if (rowIndex < tblView.getItems().size()) {
						item = tblView.getItems().get(rowIndex);
					} else if (mode.get() == Mode.EDIT) {
						forceUpdateContent();
						/**
						 * Will ensure the content display to TEXT_ONLY because
						 * there is no way to update cell editors value (in
						 * agile editing mode)
						 */
						tblView.getSelectionModel().clearSelection();
						return;//stop pasting as it already touched last row
					}


					if (!lstChangedRow.contains(item)) {
						if (mode.get() == Mode.INSERT) {
							//means that selected row is not new row. Let's create new row
							createNewRow(rowIndex);
							item = tblView.getItems().get(rowIndex);
						} else {
							lstChangedRow.add(item);
						}
					}

					showRow(rowIndex);
					/**
					 * Handle multicolumn paste
					 */
					String[] stringCellValues = line.split("\t");
					TableColumn toFillColumn = startColumn;
					tblView.getSelectionModel().select(rowIndex, toFillColumn);
					for (String stringCellValue : stringCellValues) {
						if (toFillColumn == null) {
							break;
						}
						if (toFillColumn instanceof BaseColumn && toFillColumn.isEditable() && toFillColumn.isVisible()) {
							try {
								Object oldValue = toFillColumn.getCellData(item);
								Object newValue = ((BaseColumn) toFillColumn).convertFromString(stringCellValue);
								PropertyUtils.setSimpleProperty(item, ((BaseColumn) toFillColumn).getPropertyName(), newValue);
								if (mode.get() == Mode.EDIT) {
									((BaseColumn) toFillColumn).addRecordChange(item, oldValue, newValue);
								}
							} catch (Exception ex) {
								MessageDialog.Answer answer = MessageDialogBuilder
										.error(ex)
										.message("msg.paste.error", stringCellValue, toFillColumn.getText())
										.buttonType(MessageDialog.ButtonType.YES_NO)
										.yesOkButtonText("continue.pasting")
										.noButtonText("stop")
										.show(getScene().getWindow());
								if (answer == MessageDialog.Answer.NO) {
									stopPasting = true;
									break;
								}
							}
						}
						tblView.getSelectionModel().selectRightCell();
						TablePosition nextCell = tblView.getSelectionModel().getSelectedCells().get(0);
						if (nextCell.getTableColumn() instanceof BaseColumn && nextCell.getTableColumn() != toFillColumn) {
							toFillColumn = (BaseColumn) nextCell.getTableColumn();
						} else {
							toFillColumn = null;
						}
					}
					rowIndex++;
				}
				forceUpdateContent();
				/**
				 * Will ensure the content display to TEXT_ONLY because there is
				 * no way to update cell editors value (in agile editing mode)
				 */
				tblView.getSelectionModel().clearSelection();
			}
		}
	}

	/**
	 * Dirty method to force table to update its content
	 */
	private void forceUpdateContent() {
		boolean visible = tblView.getColumns().get(0).isVisible();
		tblView.getColumns().get(0).setVisible(false);
		tblView.getColumns().get(0).setVisible(true);
		tblView.getColumns().get(0).setVisible(visible);
	}
	private ScrollBar scrollBar = null;
	private Runnable horizontalScroller = new Runnable() {
		@Override
		public void run() {
			TableView.TableViewFocusModel fm = tblView.getFocusModel();
			if (fm == null) {
				return;
			}

			TableColumn col = fm.getFocusedCell().getTableColumn();
			if (col == null || !col.isVisible()) {
				return;
			}
			if (scrollBar == null) {
				for (Node n : tblView.lookupAll(".scroll-bar")) {
					if (n instanceof ScrollBar) {
						ScrollBar bar = (ScrollBar) n;
						if (bar.getOrientation().equals(Orientation.HORIZONTAL)) {
							scrollBar = bar;
							break;
						}
					}
				}
			}
			// work out where this column header is, and it's width (start -> end)
			double start = 0;
			for (TableColumn c : tblView.getVisibleLeafColumns()) {
				if (c.equals(col)) {
					break;
				}
				start += c.getWidth();
			}
			double end = start + col.getWidth();

			// determine the width of the table
			double headerWidth = tblView.getWidth() - getInsets().getLeft() + getInsets().getRight();

			// determine by how much we need to translate the table to ensure that
			// the start position of this column lines up with the left edge of the
			// tableview, and also that the columns don't become detached from the
			// right edge of the table
			double pos = scrollBar.getValue();
			double max = scrollBar.getMax();
			double newPos = pos;

			if (start < pos && start >= 0) {
				newPos = start;
			} else {
				double delta = start < 0 || end > headerWidth ? start : 0;
				newPos = pos + delta > max ? max : pos + delta;
			}

			// FIXME we should add API in VirtualFlow so we don't end up going
			// direct to the hbar.
			// actually shift the flow - this will result in the header moving
			// as well
			scrollBar.setValue(newPos);
		}
	};

	/**
	 * Get single selected record property. If multiple records are selected, it
	 * returns the last one
	 *
	 * @return
	 */
	public ReadOnlyObjectProperty<T> selectedItemProperty() {
		return tblView.getSelectionModel().selectedItemProperty();
	}

	/**
	 * @see #selectedItemProperty()
	 * @return
	 */
	public T getSelectedItem() {
		return tblView.getSelectionModel().selectedItemProperty().get();
	}

	/**
	 * @see TableView#getSelectionModel()#getSelectionItems()
	 * @return
	 */
	public ObservableList<T> getSelectedItems() {
		return tblView.getSelectionModel().getSelectedItems();
	}

	/**
	 *
	 * @see TableView#getSelectionModel()
	 */
	public TableView.TableViewSelectionModel getSelectionModel() {
		return tblView.getSelectionModel();
	}
	/**
	 * Prevent moving focus to not-inserted-row in INSERT mode
	 */
	private ChangeListener<TablePosition> tableFocusListener = new ChangeListener<TablePosition>() {
		@Override
		public void changed(ObservableValue<? extends TablePosition> observable, TablePosition oldValue, TablePosition newValue) {
			if (!mode.get().equals(Mode.INSERT) || newValue.getRow() == -1 || oldValue.getRow() == -1) {
				return;
			}
			T oldRow = tblView.getItems().get(oldValue.getRow());
			T newRow = tblView.getItems().get(newValue.getRow());
			if (lstChangedRow.contains(oldRow) && !lstChangedRow.contains(newRow)) {
				tblView.getFocusModel().focus(oldValue);
				tblView.getSelectionModel().select(oldValue.getRow(), oldValue.getTableColumn());
			}
		}
	};
	private ContextMenu cm;
	private MenuItem searchMenuItem;
	private EventHandler<MouseEvent> tableRightClickListener = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent event) {
			if (cm.isShowing()) {
				cm.hide();
			}
			if (event.getButton().equals(MouseButton.SECONDARY)) {

				if (tblView.getSelectionModel().getSelectedCells().isEmpty()) {
					return;
				}
				TablePosition pos = tblView.getSelectionModel().getSelectedCells().get(0);
				if (searchMenuItem != null) {
					cm.getItems().remove(searchMenuItem);
				}
				cm.getItems().remove(getPasteMenuItem());
				if (pos.getTableColumn() instanceof BaseColumn) {
					BaseColumn clm = (BaseColumn) pos.getTableColumn();
					clm.setDefaultSearchValue(pos.getTableColumn().getCellData(pos.getRow()));
					
					searchMenuItem = clm.getSearchMenuItem();
					if (searchMenuItem != null && clm.isFilterable()) {
						cm.getItems().add(0, searchMenuItem);
					}
					if (mode.get() != Mode.READ && !isACellInEditing() && Clipboard.getSystemClipboard().hasString()) {
						if (!cm.getItems().contains(getPasteMenuItem())) {
							cm.getItems().add(getPasteMenuItem());
						}
					}
				}
				cm.show(tblView, event.getScreenX(), event.getScreenY());
			}
		}
	};
	private MenuItem miPaste;

	private MenuItem getPasteMenuItem() {
		if (miPaste == null) {
			miPaste = new MenuItem(TiwulFXUtil.getLiteral("paste"));
			miPaste.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					paste();
				}
			});
		}
		return miPaste;
	}

	/**
	 * Add menu item to context menu. It will be displayed under search menu
	 * item
	 *
	 * @param label the label of menu item
	 * @param eventHandler event handler that will be executed when the menu
	 * item is clicked
	 * @deprecated use {@link #addContextMenuItem(javafx.scene.control.MenuItem)} instead 
	 */
	public void addContextMenuItem(String label, EventHandler<ActionEvent> eventHandler) {
		MenuItem mi = new MenuItem(label);
		mi.setOnAction(eventHandler);
		cm.getItems().add(mi);
	}
	
	/**
	 * Add menu item to context menu. The context menu is displayed when right-clicking a row.
	 * @param menuItem 
	 * @see #removeContextMenuItem(javafx.scene.control.MenuItem) 
	 */
	public void addContextMenuItem(MenuItem menuItem) {
		cm.getItems().add(menuItem);
	}
	
	/**
	 * Remove passed menuItem from context menu.
	 * @param menuItem 
	 * @see #addContextMenuItem(javafx.scene.control.MenuItem) 
	 */
	public void removeContextMenuItem(MenuItem menuItem) {
		cm.getItems().remove(menuItem);
	}

	protected void resizeToFit(TableColumn col, int maxRows) {
		List<?> items = tblView.getItems();
		if (items == null || items.isEmpty()) {
			return;
		}

		Callback cellFactory = col.getCellFactory();
		if (cellFactory == null) {
			return;
		}

		TableCell cell = (TableCell) cellFactory.call(col);
		if (cell == null) {
			return;
		}

		// set this property to tell the TableCell we want to know its actual
		// preferred width, not the width of the associated TableColumn
		cell.getProperties().put("deferToParentPrefWidth", Boolean.TRUE);

		// determine cell padding
		double padding = 10;
		Node n = cell.getSkin() == null ? null : cell.getSkin().getNode();
		if (n instanceof Region) {
			Region r = (Region) n;
			padding = r.getInsets().getLeft() + r.getInsets().getRight();
		}

		int rows = maxRows == -1 ? items.size() : Math.min(items.size(), maxRows);
		double maxWidth = 0;
		for (int row = 0; row < rows; row++) {
			cell.updateTableColumn(col);
			cell.updateTableView(tblView);
			cell.updateIndex(row);

			if ((cell.getText() != null && !cell.getText().isEmpty()) || cell.getGraphic() != null) {
				getChildren().add(cell);
				cell.impl_processCSS(false);
				maxWidth = Math.max(maxWidth, cell.prefWidth(-1));
				getChildren().remove(cell);
			}
		}

		col.impl_setWidth(maxWidth + padding);
	}

	public TableControl() {
		this(null);
	}

	/**
	 * 
	 * @return Object set from {@link #setController(com.panemu.tiwulfx.table.TableController)}
	 */
	public TableController getController() {
		return controller;
	}

	/**
	 * Set object responsible to fetch, insert, delete and update data
	 *
	 * @param controller
	 */
	public void setController(TableController<T> controller) {
		this.controller = controller;
	}

	private void initColumn(TableColumn<T, ?> clm) {
		List<TableColumn<T, ?>> lstColumn = new ArrayList<>();
		lstColumn.add(clm);
		lstColumn = getColumnsRecursively(lstColumn);
		for (TableColumn column : lstColumn) {
			if (column instanceof BaseColumn) {
				final BaseColumn baseColumn = (BaseColumn) column;
				baseColumn.tableCriteriaProperty().addListener(tableCriteriaListener);
				baseColumn.sortTypeProperty().addListener(sortTypeChangeListener);
				baseColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<T, ?>>() {
					@Override
					public void handle(CellEditEvent<T, ?> t) {
						try {
							if (!(t.getTablePosition().getRow() < t.getTableView().getItems().size())) {
								return;
							}
							Object persistentObj = t.getTableView().getItems().get(t.getTablePosition().getRow());
							if ((t.getNewValue() == null && t.getOldValue() == null)
									|| (t.getNewValue() != null && t.getNewValue().equals(t.getOldValue()))) {
							}
							if (mode.get().equals(Mode.EDIT)
									&& t.getOldValue() != t.getNewValue()
									&& (t.getOldValue() == null || !t.getOldValue().equals(t.getNewValue()))) {
								if (!lstChangedRow.contains((T) persistentObj)) {
									lstChangedRow.add((T) persistentObj);
								}
								baseColumn.addRecordChange(persistentObj, t.getOldValue(), t.getNewValue());
							}
							PropertyUtils.setSimpleProperty(persistentObj, baseColumn.getPropertyName(), t.getNewValue());
							fireEditCommitEvent(t);
						} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
							throw new RuntimeException(ex);
						}
					}
				});
			}
		}
	}

	/**
	 * Add column to TableView. You can also call {@link TableView#getColumns()}
	 * and then add columns to it.
	 *
	 * @param columns
	 */
	public void addColumn(TableColumn<T, ?>... columns) {
		tblView.getColumns().addAll(columns);
	}

	/**
	 * Get list of columns including the nested ones.
	 *
	 * @param lstColumn
	 * @return
	 */
	private List<TableColumn<T, ?>> getColumnsRecursively(List<TableColumn<T, ?>> lstColumn) {
		List<TableColumn<T, ?>> newColumns = new ArrayList<>();
		for (TableColumn column : lstColumn) {
			if (column.getColumns().isEmpty()) {
				newColumns.add(column);
			} else {
				/**
				 * Should be in new arraylist to avoid
				 * java.lang.IllegalArgumentException: Children: duplicate
				 * children added
				 */
				newColumns.addAll(getColumnsRecursively(new ArrayList(column.getColumns())));
			}
		}
		return newColumns;
	}

	/**
	 * Get list of columns that is hold cell. It excludes columns that are
	 * containers of nested columns.
	 *
	 * @return
	 */
	public List<TableColumn<T, ?>> getLeafColumns() {
		List<TableColumn<T, ?>> result = new ArrayList<>();
		for (TableColumn<T, ?> clm : tblView.getColumns()) {
			if (clm.getColumns().isEmpty()) {
				result.add(clm);
			} else {
				result.addAll(getColumnsRecursively(clm.getColumns()));
			}
		}
		return result;
	}

	/**
	 * Clear all criteria/filters applied to columns then reload the first page.
	 */
	public void clearTableCriteria() {
		setReloadOnCriteriaChange(false);
		for (TableColumn clm : getLeafColumns()) {
			if (clm instanceof BaseColumn) {
				((BaseColumn) clm).setTableCriteria(null);
			}
		}
		setReloadOnCriteriaChange(true);
		reloadFirstPage();
	}

	/**
	 * Reload data on current page. This method is called when pressing reload button TODO: put
	 * it in separate thread and display progress indicator on TableView
	 * @see #reloadFirstPage() 
	 */
	public void reload() {
		if (!lstChangedRow.isEmpty()) {
			if (!controller.revertConfirmation(this, lstChangedRow.size())) {
				return;
			}
		}
		lstCriteria.clear();
		/**
		 * Should be in new arraylist to avoid
		 * java.lang.IllegalArgumentException: Children: duplicate children
		 * added
		 */
		List<TableColumn<T, ?>> lstColumns = new ArrayList<>(tblView.getColumns());
		lstColumns = getColumnsRecursively(lstColumns);
		for (TableColumn clm : lstColumns) {
			if (clm instanceof BaseColumn) {
				BaseColumn baseColumn = (BaseColumn) clm;
				if (baseColumn.getTableCriteria() != null) {
					lstCriteria.add(baseColumn.getTableCriteria());
				}
			}
		}
		List<String> lstSortedColumn = new ArrayList<>();
		List<SortType> lstSortedType = new ArrayList<>();
		for (TableColumn<T, ?> tc : tblView.getSortOrder()) {
			if (tc instanceof BaseColumn) {
				lstSortedColumn.add(((BaseColumn) tc).getPropertyName());
			} else {
				PropertyValueFactory valFactory = (PropertyValueFactory) tc.getCellValueFactory();
				lstSortedColumn.add(valFactory.getProperty());
			}
			lstSortedType.add(tc.getSortType());
		}
		final TableData vol = controller.loadData(startIndex.get(), lstCriteria, lstSortedColumn, lstSortedType, maxResult.get());
		totalRows = vol.getTotalRows();

		//keep track of previous selected row
		int selectedIndex = tblView.getSelectionModel().getSelectedIndex();
		TableColumn selectedColumn = null;
		if (!tblView.getSelectionModel().getSelectedCells().isEmpty()) {
			selectedColumn = tblView.getSelectionModel().getSelectedCells().get(0).getTableColumn();
		}

		if (isACellInEditing()) {
			/**
			 * Trigger cancelEdit if there is cell being edited. Otherwise
			 * ArrayIndexOutOfBound exception happens since tblView items are
			 * cleared (see next lines) but setOnEditCommit listener is
			 * executed.
			 */
			tblView.edit(-1, tblView.getColumns().get(0));
		}

		//clear items and add with objects that has just been retrieved
		tblView.getItems().clear();
		tblView.getItems().addAll((Collection<T>) vol.getRows());

		if (selectedIndex < vol.getRows().size()) {
			tblView.getSelectionModel().select(selectedIndex, selectedColumn);
		} else {
			tblView.getSelectionModel().select(vol.getRows().size() - 1, selectedColumn);
		}

		long page = vol.getTotalRows() / maxResult.get();
		if (vol.getTotalRows() % maxResult.get() != 0) {
			page++;
		}
		cmbPage.setDisable(page == 0);
		startIndex.removeListener(startIndexChangeListener);
		cmbPage.getItems().clear();
		for (int i = 1; i <= page; i++) {
			cmbPage.getItems().add(i);
		}
		cmbPage.getSelectionModel().select((int) (startIndex.get() / maxResult.get()));
		startIndex.addListener(startIndexChangeListener);
		toggleButtons(page > 0, vol.isMoreRows());

		mode.set(Mode.READ);
		
		clearChange();
		if (fitColumnAfterReload) {
			for (TableColumn clm : tblView.getColumns()) {
				resizeToFit(clm, -1);
			}
		}
		lblTotalRow.setText(TiwulFXUtil.getLiteral("total.record.param", totalRows));
		controller.postLoadData();
	}
	
	private void clearChange() {
		lstChangedRow.clear();
        for (TableColumn clm : getLeafColumns()) {
            if (clm instanceof BaseColumn) {
                ((BaseColumn) clm).clearRecordChange();
            }
        }
    }
	
	/**
	 * Get list of change happens on cells. It is useful to get detailed information of old and new values
	 * of particular record's property
	 * @return 
	 */
	public List<RecordChange<T,?>> getRecordChangeList() {
		List<RecordChange<T,?>> lstRecordChange = new ArrayList<>();
		for (TableColumn column : getLeafColumns()) {
			if (column instanceof BaseColumn) {
				BaseColumn<T,?> baseColumn = (BaseColumn) column;
				Map<T, RecordChange<T,?>> map = (Map<T, RecordChange<T,?>>) baseColumn.getRecordChangeMap();
				lstRecordChange.addAll(map.values());
			}
		}
		return lstRecordChange;
	}

	private void toggleButtons(boolean notEmpty, boolean moreRows) {
		boolean firstPage = startIndex.get() == 0;
		btnFirstPage.setDisable(firstPage && notEmpty);
		btnPrevPage.setDisable(firstPage && notEmpty);
		btnNextPage.setDisable(!moreRows);
		btnLastPage.setDisable(!moreRows);
	}

	/**
	 * Reload data from the first page.
	 *
	 */
	public void reloadFirstPage() {
		if (startIndex.get() != 0) {
			/**
			 * it will automatically reload data. See StartIndexChangeListener
			 */
			startIndex.set(0);
		} else {
			reload();
		}
	}

	private void lastPageFired(ActionEvent event) {
		cmbPage.getSelectionModel().selectLast();
	}

	private void prevPageFired(ActionEvent event) {
		cmbPage.getSelectionModel().selectPrevious();
	}

	private void nextPageFired(ActionEvent event) {
		cmbPage.getSelectionModel().selectNext();
	}

	private void pageChangeFired(ActionEvent event) {
		if (cmbPage.getValue() != null) {
			page = Integer.valueOf(cmbPage.getValue() + "");//since the combobox is editable, it might have String value
			page = page - 1;
			startIndex.set(page * maxResult.get());
		}
	}

	/**
	 * Return false if the insertion is canceled because the controller return
	 * null object. It is controller's way to abort insertion.
	 *
	 * @param rowIndex
	 * @return
	 */
	private void createNewRow(int rowIndex) {
		T newRecord;
		try {
			newRecord = (T) Class.forName(recordClass.getName()).newInstance();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}

		if (tblView.getItems().size() == 0) {
			rowIndex = 0;
		}

		tblView.getItems().add(rowIndex, newRecord);
		lstChangedRow.add(newRecord);
	}

	/**
	 * Add new row under selected row or in the first row if there is no row
	 * selected. This method is called when pressing insert button
	 */
	public void insert() {
		if (recordClass == null) {
			throw new RuntimeException("Cannot add new row because the class of the record is undefined.\nPlease call setRecordClass(Class<T> recordClass)");
		}
		T newRecord;
		try {
			newRecord = (T) Class.forName(recordClass.getName()).newInstance();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}
		newRecord = controller.preInsert(newRecord);
		if (newRecord == null) {
			return;
		}

		int selectedRow = tblView.getSelectionModel().getSelectedIndex() + 1;
		if (tblView.getItems().size() == 0) {
			selectedRow = 0;
		}

		tblView.getItems().add(selectedRow, newRecord);
		lstChangedRow.add(newRecord);
		final int row = selectedRow;
		mode.set(Mode.INSERT);

		/**
		 * Force the table to layout before selecting the newly added row.
		 * Without this call, the selection will land on existing row at
		 * specified index because the new row is not yet actually added to the
		 * table. It makes the editor controls are not displayed in agileEditing
		 * mode.
		 */
		tblView.layout();
		tblView.requestFocus();
		showRow(row);
		tblView.getSelectionModel().select(row, tblView.getColumns().get(0));
	}

	/**
	 * Save changes. This method is called when pressing save button
	 *
	 */
	public void save() {
		/**
		 * In case there is a cell being edited, call selectFirst to trigger
		 * commitEdit() in the edited cell.
		 */
		tblView.getSelectionModel().selectFirst();

		try {
			if (lstChangedRow.isEmpty()) {
				mode.set(Mode.READ);
				return;
			}
			if (!controller.validate(this, lstChangedRow)) {
				return;
			}
			List<T> lstResult = new ArrayList<>();
			Mode prevMode = mode.get();
			if (mode.get().equals(Mode.EDIT)) {
				lstResult = controller.update(lstChangedRow);
			} else if (mode.get().equals(Mode.INSERT)) {
				lstResult = controller.insert(lstChangedRow);
			}
			mode.set(Mode.READ);
			/**
			 * In case objects in lstResult differ with original object. Ex: In
			 * SOA architecture, sent objects always differ with received object
			 * due to serialization.
			 */
			int i = 0;
			for (T row : lstChangedRow) {
				int index = tblView.getItems().indexOf(row);
				tblView.getItems().remove((int) index);
				tblView.getItems().add(index, lstResult.get(i));
				i++;
			}

			/**
			 * Refresh cells. They won't refresh automatically if the entity's
			 * properties bound to the cells are not javaFX property object.
			 */
			forceUpdateContent();
			clearChange();
			controller.postSave(prevMode);
		} catch (Exception ex) {
			MessageDialogBuilder.error(ex).message("An error occured").show(TableControl.this.getScene().getWindow());
		}
	}

	/**
	 * Edit table. This method is called when pressing edit button.
	 *
	 */
	public void edit() {
		if (controller.canEdit(tblView.getSelectionModel().getSelectedItem())) {
			mode.set(Mode.EDIT);
		}
	}

	/**
	 * Delete selected row. This method is called when pressing delete button.
	 * It will delete selected record(s)
	 *
	 */
	public void delete() {
		/**
		 * Delete row that is not yet persisted in database.
		 */
		if (mode.get() == Mode.INSERT) {
			TablePosition selectedCell = tblView.getSelectionModel().getSelectedCells().get(0);
			int selectedRow = selectedCell.getRow();
			lstChangedRow.removeAll(tblView.getSelectionModel().getSelectedItems());
			tblView.getSelectionModel().clearSelection();// it is needed if agile editing is enabled to trigger content display change later
			tblView.getItems().remove(selectedRow);
			tblView.layout();//relayout first before set selection. Without this, cell contend display won't be set propertly
			tblView.requestFocus();
			if (selectedRow == tblView.getItems().size()) {
				selectedRow--;
			}
			if (lstChangedRow.contains(tblView.getItems().get(selectedRow))) {
				tblView.getSelectionModel().select(selectedRow, selectedCell.getTableColumn());
			} else {
				tblView.getSelectionModel().select(selectedRow - 1, selectedCell.getTableColumn());
			}
			return;
		}


		/**
		 * Delete persistence record.
		 */
		try {
			if (!controller.canDelete(this)) {
				return;
			}
			int selectedRow = tblView.getSelectionModel().getSelectedIndex();
			List<T> lstToDelete = new ArrayList<>(tblView.getSelectionModel().getSelectedItems());
			controller.delete(lstToDelete);
			tblView.getItems().removeAll(lstToDelete);
			/**
			 * select a row
			 */
			if (!tblView.getItems().isEmpty()) {
				if (selectedRow >= tblView.getItems().size()) {
					tblView.getSelectionModel().select(tblView.getItems().size() - 1);
				} else {
					tblView.getSelectionModel().select(selectedRow);
				}
			}
			tblView.requestFocus();
		} catch (Exception ex) {
			MessageDialogBuilder.error(ex).message("An error occured").show(this.getScene().getWindow());
		}
	}

	/**
	 * Export table to Excel. All pages will be exported. The criteria set on
	 * columns are taken into account. This method is called by export button.
	 */
	public void export() {
		controller.exportToExcel("Override TableController.exportToExcel to reset the title.", maxResult.get(), this, lstCriteria);
	}

	private class StartIndexChangeListener implements ChangeListener<Number> {

		@Override
		public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
			reload();
		}
	}
	private InvalidationListener tableCriteriaListener = new InvalidationListener() {
		@Override
		public void invalidated(Observable observable) {
			if (reloadOnCriteriaChange) {
				reloadFirstPage();
			}
		}
	};

	private class SortTypeChangeListener implements InvalidationListener {

		@Override
		public void invalidated(Observable o) {
			/**
			 * If the column is not in sortOrder list, just ignore. It avoids
			 * intermittent duplicate reload() calling
			 */
			TableColumn col = (TableColumn) ((SimpleObjectProperty) o).getBean();
			if (!tblView.getSortOrder().contains(col)) {
				return;
			}
			reload();
		}
	}

	public void addObjectExposer(ObjectExposer exposer) {
		lstExposer.add(exposer);
	}
	private IntegerProperty maxResult = new SimpleIntegerProperty(TiwulFXUtil.DEFAULT_TABLE_MAX_ROW);

	/**
	 * Set max record per retrieval
	 *
	 * @param maxRecord
	 */
	public void setMaxRecord(int maxRecord) {
		this.maxResult.set(maxRecord);
	}

	/**
	 * Get max number of records per-retrieval.
	 * @return 
	 */
	public int getMaxRecord() {
		return maxResult.get();
	}

	public IntegerProperty maxRecordProperty() {
		return maxResult;
	}

	/**
	 * 
	 * @return Class object set on {@link #setRecordClass(java.lang.Class) }
	 * @see #setRecordClass(java.lang.Class) 
	 */
	public Class<T> getRecordClass() {
		return recordClass;
	}

	/**
	 * Set the class of object that will be displayed in the table.
	 *
	 * @param recordClass
	 */
	public void setRecordClass(Class<T> recordClass) {
		this.recordClass = recordClass;
	}

	public void setFitColumnAfterReload(boolean fitColumnAfterReload) {
		this.fitColumnAfterReload = fitColumnAfterReload;
	}

	/**
	 * 
	 * @return 
	 * @see #setReloadOnCriteriaChange(boolean) 
	 */
	public boolean isReloadOnCriteriaChange() {
		return reloadOnCriteriaChange;
	}

	/**
	 * Set it to false to prevent auto-reloading when there is table criteria
	 * change. It is useful if we want to change tableCriteria of several
	 * columns at a time. After that set it to true and call {@link TableControl#reloadFirstPage()
	 * }
	 *
	 * @param reloadOnCriteriaChange
	 */
	public void setReloadOnCriteriaChange(boolean reloadOnCriteriaChange) {
		this.reloadOnCriteriaChange = reloadOnCriteriaChange;
	}

	/**
	 * Get displayed record. It is just the same with
	 * {@link TableView#getItems()}
	 *
	 * @return
	 */
	public ObservableList<T> getRecords() {
		return tblView.getItems();
	}

	private void setOrNot(ToolBar parent, Node control, boolean visible) {
		if (!visible) {
			parent.getItems().remove(control);
		} else if (!parent.getItems().contains(control)) {
			parent.getItems().add(control);
		}
	}

	/**
	 * Add button to toolbar. The button's style is set by this method. Make
	 * sure to add image on the button and also define the action method.
	 *
	 * @param btn
	 */
	public void addButton(Button btn) {
		btn.getStyleClass().add("flat-button");
		boolean hasPagination = toolbar.getItems().contains(paginationBox);
		if (hasPagination) {
			toolbar.getItems().remove(spacer);
			toolbar.getItems().remove(paginationBox);
		}
		toolbar.getItems().add(btn);
		if (hasPagination) {
			toolbar.getItems().add(spacer);
			toolbar.getItems().add(paginationBox);
		}
	}

	/**
	 * Set UI component visibility.
	 *
	 * @param visible
	 * @param controls
	 */
	public void setVisibleComponents(boolean visible, TableControl.Component... controls) {
		for (Component comp : controls) {
			switch (comp) {
				case BUTTON_DELETE:
					setOrNot(toolbar, btnDelete, visible);
					break;
				case BUTTON_EDIT:
					setOrNot(toolbar, btnEdit, visible);
					break;
				case BUTTON_INSERT:
					setOrNot(toolbar, btnAdd, visible);
					break;
				case BUTTON_EXPORT:
					setOrNot(toolbar, btnExport, visible);
					break;
				case BUTTON_PAGINATION:
					setOrNot(toolbar, spacer, visible);
					setOrNot(toolbar, paginationBox, visible);
					break;
				case BUTTON_RELOAD:
					setOrNot(toolbar, btnReload, visible);
					break;
				case BUTTON_SAVE:
					setOrNot(toolbar, btnSave, visible);
					break;
				case FOOTER:
					if (!visible) {
						this.getChildren().remove(footer);
					} else if (!this.getChildren().contains(footer)) {
						this.getChildren().add(footer);
					}
					break;
				case TOOLBAR:
					if (!visible) {
						this.getChildren().remove(toolbar);
					} else if (!this.getChildren().contains(toolbar)) {
						this.getChildren().add(0, toolbar);
					}
					break;
			}
		}
	}

	public Mode getMode() {
		return mode.get();
	}

	public ReadOnlyObjectProperty<Mode> modeProperty() {
		return mode.getReadOnlyProperty();
	}

	public TableView<T> getTableView() {
		return tblView;
	}

	public final ReadOnlyObjectProperty<TablePosition<T, ?>> editingCellProperty() {
		return tblView.editingCellProperty();
	}

	/**
	 * Add listener that will be called when a cell receive new value from user.
	 *
	 * @param listener
	 */
	public void addEditCommitListener(EditCommitListener<T> listener) {
		if (!lstEditCommitListener.contains(listener)) {
			lstEditCommitListener.add(listener);
		}
	}

	/**
	 * 
	 * @param listener 
	 * @see #addEditCommitListener(com.panemu.tiwulfx.table.EditCommitListener) 
	 */
	public void removeEditCommitListener(EditCommitListener listener) {
		lstEditCommitListener.remove(listener);
	}

	private void fireEditCommitEvent(TableColumn.CellEditEvent<T, ?> t) {
		for (EditCommitListener listener : lstEditCommitListener) {
			listener.valueCommitted(t);
		}
	}

	/**
	 * Check if a record is editable. After ensure that the item is not null and
	 * the mode is not {@link Mode#INSERT} it will propagate the call to
	 * {@link TableController#isRecordEditable}.
	 *
	 * @see TableController#isRecordEditable()
	 * @param item
	 * @return false if item == null. True if mode is INSERT. otherwise depends
	 * on the logic in {@link TableController#isRecordEditable}
	 */
	public final boolean isRecordEditable(T item) {
		if (item == null) {
			return false;
		}
		if (mode.get() == Mode.INSERT) {
			return true;
		}
		return controller.isRecordEditable(item);
	}
}

