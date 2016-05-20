package com.companyname.blockgame;

import java.awt.*;
import java.util.*;

import static com.companyname.blockgame.Constants.*;

public class Mob extends DoubleRectangle {
	public int[] id;
	public int arrayId = 0;
	
	public double maxSpeed = .5;
	public double speed = maxSpeed;
	public double knockBackSpeed = 5;
	public boolean isJumping = false;
	public boolean isMoving = false;
	public boolean isFalling = false;

	public int maxHP = 10;
	public int HP = maxHP;
	
	
	
	public double movementSpeed = 0.4;
	public double fallingSpeed = 1;
	public double jumpingSpeed = 8;
	//public double dir = movementSpeed;
	public int jumpingHeight = 8, jumpingCount = 0;
	public int animation = 0;  
	public int animationFrame = 0, animationTime = 25;
	public int step = 0;
	public int[] stepArr = {0,1,0,2};
	public int stepsToTake = 0;
	public int dir;
	public Random rand = new Random();
	public Random rand2 = new Random();
	
	public boolean isKnockingBack = false;
	
	public double moveX = 0;
	public double moveY = 0;
	
	public double vX;
	public double vY;
	
	public static double frameOffsetLeft = 6;
	public static double frameOffsetRight = 17;
	public static double frameOffsetBottom = 31;
	public static double frameOffsetTop = 20;
	
	public static boolean isMovingUp = false;
	public static boolean isMovingDown = false;
	public static boolean isMovingRight = false;
	public static boolean isMovingLeft = false;
	
	public static boolean isFacingUp = false;
	public static boolean isFacingDown = true;
	public static boolean isFacingRight = false;
	public static boolean isFacingLeft = false;
	
	
	public Mob(int x, int y, int width, int height, int[] id, int idOfArray) {
		setBounds(x,y,width,height);
		this.id = id;
		arrayId = idOfArray;
	}
	
	public Rectangle bounds() {
		return (new Rectangle((int)x - (int) Component.sX+ (int) frameOffsetLeft,(int)y+(int)frameOffsetTop- (int)Component.sY,(int)frameOffsetRight- (int) frameOffsetLeft,(int)frameOffsetBottom-(int)frameOffsetTop));
	}
	
	public double getCollisionX() {
		double horizontalExtra = 0;
		double x1 = Math.floor((x+frameOffsetLeft + moveX) /TILE_SIZE-(Component.level.chunkOffsetX * CHUNCK_SIZE));
		double x3 = Math.floor((x+frameOffsetLeft + moveX) /TILE_SIZE);
		double x2 = Math.floor((x+frameOffsetRight + moveX) /TILE_SIZE-(Component.level.chunkOffsetX * CHUNCK_SIZE));
		double y1 = Math.floor((y+frameOffsetTop) /TILE_SIZE-(Component.level.chunkOffsetY * CHUNCK_SIZE));
		double y2 = Math.floor((y+frameOffsetBottom) /TILE_SIZE-(Component.level.chunkOffsetY * CHUNCK_SIZE));
		
		if(isMovingLeft) {
			if(Component.level.block[(int)x1][(int)y1].isPassable == false || Component.level.block[(int)x1][(int)y2].isPassable == false
					||Component.level.block2[(int)x1][(int)y1].isPassable == false || Component.level.block2[(int)x1][(int)y2].isPassable == false) {
				horizontalExtra = (x + frameOffsetLeft) - x3 * TILE_SIZE - TILE_SIZE;
				return Math.min(0, -(horizontalExtra));
			}
		}
		if(isMovingRight) {
			if(Component.level.block[(int)x2][(int)y1].isPassable == false || Component.level.block[(int)x2][(int)y2].isPassable == false
					|| Component.level.block2[(int)x2][(int)y1].isPassable == false || Component.level.block2[(int)x2][(int)y2].isPassable == false) {
				horizontalExtra = x2 * TILE_SIZE - (x + frameOffsetRight) - 1;
				return Math.max(0, horizontalExtra);
			}
		}
		return moveX;
	}
	
	public double getCollisionY() {
		double verticalExtra = 0;
		double x1 = Math.floor((x+frameOffsetLeft) /TILE_SIZE-(Component.level.chunkOffsetX * CHUNCK_SIZE));
		double x2 = Math.floor((x+frameOffsetRight) /TILE_SIZE-(Component.level.chunkOffsetX * CHUNCK_SIZE));
		double y1 = Math.floor((y+frameOffsetTop + moveY) /TILE_SIZE-(Component.level.chunkOffsetY * CHUNCK_SIZE));
		double y3 = Math.floor((y+frameOffsetTop + moveY) /TILE_SIZE);
		double y2 = Math.floor((y+frameOffsetBottom + moveY) /TILE_SIZE-(Component.level.chunkOffsetY * CHUNCK_SIZE));
		
		if(isMovingDown) {
			if(Component.level.block[(int)x2][(int)y2].isPassable == false || Component.level.block[(int)x1][(int)y2].isPassable == false
					|| Component.level.block2[(int)x2][(int)y2].isPassable == false || Component.level.block2[(int)x1][(int)y2].isPassable == false) {
				verticalExtra = y2 * TILE_SIZE - (y + frameOffsetBottom) - 1;
				return Math.max(0, verticalExtra);
				
			}
		}
		if(isMovingUp) {
			if(Component.level.block[(int)x1][(int)y1].isPassable == false || Component.level.block[(int)x2][(int)y1].isPassable == false
					|| Component.level.block2[(int)x1][(int)y1].isPassable == false || Component.level.block2[(int)x2][(int)y1].isPassable == false) {
				verticalExtra = (y + frameOffsetTop) - y3 * TILE_SIZE - TILE_SIZE;
				return Math.min(0, -(verticalExtra));
			}
		}
		return moveY;	
	}
	
