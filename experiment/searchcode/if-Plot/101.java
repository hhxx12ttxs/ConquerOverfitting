package pathfinder;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Vector;

import map.Cell;
import map.Cell.Value;
import map.Map;
import uchicago.src.reflector.ListPropertyDescriptor;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.space.Object2DGrid;
import uchicago.src.sim.util.SimUtilities;
import utils.Config;
import agents.ArmyUnit;
import agents.Captain;
import agents.Exit;
import agents.Robot;
import agents.Soldier;
import agents.Visited;
import agents.Wall;

public class ArmyPathFinderModel extends SimModelImpl {
	private static final char WALL = '#';
	private static final char SOLDIER = 's';
	private static final char CAPTAIN = 'c';
	private static final char ROBOT = 'r';
	private static final char EXIT = 'E';
	private ArrayList<ArmyUnit> agentList;
	private Schedule schedule;
	private DisplaySurface dsurf;
	private Object2DGrid space;
	private int xSize;
	private int ySize;
	private Map map;
	private Config conf;
	private OpenSequenceGraph plot;
	private OpenSequenceGraph plot2;


	public enum MapName {
		Map1("mapa1.txt"),Map2("mapa2.txt"),Map3("mapa3.txt"), Map4("mapa4.txt"),Map5("mapa5.txt");
		
		private final String text;

		  MapName(String text) {
		    this.text = text;
		  }
		  
		  
		 
		  @Override
		  public String toString() {
		    return this.text;
		  }
	}
	
	public ArmyPathFinderModel() {
		conf = new Config(100,2,1,3,3,4,100,false,MapName.Map1,150,false);
		
		

	}

	public String getName() {
		return "Army Unit Path Finding";
	}

	public String[] getInitParam() {
		return new String[] { "live_robot", "EMPTYWEIGHT", "UNKOWNWEIGHT", "DISPERSIONWEIGHT",
				"communication_Range", "sight_Range","TIMEOUT","VERBOSE","MapName","BatteryLife","VisualizeComm" };
	}

	public Schedule getSchedule() {
		return schedule;
	}

	public int getlive_robot() {
		return conf.getLive_robot();
	}

	public void setlive_robot(int live_robot) {
		conf.setLive_robot(live_robot);
	}
	
	
	public float getEMPTYWEIGHT() {
		return conf.getEMPTYWEIGHT();
	}

	public void setEMPTYWEIGHT(float EMPTYWEIGHT) {
		conf.setEMPTYWEIGHT(EMPTYWEIGHT);
	}
	
	public float getUNKOWNWEIGHT() {
		return conf.getUNKOWNWEIGHT();
	}

	public void setUNKOWNWEIGHT(float UNKOWNWEIGHT) {
		conf.setUNKOWNWEIGHT(UNKOWNWEIGHT);
	}
	
	public float getDISPERSIONWEIGHT() {
		return conf.getDISPERSIONWEIGHT();
	}

	public void setDISPERSIONWEIGHT(float DISPERSIONWEIGHT) {
		conf.setDISPERSIONWEIGHT(DISPERSIONWEIGHT);
	}
	
	public int getcommunication_Range() {
		return conf.getCommunication_Range();
	}

	public void setcommunication_Range(int communication_Range) {
		conf.setCommunication_Range(communication_Range);
	}
	
	public int getsight_Range() {
		return conf.getSight_Range();
	}

	public void setsight_Range(int sight_Range) {
		conf.setSight_Range(sight_Range);
	}
	
	public int getTIMEOUT() {
		return conf.getTIMEOUT();
	}

	public void setTIMEOUT(int TIMEOUT) {
		conf.setTIMEOUT(TIMEOUT);
	}
	
	public boolean getVERBOSE() {
		return conf.isVERBOSE();
	}

	public void setVERBOSE(boolean VERBOSE) {
		conf.setVERBOSE(VERBOSE);
	}
	
	public MapName getMapName(){
		
		return conf.getMapName();
	}
	public void setMapName(MapName mn){
		conf.setMapName(mn);
		
	}
	
	public int getBatteryLife(){
		return conf.getRadioBattery();
	}
	public void setBatteryLife(int bl){
		conf.setRadioBattery(bl);
	}
	
	
	public boolean getVisualizeComm() {
		return conf.isVisualizeComm();
	}

	public void setVisualizeComm(boolean VERBOSE) {
		conf.setVisualizeComm(VERBOSE);
	}
	
	public void setup() {
		schedule = new Schedule();
		if (dsurf != null)
			dsurf.dispose();
		dsurf = new DisplaySurface(this, "Army PathFinder");
		dsurf.getBounds().width = 600;
		dsurf.getBounds().height = 200;
		registerDisplaySurface("Army PathFinder", dsurf);

		
		Vector<MapName> vMN = new Vector<MapName>();
		for(int i=0; i<MapName.values().length; i++) {
			vMN.add(MapName.values()[i]);
		}
		descriptors.put("MapName", new ListPropertyDescriptor("MapName", vMN));
	
		 

	}

	public void begin() {

		buildModel();
		buildDisplay();
		buildSchedule();
	
	}

	public void buildModel() {
		agentList = new ArrayList<ArmyUnit>();
		readMap(conf.getMapName().toString());

	
	}

