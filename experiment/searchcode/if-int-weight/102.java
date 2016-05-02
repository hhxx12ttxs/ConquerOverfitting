package net.firesquared.hardcorenomad.helpers.enums;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.firesquared.hardcorenomad.CreativeTab;
import net.firesquared.hardcorenomad.helpers.Helper;
import net.firesquared.hardcorenomad.item.ItemUpgrade;
import net.firesquared.hardcorenomad.item.backpacks.*;
import net.firesquared.hardcorenomad.item.healing.ItemHealingHerb;
import net.firesquared.hardcorenomad.item.misc.ItemPebble;
import net.firesquared.hardcorenomad.item.misc.ItemSlingShot;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.client.IItemRenderer;

public enum Items
{
	// TODO: Add Items
	// Example ITEM_NAME("item.name", new ItemClass())
	ITEM_BACKPACK("item.backpack", new ItemBackPack(), CreativeTab.HardCoreNomadTab),
	//ITEM_FIREBOW("item.firebow", new ItemFireBow(0.0F, null, null)),
	//ITEM_FIREBUNDLE("campfire.bundle", new ItemFireBundle(), CreativeTab.HardCoreNomadTab),
	//ITEM_HEALINGFIRSTAID("healing.firstaid", new ItemHealingFirstAid(), CreativeTab.HardCoreNomadTab),
	ITEM_HEALINGHERB("healing.herb", new ItemHealingHerb(2, 1.0F, false), CreativeTab.HardCoreNomadTab, 1, 5, 4),
	//ITEM_HEALINGMAGICALAID("healing.magicalaid", new ItemHealingMagicalAid(), CreativeTab.HardCoreNomadTab),
	ITEM_FLINTSHEARS("tools.flintshears", new ItemShears().setMaxDamage(42).setTextureName("hardcorenomad:flintshears"), CreativeTabs.tabTools),

	ITEM_MISC_PEBBLE("misc.pebble", new ItemPebble(), CreativeTab.HardCoreNomadTab, 1, 23, 5),
	ITEM_MISC_SLINGSHOT("misc.slingshot", new ItemSlingShot(), CreativeTab.HardCoreNomadTab),

	//Upgrade Items
	ITEM_UPGRADE("upgrade", new ItemUpgrade(), CreativeTab.HardCoreNomadUpgradesTab)

	;

	private final String internalName;
	private final boolean isDungeonLoot;
	private final Item item;
	private int dungeonChestMin;
	private int dungeonChestMax;
	private int weight;

	Items(String internalName, Item item, CreativeTabs creativeTabs)
	{
		this(internalName, item, creativeTabs, -1, -1, -1);
	}

	Items(String internalName, Item item, CreativeTabs creativeTabs, int dungeonChestMin, int dungeonChestMax, int weight) 
	{
		this.internalName = internalName;
		this.item = item;
		item.setUnlocalizedName(Helper.Strings.MOD_ID + "." + internalName);
		item.setTextureName(Helper.Strings.MOD_ID + ":textures.item."+internalName);
		item.setCreativeTab(creativeTabs);
		if(dungeonChestMax < 0 || dungeonChestMin < 0 || weight < 0)
			isDungeonLoot = false;
		else
		{
			this.dungeonChestMax = dungeonChestMax;
			this.dungeonChestMin = dungeonChestMin;
			this.weight = weight;
			isDungeonLoot = true;
		}
	}
	
	private void register()
	{
		Helper.getLogger().debug("Registering Item: " + internalName);
		Item itemObject = item;
		itemObject.setTextureName(Helper.Strings.MOD_ID + ":" + itemObject.getUnlocalizedName());
		GameRegistry.registerItem(itemObject, internalName);
	}

	public String getInternalName()
	{
		return internalName;
	}

	public Item getItem()
	{
		return item;
	}

	public String getStatName()
	{
		return StatCollector.translateToLocal(item.getUnlocalizedName());
	}
	
	public ItemStack getStack(int damage, int size)
	{
		return new ItemStack(item, size, damage);
	}
	
	public boolean isDungeonLoot()
	{
		return isDungeonLoot;
	}

	public int getDungeonChestMin() {
		return isDungeonLoot ? this.dungeonChestMin : -1;
	}

	public int getDungeonChestMax() {
		return isDungeonLoot ? this.dungeonChestMax : -1;
	}

	public int getWeight() {
		return isDungeonLoot ? this.weight : -1;
	}

	public static void registerAll()
	{
		for(Items i : Items.values())
			i.register();
	}
}

