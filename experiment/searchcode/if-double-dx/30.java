package net.minecraft.src;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import net.minecraft.client.Minecraft;
import sirentropy.blocks.BlockSet;
import sirentropy.emob.EmobManager;
import sirentropy.emob.EmobPlayerControllerSP;
import sirentropy.emob.EmobQuickBB;
import sirentropy.emob.EntityWrapper;
import sirentropy.emob.State;
import sirentropy.emob.world.BlockCounter;
import sirentropy.emob.world.EmobSaveUtil;
import sirentropy.event.EventDispatcher;
import sirentropy.event.EventListener;
import sirentropy.physics.Actuator;
import sirentropy.util.CoordsUtil;
import sirentropy.util.MathUtil;
import sirentropy.util.Vector3i;
import sirentropy.util.WorldUtil;

/**
 * @author sirentropy
 * 
 */
public abstract class Emob extends Entity implements EventListener {

	protected State ALIVE = new State() {
		@Override
		public void onUpdate(Emob emob) {
			onAlive();
		}
	};

	protected State CONVERTING_BACK_TO_NORMAL_BLOCKS = new State() {
		@Override
		public void onUpdate(Emob emob) {
			onConvertingBackToNormalBlocks();
		}
	};

	protected State DYING = new State() {
		@Override
		public void onUpdate(Emob emob) {
			onDying();
		}
	};

	protected State ALIGNING = new State() {
		@Override
		public void onUpdate(Emob emob) {
			onAligning();
		}
	};

	protected State state = ALIVE;

	private Vec3 minMotion = Vec3.createVectorHelper(0, 0.001, 0);

	public EmobWorld emobWorld;
	private Set<Entity> entitiesAboard = new HashSet<Entity>();
	protected Minecraft mc;
	public MovingObjectPosition objectMouseOver;
	protected EmobPlayerControllerSP emobPlayerController;
	public EntityPlayerWrapper emobPlayerEntity;
	Vec3 motionAfterUpdate = Vec3.createVectorHelper(0, 0, 0);
	public Vec3 collidingEntityOffset = Vec3.createVectorHelper(0, 0, 0);
	private Vector3d posLastExplosion = new Vector3d(0, 0, 0);
	// protected boolean hasCoarseCollided;
	public RenderGlobal emobRender;
	private boolean needTweak = true;
	private String emobID;

	public Vector3f angularVelocity = new Vector3f();
	protected EventDispatcher eventManager = new EventDispatcher();
	protected Set<Actuator<?>> actuators = new LinkedHashSet<Actuator<?>>();

	private boolean needPlayerTweak = true;

	public Emob(World world) {
		super(world);
		mc = ModLoader.getMinecraftInstance();
		boundingBox = new EmobQuickBB(this, 0, 0, 0, 0, 0, 0);
		ignoreFrustumCheck = true;
		EmobManager.registerEmob(this);
		initEvents();
		initActuators();
		eventManager.registerEventListener(this);
		isImmuneToFire = true;
	}

	protected abstract void initEvents();

	public abstract void initActuators();

	public Emob(World world, int x0, int y0, int z0, int length, int height, int width) {
		this(world);
		createEmobWorld(world, x0, y0, z0, length, height, width);
		initPosAndMotion(x0, y0, z0, length, width);
		emobWorld.copyBlocksFromWorld(worldObj, x0, y0, z0, length, height, width);
	}

	public void initPosAndMotion(int x0, int y0, int z0, int length, int width) {
		double x = x0 + length / 2;
		double y = y0 - 2e-3;
		double z = z0 + width / 2;
		setPosition(x, y, z);
		motionX = 0.0D;
		motionY = 0.0D;
		motionZ = 0.0D;
		prevPosX = x;
		prevPosY = y;
		prevPosZ = z;
		// setPosition(x, y, z);
	}

	public Emob(World world, int x0, int y0, int z0, int length, int height, int width, Set<Vector3i> blocks) {
		this(world);
		createEmobWorld(world, x0, y0, z0, length, height, width, blocks);
		initPosAndMotion(x0, y0, z0, length, width);
		emobWorld.copyBlocksFromWorld(worldObj, x0, y0, z0, length, height, width, blocks);
	}

	public String getNewEmobID() {
		return UUID.randomUUID().toString();
	}

	private void createEmobWorld(World parentWorld, int x0, int y0, int z0, int length, int height,
			int width, Set<Vector3i> blocks) {
		emobID = getNewEmobID();
		createEmobWorld(emobID, parentWorld);
		emobWorld.updateSize(length, height, width);

	}

	public void createEmobWorld(World parentWorld, int x0, int y0, int z0, int length, int height, int width) {
		emobID = getNewEmobID();
		createEmobWorld(emobID, parentWorld);
		emobWorld.updateSize(length, height, width);

	}

	public void createEmobWorld(String emobID, World parentWorld) {
		emobWorld = new EmobWorld(emobID, this, parentWorld);
		emobRender = new EmobRenderGlobal(this);
		emobRender.changeWorld(emobWorld);
		// initPlayerWrapper();
	}

