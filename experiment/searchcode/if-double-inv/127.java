/*     */ package com.skitscape.survivalgames;
/*     */ 
/*     */ import com.skitscape.survivalgames.stats.StatsManager;
/*     */ import com.skitscape.survivalgames.util.GameReset;
/*     */ import java.io.PrintStream;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.HashSet;
/*     */ import java.util.List;
/*     */ import java.util.Set;
/*     */ import org.bukkit.Bukkit;
/*     */ import org.bukkit.ChatColor;
/*     */ import org.bukkit.GameMode;
/*     */ import org.bukkit.Location;
/*     */ import org.bukkit.Material;
/*     */ import org.bukkit.Server;
/*     */ import org.bukkit.World;
/*     */ import org.bukkit.configuration.file.FileConfiguration;
/*     */ import org.bukkit.entity.EntityType;
/*     */ import org.bukkit.entity.Player;
/*     */ import org.bukkit.event.entity.EntityDamageEvent;
/*     */ import org.bukkit.inventory.ItemStack;
/*     */ import org.bukkit.inventory.PlayerInventory;
/*     */ import org.bukkit.plugin.PluginManager;
/*     */ import org.bukkit.scheduler.BukkitScheduler;
/*     */ 
/*     */ public class Game
/*     */ {
/*  28 */   private GameMode mode = GameMode.DISABLED;
/*  29 */   private ArrayList<Player> activePlayers = new ArrayList();
/*  30 */   private ArrayList<Player> inactivePlayers = new ArrayList();
/*  31 */   private ArrayList<String> spectators = new ArrayList();
/*  32 */   private ArrayList<Player> queue = new ArrayList();
/*  33 */   HashMap<Player, Integer> nextspec = new HashMap();
/*     */   private Arena arena;
/*     */   private int gameID;
/*     */   private int arenano;
/*  39 */   private int gcount = 0;
/*     */   private FileConfiguration c;
/*     */   private FileConfiguration s;
/*  42 */   private HashMap<Integer, Player> spawns = new HashMap();
/*  43 */   private HashMap<Player, ItemStack[][]> inv_store = new HashMap();
/*  44 */   private int spawnCount = 0;
/*  45 */   private int vote = 0;
/*  46 */   private boolean disabled = false;
/*  47 */   private int endgameTaskID = 0;
/*  48 */   private boolean endgameRunning = false;
/*  49 */   private double rbpercent = 0.0D;
/*  50 */   private String rbstatus = "";
/*  51 */   private long startTime = 0L;
/*     */   private boolean countdownRunning;
/*  53 */   private StatsManager sm = StatsManager.getInstance();
/*     */ 
/* 213 */   ArrayList<Player> voted = new ArrayList();
/*     */ 
/* 675 */   int counttime = 0;
/* 676 */   int threadsync = 0;
/*     */ 
/*     */   public Game(int gameid)
/*     */   {
/*  56 */     this.gameID = gameid;
/*  57 */     this.c = SettingsManager.getInstance().getConfig();
/*     */ 
/*  59 */     this.s = SettingsManager.getInstance().getSystemConfig();
/*  60 */     setup();
/*     */   }
/*     */ 
/*     */   public void setup()
/*     */   {
/*  65 */     this.mode = GameMode.LOADING;
/*  66 */     int x = this.s.getInt("sg-system.arenas." + this.gameID + ".x1");
/*  67 */     int y = this.s.getInt("sg-system.arenas." + this.gameID + ".y1");
/*  68 */     int z = this.s.getInt("sg-system.arenas." + this.gameID + ".z1");
/*  69 */     System.out.println(x + " " + y + " " + z);
/*  70 */     int x1 = this.s.getInt("sg-system.arenas." + this.gameID + ".x2");
/*  71 */     int y1 = this.s.getInt("sg-system.arenas." + this.gameID + ".y2");
/*  72 */     int z1 = this.s.getInt("sg-system.arenas." + this.gameID + ".z2");
/*  73 */     System.out.println(x1 + " " + y1 + " " + z1);
/*  74 */     Location max = new Location(SettingsManager.getGameWorld(this.gameID), Math.max(x, x1), Math.max(y, y1), Math.max(z, z1));
/*  75 */     System.out.println(max.toString());
/*  76 */     Location min = new Location(SettingsManager.getGameWorld(this.gameID), Math.min(x, x1), Math.min(y, y1), Math.min(z, z1));
/*  77 */     System.out.println(min.toString());
/*     */ 
/*  79 */     this.arena = new Arena(min, max);
/*     */ 
/*  81 */     loadspawns();
/*     */ 
/*  83 */     this.mode = GameMode.WAITING;
/*     */   }
/*     */ 
/*     */   public void loadspawns() {
/*  87 */     for (int a = 1; a <= SettingsManager.getInstance().getSpawnCount(this.gameID); a++) {
/*  88 */       this.spawns.put(Integer.valueOf(a), null);
/*  89 */       this.spawnCount = a;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void addSpawn() {
/*  94 */     this.spawnCount += 1;
/*  95 */     this.spawns.put(Integer.valueOf(this.spawnCount), null);
/*     */   }
/*     */ 
/*     */   public void setMode(GameMode m) {
/*  99 */     this.mode = m;
/*     */   }
/*     */ 
/*     */   public GameMode getGameMode() {
/* 103 */     return this.mode;
/*     */   }
/*     */ 
/*     */   public Arena getArena() {
/* 107 */     return this.arena;
/*     */   }
/*     */ 
/*     */   public void enable() {
/* 111 */     this.mode = GameMode.WAITING;
/*     */ 
/* 113 */     this.disabled = false;
/* 114 */     int b = SettingsManager.getInstance().getSpawnCount(this.gameID) > this.queue.size() ? this.queue.size() : SettingsManager.getInstance().getSpawnCount(this.gameID);
/* 115 */     for (int a = 0; a < b; a++) {
/* 116 */       addPlayer((Player)this.queue.remove(0));
/*     */     }
/* 118 */     int c = 1;
/* 119 */     for (Player p : this.queue) {
/* 120 */       p.sendMessage(ChatColor.GREEN + "You are now #" + c + " in line for arena " + this.gameID);
/* 121 */       c++;
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean addPlayer(Player p)
/*     */   {
/* 128 */     GameManager.getInstance().removeFromOtherQueues(p, this.gameID);
/*     */ 
/* 130 */     if ((GameManager.getInstance().getPlayerGameId(p) != -1) && 
/* 131 */       (GameManager.getInstance().isPlayerActive(p))) {
/* 132 */       p.sendMessage(ChatColor.RED + "Cannot join multiple games!");
/* 133 */       return false;
/*     */     }
/*     */ 
/* 136 */     if (this.spectators.contains(p)) removeSpectator(p);
/* 137 */     if ((this.mode == GameMode.WAITING) || (this.mode == GameMode.STARTING))
/*     */     {
/*     */       int a;
/* 138 */       if (this.activePlayers.size() < SettingsManager.getInstance().getSpawnCount(this.gameID)) {
/* 139 */         p.sendMessage("Joining Arena " + this.gameID);
/* 140 */         boolean placed = false;
/*     */ 
/* 142 */         for (a = 1; a <= SettingsManager.getInstance().getSpawnCount(this.gameID); a++) {
/* 143 */           if (this.spawns.get(Integer.valueOf(a)) == null)
/*     */           {
/* 145 */             placed = true;
/* 146 */             this.spawns.put(Integer.valueOf(a), p);
/* 147 */             p.teleport(SettingsManager.getInstance().getLobbySpawn());
/* 148 */             saveInv(p);
/* 149 */             clearInv(p);
/* 150 */             p.teleport(SettingsManager.getInstance().getSpawnPoint(this.gameID, a));
/*     */ 
/* 152 */             p.setHealth(20); p.setFoodLevel(20);
/* 153 */             clearInv(p);
/* 154 */             p.setGameMode(GameMode.SURVIVAL);
/* 155 */             this.activePlayers.add(p);
/* 156 */             this.sm.addPlayer(p, this.gameID);
/* 157 */             break;
/*     */           }
/*     */         }
/* 160 */         if (!placed) {
/* 161 */           p.sendMessage(ChatColor.RED + "Game " + this.gameID + " Is Full!");
/* 162 */           return false;
/*     */         }
/*     */       }
/*     */       else {
/* 166 */         if (SettingsManager.getInstance().getSpawnCount(this.gameID) == 0) {
/* 167 */           p.sendMessage(ChatColor.RED + "No spawns set for Arena " + this.gameID + "!");
/* 168 */           return false;
/*     */         }
/*     */ 
/* 171 */         p.sendMessage(ChatColor.RED + "Game " + this.gameID + " Is Full!");
/* 172 */         return false;
/*     */       }
/* 174 */       for (Player pl : this.activePlayers) {
/* 175 */         pl.sendMessage(ChatColor.GREEN + p.getName() + " joined the game! " + getActivePlayers() + "/" + SettingsManager.getInstance().getSpawnCount(this.gameID));
/*     */       }
/* 177 */       if ((this.activePlayers.size() >= this.c.getInt("auto-start-players")) && (!this.countdownRunning))
/* 178 */         countdown(this.c.getInt("auto-start-time"));
/* 179 */       return true;
/*     */     }
/*     */ 
/* 182 */     if (this.c.getBoolean("enable-player-queue")) {
/* 183 */       if (!this.queue.contains(p)) {
/* 184 */         this.queue.add(p);
/* 185 */         p.sendMessage(ChatColor.GREEN + "Added to queue line");
/*     */       }
/* 187 */       int a = 1;
/* 188 */       for (Player qp : this.queue) {
/* 189 */         if (qp == p) {
/* 190 */           p.sendMessage(ChatColor.AQUA + "You are #" + a + " in line");
/* 191 */           break;
/*     */         }
/* 193 */         a++;
/*     */       }
/*     */     }
/*     */ 
/* 197 */     if (this.mode == GameMode.INGAME)
/* 198 */       p.sendMessage(ChatColor.RED + "Game already started!");
/* 199 */     else if (this.mode == GameMode.DISABLED)
/* 200 */       p.sendMessage(ChatColor.RED + "Arena disabled!");
/* 201 */     else if (this.mode == GameMode.RESETING)
/* 202 */       p.sendMessage(ChatColor.RED + "The arena is reseting!");
/*     */     else {
/* 204 */       p.sendMessage(ChatColor.RED + "Cannot join the game!");
/*     */     }
/* 206 */     return false;
/*     */   }
/*     */ 
/*     */   public void removeFromQueue(Player p) {
/* 210 */     this.queue.remove(p);
/*     */   }
/*     */ 
/*     */   public void vote(Player pl)
/*     */   {
/* 217 */     if (GameMode.STARTING == this.mode) { pl.sendMessage(ChatColor.GREEN + "Game already starting!"); return; }
/* 218 */     if (GameMode.WAITING != this.mode) { pl.sendMessage(ChatColor.GREEN + "Game already started!"); return; }
/* 219 */     if (this.voted.contains(pl)) {
/* 220 */       pl.sendMessage(ChatColor.RED + "You already voted!");
/* 221 */       return;
/*     */     }
/* 223 */     this.vote += 1;
/* 224 */     this.voted.add(pl);
/* 225 */     pl.sendMessage(ChatColor.GREEN + "Voted to start the game!");
/*     */ 
/* 230 */     if (((this.vote + 0.0D) / (getActivePlayers() + 0.0D) >= (this.c.getInt("auto-start-vote") + 0.0D) / 100.0D) && (getActivePlayers() > 1)) {
/* 231 */       countdown(this.c.getInt("auto-start-time"));
/* 232 */       for (Player p : this.activePlayers)
/* 233 */         p.sendMessage(ChatColor.LIGHT_PURPLE + "Game Starting in " + this.c.getInt("auto-start-time"));
/*     */     }
/*     */   }
/*     */ 
/*     */   public void removePlayer(Player p, boolean b)
/*     */   {
/* 241 */     if (this.mode == GameMode.INGAME) {
/* 242 */       p.teleport(SettingsManager.getInstance().getLobbySpawn());
/* 243 */       killPlayer(p, b);
/*     */     }
/*     */     else {
/* 246 */       this.sm.removePlayer(p, this.gameID);
/* 247 */       if (!b)
/* 248 */         p.teleport(SettingsManager.getInstance().getLobbySpawn());
/* 249 */       restoreInv(p);
/* 250 */       this.activePlayers.remove(p);
/* 251 */       this.inactivePlayers.remove(p);
/* 252 */       for (Object in : this.spawns.keySet().toArray()) {
/* 253 */         if (this.spawns.get(in) == p) this.spawns.remove(in);
/*     */       }
/* 255 */       LobbyManager.getInstance().clearSigns();
/*     */     }
/* 257 */     GameManager.getInstance().removePlayerRefrence(p);
/*     */   }
/*     */ 
/*     */   public void playerLeave(Player p)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void killPlayer(Player p, boolean left)
/*     */   {
/* 269 */     GameManager.getInstance().removePlayerRefrence(p);
/* 270 */     clearInv(p);
/* 271 */     if (!left) {
/* 272 */       p.teleport(SettingsManager.getInstance().getLobbySpawn());
/*     */     }
/* 274 */     this.sm.playerDied(p, this.activePlayers.size(), this.gameID, new Date().getTime() - this.startTime);
/*     */ 
/* 276 */     if (!this.activePlayers.contains(p))
/* 277 */       return;
/* 278 */     if (!p.isOnline())
/* 279 */       restoreInvOffline(p.getName());
/*     */     else {
/* 281 */       restoreInv(p);
/*     */     }
/* 283 */     this.activePlayers.remove(p);
/* 284 */     this.inactivePlayers.add(p);
/* 285 */     if (left) {
/* 286 */       for (Player pl : getAllPlayers()) {
/* 287 */         pl.sendMessage(ChatColor.DARK_AQUA + p.getName() + " left the arena");
/*     */       }
/*     */ 
/*     */     }
/* 291 */     else if (this.mode != GameMode.WAITING) {
/* 292 */       String damagemsg = "";
/*     */       try {
/* 294 */         switch ($SWITCH_TABLE$org$bukkit$event$entity$EntityDamageEvent$DamageCause()[p.getLastDamageCause().getCause().ordinal()]) { case 11:
/* 295 */           damagemsg = "{player} Exploded!";
/* 296 */           break;
/*     */         case 10:
/* 297 */           damagemsg = "{player} Drowned!";
/* 298 */           break;
/*     */         case 2:
/* 299 */           damagemsg = EntDmgMsg(p, p.getLastDamageCause());
/* 300 */           break;
/*     */         case 5:
/* 301 */           damagemsg = "{player} hit the ground too hard!";
/* 302 */           break;
/*     */         case 9:
/* 303 */           damagemsg = "{player} burned in lava!";
/* 304 */           break;
/*     */         case 6:
/* 305 */           damagemsg = "{player} burned to death!";
/* 306 */           break;
/*     */         case 7:
/* 307 */           damagemsg = "{player} burned to death!";
/* 308 */           break;
/*     */         case 16:
/* 309 */           damagemsg = "{player} starved to death!";
/* 310 */           break;
/*     */         case 12:
/* 311 */           damagemsg = "{player} was creeper bombed!";
/* 312 */           break;
/*     */         case 3:
/*     */         case 4:
/*     */         case 8:
/*     */         case 13:
/*     */         case 14:
/*     */         case 15:
/*     */         default:
/* 313 */           damagemsg = "{player} died!";
/*     */         }
/*     */ 
/* 316 */         damagemsg = damagemsg.replace("{player}", (SurvivalGames.auth.contains(p.getName()) ? ChatColor.DARK_RED + ChatColor.BOLD : ChatColor.YELLOW) + p.getName() + ChatColor.RESET + ChatColor.DARK_AQUA);
/*     */ 
/* 318 */         if (getActivePlayers() > 1)
/* 319 */           for (Player pl : getAllPlayers()) {
/* 320 */             pl.sendMessage(ChatColor.DARK_AQUA + damagemsg);
/* 321 */             pl.sendMessage(ChatColor.DARK_AQUA + "There are " + ChatColor.YELLOW + getActivePlayers() + ChatColor.DARK_AQUA + " players remaining!");
/*     */           }
/*     */       }
/*     */       catch (Exception localException1)
/*     */       {
/*     */       }
/*     */     }
/* 328 */     for (Player pe : this.activePlayers) {
/* 329 */       Location l = pe.getLocation();
/* 330 */       l.setY(l.getWorld().getMaxHeight());
/* 331 */       l.getWorld().strikeLightningEffect(l);
/*     */     }
/*     */ 
/* 334 */     if ((getActivePlayers() <= this.c.getInt("endgame.players")) && (this.c.getBoolean("endgame.fire-lighting.enabled")) && (!this.endgameRunning)) {
/* 335 */       this.endgameRunning = true;
/*     */ 
/* 337 */       new EndgameManager().start();
/*     */     }
/*     */ 
/* 342 */     if ((this.activePlayers.size() < 2) && (this.mode != GameMode.WAITING)) {
/* 343 */       playerWin(p);
/* 344 */       endGame();
/*     */     }
/*     */   }
/*     */ 
/*     */   public String EntDmgMsg(Player p, EntityDamageEvent e) {
/* 349 */     if (e.getEntityType() == EntityType.PLAYER) {
/*     */       try {
/* 351 */         Player e1 = p.getKiller();
/* 352 */         this.sm.addKill(e1, p, this.gameID);
/* 353 */         Material m = e1.getItemInHand().getType();
/* 354 */         return (SurvivalGames.auth.contains(e1.getName()) ? ChatColor.DARK_RED + ChatColor.BOLD : ChatColor.YELLOW) + e1.getName() + ChatColor.RESET + ChatColor.DARK_AQUA + " Killed {player} with " + m + ". ";
/*     */       }
/*     */       catch (Exception e7) {
/* 357 */         return "{player} was killed by ";
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 362 */     String msg = "";
/* 363 */     switch ($SWITCH_TABLE$org$bukkit$entity$EntityType()[e.getEntityType().ordinal()]) {
/*     */     case 18:
/* 365 */       msg = "{player} was Creeper bombed!";
/* 366 */       break;
/*     */     case 24:
/* 368 */       msg = "{player} was fireballed by a ghast!";
/* 369 */       break;
/*     */     case 4:
/* 371 */       Player p5 = (Player)e;
/* 372 */       msg = p5.getName() + " shot {player}!";
/* 373 */       break;
/*     */     case 50:
/* 375 */       msg = "{player} was electrocuted!";
/* 376 */       break;
/*     */     case 27:
/* 378 */       msg = "{player} was killed by a Cave Spider!";
/* 379 */       break;
/*     */     default:
/* 381 */       msg = "{player} was killed by a " + e.getEntityType().toString().toLowerCase() + "!";
/*     */     }
/*     */ 
/* 384 */     return msg;
/*     */   }
/*     */ 
/*     */   public void playerWin(Player p)
/*     */   {
/* 390 */     if (GameMode.DISABLED == this.mode) return;
/* 391 */     Player win = (Player)this.activePlayers.get(0);
/*     */ 
/* 393 */     win.teleport(SettingsManager.getInstance().getLobbySpawn());
/*     */ 
/* 395 */     restoreInv(win);
/*     */ 
/* 397 */     Bukkit.getServer().broadcastMessage(ChatColor.DARK_AQUA + win.getName() + " won the Survival Games on arena " + this.gameID);
/* 398 */     LobbyManager.getInstance().showMessage(new String[] { win.getName(), "", "Won the ", "Survival Games!" });
/*     */ 
/* 400 */     this.mode = GameMode.FINISHING;
/*     */ 
/* 402 */     setRBStatus("Clearing Specs");
/* 403 */     clearSpecs();
/* 404 */     setRBStatus("Sw player;");
/* 405 */     this.sm.playerWin(win, this.gameID, new Date().getTime() - this.startTime);
/* 406 */     setRBStatus("Saving Game");
/* 407 */     this.sm.saveGame(this.gameID, win, getActivePlayers() + getInactivePlayers(), new Date().getTime() - this.startTime);
/* 408 */     setRBStatus("Clear active");
/*     */ 
/* 410 */     this.activePlayers.clear();
/* 411 */     setRBStatus("clear in");
/*     */ 
/* 413 */     this.inactivePlayers.clear();
/* 414 */     setRBStatus("clearing spawns");
/*     */ 
/* 416 */     this.spawns.clear();
/* 417 */     setRBStatus("loading spawns");
/*     */ 
/* 419 */     loadspawns();
/*     */   }
/*     */ 
/*     */   public void endGame() {
/* 423 */     this.mode = GameMode.WAITING;
/* 424 */     resetArena();
/* 425 */     LobbyManager.getInstance().clearSigns();
/* 426 */     this.endgameRunning = false;
/*     */   }
/*     */ 
/*     */   public void disable() {
/* 430 */     this.mode = GameMode.DISABLED;
/* 431 */     this.disabled = true;
/* 432 */     this.spawns.clear();
/*     */ 
/* 434 */     for (int a = 0; a < this.activePlayers.size(); a = 0)
/*     */       try
/*     */       {
/* 437 */         Player p = (Player)this.activePlayers.get(a);
/* 438 */         p.sendMessage(ChatColor.RED + "Game disabled");
/* 439 */         removePlayer(p, false);
/*     */       }
/*     */       catch (Exception localException)
/*     */       {
/*     */       }
/* 444 */     for (int a = 0; a < this.inactivePlayers.size(); a = 0)
/*     */       try
/*     */       {
/* 447 */         Player p = (Player)this.inactivePlayers.remove(a);
/* 448 */         p.sendMessage(ChatColor.RED + "Game disabled");
/*     */       }
/*     */       catch (Exception localException1)
/*     */       {
/*     */       }
/*     */     try
/*     */     {
/* 455 */       for (int a = 0; a < this.spectators.size(); a = 0) {
/* 456 */         String p = (String)this.spectators.get(a);
/* 457 */         Bukkit.getPlayer(p).sendMessage(ChatColor.RED + "Game disabled");
/* 458 */         removeSpectator(Bukkit.getPlayer(p));
/*     */       } } catch (Exception localException2) {  }
/*     */ 
/* 460 */     this.queue.clear();
/* 461 */     resetArena();
/* 462 */     GameManager.getInstance().clearPlayerCache();
/*     */   }
/*     */ 
/*     */   public void resetArena() {
/* 466 */     this.vote = 0;
/* 467 */     this.voted.clear();
/*     */ 
/* 469 */     this.mode = GameMode.RESETING;
/* 470 */     setRBStatus("starting");
/* 471 */     this.endgameRunning = false;
/* 472 */     Bukkit.getScheduler().cancelTask(this.endgameTaskID);
/* 473 */     GameManager.getInstance().gameEndCallBack(this.gameID);
/* 474 */     setRBStatus("starting...");
/* 475 */     GameReset r = new GameReset(this);
/* 476 */     r.resetArena();
/*     */   }
/*     */ 
/*     */   public void resetCallback() {
/* 480 */     if (!this.disabled)
/* 481 */       enable();
/*     */     else
/* 483 */       this.mode = GameMode.DISABLED;
/*     */   }
/*     */ 
/*     */   public void messageAll(String msg)
/*     */   {
/* 490 */     for (Player p : getAllPlayers())
/* 491 */       p.sendMessage(msg);
/*     */   }
/*     */ 
/*     */   public void saveInv(Player p)
/*     */   {
/* 498 */     ItemStack[][] store = new ItemStack[2][1];
/*     */ 
/* 500 */     store[0] = p.getInventory().getContents();
/* 501 */     store[1] = p.getInventory().getArmorContents();
/*     */ 
/* 503 */     this.inv_store.put(p, store);
/*     */   }
/*     */ 
/*     */   public void restoreInvOffline(String p)
/*     */   {
/* 508 */     restoreInv(Bukkit.getPlayer(p));
/*     */   }
/*     */ 
/*     */   public void addSpectator(Player p)
/*     */   {
/* 513 */     if (this.mode != GameMode.INGAME) {
/* 514 */       p.sendMessage(ChatColor.RED + "You Can only spectate running games!");
/* 515 */       return;
/*     */     }
/* 517 */     saveInv(p);
/* 518 */     clearInv(p);
/* 519 */     p.teleport(SettingsManager.getInstance().getSpawnPoint(this.gameID, 1).add(0.0D, 10.0D, 0.0D));
/*     */ 
/* 522 */     for (Player pl : Bukkit.getOnlinePlayers()) {
/* 523 */       pl.hidePlayer(p);
/*     */     }
/* 525 */     for (int a = 0; a < 9; a++) {
/* 526 */       p.getInventory().setItem(a, new ItemStack(59, 1));
/*     */     }
/* 528 */     p.updateInventory();
/*     */ 
/* 530 */     p.setAllowFlight(true);
/* 531 */     p.setFlying(true);
/* 532 */     this.spectators.add(p.getName());
/* 533 */     p.sendMessage(ChatColor.GREEN + "You are now spectating! /sg spectate again to return to lobby");
/* 534 */     p.sendMessage(ChatColor.GREEN + "Right click while holding shift to teleport to the next ingame player, left click to go back!");
/*     */ 
/* 536 */     this.nextspec.put(p, Integer.valueOf(0));
/*     */   }
/*     */ 
/*     */   public void removeSpectator(Player p)
/*     */   {
/* 541 */     ArrayList players = new ArrayList();
/* 542 */     players.addAll(this.activePlayers);
/* 543 */     players.addAll(this.inactivePlayers);
/*     */ 
/* 545 */     for (Player pl : Bukkit.getOnlinePlayers()) {
/* 546 */       pl.showPlayer(p);
/*     */     }
/*     */ 
/* 549 */     restoreInv(p);
/* 550 */     p.setAllowFlight(false);
/* 551 */     p.setFlying(false);
/* 552 */     p.setFallDistance(0.0F);
/* 553 */     p.setHealth(20);
/* 554 */     p.setFoodLevel(20);
/* 555 */     p.setSaturation(20.0F);
/* 556 */     p.teleport(SettingsManager.getInstance().getLobbySpawn());
/*     */ 
/* 558 */     for (String pl : (String[])this.spectators.toArray(new String[0])) {
/* 559 */       if (pl.equals(p.getName())) {
/* 560 */         this.spectators.remove(pl);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 565 */     this.nextspec.remove(p);
/*     */   }
/*     */ 
/*     */   public void clearSpecs() {
/* 569 */     Bukkit.getScheduler().scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("SurvivalGames"), new Runnable() {
/*     */       public void run() {
/* 571 */         ArrayList players = new ArrayList();
/* 572 */         players.addAll(Game.this.activePlayers);
/* 573 */         players.addAll(Game.this.inactivePlayers);
/*     */ 
/* 575 */         for (String p : Game.this.spectators)
/*     */           try {
/* 577 */             for (Player pl : players) {
/* 578 */               pl.showPlayer(Bukkit.getPlayer(p));
/*     */             }
/*     */ 
/* 581 */             Player player2 = Bukkit.getPlayer(p);
/* 582 */             Game.this.restoreInv(player2);
/* 583 */             player2.setAllowFlight(false);
/* 584 */             player2.setFlying(false);
/* 585 */             player2.setFallDistance(0.0F);
/* 586 */             player2.setHealth(20);
/* 587 */             player2.setFoodLevel(20);
/* 588 */             player2.setSaturation(20.0F);
/* 589 */             player2.teleport(SettingsManager.getInstance().getLobbySpawn());
/*     */           } catch (Exception localException) {
/*     */           }
/* 592 */         Game.this.spectators.clear();
/* 593 */         Game.this.nextspec.clear();
/*     */       }
/*     */     }
/*     */     , 100L);
/*     */   }
/*     */ 
/*     */   public HashMap<Player, Integer> getNextSpec() {
/* 599 */     return this.nextspec;
/*     */   }
/*     */ 
/*     */   public void restoreInv(Player p)
/*     */   {
/*     */     try {
/* 605 */       clearInv(p);
/* 606 */       p.getInventory().setContents(((ItemStack[][])this.inv_store.get(p))[0]);
/* 607 */       p.getInventory().setArmorContents(((ItemStack[][])this.inv_store.get(p))[1]);
/* 608 */       this.inv_store.remove(p);
/* 609 */       p.updateInventory();
/*     */     } catch (Exception localException) {
/*     */     }
/*     */   }
/*     */ 
/*     */   public void clearInv(Player p) {
/* 615 */     ItemStack[] inv = p.getInventory().getContents();
/* 616 */     for (int i = 0; i < inv.length; i++) {
/* 617 */       inv[i] = null;
/*     */     }
/* 619 */     p.getInventory().setContents(inv);
/* 620 */     inv = p.getInventory().getArmorContents();
/* 621 */     for (int i = 0; i < inv.length; i++) {
/* 622 */       inv[i] = null;
/*     */     }
/* 624 */     p.getInventory().setArmorContents(inv);
/* 625 */     p.updateInventory();
/*     */   }
/*     */ 
/*     */   public void startGame()
/*     */   {
/* 630 */     if (this.mode == GameMode.INGAME) {
/* 631 */       return;
/*     */     }
/*     */ 
/* 634 */     if (this.activePlayers.size() <= 1) {
/* 635 */       for (Player pl : this.activePlayers) {
/* 636 */         pl.sendMessage(ChatColor.RED + "Not Enought Players!");
/*     */       }
/* 638 */       return;
/*     */     }
/*     */ 
/* 641 */     this.startTime = new Date().getTime();
/* 642 */     for (Player pl : this.activePlayers) {
/* 643 */       pl.setHealth(20);
/* 644 */       pl.setHealth(20);
/* 645 */       clearInv(pl);
/* 646 */       pl.sendMessage(ChatColor.AQUA + "Good Luck!");
/*     */     }
/* 648 */     if (SettingsManager.getInstance().getConfig().getBoolean("restock-chest")) {
/* 649 */       SettingsManager.getGameWorld(this.gameID).setTime(0L);
/* 650 */       this.gcount += 1;
/* 651 */       new NightChecker().start();
/*     */     }
/* 653 */     if (SettingsManager.getInstance().getConfig().getInt("grace-period") != 0) {
/* 654 */       for (Player play : this.activePlayers) {
/* 655 */         play.sendMessage(ChatColor.LIGHT_PURPLE + "You have a " + SettingsManager.getInstance().getConfig().getInt("grace-period") + " second grace period!");
/*     */       }
/* 657 */       Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(GameManager.getInstance().getPlugin(), new Runnable() {
/*     */         public void run() {
/* 659 */           for (Player play : Game.this.activePlayers)
/* 660 */             play.sendMessage(ChatColor.LIGHT_PURPLE + "Grace period has ended!");
/*     */         }
/*     */       }
/*     */       , SettingsManager.getInstance().getConfig().getInt("grace-period") * 20);
/*     */     }
/*     */ 
/* 667 */     this.mode = GameMode.INGAME;
/*     */   }
/*     */ 
/*     */   public int getCountdownTime()
/*     */   {
/* 673 */     return this.counttime;
/*     */   }
/*     */ 
/*     */   public void countdown(int time)
/*     */   {
/* 679 */     this.threadsync += 1;
/* 680 */     this.mode = GameMode.STARTING;
/* 681 */     this.countdownRunning = true;
/* 682 */     this.counttime = time;
/* 683 */     if (time < 11) {
/* 684 */       for (Player p : this.activePlayers) {
/* 685 */         p.sendMessage(ChatColor.GREEN + "Game Starting in " + time);
/*     */       }
/*     */     }
/* 688 */     if ((SurvivalGames.isActive()) && (time > 0)) {
/* 689 */       new CountdownThread(time).start();
/*     */     }
/* 691 */     else if ((SurvivalGames.isActive()) && (time <= 0)) {
/* 692 */       this.countdownRunning = false;
/* 693 */       startGame();
/*     */     }
/*     */     else;
/*     */   }
/*     */ 
/*     */   public boolean isBlockInArena(Location v)
/*     */   {
/* 755 */     return this.arena.containsBlock(v);
/*     */   }
/*     */ 
/*     */   public boolean isProtectionOn() {
/* 759 */     long t = this.startTime / 1000L;
/* 760 */     long l = SettingsManager.getInstance().getConfig().getLong("grace-period");
/* 761 */     long d = new Date().getTime() / 1000L;
/*     */ 
/* 763 */     if (d - t < l) return true;
/* 764 */     return false;
/*     */   }
/*     */ 
/*     */   public int getID()
/*     */   {
/* 769 */     return this.gameID;
/*     */   }
/*     */ 
/*     */   public int getActivePlayers()
/*     */   {
/* 774 */     return this.activePlayers.size();
/*     */   }
/*     */ 
/*     */   public int getInactivePlayers() {
/* 778 */     return this.inactivePlayers.size();
/*     */   }
/*     */ 
/*     */   public Player[][] getPlayers() {
/* 782 */     return new Player[][] { (Player[])this.activePlayers.toArray(new Player[0]), (Player[])this.inactivePlayers.toArray(new Player[0]) };
/*     */   }
/*     */ 
/*     */   public ArrayList<Player> getAllPlayers() {
/* 786 */     ArrayList all = new ArrayList();
/* 787 */     all.addAll(this.activePlayers);
/* 788 */     all.addAll(this.inactivePlayers);
/* 789 */     return all;
/*     */   }
/*     */ 
/*     */   public boolean isSpectator(Player p) {
/* 793 */     return this.spectators.contains(p.getName());
/*     */   }
/*     */ 
/*     */   public boolean isInQueue(Player p) {
/* 797 */     return this.queue.contains(p);
/*     */   }
/*     */ 
/*     */   public boolean isPlayerActive(Player player) {
/* 801 */     return this.activePlayers.contains(player);
/*     */   }
/*     */   public boolean isPlayerinactive(Player player) {
/* 804 */     return this.inactivePlayers.contains(player);
/*     */   }
/*     */   public boolean hasPlayer(Player p) {
/* 807 */     return (this.activePlayers.contains(p)) || (this.inactivePlayers.contains(p));
/*     */   }
/*     */   public GameMode getMode() {
/* 810 */     return this.mode;
/*     */   }
/*     */ 
/*     */   public synchronized void setRBPercent(double d) {
/* 814 */     this.rbpercent = d;
/*     */   }
/*     */ 
/*     */   public double getRBPercent()
/*     */   {
/* 819 */     return this.rbpercent;
/*     */   }
/*     */ 
/*     */   public void setRBStatus(String s) {
/* 823 */     this.rbstatus = s;
/*     */   }
/*     */ 
/*     */   public String getRBStatus() {
/* 827 */     return this.rbstatus;
/*     */   }
/*     */ 
/*     */   class CountdownThread extends Thread
/*     */   {
/*     */     int time;
/* 702 */     int trun = Game.this.threadsync;
/*     */ 
/* 704 */     public CountdownThread(int t) { this.time = t; }
/*     */ 
/*     */     public void run() {
/* 707 */       this.time -= 1;
/*     */       try { Thread.sleep(1000L); } catch (Exception localException) {
/* 709 */       }if (this.trun == Game.this.threadsync)
/* 710 */         Game.this.countdown(this.time);
/*     */     }
/*     */   }
/*     */ 
/*     */   class EndgameManager extends Thread
/*     */   {
/*     */     EndgameManager()
/*     */     {
/*     */     }
/*     */ 
/*     */     public void run()
/*     */     {
/* 740 */       while (Game.this.endgameRunning) {
/* 741 */         for (Player player : (Player[])Game.this.activePlayers.toArray(new Player[0])) {
/* 742 */           Location l = player.getLocation();
/* 743 */           l.add(0.0D, 5.0D, 0.0D);
/* 744 */           player.getWorld().strikeLightningEffect(l);
/*     */         }try {
/* 746 */           Thread.sleep(SettingsManager.getInstance().getConfig().getInt("endgame.fire-lighting.interval") * 1000);
/*     */         }
/*     */         catch (Exception localException)
/*     */         {
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static enum GameMode
/*     */   {
/*  24 */     DISABLED, LOADING, INACTIVE, WAITING, 
/*  25 */     STARTING, INGAME, FINISHING, RESETING, ERROR;
/*     */   }
/*     */ 
/*     */   class NightChecker extends Thread
/*     */   {
/* 717 */     boolean reset = false;
/* 718 */     int tgc = Game.this.gcount;
/*     */ 
/*     */     NightChecker() {  } 
/* 720 */     public void run() { while ((!this.reset) && (Game.this.mode == Game.GameMode.INGAME) && (this.tgc == Game.this.gcount)) {
/*     */         try { Thread.sleep(5000L); } catch (Exception localException) {
/* 722 */         }if (SettingsManager.getGameWorld(Game.this.gameID).getTime() > 14000L) {
/* 723 */           for (Player pl : Game.this.activePlayers) {
/* 724 */             pl.sendMessage(ChatColor.AQUA + "Chest have been restocked!");
/*     */           }
/* 726 */           ((HashSet)GameManager.openedChest.get(Integer.valueOf(Game.this.gameID))).clear();
/* 727 */           this.reset = true;
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Users\SUPERCOMPUTER\Desktop\SGAMESCONFIG1\SurvivalGames_B_0.4.10 (1)\SurvivalGames B 0.4.10\jd-gui-0.3.5.windows\SurvivalGames (2)\
 * Qualified Name:     com.skitscape.survivalgames.Game
 * JD-Core Version:    0.6.2
 */
