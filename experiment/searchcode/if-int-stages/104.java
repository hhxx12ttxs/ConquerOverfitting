package se.rhel.gunslinger.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import se.rhel.gunslinger.model.entities.Boss;
import se.rhel.gunslinger.model.entities.Enemy;
import se.rhel.gunslinger.model.entities.Decal.DecalType;
import se.rhel.gunslinger.model.entities.Enemy.EnemyType;
import se.rhel.gunslinger.model.entities.GameObject;
import se.rhel.gunslinger.model.entities.GameObject.State;
import se.rhel.gunslinger.model.entities.GameObject.Type;
import se.rhel.gunslinger.model.entities.Decal;
import se.rhel.gunslinger.model.entities.IEntity;
import se.rhel.gunslinger.model.entities.PickupAbleObject;
import se.rhel.gunslinger.model.entities.PickupAbleWeapon;
import se.rhel.gunslinger.model.entities.Player;
import se.rhel.gunslinger.model.entities.Sign;
import se.rhel.gunslinger.model.options.StageRequirement;
import se.rhel.gunslinger.model.weapons.WeaponCreator.Weapons;
import se.rhel.gunslinger.observers.SpawnObserver;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

public class Stage {
	
	private static final int 				PART_ONE = 0;
	private static final int 				PART_TWO = 1;

	private Vector2 						mPosition;
	private Stages							mThisStage;

	private StageRequirement[] 				mRequirement;
	
	// Högsta och minsta positionen i kroppen
	private float							MAX_Y;
	private float							MIN_Y;
	
	// Objekt pĺ stagen
	private Map<Integer, Enemy> 			mEnemies;
	private ArrayList<GameObject>			mObjects = new ArrayList<GameObject>();
	private ArrayList<Sign>					mSigns = new ArrayList<Sign>();
	//private ArrayList<Decal>				mDecals = new ArrayList<Decal>();
	private boolean 						mIsBossTime = false;
	

	// Kollisionshantering för vägg fram och vägg bak
	private Body 							mBody;
	private World 							mPhysWorld;

	
	private Part[]							mParts;
	
	// Status
	private StageStatus						mStatus;
	private boolean							mIsFinalStage;
	private boolean							mNoMoreSpawn = false;
	
	// Observer
	private SpawnObserver					mSpawnObserver;
	
	// Tid
	private float 							mEnemySpawnTimeElapsed;
	private float 							mEnemySpawnTime;
	private boolean							mPartTwo = false;
	
	/**
	 * Första kontakt, koppla bara ihop enum med Stage
	 * @param stage
	 */


	public Stage(Stages stage, World world, StageRequirement[] reqs, Vector2 pos, boolean finalStage) {
		mThisStage = stage;
		mPhysWorld = world;
		mRequirement = reqs;
		mEnemies = new HashMap<Integer, Enemy>();
		mParts = new Part[2];
		mIsFinalStage = finalStage;
		
		mEnemySpawnTime = 3f;
		mEnemySpawnTimeElapsed = 0f;
		
		BodyDef def = new BodyDef();
		
		// Skapa väggar fram
		def.type = BodyType.StaticBody;

		def.position.x = pos.x;
		def.position.y = pos.y + Camera.CAMERA_HEIGHT / 2;
		PolygonShape rect = new PolygonShape();
		rect.setAsBox(0.01f, 4f);
		FixtureDef fd = new FixtureDef();
		fd.shape = rect;
		fd.isSensor = false;
		fd.density = 1;
		fd.filter.categoryBits = Filter.CATEGORY_WALLS;
		fd.filter.maskBits = Filter.MASK_WALLS;
		
		mBody = mPhysWorld.createBody(def);
		mBody.createFixture(fd);	
		mBody.setUserData(this);
		
		// Om det är första Stagen sĺ ska en vägg bak finnas

		if(mThisStage == Stages.STAGE1) {
			// Vill inte att den här kroppen ska vara kopplad med userdata
			Body body;

			def.position.x = 0;
			body = mPhysWorld.createBody(def);
			body.createFixture(fd);
		}
		
		rect.dispose();
	}
	