	public void updateSize() {
		height = getHeightBlocks();
		width = getWidthBlocks();
		updateBoundingBox();
	}

	private void setWorldMaxEntitySize(int l, int w) {
		int maxDimension = Math.max(l, w);
		if (maxDimension > World.MAX_ENTITY_RADIUS) {
			World.MAX_ENTITY_RADIUS = maxDimension;
		}
	}

	@Override
	public void setPosition(double x, double y, double z) {
		posX = x;
		posY = y;
		posZ = z;
		updateBoundingBox();
	}

	public void updateBoundingBox() {
		if (boundingBox != null && emobWorld != null) {
			Vector3d corner1 = emobWorld.emob.emobWorld.getLowerBackLeftCorner();
			Vector3d corner2 = emobWorld.emob.emobWorld.getUpperFrontRightCorner(corner1);
			Vector3d corner3 = new Vector3d(corner1.x, corner1.y, corner1.z + getWidthBlocks());
			Vector3d corner4 = new Vector3d(corner1.x + getLengthBlocks(), corner1.y, corner1.z);
			Vector3d corner1Parent = convertToPosInParentCoords(corner1);
			Vector3d corner2Parent = convertToPosInParentCoords(corner2);
			Vector3d corner3Parent = convertToPosInParentCoords(corner3);
			Vector3d corner4Parent = convertToPosInParentCoords(corner4);

			boundingBox.setBounds(MathUtil.min(corner1Parent.x, corner2Parent.x, corner3Parent.x, corner4Parent.x),
					posY, MathUtil.min(corner1Parent.z, corner2Parent.z, corner3Parent.z, corner4Parent.z),
					MathUtil.max(corner1Parent.x, corner2Parent.x, corner3Parent.x, corner4Parent.x), posY
							+ getHeightBlocks(),
					MathUtil.max(corner1Parent.z, corner2Parent.z, corner3Parent.z, corner4Parent.z));
		}
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		emobID = nbttagcompound.getString("vehicleID");
		EmobSaveUtil.convertToAnvil(emobID, worldObj);
		createEmobWorld(emobID, worldObj);
		emobWorld.updateSize(nbttagcompound.getInteger("lengthBlocks"),
				nbttagcompound.getInteger("heightBlocks"), nbttagcompound.getInteger("widthBlocks"));
		posY -= 0.2;
		setPosition(posX, posY, posZ);
		setMotionAfterUpdate();
		emobWorld.calculateNumBlocks();
		System.out.println("Loaded emob " + emobID);
		System.out.println(emobID + " " + emobWorld.getTotalBlocks() + " blocks");

	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setTag("Pos", newDoubleNBTList(new double[] { posX, posY, posZ }));
		nbttagcompound.setInteger("lengthBlocks", getLengthBlocks());
		nbttagcompound.setInteger("heightBlocks", getHeightBlocks());
		nbttagcompound.setInteger("widthBlocks", getWidthBlocks());
		nbttagcompound.setString("vehicleID", emobWorld.getEmobID());
		emobWorld.save();
		System.out.println("Saved emob " + emobID);
	}

	public int getWidthBlocks() {
		return emobWorld.getWidthBlocks();
	}

	public int getHeightBlocks() {
		return emobWorld.getHeightBlocks();
	}

	public int getLengthBlocks() {
		return emobWorld.getLengthBlocks();
	}

	public Vector3d parentToLocal(Entity entity) {
		return CoordsUtil.parentPosToLocal(getPosVector3d(entity), getRotVector3d(), getPosVector3d());
	}

	public Vector3d localToParent(Entity entity) {
		return CoordsUtil.localPosToParent(getPosVector3d(entity), getRotVector3d(), getPosVector3d());
	}

	public Vector3d getPosVector3d() {
		return getPosVector3d(this);
	}

	public static Vector3d getPosVector3d(Entity entity) {
		return new Vector3d((float) entity.posX, (float) entity.posY, (float) entity.posZ);
	}

	public Vector3f getPosVector3f() {
		return getPosVector3f(this);
	}

	public static Vector3f getPosVector3f(Entity entity) {
		return new Vector3f((float) entity.posX, (float) entity.posY, (float) entity.posZ);
	}

	public Vector3d getRotVector3d() {
		return getRotVector3d(this);
	}

	public static Vector3d getRotVector3d(Entity entity) {
		return new Vector3d(0F, entity.rotationYaw, entity.rotationPitch);
	}

	@Override
	public void onCollideWithPlayer(EntityPlayer entityplayer) {
	}

	public boolean canBeCollidedWith() {
		return true;
	}

	public AxisAlignedBB getCollisionBox(Entity entity) {
		return null;
	}

	public AxisAlignedBB getBoundingBox() {
		return boundingBox;
	}

	public boolean canBePushed() {
		return false;
	}


