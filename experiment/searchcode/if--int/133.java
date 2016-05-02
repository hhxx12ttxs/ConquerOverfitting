// Ship.java
// Jim Sproch
// Created: April 29, 2006
// Modified: February 20, 2008
// Part of the Aforce Port
// Mac < Windows < Linux

/**
	Ships are the little things that fly around and shoot :) !
	@author Jim Sproch
	@version 0.1a beta
*/

import java.awt.*;
import javax.swing.*;
import java.util.*;

public class Ship extends JComponent implements MovableObject
{
	public static Ship userShip = null;
	public static Ship oldUserShip = null;

	Size mysize;
	Location mylocation;
	int myid;
	Status mystatus;
	boolean moving;
	int myai = 0;
	Laser mylaser = null;

	double initialVelocity = 0.07;  // Default: 0.07
	double initialAcceleration = 0.005; // Default: 0.005
	int distanceTraveled = 0;

	Mine mymine;

	AForceEnv field;

	ArrayList<Boundary> boundaries = new ArrayList<Boundary>();


	// Because this object is Serializable because it extends JComponent
	private static final long serialVersionUID = 7526471155622776147L;

	int direction;
	int turndirection;
	boolean stoped;
	boolean computerstoped;
	boolean userstoped;
	int myteam;
	Picture mypicture;

	Bullet mybullet;

	Ship(Picture picture, Size size, Location location, int team)
	{
		mypicture = picture;
		myteam = team;
		userstoped = false;
		stoped = false;
		mysize = size;
		mylocation = location;
		direction = Direction.WEST;
		moving = false;
		computerstoped = false;
		turndirection = -1;

		boundaries.add(new Boundary(location, mysize.getoffsetx(), mysize.getoffsety(), Direction.EAST, mysize.getx(), this));
		boundaries.add(new Boundary(location, mysize.getoffsetx(), mysize.gety()+mysize.getoffsety(), Direction.EAST, mysize.getx(), this));
		boundaries.add(new Boundary(location, mysize.getoffsetx(), mysize.getoffsety(), Direction.SOUTH, mysize.gety(), this));
		boundaries.add(new Boundary(location, mysize.gety()+mysize.getoffsety(), mysize.getoffsetx(), Direction.SOUTH, mysize.gety(), this));

		setstatus(new Status(this));

		// New ships are created each level.  The first time around elapsed time will be zero,
		//  so the distance traveled will also be zero and everything starts normally.
		distanceTraveled = (int)(getVelocity()*AForce.clicker.getElapsedTime());
	}



	Ship(Picture picture, Size size, Location location, int direction, int team)
	{
		this(picture, size, location, team);
		this.direction = direction;
	}

	public static Ship getUserShip()
	{
		return userShip;
	}



	public void setstatus(Status status)
	{
		mystatus = status;
	}

	@Deprecated
	public int getdirection()
	{
		return getDirection();
	}

	public int getAILevel()
	{
		return myai;
	}

	public void setAILevel(int level)
	{
		myai = level;
	}


	public void warp()
	{
		if(Commander.WeaponsControl.canUse(this, Warp.class))
		{
			distanceTraveled -= 50;
			Commander.WeaponsControl.register(this, new Warp(this));
		}
		else Printer.wrn.println("WARNING 59: Warp Drive has not had time to cool down #Ship.warp();");
	}

	public int getDirection()
	{
		return direction;
	}

	@Deprecated
	public Boundary relaventboundary(int direction)
	{
		return relaventBoundary(direction);
	}

	@Deprecated
	public boolean isdestroyable()
	{
		return true;
	}

	public Boundary relaventBoundary(int direction)
	{
		if(direction == Direction.NORTH) return boundaries.get(0);
		if(direction == Direction.SOUTH) return boundaries.get(1);
		if(direction == Direction.WEST) return boundaries.get(2);
		if(direction == Direction.EAST) return boundaries.get(3);

		Printer.err.println("ERROR 514: Direction could not be detected! Returning Null! #Ship.relaventboundary()");
		return null;
	}

	public boolean isDestroyable()
	{
		return true;
	}

	public void destroy()
	{
		this.getStatus().setHealth(0);
		if(this.getStatus().getHealth() != 0) return; // Probably means we just used up a life

		field.removeObject(this);

		if(this == userShip)
		{
			Printer.debug.println("UserShip Dead!");
			if(!AForce.clicker.isRunning()) AForce.pause();
			Printer.debug.println("paused, giving points!");
			field.getAForce().getScoreBoard().add(field.getAForce(), AForceEnv.getLevel(), field.getScore(getTeam()));
			Printer.debug.println("returning");
			return;
		}
	}

