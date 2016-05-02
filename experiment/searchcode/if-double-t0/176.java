package net.tortuga.level.map.entities;


import java.util.Random;
import java.util.Set;

import net.tortuga.annotations.Unimplemented;
import net.tortuga.level.map.TurtleMap;
import net.tortuga.level.map.entities.mobile.EntityBlock;
import net.tortuga.level.map.tiles.MapTile;
import net.tortuga.level.map.tiles.mechanisms.TileElevator;
import net.tortuga.textures.TxQuad;
import net.tortuga.util.AnimDouble;
import net.tortuga.util.Log;
import net.tortuga.util.RenderUtils;

import com.porcupine.color.RGB;
import com.porcupine.coord.Coord;
import com.porcupine.coord.CoordI;
import com.porcupine.coord.Rect;
import com.porcupine.math.Calc;


/**
 * Base class for map entities
 * 
 * @author OndĹej HruĹĄka (MightyPork)
 */
public abstract class Entity {

	/**
	 * Hook called when entity is added to map
	 */
	public void onAddedToMap()
	{
		tryToFall();
	}

	// animation constants

	/** default horizontal animation time */
	public static final double ANIM_TIME_HORIZONTAL = 0.8;

	/** default rotate animation time */
	public static final double ANIM_TIME_ROTATE = 0.7;

	/** default horizontal slide animation time */
	public static final double ANIM_TIME_SLIDE = 0.4; //0.36;

	/** Random number generator */
	public static Random rand = new Random();

	/** Flag that entity is dead and should be removed from list */
	private boolean dead = false;

	/** Flag that this entity is carried on a block. */
	public boolean isCarried;

	/** Flag that this entity was rendered during a render loop */
	public boolean rendered = false;

	/** Entity center position (tile in map) */
	public CoordI pos = new CoordI(0, 0, 0);

	/** The entity's map */
	public TurtleMap map;

	// movement variables

	/** Entity facing direction */
	public MoveDir direction = MoveDir.EAST;

	/** Direction of movement */
	public MoveDir moveDir = MoveDir.EAST;

	/** Last move direction (may be used) */
	public MoveDir moveDirLast = MoveDir.EAST;

	/** Entity move progress (0-1) */
	public AnimDouble moveProgress = new AnimDouble(0);

	/** Array of doubles indicating move trigger points */
	private double[] moveTriggers = new double[0];

	/** Array of flags that move triggers were passed */
	private boolean[] moveTriggersPassed = new boolean[0];

	/** Flag that entity is moving forward */
	public boolean inMotion = false;

	/** Status that the block is sliding */
	public boolean isSliding = false;

	/** Number of steps falling (gets cleared on floor) */
	public int stepsFalling = 0;

	/**
	 * Flag that shadow shall be rendered right under the entity, not at the
	 * nearest non-transparent cube
	 */
	public boolean useImmediateShadow = false;

	/** Direction of rotation */
	public RotateDir rotateDir = RotateDir.CW;

	/** Flag that entity is in rotation */
	public boolean inRotation = false;

	/** Entity rotation progress (0-1) */
	public AnimDouble rotateProgress = new AnimDouble(0);


	/**
	 * Set immediate shadow flag - false = normal, true = shadow fixed to entity
	 * 
	 * @param state shadow immediate
	 */
	public void setImmediateShadow(boolean state)
	{
		useImmediateShadow = state;
	}


	/**
	 * Animate fall
	 */
	public void animateFall()
	{
		moveProgress.setTo(0);
		moveProgress.animIn(getAnimTimeFall(stepsFalling));
	}


	/**
	 * Animate horizontal walking step
	 */
	public void animateHorizontal()
	{
		moveProgress.setTo(0);
		moveProgress.animIn(getAnimTimeHorizontal());
	}


	/**
	 * Animate rotation
	 */
	public void animateRotate()
	{
		rotateProgress.setTo(0);
		rotateProgress.animIn(getAnimTimeRotate());
	}


	/**
	 * Animate horizontal slide
	 */
	public void animateSlide()
	{
		moveProgress.setTo(0);
		moveProgress.animIn(getAnimTimeSlide());
	}