	/**
	 * Resettar en stage
	 */
	public void reset() {
		mParts[PART_ONE].reset();
		mParts[PART_TWO].reset();
		mPartTwo = false;
		mNoMoreSpawn = false;
		
		/*for(Enemy e : mEnemies.values()) {
			e.damageEnemy(1000);
		}
		*/
		mEnemies.clear(); 
	}
	
	/**
	 * Andra kontakt där position sätts
	 * @param pos
	 * @param world
	 */
	public void initialize(Vector2 pos, boolean createOnlyWalls, int part, SpawnObserver spawnObserver, float minY, float maxY) {
		mParts[part] = new Part(pos, part);
		mPosition = mParts[0].getPartPosition();
		
		MIN_Y = minY;
		MAX_Y = maxY;

		mStatus = StageStatus.COMING;
		mSpawnObserver = spawnObserver;

		// Läs frĺn kraven vad som ska finnas pĺ stagen
		if(!createOnlyWalls) {

			for(int i = 0; i < mRequirement[part].enemies(); i++) {
				
				// Fiender ska spawna antingen till höger om parten eller till vänster
				Vector2 spawn = new Vector2(0, Camera.CAMERA_HEIGHT / 2);
				
				if(part == PART_ONE) {
					boolean left = Math.random() <= 0.5 ? true : false;
					if(left) {
						spawn = getRandomStagePosition(mPosition.x - Enemy.SIZE * 2 - 2f, mPosition.x - Enemy.SIZE * 2, Camera.CAMERA_HEIGHT / 2 - 2, Camera.CAMERA_HEIGHT / 2 + 2);
					} else {
						spawn = getRandomStagePosition(mPosition.x + Camera.CAMERA_WIDTH + Enemy.SIZE * 2, mPosition.x + Camera.CAMERA_WIDTH + Enemy.SIZE * 2 + 2f, Camera.CAMERA_HEIGHT / 2 - 2, Camera.CAMERA_HEIGHT / 2 + 2);
					}
				} else {
					boolean left = Math.random() <= 0.5 ? true : false;
					if(left) {
						spawn = getRandomStagePosition(mPosition.x + Camera.CAMERA_WIDTH - Enemy.SIZE * 2 - 2f, mPosition.x + Camera.CAMERA_WIDTH - Enemy.SIZE * 2, Camera.CAMERA_HEIGHT / 2 - 2, Camera.CAMERA_HEIGHT / 2 + 2);
					} else {
						spawn = getRandomStagePosition(mPosition.x + Camera.CAMERA_WIDTH * 2 + Enemy.SIZE * 2, mPosition.x + Camera.CAMERA_WIDTH * 2 + Enemy.SIZE * 2 + 2f, Camera.CAMERA_HEIGHT / 2 - 2, Camera.CAMERA_HEIGHT / 2 + 2);
					}
				}
				
				int id = Id.getInstance().get();
				
				if (mRequirement[part].enemyTypes().get(i) == EnemyType.BOSS) {
					//Lägg till nĺgon boss
					mParts[part].addBoss(new Boss(id, new Vector2(Camera.CAMERA_WIDTH * 2 + 3, 3), mPhysWorld));
				} else {
					// Lägg till fiende till vĺg
					mParts[part].addEnemyToWave(new Enemy(id,
												spawn,
												mPhysWorld,
												mRequirement[part].enemyTypes().get(i)));
				}
			}
			
			// Finns det nĺgra objekt?

			if(mRequirement[part].objects() != 0) {
				Array<GameObject.Type> types = mRequirement[part].types();
				
				// TODO: Objekt borde placeras ut random, alternativt styras frĺn stage-req
				// placeras ut i mitten sĺ länge
				for(GameObject.Type t : types) {
					// Mĺste göra koll ifall det är av rätt typ
					if(GameObject.isPickupAbleObject(t)) {

						float minX = part == 0 ? mPosition.cpy().x : mPosition.cpy().x + Camera.CAMERA_WIDTH;
						float maxX = part == 0 ? mPosition.cpy().x + Camera.CAMERA_WIDTH : mPosition.cpy().x + Camera.CAMERA_WIDTH * 2;
						Vector2 p = getRandomStagePosition(minX, maxX,
								Camera.CAMERA_HEIGHT / 2 - 2, Camera.CAMERA_HEIGHT / 2 + 2);
						
						// Pistolen ska spawna 5,5
						if (t == GameObject.Type.WEAPON) {
							mObjects.add(new PickupAbleWeapon(Id.getInstance().get(), new Vector2(5, 6), mPhysWorld, false, Weapons.GUN));
						} else {
							mObjects.add(new PickupAbleObject(Id.getInstance().get(), p, t, false, mPhysWorld));
						}
					}
				}
			}
		}
			
		// Finns det nĺgra signs?
		if(mRequirement[part].signs() != 0) {
			Array<GameObject.Type> types = mRequirement[part].types();
		
			// TODO: Objekt borde placeras ut random, alternativt styras frĺn stage-req
			// TODO: Lösa snyggare med vilket meddelande
		
			// placeras ut i mitten sĺ länge
			for(GameObject.Type t : types) {
				// Mĺste göra koll ifall det är av rätt typ
				if(GameObject.isSign(t)) {
					mSigns.add(new Sign(Id.getInstance().get(), new Vector2(10,6), mRequirement[part].messages().get(0), mPhysWorld));
				}
			}
		}	

		// Instansiera fienderna

		if(part == PART_TWO && !createOnlyWalls) {
			System.out.println("enemies initialized correct, i guess");
			mParts[PART_ONE].reset();
			mParts[PART_TWO].reset();
		}
		
		//TODO: Ändra inläsning till textfil eller dylikt
		//Ladda in decaler
//		if (part == PART_ONE && createOnlyWalls) {
//			mDecals.add(new Decal(new Vector2(2, 1.5f), Decal.LARGE_CACTUS_SIZE, DecalType.LARGE_CACTUS, mPhysWorld));
//			mDecals.add(new Decal(new Vector2(8, 6), Decal.SMALL_CACTUS_SIZE, DecalType.SMALL_CACTUS, mPhysWorld));
//		}
	}


