/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
<<<<<<< HEAD
package com.badlogic.gdx.scenes.scene2d;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

/**
 * <p>
 * A Stage is a container for {@link Actor}s and handles distributing touch events, animating Actors and asking them to render
 * themselves. A Stage is basically a 2D scenegraph with hierarchies of Actors.
 * </p>
 * 
 * <p>
 * A Stage object fills the whole screen. It has a width and height given in device independent pixels. It will create a
 * {@link Camera} that maps this viewport to the given real screen resolution. If the stretched attribute is set to true then
 * the viewport is enforced no matter the difference in aspect ratio between the stage object and the screen dimensions. In case
 * stretch is disabled then the viewport is extended in the bigger screen dimensions.
 * </p>
 * 
 * <p>
 * Actors have a z-order which is equal to the order they were inserted into this Stage. Actors inserted later will be drawn on
 * top of Actors added earlier. Touch events that will get distributed to later Actors first.
 * </p>
 * 
 * <p>Actors can get focused. When your game pauses and resumes make sure to call the {@link Stage#unfocusAll()} method so 
 * that the focus states get reset for each pointer id. You also have to make sure that the Actors that were focused reset
 * their state if the depend on being focused, e.g. wait for a touch up event. An easier way to tackle this is to recreate the 
 * Stage if possible.</p>
 * 
 * @author mzechner
 * 
 */
public class Stage extends InputAdapter implements Disposable {
	protected float width;
	protected float height;
	protected float centerX;
	protected float centerY;
	protected boolean stretch;

	protected final Group root;

	protected final SpriteBatch batch;	
	protected Camera camera;

	/**
	 * <p>
	 * Constructs a new Stage object with the given dimensions. If the device resolution does not equal the Stage objects
	 * dimensions the stage object will setup a projection matrix to guarantee a fixed coordinate system. If stretch is disabled
	 * then the bigger dimension of the Stage will be increased to accomodate the actual device resolution.
	 * </p>
	 * 
	 * @param width the width of the viewport
	 * @param height the height of the viewport
	 * @param stretch whether to stretch the viewport to the real device resolution
	 */
	public Stage (float width, float height, boolean stretch) {
		this.width = width;
		this.height = height;
		this.stretch = stretch;
		this.root = new Group("root");
		this.batch = new SpriteBatch();
		this.camera = new OrthographicCamera();		
		setViewport(width, height, stretch);
	}

