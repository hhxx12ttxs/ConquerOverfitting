/**
 * 
 * Copyright 2011 MilkBowl (https://github.com/MilkBowl)
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 */
package net.milkbowl.localshops;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.milkbowl.localshops.objects.GlobalShop;
import net.milkbowl.localshops.objects.Item;
import net.milkbowl.localshops.objects.ShopRecord;
import net.milkbowl.localshops.objects.ItemInfo;
import net.milkbowl.localshops.objects.LocalShop;
import net.milkbowl.localshops.objects.Shop;
import net.milkbowl.localshops.objects.ShopLocation;
import net.milkbowl.localshops.objects.ShopSign;
import net.milkbowl.localshops.util.GenericFunctions;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class ShopManager {

    private LocalShops plugin;
    private Map<UUID, Shop> shops = Collections.synchronizedMap(new HashMap<UUID, Shop>());
    private Map<String, Set<UUID>> localShops = Collections.synchronizedMap(new HashMap<String, Set<UUID>>());
    private Map<String, UUID> worldShops = Collections.synchronizedMap(new HashMap<String, UUID>());
    // Logging
    private static final Logger log = Logger.getLogger("Minecraft");

    public ShopManager(LocalShops plugin) {
        this.plugin = plugin;
    }

    public Shop getLocalShop(UUID uuid) {
        return shops.get(uuid);
    }

    /**
     * Attempts to match any shop from a partial UUID
     * 
     * @param partialUuid
     * @return shop object if found, otherwise null
     */
    public Shop getShop(String partialUuid) {
        Iterator<Shop> it = shops.values().iterator();
        while (it.hasNext()) {
            Shop cShop = it.next();
            if (cShop.getUuid().toString().matches(".*" + partialUuid.toLowerCase() + "$")) {
                return cShop;
            }
        }

        return null;
    }

    /**
     * Attempts to find a local shop from a partial uuid
     * 
     * 
     * @param partialUuid
     * @return shop if the shop was found, or null if it was not found in the
     *         map
     */
    public LocalShop getLocalShop(String partialUuid) {
        Iterator<Shop> it = shops.values().iterator();
        while (it.hasNext()) {
            Shop cShop = it.next();
            if (cShop instanceof LocalShop) {
                LocalShop lShop = (LocalShop) cShop;
                if (cShop.getUuid().toString().matches(".*" + partialUuid.toLowerCase() + "$")) {
                    return lShop;
                }
            }
        }

        return null;
    }

    /**
     * Attempts to find a global shop from a partial uuid
     * 
     * 
     * @param partialUuid
     * @return shop object if the shop was found, or null if it was not found in
     *         the map
     */
    public GlobalShop getGlobalShop(String partialUuid) {
        Iterator<Shop> it = shops.values().iterator();
        while (it.hasNext()) {
            Shop cShop = it.next();
            if (cShop instanceof GlobalShop) {
                GlobalShop gShop = (GlobalShop) cShop;
                if (cShop.getUuid().toString().matches(".*" + partialUuid.toLowerCase() + "$")) {
                    return gShop;
                }
            }
        }

        return null;
    }

    public GlobalShop getGlobalShopByWorld(String worldName) {
        UUID uuid = worldShops.get(worldName);
        Shop shop = shops.get(uuid);
        if (shop instanceof GlobalShop) {
            log.info("Return GS");
            return (GlobalShop) shop;
        } else {
            return null;
        }
    }

    public void removeGlobalShopByWorld(String worldName) {
        worldShops.remove(worldName);
    }

    public GlobalShop getGlobalShop(World world) {
        return getGlobalShopByWorld(world.getName());
    }

    public LocalShop getLocalShop(Location loc) {
        return getLocalShop(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public LocalShop getLocalShop(String world, int x, int y, int z) {
        // Null check on the map before trying to check for shops
        if (localShops.containsKey(world)) {
            for (UUID uuid : localShops.get(world)) {
                Shop shop = shops.get(uuid);
                if (shop instanceof LocalShop) {
                    LocalShop lShop = (LocalShop) shop;

                    if (lShop.containsPoint(world, x, y, z)) {
                        return lShop;
                    }
                }
            }
        }
        return null;
    }

    public boolean shopPositionOk(int[] xyzA, int[] xyzB, String worldName) {
        // make sure coords are in right order
        for (int i = 0; i < 3; i++) {
            if (xyzA[i] > xyzB[i]) {
                int temp = xyzA[i];
                xyzA[i] = xyzB[i];
                xyzB[i] = temp;
            }
        }

        for (Shop shop : shops.values()) {
            // ignore global shops
            if (shop instanceof GlobalShop) {
                continue;
            }

            LocalShop lShop = (LocalShop) shop;
            // ignore shops on different worlds
            if (!lShop.getWorld().equals(worldName)) {
                continue;
            }

            for (ShopLocation sLoc : lShop.getShopLocations()) {
                /**
                 * For each Val is one of x,y,z
                 * 
                 * If minVal is greater than shop maxVal or maxVal is less than
                 * shop minVal these shops never converge, so skip to another
                 * location.
                 * 
                 * If all non-convergence checks are false, then must Converge
                 * on all 3 planes on at least one point.
                 * 
                 */
                if (xyzA[1] > sLoc.getLocation2()[1] || xyzB[1] < sLoc.getLocation1()[1]) {
                    continue;
                } else if (xyzA[0] > sLoc.getLocation2()[0] || xyzB[0] < sLoc.getLocation1()[0]) {
                    continue;
                } else if (xyzA[2] > sLoc.getLocation2()[2] || xyzB[2] < sLoc.getLocation1()[2]) {
                    continue;
                } // If All three checks are false, this cube converges on all 3
                // planes
                else {
                    return false;
                }
            }
        }

        return true;
    }

    public void addShop(Shop shop) {
        if (Config.getSrvDebug()) {
            log.info(String.format("[%s] Adding %s", plugin.getDescription().getName(), shop.toString()));
        }
        String uuid = shop.getUuid().toString();
        while (true) {
            if (Config.uuidListContains(uuid.substring(uuid.length() - Config.getUuidMinLength()))) {
                calcShortUuidSize();
            } else {
                Config.addUuidList(uuid.substring(uuid.length() - Config.getUuidMinLength()));
                break;
            }
        }

        if (shop instanceof GlobalShop) {
            for (String world : ((GlobalShop) shop).getWorlds()) {
                if (worldShops.containsKey(world)) {
                    // Warning
                    log.warning(String.format("[%s] Warning, Global Shop already exists for World \"%s\"!", plugin.getDescription().getName(), world));
                } else {
                    // Add to map
                    worldShops.put(world, shop.getUuid());
                }
            }
        } else if (shop instanceof LocalShop) {
            String world = ((LocalShop) shop).getWorld();
            if (localShops.containsKey(world)) {
                // World already has a shop, lets add to it!
                localShops.get(world).add(shop.getUuid());
            } else {
                Set<UUID> u = Collections.synchronizedSet(new HashSet<UUID>());
                u.add(shop.getUuid());
                localShops.put(world, u);
            }
        }
        shops.put(shop.getUuid(), shop);
    }

    private void calcShortUuidSize() {
        if (Config.getUuidMinLength() < 36) {
            Config.incrementUuidMinLength();
        }
        Config.clearUuidList();
        Iterator<Shop> it = shops.values().iterator();
        while (it.hasNext()) {
            Shop cShop = it.next();
            String cUuid = cShop.getUuid().toString();
            String sUuid = cUuid.substring(cUuid.length() - Config.getUuidMinLength());
            if (Config.uuidListContains(sUuid)) {
                calcShortUuidSize();
            } else {
                Config.addUuidList(sUuid);
            }
        }
    }

    public List<Shop> getAllShops() {
        return new ArrayList<Shop>(shops.values());
    }

    public int getNumShops() {
        return shops.size();
    }

    public int numOwnedShops(String playerName) {
        int numShops = 0;
        for (Shop shop : shops.values()) {
            if (shop.getOwner().equalsIgnoreCase(playerName)) {
                numShops++;
            }
        }
        return numShops;
    }

    public void loadShops(File shopsDir) {
        if (Config.getSrvDebug()) {
            log.info(String.format("[%s] %s.%s", plugin.getDescription().getName(), "ShopData", "loadShops(File shopsDir)"));
        }

        File[] shopsList = shopsDir.listFiles();
        for (File file : shopsList) {

            if (Config.getSrvDebug()) {
                log.info(String.format("[%s] Loading Shop file \"%s\".", plugin.getDescription().getName(), file.toString()));
            }
            Shop shop = null;

            // Determine if filename is a UUID or not
            if (file.getName().matches("^(\\{{0,1}([0-9a-fA-F]){8}-([0-9a-fA-F]){4}-([0-9a-fA-F]){4}-([0-9a-fA-F]){4}-([0-9a-fA-F]){12}\\}{0,1})\\.shop$")) {
                try {
                    shop = loadShop(file);
                } catch (Exception e) {
                    // log error
                    log.log(Level.WARNING, String.format("[%s] Error loading Shop file \"%s\", ignored.", plugin.getDescription().getName(), file.toString()), e);
                }
            } else {
                // Convert old format & delete the file...immediately save using
                // the new format (will generate a new UUID for this shop)
                shop = convertShopOldFormat(file);
            }

            // Check if not null, and add to world
            if (shop != null) {
                if (Config.getSrvDebug()) {
                    log.info(String.format("[%s] Loaded %s", plugin.getDescription().getName(), shop.toString()));
                }
                plugin.getShopManager().addShop(shop);
            } else {
                log.warning(String.format("[%s] Failed to load Shop file: \"%s\"", plugin.getDescription().getName(), file.getName()));
            }
        }

    }

    public Shop convertShopOldFormat(File file) {
        if (Config.getSrvDebug()) {
            log.info(String.format("[%s] %s.%s", plugin.getDescription().getName(), "ShopData", "loadShopOldFormat(File file)"));
        }

        try {
            // Create new empty shop (this format has no UUID, so generate one)
            LocalShop shop = new LocalShop(UUID.randomUUID());

            // Retrieve Shop Name (from filename)
            shop.setName(file.getName().split("\\.")[0]);

            // Open file & iterate over lines
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            int x1 = 0, x2 = 0, y1 = 0, y2 = 0, z1 = 0, z2 = 0;
            while (line != null) {

                if (Config.getSrvDebug()) {
                    log.info(String.format("[%s] %s", plugin.getDescription().getName(), line));
                }

                // Skip comment lines / metadata
                if (line.startsWith("#")) {
                    line = br.readLine();
                    continue;
                }

                // Data is separated by =
                String[] cols = line.split("=");

                // Check if there are enough columns (needs key and value)
                if (cols.length < 2) {
                    line = br.readLine();
                    continue;
                }

                if (cols[0].equalsIgnoreCase("world")) { // World
                    shop.setWorld(cols[1]);
                } else if (cols[0].equalsIgnoreCase("owner")) { // Owner
                    shop.setOwner(cols[1]);
                } else if (cols[0].equalsIgnoreCase("managers")) { // Managers
                    String[] managers = cols[1].split(",");
                    shop.setManagers(managers);
                } else if (cols[0].equalsIgnoreCase("creator")) { // Creator
                    shop.setCreator(cols[1]);
                } else if (cols[0].equalsIgnoreCase("position1")) { // Position
                    // A
                    String[] xyzStr = cols[1].split(",");
                    try {
                        x1 = Integer.parseInt(xyzStr[0].trim());
                        y1 = Integer.parseInt(xyzStr[1].trim());
                        z1 = Integer.parseInt(xyzStr[2].trim());

                    } catch (NumberFormatException e) {
                        if (isolateBrokenShopFile(file)) {
                            log.warning(String.format("[%s] Shop File \"%s\" has bad Location Data, Moving to \"" + Config.getDirShopsBrokenPath() + "\"", plugin.getDescription().getName(), file.toString()));
                        } else {
                            log.warning(String.format("[%s] Shop File \"%s\" has bad Location Data, Error moving to \"" + Config.getDirShopsBrokenPath() + "\"", plugin.getDescription().getName(), file.toString()));
                        }
                        return null;
                    }
                } else if (cols[0].equalsIgnoreCase("position2")) { // Position
                    // B
                    String[] xyzStr = cols[1].split(",");
                    try {
                        x2 = Integer.parseInt(xyzStr[0].trim());
                        y2 = Integer.parseInt(xyzStr[1].trim());
                        z2 = Integer.parseInt(xyzStr[2].trim());
                    } catch (NumberFormatException e) {
                        if (isolateBrokenShopFile(file)) {
                            log.warning(String.format("[%s] Shop File \"%s\" has bad Location Data, Moving to \"" + Config.getDirShopsBrokenPath() + "\"", plugin.getDescription().getName(), file.toString()));
                        } else {
                            log.warning(String.format("[%s] Shop File \"%s\" has bad Location Data, Error moving to \"" + Config.getDirShopsBrokenPath() + "\"", plugin.getDescription().getName(), file.toString()));
                        }
                        return null;
                    }
                } else if (cols[0].equalsIgnoreCase("unlimited-money")) { // Unlimited
                    // Money
                    shop.setUnlimitedMoney(Boolean.parseBoolean(cols[1]));
                } else if (cols[0].equalsIgnoreCase("unlimited-stock")) { // Unlimited
                    // Stock
                    shop.setUnlimitedStock(Boolean.parseBoolean(cols[1]));
                } else if (cols[0].matches("\\d+:\\d+")) { // Items
                    String[] itemInfo = cols[0].split(":");
                    if (itemInfo.length < 2) {
                        if (isolateBrokenShopFile(file)) {
                            log.warning(String.format("[%s] Shop File \"%s\" has bad Item Data, Moving to \"" + Config.getDirShopsBrokenPath() + "\"", plugin.getDescription().getName(), file.toString()));
                        } else {
                            log.warning(String.format("[%s] Shop File \"%s\" has bad Item Data, Error moving to \"" + Config.getDirShopsBrokenPath() + "\"", plugin.getDescription().getName(), file.toString()));
                        }
                        return null;
                    }
                    int itemId = Integer.parseInt(itemInfo[0]);
                    short damageMod = Short.parseShort(itemInfo[1]);

                    String[] dataCols = cols[1].split(",");
                    if (dataCols.length < 3) {
                        if (isolateBrokenShopFile(file)) {
                            log.warning(String.format("[%s] Shop File \"%s\" has bad Item Data, Moving to \"" + Config.getDirShopsBrokenPath() + "\"", plugin.getDescription().getName(), file.toString()));
                        } else {
                            log.warning(String.format("[%s] Shop File \"%s\" has bad Item Data, Error moving to \"" + Config.getDirShopsBrokenPath() + "\"", plugin.getDescription().getName(), file.toString()));
                        }
                        return null;
                    }

                    String[] buyInfo = dataCols[0].split(":");
                    if (buyInfo.length < 2) {
                        if (isolateBrokenShopFile(file)) {
                            log.warning(String.format("[%s] Shop File \"%s\" has bad Item Data, Moving to \"" + Config.getDirShopsBrokenPath() + "\"", plugin.getDescription().getName(), file.toString()));
                        } else {
                            log.warning(String.format("[%s] Shop File \"%s\" has bad Item Data, Error moving to \"" + Config.getDirShopsBrokenPath() + "\"", plugin.getDescription().getName(), file.toString()));
                        }
                        return null;
                    }
                    int buyPrice = Integer.parseInt(buyInfo[0]);

                    String[] sellInfo = dataCols[1].split(":");
                    if (sellInfo.length < 2) {
                        if (isolateBrokenShopFile(file)) {
                            log.warning(String.format("[%s] Shop File \"%s\" has bad Item Data, Moving to \"" + Config.getDirShopsBrokenPath() + "\"", plugin.getDescription().getName(), file.toString()));
                        } else {
                            log.warning(String.format("[%s] Shop File \"%s\" has bad Item Data, Error moving to \"" + Config.getDirShopsBrokenPath() + "\"", plugin.getDescription().getName(), file.toString()));
                        }
                        return null;
                    }
                    int sellPrice = Integer.parseInt(sellInfo[0]);

                    String[] stockInfo = dataCols[2].split(":");
                    if (stockInfo.length < 2) {
                        if (isolateBrokenShopFile(file)) {
                            log.warning(String.format("[%s] Shop File \"%s\" has bad Item Data, Moving to \"" + Config.getDirShopsBrokenPath() + "\"", plugin.getDescription().getName(), file.toString()));
                        } else {
                            log.warning(String.format("[%s] Shop File \"%s\" has bad Item Data, Error moving to \"" + Config.getDirShopsBrokenPath() + "\"", plugin.getDescription().getName(), file.toString()));
                        }
                        return null;
                    }
                    int stock = Integer.parseInt(stockInfo[0]);
                    int maxStock = Integer.parseInt(stockInfo[1]);

                    if (!shop.addItem(itemId, damageMod, buyPrice, sellPrice, stock, maxStock)) {
                        if (isolateBrokenShopFile(file)) {
                            log.warning(String.format("[%s] Shop File \"%s\" has bad Item Data (%d:%d), Moving to \"" + Config.getDirShopsBrokenPath() + "\"", plugin.getDescription().getName(), file.toString(), itemId, damageMod));
                        } else {
                            log.warning(String.format("[%s] Shop File \"%s\" has bad Item Data (%d:%d), Error moving to \"" + Config.getDirShopsBrokenPath() + "\"", plugin.getDescription().getName(), file.toString(), itemId, damageMod));
                        }
                        return null;
                    }
                } else { // Not defined
                    log.info(String.format("[%s] Shop File \"%s\" has undefined data, ignoring.", plugin.getDescription().getName(), file.toString()));
                }
                line = br.readLine();
            }
            shop.getShopLocations().add(new ShopLocation(x1, y1, z1, x2, y2, z2));
            br.close();

            File dir = new File(Config.getDirShopsConvertedPath());
            if(dir.mkdir()) {
                log.log(Level.INFO, "Created directory ''{0}''", dir.getName());
            }
            if (file.renameTo(new File(dir, file.getName()))) {
                if(!file.delete()) {
                    log.log(Level.WARNING, "Failed to delete ''{0}'' during migration.", file.getName());
                }
                return shop;
            } else {
                return null;
            }

        } catch (IOException e) {
            if (isolateBrokenShopFile(file)) {
                log.warning(String.format("[%s] Shop File \"%s\" Exception: %s, Moving to \"" + Config.getDirShopsBrokenPath() + "\"", plugin.getDescription().getName(), file.toString(), e.toString()));
            } else {
                log.warning(String.format("[%s] Shop File \"%s\" Exception: %s, Error moving to \"" + Config.getDirShopsBrokenPath() + "\"", plugin.getDescription().getName(), file.toString(), e.toString()));
            }
            return null;
        }
    }

    public static double[] convertStringArraytoDoubleArray(String[] sarray) {
        if (sarray != null) {
            double longArray[] = new double[sarray.length];
            for (int i = 0; i < sarray.length; i++) {
                longArray[i] = Long.parseLong(sarray[i]);
            }
            return longArray;
        }
        return null;
    }

    public static int[] convertStringArraytoIntArray(String[] sarray) {
        if (sarray != null) {
            int intArray[] = new int[sarray.length];
            for (int i = 0; i < sarray.length; i++) {
                intArray[i] = Integer.parseInt(sarray[i]);
            }
            return intArray;
        }
        return null;
    }

    public Shop loadShop(File file) throws Exception {
        List<ShopSign> signList = new ArrayList<ShopSign>(4);

        Shop shop = null;
        int[] locA = null;
        int[] locB = null;

        SortedProperties props = new SortedProperties();
        try {
            props.load(new FileInputStream(file));
        } catch (IOException e) {
            log.warning(String.format("[%s] %s", plugin.getDescription().getName(), "IOException: " + e.getMessage()));
            return null;
        }

        // Shop attributes
        UUID uuid = UUID.fromString(props.getProperty("uuid", "00000000-0000-0000-0000-000000000000"));
        String name = props.getProperty("name", "Nameless Shop");
        boolean unlimitedMoney = Boolean.parseBoolean(props.getProperty("unlimited-money", "false"));
        boolean unlimitedStock = Boolean.parseBoolean(props.getProperty("unlimited-stock", "false"));
        double minBalance = Double.parseDouble((props.getProperty("min-balance", "0.0")));
        boolean notification = Boolean.parseBoolean(props.getProperty("notification", "true"));
        boolean global = Boolean.parseBoolean(props.getProperty("global", "false"));
        boolean dynamic = Boolean.parseBoolean(props.getProperty("dynamic-prices", "false"));
        double sharePercent;
        try {
            sharePercent = Double.parseDouble(props.getProperty("share-percent", "0"));
            if (sharePercent < 0) {
                sharePercent = 0;
            } else if (sharePercent > 100) {
                sharePercent = 100;
            }
        } catch (NumberFormatException e) {
            sharePercent = 0;
        }

        // People
        String owner = props.getProperty("owner", "");
        String[] managers = props.getProperty("managers", "").replaceAll("[\\[\\]]", "").split(", ");
        String creator = props.getProperty("creator", "LocalShops");

        // Users and Groups
        String[] groups = props.getProperty("groups", "").replaceAll("[\\[\\]]", "").split(", ");
        String[] users = props.getProperty("users", "").replaceAll("[\\[\\]]", "").split(", ");

        // Construct our shop object
        if (global) {
            GlobalShop gShop = new GlobalShop(uuid);
            String worlds = props.getProperty("worlds");
            for (String worldName : worlds.split(", ")) {
                if (!worldName.equals("")) {
                    gShop.addWorld(worldName);
                }
            }
            shop = gShop;
        } else {
            LocalShop lShop = new LocalShop(uuid);
            String world = props.getProperty("world");
            lShop.setWorld(world);
            shop = lShop;
        }

        // Convert old Location data
        if (props.getProperty("config-version").equals("2.0")) {
            if (shop instanceof LocalShop) {
                log.info("[LocalShops] - Converting v2.0 shop location data to new v3.0 format.");
                try {
                    locA = convertStringArraytoIntArray(props.getProperty("locationA").split(", "));
                    locB = convertStringArraytoIntArray(props.getProperty("locationB").split(", "));
                    // Try to convert to new Location data
                    ((LocalShop) shop).getShopLocations().add(new ShopLocation(locA[0], locA[1], locA[2], locB[0], locB[1], locB[2]));
                } catch (Exception e) {
                    if (isolateBrokenShopFile(file)) {
                        log.warning(String.format("[%s] Shop File \"%s\" has bad Location Data, Moving to \"" + Config.getDirShopsBrokenPath() + "\"", plugin.getDescription().getName(), file.toString()));
                    } else {
                        log.warning(String.format("[%s] Shop File \"%s\" has bad Location Data, Error moving to \"" + Config.getDirShopsBrokenPath() + "\"", plugin.getDescription().getName(), file.toString()));
                    }
                    return null;
                }
            }
        }
        // Add necessary shop data.that is for all shop types
        shop.setName(name);
        shop.setUnlimitedMoney(unlimitedMoney);
        shop.setUnlimitedStock(unlimitedStock);
        shop.setOwner(owner);
        shop.setManagers(managers);
        shop.setCreator(creator);
        shop.setNotification(notification);
        shop.setDynamicPrices(dynamic);
        shop.setSharePercent(sharePercent);

        // Only set our Users & Groups if they are not empty strings
        for (String group : groups) {
            if (!group.equals("")) {
                shop.addGroup(group);
            }
        }
        for (String user : users) {
            if (!user.equals("")) {
                shop.addUser(user);
            }
        }

        // Make sure minimum balance isn't negative
        if (minBalance < 0) {
            shop.setMinBalance(0);
        } else {
            shop.setMinBalance(minBalance);
        }

        // Iterate through all keys, find items & parse
        // props.setProperty(String.format("%d:%d", info.typeId,
        // info.subTypeId), String.format("%d:%d,%d:%d,%d:%d", buyPrice,
        // buySize, sellPrice, sellSize, stock, maxStock));
        Iterator<Object> it = props.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            if (key.matches("\\d+:\\d+")) {
                String[] k = key.split(":");
                int id = Integer.parseInt(k[0]);
                short type = Short.parseShort(k[1]);

                String value = props.getProperty(key);
                String[] v = value.split(",");

                String[] buy = v[0].split(":");
                double buyPrice = Double.parseDouble(buy[0]);
                int buyStackSize = Integer.parseInt(buy[1]);
                buyPrice = buyPrice / buyStackSize;
                buyStackSize = 1;

                String[] sell = v[1].split(":");
                double sellPrice = Double.parseDouble(sell[0]);
                int sellStackSize = Integer.parseInt(sell[1]);
                sellPrice = sellPrice / sellStackSize;
                sellStackSize = 1;

                String[] stock = v[2].split(":");
                int currStock = Integer.parseInt(stock[0]);
                int maxStock = Integer.parseInt(stock[1]);

                boolean dynamicItem;

                if (v.length > 3) {
                    dynamicItem = (Integer.parseInt(v[3]) == 1);
                    if (!shop.addItem(id, type, buyPrice, sellPrice, currStock, maxStock, dynamicItem)) {
                        if (isolateBrokenShopFile(file)) {
                            log.warning(String.format("[%s] Shop File \"%s\" has bad Item Data (%d:%d), Moving to \"" + Config.getDirShopsBrokenPath() + "\"", plugin.getDescription().getName(), file.toString(), id, type));
                        } else {
                            log.warning(String.format("[%s] Shop File \"%s\" has bad Item Data (%d:%d), Error moving to \"" + Config.getDirShopsBrokenPath() + "\"", plugin.getDescription().getName(), file.toString(), id, type));
                        }
                        return null;
                    }
                } else if (!shop.addItem(id, type, buyPrice, sellPrice, currStock, maxStock)) {
                    if (isolateBrokenShopFile(file)) {
                        log.warning(String.format("[%s] Shop File \"%s\" has bad Item Data (%d:%d), Moving to \"" + Config.getDirShopsBrokenPath() + "\"", plugin.getDescription().getName(), file.toString(), id, type));
                    } else {
                        log.warning(String.format("[%s] Shop File \"%s\" has bad Item Data (%d:%d), Error moving to \"" + Config.getDirShopsBrokenPath() + "\"", plugin.getDescription().getName(), file.toString(), id, type));
                    }
                    return null;
                }
            } else if (key.matches("location\\d+$")) {
                // Attempt to match new Location Data String
                try {
                    String[] values = props.getProperty(key).split(", ");
                    ShopLocation shopLoc = new ShopLocation(Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]), Integer.parseInt(values[3]), Integer.parseInt(values[4]), Integer.parseInt(values[5]));
                    ((LocalShop) shop).getShopLocations().add(shopLoc);
                } catch (Exception e) {
                    if (isolateBrokenShopFile(file)) {
                        log.warning(String.format("[%s] Shop File \"%s\" has bad Location Data, Moving to \"" + Config.getDirShopsBrokenPath() + "\"", plugin.getDescription().getName(), file.toString()));
                    } else {
                        log.warning(String.format("[%s] Shop File \"%s\" has bad Location Data, and Error moving file to \"" + Config.getDirShopsBrokenPath() + "\"", plugin.getDescription().getName(), file.toString()));
                    }
                    return null;
                }

            } else if (key.matches("sign\\d+$")) {

                try {
                    String values = props.getProperty(key);

                    String[] v = values.split(":");
                    String[] v2 = v[1].split(",");

                    String signWorld = v[0];
                    int x = Integer.parseInt(v2[0]);
                    int y = Integer.parseInt(v2[1]);
                    int z = Integer.parseInt(v2[2]);
                    String itemName = v2[3];
                    int signId = Integer.parseInt(v2[4]);
                    int amount = 1;
                    if (v2.length == 6) {
                        amount = Integer.parseInt(v2[5]);
                    }

                    if (shop instanceof LocalShop) {
                        LocalShop lShop = (LocalShop) shop;
                        // Make sure the sign we're adding actually exists in
                        // the same world as the shop
                        if (!lShop.getWorld().equals(signWorld)) {
                            continue;
                        }
                        // Add the sign to the
                        if (plugin.getServer().getWorld(signWorld) != null) {
                            signList.add(new ShopSign(plugin.getServer().getWorld(signWorld), x, y, z, itemName, signId, amount));
                        } else {
                            signList.add(new ShopSign(signWorld, x, y, z, itemName, signId, amount));
                        }
                    }
                } catch (Exception e) {
                    // Just ignore the sign data for this if it fails to parse
                    // properly.
                    continue;
                }
            }
        }

        // After loading sign data, verify they exist in the world
        for (ShopSign sign : signList) {
            if (sign == null) {
                continue;
            }
            // Make sure we don't already have a sign at this location
            boolean ignoreSign = false;
            for (ShopSign s : shop.getSigns()) {
                if (s == null) {
                    continue;
                } else if (s.getLoc() == null) {
                    continue;
                } else if (s.getLoc().equals(sign.getLoc())) {
                    ignoreSign = true;
                    break;
                }
            }
            if (ignoreSign) {
                continue;
            }

            // Add signs that can't be verified yet.
            if (sign.getWorld() == null) {
                shop.getSigns().add(sign);
                continue;
            }

            // Check if the block is not a sign.
            if (!sign.isValid()) {
                continue;
            } else {
                ItemInfo item = Search.itemByName(sign.getItemName());
                // if We can't find the item in the shop ignore the sign
                if (!(shop.containsItem(item))) {
                    continue;
                } else {
                    shop.getSigns().add(sign);
                    updateSign(shop, sign);
                }
            }
        }

        // Sanity Checks
        // Check that filename == UUID from file
        if (!file.getName().equalsIgnoreCase(String.format("%s.shop", shop.getUuid().toString()))) {
            shop = null;

            if (isolateBrokenShopFile(file)) {
                log.warning(String.format("[%s] Shop file %s has bad data!  Moving to \"" + Config.getDirShopsBrokenPath() + "\"", plugin.getDescription().getName(), file.getName()));
            } else {
                log.warning(String.format("[%s] Shop file %s has bad data!  Error moving to \"" + Config.getDirShopsBrokenPath() + "\"", plugin.getDescription().getName(), file.getName()));
            }
            return null;
        }

        return shop;
    }

    public boolean isolateBrokenShopFile(File file) {
        File dir = new File(Config.getDirShopsBrokenPath());
        if(dir.mkdir()) {
            log.log(Level.INFO, "Created directory ''{0}''", dir.getName());
        }
        if (file.renameTo(new File(dir, file.getName())) && file.delete()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean saveAllShops() {
        log.info(String.format("[%s] %s", plugin.getDescription().getName(), "Saving All Shops"));

        // Local Shops
        for (Shop shop : shops.values()) {
            try {
                saveShop(shop);
            } catch (Exception e) {
                log.log(Level.WARNING, "Exception while saving shop " + shop.getUuid().toString(), e);
            }
        }
        return true;
    }

    public boolean saveShop(Shop shop) {
        SortedProperties props = new SortedProperties();

        // Config attributes
        props.setProperty("config-version", "3.0");

        // Shop attributes
        props.setProperty("uuid", shop.getUuid().toString());
        props.setProperty("name", shop.getName());
        props.setProperty("unlimited-money", String.valueOf(shop.isUnlimitedMoney()));
        props.setProperty("unlimited-stock", String.valueOf(shop.isUnlimitedStock()));
        props.setProperty("min-balance", String.valueOf(shop.getMinBalance()));
        props.setProperty("notification", String.valueOf(shop.getNotification()));
        props.setProperty("dynamic-prices", String.valueOf(shop.isDynamicPrices()));
        props.setProperty("share-percent", String.valueOf(shop.getSharePercent()));

        // Location
        if (shop instanceof GlobalShop) {
            props.setProperty("global", "true");
            props.setProperty("worlds", GenericFunctions.join(((GlobalShop) shop).getWorlds(), ", "));
        } else if (shop instanceof LocalShop) {
            LocalShop lShop = (LocalShop) shop;
            props.setProperty("world", lShop.getWorld());
            int i = 1;
            for (ShopLocation shopLoc : lShop.getShopLocations()) {
                props.setProperty("location" + i, shopLoc.toString());
                i++;
            }
        } else {
            // Unknown shop type!
        }

        // People
        props.setProperty("owner", shop.getOwner());
        props.setProperty("managers", GenericFunctions.join(shop.getManagers(), ", "));
        props.setProperty("creator", shop.getCreator());

        // Users & Groups
        if (!shop.getUserSet().isEmpty()) {
            props.setProperty("users", GenericFunctions.join(shop.getUserSet(), ", "));
        }
        if (!shop.getGroupSet().isEmpty()) {
            props.setProperty("groups", GenericFunctions.join(shop.getGroupSet(), ", "));
        }

        // Inventory
        for (Item item : shop.getItems()) {
            int id = item.getId();
            short subTypeId = item.getSubTypeId();
            ShopRecord record = shop.getItem(item);
            double buyPrice = record.getSellPrice();
            double sellPrice = record.getBuyPrice();
            int stock = record.getStock();
            int maxStock = record.getMaxStock();
            int dynamic = (record.isDynamic() ? 1 : 0);
            // TODO: Update and remove bundle storage completely?
            props.setProperty(String.format("%d:%d", id, subTypeId), String.format("%f:%d,%f:%d,%d:%d,%d", buyPrice, 1, sellPrice, 1, stock, maxStock, dynamic));
        }

        // Sign Data saving
        Iterator<ShopSign> iter = shop.getSigns().iterator();
        for (int index = 1; iter.hasNext(); index++) {
            ShopSign sign = iter.next();
            props.setProperty("sign" + index, String.format("%s:%d,%d,%d,%s,%d,%d", sign.getWorldName(), sign.getX(), sign.getY(), sign.getZ(), sign.getItemName(), sign.getType().getId(), sign.getAmount()));
        }

        String fileName = Config.getDirShopsActivePath() + shop.getUuid().toString() + ".shop";
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(fileName, false);
            props.store(stream, "LocalShops Config Version 2.0");
        } catch (IOException e) {
            log.log(Level.WARNING, "IOException: {0}", e.getMessage());
        } finally {
            try {
                if(stream != null) {
                    stream.close();
                }
            } catch (IOException ex) {
                log.log(Level.WARNING, "Failed to close stream (ShopManager.saveShop(...)", ex);
            }
        }

        return true;
    }

    public boolean deleteShop(Shop shop) {
        String shortUuid = shop.getShortUuidString();

        // remove shop from data structure
        shops.remove(shop.getUuid());

        // if global, remove from map
        if (shop instanceof GlobalShop) {
            for (String w : ((GlobalShop) shop).getWorlds()) {
                worldShops.remove(w);
            }
        } else if (shop instanceof LocalShop) {
            localShops.get(((LocalShop) shop).getWorld()).remove(shop.getUuid());
        }

        // remove string from uuid short list
        Config.removeUuidList(shortUuid);

        // delete the file from the directory
        String filePath = Config.getDirShopsActivePath() + shop.getUuid() + ".shop";
        File shopFile = new File(filePath);
        if(shopFile.delete()) {
            return true;
        } else {
            return false;
        }
    }

    public void mapWorldShop(String world, GlobalShop shop) {
        worldShops.put(world, shop.getUuid());
    }

    public boolean logItems(String playerName, String shopName, String action, String itemName, int numberOfItems, int startNumberOfItems, int endNumberOfItems) {

        return logTransaciton(playerName, shopName, action, itemName, numberOfItems, startNumberOfItems, endNumberOfItems, 0, 0, 0);

    }

    public boolean logPayment(String playerName, String action, double moneyTransfered, double startingbalance, double endingbalance) {

        return logTransaciton(playerName, null, action, null, 0, 0, 0, moneyTransfered, startingbalance, endingbalance);
    }

    public boolean logTransaciton(String playerName, String shopName, String action, String itemName, int numberOfItems, int startNumberOfItems, int endNumberOfItems, double moneyTransfered, double startingbalance, double endingbalance) {
        if (!Config.getSrvLogTransactions()) {
            return false;
        }

        String filePath = Config.getFileTransactionLog();

        File logFile = new File(filePath);
        FileOutputStream logFileOut = null;
        try {

            if(logFile.createNewFile()) {
                log.log(Level.INFO, "Created file ''{0}''", logFile.getName());
            }

            String fileOutput = "";

            DateFormat dateFormat = new SimpleDateFormat(
                    "yyyy/MM/dd HH:mm:ss z");
            Date date = new Date();
            fileOutput += dateFormat.format(date) + ": ";
            fileOutput += "Action: ";
            if (action != null) {
                fileOutput += action;
            }
            fileOutput += ": ";
            fileOutput += "Player: ";
            if (playerName != null) {
                fileOutput += playerName;
            }
            fileOutput += ": ";
            fileOutput += "Shop: ";
            if (shopName != null) {
                fileOutput += shopName;
            }
            fileOutput += ": ";
            fileOutput += "Item Type: ";
            if (itemName != null) {
                fileOutput += itemName;
            }
            fileOutput += ": ";
            fileOutput += "Number Transfered: ";
            fileOutput += numberOfItems;
            fileOutput += ": ";
            fileOutput += "Stating Stock: ";
            fileOutput += startNumberOfItems;
            fileOutput += ": ";
            fileOutput += "Ending Stock: ";
            fileOutput += endNumberOfItems;
            fileOutput += ": ";
            fileOutput += "Money Transfered: ";
            fileOutput += moneyTransfered;
            fileOutput += ": ";
            fileOutput += "Starting balance: ";
            fileOutput += startingbalance;
            fileOutput += ": ";
            fileOutput += "Ending balance: ";
            fileOutput += endingbalance;
            fileOutput += ": ";
            fileOutput += "\n";

            logFileOut = new FileOutputStream(logFile, true);
            logFileOut.write(fileOutput.getBytes());

        } catch (IOException e1) {
            System.out.println(plugin.getDescription().getName() + ": Error - Could not write to file " + logFile.getName());
            return false;
        } finally {
            try {
                if(logFileOut != null) {
                    logFileOut.close();
                }
            } catch (IOException ex) {
                log.log(Level.WARNING, "Failed to close stream (ShopManager.logTransaction(...)", ex);
            }
        }

        return true;
    }

    public void updateSign(Shop shop, ShopSign sign, int delay) {
        String[] signLines = shop.generateSignLines(sign);

        BlockState signState = sign.getLoc().getBlock().getState();
        //Set the lines
        ((Sign) signState).setLine(0, signLines[0]);
        ((Sign) signState).setLine(1, signLines[1]);
        ((Sign) signState).setLine(2, signLines[2]);
        ((Sign) signState).setLine(3, signLines[3]);

        plugin.scheduleUpdate(sign, 2 * delay);

    }

    public void updateSign(Shop shop, ShopSign sign) {
        updateSign(shop, sign, 0);
    }

    public void updateSign(Shop shop, Location loc) {
        for (ShopSign sign : shop.getSigns()) {
            if (sign.getLoc().equals(loc)) {
                updateSign(shop, sign);
                break;
            }
        }
    }

    public void updateSign(Shop shop, Block block) {
        updateSign(shop, block.getLocation());
    }

    public void updateSigns(Shop shop, Item item) {
        int index = 0;
        for (ShopSign sign : shop.getSigns()) {
            if (sign.getItemName().equalsIgnoreCase(item.getName())) {
                updateSign(shop, sign, index);
                index++;
            }
        }
    }

    public void updateSigns(Shop shop, Set<ShopSign> signSet) {
        int index = 0;
        for (ShopSign sign : shop.getSigns()) {
            updateSign(shop, sign, index);
            index++;
        }
    }

    /**
     * Only checks if a player is on the shop access list, or an owner, manager, or admin.
     * This IS NOT used for checking permission to use a command, ONLY for checking access list.
     *
     * @param player
     * @return
     */
    public boolean hasAccess(Shop shop, Player player) {
        if (shop.getOwner().equals(player.getName())) {
            return true;
        } else if (shop.getManagers().contains(player.getName())) {
            return true;
        } else if (shop.getUserSet().isEmpty() && shop.getGroupSet().isEmpty()) {
            return true;
        } else if (shop.getUserSet().contains(player.getName())) {
            return true;
        } else {
            for (String group : plugin.getPerm().getPlayerGroups(player.getWorld().getName(), player.getName())) {
                if (shop.getGroupSet().contains(group)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * @return null
     */
    public Callable<Object> updateSigns() {
        for (UUID key : shops.keySet()) {
            Shop shop = shops.get(key);
            if (shop.isDynamicPrices()) {
                for (ShopSign sign : shop.getSigns()) {
                    updateSign(shop, sign);
                }
            }
        }
        return null;
    }
}