	/**
	 * Get if the entity can legally go to a given direction
	 * 
	 * @param dir probed direction
	 * @return can go there
	 */
	public boolean canGoToDir(MoveDir dir)
	{
		CoordI coord = getCoord();

		CoordI move = dir.getMoveVector();

		CoordI posFront = coord.add(move);
		CoordI posFront2 = coord.add(move.mul(2, 2));
		MapTile tileFront = map.getTile(posFront);

		// non-solid block or air
		if (tileFront == null) return true;

		if (tileFront instanceof TileElevator) {
			Set<Entity> ents = map.getEntitiesAtCoord(posFront);
			if (!ents.isEmpty()) return false; // && !((TileElevator)tileFront).inMotion;
		}

		if (!tileFront.isSolidForCollision(map, posFront)) return true;

		if (isSliding) return false;

		if (!canPushBlocks()) return false;

		// try to push.

		// get tile past front tile (should be empty)
		MapTile tileFront2 = map.getTile(posFront2);

		if (tileFront2 == null || tileFront2.canPushBlocksInto(map, posFront2)) {
			CoordI posFrontUp = coord.add(move).add_ip(0, 0, 1); // above front
			MapTile tileFrontUp = map.getTile(posFrontUp);
			if (tileFront.canBePushed(map, posFront) && tileFrontUp == null) {
				// return true, if there is not a GOAL mark on top of this tile
				return !map.getGoal().equals(posFrontUp);

			} else {
				return false;
			}
		}

		return false;
	}


	/**
	 * Get if this entity can push blocks
	 * 
	 * @return can actively push blocks
	 */
	public abstract boolean canPushBlocks();


	/**
	 * Get a deep copy
	 * 
	 * @return copy
	 */
	public Entity copy()
	{
		try {
			return getClass().newInstance().copyFrom(this);
		} catch (Exception e) {
			Log.e(e);
			return null;
		}
	}


	/**
	 * Fill this entity with data from other, copied entity.
	 * 
	 * @param copied
	 * @return this
	 */
	public Entity copyFrom(Entity copied)
	{
		dead = copied.dead;
		direction = copied.direction;
		pos = copied.pos.copy();

		inMotion = copied.inMotion;
		moveProgress = copied.moveProgress.copy();
		moveDir = copied.moveDir;

		inRotation = copied.inRotation;
		rotateProgress = copied.rotateProgress.copy();
		rotateDir = copied.rotateDir;
		return this;
	}


	/**
	 * Get anim time for fall
	 * 
	 * @param steps steps already fallen (for acceleration)
	 * @return time (s)
	 */
	public double getAnimTimeFall(int steps)
	{
		double a = 300;
		double s = (1 + steps) * 10;
		double t0 = Math.sqrt(2 * a * (s));
		double t1 = Math.sqrt(2 * a * (s + 10));
		double time = (t1 - t0) / 140;
		return time;
	}


	/**
	 * Get length of side-move animation
	 * 
	 * @return time in secs
	 */
	public double getAnimTimeHorizontal()
	{
		return ANIM_TIME_HORIZONTAL;
	}


	/**
	 * Get length of rotate animation
	 * 
	 * @return time in secs
	 */
	public double getAnimTimeRotate()
	{
		return ANIM_TIME_ROTATE;
	}


	/**
	 * Get length of side-move animation [SLIDING - ON ICE]
	 * 
	 * @return time in secs
	 */
	public double getAnimTimeSlide()
	{
		return ANIM_TIME_SLIDE;
	}


	/**
	 * Get currect tile coord (COPY)
	 * 
	 * @return coord
	 */
	public CoordI getCoord()
	{
		return pos.copy();
	}


	/**
	 * Get coord at move target
	 * 
	 * @return tile coord
	 */
	public CoordI getCoordMoveTarget()
	{
		CoordI old = pos.copy();
		if (!inMotion) return old;

		switch (moveDir) {
			case NORTH:
				return old.add(0, -1, 0);
			case SOUTH:
				return old.add(0, 1, 0);
			case WEST:
				return old.add(-1, 0, 0);
			case EAST:
				return old.add(1, 0, 0);
			case DOWN:
				return old.add(0, 0, -1);
			case UP:
				return old.add(0, 0, 1);
		}

		return old;
	}


