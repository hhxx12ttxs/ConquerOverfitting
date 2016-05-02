package cc.creativecomputing.simulation.steering.behavior;

import cc.creativecomputing.math.CCVector3f;
import cc.creativecomputing.simulation.CCParticle;

/**
 * <p>CCSeek (or pursuit of a static target) acts to steer the character towards a
 * specified position in global space. This behavior adjusts the character so
 * that its velocity is radially aligned towards the target. Note that this is
 * different from an attractive force (such as gravity) which would produce an
 * orbital path around the target point.</p>
 * <p>The desired velocity is a vector in
 * the direction from the character to the target. The length of desired
 * velocity could be max_speed, or it could be the character's current speed,
 * depending on the particular application. The steering vector is the
 * difference between this desired velocity and the character's current
 * velocity.</p>
 * <p>If a character continues to seek, it will eventually pass through
 * the target, and then turn back to approach again. This produces motion a bit
 * like a moth buzzing around a light bulb.</p>
 * 
 * @author tex
 */
public class CCSeek extends CCTargetBehavior{
	
	/**
	 * Initializes a new seek directing the agent to the given target.
	 * @param i_target, Vector3f the target the agent is directed to
	 */
	public CCSeek(final CCVector3f theTarget){
		super(theTarget);
	}

	/**
	 * @invisible
	 */
	public boolean apply(final CCParticle theAgent, final CCVector3f theForce, float theDeltaTime){
		final float goalLength = 1.1F * theAgent.velocity().length();

		theForce.set(_myTarget);
		theForce.subtract(theAgent.position);
		theForce.truncate(goalLength);
		theForce.subtract(theAgent.velocity());
		theForce.truncate(theAgent.maxForce);
		return true;
	}

}

