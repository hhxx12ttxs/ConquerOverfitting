package net.minecraft.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import net.canarymod.api.inventory.CanaryBaseItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockNewLog;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockQuartz;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockSandStone;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.block.BlockStoneBrick;
import net.minecraft.block.BlockWall;
import net.minecraft.block.BlockWood;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.RegistryNamespaced;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionHelper;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;

public class Item {

    public static final RegistryNamespaced e = new RegistryNamespaced();
    protected static final UUID f = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    private CreativeTabs a;
    protected static Random g = new Random();
    protected int h = 64;
    private int b;
    protected boolean i;
    protected boolean j;
    private Item c;
    private String d;
    private String m;
    protected String l;

    private final CanaryBaseItem base; // CanaryMod

    public static int b(Item item) {
        return item == null ? 0 : e.b((Object) item);
    }

    public static Item d(int i0) {
        return (Item) e.a(i0);
    }

    public static Item a(Block block) {
        return d(Block.b(block));
    }

    public static void l() {
        e.a(256, "iron_shovel", (new ItemSpade(ToolMaterial.IRON)).c("shovelIron").f("iron_shovel"));
        e.a(257, "iron_pickaxe", (new ItemPickaxe(ToolMaterial.IRON)).c("pickaxeIron").f("iron_pickaxe"));
        e.a(258, "iron_axe", (new ItemAxe(ToolMaterial.IRON)).c("hatchetIron").f("iron_axe"));
        e.a(259, "flint_and_steel", (new ItemFlintAndSteel()).c("flintAndSteel").f("flint_and_steel"));
        e.a(260, "apple", (new ItemFood(4, 0.3F, false)).c("apple").f("apple"));
        e.a(261, "bow", (new ItemBow()).c("bow").f("bow"));
        e.a(262, "arrow", (new Item()).c("arrow").a(CreativeTabs.j).f("arrow"));
        e.a(263, "coal", (new ItemCoal()).c("coal").f("coal"));
        e.a(264, "diamond", (new Item()).c("diamond").a(CreativeTabs.l).f("diamond"));
        e.a(265, "iron_ingot", (new Item()).c("ingotIron").a(CreativeTabs.l).f("iron_ingot"));
        e.a(266, "gold_ingot", (new Item()).c("ingotGold").a(CreativeTabs.l).f("gold_ingot"));
        e.a(267, "iron_sword", (new ItemSword(ToolMaterial.IRON)).c("swordIron").f("iron_sword"));
        e.a(268, "wooden_sword", (new ItemSword(ToolMaterial.WOOD)).c("swordWood").f("wood_sword"));
        e.a(269, "wooden_shovel", (new ItemSpade(ToolMaterial.WOOD)).c("shovelWood").f("wood_shovel"));
        e.a(270, "wooden_pickaxe", (new ItemPickaxe(ToolMaterial.WOOD)).c("pickaxeWood").f("wood_pickaxe"));
        e.a(271, "wooden_axe", (new ItemAxe(ToolMaterial.WOOD)).c("hatchetWood").f("wood_axe"));
        e.a(272, "stone_sword", (new ItemSword(ToolMaterial.STONE)).c("swordStone").f("stone_sword"));
        e.a(273, "stone_shovel", (new ItemSpade(ToolMaterial.STONE)).c("shovelStone").f("stone_shovel"));
        e.a(274, "stone_pickaxe", (new ItemPickaxe(ToolMaterial.STONE)).c("pickaxeStone").f("stone_pickaxe"));
        e.a(275, "stone_axe", (new ItemAxe(ToolMaterial.STONE)).c("hatchetStone").f("stone_axe"));
        e.a(276, "diamond_sword", (new ItemSword(ToolMaterial.EMERALD)).c("swordDiamond").f("diamond_sword"));
        e.a(277, "diamond_shovel", (new ItemSpade(ToolMaterial.EMERALD)).c("shovelDiamond").f("diamond_shovel"));
        e.a(278, "diamond_pickaxe", (new ItemPickaxe(ToolMaterial.EMERALD)).c("pickaxeDiamond").f("diamond_pickaxe"));
        e.a(279, "diamond_axe", (new ItemAxe(ToolMaterial.EMERALD)).c("hatchetDiamond").f("diamond_axe"));
        e.a(280, "stick", (new Item()).q().c("stick").a(CreativeTabs.l).f("stick"));
        e.a(281, "bowl", (new Item()).c("bowl").a(CreativeTabs.l).f("bowl"));
        e.a(282, "mushroom_stew", (new ItemSoup(6)).c("mushroomStew").f("mushroom_stew"));
        e.a(283, "golden_sword", (new ItemSword(ToolMaterial.GOLD)).c("swordGold").f("gold_sword"));
        e.a(284, "golden_shovel", (new ItemSpade(ToolMaterial.GOLD)).c("shovelGold").f("gold_shovel"));
        e.a(285, "golden_pickaxe", (new ItemPickaxe(ToolMaterial.GOLD)).c("pickaxeGold").f("gold_pickaxe"));
        e.a(286, "golden_axe", (new ItemAxe(ToolMaterial.GOLD)).c("hatchetGold").f("gold_axe"));
        e.a(287, "string", (new ItemReed(Blocks.bD)).c("string").a(CreativeTabs.l).f("string"));
        e.a(288, "feather", (new Item()).c("feather").a(CreativeTabs.l).f("feather"));
        e.a(289, "gunpowder", (new Item()).c("sulphur").e(PotionHelper.k).a(CreativeTabs.l).f("gunpowder"));
        e.a(290, "wooden_hoe", (new ItemHoe(ToolMaterial.WOOD)).c("hoeWood").f("wood_hoe"));
        e.a(291, "stone_hoe", (new ItemHoe(ToolMaterial.STONE)).c("hoeStone").f("stone_hoe"));
        e.a(292, "iron_hoe", (new ItemHoe(ToolMaterial.IRON)).c("hoeIron").f("iron_hoe"));
        e.a(293, "diamond_hoe", (new ItemHoe(ToolMaterial.EMERALD)).c("hoeDiamond").f("diamond_hoe"));
        e.a(294, "golden_hoe", (new ItemHoe(ToolMaterial.GOLD)).c("hoeGold").f("gold_hoe"));
        e.a(295, "wheat_seeds", (new ItemSeeds(Blocks.aj, Blocks.ak)).c("seeds").f("seeds_wheat"));
        e.a(296, "wheat", (new Item()).c("wheat").a(CreativeTabs.l).f("wheat"));
        e.a(297, "bread", (new ItemFood(5, 0.6F, false)).c("bread").f("bread"));
        e.a(298, "leather_helmet", (new ItemArmor(ItemArmor.ArmorMaterial.CLOTH, 0, 0)).c("helmetCloth").f("leather_helmet"));
        e.a(299, "leather_chestplate", (new ItemArmor(ItemArmor.ArmorMaterial.CLOTH, 0, 1)).c("chestplateCloth").f("leather_chestplate"));
        e.a(300, "leather_leggings", (new ItemArmor(ItemArmor.ArmorMaterial.CLOTH, 0, 2)).c("leggingsCloth").f("leather_leggings"));
        e.a(301, "leather_boots", (new ItemArmor(ItemArmor.ArmorMaterial.CLOTH, 0, 3)).c("bootsCloth").f("leather_boots"));
        e.a(302, "chainmail_helmet", (new ItemArmor(ItemArmor.ArmorMaterial.CHAIN, 1, 0)).c("helmetChain").f("chainmail_helmet"));
        e.a(303, "chainmail_chestplate", (new ItemArmor(ItemArmor.ArmorMaterial.CHAIN, 1, 1)).c("chestplateChain").f("chainmail_chestplate"));
        e.a(304, "chainmail_leggings", (new ItemArmor(ItemArmor.ArmorMaterial.CHAIN, 1, 2)).c("leggingsChain").f("chainmail_leggings"));
        e.a(305, "chainmail_boots", (new ItemArmor(ItemArmor.ArmorMaterial.CHAIN, 1, 3)).c("bootsChain").f("chainmail_boots"));
        e.a(306, "iron_helmet", (new ItemArmor(ItemArmor.ArmorMaterial.IRON, 2, 0)).c("helmetIron").f("iron_helmet"));
        e.a(307, "iron_chestplate", (new ItemArmor(ItemArmor.ArmorMaterial.IRON, 2, 1)).c("chestplateIron").f("iron_chestplate"));
        e.a(308, "iron_leggings", (new ItemArmor(ItemArmor.ArmorMaterial.IRON, 2, 2)).c("leggingsIron").f("iron_leggings"));
        e.a(309, "iron_boots", (new ItemArmor(ItemArmor.ArmorMaterial.IRON, 2, 3)).c("bootsIron").f("iron_boots"));
        e.a(310, "diamond_helmet", (new ItemArmor(ItemArmor.ArmorMaterial.DIAMOND, 3, 0)).c("helmetDiamond").f("diamond_helmet"));
        e.a(311, "diamond_chestplate", (new ItemArmor(ItemArmor.ArmorMaterial.DIAMOND, 3, 1)).c("chestplateDiamond").f("diamond_chestplate"));
        e.a(312, "diamond_leggings", (new ItemArmor(ItemArmor.ArmorMaterial.DIAMOND, 3, 2)).c("leggingsDiamond").f("diamond_leggings"));
        e.a(313, "diamond_boots", (new ItemArmor(ItemArmor.ArmorMaterial.DIAMOND, 3, 3)).c("bootsDiamond").f("diamond_boots"));
        e.a(314, "golden_helmet", (new ItemArmor(ItemArmor.ArmorMaterial.GOLD, 4, 0)).c("helmetGold").f("gold_helmet"));
        e.a(315, "golden_chestplate", (new ItemArmor(ItemArmor.ArmorMaterial.GOLD, 4, 1)).c("chestplateGold").f("gold_chestplate"));
        e.a(316, "golden_leggings", (new ItemArmor(ItemArmor.ArmorMaterial.GOLD, 4, 2)).c("leggingsGold").f("gold_leggings"));
        e.a(317, "golden_boots", (new ItemArmor(ItemArmor.ArmorMaterial.GOLD, 4, 3)).c("bootsGold").f("gold_boots"));
        e.a(318, "flint", (new Item()).c("flint").a(CreativeTabs.l).f("flint"));
        e.a(319, "porkchop", (new ItemFood(3, 0.3F, true)).c("porkchopRaw").f("porkchop_raw"));
        e.a(320, "cooked_porkchop", (new ItemFood(8, 0.8F, true)).c("porkchopCooked").f("porkchop_cooked"));
        e.a(321, "painting", (new ItemHangingEntity(EntityPainting.class)).c("painting").f("painting"));
        e.a(322, "golden_apple", (new ItemAppleGold(4, 1.2F, false)).j().a(Potion.l.H, 5, 1, 1.0F).c("appleGold").f("apple_golden"));
        e.a(323, "sign", (new ItemSign()).c("sign").f("sign"));
        e.a(324, "wooden_door", (new ItemDoor(Material.d)).c("doorWood").f("door_wood"));
        Item item = (new ItemBucket(Blocks.a)).c("bucket").e(16).f("bucket_empty");

        e.a(325, "bucket", item);
        e.a(326, "water_bucket", (new ItemBucket(Blocks.i)).c("bucketWater").c(item).f("bucket_water"));
        e.a(327, "lava_bucket", (new ItemBucket(Blocks.k)).c("bucketLava").c(item).f("bucket_lava"));
        e.a(328, "minecart", (new ItemMinecart(0)).c("minecart").f("minecart_normal"));
        e.a(329, "saddle", (new ItemSaddle()).c("saddle").f("saddle"));
        e.a(330, "iron_door", (new ItemDoor(Material.f)).c("doorIron").f("door_iron"));
        e.a(331, "redstone", (new ItemRedstone()).c("redstone").e(PotionHelper.i).f("redstone_dust"));
        e.a(332, "snowball", (new ItemSnowball()).c("snowball").f("snowball"));
        e.a(333, "boat", (new ItemBoat()).c("boat").f("boat"));
        e.a(334, "leather", (new Item()).c("leather").a(CreativeTabs.l).f("leather"));
        e.a(335, "milk_bucket", (new ItemBucketMilk()).c("milk").c(item).f("bucket_milk"));
        e.a(336, "brick", (new Item()).c("brick").a(CreativeTabs.l).f("brick"));
        e.a(337, "clay_ball", (new Item()).c("clay").a(CreativeTabs.l).f("clay_ball"));
        e.a(338, "reeds", (new ItemReed(Blocks.aH)).c("reeds").a(CreativeTabs.l).f("reeds"));
        e.a(339, "paper", (new Item()).c("paper").a(CreativeTabs.f).f("paper"));
        e.a(340, "book", (new ItemBook()).c("book").a(CreativeTabs.f).f("book_normal"));
        e.a(341, "slime_ball", (new Item()).c("slimeball").a(CreativeTabs.f).f("slimeball"));
        e.a(342, "chest_minecart", (new ItemMinecart(1)).c("minecartChest").f("minecart_chest"));
        e.a(343, "furnace_minecart", (new ItemMinecart(2)).c("minecartFurnace").f("minecart_furnace"));
        e.a(344, "egg", (new ItemEgg()).c("egg").f("egg"));
        e.a(345, "compass", (new Item()).c("compass").a(CreativeTabs.i).f("compass"));
        e.a(346, "fishing_rod", (new ItemFishingRod()).c("fishingRod").f("fishing_rod"));
        e.a(347, "clock", (new Item()).c("clock").a(CreativeTabs.i).f("clock"));
        e.a(348, "glowstone_dust", (new Item()).c("yellowDust").e(PotionHelper.j).a(CreativeTabs.l).f("glowstone_dust"));
        e.a(349, "fish", (new ItemFishFood(false)).c("fish").f("fish_raw").a(true));
        e.a(350, "cooked_fished", (new ItemFishFood(true)).c("fish").f("fish_cooked").a(true));
        e.a(351, "dye", (new ItemDye()).c("dyePowder").f("dye_powder"));
        e.a(352, "bone", (new Item()).c("bone").q().a(CreativeTabs.f).f("bone"));
        e.a(353, "sugar", (new Item()).c("sugar").e(PotionHelper.b).a(CreativeTabs.l).f("sugar"));
        e.a(354, "cake", (new ItemReed(Blocks.aQ)).e(1).c("cake").a(CreativeTabs.h).f("cake"));
        e.a(355, "bed", (new ItemBed()).e(1).c("bed").f("bed"));
        e.a(356, "repeater", (new ItemReed(Blocks.aR)).c("diode").a(CreativeTabs.d).f("repeater"));
        e.a(357, "cookie", (new ItemFood(2, 0.1F, false)).c("cookie").f("cookie"));
        e.a(358, "filled_map", (new ItemMap()).c("map").f("map_filled"));
        e.a(359, "shears", (new ItemShears()).c("shears").f("shears"));
        e.a(360, "melon", (new ItemFood(2, 0.3F, false)).c("melon").f("melon"));
        e.a(361, "pumpkin_seeds", (new ItemSeeds(Blocks.bb, Blocks.ak)).c("seeds_pumpkin").f("seeds_pumpkin"));
        e.a(362, "melon_seeds", (new ItemSeeds(Blocks.bc, Blocks.ak)).c("seeds_melon").f("seeds_melon"));
        e.a(363, "beef", (new ItemFood(3, 0.3F, true)).c("beefRaw").f("beef_raw"));
        e.a(364, "cooked_beef", (new ItemFood(8, 0.8F, true)).c("beefCooked").f("beef_cooked"));
        e.a(365, "chicken", (new ItemFood(2, 0.3F, true)).a(Potion.s.H, 30, 0, 0.3F).c("chickenRaw").f("chicken_raw"));
        e.a(366, "cooked_chicken", (new ItemFood(6, 0.6F, true)).c("chickenCooked").f("chicken_cooked"));
        e.a(367, "rotten_flesh", (new ItemFood(4, 0.1F, true)).a(Potion.s.H, 30, 0, 0.8F).c("rottenFlesh").f("rotten_flesh"));
        e.a(368, "ender_pearl", (new ItemEnderPearl()).c("enderPearl").f("ender_pearl"));
        e.a(369, "blaze_rod", (new Item()).c("blazeRod").a(CreativeTabs.l).f("blaze_rod"));
        e.a(370, "ghast_tear", (new Item()).c("ghastTear").e(PotionHelper.c).a(CreativeTabs.k).f("ghast_tear"));
        e.a(371, "gold_nugget", (new Item()).c("goldNugget").a(CreativeTabs.l).f("gold_nugget"));
        e.a(372, "nether_wart", (new ItemSeeds(Blocks.bm, Blocks.aM)).c("netherStalkSeeds").e("+4").f("nether_wart"));
        e.a(373, "potion", (new ItemPotion()).c("potion").f("potion"));
        e.a(374, "glass_bottle", (new ItemGlassBottle()).c("glassBottle").f("potion_bottle_empty"));
        e.a(375, "spider_eye", (new ItemFood(2, 0.8F, false)).a(Potion.u.H, 5, 0, 1.0F).c("spiderEye").e(PotionHelper.d).f("spider_eye"));
        e.a(376, "fermented_spider_eye", (new Item()).c("fermentedSpiderEye").e(PotionHelper.e).a(CreativeTabs.k).f("spider_eye_fermented"));
        e.a(377, "blaze_powder", (new Item()).c("blazePowder").e(PotionHelper.g).a(CreativeTabs.k).f("blaze_powder"));
        e.a(378, "magma_cream", (new Item()).c("magmaCream").e(PotionHelper.h).a(CreativeTabs.k).f("magma_cream"));
        e.a(379, "brewing_stand", (new ItemReed(Blocks.bo)).c("brewingStand").a(CreativeTabs.k).f("brewing_stand"));
        e.a(380, "cauldron", (new ItemReed(Blocks.bp)).c("cauldron").a(CreativeTabs.k).f("cauldron"));
        e.a(381, "ender_eye", (new ItemEnderEye()).c("eyeOfEnder").f("ender_eye"));
        e.a(382, "speckled_melon", (new Item()).c("speckledMelon").e(PotionHelper.f).a(CreativeTabs.k).f("melon_speckled"));
        e.a(383, "spawn_egg", (new ItemMonsterPlacer()).c("monsterPlacer").f("spawn_egg"));
        e.a(384, "experience_bottle", (new ItemExpBottle()).c("expBottle").f("experience_bottle"));
        e.a(385, "fire_charge", (new ItemFireball()).c("fireball").f("fireball"));
        e.a(386, "writable_book", (new ItemWritableBook()).c("writingBook").a(CreativeTabs.f).f("book_writable"));
        e.a(387, "written_book", (new ItemEditableBook()).c("writtenBook").f("book_written").e(16));
        e.a(388, "emerald", (new Item()).c("emerald").a(CreativeTabs.l).f("emerald"));
        e.a(389, "item_frame", (new ItemHangingEntity(EntityItemFrame.class)).c("frame").f("item_frame"));
        e.a(390, "flower_pot", (new ItemReed(Blocks.bL)).c("flowerPot").a(CreativeTabs.c).f("flower_pot"));
        e.a(391, "carrot", (new ItemSeedFood(4, 0.6F, Blocks.bM, Blocks.ak)).c("carrots").f("carrot"));
        e.a(392, "potato", (new ItemSeedFood(1, 0.3F, Blocks.bN, Blocks.ak)).c("potato").f("potato"));
        e.a(393, "baked_potato", (new ItemFood(6, 0.6F, false)).c("potatoBaked").f("potato_baked"));
        e.a(394, "poisonous_potato", (new ItemFood(2, 0.3F, false)).a(Potion.u.H, 5, 0, 0.6F).c("potatoPoisonous").f("potato_poisonous"));
        e.a(395, "map", (new ItemEmptyMap()).c("emptyMap").f("map_empty"));
        e.a(396, "golden_carrot", (new ItemFood(6, 1.2F, false)).c("carrotGolden").e(PotionHelper.l).f("carrot_golden"));
        e.a(397, "skull", (new ItemSkull()).c("skull").f("skull"));
        e.a(398, "carrot_on_a_stick", (new ItemCarrotOnAStick()).c("carrotOnAStick").f("carrot_on_a_stick"));
        e.a(399, "nether_star", (new ItemSimpleFoiled()).c("netherStar").a(CreativeTabs.l).f("nether_star"));
        e.a(400, "pumpkin_pie", (new ItemFood(8, 0.3F, false)).c("pumpkinPie").a(CreativeTabs.h).f("pumpkin_pie"));
        e.a(401, "fireworks", (new ItemFirework()).c("fireworks").f("fireworks"));
        e.a(402, "firework_charge", (new ItemFireworkCharge()).c("fireworksCharge").a(CreativeTabs.f).f("fireworks_charge"));
        e.a(403, "enchanted_book", (new ItemEnchantedBook()).e(1).c("enchantedBook").f("book_enchanted"));
        e.a(404, "comparator", (new ItemReed(Blocks.bU)).c("comparator").a(CreativeTabs.d).f("comparator"));
        e.a(405, "netherbrick", (new Item()).c("netherbrick").a(CreativeTabs.l).f("netherbrick"));
        e.a(406, "quartz", (new Item()).c("netherquartz").a(CreativeTabs.l).f("quartz"));
        e.a(407, "tnt_minecart", (new ItemMinecart(3)).c("minecartTnt").f("minecart_tnt"));
        e.a(408, "hopper_minecart", (new ItemMinecart(5)).c("minecartHopper").f("minecart_hopper"));
        e.a(417, "iron_horse_armor", (new Item()).c("horsearmormetal").e(1).a(CreativeTabs.f).f("iron_horse_armor"));
        e.a(418, "golden_horse_armor", (new Item()).c("horsearmorgold").e(1).a(CreativeTabs.f).f("gold_horse_armor"));
        e.a(419, "diamond_horse_armor", (new Item()).c("horsearmordiamond").e(1).a(CreativeTabs.f).f("diamond_horse_armor"));
        e.a(420, "lead", (new ItemLead()).c("leash").f("lead"));
        e.a(421, "name_tag", (new ItemNameTag()).c("nameTag").f("name_tag"));
        e.a(422, "command_block_minecart", (new ItemMinecart(6)).c("minecartCommandBlock").f("minecart_command_block").a((CreativeTabs) null));
        e.a(2256, "record_13", (new ItemRecord("13")).c("record").f("record_13"));
        e.a(2257, "record_cat", (new ItemRecord("cat")).c("record").f("record_cat"));
        e.a(2258, "record_blocks", (new ItemRecord("blocks")).c("record").f("record_blocks"));
        e.a(2259, "record_chirp", (new ItemRecord("chirp")).c("record").f("record_chirp"));
        e.a(2260, "record_far", (new ItemRecord("far")).c("record").f("record_far"));
        e.a(2261, "record_mall", (new ItemRecord("mall")).c("record").f("record_mall"));
        e.a(2262, "record_mellohi", (new ItemRecord("mellohi")).c("record").f("record_mellohi"));
        e.a(2263, "record_stal", (new ItemRecord("stal")).c("record").f("record_stal"));
        e.a(2264, "record_strad", (new ItemRecord("strad")).c("record").f("record_strad"));
        e.a(2265, "record_ward", (new ItemRecord("ward")).c("record").f("record_ward"));
        e.a(2266, "record_11", (new ItemRecord("11")).c("record").f("record_11"));
        e.a(2267, "record_wait", (new ItemRecord("wait")).c("record").f("record_wait"));
        HashSet hashset = Sets.newHashSet(new Block[]{ Blocks.a, Blocks.bo, Blocks.C, Blocks.bm, Blocks.bp, Blocks.bL, Blocks.aj, Blocks.aH, Blocks.aQ, Blocks.bP, Blocks.K, Blocks.M, Blocks.ay, Blocks.aS, Blocks.bb, Blocks.an, Blocks.bV, Blocks.bD, Blocks.bv, Blocks.bc, Blocks.az, Blocks.bU, Blocks.af, Blocks.as, Blocks.aR, Blocks.av, Blocks.ao });
        Iterator iterator = Block.c.b().iterator();

        while (iterator.hasNext()) {
            String s0 = (String) iterator.next();
            Block block = (Block) Block.c.a(s0);
            Object object;

            if (block == Blocks.L) {
                object = (new ItemCloth(Blocks.L)).b("cloth");
            }
            else if (block == Blocks.ce) {
                object = (new ItemCloth(Blocks.ce)).b("clayHardenedStained");
            }
            else if (block == Blocks.cn) {
                object = (new ItemCloth(Blocks.cn)).b("stainedGlass");
            }
            else if (block == Blocks.co) {
                object = (new ItemCloth(Blocks.co)).b("stainedGlassPane");
            }
            else if (block == Blocks.cg) {
                object = (new ItemCloth(Blocks.cg)).b("woolCarpet");
            }
            else if (block == Blocks.d) {
                object = (new ItemMultiTexture(Blocks.d, Blocks.d, BlockDirt.a)).b("dirt");
            }
            else if (block == Blocks.m) {
                object = (new ItemMultiTexture(Blocks.m, Blocks.m, BlockSand.a)).b("sand");
            }
            else if (block == Blocks.r) {
                object = (new ItemMultiTexture(Blocks.r, Blocks.r, BlockOldLog.M)).b("log");
            }
            else if (block == Blocks.s) {
                object = (new ItemMultiTexture(Blocks.s, Blocks.s, BlockNewLog.M)).b("log");
            }
            else if (block == Blocks.f) {
                object = (new ItemMultiTexture(Blocks.f, Blocks.f, BlockWood.a)).b("wood");
            }
            else if (block == Blocks.aU) {
                object = (new ItemMultiTexture(Blocks.aU, Blocks.aU, BlockSilverfish.a)).b("monsterStoneEgg");
            }
            else if (block == Blocks.aV) {
                object = (new ItemMultiTexture(Blocks.aV, Blocks.aV, BlockStoneBrick.a)).b("stonebricksmooth");
            }
            else if (block == Blocks.A) {
                object = (new ItemMultiTexture(Blocks.A, Blocks.A, BlockSandStone.a)).b("sandStone");
            }
            else if (block == Blocks.ca) {
                object = (new ItemMultiTexture(Blocks.ca, Blocks.ca, BlockQuartz.a)).b("quartzBlock");
            }
            else if (block == Blocks.U) {
                object = (new ItemSlab(Blocks.U, Blocks.U, Blocks.T, false)).b("stoneSlab");
            }
            else if (block == Blocks.T) {
                object = (new ItemSlab(Blocks.T, Blocks.U, Blocks.T, true)).b("stoneSlab");
            }
            else if (block == Blocks.bx) {
                object = (new ItemSlab(Blocks.bx, Blocks.bx, Blocks.bw, false)).b("woodSlab");
            }
            else if (block == Blocks.bw) {
                object = (new ItemSlab(Blocks.bw, Blocks.bx, Blocks.bw, true)).b("woodSlab");
            }
            else if (block == Blocks.g) {
                object = (new ItemMultiTexture(Blocks.g, Blocks.g, BlockSapling.a)).b("sapling");
            }
            else if (block == Blocks.t) {
                object = (new ItemLeaves(Blocks.t)).b("leaves");
            }
            else if (block == Blocks.u) {
                object = (new ItemLeaves(Blocks.u)).b("leaves");
            }
            else if (block == Blocks.bd) {
                object = new ItemColored(Blocks.bd, false);
            }
            else if (block == Blocks.H) {
                object = (new ItemColored(Blocks.H, true)).a(new String[]{ "shrub", "grass", "fern" });
            }
            else if (block == Blocks.N) {
                object = (new ItemMultiTexture(Blocks.N, Blocks.N, BlockFlower.b)).b("flower");
            }
            else if (block == Blocks.O) {
                object = (new ItemMultiTexture(Blocks.O, Blocks.O, BlockFlower.a)).b("rose");
            }
            else if (block == Blocks.aC) {
                object = new ItemSnow(Blocks.aC, Blocks.aC);
            }
            else if (block == Blocks.bi) {
                object = new ItemLilyPad(Blocks.bi);
            }
            else if (block == Blocks.J) {
                object = new ItemPiston(Blocks.J);
            }
            else if (block == Blocks.F) {
                object = new ItemPiston(Blocks.F);
            }
            else if (block == Blocks.bK) {
                object = (new ItemMultiTexture(Blocks.bK, Blocks.bK, BlockWall.a)).b("cobbleWall");
            }
            else if (block == Blocks.bQ) {
                object = (new ItemAnvilBlock(Blocks.bQ)).b("anvil");
            }
            else if (block == Blocks.cm) {
                object = (new ItemDoublePlant(Blocks.cm, Blocks.cm, BlockDoublePlant.a)).b("doublePlant");
            }
            else {
                if (hashset.contains(block)) {
                    continue;
                }

                object = new ItemBlock(block);
            }

            e.a(Block.b(block), s0, object);
        }

    }