	@Deprecated
	public Status getstatus()
	{
		return getStatus();
	}

	public Status getStatus()
	{
		return mystatus;
	}


	public Size getsize()
	{
		return mysize;
	}

	@Deprecated
	public ArrayList<Boundary> getboundaries()
	{
		return boundaries;
	}

	public ArrayList<Boundary> getBoundaries()
	{
		return boundaries;
	}

	public Location getlocation()
	{
		return mylocation;
	}

	@Deprecated
	public void setteam(int team)
	{
		myteam = team;
	}

	@Deprecated
	public int getteam()
	{
		return myteam;
	}


	public void setTeam(int team)
	{
		myteam = team;
	}

	public int getTeam()
	{
		return myteam;
	}

	public double getVelocity()
	{
		return initialVelocity+initialAcceleration*((double)AForceEnv.getLevel());
	}

	@Deprecated
	public void setfield(AForceEnv field)
	{
		this.field = field;
	}

	public void setField(AForceEnv field)
	{
		this.field = field;
	}

	@Deprecated
	public AForceEnv getfield()
	{
		return field;
	}

	public AForceEnv getField()
	{
		return field;
	}

	public void move()
	{
		// No need to check for a collision if we aren't moving, just uses lots of cpu
		if(!userstoped && !computerstoped) field.collisionCheck(this);

		if(getStatus().getHealth() == 0) return;
		turn();

		if(getStatus().getHealth() == 0) return;

		while(0 < AForce.clicker.getElapsedTime()*getVelocity() - distanceTraveled)
		{
			turn();
			if(!userstoped && !computerstoped) mylocation.move(direction);
			if(!userstoped && !computerstoped) field.collisionCheck(this);
			if(getStatus().getHealth() == 0) return;
			if(myai != 0) ai();
			distanceTraveled++;
		}

		if(mylaser != null) mylaser.update();
	}

	@Deprecated
	public int getid()
	{
		if(this == userShip) return 1;
		return myid;
	}

	public int getID()
	{
		if(this == userShip) return 1;
		return myid;
	}

	public String toString()
	{
		return "Ship @ " + mylocation;
	}

	public void reverse()
	{
		direction = Direction.reverse(direction);
	}


	public void paint(Graphics g)
	{
		g.drawImage(mypicture.getImage(direction), mylocation.getx(), mylocation.gety(), this);
	}

	public void setUserShip()
	{
		setUserShip(this);
	}

	public static void setUserShip(Ship ship)
	{
		oldUserShip = userShip;
		userShip = ship;
		// Null ship means the ship has just died :(, but that is ok, because we will make a new one :)
		if(userShip != null)
		{
			userShip.setstatus(new Status(userShip));
			userShip.getStatus().setLives(3);
		}
	}

	public void turn(int direction)
	{
	//	stoped = false;
		userstoped = false;
	//	computerstoped = false;
		turndirection = direction;
	}

	private synchronized void turn()
	{
		if(turndirection == -1) return;

		if(turndirection != this.direction && !field.collisionCheck(this, turndirection))
		{
			
			stoped = false;
			userstoped = false;
			computerstoped = false;
			if(getStatus().getHealth() == 0) return;
			this.direction = turndirection;
			turndirection = -1;
		}
	}

	public void shoot()
	{
		if(mybullet == null)
		{
			Location bulletlocation = null;  //bullet location should be fixed up (placed 20060526)
			if(direction == Direction.NORTH) bulletlocation = new Location(mylocation.getx()+mysize.getx()/2+mysize.getoffsetx(), mylocation.gety());
			if(direction == Direction.SOUTH) bulletlocation = new Location(mylocation.getx()+mysize.getx()/2, mylocation.gety()+mysize.gety()-1);
			if(direction == Direction.WEST) bulletlocation = new Location(mylocation.getx()+1, mylocation.gety()+mysize.gety()/2);
			if(direction == Direction.EAST) bulletlocation = new Location(mylocation.getx()+mysize.getx()-1, mylocation.gety()+mysize.gety()/2);

			mybullet = new Bullet(new Size(2,2,1,1), bulletlocation, direction);
			mybullet.setOwner(this);
			mybullet.setTeam(myteam);

			String bulletPicturePath;
			if(this == userShip) bulletPicturePath = "bullet_4x4_1x1_grey";
			else if(this.getteam() == userShip.getteam()) bulletPicturePath = "bullet_4x4_1x1_green";
			else bulletPicturePath = "bullet_4x4_1x1_yellow";
			mybullet.setPicture(Images.getPicture(bulletPicturePath));

			field.addobject(mybullet);
		}
		else Printer.wrn.println("WARNING 545: Only one bullet permitted per ship #Ship.shoot();");
	}