	private CoordI getCoordOriginShadow(TurtleMap map)
	{
		CoordI originShadow = getCoord();

		boolean originShadowFound = false;

		// find tile to cast the origin shadow on
		originShadow.z--;
		for (; originShadow.z >= 0; originShadow.z--) {
			MapTile tile = map.getTile(originShadow);
			if (tile != null && tile.isShadowSurface(map, originShadow)) {
				originShadowFound = true;
				originShadow.z++;
				break;
			}
		}

		return originShadowFound ? originShadow : null;
	}


	private CoordI getCoordTargetShadow(TurtleMap map)
	{
		CoordI targetShadow = getCoordMoveTarget();

		boolean targetShadowFound = false;

		// find tile to cast the origin shadow on
		targetShadow.z--;
		for (; targetShadow.z >= 0; targetShadow.z--) {
			MapTile tile = map.getTile(targetShadow);
			if (tile != null && tile.isShadowSurface(map, targetShadow)) {
				targetShadowFound = true;
				targetShadow.z++;
				break;
			}
		}

		return targetShadowFound ? targetShadow : null;
	}


	/**
	 * Get a draw rect (Rect)
	 * 
	 * @param mapMin map min render coord
	 * @param texture texture used for size - is aligned bottom left
	 * @param addZ flag whether 32xZ should be aded - disable to get map bottom
	 * @return the rect
	 */
	protected Rect getDrawRect(Coord mapMin, TxQuad texture, boolean addZ)
	{
		CoordI renderPos = getRenderCoord();

		int minX = (int) (mapMin.x + renderPos.x * 64);
		int minY = (int) (mapMin.y + (map.getSize().y - 1) * 64 - renderPos.y * 64 + (addZ ? 1 : 0) * renderPos.z * 32);

		Rect drawRect = Rect.fromSize(minX, minY, texture.size.toCoordI());
		if (inMotion) {
			switch (moveDir) {
				case NORTH:
					drawRect.add_ip(0, 64D * moveProgress.delta());
					break;

				case SOUTH:
					drawRect.add_ip(0, 64D * (1 - moveProgress.delta()));
					break;

				case EAST:
					drawRect.add_ip(-64D * (1D - moveProgress.delta()), 0);
					break;

				case WEST:
					drawRect.add_ip(-64D * moveProgress.delta(), 0);
					break;

				case UP:
					if (addZ) drawRect.add_ip(0, 32D * moveProgress.delta());
					break;

				case DOWN:
					if (addZ) drawRect.add_ip(0, 32D * (1 - moveProgress.delta()));
					break;
			}
		}

		return drawRect;
	}


	/**
	 * Get alpha for sprite
	 * 
	 * @return alpha
	 */
	public double getAlphaSprite()
	{
		return isCarried ? 0.6 : 1;
	}


	/**
	 * Get alpha for shadow
	 * 
	 * @return alpha
	 */
	public double getAlphaShadow()
	{
		return getAlphaSprite();
	}


	/**
	 * Get coord for render order (which tile this entity should be rendered at)
	 * 
	 * @return tile coord
	 */
	public CoordI getRenderCoord()
	{
		CoordI old = pos.copy();
		if (!inMotion) return old;

		switch (moveDir) {
			case NORTH:
				return old;
			case SOUTH:
				return old.add(0, 1, 0);
			case WEST:
				return old;
			case EAST:
				return old.add(1, 0, 0);
			case DOWN:
				return old.add(0, 0, -1);
			case UP:
				return old;
		}

		return old;
	}


	/**
	 * Get texture quad of the current sprite frame<br>
	 * Texture width must be 64, will be aligned to tile left bottom corner:
	 * 64x64 tile, 64x96 block
	 * 
	 * @return texture quad
	 */
	public abstract TxQuad getSpriteFrame();


	/**
	 * Get shadow render offset (y negative = down)
	 * 
	 * @return offset
	 */
	public Coord getSpriteOffset()
	{
		return new Coord(0, 0);
	}


