package vivace.helper;

import java.awt.*;
import vivace.model.*;
import vivace.view.Keyboard;

import java.util.*;
import java.util.List;

/**
 * Helper for drawing grids in the application
 *
 */
public class GridHelper implements Observer {

	/* The reference to the singleton instance */
	private static GridHelper theInstance = new GridHelper();

	/* Collections that keeps tracks of the positions in the grid. These are needed in order
	 * to simplify the calculation of snap points. */
	private static ArrayList<Integer> mainBarPositions = new ArrayList<Integer>();
	private static ArrayList<Integer> smallBarPositions = new ArrayList<Integer>();
	private static ArrayList<Integer> notePositions = new ArrayList<Integer>(); // Array list with the note positions. The index corresponds to the note number.

	/**
	 * Performs some needed initialization. Always call before you start to use the
	 * grid helper functions.
	 */
	public static void initialize(){
		App.addProjectObserver(theInstance, App.Source.MODEL);
		timeSignatures = null;
		initializeNotePositions();
	}

	/**
	 * Returns the needed dimension for the overview grid
	 */
	public static Dimension getOverviewDimension(){
		return new Dimension(tickToXPosition( Math.max(GUIHelper.MIN_TICKS, App.Project.getSequenceLength() ) ),GUIHelper.TRACK_HEIGHT * App.Project.getTracks().length);
	}

	/**
	 * Returns the needed dimension for the piano roll grid
	 */
	public static Dimension getPianoRollDimension(){
		return new Dimension(tickToXPosition( App.Project.getSequenceLength() ), notePositions.get(notePositions.size()-1) + Keyboard.BIGNOTE_HEIGHT);
		//return new Dimension(tickToXPosition( Math.max(GUIHelper.MIN_TICKS, App.Project.getSequenceLength() ) ), notePositions.get(notePositions.size()-1) + Keyboard.BIGNOTE_HEIGHT);
	}

	/**
	 * Returns the needed dimension for the time line grid
	 */
	public static Dimension getTimelineDimension(){
		return new Dimension(tickToXPosition( Math.max(GUIHelper.MIN_TICKS, App.Project.getSequenceLength() ) ),GUIHelper.HEADER_HEIGHT );
	}

	/** 
	 * Returns the x-position of a tick
	 */
	public static int tickToXPosition( long tick ){
		double m = App.Project.getPPQ() * 4;
		int n = App.UI.getZoomLevel();
		return (int) Math.ceil(tick/m * n);
	}

	/**
	 * Returns a tick from a x-position. 
	 */
	public static int xPositionToTick( int xPos ){
		int m = App.Project.getPPQ() * 4;
		int n = App.UI.getZoomLevel();
		int tick = (int) Math.ceil(xPos*m/n);
		return tick;
	}

	/**
	 * Returns the y-position of a note
	 */
	public static int noteToYPosition( int note ){
		return notePositions.get(127-note);
	}

	/**
	 * Returns a note value from an y-position.
	 */
	public static int yPositionToNote( int yPos ){
		return 127-getClosestIndex(yPos, notePositions);
	}

	/**
	 * Returns the x-position of the closest main bar .
	 */
	public static int getClosestMainBarPosition( int xPosition ){
		return getClosestValue(xPosition, mainBarPositions );
	}

	/**
	 * Returns the x-position of the closest "snappable" position, in other words
	 * the closest vertical grid line.
	 */
	public static int getClosestSnapPosition( int xPosition ){
		return getClosestValue(xPosition, smallBarPositions );
	}

	/**
	 * Returns the y-position of the closest horizontal grid line in the piano roll
	 */
	public static int getClosestNotePosition( int yPosition ){
		return getClosestValue(yPosition, notePositions);
	}

	/* Calculates the note positions. */
	private static void initializeNotePositions(){
		// Special case since the first note is a small one, but should be as large as a big one
		notePositions.clear();
		notePositions.add(0);
		int currentY = Keyboard.BIGNOTE_HEIGHT;
		for(int i = 1; i < 128; i++){
			notePositions.add(currentY);
			switch( i % 12 ){
			case 2: case 3: case 7: case 8:
				currentY += Keyboard.BIGNOTE_HEIGHT;
				break;
			default:
				currentY += Keyboard.SMALLNOTE_HEIGHT;
			break;
			}

		}
	}

