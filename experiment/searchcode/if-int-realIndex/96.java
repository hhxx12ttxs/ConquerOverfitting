package com.suijten.bordermaker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.suijten.bordermaker.actions.AddAction;
import com.suijten.bordermaker.actions.CopyAction;
import com.suijten.bordermaker.actions.DeleteAction;
import com.suijten.bordermaker.actions.DownAction;
import com.suijten.bordermaker.actions.EditAction;
import com.suijten.bordermaker.actions.GoDownAction;
import com.suijten.bordermaker.actions.GoUpAction;
import com.suijten.bordermaker.actions.OkActionListener;
import com.suijten.bordermaker.actions.UpAction;

public class BorderMakerTable<T extends Serializable> extends JPanel {
	private JTable table;
	private JScrollPane tableScroll;
	private JToolBar tableBar;
	private JButton editButton;
	private JButton addButton;
	private JButton deleteButton;
	private JButton moveUpButton;
	private JButton moveDownButton;
	private JButton copyButton;
	private AddAction addAction;
	private EditAction editAction;
	private DeleteAction deleteAction;
	private UpAction upAction;
	private DownAction downAction;
	private CopyAction copyAction;
	private BorderMakerBean bean;
	private OkActionListener okActionListener;
	private ChangeListener changeListener;
	private List<T> beans;
	private Class<T> cls;
	private GoUpAction goUpAction;
	private GoDownAction goDownAction;
	

	public BorderMakerTable(OkActionListener okActionListener) {
		this.okActionListener = okActionListener;
		initializeComponents();
	}
	
	public void setSelected(int row) { 
		table.setRowSelectionInterval(row, row);
		JTableScrolling.makeRowVisible(table, row);
	}
	
	public void init(BorderMakerBean bean, final List<T> beans, Class<T> cls, TableModel tableModel) {
		this.bean = bean;
		this.beans = beans;
		this.cls = cls;
		table.setModel(tableModel);

        if(tableModel instanceof TooltipTableModel) {
        	for (int i = 0; i < table.getColumnCount(); i++) {
        		int width = ((TooltipTableModel)tableModel).getColumnWidth(i);
        		if(width > 0) {
        			table.getColumnModel().getColumn(i).setPreferredWidth(width);
        		}
			}
        }
        
		addAction = new AddAction(beans, table, bean, cls) {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(okActionListener != null) {
					showDialog(-1);
				} else {
					super.actionPerformed(e);
				}
			}
		};
		
		editAction = new EditAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(okActionListener != null) {
					showDialog(table.getSelectedRow());
				}
			}
		};
		
		deleteAction = new DeleteAction(beans, table);
		upAction = new UpAction(beans, table);
		downAction = new DownAction(beans, table);
		copyAction = new CopyAction(beans, table);
		
		goUpAction = new GoUpAction(table);
		goDownAction = new GoDownAction(table);
		
//		addBorder
		addButton.setAction(addAction);
		addButton.setText("");
		addButton.setOpaque(false);
		
//		editBorder
		editButton.setAction(editAction);
		editButton.setText("");
		editButton.setOpaque(false);
		
//		deleteBorder
		deleteButton.setAction(deleteAction);
		deleteButton.setText("");
		deleteButton.setOpaque(false);
		
//		moveUpBorder
		moveUpButton.setAction(upAction);
		moveUpButton.setText("");
		moveUpButton.setOpaque(false);
		
//		moveDownBorder
		moveDownButton.setAction(downAction);
		moveDownButton.setText("");
		moveDownButton.setOpaque(false);
		
