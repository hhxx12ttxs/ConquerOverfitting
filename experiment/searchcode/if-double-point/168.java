<<<<<<< HEAD
package ncsa.d2k.modules.core.optimize.random;



import ncsa.d2k.modules.core.datatype.parameter.*;
import ncsa.d2k.modules.core.datatype.parameter.impl.*;
import ncsa.d2k.modules.core.datatype.table.basic.*;
import ncsa.d2k.modules.core.datatype.table.*;
import java.util.Random;
import java.util.ArrayList;
import ncsa.d2k.core.modules.ComputeModule;
import ncsa.d2k.core.modules.PropertyDescription;
import java.beans.PropertyVetoException;

public class RandomSampling extends ComputeModule {

	/** these are the paramter points to test. */
	int pointsPushed = 0;;

	/** these are the scored paramter points. */
	ArrayList examples;

	public PropertyDescription[] getPropertiesDescriptions () {
		PropertyDescription[] descriptions = new PropertyDescription[7];
		descriptions[0] = new PropertyDescription (
				"minimize",
				"Minimize Objective Score",
				"Set to true if the objective score should be minimize, true if it should be maximized.");
		descriptions[1] = new PropertyDescription (
				"threashhold",
				"Objective Threashhold Value",
				"Stop optimization when this threashhold value is reached.");
		descriptions[2] = new PropertyDescription (
				"maxIterations",
				"Maximum Number of Iterations",
				"Optimization halts when this limit on the number of iterations is exceeded.  ");
		descriptions[3] = new PropertyDescription (
				"seed",
				"Random Number Seed",
				"This integer is use to seed the random number generator which is used to select points in parameter space.");
		descriptions[4] = new PropertyDescription (
				"trace",
				"Trace",
				"Report each scored point in parameter space as it becomes available.");
		descriptions[5] = new PropertyDescription (
				"verbose",
				"Verbose Output",
				"Report each scored point in parameter space as it becomes available, and each parameter point that has been pushed.");
		descriptions[6] = new PropertyDescription (
				"useResolution",
				"Constrain Resolution",
				"If this parameter is set, we will use the resolution defined in the paramter space value for each paramter to define the number of distinct values in the ranges.");

		return descriptions;
	}

	private boolean minimizing = true;
	public void setMinimize (boolean value) {
		minimizing = value;
	}
	public boolean getMinimize () {
		return this.minimizing;
	}

	private double threashhold = 0.0;
	public void setThreashhold (double value) {
		this.threashhold = value;
	}
	public double getThreashhold () {
		return this.threashhold;
	}

	private int maxIterations = 100;
	public void setMaxIterations (int value) throws PropertyVetoException {
		if (value < 1) {
			throw new PropertyVetoException (" < 1", null);
		}
		this.maxIterations = value;
	}
	public int getMaxIterations () {
		return this.maxIterations;
	}

	private int seed = 1;
	public void setSeed (int value) {
		this.seed = value;
	}
	public int getSeed () {
		return this.seed;
	}

	private boolean trace = false;
	public void setTrace (boolean value) {
		this.trace = value;
	}
	public boolean getTrace () {
		return this.trace;
	}

	private boolean verbose = false;
	public void setVerbose (boolean value) {
		this.verbose = value;
	}
	public boolean getVerbose () {
		return this.verbose;
	}

	private boolean useresolution = false;

	public void setUseResolution (boolean value) {
		this.useresolution = value;
	}

	public boolean getUseResolution () {
		return this.useresolution;
	}

	public String getModuleName () {
		return "Random Sample";
	}

	public String getModuleInfo () {
		return "<p>      Overview: Generate random points in a space defined by a parameter space       input"+
			" until we push a user defined maximum number of points, we we       converge to a user defined"+
			" optima.    </p>    <p>      Detailed Description: This module will produce <i>Maximum Number"+
			" of       Iterations</i> points in parameter space, unless it converges before       generating"+
			" that many points. It will produce only one point per       invocation, unless it has already"+
			" produced all the points it is going to       and it is just waiting for scored points to come"+
			" back. This module will       not wait for a scored point to come back before producing the"+
			" next one,       it will produce as many poiints as it can, and it will remain enabled     "+
			"  until all those points are produced, or it has converged. The module       converges if a"+
			" score exceeds the property named <i>Objective Threashhold</i>. The Random Seed can be set to"+
			" a positive value to cause this module to       produce the same points, given the same inputs,"+
			" on multiple runs. <i>      Trace</i> and <i>Verbose Output</i> properties can be set to produce"+
			" a       little or a lot of console output respectively. If <i>Constrain       Resolution</i>"+
			" is not set, the resolution value from the parameter space       object will be ignored. We"+
			" can minimize the objective score by setting       the <i>Minimize Objective Score</i> property"+
			" to true.    </p>";
	}

	public String getInputName (int i) {
		switch(i) {
			case 0:
				return "Control Parameter Space";
			case 1:
				return "Example";
			default: return "NO SUCH INPUT!";
		}
	}