    protected Item() {
        this.base = new CanaryBaseItem(this); // CanaryMod: wrap Item
    }

    public Item e(int i0) {
        this.h = i0;
        return this;
    }

    public boolean a(ItemStack itemstack, EntityPlayer entityplayer, World world, int i0, int i1, int i2, int i3, float f0, float f1, float f2) {
        return false;
    }

    public float a(ItemStack itemstack, Block block) {
        return 1.0F;
    }

    public ItemStack a(ItemStack itemstack, World world, EntityPlayer entityplayer) {
        return itemstack;
    }

    public ItemStack b(ItemStack itemstack, World world, EntityPlayer entityplayer) {
        return itemstack;
    }

    public int m() {
        return this.h;
    }

    public int a(int i0) {
        return 0;
    }

    public boolean n() {
        return this.j;
    }

    protected Item a(boolean flag0) {
        this.j = flag0;
        return this;
    }

    public int o() {
        return this.b;
    }

    public Item f(int i0) { // CanaryMod: protected -> public
        this.b = i0;
        return this;
    }

    public boolean p() {
        return this.b > 0 && !this.j;
    }

    public boolean a(ItemStack itemstack, EntityLivingBase entitylivingbase, EntityLivingBase entitylivingbase1) {
        return false;
    }

    public boolean a(ItemStack itemstack, World world, Block block, int i0, int i1, int i2, EntityLivingBase entitylivingbase) {
        return false;
    }

