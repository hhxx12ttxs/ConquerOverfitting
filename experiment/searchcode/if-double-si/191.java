package screen;

import java.io.BufferedReader;

import menuV2.MainScreen;
import menuV2.endTrack;

import AudioEngine.AudioEngine;
import Game.AccelerationCounter;
import Game.Application;
import Game.Circuit;
import Game.Input;
import Game.Voiture;

import android.util.Log;
import chronometer.Chronometer;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.GdxRuntimeException;

import configuration.Config;

public class InGame extends Screen {

	private OrthographicCamera camera;
	private ShaderProgram shaders;
	private SpriteBatch batch;

	private boolean volantActif;
	private Sprite volantSprite;
	private Texture volant;

	private Mesh fullscreenQuad;
	private float angle;
	private int posX;
	private int posY;
	private float FCentrifuge;
	private Voiture voiture;
	private AudioEngine audio;
	private Pixmap map;
	private Circuit circuit;
	private Texture circuitTexture;
    private int comptLap;
	private int compteurTiles;
	Application appli;


	private Texture skyTexture;
	
	/* TUTORIEL */
	private boolean tutorial = true;
	private Texture tapToGo;
	private Sprite tapToGoSprite;
	private BitmapFont font;

	private TextButton quit;
	
	private boolean collisionLimite;
	
	AccelerationCounter accelerationCounter;
	Chronometer chrono;
	
	private Screen sc;
	public InGame(String cheminCircuit, Application app) {
		
		Gdx.input.setInputProcessor(app.getInput());
		
		appli = app;
		sc = this;//pas inutile
		Skin skin;
		Gdx.input.setInputProcessor(stage);
		FileHandle f1 = Gdx.files.internal("skins/uiskin.json");
		skin = new Skin(f1);
		
		quit = new TextButton("quitter", skin);
		quit.setTouchable(Touchable.enabled);
		quit.setBounds(0, Gdx.graphics.getHeight()-quit.getHeight(), quit.getWidth(), quit.getHeight());
		
		quit.addListener(new InputListener(){
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				return true;  // must return true for touchUp event to occur
			}
		}
				);
		this.stage.addActor(quit);
		
		volantActif = false;
		
		application = app;
		this.camera = new OrthographicCamera();
		this.camera.setToOrtho(false, Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight());
		FileHandle VSShaderFile;
		FileHandle PSShaderFile;
		String VSShader;
		String PSShader;
		BufferedReader input;

		this.collisionLimite = false;

		VSShader = new String();
		PSShader = new String();
		VSShaderFile = Gdx.files.internal("Mode7RendererVS.glsl");

		PSShaderFile = Gdx.files.internal("Mode7RendererPS.glsl");
		if (!PSShaderFile.exists() || !VSShaderFile.exists())
			throw new GdxRuntimeException("Cannot read shaders");
		if (!Gdx.app.getGraphics().isGL20Available())
			throw new GdxRuntimeException("OpenGLES 2 needed");

		try {
			String line;

			input = new BufferedReader(VSShaderFile.reader());
			while ((line = input.readLine()) != null) {
				VSShader = VSShader + line
						+ System.getProperty("line.separator");
			}
			input.close();
			input = new BufferedReader(PSShaderFile.reader());
			while ((line = input.readLine()) != null) {
				PSShader = PSShader + line
						+ System.getProperty("line.separator");
			}
			input.close();

		} catch (Exception e) {
			throw new GdxRuntimeException("Can't read shaders");
		}

		createFullScreenQuad();
		circuit = new Circuit(cheminCircuit,app.config);
		this.circuitTexture = circuit.getTexture();

		/* VOLANT */
		this.batch = new SpriteBatch();
		this.volant = new Texture( Gdx.files.internal("volant.png") );
		this.skyTexture = new Texture( Gdx.files.internal("sky.png") );

