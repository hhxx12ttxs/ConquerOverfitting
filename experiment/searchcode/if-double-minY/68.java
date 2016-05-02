package happywallrobot;

import basicrobotcontrol.MainControl;
import happywallrobot.CleanTileTracker.TileState;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.PriorityQueue;
import javax.swing.JFrame;

public class PointWallMapper extends JFrame implements WallMapper, KeyListener, MouseListener {

    private int minX, maxX, minY, maxY;
    private ArrayList<Point> walls;
    private ArrayList<Point> robotHistory;
    private static final int SCALE = 10;
    private MainControl mc;
    private TileState[][] lastTileState;
    private Point[] lastPath;
    private static final int PADDING = 50;

    public PointWallMapper() {
        minY = minX = Integer.MAX_VALUE;
        maxY = maxX = Integer.MIN_VALUE;
        walls = new ArrayList<>();
        robotHistory = new ArrayList<>();
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //addKeyListener(this);
        //addMouseListener(this);
    }

    public static void rescale(Point p) {
        p.setLocation(p.x / SCALE, p.y / SCALE);
    }

    public static void descale(Point p) {
        p.setLocation(p.x * SCALE, p.y * SCALE);
    }

    @Override
    public void paint(Graphics g) {
        int padding = PADDING;
        int width = Math.max(maxX - minX, 0) + padding * 2, height = Math.max(maxY - minY, 0) + padding * 2;
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D big = bi.createGraphics();
        setSize(width, height);
        if (lastTileState != null) {
            int rows = lastTileState.length, columns = lastTileState[0].length;
            Rectangle r = getRoomDimension();
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    int x = r.width / columns * j + padding,
                            y = r.height / rows * i + padding,
                            w = r.width / columns,
                            h = r.height / rows;
                    if (lastTileState[i][j] == TileState.Block) {
                        big.setColor(Color.red);
                    } else if (lastTileState[i][j] == TileState.Clean) {
                        big.setColor(Color.green);
                    } else if (lastTileState[i][j] == TileState.Dirty) {
                        big.setColor(new Color(139, 69, 19));
                    }
                    big.fillRect(x, y, w, h);
                    big.setColor(Color.blue);
                    big.drawRect(x, y, w, h);
                }
            }
        }
        big.setColor(Color.white);
        for (int i = 0; i < walls.size(); i++) {
            Point p = walls.get(i);
            big.fillRect((p.x - minX) + padding, (p.y - minY) + padding, 1, 1);
        }
        big.setColor(Color.cyan);
        for (int i = 0; i < robotHistory.size(); i++) {
            Point p = robotHistory.get(i);
            big.fillRect((p.x - minX) + padding, (p.y - minY) + padding, 1, 1);
        }
        if (mc != null) {
            double[] pose = mc.robot.getPose();
            big.setColor(Color.white);
            big.fillRect((int) (-minX + pose[0] / SCALE + padding), (int) (-minY + -pose[1] / SCALE + padding), 1, 1);
            big.setTransform(new AffineTransform());
        }
        if (lastPath != null) {
            big.setColor(Color.yellow);
            for (int i = 0; i < lastPath.length - 1; i++) {
                big.drawLine(lastPath[i].x + PADDING - minX, lastPath[i].y + PADDING - minY, lastPath[i + 1].x + PADDING - minX, lastPath[i + 1].y + PADDING - minY);
            }
        }
        g.drawImage(bi, 0, 0, null);
        repaint();
    }

    @Override
    public void setMainControl(MainControl mc) {
        this.mc = mc;
    }

    @Override
    public void mapWalls() {
        double[] pose = mc.robot.getPhysicalPose(); //x, y, theta
        pose[1] = -pose[1];
        insert(robotHistory, new Point((int) (pose[0] / SCALE), (int) (pose[1] / SCALE)));
        if (mc != null && !isSpinning(mc)) {
            double[] distance = mc.laser.readPolar(true);
            for (int i = 0; i < distance.length; i++) {
                double theta = (-i / 2.0 + 135.0 - pose[2]) * (Math.PI / 180);
                int x = (int) ((pose[0] + (distance[i] * Math.cos(theta))) / SCALE),
                        y = (int) ((pose[1] + (distance[i] * Math.sin(theta))) / SCALE);
                Point p = new Point(x, y);
                if (insert(walls, p)) {
                    if (x < minX) {
                        minX = x;
                    }
                    if (x > maxX) {
                        maxX = x;
                    }
                    if (y < minY) {
                        minY = y;
                    }
                    if (y > maxY) {
                        maxY = y;
                    }
                }
            }
        }
    }

    private static boolean isSpinning(MainControl mc) {
        double[] lr = mc.robot.getVelocityLR();
        double[] linAng = mc.robot.velocityMotorLRToVelocityLinAngRad(lr[0], lr[1]);
        boolean spinning = linAng[1] != 0;
        return (spinning);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        getDefaulTileState();
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public MainControl getMainControl() {
        return mc;
    }

    @Override
    public Iterator<Point> getPath(Point p) {
        PriorityQueue<PointWallMapper.Node> pq = new PriorityQueue<>();
        double[] pose = mc.robot.getPose();
        Rectangle r = getRoomDimension();
        int columns = lastTileState[0].length;
        int rows = lastTileState.length;
        boolean[][] marked = new boolean[rows][columns];
        int ec = (p.x - r.x) / (r.width / columns); //end col
        int er = (p.y - r.y) / (r.height / rows); //end row
        int ew = 0; //end weight
        int sc = Math.max(Math.min((int) ((pose[0] / SCALE - r.x) / (r.width / columns)), columns), 0); //start col
        int sr = Math.max(Math.min((int) ((-pose[1] / SCALE - r.y) / (r.height / rows)), rows), 0); //start row
        int sw = Math.abs(ec - sc) + Math.abs(er - sr); //start weight
        Point[] path = null;
        if (sc >= 0 && sc < columns && sr >= 0 && sr < rows) {
            PointWallMapper.Node start = new PointWallMapper.Node(null, sc, sr, sw);
            PointWallMapper.Node goal = new PointWallMapper.Node(null, ec, er, ew);
            pq.add(start);
            while (!pq.isEmpty()) {
                PointWallMapper.Node current = pq.poll();
                marked[current.row][current.column] = true;
                if (current.equals(goal)) {
                    path = new Point[current.length];
                    for (int i = path.length - 1; i >= 0; i--) {
                        path[i] = new Point(current.column * (r.width / columns) + minX + (r.width / columns) / 2, current.row * (r.height / rows) + minY + (r.height / rows) / 2);
                        current = current.parent;
                    }
                    break;
                } else {
                    if (lastTileState[current.row][current.column] != TileState.Block) {
                        addNode(pq, goal, current, current.column + 1, current.row, marked);
                        addNode(pq, goal, current, current.column, current.row + 1, marked);
                        addNode(pq, goal, current, current.column - 1, current.row, marked);
                        addNode(pq, goal, current, current.column, current.row - 1, marked);
                    }
                }
            }
        }
        if (path != null && path.length > 1) {
            path = optimizePath(path);
        }
        return new PointWallMapper.PointIterator(lastPath = path);
    }

    @Override
    public Iterator<Point> getBestPath() {
        ArrayList<ScoredPath> allPaths = new ArrayList<>();
        for (int x = 0; x < lastTileState.length; x++) {
            for (int y = 0; y < lastTileState[x].length; y++) {
                if (lastTileState[x][y] == TileState.Dirty) {
                    Rectangle r = getRoomDimension();
                    int cols = lastTileState.length;
                    int rows = lastTileState[0].length;
                    Point p = new Point(x * (r.width / cols) + minX + (r.width / cols) / 2, y * (r.height / rows) + minY + (r.height / rows) / 2);
                    allPaths.add(getScoredPath(p));
                }
            }
        }
        Collections.sort(allPaths);
        if (allPaths.size() > 0 && allPaths.get(allPaths.size() - 1).myScore > 0) {
            Point[] bestPath = allPaths.get(allPaths.size() - 1).myPath;
            lastPath = bestPath;
            Point[] optimizedPath = optimizePath(bestPath);
            return new PointIterator(optimizedPath);
        } else {
            return new PointIterator();
        }
    }

    private ScoredPath getScoredPath(Point p) {
        PriorityQueue<PointWallMapper.Node> pq = new PriorityQueue<>();
        double[] pose = mc.robot.getPose();
        Rectangle r = getRoomDimension();
        int columns = lastTileState.length;
        int rows = lastTileState[0].length;
        boolean[][] marked = new boolean[rows][columns];
        int ec = (p.x - r.x) / (r.width / columns); //end col
        int er = (p.y - r.y) / (r.height / rows); //end row
        int ew = 0; //end weight
        int sc = Math.max(Math.min((int) ((pose[0] / SCALE - r.x) / (r.width / columns)), columns), 0); //start col
        int sr = Math.max(Math.min((int) ((-pose[1] / SCALE - r.y) / (r.height / rows)), rows), 0); //start row
        int sw = Math.abs(ec - sc) + Math.abs(er - sr); //start weight
        double score = 0.0;
        Point[] path = null;
        if (sc >= 0 && sc < columns && sr >= 0 && sr < rows) {
            PointWallMapper.Node start = new PointWallMapper.Node(null, sc, sr, sw);
            PointWallMapper.Node goal = new PointWallMapper.Node(null, ec, er, ew);
            pq.add(start);
            while (!pq.isEmpty()) {
                PointWallMapper.Node current = pq.poll();
                marked[current.row][current.column] = true;
                if (current.equals(goal)) {
                    path = new Point[current.length];
                    for (int i = path.length - 1; i >= 0; i--) {
                        path[i] = new Point(current.column * (r.width / columns) + minX + (r.width / columns) / 2, current.row * (r.height / rows) + minY + (r.height / rows) / 2);
                        if ((current.column != sc) || (current.row != sr)) {
                            if (lastTileState[current.column][current.row] == TileState.Dirty) {
                                score += 20 / distToPoint(sc + 1, sr + 1, current.column + 1, current.row + 1);
                            } else if (lastTileState[current.column][current.row] == TileState.Clean) {
                                score -= 0.75; //small penalty for driving across clean tiles
                                //score = Math.max(score, 0);
                            } else {
                                //hopefully it won't try to path find through blocked tiles,
                                // but if it does the score needs to reflect that the path is impossible
                                score = Double.NEGATIVE_INFINITY;
                            }
                        }
                        current = current.parent;
                    }
                    break;
                } else {
                    if (current.column + 1 < lastTileState[0].length && lastTileState[current.row][current.column + 1] != TileState.Block) {
                        addNode(pq, goal, current, current.column + 1, current.row, marked);
                    }
                    if (current.row + 1 < lastTileState.length && lastTileState[current.row + 1][current.column] != TileState.Block) {
                        addNode(pq, goal, current, current.column, current.row + 1, marked);
                    }
                    if (current.column - 1 >= 0 && lastTileState[current.row][current.column - 1] != TileState.Block) {
                        addNode(pq, goal, current, current.column - 1, current.row, marked);
                    }
                    if (current.row - 1 >= 0 && lastTileState[current.row - 1][current.column] != TileState.Block) {
                        addNode(pq, goal, current, current.column, current.row - 1, marked);
                    }
                }
            }
        }
        //System.out.println("Path from "+sc+","+sr+" to "+ec+","+er+" has score "+score);
        return new ScoredPath(path, score);
    }

    private class ScoredPath implements Comparable<ScoredPath> {

        private Point[] myPath;
        private double myScore;

        ScoredPath(Point[] path, double score) {
            this.myPath = path;
            this.myScore = score;
        }

        @Override
        public int compareTo(ScoredPath o) {
            if (this.myScore < o.myScore) {
                return -1;
            } else if (this.myScore > o.myScore) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    private Point[] optimizePath(Point[] original) {
        ArrayList<Point> newPath;
        newPath = new ArrayList<>();
        newPath.add(original[0]);
        boolean sameX;
        if (original[1].x == original[0].x) {
            sameX = true;
        } else {
            sameX = false;
        }
        for (int i = 1; i < original.length; i++) {
            if (sameX) {
                if (original[i].x != newPath.get(newPath.size() - 1).x) {
                    sameX = false;
                    newPath.add(original[i - 1]);
                }
            } else {
                if (original[i].y != newPath.get(newPath.size() - 1).y) {
                    sameX = true;
                    newPath.add(original[i - 1]);
                }
            }
        }
        newPath.add(original[original.length - 1]);
        return newPath.toArray(new Point[newPath.size()]);
    }

    private double distToPoint(int x1, int y1, int x2, int y2) {
        double distance;
        double[] diff = new double[2];
        diff[0] = x1 - x2;
        diff[1] = y1 - y2;
        distance = Math.sqrt(Math.pow(diff[0], 2) + Math.pow(diff[1], 2));
        return distance;
    }

    private Point[] streamlinePath(Point[] path) {
        return null;//TODO implement
    }

    private void addNode(PriorityQueue q, PointWallMapper.Node goal, PointWallMapper.Node parent, int column, int row, boolean[][] marked) {
        int columns = lastTileState[0].length;
        int rows = lastTileState.length;
        if (row >= 0 && column >= 0 && row < rows && column < columns && !marked[row][column]) {
            q.add(new PointWallMapper.Node(parent, column, row, Math.abs(goal.column - column) + Math.abs(goal.row - row) + (lastTileState[row][column] == TileState.Clean ? 2 : -2)));
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();
        p.setLocation(p.x - PADDING + minX, p.y - PADDING + minY);
        getPath(p);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    private class Node implements Comparable<PointWallMapper.Node> {

        private int row, column, weight, length;
        private PointWallMapper.Node parent;

        public Node(PointWallMapper.Node p, int c, int r, int w) {
            row = r;
            column = c;
            weight = w;
            parent = p;
            length = 1;
            if (p != null) {
                length += p.length;
            }
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof PointWallMapper.Node && row == ((PointWallMapper.Node) o).row && column == ((PointWallMapper.Node) o).column;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + this.row;
            hash = 97 * hash + this.column;
            hash = 97 * hash + this.weight;
            hash = 97 * hash + Objects.hashCode(this.parent);
            return hash;
        }

        @Override
        public int compareTo(PointWallMapper.Node o) {
            return (weight + length + getTurns()) - (o.weight + o.length + o.getTurns());
        }

        private int getTurns() {
            int rows = 0, columns = 0, turns = 0;
            Node current = this;
            while (current != null && current.parent != null) {
                Node next = current.parent;
                rows += Math.abs(current.row - next.row);
                columns += Math.abs(current.column - next.column);
                if (rows != 0 && columns != 0) {
                    rows = Math.abs(current.row - next.row);
                    columns = Math.abs(current.column - next.column);
                    turns++;
                }
                current = next;
            }
            return turns;
        }
    }

    @Override
    public TileState[][] getDefaulTileState() {
        double size = 0;
        double[] dim = mc.robot.getDimensions();
        for (int i = 0; i < dim.length; i++) {
            if (dim[i] > size) {
                size = dim[i];
            }
        }
        size = size / SCALE;
        int columns = 1, rows = 1;
        Rectangle r = getRoomDimension();
        while (r.width / (columns + 1) > size) {
            columns++;
        }
        while (r.height / (rows + 1) > size) {
            rows++;
        }
        TileState[][] ts = new TileState[rows][columns];
        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                int x = r.width / columns * j + minX;
                int y = r.height / rows * i + minY;
                int w = r.width / columns;
                int h = r.height / rows;
                Rectangle tile = new Rectangle(x, y, w, h);
                if (isContained(walls, tile)) {
                    ts[i][j] = TileState.Block;
                } else if (isContained(robotHistory, tile)) {
                    ts[i][j] = TileState.Clean;
                } else {
                    ts[i][j] = TileState.Dirty;
                }
            }
        }
        return lastTileState = ts;
    }

    @Override
    public Rectangle getRoomDimension() {
        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    private static boolean isContained(ArrayList<Point> points, Rectangle tile) {
        int i = 0;
        while (i < points.size() && points.get(i).y <= tile.y) {
            i++;
        }
        while (i < points.size() && points.get(i).y < tile.y + tile.height) {
            if (tile.x < points.get(i).x && points.get(i).x < tile.x + tile.width) {
                return true;
            }
            i++;
        }
        return false;
    }

    public static boolean insert(ArrayList<Point> points, Point p) {
        int left = 0, right = points.size();
        while (left < right) {
            int index = (left + right) / 2;
            int value = compareTo(p, points.get(index));
            if (value < 0) {
                right = index;
            } else if (value > 0) {
                left = index + 1;
            } else {
                return false;
            }
        }
        if (left == points.size()) {
            points.add(p);
        } else {
            points.add(left, p);
        }
        return true;
    }

    public static int compareTo(Point a, Point b) {
        return a.y != b.y ? a.y - b.y : a.x - b.x;
    }

    private class PointIterator implements Iterator<Point> {

        private Point[] points;
        private int index, length;

        public PointIterator() {
        }

        public PointIterator(Point[] p) {
            if (p != null) {
                points = new Point[length = p.length];
                for (int i = 0; i < points.length; i++) {
                    points[i] = new Point(p[i].x * SCALE, -p[i].y * SCALE);
                }
            }
        }

        @Override
        public boolean hasNext() {
            return points != null && index < length;
        }

        public String toString() {
            return String.format("%d / %d", index, length);
        }

        @Override
        public Point next() {
            int i = index;
            //double[] pose = mc.robot.getPose();
            Point p = points[i];
            //double x = p.x - pose[0], y = p.y - pose[1];
            //if (Math.sqrt(x * x + y * y) <= 300) {
            index++;
            //}
            return p;
        }

        @Override
        public void remove() {
            throw new Error("Cannot remove from iterator.");
        }
    }
}