    public boolean b(Block block) {
        return false;
    }

    public boolean a(ItemStack itemstack, EntityPlayer entityplayer, EntityLivingBase entitylivingbase) {
        return false;
    }

    public Item q() {
        this.i = true;
        return this;
    }

    public Item c(String s0) {
        this.m = s0;
        return this;
    }

    public String k(ItemStack itemstack) {
        String s0 = this.a(itemstack);

        return s0 == null ? "" : StatCollector.a(s0);
    }

    public String a() {
        return "item." + this.m;
    }

    public String a(ItemStack itemstack) {
        return "item." + this.m;
    }

    public Item c(Item item) {
        this.c = item;
        return this;
    }

    public boolean l(ItemStack itemstack) {
        return true;
    }

    public boolean s() {
        return true;
    }

    public Item t() {
        return this.c;
    }

    public boolean u() {
        return this.c != null;
    }

    public void a(ItemStack itemstack, World world, Entity entity, int i0, boolean flag0) {
    }

    public void d(ItemStack itemstack, World world, EntityPlayer entityplayer) {
    }

    public boolean h() {
        return false;
    }

    public EnumAction d(ItemStack itemstack) {
        return EnumAction.none;
    }

    public int d_(ItemStack itemstack) {
        return 0;
    }

    public void a(ItemStack itemstack, World world, EntityPlayer entityplayer, int i0) {
    }

