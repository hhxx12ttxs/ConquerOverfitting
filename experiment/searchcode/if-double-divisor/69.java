package com.afforess.minecartmaniacore.minecart;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.PoweredMinecart;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.afforess.minecartmaniacore.MinecartManiaCore;
import com.afforess.minecartmaniacore.config.ControlBlockList;
import com.afforess.minecartmaniacore.config.MinecartManiaConfiguration;
import com.afforess.minecartmaniacore.event.MinecartCaughtEvent;
import com.afforess.minecartmaniacore.event.MinecartElevatorEvent;
import com.afforess.minecartmaniacore.event.MinecartLaunchedEvent;
import com.afforess.minecartmaniacore.event.MinecartManiaMinecartCreatedEvent;
import com.afforess.minecartmaniacore.event.MinecartManiaMinecartDestroyedEvent;
import com.afforess.minecartmaniacore.event.MinecartPassengerEjectEvent;
import com.afforess.minecartmaniacore.event.MinecartSpeedMultiplierEvent;
import com.afforess.minecartmaniacore.event.MinecartTimeEvent;
import com.afforess.minecartmaniacore.inventory.MinecartManiaChest;
import com.afforess.minecartmaniacore.inventory.MinecartManiaInventory;
import com.afforess.minecartmaniacore.signs.LaunchMinecartAction;
import com.afforess.minecartmaniacore.signs.Sign;
import com.afforess.minecartmaniacore.utils.BlockUtils;
import com.afforess.minecartmaniacore.utils.DirectionUtils;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;
import com.afforess.minecartmaniacore.utils.MinecartUtils;
import com.afforess.minecartmaniacore.utils.SignUtils;
import com.afforess.minecartmaniacore.utils.ThreadSafe;
import com.afforess.minecartmaniacore.world.Item;
import com.afforess.minecartmaniacore.world.MinecartManiaWorld;
import com.afforess.minecartmaniacore.world.SpecificMaterial;

@SuppressWarnings("deprecation")
public class MinecartManiaMinecart {
    public final Minecart minecart;
    public static final double MAXIMUM_MOMENTUM = 1E150D;
    public boolean createdLastTick = true;
    protected volatile Vector previousLocation;
    protected volatile Vector previousMotion;
    protected Calendar cal;
    protected volatile CompassDirection previousFacingDir = DirectionUtils.CompassDirection.NO_DIRECTION;
    protected volatile boolean wasMovingLastTick;
    protected MinecartOwner owner = null;
    protected volatile int range = 4;
    protected volatile int rangeY = 4;
    protected volatile boolean dead = false;
    protected ConcurrentHashMap<String, Object> data = new ConcurrentHashMap<String, Object>();
    
    public MinecartManiaMinecart(final Minecart cart) {
        minecart = cart;
        ChunkManager.init(cart.getWorld());
        findOwner();
        initialize();
    }
    
    public MinecartManiaMinecart(final Minecart cart, final String owner) {
        minecart = cart;
        ChunkManager.init(cart.getWorld());
        this.owner = new MinecartOwner(owner);
        this.owner.setId(minecart.getEntityId());
        this.owner.setWorld(minecart.getWorld().getName());
        //clear previous owners
        /*
         * List<MinecartOwner> list = MinecartManiaCore.instance.getDatabase().find(MinecartOwner.class).where().idEq(minecart.getEntityId()).findList(); for (MinecartOwner temp : list) { MinecartManiaCore.instance.getDatabase().delete(temp); } //save new owner MinecartManiaCore.instance.getDatabase().save(this.owner);
         */
        initialize();
    }
    
    private void initialize() {
        setRange(MinecartManiaConfiguration.getMinecartRange());
        setRangeY(MinecartManiaConfiguration.getMinecartRangeY());
        cal = Calendar.getInstance();
        setWasMovingLastTick(isMoving());
        previousMotion = minecart.getVelocity().clone();
        previousLocation = minecart.getLocation().toVector().clone();
        previousLocation.setY(previousLocation.getX() - 1); //fool game into thinking we've already moved
        minecart.setMaxSpeed((MinecartManiaConfiguration.getDefaultMinecartSpeedPercent() * 0.4D) / 100);
        MinecartManiaCore.callEvent(new MinecartManiaMinecartCreatedEvent(this));
    }
    
