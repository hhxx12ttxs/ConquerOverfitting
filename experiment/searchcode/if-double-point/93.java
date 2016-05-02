/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gamelib.shape;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * @author Angus Thomsen
 *
 * GameShape is an abstract class that represents 2D shapes. These 2D shapes are
 * designed to move towards a projection angle
 *
 * point represent the it's location on a x/y axis.
 *
 * height & width represent the height and width of the object. This doesn't
 * actually define the shape of the shape as a circle can be a shape, including
 * more obscure shapes (such as letters, numbers and symbols). The easist way to
 * think of width and height is, width is the maximum width of the shape on the
 * horizonal axis, and height is the maximum height of the shape on the vertical
 * axis. height & width are always between 3 and 200.
 *
 * velocity is the distance the point is displaced on the protectory.
 *
 * acceleration is how much the velocity is incremented.
 */
public abstract class GameShape {




    //the location of the Shape
    protected Point2D point = new Point2D.Double();
    protected int width;
    protected int height;
    //the distance it travels when it is displaced
    protected double velocity;
    //how much the speed is increimented
    protected double acceleration;
    //the angle in which the shape is facing
    protected double projectory;
    //rotation values NEED METHODS FOR ROTATION
    protected double rotationVelocity;
    protected double rotationAcceleration;
    protected double velocityCap;

    protected Rectangle2D.Double hitBox;

    public GameShape() {
        setSize(0);
        centerPoints(0, 0);

        projectory = 
                velocity = acceleration = rotationVelocity = 
                rotationAcceleration =  velocityCap = 0.0;

        hitBox = new Rectangle2D.Double();
    }




    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                      HELPERS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////




    protected final void checkIntBounds(double input, double min, double max) {
        if (input < min) {
            throw new GameShapeException("must be greater than " + min + "");
        }
        if (input > max) {
            throw new GameShapeException("must be lesser than " + max + "");
        }
    }
    protected final void checkIntBounds(double input, double min) {
        if (input < min) {
            throw new GameShapeException("must be greater than " + min + "");
        }
    }




    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                      GETTERS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////