	/**
	 * Get texture quad of the shadow of the current sprite frame<br>
	 * Texture width must be 64, does not have to be the same size as Sprite
	 * Frame
	 * 
	 * @return texture quad of the shadow
	 */
	public abstract TxQuad getSpriteShadowFrame();


	/**
	 * Get tile behind this entity
	 * 
	 * @return tile behind
	 */
	public MapTile getTileBack()
	{
		return map.getTile(getCoord().sub_ip(direction.getMoveVector()));
	}


	/**
	 * Get tile under this entity
	 * 
	 * @return tile under
	 */
	public MapTile getTileDown()
	{
		return map.getTile(getCoord().add_ip(0, 0, -1));
	}


	/**
	 * Get tile in front of this entity
	 * 
	 * @return tile forward
	 */
	public MapTile getTileFront()
	{
		return map.getTile(getCoord().add_ip(direction.getMoveVector()));
	}


	/**
	 * Get tile front down
	 * 
	 * @return tile front down
	 */
	public MapTile getTileFrontDown()
	{
		return map.getTile(getCoord().add_ip(0, 0, -1).add_ip(direction.getMoveVector()));
	}


	/**
	 * Animate movement to direction - if last animation is finished and
	 * movement is legal
	 * 
	 * @param dir move dir
	 */
	public void goToDir(MoveDir dir)
	{
		if (!canGoToDir(dir)) {
			Log.w("Can't go to dir " + dir + " at " + Calc.cname(this));
			return;
		}

		if (!isMoveFinished()) {
			Log.w("Entity " + Calc.cname(this) + " is still in motion, can't start another move.");
		} else {
			changeMoveDir(dir);

			initMoveTriggers();

			if (dir.isVertical()) {
				stepsFalling++;
				animateFall();
				changeMoveDir(MoveDir.DOWN);
				inMotion = true;
			} else {
				CoordI front = getCoord();
				front.add_ip(dir.getMoveVector());
				MapTile tileFront = map.getTile(front);
				boolean pushing = false;
				if (tileFront != null && canPushBlocks() && !isSliding && tileFront.canBePushed(map, front) && tileFront.isSolidForCollision(map, front)) {
					// start pushing
					map.setTile(front, null);
					EntityBlock e = new EntityBlock(tileFront);
					e.setPos(front);
					map.addEntity(e);
					e.goToDir(dir);
					e.onMoveStarted();
					pushing = true;

				}

				CoordI frontDown = getCoord().add(dir.getMoveVector()).add_ip(0, 0, -1);
				MapTile tileFrontDown = map.getTile(frontDown);
				//MapTile down = getTileDown();
				if (tileFrontDown != null && tileFrontDown.isSlipperly(map, frontDown) && !pushing) {
					isSliding = true;
					animateSlide();
				} else {
					isSliding = false;
					animateHorizontal();
				}
			}
			inMotion = true;
			onMoveStarted();
		}
	}


	/**
	 * Get if shadow should be rendered
	 * 
	 * @return has shadow
	 */
	public abstract boolean hasShadow();


	/**
	 * Change move direction, store old dir in moveDirLast if different from
	 * new.
	 * 
	 * @param newMoveDir new dir
	 */
	public void changeMoveDir(MoveDir newMoveDir)
	{
		if (newMoveDir != moveDir) {
			moveDirLast = moveDir;
		}
		moveDir = newMoveDir;
	}


	/**
	 * Get if this entity is dead
	 * 
	 * @return is dead
	 */
	public boolean isDead()
	{
		return dead;
	}


	/**
	 * Get if entity movement is finished
	 * 
	 * @return is finished
	 */
	public boolean isMoveFinished()
	{
		return !inMotion && !inRotation;
	}


	/**
	 * Called when a "movement trigger" was reached
	 * 
	 * @param i number of trigger passed
	 */
	@Unimplemented
	public void onMoveTrigger(int i)
	{}


