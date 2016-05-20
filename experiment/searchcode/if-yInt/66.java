/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package happywallrobot;

import basicrobotcontrol.MainControl;
import com.infomatiq.jsi.Point;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JFrame;

/**
 *
 * @author Logan Murphy
 */
public abstract class BooleanWallMapper extends JFrame implements WallMapper {

    private boolean[][] walls;
    public static final int SCALE = 50;
    public static final int PADDING = 50;
    public static final int WIDTH = 9000 / SCALE, HEIGHT = 9000 / SCALE;
    private Point p;
    private MainControl mc;

    public BooleanWallMapper() {
        walls = new boolean[HEIGHT][WIDTH];
        this.setSize(WIDTH + PADDING * 2, HEIGHT + PADDING * 2);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        p = new Point(WIDTH / 2, HEIGHT / 2);
    }

    @Override
    public void paint(Graphics g) {
        int count = 0;
        g.setColor(Color.black);
        for (int i = 0; i < this.walls.length; i++) {
            for (int j = 0; j < this.walls[i].length; j++) {
                if (this.walls[i][j]) {
                    g.drawRect(i + PADDING, j + PADDING, 1, 1);
                    count++;
                }
            }
        }
        System.out.println(count);
        g.setColor(Color.red);
        g.drawRect(p.xInt() + PADDING, p.yInt() + PADDING, 5, 5);
        this.repaint();
    }

    public void mapWalls() {
        if (!isSpinning(mc)) {
            double[] distance = mc.laser.readPolar(true);
            double[] pose = mc.robot.getPhysicalPose(); //x, y, theta
            pose[1] = -pose[1];
            for (int i = 0; i < distance.length; i++) {
                double theta = (-i / 2.0 + 135.0 - pose[2]) * (Math.PI / 180);
                int x = (int) ((pose[0] + (distance[i] * Math.cos(theta))) / SCALE + WIDTH / 2),
                        y = (int) ((pose[1] + (distance[i] * Math.sin(theta))) / SCALE + HEIGHT / 2);
                walls[y][x] = true;
            }
            p.x = (float) pose[0] / SCALE + WIDTH / 2;
            p.y = (float) pose[1] / SCALE + HEIGHT / 2;
        }
    }

    private static boolean isSpinning(MainControl mc) {
        double[] lr = mc.robot.getVelocityLR();
        double[] linAng = mc.robot.velocityMotorLRToVelocityLinAngRad(lr[0], lr[1]);
        boolean spinning = linAng[1] != 0;
        return (spinning);
    }

    @Override
    public void setMainControl(MainControl mc) {
        this.mc = mc;
    }
}