    /**
     ** Attempts to find the player that spawned this minecart.
     */
    private void findOwner() {
        /*
         * final EbeanServer db = MinecartManiaCore.instance.getDatabase(); try { MinecartOwner temp = db.find(MinecartOwner.class).where().idEq(minecart.getEntityId()).findUnique(); if (temp != null) { owner = temp; return; } } catch (Exception e) { //clear duplicates MinecartManiaLogger.getInstance().debug("Clearing Duplicate Minecart Id's : " + minecart.getEntityId()); try { List<MinecartOwner> list = db.find(MinecartOwner.class).where().idEq(minecart.getEntityId()).findList(); for (MinecartOwner temp : list) { db.delete(temp); } } catch (NullPointerException npe) { MinecartManiaLogger.getInstance().info("Failed to clear duplicate minecart entities!"); } }
         */
        double closest = Double.MAX_VALUE;
        Player closestPlayer = null;
        for (final LivingEntity le : minecart.getWorld().getLivingEntities()) {
            if (le instanceof Player) {
                final double distance = le.getLocation().toVector().distance(minecart.getLocation().toVector());
                if (distance < closest) {
                    closestPlayer = (Player) le;
                    closest = distance;
                }
            }
        }
        if (closestPlayer != null) {
            owner = new MinecartOwner(closestPlayer.getName());
        } else {
            owner = new MinecartOwner();
        }
        owner.setId(minecart.getEntityId());
        owner.setWorld(minecart.getWorld().getName());
        if (owner.hasOwner()) {
            //	db.save(this.owner);
        }
    }
    
    /**
     * Get's the X coordinate of this minecart
     * 
     * @return X coordinate
     */
    public final int getX() {
        return getLocation().getBlockX();
    }
    
    /**
     * Get's the Y coordinate of this minecart
     * 
     * @return Y coordinate
     */
    public final int getY() {
        return getLocation().getBlockY();
    }
    
    /**
     * Get's the Z coordinate of this minecart
     * 
     * @return Z coordinate of this minecart
     */
    public final int getZ() {
        return getLocation().getBlockZ();
    }
    
    /**
     * Is true if the minecart is dead, and has yet to be removed by the garbage collector
     * 
     * @return is dead
     */
    @ThreadSafe
    public final boolean isDead() {
        if (minecart.isDead()) {
            dead = true;
        }
        return dead;
    }
    
    /**
     * Get's the location of this minecart in the world
     * 
     * @return location
     */
    public final Location getLocation() {
        return minecart.getLocation();
    }
    
    /**
     * Get's the block this minecart is current occupying
     * 
     * @return block
     */
    public final Block getOccupiedBlock() {
        return getLocation().getBlock();
    }
    
    /**
     * Get's the previous position (from the last tick) of the minecart in the world
     * 
     * @return previous position
     */
    @Deprecated
    public final Vector getPreviousLocation() {
        return previousLocation.clone();
    }
    
    /**
     * Get's the previous position (from the last tick) of the minecart in the world
     * 
     * @return previous position
     */
    @ThreadSafe
    public final Vector getPreviousPosition() {
        return previousLocation.clone(); //cloned to avoid others messing with the mutable reference
    }
    
    /**
     * Get's the previous location (from the last tick) of the minecart in the world
     * 
     * @return previous location
     */
    public final Location getPrevLocation() {
        //using minecart.getWorld is safe for estimating the previous location because teleporting minecarts between worlds does not work
        return previousLocation.toLocation(minecart.getWorld());
    }
    
    /**
     * Get's the world this minecart is in
     * 
     * @return world
     */
    public final World getWorld() {
        return minecart.getWorld();
    }
    
    /**
     * Teleports this minecart to the given location. Works with locations in other worlds
     * 
     * @param location
     * @return the new MinecartManiaMinecart at the end of the teleport, null if the teleport was unsuccessful
     */
    public MinecartManiaMinecart teleport(final Location location) {
        if (!location.getWorld().equals(getWorld())) {
            final Minecart newCart;
            location.getWorld().loadChunk(location.getBlock().getChunk());
            if (isStandardMinecart()) {
                newCart = location.getWorld().spawn(location, Minecart.class);
            } else if (isPoweredMinecart()) {
                newCart = location.getWorld().spawn(location, PoweredMinecart.class);
            } else {
                newCart = location.getWorld().spawn(location, StorageMinecart.class);
            }
            final Entity passenger = minecart.getPassenger();
            minecart.eject();
            if (passenger != null) {
                passenger.teleport(location);
            }
            final Runnable update = new Runnable() {
                public void run() {
                    if (passenger != null) {
                        newCart.setPassenger(passenger);
                    }
                    newCart.setVelocity(minecart.getVelocity());
                }
            };
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(MinecartManiaCore.getInstance(), update, 5);
            
            final MinecartManiaMinecart newMinecartManiaMinecart = copy(newCart);
            kill(false);
            return newMinecartManiaMinecart;
        }
        if (minecart.teleport(location))
            return this;
        return null;
    }
    
    /**
     * Get's the chunk this minecart is at
     * 
     * @return chunk
     */
    public final Chunk getChunkAt() {
        return getLocation().getBlock().getChunk();
    }
    
    /**
     * Updates the minecart's previous location
     */
    public void updateLocation() {
        previousLocation = minecart.getLocation().toVector().clone();
    }
    