	/**
	 * Try to slide on ice
	 * 
	 * @return is sliding
	 */
	public boolean onStopMove_tryToSlide()
	{
		CoordI downPos = getCoord().add(0, 0, -1);
		MapTile down = getTileDown();

		isSliding = true; // set, because of checks in canGoToDir
		if (down.isSlipperly(map, downPos) && !moveDir.isVertical() && canGoToDir(moveDir)) {
			animateSlide();
			inMotion = true;

			initMoveTriggers();

			onMoveStarted();
			return true;
		} else {
			isSliding = false;
			return false;
		}
	}


	/**
	 * Try to fall!
	 */
	public void tryToFall()
	{
		if (inMotion || isCarried) return;
		onStopMove_tryToFall();
	}


	/**
	 * Check if the turtle should fall, start falling if needed.
	 * 
	 * @return is falling or was in motion
	 */
	public boolean onStopMove_tryToFall()
	{
		if (inMotion) return true;

		CoordI downPos = getCoord().add(0, 0, -1);
		MapTile down = getTileDown();
		if (down == null || !down.isSolidForCollision(map, downPos)) {
			goToDir(MoveDir.DOWN);
			return true;
		} else {
			stepsFalling = 0;
			return false;
		}
	}


	/**
	 * Check if the turtle is out of map, set death if true.
	 * 
	 * @return is now dead
	 */
	public boolean onStopMove_checkDieFall()
	{
		if (getCoord().z < -10) {
			Log.f3("Entity '" + Calc.cname(this) + "' died.");
			setDead();
			return true;
		}
		return false;
	}


	/**
	 * Called when movement animation is finished
	 */
	public void onStopMove()
	{
		isSliding = false;

		if (onStopMove_checkDieFall()) return;
		if (isCarried) return;

		if (onStopMove_tryToFall()) return;

		onStopMove_tryToSlide();
	}


	/**
	 * Called when rotation animation is finished
	 */
	public void onStopRotation()
	{
		tryToFall();
	}


	/**
	 * Render the entity
	 * 
	 * @param mapMin min map render coord
	 */
	public void render(Coord mapMin)
	{
		if (isDead()) return;

		// current sprite tile				
		if (hasShadow()) renderShadow(mapMin);

		renderSprite(mapMin);
	}


