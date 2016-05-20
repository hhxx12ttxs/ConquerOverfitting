import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class Pong {

    private int x;
    private int y;
    private Position pone;
    private Position ptwo;

    public void start() {
        try {
            Display.setDisplayMode(new DisplayMode(800, 600));
            Display.setTitle("Pong");
            Display.create();

        }catch(LWJGLException e) {
            e.printStackTrace();
            System.exit(0);
        }

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0, 800, 0, 600, 1, -1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);

        Ball ball = new Ball(Display.getWidth()/2, Display.getWidth()/2);
        fillRect(ball.getX(),ball.getY(),20, 20);
        pone = new Position(35, Display.getHeight()/2);
        ptwo = new Position(Display.getWidth()-35, Display.getHeight()/2);
        int numseperators = (Display.getHeight()-80)/20;
        System.out.println("numsep: " + numseperators);
        while(!Display.isCloseRequested()) {



            //fillRect(Display.getWidth()/2, Display.getHeight()/2, 200, 300);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            //Display.setInitialBackground(1.0f, 1.0f, 1.0f);
            fillRect(pone.getX(), pone.getY(), 20, 100);
            fillRect(ptwo.getX(), ptwo.getY(), 20, 100);
            fillRect(Display.getWidth()/2, 30, Display.getWidth()-50, 20);
            fillRect(Display.getWidth()/2, Display.getHeight()-30, Display.getWidth()-50, 20);

            for(int i = 0; i <= numseperators/2; i++){
                fillRect(Display.getWidth()/2, Display.getHeight()-40-(40 * i), 20, 20);
            }

            ball.move();
            fillRect(ball.getX(),ball.getY(),20, 20);
            pollingInput();


            Display.update();
        }

        Display.destroy();
    }

    public void createGroupTri(double x, double y, double sidelength) {

        for (int i = 0; i < 5; i++) {
            if (i % 2 == 0) {

                GL11.glColor3f(0f, 1.0f, 0f);
                fillTri(x, y, sidelength);
                GL11.glColor3f(0.0f, .749f, 1.0f);
                fillTri(x, y, -sidelength);
                sidelength /= 2;
                //System.out.println("Done");
            }else{
                GL11.glColor3f(0.0f, .749f, 1.0f);
                fillTri(x, y, sidelength);
                GL11.glColor3f(0f, 1.0f, 0f);
                fillTri(x, y, -sidelength);
                sidelength /= 2;
            }
        }
    }

    public void fillRect(double x, double y, double width, double height){
        x -= width/2;
        y -= height/2;

        GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex2d(x, y);
            GL11.glVertex2d(x + width, y);
            GL11.glVertex2d(x + width, y + height);
            GL11.glVertex2d(x, y + height);
        GL11.glEnd();
    }

    public void fillTri(double x, double y, double side) {
        x -= side/2;
        y -= side/2;

        GL11.glBegin(GL11.GL_POLYGON);
            GL11.glVertex2d(x, y);
            GL11.glVertex2d((x + side), y);
            //GL11.glVertex2d((x + height), (y + width));
            GL11.glVertex2d(x, (y + side));
        GL11.glEnd();
    }

    public void pollingInput(){
        /*
        if(x != Mouse.getX() && y != Mouse.getY()) {
            x = Mouse.getX();
            y = Mouse.getY();
            System.out.println("X: " + x + " Y: " + y);
        }

        if(Mouse.isButtonDown(0)){
            createGroupTri(Mouse.getX(), Mouse.getY(), 300);

        }
        */

        //while(Keyboard.next()) {
            if(Keyboard.isKeyDown(Keyboard.KEY_S)){
                pone.moveDown();


            }else if(Keyboard.isKeyDown(Keyboard.KEY_W)){
                pone.moveUp();
            }if(Keyboard.isKeyDown(Keyboard.KEY_DOWN)){
                ptwo.moveDown();


            }else if(Keyboard.isKeyDown(Keyboard.KEY_UP)){
                ptwo.moveUp();
            }
            /*
            else if(Keyboard.getEventKey() == Keyboard.KEY_DOWN){
                System.out.println("DOWN");
            }
            */
        //}

    }

    class Position{
        private double x;
        private double y;

        Position(double x, double y){
            this.x = x;
            this.y = y;
        }

        public double getX(){

            return x;
        }

        public double getY(){

            return y;
        }

        public void moveDown(){

            y-=.7;
            if(!(y >= 90 && y <= Display.getHeight()-90)){
                y+=.7;
            }
            //Display.getHeight();
        }

        public void moveUp(){

            y+=.7;
            if(!(y >= 90 && y <= Display.getHeight()-90)){
                y-=.7;
            }
            //Display.getHeight();
        }

        public void addX(double add){
            x += add;
        }

        public void addY(double add){
            y += add;
        }

        public void setX(double x){
            this.x = x;
        }

        public void setY(double y){
            this.y = y;
        }
    }

    class Ball{
        Position pos;
        private double hmove;
        private double vmove;
        private double direction;

        Ball(double x, double y){
            pos = new Position(x, y);
            initBall();
        }

        public void initBall(){
            hmove = .25;
            direction = 0;

            while(correctDir(direction)){
                direction = Math.random() * 360;

            }
            System.out.println("Direciton " + direction);
            //System.out.println("awesomeness " + direction);

            vmove = hmove * Math.tan(Math.toRadians(direction));
            if(direction > 90 && direction < 270){
                hmove = -hmove;
            }
            //System.out.println("look here " + vmove);
        }

        public boolean correctDir(double direction){
            if((direction < 5 || direction > 355) || (direction > 40 && direction < 140) || (direction > 185 && direction < 315) || (direction > 220 && direction < 320)){
                return true;
            }
            return false;
        }

        public void move(){
            pos.addX(hmove);
            pos.addY(vmove);
            if(!(pos.getY() >= 50 && pos.getY() <= Display.getHeight()- 50)){

                pos.addY(-vmove);
                bounce();
            }else if(playerCollision()){

                reflect();
            }else if(outOfBounds()){
                pos.setX(Display.getWidth()/2);
                pos.setY(Display.getHeight()/2);
                initBall();
            }
        }

        public boolean outOfBounds(){
            pos.getX();
            pos.getY();
            if((pos.getX() > Display.getWidth() || pos.getX() < 0)){

                return true;
            }

            return false;
        }

        public boolean playerCollision(){
            //System.out.println("IN");
            if(((pos.getX() == pone.getX() + 20) && (pos.getY() >= pone.getY() - 50 && pos.getY() <= pone.getY()+50)) || (pos.getX() == ptwo.getX() - 20) && (pos.getY() >= ptwo.getY()-50 && pos.getY() <= ptwo.getY()+50)){
                //System.out.println("true");
                return true;

            }
            return false;
        }

        public double getX(){
            return pos.getX();
        }

        public double getY(){
            return pos.getY();
        }

        public void bounce(){
            vmove = -vmove;
        }

        public void reflect(){
            vmove = vmove;
            hmove = -hmove;
        }
    }

    public static void main(String[] args){
        Pong bob = new Pong();
        bob.start();
    }
}




