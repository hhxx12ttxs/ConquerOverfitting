package com.afforess.minecartmaniacore;

import java.util.ArrayList;
import java.util.HashMap;

import javax.persistence.OptimisticLockException;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.afforess.minecartmaniacore.config.ControlBlockList;
import com.afforess.minecartmaniacore.config.MinecartManiaConfiguration;
import com.afforess.minecartmaniacore.config.RedstoneState;
import com.afforess.minecartmaniacore.debug.MinecartManiaLogger;
import com.afforess.minecartmaniacore.entity.MinecartManiaPlayer;
import com.afforess.minecartmaniacore.event.ChestPoweredEvent;
import com.afforess.minecartmaniacore.event.MinecartActionEvent;
import com.afforess.minecartmaniacore.event.MinecartClickedEvent;
import com.afforess.minecartmaniacore.event.MinecartDirectionChangeEvent;
import com.afforess.minecartmaniacore.event.MinecartIntersectionEvent;
import com.afforess.minecartmaniacore.event.MinecartManiaListener;
import com.afforess.minecartmaniacore.event.MinecartManiaSignFoundEvent;
import com.afforess.minecartmaniacore.event.MinecartMotionStartEvent;
import com.afforess.minecartmaniacore.event.MinecartMotionStopEvent;
import com.afforess.minecartmaniacore.inventory.MinecartManiaChest;
import com.afforess.minecartmaniacore.minecart.MinecartManiaMinecart;
import com.afforess.minecartmaniacore.minecart.MinecartManiaMinecartDataTable;
import com.afforess.minecartmaniacore.signs.LaunchMinecartAction;
import com.afforess.minecartmaniacore.signs.LaunchPlayerAction;
import com.afforess.minecartmaniacore.signs.MinecartTypeSign;
import com.afforess.minecartmaniacore.signs.SignAction;
import com.afforess.minecartmaniacore.signs.SignManager;
import com.afforess.minecartmaniacore.utils.MinecartUtils;
import com.afforess.minecartmaniacore.utils.SignUtils;
import com.afforess.minecartmaniacore.world.MinecartManiaWorld;
import com.afforess.minecartmaniacore.world.SpecificMaterial;

