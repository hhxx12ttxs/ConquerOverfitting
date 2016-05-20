package se.rhel.gunslinger.model;

import java.util.HashMap;

import se.rhel.gunslinger.Resources;
import se.rhel.gunslinger.model.Stage.StageStatus;
import se.rhel.gunslinger.observers.ScreenChangeObserver;
import se.rhel.gunslinger.observers.SpawnObserver;
import se.rhel.gunslinger.observers.StageObserver;
import se.rhel.gunslinger.screens.Screens;

import aurelienribon.bodyeditor.BodyEditorLoader;
import aurelienribon.bodyeditor.BodyEditorLoader.PolygonModel;
import aurelienribon.bodyeditor.BodyEditorLoader.RigidBodyModel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

/**
 * Sköter hantering av Levlar och deras Stages
 * @author Emil
 *
 */
public class LevelManager {
	
	private static final Levels 	MAX_LEVEL = Levels.LEVEL2;

	private World 					mPhysicsWorld;	
	private HashMap<Levels, Level> 	mLevels;
	private StageObserver			mStageObserver;
	
	private Levels 					mCurrentLevel;
	private int						mCurrentIntegerLevel = 1;
	private boolean					mCreateOnlyWalls;
		
	private SpawnObserver			mSpawnObserver;
	private ScreenChangeObserver	mScreenObserver;
	private boolean					mLevelRead = false;
	private boolean					mIsServerStageCleared = false;
		
	public LevelManager(World world, boolean createOnlyWalls, SpawnObserver spawnObserver, ScreenChangeObserver screenObserver) {
		mPhysicsWorld = world;
		mCreateOnlyWalls = createOnlyWalls;
		mSpawnObserver = spawnObserver;
		mScreenObserver = screenObserver;
		
		initializeLevels();
	}
	
	public void setStartLevel(Levels startLevel) {
		System.out.println("LEVEL CHANGED!!!! " + startLevel);
		setCurrentLevel(startLevel.getAbbreviation());
		// När leveln är inläst
		mLevelRead = setLevel(startLevel);
		
		getCurrentLevel().initializeLevelDecals(getCurrentLevel().readLevelDecals(startLevel));
	}
	
	public Levels getLevelID() {
		return mCurrentLevel;
	}
		
	/**
	 * Skapa upp hashmapen med levels
	 */
	private void initializeLevels() {
		mLevels = new HashMap<Levels, Level>();
		
		// OBS! Mĺste specifiera antal Stages här vid initiering
		// annars självgĺende
		mLevels.put(Levels.LEVEL1, new Level(Levels.LEVEL1, 2, mPhysicsWorld));
		mLevels.put(Levels.LEVEL2, new Level(Levels.LEVEL2, 1, mPhysicsWorld));
	}
	
	public boolean setLevel(Levels level) {
		
		boolean done = false;
		switch(level) {
		case LEVEL1:
				done = createLevelCollisionObjects(mLevels.get(Levels.LEVEL1));
			break;
			
		case LEVEL2:
				done = createLevelCollisionObjects(mLevels.get(Levels.LEVEL2));
			break;
		default: assert false;
		}
		
		return done;
	}
	
	/**
	 * Skapar polygon-bodies frĺn .json filer
	 * @param filename namnet pĺ .json filen i data-katalogen
	 * @param mName namnet frĺn Physics body editor
	 * @return positionen
	 */
	private boolean createLevelCollisionObjects(Level level) {
		
		// Mĺste göra det för varje stage pĺ leveln
		for(int i = 0; i < level.allStages().size(); i++) {
			
			// 0. Ladda in .json filen frĺn Physics body editor
			String[] filenames = buildLevelStageName(level.name(), level.allStages().get(i), false);
			
			for(int n = 0; n < filenames.length; n++) {
				// .. namnet fĺr vi frĺn filnamnet - .json
				String name = filenames[n].split(".json")[0];
				BodyEditorLoader loader = new BodyEditorLoader(Gdx.files.internal("data/" + filenames[n]));
				
				float minY = 16;
				float maxY = 0;
				// loader.getInternalModel().rigidBodies.get("aa").polygons.get(1).vertices.get(1).y;
				for(RigidBodyModel rbm : loader.getInternalModel().rigidBodies.values()) {
					for(PolygonModel pm : rbm.polygons) {
						for(Vector2 v : pm.vertices) {
							if(v.y * 16f < minY)
								minY = v.y * 16f;
							if(v.y * 16f > maxY)
								maxY = v.y * 16f;
						}
					}
				}
				System.out.println("Min Y: " + minY);
				System.out.println("Max Y: " + maxY);
				
				// 1. Skapa body def (borde ha namnkonvention pĺ själva banans stage-namn)
				BodyDef bd = new BodyDef();
				bd.position.set(getCurrentStagePosition(level.name(), level.allStages().get(i), n));
				bd.type = BodyType.StaticBody;
				
				// 2. Skapa FixtureDef
				FixtureDef fd = new FixtureDef();
				fd.density = 1;
				fd.friction = 0.0f;
				fd.restitution = 0.0f;
				fd.filter.categoryBits = Filter.CATEGORY_SCENERY;
				fd.filter.maskBits = Filter.MASK_SCENERY;		
				
				// 3. Skapa kroppen
				Body groundModel = mPhysicsWorld.createBody(bd);
				groundModel.setUserData(this);
				
				// 4. Ska body fixture automatiskt genom bodyeditorloader
				loader.attachFixture(groundModel, name, fd, 16f);
				
				// 5. Lägg till stagen i arrayen
				level.getStage(level.allStages().get(i)).initialize(bd.position, mCreateOnlyWalls, n, mSpawnObserver, minY, maxY);
			}
		}
		
		return true;
	}
	
