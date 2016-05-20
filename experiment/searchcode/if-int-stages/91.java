package se.rhel.gunslinger.model;

import java.util.ArrayList;

import se.rhel.gunslinger.model.Stage.StageStatus;
import se.rhel.gunslinger.model.entities.Decal;
import se.rhel.gunslinger.model.entities.Decal.DecalParse;

import se.rhel.gunslinger.model.options.StageRequirement;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;


public class Level {

	private Levels 				mThisLevel;
	private ArrayList<Stage> 	mStages;
	private Stages 				mCurrentStage;
	private int					mCurrentStageInteger;
	private Stages				mFinalStage;
	private World				mPhysicsWorld;
	private ArrayList<Decal> 	mDecals = new ArrayList<Decal>();
	
	public Level(Levels level, int noOfStages, World world) {
		mThisLevel = level;
		// När en level initieras kan vi anta att den börjar pĺ stage 1
		mCurrentStage = Stages.STAGE1;
		mPhysicsWorld = world;
		mStages = new ArrayList<Stage>(noOfStages);
		initializeStages(noOfStages);
		//initializeLevelDecals(readLevelDecals(level));
	}

	// Första initieringen
	private void initializeStages(int noOfStages) {
		
		// Lägg till rätt antal stages till leveln
		for(int i = 1; i <= noOfStages; i++) {
			// Läs ut vilka krav som hör till stagen och lägg till

			boolean finalStage = i == noOfStages ? true : false;
			StageRequirement[] reqs = readStageRequirements(Stages.get(i));
			mStages.add(new Stage(Stages.get(i), mPhysicsWorld, reqs, new Vector2(i*(Camera.CAMERA_WIDTH*2), 0), finalStage));
		}
		
		// Börjar alltid pĺ första stagen
		mCurrentStageInteger = 1;
		mFinalStage = Stages.get(noOfStages);
		
		// Första stagen mĺste fĺ rätt status
		getCurrentStage().setStatus(StageStatus.CURRENT);
	}
	
	/**

	 * Läser ut kraven för varje stage, bestĺr egentligen av tvĺ krav
	 * frĺn stagens part
	 * @param stage
	 */

	private StageRequirement[] readStageRequirements(Stages stage) {
		// Använder hjälpmetoden i levelemanager med req-parametern satt till true

		String filenames[] = LevelManager.buildLevelStageName(mThisLevel, stage, true);
		StageRequirement[] reqs = new StageRequirement[2];
		
		for(int i = 0; i < filenames.length; i++) {
			String path = "data/lvlreq/" + filenames[i];
			
		// Läs filen och kasta till StageReq objekt
		FileHandle file = Gdx.files.internal(path);
		Json json = new Json();
		StageRequirement rq = json.fromJson(StageRequirement.class, file);
			reqs[i] = rq;	
		}
		
		return reqs;
	}
	
	public void initializeLevelDecals(ArrayList<DecalParse> parses) {
		mDecals.clear();
		for (DecalParse dp : parses) {
			mDecals.add(new Decal(dp.getPosition(), dp.getType(), mPhysicsWorld));
		}
	}
	
	public ArrayList<DecalParse> readLevelDecals(Levels level) {
		String filename = "l" + level.getAbbreviation() + ".json";
		String path = "data/decals/" + filename;
		
		FileHandle file = Gdx.files.internal(path);
		Json json = new Json();
		ArrayList<DecalParse> decalparses = new ArrayList<DecalParse>();	
		
		if (file.exists()) {
			Array decals = (Array) new JsonReader().parse(file);
			
			for (int i = 0; i < decals.size; i++) {
				DecalParse dp = json.fromJson(DecalParse.class, json.toJson(decals.get(i)));
				decalparses.add(dp);
			}
	
		}
		
		return decalparses;
	}
	
	public Levels name() {
		return mThisLevel;
	}
	
	public Stages currentStage() {
		return mCurrentStage;
	}
	
	public Stage getCurrentStage() {
		for(Stage s : mStages) {
			if(s.name() == mCurrentStage) {
				return s;
			}
		}
		return null;
	}
	
	public boolean isStageCleared() {

		// return true;
		return getCurrentStage().isStageCleared();
	}
	
	public ArrayList<Stages> allStages() {
		ArrayList<Stages> stages = new ArrayList<Stages>();
		for(Stage s : mStages) {
			stages.add(s.name());
		}
		
		return stages;
	}
	
	public Stage getSpecificStage(int stage) {
		for(Stage s : mStages) {
			if(s.name().getAbbreviation() == stage) {
				return s;
			}
		}
		return null;
	}
	
	/**
	 * Retunera ett specifikt Stage-objekt
	 * @return
	 */
	public Stage getStage(Stages stage) {
		for(Stage s : mStages) {
			if(s.name() == stage) {
				return s;
			}
		}
		return null;
	}
	
	public boolean isFinalStage() {
		if(mFinalStage.equals(mCurrentStage)) {
			return true;
		}
		return false;
	}
	
	public void nextStage() {
		mCurrentStageInteger++;
		mCurrentStage = Stages.get(mCurrentStageInteger);
		
		// Ändra status pĺ nya Stagen
		getCurrentStage().setStatus(StageStatus.CURRENT);
	}
	
	public ArrayList<Decal> decals() {
		return mDecals;
	}
	
	/*
	public Stage currentStage() {
		for(Stage s : mStages) {
			if(s.name() == mCurrentStage) {
				return s;
			}
		}
		return null;
	} */
}

