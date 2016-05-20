package me.sorroko.LevelsPlus.listener;

import me.sorroko.LevelsPlus.LevelsPlus;
import me.sorroko.LevelsPlus.Util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class PlayerListener implements Listener {

	LevelsPlus plugin;
	public PlayerListener(LevelsPlus instance) {
		plugin = instance;
	}
	
	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event){
		Player player = (Player) event.getEntity(); //get the player
		
		if(plugin.mainConf.getBoolean("level_based_hunger", true) && plugin.hasPermission(player, "level_hunger")){
			int lvl = player.getLevel(); //get the players current exp level
			
			int realHunger = player.getMaxHealth(); //get the players max hunger (20)

			double cModifier = plugin.mainConf.getDouble("hunger_modifier", 1);
			
			//int health = realHunger + (lvl * 2); //calculate the fictional hunger depending on level
			Double hungerDouble = realHunger + ((Math.sqrt(lvl) * 1.04) * cModifier); //alternative to the above (note: do not use primitive "double")
			int hunger = hungerDouble.intValue(); //convert double to int for use below

			int cap = plugin.mainConf.getInt("hunger_level_cap", 40);
			
			if(hunger > cap) hunger = cap;
			int calcFood = (hunger / realHunger) * event.getFoodLevel();
			
			event.setFoodLevel(calcFood);
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event){
		if(event.getEntity() instanceof Player){ //check if instance of a player
			Player player = (Player) event.getEntity(); //get the player
			
			if(plugin.mainConf.getBoolean("level_based_health", true) && plugin.hasPermission(player, "level_health")){
				int dmg = event.getDamage(); //get the event damage based on the normal dmg/20 value
				int lvl = player.getLevel(); //get the players current exp level
			
				int realHealth = player.getMaxHealth(); //get the players max health (20)

				double cModifier = plugin.mainConf.getDouble("health_modifier", 1);

				//int health = realHealth + (lvl * 2); //calculate the fictional health depending on level
				Double healthDouble = realHealth + (Math.sqrt(lvl) * 1.04 * cModifier); //alternative to the above (note: do not use primitive "double")
				int health = healthDouble.intValue(); //convert double to int for use below

				/*
				 * TODO
				 * Add config option for modifier
				 */
				int cap = plugin.mainConf.getInt("health_level_cap", 40);

				if(health > cap) health = cap;
				int calcDmg = (realHealth / health) * dmg; //simple ratio operation to convert the original dmg to the new health
				if(calcDmg == 0) calcDmg = 1;
				Util.debug("Player health: OrigDamage: " + dmg + " MaxHealth: " + health + " Damage: " + calcDmg);
				
				if(event.getCause().equals(DamageCause.CONTACT) || event.getCause().equals(DamageCause.MAGIC)){
					double rand = Math.random();
					if(rand * lvl > (lvl * 0.9) * 0.9){
						player.sendMessage(Util.formatMessage(ChatColor.DARK_RED + "Dodge!"));
						event.setCancelled(true);
					} else
						event.setDamage(calcDmg);
				} else {
					event.setDamage(calcDmg);
				}
			}
			
//			PlayerInventory inv = player.getInventory();
//			
//			ItemStack helmet = inv.getArmorContents()[0];
//			ItemStack chestplate = inv.getArmorContents()[1];
//			ItemStack pants = inv.getArmorContents()[2];
//			ItemStack boots = inv.getArmorContents()[3];
//			
//			int maxHelmet = helmet.getType().getMaxDurability();
//			int maxChestplate = chestplate.getType().getMaxDurability();
//			int maxPants = pants.getType().getMaxDurability();
//			int maxBoots = boots.getType().getMaxDurability();
//			
//			int curHelmet = helmet.getDurability();
//			int curChestplate = chestplate.getDurability();
//			int curPants = pants.getDurability();
//			int curBoots = boots.getDurability();
//			
//			int curOverall = curHelmet + curChestplate + curPants + curBoots;
//			int maxOverall = maxHelmet + maxChestplate + maxPants + maxBoots;
//			
//			int durPoint = curOverall / maxOverall;
//			int split = curOverall / 4;
//			
//			helmet.setDurability((short) (split));
//			chestplate.setDurability((short) (split));
//			pants.setDurability((short) (split));
//			boots.setDurability((short) (split));
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event){
		if(event.getDamager() instanceof Player){
			Player damager = (Player) event.getDamager();
			if(plugin.mainConf.getBoolean("level_based_damage", true) && plugin.hasPermission(damager, "level_damage")){
				int start_dmg = event.getDamage();
				int lvl = damager.getLevel();
				if(lvl == 0) lvl = 1;
				
				int modifier = lvl / 2;
				if(modifier > 15) modifier = 15;
				
				modifier = (modifier / 10) + 1;

				double cModifier = plugin.mainConf.getDouble("damage_modifier", 1);
			
				double dmg = (start_dmg * modifier) * cModifier;
			
				event.setDamage((int) dmg);
				Util.debug("Player attack: Modifier: " + modifier + " Damage: " + dmg);
			}
		}
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event){
		Player player = event.getEntity();
		if(plugin.mainConf.getBoolean("death.keep_levels", true) && plugin.hasPermission(player, "death_keep_levels")){
			event.setNewExp(0);
			event.setDroppedExp(0);
			if(player.getLevel() - 2 < 0)
				event.setNewLevel(0);
			else
				event.setNewLevel(player.getLevel() - 2);
		}
		if(plugin.mainConf.getBoolean("death.lose_items", true) && !plugin.hasPermission(player, "death_drop_items")){
			event.getDrops().clear();
		}
	}
	
	@EventHandler
	public void onEnchantItem(EnchantItemEvent event){
		Player player = event.getEnchanter();
		
		if(plugin.mainConf.getBoolean("enchanting.no_levels", true) && plugin.hasPermission(player, "enchant_no_levels"))
			event.setExpLevelCost(0);
		
		if(plugin.mainConf.getBoolean("enchanting.use_items", true) && !plugin.hasPermission(player, "enchant_require_no_items")){
			ItemStack gold = new ItemStack(Material.GOLD_INGOT, 2);
			ItemStack eyeOfEnder = new ItemStack(Material.EYE_OF_ENDER, 1);
			ItemStack blazePowder = new ItemStack(Material.BLAZE_POWDER, 1);
			
			PlayerInventory inv = player.getInventory();
			if(inv.contains(gold) && inv.contains(eyeOfEnder) && inv.contains(blazePowder)){
				inv.removeItem(gold);
				inv.removeItem(eyeOfEnder);
				inv.removeItem(blazePowder);
			} else {
				event.setCancelled(true);
				player.sendMessage(Util.formatMessage(ChatColor.RED + "You do not have enough items."));
				player.sendMessage(Util.formatMessage(ChatColor.RED + "2 Gold Ingots, 1 Eye Of Ender and 1 Blaze Powder are required!."));
			}
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event){
		if(event.isCancelled()) return;
		Player player = event.getPlayer();
		
		if(plugin.mainConf.getBoolean("mining.double_ore_drops", true) && plugin.hasPermission(player, "mining_double_drops")){
			Block block = event.getBlock();
			Material blockType = event.getBlock().getType();
			if(blockType.equals(Material.COAL_ORE) 
					|| blockType.equals(Material.IRON_ORE) 
					|| blockType.equals(Material.GOLD_ORE) 
					|| blockType.equals(Material.DIAMOND_ORE)){
				int lvl = player.getLevel();
				
				double mining_modifier = plugin.mainConf.getDouble("mining.double_drop_random", 0.9);
				if(mining_modifier > 1){
					mining_modifier = 0.9;
					Util.log(Util.formatBroadcast("Invalid config option: double_drop_random value bigger than 1, using default"));
				} else if(mining_modifier <= 0){
					mining_modifier = 0.1;
					if(mining_modifier == 0)
						Util.log(Util.formatBroadcast("Invalid config option: double_drop_random value cannot be 0, using default"));
					else
						Util.log(Util.formatBroadcast("Invalid config option: double_drop_random value less than 0.1, using default"));
				}
				double rand = Math.random();
				if(rand * lvl > lvl * mining_modifier){
					if(blockType.equals(Material.COAL_ORE)){
						block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.COAL, 1)); //And the normal drop
					} else if(blockType.equals(Material.IRON_ORE)){
						block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.IRON_ORE, 1)); //And the normal drop
					} else if(blockType.equals(Material.GOLD_ORE)){
						block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.GOLD_ORE, 1)); //And the normal drop
					} else if(blockType.equals(Material.DIAMOND_ORE)){
						block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.DIAMOND, 1)); //And the normal drop
					}
				}
			}
		}
	}
	
