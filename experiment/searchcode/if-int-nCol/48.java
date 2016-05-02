package sma.modules.scoutcoordinatoragent;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import sma.ScoutCoordinatorAgent;
import sma.modules.BaseCommunicationModule;
import sma.ontology.AuxInfo;
import sma.ontology.Cell;
import sma.ontology.InfoAgent;

/**
 * Communication Module of Coordinator Agent. It both receives messages and
 * sends messages.
 * 
 * @author urvteam
 * 
 */
public class CommunicationModule extends BaseCommunicationModule {

    /**
     * 
     */
    private static final long serialVersionUID = -4439963533224683923L;
    HashSet visited = new HashSet();

    public CommunicationModule(ScoutCoordinatorAgent scCoordinatorAgent) {
        super(scCoordinatorAgent);
    }

    @Override
    protected void handleInform(ACLMessage msg) throws InterruptedException {
        /*
         * if(msg.getOntology().equalsIgnoreCase("COORDINATOR-MAP-UPDATE")) {
         * try { ArrayList<Cell> HarvesterStack = (ArrayList<Cell>)
         * msg.getContentObject(); for(Object c : HarvesterStack){ Cell
         * harvester = (Cell) c;
         * System.out.println("Row:"+harvester.getRow()+" Column:"
         * +harvester.getColumn()); }
         * 
         * } catch (UnreadableException e) { // TODO Auto-generated catch block
         * e.printStackTrace(); } }
         */

        try {
            AuxInfo info = (AuxInfo) msg.getContentObject();
            Random generator = new Random(19580427);
            if (info instanceof AuxInfo) {
                int movement[][] = { { 1, 0 }, { 0, 1 }, { -1, 0 }, { 0, -1 } };
                ArrayList<Point> movementsList = new ArrayList<Point>();
                int min = 0;
                // System.out.println("Got Message");
                for (InfoAgent ia : info.getAgentsInitialPosition().keySet()) {
                    Cell pos = (Cell) info.getAgentsInitialPosition().get(ia);
                    if (pos.getAgent().getAgentType() == 0) {
                        // System.out.println("Scout: "+pos.getColumn()+" "+pos.getRow());
                        int current_position = (pos.getRow() * info.getMap().length) + pos.getColumn();
                        if (!visited.contains(current_position))
                            visited.add(current_position);
                        for (int i = 0; i < 4; i++) {
                            int nCol = pos.getColumn() + movement[i][0];
                            int nRow = pos.getRow() + movement[i][1];
                            Point newCell = new Point(nRow, nCol);
                            // int board_number = (nRow*info.getMap().length) +
                            // nCol;
                            if (!visited.contains(newCell) && nRow >= 0 && nCol >= 0 && nCol < info.getMap().length
                                    && nRow < info.getMap().length && info.getCell(nRow, nCol).getCellType() == 2
                                    && info.getCell(nRow, nCol).getBusy() == false) {
                                movementsList.add(newCell);
                                info.getCell(nRow, nCol).setBusy(true);
                            }
                        }
                        if (movementsList.size() == 0) {
                            min = 0;
                            int visited = 0;
                            for (int i = 0; i < 4; i++) {
                                int nCol = pos.getColumn() + movement[i][0];
                                int nRow = pos.getRow() + movement[i][1];
                                Point newCell = new Point(nRow, nCol);
                                // int board_number =
                                // (nRow*info.getMap().length) + nCol;
                                if (nRow >= 0 && nCol >= 0 && nCol < info.getMap().length
                                        && nRow < info.getMap().length && info.getCell(nRow, nCol).getCellType() == 2
                                        && info.getCell(nRow, nCol).getBusy() == false) {
                                    // System.out.println("data: "+board_number+" "+((info.getMap())[nRow][nCol]).getVisited());
                                    visited = ((info.getMap())[nRow][nCol]).getVisited();
                                    if (min == 0 || min == visited) {
                                        min = visited;
                                        movementsList.add(newCell);
                                        info.getCell(nRow, nCol).setBusy(true);
                                    } else if (visited < min) {
                                        movementsList.clear();
                                        movementsList.add(newCell);
                                        info.getCell(nRow, nCol).setBusy(true);
                                    }
                                }
                            }
                            if (movementsList.size() == 0) {
                            	Point newCell = new Point(pos.getRow(), pos.getColumn());
                            	movementsList.add(newCell);
                            	info.getCell(pos.getColumn(), pos.getRow()).setBusy(true);
                            }
                        }
                        
                        int randomIndex = generator.nextInt(movementsList.size());
                        Point new_move = movementsList.get(randomIndex);
                        visited.add(new_move);

                        int move_row = new_move.x;// new_move%info.getMap().length;
                        int move_col = new_move.y;// new_move/info.getMap().length;
                        ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
                        reply.setContent(pos.getColumn() + "-" + pos.getRow() + "-" + Integer.toString(move_col) + "-"
                                + Integer.toString(move_row));
                        reply.setOntology("MOVE-SCOUT");
                        reply.addReceiver(msg.getSender());
                        myAgent.send(reply);
                        // System.out.println("Visited :"+visited.toString() );
                        movementsList.clear();

                    }
                }
            }
        } catch (UnreadableException e) {
            // TODO Auto-generated catch block
            System.out.println("Scout error");
            e.printStackTrace();
        }

    }

    @Override
    protected void handleRequest(ACLMessage msg) {
        // TODO Auto-generated method stub

    }
}

