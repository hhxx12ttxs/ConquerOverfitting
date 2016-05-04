package vivace.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import javax.sound.midi.Instrument;
import javax.sound.midi.Track;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import lib.Komponenter.CTable;
import vivace.exception.MidiMessageNotFoundException;
import vivace.exception.NoChannelAssignedException;
import vivace.helper.GUIHelper;
import vivace.model.Action;
import vivace.model.App;
import vknob.DefaultVKnobSnapper;
import vknob.VKnob;
import vknob.VKnobModel;

public class TrackList extends JScrollPane implements Observer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7874380464656367534L;
	private Controller controller;
	private TracksTable tracksTable;
	private String[] columnNames = 
	{"TRACK_NUMBER", "NAME", "CHANNEL", "VOICE", 
			 "SOLO", "MUTE", "VOLUME", "PAN", "COLOR"
			};
	
	public TrackList() {

		// Tell the model to notify about model changes 
		// for the currently active project
		App.addProjectObserver(this,App.Source.MODEL);

		// Create a reference to the controller
		controller = new Controller();
		
		// Initialize the GUI
		createTrackListTable();
		setVisible(true);
		
	}
	
	private void createTrackListTable(){
		
		Track[] tracks = App.Project.getTracks();
		
		Object[][] data = new Object[tracks.length][columnNames.length];
	
		for (int i = 0; i < tracks.length; i++) {
			
			data[i] = new Object[columnNames.length];
			
			/* add track no */
			data[i][0] = i + 1;
			
			/* get track name (meta message) */
			data[i][1] = App.Project.getTrackName(i);
			
			/* get track channel */
			try {
				data[i][2] = App.Project.getTrackChannel(i) + 1;
			} catch (NoChannelAssignedException e) {
				data[i][2] = null;
			}
			
			/* get track instrument */
			try {
				data[i][3] = App.Project.getTrackInstrument(i);
			} catch (MidiMessageNotFoundException e) {
				data[i][3] = null;
			}
			
			/* get track solo status */
			data[i][4] = App.Project.getSequencer().getTrackSolo(i);
			
			/* get track mute status */
			data[i][5] = App.Project.getSequencer().getTrackMute(i);
			
			/* get track volume */
			try {
				data[i][6] = App.Project.getTrackVolume(i);
			} catch (NoChannelAssignedException e) {
				data[i][6] = null;
			}
			/* get track pan */
			try {
				data[i][7] = App.Project.getTrackBalance(i);
			} catch (NoChannelAssignedException e) {
				data[i][7] = null;
			}
			
			data[i][8] = App.UI.getTrackColor(i);
			
		}
	
		TracksTableModel dataModel = new TracksTableModel(columnNames, data);
		dataModel.addTableModelListener(controller);
		
		tracksTable = new TracksTable(dataModel, columnNames);

		tracksTable.setColumnSelectionAllowed(false);
		tracksTable.setRowSelectionAllowed(true);
		tracksTable.getSelectionModel().addListSelectionListener(controller);
		
		// Set the height of the header and the rows
		int headerWidth = tracksTable.getTableHeader().getWidth();
		int headerHeight = GUIHelper.HEADER_HEIGHT + 1; 
		tracksTable.getTableHeader().setPreferredSize(new Dimension(headerWidth, headerHeight));
		
		tracksTable.setRowHeight(GUIHelper.TRACK_HEIGHT);
		tracksTable.setFillsViewportHeight(true);
		
		// set 1st column (#) to a fixed width and prevent resizing and editing
		TableColumn firstColumn = tracksTable.getColumnModel().getColumn(0);
		firstColumn.setPreferredWidth(35);
		firstColumn.setResizable(false);
		
		// set 4th column (voice) to use combo boxes as editors
		// also, set it to a determined width
		JComboBox voiceComboBox = new JComboBox(App.Project.getInstruments());
		voiceComboBox.setRenderer(new VoiceComboBoxCellRenderer());
		//voiceComboBox.setModel(new VoiceComboBoxModel());
		
		TableColumn voiceColumn = tracksTable.getColumnModel().getColumn(3);
		voiceColumn.setCellEditor(new DefaultCellEditor(voiceComboBox));
		voiceColumn.setCellRenderer(new VoiceCellRenderer());
		voiceColumn.setPreferredWidth(250);
		
		// Set the volume- and pan columns to use knobs as editors
		TableColumn volumeColumn = tracksTable.getColumnModel().getColumn(6);
		volumeColumn.setCellEditor(new KnobEditor());
		TableColumn balanceColumn = tracksTable.getColumnModel().getColumn(7);
		balanceColumn.setCellEditor(new KnobEditor());
		
		TableColumn colorColumn = tracksTable.getColumnModel().getColumn(8);
		colorColumn.setPreferredWidth(10);
		colorColumn.setCellEditor(new ColorEditor());
		colorColumn.setCellRenderer( new ColorCellRenderer(true));
		// set key listener
		tracksTable.addKeyListener(new TrackListKeyListener());
		
		// get ListSelectionModel for handling 
				
		// add tracks table to TrackList panel
		this.add(tracksTable);

		setViewportView(tracksTable);
		setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		
	}
	
	
	private class TracksTableModel extends DefaultTableModel {
		
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -2057010533088454708L;

		public TracksTableModel(String[] columnNames, Object[][] data) {
			super(data, columnNames);
		}
		
		public int getColumnCount() {
			return columnIdentifiers.size();
		}
		
		public int getRowCount() {
			return dataVector.size();
		}
		
		public String getColumnName(int col) {
			return (String) columnIdentifiers.get(col);
		}
		
		public Object getValueAt(int row, int col) {
			Vector rowVector = (Vector) dataVector.get(row);
			return rowVector.get(col);
		}
		
		public Class<?> getColumnClass(int col) {
			int rowCount = getRowCount();
			
			for(int row = 0; row < rowCount; row++) {
				
				if(getValueAt(row, col) != null) {
					return getValueAt(row, col).getClass();
				}
			
			}
			
			// default to string if all rows in the column were empty
			// hence no class could be identified
			return ( new String() ).getClass();
	    }

		
		public boolean isCellEditable(int row, int col) {
			// column 0 uneditable
			// also, cell on a row is uneditable if no
			// channel value (col 2) has been set
			// TODO: use a channelAssigned(track):boolean method instead?
			return col > 0 && (col == 2 || getValueAt(row, 2) != null);
		}
		
		public void setValueAt(Object value, int row, int col) {
			Vector rowVector = (Vector) dataVector.get(row);
			Object oldValue = rowVector.get(col);
	
			/* only fire table change if value actually changed */
			if (oldValue == null && value == null) 
				return;
			
			if ((oldValue == null && value != null) ||
					(oldValue != null && value == null) ||
					!oldValue.equals(value)) {
				rowVector.set(col, value);
				fireTableCellUpdated(row, col);
			}

		}
		
	
		
	}
	
	public void update(Observable o, Object arg) {
	
		// Check which type of action that was performed
		Action action = (Action) arg; 
		
		// Then perform the desired updates depending on the action
		if (action == Action.TRACK_ADDED ||
				action == Action.TRACK_REMOVED) {
			createTrackListTable();		
		}

	}
	
	private class Controller implements TableModelListener, ListSelectionListener {
		
		public void tableChanged(TableModelEvent e) {
			
			int row = e.getFirstRow();
	        int col = e.getColumn();
	       
	        
	       TracksTableModel dataModel = (TracksTableModel) e.getSource();
	        switch(col) {
	        	case 1:		// track name
	        		
	        		// get new track name
	        		String newName = (String) dataModel.getValueAt(row, col);
	        		
	        		// set new track name
	        		App.Project.setTrackName(row, newName);
	
	        		break;
	        		
	        	case 2:		// channel
	        		int channel;
	        		
	        		// get new channel
	        		if (dataModel.getValueAt(row, col) instanceof Integer) {
	        			
	        			channel = (Integer) dataModel.getValueAt(row, col);
	        		} else {
	        			channel = Integer.parseInt( (String) dataModel.getValueAt(row, col));
	        		}
	        		
	        		// set new channel
	        		
	        		try {
	        			App.Project.setTrackChannel(row, channel - 1);
	        		} catch (IllegalArgumentException ex) {
	        			GUIHelper.displayError(ex);
	        		}
	        		
	        		break;
	        	
	        	case 3:		// voice
	        		
	        		// get new instrument
	        		Instrument instr = (Instrument) dataModel.getValueAt(row, col);
	        		
	        		if (instr != null) {
		        		try {
		        			App.Project.setTrackInstrument(row, instr);
		        		} catch (NoChannelAssignedException ex) {
		        			GUIHelper.displayError(ex);
		        		}
	        		}
	        			
	        		break;
	        		
	        	case 4:		// solo
	        		Boolean solo = (Boolean) dataModel.getValueAt(row, col);
	        		App.Project.getSequencer().setTrackSolo(row, solo);
	        		break;
	        		
	        	case 5:		// mute
	        		Boolean mute = (Boolean) dataModel.getValueAt(row, col);
	        		App.Project.getSequencer().setTrackMute(row, mute);
	        		break;
	        		
	        	case 6:		// volume
	        		Integer vol = (Integer) dataModel.getValueAt(row, col);
	        		try {
	        			App.Project.setTrackVolume(row, vol);
	        		} catch (NoChannelAssignedException ex) {
	        			GUIHelper.displayError(ex);
	        		}
	        		break;
	        		
	        	case 7:		// balance
	        		Integer balance = (Integer) dataModel.getValueAt(row, col);
	        		
	        		try {
	        			App.Project.setTrackBalance(row, balance);
	        		} catch (NoChannelAssignedException ex) {
	        			GUIHelper.displayError(ex);
	        		}
	        		break;
	        		
	        	case 8: // Color
	        		Color color = (Color) dataModel.getValueAt(row, col);
	        		App.UI.setTrackColor(row, color);
	        		break;
	        		
	        	default:
	        
	        }
	        
		}
		
		public void valueChanged(ListSelectionEvent e) {
			
			
			if(e.getValueIsAdjusting()) {
				
				App.UI.addToTrackSelection(e.getLastIndex(),false);
				
				/* If we run clearSelection, getLastIndex always returns which track we selected. */
				ListSelectionModel l = (ListSelectionModel) e.getSource();
				l.clearSelection();
			} 
			
			
		}
		
		
	 
	}
	
	private class VoiceComboBoxCellRenderer extends DefaultListCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4542903219103479637L;

		public Component getListCellRendererComponent(JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
		
			JLabel jl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			
			Instrument instr = (Instrument) value;
			
			if (instr != null) {
				jl.setText(instr.getName());
			}
			
			return jl;
		}
	}
	
	private class VoiceCellRenderer extends DefaultTableCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 7841068090607101093L;

		public Component getTableCellRendererComponent(JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
			
			JLabel jl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			
			Instrument instr = (Instrument) value;
			
			if (instr != null) {
				jl.setText(instr.getName());
			}
			
			
			return jl;
		}
	}
	
	
	public class ColorCellRenderer extends JLabel implements TableCellRenderer {

		Border unselectedBorder = null;
		Border selectedBorder = null;
		boolean isBordered = true;

		public ColorCellRenderer(boolean isBordered) {
		
			this.isBordered = isBordered;
			setOpaque(true); //MUST do this for background to show up.
		}

		public Component getTableCellRendererComponent(
		     JTable table, Object color,
		     boolean isSelected, boolean hasFocus,
		     int row, int column) {
		Color newColor = (Color)color;
		setBackground(newColor);
			if (isBordered) {
				if (isSelected) {
					if (selectedBorder == null) {
						selectedBorder = BorderFactory.createMatteBorder(2,2,2,2,
			                       table.getSelectionBackground());
					}
					setBorder(selectedBorder);
				} else {
					if (unselectedBorder == null) {
						unselectedBorder = BorderFactory.createMatteBorder(2,2,2,2,
			                       table.getBackground());
					}
					setBorder(unselectedBorder);
				}
			}
		
			setToolTipText("RGB value: " + newColor.getRed() + ", "
			              + newColor.getGreen() + ", "
			              + newColor.getBlue());
			return this;
		}
	}

	private class SliderEditor extends AbstractCellEditor implements TableCellEditor {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4253318317459046682L;
		private JSlider editor;
		private Popup popup;
		
		private static final int SLIDER_MIN = 0;
		private static final int SLIDER_MAX = 127;
			
		public SliderEditor(int min, int max) {
			editor = new JSlider(min, max);
			editor.setOrientation(SwingConstants.VERTICAL);
			editor.setVisible(true);
			addCellEditorListener(new SliderEditorListener());
			
		}
		
		public SliderEditor() {
			this(SLIDER_MIN, SLIDER_MAX);
		}
		
		public Object getCellEditorValue() {
			return editor.getValue();
		}
		
		public Component getTableCellEditorComponent(JTable table,
                Object value,
                boolean isSelected,
                int row,
                int column) {
			
			PopupFactory pf = PopupFactory.getSharedInstance();
			
			// Determine position (x,y coordinates) of popup
			Point p = table.getLocationOnScreen();
			double x = p.getX();
			double y = p.getY();
			
			
			
			Rectangle rect = table.getCellRect(row, column, true);
			x += rect.getX();	
			y += rect.getY();
			
			// set JSlider value
			if (value != null) {
				editor.setValue((Integer) value);
			} else {
				editor.setValue(127);
			}
			
			// get popup containing editor
			popup = pf.getPopup(table, editor, (int) Math.round(x), (int) Math.round(y));
			
			// show popup
			popup.show();
			
			return new JPanel();
		}
		
		/* Listening for events fired by the cell editor */
		private class SliderEditorListener implements CellEditorListener {
			public void editingCanceled(ChangeEvent e) {
				// hide popup
				popup.hide();
			}
			
			public void editingStopped(ChangeEvent e) {
//				 hide popup
				popup.hide();
			}
		}
	}

	
	private class KnobEditor extends AbstractCellEditor implements TableCellEditor, VKnobModel {
		/**
		 * 
		 */
		
		private VKnob editor;
		private Popup popup;
		
		private int min;
		private int max;
			
		public KnobEditor(int min, int max ) {
			this.min = min;
			this.max = max;
			editor = new VKnob(this);
			editor.setSnapper( new DefaultVKnobSnapper(2));
			editor.setDragType(vknob.VKnob.ROUND);
			editor.setValue(100);
			editor.setPreferredSize(new Dimension(60,60));
	        addCellEditorListener(new KnobEditorListener());
		}
		
		public KnobEditor(){
			this(0,127);
		}
		
	    @Override
		public float getKnobDirectionOfValue(Object value) {
	    	int i = ((Integer) value);
	    	if( i > max ) i = max;
	    	if( i < min ) i = min;
	    	return ((float) i/max);
	   }

		@Override
		public Object getValueAtKnobDirection(float position) {
			return (Integer) Math.round(position*max);
		}
		
		public Object getCellEditorValue() {
			return editor.getValue();
		}
		
		public Component getTableCellEditorComponent(JTable table,
                Object value,
                boolean isSelected,
                int row,
                int column) {
			
			PopupFactory pf = PopupFactory.getSharedInstance();
			
			// Determine position (x,y coordinates) of popup
			Point p = table.getLocationOnScreen();
			double x = p.getX();
			double y = p.getY();
			
			Rectangle rect = table.getCellRect(row, column, true);
			x += rect.getX();	
			y += rect.getY();
			
			// set knob value
			if (value != null) {
				editor.setValue((Integer) value);
			} else {
				editor.setValue(127);
			}
			
			// get popup containing editor
			popup = pf.getPopup(table, editor, (int) Math.round(x), (int) Math.round(y));
			
			// show popup
			popup.show();
			
			// grab focus for the editor
			editor.grabFocus();
			
			return new JPanel();
		}
		
		/* Listening for events fired by the cell editor */
		private class KnobEditorListener implements CellEditorListener {
			public void editingCanceled(ChangeEvent e) {
				// hide popup
				popup.hide();
			}
			
			public void editingStopped(ChangeEvent e) {
//				 hide popup
				popup.hide();
			}
		}
	}
	
	private class ColorEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
	
		Color currentColor;
		JButton button;
		JColorChooser colorChooser;
		JDialog dialog;
		protected static final String EDIT = "edit";
		
		public ColorEditor() {
			button = new JButton();
			button.setActionCommand(EDIT);
			button.addActionListener(this);
			button.setBorderPainted(false);
			
			//Set up the dialog that the button brings up.
			colorChooser = new JColorChooser();
			dialog = JColorChooser.createDialog(button,
			                   "Pick a Color",
			                   true,  //modal
			                   colorChooser,
			                   this,  //OK button handler
			                   null); //no CANCEL button handler
		}
		
		public void actionPerformed(ActionEvent e) {
			if (EDIT.equals(e.getActionCommand())) {
				//The user has clicked the cell, so
				//bring up the dialog.
				button.setBackground(currentColor);
				colorChooser.setColor(currentColor);
				dialog.setVisible(true);
				
				fireEditingStopped(); //Make the renderer reappear.
				
			} else { //User pressed dialog's "OK" button.
				currentColor = colorChooser.getColor();
			}
		}
		
		//Implement the one CellEditor method that AbstractCellEditor doesn't.
		public Object getCellEditorValue() {
			return currentColor;
		}
		
		//Implement the one method defined by TableCellEditor.
		public Component getTableCellEditorComponent(JTable table,
		                            Object value,
		                            boolean isSelected,
		                            int row,
		                            int column) {
			currentColor = (Color)value;
			return button;
		}
	}

	
	private class TracksTable extends CTable {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -2768857011923403618L;

		public TracksTable(TracksTableModel model, String[] headerKeys) {
			super(model, headerKeys);
		}
		
		public void setCText() {
			super.setCText();	// alter column headers
			
			// set 1st column (#) to a fixed width and prevent resizing and editing
			TableColumn firstColumn = getColumnModel().getColumn(0);
			firstColumn.setPreferredWidth(35);
			firstColumn.setResizable(false);
			
			// set 4th column (voice) to use combo boxes as editors
			// also, set it to a determined width
			JComboBox voiceComboBox = new JComboBox(App.Project.getInstruments());
			voiceComboBox.setRenderer(new VoiceComboBoxCellRenderer());
			//voiceComboBox.setModel(new VoiceComboBoxModel());
			
			TableColumn voiceColumn = getColumnModel().getColumn(3);
			voiceColumn.setCellEditor(new DefaultCellEditor(voiceComboBox));
			voiceColumn.setCellRenderer(new VoiceCellRenderer());
			voiceColumn.setPreferredWidth(250);
			
			TableColumn volumeColumn = getColumnModel().getColumn(6);
			volumeColumn.setCellEditor(new KnobEditor());
			TableColumn balanceColumn = getColumnModel().getColumn(7);
			balanceColumn.setCellEditor(new KnobEditor());
			
			TableColumn colorColumn = getColumnModel().getColumn(8);
			colorColumn.setCellEditor(new ColorEditor());
			colorColumn.setCellRenderer( new ColorCellRenderer(true));
			colorColumn.setPreferredWidth(10);
		}
	}
	
	private class TrackListKeyListener extends KeyAdapter{
		
		public void keyReleased (KeyEvent e){
			
			if(e.getKeyCode() == KeyEvent.VK_DELETE){
				App.Project.removeTracks(App.UI.getTrackSelection());
			}
				
			
		}
	}
	
}

	