	/** Paints the grid for the piano roll */
	public static void paintPianoRollGrid( Graphics g ){

		// Get the dimension of the grid
		Dimension d = getPianoRollDimension();

		// Special case since the first note is a small one, but should be as large as a big one
		int currentY = Keyboard.BIGNOTE_HEIGHT - Keyboard.SMALLNOTE_HEIGHT;

		// Iterate over the notes 
		g.setColor(Color.decode("#d6dde2"));
		for( int i = 1; i < notePositions.size(); i++ ){
			currentY = notePositions.get(i);
			switch( i % 12 ){
			// For the large white keys, draw a line
			case 2: case 3: case 7: case 8: 
				g.drawLine(0, currentY, d.width, currentY);
				break;
				// For the black keys, draw a rectangle
			case 1: case 4: case 6: case 9: case 11:
				g.fillRect(0, currentY, d.width, Keyboard.SMALLNOTE_HEIGHT);
				break;
			}			
		}

		// Paint the vertical lines
		paintBarPositions( g, Math.max(d.height, g.getClipBounds().height), 0 );

	}

	/** Paints the grid for the overview */
	public static void paintOverviewGrid( Graphics g ){

		// Get the dimension of the grid
		Dimension d = getOverviewDimension();
		
		if(d.width < 3000){
			d.width = 3000;
		}

		// Paint a horizontal line for each track.
		int currentY = 0;
		g.setColor(Color.decode("#d6dde2"));

		for (int i = 0; i <= App.Project.getTracks().length; i++) {

			g.drawLine(0,currentY,d.width,currentY);
			currentY += GUIHelper.TRACK_HEIGHT;
		}

		// Paint the vertical lines
		paintBarPositions( g, Math.max(d.height, g.getClipBounds().height), 0 );

	}

	/** Paints the grid for the histogram */
	public static void paintHistogramGrid( Graphics g, int height ){

		// Get the dimension of the grid
		Dimension d = getOverviewDimension();

		// Paint the vertical lines
		paintBarPositions( g, height, 0 );

	}


	private static TimeSignatureHelper[] timeSignatures;

	/* Returns a cached version of the time signature list if possible */
	private static TimeSignatureHelper[] getTimeSignatures(){
		if( timeSignatures == null ) {
			timeSignatures = App.Project.getTimeSignatures();
		}
		return timeSignatures;
	}

