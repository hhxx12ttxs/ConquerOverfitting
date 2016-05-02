package com.me.canyonbunny.game_objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.me.canyonbunny.game_objects.BunnyHead.JUMP_STATE;
import com.me.mygdxgame.Assets;

public class Hook extends AbstractGameObject {
	private static BunnyHead bunny;
	private Array<Link> links;
	private int numLinks;
	private float timeBetweenChains, maxTime = .02f, deChainTime = .025f;
	public static float hookCD;
	public boolean chaining, deChaining;
	boolean hit;
	private Rock rock;
	public static float angle;
	public boolean swungThisHook;
	private Vector2 hookPull;
	Vector2 target, currentVector;
	public Hook(BunnyHead bunny){
		hit= false;
		chaining = false;
		swungThisHook = false;
		deChaining = false;
		Hook.bunny = bunny;
		links = new Array<Link>();
		timeBetweenChains = 0;
		target = new Vector2();
		hookPull = new Vector2();
		numLinks = 0;
		hookCD = 0;
		
	}
	public Hook() {
		// TODO Auto-generated constructor stub
	}
	public void startHook(Rock rock){
		if(rock != null && hookCD <= 0){
			chaining = true;
			this.rock = rock;
			
			target.set(rock.position.x, rock.position.y + rock.bounds.height / 2);
			timeBetweenChains = 0;
		}
		
	}
	public void stopHook(){
		chaining = false;
		deChaining = true;
		timeBetweenChains = 0;
		
	}
	public Vector2 getRockVector(){
		return target;
	}
	public Link getLastLink(){
		if(links.size > 0)
			return links.peek();
		return null;
	}
	public int getNumLinks(){
		return numLinks;
	}
	public float getAngle(){
		return (float)(Math.atan2((double)( (bunny.position.y + bunny.bounds.height / 2) - target.y), (double)((bunny.position.x + bunny.bounds.width) - target.x)));
	}
	public void hit(){
		Vector2 current = new Vector2(rock.position.x + rock.bounds.width / 2 - bunny.bounds.width / 2, rock.position.y + rock.bounds.height / 2);
		bunny.jumpState = JUMP_STATE.ZIPPING;
		bunny.terminalVelocity.y = 0;
		bunny.terminalVelocity.x = 0;
		hookPull.set(current.sub(bunny.position));
		hookPull.set(hookPull.x / numLinks, hookPull.y / numLinks);
		hit = true;
	}
	public Rectangle getRockRect(){
		return new Rectangle(rock.position.x - .2f, rock.position.y - .2f, rock.bounds.width + .4f, rock.bounds.height + .4f);
	}
	public double getRadiusForSwing(){
		return Math.sqrt(Math.pow(numLinks * .2f * Math.cos(angle), 2) + Math.pow(numLinks * .1f * Math.sin(angle), 2));
		//return Math.sqrt(Math.pow((double)(target.y - (bunny.position.y + bunny.bounds.height / 2)), 2) + Math.pow((double)(target.x - (bunny.position.x + bunny.bounds.width)), 2));
	}
	
	
	private void makeNewLink(){
		links.add(new Link(links.size));
		numLinks++;
		if(numLinks > 20)
			stopHook();
		
	}
	private void removeLastLink(){
		
		if(links.size > 0){
			links.pop();
			numLinks--;
			
			//If hook hit a ledge, reel the player in
			if(hit){
				bunny.position.x += hookPull.x;
				bunny.position.y += hookPull.y;
			}
			
		}else{
			//No links left, assure the bunny lands on top of the ledge
			if(hit){
				bunny.position.x = rock.position.x + rock.bounds.width / 2 - bunny.bounds.width / 2;
				bunny.position.y = rock.position.y + rock.bounds.height - .01f;
				hookCD = 4;
				swungThisHook = false;

			}
			//Reset variables for the next grapple
			deChaining = false;
			bunny.jumpState = JUMP_STATE.JUMP_RISING;
			rock = null;
			hit = false;
		}
		
	}
	@Override
	public void update(float deltaTime){
		if(chaining){

			angle = (float) Math.atan2((double)(target.y - (bunny.position.y + bunny.bounds.height / 2)), (double)(target.x - (bunny.position.x + bunny.bounds.width)));
			timeBetweenChains += deltaTime;
			while(timeBetweenChains  > maxTime){
				makeNewLink();
				timeBetweenChains -= maxTime;
			}
			for(int i = 0; i < links.size; i++){
				links.get(i).update(deltaTime);
			}
			
		}else if(deChaining){

			angle = (float) Math.atan2((double)(target.y - (bunny.position.y + bunny.bounds.height / 2)), (double)(target.x - (bunny.position.x + bunny.bounds.width)));

			timeBetweenChains += deltaTime;
			while(timeBetweenChains > deChainTime){
				removeLastLink();
				timeBetweenChains -= deChainTime;
			}
			for(int i = 0; i < links.size; i++){
				links.get(i).update(deltaTime);
			}
		}else if(bunny.jumpState == JUMP_STATE.SWINGING){
			angle = (float) Math.atan2((double)(target.y - (bunny.position.y + bunny.bounds.height / 2)), (double)(target.x - (bunny.position.x + bunny.bounds.width)));

			for(int i = 0; i < links.size; i++){
				links.get(i).update(deltaTime);
			}
		}
		if(hookCD > 0){
			hookCD -= deltaTime;
		}
		
	}
	
	@Override
	public void render(SpriteBatch batch) {
		for(int  i = 0; i < links.size; i++){
			links.get(i).render(batch);
		}
	}
	
	public class Link extends AbstractGameObject{
		private int spotNumber;
		private TextureRegion reg; 
		
		public Link(){
			reg = Assets.instance.link.link;
			dimension.set(.2f, .1f);
			bounds.set(0, 0, dimension.x, dimension.y);
			origin.set(0, 0);
		}
		public Link(int spotNumber){
			reg = Assets.instance.link.link;
			this.spotNumber = spotNumber;
			dimension.set(.2f, .1f);
			bounds.set(0, 0, dimension.x, dimension.y);
			origin.set(0, 0);

		}
		
		@Override
		public void update(float deltaTime){
			this.rotation = (float) (Hook.angle * 180 / Math.PI);
			float x = (float)(Math.cos((double)(Hook.angle)));
			float y = (float)(Math.sin((double)(Hook.angle)));
			
			this.position.set(Hook.bunny.position.x + Hook.bunny.bounds.width + spotNumber * x * bounds.width,
					Hook.bunny.position.y + Hook.bunny.bounds.height / 2 + spotNumber * y * bounds.height);
		}
		@Override
		public void render(SpriteBatch batch) {
			batch.draw(reg.getTexture(), position.x, position.y, origin.x, origin.y, bounds.width, bounds.height, scale.x,
					scale.y, rotation, reg.getRegionX(), reg.getRegionY(), reg.getRegionWidth(), reg.getRegionHeight(), false, false);
		}
	}
	

}

