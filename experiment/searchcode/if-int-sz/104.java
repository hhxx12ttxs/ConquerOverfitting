package norc.pgrd.demo;

import java.util.Random;

import norc.SimpleDriver;
import norc.domains.demo.DemoSim;
import norc.domains.demo.DemoState;
import norc.domains.demo.Maze;
import norc.pgrd.Agent_PGRDUCT;


/**
 * Runs PGRD UCT on simple maze.
 * @author Jeshua Bratman
 */
public class DemoPGRD {
	public static void main(String[] args) throws InterruptedException {
		// real world
		Random rand1 = new Random();
		int sz = 5;
		Maze maze = new Maze(Maze.randomMaze(sz, sz, rand1));   
		//Maze maze = new Maze(new int[sz][sz]);
		maze.setCell(sz-1, sz-1, Maze.G);
		DemoSim.maze = maze;
		DemoSim simReal = new DemoSim(rand1);
		
		// simulator for planning
		Random rand2 = new Random();
		DemoSim simPlan = new DemoSim(rand2);
		DemoRFunction rf = new DemoRFunction();
				
		int trajectories = 500;		
		int depth = 4;
		double alpha = .001;
		double temperature = .1;
		double gamma = .95;
		Agent_PGRDUCT<DemoState> pgrd = new Agent_PGRDUCT<DemoState>(simPlan,rf,alpha,temperature,trajectories,depth,gamma,rand2);		
		DemoVisualizeR p = new DemoVisualizeR(DemoSim.maze,pgrd.getRF());
		SimpleDriver<DemoState> driver = new SimpleDriver<DemoState>(simReal,pgrd);
		for (int timestep = 0;; timestep++) {
			driver.step();			
			DemoState curr_state = (DemoState)driver.curr_state;
			if(timestep > 10000 || ((timestep%30)==0)){
				p.redraw(curr_state.x, curr_state.y);
				Thread.sleep(10);
			}
		}
	}
}

