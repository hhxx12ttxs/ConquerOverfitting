package ifs;

import static org.lwjgl.opengl.GL11.*;

import java.util.Comparator;
import java.util.Random;


import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.Color;
//import org.newdawn.slick.opengl.Texture;

import static ifs.Utility.*;
import static ifs.IFS.*;


public class EntityCharacter extends Entity {
	
	static final int STATE_STANDING = 0;
	static final int STATE_HAKKING = 1;
	static final int STATE_WALKING = 2;
	
	static final int COLOUR_HAT   =	0;
	static final int COLOUR_SHIRT = 1;
	static final int COLOUR_PANTS = 2;
	static final int COLOUR_SHOES = 3;
	
	static final byte[] HAT_COLOURS = new byte[] {-1, 12, 1, -23};  //white, blue, red, black(charcoal)
	static final byte[] SHIRT_COLOURS = new byte[] {-1, 4, 3, 5};  //white, blue, yellow, purple
	static final byte[] PANTS_COLOURS = new byte[] {-36, 88, -16, 100};  //beige, maroon, grey, vomit green 
	static final byte[] SHOES_COLOURS = new byte[] {7, -23, 124};  //white, black, redish
	
	//Vector2f position;
	Vector2f vel = new Vector2f(0,0);
	Vector2f target;
	
	byte[] colours = new byte[4];
	
	Texture t;
	
	double animTimer = 0;//time in seconds
	
	int framex = 0;
	int framey = 0;
	int state = STATE_STANDING; // standing 0, hakking 1, walking 2
	
	float hakkBpm = 1; // bpm to hakk at
	
	double hakkRandomizerF = 0;
	double hakkRandomizerO = 0;
	
	public EntityCharacter(float x, float y, Texture t)
	{
		position = new Vector2f(x,y);
		target = new Vector2f(position);
		this.t = t;
		
		//pesh hits shit:
		Random r = new Random();
		//r.nextBytes(colours);
		colours[COLOUR_HAT]  = HAT_COLOURS[r.nextInt(HAT_COLOURS.length)];
		colours[COLOUR_SHIRT] = SHIRT_COLOURS[r.nextInt(SHIRT_COLOURS.length)];
		colours[COLOUR_PANTS] = PANTS_COLOURS[r.nextInt(PANTS_COLOURS.length)];
		colours[COLOUR_SHOES] = SHOES_COLOURS[r.nextInt(SHOES_COLOURS.length)];
	}
	
	public synchronized void UpdateHakk(boolean hakk, float bpm)
	{
		if(hakk)
		{
			animTimer = 0;
			state = STATE_HAKKING;
			if(bpm <= 0) bpm = 1;
			hakkBpm = bpm;
			
			Random rand = new Random();
			hakkRandomizerF = (rand.nextDouble() + 1d); // 1 to 2;
			hakkRandomizerO = (rand.nextDouble() - 0.5 * 2) * 2 * Math.PI;
			
			if(rand.nextBoolean())//if start on left foot (50% chance)
				animTimer += 60d/hakkBpm; // move the timer forward 1 beat
		}
		else
		{
			state = STATE_STANDING;
		}
	}
	
	public void WalkTo(Vector2f vec)
	{
		state = STATE_WALKING;
		animTimer = 0;
		target = new Vector2f(vec);
		//elapsedTime = 0;
	}
	
	public void Draw() // draws the character
	{
		GraphicsResources.characterPainter.draw(new Vector3f(position.x,position.y,0), framex, framey,colours);
	}
	public void Update() 
	{
		if(state == STATE_STANDING)//if standing
		{
			framex = 0;
			framey = 1;
		}
		else if(state == STATE_HAKKING)//if hakking
		{
			animTimer += deltaTime;
			
			double divisor = (60d/hakkBpm)*2d; //length of 2 beats at this bpm in seconds;
			double multiplier = 8d / divisor;//result of division is multiplied by this to get 8 integer states
			
			double randomInfluence = Math.sin(hakkRandomizerF * animTimer + hakkRandomizerO);
			
			framex = (int)Math.floor(((animTimer + randomInfluence * 0.07) % divisor)*multiplier); // 0-> 7
			framey = 0;
			
			if(framex < 3) framex = 0;
			else if(framex == 3) framex = 2; // not actually needed lel
			else if(framex == 7) framex = 2;
			else if(framex > 3) framex = 1;
		} 
		else if (state == STATE_WALKING)//if walking
		{
			animTimer += deltaTime;
			framex = (int)Math.floor((animTimer % 0.8d)*5d); // 0-> 3
			framey = 1;
			
			if(framex == 0) framex = 1;
			else if(framex == 1) framex = 0; // not actually needed lel
			else if(framex == 2) framex = 2;
			else if(framex == 3) framex = 0;
			
			Vector2f.sub(target, position, vel);
			if(vel.length() <= 0.1f)
			{
				state = 0;//finished walking go back to standing
				vel.set(0, 0);
			}
			else
			{
				vel.normalise();
				vel.scale(1.5f);
			}
		}
		Vector2f scaleVec = (Vector2f) new Vector2f(vel).scale((float) deltaTime);
		Vector2f.add(position, (Vector2f) scaleVec, position);
		
		this.calcDepth(new Vector3f(position.x,position.y,0));
	}
}

