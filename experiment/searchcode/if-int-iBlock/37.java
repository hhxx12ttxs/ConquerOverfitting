package farrael.fr.battleground.utils;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import farrael.fr.battleground.Battleground;
import farrael.fr.battleground.classes.Job;
import farrael.fr.battleground.classes.Team;
import farrael.fr.battleground.managers.FileManager.FileType;

public class InventoryUtil {

	//Reflection
	private static ItemUtil itemUtil	= Battleground.itemUtil;

	@SuppressWarnings("deprecation")
	public static ItemStack getItemStack(Player player, String value){
		String data   = null;
		if (value.contains(":")){
			String[] calleds = value.split(":");
			value 	= calleds[0].trim();
			data 	= calleds[1].trim();
		}

		Integer iblock;
		try {
			iblock = Integer.parseInt(value);
		} catch (Exception e) {
			try {
				iblock = Material.getMaterial(value.trim().replace(" ", "_").toUpperCase()).getId();
			} catch (Exception e2) {
				player.sendMessage(Battleground.CAlert + "Impossible de parser l'ID " + Battleground.CName + value + Battleground.CMessage + " !");
				return null;
			}
		}
		
		ItemStack toInv = null;
		if (data != null) {
			if (Material.getMaterial(iblock) == null) {
				player.sendMessage(Battleground.CAlert + "Le block " + Battleground.CName + iblock + Battleground.CMessage +" n'existe pas !");
				return null;
			}
			int data2;
			try {
				data2 = Integer.parseInt(data);
			} catch (Exception e) {
				player.sendMessage(Battleground.CAlert + "Le metadata est invalid !");
				return null;
			}
			if (data2 < 0) {
				player.sendMessage(Battleground.CAlert + "Le metadata est invalid !");
				return null;
			}

			toInv = new ItemStack(Material.getMaterial(iblock).getId(), 1, (short) data2);
		} else {
			toInv = new ItemStack(Material.getMaterial(iblock).getId(), 1);
		}

		return toInv;
	}

	/**
	 * Return if team have inventory of armor
	 * @param player - Player to send error message
	 * @param team - Team who need to verify
	 */
	public static boolean haveEquipment(Player player, Team team){
		if(team.getArmor() == null && team.getInv() == null){ 
			player.sendMessage(ChatUtil.item_team);
			return false;
		}
		return true;
	}
	/**
	 * Create armor inventory based on Player.
	 * @param p - Player who need to take Armor.
	 */
	public static Inventory setArmor(Player p, Boolean unbreak){
		Inventory storage = itemUtil.getArmorInventory(p.getInventory(), unbreak);
		return storage;
	}

	/**
	 * Create inventory based on Player.
	 * @param p - Player who need to take Inventory.
	 */
	public static Inventory setInv(Player p, Boolean unbreak){
		Inventory storage = itemUtil.getContentInventory(p.getInventory(), unbreak);
		return storage;
	}

	/**
	 * Create inventory based on file.
	 * @param Obj - Name of Team/Class.
	 * @param Type - Inventory Type (Team./Class.).
	 */
	public static Inventory LoadI(String Obj, FileType type, boolean unbreakable){
		String data = (String) Battleground.fileManager.getData(type, Obj + ".Item", null);
		if(data != null && data.length() > 1){
			Inventory inv = itemUtil.fromBase64(data, unbreakable);
			return inv;
		}
		return null;
	}
	
	/**
	 * Create armor inventory based on file.
	 * @param Obj - Name of Team/Class.
	 * @param Type - Inventory Type (Team./Class.).
	 */
	public static Inventory LoadA(String Obj, FileType type, boolean unbreakable){
		String data = (String) Battleground.fileManager.getData(type, Obj + ".Armor", null);
		if(data != null && data.length() > 1){
			Inventory inv = itemUtil.fromBase64(data, unbreakable);
			return inv;
		}
		return null;
	}

	/**
	 * Save inventory based on player.
	 * @param Obj - Name of Team/Class.
	 * @param player - Player who need to take inventory.
	 * @param Type - Type of inventory (Team/Class)
	 */
	public static void SaveI(String Obj, Player player, FileType type){

		Inventory bag	= setInv(player, true);
		Inventory armor = setArmor(player, true);

		if(type == FileType.TEAM){
			Battleground.battleManager.getTeam(Obj).setInv(bag);
			Battleground.battleManager.getTeam(Obj).setArmor(armor);
		} else if(type == FileType.CLASS){
			Battleground.battleManager.getJob(Obj).setInv(bag);
			Battleground.battleManager.getJob(Obj).setArmor(armor);
		} else {
			return;
		}

		String data = itemUtil.toBase64(bag);
		String data2 = itemUtil.toBase64(armor);
		Battleground.fileManager.setData(type, Obj + ".Item", data);
		Battleground.fileManager.setData(type, Obj + ".Armor", data2);
		Battleground.fileManager.saveFile(type);
	}

