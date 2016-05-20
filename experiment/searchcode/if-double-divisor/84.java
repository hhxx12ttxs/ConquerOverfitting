/**
 * 
 */
package net.cellingo.sequence_tools.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JPanel;

import net.cellingo.sequence_tools.annotation.SequenceCoordinates;

/**
 * This class can be used to draw sequence maps with different sequence lanes, possible of variable height,
 * showing sequence elements. These elements should be presented in so-called SequenceTrack objects. 
 * @author Michiel Noback (www.cellingo.net, michiel@cellingo.net)
 * @version 1.0
 */
public class SequenceMap extends JPanel {
	private static final long serialVersionUID = 1L;
	//the list of sequence tracks
	private ArrayList<SequenceTrack> sequenceTracks;
	//the title of the map
	private String mapTitle;
	//the height of the map title
	private int mapTitleHeight = 15;
	//the heigth of the map; if not set it will be determined dynamically
	private int mapHeight = 0;
	//the width of the map
	private int mapWidth = 600;
	//the position at which to start numbering
	private int startPosition;
	//the length of the sequence to display
	private int sequenceLength;
	//the image background color
	private Color backgroundColor;
	//the offset for current track that is processed
	private int currentTrackOffset = 0;
	//defines whether a ruler will be drawn or not
	private boolean createRuler = true;
	//the height of the ruler
	private double rulerHeight = 15;
	//defines whether tracks will have a legend displayed 
	private boolean createTrackLegend = false;
	
	//trackLegendWidth defines the width of the track legend
	private int trackLegendWidth = 0;
	//the distance between two ruler ticks in nucleotides
	private int tickSize = 1;
	//the nucleotide to pixel ratio used for all calculations
	private double nt2pixelRatio;
	//the font used for track legends
	private Font legendFont;
	
	//if true, an image map will be created which can be fetched by calling getImageMap(); 
	private boolean createImageMap = false;
	//the stringbuilder object that will hold the image map
	private StringBuilder imageMap;
	//a listener for events on the sequence map
	private SequenceMapListener listener;
	//flag whether graphics coordinates should be created
	private boolean createGraphicsCoordinates;
	//a toggle to indicate the first drawing round is done; to prevent recalculation of graphics coordinates
	private boolean firstPaint = true;
	//defines whether a slider should be added
	private boolean useSlider = false;
	//graphics Coordinate of the slider
	private GraphicsCoordinates sliderCoordinates;
	//the SequenceCoordinates selection coordinates of the slider
	private SequenceCoordinates sequenceSelectionCoodinates;
	

	/**
	 * constructs with sequence length and map width 
	 * @param sequenceLength in nucleotides
	 * @param mapWidth in pixels
	 */
	public SequenceMap( int sequenceLength, int mapWidth ){
		super();
		this.sequenceLength = sequenceLength;
		this.mapWidth = mapWidth;
		initialize();
	}

	/**
	 * constructs with sequence length, map width and a listener to map events 
	 * @param sequenceLength in nucleotides
	 * @param mapWidth in pixels
	 * @param listener
	 */
	public SequenceMap( int sequenceLength, int mapWidth, SequenceMapListener listener ){
		super();
		this.sequenceLength = sequenceLength;
		this.mapWidth = mapWidth;
		this.listener = listener;
		if(this.listener != null){
			this.createGraphicsCoordinates = true;
			initiateListenerInterface(false);
		}
		initialize();
	}

	/**
	 * constructs with sequence length, map width, a listener to map events and a flag whether to use a slider for navigation 
	 * @param sequenceLength in nucleotides
	 * @param mapWidth in pixels
	 * @param listener
	 * @param useSlider
	 */
	public SequenceMap( int sequenceLength, int mapWidth, SequenceMapListener listener, boolean useSlider ){
		super();
		this.sequenceLength = sequenceLength;
		this.mapWidth = mapWidth;
		this.listener = listener;
		/*it only makes sence to add a slider when there is a listener*/
		if(this.listener != null){
			this.createGraphicsCoordinates = true;
			initiateListenerInterface(useSlider);
		}
		initialize();
	}

