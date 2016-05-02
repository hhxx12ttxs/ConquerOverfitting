package ca.viaware.rpg.map;

import java.util.ArrayList;

import ca.viaware.rpg.game.Globals;
import ca.viaware.rpg.utilities.Location;

public class Map {
	public int xSize, ySize;
	private double xOffset, yOffset;
	public Tile mapTiles[][];
	Location spawn;
	private ArrayList<Tile> ObstacleMap = new ArrayList<Tile>();

	
	public void setSize(int x, int y) {
		xSize = x;
		ySize = y;
		mapTiles = new Tile[x][y];
	}

	public void renderMap() {
		int countx = 0;
		int county = 0;
		while (county < ySize) {
			while (countx < xSize) {

				if (mapTiles[countx][county].isEnabled()) {
					mapTiles[countx][county].render(countx, county, (int) xOffset, (int) yOffset);
					Globals.temp++;
				} else {
				}

				countx++;
			}
			countx = 0;
			county++;

		}

	}

	public Tile[][] getMap(){
			
			return mapTiles;
	}
	public void moveMap(double x, double y, int delta) {
		xOffset += x * delta;
		yOffset += y * delta;

	}

	public int getXOffset() {
		return (int) xOffset;
	}

	public int getYOffset() {
		return (int) yOffset;
	}

	public void setOffsets(double x, double y) {
		xOffset = x;
		yOffset = y;
	}

	public void setSpawn(int x, int y) {
		spawn = new Location(x, y);
	}

	public int getXSize(){
		return xSize;
	}
	public int getYSize(){
		return ySize;
	}
	public Location getSpawn() {
		return spawn;
	}
	public ArrayList<Tile> getObstacleMap() {
		return ObstacleMap;
	}
	public void setObstacleMap() {
		// inner arrray;
		// x
		
		for (int X = 0; Globals.gameMap.getMap().length > X; X++) {

			// y
			// this goes through every block
			for (int Y = 0; Globals.gameMap.getMap()[0].length > Y; Y++) {

				if (Globals.gameMap.getMap()[X][Y].hasCollision()) {
					getObstacleMap().add(Globals.gameMap.getMap()[X][Y]);

				}
			}
		}
	}
}