	/**
	 * Hämta ut positionerna för varje stage
	 * @param level
	 * @param stage
	 * @return
	 */
	private Vector2 getCurrentStagePosition(Levels level, Stages stage, int part) {
		int x = 0;
		// Bias-värde
		float y = 0.5f;
		Vector2 position = new Vector2(x, y);
		
		// Kan lösa det snyggare med en liknande uträknin som tar hänsyn till level
		// position.x = stage.getAbbreviation() - 1 * 16;
		
		switch(level) {
		case LEVEL1:
			switch(stage) {
			case STAGE1:
				if(part == 0) 
					position.x = 0;
				else 
					position.x = 16;
				break;
			case STAGE2:
				if(part == 0)
					position.x = 32;
				else
					position.x = 48;
				break;
			default:
				break;
			}
			break;
			
		case LEVEL2:
			switch(stage) {
			case STAGE1:
				if(part == 0)
					position.x = 0;
				else
					position.x = 16;
				break;
			case STAGE2:
					position.x = 32;
				break;
			default:
				break;
			}
		}
		
		return position;
	}
	
	/**
	 * Skapar namn beroende pĺ Level och Stage
	 * Namnstandard enligt lxsypz där x = levelsiffra, y = stagesiffra, z = part ex. l1s2p1
	 * @param level
	 * @param stage
	 * @return filnamnet
	 */
	public static String[] buildLevelStageName(Levels level, Stages stage, boolean reqs) {
		String levelPart = "";
		String stagePart = "";
		String[] full = new String[2];
		
		for(int i = 0; i < Levels.values().length; i++) {
			if(Levels.values()[i].equals(level)) {
				levelPart = "l" + (1 + i);
			}
			for(int n = 0; n < Stages.values().length; n++) {
				if(Stages.values()[n].equals(stage)) {
					stagePart = "s" + (1 + n);
				}
			}
		}
		
		for(int p = 0; p < full.length; p++) {
			String reqPart = reqs ? "_req" : "";
			int partPart = p+1;
			full[p] = levelPart + stagePart + "p" + partPart +  reqPart + ".json";	
		}
		
		return full;
	}

	/**
	 * Kallas när spelare försöker byta stage
	 * Hanterar currentLevel, currentStage
	 * Mĺste uppfylla vissa krav, som att alla fiender mĺste vara döda
	 * @return
	 */
	public boolean trySwitchStage() {
		// TODO: Koll ifall alla fiender är döda
		Level level = getCurrentLevel();
		if(!level.isStageCleared()) {
			return false;
		}
		
		return switchStage();
	}
	
	public boolean isCurrentStageCleared() {
		return getCurrentLevel().isStageCleared();
	}
	
	public boolean isCurrentServerStageCleared() {
		return mIsServerStageCleared;
	}
	
	public void setStageCleared(boolean b) {
		mIsServerStageCleared = b;
	}
	
	public boolean switchStage() {
		mIsServerStageCleared = false;
		
		Level level = getCurrentLevel();
		if(level.isFinalStage()) {
			// Ska öka level
			if(mCurrentLevel.equals(MAX_LEVEL)) {
				return false;
			} else {
				// Dags att byta level - level avklarad
				
				// Manipulera gamestaten
				Resources.getInstance().gameState.newLevelCleared(mCurrentIntegerLevel);
				
				mLevelRead = false;
				if(mScreenObserver != null) {
					mScreenObserver.changeScreen(Screens.CHOOSE);
				}
				return false;	
			}
		} else {
			// Ställ tillbaka väggen och ändra status pĺ stagen
			level.getCurrentStage().setSensor(false);
			level.getCurrentStage().setStatus(StageStatus.PASSED);
			// Ska öka stage
			level.nextStage();
		}
		return true;
	}
	
	public boolean isLevelRead() {
		return mLevelRead;
	}
	
	public Level getCurrentLevel() {
		return mLevels.get(mCurrentLevel);
	}
	
	private void setCurrentLevel(int level) {
		mCurrentIntegerLevel = level;
		mCurrentLevel = Levels.get(level);
	}
}

enum Stages {
	STAGE1(1), STAGE2(2), STAGE3(3), STAGE4(4);
	
	private final int abbreviation;
	private static final HashMap<Integer, Stages> lookup = new HashMap<Integer, Stages>();
	static {
		for (Stages s : Stages.values())
			lookup.put(s.getAbbreviation(), s);
	}
	
	private Stages(int abbreviation) {
		this.abbreviation = abbreviation;
	}
	
	public int getAbbreviation() {
		return abbreviation;
	}
	
	public static Stages get(int abbreviation) {
		return lookup.get(abbreviation);
	}
}