	/**
	 * initializes the correct type of event handling
	 */
	private void initiateListenerInterface(boolean useSlider){
		this.useSlider = useSlider;
		if(listener.getSelectionType() == SequenceMapListener.SELECTION_TYPE_REGION ){
			MapRegionSelectionListener mapMouseListener = new MapRegionSelectionListener();
			this.addMouseListener( mapMouseListener );
			this.addMouseMotionListener( new MapMouseMotionListener( mapMouseListener ) );
		}
		else if(listener.getSelectionType() == SequenceMapListener.SELECTION_TYPE_TRACK_AND_ELEMENTS ){
			MapElementSelectionListener mapMouseListener = new MapElementSelectionListener();
			this.addMouseListener( mapMouseListener );
		}
		else if(listener.getSelectionType() == SequenceMapListener.SELECTION_TYPE_ALL ){
			MapRegionSelectionListener mapMouseListener = new MapRegionSelectionListener();
			this.addMouseListener( mapMouseListener );
			this.addMouseMotionListener( new MapMouseMotionListener( mapMouseListener ) );

			MapElementSelectionListener mapElementSelectionListener = new MapElementSelectionListener();
			this.addMouseListener( mapElementSelectionListener );
		}	
	}
	
	/**
	 * initialization procedures
	 */
	private void initialize(){
		this.sequenceTracks = new ArrayList<SequenceTrack>();
		this.legendFont = new Font( "sansserif", Font.BOLD, 12 );
		this.backgroundColor = Color.LIGHT_GRAY;
	}
	
	/**
	 * @param mapWidth the mapWidth to set
	 */
	public void setMapWidth(int mapWidth) {
		this.mapWidth = mapWidth;
	}

	/**
	 * @param createImageMap the createImageMap to set
	 */
	public void setCreateImageMap(boolean createImageMap) {
		this.createImageMap = createImageMap;
		if( this.createImageMap ){
			this.imageMap = new StringBuilder();
			imageMap.append("<map name=\"sequenceImageMap\">");
		}
	}
	
	/**
	 * get the image map as string representation. If not present, an emprty string will be returned
	 * @return image map as string
	 */
	public String getImageMap(){
		if(imageMap == null) return "";
		else{
			imageMap.append("</map>");
			return imageMap.toString();
		}
	}

	/**
	 * @param trackLegendWidth the trackLegendWidth to set
	 */
	public void setTrackLegendWidth(int trackLegendWidth) {
		this.trackLegendWidth = trackLegendWidth;
	}

	/**
	 * @param mapTitleHeight the mapTitleHeight to set
	 */
	public void setMapTitleHeight(int mapTitleHeight) {
		this.mapTitleHeight = mapTitleHeight;
	}

	/**
	 * @param rulerHeight the rulerHeight to set
	 */
	public void setRulerHeight(double rulerHeight) {
		this.rulerHeight = rulerHeight;
	}

	/**
	 * add a sequence track
	 * @param sequenceTrack
	 */
	public void addSequenceTrack( SequenceTrack sequenceTrack ){
		sequenceTrack.setTrackWidth(this.mapWidth);
		sequenceTrack.setCreateGraphicsCoordinates(createGraphicsCoordinates);
		this.sequenceTracks.add( sequenceTrack );
	}
	
	/**
	 * specify whether a ruler should be drawn;
	 * default is "true"
	 * @param createRuler
	 */
	public void setCreateRuler( boolean createRuler ){
		this.createRuler = createRuler;
	}
	
	/**
	 * @return the mapTitle
	 */
	public String getMapTitle() {
		return mapTitle;
	}

	/**
	 * @param mapTitle the mapName to set
	 */
	public void setMapTitle(String mapTitle) {
		this.mapTitle = mapTitle;
	}

	/**
	 * @return the mapHeight
	 */
	public int getMapHeight() {
		return mapHeight;
	}

	/**
	 * @return the mapWidth
	 */
	public int getMapWidth() {
		return mapWidth;
	}

	/**
	 * @return the startPosition
	 */
	public int getSequenceStartPosition() {
		return startPosition;
	}

	/**
	 * @param startPosition the startPosition to set
	 */
	public void setSequenceStartPosition(int startPosition) {
		this.startPosition = startPosition;
	}

	/**
	 * @return the sequenceLength
	 */
	public int getSequenceLength() {
		return sequenceLength;
	}

	/**
	 * @param sequenceLength the sequenceLength to set
	 */
	public void setSequenceLength(int sequenceLength) {
		this.sequenceLength = sequenceLength;
	}
	
	/**
	 * set the background color of the image
	 * @param color
	 */
	public void setBackground( Color color ){
//		super.setBackground(color);
		this.backgroundColor = color;
	}

