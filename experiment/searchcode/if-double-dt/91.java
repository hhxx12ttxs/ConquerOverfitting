package swarm;


import forces.Force;
import main.MarmarMain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by derio on 02.07.2014.
 */
public class Swimmer extends Sprite {
    private static final boolean DEBUG_FORCE = false;
    private final List<Effect> effects= new ArrayList<>();
    private final Swarm swarm;
    private Set<Force> forces;
    private double mass = 0.1;

    Swimmer( Swarm swarm) {
        super(swarm.getAnimation());
        this.swarm=swarm;
        this.forces= new HashSet<>();
        swarm.add(this);
    }

    public List<Effect> getEffects() {
        return effects;
    }

    public void addEffect(Effect effect) {
       effects.add(effect);
    }

    public void removeAllEffects() {
        effects.clear();
    }

    public Swarm getSwarm() {
        return swarm;
    }

    public Animation getAnimation() {
        return animation;
    }

    public void setAnimation(Animation animation) {
        this.animation = animation;
    }

    public void update(long timePassed){
        double[] f = calculateTotalForce(timePassed);
      calculateNextPositionAndVelocity(f);
      interactWithBoundaries();
    }

    private void interactWithBoundaries() {
        swarm.getWallBehavior().interactWithWall(this);
    }

    private void calculateNextPositionAndVelocity(double[] f) {
        double fx = f[0];
        double fy = f[1];
        final double dt = MarmarMain.TIME_STEP;

        // Kinematic equations
        final double nextX = getX() + getVelocityX() * dt + 0.5 * fx * mass  * dt * dt;
        final double nextY = getY() + getVelocityY() * dt + 0.5 * fy * mass  * dt * dt;
        final double nextVX = getVelocityX() + fx * mass * dt;
        final double nextVY = getVelocityY() + fy * mass * dt;

        setX(nextX);
        setY(nextY);
        setVelocityX(nextVX);
        setVelocityY(nextVY);

        if(DEBUG_FORCE)
            System.out.println("fx: "+fx+ " fy: "+fy + " nextX: "+nextX +" nextY: "+nextY +" nextVX: "+nextVX +" nextVY: "+nextVY);
    }

    private double[] calculateTotalForce(long timePassed) {
        double[] totalF= new double[]{0., 0.};
        for(Force force:forces){
            double[] f = force.calculateForce(this);
            if(DEBUG_FORCE)
                System.out.println("force = " + force.getClass().getCanonicalName() + "f: [" + f[0]+ ", "+ f[1] +"]" );
            totalF[0]+=f[0];
            totalF[1]+=f[1];
        }
        return totalF;
    }

    public void addForce(Force force) {
//        final Iterator<Force> iterator = forces.iterator();
//        while(iterator.hasNext()){
//            final Force f = iterator.next();
//            // TODO: Do it correctly.. The idea is that not two same Force types can be present simultaneously
//            if(f.getClass().getCanonicalName().equals(force.getClass().getCanonicalName())){
//                iterator.remove();
//                break;
//            }
//        }

        this.forces.add(force);
    }

    public double getMass() {
        return mass;
    }
}

