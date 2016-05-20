package me.ChrizC.blockdamage;
 
import java.io.*;

import org.bukkit.util.config.Configuration;
 
public class BDConfigHandler {
    private final BlockDamage plugin;
    
    String fileName;
    Configuration file;
    
    int damageDealt;
    int damageDelay;
    
    boolean armorMod;
    boolean message;
    boolean verbose;
    
    String painMessage;
    String blockID;
    String protectedWorlds;

    public BDConfigHandler(BlockDamage instance) {
        plugin = instance;
        
    }
    public void doConfig() {
        file = new Configuration(new File(plugin.getDataFolder(), "config.yml"));
        file.load();
        if (new File(plugin.getDataFolder(), "config.yml").exists()) {
            System.out.println("[BlockDamage] Configuration file loaded!");
        } else {
            file.setProperty("damageDealt", 1);
            file.setProperty("damageDelay", 1);
            file.setProperty("protectedWorlds", "none");
            file.setProperty("blockID", "87");
            file.setProperty("armorModification", true);
            file.setProperty("messageOnDamage", false);	
            file.setProperty("painMessage", "Be careful! This block is hurting you.");	
            file.setProperty("verbose", false);
            file.save();
            System.out.println("[BlockDamage] Configuration file created with default values!");
        }
        
        //Get configs
        damageDealt = file.getInt("damageDealt", 1);
        damageDelay = file.getInt("damageDelay", 1);
        
        armorMod = file.getBoolean("armorModification", true);
        message = file.getBoolean("messageOnDamage", false);
        verbose = file.getBoolean("verbose", false);
        
        painMessage = file.getString("painMessage", "Be careful! This block is hurting you.");
        blockID = file.getString("blockID", "87");
        protectedWorlds = file.getString("protectedWorlds", "none");
        
    }
    public void relConfig() {
        file = new Configuration(new File(plugin.getDataFolder(), "config.yml"));
        file.load();

        //Get configs
        damageDealt = file.getInt("damageDealt", 1);
        damageDelay = file.getInt("damageDelay", 1);
        
        armorMod = file.getBoolean("armorModification", true);
        message = file.getBoolean("messageOnDamage", false);
        verbose = file.getBoolean("verbose", false);
        
        painMessage = file.getString("painMessage", "Be careful! This block is hurting you.");
        blockID = file.getString("blockID", "87");
        protectedWorlds = file.getString("protectedWorlds", "none");
    }
    
    public boolean changeInt(String option, int newValue) {
        if (option.equalsIgnoreCase("damageDealt")) {
            if (newValue > 0) {
                file = new Configuration(new File(plugin.getDataFolder(), "config.yml"));
                file.load();
                file.setProperty("damageDealt", newValue);
                file.save();
                return true;
            }
        } else if (option.equalsIgnoreCase("damageDelay")) {
            if (newValue > 0) {
                file = new Configuration(new File(plugin.getDataFolder(), "config.yml"));
                file.load();
                file.setProperty("damageDelay", newValue);
                file.save();
                return true;
            }
        }
        
        return false;
    }
    
    public boolean changeString(String option, String newValue) {
        if (option.equalsIgnoreCase("message")) {
            file = new Configuration(new File(plugin.getDataFolder(), "config.yml"));
            file.load();
            file.setProperty("painMessage", newValue);
            file.save();
            return true;
        } else if (option.equalsIgnoreCase("blocks")) {
            file = new Configuration(new File(plugin.getDataFolder(), "config.yml"));
            file.load();
            file.setProperty("blockID", newValue);
            file.save();
            return true;
        } else if (option.equalsIgnoreCase("protected")) {
            file = new Configuration(new File(plugin.getDataFolder(), "config.yml"));
            file.load();
            file.setProperty("protectedWorlds", newValue);
            file.save();
            return true;
        }
        
        return false;
    }
    
    public boolean changeBool(String option, boolean newValue) {
        if (option.equalsIgnoreCase("verbose")) {
            file = new Configuration(new File(plugin.getDataFolder(), "config.yml"));
            file.load();
            file.setProperty("verbose", newValue);
            file.save();
            return true;
        } else if (option.equalsIgnoreCase("message")) {
            file = new Configuration(new File(plugin.getDataFolder(), "config.yml"));
            file.load();
            file.setProperty("messageOnDamage", newValue);
            file.save();
            return true;
        } else if (option.equalsIgnoreCase("armor")) {
            file = new Configuration(new File(plugin.getDataFolder(), "config.yml"));
            file.load();
            file.setProperty("armorMod", newValue);
            file.save();
            return true;
        }
        
        return false;
    }
    
}