	/**
	 * sets the coordinates of the viewable area of this sequence map and causes a redraw of the map
	 * @param coordinates
	 */
	public void setSequenceCoordinates( SequenceCoordinates coordinates ) {
		this.startPosition = coordinates.getStart();
		this.sequenceLength = ( coordinates.getStop() - coordinates.getStart() );
		//everything has to be recalculated when a repaint is performed
		this.firstPaint = true;
		paintComponent(getGraphics());
	}

	
	/**
	 * sets the start and stop of the slider in sequence coordinates.
	 * @param coordinates
	 * @throws IllegalArgumentException when selection is out of range
	 */
	public void setSequenceSelectionCoordinates( SequenceCoordinates coordinates ) throws IllegalArgumentException{
		if( coordinates.getStart() < this.startPosition ) throw new IllegalArgumentException("start position " + coordinates.getStart() + " out of range");
		if( coordinates.getStop() > (this.startPosition+this.sequenceLength) ) throw new IllegalArgumentException("stop position " + coordinates.getStop() + " out of range");
		//coordinates checked out: process it
		this.sequenceSelectionCoodinates = coordinates;
		
		int left = this.trackLegendWidth + (int)( coordinates.getStart() * nt2pixelRatio );
		int width = (int)( (coordinates.getStop() - coordinates.getStart()) * nt2pixelRatio );
		this.sliderCoordinates = new GraphicsCoordinates(2, left, width, this.mapHeight-4);
		paintComponent(getGraphics());
	}
	
	/**
	 * sets the initial slider selection
	 * @param coordinates
	 * @throws IllegalArgumentException
	 */
	public void setInitSequenceSelectionCoordinates( SequenceCoordinates coordinates ) throws IllegalArgumentException{
		if( coordinates.getStart() < this.startPosition ) throw new IllegalArgumentException("start position " + coordinates.getStart() + " out of range");
		if( coordinates.getStop() > (this.startPosition+this.sequenceLength) ) throw new IllegalArgumentException("stop position " + coordinates.getStop() + " out of range");
		//coordinates checked out: process it
		this.sequenceSelectionCoodinates = coordinates;
		
	}
	
	/**
	 * returns the currently selected coordinates
	 * @return
	 */
	public SequenceCoordinates getSequenceSelectionCoordinates(){
		return sequenceSelectionCoodinates;
	}
	
	/**
	 * returns the stop of the slider (sequence position)
	 * @return sliderStop
	 */
	public int getSliderStop(){
		return this.sequenceSelectionCoodinates.getStop();
	}
	
	/**
	 * returns the start of the slider (sequence position)
	 * @return sliderStart
	 */
	public int getSliderStart(){
		return this.sequenceSelectionCoodinates.getStart();
	}
	
	
	/**
	 * draw the sequence map
	 * @return image
	 */
	public BufferedImage drawMap(){
		
		//first determine the track height and other dynamic parameters
		calculateDynamicParameters();
		
		BufferedImage image = new BufferedImage( mapWidth, mapHeight, BufferedImage.TYPE_INT_RGB);
		//create the graphics object and draw the background
		Graphics2D graphics = image.createGraphics();
		graphics.setColor(backgroundColor);
		graphics.fillRect(0,0,mapWidth,mapHeight);
		
		
		if( this.mapTitle != null){
			createTitle( graphics );
		}
		if(this.createRuler){
			createRuler( graphics );
		}
		int legendOffset = currentTrackOffset;
		if(this.sequenceTracks.size() != 0){
			createTracks( graphics );
		}
		if(this.createTrackLegend){
			createTrackLegends( legendOffset, graphics );
		}
		
		return image;
	}
	
	/**
	 * same as the drawMap() method, but this one for GUI integration
	 */
	protected void paintComponent(Graphics g){
		//System.out.println( this.getClass().getSimpleName() + " painting components...");
		
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;
		
		if(firstPaint){
			calculateDynamicParameters();
		}
		currentTrackOffset = 0;
		
		g2.setColor( this.backgroundColor );
		g2.fillRect(0,0,this.mapWidth,this.mapHeight);
		
		g2.setColor(Color.BLACK);
		g2.draw3DRect(0, 0, this.mapWidth, this.mapHeight, false);
		
		if( this.mapTitle != null){
			createTitle( g2 );
		}
		if(this.createRuler){
			createRuler( g2 );
		}
		int legendOffset = currentTrackOffset;
		if(this.sequenceTracks.size() != 0){
			createTracks( g2 );
		}
		if(this.createTrackLegend){
			createTrackLegends( legendOffset, g2 );
		}
		if(this.useSlider){
			createSlider( g2 );
		}
		firstPaint = false;
	}