	// ---------------------------------
	// LOGIK
	// ---------------------------------
	
	/**
	 * Genererar en random-position pĺ stagen
	 */
	private Vector2 getRandomStagePosition(float minX, float maxX, float minY, float maxY) {
		Random rand = new Random();
		int n = (int) (maxX - minX);
		int x = (int) (rand.nextInt(n) + minX);
		n = (int)(maxY - minY);
		int y = (int) (rand.nextInt(n) + minY);
		
		return new Vector2(x, y);
	}

	// Gör väggarna till sensorer eller inte
	public void setSensor(boolean bool) {
		for(Fixture f : mBody.getFixtureList()) {
			f.setSensor(bool);
		}
	}
	
	/**
	 * Boss spawner
	 * @param part
	 */
	private void spawnBoss(int part) {
		Boss boss = mParts[part].mBoss;
		boss.spawn(true);
		System.out.println("STAGE->SPAWNBOSS->GETID():" + boss.getID());
		mEnemies.put(boss.getID(), boss);
		mSpawnObserver.enemySpawn();
		mNoMoreSpawn = true;
	}
	
	/**
	 * När nya fiender ska spawnas
	 */
	public void spawnEnemies(int part) {

		if(!mNoMoreSpawn) {
			mEnemies.clear();
			for(Enemy e : mParts[part].getCurrentWaveEnemies()) {
				e.spawn(true);
				mEnemies.put(e.getID(), e);	
			}
			
			// Meddela servern och klienten
			mSpawnObserver.enemySpawn();	
		}
		
		// Koll om det här var sista spawnen som skulle göras pĺ leveln
		if(part == PART_TWO && mParts[PART_TWO].isFinalWave()) {
			mNoMoreSpawn = true;
		}
	}
	
	/**
	 * Uppdatera allting som befinner sig i stagen
	 * @param delta
	 */