    /**
     * Get's the previous motion of the minecart from the last tick
     * 
     * @return previous motion
     */
    public Vector getPreviousMotion() {
        return previousMotion.clone();
    }
    
    /**
     * Updates the minecart's previous motion
     */
    public void updateMotion() {
        previousMotion = minecart.getVelocity().clone();
    }
    
    /**
     * Returns the motion of the cart
     * 
     * @return motion
     */
    public Vector getMotion() {
        return minecart.getVelocity();
    }
    
    /**
     * Set's the motion of this minecart
     * 
     * @param motion to set
     */
    public void setMotion(final Vector motion) {
        minecart.setVelocity(motion);
    }
    
    /**
     * Checks to see if the minecart has moved positions from it's previous position
     * 
     * @return true if the minecart has moved positions
     */
    public boolean hasChangedPosition() {
        if (getPreviousPosition().getBlockX() != minecart.getLocation().getBlockX())
            return true;
        if (getPreviousPosition().getBlockY() != minecart.getLocation().getBlockY())
            return true;
        if (getPreviousPosition().getBlockZ() != minecart.getLocation().getBlockZ())
            return true;
        return false;
    }
    
    /**
     * Get's the motion in the X direction
     * 
     * @return X motion
     */
    public double getMotionX() {
        return minecart.getVelocity().getX();
    }
    
    public void changeMotionX(final double change) {
        setMotionX(getMotionX() + change);
    }
    
    public void setMotionX(final double motionX) {
        setMotion(motionX, getMotionY(), getMotionZ());
    }
    
    /**
     * Get's the motion in the Y direction
     * 
     * @return Y motion
     */
    public double getMotionY() {
        return minecart.getVelocity().getY();
    }
    
    public void changeMotionY(final double change) {
        setMotionY(getMotionY() + change);
    }
    
    public void setMotionY(final double motionY) {
        setMotion(getMotionX(), motionY, getMotionZ());
    }
    
    /**
     * Get's the motion in the Z direction
     * 
     * @return Z motion
     */
    public double getMotionZ() {
        return minecart.getVelocity().getZ();
    }
    
    public void changeMotionZ(final double change) {
        setMotionZ(getMotionZ() + change);
    }
    
    public void setMotionZ(final double motionZ) {
        setMotion(getMotionX(), getMotionY(), motionZ);
    }
    
    /**
     * Multiplies the minecarts current motion by the given multiplier in a safe way that will avoid causing overflow, which will cause the minecart to grind to a halt.
     * 
     * @param multiplier
     */
    public void multiplyMotion(final double multiplier) {
        if ((MAXIMUM_MOMENTUM / Math.abs(multiplier)) > Math.abs(getMotionX())) {
            setMotionX(getMotionX() * multiplier);
        }
        if ((MAXIMUM_MOMENTUM / Math.abs(multiplier)) > Math.abs(getMotionZ())) {
            setMotionZ(getMotionZ() * multiplier);
        }
    }
    
    private void setMotion(final double motionX, final double motionY, final double motionZ) {
        final Vector newVelocity = new Vector();
        newVelocity.setX(motionX);
        newVelocity.setY(motionY);
        newVelocity.setZ(motionZ);
        minecart.setVelocity(newVelocity);
    }
    
    /**
     * Get's the previous direction of motion for this minecart
     * 
     * @return previous direction
     */
    public CompassDirection getPreviousDirectionOfMotion() {
        return previousFacingDir;
    }
    
    /**
     * Set's the previous direction of motion for this minecart
     * 
     * @param previous direction
     */
    public void setPreviousDirectionOfMotion(final CompassDirection direction) {
        previousFacingDir = direction;
    }
    
    @Deprecated
    public void setPreviousFacingDir(final DirectionUtils.CompassDirection dir) {
        previousFacingDir = dir;
    }
    
    @Deprecated
    public DirectionUtils.CompassDirection getPreviousFacingDir() {
        return previousFacingDir;
    }
    
    /**
     * Get's the direction that this minecart is moving, or NO_DIRECTION if it is not moving
     * 
     * @return direction
     */
    public CompassDirection getDirectionOfMotion() {
        if (getMotionX() < 0.0D)
            return CompassDirection.NORTH;
        if (getMotionZ() < 0.0D)
            return CompassDirection.EAST;
        if (getMotionX() > 0.0D)
            return CompassDirection.SOUTH;
        if (getMotionZ() > 0.0D)
            return CompassDirection.WEST;
        return CompassDirection.NO_DIRECTION;
    }
    
    /**
     * Attempts a "best guess" at the direction of the minecart. If the minecart is moving, it will return the correct direction, but if it's stopped, it will use the value stored in memory.
     * 
     * @return CompassDirection that the minecart is moving towards
     */
    public CompassDirection getDirection() {
        if (isMoving())
            return getDirectionOfMotion();
        return getPreviousDirectionOfMotion();
    }
    