public class MinecartManiaCoreListener extends MinecartManiaListener {
    private final HashMap<Location, Long> lastSpawn = new HashMap<Location, Long>();
    public static final int CHUNK_RANGE = 4;
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onChunkUnload(final ChunkUnloadEvent event) {
        if (!event.isCancelled()) {
            if (MinecartManiaConfiguration.isKeepMinecartsLoaded()) {
                final ArrayList<MinecartManiaMinecart> minecarts = MinecartManiaWorld.getMinecartManiaMinecartList();
                for (final MinecartManiaMinecart minecart : minecarts) {
                    if (Math.abs(event.getChunk().getX() - minecart.minecart.getLocation().getBlock().getChunk().getX()) > CHUNK_RANGE) {
                        continue;
                    }
                    if (Math.abs(event.getChunk().getZ() - minecart.minecart.getLocation().getBlock().getChunk().getZ()) > CHUNK_RANGE) {
                        continue;
                    }
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        if (MinecartManiaConfiguration.isDisappearOnDisconnect()) {
            final MinecartManiaPlayer player = MinecartManiaWorld.getMinecartManiaPlayer(event.getPlayer());
            if (event.getPlayer().getVehicle() instanceof Minecart) {
                final MinecartManiaMinecart minecart = MinecartManiaWorld.getMinecartManiaMinecart((Minecart) player.getPlayer().getVehicle());
                try {
                    final MinecartManiaMinecartDataTable data = new MinecartManiaMinecartDataTable(minecart, player.getName());
                    MinecartManiaMinecartDataTable.save(data);
                    minecart.kill(false);
                } catch (final Exception e) {
                    MinecartManiaLogger.getInstance().severe("Failed to remove the minecart when " + player.getName() + " disconnected");
                    MinecartManiaLogger.getInstance().log(e.getMessage(), false);
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        if (MinecartManiaConfiguration.isDisappearOnDisconnect()) {
            final MinecartManiaPlayer player = MinecartManiaWorld.getMinecartManiaPlayer(event.getPlayer());
            final MinecartManiaMinecartDataTable data = MinecartManiaMinecartDataTable.getDataTable(player.getName());
            if (data != null) {
                final MinecartManiaMinecart minecart = data.toMinecartManiaMinecart();
                minecart.minecart.setPassenger(player.getPlayer());
                try {
                    MinecartManiaMinecartDataTable.delete(data);
                }
                //Make every effort to delete the entry
                catch (final OptimisticLockException ole) {
                    final String name = event.getPlayer().getName();
                    final Thread deleteEntry = new Thread() {
                        @Override
                        public void run() {
                            try {
                                sleep(5000);
                                MinecartManiaMinecartDataTable.delete(data);
                            } catch (final Exception e) {
                                MinecartManiaLogger.getInstance().severe("Failed to remove the minecart data entry when " + name + " connected");
                                MinecartManiaLogger.getInstance().log(e.getMessage(), false);
                            }
                        }
                    };
                    deleteEntry.start();
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockRedstoneChange(final BlockRedstoneEvent event) {
        if ((event.getOldCurrent() > 0) && (event.getNewCurrent() > 0))
            return;
        final boolean power = event.getNewCurrent() > 0;
        final Block block = event.getBlock();
        
        final int range = 1;
        for (int dx = -(range); dx <= range; dx++) {
            for (int dy = -(range); dy <= range; dy++) {
                for (int dz = -(range); dz <= range; dz++) {
                    final Block b = MinecartManiaWorld.getBlockAt(block.getWorld(), block.getX() + dx, block.getY() + dy, block.getZ() + dz);
                    if (b.getState() instanceof Chest) {
                        final Chest chest = (Chest) b.getState();
                        final MinecartManiaChest mmc = MinecartManiaWorld.getMinecartManiaChest(chest);
                        if (mmc != null) {
                            final boolean previouslyPowered = mmc.isRedstonePower();
                            if (!previouslyPowered && power) {
                                mmc.setRedstonePower(power);
                                final ChestPoweredEvent cpe = new ChestPoweredEvent(mmc, power);
                                MinecartManiaCore.callEvent(cpe);
                            } else if (previouslyPowered && !power) {
                                mmc.setRedstonePower(power);
                                final ChestPoweredEvent cpe = new ChestPoweredEvent(mmc, power);
                                MinecartManiaCore.callEvent(cpe);
                            }
                        }
                    }
                    final SpecificMaterial material = new SpecificMaterial(b.getTypeId(), b.getData());
                    if (ControlBlockList.isSpawnMinecartBlock(material)) {
                        if ((ControlBlockList.getControlBlock(material).getSpawnState() != RedstoneState.Enables) || power) {
                            if ((ControlBlockList.getControlBlock(material).getSpawnState() != RedstoneState.Disables) || !power) {
                                if (MinecartUtils.isTrack(b.getRelative(0, 1, 0).getTypeId())) {
                                    final Long lastSpawn = this.lastSpawn.get(b.getLocation());
                                    if ((lastSpawn == null) || (Math.abs(System.currentTimeMillis() - lastSpawn) > 1000)) {
                                        final Location spawn = b.getLocation().clone();
                                        spawn.setY(spawn.getY() + 1);
                                        final MinecartManiaMinecart minecart = MinecartManiaWorld.spawnMinecart(spawn, getMinecartType(b.getLocation()), null);
                                        this.lastSpawn.put(b.getLocation(), System.currentTimeMillis());
                                        if (ControlBlockList.getLaunchSpeed(SpecificMaterial.convertBlock(b)) != 0.0) {
                                            minecart.launchCart(ControlBlockList.getLaunchSpeed(SpecificMaterial.convertBlock(b)));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private static Material getMinecartType(final Location loc) {
        final ArrayList<com.afforess.minecartmaniacore.signs.Sign> signList = SignUtils.getAdjacentMinecartManiaSignList(loc, 2);
        for (final com.afforess.minecartmaniacore.signs.Sign sign : signList) {
            if (sign instanceof MinecartTypeSign) {
                final MinecartTypeSign type = (MinecartTypeSign) sign;
                if (type.canDispenseMinecartType(Material.MINECART))
                    return Material.MINECART;
                if (type.canDispenseMinecartType(Material.POWERED_MINECART))
                    return Material.POWERED_MINECART;
                if (type.canDispenseMinecartType(Material.STORAGE_MINECART))
                    return Material.STORAGE_MINECART;
            }
        }
        
        //Returns standard minecart by default
        return Material.MINECART;
    }
    
    @Override
    @EventHandler(priority = EventPriority.NORMAL)
    public void onMinecartManiaSignFoundEvent(final MinecartManiaSignFoundEvent event) {
        MinecartManiaLogger.getInstance().debug("MinecartManiaCore - Minecart Mania Sign Found Event");
        com.afforess.minecartmaniacore.signs.Sign sign = event.getSign();
        if (MinecartTypeSign.isMinecartTypeSign(sign)) {
            event.setSign(new MinecartTypeSign(sign));
            sign = event.getSign();
        }
        SignAction action = new LaunchPlayerAction(sign);
        if (action.valid(sign)) {
            sign.addSignAction(action);
        }
        action = new LaunchMinecartAction(sign);
        if (action.valid(sign)) {
            sign.addSignAction(action);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onVehicleUpdate(final VehicleUpdateEvent event) {
        if (event.getVehicle() instanceof Minecart) {
            final Minecart cart = (Minecart) event.getVehicle();
            final MinecartManiaMinecart minecart = MinecartManiaWorld.getMinecartManiaMinecart(cart);
            
            if (minecart.isDead())
                return;
            
            minecart.updateCalendar();
            if (minecart.isMoving()) {
                if (minecart.getDirectionOfMotion() != minecart.getPreviousDirectionOfMotion()) {
                    MinecartManiaCore.callEvent(new MinecartDirectionChangeEvent(minecart, minecart.getPreviousDirectionOfMotion(), minecart.getDirectionOfMotion()));
                    minecart.setPreviousDirectionOfMotion(minecart.getDirectionOfMotion());
                }
            }
            
            //Fire new events
            if (minecart.wasMovingLastTick() && !minecart.isMoving()) {
                final MinecartMotionStopEvent mmse = new MinecartMotionStopEvent(minecart);
                MinecartManiaCore.callEvent(mmse);
                mmse.logProcessTime();
            } else if (!minecart.wasMovingLastTick() && minecart.isMoving()) {
                final MinecartMotionStartEvent mmse = new MinecartMotionStartEvent(minecart);
                MinecartManiaCore.callEvent(mmse);
                mmse.logProcessTime();
            }
            minecart.setWasMovingLastTick(minecart.isMoving());
            minecart.doRealisticFriction();
            minecart.doLauncherBlock();
            minecart.undoPoweredRails();
            
            //total hack workaround because of the inability to create runnables/threads w/o IllegalAccessError
            if (minecart.getDataValue("launch") != null) {
                minecart.launchCart();
                minecart.setDataValue("launch", null);
            }
            
            if (minecart.hasChangedPosition() || minecart.createdLastTick) {
                minecart.updateChunks();
                if (minecart.isAtIntersection()) {
                    final MinecartIntersectionEvent mie = new MinecartIntersectionEvent(minecart);
                    MinecartManiaCore.callEvent(mie);
                    mie.logProcessTime();
                }
                
                final MinecartActionEvent mae = new MinecartActionEvent(minecart);
                if (!minecart.createdLastTick) {
                    MinecartManiaCore.callEvent(mae);
                    mae.logProcessTime();
                }
                
                minecart.doSpeedMultiplierBlock();
                minecart.doCatcherBlock();
                minecart.doPlatformBlock(); //platform must be after catcher block
                minecart.doElevatorBlock();
                minecart.doEjectorBlock();
                
                MinecartUtils.updateNearbyItems(minecart);
                
                minecart.updateMotion();
                minecart.updateLocation();
                
                //should do last
                minecart.doKillBlock();
                minecart.createdLastTick = false;
            }
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onVehicleDestroy(final VehicleDestroyEvent event) {
        if ((event.getVehicle() instanceof Minecart) && !event.isCancelled()) {
            final MinecartManiaMinecart minecart = MinecartManiaWorld.getMinecartManiaMinecart((Minecart) event.getVehicle());
            minecart.kill(false);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onVehicleDamage(final VehicleDamageEvent event) {
        if (event.getVehicle() instanceof Minecart) {
            final MinecartManiaMinecart minecart = MinecartManiaWorld.getMinecartManiaMinecart((Minecart) event.getVehicle());
            //Start workaround for double damage events
            long lastDamage = -1;
            if (minecart.getDataValue("Last Damage") != null) {
                lastDamage = (Long) minecart.getDataValue("Last Damage");
            }
            if (lastDamage > -1) {
                if ((lastDamage + 100) > System.currentTimeMillis())
                    return;
            }
            minecart.setDataValue("Last Damage", System.currentTimeMillis());
            //End Workaround
            if (!event.isCancelled()) {
                MinecartManiaLogger.getInstance().debug("Damage: " + event.getDamage() + " Existing: " + minecart.minecart.getDamage());
                if (((event.getDamage() * 10) + minecart.minecart.getDamage()) > 40) {
                    minecart.kill();
                    event.setCancelled(true);
                    event.setDamage(0);
                }
                if (minecart.minecart.getPassenger() != null) {
                    if (minecart.isOnRails()) {
                        if ((event.getAttacker() != null) && (event.getAttacker().getEntityId() == minecart.minecart.getPassenger().getEntityId())) {
                            final MinecartClickedEvent mce = new MinecartClickedEvent(minecart);
                            MinecartManiaCore.callEvent(mce);
                            if (mce.isActionTaken()) {
                                event.setDamage(0);
                                event.setCancelled(true);
                            }
                        }
                    }
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onVehicleEntityCollision(final VehicleEntityCollisionEvent event) {
        if (event.getVehicle() instanceof Minecart) {
            final Minecart cart = (Minecart) event.getVehicle();
            final MinecartManiaMinecart minecart = MinecartManiaWorld.getMinecartManiaMinecart(cart);
            final Entity collisioner = event.getEntity();
            
            if (minecart.doCatcherBlock()) {
                event.setCancelled(true);
                event.setCollisionCancelled(true);
                event.setPickupCancelled(true);
                return;
            }
            if (collisioner instanceof LivingEntity) {
                final LivingEntity victim = (LivingEntity) (collisioner);
                if (!(victim instanceof Player) && !(victim instanceof Wolf)) {
                    if (MinecartManiaConfiguration.isMinecartsKillMobs()) {
                        if (minecart.isMoving()) {
                            victim.remove();
                            event.setCancelled(true);
                            event.setCollisionCancelled(true);
                            event.setPickupCancelled(true);
                        }
                    }
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onVehicleEnter(final VehicleEnterEvent event) {
        if (event.isCancelled() || !(event.getVehicle() instanceof Minecart))
            return;
        
        final MinecartManiaMinecart minecart = MinecartManiaWorld.getMinecartManiaMinecart((Minecart) event.getVehicle());
        if (minecart.minecart.getPassenger() != null)
            return;
        if (ControlBlockList.getLaunchSpeed(minecart.getSpecificMaterialBeneath()) != 0.0D) {
            if (!minecart.isMoving()) {
                final ArrayList<Sign> signs = SignUtils.getAdjacentSignList(minecart, 2);
                for (final Sign s : signs) {
                    final com.afforess.minecartmaniacore.signs.Sign sign = SignManager.getSignAt(s.getBlock());
                    if (sign.executeAction(minecart, LaunchPlayerAction.class)) {
                        break;
                    }
                }
            }
        }
    }
    
}

