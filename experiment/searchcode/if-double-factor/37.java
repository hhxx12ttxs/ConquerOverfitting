package vivace.view;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.swing.JPanel;

import vivace.helper.GridHelper;
import vivace.model.Action;
import vivace.model.App;
import vivace.model.NoteEvent;
import vivace.model.UI;

public class HistogramBar extends JPanel implements Observer {
	
	/**
	 * Collection over the visible event bars and their corresponding note.
	 * Each event bar adds itself to the collection when it appears.
	 */
	public static HashMap<MidiEvent,HistogramBar> visible = new HashMap<MidiEvent,HistogramBar>();
	
	/**
	 * Collection all event bars and their corresponding note.
	 * Each event bar adds itself to the collection on creation.
	 */
	public static HashMap<MidiEvent,HistogramBar> all = new HashMap<MidiEvent,HistogramBar>();

	/** The maximum height of the bar's container **/
	public static int MAX_HEIGHT;
	
	/** The left margin **/
	public static int LEFT_MARGIN;
	
	/** The top margin **/
	private static int TOP_MARGIN = 1;
	
	private boolean drawEnabled;
	private boolean selected;
	private Color color;
	private int trackIndex;
	private MidiEvent event;

	/**
	 * Returns whether the bar is "drawable" or not
	 * @return
	 */
	public boolean isDrawEnabled(){
		return drawEnabled;
	}
	
	/**
	 * Sets whether the bar is "drawable" or not
	 * @param value
	 */
	public void setDrawEnabled( boolean value ){
		drawEnabled = value;
	}
	
	/**
	 * Returns the event
	 * @return
	 */
	public MidiEvent getEvent(){
		return event;
	}
	
	/**
	 * Returns a dummy NoteEvent which can be used to compare the event with
	 * a NoteEvent.
	 * @return
	 */
	public NoteEvent getNote(){
		return new NoteEvent( trackIndex, event, null);
	}
	
	/**
	 * Returns the track index
	 * @return
	 */
	public int getTrackIndex(){
		return trackIndex;
	}
	
	private int valueToYPosition( int value ){
		double factor = (double) value/128;
		if( factor > 1.0 ) factor = 1.0;
		return (int) Math.round((MAX_HEIGHT-TOP_MARGIN) * factor);
	}
	
	private int yPositionToValue( int yPosition ){
		double factor = (double) (MAX_HEIGHT-TOP_MARGIN)/128; // How many pixels for each "step"?
		int topY = MAX_HEIGHT - yPosition; // The "real" y
		int returnValue = (int) Math.ceil( ((double) topY / factor) );
		if( returnValue < 1 ) returnValue = 1;
		if( returnValue > 127 ) returnValue = 127;
		return returnValue;
	}
	
	/**
	 * Sets the value of the event to match the yposition
	 * @param yPosition
	 */
	public void setEventValueFromYPosition( int yPosition, boolean notify ){
		App.Project.setNoteVelocity(getNote(),yPositionToValue( yPosition ), notify );
		updateBounds();
	}
	
	/**
	 * Constructor
	 * @param trackIndex
	 * @param event
	 */
	public HistogramBar( int trackIndex, MidiEvent event ){
		App.addProjectObserver(this, App.Source.UI);
		this.drawEnabled = false;
		this.selected = false;
		this.trackIndex = trackIndex;
		this.color = App.UI.getTrackColor(trackIndex);
		this.event = event;
		this.setOpaque(false);
		//this.setBackground(color);
		updateBounds();
	}
	
	/** Updates the bounds for the histogram bar **/
	public void updateBounds(){

		// Get the property value
		int value = 0;
		try{
			value = ((ShortMessage) event.getMessage()).getData2();
		} catch (Exception e ){}
		
		// Set the color
		float[] hsb = new float[3];
		Color tmpColor = App.UI.getTrackColor(trackIndex);
		Color.RGBtoHSB(tmpColor.getRed(), tmpColor.getGreen(), tmpColor.getBlue(), hsb);

		// Set saturation and brightness depending on the velocity
		float ratio = (float) value/127;
		hsb[1] = (float) (0.50 + ratio*0.50);;
		//hsb[2] = (float) (1 - ratio*0.40);
		hsb[2] = (float) (0.6 + ratio*0.40);
		this.color = Color.getHSBColor(hsb[0],hsb[1],hsb[2]); 
		
		// Calculate the bounds of the histogram bar
		int x = GridHelper.tickToXPosition(event.getTick()) + LEFT_MARGIN;
		int width = 6; // The width of the bars doesn't depend on the note lengths
		int y = 0;
		barHeight = valueToYPosition(value); // The height of the bars depends on the value and is relative to the height of the panel
		setToolTipText( "Velocity: " + value );
		setBounds(x,y,width,MAX_HEIGHT);
	}
	
	/**
	 * Returns whether the bar is selected or not.
	 * @return
	 */
	public boolean isSelected(){
		return selected;
	}
	
	/**
	 * Sets whether the bar should be selected or not.
	 * @param value
	 */
	public void setSelected( boolean value ){
		selected = value;
		repaint();
	}
	
	int cachedHeight = -1;
	int barHeight = -1;
	boolean animStarted = false;

	@Override
	public void paintComponent( Graphics g ){
		super.paintComponent(g);
		
		HistogramBar.visible.put(getEvent(), this);
		if( cachedHeight != MAX_HEIGHT ){
			cachedHeight = MAX_HEIGHT;
			updateBounds();
		}
		
		// If the sequencer is running, mark the note when it's being played
		// The null check is made to prevent the empty "paint" event bar to play :)
		int realHeight = barHeight;
		if( App.UI.getDiscoMode() && App.Project.getSequencer().isRunning() && event != null ){
			realHeight = 2;
			long tick = App.Project.getSequencer().getTickPosition();
			int duration = App.Project.getPPQ()*2;
			boolean isPlayed = event.getTick() <= (tick-App.Project.getLatencyInTicks()) && (event.getTick()+duration) >= (tick-App.Project.getLatencyInTicks());
			if( isPlayed ){
				animStarted = true;
				long distance = tick - event.getTick();
				realHeight = (int) Math.round(barHeight - ((double) distance/(duration/2))*barHeight/2);
			}
		}
		if( animStarted == true ){
			repaint(); 
			animStarted = false;
		}
		
		// Draw a rectangle, which height represents the value
		g.setColor( (selected ? Color.BLACK : color) );
		g.fillRect(0, getBounds().height-realHeight, getBounds().width, realHeight);
	}

	@Override
	public void update(Observable o,Object arg) { 

		// First of all, check if we really need to bother about the update 
		if( App.UI.getPerspective() != UI.Perspective.PIANOROLL ){
			return;
		}
		
		/* TODO: Denna update behövs pga av att histogram-baren m?ste uppdatera
		 * sina bounds d? zoomlevel ändras. Dock 
		 */ 
		
		// Check which type of action that was performed
		Action action = (Action) arg;
		
		// Then perform the desired updates depending on the action
		switch( action ){

		case ZOOMLEVEL_CHANGED:
			updateBounds(); // TODO: Ev. ha metod i gridhelper (isVisible) o endast anropa denna d?
			
		}
		
	}
}

