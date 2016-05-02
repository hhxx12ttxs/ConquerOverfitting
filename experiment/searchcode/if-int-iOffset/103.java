package org.pentaho.di.ui.core.widget;

/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
/**********************************************************************
 **                                                                   **
 ** This Script has been developed for more StyledText Enrichment     **
 ** December-2006 by proconis GmbH / Germany                          **
 **                                                                   ** 
 ** http://www.proconis.de                                            **
 ** info@proconis.de                                                  **
 **                                                                   **
 **********************************************************************/

import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.ingres.pentaho.sqleditor.util.Messages;

public class SqlStyledTextComposite extends Composite {

	// Modification for Undo/Redo on Styled Text
	private static final int MAX_STACK_SIZE = 25;
	private final List<SqlSyledTextUndoRedoStack> undoStack;
	private final List<SqlSyledTextUndoRedoStack> redoStack;
	private boolean bFullSelection = false;
	private final StyledText styledText;
	private final Menu styledTextPopupmenu;
	private final Composite xParent;
	private final KeyListener kls;
	private MenuDetectListener menuDetectListener = null;

	/**
	 * Create a Styled Text Component
	 * 
	 * @param parent
	 * @param args
	 * @param strTabName
	 */
	public SqlStyledTextComposite(final Composite parent, final int args) {
		super(parent, SWT.NONE);
		this.undoStack = new LinkedList<SqlSyledTextUndoRedoStack>();
		this.redoStack = new LinkedList<SqlSyledTextUndoRedoStack>();
		this.styledText = new StyledText(this, args);
		this.styledTextPopupmenu = new Menu(parent.getShell(), SWT.POP_UP);
		this.xParent = parent;

		this.setLayout(new FillLayout());
		this.buildStyledTextMenu();
		this.addUndoRedoSupport();

		// Set the line styler for SQL
		final SqlLineStyle lineStyler = new SqlLineStyle();
		this.styledText.addLineStyleListener(lineStyler);

		// Create all of the keyboard equivalents
		this.kls = new KeyAdapter() {
			@Override
			public void keyReleased(final KeyEvent e) {
				if (e.keyCode == 'h' && (e.stateMask & SWT.CTRL) != 0) {
					new SqlStyledTextReplaceDialog(
							SqlStyledTextComposite.this.styledTextPopupmenu
									.getShell(),
							SqlStyledTextComposite.this.styledText).open();
				} else if (e.keyCode == 'z' && (e.stateMask & SWT.CTRL) != 0) {
					SqlStyledTextComposite.this.undo();
				} else if (e.keyCode == 'y' && (e.stateMask & SWT.CTRL) != 0) {
					SqlStyledTextComposite.this.redo();
				} else if (e.keyCode == 'a' && (e.stateMask & SWT.CTRL) != 0) {
					SqlStyledTextComposite.this.bFullSelection = true;
					SqlStyledTextComposite.this.styledText.selectAll();
				} else if (e.keyCode == 'f' && (e.stateMask & SWT.CTRL) != 0) {
					SqlStyledTextComposite.this.openFindWindow();
				}
			}
		};
		this.styledText.addKeyListener(this.kls);

		// Create the drop target on the StyledText (for drag and drop)
		final DropTarget dt = new DropTarget(this.styledText, DND.DROP_MOVE);
		dt.setTransfer(new Transfer[] { TextTransfer.getInstance() });
		dt.addDropListener(new DropTargetAdapter() {
			@Override
			public void dragOver(final DropTargetEvent e) {
				SqlStyledTextComposite.this.styledText.setFocus();
				final Point location = SqlStyledTextComposite.this.xParent
						.getDisplay().map(null,
								SqlStyledTextComposite.this.styledText, e.x,
								e.y);
				location.x = Math.max(0, location.x);
				location.y = Math.max(0, location.y);
				try {
					final int offset = SqlStyledTextComposite.this.styledText
							.getOffsetAtLocation(new Point(location.x,
									location.y));
					SqlStyledTextComposite.this.styledText
							.setCaretOffset(offset);
				} catch (final IllegalArgumentException ex) {
					final int maxOffset = SqlStyledTextComposite.this.styledText
							.getCharCount();
					final Point maxLocation = SqlStyledTextComposite.this.styledText
							.getLocationAtOffset(maxOffset);
					if (location.y >= maxLocation.y) {
						if (location.x >= maxLocation.x) {
							SqlStyledTextComposite.this.styledText
									.setCaretOffset(maxOffset);
						} else {
							final int offset = SqlStyledTextComposite.this.styledText
									.getOffsetAtLocation(new Point(location.x,
											maxLocation.y));
							SqlStyledTextComposite.this.styledText
									.setCaretOffset(offset);
						}
					} else {
						SqlStyledTextComposite.this.styledText
								.setCaretOffset(maxOffset);
					}
				}
			}

			@Override
			public void drop(final DropTargetEvent event) {
				// Set the buttons text to be the text being dropped
				SqlStyledTextComposite.this.styledText
						.insert((String) event.data);
			}
		});

	}

