package org.micoli.minecraft.utils;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.micoli.minecraft.bukkit.QDBukkitPlugin;

// TODO: Auto-generated Javadoc
/**
 * The Class BlockUtils.
 */
public class BlockUtils {
	
	/**
	 * Draw line on top of blocks.
	 *
	 * @param qdplugin the qdplugin
	 * @param location1 the location1
	 * @param location2 the location2
	 * @param material the material
	 * Algorithm of the line available at : http://tech-algorithm.com/articles/drawing-line-using-bresenham-algorithm/
	 * @param listBlock the list block
	 */
	public static void drawLineOnTop(QDBukkitPlugin qdplugin,Location location1, Location location2, Material material,List<Block> listBlock) {
		//ServerLogger.log("draw line from %f,%f=>%f,%f %s", location1.getX(), location1.getZ(), location2.getX(), location2.getZ(), material.toString());
		//sendComments(player,ChatFormater.format("draw line from %f,%f=>%f,%f %s", location1.getX(), location1.getZ(), location2.getX(), location2.getZ(), material.toString()));
		int x = (int) location1.getX();
		int y = (int) location1.getZ();
		int w = (int) location2.getX() - x;
		int h = (int) location2.getZ() - y;
		int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0;
		if (w < 0){
			dx1 = -1;
		}else if (w > 0){
			dx1 = 1;
		}
		if (h < 0){
			dy1 = -1;
		}else if (h > 0){
			dy1 = 1;
		}
		if (w < 0){
			dx2 = -1;
		}else if (w > 0){
			dx2 = 1;
		}
		int longest = Math.abs(w);
		int shortest = Math.abs(h);
		
		if (!(longest > shortest)) {
			longest = Math.abs(h);
			shortest = Math.abs(w);
			if (h < 0){
				dy2 = -1;
			}else if (h > 0){
				dy2 = 1;
			}	
			dx2 = 0;
		}
		int numerator = longest >> 1;
		for (int i = 0; i <= longest; i++) {
			if(listBlock==null){
				setMaterialOnTop(qdplugin,new Location(location1.getWorld(),x,(double)0,y), material);
			}else{
				listBlock.add(setMaterialOnTop(qdplugin,new Location(location1.getWorld(),x,(double)0,y), material));
			}
			//ServerLogger.log("Point at %d,%d",x,y);
			numerator += shortest;
			if (!(numerator < longest)) {
				numerator -= longest;
				x += dx1;
				y += dy1;
			} else {
				x += dx2;
				y += dy2;
			}
		}
	}
	
	/**
	 * Sets the material on top.
	 *
	 * @param qdplugin the qdplugin
	 * @param location the location
	 * @param material the material
	 * @return the block
	 */
	public static Block setMaterialOnTop(QDBukkitPlugin qdplugin,Location location, Material material) {
		Location dstLocation = getTopPositionAtPos(location);
		location.getWorld().getBlockAt(dstLocation).setType(material);
		qdplugin.logger.log("Point %s", dstLocation.toString());
		return location.getWorld().getBlockAt(dstLocation);
	}
	
	/**
	 * Gets the top position at pos.
	 *
	 * @param location the location
	 * @return the top position at pos
	 */
	public static Location getTopPositionAtPos(Location location) {
		Location workLocation = location;
		World world = workLocation.getWorld();
		workLocation.setY(world.getMaxHeight() - 1);
		while (world.getBlockAt(workLocation).getType().getId() == 0 && workLocation.getY() > 0) {
			workLocation = workLocation.subtract(0, 1, 0);
		}
		return workLocation.getY() > 0 ? workLocation.add(0, 1, 0) : null;
	}



}

