package com.n9works.bukkit.commands;

import com.n9works.bukkit.Account;
import com.n9works.bukkit.ConfigFile;
import com.n9works.bukkit.Portal;
import com.n9works.bukkit.TheArtifact;
import com.n9works.bukkit.towns.Alliance;
import com.n9works.bukkit.towns.Embassy;
import com.n9works.bukkit.towns.Generator;
import com.n9works.bukkit.towns.Zone;
import com.n9works.bukkit.towns.mastery.MasteryPerk;
import com.n9works.bukkit.utils.StringLocation;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.regions.CuboidRegion;
import mkremins.fanciful.FancyMessage;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static com.n9works.bukkit.utils.LocationUtil.coordsToString;
import static com.n9works.bukkit.utils.LocationUtil.prettyCoords;
import static org.bukkit.ChatColor.*;

public class CommandGenerator implements CommandExecutor {

    private final Logger log = Logger.getLogger("Command-Generic");
    private final TheArtifact plugin;

    //Confirmations
    private final Map<String, String> genTransferConfirm = new HashMap<>();
    private final Map<String, String> genDeleteConfirm = new HashMap<>();
    private final Map<String, Zone> protoZones = new HashMap<>();
    private final Map<String, Embassy> protoEmbassies = new HashMap<>();
    private final Map<String, String> pendingAllianceInvites = new HashMap<>();

    //Other
    private final Server server;
    private final WorldEditPlugin worldEdit;

