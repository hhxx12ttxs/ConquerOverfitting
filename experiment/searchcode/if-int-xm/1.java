package net.mdked.Jrachgame.core;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;

import net.mdked.Jrachgame.utils.Pifagor;
import net.mdked.Jrachgame.world.Animal;
import net.mdked.Jrachgame.world.Entity;
import net.mdked.Jrachgame.world.Missile;
import net.mdked.Jrachgame.world.MousePoint;
import net.mdked.Jrachgame.world.Player;
import net.mdked.Jrachgame.world.StaticObj;
import net.mdked.Jrachgame.world.World;


import org.newdawn.slick.Animation;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.Rectangle;

public class DasGame extends BasicGame{

	Image introbg, snow1, snow2, player, bear,
	deer, tree, stone, spearPlStand, spearGrnd, playerDead
	, deerDead, bearDead, scoreTable, gameOver, timer, ammo;

	Animation playerWalk, playerAttack, heart, spearWalk,
	spearFly, spearAttack, playerDyin;

	static Animation deerWalk, deerAttack, bearWalk, bearAttack,
	deerDyin, bearDyin;

	SpriteSheet ammonum, cutscene;

	public static Entity delCache;

	boolean isIntrd1, charDrawd, muteMusic, gOver, gameEnded, drawEnd, drawvoid;

	public static float xScale, time, timeEnd;

	int minleft, secleft, cuttimer, cutnum;

	World world;

	Image[][] map;

	Player playerC;

	ArrayList<Entity> upRen, downRen;

	static int scrWi;

	static int scrHe;

	static Sound plStep, deerStp, bearStp, swing, thrw, spHit, dmgs,
	deerAgr, deerAtk, deerDmg, deerDin, bearAgr,
	bearAtk, bearDmg, bearDin, maceHit, lyhye, intro, outro;

	Music polka;

	MousePoint mouz;

	public DasGame()
	{
		super("Lednikovyi Jrache");
	}