	/**
	 * calculate several dynamic parameters that are dependent on the data and settings
	 */
	private void calculateDynamicParameters(){

		//determine the height of the map
		determineMapHeigth();

		//determine tick size for ruler
		if(this.createRuler){
			for( int i=10; (i<100000000 && this.sequenceLength > i); i*=10 ){
				//System.out.println( "i=" + i );
				this.tickSize = i ;
			}
		}
		
		/*the conversion ratio between pixels and nucleotides*/ 
		this.nt2pixelRatio = (double)(this.mapWidth - this.trackLegendWidth)/ (double)this.sequenceLength;
		
		
		//System.out.println( this.getClass().getSimpleName() + ".calculateDynamicParameters() map:" + getName() + ": visible sequence length=" + sequenceLength + " tickSize=" + tickSize);
		
		
		if(useSlider){
			
			if(sequenceSelectionCoodinates == null ){
				int left = this.trackLegendWidth;// + (int)(this.sliderStart * nt2pixelRatio);
				//int width = (int)(((this.startPosition + this.tickSize) - this.startPosition) * nt2pixelRatio);
				int width = (int)( this.tickSize * nt2pixelRatio);
				this.sliderCoordinates = new GraphicsCoordinates(0, left, width, this.mapHeight);
				
				//int newStart = (int)((sliderCoordinates.getLeft() - trackLegendWidth) / nt2pixelRatio);
				int selStop = (int)(( (sliderCoordinates.getLeft() + sliderCoordinates.getWidth())- trackLegendWidth) / nt2pixelRatio) + startPosition;
				this.sequenceSelectionCoodinates = new SequenceCoordinates(this.startPosition, selStop, false, true );
			}
			else{
				int left = this.trackLegendWidth + (int)( sequenceSelectionCoodinates.getStart() * nt2pixelRatio );
				int width = (int)( (sequenceSelectionCoodinates.getStop() - sequenceSelectionCoodinates.getStart()) * nt2pixelRatio );
				this.sliderCoordinates = new GraphicsCoordinates(2, left, width, this.mapHeight-4);
				
//				int left = this.trackLegendWidth;// + (int)(this.sliderStart * nt2pixelRatio);
//				//int width = (int)(((this.startPosition + this.tickSize) - this.startPosition) * nt2pixelRatio);
//				int width = (int)( this.tickSize * nt2pixelRatio);
//				this.sliderCoordinates = new GraphicsCoordinates(0, left, width, this.mapHeight);
			}
		}
	}
	
	/**
	 * determine the height of the map depending on the number of tracks and track lanes
	 */
	private void determineMapHeigth(){
		this.mapHeight = 0;
		if( createRuler ){
			this.mapHeight += this.rulerHeight;
		}
		if( this.mapTitle != null ){
			this.mapHeight += this.mapTitleHeight;
		}

		//check whether any of the tracks has a legend. If so, it will be displayed and the nt2pixelRatio is redefined
		int maxTrackLegendLength = 0;
		for( SequenceTrack track : sequenceTracks ){
			
			this.mapHeight += track.getTrackHeight( this.startPosition, this.startPosition+this.sequenceLength );
			
			if( track.getTrackName() != null ){
				if( track.getTrackName().length() > maxTrackLegendLength ){
					maxTrackLegendLength = track.getTrackName().length();
				}
				this.createTrackLegend = true;
			}
		}
		
		//System.out.println( this.getClass().getSimpleName() + ".determineMapHeigth(): mapheight=" + mapHeight );
		
		//calculate the width of the track legend on the fly if not specified
		if(trackLegendWidth == 0){
			trackLegendWidth = (maxTrackLegendLength * (int)((double)legendFont.getSize() * 0.5) ) + legendFont.getSize(); 
		}
	}
	
