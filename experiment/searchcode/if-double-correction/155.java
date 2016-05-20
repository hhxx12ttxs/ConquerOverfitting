package REALDrummer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class myScribe extends JavaPlugin implements Listener {
    public static Server server;
    public static ConsoleCommandSender console;
    public static final ChatColor COLOR = ChatColor.BLUE;
    private String[] parameters;
    private static final String[] enable_messages = { "The pen is mightier than the pixelated diamond sword.",
            "I shall demonstrate unto all of you the proper way to use this thrillingly versatile language that we refer to as 'English.'",
            "I advise against the use of my AutoCorrection features if many persons that choose to enter your server do not speak English.",
            "William Shakespeare? I once knew him as \"Bill.\"", "I am rapping. ...rapping at your chamber door." },
            disable_messages = {
                    "Though I am now disabled, I shall continue to spread proper literacy across the globe in the hope that some day soon, we will see people speaking proper English once again.",
                    "If you believe plugins can't dream,...\n...you're wrong.", "Farewell, good literate sir.", "Good evening, Sir Operator.",
                    "I shall return with the gifts of proper language upon the arrival of the upcoming morn." }, profanities = { "fuck", "fck", "fuk", "Goddamn", "Goddam",
                    "damn", "shit", "dammit", "bastard", "bitch", "btch", "damnit", "cunt", "asshole", "bigass", "dumbass", "badass", "dick" }, borders = { "[]", "\\/",
                    "\"*", "_^", "-=", ":;", "&%", "#@", ",.", "<>", "~$", ")(" }, yeses = { "yes", "yeah", "yep", "ja", "sure", "why not", "okay", "do it", "fine",
                    "whatever", "very well", "accept", "tpa", "cool", "hell yeah", "hells yeah", "hells yes", "come" }, nos = { "no", "nah", "nope", "no thanks", "no don't",
                    "shut up", "ignore", "it's not", "its not", "creeper", "unsafe", "wait", "one ", "1 " }, common_acronyms = { "RAM", "NASA", "ASAP", "B&B", "D&D",
                    "HAZMAT", "HAZ-MAT", "NIMBY", "Q&A", "R&R", "AIDS", "HIV", "DNA", "NATO", "AC", "AD", "AKA", "AM", "ASAP", "ATM", "B&B", "BC", "BCE", "BLT", "CC", "CIA",
                    "CO", "CST", "DOA", "DST", "EST", "FAQ", "FBI", "FDR", "FM", "GI", "GMO", "HAZMAT", "HAZ-MAT", "HMO", "ID", "IQ", "ISBN", "JFK", "JV", "KO", "LCD", "LED",
                    "MC", "MLK", "MO", "MST", "MTG", "NAFTA", "NASA", "NATO", "NBA", "NFL", "NHL", "NIMBY", "OJ", "OPEC", "PBJ", "PB&J", "PC", "PI", "PM", "POTUS", "PS",
                    "PR", "PSI", "PST", "Q&A", "R&R", "RAM", "RGB", "ROM", "ROTC", "ROYGBIV", "RPG", "RSVP", "RV", "SCUBA", "SNAFU", "SOP", "SOS", "SPF", "TBA", "TGIF",
                    "TLC", "TV", "UFO", "UN", "UNICEF", "UPC", "VIP", "VP" };
    private static final String[][] default_login_messages = {
            { "[server]", "&9Hide your wife! [epithet] just got on!", "&9Did someone order a [epithet]?", "&9Please insert [epithet] to proceed",
                    "&9WARNING: [epithet] &9detected", "&9[epithet] has arrived.", "&9[epithet], you are the winning visitor!",
                    "&bNotch&9 has arrived. \nNope, just [epithet]!", "&9I knew [epithet] wasn't gone forever!", "&9[epithet] loves you all!",
                    "&9I shall call [epithet]&9 \"Squishy\" and they shall be mine and they shall be my Squishy.", "&9Oh, no. It's [epithet] again.",
                    "&9We seem to have created a [epithet].", "&9Nooo! Why did it have to be [epithet]?!", "&9Hey there, [epithet], kill anyone today?",
                    "&9[epithet] is bigger than you and higher up the food chain. &oGet in [epithet]'s BELLY!!", "&9Say hello to my little friend [epithet]!",
                    "&9First rule of the server is...you don't talk about [epithet].", "&9*DING* Your [epithet] is ready.", "&9Good morning, [epithet]!",
                    "&9Hello, gorgeous [epithet]!", "[epithet], I am your father!", "&9Always let the [epithet] win.", "[epithet] is queen of the world!",
                    "&9Bond. [epithet] Bond.", "&9You're the disease and [epithet] is the cure.", "&9Heeeeeeeeeeere's [epithet]!", "[epithet] had me at 'Hello'.",
                    "&9May the Force be with [epithet].", "&9One [epithet] a day keeps the zombies at bay.", "&9Here's your daily [epithet].",
                    "&9Elementary, my dear [epithet].", "&9Yo, [epithet]!", "&9&oRun! It's &b&o[epithet]&9&o.", "&9One, two, [epithet]'s coming for you.",
                    "&9[epithet], you complete me.", "&9[epithet] will always triumph over good because good is dumb.", "[epithet]...very powerful stuff...",
                    "&9Good Morning, good morning, to [epithet], and you and youuuuu.",
                    "&9My name is [epithet], and no, I'm not a licensed digger, but I have been touched by your blocks...and I'm pretty sure I've touched them." },
            { "[admin]", "&1Oh, jeez! It's [epithet]! Quick! Hide the stuff!", "&1All you have to download Nodus and -- &oOH! &1Hi, [epithet]!",
                    "&1[epithet] is here and ready to administrate the heck out of this server!", "&1[epithet], the best admin the world, is here to help!",
                    "&1Problem Solver [epithet] is on the case!", "[epithet] seigi desu!" }, { "REALDrummer", "&1&l&n&oThe creator is here! The creator is here!" },
            { "Notch", "&1Uh...Notch just connected. ...No, I'm serious, Notch just entered your server! Holy moley!!" } }, default_logout_messages = {
            { "[server]", "&9[epithet] hit Alt+F4.", "&9[epithet] tried to divide by 0.", "&9[epithet] left. We can talk behind their back now.",
                    "&9[epithet] prematurely departed.", "&9[epithet] is sleepin' wit da squids now.", "&9[epithet] warped to another dimension.",
                    "&9[epithet] had cake waiting.", "&9[epithet] vanished in thin air.", "&9[epithet] chose not to be.", "&9[epithet] found something better to do.",
                    "&9[epithet] stopped believing in the god of cubes.", "&9[epithet] stumbled on a round block and couldn't compute.",
                    "&9[epithet] lost their happy thought.", "&9No, [epithet], I expect you to die.", "&9If [epithet] is not back in five minutesďż˝wait longer!",
                    "&9[epithet] has entered orbit.", "&9[epithet] will be back after these messages.",
                    "&9The doctors say [epithet] has a 50/50 chance of surviving, but thereďż˝s only a ten percent chance of that.", "&9[epithet] successfully unloaded",
                    "&9[epithet] core dumped.", "&9[epithet] has experienced a 404 error.", "&9[epithet] received the blue screen of death.",
                    "&9[epithet] was given item #0.", "&9Hasta la vista, [epithet].", "&9[epithet] couldn't handle the truth!", "&9[epithet] will be back.",
                    "&9[epithet]?...[epithet]?...&ocome back!!", "&9Scotty beamed up [epithet].", "&9[epithet] rage-quitted." },
            { "[admin]", "&1[epithet] is gone. We're clear. Now about breaking the rules...",
                    "&1[epithet]'s day of stopping Minecraft-related evil has come to an end for now.", "&1[epithet] found something better to do.",
                    "&1[epithet] has disappeared back inside the server from whence they came." }, { "REALDrummer", "&1Bye, REALDrummer! I'll miss you!" },
            { "Notch", "&1Bye, Notch! Come back soon! ...I still can't believe that was actually Notch!" } };
    private AutoCorrection[] default_AutoCorrections = { new AutoCorrection(" i ", " I "), new AutoCorrection(" ik ", " I know "), new AutoCorrection(" ib ", " I'm back "),
            new AutoCorrection(" ic ", " I see "), new AutoCorrection(" tp ", " teleport "), new AutoCorrection(" idk ", " I don't know "),
            new AutoCorrection(" idc ", " I don't care "), new AutoCorrection(" idgaf ", " I don't give a fuck "),
            new AutoCorrection(" ikr ", " I know, right? ", "?", false), new AutoCorrection(" ikr ", " I know, right "), new AutoCorrection(" lmk ", " let me know "),
            new AutoCorrection(" irl ", " in real life "), new AutoCorrection(" wtf ", " what the fuck? ", "?", false), new AutoCorrection(" wtf ", " what the fuck "),
            new AutoCorrection(" wth ", " what the hell? ", "?", false), new AutoCorrection(" wth ", " what the hell "), new AutoCorrection(" ftw ", " for the win "),
            new AutoCorrection(" y ", " why ", "=", false), new AutoCorrection(" u ", " you "), new AutoCorrection(" ur ", " your "), new AutoCorrection(" r ", " are "),
            new AutoCorrection(" o.o ", " \\o.\\o\\ "), new AutoCorrection(" o ", " oh "), new AutoCorrection(" c: ", " c\\: "), new AutoCorrection(" :c ", " :\\c "),
            new AutoCorrection(" c; ", " c\\; "), new AutoCorrection(" ;c ", " ;\\c "), new AutoCorrection(" c= ", " c\\= "), new AutoCorrection(" =c ", " =\\c "),
            new AutoCorrection(" c ", " see "), new AutoCorrection(" k ", " okay "), new AutoCorrection(" kk ", " okay "), new AutoCorrection(" ic ", " I see "),
            new AutoCorrection(" cya ", " see ya "), new AutoCorrection(" sum1", " someone "), new AutoCorrection(" some1", " someone "),
            new AutoCorrection("every1", "everyone"), new AutoCorrection("any1", "anyone"), new AutoCorrection(" ttyl ", " I'll talk to you later "),
            new AutoCorrection(" wb ", " welcome back "), new AutoCorrection(" ty ", " thank you "), new AutoCorrection(" yw ", " you're welcome "),
            new AutoCorrection(" gb ", " goodbye "), new AutoCorrection(" hb ", " happy birthday "), new AutoCorrection(" gl ", " good luck "),
            new AutoCorrection(" glhf ", " good luck and have fun "), new AutoCorrection(" jk ", " just kidding "), new AutoCorrection(" jking ", " just kidding "),
            new AutoCorrection(" jkjk ", " just kidding "), new AutoCorrection(" np ", " no problem "), new AutoCorrection(" tmi ", " too much information "),
            new AutoCorrection(" omg ", " oh my God "), new AutoCorrection(" omfg ", " oh my fucking God "), new AutoCorrection(" stfu ", " shut the fuck up "),
            new AutoCorrection(" btw ", " by the way "), new AutoCorrection(" ul ", " upload "), new AutoCorrection(" dl ", " download "),
            new AutoCorrection(" i gtg ", " I have to go "), new AutoCorrection(" i g2g ", " I have to go "), new AutoCorrection(" igtg ", " I have to go "),
            new AutoCorrection(" ig2g ", " I have to go "), new AutoCorrection(" gtg ", " I have to go "), new AutoCorrection(" g2g ", " I have to go "),
            new AutoCorrection(" i stg ", " I swear to God "), new AutoCorrection(" i s2g ", " I swear to God "), new AutoCorrection(" istg ", " I swear to God "),
            new AutoCorrection(" is2g ", " I swear to God "), new AutoCorrection(" stg ", " I swear to God "), new AutoCorrection(" s2g ", " I swear to God "),
            new AutoCorrection(" 2nite ", " tonight "), new AutoCorrection(" l8", "late"), new AutoCorrection(" w8", " wait "), new AutoCorrection(" m8", " mate"),
            new AutoCorrection(" 4got ", " forgot "), new AutoCorrection(" 4get ", " forget "), new AutoCorrection(" i brb ", " I'll be right back "),
            new AutoCorrection(" ibrb ", " I'll be right back "), new AutoCorrection(" brb ", " I'll be right back "), new AutoCorrection(" i bbl ", " I'll be back later "),
            new AutoCorrection(" ibbl ", " I'll be back later "), new AutoCorrection(" bbl ", " I'll be back later "), new AutoCorrection(" nvm ", " never mind "),
            new AutoCorrection(" ppl ", " people "), new AutoCorrection(" nm ", " never mind "), new AutoCorrection(" tp ", " teleport "),
            new AutoCorrection(" tpa ", " teleport "), new AutoCorrection(" cuz ", " because "), new AutoCorrection(" plz ", " please "),
            new AutoCorrection(" ppl ", " people "), new AutoCorrection(" thx ", " thanks "), new AutoCorrection(" thnx ", " thanks "),
            new AutoCorrection(" xmas ", " Christmas "), new AutoCorrection(" becuz ", " because "), new AutoCorrection(" sry ", " sorry "),
            new AutoCorrection(" cm ", " Creative Mode "), new AutoCorrection(" cmp ", " Creative multiplayer "), new AutoCorrection(" sm ", " Survival Mode "),
            new AutoCorrection(" smp ", " Survival multiplayer "), new AutoCorrection(" im ", " I'm "), new AutoCorrection(" wont ", " won't "),
            new AutoCorrection(" didnt ", " didn't "), new AutoCorrection(" dont ", " don't "), new AutoCorrection(" cant ", " can't "),
            new AutoCorrection(" wouldnt ", " wouldn't "), new AutoCorrection(" shouldnt ", " shouldn't "), new AutoCorrection(" couldnt ", " couldn't "),
            new AutoCorrection(" isnt ", " isn't "), new AutoCorrection(" aint ", " ain't "), new AutoCorrection(" doesnt ", " doesn't "),
            new AutoCorrection(" youre ", " you're "), new AutoCorrection(" hes ", " he's "), new AutoCorrection(" shes ", " she's "), new AutoCorrection(" hed ", " he'd "),
            new AutoCorrection(" could of ", " could have "), new AutoCorrection(" should of ", "should have "), new AutoCorrection(" would of ", " would have "),
            new AutoCorrection(" itz ", " it's "), new AutoCorrection("wierd", "weird"), new AutoCorrection("recieve", "receive"),
            new AutoCorrection(" blowed up ", " blew up "), new AutoCorrection(" blowed it up ", " blew it up "), new AutoCorrection("!1", "!!"),
            new AutoCorrection("\".", ".\"", "\"", false) };
    // [0] is for important announcements, [1] is for normal announcements, and [2] is for unimportant announcements
    private long[] expiration_times_for_announcements = new long[3];
    // muted players = new HashMap<muted player's name, name of player who muted them or "someone on the console">
    // mail = new HashMap<recipient, ArrayList<messages>>
    private ArrayList<Announcement> announcements = new ArrayList<Announcement>();
    private ArrayList<AutoCorrection> AutoCorrections = new ArrayList<AutoCorrection>();
    public static HashMap<String, String> epithets_by_user = new HashMap<String, String>();
    private HashMap<String, String> muted_players = new HashMap<String, String>(), birthday_players = new HashMap<String, String>(),
            birthday_today = new HashMap<String, String>();
    private HashMap<String, ArrayList<String>> death_messages_by_cause = new HashMap<String, ArrayList<String>>(),
            default_death_messages = new HashMap<String, ArrayList<String>>(), mail = new HashMap<String, ArrayList<String>>(),
            login_messages = new HashMap<String, ArrayList<String>>(), logout_messages = new HashMap<String, ArrayList<String>>();
    private HashMap<CommandSender, String> message_beginnings = new HashMap<CommandSender, String>(), command_beginnings = new HashMap<CommandSender, String>();
    public static ArrayList<String> debuggers = new ArrayList<String>(), AFK_players = new ArrayList<String>(), players_who_have_accepted_the_rules = new ArrayList<String>(),
            players_who_have_read_the_rules = new ArrayList<String>();
    private static String rules = "", default_epithet = "", default_message_format = "";
    private static boolean players_must_accept_rules = true, AutoCorrect_on = true, capitalize_first_letter = true, end_with_period = true, change_all_caps_to_italics = true,
            cover_up_profanities = true, insert_command_usages = true, true_username_required = true, display_death_messages = true;
    private static Plugin Vault = null;
    private static Permission permissions = null;
    private static Economy economy = null;

    // TODO: eliminate spacing addition near color codes
    // TODO: make a final array of recognized emoticons
    // TODO: add ":c" and "c:" to emoticon lists
    // TODO: fix all-caps profanity filtering not stopping the magic
    // TODO: make it so that once players accept the rules, myScribe shows them
    // the announcements
    // TODO: the AutoCorrections go to default no matter what. Fix it.
    // TODO: if there is an asterisk at the end, only cancel first letter
    // capitalizations and ending with periods
    /* TODO: set up config questions for true_username_required and if the true username IS required, should we just not allow them to make the epithet or allow them to make
     * it, but put their true username at the end in parentheses? */
    // TODO: fix abbreviation screwups
    // TODO: make all-caps-to-italics changes capitalize the first letter if the
    // first letter is lowercase
    // TODO: make a HashMap for Bukkit commands and their usages for inserting
    // command usages
    // TODO: make customizable messages that will appear on holidays

    // plugin enable/disable and the command operator
    @Override
    public void onEnable() {
        server = getServer();
        console = server.getConsoleSender();
        // register this class as a listener
        server.getPluginManager().registerEvents(this, this);
        // put together the list of Bukkit command usages

        loadTheAnnouncements(console);
        loadTheAutoCorrections(console);
        loadTheDeathMessages(console);
        loadTheEpithets(console);
        loadTheLoginMessages(console);
        loadTheLogoutMessages(console);
        loadTheRules(console);
        loadTheTemporaryData();
        loadTheBirthdayPeople(console);
        // done enabling
        tellOps(COLOR + enable_messages[(int) (Math.random() * enable_messages.length)], true);
    }

    @Override
    public void onDisable() {
        saveTheAnnouncements(console, true);
        saveTheAutoCorrectSettings(console, true);
        saveTheDeathMessages(console, true);
        saveTheEpithets(console, true);
        saveTheLoginMessages(console, true);
        saveTheLogoutMessages(console, true);
        saveTheRules(console, true);
        saveTheTemporaryData();
        saveTheBirthdayPeople(console, true);
        // done disabling
        tellOps(COLOR + disable_messages[(int) (Math.random() * disable_messages.length)], true);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String command, String[] my_parameters) {
        parameters = my_parameters;
        if ((command.equalsIgnoreCase("myScribe") || command.equalsIgnoreCase("mS"))
                && parameters.length > 1
                && parameters[0].equalsIgnoreCase("load")
                && (parameters[1].toLowerCase().startsWith("e") || (parameters.length > 2 && parameters[1].equalsIgnoreCase("the") && parameters[2].toLowerCase().startsWith(
                        "e")))) {
            if (!(sender instanceof Player) || sender.isOp())
                loadTheEpithets(sender);
            else
                sender.sendMessage(ChatColor.RED + "Sorry, but you're not allowed to use " + COLOR + "/myScribe load" + ChatColor.RED + ".");
            return true;
        } else if ((command.equalsIgnoreCase("myScribe") || command.equalsIgnoreCase("mS"))
                && parameters.length > 1
                && parameters[0].equalsIgnoreCase("load")
                && (parameters[1].toLowerCase().startsWith("a") || (parameters.length > 2 && parameters[1].equalsIgnoreCase("the") && parameters[2].toLowerCase().startsWith(
                        "a")))) {
            if (!(sender instanceof Player) || sender.isOp())
                loadTheAutoCorrections(sender);
            else
                sender.sendMessage(ChatColor.RED + "Sorry, but you're not allowed to use " + COLOR + "/myScribe load" + ChatColor.RED + ".");
            return true;
        } else if ((command.equalsIgnoreCase("myScribe") || command.equalsIgnoreCase("mS"))
                && parameters.length > 1
                && parameters[0].equalsIgnoreCase("load")
                && (parameters[1].toLowerCase().startsWith("d") || (parameters.length > 2 && parameters[1].equalsIgnoreCase("the") && parameters[2].toLowerCase().startsWith(
                        "d")))) {
            if (!(sender instanceof Player) || sender.isOp())
                loadTheDeathMessages(sender);
            else
                sender.sendMessage(ChatColor.RED + "Sorry, but you're not allowed to use " + COLOR + "/myScribe load" + ChatColor.RED + ".");
            return true;
        } else if ((command.equalsIgnoreCase("myScribe") || command.equalsIgnoreCase("mS"))
                && parameters.length > 1
                && parameters[0].equalsIgnoreCase("load")
                && (parameters[1].toLowerCase().startsWith("login")
                        || (parameters.length > 2 && parameters[1].equalsIgnoreCase("the") && parameters[2].toLowerCase().startsWith("login"))
                        || (parameters.length > 2 && parameters[1].equalsIgnoreCase("log") && parameters[2].equalsIgnoreCase("in")) || (parameters.length > 3
                        && parameters[1].equalsIgnoreCase("the") && parameters[2].equalsIgnoreCase("log") && parameters[3].equalsIgnoreCase("in")))) {
            if (!(sender instanceof Player) || sender.isOp())
                loadTheLoginMessages(sender);
            else
                sender.sendMessage(ChatColor.RED + "Sorry, but you're not allowed to use " + COLOR + "/myScribe load" + ChatColor.RED + ".");
            return true;
        } else if ((command.equalsIgnoreCase("myScribe") || command.equalsIgnoreCase("mS"))
                && parameters.length > 1
                && parameters[0].equalsIgnoreCase("load")
                && (parameters[1].toLowerCase().startsWith("logout")
                        || (parameters.length > 2 && parameters[1].equalsIgnoreCase("the") && parameters[2].toLowerCase().startsWith("logout"))
                        || (parameters.length > 2 && parameters[1].equalsIgnoreCase("log") && parameters[2].equalsIgnoreCase("out")) || (parameters.length > 3
                        && parameters[1].equalsIgnoreCase("the") && parameters[2].equalsIgnoreCase("log") && parameters[3].equalsIgnoreCase("out")))) {
            if (!(sender instanceof Player) || sender.isOp())
                loadTheLogoutMessages(sender);
            else
                sender.sendMessage(ChatColor.RED + "Sorry, but you're not allowed to use " + COLOR + "/myScribe load" + ChatColor.RED + ".");
            return true;
        } else if ((command.equalsIgnoreCase("myScribe") || command.equalsIgnoreCase("mS"))
                && parameters.length > 1
                && parameters[0].equalsIgnoreCase("load")
                && (parameters[1].toLowerCase().startsWith("r") || (parameters.length > 2 && parameters[1].equalsIgnoreCase("the") && parameters[2].toLowerCase().startsWith(
                        "r")))) {
            if (!(sender instanceof Player) || sender.isOp())
                loadTheRules(sender);
            else
                sender.sendMessage(ChatColor.RED + "Sorry, but you're not allowed to use " + COLOR + "/myScribe load" + ChatColor.RED + ".");
            return true;
        } else if ((command.equalsIgnoreCase("myScribe") || command.equalsIgnoreCase("mS")) && parameters.length > 0 && parameters[0].equalsIgnoreCase("load")) {
            if (!(sender instanceof Player) || sender.isOp()) {
                loadTheEpithets(sender);
                loadTheAutoCorrections(sender);
                loadTheDeathMessages(sender);
                loadTheLoginMessages(sender);
                loadTheLogoutMessages(sender);
                loadTheRules(sender);
            } else
                sender.sendMessage(ChatColor.RED + "Sorry, but you're not allowed to use " + COLOR + "/myScribe load" + ChatColor.RED + ".");
            return true;
        }
        // TODO add partial saves
        else if ((command.equalsIgnoreCase("myScribe") || command.equalsIgnoreCase("mS")) && parameters.length > 0 && parameters[0].equalsIgnoreCase("save")) {
            if (!(sender instanceof Player) || sender.hasPermission("myscribe.admin")) {
                saveTheEpithets(sender, true);
                saveTheAutoCorrectSettings(sender, true);
                saveTheDeathMessages(sender, true);
                saveTheLoginMessages(sender, true);
                saveTheLogoutMessages(sender, true);
                saveTheRules(sender, true);
            } else
                sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/myScribe save" + ChatColor.RED + ".");
            return true;
        } else if ((command.equalsIgnoreCase("myScribe") || command.equalsIgnoreCase("mS")) && parameters.length > 0 && parameters[0].equalsIgnoreCase("debug")) {
            if (sender instanceof ConsoleCommandSender || sender.hasPermission("myscribe.admin")) {
                String sender_name = "console";
                if (sender instanceof Player)
                    sender_name = ((Player) sender).getName();
                if (debuggers.contains(sender_name)) {
                    debuggers.remove(sender_name);
                    sender.sendMessage(COLOR + "These striptual mistakes have been remedied.");
                } else {
                    debuggers.add(sender_name);
                    sender.sendMessage(COLOR + "The time has come to write a few wrong of this code.");
                }
            } else
                sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/myScribe debug" + ChatColor.RED + ".");
            return true;
        } else if (command.equalsIgnoreCase("epithet") || command.equalsIgnoreCase("nick")) {
            if (parameters.length > 0 && (!(sender instanceof Player) || sender.hasPermission("myscribe.epithet")))
                changeEpithet(sender);
            else if (parameters.length > 0)
                sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to change your epithet.");
            else
                sender.sendMessage(ChatColor.RED + "You forgot to tell me what I should change your epithet to!");
            return true;
        } else if (command.equalsIgnoreCase("correct")) {
            if (parameters.length >= 2 && (!(sender instanceof Player) || sender.hasPermission("myscribe.correct") || sender.hasPermission("myscribe.admin")))
                addCorrection(sender);
            else if (sender instanceof Player && !sender.hasPermission("myscribe.correct"))
                sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to create your own AutoCorrections.");
            else
                sender.sendMessage(ChatColor.RED + "You forgot to tell me what correction you want to make!");
            return true;
        } else if (command.equalsIgnoreCase("afklist") || (command.equalsIgnoreCase("afk") && parameters.length > 0 && parameters[0].equalsIgnoreCase("list"))) {
            AFKList(sender, false);
            return true;
        } else if (command.equalsIgnoreCase("afk") && parameters.length > 0) {
            AFKCheck(sender);
            return true;
        } else if (command.equalsIgnoreCase("afk")) {
            if (sender instanceof Player)
                AFKToggle(sender);
            else
                sender.sendMessage(ChatColor.RED + "You're a console! You can't be away from the keyboard! You're IN the computer!");
            return true;
        } else if (command.equalsIgnoreCase("rules")) {
            if (!rules.equals("")) {
                if (sender instanceof Player)
                    players_who_have_read_the_rules.add(sender.getName());
                sender.sendMessage(colorCode(rules));
            } else
                sender.sendMessage(ChatColor.RED + "As I said, the rules haven't been written down yet, so you can't read them right now. Sorry.");
            return true;
        } else if (command.equalsIgnoreCase("accept") || command.equalsIgnoreCase("acceptrules") || command.equalsIgnoreCase("accepttherules")) {
            if (!(sender instanceof Player) || sender.isOp())
                sender.sendMessage(ChatColor.RED + "You don't need to accept the rules! " + ChatColor.ITALIC + "You made the rules!");
            else if (players_who_have_read_the_rules.contains(sender.getName()) && !players_who_have_accepted_the_rules.contains(sender.getName())) {
                players_who_have_accepted_the_rules.add(sender.getName());
                server.broadcastMessage(COLOR + sender.getName() + " has just accepted the rules! Everyone welcome them to the server!");
                sender.sendMessage(COLOR + "Remember the rules and have fun! You can read the rules again any time with /rules.");

                // TODO
            } else if (players_who_have_accepted_the_rules.contains(sender.getName()))
                sender.sendMessage(COLOR + "You've already accepted the rules. You're good to go. Have fun.");
            else if (!rules.equals(""))
                sender.sendMessage(colorCode("&cYou haven't even &oread%o the rules! I know you didn't! Read 'em!"));
            else
                sender.sendMessage(ChatColor.RED + "I already told you: you don't have to accept the rules right now. They haven't been written down yet.");
            return true;
        } else if (command.toLowerCase().startsWith("color") || command.equalsIgnoreCase("codes")) {
            sender.sendMessage(colorCode("&00 &11 &22 &33 &44 &55 &16 &77 &88 &99 &aa &bb &cc &dd &9e &ff &kk&f(k) &f&ll&f &mm&f &nn&f &oo"));
            return true;
        } else if (command.equalsIgnoreCase("announce") || command.equalsIgnoreCase("declare") || command.equalsIgnoreCase("decree")) {
            if ((!(sender instanceof Player) || sender.hasPermission("myscribe.announce.unimportant") || sender.hasPermission("myscribe.announce") || sender
                    .hasPermission("myscribe.announce.important"))
                    && parameters.length > 0)
                announce(sender);
            else if (parameters.length > 0)
                sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/" + command.toLowerCase() + ChatColor.RED + ".");
            else
                sender.sendMessage(ChatColor.RED + "You forgot to tell me what you want to announce!");
        } else if (command.equals("trade") || command.equals("exchange")) {
            // TODO
        } else if (command.equalsIgnoreCase("login") || command.equalsIgnoreCase("loginmessage")) {
            if (sender instanceof Player && !sender.hasPermission("myscribe.loginmessage")) {
                sender.sendMessage(ChatColor.RED + "Sorry, but you're not allowed to use " + COLOR + "/" + command.toLowerCase() + ChatColor.RED + ".");
                return true;
            }
            String message = "", target = "[server]";
            int extra_param = 0;
            if (parameters[0].toLowerCase().startsWith("for:")) {
                target = parameters[0].substring(4);
                extra_param++;
            }
            if (parameters.length <= extra_param) {
                sender.sendMessage(ChatColor.RED + "You forgot to tell me what you want the new login message to say!");
                return true;
            }
            for (int i = extra_param; i < parameters.length; i++)
                message += parameters[i] + " ";
            if (!message.contains("\\[player\\]") && !message.contains("\\[epithet]\\")) {
                sender.sendMessage(ChatColor.RED + "You forgot to tell me where you want the player's username to go in the message!");
                sender.sendMessage(ChatColor.RED
                        + "Just put \"[player]\" in the message to indicate where you want the player's username to go or \"[epithet]\" to indicate where the player's epithet should go.");
                return true;
            }
            ArrayList<String> messages = login_messages.get(sender.getName());
            // use .substring() to eliminate the extra space at the end
            messages.add(message.substring(0, message.length() - 1));
            login_messages.put(target, messages);
            sender.sendMessage(COLOR + "All right. I've added \"" + ChatColor.WHITE + colorCode(message) + COLOR + "\" to the list of login messages.");
            // get sender's friendly name
            String sender_name = "someone on the console";
            if (sender instanceof Player)
                sender_name = sender.getName();
            // inform the ops of the change
            tellOps(COLOR + sender_name + " added a new login message.\n\"" + colorCode(message) + COLOR + "\"", sender instanceof Player, sender.getName());
            return true;
        } else if (command.equalsIgnoreCase("mute") || command.equalsIgnoreCase("silence")) {
            if (sender instanceof Player && !sender.hasPermission("myscribe.mute"))
                sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/" + command.toLowerCase() + ChatColor.RED + ".");
            else if (parameters.length == 0)
                sender.sendMessage(ChatColor.RED + "You forgot to tell me who you want to mute!");
            else {
                String target = getFullName(parameters[0]);
                if (target == null)
                    sender.sendMessage(ChatColor.RED + "I can't find anyone named \"" + parameters[0] + "\".");
                else if (muted_players.containsKey(target))
                    sender.sendMessage(ChatColor.RED + muted_players.get(target) + " already muted " + target + ".");
                else {
                    // get sender's friendly name
                    String sender_name;
                    if (sender instanceof Player)
                        sender_name = sender.getName();
                    else
                        sender_name = "someone on the console";
                    // mute the specified player
                    muted_players.put(target, sender_name);
                    sender.sendMessage(COLOR + target + " has been muted.");
                    // capitalize the first letter of sender's name and tell the other ops what happened
                    if (sender_name.length() > 1)
                        sender_name = sender_name.substring(0, 1).toUpperCase() + sender_name.substring(1);
                    else
                        sender_name = sender_name.toUpperCase();
                    tellOps(COLOR + sender_name + " muted " + target + ".", sender instanceof Player, sender.getName());
                }
            }
            return true;
        } else if (command.equalsIgnoreCase("unmute") || command.equalsIgnoreCase("unsilence")) {
            if (sender instanceof Player && !sender.hasPermission("myscribe.unmute"))
                sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/" + command.toLowerCase() + ChatColor.RED + ".");
            else if (parameters.length == 0)
                sender.sendMessage(ChatColor.RED + "You forgot to tell me who you want to unmute!");
            else {
                String target = getFullName(parameters[0]);
                if (target == null)
                    sender.sendMessage(ChatColor.RED + "I can't find anyone named \"" + parameters[0] + "\".");
                else if (!muted_players.containsKey(target))
                    sender.sendMessage(ChatColor.RED + "No one ever muted " + target + ".");
                else {
                    // get sender's friendly name with the first letter capitalized to begin the sentence
                    String sender_name = "someone on the console";
                    if (sender instanceof Player)
                        sender_name = sender.getName();
                    // unmute the specified player
                    muted_players.remove(target);
                    sender.sendMessage(COLOR + target + " has been unmuted.");
                    tellOps(COLOR + sender_name + " unmuted " + target + ".", sender instanceof Player, sender.getName());
                }
            }
            return true;
        } else if (command.equalsIgnoreCase("bd") || command.equalsIgnoreCase("setbd") || command.equalsIgnoreCase("birthday") || command.equalsIgnoreCase("setbirthday")
                || command.equalsIgnoreCase("birthdate") || command.equalsIgnoreCase("setbirthdate")) {
            if (sender instanceof Player && birthday_players.containsKey(sender) && !sender.isOp()) {
                sender.sendMessage(ChatColor.RED + "I'm sorry, but you've already set your birthday");
            } else if (sender instanceof Player && parameters.length == 2) {
                if (parameters[0].length() == 1) {
                    parameters[0] = "0" + parameters[0];
                }
                if (parameters[1].length() == 1) {
                    parameters[1] = "0" + parameters[1];
                }
                if (parameters[0].length() != 2) {
                    sender.sendMessage("Sorry, but you can't have a day with " + parameters[0].length() + "units.");
                }
                if (parameters[1].length() != 2) {
                    sender.sendMessage("Sorry, but you can't have a day with " + parameters[1].length() + "units.");
                }

                String temp = parameters[0] + "/" + parameters[1];
                birthday_players.put(sender.getName(), temp);
            } else if (sender instanceof Player && parameters.length == 3 && sender.isOp()) {
                if (parameters[1].length() == 1) {
                    parameters[1] = "0" + parameters[1];
                }
                if (parameters[2].length() == 1) {
                    parameters[2] = "0" + parameters[2];
                }
                if (parameters[1].length() != 2) {
                    sender.sendMessage("Sorry, but you can't have a day with " + parameters[1].length() + "units.");
                }
                if (parameters[2].length() != 2) {
                    sender.sendMessage("Sorry, but you can't have a day with " + parameters[2].length() + "units.");
                }

                String temp = parameters[1] + "/" + parameters[2];
                birthday_players.put(getFullName(parameters[0]), temp);
            }
            return true;
        } else if (command.equalsIgnoreCase("say")) {
            if (sender instanceof Player && !sender.isOp())
                sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/say" + ChatColor.RED + ".");
            else if (parameters.length == 0)
                sender.sendMessage(ChatColor.RED + "You forgot to tell me what you want to say!");
            else {
                String message = parameters[0], epithet = epithets_by_user.get("console");
                if (parameters.length > 1)
                    for (int i = 1; i < parameters.length; i++)
                        message += " " + parameters[i];
                if (epithet == null)
                    epithet = "&dGod";
                server.broadcastMessage(colorCode(replaceAll(default_message_format, "[epithet]", colorCode(epithet), "[message]", colorCode(AutoCorrect(sender, message)))));
            }
            return true;
        }
        return false;
    }

    // String utils
    private String AutoCorrect(CommandSender sender, String message) {
        try {
            if (AutoCorrect_on && !message.startsWith("http") && !message.startsWith("www.")) {
                // Remove accidental...
                while (// ...slashes that are not part of an emoticon;...
                message.endsWith("/") && !message.endsWith(":/") && !message.endsWith("=/")
                        // ...backslashes that are not part of an emoticon;...
                        || message.endsWith("\\") && !message.endsWith(":\\") && !message.endsWith("=\\")
                        // ...greater than or less than signs that are not part of an emoticon;...
                        || (message.endsWith(">") || message.endsWith("<"))
                        && !(message.toCharArray()[message.length() - 3] == '>' || message.toCharArray()[message.length() - 3] == '<')
                        // ...extra periods (as there should never be more than four);...
                        || message.endsWith(".....")
                        // ...and random commas, semicolons, or colons.
                        || message.endsWith(",") || message.endsWith(";") || message.endsWith(":"))
                    message = message.substring(0, message.length() - 1);

                // use spaces to make punctuation separate words
                String[] do_not_add_spaces_chars = { " ", "'", "\\", "-", "_", "." };
                if (message.length() > 1)
                    for (int i = 1; i < message.length(); i++) {
                        char[] chars = message.toCharArray();
                        // temporarily separate two characters with a space for AutoCorrections if...
                        if (// ...one is a number or letter and the other is not;...
                        (!isNumberOrLetter(chars[i - 1]) && isNumberOrLetter(chars[i]) || isNumberOrLetter(chars[i - 1]) && !isNumberOrLetter(chars[i]) || chars[i - 1] == '.'
                                && !isNumberOrLetter(chars[i]))
                                // ...neither character is a space or one of the "do_not_add_spaces" characters;...
                                && !contains(do_not_add_spaces_chars, chars[i - 1])
                                && !contains(do_not_add_spaces_chars, chars[i])
                                // ...the non-letter/number is not a period in an abbreviation, hyperlink, or decimal;...
                                && !(chars[i - 1] == '.' && (isNumberOrLetter(chars[i]) || chars[i] == ' ' && i >= 3 && message.substring(i - 3, i - 2).equals(".")))
                                && !(chars[i] == '.' && (message.length() >= i + 2 && isNumberOrLetter(chars[i + 1]) || i >= 2 && message.substring(i - 2, i - 1).equals(".")))
                                // ...the non-letter/number is not a comma in a number;...
                                && !(chars[i - 1] == ',' && (isNumberOrLetter(chars[i])
                                        && String.valueOf(chars[i]).toLowerCase().equals(String.valueOf(chars[i]).toUpperCase()) || i >= 2
                                        && isNumberOrLetter(message.substring(i - 2, i - 1))
                                        && message.substring(i - 2, i - 1).toLowerCase().equals(message.substring(i - 2, i - 1).toUpperCase())))
                                && !(chars[i] == ',' && (message.length() >= i + 2 && isNumberOrLetter(chars[i + 1])
                                        && Character.toLowerCase(chars[i]) == Character.toUpperCase(chars[i + 1]) || isNumberOrLetter(message.substring(i - 1, i))
                                        && Character.toLowerCase(chars[i - 1]) == Character.toUpperCase(chars[i - 1])))
                                // ...the two characters aren't an emoticon;...
                                && !((chars[i - 1] == ':' || chars[i - 1] == ';' || chars[i - 1] == '=') && (i + 1 >= message.length() || chars[i + 1] == ' '))
                                && !(String.valueOf(chars[i - 1]) + String.valueOf(chars[i])).equals("<3")
                                // ...and the two characters aren't a color code.
                                && !isColorCode(String.valueOf(chars[i - 1]) + String.valueOf(chars[i]), null, null))
                            message = message.substring(0, i) + " " + message.substring(i);
                    }
                message = " " + message + " ";

                // perform AutoCorrections
                for (AutoCorrection correction : AutoCorrections)
                    if (sender instanceof ConsoleCommandSender || correction.target == null || correction.target.startsWith("[") && correction.target.endsWith("]")
                            && permissions != null && correction.target.equals("[" + permissions.getPrimaryGroup((Player) sender) + "]") || !correction.target.startsWith("[")
                            && !correction.target.endsWith("]") && sender.getName().equals(correction.target))
                        message = correct(message, correction, true);
                String[] words = message.split(" ");

                // change all capital words to italics
                if (change_all_caps_to_italics && !message.startsWith("*") && !message.endsWith("*")) {
                    for (int i = 0; i < words.length; i++) {
                        // temporarily eliminate color codes
                        String word = decolor(words[i]);
                        // change the word to lowercase italics if...
                        // ...the word has no backslashes (used to cancel AutoCorrections) and...
                        if (!word.contains("\\") && (
                        // ...EITHER the word before it is italicized OR...
                                /* TODO: checking the last word for italics only won't cut it because the word(s) before it may have been italicized, so the "&o" color code
                                 * may be further back */
                                i > 0 && words[i - 1].toLowerCase().contains("&o")
                                // ...the word is longer than one letter;...
                                || word.length() > 1
                                // ...the word is not an abbreviation;...
                                        && replaceAll(word, ".", "").length() * 2 != word.length()
                                        // ...there are no underscores (found in usernames or emoticons);...
                                        && !word.contains("_")
                                        // ...the word is all caps (of course);...
                                        && word.equals(word.toUpperCase())
                                        // ...the word actually contains letters;...
                                        && !word.toLowerCase().equals(word)
                                        // ...the word doesn't have a length of two and start or end with an "X", "x", ":", ";", or "=" (emoticons);...
                                        && (word.length() != 2 || !word.toUpperCase().startsWith("X") && !word.toUpperCase().endsWith("X") && !word.startsWith(":")
                                                && !word.endsWith(":") && !word.startsWith(";") && !word.endsWith(";") && !word.startsWith("=") && !word.endsWith("="))
                                        // ...the word doesn't have a length of three and start or end with a ">:", ">;", ">X", ">x", or ">=" (angry emoticons);...
                                        && (word.length() != 3 || !word.toUpperCase().startsWith(">X") && !word.toUpperCase().endsWith("X<") && !word.startsWith(">:")
                                                && !word.endsWith(":<") && !word.startsWith(">;") && !word.endsWith(";<") && !word.startsWith(">=") && !word.endsWith("=<"))
                                        // ...and the word isn't a Roman numeral
                                        && readRomanNumeral(word) == 0)) {
                            // if it's the last word in the message and it doesn't have terminal punctuation, add an "!"
                            if (i == words.length - 1 && !word.endsWith(".") && !word.endsWith("!") && !word.endsWith("?") && !word.endsWith(".\"") && !word.endsWith("!\"")
                                    && !word.endsWith("?\""))
                                words[i] = words[i] + "!";
                            words[i] = "&o" + words[i] + "%o";
                            // only change the word to lower case if it's not an abbreviation
                            if (word.length() <= 1 || replaceAll(word, ".", "").length() * 2 != word.length())
                                words[i] = words[i].toLowerCase();
                        }
                    }
                }
                // reconstruct the message from the words
                message = "";
                for (String word : words)
                    if (message.equals(""))
                        message = word;
                    else
                        message = message + " " + word;
                if (cover_up_profanities) {
                    message = replaceAll(message, " ass ", " a&kss%k ");
                    for (String profanity : profanities)
                        message = replaceAll(message, profanity, profanity.substring(0, 1) + "&k" + profanity.substring(1) + "%k");
                }
                message = replaceAll(message, "./ ", "./");
                if (insert_command_usages)
                    for (int i = 0; i < message.length() - 1; i++) {
                        if (!message.contains("./"))
                            break;
                        if (message.substring(i, i + 2).equals("./")) {
                            int end_index = i + 2;
                            while (end_index < message.length())
                                if (!message.substring(end_index, end_index + 1).toLowerCase().equals(message.substring(end_index, end_index + 1).toUpperCase()))
                                    end_index++;
                                else
                                    try {
                                        Integer.parseInt(message.substring(end_index, end_index + 1));
                                        end_index++;
                                    } catch (NumberFormatException exception) {
                                        break;
                                    }
                            PluginCommand command = server.getPluginCommand(message.substring(i + 2, end_index).toLowerCase());
                            if (command != null) {
                                char color_code = 'f';
                                if (command.getPlugin().getName().equals("myScribe"))
                                    color_code = '9';
                                else if (command.getPlugin().getName().equals("myUltraWarps"))
                                    color_code = 'a';
                                else if (command.getPlugin().getName().equals("myZeus"))
                                    color_code = 'b';
                                else if (command.getPlugin().getName().equals("myGuardDog"))
                                    color_code = 'e';
                                else if (command.getPlugin().getName().equals("myCarpet"))
                                    color_code = '6';
                                else if (command.getPlugin().getName().equals("myOpAids"))
                                    color_code = '7';
                                else if (command.getPlugin().getName().equals("myGroundsKeeper"))
                                    color_code = '2';
                                String usage = "&" + color_code + replaceAll(command.getUsage(), "<command>", command.getName()) + "%" + color_code;
                                if (command.getAliases().size() > 0) {
                                    usage = usage + " (";
                                    for (String alias : command.getAliases()) {
                                        if (!usage.endsWith("("))
                                            usage = usage + " ";
                                        usage = usage + "or &" + color_code + "/" + alias + "%" + color_code;
                                    }
                                    usage = usage + ")";
                                }
                                message = replaceAll(message, "./" + message.substring(i + 2, end_index), usage);
                            }
                        }
                    }
                // eliminate extra spaces between letters and punctuation
                message = replaceAll(replaceAll(message, "... ", "..."), "/ ", "/");
                int quote_counter = 0;
                for (int i = 1; i < message.length(); i++)
                    if (!isNumberOrLetter(message.substring(i, i + 1)) && (i + 2 > message.length() || !isColorCode(message.substring(i, i + 2), null, null))) {
                        if (message.substring(i, i + 1).equals("\""))
                            quote_counter++;
                        // eliminate spaces before punctuation except in the case of the ones listed
                        if (!message.substring(i, i + 1).equals("(") && !message.substring(i, i + 1).equals("[") && !message.substring(i, i + 1).equals("{")
                                && !message.substring(i, i + 1).equals("\\") && !message.substring(i, i + 1).equals("+") && !message.substring(i, i + 1).equals("/")
                                && !(message.substring(i, i + 1).equals("\"") && quote_counter % 2 == 1))
                            while (i > 0 && message.substring(i - 1, i).equals(" "))
                                message = message.substring(0, i - 1) + message.substring(i);
                        else if (message.substring(i, i + 1).equals("/")) {
                            int end_index = i + 1;
                            while (end_index < message.length() && isNumberOrLetter(message.substring(end_index, end_index + 1)))
                                end_index++;
                            // TODO: change the list of commands here to
                            // Bukkit_command_usages.keySet().contains(message.substring(i+1,
                            // end_index)
                            if (server.getPluginCommand(message.substring(i + 1, end_index)) == null
                                    && !(message.substring(i + 1, end_index).equalsIgnoreCase("version") || message.substring(i + 1, end_index).equalsIgnoreCase("plugins")
                                            || message.substring(i + 1, end_index).equalsIgnoreCase("reload")
                                            || message.substring(i + 1, end_index).equalsIgnoreCase("timings") || message.substring(i + 1, end_index).equalsIgnoreCase("tell")
                                            || message.substring(i + 1, end_index).equalsIgnoreCase("kill") || message.substring(i + 1, end_index).equalsIgnoreCase("me")
                                            || message.substring(i + 1, end_index).equalsIgnoreCase("help") || message.substring(i + 1, end_index).equalsIgnoreCase("?")
                                            || message.substring(i + 1, end_index).equalsIgnoreCase("kick") || message.substring(i + 1, end_index).equalsIgnoreCase("ban")
                                            || message.substring(i + 1, end_index).equalsIgnoreCase("banlist")
                                            || message.substring(i + 1, end_index).equalsIgnoreCase("pardon")
                                            || message.substring(i + 1, end_index).equalsIgnoreCase("ban-ip")
                                            || message.substring(i + 1, end_index).equalsIgnoreCase("pardon-ip") || message.substring(i + 1, end_index).equalsIgnoreCase("op")
                                            || message.substring(i + 1, end_index).equalsIgnoreCase("deop") || message.substring(i + 1, end_index).equalsIgnoreCase("tp")
                                            || message.substring(i + 1, end_index).equalsIgnoreCase("give") || message.substring(i + 1, end_index).equalsIgnoreCase("stop")
                                            || message.substring(i + 1, end_index).equalsIgnoreCase("save-all")
                                            || message.substring(i + 1, end_index).equalsIgnoreCase("save-off")
                                            || message.substring(i + 1, end_index).equalsIgnoreCase("save-on") || message.substring(i + 1, end_index).equalsIgnoreCase("list")
                                            || message.substring(i + 1, end_index).equalsIgnoreCase("say")
                                            || message.substring(i + 1, end_index).equalsIgnoreCase("whitelist")
                                            || message.substring(i + 1, end_index).equalsIgnoreCase("time")
                                            || message.substring(i + 1, end_index).equalsIgnoreCase("gamemode") || message.substring(i + 1, end_index).equalsIgnoreCase("xp")
                                            || message.substring(i + 1, end_index).equalsIgnoreCase("toggledownfall")
                                            || message.substring(i + 1, end_index).equalsIgnoreCase("defaultgamemode") || message.substring(i + 1, end_index)
                                            .equalsIgnoreCase("seed")))
                                while (i > 0 && message.substring(i - 1, i).equals(" "))
                                    message = message.substring(0, i - 1) + message.substring(i);
                        } else if (message.substring(i, i + 1).equals("\"") || message.substring(i, i + 1).equals("(") || message.substring(i, i + 1).equals("[")
                                || message.substring(i, i + 1).equals("{"))
                            while (i < message.length() - 1 && message.substring(i + 1, i + 2).equals(" "))
                                if (i == message.length() - 2)
                                    message = message.substring(0, i + 1);
                                else
                                    message = message.substring(0, i + 1) + message.substring(i + 2);
                    }
                message = replaceAll(message, "  ", " ");
                message = replaceAll(replaceAll(replaceAll(message, "%o.", ".%o"), "%o!", "!%o"), "%o?", "?%o");
                while (message.length() >= 2 && isColorCode(message.substring(message.length() - 2), null, null))
                    message = message.substring(0, message.length() - 2);
                // capitalize the first letter of every sentence if it is not a correction
                if (capitalize_first_letter && !message.startsWith("*") && !message.endsWith("*")) {
                    message = ". " + message;
                    for (int i = 0; i < message.length(); i++) {
                        String check_message = message.substring(i).trim();
                        // locate terminal punctuation and make sure that the thing after them isn't an emoticon or a website U.R.L.
                        if ((message.substring(i, i + 1).equals(".")
                                && !(message.length() > i + 1 && isNumberOrLetter(message.substring(i + 1, i + 2)) && !(message.length() > i + 2 && message.substring(i + 2,
                                        i + 3).equals("."))) || message.substring(i, i + 1).equals("!") || message.substring(i, i + 1).equals("?"))
                                && !check_message.startsWith(":")
                                && !check_message.startsWith(";")
                                && !check_message.startsWith("=")
                                && !(check_message.length() >= 3 && check_message.substring(0, 1).equalsIgnoreCase(check_message.substring(2, 3)))) {
                            while (i < message.length() - 1)
                                // if it's not a letter or number, skip it
                                if (message.substring(i, i + 1).toUpperCase().equals(message.substring(i, i + 1).toLowerCase()) && !message.substring(i, i + 1).equals("0")
                                        && !message.substring(i, i + 1).equals("1") && !message.substring(i, i + 1).equals("2") && !message.substring(i, i + 1).equals("3")
                                        && !message.substring(i, i + 1).equals("4") && !message.substring(i, i + 1).equals("5") && !message.substring(i, i + 1).equals("6")
                                        && !message.substring(i, i + 1).equals("7") && !message.substring(i, i + 1).equals("8") && !message.substring(i, i + 1).equals("9")) {
                                    // skip two characters if it's a color code
                                    if (i < message.length() - 2 && isColorCode(message.substring(i, i + 2), null, null))
                                        i++;
                                    i++;
                                }
                                // both of these stop the loop, but if i=message.length+1, it means it found a "\" before the letter, so it should cancel
                                // capitalization
                                else if (message.substring(i, i + 1).equals("\\"))
                                    i = message.length() + 1;
                                else
                                    break;
                            // don't capitalize after an ellipsis or a "\" (with the presence of a "\" indicated with i==message.length()+1)
                            if (i < message.length() + 1 && (i < 3 || !message.substring(i - 3, i).equals("...")))
                                if (i + 1 == message.length())
                                    message = message.substring(0, i) + message.substring(i, i + 1).toUpperCase();
                                else
                                    message = message.substring(0, i) + message.substring(i, i + 1).toUpperCase() + message.substring(i + 1);
                        }
                    }
                    message = message.substring(2);
                }
                // end lines with a period if no terminal punctuation exists and the message doesn't start with or end with a * (correction) and the message's last
                // or second to last characters are not colons (emoticons)
                if (end_with_period
                        && !message.endsWith(".")
                        && !message.endsWith("!")
                        && !message.endsWith("?")
                        && !message.endsWith(".\"")
                        && !message.endsWith("!\"")
                        && !message.endsWith("?\"")
                        && !message.endsWith("*")
                        && !message.startsWith("*")
                        && message.length() > 0
                        && !(message.endsWith(":")
                                || message.endsWith("=")
                                || message.endsWith(";")
                                || (message.length() >= 2 && (message.toCharArray()[message.length() - 2] == ':' || message.toCharArray()[message.length() - 2] == '=' || message
                                        .toCharArray()[message.length() - 2] == ';')) || message.toUpperCase().endsWith("XD") || message.endsWith("<3") || message.length() >= 3
                                && message.substring(message.length() - 1).equalsIgnoreCase(message.substring(message.length() - 3, message.length() - 2)))
                        && !message.endsWith("\\"))
                    message = message + "%k.";
            }
            // get rid of all color codes immediately followed by an anti color code of the same kind
            String[] color_color_code_chars = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" }, formatting_color_code_chars =
                    { "k", "l", "m", "n", "o", "r" };
            for (String color_code_char : color_color_code_chars)
                message =
                        replaceAll(message, "%" + color_code_char + "&" + color_code_char, "", "&" + color_code_char + "%" + color_code_char, "", "%" + color_code_char + " &"
                                + color_code_char, " ", "&" + color_code_char + " %" + color_code_char, " ");
            for (String color_code_char : formatting_color_code_chars)
                message =
                        replaceAll(message, "%" + color_code_char + "&" + color_code_char, "", "&" + color_code_char + "%" + color_code_char, "", "%" + color_code_char + " &"
                                + color_code_char, " ", "&" + color_code_char + " %" + color_code_char, " ");
            return replace(message, "\\", "", "\\", false, true);
        } catch (Exception e) {
            processException("There was a problem in the AutoCorrect algorithm!", e);
            return "";
        }
    }

    public static int readRomanNumeral(String roman_numeral) {
        int value = 0;
        char[] chars = new char[] { 'M', 'D', 'C', 'L', 'X', 'V', 'I' };
        int[] values = new int[] { 1000, 500, 100, 50, 10, 5, 1 };
        while (roman_numeral.length() > 0) {
            char[] digits = roman_numeral.trim().toUpperCase().toCharArray();
            int digit_value = 0;
            for (int i = 0; i < chars.length; i++)
                if (digits[0] == chars[i])
                    digit_value = values[i];
            if (digit_value == 0)
                return 0;
            int zeroless = digit_value;
            while (zeroless >= 10)
                zeroless = zeroless / 10;
            if (digits[0] != chars[0] && zeroless == 1 && digits.length > 1) {
                // if the digit value starts with a 1 and it's not 'M', it could be being used to subtract from the subsequent digit (e.g. "IV"); however, this
                // can only be true if the subsequent digit has a greater value than the current one
                int next_digit_value = 0;
                for (int i = 0; i < chars.length; i++)
                    if (digits[1] == chars[i])
                        next_digit_value = values[i];
                if (next_digit_value == 0)
                    return 0;
                // so, if the current digit's value is less than the subsequent digit's value, the current digit's value must be subtracted, not added
                if (next_digit_value > digit_value)
                    value -= digit_value;
                else
                    value += digit_value;
            } else
                value += digit_value;
            roman_numeral = roman_numeral.substring(1);
        }
        return value;
    }

    private static boolean contains(Object[] objects, Object target) {
        for (Object object : objects)
            if (object.equals(target))
                return true;
        return false;
    }

    private static String colorCode(String text) {
        // put color codes in the right order if they're next to each other
        for (int i = 0; i < text.length() - 3; i++)
            if (isColorCode(text.substring(i, i + 2), false, true) && isColorCode(text.substring(i + 2, i + 4), true, true))
                text = text.substring(0, i) + text.substring(i + 2, i + 4) + text.substring(i, i + 2) + text.substring(i + 4);
        // replace all anti color codes with non antis
        String current_color_code = "";
        for (int i = 0; i < text.length() - 1; i++) {
            if (isColorCode(text.substring(i, i + 2), null, true))
                current_color_code = current_color_code + text.substring(i, i + 2);
            else if (isColorCode(text.substring(i, i + 2), null, false)) {
                while (text.length() > i + 2 && isColorCode(text.substring(i, i + 2), null, false)) {
                    current_color_code = replaceAll(current_color_code, "&" + text.substring(i + 1, i + 2), "");
                    if (current_color_code.equals(""))
                        current_color_code = "&f";
                    text = text.substring(0, i) + text.substring(i + 2);
                }
                text = text.substring(0, i) + current_color_code + text.substring(i);
            }
        }
        String colored_text = ChatColor.translateAlternateColorCodes('&', text);
        return colored_text;
    }

    private static Boolean isColorCode(String text, Boolean true_non_formatting_null_either, Boolean true_non_anti_null_either) {
        if (!text.startsWith("&") && !text.startsWith("%"))
            return false;
        if (true_non_anti_null_either != null)
            if (true_non_anti_null_either && text.startsWith("%"))
                return false;
            else if (!true_non_anti_null_either && text.startsWith("&"))
                return false;
        String[] color_color_code_chars = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" }, formatting_color_code_chars =
                { "k", "l", "m", "n", "o", "r" };
        if (true_non_formatting_null_either == null || true_non_formatting_null_either)
            for (String color_color_code_char : color_color_code_chars)
                if (text.substring(1, 2).equalsIgnoreCase(color_color_code_char))
                    return true;
        if (true_non_formatting_null_either == null || !true_non_formatting_null_either)
            for (String formatting_color_code_char : formatting_color_code_chars)
                if (text.substring(1, 2).equalsIgnoreCase(formatting_color_code_char))
                    return true;
        return false;
    }

    private static String decolor(String text) {
        if (!text.contains("&") && !text.contains("%"))
            return text;
        for (int i = 0; i < text.length() - 2; i++) {
            if (isColorCode(text.substring(i, i + 2), null, null)) {
                if (i + 2 < text.length())
                    text = text.substring(0, i) + text.substring(i + 2);
                else
                    text = text.substring(0, i);
                i -= 2;
                if (i < -1)
                    i = -1;
            }
        }
        return text;
    }

    private static boolean isNumberOrLetter(String character) {
        if (
        // if the character's lowercase form is different from its uppercase form, it must be a letter since only letters have cases
        !character.toLowerCase().equals(character.toUpperCase())
                // just check for numbers manually; it could only be one of 10 digits and Integer.parseInt() is inefficient for this task
                || character.equals("0") || character.equals("1") || character.equals("2") || character.equals("3") || character.equals("4") || character.equals("5")
                || character.equals("6") || character.equals("7") || character.equals("8") || character.equals("9"))
            return true;
        return false;
    }

    private static boolean isNumberOrLetter(char character) {
        if (
        // if the character's lowercase form is different from its uppercase form, it must be a letter since only letters have cases
        !String.valueOf(character).toLowerCase().equals(String.valueOf(character).toUpperCase())
                // just check for numbers manually; it could only be one of 10 digits and Integer.parseInt() is inefficient for this task
                || character == '0' || character == '1' || character == '2' || character == '3' || character == '4' || character == '5' || character == '6'
                || character == '7' || character == '8' || character == '9')
            return true;
        return false;
    }

    private boolean isBorder(String test) {
        if (test.length() == 40) {
            for (String border : borders)
                if (test.contains(border)) {
                    test = replaceAll(test, border, "");
                    break;
                }
            if (test.equals(""))
                return true;
        }
        return false;
    }

    private static String replace(String to_return, String to_change, String to_change_to, String unless, boolean true_means_before, boolean replace_all) {
        if (!to_return.toLowerCase().contains(to_change.toLowerCase()))
            return to_return;
        for (int i = 0; to_return.length() >= i + to_change.length(); i++) {
            if (to_return.substring(i, i + to_change.length()).equalsIgnoreCase(to_change)
                    && (unless == null || (true_means_before && (i < unless.length() || !to_return.substring(i - unless.length(), i).equals(unless))) || (!true_means_before && (to_return
                            .length() < i + to_change.length() + unless.length() || !to_return.substring(i + to_change.length(), i + to_change.length() + unless.length())
                            .equalsIgnoreCase(unless))))) {
                to_return = to_return.substring(0, i) + to_change_to + to_return.substring(i + to_change.length());
                if (!replace_all)
                    break;
                else
                    i += to_change_to.length() - 1;
            }
            if (!to_return.toLowerCase().contains(to_change.toLowerCase()))
                break;
        }
        return to_return;
    }

    private static String replaceAll(String to_return, String... changes) {
        for (int j = 0; j < changes.length; j += 2) {
            if (!to_return.toLowerCase().contains(changes[j].toLowerCase()))
                continue;
            for (int i = 0; to_return.length() >= i + changes[j].length(); i++) {
                if (to_return.substring(i, i + changes[j].length()).equalsIgnoreCase(changes[j])) {
                    to_return = to_return.substring(0, i) + changes[j + 1] + to_return.substring(i + changes[j].length());
                    i += changes[j + 1].length() - 1;
                }
                if (!to_return.toLowerCase().contains(changes[j].toLowerCase()))
                    break;
            }
        }
        return to_return;
    }

    private static String correct(String to_return, AutoCorrection correction, boolean replace_all) {
        return replace(to_return, correction.to_correct, correction.to_correct_to, correction.unless, correction.before, replace_all);
    }

    /** This is a simple auto-complete method that can take the first few letters of a player's name and return the full name of the player. It prioritizes in two ways:
     * <b>1)</b> it gives online players priority over offline players and <b>2)</b> it gives shorter names priority over longer usernames because if a player tries to
     * designate a player and this plugin returns a different name than the user meant that starts with the same letters, the user can add more letters to get the longer
     * username instead. If these priorities were reversed, then there would be no way to specify a user whose username is the first part of another username, e.g. "Jeb" and
     * "Jebs_bro". This matching is <i>not</i> case-sensitive.
     * 
     * @param name
     *            is the String that represents the first few letters of a username that needs to be auto-completed.
     * @return the completed username that begins with <b><tt>name</b></tt> (<i>not</i> case-sensitive) */
    private static String getFullName(String name) {
        String full_name = null;
        for (Player possible_owner : server.getOnlinePlayers())
            // if this player's name also matches and it shorter, return it
            // instead becuase if someone is using an autocompleted command, we
            // need to make sure
            // to get the shortest name because if they meant to use the longer
            // username, they can remedy this by adding more letters to the
            // parameter; however,
            // if they meant to do a shorter username and the auto-complete
            // finds the longer one first, they're screwed
            if (possible_owner.getName().toLowerCase().startsWith(name.toLowerCase()) && (full_name == null || full_name.length() > possible_owner.getName().length()))
                full_name = possible_owner.getName();
        for (OfflinePlayer possible_owner : server.getOfflinePlayers())
            if (possible_owner.getName().toLowerCase().startsWith(name.toLowerCase()) && (full_name == null || full_name.length() > possible_owner.getName().length()))
                full_name = possible_owner.getName();
        return full_name;
    }

    private static Boolean getResponse(CommandSender sender, String unformatted_response, String current_status_line, String current_status_is_true_message) {
        boolean said_yes = false, said_no = false;
        String formatted_response = unformatted_response;
        // elimiate unnecessary spaces and punctuation
        while (formatted_response.startsWith(" "))
            formatted_response = formatted_response.substring(1);
        while (formatted_response.endsWith(" "))
            formatted_response = formatted_response.substring(0, formatted_response.length() - 1);
        formatted_response = formatted_response.toLowerCase();
        // check their response
        for (String yes : yeses)
            if (formatted_response.startsWith(yes))
                said_yes = true;
        if (said_yes)
            return true;
        else {
            for (String no : nos)
                if (formatted_response.startsWith(no))
                    said_no = true;
            if (said_no)
                return false;
            else if (current_status_line != null) {
                if (!formatted_response.equals("")) {
                    if (unformatted_response.substring(0, 1).equals(" "))
                        unformatted_response = unformatted_response.substring(1);
                    sender.sendMessage(ChatColor.RED + "I don't know what \"" + unformatted_response + "\" means.");
                }
                while (current_status_line.startsWith(" "))
                    current_status_line = current_status_line.substring(1);
                if (current_status_line.startsWith(current_status_is_true_message))
                    return true;
                else
                    return false;
            } else
                return null;
        }
    }

    public static int readTime(String written) {
        int time = 0;
        String[] temp = written.split(" ");
        ArrayList<String> words = new ArrayList<String>();
        for (String word : temp)
            if (!word.equalsIgnoreCase("and") && !word.equalsIgnoreCase("&"))
                words.add(replaceAll(word.toLowerCase(), ",", ""));
        while (words.size() > 0) {
            // for formats like "2 days 3 minutes 5.57 seconds" or
            // "3 d 5 m 12 s"
            try {
                double amount = Double.parseDouble(words.get(0));
                if (words.get(0).contains("d") || words.get(0).contains("h") || words.get(0).contains("m") || words.get(0).contains("s"))
                    throw new NumberFormatException();
                int factor = 0;
                if (words.size() > 1) {
                    if (words.get(1).startsWith("d"))
                        factor = 86400000;
                    else if (words.get(1).startsWith("h"))
                        factor = 3600000;
                    else if (words.get(1).startsWith("m"))
                        factor = 60000;
                    else if (words.get(1).startsWith("s"))
                        factor = 1000;
                    if (factor > 0)
                        // since a double of, say, 1.0 is actually 0.99999...,
                        // (int)ing it will reduce exact numbers by one, so I
                        // added 0.1 to it to avoid that.
                        time = time + (int) (amount * factor + 0.1);
                    words.remove(0);
                    words.remove(0);
                } else
                    words.remove(0);
            } catch (NumberFormatException exception) {
                // if there's no space between the time and units, e.g.
                // "2h, 5m, 25s" or "4hours, 3min, 2.265secs"
                double amount = 0;
                int factor = 0;
                try {
                    if (words.get(0).contains("d") && (!words.get(0).contains("s") || words.get(0).indexOf("s") > words.get(0).indexOf("d"))) {
                        amount = Double.parseDouble(words.get(0).split("d")[0]);
                        console.sendMessage("amount should=" + words.get(0).split("d")[0]);
                        factor = 86400000;
                    } else if (words.get(0).contains("h")) {
                        amount = Double.parseDouble(words.get(0).split("h")[0]);
                        factor = 3600000;
                    } else if (words.get(0).contains("m")) {
                        amount = Double.parseDouble(words.get(0).split("m")[0]);
                        factor = 60000;
                    } else if (words.get(0).contains("s")) {
                        amount = Double.parseDouble(words.get(0).split("s")[0]);
                        factor = 1000;
                    }
                    if (factor > 0)
                        // since a double of, say, 1.0 is actually 0.99999..., (int)ing it will reduce exact numbers by one, so I added 0.1 to it to avoid that.
                        time = time + (int) (amount * factor + 0.1);
                } catch (NumberFormatException exception2) {
                    //
                }
                words.remove(0);
            }
        }
        return time;
    }

    public static String writeTime(long time, boolean round_seconds) {
        // get the values (e.g. "2 days" or "55.7 seconds")
        ArrayList<String> values = new ArrayList<String>();
        if (time > 86400000) {
            values.add((int) (time / 86400000) + " days");
            time = time % 86400000;
        }
        if (time > 3600000) {
            values.add((int) (time / 3600000) + " hours");
            time = time % 3600000;
        }
        if (time > 60000) {
            values.add((int) (time / 60000) + " minutes");
            time = time % 60000;
        }
        // add a seconds value if there is still time remaining or if there are
        // no other values
        if (time > 0 || values.size() == 0)
            // if you have partial seconds and !round_seconds, it's written as a
            // double so it doesn't truncate the decimals
            if ((time / 1000.0) != (time / 1000) && !round_seconds)
                values.add((time / 1000.0) + " seconds");
            // if seconds are a whole number, just write it as a whole number
            // (integer)
            else
                values.add(Math.round(time / 1000) + " seconds");
        // if there are two or more values, add an "and"
        if (values.size() >= 2)
            values.add(values.size() - 1, "and");
        // assemble the final String
        String written = "";
        for (int i = 0; i < values.size(); i++) {
            // add spaces as needed
            if (i > 0)
                written = written + " ";
            written = written + values.get(i);
            // add commas as needed
            if (values.size() >= 4 && i < values.size() - 1 && !values.get(i).equals("and"))
                written = written + ",";
        }
        if (!written.equals(""))
            return written;
        else
            return null;
    }

    // mass-message utils
    public static void processException(String message, Throwable e) {
        // TODO: test processing "Caused by" scenarios
        tellOps(ChatColor.DARK_RED + message, true);
        /* skip stack trace lines until we get to the part with explicit line numbers and class names that don't come from Java's standard libraries; the stuff we're skipping
         * is anything that comes from the native Java code with no line numbers or class names that will help us pinpoint the issue */
        int lines_to_skip = 0;
        while (lines_to_skip < e.getStackTrace().length
                && (e.getStackTrace()[lines_to_skip].getLineNumber() < 0 || e.getStackTrace()[lines_to_skip].getClassName().startsWith("java")))
            lines_to_skip++;
        while (e != null) {
            // output a maximum of three lines of the stack trace
            tellOps(ChatColor.DARK_RED + e.getClass().getName().substring(e.getClass().getName().lastIndexOf('.') + 1) + " at line "
                    + e.getStackTrace()[lines_to_skip].getLineNumber() + " of " + e.getStackTrace()[lines_to_skip].getClassName() + ".java (myScribe)", true);
            if (lines_to_skip + 1 < e.getStackTrace().length)
                tellOps(ChatColor.DARK_RED + "  ...and at line " + e.getStackTrace()[lines_to_skip + 1].getLineNumber() + " of "
                        + e.getStackTrace()[lines_to_skip + 1].getClassName() + ".java (myScribe)", true);
            if (lines_to_skip + 2 < e.getStackTrace().length)
                tellOps(ChatColor.DARK_RED + "  ...and at line " + e.getStackTrace()[lines_to_skip + 2].getLineNumber() + " of "
                        + e.getStackTrace()[lines_to_skip + 2].getClassName() + ".java (myScribe)", true);
            e = e.getCause();
            if (e != null)
                tellOps(ChatColor.DARK_RED + "...which was caused by:", true);
        }
    }

    public static void tellOps(String message, boolean also_tell_console, String... exempt_ops) {
        // capitalize the first letters of sentences
        if (message.length() > 1)
            message = message.substring(0, 1).toUpperCase() + message.substring(1);
        for (Player player : server.getOnlinePlayers())
            if (player.isOp() && !contains(exempt_ops, player.getName()))
                player.sendMessage(message);
        if (also_tell_console)
            console.sendMessage(message);
    }

    /** This method sends a given message to everyone who is currently debugging this plugin. Players and the console can enter debugging mode using <i>/mUW debug</i>.
     * 
     * @param message
     *            is the <tt>String</tt> that will be sent as a message to any users currently debugging this plugin. */
    public static void debug(String message) {
        if (debuggers.size() == 0)
            return;
        if (debuggers.contains("console")) {
            console.sendMessage(COLOR + message);
            if (debuggers.size() == 1)
                return;
        }
        for (Player player : server.getOnlinePlayers())
            if (debuggers.contains(player.getName()))
                player.sendMessage(COLOR + message);
    }

    // listeners
    @EventHandler(priority = EventPriority.LOWEST)
    public void cancelMutedPlayersChatOrFormatMessage(AsyncPlayerChatEvent event) {
        if (event.isCancelled())
            return;
        // cancel messages accidentally typed while walking or opening chat
        if (event.getMessage().equals("w") || event.getMessage().equals("t")) {
            event.setCancelled(true);
            return;
        }
        // if the player hasn't accepted the rules, cancel the event
        if (!players_who_have_accepted_the_rules.contains(event.getPlayer().getName()) && !event.getPlayer().isOp() && !rules.equals("")) {
            if (event.getMessage().toLowerCase().contains("shut up") || event.getMessage().toLowerCase().contains("stfu"))
                event.getPlayer().sendMessage(colorCode("&4&oNo, &lyou %lshut up!"));
            else
                event.getPlayer().sendMessage(
                        COLOR + "You have to read and accept the rules first.\nUse " + ChatColor.ITALIC + "/rules" + COLOR + " to read the rules and " + ChatColor.ITALIC
                                + "/accept" + COLOR + " to accept them.");
            event.setCancelled(true);
        } // if the player is muted, cancel the event
        else if (muted_players.containsKey(event.getPlayer().getName())) {
            if (event.getMessage().toLowerCase().contains("shut up") || event.getMessage().toLowerCase().contains("stfu"))
                event.getPlayer().sendMessage(colorCode("&4&oNo, &lyou %lshut up!"));
            else
                event.getPlayer().sendMessage(COLOR + muted_players.get(event.getPlayer().getName()) + " muted you. You're not allowed to talk.");
            event.setCancelled(true);
        } // if it's the continuation of a command
        else if (command_beginnings.containsKey(event.getPlayer())) {
            String command_beginning = command_beginnings.get(event.getPlayer());
            if (command_beginnings.get(event.getPlayer()) != null) {
                command_beginning = command_beginnings.get(event.getPlayer());
                command_beginnings.remove(event.getPlayer());
            }
            if (event.getMessage().endsWith("[...]")) {
                command_beginnings.put(event.getPlayer(), command_beginning + event.getMessage().substring(0, event.getMessage().length() - 5));
                event.getPlayer().sendMessage(COLOR + "You may continue typing.");
            } else
                event.getPlayer().performCommand(command_beginning + event.getMessage());
            event.setCancelled(true);
        }
        // if it's a chat message
        else if (!event.getMessage().endsWith("[...]")) {
            String epithet = epithets_by_user.get(event.getPlayer().getName());
            // if the user has no epithet, use the default
            if (epithet == null)
                epithet = replaceAll(default_epithet, "[player]", event.getPlayer().getName());
            // fit the epithet and message into the message format
            String full_chat_message = event.getMessage();
            if (message_beginnings.get(event.getPlayer()) != null) {
                full_chat_message = message_beginnings.get(event.getPlayer()) + event.getMessage();
                message_beginnings.remove(event.getPlayer());
            }
            event.setFormat(colorCode(replaceAll(default_message_format, "[player]", event.getPlayer().getName(), "[epithet]", colorCode(epithet), "[message]",
                    colorCode(AutoCorrect(event.getPlayer(), full_chat_message)))));
        } // if it's a chat message that will continue indefinitely
        else {
            String message_beginning = message_beginnings.get(event.getPlayer());
            if (message_beginning == null)
                message_beginning = "";
            message_beginnings.put(event.getPlayer(), message_beginning + event.getMessage().substring(0, event.getMessage().length() - 5));
            event.getPlayer().sendMessage(COLOR + "You may continue typing.");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void commandProcessing(PlayerCommandPreprocessEvent event) {
        if (!players_who_have_accepted_the_rules.contains(event.getPlayer().getName()) && !event.getPlayer().isOp() && !event.getMessage().toLowerCase().startsWith("/rules")
                && !event.getMessage().toLowerCase().startsWith("/accept") && !rules.equals("")) {
            event.getPlayer().sendMessage(
                    COLOR + "You have to read and accept the rules first.\nUse " + ChatColor.ITALIC + "/rules" + COLOR + " to read the rules and " + ChatColor.ITALIC
                            + "/accept" + COLOR + " to accept them.");
            event.setCancelled(true);
        } else if (event.getMessage().endsWith("[...]")) {
            String command_beginning = command_beginnings.get(event.getPlayer());
            if (command_beginning == null)
                command_beginning = "";
            command_beginnings.put(event.getPlayer(), command_beginning + event.getMessage().substring(1, event.getMessage().length() - 5));
            event.getPlayer().sendMessage(COLOR + "You may continue typing.");
            event.setCancelled(true);
        } else {
            String command = event.getMessage().substring(1);
            if (command_beginnings.get(event.getPlayer()) != null) {
                command = command_beginnings.get(event.getPlayer()) + command;
                command_beginnings.remove(event.getPlayer());
            }
            event.setMessage("/" + command);
        }
        if (AFK_players.contains(event.getPlayer().getName()) && !event.getMessage().equalsIgnoreCase("/afk")) {
            AFK_players.remove(event.getPlayer().getName());
            server.broadcastMessage(COLOR + event.getPlayer().getName() + " is back.");
        }
    }

    @EventHandler
    public void consoleCommandProcessing(ServerCommandEvent event) {
        // if it's a chat message
        String message_beginning = message_beginnings.get(console);
        if (message_beginning == null)
            message_beginning = "";
        if (event.getCommand().endsWith("[...]")) {
            message_beginnings.put(console, message_beginning + event.getCommand().substring(0, event.getCommand().length() - 5));
            console.sendMessage(COLOR + "You may continue typing.");
            event.setCommand("");
        } else {
            message_beginnings.remove(console);
            event.setCommand(message_beginning + event.getCommand());
        }
    }

    @EventHandler
    public void onLogin(PlayerJoinEvent event) {
        String epithet = epithets_by_user.get(event.getPlayer().getName());
        if (epithet == null)
            epithet = default_epithet.replaceAll("\\[player\\]", event.getPlayer().getName());
        event.getPlayer().setDisplayName(colorCode(epithet));
        if (!event.getPlayer().hasPlayedBefore()) {
            event.setJoinMessage(COLOR + event.getPlayer().getName() + " has just logged onto the server for the first time!");
            event.getPlayer().sendMessage(
                    COLOR + "Welcome to the server! Type /rules to read the rules, " + ChatColor.ITALIC + " read them carefully" + COLOR
                            + ", then type /accept to accept the rules.");
        } else {
            ArrayList<String> possible_messages = new ArrayList<String>();
            if (login_messages.get("[server]") != null) {
                possible_messages = login_messages.get("[server]");
                debug("global messages retrieved (" + possible_messages.size() + ")");
            }
            if (server.getPluginManager().getPlugin("Vault") != null && server.getPluginManager().getPlugin("Vault").isEnabled()
                    && permissions.getPrimaryGroup(event.getPlayer()) != null && login_messages.get("[" + permissions.getPrimaryGroup(event.getPlayer()) + "]") != null) {
                possible_messages = login_messages.get("[" + permissions.getPrimaryGroup(event.getPlayer()) + "]");
                debug("group messages retrieved (\"" + permissions.getPrimaryGroup(event.getPlayer()) + "\", " + possible_messages.size() + ")");
            }
            if (login_messages.get(event.getPlayer().getName()) != null) {
                possible_messages = login_messages.get(event.getPlayer().getName());
                debug("individual messages retrieved (\"" + event.getPlayer().getName() + "\", " + possible_messages.size() + ")");
            }
            if (possible_messages == null || possible_messages.size() == 0) {
                debug("no login messages available; using default...");
                return;
            }
            String message = possible_messages.get((int) (Math.random() * possible_messages.size()));
            debug("login message format: \"" + message + "\"");
            message = replaceAll(message, "[player]", event.getPlayer().getName(), "[epithet]", epithet);
            debug("login message w/ insertions: \"" + message + "\"");
            event.setJoinMessage(colorCode(message));
        }
        if (!players_who_have_accepted_the_rules.contains(event.getPlayer().getName()) && event.getPlayer().hasPlayedBefore()) {
            event.getPlayer().sendMessage(
                    ChatColor.RED + "You still haven't accepted the rules! You need to read and accept the rules before you can use commands or talk on this server!");
            event.getPlayer().sendMessage(
                    ChatColor.RED + "Type " + COLOR + "/rules" + ChatColor.RED + " to read the rules, " + ChatColor.ITALIC + "read them carefully" + ChatColor.RED
                            + ", then type " + COLOR + "/accept" + ChatColor.RED + " to accept the rules.");
        }
        if (birthday_today.get(event.getPlayer().getName()) != null) {
            event.getPlayer().sendMessage("HAPPY BIRTHDAY!!!!!! Here's a present.");
            event.getPlayer().getInventory().addItem(new ItemStack(354, 1));
        }
    }

    @EventHandler
    public void colorCodeSigns(SignChangeEvent event) {
        for (int i = 0; i < 4; i++)
            event.setLine(i, colorCode(event.getLine(i)));
    }

    @EventHandler(ignoreCancelled = true)
    public void cancelAFKOnMove(PlayerMoveEvent event) {
        if (AFK_players.contains(event.getPlayer().getName())) {
            AFK_players.remove(event.getPlayer().getName());
            server.broadcastMessage(COLOR + event.getPlayer().getName() + " is back.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void cancelAFKOnChat(AsyncPlayerChatEvent event) {
        if (AFK_players.contains(event.getPlayer().getName())) {
            AFK_players.remove(event.getPlayer().getName());
            server.broadcastMessage(COLOR + event.getPlayer().getName() + " is back.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void cancelAFKOnBedEnter(PlayerBedEnterEvent event) {
        if (AFK_players.contains(event.getPlayer().getName())) {
            AFK_players.remove(event.getPlayer().getName());
            server.broadcastMessage(COLOR + event.getPlayer().getName() + " is back.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void cancelAFKOnBedLeave(PlayerBedLeaveEvent event) {
        if (AFK_players.contains(event.getPlayer().getName())) {
            AFK_players.remove(event.getPlayer().getName());
            server.broadcastMessage(COLOR + event.getPlayer().getName() + " is back.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void cancelAFKOnBucketFill(PlayerBucketFillEvent event) {
        if (AFK_players.contains(event.getPlayer().getName())) {
            AFK_players.remove(event.getPlayer().getName());
            server.broadcastMessage(COLOR + event.getPlayer().getName() + " is back.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void cancelAFKOnBucketEmpty(PlayerBucketEmptyEvent event) {
        if (AFK_players.contains(event.getPlayer().getName())) {
            AFK_players.remove(event.getPlayer().getName());
            server.broadcastMessage(COLOR + event.getPlayer().getName() + " is back.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void cancelAFKOnDropItem(PlayerDropItemEvent event) {
        if (AFK_players.contains(event.getPlayer().getName())) {
            AFK_players.remove(event.getPlayer().getName());
            server.broadcastMessage(COLOR + event.getPlayer().getName() + " is back.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void cancelAFKOnEggThrow(PlayerEggThrowEvent event) {
        if (AFK_players.contains(event.getPlayer().getName())) {
            AFK_players.remove(event.getPlayer().getName());
            server.broadcastMessage(COLOR + event.getPlayer().getName() + " is back.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void cancelAFKOnFish(PlayerFishEvent event) {
        if (AFK_players.contains(event.getPlayer().getName())) {
            AFK_players.remove(event.getPlayer().getName());
            server.broadcastMessage(COLOR + event.getPlayer().getName() + " is back.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void cancelAFKOnInteract(PlayerInteractEvent event) {
        if (AFK_players.contains(event.getPlayer().getName())) {
            AFK_players.remove(event.getPlayer().getName());
            server.broadcastMessage(COLOR + event.getPlayer().getName() + " is back.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void cancelAFKOnRespawn(PlayerRespawnEvent event) {
        if (AFK_players.contains(event.getPlayer().getName())) {
            AFK_players.remove(event.getPlayer().getName());
            server.broadcastMessage(COLOR + event.getPlayer().getName() + " is back.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void cancelAFKOnShear(PlayerShearEntityEvent event) {
        if (AFK_players.contains(event.getPlayer().getName())) {
            AFK_players.remove(event.getPlayer().getName());
            server.broadcastMessage(COLOR + event.getPlayer().getName() + " is back.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void cancelAFKOnSneak(PlayerToggleSneakEvent event) {
        if (AFK_players.contains(event.getPlayer().getName())) {
            AFK_players.remove(event.getPlayer().getName());
            server.broadcastMessage(COLOR + event.getPlayer().getName() + " is back.");
        }
    }

    @EventHandler
    public void onLogout(PlayerQuitEvent event) {
        if (AFK_players.contains(event.getPlayer().getName()))
            AFK_players.remove(event.getPlayer().getName());
        if (message_beginnings.containsKey(event.getPlayer().getName()))
            message_beginnings.remove(event.getPlayer().getName());
        if (command_beginnings.containsKey(event.getPlayer().getName()))
            command_beginnings.remove(event.getPlayer().getName());
        // format the logout message
        ArrayList<String> possible_messages = new ArrayList<String>();
        if (logout_messages.get("[server]") != null)
            possible_messages = logout_messages.get("[server]");
        if (server.getPluginManager().getPlugin("Vault") != null && server.getPluginManager().getPlugin("Vault").isEnabled()
                && permissions.getPrimaryGroup(event.getPlayer()) != null && logout_messages.get("[" + permissions.getPrimaryGroup(event.getPlayer()) + "]") != null)
            possible_messages = logout_messages.get("[" + permissions.getPrimaryGroup(event.getPlayer()) + "]");
        if (login_messages.get(event.getPlayer().getName()) != null)
            possible_messages = login_messages.get(event.getPlayer().getName());
        if (possible_messages == null || possible_messages.size() == 0)
            return;
        String epithet = epithets_by_user.get(event.getPlayer().getName());
        if (epithet == null)
            epithet = default_epithet.replaceAll("\\[player\\]", event.getPlayer().getName());
        event.setQuitMessage(possible_messages.get((int) (Math.random() * possible_messages.size())).replaceAll("\\[player\\]", event.getPlayer().getName()).replaceAll(
                "\\[epithet\\]", epithet));
    }

    // loading
    private void loadTheAnnouncements(CommandSender sender) {
        announcements = new ArrayList<Announcement>();
        // default expiration times: 4 weeks (2,419,200,000ms) for important
        // ones, 2 weeks (1,209,600,000ms) for normal ones, and 1 week
        // (604,800,000ms) for
        // unimportant ones
        expiration_times_for_announcements = new long[] { 2419200000L, 1209600000L, 604800000L };
        // check the announcements file
        File corrections_file = new File(getDataFolder(), "announcements.txt");
        // read the announcements.txt file
        try {
            if (!corrections_file.exists()) {
                getDataFolder().mkdir();
                sender.sendMessage(ChatColor.YELLOW + "I couldn't find an announcements.txt file. I'll make a new one.");
                corrections_file.createNewFile();
            }
            BufferedReader in = new BufferedReader(new FileReader(corrections_file));
            String save_line = in.readLine();
            while (save_line != null) {
                if (save_line.startsWith("expiration time for important announcements:"))
                    expiration_times_for_announcements[0] = readTime(save_line.substring(45));
                else if (save_line.startsWith("expiration time for normal announcements:"))
                    expiration_times_for_announcements[1] = readTime(save_line.substring(42));
                else if (save_line.startsWith("expiration time for unimportant announcements:"))
                    expiration_times_for_announcements[2] = readTime(save_line.substring(47));
                else if (save_line.startsWith("At time = ")) {
                    final Announcement read_announcement = new Announcement(save_line);
                    // check to see if the announcement has expired already and
                    // if not, and if it hasn't, add it and schedule an event to
                    // cancel it later
                    if (read_announcement.time_created + expiration_times_for_announcements[read_announcement.is_important_index] > Calendar.getInstance().getTimeInMillis()) {
                        announcements.add(read_announcement);
                        server.getScheduler().scheduleSyncDelayedTask(
                                this,
                                new Runnable() {

                                    @Override
                                    public void run() {
                                        announcements.remove(read_announcement);
                                    }

                                }, // divide by 50 because the scheduler
                                   // schedules 1 tick/20 seconds;
                                   // ms*1s/1000ms*20ticks/s = ms*20/1000 =
                                   // ms/50
                                (read_announcement.time_created + expiration_times_for_announcements[read_announcement.is_important_index] - Calendar.getInstance()
                                        .getTimeInMillis()) / 50);
                    }
                }
                save_line = in.readLine();
            }
            in.close();
        } catch (IOException exception) {
            sender.sendMessage(ChatColor.DARK_RED + "I got an IOException while trying to save your announcements.");
            exception.printStackTrace();
            return;
        }
        saveTheAnnouncements(sender, false);
        if (announcements.size() == 0)
            sender.sendMessage(COLOR + "Your announcement settings have been loaded.");
        else if (announcements.size() == 1)
            sender.sendMessage(COLOR + "Your announcement settings and your server's only current announcement have been loaded.");
        else
            sender.sendMessage(COLOR + "Your announcement settings and your server's " + announcements.size() + " current announcements have been loaded.");
        if (sender instanceof Player)
            if (announcements.size() == 0)
                console.sendMessage(COLOR + sender.getName() + " loaded your announcement settings from file.");
            else if (announcements.size() == 1)
                console.sendMessage(COLOR + sender.getName() + " loaded your announcement settings and your one current announcement from file.");
            else
                console.sendMessage(COLOR + sender.getName() + " loaded your announcement settings and your " + announcements.size() + " current announcements from file.");
    }

    private void loadTheAutoCorrections(CommandSender sender) {
        AutoCorrections = new ArrayList<AutoCorrection>();
        // check the AutoCorrections file
        File corrections_file = new File(getDataFolder(), "AutoCorrections.txt");
        try {
            if (!corrections_file.exists()) {
                getDataFolder().mkdir();
                sender.sendMessage(ChatColor.YELLOW + "I couldn't find an AutoCorrections.txt file. I'll make a new one.");
                corrections_file.createNewFile();
                for (AutoCorrection correction : default_AutoCorrections)
                    AutoCorrections.add(correction);
            } else {
                // read the AutoCorrections.txt file
                BufferedReader in = new BufferedReader(new FileReader(corrections_file));
                String save_line = in.readLine();
                while (save_line != null) {
                    save_line = save_line.trim();
                    if (save_line.startsWith("Would you like to use AutoCorrections in your server's chat?"))
                        AutoCorrect_on = getResponse(sender, save_line.substring(60), in.readLine(), "Right now, AutoCorrections are enabled.");
                    else if (save_line.startsWith("Would you like me to capitalize the first letter of every sentence?"))
                        capitalize_first_letter = getResponse(sender, save_line.substring(67), in.readLine(), "Right now, first letter capitalization is enabled.");
                    else if (save_line.equals("Would you like me to put periods at the end of any sentences that have no terminal punctuation?"))
                        end_with_period = getResponse(sender, save_line.substring(95), in.readLine(), "Right now, period addition is enabled.");
                    else if (save_line.equals("Would you like me to change any all-caps words or sentences to lowercase italics?"))
                        change_all_caps_to_italics = getResponse(sender, save_line.substring(81), in.readLine(), "Right now, caps to italics conversion is enabled.");
                    else if (save_line.equals("Would you like me to cover up profanities using magic (meaning the &k color code, not fairy dust)?"))
                        cover_up_profanities = getResponse(sender, save_line.substring(98), in.readLine(), "Right now, profanity coverup is enabled.");
                    else if (save_line.equals("Would you like me to replace all in-text commands with their usages when they start with a \"./\"?"))
                        insert_command_usages = getResponse(sender, save_line.substring(96), in.readLine(), "Right now, command usage insertion is enabled.");
                    else if (save_line.startsWith("Change \""))
                        AutoCorrections.add(new AutoCorrection(save_line));
                    save_line = in.readLine();
                }
                in.close();
            }
        } catch (IOException exception) {
            sender.sendMessage(ChatColor.DARK_RED + "I got an IOException while trying to save your AutoCorrections.");
            exception.printStackTrace();
            return;
        }
        saveTheAutoCorrectSettings(sender, false);
        if (AutoCorrections.size() == 0)
            sender.sendMessage(COLOR + "Your AutoCorrect settings have been loaded.");
        else if (AutoCorrections.size() == 1)
            sender.sendMessage(COLOR + "Your AutoCorrect settings and your server's only AutoCorrection have been loaded.");
        else
            sender.sendMessage(COLOR + "Your AutoCorrect settings and your server's " + AutoCorrections.size() + " AutoCorrections have been loaded.");
        if (sender instanceof Player)
            if (AutoCorrections.size() == 0)
                console.sendMessage(COLOR + sender.getName() + " loaded your AutoCorrect settings from file.");
            else if (AutoCorrections.size() == 1)
                console.sendMessage(COLOR + sender.getName() + " loaded your AutoCorrect settings and your one AutoCorrection from file.");
            else
                console.sendMessage(COLOR + sender.getName() + " loaded your AutoCorrect settings and your " + AutoCorrections.size() + " AutoCorrections from file.");
    }

    private void loadTheDeathMessages(CommandSender sender) {
        death_messages_by_cause = new HashMap<String, ArrayList<String>>();
        // check the death messages file
        File death_messages_file = new File(getDataFolder(), "death messages.txt");
        // read the death messages.txt file
        try {
            if (!death_messages_file.exists()) {
                getDataFolder().mkdir();
                sender.sendMessage(ChatColor.YELLOW + "I couldn't find an death messages.txt file. I'll make a new one.");
                death_messages_file.createNewFile();
                death_messages_by_cause = default_death_messages;
            }
            BufferedReader in = new BufferedReader(new FileReader(death_messages_file));
            String save_line = in.readLine();
            while (save_line != null) {
                while (save_line.startsWith(" "))
                    save_line = save_line.substring(1);
                if (save_line.startsWith("Do you want hilarious death messages to appear when people die?"))
                    display_death_messages = getResponse(sender, save_line.substring(63), in.readLine(), "Right now, hilarious death messages will appear when someone dies.");
                save_line = in.readLine();
            }
            in.close();
        } catch (IOException exception) {
            sender.sendMessage(ChatColor.DARK_RED + "I got an IOException while trying to save your death messages.");
            exception.printStackTrace();
            return;
        }
        saveTheDeathMessages(sender, false);
        if (death_messages_by_cause.size() == 0)
            sender.sendMessage(ChatColor.RED + "You don't have any death messages to load.");
        else if (death_messages_by_cause.size() == 1)
            sender.sendMessage(COLOR + "Your only death message has been loaded.");
        else
            sender.sendMessage(COLOR + "Your " + death_messages_by_cause.size() + " death messages have been loaded.");
        if (sender instanceof Player)
            if (death_messages_by_cause.size() == 0)
                console.sendMessage(COLOR + sender.getName() + " tried to laod your death messages from file, but there were none.");
            else if (death_messages_by_cause.size() == 1)
                console.sendMessage(COLOR + sender.getName() + " loaded your only death message from file.");
            else
                console.sendMessage(COLOR + sender.getName() + " loaded your " + death_messages_by_cause.size() + " death messages from file.");
    }

    private void loadTheEpithets(CommandSender sender) {
        epithets_by_user = new HashMap<String, String>();
        default_epithet = "&f[player]";
        default_message_format = "[epithet]&f: [message]";
        // check the epithets file
        File epithets_file = new File(getDataFolder(), "epithets.txt");
        // read the epithets.txt file
        try {
            if (!epithets_file.exists()) {
                getDataFolder().mkdir();
                sender.sendMessage(ChatColor.YELLOW + "I couldn't find an epithets.txt file. I'll make a new one.");
                epithets_file.createNewFile();
            }
            BufferedReader in = new BufferedReader(new FileReader(epithets_file));
            String save_line = in.readLine();
            while (save_line != null) {
                save_line = save_line.trim();

                if (save_line.toLowerCase().startsWith("default epithet: "))
                    default_epithet = save_line.substring(17);
                else if (save_line.toLowerCase().startsWith("default message format: "))
                    default_message_format = save_line.substring(24);
                else if (save_line.contains(": ") && !isBorder(save_line))
                    epithets_by_user.put(save_line.substring(0, save_line.indexOf(": ")), save_line.substring(save_line.indexOf(": ") + 2));
                save_line = in.readLine();
            }
            in.close();
        } catch (IOException exception) {
            sender.sendMessage(ChatColor.DARK_RED + "I got an IOException while trying to save your epithets.");
            exception.printStackTrace();
            return;
        }
        saveTheEpithets(sender, false);
        if (epithets_by_user.size() == 0)
            sender.sendMessage(COLOR + "Your epithet settings have been loaded.");
        else if (epithets_by_user.size() == 1)
            sender.sendMessage(COLOR + "Your epithet settings and your server's only epithet have been loaded.");
        else
            sender.sendMessage(COLOR + "Your epithet settings and your server's " + epithets_by_user.size() + " epithets have been loaded.");
    }

    private void loadTheLoginMessages(CommandSender sender) {
        File login_messages_file = new File(getDataFolder(), "login messages.txt");
        int number_of_messages = 0;
        try {
            if (!login_messages_file.exists()) {
                sender.sendMessage(ChatColor.YELLOW + "I couldn't find a login messages.txt file. I'll make a new one.");
                login_messages_file.createNewFile();
                // insert the default login messages
                for (String[] target_and_messages : default_login_messages) {
                    String config_target = target_and_messages[0];
                    ArrayList<String> messages = new ArrayList<String>();
                    for (int i = 1; i < target_and_messages.length; i++)
                        messages.add(target_and_messages[i]);
                    login_messages.put(config_target, messages);
                }
            }
            BufferedReader in = new BufferedReader(new FileReader(login_messages_file));
            String save_line = in.readLine(), config_target = null;
            ArrayList<String> current_messages = new ArrayList<String>();
            while (save_line != null) {
                if (save_line.startsWith("====") && save_line.endsWith("====")) {
                    if (config_target != null) {
                        login_messages.put(config_target, current_messages);
                        number_of_messages += current_messages.size();
                        current_messages.clear();
                    }
                    config_target = save_line.substring(4, save_line.length() - 4).trim();
                } else
                    current_messages.add(save_line);
                save_line = in.readLine();
            }
            in.close();
            if (config_target != null) {
                login_messages.put(config_target, current_messages);
                number_of_messages += current_messages.size();
            }
        } catch (IOException e) {
            sender.sendMessage(ChatColor.DARK_RED + "Sorry, but I got an IOException while reading the login messages.");
            e.printStackTrace();
            return;
        }
        saveTheLoginMessages(sender, false);
        if (number_of_messages == 0)
            sender.sendMessage(ChatColor.RED + "You don't have any login messages to load.");
        else if (number_of_messages == 1)
            sender.sendMessage(COLOR + "Your 1 login message has been loaded.");
        else
            sender.sendMessage(COLOR + "Your " + number_of_messages + " login messages have been loaded.");
        if (sender instanceof Player)
            if (number_of_messages == 0)
                console.sendMessage(COLOR + sender.getName() + " tried to load your login messages from file, but there were no messages to load.");
            else if (number_of_messages == 1)
                console.sendMessage(COLOR + sender.getName() + " loaded your 1 login message from file.");
            else
                console.sendMessage(COLOR + sender.getName() + " loaded your " + number_of_messages + " login messages from file.");
    }

    private void loadTheLogoutMessages(CommandSender sender) {
        File logout_messages_file = new File(getDataFolder(), "logout messages.txt");
        int number_of_messages = 0;
        try {
            if (!logout_messages_file.exists()) {
                sender.sendMessage(ChatColor.YELLOW + "I couldn't find a logout messages.txt file. I'll make a new one.");
                logout_messages_file.createNewFile();
                // insert the default logout messages
                for (String[] target_and_messages : default_logout_messages) {
                    String config_target = target_and_messages[0];
                    ArrayList<String> messages = new ArrayList<String>();
                    for (int i = 1; i < target_and_messages.length; i++)
                        messages.add(target_and_messages[i]);
                    logout_messages.put(config_target, messages);
                }
            }
            BufferedReader in = new BufferedReader(new FileReader(logout_messages_file));
            String save_line = in.readLine(), config_target = null;
            ArrayList<String> current_messages = new ArrayList<String>();
            while (save_line != null) {
                if (save_line.startsWith("====") && save_line.endsWith("====")) {
                    if (config_target != null) {
                        logout_messages.put(config_target, current_messages);
                        number_of_messages += current_messages.size();
                        current_messages.clear();
                    }
                    config_target = save_line.substring(4, save_line.length() - 4).trim();
                } else
                    current_messages.add(save_line);
                save_line = in.readLine();
            }
            in.close();
            if (config_target != null) {
                logout_messages.put(config_target, current_messages);
                number_of_messages += current_messages.size();
            }
        } catch (IOException e) {
            sender.sendMessage(ChatColor.DARK_RED + "Sorry, but I got an IOException while reading the logout messages.");
            e.printStackTrace();
            return;
        }
        saveTheLogoutMessages(sender, false);
        if (number_of_messages == 0)
            sender.sendMessage(ChatColor.RED + "You don't have any logout messages to load.");
        else if (number_of_messages == 1)
            sender.sendMessage(COLOR + "Your 1 logout message has been loaded.");
        else
            sender.sendMessage(COLOR + "Your " + number_of_messages + " logout messages have been loaded.");
        if (sender instanceof Player)
            if (number_of_messages == 0)
                console.sendMessage(COLOR + sender.getName() + " tried to load your logout messages from file, but there were no messages to load.");
            else if (number_of_messages == 1)
                console.sendMessage(COLOR + sender.getName() + " loaded your 1 logout message from file.");
            else
                console.sendMessage(COLOR + sender.getName() + " loaded your " + number_of_messages + " logout messages from file.");
    }

    private void loadTheRules(CommandSender sender) {
        rules = "";
        players_who_have_accepted_the_rules = new ArrayList<String>();
        // check the rules file
        File rules_file = new File(getDataFolder(), "rules.txt");
        // read the rules.txt file
        players_who_have_accepted_the_rules = new ArrayList<String>();
        try {
            if (!rules_file.exists()) {
                sender.sendMessage(ChatColor.YELLOW + "I couldn't find an rules.txt file. I'll make a new one.");
                getDataFolder().mkdir();
                rules_file.createNewFile();
            }
            BufferedReader in = new BufferedReader(new FileReader(rules_file));
            String save_line = in.readLine();
            rules = "";
            while (save_line != null) {
                if (isBorder(save_line)) {
                    save_line = in.readLine();
                    if (save_line.contains(", ")) {
                        String[] players_listed = save_line.split(", ");
                        // elimiate the "and" and sentence ending around the
                        // last username
                        players_listed[players_listed.length - 1] =
                                players_listed[players_listed.length - 1].substring(4, players_listed[players_listed.length - 1].length() - 29);
                        // convert the array to an ArrayList
                        for (String listed_player : players_listed)
                            players_who_have_accepted_the_rules.add(listed_player);
                    } else if (save_line.contains(" and ")) {
                        String[] players_listed = save_line.split(" and ");
                        players_listed[1] = players_listed[1].substring(0, players_listed[1].length() - 25);
                        players_who_have_accepted_the_rules.add(players_listed[0]);
                        players_who_have_accepted_the_rules.add(players_listed[1]);
                    } else if (!save_line.startsWith("No one"))
                        players_who_have_accepted_the_rules.add(save_line.split(" ")[0]);
                } else if (rules.equals("") && !save_line.equals("Type the rules below this line and above the border line!"))
                    rules = save_line;
                else if (!save_line.equals("Type the rules below this line and above the border line!"))
                    rules = rules + "\n" + save_line;
                save_line = in.readLine();
            }
            in.close();
        } catch (IOException exception) {
            sender.sendMessage(ChatColor.DARK_RED + "I got an IOException while trying to save your rules.");
            exception.printStackTrace();
            return;
        }
        saveTheRules(sender, false);
        if (!rules.equals(""))
            sender.sendMessage(COLOR + "Your rules have been loaded.");
        else
            sender.sendMessage(ChatColor.RED + "You haven't written down your rules! You need to write down your rules in the rules.txt right now!");
    }

    private void loadTheTemporaryData() {
        // check the temporary file
        File temp_file = new File(getDataFolder(), "temp.txt");
        if (temp_file.exists())
            // read the temp.txt file
            try {
                BufferedReader in = new BufferedReader(new FileReader(temp_file));
                String save_line = in.readLine(), data_type = "", commander = "";
                while (save_line != null) {
                    if (save_line.startsWith("==== "))
                        if (save_line.substring(5).startsWith("muted"))
                            data_type = "muted players";
                        else
                            data_type = "mail";
                    else if (save_line.startsWith("== "))
                        commander = save_line.split(" ")[1];
                    else if (data_type.equals("muted players"))
                        muted_players.put(save_line.split(",")[0], save_line.split(",")[1]);
                    else if (data_type.equals("mail")) {
                        // in the case of mail, "commander" actually represents
                        // the recipient of the mail
                        ArrayList<String> mail_for_this_person = mail.get(commander);
                        if (mail_for_this_person == null)
                            mail_for_this_person = new ArrayList<String>();
                        mail_for_this_person.add(save_line);
                        mail.put(commander, mail_for_this_person);
                    }
                    save_line = in.readLine();
                }
                in.close();
            } catch (IOException exception) {
                console.sendMessage(ChatColor.DARK_RED + "I got an IOException while trying to load the temporary data.");
                exception.printStackTrace();
                return;
            }
        temp_file.delete();
    }

    private void loadTheBirthdayPeople(CommandSender sender) {

        File birthday_people_file = new File(getDataFolder(), "birthday people.txt");

        try {

            if (!birthday_people_file.exists()) {
                birthday_people_file.createNewFile();
                return;
            }

            BufferedReader in = new BufferedReader(new FileReader(birthday_people_file));
            String save_line = in.readLine();
            Calendar cal = Calendar.getInstance();

            while (save_line != null) {
                if (!save_line.endsWith("LATE")) {
                    birthday_players.put(save_line.substring(0, save_line.length() - 6), save_line.substring(save_line.length() - 5, save_line.length()));
                    if (cal.get(Calendar.MONTH) + 1 == Integer.parseInt(save_line.substring(save_line.length() - 2, save_line.length()))
                            && cal.get(Calendar.DAY_OF_MONTH) == Integer.parseInt(save_line.substring(save_line.length() - 5, save_line.length() - 3))) {
                        birthday_today.put(save_line.substring(0, save_line.length() - 6), save_line.substring(save_line.length() - 5, save_line.length()));
                    }
                } else {
                    birthday_today.put(save_line.substring(0, save_line.length() - 10), save_line.substring(save_line.length() - 9, save_line.length() - 4));
                }
                save_line = in.readLine();
            }
            in.close();

        } catch (IOException e) {
            sender.sendMessage(ChatColor.DARK_RED + "Sorry, but I got an IOException while reading your birthdays.");
            e.printStackTrace();
            return;
        }

        saveTheBirthdayPeople(sender, false);
    }

    // saving
    private void saveTheAnnouncements(CommandSender sender, boolean display_message) {
        // check the announcements file
        File announcements_file = new File(getDataFolder(), "announcements.txt");
        // save the announcements
        try {
            if (!announcements_file.exists()) {
                getDataFolder().mkdir();
                announcements_file.createNewFile();
            }
            BufferedWriter out = new BufferedWriter(new FileWriter(announcements_file));
            out.write("expiration time for important announcements: " + writeTime(expiration_times_for_announcements[0], false));
            out.newLine();
            out.write("expiration time for normal announcements: " + writeTime(expiration_times_for_announcements[1], false));
            out.newLine();
            out.write("expiration time for unimportant announcements: " + writeTime(expiration_times_for_announcements[2], false));
            out.newLine();
            String border_unit = borders[(int) (Math.random() * borders.length)], border = "";
            for (int i = 0; i < 20; i++)
                border = border + border_unit;
            out.write(border);
            out.newLine();
            for (Announcement announcement : announcements) {
                out.write(announcement.save_line);
                out.newLine();
            }
            out.close();
        } catch (IOException exception) {
            console.sendMessage(ChatColor.DARK_RED + "I got an IOException while trying to save your temporary data.");
            exception.printStackTrace();
            return;
        }
        // TODO confirmational messages!
    }

    private void saveTheAutoCorrectSettings(CommandSender sender, boolean display_message) {
        // link up with Vault
        Vault = server.getPluginManager().getPlugin("Vault");
        if (Vault != null) {
            // locate the permissions and economy plugins
            try {
                permissions = server.getServicesManager().getRegistration(Permission.class).getProvider();
            } catch (NullPointerException exception) {
                permissions = null;
            }
            try {
                economy = server.getServicesManager().getRegistration(Economy.class).getProvider();
            } catch (NullPointerException exception) {
                economy = null;
            }
            // forcibly enable the permissions plugin
            if (permissions != null) {
                Plugin permissions_plugin = server.getPluginManager().getPlugin(permissions.getName());
                if (permissions_plugin != null && !permissions_plugin.isEnabled())
                    server.getPluginManager().enablePlugin(permissions_plugin);
            }
            // send confirmation messages
            console.sendMessage(COLOR + "I see your Vault...");
            if (permissions == null && economy == null)
                console.sendMessage(ChatColor.RED + "...but I can't find any economy or permissions plugins.");
            else if (permissions != null) {
                console.sendMessage(COLOR + "...and raise you a " + permissions.getName() + "...");
                if (economy != null)
                    console.sendMessage(COLOR + "...as well as a " + economy.getName() + ".");
                else
                    console.sendMessage(ChatColor.RED + "...but I can't find your economy plugin.");
            } else if (permissions == null && economy != null) {
                console.sendMessage(COLOR + "...and raise you a " + economy.getName() + "...");
                console.sendMessage(ChatColor.RED + "...but I can't find your permissions plugin.");
            }
        }
        File corrections_file = new File(getDataFolder(), "AutoCorrections.txt");
        // save the AutoCorrections
        try {
            // check the AutoCorrections file
            if (!corrections_file.exists()) {
                getDataFolder().mkdir();
                corrections_file.createNewFile();
                for (AutoCorrection correction : default_AutoCorrections)
                    AutoCorrections.add(correction);
            }
            BufferedWriter out = new BufferedWriter(new FileWriter(corrections_file));
            out.write("Would you like to use AutoCorrections in your server's chat? ");
            out.newLine();
            if (AutoCorrect_on)
                out.write("   Right now, AutoCorrections are enabled.");
            else
                out.write("   Right now, AutoCorrections are disabled.");
            out.newLine();
            out.newLine();
            out.write("Would you like me to capitalize the first letter of every sentence? ");
            out.newLine();
            if (capitalize_first_letter)
                out.write("   Right now, first letter capitalization is enabled.");
            else
                out.write("   Right now, first letter capitalization is disabled.");
            out.newLine();
            out.newLine();
            out.write("Would you like me to put periods at the end of any sentences that have no terminal punctuation? ");
            out.newLine();
            if (end_with_period)
                out.write("   Right now, period addition is enabled.");
            else
                out.write("   Right now, period addition is diabled.");
            out.newLine();
            out.newLine();
            out.write("Would you like me to change any all-caps words or sentences to lowercase italics? ");
            out.newLine();
            if (change_all_caps_to_italics)
                out.write("   Right now, caps to italics conversion is enabled.");
            else
                out.write("   Right now, caps to italics conversion is diabled.");
            out.newLine();
            out.newLine();
            out.write("Would you like me to cover up profanities using magic (meaning the &k color code, not fairy dust)? ");
            out.newLine();
            if (cover_up_profanities)
                out.write("   Right now, profanity coverup is enabled.");
            else
                out.write("   Right now, profanity coverup is diabled.");
            out.newLine();
            out.newLine();
            out.write("Would you like me to replace all in-text commands with their usages when they start with a \"./\"? ");
            out.newLine();
            if (insert_command_usages)
                out.write("   Right now, command usage insertion is enabled.");
            else
                out.write("   Right now, command usage insertion is disabled.");
            out.newLine();
            String border_unit = borders[(int) (Math.random() * borders.length)], border = "";
            for (int i = 0; i < 20; i++)
                border = border + border_unit;
            out.write(border);
            out.newLine();
            for (AutoCorrection correction : AutoCorrections) {
                out.write(correction.toString());
                out.newLine();
            }
            out.close();
        } catch (IOException exception) {
            sender.sendMessage(ChatColor.DARK_RED + "I got an IOException while trying to save your AutoCorrections.");
            exception.printStackTrace();
            return;
        }
        if (display_message) {
            if (AutoCorrections.size() == 0)
                sender.sendMessage(COLOR + "Your AutoCorrect settings have been saved.");
            else if (AutoCorrections.size() == 1)
                sender.sendMessage(COLOR + "Your AutoCorrect settings and your server's only AutoCorrection have been saved.");
            else
                sender.sendMessage(COLOR + "Your AutoCorrect settings and your server's " + AutoCorrections.size() + " AutoCorrections have been saved.");
            if (sender instanceof Player)
                if (AutoCorrections.size() == 0)
                    console.sendMessage(COLOR + sender.getName() + " saved your AutoCorrect settings.");
                else if (AutoCorrections.size() == 1)
                    console.sendMessage(COLOR + sender.getName() + " saved your AutoCorrect settings and your server's only AutoCorrection.");
                else
                    console.sendMessage(COLOR + sender.getName() + " saved your AutoCorrect settings and your server's " + AutoCorrections.size() + " AutoCorrections.");
        }
    }

    private void saveTheDeathMessages(CommandSender sender, boolean display_message) {
        boolean failed = false;
        // check the death messages file
        File death_messages_file = new File(getDataFolder(), "death messages.txt");
        if (!death_messages_file.exists()) {
            getDataFolder().mkdir();
            try {
                death_messages_file.createNewFile();
            } catch (IOException exception) {
                sender.sendMessage(ChatColor.DARK_RED + "I couldn't create an AutoCorrections.txt file! Oh nos!");
                exception.printStackTrace();
                failed = true;
            }
        }
        // save the AutoCorrections
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(death_messages_file));
            out.write("Do you want hilarious death messages to appear when people die? ");
            out.newLine();
            if (display_death_messages)
                out.write("   Right now, hilarious death messages will appear when someone dies.");
            else
                out.write("   Right now, hilarious death messages will not appear when someone dies. Boring...");
            out.newLine();
            out.newLine();
            out.write("Below is the list of death messages. You can customize them however you like and use as many colors as you want. Put \"[player]\" to show where the name of the player who dies should appear in the message.");
            out.newLine();
            for (int i = 0; i < death_messages_by_cause.size(); i++) {
                String cause = (String) death_messages_by_cause.keySet().toArray()[i];
                ArrayList<String> messages = death_messages_by_cause.get(cause);
                out.write(" == " + cause + " == ");
                out.newLine();
                for (String message : messages) {
                    out.write(message);
                    out.newLine();
                }
            }
            out.flush();
            out.close();
        } catch (IOException exception) {
            sender.sendMessage(ChatColor.DARK_RED + "I got an IOException while trying to save your AutoCorrections.");
            exception.printStackTrace();
            failed = true;
        }
        if (!failed && display_message)
            if (death_messages_by_cause.size() == 0)
                sender.sendMessage(COLOR + "You don't actually have any death messages to save.");
            else if (death_messages_by_cause.size() == 1)
                sender.sendMessage(COLOR + "Your server's only death message has been saved.");
            else
                sender.sendMessage(COLOR + "Your server's " + death_messages_by_cause.size() + " death messages have been saved.");
    }

    private void saveTheEpithets(CommandSender sender, boolean display_message) {
        // check the epithets file
        File epithets_file = new File(getDataFolder(), "epithets.txt");
        // save the epithets
        try {
            if (!epithets_file.exists()) {
                getDataFolder().mkdir();
                epithets_file.createNewFile();
            }
            BufferedWriter out = new BufferedWriter(new FileWriter(epithets_file));
            out.write("Make sure that there is a space after the colon for each data entry. You may use color codes everywhere.");
            out.newLine();
            out.write("Designate the default epithet for new players below. Use \"[player]\" to designate where the player's name should go.");
            out.newLine();
            out.write("default epithet: " + default_epithet);
            out.newLine();
            out.newLine();
            out.write("This is the general format for a message. Use \"[epithet]\" to designate where the epithet should go and \"[message]\" for the message.");
            out.newLine();
            out.write("default message format: " + default_message_format);
            out.newLine();
            String border_unit = borders[(int) (Math.random() * borders.length)], border = "";
            for (int i = 0; i < 20; i++)
                border = border + border_unit;
            out.write(border);
            out.newLine();
            for (int i = 0; i < epithets_by_user.size(); i++) {
                String user = (String) epithets_by_user.keySet().toArray()[i];
                out.write("     " + user + ": " + epithets_by_user.get(user));
                out.newLine();
            }
            out.close();
        } catch (IOException exception) {
            sender.sendMessage(ChatColor.DARK_RED + "I got an IOException while trying to save your epithets.");
            exception.printStackTrace();
            return;
        }
        if (display_message) {
            if (epithets_by_user.size() == 0)
                sender.sendMessage(COLOR + "Your epithet settings have been saved.");
            else if (epithets_by_user.size() == 1)
                sender.sendMessage(COLOR + "Your epithet settings and your server's only epithet have been saved.");
            else
                sender.sendMessage(COLOR + "Your epithet settings and your server's " + epithets_by_user.size() + " epithets have been saved.");
            if (sender instanceof Player)
                if (epithets_by_user.size() == 0)
                    sender.sendMessage(COLOR + sender.getName() + " saved your epithet settings.");
                else if (epithets_by_user.size() == 1)
                    sender.sendMessage(COLOR + sender.getName() + " saved your epithet settings and your server's only epithet.");
                else
                    sender.sendMessage(COLOR + sender.getName() + " saved your epithet settings and your server's " + epithets_by_user.size() + " epithets.");
        }
    }

    private void saveTheLoginMessages(CommandSender sender, boolean display_message) {
        debug("saving login messages...");

        File login_messages_file = new File(getDataFolder(), "login messages.txt");
        // record the number of messages as we write them to display at the end
        int number_of_messages = 0;
        try {
            if (!login_messages_file.exists()) {
                sender.sendMessage(ChatColor.YELLOW + "I couldn't find a login messages.txt file. I'll make a new one.");
                login_messages_file.createNewFile();
                return;
            }
            BufferedWriter out = new BufferedWriter(new FileWriter(login_messages_file));

            debug("writing serverwide login messages...");
            out.write("==== [server] ====");
            out.newLine();
            if (login_messages.get("[server]") != null) {
                debug("found server login messages; writing...");

                ArrayList<String> messages = login_messages.get("[server]");
                for (String message : messages) {
                    out.write(message);
                    out.newLine();
                    number_of_messages++;
                }
            }

            debug("starting group login messages...");
            for (String key : login_messages.keySet())
                if (key.startsWith("[") && !key.equals("[server]")) {
                    debug("writing " + key + "'s login messages...");
                    out.write("==== " + key + " ====");
                    out.newLine();

                    ArrayList<String> messages = login_messages.get(key);
                    for (String message : messages) {
                        out.write(message);
                        out.newLine();
                        number_of_messages++;
                    }
                }

            debug("starting individual login messages...");
            for (String key : login_messages.keySet())
                if (!key.startsWith("[")) {
                    debug("writing " + key + "'s login messages...");
                    out.write("==== " + key + " ====");
                    out.newLine();

                    ArrayList<String> messages = login_messages.get(key);
                    for (String message : messages) {
                        out.write(message);
                        out.newLine();
                        number_of_messages++;
                    }
                }
            out.flush();
            out.close();
        } catch (IOException e) {
            tellOps(ChatColor.DARK_RED + "I got an IOException trying to save the login messages.", true);
            e.printStackTrace();
            return;
        }
        if (display_message) {
            if (number_of_messages == 0)
                sender.sendMessage(COLOR + "There were no login messages to save!");
            else if (number_of_messages == 1)
                sender.sendMessage(COLOR + "Your 1 login message has been saved.");
            else
                sender.sendMessage(COLOR + "Your " + number_of_messages + " login messages have been saved.");
            if (sender instanceof Player)
                if (number_of_messages == 0)
                    console.sendMessage(COLOR + sender.getName() + " tried to save your login messages, but there were no login messages to save!");
                else if (number_of_messages == 1)
                    console.sendMessage(COLOR + sender.getName() + " saved your 1 login message.");
                else
                    console.sendMessage(COLOR + sender.getName() + " saved your " + number_of_messages + " login messages.");
        }
    }

    private void saveTheLogoutMessages(CommandSender sender, boolean display_message) {
        File logout_messages_file = new File(getDataFolder(), "logout messages.txt");
        // record the number of messages as we write them to display at the end
        int number_of_messages = 0;
        try {
            if (!logout_messages_file.exists()) {
                sender.sendMessage(ChatColor.YELLOW + "I couldn't find a logout messages.txt file. I'll make a new one.");
                logout_messages_file.createNewFile();
                return;
            }
            BufferedWriter out = new BufferedWriter(new FileWriter(logout_messages_file));

            debug("writing serverwide logout messages...");
            out.write("==== [server] ====");
            out.newLine();
            if (logout_messages.get("[server]") != null) {
                ArrayList<String> messages = login_messages.get("[server]");
                for (String message : messages) {
                    out.write(message);
                    out.newLine();
                    number_of_messages++;
                }
            }

            debug("starting group logout messages...");
            for (String key : logout_messages.keySet())
                if (key.startsWith("[") && !key.equals("[server]")) {
                    out.write("==== " + key + " ====");
                    out.newLine();

                    ArrayList<String> messages = login_messages.get(key);
                    for (String message : messages) {
                        out.write(message);
                        out.newLine();
                        number_of_messages++;
                    }
                }

            debug("starting individual logout messages...");
            for (String key : logout_messages.keySet())
                if (!key.startsWith("[")) {
                    out.write("==== " + key + " ====");
                    out.newLine();

                    ArrayList<String> messages = login_messages.get(key);
                    for (String message : messages) {
                        out.write(message);
                        out.newLine();
                        number_of_messages++;
                    }
                }
            out.flush();
            out.close();
        } catch (IOException e) {
            tellOps(ChatColor.DARK_RED + "I got an IOException trying to save the logout messages.", true);
            e.printStackTrace();
            return;
        }
        if (display_message) {
            if (number_of_messages == 0)
                sender.sendMessage(COLOR + "There were no logout messages to save!");
            else if (number_of_messages == 1)
                sender.sendMessage(COLOR + "Your 1 logout message has been saved.");
            else
                sender.sendMessage(COLOR + "Your " + number_of_messages + " logout messages have been saved.");
            if (sender instanceof Player)
                if (number_of_messages == 0)
                    console.sendMessage(COLOR + sender.getName() + " tried to save your logout messages, but there were no logout messages to save!");
                else if (number_of_messages == 1)
                    console.sendMessage(COLOR + sender.getName() + " saved your 1 logout message.");
                else
                    console.sendMessage(COLOR + sender.getName() + " saved your " + number_of_messages + " logout messages.");
        }
    }

    private void saveTheRules(CommandSender sender, boolean display_message) {
        // check the rules file
        File rules_file = new File(getDataFolder(), "rules.txt");
        // save the rules
        try {
            if (!rules_file.exists()) {
                getDataFolder().mkdir();
                rules_file.createNewFile();
            }
            BufferedWriter out = new BufferedWriter(new FileWriter(rules_file));
            out.write("Type the rules below this line and above the border line!");
            out.newLine();
            String[] rules_lines = rules.split("\n");
            for (String rule_line : rules_lines) {
                out.write(rule_line);
                out.newLine();
            }
            String border_unit = borders[(int) (Math.random() * borders.length)], border = "";
            for (int i = 0; i < 20; i++)
                border = border + border_unit;
            out.write(border);
            out.newLine();
            // save the users who have accepted the rules
            if (players_who_have_accepted_the_rules.size() > 2) {
                for (int i = 0; i < players_who_have_accepted_the_rules.size() - 1; i++)
                    out.write(players_who_have_accepted_the_rules.get(i) + ", ");
                out.write("and " + players_who_have_accepted_the_rules.get(players_who_have_accepted_the_rules.size() - 1) + " have all accepted the rules.");
            } else if (players_who_have_accepted_the_rules.size() == 2)
                out.write(players_who_have_accepted_the_rules.get(0) + " and " + players_who_have_accepted_the_rules.get(1) + " have accepted the rules.");
            else if (players_who_have_accepted_the_rules.size() == 1)
                out.write(players_who_have_accepted_the_rules.get(0) + " is the only one who has accepted the rules.");
            else
                out.write("No one has accepted the rules yet!");
            out.flush();
            out.close();
        } catch (IOException exception) {
            sender.sendMessage(ChatColor.DARK_RED + "I got an IOException while trying to save your rules.");
            exception.printStackTrace();
            return;
        }
        if (display_message)
            if (!rules.equals(""))
                sender.sendMessage(COLOR + "Your rules have been saved.");
            else
                sender.sendMessage(ChatColor.RED + "Go write down your rules in the rules.txt!");
    }

    private void saveTheTemporaryData() {
        // ^that warning suppression is for the part were I cast an Object,
        // data[2], as an ArrayList<String> without a check
        // check the temporary file
        File temp_file = new File(getDataFolder(), "temp.txt");
        // save the warp and death histories
        try {
            if (!temp_file.exists()) {
                getDataFolder().mkdir();
                temp_file.createNewFile();
            }
            BufferedWriter out = new BufferedWriter(new FileWriter(temp_file));
            out.write("==== muted players ====");
            out.newLine();
            for (String key : muted_players.keySet()) {
                out.write(key + "," + muted_players.get(key));
                out.newLine();
            }
            out.write("==== mail ====");
            out.newLine();
            for (String key : mail.keySet()) {
                out.write("== " + key + " ==");
                out.newLine();
                for (String message : mail.get(key)) {
                    out.write(message);
                    out.newLine();
                }
            }
            out.close();
        } catch (IOException exception) {
            console.sendMessage(ChatColor.DARK_RED + "I got an IOException while trying to save your temporary data.");
            exception.printStackTrace();
            return;
        }
    }

    private void saveTheBirthdayPeople(CommandSender sender, boolean display_message) {
        File birthday_people_file = new File(getDataFolder(), "birthday people.txt");

        try {

            BufferedWriter write = new BufferedWriter(new FileWriter(birthday_people_file));

            ArrayList<String> players = new ArrayList<String>();

            for (String temp : birthday_players.keySet()) {
                players.add(temp);
            }
            while (players.size() != 0) {
                write.write(players.get(players.size() - 1) + " " + birthday_players.get(players.get(players.size() - 1)));
                write.newLine();
                players.remove(players.size() - 1);
            }

            for (String temp : birthday_today.keySet()) {
                players.add(temp);
            }
            while (players.size() != 0) {
                write.write(players.get(players.size() - 1) + " " + birthday_today.get(players.get(players.size() - 1)) + "LATE");
                write.newLine();
                players.remove(players.size() - 1);
            }

        } catch (IOException e) {
            sender.sendMessage(ChatColor.DARK_RED + "I got an IOException while trying to save your birthdays.");
            e.printStackTrace();
            return;
        }

    }

    // command methods
    private void addCorrection(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "Coming soon to a server near you!");
        // TODO
    }

    private void AFKCheck(CommandSender sender) {
        String target_player = null;
        for (Player player : server.getOnlinePlayers())
            if (player.getName().toLowerCase().startsWith(parameters[0].toLowerCase()))
                target_player = player.getName();
        if (target_player == null)
            sender.sendMessage(ChatColor.RED + "I couldn't find \"" + parameters[0] + "\" anywhere.");
        else if (AFK_players.contains(target_player))
            sender.sendMessage(COLOR + target_player + " is a.f.k. right now.");
        else
            sender.sendMessage(COLOR + target_player + " is not a.f.k right now.");
    }

    private void AFKList(CommandSender sender, boolean on_login) {
        if (AFK_players.size() == 0) {
            if (!on_login)
                sender.sendMessage(COLOR + "No one is a.f.k. right now!");
        } else if (AFK_players.size() == 1)
            sender.sendMessage(COLOR + AFK_players.get(0) + " is the only one a.f.k. right now.");
        else if (AFK_players.size() == 2)
            sender.sendMessage(COLOR + AFK_players.get(0) + " and " + AFK_players.get(1) + " are the only ones a.f.k. right now.");
        else {
            String list = COLOR + "";
            for (int i = 0; i < AFK_players.size() - 1; i++)
                list = list + AFK_players.get(i) + ", ";
            sender.sendMessage(list + " and " + AFK_players.get(AFK_players.size() - 1) + " are all a.f.k.");
        }
    }

    private void AFKToggle(CommandSender sender) {
        Player player = (Player) sender;
        if (!AFK_players.contains(player.getName())) {
            AFK_players.add(player.getName());
            server.broadcastMessage(COLOR + player.getName() + " is now a.f.k.");
        } else {
            AFK_players.remove(player.getName());
            server.broadcastMessage(COLOR + player.getName() + " is back.");
        }
    }

    private void announce(CommandSender sender) {
        byte extra_param = 0;
        Boolean is_important = null;
        // figure out if it's important, unimportant, or normal
        if (parameters[0].toLowerCase().startsWith("[i")) {
            extra_param++;
            is_important = true;
        } else if (parameters[0].toLowerCase().startsWith("[u")) {
            extra_param++;
            is_important = false;
        }
        // construct the announcement
        String announcement = "";
        for (int i = extra_param; i < parameters.length; i++) {
            announcement = announcement + parameters[i];
            if (i < parameters.length - 1)
                announcement = announcement + " ";
        }
        announcement = AutoCorrect(sender, announcement);
        if (is_important != null && is_important)
            server.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "THIS IS IMPORTANT! LISTEN UP!");
        String creator = "server";
        if (sender instanceof Player)
            creator = sender.getName();
        String epithet = epithets_by_user.get(creator);
        if (epithet == null)
            epithet = sender.getName();
        server.broadcastMessage(COLOR + colorCode(announcement) + "\n-- " + colorCode(epithet));
        if (creator.equals("server") && epithets_by_user.get("server") != null)
            creator = epithets_by_user.get("server");
        ArrayList<String> players_who_were_online = new ArrayList<String>();
        for (Player player : server.getOnlinePlayers())
            players_who_were_online.add(player.getName());
        announcements.add(new Announcement(announcement, creator, players_who_were_online, is_important));
    }

    private void changeEpithet(CommandSender sender) {
        Player player = null;
        if (sender instanceof Player)
            player = (Player) sender;
        String owner, epithet = "";
        int extra_param = 0;
        if (parameters[0].toLowerCase().startsWith("for:")) {
            owner = parameters[0].substring(4);
            extra_param++;
        } else if (player != null)
            owner = player.getName();
        else
            owner = "server";
        for (int i = extra_param; i < parameters.length; i++)
            if (!epithet.equals(""))
                epithet = epithet + " " + parameters[i];
            else
                epithet = parameters[i];
        if (player != null && !epithet.equals("")) {
            // eliminate preceding spaces
            epithet = epithet.trim();
            boolean epithet_is_acceptable = epithet.length() > 0;
            if (true_username_required && player != null && !player.hasPermission("myscribe.admin"))
                epithet_is_acceptable = epithet.length() > 0 && !epithet.contains("&k") && epithet.replaceAll("(&+([a-fA-Fk-oK-OrR0-9]))", "").contains(player.getName());
            if ((epithet_is_acceptable && player.getName().equals(owner)) || player.hasPermission("myscribe.admin")) {
                epithets_by_user.put(owner, epithet);
                if (player != null && player.getName().equalsIgnoreCase(owner))
                    player.sendMessage(COLOR + "Henceforth, you shall be known as \"" + colorCode(epithet) + COLOR + ".\"");
                else
                    sender.sendMessage(COLOR + owner + " shall henceforth be known as \"" + colorCode(epithet) + COLOR + ".\"");
            } else
                sender.sendMessage(ChatColor.RED + "Your epithet must contain your true username and not use magic.");
        } else if (owner.equalsIgnoreCase("server") && !epithet.equals("")) {
            epithets_by_user.put("console", epithet);
            sender.sendMessage(COLOR + "You set the \"/say\" epithet to \"" + colorCode(epithet) + COLOR + ".\"");
        } else if (epithet.equals(""))
            sender.sendMessage(ChatColor.RED + "You forgot to tell me the epithet you want!");
    }
}
