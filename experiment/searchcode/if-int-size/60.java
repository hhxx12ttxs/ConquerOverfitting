package com.e3roid.examples;

import android.hardware.SensorManager;
import android.view.MotionEvent;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.e3roid.E3Activity;
import com.e3roid.E3Engine;
import com.e3roid.E3Scene;
import com.e3roid.drawable.Shape;
import com.e3roid.drawable.Sprite;
import com.e3roid.drawable.texture.AssetTexture;
import com.e3roid.drawable.texture.Texture;
import com.e3roid.physics.PhysicsShape;
import com.e3roid.physics.PhysicsWorld;
import com.e3roid.util.Debug;
import com.e3roid.util.FPSListener;
import com.e3roid.util.MathUtil;

/*
 *  This class shows the example of physics.
 *  Some of the functionality was inspired by the code by Nicolas Gramlich from AndEngine(www.andengine.org).
 */
public class PhysicsExample extends E3Activity implements FPSListener {

	private final static int WIDTH  = 320;
	private final static int HEIGHT = 480;
	
	private PhysicsWorld world;
	private Texture texture;

	@Override
	public E3Engine onLoadEngine() {
		E3Engine engine = new E3Engine(this, WIDTH, HEIGHT);
		engine.requestFullScreen();
		engine.requestPortrait();
		return engine;
	}

	@Override
	public E3Scene onLoadScene() {
		E3Scene scene = new E3Scene();
		scene.addEventListener(this);
		scene.registerUpdateListener(60, world);
		engine.getFPSCounter().addListener(this);		
		
		// create physics box
		int size = 2;
		Shape ground = new Shape(0, getHeight() - size, getWidth(), size);
		Shape roof   = new Shape(0, 0, getWidth(), size);
		Shape left   = new Shape(0, 0, size, getHeight());
		Shape right  = new Shape(getWidth() - size, 0, size, getHeight());
		
		final FixtureDef wallFixtureDef = createFixtureDef(0.0f, 0.0f, 0.5f);
		createBoxBody(this.world, ground, BodyType.StaticBody, wallFixtureDef);
		createBoxBody(this.world, roof, BodyType.StaticBody, wallFixtureDef);
		createBoxBody(this.world, left, BodyType.StaticBody, wallFixtureDef);
		createBoxBody(this.world, right, BodyType.StaticBody, wallFixtureDef);
		
		scene.getTopLayer().add(ground);
		scene.getTopLayer().add(roof);
		scene.getTopLayer().add(left);
		scene.getTopLayer().add(right);
		
		// every event in the world must be handled by the update thread.
		postUpdate(new AddShapeImpl(scene, getWidth() / 2, getHeight() / 2));
		
		scene.setBackgroundColor(0.94f, 1.00f, 0.94f, 1);
		
		Toast.makeText(this, "Touch screen to add sprites.", Toast.LENGTH_LONG).show();
		return scene;
	}
	
	@Override
	public void onLoadResources() {				
		world   = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);
		texture = new AssetTexture("block.png", this);
		texture.setReusable(true);
	}
	
	@Override
	public boolean onSceneTouchEvent(final E3Scene scene, final MotionEvent motionEvent) {
		if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
			int x = getTouchEventX(scene, motionEvent);
			int y = getTouchEventY(scene, motionEvent);
			// every event in the world must be handled by the update thread.
			postUpdate(new AddShapeImpl(scene, x, y));
		}
		return false;
	}	
	
	private Sprite newSprite(int x, int y) {
		return new Sprite(texture, x, y);
	}
	
	class AddShapeImpl implements Runnable {
		private final E3Scene scene;
		private final int x;
		private final int y;
		AddShapeImpl(E3Scene scene, int x, int y) {
			this.scene = scene;
			this.x = x;
			this.y = y;
		}
		@Override
		public void run() {
			FixtureDef objectFixtureDef = createFixtureDef(1.0f, 0.0f, 0.5f);

			Sprite sprite = newSprite(x, y);
	
			Body body = createBoxBody(
					world, sprite, BodyType.DynamicBody, objectFixtureDef);
			world.addShape(new PhysicsShape(sprite, body));
	
			scene.getTopLayer().add(sprite);
		}
	}

	@Override
	public void onFPS(float fps, float minFPS, float maxFPS) {
		Debug.d(String.format("FPS: %.2f (MIN %.2f / MAX %.2f)", fps, minFPS, maxFPS));
	}

	private FixtureDef createFixtureDef(float density, float restitution, float friction) {
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.density = density;
		fixtureDef.restitution = restitution;
		fixtureDef.friction = friction;
		fixtureDef.isSensor = false;
		return fixtureDef;
	}

	private Body createBoxBody(PhysicsWorld physicsWorld, Shape shape,
			BodyType bodyType, FixtureDef fixtureDef) {
		float pixelToMeterRatio = PhysicsWorld.PIXEL_TO_METER_RATIO_DEFAULT;
		BodyDef boxBodyDef = new BodyDef();
		boxBodyDef.type = bodyType;

		float[] sceneCenterCoordinates = shape.getGlobalCenterCoordinates();
		boxBodyDef.position.x = sceneCenterCoordinates[0] / (float)pixelToMeterRatio;
		boxBodyDef.position.y = sceneCenterCoordinates[1] / (float)pixelToMeterRatio;

		Body boxBody = physicsWorld.createBody(boxBodyDef);
		PolygonShape boxPoly = new PolygonShape();

		float halfWidth = shape.getWidthScaled() * 0.5f / pixelToMeterRatio;
		float halfHeight = shape.getHeightScaled() * 0.5f / pixelToMeterRatio;

		boxPoly.setAsBox(halfWidth, halfHeight);
		fixtureDef.shape = boxPoly;
		boxBody.createFixture(fixtureDef);
		boxPoly.dispose();
		
		boxBody.setTransform(boxBody.getWorldCenter(), MathUtil.degToRad(shape.getAngle()));

		return boxBody;
	}
}