	@Override
	public void init(GameContainer gc) 
			throws SlickException {
		
		introbg = new Image("img/bg1.png",false,Image.FILTER_NEAREST);
		snow1 = new Image("img/snow.png",false,Image.FILTER_NEAREST);
		snow2 = new Image("img/snow2.png",false,Image.FILTER_NEAREST);
		player = new Image("img/player1.gif",false,Image.FILTER_NEAREST);
		bear = new Image("img/bear.gif",false,Image.FILTER_NEAREST);
		deer = new Image("img/deer.gif",false,Image.FILTER_NEAREST);
		tree = new Image("img/tree1.gif",false,Image.FILTER_NEAREST);
		stone = new Image("img/rock1.gif",false,Image.FILTER_NEAREST);
		spearPlStand = new Image("img/spear.gif",false,Image.FILTER_NEAREST);
		spearGrnd = new Image("img/speargr.gif",false,Image.FILTER_NEAREST);
		playerDead = new Image("img/plded.gif",false,Image.FILTER_NEAREST);
		deerDead = new Image("img/deerded.gif",false,Image.FILTER_NEAREST);
		bearDead = new Image("img/bearded.gif",false,Image.FILTER_NEAREST);
		scoreTable = new Image("img/sctable.gif",false,Image.FILTER_NEAREST);
		gameOver = new Image("img/GO.gif",false,Image.FILTER_NEAREST);
		timer = new Image("img/timer.gif",false,Image.FILTER_NEAREST);
		ammo = new Image("img/spearamm.gif",false,Image.FILTER_NEAREST);
	
		ammonum = new SpriteSheet(new Image("img/spearammn.gif",false,Image.FILTER_NEAREST), 32, 32);
		cutscene = new SpriteSheet(new Image("img/stry.png",false,Image.FILTER_NEAREST), 320, 240);

		playerWalk = new Animation(new SpriteSheet(new Image("img/playerwalk.gif",false,Image.FILTER_NEAREST), 32, 32),100);
		playerAttack = new Animation(new SpriteSheet(new Image("img/playerattack.gif",false,Image.FILTER_NEAREST), 32, 32),100);
		playerAttack.setLooping(false);
		deerWalk = new Animation(new SpriteSheet(new Image("img/deerwalk.gif",false,Image.FILTER_NEAREST), 32, 32),100);
		deerAttack = new Animation(new SpriteSheet(new Image("img/deerattack.gif",false,Image.FILTER_NEAREST), 32, 32),100);
		deerAttack.setLooping(false);
		bearWalk = new Animation(new SpriteSheet(new Image("img/bearwalk.gif",false,Image.FILTER_NEAREST), 32, 32),100);
		bearAttack = new Animation(new SpriteSheet(new Image("img/bearattack.gif",false,Image.FILTER_NEAREST), 32, 32),100);
		bearAttack.setLooping(false);
		heart =  new Animation(new SpriteSheet(new Image("img/heart.gif",false,Image.FILTER_NEAREST), 32, 32),200);
		spearWalk = new Animation(new SpriteSheet(new Image("img/playerwalkspear.gif",false,Image.FILTER_NEAREST), 32, 32),100);
		spearFly = new Animation(new SpriteSheet(new Image("img/spearfly.gif",false,Image.FILTER_NEAREST), 32, 32),300);
		spearAttack = new Animation(new SpriteSheet(new Image("img/spearthrow.gif",false,Image.FILTER_NEAREST), 32, 32),100); 
		spearAttack.setLooping(false);
		playerDyin = new Animation(new SpriteSheet(new Image("img/pldyn.gif",false,Image.FILTER_NEAREST), 32, 32),100); 
		playerDyin.setLooping(false);
		deerDyin = new Animation(new SpriteSheet(new Image("img/deerdyn.gif",false,Image.FILTER_NEAREST), 32, 32),100); 
		deerDyin.setLooping(false);
		bearDyin  = new Animation(new SpriteSheet(new Image("img/beardyn.gif",false,Image.FILTER_NEAREST), 32, 32),100); 
		bearDyin.setLooping(false);

		plStep = new Sound("snd/plStep.wav");
		deerStp = new Sound("snd/deerwlk.wav");
		bearStp= new Sound("snd/berWalk.wav");
		swing = new Sound("snd/swing.wav");
		thrw = new Sound("snd/seartrw.wav"); 
		spHit = new Sound("snd/sphit.wav");
		maceHit =  new Sound("snd/smash.wav");
		dmgs = new Sound("snd/dmgs.wav");
		deerAgr = new Sound("snd/elkroar.wav");
		deerAtk = new Sound("snd/deeratk.wav");
		deerDmg = new Sound("snd/deerdmgd.wav");
		deerDin = new Sound("snd/deerdyin.wav");
		bearAgr = new Sound("snd/broar1.wav");
		bearAtk = new Sound("snd/bata.wav");
		bearDmg = new Sound("snd/bdmgd.wav");
		bearDin = new Sound("snd/bded.wav");

		lyhye = new Sound("snd/lyhye.ogg");
		intro = new Sound("snd/oleni.ogg");
		outro = new Sound("snd/gover.ogg");

		polka = new Music("snd/msc/SpazzmaticaPolka.ogg");

		isIntrd1 = false;
		xScale = gc.getWidth()/320f;

		scrWi =  gc.getWidth();
		scrHe = gc.getHeight();

		mouz = new MousePoint();

		//world = new World();
		map = new Image[128][128];
		for (int x = 0; x < map.length; x++) {
			for (int y = 0; y < map[x].length; y++) {
				double rnd = Math.random();
				if (rnd <= 0.5) {
					map[x][y] = snow1;
				} else 
					map[x][y] = snow2;
			}
		}

		new World();
		playerC = World.player;
		World.objecz.add(playerC);

		upRen = new ArrayList<Entity>();
		downRen = new ArrayList<Entity>();

		time = 0;
		timeEnd = 10 * 60 * 1000;

		cuttimer = 0;
		cutnum = 0;

		minleft = 10;
		secleft = 1;

		int elknum = (int) ((map.length*4) * (Math.random()+0.2));
		for (int num = 0; num < elknum; num++) {
			StaticObj prdmt = new StaticObj();
			prdmt.x = (float) ((map.length * 32) * Math.random());
			prdmt.y = (float) ((map.length * 32) * Math.random());
			double rnd = Math.random();
			if (rnd < 0.5) prdmt.type = 0; // elka
			else prdmt.type = 1; // kamen
			World.objecz.add(prdmt);
		}
		for (int i = 0; i < World.objecz.size(); i++) {

			float min = World.objecz.get(i).y;
			int imin = i;
			for (int j = i; j < World.objecz.size(); j++) {

				if (World.objecz.get(j).y < min) {
					min = World.objecz.get(j).y;
					imin = j;
				}
			}

			if (i != imin) {
				Entity temp = World.objecz.get(i);
				World.objecz.set(i, World.objecz.get(imin));
				World.objecz.set(imin, temp);
			}
		}
		//jivotnii
		int jiv = 45;
		for (int num = 0; num < jiv; num++) {
			Animal prdmt = new Animal(0, deerAttack.copy(), deerDyin.copy(),deerWalk.copy());
			prdmt.x = (float) ((map.length * 32) * Math.random());
			prdmt.y = (float) ((map.length * 32) * Math.random());
			World.objecz.add(prdmt);
			World.animalz.add(prdmt);
		}
		int jiv2 = 10;
		for (int numa = 0; numa < jiv2; numa++) {
			Animal prdmts = new Animal(1, bearAttack.copy(),bearDyin.copy(),bearWalk.copy());
			prdmts.x = (float) ((map.length * 32) * Math.random());
			prdmts.y = (float) ((map.length * 32) * Math.random());

			World.objecz.add(prdmts);
			World.animalz.add(prdmts);
		}
		playerC.x = 1500;
		playerC.y = 1500;
	}