	/**
	 * dynamically calculate the track heights
	 */
//	private void calculateTrackHeights() {
//		double divisor = 0;
//		double availableHeight = this.mapHeight;
//		
//		if(createRuler && rulerHeight==0){
//			divisor += 1;
//		}
//		else if( createRuler && rulerHeight!=0 ){
//			availableHeight -= this.rulerHeight;
//		}
////		if( useSlider ){
////			divisor += 1;
////			availableHeight -= (sliderCoordinates.getHeight() + 4);
////		}
//		if(this.mapName != null && (mapTitleHeight==0) ){
//			divisor += 1;
//		}
//		else if( this.mapName != null && (mapTitleHeight!=0) ){
//			availableHeight -= this.mapTitleHeight;
//		}
//		
//		//calculate the track height
//		int trackHeight = (int)(availableHeight / divisor );
//		//set ruler height
//		if(rulerHeight==0){
//			this.rulerHeight = trackHeight;
//		}
//		//set title height
//		if(mapTitleHeight==0){
//			this.mapTitleHeight = trackHeight;
//		}
//		
//		//check whether any of the tracks has a legend. If so, it will be displayed and the nt2pixelRatio is redefined
//		int maxTrackLegendLength = 0;
//		for( SequenceTrack track : sequenceTracks ){
//			track.setTrackHeight((int)trackHeight);
//			if( track.getTrackName() != null ){
//				if( track.getTrackName().length() > maxTrackLegendLength ){
//					maxTrackLegendLength = track.getTrackName().length();
//				}
//				this.createTrackLegend = true;
//			}
//		}
//		//calculate the width of the track legend on the fly if not specified
//		if(trackLegendWidth == 0){
//			trackLegendWidth = (maxTrackLegendLength * (int)((double)legendFont.getSize() * 0.5) ) + legendFont.getSize(); 
//		}
//	}

	/**
	 * if non-collapsing tracks are to be displayed, the element heights should be 
	 * calculated for each individual track depending on the number of overlapping
	 * elements on one particular position
	 */
//	private void calculateTrackLaneHeights(){
//		int availableHeight = 0;
//		int allLanesSummed = 0;
//		for( SequenceTrack track : sequenceTracks ){
//			Iterator<SequenceTrackElement> trackElements = track.getSequenceTrackElements();
//			int maxTrackLanes = 1;
//			int trackLane = 1;
//			int firstLaneStop = -1;
//			while(trackElements.hasNext()){
//				SequenceTrackElement element = trackElements.next();
//				int start = element.getParentStart();
//				if( start < (this.startPosition + this.sequenceLength) ){
//
//					if(start > firstLaneStop){
//						trackLane = 1;
//						element.setTrackLane(trackLane);
//						firstLaneStop = element.getParentStop();
//					}
//					else{ //there is an overlap
//						trackLane++;
//						element.setTrackLane(trackLane);
//						if(trackLane > maxTrackLanes){
//							maxTrackLanes = trackLane;
//						}
//					}
//				}
//			}
//			track.setTrackLaneNumber(maxTrackLanes);
//			availableHeight += track.getTrackHeight();
//			allLanesSummed += maxTrackLanes;
//			
//			//track.setTrackLaneHeight( track.getTrackHeight() / maxTrackLanes );
//		}
//		//now redistribute the available height over the tracks according to the maximum
//		for( SequenceTrack track : sequenceTracks ){
//			track.setTrackHeight( (double)availableHeight * ( (double)track.getTrackLaneNumber() / allLanesSummed ) );
//			track.setTrackHeight( track.getTrackHeight() / track.getTrackLaneNumber() );
//		}
//		
//	}
	
	/**
	 * create the map slider
	 * @param g
	 */
	private void createSlider( Graphics2D g ){
		//g.setColor( new Color(180,80,80) );
		Color sliderColor = new Color(180,80,80, 50);
		//g.setColor(sliderColor);
		
		//Paint a gradient in graph background area from top to bottom
		GradientPaint gpTop = new GradientPaint( sliderCoordinates.getWidth()/2,
				sliderCoordinates.getTop(), 
				sliderColor.darker(), 
				sliderCoordinates.getWidth()/2, 
				sliderCoordinates.getTop()+(sliderCoordinates.getHeight()/2), 
				sliderColor );
		g.setPaint( gpTop );
		g.fillRect( sliderCoordinates.getLeft(), sliderCoordinates.getTop(), sliderCoordinates.getWidth(), sliderCoordinates.getHeight()/2 );

		GradientPaint gpBottom = new GradientPaint( sliderCoordinates.getWidth()/2,
				sliderCoordinates.getTop()+(sliderCoordinates.getHeight()/2), 
				sliderColor, 
				sliderCoordinates.getWidth()/2, 
				sliderCoordinates.getTop()+sliderCoordinates.getHeight(), 
				sliderColor.darker() );
		g.setPaint( gpBottom );
		g.fillRect( sliderCoordinates.getLeft(), sliderCoordinates.getTop()+(sliderCoordinates.getHeight()/2), sliderCoordinates.getWidth(), sliderCoordinates.getHeight()/2 );

		//g.drawRoundRect(sliderCoordinates.getLeft(), sliderCoordinates.getTop(), sliderCoordinates.getWidth(), sliderCoordinates.getHeight(), 5, 5);
		//g.fill3DRect(sliderCoordinates.getLeft(), sliderCoordinates.getTop(), sliderCoordinates.getWidth(), sliderCoordinates.getHeight(), true);
		
		//g.fill3DRect(sliderCoordinates.getLeft(), sliderCoordinates.getTop(), sliderCoordinates.getWidth(), sliderCoordinates.getHeight(), true);
		//currentTrackOffset += sliderCoordinates.getHeight();
	}
	
