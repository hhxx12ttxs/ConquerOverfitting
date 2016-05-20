package game;

import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.tiled.TileAtlas;
import com.badlogic.gdx.graphics.g2d.tiled.TileMapRenderer;
import com.badlogic.gdx.graphics.g2d.tiled.TiledLayer;
import com.badlogic.gdx.graphics.g2d.tiled.TiledLoader;
import com.badlogic.gdx.graphics.g2d.tiled.TiledMap;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Level {
	private TiledMap tiledMap;
	private TileAtlas tileAtlas;
	private TileMapRenderer tileMapRenderer;
	private int[] layerIndexes;
	private Array<Rectangle> collisionTiles;
	private ShapeRenderer renderer;
	private Array<MoveableEntity> enemies;
	
	public Level(String filename, GameScreen gameScreen) {
		tiledMap = TiledLoader.createMap(Gdx.files.internal("assets/" + filename));
		tileAtlas = new TileAtlas(tiledMap, Gdx.files.internal("assets"));
		tileMapRenderer = new TileMapRenderer(tiledMap, tileAtlas, 8, 8);
		collisionTiles = new Array<Rectangle>();
		initGround();
		renderer = new ShapeRenderer();
		
		enemies = new Array<MoveableEntity>();
		enemies.add(new DragonSpawn(1300, 64, gameScreen.getEntityTexture()));
	}
	
	public void debug(OrthographicCamera camera) {
		renderer.begin(ShapeType.Rectangle);
		Iterator<Rectangle> it = collisionTiles.iterator();
		renderer.setProjectionMatrix(camera.combined);
		while(it.hasNext()) {
			Rectangle r = it.next();
			renderer.rect(r.x, r.y, r.width, r.height);
		}
		renderer.setColor(Color.WHITE);
		renderer.end();
	}
	
	public void dispose() {
		tileMapRenderer.dispose();
		tileAtlas.dispose();
	}
	
	public Array<Rectangle> getCollisionTiles() {
		return collisionTiles;
	}
	
	public void initGround() {
		ArrayList<TiledLayer> layers = tiledMap.layers;
		Iterator<TiledLayer> it = layers.iterator();
		int layerIndex = 0;
		Array<Integer> tempIndex = new Array<Integer>();
		float halfTile = GameScreen.TILE_SIZE / 2;
		float boxTileWidth = halfTile * GameScreen.WORLD_TO_BOX;
		while(it.hasNext()) {
			TiledLayer layer = it.next();
			if(layer.name.equals("collision")) {
				int[][] tiles = layer.tiles;
				for(int ty = 0; ty < tiles.length; ty++) {
					
					for(int tx = 0; tx < tiles[ty].length; tx++) {
						String type = tiledMap.getTileProperty(tiles[ty][tx], "type");
						if(type != null && type.equals("solid")) {
							float x = tx * 32;
							// subtracting the y by the highest possible value, 
							// as the coordinates order needs to be reversed for OpenGL coords 
							float y = Math.abs(ty - (tiles.length-1)) * 32;
							collisionTiles.add(new Rectangle(x, y, 32, 32));
						}
					}
				}
			}
			else {
				tempIndex.add(layerIndex);
			}
			layerIndex++;
		}
		layerIndexes = new int[tempIndex.size];
		for(int i = 0; i < tempIndex.size; i++) {
			layerIndexes[i] = tempIndex.get(i);
		}
	}
	
	public void render(OrthographicCamera camera) {
		tileMapRenderer.render(camera, layerIndexes);
		//debug(camera);
	}
	
	public void renderEntities(SpriteBatch batch, OrthographicCamera camera) {
		Iterator<MoveableEntity> it = enemies.iterator();
		while(it.hasNext()) {
			MoveableEntity enemy = it.next();
			enemy.render(batch, camera);
		}
	}
	
	public void update(WorldCollision wc) {
		Iterator<MoveableEntity> it = enemies.iterator();
		while(it.hasNext()) {
			MoveableEntity enemy = it.next();
			wc.checkIfEntityIsOnGround(this, enemy);
			enemy.update();
		}
	}
}