		this.volantSprite = new Sprite(volant);
		this.volantSprite.scale(-0.5f);
		com.badlogic.gdx.graphics.Color couleurs = this.volantSprite.getColor();
		this.volantSprite.setColor(couleurs.r, couleurs.g, couleurs.b, 140);
		this.volantSprite.setPosition(Gdx.graphics.getWidth()/2 - volantSprite.getWidth()/2, -110);

		this.shaders = new ShaderProgram(VSShader, PSShader);
		if (!shaders.isCompiled())
			throw new GdxRuntimeException("Error compiling shaders "
					+ shaders.getLog());

		this.posX = circuit.getDepartX() + 128;
		this.posY = circuit.getDepartY() + 128;
		this.angle = circuit.getAngleDepart();

		this.voiture = new Voiture();

		this.map = circuitTexture.getTextureData().consumePixmap();
		circuitTexture = circuit.getTexture();
		
		this.compteurTiles =0;
		this.comptLap=0;

		/* Tutoriel */
		if(tutorial){

			// Ecriture 
			font = new BitmapFont();
			font.setColor(255.f, 255.f, 0.f, 1.f);
			font.setScale(5.f);

			// Image 
			tapToGo = new Texture(Gdx.files.internal("tapToGo.png"));
			tapToGoSprite = new Sprite(tapToGo);
			tapToGoSprite.scale(-0.5f);
			tapToGoSprite.setPosition(Gdx.graphics.getWidth()/2 - tapToGoSprite.getWidth()/2, Gdx.graphics.getHeight()/2 - tapToGoSprite.getHeight()/2 - 100);

		}

		audio = app.getAudio();
		
