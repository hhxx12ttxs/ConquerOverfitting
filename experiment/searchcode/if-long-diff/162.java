package net.cis.client.game;

import javax.vecmath.Matrix3f;

import net.cis.client.game.scenery.factory.SkyBoxFactory;
import net.cis.client.game.scenery.model.Player;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.Joystick;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.JoyAxisTrigger;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.lwjgl.JInputJoyInput;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;

/**
 * Example 9 - How to make walls and floors solid. This collision code uses Physics and a custom Action Listener.
 * 
 * @author normen, with edits by Zathras
 */
public class HelloCollision extends SimpleApplication implements ActionListener {
	private BulletAppState bulletAppState;
	private Player player;
	private RigidBodyControl vehicleControl;
	// private float max;
	private Joystick[] joys;
	private JInputJoyInput jInputJoyInput;

	private long inputDelay;
	private long angularInputTime;
	private long dampingInterval;
	private long lastAngularDamping;
	private long velocityInputTime;
	private float targetSpeed;

	private long lastTimer;

	public static void main(String[] args) {
		HelloCollision app = new HelloCollision();
		app.start();
	}

	public HelloCollision() {
		super();
		AppSettings sets = new AppSettings(true);
		sets.setFullscreen(false);
		sets.setResolution(1024, 768);
		setSettings(sets);
		setShowSettings(false);

		long ticksPerMillisecond = timer.getResolution() / 1000;
		inputDelay = ticksPerMillisecond * 100;
		dampingInterval = ticksPerMillisecond * 100;
	}

	public void simpleInitApp() {
		getContext().getKeyInput().destroy();
		getContext().getKeyInput().initialize();

		/** Set up Physics */

		bulletAppState = new BulletAppState();
		stateManager.attach(bulletAppState);
		// bulletAppState.getPhysicsSpace().enableDebug(assetManager);

		viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
		flyCam.setMoveSpeed(100);
		// flyCam.setEnabled(false);
		setUpKeys();
		setUpLight();

		rootNode.attachChild(SkyBoxFactory.createSimpleSkyBox(assetManager));

		Spatial asteroid = assetManager.loadModel("spaceobject/asteroid/dusty/Asteroid.mesh.xml");
		asteroid.setLocalTranslation(0f, 0f, 20f);
		Material mat_asteroid = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat_asteroid.setTexture("ColorMap",
				assetManager.loadTexture("spaceobject/asteroid/dusty/asteroidtextur_512.jpg"));
		asteroid.setMaterial(mat_asteroid);

		rootNode.attachChild(asteroid);

		CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.5f, 6f, 1);
		vehicleControl = new RigidBodyControl(capsuleShape, 1f);
		vehicleControl.setMass(1.0f);

		cam.setLocation(vehicleControl.getPhysicsLocation());
		cam.setRotation(vehicleControl.getPhysicsRotation());

		bulletAppState.getPhysicsSpace().setGravity(Vector3f.ZERO);
		bulletAppState.getPhysicsSpace().add(vehicleControl);

