package com.mordrum.mfish;

import com.mordrum.mfish.api.FishWeighEvent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

class FishingListener implements Listener {

    private final Main plugin;

    //Roll under defines what number on the size roll will determine what size is chosen
    //Range min and mix define the weight range for that size
    private int smallFishRollUnder = 50;
    private double smallFishRangeMin = 1.0;
    private double smallFishRangeMax = 25.0;
    private List<Double> smallFishChanceList;

    private int mediumFishRollUnder = smallFishRollUnder + 30;
    private double mediumFishRangeMin = 25.0;
    private double mediumFishRangeMax = 50.0;
    private List<Double> mediumFishChanceList;

    private int largeFishRollUnder = mediumFishRollUnder + 15;
    private double largeFishRangeMin = 50.0;
    private double largeFishRangeMax = 100.0;
    private List<Double> largeFishChanceList;

    private int hugeFishRollUnder = largeFishRollUnder + 5;
    private double hugeFishRangeMin = 50.0;
    private double hugeFishRangeMax = 100.0;
    private List<Double> hugeFishChanceList;

    public FishingListener(Main instance) {
        plugin = instance;

        int chanceRange = 10;

        smallFishChanceList = new ArrayList<>();
        for (double a = smallFishRangeMax; a > (smallFishRangeMin - 1.0); a--) {
            if (a == smallFishRangeMax) {
                chanceRange = 10;
            } else {
                chanceRange = (int) Math.pow(chanceRange, 1.025);
            }
            for (int b = 0; b < chanceRange; b++) {
                smallFishChanceList.add(a);
            }
        }
        mediumFishChanceList = new ArrayList<>();
        for (double a = mediumFishRangeMax; a > (mediumFishRangeMin - 1.0); a--) {
            if (a == mediumFishRangeMax) {
                chanceRange = 10;
            } else {
                chanceRange = (int) Math.pow(chanceRange, 1.025);
            }
            for (int b = 0; b < chanceRange; b++) {
                mediumFishChanceList.add(a);
            }
        }
        largeFishChanceList = new ArrayList<>();
        for (double a = largeFishRangeMax; a > (largeFishRangeMin - 1.0); a--) {
            if (a == largeFishRangeMax) {
                chanceRange = 10;
            } else {
                chanceRange = (int) Math.pow(chanceRange, 1.025);
            }
            for (int b = 0; b < chanceRange; b++) {
                largeFishChanceList.add(a);
            }
        }
        hugeFishChanceList = new ArrayList<>();
        for (double a = hugeFishRangeMax; a > (hugeFishRangeMin - 1.0); a--) {
            if (a == hugeFishRangeMax) {
                chanceRange = 10;
            } else {
                chanceRange = (int) Math.pow(chanceRange, 1.025);
            }
            for (int b = 0; b < chanceRange; b++) {
                hugeFishChanceList.add(a);
            }
        }
    }

