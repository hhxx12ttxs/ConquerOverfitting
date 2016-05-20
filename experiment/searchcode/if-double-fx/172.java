package tracketeer.search.arbitree;

import tracketeer.search.Search;
import tracketeer.search.geom2d.Geom2D;
import tracketeer.search.geom2d.Rect;
import tracketeer.search.geom2d.Vector;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * A rapidly randomly exploring tree (RRT).
 *
 * Naive implementation whose nearest neighbour search and therefore extension has a complexity of O(n).
 * The ParallelArbiTree yields a somewhat better performance
 * depending on the number of cores. A K-D tree would be way faster, though.
 *
 * @TODO Use K-D tree for search if too much time in the end.
 * @TODO ArbiTree without global map. Instead maybe follow path and only check with each step. Re-plan if necessary.
 */
public class ArbiTree implements Search {

    private Node root;

    private int stepSize = 7;
    private Random random = new Random();

    private Rectangle sampleBounds;
    private Set<Shape> obstacles;
    private Shape target;

    private Listener arbiTreeListener = new Listener() {
        public void nodeInserted(Node node) { }
        public void extendTowards(Point point) { }
    };

    private boolean goalBias;
    private int biasCount;
    private Rectangle biasedSampleBounds;

    private Extension connectingExtension;

    public ArbiTree(Point start, Rectangle sampleBounds, Set<Shape> obstacles, Shape target) {
        this.root = createRoot(start);
        this.sampleBounds = sampleBounds;
        this.obstacles = obstacles;
        this.target = target;

        if (sampleBounds != null) {
            this.biasedSampleBounds = new Rectangle(sampleBounds);
        }
    }

    public ArbiTree(Point start, Set<Shape> obstacles, Shape target) {
        this(start, null, obstacles, target);

        Point targetCenter = new Point(
                (int) Math.round(target.getBounds().getCenterX()),
                (int) Math.round(target.getBounds().getCenterY()));
        int distance = (int) Math.round(Math.sqrt(
                Math.pow(targetCenter.x - start.x, 2) +
                        Math.pow(targetCenter.y - start.y, 2)));

        this.sampleBounds = new Rectangle(start.x - distance, start.y - distance, distance * 2, distance * 2);
        this.biasedSampleBounds = new Rectangle(sampleBounds);
    }

    public ArbiTree(Point start, Shape target) {
        this(start, new HashSet<Shape>(), target);
    }

    protected Node createRoot(Point start) {
        return new Node(null, start);
    }

    Point randomPoint() {
        Rectangle sampleBounds = isGoalBias() ? getBiasedSampleBounds() : getSampleBounds();

        Point point = new Point(
                sampleBounds.x + random.nextInt(sampleBounds.width),
                sampleBounds.y + random.nextInt(sampleBounds.height));

        return point;
    }

    protected void narrowBias() {
        if (isGoalBias() && biasCount++ % 2 == 0) {
            int width = getBiasedSampleBounds().width;
            int height = getBiasedSampleBounds().height;

            Rect rect = new Rect(Geom2D.center(
                    getTarget()), 0, Math.max(100, width - 25), Math.max(100, height - 25));

            System.out.printf("Narrowing bias from %s to %s%n", biasedSampleBounds, rect);

            biasedSampleBounds = rect.getBounds();
        }
    }

    public Extension extend() {
        narrowBias();
        return extend(randomPoint());
    }

    public Extension extend(Point point) {
        Node node = root.getNearestNeighbour(point);
        double fx = point.getX() - node.getPoint().getX();
        double fy = point.getY() - node.getPoint().getY();
        double length = Math.sqrt(Math.pow(fx, 2) + Math.pow(fy, 2));

        fx /= length; fy /= length;

        Point next = new Point(
                (int) Math.round(node.getPoint().getX() + fx * stepSize),
                (int) Math.round(node.getPoint().getY() + fy * stepSize));

        if (isObstacleAt(next)) {
            return extend();
        }

        arbiTreeListener.extendTowards(point);

        Extension ext = new Extension(node.growTo(next), target.contains(next));

        onExtension(ext);

        if (ext.inTarget) {
            connectingExtension = ext;
        }

        return ext;
    }

    public boolean isObstacleAt(Point point) {
        for (Shape obstacle : obstacles) {
            if (obstacle.contains(point)) {
                return true;
            }
        }
        return false;
    }

    protected void onExtension(Extension ext) {

    }

    public long findPath() {
        long iterations = 0;
        while (!extend().inTarget) {
            ++iterations;
        }
        return iterations;
    }

    public Step finalStep() {
        Extension ext = null;
        while (!(ext = extend()).inTarget);

        return new Step(ext);
    }

    public Step nextStep() {
        return new Step(extend());
    }

    public int size() {
        return root.size();
    }

    @Override
    public String toString() {
        return String.format("ArbiTree(start = %s, goal = %s)", getRoot().getPoint(), getTarget());
    }

