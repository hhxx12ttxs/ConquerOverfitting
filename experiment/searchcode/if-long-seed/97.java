/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.utils;

import com.onarandombox.MultiverseCore.MVWorld;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.api.SafeTTeleporter;
import com.onarandombox.MultiverseCore.api.WorldPurger;
import com.onarandombox.MultiverseCore.event.MVWorldDeleteEvent;
import com.onarandombox.MultiverseCore.exceptions.PropertyDoesNotExistException;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.Location;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Public facing API to add/remove Multiverse worlds.
 */
public class WorldManager implements MVWorldManager {
    private final MultiverseCore plugin;
    private final WorldPurger worldPurger;
    private final ReentrantLock worldsLock = new ReentrantLock();
    private final Map<String, MultiverseWorld> worlds;
    private Map<String, MVWorld> worldsFromTheConfig;
    private FileConfiguration configWorlds = null;
    private Map<String, String> defaultGens;
    private String firstSpawn;

    public WorldManager(MultiverseCore core) {
        this.plugin = core;
        this.worldsFromTheConfig = new HashMap<String, MVWorld>();
        this.worlds = new HashMap<String, MultiverseWorld>();
        this.worldPurger = new SimpleWorldPurger(plugin);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getDefaultWorldGenerators() {
        this.defaultGens = new HashMap<String, String>();
        File[] files = this.plugin.getServerFolder().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.equalsIgnoreCase("bukkit.yml");
            }
        });
        if (files != null && files.length == 1) {
            FileConfiguration bukkitConfig = YamlConfiguration.loadConfiguration(files[0]);
            if (bukkitConfig.isConfigurationSection("worlds")) {
                Set<String> keys = bukkitConfig.getConfigurationSection("worlds").getKeys(false);
                for (String key : keys) {
                    defaultGens.put(key, bukkitConfig.getString("worlds." + key + ".generator", ""));
                }
            }
        } else {
            this.plugin.log(Level.WARNING, "Could not read 'bukkit.yml'. Any Default worldgenerators will not be loaded!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean cloneWorld(String oldName, String newName, String generator) {
        // Make sure we don't already know about the new world.
        if (this.isMVWorld(newName)) {
            return false;
        }
        // Make sure the old world is actually a world!
        if (this.getUnloadedWorlds().contains(oldName) || !this.isMVWorld(oldName)) {
            return false;
        }

        final File oldWorldFile = new File(this.plugin.getServer().getWorldContainer(), oldName);
        final File newWorldFile = new File(this.plugin.getServer().getWorldContainer(), newName);

        // Make sure the new world doesn't exist outside of multiverse.
        if (newWorldFile.exists()) {
            return false;
        }

        unloadWorld(oldName);

        removePlayersFromWorld(oldName);

        StringBuilder builder = new StringBuilder();
        builder.append("Copying data for world '").append(oldName).append("'...");
        this.plugin.log(Level.INFO, builder.toString());
        try {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    FileUtils.copyFolder(oldWorldFile, newWorldFile, Logger.getLogger("Minecraft"));
                }
            });
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                // do nothing
            }
            File uidFile = new File(newWorldFile, "uid.dat");
            uidFile.delete();
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
        this.plugin.log(Level.INFO, "Kind of copied stuff");

        WorldCreator worldCreator = new WorldCreator(newName);
        this.plugin.log(Level.INFO, "Started to copy settings");
        worldCreator.copy(this.getMVWorld(oldName).getCBWorld());
        this.plugin.log(Level.INFO, "Copied lots of settings");

        boolean useSpawnAdjust = this.getMVWorld(oldName).getAdjustSpawn();
        this.plugin.log(Level.INFO, "Copied more settings");

        Environment environment = worldCreator.environment();
        this.plugin.log(Level.INFO, "Copied most settings");
        if (newWorldFile.exists()) {
            this.plugin.log(Level.INFO, "Succeeded at copying stuff");
            if (this.addWorld(newName, environment, null, null, null, generator, useSpawnAdjust)) {
                // getMVWorld() doesn't actually return an MVWorld
                this.plugin.log(Level.INFO, "Succeeded at importing stuff");
                MVWorld newWorld = (MVWorld) this.getMVWorld(newName);
                MVWorld oldWorld = (MVWorld) this.getMVWorld(oldName);
                newWorld.copyValues(oldWorld);
                try {
                    // don't keep the alias the same -- that would be useless
                    newWorld.setPropertyValue("alias", newName);
                } catch (PropertyDoesNotExistException e) {
                    // this should never happen
                    throw new RuntimeException(e);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addWorld(String name, Environment env, String seedString, WorldType type, Boolean generateStructures,
                            String generator) {
        return this.addWorld(name, env, seedString, type, generateStructures, generator, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addWorld(String name, Environment env, String seedString, WorldType type, Boolean generateStructures,
                            String generator, boolean useSpawnAdjust) {
        Long seed = null;
        WorldCreator c = new WorldCreator(name);
        if (seedString != null && seedString.length() > 0) {
            try {
                seed = Long.parseLong(seedString);
            } catch (NumberFormatException numberformatexception) {
                seed = (long) seedString.hashCode();
            }
            c.seed(seed);
        }

        // TODO: Use the fancy kind with the commandSender
        if (generator != null && generator.length() != 0) {
            c.generator(generator);
        }
        c.environment(env);
        if (type != null) {
            c.type(type);
        }
        if (generateStructures != null) {
            c.generateStructures(generateStructures);
        }

        // Important: doLoad() needs the MVWorld-object in worldsFromTheConfig
        if (!worldsFromTheConfig.containsKey(name))
            worldsFromTheConfig.put(name, new MVWorld(useSpawnAdjust));

        StringBuilder builder = new StringBuilder();
        builder.append("Loading World & Settings - '").append(name).append("'");
        builder.append(" - Env: ").append(env);
        builder.append(" - Type: ").append(type);
        if (seed != null) {
            builder.append(" & seed: ").append(seed);
        }
        if (generator != null) {
            builder.append(" & generator: ").append(generator);
        }
        this.plugin.log(Level.INFO, builder.toString());

        if (!doLoad(c, true)) {
            this.plugin.log(Level.SEVERE, "Failed to Create/Load the world '" + name + "'");
            return false;
        }

        // set generator (special case because we can't read it from org.bukkit.World)
        Thread thread = Thread.currentThread();
        if (worldsLock.isLocked()) {
            plugin.log(Level.FINEST, "worldsLock is locked when attempting to access worlds on thread: " + thread);
        }
        worldsLock.lock();
        try {
            plugin.log(Level.FINEST, "Accessing worlds on thread: " + thread);
            this.worlds.get(name).setGenerator(generator);
        } finally {
            worldsLock.unlock();
        }

        this.saveWorldsConfig();
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChunkGenerator getChunkGenerator(String generator, String generatorID, String worldName) {
        if (generator == null) {
            return null;
        }

        Plugin myPlugin = this.plugin.getServer().getPluginManager().getPlugin(generator);
        if (myPlugin == null) {
            return null;
        } else {
            return myPlugin.getDefaultWorldGenerator(worldName, generatorID);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeWorldFromConfig(String name) {
        if (!unloadWorld(name)) {
            return false;
        }
        if (this.worldsFromTheConfig.containsKey(name)) {
            this.worldsFromTheConfig.remove(name);
            this.plugin.log(Level.INFO, "World '" + name + "' was removed from config.yml");

            this.saveWorldsConfig();
            return true;
        } else {
            this.plugin.log(Level.INFO, "World '" + name + "' was already removed from config.yml");
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFirstSpawnWorld(String world) {
        if ((world == null) && (this.plugin.getServer().getWorlds().size() > 0)) {
            this.firstSpawn = this.plugin.getServer().getWorlds().get(0).getName();
        } else {
            this.firstSpawn = world;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MultiverseWorld getFirstSpawnWorld() {
        MultiverseWorld world = this.getMVWorld(this.firstSpawn);
        if (world == null) {
            // If the spawn world was unloaded, get the default world
            this.plugin.log(Level.WARNING, "The world specified as the spawn world (" + this.firstSpawn + ") did not exist!!");
            try {
                return this.getMVWorld(this.plugin.getServer().getWorlds().get(0));
            } catch (IndexOutOfBoundsException e) {
                // This should only happen in tests.
                return null;
            }
        }
        return world;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean unloadWorld(String name) {
        Thread thread = Thread.currentThread();
        if (worldsLock.isLocked()) {
            plugin.log(Level.FINEST, "worldsLock is locked when attempting to access worlds on thread: " + thread);
        }
        worldsLock.lock();
        try {
            plugin.log(Level.FINEST, "Accessing worlds on thread: " + thread);
            if (this.worlds.containsKey(name)) {
                if (this.unloadWorldFromBukkit(name, true)) {
                    this.worlds.remove(name);
                    this.plugin.log(Level.INFO, "World '" + name + "' was unloaded from memory.");

                    this.worldsFromTheConfig.get(name).tearDown();

                    return true;
                } else {
                    this.plugin.log(Level.WARNING, "World '" + name + "' could not be unloaded. Is it a default world?");
                }
            } else if (this.plugin.getServer().getWorld(name) != null) {
                this.plugin.log(Level.WARNING, "Hmm Multiverse does not know about this world but it's loaded in memory.");
                this.plugin.log(Level.WARNING, "To let Multiverse know about it, use:");
                this.plugin.log(Level.WARNING, String.format("/mv import %s %s", name, this.plugin.getServer().getWorld(name).getEnvironment().toString()));
            } else if (this.worldsFromTheConfig.containsKey(name)) {
                return true; // it's already unloaded
            } else {
                this.plugin.log(Level.INFO, "Multiverse does not know about " + name + " and it's not loaded by Bukkit.");
            }
        } finally {
            worldsLock.unlock();
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean loadWorld(String name) {
        // Check if the World is already loaded
        Thread thread = Thread.currentThread();
        if (worldsLock.isLocked()) {
            plugin.log(Level.FINEST, "worldsLock is locked when attempting to access worlds on thread: " + thread);
        }
        worldsLock.lock();
        try {
            plugin.log(Level.FINEST, "Accessing worlds on thread: " + thread);
            if (this.worlds.containsKey(name)) {
                return true;
            }
        } finally {
            worldsLock.unlock();
        }

        // Check that the world is in the config
        if (worldsFromTheConfig.containsKey(name)) {
            return doLoad(name);
        } else {
            return false;
        }
    }

    private void brokenWorld(String name) {
        this.plugin.log(Level.SEVERE, "The world '" + name + "' could NOT be loaded because it contains errors!");
        this.plugin.log(Level.SEVERE, "Try using Chukster to repair your world! '" + name + "'");
        this.plugin.log(Level.SEVERE, "http://forums.bukkit.org/threads/admin-chunkster.8186/");
    }

    private boolean doLoad(String name) {
        return doLoad(name, false);
    }

    private boolean doLoad(String name, boolean ignoreExists) {
        if (!worldsFromTheConfig.containsKey(name))
            throw new IllegalArgumentException("That world doesn't exist!");

        MVWorld world = worldsFromTheConfig.get(name);
        WorldCreator creator = WorldCreator.name(name);

        creator.environment(world.getEnvironment()).seed(world.getSeed());
        if ((world.getGenerator() != null) && (!world.getGenerator().equals("null")))
            creator.generator(world.getGenerator());

        return doLoad(creator, ignoreExists);
    }

    private boolean doLoad(WorldCreator creator, boolean ignoreExists) {
        String worldName = creator.name();
        if (!worldsFromTheConfig.containsKey(worldName))
            throw new IllegalArgumentException("That world doesn't exist!");
        Thread thread = Thread.currentThread();
        if (worldsLock.isLocked()) {
            plugin.log(Level.FINEST, "worldsLock is locked when attempting to access worlds on thread: " + thread);
        }
        worldsLock.lock();
        try {
            plugin.log(Level.FINEST, "Accessing worlds on thread: " + thread);
            if (worlds.containsKey(worldName))
                throw new IllegalArgumentException("That world is already loaded!");
        } finally {
            worldsLock.unlock();
        }

        if (!ignoreExists && !new File(this.plugin.getServer().getWorldContainer(), worldName).exists()) {
            this.plugin.log(Level.WARNING, "WorldManager: Can't load this world because the folder was deleted/moved: " + worldName);
            this.plugin.log(Level.WARNING, "Use '/mv remove' to remove it from the config!");
            return false;
        }

        MVWorld mvworld = worldsFromTheConfig.get(worldName);
        World cbworld;
        try {
            cbworld = creator.createWorld();
        } catch (Exception e) {
            e.printStackTrace();
            brokenWorld(worldName);
            return false;
        }
        mvworld.init(cbworld, plugin);
        this.worldPurger.purgeWorld(mvworld);
        if (worldsLock.isLocked()) {
            plugin.log(Level.FINEST, "worldsLock is locked when attempting to access worlds on thread: " + thread);
        }
        worldsLock.lock();
        try {
            plugin.log(Level.FINEST, "Accessing worlds on thread: " + thread);
            this.worlds.put(worldName, mvworld);
        } finally {
            worldsLock.unlock();
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteWorld(String name, boolean removeFromConfig) {
        World world = this.plugin.getServer().getWorld(name);
        if (world == null) {
            // We can only delete loaded worlds
            return false;
        }

        // call the event!
        MVWorldDeleteEvent mvwde = new MVWorldDeleteEvent(getMVWorld(name), removeFromConfig);
        this.plugin.getServer().getPluginManager().callEvent(mvwde);
        if (mvwde.isCancelled()) {
            this.plugin.log(Level.FINE, "Tried to delete a world, but the event was cancelled!");
            return false;
        }

        if (removeFromConfig) {
            if (!removeWorldFromConfig(name)) {
                return false;
            }
        } else {
            if (!this.unloadWorld(name)) {
                return false;
            }
        }

        try {
            File worldFile = world.getWorldFolder();
            plugin.log(Level.FINER, "deleteWorld(): worldFile: " + worldFile.getAbsolutePath());
            boolean deletedWorld = FileUtils.deleteFolder(worldFile);
            if (deletedWorld) {
                this.plugin.log(Level.INFO, "World " + name + " was DELETED.");
            } else {
                this.plugin.log(Level.SEVERE, "World " + name + " was NOT deleted.");
                this.plugin.log(Level.SEVERE, "Are you sure the folder " + name + " exists?");
                this.plugin.log(Level.SEVERE, "Please check your file permissions on " + name);
            }
            return deletedWorld;
        } catch (Throwable e) {
            this.plugin.log(Level.SEVERE, "Hrm, something didn't go as planned. Here's an exception for ya.");
            this.plugin.log(Level.SEVERE, "You can go politely explain your situation in #multiverse on esper.net");
            this.plugin.log(Level.SEVERE, "But from here, it looks like your folder is oddly named.");
            this.plugin.log(Level.SEVERE, "This world has been removed from Multiverse-Core so your best bet is to go delete the folder by hand. Sorry.");
            this.plugin.log(Level.SEVERE, e.getMessage());
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteWorld(String name) {
        return this.deleteWorld(name, true);
    }

    /**
     * Unload a world from Bukkit.
     *
     * @param name   Name of the world to unload
     * @param safely Perform this safely. Set to True to save world files before unloading.
     * @return True if the world was unloaded, false if not.
     */
    private boolean unloadWorldFromBukkit(String name, boolean safely) {
        this.removePlayersFromWorld(name);
        return this.plugin.getServer().unloadWorld(name, safely);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removePlayersFromWorld(String name) {
        World w = this.plugin.getServer().getWorld(name);
        if (w != null) {
            World safeWorld = this.plugin.getServer().getWorlds().get(0);
            List<Player> ps = w.getPlayers();
            SafeTTeleporter teleporter = this.plugin.getSafeTTeleporter();
            for (Player p : ps) {
                // We're removing players forcefully from a world, they'd BETTER spawn safely.
                teleporter.safelyTeleport(null, p, safeWorld.getSpawnLocation(), true);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<MultiverseWorld> getMVWorlds() {
        Thread thread = Thread.currentThread();
        if (worldsLock.isLocked()) {
            plugin.log(Level.FINEST, "worldsLock is locked when attempting to access worlds on thread: " + thread);
        }
        worldsLock.lock();
        try {
            plugin.log(Level.FINEST, "Accessing worlds on thread: " + thread);
            return this.worlds.values();
        } finally {
            worldsLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MultiverseWorld getMVWorld(String name) {
        Thread thread = Thread.currentThread();
        if (worldsLock.isLocked()) {
            plugin.log(Level.FINEST, "worldsLock is locked when attempting to access worlds on thread: " + thread);
        }
        worldsLock.lock();
        try {
            plugin.log(Level.FINEST, "Accessing worlds on thread: " + thread);
            if (this.worlds.containsKey(name)) {
                return this.worlds.get(name);
            }
        } finally {
            worldsLock.unlock();
        }
        return this.getMVWorldByAlias(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MultiverseWorld getMVWorld(World world) {
        if (world != null) {
            return this.getMVWorld(world.getName());
        }
        return null;
    }

    /**
     * Returns a {@link MVWorld} if it exists, and null if it does not. This will search ONLY alias.
     *
     * @param alias The alias of the world to get.
     * @return A {@link MVWorld} or null.
     */
    private MultiverseWorld getMVWorldByAlias(String alias) {
        Thread thread = Thread.currentThread();
        if (worldsLock.isLocked()) {
            plugin.log(Level.FINEST, "worldsLock is locked when attempting to access worlds on thread: " + thread);
        }
        worldsLock.lock();
        try {
            plugin.log(Level.FINEST, "Accessing worlds on thread: " + thread);
            for (MultiverseWorld w : this.worlds.values()) {
                if (w.getAlias().equalsIgnoreCase(alias)) {
                    return w;
                }
            }
        } finally {
            worldsLock.unlock();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMVWorld(final String name) {
        Thread thread = Thread.currentThread();
        if (worldsLock.isLocked()) {
            plugin.log(Level.FINEST, "worldsLock is locked when attempting to access worlds on thread: " + thread);
        }
        worldsLock.lock();
        try {
            plugin.log(Level.FINEST, "Accessing worlds on thread: " + thread);
            return (this.worlds.containsKey(name) || isMVWorldAlias(name));
        } finally {
            worldsLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMVWorld(World world) {
        return world != null && this.isMVWorld(world.getName());
    }

    /**
     * This method ONLY checks the alias of each world.
     *
     * @param alias The alias of the world to check.
     * @return True if the world exists, false if not.
     */
    private boolean isMVWorldAlias(final String alias) {
        Thread thread = Thread.currentThread();
        if (worldsLock.isLocked()) {
            plugin.log(Level.FINEST, "worldsLock is locked when attempting to access worlds on thread: " + thread);
        }
        worldsLock.lock();
        try {
            plugin.log(Level.FINEST, "Accessing worlds on thread: " + thread);
            for (MultiverseWorld w : this.worlds.values()) {
                if (w.getAlias().equalsIgnoreCase(alias)) {
                    return true;
                }
            }
        } finally {
            worldsLock.unlock();
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadDefaultWorlds() {
        this.ensureConfigIsPrepared();
        List<World> myWorlds = this.plugin.getServer().getWorlds();
        for (World w : myWorlds) {
            String name = w.getName();
            if (!worldsFromTheConfig.containsKey(name)) {
                String generator = null;
                if (this.defaultGens.containsKey(name)) {
                    generator = this.defaultGens.get(name);
                }
                this.addWorld(name, w.getEnvironment(), String.valueOf(w.getSeed()), w.getWorldType(), w.canGenerateStructures(), generator);
            }
        }
    }

    private void ensureConfigIsPrepared() {
        this.configWorlds.options().pathSeparator(SEPARATOR);
        if (this.configWorlds.getConfigurationSection("worlds") == null) {
            this.configWorlds.createSection("worlds");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadWorlds(boolean forceLoad) {
        // Basic Counter to count how many Worlds we are loading.
        int count = 0;
        this.ensureConfigIsPrepared();
        this.ensureSecondNamespaceIsPrepared();

        // Force the worlds to be loaded, ie don't just load new worlds.
        if (forceLoad) {
            // Remove all world permissions.
            Permission allAccess = this.plugin.getServer().getPluginManager().getPermission("multiverse.access.*");
            Permission allExempt = this.plugin.getServer().getPluginManager().getPermission("multiverse.exempt.*");
            Thread thread = Thread.currentThread();
            if (worldsLock.isLocked()) {
                plugin.log(Level.FINEST, "worldsLock is locked when attempting to access worlds on thread: " + thread);
            }
            worldsLock.lock();
            try {
                plugin.log(Level.FINEST, "Accessing worlds on thread: " + thread);
                for (MultiverseWorld w : this.worlds.values()) {
                    // Remove this world from the master list
                    if (allAccess != null) {
                        allAccess.getChildren().remove(w.getAccessPermission().getName());
                    }
                    if (allExempt != null) {
                        allExempt.getChildren().remove(w.getAccessPermission().getName());
                    }
                    this.plugin.getServer().getPluginManager().removePermission(w.getAccessPermission().getName());
                    this.plugin.getServer().getPluginManager().removePermission(w.getExemptPermission().getName());
                    // Special namespace for gamemodes
                    this.plugin.getServer().getPluginManager().removePermission("mv.bypass.gamemode." + w.getName());
                }
                // Recalc the all permission
                this.plugin.getServer().getPluginManager().recalculatePermissionDefaults(allAccess);
                this.plugin.getServer().getPluginManager().recalculatePermissionDefaults(allExempt);
                this.worlds.clear();
            } finally {
                worldsLock.unlock();
            }
        }

        for (Map.Entry<String, MVWorld> entry : worldsFromTheConfig.entrySet()) {
            Thread thread = Thread.currentThread();
            if (worldsLock.isLocked()) {
                plugin.log(Level.FINEST, "worldsLock is locked when attempting to access worlds on thread: " + thread);
            }
            worldsLock.lock();
            try {
                plugin.log(Level.FINEST, "Accessing worlds on thread: " + thread);
                if (worlds.containsKey(entry.getKey()))
                    continue;
            } finally {
                worldsLock.unlock();
            }
            if (!entry.getValue().getAutoLoad())
                continue;

            if (doLoad(entry.getKey()))
                count++;
        }

        // Simple Output to the Console to show how many Worlds were loaded.
        this.plugin.log(Level.INFO, count + " - World(s) loaded.");
        this.saveWorldsConfig();
    }

    private void ensureSecondNamespaceIsPrepared() {
        Permission special = this.plugin.getServer().getPluginManager().getPermission("mv.bypass.gamemode.*");
        if (special == null) {
            special = new Permission("mv.bypass.gamemode.*", PermissionDefault.FALSE);
            this.plugin.getServer().getPluginManager().addPermission(special);
        }
    }

    /**
     * {@inheritDoc}
     * @deprecated This is deprecated!
     */
    @Override
    @Deprecated
    public PurgeWorlds getWorldPurger() {
        return new PurgeWorlds(plugin);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorldPurger getTheWorldPurger() {
        return worldPurger;
    }

    private static final char SEPARATOR = '\uF8FF';

    /**
     * {@inheritDoc}
     */
    @Override
    public FileConfiguration loadWorldConfig(File file) {
        this.configWorlds = YamlConfiguration.loadConfiguration(file);
        this.ensureConfigIsPrepared();
        try {
            this.configWorlds.save(new File(this.plugin.getDataFolder(), "worlds.yml"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // load world-objects
        Stack<String> worldKeys = new Stack<String>();
        worldKeys.addAll(this.configWorlds.getConfigurationSection("worlds").getKeys(false));
        Map<String, MVWorld> newWorldsFromTheConfig = new HashMap<String, MVWorld>();
        while (!worldKeys.isEmpty()) {
            String key = worldKeys.pop();
            String path = "worlds" + SEPARATOR + key;
            Object obj = this.configWorlds.get(path);
            if ((obj != null) && (obj instanceof MVWorld)) {
                String worldName = key.replaceAll(String.valueOf(SEPARATOR), ".");
                if (this.worldsFromTheConfig.containsKey(worldName)) {
                    // Object-Recycling :D
                    Thread thread = Thread.currentThread();
                    if (worldsLock.isLocked()) {
                        plugin.log(Level.FINEST, "worldsLock is locked when attempting to access worlds on thread: " + thread);
                    }
                    worldsLock.lock();
                    try {
                        plugin.log(Level.FINEST, "Accessing worlds on thread: " + thread);
                        MVWorld oldMVWorld = (MVWorld) this.worlds.get(worldName);
                        oldMVWorld.copyValues((MVWorld) obj);
                        newWorldsFromTheConfig.put(worldName, oldMVWorld);
                    } finally {
                        worldsLock.unlock();
                    }
                } else {
                    // we have to use a new one
                    World cbworld = this.plugin.getServer().getWorld(worldName);
                    MVWorld mvworld = (MVWorld) obj;
                    if (cbworld != null)
                        mvworld.init(cbworld, this.plugin);
                    newWorldsFromTheConfig.put(worldName, mvworld);
                }
            } else if (this.configWorlds.isConfigurationSection(path)) {
                ConfigurationSection section = this.configWorlds.getConfigurationSection(path);
                Set<String> subkeys = section.getKeys(false);
                for (String subkey : subkeys) {
                    worldKeys.push(key + SEPARATOR + subkey);
                }
            }
        }
        this.worldsFromTheConfig = newWorldsFromTheConfig;
        Thread thread = Thread.currentThread();
        if (worldsLock.isLocked()) {
            plugin.log(Level.FINEST, "worldsLock is locked when attempting to access worlds on thread: " + thread);
        }
        worldsLock.lock();
        try {
            plugin.log(Level.FINEST, "Accessing worlds on thread: " + thread);
            this.worlds.keySet().retainAll(this.worldsFromTheConfig.keySet());
        } finally {
            worldsLock.unlock();
        }
        return this.configWorlds;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean saveWorldsConfig() {
        try {
            this.configWorlds.options().pathSeparator(SEPARATOR);
            this.configWorlds.set("worlds", null);
            for (Map.Entry<String, ? extends MultiverseWorld> entry : worldsFromTheConfig.entrySet()) {
                this.configWorlds.set("worlds" + SEPARATOR + entry.getKey(), entry.getValue());
            }
            this.configWorlds.save(new File(this.plugin.getDataFolder(), "worlds.yml"));
            return true;
        } catch (IOException e) {
            this.plugin.log(Level.SEVERE, "Could not save worlds.yml. Please check your settings.");
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MultiverseWorld getSpawnWorld() {
        return this.getMVWorld(this.plugin.getServer().getWorlds().get(0));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getUnloadedWorlds() {
        List<String> allNames = new ArrayList<String>(this.worldsFromTheConfig.keySet());
        Thread thread = Thread.currentThread();
        if (worldsLock.isLocked()) {
            plugin.log(Level.FINEST, "worldsLock is locked when attempting to access worlds on thread: " + thread);
        }
        worldsLock.lock();
        try {
            plugin.log(Level.FINEST, "Accessing worlds on thread: " + thread);
            allNames.removeAll(worlds.keySet());
        } finally {
            worldsLock.unlock();
        }
        return allNames;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean regenWorld(String name, boolean useNewSeed, boolean randomSeed, String seed) {
        MultiverseWorld world = this.getMVWorld(name);
        if (world == null)
            return false;

        List<Player> ps = world.getCBWorld().getPlayers();

        if (useNewSeed) {
            long theSeed;

            if (randomSeed) {
                theSeed = new Random().nextLong();
            } else {
                try {
                    theSeed = Long.parseLong(seed);
                } catch (NumberFormatException e) {
                    theSeed = seed.hashCode();
                }
            }

            world.setSeed(theSeed);
        }

        if (this.deleteWorld(name, false)) {
            this.doLoad(name, true);
            SafeTTeleporter teleporter = this.plugin.getSafeTTeleporter();
            Location newSpawn = world.getSpawnLocation();
            // Send all players that were in the old world, BACK to it!
            for (Player p : ps) {
                teleporter.safelyTeleport(null, p, newSpawn, true);
            }
            return true;
        }
        return false;
    }

    /**
     * Gets the {@link FileConfiguration} that this {@link WorldManager} is using.
     * @return The {@link FileConfiguration} that this {@link WorldManager} is using.
     */
    public FileConfiguration getConfigWorlds() {
        return this.configWorlds;
    }
}