	@SuppressWarnings("null")
	// wrong detect
	private final void renderShadow(Coord mapMin)
	{
		RGB color = new RGB(RGB.WHITE, getAlphaCalculated(getAlphaShadow()));

		TxQuad shadow = getSpriteShadowFrame();
		if (shadow.size.xi() != 64 || shadow.size.yi() != 64) {
			Log.w("Shadow texture should be 64x64 px in " + Calc.cname(this));
		}

		Rect drawRect = getDrawRect(mapMin, shadow, false);

		//Coord min = new Coord(, y)
		Rect shadowRect = Rect.fromSize(drawRect.getMin(), shadow.size);

		// rendering part of the shadow which is on the last solid tile
		CoordI posStart = getCoordOriginShadow(map);
		CoordI posEnd = getCoordTargetShadow(map);

		boolean hasStart = (posStart != null);
		boolean hasEnd = (posEnd != null);

		// if immediate mode
		if (useImmediateShadow) {
			drawRect = getDrawRect(mapMin, shadow, true);
			shadowRect = Rect.fromSize(drawRect.getMin(), shadow.size);
			RenderUtils.quadTextured(shadowRect, shadow, color);
			return;
		}

		// if static or shadows the same
		if (!inMotion || (hasStart && hasEnd && posStart.z == posEnd.z)) {
			if (hasStart) {
				RenderUtils.quadTextured(shadowRect.add(0, (posStart.z) * 32), shadow, color);
			} else if (hasEnd) {
				RenderUtils.quadTextured(shadowRect.add(0, (posEnd.z) * 32), shadow, color);
			}

			return;
		}

		// ORIGIN SHADOW
		do {
			if (hasStart) {
				boolean southUp = false;
				boolean southDown = false;
				if (hasEnd && moveDir == MoveDir.SOUTH) {
					if (posEnd.z - posStart.z > 1) break; // double step, up = no shadow visible.
					southUp = posEnd.z - posStart.z > 0;
				}

				if (hasEnd && moveDir == MoveDir.SOUTH) {
					southDown = posStart.z - posEnd.z > 0;
				}

				if (!hasEnd && moveDir == MoveDir.SOUTH) {
					southDown = true; // off the map
				}

				CoordI originPos = getCoord();
				int minX2 = (int) (mapMin.x + originPos.x * 64);
				int minY2 = (int) (mapMin.y + (map.getSize().y - 1) * 64 - originPos.y * 64);

				Rect rect = Rect.fromSize(new Coord(minX2, minY2), 64, 64).add(0, (posStart.z) * 32);
				TxQuad tx = shadow.copy();

				boolean elevatorFix = false;
				double cut = (64) * moveProgress.delta();
				if (map.getTile(originPos.sub(0, 0, 1)) instanceof TileElevator) {
//					if(moveDir == MoveDir.NORTH) {
//						rect.growUp_ip(-8);
//						tx.size.y -= 8;
//						tx.uvs.growUp_ip(-8);
//					}
					if (moveDir == MoveDir.SOUTH) {
						rect.growDown_ip(-4);
						tx.size.y -= 4;
						tx.uvs.growUp_ip(-4);
					} else if (moveDir == MoveDir.NORTH) {
						rect.growUp_ip(-8);
						tx.size.y -= 8;
						tx.uvs.growDown_ip(-8);
						elevatorFix = true;
					} else if (moveDir == MoveDir.EAST) {
						rect.growRight_ip(-8);
						tx.size.x -= 8;
						tx.uvs.growRight_ip(-8);
						elevatorFix = true;
					} else if (moveDir == MoveDir.WEST) {
						rect.growLeft_ip(-8);
						tx.size.x -= 8;
						tx.uvs.growLeft_ip(-8);
						elevatorFix = true;
					}

					// TODO east west
				}
				switch (moveDir) {
					case NORTH:
						rect.growDown_ip(-cut);
						tx.size.y -= cut;
						tx.uvs.growDown_ip(-cut);
						break;

					case SOUTH:
						rect.growUp_ip(-cut);
						if (southUp) rect.growDown_ip(-32);
						tx.size.y -= cut + (southUp ? 32 : 0);
						tx.uvs.growUp_ip(-(cut + (southUp ? 32 : 0)));
						break;

					case EAST:
						rect.growLeft_ip(-cut);
						tx.size.x -= cut;
						tx.uvs.growRight_ip(-cut);
						break;

					case WEST:
						rect.growRight_ip(-cut);
						tx.size.x -= cut;
						tx.uvs.growLeft_ip(-cut);
						break;

				}

				if (southUp && cut + 32 >= 64) {
					// dont render
				} else if (elevatorFix && cut + 8 >= 64) {
					// dont render
				} else {
					RenderUtils.quadTextured(rect, tx, color);

					if (southDown && map.getTile(originPos.sub(0, 0, 1)).isShadowSide(map, originPos)) {
						// render the side
						tx.size.y = 1;
						tx.uvs.growDown_ip(-tx.uvs.getSize().y - 1);

						rect = Rect.fromSize(new Coord(minX2, minY2), 64, 0).add(0, (posStart.z) * 32);
						rect.growDown_ip((posStart.z - (hasEnd ? posEnd.z : 0)) * 32);

						RenderUtils.quadTextured(rect, tx, color);
					}
				}
			}
		} while (false);

		// TARGET SHADOW
		if (hasEnd) {
			boolean northDown = false;
			boolean northUp = false;
			if (hasStart && moveDir == MoveDir.NORTH) {
				if (posStart.z - posEnd.z > 1) return; // double step = no shadow visible.
				northDown = posEnd.z - posStart.z == -1;
			}

			if (hasStart && moveDir == MoveDir.NORTH) {
				northUp = posEnd.z - posStart.z > 0;
			}

			if (!hasStart && moveDir == MoveDir.NORTH) {
				northUp = true;
			}

			CoordI targetPos = getCoordMoveTarget();
			int minX2 = (int) (mapMin.x + targetPos.x * 64);
			int minY2 = (int) (mapMin.y + (map.getSize().y - 1) * 64 - targetPos.y * 64);

			Rect rect = Rect.fromSize(new Coord(minX2, minY2), 64, 64).add(0, (posEnd.z) * 32);
			TxQuad tx = shadow.copy();

			double cut = 64 * (1 - moveProgress.delta());

			boolean elevatorFix = false;
			if (map.getTile(getCoord().sub(0, 0, 1)) instanceof TileElevator) {
				if (moveDir == MoveDir.SOUTH) {
					rect.growUp_ip(8);
					tx.size.y += 8;
					tx.uvs.growDown_ip(8);
				}

				if (moveDir == MoveDir.NORTH) {
					rect.growDown_ip(8);
					tx.size.y += 8;
					tx.uvs.growUp_ip(8);
					elevatorFix = true;
				}

				if (moveDir == MoveDir.EAST) {
					rect.growLeft_ip(8);
					tx.size.y += 8;
					tx.uvs.growLeft_ip(8);
					elevatorFix = true;
				}

				if (moveDir == MoveDir.WEST) {
					rect.growRight_ip(8);
					tx.size.y += 8;
					tx.uvs.growRight_ip(8);
					elevatorFix = true;
				}
			}
			switch (moveDir) {
				case NORTH:
					rect.growUp_ip(-cut);
					if (northDown) rect.growDown_ip(-32);
					tx.size.y -= cut + (northDown ? 32 : 0);
					tx.uvs.growUp_ip(-(cut + (northDown ? 32 : 0)));
					break;

				case SOUTH:
					rect.growDown_ip(-cut);
					tx.size.y -= cut;
					tx.uvs.growDown_ip(-cut);
					break;

				case WEST:
					rect.growLeft_ip(-cut);
					tx.size.x -= cut;
					tx.uvs.growRight_ip(-cut);
					break;

				case EAST:
					rect.growRight_ip(-cut);
					tx.size.x -= cut;
					tx.uvs.growLeft_ip(-cut);
					break;

			}

			if (rect.getSize().x > 64) {
				// prevent "repeating"
				tx = shadow.copy();

				if (moveDir == MoveDir.WEST) {
					rect.growRight_ip(64 - rect.getSize().x);
				}

				if (moveDir == MoveDir.EAST) {
					rect.growLeft_ip(64 - rect.getSize().x);
				}

			}

			if (elevatorFix && (cut + 8 >= 64)) {
				// dont render
			} else {
				RenderUtils.quadTextured(rect, tx, color);

				if (northUp && map.getTile(targetPos.sub(0, 0, 1)).isShadowSurface(map, targetPos)) {
					// render the side
					tx.size.y = 1;
					tx.uvs.growDown_ip(-tx.uvs.getSize().y - 1);

					rect = Rect.fromSize(new Coord(minX2, minY2), 64, 0).add(0, (posEnd.z) * 32);
					rect.growDown_ip((posEnd.z - (hasStart ? posStart.z : 0)) * 32);

					RenderUtils.quadTextured(rect, tx, color);
				}

			}
		}
	}


