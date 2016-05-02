package sma.modules.centralagent;

import jade.lang.acl.ACLMessage;

import java.io.IOException;

import sma.BaseAgent;
import sma.CentralAgent;
import sma.modules.BaseCommunicationModule;
import sma.modules.harvestercoordinatoragent.MoveStrategyModule;
import sma.ontology.Cell;
import sma.ontology.InfoAgent;
import sma.ontology.InfoGame;

/**
 * Module responsible for Collect Garbage by Harvester.
 * 
 * @author Manas Kanti Dey
 * 
 */

public class CollectGarbage {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    /**
     * Collect garbage near cell
     * 
     * @param mycurrentGame
     * @param msg
     *            the message get from coordinator agent
     */
    public void CollectGarbageNearCell(ACLMessage msg) {
        String[] temp;
        temp = msg.getContent().split("-");
        int new_row = Integer.parseInt(temp[3]);
        int new_col = Integer.parseInt(temp[2]);
        if ((InfoGame.getInstance().getInfo().getMap())[new_row][new_col].getBusy() == false) {
            for (InfoAgent ia : InfoGame.getInstance().getInfo().getAgentsInitialPosition().keySet()) {
                Cell pos = (Cell) InfoGame.getInstance().getInfo().getAgentsInitialPosition().get(ia);
                if (ia.getAgentType() == 1) {
                    if (Integer.parseInt(temp[0]) == pos.getColumn() && Integer.parseInt(temp[1]) == pos.getRow()) {
                        System.out.println("centralAgent: updating Harvester " + ia.id);
                        // System.out.println("Central Move: " + pos.getColumn()
                        // + " " + pos.getRow());
                        try {
                            String status = temp[4];
                            
                            if (status.equalsIgnoreCase("READY_TO_COLLECT")) 
                            {
                            	boolean is_found_garbage = false;
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
                                                    System.out.println("Garbage:" + garbage.getRow() + " " + garbage.getColumn() + " " + garbage.getGarbageUnits()
                                                            + " " + garbage.getGarbageType());
                                                    boolean[] gerbageType = ia.getGarbageType();    // The garbage type the agent can take
                                                    int garbage_type = garbage.getGarbageTypeInt(); // Get the garbage type in integer value
                                                    if (  gerbageType[garbage_type] ) {
                                                        System.out.println(" G:" + gerbageType[0] + ", P:" + gerbageType[1] + ", M:" + gerbageType[2] + ", A:"
                                                                + gerbageType[3]);
                                                        is_found_garbage = true;

                                                        if ((ia.getMaxUnits() - ia.getUnits()) >= garbage.getGarbageUnits()) {
                                                            ia.setUnits(garbage.getGarbageUnits());
                                                            ia.setCurrentType(garbage.getGarbageTypeInt());                                                            
                                                            InfoGame.getInstance().getBuildingsGarbage().remove(garbage);
                                                            InfoGame.getInstance().cleanUpCell(garbage.getRow(), garbage.getColumn());
                                                            
                                                            break;
                                                        } else {
                                                            int unit = garbage.getGarbageUnits()- (ia.getMaxUnits() - ia.getUnits());
                                                            ia.setUnits(ia.getUnits() + unit);
                                                            // ia.setCurrentType(2);
                                                            garbage.setGarbageUnits(garbage.getGarbageUnits() - (ia.getMaxUnits() - ia.getUnits()));                                                            
                                                        }
                                                        /*
                                                         * for(Cell ga:
                                                         * this.mycurrentGame
                                                         * .getBuildingsGarbage
                                                         * ()) { try{
                                                         * System.out.
                                                         * println("Garbage:"
                                                         * +ga.
                                                         * getRow()+" "+ga.getColumn
                                                         * (
                                                         * )+" "+ga.getGarbageUnits
                                                         * (
                                                         * )+" "+ga.getGarbageType
                                                         * ()); }catch(Exception
                                                         * e){
                                                         * System.out.println
                                                         * (e.getMessage()); } }
                                                         */

                                                    }
                                                    else
                                                    {
                                                    	System.out.println("Error "+gerbageType[garbage_type]+" "+garbage_type);
                                                    	System.out.println(ia.getGarbageType().toString());
                                                    }
                                                }
                                            } catch (Exception e) {
                                                System.out.println(e.getMessage());
                                            }
                                        }

                                    }
                                }

                                if (is_found_garbage) {
                                    CentralAgent._cmCommunicationModule.send(ACLMessage.INFORM, msg.getSender(), "", "HARVESTER-COLLECT-GARBAGE", "", ia);
                                }
                                else{
                                	CentralAgent._cmCommunicationModule.send(ACLMessage.INFORM, msg.getSender(), "", "HARVESTER-COLLECT-GARBAGE-FAILD", "", ia);
                                }
                            }
                            if (status.equalsIgnoreCase("READY_TO_EMPTY")) {
                            	boolean is_found_garbage = false;
                                int map_length = InfoGame.getInstance().getInfo().getMap().length;
                                int movement[][] = { { 1, 0 }, // right
                                        { 0, 1 }, // down
                                        { -1, 0 }, // left
                                        { 0, -1 } // top
                                };
                                for (int i = 0; i < 4; i++) {
                                    int nCol = new_col + movement[i][0];
                                    int nRow = new_row + movement[i][1];

                                    // System.out.println("["+nCol+":"+nRow+"] Celtype = "+CommunicationModule.mycurrentGame.getInfo().getCell(nRow,
                                    // nCol).getCellType());
                                    if (nRow >= 0
                                            && nCol >= 0
                                            && nCol < map_length
                                            && nRow < map_length
                                            && InfoGame.getInstance().getInfo().getCell(nRow, nCol)
                                                    .getCellType() == 3) {
                                        for (Cell recycleing : InfoGame.getInstance().getRecyclingCenters()) {
                                            try {
                                                if (recycleing.getRow() == nRow && recycleing.getColumn() == nCol) {
                                                    //now put the garbage to recycling center
                                                    int[] gerbagePoint = recycleing.getGarbagePoints();
                                                    // System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa Garbage Point "+gerbagePoint[0]+" "+gerbagePoint[1]+" "+gerbagePoint[2]+" "+gerbagePoint[3]+" "+ia.getCurrentType());
                                                    int garbagetype = 0;
                                                    garbagetype = ia.getCurrentType();
                                                    int current_point = gerbagePoint[garbagetype] * ia.getUnits();
                                                    ia.setUnits(0);
                                                    ia.setCurrentType(-1);
                                                                                                       
                                                    System.out.println("Current Point "+ current_point);
                                                    CentralAgent.total_point = CentralAgent.total_point+current_point;
                                                    CentralAgent.showStistic("Current Point: "+ current_point + ", Total Point: "+ CentralAgent.total_point);
                                                    break;
                                                    /*
                                                     * if(
                                                     * recycleing.getGarbageType
                                                     * () == 'P' &&
                                                     * gerbageType[1]) {
                                                     * System.out
                                                     * .println(" G:"+gerbageType
                                                     * [
                                                     * 0]+", P:"+gerbageType[1]+
                                                     * ", M:"
                                                     * +gerbageType[2]+", A:"
                                                     * +gerbageType[3]);
                                                     * is_found_garbage = true;
                                                     * 
                                                     * if( (
                                                     * ia.getMaxUnits()-ia.
                                                     * getUnits() ) >=
                                                     * recycleing
                                                     * .getGarbageUnits() ) {
                                                     * ia.setUnits(recycleing.
                                                     * getGarbageUnits());
                                                     * ia.setCurrentType
                                                     * (recycleing
                                                     * .getGarbageTypeInt());
                                                     * CommunicationModule
                                                     * .mycurrentGame
                                                     * .getBuildingsGarbage
                                                     * ().remove(recycleing);
                                                     * break; } else{ int unit =
                                                     * recycleing
                                                     * .getGarbageUnits()-
                                                     * (ia.getMaxUnits
                                                     * ()-ia.getUnits());
                                                     * ia.setUnits
                                                     * (ia.getUnits()+unit);
                                                     * //ia.setCurrentType(2);
                                                     * recycleing
                                                     * .setGarbageUnits(
                                                     * recycleing
                                                     * .getGarbageUnits()-
                                                     * (ia.getMaxUnits
                                                     * ()-ia.getUnits()) ); }
                                                     * 
                                                     * }
                                                     */
                                                }
                                            } catch (Exception e) {
                                                System.out.println(e.getMessage());
                                            }
                                        }

                                    }
                                }

                                if (is_found_garbage) {
                                    CentralAgent._cmCommunicationModule.send(ACLMessage.INFORM, msg.getSender(), "",
                                            "HARVESTER-COLLECT-GARBAGE", "", ia);
                                }
                            }
                            System.out.println("HMOVE"
                                    + ((InfoGame.getInstance().getInfo().getMap())[new_row][new_col])
                                            .getCellType());
                            InfoGame.getInstance().setNewCell(pos.getRow(), pos.getColumn(),
                                    pos.getVisited());
                            ((InfoGame.getInstance().getInfo().getMap())[new_row][new_col]).setAgent(ia);
                            InfoGame.getInstance().getInfo().getAgentsInitialPosition()
                                    .put(ia, InfoGame.getInstance().getInfo().getCell(new_row, new_col));
                            ((InfoGame.getInstance().getInfo().getMap())[new_row][new_col]).setAgent(ia);
                            InfoGame.getInstance().getInfo().getAgentsInitialPosition()
                                    .put(ia, InfoGame.getInstance().getInfo().getCell(new_row, new_col));
                            (InfoGame.getInstance().getInfo().getMap())[new_row][new_col].setBusy(true);
                            (InfoGame.getInstance().getInfo().getMap())[Integer.parseInt(temp[0])][Integer
                                    .parseInt(temp[1])].setBusy(false);

                        } catch (Exception e) {
                            System.out.println("Central Error" + e.getMessage());
                        }

                        // send(ACLMessage.INFORM, msg.getSender(), "",
                        // "UPDATE-MAP", "", mycurrentGame.getInfo());
                    }
                }
            }
        }
    }
    
    /**
     * Signleton part
     */

    private static CollectGarbage instance = new CollectGarbage();

    private CollectGarbage() {

    }

    public static CollectGarbage getInstance() {
        return instance;
    }
}

