package sma.modules.harvestercoordinatoragent;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import sma.UtilsAgents;
import sma.modules.BaseModule;
import sma.ontology.AuxInfo;
import sma.ontology.Cell;
import sma.ontology.Dijkstra;
import sma.ontology.InfoAgent;
import sma.ontology.InfoAgent.HarvesterStatus;

/**
 * Module responsible for harvester movement Singleton class.
 * 
 * @author kolczak
 * 
 */
public class MoveStrategyModule extends BaseModule {
    
    HashMap<InfoAgent, ArrayList<Point>> visited = new HashMap<InfoAgent, ArrayList<Point>>();
    Random randGenerator = new Random(19580427);
    CommunicationModule communicationModule = null;
    private AuxInfo currentAuxInfo;


    // x = rowId; y = colId;
    HashMap<InfoAgent, ArrayList<Point>> harvestersMovements = new HashMap<InfoAgent, ArrayList<Point>>();

    @Override
    protected boolean doInitialize(Object... objects) {
        if (objects.length == 1) {
            CommunicationModule communicationModule = (CommunicationModule) objects[0];
            if (communicationModule instanceof CommunicationModule)
                this.communicationModule = communicationModule;
            else
                return false;
        } else
            return false;

        return true;
    }

    /**
     * Method called by others (ex. CommunicationModule) to move harvesters.
     * @param aiInfo
     * @param replyTo
     */
    public void moveHarvesters(AuxInfo aiInfo, AID replyTo) {
        //HarvesterStatus status;
        
        // initialize hashmap of harvesters
        if (harvestersMovements.size() == 0)
            initializeHarvesterMovements(aiInfo);

        // initialize hashmap of harvesters
        if (visited.size() == 0)
            initializeVisited(aiInfo);
        
        currentAuxInfo = aiInfo;
        updateCurrentAgents();       
        
        // check garbages that are unassigned.
        GarbageManagerModule.getInstance().assignFoundedGarbages();
        
        for (InfoAgent infoAgent : harvestersMovements.keySet()) {
            System.out.println("HARVESTER: " + infoAgent.id + ", status: " + infoAgent.getHarvesterStatus() + ", movementsToDo: " + harvestersMovements.get(infoAgent).size());

        	//set harvester status
        	if (harvestersMovements.get(infoAgent).size() == 1
        	        && infoAgent.getHarvesterStatus() == HarvesterStatus.ON_WAY_TO_GARBAGE) {
        	    
        	    infoAgent.setHarvesterStatus(HarvesterStatus.READY_TO_COLLECT);
        	    
        	} else if (harvestersMovements.get(infoAgent).size() == 1
        	        && infoAgent.getHarvesterStatus() == HarvesterStatus.ON_WAY_TO_RC)  {
        	    
        	    infoAgent.setHarvesterStatus(HarvesterStatus.READY_TO_EMPTY);
        	    
        	} else if (harvestersMovements.get(infoAgent).size() == 0) {
        	    
                randomlyMoveHarvester(infoAgent, aiInfo, replyTo);
                infoAgent.setHarvesterStatus(HarvesterStatus.RANDOM_MOVE);
                
            } else if (infoAgent.getHarvesterStatus() == HarvesterStatus.RANDOM_MOVE &&
                    infoAgent.getUnits() != 0) {
              
                //send it to RC (some collision occured)
                sendHarvesterToRecyclingCentre(infoAgent);
                
                
            } else
        	    if (infoAgent.getHarvesterStatus() != HarvesterStatus.ON_WAY_TO_GARBAGE &&
        	            infoAgent.getHarvesterStatus() != HarvesterStatus.ON_WAY_TO_RC)
        	        System.out.println("ERROR! wrong status: " + infoAgent.getHarvesterStatus());
        	
            moveHarvester(infoAgent, aiInfo, replyTo);
        }
    }

    /**
     * updates current information about agents received from Central Agent
     */
    private void updateCurrentAgents() {
        for( InfoAgent agent : currentAuxInfo.getAgentsInitialPosition().keySet()) {
           ArrayList<Point> moves = harvestersMovements.get(agent);
           if(moves != null)
            {
    		   for (InfoAgent infoAgent : harvestersMovements.keySet()) {
    			   if(infoAgent.id == agent.id)
    			   {
    				   agent.setHarvesterStatus(infoAgent.getHarvesterStatus());
    				   harvestersMovements.remove(agent);
    	    		   harvestersMovements.put(agent, moves);
    	    		   break;
    			   }
    		   }    		   
            }
        }
        
    }

