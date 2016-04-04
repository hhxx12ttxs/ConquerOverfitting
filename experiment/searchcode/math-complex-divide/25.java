package ca.sandstorm.luminance.gamelogic;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.microedition.khronos.opengles.GL10;
import javax.vecmath.Vector3f;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;

import ca.sandstorm.luminance.Engine;
import ca.sandstorm.luminance.graphics.PrimitiveQuad;
import ca.sandstorm.luminance.gui.Button;
import ca.sandstorm.luminance.gui.GUIManager;
import ca.sandstorm.luminance.gui.IWidget;
import ca.sandstorm.luminance.gui.Label;
import ca.sandstorm.luminance.input.InputButton;
import ca.sandstorm.luminance.input.InputTouchScreen;
import ca.sandstorm.luminance.resources.SoundResource;
import ca.sandstorm.luminance.resources.TextureResource;
import ca.sandstorm.luminance.state.IState;


/**
 * State for handling the main menu.
 * 
 * @author Kumaran Vijayan
 * 
 */
public class MenuState implements IState
{
    private static final Logger logger = LoggerFactory
	    .getLogger(MenuState.class);
    private boolean _initialized = false;
    private GUIManager _guiManager;
    private boolean _tapped;
    private Button _soundButton;

    private TextureResource _background;
    private PrimitiveQuad _backgroundQuad;

    private SoundResource _startSound;
    
    private Button _creditsButton;


    public MenuState()
    {
	_tapped = false;
	_guiManager = new GUIManager(false);
    }


    public MenuState(IWidget[] widgets)
    {
	this();

	_guiManager.addWidgets(widgets);
    }


    /**
     * This method exists solely for testing the button action features. Its
     * existence is temporary.
     */
    public void test()
    {
	logger.debug("test()");

	Engine.getInstance().getAudio().play(_startSound, 0.9f);

	// TODO: this really isnt how we want to deal with states is it?
	Engine.getInstance().popState();
	Engine.getInstance().pushState(new GameState(0));
    }
    
    /**
     * This method creates and pushes a LevelMenuState.
     */
    public void pushLevelMenuState()
    {
	logger.debug("pushLevelMenuState()");
	
	if (_creditsButton != null && _creditsButton.getVisible())
	{
	    return;
	}
	
	Engine.getInstance().popState();
	Engine.getInstance().pushState(new LevelMenuState());
    }
    
    /**
     * This method creates and pushes a HelpMenuState
     */
    public void pushHelpMenuState()
    {
	logger.debug("pushHelpMenuState()");
	
	if (_creditsButton != null && _creditsButton.getVisible())
	{
	    return;
	}	
	
	Engine.getInstance().popState();
	Engine.getInstance().pushState(new HelpMenuState());
    }
      
    
    /**
     * This method exists solely for testing the button action features. Its
     * existence is temporary.
     */
    public void toggleSound()
    {
	logger.debug("toggleSound()");
	
	if (_creditsButton != null && _creditsButton.getVisible())
	{
	    return;
	}	
	
	boolean currentState = Engine.getInstance().getAudioEnabled();
	
	Engine.getInstance().setAudioEnabled(!currentState);
	_soundButton.setIsSelected(currentState);
    }    


    
    /**
     * Shows the credits quad
     */
    public void showCredits()
    {
	float width = Engine.getInstance().getViewWidth();
	float height = Engine.getInstance().getViewHeight();
	
	if (_creditsButton == null)
	{
    		_creditsButton = new Button(0.0f * width,
    	                           0.0f * height,
    	                           width,
    	                           height,
    	                           "Credits");
    		_guiManager.addButton(_creditsButton);	    		
	}
	
	_creditsButton.setVisible(true);
	
	_creditsButton.setTexture((TextureResource)Engine.getInstance().getResourceManager().getResource("textures/credits.png"));
	_creditsButton.setTextureResourceLocation("textures/credits.png");
	_creditsButton.setCalleeAndMethod(this, "hideCredits");
    }
    
    
    /**
     * Hides the credits quad
     */
    public void hideCredits()
    {
	_creditsButton.setVisible(false);
    }

    /**
     * Get the GUIManager being used by this MenuState.
     * 
     * @return the GUIManager being by the MenuState.
     */
    public GUIManager getGUIManager()
    {
	return _guiManager;
    }

    /**
     * Method to add widget to gui manager
     * @param widgets
     * 			array of IWidget that store widget
     */
    public void addWidgets(IWidget widgets[])
    {
	_guiManager.addWidgets(widgets);
    }


