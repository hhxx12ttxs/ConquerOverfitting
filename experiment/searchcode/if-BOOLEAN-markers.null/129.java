package net.sacredlabyrinth.nilla.dynmap.physicalshop.layers;

import com.wolvereness.physicalshop.Shop;
import org.bukkit.Location;
import org.bukkit.World;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;
import net.sacredlabyrinth.nilla.dynmap.physicalshop.DynmapPhysicalShop;
import net.sacredlabyrinth.nilla.dynmap.physicalshop.Helper;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sacredlabyrinth.nilla.dynmap.physicalshop.MapShop;

public class Shops
{
    private final String MARKER_SET = "physicalshop.shops";
    private final String ICON_ID = "physicalshop.shop";
    private final String ICON = "chest.png";
    private final String CONFIG = "layer.shops.";
    private final String LABEL = "Shops";
    
                
    private final String FORMAT = "Shop by: &e{owner_name}|&f{material} ({shop_items})|&f{buy_rate} &e{buy_currency} |&f{sell_rate} &e{sell_currency}|&d{buy_capital} &d{sell_capital}";

    private DynmapPhysicalShop plugin;
    private boolean stop;
    private int task;

    private boolean enable;
    private int updateSeconds;
    private String label;
    private String format;
    private int layerPriority;
    private boolean hideByDefault;
    private int minZoom;

    List<String> hidden;

    private MarkerSet markerSet;
    private MarkerIcon icon;
    private Map<String, Marker> markers = new HashMap<String, Marker>();

    public Shops()
    {
        plugin = DynmapPhysicalShop.getInstance();
        readConfig();

        if (enable)
        {
            initMarkerSet();
            initIcon();
            
            //Initially update in 10 seconds
            scheduleNextUpdate(10);
        }
    }

    private void readConfig()
    {
        enable = plugin.getCfg().getBoolean(CONFIG + "enable", true);
        updateSeconds = Math.max(plugin.getCfg().getInt(CONFIG + "update-seconds", 10), 2);
        label = plugin.getCfg().getString(CONFIG + "label", LABEL);
        format = plugin.getCfg().getString(CONFIG + "format", FORMAT);
        layerPriority = plugin.getCfg().getInt(CONFIG + "layer-priority", 1);
        hideByDefault = plugin.getCfg().getBoolean(CONFIG + "hide-by-default", true);
        minZoom = Math.max(plugin.getCfg().getInt(CONFIG + "min-zoom", 0), 0);

        hidden = plugin.getCfg().getStringList(CONFIG + "hidden-markers");
    }

    private void initMarkerSet()
    {
        markerSet = plugin.getMarkerApi().getMarkerSet(MARKER_SET);

        if (markerSet == null)
        {
            markerSet = plugin.getMarkerApi().createMarkerSet(MARKER_SET, label, null, false);
        }
        else
        {
            markerSet.setMarkerSetLabel(label);
        }

        if (markerSet == null)
        {
            DynmapPhysicalShop.severe("Error creating " + LABEL + " marker set");
            return;
        }

        markerSet.setLayerPriority(layerPriority);
        markerSet.setHideByDefault(hideByDefault);
        markerSet.setMinZoom(minZoom);
    }

    private void initIcon()
    {
        icon = plugin.getMarkerApi().getMarkerIcon(ICON_ID);

        if (icon == null)
        {
            InputStream stream = DynmapPhysicalShop.class.getResourceAsStream("/images/" + ICON);
            icon = plugin.getMarkerApi().createMarkerIcon(ICON_ID, ICON_ID, stream);
        }

        if (icon == null)
        {
            DynmapPhysicalShop.severe("Error creating icon");
        }

    }

    private void scheduleNextUpdate(int seconds)
    {
        plugin.getServer().getScheduler().cancelTask(task);
        task = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Update(), seconds * 20);
    }

    private class Update implements Runnable
    {
        public void run()
        {
            if (!stop)
            {
                updateMarkerSet();
                scheduleNextUpdate(updateSeconds);
            }
        }
    }

    public void cleanup()
    {
        if (markerSet != null)
        {
            markerSet.deleteMarkerSet();
            markerSet = null;
        }
        markers.clear();
        stop = true;
    }

    private boolean isVisible(String id, String worldName)
    {
        if (hidden != null && !hidden.isEmpty())
        {
            if (hidden.contains(id) || hidden.contains("world:" + worldName))
            {
                return false;
            }
        }
        return true;
    }

    private void updateMarkerSet()
    {
        Map<String, Marker> newMarkers = new HashMap<String, Marker>();

        // get clans with homes

        List<MapShop> shops = plugin.getShopManager().getShops();

        for (World world : plugin.getServer().getWorlds())
        {
            for (MapShop shop : shops)
            {
                
                Location loc = shop.getLocation();
                String id = loc.getWorld().getName() + "_" + loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ();
                // one world at a time

                if (loc.getWorld() != world)
                {
                    continue;
                }

                // skip if not visible

                if (!isVisible(id, world.getName()))
                {
                    continue;
                }

                // expand the label format

                String label = format;
                
                try{
                    
                label = shop.FormatLabel(format);
                
                }
                catch(Exception ex){
                
                }
                label = Helper.colorToHTML(label);

                // pull out the markers from the old set to reuse them

                Marker m = markers.remove(id);

                if (m == null)
                {
                    m = markerSet.createMarker(id, label, true, world.getName(), loc.getX(), loc.getY(), loc.getZ(), icon, false);
                }
                else
                {
                    m.setLocation(world.getName(), loc.getX(), loc.getY(), loc.getZ());
                    m.setLabel(label, true);
                    m.setMarkerIcon(icon);
                }

                newMarkers.put(id, m);
            }
        }

        // delete all markers that we will no longer use

        for (Marker oldMarker : markers.values())
        {
            oldMarker.deleteMarker();
        }

        // clean and replace the marker set

        markers.clear();
        markers = newMarkers;
    }
}

