/**
 * Copyright (c) 2012, http://www.yissugames.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yissugames.blocklife.gamelogic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import com.yissugames.blocklife.BlockLife;
import com.yissugames.blocklife.RenderSystem;
import com.yissugames.blocklife.Voice;
import com.yissugames.blocklife.gamelogic.Inventory.StackInfo;
import com.yissugames.blocklife.generators.BiomPlains;
import com.yissugames.blocklife.server.ShooterClient;

public class World implements Serializable {

	public static final int TOGENERATECHUNKS = 10;
	private ArrayList<Chunk> chunks = new ArrayList<Chunk>();
	private long seed;
	public long getSeed() {
		return seed;
	}
	private String name;
	public String getName() {
		return name;
	}
	private Random random;
	
	private boolean isLeftButtonDownOld = true;
	private boolean isRightButtonDownOld = false;
	
	private Player player;
	private ArrayList<OnlinePlayer> players;
	
	public World(String seed, String name)
	{
		this.seed = Long.parseLong(seed.toLowerCase().replace('a', '1').replace('b', '2').replace('c', '3').replace('d', '4').replace('e', '5')
				.replace('f', '6').replace('g', '7').replace('h', '8').replace('i', '9').replace("j", "10").replace("k", "11")
				.replace("l", "12").replace("m", "13").replace("n", "14").replace("o", "15").replace("p", "16").replace("q", "17")
				.replace("r", "18").replace("s", "19").replace("t", "20").replace("u", "21").replace("v", "22").replace("w", "23")
				.replace("x", "24").replace("y", "25").replace("z", "26"));
		
		random = new Random(this.seed);
		this.name = name;
		
		int last = 130;
		for(int i = 0; i < TOGENERATECHUNKS; i++)
		{
			chunks.add(new Chunk(new BiomPlains(), last, i * 32, random));
			last = chunks.get(chunks.size() - 1).getLastHeight();
		}
		
		this.player = new Player("xy", 0, chunks.get(0).getFirstHeight());
	}
	
	public World(String seed, int togeneratechunks) {
		this.seed = Long.parseLong(seed.toLowerCase().replace('a', '1').replace('b', '2').replace('c', '3').replace('d', '4').replace('e', '5')
				.replace('f', '6').replace('g', '7').replace('h', '8').replace('i', '9').replace("j", "10").replace("k", "11")
				.replace("l", "12").replace("m", "13").replace("n", "14").replace("o", "15").replace("p", "16").replace("q", "17")
				.replace("r", "18").replace("s", "19").replace("t", "20").replace("u", "21").replace("v", "22").replace("w", "23")
				.replace("x", "24").replace("y", "25").replace("z", "26"));
		
		random = new Random(this.seed);
		this.name = "noname";
		
		int last = 130;
		for(int i = 0; i < togeneratechunks; i++)
		{
			chunks.add(new Chunk(new BiomPlains(), last, i * 32, random));
			last = chunks.get(chunks.size() - 1).getLastHeight();
		}
	}
	
	public World(SerializableWorld sWorld) {
		Chunk[] tmpChunks = sWorld.chunks;
		for(Chunk c: tmpChunks) {
			chunks.add(c);
		}
		
		this.player = new Player("xy", 0, chunks.get(0).getFirstHeight());
	}

	
	public ArrayList<Chunk> getAllChunks()
	{
		return this.chunks;
	}
	
	public boolean save()
	{
		try {
			if(!new File(BlockLife.ApplicationFolder + "world" + BlockLife.DS + name + BlockLife.DS).exists())
				new File(BlockLife.ApplicationFolder + "world" + BlockLife.DS + name + BlockLife.DS).mkdirs();
			
			FileOutputStream file = new FileOutputStream(BlockLife.ApplicationFolder + "world" + BlockLife.DS + name + BlockLife.DS + "world.blfile");
			ObjectOutputStream o = new ObjectOutputStream(file);
			o.writeObject(new SerializableWorld(this));
			o.close();
			file.close();
		} catch (IOException e) { 
			BlockLife.log.log(Level.SEVERE, e.toString());
			return false;
		}
		
		return true;
	}
	
	public static World load(String name) {
		return loadStatic(BlockLife.ApplicationFolder + "world" + BlockLife.DS + name + BlockLife.DS + "world.blfile");
	}
	
	public static World loadStatic(String path) {
		try {
			FileInputStream file = new FileInputStream(path);
			ObjectInputStream o = new ObjectInputStream(file);
			SerializableWorld world = (SerializableWorld) o.readObject();
			o.close();
			file.close();
			
			return new World(world);
		} catch (IOException e) {
			BlockLife.log.log(Level.SEVERE, e.toString());
			return null;
		} catch (ClassNotFoundException e) {
			BlockLife.log.log(Level.SEVERE, e.toString());
			return null;
		}
	}
	
	public Chunk get(int id)
	{
		return chunks.get(id);
	}
	
	public void update() {
		for(Chunk c: chunks) {
			c.update();
		}
	}
	
	public void render(boolean onlyRender, boolean ignoreInput)
	{	
		player.begin(this, ignoreInput);
		BlockCamera camera = player.getCamera();
		
		int chunkAtTheLeft = (int) (camera.getScreenLeft() / 32) / 32;
		int chunkAtTheRight = (int) (camera.getScreenRight() / 32) / 32;
		
		for(int i = chunkAtTheLeft; i <= chunkAtTheRight; i++)
			chunks.get(i).render();
		
		if(onlyRender)
		{
			GL11.glLoadIdentity();  // Reset matrix what player.end() normally do
			return;
		}
		
		int mouseX = (int) (camera.getMouseX() / 32);
		int mouseY = (int) (camera.getMouseY() / 32);
		int chunkId = (int) (mouseX / 32);
		boolean chunkFound = chunkId >= 0 && chunks.size() > chunkId;
		
		//Display.setTitle("X: " + mouseX + " Y: " + mouseY + " ChunkID: " + chunkId + " ToRender: " + (chunkAtTheRight - chunkAtTheLeft + 1));
		if(Mouse.isButtonDown(0) && !isLeftButtonDownOld && chunkFound && !player.getInventory().isFullInventoryOpened())
		{
			player.getInventory().addItem(chunks.get(chunkId).destroy(mouseX - (32 * chunkId), mouseY), 1);
			ShooterClient.sendDestroy(chunkId, mouseX - (32 * chunkId), mouseY);
		}
		if(Mouse.isButtonDown(1) && !isRightButtonDownOld && chunkFound && !player.getInventory().isFullInventoryOpened())
		{
			if(!chunks.get(chunkId).hasBlock(mouseX - (32 * chunkId), mouseY))
			{
				int blockId = player.getInventory().removeCurrentQuick();
				chunks.get(chunkId).addBlock(mouseX - (32 * chunkId), mouseY, blockId);
				ShooterClient.sendAdd(chunkId, mouseX - (32 * chunkId), mouseY, blockId);
			}
		}
				
		if(chunkFound && !player.getInventory().isFullInventoryOpened())
			if(chunks.get(chunkId).hasBlock(mouseX - chunkId * 32, mouseY))
				RenderSystem.renderColoredQuad(mouseX * 32, mouseY * 32, 32, 32, 1, 1, 1, .3f);
		
		player.end(ignoreInput);
		
		isLeftButtonDownOld = Mouse.isButtonDown(0);
		isRightButtonDownOld = Mouse.isButtonDown(1);
	}
	
}

