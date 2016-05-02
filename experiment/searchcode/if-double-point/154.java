<<<<<<< HEAD
package cz.vutbr.fit.pdb03.gui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MemoryTileCache;
import org.openstreetmap.gui.jmapviewer.OsmTileLoader;
import org.openstreetmap.gui.jmapviewer.OsmTileSource;

import cz.vutbr.fit.pdb03.AnimalsDatabase;
import cz.vutbr.fit.pdb03.Log;
import cz.vutbr.fit.pdb03.controllers.MapController;

/**
 * Trida rozsirujici moznosti zakladni mapy
 * @author Ondřej Beneš <xbenes00@stud.fit.vutbr.cz>
 *
 */
public class JMapPanel extends JMapViewer {

	private static final long serialVersionUID = -7269660504108541606L;

	private final static BasicStroke stroke = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);

	private final static double DEFAULT_LAT = 49.226776664238656;
	private final static double DEFAULT_LON = 16.59532070159912;
	private final static int DEFAULT_ZOOM = 2;

	private final static int MY_POINT_SIZE = 8;
	private final static Color MY_POINT_COLOR = Color.RED;

	private final static int POINT_SIZE = 8;
	private final static Color POINT_COLOR = Color.GREEN;

	private final static Color POINT_SELECTED_COLOR = Color.CYAN;

	private final static Color CURVE_COLOR = Color.GREEN;
	private final static Color CURVE_SELECTED_COLOR = Color.CYAN;

	// konstanty akci
	public final static String ACTION_EDIT = "EDIT";
	public final static String ACTION_SAVE = "SAVE";
	public final static String ACTION_CHANGE_TYPE = "CHANGE";
	public final static String ACTION_CANCEL = "CANCEL";

	public final static int MODE_POINT = 0;
	public final static int MODE_CURVE = 1;
	public final static int MODE_POLYGON = 2;

	// maximalni distance, ktera se bere v uvahu
	private final static double MAX_DISTANCE = POINT_SIZE/2;

	// hlavni frame
	AnimalsDatabase frame;

	// kontroler
	MapController mapController;

	// indikace editacniho modu
	private boolean editMode = false;

	// mod vykreslovani
	private int drawMode = MODE_POINT;

	// komponenta pro mapu
	JButton bEdit, bSave, bCancel;
	private JComboBox comboElements;

	// detekce kolize
	private JEntity hitEntity;

	// data
	JEntity myPosition;	// moje poloha
	List<JEntity> data;	// puvodni data

	List<JEntity> insertData;	// nova data
	List<JEntity> updateData;	// zmenena data
	List<JEntity> deleteData;	// odstranene entity

	List<JEntity> tempDraw;	// docasne pole pro kresleni krivek a polygonu

	public JMapPanel(AnimalsDatabase frame) {
		super(new MemoryTileCache(), 4);

		// hlavni frame
		this.frame = frame;

		// kontrolery
		mapController = new MapController(this);

		// vlastnosti mapy
		setPreferredSize(null);
		setTileSource(new OsmTileSource.CycleMap());
		setTileLoader(new OsmTileLoader(this));

		// inicializace tlacitek
		initEditButtons();

		// inicializace dat
		initData();

		// vycentrovani
		setDisplayPositionByLatLon(DEFAULT_LAT, DEFAULT_LON, DEFAULT_ZOOM);
	}


	/**
	 * Inicializace editacnich tlacitek
	 */
	private void initEditButtons(){

		int buttonSizeX = 80;
		int buttonSizeY = 20;
		int smallSpace = 10;

		// edit tlacitko
		bEdit = new JButton("Upravit");
                bEdit.setToolTipText("Po kliknutí lze upravovat a mazat geometrické entity s ohledem na vaše nastavení času");
		bEdit.setBounds(50, smallSpace, buttonSizeX, buttonSizeY);
		bEdit.setActionCommand(ACTION_EDIT);
		bEdit.addActionListener(mapController);
		add(bEdit);

		bCancel = new JButton("Zrušit");
                bCancel.setToolTipText("Provedené změny nebudou uloženy");
		bCancel.setBounds(50, smallSpace + 30, buttonSizeX, buttonSizeY);
		bCancel.setActionCommand(ACTION_CANCEL);
		bCancel.addActionListener(mapController);
		add(bCancel);

		// komponenty pro editaci
		// tlacitko pro ukladani
		bSave = new JButton("Uložit");
                bSave.setToolTipText("Provedené změny budou uloženy s ohledem na vaše časové nastavení");
		bSave.setBounds(50, smallSpace, buttonSizeX, buttonSizeY);
		bSave.setActionCommand(ACTION_SAVE);
		bSave.addActionListener(mapController);
		add(bSave);

		// kombo pro vyber elementu
		String[] elements = {"Výskyt", "Trasa", "Území"};
		comboElements = new JComboBox(elements);
                comboElements.setToolTipText("<html>Zvolte požadovanou geometrickou entitu - výskyt (bod), trasa (čára) a území (polygon).<br>Bod entity vložíte do mapy kliknutím levého tlačítka myši.<br>Pro vložení další entity stejného druhu stiskněte pravé tlačítko myši.<html>");
		comboElements.setBounds(50 + buttonSizeX + smallSpace, smallSpace, 120, buttonSizeY);
		comboElements.setActionCommand(ACTION_CHANGE_TYPE);
		setDrawMode(MODE_POINT);
		comboElements.addActionListener(mapController);
		add(comboElements);

		setEditMode(false);
	}

	/**
	 * Prvnotni inicializace dat
	 */
	private void initData(){
		data = new LinkedList<JEntity>();
		insertData = new LinkedList<JEntity>();
		updateData = new LinkedList<JEntity>();
		deleteData = new LinkedList<JEntity>();
		tempDraw = new LinkedList<JEntity>();
		myPosition = new JEntity(DEFAULT_LAT, DEFAULT_LON);
	}

	/**
	 * Vycisteni pomocnych poli
	 */
	public void clearTempData(){
		insertData.clear();
		updateData.clear();
		deleteData.clear();
		tempDraw.clear();
	}


	/**
	 * Smaze z mapy vsechny data
	 */
	public void clearMap(){
		data.clear();
		clearTempData();
		repaint();
	}


	/**
	 * Detekce nejblizsiho bodu/entity
	 * @param clickedPoint
	 */
	public void detectHit(Point clickedPoint) {

		// pomocne promenne
		double minDistance = MAX_DISTANCE;
		double distance = 0;
		JEntity tempHit = null;
		hitEntity = null;

		// pro puvodni data
		for (JEntity entity : data) {
			distance = getMinDistance(entity, clickedPoint);

			// pokud je bliz, zvol tento
			if (distance < minDistance) {
				tempHit = entity;
				minDistance = distance;
			}
		}

		// pridana data
		for (JEntity entity : insertData) {
			distance = getMinDistance(entity, clickedPoint);

			// pokud je bliz, zvol tento
			if (distance < minDistance) {
				tempHit = entity;
				minDistance = distance;
			}
		}

		// zmenena data
		for (JEntity entity : updateData) {
			distance = getMinDistance(entity, clickedPoint);

			// pokud je bliz, zvol tento
			if (distance < minDistance) {
				tempHit = entity;
				minDistance = distance;
			}
		}

		// vyber nejblizsi
		if(tempHit != null){
			tempHit.setSelected(true);
			hitEntity = tempHit;
			repaint();
		}
	}

	/**
	 * Ziska vzdalenost nejblizsiho bodu dane entity k bodu. Na zaklade toho se
	 * pote vybere nejblizsi entita
	 *
	 * @param entity
	 * @param clickedPoint
	 * @return vzdalenost
	 */
	private double getMinDistance(JEntity entity, Point clickedPoint) {
		double distance = MAX_DISTANCE;
		double minDistance = MAX_DISTANCE;

		// predpokladejme ze neni hit
		entity.setSelected(false);

		// najdi nejblizsi bod jakekoliv entity
		switch (entity.getType()) {
		case JEntity.GTYPE_POINT:
			distance = entity.diffPoint(clickedPoint, this);
			Log.debug("testuju bod " + entity + " je vzdaleny " + distance);
			break;
		case JEntity.GTYPE_MULTIPOINT:
		case JEntity.GTYPE_CURVE:
		case JEntity.GTYPE_POLYGON:
			List<JEntity> points = JEntity.convert(entity.getOrdinatesArray());

			for (JEntity p : points) {
				distance = p.diffPoint(clickedPoint, this);
				if(distance < minDistance){
					minDistance = distance;
				}
			}
			distance = minDistance;
			break;
		case JEntity.GTYPE_MULTICURVE:
			List<JEntity> curves = JEntity.convertMulti(entity
					.getOrdinatesOfElements(), JEntity.GTYPE_MULTICURVE);
			for (JEntity curve : curves) {
				distance = getMinDistance(curve, clickedPoint);

				// pokud je bliz, zvol tento
				if (distance < minDistance) {
					minDistance = distance;
				}
			}
			distance = minDistance;
			break;
		case JEntity.GTYPE_MULTIPOLYGON:
			List<JEntity> polygons = JEntity.convertMulti(entity
					.getOrdinatesOfElements(), JEntity.GTYPE_MULTIPOLYGON);
			for (JEntity polygon : polygons) {
				distance = getMinDistance(polygon, clickedPoint);

				// pokud je bliz, zvol tento
				if (distance < minDistance) {
					minDistance = distance;
				}
			}
			distance = minDistance;
			break;
		}

		return distance;
	}

	/**
	 * Metoda disablujici tlacitka v mape
	 * @param enabled
	 */
	public void setEditButtonsEnabled(boolean enabled){
		bEdit.setEnabled(enabled);
		bSave.setEnabled(enabled);
		comboElements.setEnabled(enabled);
	}

	/**
	 * Metoda ktera naplni mapu z data z JGeometry
	 * @param data
	 */
	public void setMapData(List<JEntity> data){
		this.data = data;
		Log.debug("Nacteno " + data.size() + " geometrii");
		repaint();
	}

	/**
	 * Ukonci docasne kresleni a podle modu ulozi danou entitu do pole k ulozeni
	 */
	public void saveTempDraw() {

		if (tempDraw.size() > 0) {
			switch (drawMode) {
			case MODE_POINT:	// tohle by tu ciste teoreticky nemuselo byt
				break;
			case MODE_CURVE:
				JEntity curve = new JEntity(JEntity.createCurve(tempDraw));
				insertData.add(curve);
				tempDraw.clear();
				repaint();
				break;
			case MODE_POLYGON:
				JEntity polygon = new JEntity(JEntity.createPolygon(tempDraw));
				insertData.add(polygon);
				tempDraw.clear();
				repaint();
				break;
			}
		}
	}

	/**
	 * Pridavani bodu pri vkladani noveho elementu
	 * @param point
	 */
	public void addPoint(JEntity point){
		switch (drawMode) {
		case MODE_POINT: insertData.add(point);	break;
		case MODE_CURVE:
		case MODE_POLYGON:
			tempDraw.add(point);
			break;
		}

		repaint();
	}

	/**
	 * Presun polozky mezi upravene
	 * @param entity
	 */
	public void updateEntity(JEntity entity){
		data.remove(entity);
		insertData.remove(entity);
		updateData.add(entity);
		repaint();
	}

	/**
	 * Vlozeni entity do mazani
	 * @param entity
	 */
	public void deleteEntity(JEntity entity){
		data.remove(entity);
		insertData.remove(entity);
		updateData.remove(entity);
		deleteData.add(entity);

		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		// puvodni data
		for (JEntity entity : data) {
			paintEntity(g, entity);
		}

		// pokud je v editacnim modu, kresli i nove vykreslene body
		if (isEditMode()) {
			// nove vlozene
			for (JEntity entity : insertData) {
				paintEntity(g, entity);
			}

			// updatovane
			for (JEntity entity : updateData) {
				paintEntity(g, entity);
			}
		}

		// vykresli moji polohu
		paintMyPoint(g, myPosition);

		// vykresli docasne kresleni
		paintTemp(g);
	}

	/**
	 * Vyber jaka entita se ma kreslit
	 * @param g
	 * @param entity
	 */
	protected void paintEntity(Graphics g, JEntity entity){
		switch (entity.getType()) {
		case JEntity.GTYPE_POINT: paintPoint(g, entity); break;
		case JEntity.GTYPE_MULTIPOINT: paintMultiPoint(g, entity); break;
		case JEntity.GTYPE_CURVE: paintCurve(g, entity); break;
		case JEntity.GTYPE_MULTICURVE: paintMultiCurve(g, entity); break;
		case JEntity.GTYPE_POLYGON: paintPolygon(g, entity); break;
		case JEntity.GTYPE_MULTIPOLYGON: paintMultiPolygon(g, entity); break;
		}
	}

	/**
	 * Vykresleni bodu s moji pozici
	 * @param g
	 * @param myPoint
	 */
	protected void paintMyPoint(Graphics g, JEntity myPoint) {
		Graphics2D g2 = (Graphics2D) g;
		Point2D p = myPoint.getJavaPoint();
		Point mp = getMapPosition(p.getX(), p.getY(), false);

		g2.setColor(MY_POINT_COLOR);
		g2.fillOval(mp.x - MY_POINT_SIZE / 2, mp.y - MY_POINT_SIZE / 2,
				MY_POINT_SIZE, MY_POINT_SIZE);
	}

	/**
	 * Vykresleni docasne krivky
	 * @param g
	 */
	protected void paintTemp(Graphics g) {

		Graphics2D g2 = (Graphics2D) g;

		int i = 0;

		GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD,
				tempDraw.size());
		for (JEntity point : tempDraw) {
			Point p = getMapPosition(point.getLat(), point.getLon(), false);

			paintPoint(g, point);
			if(i == 0){
				path.moveTo(p.getX(), p.getY());
			}
			path.lineTo(p.getX(), p.getY());
			i++;
		}

		g2.setColor(CURVE_COLOR);
		g2.setStroke(stroke);
		g2.draw(path);
	}


	/**
	 * Vykresleni bodu
	 * @param g
	 * @param point
	 */
	protected void paintPoint(Graphics g, JEntity point){
		Graphics2D g2 = (Graphics2D) g;
		Point2D p = point.getJavaPoint();
		Point mp = getMapPosition(p.getX(), p.getY(), false);

		// pokud bod vybran
		if(point.isSelected()){
			g2.setColor(POINT_SELECTED_COLOR);
		}
		else {
			g2.setColor(POINT_COLOR);
		}
		g2.fillOval(mp.x - POINT_SIZE / 2, mp.y - POINT_SIZE / 2,
				POINT_SIZE, POINT_SIZE);
	}

	/**
	 * Vykresleni skupin bodu
	 * @param g
	 * @param points
	 */
	protected void paintMultiPoint(Graphics g, JEntity points) {

		List<JEntity> data = JEntity.convert(points.getOrdinatesArray());
		for (JEntity point : data) {
			point.setSelected(points.isSelected());	// pripadne oznaceni bodu
			paintPoint(g, point);
		}
	}

	/**
	 * Vykresleni krivky
	 * @param g
	 * @param curve
	 */
	protected void paintCurve(Graphics g, JEntity curve) {

		Graphics2D g2 = (Graphics2D) g;

		List<JEntity> points = JEntity.convert(curve.getOrdinatesArray());

		int i = 0;

		GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD,
				points.size());
		for (JEntity point : points) {
			Point p = getMapPosition(point.getLat(), point.getLon(), false);

			if(isEditMode()){
				point.setSelected(curve.isSelected());
				paintPoint(g, point);
			}
			if(i == 0){
				path.moveTo(p.getX(), p.getY());
			}
			path.lineTo(p.getX(), p.getY());
			i++;
		}

		g2.setColor(curve.isSelected()?CURVE_SELECTED_COLOR:CURVE_COLOR);
		g2.setStroke(stroke);
		g2.draw(path);
	}

	/**
	 * Vykresleni multicurve
	 * @param g
	 * @param curves
	 */
	protected void paintMultiCurve(Graphics g, JEntity curves) {

		List<JEntity> cs = curves.getCurves();

		for (JEntity c : cs) {
			c.setSelected(curves.isSelected());
			paintCurve(g, c);
		}
	}

	/**
         * Vykresleni polygonu
         * @param g
         * @param polygon
         */
	protected void paintPolygon(Graphics g, JEntity polygon) {
		Graphics2D g2 = (Graphics2D) g;

		List<JEntity> points = JEntity.convert(polygon.getOrdinatesArray());

		int i = 0;

		GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD,
				points.size());
		for (JEntity point : points) {
			Point p = getMapPosition(point.getLat(), point.getLon(), false);

			if(isEditMode()){
				point.setSelected(polygon.isSelected());
				paintPoint(g, point);
			}
			if(i == 0){
				path.moveTo(p.getX(), p.getY());
			}
			path.lineTo(p.getX(), p.getY());
			i++;
		}

		path.closePath();

		g2.setColor(polygon.isSelected()?CURVE_SELECTED_COLOR:CURVE_COLOR);

		Composite originComposite = g2.getComposite();
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				(float) 0.5));
		g2.fill(path);
		g2.setComposite(originComposite);
		g2.setStroke(stroke);
		g2.draw(path);
	}

	/**
	 * Vykresleni multipolygonu
	 * @param g
	 * @param polygons
	 */
	protected void paintMultiPolygon(Graphics g, JEntity polygons) {
		List<JEntity> ps = polygons.getPolygons();

		for (JEntity p : ps) {
			p.setSelected(polygons.isSelected());
			paintPolygon(g, p);
		}
	}

	public AnimalsDatabase getFrame() {
		return frame;
	}

	public JEntity getMyPosition() {
		return myPosition;
	}

	public void setMyPosition(JEntity myPosition) {
		this.myPosition = myPosition;

		// obnova zvirete
		frame.getAnimalsPanel().updateAnimalSpatialData();
		repaint();
	}

	public boolean isEditMode() {
		return editMode;
	}

	/**
	 * Metoda zobrazujici a schovavajici komponenty pro editaci elementu
	 * @param visible zda zobrazit ci nezobrazit
	 */
	public void setEditMode(boolean visible){

		setEditButtonsEnabled(true);

		// mod
		editMode = visible;

		// komponenty
		bEdit.setVisible(!visible);
		bCancel.setVisible(visible);
		bSave.setVisible(visible);
		comboElements.setVisible(visible);
	}

	public int getDrawMode() {
		return drawMode;
	}

	public void setDrawMode(int drawMode) {
		this.drawMode = drawMode;
		comboElements.setSelectedIndex(drawMode);
	}

	public List<JEntity> getData() {
		return data;
	}

	public void setData(List<JEntity> data) {
		this.data = data;
	}

	public List<JEntity> getInsertData() {
		return insertData;
	}

	public List<JEntity> getUpdateData() {
		return updateData;
	}

	public List<JEntity> getDeleteData() {
		return deleteData;
	}

	public void setTempData(List<JEntity> tempData) {
		insertData = tempData;
	}

	public JEntity getHitEntity() {
		return hitEntity;
	}
}