	/**
	 * Render the entity sprite (no shadows)
	 * 
	 * @param mapMin map min coord
	 */
	public void renderSprite(Coord mapMin)
	{
		TxQuad texture = getSpriteFrame();
		if (texture.size.xi() != 64) {
			Log.w("Sprite texture should be 64px wide in " + getClass().getSimpleName());
		}

		Rect drawRect = getDrawRect(mapMin, texture, true);

		RGB color = new RGB(RGB.WHITE, getAlphaCalculated(getAlphaSprite()));
		RenderUtils.quadTextured(drawRect.add_ip(getSpriteOffset()), texture, color);
	}


	private double getAlphaCalculated(double alpha)
	{
		if (getCoord().z <= 0) {
			double down = (inMotion && moveDir == MoveDir.DOWN) ? moveProgress.delta() : 0;
			double up = (inMotion && moveDir == MoveDir.UP) ? moveProgress.delta() : 0;
			return Calc.clampd((1 - (-pos.z + down - up) * 0.15D) * alpha, 0, 1);
		}

		double roof = map.getSize().z + 3;
		if (getCoord().z >= roof) {
			double down = (inMotion && moveDir == MoveDir.DOWN) ? moveProgress.delta() : 0;
			double up = (inMotion && moveDir == MoveDir.UP) ? moveProgress.delta() : 0;
			return Calc.clampd((1 - (pos.z - roof - down + up) * 0.5D) * alpha, 0, 1);
		}

		return alpha;
	}


