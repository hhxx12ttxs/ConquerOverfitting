package com.me.mygdxgame.scene.map;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.me.mygdxgame.App;
import com.me.mygdxgame.game.Game;
import com.me.mygdxgame.game.GameCamera;
import com.me.mygdxgame.game.GameMover;
import com.me.mygdxgame.game.action.DestroyWallAction;
import com.me.mygdxgame.mgr.UiManager;
import com.me.mygdxgame.scene.SceneBase;
import com.me.mygdxgame.sprite.SpriteBase;
import com.me.mygdxgame.sprite.WallSpriteState;
import com.me.mygdxgame.ui.map.MapUi;
import com.me.mygdxgame.utils.Cst;
import com.me.mygdxgame.utils.Grid;

public class SceneMap extends SceneBase implements InputProcessor, GestureListener{

	final Vector3 curr = new Vector3();
	final Vector3 last = new Vector3(-1, -1, -1);
	final Vector3 delta = new Vector3();
	Texture tileset;
	SpriteBatch spriteBatch;
	InputMultiplexer plex;
	public static Grid grid = new Grid();
	public MapUi mapUi;
	
	public SceneMap(){
		//StageMgr.startStageLater(new StageMap());
		Game.map.setup(-1);
		plex = new InputMultiplexer();
		
		plex.addProcessor(UiManager.instance().stage);
		plex.addProcessor(this);
		spriteBatch = new SpriteBatch();
		//tileset = TextureManager.get("tileset.png");
		Gdx.input.setInputProcessor(plex);
		
		mapUi = new MapUi();
		UiManager.instance().clear();
		UiManager.instance().setFpsVisible(true);
		UiManager.instance().setRoot(mapUi);
		
	}

	public void updatePre(){
		super.updatePre();
		//updateTime = ...
	}

