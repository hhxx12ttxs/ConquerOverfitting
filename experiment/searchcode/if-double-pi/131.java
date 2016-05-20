import java.util.Set;
import java.util.HashSet;
import java.util.Random;

public abstract class Sensor {
    protected double angle; // Current direction
    protected final int k; // Number of sectors
    protected final int length; // Beam distance
    protected int colour = 0; // The graph-colouring of this node (or 0)
    protected Coord position;
    protected Neighbourhood n;

    private final int MIN_SECTORS = 3;
    private final int MAX_SECTORS = 12;

    // Neighbours that have been "discovered" by this sensor.
    public Set<Sensor> knownNeighbours = new HashSet<Sensor>();

    public Sensor(Neighbourhood n, int k, Coord position, int length) {
        if (k == 0) {
            Random r = new Random();
            k = MIN_SECTORS + r.nextInt(MAX_SECTORS - MIN_SECTORS);
        }
        this.n = n;
        this.k = k;
        this.position = position;
        this.length = length;
        this.angle = Math.random() * 2 * pi - pi;
    }

    abstract public void tick();

    // Beam width (as an angle in radians)
    public double width() {
        return 2 * pi / this.k;
    }

    public int length() { return this.length; }
    public int len() { return this.length; } // alias for convenience
    public double angle() { return this.angle; }
    public Coord position() { return this.position; }
    public Coord pos() { return this.position; } // alias for convenience

    protected void communicate() {
        knownNeighbours.addAll(n.communicate(this));
    }

    protected void rotate() {
        this.angle += this.width();
        if (this.angle > pi) this.angle -= 2 * pi;
    }

    public int colour() { return colour; }
    public void setColour(int c) { this.colour = c; }

    public boolean inBeam(Sensor other) {
        return this.inBeam(other.position) && other.inBeam(this.position);
    }

    public boolean inRange(Sensor other) {
        return this.inRange(other.position) && other.inRange(this.position);
    }

    protected boolean inRange(Coord point) {
        int x = point.x() - this.position.x();
        int y = point.y() - this.position.y();
        return sq(x) + sq(y) < sq(this.length);
    }

    protected boolean inBeam(Coord point) {
        int x = point.x() - this.position.x();
        int y = point.y() - this.position.y();
        double minAngle = clampAngle(this.angle - this.width()/2);
        double maxAngle = clampAngle(this.angle + this.width()/2);
        double angle = Math.atan2(y, x);

        return inRange(point) && betweenAngles(minAngle, angle, maxAngle);
    }

    protected final double pi = Math.PI;
    protected double sq(double x) { return Math.pow(x, 2); }

    // Return min <= test <= max, accounting for discontinutity at pi.
    protected boolean betweenAngles(double min, double test, double max) {
        assert Math.min(min, max) >= -pi;
        assert Math.max(min, max) <= pi;
        assert test >= -pi;
        assert test <= pi;
        if (min > max) {
            return betweenAngles(min, test, pi)
                || betweenAngles(-pi, test, max);
        }
        return min <= test && test <= max;
    }

    protected double clampAngle(double a) {
        while (a < -pi) a += 2 * pi;
        while (a > pi) a -= 2 * pi;
        return a;
    }
}