	@Override
	public synchronized void update(GameContainer gc, int delta) 
			throws SlickException {


		if (!isIntrd1) {

			if (cuttimer == 1) try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {e.printStackTrace();}

			cuttimer += delta;

			if (cuttimer > 2000 && cuttimer < 2500 && !lyhye.playing()) {
				cutnum = 0;
				//System.out.println("eto bili lyhye..");
				lyhye.play();
			}
			if (cuttimer > 7556 && cuttimer < 12556 && !intro.playing()) {
				cutnum = 1;
				//System.out.println("dialog 1");
				intro.play();
			}
			if (cuttimer > 12556 && cutnum != 2) {
				cutnum = 2;
				//System.out.println("dialog 2");

			}
			if (cuttimer > 28185 && !polka.playing()) {
				//System.out.println("myzlo");
				polka.loop(1, 0.0f);

				polka.fade(2000, 0.2f, false);

			}
			if (cuttimer >= 30185)  {
				cuttimer = 0;
				isIntrd1 = true;
			}
		}

		if (gameEnded) {
			
			cuttimer += delta;
			if (!drawvoid && cuttimer >= 3000 && cuttimer < 12000 && !outro.playing())  {
				outro.play();
				drawEnd = true;
				cutnum = 2;
			}
			if (cuttimer >= 12000 && cuttimer < 17000)  {
				cutnum = 3;
			}
			if (!drawvoid && cuttimer >= 17000 ) {
				drawvoid = true;
				System.out.println("igsray!");
				polka.loop(1, 0.0f);
				polka.fade(2000, 0.2f, false);
				
				
			}
		}

		if (time >= timeEnd) {
			gOver = true;
			gameEnded = true;
		}
		if (!gOver && isIntrd1) {
			time +=delta;
			//�����  for (int a = 0; a < World.animalz.size(); a++ ) {
			for (Animal animal : World.animalz) {
				//Animal animal = World.animalz.get(a);

				animal.think(delta);


				if (animal.x < 0) animal.x = 128*32;
				if (animal.x > 128*32) animal.x = 0;
				if (animal.y < 0) animal.y = 128*32;
				if (animal.y > 128*32) animal.y = 0;

				if (!animal.attacking && animal.haveTgt && !animal.dead && !animal.dying) {
					//System.out.println(" olens GO hp = " + animal.hp);

					float speed = 0.08f;
					switch (animal.type) {
					case 0:
						speed = 0.08f;
						break;
					case 1:
						speed = 0.05f;
						break;
					}
					if (animal.criticalWound) speed /= 2;

					if (animal.tgtX - 5 > animal.x ) {
						//System.out.println( animal.x);
						//float oldX = animal.x;
						animal.x += speed * delta;

						if (!checkCollison(animal)) {
							animal.moving = true;
							animal.faceRight = true;
						} else {
							//System.out.println("RASPIDORASILO");
							animal.x -=  speed*2 * delta;
							animal.resetTgt();
							animal.setTgt();
						}

					}
					if (animal.tgtX + 5 < animal.x) {
						//float oldX = animal.x;
						animal.x += -speed * delta;

						if (!checkCollison(animal)) {
							animal.moving = true;
							animal.faceRight = false;
						} else {
							animal.x +=  speed * delta;
							animal.resetTgt();
							animal.setTgt();
						}
					}
					if (animal.tgtY - 5 > animal.y ) {

						//float oldY = animal.y;
						animal.y += speed * delta;

						if (!checkCollison(animal)) {
							animal.moving = true;
						} else {
							animal.y -=  speed * delta;
							animal.resetTgt();
							animal.setTgt();
						}

					}
					if (animal.tgtY + 5 < animal.y) {
						//float oldY = animal.y;
						animal.y += -speed * delta;

						if (!checkCollison(animal)) {
							animal.moving = true;
						} else {
							animal.y +=  speed * delta;
							animal.resetTgt();
							animal.setTgt();
						}
					}
				}


			}
			//������
			for (Missile misl : World.misll) {
				float hip = 0.2f * delta;

				float rotation = (float) (misl.rot - Math.toRadians(90));

				if (misl.x < 0) misl.x = 128*32;
				if (misl.x > 128*32) misl.x = 0;
				if (misl.y < 0) misl.y = 128*32;
				if (misl.y > 128*32) misl.y = 0;

				if (!checkCollison(misl) && !misl.stuck) {

					misl.x += hip * Math.sin(rotation);
					misl.y -= hip * Math.cos(rotation);

				}

			}

			//TODO Soind
			if ( playerWalk.getFrame() == 5  && playerC.moving || playerWalk.getFrame() == 1  && playerC.moving) {
				playSound(0,getDRX(playerC.x),getDRY(playerC.y));
			}

			if (playerAttack.getFrame() == 4) playSound(3,getDRX(playerC.x),getDRY(playerC.y));
			if (spearAttack.getFrame() == 3) playSound(4,getDRX(playerC.x),getDRY(playerC.y));

			//TODO player
			if (playerC.hp <= 0) {
				playerC.attacking = false;
				playerC.moving = false;
				playerC.dyin = true;
			}

			if (!playerC.dead && !playerC.dyin && !playerC.inbatl ) {
				if (playerC.hp < 100) {
					playerC.hp += 0.001f * delta;
				}
			} else {
				if (playerC.agro < 8000) {
					playerC.agro += delta;
				} else
				{
					playerC.agro = 0;
					playerC.inbatl = false;
				}
			}

			//soind?
			if (!playerC.dead && !playerC.dyin && playerC.attacking && playerC.weaponWld == 2 && spearAttack.getFrame() == 2 && playerC.shotdone) {
				playerC.shotdone = false;
				Missile spear = new Missile();
				spear.rot = (float)Math.atan2((playerC.y-mouz.y), (playerC.x-mouz.x)) ;
				spear.x = playerC.x;
				spear.y = playerC.y;
				spear.host = playerC;

				World.misll.add(spear);
				World.objecz.add(spear);
				playerC.spearQ--;

				playSound(4, getDRX(playerC.x), getDRY(playerC.y));


			}
			//dybina
			if (!playerC.dead && !playerC.dyin && playerC.attacking && playerC.weaponWld == 1 && playerAttack.getFrame() == 4 && playerC.shotdone) {
				playerC.shotdone = false;
				playerC.dealDamage();
				//System.out.println(playerC.score);

			}


			//TODO animazionen
			playerC.moving = false;
			spearWalk.setCurrentFrame(playerWalk.getFrame());



			if (playerAttack.isStopped()) {
				playerAttack.restart();
				playerC.shotdone = true;
				playerC.attacking = false;
			}
			if (spearAttack.isStopped()) {
				spearAttack.restart();
				playerC.shotdone = true;
				playerC.attacking = false;
				if (playerC.spearQ == 0) playerC.weaponWld = 1;
			}
			if (playerDyin.isStopped() && playerC.hp <= 0) {
				playerC.dead = true;
				playerC.target = null;
				gOver = true;

			}


			Input input = gc.getInput();
			if (!playerC.dead && !playerC.dyin ) {
				if(!playerC.attacking && input.isKeyDown(Input.KEY_1))
				{
					//bulowa
					playerC.weaponWld = 1;
				}
				if(!playerC.attacking && input.isKeyDown(Input.KEY_2) && playerC.spearQ > 0)
				{
					//kopie
					playerC.weaponWld = 2;
				}

				if(input.isKeyDown(Input.KEY_A) && !playerC.attacking)
				{
					//float oldX = playerC.x;
					playerC.x += -0.08f * delta;
					if (!checkCollison(playerC)) {
						playerC.moving = true;
						playerC.faceLeft = true;
					} else
						playerC.x += 0.08f * delta;
				} 

				if(input.isKeyDown(Input.KEY_D) && !playerC.attacking)
				{
					//float oldX = playerC.x;
					playerC.x += 0.08f * delta;
					if (!checkCollison(playerC)) {
						playerC.moving = true;
						playerC.faceLeft = false;
					} else
						playerC.x -= 0.08f * delta;

				}

				if(input.isKeyDown(Input.KEY_W) && !playerC.attacking)
				{
					float oldY = playerC.y;
					playerC.y -= 0.08f * delta;
					if (!checkCollison(playerC)) 
						playerC.moving = true;
					else playerC.y = oldY;
				}
				if(input.isKeyDown(Input.KEY_S) && !playerC.attacking)
				{
					float oldY = playerC.y;
					playerC.y += 0.08f * delta;
					if (!checkCollison(playerC)) 
						playerC.moving = true;
					else playerC.y = oldY;
				}
				if(input.isKeyPressed(Input.KEY_M))
				{
					if (!muteMusic) {
						muteMusic = true;
						polka.stop();
					}
					else {
						muteMusic = false;
						polka.loop(1, 0.2f);
					}
				}
				//TODO
				if (input.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
					float mx = ((input.getMouseX()  - scrWi/2)/xScale) + playerC.x  ;
					float my = ((input.getMouseY()  - scrHe/2)/xScale) + playerC.y;
					mouz.setXY(mx, my);
					//System.out.println(" mxy = " + mx + "x" + my + " plxy = " + playerC.x + "x" + playerC.y);
					//DasGame.playSound(15, DasGame.getDRX(playerC.x), DasGame.getDRX(playerC.y));

					if (!playerC.attacking && playerC.weaponWld == 1) {
						playerC.attacking = true;
						playerC.inbatl = true;
						playerC.agro = 0;
					} else if (!playerC.attacking && playerC.weaponWld == 2 && playerC.spearQ > 0) {
						playerC.attacking = true;
						playerC.inbatl = true;
						playerC.agro = 0;
					}
					checkCollison(mouz);



				}
			}

			if (playerC.x < 0) playerC.x = 128*32;
			if (playerC.x > 128*32) playerC.x = 0;
			if (playerC.y < 0) playerC.y = 128*32;
			if (playerC.y > 128*32) playerC.y = 0;
			//Raspredelenie (48*xScale)

			upRen.clear();
			downRen.clear();


			for (Entity obj : World.objecz) {
				float wtdX = (obj.x - playerC.x) * xScale + scrWi/2;
				float wtdY = (obj.y - playerC.y)* xScale + scrHe/2;

				int xm = 64;
				int xmm = 128;
				if (wtdX >= ((128 * 32) - xm)*xScale ) wtdX -= (128 * 32)*xScale ;
				if (wtdX < scrWi + ((xm - (128 * 32))*xScale) ) wtdX =  wtdX + ((128 * 32)*xScale);
				if (wtdY >= ((128 * 32) - xm)*xScale ) wtdY -= (128 * 32)*xScale  ;
				if (wtdY < scrHe+ ((xm - (128 * 32))*xScale)  ) wtdY = wtdY + ((128 * 32)*xScale);

				/*
			if (wtdX >= -xm*xScale && wtdX < scrWi+xm*xScale 
					&& wtdY >= -xm*xScale && wtdY < scrHe+xm*xScale) 
				 */
				/*
				 * if (obj.y <= playerC.y && obj.y > playerC.y - ((scrHe/1.75)/xScale) && wtdX >= -64*xScale && wtdX < scrWi+64*xScale) upRen.add(obj);
			else if (obj.x > playerC.x - ((scrWi/1.75)/xScale) && obj.x < playerC.x + ((scrWi/1.75)/xScale) && obj.y < playerC.y + ((scrHe/1.75)/xScale)+32) downRen.add(obj);

				 */

				if (obj.y <= playerC.y &&  wtdY >= -xm*xScale && wtdX >= -xmm*xScale && wtdX < scrWi+xm*xScale) upRen.add(obj);
				else if (wtdX >= -xmm*xScale && wtdX < scrWi+xm*xScale && wtdY < scrHe+xm*xScale) downRen.add(obj);
			}
			

			for (int i = 0; i < upRen.size(); i++) {

				float min = upRen.get(i).y;
				int imin = i;
				for (int j = i; j < upRen.size(); j++) {

					if (upRen.get(j).y < min) {
						min = upRen.get(j).y;
						imin = j;
					}
				}

				if (i != imin) {
					Entity temp = upRen.get(i);
					upRen.set(i, upRen.get(imin));
					upRen.set(imin, temp);
				}
			}
			for (int i = 0; i < downRen.size(); i++) {

				float min = downRen.get(i).y;
				int imin = i;
				for (int j = i; j < downRen.size(); j++) {

					if (downRen.get(j).y < min) {
						min = downRen.get(j).y;
						imin = j;
					}
				}

				if (i != imin) {
					Entity temp = downRen.get(i);
					downRen.set(i, downRen.get(imin));
					downRen.set(imin, temp);
				}
			}
		}
		else if (!drawvoid && isIntrd1) {
			if (!muteMusic) {
				System.out.println("igsray!");
				polka.fade(2000, 0, true);
				muteMusic = true;
			}
			if (gOver && !gameEnded) {
				Input input = gc.getInput();
				if (input.isKeyDown(input.KEY_R)) restartgame();
			}


			//TODO ���������� ����
		
		}
	
		//System.out.println("up= " + upRen.size() + " down= " + downRen.size());

	}

