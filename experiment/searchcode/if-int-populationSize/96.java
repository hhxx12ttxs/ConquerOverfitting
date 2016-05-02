package com.matjazmuhic;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;

import com.ajexperience.utils.DeepCopyException;
import com.jme3.app.SimpleApplication;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext;
import com.matjazmuhic.ga.GaManager;
import com.matjazmuhic.persistence.OrganismRepository;
import com.matjazmuhic.persistence.PropertiesStore;
import com.matjazmuhic.tree.OrganismTree;
import com.matjazmuhic.util.Dictionary;
import com.matjazmuhic.util.KeyInputActionListener;
import com.matjazmuhic.util.MainJudge;

public class OrganismEvolution extends SimpleApplication
{
	boolean headless;
	boolean testsStarted;	
	boolean flag = false;
	
	BulletAppState bulletAppState;
	KeyInputActionListener keyActionListener;
	Camera mainCam;
	List<Material> materialsStore;
	
	GaManager gaManager;
	Dictionary dictionary;
	int generationNum = 0;
	int numGenerations;
	int populationSize;
	ExecutorService organismExecutor;
	ExecutorService mainJudgeExecutor;
	List<Future<Float>> resultsFuturesList;
	List<Organism> organismList;
	
	public OrganismEvolution()
	{	
	}
	
	public static void main(String[] args) 
	{
		Logger.getLogger("com.jme3").setLevel(Level.SEVERE);
		Logger.getLogger("com.bulletphysics").setLevel(Level.SEVERE);
		OrganismEvolution app = new OrganismEvolution();
		app.keyActionListener = new KeyInputActionListener(app);
		app.headless = Boolean.parseBoolean(PropertiesStore.getIstance().get("headless"));
		app.organismList = new ArrayList<Organism>();
		app.numGenerations = Integer.valueOf(PropertiesStore.getIstance().get("numGenerations"));
		app.populationSize = Integer.valueOf(PropertiesStore.getIstance().get("populationSize"));
	
		app.organismExecutor = Executors.newFixedThreadPool(app.populationSize);
		app.mainJudgeExecutor = Executors.newFixedThreadPool(1);
		app.resultsFuturesList = new ArrayList<Future<Float>>();

		app.dictionary = new Dictionary("resources/com/matjazmuhic/dict.txt");
		
		AppSettings settings = new AppSettings(true);
		settings.setResolution(640,480);
		settings.setFrameRate(30);
		settings.setVSync(true);
		app.setSettings(settings);
		app.setShowSettings(false);
		
		app.materialsStore = new ArrayList<Material>();

		if(app.headless)
		{
			app.start(JmeContext.Type.Headless);
		}
		else
		{
			app.start();
		}
		
	}

	@Override
	public void simpleInitApp()
	{		
		initCamera();
		initPhysics();
		keyActionListener.mapKeys();

		/*
		makePopulation(populationSize);
		gaManager = new GaManager(this);
		*/
		
		/* single organism load */
		
		
		Node sceneNode = new Node("sceneNode");
		OrganismTree ot = OrganismRepository.getInstance().readFromXml("G19_analogue_indeed_9");
		List<OrganismTree> l = new ArrayList<OrganismTree>();
		l.add(ot);
		makePopulation(l);
		
	}
	
	@Override
	public void update() 
	{
		super.update();

		boolean finished = false;

		if(generationNum==numGenerations && !flag)
		{
			flag = true;
			OrganismRepository.getInstance().printResults();
			System.out.println("=================================!");
			System.out.println("FINISHED!");
			System.out.println("FINISHED!");
			System.out.println("FINISHED!");
			System.out.println("=================================!");
		}
			
		if(testsStarted)
		{
			for(Future<Float> f: resultsFuturesList)
			{
				finished = f.isDone();
			}

			if(finished)
			{
				for(Organism o: organismList)
				{
					o.getOrganismJme().getOrganismTimer().setFinished(true);
				}
				
				if(generationNum<=numGenerations)
				{					
					testsStarted=false;
					List<OrganismTree> newGen = null;
					cleanUpPhysics();
					
					try
					{
						newGen = gaManager.step(generationNum);
						System.out.println("GENERATION "+generationNum);
					} 
					catch (DeepCopyException e) 
					{
						e.printStackTrace();
					}
					
					rootNode.detachAllChildren();
					organismList.clear();
					makePopulation(newGen);
				}
			}
		}	
		
	};
	