    /*
        Heart of the fishing logic
     */
    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            if (event.getCaught().getType() == EntityType.DROPPED_ITEM) {
                Item item = (Item) event.getCaught();
                final ItemStack itemStack = item.getItemStack();
                if (itemStack.getType() != Material.RAW_FISH) return;
                final Player player = event.getPlayer();
                final String name = player.getName();
                short fishType = itemStack.getDurability();
                String formattedName = "unknown";
                switch (itemStack.getDurability()) {
                    case 0:
                        formattedName = "fish";
                        break;
                    case 1:
                        formattedName = "salmon";
                        break;
                    case 2:
                        formattedName = "clown fish";
                        break;
                    case 3:
                        formattedName = "puffer fish";
                        break;
                }

                double fishWeight = new BigDecimal(GenerateFishWeight(event.getPlayer())).setScale(2, RoundingMode.HALF_EVEN).doubleValue();

                //Call a new FishWeighEvent so that other plugins can interact with mFish
                FishWeighEvent fishWeighEvent = new FishWeighEvent(event.getPlayer(), itemStack.getType(), fishWeight);
                plugin.getServer().getPluginManager().callEvent(fishWeighEvent);

                //If nothing cancelled the event
                if (!event.isCancelled()) {
                    player.sendMessage(String.format("%sYou caught a %s%s%s weighing %s%s%s pounds!",
                            ChatColor.LIGHT_PURPLE, ChatColor.YELLOW, formattedName, ChatColor.LIGHT_PURPLE,
                            ChatColor.RED, fishWeighEvent.getFishWeight(), ChatColor.LIGHT_PURPLE));
                    //Give the player the fish they have earned, minus 1 to account for the fish they already picked up
                    int nonDoubleWeight = (int) fishWeighEvent.getFishWeight();
                    itemStack.setAmount(nonDoubleWeight);
                    plugin.SavePlayerScore(name, formattedName, fishWeighEvent.getFishWeight());
                    UpdateGlobalHighscore(name, formattedName, fishWeighEvent.getFishWeight());
                    plugin.TrackCatch(fishWeighEvent.getFishWeight());
                }
            }
        }
    }

    private double GenerateFishWeight(Player player) {
        Random r = new Random();
        //The upper and lower roll limits, by default nothing larger than a medium fish can be caught
        int upperLimit = mediumFishRollUnder;
        int lowerLimit = 0;

        //Roll modifiers
        World world = player.getWorld();

        if ((world.getTime() > 14500) && (world.getTime() < 22000)) lowerLimit += 15; //A night, smaller fish are rarer
        if (world.hasStorm()) lowerLimit += -5; //Rain attracts fish of all sizes
        if (world.isThundering()) lowerLimit += 30; //Thunder scares little fish, but attracts large fish
        if ((player.isInsideVehicle()) && (player.getVehicle().getType() == EntityType.BOAT))
            lowerLimit += 10;//Boats attract all fish types

        //Biome modifiers are the last determining factor in what fish size can be caught
        Biome playerBiome = world.getBiome((int) player.getLocation().getX(), (int) player.getLocation().getZ());
        if (playerBiome == Biome.BEACH || playerBiome == Biome.RIVER) upperLimit = mediumFishRollUnder;
        else if (playerBiome == Biome.OCEAN) upperLimit = largeFishRollUnder;
        else if (playerBiome == Biome.DEEP_OCEAN) upperLimit = hugeFishRollUnder;
        else upperLimit = smallFishRollUnder;

        //Rolls a number between the lower and upper limit, this will determine our fish size
        int sizeRoll = r.nextInt(upperLimit) + lowerLimit;

        //Sanitize the roll
        if (sizeRoll <= 0) sizeRoll = 1;
        else if (sizeRoll > upperLimit) sizeRoll = upperLimit;

        //This next bit calibrates the fish weight to the size
        List<Double> oddsList;

        if (sizeRoll <= smallFishRollUnder) {
            oddsList = smallFishChanceList;
        } else if (sizeRoll <= mediumFishRollUnder) {
            oddsList = mediumFishChanceList;
        } else if (sizeRoll <= largeFishRollUnder) {
            oddsList = largeFishChanceList;
        } else if (sizeRoll <= hugeFishRollUnder) {
            oddsList = hugeFishChanceList;
        } else {
            plugin.getLogger().info("Somehow, a sizeroll occurred that exceeded the rollunders for all fish sizes");
            return 0.0;
        }

        //The core weight of the fish, before modifiers
        double coreWeight = oddsList.get(r.nextInt(oddsList.size()));
        //Gives us a number between 0.1 and 0.9, we will add this to our core weight
        double trailingWeight = 0.1 + (0.9 - 0.1) * r.nextDouble();

        return (coreWeight + trailingWeight);
    }

    private boolean UpdateGlobalHighscore(String player, String category, Double score) {
        Map resultMap = this.plugin.GetGlobalHighScore(category);
        if (resultMap == null) {
            plugin.SaveGlobalHighScore(category, player, score);
            return true;
        } else {
            if ((double) resultMap.get("score") < score) {
                plugin.getServer().broadcastMessage(String.format("%s%s%s has caught a %s%s%s weighing in at %s%s pounds%s, smashing " +
                        "%s%s's%s previous record of %s%s pounds%s!",
                        ChatColor.AQUA, player, ChatColor.LIGHT_PURPLE, ChatColor.YELLOW, category, ChatColor.LIGHT_PURPLE,
                        ChatColor.RED, score, ChatColor.LIGHT_PURPLE, ChatColor.AQUA, resultMap.get("player"), ChatColor.LIGHT_PURPLE,
                        ChatColor.RED, resultMap.get("score"), ChatColor.LIGHT_PURPLE));
                plugin.SaveGlobalHighScore(category, player, score);
                return true;
            }
        }
        return false;
    }
}