	/**
	 * Sets the viewport dimensions in device independent pixels. If stretch is false and the viewport aspect ratio is not equal to
	 * the device ratio then the bigger dimension of the viewport will be extended (device independent pixels stay quardatic
	 * instead of getting stretched).
	 * 
	 * @param width thew width of the viewport in device independent pixels
	 * @param height the height of the viewport in device independent pixels
	 * @param stretch whether to stretch the viewport or not
	 */
	public void setViewport (float width, float height, boolean stretch) {
		if (!stretch) {
			if (width > height && width/(float)Gdx.graphics.getWidth() <= height/(float)Gdx.graphics.getHeight()) {
				float toDeviceSpace = Gdx.graphics.getHeight() / height;
				float toViewportSpace = height / Gdx.graphics.getHeight();

				float deviceWidth = width * toDeviceSpace;
				this.width = width + (Gdx.graphics.getWidth() - deviceWidth) * toViewportSpace;
				this.height = height;
			} else {
				float toDeviceSpace = Gdx.graphics.getWidth() / width;
				float toViewportSpace = width / Gdx.graphics.getWidth();

				float deviceHeight = height * toDeviceSpace;
				this.height = height + (Gdx.graphics.getHeight() - deviceHeight) * toViewportSpace;
				this.width = width;
=======

package com.badlogic.gdx.scenes.scene2d;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener.FocusEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.SnapshotArray;

/** A 2D scenegraph containing hierarchies of {@link Actor actors}. Stage handles the viewport and distributing input events.
 * <p>
 * A stage fills the whole screen. {@link #setViewport} controls the coordinates used within the stage and sets up the camera used
 * to convert between stage coordinates and screen coordinates. *
 * <p>
 * A stage must receive input events so it can distribute them to actors. This is typically done by passing the stage to
 * {@link Input#setInputProcessor(com.badlogic.gdx.InputProcessor) Gdx.input.setInputProcessor}. An {@link InputMultiplexer} may be
 * used to handle input events before or after the stage does. If an actor handles an event by returning true from the input
 * method, then the stage's input method will also return true, causing subsequent InputProcessors to not receive the event.
 * @author mzechner
 * @author Nathan Sweet */
public class Stage extends InputAdapter implements Disposable {
	private float width, height;
	private float gutterWidth, gutterHeight;
	private float centerX, centerY;
	private Camera camera;
	private final SpriteBatch batch;
	private final boolean ownsBatch;
	private Group root;
	private final Vector2 stageCoords = new Vector2();
	private Actor[] pointerOverActors = new Actor[20];
	private boolean[] pointerTouched = new boolean[20];
	private int[] pointerScreenX = new int[20];
	private int[] pointerScreenY = new int[20];
	private int mouseScreenX, mouseScreenY;
	private Actor mouseOverActor;
	private Actor keyboardFocus, scrollFocus;
	private SnapshotArray<TouchFocus> touchFocuses = new SnapshotArray(true, 4, TouchFocus.class);

	/** Creates a stage with a {@link #setViewport(float, float, boolean) viewport} equal to the device screen resolution. The stage
	 * will use its own {@link SpriteBatch}. */
	public Stage () {
		this(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
	}

	/** Creates a stage with the specified {@link #setViewport(float, float, boolean) viewport}. The stage will use its own
	 * {@link SpriteBatch}, which will be disposed when the stage is disposed. */
	public Stage (float width, float height, boolean keepAspectRatio) {
		batch = new SpriteBatch();
		ownsBatch = true;
		initialize(width, height, keepAspectRatio);
	}

	/** Creates a stage with the specified {@link #setViewport(float, float, boolean) viewport} and {@link SpriteBatch}. This can be
	 * used to avoid creating a new SpriteBatch (which can be somewhat slow) if multiple stages are used during an applications
	 * life time.
	 * @param batch Will not be disposed if {@link #dispose()} is called. Handle disposal yourself. */
	public Stage (float width, float height, boolean keepAspectRatio, SpriteBatch batch) {
		this.batch = batch;
		ownsBatch = false;
		initialize(width, height, keepAspectRatio);
	}

	private void initialize (float width, float height, boolean keepAspectRatio) {
		this.width = width;
		this.height = height;

		root = new Group();
		root.setStage(this);

		camera = new OrthographicCamera();
		setViewport(width, height, keepAspectRatio);
	}

	/** Sets the dimensions of the stage's viewport. The viewport covers the entire screen. If keepAspectRatio is false, the
	 * viewport is simply stretched to the screen resolution, which may distort the aspect ratio. If keepAspectRatio is true, the
	 * viewport is first scaled to fit then the shorter dimension is lengthened to fill the screen, which keeps the aspect ratio
	 * from changing. The {@link #getGutterWidth()} and {@link #getGutterHeight()} provide access to the amount that was
	 * lengthened. */
	public void setViewport (float width, float height, boolean keepAspectRatio) {
		if (keepAspectRatio) {
			float screenWidth = Gdx.graphics.getWidth();
			float screenHeight = Gdx.graphics.getHeight();
			if (screenHeight / screenWidth < height / width) {
				float toScreenSpace = screenHeight / height;
				float toViewportSpace = height / screenHeight;
				float deviceWidth = width * toScreenSpace;
				float lengthen = (screenWidth - deviceWidth) * toViewportSpace;
				this.width = width + lengthen;
				this.height = height;
				gutterWidth = lengthen / 2;
				gutterHeight = 0;
			} else {
				float toScreenSpace = screenWidth / width;
				float toViewportSpace = width / screenWidth;
				float deviceHeight = height * toScreenSpace;
				float lengthen = (screenHeight - deviceHeight) * toViewportSpace;
				this.height = height + lengthen;
				this.width = width;
				gutterWidth = 0;
				gutterHeight = lengthen / 2;
>>>>>>> 0b8f0822f1b4e9813ecd083ada35c3ed92a6f861
			}
		} else {
			this.width = width;
			this.height = height;
<<<<<<< HEAD
		}

		this.stretch = stretch;
		centerX = width / 2;
		centerY = height / 2;

		camera.position.set(centerX, centerY, 0);
		camera.viewportWidth = this.width;
		camera.viewportHeight = this.height;		
	}

	/**8
	 * @return the width of the stage in dips
	 */
	public float width () {
		return width;
	}

	/**
	 * @return the height of the stage in dips
	 */
	public float height () {
		return height;
	}

	/**
	 * @return the x-coordinate of the left edge of the stage in dips
	 */
	public int left () {
		return 0;
	}

	/**
	 * @return the x-coordinate of the right edge of the stage in dips
	 */
	public float right () {
		return width - 1;
	}

	/**
	 * @return the y-coordinate of the top edge of the stage in dips
	 */
	public float top () {
		return height - 1;
	}

	/**
	 * @return the y-coordinate of the bottom edge of the stage in dips
	 */
	public float bottom () {
		return 0;
	}

	/**
	 * @return the center x-coordinate of the stage in dips
	 */
	public float centerX () {
		return centerX;
	}

	/**
	 * @return the center y-coordinate of the stage in dips
	 */
	public float centerY () {
		return centerY;
	}

	/**
	 * @return whether the stage is stretched
	 */
	public boolean isStretched () {
		return stretch;
	}

	/**
	 * Finds the {@link Actor} with the given name in the stage hierarchy.
	 * @param name
	 * @return the Actor or null if it couldn't be found.
	 */
	public Actor findActor (String name) {
		return root.findActor(name);
	}

	/**
	 * @return all top level {@link Actor}s
	 */
	public List<Actor> getActors () {
		return root.getActors();
	}

	/**
	 * @return all top level {@link Group}s
	 */
	public List<Group> getGroups () {
		return root.getGroups();
	}

	final Vector2 point = new Vector2();
	final Vector2 coords = new Vector2();

	/**
	 * Call this to distribute a touch down event to the stage.
	 * @param x the x coordinate of the touch in screen coordinates
	 * @param y the y coordinate of the touch in screen coordinates
	 * @param pointer the pointer index
	 * @param button the button that's been pressed
	 * @return whether an {@link Actor} in the scene processed the event or not
	 */
	@Override
	public boolean touchDown (int x, int y, int pointer, int button) {
		toStageCoordinates(x, y, coords);
		Group.toChildCoordinates(root, coords.x, coords.y, point);
		return root.touchDown(point.x, point.y, pointer);
	}

	/**
	 * Call this to distribute a touch Up event to the stage.
	 * 
	 * @param x the x coordinate of the touch in screen coordinates
	 * @param y the y coordinate of the touch in screen coordinates
	 * @param pointer the pointer index
	 * @return whether an {@link Actor} in the scene processed the event or not
	 */
	@Override
	public boolean touchUp (int x, int y, int pointer, int button) {
		toStageCoordinates(x, y, coords);
		Group.toChildCoordinates(root, coords.x, coords.y, point);
		return root.touchUp(point.x, point.y, pointer);
	}

	/**
	 * Call this to distribute a touch dragged event to the stage.
	 * @param x the x coordinate of the touch in screen coordinates
	 * @param y the y coordinate of the touch in screen coordinates
	 * @param pointer the pointer index
	 * @return whether an {@link Actor} in the scene processed the event or not
	 */
	@Override
	public boolean touchDragged (int x, int y, int pointer) {
		toStageCoordinates(x, y, coords);
		Group.toChildCoordinates(root, coords.x, coords.y, point);
		return root.touchDragged(point.x, point.y, pointer);
	}
	
	/**
	 * Call this to distribute a touch moved event to the stage. This event will
	 * only ever appear on the desktop.
	 * @param x the x coordinate of the touch in screen coordinates
	 * @param y the y coordinate of the touch in screen coordinates
	 * @return whether an {@link Actor} in the scene processed the event or not
	 */
	@Override
	public boolean touchMoved(int x, int y) {
		toStageCoordinates(x, y, coords);
		Group.toChildCoordinates(root, coords.x, coords.y, point);
		return root.touchMoved(point.x, point.y);
	}
	
	/**
	 * Call this to distribute a mouse scroll event to the stage. This event will
	 * only ever appear on the desktop.
	 * @param amount the scroll amount.
	 * @return whether an {@link Actor} in the scene processed the event or not.
	 */
	@Override
	public boolean scrolled(int amount) {		
		return root.scrolled(amount);
	}
	
	/**
	 * Called when a key was pressed
	 * 
	 * @param keycode one of the constants in {@link Keys}
	 * @return whether the input was processed
	 */
	@Override
	public boolean keyDown (int keycode) {
		return root.keyDown(keycode);
	}

	/**
	 * Called when a key was released
	 * 
	 * @param keycode one of the constants in {@link Keys}
	 * @return whether the input was processed
	 */
	@Override
	public boolean keyUp (int keycode) {
		return root.keyUp(keycode);
	}

	/**
	 * Called when a key was typed
	 * 
	 * @param character The character
	 * @return whether the input was processed
	 */
	@Override
	public boolean keyTyped (char character) {
		return root.keyTyped(character);
	}

	/**
	 * Calls the {@link Actor#act(float)} method of all contained Actors. This will advance any {@link Action}s active for an
	 * Actor.
	 * @param delta the delta time in seconds since the last invocation
	 */
	public void act (float delta) {
		root.act(delta);
	}

	/**
	 * Renders the stage
	 */
	public void draw () {
		camera.update();
		batch.setProjectionMatrix(camera.combined);		
		batch.begin();
		root.draw(batch, 1);
		batch.end();
	}

	/**
	 * Disposes the stage
	 */
	public void dispose () {
		batch.dispose();
	}

	/**
	 * Adds an {@link Actor} to this stage
	 * @param actor the Actor
	 */
	public void addActor (Actor actor) {
		root.addActor(actor);
	}

	/**
	 * @return the Stage graph as a silly string
	 */
	public String graphToString () {
		StringBuilder buffer = new StringBuilder();
		graphToString(buffer, root, 0);
		return buffer.toString();
	}

	private void graphToString (StringBuilder buffer, Actor actor, int level) {
		for (int i = 0; i < level; i++)
			buffer.append(' ');

		buffer.append(actor);
		buffer.append("\n");

		if (actor instanceof Group) {
			Group group = (Group)actor;
			for (int i = 0; i < group.getActors().size(); i++)
				graphToString(buffer, group.getActors().get(i), level + 1);
		}
	}

	/**
	 * @return the root {@link Group} of this Stage.
	 */
	public Group getRoot () {
		return root;
	}

	/**
	 * @return the {@link SpriteBatch} offers its {@link Actor}s for rendering.
	 */
	public SpriteBatch getSpriteBatch () {
		return batch;
	}
	
	/**	 
	 * @return the {@link Camera} of this stage.
	 */
	public Camera getCamera() {
		return camera;
	}
	
	/**
	 * Sets the {@link Camera} this stage uses. You are responsible for setting
	 * it up properly! The {@link Stage#draw()} will call the Camera's update() method
	 * and use it's combined matrix as the projection matrix for the SpriteBatch.
	 * @param camera the {@link Camera}
	 */
	public void setCamera(Camera camera) {
		this.camera = camera;
	}

	/**
	 * @return the {@link Actor} last hit by a touch event.
	 */
	public Actor getLastTouchedChild () {
		return root.lastTouchedChild;
	}

	/**
	 * Returns the {@link Actor} intersecting with the point (x,y) in stage coordinates. Hit testing is performed in the order the
	 * Actors were inserted into the Stage, last inserted Actors being tested first. To get stage coordinates from screen
	 * coordinates use {@link #toStageCoordinates(int, int, Vector2)}.
	 * 
	 * @param x the x-coordinate in stage coordinates
	 * @param y the y-coordinate in stage coordinates
	 * @return the hit Actor or null
	 */
	public Actor hit (float x, float y) {
		Group.toChildCoordinates(root, x, y, point);
		return root.hit(point.x, point.y);
	}

	final Vector3 tmp = new Vector3();
	/**
	 * Transforms the given screen coordinates to stage coordinates
	 * @param x the x-coordinate in screen coordinates
	 * @param y the y-coordinate in screen coordinates
	 * @param out the output {@link Vector2}.
	 */
	public void toStageCoordinates (int x, int y, Vector2 out) {
		camera.unproject(tmp.set(x, y, 0));
		out.x = tmp.x;
		out.y = tmp.y;
	}
	
	/**
	 * Clears this stage, removing all {@link Actor}s and {@link Group}s.
	 */
	public void clear() {
		root.clear();
	}
	
	/**
	 * Removes the given {@link Actor} from the stage by trying to find it
	 * recursively in the scenegraph.
	 * @param actor the actor
	 */
	public void removeActor(Actor actor) {
		root.removeActorRecursive(actor);
	}
	
	/**
	 * Unfocues all {@link Actor} instance currently focused. You should 
	 * call this in case your app resumes to clear up any pressed states.
	 * Make sure the Actors forget their states as well!
	 */
	public void unfocusAll() {
		root.unfocusAll();
	}	
=======
			gutterWidth = 0;
			gutterHeight = 0;
		}

		centerX = this.width / 2;
		centerY = this.height / 2;

		camera.position.set(centerX, centerY, 0);
		camera.viewportWidth = this.width;
		camera.viewportHeight = this.height;
	}

	public void draw () {
		camera.update();
		if (!root.isVisible()) return;
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		root.draw(batch, 1);
		batch.end();
	}

	/** Calls {@link #act(float)} with {@link Graphics#getDeltaTime()}. */
	public void act () {
		act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
	}

	/** Calls the {@link Actor#act(float)} method on each actor in the stage. Typically called each frame. This method also fires
	 * enter and exit events.
	 * @param delta Time in seconds since the last frame. */
	public void act (float delta) {
		// Update over actors. Done in act() because actors may change position, which can fire enter/exit without an input event.
		for (int pointer = 0, n = pointerOverActors.length; pointer < n; pointer++) {
			Actor overLast = pointerOverActors[pointer];
			// Check if pointer is gone.
			if (!pointerTouched[pointer]) {
				if (overLast != null) {
					pointerOverActors[pointer] = null;
					screenToStageCoordinates(stageCoords.set(pointerScreenX[pointer], pointerScreenY[pointer]));
					// Exit over last.
					InputEvent event = Pools.obtain(InputEvent.class);
					event.setType(InputEvent.Type.exit);
					event.setStage(this);
					event.setStageX(stageCoords.x);
					event.setStageY(stageCoords.y);
					event.setRelatedActor(overLast);
					event.setPointer(pointer);
					overLast.fire(event);
					Pools.free(event);
				}
				continue;
			}
			// Update over actor for the pointer.
			pointerOverActors[pointer] = fireEnterAndExit(overLast, pointerScreenX[pointer], pointerScreenY[pointer], pointer);
		}
		// Update over actor for the mouse on the desktop.
		ApplicationType type = Gdx.app.getType();
		if (type == ApplicationType.Desktop || type == ApplicationType.Applet || type == ApplicationType.WebGL)
			mouseOverActor = fireEnterAndExit(mouseOverActor, mouseScreenX, mouseScreenY, -1);

		root.act(delta);
	}

	private Actor fireEnterAndExit (Actor overLast, int screenX, int screenY, int pointer) {
		// Find the actor under the point.
		screenToStageCoordinates(stageCoords.set(screenX, screenY));
		Actor over = hit(stageCoords.x, stageCoords.y, true);
		if (over == overLast) return overLast;

		InputEvent event = Pools.obtain(InputEvent.class);
		event.setStage(this);
		event.setStageX(stageCoords.x);
		event.setStageY(stageCoords.y);
		event.setPointer(pointer);
		// Exit overLast.
		if (overLast != null) {
			event.setType(InputEvent.Type.exit);
			event.setRelatedActor(over);
			overLast.fire(event);
		}
		// Enter over.
		if (over != null) {
			event.setType(InputEvent.Type.enter);
			event.setRelatedActor(overLast);
			over.fire(event);
		}
		Pools.free(event);
		return over;
	}

	/** Applies a touch down event to the stage and returns true if an actor in the scene {@link Event#handle() handled} the event. */
	public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		pointerTouched[pointer] = true;
		pointerScreenX[pointer] = screenX;
		pointerScreenY[pointer] = screenY;

		screenToStageCoordinates(stageCoords.set(screenX, screenY));

		InputEvent event = Pools.obtain(InputEvent.class);
		event.setType(Type.touchDown);
		event.setStage(this);
		event.setStageX(stageCoords.x);
		event.setStageY(stageCoords.y);
		event.setPointer(pointer);
		event.setButton(button);

		Actor target = hit(stageCoords.x, stageCoords.y, true);
		if (target == null) target = root;

		target.fire(event);
		boolean handled = event.isHandled();
		Pools.free(event);
		return handled;
	}

	/** Applies a touch moved event to the stage and returns true if an actor in the scene {@link Event#handle() handled} the event.
	 * Only {@link InputListener listeners} that returned true for touchDown will receive this event. */
	public boolean touchDragged (int screenX, int screenY, int pointer) {
		pointerScreenX[pointer] = screenX;
		pointerScreenY[pointer] = screenY;

		if (touchFocuses.size == 0) return false;

		screenToStageCoordinates(stageCoords.set(screenX, screenY));

		InputEvent event = Pools.obtain(InputEvent.class);
		event.setType(Type.touchDragged);
		event.setStage(this);
		event.setStageX(stageCoords.x);
		event.setStageY(stageCoords.y);
		event.setPointer(pointer);

		SnapshotArray<TouchFocus> touchFocuses = this.touchFocuses;
		TouchFocus[] focuses = touchFocuses.begin();
		for (int i = 0, n = touchFocuses.size; i < n; i++) {
			TouchFocus focus = focuses[i];
			if (focus.pointer != pointer) continue;
			event.setTarget(focus.target);
			event.setListenerActor(focus.listenerActor);
			if (focus.listener.handle(event)) event.handle();
		}
		touchFocuses.end();

		boolean handled = event.isHandled();
		Pools.free(event);
		return handled;
	}

	/** Applies a touch up event to the stage and returns true if an actor in the scene {@link Event#handle() handled} the event.
	 * Only {@link InputListener listeners} that returned true for touchDown will receive this event. */
	public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		pointerTouched[pointer] = false;
		pointerScreenX[pointer] = screenX;
		pointerScreenY[pointer] = screenY;

		if (touchFocuses.size == 0) return false;

		screenToStageCoordinates(stageCoords.set(screenX, screenY));

		InputEvent event = Pools.obtain(InputEvent.class);
		event.setType(Type.touchUp);
		event.setStage(this);
		event.setStageX(stageCoords.x);
		event.setStageY(stageCoords.y);
		event.setPointer(pointer);
		event.setButton(button);

		SnapshotArray<TouchFocus> touchFocuses = this.touchFocuses;
		TouchFocus[] focuses = touchFocuses.begin();
		for (int i = 0, n = touchFocuses.size; i < n; i++) {
			TouchFocus focus = focuses[i];
			if (focus.pointer != pointer || focus.button != button) continue;
			if (!touchFocuses.removeValue(focus, true)) continue; // Touch focus already gone.
			event.setTarget(focus.target);
			event.setListenerActor(focus.listenerActor);
			if (focus.listener.handle(event)) event.handle();
			Pools.free(focus);
		}
		touchFocuses.end();

		boolean handled = event.isHandled();
		Pools.free(event);
		return handled;
	}

	/** Applies a mouse moved event to the stage and returns true if an actor in the scene {@link Event#handle() handled} the event.
	 * This event only occurs on the desktop. */
	public boolean mouseMoved (int screenX, int screenY) {
		mouseScreenX = screenX;
		mouseScreenY = screenY;

		screenToStageCoordinates(stageCoords.set(screenX, screenY));

		InputEvent event = Pools.obtain(InputEvent.class);
		event.setStage(this);
		event.setType(Type.mouseMoved);
		event.setStageX(stageCoords.x);
		event.setStageY(stageCoords.y);

		Actor target = hit(stageCoords.x, stageCoords.y, true);
		if (target == null) target = root;

		target.fire(event);
		boolean handled = event.isHandled();
		Pools.free(event);
		return handled;
	}

	/** Applies a mouse scroll event to the stage and returns true if an actor in the scene {@link Event#handle() handled} the
	 * event. This event only occurs on the desktop. */
	public boolean scrolled (int amount) {
		Actor target = scrollFocus == null ? root : scrollFocus;

		screenToStageCoordinates(stageCoords.set(mouseScreenX, mouseScreenY));

		InputEvent event = Pools.obtain(InputEvent.class);
		event.setStage(this);
		event.setType(InputEvent.Type.scrolled);
		event.setScrollAmount(amount);
		event.setStageX(stageCoords.x);
		event.setStageY(stageCoords.y);
		target.fire(event);
		boolean handled = event.isHandled();
		Pools.free(event);
		return handled;
	}

	/** Applies a key down event to the actor that has {@link Stage#setKeyboardFocus(Actor) keyboard focus}, if any, and returns
	 * true if the event was {@link Event#handle() handled}. */
	public boolean keyDown (int keyCode) {
		Actor target = keyboardFocus == null ? root : keyboardFocus;
		InputEvent event = Pools.obtain(InputEvent.class);
		event.setStage(this);
		event.setType(InputEvent.Type.keyDown);
		event.setKeyCode(keyCode);
		target.fire(event);
		boolean handled = event.isHandled();
		Pools.free(event);
		return handled;
	}

	/** Applies a key up event to the actor that has {@link Stage#setKeyboardFocus(Actor) keyboard focus}, if any, and returns true
	 * if the event was {@link Event#handle() handled}. */
	public boolean keyUp (int keyCode) {
		Actor target = keyboardFocus == null ? root : keyboardFocus;
		InputEvent event = Pools.obtain(InputEvent.class);
		event.setStage(this);
		event.setType(InputEvent.Type.keyUp);
		event.setKeyCode(keyCode);
		target.fire(event);
		boolean handled = event.isHandled();
		Pools.free(event);
		return handled;
	}

	/** Applies a key typed event to the actor that has {@link Stage#setKeyboardFocus(Actor) keyboard focus}, if any, and returns
	 * true if the event was {@link Event#handle() handled}. */
	public boolean keyTyped (char character) {
		Actor target = keyboardFocus == null ? root : keyboardFocus;
		InputEvent event = Pools.obtain(InputEvent.class);
		event.setStage(this);
		event.setType(InputEvent.Type.keyTyped);
		event.setCharacter(character);
		target.fire(event);
		boolean handled = event.isHandled();
		Pools.free(event);
		return handled;
	}

	/** Adds the listener to be notified for all touchDragged and touchUp events for the specified pointer and button. The actor
	 * will be used as the {@link Event#getListenerActor() listener actor} and {@link Event#getTarget() target}. */
	public void addTouchFocus (EventListener listener, Actor listenerActor, Actor target, int pointer, int button) {
		TouchFocus focus = Pools.obtain(TouchFocus.class);
		focus.listenerActor = listenerActor;
		focus.target = target;
		focus.listener = listener;
		focus.pointer = pointer;
		focus.button = button;
		touchFocuses.add(focus);
	}

	/** Removes the listener from being notified for all touchDragged and touchUp events for the specified pointer and button. Note
	 * the listener may never receive a touchUp event if this method is used. */
	public void removeTouchFocus (EventListener listener, Actor listenerActor, Actor target, int pointer, int button) {
		SnapshotArray<TouchFocus> touchFocuses = this.touchFocuses;
		for (int i = touchFocuses.size - 1; i >= 0; i--) {
			TouchFocus focus = touchFocuses.get(i);
			if (focus.listener == listener && focus.listenerActor == listenerActor && focus.target == target
				&& focus.pointer == pointer && focus.button == button) {
				touchFocuses.removeIndex(i);
				Pools.free(focus);
			}
		}
	}

	/** Sends a touchUp event to all listeners that are registered to receive touchDragged and touchUp events and removes their
	 * touch focus. The location of the touchUp is {@link Integer#MIN_VALUE}. This method removes all touch focus listeners, but
	 * sends a touchUp event so that the state of the listeners remains consistent (listeners typically expect to receive touchUp
	 * eventually). */
	public void cancelTouchFocus () {
		cancelTouchFocus(null, null);
	}

	/** Cancels touch focus for all listeners except the specified listener.
	 * @see #cancelTouchFocus() */
	public void cancelTouchFocus (EventListener listener, Actor actor) {
		InputEvent event = Pools.obtain(InputEvent.class);
		event.setStage(this);
		event.setType(InputEvent.Type.touchUp);
		event.setStageX(Integer.MIN_VALUE);
		event.setStageY(Integer.MIN_VALUE);

		// Cancel all current touch focuses except for the specified listener, allowing for concurrent modification, and never
		// cancel the same focus twice.
		SnapshotArray<TouchFocus> touchFocuses = this.touchFocuses;
		TouchFocus[] items = touchFocuses.begin();
		for (int i = 0, n = touchFocuses.size; i < n; i++) {
			TouchFocus focus = items[i];
			if (focus.listener == listener && focus.listenerActor == actor) continue;
			if (!touchFocuses.removeValue(focus, true)) continue; // Touch focus already gone.
			event.setTarget(focus.target);
			event.setListenerActor(focus.listenerActor);
			event.setPointer(focus.pointer);
			event.setButton(focus.button);
			focus.listener.handle(event);
			// Cannot return TouchFocus to pool, as it may still be in use (eg if cancelTouchFocus is called from touchDragged).
		}
		touchFocuses.end();

		Pools.free(event);
	}

	/** Adds an actor to the root of the stage.
	 * @see Group#addActor(Actor) */
	public void addActor (Actor actor) {
		root.addActor(actor);
	}

	/** Adds an action to the root of the stage.
	 * @see Group#addAction(Action) */
	public void addAction (Action action) {
		root.addAction(action);
	}

	/** Returns the root's child actors.
	 * @see Group#getChildren() */
	public Array<Actor> getActors () {
		return root.getChildren();
	}

	/** Adds a listener to the root.
	 * @see Actor#addListener(EventListener) */
	public boolean addListener (EventListener listener) {
		return root.addListener(listener);
	}

	/** Removes a listener from the root.
	 * @see Actor#removeListener(EventListener) */
	public boolean removeListener (EventListener listener) {
		return root.removeListener(listener);
	}

	/** Adds a capture listener to the root.
	 * @see Actor#addCaptureListener(EventListener) */
	public boolean addCaptureListener (EventListener listener) {
		return root.addCaptureListener(listener);
	}

	/** Removes a listener from the root.
	 * @see Actor#removeCaptureListener(EventListener) */
	public boolean removeCaptureListener (EventListener listener) {
		return root.removeCaptureListener(listener);
	}

	/** Clears the stage, removing all actors. */
	public void clear () {
		unfocusAll();
		root.clear();
	}

	/** Removes the touch, keyboard, and scroll focused actors. */
	public void unfocusAll () {
		scrollFocus = null;
		keyboardFocus = null;
		cancelTouchFocus();
	}

	/** Removes the touch, keyboard, and scroll focus for the specified actor. */
	public void unfocus (Actor actor) {
		if (scrollFocus == actor) scrollFocus = null;
		if (keyboardFocus == actor) keyboardFocus = null;
	}

	/** Sets the actor that will receive key events.
	 * @param actor May be null. */
	public void setKeyboardFocus (Actor actor) {
		if (keyboardFocus == actor) return;
		FocusEvent event = Pools.obtain(FocusEvent.class);
		event.setStage(this);
		event.setType(FocusEvent.Type.keyboard);
		if (keyboardFocus != null) {
			event.setFocused(false);
			keyboardFocus.fire(event);
		}
		keyboardFocus = actor;
		if (keyboardFocus != null) {
			event.setFocused(true);
			keyboardFocus.fire(event);
		}
		Pools.free(event);
	}

	/** Gets the actor that will receive key events.
	 * @return May be null. */
	public Actor getKeyboardFocus () {
		return keyboardFocus;
	}

	/** Sets the actor that will receive scroll events.
	 * @param actor May be null. */
	public void setScrollFocus (Actor actor) {
		if (scrollFocus == actor) return;
		FocusEvent event = Pools.obtain(FocusEvent.class);
		event.setStage(this);
		event.setType(FocusEvent.Type.scroll);
		if (scrollFocus != null) {
			event.setFocused(false);
			scrollFocus.fire(event);
		}
		scrollFocus = actor;
		if (scrollFocus != null) {
			event.setFocused(true);
			scrollFocus.fire(event);
		}
		Pools.free(event);
	}

	/** Gets the actor that will receive scroll events.
	 * @return May be null. */
	public Actor getScrollFocus () {
		return scrollFocus;
	}

	/** The width of the stage's viewport.
	 * @see #setViewport(float, float, boolean) */
	public float getWidth () {
		return width;
	}

	/** The height of the stage's viewport.
	 * @see #setViewport(float, float, boolean) */
	public float getHeight () {
		return height;
	}

	/** Half the amount in the x direction that the stage's viewport was lengthened to fill the screen.
	 * @see #setViewport(float, float, boolean) */
	public float getGutterWidth () {
		return gutterWidth;
	}

	/** Half the amount in the y direction that the stage's viewport was lengthened to fill the screen.
	 * @see #setViewport(float, float, boolean) */
	public float getGutterHeight () {
		return gutterHeight;
	}

	public SpriteBatch getSpriteBatch () {
		return batch;
	}

	public Camera getCamera () {
		return camera;
	}

	/** Sets the stage's camera. The camera must be configured properly or {@link #setViewport(float, float, boolean)} can be called
	 * after the camera is set. {@link Stage#draw()} will call {@link Camera#update()} and use the {@link Camera#combined} matrix
	 * for the SpriteBatch {@link SpriteBatch#setProjectionMatrix(com.badlogic.gdx.math.Matrix4) projection matrix}. */
	public void setCamera (Camera camera) {
		this.camera = camera;
	}

	/** Returns the root group which holds all actors in the stage. */
	public Group getRoot () {
		return root;
	}

	/** Returns the {@link Actor} at the specified location in stage coordinates. Hit testing is performed in the order the actors
	 * were inserted into the stage, last inserted actors being tested first. To get stage coordinates from screen coordinates, use
	 * {@link #screenToStageCoordinates(Vector2)}.
	 * @param touchable If true, the hit detection will respect the {@link Actor#setTouchable(Touchable) touchability}.
	 * @return May be null if no actor was hit. */
	public Actor hit (float stageX, float stageY, boolean touchable) {
		Vector2 actorCoords = Vector2.tmp;
		root.parentToLocalCoordinates(actorCoords.set(stageX, stageY));
		return root.hit(actorCoords.x, actorCoords.y, touchable);
	}

	/** Transforms the screen coordinates to stage coordinates.
	 * @param screenCoords Stores the result. */
	public Vector2 screenToStageCoordinates (Vector2 screenCoords) {
		camera.unproject(Vector3.tmp.set(screenCoords.x, screenCoords.y, 0));
		screenCoords.x = Vector3.tmp.x;
		screenCoords.y = Vector3.tmp.y;
		return screenCoords;
	}

	/** Transforms the stage coordinates to screen coordinates. */
	public Vector2 stageToScreenCoordinates (Vector2 stageCoords) {
		Vector3.tmp.set(stageCoords.x, stageCoords.y, 0);
		camera.project(Vector3.tmp);
		stageCoords.x = Vector3.tmp.x;
		stageCoords.y = Vector3.tmp.y;
		return stageCoords;
	}

	/** Transforms the coordinates to screen coordinates. The coordinates can be anywhere in the stage since the transform matrix
	 * describes how to convert them. The transform matrix is typically obtained from {@link SpriteBatch#getTransformMatrix()}. */
	public Vector2 toScreenCoordinates (Vector2 coords, Matrix4 transformMatrix) {
		ScissorStack.toWindowCoordinates(camera, transformMatrix, coords);
		return coords;
	}

	public void dispose () {
		if (ownsBatch) batch.dispose();
	}

	/** Internal class for managing touch focus. Public only for GWT.
	 * @author Nathan Sweet */
	public static final class TouchFocus implements Poolable {
		EventListener listener;
		Actor listenerActor, target;
		int pointer, button;

		public void reset () {
			listenerActor = null;
			listener = null;
		}
	}
>>>>>>> 0b8f0822f1b4e9813ecd083ada35c3ed92a6f861
}