    /**
     * Engine has informed the state the device has changed.
     * 
     * @param gl
     *            OpenGL context
     * @param w
     *            The new width value
     * @param h
     *            The new height value
     * @precond We do not control the values here. w > 0 && h > 0 would be
     *          optimal.
     */
    @Override
    public void deviceChanged(GL10 gl, int w, int h)
    {
	logger.debug("deviceChanged(" + gl + ", " + w + ", " + h + ")");

	gl.glShadeModel(GL10.GL_SMOOTH); // Enable Smooth Shading
	gl.glClearColor(0.182f, 0.182f, 1, 1); // Error blue
	gl.glClearDepthf(1.0f); // Depth Buffer Setup
	gl.glEnable(GL10.GL_DEPTH_TEST); // Enables Depth Testing
	gl.glDepthFunc(GL10.GL_LEQUAL); // The Type Of Depth Testing To Do

	// Really Nice Perspective Calculations
	// gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);

	// prevent divide by zero.
	// @HACK - Forgiven since h == 0 means the game window is probably
	// hidden.
	if (h == 0) {
	    h = 1;
	}
    }


    /**
     * init() Engine has informed this state can init any openGL required
     * resources.
     * 
     * @param gl
     *            OpenGL context
     */
    @Override
    public void init(GL10 gl)
    {
	logger.debug("init(" + gl + ")");

	// Load sound effects and music
	try {
	    _startSound = Engine
		    .getInstance()
		    .getResourceManager()
		    .loadSound(Engine.getInstance().getAudio().getPool(),
			       "sounds/startSelect.ogg");
	    // Engine.getInstance().getResourceManager().loadSound(Engine.getInstance().getAudio().getPool(),
	    // "sounds/iconClick.mp3");
	    
	    Engine.getInstance().getResourceManager().loadTexture(gl, "textures/credits.png");
	    
	} catch (IOException e) {
	    logger.error("Unable to load a required sound: " + e.getMessage());
	    e.printStackTrace();
	}

	float width = Engine.getInstance().getViewWidth();
	float height = Engine.getInstance().getViewHeight();
	_backgroundQuad = new PrimitiveQuad(new Vector3f(0, 0, 0), new Vector3f(width,
		height, 0));

	// Recreate GUI on orientation flip
	if (Engine.getInstance().isInitialized()) {
	    _guiManager = new GUIManager(false);
	}

	// Initiate the GUIManager
	_guiManager.initialize(gl);

	// Create the Buttons
	Label luminanceTitle = new Label(0.0f * width, 0.075f * height,
		1.0f * width, 0.25f * height, "luminanceTitle");
	luminanceTitle.setTextureResourceLocation("textures/LuminanceLogo.png");

	Button startButton = new Button(0.175f * width, 0.350f * height,
		0.650f * width, 0.1250f * height, "Start");
	startButton.setTextureResourceLocation("textures/startImage.png");
	startButton.setTappedTextureLocation("textures/startImageClicked.png");
	startButton.setCalleeAndMethod(this, "pushLevelMenuState");

	Button helpButton = new Button(0.175f * width, 0.350f * height + .185f *
						       height, 0.650f * width,
		0.1250f * height, "Help");
	helpButton.setTextureResourceLocation("textures/helpImage.png");
	helpButton.setTappedTextureLocation("textures/helpImageClicked.png");
	helpButton.setCalleeAndMethod(this, "pushHelpMenuState");

	// Button levelButton = new Button(0.175f*width,
	// 0.350f*height + 2*.185f*height,
	// 0.650f*width,
	// 0.1250f*height,
	// "levels");
	// levelButton.setTextureResourceLocation("textures/helpImage.png");
	// levelButton.setTappedTextureLocation("textures/helpImageClicked.png");
	// levelButton.setCalleeAndMethod(this, "showLevelMenu");

	_soundButton = new Button(0.86f * width, 0.86f * height,
		0.14f * width, 0.12f * height, "Sound");
	_soundButton.setTextureResourceLocation("textures/sound.png");
	_soundButton.setTappedTextureLocation("textures/soundClicked.png");
	_soundButton.setCalleeAndMethod(this, "toggleSound");
	_soundButton.setIsSelected(!Engine.getInstance().getAudioEnabled());
	
	// Button scoreButton = new Button(0.010f*width,
	// 0.740f*height,
	// 0.140f*width,
	// 0.120f*height,
	// "Score");
	// scoreButton.setTextureResourceLocation("textures/scoreBoard.png");

	Button infoButton = new Button(0.010f * width, 0.860f * height,
		0.140f * width, 0.120f * height, "Info");
	infoButton.setTextureResourceLocation("textures/info.png");
	infoButton.setTappedTextureLocation("textures/infoClicked.png");
	infoButton.setCalleeAndMethod(this, "showCredits");

	_guiManager.addButton(luminanceTitle);
	_guiManager.addButton(startButton);
	_guiManager.addButton(helpButton);
	// _guiManager.addButton(levelButton);
	_guiManager.addButton(_soundButton);
	// _guiManager.addButton(scoreButton);
	_guiManager.addButton(infoButton);

	_loadTextures(gl);
    }