	public void update(float delta, Array<Player> players) {
		
		int part = mPartTwo == true ? PART_TWO : PART_ONE;
		
		if (mParts[part].mIsBossTime) {
			if (mEnemies.isEmpty()) {
				mIsBossTime = true;
				//Spawna boss
				spawnBoss(part);
			}
			
			if (mParts[part].mBoss.isAlive()) {
			} else {
				mIsBossTime = false;
			}
		} else {
			// Första förfarandet - när de första fienderna ska spawna
			mEnemySpawnTimeElapsed += delta;
			if(mEnemySpawnTimeElapsed > mEnemySpawnTime) {
				mEnemySpawnTimeElapsed = 0f;
				
				if(mEnemies.isEmpty()) {
					spawnEnemies(part);
				} else {
					// Är alla fiender döda?
					boolean dead = true;
					for(Enemy e : mEnemies.values()) {
						if(e.isAlive())
							dead = false;
					}
					
					// I sĺ fall kolla om det finns fler vĺgor
					if(dead) {
						if((mParts[part].getCurrentWave() + 1) < mParts[part].getNoOfWaves()) {
							// Det finns fler vĺgor
							mParts[part].nextWave();
							spawnEnemies(part);
						} else {
							// Det finns inte fler vĺgor, nästa part kan triggas
							
							// Koll om nĺgon spelare befinner sig pĺ del tvĺ av stagen
							for(Player p : players) {
								float divider = getPosition().x + length() / 2;
								if(p.getPosition().x > divider) {
									//mEnemies = null;
									//mEnemies = new HashMap<Integer, Enemy>();
									if(!mPartTwo) {
										mEnemies.clear();
										mPartTwo = true;	
									} else {
										mNoMoreSpawn = true;
									}
								}	
							}
						}
					}
				}
			}
		}	
	}

	public boolean isThereObjects() {
		boolean objects = false;
		
		if(mObjects != null) { 
			for(GameObject obj : mObjects) {
				if (obj.getType() != Type.SIGN) {
					if(obj.getState() != State.DEAD)
						objects = true;
				}
			}
		}
		
		return objects;
	}
	
	/**
	 * Koll om all objekt som krävs är upplockade
	 * @return
	 */
	public void isRequiredObjectsPickedUp() {
		if(mObjects != null) {
			for(GameObject obj : mObjects) {
				if(obj.getState() != State.DEAD) {
					if(obj.isPickedUp())
						obj.setState(State.DEAD);
				}
			}
		}
	}
	
	/**
	 * Kollar om alla krav som finns pĺ stagen är uppfyllda
	 * @return
	 */
	public boolean isStageCleared() {
		
		boolean o = false, e = false, s = false;
		
			o = true;
 			// Objekt mĺste kollas
 		for(GameObject obj : mObjects) {
 			if(obj.isRequired()) {
 				if(obj.getState() != State.DEAD)
 					o = false;
 			}
 		}
		
 		s = true;
// 		if(mRequirement[i].signs() == 0) {
//			s = true;
//		} else {
//			// Signs mĺste kollas
//			s = true;
//		}
		

		// Fiender mĺste kollas
		e = true;
		if(mNoMoreSpawn) {
			// Slĺr tillbaka till false om nĺgon fiende fortfarande lever
			for(Iterator<Entry<Integer, Enemy>> it = mEnemies.entrySet().iterator(); it.hasNext();) {
				Enemy enemy = it.next().getValue();
				if(enemy.isAlive())
					e = false;
			}
			
			// Kan ocksĺ vara sĺ att arrayen är clearad
			if(mEnemies.size() == 0) {
				e = true;
			}
		} else {
			e = false;
		}
		
		//System.out.println("E: "+e + " O: "+o + " S: "+s);
		// Kolla sĺ alla switchar gĺtt igenom och att stagen är clearad
		if(e && o && s) {
			return true;
		} else {
			return false;	
		}
	}
	
	// ---------------------------------
	// GETTERS & SETTERS
	// ---------------------------------
	
	public Stages name() {
		return mThisStage;
	}
	
	public Vector2 getPosition() {
		return mPosition;
	}
	
	public Map<Integer, Enemy> enemies() {
		return mEnemies;
	}
	
