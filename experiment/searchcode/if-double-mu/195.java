package pingball;

import physics.Circle;
import physics.Vect;

public class Ball {
    
    /**
     * Rep invariant: position == circle.getCenter(), -.25 <= position.x/y <= 20.25
     *      circle.getRadius() == 0.25
     */

    private Vect position;
    private Vect velocity;
    private Circle circle;
    private String name;
    
    @Override
    public String toString(){
        return "*";
    }
    
    /**
     * Constructor for Ball
     * @param x the initial x coordinate of the ball
     * @param y the initial y coordinate of the ball
     * @param xVel the initial x velocity of the ball
     * @param yVel the initial y velocity of the ball
     * @param board the board that the ball is on
     */
    public Ball(String name, double x, double y, double xVel, double yVel){
        this.name = name;
        position = new Vect(x,y);
        velocity = new Vect(xVel, yVel);
        circle = new Circle(position, 0.25);
    }
    
    public Ball(double x, double y, double xVel, double yVel){
        position = new Vect(x,y);
        velocity = new Vect(xVel, yVel);
        circle = new Circle(position, 0.25);
    }
    
    public String getName(){
        return this.name;
    }
    
    
    
    /**
     * Check rep invariant.
     */
    public void checkRep(){
        assert (position.x() >= -0.25 && position.x() <= 20.25);
        assert (position.y() >= -0.25 && position.y() <= 20.25);
        assert (position.x() == circle.getCenter().x() && position.y() == circle.getCenter().y());
        assert (circle.getRadius() == 0.25);
    }
    
    /**
     * Accessor method
     * @return a Vect representing the Ball's current velocity
     */
    public Vect getVelocity(){
        return new Vect(velocity.x(), velocity.y());
    }
    
    /**
     * Accessor method
     * @return the Ball's Circle representation
     */
    public Circle getCircle(){
        return new Circle(circle.getCenter(), circle.getRadius());
    }
    
    /**
     * Accessor method
     * @return a Vect representing the Ball's current position
     */
    public Vect getPosition(){
        return new Vect(position.x(), position.y());
    }
    
    /**
     * Mutator method to change Ball's velocity
     * @param velocity a Vect representing the velocity to set
     */
    public void setVelocity(Vect velocity){
        this.velocity = velocity;
    }
    
    /**
     * Mutator method to change Ball's position
     * @param position the Vect position to set
     */
    public void setPosition(Vect position){
        this.position = position;
    }
    
    /**
     * Alter the 2D char array boardRep in board to reflect this Ball at its current position
     * @param board the Board that this Ball is on
     * @param remove true if we are removing a previous representation of this ball in the board,
     *        false if we are adding the current representation
     */
    public void putInBoardRep(Board board, boolean remove){

        char[][] boardRep = board.getBoardRep();
        if (!remove){
            if (boardRep[(int) Math.round(position.y()+1)][(int) Math.round(position.x()+1)] == ' ')
            boardRep[(int) Math.round(position.y()+1)][(int) Math.round(position.x()+1)] = '*';
        }
        else{
            if (boardRep[(int) Math.round(position.y()+1)][(int) Math.round(position.x()+1)] == '*')
            boardRep[(int) Math.round(position.y()+1)][(int) Math.round(position.x()+1)] = ' ';
        }
        board.setBoardRep(boardRep);
    }
    
    /**
     * Move the ball on the board according to the ball's velocity and external forces
     * @param gravity the gravity of the board
     * @param mu the first friction coefficient of the board
     * @param mu2 the second friction coefficient of the board
     * @param deltaT the time passed
     */
    public void move(double gravity, double mu, double mu2, double deltaT, Board board){
        putInBoardRep(board, true);
        position = position.plus(velocity.times(deltaT));
        velocity = velocity.times(1+gravity*deltaT-mu*deltaT-mu2*velocity.length()*deltaT);
        circle = new Circle(position, 0.25);
        putInBoardRep(board, false);
    }
}