		accelerationCounter = new AccelerationCounter();
		chrono = new Chronometer();
		
	}

	public void createFullScreenQuad() {

		float[] verts = new float[12];
		int i = 0;

		verts[i++] = -1f;
		verts[i++] = -1f;
		verts[i++] = 0;

		verts[i++] = 1f;
		verts[i++] = -1f;
		verts[i++] = 0;

		verts[i++] = 1f;
		verts[i++] = 0f;
		verts[i++] = 0;

		verts[i++] = -1f;
		verts[i++] = 0f;
		verts[i++] = 0;

		fullscreenQuad = new Mesh(true, 4, 0, new VertexAttribute(
				VertexAttributes.Usage.Position, 3, "vPosition"));

		fullscreenQuad.setVertices(verts);
	}
	

	  private void renderSky()
	  {
		TextureRegion skyRegion;
		int startPixel;
		double ratio;
		double toDraw;
		
	    startPixel = (int)(this.angle / 360.0 * this.skyTexture.getWidth());
	    toDraw = (int)(Config.RENDERER_FOV / 360.0 * this.skyTexture.getWidth());
	    ratio = Gdx.graphics.getWidth() / toDraw;
	    this.batch.begin();

	    if (this.skyTexture.getWidth() - startPixel < toDraw)
	    {
	      skyRegion = new TextureRegion(this.skyTexture, startPixel, 0, this.skyTexture.getWidth() - startPixel, this.skyTexture.getHeight());
	      this.batch.draw(skyRegion, 0, 0, (int)(ratio * (this.skyTexture.getWidth() - startPixel)), Gdx.graphics.getHeight());
	      skyRegion = new TextureRegion(this.skyTexture, 0, 0, (int)(toDraw - (this.skyTexture.getWidth() - startPixel)), this.skyTexture.getHeight());
	      this.batch.draw(skyRegion, (int)(ratio * (this.skyTexture.getWidth() - startPixel)), 0, (int)(ratio * (toDraw - (this.skyTexture.getWidth() - startPixel))) + 1, Gdx.graphics.getHeight());
	    }
	    else
	    {
	      skyRegion = new TextureRegion(this.skyTexture, startPixel, 0, (int)toDraw, this.skyTexture.getHeight());
	      this.batch.draw(skyRegion, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	    }
	    this.batch.end();

	  }


	public void render() {

		Gdx.graphics.getGL20().glClearColor(0, 0, 0, 1);
		Gdx.graphics.getGL20().glClear(
				GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		renderSky();
		circuitTexture.bind(0);

		shaders.begin();
		shaders.setUniformi("trackTexture", 0);
		shaders.setUniformf("screenSize", new Vector2(Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight()));

		shaders.setUniformf(
				"trackTextureSize",
				new Vector2(circuitTexture.getWidth(), circuitTexture
						.getHeight()));

		shaders.setUniformf("windowCenter", new Vector2(
				Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2));

		shaders.setUniformf("cameraFov", (float) Math.toRadians(60));

		shaders.setUniformf("cameraScreenPosition", new Vector2(255, 64));

		shaders.setUniformf("deltaAngle",
				(float) (Math.toRadians(60) / Gdx.graphics.getWidth()));

		shaders.setUniformf("cameraPosition", new Vector2(posX, posY));

		shaders.setUniformf("cameraAngle", (float) Math.toRadians(angle));

		fullscreenQuad.render(shaders, GL20.GL_TRIANGLE_FAN);

		shaders.end();

		/* VOLANT */
		
		this.batch.begin();

		if(! tutorial){ // Ne pas afficher volant si phase tutoriel
			
			if(volantActif){
				if(! (Gdx.input.getAccelerometerY() > -0.2 && Gdx.input.getAccelerometerY() < 0.2)){
					this.volantSprite.setRotation( - (Gdx.input.getAccelerometerY() * 15) );
				}
				
				this.volantSprite.draw(batch);
			}
		}else{
			font.draw(batch, "Appuyez pour freiner!", 100 , Gdx.graphics.getHeight() - 50);
			audio.playTuto();
			this.tapToGoSprite.draw(batch);
		}

		voiture.render(batch);
		
		quit.draw(batch, 1);
		batch.draw(accelerationCounter.getCurrentSpeed((int)voiture.getVitesse()), Gdx.graphics.getWidth()-10, 0);
		chrono.updateAndDisplay(batch);
		this.batch.end();
	}

	public void resize(int width, int height) {
	}

	public void pause() {
		// audio.pause();
	}

	public void resume() {
	}

	public void tick(Input input) {

		Gdx.input.setInputProcessor(input);
		if(tutorial){ // Si phase de tutorial, ne pas prendre en compte l'accélération etc..
			if(Gdx.input.isTouched()){
				tutorial = false;
				audio.stopTuto();
				chrono.begin();
			}

			return;
		}

		if (Gdx.app.getType() != ApplicationType.Android) {

			if (!input.allKeyReleased()) { // PC

				/* Clavier HAUT-BAS */
				if (input.keyTyped((char) Input.UP)) {
					voiture.accelerer(Gdx.graphics.getDeltaTime());
				} else if (input.keyTyped((char) Input.DOWN)
						&& input.buttons[Input.DOWN]) {
					voiture.decelerer(Gdx.graphics.getDeltaTime() * 3);
				} else {
					voiture.decelerer(Gdx.graphics.getDeltaTime());
				}

				/* Clavier GAUCHE-DROITE */
				if (input.keyTyped((char) Input.LEFT)
						&& input.buttons[Input.LEFT]) {
					tourner(-1.3f);
				} else if (input.keyTyped((char) Input.RIGHT)
						&& input.buttons[Input.RIGHT]) {
					tourner(1.3f);
				} else {
					FCentrifuge = 0;
				}

			}

		} else { // ANDROID
			if (!Gdx.input.isTouched()) {
				// Log.v("test" , ""+Gdx.input.getAccelerometerZ());
				// acceleration
				voiture.accelerer(Gdx.graphics.getDeltaTime());
				audio.acceleration();
				
			} else {
				// frottement de la route
				voiture.decelerer(Gdx.graphics.getDeltaTime());
				audio.deceleration(voiture.getVitesse());
			}

			/* ACCELEROMETRE - AXE Y */
			if (Gdx.input.getAccelerometerY() > 0.4) {
				tourner(Gdx.input.getAccelerometerY());
			} else if (Gdx.input.getAccelerometerY() < -0.4) {
				tourner(Gdx.input.getAccelerometerY());
			} else {
				FCentrifuge = 0;
			}
			if(Gdx.input.getX()<=quit.getWidth() && Gdx.input.getY()<=quit.getHeight())
			{
				//TODO disposer les objects sinon bug au bout de 3 lancers
				audio.pause();
				circuit.dispose();
				appli.setScreen(new MainScreen(appli), sc);
				return ;
			}

		}
		setNouvellePosition();

	}

	public void tourner(float inclinaison) {

		inclinaison /= 3;
		/*
		if (inclinaison < 0) {
			if (FCentrifuge < 0.3 * -inclinaison) {
				FCentrifuge += voiture.getVitesse() * inclinaison / 1000;
			}
		} 
		else {
			if (FCentrifuge < 0.3 * -inclinaison) {
				FCentrifuge += voiture.getVitesse() * inclinaison / 1000;
			}
		}
		 */
		angle += inclinaison;
		angle %= 360;

		if(angle<0){
			angle = (360+angle) % 360;
		}

		if(!isOnRightWay()){
			audio.playWrongWay();
		}
		else{
			audio.stopWrongWay();
		}
	}

	public void setNouvellePosition() {
		
		double x = posX + Math.cos(Math.toRadians(angle))
				* voiture.getVitesse() * Gdx.graphics.getDeltaTime();

		double y = posY + Math.sin(Math.toRadians(angle))
				* voiture.getVitesse() * Gdx.graphics.getDeltaTime();

		if (isOnImage(x, y) && circuit.tileAt((int)x, (int)y).isRoad()) {
			posX = (int) x;
			posY = (int) y;
			if(this.collisionLimite){
				this.collisionLimite=false;
			}
		}
		else{
			// Faire vibrer
			if(! this.collisionLimite){
				Gdx.input.vibrate(500);
				this.collisionLimite = true;
				audio.deceleration(voiture.getVitesse());
			}
			voiture.decelerer(Gdx.graphics.getDeltaTime() * 6);
		}

		if (circuit.isNewTile(posX/256, posY/256)){
			compteurTiles++;
			if(circuit.isLapOver(compteurTiles)){
				comptLap++;
				compteurTiles=0;
				// TODO : recuperer le temps de tour
			}
			
			if(comptLap == circuit.getnbLap()){
				//TODO : terminer le jeu
				audio.pause();
				application.setScreen(new endTrack(application,chrono,circuit.getName()),this);
			}
			
			if(circuit.getNextTurn(posX/256, posY/256) == -1 && application.config.audio){
				//annoncer le virage � droite au joueur
				audio.playTournezADroite();
			}

			else if(circuit.getNextTurn(posX/256, posY/256) == 1 && application.config.audio){
				//annoncer le gauche � droite au joueur
				audio.playTournezAGauche();
			}
		}
	}

	public boolean isOnImage(double x, double y) {
		if (x > circuitTexture.getWidth() || y > circuitTexture.getHeight()
				|| x < 0 || y < 0) {
			return false;
		} else {
			return true;
		}
	}

	public boolean isOnRightWay(){

		int direction = circuit.getDirectionToNextTile(posX/256, posY/256);

		if (direction ==0 && ( angle>110 && angle<=250) ){
			return false;
		}
		else if (direction ==1 && (angle>200 && angle<=340) ){
			return false;
		}
		else if (direction ==2 && ( (angle> 290 && angle<=360) || (angle>0 && angle<=70 ) )){
			return false;
		}
		else if (direction ==3 && (angle >20 && angle<=160) ){
			return false;
		}
		else{
			return true;
		}
	}

	public void dispose() {
		audio.pause();
		//rajouter tou les dispose necessaires ainsi que dans voiture...
	}


	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void render(float arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void show() {
		// TODO Auto-generated method stub

	}

}