	public Vector3d convertToPosInLocalCoords(Vector3d posInParentCoords) {
		Vector3d posInLocalCoords = CoordsUtil.parentPosToLocal(posInParentCoords, CoordsUtil.getPosVector3d(this),
				CoordsUtil.getRotVector3d(this));
		return posInLocalCoords;
	}

	
	public Vec3 convertToPosInLocalCoords(Vec3 posInParentCoords) {
		Vec3 emobPos = Vec3.createVectorHelper(posX, posY, posZ);

		Vec3 difference = Vec3.createVectorHelper(posInParentCoords.xCoord - emobPos.xCoord, posInParentCoords.yCoord - emobPos.yCoord,
				posInParentCoords.zCoord - emobPos.zCoord);
		Vec3 localPos = CoordsUtil.rotateVector(difference, -rotationYaw);
		return localPos;
	}

	public Vector3i convertToPosInLocalCoords(Vector3i posInParentCoords) {
		Vector3d pos = convertToPosInLocalCoords(new Vector3d(posInParentCoords.x, posInParentCoords.y,
				posInParentCoords.z));
		return new Vector3i((int) Math.floor(pos.x), (int) Math.floor(pos.y), (int) Math.floor(pos.z));
	}

	public Vector3d convertToPosInParentCoords(Vector3d posInLocalCoordinates) {
		return CoordsUtil.localPosToParent(posInLocalCoordinates, CoordsUtil.getPosVector3d(this), CoordsUtil.getRotVector3d(this));
	}

	
	public Vector3i convertToPosInParentCoords(Vector3i posInLocalCoordinates) {
		Vector3d pos = convertToPosInParentCoords(Vec3.createVectorHelper(posInLocalCoordinates.x,
				posInLocalCoordinates.y, posInLocalCoordinates.z));
		return new Vector3i((int) Math.floor(pos.x), (int) Math.floor(pos.y), (int) Math.floor(pos.z));
	}

	public Vector3d convertToPosInParentCoords(Vec3 posInLocalCoordinates) {
		return convertToPosInParentCoords(new Vector3d((float) posInLocalCoordinates.xCoord,
				(float) posInLocalCoordinates.yCoord, (float) posInLocalCoordinates.zCoord));
		// Vec3 rotatedVector = CoordsUtil.rotateVector(posInLocalCoordinates,
		// rotationYaw);
		// return rotatedVector.addVector(posX, posY, posZ);
	}