	public void addEnemy(Enemy enemy) {
		// Spawna fienden pĺ klienten
		enemy.spawn(false);
		mEnemies.put(enemy.getID(), enemy);
	}	

	public ArrayList<Sign> signs() {
		return mSigns;
	}
	
	public ArrayList<GameObject> objects() {
		return mObjects;
	}
	
	public GameObject getObjectById(int id) {
		for(GameObject obj : objects()) {
			if (obj.getID() == id)
				return obj;
		}
		
		return null;
	}
	
	public StageStatus getStatus() {
		return mStatus;
	}
	
	public void setStatus(StageStatus status) {
		mStatus = status;
	}
	
	public Part getPart(int nr) {
		return mParts[nr];
	}
	
	public float length() {
		return mParts[0].getPartLength() + mParts[1].getPartLength();
	}
	
	public boolean isBossTime() {
		return mIsBossTime;
	}
	
	public void addGameObject(GameObject obj) {
		mObjects.add(obj);
	}
	
	public float getMaxY() {
		return MAX_Y;
	}
	
	public float getMinY() {
		return MIN_Y;
	}

	/**
	 * Enum för att sätta status pĺ 
	 * stages
	 * @author Emil
	 *
	 */

	enum StageStatus {
		CURRENT, PASSED, COMING;
	}
	
	/**
	 * Hjälpklass för hantering av en stages
	 * delar
	 * @author Emil
	 *
	 */
	public class Part {
		
		private static final int 	MAX_NO_OF_ENEMIES_PER_WAVE = 4;
		
		private Vector2 			mPartPosition;
		private int		 			mNumber;
		private ArrayList<Wave>		mWaves;
		private int					mNoOfWaves;
		private int					mCurrentWave;
		private Boss				mBoss;
		private boolean 			mIsBossTime = false;
		
		public Part(Vector2 pos, int number) {
			setPartPosition(pos);
			setNumber(number+1);
			mWaves = new ArrayList<Wave>();
			
			mWaves.add(new Wave());
			mNoOfWaves = 1;
			mCurrentWave = 0;
		}
		
		public void addBoss(Boss boss) {
			mBoss = boss;
			mIsBossTime = true;
		}

		public void addEnemyToWave(Enemy e) {
			if(mWaves.get(mCurrentWave).getEnemies().size() > MAX_NO_OF_ENEMIES_PER_WAVE) {
				mCurrentWave++;
				mNoOfWaves++;
				mWaves.add(new Wave());
			}
			mWaves.get(mCurrentWave).addEnemy(e);
		}
		
		public void nextWave() {
			mCurrentWave++;
			if(mCurrentWave > mNoOfWaves)
				mCurrentWave = mNoOfWaves;
		}
		
		public void reset() {
			mCurrentWave = 0;
		}
		
		public ArrayList<Enemy> getCurrentWaveEnemies() {
			return mWaves.get(mCurrentWave).getEnemies();
		}
		
		public int getNoOfWaves() {
			return mNoOfWaves;
		}
		
		public int getCurrentWave() {
			return mCurrentWave;
		}
		
		public boolean isFinalWave() {
			return mNoOfWaves == mCurrentWave;
		}
		
		public float getPartLength() {
			return Camera.CAMERA_WIDTH;
		}

		public Vector2 getPartPosition() {
			return mPartPosition;
		}

		public void setPartPosition(Vector2 mPartPosition) {
			this.mPartPosition = mPartPosition;
		}

		public int getNumber() {
			return mNumber;
		}

		public void setNumber(int mNumber) {
			this.mNumber = mNumber;
		}
	}
	
	public class Wave {
		
		private ArrayList<Enemy> mEnemies;
		
		public Wave() {
			mEnemies = new ArrayList<Enemy>();
		}
		
		public void addEnemy(Enemy e) {
			mEnemies.add(e);
		}
		
		public ArrayList<Enemy> getEnemies() {
			return mEnemies;
		}
	}

	public void clear() {
		enemies().clear();
		objects().clear();
		signs().clear();
	}

//	public ArrayList<Decal> decals() {
//		return mDecals;
//	}
}