	public void remoteDetonator()
	{
		if(mymine == null)
		{
			Location remotelocation = new Location(mylocation.getx(), mylocation.gety());

			RemoteDetonator rd = new RemoteDetonator(new Size(25,25), remotelocation);
			rd.setOwner(this);
			rd.setTeam(myteam);
			rd.setField(field);

			field.addobject(rd);
		}
		else Printer.wrn.println("WARNING 549: Only one mine permitted per ship #Ship.placemine();");
	}

	public void timeBomb()
	{
		Location bomblocation = new Location(mylocation.getx(), mylocation.gety());

		TimeBomb tb = new TimeBomb(new Size(25,25), bomblocation);
		tb.setOwner(this);
		tb.setTeam(myteam);
		tb.setField(field);

		field.addobject(tb);
	}


	public void xshoot()
	{
		if(false)
		{
			String bulletPicturePath;
			if(this == userShip) bulletPicturePath = "bullet_4x4_1x1_grey";
			else if(this.getteam() == userShip.getteam()) bulletPicturePath = "bullet_4x4_1x1_green";
			else bulletPicturePath = "bullet_4x4_1x1_yellow";

			Location bulletlocation = null;  //bullet location should be fixed up (placed 20060526)
			Bullet bullet = null;

			bulletlocation = new Location(mylocation.getx()+mysize.getx()/2+mysize.getoffsetx(), mylocation.gety());
			bullet = new Bullet(new Size(2,2,1,1), bulletlocation, Direction.NORTH);
			bullet.setOwner(this);
			bullet.setTeam(myteam);
			bullet.setPicture(Images.getPicture(bulletPicturePath));
			field.addobject(bullet);

			bulletlocation = new Location(mylocation.getx()+mysize.getx()/2, mylocation.gety()+mysize.gety()-1);
			bullet = new Bullet(new Size(2,2,1,1), bulletlocation, Direction.SOUTH);
			bullet.setOwner(this);
			bullet.setTeam(myteam);
			bullet.setPicture(Images.getPicture(bulletPicturePath));
			field.addobject(bullet);

			bulletlocation = new Location(mylocation.getx()+1, mylocation.gety()+mysize.gety()/2);
			bullet = new Bullet(new Size(2,2,1,1), bulletlocation, Direction.EAST);
			bullet.setOwner(this);
			bullet.setTeam(myteam);
			bullet.setPicture(Images.getPicture(bulletPicturePath));
			field.addobject(bullet);

			bulletlocation = new Location(mylocation.getx()+mysize.getx()-1, mylocation.gety()+mysize.gety()/2);
			bullet = new Bullet(new Size(2,2,1,1), bulletlocation, Direction.WEST);
			bullet.setOwner(this);
			bullet.setTeam(myteam);
			bullet.setPicture(Images.getPicture(bulletPicturePath));
			field.addobject(bullet);


		}
		else Printer.wrn.println("WARNING 545: Only one bullet permitted per ship #Ship.xshoot();");
	}

	public void tshoot()
	{
		if(false)
		{

			String bulletPicturePath;
			if(this == userShip) bulletPicturePath = "bullet_4x4_1x1_grey";
			else if(this.getteam() == userShip.getteam()) bulletPicturePath = "bullet_4x4_1x1_green";
			else bulletPicturePath = "bullet_4x4_1x1_yellow";

			for(int x = 0; x < 3; x++)
			{
				Location bulletlocation = null;  //bullet location should be fixed up (placed 20060526)
				if(direction == Direction.NORTH) bulletlocation = new Location(mylocation.getx()+mysize.getx()/2+mysize.getoffsetx(), mylocation.gety());
				if(direction == Direction.SOUTH) bulletlocation = new Location(mylocation.getx()+mysize.getx()/2, mylocation.gety()+mysize.gety()-1);
				if(direction == Direction.WEST) bulletlocation = new Location(mylocation.getx()+1, mylocation.gety()+mysize.gety()/2);
				if(direction == Direction.EAST) bulletlocation = new Location(mylocation.getx()+mysize.getx()-1, mylocation.gety()+mysize.gety()/2);
		
				Bullet bullet = new Bullet(new Size(2,2,1,1), bulletlocation, direction);
				bullet.setOwner(this);
				bullet.setTeam(myteam);
				bullet.setPicture(Images.getPicture(bulletPicturePath));
				field.addobject(bullet);

				try{ Thread.sleep(200); }catch(Exception e){}
			}



		}
	}

