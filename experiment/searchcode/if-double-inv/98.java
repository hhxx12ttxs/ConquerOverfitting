package me.dalton.capturethepoints.listeners;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import me.dalton.capturethepoints.CaptureThePoints;
import me.dalton.capturethepoints.HealingItems;
import me.dalton.capturethepoints.Util;
import me.dalton.capturethepoints.beans.Items;
import me.dalton.capturethepoints.beans.Spawn;
import me.dalton.capturethepoints.events.CTPPlayerDeathEvent;
import me.dalton.capturethepoints.util.PotionManagement;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.Wool;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CaptureThePointsEntityListener  implements Listener {

    private final CaptureThePoints ctp;

    public CaptureThePointsEntityListener(CaptureThePoints plugin) {
        this.ctp = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!ctp.isGameRunning())
            return;
        if(ctp.getGlobalConfigOptions().enableHardArenaRestore)
            return;

        if (ctp.playerListener.isInside(event.getLocation().getBlockX(), ctp.mainArena.getX1(), ctp.mainArena.getX2()) && ctp.playerListener.isInside(event.getLocation().getBlockY(), ctp.mainArena.getY1(), ctp.mainArena.getY2()) && ctp.playerListener.isInside(event.getLocation().getBlockZ(), ctp.mainArena.getZ1(), ctp.mainArena.getZ2()) && event.getLocation().getWorld().getName().equalsIgnoreCase(ctp.mainArena.getWorld())) {
            List<Block> explodedBlocks = event.blockList();

            for (Block block : explodedBlocks)
                ctp.arenaRestore.addBlock(block, true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;
        
        if((this.ctp.playerData.get(((Player) event.getEntity()).getName()) == null))
            return;
        
        if(!ctp.isGameRunning() && this.ctp.playerData.get(((Player) event.getEntity()).getName()).inLobby())  {
            event.setDroppedExp(0);
            event.getDrops().clear();
            return;
        }

        event.setDroppedExp(0);
        event.getDrops().clear();
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void healthRegain(EntityRegainHealthEvent event) {
    	if (!(event.getEntity() instanceof Player)) return;
    	
    	 if (ctp.isGameRunning()) {
             if ((this.ctp.playerData.get(((Player) event.getEntity()).getName()) != null)) {
            	 if(!ctp.mainArena.getConfigOptions().regainHealth) {
	            	 if (event.getRegainReason() == RegainReason.SATIATED) {
	             		event.setCancelled(true);
	             		if(ctp.getGlobalConfigOptions().debugMessages)
	                    	ctp.getLogger().info("Just cancelled a EntityRegainHealthEvent as you have it turned off during the game.");
	             	}else return;
            	 }else return;
             }else return;
    	 }else return;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPotionEffect(PotionSplashEvent event) {
        // lobby damage check


        if (ctp.isGameRunning()) {
            Player thrower = (Player) event.getEntity().getShooter();
            
            if (ctp.playerData.get(thrower.getName()) != null) {
                ThrownPotion potion = event.getEntity();
                PotionEffect effect = null;
                boolean harmful = false;
                for(PotionEffect e: potion.getEffects()){
                	effect = e;
                }
                
                harmful = PotionManagement.isHarmful(effect);
                for(Iterator<LivingEntity> iter = event.getAffectedEntities().iterator(); iter.hasNext();){
                	LivingEntity hitPlayerEntity = iter.next();
                	Player hitPlayer = (Player)hitPlayerEntity;
                	
                	//Is potion negative/positive
                	if(harmful){	                   //Negative
                		//If hit self
                		if(thrower.equals(hitPlayer)){
                			event.setIntensity(hitPlayerEntity, 0); 
                		}
                		//Is thrower on the same team as player hit
                		if (this.ctp.playerData.get(thrower.getName()).getTeam().getColor().equalsIgnoreCase(this.ctp.playerData.get(hitPlayer.getName()).getTeam().getColor())){ // Yes
                			event.setIntensity(hitPlayerEntity, 0); 
                		}else{ // No
                            if (isProtected(hitPlayer)) {
                            	event.setIntensity(hitPlayerEntity, 0);                		                	
                            }	
                		}
                        //Player has "died"
                        if(effect.getType().equals(PotionEffectType.HARM)){
                        	int damage = 6;
                        	
                        	if(effect.getAmplifier()==1){
                        		damage = 12;
                        	}
                        	
                        	double intensity = event.getIntensity(hitPlayerEntity);
                        	
                        	double tmpDamage = ((double)damage)*intensity;
                        	damage = (int) tmpDamage;
                        	
                        	tmpDamage = tmpDamage - ((int)tmpDamage);
                        	
                        	if(tmpDamage >= .5){
                        		damage++;
                        	}
                        	
                            if ((this.ctp.playerData.get(hitPlayer.getName()) != null) && (hitPlayer.getHealth() - damage <= 0)) {
                            	event.setIntensity(hitPlayerEntity, 0); 
                                respawnPlayer(hitPlayer, thrower);
                            }
                        }
                	}else{                            //Positive
                		if (!this.ctp.playerData.get(thrower.getName()).getTeam().getColor().equalsIgnoreCase(this.ctp.playerData.get(hitPlayer.getName()).getTeam().getColor())){ 
                			event.setIntensity(hitPlayerEntity, 0); 
                		}
                	}
                }
            }
        }
    }


	@EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            // Kj -- Didn't involve a player. So we don't care.
            return;
        }

        //Only check if game is running
        if (ctp.isGameRunning()) {
            Player attacker = null;
            if ((this.ctp.playerData.get(((Player) event.getEntity()).getName()) != null)) {

                // for melee
                if (checkForPlayerEvent(event)) {
                    attacker = ((Player) ((EntityDamageByEntityEvent) event).getDamager());
                }

                // for arrows
                if ((event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) && (((Projectile) ((EntityDamageByEntityEvent) event).getDamager()).getShooter() instanceof Player)) {
                    attacker = (Player) ((Projectile) ((EntityDamageByEntityEvent) event).getDamager()).getShooter();
                }

                Player playa = (Player) event.getEntity();

                // lobby damage check
                if (this.ctp.playerData.get(playa.getName()).inLobby() || (attacker != null && this.ctp.playerData.get(attacker.getName()) != null && this.ctp.playerData.get(attacker.getName()).inLobby())) {
                    event.setCancelled(true);
                    if(ctp.getGlobalConfigOptions().debugMessages)
                    	ctp.getLogger().info("Just cancelled a EntityDamageEvent because the player is in the lobby.");
                    return;
                }

                if (isProtected(playa)) {
                    // If you damage yourself
                    if (attacker != null) {
                    	ctp.sendMessage(attacker, ChatColor.LIGHT_PURPLE + "You can't damage enemy in their spawn!");
                    }
                    
                    event.setCancelled(true);
                    if(ctp.getGlobalConfigOptions().debugMessages)
                    	ctp.getLogger().info("Just cancelled a EntityDamageEvent because the player is in his/her spawn area.");
                    return;
                }

                //disable pvp damage
                if (attacker != null) {
                    if ((this.ctp.playerData.get(playa.getName()) != null) && (this.ctp.playerData.get(attacker.getName()) != null)) {
                        if (this.ctp.playerData.get(playa.getName()).getTeam().getColor().equalsIgnoreCase(this.ctp.playerData.get(attacker.getName()).getTeam().getColor())) {
                        	ctp.sendMessage(attacker, ctp.playerData.get(playa.getName()).getTeam().getChatColor() + playa.getName() + ChatColor.LIGHT_PURPLE + " is on your team!");
                            event.setCancelled(true);
                            if(ctp.getGlobalConfigOptions().debugMessages)
                            	ctp.getLogger().info("Just cancelled a EntityDamageEvent because the player is on the same team as the attacker.");
                            return;
                        } else {
                        	// This is if there exists something like factions group protection
                            if (event.isCancelled()) {
                                event.setCancelled(false);
                                if(ctp.getGlobalConfigOptions().debugMessages)
                                	ctp.getLogger().info("Just uncancelled a EntityDamageEvent because the event was cancelled by some other plugin.");
                            }
                        }
                    }
                }

                //Player has "died"
                if ((this.ctp.playerData.get(playa.getName()) != null) && (playa.getHealth() - event.getDamage() <= 0)) {
                    event.setCancelled(true);
                    if(ctp.getGlobalConfigOptions().debugMessages)
                    	ctp.getLogger().info("Just cancelled a EntityDamageEvent because the player 'died' therefore we are respawning it.");
                    respawnPlayer(playa, attacker);
                    
                    //Throw a custom event for when the player dies in the arena
                    CTPPlayerDeathEvent CTPevent = new CTPPlayerDeathEvent(playa, ctp.mainArena, ctp.playerData.get(playa.getName()));
                    ctp.getPluginManager().callEvent(CTPevent);
                }
            }
        }
        if (ctp.playerData.get(((Player) event.getEntity()).getName()) != null && ctp.playerData.get(((Player) event.getEntity()).getName()).inLobby()) {
            event.setCancelled(true);
            if(ctp.getGlobalConfigOptions().debugMessages)
            	ctp.getLogger().info("Just cancelled a EntityDamageEvent because the player is in the lobby.");
        }
    }
    
    private boolean checkForPlayerEvent(EntityDamageEvent event) {
        if (!(event instanceof EntityDamageByEntityEvent)) {
            return false;
        }
        // You now know the player getting damaged was damaged by another entity
        if (!(((EntityDamageByEntityEvent) event).getDamager() instanceof Player)) {
            return false;
        }
        // You now know the entity that is attacking is a player
        return true;
    }
    
	@SuppressWarnings("deprecation")
	private boolean dropWool(Player player) {
        if (!ctp.mainArena.getConfigOptions().dropWoolOnDeath) {
            return false;
        }

        PlayerInventory inv = player.getInventory();
        int ownedWool = 0;
        for (ItemStack item : inv.getContents()) {
            if (item != null && item.getTypeId() == 35) {
                if (!((Wool) item.getData()).getColor().toString().equalsIgnoreCase(ctp.playerData.get(player.getName()).getTeam().getColor())) {
                    inv.remove(35);
                    ItemStack tmp = new ItemStack(item.getType(), item.getAmount(), (short) ((Wool) item.getData()).getColor().getData());
                    player.getWorld().dropItem(player.getLocation(), tmp);
                } else {
                    ownedWool += item.getAmount();
                }
            }
        }
        inv.remove(Material.WOOL);
        
        if (ownedWool != 0) {
            DyeColor color = DyeColor.valueOf(ctp.playerData.get(player.getName()).getTeam().getColor().toUpperCase());
            ItemStack wool = new ItemStack(35, ownedWool, color.getData());
            player.getInventory().addItem(new ItemStack[]{wool});
            
    		//It's deprecated but it's currently the only way to get the desired effect.
    		player.updateInventory();
        }
        return true;
    }
    
	@SuppressWarnings("deprecation")
	public void giveRoleItemsAfterDeath(Player player) {
    	
        PlayerInventory inv = player.getInventory();
        
        //Get wool for return
        int ownedWool = 0;
        for (ItemStack item : inv.getContents()) {
            if (item != null && item.getTypeId() == 35) {
                if (!((Wool) item.getData()).getColor().toString().equalsIgnoreCase(ctp.playerData.get(player.getName()).getTeam().getColor())) {
                    inv.remove(35);
                    ItemStack tmp = new ItemStack(item.getType(), item.getAmount(), (short) ((Wool) item.getData()).getColor().getData());
                    player.getWorld().dropItem(player.getLocation(), tmp);
                } else {
                    ownedWool += item.getAmount();
                }
            }
        }
        
        inv.clear(); // Removes inventory
        
        for (Items item : ctp.roles.get(ctp.playerData.get(player.getName()).getRole())) {
            if(item.getItem().equals(Material.AIR))
                continue;

            if (inv.contains(item.getItem())) {
                if(item.getItem().getId() == 373) {   // Potions
                    ItemStack stack = new ItemStack(item.getItem());
                    stack.setAmount(item.getAmount());
                    stack.setDurability(item.getType());

                    HashMap<Integer, ? extends ItemStack> slots = inv.all(item.getItem());
                    int amount = 0;
                    for (int slotNum : slots.keySet()) {
                        if(slots.get(slotNum).getDurability() == item.getType())
                            amount += slots.get(slotNum).getAmount();
                    }

                    if (amount < item.getAmount()) {
                        //Removing old potions
                        for (int slotNum : slots.keySet()) {
                            if(slots.get(slotNum).getDurability() == item.getType())
                                inv.setItem(slotNum, null);
                        }

                        inv.addItem(stack);
                    }
                }
                else if (!Util.ARMORS_TYPE.contains(item.getItem())/* && (!Util.WEAPONS_TYPE.contains(item.getType()))*/) {
                    HashMap<Integer, ? extends ItemStack> slots = inv.all(item.getItem());
                    int amount = 0;
                    for (int slotNum : slots.keySet()) {
                        amount += slots.get(slotNum).getAmount();
                    }
                    
                    if (amount < item.getAmount()) {
                        inv.remove(item.getItem());

                        ItemStack stack = new ItemStack(item.getItem());
                        stack.setAmount(item.getAmount());
                        if(item.getType() != -1)
                            stack.setDurability(item.getType());
                        // Add enchantments
                        for(int j = 0; j < item.getEnchantments().size(); j++) {
                            stack.addEnchantment(item.getEnchantments().get(j), item.getEnchantmentLevels().get(j));
                        }
                        
                        inv.addItem(stack);
                    }
                }
            } else {
                if (!Util.ARMORS_TYPE.contains(item.getItem())) {
                    ItemStack stack = new ItemStack(item.getItem());
                    stack.setAmount(item.getAmount());
                    if(item.getType() != -1)
                        stack.setDurability(item.getType());
                    // Add enchantments
                    for(int j = 0; j < item.getEnchantments().size(); j++) {
                        stack.addEnchantment(item.getEnchantments().get(j), item.getEnchantmentLevels().get(j));
                    }
                    
                    inv.addItem(stack);
                } else {// find if there is something equipped
                    ItemStack stack = new ItemStack(item.getItem(), item.getAmount());

                    // Add enchantments
                    for(int j = 0; j < item.getEnchantments().size(); j++)
                        stack.addEnchantment(item.getEnchantments().get(j), item.getEnchantmentLevels().get(j));
                    
                    //If the armour slot is the role's thing, reset it to reset the durability but if it isn't (besides being null) then we just add this to their inventory.
                    //This way players keep their extra armor that they could have bought from the in game store.
                    if (Util.BOOTS_TYPE.contains(item.getItem())) {
                    	if(inv.getBoots() == null)
                    		inv.setBoots(stack);
                    	else if (inv.getBoots().getType() == item.getItem())
                            inv.setBoots(stack);
                        else
                            inv.addItem(stack);
                    } else if (Util.LEGGINGS_TYPE.contains(item.getItem())) {
                    	if (inv.getLeggings() == null)
                    		inv.setLeggings(stack);
                    	else if (inv.getLeggings().getType() == item.getItem())
                            inv.setLeggings(stack);
                        else
                            inv.addItem(stack);
                    } else if (Util.CHESTPLATES_TYPE.contains(item.getItem())) {
                    	if (inv.getChestplate() == null)
                    		inv.setChestplate(stack);
                    	else if (inv.getChestplate().getType() == item.getItem())
                            inv.setChestplate(stack);
                        else
                            inv.addItem(stack);
                    }
                }
            }
        }
        
        //Re-add Wool
        if (ownedWool != 0) {
            DyeColor color = DyeColor.valueOf(ctp.playerData.get(player.getName()).getTeam().getColor().toUpperCase());
            ItemStack wool = new ItemStack(35, ownedWool, color.getData());
            player.getInventory().addItem(new ItemStack[]{wool});
        }
        
		//It's deprecated but it's currently the only way to get the desired effect.
		player.updateInventory();
    }
    
    public boolean isProtected(Player player) {
        // Kj -- null checks
        if (ctp.mainArena == null || player == null) {
            return false;
        }
        if (ctp.playerData.get(player.getName()) == null) {
            return false;
        }

        Spawn spawn = new Spawn();

        try {
            spawn = ctp.playerData.get(player.getName()).getTeam().getSpawn();
        } catch(Exception e) { // For debugging
            System.out.println("[ERROR][CTP] Team spawn could not be found!  Player Name: " + player.getName());
            return false;
        }
                            
        Location protectionPoint = new Location(ctp.getServer().getWorld(ctp.mainArena.getWorld()), spawn.getX(), spawn.getY(), spawn.getZ());
        double distance = Util.getDistance(player.getLocation(), protectionPoint); // Kj -- this method is world-friendly.
        
        if (distance == Double.NaN) {
            return false; // Kj -- it will return Double.NaN if cross-world or couldn't work out distance for whatever reason.
        } else {
            return distance <= ctp.mainArena.getConfigOptions().protectionDistance;
        }
    }
    
    public void respawnPlayer(Player player, Player attacker) {
        if (attacker != null) {
            if(!ctp.getGlobalConfigOptions().disableKillMessages) {
                Util.sendMessageToPlayers(ctp, ctp.playerData.get(player.getName()).getTeam().getChatColor() + player.getName() + ChatColor.WHITE
                        + " was killed by " + ctp.playerData.get(attacker.getName()).getTeam().getChatColor() + attacker.getName());
            }
            
            dropWool(player);
            ctp.playerData.get(attacker.getName()).setMoney(ctp.playerData.get(attacker.getName()).getMoney() + ctp.mainArena.getConfigOptions().moneyForKill);
            attacker.sendMessage("Money: " + ChatColor.GREEN + ctp.playerData.get(attacker.getName()).getMoney());
            ctp.checkForKillMSG(attacker, false);
            ctp.checkForKillMSG(player, true);
        } else {
            if(!ctp.getGlobalConfigOptions().disableKillMessages)
                Util.sendMessageToPlayers(ctp, ctp.playerData.get(player.getName()).getTeam().getChatColor() + player.getName() + ChatColor.WHITE
                        + " was killed by " + ChatColor.LIGHT_PURPLE + "Herobrine");
            ctp.sendMessage(player, ChatColor.RED + "Please do not remove your Helmet.");
            ctp.checkForKillMSG(player, true);
        }

        PotionManagement.removeAllEffects(player);
        setFullHealthPlayerAndCallEvent(player);
        player.setFoodLevel(20);
        Spawn spawn = ctp.playerData.get(player.getName()).getTeam().getSpawn();

        if (ctp.mainArena.getConfigOptions().giveNewRoleItemsOnRespawn) {
            giveRoleItemsAfterDeath(player);
        }

        // Reseting player cooldowns
        for (HealingItems item : ctp.healingItems) {
            if (item != null && item.cooldowns != null && item.cooldowns.size() > 0 && item.resetCooldownOnDeath) {
                for (String playName : item.cooldowns.keySet()) {
                    if (playName.equalsIgnoreCase(player.getName())) {
                        item.cooldowns.remove(playName);
                    }
                }
            }
        }

        Location loc = new Location(ctp.getServer().getWorld(ctp.mainArena.getWorld()), spawn.getX(), spawn.getY(), spawn.getZ());
        loc.setYaw((float) spawn.getDir());
        ctp.getServer().getWorld(ctp.mainArena.getWorld()).loadChunk(loc.getBlockX(), loc.getBlockZ());
        boolean teleport = player.teleport(loc);
        
        if (!teleport) {
            player.teleport(new Location(player.getWorld(), spawn.getX(), spawn.getY(), spawn.getZ(), 0.0F, (float)spawn.getDir()));
        }
        player.setFireTicks(0);
        player.getActivePotionEffects().clear();
    }
    
    /**
     * Heal the player (set the health) and cause an event to happen from it, thus improving relations with other plugins.
     * 
     * @param player The player to heal.
     * @param amount The amount to heal the player.
     */
    public void setFullHealthPlayerAndCallEvent(Player player) {
    	int gained = ctp.mainArena.getConfigOptions().maxPlayerHealth - player.getHealth();
    	
    	player.setHealth(ctp.mainArena.getConfigOptions().maxPlayerHealth);
    	
    	EntityRegainHealthEvent regen = new EntityRegainHealthEvent(player, gained, RegainReason.CUSTOM);
    	ctp.getPluginManager().callEvent(regen);
    }
}