	public void render(GameContainer gc, Graphics g) 
			throws SlickException	{
		//System.out.println(xScale + "x"); 
		if (!isIntrd1  || drawEnd) {
			if(!drawEnd
					 && cuttimer < 2000)
				introbg.draw(0,0,xScale);
			else {
				if (!drawvoid)
				cutscene.getSprite(cutnum, 0).draw(0, 0, xScale);
				else {
					g.setColor(Color.white);
					g.drawString(" CREDITS: ", 80*xScale, 0*xScale);
					g.drawString(" SPASIBO ZA IGRY! ", 80*xScale, 10*xScale);
					g.drawString("Vpadlo prikruchivat russkie shrifty, lol.", 80*xScale, 20*xScale);
					g.drawString("Sound- www.FreeSound.org", 80*xScale, 40*xScale);
					g.drawString("Drugie zapisal sam, takie dela.", 80*xScale, 50*xScale);
					g.drawString("Music - Spazzmatica Polka by Kevin MacLeod" , 80*xScale, 70*xScale);
					g.drawString("www.incompetech.com/music/royalty-free/" , 85*xScale, 80*xScale);
	
					g.drawString("Sprites - MDKed." , 80*xScale, 100*xScale);
					g.drawString("Programming - MDKed." , 80*xScale, 110*xScale);
					g.drawString("Etc - x-and1988, MDKed." , 80*xScale, 120*xScale);
					
					g.drawString("Special for GcUp.ru 2012-2013." , 80*xScale, 230*xScale);




				}
			}
		}
		else {
			//����� ������  
			float chX = gc.getWidth()/2;
			float chY = gc.getHeight()/2;

			int cX = (int) (playerC.x / 32);
			int cY = (int) (playerC.y / 32);
			//System.out.println(" Player locCh= " + cX + "x" + cY + " xy= " + playerC.x + "x" +  playerC.y );


			// ��������� ����

			for (int x = 0; x < 11; x++) {
				for (int y = 0; y < 9; y++) {
					//���������� ��� ��������� �����(����)
					int drCX = x+cX-5;
					int drCY = y+cY-4;
					float drX = (( drCX * 32)  - playerC.x) * xScale + chX;
					float drY = (( drCY * 32) - playerC.y) * xScale + chY;

					if (drCX >= 0 && drCX < map.length && drCY >=0 && drCY < map[drCX].length)
						map[drCX][drCY].draw(drX,drY,xScale);
					else {
						if (drCX < 0) drCX = map.length + drCX;
						if (drCX >= map.length ) drCX -= map.length;
						if (drCY < 0) drCY = map.length + drCY;
						if (drCY >= map[drCX].length ) drCY -= map[drCX].length;
						//System.out.println(" ��������� " + drCX + "x" + drCY);
						if (drCX >= 0 && drCX < map.length && drCY >=0 && drCY < map[drCX].length)
							map[drCX][drCY].draw(drX,drY,xScale);
					}

				}
			}

			
			for (Entity obj : upRen) {
				drawEntity(obj);
			}

			drawPlayer(chX, chY);

			for (Entity obj : downRen) {
				drawEntity(obj);
			}


		
			g.setColor(Color.white);
			heart.draw(10*xScale, 200 *xScale , 32*xScale, 32*xScale);
			g.drawString(Integer.toString((int)playerC.hp) , 20*xScale, 210 *xScale);

			scoreTable.draw(256*xScale, 0, xScale); 
			g.drawString(Integer.toString((int)playerC.score), 276*xScale, 17*xScale);

			timer.draw(128*xScale, 0, xScale);
			if (time < timeEnd) {
				minleft = (int) (((timeEnd - time) / 1000)/60);
				secleft = (int) ((timeEnd - time)/1000 - minleft*60); 
			}
			g.drawString(Integer.toString(minleft), 154*xScale, 14*xScale);
			g.drawString(Integer.toString(secleft), 162*xScale, 14*xScale);

			ammo.draw(278*xScale, 200*xScale, xScale);
			ammonum.getSprite(playerC.spearQ, 0).draw(278*xScale, 200*xScale, xScale);

			if (gOver) gameOver.draw(96*xScale, 128*xScale, xScale);
			if (gOver && !gameEnded) 	{
				g.setColor(Color.black);
				g.drawString("Press R - to restart", 128*xScale, 80*xScale);
			}

		}
	}