	public void cshoot()
	{
		if(false)
		{
			for(int x = 0; x < 100; x++)
			{
				xshoot();
				try{ Thread.sleep(500); }catch(Exception e){}
			}
		}
	}


	public void placemine()
	{
		if(mymine == null)
		{
			Location minelocation = null;  //bullet location should be fixed up (placed 20060526)
			minelocation = new Location(mylocation.getx(), mylocation.gety());

			mymine = new Mine(new Size(25,25), minelocation);
			mymine.setOwner(this);
			mymine.setTeam(myteam);
			mymine.setField(field);

			String bulletPicturePath = "mine";
			mymine.setPicture(Images.getPicture(bulletPicturePath));

			field.addobject(mymine);
		}
		else Printer.wrn.println("WARNING 549: Only one mine permitted per ship #Ship.placemine();");
	}


	public void laser()
	{
		laser(true);
	}


	public void laser(boolean toKill)
	{
		if(!toKill || Commander.WeaponsControl.canUse(this, Laser.class))
		{
			if(!toKill) mylaser = new Laser(this, Color.blue);
			else mylaser = new Laser(this);
			if(!toKill) mylaser.setReal(false);
			if(mylaser != null) mylaser.update();
			if(toKill) Commander.WeaponsControl.register(this, mylaser);
			if(toKill) field.addobject(mylaser);
		}
		else Printer.wrn.println("WARNING 058: Laser has not had enough time to charge #Ship.laser();");
	}


	public void setbullet(Bullet bullet)
	{
		mybullet = bullet;
	}



	public void setmine(Mine mine)
	{
		mymine = mine;
	}

	public void autopilot()
	{
		if(myai > 0) autopilot(0);
		else autopilot(1);
	}

	public void autopilot(int ai)
	{
		myai = ai;
	}

	public void setLaser(Laser laser)
	{
		mylaser = laser;
	}

	public Laser getLaser()
	{
		return mylaser;
	}

	public void stop()
	{
		stoped = true;
		userstoped = true;
	}

	public void stop(int reason)
	{
		stoped = true;
		computerstoped = true;
	}

	@Deprecated
	public AbstractObject getowner()
	{
		return null;
	}

	public AbstractObject getOwner()
	{
		return null;
	}


	public void ai()
	{
		boolean[] cango = new boolean[13];
		int turningprob = 5; // used to keep ships away from edges


		if(mylocation.gety() <= 0)
		{
			cango[Direction.NORTH] = false;
			turningprob = 3;
		} else cango[Direction.NORTH] = true;

		if(mylocation.getx() <= 0)
		{
			cango[Direction.SOUTH] = false;
			turningprob = 3;
		} else cango[Direction.SOUTH] = true;

		if(mylocation.gety() >= 500)
		{
			cango[Direction.WEST] = false;
			turningprob = 3;
		} else cango[Direction.WEST] = true;

		if(mylocation.getx() >= 500)
		{
			cango[Direction.EAST] = false;
			turningprob = 3;
		} else cango[Direction.EAST] = true;

		int tempdirection = -1;  // so we can see if a change was made
	//	if((mylocation.gety() %50 == 0 && mylocation.getx() %50 == 0) || (!userstoped && stoped))
			if((((int)(Math.random()*8454761) %(turningprob*50)) == 0) || (!userstoped && stoped))
				do tempdirection = ((int)(Math.random()*8461%4+1))*3;
				while(!cango[tempdirection]);
			if(tempdirection != -1) turn(tempdirection);

		
		if(((int)(Math.random()*8454761) % (1000 - AForceEnv.getLevel())) == 0) shoot();
		if(((int)(Math.random()*8454761) % (3000 - AForceEnv.getLevel())) == 0) placemine();
		if(((int)(Math.random()*8454761) % (3000 - AForceEnv.getLevel())) == 0 && this.clearLaserShot()) {laser(); AForce.getClicker().stop();}
	}


	private boolean clearLaserShot()
	{
		laser(false);
		ArrayList<AbstractObject> toCheck = mylaser.inRange();
		mylaser = null;
		boolean enemyInShot = false;


		for(int i = 0; i < toCheck.size(); i++)
		{
			if(!(toCheck.get(i) instanceof Ship)) continue;
			if(toCheck.get(i).getTeam() == this.getTeam()) return false;
			if(toCheck.get(i).getTeam() != this.getTeam()) return true;
		}

		return enemyInShot;
	}


	public static void main(String[] args)
	{
		Printer.noexecute();
	}


}
