/*
 * Copyright (c) 2012-2014, Philip Smith (super_tycoon@supertycoon.net)
 * All rights reserved.
 *
 * [Simplified BSD License]
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL PHILIP SMITH BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.supertycoon.mc.asyncblockevents;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.reflect.StructureModifier;
import net.minecraft.server.v1_5_R3.*;
import net.supertycoon.mc.asyncblockevents.api.BonemealResult;
import net.supertycoon.mc.asyncblockevents.api.PsuedoPlayerInteractEvent;
import net.supertycoon.mc.asyncblockevents.api.async.*;
import net.supertycoon.mc.asyncblockevents.api.materials.BlockMeta;
import net.supertycoon.mc.asyncblockevents.api.materials.SkullMeta;
import net.supertycoon.mc.asyncblockevents.api.prefire.*;
import org.bukkit.*;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.defaults.ReloadCommand;
import org.bukkit.command.defaults.TimingsCommand;
import org.bukkit.craftbukkit.v1_5_R3.CraftServer;
import org.bukkit.craftbukkit.v1_5_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_5_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_5_R3.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_5_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class AsyncBlockEvents extends JavaPlugin {
	public static final Collection<ItemStack> EMPTY_STACK = new ArrayList<>(0);

//	static final Map<BlockLocation, AsyncEventType> processing = Collections.synchronizedMap(
//			new HashMap<BlockLocation, AsyncEventType>()
//	);
	static final SynchroRandom random = new SynchroRandom();
	static int vdistance2;

	static AsyncBlockEvents reference;

	static InventoryManager invManager;
	static BlockProcessor processor;
	static AssManager assManager;
	static AsyncPluginManager plManager;

	static Method tripwireUpdateMethod;

	static @Nullable Command stockReload;
	static @Nullable Command stockRl;
	static @Nullable Command stockTimings;

	@Override
	public void onLoad() {
		reference = this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onEnable() {
		// sneaky / ugly bit, use reflection to hot swap PluginManager
		final Object cserver = Bukkit.getServer();
		try {
			final Field pmfield = CraftServer.class.getDeclaredField("pluginManager");
			pmfield.setAccessible(true);
			final Object pmanager = pmfield.get(cserver);

			if (pmanager instanceof SimplePluginManager) {
				synchronized (pmanager) {
					Field temp = SimplePluginManager.class.getDeclaredField("server");
					temp.setAccessible(true);
					final Server server = (Server) temp.get(pmanager);

					temp = SimplePluginManager.class.getDeclaredField("fileAssociations");
					temp.setAccessible(true);
					final Map<Pattern, PluginLoader> fileAssocations = (Map<Pattern, PluginLoader>) temp.get(pmanager);

					temp = SimplePluginManager.class.getDeclaredField("plugins");
					temp.setAccessible(true);
					final List<Plugin> plugins = (List<Plugin>) temp.get(pmanager);

					temp = SimplePluginManager.class.getDeclaredField("lookupNames");
					temp.setAccessible(true);
					final Map<String, Plugin> lookupNames = (Map<String, Plugin>) temp.get(pmanager);

					temp = SimplePluginManager.class.getDeclaredField("updateDirectory");
					temp.setAccessible(true);
					final File updateDirectory = (File) temp.get(pmanager);

					temp = SimplePluginManager.class.getDeclaredField("commandMap");
					temp.setAccessible(true);
					final SimpleCommandMap commandMap = (SimpleCommandMap) temp.get(pmanager);

					temp = SimplePluginManager.class.getDeclaredField("permissions");
					temp.setAccessible(true);
					final Map<String, Permission> permissions = (Map<String, Permission>) temp.get(pmanager);

					temp = SimplePluginManager.class.getDeclaredField("defaultPerms");
					temp.setAccessible(true);
					final Map<Boolean, Set<Permission>> defaultPerms = (Map<Boolean, Set<Permission>>) temp.get(pmanager);

					temp = SimplePluginManager.class.getDeclaredField("permSubs");
					temp.setAccessible(true);
					final Map<String, Map<Permissible, Boolean>> permSubs = (Map<String, Map<Permissible, Boolean>>) temp.get(pmanager);

					temp = SimplePluginManager.class.getDeclaredField("defSubs");
					temp.setAccessible(true);
					final Map<Boolean, Map<Permissible, Boolean>> defSubs = (Map<Boolean, Map<Permissible, Boolean>>) temp.get(pmanager);

					temp = SimplePluginManager.class.getDeclaredField("useTimings");
					temp.setAccessible(true);
					final boolean useTimings = temp.getBoolean(pmanager);

					final AsyncPluginManager replacement = new AsyncPluginManager(
							server, fileAssocations, plugins, lookupNames, updateDirectory, commandMap, permissions, defaultPerms,
							permSubs, defSubs, useTimings, this
					);

					pmfield.set(cserver, replacement);
					plManager = replacement;
					
					getLogger().log(Level.INFO, "Replacement PluginManager successfully injected");
				}
			} else {
				throw new Exception(
						"Server's PluginManager not recognized. Could someone else be overriding it? Is this a modded server?"
				);
			}
		} catch (Exception e) {
			getLogger().log(
					Level.SEVERE, "Could not inject replacement PluginManager. AsyncBlockEvents cannot start", e
			);
			setEnabled(false);
			return;
		}

		// also use reflection to nab a reference to tripwire updater
		try {
			tripwireUpdateMethod = BlockTripwire.class.getDeclaredMethod(
					"d", net.minecraft.server.v1_5_R3.World.class, int.class, int.class, int.class, int.class
			);
			tripwireUpdateMethod.setAccessible(true);
		} catch (Exception e) {
			getLogger().log(Level.SEVERE, "Could not fetch tripwire update logic. AsyncBlockEvents cannot start", e);
			setEnabled(false);
			return;
		}

		// init after potential return above
		vdistance2 = Bukkit.getViewDistance() * 16;
		vdistance2 *= vdistance2;
		invManager = new InventoryManager();
		processor = new BlockProcessor();
		assManager = new AssManager();
		
		stockReload = null;
		stockRl = null;
		stockTimings = null;

		try {
			// Spigot testing
			CraftServer.class.getDeclaredField("spamGuardExclusions");

			// Spigot patching
			try {
				final Field kcfield = SimpleCommandMap.class.getDeclaredField("knownCommands");
				kcfield.setAccessible(true);
				final Map<String, Command> knownCommands = (Map<String, Command>) kcfield.get(plManager.commandMap);

				final AsyncReloadCommand reloadCommand = new AsyncReloadCommand();
				final AsyncTimingsCommand timingsCommand = new AsyncTimingsCommand();

				if (knownCommands.get("reload").getClass().equals(ReloadCommand.class)) {
					stockReload = knownCommands.get("reload");
					knownCommands.put("reload", reloadCommand);
					getLogger().log(Level.INFO, "Spigot compatiblity: 'reload' command successfully overridden");
				} else {
					getLogger().log(
							Level.WARNING, "Spigot compatibility: 'reload' command was NOT overridden as it appears to have already" +
							               " been so. Using '/reload' may not function correctly."
					);
				}
				if (knownCommands.get("rl").getClass().equals(ReloadCommand.class)) {
					stockRl = knownCommands.get("reload");
					knownCommands.put("rl", reloadCommand);
					getLogger().log(Level.INFO, "Spigot compatiblity: 'rl' command successfully overridden");
				} else {
					getLogger().log(
							Level.WARNING, "Spigot compatibility: 'rl' command was NOT overridden as it appears to have already" +
							               " been so. Using '/rl' may not function correctly."
					);
				}
				if (knownCommands.get("timings").getClass().equals(TimingsCommand.class)) {
					stockTimings = knownCommands.get("timings");
					knownCommands.put("timings", timingsCommand);
					getLogger().log(Level.INFO, "Spigot compatiblity: 'timings' command successfully overridden");
				} else {
					getLogger().log(
							Level.WARNING, "Spigot compatibility: 'timings' command was NOT overridden as it appears to have " +
							               "already been so. Using '/timings' may not function correctly."
					);
				}
			} catch (Exception e) {
				getLogger().log(
						Level.SEVERE,
						"Spigot compatiblity layer could not be injected. /reload and /timings will not have full functionality",
				    e
				);
			}
		} catch (Exception e) {
			// do nothing, not on spigot
		}
	}

	public static class AsyncReloadCommand extends ReloadCommand {
		public AsyncReloadCommand() {
			super("reload");
		}

		@Override
		public boolean execute(final CommandSender sender, final String currentAlias, final String[] args) {
			plManager.disablePlugin(reference);
			return super.execute(sender, currentAlias, args);
		}
	}

	public static class AsyncTimingsCommand extends TimingsCommand {
		public AsyncTimingsCommand() {
			super("timings");
		}

		@Override
		public boolean execute(final CommandSender sender, final String currentAlias, final String[] args) {
			if (args.length > 0) {
				if (args[0].equals("on")) {
					plManager.useTimings(true);
					sender.sendMessage("Enabled Timings");
					return false;
				}
				if (args[0].equals("off")) {
					plManager.useTimings(false);
					sender.sendMessage("Disabled Timings");
					return false;
				}
			}
			return super.execute(sender, currentAlias, args);
		}
	}

	@Override
	public void onDisable() {
		// never started in the first place
		if (plManager == null)
			return;
		// set up default PluginManager
		final Object cserver = Bukkit.getServer();
		try {
			final Field pmfield = CraftServer.class.getDeclaredField("pluginManager");
			pmfield.setAccessible(true);
			final Object pmanager = pmfield.get(cserver);

			//noinspection ObjectEquality
			if (pmanager == plManager) {
				//noinspection SynchronizeOnNonFinalField
				synchronized (plManager) {
					final SimplePluginManager spm = new SimplePluginManager(plManager.server, plManager.commandMap);

					Field temp = SimplePluginManager.class.getDeclaredField("fileAssociations");
					temp.setAccessible(true);
					temp.set(spm, plManager.fileAssociations);

					temp = SimplePluginManager.class.getDeclaredField("plugins");
					temp.setAccessible(true);
					temp.set(spm, plManager.plugins);

					temp = SimplePluginManager.class.getDeclaredField("lookupNames");
					temp.setAccessible(true);
					temp.set(spm, plManager.lookupNames);

					temp = SimplePluginManager.class.getDeclaredField("updateDirectory");
					temp.setAccessible(true);
					temp.set(spm, AsyncPluginManager.updateDirectory);

					temp = SimplePluginManager.class.getDeclaredField("permissions");
					temp.setAccessible(true);
					temp.set(spm, plManager.permissions);

					temp = SimplePluginManager.class.getDeclaredField("defaultPerms");
					temp.setAccessible(true);
					temp.set(spm, plManager.defaultPerms);

					temp = SimplePluginManager.class.getDeclaredField("permSubs");
					temp.setAccessible(true);
					temp.set(spm, plManager.permSubs);

					temp = SimplePluginManager.class.getDeclaredField("defSubs");
					temp.setAccessible(true);
					temp.set(spm, plManager.defSubs);

					temp = SimplePluginManager.class.getDeclaredField("useTimings");
					temp.setAccessible(true);
					temp.set(spm, plManager.useTimings);

					pmfield.set(cserver, spm);

					getLogger().log(Level.INFO, "Default PluginManager successfully restored");
				}
			} else {
				throw new Exception(
						"Server's PluginManager not recognized. Could someone else be overriding it? Is this a modded server?"
				);
			}
		} catch (Exception e) {
			getLogger().log(
					Level.SEVERE,
					"Could not inject replacement PluginManager.\n" +
					"DESPITE BEING 'DISABLED', ASYNCBLOCKEVENTS WILL CONTINUTE TO FUNCTION",
					e
			);
		}

		// Spigot patching
		try {
			final Field kcfield = SimpleCommandMap.class.getDeclaredField("knownCommands");
			kcfield.setAccessible(true);
			@SuppressWarnings("unchecked")
			final Map<String, Command> knownCommands = (Map<String, Command>) kcfield.get(plManager.commandMap);

			if (stockReload != null) {
				knownCommands.put("reload", stockReload);
				getLogger().log(Level.INFO, "Default 'reload' command successfully restored");
			}
			if (stockRl != null) {
				knownCommands.put("rl", stockRl);
				getLogger().log(Level.INFO, "Default 'rl' command successfully restored");
			}
			if (stockTimings != null) {
				knownCommands.put("timings", stockTimings);
				getLogger().log(Level.INFO, "Default 'timings' command successfully restored");
			}
		} catch (Exception e) {
			getLogger().log(
					Level.SEVERE,
					"Spigot compatiblity layer could not be removed. /reload and /timings will not have full functionality",
					e
			);
		}
	}

	public boolean onBlockPlace(final BlockPlaceEvent event) {
		// used all over
		final Location loc = event.getBlock().getLocation();
		final BlockLocation set_loc = new BlockLocation(
				loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()
		);

		final boolean cancelled = event.isCancelled();
		event.setCancelled(true);

		// TNT checking
		if (event.getBlock().getType() == Material.TNT && event.getBlock().isBlockIndirectlyPowered()) {
			final BukkitTask[] task = new BukkitTask[1];
			task[0] = Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
				int counter = 0;

				@Override
				public void run() {
					final Collection<TNTPrimed> tnts = event.getBlock().getWorld().getEntitiesByClass(TNTPrimed.class);
					for (final TNTPrimed tnt : tnts) {
						if (tnt.getLocation().distanceSquared(event.getBlock().getLocation()) < 1) {
							tnt.remove();
							task[0].cancel();
							break;
						}
						if (++counter == 100) {
							task[0].cancel();
						}
					}
				}
			}, 0, 1);
		}

		// check for contention
		if (processor.processing.containsKey(set_loc)) {
			if (processor.processing.get(set_loc) != AsyncEventType.BLOCK_PLACE)
				event.getPlayer().sendMessage(ChatColor.GRAY + "[Async] Sorry, this block has a cooldown in effect");
			return false;
		}

		// Async event requires:

		// - have block
		// - get oldstate
		// - get player
		// - get hand item
		// - get newID
		// NEW DATA calculation
		// - 0 drops by default
		// - 0 exp by default
		// - have cancelled
		// COST calculation
		//   - as removed

		final int cost = calculatePlaceCost(event);
		final ItemStack hand = event.getPlayer().getItemInHand();

		final PrefireBlockPlaceEvent prefire_event;
		if (event.getBlock().getType() == Material.SKULL) {
			prefire_event = new PrefireBlockPlaceEvent(
					event.getBlock(), event.getBlockReplacedState(), event.getPlayer(), hand.clone(),
					new SkullMeta(
							calculatePlaceData(event),
							getSkullFace(floor((double) (event.getPlayer().getLocation().getYaw() * 16.0F / 360.0F) + 0.5D) & 15),
							getSkullType(hand.getDurability()),
							((org.bukkit.inventory.meta.SkullMeta) hand.getItemMeta()).getOwner()
					),
					EMPTY_STACK, 0, cancelled,
					cost > 0 ? hand.getType() : null,
					hand.getDurability()
			);
		} else {
			prefire_event = new PrefireBlockPlaceEvent(
					event.getBlock(), event.getBlockReplacedState(), event.getPlayer(), hand.clone(),
					new BlockMeta(event.getBlock().getTypeId(), calculatePlaceData(event)), EMPTY_STACK, 0, cancelled,
					cost > 0 ? hand.getType() : null,
					hand.getDurability()
			);
		}

		plManager.callEvent(prefire_event);

		final AsyncBlockPlaceEvent async_event = new AsyncBlockPlaceEvent(
				prefire_event.block, prefire_event.oldState, prefire_event.player, prefire_event.getHand(),
				prefire_event.getFutureData(), prefire_event.getDrops(), prefire_event.exp, prefire_event.isCancelled(),
				prefire_event.removedType, prefire_event.removedData
		);

		// simulate block usage to prevent abuse of async hand finding
		if (cost > 0) {
			if (async_event.player.getItemInHand().getAmount() == 1) {
				async_event.player.setItemInHand(null);
			} else {
				async_event.player.getItemInHand().setAmount(event.getPlayer().getItemInHand().getAmount() - 1);
			}
		}

		processor.add(
				set_loc, AsyncEventType.BLOCK_PLACE, async_event.getFutureData().id, async_event.getFutureData().data
		);
		Bukkit.getScheduler().runTaskAsynchronously(
				this, new Runnable() {
			@Override
			public void run() {
				plManager.callEvent(async_event);
				Bukkit.getScheduler().runTask(
						AsyncBlockEvents.this, new Runnable() {
					@Override
					public void run() {
						// set block no physics to correctly emulate blockplace
						async_event.block.setTypeIdAndData(
								async_event.getFutureData().id, async_event.getFutureData().data, false
						);
						async_event.block.setData(async_event.getFutureData().data, false);

						// modify base event to account for async changes
						event.setCancelled(async_event.isCancelled());
						plManager.callEvent(event, EventPriority.MONITOR);
						processor.unlock(set_loc);
						if (!event.isCancelled()) {
							final Location middle = new Location(
									async_event.block.getWorld(), async_event.block.getX() + 0.5, async_event.block.getY() + 0.5,
									async_event.block.getZ() + 0.5
							);

							// effects / sound
							final MCSound sound = BlockSound.values()[async_event.getFutureData().id - 1].sound;
							event.getBlock().getWorld().playSound(
									middle, sound.getPlaceSound(), (sound.getVolume() + 1.0F) / 2.0F,
									sound.getPitch() * 0.8F
							);

							// item drop
							for (final ItemStack stack : async_event.getDrops()) {
								async_event.block.getWorld().dropItem(middle, stack);
							}

							// xp drop
							if (async_event.exp > 0) {
								int tempexp = async_event.exp;
								int orb_val;
								while (tempexp > 0) {
									orb_val = EntityExperienceOrb.getOrbValue(tempexp);
									tempexp -= orb_val;
									middle.getWorld().spawn(middle, ExperienceOrb.class).setExperience(orb_val);
								}
							} else if (async_event.exp < 0) {
								// use xp utils to decrement
								new ExperienceManager(async_event.player).changeExp(async_event.exp);
							}

							// actually setting the block
							async_event.getFutureData().apply(async_event.block);

							update(
									async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
									async_event.block.getZ(), async_event.getFutureData().id
							);

							final Material special = Material.getMaterial(async_event.getFutureData().id);
							switch (special) {
								case WOODEN_DOOR:
								case IRON_DOOR_BLOCK:
									final Block check;
									final Block above = async_event.block.getRelative(BlockFace.UP);
									int left = 0, right = 0;
									switch (async_event.getFutureData().data) {
										case 0: // west
											check = async_event.block.getRelative(0, 0, -1);
											if (check.getType() == special && (check.getData() & 0x03) == 0) {
												above.setTypeIdAndData(special.getId(), (byte) 9, true);
												above.setData((byte) 9, true);
											} else {
												left += async_event.block.getRelative(0, 0, -1).getType().isOccluding() ? 1 : 0;
												left += above.getRelative(0, 0, -1).getType().isOccluding() ? 1 : 0;
												right += async_event.block.getRelative(0, 0, 1).getType().isOccluding() ? 1 : 0;
												right += above.getRelative(0, 0, 1).getType().isOccluding() ? 1 : 0;
												if (right > left) {
													above.setTypeIdAndData(special.getId(), (byte) 9, true);
													above.setData((byte) 9, true);
												} else {
													above.setTypeIdAndData(special.getId(), (byte) 8, true);
													above.setData((byte) 8, true);
												}
											}
											break;
										case 1:  // north
											check = async_event.block.getRelative(1, 0, 0);
											if (check.getType() == special && (check.getData() & 0x03) == 1) {
												above.setTypeIdAndData(special.getId(), (byte) 9, true);
												above.setData((byte) 9, true);
											} else {
												left += async_event.block.getRelative(1, 0, 0).getType().isOccluding() ? 1 : 0;
												left += above.getRelative(1, 0, 0).getType().isOccluding() ? 1 : 0;
												right += async_event.block.getRelative(-1, 0, 0).getType().isOccluding() ? 1 : 0;
												right += above.getRelative(-1, 0, 0).getType().isOccluding() ? 1 : 0;
												if (right > left) {
													above.setTypeIdAndData(special.getId(), (byte) 9, true);
													above.setData((byte) 9, true);
												} else {
													above.setTypeIdAndData(special.getId(), (byte) 8, true);
													above.setData((byte) 8, true);
												}
											}
											break;
										case 2:  // east
											check = async_event.block.getRelative(0, 0, 1);
											if (check.getType() == special && (check.getData() & 0x03) == 2) {
												above.setTypeIdAndData(special.getId(), (byte) 9, true);
												above.setData((byte) 9, true);
											} else {
												left += async_event.block.getRelative(0, 0, 1).getType().isOccluding() ? 1 : 0;
												left += above.getRelative(0, 0, 1).getType().isOccluding() ? 1 : 0;
												right += async_event.block.getRelative(0, 0, -1).getType().isOccluding() ? 1 : 0;
												right += above.getRelative(0, 0, -1).getType().isOccluding() ? 1 : 0;
												if (right > left) {
													above.setTypeIdAndData(special.getId(), (byte) 9, true);
													above.setData((byte) 9, true);
												} else {
													above.setTypeIdAndData(special.getId(), (byte) 8, true);
													above.setData((byte) 8, true);
												}
											}
											break;
										default: // case 3, south
											check = async_event.block.getRelative(-1, 0, 0);
											if (check.getType() == special && (check.getData() & 0x03) == 3) {
												above.setTypeIdAndData(special.getId(), (byte) 9, true);
												above.setData((byte) 9, true);
											} else {
												left += async_event.block.getRelative(-1, 0, 0).getType().isOccluding() ? 1 : 0;
												left += above.getRelative(-1, 0, 0).getType().isOccluding() ? 1 : 0;
												right += async_event.block.getRelative(1, 0, 0).getType().isOccluding() ? 1 : 0;
												right += above.getRelative(1, 0, 0).getType().isOccluding() ? 1 : 0;
												if (right > left) {
													above.setTypeIdAndData(special.getId(), (byte) 9, true);
													above.setData((byte) 9, true);
												} else {
													above.setTypeIdAndData(special.getId(), (byte) 8, true);
													above.setData((byte) 8, true);
												}
											}
											break;
									}
									break;
								case BED_BLOCK:
									final Block head;
									switch (async_event.getFutureData().data) {
										case 0: // south
											head = async_event.block.getRelative(BlockFace.SOUTH);
											head.setTypeIdAndData(26, (byte) 8, true);
											head.setData((byte) 8, true);
											break;
										case 1: // west
											head = async_event.block.getRelative(BlockFace.WEST);
											head.setTypeIdAndData(26, (byte) 9, true);
											head.setData((byte) 9, true);
											break;
										case 2: // north
											head = async_event.block.getRelative(BlockFace.NORTH);
											head.setTypeIdAndData(26, (byte) 10, true);
											head.setData((byte) 10, true);
											break;
										default: // case 3, east
											head = async_event.block.getRelative(BlockFace.EAST);
											head.setTypeIdAndData(26, (byte) 11, true);
											head.setData((byte) 11, true);
											break;
									}
									break;
								case RAILS:
								case POWERED_RAIL:
								case DETECTOR_RAIL:
								case ACTIVATOR_RAIL:
									new TrackLogic(
											async_event.block, async_event.block.getWorld(), async_event.block.getX(),
											async_event.block.getY(), async_event.block.getZ()
									).update(
											async_event.block.isBlockIndirectlyPowered(), true
									);
									break;
								case REDSTONE_WIRE:
									RedstoneLogic.update(
											async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
											async_event.block.getZ()
									);
									break;
								case TRIPWIRE:
									updateTripwire(
											async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
											async_event.block.getZ(), async_event.getFutureData().data
									);
									break;
							}
						} else {
							// event cancelled, revert block, fix inventory
							async_event.block.setTypeIdAndData(
									async_event.oldState.getTypeId(), async_event.oldState.getRawData(), false
							);
							if (cost > 0) {
								if (async_event.player.getItemInHand().getAmount() == 0) {
									async_event.player.setItemInHand(new ItemStack(async_event.removedType, 1, async_event.removedData));
								} else //noinspection ConstantConditions
									if (
											async_event.player.getItemInHand().getType() == async_event.removedType
											&& async_event.player.getItemInHand().getDurability() == async_event.removedData
											&& async_event.player.getItemInHand().getAmount() != async_event.removedType.getMaxStackSize()
											) {
										async_event.player.getItemInHand().setAmount(async_event.player.getItemInHand().getAmount() + 1);
									} else {
										async_event.player.getInventory().addItem(
												new ItemStack(async_event.removedType, 1, async_event.removedData)
										);
									}
							}
						}
						processor.remove(set_loc);
					}
				}
				);
			}
		}
		);
		return true;
	}

	public boolean onBlockBreak(final BlockBreakEvent event) {
		// used all over
		final Location loc = event.getBlock().getLocation();
		final BlockLocation set_loc = new BlockLocation(
				loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()
		);

		final boolean cancelled = event.isCancelled();
		event.setCancelled(true);

		// check for contention
		if (processor.processing.containsKey(set_loc)) {
			if (processor.processing.get(set_loc) != AsyncEventType.BLOCK_BREAK)
				event.getPlayer().sendMessage(ChatColor.GRAY + "[Async] Sorry, this block has a cooldown in effect");
			return false;
		}

		// Async event requires:

		// - have block
		// - capture state
		// - get player
		// - get tool
		// NEWMAT calculation
		// - new data always 0
		// DROPS calculation
		// - get exp
		// - have cancelled
		// DURABILITY calculation

		final ItemStack hand = event.getPlayer().getItemInHand();
		
		final PrefireBlockBreakEvent prefire_event = new PrefireBlockBreakEvent(
				event.getBlock(), event.getBlock().getState(), event.getPlayer(), hand.clone(),
				new BlockMeta(getNewMat(event).getId(), (byte) 0), calculateDrops(event), event.getExpToDrop(), cancelled,
				calculateDurability(event)
		);
		
		plManager.callEvent(prefire_event);

		final AsyncBlockBreakEvent async_event = new AsyncBlockBreakEvent(
				prefire_event.block, prefire_event.oldState, prefire_event.player, prefire_event.getHand(),
				prefire_event.getFutureData(), prefire_event.getDrops(), prefire_event.exp, prefire_event.isCancelled(),
				prefire_event.durability
		);

		processor.add(
				set_loc, AsyncEventType.BLOCK_BREAK, async_event.getFutureData().id, async_event.getFutureData().data
		);
		Bukkit.getScheduler().runTaskAsynchronously(
				this, new Runnable() {
			@Override
			public void run() {
				plManager.callEvent(async_event);
				Bukkit.getScheduler().runTask(
						AsyncBlockEvents.this, new Runnable() {
					@Override
					public void run() {
						// modify base event to account for async changes
						event.setCancelled(async_event.isCancelled());
						event.setExpToDrop(async_event.exp);
						plManager.callEvent(event, EventPriority.MONITOR);
						processor.unlock(set_loc);
						if (!event.isCancelled()) {
							final Location middle = new Location(
									async_event.block.getWorld(), async_event.block.getX() + 0.5, async_event.block.getY() + 0.5,
									async_event.block.getZ() + 0.5
							);

							// tripwire exception
							if (async_event.block.getType() == Material.TRIPWIRE && hand.getType() == Material.SHEARS) {
								async_event.block.setData((byte) (async_event.block.getData() | 8));
							}

							// effects / sound
							for (final Player wplayer : async_event.block.getWorld().getPlayers()) {
								if (wplayer.equals(async_event.player))
									continue;
								if (wplayer.getLocation().distanceSquared(middle) > 16 * 16)
									continue;
								wplayer.playEffect(middle, Effect.STEP_SOUND, async_event.block.getTypeId());
							}

							// item drop
							for (final ItemStack stack : async_event.getDrops()) {
								async_event.block.getWorld().dropItem(middle, stack);
							}

							// xp drop - uses base event since it supported modification
							if (event.getExpToDrop() > 0) {
								int tempexp = event.getExpToDrop();
								int orb_val;
								while (tempexp > 0) {
									orb_val = EntityExperienceOrb.getOrbValue(tempexp);
									tempexp -= orb_val;
									middle.getWorld().spawn(middle, ExperienceOrb.class).setExperience(orb_val);
								}
							} else if (event.getExpToDrop() < 0) {
								// use xp utils to decrement
								new ExperienceManager(async_event.player).changeExp(event.getExpToDrop());
							}

							// durability
							if (async_event.durability != 0) {
								hand.setDurability((short) (hand.getDurability() + async_event.durability));
								if (hand.getDurability() > hand.getType().getMaxDurability()) {
									async_event.player.getInventory().remove(hand);
								}
								//								async_event.player.updateInventory();
							}

							// actually setting the block
							async_event.getFutureData().apply(async_event.block);
							update(
									async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
									async_event.block.getZ(), async_event.getFutureData().id
							);

							// NOTE: uses old block-break logic, so no need to break other, though drops still happen in creative
						} else {
							// event cancelled, send correct block
							processor.sendBlock(set_loc, async_event.block.getTypeId(), async_event.block.getData());

							// resend sign data if was sign
							if (
									async_event.oldState.getType() == Material.WALL_SIGN
									|| async_event.oldState.getType() == Material.SIGN_POST
									) {
								processor.sendSignUpdate(set_loc, ((Sign) async_event.oldState).getLines());
							}
						}
						processor.remove(set_loc);
					}
				}
				);
			}
		}
		);
		return true;
	}

	public boolean onBlockInteract(final PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.PHYSICAL)
			return false;

		// used all over
		final Location loc = event.getClickedBlock().getLocation();
		final BlockLocation set_loc = new BlockLocation(
				loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()
		);
		final @Nullable BlockLocation final_door_loc;

		if (event.getAction() == Action.PHYSICAL && !assManager.doubleTap(set_loc, event.getPlayer())) {
			event.setCancelled(true);
			return false;
		}

		// Async event requires:

		// - have block
		// - get oldstate
		// - get player
		// - get hand item
		// - get newID
		// NEW DATA CALCULATION
		// DROPS CALCULATION
		// - 0 exp by default
		// - have cancelled
		// --- per case item removal

		final Block block = event.getClickedBlock();
		final BlockState oldState = block.getState();
		final Player player = event.getPlayer();
		final ItemStack hand = player.getItemInHand();
		final boolean cancelled = event.isCancelled();

		final PrefireBlockInteractEvent prefire_event;

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (event.getPlayer().isSneaking() && hand.getType() != Material.AIR)
				return false;
			switch (block.getType()) {
				case LEVER:
					prefire_event = new PrefireBlockInteractEvent(
							block, oldState, player, hand.clone(), new BlockMeta(block.getTypeId(), (byte) (block.getData() ^ 8)),
							EMPTY_STACK, 0, cancelled, null, (short) 0
					);
					break;
				case STONE_BUTTON:
				case WOOD_BUTTON:
					if ((block.getData() & 8) == 8)
						return false;
					prefire_event = new PrefireBlockInteractEvent(
							block, oldState, player, hand.clone(), new BlockMeta(block.getTypeId(), (byte) (block.getData() ^ 8)),
							EMPTY_STACK, 0, cancelled, null, (short) 0
					);
					break;
				case NOTE_BLOCK:
					prefire_event = new PrefireBlockInteractEvent(
							block, oldState, player, hand.clone(), new BlockMeta(block.getTypeId(), (byte) 0), EMPTY_STACK, 0, cancelled,
							null, (short) 0
					);
					break;
				case WOODEN_DOOR:
					if ((block.getData() & 8) == 8) { // top half
						final Block bottom = block.getRelative(BlockFace.DOWN);
						prefire_event = new PrefireBlockInteractEvent(
								bottom, bottom.getState(), player, hand.clone(),
								new BlockMeta(bottom.getTypeId(), (byte) (bottom.getData() ^ 4)), EMPTY_STACK, 0, cancelled, null,
								(short) 0
						);
						break;
					}
					prefire_event = new PrefireBlockInteractEvent(
							block, oldState, player, hand.clone(), new BlockMeta(block.getTypeId(), (byte) (block.getData() ^ 4)),
							EMPTY_STACK, 0, cancelled, null, (short) 0
					);
					break;
				case TRAP_DOOR:
					prefire_event = new PrefireBlockInteractEvent(
							block, oldState, player, hand.clone(), new BlockMeta(block.getTypeId(), (byte) (block.getData() ^ 4)),
							EMPTY_STACK, 0, cancelled, null, (short) 0
					);
					break;
				case FENCE_GATE:
					byte newdir = block.getData();
					if ((newdir & 4) == 4) {
						newdir ^= 4;
					} else {
						final int temp1 = (floor((double) (player.getLocation().getYaw() * 4.0F / 360.0F) + 0.5D) & 3) % 4;
						final int temp2 = block.getData() & 3;
						if (temp2 == (temp1 + 2) % 4) {
							newdir = (byte) (temp1 | 4);
						} else {
							newdir |= 4;
						}
					}
					prefire_event = new PrefireBlockInteractEvent(
							block, oldState, player, hand.clone(), new BlockMeta(block.getTypeId(), newdir), EMPTY_STACK, 0, cancelled,
							null, (short) 0
					);
					break;
				case DIODE_BLOCK_OFF:
				case DIODE_BLOCK_ON:
					byte delay = (byte) (block.getData() & 12);
					switch (delay) {
						case 0:
							delay = 4;
							break;
						case 4:
							delay = 8;
							break;
						case 8:
							delay = 12;
							break;
						case 12:
							delay = 0;
							break;
					}
					prefire_event = new PrefireBlockInteractEvent(
							block, oldState, player, hand.clone(),
							new BlockMeta(block.getTypeId(), (byte) (block.getData() & 3 | delay)),
							EMPTY_STACK, 0, cancelled, null, (short) 0
					);
					break;
				case REDSTONE_COMPARATOR_OFF:
				case REDSTONE_COMPARATOR_ON:
					prefire_event = new PrefireBlockInteractEvent(
							block, oldState, player, hand.clone(), new BlockMeta(block.getTypeId(), (byte) (block.getData() ^ 4)),
							EMPTY_STACK, 0, cancelled, null, (short) 0
					);
					break;
				case CAULDRON:
					switch (hand.getType()) {
						case WATER_BUCKET:
							if (block.getData() == 3)
								return false;
							prefire_event = new PrefireBlockInteractEvent(
									block, oldState, player, hand.clone(), new BlockMeta(block.getTypeId(), (byte) 3), EMPTY_STACK, 0,
									cancelled, player.getGameMode() == GameMode.CREATIVE ? null : Material.WATER_BUCKET, (short) 0
							);
							break;
						case GLASS_BOTTLE:
							if (block.getData() == 0)
								return false;
							prefire_event = new PrefireBlockInteractEvent(
									block, oldState, player, hand.clone(), new BlockMeta(block.getTypeId(), (byte) (block.getData() - 1)),
									EMPTY_STACK, 0, cancelled, Material.GLASS_BOTTLE, (short) 0
							);
							break;
						default:
							return false;
					}
					break;
				case CAKE_BLOCK:
					if (player.getGameMode() == GameMode.CREATIVE)
						return false;
					if (player.getFoodLevel() == 20)
						return false;
					prefire_event = new PrefireBlockInteractEvent(
							block, oldState, player, hand.clone(), new BlockMeta(block.getTypeId(), (byte) (block.getData() + 1)),
							EMPTY_STACK, 0, cancelled, null, (short) 0
					);
					break;
				case JUKEBOX:
					if (block.getData() != 0) {
						prefire_event = new PrefireBlockInteractEvent(
								block, oldState, player, hand.clone(), new BlockMeta(block.getTypeId(), (byte) 0), EMPTY_STACK, 0,
								cancelled, null, (short) 0
						);
						break;
					}
					final byte type;
					switch (hand.getType()) {
						case GOLD_RECORD:
							type = 1;
							break;
						case GREEN_RECORD:
							type = 2;
							break;
						case RECORD_3:
							type = 3;
							break;
						case RECORD_4:
							type = 4;
							break;
						case RECORD_5:
							type = 5;
							break;
						case RECORD_6:
							type = 6;
							break;
						case RECORD_7:
							type = 7;
							break;
						case RECORD_8:
							type = 8;
							break;
						case RECORD_9:
							type = 9;
							break;
						case RECORD_10:
							type = 10;
							break;
						case RECORD_11:
							type = 11;
							break;
						case RECORD_12:
							type = 12;
							break;
						default:
							return false;
					}
					prefire_event = new PrefireBlockInteractEvent(
							block, oldState, player, hand.clone(), new BlockMeta(block.getTypeId(), type), EMPTY_STACK, 0, cancelled,
							player.getGameMode() == GameMode.CREATIVE ? null : hand.getType(), (short) 0
					);
					break;
				//			case ENDER_PORTAL_FRAME:
				//
				//				System.out.println("frame clicked");
				//
				//				if ((block.getData() & 4) == 4) {
				//					System.out.println("frame filled");
				//					return;
				//				}
				//				if (hand.getType() != Material.EYE_OF_ENDER) {
				//					System.out.println("not eye");
				//					return;
				//				}
				//				prefire_event = new PrefireBlockInteractEvent(
				//						block, oldState, player, hand.clone(), block.getType().getNewData((byte) (block.getData() | 4)),
				// EMPTY_STACK, 0,
				//						cancelled, player.getGameMode() == GameMode.CREATIVE ? null : Material.EYE_OF_ENDER
				//				);
				//				break;
				case CHEST:
				case ENDER_CHEST:
				case TRAPPED_CHEST:
				case FURNACE:
				case BURNING_FURNACE:
				case DISPENSER:
				case DROPPER:
				case BREWING_STAND:
				case HOPPER:
				case WORKBENCH:
				case ENCHANTMENT_TABLE:
				case ANVIL:
					prefire_event = new PrefireBlockInteractEvent(
							block, oldState, player, hand.clone(), new BlockMeta(block.getTypeId(), block.getData()), EMPTY_STACK, 0,
							cancelled, null, (short) 0
					);
					break;
				case TNT:
					if (hand.getType() != Material.FLINT_AND_STEEL)
						return false;
					prefire_event = new PrefireBlockInteractEvent(
							block, oldState, player, hand.clone(), new BlockMeta(block.getTypeId(), block.getData()), EMPTY_STACK, 0,
							cancelled, null, (short) 0
					);
					break;
				case SAPLING:
				case BROWN_MUSHROOM:
				case RED_MUSHROOM:
					if (hand.getType() != Material.INK_SACK)
						return false;
					if (hand.getDurability() != 15)
						return false;
					prefire_event = new PrefireBlockBonemealEvent(
							block, oldState, player, hand.clone(), new BlockMeta(block.getTypeId(), block.getData()), EMPTY_STACK, 0,
							cancelled, player.getGameMode() == GameMode.CREATIVE ? null : Material.INK_SACK, (short) 15,
							BonemealResult.NORMAL
					);
					break;
				default:
					return false;
			}
		} else if (event.getAction() == Action.PHYSICAL) {
			switch (block.getType()) {
				case STONE_PLATE:
				case WOOD_PLATE:
					prefire_event = new PrefireBlockInteractEvent(
							block, oldState, player, hand.clone(), new BlockMeta(block.getTypeId(), (byte) 1), EMPTY_STACK, 0,
							cancelled, null, (short) 0
					);
					break;
				case TRIPWIRE:
					prefire_event = new PrefireBlockInteractEvent(
							block, oldState, player, hand.clone(), new BlockMeta(block.getTypeId(), (byte) (block.getData() | 1)),
							EMPTY_STACK, 0, cancelled, null, (short) 0
					);
					break;
				default:
					return false;
			}
		} else {
			return false;
		}

		event.setCancelled(true);

		// check for contention
		if (processor.processing.containsKey(set_loc)) {
			if (processor.processing.get(set_loc) != AsyncEventType.BLOCK_INTERACT)
				event.getPlayer().sendMessage(ChatColor.GRAY + "[Async] Sorry, this block has a cooldown in effect");
			return false;
		}

		plManager.callEvent(prefire_event);

		final AsyncBlockInteractEvent async_event;
		if (prefire_event instanceof PrefireBlockBonemealEvent) {
			final PrefireBlockBonemealEvent temp = (PrefireBlockBonemealEvent) prefire_event;
			async_event = new AsyncBlockBonemealEvent(
					temp.block, temp.oldState, temp.player, temp.getHand(), temp.getFutureData(), temp.getDrops(), temp.exp,
					temp.isCancelled(), temp.removedType, temp.removedData, temp.result
			);
		} else {
			async_event = new AsyncBlockInteractEvent(
					prefire_event.block, prefire_event.oldState, prefire_event.player, prefire_event.getHand(),
					prefire_event.getFutureData(), prefire_event.getDrops(), prefire_event.exp, prefire_event.isCancelled(),
					prefire_event.removedType, prefire_event.removedData
			);
		}

		// take item
		if (async_event.removedType != null) {
			if (async_event.player.getItemInHand().getAmount() == 1) {
				async_event.player.setItemInHand(null);
			} else {
				async_event.player.getItemInHand().setAmount(event.getPlayer().getItemInHand().getAmount() - 1);
			}
		}

		if (Material.getMaterial(async_event.getFutureData().id) == Material.WOODEN_DOOR
		    && async_event.block.getRelative(BlockFace.UP).getType() == Material.WOODEN_DOOR) {
			final BlockLocation door_bottom = new BlockLocation(
					async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(), async_event.block.getZ()
			);
			final BlockLocation door_up = new BlockLocation(
					door_bottom.world, door_bottom.x, door_bottom.y + 1, door_bottom.z
			);
			processor.add(door_bottom, AsyncEventType.BLOCK_INTERACT, 64, async_event.getFutureData().data);
			processor.add(door_up, AsyncEventType.BLOCK_INTERACT, 64, async_event.block.getRelative(BlockFace.UP).getData());
			if (door_bottom.equals(set_loc)) {
				final_door_loc = door_up;
			} else {
				final_door_loc = door_bottom;
			}
		} else if (Material.getMaterial(async_event.getFutureData().id) == Material.CAKE_BLOCK
		           && async_event.getFutureData().data == 6) {
			processor.add(set_loc, AsyncEventType.BLOCK_INTERACT, 0, 0);
			final_door_loc = null;
		} else {
			processor.add(
					set_loc, AsyncEventType.BLOCK_INTERACT, async_event.getFutureData().id, async_event.getFutureData().data
			);
			final_door_loc = null;
		}

		Bukkit.getScheduler().runTaskAsynchronously(
				this, new Runnable() {
			@Override
			public void run() {
				plManager.callEvent(async_event);
				Bukkit.getScheduler().runTask(
						AsyncBlockEvents.this, new Runnable() {
					@Override
					public void run() {
						// modify base event to account for async changes
						event.setCancelled(async_event.isCancelled());
						plManager.callEvent(event, EventPriority.MONITOR);
						processor.unlock(set_loc);
						if (final_door_loc != null)
							processor.unlock(final_door_loc);
						if (!event.isCancelled()) {
							final Location middle = new Location(
									async_event.block.getWorld(), async_event.block.getX() + 0.5, async_event.block.getY() + 0.5,
									async_event.block.getZ() + 0.5
							);

							// item drop
							for (final ItemStack stack : async_event.getDrops()) {
								async_event.block.getWorld().dropItem(middle, stack);
							}

							// xp drop
							if (async_event.exp > 0) {
								int tempexp = async_event.exp;
								int orb_val;
								while (tempexp > 0) {
									orb_val = EntityExperienceOrb.getOrbValue(tempexp);
									tempexp -= orb_val;
									middle.getWorld().spawn(middle, ExperienceOrb.class).setExperience(orb_val);
								}
							} else if (async_event.exp < 0) {
								// use xp utils to decrement
								new ExperienceManager(async_event.player).changeExp(async_event.exp);
							}

							final Block attached;
							final InventoryView iv;
							final InventoryOpenEvent ioevent;
							final Location location;
							final AsyncBlockChangeDelegate delegate;
							final BlockLocation bloc2;

							final Packet61WorldEvent packet;

							// mat based block setting / effects
							big:
							switch (Material.getMaterial(async_event.getFutureData().id)) {
								case LEVER:
									async_event.block.getWorld().playSound(
											middle, Sound.CLICK, 0.3F, (async_event.getFutureData().data & 8) == 8 ? 0.6F : 0.5F
									);
									async_event.block.setTypeIdAndData(69, async_event.getFutureData().data, false);
									update(
											async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
											async_event.block.getZ(), 69
									);
									switch (async_event.getFutureData().data & 7) {
										case 1:
											attached = async_event.block.getRelative(BlockFace.WEST);
											break;
										case 2:
											attached = async_event.block.getRelative(BlockFace.EAST);
											break;
										case 3:
											attached = async_event.block.getRelative(BlockFace.NORTH);
											break;
										case 4:
											attached = async_event.block.getRelative(BlockFace.SOUTH);
											break;
										case 5:
										case 6:
											attached = async_event.block.getRelative(BlockFace.DOWN);
											break;
										default: // 7 and 0
											attached = async_event.block.getRelative(BlockFace.UP);
											break;
									}
									update(attached.getWorld(), attached.getX(), attached.getY(), attached.getZ(), attached.getTypeId());
									break;
								case STONE_BUTTON:
									async_event.block.getWorld().playSound(middle, Sound.CLICK, 0.3F, 0.6F);
									async_event.block.setTypeIdAndData(77, async_event.getFutureData().data, false);
									update(
											async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
											async_event.block.getZ(), 77
									);
									switch (async_event.getFutureData().data & 7) {
										case 1:
											attached = async_event.block.getRelative(BlockFace.WEST);
											break;
										case 2:
											attached = async_event.block.getRelative(BlockFace.EAST);
											break;
										case 3:
											attached = async_event.block.getRelative(BlockFace.NORTH);
											break;
										default: // 4
											attached = async_event.block.getRelative(BlockFace.SOUTH);
											break;
									}
									update(attached.getWorld(), attached.getX(), attached.getY(), attached.getZ(), attached.getTypeId());
									Bukkit.getScheduler().runTaskLater(
											AsyncBlockEvents.this, new Runnable() {
										@Override
										public void run() {
											async_event.block.getWorld().playSound(middle, Sound.CLICK, 0.3F, 0.5F);
											async_event.block.setTypeIdAndData(
													77, (byte) (async_event.getFutureData().data ^ 8), false
											);
											update(
													async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
													async_event.block.getZ(), 77
											);
											update(
													attached.getWorld(), attached.getX(), attached.getY(), attached.getZ(), attached.getTypeId()
											);
										}
									}, 20
									);
									break;
								case WOOD_BUTTON:
									async_event.block.getWorld().playSound(middle, Sound.CLICK, 0.3F, 0.6F);
									async_event.block.setTypeIdAndData(143, async_event.getFutureData().data, false);
									update(
											async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
											async_event.block.getZ(), 143
									);
									switch (async_event.getFutureData().data & 7) {
										case 1:
											attached = async_event.block.getRelative(BlockFace.WEST);
											break;
										case 2:
											attached = async_event.block.getRelative(BlockFace.EAST);
											break;
										case 3:
											attached = async_event.block.getRelative(BlockFace.NORTH);
											break;
										default: // 4
											attached = async_event.block.getRelative(BlockFace.SOUTH);
											break;
									}
									update(attached.getWorld(), attached.getX(), attached.getY(), attached.getZ(), attached.getTypeId());
									Bukkit.getScheduler().runTaskLater(
											AsyncBlockEvents.this, new Runnable() {
										@Override
										public void run() {
											async_event.block.getWorld().playSound(middle, Sound.CLICK, 0.3F, 0.5F);
											async_event.block.setTypeIdAndData(
													143, (byte) (async_event.getFutureData().data ^ 8), false
											);
											update(
													async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
													async_event.block.getZ(), 143
											);
											update(
													attached.getWorld(), attached.getX(), attached.getY(), attached.getZ(), attached.getTypeId()
											);
										}
									}, 30
									);
									break;
								case STONE_PLATE:
									if (!(event instanceof PsuedoPlayerInteractEvent))
										async_event.block.getWorld().playSound(middle.subtract(0, 0.4, 0), Sound.CLICK, 0.3F, 0.6F);
									async_event.block.setTypeIdAndData(70, async_event.getFutureData().data, false);
									update(
											async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
									    async_event.block.getZ(), 70
									);
									attached = async_event.block.getRelative(BlockFace.DOWN);
									update(attached.getWorld(), attached.getX(), attached.getY(), attached.getZ(), attached.getTypeId());
									assManager.add(set_loc, ((CraftPlayer) async_event.player).getHandle(), true);
									break;
								case WOOD_PLATE:
									if (!(event instanceof PsuedoPlayerInteractEvent))
										async_event.block.getWorld().playSound(middle.subtract(0, 0.4, 0), Sound.CLICK, 0.3F, 0.6F);
									async_event.block.setTypeIdAndData(72, async_event.getFutureData().data, false);
									update(
											async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
									    async_event.block.getZ(), 72
									);
									attached = async_event.block.getRelative(BlockFace.DOWN);
									update(attached.getWorld(), attached.getX(), attached.getY(), attached.getZ(), attached.getTypeId());
									assManager.add(set_loc, ((CraftPlayer) async_event.player).getHandle(), true);
									break;
								case TRIPWIRE:
									processor.blockSound = true;
									try {
										async_event.block.setTypeIdAndData(132, async_event.getFutureData().data, false);
									} finally {
										processor.blockSound = false;
									}
									async_event.block.setData(async_event.getFutureData().data, false);
									updateTripwire(
											async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
											async_event.block.getZ(), async_event.getFutureData().data
									);
									update(
											async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
											async_event.block.getZ(), 132
									);
									assManager.add(set_loc, ((CraftPlayer) async_event.player).getHandle(), true);
									break;
								case NOTE_BLOCK:
									if (async_event.block.getType() != Material.NOTE_BLOCK) {
										async_event.block.setTypeIdAndData(25, (byte) 0, false);
										update(
												async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
												async_event.block.getZ(), 25
										);
									}
									final NoteBlock nb = (NoteBlock) async_event.block.getState();
									nb.setRawNote((byte) (nb.getRawNote() == 24 ? 0 : nb.getRawNote() + 1));
									nb.play();
									break;
								case WOODEN_DOOR:
									final Location dmiddle = middle.add(0, .5, 0);
									for (final Player wplayer : async_event.block.getWorld().getPlayers()) {
										if (wplayer.equals(async_event.player))
											continue;
										if (wplayer.getLocation().distanceSquared(dmiddle) > 16 * 16)
											continue;
										wplayer.playEffect(dmiddle, Effect.DOOR_TOGGLE, 0);
									}
									async_event.block.setTypeIdAndData(64, async_event.getFutureData().data, false);
									update(
											async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
											async_event.block.getZ(), 64
									);
									break;
								case TRAP_DOOR:
									for (final Player wplayer : async_event.block.getWorld().getPlayers()) {
										if (wplayer.equals(async_event.player))
											continue;
										if (wplayer.getLocation().distanceSquared(middle) > 16 * 16)
											continue;
										wplayer.playEffect(middle, Effect.DOOR_TOGGLE, 0);
									}
									async_event.block.setTypeIdAndData(96, async_event.getFutureData().data, false);
									update(
											async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
											async_event.block.getZ(), 96
									);
									break;
								case WORKBENCH:
									async_event.block.setTypeIdAndData(58, async_event.getFutureData().data, false);
									update(
											async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
											async_event.block.getZ(), 58
									);
									iv = async_event.player.openWorkbench(async_event.block.getLocation(), false);
									ioevent = new InventoryOpenEvent(iv);
									plManager.callEvent(ioevent);
									if (ioevent.isCancelled())
										iv.close();
									break;
								case ENCHANTMENT_TABLE:
									async_event.block.setTypeIdAndData(116, async_event.getFutureData().data, false);
									update(
											async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
											async_event.block.getZ(), 116
									);
									iv = async_event.player.openEnchanting(async_event.block.getLocation(), false);
									ioevent = new InventoryOpenEvent(iv);
									plManager.callEvent(ioevent);
									if (ioevent.isCancelled())
										iv.close();
									break;
								case ENDER_CHEST:
									async_event.block.setTypeIdAndData(130, async_event.getFutureData().data, false);
									update(
											async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
											async_event.block.getZ(), 130
									);
									iv = async_event.player.openInventory(async_event.player.getEnderChest());
									invManager.add(set_loc, async_event.block.getLocation(), async_event.player, iv);
									break;
								case FENCE_GATE:
									for (final Player wplayer : async_event.block.getWorld().getPlayers()) {
										if (wplayer.equals(async_event.player))
											continue;
										if (wplayer.getLocation().distanceSquared(middle) > 16 * 16)
											continue;
										wplayer.playEffect(middle, Effect.DOOR_TOGGLE, 0);
									}
									async_event.block.setTypeIdAndData(107, async_event.getFutureData().data, false);
									update(
											async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
											async_event.block.getZ(), 107
									);
									break;
								case DIODE_BLOCK_OFF:
									async_event.block.setTypeIdAndData(93, async_event.getFutureData().data, false);
									update(
											async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
											async_event.block.getZ(), 93
									);
									break;
								case DIODE_BLOCK_ON:
									async_event.block.setTypeIdAndData(94, async_event.getFutureData().data, false);
									update(
											async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
											async_event.block.getZ(), 94
									);
									break;
								case REDSTONE_COMPARATOR_OFF:
									async_event.block.setTypeIdAndData(149, async_event.getFutureData().data, false);
									update(
											async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
									    async_event.block.getZ(), 149
									);
									break;
								case REDSTONE_COMPARATOR_ON:
									async_event.block.setTypeIdAndData(150, async_event.getFutureData().data, false);
									update(
											async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
											async_event.block.getZ(), 150
									);
									break;
								case CAULDRON:
									async_event.block.setTypeIdAndData(118, async_event.getFutureData().data, false);
									update(
											async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
											async_event.block.getZ(), 118
									);
									final Material togive;
									switch (async_event.removedType) {
										case WATER_BUCKET:
											togive = Material.BUCKET;
											break;
										case GLASS_BOTTLE:
											togive = Material.POTION;
											break;
										default:
											break big;
									}
									if (async_event.player.getItemInHand().getAmount() == 0) {
										async_event.player.setItemInHand(new ItemStack(togive));
									} else if (
											async_event.player.getItemInHand().getType() == togive
											&& async_event.player.getItemInHand().getAmount() != togive.getMaxStackSize()
											) {
										async_event.player.getItemInHand().setAmount(async_event.player.getItemInHand().getAmount() + 1);
									} else {
										async_event.player.getInventory().addItem(new ItemStack(togive));
									}
									break;
								case CAKE_BLOCK:
									if (async_event.getFutureData().data == 6) {
										async_event.block.setTypeIdAndData(0, (byte) 0, false);
									} else {
										async_event.block.setTypeIdAndData(92, async_event.getFutureData().data, false);
									}
									update(
											async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
											async_event.block.getZ(), 92
									);
									async_event.player.setFoodLevel(Math.min(20, async_event.player.getFoodLevel() + 2));
									break;
								case JUKEBOX:
									async_event.block.setTypeIdAndData(84, (byte) 0, false);
									update(
											async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
											async_event.block.getZ(), 84
									);
									final Jukebox jb = (Jukebox) async_event.block.getState();
									switch (async_event.getFutureData().data) {
										case 1:
											jb.setPlaying(Material.GOLD_RECORD);
											break;
										case 2:
											jb.setPlaying(Material.GREEN_RECORD);
											break;
										case 3:
											jb.setPlaying(Material.RECORD_3);
											break;
										case 4:
											jb.setPlaying(Material.RECORD_4);
											break;
										case 5:
											jb.setPlaying(Material.RECORD_5);
											break;
										case 6:
											jb.setPlaying(Material.RECORD_6);
											break;
										case 7:
											jb.setPlaying(Material.RECORD_7);
											break;
										case 8:
											jb.setPlaying(Material.RECORD_8);
											break;
										case 9:
											jb.setPlaying(Material.RECORD_9);
											break;
										case 10:
											jb.setPlaying(Material.RECORD_10);
											break;
										case 11:
											jb.setPlaying(Material.RECORD_11);
											break;
										case 12:
											jb.setPlaying(Material.RECORD_12);
											break;
										default:
											break;

									}
									break;
								//									case ENDER_PORTAL_FRAME:
								//										async_event.block.setTypeIdAndData(
								// 120, async_event.getNewMatData().getData(), false);
								//										update(
								//												async_event.block.getWorld(), async_event.block.getX(),
								// async_event.block.getY(),
								//												async_event.block.getZ(), 120
								//										);
								//										break;
								case CHEST:
									async_event.block.setTypeIdAndData(54, async_event.getFutureData().data, false);
									update(
											async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
											async_event.block.getZ(), 54
									);
									if (async_event.block.getRelative(BlockFace.NORTH).getType() == Material.CHEST) {
										bloc2 = new BlockLocation(
												async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
												async_event.block.getZ() - 1
										);
									} else if (async_event.block.getRelative(BlockFace.WEST).getType() == Material.CHEST) {
										bloc2 = new BlockLocation(
												async_event.block.getWorld(), async_event.block.getX() - 1, async_event.block.getY(),
												async_event.block.getZ()
										);
									} else {
										bloc2 = set_loc;
									}
									iv = async_event.player.openInventory(((Chest) async_event.block.getState()).getInventory());
									invManager.add(bloc2, bloc2.getBukkitLocation(), async_event.player, iv);
									break;
								case TRAPPED_CHEST:
									async_event.block.setTypeIdAndData(146, async_event.getFutureData().data, false);
									update(
											async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
									    async_event.block.getZ(), 146
									);
									if (async_event.block.getRelative(BlockFace.NORTH).getType() == Material.TRAPPED_CHEST) {
										bloc2 = new BlockLocation(
												async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
										    async_event.block.getZ() - 1
										);
									} else if (async_event.block.getRelative(BlockFace.WEST).getType() == Material.TRAPPED_CHEST) {
										bloc2 = new BlockLocation(
												async_event.block.getWorld(), async_event.block.getX() - 1, async_event.block.getY(),
										    async_event.block.getZ()
										);
									} else {
										bloc2 = set_loc;
									}
									iv = async_event.player.openInventory(((Chest) async_event.block.getState()).getInventory());
									invManager.add(bloc2, bloc2.getBukkitLocation(), async_event.player, iv);
									break;
								case DISPENSER:
									async_event.block.setTypeIdAndData(23, async_event.getFutureData().data, false);
									update(
											async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
											async_event.block.getZ(), 23
									);
									iv = async_event.player.openInventory(((Dispenser) async_event.block.getState()).getInventory());
									ioevent = new InventoryOpenEvent(iv);
									plManager.callEvent(ioevent);
									if (ioevent.isCancelled())
										iv.close();
									break;
								case DROPPER:
									async_event.block.setTypeIdAndData(158, async_event.getFutureData().data, false);
									update(
											async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
											async_event.block.getZ(), 158
									);

//									System.out.println("Inv null? -- " + ((Dropper) async_event.block.getState()).getInventory());

//									iv = async_event.player.openInventory(((Dropper) async_event.block.getState()).getInventory());

									((CraftPlayer) async_event.player).getHandle().openDispenser(
											(TileEntityDropper) ((CraftWorld) async_event.block.getWorld()).getTileEntityAt(
													async_event.block.getX(), async_event.block.getY(), async_event.block.getZ()
											)
									);

									((CraftPlayer) async_event.player).getHandle().activeContainer.checkReachable = false;

									iv = ((CraftPlayer) async_event.player).getHandle().activeContainer.getBukkitView();


//									iv = async_event.player.openInventory(
//											new CraftInventory(
//													(TileEntityDropper) ((CraftWorld) async_event.block.getWorld()).getTileEntityAt(
//															async_event.block.getX(), async_event.block.getY(), async_event.block.getZ()
//													)
//											)
//									);

//									System.out.println("Probe -- " + iv);

									ioevent = new InventoryOpenEvent(iv);
									plManager.callEvent(ioevent);
									if (ioevent.isCancelled())
										iv.close();
									break;
								case FURNACE:
									async_event.block.setTypeIdAndData(61, async_event.getFutureData().data, false);
									update(
											async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
											async_event.block.getZ(), 61
									);
									iv = async_event.player.openInventory(((Furnace) async_event.block.getState()).getInventory());
									ioevent = new InventoryOpenEvent(iv);
									plManager.callEvent(ioevent);
									if (ioevent.isCancelled())
										iv.close();
									break;
								case BURNING_FURNACE:
									async_event.block.setTypeIdAndData(62, async_event.getFutureData().data, false);
									update(
											async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
											async_event.block.getZ(), 62
									);
									iv = async_event.player.openInventory(((Furnace) async_event.block.getState()).getInventory());
									ioevent = new InventoryOpenEvent(iv);
									plManager.callEvent(ioevent);
									if (ioevent.isCancelled())
										iv.close();
									break;
								case BREWING_STAND:
									async_event.block.setTypeIdAndData(117, async_event.getFutureData().data, false);
									update(
											async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
											async_event.block.getZ(), 117
									);
									iv = async_event.player.openInventory(((BrewingStand) async_event.block.getState()).getInventory());
									ioevent = new InventoryOpenEvent(iv);
									plManager.callEvent(ioevent);
									if (ioevent.isCancelled())
										iv.close();
									break;
								case HOPPER:
									async_event.block.setTypeIdAndData(154, async_event.getFutureData().data, false);
									update(
											async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
									    async_event.block.getZ(), 154
									);
									iv = async_event.player.openInventory(((Hopper) async_event.block.getState()).getInventory());
									ioevent = new InventoryOpenEvent(iv);
									plManager.callEvent(ioevent);
									if (ioevent.isCancelled())
										iv.close();
									break;
								case ANVIL:
									async_event.block.setTypeIdAndData(145, async_event.getFutureData().data, false);
									update(
											async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
									    async_event.block.getZ(), 145
									);
									((CraftPlayer) async_event.player).getHandle().openAnvil(
											async_event.block.getX(), async_event.block.getY(), async_event.block.getZ()
									);
									((CraftPlayer) async_event.player).getHandle().activeContainer.checkReachable = false;
									iv = ((CraftPlayer) async_event.player).getHandle().activeContainer.getBukkitView();
									ioevent = new InventoryOpenEvent(iv);
									plManager.callEvent(ioevent);
									if (ioevent.isCancelled())
										iv.close();
									break;
								case TNT:
									async_event.block.getWorld().playSound(middle, Sound.FUSE, 1.0F, 1.0F);
									async_event.block.setTypeIdAndData(0, (byte) 0, false);
									update(
											async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
											async_event.block.getZ(), 0
									);
									async_event.block.getWorld().spawnEntity(middle, EntityType.PRIMED_TNT);
									break;
								case SAPLING:
									packet = new Packet61WorldEvent(
											2005, async_event.block.getX(), async_event.block.getY(), async_event.block.getZ(), 0, false
									);
									for (final Player wplayer : async_event.block.getWorld().getPlayers()) {
										if (wplayer.getLocation().distanceSquared(middle) > 64 * 64)
											continue;
										((CraftPlayer) wplayer).getHandle().playerConnection.sendPacket(packet);
									}
									async_event.block.setTypeIdAndData(6, async_event.getFutureData().data, false);
									// HANDLE BONEMEAL EVENT STUFF
									final @Nullable AsyncBlockBonemealEvent temp;
									if (async_event instanceof AsyncBlockBonemealEvent) {
										temp = (AsyncBlockBonemealEvent) async_event;
									} else {
										temp = null;
									}
									if ((temp == null || temp.result == BonemealResult.NORMAL) && random.nextFloat() < 0.45F) {
										break;
									}
									if ((temp == null || temp.result == BonemealResult.NORMAL)
											&& (8 & async_event.getFutureData().data) == 0) {
										async_event.block.setData((byte) (async_event.getFutureData().data | 8), true);
										break;
									} else if (temp != null && temp.result == BonemealResult.FORCE_BYTE) {
										async_event.block.setData(async_event.getFutureData().data, true);
									}
									// DEFAULT TREE GROWING BULLSHIT
									async_event.block.setTypeId(0, false);
									switch (async_event.getFutureData().data) {
										case 0:
										case 8:
											if (random.nextInt(10) == 0) {
												location = async_event.block.getLocation();
												delegate = new AsyncBlockChangeDelegate(location, async_event.player, TreeType.BIG_TREE);
												if (!async_event.block.getWorld().generateTree(location, TreeType.BIG_TREE, delegate)) {
													async_event.block.setTypeIdAndData(6, async_event.getFutureData().data, false);
												} else {
													Bukkit.getScheduler().runTaskAsynchronously(AsyncBlockEvents.this, new Runnable() {
														@Override
														public void run() {
															if (!delegate.flush()) {
																Bukkit.getScheduler().runTask(AsyncBlockEvents.this, new Runnable() {
																	@Override
																	public void run() {
																		async_event.block.setTypeIdAndData(6, async_event.getFutureData().data, false);
																		if (async_event.removedType != null) {
																			if (async_event.player.getItemInHand().getAmount() == 0) {
																				async_event.player.setItemInHand(new ItemStack(
																						async_event.removedType, 1, async_event.removedData
																				));
																			} else if (
																					async_event.player.getItemInHand().getType() == async_event.removedType
																					&& async_event.player.getItemInHand().getDurability()
																					   == async_event.removedData
																					&& async_event.player.getItemInHand().getAmount()
																					   != async_event.removedType.getMaxStackSize()
																					) {
																				async_event.player.getItemInHand().setAmount(
																						async_event.player.getItemInHand().getAmount() + 1
																				);
																			} else {
																				async_event.player.getInventory().addItem(
																						new ItemStack(async_event.removedType, 1, async_event.removedData)
																				);
																			}
																		}
																	}
																});
															}
														}
													});
												}
											} else {
												location = async_event.block.getLocation();
												delegate = new AsyncBlockChangeDelegate(location, async_event.player, TreeType.TREE);
												if (!async_event.block.getWorld().generateTree(location, TreeType.TREE, delegate)) {
													async_event.block.setTypeIdAndData(6, async_event.getFutureData().data, false);
												} else {
													Bukkit.getScheduler().runTaskAsynchronously(AsyncBlockEvents.this, new Runnable() {
														@Override
														public void run() {
															if (!delegate.flush()) {
																Bukkit.getScheduler().runTask(AsyncBlockEvents.this, new Runnable() {
																	@Override
																	public void run() {
																		async_event.block.setTypeIdAndData(6, async_event.getFutureData().data, false);
																		if (async_event.removedType != null) {
																			if (async_event.player.getItemInHand().getAmount() == 0) {
																				async_event.player.setItemInHand(new ItemStack(
																						async_event.removedType, 1, async_event.removedData
																				));
																			} else if (
																					async_event.player.getItemInHand().getType() == async_event.removedType
																					&& async_event.player.getItemInHand().getDurability()
																					   == async_event.removedData
																					&& async_event.player.getItemInHand().getAmount()
																					   != async_event.removedType.getMaxStackSize()
																					) {
																				async_event.player.getItemInHand().setAmount(
																						async_event.player.getItemInHand().getAmount() + 1
																				);
																			} else {
																				async_event.player.getInventory().addItem(
																						new ItemStack(async_event.removedType, 1, async_event.removedData)
																				);
																			}
																		}
																	}
																});
															}
														}
													});
												}
											}
											break;
										case 1:
										case 9:
											location = async_event.block.getLocation();
											delegate = new AsyncBlockChangeDelegate(location, async_event.player, TreeType.REDWOOD);
											if (!async_event.block.getWorld().generateTree(location, TreeType.REDWOOD, delegate)) {
												async_event.block.setTypeIdAndData(6, async_event.getFutureData().data, false);
											} else {
												Bukkit.getScheduler().runTaskAsynchronously(AsyncBlockEvents.this, new Runnable() {
													@Override
													public void run() {
														if (!delegate.flush()) {
															Bukkit.getScheduler().runTask(AsyncBlockEvents.this, new Runnable() {
																@Override
																public void run() {
																	async_event.block.setTypeIdAndData(6, async_event.getFutureData().data, false);
																	if (async_event.removedType != null) {
																		if (async_event.player.getItemInHand().getAmount() == 0) {
																			async_event.player.setItemInHand(new ItemStack(
																					async_event.removedType, 1, async_event.removedData
																			));
																		} else if (
																				async_event.player.getItemInHand().getType() == async_event.removedType
																				&& async_event.player.getItemInHand().getDurability()
																				   == async_event.removedData
																				&& async_event.player.getItemInHand().getAmount()
																				   != async_event.removedType.getMaxStackSize()
																				) {
																			async_event.player.getItemInHand().setAmount(
																					async_event.player.getItemInHand().getAmount() + 1
																			);
																		} else {
																			async_event.player.getInventory().addItem(
																					new ItemStack(async_event.removedType, 1, async_event.removedData)
																			);
																		}
																	}
																}
															});
														}
													}
												});
											}
											break;
										case 2:
										case 10:
											location = async_event.block.getLocation();
											delegate = new AsyncBlockChangeDelegate(location, async_event.player, TreeType.BIRCH);
											if (!async_event.block.getWorld().generateTree(location, TreeType.BIRCH, delegate)) {
												async_event.block.setTypeIdAndData(6, async_event.getFutureData().data, false);
											} else {
												Bukkit.getScheduler().runTaskAsynchronously(AsyncBlockEvents.this, new Runnable() {
													@Override
													public void run() {
														if (!delegate.flush()) {
															Bukkit.getScheduler().runTask(AsyncBlockEvents.this, new Runnable() {
																@Override
																public void run() {
																	async_event.block.setTypeIdAndData(6, async_event.getFutureData().data, false);
																	if (async_event.removedType != null) {
																		if (async_event.player.getItemInHand().getAmount() == 0) {
																			async_event.player.setItemInHand(new ItemStack(
																					async_event.removedType, 1, async_event.removedData
																			));
																		} else if (
																				async_event.player.getItemInHand().getType() == async_event.removedType
																				&& async_event.player.getItemInHand().getDurability()
																				   == async_event.removedData
																				&& async_event.player.getItemInHand().getAmount()
																				   != async_event.removedType.getMaxStackSize()
																				) {
																			async_event.player.getItemInHand().setAmount(
																					async_event.player.getItemInHand().getAmount() + 1
																			);
																		} else {
																			async_event.player.getInventory().addItem(
																					new ItemStack(async_event.removedType, 1, async_event.removedData)
																			);
																		}
																	}
																}
															});
														}
													}
												});
											}
											break;
										case 3:
										case 11:
											if (
													async_event.block.getRelative(BlockFace.NORTH).getType() == Material.SAPLING
													&& (7 & async_event.block.getRelative(BlockFace.NORTH).getData()) == 3
													&& async_event.block.getRelative(BlockFace.NORTH_EAST).getType() == Material.SAPLING
													&& (7 & async_event.block.getRelative(BlockFace.NORTH_EAST).getData()) == 3
													&& async_event.block.getRelative(BlockFace.EAST).getType() == Material.SAPLING
													&& (7 & async_event.block.getRelative(BlockFace.EAST).getData()) == 3
											   ) {
												final byte b1 = async_event.block.getRelative(BlockFace.NORTH).getData();
												final byte b2 = async_event.block.getRelative(BlockFace.NORTH_EAST).getData();
												final byte b3 = async_event.block.getRelative(BlockFace.EAST).getData();
												async_event.block.getRelative(BlockFace.NORTH).setTypeId(0, false);
												async_event.block.getRelative(BlockFace.NORTH_EAST).setTypeId(0, false);
												async_event.block.getRelative(BlockFace.EAST).setTypeId(0, false);

												location = async_event.block.getLocation().add(0.5, 0, -0.5);
												delegate = new AsyncBlockChangeDelegate(location, async_event.player, TreeType.JUNGLE);
												if (!async_event.block.getWorld().generateTree(location, TreeType.JUNGLE, delegate)) {
													async_event.block.setTypeIdAndData(6, async_event.getFutureData().data, false);
													async_event.block.getRelative(BlockFace.NORTH).setTypeIdAndData(6, b1, false);
													async_event.block.getRelative(BlockFace.NORTH_EAST).setTypeIdAndData(6, b2, false);
													async_event.block.getRelative(BlockFace.EAST).setTypeIdAndData(6, b3, false);
												} else {
													Bukkit.getScheduler().runTaskAsynchronously(AsyncBlockEvents.this, new Runnable() {
														@Override
														public void run() {
															if (!delegate.flush()) {
																Bukkit.getScheduler().runTask(AsyncBlockEvents.this, new Runnable() {
																	@Override
																	public void run() {
																		async_event.block.setTypeIdAndData(6, async_event.getFutureData().data, false);
																		async_event.block.getRelative(BlockFace.NORTH).setTypeIdAndData(6, b1, false);
																		async_event.block.getRelative(BlockFace.NORTH_EAST).setTypeIdAndData(6, b2, false);
																		async_event.block.getRelative(BlockFace.EAST).setTypeIdAndData(6, b3, false);
																		if (async_event.removedType != null) {
																			if (async_event.player.getItemInHand().getAmount() == 0) {
																				async_event.player.setItemInHand(new ItemStack(
																						async_event.removedType, 1, async_event.removedData
																				));
																			} else if (
																					async_event.player.getItemInHand().getType() == async_event.removedType
																					&& async_event.player.getItemInHand().getDurability()
																					   == async_event.removedData
																					&& async_event.player.getItemInHand().getAmount()
																					   != async_event.removedType.getMaxStackSize()
																					) {
																				async_event.player.getItemInHand().setAmount(
																						async_event.player.getItemInHand().getAmount() + 1
																				);
																			} else {
																				async_event.player.getInventory().addItem(
																						new ItemStack(async_event.removedType, 1, async_event.removedData)
																				);
																			}
																		}
																	}
																});
															}
														}
													});
												}
											} else if (
													async_event.block.getRelative(BlockFace.EAST).getType() == Material.SAPLING
													&& (7 & async_event.block.getRelative(BlockFace.EAST).getData()) == 3
													&& async_event.block.getRelative(BlockFace.SOUTH_EAST).getType() == Material.SAPLING
													&& (7 & async_event.block.getRelative(BlockFace.SOUTH_EAST).getData()) == 3
													&& async_event.block.getRelative(BlockFace.SOUTH).getType() == Material.SAPLING
													&& (7 & async_event.block.getRelative(BlockFace.SOUTH).getData()) == 3
													) {
												final byte b1 = async_event.block.getRelative(BlockFace.EAST).getData();
												final byte b2 = async_event.block.getRelative(BlockFace.SOUTH_EAST).getData();
												final byte b3 = async_event.block.getRelative(BlockFace.SOUTH).getData();
												async_event.block.getRelative(BlockFace.EAST).setTypeId(0, false);
												async_event.block.getRelative(BlockFace.SOUTH_EAST).setTypeId(0, false);
												async_event.block.getRelative(BlockFace.SOUTH).setTypeId(0, false);

												location = async_event.block.getLocation().add(0.5, 0, 0.5);
												delegate = new AsyncBlockChangeDelegate(location, async_event.player, TreeType.JUNGLE);
												if (!async_event.block.getWorld().generateTree(location, TreeType.JUNGLE, delegate)) {
													async_event.block.setTypeIdAndData(6, async_event.getFutureData().data, false);
													async_event.block.getRelative(BlockFace.EAST).setTypeIdAndData(6, b1, false);
													async_event.block.getRelative(BlockFace.SOUTH_EAST).setTypeIdAndData(6, b2, false);
													async_event.block.getRelative(BlockFace.SOUTH).setTypeIdAndData(6, b3, false);
												} else {
													Bukkit.getScheduler().runTaskAsynchronously(AsyncBlockEvents.this, new Runnable() {
														@Override
														public void run() {
															if (!delegate.flush()) {
																Bukkit.getScheduler().runTask(AsyncBlockEvents.this, new Runnable() {
																	@Override
																	public void run() {
																		async_event.block.setTypeIdAndData(6, async_event.getFutureData().data, false);
																		async_event.block.getRelative(BlockFace.EAST).setTypeIdAndData(6, b1, false);
																		async_event.block.getRelative(BlockFace.SOUTH_EAST).setTypeIdAndData(6, b2, false);
																		async_event.block.getRelative(BlockFace.SOUTH).setTypeIdAndData(6, b3, false);
																		if (async_event.removedType != null) {
																			if (async_event.player.getItemInHand().getAmount() == 0) {
																				async_event.player.setItemInHand(new ItemStack(
																						async_event.removedType, 1, async_event.removedData
																				));
																			} else if (
																					async_event.player.getItemInHand().getType() == async_event.removedType
																					&& async_event.player.getItemInHand().getDurability()
																					   == async_event.removedData
																					&& async_event.player.getItemInHand().getAmount()
																					   != async_event.removedType.getMaxStackSize()
																					) {
																				async_event.player.getItemInHand().setAmount(
																						async_event.player.getItemInHand().getAmount() + 1
																				);
																			} else {
																				async_event.player.getInventory().addItem(
																						new ItemStack(async_event.removedType, 1, async_event.removedData)
																				);
																			}
																		}
																	}
																});
															}
														}
													});
												}
											} else if (
													async_event.block.getRelative(BlockFace.SOUTH).getType() == Material.SAPLING
													&& (7 & async_event.block.getRelative(BlockFace.SOUTH).getData()) == 3
													&& async_event.block.getRelative(BlockFace.SOUTH_WEST).getType() == Material.SAPLING
													&& (7 & async_event.block.getRelative(BlockFace.SOUTH_WEST).getData()) == 3
													&& async_event.block.getRelative(BlockFace.WEST).getType() == Material.SAPLING
													&& (7 & async_event.block.getRelative(BlockFace.WEST).getData()) == 3
													) {
												final byte b1 = async_event.block.getRelative(BlockFace.SOUTH).getData();
												final byte b2 = async_event.block.getRelative(BlockFace.SOUTH_WEST).getData();
												final byte b3 = async_event.block.getRelative(BlockFace.WEST).getData();
												async_event.block.getRelative(BlockFace.SOUTH).setTypeId(0, false);
												async_event.block.getRelative(BlockFace.SOUTH_WEST).setTypeId(0, false);
												async_event.block.getRelative(BlockFace.WEST).setTypeId(0, false);

												location = async_event.block.getLocation().add(-0.5, 0, 0.5);
												delegate = new AsyncBlockChangeDelegate(location, async_event.player, TreeType.JUNGLE);
												if (!async_event.block.getWorld().generateTree(location, TreeType.JUNGLE, delegate)) {
													async_event.block.setTypeIdAndData(6, async_event.getFutureData().data, false);
													async_event.block.getRelative(BlockFace.SOUTH).setTypeIdAndData(6, b1, false);
													async_event.block.getRelative(BlockFace.SOUTH_WEST).setTypeIdAndData(6, b2, false);
													async_event.block.getRelative(BlockFace.WEST).setTypeIdAndData(6, b3, false);
												} else {
													Bukkit.getScheduler().runTaskAsynchronously(AsyncBlockEvents.this, new Runnable() {
														@Override
														public void run() {
															if (!delegate.flush()) {
																Bukkit.getScheduler().runTask(AsyncBlockEvents.this, new Runnable() {
																	@Override
																	public void run() {
																		async_event.block.setTypeIdAndData(6, async_event.getFutureData().data, false);
																		async_event.block.getRelative(BlockFace.SOUTH).setTypeIdAndData(6, b1, false);
																		async_event.block.getRelative(BlockFace.SOUTH_WEST).setTypeIdAndData(6, b2, false);
																		async_event.block.getRelative(BlockFace.WEST).setTypeIdAndData(6, b3, false);
																		if (async_event.removedType != null) {
																			if (async_event.player.getItemInHand().getAmount() == 0) {
																				async_event.player.setItemInHand(new ItemStack(
																						async_event.removedType, 1, async_event.removedData
																				));
																			} else if (
																					async_event.player.getItemInHand().getType() == async_event.removedType
																					&& async_event.player.getItemInHand().getDurability()
																					   == async_event.removedData
																					&& async_event.player.getItemInHand().getAmount()
																					   != async_event.removedType.getMaxStackSize()
																					) {
																				async_event.player.getItemInHand().setAmount(
																						async_event.player.getItemInHand().getAmount() + 1
																				);
																			} else {
																				async_event.player.getInventory().addItem(
																						new ItemStack(async_event.removedType, 1, async_event.removedData)
																				);
																			}
																		}
																	}
																});
															}
														}
													});
												}
											} else if (
													async_event.block.getRelative(BlockFace.WEST).getType() == Material.SAPLING
													&& (7 & async_event.block.getRelative(BlockFace.WEST).getData()) == 3
													&& async_event.block.getRelative(BlockFace.NORTH_WEST).getType() == Material.SAPLING
													&& (7 & async_event.block.getRelative(BlockFace.NORTH_WEST).getData()) == 3
													&& async_event.block.getRelative(BlockFace.NORTH).getType() == Material.SAPLING
													&& (7 & async_event.block.getRelative(BlockFace.NORTH).getData()) == 3
													) {
												final byte b1 = async_event.block.getRelative(BlockFace.WEST).getData();
												final byte b2 = async_event.block.getRelative(BlockFace.NORTH_WEST).getData();
												final byte b3 = async_event.block.getRelative(BlockFace.NORTH).getData();
												async_event.block.getRelative(BlockFace.WEST).setTypeId(0, false);
												async_event.block.getRelative(BlockFace.NORTH_WEST).setTypeId(0, false);
												async_event.block.getRelative(BlockFace.NORTH).setTypeId(0, false);

												location = async_event.block.getLocation().add(-0.5, 0, -0.5);
												delegate = new AsyncBlockChangeDelegate(location, async_event.player, TreeType.JUNGLE);
												if (!async_event.block.getWorld().generateTree(location, TreeType.JUNGLE, delegate)) {
													async_event.block.setTypeIdAndData(6, async_event.getFutureData().data, false);
													async_event.block.getRelative(BlockFace.WEST).setTypeIdAndData(6, b1, false);
													async_event.block.getRelative(BlockFace.NORTH_WEST).setTypeIdAndData(6, b2, false);
													async_event.block.getRelative(BlockFace.NORTH).setTypeIdAndData(6, b3, false);
												} else {
													Bukkit.getScheduler().runTaskAsynchronously(AsyncBlockEvents.this, new Runnable() {
														@Override
														public void run() {
															if (!delegate.flush()) {
																Bukkit.getScheduler().runTask(AsyncBlockEvents.this, new Runnable() {
																	@Override
																	public void run() {
																		async_event.block.setTypeIdAndData(6, async_event.getFutureData().data, false);
																		async_event.block.getRelative(BlockFace.WEST).setTypeIdAndData(6, b1, false);
																		async_event.block.getRelative(BlockFace.NORTH_WEST).setTypeIdAndData(6, b2, false);
																		async_event.block.getRelative(BlockFace.NORTH).setTypeIdAndData(6, b3, false);
																		if (async_event.removedType != null) {
																			if (async_event.player.getItemInHand().getAmount() == 0) {
																				async_event.player.setItemInHand(new ItemStack(
																						async_event.removedType, 1, async_event.removedData
																				));
																			} else if (
																					async_event.player.getItemInHand().getType() == async_event.removedType
																					&& async_event.player.getItemInHand().getDurability()
																					   == async_event.removedData
																					&& async_event.player.getItemInHand().getAmount()
																					   != async_event.removedType.getMaxStackSize()
																					) {
																				async_event.player.getItemInHand().setAmount(
																						async_event.player.getItemInHand().getAmount() + 1
																				);
																			} else {
																				async_event.player.getInventory().addItem(
																						new ItemStack(async_event.removedType, 1, async_event.removedData)
																				);
																			}
																		}
																	}
																});
															}
														}
													});
												}
											} else {
												location = async_event.block.getLocation();
												delegate = new AsyncBlockChangeDelegate(location, async_event.player, TreeType.SMALL_JUNGLE);
												if (!async_event.block.getWorld().generateTree(location, TreeType.SMALL_JUNGLE, delegate)) {
													async_event.block.setTypeIdAndData(6, async_event.getFutureData().data, false);
												} else {
													Bukkit.getScheduler().runTaskAsynchronously(AsyncBlockEvents.this, new Runnable() {
														@Override
														public void run() {
															if (!delegate.flush()) {
																Bukkit.getScheduler().runTask(AsyncBlockEvents.this, new Runnable() {
																	@Override
																	public void run() {
																		async_event.block.setTypeIdAndData(6, async_event.getFutureData().data, false);
																		if (async_event.removedType != null) {
																			if (async_event.player.getItemInHand().getAmount() == 0) {
																				async_event.player.setItemInHand(new ItemStack(
																						async_event.removedType, 1, async_event.removedData
																				));
																			} else if (
																					async_event.player.getItemInHand().getType() == async_event.removedType
																					&& async_event.player.getItemInHand().getDurability()
																					   == async_event.removedData
																					&& async_event.player.getItemInHand().getAmount()
																					   != async_event.removedType.getMaxStackSize()
																					) {
																				async_event.player.getItemInHand().setAmount(
																						async_event.player.getItemInHand().getAmount() + 1
																				);
																			} else {
																				async_event.player.getInventory().addItem(
																						new ItemStack(async_event.removedType, 1, async_event.removedData)
																				);
																			}
																		}
																	}
																});
															}
														}
													});
												}
											}
											break;
									}
									break;
								case BROWN_MUSHROOM:
									packet = new Packet61WorldEvent(
											2005, async_event.block.getX(), async_event.block.getY(), async_event.block.getZ(), 0, false
									);
									for (final Player wplayer : async_event.block.getWorld().getPlayers()) {
										if (wplayer.getLocation().distanceSquared(middle) > 64 * 64)
											continue;
										((CraftPlayer) wplayer).getHandle().playerConnection.sendPacket(packet);
									}
									async_event.block.setTypeIdAndData(39, async_event.getFutureData().data, false);
									if (random.nextFloat() < 0.4F)
										break;
									async_event.block.setTypeId(0, false);
									location = async_event.block.getLocation();
									delegate = new AsyncBlockChangeDelegate(location, async_event.player, TreeType.BROWN_MUSHROOM);
									if (!async_event.block.getWorld().generateTree(location, TreeType.BROWN_MUSHROOM, delegate)) {
										async_event.block.setTypeIdAndData(39, async_event.getFutureData().data, false);
									} else {
										Bukkit.getScheduler().runTaskAsynchronously(
												AsyncBlockEvents.this, new Runnable() {
											@Override
											public void run() {
												if (!delegate.flush()) {
													Bukkit.getScheduler().runTask(
															AsyncBlockEvents.this, new Runnable() {
														@Override
														public void run() {
															async_event.block.setTypeIdAndData(39, async_event.getFutureData().data, false);
															if (async_event.removedType != null) {
																if (async_event.player.getItemInHand().getAmount() == 0) {
																	async_event.player.setItemInHand(
																			new ItemStack(
																					async_event.removedType, 1, async_event.removedData
																			)
																	);
																} else if (
																		async_event.player.getItemInHand().getType() == async_event.removedType
																		&& async_event.player.getItemInHand().getDurability()
																		   == async_event.removedData
																		&& async_event.player.getItemInHand().getAmount()
																		   != async_event.removedType.getMaxStackSize()
																		) {
																	async_event.player.getItemInHand().setAmount(
																			async_event.player.getItemInHand().getAmount() + 1
																	);
																} else {
																	async_event.player.getInventory().addItem(
																			new ItemStack(async_event.removedType, 1, async_event.removedData)
																	);
																}
															}
														}
													}
													);
												}
											}
										}
										);
									}
									break;
								case RED_MUSHROOM:
									packet = new Packet61WorldEvent(
											2005, async_event.block.getX(), async_event.block.getY(), async_event.block.getZ(), 0, false
									);
									for (final Player wplayer : async_event.block.getWorld().getPlayers()) {
										if (wplayer.getLocation().distanceSquared(middle) > 64 * 64)
											continue;
										((CraftPlayer) wplayer).getHandle().playerConnection.sendPacket(packet);
									}
									async_event.block.setTypeIdAndData(40, async_event.getFutureData().data, false);
									if (random.nextFloat() < 0.4F)
										break;
									async_event.block.setTypeId(0, false);
									location = async_event.block.getLocation();
									delegate = new AsyncBlockChangeDelegate(location, async_event.player, TreeType.RED_MUSHROOM);
									if (!async_event.block.getWorld().generateTree(location, TreeType.RED_MUSHROOM, delegate)) {
										async_event.block.setTypeIdAndData(40, async_event.getFutureData().data, false);
									} else {
										Bukkit.getScheduler().runTaskAsynchronously(
												AsyncBlockEvents.this, new Runnable() {
											@Override
											public void run() {
												if (!delegate.flush()) {
													Bukkit.getScheduler().runTask(
															AsyncBlockEvents.this, new Runnable() {
														@Override
														public void run() {
															async_event.block.setTypeIdAndData(40, async_event.getFutureData().data, false);
															if (async_event.removedType != null) {
																if (async_event.player.getItemInHand().getAmount() == 0) {
																	async_event.player.setItemInHand(
																			new ItemStack(
																					async_event.removedType, 1, async_event.removedData
																			)
																	);
																} else if (
																		async_event.player.getItemInHand().getType() == async_event.removedType
																		&& async_event.player.getItemInHand().getDurability()
																		   == async_event.removedData
																		&& async_event.player.getItemInHand().getAmount()
																		   != async_event.removedType.getMaxStackSize()
																		) {
																	async_event.player.getItemInHand().setAmount(
																			async_event.player.getItemInHand().getAmount() + 1
																	);
																} else {
																	async_event.player.getInventory().addItem(
																			new ItemStack(async_event.removedType, 1, async_event.removedData)
																	);
																}
															}
														}
													}
													);
												}
											}
										}
										);
									}
									break;
								default:
									async_event.getFutureData().apply(async_event.block);
									update(
											async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
											async_event.block.getZ(), async_event.getFutureData().id
									);
									break;
							}
						} else {
							// event cancelled, send correct block, fix inventory
							// ass tracking
							if (async_event.block.getType() == Material.STONE_PLATE
									|| async_event.block.getType() == Material.WOOD_PLATE
									|| async_event.block.getType() == Material.TRIPWIRE) {
								assManager.add(set_loc, ((CraftPlayer) async_event.player).getHandle(), false);
							}
							if (async_event.block.getType() == Material.WOODEN_DOOR && final_door_loc != null) {
								final BlockLocation door_bottom = new BlockLocation(
										async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
										async_event.block.getZ()
								);
								final BlockLocation door_up = new BlockLocation(
										door_bottom.world, door_bottom.x, door_bottom.y + 1, door_bottom.z
								);
								processor.sendBlock(door_bottom, 64, async_event.block.getData());
								processor.sendBlock(door_up, 64, async_event.block.getRelative(BlockFace.UP).getData());
							} else {
								processor.sendBlock(set_loc, async_event.block.getTypeId(), async_event.block.getData());
							}
							if (async_event.removedType != null) {
								if (async_event.player.getItemInHand().getAmount() == 0) {
									async_event.player.setItemInHand(new ItemStack(async_event.removedType, 1, async_event.removedData));
								} else if (
										async_event.player.getItemInHand().getType() == async_event.removedType
										&& async_event.player.getItemInHand().getDurability() == async_event.removedData
										&& async_event.player.getItemInHand().getAmount() != async_event.removedType.getMaxStackSize()
										) {
									async_event.player.getItemInHand().setAmount(async_event.player.getItemInHand().getAmount() + 1);
								} else {
									async_event.player.getInventory().addItem(
											new ItemStack(async_event.removedType, 1, async_event.removedData)
									);
								}
							}
						}
						processor.remove(set_loc);
						if (final_door_loc != null)
							processor.remove(final_door_loc);
					}
				}
				);
			}
		}
		);
		return true;
	}

	public boolean onBucketEmpty(final PlayerBucketEmptyEvent event) {
		// used all over
		final Block block = event.getBlockClicked().getRelative(event.getBlockFace());
		final Location loc = block.getLocation();
		final BlockLocation set_loc = new BlockLocation(
				loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()
		);

		final boolean cancelled = event.isCancelled();
		event.setCancelled(true);

		// check for contention
		if (processor.processing.containsKey(set_loc)) {
			if (processor.processing.get(set_loc) != AsyncEventType.BUCKET_EMPTY)
				event.getPlayer().sendMessage(ChatColor.GRAY + "[Async] Sorry, this block has a cooldown in effect");
			return false;
		}

		final PrefireBucketEmptyEvent prefire_event;

		switch (event.getPlayer().getItemInHand().getType()) {
			case WATER_BUCKET:
				prefire_event = new PrefireBucketEmptyEvent(
						block, block.getState(), event.getPlayer(), event.getPlayer().getItemInHand().clone(),
						new BlockMeta(8, (byte) 0), EMPTY_STACK, 0, cancelled,
						event.getPlayer().getGameMode() == GameMode.CREATIVE ? null : Material.WATER_BUCKET, (short) 0
				);
				break;
			case LAVA_BUCKET:
				prefire_event = new PrefireBucketEmptyEvent(
						block, block.getState(), event.getPlayer(), event.getPlayer().getItemInHand().clone(),
						new BlockMeta(10, (byte) 0), EMPTY_STACK, 0, cancelled,
						event.getPlayer().getGameMode() == GameMode.CREATIVE ? null : Material.LAVA_BUCKET, (short) 0
				);
				break;
			default:
				return false;
		}

		plManager.callEvent(prefire_event);

		final AsyncBucketEmptyEvent async_event = new AsyncBucketEmptyEvent(
				prefire_event.block, prefire_event.oldState, prefire_event.player, prefire_event.getHand(),
				prefire_event.getFutureData(), prefire_event.getDrops(), prefire_event.exp, prefire_event.isCancelled(),
				prefire_event.removedType, prefire_event.removedData
		);

		// simulate block usage to prevent abuse of async hand finding
		if (async_event.removedType != null) {
			if (async_event.player.getItemInHand().getAmount() == 1) {
				async_event.player.setItemInHand(null);
			} else {
				async_event.player.getItemInHand().setAmount(event.getPlayer().getItemInHand().getAmount() - 1);
			}
		}

		processor.add(
				set_loc, AsyncEventType.BUCKET_EMPTY, async_event.getFutureData().id, async_event.getFutureData().data
		);
		Bukkit.getScheduler().runTaskAsynchronously(
				this, new Runnable() {
			@Override
			public void run() {
				plManager.callEvent(async_event);
				Bukkit.getScheduler().runTask(
						AsyncBlockEvents.this, new Runnable() {
					@Override
					public void run() {
						// modify base event to account for async changes
						event.setCancelled(async_event.isCancelled());
						plManager.callEvent(event, EventPriority.MONITOR);
						processor.unlock(set_loc);
						if (!event.isCancelled()) {
							final Location middle = new Location(
									async_event.block.getWorld(), async_event.block.getX() + 0.5, async_event.block.getY() + 0.5,
									async_event.block.getZ() + 0.5
							);

							// item drop
							for (final ItemStack stack : async_event.getDrops()) {
								async_event.block.getWorld().dropItem(middle, stack);
							}

							// xp drop
							if (async_event.exp > 0) {
								int tempexp = async_event.exp;
								int orb_val;
								while (tempexp > 0) {
									orb_val = EntityExperienceOrb.getOrbValue(tempexp);
									tempexp -= orb_val;
									middle.getWorld().spawn(middle, ExperienceOrb.class).setExperience(orb_val);
								}
							} else if (async_event.exp < 0) {
								// use xp utils to decrement
								new ExperienceManager(async_event.player).changeExp(async_event.exp);
							}

							// actually setting the block
							async_event.getFutureData().apply(async_event.block);
							update(
									async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
									async_event.block.getZ(), async_event.getFutureData().id
							);

							// gimme mah bukkit! *if a bucket was removed in the first place
							if (async_event.removedType != null) {
								if (async_event.player.getItemInHand().getAmount() == 0) {
									async_event.player.setItemInHand(new ItemStack(Material.BUCKET));
								} else if (
										async_event.player.getItemInHand().getType() == Material.BUCKET
										&& async_event.player.getItemInHand().getAmount() != Material.BUCKET.getMaxStackSize()
										) {
									async_event.player.getItemInHand().setAmount(async_event.player.getItemInHand().getAmount() + 1);
								} else {
									async_event.player.getInventory().addItem(new ItemStack(Material.BUCKET));
								}
							}
						} else {
							// event cancelled, send correct block, fix inventory
							processor.sendBlock(set_loc, async_event.block.getTypeId(), async_event.block.getData());
							if (async_event.removedType != null) {
								if (async_event.player.getItemInHand().getAmount() == 0) {
									async_event.player.setItemInHand(new ItemStack(async_event.removedType, 1, async_event.removedData));
								} else if (
										async_event.player.getItemInHand().getType() == async_event.removedType
										&& async_event.player.getItemInHand().getDurability() == async_event.removedData
										&& async_event.player.getItemInHand().getAmount() != async_event.removedType.getMaxStackSize()
										) {
									async_event.player.getItemInHand().setAmount(async_event.player.getItemInHand().getAmount() + 1);
								} else {
									async_event.player.getInventory().addItem(
											new ItemStack(async_event.removedType, 1, async_event.removedData)
									);
								}
							}
						}
						processor.remove(set_loc);
					}
				}
				);
			}
		}
		);
		return true;
	}

	public boolean onBucketFill(final PlayerBucketFillEvent event) {
		// used all over
		final Block block = event.getBlockClicked().getRelative(event.getBlockFace());
		final Location loc = block.getLocation();
		final BlockLocation set_loc = new BlockLocation(
				loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()
		);

		final boolean cancelled = event.isCancelled();
		event.setCancelled(true);

		// check for contention
		if (processor.processing.containsKey(set_loc)) {
			if (processor.processing.get(set_loc) != AsyncEventType.BUCKET_FILL)
				event.getPlayer().sendMessage(ChatColor.GRAY + "[Async] Sorry, this block has a cooldown in effect");
			return false;
		}

		final PrefireBucketFillEvent prefire_event = new PrefireBucketFillEvent(
				block, block.getState(), event.getPlayer(), event.getPlayer().getItemInHand().clone(),
				new BlockMeta(0, (byte) 0), EMPTY_STACK, 0, cancelled,
				event.getPlayer().getGameMode() == GameMode.CREATIVE ? null : Material.BUCKET, (short) 0
		);

		plManager.callEvent(prefire_event);

		final AsyncBucketFillEvent async_event = new AsyncBucketFillEvent(
				prefire_event.block, prefire_event.oldState, prefire_event.player, prefire_event.getHand(),
				prefire_event.getFutureData(), prefire_event.getDrops(), prefire_event.exp, prefire_event.isCancelled(),
				prefire_event.removedType, prefire_event.removedData
		);

		// simulate block usage to prevent abuse of async hand finding
		if (async_event.removedType != null) {
			if (async_event.player.getItemInHand().getAmount() == 1) {
				async_event.player.setItemInHand(null);
			} else {
				async_event.player.getItemInHand().setAmount(event.getPlayer().getItemInHand().getAmount() - 1);
			}
		}

		processor.add(
				set_loc, AsyncEventType.BUCKET_FILL, async_event.getFutureData().id, async_event.getFutureData().data
		);
		Bukkit.getScheduler().runTaskAsynchronously(
				this, new Runnable() {
			@Override
			public void run() {
				plManager.callEvent(async_event);
				Bukkit.getScheduler().runTask(
						AsyncBlockEvents.this, new Runnable() {
					@Override
					public void run() {
						// modify base event to account for async changes
						event.setCancelled(async_event.isCancelled());
						plManager.callEvent(event, EventPriority.MONITOR);
						processor.unlock(set_loc);
						if (!event.isCancelled()) {
							final Location middle = new Location(
									async_event.block.getWorld(), async_event.block.getX() + 0.5, async_event.block.getY() + 0.5,
									async_event.block.getZ() + 0.5
							);

							// item drop
							for (final ItemStack stack : async_event.getDrops()) {
								async_event.block.getWorld().dropItem(middle, stack);
							}

							// xp drop
							if (async_event.exp > 0) {
								int tempexp = async_event.exp;
								int orb_val;
								while (tempexp > 0) {
									orb_val = EntityExperienceOrb.getOrbValue(tempexp);
									tempexp -= orb_val;
									middle.getWorld().spawn(middle, ExperienceOrb.class).setExperience(orb_val);
								}
							} else if (async_event.exp < 0) {
								// use xp utils to decrement
								new ExperienceManager(async_event.player).changeExp(async_event.exp);
							}

							// actually setting the block
							async_event.getFutureData().apply(async_event.block);
							update(
									async_event.block.getWorld(), async_event.block.getX(), async_event.block.getY(),
									async_event.block.getZ(), async_event.getFutureData().id
							);

							// give player filled bucket *if a bucket was removed in the first place
							if (async_event.removedType != null) {
								final Material filledbucket;
								if (async_event.oldState.getType() == Material.WATER
								    || async_event.oldState.getType() == Material.STATIONARY_WATER) {
									filledbucket = Material.WATER_BUCKET;
								} else if (async_event.oldState.getType() == Material.LAVA
								           || async_event.oldState.getType() == Material.STATIONARY_LAVA) {
									filledbucket = Material.LAVA_BUCKET;
								} else {
									filledbucket = Material.BUCKET;
								}
								if (async_event.player.getItemInHand().getAmount() == 0) {
									async_event.player.setItemInHand(new ItemStack(filledbucket));
								} else if (
										async_event.player.getItemInHand().getType() == filledbucket
										&& async_event.player.getItemInHand().getAmount() != filledbucket.getMaxStackSize()
										) {
									async_event.player.getItemInHand().setAmount(async_event.player.getItemInHand().getAmount() + 1);
								} else {
									async_event.player.getInventory().addItem(new ItemStack(filledbucket));
								}
							}
						} else {
							// event cancelled, send correct block, fix inventory
							processor.sendBlock(set_loc, async_event.block.getTypeId(), async_event.block.getData());
							if (async_event.removedType != null) {
								if (async_event.player.getItemInHand().getAmount() == 0) {
									async_event.player.setItemInHand(new ItemStack(async_event.removedType, 1, async_event.removedData));
								} else if (
										async_event.player.getItemInHand().getType() == async_event.removedType
										&& async_event.player.getItemInHand().getDurability() == async_event.removedData
										&& async_event.player.getItemInHand().getAmount() != async_event.removedType.getMaxStackSize()
										) {
									async_event.player.getItemInHand().setAmount(async_event.player.getItemInHand().getAmount() + 1);
								} else {
									async_event.player.getInventory().addItem(
											new ItemStack(async_event.removedType, 1, async_event.removedData)
									);
								}
							}
						}
						processor.remove(set_loc);
					}
				}
				);
			}
		}
		);
		return true;
	}

	public static final InventoryHolder ABEVoidInventoryHolder = new InventoryHolder() {
		@Override
		public Inventory getInventory() {
			return ABEVoidInventory;
		}
	};

	public static final Inventory ABEVoidInventory = new Inventory() {
		@Override
		public int getSize() {
			return 0;
		}

		@Override
		public int getMaxStackSize() {
			return Integer.MAX_VALUE;
		}

		@Override
		public void setMaxStackSize(final int i) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getName() {
			return "AsyncBlockEvents null inventory";
		}

		@Override
		public ItemStack getItem(final int i) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setItem(final int i, final ItemStack itemStack) {
			throw new UnsupportedOperationException();
		}

		@Override
		public HashMap<Integer, ItemStack> addItem(final ItemStack... itemStacks) throws IllegalArgumentException {
			return new HashMap<>();
		}

		@Override
		public HashMap<Integer, ItemStack> removeItem(final ItemStack... itemStacks) throws IllegalArgumentException {
			final HashMap<Integer, ItemStack> toreturn = new HashMap<>();
			for (int i = 0; i < itemStacks.length; i++) {
				toreturn.put(i, itemStacks[i]);
			}
			return toreturn;
		}

		@Override
		public ItemStack[] getContents() {
			return new ItemStack[0];
		}

		@Override
		public void setContents(final ItemStack[] itemStacks) throws IllegalArgumentException {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean contains(final int i) {
			return false;
		}

		@Override
		public boolean contains(final Material material) throws IllegalArgumentException {
			return false;
		}

		@Override
		public boolean contains(final ItemStack itemStack) {
			return false;
		}

		@Override
		public boolean contains(final int i, final int i2) {
			return false;
		}

		@Override
		public boolean contains(final Material material, final int i) throws IllegalArgumentException {
			return false;
		}

		@Override
		public boolean contains(final ItemStack itemStack, final int i) {
			return false;
		}

		@Override
		public boolean containsAtLeast(final ItemStack itemStack, final int i) {
			return false;
		}

		@Override
		public HashMap<Integer, ? extends ItemStack> all(final int i) {
			return new HashMap<>();
		}

		@Override
		public HashMap<Integer, ? extends ItemStack> all(final Material material) throws IllegalArgumentException {
			return new HashMap<>();
		}

		@Override
		public HashMap<Integer, ? extends ItemStack> all(final ItemStack itemStack) {
			return new HashMap<>();
		}

		@Override
		public int first(final int i) {
			return -1;
		}

		@Override
		public int first(final Material material) throws IllegalArgumentException {
			return -1;
		}

		@Override
		public int first(final ItemStack itemStack) {
			return -1;
		}

		@Override
		public int firstEmpty() {
			return -1;
		}

		@Override
		public void remove(final int i) { /* DO NOTHING */ }

		@Override
		public void remove(final Material material) throws IllegalArgumentException { /* DO NOTHING */ }

		@Override
		public void remove(final ItemStack itemStack) { /* DO NOTHING */ }

		@Override
		public void clear(final int i) { /* DO NOTHING */ }

		@Override
		public void clear() { /* DO NOTHING */ }

		@Override
		public List<HumanEntity> getViewers() {
			return new ArrayList<>(0);
		}

		@Override
		public String getTitle() {
			return "AsyncBlockEvents null inventory";
		}

		@Override
		public InventoryType getType() {
			return InventoryType.CHEST;
		}

		@Override
		public InventoryHolder getHolder() {
			return ABEVoidInventoryHolder;
		}

		@Override
		public ListIterator<ItemStack> iterator() {
			return new ArrayList<ItemStack>(0).listIterator();
		}

		@Override
		public ListIterator<ItemStack> iterator(final int i) {
			return new ArrayList<ItemStack>(0).listIterator();
		}
	};

	public boolean onItemMove(final EnhancedInventoryMoveItemEvent event) {
		// Async event requires:

		// - have source
		// - have destination
		// - have initiator
		// - have original
		// PULL calculation
		// PUSH calculation
		// - have cancelled

		final int slot;
		//noinspection ObjectEquality
		if (event.getInitiator() == event.getDestination()) {
			slot = -1;
		} else {
			final InventoryHolder holder = event.getSource().getHolder();
			if (holder instanceof Hopper) {
				slot = Facing.OPPOSITE_FACING[BlockHopper.c(((Hopper) holder).getRawData())];
			} else if (holder instanceof Dropper) {
				slot = Facing.OPPOSITE_FACING[((Dropper) holder).getRawData() & 7];
			} else {
				System.out.println("Could not rationalize InventoryMoveItemEvent");
				return false;
			}
		}

		if (!(event.getDestination() instanceof CraftInventory)) {
			return false;
		}

		final boolean cancelled = event.isCancelled();
		event.setCancelled(true);

		Bukkit.getScheduler().runTask(
				this, new Runnable() {
			@Override
			public void run() {
				final ItemStack testResult
						= testAddItem(((CraftInventory) event.getDestination()).getInventory(), event.getItem(), slot);

				System.out.println("testresult -- " + testResult);

				final ItemStack push = event.getItem();
				push.setAmount(event.getItem().getAmount() - (testResult == null ? 0 : testResult.getAmount()));

				final ItemStack pull;
				if (!event.getItem().equals(event.original)) {
					pull = new ItemStack(event.original.getType(), 0);
				} else {
					pull = push.clone();
				}

				final PrefireInventoryMoveItemEvent prefire_event = new PrefireInventoryMoveItemEvent(
						event.getSource(), event.getDestination(), event.getInitiator(), event.original, pull, push, cancelled
				);

				plManager.callEvent(prefire_event);

				System.out.println("source inventory:");
				for (ItemStack item : prefire_event.source)
					if (item != null)
						System.out.println(item);

				// simulate action to prevent duping
				final Map<Integer, ItemStack> resid = prefire_event.source.removeItem(prefire_event.getPull());
				if (!resid.isEmpty()) {
					System.out.println("fuck " + resid);
					int sum = 0;
					for (final ItemStack item : resid.values())
						sum += item.getAmount();
					prefire_event.getPull().setAmount(prefire_event.getPull().getAmount() - sum);
				}

				final AsyncInventoryMoveItemEvent async_event = new AsyncInventoryMoveItemEvent(
						prefire_event.source, prefire_event.destination, prefire_event.initiator, prefire_event.getOriginal(),
						prefire_event.getPull(), prefire_event.getPush(), prefire_event.isCancelled()
				);

				Bukkit.getScheduler().runTaskAsynchronously(
						AsyncBlockEvents.reference, new Runnable() {
					@Override
					public void run() {
						plManager.callEvent(async_event);
						Bukkit.getScheduler().runTask(
								AsyncBlockEvents.this, new Runnable() {
							@Override
							public void run() {
								final boolean iscancelled;
								// reconstruct event(s) for MONITOR plugins
								if (prefire_event.getPull().equals(prefire_event.getPush())) {
									//						    && async_event.getPull().equals(event.getItem())) {
									// modify original event
									event.setCancelled(async_event.isCancelled());
									event.setItem(prefire_event.getPull());
									plManager.callEvent(event, EventPriority.MONITOR);
									iscancelled = async_event.isCancelled();
								} else {
									// construct two events
									// pull event
									final InventoryMoveItemEvent pull_event = new InventoryMoveItemEvent(
											async_event.source, prefire_event.getPull(), ABEVoidInventory, true
									);
									pull_event.setCancelled(async_event.isCancelled());
									plManager.callEvent(pull_event, EventPriority.MONITOR);
									// push event
									final InventoryMoveItemEvent push_event = new InventoryMoveItemEvent(
											ABEVoidInventory, prefire_event.getPush(), async_event.destination, true
									);
									push_event.setCancelled(async_event.isCancelled());
									plManager.callEvent(push_event, EventPriority.MONITOR);
									iscancelled = pull_event.isCancelled() || push_event.isCancelled();
								}

								if (!iscancelled) {
									System.out.println("Item moved -- " + prefire_event.getPull() + " -- " + prefire_event.getPush());
									TileEntityHopper.addItem(
											((CraftInventory) event.getDestination()).getInventory(),
											CraftItemStack.asNMSCopy(prefire_event.getPush()), slot
									);
								} else {
									System.out.println("Item move cancelled");
									async_event.source.addItem(prefire_event.getPull());
								}
							}
						}
						);
					}
				}
				);
			}
		}
		);
		return true;
	}

	public <T extends Minecart & InventoryHolder> boolean onEntityInteract(final PlayerInteractEntityEvent event) {
		final Entity entity = event.getRightClicked();

		if (!(entity instanceof Minecart) || !(entity instanceof InventoryHolder))
			return false;

		//noinspection unchecked
		final T invcart = (T) entity;

		final boolean cancelled = event.isCancelled();
		event.setCancelled(true);

		// Async event requires:

		// - have entity
		// - have player
		// - have cancelled

		final PrefirePlayerInteractEntityEvent prefire_event = new PrefirePlayerInteractEntityEvent(
				invcart, event.getPlayer(), cancelled
		);

		plManager.callEvent(prefire_event);

		final AsyncPlayerInteractEntityEvent async_event = new AsyncPlayerInteractEntityEvent(
				invcart, prefire_event.player, prefire_event.isCancelled()
		);

		Bukkit.getScheduler().runTaskAsynchronously(
				AsyncBlockEvents.this, new Runnable() {
			@Override
			public void run() {
				plManager.callEvent(async_event);
				Bukkit.getScheduler().runTask(
						AsyncBlockEvents.this, new Runnable() {
					@Override
					public void run() {
						// modify base event to account for async changes
						event.setCancelled(async_event.isCancelled());
						plManager.callEvent(event, EventPriority.MONITOR);
						if (!event.isCancelled()) {
							final InventoryView iv = async_event.player.openInventory(invcart.getInventory());
							final InventoryOpenEvent ioevent = new InventoryOpenEvent(iv);
							plManager.callEvent(ioevent);
							if (ioevent.isCancelled())
								iv.close();
						}
					}
				}
				);
			}
		}
		);
		return true;
	}

	public boolean onVehicleBreak(final VehicleDestroyEvent event) {
		final boolean cancelled = event.isCancelled();
		event.setCancelled(true);

		// Async event requires:

		// - have vehicle
		// - have destroyer
		// IS_PLAYER calculation
		// PLAYER calculation
		// HAND calculation
		// DROPS calculation
		// - have cancelled

		final boolean isPlayer = event.getAttacker() instanceof Player;
		final Player player = isPlayer ? (Player) event.getAttacker() : null;
		final ItemStack hand = isPlayer ? player.getItemInHand() : null;

		final Collection<ItemStack> drops;
		if (isPlayer) {
			if (player.getGameMode() == GameMode.CREATIVE) {
				drops = EMPTY_STACK;
			} else {
				drops = vehicleToDrops(event.getVehicle());
			}
		} else {
			if (event.getAttacker() instanceof Arrow
					&& ((Arrow) event.getAttacker()).getShooter() instanceof Player
					&& ((Player) ((Arrow) event.getAttacker()).getShooter()).getGameMode() == GameMode.CREATIVE) {
				drops = EMPTY_STACK;
			} else {
				drops = vehicleToDrops(event.getVehicle());
			}
		}

		final PrefireVehicleDestroyEvent prefire_event = new PrefireVehicleDestroyEvent(
				event.getVehicle(), event.getAttacker(), isPlayer, player, hand, drops, cancelled
		);

		plManager.callEvent(prefire_event);

		final AsyncVehicleDestroyEvent async_event = new AsyncVehicleDestroyEvent(
				prefire_event.vehicle, prefire_event.entity, prefire_event.hasPlayer(),
				prefire_event.hasPlayer() ? prefire_event.getPlayer() : null,
				prefire_event.hasPlayer() ? prefire_event.getHand() : null,
				prefire_event.getDrops(), prefire_event.isCancelled()
		);

		Bukkit.getScheduler().runTaskAsynchronously(
				AsyncBlockEvents.this, new Runnable() {
			@Override
			public void run() {
				plManager.callEvent(async_event);
				Bukkit.getScheduler().runTask(
						AsyncBlockEvents.this, new Runnable() {
					@Override
					public void run() {
						// modify base event to account for async changes
						event.setCancelled(async_event.isCancelled());
						plManager.callEvent(event, EventPriority.MONITOR);
						if (!event.isCancelled()) {
							// item drop
							final Location loc = async_event.vehicle.getLocation();
							for (final ItemStack stack : async_event.getDrops()) {
								loc.getWorld().dropItem(loc, stack);
							}

							// vehicle removal
							async_event.vehicle.remove();
						}
					}
				}
				);
			}
		}
		);
		return true;
	}

	private static class SynchroRandom {
		final Random random = new Random();

		public synchronized int nextInt(final int n) {
			return this.random.nextInt(n);
		}

		public synchronized float nextFloat() {
			return this.random.nextFloat();
		}
	}

	private static Collection<ItemStack> calculateDrops(final BlockBreakEvent event) {
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
			return EMPTY_STACK;

		final ItemStack tool = event.getPlayer().getItemInHand();
		final ArrayList<ItemStack> toreturn = new ArrayList<>(event.getBlock().getDrops(tool));

		if (tool.getEnchantmentLevel(Enchantment.SILK_TOUCH) > 0) {
			switch (event.getBlock().getType()) {
				// unaffected by silk touch
				case WATER:
				case STATIONARY_WATER:
				case LAVA:
				case STATIONARY_LAVA:
				case FIRE:
				case MOB_SPAWNER:
				case PORTAL:
				case CAKE_BLOCK:
				case LOCKED_CHEST:
				case ENDER_PORTAL:
				case SNOW:
				case BED:
				case PISTON_BASE:
				case PISTON_STICKY_BASE:
				case PISTON_EXTENSION:
				case PISTON_MOVING_PIECE:
				case DOUBLE_STEP:
				case WOOD_DOUBLE_STEP:
				case REDSTONE_WIRE:
				case CROPS:
				case SOIL:
				case SIGN_POST:
				case WALL_SIGN:
				case WOODEN_DOOR:
				case IRON_DOOR_BLOCK:
				case REDSTONE_TORCH_OFF:
				case SUGAR_CANE_BLOCK:
				case DIODE_BLOCK_OFF:
				case DIODE_BLOCK_ON:
				case MONSTER_EGGS:
				case PUMPKIN_STEM:
				case MELON_STEM:
				case NETHER_WARTS:
				case BREWING_STAND:
				case CAULDRON:
				case COCOA:
				case TRIPWIRE:
				case FLOWER_POT:
				case CARROT:
				case POTATO:
				case SKULL:
				case VINE:
				case REDSTONE_COMPARATOR_OFF:
				case REDSTONE_COMPARATOR_ON:
					break;
				// always drop if silk touch (and no default drop)
				case GLASS:
				case THIN_GLASS:
				case LEAVES:
				case ICE:
				case HUGE_MUSHROOM_1:
				case HUGE_MUSHROOM_2:
					toreturn.clear();
					toreturn.add(new ItemStack(event.getBlock().getType()));
					return toreturn;
				default:
					if (toreturn.isEmpty())
						return EMPTY_STACK;
					toreturn.clear();
					toreturn.add(new ItemStack(event.getBlock().getType()));
					return toreturn;
			}
		}

		final int level = tool.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
		final int data = event.getBlock().getData();

		int numstacks;

		switch (event.getBlock().getType()) {
			case COAL_ORE:
				numstacks = toreturn.size() * (Math.max(0, random.nextInt(level + 2) - 1) + 1);
				toreturn.clear();
				for (int i = 0; i < numstacks; i++)
					toreturn.add(new ItemStack(Material.COAL));
				return toreturn;

			case DIAMOND_ORE:
				numstacks = toreturn.size() * (Math.max(0, random.nextInt(level + 2) - 1) + 1);
				toreturn.clear();
				for (int i = 0; i < numstacks; i++)
					toreturn.add(new ItemStack(Material.DIAMOND));
				return toreturn;

			case EMERALD_ORE:
				numstacks = toreturn.size() * (Math.max(0, random.nextInt(level + 2) - 1) + 1);
				toreturn.clear();
				for (int i = 0; i < numstacks; i++)
					toreturn.add(new ItemStack(Material.EMERALD));
				return toreturn;

			case LAPIS_ORE:
				numstacks = toreturn.size() * (Math.max(0, random.nextInt(level + 2) - 1) + 1);
				toreturn.clear();
				for (int i = 0; i < numstacks; i++)
					toreturn.add(new ItemStack(Material.INK_SACK, 1, (byte) 4));
				return toreturn;

			case QUARTZ_ORE:
				numstacks = toreturn.size() * (Math.max(0, random.nextInt(level + 2) - 1) + 1);
				toreturn.clear();
				for (int i = 0; i < numstacks; i++)
					toreturn.add(new ItemStack(Material.QUARTZ));
				return toreturn;

			case REDSTONE_ORE:
			case GLOWING_REDSTONE_ORE:
				numstacks = toreturn.isEmpty() ? 0 : toreturn.size() + random.nextInt(level + 1);
				toreturn.clear();
				for (int i = 0; i < numstacks; i++)
					toreturn.add(new ItemStack(Material.REDSTONE));
				return toreturn;

			case CARROT:
				numstacks = 1;
				for (int i = 0; i < 3 + level; i++)
					if (random.nextInt(15) <= data)
						numstacks++;
				toreturn.clear();
				for (int i = 0; i < numstacks; i++)
					toreturn.add(new ItemStack(Material.CARROT_ITEM));
				return toreturn;

			case POTATO:
				numstacks = 1;
				for (int i = 0; i < 3 + level; i++)
					if (random.nextInt(15) <= data)
						numstacks++;
				toreturn.clear();
				for (int i = 0; i < numstacks; i++)
					toreturn.add(new ItemStack(Material.POTATO_ITEM));
				if (random.nextInt(50) == 0)
					toreturn.add(new ItemStack(Material.POISONOUS_POTATO));
				return toreturn;

			case GLOWSTONE:
				numstacks = Math.min(4, 2 + random.nextInt(3) + random.nextInt(level + 1));
				toreturn.clear();
				for (int i = 0; i < numstacks; i++)
					toreturn.add(new ItemStack(Material.GLOWSTONE_DUST));
				return toreturn;

			case MELON_BLOCK:
				numstacks = Math.min(9, 3 + random.nextInt(5) + random.nextInt(level + 1));
				toreturn.clear();
				for (int i = 0; i < numstacks; i++)
					toreturn.add(new ItemStack(Material.MELON));
				return toreturn;

			case NETHER_WARTS:
				numstacks = 1;
				if (data >= 3)
					numstacks = 2 + random.nextInt(3) + random.nextInt(level + 1);
				toreturn.clear();
				for (int i = 0; i < numstacks; i++)
					toreturn.add(new ItemStack(Material.NETHER_STALK));
				return toreturn;

			case LONG_GRASS:
				numstacks = random.nextInt(8) == 0 ? 1 + random.nextInt(level * 2 + 1) : 0;
				toreturn.clear();
				for (int i = 0; i < numstacks; i++)
					toreturn.add(new ItemStack(Material.SEEDS));
				return toreturn;

			case CROPS:
				toreturn.clear();
				if (data >= 7) {
					toreturn.add(new ItemStack(Material.WHEAT));
					numstacks = 0;
					for (int i = 0; i < 3 + level; i++)
						if (random.nextInt(15) <= data)
							numstacks++;
					for (int i = 0; i < numstacks; i++)
						toreturn.add(new ItemStack(Material.SEEDS));
				} else {
					toreturn.add(new ItemStack(Material.SEEDS));
				}
				return toreturn;

			case GRAVEL:
				toreturn.clear();
				if (random.nextInt(10 - (Math.min(3, level) * 3)) == 0)
					toreturn.add(new ItemStack(Material.FLINT));
				else
					toreturn.add(new ItemStack(Material.GRAVEL));
				return toreturn;

			case SNOW:
				toreturn.clear();
				switch (tool.getType()) {
					case WOOD_SPADE:
					case STONE_SPADE:
					case IRON_SPADE:
					case GOLD_SPADE:
					case DIAMOND_SPADE:
						toreturn.add(new ItemStack(Material.SNOW_BALL));
				}
				return toreturn;

			case PUMPKIN_STEM:
				numstacks = 0;
				for (int i = 0; i < 3; i++)
					if (random.nextInt(15) <= data)
						numstacks++;
				toreturn.clear();
				for (int i = 0; i < numstacks; i++)
					toreturn.add(new ItemStack(Material.PUMPKIN_SEEDS));
				return toreturn;

			case MELON_STEM:
				numstacks = 0;
				for (int i = 0; i < 3; i++)
					if (random.nextInt(15) <= data)
						numstacks++;
				toreturn.clear();
				for (int i = 0; i < numstacks; i++)
					toreturn.add(new ItemStack(Material.MELON_SEEDS));
				return toreturn;

			case LEAVES:
				toreturn.clear();
				int odds;
				switch (3 & event.getBlock().getData()) {
					case 0:
						int appleodds = 200;
						if (level > 0) {
							appleodds -= 10 << level;
							if (appleodds < 40)
								appleodds = 40;
						}
						if (random.nextInt(appleodds) == 0)
							toreturn.add(new ItemStack(Material.APPLE));
						//noinspection fallthrough
					case 1:
					case 2:
						odds = 20;
						break;
					default:
						odds = 40;
						break;
				}
				if (level > 0) {
					odds -= 2 << level;
					if (odds < 10)
						odds = 10;
				}
				if (random.nextInt(odds) == 0)
					toreturn.add(new ItemStack(Material.SAPLING, 1, (short) (3 & event.getBlock().getData())));
				return toreturn;

			default:
				return toreturn;
		}
	}

	private static Material getNewMat(final BlockBreakEvent event) {
		// Only block that breaks into another block in vanilla is ice -> water
		// world can't be nether, must be a block underneath
		if (event.getBlock().getType() == Material.ICE && event.getPlayer().getGameMode() != GameMode.CREATIVE
		    && event.getBlock().getWorld().getEnvironment() != Environment.NETHER) {
			final Material under = event.getBlock().getRelative(BlockFace.DOWN).getType();
			if (under.isSolid() || under == Material.WATER || under == Material.STATIONARY_WATER || under == Material.LAVA
			    || under == Material.STATIONARY_LAVA)
				return Material.WATER;
		}
		return Material.AIR;
	}

	private static int calculateDurability(final BlockBreakEvent event) {
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
			return 0;
		final ItemStack item = event.getPlayer().getItemInHand();
		int damage = 0;
		switch (item.getType()) {
			case WOOD_SWORD:
			case STONE_SWORD:
			case IRON_SWORD:
			case GOLD_SWORD:
			case DIAMOND_SWORD:
				damage = 2;
				break;
			case WOOD_PICKAXE:
			case STONE_PICKAXE:
			case IRON_PICKAXE:
			case GOLD_PICKAXE:
			case DIAMOND_PICKAXE:
			case WOOD_AXE:
			case STONE_AXE:
			case IRON_AXE:
			case GOLD_AXE:
			case DIAMOND_AXE:
			case WOOD_SPADE:
			case STONE_SPADE:
			case IRON_SPADE:
			case GOLD_SPADE:
			case DIAMOND_SPADE:
				damage = 1;
				break;
			case SHEARS:
				switch (event.getBlock().getType()) {
					case WEB:
					case LEAVES:
					case LONG_GRASS:
					case TRIPWIRE:
					case VINE:
						damage = 1;
				}
				break;
		}
		if (damage != 0) {
			final int level = item.getEnchantmentLevel(Enchantment.DURABILITY);
			if (level > 0 && random.nextInt(level + 1) > 0) {
				damage = 0;
			}
		}
		return damage;
	}

	private static byte calculatePlaceData(final BlockPlaceEvent event) {
		final byte def = event.getBlock().getData();
		final float yaw = event.getPlayer().getLocation().getYaw();
		final int temp1;
		final int temp2;
		switch (event.getBlock().getType()) {
			case COCOA:
			case FENCE_GATE:
				return (byte) ((floor((double) (yaw * 4.0F / 360.0F) + 0.5D) & 3) % 4);
			case ANVIL:
				temp1 = ((floor((double) (yaw * 4.0F / 360.0F) + 0.5D) & 3) + 1) % 4;
				temp2 = def >> 2;
				switch (temp1) {
					case 0:
						return (byte) (2 | temp2 << 2);
					case 1:
						return (byte) (3 | temp2 << 2);
					case 2:
						return (byte) (temp2 << 2);
					default: // case 3
						return (byte) (1 | temp2 << 2);
				}
			case SKULL:
				if (event.getBlock().getRelative(BlockFace.DOWN).equals(event.getBlockAgainst())) {
					return (byte) 1;
				} else if (event.getBlock().getRelative(BlockFace.NORTH).equals(event.getBlockAgainst())) {
					return (byte) 3;
				} else if (event.getBlock().getRelative(BlockFace.EAST).equals(event.getBlockAgainst())) {
					return (byte) 4;
				} else if (event.getBlock().getRelative(BlockFace.SOUTH).equals(event.getBlockAgainst())) {
					return (byte) 2;
				} else if (event.getBlock().getRelative(BlockFace.WEST).equals(event.getBlockAgainst())) {
					return (byte) 5;
				}
				// FALLTHROUGH -- DO NOT REORDER
				//noinspection fallthrough,SuspiciousIndentAfterControlStatement
			case PUMPKIN:
			case JACK_O_LANTERN:
				return (byte) (floor((double) (yaw * 4.0F / 360.0F) + 2.5D) & 3);
			case DIODE_BLOCK_ON:
			case DIODE_BLOCK_OFF:
			case ENDER_PORTAL_FRAME:
			case REDSTONE_COMPARATOR_ON:
			case REDSTONE_COMPARATOR_OFF:
				return (byte) (((floor((double) (yaw * 4.0F / 360.0F) + 0.5D) & 3) + 2) % 4);
			case WOOD_STAIRS:
			case COBBLESTONE_STAIRS:
			case BRICK_STAIRS:
			case SMOOTH_STAIRS:
			case NETHER_BRICK_STAIRS:
			case SANDSTONE_STAIRS:
			case SPRUCE_WOOD_STAIRS:
			case BIRCH_WOOD_STAIRS:
			case JUNGLE_WOOD_STAIRS:
			case QUARTZ_STAIRS:
				temp1 = floor((double) (yaw * 4.0F / 360.0F) + 0.5D) & 3;
				temp2 = def & 4;
				switch (temp1) {
					case 0:
						return (byte) (2 | temp2);
					case 1:
						return (byte) (1 | temp2);
					case 2:
						return (byte) (3 | temp2);
					default: // case 3
						return (byte) temp2;
				}
			case PISTON_BASE:
			case PISTON_STICKY_BASE:
			case DISPENSER:
			case DROPPER:
				final Location ploc = event.getPlayer().getLocation();
				final Location bloc = event.getBlock().getLocation();
				if (abs((float) ploc.getX() - (float) bloc.getBlockX()) < 2.0F
				    && abs((float) ploc.getZ() - (float) bloc.getBlockZ()) < 2.0F) {
					//					final double height = ploc.getY() + 1.82D - 1.62D;
					final double height = ploc.getY() + 1.82D;
					if (height - (double) bloc.getBlockY() > 2.0D)
						return 1;
					if ((double) bloc.getBlockY() - height > 0.0D)
						return 0;
				}
				// FALLTHROUGH -- DO NOT REORDER
				//noinspection fallthrough
			case FURNACE:
			case CHEST:
			case ENDER_CHEST:
			case TRAPPED_CHEST:
				temp1 = floor((double) (yaw * 4.0F / 360.0F) + 0.5D) & 3;
				switch (temp1) {
					case 0:
						return (byte) 2;
					case 1:
						return (byte) 5;
					case 2:
						return (byte) 3;
					default: // case 3
						return (byte) 4;
				}
			case LEVER:
				temp1 = def & 7;
				temp2 = def & 8;
				if (temp1 == 5) {
					if ((floor((double) (yaw * 4.0F / 360.0F) + 0.5D) & 1) == 0) {
						return (byte) (5 | temp2);
					} else {
						return (byte) (6 | temp2);
					}
				} else if (temp1 == 0) {
					if ((floor((double) (yaw * 4.0F / 360.0F) + 0.5D) & 1) == 0) {
						return (byte) (7 | temp2);
					} else {
						return (byte) temp2;
					}
				} else {
					return def;
				}
				//noinspection SuspiciousIndentAfterControlStatement
			case TRIPWIRE:
				final Block block = event.getBlock().getRelative(BlockFace.DOWN);
				if (((CraftWorld) block.getWorld()).getHandle().w(block.getX(), block.getY(), block.getZ()))
					return calculateTripwireConnectivity(event);
				else
					return (byte) (2 + calculateTripwireConnectivity(event));
			default:
				return def;
		}
	}

	private static byte calculateTripwireConnectivity(final BlockPlaceEvent event) {
		// north / south
		Block seed = event.getBlock();
		BlockFace direction = BlockFace.NORTH;
		int count = 1;
		loop: while (count < 42) {
			seed = seed.getRelative(direction);
			switch (seed.getType()) {
				case TRIPWIRE_HOOK:
					if (direction == BlockFace.SOUTH)
						return 4;
					direction = BlockFace.SOUTH;
					break;
				case TRIPWIRE:
					count++;
					break;
				default:
					break loop;
			}
		}
		// east / west
		seed = event.getBlock();
		direction = BlockFace.EAST;
		count = 1;
		loop: while (count < 42) {
			seed = seed.getRelative(direction);
			switch (seed.getType()) {
				case TRIPWIRE_HOOK:
					if (direction == BlockFace.WEST)
						return 4;
					direction = BlockFace.WEST;
					break ;
				case TRIPWIRE:
					count++;
					break;
				default:
					break loop;
			}
		}
		return 0;
	}

	private static int calculatePlaceCost(final BlockPlaceEvent event) {
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
			return 0;
		return 1;
	}

	@SuppressWarnings("AssignmentToNull")
	public static @Nullable ItemStack testAddItem(
			final IInventory iinventory, final ItemStack itemstack, final int i
	) {
		net.minecraft.server.v1_5_R3.ItemStack nmsstack = CraftItemStack.asNMSCopy(itemstack);

		if (iinventory instanceof IWorldInventory && i > -1) {
			final IWorldInventory iworldinventory = (IWorldInventory) iinventory;
			final int[] aint = iworldinventory.getSlotsForFace(i);

			for (int j = 0; j < aint.length && nmsstack != null && nmsstack.count > 0; ++j) {
				nmsstack = testTryMoveInItem(iinventory, nmsstack, aint[j], i);
			}
		} else {
			final int k = iinventory.getSize();

			for (int l = 0; l < k && nmsstack != null && nmsstack.count > 0; ++l) {
				nmsstack = testTryMoveInItem(iinventory, nmsstack, l, i);
			}
		}

		if (nmsstack != null && nmsstack.count == 0) {
			nmsstack = null;
		}

		if (nmsstack == null)
			return null;
		return CraftItemStack.asCraftMirror(nmsstack);
	}

	@SuppressWarnings({"AssignmentToNull", "ConstantConditions"})
	private static net.minecraft.server.v1_5_R3.ItemStack testTryMoveInItem(
			final IInventory iinventory, net.minecraft.server.v1_5_R3.ItemStack itemstack, final int i, final int j
	) {
		final net.minecraft.server.v1_5_R3.ItemStack itemstack1 = iinventory.getItem(i);

		if (canPlaceItemInInventory(iinventory, itemstack, i, j)) {
			if (itemstack1 == null) {
				itemstack = null;
			} else if (canMergeItems(itemstack1, itemstack)) {
				final int k = itemstack.getMaxStackSize() - itemstack1.count;
				final int l = Math.min(itemstack.count, k);
				itemstack.count -= l;
			}
		}

		return itemstack;
	}

	private static boolean canPlaceItemInInventory(
			final IInventory iinventory, final net.minecraft.server.v1_5_R3.ItemStack itemstack, final int i, final int j
	) {
		return iinventory.b(i, itemstack)
		       && (!(iinventory instanceof IWorldInventory)
		           || ((IWorldInventory) iinventory).canPlaceItemThroughFace(i, itemstack, j));
	}

	private static boolean canMergeItems(
			final net.minecraft.server.v1_5_R3.ItemStack itemstack, final net.minecraft.server.v1_5_R3.ItemStack itemstack1
	) {
//		return itemstack.id != itemstack1.id
//		       ? false
//		       : (itemstack.getData() != itemstack1.getData()
//		          ? false
//		          : (itemstack.count > itemstack.getMaxStackSize()
//		             ? false
//		             : net.minecraft.server.v1_5_R2.ItemStack.equals(itemstack, itemstack1)));
		return itemstack.id == itemstack1.id
		       && itemstack.getData() == itemstack1.getData()
		       && itemstack.count <= itemstack.getMaxStackSize()
		       && net.minecraft.server.v1_5_R3.ItemStack.equals(itemstack, itemstack1);
	}

	private static Collection<ItemStack> vehicleToDrops(final Vehicle vehicle) {
		final ArrayList<ItemStack> drops = new ArrayList<>();
		switch(vehicle.getType()) {
			case BOAT:
				drops.add(new ItemStack(Material.BOAT));
				break;
			case MINECART:
				drops.add(new ItemStack(Material.MINECART));
				break;
			case MINECART_FURNACE:
				drops.add(new ItemStack(Material.MINECART));
				drops.add(new ItemStack(Material.FURNACE));
				break;
			case MINECART_CHEST:
				drops.add(new ItemStack(Material.MINECART));
				drops.add(new ItemStack(Material.CHEST));
				break;
			case MINECART_TNT:
				drops.add(new ItemStack(Material.MINECART));
				drops.add(new ItemStack(Material.TNT));
				break;
			case MINECART_HOPPER:
				drops.add(new ItemStack(Material.MINECART));
				drops.add(new ItemStack(Material.HOPPER));
				break;
			case MINECART_MOB_SPAWNER:
				drops.add(new ItemStack(Material.MINECART));
				break;
		}
		return drops;
	}

	private static int floor(final double val) {
		final int temp = (int) val;
		return val < (double) temp ? temp - 1 : temp;
	}

	private static float abs(final float f) {
		return f >= 0.0F ? f : -f;
	}

	public static void update(final World world, final int x, final int y, final int z, final int blockID) {
		((CraftWorld) world).getHandle().applyPhysics(x, y, z, blockID);
	}

	public void updateTripwire(final World world, final int x, final int y, final int z, final int data) {
		try {
			tripwireUpdateMethod.invoke(
					net.minecraft.server.v1_5_R3.Block.TRIPWIRE, ((CraftWorld) world).getHandle(), x, y, z, data
			);
		} catch (Exception e) {
			getLogger().log(Level.SEVERE, "Could not update tripwire data", e);
		}
	}

	public static BlockFace getSkullFace(final int rotation) {
		switch (rotation) {
			case 0:
				return BlockFace.NORTH;
			case 1:
				return BlockFace.NORTH_NORTH_EAST;
			case 2:
				return BlockFace.NORTH_EAST;
			case 3:
				return BlockFace.EAST_NORTH_EAST;
			case 4:
				return BlockFace.EAST;
			case 5:
				return BlockFace.EAST_SOUTH_EAST;
			case 6:
				return BlockFace.SOUTH_EAST;
			case 7:
				return BlockFace.SOUTH_SOUTH_EAST;
			case 8:
				return BlockFace.SOUTH;
			case 9:
				return BlockFace.SOUTH_SOUTH_WEST;
			case 10:
				return BlockFace.SOUTH_WEST;
			case 11:
				return BlockFace.WEST_SOUTH_WEST;
			case 12:
				return BlockFace.WEST;
			case 13:
				return BlockFace.WEST_NORTH_WEST;
			case 14:
				return BlockFace.NORTH_WEST;
			case 15:
				return BlockFace.NORTH_NORTH_WEST;
			default:
				throw new AssertionError(rotation);
		}
	}

	public static SkullType getSkullType(final int id) {
		switch (id) {
			case 0:
				return SkullType.SKELETON;
			case 1:
				return SkullType.WITHER;
			case 2:
				return SkullType.ZOMBIE;
			case 3:
				return SkullType.PLAYER;
			case 4:
				return SkullType.CREEPER;
			default:
				return SkullType.SKELETON;
		}
	}

	public static enum AsyncEventType {
		BLOCK_BREAK, BLOCK_PLACE, BLOCK_INTERACT, BUCKET_EMPTY, BUCKET_FILL
	}

	public class BlockProcessor implements Listener {
		public final ProtocolManager pmanager = ProtocolLibrary.getProtocolManager();
		public final HashSet<BlockLocation> locked = new HashSet<>();
		public final HashMap<BlockLocation, AsyncEventType> processing = new HashMap<>();
		public final HashMap<Chunk, Integer> chunker = new HashMap<>();

		public boolean blockSound = false;

		public BlockProcessor() {
			plManager.registerEvents(this, AsyncBlockEvents.this);

			this.pmanager.addPacketListener(
					new PacketAdapter(
							AsyncBlockEvents.this, ConnectionSide.SERVER_SIDE, ListenerPriority.HIGH, Packets.Server.BLOCK_CHANGE
					) {
						@Override
						public void onPacketSending(final PacketEvent event) {
							if (BlockProcessor.this.locked.isEmpty())
								return;

							final StructureModifier<Integer> fields = event.getPacket().getIntegers();
							if (BlockProcessor.this.locked.contains(
									new BlockLocation(
											event.getPlayer().getWorld(), fields.read(0), fields.read(1), fields.read(2)
									)
							)) {
								event.setCancelled(true);
							}
						}
					}
			);

			this.pmanager.addPacketListener(
					new PacketAdapter(
							AsyncBlockEvents.this, ConnectionSide.SERVER_SIDE, ListenerPriority.HIGH, Packets.Server.UPDATE_SIGN
					) {
						@Override
						public void onPacketSending(final PacketEvent event) {
							if (BlockProcessor.this.locked.isEmpty())
								return;

							final StructureModifier<Integer> fields = event.getPacket().getIntegers();
							if (BlockProcessor.this.locked.contains(
									new BlockLocation(
											event.getPlayer().getWorld(), fields.read(0), fields.read(1), fields.read(2)
									)
							)) {
								event.setCancelled(true);
							}
						}
					}
			);

			this.pmanager.addPacketListener(
					new PacketAdapter(
							AsyncBlockEvents.this, ConnectionSide.SERVER_SIDE, ListenerPriority.HIGH, Packets.Server.NAMED_SOUND_EFFECT
					) {
						@Override
						public void onPacketSending(final PacketEvent event) {
							if (BlockProcessor.this.blockSound) {
								event.setCancelled(true);
							}
						}
					}
			);
		}

		@EventHandler(ignoreCancelled = true)
		public void onChunkUnload(final ChunkUnloadEvent event) {
			if (this.chunker.containsKey(event.getChunk()))
				event.setCancelled(true);
		}

		public void add(final BlockLocation loc, final AsyncEventType etype, final int type, final int data) {
			sendBlock(loc, type, data);
			add(loc, etype);
		}

		public void add(final BlockLocation loc, final AsyncEventType etype) {
			this.locked.add(loc);
			this.processing.put(loc, etype);
			final Chunk chunk = loc.world.getChunkAt(loc.x, loc.z);
			if (this.chunker.containsKey(chunk)) {
				this.chunker.put(chunk, this.chunker.get(chunk) + 1);
			} else {
				this.chunker.put(chunk, 1);
			}
//			System.out.println("Adding " + loc);
		}

		public void unlock(final BlockLocation loc) {
			this.locked.remove(loc);
//			System.out.println("Unlocking " + loc);
		}

		public void remove(final BlockLocation loc) {
			this.locked.remove(loc); // just a precaution
			this.processing.remove(loc);
			final Chunk chunk = loc.world.getChunkAt(loc.x, loc.z);
			if (this.chunker.containsKey(chunk)) {
				final int val = this.chunker.get(chunk);
				if (val == 1) {
					this.chunker.remove(chunk);
				} else {
					this.chunker.put(chunk, val - 1);
				}
			}
//			System.out.println("Removing " + loc);
		}

		public void sendBlock(final BlockLocation loc, final int type, final int data) {
			final Location temp = loc.world.getBlockAt(loc.x, loc.y, loc.z).getLocation();
			final PacketContainer packet = new PacketContainer(53);
			final StructureModifier<Integer> fields = packet.getIntegers();
			fields.write(0, loc.x);
			fields.write(1, loc.y);
			fields.write(2, loc.z);
			fields.write(3, type);
			fields.write(4, data);
			for (final Player player : loc.world.getPlayers()) {
				if (player.getLocation().distanceSquared(temp) < vdistance2) {
					try {
						this.pmanager.sendServerPacket(player, packet, false);
					} catch (InvocationTargetException e) {
						AsyncBlockEvents.this.getLogger().log(Level.WARNING, "Failed to send block to player", e);
					}
				}
			}
		}

		public void sendSignUpdate(final BlockLocation loc, final String[] lines) {
			final Location temp = loc.world.getBlockAt(loc.x, loc.y, loc.z).getLocation();
			final PacketContainer packet = new PacketContainer(130);
			final StructureModifier<Integer> fields = packet.getIntegers();
			fields.write(0, loc.x);
			fields.write(1, loc.y);
			fields.write(2, loc.z);
			final StructureModifier<String[]> strings = packet.getStringArrays();
			strings.write(0, lines);
			for (final Player player : loc.world.getPlayers()) {
				if (player.getLocation().distanceSquared(temp) < vdistance2) {
					try {
						this.pmanager.sendServerPacket(player, packet, false);
					} catch (InvocationTargetException e) {
						AsyncBlockEvents.this.getLogger().log(Level.WARNING, "Failed to send sign update to player", e);
					}
				}
			}
		}
	}

	public class AsyncBlockChangeDelegate implements BlockChangeDelegate {
		public final Location location;
		public final World world;
		public final Player player;
		public final TreeType treeType;

		private final HashMap<Coordinate, SimpleBlock> cache;

		public AsyncBlockChangeDelegate(final Location location, final Player player, final TreeType treeType) {
			this.location = location;
			this.world = this.location.getWorld();
			this.player = player;
			this.treeType = treeType;
			this.cache = new HashMap<>();
		}

		public boolean flush() {
			final ArrayList<BlockState> blocks = new ArrayList<>();
			BlockState temp;
			for (final Entry<Coordinate, SimpleBlock> entry : this.cache.entrySet()) {
				temp = this.world.getBlockAt(entry.getKey().x, entry.getKey().y, entry.getKey().z).getState();
				temp.setTypeId(entry.getValue().type);
				temp.setRawData((byte) entry.getValue().data);
				blocks.add(temp);
			}
			// async event, then sync event
			final AsyncStructureGrowEvent async_event = new AsyncStructureGrowEvent(
					this.location, this.player, this.treeType, blocks, false
			);
			plManager.callEvent(async_event);
			final StructureGrowEvent sync_event = new StructureGrowEvent(
					this.location, this.treeType, true, this.player, async_event.getBlocks()
			);
			if (async_event.isCancelled())
				sync_event.setCancelled(true);
			plManager.callEvent(sync_event);
			if (!sync_event.isCancelled()) {
				Bukkit.getScheduler().runTask(AsyncBlockEvents.this, new Runnable() {
					@Override
					public void run() {
						for (final BlockState state : sync_event.getBlocks()) {
							state.update(true);
						}
						for (final BlockState state : sync_event.getBlocks()) {
							update(AsyncBlockChangeDelegate.this.world, state.getX(), state.getY(), state.getZ(), state.getTypeId());
						}
					}
				});
				return true;
			}
			return false;
		}

		@Override
		public int getHeight() {
			return this.world.getMaxHeight();
		}

		@Override
		public int getTypeId(final int x, final int y, final int z) {
			final Coordinate temp = new Coordinate(x, y, z);
			if (this.cache.containsKey(temp))
				return this.cache.get(temp).type;
			return this.world.getBlockTypeIdAt(x, y, z);
		}

		@Override
		public boolean isEmpty(final int x, final int y, final int z) {
			return getTypeId(x, y, z) == 0;
		}

		@Override
		public boolean setRawTypeId(final int x, final int y, final int z, final int typeId) {
			return setTypeIdAndData(x, y, z, typeId, 0);
		}

		@Override
		public boolean setRawTypeIdAndData(final int x, final int y, final int z, final int typeId, final int data) {
			return setTypeIdAndData(x, y, z, typeId, data);
		}

		@Override
		public boolean setTypeId(final int x, final int y, final int z, final int typeId) {
			return setTypeIdAndData(x, y, z, typeId, 0);
		}

		@Override
		public boolean setTypeIdAndData(final int x, final int y, final int z, final int typeId, final int data) {
			this.cache.put(new Coordinate(x, y, z), new SimpleBlock(typeId, data));
			return true;
		}
	}

	public class AssManager {
		final HashMap<BlockLocation, HashMap<EntityHuman, Boolean>> data;
		final HashMap<BlockLocation, Integer> decay;

		final HashMap<PlayerBlock, Integer> doubleTap;

		public AssManager() {
			this.data = new HashMap<>();
			this.decay = new HashMap<>();
			this.doubleTap = new HashMap<>();

			// ass pressure unload detection
			Bukkit.getScheduler().runTaskTimer(
					AsyncBlockEvents.this, new Runnable() {
				@Override
				public void run() {
					List inrange;
					boolean onlyliving;
					double a, b, c, d, e, f;
					boolean shoulddie;
					int delay;
					HashMap<EntityHuman, Boolean> scratch = new HashMap<>();

					final List<PsuedoPlayerInteractEvent> events = new ArrayList<>();
					for (
							Iterator<Entry<BlockLocation, HashMap<EntityHuman, Boolean>>> it =
									AssManager.this.data.entrySet().iterator();
					    it.hasNext();
					) {
						final Entry<BlockLocation, HashMap<EntityHuman, Boolean>> entry = it.next();
						final BlockLocation loc = entry.getKey();
						final Block block = loc.world.getBlockAt(loc.x, loc.y, loc.z);
						final Material type = block.getType();

						a = loc.x; b = loc.y; c = loc.z; d = loc.x; e = loc.y; f = loc.z;
						onlyliving = false;
						switch (type) {
							case STONE_PLATE:
								onlyliving = true;
								// FALLTHOUGH - DO NOT REORDER
								//noinspection fallthrough
							case WOOD_PLATE:
								a += 0.125; b += 0.0; c += 0.125; d += 1.0 - 0.125; e += 0.25; f += 1.0 - 0.125;
								delay = 20;
								break;
							case TRIPWIRE:
								final byte blockData = block.getData();
								if (!((blockData & 2) == 2)) {
									a += 0.0; b += 0.0; c += 0.0; d += 1.0; e += 0.09375; f += 1.0;
								} else if (!((blockData & 4) == 4)) {
									a += 0.0; b += 0.0; c += 0.0; d += 1.0; e += 0.5; f += 1.0;
								} else {
									a += 0.0; b += 0.0625; c += 0.0; d += 1.0; e += 0.15625; f += 1.0;
								}
								delay = 10;
								break;
							default:
								it.remove();
								continue;
						}

						inrange =
								((CraftWorld) loc.world).getHandle().getEntities(null, AxisAlignedBB.a().a(a, b, c, d, e, f));

						shoulddie = true;
						for (final Object entity : inrange) {
							if (entity == null)
								continue;
							if (entity instanceof EntityHuman) {
								final EntityHuman hooman = (EntityHuman) entity;
								if (entry.getValue().containsKey(hooman)) {
									final Boolean fetch = entry.getValue().get(hooman);
									if (fetch)
										shoulddie = false;
									scratch.put(hooman, fetch);
								} else {
									events.add(new PsuedoPlayerInteractEvent(
											(Player) hooman.getBukkitEntity(), Action.PHYSICAL, null, block, BlockFace.SELF)
									);
								}
							} else if (entity instanceof EntityLiving) {
								shoulddie = false;
							} else {
								if (!onlyliving)
									shoulddie = false;
							}
						}

						// handle death
						if (shoulddie) {
							if (!AssManager.this.decay.containsKey(loc))
								AssManager.this.decay.put(loc, delay);
						} else {
							AssManager.this.decay.remove(loc);
						}

						// handle swap and clear
						entry.getValue().clear();
						final HashMap<EntityHuman, Boolean> temp = entry.getValue();
						entry.setValue(scratch);
						scratch = temp;
					}

					for (
							Iterator<Entry<BlockLocation, Integer>> it = AssManager.this.decay.entrySet().iterator(); it.hasNext();
					) {
						final Entry<BlockLocation, Integer> entry = it.next();
						if (entry.getValue() > 1) {
							// decrement decay entry
							entry.setValue(entry.getValue() - 1);
						} else {
							// die
							final Block block =
									entry.getKey().world.getBlockAt(entry.getKey().x, entry.getKey().y, entry.getKey().z);
							switch (block.getType()) {
								case STONE_PLATE:
								case WOOD_PLATE:
									if (block.getData() == 1) {
										block.getWorld().playSound(block.getLocation().add(0.5, 0.1, 0.5), Sound.CLICK, 0.3F, 0.5F);
										block.setData((byte) 0);
										update(block.getWorld(), block.getX(), block.getY(), block.getZ(), block.getTypeId());
										update(
												block.getWorld(), block.getX(), block.getY() - 1, block.getZ(),
												block.getRelative(BlockFace.DOWN).getTypeId()
										);
									}
									break;
								case TRIPWIRE:
									block.setData((byte) (block.getData() & 14));
									updateTripwire(block.getWorld(), block.getX(), block.getY(), block.getZ(), block.getData());
									update(block.getWorld(), block.getX(), block.getY(), block.getZ(), 132);
									break;
							}
							it.remove();
							AssManager.this.data.remove(entry.getKey());
						}
					}

					// call new Interact events
					for (final Event event : events) {
						plManager.callEvent(event);
					}

					// clear out double tap entries
					for (
							Iterator<Entry<PlayerBlock, Integer>> it = AssManager.this.doubleTap.entrySet().iterator();
							it.hasNext();
					) {
						final Entry<PlayerBlock, Integer> entry = it.next();
						if (entry.getValue() > 1) {
							entry.setValue(entry.getValue() - 1);
						} else {
							it.remove();
						}
					}
				}
			}, 1, 1
			);
		}

		public void add(final BlockLocation bloc, final EntityHuman hooman, final boolean allowed) {
			if (!this.data.containsKey(bloc))
				this.data.put(bloc, new HashMap<EntityHuman, Boolean>());
			this.data.get(bloc).put(hooman, allowed);
		}

		public boolean doubleTap(final BlockLocation bloc, final Player player) {
			final PlayerBlock pb = new PlayerBlock(bloc, player);
			if (this.doubleTap.containsKey(pb)) {
				this.doubleTap.put(pb, 2);
				return false;
			} else {
				this.doubleTap.put(pb, 2);
				return true;
			}
		}
	}

	public class InventoryManager {
		final HashMap<BlockLocation, InventoryHelper> data;

		public InventoryManager() {
			this.data = new HashMap<>();

			// register close listeners
			Bukkit.getPluginManager().registerEvents(
					new Listener() {
						@EventHandler(priority = EventPriority.HIGHEST)
						public void onInvClose(final InventoryCloseEvent event) {
							if (event.getPlayer() instanceof Player) {
								final Player temp = (Player) event.getPlayer();
								remove(temp);
							}
						}

						@EventHandler(priority = EventPriority.HIGHEST)
						public void onQuit(final PlayerQuitEvent event) {
							remove(event.getPlayer());
						}
					}, AsyncBlockEvents.this
			);

			// chest open clock
			Bukkit.getScheduler().runTaskTimer(
					AsyncBlockEvents.this, new Runnable() {
				@Override
				public void run() {
					World temp;
					for (final InventoryHelper invhelper : InventoryManager.this.data.values()) {
						temp = invhelper.loc.getWorld();
						for (final Player player : temp.getPlayers()) {
							if (player.getLocation().distanceSquared(invhelper.loc) < vdistance2) {
								player.playNote(invhelper.loc, (byte) 1, (byte) 1);
							}
						}
					}
				}
			}, 20, 20
			);

			// stuck chest clock
			Bukkit.getScheduler().runTaskTimer(
					AsyncBlockEvents.this, new Runnable() {
				@Override
				public void run() {
					final LinkedList<Player> toremove = new LinkedList<>();
					for (final Entry<BlockLocation, InventoryHelper> entry : InventoryManager.this.data.entrySet()) {
						for (final Player dude : entry.getKey().world.getPlayers()) {
							if (dude.getOpenInventory().getType() == InventoryType.CRAFTING) {
								toremove.add(dude);
							}
						}
					}
					for (final Player player : toremove) {
						remove(player);
					}
				}
			}, 30 * 20, 30 * 20
			);
		}

		public void add(final BlockLocation bloc, final Location loc, final Player player, final InventoryView iv) {
			final InventoryOpenEvent ioevent = new InventoryOpenEvent(iv);
			Bukkit.getPluginManager().callEvent(ioevent);
			if (ioevent.isCancelled()) {
				iv.close();
				return;
			}
			if (!this.data.containsKey(bloc)) {
				this.data.put(bloc, new InventoryHelper(loc));
				// play note
				for (final Player dude : loc.getWorld().getPlayers()) {
					if (dude.getLocation().distanceSquared(loc) < vdistance2) {
						dude.playNote(loc, (byte) 1, (byte) 1);
					}
				}
			}
			this.data.get(bloc).players.add(player);
		}

		public void remove(final Player player) {
			final LinkedList<BlockLocation> toremove = new LinkedList<>();
			for (final Entry<BlockLocation, InventoryHelper> entry : this.data.entrySet()) {
				entry.getValue().players.remove(player);
				if (entry.getValue().players.isEmpty()) {
					toremove.add(entry.getKey());
				}
			}
			for (final BlockLocation bloc : toremove) {
				// play note
				final Location temp = this.data.get(bloc).loc;
				for (final Player dude : temp.getWorld().getPlayers()) {
					if (dude.getLocation().distanceSquared(temp) < vdistance2) {
						dude.playNote(temp, (byte) 1, (byte) 0);
					}
				}
				this.data.remove(bloc);
			}
		}
	}

	public static class InventoryHelper {
		public final Location loc;
		public final LinkedList<Player> players;

		public InventoryHelper(final Location loc) {
			this.loc = loc;
			this.players = new LinkedList<>();
		}
	}

	public static class SimpleBlock {
		public final int type;
		public final int data;

		public SimpleBlock(final int type, final int data) {
			this.type = type;
			this.data = data;
		}
	}

	public static class BlockLocation {
		public final World world;
		public final int x;
		public final int y;
		public final int z;
		public final int hashcode;

		public BlockLocation(final World world, final int x, final int y, final int z) {
			this.world = world;
			this.x = x;
			this.y = y;
			this.z = z;
			this.hashcode = calculateHashCode();
		}

		public Location getBukkitLocation() {
			return new Location(this.world, this.x, this.y, this.z);
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			final BlockLocation that = (BlockLocation) o;

			return this.x == that.x && this.y == that.y && this.z == that.z && this.world.equals(that.world);
		}

		@Override
		public int hashCode() {
			return this.hashcode;
		}

		public int calculateHashCode() {
			int result = this.world.hashCode();
			result = 31 * result + this.x;
			result = 31 * result + this.y;
			result = 31 * result + this.z;
			return result;
		}

		@Override
		public String toString() {
			return "BlockLocation{" +
			       "world=" + this.world +
			       ", x=" + this.x +
			       ", y=" + this.y +
			       ", z=" + this.z +
			       ", hashcode=" + this.hashcode +
			       '}';
		}
	}

	public static class PlayerBlock {
		public final BlockLocation bloc;
		public final Player player;
		public final int hashcode;

		public PlayerBlock(final BlockLocation bloc, final Player player) {
			this.bloc = bloc;
			this.player = player;
			this.hashcode = calculateHashCode();
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			final PlayerBlock that = (PlayerBlock) o;

			return this.bloc.equals(that.bloc) && this.player.equals(that.player);
		}

		@Override
		public int hashCode() {
			return this.hashcode;
		}

		public int calculateHashCode() {
			int result = this.bloc.hashCode();
			result = 31 * result + this.player.hashCode();
			return result;
		}
	}
}

