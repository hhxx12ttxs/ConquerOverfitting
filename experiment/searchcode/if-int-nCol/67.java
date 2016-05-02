package sma.modules.centralagent;

import jade.lang.acl.ACLMessage;

import java.io.IOException;

import sma.BaseAgent;
import sma.CentralAgent;
import sma.modules.BaseCommunicationModule;
import sma.modules.harvestercoordinatoragent.GarbageManagerModule;
import sma.ontology.Cell;
import sma.ontology.InfoAgent;
import sma.ontology.InfoGame;

/**
 * Module responsible for Collect Garbage by Harvester.
 * 
 * @author Manas Kanti Dey
 * 
 */
public class DiscoverGarbage {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public void discoverGabageNearCell(ACLMessage msg) {
        String[] temp;
        String message;
        message = msg.getContent();
        temp = message.split("-");
        // New Row for Movement
        int new_row = Integer.parseInt(temp[3]);  
        // New Column for Movement
        int new_col = Integer.parseInt(temp[2]);

        // Get all the agent
        for (InfoAgent ia : InfoGame.getInstance().getInfo().getAgentsInitialPosition().keySet()) {
            if ((InfoGame.getInstance().getInfo().getMap())[new_row][new_col].getBusy() == false) {
                Cell pos = (Cell) InfoGame.getInstance().getInfo().getAgentsInitialPosition().get(ia);

                if (ia.getAgentType() == 0) { // if it is scout
                    if (Integer.parseInt(temp[0]) == pos.getColumn() && Integer.parseInt(temp[1]) == pos.getRow()) {
                       // System.out.println("centralAgent: updating Scout " + ia.id);
                        try {
                            // System.out.println("Central Move: "+pos.getRow()+" "+pos.getColumn()+" "+pos.getVisited());
                            /*
                              
                            */
                        	InfoGame.getInstance().setNewCell(pos.getRow(), pos.getColumn(),
                                    pos.getVisited() + 1);
                            ((InfoGame.getInstance().getInfo().getMap())[new_row][new_col]).setAgent(ia);
                            InfoGame.getInstance().getInfo().getAgentsInitialPosition()
                                    .put(ia, InfoGame.getInstance().getInfo().getCell(new_row, new_col));
                            ((InfoGame.getInstance().getInfo().getMap())[new_row][new_col]).setVisited();
                            (InfoGame.getInstance().getInfo().getMap())[new_row][new_col].setBusy(true);
                            (InfoGame.getInstance().getInfo().getMap())[Integer.parseInt(temp[0])][Integer
                                    .parseInt(temp[1])].setBusy(false);

                        } catch (Exception e) {
                            // System.out.println("Central Error"+e.getMessage());
                        }

                        try {
                            int map_length = InfoGame.getInstance().getInfo().getMap().length;
                            int movement[][] = { { 1, 0 }, // right
                                    { 0, 1 }, // down
                                    { -1, 0 }, // left
                                    { 0, -1 } // top
                            };
                            for (int i = 0; i < 4; i++) {
                                int nCol = new_col + movement[i][0];
                                int nRow = new_row + movement[i][1];

                                if (nRow >= 0
                                        && nCol >= 0
                                        && nCol < map_length
                                        && nRow < map_length
                                        && InfoGame.getInstance().getInfo().getCell(nRow, nCol)
                                                .getCellType() == 1) {
                                    for (Cell garbage : InfoGame.getInstance().getBuildingsGarbage()) {
                                        try {
                                            if (garbage.getRow() == nRow && garbage.getColumn() == nCol) {
                                                System.out.println("Garbage:" + garbage.getRow() + " "
                                                        + garbage.getColumn() + " " + garbage.getGarbageUnits() + " "
                                                        + garbage.getGarbageType());

                                                CentralAgent._cmCommunicationModule.send(ACLMessage.INFORM,
                                                        msg.getSender(), "", "FOUND-GARBAGE", "", garbage);
                                                
                                                InfoGame.getInstance().garbageDiscovered(garbage.getRow(), garbage.getColumn());
                                                CentralAgent.showStistic("Percentage of Building with Garbage Discovered: "+ InfoGame.getInstance().parcentageGarbageDiscovered());
                                            }
                                        } catch (Exception e) {
                                            System.out.println(e.getMessage());
                                        }
                                    }

                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Central Error" + e.getMessage());
                        }

                    }
                }
            }
        }
    }
    
    /**
     * Signleton part
     */

    private static DiscoverGarbage instance = new DiscoverGarbage();

    private DiscoverGarbage() {

    }

    public static DiscoverGarbage getInstance() {
        return instance;
    }
}