	/**
	 * create the map title
	 * @param g
	 */
	private void createTitle( Graphics2D g ){
		int fontHeight = (int)( (double)this.mapTitleHeight * 0.8 );
		g.setColor( Color.BLACK );
		Font titleFont = new Font( "sansserif", Font.BOLD, fontHeight );
		g.setFont(titleFont);
		
//		System.out.println("creating title " + getMapName() 
//				+ " fontheight=" + fontHeight 
//				+ " trackLegendWidth=" + trackLegendWidth 
//				+ " currentTrackOffset=" + currentTrackOffset 
//				+ " mapTitleHeight=" + mapTitleHeight);
		
		g.drawString( getMapTitle(), trackLegendWidth, (currentTrackOffset + this.mapTitleHeight - 2 ) );
		
		//and finally reset the track offset
		currentTrackOffset += mapTitleHeight;
		//System.out.println("title created");
	}
	
	/**
	 * create the legends for each track
	 */
	private void createTrackLegends( int legendOffset, Graphics2D g ){
		int elementYoffset = legendOffset;
		for( int trackNo=0; trackNo<this.sequenceTracks.size(); trackNo++){
			//int yOffset = 0;
			SequenceTrack track = this.sequenceTracks.get(trackNo);
			//int elementYoffset = (int)( (trackNo + 1) * track.getTrackHeight() );
			
			g.setColor( Color.BLACK );
			g.setFont(legendFont);
			if(track.getTrackName() != null){
				g.drawString( track.getTrackName(), 2, (int)(elementYoffset + legendFont.getSize()+1 ) );
				
				if(listener != null && firstPaint){
					GraphicsCoordinates gc = new GraphicsCoordinates( elementYoffset, 2, trackLegendWidth, (int)track.getTrackHeight( this.startPosition, this.startPosition+this.sequenceLength ) );
					track.setGraphicsCoordinates(gc);
				}
			}
			elementYoffset += (int)track.getTrackHeight( this.startPosition, this.startPosition+this.sequenceLength );
		}
	}
	
	/**
	 * create a rule in the image
	 * @param g
	 */
	private void createRuler( Graphics2D g ){
		//ruler will be drawn on the first or second track of the map, depending whether a title has been drawn
		int rulerLineHeight = (int)( this.rulerHeight * 0.1 );
		if( rulerLineHeight > 3 ){
			rulerLineHeight = 3;
		}
		else if( rulerLineHeight < 1 ){
			rulerLineHeight = 1;
		}
		
		int rulerLineOffset = currentTrackOffset + (int)( (this.rulerHeight / 2) - ( rulerLineHeight/2 ) );
		int rulerTickHeight = (int)(this.rulerHeight * 0.6);
		int rulerTickOffset = currentTrackOffset + (int)(this.rulerHeight * 0.2);
		int tickPixelDistance = (int)( (double)this.tickSize * this.nt2pixelRatio );
		int tickNumber = sequenceLength / tickSize; 
			
		g.setColor( Color.BLACK );
		//draw horizontal line
		g.fillRect(trackLegendWidth, rulerLineOffset, (this.mapWidth-trackLegendWidth), rulerLineHeight);
		g.setFont( new Font( "sansserif", Font.PLAIN, (int)(rulerHeight * 0.5) ) );
		//draw ticks
		for( int i=0; i<=tickNumber; i++){
			g.fillRect( ( this.trackLegendWidth + (i * tickPixelDistance) ), rulerTickOffset, rulerLineHeight, rulerTickHeight);
			
			String tickStr ="" + ( this.startPosition + (i*tickSize) );
			int left = (this.trackLegendWidth + (i * tickPixelDistance + rulerLineHeight + 1));
			if( left < mapWidth-50){
				g.drawString(tickStr , left, (currentTrackOffset + (int)rulerHeight));
			}
		}
		//and finally reset the track offset
		currentTrackOffset += rulerHeight;
	}
	