    /**
     * Method to handle loading textures needed by menu state
     * @param gl
     * 			openGL context
     */
    private void _loadTextures(GL10 gl)
    {
	try {
	    _background = Engine.getInstance().getResourceManager()
		    .loadTexture(gl, "textures/menuBackground.png");
	    for (IWidget widget : _guiManager.getWidgets()) {
		if (widget != null) {
		    String textureResourceLocation = widget
			    .getTextureResourceLocation();
		    TextureResource texture = Engine.getInstance()
			    .getResourceManager()
			    .loadTexture(gl, textureResourceLocation);
		    widget.setTexture(texture);

		    if (widget instanceof Button &&
			((Button) widget).getTappedTextureLocation() != null) {
			Button button = (Button)widget;
			String tappedTextureLocation = button.getTappedTextureLocation();
			TextureResource tappedTexture = Engine.getInstance().getResourceManager().loadTexture(gl, tappedTextureLocation);
			button.setTappedTexture(tappedTexture);
			
			// If the button is selected initially, set it to tapped texture
			if (button.getIsSelected()) {
			    // This causes the right things to happen internally
			    button.setIsSelected(button.getIsSelected());
			}
		    }
		}
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }


    /**
     * pause() Engine has requested this state be paused. This is different from
     * ingame pausing.
     */
    @Override
    public void pause()
    {
	// TODO Auto-generated method stub

    }


    /**
     * resume() Engine has requested this state to resume.
     */
    @Override
    public void resume()
    {
	// TODO Auto-generated method stub

    }


    /**
     * Engine has requested this state update itself.
     * 
     * @param OpenGL
     *            context
     */
    @Override
    public void update(GL10 gl)
    {
	// TODO Auto-generated method stub
	if (Engine.getInstance().getInputSystem().getTouchScreen()
		.getTouchEvent() != null) {
	    MotionEvent touchEvent = Engine.getInstance().getInputSystem()
		    .getTouchScreen().getTouchEvent();

	    if (Engine.getInstance().getInputSystem().getTouchScreen()
		    .getTouchMode() == InputTouchScreen.ON_DOWN) {

		Button eventWidget = _guiManager.touchOccured(touchEvent);
		if (eventWidget != null) {
		    logger.debug("button has been tapped");
		    eventWidget.setIsTapped(true);
		    _tapped = true;
		}

		Engine.getInstance().getInputSystem().getTouchScreen()
			.setTouchMode(InputTouchScreen.NONE);
	    }

	    if (touchEvent.getAction() == MotionEvent.ACTION_UP && _tapped) {
		logger.debug("Menu has been touched");
		if (_guiManager.buttonIsTapped()) {
		    logger.debug(_guiManager.getTappedButton().getIdentifier());
		}
		_guiManager.letGoOfButton();
		_tapped = false;
	    }

	}

	InputButton[] keys = Engine.getInstance().getInputSystem()
		.getKeyboard().getKeys();
	if (keys[KeyEvent.KEYCODE_1].getPressed()) {
	    System.exit(0);
	}
    }


    /**
     * Engine has requested this state draw itself.
     * 
     * @param OpenGL
     *            context
     */
    @Override
    public void draw(GL10 gl)
    {
	// clear to back and clear the depth buffer!
	// gl.glClearColor(0, 0, 0, 1);
	gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

	gl.glViewport(0, 0, Engine.getInstance().getViewWidth(), Engine
		.getInstance().getViewHeight());

	// render 3D stuff - if any
	// ... matrix push pops etc

	// render 2D stuff in a complex matrix saving manner
	gl.glMatrixMode(GL10.GL_MODELVIEW);
	gl.glPushMatrix();
	gl.glLoadIdentity();

	gl.glMatrixMode(GL10.GL_PROJECTION);
	gl.glPushMatrix();
	gl.glLoadIdentity();
	gl.glOrthof(0, Engine.getInstance().getViewWidth(), Engine
		.getInstance().getViewHeight(), 0, -1.0f, 1.0f);

	gl.glMatrixMode(GL10.GL_MODELVIEW);

	gl.glTranslatef(0, 0, 0);
	gl.glEnable(GL10.GL_TEXTURE_2D);
	gl.glBindTexture(GL10.GL_TEXTURE_2D, _background.getTexture());
	_backgroundQuad.draw(gl);

	_guiManager.draw(gl);

	gl.glMatrixMode(GL10.GL_PROJECTION);
	gl.glPopMatrix();

	gl.glMatrixMode(GL10.GL_MODELVIEW);
	gl.glPopMatrix();
    }


    @Override
    public void messageRecieved()
    {
	// TODO Auto-generated method stub

    }


    @Override
    public boolean isActive()
    {
	// TODO Auto-generated method stub
	return true;
    }


    @Override
    public boolean isVisible()
    {
	// TODO Auto-generated method stub
	return true;
    }


    @Override
    public boolean isInitialized()
    {
	return _initialized;
    }


    @Override
    public void saveInstance(Bundle savedInstanceState)
    {

    }

}