    protected Item e(String s0) {
        this.d = s0;
        return this;
    }

    public String i(ItemStack itemstack) {
        return this.d;
    }

    public boolean m(ItemStack itemstack) {
        return this.i(itemstack) != null;
    }

    public String n(ItemStack itemstack) {
        return ("" + StatCollector.a(this.k(itemstack) + ".name")).trim();
    }

    public EnumRarity f(ItemStack itemstack) {
        return itemstack.y() ? EnumRarity.rare : EnumRarity.common;
    }

    public boolean e_(ItemStack itemstack) {
        return this.m() == 1 && this.p();
    }

    protected MovingObjectPosition a(World world, EntityPlayer entityplayer, boolean flag0) {
        float f0 = 1.0F;
        float f1 = entityplayer.C + (entityplayer.A - entityplayer.C) * f0;
        float f2 = entityplayer.B + (entityplayer.z - entityplayer.B) * f0;
        double d0 = entityplayer.q + (entityplayer.t - entityplayer.q) * (double) f0;
        double d1 = entityplayer.r + (entityplayer.u - entityplayer.r) * (double) f0 + 1.62D - (double) entityplayer.M;
        double d2 = entityplayer.s + (entityplayer.v - entityplayer.s) * (double) f0;
        Vec3 vec3 = world.U().a(d0, d1, d2);
        float f3 = MathHelper.b(-f2 * 0.017453292F - 3.1415927F);
        float f4 = MathHelper.a(-f2 * 0.017453292F - 3.1415927F);
        float f5 = -MathHelper.b(-f1 * 0.017453292F);
        float f6 = MathHelper.a(-f1 * 0.017453292F);
        float f7 = f4 * f5;
        float f8 = f3 * f5;
        double d3 = 5.0D;
        Vec3 vec31 = vec3.c((double) f7 * d3, (double) f6 * d3, (double) f8 * d3);

        return world.a(vec3, vec31, flag0, !flag0, false);
    }