	/**
	 * create the actual sequence tracks
	 */
	private void createTracks( Graphics2D g ){
		int endPosition = (this.startPosition + this.sequenceLength);
		
		/*iterate the tracks*/
		for( int trackNo=0; trackNo<this.sequenceTracks.size(); trackNo++){			
			SequenceTrack track = this.sequenceTracks.get(trackNo);
			
			/*sets the base color*/
			g.setBackground(backgroundColor);
			
			/*let the track draw itself, and its components as well*/
			track.drawTrack( g, nt2pixelRatio, trackLegendWidth, currentTrackOffset, startPosition, endPosition );
			
			/*update the vertical track offset*/
			currentTrackOffset+=track.getTrackHeight( this.startPosition, this.startPosition+this.sequenceLength );
		}
	}

	/**
	 * listens to mouse motion events; is used for region selection
	 */
	private class MapMouseMotionListener implements MouseMotionListener{
		//the listener to mouse click events
		private MouseClickPersistable mouseClickPersistable;

		public MapMouseMotionListener( MouseClickPersistable mouseClickPersistable ){
			this.mouseClickPersistable = mouseClickPersistable;
		}
		
		public void mouseDragged(MouseEvent e) {
			//System.out.println( "dragging mouse..." );
			int x = e.getX();
			if( sliderCoordinates.overlaps(x, e.getY()) ){
				//System.out.println( "dragging slider..." );
				int newLeft = x - sliderCoordinates.getWidth()/2;
				if( newLeft > (mapWidth - sliderCoordinates.getWidth() ) ) newLeft = (mapWidth - sliderCoordinates.getWidth() );
				else if( newLeft < trackLegendWidth ) newLeft = trackLegendWidth;
					
				sliderCoordinates.setLeft( newLeft );
				//paintComponent(getGraphics());
			}
			else{
				//can be sure the mouse drag started outside the slider
				int startX = mouseClickPersistable.getXmouseStart();
				if( x > startX ){
					//moving to the right
					//System.out.println( "dragging new forward slider selection" );
					sliderCoordinates.setLeft(startX);
					sliderCoordinates.setWidth(x - startX );
					
				}
				else if( x < startX ){
					//moving to the left
					//System.out.println( "dragging new reverse slider selection" );
					sliderCoordinates.setLeft(x);
					sliderCoordinates.setWidth( startX - x );
				}
			}
			paintComponent(getGraphics());
		}
		public void mouseMoved(MouseEvent e) { /*do nothing*/ }
	}

	
	/**
	 * simple region selection listener using a draggable slider
	 */
	private class MapRegionSelectionListener implements MouseListener, MouseClickPersistable{
		private int xMouseStart;
		private int yMouseStart;
		private int xMouseEnd;
		//private int yMouseEnd;
		private boolean createNewSelection = false;
		private boolean sliderSelected = false;
		
		public int getXmouseStart(){
			return xMouseStart;
		}
		public int getYmouseStart(){
			return xMouseStart;
		}
		
		public void mouseClicked(MouseEvent e) {
			//System.out.println("mouse clicked on the map x=" + e.getX() + " y=" + e.getY());
		}

		public void mouseEntered(MouseEvent e) {
			//System.out.println("mouse entered on the map");
		}

		public void mouseExited(MouseEvent e) {
			//System.out.println("mouse exited the map");			
		}

		public void mousePressed(MouseEvent e) {
			//System.out.println("mouse pressed on the map");
			xMouseStart = e.getX();
			yMouseStart = e.getY();
			/*check whether the slider was NOT selected; if the slider is selected, handle it through MouseMotionListener*/
			if( xMouseStart < trackLegendWidth ){
				createNewSelection = false;
				sliderSelected = false;
			}
			else if( ! sliderCoordinates.overlaps(xMouseStart, yMouseStart) ){ //not in slider
				createNewSelection = true;
				sliderSelected = false;
			}
			else{
				createNewSelection = false;
				sliderSelected = true;
			}
		}