	public void checkDeath() {
		if(HP <= 0) {
			for(int i = 0; i < Component.mob.toArray().length; i ++) {
				if(Component.mob.get(i).arrayId == arrayId) {
					//Inventory.addToInventory(id, 1);
					Component.mob.remove(i);
					System.out.println("DEATH FOR MOB" + i);
				}
			}
			
		}
	}
	public void knockBack() {
//		x = x+(vX*knockBackSpeed);
//		y = y+(vY*knockBackSpeed);
		//getKnockBackDir
		float angle = (float) Math.atan2(((y+24- (int) Component.sY) - (Component.character.y+20-(int) Component.sY)), 
				((x+8 -(int) Component.sX) - (Component.character.x+8 - (int) Component.sX)));
		//Convert the Radian to degrees
		angle = angle * 180 / (int) Math.PI;
		if(angle < 0){
	        angle += 360;
	    } 
		dir = (int) angle;
		speed = knockBackSpeed;
		stepsToTake = 5;
		//isKnockingBack = false;
	}
	
	public void tick() {
		//Collision with main character
		if(Component.character.isKnockingBack == false) {
			Rectangle rectangle3 = Component.character.bounds();
			Rectangle rectangle4 = bounds();
			if (rectangle3.intersects(rectangle4)) {
				Component.character.HP = Component.character.HP - 1;
				
				Component.character.isKnockingBack = true;
				Component.character.knockBack((int)x,(int)y);
				
			}
			
		}
		//Collision with main characters weapon.  
		if(Component.weapon.isInUse == true && Component.weapon.isPositionedCorrectly == true && isKnockingBack == false) {
			Rectangle rectangle1 = Component.weapon.collisionArea;
			Rectangle rectangle2 = bounds();
			if (rectangle1.intersects(rectangle2)) {
				//System.out.println("Collission Detected");
				
				HP = HP -1;
				System.out.println(HP);
				
				checkDeath();
				isKnockingBack = true;
				knockBack();
			}
		}
			
		

			if(stepsToTake == 0) {
				if(isKnockingBack == true) {
					isKnockingBack = false;
				}
				speed = maxSpeed;
				//Calculate a random number of steps
				stepsToTake = rand.nextInt(50) + 20;
				//Calculate a random direction
				dir = rand2.nextInt(360) + 1;
				
				
			}
		
		stepsToTake = stepsToTake - 1;
		//stepsToTake = 1;
		
		
		
		//dir = 270;
		
		
		
			if(dir > 180 && dir < 360) {
				Mob.isMovingUp = true;
				Mob.isMovingDown = false;
			}
			if(dir > 0 && dir < 180) {
				Mob.isMovingUp = false;
				Mob.isMovingDown = true;
			}
			if(dir > 90 && dir < 270) {
				Mob.isMovingLeft = true;
				Mob.isMovingRight = false;
			}
			if(dir > 270 || dir < 90) {
				Mob.isMovingLeft = false;
				Mob.isMovingRight = true;
			}
			
			
			
			
			if(dir > 225 && dir < 315) {
				Mob.isFacingUp = true;
				Mob.isFacingDown = false;
				Mob.isFacingLeft = false;
				Mob.isFacingRight = false;
				
			}
			if(dir > 45 && dir < 135) {
			
				Mob.isFacingDown = true;
				Mob.isFacingLeft = false;
				Mob.isFacingRight = false;
				Mob.isFacingUp = false;
			}
			if(dir > 135 && dir < 225) {
				Mob.isFacingLeft = true;
				Mob.isFacingDown = false;
				Mob.isFacingRight = false;
				Mob.isFacingUp = false;
				
			}
			if(dir > 315 || dir < 45) {
				Mob.isFacingRight = true;
				Mob.isFacingDown = false;
				Mob.isFacingLeft = false;
				Mob.isFacingUp = false;
			}
			
			if(Inventory.isOpen == false) {
				boolean canMove = false;
				moveX = 0;
				moveY = 0;
				
				
				double newX = ((int) x) + 100 * Math.cos(Math.toRadians(dir));
				double newY = ((int) y) + 100 * Math.sin(Math.toRadians(dir));
				
				vX = newX - x;
				vY = newY - y;
				
				double length = Math.sqrt((vX*vX)+(vY*vY));
				
				vX = vX/length;
				vY = vY/length;
				
				moveX = (vX*speed);
				moveY = (vY*speed);
	
				

				moveX = getCollisionX();
				moveY = getCollisionY();
				
	
				
				if(!canMove) {
					
					x += moveX;
					y += moveY;
					
					if(animationFrame >= animationTime) {
						
						
						if(step > 2) {
							step = 0;
						} else {
							step += 1;
							
						}
						animation = stepArr[step];
						animationFrame = 0;
					} else {
						animationFrame += 1;
					}
				}
				
			} else {
				animation = 0;
			}
			
		
	}
	