=======
package VEW.Scenario2; 

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

import VEW.Analyser4.BackgroundColour;

public class MapPanel extends JPanel implements Observer {
  private static BufferedImage MapImage = FileIO.FetchMapPicture(); // Res = 8640x2160
  private static BufferedImage MaskImage = FileIO.FetchMaskPicture(); // Res = 8640x2160
  private static final String IconRoot = "Data/Graphics/icons/";
  private boolean Locked = false;

  // Map Panel modes
  private static ScenarioPanel2 sp2 = null;
  public static final int AddPointMode = 0;
  public static final int ZoomInMode   = 2;
  public static final int ZoomOutMode  = 3;

  private boolean MouseIn = false;

  private int[] MapBoundaries = new int[4];

  private int currentMode = AddPointMode;

  private MapMonitor thisListener = new MapMonitor();
  private MapChangeNotifier thisNotifier = new MapChangeNotifier();

  private double Latitude = 90.00;
  private double Longitude = -180.00;

  private int StartDay;
  private int CollectedPoints = 0;

  private Point2D.Double StartPoint;

  private DecimalFormat TwoDecPlaces = new DecimalFormat("0.##");

  private TrackPosition[] GeneratedTrack;

  // Custom Mouse Cursors
  private static Cursor ZoomInCursor;
  private static Cursor ZoomOutCursor;
  private Cursor CrosshairCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);

  private GeneralPath VisualPath = new GeneralPath();
  //private ArrayList KeyPoints = new ArrayList();

  private Generator TrackGenerator;

  private boolean GoForward = true;
  
  public final static String MAP_WORLD = new String("World");
  public final static String MAP_NORTH_ATLANTIC = new String("North Atlantic");
  public final static String MAP_NEAR_GOOS = new String("NEAR GOOS");
  public final static String MAP_SEA_GOOS = new String("SEA GOOS");
  public final static String MAP_IO_GOOS = new String("IO GOOS");
  public final static String MAP_AFRICA_GOOS = new String("Africa GOOS");
  public final static String MAP_BLACK_SEA_GOOS = new String("Black Sea GOOS"); 
  public final static String MAP_EURO_GOOS = new String("Euro GOOS");
  public final static String MAP_MED_GOOS = new String("Med GOOS");
  public final static String MAP_PI_GOOS = new String("PI GOOS"); 
  public final static String MAP_IOCARIBE_GOOS = new String("IOCARIBE GOOS");
  public final static String MAP_US_GOOS = new String("US GOOS");
  public final static String MAP_GRASP = new String("GRASP"); 
  public final static String MAP_SW_TOP_ATLANTIC = new String("SW/Trop Atlantic");
  private static final GeneralPath ArrowHead, ArrowTail;
  
  public boolean useOverlay = false;
  static {
    ArrowHead = new GeneralPath();
    ArrowHead.moveTo(22.5f,12.5f);
    ArrowHead.lineTo(12.5f,10f);
    ArrowHead.lineTo(15f,12.5f);
    ArrowHead.lineTo(12.5f,15f);
    ArrowHead.closePath();
    ArrowTail = new GeneralPath();
    ArrowTail.moveTo(2.5f,12.5f);
    ArrowTail.lineTo(22.5f,12.5f);
  }

  
  public static void writeString(ZipOutputStream zos, String s) {
    final byte[] bytes = new byte[s.length()];
    for (int i=0; i<bytes.length; i++) bytes[i]=(byte)(s.charAt(i));
    try {
      zos.write(bytes);
    } catch (Exception e) { e.printStackTrace(); }
  }
      
  
  
  
  public static void showGoogleEarth(double[][] data, String tempDir, boolean mask) {
    // Create a .kmz file for GoogleEarth.
    // NB, As this file could get huge, I'm avoiding using the XML libraries.
    // I don't want this file built in RAM!
    
    double min=Double.POSITIVE_INFINITY; 
    double max=Double.NEGATIVE_INFINITY;
    for (int i=0; i<data.length; i++) {
      for (int j=0; j<data[i].length; j++) {
        if (data[i][j]>max) max=data[i][j];
        if (data[i][j]<min) min=data[i][j];
      }
    }
    
    try {
      ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempDir+File.separator+"vew.kmz"));
      zos.putNextEntry(new ZipEntry("vew.kml"));
      writeString(zos,"<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      writeString(zos,"<kml xmlns=\"http://earth.google.com/kml/2.0\">");
      writeString(zos,"<Document>");
      Color c;
      int lat,lon;
      for (int i=0; i<data.length; i++) {
        for (int j=0; j<data[i].length-1; j++) {
          if (i<180) lon=i; else lon=i-360;
          lat=90-j;
          
          final int col = (mask)?MaskImage.getRGB(i*12,j*12):-16777216;
          if (col==-16777216) {
            writeString(zos,"<Placemark>");
            writeString(zos,"<name>"+lon+","+lat+"</name>");
            writeString(zos,"<description>"+data[i][j]+"</description>");
            writeString(zos,"<Style><PolyStyle><outline>0</outline><color>ff");
            c = BackgroundColour.getColour(data[i][j],min,max);
            writeString(zos,Integer.toHexString(c.getBlue()));
            writeString(zos,Integer.toHexString(c.getGreen()));
            writeString(zos,Integer.toHexString(c.getRed()));
            writeString(zos,"</color></PolyStyle></Style><Polygon><extrude>0</extrude><altitudeMode>clampToGround</altitudeMode>");
            writeString(zos,"<outerBoundaryIs><LinearRing><coordinates>");
            writeString(zos,String.valueOf(lon-0.5)+","+String.valueOf(lat-0.5)+" ");
            writeString(zos,String.valueOf(lon+0.5)+","+String.valueOf(lat-0.5)+" ");
            writeString(zos,String.valueOf(lon+0.5)+","+String.valueOf(lat+0.5)+" ");
            writeString(zos,String.valueOf(lon-0.5)+","+String.valueOf(lat+0.5)+" ");
            writeString(zos,"</coordinates></LinearRing></outerBoundaryIs></Polygon></Placemark>");
          }
        }
      }
      writeString(zos,"</Document>");
      writeString(zos,"</kml>");
      zos.closeEntry();
      zos.flush();
      zos.close();
    } catch (Exception e) { e.printStackTrace(); }
  }
  
  public static void showGoogleEarthOCCAM(double[][] data, double[][] angle, String tempDir, boolean mask) {
    // Create a .kmz file for GoogleEarth.
    // NB, As this file could get huge, I'm avoiding using the XML libraries.
    // I don't want this file built in RAM!
    
    double min=Double.POSITIVE_INFINITY; 
    double max=Double.NEGATIVE_INFINITY;
    for (int i=0; i<data.length; i++) {
      for (int j=0; j<data[i].length; j++) {
        if (data[i][j]>max) max=data[i][j];
        if (data[i][j]<min) min=data[i][j];
      }
    }
    
    try {
      ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempDir+File.separator+"vew.kmz"));
      zos.putNextEntry(new ZipEntry("vew.kml"));
      writeString(zos,"<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      writeString(zos,"<kml xmlns=\"http://earth.google.com/kml/2.0\">");
      writeString(zos,"<Document>");
      double lat,lon;
      for (int i=0; i<data.length; i+=2) {
        for (int j=0; j<data[i].length; j+=2) {
          if (i<720) lon=i; else lon=i-1440.0;
          lat=90.0-(j/4.0);
          final int col = (mask)?MaskImage.getRGB((i*3),j*3):-16777216;
          if (col==-16777216) {
            double theAngle=angle[i][j];
            if ((!Double.isNaN(theAngle)) && (!Double.isInfinite(theAngle))) {
              writeString(zos,"<Placemark>");
              writeString(zos,"<name>"+lon+","+lat+"</name>");
              writeString(zos,"<description>"+data[i][j]+"</description>");
              writeString(zos,"<Style><PolyStyle><outline>0</outline><color>ff");
              final int lum = (int) (255-(((1.0*(data[i][j]-min))/((1.0*max-min)))*255.0));
              writeString(zos,Integer.toHexString(lum));
              writeString(zos,Integer.toHexString(lum));
              writeString(zos,Integer.toHexString(lum));
              writeString(zos,"</color></PolyStyle></Style>");
              writeString(zos,"<Polygon><altitudeMode>clampToGround</altitudeMode>");
              writeString(zos,"<outerBoundaryIs><LinearRing><coordinates>");
              final double tipX = (lon+(0.075*(Math.cos(theAngle))));
              final double tipY = (lat+(0.075*(Math.sin(theAngle))));
              final double leftX = (lon-(0.0375*(Math.cos(theAngle-0.4))));
              final double leftY = (lat-(0.0375*(Math.sin(theAngle-0.4))));
              final double rightX = (lon-(0.0375*(Math.cos(theAngle+0.4))));
              final double rightY = (lat-(0.0375*(Math.sin(theAngle+0.4))));
              writeString(zos,String.valueOf(tipX)+","+String.valueOf(tipY)+" ");
              writeString(zos,String.valueOf(leftX)+","+String.valueOf(leftY)+" ");
              writeString(zos,String.valueOf(rightX)+","+String.valueOf(rightY)+" ");
              writeString(zos,String.valueOf(tipX)+","+String.valueOf(tipY)+" ");
              writeString(zos,"</coordinates></LinearRing></outerBoundaryIs>");
              writeString(zos,"</Polygon></Placemark>");
            }
          }
        }
      }
      writeString(zos,"</Document>");
      writeString(zos,"</kml>");
      zos.closeEntry();
      zos.flush();
      zos.close();
    } catch (Exception e) { e.printStackTrace(); }
  }

  public static void showGoogleEarth(double[][] data, double[][] angle, String tempDir, boolean mask) {
    // Create a .kmz file for GoogleEarth.
    // NB, As this file could get huge, I'm avoiding using the XML libraries.
    // I don't want this file built in RAM!

    double min=Double.POSITIVE_INFINITY; 
    double max=Double.NEGATIVE_INFINITY;
    for (int i=0; i<data.length; i++) {
      for (int j=0; j<data[i].length; j++) {
        if (data[i][j]>max) max=data[i][j];
        if (data[i][j]<min) min=data[i][j];
      }
    }
    
    try {
      ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempDir+File.separator+"vew.kmz"));
      zos.putNextEntry(new ZipEntry("vew.kml"));
      writeString(zos,"<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      writeString(zos,"<kml xmlns=\"http://earth.google.com/kml/2.0\">");
      writeString(zos,"<Document>");
      
      double lat,lon;
      for (int i=0; i<data.length; i++) {
        for (int j=0; j<data[i].length; j++) {
          if (i<180) lon=i; else lon=i-360.0;
          lat=90.0-j;
          final int col = (mask)?MaskImage.getRGB(i*12,j*12):-16777216;
          if (col==-16777216) {
            writeString(zos,"<Placemark>");
            writeString(zos,"<name>"+lon+","+lat+"</name>");
            writeString(zos,"<description>"+data[i][j]+"</description>");
            writeString(zos,"<Style><PolyStyle><outline>0</outline><color>ff");
            final int lum = (int) (255-(((1.0*(data[i][j]-min))/((1.0*max-min)))*255.0));
            writeString(zos,Integer.toHexString(lum));
            writeString(zos,Integer.toHexString(lum));
            writeString(zos,Integer.toHexString(lum));
            
            writeString(zos,"</color></PolyStyle></Style>");
            writeString(zos,"<Polygon><altitudeMode>clampToGround</altitudeMode>");
            writeString(zos,"<outerBoundaryIs><LinearRing><coordinates>");
            double theAngle=angle[i][j];
            if (Double.isNaN(theAngle)) theAngle=0;
            final double tipX = (lon+(0.45*(Math.cos(theAngle))));
            final double tipY = (lat+(0.45*(Math.sin(theAngle))));
            final double leftX = (lon-(0.25*(Math.cos(theAngle-0.4))));
            final double leftY = (lat-(0.25*(Math.sin(theAngle-0.4))));
            final double rightX = (lon-(0.25*(Math.cos(theAngle+0.4))));
            final double rightY = (lat-(0.25*(Math.sin(theAngle+0.4))));
            writeString(zos,String.valueOf(tipX)+","+String.valueOf(tipY)+" ");
            writeString(zos,String.valueOf(leftX)+","+String.valueOf(leftY)+" ");
            writeString(zos,String.valueOf(rightX)+","+String.valueOf(rightY)+" ");
            writeString(zos,String.valueOf(tipX)+","+String.valueOf(tipY)+" ");
            writeString(zos,"</coordinates></LinearRing></outerBoundaryIs>");
            writeString(zos,"</Polygon></Placemark>");
          }
        }
      }
      writeString(zos,"</Document>");
      writeString(zos,"</kml>");
      zos.closeEntry();
      zos.flush();
      zos.close();
    } catch (Exception e) { e.printStackTrace(); }
  }

  
  
  
  public ArrayList setOverlay(double[][] data, boolean mask) {
    ArrayList a = new ArrayList();
    useOverlay=(data!=null);
    MapImage = FileIO.FetchMapPicture();
    if (useOverlay) {
      double min=Double.POSITIVE_INFINITY; 
      double max=Double.NEGATIVE_INFINITY;
      for (int i=0; i<data.length; i++) {
        for (int j=0; j<data[i].length; j++) {
          if (data[i][j]>max) max=data[i][j];
          if (data[i][j]<min) min=data[i][j];
        }
      }
      a.add(new Double(max));
      a.add(new Double(min));
      int iCoord,jCoord;
      Color c;
      int width = MapImage.getWidth();
      int height = MapImage.getHeight();
      for (int i=0; i<width; i++) { 
        for (int j=0; j<height; j++) {
          if (i<4320) iCoord=(i/12);
          else iCoord=(i-4320)/12;
          jCoord = j/12;
          c = BackgroundColour.getColour(data[iCoord][jCoord],min,max);
          if (mask) {
            int col = MaskImage.getRGB(i,j);
            if (col>-16777216) 
            c = new Color(col);
          }
          MapImage.setRGB(i,j,c.getRGB());
        }
      }
    }
    return a;
  }
  
  public ArrayList setOverlay(double[][] mag, double[][] angle, boolean mask) {
    ArrayList a = new ArrayList();
    useOverlay=(mag!=null);
    MapImage = FileIO.FetchMapPicture();
    Graphics g = MapImage.getGraphics();
    if (useOverlay) {
      double min=Double.POSITIVE_INFINITY; 
      double max=Double.NEGATIVE_INFINITY;
      for (int i=0; i<mag.length; i++) {
        for (int j=0; j<mag[i].length; j++) {
          if (mag[i][j]>max) max=mag[i][j];
          if (mag[i][j]<min) min=mag[i][j];
        }
      }
      a.add(new Double(max));
      a.add(new Double(min));
      int iCoord,jCoord;
      int width = MapImage.getWidth();
      int height = MapImage.getHeight();
      for (int i=0; i<width; i++) { 
        for (int j=0; j<height; j++) {
          if (i<4320) iCoord=(i/12);
          else iCoord=(i-4320)/12;
          jCoord = j/12;
          MapImage.setRGB(i,j,BackgroundColour.getColour(mag[iCoord][jCoord],min,max).getRGB());
        }
      }
      g.setColor(Color.black);
      int baseX,baseY,tipX,tipY,leftX,leftY,rightX,rightY;
      double theAngle = 0;
      int centreX,centreY;
      for (int i=0; i<width; i+=12) { 
        for (int j=0; j<height; j+=12) {
          if (i<4320) iCoord=(i/12);
          else iCoord=(i-4320)/12;
          jCoord = j/12;
          centreX=i+6;
          centreY=j+6;
          theAngle=angle[iCoord][jCoord];
          
          baseX = (int) (centreX-(5*(Math.cos(theAngle))));
          baseY = (int) (centreY-(5*(Math.sin(theAngle))));
          tipX = (int) (centreX+(5*(Math.cos(theAngle))));
          tipY = (int) (centreY+(5*(Math.sin(theAngle))));
          leftX = (int) (centreX+(5*(Math.cos(1.3+theAngle))));
          leftY = (int) (centreY+(5*(Math.sin(1.3+theAngle))));
          rightX = (int) (centreX+(5*(Math.cos(theAngle-1.3))));
          rightY = (int) (centreY+(5*(Math.sin(theAngle-1.3))));
          
          g.drawLine(baseX,baseY,tipX,tipY);
          g.fillPolygon(new int[] {leftX,tipX,rightX}, new int[] {leftY,tipY,rightY},3);
        }
      }
      if (mask) {
        for (int i=0; i<width; i++) { 
          for (int j=0; j<height; j++) {
            if (i<4320) iCoord=(i/12);
            else iCoord=(i-4320)/12;
            jCoord = j/12;
            int col = MaskImage.getRGB(i,j);
            if (col>-16777216) { 
              Color c = new Color(col);
              MapImage.setRGB(i,j,c.getRGB());
            }
          }
        }
      }
    }
    return a;
  }
  
  public ArrayList setOccamOverlay(double[][] mag, double[][] angle, boolean mask) {
    ArrayList a = new ArrayList();
    useOverlay=(mag!=null);
    MapImage = FileIO.FetchMapPicture();
    Graphics g = MapImage.getGraphics();
    if (useOverlay) {
      double min=Double.POSITIVE_INFINITY; 
      double max=Double.NEGATIVE_INFINITY;
      for (int i=0; i<mag.length; i++) {
        for (int j=0; j<mag[i].length; j++) {
          if (!Double.isInfinite(mag[i][j])) {
            if (mag[i][j]>max) max=mag[i][j];
            if (mag[i][j]<min) min=mag[i][j];
          }
        }
      }
      a.add(new Double(max));
      a.add(new Double(min));
      int iCoord,jCoord;
      int width = MapImage.getWidth();
      int height = MapImage.getHeight();
      for (int i=0; i<width; i++) { 
        for (int j=0; j<height; j++) {
          if (i<4320) iCoord=(i/3);
          else iCoord=(i-4320)/3;
          jCoord = j/3;
          MapImage.setRGB(i,j,BackgroundColour.getColour(mag[iCoord][jCoord],min,max).getRGB());
        }
      }
      g.setColor(Color.black);
      int baseX,baseY,tipX,tipY;
      double theAngle = 0;
      int centreX,centreY;
      for (int i=0; i<width; i+=3) { 
        for (int j=0; j<height; j+=3) {
          if (i<4320) iCoord=(i/3);
          else iCoord=(i-4320)/3;
          jCoord = j/3;
          centreX=i+1;
          centreY=j+1;
          theAngle=angle[iCoord][jCoord];
          
          baseX = (int) (centreX-(1*(Math.cos(theAngle))));
          baseY = (int) (centreY-(1*(Math.sin(theAngle))));
          tipX = (int) (centreX+(1*(Math.cos(theAngle))));
          tipY = (int) (centreY+(1*(Math.sin(theAngle))));
          g.setColor(Color.black);
          g.drawLine(baseX,baseY,tipX,tipY);
          g.setColor(Color.RED);
          g.drawLine(tipX,tipY,tipX,tipY);
        }
      }
      if (mask) {
        for (int i=0; i<width; i++) { 
          for (int j=0; j<height; j++) {
            if (i<4320) iCoord=(i/12);
            else iCoord=(i-4320)/12;
            jCoord = j/12;
            int col = MaskImage.getRGB(i,j);
            if (col>-16777216) { 
              Color c = new Color(col);
              MapImage.setRGB(i,j,c.getRGB());
            }
          }
        }
      }
    }
    return a;
  }
  
    

  public void drawArrow(BufferedImage map, int i, int j, double Angle) {
    AffineTransform ScaleAndRotate = AffineTransform.getScaleInstance(1, 1);
    ScaleAndRotate.rotate(Angle,12.5,12.5);
    GeneralPath AngledHead = (GeneralPath)ArrowHead.clone();
    GeneralPath AngledTail = (GeneralPath)ArrowTail.clone();
    AngledHead.transform(ScaleAndRotate);
    AngledTail.transform(ScaleAndRotate);
    AffineTransform location = AffineTransform.getTranslateInstance(i,j);
    AngledHead.transform(location);
    AngledTail.transform(location);
    Graphics2D drawer = (Graphics2D)map.getGraphics();
    drawer.setColor(Color.black);
    drawer.draw(AngledTail);
    drawer.fill(AngledHead);
  }

  
  public static void addMaps(JComboBox j) {
    j.addItem(MAP_WORLD);
    j.addItem(MAP_NORTH_ATLANTIC);
    j.addItem(MAP_NEAR_GOOS);
    j.addItem(MAP_SEA_GOOS);
    j.addItem(MAP_IO_GOOS);
    j.addItem(MAP_AFRICA_GOOS);
    j.addItem(MAP_BLACK_SEA_GOOS);
    j.addItem(MAP_EURO_GOOS);
    j.addItem(MAP_MED_GOOS);
    j.addItem(MAP_PI_GOOS);
    j.addItem(MAP_IOCARIBE_GOOS);
    j.addItem(MAP_US_GOOS);
    j.addItem(MAP_GRASP);
    j.addItem(MAP_SW_TOP_ATLANTIC);
  }
  
  public MapPanel(ScenarioPanel2 _sp2) {
    setDoubleBuffered(true);
    sp2=_sp2;
    initialiseCursors();
    setCursor(CrosshairCursor);
    this.addMouseListener(thisListener);
    this.addMouseMotionListener(thisListener);
    changeViewWindow(180, 90, 360, 180);
  }

  
  public void changeViewWindow(int W, int N, int Width, int Height) {
    MapBoundaries[0] = 360 - W;
    while (MapBoundaries[0] > 540) MapBoundaries[0] -= 360;
    while (MapBoundaries[0] < 0) MapBoundaries[0] += 360;
    MapBoundaries[1] = 90 - N;
    if (MapBoundaries[1] < 0) MapBoundaries[1] = 0;
    MapBoundaries[2] = Width;
    if (MapBoundaries[2] > 360) MapBoundaries[2] = 360;
    if (MapBoundaries[2] + MapBoundaries[0] > 720) MapBoundaries[0] -= 360;
    MapBoundaries[3] = Height;
    if (MapBoundaries[3] > 180) MapBoundaries[3] = 180;
    if (MapBoundaries[3] + MapBoundaries[1] > 180) MapBoundaries[1] = 180 - MapBoundaries[3];
    for (int i = 0; i < 4; i++) MapBoundaries[i] *= 12;
   
  }

  public void changeViewCoords(int N, int S, int E, int W) {
    int[] TempArray = ConvertCoordsToBounds(N,S,W,E);
    for(int i = 0; i < 4; i++) MapBoundaries[i] = TempArray[i];
    
  }
  
  public static int[] ConvertCoordsToBounds(int N, int S, int W, int E) {
    int[] TempInt = new int[4];
    TempInt[0] = 360 - W;
    TempInt[1] = 90 - N;
    TempInt[2] = W + E;
    TempInt[3] = N + S;
    if (TempInt[2] < 0) TempInt[2] += 360;
    for (int i = 0; i < 4; i++) TempInt[i] *= 12;
    return TempInt;
  }

  public void setLock(boolean LockStatus) {
    Locked = LockStatus;
  }

   public void addObserver(Observer ob) {
      thisNotifier.addObserver(ob);
   }

  public void GoForth(boolean _GoForward) {
    GoForward = _GoForward;
    repaint();
  }

   public void GenerateTrack(int StartMonth, int _StartDay, int RunDuration, String IntegrationMethod, String VelocityName) {
     GoForward = (!IntegrationMethod.equals(Generator.INT_BWD));
     GeneratedTrack = new TrackPosition[RunDuration + 1];
     StartDay = _StartDay;
     if ((IntegrationMethod.equals(Generator.INT_FIX)) || (FileIO.checkForExistence("Velocities"+File.separator+Generator.velFiles[Generator.findVelocity(VelocityName)]))) {
       TrackGenerator = new Generator(StartMonth, StartDay, IntegrationMethod, RunDuration+1, new TrackPosition(StartPoint.getY(), StartPoint.getX()), VelocityName);
       TrackGenerator.addObserver(this);
       TrackGenerator.start();
     } else {
       sp2.generateButton.setEnabled(true);
       sp2.setLongitude.setEnabled(true);
       sp2.setLatitude.setEnabled(true);
       GeneratedTrack = null;
       setLock(false);
     }
   }

   public TrackPosition[] getGeneratedTrack() {
      return GeneratedTrack;
   }

   public static Point2D.Double GridToLatLon(Point gridPoint, int[] MapBounds, Rectangle2D.Double DisplayBounds) {
      double lat = 1080 - MapBounds[1];
      lat -= (gridPoint.getY() - DisplayBounds.y) * MapBounds[3] / DisplayBounds.height;
      lat /= 12.0;
      double lon = MapBounds[0];
      lon += (gridPoint.getX() - DisplayBounds.x) * MapBounds[2] / DisplayBounds.width;
      lon /= 12.0;
      if(lon > 180)  lon -= 360;
      return new Point2D.Double(-1 * lon, lat);
   }

   public static Point2D.Double LatLonToGrid(Point2D.Double latLonPoint, int[] MapBounds, Rectangle2D.Double DisplayBounds) {

     double x = (360 - latLonPoint.getX()) * 12;
     while (x < MapBounds[0]) x += 4320;
     x -= MapBounds[0];
     x *= DisplayBounds.width;
     x /= MapBounds[2];
     x += DisplayBounds.x;

     double y = 1080 - MapBounds[1];
     y -= (latLonPoint.getY() * 12);
     y *= DisplayBounds.height;
     y /= MapBounds[3];
     y += DisplayBounds.y;
     return new Point2D.Double(x, y);
   }

   public double getLatitude() { return Latitude; }
   public double getLongitude() { return Longitude; }
   public String getLatitudeString() { return MouseIn ? TwoDecPlaces.format(Latitude) : "XXX"; }
   public String getLongitudeString() { return MouseIn ? TwoDecPlaces.format(Longitude) : "XXX"; }

   public void setMode(int newMode) {
     if (currentMode != newMode) {
       currentMode = newMode;
       if (currentMode==AddPointMode) setCursor(CrosshairCursor);
       else if (currentMode==ZoomInMode) setCursor(ZoomInCursor);
       else if (currentMode==ZoomOutMode) setCursor(ZoomOutCursor);
     }
   }

   public void addPoint(double latitude, double longitude) {
     StartPoint = new Point2D.Double(latitude, longitude);
     repaint();
     thisNotifier.NotifyPointSet();
   }

   public void removeAllPoints() {
     StartPoint = null;
     VisualPath.reset();
     GeneratedTrack = null;
     CollectedPoints = 0;
     repaint();
   }

   protected void initialiseCursors() {
     Toolkit defaultKit = Toolkit.getDefaultToolkit();
     try {
        ZoomInCursor = defaultKit.createCustomCursor(ImageIO.read(new File(IconRoot + "zoomin.gif")), new Point(5, 5), "ZoomIn");
        ZoomOutCursor = defaultKit.createCustomCursor(ImageIO.read(new File(IconRoot + "zoomout.gif")), new Point(5, 5), "ZoomOut");
     } catch(Exception e) {}
   }
   
  public static void paintMapTrack(Graphics2D outputGraphics, int[] MapBounds, TrackPosition[] Track, Rectangle2D.Double DisplayBounds, boolean GoForward, int CollectedPoints, int StartDay, int StartYear) {
    //Rectangle2D.Double nextToFill = new Rectangle2D.Double(0, 0, 1,1);
    // Draw the map.
    BufferedImage ImageToShow = MapImage.getSubimage(MapBounds[0], MapBounds[1], MapBounds[2], MapBounds[3]);
    outputGraphics.drawImage(ImageToShow, (int)DisplayBounds.x, (int)DisplayBounds.y, (int)DisplayBounds.width, (int)DisplayBounds.height, null);
    // Draw the track.
    int DayMover = GoForward ? 1 : -1;
    //CollectedPoints = GeneratedTrack.length);
    GeneralPath TempPath = new GeneralPath();
    if (Track[0]!=null) {
      Point2D.Double LatLonPoint = LatLonToGrid(Track[0].getCoordinates(), MapBounds, DisplayBounds);
      double LastX = LatLonPoint.getX();
      double Distance = 0;
      TempPath.moveTo((float)LatLonPoint.getX(), (float)LatLonPoint.getY());
      for(int i = 1; i < CollectedPoints; i++) {
        LatLonPoint = LatLonToGrid(Track[i].getCoordinates(), MapBounds, DisplayBounds);
        Distance = Math.abs(LatLonPoint.getX() - LastX);
        if (Distance < 0) Distance *= -1;
        if (Distance >= DisplayBounds.width - 1) TempPath.moveTo((float)LatLonPoint.getX(), (float)LatLonPoint.getY());
        else TempPath.lineTo((float)LatLonPoint.getX(), (float)LatLonPoint.getY());
        LastX = LatLonPoint.getX();
      }
    
      outputGraphics.setColor(Color.black);
      outputGraphics.draw(TempPath);
      outputGraphics.setColor(Color.orange);
      int DayOfYear = StartDay - DayMover;
      int TrackPointer = 0;
    // Mark the intermediate years
      while(TrackPointer < CollectedPoints) {
        DayOfYear += DayMover;
        TrackPointer++;
        if (DayOfYear==366) DayOfYear = 1;
        else if (DayOfYear==0) DayOfYear=365;
        if (DayOfYear == 1 && TrackPointer < (Track.length - 1) && TrackPointer != 1) {
          LatLonPoint = LatLonToGrid(Track[TrackPointer].getCoordinates(), MapBounds, DisplayBounds);
          markPoint(outputGraphics, LatLonPoint, 2);
        }
      }
      // Mark the first track point
      outputGraphics.setColor(GoForward ? Color.yellow : Color.red);
      LatLonPoint = LatLonToGrid(Track[0].getCoordinates(), MapBounds, DisplayBounds);
      markPoint(outputGraphics, LatLonPoint, 4);
      if(CollectedPoints == Track.length) { // If we have it, mark the last track point
        outputGraphics.setColor(GoForward ? Color.red : Color.yellow);
        TrackPointer--;
        LatLonPoint = LatLonToGrid(Track[TrackPointer].getCoordinates(), MapBounds, DisplayBounds);
        markPoint(outputGraphics, LatLonPoint, 4);
      }
    }
  }

     
  protected void paintComponent(Graphics g) {
    Graphics2D g2D = (Graphics2D)g;
    Rectangle2D.Double DisplayBounds = new Rectangle2D.Double(0, 0, getWidth(), getHeight());
    if(GeneratedTrack != null && CollectedPoints > 0)
      paintMapTrack(g2D, MapBoundaries, GeneratedTrack, DisplayBounds, GoForward, CollectedPoints, StartDay, 2000);//StartYear);
    else if(StartPoint != null) { // Mark start point.
      BufferedImage ImageToShow = MapImage.getSubimage(MapBoundaries[0], MapBoundaries[1], MapBoundaries[2], MapBoundaries[3]);
      g2D.drawImage(ImageToShow,0,0,getWidth(),getHeight(),null);
      Point2D.Double LatLonPoint = LatLonToGrid(StartPoint, MapBoundaries, DisplayBounds);
      g2D.setColor(GoForward ? Color.yellow : Color.red);
      markPoint(g2D, LatLonPoint, 4);
    } else {
      BufferedImage ImageToShow = MapImage.getSubimage(MapBoundaries[0], MapBoundaries[1], MapBoundaries[2], MapBoundaries[3]);
      g2D.drawImage(ImageToShow,0,0,getWidth(),getHeight(),null);
    }
  }

  private static void markPoint(Graphics2D g2D, Point2D.Double Coords, int Size) {
    double X = Coords.getX();
    double Y = Coords.getY();
    g2D.fill(new Rectangle2D.Double(X - Size, Y, 1 + (2 * Size), 1));
    g2D.fill(new Rectangle2D.Double(X, Y - Size, 1, 1 + (2 * Size)));
    
  }

   public void changeRegion(String region) {
    if (region.equals(MAP_WORLD)) changeViewCoords(90,90,180,180);
    else if (region.equals(MAP_NORTH_ATLANTIC)) changeViewCoords(75,15,15,100);
    else if (region.equals(MAP_NEAR_GOOS)) changeViewCoords(56,-20,173,-115);
    else if (region.equals(MAP_SEA_GOOS)) changeViewCoords(21,17,147,-92);
    else if (region.equals(MAP_IO_GOOS)) changeViewCoords(19,54,105,-35);
    else if (region.equals(MAP_AFRICA_GOOS)) changeViewCoords(32,49,51,23);
    else if (region.equals(MAP_BLACK_SEA_GOOS)) changeViewCoords(47,-35,49,-30);
    else if (region.equals(MAP_EURO_GOOS)) changeViewCoords(90,-37,59,34);
    else if (region.equals(MAP_MED_GOOS)) changeViewCoords(46,-30,32,13);
    else if (region.equals(MAP_PI_GOOS)) changeViewCoords(5,50,-149,-143);
    else if (region.equals(MAP_IOCARIBE_GOOS)) changeViewCoords(27,9,-32,103);
    else if (region.equals(MAP_US_GOOS)) changeViewCoords(72,-25,-42,143);
    else if (region.equals(MAP_GRASP)) changeViewCoords(-10,75,-63,100);
    else if (region.equals(MAP_SW_TOP_ATLANTIC)) changeViewCoords(-10,65,-29,77);
    repaint();
  }
 
  // Has to be implemented for the Observer interface
  public void update(Observable o, Object arg) {
    if (arg != null) {
      if(arg.getClass().getName().equals("VEW.Scenario.TrackPosition")) {
        GeneratedTrack[CollectedPoints] = (TrackPosition)arg;
        CollectedPoints++;
        repaint();
      } else if (arg.getClass().getName().equals("java.lang.Integer")) {
        Number CodeNumber = (Number)arg;
        if(CodeNumber.intValue() == 0) { // Run Aborted
          GeneratedTrack = null;
          CollectedPoints = 0;
          repaint();
          thisNotifier.sendCode(0);
          
        } else if (CodeNumber.intValue() == 1) { // Run Completed
          sp2.generateButton.setEnabled(true);
          sp2.setLongitude.setEnabled(true);
          sp2.setLatitude.setEnabled(true);
          setLock(false);
          GeneratedTrack = TrackGenerator.getGeneratedTrack(false);
          if (GeneratedTrack != null) CollectedPoints = GeneratedTrack.length;
          GoForward = true;
          repaint();
          thisNotifier.sendCode(1);
        }
      }
    }
  }

  public void abortGeneration() { TrackGenerator.abort(); }

  private class MapChangeNotifier extends Observable {
    public MapChangeNotifier() { super(); }

    public void makeDirtyAndNotify() {
      setChanged();
      notifyObservers();
    }

    public void NotifyPointSet() {
      setChanged();
      notifyObservers(StartPoint);
    }

    public void sendCode(int CodeNumber) {
      setChanged();
      notifyObservers(new Integer(CodeNumber));
    }
  }

  private class MapMonitor extends MouseInputAdapter {
    public MapMonitor() { super(); }

    public void mouseClicked(MouseEvent e) {
      if (currentMode == AddPointMode) {
        if (!Locked) {
          removeAllPoints();          
          StartPoint = GridToLatLon(e.getPoint(), MapBoundaries, new Rectangle2D.Double(0, 0, getWidth(), getHeight()));// = new Point2D.Double(e.getX(), e.getY());
          thisNotifier.NotifyPointSet();
          repaint();
        }
      }
      else if (currentMode == ZoomInMode) {
        Point2D.Double centerPoint = GridToLatLon(e.getPoint(), MapBoundaries, new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
        int Y = (int)centerPoint.getY();
        int X = (int)centerPoint.getX();
        int newWidth  = MapBoundaries[2] / 36;
        int newHeight = MapBoundaries[3] / 36;
        if (newWidth < 15) newWidth = 15;
        if (newHeight < 15) newHeight = 15;
        changeViewWindow(X + (newWidth / 2), Y + (newHeight / 2), newWidth, newHeight);
        repaint();
      } else if (currentMode == ZoomOutMode) {
        Point2D.Double centerPoint = GridToLatLon(e.getPoint(), MapBoundaries, new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
        int Y = (int)centerPoint.getY();
        int X = (int)centerPoint.getX();
        int newWidth  = MapBoundaries[2] / 8;
        if(newWidth > 360) newWidth = 360;
        int newHeight = MapBoundaries[3] / 8;
        changeViewWindow(X + (newWidth / 2), Y + (newHeight / 2), newWidth, newHeight);
        repaint();
      }
    }

    public void mouseMoved(MouseEvent e) {
      Point2D.Double convE = GridToLatLon(e.getPoint(), MapBoundaries, new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
      Longitude = convE.getX();
      Latitude = convE.getY();
      MouseIn = true;
      thisNotifier.makeDirtyAndNotify();
    }

    public void mouseExited(MouseEvent e) {
      MouseIn = false;
      thisNotifier.makeDirtyAndNotify();
    }
  }
}
>>>>>>> 76aa07461566a5976980e6696204781271955163