	public String getSelectionText() {
		return this.styledText.getSelectionText();
	}

	public String getText() {
		return this.styledText.getText();
	}

	public void setText(final String text) {
		this.styledText.setText(text);
	}

	public int getCaretOffset() {
		return this.styledText.getCaretOffset();
	}

	public int getLineAtOffset(final int iOffset) {
		return this.styledText.getLineAtOffset(iOffset);
	}

	public void insert(final String strInsert) {
		this.styledText.insert(strInsert);
	}

	public void addModifyListener(final ModifyListener lsMod) {
		this.styledText.addModifyListener(lsMod);
	}

	public void addLineStyleListener(final LineStyleListener lineStyler) {
		this.styledText.addLineStyleListener(lineStyler);
	}

	public void addKeyListener(final KeyAdapter keyAdapter) {
		this.styledText.addKeyListener(keyAdapter);
	}

	public void addFocusListener(final FocusAdapter focusAdapter) {
		this.styledText.addFocusListener(focusAdapter);
	}

	public void addMouseListener(final MouseAdapter mouseAdapter) {
		this.styledText.addMouseListener(mouseAdapter);
	}

	public int getSelectionCount() {
		return this.styledText.getSelectionCount();
	}

	public void setSelection(final int arg0) {
		this.styledText.setSelection(arg0);
	}

	public void setSelection(final int arg0, final int arg1) {
		this.styledText.setSelection(arg0, arg1);

	}

	@Override
	public void setFont(final Font fnt) {
		this.styledText.setFont(fnt);
	}