	@Override
	public void destroy() 
	{
		OrganismRepository.getInstance().printResults();
		
		for(Organism organism: organismList)
		{
			organism.notifyDestroy();
		}
		
		organismExecutor.shutdownNow();
		mainJudgeExecutor.shutdownNow();
		
		super.destroy();
		
	};
	
	private void cleanUpPhysics()
	{
		resultsFuturesList.clear();
		
		for(Organism organism: organismList)
		{
			organism.notifyDestroy();
			for(HingeJoint hjTemp: organism.getOrganismJme().jointsMap.keySet())
			{
				bulletAppState.getPhysicsSpace().remove(hjTemp);
			}	
			organism = null;
		}
		
		for(Spatial spatial: rootNode.getChildren())
		{
			bulletAppState.getPhysicsSpace().removeAll(spatial);
		}
		
	}
		
	private void initCamera()
	{
		cam.setLocation(new Vector3f(50f, 50f, 150f));
		cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
		mainCam = cam;
	}
	
	private void initPhysics()
	{
		bulletAppState = new BulletAppState();
		stateManager.attach(bulletAppState);
		
		bulletAppState.setSpeed(1.0f);
		bulletAppState.setEnabled(true);
		bulletAppState.getPhysicsSpace().setGravity(new Vector3f(0.0f, -400.0f, 0.0f));
		bulletAppState.getPhysicsSpace().setAccuracy(0.01f);
		//bulletAppState.getPhysicsSpace().setMaxSubSteps(10);
		bulletAppState.getPhysicsSpace().enableDebug(assetManager);
	}
	
	private void addFloor(Node node)
	{
		Vector3f min = ((BoundingBox)node.getWorldBound()).getMin(null);
		Box f = new Box(Vector3f.ZERO, 10000.0f, 10.0f, 10000.f);
		Geometry floor = new Geometry(UUID.randomUUID().toString(), f);
		floor.setShadowMode(ShadowMode.Receive);
		Material matf = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		matf.setColor("Color", ColorRGBA.Gray);
		floor.setMaterial(matf);
		floor.move(0, min.y-50.0f, 0);
		RigidBodyControl fc = new RigidBodyControl(0.0f);
		fc.setCollisionGroup(1);
		floor.addControl(fc);
		floor.getControl(RigidBodyControl.class).setFriction(50.0f);
		fc.setPhysicsLocation(floor.getWorldTranslation());
		bulletAppState.getPhysicsSpace().add(fc);
		floor.setShadowMode(ShadowMode.Receive);
		rootNode.attachChild(floor);
	}

	public void makePopulation(int size)
	{
		generationNum++;
		float heightDiff = 300.0f;
		
		for(int i=0; i<size; i++)
		{
			Node organismNode = new Node();
			rootNode.attachChild(organismNode);
			Organism organism;
			organism = OrganismFactory.getInstance(this).createRandomOrganism(organismNode);
			organismExecutor.execute(organism.getOrganismJme().getOrganismTimer());
			organism.move(new Vector3f(0.0f, i*heightDiff, 0.0f));
			organismList.add(organism);
			addFloor(organismNode);
		}
		createJudge();
	}
	
	public void makePopulation(List<OrganismTree> oTrees)
	{		
		generationNum++;
		float heightDiff = 300.0f;
		
		for(int i=0; i<oTrees.size(); i++)
		{
			
			Node organismNode = new Node();
			rootNode.attachChild(organismNode);
			Organism organism;
			organism = OrganismFactory.getInstance(this).createFromTree(oTrees.get(i), organismNode);
			organismExecutor.execute(organism.getOrganismJme().getOrganismTimer());
			organism.move(new Vector3f(0.0f, i*heightDiff, 0.0f));
			organismList.add(organism);
			addFloor(organismNode);
		}	
		createJudge();
	}
	
	public void createJudge()
	{
		MainJudge j = new MainJudge(this, generationNum);
		Future<Float> fJudge = mainJudgeExecutor.submit(j);
		resultsFuturesList.add(fJudge);
		testsStarted = true;
	}
	
	//Getters
	public BulletAppState getBulletAppState()
	{
		return bulletAppState;
	}

	public List<Material> getMaterialsStore() 
	{
		return materialsStore;
	}

	public Camera getMainCam() 
	{
		return mainCam;
	}
	
	public List<Organism> getOrganismList() 
	{
		return organismList;
	}

	public Dictionary getDictionary()
	{
		return dictionary;
	}
	
}