		vehicleControl.setAngularSleepingThreshold(0.01f);

	}

	private void setUpLight() {
		// We add light so we see the scene
		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White.mult(1.3f));
		rootNode.addLight(al);

		DirectionalLight dl = new DirectionalLight();
		dl.setColor(ColorRGBA.White);
		dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
		rootNode.addLight(dl);
	}

	/**
	 * We over-write some navigational key mappings here, so we can add physics-controlled walking and jumping:
	 */
	private void setUpKeys() {

		inputManager.clearMappings();
		inputManager.getJoysticks();

		// jInputJoyInput = new JInputJoyInput();
		// joys = jInputJoyInput.loadJoysticks(inputManager);
		// jInputJoyInput.initialize();

		inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_LEFT));
		inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_RIGHT));
		inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_UP));
		inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_DOWN));
		inputManager.addMapping("Taste", new KeyTrigger(KeyInput.KEY_Q));
		inputManager.addMapping("GibGas", new KeyTrigger(KeyInput.KEY_W));
		inputManager.addMapping("Brems", new KeyTrigger(KeyInput.KEY_S));

		// inputManager.addMapping("Accelerate", new KeyTrigger(KeyInput.KEY_W));
		// inputManager.addMapping("Brake", new KeyTrigger(KeyInput.KEY_S));

		// inputManager.addMapping("MouseXleft", new JoyAxisTrigger(0,
		// JoyInput.AXIS_POV_X, false));
		// inputManager.addMapping("MouseXright", new JoyAxisTrigger(0,
		// JoyInput.AXIS_POV_X, true));

		inputManager.addMapping("MouseXleft", new JoyAxisTrigger(0, 1, true));
		inputManager.addMapping("MouseXright", new JoyAxisTrigger(0, 1, false));
		inputManager.addMapping("MouseYdown", new JoyAxisTrigger(0, 0, false));
		inputManager.addMapping("MouseYup", new JoyAxisTrigger(0, 0, true));
		// inputManager.addListener(this, "Joy Left", "Joy Right", "J",
		// "Joy Up");

		inputManager.addMapping("MouseXleft", new MouseAxisTrigger(MouseInput.AXIS_X, true));
		inputManager.addMapping("MouseXright", new MouseAxisTrigger(MouseInput.AXIS_X, false));
		inputManager.addMapping("MouseYup", new MouseAxisTrigger(MouseInput.AXIS_Y, true));
		inputManager.addMapping("MouseYdown", new MouseAxisTrigger(MouseInput.AXIS_Y, false));

		// inputManager.addMapping("", new JoyAxisTrigger(, JoyInput.AXIS_POV_X,
		// true));
		// Joystick joy = joys[0];
		// joys[0].assignAxis("MouseXleft", "MouseXright", joy.getXAxisIndex());

		// System.out.println(joy.getName());
		// System.out.println(joy.getAxisCount());

		// inputManager.addListener(this, "Left");
		// inputManager.addListener(this, "MouseXleft");
		// inputManager.addListener(this, "MouseXright");
		inputManager.addListener(this, "GibGas");
		inputManager.addListener(this, "Brems");

		inputManager.addListener(new AnalogListener() {
			private float thrusterX = 1.8f;
			private float thrusterY = .6f;
			private float maxRotation = 200.0f;
			private long boosterInputTime;

			@Override
			public void onAnalog(String name, float value, float tpf) {
				float xRotation = .0f;
				float yRotation = .0f;

				angularInputTime = timer.getTime();

				if (name.equals("MouseXleft")) {
					if (vehicleControl.getAngularVelocity().y > maxRotation) {
						yRotation = 0f;
					} else {
						yRotation += value * thrusterY;
					}
				}
				if (name.equals("MouseXright")) {
					if (vehicleControl.getAngularVelocity().y < -maxRotation) {
						yRotation = 0f;
					} else {
						yRotation -= value * thrusterY;
					}
				}

				// if (name.equals("MouseYup")) {
				// xRotation += value * thrusterY;
				// if (vehicleControl.getAngularVelocity().x > maxRotation)
				// xRotation = 0f;
				// }
				// if (name.equals("MouseYdown")) {
				// xRotation -= value * thrusterY;
				// if (vehicleControl.getAngularVelocity().x < -maxRotation)
				// xRotation = 0f;
				// }

				Vector3f mult = vehicleControl.getPhysicsRotation().mult(new Vector3f(xRotation, yRotation, 0.0f));
				vehicleControl.applyTorqueImpulse(mult);

				// optimieren... überprüfung bereits vor berechnung
				Vector3f angVel = vehicleControl.getAngularVelocity();
				if (angVel.y > maxRotation) {
					angVel.y = maxRotation;
					vehicleControl.setAngularVelocity(angVel);
				} else if (angVel.y < -maxRotation) {
					angVel.y = -maxRotation;
					vehicleControl.setAngularVelocity(angVel);
				}

				// Hier nur Booster einbauen
				float speedThruster = 10f;
				float acceleration = 0f;
				if (name.equals("Accelerate")) {
					acceleration = (value * speedThruster);
					Vector3f acc = new Vector3f(0f, 0f, acceleration);

					vehicleControl.applyImpulse(vehicleControl.getPhysicsRotation().mult(acc), Vector3f.ZERO);

					boosterInputTime = timer.getTime();
				} else if (name.equals("Brake")) {
					acceleration = -(value * speedThruster);
					Vector3f acc = new Vector3f(0f, 0f, acceleration);

					vehicleControl.applyImpulse(vehicleControl.getPhysicsRotation().mult(acc), Vector3f.ZERO);
					boosterInputTime = timer.getTime();
				}
			}
		}, "MouseXleft", "MouseXright", "MouseYup", "MouseYdown", "Accelerate", "Brake");

	}

	/**
	 * These are our custom actions triggered by key presses. We do not walk yet, we just keep track of the direction
	 * the user pressed.
	 */
	public void onAction(String binding, boolean value, float tpf) {
		if (binding.equals("GibGas")) {
			if (value)
				velocityInputTime = timer.getTime();
			else
				velocityInputTime = 0;
		} else if (binding.equals("Brems")) {
			if (value)
				velocityInputTime = -timer.getTime();
			else
				velocityInputTime = 0;

		}

		// TODO: Hier normalen Antrieb mit fester Geschwindigkeit einbauen

		// float acc = 0f;
		// if (binding.equals("Forward")) {
		// acc = 50f;
		// }
		// if (binding.equals("Backward")) {
		// acc = -50f;
		// }
		// System.out.println(max);
		//
		// vehicleControl.setLinearVelocity(vehicleControl.getPhysicsRotation().mult(new Vector3f(0f, 0f, acc)));

		// System.out.println(binding + "  " + value);

		// System.out.println(cam.getLocation());
		// System.out.println(vehicleControl.getPhysicsLocation());
	}

	/**
	 * This is the main event loop--walking happens here. We check in which direction the player is walking by
	 * interpreting the camera direction forward (camDir) and to the side (camLeft). The setWalkDirection() command is
	 * what lets a physics-controlled player walk. We also make sure here that the camera moves with player.
	 */
	@Override
	public void simpleUpdate(float tpf) {
		if ((timer.getTime() - lastTimer) > (inputDelay)) {
			lastTimer = timer.getTime();
			if (velocityInputTime > 0)
				targetSpeed += 0.5f;
			else if (velocityInputTime < 0)
				targetSpeed -= 0.5f;

			// Velocity
			Vector3f currentVelocity = new Vector3f(vehicleControl.getLinearVelocity());
			Quaternion currentRotation = new Quaternion(vehicleControl.getPhysicsRotation());

			Vector3f targetVelocity = currentRotation.mult(new Vector3f(0f, 0f, targetSpeed));

			float xDiff = currentVelocity.x - targetVelocity.x;
			float zDiff = currentVelocity.z - targetVelocity.z;
			Vector3f diffVector = new Vector3f(xDiff, 0f, zDiff);

			// diffVector nach z drehen
			diffVector = currentRotation.inverse().mult(diffVector);

			float zThrust = 0f, yThrust, xThrust = 0f;
			float engine = .1f, thrust = .1f;

			if (diffVector.z < 0) { // accelerate
				zThrust = (-diffVector.z > engine) ? engine : -diffVector.z;
			} else if (diffVector.z > 0) { // brake
				zThrust = (diffVector.z > engine) ? -engine : -diffVector.z;
			}

			if (diffVector.x < 0) { // accelerate
				xThrust = (-diffVector.x > engine) ? engine : -diffVector.x;
			} else if (diffVector.x > 0) { // brake
				xThrust = (diffVector.x > engine) ? -engine : -diffVector.x;
			}

			Vector3f hemisphereVector = new Vector3f(0f, 0f, 1f);
			hemisphereVector = currentRotation.multLocal(hemisphereVector);

			xThrust = (hemisphereVector.x > 0) ? xThrust : xThrust;
			zThrust = (hemisphereVector.z > 0) ? zThrust : zThrust;

			Vector3f impulse = new Vector3f(xThrust, 0f, zThrust);

			vehicleControl.applyImpulse(currentRotation.mult(impulse), Vector3f.ZERO);

			// // Velocity

			// System.out.println(vehicleControl.getPhysicsRotation().mult(v));
			// System.out.println("tar: " + targetVelocity);
			// System.out.println("cur: " + currentVelocity);
			// System.out.println(zThrust);
			// System.out.println(v);

			// Quaternion quatOrig = new Quaternion(vehicleControl.getPhysicsRotation());
			// float invMass = vehicleControl.getObjectId().getInvMass();
			//
			// // Bewegungsvektor bei gleicher Geschwindigkeit
			// Vector3f intpolActVel = quatOrig.mult(origVelocity);
			// float speed = intpolActVel.length();
			//
			// // falls speed nicht schnell genug, draufrechnen
			// Vector3f newImpuls = quatOrig.mult(new Vector3f(0f, 0f, 0.1f));
			// Vector3f intpolVel = origVelocity.scaleAdd(invMass, newImpuls, origVelocity);
			//
			// if (!intpolVel.equals(origVelocity)) {
			// float thrust = .1f;
			// float engineThrust = .1f;
			// float xThrust = 0f;
			// float yThrust = 0f;
			// float zThrust = 0f;
			//
			// if (origVelocity.z > 0) {
			// if (intpolVel.z > origVelocity.z) {
			// float diff = intpolVel.z - origVelocity.z;
			// if (diff > engineThrust)
			// zThrust = engineThrust;
			// else
			// zThrust = diff;
			// } else {
			// float diff = origVelocity.z - intpolVel.z;
			// if (diff > engineThrust)
			// zThrust = -engineThrust;
			// else
			// zThrust = -diff;
			// }
			// } else {
			// if (intpolVel.z > origVelocity.z) {
			// float diff = intpolVel.z - origVelocity.z;
			// if (diff > engineThrust)
			// zThrust = engineThrust;
			// else
			// zThrust = diff;
			// } else {
			// float diff = origVelocity.z - intpolVel.z;
			// if (diff > engineThrust)
			// zThrust = -engineThrust;
			// else
			// zThrust = -diff;
			// }
			// }
			//

			// if (velocity.length() < targetSpeed) {
			// float thrustNeeded = targetSpeed - velocity.length();
			// if (thrustNeeded < thrust)
			// thrust = thrustNeeded;
			// vehicleControl.applyImpulse(quatOrig.mult(new Vector3f(0f, 0f, thrust)), Vector3f.ZERO);
			// } else if (velocity.length() > targetSpeed) {
			// float thrustNeeded = -targetSpeed - velocity.length();
			// if (thrustNeeded < thrust)
			// thrust = thrustNeeded;
			// vehicleControl.applyImpulse(quatOrig.mult(new Vector3f(0f, 0f, -thrust)), Vector3f.ZERO);
			// }
			//
			// System.out.println("meins: " + intpolVel);
			// System.out.println("real: " + vehicleControl.getLinearVelocity());
			// System.out.println("vel: " + targetVelocity);
			// }
		}

		// if ((timer.getTime() - boosterInputTime) > (inputDelay)) {
		// if ((timer.getTime() - lastBoosterDamping) > dampingInterval) {
		// float dampingFactor = 0.5f;
		//
		// // System.out.println("1234");
		// Vector3f vel = vehicleControl.getLinearVelocity();
		//
		// float aenderung = dampingFactor;
		// float grenze = 10f;
		//
		// if (vel.z > 0) {
		// if (vel.z / aenderung > grenze)
		// vel.z = -dampingFactor;
		// else {
		// float bla = (vel.z / aenderung);
		// float df = (dampingFactor / grenze) * bla;
		// vel.z = -df;
		// }
		// } else {
		// if (vel.z / aenderung < -grenze) {
		// vel.z = dampingFactor;
		// } else {
		// float bla = (vel.z / aenderung);
		// float df = (dampingFactor / grenze) * bla;
		// vel.z = -df;
		// }
		// }
		//
		// vehicleControl.applyImpulse(vel, Vector3f.ZERO);
		// // System.out.println(vehicleControl.getLinearVelocity().z + "    " + vel.z);
		// // System.out.println("sdfasdf");
		//
		// // if (vel.x > 0) {
		// // if (vel.x < dampingFactor) {
		// // vel.x = -vel.x;
		// // } else {
		// // vel.x = -dampingFactor;
		// // }
		// // } else {
		// // if (vel.x < -dampingFactor) {
		// // vel.x = dampingFactor;
		// // } else {
		// // vel.x = -vel.x;
		// // }
		// // }
		// //
		// // if (vel.y > 0) {
		// // if (vel.y < dampingFactor) {
		// // vel.y = -vel.y;
		// // } else {
		// // vel.y = -dampingFactor;
		// // }
		// // } else {
		// // if (vel.y < -dampingFactor) {
		// // vel.y = dampingFactor;
		// // } else {
		// // vel.y = -vel.y;
		// // }
		// // }
		//
		// lastBoosterDamping = timer.getTime();
		//
		// // System.out.println(vehicleControl.getLinearVelocity());
		// }
		// }

		if ((timer.getTime() - angularInputTime) > (inputDelay)) {
			if ((timer.getTime() - lastAngularDamping) > dampingInterval) {
				Vector3f vec = vehicleControl.getAngularVelocity();
				float dampingFactor = .1f;

				// ////////////
				Matrix3f mat = new Matrix3f();
				vehicleControl.getObjectId().getInvInertiaTensorWorld(mat);
				javax.vecmath.Vector3f vector3f = new javax.vecmath.Vector3f(new float[] { 0f, dampingFactor, 0f });
				mat.transform(vector3f);
				javax.vecmath.Vector3f interpolatedVec = new javax.vecmath.Vector3f();
				vehicleControl.getObjectId().getAngularVelocity(interpolatedVec).add(vector3f);
				// ////////////

				// diff berechnen (pos)
				float wall = 5.0f;
				float absSpeed = Math.abs(vec.y);

				float diff = Math.abs(vec.y - interpolatedVec.y);

				float yTorque = 0f;
				if (absSpeed > diff * wall) { // prüfen ob geschwindigkeit(pos) > "diff" * wall
					// // fullimpuls (-geschwindigkeit)
					yTorque = dampingFactor;
				} else {
					// // teilimpuls (-geschwindigkeit)
					float bla = absSpeed / (diff * wall);
					yTorque = bla * dampingFactor;
				}
				if (vec.y > 0)
					yTorque = -yTorque;

				vehicleControl.applyTorqueImpulse(vehicleControl.getPhysicsRotation().mult(
						new Vector3f(0.0f, yTorque, 0.0f)));

				lastAngularDamping = timer.getTime();

				// if (vec.y < 0) {
				//
				// float changedY = -vec.y + ((vector3f.y < 0) ? -vector3f.y : vector3f.y);
				// float wall = 2.0f;
				//
				// if (-vec.y > changedY * wall) {
				// vehicleControl.applyTorqueImpulse(vehicleControl.getPhysicsRotation().mult(
				// new Vector3f(0.0f, dampingFactor, 0.0f)));
				// System.out.println("AAAAAAAAAAAAAAAAAAAAA");
				// } else {
				// float bla = (-vec.y / (changedY * wall));
				// float df = bla * dampingFactor;
				//
				// vehicleControl.applyTorqueImpulse(vehicleControl.getPhysicsRotation().mult(
				// new Vector3f(0.0f, df, 0.0f)));
				// System.out.println(df);
				// }
				//
				// lastAngularDamping = timer.getTime();
				// //
				// } else if (vec.y > 0) {
				// float aenderung = 0f;
				// aenderung = vec.y - vector3f.y;
				//
				// // if (vec.y / aenderung > 1.5) {
				// // vehicleControl.applyTorqueImpulse(vehicleControl.getPhysicsRotation().mult(
				// // new Vector3f(0.0f, dampingFactor, 0.0f)));
				// // } else {
				// // float bla = (vec.y / aenderung);
				// // float df = (dampingFactor / 1.5f) * bla;
				// //
				// // vehicleControl.applyTorqueImpulse(vehicleControl.getPhysicsRotation().mult(
				// // new Vector3f(0.0f, df, 0.0f)));
				// // }
				// //
				//
				// // lastAngularDamping = timer.getTime();
				// }
			}
		}

		cam.setLocation(vehicleControl.getPhysicsLocation());
		cam.setRotation(vehicleControl.getPhysicsRotation());

	}
}

