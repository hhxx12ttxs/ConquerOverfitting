package ca.sandstorm.luminance.test.usecase;

import javax.vecmath.Vector2f;

import android.app.Instrumentation;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import ca.sandstorm.luminance.Engine;
import ca.sandstorm.luminance.Luminance;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.TextView;
import ca.sandstorm.luminance.R;

//import org.slf4j.//logger;
//import org.slf4j.//loggerFactory;
import com.jayway.android.robotium.solo.Solo;

/**
 * Use case based GUI testing
 * 
 * @author Amara Daal 
 * 		NOTE: Since Luminance doesn't use android.widget none of
 *         the buttons will be detected by robotium 
 *      NOTE: These GUI tests depend
 *         on what machine you are running, the emulator may not be responsive
 *         if the system is less than i5 M460 2.53Ghz, 3Gigs of memory. Even
 *         when running multiple times depending on system resources the
 *         emulator could be non responsive and miss one touch input, that would
 *         cause the tests to fail.
 *      NOTE: A usercase test may fail after running many tests the emulator 
 *      	becomes unresponsive
 */
public class LuminanceTest extends ActivityInstrumentationTestCase2<Luminance> {

	// Package we are testing
	private static final String TARGET_PACKAGE_ID = "ca.sandstorm.luminance";

	/*
	 * Touch related variables
	 */
	// Robotium
	private Solo _solo;
	// Time in between game operations
	int _timeBetweenOperations = 2000;
	// Time between click commands
	int _timeBetweenClicks = 200;
	// Instrumentation used to send touch events directly to the engine
	Instrumentation _inst;
	/*
	 * A matrix with centre positions for every square used for easily placing
	 * objects.
	 */
	Vector2f[][] _selectLevelMatrix;

	/*
	 * Level related variables
	 */
	// Levels
	Level _levelArray[];
	// Tested levels
	int _numberOfTestedLevels = 14;
	// Level names
	int _simplyMirrors = 0;
	int _mirrorBasics = 1;
	int _zigZag = 2;
	int _justLikeChristmas = 3;
	int _thinkAboutIt = 4;
	int _yourBrainOnRGB = 5;
	int _entryLevel = 6;
	int _simplyPrisms = 7;
	int _simpleCombo = 8;
	int _allTooEasy = 9;
	int _doubleSided = 10;
	int _thePrism = 11;
	int _morePrism = 12;
	int _firstBlood = 13;

	/*
	 * In game grid variables
	 */
	// Offset of the matrix from top left corner of the screen
	Vector2f _matrixLocation;
	// Space between grid squares
	float _spaceBtwnSquares = 29;
	// Start in the middle of the square
	float _xCoordView = (_spaceBtwnSquares / 2);
	float _yCoordView = (_spaceBtwnSquares / 2);
	/*
	 * A matrix with centre positions for every square used for easily placing
	 * objects.
	 */
	Vector2f[][] _matrix;

	/**
	 * Construct Usecase tests
	 */
	public LuminanceTest() {
		super(TARGET_PACKAGE_ID, Luminance.class);

	}

	/**
	 * Calculate the global grid square positions called at the start of each
	 * level
	 * 
	 * @precondition newMatrixLocation is not null
	 * @postcondition A symbolic representation of the grid is created
	 * 
	 * @param newMatrixLocatin
	 *            the upper left corner of the grid
	 * @param matrixWidth
	 *            cols
	 * @param matrixHeight
	 *            rows
	 */
	public void calculateInGameGridMatrix(Vector2f newMatrixLocation,
			int gotSpaceBtwnSquares, int matrixHeight, int matrixWidth) {
		// logger.info("LuminanceTestSuite: UseCase: Calculating grid squarePositions");
		_spaceBtwnSquares = gotSpaceBtwnSquares;
		_matrix = new Vector2f[matrixHeight][matrixWidth];
		_xCoordView = (_spaceBtwnSquares / 2);
		_yCoordView = (_spaceBtwnSquares / 2);
		for (int row = 0; row < matrixHeight; row++) {

			for (int col = 0; col < matrixWidth; col++) {
				_matrix[row][col] = new Vector2f(_xCoordView
						+ newMatrixLocation.x, _yCoordView + newMatrixLocation.y);
				// *****
				// ***->
				// adjust x to match next square
				// start in the middle of the square
				_xCoordView += _spaceBtwnSquares;

			}
			// reset x
			_xCoordView = (_spaceBtwnSquares / 2);
			_yCoordView += _spaceBtwnSquares;
		}
	}