    /**
     * Stops this minecart
     */
    public void stopCart() {
        setMotion(0D, 0D, 0D);
    }
    
    /**
     * Returns true if the minecart is moving
     * 
     * @return true if moving
     */
    public boolean isMoving() {
        return (getMotionX() != 0D) || (getMotionY() != 0D) || (getMotionZ() != 0D);
    }
    
    /**
     * Returns true if the minecart has a player passenger onboard
     * 
     * @return true if player onboard
     */
    public boolean hasPlayerPassenger() {
        return getPlayerPassenger() != null;
    }
    
    /**
     * Returns true if the minecart has a passenger on board
     * 
     * @return
     */
    public boolean hasPassenger() {
        return minecart.getPassenger() != null;
    }
    
    public Entity getPassenger() {
        return minecart.getPassenger();
    }
    
    public void setPassenger(final Entity entity) {
        minecart.setPassenger(entity);
    }
    
    public Player getPlayerPassenger() {
        if (minecart.getPassenger() == null)
            return null;
        if (minecart.getPassenger() instanceof Player)
            return (Player) minecart.getPassenger();
        return null;
    }
    
    public boolean eject() {
        return minecart.eject();
    }
    
    /**
     * Returns the value from the loaded data
     * 
     * @param the string key the data value is associated with
     */
    @ThreadSafe
    public final Object getDataValue(final String key) {
        if (data.containsKey(key))
            return data.get(key);
        return null;
    }
    
    /**
     * Creates a new data value if it does not already exists, or resets an existing value
     * 
     * @param the string key the data value is associated with
     * @param the value to store
     */
    @ThreadSafe
    public final void setDataValue(final String key, final Object value) {
        if (value == null) {
            data.remove(key);
        } else {
            data.put(key, value);
        }
    }
    
    public int getBlockIdBeneath() {
        return getBlockBeneath().getTypeId();
    }
    
    public Material getMaterialBeneath() {
        return getBlockBeneath().getType();
    }
    
    @Deprecated
    public Item getItemBeneath() {
        return Item.getItem(getBlockBeneath());
    }
    
    public Block getBlockBeneath() {
        if (ControlBlockList.getControlBlock(new SpecificMaterial(getLocation().getBlock().getTypeId(), getLocation().getBlock().getData())) != null)
            return getLocation().getBlock();
        else {
            final Location temp = getLocation();
            temp.setY(temp.getY() - 1);
            return temp.getBlock();
        }
    }
    
    public boolean isPoweredBeneath() {
        if (MinecartManiaWorld.isBlockPowered(minecart.getWorld(), getX(), getY() - 2, getZ()) || MinecartManiaWorld.isBlockIndirectlyPowered(minecart.getWorld(), getX(), getY() - 1, getZ()) || MinecartManiaWorld.isBlockIndirectlyPowered(minecart.getWorld(), getX(), getY(), getZ()))
            return true;
        return false;
    }
    
    public void reverse() {
        setMotionX(getMotionX() * -1);
        setMotionY(getMotionY() * -1);
        setMotionZ(getMotionZ() * -1);
    }
    
    public void undoPoweredRails() {
        //this server has decided to override the default boost value, so we need to undo notch's changes
        if (getLocation().getBlock().getTypeId() == Material.POWERED_RAIL.getId()) {
            if ((ControlBlockList.getSpeedMultiplier(this) != 1.0D) && isMoving()) {
                final int data = getLocation().getBlock().getData();
                final boolean powered = (data & 8) != 0;
                final double divisor = 0.06D; //magic number from MC code
                final double multiplier = Math.sqrt(getMotionX() * getMotionX() * getMotionZ() * getMotionZ()); //magic number from MC code
                if (powered) {
                    multiplyMotion(multiplier);
                    multiplyMotion(divisor);
                } else {
                    multiplyMotion(2.0D);
                }
            }
        }
        
    }
    
    public boolean doSpeedMultiplierBlock() {
        double multiplier = ControlBlockList.getSpeedMultiplier(this);
        if (multiplier != 1.0D) {
            final MinecartSpeedMultiplierEvent msme = new MinecartSpeedMultiplierEvent(this, multiplier);
            MinecartManiaCore.callEvent(msme);
            multiplyMotion(msme.getSpeedMultiplier());
            return msme.isCancelled();
        }
        //check for powered rails
        multiplier = ControlBlockList.getSpeedMultiplier(this);
        if (multiplier != 1.0D) {
            final MinecartSpeedMultiplierEvent msme = new MinecartSpeedMultiplierEvent(this, multiplier);
            MinecartManiaCore.callEvent(msme);
            multiplyMotion(msme.getSpeedMultiplier());
            return msme.isCancelled();
        }
        return false;
    }
    