	private void buildStyledTextMenu() {
		final MenuItem undoItem = new MenuItem(this.styledTextPopupmenu,
				SWT.PUSH);
		undoItem.setText(Messages.SqlStyledTextComposite_UNDO);
		undoItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(final Event e) {
				SqlStyledTextComposite.this.undo();
			}
		});

		final MenuItem redoItem = new MenuItem(this.styledTextPopupmenu,
				SWT.PUSH);
		redoItem.setText(Messages.SqlStyledTextComposite_REDO);
		redoItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(final Event e) {
				SqlStyledTextComposite.this.redo();
			}
		});

		new MenuItem(this.styledTextPopupmenu, SWT.SEPARATOR);
		final MenuItem cutItem = new MenuItem(this.styledTextPopupmenu,
				SWT.PUSH);
		cutItem.setText(Messages.SqlStyledTextComposite_CUT);
		cutItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(final Event e) {
				SqlStyledTextComposite.this.styledText.cut();
			}
		});

		final MenuItem copyItem = new MenuItem(this.styledTextPopupmenu,
				SWT.PUSH);
		copyItem.setText(Messages.SqlStyledTextComposite_COPY);
		copyItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(final Event e) {
				SqlStyledTextComposite.this.styledText.copy();
			}
		});

		final MenuItem pasteItem = new MenuItem(this.styledTextPopupmenu,
				SWT.PUSH);
		pasteItem.setText(Messages.SqlStyledTextComposite_PASTE);
		pasteItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(final Event e) {
				SqlStyledTextComposite.this.styledText.paste();
			}
		});

		final MenuItem selectAllItem = new MenuItem(this.styledTextPopupmenu,
				SWT.PUSH);
		selectAllItem.setText(Messages.SqlStyledTextComposite_SELECTALL);
		selectAllItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(final Event e) {
				SqlStyledTextComposite.this.styledText.selectAll();
			}
		});

		new MenuItem(this.styledTextPopupmenu, SWT.SEPARATOR);
		final MenuItem findItem = new MenuItem(this.styledTextPopupmenu,
				SWT.PUSH);
		findItem.setText(Messages.SqlStyledTextComposite_FIND);
		// accelerator is ctrl-f, set above
		findItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(final Event e) {
				SqlStyledTextComposite.this.openFindWindow();
			}
		});
		final MenuItem replaceItem = new MenuItem(this.styledTextPopupmenu,
				SWT.PUSH);
		replaceItem.setText(Messages.SqlStyledTextComposite_REPLACE);
		replaceItem.setAccelerator(SWT.CTRL + 'H');
		replaceItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(final Event e) {
				final SqlStyledTextReplaceDialog stReplace = new SqlStyledTextReplaceDialog(
						SqlStyledTextComposite.this.styledText.getShell(),
						SqlStyledTextComposite.this.styledText);
				stReplace.open();
			}
		});

		this.menuDetectListener = new MenuDetectListener() {
			@Override
			public void menuDetected(final MenuDetectEvent e) {
				// Enable menus, if the Selection is ok
				if (SqlStyledTextComposite.this.undoStack.size() > 0) {
					SqlStyledTextComposite.this.styledTextPopupmenu.getItem(0)
							.setEnabled(true);
				} else {
					SqlStyledTextComposite.this.styledTextPopupmenu.getItem(0)
							.setEnabled(false);
				}

				if (SqlStyledTextComposite.this.redoStack.size() > 0) {
					SqlStyledTextComposite.this.styledTextPopupmenu.getItem(1)
							.setEnabled(true);
				} else {
					SqlStyledTextComposite.this.styledTextPopupmenu.getItem(1)
							.setEnabled(false);
				}

				SqlStyledTextComposite.this.styledTextPopupmenu.getItem(5)
						.setEnabled(SqlStyledTextComposite.this.checkPaste());
				if (SqlStyledTextComposite.this.styledText.getSelectionCount() > 0) {
					SqlStyledTextComposite.this.styledTextPopupmenu.getItem(3)
							.setEnabled(true);
					SqlStyledTextComposite.this.styledTextPopupmenu.getItem(4)
							.setEnabled(true);
				} else {
					SqlStyledTextComposite.this.styledTextPopupmenu.getItem(3)
							.setEnabled(false);
					SqlStyledTextComposite.this.styledTextPopupmenu.getItem(4)
							.setEnabled(false);
				}
			}
		};
		this.styledText.addMenuDetectListener(this.menuDetectListener);
		this.styledText.setMenu(this.styledTextPopupmenu);
	}

	/**
	 * Disable all options in the popup menu
	 */
	public void ablePopupMenuOptions(final Boolean enable) {
		final int size = this.styledTextPopupmenu.getItemCount();
		for (int i = 0; i < size; i++) {
			this.styledTextPopupmenu.getItem(i).setEnabled(enable);
		}
	}

	/**
	 * Disable the pop up menu.
	 */
	public void disablePopupMenu() {
		this.styledText.removeMenuDetectListener(this.menuDetectListener);
		this.menuDetectListener = null;
		this.styledTextPopupmenu.dispose();
	}

	// Check if something is stored inside the Clipboard
	private boolean checkPaste() {
		try {
			final Clipboard clipboard = new Clipboard(this.xParent.getDisplay());
			final TextTransfer transfer = TextTransfer.getInstance();
			final String text = (String) clipboard.getContents(transfer);
			if (text != null && text.length() > 0) {
				return true;
			} else {
				return false;
			}
		} catch (final Exception e) {
			return false;
		}
	}

	// Start Functions for Undo / Redo on wSrcipt
	private void addUndoRedoSupport() {

		this.styledText.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				if (SqlStyledTextComposite.this.styledText.getSelectionCount() == SqlStyledTextComposite.this.styledText
						.getCharCount()) {
					SqlStyledTextComposite.this.bFullSelection = true;
					try {
						event.wait(2);
					} catch (final Exception e) {
					}
				}
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent event) {
			}
		});

		this.styledText.addExtendedModifyListener(new ExtendedModifyListener() {
			@Override
			public void modifyText(final ExtendedModifyEvent event) {
				int iEventLength = event.length;
				final int iEventStartPostition = event.start;

				// Delete or Insert
				final String newText = SqlStyledTextComposite.this.styledText
						.getText();
				final String repText = event.replacedText;
				String oldText = ""; //$NON-NLS-1$
				int iEventType = -1;

				// if((event.length!=newText.length()) || newText.length()==1){
				if ((event.length != newText.length())
						|| (SqlStyledTextComposite.this.bFullSelection)) {
					if (repText != null && repText.length() > 0) {
						oldText = newText.substring(0, event.start) + repText
								+ newText.substring(event.start + event.length);
						iEventType = SqlSyledTextUndoRedoStack.DELETE;
						iEventLength = repText.length();
					} else {
						oldText = newText.substring(0, event.start)
								+ newText.substring(event.start + event.length);
						iEventType = SqlSyledTextUndoRedoStack.INSERT;
					}

					if ((oldText != null && oldText.length() > 0)
							|| (iEventStartPostition == event.length)) {
						final SqlSyledTextUndoRedoStack urs = new SqlSyledTextUndoRedoStack(
								iEventStartPostition, newText, oldText,
								iEventLength, iEventType);
						if (SqlStyledTextComposite.this.undoStack.size() == MAX_STACK_SIZE) {
							SqlStyledTextComposite.this.undoStack
									.remove(SqlStyledTextComposite.this.undoStack
											.size() - 1);
						}
						SqlStyledTextComposite.this.undoStack.add(0, urs);
					}
				}
				SqlStyledTextComposite.this.bFullSelection = false;
			}
		});

	}

	private void undo() {
		if (this.undoStack.size() > 0) {
			final SqlSyledTextUndoRedoStack urs = this.undoStack.remove(0);
			if (this.redoStack.size() == MAX_STACK_SIZE) {
				this.redoStack.remove(this.redoStack.size() - 1);
			}
			final SqlSyledTextUndoRedoStack rro = new SqlSyledTextUndoRedoStack(
					urs.getCursorPosition(), urs.getReplacedText(),
					this.styledText.getText(), urs.getEventLength(),
					urs.getType());
			this.bFullSelection = false;
			this.styledText.setText(urs.getReplacedText());
			if (urs.getType() == SqlSyledTextUndoRedoStack.INSERT) {
				this.styledText.setCaretOffset(urs.getCursorPosition());
			} else if (urs.getType() == SqlSyledTextUndoRedoStack.DELETE) {
				this.styledText.setCaretOffset(urs.getCursorPosition()
						+ urs.getEventLength());
				this.styledText.setSelection(urs.getCursorPosition(),
						urs.getCursorPosition() + urs.getEventLength());
				if (this.styledText.getSelectionCount() == this.styledText
						.getCharCount()) {
					this.bFullSelection = true;
				}
			}
			this.redoStack.add(0, rro);
		}
	}

	private void redo() {
		if (this.redoStack.size() > 0) {
			final SqlSyledTextUndoRedoStack urs = this.redoStack.remove(0);
			if (this.undoStack.size() == MAX_STACK_SIZE) {
				this.undoStack.remove(this.undoStack.size() - 1);
			}
			final SqlSyledTextUndoRedoStack rro = new SqlSyledTextUndoRedoStack(
					urs.getCursorPosition(), urs.getReplacedText(),
					this.styledText.getText(), urs.getEventLength(),
					urs.getType());
			this.bFullSelection = false;
			this.styledText.setText(urs.getReplacedText());
			if (urs.getType() == SqlSyledTextUndoRedoStack.INSERT) {
				this.styledText.setCaretOffset(urs.getCursorPosition());
			} else if (urs.getType() == SqlSyledTextUndoRedoStack.DELETE) {
				this.styledText.setCaretOffset(urs.getCursorPosition()
						+ urs.getEventLength());
				this.styledText.setSelection(urs.getCursorPosition(),
						urs.getCursorPosition() + urs.getEventLength());
				if (this.styledText.getSelectionCount() == this.styledText
						.getCharCount()) {
					this.bFullSelection = true;
				}
			}
			this.undoStack.add(0, rro);
		}
	}

	/**
	 * Open the Find Window
	 */
	public void openFindWindow() {
		final SqlStyledTextFindDialog stFind = new SqlStyledTextFindDialog(
				SqlStyledTextComposite.this.styledText.getShell(),
				SqlStyledTextComposite.this.styledText);
		stFind.open();
	}

	public StyledText getStyledText() {
		return this.styledText;
	}

	public Menu getStyledTextPopupmenu() {
		return this.styledTextPopupmenu;
	}
}