    public void sendHarvesterToRecyclingCentre(InfoAgent aiInfo) {
        
        if (harvestersMovements.get(aiInfo) == null) {
            System.out.print("Error: no agent in harvesterMovements");
            return;
        }
        	    
	    ArrayList<Point> currentPath = null;
	    ArrayList<Point> shortestPath = null;
	    
	    if (currentAuxInfo == null)
	        System.out.println("currentAuxInfo null");
	    
	    if (currentAuxInfo.getAgentsInitialPosition().get(aiInfo) == null)
	        System.out.println("currentAuxInfo.getAgent null");
	    
	    Cell agentPosition = (Cell) currentAuxInfo.getAgentsInitialPosition().get(aiInfo);
        int x, y;
        // printout information about harvester
        x = agentPosition.getRow();
        y = agentPosition.getColumn();
        
        List<Cell> recyclingCentres = currentAuxInfo.getRecyclingCenters();
        
        for(Cell recyclingCentre : recyclingCentres) {
            
            //check if garbage type is appropriate
            int[] rcGarbagesTypes;
            try {
                rcGarbagesTypes = recyclingCentre.getGarbagePoints();
                if (rcGarbagesTypes[aiInfo.getCurrentType()] == 0)
                    continue;
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            
            // choose shortest path ( DIJKSTRA )
            Dijkstra dk = new Dijkstra();
            dk.generateMap(currentAuxInfo, x, y);
            dk.generatePath(recyclingCentre.getRow(), recyclingCentre.getColumn());
            currentPath = dk.getPath();
            
            //System.out.println("DIJKSTRA: path size: " + currentPath.size());
            if (shortestPath == null)
                shortestPath = currentPath;
            if (currentPath.size() < shortestPath.size()) {
                shortestPath = currentPath;
            }
        }
	    
        aiInfo.setHarvesterStatus(HarvesterStatus.ON_WAY_TO_RC);
        harvestersMovements.remove(aiInfo);
        harvestersMovements.put(aiInfo, shortestPath);
    }

    /**
     * Initialize harvester movements hash map.
     * 
     * @param aiInfo
     */
    private void initializeHarvesterMovements(AuxInfo aiInfo) {
        for (InfoAgent infoAgnet : aiInfo.getAgentsInitialPosition().keySet()) {
            Cell position = (Cell) aiInfo.getAgentsInitialPosition().get(infoAgnet);
            if (position.getAgent().getAgentType() == 1) {// just Harvesters 
                harvestersMovements.put(infoAgnet, new ArrayList<Point>());
            }
        }
    }
    
    /**
     * Initialize visited Cells vector for every agent.
     * 
     * @param aiInfo
     */
    private void initializeVisited(AuxInfo aiInfo) {
        for (InfoAgent infoAgnet : aiInfo.getAgentsInitialPosition().keySet()) {
            Cell position = (Cell) aiInfo.getAgentsInitialPosition().get(infoAgnet);
            if (position.getAgent().getAgentType()==1) { // just Harvesters 
                visited.put(infoAgnet, new ArrayList<Point>());
            }
        }
    }

    /**
     * Sends movement information to central agent about movements
     * 
     * @param infoAgent
     * @param aiInfo
     * @param replyTo
     */
    private void moveHarvester(InfoAgent infoAgent, AuxInfo aiInfo, AID replyTo) {
        currentAuxInfo = aiInfo;
        
        ArrayList<Point> movements = harvestersMovements.get(infoAgent);

        if (movements.size() == 0) {
            System.out.print("ERROR: movements empty");
        }

        // get current position of agent
        Cell agentPosition = UtilsAgents.getAgentPosition(aiInfo, infoAgent);

        // send new position and remove it from stack
        String content = agentPosition.getColumn() + "-" + agentPosition.getRow() + "-" + movements.get(0).y + "-"
                + movements.get(0).x + "-" + infoAgent.getHarvesterStatus();
        communicationModule.send(ACLMessage.INFORM, replyTo, null, "MOVE-HARVESTER", content, null);
        movements.remove(0);
    }

    /**
     * MOST IMPORTANT METHOD
     * Assign harvester to garbage. Use Dijkstra algorithm.
     * 
     * @param aiInfo
     * @param replyTo
     * @param garbage
     * 
     * TODO: think about synchronization with GarbageManager.
     */
    public synchronized void assignHarvesterToGarbage(ArrayList<Cell> uncollectedGarbages) {

        System.out.println("ASSIGN: Assigning Harvester to garbage");

        ArrayList<Point> currentPath = null;
        ArrayList<Point> shortestPath = null;
        InfoAgent closesAgent = null;
        HashMap<Cell, InfoAgent> assignedGarbages = new HashMap<Cell, InfoAgent>();

        // choose harvesters
        // for each garbage
        for (Cell garbage : uncollectedGarbages) {
            currentPath = null;
            closesAgent = null;
            shortestPath = null;
            
            // for each agent
            for (InfoAgent infoAgent : harvestersMovements.keySet()) {

                // if harvester has some work
                if (infoAgent.getHarvesterStatus() != HarvesterStatus.RANDOM_MOVE || !canPickUp(infoAgent, garbage))
                    continue;

                // calculate DIJKSTRA
                currentPath = getShortestPath(infoAgent, garbage);
                System.out.println("ASSIGN: Shortest path: " + currentPath.size());
                if (shortestPath == null || shortestPath.size() > currentPath.size()) {
                    shortestPath = currentPath;
                    closesAgent = infoAgent;
                }
            }
            
            // send agent
            if (closesAgent != null) {
                System.out.println("ASSIGNED: Harvester assigned: " + closesAgent.id + " garbage: " + garbage.getRow()
                        + ", " + garbage.getColumn());

                //put movements and set harvester status
                harvestersMovements.put(closesAgent, shortestPath);
                closesAgent.setHarvesterStatus(HarvesterStatus.ON_WAY_TO_GARBAGE);

                // check if all garbage will be collected
                try {
                    if (closesAgent.getMaxUnits() - closesAgent.getUnits() >= garbage.getGarbageUnits()) {
                        assignedGarbages.put(garbage, closesAgent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        //inform GarbageManagerModule about assigned garbages
        for (Cell garbage : assignedGarbages.keySet()) {
            GarbageManagerModule.getInstance().garbageProperlyAssigned(garbage, assignedGarbages.get(garbage));
        }

    }

    private ArrayList<Point> getShortestPath(InfoAgent infoAgent, Cell garbage) {
        Cell agentPosition = (Cell) currentAuxInfo.getAgentsInitialPosition().get(infoAgent);
        int x, y;
        // printout information about harvester
        x = agentPosition.getRow();
        y = agentPosition.getColumn();
        
        //type we can collect
        boolean[] gerbageType = infoAgent.getGarbageType();

        System.out.println(x + " " + y + " " + infoAgent.getMaxUnits() + " G:" + gerbageType[0] + ", P:"
                + gerbageType[1] + ", M:" + gerbageType[2] + ", A:" + gerbageType[3]);

        // choose shortest path ( DIJKSTRA )
        Dijkstra dk = new Dijkstra();
        dk.generateMap(currentAuxInfo, x, y);
        dk.generatePath(garbage.getRow(), garbage.getColumn());
        return dk.getPath();
        //System.out.println("DIJKSTRA: path size: " + currentPath.size());
    }

    private boolean canPickUp(InfoAgent infoAgent, Cell garbage) {
        boolean[] possibleTypes = infoAgent.getGarbageType();
        if (possibleTypes[garbage.getGarbageTypeInt()] == false)
            return false;
        return true;
    }

    /**
     * Generate new movement for given harvester
     * 
     * @param infoAgent
     *            is Harvester
     * @param aiInfo
     * @param replyTo
     */
    private void randomlyMoveHarvester(InfoAgent infoAgent, AuxInfo aiInfo, AID replyTo) {
        // array of possible movements, indexes: column, row
        int movement[][] = { { 1, 0 }, // right
                { 0, 1 }, // down
                { -1, 0 }, // left
                { 0, -1 } }; // right
        ArrayList<Point> movementsList = new ArrayList<Point>();
        
        Cell agentPosition = UtilsAgents.getAgentPosition(aiInfo, infoAgent);
        if (agentPosition == null) return;

        while(movementsList.size() == 0) {
            for (int i = 0; i < 4; i++) {
                int nCol = agentPosition.getColumn() + movement[i][0];
                int nRow = agentPosition.getRow() + movement[i][1];
                Point newCell = new Point(nRow, nCol);
    
                if (!visited.get(infoAgent).contains(newCell) && nRow >= 0 && nCol >= 0 && nCol < aiInfo.getMap().length
                        && nRow < aiInfo.getMap().length && aiInfo.getCell(nRow, nCol).getCellType() == 2
                        && aiInfo.getCell(nRow, nCol).getBusy() == false) {
                    movementsList.add(newCell);
                    aiInfo.getCell(nRow, nCol).setBusy(true);
                }
            }
            if (movementsList.size() == 0)         //if we are in dead end 
                visited.get(infoAgent).clear();
            	Point newCell = new Point(agentPosition.getRow(), agentPosition.getColumn());
            	movementsList.add(newCell);
            	aiInfo.getCell(agentPosition.getColumn(), agentPosition.getRow()).setBusy(true);
        }
        int randomIndex = randGenerator.nextInt(movementsList.size());
        Point new_move = movementsList.get(randomIndex);

        // avoid cycles and clean old moves
        if (visited.get(infoAgent).size() >= 5)
            visited.get(infoAgent).remove(0);

        // add move to movements list
        harvestersMovements.get(infoAgent).add(new_move);
        
        //add movement to visited
        visited.get(infoAgent).add(new_move);
    }
    
    /**
     * Signleton part
     */

    private static MoveStrategyModule instance = new MoveStrategyModule();

    private MoveStrategyModule() {

    }

    public static MoveStrategyModule getInstance() {
        return instance;
    }
}