	protected final void findEntitiesAboard() {
		Profiler.startSection("EmobFindEntitiesAboard");
		entitiesAboard.clear();
		// boolean isSubmarine = (this instanceof Ship) && ((Ship)
		// this).isSubmarine();
		List list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(0, 4D, 0));
		if (list != null && list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				Entity entity = (Entity) list.get(i);
				if (
				// entity != riddenByEntity &&
				!(entity instanceof Emob)
						&& WorldUtil.hasBlockBelow(emobWorld, convertBBToLocalCoords(entity.boundingBox),
								(int) Math.ceil(entity.height) * 2)) {
					entitiesAboard.add(entity);
					EmobManager.registerEntityAboardEmob(entity, this);
				}
			}
		}
		Profiler.endSection();
	}

	/**
	 * Determines whether a given entity can interact with this emob. An entity
	 * can interact if it is within the emob bounds.
	 * 
	 * @param entity
	 * @return
	 */
	public boolean canInteractWith(Entity entity) {
		// Emob emobAboard = EmobManager.getEmobAboard(entity);
		return mc.inGameHasFocus && entity.posX < boundingBox.maxX + getInteractionDist()
				&& entity.posX > boundingBox.minX - getInteractionDist()
				&& entity.posY < boundingBox.maxY + entity.height && entity.posY > boundingBox.minY
				&& entity.posZ < boundingBox.maxZ + getInteractionDist()
				&& entity.posZ > boundingBox.minZ - getInteractionDist();
		// && objectMouseOver != null;
		// && (emobAboard == null || emobAboard == this))

	}

	/**
	 * Gets the minimum distance required to interact with this emob.
	 * 
	 * @return
	 */
	private float getInteractionDist() {
		return (float) (Math.max(Math.max(getLengthBlocks(), getWidthBlocks()), 5) / 2D);
	}

	@Override
	protected void entityInit() {
	}

	public AxisAlignedBB convertBBToParentCoords(AxisAlignedBB bbInLocalCoords) {
		Vector3d posInLocalCoords = getBBMiddle(bbInLocalCoords);
		Vector3d posInParentCoords = convertToPosInParentCoords(posInLocalCoords);
		return moveBBTo(bbInLocalCoords, posInParentCoords);
	}

	public AxisAlignedBB convertBBToLocalCoords(AxisAlignedBB bbInParentCoords) {
		Vector3d posInParentCoords = getBBMiddle(bbInParentCoords);
		Vector3d posInLocalCoords = convertToPosInLocalCoords(posInParentCoords);
		return moveBBTo(bbInParentCoords, posInLocalCoords);
	}

	public Vector3d getBBMiddle(AxisAlignedBB bb) {
		return new Vector3d((float) (bb.maxX + bb.minX) / 2f, (float) (bb.maxY + bb.minY) / 2f,
				(float) (bb.maxZ + bb.minZ) / 2f);
	}

	public AxisAlignedBB moveBBTo(AxisAlignedBB bb, Vector3d pos) {
		double halfLength = (bb.maxX - bb.minX) / 2D;
		double halfHeight = (bb.maxY - bb.minY) / 2D;
		double halfWidth = (bb.maxZ - bb.minZ) / 2D;
		return AxisAlignedBB.getBoundingBox(pos.x - halfLength, pos.y - halfHeight, pos.z - halfWidth, pos.x
				+ halfLength, pos.y + halfHeight, pos.z + halfWidth);
	}

	public void initPlayerWrapper() {
		if (emobPlayerEntity == null) {
			emobPlayerController = new EmobPlayerControllerSP(mc, this);
			// System.out.println(mc + " " + mc.thePlayer);
			emobPlayerEntity = new EntityPlayerWrapper(mc, emobWorld, mc.session, mc.thePlayer.dimension,
					this, mc.thePlayer);
			emobPlayerEntity.parentEntityPlayer = mc.thePlayer;
			emobPlayerController.setThePlayer(emobPlayerEntity);
			emobRender.mc.renderViewEntity = emobPlayerEntity;
		}
	}

	@Override
	public final void onUpdate() {
		Profiler.startSection("EmobOnUpdate");
		// prevRotationYaw = rotationYaw =
		// CoordsUtil.standardizeAngle(rotationYaw);
		tweakEmob();
		tweakPlayer();
		initPlayerWrapper();
		super.onEntityUpdate();

		dampenExternalMotionChanges();
		findEntitiesAboard();
		collideEntitiesWithBlocks();
		state.onUpdate(this);
		if (!isDead) {
			updateRotation();
			capMinMotion();
			moveEntity(motionX, motionY, motionZ);
			emobWorld.tickEmobWorld();
			setMotionAfterUpdate();

			handleFireDamage();

			if (getNumBlocks() <= 0) {
				setDead();
			}
			resetPressedKeys(emobPlayerEntity);
		}
		emobPlayerEntity.update(0);
		Profiler.endSection();
	}

	private void handleFireDamage() {
		if (isBurning()) {
			emobWorld.burnFromBottom();
		}

	}

	protected void onConvertingBackToNormalBlocks() {
		onAligning();

		if (isAligned()) {
			rotationYaw = 0;
			convertBackToNormalBlocks();
		}
	}

	protected void onAligning() {
		align();
		if (isAligned()) {
			rotationYaw = 0;
			state = ALIVE;
		}
	}

	protected boolean isAligned() {
		return (Math.abs(rotationYaw) < 1e-2 || Math.abs(360 - rotationYaw) < 1e-2)
				&& (Math.abs(posX - Math.floor(posX)) < 1e-2) && (Math.abs(posY - Math.floor(posY)) < 1e-2)
				&& (Math.abs(posZ - Math.floor(posZ)) < 1e-2);
	}

	protected boolean isAligning() {
		return state == ALIGNING;
	}

	protected void align() {
		float changeRate = 0.75f;
		angularVelocity.y = rotationYaw < 180f ? -rotationYaw * changeRate : (Math.min(rotationYaw,
				360 - rotationYaw)) * changeRate;
		if (angularVelocity.y < -1) {
			angularVelocity.y = -1;
		} else if (angularVelocity.y > 1) {
			angularVelocity.y = 1;
		}

		motionX = (Math.floor(posX) - posX) * changeRate;
		motionY = (Math.floor(posY) - posY) * changeRate;
		motionZ = (Math.floor(posZ) - posZ) * changeRate;

	}

	public void updateRotation() {
		rotationYaw += angularVelocity.y;
	}

	public void capMinMotion() {
		if (Math.abs(angularVelocity.y) < 0.001) {
			angularVelocity.y = 0;
		}
		// if (Math.abs(entity.motionY) < MIN_MOTION_Y) {
		// entity.motionY = 0;
		// }
	}

	private void updateActuators() {
		for (Actuator<?> actuator : actuators) {
			actuator.onUpdate();
		}

	}

	public int getNumBlocks() {
		return emobWorld.getNumBlocks();
	}

	private void collideEntitiesWithBlocks() {
		Profiler.startSection("EmobCollideEntitiesWithBlocks");
		for (Entity entity : entitiesAboard) {
			collideEntityWithBlocks(entity);
		}
		Profiler.endSection();
	}

	/**
	 * This dampens the effect of external motion changes, such as TNT
	 * explosions. The damping is proportional to the Emob's mass
	 * 
	 */
	protected void dampenExternalMotionChanges() {
		Profiler.startSection("EmobDampenExternalMotionChanges");
		float mass = getMass();

		Vec3 dMotion = Vec3.createVectorHelper((motionX - motionAfterUpdate.xCoord) / mass,
				(motionY - motionAfterUpdate.yCoord) / mass, (motionZ - motionAfterUpdate.zCoord) / mass);
		motionX = motionAfterUpdate.xCoord + dMotion.xCoord;
		motionY = motionAfterUpdate.yCoord + dMotion.yCoord;
		motionZ = motionAfterUpdate.zCoord + dMotion.zCoord;

		Profiler.endSection();
	}

	protected void collideEntityWithBlocks(Entity entity) {
		if (!isMounted(entity)) {
			AxisAlignedBB bb = convertBBToLocalCoords(entity.boundingBox);
			int minX = MathHelper.floor_double(bb.minX + 0.001D);
			int minY = MathHelper.floor_double(bb.minY + 0.001D);
			int minZ = MathHelper.floor_double(bb.minZ + 0.001D);
			int maxX = MathHelper.floor_double(bb.maxX - 0.001D);
			int maxY = MathHelper.floor_double(bb.maxY - 0.001D);
			int maxZ = MathHelper.floor_double(bb.maxZ - 0.001D);
			if (emobWorld.checkChunksExist(minX, minY, minZ, maxX, maxY, maxZ)) {
				for (int i = minX; i <= maxX; i++) {
					for (int j = minY; j <= maxY; j++) {
						for (int k = minZ; k <= maxZ; k++) {
							int blockID = emobWorld.getBlockId(i, j, k);
							if (blockID > 0) {
								// System.out.println("Collided with block " + i
								// +
								// ", " + j + ", " + k);
								Block.blocksList[blockID].onEntityCollidedWithBlock(emobWorld, i, j, k,
										wrapEntity(entity));
							}
						}
					}
				}
			}
		}

	}

	protected void processAllEvents() {
		if (canInteractWith(mc.thePlayer)) {
			eventManager.processAllEvents();
		}
		// Profiler.startSection("EmobProcessInputEvents");
		// boolean result = false;
		// if (canEntityInteractWithEmob(mc.thePlayer)) {
		// result = processMouseEvents() || processKeyboardEvents();
		// }
		// Profiler.endSection();
		// return result;
	}

	public boolean isEntityAboard(Entity entity) {
		return entitiesAboard.contains(entity);
	}

	// public boolean processMouseEvents() {
	// boolean result = false;
	// // if (IOUtil.isMouseButtonClicked(IOUtil.MOUSE_RIGHT_BUTTON, 0)) {
	// if (InputUtil.instance.isKeyClicked(mc.gameSettings.keyBindUseItem, 100))
	// {
	// result = onMouseRightClick(emobPlayerEntity);
	// } else if (mc.gameSettings.keyBindAttack.pressed) {
	// onMouseLeftClick(emobPlayerEntity);
	// } else {
	// resetPressedKeys(emobPlayerEntity);
	// }
	//
	// return result;
	// }

	protected abstract void resetPressedKeys(EntityPlayerWrapper player);

	public boolean onAttacked(EntityPlayerWrapper thePlayer) {
		if (objectMouseOver != null) {
			if (mc.currentScreen == null && mc.inGameHasFocus && objectMouseOver != null
					&& objectMouseOver.typeOfHit == EnumMovingObjectType.TILE) {
				// System.out.println("Hitting block ");
				int x = objectMouseOver.blockX;
				int y = objectMouseOver.blockY;
				int z = objectMouseOver.blockZ;
				emobPlayerController.onPlayerDamageBlock(x, y, z, objectMouseOver.sideHit);
				if (emobPlayerEntity.canPlayerEdit(x, y, z)) {
					Vector3i effectPosInParentCoords = convertToPosInParentCoords(new Vector3i(x, y, z));
					mc.effectRenderer.addBlockHitEffects(effectPosInParentCoords.x,
							effectPosInParentCoords.y, effectPosInParentCoords.z, objectMouseOver.sideHit);
					emobPlayerEntity.swingItem();
					return true;
				}
			}
		}
		return false;
	}

	public boolean onInteract(EntityPlayerWrapper thePlayer) {
		if (objectMouseOver != null) {

			ItemStack itemstack = thePlayer.inventory.getCurrentItem();
			boolean result = false;
			int stackSize = itemstack == null ? 0 : itemstack.stackSize;
			int x = objectMouseOver.blockX;
			int y = objectMouseOver.blockY;
			int z = objectMouseOver.blockZ;
			int side = objectMouseOver.sideHit;
			if (emobPlayerController.onPlayerRightClick(thePlayer, emobWorld, itemstack, x, y, z, side)) {
				result = true;
				thePlayer.swingItem();
			}
			if (itemstack == null) {
				return false;
			}
			if (itemstack.stackSize == 0) {
				thePlayer.inventory.mainInventory[thePlayer.inventory.currentItem] = null;
			} else if (itemstack.stackSize != stackSize || emobPlayerController.isInCreativeMode()) {
				mc.entityRenderer.itemRenderer.func_9449_b();
			}
			return result;
		}
		return false;
	}

	public MovingObjectPosition updateObjectMouseOver(EntityPlayer thePlayer) {
		float f = 1.0F;
		Vector3d from = CoordsUtil.calculateFromVector(thePlayer, f);

		Vector3d to = CoordsUtil.calculateToVector(thePlayer, f, from, getInteractionDist());

		Vector3d fromLocal = convertToPosInLocalCoords(from);
		Vector3d toLocal = convertToPosInLocalCoords(to);

		objectMouseOver = emobWorld.rayTraceBlocks_do(fromLocal, toLocal, true);
		if (objectMouseOver != null) {
			if (mc.objectMouseOver != null
					&& mc.objectMouseOver.hitVec.squareDistanceTo(mc.thePlayer.posX, mc.thePlayer.posY,
							mc.thePlayer.posZ) < objectMouseOver.hitVec.squareDistanceTo(mc.thePlayer.posX,
							mc.thePlayer.posY, mc.thePlayer.posZ)) {
				return null;
			}

		}
		return objectMouseOver;
	}

	public EntityWrapper wrapEntity(Entity entity) {
		return new EntityWrapper(emobWorld, entity, this);
	}

	public void setMotionAfterUpdate() {
		motionAfterUpdate.xCoord = motionX;
		motionAfterUpdate.yCoord = motionY;
		motionAfterUpdate.zCoord = motionZ;
	}

	@Override
	public void setDead() {
		isDead = true;
		System.out.println(emobID + " is dead");
		EmobManager.removeEmob(this);
	}

	public double getXZDiagonal() {
		return Math.sqrt(getLengthBlocks() * getLengthBlocks() + getWidthBlocks() * getWidthBlocks());
	}

	public void setCollidingEntityOffsetInParentCoords(double dx, double dy, double dz) {
		collidingEntityOffset.xCoord = dx;
		collidingEntityOffset.yCoord = dy;
		collidingEntityOffset.zCoord = dz;
	}

	public void setPosLastExposion(double x, double y, double z) {
		posLastExplosion.x = x;
		posLastExplosion.y = y;
		posLastExplosion.z = z;
	}

	@Override
	public boolean attackEntityFrom(DamageSource ds, int damage) {
		super.attackEntityFrom(ds, damage);
		if (ds == DamageSource.explosion) {
			Vector3d posLastExplosionInLocalCoords = convertToPosInLocalCoords(posLastExplosion);
			boolean hadBlockBelowBeforeExplosion = WorldUtil.hasBlockBelow(emobWorld,
					posLastExplosionInLocalCoords, getHeightBlocks());
			long numBlocksBeforeExplosion = getNumBlocks();
			emobWorld.createExplosion(new EntityTNTPrimed(null), posLastExplosionInLocalCoords.x,
					posLastExplosionInLocalCoords.y, posLastExplosionInLocalCoords.z, 5);

			// If the explosion makes a hole in the hull, the ship will sink.
			if (hadBlockBelowBeforeExplosion
					&& !WorldUtil.hasBlockBelow(emobWorld, posLastExplosionInLocalCoords, getHeightBlocks())
			// && (numBlocksBeforeExplosion - getNumBlocks()) > 0.1 *
			// numBlocksBeforeExplosion

			) {
				state = DYING;
			}
			return true;
		}
		return false;
	}

	protected abstract void onDying();

	protected void onAlive() {
		processAllEvents();
		updateActuators();
	}

	/**
	 * Infers entity offset from the difference b/w {@link Entity#boundingBox}
	 * and bb
	 * 
	 * @param entity
	 * @param bb
	 */
	public void calculateOffset(Entity entity, AxisAlignedBB bb) {

		if (entity.boundingBox.minX != bb.minX) {
			collidingEntityOffset.xCoord = bb.minX - entity.boundingBox.minX;
		} else if (entity.boundingBox.maxX != bb.maxX) {
			collidingEntityOffset.xCoord = bb.maxX - entity.boundingBox.maxX;
		} else {
			collidingEntityOffset.xCoord = 0;
		}

		if (entity.boundingBox.minY != bb.minY) {
			collidingEntityOffset.yCoord = bb.minY - entity.boundingBox.minY;
		} else if (entity.boundingBox.maxY != bb.maxY) {
			collidingEntityOffset.yCoord = bb.maxY - entity.boundingBox.maxY;
		} else {
			collidingEntityOffset.yCoord = 0;
		}

		if (entity.boundingBox.minX != bb.minZ) {
			collidingEntityOffset.zCoord = bb.minZ - entity.boundingBox.minZ;
		} else if (entity.boundingBox.maxZ != bb.maxZ) {
			collidingEntityOffset.zCoord = bb.maxZ - entity.boundingBox.maxZ;
		} else {
			collidingEntityOffset.zCoord = 0;
		}

	}

	@Override
	public double getDistance(double x, double y, double z) {
		setPosLastExposion(x, y, z);
		if (isWithinEmobBoundaries(x, y, z)) {
			return 0.1;
		}
		return super.getDistance(x, y, z);
	}

	public boolean isWithinEmobBoundaries(double x, double y, double z) {
		return (x >= boundingBox.minX && x <= boundingBox.maxX)
				&& (y >= boundingBox.minY && y <= boundingBox.maxY)
				&& (z >= boundingBox.minZ && z <= boundingBox.maxZ);
	}

	@Override
	public void moveEntity(double dx, double dy, double dz) {
		Vec3 offset = Vec3.createVectorHelper(dx, dy, dz);
		if (Math.abs(offset.xCoord) < minMotion.xCoord) {
			offset.xCoord = 0;
		}
		if (Math.abs(offset.yCoord) < minMotion.yCoord) {
			offset.yCoord = 0;
		}
		if (Math.abs(offset.zCoord) < minMotion.zCoord) {
			offset.zCoord = 0;
		}
		if (isAligning()) {
			posX += offset.xCoord;
			posY += offset.yCoord;
			posZ += offset.zCoord;
			updateBoundingBox();
		} else {
			super.moveEntity(offset.xCoord, offset.yCoord, offset.zCoord);
		}
		boolean reachedTop = posY > 250 - getHeightBlocks();
		if (reachedTop) {
			posY = 250 - getHeightBlocks();
			updateBoundingBox();
		}
		if (rotationYaw != prevRotationYaw) {
			updateBoundingBox();
		}

		Profiler.startSection("EmobUpdateEntitiesAboard");
		for (Entity entity : entitiesAboard) {
			updateEntityAboard(entity, motionX, motionY, motionZ);
		}
		EmobManager.executeScheduledChanges(mc.thePlayer);

		if (reachedTop) {
			motionY = 0;
		}

		Profiler.endSection();
	}

	private void move(double dx, double dy, double dz) {
		Profiler.startSection("EmobMove");
		List collidingBBs = this.worldObj.getCollidingBoundingBoxes(this,
				this.boundingBox.addCoord(dx, dy, dz));
		double prevDx = dx;
		double prevDy = dy;
		double prevDz = dz;
		for (int var20 = 0; var20 < collidingBBs.size(); ++var20) {
			dy = ((AxisAlignedBB) collidingBBs.get(var20)).calculateYOffset(this.boundingBox, dy);
		}

		this.boundingBox.offset(0.0D, dy, 0.0D);

		if (!this.field_9293_aM && prevDy != dy) {
			dz = 0.0D;
			dy = 0.0D;
			dx = 0.0D;
		}

		boolean var36 = this.onGround || prevDy != dy && prevDy < 0.0D;
		int i;

		for (i = 0; i < collidingBBs.size(); ++i) {
			dx = ((AxisAlignedBB) collidingBBs.get(i)).calculateXOffset(this.boundingBox, dx);
		}

		this.boundingBox.offset(dx, 0.0D, 0.0D);

		if (!this.field_9293_aM && prevDx != dx) {
			dz = 0.0D;
			dy = 0.0D;
			dx = 0.0D;
		}

		for (i = 0; i < collidingBBs.size(); ++i) {
			dz = ((AxisAlignedBB) collidingBBs.get(i)).calculateZOffset(this.boundingBox, dz);
		}

		this.boundingBox.offset(0.0D, 0.0D, dz);

		if (!this.field_9293_aM && prevDz != dz) {
			dz = 0.0D;
			dy = 0.0D;
			dx = 0.0D;
		}

		this.posX = (this.boundingBox.minX + this.boundingBox.maxX) / 2.0D;
		this.posY = this.boundingBox.minY + (double) this.yOffset - (double) this.ySize;
		this.posZ = (this.boundingBox.minZ + this.boundingBox.maxZ) / 2.0D;
		this.isCollidedHorizontally = prevDx != dx || prevDz != dz;
		this.isCollidedVertically = prevDy != dy;
		this.onGround = prevDy != dy && prevDy < 0.0D;
		this.isCollided = this.isCollidedHorizontally || this.isCollidedVertically;
		this.updateFallState(dy, this.onGround);

		if (prevDx != dx) {
			this.motionX = 0.0D;
		}

		if (prevDy != dy) {
			this.motionY = 0.0D;
		}

		if (prevDz != dz) {
			this.motionZ = 0.0D;
		}

		Profiler.endSection();

	}

	// private void coarseCollision(Vec3 offset) {
	// Profiler.startSection("EmobCoarseCollision");
	// hasCoarseCollided = false;
	// List<AxisAlignedBB> bbs = worldObj.getCollidingBoundingBoxes(this,
	// boundingBox.addCoord(offset.xCoord, offset.yCoord, offset.zCoord));
	// for (AxisAlignedBB bb : bbs) {
	// if (((EmobBB) boundingBox).calculateOffset(bb, offset)) {
	// isCollidedHorizontally = true;
	// hasCoarseCollided = true;
	// motionX = offset.xCoord;
	// motionY = offset.yCoord;
	// motionZ = offset.zCoord;
	// }
	// }
	// Profiler.endSection();
	// }

	private void upudateEntityOnLadder(Entity entity) {
		if (entity instanceof EntityLiving) {
			EntityWrapper entityWrapper = wrapEntity(entity);
			if (entityWrapper.isOnLadder()) {
				float f5 = 0.15F;
				if (entity.motionX < (double) (-f5)) {
					entity.motionX = -f5;
				}
				if (entity.motionX > (double) f5) {
					entity.motionX = f5;
				}
				if (entity.motionZ < (double) (-f5)) {
					entity.motionZ = -f5;
				}
				if (entity.motionZ > (double) f5) {
					entity.motionZ = f5;
				}
				entity.fallDistance = 0.0F;
				if (entity.motionY < -0.15D) {
					entity.motionY = -0.15D;
				}
				if (entity.isSneaking() && entity.motionY < 0.0D) {
					entity.motionY = 0.0D;
				}
			}
			if (entityWrapper.isCollidedHorizontally && entityWrapper.isOnLadder()) {
				entity.motionY = 0.2D;
			}
		}

	}

	public void updateEntityAboard(Entity entity, double dx, double dy, double dz) {
		if (!isMounted(entity)) {
			float dYaw = rotationYaw - prevRotationYaw;
			float dPitch = rotationPitch - prevRotationPitch;
			Vec3 r = Vec3.createVectorHelper(entity.posX + dx - posX, entity.posY + dy + 1E-2D - posY,
					entity.posZ + dz - posZ);
			Vec3 w = Vec3.createVectorHelper(0D, -dYaw / 180D * Math.PI, 0D);
			Vec3 rotMotion = w.crossProduct(r);
			EmobManager.scheduleChange(new EntityChange(entity, Vec3.createVectorHelper(dx
					+ rotMotion.xCoord, dy + 1E-2D + rotMotion.yCoord, dz + rotMotion.zCoord), dYaw, dPitch));
			upudateEntityOnLadder(entity);
		}
	}

	public void convertBackToNormalBlocks() {
		Vector3d cornerLocal = emobWorld.getLowerBackLeftCorner();

		Vector3d corner = convertToPosInParentCoords(cornerLocal);
		Vector3i c = new Vector3i(corner);

		WorldUtil.copyBlocksRaw(emobWorld, (int) Math.round(cornerLocal.x), (int) Math.round(cornerLocal.y),
				(int) Math.round(cornerLocal.z), getLengthBlocks(), getHeightBlocks(), getWidthBlocks(),
				worldObj, c.x, c.y, c.z, getExcludedBlocks());
		emobWorld.copyScheduledUpdatesToWorld(worldObj);
		WorldUtil.updateBox(worldObj, (int) corner.x, (int) corner.y, (int) corner.z, getLengthBlocks(),
				getHeightBlocks(), getWidthBlocks());
		setDead();
	}

	/**
	 * Moves emob to the beginning of the global entity list. This is to fix
	 * issue 33.
	 * 
	 * 
	 */
	public void tweakEmob() {
		Profiler.startSection("tweakEmob");
		if (needTweak) {
			needTweak = false;
			worldObj.loadedEntityList.remove(this);
			worldObj.loadedEntityList.add(0, this);
		}
		Profiler.endSection();
	}

	/**
	 * Moves player to the beginning of the global entity list. This is to fix
	 * issue 33.
	 * 
	 * 
	 */
	public void tweakPlayer() {
		Profiler.startSection("tweakPlayer");
		if (needPlayerTweak && mc.thePlayer != null) {
			needPlayerTweak = false;
			worldObj.loadedEntityList.remove(mc.thePlayer);
			worldObj.loadedEntityList.add(0, mc.thePlayer);
		}
		Profiler.endSection();
	}

	public EmobPlayerControllerSP getEmobPlayerController() {
		return emobPlayerController;
	}

	public EntityPlayerWrapper getEmobPlayerEntity() {
		// initPlayerWrapper();
		return emobPlayerEntity;
	}

	public BlockCounter getBlockCounter() {
		return emobWorld.getBlockCounter();
	}

	public Set<Entity> getEntitiesAboard() {
		return entitiesAboard;
	}

	public void addActuator(Actuator<?> actuator) {
		actuators.add(actuator);
	}

	public abstract float getMass();

	public void startConvertingBackToNormalBlocks() {
		state = CONVERTING_BACK_TO_NORMAL_BLOCKS;

	}

	public boolean isMounted(Entity entity) {
		return entity == riddenByEntity;
	}

	public Vec3 getMinMotion() {
		return minMotion;
	}

	public void startAligning() {
		state = ALIGNING;

	}

	@Override
	protected void updateFallState(double dy, boolean onGround) {
		if (onGround && this.fallDistance > 0.0F) {
			for (Entity entity : entitiesAboard) {
				entity.fall(fallDistance);
			}
		}
		super.updateFallState(dy, onGround);
	}

	public abstract BlockSet getExcludedBlocks();

	public Vector3d getLowerBackLeftCorner() {
		return emobWorld.getLowerBackLeftCorner();
	}

	public Vector3d getUpperFrontRightCorner(Vector3d corner) {
		return emobWorld.getUpperFrontRightCorner(corner);
	}

	public MovingObjectPosition rayTraceBlocks(Vector3f pos1Local, Vector3f pos2Local) {
		return emobWorld.rayTraceBlocks(CoordsUtil.toVec3(pos1Local), CoordsUtil.toVec3(pos2Local));
	}

}