	/* Helper method that paints the vertical lines for the piano roll and the overview */
	private static void paintBarPositions( Graphics g, int height, int margin ){

		// Get some info from the model
		timeSignatures = getTimeSignatures();

		int ticksPerQuarter = App.Project.getPPQ() * 4;

		// Initialize some local counters
		int tickCounter = 0, ticksPerCurrentQuarter = 0, timeSignaturesCounter = 0,
		currentX = 0, currentEvent = 0, nextX = 0; //(AppSystem.GUI.getZoomLevel() * timeSignatures[currentEvent][0])/timeSignatures[currentEvent][1];

		// Calculate the distance in between each small vertical line. 
		// This depends on the current resolution and zoom level
		double smallWidthReal = (double) App.UI.getZoomLevel()/App.UI.getResolution();
		int smallWidth = (int) Math.round(smallWidthReal);

		// Calculate the error that will occur when we convert the small line's positions to integers.
		int error = (int) ( App.UI.getResolution() * smallWidth - App.UI.getResolution() * smallWidthReal );
		int timeToFixError = 0, fix = 0;
		if( error != 0 ){

			// Calculate how often we need to fix the error
			timeToFixError = (int) Math.abs( Math.ceil(App.UI.getResolution()/error));

			// Define the fix (positive if error < 0)
			fix = -(error/Math.abs(error));

		}

		// Clear the lists
		mainBarPositions.clear();
		smallBarPositions.clear();

		// Paint as long as the song contains information
		while( tickCounter <= App.Project.getSequenceLength() || tickCounter <= GUIHelper.MIN_TICKS){

			// Calculate where the bar will start (this code looks a bit weird, but we always need to calculate nextX here, so let it be please) :)
			currentX = nextX;

			// If the current x is to the right of the visible area, return
			if( currentX > g.getClipBounds().getMaxX() ){
				return;
			}

			// If the next timeSignatures is the current tickCounter and the upcoming timeSignatures is not the first one ...
			if(timeSignatures[currentEvent+1].getPosition() == tickCounter && timeSignatures[currentEvent+1].getPosition() != 0 ){

				//Increase the value of the currentEvent, this will make the program draw a new time signature
				currentEvent++;

				// If this is the first timeSignature in the midi-file ...
				if( timeSignaturesCounter == 0 ){

					// We will have to get the information from the current event not the new
					// Set the number of ticks between the current time signatures
					ticksPerCurrentQuarter = ticksPerQuarter*timeSignatures[currentEvent-1].getNumerator()/timeSignatures[currentEvent-1].getDenominator();

				} else {

					//else we set the number of ticks between the current time signature to the upcoming time signature
					ticksPerCurrentQuarter = ticksPerQuarter*timeSignatures[currentEvent].getNumerator()/timeSignatures[currentEvent].getDenominator();
				}

				// If the track only contains one timesignature we dont want to update to a new one
			} else {
				ticksPerCurrentQuarter = ticksPerQuarter*timeSignatures[currentEvent].getNumerator()/timeSignatures[currentEvent].getDenominator();
			}

			// The ticksCounter is the old ticks + the new number of ticks between the current time signature
			tickCounter += ticksPerCurrentQuarter;

			// Increase time signature counter
			timeSignaturesCounter++;

			// Calculate the next x
			nextX += App.UI.getZoomLevel() * timeSignatures[currentEvent].getNumerator()/timeSignatures[currentEvent].getDenominator();

			// If the current x is to the left of the visible area, go to next round
			if( currentX < g.getClipBounds().getMinX() - App.UI.getZoomLevel() ) {
				continue;
			}


			// If we have reached this line we're in the visible area
			// Add the position to the arrays and paint the line
			g.setColor(Color.decode("#d6dde2"));
			g.drawLine(currentX + margin,0,currentX + margin,height);
			mainBarPositions.add( currentX );
			smallBarPositions.add( currentX );

			// Then, paint as many lines as possible between this bar and next.
			// The number of small bars between each main bar depends on the time signature and the resolution.
			int smallCounter = 1;
			g.setColor(Color.decode("#cacfd3"));

			if( App.UI.getResolution() != 1 ){ // Don't paint small bar lines if 1/1
				// Don't be scared..it's just assignment, an conditional-expression, modulo-calculation, increase and evaluation in one :) 
				while( (currentX += timeToFixError != 0 && smallCounter % timeToFixError == 0 ? smallWidth + fix : smallWidth) < nextX ){
					// Add the position to the array and paint the line
					smallCounter++;
					g.drawLine( currentX + margin, 0, currentX + margin, height );
					smallBarPositions.add( currentX );
				}
			}
		}
	}

	/* Returns the value closest to k in a sorted integer array. */
	private static int getClosestValue( int k, List<Integer> ns ){
		return ns.get(getIndexOfClosestValue( k, ns, 0, ns.size()-1 )); 
	}
	/* Returns the index of the value closest to k in a sorted integer array. */
	private static int getClosestIndex( int k, List<Integer> ns ){
		return getIndexOfClosestValue( k, ns, 0, ns.size()-1 ); 
	}
	/* Recursive function that returns the index of the value closest to k in a sorted integer array */
	private static int getIndexOfClosestValue( int k, List<Integer> ns, int first, int last ){

		if( first == last ){ 

			// Base case: Only one item left in the array, return it!
			return first; 

		} else {

			// Otherwise, calculate the split index and continue the search
			// in the part where k's closest value is
			int split = first + (last-first)/2;
			if( Math.abs(k-ns.get(split)) < Math.abs(k-ns.get(split+1)) ) {
				return getIndexOfClosestValue(k,ns,first,split); // k has it's closest value in the left part
			} else {
				return getIndexOfClosestValue(k,ns,split+1,last); // k has it's closest value in the right part
			}

		}

	}

	public void update(Observable o, Object arg) {

		// Check which type of action that was performed
		Action action = (Action) arg;

		switch( action ){

		case TIMESIGNATURE_ADDED:
		case TIMESIGNATRUE_EDIT:
		case TIMESIGNATURE_REMOVED:
			timeSignatures = App.Project.getTimeSignatures();
			break;

		}


	}


}