//	@EventHandler
//	public void onBlockHit(BlockDamageEvent event){
//		if(event.isCancelled()) return;
//		int required = 0;
//		switch(event.getPlayer().getItemInHand().getType()){
//		case WOOD_PICKAXE:
//		case WOOD_SPADE:
//		case WOOD_AXE:
//		case WOOD_SWORD:
//		case WOOD_HOE:
//			required = 0;
//			break;
//		case STONE_PICKAXE:
//		case STONE_SPADE:
//		case STONE_AXE:
//		case STONE_SWORD:
//		case STONE_HOE:
//			required = 5;
//			break;
//		case IRON_PICKAXE:
//		case IRON_SPADE:
//		case IRON_AXE:
//		case IRON_SWORD:
//		case IRON_HOE:
//			required = 15;
//			break;
//		case GOLD_PICKAXE:
//		case GOLD_SPADE:
//		case GOLD_AXE:
//		case GOLD_SWORD:
//		case GOLD_HOE:
//			required = 20;
//			break;
//		case DIAMOND_PICKAXE:
//		case DIAMOND_SPADE:
//		case DIAMOND_AXE:
//		case DIAMOND_SWORD:
//		case DIAMOND_HOE:
//			required = 30;
//			break;
//		}
//		
//		if(required != 0 && event.getPlayer().getLevel() < required){
//			event.setCancelled(true);
//			event.getPlayer().sendMessage(Util.formatMessage("You need a higher level to use that tool!"));
//		}
//	}
}