    public Node getRoot() {
        return root;
    }

    public int getStepSize() {
        return stepSize;
    }

    public void setStepSize(int stepSize) {
        this.stepSize = stepSize;
    }

    public Random getRandom() {
        return random;
    }

    public Rectangle getSampleBounds() {
        return sampleBounds;
    }

    public void setSampleBounds(Rectangle sampleBounds) {
        this.sampleBounds = sampleBounds;
    }

    public Set<Shape> getObstacles() {
        return obstacles;
    }

    public Point getStart() {
        return getRoot().getPoint();
    }

    public Shape getTarget() {
        return target;
    }

    public void setArbiTreeListener(Listener arbiTreeListener) {
        this.arbiTreeListener = arbiTreeListener;
    }

    public Listener getArbiTreeListener() {
        return arbiTreeListener;
    }

    public boolean isGoalBias() {
        return goalBias;
    }

    public void setGoalBias(boolean goalBias) {
        this.goalBias = goalBias;

        if (biasedSampleBounds == null) {
            biasedSampleBounds = sampleBounds;
        }
    }

    public Rectangle getBiasedSampleBounds() {
        return biasedSampleBounds;
    }

    public Extension getConnectingExtension() {
        return connectingExtension;
    }

    public class Node {

        private Node parent;
        private Set<Node> children = new HashSet<Node>(3);

        private Point point;

        public Node(Node parent, Point point) {
            this.parent = parent;
            this.point = point;
        }

        public Node growTo(Point point) {
            Node node = createSibling(point);
            getChildren().add(node);
            arbiTreeListener.nodeInserted(node);

            return node;
        }

        protected Node createSibling(Point point) {
            return new Node(this, point);
        }

        public Node getNearestNeighbour(Point point) {
            return getNearestNeighbour(point, new SearchResult(this, distance(point))).node;
        }

        private SearchResult getNearestNeighbour(Point point, SearchResult nearestAsYet) {
            SearchResult nearest = nearestAsYet.min(new SearchResult(this, distance(point)));

            for (Node node : children) {
                nearest = nearest.min(node.getNearestNeighbour(point, nearest));
            }

            return nearest;
        }

        public int size() {
            int size = 1;

            for (Node child : children) {
                size += child.size();
            }

            return size;
        }

        public int distance(Point point) {
            // use quadratic distance to save the cycle for sqrt
            return (this.point.x - point.x) * (this.point.x - point.x) +
                    (this.point.y - point.y) * (this.point.y - point.y);
        }

        public boolean isInTarget() {
            return getTarget().contains(getPoint());
        }

        public List<Node> getRootPath() {
            List<Node> path = new LinkedList<Node>();

            buildRootPath(path);

            return path;
        }

        protected void buildRootPath(List<Node> path) {
            if (getParent() != null) {
                getParent().buildRootPath(path);
            }
            path.add(this);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            sb.append("[");
            for (Node child : children) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                sb.append(child.toString());
            }
            sb.append("]");
            return String.format("Node(%d, %d, %s)", point.x, point.y, sb.toString());
        }

        public Node getParent() {
            return parent;
        }

        public Set<Node> getChildren() {
            return children;
        }

        public Point getPoint() {
            return point;
        }
    }

    static final class SearchResult {
        public final Node node;
        public final int distanceToTarget;

        public SearchResult(Node node, int distanceToTarget) {
            this.node = node;
            this.distanceToTarget = distanceToTarget;
        }

        public SearchResult min(SearchResult result) {
            if (this.distanceToTarget == 0) return result;
            else if (result.distanceToTarget == 0) return this;

            if (result.distanceToTarget <= this.distanceToTarget) {
                return result;
            } else {
                return this;
            }
        }

        public static SearchResult empty() {
            return empty;
        }

        public static int min(int distanceA, int distanceB) {
            if (distanceA == 0) return distanceB;
            else if (distanceB == 0) return distanceA;

            if (distanceA <= distanceB) return distanceA;
            else return distanceB;
        }

        private final static SearchResult empty = new SearchResult(null, 0);
    }

    public static class Step extends tracketeer.search.Step {

        private Extension ext;

        public Step(Extension ext) {
            super(new Vector(ext.node.getPoint()), ext.inTarget);

            this.ext = ext;
        }

        @Override
        public List<Vector> getPath() {
            List<Node> nodes = ext.node.getRootPath();
            List<Vector> path = new LinkedList<Vector>();

            for (Node node : nodes) {
                path.add(new Vector(node.getPoint()));
            }

            return path;
        }
    }

    public static class Extension {
        public final Node node;
        public final boolean inTarget;

        public Extension(Node node, boolean inTarget) {
            this.node = node;
            this.inTarget = inTarget;
        }
    }

    public static interface Listener {
        void nodeInserted(Node node);
        void extendTowards(Point sample);
    }
}

