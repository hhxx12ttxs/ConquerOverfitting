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

package com.yissugames.gui;

import org.jdom2.Element;
import org.newdawn.slick.opengl.Texture;

import com.yissugames.blocklife.ContentLoader;
import com.yissugames.blocklife.RenderSystem;

public class AnimatedImage implements GUI {
	
	private int startX;
	private int endX;
	private double step;
	private int Y;
	private Texture image;
	private boolean isGoingLeft;
	private boolean killAfter;
	
	private boolean dead;
	public boolean isDead() {
		return dead;
	}
	
	public AnimatedImage(int startX, int endX,int Y, double step, String textureName){
		this.startX = startX;
		this.endX = endX;
		this.Y = Y;
		this.currX = startX;
		this.step = step;
		this.image = ContentLoader.getTexture(textureName);
	}
	
	public AnimatedImage(int startX, int endX,int Y, double step, String textureName, boolean kill){
		this.startX = startX;
		this.endX = endX;
		this.Y = Y;
		this.currX = startX;
		this.step = step;
		this.image = ContentLoader.getTexture(textureName);
		this.killAfter = kill;
		
		isGoingLeft = startX > endX;
	}
	
	private double currX;
	
	@Override
	public void render() {
		if(killAfter && dead)
			return;
		
		if (isGoingLeft){
			currX -= step;
		}else {
			currX += step;
		}
		
		if (currX < startX){
			isGoingLeft = false;
		}else if (currX > endX){
			isGoingLeft = true;
		}
		
		if(currX > endX && startX < endX)
			dead = true;
		if(currX < endX && startX > endX)
			dead = true;
		
		RenderSystem.renderTexture(image, (int) currX, Y);
	}

	public static AnimatedImage createByXML(Element e)
	{
		int startX = Integer.parseInt(e.getAttributeValue("startX"));
		int endX = Integer.parseInt(e.getAttributeValue("endX"));
		int posY = Integer.parseInt(e.getAttributeValue("positionY"));
		double speed = Double.parseDouble(e.getAttributeValue("speed"));
		String file = e.getAttributeValue("file");
		
		return new AnimatedImage(startX, endX, posY, speed, file);
	}
	
}