	public static void main(String[] args) 
			 throws SlickException
			   {
			  prepareSystem();
			  AppGameContainer app = 
			    new AppGameContainer(new DasGame());

			  app.setDisplayMode(800, 600, false);
			  app.setIcon("img/player1.gif");
			  app.setMinimumLogicUpdateInterval(20);
			  app.setShowFPS(false);
			  app.start();
			  
			   }
			 
			 static void prepareSystem()
			 {
			  try {
			   System.setProperty( "java.class.path", "."+File.separator+"lib"+File.separator);
			   System.setProperty( "java.library.path", "."+File.separator+"lib"+File.separator);
			   System.out.println(System.getProperties().get("java.library.path"));
			   System.out.println(System.getProperties().get("java.class.path"));
			   Field fieldSysPath = ClassLoader.class.getDeclaredField( "sys_paths" );
			   fieldSysPath.setAccessible( true );
			   fieldSysPath.set( null, null );
			  }
			  catch(Exception e){
			   e.printStackTrace();
			  }
			  
			 }
	
	
	
	private void drawPlayer(float chX, float chY) {

		float cdx = chX- (16*xScale);
		float cdy = chY - (32*xScale);

		float drx = cdx, drsclx, drscly;
		if (playerC.faceLeft) {

			drx = cdx+32*xScale;
			drsclx =  -32*xScale;
			drscly = 32*xScale;
		} else {
			drsclx  = 32*xScale;
			drscly = drsclx;
		}

		if (playerC.moving ) {
			playerWalk.draw(drx, cdy, drsclx ,drscly); 
			switch (playerC.weaponWld) {
			case 1:
				//���������� ����
				break;
			case 2:
				spearWalk.draw(drx, cdy, drsclx ,drscly); 
				break;
			}
		}
		else if (playerC.attacking) {

			switch (playerC.weaponWld) {
			case 1:
				playerAttack.draw(drx, cdy, drsclx, drscly);
				break;
			case 2:
				spearAttack.draw(drx, cdy, drsclx, drscly);
				break;
			}
		}
		else if (playerC.dyin) {
			playerDyin.draw(drx, cdy, drsclx ,drscly);
		}
		else if (playerC.dead) {
			playerDead.draw(drx, cdy, drsclx ,drscly);
		}
		//stoit
		else  {
			player.draw(drx, cdy, drsclx ,drscly);
			switch (playerC.weaponWld) {
			case 1:
				//���������� ����
				break;
			case 2:
				spearPlStand.draw(drx, cdy, drsclx ,drscly);
				break;
			}
		}

	}

