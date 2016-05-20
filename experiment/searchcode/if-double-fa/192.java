package com.miraclem4n.paycommands;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("unused")
public class API {
    PayCommands plugin;

    public API(PayCommands plugin) {
        this.plugin = plugin;
    }
    
    public CreatureSpawner getSpawner(Player player) {
        Block tBlock = player.getTargetBlock(null, 30);

        if (tBlock.getTypeId() == 52)
            return (CreatureSpawner)tBlock.getState();

        return null;
    }
    
    public Boolean isChangeable(Player player, CreatureSpawner spawner, Double saleCost, String node) {
        if (hasPerm(player, node))
            return true;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "spawners.yml"));

        Location loc = spawner.getBlock().getLocation();

        Double x = loc.getX();
        Double y = loc.getY();
        Double z = loc.getZ();


        return config.isSet(x.intValue() + "|" + y.intValue() + "|" + z.intValue() + "|" + saleCost.intValue())
                && config.getString(x.intValue() + "|" + y.intValue() + "|" + z.intValue() + "|" + saleCost.intValue()).equals(player.getName());

    }

    public void setOwnership(String player, CreatureSpawner spawner, Double saleCost) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "spawners.yml"));

        Location loc = spawner.getBlock().getLocation();

        Double x = loc.getX();
        Double y = loc.getY();
        Double z = loc.getZ();

        config.set(x.intValue() + "|" + y.intValue() + "|" + z.intValue() + "|" + saleCost.intValue(), player);

        try {
            config.save(new File(plugin.getDataFolder(), "spawners.yml"));
        } catch (IOException ignored) {}
    }

    public Double getSaleCost(CreatureSpawner spawner) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "spawners.yml"));

        Location loc = spawner.getBlock().getLocation();

        Double x = loc.getX();
        Double y = loc.getY();
        Double z = loc.getZ();

        for (String key : config.getKeys(false))
            if (key.contains(x.intValue() + "|" + y.intValue() + "|" + z.intValue() + "|"))
                return Double.valueOf(key.replace(x.intValue() + "|" + y.intValue() + "|" + z.intValue() + "|", ""));

        return 0.0;
    }
    
    Boolean hasMoney(String player, Double money) {
        return (!plugin.useEcon || plugin.econ.getBalance(player) >= money);
    }

    public Boolean removeMoney(String player, Double money) {
        return (!plugin.useEcon || (hasMoney(player, money)
                && (plugin.econ.withdrawPlayer(player, money).type == EconomyResponse.ResponseType.SUCCESS)));
    }

    public Boolean giveMoney(String player, Double money) {
        return (!plugin.useEcon || plugin.econ.depositPlayer(player, money).type == EconomyResponse.ResponseType.SUCCESS);
    }

    public String addColour(String string) {
        string = string.replace("`e", "")
                .replace("`r", "\u00A7c")           .replace("`R", "\u00A74")
                .replace("`y", "\u00A7e")           .replace("`Y", "\u00A76")
                .replace("`g", "\u00A7a")           .replace("`G", "\u00A72")
                .replace("`a", "\u00A7b")           .replace("`A", "\u00A73")
                .replace("`b", "\u00A79")           .replace("`B", "\u00A71")
                .replace("`p", "\u00A7d")           .replace("`P", "\u00A75")
                .replace("`k", "\u00A70")           .replace("`s", "\u00A77")
                .replace("`S", "\u00A78")           .replace("`w", "\u00A7f");

        string = string.replace("<r>", "")
                .replace("<black>", "\u00A70")      .replace("<navy>", "\u00A71")
                .replace("<green>", "\u00A72")      .replace("<teal>", "\u00A73")
                .replace("<red>", "\u00A74")        .replace("<purple>", "\u00A75")
                .replace("<gold>", "\u00A76")       .replace("<silver>", "\u00A77")
                .replace("<gray>", "\u00A78")       .replace("<blue>", "\u00A79")
                .replace("<lime>", "\u00A7a")       .replace("<aqua>", "\u00A7b")
                .replace("<rose>", "\u00A7c")       .replace("<pink>", "\u00A7d")
                .replace("<yellow>", "\u00A7e")     .replace("<white>", "\u00A7f");

        string = string.replaceAll("(??([a-fA-F0-9]))", "\u00A7$2");

        string = string.replaceAll("(&([a-fA-F0-9]))", "\u00A7$2");

        return string.replace("&&", "&");
    }

    public void log(Object object) {
        System.out.println(object);
    }
    
    public Boolean hasPerm(CommandSender sender, String node) {
        return plugin.vaultB && plugin.perm != null && plugin.perm.has(sender, node) || sender.hasPermission(node);
    }
}