	/**
	 * Setup robotium to test Luminance activity
	 * 
	 * @precondition All your new levels were added to the array here requires
	 *               that a private class is created which implements Level
	 *               interface
	 * 
	 */
	public void setUp() throws Exception {
		super.setUp();
		this._solo = new Solo(this.getInstrumentation(), this.getActivity());

		// Create and order tests
		_levelArray = new Level[_numberOfTestedLevels];

		// Loading Levels here
		_levelArray[_simplyMirrors] = new SimplyMirrors();
		_levelArray[_mirrorBasics] = new MirrorBasics();
		_levelArray[_zigZag] = new ZigZag();
		_levelArray[_justLikeChristmas] = new JustLikeChristmas();
		_levelArray[_thinkAboutIt] = new ThinkAboutIt();
		_levelArray[_yourBrainOnRGB] = new YourBrainOnRGB();
		_levelArray[_entryLevel] = new EntryLevel();
		_levelArray[_doubleSided] = new DoubleSided();
		_levelArray[_thePrism] = new ThePrism();
		_levelArray[_morePrism] = new MorePrism();
		_levelArray[_firstBlood] = new FirstBlood();
		_levelArray[_simplyPrisms] = new SimplyPrisms();
		_levelArray[_simpleCombo] = new SimpleCombo();
		_levelArray[_allTooEasy] = new AllTooEasy();

	}

	/**
	 * Completes all test levels solved for the game
	 * 
	 * @precondition Luminance must be on first level in game
	 * @postcondition All test levels are solved,
	 */
	public void completeAllLevels() {
		for (int i = 0; i < _numberOfTestedLevels; i++) {
			_levelArray[i].complete();
		}
	}

	/**
	 * Completes level specified
	 * 
	 * @precondition User has already reached the level in game
	 * @postcondition The level is completed and the next level starts
	 * @param level
	 */
	public void completeLevel(int level) {
		_levelArray[level].complete();
	}

	/**
	 * Reaches level specified to bypass prior levels used for testing a certain
	 * level
	 * 
	 * @precondition Luminance must be on first level in game
	 * @postcondition all levels prior to "level" is completed
	 * @param level
	 */
	public void reachLevel(int level) {
		for (int i = 0; i < level; i++) {
			_levelArray[i].complete();
		}
	}

	/**
	 * Selects first level from level menu
	 * 
	 * @precondition user is on level screen
	 * @postcondition first level is started
	 */
	public void selectFirstLevel() {
		_solo.clickOnScreen(21, 111);
		_solo.sleep(5000);
	}

	/**
	 * Select level NOTE: only 0-13 at the moment
	 * 
	 * @param level
	 *            the number shown on choose level menu
	 * @precondition user is on level menu screen
	 * @postcondition the level is started
	 */
	public void selectLevel(int level) {

		if (level == 0) {

			_solo.clickOnScreen(21, 111);
		} else if (level == 1) {
			_solo.clickOnScreen(129, 110);
		} else if (level == 2) {
			_solo.clickOnScreen(236, 110);
		} else if (level == 3) {
			_solo.clickOnScreen(21, 195);
		} else if (level == 4) {
			_solo.clickOnScreen(126, 195);
		} else if (level == 5) {
			_solo.clickOnScreen(233, 193);
		} else if (level == 6) {
			_solo.clickOnScreen(24, 281);
		} else if (level == 7) {
			_solo.clickOnScreen(131, 281);
		} else if (level == 8) {
			_solo.clickOnScreen(236, 278);
		} else if (level == 9) {
			_solo.clickOnScreen(24, 369);
		} else if (level == 10) {
			_solo.clickOnScreen(135, 373);
		} else if (level == 11) {
			_solo.clickOnScreen(241, 371);
		} else if (level == 12) {
			_solo.clickOnScreen(27, 453);
		} else if (level == 13) {
			_solo.clickOnScreen(132, 453);
		} else if (level == 13) {
			_solo.clickOnScreen(132, 453);
		} else {
			// Other levels are not covered
		}

	}

	/**
	 * Start the first level of the game by pressing the start button
	 * 
	 * @precondition Luminance is on the menu screen
	 * @postcondition Reaches the level select screen NOTE: Robotium fails at
	 *                clicking the "Start" button commented out code shows 130
	 *                clicks were sent still registration was inconsistent. We
	 *                resorted to sending manual events to the game engine
	 */
	public void startGame() {
		_solo.sleep(10000);

		// Location of the start button from the top left
		float startButtonX = 183;
		float startButtonY = 232;

		int startMatrixRows = 3;
		int startMatrixCols = 9;

		Vector2f startMatrixLocation = new Vector2f(29, 226);

		// logger.info("LuminanceTestSuite: UseCase: Starting Game");

		// Calculate the grid positions
		calculateInGameGridMatrix(startMatrixLocation, 29, startMatrixRows,
				startMatrixCols);

		/*
		 * Simulate touch event, inform the engine that a motion event has
		 * occured
		 */
		long downTime = SystemClock.uptimeMillis();
		// event time MUST be retrieved only by this way!
		long eventTime = SystemClock.uptimeMillis();

		// Loops and presses start button 10 times instead of just 1, behaves
		// differently
		// on different systems needed to ensure this registers on all systems
		// for (int i = 0; i < 10; i++) {

		MotionEvent event = MotionEvent.obtain(downTime, eventTime,
				MotionEvent.ACTION_DOWN, startButtonX, startButtonY, 0);
		_inst.sendPointerSync(event);
		eventTime = SystemClock.uptimeMillis() + 600;

		event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP,
				startButtonX, startButtonY, 0);
		_inst.sendPointerSync(event);
		_solo.sleep(500);
		// }