	public void updateMain(){
		super.updateMain();
		//Game.camera.moveToStartPosition();
		//System.out.println(Game.camera.position);

		Game.camera.update();
		Game.map.update();
		App.instance().tweenManager.update(updateTime);
		
		spriteBatch.setProjectionMatrix(Game.camera.combined);
		
		//Gdx.graphics.getGL20().glClearColor( 1, 0, 0, 1 );
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

        spriteBatch.begin();
        
        short[][] tilemap = Game.map.mapData.tilemap;
        GameCamera cam = Game.camera;
        
        Vector3 tmp = new Vector3();
        tmp.set(0, 0, 0);
        cam.unproject(tmp);
        
        int startI = (int) (tmp.x/Cst.TILE_W);
        int startJ = (int) (tmp.y/Cst.TILE_H);
        startI = Math.max(startI, 0);
        startJ = Math.max(startJ, 0);
        
        int iOffset = 2;
        int jOffset = 10;
        
        int baseEndI = startI + (int)((cam.width())/Cst.TILE_W);
        int endI = baseEndI  + iOffset;
        int baseEndJ =  startJ + (int)((cam.height())/Cst.TILE_H);
        int endJ = baseEndJ + jOffset;
        endI = Math.min(endI, Game.map.mapData.width);
        endJ = Math.min(endJ, Game.map.mapData.height);

        
        SpriteBase sprite = new SpriteBase("tileset6.png");
        SpriteBase highlightedSprite = new SpriteBase("highlightedSprite.png");
		highlightedSprite.setOrigin(0, Cst.WALL_HEIGHT);
		highlightedSprite.setColor(1f, 1f, 1f, 0.5f);
		
        //Draw floor
        for(int i=startI; i<endI; i++){
        	for(int j=startJ; j<endJ; j++){
        		
        		short spriteIndex = tilemap[i][j];
        		
        		if(spriteIndex != 0){
        			continue;
        		}
        		
				sprite.setRegion((spriteIndex%16)*Cst.TILE_W, (spriteIndex/16)*Cst.TILE_PLUS_WALL_HEIGHT, Cst.TILE_W, Cst.TILE_H);
				sprite.setSize(Cst.TILE_W, Cst.TILE_H);
				sprite.flip(false, true);
				sprite.setPosition(i*Cst.TILE_W, j*Cst.TILE_H);
				sprite.draw(spriteBatch);
        	}
        }
        
        
        for(int j=startJ; j<endJ; j++){
        	for(int i=startI; i<endI; i++){
        		
        		sprite.setColor(1f, 1f, 1f, 1f);
        		//spriteBatch.setColor(1f, 1f, 1f, 1f);
		        //spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);  
        		
        		WallSpriteState wallState = Game.map.parties.get(0).wallStates.get(i, j);
				if(wallState != null){
					//System.out.println(i + " " + j);
					if(wallState.selected){
						sprite.setColor(1f, 1f, 0f, 1f);
						//Pixmap pixmap = new Pixmap();
				         //spriteBatch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE);
				         //spriteBatch.setColor(1, 1, 0, 1f);
				         //spriteBatch.draw(region, position.x, position.y);
				         //spriteBatch.setColor(1f, 1f, 1f, 1f);
				         //spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);  
				         
						//highlightedSprite.setPosition(i*Cst.TILE_W, j*Cst.TILE_H);
						//highlightedSprite.draw(spriteBatch);
					}
				}
				
				List<GameMover> events =  Game.map.eventsAt(i, j);
				if(events != null){
					for(GameMover event : events){
						if(event.sprite != null){
							event.sprite.draw(spriteBatch);
						}
					}
				}
				
        		short spriteIndex = tilemap[i][j];
        		
        		if(spriteIndex != 0 && spriteIndex != 0xF00){
        		
	        		short borderIndex = (short) (tilemap[i][j] & 0x0F);
	        		if(borderIndex != 0){
						sprite.setRegion((borderIndex%16)*Cst.TILE_W, (borderIndex/16)*Cst.TILE_PLUS_WALL_HEIGHT, Cst.TILE_W, Cst.TILE_PLUS_WALL_HEIGHT);
						sprite.setSize(Cst.TILE_W, Cst.TILE_PLUS_WALL_HEIGHT);
						sprite.flip(false, true);
						sprite.setPosition(i*Cst.TILE_W, j*Cst.TILE_H);
						sprite.setOrigin(0, Cst.WALL_HEIGHT);
						sprite.draw(spriteBatch);
	        		}
	        		
					short cornerIndex = (short) (tilemap[i][j] & 0xF0);
	        			
	        		if((cornerIndex & 0x10) == 0x10){
	        			sprite.setRegion(0, Cst.TILE_H, Cst.TILE_HW, Cst.TILE_HH);
	            		sprite.setSize(Cst.TILE_HW, Cst.TILE_HH);
	    				sprite.flip(false, true);
	    				sprite.setPosition(i*Cst.TILE_W, j*Cst.TILE_H);
	    				sprite.setOrigin(0, Cst.WALL_HEIGHT);
	    				sprite.draw(spriteBatch);
	        		}
	        		if((cornerIndex & 0x20) == 0x20){
	        			sprite.setRegion(Cst.TILE_HW, Cst.TILE_H, Cst.TILE_HW, Cst.TILE_HH);
	        			sprite.setSize(Cst.TILE_HW, Cst.TILE_HH);
	    				sprite.flip(false, true);
	    				sprite.setPosition(Cst.TILE_HW+i*Cst.TILE_W, j*Cst.TILE_H);
	    				sprite.setOrigin(0, Cst.WALL_HEIGHT);
	    				sprite.draw(spriteBatch);
	        		}
	        		if((cornerIndex & 0x40) == 0x40){
	        			sprite.setRegion(Cst.TILE_HW, Cst.TILE_H+Cst.TILE_HH, Cst.TILE_HW, Cst.TILE_HH);
	        			sprite.setSize(Cst.TILE_HW, Cst.TILE_HH);
	    				sprite.flip(false, true);
	    				sprite.setPosition(Cst.TILE_HW+i*Cst.TILE_W, Cst.TILE_HH+j*Cst.TILE_H);
	    				sprite.setOrigin(0, Cst.WALL_HEIGHT);
	    				sprite.draw(spriteBatch);
	        		}
	        		if((cornerIndex & 0x80) == 0x80){
	        			sprite.setRegion(0, Cst.TILE_H+Cst.TILE_HH, Cst.TILE_HW, Cst.TILE_HH);
	        			sprite.setSize(Cst.TILE_HW, Cst.TILE_HH);
	    				sprite.flip(false, true);
	    				sprite.setPosition(i*Cst.TILE_W, Cst.TILE_HH+j*Cst.TILE_H);
	    				sprite.setOrigin(0, Cst.WALL_HEIGHT);
	    				sprite.draw(spriteBatch);
	        		}
        		}
				

				
				if(wallState != null){
					//System.out.println(i + " " + j);
					if(wallState.selected){
						highlightedSprite.setPosition(i*Cst.TILE_W, j*Cst.TILE_H);
						highlightedSprite.draw(spriteBatch);
					}
				}
				
			}
		}
        

        mapUi.minimap.update(spriteBatch, startI, startJ, Math.min(baseEndI, Game.map.mapData.width), Math.min(baseEndJ, Game.map.mapData.height));
        //mapUi.update();
        //spriteBatch.draw(dynamicTexture, 0, 0, 0, 0, 60, 60, 1, 1);
        
		spriteBatch.end();
		//grid.update(startI, startJ, endI, endJ);

        
	}
	