	public synchronized boolean checkCollison(Entity obj) {
		boolean ismouse = false
				, statObj = false, animal = false, anical = false, misile = false, 
				player = false, misileget = false;
		Entity inittr = null ,tgt = null;

		Rectangle obj1r = new Rectangle(0,0,0,0), obj2r = new Rectangle(2,2,2,2);
		if (obj instanceof Player) {
			player = true;
			obj1r = playerC.getBounds();
		}
		//System.out.println(" pl= " + obj1r.getCenterX() +"x"+ obj1r.getCenterY());

		if (obj instanceof Animal) {
			anical = true;
			Animal chk = (Animal) obj;
			obj1r = chk.getBounds();
		}

		if (obj instanceof MousePoint) {
			ismouse = true;
			MousePoint ms = (MousePoint) obj;
			obj1r = ms.getBounds();
		}

		if (obj instanceof Missile) {
			misile = true;
			Missile mis = (Missile) obj;
			inittr = mis;
			obj1r = mis.getBounds();
		}


		for (Entity chCol : World.objecz) {
			if ( chCol instanceof StaticObj) {
				StaticObj ob = (StaticObj) chCol;
				obj2r = ob.getBounds();
				if (ismouse) {
					statObj = true;
				}
				if (misile) {
					tgt = chCol;
				}
			}
			if ( chCol instanceof Animal && !chCol.equals(obj) && !player) {
				animal = true;
				Animal an = (Animal) chCol;
				if (!an.dead) {
					obj2r = an.getBounds();

					if (misile || ismouse) {
						tgt = chCol;
					}
				}
			}
			if ( chCol instanceof Missile) {
				Missile an = (Missile) chCol;
				if (an.stuck && !anical) {
					misileget = true;
					obj2r = an.getBounds();
				}

			}
			if (obj1r.intersects(obj2r)) {

				if (misile) {
					Missile mis = (Missile) inittr;
					if (!mis.stuck && animal) {
						//System.out.println("olen ranen");
						Animal an = (Animal) tgt;
						tgt = an;

						mis.stuck = true;
						//						kopie
						playSound(5,getDRX(an.x),getDRY(an.y));

						an.criticalWound = true;
						an.hp -= 50;
						an.attacked(mis.host);
						switch (an.type) {
						case 0:
							playSound(9,getDRX(an.x),getDRY(an.y));
							break;
						case 1:
							playSound(13,getDRX(an.x),getDRY(an.y));
							break;
						}
						//System.out.println(" olens hp = " + an.hp);
					}
					else if (!mis.stuck && !player && !misileget) {
						Missile misa = (Missile) inittr;
						misa.stuck = true;
					}
					else if ( misileget ) return false;
				}
				if (ismouse && playerC.attacking) {
					Rectangle plr = playerC.getBounds();
					double dist1 =  Pifagor.getDist(plr.getCenterX(), obj2r.getCenterX(), plr.getCenterY(), obj2r.getCenterY()); 
					double dist2 = Pifagor.getDist(obj1r.getCenterX(), obj2r.getCenterX(), obj1r.getCenterY(), obj2r.getCenterY());
					if (dist1 >= 0 &&  dist1 < 30 && dist2 >=0 && dist2 < 30) {
						//	System.out.println(" attaka igroka tzeli");
						playerC.target = tgt;
					}

				}

				if (player && misileget) {
					World.misll.remove(chCol);
					World.objecz.remove(chCol);
					playerC.spearQ++;
				}
				return true;
			}

			misileget = false;
			animal = false;
			statObj = false;

		}

		return false;

	}

