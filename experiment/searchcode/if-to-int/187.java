package com.e3roid.examples;

import java.util.ArrayList;

import android.widget.Toast;

import com.e3roid.E3Activity;
import com.e3roid.E3Engine;
import com.e3roid.E3Scene;
import com.e3roid.drawable.controls.StickController;
import com.e3roid.drawable.sprite.AnimatedSprite;
import com.e3roid.drawable.sprite.TerminalSprite;
import com.e3roid.drawable.texture.AssetTexture;
import com.e3roid.drawable.texture.TiledTexture;
import com.e3roid.event.ControllerEventListener;
import com.e3roid.util.Debug;

public class DebugTerminalExample extends E3Activity implements ControllerEventListener {

	private final static int WIDTH  = 480;
	private final static int HEIGHT = 320;
	
	private TiledTexture texture;
	private AnimatedSprite sprite;
	private AssetTexture controlBaseTexture;
	private AssetTexture controlKnobTexture;
	private TerminalSprite terminal;
		
	private ArrayList<AnimatedSprite.Frame> downFrames  = new ArrayList<AnimatedSprite.Frame>();
	private ArrayList<AnimatedSprite.Frame> upFrames    = new ArrayList<AnimatedSprite.Frame>();
	private ArrayList<AnimatedSprite.Frame> leftFrames  = new ArrayList<AnimatedSprite.Frame>();
	private ArrayList<AnimatedSprite.Frame> rightFrames = new ArrayList<AnimatedSprite.Frame>();

	@Override
	public E3Engine onLoadEngine() {
		E3Engine engine = new E3Engine(this, WIDTH, HEIGHT);
		engine.requestFullScreen();
		engine.requestLandscape();
		return engine;
	}

	@Override
	public E3Scene onLoadScene() {
		E3Scene scene = new E3Scene();

		// add sprite
		int centerX = (getWidth()  - texture.getTileWidth())  / 2;
		int centerY = (getHeight() - texture.getTileHeight()) / 2;
		
		sprite = new AnimatedSprite(texture, centerX, centerY);
		sprite.animate(500, downFrames);
		scene.getTopLayer().add(sprite);
		
		// add analog controller
		StickController controller = new StickController(
				controlBaseTexture, controlKnobTexture,
				0, getHeight() - controlBaseTexture.getHeight(), scene, this);
		controller.setAlpha(0.7f);
		scene.addHUD(controller);
		scene.addEventListener(controller);
		
		scene.setBackgroundColor(0.94f, 1.00f, 0.94f, 1);

		// create debug terminal, add to HUD and hides it.
		try {
			terminal = new TerminalSprite(
					getEngine().getTerminalManager(this).openDebugConnection(), this);
			terminal.setAlpha(0.0f); // hide the terminal
			scene.addHUD(terminal);
		} catch (Exception e) {
			Toast.makeText(getContext(), "Failed to load terminal", Toast.LENGTH_SHORT).show();
			Debug.e(e);
		}
		return scene;
	}
	
	@Override
	public void onUserDisposed() {
		getEngine().getTerminalManager(this).closeDebugConnection();
	}

	@Override
	public void onLoadResources() {
		// 31x49 pixel sprite with 1px border and (0,0) tile.
		texture = new TiledTexture("king.png", 31, 49, 0, 0, 3, 2, this);
		
		// Initialize animation frames from tile.
		downFrames = new ArrayList<AnimatedSprite.Frame>();
		downFrames.add(new AnimatedSprite.Frame(0, 0));
		downFrames.add(new AnimatedSprite.Frame(1, 0));
		downFrames.add(new AnimatedSprite.Frame(2, 0));
		downFrames.add(new AnimatedSprite.Frame(3, 0));
		
		leftFrames = new ArrayList<AnimatedSprite.Frame>();
		leftFrames.add(new AnimatedSprite.Frame(0, 1));
		leftFrames.add(new AnimatedSprite.Frame(1, 1));
		leftFrames.add(new AnimatedSprite.Frame(2, 1));
		leftFrames.add(new AnimatedSprite.Frame(3, 1));

		rightFrames = new ArrayList<AnimatedSprite.Frame>();
		rightFrames.add(new AnimatedSprite.Frame(0, 2));
		rightFrames.add(new AnimatedSprite.Frame(1, 2));
		rightFrames.add(new AnimatedSprite.Frame(2, 2));
		rightFrames.add(new AnimatedSprite.Frame(3, 2));

		upFrames = new ArrayList<AnimatedSprite.Frame>();
		upFrames.add(new AnimatedSprite.Frame(0, 3));
		upFrames.add(new AnimatedSprite.Frame(1, 3));
		upFrames.add(new AnimatedSprite.Frame(2, 3));
		upFrames.add(new AnimatedSprite.Frame(3, 3));
		
		controlBaseTexture = new AssetTexture("controller_base.png", this);
		controlKnobTexture = new AssetTexture("controller_knob.png", this);		
	}

	@Override
	public void onControlUpdate(StickController controller,
			int relativeX, int relativeY, boolean hasChanged) {
		if (hasChanged) {
			int dir = controller.getDirection();
			if (dir == StickController.LEFT) {
				sprite.animate(500, leftFrames);
			} else if (dir == StickController.RIGHT) {
				sprite.animate(500, rightFrames);
			} else if (dir == StickController.UP) {
				sprite.animate(500, upFrames);
			} else if (dir == StickController.DOWN) {
				sprite.animate(500, downFrames);
			}

			// Show/hide the debug terminal
			if (dir == StickController.CENTER) {
				terminal.setAlpha(0.0f);
			} else {
				terminal.setAlpha(0.5f);
			}
			
			// Send debug log.
			Debug.d(String.format("onControlUpdate: X=%d, Y=%d", relativeX, relativeY));
		}
		int x = sprite.getRealX() + (relativeX / 5);
		int y = sprite.getRealY() + (relativeY / 5);
		
		if (x > 0 && y > 0 && x < getWidth()  - sprite.getWidth() &&
							  y < getHeight() - sprite.getHeight()) {
			sprite.move(x, y);
		}
	}
}