	public String getInputInfo (int i) {
		switch (i) {
			case 0: return "The Control Parameter Space to search";
			case 1: return "The scored parameter point from a previously pushed point in parameter     space.";
			default: return "No such input";
		}
	}

	public String[] getInputTypes () {
		String[] types = {"ncsa.d2k.modules.core.datatype.parameter.ParameterSpace","ncsa.d2k.modules.core.datatype.table.Example"};
		return types;
	}

	public String getOutputName (int i) {
		switch(i) {
			case 0:
				return "Parameter Point";
			case 1:
				return "Optimal Example Table";
			case 2:
				return "Complete Example Table";
			default: return "NO SUCH OUTPUT!";
		}
	}

	public String getOutputInfo (int i) {
		switch (i) {
			case 0: return "The next Parameter Point selected for evaluation";
			case 1: return "An example table consisting of only the Optimal Example(s)";
			case 2: return "An example table consisting of all Examples generated during optimization";
			default: return "No such output";
		}
	}

	public String[] getOutputTypes () {
		String[] types = {"ncsa.d2k.modules.core.datatype.parameter.ParameterPoint","ncsa.d2k.modules.core.datatype.table.ExampleTable","ncsa.d2k.modules.core.datatype.table.ExampleTable"};
		return types;
	}

	private Random randomNumberGenerator = null;

	/**
	 * Init the standard fields
	 */
	public void beginExecution () {
		pointsPushed = 0;
		examples = new ArrayList ();
		best = 0;
		randomNumberGenerator = new Random (seed);
		donePushing = false;
	}

	/**
	 * We trigger whenever we have a scored point as input. We process these first to ensure
	 * we don't fill pipes. Then if we have processed a space fully, we check to see if there
	 * is another space input to process. If none of the above, the we check to see if we still
	 * need to push parameter points out.
	 * @return
	 */
	public boolean isReady () {
		if (this.getFlags()[1] > 0)

		// We are always ready if there are examples to process.
			return true;
		else if (pointsPushed == 0 && this.getFlags()[0] > 0)

		// we have another space to process.
			return true;
		else if (pointsPushed != 0 && !donePushing)

		// We have already gotten the space, but have yet more points to push.
			return true;
		else
			return false;

	}


	ParameterSpace space;
	int best = 0;
	boolean donePushing = false;

	public void doit () {

		// If we have an example process it.
		if (this.getFlags()[1] > 0) {

			// We have an example, put it into our list of examples and we are done.
			// reading examples is given priority because this will keep the pipes from filling
			// up and overflowing.
			Example ex = (Example) this.pullInput (1);
			double newScore = ex.getOutputDouble (0);
			if (examples.size () > 0) {
				double bestScore = ((Example) examples.get (best)).getOutputDouble (0);
				if (minimizing) {
					if (newScore < bestScore) {
						best = examples.size ();
						if (newScore <= threashhold) {
							if (verbose) System.out.println ("\nMEANT THREASHHOLD.\n");
							donePushing = true;
						}
					}
				} else {
					if (newScore > bestScore) {
						best = examples.size ();
						if (newScore >= threashhold) {
							if (verbose) System.out.println ("\nMEANT THREASHHOLD.\n");
							donePushing = true;
						}
					}
				}
			} else {
				// this is the first example, it is always best.
				best = 0;
				if (minimizing) {
					if (newScore <= threashhold) {
						if (verbose) System.out.println ("\nMEANT THREASHHOLD.\n");
						donePushing = true;
					}
				} else {
					if (newScore >= threashhold) {
						if (verbose) System.out.println ("\nMEANT THREASHHOLD.\n");
						donePushing = true;
					}
				}
			}
			examples.add (ex);
			if (trace) {
				this.printExample (examples.size () + " - Acquired example: ", ex);
			}

			// Are we done processing examples for the current space?
			if (examples.size () == pointsPushed & donePushing) {

				// WE have processed all the example. Push the result tables, and get ready
				// for the next batch.
				this.pushOutput (this.getTable (examples), 2);
				Example winner = (Example) examples.get (best);
				ArrayList l = new ArrayList ();
				l.add (winner);
				this.pushOutput (this.getTable (l), 1);
				examples = new ArrayList ();
				pointsPushed = 0;
				donePushing = false;
				space = null;
				if (trace) {
					System.out.println ();
					this.printExample ("Winner: ", winner);
				}
			} else {
				if (!donePushing && pointsPushed != 0) {
					this.pushParameterPoint ();
				}
			}
		} else {

			// OK, so we don't have any examples to process, we must either have a new space
			// to start processing or more points to push.
			if (pointsPushed == 0)
				space = (ParameterSpace) this.pullInput (0);
			this.pushParameterPoint ();
		}
	}