    //simple getters
    public double getXPoint() {
        return point.getX();
    }
    public double getYPoint() {
        return point.getY();
    }
    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }
    public double getVelocity() {
        return velocity;
    }
    public double getAcceleration() {
        return acceleration;
    }
    public double getProjectory() {
        return projectory;
    }
    public double getRotationVelocity() {
        return rotationVelocity;
    }
    public double getRotationAcceleration() {
        return rotationAcceleration;
    }
    public double getVelocityCap(){
        return velocityCap;
    }

    /**
     * returns a rectangle of the space taken up by the hitbox
     */
    public Rectangle2D.Double getHitBox(){
        return hitBox;
    }


   /**
    * returns a rectangle of the space taken up by the shape
    */
   public Rectangle2D.Double getBounds() {
        //returns a rectangle of the space taken up by the object
        return new Rectangle2D.Double(
                point.getX(), point.getY(), width, height);
    }
    /**
     * returns a rectangle of the shape as if it was displaced
     */
    public Rectangle2D.Double getDisplacedBounds() {
        //returns a rectangle of the space taken up by the object
        return new Rectangle2D.Double(
                point.getX() + velocityCosProjectory(),
                point.getY() + velocitySinProjectory(),
                width, height
        );
    }
    /**
     * returns a rectangle of the space taken up by the hitbox
     */
    public Rectangle2D.Double getDisplacedHitBox(){
        return new Rectangle2D.Double(
                hitBox.x + velocityCosProjectory(),
                hitBox.y + velocitySinProjectory(),
                width, height
        );
    }

    public Point2D getPoint() {
        return point;
    }
    public Point2D getDisplacedPoint() {
        return new Point2D.Double(
                point.getX() + velocityCosProjectory(),
                point.getY() + velocitySinProjectory());
    }






    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                      SETTERS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////




    public void setXPoint(final double xPoint) {
        point.setLocation(xPoint, point.getY());
    }
    public void setYPoint(final double yPoint) {
        point.setLocation(point.getX(), yPoint);
    }
    public void setWidth(final int width) {
        //set this.width to width if between 3 & 200, otherwise throw exception
        try {
            checkIntBounds(width, 0);
            this.width = width;
        } catch (GameShapeException e) {
            throw new GameShapeException("width " + e);
        }
    }
    public void setHeight(final int height) {
        //set this.height to height if between 3 & 200,
        //otherwise throw exception
        try {
            checkIntBounds(height, 0);
            this.height = height;
        } catch (GameShapeException e) {
            throw new GameShapeException("height " + e);
        }
    }
    public void setVelocity(final double velocity) {
        try {
            checkIntBounds(velocity, 0, velocityCap);
            this.velocity = velocity;
        } catch (GameShapeException e){
            throw new GameShapeException("width " + e);
        }
    }
    public void setAcceleration(final double acceleration) {
        this.acceleration = acceleration;
    }
    public void setProjectory(final double projectory) {
        this.projectory = projectory;
    }
    public void setRotationVelocity(double rotationVelocity) {
        this.rotationVelocity = rotationVelocity;
    }
    public void setRotationAcceleration(double rotationAcceleration) {
        this.rotationAcceleration = rotationAcceleration;
    }
    public void setVelocityCap(double velocityCap){
        this.velocityCap = velocityCap;
    }


    public void setHitBox(double xOffset, double yOffset, double width, double height) {
        setHitBox(new Rectangle2D.Double(xOffset, yOffset, width, height));
    }
    public void setHitBox(Rectangle2D.Double other) {
        hitBox.x        = other.x;
        hitBox.y        = other.y;
        hitBox.width    = other.width;
        hitBox.height   = other.height;
    }
    public final void setPoint(double point) {
        //sets position to the positions given
        setXPoint(point);
        setYPoint(point);
    }
    public final void setPoint(double xPoint, double yPoint) {
        //sets position to the positions given
        setXPoint(xPoint);
        setYPoint(yPoint);
    }
    public final void setPoint(Point2D point) {
        //sets position to the positions given
        setXPoint(point.getX());
        setYPoint(point.getY());
    }
    public final void setSize(int size) {
        setSize(size, size);
    }
    public final void setSize(int width, int height) {
        setWidth(width);
        setHeight(height);
    }


    //helper methods
    public final void centerXPoint(double xPoint) {
        //centres shape on xPoint
        setXPoint(xPoint - (width / 2));
    }
    public final void centerYPoint(double yPoint) {
        //centres shape  on yPoint
        setYPoint(yPoint - (height / 2));
    }
    public final void centerPoints(double point) {
        //centres shape on xPoint & yPoint
        centerXPoint(point);
        centerYPoint(point);
    }
    public final void centerPoints(double xPoint, double yPoint) {
        //centres shape on xPoint & yPoint
        centerXPoint(xPoint);
        centerYPoint(yPoint);
    }
    public final void centerPoints(Point2D point) {
        //centres shape on xPoint & yPoint
        centerXPoint(point.getX());
        centerYPoint(point.getY());
    }
    public void centerHitBox() {
        hitBox.x        = (width / 2) - (hitBox.width / 2);
        hitBox.y        = (height / 2) - (hitBox.height / 2);
    }
    public void setHitBoxToShapeParams(){
        hitBox.x = getXPoint();
        hitBox.y = getYPoint();
        hitBox.width = width;
        hitBox.height = height;
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                      TURNING
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////




    //ADD ROTATION METHODS
    public void turn() {
        projectory += rotationVelocity;
    }
    public void turn(boolean direction) {
        projectory += rotationVelocity * (direction ? 1 : -1);
    }
    public void turn(double rotationVelocity) {
        projectory += rotationVelocity;
    }

    public void accelerateTurningVelocity() {
        rotationVelocity += rotationAcceleration;
    }
    public void accelerateTurningVelocity(boolean direction) {
        rotationVelocity += rotationAcceleration * (direction ? 1 : -1);
    }
    public void accelerateTurningVelocity(double rotationAcceleration) {
        rotationVelocity += rotationAcceleration;

    }




    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                              DISPLACEMENT & ACCELERATION
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////




    //displacement methods
    public void displace() {
        displace(velocity);
    }
    public void displace(boolean direction) {
        displace(direction ? velocity : -velocity);
    }
    public void displace(double velocity) {
        displace(velocity, projectory);
    }
    public void displace(double velocity, double projectory) {
        point.setLocation(
                point.getX() + velocitySinProjectory(velocity, projectory),
                point.getY() + velocityCosProjectory(velocity, projectory));
    }

    //mutate speed
    public void accelerate() {
        accelerate(acceleration);
    }
    public void accelerate(boolean direction) {
        accelerate(acceleration * (direction ? 1 : -1));
    }
    public void accelerate(double acceleration) {
        if (velocity + acceleration <= velocityCap){
            velocity += acceleration;
        }
    }




    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                               TRIGONOMY FUNCTIONS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////




    protected final double velocityCosProjectory() {
        return velocityCosProjectory(velocity, projectory);
    }
    protected final double velocityCosProjectory(double velocity,
            double projectory) {
        return velocity * Math.cos(Math.toRadians(projectory));
    }

    protected final double velocitySinProjectory() {
        return velocitySinProjectory(velocity, projectory);
    }
    protected final double velocitySinProjectory(double velocity,
            double projectory) {
        return velocity * Math.sin(Math.toRadians(projectory));
    }




    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                               COLLISION DETECTION
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////




    //checks if shape is overlapping another
    public boolean isColliding(GameShape... others) {
        //returns true if shape is colliding with Shape object
        for (GameShape other : others) {
            if (isColliding(other.getBounds())) {
                return true;
            }
        }
        return false;
    }
    public boolean isColliding(Rectangle2D.Double... others) {
        //returns true if shape is colliding with Rectangle object
        for (Rectangle2D.Double other : others) {
            if (isColliding(other)) {
                return true;
            }
        }
        return false;
    }
    public boolean isColliding(GameShape other) {
        //returns true if shape is colliding with rectangle object
        return isColliding(other.getBounds());
    }
    public boolean isColliding(Rectangle2D.Double other) {
        //returns true if shape is colliding with rectangle object
        return getBounds().intersects(other);
    }

    //checks if shape is going to overlap another
    public boolean willCollide(GameShape ... others) {
        //returns true if shape is colliding with Shape object
        for (GameShape other : others) {
            if (willCollide(other)) {
                return true;
            }
        }
        return false;
    }
    public boolean willCollide(Rectangle2D.Double ... others) {
        //returns true if shape is colliding with Rectangle object
        for (Rectangle2D.Double other : others) {
            if (willCollide(other)) {
                return true;
            }
        }
        return false;
    }
    public boolean willCollide(GameShape other) {
        //returns true if shape is colliding with rectangle object
        return isColliding(other.getDisplacedBounds());
    }
    public boolean willCollide(Rectangle2D.Double other) {
        //returns true if shape is colliding with rectangle object
        return other.getBounds().intersects(this.getDisplacedBounds());
    }




    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                               UTILITY FUNCTIONS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////





    public void copyMovementParameters(GameShape other){
        this.acceleration           = other.acceleration;
        this.rotationAcceleration   = other.rotationAcceleration;
        this.rotationVelocity       = other.rotationVelocity;
        this.velocityCap            = other.velocityCap;
        this.velocity               = other.velocity;
        this.projectory             = other.projectory;
    }




    @Override
    public String toString() {
        //packageName.className(point.x,point.y,width,height)
        return String.format("%s(%d,%d,%d,%d)",
                getClass().getName(), point.getX(), point.getY(), width, height);
    }


} // end of GameShape
