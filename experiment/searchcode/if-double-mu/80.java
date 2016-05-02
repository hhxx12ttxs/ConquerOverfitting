package pingball;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import physics.Geometry;

public class Board {

    /**
     * Rep invariant: boardRep is the 2D char representation of the board
     */

    private double gravity;
    private double mu;
    private double mu2;
    private String name;
    private List<Gadget> gadgets = new ArrayList<Gadget>();
    private List<Ball> balls = new ArrayList<Ball>();
    private char[][] boardRep = new char[22][22];
    private Wall[] borders = new Wall[4];
    public Map<String, Gadget> gadgetNames = new HashMap<String, Gadget>();
    

    /**
     * Constructor for a default empty Board with one ball
     */
    public Board(String name, double gravity, double mu, double mu2) {
        this.name = name;
        this.gravity = gravity;
        this.mu = mu;
        this.mu2 = mu2;
        this.boardConstructor();
    }

    /**
     * Constructor for a default empty Board with one ball
     */
    public Board(double gravity, double mu, double mu2) {
        this.name = null;
        this.gravity = gravity;
        this.mu = mu;
        this.mu2 = mu2;
        this.boardConstructor();
    }

    /**
     * Initializes appropriate boarders and walls for board object
     */
    private void boardConstructor() {
        // top wall
        borders[0] = new Wall(0);

        // right wall
        borders[1] = new Wall(1);

        // bottom wall
        borders[2] = new Wall(2);

        // left wall
        borders[3] = new Wall(3);

        for (int i = 0; i < 22; i++) {
            boardRep[0][i] = '.';
            boardRep[i][0] = '.';
            boardRep[21][i] = '.';
            boardRep[i][21] = '.';
            if (i > 0 && i < 21) {
                for (int j = 1; j < 21; j++) {
                    boardRep[i][j] = ' ';
                }
            }
        }
    }

    /**
     * Accessor method
     * 
     * @return the boardRep of this Board
     */
    public char[][] getBoardRep() {
        return boardRep;
    }

    public void triggerActions(String[] triggers) {
        String trigger = triggers[0];
        String action = triggers[1];
    }

    /**
     * Accessor method
     * 
     * @return the name of this Board
     */
    public String getName() {
        return name;
    }

    /**
     * Mutator method
     * 
     * @param boardRep
     *            the 2D char array boardRep to assign to this Board
     */
    public void setBoardRep(char[][] boardRep) {
        this.boardRep = boardRep;
    }

    /**
     * Add a gadget to the board at the position of the Gadget object
     * 
     * @param gadget
     *            Gadget to add to the board
     */
    public void addGadget(Gadget gadget) {
        synchronized (gadgets) {
            synchronized (gadgetNames) {
                gadgets.add(gadget);
                gadget.putInBoardRep(this, false);
                String name = gadget.getName();
                if (name != null) {
                    gadgetNames.put(name, gadget);
                }
            }
        }

    }

    /**
     * Add a Ball to the board with position and velocity specified in the Ball
     * object
     * 
     * @param ball
     *            Ball to add
     */
    public void addBall(Ball ball) {
        balls.add(ball);
        int x = (int) Math.round(ball.getPosition().x());
        int y = (int) Math.round(ball.getPosition().y());
        boardRep[y + 1][x + 1] = '*';
    }

    @Override
    public String toString() {
        String string = "";
        for (char[] line : boardRep) {
            string += String.valueOf(line);
            string += "\n";
        }
        return string;
    }

    public void changeWall(int wallNumber, String name) {
        borders[wallNumber].open = !borders[wallNumber].open;
        for (int i = 1; i <= name.length(); i++){
            if (wallNumber == 0){
                boardRep[0][i] = name.charAt(i-1);
            }
            
            else if (wallNumber == 1){
                boardRep[i][21] = name.charAt(i-1);
            }
            
            else if (wallNumber == 2){
                boardRep[22][i] = name.charAt(i-1);
            }
            
            else if (wallNumber == 3){
                boardRep[i][0] = name.charAt(i-1);
            }
        }
    }

    /**
     * Refresh the board, taking into account elapsed time causing motion, as
     * well as possible collisions
     */
    public Map<Ball, Integer> update(double deltaT) {

        Map<Ball, Integer> passed = new HashMap<Ball, Integer>();

        for (int i = 0; i < balls.size(); i++) {
            double time = 0;
            double timeHit = 0;
            double minTime;

            while (time < deltaT) {
                minTime = Double.POSITIVE_INFINITY;
                Gadget firstGadget = null;

                for (Gadget gadget : gadgets) {
                    timeHit = gadget.timeUntilCollision(balls.get(i));
                    if (timeHit < deltaT - time && timeHit < minTime) {
                        minTime = timeHit;
                        firstGadget = gadget;
                    }
                }

                Ball firstBall = null;

                for (int j = i + 1; j < balls.size(); j++) {
                    timeHit = Geometry.timeUntilBallBallCollision(balls.get(i)
                            .getCircle(), balls.get(i).getVelocity(), balls
                            .get(j).getCircle(), balls.get(j).getVelocity());
                    if (timeHit < deltaT - time && timeHit < minTime) {
                        minTime = timeHit;
                        firstBall = balls.get(j);
                    }
                }

                Wall firstWall = null;
                int hitWall = -1;

                for (Wall wall : borders) {
                    timeHit = wall.timeUntilCollision(balls.get(i));
                    if (timeHit < deltaT - time && timeHit < minTime) {
                        minTime = timeHit;
                        firstWall = wall;
                    }
                }

                if (firstBall == null) {
                    if (firstWall == null) {
                        if (firstGadget != null) {
                            balls.get(i).move(gravity, mu, mu2, minTime, this);
                            firstGadget.hit(balls.get(i), this);
                            time += minTime;
                        } else {
                            balls.get(i).move(gravity, mu, mu2, deltaT, this);
                            time = deltaT;
                        }
                    } else {
                        balls.get(i).move(gravity, mu, mu2, minTime, this);
                        hitWall = firstWall.hit(balls.get(i), this);
                        time += minTime;
                    }
                }

                else if (firstWall == null) {

                    balls.get(i).move(gravity, mu, mu2, minTime, this);

                    firstBall.move(gravity, mu, mu2, minTime, this);
                    Geometry.VectPair velocities = Geometry.reflectBalls(balls
                            .get(i).getPosition(), 1, balls.get(i)
                            .getVelocity(), firstBall.getPosition(), 1,
                            firstBall.getVelocity());
                    balls.get(i).setVelocity(velocities.v1);
                    firstBall.setVelocity(velocities.v2);
                    time += minTime;
                }

                else {
                    balls.get(i).move(gravity, mu, mu2, minTime, this);
                    hitWall = firstWall.hit(balls.get(i), this);
                    time += minTime;
                }

                if (hitWall >= 0) {
                    time = deltaT;
                    Ball transferBall = balls.remove(i);
                    transferBall.putInBoardRep(this, true);
                    passed.put(transferBall, hitWall);
                }

            }
        }
        return passed;

    }

}

