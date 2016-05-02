/*     */ package com.skitscape.survivalgames;
/*     */ 
/*     */ import com.sk89q.worldedit.Vector;
/*     */ import com.sk89q.worldedit.bukkit.WorldEditPlugin;
/*     */ import com.sk89q.worldedit.bukkit.selections.Selection;
/*     */ import java.io.PrintStream;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashSet;
/*     */ import java.util.List;
/*     */ import org.bukkit.Bukkit;
/*     */ import org.bukkit.ChatColor;
/*     */ import org.bukkit.Chunk;
/*     */ import org.bukkit.Location;
/*     */ import org.bukkit.Server;
/*     */ import org.bukkit.World;
/*     */ import org.bukkit.block.Block;
/*     */ import org.bukkit.block.BlockState;
/*     */ import org.bukkit.block.Sign;
/*     */ import org.bukkit.configuration.file.FileConfiguration;
/*     */ import org.bukkit.entity.Player;
/*     */ import org.bukkit.event.Listener;
/*     */ import org.bukkit.material.MaterialData;
/*     */ import org.bukkit.scheduler.BukkitScheduler;
/*     */ 
/*     */ public class LobbyManager
/*     */   implements Listener
/*     */ {
/*     */   Sign[][] signs;
/*     */   SurvivalGames p;
/*  27 */   private int runningThread = 0;
/*  28 */   private static LobbyManager instance = new LobbyManager();
/*  29 */   public HashSet<Chunk> lobbychunks = new HashSet();
/*     */ 
/* 164 */   boolean showingMessage = false;
/* 165 */   ArrayList<String[]> messagequeue = new ArrayList(3);
/*     */   private boolean error;
/* 189 */   int tid = 0;
/*     */ 
/*     */   public static LobbyManager getInstance()
/*     */   {
/*  36 */     return instance;
/*     */   }
/*     */ 
/*     */   public void setup(SurvivalGames p)
/*     */   {
/*  42 */     this.p = p;
/*  43 */     loadSigns();
/*     */   }
/*     */ 
/*     */   public void loadSigns()
/*     */   {
/*  48 */     FileConfiguration c = SettingsManager.getInstance().getSystemConfig();
/*     */     try {
/*  50 */       if (!c.getBoolean("sg-system.lobby.sign.set"))
/*  51 */         return; 
/*     */     } catch (Exception e) { return; }
/*  53 */     boolean usingx = false;
/*  54 */     int hdiff = 0;
/*  55 */     int x1 = c.getInt("sg-system.lobby.sign.x1");
/*  56 */     int y1 = c.getInt("sg-system.lobby.sign.y1");
/*  57 */     int z1 = c.getInt("sg-system.lobby.sign.z1");
/*  58 */     int x2 = c.getInt("sg-system.lobby.sign.x2");
/*  59 */     int y2 = c.getInt("sg-system.lobby.sign.y2");
/*  60 */     int z2 = c.getInt("sg-system.lobby.sign.z2");
/*  61 */     int inc = 0;
/*     */ 
/*  64 */     byte temp = ((Sign)new Location(this.p.getServer().getWorld(c.getString("sg-system.lobby.sign.world")), x1, y1, z1).getBlock().getState()).getData().getData();
/*     */     Location l;
/*  66 */     if ((temp == 3) || (temp == 4)) {
/*  67 */       Location l = new Location(Bukkit.getWorld(c.getString("sg-system.lobby.sign.world")), x1, y1, z1);
/*  68 */       inc = -1;
/*     */     } else {
/*  70 */       l = new Location(Bukkit.getWorld(c.getString("sg-system.lobby.sign.world")), x2, y1, z2);
/*  71 */       inc = 1;
/*     */     }
/*     */ 
/*  75 */     usingx = x2 - x1 != 0;
/*  76 */     if (usingx) {
/*  77 */       hdiff = x1 - x2 + 1;
/*     */     }
/*     */     else {
/*  80 */       hdiff = z1 - z2 + 1;
/*     */     }
/*  82 */     int vdiff = y1 - y2 + 1;
/*     */ 
/*  85 */     System.out.println(vdiff + "              " + hdiff);
/*  86 */     this.signs = new Sign[vdiff][hdiff];
/*  87 */     for (int y = vdiff - 1; y >= 0; y--) {
/*  88 */       for (int x = hdiff - 1; x >= 0; x--)
/*     */       {
/*  91 */         BlockState b = this.p.getServer().getWorld(SettingsManager.getInstance().getSystemConfig().getString("sg-system.lobby.sign.world")).getBlockAt(l).getState();
/*  92 */         this.lobbychunks.add(b.getChunk());
/*  93 */         if ((b instanceof Sign)) {
/*  94 */           this.signs[y][x] = ((Sign)b);
/*     */         }
/*  96 */         if (usingx)
/*  97 */           l = l.add(inc, 0.0D, 0.0D);
/*     */         else {
/*  99 */           l = l.add(0.0D, 0.0D, inc);
/*     */         }
/*     */       }
/* 102 */       l = l.add(0.0D, -1.0D, 0.0D);
/* 103 */       if (inc == -1) {
/* 104 */         l.setX(x1);
/* 105 */         l.setZ(z1);
/*     */       }
/*     */       else {
/* 108 */         l.setX(x2);
/* 109 */         l.setZ(z2);
/*     */       }
/*     */     }
/* 112 */     this.runningThread += 1;
/* 113 */     showMessage(new String[] { "", "Survival Games", "", "Double0negative", "iMalo", "mc-sg.org", "" });
/*     */   }
/*     */ 
/*     */   public int[] getSignMidPoint()
/*     */   {
/* 119 */     double x = this.signs[0].length * 8;
/* 120 */     double y = this.signs.length * 2;
/*     */ 
/* 122 */     return new int[] { (int)x, (int)y };
/*     */   }
/*     */ 
/*     */   public void setLobbySignsFromSelection(Player pl)
/*     */   {
/* 128 */     FileConfiguration c = SettingsManager.getInstance().getSystemConfig();
/* 129 */     SettingsManager s = SettingsManager.getInstance();
/* 130 */     if (!c.getBoolean("sg-system.lobby.sign.set", false)) {
/* 131 */       c.set("sg-system.lobby.sign.set", Boolean.valueOf(true));
/* 132 */       s.saveSystemConfig();
/*     */     }
/*     */ 
/* 135 */     WorldEditPlugin we = this.p.getWorldEdit();
/* 136 */     Selection sel = we.getSelection(pl);
/* 137 */     if (sel == null) {
/* 138 */       pl.sendMessage(ChatColor.RED + "You must make a WorldEdit Selection first");
/* 139 */       return;
/*     */     }
/* 141 */     if ((sel.getNativeMaximumPoint().getBlockX() - sel.getNativeMaximumPoint().getBlockX() != 0) && (sel.getNativeMaximumPoint().getBlockZ() - sel.getNativeMaximumPoint().getBlockZ() != 0)) {
/* 142 */       pl.sendMessage(ChatColor.RED + " Must be in a straight line!");
/* 143 */       return;
/*     */     }
/*     */ 
/* 146 */     Vector max = sel.getNativeMaximumPoint();
/* 147 */     Vector min = sel.getNativeMinimumPoint();
/*     */ 
/* 149 */     c.set("sg-system.lobby.sign.world", pl.getWorld().getName());
/* 150 */     c.set("sg-system.lobby.sign.x1", Integer.valueOf(max.getBlockX()));
/* 151 */     c.set("sg-system.lobby.sign.y1", Integer.valueOf(max.getBlockY()));
/* 152 */     c.set("sg-system.lobby.sign.z1", Integer.valueOf(max.getBlockZ()));
/* 153 */     c.set("sg-system.lobby.sign.x2", Integer.valueOf(min.getBlockX()));
/* 154 */     c.set("sg-system.lobby.sign.y2", Integer.valueOf(min.getBlockY()));
/* 155 */     c.set("sg-system.lobby.sign.z2", Integer.valueOf(min.getBlockZ()));
/*     */ 
/* 157 */     pl.sendMessage(ChatColor.GREEN + "Lobby Status wall successfuly created");
/* 158 */     s.saveSystemConfig();
/* 159 */     loadSigns();
/*     */   }
/*     */ 
/*     */   public void showMessage(String[] msg9)
/*     */   {
/* 170 */     signShowMessage(msg9);
/*     */   }
/*     */ 
/*     */   public void signShowMessage(String[] msg)
/*     */   {
/* 186 */     signShowMessage(msg, 5000L);
/*     */   }
/*     */ 
/*     */   public void signShowMessage(String[] msg9, long wait)
/*     */   {
/*     */     try
/*     */     {
/* 193 */       this.runningThread += 1;
/*     */ 
/* 195 */       this.messagequeue.add(msg9);
/* 196 */       if (this.showingMessage)
/* 197 */         return;
/* 198 */       this.showingMessage = true;
/* 199 */       if (this.tid != 0) {
/* 200 */         Bukkit.getScheduler().cancelTask(this.tid);
/*     */       }
/*     */ 
/* 240 */       clearSigns();
/*     */ 
/* 242 */       for (int c = 0; c < this.messagequeue.size(); c++) {
/* 243 */         String[] msg = (String[])this.messagequeue.get(c);
/* 244 */         int x = getSignMidPoint()[1] - msg.length / 2;
/* 245 */         int lineno = x % 3;
/* 246 */         x /= 4;
/* 247 */         for (int a = msg.length - 1; a > -1; a--) {
/* 248 */           int y = getSignMidPoint()[0] - msg[a].length() / 2;
/*     */ 
/* 251 */           char[] line = msg[a].toCharArray();
/* 252 */           for (int b = 0; b < line.length; b++)
/*     */           {
/* 255 */             Sign sig = this.signs[x][(y / 16)];
/* 256 */             sig.setLine(lineno, sig.getLine(lineno) + line[b]);
/*     */ 
/* 258 */             this.signs[x][(y / 16)].update();
/*     */ 
/* 260 */             y++;
/*     */           }
/* 262 */           if (lineno == 0) {
/* 263 */             lineno = 3;
/* 264 */             x++;
/*     */           }
/*     */           else {
/* 267 */             lineno--;
/*     */           }
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 273 */       this.tid = Bukkit.getScheduler().scheduleSyncDelayedTask(this.p, new Runnable() {
/*     */         public void run() {
/* 275 */           Bukkit.getScheduler().scheduleSyncRepeatingTask(LobbyManager.this.p, new LobbyManager.LobbySignUpdater(LobbyManager.this), 1L, 20L);
/* 276 */           LobbyManager.this.clearSigns();
/*     */         }
/*     */       }
/*     */       , 100L);
/*     */ 
/* 283 */       this.messagequeue.clear();
/*     */ 
/* 285 */       this.showingMessage = false;
/*     */     }
/*     */     catch (Exception localException)
/*     */     {
/*     */     }
/*     */   }
/*     */ 
/*     */   public void updateGameStatus()
/*     */   {
/* 310 */     int b = this.signs.length - 1;
/* 311 */     if (!SurvivalGames.config_todate) {
/* 312 */       this.signs[b][0].setLine(0, ChatColor.RED + "CONFIG");
/* 313 */       this.signs[b][0].setLine(1, ChatColor.RED + "OUTDATED!");
/* 314 */       this.signs[b][1].setLine(0, ChatColor.RED + "Please reset");
/* 315 */       this.signs[b][1].setLine(1, ChatColor.RED + "your config");
/* 316 */       this.signs[b][0].update();
/* 317 */       this.signs[b][1].update();
/* 318 */       return;
/*     */     }
/* 320 */     if (!SurvivalGames.dbcon) {
/* 321 */       this.signs[b][0].setLine(0, ChatColor.RED + "No Database");
/* 322 */       this.signs[b][0].update();
/* 323 */       return;
/*     */     }
/* 325 */     if (GameManager.getInstance().getGameCount() == 0) {
/* 326 */       this.signs[b][0].setLine(1, ChatColor.RED + "No Arenas");
/* 327 */       this.signs[b][0].update();
/* 328 */       return;
/*     */     }
/*     */     try {
/* 331 */       SettingsManager.getInstance().getLobbySpawn();
/*     */     }
/*     */     catch (Exception e) {
/* 334 */       this.signs[b][0].setLine(1, ChatColor.RED + "No Lobby spawn!");
/* 335 */       this.signs[b][0].update();
/* 336 */       return;
/*     */     }
/* 338 */     if (this.error) {
/* 339 */       this.signs[b][0].setLine(1, ChatColor.RED + "Error");
/* 340 */       this.signs[b][0].update();
/* 341 */       return;
/*     */     }
/*     */ 
/* 344 */     ArrayList games = GameManager.getInstance().getGames();
/*     */ 
/* 346 */     for (int a = 0; a < games.size(); a++)
/*     */       try {
/* 348 */         Game game = (Game)games.get(a);
/*     */ 
/* 350 */         this.signs[b][0].setLine(0, "[SurvivalGames]");
/* 351 */         this.signs[b][0].setLine(1, "Click to join");
/* 352 */         this.signs[b][0].setLine(2, "Arena " + game.getID());
/* 353 */         this.signs[b][1].setLine(0, "Arena " + game.getID());
/* 354 */         this.signs[b][1].setLine(1, game.getMode());
/* 355 */         this.signs[b][1].setLine(2, game.getActivePlayers() + 
/* 356 */           "/" + ChatColor.GRAY + game.getInactivePlayers() + ChatColor.BLACK + 
/* 357 */           "/" + SettingsManager.getInstance().getSpawnCount(game.getID()));
/* 358 */         if (game.getMode() == Game.GameMode.STARTING) {
/* 359 */           this.signs[b][1].setLine(3, game.getCountdownTime());
/* 360 */         } else if ((game.getMode() == Game.GameMode.RESETING) || (game.getGameMode() == Game.GameMode.FINISHING)) {
/* 361 */           this.signs[b][2].setLine(3, game.getRBStatus());
/* 362 */           if (game.getRBPercent() > 100.0D) {
/* 363 */             this.signs[b][a].setLine(1, "Saving Queue");
/* 364 */             this.signs[b][1].setLine(3, (int)game.getRBPercent() + " left");
/*     */           }
/*     */           else
/*     */           {
/* 368 */             this.signs[b][1].setLine(3, (int)game.getRBPercent() + "%");
/*     */           }
/*     */         } else {
/* 371 */           this.signs[b][1].setLine(3, "");
/* 372 */         }this.signs[b][0].update();
/* 373 */         this.signs[b][1].update();
/* 374 */         this.signs[b][2].update();
/*     */ 
/* 376 */         int signno = 2;
/* 377 */         int line = 0;
/* 378 */         Player[] active = game.getPlayers()[0];
/* 379 */         Player[] inactive = game.getPlayers()[1];
/* 380 */         for (Player p : active) {
/* 381 */           if (signno < this.signs[b].length)
/*     */           {
/* 383 */             this.signs[b][signno].setLine(line, (SurvivalGames.auth.contains(p.getName()) ? ChatColor.DARK_BLUE : ChatColor.BLACK) + (p.getName().equalsIgnoreCase("Double0negative") ? "Double0" : p.getName()));
/* 384 */             this.signs[b][signno].update();
/*     */ 
/* 386 */             line++;
/* 387 */             if (line == 4) {
/* 388 */               line = 0;
/* 389 */               signno++;
/*     */             }
/*     */           }
/*     */         }
/* 393 */         for (Player p : inactive) {
/* 394 */           if (signno < this.signs[b].length) {
/* 395 */             this.signs[b][signno].setLine(line, (SurvivalGames.auth.contains(p.getName()) ? ChatColor.DARK_RED : ChatColor.GRAY) + (p.getName().equalsIgnoreCase("Double0negative") ? "Double0" : p.getName()));
/* 396 */             this.signs[b][signno].update();
/* 397 */             line++;
/* 398 */             if (line == 4) {
/* 399 */               line = 0;
/* 400 */               signno++;
/*     */             }
/*     */ 
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 407 */         b--; } catch (Exception e) {
/* 408 */         e.printStackTrace(); this.signs[0][0].setLine(1, ChatColor.RED + "ERROR"); this.signs[0][0].setLine(1, ChatColor.RED + "Check Console");
/*     */       }
/*     */   }
/*     */ 
/*     */   public void clearSigns()
/*     */   {
/*     */     try
/*     */     {
/* 417 */       for (int y = this.signs.length - 1; y != -1; y--)
/* 418 */         for (int a = 0; a < 4; a++)
/*     */         {
/* 420 */           for (int x = 0; x != this.signs[0].length; x++)
/*     */           {
/* 422 */             Sign sig = this.signs[y][x];
/* 423 */             sig.setLine(a, "");
/* 424 */             sig.update();
/*     */           }
/*     */         }
/*     */     }
/*     */     catch (Exception localException)
/*     */     {
/*     */     }
/*     */   }
/*     */ 
/*     */   public void error(boolean e) {
/* 434 */     this.error = e;
/*     */   }
/*     */ 
/*     */   class LobbySignUpdater
/*     */     implements Runnable
/*     */   {
/*     */     LobbySignUpdater()
/*     */     {
/*     */     }
/*     */ 
/*     */     public void run()
/*     */     {
/* 297 */       LobbyManager.this.updateGameStatus();
/*     */     }
/*     */   }
/*     */ 
/*     */   class ThreadMessageDisplay extends Thread
/*     */   {
/*     */     String[] message;
/*     */ 
/*     */     ThreadMessageDisplay(String[] msg)
/*     */     {
/* 177 */       this.message = msg;
/*     */     }
/*     */ 
/*     */     public void run() {
/* 181 */       LobbyManager.this.signShowMessage(this.message);
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Users\SUPERCOMPUTER\Desktop\SGAMESCONFIG1\SurvivalGames_B_0.4.10 (1)\SurvivalGames B 0.4.10\jd-gui-0.3.5.windows\SurvivalGames (2)\
 * Qualified Name:     com.skitscape.survivalgames.LobbyManager
 * JD-Core Version:    0.6.2
 */