	public static void playSound(int numer, float x, float y) {
		float pitch;

		switch (numer) {
		case 0:
			if (!plStep.playing()) {
				pitch = (float) (0.9 + (Math.random() * 0.1));
				plStep.playAt(pitch, 0.15f,x,y,0.0f);
			}
			break;
		case 1:
			//if (!deerStp.playing()) {
			pitch = (float) (0.9 + (Math.random() * 0.1));
			deerStp.playAt(pitch, 0.15f,x,y,0.0f);
			//}
			break;
		case 2:
			//if (!bearStp.playing()) {
			pitch = (float) (0.9 + (Math.random() * 0.1));
			bearStp.playAt(pitch, 0.15f,x,y,0.0f);
			//}
			break;
		case 3:
			if (!swing.playing()) {
				pitch = (float) (0.9 + (Math.random() * 0.1));
				swing.playAt(pitch, 0.3f,x,y,0.0f);
			}
			break;
		case 4:
			//if (!thrw.playing()) {
			pitch = (float) (0.9 + (Math.random() * 0.1));
			thrw.playAt(pitch, 0.2f,x,y,0.0f);
			//	}
			break;
		case 5:
			//if (!spHit.playing()) {
			pitch = (float) (0.9 + (Math.random() * 0.1));
			spHit.playAt(pitch, 0.4f,x,y,1.0f);
			//	}
			break;
		case 6:
			if (!dmgs.playing()) {
				pitch = (float) (0.9 + (Math.random() * 0.1));
				dmgs.playAt(pitch, 0.4f,x,y,0.0f);
			}
			break;
		case 7:
			//	if (!deerAgr.playing()) {
			pitch = (float) (0.9 + (Math.random() * 0.1));
			deerAgr.playAt(pitch, 0.4f,x,y,0.0f);;
			//	}
			break;
		case 8:
			//if (!deerAtk.playing()) {
			pitch = (float) (0.9 + (Math.random() * 0.1));
			deerAtk.playAt(pitch, 0.4f,x,y,0.0f);
			//}
			break;
		case 9:
			//	if (!deerDmg.playing()) {
			pitch = (float) (0.9 + (Math.random() * 0.1));
			deerDmg.playAt(pitch, 0.4f,x,y,1.0f);
			//	}
			break;
		case 10:
			//if (!deerDin.playing()) {
			pitch = (float) (0.9 + (Math.random() * 0.1));
			deerDin.playAt(pitch, 0.4f,x,y,0.0f);
			//}
			break;
		case 11:
			//if (!bearAgr.playing()) {
			pitch = (float) (0.9 + (Math.random() * 0.1));
			bearAgr.playAt(pitch, 0.4f,x,y,0.0f);
			//}
			break;
		case 12:
			//if (!bearAtk.playing()) {
			pitch = (float) (0.9 + (Math.random() * 0.1));
			bearAtk.playAt(pitch, 0.4f,x,y,0.0f);
			//}
			break;
		case 13:
			//if (!bearDmg.playing()) {
			pitch = (float) (0.9 + (Math.random() * 0.1));
			bearDmg.playAt(pitch, 0.4f,x,y,1.0f);
			//}
			break;
		case 14:
			//if (!bearDin.playing()) {
			pitch = (float) (0.9 + (Math.random() * 0.1));
			bearDin.playAt(pitch, 0.4f,x,y,0.0f);
			//}
			break;
		case 15:
			//if (!maceHit.playing()) {
			pitch = (float) (0.9 + (Math.random() * 0.1));
			maceHit.playAt(pitch, 1f,x,y,1.0f);
			System.out.println(" ydar dubinoy!");

			//}
			break;

		}
	}