		// Previous way of clicking the start button
		// for(int i = 0; i < 130; i++){
		// solo.clickOnScreen(startButtonX, startButtonY);
		// }

		_solo.sleep(5000);

	}

	/**
	 * Pauses level
	 * 
	 * @precondition Luminance is in game
	 * @postcondition Luminance is paused
	 */
	public void pause() {

		// logger.info("LuminanceTestSuite: UseCase: Pausing Game");
		_solo.clickOnScreen(295, 447);
		_solo.sleep(5000);
	}

	/**
	 * Restarts level
	 * 
	 * @precondition Luminance is in a level
	 * @postcondition the level is restarted
	 * @param from
	 *            either ingame or inmenu
	 */
	public void restart(String from) {
		if (from.equalsIgnoreCase("ingame")) {
			_solo.clickOnScreen(296, 441);
			_solo.sleep(_timeBetweenClicks);
			_solo.clickOnScreen(142, 241);

		} else if (from.equalsIgnoreCase("inmenu")) {
			_solo.clickOnScreen(142, 241);
		}

	}

	/**
	 * Places prism at given coordinates Grid starts at (1,1)
	 * 
	 * @precondition Luminance is in game
	 * @postcondition the prism is placed
	 * @param row
	 *            start from 1
	 * @param col
	 *            start from 1
	 */
	public void placePrism(int row, int col) {
		// Offset user choice to vector index
		row -= 1;
		col -= 1;
		// logger.info("LuminanceTestSuite: UseCase: Placing prism at "+ "(" +
		// row + "," + col+")");

		// press on toolbar
		_solo.clickOnScreen(94, 443);
		// place object
		_solo.sleep(_timeBetweenOperations);
		_solo.clickOnScreen(_matrix[row][col].x, _matrix[row][col].y);
		_solo.sleep(_timeBetweenOperations);
	}

	/**
	 * Places mirror at given coordinates Grid starts at (1,1)
	 * 
	 * @precondition Luminance is in game
	 * @postcondition the mirror is placed
	 * @param row
	 *            start from 1
	 * @param col
	 *            start from 1
	 */
	public void placeMirror(int row, int col) {
		// Offset user choice to vector index
		row -= 1;
		col -= 1;

		// logger.info("LuminanceTestSuite: UseCase: Placing Mirror at "+ "(" +
		// row + "," + col+")");
		_solo.clickOnScreen(37, 431);

		_solo.sleep(_timeBetweenOperations);
		// place mirror
		_solo.clickOnScreen(_matrix[row][col].x, _matrix[row][col].y);

		_solo.sleep(_timeBetweenOperations);
	}

	/**
	 * Rotates mirror by double tap Grid starts at (1,1)
	 * 
	 * @precondition Luminance is in game, the object must rotatable
	 * @postcondition the object at the location is rotated
	 * @param row
	 *            start from 1
	 * @param col
	 *            start from 1
	 */
	public void rotate(int row, int col) {
		// Offset user choice to vector index
		row -= 1;
		col -= 1;
		// logger.info("LuminanceTestSuite: UseCase: Rotating mirror at "+ "(" +
		// row + "," + col+")");

		// Double tap,
		_solo.sleep(_timeBetweenOperations);

		long downTime = SystemClock.uptimeMillis();
		// event time MUST be retrieved only by this way!
		long eventTime = SystemClock.uptimeMillis();

		MotionEvent event = MotionEvent.obtain(downTime, eventTime,
				MotionEvent.ACTION_DOWN, _matrix[row][col].x,
				_matrix[row][col].y, 0);
		_inst.sendPointerSync(event);
		// eventTime = SystemClock.uptimeMillis() + 10;

		event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP,
				_matrix[row][col].x, _matrix[row][col].y, 0);
		_inst.sendPointerSync(event);

		event = MotionEvent.obtain(downTime, eventTime,
				MotionEvent.ACTION_DOWN, _matrix[row][col].x,
				_matrix[row][col].y, 0);
		_inst.sendPointerSync(event);

		event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP,
				_matrix[row][col].x, _matrix[row][col].y, 0);
		_inst.sendPointerSync(event);

		_solo.sleep(_timeBetweenOperations);

	}

	/**
	 * Erases Object at location Grid starts at (1,1)
	 * 
	 * @precondition Luminance is in game, the object is erasable
	 * @postcondition the object is erased
	 * @param row
	 *            start from 1
	 * @param col
	 *            start from 1
	 */
	public void erase(int row, int col) {
		row -= 1;
		col -= 1;
		// logger.info("LuminanceTestSuite: UseCase: Erasing object at "+ "(" +
		// row + "," + col+")");
		_solo.clickOnScreen(159, 434);
		_solo.sleep(_timeBetweenOperations);
		_solo.clickOnScreen(_matrix[row][col].x, _matrix[row][col].y);
		_solo.sleep(_timeBetweenOperations);
	}

	private void reduceMemory() {

	}

	/**
	 * Starts the tutorial for the user
	 * 
	 * @precondition the user is in menu
	 * @postcodition the tutorial is started
	 */
	private void tutorial() {
		// TODO Auto-generated method stub
		_solo.clickOnScreen(22, 441);
	}

	/**
	 * Brings the user back to the main menu
	 * 
	 * @precondition the user is in game playing a level
	 * @postcondition the user is brought back to the main menu
	 */
	private void mainMenu() {
		// TODO Auto-generated method stub
		_solo.clickOnScreen(298, 446);
		_solo.sleep(4000);
		_solo.clickOnScreen(147, 317);

	}

	/**
	 * Resumes the game
	 * 
	 * @precondition the in game menu has been pulled up
	 * @postcondition the game is resumed
	 * @param state
	 *            either ingame or inmenu
	 */
	private void resume(String state) {
		if (state.equalsIgnoreCase("ingame")) {
			_solo.clickOnScreen(299, 444);
			_solo.sleep(_timeBetweenClicks);
			_solo.clickOnScreen(162, 173);

		} else if (state.equalsIgnoreCase("inmenu")) {
			_solo.clickOnScreen(162, 173);
		}

	}

	/**
	 * Moves onto the next level
	 * 
	 * @precondition the level complete graphic is on screen
	 * @postcondition the game moves onto the next level
	 */
	private void nextLevel() {
		_solo.clickOnScreen(161, 266);
		_solo.sleep(5000);
	}

	/**
	 * Toggles sound on or off
	 * 
	 * @precondition none
	 * @postcondition the sound is either turned on or off
	 * @param state
	 */
	private void toggleSound(String state) {
		if (state.equalsIgnoreCase("ingame")) {
			_solo.clickOnScreen(297, 443);
			_solo.sleep(_timeBetweenClicks);
			_solo.clickOnScreen(143, 318);
			_solo.sleep(_timeBetweenClicks);
			_solo.clickOnScreen(302, 442);

		} else if (state.equalsIgnoreCase("inmenu")) {
			_solo.clickOnScreen(302, 442);
		}
		// TODO Auto-generated method stub

	}

	/**
	 * NOTE: There may be no way to implement this without sending command line
	 * args to emulator
	 */
	private void openLuminance() {
		// TODO Auto-generated method stub

	}

	/**
	 * Takes the game to the main test level
	 * 
	 * @precondition user is at the select level menu
	 * @postcondition morePrism level is started
	 */
	private void reachMorePrism() {
		_solo.clickOnScreen(28, 452);
		_solo.sleep(_timeBetweenOperations);
	}

	/**
	 * Brings user back to main menu
	 */
	private void exit() {
		_solo.clickOnScreen(297, 443);
		_solo.sleep(_timeBetweenClicks);
		_solo.clickOnScreen(143, 318);
		// TODO Auto-generated method stub

	}

	/**
	 * Chooses the help option
	 * 
	 * @param from
	 *            either ingame or inmenu
	 */
	private void help(String from) {
		if (from.equalsIgnoreCase("ingame")) {
			_solo.clickOnScreen(297, 443);
			_solo.sleep(_timeBetweenClicks);
			_solo.clickOnScreen(143, 318);
			_solo.sleep(_timeBetweenClicks);

			_solo.clickOnScreen(161, 308);
		} else if (from.equalsIgnoreCase("inmenu")) {
			_solo.clickOnScreen(161, 308);
		}

	}

	/**
	 * Alters the emulator orientation
	 * 
	 * @precondition The emulator is on
	 * @postcondtion The orientation of the emulator is swapped
	 * @param orientation
	 *            either landscape or portrait
	 */
	@SuppressWarnings("static-access")
	private void alterOrientation(String orientation) {
		if (orientation.equalsIgnoreCase("landscape")) {
			_solo.setActivityOrientation(_solo.LANDSCAPE);
		} else if (orientation.equalsIgnoreCase("portrait")) {
			_solo.setActivityOrientation(_solo.PORTRAIT);
		}

	}

	/**
	 * NOTE: May not be implemented
	 */
	private void stateCallRecieved() {
		// TODO Auto-generated method stub

	}

	/**
	 * Test Case (1): Light to obstacle collision
	 * 
	 * Purpose: To test and verify connections to objects are being handled
	 * appropriately.
	 * 
	 * Tests Level: "The Prism"
	 * 
	 * Grid Top Left(29, 226)
	 */
	public void testCaseOne() {

		// logger.info("LuminanceTestSuite: UseCase: Test Case One");
		// logger.info("LuminanceTestSuite: UseCase: Matrix Located at "+ "(" +
		// 29 + "," + 226+")");

		_inst = getInstrumentation();

		startGame();
		reachMorePrism();
		_levelArray[_morePrism].setGrid();

		placeMirror(1, 1);
		rotate(1, 1);
		placeMirror(6, 1);
		// User chooses mirror
		// User places mirror(1)
		// White Light collides with mirror
		// White Light reflects south
		// User chooses mirror

		placeMirror(6, 5);
		// User places mirror(2)
		// Light collides with mirror
		placePrism(8, 5);
		// User chooses prism
		// User places Prism
		// System refracts light into RGB
		// System deactivates prism from toolbar
		// Light collides with outer boundary
		// prism -> outer boundary
		// Light collides with differing color orb
		// prism -> wrong orb
		// Light collides with mirror bounces back to prism
		// prism -> mirror -> prism

		placeMirror(1, 8);
		// User places mirror(3)
		// Light collides with mirror
		// Light collides with Wall obstacle
		// prism -> brick

		rotate(6, 5);
		// User Rotates mirror(2)
		// Light reflects to a different angle
		// Prism no longer refracts light
		// All coloured lights dissapear

		erase(6, 5);
		// User erases mirror(2)
		erase(1, 8);
		// User erases mirror(3)
		erase(8, 5);
		// User erases prism
		placePrism(5, 6);
		// User chooses prism
		// User places prism (5, 6)
		// System refracts light into RGB
		// System deactivates prism from toolbar
		// Green light collides with outer boundary
		// Blue light collides with wall obstacle
		// Red light intersects with red orb
		placeMirror(4, 6);
		// User chooses mirror
		// User places mirror(4) at (4, 6)
		// Green light collides with mirror
		// green light connects with green orb
		rotate(4, 6);
		// User Rotates mirror(4)
		// Light reflects south
		placeMirror(4, 7);
		// User chooses mirror
		// User places mirror (5) at (4, 7)
		// Green light reflects to east
		// Light intersects with different light
		erase(4, 7);
		// User chooses eraser
		// User erases mirror(5)
		placeMirror(4, 8);
		// User chooses mirror
		// User places mirror (6) at (4, 8)
		// Light collides with mirror
		// two different lights into one orb
		// prism -> mirror -> mirror -> wrong orb
		erase(4, 8);
		// User chooses eraser
		// User erases mirror(6)
		rotate(4, 6);
		// User rotates mirror(4)
		// Green light collides with mirror
		// Green light connects with green orb
		placeMirror(8, 6);
		// User places mirror (7) at (8, 6)
		// Blue light collides with mirror
		// Blue light connects with blue orb
		// User completes level
		mainMenu();
	}

	/**
	 * Tear down method to dispose robotium
	 */
	public void tearDown() throws Exception {
		try {
			this._solo.finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		getActivity().finish();
		super.tearDown();
	}

	/**
	 * Test Case (2): Object interaction/function
	 * 
	 * Purpose: To test and verify that objects unique, functional, and interact
	 * with each other.
	 * 
	 * Tests Level: "More Prism"
	 */
	public void testCaseTwo() {
		// logger.info("LuminanceTestSuite: UseCase: Test Case Two");
		// logger.info("LuminanceTestSuite: UseCase: Matrix Located at "+ "(" +
		// 29 + "," + 226+")");
		_inst = getInstrumentation();

		startGame();
		reachLevel(_morePrism);
		_levelArray[_morePrism].setGrid();
		// User starts game
		// Game view is shown
		// prism -> mirror -> orb
		placeMirror(6, 0);
		// User chooses mirror
		// User places mirror(1)
		// White Light collides with mirror
		// White Light reflects south
		// User chooses mirror

		placeMirror(6, 5);
		// User places mirror(2)
		// Light collides with mirror
		placePrism(8, 5);
		// User chooses prism
		// User places Prism
		// System refracts light into RGB
		// System deactivates prism from toolbar
		// Light collides with outer boundary
		// prism -> outer boundary
		// Light collides with differing color orb
		// prism -> wrong orb
		// Light collides with mirror bounces back to prism
		// prism -> mirror -> prism
		// placeMirror()
		// User chooses mirror
		// User places mirror
		// Light collides with mirror
		// User chooses eraser
		erase(8, 5);
		// User erases prism
		// Prism disappears
		// RGB disappears
		// System reactivates prism in toolbar
		// White light collides with mirror
		placePrism(8, 5);
		// User chooses prism
		// User places prism
		// System refracts light into RGB
		// System deactivates prism from toolbar
		// Lights reflects off of mirror

		rotate(6, 0);
		// User Rotates mirror
		// Light reflects to a different angle
		erase(6, 0);
		// User chooses eraser
		// User erases mirror
		// Light continues on with no obstacle in the way
		pause();
		// User presses pause
		// System brings up pop up menu
		restart("inmenu");
		// User presses restart
		// System brings up the same map reset
		mainMenu();

	}

	/**
	 * Test Case (3): Object to obstacle collision
	 * 
	 * Purpose: To test and verify that objects unique, functional, and interact
	 * with each other.
	 * 
	 * Tests Level: "More Prism"
	 */
	public void testCaseThree() {
		_inst = getInstrumentation();
		startGame();
		reachMorePrism();
		_levelArray[_morePrism].setGrid();

		// prism -> mirror -> orb
		placePrism(4, 5);
		// User chooses prism
		// User places Prism
		// System refracts light into RGB
		// System deactivates prism from toolbar
		placeMirror(4, 6);
		// User chooses mirror
		// User places mirror
		// Light collides with mirror
		erase(3, 4);
		// User chooses eraser
		// User tries to erase wall
		rotate(4, 6);
		// User Rotates mirror
		// Light reflects to a different angle
		placeMirror(4, 6);
		// User chooses mirror
		// User tries to place a mirror on top of another mirror
		placeMirror(3, 4);
		// User tries to place a mirror on top of a Wall
		placeMirror(4, 5);
		// User tries to place a mirror on top of prism
		rotate(4, 6);
		placeMirror(1, 5);
		placeMirror(6, 5);
		mainMenu();
	}

	/**
	 * Currently plays the first 12 levels of the game
	 * 
	 * @precondition user is on game menu
	 * @postcondition user has reached level first blood
	 */
	public void testPlayFirstLevels() {
		_inst = getInstrumentation();
		startGame();
		selectFirstLevel();
		reachLevel(12);
		mainMenu();
	}

	/**
	 * Test Case (4): User statistics registration
	 * 
	 * Purpose: To test and verify that toolbar/high score is correctly updated
	 * after each object use
	 * 
	 * Tests Level: "More Prism"
	 */
	public void testCaseFour() {
		_inst = getInstrumentation();
		startGame();

		reachMorePrism();
		_levelArray[_morePrism].setGrid();

		placePrism(4, 5);
		// User chooses prism
		// User places prism in empty grid square
		// Prism count is reduced in toolbar
		erase(4, 6);
		// User chooses eraser
		// User erases prism
		// System increments number of prisms
		placePrism(4, 5);
		placeMirror(4, 6);
		rotate(4, 6);
		placeMirror(1, 5);
		placeMirror(6, 5);
		rotate(6, 5);
		// User beats game
		// System updates high score with time
		// User chooses main menu
		// User selects high score bored
		// User sees high score bored is updated
		mainMenu();
	}

	/**
	 * Test Case (5): User transition between states, saved menu, resume
	 * 
	 * Purpose: To test and verify that state changes are taking place and are
	 * handled appropriately
	 * 
	 * Tests Level: "More Prism"
	 */
	public void testCaseFive() {
		_inst = getInstrumentation();
		startGame();

		reachMorePrism();
		_levelArray[_morePrism].setGrid();
		// completeDoubleSided();

		placePrism(4, 5);
		// User chooses prism
		// User places Prism
		// System refracts light into RGB
		// System deactivates prism from toolbar
		placeMirror(1, 6);
		// User chooses mirror
		// User places mirror
		// Light collides with mirror
		stateCallRecieved();
		// User receives a call
		// Game sound is automatically turned off
		alterOrientation("landscape");
		alterOrientation("portrait");
		// User rotates phone
		// Phones changes orientation
		// Game is automatically paused
		exit();
		// User exits the Luminance
		openLuminance();
		// User reenters Luminance
		// System shows pop up menu screen
		toggleSound("inmenu");
		// User chooses sound on
		// User chooses resume
		startGame();
		selectFirstLevel();
		// resume();
		// System reloads all previously made moves
		pause();
		// User chooses pause
		// System loads pop up menu
		mainMenu();
		// User chooses main menu
		// System loads main menu
	}

	/**
	 * Test Case (6): Stress test
	 * 
	 * Purpose: To test and verify that is able to cope under stranious
	 * conditions
	 * 
	 * Tests Level: "Tutorial"
	 */
	public void testCaseSix() {
		_inst = getInstrumentation();
		startGame();

		// User starts game
		// Android's Luminance memory is reduced
		tutorial();
		// User chooses first level play through tutorial
		reduceMemory();
		// Android slowly reduces memory for Luminance to 0
		// Luminance system realizes memory is too low returns error exits
		reduceMemory();
		// Android's Luminance memory is reset to normal
		// Android's hard drive space is set to very low
		startGame();
		// User starts game
		tutorial();
		mainMenu();
		// User chooses first level play through tutorial
		// Luminance gives error hard drive space is too low
		// Luminance exits
	}

	/**
	 * Level in Luminance
	 * 
	 * @author Amara Daal
	 * 
	 */
	private class MirrorBasics implements Level {
		/**
		 * Completes this level
		 */
		public void complete() {
			// logger.info("LuminanceTestSuite: UseCase: Completing level 1 ");

			calculateInGameGridMatrix(new Vector2f(29, 226), 29, 3, 9);
			placeMirror(1, 1);
			rotate(1, 1);
			placeMirror(1, 5);
			placeMirror(3, 5);

			placeMirror(3, 7);
			rotate(3, 7);
			placeMirror(1, 7);
			rotate(1, 7);

		}

		/**
		 * Set the current grid to this levels archtecture
		 */
		public void setGrid() {
			calculateInGameGridMatrix(new Vector2f(29, 226), 29, 3, 9);
		}
	}

	/**
	 * Level in Luminance
	 * 
	 * @author Amara Daal
	 * 
	 */
	private class JustLikeChristmas implements Level {
		/**
		 * Completes this level
		 */
		public void complete() {
			calculateInGameGridMatrix(new Vector2f(29, 140), 52, 5, 5);
			rotate(1, 1);
			rotate(1, 1);
			rotate(1, 1);
			rotate(5, 5);
			nextLevel();

			// TODO Auto-generated method stub

		}

		public void setGrid() {
			calculateInGameGridMatrix(new Vector2f(29, 140), 52, 5, 5);
		}
	}

	/**
	 * Level in Luminance
	 * 
	 * @author Amara Daal
	 * 
	 */
	private class ZigZag implements Level {
		/**
		 * Completes this level
		 */
		public void complete() {
			calculateInGameGridMatrix(new Vector2f(110, 140), 33, 8, 3);
			_solo.sleep(1000);
			placeMirror(1, 3);
			placeMirror(1, 3);
			placeMirror(3, 3);
			rotate(3, 3);
			placeMirror(3, 1);
			rotate(3, 1);
			placeMirror(5, 1);
			placeMirror(5, 3);
			placeMirror(7, 3);
			rotate(7, 3);
			placeMirror(7, 2);
			rotate(7, 2);
			nextLevel();

			// TODO Auto-generated method stub

		}

		public void setGrid() {
			calculateInGameGridMatrix(new Vector2f(110, 140), 33, 8, 3);
		}
	}

	/**
	 * Level in Luminance
	 * 
	 * @author Amara Daal
	 * 
	 */
	private class ThinkAboutIt implements Level {
		/**
		 * Completes this level
		 */
		public void complete() {
			calculateInGameGridMatrix(new Vector2f(29, 140), 52, 5, 5);
			placeMirror(2, 4);

			placeMirror(3, 4);
			rotate(3, 4);

			placePrism(3, 3);
			nextLevel();

		}

		/**
		 * Set the current grid to this levels archtecture
		 */
		public void setGrid() {
			calculateInGameGridMatrix(new Vector2f(29, 140), 52, 5, 5);
		}
	}

	/**
	 * Level in Luminance
	 * 
	 * @author Amara Daal
	 * 
	 */
	private class ThePrism implements Level {
		/**
		 * Completes this level
		 */
		public void complete() {
			calculateInGameGridMatrix(new Vector2f(29, 140), 52, 6, 6);
			placePrism(5, 4);
			placeMirror(6, 4);
			rotate(6, 4);
			placeMirror(5, 6);
			rotate(5, 6);
			placeMirror(5, 1);
			nextLevel();
		}

		/**
		 * Set the current grid to this levels archtecture
		 */
		public void setGrid() {
			calculateInGameGridMatrix(new Vector2f(29, 140), 52, 6, 6);
		}
	}

	/**
	 * Level in Luminance
	 * 
	 * @author Amara Daal
	 * 
	 */
	private class MorePrism implements Level {
		/**
		 * Completes this level
		 */
		public void complete() {
			calculateInGameGridMatrix(new Vector2f(29, 140), 33, 8, 8);
			placeMirror(1, 6);
			placePrism(7, 6);
			placeMirror(7, 4);
			placeMirror(7, 8);
			nextLevel();
		}

		/**
		 * Set the current grid to this levels archtecture
		 */
		public void setGrid() {
			calculateInGameGridMatrix(new Vector2f(29, 140), 33, 8, 8);
		}
	}

	/**
	 * Level in Luminance
	 * 
	 * @author Amara Daal
	 * 
	 */
	private class SimplyMirrors implements Level {
		/**
		 * Completes this level
		 */
		public void complete() {
			calculateInGameGridMatrix(new Vector2f(29, 215), 37, 3, 7);
			placeMirror(1, 7);
			placeMirror(3, 7);
			rotate(3, 7);
			nextLevel();
		}

		/**
		 * Set the current grid to this levels archtecture
		 */
		public void setGrid() {
			calculateInGameGridMatrix(new Vector2f(29, 215), 37, 3, 7);
		}
	}

	/**
	 * Level in Luminance
	 * 
	 * @author Amara Daal
	 * 
	 */
	private class FirstBlood implements Level {
		/**
		 * Completes this level TODO:
		 */
		public void complete() {

		}

		/**
		 * Set the current grid to this levels archtecture
		 */
		public void setGrid() {

		}
	}

	/**
	 * Level in Luminance
	 * 
	 * @author Amara Daal
	 * 
	 */
	private class DoubleSided implements Level {
		/**
		 * Completes this level
		 */
		public void complete() {
			calculateInGameGridMatrix(new Vector2f(29, 140), 52, 5, 5);
			placeMirror(4, 1);
			placeMirror(4, 5);
			rotate(4, 5);
			placeMirror(4, 3);
			nextLevel();
		}

		/**
		 * Set the current grid to this levels archtecture
		 */
		public void setGrid() {
			calculateInGameGridMatrix(new Vector2f(29, 140), 52, 5, 5);
		}
	}

	/**
	 * Level in Luminance
	 * 
	 * @author Amara Daal
	 * 
	 */
	private class EntryLevel implements Level {
		/**
		 * Completes this level
		 */
		public void complete() {
			calculateInGameGridMatrix(new Vector2f(29, 140), 52, 5, 5);
			rotate(3, 3);
			rotate(3, 3);
			placePrism(3, 2);
			placeMirror(1, 2);
			rotate(1, 2);
			placeMirror(5, 2);
			nextLevel();
		}

		/**
		 * Set the current grid to this levels archtecture
		 */
		public void setGrid() {
			calculateInGameGridMatrix(new Vector2f(29, 140), 52, 5, 5);
		}
	}

	/**
	 * Level in Luminance
	 * 
	 * @author Amara Daal
	 * 
	 */
	private class YourBrainOnRGB implements Level {

		/**
		 * Completes this level
		 */
		public void complete() {
			calculateInGameGridMatrix(new Vector2f(29, 140), 44, 6, 6);
			// Rotate red emmitter
			rotate(2, 6);
			rotate(2, 6);
			// Rotate blue emmitter
			rotate(3, 5);
			// Rotate green emitter
			rotate(4, 6);
			rotate(4, 6);
			placeMirror(4, 1);
			placeMirror(2, 2);
			rotate(2, 2);
			placeMirror(1, 5);
			nextLevel();
		}

		/**
		 * Set the current grid to this levels archtecture
		 */
		public void setGrid() {
			calculateInGameGridMatrix(new Vector2f(29, 140), 44, 6, 6);
		}
	}

	/**
	 * Level in Luminance
	 * 
	 * @author Amara Daal
	 * 
	 */
	private class SimplyPrisms implements Level {
		/**
		 * Completes this level
		 */
		public void complete() {
			calculateInGameGridMatrix(new Vector2f(26, 138), 52, 5, 5);
			placePrism(3, 3);
			nextLevel();
		}

		/**
		 * Set the current grid to this levels archtecture
		 */
		public void setGrid() {
			calculateInGameGridMatrix(new Vector2f(26, 138), 52, 5, 5);
		}
	}

	/**
	 * Level in Luminance
	 * 
	 * @author Amara Daal
	 * 
	 */
	private class SimpleCombo implements Level {
		/**
		 * Completes this level
		 */
		public void complete() {
			calculateInGameGridMatrix(new Vector2f(29, 140), 52, 5, 5);
			placeMirror(5, 3);
			rotate(5, 3);
			placePrism(3, 3);
			placeMirror(3, 1);
			placeMirror(3, 5);
			rotate(3, 5);
			nextLevel();
		}

		/**
		 * Set the current grid to this levels archtecture
		 */
		public void setGrid() {
			calculateInGameGridMatrix(new Vector2f(29, 140), 52, 5, 5);
		}
	}

	/**
	 * Level in Luminance
	 * 
	 * @author Amara Daal
	 * 
	 */
	private class AllTooEasy implements Level {
		/**
		 * Completes this level
		 */
		public void complete() {
			calculateInGameGridMatrix(new Vector2f(29, 161), 44, 5, 6);
			rotate(1, 4);
			rotate(1, 4);
			rotate(1, 4);
			rotate(5, 4);
			placeMirror(3, 4);
			placePrism(3, 3);
			placePrism(3, 5);

			nextLevel();
		}

		/**
		 * Set the current grid to this levels archtecture
		 */
		public void setGrid() {
			calculateInGameGridMatrix(new Vector2f(29, 161), 44, 5, 6);
		}
	}
}