		public void mouseReleased(MouseEvent e) {
			xMouseEnd = e.getX();

			//only if a real area was dragged is a new selection area created
			if( (xMouseStart - xMouseEnd)*(xMouseStart - xMouseEnd) <= 4 ) createNewSelection = false;

//			System.out.println( "xstart=" + xMouseStart + " xend=" + xMouseEnd + " ystart=" + yMouseStart + " yend=" + yMouseEnd 
//					+ " createNewSelection=" + createNewSelection + " sliderSelected=" + sliderSelected );

			/*a new slider position and width was created*/
			if( createNewSelection ){
				if( xMouseStart > trackLegendWidth ){
					int start;
					int stop;
					boolean complement = false;
					/*determine orientation with dragging of significant area*/
					
					if( xMouseEnd - xMouseStart > 2 ){
						//System.out.println("mouse dragged forward on the map");
						start = (int)( (xMouseStart - trackLegendWidth) / nt2pixelRatio ) + startPosition;
						stop = (int)( (xMouseEnd - trackLegendWidth) / nt2pixelRatio) + startPosition;
						if(stop > sequenceLength ) stop = sequenceLength;
						sequenceSelectionCoodinates.setStart(start);
						sequenceSelectionCoodinates.setStop(stop);
						sequenceSelectionCoodinates.setComplement(complement);
						listener.regionSelected( sequenceSelectionCoodinates );
					}
					else if( xMouseStart - xMouseEnd > 2 ){
						//System.out.println("mouse dragged reverse on the map");
						complement = true;
						start = (int)( (xMouseEnd - trackLegendWidth) / nt2pixelRatio ) + startPosition;
						if( start < 0 ) start = 0;
						stop = (int)( (xMouseStart - trackLegendWidth) / nt2pixelRatio) + startPosition;
						sequenceSelectionCoodinates.setStart(start);
						sequenceSelectionCoodinates.setStop(stop);
						sequenceSelectionCoodinates.setComplement(complement);
						listener.regionSelected( sequenceSelectionCoodinates );
					}
					//else{ /*nothing interesting happened*/ }
					
					paintComponent(getGraphics());
				}
			}
			else if(sliderSelected){
				int newStart = (int)((sliderCoordinates.getLeft() - trackLegendWidth) / nt2pixelRatio) + startPosition;
				int newStop =  (int)(( (sliderCoordinates.getLeft() + sliderCoordinates.getWidth())- trackLegendWidth) / nt2pixelRatio) + startPosition;
				//System.out.println("new sequence coordinates: start=" + newStart + " stop=" + newStop);
				sequenceSelectionCoodinates.setStart(newStart);
				sequenceSelectionCoodinates.setStop(newStop);
				listener.regionSelected( sequenceSelectionCoodinates );
				paintComponent(getGraphics());
			}
		}
	}
	
	/**
	 * listener and handler of element selection types
	 */
	private class MapElementSelectionListener implements MouseListener{
		public void mouseClicked(MouseEvent e) {
			//System.out.println("mouse clicked on the map x=" + e.getX() + " y=" + e.getY());
			int x = e.getX();
			int y = e.getY();
			
			/*check for click in track legends*/
			for( SequenceTrack track : sequenceTracks ){
				GraphicsCoordinates trackCoordinates = track.getGraphicsCoordinates();
				/*only enter if within track height range*/
				if( y > trackCoordinates.getTop() && y < (trackCoordinates.getTop() + trackCoordinates.getHeight() ) ){
					if( trackCoordinates.overlaps( x, y ) ){
						if(track.isSelected()) track.setSelected(false);
						else{
							track.setSelected(true);
							listener.trackSelected(track);
						}
					}
					else track.setSelected(false);

					/*check all track elements*/
					for( SequenceTrackElement ste : track.getSequenceTrackElementList() ){
						//System.out.println( "checking track element " + ste + " with x=" + x + " y=" + y );
						if( ste.getGraphicsCoordinates() != null && ste.getGraphicsCoordinates().overlaps(x, y) ){// &&
							if(ste.isSelected()) ste.setSelected(false);
							else{
								ste.setSelected(true);
								listener.trackElementSelected(ste);
							}
						}
						else ste.setSelected(false);
					}
				}
			}
			paintComponent(getGraphics());
		}

		public void mouseEntered(MouseEvent e) { }
		
		public void mouseExited(MouseEvent e) { }
		
		public void mousePressed(MouseEvent e) { }
		
		public void mouseReleased(MouseEvent e) { }
	}
}