	public void terminate() {
		super.terminate();
		//removeInputListener(this);
	}


	
	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		switch(character) {
		case '-':
			if(Game.camera.zoom >= 8){
				return false;
			}
			Game.camera.zoom *= 2;
			break;
		case '+':
			if(Game.camera.zoom <= 0.25){
				return false;
			}
			Game.camera.zoom /= 2;
			break;
		case '5':
			//System.out.println("lala");
			GameMover mover = Game.map.parties.get(0).units.get(0);
			//mover.findPath(0,0);
			//System.out.println("lol");
			//Game.map.parties.get(0).members.get(0).setTilePosition(Game.map.mapData.startI, Game.map.mapData.startJ);
			break;
		}
		return false;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		if(button == Input.Buttons.LEFT){
			Vector3 v = new Vector3();
			Ray pickRay = Game.camera.getPickRay(x, y);
			Intersector.intersectRayPlane(pickRay, Cst.XY_PLANE, v);
			
			float dy = v.y + Cst.WALL_HEIGHT;
			if(v.x < 0 || dy < 0){
				return false;
			}
			
			int i = (int)v.x/Cst.TILE_W;
			int j = (int)dy/Cst.TILE_H;
			
			//System.out.println(i + " " + j);
			if(i<=0 || j <= 0 || i >= Game.map.mapData.width-1 || j >= Game.map.mapData.height-1){
				return false;
			}

			if((j==0 && Game.map.mapData.tilemap[i][j+1] == 0) || (j==Game.map.mapData.height-2 && Game.map.mapData.tilemap[i][j+1] != 0)){
				return false;
			}
			/*
			if(j+1 < Game.map.mapData.height-1 && Game.map.mapData.tilemap[i][j+1] != 0){
				System.out.println("olé");
				j += 1;
			}*/
			
			//System.out.println(Game.map.mapData.tilemap[i][j]);
			if(Game.map.mapData.tilemap[i][j] != 0){
				DestroyWallAction cachedAction = DestroyWallAction.cache.get(i, j);
				if(cachedAction != null){
					cachedAction.interrupt();
					Game.map.parties.get(0).completedActionQueue.add(cachedAction);
				}
				else{
					DestroyWallAction action = new DestroyWallAction();
					action.setup(Game.map.parties.get(0), i, j);
					Game.map.parties.get(0).actionQueue.add(action);
				}
				
			}
			//GameMover mover = Game.map.parties.get(0).units.get(0);
			//mover.findPath(i, j);
		}

		return true;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		last.set(-1, -1, -1);
		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		if((Gdx.input.isButtonPressed(Input.Buttons.RIGHT) || Gdx.input.isTouched(1))){

			Ray pickRay = Game.camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());
			Intersector.intersectRayPlane(pickRay, Cst.XY_PLANE, curr);
			if(!(last.x == -1 && last.y == -1 && last.z == -1)) {
				pickRay = Game.camera.getPickRay(last.x, last.y);
				Intersector.intersectRayPlane(pickRay, Cst.XY_PLANE, delta);			
				delta.sub(curr);
				//Game.camera.position.add(delta.x, delta.y, delta.z);
				Game.camera.setPosition(Game.camera.position.x+delta.x, Game.camera.position.y+delta.y);
			}
			last.set(Gdx.input.getX(), Gdx.input.getY(), 0);
		}
		return true;
	}

	//@Override
	public boolean touchMoved(int x, int y) {

		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		switch(amount) {
		case 1:
			Game.camera.zoom += 0.1;
			break;
		case -1:
			Game.camera.zoom -= 0.1;
			break;
		}
		return true;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		/*
		Vector3 v = new Vector3();
		Ray pickRay = Game.camera.getPickRay(screenX, screenY);
		Intersector.intersectRayPlane(pickRay, Cst.XY_PLANE, v);
		System.out.println(v.x + " " + v.y);*/
		return true;
	}

	@Override
	public boolean touchDown(float x, float y, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean tap(float x, float y, int count, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean longPress(float x, float y) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean fling(float velocityX, float velocityY, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean pan(float x, float y, float deltaX, float deltaY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean zoom(float initialDistance, float distance) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2,
			Vector2 pointer1, Vector2 pointer2) {
		// TODO Auto-generated method stub
		return false;
	}
	
}