	private void buildDisplay() {
		// space and display surface
		Object2DDisplay display = new Object2DDisplay(space);

		dsurf.addDisplayableProbeable(display, "Agents Space");
		dsurf.display();
		
		// graph
		if (plot != null) plot.dispose();
		plot = new OpenSequenceGraph("Map Explored", this);
		plot.setAxisTitles("time", "n");
		// plot number of different existing colors
		plot.addSequence("Number of global Unknowns", new Sequence() {
			public double getSValue() {
				return map.getNumberOf(Value.Unknown);
			}
		});
		// plot number of agents with the most abundant color
		plot.addSequence("Number of global Visited", new Sequence() {
			public double getSValue() {
				return map.getNumberOf(Value.Visited);
			}
		});
		
		plot.addSequence("Number of global Empty", new Sequence() {
			public double getSValue() {
				return map.getNumberOf(Value.Empty);
			}
		});
		plot.addSequence("Number of known Unreacheable", new Sequence() {
			public double getSValue() {
				return map.getNumberOf(Value.Unreachable);
			}
		});
		plot.display();
		
		if (plot2 != null) plot2.dispose();
		plot2 = new OpenSequenceGraph("Agents", this);
		plot2.setAxisTitles("time", "n");
		// plot number of different existing colors
		plot2.addSequence("Agents in the maze", new Sequence() {
			public double getSValue() {
				int n = 0;
				for(ArmyUnit a : agentList)
					if(a.getValue() != Value.Robot)
						n++;
				return n;
			}
		});
		
		plot2.addSequence("Knows exit location", new Sequence() {
			public double getSValue() {
				int n = 0;
				for(ArmyUnit a : agentList)
					if(a.knowsExitLocation() && a.getValue() != Value.Robot)
						n++;
				return n;
			}
		});
		plot2.display();
		
		

	}

	private void buildSchedule() {
		schedule.scheduleActionBeginning(0, new MainAction());
		schedule.scheduleActionAtInterval(1, dsurf, "updateDisplay",
				Schedule.LAST);
	    schedule.scheduleActionAtInterval(1, plot, "step", Schedule.LAST);
	    schedule.scheduleActionAtInterval(1, plot2, "step", Schedule.LAST);
	}

	class MainAction extends BasicAction {

		public void execute() {

			// shuffle agents
			SimUtilities.shuffle(agentList);
			if(conf.isVERBOSE()){
				System.out.println("Another it");
				System.out.println(agentList);
			}
			// iterate through all agents
			for (int i = 0; i < agentList.size(); i++) {
				ArmyUnit agent = (ArmyUnit) agentList.get(i);

				
				if (!agent.hasExited()) {
					agent.resetColor();
					agent.lookAround();
					agent.move();		
					agent.broadcastMap();
					updateGlobalMap(agent.getMap(), agent.getValue());
					
				
				}else{
					space.putObjectAt(agent.getX(), agent.getY(), new Visited(agent.getX(),agent.getY()));
					agentList.remove(agent);
				}
				

			}
		}

	}

	public static void main(String[] args) {
		SimInit init = new SimInit();
		init.loadModel(new ArmyPathFinderModel(), null, false);

	}

	public void updateGlobalMap(Map map2,Value value) {
		for (int i = 0; i < map.getY(); i++)
			for (int j = 0; j < map.getX(); j++)
				if (map.getPosition(j, i).getValue() == Value.Unknown) {
					switch (map2.getPosition(j, i).getValue()) {
					case Me:
						map.setPosition(j, i, new Cell(value, j, i));
						break;
					default:
						map.setPosition(j, i, map2.getPosition(j, i));
						break;

					}

				} else if (map.getPosition(j, i).getValue() == Value.Empty) {
					switch (map2.getPosition(j, i).getValue()) {
					case Me:
						map.setPosition(j, i, new Cell(value, j, i));
						break;
					case Visited:
						map.setPosition(j, i, new Cell(Value.Visited, j, i));
						break;
					default:
						break;

					}

				}

		
	}

	public void readMap(String filename) {

		try {
			int x = 0;
			FileInputStream fstream = new FileInputStream(filename);
			FileInputStream fstream2 = new FileInputStream(filename);

			DataInputStream in = new DataInputStream(fstream);
			DataInputStream in2 = new DataInputStream(fstream2);

			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			BufferedReader br2 = new BufferedReader(new InputStreamReader(in2));

			String firstLine = br2.readLine();

			x = firstLine.length();
			int y = 1;
			while ((firstLine = br2.readLine()) != null) {
				if (firstLine.length() > x)
					x = firstLine.length();
				y++;
			}

			xSize = x;
			ySize = y;
			space = new Object2DGrid(xSize, ySize);
			map = new Map(xSize, ySize);

			int i = 0, j = 0;
			String line;
			while ((line = br.readLine()) != null) {
				while (line.length() > j) {
					switch (line.charAt(j)) {
					case WALL:
						Wall w = new Wall(j, i);

						space.putObjectAt(j, i, w);
						 map.setPosition(j, i, new Cell(Value.Unknown,j,i));
						break;
					case SOLDIER:
						Soldier s = new Soldier(j, i, space, conf);
						agentList.add(s);
						space.putObjectAt(j, i, s);
						map.setPosition(j, i, new Cell(Value.Soldier, j, i));
						break;
					case CAPTAIN:
						Captain c = new Captain(j, i, space,conf);
						agentList.add(c);
						space.putObjectAt(j, i, c);
						map.setPosition(j, i, new Cell(Value.Captain, j, i));
						break;
					case ROBOT:
						Robot r = new Robot(j, i, space,conf);
						agentList.add(r);
						space.putObjectAt(j, i, r);
						map.setPosition(j, i, new Cell(Value.Robot, j, i));
						break;
					case EXIT:
						Exit e = new Exit(j, i);
						space.putObjectAt(j, i, e);
						map.setPosition(j, i, new Cell(Value.Unknown, j, i));
						break;
					default:
						map.setPosition(j, i, new Cell(Value.Unknown, j, i));
						break;

					}
					j++;

				}

				j = 0;
				i++;
			}

			in.close();
			in2.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}

}