	/**
	 * Load player inventory.
	 * @param player - Player who need to change inventory.
	 * @param apply - Apply change to player.
	 */
	public static void LoadInv(UUID player, boolean apply){
		String data   = (String) Battleground.fileManager.getData(FileType.SAVE, player + ".Item", null);
		String data2  = (String) Battleground.fileManager.getData(FileType.SAVE, player + ".Armor", null);
		if(data != null && data.length() > 1){
			Inventory inv = itemUtil.fromBase64(data, false);
			if(!apply){
				if(Battleground.battleManager.isMember(player)) Battleground.battleManager.getMember(player).setInv(inv);
			} else {
				Player p = Bukkit.getServer().getPlayer(player);
				if(p != null){
					p.getInventory().setContents(inv.getContents());
				} else {
					if(Battleground.battleManager.isMember(player)) Battleground.battleManager.getMember(player).setInv(inv);
				}
			}
		}
		if(data2 != null && data2.length() > 1){
			Inventory inv2 = itemUtil.fromBase64(data2, false);
			if(!apply){
				if(Battleground.battleManager.isMember(player)) Battleground.battleManager.getMember(player).setArmor(inv2);
			} else {
				Player p = Bukkit.getServer().getPlayer(player);
				if(p != null){
					p.getInventory().setArmorContents(inv2.getContents());
				} else {
					if(Battleground.battleManager.isMember(player)) Battleground.battleManager.getMember(player).setArmor(inv2);
				}
			}
		}
		return;
	}

	/**
	 * Save player inventory.
	 * @param player - Player who need to save inventory.
	 */
	public static void SaveInv(Player player){
		String data = ItemUtil.instance.toBase64(player.getInventory());
		String data2= itemUtil.toBase64(setArmor(player, false));
		Battleground.fileManager.setDataWithoutSave(FileType.SAVE, player.getUniqueId() + ".Item", data);
		Battleground.fileManager.setDataWithoutSave(FileType.SAVE, player.getUniqueId() + ".Armor", data2);
		Battleground.fileManager.saveFile(FileType.SAVE);
	}

	/**
	 * Clear player inventory
	 * @param player - Player who need to clear inventory.
	 */
	public static void clearinv(Player player){
		PlayerInventory playerInv = player.getInventory();
		playerInv.clear();
		playerInv.setHelmet(null);
		playerInv.setChestplate(null);
		playerInv.setLeggings(null);
		playerInv.setBoots(null);
	}

	/**
	 * Set player inventory based on file.
	 * @param player - Player who need to change inventory.
	 * @param Obj - Name of Team/Class.
	 * @param Type - Type of inventory (Team./Class.).
	 */
	public static void SetItem(Player player, String Obj, String Type){	
		Type = Type.replace(".", "");

		if(Type.equalsIgnoreCase("Class")){
			Job job = Battleground.battleManager.getJob(Obj);
			if(job.asInv()) player.getInventory().setContents(job.getInv().getContents());
			if(job.asArmor()) player.getInventory().setArmorContents(job.getArmor().getContents());
		} else {
			Team team = Battleground.battleManager.getTeam(Obj);
			if(team.asInv()) player.getInventory().setContents(team.getInv().getContents());
			if(team.asArmor()) player.getInventory().setArmorContents(team.getArmor().getContents());
		}

		if(!Battleground.battleManager.isMember(player) || !Battleground.battleManager.getMember(player).inTeam()) return;

		Team team = Battleground.battleManager.getMember(player).getTeam();
		if(team.asFlag()){
			ItemStack Flag = team.getFlag();
			player.getInventory().setHelmet(Flag);
		}
	}

	/**
	 * Return if item is Tool or Armor
	 * @param item
	 */
	public static boolean isEquipment(ItemStack item){
		return item.getType().getMaxDurability() >= 30;
	}

	/**
	 * Set item tag to Unbreakable.
	 * @param item - Item to set Unbreakable.
	 */
	public static ItemStack setUnbreakable(ItemStack item){
		if(item == null || !isEquipment(item)) return item;
		itemUtil.setBoolean(item, "Unbreakable", true);
		return item;
	}
}