	public boolean isCollidingWithBlock(Point pt1, Point pt2) {
		
		for(int x=(int) (this.x/TILE_SIZE);x<(int) (this.x/TILE_SIZE) +3;x++) {
			for(int y=(int) (this.y/TILE_SIZE);y<(int) (this.y/TILE_SIZE) +3;y++) {
				if(x >= 0 && y>= 0 && x < Component.level.block.length && y < Component.level.block[0].length) {
					if(Component.level.block[x][y].id != AIR) {
						if(Component.level.block[x][y].contains(pt1) || Component.level.block[x][y].contains(pt2)) {
							return true;
							
						}
					}
				}
			}
		}
		return false;
	}
	
	public void render(Graphics g) {
//		Rectangle rectangle1 = Component.weapon.collisionArea;
//		Rectangle rectangle2 = bounds();
		
//		g.drawImage(ImageAssets.TERRAIN_IMAGE, 
//				(int) x - (int) Component.sX, 
//				(int) y - (int) Component.sY, 
//				(int) (x + width) - (int) Component.sX, 
//				(int) (y + height) - (int) Component.sY, 
//				/*Where it's cut out*/
//				(Tile.character[0] * 24) + (24 * Component.weapon.animation) +288, 
//				Tile.character[1] * TILE_SIZE, 
//				Tile.character[0] * 24 +(24 * Component.weapon.animation) + (int) width +288, 
//				Tile.character[1] * TILE_SIZE +(int) height, 
//				null);
		if(isFacingDown) {
			g.drawImage(ImageAssets.TERRAIN_IMAGE,
					(int) x - (int) Component.sX,
					(int) y - (int) Component.sY,
					(int) (x + width) - (int) Component.sX,
					(int) (y + height) - (int) Component.sY,
					(id[0] * 24) + (24*animation),
					id[1] * TILE_SIZE,
					id[0] * 24 +(24 * animation) + (int) width,
					id[1] * TILE_SIZE +(int) height,
					null);
		} else if(isFacingRight) {
			int a = animation + 3;
			g.drawImage(ImageAssets.TERRAIN_IMAGE,
					(int) x - (int) Component.sX,
					(int) y - (int) Component.sY,
					(int) (x + width) - (int) Component.sX,
					(int) (y + height) - (int) Component.sY,
					(id[0] * 24) + (24*a),
					id[1] * TILE_SIZE,
					id[0] * 24 +(24 * a) + (int) width,
					id[1] * TILE_SIZE +(int) height,
					null);
		} else if(isFacingUp) {
			int a = animation + 6;
			g.drawImage(ImageAssets.TERRAIN_IMAGE,
					(int) x - (int) Component.sX,
					(int) y - (int) Component.sY,
					(int) (x + width) - (int) Component.sX,
					(int) (y + height) - (int) Component.sY,
					(id[0] * 24) + (24*a),
					id[1] * TILE_SIZE,
					id[0] * 24 +(24 * a) + (int) width,
					id[1] * TILE_SIZE +(int) height,
					null);
		} else if(isFacingLeft) {
			int a = animation + 9;
			g.drawImage(ImageAssets.TERRAIN_IMAGE,
					(int) x - (int) Component.sX,
					(int) y - (int) Component.sY,
					(int) (x + width) - (int) Component.sX,
					(int) (y + height) - (int) Component.sY,
					(id[0] * 24) + (24*a),
					id[1] * TILE_SIZE,
					id[0] * 24 +(24 * a) + (int) width,
					id[1] * TILE_SIZE +(int) height,
					null);
		}
			
//			g.setColor(new Color(255,0,0, 255));
//			g.drawRect (bounds().x, bounds().y, bounds().width,bounds().height);
//			
//			//Weapon Collision detection
//			g.setColor(new Color(0,255,0, 255));
//			g.drawRect (Component.weapon.collisionArea.x,Component.weapon.collisionArea.y,Component.weapon.collisionArea.width,Component.weapon.collisionArea.height);
//			
//			//Bounding Box for wall collision
//			g.setColor(new Color(0,0,255, 255));
//			g.drawRect (
//					(int)x+(int)frameOffsetLeft,
//					(int)y+ (int)frameOffsetTop,
//					(int)frameOffsetRight-(int)frameOffsetLeft,
//					(int)frameOffsetBottom-(int)frameOffsetTop);
//			//Main Character hit collision detection
//			g.setColor(new Color(255,0,0, 255));
//			g.drawRect (Component.character.bounds().x,Component.character.bounds().y,Component.character.bounds().width,Component.character.bounds().height);
//		
//		
	}
}