	private void drawEntity(Entity obj) {
		float wtdX = (obj.x - playerC.x) * xScale + scrWi/2;
		float wtdY = (obj.y - playerC.y)* xScale + scrHe/2;

		//if (!charDrawd && obj.y > playerC.y) drawPlayer(chX, chY);
		int xm = 64;
		if (wtdX >= ((128 * 32) - xm)*xScale ) wtdX -= ((128 * 32)*xScale) ;
		if (wtdX < scrWi + ((xm - (128 * 32))*xScale) ) wtdX =  wtdX + ((128 * 32)*xScale);
		if (wtdY >= ((128 * 32) - xm)*xScale ) wtdY -= ((128 * 32)*xScale) ;
		if (wtdY < scrHe+ ((xm - (128 * 32))*xScale)  ) wtdY = wtdY + ((128 * 32)*xScale);


		if (wtdX >= -64*xScale && wtdX < scrWi+64*xScale 
				&& wtdY >= -64*xScale && wtdY < scrHe+64*xScale) {
			if (obj instanceof StaticObj) {
				StaticObj so = (StaticObj) obj;
				switch (so.type) {
				case 0:
					tree.draw(wtdX - (32*xScale), wtdY - (64*xScale), xScale);
					break;
				case 1:
					stone.draw(wtdX - (32*xScale), wtdY - (64*xScale), xScale);
					break;
				}
			}
			if (obj instanceof Animal) {
				Animal anim = (Animal) obj;
				if (anim.type == 0) {
					if (anim.moving &&  !anim.faceRight) anim.moveani.draw(wtdX-(16*xScale), wtdY-(32*xScale), 32*xScale ,32*xScale); 
					else if (anim.moving && anim.faceRight) anim.moveani.draw((wtdX-16*xScale)+(32*xScale), wtdY-(32*xScale), -32*xScale ,32*xScale);

					else if (anim.attacking &&  !anim.faceRight) anim.atani.draw(wtdX-(16*xScale), wtdY-(32*xScale), 32*xScale ,32*xScale);
					else if (anim.attacking &&  anim.faceRight) anim.atani.draw((wtdX-16*xScale)+(32*xScale), wtdY-(32*xScale), -32*xScale ,32*xScale);

					else if (anim.dying &&  !anim.faceRight) anim.deadani.draw(wtdX-(16*xScale), wtdY-(32*xScale), 32*xScale ,32*xScale); 
					else if (anim.dying &&  anim.faceRight) anim.deadani.draw((wtdX-16*xScale)+(32*xScale), wtdY-(32*xScale), -32*xScale ,32*xScale); 

					else if (anim.dead && !anim.faceRight) deerDead.draw(wtdX - (16*xScale), wtdY-(32*xScale), xScale);
					else if (anim.dead && anim.faceRight) deerDead.draw((wtdX-16*xScale)+32*xScale, wtdY-(32*xScale), -32*xScale ,32*xScale);


					else if (anim.faceRight) deer.draw((wtdX-16*xScale)+32*xScale, wtdY-(32*xScale), -32*xScale ,32*xScale);
					else  deer.draw(wtdX - (16*xScale), wtdY-(32*xScale), xScale);
				} 
				if (anim.type == 1) {
					if (anim.moving &&  !anim.faceRight) anim.moveani.draw(wtdX-(16*xScale), wtdY-(32*xScale), 32*xScale ,32*xScale); 
					else if (anim.moving && anim.faceRight) anim.moveani.draw((wtdX-16*xScale)+(32*xScale), wtdY-(32*xScale), -32*xScale ,32*xScale);

					else if (anim.attacking &&  !anim.faceRight) anim.atani.draw(wtdX-(16*xScale), wtdY-(32*xScale), 32*xScale ,32*xScale);
					else if (anim.attacking &&  anim.faceRight) anim.atani.draw((wtdX-16*xScale)+(32*xScale), wtdY-(32*xScale), -32*xScale ,32*xScale);

					else if (anim.dying &&  !anim.faceRight) anim.deadani.draw(wtdX-(16*xScale), wtdY-(32*xScale), 32*xScale ,32*xScale); 
					else if (anim.dying &&  anim.faceRight) anim.deadani.draw((wtdX-16*xScale)+(32*xScale), wtdY-(32*xScale), -32*xScale ,32*xScale); 

					else if (anim.dead && !anim.faceRight) bearDead.draw(wtdX - (16*xScale), wtdY-(32*xScale), xScale);
					else if (anim.dead && anim.faceRight) bearDead.draw((wtdX-16*xScale)+32*xScale, wtdY-(32*xScale), -32*xScale ,32*xScale);

					else if (anim.faceRight) bear.draw((wtdX-16*xScale)+32*xScale, wtdY-(32*xScale), -32*xScale ,32*xScale);
					else  bear.draw(wtdX - (16*xScale), wtdY-(32*xScale), xScale);

				}
			}
			if ( obj instanceof Missile ) {
				Missile misl = (Missile) obj;
				if (misl.rot < Math.toRadians(90) && misl.rot > Math.toRadians(-90)) {
					if (!misl.stuck) spearFly.draw((wtdX-16*xScale)+(32*xScale), wtdY-(32*xScale), -32*xScale ,32*xScale); 
					else spearGrnd.draw((wtdX-16*xScale)+(32*xScale), wtdY-(32*xScale), -32*xScale ,32*xScale);
				}
				else {
					if (!misl.stuck) spearFly.draw(wtdX-(16*xScale), wtdY-(32*xScale), 32*xScale ,32*xScale); 
					else spearGrnd.draw(wtdX-(16*xScale), wtdY-(32*xScale),xScale); 

				}
			}
		}
	}
	public static float getDRX(float x) {
		int xm = 64;

		float wtdX = (x - World.player.x) * xScale + scrWi/2;
		if (wtdX >= ((128 * 32) - xm)*xScale ) wtdX -= ((128 * 32)*xScale) ;
		if (wtdX < scrWi + ((xm - (128 * 32))*xScale) ) wtdX =  wtdX + ((128 * 32)*xScale);

		return wtdX;
	}

	public static float getDRY(float y) {
		int xm = 64;
		float wtdY = (y - World.player.y)* xScale + scrHe/2;
		if (wtdY >= ((128 * 32) - xm)*xScale ) wtdY -= ((128 * 32)*xScale) ;
		if (wtdY < scrHe+ ((xm - (128 * 32))*xScale)  ) wtdY = wtdY + ((128 * 32)*xScale);
		return wtdY;
	}
	public static void spawnMob(int type) {
		Animal prdmt;
		switch (type) {
		case 0:
			prdmt = new Animal(type, deerAttack.copy(), deerDyin.copy(),deerWalk.copy());
			prdmt.x = (float) ((128 * 32) * Math.random());
			prdmt.y = (float) ((128 * 32) * Math.random());
			World.objecz.add(prdmt);
			World.animalz.add(prdmt);
			break;
		case 1:
			prdmt = new Animal(type, bearAttack.copy(), bearDyin.copy(),bearWalk.copy());
			prdmt.x = (float) ((128 * 32) * Math.random());
			prdmt.y = (float) ((128 * 32) * Math.random());
			World.objecz.add(prdmt);
			World.animalz.add(prdmt);
			break;
		}
	}
	public void restartgame() {
	
		playerC.x = 1500;
		playerC.y = 1500;
		playerC.hp = 100;
		playerC.dyin = false;
		playerC.dead = false;
		playerC.score = 0;
		playerC.moving = false;
		playerC.attacking = false;
		playerC.spearQ = 3;
		playerDyin.restart();
	

		if (World.misll.size() > 0 ) {
			for (int a = 0 ; a < World.misll.size(); a++) {
				World.objecz.remove(World.misll.get(a));
			}
			World.misll.clear();
		}
		
		time = 0;
		gOver = false;
		
		muteMusic = false;
		
		polka.loop(1, 0.0f);
		polka.fade(2000, 0.2f, false);



	}
}

