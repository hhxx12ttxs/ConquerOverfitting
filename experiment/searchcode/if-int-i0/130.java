package net.minecraft.block;

import net.canarymod.hook.world.BlockDropXpHook;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.RegistryNamespaced;
import net.minecraft.init.RegistryNamespacedDefaultedByKey;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Block {

    public static final RegistryNamespaced c = new RegistryNamespacedDefaultedByKey("air");
    private CreativeTabs a;
    protected String d;
    public static final SoundType e = new SoundType("stone", 1.0F, 1.0F);
    public static final SoundType f = new SoundType("wood", 1.0F, 1.0F);
    public static final SoundType g = new SoundType("gravel", 1.0F, 1.0F);
    public static final SoundType h = new SoundType("grass", 1.0F, 1.0F);
    public static final SoundType i = new SoundType("stone", 1.0F, 1.0F);
    public static final SoundType j = new SoundType("stone", 1.0F, 1.5F);
    public static final SoundType k = new SoundType("stone", 1.0F, 1.0F) {

        public String a() {
            return "dig.glass";
        }

        public String b() {
            return "step.stone";
        }
    };
    public static final SoundType l = new SoundType("cloth", 1.0F, 1.0F);
    public static final SoundType m = new SoundType("sand", 1.0F, 1.0F);
    public static final SoundType n = new SoundType("snow", 1.0F, 1.0F);
    public static final SoundType o = new SoundType("ladder", 1.0F, 1.0F) {

        public String a() {
            return "dig.wood";
        }
    };
    public static final SoundType p = new SoundType("anvil", 0.3F, 1.0F) {

        public String a() {
            return "dig.stone";
        }

        public String b() {
            return "random.anvil_land";
        }
    };
    protected boolean q;
    protected int r;
    protected boolean s;
    protected int t;
    protected boolean u;
    protected float v;
    protected float w;
    protected boolean x = true;
    protected boolean y = true;
    protected boolean z;
    protected boolean A;
    protected double B;
    protected double C;
    protected double D;
    protected double E;
    protected double F;
    protected double G;
    public SoundType H;
    public float I;
    protected final Material J;
    public float K;
    private String b;

    public static int b(Block block) {
        return c.b((Object) block);
    }

    public static Block e(int i0) {
        return (Block) c.a(i0);
    }

    public static Block a(Item item) {
        return e(Item.b(item));
    }

    public static Block b(String s0) {
        if (c.b(s0)) {
            return (Block) c.a(s0);
        }
        else {
            try {
                return (Block) c.a(Integer.parseInt(s0));
            }
            catch (NumberFormatException numberformatexception) {
                return null;
            }
        }
    }

    public boolean j() {
        return this.q;
    }

    public int k() {
        return this.r;
    }

    public int m() {
        return this.t;
    }

    public boolean n() {
        return this.u;
    }

    public Material o() {
        return this.J;
    }

    public MapColor f(int i0) {
        return this.o().r();
    }

    public static void p() {
        c.a(0, "air", (new BlockAir()).c("air"));
        c.a(1, "stone", (new BlockStone()).c(1.5F).b(10.0F).a(i).c("stone").d("stone"));
        c.a(2, "grass", (new BlockGrass()).c(0.6F).a(h).c("grass").d("grass"));
        c.a(3, "dirt", (new BlockDirt()).c(0.5F).a(g).c("dirt").d("dirt"));
        Block block = (new Block(Material.e)).c(2.0F).b(10.0F).a(i).c("stonebrick").a(CreativeTabs.b).d("cobblestone");

        c.a(4, "cobblestone", block);
        Block block1 = (new BlockWood()).c(2.0F).b(5.0F).a(f).c("wood").d("planks");

        c.a(5, "planks", block1);
        c.a(6, "sapling", (new BlockSapling()).c(0.0F).a(h).c("sapling").d("sapling"));
        c.a(7, "bedrock", (new Block(Material.e)).s().b(6000000.0F).a(i).c("bedrock").H().a(CreativeTabs.b).d("bedrock"));
        c.a(8, "flowing_water", (new BlockDynamicLiquid(Material.h)).c(100.0F).g(3).c("water").H().d("water_flow"));
        c.a(9, "water", (new BlockStaticLiquid(Material.h)).c(100.0F).g(3).c("water").H().d("water_still"));
        c.a(10, "flowing_lava", (new BlockDynamicLiquid(Material.i)).c(100.0F).a(1.0F).c("lava").H().d("lava_flow"));
        c.a(11, "lava", (new BlockStaticLiquid(Material.i)).c(100.0F).a(1.0F).c("lava").H().d("lava_still"));
        c.a(12, "sand", (new BlockSand()).c(0.5F).a(m).c("sand").d("sand"));
        c.a(13, "gravel", (new BlockGravel()).c(0.6F).a(g).c("gravel").d("gravel"));
        c.a(14, "gold_ore", (new BlockOre()).c(3.0F).b(5.0F).a(i).c("oreGold").d("gold_ore"));
        c.a(15, "iron_ore", (new BlockOre()).c(3.0F).b(5.0F).a(i).c("oreIron").d("iron_ore"));
        c.a(16, "coal_ore", (new BlockOre()).c(3.0F).b(5.0F).a(i).c("oreCoal").d("coal_ore"));
        c.a(17, "log", (new BlockOldLog()).c("log").d("log"));
        c.a(18, "leaves", (new BlockOldLeaf()).c("leaves").d("leaves"));
        c.a(19, "sponge", (new BlockSponge()).c(0.6F).a(h).c("sponge").d("sponge"));
        c.a(20, "glass", (new BlockGlass(Material.s, false)).c(0.3F).a(k).c("glass").d("glass"));
        c.a(21, "lapis_ore", (new BlockOre()).c(3.0F).b(5.0F).a(i).c("oreLapis").d("lapis_ore"));
        c.a(22, "lapis_block", (new BlockCompressed(MapColor.H)).c(3.0F).b(5.0F).a(i).c("blockLapis").a(CreativeTabs.b).d("lapis_block"));
        c.a(23, "dispenser", (new BlockDispenser()).c(3.5F).a(i).c("dispenser").d("dispenser"));
        Block block2 = (new BlockSandStone()).a(i).c(0.8F).c("sandStone").d("sandstone");

        c.a(24, "sandstone", block2);
        c.a(25, "noteblock", (new BlockNote()).c(0.8F).c("musicBlock").d("noteblock"));
        c.a(26, "bed", (new BlockBed()).c(0.2F).c("bed").H().d("bed"));
        c.a(27, "golden_rail", (new BlockRailPowered()).c(0.7F).a(j).c("goldenRail").d("rail_golden"));
        c.a(28, "detector_rail", (new BlockRailDetector()).c(0.7F).a(j).c("detectorRail").d("rail_detector"));
        c.a(29, "sticky_piston", (new BlockPistonBase(true)).c("pistonStickyBase"));
        c.a(30, "web", (new BlockWeb()).g(1).c(4.0F).c("web").d("web"));
        c.a(31, "tallgrass", (new BlockTallGrass()).c(0.0F).a(h).c("tallgrass"));
        c.a(32, "deadbush", (new BlockDeadBush()).c(0.0F).a(h).c("deadbush").d("deadbush"));
        c.a(33, "piston", (new BlockPistonBase(false)).c("pistonBase"));
        c.a(34, "piston_head", new BlockPistonExtension());
        c.a(35, "wool", (new BlockColored(Material.n)).c(0.8F).a(l).c("cloth").d("wool_colored"));
        c.a(36, "piston_extension", new BlockPistonMoving());
        c.a(37, "yellow_flower", (new BlockFlower(0)).c(0.0F).a(h).c("flower1").d("flower_dandelion"));
        c.a(38, "red_flower", (new BlockFlower(1)).c(0.0F).a(h).c("flower2").d("flower_rose"));
        c.a(39, "brown_mushroom", (new BlockMushroom()).c(0.0F).a(h).a(0.125F).c("mushroom").d("mushroom_brown"));
        c.a(40, "red_mushroom", (new BlockMushroom()).c(0.0F).a(h).c("mushroom").d("mushroom_red"));
        c.a(41, "gold_block", (new BlockCompressed(MapColor.F)).c(3.0F).b(10.0F).a(j).c("blockGold").d("gold_block"));
        c.a(42, "iron_block", (new BlockCompressed(MapColor.h)).c(5.0F).b(10.0F).a(j).c("blockIron").d("iron_block"));
        c.a(43, "double_stone_slab", (new BlockStoneSlab(true)).c(2.0F).b(10.0F).a(i).c("stoneSlab"));
        c.a(44, "stone_slab", (new BlockStoneSlab(false)).c(2.0F).b(10.0F).a(i).c("stoneSlab"));
        Block block3 = (new Block(Material.e)).c(2.0F).b(10.0F).a(i).c("brick").a(CreativeTabs.b).d("brick");

        c.a(45, "brick_block", block3);
        c.a(46, "tnt", (new BlockTNT()).c(0.0F).a(h).c("tnt").d("tnt"));
        c.a(47, "bookshelf", (new BlockBookshelf()).c(1.5F).a(f).c("bookshelf").d("bookshelf"));
        c.a(48, "mossy_cobblestone", (new Block(Material.e)).c(2.0F).b(10.0F).a(i).c("stoneMoss").a(CreativeTabs.b).d("cobblestone_mossy"));
        c.a(49, "obsidian", (new BlockObsidian()).c(50.0F).b(2000.0F).a(i).c("obsidian").d("obsidian"));
        c.a(50, "torch", (new BlockTorch()).c(0.0F).a(0.9375F).a(f).c("torch").d("torch_on"));
        c.a(51, "fire", (new BlockFire()).c(0.0F).a(1.0F).a(f).c("fire").H().d("fire"));
        c.a(52, "mob_spawner", (new BlockMobSpawner()).c(5.0F).a(j).c("mobSpawner").H().d("mob_spawner"));
        c.a(53, "oak_stairs", (new BlockStairs(block1, 0)).c("stairsWood"));
        c.a(54, "chest", (new BlockChest(0)).c(2.5F).a(f).c("chest"));
        c.a(55, "redstone_wire", (new BlockRedstoneWire()).c(0.0F).a(e).c("redstoneDust").H().d("redstone_dust"));
        c.a(56, "diamond_ore", (new BlockOre()).c(3.0F).b(5.0F).a(i).c("oreDiamond").d("diamond_ore"));
        c.a(57, "diamond_block", (new BlockCompressed(MapColor.G)).c(5.0F).b(10.0F).a(j).c("blockDiamond").d("diamond_block"));
        c.a(58, "crafting_table", (new BlockWorkbench()).c(2.5F).a(f).c("workbench").d("crafting_table"));
        c.a(59, "wheat", (new BlockCrops()).c("crops").d("wheat"));
        Block block4 = (new BlockFarmland()).c(0.6F).a(g).c("farmland").d("farmland");

        c.a(60, "farmland", block4);
        c.a(61, "furnace", (new BlockFurnace(false)).c(3.5F).a(i).c("furnace").a(CreativeTabs.c));
        c.a(62, "lit_furnace", (new BlockFurnace(true)).c(3.5F).a(i).a(0.875F).c("furnace"));
        c.a(63, "standing_sign", (new BlockSign(TileEntitySign.class, true)).c(1.0F).a(f).c("sign").H());
        c.a(64, "wooden_door", (new BlockDoor(Material.d)).c(3.0F).a(f).c("doorWood").H().d("door_wood"));
        c.a(65, "ladder", (new BlockLadder()).c(0.4F).a(o).c("ladder").d("ladder"));
        c.a(66, "rail", (new BlockRail()).c(0.7F).a(j).c("rail").d("rail_normal"));
        c.a(67, "stone_stairs", (new BlockStairs(block, 0)).c("stairsStone"));
        c.a(68, "wall_sign", (new BlockSign(TileEntitySign.class, false)).c(1.0F).a(f).c("sign").H());
        c.a(69, "lever", (new BlockLever()).c(0.5F).a(f).c("lever").d("lever"));
        c.a(70, "stone_pressure_plate", (new BlockPressurePlate("stone", Material.e, BlockPressurePlate.Sensitivity.mobs)).c(0.5F).a(i).c("pressurePlate"));
        c.a(71, "iron_door", (new BlockDoor(Material.f)).c(5.0F).a(j).c("doorIron").H().d("door_iron"));
        c.a(72, "wooden_pressure_plate", (new BlockPressurePlate("planks_oak", Material.d, BlockPressurePlate.Sensitivity.everything)).c(0.5F).a(f).c("pressurePlate"));
        c.a(73, "redstone_ore", (new BlockRedstoneOre(false)).c(3.0F).b(5.0F).a(i).c("oreRedstone").a(CreativeTabs.b).d("redstone_ore"));
        c.a(74, "lit_redstone_ore", (new BlockRedstoneOre(true)).a(0.625F).c(3.0F).b(5.0F).a(i).c("oreRedstone").d("redstone_ore"));
        c.a(75, "unlit_redstone_torch", (new BlockRedstoneTorch(false)).c(0.0F).a(f).c("notGate").d("redstone_torch_off"));
        c.a(76, "redstone_torch", (new BlockRedstoneTorch(true)).c(0.0F).a(0.5F).a(f).c("notGate").a(CreativeTabs.d).d("redstone_torch_on"));
        c.a(77, "stone_button", (new BlockButtonStone()).c(0.5F).a(i).c("button"));
        c.a(78, "snow_layer", (new BlockSnow()).c(0.1F).a(n).c("snow").g(0).d("snow"));
        c.a(79, "ice", (new BlockIce()).c(0.5F).g(3).a(k).c("ice").d("ice"));
        c.a(80, "snow", (new BlockSnowBlock()).c(0.2F).a(n).c("snow").d("snow"));
        c.a(81, "cactus", (new BlockCactus()).c(0.4F).a(l).c("cactus").d("cactus"));
        c.a(82, "clay", (new BlockClay()).c(0.6F).a(g).c("clay").d("clay"));
        c.a(83, "reeds", (new BlockReed()).c(0.0F).a(h).c("reeds").H().d("reeds"));
        c.a(84, "jukebox", (new BlockJukebox()).c(2.0F).b(10.0F).a(i).c("jukebox").d("jukebox"));
        c.a(85, "fence", (new BlockFence("planks_oak", Material.d)).c(2.0F).b(5.0F).a(f).c("fence"));
        Block block5 = (new BlockPumpkin(false)).c(1.0F).a(f).c("pumpkin").d("pumpkin");

        c.a(86, "pumpkin", block5);
        c.a(87, "netherrack", (new BlockNetherrack()).c(0.4F).a(i).c("hellrock").d("netherrack"));
        c.a(88, "soul_sand", (new BlockSoulSand()).c(0.5F).a(m).c("hellsand").d("soul_sand"));
        c.a(89, "glowstone", (new BlockGlowstone(Material.s)).c(0.3F).a(k).a(1.0F).c("lightgem").d("glowstone"));
        c.a(90, "portal", (new BlockPortal()).c(-1.0F).a(k).a(0.75F).c("portal").d("portal"));
        c.a(91, "lit_pumpkin", (new BlockPumpkin(true)).c(1.0F).a(f).a(1.0F).c("litpumpkin").d("pumpkin"));
        c.a(92, "cake", (new BlockCake()).c(0.5F).a(l).c("cake").H().d("cake"));
        c.a(93, "unpowered_repeater", (new BlockRedstoneRepeater(false)).c(0.0F).a(f).c("diode").H().d("repeater_off"));
        c.a(94, "powered_repeater", (new BlockRedstoneRepeater(true)).c(0.0F).a(0.625F).a(f).c("diode").H().d("repeater_on"));
        c.a(95, "stained_glass", (new BlockStainedGlass(Material.s)).c(0.3F).a(k).c("stainedGlass").d("glass"));
        c.a(96, "trapdoor", (new BlockTrapDoor(Material.d)).c(3.0F).a(f).c("trapdoor").H().d("trapdoor"));
        c.a(97, "monster_egg", (new BlockSilverfish()).c(0.75F).c("monsterStoneEgg"));
        Block block6 = (new BlockStoneBrick()).c(1.5F).b(10.0F).a(i).c("stonebricksmooth").d("stonebrick");

        c.a(98, "stonebrick", block6);
        c.a(99, "brown_mushroom_block", (new BlockHugeMushroom(Material.d, 0)).c(0.2F).a(f).c("mushroom").d("mushroom_block"));
        c.a(100, "red_mushroom_block", (new BlockHugeMushroom(Material.d, 1)).c(0.2F).a(f).c("mushroom").d("mushroom_block"));
        c.a(101, "iron_bars", (new BlockPane("iron_bars", "iron_bars", Material.f, true)).c(5.0F).b(10.0F).a(j).c("fenceIron"));
        c.a(102, "glass_pane", (new BlockPane("glass", "glass_pane_top", Material.s, false)).c(0.3F).a(k).c("thinGlass"));
        Block block7 = (new BlockMelon()).c(1.0F).a(f).c("melon").d("melon");

        c.a(103, "melon_block", block7);
        c.a(104, "pumpkin_stem", (new BlockStem(block5)).c(0.0F).a(f).c("pumpkinStem").d("pumpkin_stem"));
        c.a(105, "melon_stem", (new BlockStem(block7)).c(0.0F).a(f).c("pumpkinStem").d("melon_stem"));
        c.a(106, "vine", (new BlockVine()).c(0.2F).a(h).c("vine").d("vine"));
        c.a(107, "fence_gate", (new BlockFenceGate()).c(2.0F).b(5.0F).a(f).c("fenceGate"));
        c.a(108, "brick_stairs", (new BlockStairs(block3, 0)).c("stairsBrick"));
        c.a(109, "stone_brick_stairs", (new BlockStairs(block6, 0)).c("stairsStoneBrickSmooth"));
        c.a(110, "mycelium", (new BlockMycelium()).c(0.6F).a(h).c("mycel").d("mycelium"));
        c.a(111, "waterlily", (new BlockLilyPad()).c(0.0F).a(h).c("waterlily").d("waterlily"));
        Block block8 = (new Block(Material.e)).c(2.0F).b(10.0F).a(i).c("netherBrick").a(CreativeTabs.b).d("nether_brick");

        c.a(112, "nether_brick", block8);
        c.a(113, "nether_brick_fence", (new BlockFence("nether_brick", Material.e)).c(2.0F).b(10.0F).a(i).c("netherFence"));
        c.a(114, "nether_brick_stairs", (new BlockStairs(block8, 0)).c("stairsNetherBrick"));
        c.a(115, "nether_wart", (new BlockNetherWart()).c("netherStalk").d("nether_wart"));
        c.a(116, "enchanting_table", (new BlockEnchantmentTable()).c(5.0F).b(2000.0F).c("enchantmentTable").d("enchanting_table"));
        c.a(117, "brewing_stand", (new BlockBrewingStand()).c(0.5F).a(0.125F).c("brewingStand").d("brewing_stand"));
        c.a(118, "cauldron", (new BlockCauldron()).c(2.0F).c("cauldron").d("cauldron"));
        c.a(119, "end_portal", (new BlockEndPortal(Material.E)).c(-1.0F).b(6000000.0F));
        c.a(120, "end_portal_frame", (new BlockEndPortalFrame()).a(k).a(0.125F).c(-1.0F).c("endPortalFrame").b(6000000.0F).a(CreativeTabs.c).d("endframe"));
        c.a(121, "end_stone", (new Block(Material.e)).c(3.0F).b(15.0F).a(i).c("whiteStone").a(CreativeTabs.b).d("end_stone"));
        c.a(122, "dragon_egg", (new BlockDragonEgg()).c(3.0F).b(15.0F).a(i).a(0.125F).c("dragonEgg").d("dragon_egg"));
        c.a(123, "redstone_lamp", (new BlockRedstoneLight(false)).c(0.3F).a(k).c("redstoneLight").a(CreativeTabs.d).d("redstone_lamp_off"));
        c.a(124, "lit_redstone_lamp", (new BlockRedstoneLight(true)).c(0.3F).a(k).c("redstoneLight").d("redstone_lamp_on"));
        c.a(125, "double_wooden_slab", (new BlockWoodSlab(true)).c(2.0F).b(5.0F).a(f).c("woodSlab"));
        c.a(126, "wooden_slab", (new BlockWoodSlab(false)).c(2.0F).b(5.0F).a(f).c("woodSlab"));
        c.a(127, "cocoa", (new BlockCocoa()).c(0.2F).b(5.0F).a(f).c("cocoa").d("cocoa"));
        c.a(128, "sandstone_stairs", (new BlockStairs(block2, 0)).c("stairsSandStone"));
        c.a(129, "emerald_ore", (new BlockOre()).c(3.0F).b(5.0F).a(i).c("oreEmerald").d("emerald_ore"));
        c.a(130, "ender_chest", (new BlockEnderChest()).c(22.5F).b(1000.0F).a(i).c("enderChest").a(0.5F));
        c.a(131, "tripwire_hook", (new BlockTripWireHook()).c("tripWireSource").d("trip_wire_source"));
        c.a(132, "tripwire", (new BlockTripWire()).c("tripWire").d("trip_wire"));
        c.a(133, "emerald_block", (new BlockCompressed(MapColor.I)).c(5.0F).b(10.0F).a(j).c("blockEmerald").d("emerald_block"));
        c.a(134, "spruce_stairs", (new BlockStairs(block1, 1)).c("stairsWoodSpruce"));
        c.a(135, "birch_stairs", (new BlockStairs(block1, 2)).c("stairsWoodBirch"));
        c.a(136, "jungle_stairs", (new BlockStairs(block1, 3)).c("stairsWoodJungle"));
        c.a(137, "command_block", (new BlockCommandBlock()).s().b(6000000.0F).c("commandBlock").d("command_block"));
        c.a(138, "beacon", (new BlockBeacon()).c("beacon").a(1.0F).d("beacon"));
        c.a(139, "cobblestone_wall", (new BlockWall(block)).c("cobbleWall"));
        c.a(140, "flower_pot", (new BlockFlowerPot()).c(0.0F).a(e).c("flowerPot").d("flower_pot"));
        c.a(141, "carrots", (new BlockCarrot()).c("carrots").d("carrots"));
        c.a(142, "potatoes", (new BlockPotato()).c("potatoes").d("potatoes"));
        c.a(143, "wooden_button", (new BlockButtonWood()).c(0.5F).a(f).c("button"));
        c.a(144, "skull", (new BlockSkull()).c(1.0F).a(i).c("skull").d("skull"));
        c.a(145, "anvil", (new BlockAnvil()).c(5.0F).a(p).b(2000.0F).c("anvil"));
        c.a(146, "trapped_chest", (new BlockChest(1)).c(2.5F).a(f).c("chestTrap"));
        c.a(147, "light_weighted_pressure_plate", (new BlockPressurePlateWeighted("gold_block", Material.f, 15)).c(0.5F).a(f).c("weightedPlate_light"));
        c.a(148, "heavy_weighted_pressure_plate", (new BlockPressurePlateWeighted("iron_block", Material.f, 150)).c(0.5F).a(f).c("weightedPlate_heavy"));
        c.a(149, "unpowered_comparator", (new BlockRedstoneComparator(false)).c(0.0F).a(f).c("comparator").H().d("comparator_off"));
        c.a(150, "powered_comparator", (new BlockRedstoneComparator(true)).c(0.0F).a(0.625F).a(f).c("comparator").H().d("comparator_on"));
        c.a(151, "daylight_detector", (new BlockDaylightDetector()).c(0.2F).a(f).c("daylightDetector").d("daylight_detector"));
        c.a(152, "redstone_block", (new BlockCompressedPowered(MapColor.f)).c(5.0F).b(10.0F).a(j).c("blockRedstone").d("redstone_block"));
        c.a(153, "quartz_ore", (new BlockOre()).c(3.0F).b(5.0F).a(i).c("netherquartz").d("quartz_ore"));
        c.a(154, "hopper", (new BlockHopper()).c(3.0F).b(8.0F).a(f).c("hopper").d("hopper"));
        Block block9 = (new BlockQuartz()).a(i).c(0.8F).c("quartzBlock").d("quartz_block");

        c.a(155, "quartz_block", block9);
        c.a(156, "quartz_stairs", (new BlockStairs(block9, 0)).c("stairsQuartz"));
        c.a(157, "activator_rail", (new BlockRailPowered()).c(0.7F).a(j).c("activatorRail").d("rail_activator"));
        c.a(158, "dropper", (new BlockDropper()).c(3.5F).a(i).c("dropper").d("dropper"));
        c.a(159, "stained_hardened_clay", (new BlockColored(Material.e)).c(1.25F).b(7.0F).a(i).c("clayHardenedStained").d("hardened_clay_stained"));
        c.a(160, "stained_glass_pane", (new BlockStainedGlassPane()).c(0.3F).a(k).c("thinStainedGlass").d("glass"));
        c.a(161, "leaves2", (new BlockNewLeaf()).c("leaves").d("leaves"));
        c.a(162, "log2", (new BlockNewLog()).c("log").d("log"));
        c.a(163, "acacia_stairs", (new BlockStairs(block1, 4)).c("stairsWoodAcacia"));
        c.a(164, "dark_oak_stairs", (new BlockStairs(block1, 5)).c("stairsWoodDarkOak"));
        c.a(170, "hay_block", (new BlockHay()).c(0.5F).a(h).c("hayBlock").a(CreativeTabs.b).d("hay_block"));
        c.a(171, "carpet", (new BlockCarpet()).c(0.1F).a(l).c("woolCarpet").g(0));
        c.a(172, "hardened_clay", (new BlockHardenedClay()).c(1.25F).b(7.0F).a(i).c("clayHardened").d("hardened_clay"));
        c.a(173, "coal_block", (new Block(Material.e)).c(5.0F).b(10.0F).a(i).c("blockCoal").a(CreativeTabs.b).d("coal_block"));
        c.a(174, "packed_ice", (new BlockPackedIce()).c(0.5F).a(k).c("icePacked").d("ice_packed"));
        c.a(175, "double_plant", new BlockDoublePlant());
        Iterator iterator = c.iterator();

        while (iterator.hasNext()) {
            Block block10 = (Block) iterator.next();

            if (block10.J == Material.a) {
                block10.u = false;
            }
            else {
                boolean flag0 = false;
                boolean flag1 = block10.b() == 10;
                boolean flag2 = block10 instanceof BlockSlab;
                boolean flag3 = block10 == block4;
                boolean flag4 = block10.s;
                boolean flag5 = block10.r == 0;

                if (flag1 || flag2 || flag3 || flag4 || flag5) {
                    flag0 = true;
                }

                block10.u = flag0;
            }
        }

    }

    protected Block(Material material) {
        this.H = e;
        this.I = 1.0F;
        this.K = 0.6F;
        this.J = material;
        this.a(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        this.q = this.c();
        this.r = this.c() ? 255 : 0;
        this.s = !material.b();
    }

    protected Block a(SoundType block_soundtype) {
        this.H = block_soundtype;
        return this;
    }

    protected Block g(int i0) {
        this.r = i0;
        return this;
    }

    protected Block a(float f0) {
        this.t = (int) (15.0F * f0);
        return this;
    }

    protected Block b(float f0) {
        this.w = f0 * 3.0F;
        return this;
    }

    public boolean r() {
        return this.J.k() && this.d() && !this.f();
    }

    public boolean d() {
        return true;
    }

    public boolean b(IBlockAccess iblockaccess, int i0, int i1, int i2) {
        return !this.J.c();
    }

    public int b() {
        return 0;
    }

    protected Block c(float f0) {
        this.v = f0;
        if (this.w < f0 * 5.0F) {
            this.w = f0 * 5.0F;
        }

        return this;
    }

    protected Block s() {
        this.c(-1.0F);
        return this;
    }

    public float f(World world, int i0, int i1, int i2) {
        return this.v;
    }

    protected Block a(boolean flag0) {
        this.z = flag0;
        return this;
    }

    public boolean t() {
        return this.z;
    }

    public boolean u() {
        return this.A;
    }

    protected final void a(float f0, float f1, float f2, float f3, float f4, float f5) {
        this.B = (double) f0;
        this.C = (double) f1;
        this.D = (double) f2;
        this.E = (double) f3;
        this.F = (double) f4;
        this.G = (double) f5;
    }

    public boolean d(IBlockAccess iblockaccess, int i0, int i1, int i2, int i3) {
        return iblockaccess.a(i0, i1, i2).o().a();
    }

    public void a(World world, int i0, int i1, int i2, AxisAlignedBB axisalignedbb, List list, Entity entity) {
        AxisAlignedBB axisalignedbb1 = this.a(world, i0, i1, i2);

        if (axisalignedbb1 != null && axisalignedbb.b(axisalignedbb1)) {
            list.add(axisalignedbb1);
        }
    }

    public AxisAlignedBB a(World world, int i0, int i1, int i2) {
        return AxisAlignedBB.a().a((double) i0 + this.B, (double) i1 + this.C, (double) i2 + this.D, (double) i0 + this.E, (double) i1 + this.F, (double) i2 + this.G);
    }

    public boolean c() {
        return true;
    }

    public boolean a(int i0, boolean flag0) {
        return this.v();
    }

    public boolean v() {
        return true;
    }

    public void a(World world, int i0, int i1, int i2, Random random) {
    }

    public void b(World world, int i0, int i1, int i2, int i3) {
    }

    public void a(World world, int i0, int i1, int i2, Block block) {
    }

    public int a(World world) {
        return 10;
    }

    public void b(World world, int i0, int i1, int i2) {
    }

    public void a(World world, int i0, int i1, int i2, Block block, int i3) {
    }

    public int a(Random random) {
        return 1;
    }

    public Item a(int i0, Random random, int i1) {
        return Item.a(this);
    }

    public float a(EntityPlayer entityplayer, World world, int i0, int i1, int i2) {
        float f0 = this.f(world, i0, i1, i2);

        return f0 < 0.0F ? 0.0F : (!entityplayer.a(this) ? entityplayer.a(this, false) / f0 / 100.0F : entityplayer.a(this, true) / f0 / 30.0F);
    }

    public final void b(World world, int i0, int i1, int i2, int i3, int i4) {
        this.a(world, i0, i1, i2, i3, 1.0F, i4);
    }

    public void a(World world, int i0, int i1, int i2, int i3, float f0, int i4) {
        if (!world.E) {
            int i5 = this.a(i4, world.s);

            for (int i6 = 0; i6 < i5; ++i6) {
                if (world.s.nextFloat() <= f0) {
                    Item item = this.a(i3, world.s, i4);

                    if (item != null) {
                        this.a(world, i0, i1, i2, new ItemStack(item, 1, this.a(i3)));
                    }
                }
            }
        }
    }

    protected void a(World world, int i0, int i1, int i2, ItemStack itemstack) {
        if (!world.E && world.N().b("doTileDrops")) {
            float f0 = 0.7F;
            double d0 = (double) (world.s.nextFloat() * f0) + (double) (1.0F - f0) * 0.5D;
            double d1 = (double) (world.s.nextFloat() * f0) + (double) (1.0F - f0) * 0.5D;
            double d2 = (double) (world.s.nextFloat() * f0) + (double) (1.0F - f0) * 0.5D;
            EntityItem entityitem = new EntityItem(world, (double) i0 + d0, (double) i1 + d1, (double) i2 + d2, itemstack);

            entityitem.b = 10;
            world.d((Entity) entityitem);
        }
    }

    protected void c(World world, int i0, int i1, int i2, int i3) {
        if (!world.E) {
            // CanaryMod: BlockDropXpHook
            net.canarymod.api.world.blocks.Block block = world.getCanaryWorld().getBlockAt(i0, i1, i2);
            BlockDropXpHook hook = (BlockDropXpHook) new BlockDropXpHook(block, i3).call();
            if (hook.isCanceled()) {
                return;
            }
            i3 = hook.getXp();
            //

            while (i3 > 0) {
                int i4 = EntityXPOrb.a(i3);

                i3 -= i4;
                world.d((Entity) (new EntityXPOrb(world, (double) i0 + 0.5D, (double) i1 + 0.5D, (double) i2 + 0.5D, i4)));
            }
        }
    }

    public int a(int i0) {
        return 0;
    }

    public float a(Entity entity) {
        return this.w / 5.0F;
    }

    public MovingObjectPosition a(World world, int i0, int i1, int i2, Vec3 vec3, Vec3 vec31) {
        this.a((IBlockAccess) world, i0, i1, i2);
        vec3 = vec3.c((double) (-i0), (double) (-i1), (double) (-i2));
        vec31 = vec31.c((double) (-i0), (double) (-i1), (double) (-i2));
        Vec3 vec32 = vec3.b(vec31, this.B);
        Vec3 vec33 = vec3.b(vec31, this.E);
        Vec3 vec34 = vec3.c(vec31, this.C);
        Vec3 vec35 = vec3.c(vec31, this.F);
        Vec3 vec36 = vec3.d(vec31, this.D);
        Vec3 vec37 = vec3.d(vec31, this.G);

        if (!this.a(vec32)) {
            vec32 = null;
        }

        if (!this.a(vec33)) {
            vec33 = null;
        }

        if (!this.b(vec34)) {
            vec34 = null;
        }

        if (!this.b(vec35)) {
            vec35 = null;
        }

        if (!this.c(vec36)) {
            vec36 = null;
        }

        if (!this.c(vec37)) {
            vec37 = null;
        }

        Vec3 vec38 = null;

        if (vec32 != null && (vec38 == null || vec3.e(vec32) < vec3.e(vec38))) {
            vec38 = vec32;
        }

        if (vec33 != null && (vec38 == null || vec3.e(vec33) < vec3.e(vec38))) {
            vec38 = vec33;
        }

        if (vec34 != null && (vec38 == null || vec3.e(vec34) < vec3.e(vec38))) {
            vec38 = vec34;
        }

        if (vec35 != null && (vec38 == null || vec3.e(vec35) < vec3.e(vec38))) {
            vec38 = vec35;
        }

        if (vec36 != null && (vec38 == null || vec3.e(vec36) < vec3.e(vec38))) {
            vec38 = vec36;
        }

        if (vec37 != null && (vec38 == null || vec3.e(vec37) < vec3.e(vec38))) {
            vec38 = vec37;
        }

        if (vec38 == null) {
            return null;
        }
        else {
            byte b0 = -1;

            if (vec38 == vec32) {
                b0 = 4;
            }

            if (vec38 == vec33) {
                b0 = 5;
            }

            if (vec38 == vec34) {
                b0 = 0;
            }

            if (vec38 == vec35) {
                b0 = 1;
            }

            if (vec38 == vec36) {
                b0 = 2;
            }

            if (vec38 == vec37) {
                b0 = 3;
            }

            return new MovingObjectPosition(i0, i1, i2, b0, vec38.c((double) i0, (double) i1, (double) i2));
        }
    }

    private boolean a(Vec3 vec3) {
        return vec3 == null ? false : vec3.d >= this.C && vec3.d <= this.F && vec3.e >= this.D && vec3.e <= this.G;
    }

    private boolean b(Vec3 vec3) {
        return vec3 == null ? false : vec3.c >= this.B && vec3.c <= this.E && vec3.e >= this.D && vec3.e <= this.G;
    }

    private boolean c(Vec3 vec3) {
        return vec3 == null ? false : vec3.c >= this.B && vec3.c <= this.E && vec3.d >= this.C && vec3.d <= this.F;
    }

    public void a(World world, int i0, int i1, int i2, Explosion explosion) {
    }

    public boolean a(World world, int i0, int i1, int i2, int i3, ItemStack itemstack) {
        return this.d(world, i0, i1, i2, i3);
    }

    public boolean d(World world, int i0, int i1, int i2, int i3) {
        return this.c(world, i0, i1, i2);
    }

    public boolean c(World world, int i0, int i1, int i2) {
        return world.a(i0, i1, i2).J.j();
    }

    public boolean a(World world, int i0, int i1, int i2, EntityPlayer entityplayer, int i3, float f0, float f1, float f2) {
        return false;
    }

    public void b(World world, int i0, int i1, int i2, Entity entity) {
    }

    public int a(World world, int i0, int i1, int i2, int i3, float f0, float f1, float f2, int i4) {
        return i4;
    }

    public void a(World world, int i0, int i1, int i2, EntityPlayer entityplayer) {
    }

    public void a(World world, int i0, int i1, int i2, Entity entity, Vec3 vec3) {
    }

    public void a(IBlockAccess iblockaccess, int i0, int i1, int i2) {
    }

    public final double x() {
        return this.B;
    }

    public final double y() {
        return this.E;
    }

    public final double z() {
        return this.C;
    }

    public final double A() {
        return this.F;
    }

    public final double B() {
        return this.D;
    }

    public final double C() {
        return this.G;
    }

    public int b(IBlockAccess iblockaccess, int i0, int i1, int i2, int i3) {
        return 0;
    }

    public boolean f() {
        return false;
    }

    public void a(World world, int i0, int i1, int i2, Entity entity) {
    }

    public int c(IBlockAccess iblockaccess, int i0, int i1, int i2, int i3) {
        return 0;
    }

    public void g() {
    }

    public void a(World world, EntityPlayer entityplayer, int i0, int i1, int i2, int i3) {
        entityplayer.a(StatList.C[b(this)], 1);
        entityplayer.a(0.025F);
        if (this.E() && EnchantmentHelper.e(entityplayer)) {
            ItemStack itemstack = this.j(i3);

            if (itemstack != null) {
                this.a(world, i0, i1, i2, itemstack);
            }
        }
        else {
            int i4 = EnchantmentHelper.f(entityplayer);

            this.b(world, i0, i1, i2, i3, i4);
        }
    }

    protected boolean E() {
        return this.d() && !this.A;
    }

    protected ItemStack j(int i0) {
        int i1 = 0;
        Item item = Item.a(this);

        if (item != null && item.n()) {
            i1 = i0;
        }

        return new ItemStack(item, 1, i1);
    }

    public int a(int i0, Random random) {
        return this.a(random);
    }

    public boolean j(World world, int i0, int i1, int i2) {
        return true;
    }

    public void a(World world, int i0, int i1, int i2, EntityLivingBase entitylivingbase, ItemStack itemstack) {
    }

    public void e(World world, int i0, int i1, int i2, int i3) {
    }

    public Block c(String s0) {
        this.b = s0;
        return this;
    }

    public String F() {
        return StatCollector.a(this.a() + ".name");
    }

    public String a() {
        return "tile." + this.b;
    }

    public boolean a(World world, int i0, int i1, int i2, int i3, int i4) {
        return false;
    }

    public boolean G() {
        return this.y;
    }

    protected Block H() {
        this.y = false;
        return this;
    }

    public int h() {
        return this.J.m();
    }

    public void a(World world, int i0, int i1, int i2, Entity entity, float f0) {
    }

    public int k(World world, int i0, int i1, int i2) {
        return this.a(world.e(i0, i1, i2));
    }

    public Block a(CreativeTabs creativetabs) {
        this.a = creativetabs;
        return this;
    }

    public void a(World world, int i0, int i1, int i2, int i3, EntityPlayer entityplayer) {
    }

    public void f(World world, int i0, int i1, int i2, int i3) {
    }

    public void l(World world, int i0, int i1, int i2) {
    }

    public boolean L() {
        return true;
    }

    public boolean a(Explosion explosion) {
        return true;
    }

    public boolean c(Block block) {
        return this == block;
    }

    public static boolean a(Block block, Block block1) {
        return block != null && block1 != null ? (block == block1 ? true : block.c(block1)) : false;
    }

    public boolean M() {
        return false;
    }

    public int g(World world, int i0, int i1, int i2, int i3) {
        return 0;
    }

    protected Block d(String s0) {
        this.d = s0;
        return this;
    }

    public static class SoundType {

        public final String a;
        public final float b;
        public final float c;

        public SoundType(String s0, float f0, float f1) {
            this.a = s0;
            this.b = f0;
            this.c = f1;
        }

        public float c() {
            return this.b;
        }

        public float d() {
            return this.c;
        }

        public String a() {
            return "dig." + this.a;
        }

        public String e() {
            return "step." + this.a;
        }

        public String b() {
            return this.a();
        }
    }
}

