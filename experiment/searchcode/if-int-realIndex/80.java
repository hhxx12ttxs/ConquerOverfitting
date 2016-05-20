package de.taliis.plugins.dbc.mixeditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.InvalidClassException;
import java.util.HashMap;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import de.taliis.editor.fileMananger;
import de.taliis.editor.openedFile;
import de.taliis.editor.plugin.Plugin;
import de.taliis.editor.plugin.PluginRequest;

import starlight.taliis.core.chunks.ChunkNotFoundException;
import starlight.taliis.core.files.dbc;

import alien.CorrectStrangeBehaviourListener;

public class MixEditor 
		extends JPanel 
		implements MouseListener, ActionListener, KeyListener 
	{
	
	// data
	MixDataSet data;
	protected String[] desc;

	Plugin mpqLoader;
	fileMananger fm;
	
	// visuals
	JTable table;
	JPopupMenu popup;
	JMenuItem miCopy, miDelete;
	
	/**
	 * The direct uplink to the plugin is needed ?
	 * @param src
	 * @throws ChunkNotFoundException 
	 * @throws InvalidClassException 
	 */
	public MixEditor(MixDataSet data, fileMananger fm, Plugin mpqLoader) throws InvalidClassException, ChunkNotFoundException {
		this.data = data;
		this.mpqLoader = mpqLoader;
		this.fm = fm;
		
		// stuff todo
		detectVersion();
		preloadDesc();
		getDataTypes();
		prepareMixValues();
		
		initPopup();
		
		// the table and its layout
		table = new JTable(new MixTableModel(data))
		// overwritten methods:
		 {
		    //Implement table header tool tips.
			protected JTableHeader createDefaultTableHeader() {
				return new JTableHeader(columnModel) {
					public String getToolTipText(MouseEvent e) {
						String tip = null;
						java.awt.Point p = e.getPoint();
						int index = columnModel.getColumnIndexAtX(p.x);
						int realIndex = 
							columnModel.getColumn(index).getModelIndex();
						return desc[realIndex];
					}
				};
			}
		};
		table.setPreferredScrollableViewportSize(new Dimension(100, 70));
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		table.setAutoCreateRowSorter(true);
		table.addMouseListener(this);
		table.addKeyListener(this);
		
		JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.addComponentListener(new
                CorrectStrangeBehaviourListener(table, scrollPane)); 

        //Add the scroll pane to this panel.
    	this.setLayout(new BorderLayout());
        add(scrollPane);
        updateVisuals();
	}
	
	/**
	 * Preload our descriptions for the tooltip
	 */
	private void preloadDesc() {
		if(data.config==null) return;
		int dbcl = data.dbcFile.getNFields();
		desc = new String[dbcl];
		
		for(int i=0; i<dbcl; i++) {
			desc[i] = data.config.getProperty(
					data.configBaseString + i + "_desc" 
			   );
		}
	}
	
	/**
	 * check what version of our config file we need
	 */
	private void detectVersion() {
		if(data.config==null) return;
		
		int dbcl = data.dbcFile.getNFields();
		desc = new String[dbcl];
		
		String tmp;
		tmp = data.config.getProperty("dbc_global_rev_count");
		// broken file?
		if(tmp==null) return;
		
		int vers = Integer.parseInt(tmp);
		
		// scan for compatible col count
		for(int i=1; i<=vers;i++) {
			int c;
			tmp = data.config.getProperty("dbc_entry_"+i+"_fields");
			c = Integer.parseInt(tmp);
			
			if(c==dbcl) {
				data.configRevIndex = i;
				data.configBaseString = "dbc_entry_"+i+"_field_";		
			}
		}
	}
	
	/**
	 * Whenever we need to update our column settings ..
	 */
	public void updateVisuals() {
		// number of bits for each record
		int bits = data.dbcFile.getRecordSize()
					/ data.dbcFile.getNFields() * 8;

		for(int c=0; c<data.dbcFile.getNFields(); c++) {
    		TableColumn column = table.getColumnModel().getColumn(c);
    		
    		// cross entry?
    		if(data.crossed[c]==true) {
    			column.setPreferredWidth(150);
    			column.setMinWidth(50);
    			
    			// mask
    			if(data.types[c]==data.COL_TYPE_MASK) {
    				//column.setCellEditor(new FlagsCellEditor(bits));
    				column.setCellEditor(new MixCellEditor());
    				column.setCellRenderer(
	    					new ColorTableCellRenderer(ColorTableCellRenderer.COLOR_YELLOW)
	    				);
    			}
    			// else -> index
    			else {
	    			column.setCellEditor(new MixCellEditor());
	    			column.setCellRenderer(
	    					new ColorTableCellRenderer(ColorTableCellRenderer.COLOR_RED)
	    				);
    			}
    		}
    		// string?
    		else if(data.types[c]==dbc.COL_TYPE_STRING) {
    			column.setMinWidth(50);
    			column.setPreferredWidth(150);
    			column.setCellEditor(new ZTSCellEditor());
    			column.setCellRenderer(
    					new ColorTableCellRenderer(ColorTableCellRenderer.COLOR_BLUE)
    				);
    		}
    		// float?
    		else if(data.types[c]==dbc.COL_TYPE_FLOAT) {
    			column.setMinWidth(30);
    			column.setPreferredWidth(80);
    			column.setCellRenderer(
    					new ColorTableCellRenderer(ColorTableCellRenderer.COLOR_GREEN)
    				);
    			
    		}
    		//TODO: flags
    		else {
    			column.setMinWidth(30);
    			column.setPreferredWidth(60);
    		}    		
    	}
	}
	
	/**
	 * Whats the datatype of our coloumns?
	 * Here we find out (first ask config, dann dbc auto detection)
	 */
	private void getDataTypes() {
		// init data type values
		data.initDataTypeStuff( );
		int types[] = data.types;	// lazy shortwriting ..
		
		// we have no fitting config?
		if(data.configRevIndex==-1) {
			for(int i=0; i<types.length; i++)
				types[i] = data.dbcFile.getColType(i);
		}
		else {
			for(int i=0; i<types.length; i++) {
				data.crossed[i]=false;
				
				// get data
				String tmp = data.config.getProperty(
								data.configBaseString + i + "_datatype"
							);
				String cross =  data.config.getProperty(
						data.configBaseString + i + "_cross_file"
				);
				
				// ask editor?
				if(tmp==null) {
					types[i] = data.dbcFile.getColType(i);
					continue;
				}			
				
				// set by our self
				int val = -1;
				try {
					val= Integer.parseInt(tmp);
				} catch(Exception e) {}
					
				// value type
				if(tmp.endsWith("num")||val==dbc.COL_TYPE_NUMERIC) {
					types[i] = dbc.COL_TYPE_NUMERIC;
				}
				else if(tmp.endsWith("float")||val==dbc.COL_TYPE_FLOAT) {
					types[i] = dbc.COL_TYPE_FLOAT;
				}
				else if(tmp.endsWith("string")||val==dbc.COL_TYPE_STRING) {
					types[i] = dbc.COL_TYPE_STRING;
				}
				else if(tmp.endsWith("bool")||val==dbc.COL_TYPE_BOOLEAN){
					types[i]=dbc.COL_TYPE_BOOLEAN;
				}
				//selfmade type -> flags and mask
				else if(tmp.endsWith("flags") || tmp.endsWith("mask")) {
					types[i] = data.COL_TYPE_FLAGS;
				}
				else types[i] = data.dbcFile.getColType(i);
				
				
				// cross field?
				if(cross!=null) {
					data.crossed[i] = true;
					data.mixArrIndexies[i] = data.crossers;
					
					// mask ?
					if(tmp.endsWith("mask")) {
						types[i] = data.COL_TYPE_MASK;
					}
					
					data.crossers++;
				}
			}		
		}
	}
	
	/**
	 * Mix values also means we need to display a 
	 * foreign string instead of a value.
	 * This string is an index and here we start to
	 * translate.
	 * @throws ChunkNotFoundException 
	 * @throws InvalidClassException 
	 */
	private void prepareMixValues() throws InvalidClassException, ChunkNotFoundException {
		// data storage
		data.initHashArray();

		// scan thought
		for(int i=0; i<data.crossed.length; i++) {
			// normal data?
			if(data.crossed[i]==false) continue;
			
		// get config infos
		//-----------------------------------------------------
			String mix = data.config.getProperty(
					data.configBaseString + i + "_cross_file"
			);
			String valField  = data.config.getProperty(
					data.configBaseString + i + "_cross_val_field"
			); 
			String dispField = data.config.getProperty(
					data.configBaseString + i + "_cross_displ_field"
			);
			if(mix==null || valField==null | dispField==null) continue;
	
			
			int vCol, dCol;
			try {
				vCol = Integer.parseInt(valField);
				dCol = Integer.parseInt(dispField);
			} catch(Exception e) {
				continue;
			}
			
			
		// catch the dbc file we need
		//-----------------------------------------------------
			dbc src;
			if(mix.endsWith("same")) {
				src = data.dbcFile;
			}
			else {
				//request (load) DBC file
				openedFile of = null;
				
				// ask the file loader
				for(openedFile af : fm.getFileList()) {
					if(af.f.getName().endsWith(mix)) {
						of = af;
						break;
					}
				}
				// ask the mpq loader
				if(of==null) {
					if(mpqLoader==null) continue;
					if(!(mpqLoader instanceof PluginRequest)) continue;
					PluginRequest mpql = (PluginRequest)mpqLoader;
					
					of = mpql.requestRessource("DBFilesClient\\" + mix);
				}
				
				if(of==null) continue;
				src = (dbc)of.obj;
			}
			
		// prepare create crosslist vector
		//-----------------------------------------------------
			int l = data.mixArrIndexies[i];
			data.CrossLists[l] = new HashMap<Integer, String>();
			
			// lazy shortcut ..
			HashMap<Integer, String> tmpVec = data.CrossLists[l];
			
			// copy data
			for(int j=0; j<src.getNRecords(); j++) {
				// index in our tmpVector
				Object t = src.getData(vCol, j);
		    	int vIndex = ((Number)t).intValue();
		    	
		    	// index in the string table
		    	t = src.getData(dCol, j);
		    	int strIndex = ((Number)t).intValue();
		    	
		    	// string we want to store
		    	tmpVec.put(vIndex, src.getStringByOffset(strIndex).toString());
			}
		}
	}
	
	/**
	 * We use popup windows for basic operations like
	 * - copy
	 * - delete
	 */
	private void initPopup() {
		popup = new JPopupMenu();
		miCopy = new JMenuItem("Clone selected Row");
		miCopy.addActionListener(this);
		miDelete = new JMenuItem("Delete selected Row(s)");
		miDelete.addActionListener(this);
		popup.add(miCopy);
		popup.add(miDelete);
	}

	
	/**
	 * Event stuff
	 */
	public void mouseClicked(MouseEvent e) { }
	public void mouseEntered(MouseEvent e) { }
	public void mouseExited(MouseEvent e) {	}
	public void mousePressed(MouseEvent e) { }
	
	public void mouseReleased(MouseEvent e) {
		// right click
		if(e.getButton()==MouseEvent.BUTTON3) {
			// menues need selected rows in order to perform
			if(table.getSelectedRowCount()>0)
				popup.show(e.getComponent(), e.getX(), e.getY());
		}
	}
	public void keyPressed(KeyEvent e) { }
	public void keyTyped(KeyEvent e) { }
	
	public void actionPerformed(ActionEvent e) {
		// delete rows
		if(e.getSource()==miDelete) {
			if(table.getSelectedRowCount()<=0) return;
			
			// delete all selected rows
			data.dbcFile.deleteRows(table.getSelectedRow(), table.getSelectedRowCount());

			table.removeRowSelectionInterval(0, table.getRowCount()-1);
			table.updateUI();
		}
		
		// copy row
		if(e.getSource()==miCopy) {
			int row = table.getSelectedRow(); 
			if(row==-1) return;
			
			data.dbcFile.copyRow(row);
			table.updateUI();
		}
	}


	public void keyReleased(KeyEvent e) {
		if( e.getKeyCode()==KeyEvent.VK_DELETE) {
			if(table.getSelectedRow()==-1) return;
			if(table.getSelectedRowCount()<=0) return;
			
			int sr = table.getSelectedRow();
			int src = table.getSelectedRowCount();
			
			// take care about editor and selection
			int tc = table.getRowCount();
			table.removeEditor();
			table.removeRowSelectionInterval(0, tc-1);
			
			// delete all selected rows
			data.dbcFile.deleteRows(sr, src);
			table.updateUI();
			
			// new selection interval
			tc = table.getRowCount();
			if(tc==0) return;
			if(sr<tc)
				table.addRowSelectionInterval(sr, sr);
			else
				table.addRowSelectionInterval(tc-1,tc-1);
		}
	}
}