    public boolean doPlatformBlock() {
        if (ControlBlockList.isValidPlatformBlock(this) && isStandardMinecart()) {
            if (minecart.getPassenger() == null) {
                final List<LivingEntity> list = minecart.getWorld().getLivingEntities();
                double range = ControlBlockList.getControlBlock(getSpecificMaterialBeneath()).getPlatformRange();
                range *= range;
                LivingEntity closest = null;
                double distance = -1;
                for (final LivingEntity le : list) {
                    if ((le.getLocation().toVector().distanceSquared(minecart.getLocation().toVector()) < distance) || (closest == null)) {
                        closest = le;
                        distance = le.getLocation().toVector().distanceSquared(minecart.getLocation().toVector());
                    }
                }
                if ((closest != null) && (closest.getLocation().toVector().distanceSquared(minecart.getLocation().toVector()) < range)) {
                    //Let the world know about this
                    final VehicleEnterEvent vee = new VehicleEnterEvent(minecart, closest);
                    MinecartManiaCore.callEvent(vee);
                    if (!vee.isCancelled()) {
                        minecart.setPassenger(closest);
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public void doLauncherBlock() {
        if (ControlBlockList.getLaunchSpeed(getSpecificMaterialBeneath()) != 0.0D) {
            if (ControlBlockList.isValidLauncherBlock(this)) {
                if (!isMoving()) {
                    launchCart(ControlBlockList.getLaunchSpeed(getSpecificMaterialBeneath()));
                }
            }
        }
    }
    
    public boolean doCatcherBlock() {
        if (ControlBlockList.isCatcherBlock(getSpecificMaterialBeneath())) {
            if (ControlBlockList.isValidCatcherBlock(this)) {
                final MinecartCaughtEvent mce = new MinecartCaughtEvent(this);
                MinecartManiaCore.callEvent(mce);
                if (!mce.isActionTaken()) {
                    stopCart();
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean doKillBlock() {
        if (ControlBlockList.isValidKillMinecartBlock(this)) {
            kill(getOwner() instanceof MinecartManiaChest);
            return true;
        }
        return false;
    }
    
    public void launchCart() {
        launchCart(0.6D);
    }
    
    public void launchCart(final double speed) {
        final ArrayList<Sign> signList = SignUtils.getAdjacentMinecartManiaSignList(getLocation(), 2);
        for (final Sign sign : signList) {
            if (sign.executeAction(this, LaunchMinecartAction.class)) {
                break;
            }
        }
        if (!isMoving()) {
            if (MinecartUtils.validMinecartTrack(minecart.getWorld(), getX(), getY(), getZ(), 2, DirectionUtils.CompassDirection.NORTH)) {
                setMotion(DirectionUtils.CompassDirection.NORTH, speed);
            } else if (MinecartUtils.validMinecartTrack(minecart.getWorld(), getX(), getY(), getZ(), 2, DirectionUtils.CompassDirection.EAST)) {
                setMotion(DirectionUtils.CompassDirection.EAST, speed);
            } else if (MinecartUtils.validMinecartTrack(minecart.getWorld(), getX(), getY(), getZ(), 2, DirectionUtils.CompassDirection.SOUTH)) {
                setMotion(DirectionUtils.CompassDirection.SOUTH, speed);
            } else if (MinecartUtils.validMinecartTrack(minecart.getWorld(), getX(), getY(), getZ(), 2, DirectionUtils.CompassDirection.WEST)) {
                setMotion(DirectionUtils.CompassDirection.WEST, speed);
            }
        }
        
        //Create event, then stop the cart and wait for the results
        final MinecartLaunchedEvent mle = new MinecartLaunchedEvent(this, minecart.getVelocity().clone());
        stopCart();
        MinecartManiaCore.callEvent(mle);
        if (mle.isActionTaken())
            return;
        else {
            minecart.setVelocity(mle.getLaunchSpeed());
        }
    }
    
    public void setMotion(final CompassDirection direction, final double speed) {
        setMotion(direction.toVector(speed));
        //        if (direction.equals(DirectionUtils.CompassDirection.NORTH)) {
        //            setMotionX(-speed);
        //        } else if (direction.equals(DirectionUtils.CompassDirection.SOUTH)) {
        //            setMotionX(speed);
        //        } else if (direction.equals(DirectionUtils.CompassDirection.EAST)) {
        //            setMotionZ(-speed);
        //        } else if (direction.equals(DirectionUtils.CompassDirection.WEST)) {
        //            setMotionZ(speed);
        //        } else
        //            throw new IllegalArgumentException();
    }
    
    public boolean doEjectorBlock() {
        if (ControlBlockList.isValidEjectorBlock(this)) {
            if (minecart.getPassenger() != null) {
                final double ejectY = ControlBlockList.getControlBlock(getSpecificMaterialBeneath()).getEjectY();
                final MinecartPassengerEjectEvent mpee = new MinecartPassengerEjectEvent(this, minecart.getPassenger());
                MinecartManiaCore.callEvent(mpee);
                if (!mpee.isCancelled()) {
                    final Entity passenger = minecart.getPassenger();
                    if (minecart.eject()) {
                        final Location dest = passenger.getLocation();
                        dest.setY(dest.getY() + ejectY);
                        passenger.teleport(dest);
                        return true;
                    }
                    return false;
                }
            }
        }
        return false;
    }
    
    public void doRealisticFriction() {
        if ((minecart.getPassenger() == null) && isOnRails()) {
            multiplyMotion(1.0385416D);
        }
    }
    
    public boolean isOnRails() {
        return MinecartUtils.isTrack(minecart.getLocation());
    }
    
    /**
     ** Determines whether or not the track the minecart is currently on is the center piece of a large track intersection. Returns true if it is an intersection.
     **/
    public boolean isAtIntersection() {
        if (isOnRails())
            return MinecartUtils.isAtIntersection(minecart.getWorld(), getX(), getY(), getZ());
        return false;
    }
    
    public Block getBlockTypeAhead() {
        return DirectionUtils.getBlockTypeAhead(minecart.getWorld(), getDirectionOfMotion(), getX(), getY(), getZ());
    }
    
    public Block getBlockTypeBehind() {
        return DirectionUtils.getBlockTypeAhead(minecart.getWorld(), DirectionUtils.getOppositeDirection(getDirectionOfMotion()), getX(), getY(), getZ());
    }
    
    public void updateCalendar() {
        final Calendar current = Calendar.getInstance();
        if (cal.get(Calendar.SECOND) != current.get(Calendar.SECOND)) {
            final MinecartTimeEvent e = new MinecartTimeEvent(this, cal, current);
            MinecartManiaCore.callEvent(e);
            cal = current;
        }
    }
    
    public MinecartManiaMinecart getAdjacentMinecartFromDirection(final DirectionUtils.CompassDirection direction) {
        if (direction == DirectionUtils.CompassDirection.NORTH)
            return MinecartManiaWorld.getMinecartManiaMinecartAt(getX() - 1, getY(), getZ());
        if (direction == DirectionUtils.CompassDirection.EAST)
            return MinecartManiaWorld.getMinecartManiaMinecartAt(getX(), getY(), getZ() - 1);
        if (direction == DirectionUtils.CompassDirection.SOUTH)
            return MinecartManiaWorld.getMinecartManiaMinecartAt(getX() + 1, getY(), getZ());
        if (direction == DirectionUtils.CompassDirection.WEST)
            return MinecartManiaWorld.getMinecartManiaMinecartAt(getX(), getY(), getZ() + 1);
        return null;
    }
    
    public MinecartManiaMinecart getMinecartAhead() {
        return getAdjacentMinecartFromDirection(getDirection());
    }
    
    public MinecartManiaMinecart getMinecartBehind() {
        return getAdjacentMinecartFromDirection(DirectionUtils.getOppositeDirection(getDirection()));
    }
    
    public ArrayList<Block> getParallelBlocks() {
        final ArrayList<Block> blocks = new ArrayList<Block>(4);
        final Block occupied = getOccupiedBlock();
        blocks.add(occupied.getRelative(-1, 0, 0));
        blocks.add(occupied.getRelative(1, 0, 0));
        blocks.add(occupied.getRelative(0, 0, -1));
        blocks.add(occupied.getRelative(0, 0, 1));
        return blocks;
    }
    
    public ArrayList<Block> getPreviousLocationParallelBlocks() {
        final ArrayList<Block> blocks = new ArrayList<Block>(4);
        final Block occupied = getPrevLocation().getBlock();
        blocks.add(occupied.getRelative(-1, 0, 0));
        blocks.add(occupied.getRelative(1, 0, 0));
        blocks.add(occupied.getRelative(0, 0, -1));
        blocks.add(occupied.getRelative(0, 0, 1));
        return blocks;
    }
    
    public HashSet<Block> getAdjacentBlocks(final int range) {
        return BlockUtils.getAdjacentBlocks(getLocation(), range);
    }
    
    public HashSet<Block> getPreviousLocationAdjacentBlocks(final int range) {
        return BlockUtils.getAdjacentBlocks(getPrevLocation(), range);
    }
    
    public HashSet<Block> getBlocksBeneath(final int range) {
        return BlockUtils.getBlocksBeneath(getLocation(), range);
    }
    
    public HashSet<Block> getPreviousLocationBlocksBeneath(final int range) {
        return BlockUtils.getBlocksBeneath(getPrevLocation(), range);
    }
    
    public boolean isMovingAway(final Location l) {
        //North of us
        if ((l.getBlockX() - getX()) < 0) {
            if (getDirection().equals(CompassDirection.SOUTH))
                return true;
        }
        //South of us
        if ((l.getBlockX() - getX()) > 0) {
            if (getDirection().equals(CompassDirection.NORTH))
                return true;
        }
        //East of us
        if ((l.getBlockZ() - getZ()) < 0) {
            if (getDirection().equals(CompassDirection.WEST))
                return true;
        }
        //West of us
        if ((l.getBlockZ() + getZ()) > 0) {
            if (getDirection().equals(CompassDirection.WEST))
                return true;
        }
        
        return false;
    }
    
    public void setWasMovingLastTick(final boolean wasMovingLastTick) {
        this.wasMovingLastTick = wasMovingLastTick;
    }
    
    public boolean wasMovingLastTick() {
        return wasMovingLastTick;
    }
    
    public boolean isPoweredMinecart() {
        return minecart instanceof PoweredMinecart;
    }
    
    public boolean isStorageMinecart() {
        return minecart instanceof StorageMinecart;
    }
    
    public boolean isStandardMinecart() {
        return !isPoweredMinecart() && !isStorageMinecart();
    }
    
    public Material getType() {
        if (isPoweredMinecart())
            return Material.POWERED_MINECART;
        if (isStorageMinecart())
            return Material.STORAGE_MINECART;
        
        return Material.MINECART;
    }
    
    /**
     * attempts to find and return the owner of this object, a player or a minecart mania chest. It will fail if the owner is offline, wasn't found, or the chest was destroyed.
     * 
     * @return Player or Minecart Mania Chest that spawned this minecart.
     */
    public Object getOwner() {
        return owner.getRealOwner();
    }
    
    /**
     * Attempts to determine if the given object is the owner if this minecart. Valid datatypes: Entity, Vector, Location, Chest, MinecartManiaChest
     * 
     * @param obj to test
     * @return true if obj represents the owner
     */
    public boolean isOwner(final Object obj) {
        final Object owner = getOwner();
        if (owner == null)
            return false;
        if ((owner instanceof Player) && (obj instanceof Entity))
            return ((Player) owner).getEntityId() == ((Entity) obj).getEntityId();
        if (owner instanceof MinecartManiaChest) {
            if (obj instanceof Vector)
                return ((MinecartManiaChest) owner).getLocation().equals(((Vector) obj).toLocation(getWorld()));
            if (obj instanceof Location)
                return ((MinecartManiaChest) owner).getLocation().equals(obj);
            if (obj instanceof Chest)
                return ((MinecartManiaChest) owner).getLocation().equals(((Chest) obj).getBlock().getLocation());
            if (obj instanceof MinecartManiaChest)
                return ((MinecartManiaChest) owner).getLocation().equals(((MinecartManiaChest) obj).getLocation());
        }
        return false;
    }
    
    public void kill() {
        kill(true);
    }
    
    public void kill(final boolean returnToOwner) {
        if (!isDead()) {
            
            if (returnToOwner) {
                //give the items back inside too
                final ArrayList<ItemStack> items = new ArrayList<ItemStack>();
                if (isStorageMinecart()) {
                    for (final ItemStack i : ((MinecartManiaStorageCart) this).getContents()) {
                        if ((i != null) && (i.getTypeId() != 0)) {
                            items.add(i);
                        }
                    }
                }
                if (!(Boolean) MinecartManiaWorld.getConfigurationValue("RemoveDeadMinecarts")) {
                    items.add(new ItemStack(getType(), 1));
                }
                
                final Object owner = getOwner();
                MinecartManiaInventory inventory = null;
                Player invOwner = null;
                if ((owner instanceof Player) && MinecartManiaConfiguration.isReturnMinecartToOwner()) {
                    inventory = MinecartManiaWorld.getMinecartManiaPlayer((Player) owner);
                } else if ((owner instanceof MinecartManiaChest) && MinecartManiaConfiguration.isReturnMinecartToOwner()) {
                    inventory = ((MinecartManiaChest) owner);
                    final String temp = ((MinecartManiaChest) owner).getOwner();
                    if (temp != null) {
                        invOwner = Bukkit.getServer().getPlayer(temp);
                    }
                }
                
                if (inventory != null) {
                    for (int i = 0; i < items.size(); i++) {
                        try {
                            if (!inventory.addItem(items.get(i), invOwner)) {
                                minecart.getWorld().dropItemNaturally(minecart.getLocation(), items.get(i));
                            }
                        } catch (final Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                } else {
                    for (final ItemStack i : items) {
                        minecart.getWorld().dropItemNaturally(minecart.getLocation(), i);
                    }
                }
            }
            
            //Fire destroyed event
            final MinecartManiaMinecartDestroyedEvent mmmee = new MinecartManiaMinecartDestroyedEvent(this);
            MinecartManiaCore.callEvent(mmmee);
            
            ChunkManager.unloadChunks(minecart);
            
            minecart.remove();
            dead = true;
        }
    }
    
    public void setRange(final int range) {
        this.range = range;
    }
    
    public int getRange() {
        return range;
    }
    
    public void setRangeY(final int range) {
        rangeY = range;
    }
    
    public int getRangeY() {
        return rangeY;
    }
    
    public void updateChunks() {
        if (MinecartManiaConfiguration.isKeepMinecartsLoaded()) {
            ChunkManager.updateChunks(minecart);
        }
    }
    
    public boolean isApproaching(final Vector v) {
        if (!isMoving())
            return false;
        final CompassDirection direction = getDirectionOfMotion();
        if (direction == CompassDirection.NORTH) {
            if (((minecart.getLocation().getX() - v.getX()) < 3.0D) && ((minecart.getLocation().getX() - v.getX()) > 0.0D))
                return Math.abs(minecart.getLocation().getZ() - v.getZ()) < 1.5D;
        }
        if (direction == CompassDirection.SOUTH) {
            if (((minecart.getLocation().getX() - v.getX()) > -3.0D) && ((minecart.getLocation().getX() - v.getX()) < 0.0D))
                return Math.abs(minecart.getLocation().getZ() - v.getZ()) < 1.5D;
        }
        if (direction == CompassDirection.EAST) {
            if (((minecart.getLocation().getZ() - v.getZ()) < 3.0D) && ((minecart.getLocation().getZ() - v.getZ()) > 0.0D))
                return Math.abs(minecart.getLocation().getX() - v.getX()) < 1.5D;
        }
        if (direction == CompassDirection.WEST) {
            if (((minecart.getLocation().getZ() - v.getZ()) > -3.0D) && ((minecart.getLocation().getZ() - v.getZ()) < 0.0D))
                return Math.abs(minecart.getLocation().getX() - v.getX()) < 1.5D;
        }
        
        return false;
    }
    
    public MinecartManiaMinecart copy(final Minecart newMinecart) {
        final MinecartManiaMinecart newCopy = MinecartManiaWorld.getMinecartManiaMinecart(newMinecart);
        newCopy.cal = cal;
        newCopy.data = data;
        newCopy.range = range;
        newCopy.owner = owner;
        newCopy.previousFacingDir = previousFacingDir;
        newCopy.previousLocation = previousLocation;
        newCopy.previousMotion = previousMotion;
        newCopy.wasMovingLastTick = wasMovingLastTick;
        
        return newCopy;
    }
    
    public boolean doElevatorBlock() {
        if (ControlBlockList.isValidElevatorBlock(this)) {
            //Get where we are
            final Block elevatorBlock = getBlockBeneath();
            final int y = elevatorBlock.getY();
            //Find the closest elevator block. yOffset of 1 above us is our own track, so we can't look there, so don't.
            //If we start yOffset = 2, we will miss the possible track directly below us. It won't fit a person, but will fit a storage cart.
            for (int yOffset = 1; yOffset < 128; yOffset++) {
                if (((y + yOffset) < 128) && (yOffset > 1)) {
                    //See if we have a valid destination
                    if (MinecartUtils.isTrack(elevatorBlock.getRelative(0, yOffset, 0)) && ControlBlockList.isElevatorBlock(blockToSpecificMaterial(elevatorBlock.getRelative(0, yOffset - 1, 0)))) {
                        //do the teleport and return
                        final MinecartElevatorEvent event = new MinecartElevatorEvent(this, elevatorBlock.getRelative(0, yOffset, 0).getLocation());
                        MinecartManiaCore.callEvent(event);
                        if (!event.isCancelled())
                            return minecart.teleport(event.getTeleportLocation());
                    }
                }
                if ((y - yOffset) > 0) {
                    //See if we have a valid destination
                    if (MinecartUtils.isTrack(elevatorBlock.getRelative(0, -yOffset, 0)) && ControlBlockList.isElevatorBlock(blockToSpecificMaterial(elevatorBlock.getRelative(0, -yOffset - 1, 0)))) {
                        //do the teleport and return
                        final MinecartElevatorEvent event = new MinecartElevatorEvent(this, elevatorBlock.getRelative(0, -yOffset, 0).getLocation());
                        MinecartManiaCore.callEvent(event);
                        if (!event.isCancelled())
                            return minecart.teleport(event.getTeleportLocation());
                    }
                }
            }
        }
        return false;
    }
    
    private SpecificMaterial blockToSpecificMaterial(final Block block) {
        final int type = block.getTypeId();
        final short data = block.getData();
        return new SpecificMaterial(type, data);
    }
    
    public SpecificMaterial getSpecificMaterialBeneath() {
        return blockToSpecificMaterial(getBlockBeneath());
    }
}