    public int c() {
        return 0;
    }

    public Item a(CreativeTabs creativetabs) {
        this.a = creativetabs;
        return this;
    }

    public boolean v() {
        return true;
    }

    public boolean a(ItemStack itemstack, ItemStack itemstack1) {
        return false;
    }

    public Multimap k() {
        return HashMultimap.create();
    }

    protected Item f(String s0) {
        this.l = s0;
        return this;
    }

    public static enum ToolMaterial {

        WOOD("WOOD", 0, 0, 59, 2.0F, 0.0F, 15), STONE("STONE", 1, 1, 131, 4.0F, 1.0F, 5), IRON("IRON", 2, 2, 250, 6.0F, 2.0F, 14), EMERALD("EMERALD", 3, 3, 1561, 8.0F, 3.0F, 10), GOLD("GOLD", 4, 0, 32, 12.0F, 0.0F, 22);
        private final int f;
        private final int g;
        private final float h;
        private final float i;
        private final int j;

        private static final ToolMaterial[] $VALUES = new ToolMaterial[]{ WOOD, STONE, IRON, EMERALD, GOLD };

        private ToolMaterial(String s0, int i0, int i1, int i2, float f0, float f1, int i3) {
            this.f = i1;
            this.g = i2;
            this.h = f0;
            this.i = f1;
            this.j = i3;
        }

        public int a() {
            return this.g;
        }

        public float b() {
            return this.h;
        }

        public float c() {
            return this.i;
        }

        public int d() {
            return this.f;
        }

        public int e() {
            return this.j;
        }

        public Item f() {
            return this == WOOD ? Item.a(Blocks.f) : (this == STONE ? Item.a(Blocks.e) : (this == GOLD ? Items.k : (this == IRON ? Items.j : (this == EMERALD ? Items.i : null))));
        }

    }

    // CanaryMod
    public CanaryBaseItem getBaseItem() {
        return this.base;
    }

    //
}