	/**
	 * Push another paramter point, and update the accounting.
	 */
	private void pushParameterPoint () {
		int numParams = space.getNumParameters ();
		double[] point = new double[numParams];

		// Create one point in parameter space.
		for (int i = 0; i < numParams; i++) {
			double range = space.getMaxValue (i) - space.getMinValue (i);
			if (useresolution) {
				int resolution = space.getResolution (i) - 1;

				// This would be an error on the users part, resolution should never be zero.
				double increment;
				if (resolution <= 0) {
					increment = 0;
					resolution = 1;
				} else
					increment = range / (double) resolution;
				point[i] = space.getMinValue (i) + increment * randomNumberGenerator.nextInt (resolution+1);
			} else
				switch (space.getType (i)) {
					case ColumnTypes.DOUBLE:
						point[i] = space.getMinValue (i) + range * randomNumberGenerator.nextDouble ();
						break;
					case ColumnTypes.FLOAT:
						point[i] = space.getMinValue (i) + range * randomNumberGenerator.nextFloat ();
						break;
					case ColumnTypes.INTEGER:
						if ((int) range == 0) {
							point[i] = space.getMinValue (i);
						} else {
							point[i] = space.getMinValue (i) + randomNumberGenerator.nextInt ((int) (range + 1));
						}
						break;
					case ColumnTypes.BOOLEAN:
						if ((int) range == 0) {
							point[i] = space.getMinValue (i);
						} else {
							point[i] = space.getMinValue (i) + randomNumberGenerator.nextInt ((int) (range + 1));
						}
						break;
				}
		}
		String[] names = new String[space.getNumParameters ()];
		for (int i = 0; i < space.getNumParameters (); i++) {
			names[i] = space.getName (i);
		}
		ParameterPointImpl parameterPoint = (ParameterPointImpl) ParameterPointImpl.getParameterPoint (names, point);
		this.pushOutput (parameterPoint, 0);
		if (trace) System.out.println("Pushed point "+pointsPushed);
		if (verbose)
			this.printExample (pointsPushed + " - Pushed a point : ", parameterPoint);
		if (pointsPushed++ == maxIterations) {
			if (verbose) System.out.println ("\nPushed Max Points.\n");
			donePushing = true;
			if (trace) System.out.println("DONE");
=======
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
>>>>>>> 76aa07461566a5976980e6696204781271955163
		}
	}

	/**
<<<<<<< HEAD
	 * Given a two d array of doubles, create a table.
	 * @param ss the array of examples to include in the table.
	 * @return
	 */
	public ExampleTable getTable (ArrayList ss) {
		Example ex = (Example) ss.get (0);
		ExampleTable et = (ExampleTable) ex.getTable();
		int numInputs = et.getNumInputs (0);
		int numOutputs = et.getNumOutputs (0);
		int numColumns = numInputs + numOutputs;
		int numExamples = ss.size ();
		double[][] data = new double[numColumns][numExamples];
		String[] labels = new String[numColumns];
		Column[] cols = new Column[numColumns];

		// Construct the column nnames.
		for (int i = 0; i < numInputs; i++)
			labels[i] = et.getInputName (i);
		for (int i = 0; i < numOutputs; i++)
			labels[i + numInputs] = et.getOutputName (i);

		// First populate the double array entries corresponding to the inputs. Each input will go into
		// a different array so that it may arrive in a different column of the resulting table.
		for (int row = 0; row < numExamples; row++) {

			// Get the example for the row.
			ex = (Example) ss.get (row);

			// Put each input into the appropiate column.
			for (int column = 0; column < numInputs; column++) {
				data[column][row] = ex.getInputDouble (column);
			}
		}

		// Now do the outputs.
		for (int row = 0; row < numExamples; row++) {

			// Get the example for the row.
			ex = (Example) ss.get (row);

			// Put each input into the appropiate column.
			for (int column = 0; column < numOutputs; column++) {
				data[column + numInputs][row] = ex.getOutputDouble (column);
			}
		}

		// now create the columns
		for (int i = 0; i < numColumns; i++) {
			cols[i] = new DoubleColumn (data[i]);
			cols[i].setLabel (labels[i]);
		}

		// create the table.
		MutableTable mt = new MutableTableImpl (cols);
		et = mt.toExampleTable ();

		// Construct the input and output arrays.
		int[] inputs = new int[numInputs];
		for (int i = 0; i < numInputs; i++) inputs[i] = i;
		int[] outputs = new int[numOutputs];
		for (int i = 0; i < numOutputs; i++) outputs[i] = i + numInputs;
		et.setInputFeatures (inputs);
		et.setOutputFeatures (outputs);
		return et;
	}

	/**
	 * Just print an example.
	 * @param label
	 * @param ex
	 */
	private void printExample (String label, Example ex) {
		System.out.println (label);
		System.out.println ("  Inputs");
		ExampleTable et = (ExampleTable)ex.getTable();
		int ni = et.getNumInputs(0);
		int no = et.getNumOutputs(0);
		for (int i = 0; i < ni; i++) {
			System.out.println ("    " + et.getInputName (i) + " = " + ex.getInputDouble (i));
		}
		System.out.println ("  Outputs");
		for (int i = 0; i < no; i++) {
			System.out.println ("    " + et.getOutputName (i) + " = " + ex.getOutputDouble (i));
		}
	}
=======
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
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

