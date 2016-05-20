package forces;

import swarm.Swimmer;
import input.ForceSourcePoints;
import input.ElectrostaticSource;

import java.util.HashMap;

/**
 * Created by id on 03.07.14.
 */
public class CoulombForce extends Force {
    private static final double COULOMB_CONSTANT = 5e3;

    private final HashMap<ForceSourcePoints, ElectrostaticSource> forceSourcePoints;
    private final double particleCharge;

    public CoulombForce(HashMap<ForceSourcePoints, ElectrostaticSource> forceSourcePoints, double particleCharge) {
        this.forceSourcePoints = forceSourcePoints;

        this.particleCharge = particleCharge;
    }

    @Override
    public double[] calculateForce(Swimmer swimmer) {
        if (forceSourcePoints.isEmpty()) {
            return new double[]{0,0};
        }
        final double swimmerX = swimmer.getX();
        final double swimmerY = swimmer.getY();
        double fx = 0;
            double fy = 0;
            for (ElectrostaticSource fs : forceSourcePoints.values()) {
                // System.out.println("dx: " + (swimmer.getX() - fs.getX()));
                double dx = calculateNormalizedDelta(swimmerX - fs.getX());
                double dy = calculateNormalizedDelta(swimmerY - fs.getY());

                double distance = Math.sqrt(dx * dx + dy * dy);
                final double force = COULOMB_CONSTANT * particleCharge * fs.getCharge() / distance / distance;
                fx += force * dx / distance;
                fy += force * dy / distance;
            }
        return new double[]{fx,fy};
    }

    /**
     * Make sure that distance is never smaller than a minimum distance (prevents infinities)
     */
    private double calculateNormalizedDelta(double dx) {
        return dx < 0 ? Math.min(-MINIMUM_DISTANCE, dx) : Math.max(MINIMUM_DISTANCE, dx);
    }


}