    public CommandGenerator(TheArtifact plugin) {
        this.plugin = plugin;

        worldEdit = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
        server = plugin.server;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player)) return false;

        final Player player = (Player) commandSender;

        if (args.length < 1) {
            player.sendMessage(AQUA + "Type " + GOLD + "/g help" +
                    AQUA + " to get more help on generators.");
            return true;
        } else if (args[0].equals("setportal")) {
            if (args.length < 1) {
                player.sendMessage(GRAY + "Sets up a town portal. Use a golden axe to define the" +
                        " bounds of your portal. Then, stand in the position you want the return point to be at and use the command.");
                player.sendMessage(AQUA + "Usage: " +
                        YELLOW + "/g setportal" +
                        AQUA + ".");
                return true;
            } else {
                Selection sel = worldEdit.getSelection(player);
                Generator g = plugin.getAccount(player.getName()).getHomeTown();
                if (g == null) {
                    player.sendMessage(AQUA
                            + "You need to set your town with " + YELLOW + "/town" +
                            AQUA + " first.");
                } else if (!plugin.isTownOwner(player.getName())) {
                    player.sendMessage(AQUA
                            + "You must be a town owner to set the town portal.");
                    return true;
                } else if (sel == null) {
                    player.sendMessage(AQUA + "No selection made");
                } else {
                    Portal portal = plugin.getPortal(g.name);
                    Location playerLocation = player.getLocation();
                    BlockVector min = sel.getNativeMinimumPoint().toBlockVector();
                    BlockVector max = sel.getNativeMaximumPoint().toBlockVector();
                    if ((!g.withinProtection(min)) || (!g.withinProtection(max))) {
                        player.sendMessage(AQUA + "Your portal must be within your generator's protection.");
                    } else if (!g.withinProtection(playerLocation)) {
                        player.sendMessage(AQUA + "Your arrival point must be within your generator's protection.");
                    } else if ((playerLocation.distance(sel.getMaximumPoint()) > 10) ||
                            (playerLocation.distance(sel.getMinimumPoint()) > 10)) {
                        player.sendMessage(AQUA + "Your arrival point must be within 10 blocks of your portal");
                    } else if (sel.getArea() > 27) {
                        player.sendMessage(AQUA + "Your portal dimensions are too large. The dimensions cannot cover more than 27 blocks.");
                    } else if (sel.contains(playerLocation)) {
                        player.sendMessage(AQUA + "Your return block cannot be inside of your portal bounds. This would create a black hole.");
                    } else {
                        portal.region = new CuboidRegion(min, max);
                        portal.arrivalPoint = playerLocation;
                        portal.singleDestination = "lokapyramid";
                        portal.world = playerLocation.getWorld();
                        portal.facingDirection = (int) playerLocation.getYaw();
                        player.sendMessage(AQUA + "Portal dimensions set. Your arrival point is where you currently stand and face.");
                        File saveTo = new File(plugin.getDataFolder(), "portals/" + g.homePortal.name + ".yml");
                        Boolean firstPortal = !saveTo.exists();
                        portal.save();

                        if (firstPortal) {
                            g.homePortal = portal;
                            g.save(false);
                            plugin.loadPortal(saveTo);
                            log.info(player.getName() + " defined first time portal for " + portal.name);
                        }
                        portal.portalCreatedEffect(sel.getMaximumPoint(), sel.getMinimumPoint());
                        portal.world.playSound(sel.getMinimumPoint(), Sound.FIZZ, 1, .1f);
                        log.info(player.getName() + " defined a portal for " + g.name + " at " + coordsToString(portal.arrivalPoint));
                    }
                }
            }
            return true;
        } else if (args[0].equals("warp")) {
            if (plugin.isAdmin(player)) {
                if (args.length < 2) {
                    player.sendMessage(GRAY + "Warp to a town's portal.");
                    player.sendMessage(AQUA + "Usage: " +
                            YELLOW + "/g warp <townname>" +
                            AQUA + ".");
                    return true;
                } else {
                    Generator g = plugin.findGenerator(args[1], player.getName());
                    if (g != null) {
                        Portal portal = plugin.getPortal(g.name);
                        if (portal == null || portal.arrivalPoint == null) {
                            player.sendMessage(GOLD + g.name + AQUA +
                                    " has no town portal defined.");
                        } else {
                            player.teleport(portal.arrivalPoint);
                        }
                    } else {
                        player.sendMessage(AQUA + "No such generator");
                    }
                }
            }
            return true;
        } else if (args[0].equals("addmember") || args[0].equals("add")) {
            if (args.length < 2) {
                player.sendMessage(GRAY + "Adds the player as a member of the town. This grants them build rights, access to the town portal, and use of town chat.");
                player.sendMessage(AQUA + "Usage: " +
                        YELLOW + "/g add <player>" +
                        AQUA + ".");
                return true;
            } else {
                Generator gen = plugin.getAccount(player.getName()).getHomeTown();

                //could be a town in progress? Try to find it
                if (gen == null) {
                    gen = plugin.townInProgress(player.getName());
                }

                if (gen == null) {
                    player.sendMessage(AQUA
                            + "You need to set your town with " + YELLOW + "/town" +
                            AQUA + " first.");
                } else if (!gen.owner.equals(player.getName())
                        && !gen.subowners.contains(player.getName())
                        && !plugin.inGroup(player.getName(), "Elder")) {
                    player.sendMessage(AQUA + "Only owners or subowners may add members.");
                } else {
                    for (int i = 1; i < args.length; i++) {
                        OfflinePlayer p = server.getOfflinePlayer(
                                args[i]);
                        if (p == null || p.getFirstPlayed() == 0) {
                            player.sendMessage(AQUA
                                    + "No such player " + GREEN
                                    + args[i]);
                        } else if (plugin.getAccount(p).town != null) {
                            player.sendMessage(GREEN + p.getName()
                                    + AQUA + " is already a member of a town.");
                        } else {
                            gen.members.add(p.getName());
                            if (!gen.finished) {
                                player.sendMessage(GREEN + p.getName()
                                        + AQUA + " has been added as a founder.");
                                gen.founders.put(p.getName(), false);
                                plugin.tryPlayerNotification(p, GREEN +
                                        player.getName() + AQUA + " has invited you to found a new town, " +
                                        GOLD + gen.name);
                                plugin.tryPlayerNotification(p, AQUA +
                                        "To accept, you must " + GOLD + "touch the town generator's " +
                                        "diamond block");
                            } else {
                                player.sendMessage(GREEN + p.getName()
                                        + AQUA + " is now a member of " +
                                        GOLD + gen.name + AQUA + ".");
                                Player newMember = server.getPlayer(p.getName());
                                if ((newMember != null) && (newMember.isOnline())) {
                                    newMember.sendMessage(GREEN + player.getName() +
                                            AQUA + " has added you to their town, " +
                                            GOLD + gen.name + AQUA +
                                            ".");
                                    newMember.sendMessage(AQUA + "You may set " +
                                            GOLD + gen.name + AQUA +
                                            " as your home by typing " + YELLOW +
                                            "/town " + gen.name + AQUA +
                                            " when you are within its borders.");
                                }
                            }
                        }
                    }

                    gen.save(true);
                }
            }
            return true;
        } else if (args[0].equals("delmember") || args[0].equals("del")) {
            if (!player.hasPermission("artifact.generator.delmember")) {
                player.sendMessage(AQUA
                        + "You don't have permission to manipulate generators.");
            } else if (args.length < 2) {
                player.sendMessage(GRAY + "Removes the player as a member of the town. They will no longer have access to any town functions.");
                player.sendMessage(AQUA + "Usage: " +
                        YELLOW + "/g del <player>" +
                        AQUA + ".");
                return true;
            } else {
                Generator gen = plugin.getAccount(player.getName()).getHomeTown();
                if (gen == null) {
                    player.sendMessage(AQUA
                            + "You need to set your town with " + YELLOW + "/town" +
                            AQUA + " first.");
                } else if (!gen.owner.equals(player.getName())
                        && !gen.subowners.contains(player.getName())
                        && !plugin.inGroup(player.getName(), "Elder")) {
                    player.sendMessage(AQUA
                            + "Only owners or subowners may remove members.");
                } else {
                    for (int i = 1; i < args.length; i++) {
                        OfflinePlayer p = server.getOfflinePlayer(
                                args[i]);
                        if ((p == null) || (p.getFirstPlayed() == 0)) {
                            player.sendMessage(AQUA
                                    + "No such player " + GREEN
                                    + args[i]);
                        } else {
                            if (gen.subowners.contains(p.getName()) && !gen.owner.equals(player.getName())) {
                                //If we're trying to delete a subowner and we're NOT the owner
                                player.sendMessage(AQUA + "Only town owners can modify subowners.");

                                return true;
                            } else if (gen.owner.equals(p.getName()) && gen.owner.equals(player.getName())) {
                                //If the owner tries /delmember on himself
                                player.sendMessage(AQUA
                                        + "You cannot remove yourself from the town as an owner. You would need to " +
                                        YELLOW + "/g transfer" + AQUA + " first.");

                                return true;
                            }

                            gen.members.remove(p.getName());
                            gen.config.set("members." + p.getName(), null);
                            if (gen.subowners.contains(p.getName())) {
                                gen.subowners.remove(p.getName());
                                gen.config.set("subowners." + p.getName(), null);
                                player.sendMessage(GREEN
                                        + p.getName() + AQUA
                                        + " no longer an owner of " +
                                        GOLD + gen.name +
                                        AQUA + ".");
                            }

                            Account a = plugin.getAccount(p);
                            if (a.town != null && a.getHomeTown().equals(gen)) {
                                a.town = null;
                                a.save();
                                if (plugin.inGroup(p.getName(), "Settler")) {
                                    if (p.getFirstPlayed() > 1000
                                            && (System.currentTimeMillis() - p.getFirstPlayed() > (TheArtifact.TWO_WEEKS))) {
                                        // Demote to Nomad
                                        plugin.promoteTo(p.getName(), "Nomad",
                                                "You have returned to the Nomadic lifestyle.", false);
                                        plugin.achievements.checkSpecificAchievement(player.getName(), "general", "learntip");
                                    } else {
                                        //Demote to Wanderer
                                        plugin.promoteTo(p.getName(), "Wanderer",
                                                "You have returned to the Wandering lifestyle.", false);
                                    }
                                }
                            }

                            player.sendMessage(GREEN
                                    + p.getName() + AQUA
                                    + " no longer a member of " +
                                    GOLD + gen.name +
                                    AQUA + ".");
                            Player removedPlayer = server.getPlayer(p.getName());
                            if (removedPlayer != null && removedPlayer.isOnline()) {
                                removedPlayer.sendMessage(GREEN + player.getName() +
                                        AQUA + " has removed you from " +
                                        GOLD + gen.name + AQUA +
                                        ".");
                            }
                        }
                    }

                    gen.save(true);
                }
            }
            return true;
        } else if (args[0].equals("addowner")) {
            if (!player.hasPermission("artifact.generator.addowner")) {
                player.sendMessage(AQUA
                        + "You don't have permission to manipulate generators.");
            } else if (args.length < 2) {
                player.sendMessage(GRAY + "Adds a player as an owner of the town with all permissions except adding/removing other owners.");
                player.sendMessage(AQUA + "Usage: " +
                        YELLOW + "/g addowner <player>" +
                        AQUA + ".");
                return true;
            } else {
                Generator gen = plugin.getAccount(player.getName()).getHomeTown();
                if (gen == null) {
                    player.sendMessage(AQUA
                            + "You need to set your town with " + YELLOW + "/town" +
                            AQUA + " first.");
                } else if (!gen.owner.equals(player.getName())) {
                    player.sendMessage(AQUA + "Only owners may add subowners.");
                } else {
                    for (int i = 1; i < args.length; i++) {
                        OfflinePlayer p = server.getOfflinePlayer(
                                args[i]);
                        if ((p == null) || (p.getFirstPlayed() == 0)) {
                            player.sendMessage(AQUA
                                    + "No such player " + GREEN
                                    + args[i]);
                        } else {
                            gen.subowners.add(p.getName());
                            gen.members.add(p.getName());
                            gen.memberLevels.put(p.getName(), 5);
                            plugin.spawnPlots.updatePlot(gen);
                            player.sendMessage(GREEN + p.getName()
                                    + AQUA + " is now an owner of " +
                                    GOLD + gen.name + AQUA + ".");
                            Player newMember = server.getPlayer(p.getName());
                            if ((newMember != null) && (newMember.isOnline())) {
                                newMember.sendMessage(GREEN + player.getName() +
                                        AQUA + " has added you as an owner to " +
                                        GOLD + gen.name + AQUA +
                                        ".");
                            }
                        }
                    }

                    gen.save(true);
                }
            }
            return true;
        } else if (args[0].equals("delowner")) {
            if (!player.hasPermission("artifact.generator.delowner")) {
                player.sendMessage(AQUA
                        + "You don't have permission to manipulate generators.");
            } else if (args.length < 2) {
                player.sendMessage(GRAY + "Removes the player as an owner of the town. The player will remain as a normal town member.");
                player.sendMessage(AQUA + "Usage: " +
                        YELLOW + "/g delowner <player>" +
                        AQUA + ".");
                return true;
            } else {
                Generator gen = plugin.getAccount(player.getName()).getHomeTown();
                if (gen == null) {
                    player.sendMessage(AQUA
                            + "You need to set your town with " + YELLOW + "/town" +
                            AQUA + " first.");
                } else if (!gen.owner.equals(player.getName())) {
                    player.sendMessage(AQUA
                            + "Only owners may remove subowners.");
                } else {
                    for (int i = 1; i < args.length; i++) {
                        OfflinePlayer p = server.getOfflinePlayer(
                                args[i]);
                        if (!gen.subowners.contains(p.getName())) {
                            player.sendMessage(GREEN + p.getName() + AQUA + " is not an owner.");
                            return true;
                        } else {
                            gen.subowners.remove(p.getName());
                            gen.memberLevels.put(p.getName(), gen.maxAvailableMemberLevel());
                            gen.config.set("subowners." + args[i], null);
                            plugin.spawnPlots.updatePlot(gen);
                            player.sendMessage(GREEN
                                    + args[i] + AQUA
                                    + " is no longer an owner.");
                            Player removedPlayer = server.getPlayer(args[i]);
                            if (removedPlayer != null && removedPlayer.isOnline()) {
                                removedPlayer.sendMessage(GREEN + player.getName() +
                                        AQUA + " has removed you as an owner of " +
                                        GOLD + gen.name + AQUA +
                                        ".");
                            }

                        }
                    }

                    gen.save(true);
                }
            }
            return true;
        } else if (args[0].equals("transfer")) {
            if (!player.hasPermission("artifact.generator.delowner")) {
                player.sendMessage(AQUA
                        + "You don't have permission to manipulate generators.");
            } else if (args.length < 2) {
                player.sendMessage(GRAY + "Transfers complete ownership of generator to player and demotes you to subowner.");
                player.sendMessage(AQUA + "Usage: " +
                        YELLOW + "/g transfer <player>" +
                        AQUA + ".");
                return true;
            } else {
                Generator gen = plugin.getAccount(player.getName()).getHomeTown();
                if (gen == null) {
                    player.sendMessage(AQUA
                            + "You need to set your town with " + YELLOW + "/town" +
                            AQUA + " first.");
                } else if (!gen.owner.equals(player.getName())) {
                    player.sendMessage(AQUA
                            + "Only owners may transfer ownership.");
                } else {
                    OfflinePlayer p = server.getOfflinePlayer(
                            args[1]);
                    if ((p == null) || (p.getFirstPlayed() == 0)) {
                        player.sendMessage(AQUA
                                + "No such player " + GREEN
                                + args[1]);
                    } else if (!p.isOnline()) {
                        player.sendMessage(GREEN + p.getName() + AQUA +
                                " must be online to successfully transfer ownership.");
                    } else if (!gen.members.contains(p.getName())) {
                        player.sendMessage(GREEN + p.getName() + AQUA +
                                " must be a member of " + GOLD + gen.name +
                                AQUA + " in order to be transferred ownership.");
                    } else if (plugin.isTownOwner(p.getName())) {
                        player.sendMessage(GREEN + p.getName() + AQUA +
                                " already owns a town.");
                    } else {
                        if (!genTransferConfirm.containsKey(player.getName())) {
                            player.sendMessage(AQUA +
                                    "Are you sure you wish to transfer ownership of " + GOLD +
                                    gen.name + AQUA + " to " +
                                    GREEN + args[1] + AQUA + "?");
                            player.sendMessage(AQUA +
                                    "This is irreversible. Repeat this command if you are sure.");
                            genTransferConfirm.put(player.getName(), player.getName());
                        } else {
                            gen.owner = p.getName();
                            gen.subowners.add(player.getName());
                            gen.memberLevels.put(p.getName(), 5);
                            if (gen.subowners.contains(p.getName())) {
                                gen.config.set("subowners." + p.getName(), null);
                                gen.subowners.remove(p.getName());
                            }
                            player.sendMessage(AQUA + "Successfully transferred ownership of " +
                                    GOLD + gen.name + AQUA + " to " + GREEN
                                    + p.getName() + AQUA
                                    + ".");
                            Player newOwner = server.getPlayer(p.getName());
                            if ((newOwner != null) && (newOwner.isOnline())) {
                                newOwner.sendMessage(GREEN + player.getName() +
                                        AQUA + " has transferred ownership of " +
                                        GOLD + gen.name + AQUA +
                                        " to you.");
                            }
                            genTransferConfirm.remove(player.getName());
                            gen.save(true);
                        }
                    }
                }
            }
            return true;
        } else if (args[0].equals("delete")) {
            if (args.length < 1) {
                player.sendMessage(GRAY + "Completely deletes the generator. This removes all town members from the " +
                        "town and is not reversible!");
                player.sendMessage(AQUA + "Usage: " +
                        YELLOW + "/g delete" +
                        AQUA + ".");
                return true;
            } else {
                Generator gen = plugin.getAccount(player.getName()).getHomeTown();
                if (gen == null || gen.finished) {
                    player.sendMessage(AQUA
                            + "You need to set your town with " + YELLOW + "/town" +
                            AQUA + " first.");
                } else if (!gen.owner.equals(player.getName())) {
                    player.sendMessage(AQUA
                            + "Only owners may delete their generators.");
                } else {
                    if (!genDeleteConfirm.containsKey(player.getName())) {
                        player.sendMessage(AQUA +
                                "Are you sure you wish to completely delete " + GOLD +
                                gen.name + AQUA + "?");
                        player.sendMessage(RED +
                                "This cannot be undone! " + AQUA +
                                "Repeat this command if you are sure.");
                        genDeleteConfirm.put(player.getName(), player.getName());
                    } else {
                        for (String p : gen.allowedPlayers) {
                            Account a = plugin.getAccount(p);
                            Player townPlayer = server.getPlayerExact(p);
                            if (townPlayer != null) {
                                townPlayer.sendMessage(GOLD + player.getName() + AQUA +
                                        " has deleted " + GOLD + gen.name + AQUA +
                                        "!");
                            }
                            if ((townPlayer != null) && (a.getHomeTown() == gen)) {
                                townPlayer.sendMessage(AQUA +
                                        "Your home town has been removed.");

                            }
                            a.town = null;
                            a.save();
                        }
                        player.sendMessage(GOLD + gen.name + AQUA
                                + " has been deleted.");
                        gen.delete(false);
                        genDeleteConfirm.remove(player.getName());

                    }
                }
            }
            return true;
        } else if (args[0].equals("tag")) {
            if (args.length < 2) {
                player.sendMessage(GRAY + "Sets the town prefix in town chat. Useful for towns with long names.");
                player.sendMessage(AQUA + "Usage: " +
                        YELLOW + "/g tag <tag>" +
                        AQUA + ".");
                return true;
            } else {
                Generator gen = plugin.getAccount(player.getName()).getHomeTown();
                if (gen == null) {
                    player.sendMessage(AQUA
                            + "You need to set your town with " + YELLOW + "/town" +
                            AQUA + " first.");
                } else if (!gen.owner.equals(player.getName())) {
                    player.sendMessage(AQUA
                            + "Only owners change the town tag.");
                } else {
                    gen.tag = args[1];
                    gen.save(false);
                    player.sendMessage(AQUA + "Tag for " +
                            GOLD + gen.name + AQUA +
                            " is now " + GOLD + args[1] +
                            AQUA + ".");
                }
            }
            return true;
        } else if (args[0].equals("private")) {
            if (args.length < 2) {
                player.sendMessage(GRAY + "Sets the town as private. This prevents discovery of your town through " +
                        YELLOW + "/g info" + GRAY + " as well as using the feather tool.");
                player.sendMessage(AQUA + "Usage: " +
                        YELLOW + "/g private <on/off>" +
                        AQUA + ".");
                return true;
            } else {
                Generator gen = plugin.getAccount(player.getName()).getHomeTown();
                if (gen == null) {
                    player.sendMessage(AQUA
                            + "You need to set your town with " + YELLOW + "/town" +
                            AQUA + " first.");
                } else if (!gen.owner.equals(player.getName())) {
                    player.sendMessage(AQUA
                            + "Only owners may set the town as private.");
                } else {
                    if (args[1].equalsIgnoreCase("on")) {
                        gen.isPrivate = true;
                        gen.save(false);
                        player.sendMessage(GOLD + gen.name + AQUA +
                                " is now a private town.");
                    } else if (args[1].equalsIgnoreCase("off")) {
                        gen.isPrivate = false;
                        gen.save(false);
                        player.sendMessage(GOLD + gen.name + AQUA +
                                " is now a public town.");
                    } else {
                        player.sendMessage(AQUA + "Usage: " +
                                YELLOW + "/g private <on/off>" +
                                AQUA + ".");
                    }
                }
            }
            return true;
        } else if (args[0].equals("mobspawning")) {
            if (args.length < 2) {
                player.sendMessage(GRAY + "Toggle the spawning of monsters in your town. This does not " +
                        "affect monster spawners.");
                player.sendMessage(AQUA + "Usage: " +
                        YELLOW + "/g mobspawning <on/off>" +
                        AQUA + ".");
                return true;
            } else {
                Generator gen = plugin.getAccount(player.getName()).getHomeTown();
                if (gen == null) {
                    player.sendMessage(AQUA
                            + "You need to set your town with " + YELLOW + "/town" +
                            AQUA + " first.");
                } else if (!gen.owner.equals(player.getName())) {
                    player.sendMessage(AQUA
                            + "Only owners may toggle mob spawning.");
                } else if (gen.townLevel < MasteryPerk.toInt(MasteryPerk.MOB_SPAWNING)) {
                    player.sendMessage(AQUA
                            + "Toggling Mob Spawning is available at town level " +
                            MasteryPerk.toInt(MasteryPerk.MOB_SPAWNING) + ".");
                } else {
                    if (args[1].equalsIgnoreCase("on")) {
                        gen.mobSpawning = true;
                        gen.save(false);
                        player.sendMessage(AQUA + "Mob spawning is now " + GREEN + "on" + AQUA + " in " + GOLD +
                                gen.name + AQUA + ".");
                    } else if (args[1].equalsIgnoreCase("off")) {
                        gen.mobSpawning = false;
                        gen.save(false);
                        player.sendMessage(AQUA + "Mob spawning is now " + RED + "off" + AQUA + " in " + GOLD +
                                gen.name + AQUA + ".");
                    } else {
                        player.sendMessage(AQUA + "Usage: " +
                                YELLOW + "/g mobspawning <on/off>" +
                                AQUA + ".");
                    }
                }
            }
            return true;
        } else if (args[0].equals("motd")) {
            if (args.length < 2) {
                player.sendMessage(GRAY + "Sets a town's message of the day. This is displayed for " +
                        "all town members on login and whenever changed.");
                player.sendMessage(AQUA + "Usage: " +
                        YELLOW + "/g motd <motd>" +
                        AQUA + ".");
                return true;
            } else if (args.length == 2) {
                Generator gen = plugin.getAccount(player.getName()).getHomeTown();
                if (gen == null) {
                    player.sendMessage(AQUA
                            + "You need to set your town with " + YELLOW + "/town" +
                            AQUA + " first.");
                    return true;
                }
                player.sendMessage(GRAY + "[" + GOLD + gen.name +
                        GRAY + "] " + GRAY + gen.motd);
                return true;
            } else {
                Generator gen = plugin.getAccount(player.getName()).getHomeTown();
                if (gen == null) {
                    player.sendMessage(AQUA
                            + "You need to set your town with " + YELLOW + "/town" +
                            AQUA + " first.");
                } else if ((!gen.owner.equals(player.getName())) &&
                        (!gen.subowners.contains(player.getName()))) {
                    player.sendMessage(AQUA
                            + "Only owners or subowners may change the motd.");
                } else {
                    StringBuilder motd = new StringBuilder();
                    for (int i = 1; i < args.length; i++) {
                        motd.append(" ").append(args[i]);
                    }
                    gen.motd = motd.toString().substring(1, motd.toString().length());

                    gen.save(true);
                    player.sendMessage(AQUA + "Motd for " +
                            GOLD + gen.name + AQUA +
                            " set.");
                    gen.messageMembers(AQUA + "Message of the Day for " +
                            GOLD + gen.name + AQUA +
                            " was changed: ");
                    gen.messageMembers(GRAY + "[" + GOLD + gen.name +
                            GRAY + "] " + GRAY + gen.motd);
                }
            }
            return true;
        } else if (args[0].equals("info")) {
            if (!player.hasPermission("artifact.generator.info")) {
                player.sendMessage(AQUA
                        + "You don't have permission to manipulate generators.");
            } else if (args.length < 2 && plugin.isAdmin(player)) {
                player.sendMessage(GRAY + "Returns information on a town generator.");
                player.sendMessage(AQUA + "Usage: " +
                        YELLOW + "/g info <townname>" +
                        AQUA + ".");
                return true;
            } else {
                Generator gen;
                if (args.length < 2) {
                    gen = plugin.getHomeTown(player.getName());
                } else {
                    gen = plugin.findGenerator(args[1], player.getName());
                }
                if (gen == null) {
                    player.sendMessage(AQUA + "You are not in a town.");
                } else {
                    if ((gen.allowedPlayers.contains(player.getName())) ||
                            (plugin.isAdmin(player))) {
                        String genNumerics = AQUA + "Generator: "
                                + GOLD + gen.name + AQUA + " (" + GOLD + (gen.getRadius() - 1) + "m" + AQUA + ")";
                        if ((gen.owner.equals(player.getName()) ||
                                (gen.subowners.contains(player.getName())) ||
                                (plugin.isAdmin(player)))) {
                            genNumerics += AQUA + " | Balance: " +
                                    GOLD + (int) gen.balance;
                            player.sendMessage(genNumerics);

                            float cost = gen.getCost(gen.getRadius());
                            StringBuilder b = new StringBuilder(AQUA + "Cost: " +
                                    WHITE + (int) cost);
                            if (gen.netherProtection) {
                                b.append(" (+");
                                b.append((int) (cost * 0.20));
                                b.append(" for nether)");
                            }
                            player.sendMessage(b.toString());
                        } else {
                            player.sendMessage(genNumerics);
                        }

                        player.sendMessage(AQUA + "Level: " + GREEN + gen.townLevel);

                        player.sendMessage(AQUA + "Owner: " + GREEN + gen.owner);

                        if (gen.subowners.size() > 0) {
                            StringBuilder c = new StringBuilder(AQUA
                                    + "Subowners: " + GREEN);
                            for (String p : gen.subowners) {
                                c.append(p).append(WHITE).append(", ").append(GREEN);
                            }
                            player.sendMessage(c.substring(0, c.length() - 4)); // Strip off the last comma
                        }

                        player.sendMessage(AQUA + "Members: " + GREEN + gen.members.size());

                        if (gen.finished) {
                            new FancyMessage("Town Website: ")
                                    .color(AQUA)
                                    .then("[Click to visit]")
                                    .color(GOLD)
                                    .style(BOLD)
                                    .tooltip("Click to visit the " + gen.name + " town website.")
                                    .link("http://loka.minecraftarium.com/town/" + gen.name)
                                    .send(player);
                        }
                    } else {
                        if (gen.isPrivate && !gen.allowedPlayers.contains(player.getName())) {
                            player.sendMessage(AQUA + "There is no town by that name.");
                            return true;
                        }
                        player.sendMessage(AQUA + "Generator: "
                                + GOLD + gen.name);

                        StringBuilder c = new StringBuilder(AQUA
                                + "Owner: " + GREEN);
                        for (String p : gen.owners) {
                            c.append(p).append(WHITE).append(", ").append(GREEN);
                        }

                        player.sendMessage(c.substring(0, c.length() - 4)); // Strip off the last comma

                        if (gen.subowners.size() > 0) {
                            StringBuilder d = new StringBuilder(AQUA
                                    + "Subowners: " + GREEN);
                            for (String p : gen.subowners) {
                                d.append(p).append(WHITE).append(", ").append(GREEN);
                            }
                            player.sendMessage(d.substring(0, d.length() - 4)); // Strip off the last comma
                        }
                    }
                }
            }
            return true;
        } else if (args[0].equals("members")) {
            if (!player.hasPermission("artifact.generator.info")) {
                player.sendMessage(AQUA
                        + "You don't have permission to manipulate generators.");
            } else {
                Generator gen;
                if (args.length < 2) {
                    gen = plugin.getHomeTown(player.getName());
                } else {
                    gen = plugin.findGenerator(args[1], player.getName());
                }
                if (gen == null) {
                    player.sendMessage(AQUA + "You are not in a town.");
                } else {
                    if ((gen.allowedPlayers.contains(player.getName())) ||
                            (plugin.isAdmin(player))) {
                        player.sendMessage(GOLD + gen.name + AQUA + " Members" + GRAY + " - Hover to see titles.");
                        player.sendMessage(GRAY + "----------------------");

                        for (int i = 1; i <= 5; i++) {
                            if (!gen.memberLevels.containsValue(i)) continue;

                            FancyMessage msg;
                            if (i != 5) {
                                msg = new FancyMessage("Level " + i)
                                        .color(GOLD)
                                        .then(": ")
                                        .color(GRAY);
                            } else {
                                msg = new FancyMessage("Owners")
                                        .color(GOLD)
                                        .then(": ")
                                        .color(GRAY);
                            }

                            for (String p : gen.memberLevels.keySet()) {
                                if (gen.memberLevels.get(p) == i) {
                                    Player member = plugin.server.getPlayerExact(p);
                                    if (member != null && member.isOnline()) {
                                        msg.then(p)
                                                .color(GREEN);
                                    } else {
                                        msg.then(p)
                                                .color(GRAY);
                                    }
                                    msg.tooltip("Title: " + gen.getPlayerRank(p))
                                            .then(" ");
                                }
                            }
                            msg.send(player);
                        }
                    } else {
                        if (gen.isPrivate && !gen.allowedPlayers.contains(player.getName())) {
                            player.sendMessage(AQUA + "There is no town by that name.");
                            return true;
                        }
                        player.sendMessage(AQUA + "Generator: "
                                + GOLD + gen.name);

                        StringBuilder c = new StringBuilder(AQUA
                                + "Owner: " + GREEN);
                        for (String p : gen.owners) {
                            c.append(p).append(WHITE).append(", ").append(GREEN);
                        }

                        player.sendMessage(c.substring(0, c.length() - 4)); // Strip off the last comma

                        if (gen.subowners.size() > 0) {
                            StringBuilder d = new StringBuilder(AQUA
                                    + "Subowners: " + GREEN);
                            for (String p : gen.subowners) {
                                d.append(p).append(WHITE).append(", ").append(GREEN);
                            }
                            player.sendMessage(d.substring(0, d.length() - 4)); // Strip off the last comma
                        }
                    }
                }
            }
            return true;
        } else if (args[0].equals("level")) {
            if (!player.hasPermission("artifact.generator.info")) {
                player.sendMessage(AQUA
                        + "You don't have permission to manipulate generators.");
            } else {
                Generator g = plugin.getHomeTown(player.getName());
                if (g == null) {
                    player.sendMessage(AQUA + "You have no town.");
                } else {
                    player.sendMessage(GOLD + g.name + AQUA + ": Town Level " + GREEN + g.townLevel);
                    player.sendMessage(AQUA + "---------------------");
                    ConfigFile perkMsgs = new ConfigFile(plugin, "perks.yml");

                    for (String category : plugin.mastery.masteryCategories.keySet()) {
                        FancyMessage msg = new FancyMessage(category)
                                .color(GOLD)
                                .style(BOLD)
                                .then(":");
                        for (String subCategory : plugin.mastery.masteryCategories.get(category)) {
                            String subCategoryDescriptor = perkMsgs.get("perks." + subCategory, null);
                            if (subCategoryDescriptor == null) continue;

                            msg.then(" [" + subCategory)
                                    .color(YELLOW)
                                    .tooltip(subCategoryDescriptor)
                                    .then("-")
                                    .tooltip(subCategoryDescriptor)
                                    .color(GRAY)
                                    .then(g.townLevels.get(subCategory))
                                    .tooltip(subCategoryDescriptor)
                                    .color(GREEN)
                                    .then("] ")
                                    .tooltip(subCategoryDescriptor)
                                    .color(YELLOW);
                        }
                        msg.send(player);
                    }
                }
            }
            return true;
        } else if (args[0].equals("perks")) {
            if (!player.hasPermission("artifact.generator.info")) {
                player.sendMessage(AQUA
                        + "You don't have permission to manipulate generators.");
            } else {
                Generator g = plugin.getHomeTown(player.getName());
                if (g == null) {
                    player.sendMessage(AQUA + "You have no town.");
                } else {
                    player.sendMessage(GOLD + g.name + AQUA + " Town Perks");
                    player.sendMessage(AQUA + "---------------------");
                    ConfigFile perkMsgs = new ConfigFile(plugin, "perks.yml");

                    FancyMessage perks = new FancyMessage("Perks: ")
                            .color(AQUA);

                    for (MasteryPerk p : g.townPerks) {
                        String levelPerkTitle = perkMsgs.get("perks." + MasteryPerk.toInt(p) + ".title", null);
                        String levelPerkDesc = perkMsgs.get("perks." + MasteryPerk.toInt(p) + ".desc", null);
                        if (levelPerkDesc == null || levelPerkTitle == null) continue;

                        perks.then("[" + levelPerkTitle + "]")
                                .color(YELLOW)
                                .style(BOLD)
                                .tooltip(levelPerkDesc)
                                .then(" ");
                    }

                    perks.send(player);
                }
            }
            return true;
        } else if (args[0].equals("help")) {
            if (!player.hasPermission("artifact.generator.info")) {
                player.sendMessage(AQUA
                        + "You don't have permission to do that.");
            } else {
                if (args.length < 2) {
                    player.sendMessage(RED + "General Commands");
                    player.sendMessage(AQUA + "Use " + YELLOW +
                            "/g <command> " + AQUA + "" +
                            "to get more help on each command.");
                    player.sendMessage(YELLOW + "info" + GRAY +
                            ": Get info on your town.");
                    player.sendMessage(YELLOW + "members" + GRAY +
                            ": List the members in your town.");
                    player.sendMessage(YELLOW + "level" + GRAY +
                            ": View your town's leveling breakdown.");
                    player.sendMessage(YELLOW + "perks" + GRAY +
                            ": View the perks unlocked by your town.");
                    if (plugin.isTownSubOwner(player.getName()) || plugin.isTownOwner(player.getName())) {
                        player.sendMessage(YELLOW + "motd" + GRAY +
                                ": Set the town's message of the day.");
                        player.sendMessage(YELLOW + "title" + GRAY +
                                ": Set a player's title. Shown only in town chat.");
                        player.sendMessage(YELLOW + "promote/demote" + GRAY +
                                ": Promote or Demote a player's town level.");
                        player.sendMessage(YELLOW + "add" + GRAY +
                                ": Add a player to your town.");
                        player.sendMessage(YELLOW + "del" + GRAY +
                                ": Remove a player from your town.");
                    }
                    if (plugin.isTownOwner(player.getName())) {
                        player.sendMessage(RED + "Owner Commands");
                        player.sendMessage(YELLOW + "tag" + GRAY +
                                ": Set town's abbreviated name displayed in town chat.");
                        player.sendMessage(YELLOW + "private" + GRAY +
                                ": If private, a player using a Feather will not see your town's name.");
                        player.sendMessage(YELLOW + "setportal" + GRAY +
                                ": Set up your town portal.");
                        player.sendMessage(YELLOW + "addowner" + GRAY +
                                ": Add owner to generator. Gives player full access to gen commands.");
                        player.sendMessage(YELLOW + "delowner" + GRAY +
                                ": Removes owner from generator.");
                        player.sendMessage(YELLOW + "transfer" + GRAY +
                                ": Transfer ownership of town to another player.");
                        player.sendMessage(YELLOW + "delete" + GRAY +
                                ": Completely delete your town.");
                        player.sendMessage(GRAY + "Use " + YELLOW + "/g help 2" + GRAY + " to view Town Perk Commands");
                    }
                } else if (args[1].equalsIgnoreCase("2") && plugin.isTownOwner(player.getName())) {
                    player.sendMessage(RED + "Town Perk Commands");
                    player.sendMessage(YELLOW + "zone" + GRAY +
                            ": Define member-level based building zones within town.");
                    player.sendMessage(YELLOW + "embassy" + GRAY +
                            ": Setup an Embassy if you are in an alliance.");
                    player.sendMessage(YELLOW + "mobspawning" + GRAY +
                            ": Toggle mob spawning inside of your town.");
                }
            }
            return true;
        } else if (args[0].equals("rank") || args[0].equals("title")) {
            if (args.length < 2) {
                player.sendMessage(GRAY + "Sets player's in-town title, shown in town chat.");
                player.sendMessage(AQUA + "Usage: " +
                        YELLOW + "/g title <player> <title>" +
                        AQUA + ".");
                return true;
            } else {
                Generator g = plugin.getAccount(player.getName()).getHomeTown();
                if (g == null) {
                    player.sendMessage(AQUA
                            + "You need to set your town with " + YELLOW + "/town" +
                            AQUA + " first.");
                } else if (!g.owner.equals(player.getName()) && !g.subowners.contains(player.getName())) {
                    player.sendMessage(AQUA
                            + "You must be a town owner or subowner to set rank.");
                    return true;
                } else if (plugin.playerExists(args[1])) {
                    player.sendMessage(AQUA + "No such player "
                            + GREEN + args[1] + AQUA + ".");
                    return true;
                } else {
                    OfflinePlayer rankee = server.getOfflinePlayer(
                            args[1]);
                    if ((rankee != null) && (rankee.getFirstPlayed() > 0)) {
                        if (g.subowners.contains(player.getName()) && g.owner.equals(rankee.getName())) {
                            player.sendMessage(AQUA + "You cannot change a town owner's title");
                        } else {
                            if (args.length == 2) {
                                g.removePlayerRank(rankee.getName());
                                player.sendMessage(GREEN + rankee.getName() +
                                        AQUA + "'s title was removed.");
                                if (rankee.isOnline()) {
                                    Player rankeePlayer = server.getPlayer(rankee.getName());
                                    rankeePlayer.sendMessage(GREEN + player.getName() +
                                            AQUA + " has removed your title in " +
                                            GOLD + g.name + AQUA +
                                            ".");
                                }

                            } else {
                                StringBuilder rank = new StringBuilder();
                                for (int i = 2; i < args.length; i++) {
                                    rank.append(args[i]).append(" ");
                                }
                                g.setPlayerRank(rankee.getName(), rank.substring(0, rank.length() - 1));
                                player.sendMessage(GREEN + rankee.getName() +
                                        AQUA + " now has the title " +
                                        GREEN + rank.toString() + AQUA +
                                        " in " + GOLD + g.name + AQUA +
                                        ".");
                                if (rankee.isOnline()) {
                                    Player rankeePlayer = server.getPlayer(rankee.getName());
                                    rankeePlayer.sendMessage(GREEN + player.getName() +
                                            AQUA + " has set your title as " +
                                            GREEN + rank.toString() + AQUA +
                                            " in " + GOLD + g.name + AQUA +
                                            ".");
                                }
                            }
                            return true;
                        }
                    }

                }
            }
        } else if (args[0].equals("promote")) {
            if (args.length < 2) {
                player.sendMessage(GRAY + "Increases a player's level in town. Town zones are based off of this level.");
                player.sendMessage(AQUA + "Usage: " + YELLOW + "/g promote <player>" + AQUA + ".");
                return true;
            } else {
                Generator g = plugin.getAccount(player.getName()).getHomeTown();
                if (g == null) {
                    player.sendMessage(AQUA
                            + "You need to set your town with " + YELLOW + "/town" +
                            AQUA + " first.");
                } else if (!g.owner.equals(player.getName()) && !g.subowners.contains(player.getName())) {
                    player.sendMessage(AQUA
                            + "You must be a town owner or subowner to set rank.");
                    return true;
                } else if (plugin.playerExists(args[1])) {
                    player.sendMessage(AQUA + "No such player "
                            + GREEN + args[1] + AQUA + ".");
                    return true;
                } else {
                    OfflinePlayer rankee = server.getOfflinePlayer(
                            args[1]);
                    if ((rankee != null) && (rankee.getFirstPlayed() > 0)) {
                        if (g.subowners.contains(rankee.getName()) || g.owner.equals(rankee.getName())) {
                            player.sendMessage(AQUA + "You cannot change a town owner's rank");
                        } else if (g.getMemberLevel(rankee) + 1 > g.maxAvailableMemberLevel()) {
                            player.sendMessage(AQUA + "Your town has not yet unlocked the ability to promote a member this high.");
                        } else if (g.getMemberLevel(rankee) == 4) {
                            player.sendMessage(GREEN + rankee.getName() + AQUA + " cannot be promoted above level 4");
                        } else {
                            g.promotePlayer(rankee.getName());
                            player.sendMessage(GREEN + rankee.getName() +
                                    AQUA + " is now level " +
                                    GREEN + g.getMemberLevel(rankee) + AQUA +
                                    " in " + GOLD + g.name + AQUA +
                                    ".");
                            if (rankee.isOnline()) {
                                Player rankeePlayer = server.getPlayer(rankee.getName());
                                rankeePlayer.sendMessage(GREEN + player.getName() +
                                        AQUA + " has promoted you to level " +
                                        GREEN + g.getMemberLevel(rankee) + AQUA +
                                        " in " + GOLD + g.name + AQUA +
                                        ".");
                            }
                        }
                    }
                    return true;
                }
            }
        } else if (args[0].equals("demote")) {
            if (args.length < 2) {
                player.sendMessage(GRAY + "Decreases a player's level in town. Town zones are based off of this level.");
                player.sendMessage(AQUA + "Usage: " + YELLOW + "/g demote <player>" + AQUA + ".");
                return true;
            } else {
                Generator g = plugin.getAccount(player.getName()).getHomeTown();
                if (g == null) {
                    player.sendMessage(AQUA
                            + "You need to set your town with " + YELLOW + "/town" +
                            AQUA + " first.");
                } else if (!g.owner.equals(player.getName()) && !g.subowners.contains(player.getName())) {
                    player.sendMessage(AQUA
                            + "You must be a town owner or subowner to set rank.");
                } else if (plugin.playerExists(args[1])) {
                    player.sendMessage(AQUA + "No such player "
                            + GREEN + args[1] + AQUA + ".");
                } else {
                    OfflinePlayer rankee = server.getOfflinePlayer(
                            args[1]);
                    if ((rankee != null) && (rankee.getFirstPlayed() > 0)) {
                        if (g.subowners.contains(rankee.getName()) || g.owner.equals(rankee.getName())) {
                            player.sendMessage(AQUA + "You cannot change a town owner's rank");
                        } else if (g.getMemberLevel(rankee) == 1) {
                            player.sendMessage(GREEN + rankee.getName() + AQUA + " cannot be demoted below level 1");
                        } else {
                            g.demotePlayer(rankee.getName());
                            player.sendMessage(GREEN + rankee.getName() +
                                    AQUA + " is now level " +
                                    GREEN + g.getMemberLevel(rankee) + AQUA +
                                    " in " + GOLD + g.name + AQUA +
                                    ".");
                            if (rankee.isOnline()) {
                                Player rankeePlayer = server.getPlayer(rankee.getName());
                                rankeePlayer.sendMessage(GREEN + player.getName() +
                                        AQUA + " has demoted you to level " +
                                        GREEN + g.getMemberLevel(rankee) + AQUA +
                                        " in " + GOLD + g.name + AQUA +
                                        ".");
                            }
                        }
                    }
                    return true;
                }
            }
        } else if (args[0].equals("zone")) {
            if (args.length < 2) {
                player.sendMessage(GRAY + "Manipulate Town Zones:");
                player.sendMessage(GRAY + "----------------------");
                player.sendMessage(YELLOW + "/g zone create" + GRAY +
                        ": Create a town zone.");
                player.sendMessage(YELLOW + "/g zone redefine" + GRAY +
                        ": Redefine the boundaries of a zone given a selection.");
                player.sendMessage(YELLOW + "/g zone default" + GRAY +
                        ": For unzoned parts of town, this allows members of the specified level and above to build.");
                player.sendMessage(YELLOW + "/g zone list" + GRAY +
                        ": List zones for your town.");
                player.sendMessage(YELLOW + "/g zone delete" + GRAY +
                        ": Delete a town zone");
                return true;
            } else {
                Generator g = plugin.getAccount(player.getName()).getHomeTown();
                if (g == null) {
                    player.sendMessage(AQUA
                            + "You need to set your town with " + YELLOW + "/town" +
                            AQUA + " first.");
                } else if (!g.townPerks.contains(MasteryPerk.TOWN2)) {
                    player.sendMessage(GRAY + "Your town has not yet unlocked zoning.");
                    return true;
                } else if (!g.owner.equals(player.getName()) && !g.subowners.contains(player.getName())) {
                    player.sendMessage(AQUA
                            + "Only town owners may edit zones.");
                } else if (!g.withinProtection(player.getLocation())) {
                    player.sendMessage(GRAY + "You must be within your town's protection to use this.");
                } else {
                    switch (args[1]) {
                        case "create":
                            if (args.length < 5) {
                                player.sendMessage(GRAY + "Define an area around you that only members of a certain level can build in.");
                                player.sendMessage(AQUA + "Usage: " + YELLOW + "/g zone create <zonename> <memberlevel> <radius/selection>" + AQUA + ".");
                                player.sendMessage(AQUA + "By Radius: " + YELLOW + "/g zone create Bank 3 50" + GRAY + " would create " +
                                        "a zone called " + GOLD + "Bank" + GRAY + " for " + GREEN + "level 3+ members" + GRAY
                                        + " with a radius of " + GREEN + "50 (100x100 blocks) from bedrock to sky.");
                                player.sendMessage(AQUA + "By selection: " + YELLOW + "/g zone create Bank 3 sel" + GRAY + " would create " +
                                        "a zone based on the selection created by your " + GOLD + "Golden Axe" + GRAY + ".");
                                return true;
                            } else {
                                if (g.getZone(args[2]) != null) {
                                    player.sendMessage(GRAY + "There is already a zone called " + GOLD + args[2] +
                                            GRAY + ". Please use " + YELLOW + "/g zone redefine" + GRAY + " or delete the old one.");
                                    return true;
                                }

                                int level = Integer.parseInt(args[3]);
                                if (level != 5 && g.getUnlockedZoneLevels().size() == 1) {
                                    player.sendMessage(GRAY + "Your town only has the ability to create the Owner " +
                                            "zone thus far. To indicate the owner zone, use a " + GREEN + "member level of 5");
                                    return true;
                                } else if (!g.getUnlockedZoneLevels().contains(level)) {
                                    player.sendMessage(GRAY + "Your town cannot yet create zones for " + GREEN +
                                            "level " + level + GRAY + " members.");
                                    return true;
                                }

                                Selection sel = worldEdit.getSelection(player);
                                if (args[4].equals("sel")) {
                                    if (sel == null) {
                                        player.sendMessage(GRAY + "You must have a selection defined.");
                                        return true;
                                    } else {
                                        Zone proto = new Zone(plugin, args[2], g);
                                        proto.p1 = new StringLocation(sel.getMinimumPoint());
                                        proto.p2 = new StringLocation(sel.getMaximumPoint());
                                        proto.level = level;

                                        if (!g.zoneWithinProtection(sel.getMaximumPoint(), sel.getMinimumPoint())) {
                                            player.sendMessage(GRAY + "This zone would extend beyond your town's protection. " +
                                                    "Zones must lie completely within town protection.");
                                            return true;
                                        }
                                        player.sendMessage(AQUA + "Zone: " + GOLD + args[2] + AQUA + ", Range: "
                                                + proto.printRegion());
                                        protoZones.put(player.getName(), proto);

//                                        scheduler.runTask(this, new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                proto.playSelection(player, true);
//                                            }
//                                        });

                                        new FancyMessage("Create this zone? ")
                                                .color(GRAY)
                                                .then("[Create]")
                                                .color(GREEN)
                                                .style(BOLD)
                                                .tooltip("Create this zone")
                                                .command("/g zone confirm")
                                                .then(" or ")
                                                .color(GRAY)
                                                .then("[Cancel]")
                                                .tooltip("Cancel creating this zone")
                                                .command("/g zone cancel")
                                                .color(RED)
                                                .style(BOLD)
                                                .send(player);
                                    }
                                } else {
                                    //Creating zone via radius
                                    int radius = Integer.parseInt(args[4]);
                                    Location p1 = player.getLocation().clone().add(radius, 0, radius);
                                    p1.setY(0);
                                    Location p2 = player.getLocation().clone().subtract(radius, 0, radius);
                                    p2.setY(255);

                                    if (!g.zoneWithinProtection(p1, p2)) {
                                        player.sendMessage(GRAY + "This zone would extend beyond your town's protection. " +
                                                "Zones must lie completely within town protection.");
                                        return true;
                                    }

                                    Zone proto = new Zone(plugin, args[2], g);
                                    proto.p1 = new StringLocation(p1);
                                    proto.p2 = new StringLocation(p2);
                                    proto.level = level;
                                    player.sendMessage(AQUA + "Zone: " + GOLD + args[2] + AQUA + ", Range: "
                                            + proto.printRegion());
                                    protoZones.put(player.getName(), proto);

//                                    plugin.scheduler.runTask(plugin, () -> proto.playSelection(player, false));

                                    new FancyMessage("Create this zone? ")
                                            .color(GRAY)
                                            .then("[Create]")
                                            .color(GREEN)
                                            .style(BOLD)
                                            .tooltip("Create this zone")
                                            .command("/g zone confirm")
                                            .then(" or ")
                                            .color(GRAY)
                                            .then("[Cancel]")
                                            .tooltip("Cancel creating this zone")
                                            .command("/g zone cancel")
                                            .color(RED)
                                            .style(BOLD)
                                            .send(player);
                                }
                            }
                            break;
                        case "confirm": {
                            if (!protoZones.containsKey(player.getName())) return true;

                            Zone z = protoZones.get(player.getName());
                            z.saveZone();
                            z.updateRegion();
                            g.zones.add(z);

                            player.sendMessage(GOLD + protoZones.get(player.getName()).name + GRAY + " created.");

                            protoZones.remove(player.getName());
                            return true;
                        }
                        case "cancel":
                            if (!protoZones.containsKey(player.getName())) return true;

                            protoZones.get(player.getName()).cancelSelection(player.getName());
                            protoZones.remove(player.getName());
                            player.sendMessage(GRAY + "Zone creation cancelled.");
                            return true;
                        case "delete": {
                            if (args.length < 3) {
                                player.sendMessage(AQUA + "Usage: " + YELLOW + "/g zone delete <zonename>");
                                return true;
                            }
                            Zone z = g.getZone(args[2]);
                            if (z == null) {
                                player.sendMessage(GRAY + "No such zone: " + GOLD + args[2]);
                                return true;
                            }

                            z.delete();
                            player.sendMessage(GOLD + z.name + AQUA + " was deleted.");
                            break;
                        }
                        case "default":
                            if (!g.townPerks.contains(MasteryPerk.TOWN3)) {
                                player.sendMessage(GRAY + "Your town has not unlocked this yet.");
                                return true;
                            } else if (args.length < 3) {
                                player.sendMessage(GRAY + "Any town member with a level less than default cannot " +
                                        "build except in their specific zone.");
                                player.sendMessage(AQUA + "Usage: " + YELLOW + "/g zone default <zonename>");
                                return true;
                            }

                            g.defaultLevel = Integer.parseInt(args[2]);
                            g.save(false);
                            player.sendMessage(AQUA + "Default permission level set to " + GREEN + args[2]);
                            break;
                        case "redefine": {
                            if (args.length < 3) {
                                player.sendMessage(AQUA + "Usage: " + YELLOW + "/g zone redefine <zonename>");
                                return true;
                            }

                            Zone z = g.getZone(args[2]);
                            if (z == null) {
                                player.sendMessage(GRAY + "No such zone: " + GOLD + args[2]);
                                return true;
                            }

                            Selection sel = worldEdit.getSelection(player);
                            if (sel == null) {
                                player.sendMessage(GRAY + "You must have a selection defined.");
                                return true;
                            }

                            if (!g.zoneWithinProtection(sel.getMaximumPoint(), sel.getMinimumPoint())) {
                                player.sendMessage(GRAY + "This zone would extend beyond your town's protection. " +
                                        "Zones must lie completely within town protection.");
                                return true;
                            }

                            z.p1 = new StringLocation(sel.getMinimumPoint());
                            z.p2 = new StringLocation(sel.getMaximumPoint());
                            z.saveZone();
                            z.updateRegion();

                            player.sendMessage(AQUA + "Zone: " + GOLD + z.name + AQUA + " updated with new range: "
                                    + GREEN + prettyCoords(z.p1) + GRAY + " - " + GREEN + prettyCoords(z.p2));
                            break;
                        }
                        case "list":
                            for (Zone z : g.zones) {
                                if (z.p1 == null || z.p2 == null) continue;
                                player.sendMessage(GOLD + z.name + AQUA + " (Member Level " + GREEN + z.level + AQUA
                                        + ") - Covers: " + z.printRegion());
                            }
                            break;
                    }
                }
                return true;
            }
        } else if (args[0].equals("embassy")) {
            if (args.length < 2) {
                player.sendMessage(GRAY + "Create an Embassy for use with allied towns.");
                player.sendMessage(GRAY + "----------------------");
                player.sendMessage(YELLOW + "/g embassy create" + GRAY +
                        ": Create an embassy.");
                player.sendMessage(YELLOW + "/g zone redefine" + GRAY +
                        ": Redefine the boundaries of an embassy given a selection.");
                player.sendMessage(YELLOW + "/g zone delete" + GRAY +
                        ": Delete an embassy");
                return true;
            } else {
                Generator g = plugin.getAccount(player.getName()).getHomeTown();
                if (g == null) {
                    player.sendMessage(AQUA
                            + "You need to set your town with " + YELLOW + "/town" +
                            AQUA + " first.");
                } else if (!g.townPerks.contains(MasteryPerk.EMBASSY)) {
                    player.sendMessage(GRAY + "Your town has not yet unlocked embassies.");
                    return true;
                } else if (!g.owner.equals(player.getName()) && !g.subowners.contains(player.getName())) {
                    player.sendMessage(AQUA
                            + "Only town owners may create embassies.");
                } else if (!g.withinProtection(player.getLocation())) {
                    player.sendMessage(GRAY + "You must be within your town's protection to use this.");
                    return true;
                } else {
                    switch (args[1]) {
                        case "create":
                            if (args.length < 4) {
                                player.sendMessage(GRAY + "Define the area and the town for the embassy with a max range of 30.");
                                player.sendMessage(AQUA + "Usage: " + YELLOW + "/g embassy create <townname> <memberlevel>" + AQUA + ".");
                                player.sendMessage(AQUA + "Embassy zone creation is the same as " + YELLOW + "/g zone"
                                        + AQUA + ".");
                                return true;
                            } else {
                                Generator alliedTown = plugin.getGenerator(args[2]);
                                if (alliedTown == null) {
                                    player.sendMessage(GRAY + "There is no town " + GOLD + args[2] + GRAY + ".");
                                    return true;
                                } else if (!g.alliance.towns.contains(alliedTown.name)) {
                                    player.sendMessage(GRAY + "You are not allied with " + GOLD + alliedTown.name +
                                            GRAY + ".");
                                    return true;
                                } else if (g.getZone(args[2]) != null) {
                                    player.sendMessage(GRAY + "You already have an embassy for " + GOLD + args[2] +
                                            GRAY + ". Please use " + YELLOW + "/g zone redefine" + GRAY + " or delete the old one.");
                                    return true;
                                }

                                int level = Integer.parseInt(args[3]);
                                if (!g.getUnlockedZoneLevels().contains(level)) {
                                    player.sendMessage(GRAY + "Your town cannot yet create embassies for " + GREEN +
                                            "level " + level + GRAY + " members.");
                                    return true;
                                }

                                Selection sel = worldEdit.getSelection(player);
                                if (sel == null) {
                                    player.sendMessage(GRAY + "You must have a selection defined.");
                                    return true;
                                } else {
                                    Embassy proto = new Embassy(plugin, args[2], g);
                                    proto.p1 = new StringLocation(sel.getMinimumPoint());
                                    proto.p2 = new StringLocation(sel.getMaximumPoint());
                                    proto.level = level;

                                    if (proto.biggerThan(60)) {
                                        player.sendMessage(GRAY + "Embassies must have a minimum radius of 5 and a maximum of 30.");
                                        return true;
                                    }

                                    if (!g.zoneWithinProtection(sel.getMaximumPoint(), sel.getMinimumPoint())) {
                                        player.sendMessage(GRAY + "This embassy zone would extend beyond your town's protection. " +
                                                "Zones must lie completely within town protection.");
                                        return true;
                                    }
                                    player.sendMessage(AQUA + "Embassy for: " + GOLD + args[2] + AQUA + ", Range: "
                                            + proto.printRegion());
                                    protoEmbassies.put(player.getName(), proto);

//                                    plugin.scheduler.runTask(plugin, () -> proto.playSelection(player, false));

                                    new FancyMessage("Create this embassy? ")
                                            .color(GRAY)
                                            .then("[Create]")
                                            .color(GREEN)
                                            .style(BOLD)
                                            .tooltip("Create this zone")
                                            .command("/g embassy confirm")
                                            .then(" or ")
                                            .color(GRAY)
                                            .then("[Cancel]")
                                            .tooltip("Cancel creating this zone")
                                            .command("/g embassy cancel")
                                            .color(RED)
                                            .style(BOLD)
                                            .send(player);
                                }
                            }
                            break;
                        case "confirm": {
                            if (!protoEmbassies.containsKey(player.getName())) return true;

                            Embassy e = protoEmbassies.get(player.getName());

                            //Does our ally have an embassy already? If not, we're the chest inventory owner.
                            e.ownsInventory = g.getAllyEmbassy(e.name) == null;

                            e.save(null);
                            e.updateRegion();
                            g.embassies.add(e);

                            player.sendMessage(AQUA + "Embassy for " + GOLD + protoEmbassies.get(player.getName()).name
                                    + GRAY + " created.");

                            protoEmbassies.remove(player.getName());
                            return true;
                        }
                        case "cancel":
                            if (!protoEmbassies.containsKey(player.getName())) return true;

                            protoEmbassies.get(player.getName()).cancelSelection(player.getName());
                            protoEmbassies.remove(player.getName());
                            player.sendMessage(GRAY + "Embassy creation cancelled.");
                            return true;
                        case "delete": {
                            if (args.length < 3) {
                                player.sendMessage(AQUA + "Usage: " + YELLOW + "/g embassy delete <townname>");
                                return true;
                            }
                            Embassy e = g.getEmbassy(args[2]);
                            if (e == null) {
                                player.sendMessage(GRAY + "No such Embassy: " + GOLD + args[2]);
                                return true;
                            }

                            e.delete();
                            player.sendMessage(GOLD + e.name + AQUA + " was deleted.");
                            break;
                        }
                        case "redefine": {
                            if (args.length < 3) {
                                player.sendMessage(AQUA + "Usage: " + YELLOW + "/g embassy redefine <townname>");
                                return true;
                            }

                            Embassy e = g.getEmbassy(args[2]);
                            if (e == null) {
                                player.sendMessage(GRAY + "No such embassy for: " + GOLD + args[2]);
                                return true;
                            }

                            Selection sel = worldEdit.getSelection(player);
                            if (sel == null) {
                                player.sendMessage(GRAY + "You must have a selection defined.");
                                return true;
                            }

                            if (!g.zoneWithinProtection(sel.getMaximumPoint(), sel.getMinimumPoint())) {
                                player.sendMessage(GRAY + "This zone would extend beyond your town's protection. " +
                                        "Zones must lie completely within town protection.");
                                return true;
                            }

                            e.p1 = new StringLocation(sel.getMinimumPoint());
                            e.p2 = new StringLocation(sel.getMaximumPoint());
                            e.saveZone();
                            e.updateRegion();

                            player.sendMessage(AQUA + "Embassy for: " + GOLD + e.name + AQUA + " updated with new range: "
                                    + GREEN + prettyCoords(e.p1) + GRAY + " - " + GREEN + prettyCoords(e.p2));
                            break;
                        }
                    }
                }
                return true;
            }
        } else if (args[0].equalsIgnoreCase("alliance")) {
            if (args.length < 2) {
                player.sendMessage(GRAY + "Create or join Alliances with other towns");
                player.sendMessage(GRAY + "----------------------");
                player.sendMessage(YELLOW + "/g alliance invite" + GRAY +
                        ": Invite a town to your Alliance.");
                player.sendMessage(YELLOW + "/g alliance remove" + GRAY +
                        ": Remove a town from your Alliance.");
                player.sendMessage(YELLOW + "/g alliance leave" + GRAY +
                        ": Leave your Alliance.");
                player.sendMessage(YELLOW + "/g alliance info" + GRAY +
                        ": Get info about your Alliance");
                return true;
            }

            Generator g = plugin.getAccount(player.getName()).getHomeTown();
            if (g == null) {
                player.sendMessage(AQUA
                        + "You need to set your town with " + YELLOW + "/town" +
                        AQUA + " first.");
            } else if (!g.townPerks.contains(MasteryPerk.ALLIANCES)) {
                player.sendMessage(GRAY + "Your town has not yet unlocked alliances.");
                return true;
            } else if (!g.owner.equals(player.getName()) && !g.subowners.contains(player.getName())) {
                player.sendMessage(AQUA
                        + "Only town owners may form alliances.");
            } else {
                switch (args[1]) {
                    case "invite":
                        if (args.length < 2) {
                            player.sendMessage(GRAY + "Invite another town to join your alliance..");
                            player.sendMessage(AQUA + "Usage: " + YELLOW + "/g alliance invite <townname>" + AQUA + ".");
                            return true;
                        } else {
                            Generator invitee = plugin.getGenerator(args[2]);
                            Player inviteeOwner = server.getPlayerExact(invitee.owner);
                            if (invitee.alliance != null) {
                                player.sendMessage(GOLD + invitee.name + GRAY + " is already in an Alliance");
                                return true;
                            } else if (!invitee.townPerks.contains(MasteryPerk.ALLIANCES)) {
                                player.sendMessage(GOLD + invitee.name + GRAY + " has not yet unlocked Alliances.");
                                return true;
                            } else if (inviteeOwner == null) {
                                player.sendMessage(GRAY + "The owner of " + GOLD + invitee.name + GRAY + " must be " +
                                        "online to accept an Alliance invite.");
                                return true;
                            }

                            pendingAllianceInvites.put(invitee.name, g.name);

                            inviteeOwner.sendMessage(GREEN + player.getName() + AQUA + " of " + GOLD + g.name +
                                    AQUA + " has invited you to create an Alliance!");

                            new FancyMessage("Do you wish to join? ")
                                    .color(GRAY)
                                    .then("[Join]")
                                    .color(GREEN)
                                    .style(BOLD)
                                    .tooltip("Join an Alliance with " + invitee.name)
                                    .command("/g alliance accept")
                                    .then(" or ")
                                    .color(GRAY)
                                    .then("[Refuse]")
                                    .tooltip("Do not join an Alliance with " + invitee.name)
                                    .command("/g alliance refuse")
                                    .color(RED)
                                    .style(BOLD)
                                    .send(inviteeOwner);
                        }
                        break;
                    case "accept":
                        if (!pendingAllianceInvites.containsKey(g.name)) return true;

                        Generator inviter = plugin.getGenerator(pendingAllianceInvites.get(g.name));

                        if (inviter.alliance == null) {
                            //This is a new Alliance
                            Alliance a = new Alliance(inviter.name);
                            a.towns.add(inviter.name);
                            a.towns.add(g.name);
                            a.leader = inviter.name;
                            a.save();

                            inviter.alliance = a;
                            g.alliance = a;

                            inviter.messageMembers(AQUA + "Your town has created an an Alliance with " + GOLD
                                    + g.name + AQUA + "!");
                            g.messageMembers(AQUA + "Your town has join an an Alliance with " + GOLD
                                    + inviter.name + AQUA + "!");
                        } else {
                            inviter.alliance.towns.add(g.name);
                            inviter.alliance.save();

                            g.alliance = inviter.alliance;
                            inviter.messageMembers(GOLD + g.name + AQUA + " has joined your Alliance!");
                            g.messageMembers(AQUA + "Your town has joined in an Alliance with " + GOLD
                                    + inviter.name + AQUA + "!");
                        }
                        break;
                    case "refuse":
                        if (!pendingAllianceInvites.containsKey(g.name)) return true;

                        Generator townInviter = plugin.getGenerator(pendingAllianceInvites.get(g.name));
                        Player inviterOwner = server.getPlayerExact(townInviter.owner);
                        if (inviterOwner == null) return true;

                        pendingAllianceInvites.remove(g.name);
                        inviterOwner.sendMessage(GREEN + player.getName() + GRAY + " has refused your Alliance offer.");
                        break;
                    case "remove":
                        if (g.alliance == null) {
                            player.sendMessage(GRAY + "Your town is not in an Alliance.");
                            return true;
                        } else if (!g.alliance.leader.equals(g.name)) {
                            player.sendMessage(GRAY + "Only the leader of the alliance can remove other towns from it.");
                            return true;
                        }

                        Generator removee = plugin.getGenerator(args[2]);
                        if (removee == null) {
                            player.sendMessage(GRAY + "No such town " + GOLD + args[2]);
                            return true;
                        } else if (!g.alliance.towns.contains(removee.name)) {
                            player.sendMessage(GOLD + removee.name + GRAY + " is not in your Alliance.");
                            return true;
                        }

                        g.alliance.removeTown(removee.name);

                        removee.alliance = null;
                        removee.messageMembers(AQUA + "Your town was removed from your Alliance!");
                        g.messageMembers(GOLD + removee.name + AQUA + " was removed from your Alliance!");

                        if (g.alliance.towns.size() == 1) {
                            //Alliance will disband
                            g.alliance.disband();
                            g.alliance = null;

                            g.messageMembers(GRAY + "Your alliance was disbanded.");
                        }
                        break;
                    case "leave":
                        if (g.alliance == null) {
                            player.sendMessage(GRAY + "Your town is not in an Alliance.");
                            return true;
                        }

                        g.alliance.removeTown(g.name);
                        g.alliance = null;

                        g.messageMembers(AQUA + "Your town has left its Alliance!");

                        if (g.alliance.towns.size() == 1) {
                            //Alliance will disband
                            g.alliance.disband();
                            g.alliance.messageMembers(GRAY + "Your alliance was disbanded.");
                        }
                        break;
                    case "info":
                        if (g.alliance == null) {
                            player.sendMessage(GRAY + "Your town is not in an Alliance.");
                            return true;
                        }

                        player.sendMessage(GRAY + "Alliance - " + YELLOW + g.alliance.name);
                        player.sendMessage(GRAY + "----------------------");
                        player.sendMessage(AQUA + "Leader: " + GOLD + g.alliance.leader);
                        StringBuilder b = new StringBuilder();
                        for (String town : g.alliance.towns) {
                            b.append(GOLD).append(town).append(AQUA).append(", ");
                        }
                        player.sendMessage(AQUA + "Towns: " + b.toString().substring(0, b.toString().length() - 2));
                        break;
                }
            }

            return true;
        } else if (args[0].equalsIgnoreCase("industry")) {
            if (args.length < 2) {
                player.sendMessage(GRAY + "Create and Modify Town Industries");
                player.sendMessage(GRAY + "----------------------");
                player.sendMessage(YELLOW + "/g industry create" + GRAY +
                        ": Create an industry for any town level over 50.");
                player.sendMessage(YELLOW + "/g industry redefine" + GRAY +
                        ": Redefine the zone for an industry with a selection.");
                player.sendMessage(YELLOW + "/g industry level" + GRAY +
                        ": Change the member permission level for an Industry's zone.");
                return true;
            }
            if (args[1].equalsIgnoreCase("create")) {
                plugin.industry.showIndustries(player);
            } else if (args[1].equalsIgnoreCase("confirm")) {
                plugin.industry.confirmBuilding(player);
            } else if (args[1].equalsIgnoreCase("freeze")) {
                plugin.industry.freezeBuilding(player);
            } else if (args[1].equalsIgnoreCase("resume")) {
                plugin.industry.resumeBuilding(player);
            } else if (args[1].equalsIgnoreCase("cancel")) {
                plugin.industry.cancelBuilding(player);
            } else if (args[1].equalsIgnoreCase("redefine")) {
                if (args.length < 3) {
                    player.sendMessage(GRAY + "Usage: " + YELLOW + "/g industry redefine <industry>");
                    return true;
                }
                plugin.industry.redefineIndustry(player, args[2]);
            } else if (args[1].equalsIgnoreCase("level")) {
                if (args.length < 4) {
                    player.sendMessage(GRAY + "Usage: " + YELLOW + "/g industry level <industry> <level>");
                    return true;
                }

                plugin.industry.changeIndustryLevel(player, args[2], Integer.parseInt(args[3]));
            }
        } else if (args[0].equalsIgnoreCase("create")) {
            if (args[1].equalsIgnoreCase("Logging")) {
                plugin.industry.previewIndustryBuilding(player, args[1]);
            } else if (args[1].equalsIgnoreCase("Cooking")) {
                plugin.industry.previewIndustryBuilding(player, args[1]);
            } else {
                player.sendMessage(GRAY + "That is not yet available.");
            }
        }
        return true;
    }
}