	/**
	 * Kill entity
	 */
	public final void setDead()
	{
		dead = true;
	}


	/**
	 * Set a turtle map this entity is in
	 * 
	 * @param map the map
	 */
	public final void setMap(TurtleMap map)
	{
		this.map = map;
	}


	/**
	 * Set entity position
	 * 
	 * @param thePos new pos
	 * @return this
	 */
	public Entity setPos(CoordI thePos)
	{
		pos.setTo(thePos);
		return this;
	}


	/**
	 * Start animating right turn (CW)
	 */
	public void turnLeft()
	{
		if (!isMoveFinished()) {
			Log.w("Entity " + Calc.cname(this) + " is still in motion, can't start another move.");
		} else {
			animateRotate();
			rotateDir = RotateDir.CCW;
			inRotation = true;
			onRotationStarted();
		}
	}


	/**
	 * Start animating left turn (CCW)
	 */
	public void turnRight()
	{
		if (!isMoveFinished()) {
			Log.w("Entity " + Calc.cname(this) + " is still in motion, can't start another move.");
		} else {
			animateRotate();
			rotateDir = RotateDir.CW;
			inRotation = true;
			onRotationStarted();
		}
	}


	/**
	 * Update movement animations
	 * 
	 * @param delta delta time
	 */
	public void updatePos(double delta)
	{
		moveProgress.update(delta);
		rotateProgress.update(delta);

		// apply movement if finished
		if (inMotion && moveProgress.isFinished()) {
			// remember left tile
			CoordI posLast = pos.copy();
			MapTile tileLast = map.getTile(posLast);

			CoordI posLastUnder = pos.add(0, 0, -1);
			MapTile tileLastUnder = map.getTile(posLastUnder);

			// add movement increment to pos
			pos.add_ip(moveDir.getMoveVector());
			inMotion = false;

			// get entered tiles
			CoordI posNew = pos.copy();
			MapTile tileNew = map.getTile(posNew);

			CoordI posNewUnder = pos.add(0, 0, -1);
			MapTile tileNewUnder = map.getTile(posNewUnder);

			// call tile triggers
			if (tileLast != null) tileLast.onEntityLeave(map, posLast, this);
			if (tileLastUnder != null) tileLastUnder.onEntityStepOff(map, posLastUnder, this);

			if (tileNew != null) tileNew.onEntityEnter(map, posNew, this);
			if (tileNewUnder != null) tileNewUnder.onEntityStepOn(map, posNewUnder, this);

			onStopMove();
		}

		// apply rotation if finished
		if (inRotation && rotateProgress.isFinished()) {
			direction = direction.turn(rotateDir);

			inRotation = false;
			onStopRotation();
		}

		double progress = moveProgress.delta();
		for (int i = 0; i < moveTriggers.length; i++) {
			if (progress >= moveTriggers[i]) {
				if (moveTriggersPassed[i] == false) {
					onMoveTrigger(i);
					moveTriggersPassed[i] = true;
				}
			}
		}
	}


	/**
	 * Hook called when new move was just started
	 */
	@Unimplemented
	public void onMoveStarted()
	{}


	/**
	 * Hook called when rotation was started
	 */
	@Unimplemented
	public void onRotationStarted()
	{}


	/**
	 * Get trigger points for movement (0 to 1)
	 * 
	 * @return array of triggers
	 */
	@Unimplemented
	public double[] getMoveTriggerPoints()
	{
		return null;
	}


	/**
	 * Clear flags used for move triggers
	 */
	public void initMoveTriggers()
	{
		double[] triggers = getMoveTriggerPoints();

		if (triggers == null) {
			moveTriggers = new double[0];
			moveTriggersPassed = new boolean[0];
		} else {
			moveTriggers = triggers;
			moveTriggersPassed = new boolean[triggers.length];
		}

	}

}