//		copyBorder
		copyButton.setAction(copyAction);
		copyButton.setText("");
		copyButton.setOpaque(false);
		
		if(changeListener != null) {
			setChangeListener(changeListener);
		}
		
		addActionsToComponent(table);
	}

	private void showDialog(int row) {
		try {
			if(okActionListener != null) {
				okActionListener.openDialog();
			}
			
			ArrayList<T> copy = new ArrayList<T>();
			for (T b : beans) {
				copy.add(SemanticaUtil.clone(b));
			}
			
			if(row < 0) {
				row = copy.size();
				copy.add(cls.newInstance());
			}
			
			Constructor c = okActionListener.getDialogClass().getConstructor(BorderMakerBean.class, List.class, int.class, Frame.class);
			
			JDialog p = (JDialog) c.newInstance(bean, copy, row, BorderMaker.mainFrame);
			p.setVisible(true);
			PropertiesDialog pd = (PropertiesDialog) p;
			if(pd.isOkButtonClicked()) {
				okActionListener.actionPerformed(pd.getBeans());
			} else {
				okActionListener.cancel();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void initializeComponents() {
		tableScroll = new JScrollPane();
		tableBar = new JToolBar();
		deleteButton = new JButton();
		editButton = new JButton();
		addButton = new JButton();
		moveUpButton = new JButton();
		moveDownButton = new JButton();
		copyButton = new JButton();
		table = new JTable() { 
			public TableCellRenderer getCellRenderer(int row, int column) {
				Object value = getValueAt(row,column);
				if (value != null) {
					return getDefaultRenderer(value.getClass());
				}
				return super.getCellRenderer(row,column);
			}
			
            //Implement table cell tool tips.
            public String getToolTipText(MouseEvent e) {
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);
                int realColumnIndex = convertColumnIndexToModel(colIndex);

                String retValue = null;
                TableModel tm  = getModel();
                if(tm instanceof TooltipTableModel) {
                	retValue = ((TooltipTableModel) tm).getTooltipText(rowIndex, realColumnIndex);
                }
                return retValue;
            }

            @Override
            protected TableColumnModel createDefaultColumnModel() {
            	return super.createDefaultColumnModel();
            }
            
            //Implement table header tool tips. 
            protected JTableHeader createDefaultTableHeader() {
            	return new JTableHeader(columnModel) {
                    public String getToolTipText(MouseEvent e) {
                        String tip = null;
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        String retValue = null;
                        if(index != -1) {
                        	int realIndex = columnModel.getColumn(index).getModelIndex();
                        	TableModel tm  = getModel();
                        	if(tm instanceof TooltipTableModel) {
                        		retValue = ((TooltipTableModel) tm).getTooltipText(-1, realIndex);
                        	}
                        }
                        return retValue;
                    }
                };
            }
		};
		

//		borderBar
		tableBar.setOrientation(JToolBar.VERTICAL);
		tableBar.setName(BorderMaker.getMessage("borders"));
		tableBar.setFloatable(false);
		tableBar.setRollover(true);
		tableBar.setBorderPainted(false);
		
		tableBar.add(addButton);
		
		if(this.okActionListener != null) {
			tableBar.add(editButton);
		}
		
		tableBar.add(deleteButton);
		tableBar.add(moveUpButton);
		tableBar.add(moveDownButton);
		tableBar.add(copyButton);
		tableBar.setOpaque(false);
		tableBar.setBorder(new EmptyBorder(0, 7, 5, 0));
		
//		bordersScroll
		tableScroll.setViewportView(table);
		tableScroll.setPreferredSize(new Dimension(300,150));
		
//		borders
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setDefaultRenderer(Color.class, new ColorRenderer());
		table.setDefaultRenderer(ColoredText.class, new ColoredTextRenderer());
		table.setRowHeight(table.getRowHeight() + 4);
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2) {
					editButton.doClick();
				}
			}
		});
		
		
		this.setBorder(new EmptyBorder(10,10,10,10));
		this.setLayout(new BorderLayout());
		this.add(tableScroll, BorderLayout.CENTER);
		this.add(tableBar, BorderLayout.EAST);
		this.setOpaque(false);
	}

	public void setChangeListener(ChangeListener changeListener) {
		this.changeListener = changeListener;
		if(addAction != null) {
			addAction.setChangeListener(changeListener);
			deleteAction.setChangeListener(changeListener);
			upAction.setChangeListener(changeListener);
			downAction.setChangeListener(changeListener);
			copyAction.setChangeListener(changeListener);
		}
	}

	public void addUpDownActionsToComponent(JComponent c, int condition) {
		addActionToComponent(c, goUpAction, condition);
		addActionToComponent(c, goDownAction, condition);
	}
	
	public void addActionsToComponent(JComponent c) {
		addActionsToComponent(c, JComponent.WHEN_FOCUSED);
	}
	
	public void addActionsToComponent(JComponent c, int condition) {
		addActionToComponent(c, addAction, condition);
		addActionToComponent(c, deleteAction, condition);
		if(okActionListener != null) {
			addActionToComponent(c, editAction, condition);
		}
		addActionToComponent(c, upAction, condition);
		addActionToComponent(c, downAction, condition);
		addActionToComponent(c, copyAction, condition);
	}
		
	private void addActionToComponent(JComponent c, TAction action, int condition) {
		if(c != null && action != null) {
			c.getActionMap().put(action.getClass().getName(), action);
			c.getInputMap(condition).put(action.getAccelerator(), action.getClass().getName());
		}
	}
	
	public void addListSelectionListener(ListSelectionListener l) {
		table.getSelectionModel().addListSelectionListener(l);
	}

	public void refresh() {
		table.repaint();
	}

	public BorderMakerBean getBean() {
		return bean;
	}

	public void setBean(BorderMakerBean bean) {
		this.bean = bean;
	}
}

