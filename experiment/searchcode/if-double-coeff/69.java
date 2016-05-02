package cs5643.finalproj.knitted;

import java.util.*;
import cs5643.finalproj.Utils;
import cs5643.finalproj.Constants;
import javax.vecmath.*;
import javax.media.opengl.*;

/**
 * The structure is designed by Jonathan Kaldor
 * Current implementation has data duplication like particle_mass, segments, segment lengths.
 * The assumption is that the Cloth does not change after it's initialized (i.e., assigned Yarns).
 * @author cpwang
 */
public class Cloth {

	/** Number of particles */
	private int			N;

	/** Number of yarns */
	private	int			number_of_yarns;

	/** Number of segments */
	private int			number_of_segments;

	/** 3N list of N control point positions as [x1,y1,z1,x2,y2,z2,...xN,yN,zN] */
	private double[]	positions;

	/** Same as above, but for particle velocities */
	private double[]	velocities;

	/** Mass of each particle */
	private double[]	particle_mass;

	/** External forces on particles */
	private double[]	external_forces;

	/** Pin */
	private boolean[]	is_pinned;

	/** Segments */
	private int[][]		segments;

	/** Lengths of each segment */
	// TODO: Is it needed to have different lengths for each segment?
	private double[]	segment_lengths;

	/** Radius of each segment */
	// TODO: This is really a waste of space, since each yarn has only one radius
	private double[]	segment_radii;

	/** List of yarns */
	private Yarn[]		yarns;

	/** List of sheets. Each cloth contains one or more sheets. Most have one,
	 * for the entire cloth. Sweater is the only one with more than one
	 */
	private Sheet[]		sheets;

	/** List of places where one yarn is glued to another */
	private Glue[]		glues;

	/** Global force applied to whole cloth */
	// TODO: Remove
	//private Vector3d	force;

	/** Total mass */
	private double		mass;

	/** AABB Tree */
	private AABBTree	aabb_tree;

	/** Constructor */
	public Cloth () {
		double[]	roots = new double[3];
		int number_of_roots = solveCubicEquation(2.0, -4.0, -22.0, 24.0, roots);
		number_of_roots = 0;
	}

	/** Add yarns */
	public void addYarns (Yarn[] yarns, boolean[] is_pinned, Glue[] glues) {
		
		int	index;

		this.yarns = yarns.clone();
		mass = 0;
		N = 0;
		number_of_segments = 0;
		number_of_yarns = yarns.length;
		for(int i=0; i<number_of_yarns; i++) {
			N += yarns[i].getNumberOfParticles();
			mass += yarns[i].getMass();
			number_of_segments += yarns[i].getNumberOfSegments();
		}

		// Pinned particles
		this.is_pinned = is_pinned.clone();

		// Build an array to store the ids of endpoints of all segments
		segments = new int[number_of_segments][2];
		segment_lengths = new double[number_of_segments];
		segment_radii = new double[number_of_segments];
		index = 0;
		for(int i=0; i<number_of_yarns; i++) {
			for(int j=yarns[i].getFirstParticle(); j<yarns[i].getLastParticle(); j++) {
				segments[index][0] = j;
				segments[index][1] = j+1;
				segment_lengths[index] = yarns[i].getSegmentLength(j-yarns[i].getFirstParticle());
				segment_radii[index] = yarns[i].getRadius();
				index++;
			}
		}

		// Set mass of each particle
		particle_mass = new double[N];
		index = 0;
		for(int i=0; i<number_of_yarns; i++) {
			for(int j=0; j<yarns[i].getNumberOfParticles(); j++)
				particle_mass[index++] = yarns[i].getMassPerParticle();
		}

		// Allocate external force accumulator
		external_forces = new double[N*3];
		clearExternalForces();

		// Initialize AABB Tree (not built yet)
		aabb_tree = new AABBTree(this);

		// Glues
		this.glues = glues.clone();
	}

	/** Clear external forces */
	public void clearExternalForces () {
		for(int i=0; i<3*N ;i++)
			external_forces[i] = 0;
	}

	/** Apply force on particle */
	public void applyForce (int particle_id, Vector3d force) {
		external_forces[particle_id*3] += force.x;
		external_forces[particle_id*3+1] += force.y;
		external_forces[particle_id*3+2] += force.z;
	}

	/** Set positions and velocities of particles */
	public void setPositionsAndVelocities (double[] positions, double[] velocities) {
		// N should be equal to positions.length / 3
		this.positions = positions.clone();
		this.velocities = velocities.clone();
	}

	/** Draw the cloth */
	public void display (GL gl) {
		for(int i=0; i<number_of_yarns; i++) {
			yarns[i].display(gl);
		}
		aabb_tree.display(gl);
	}

	/** Get position of a particle */
	public Point3d getPosition (int particle_id) {
		return new Point3d(positions[particle_id*3], positions[particle_id*3+1], positions[particle_id*3+2]);
	}

	/** Get velocity of a particle */
	public Vector3d getVelocity (int particle_id) {
		return new Vector3d(velocities[particle_id*3], velocities[particle_id*3+1], velocities[particle_id*3+2]);
	}

	/** Compute the mass of the cloth (sum of all yarns) */
	public double getMass () {
		return mass;
	}
	
	/** Get positions of particles */
	public double[] getPositions () { return positions; }
	
	/** Get velocities of particles */
	public double[] getVelocities () { return velocities; }

	/** Get pin information */
	public boolean[] getPins() { return is_pinned; }

	/** Get the number of segments */
	public int getNumberOfSegments () { return number_of_segments; }

	/** Get the number of particles */
	public int getNumberOfParticles () { return N; }

	/** Get number of yarns */
	public int getNumberOfYarns () { return number_of_yarns; }

	/** Number of glues */
	public int getNumberOfGlues () { return glues.length; }

	/** Get yarn */
	public Yarn getYarn (int id) { return yarns[id]; }

	/** Get glue */
	public Glue getGlue (int id) { return glues[id]; }

	/** Get segments */
	public int[][] getSegments () { return segments; }

	/** Get segment radius */
	public double[] getSegmentRadii () { return segment_radii; }

	/** Apply global external forces to particles (gravity, mass-proportional damping) */
	public void applyGlobalExternalForces () {

		// Apply forces to each particle
		for(int i=0; i<N; i++) {
			// Gravity to Y-axis
			external_forces[i*3+1] += -10.0f * particle_mass[i];
			// TODO: Debug: generate oblique velocity
			/*external_forces[i*3] += 5.0f * particle_mass[i];
			external_forces[i*3+2] += 5.0f * particle_mass[i];*/

			// Mass-proportional damping
			external_forces[i*3] += -Constants.DAMPING_MASS * particle_mass[i] * velocities[i*3];
			external_forces[i*3+1] += -Constants.DAMPING_MASS * particle_mass[i] * velocities[i*3+1];
			external_forces[i*3+2] += -Constants.DAMPING_MASS * particle_mass[i] * velocities[i*3+2];
		}

	}

	/** Accumulate external force for one particle */
	private void addExternalForce (int id, double coeff, Vector3d f) {
		external_forces[id*3] += coeff * f.x;
		external_forces[id*3+1] += coeff * f.y;
		external_forces[id*3+2] += coeff * f.z;
	}

	/** Apply internal forces */
	public void applyInternalForces (double dt) {

		// Update AABB Tree
		// This is needed because penalty force depends on positions, and the positions is changed in the last iteration
		aabb_tree.buildTree(dt);
		
		// Apply spring forces to each segment
		for(int i=0; i<number_of_segments; i++) {
			final int		i1 = segments[i][0],
							i2 = segments[i][1];
			final double	rest_length = segment_lengths[i];

			Vector3d	l = new Vector3d(positions[3*i1]-positions[3*i2], positions[3*i1+1]-positions[3*i2+1], positions[3*i1+2]-positions[3*i2+2]),
						l_dot = new Vector3d(velocities[3*i1]-velocities[3*i2], velocities[3*i1+1]-velocities[3*i2+1], velocities[3*i1+2]-velocities[3*i2+2]);
			double		cur_length = l.length(),
						coeff = -(Constants.STIFFNESS_STRETCH * (cur_length - rest_length) + Constants.DAMPING_STRETCH * l_dot.dot(l) / cur_length) / cur_length;

			addExternalForce(i1, coeff, l);
			addExternalForce(i2, -coeff, l);
		}

		// Apply spring force to the endpoints of two connected segments to resist bending
		/*for(int i=0; i<number_of_yarns; i++) {
			final Yarn	yarn = yarns[i];
			final int	begin_index = yarn.getFirstParticle(),
						end_index = yarn.getLastParticle();
			for(int j=begin_index; j<=end_index-2; j++) {
				// If the two endpoints of the connected two segments are closer
				// then the sum of their rest-length, push them away.
				// Note that this will also increase the stiffness of the spring
				// forces between the endpoints of a segment.
				final int		i1 = j, i2 = j+2;
				final double	len1 = yarn.getSegmentLength(j-begin_index),
								len2 = yarn.getSegmentLength(j-begin_index+1),
								rest_length = len1 + len2;
				Vector3d		l = new Vector3d(positions[i1*3]-positions[i2*3],
									positions[i1*3+1]-positions[i2*3+1],
									positions[i1*3+2]-positions[i2*3+2]),
								l_dot = new Vector3d(velocities[i1*3]-velocities[i2*3],
									velocities[i1*3+1]-velocities[i2*3+1],
									velocities[i1*3+2]-velocities[i2*3+2]);
				double			cur_length = l.length(),
								coeff = -(Constants.STIFFNESS_BEND * (cur_length - rest_length) + Constants.DAMPING_STRETCH * l_dot.dot(l) / cur_length) / cur_length;

				addExternalForce(i1, coeff, l);
				addExternalForce(i2, -coeff, l);
			}
		}*/

		// Applying penalty force to separate segments that are too close
		// TODO: All pair test: remove
		/*for(int i=0; i<number_of_segments-1; i++) {
			for(int j=i+1; j<number_of_segments; j++) {*/
		/*HashSet<SegmentPair>	overlapped_segments = new HashSet<SegmentPair>();
		aabb_tree.intersectSelf(overlapped_segments);
		for(SegmentPair sp : overlapped_segments) {
			final int	i = sp.first, j = sp.second;
			{

				final int	i1 = segments[i][0],
							i2 = segments[i][1],
							i3 = segments[j][0],
							i4 = segments[j][1];

				if(i1 == i3 || i1 == i4 || i2 == i3 || i2 == i4) {
					// If two segments share common particle, skip them
					continue;
				}

				Point3d		x1 = new Point3d(positions[i1*3], positions[i1*3+1], positions[i1*3+2]),
							x2 = new Point3d(positions[i2*3], positions[i2*3+1], positions[i2*3+2]),
							x3 = new Point3d(positions[i3*3], positions[i3*3+1], positions[i3*3+2]),
							x4 = new Point3d(positions[i4*3], positions[i4*3+1], positions[i4*3+2]);
				Vector3d	v1 = new Vector3d(velocities[i1*3], velocities[i1*3+1], velocities[i1*3+2]),
							v2 = new Vector3d(velocities[i2*3], velocities[i2*3+1], velocities[i2*3+2]),
							v3 = new Vector3d(velocities[i3*3], velocities[i3*3+1], velocities[i3*3+2]),
							v4 = new Vector3d(velocities[i4*3], velocities[i4*3+1], velocities[i4*3+2]),
							x21 = new Vector3d(), x43 = new Vector3d(),
							v21 = new Vector3d(), v43 = new Vector3d();

				x21.sub(x2, x1);
				x43.sub(x4, x3);
				v21.sub(v2, v1);
				v43.sub(v4, v3);
				
				// Calculate the penetration depth
				// TODO: Use the radius of yarns
				double[]		coeffs = new double[2];
				double			distance = findClosestPoints(x1, x2, x3, x4, coeffs),
								depth = (segment_radii[i] + segment_radii[j]) - distance;

				if(i == 24 || j == 24)
					depth = depth;

				if(depth > 0) {
					// TODO: Debug: Note the rest length of a segment should not be less than 2*h, or the penalty force will be always applied
					//System.out.println(String.format("Segment %d and %d are too close", i, j));

					// Use the line connected the nearest points as the normal
					Point3d		c1 = new Point3d(), c2 = new Point3d();
					Vector3d	c1_dot = new Vector3d(), c2_dot = new Vector3d(), c12_dot = new Vector3d();
					Vector3d	normal = new Vector3d(),
								c12_dot_t = new Vector3d(),
								c12_dot_n = new Vector3d();
					c1.scaleAdd(coeffs[0], x21, x1);
					c2.scaleAdd(coeffs[1], x43, x3);
					c1_dot.scaleAdd(coeffs[0], v21, v1);
					c2_dot.scaleAdd(coeffs[1], v43, v3);
					c12_dot.sub(c1_dot, c2_dot);
					normal.sub(c1, c2);

					if(normal.x == 0 && normal.y == 0 && normal.z == 0)
						normal.set(0, 0, 1);	// Arbitrary normal
					else
						normal.normalize();

					c12_dot_n.scale(c12_dot.dot(normal), normal);
					c12_dot_t.sub(c12_dot, c12_dot_n);
					double	vn = c12_dot.dot(normal),	// vn may < 0
							vt = c12_dot_t.length(),	// vt always >= 0
							v0 = 0.1 * depth / dt;

					// Apply penalty force if the separating speed is not enough
					if(vn < v0) {
						// Impluse is the smaller of
						// 1. the spring force act on the other segment, and
						// 2. the impulse that can push the segments to enough separating speed (v0)
						// First, compute the inverse mass
						double	iw1 = is_pinned[i1] ? 0 : 1 / particle_mass[i1],
								iw2 = is_pinned[i2] ? 0 : 1 / particle_mass[i2],
								iw3 = is_pinned[i3] ? 0 : 1 / particle_mass[i3],
								iw4 = is_pinned[i4] ? 0 : 1 / particle_mass[i4];
						// Then compute the force needed to make segments separating fast enough
						// Note that goal_separating_force > 0 since v0 > vn
						double	goal_separating_force = 
									(v0 - vn) / (((1-coeffs[0])*(1-coeffs[0])*iw1 + coeffs[0]*coeffs[0]*iw2 + (1-coeffs[1])*(1-coeffs[1])*iw3 + coeffs[1]*coeffs[1]*iw4) * dt);
						// The force cannot be larger than a force which spring force model supplies
						// Note that force > 0
						double	force = Math.min(Constants.STIFFNESS_STRETCH * depth, goal_separating_force);

						// TODO: Debug
						//if(Constants.STIFFNESS_STRETCH * depth > goal_separating_force)
						//	System.out.println("Achieve goal");

						// Calculate each point's new velocity
						if(!is_pinned[i1]) {
							final double	fn = (1-coeffs[0]) * force;	// fn > 0
							addExternalForce(i1, fn, normal);
							// Calculate friction if there is tangential velocity
							if(vt > 0)
								addExternalForce(i1, -Math.min(Constants.FRICTION_COEFF*fn, vt*particle_mass[i1]/dt), c12_dot_t);
						}
						if(!is_pinned[i2]) {
							final double	fn = coeffs[0] * force;	// fn > 0
							addExternalForce(i2, fn, normal);
							// Calculate friction if there is tangential velocity
							if(vt > 0)
								addExternalForce(i2, -Math.min(Constants.FRICTION_COEFF*fn, vt*particle_mass[i2]/dt), c12_dot_t);
						}
						if(!is_pinned[i3]) {
							final double	fn = (1-coeffs[1]) * force;	// fn > 0
							addExternalForce(i3, -fn, normal);
							// Calculate friction if there is tangential velocity
							if(vt > 0)
								addExternalForce(i3, Math.min(Constants.FRICTION_COEFF*fn, vt*particle_mass[i3]/dt), c12_dot_t);
						}
						if(!is_pinned[i4]) {
							final double	fn = coeffs[1] * force;	// fn > 0
							addExternalForce(i4, -fn, normal);
							// Calculate friction if there is tangential velocity
							if(vt > 0)
								addExternalForce(i4, Math.min(Constants.FRICTION_COEFF*fn, vt*particle_mass[i4]/dt), c12_dot_t);
						}
						
					} // if vn < v0
				} // if depth > 0

			}
		}*/
	}

	/** Advance velocity (unconstrained) */
	public void advanceVelocities (double dt) {

		// Apply forces to each particle
		for(int i=0; i<N; i++) {
			// Apply external forces
			velocities[3*i] += external_forces[3*i] * dt / particle_mass[i];
			velocities[3*i+1] += external_forces[3*i+1] * dt / particle_mass[i];
			velocities[3*i+2] += external_forces[3*i+2] * dt / particle_mass[i];
		}
	}

	/** Advance position */
	public void advancePositions (double dt) {
		for(int i=0; i<3*N; i++) {
			positions[i] += velocities[i] * dt;
		}
	}

	/** Velocity filter: the floor (y=0) */
	private int applyVFFloor (double dt) {

		final double	epsilon = 0.5;
		int				number_of_collisions = 0;

		for(int i=0; i<N; i++) {
			final double	y_pos = positions[i*3+1],
							y_vel = velocities[i*3+1];
			if(y_pos + y_vel*dt <= 0) {
				// Collide
				number_of_collisions++;

				velocities[i*3+1] += (1 + epsilon) * (-y_vel) * particle_mass[i];
			}
		}

		return number_of_collisions;
	}

	/** Compute a X b dot c
	 * TODO: may need to make this faster after it's correct */
	private double crossDotProduct (Vector3d a, Vector3d b, Vector3d c) {
		Vector3d	v = new Vector3d();
		v.cross(a, b);
		return v.dot(c);
	}

	/** Solve the quadratic equation of t: A*t^2 + B*t + C = 0 */
	private int solveQuadraticEquation (double A, double B, double C, double[] roots) {
		// Normalize A,B,C so that the largest magnitude of coefficient is 1
		// (see [Heath, p26])
		double	max = Math.max(Math.max(Math.abs(A), Math.abs(B)), Math.abs(C));
		if(max == 0)
			return 0;	// No root
		A /= max;
		B /= max;
		C /= max;

		double	D = B*B - 4*A*C;
		// TODO: triggered when A is small?
		if(A == 0) {
			if(B == 0)
				return 0;
			else {
				roots[0] = -C/B;
				return 1;
			}
		} else if(B == 0) {
			double	double_root = -C/A;
			if(double_root < 0)
				return 0;
			else {
				double_root = Math.sqrt(double_root);
				roots[0] = -double_root;
				roots[1] = double_root;
			}
		} else if(D < 0) {
			return 0;
		} else {	// B != 0 and D >= 0
			double	X;
			// The reason to use this is to prevent "catastrophic cancellation" mentioned in [Heath,p26]
			// i.e. make the absolute value of X as large as possible
			if(B > 0)
				X = -0.5 * (B + Math.sqrt(D));
			else
				X = -0.5 * (B - Math.sqrt(D));
			double	t1 = X/A, t2 = C/X;
			if(t1 > t2) {
				roots[0] = t2;
				roots[1] = t1;
			} else {
				roots[0] = t1;
				roots[1] = t2;
			}
			return 2;
		}
		return -1;	// Should not be here
	}

	/** Solve the cubic equation of t: c0 + c1*t + c2*t^2 + c3*t^3 = 0 */
	private int solveCubicEquation (double c3, double c2, double c1, double c0, double[] roots) {
		// Using the Root-finding formula in http://en.wikipedia.org/wiki/Cubic_function
		// Also see http://www.1728.com/cubic2.htm
		// TODO: Not sure if this is accurate enough comparing to Cardano's method

		if(c3 == 0) {
			// It's a quadratic function
			return solveQuadraticEquation(c2, c1, c0, roots);
		} else {
			final double	q = (3*c3*c1-c2*c2) / (9*c3*c3),
							r = (9*c3*c2*c1-27*c3*c3*c0-2*c2*c2*c2) / (54*c3*c3*c3),
							discriminant = q*q*q + r*r;

			if(discriminant > 0) {
				// Only one real root
				final double	sqrt_discriminant = Math.sqrt(discriminant),
								s = Math.cbrt(r + sqrt_discriminant),
								t = Math.cbrt(r - sqrt_discriminant);
				roots[0] = s + t - c2/(3*c3);
				return 1;
			} else {
				// Three real roots (may have duplication)
				final double	tho = Math.sqrt(-q*q*q),
								theta = Math.acos(r / tho),
								cbrt_tho = Math.sqrt(-q),
								s_sub_t_i = -2 * cbrt_tho * Math.sin(theta/3),
								s_add_t = 2 * cbrt_tho * Math.cos(theta/3),
								sqrt_3_div_2 = Math.sqrt(3) / 2; // TODO: Make this constant
				double			x1 = s_add_t - c2/(3*c3),
								x2 = -s_add_t/2 - c2/(3*c3) - sqrt_3_div_2 * s_sub_t_i,
								x3 = -s_add_t/2 - c2/(3*c3) + sqrt_3_div_2 * s_sub_t_i;

				// Make x2 < x3
				if(s_sub_t_i < 0) {
					double	t = x2;
					x2 = x3;
					x3 = t;
				}

				if(x1 < x2) {
					roots[0] = x1;
					roots[1] = x2;
					roots[2] = x3;
				} else if(x1 < x3) {
					roots[0] = x2;
					roots[1] = x1;
					roots[2] = x3;
				} else {
					roots[0] = x2;
					roots[1] = x3;
					roots[2] = x1;
				}

				return 3;
			}
		}
	}
	/*private int solveCubicEquation (double c3, double c2, double c1, double c0, double[] roots) {
		// Using the Cardano's method in http://en.wikipedia.org/wiki/Cubic_function#Cardano.27s_method

		if(c3 == 0) {
			// It's a quadratic function
			return solveQuadraticEquation(c2, c1, c0, roots);
		} else {
			// Normalize c0~c3 so that c3 = 1
			c0 /= c3;
			c1 /= c3;
			c2 /= c3;
			// c3 shouldn't be used after here

			final double	p = c1 - (c2*c2)/3,
							q = c0 + (2*c2*c2*c2-9*c2*c1)/27;

			if(p == 0 && q == 0) {
				// Triple real root -c2/3
				roots[0] = -c2/3;
				return 1;
			} else if(p == 0) {
				// q != 0
				// One real root
				roots[0] = -Math.cbrt(q)-c2/3;
				return 1;
			} else if(q == 0) {
				// p != 0
				if(p > 0) {
					// one real root
					roots[0] = -c2/3;
					return 1;
				} else {
					// Three distinct real roots
					final double	sqrt_neg_p = Math.sqrt(-p),
									neg_c2_div_3 = -c2/3;
					roots[0] = neg_c2_div_3 - sqrt_neg_p;
					roots[1] = neg_c2_div_3;
					roots[2] = neg_c2_div_3 + sqrt_neg_p;
					return 3;
				}
			} else {
				// p !=0 and q != 0
				double[]	u3 = new double[2];
				if(solveQuadraticEquation(1.0, q, -p*p*p/27, u3) == 0) {
					// Is this possible?
					return 0;
				}
				// TODO: What now?
			}
		}

		return -1;	// Should not be here
	}*/

	/** Project p onto line q-r: the projected point is q+alpha*r_sub_q */
	private double projectToLine (Tuple3d p, Tuple3d q, Tuple3d r, Vector3d r_sub_q) {
		Vector3d	p_sub_q = new Vector3d(p);
		p_sub_q.sub(q);
		double	alpha = (r_sub_q.dot(p_sub_q) / r_sub_q.lengthSquared());

		return alpha;
	}

	/** Find the closest points of two segments x1-x2 and x3-x4. The points are
	 * x1+coeff[0]*x21 and x3+coeff[1]*x43. Return the distance between closest
	 * points */
	private double findClosestPoints (Point3d x1, Point3d x2, Point3d x3, Point3d x4, double[] coeffs) {

		final double	roundoff_tolerance = 1e-6;
		Vector3d		v = new Vector3d(),
						x21 = new Vector3d(),
						x31 = new Vector3d(),
						x43 = new Vector3d();

		x21.sub(x2, x1);
		x31.sub(x3, x1);
		x43.sub(x4, x3);

		v.cross(x21, x43);
		if(v.lengthSquared() < roundoff_tolerance*roundoff_tolerance) {
			// 1D problem: x21 and x43 are paralleled
			// Project x1 and x2 onto edge x3-x4

			// TODO: Remove this
			// In 1D case, coplanar doesn't mean they are close enough
			// (parallel vectors are always coplanar)
			// Compute the distance between them
			/*v.cross(x31, x43);
			final double	distance = v.length() / x43.length();
			if(distance > roundoff_tolerance)
				return false;	// No overlap*/

			final double	alpha1 = projectToLine(x1, x3, x4, x43),
							alpha2 = projectToLine(x2, x3, x4, x43);
			double			a1 = alpha1, a2 = alpha2;

			// Make alpha1 < alpha 2
			if(a1 > a2) {
				double	t = a1;
				a1 = a2;
				a2 = t;
			}

			// Test if [0,1] and [alpha1,alpha2] overlaps
			double	c1, c2;
			if(a1 > 1) {
				// No overlap, the closest points are 1 and a1
				c1 = (a1-alpha1)/(alpha2-alpha1);
				c2 = 1;
			} else if(a2 < 0) {
				// No overlap, the closest points are a2 and 0
				c1 = (a2-alpha1)/(alpha2-alpha1);
				c2 = 0;
			} else {
				// Overlap, take the midpoint of the overlapping region as the nearest point
				a1 = (a1>0 ? a1 : 0);
				a2 = (a2<1 ? a2 : 1);
				final double	mid = (a1+a2)/2;
				c1 = (mid-alpha1)/(alpha2-alpha1);
				c2 = mid;
			}

			coeffs[0] = c1;
			coeffs[1] = c2;
		} else {
			// General case: find the closest points by solving normal equation [Bridson et al., 2002]:
			// [ x21.x21 -x21.x43; -x21.x43 x43.x43 ] * [coeff1; coeff2] = [x21.x31; -x43.x31]
			// Let [ x21.x21 -x21.x43; -x21.x43 x43.x43 ] = [a b; c d],
			// [x21.x31; -x43.x31] = [e; f];
			final double	a = x21.dot(x21), b = -x21.dot(x43), c = b, d = x43.dot(x43),
							e = x21.dot(x31), f = -x43.dot(x31),
							determinant = a*d - b*c;

			// TODO: Debug: I guess the determinant != 0 when x21 and x43 are not paralleled, but this lacks proof
			if(determinant == 0)
				throw new RuntimeException("Determinant shouldn't be zero");

			double	c1 = (e*d - b*f) / determinant,
					c2 = (a*f - c*e) / determinant;

			// TODO: Debug
			/*if(Double.isNaN(c1) || Double.isNaN(c2))
				System.out.println(c1);*/

			// Clamp c1, c2: "the point that moved the most during clamping is
			// gauranteed to be one part of the answer" [Bridson et al., 2002]
			final double	cc1 = Math.max(Math.min(c1, 1), 0),
							cc2 = Math.max(Math.min(c2, 1), 0);
			if(cc1 != c1 || cc2 != c2) {
				// If anything is clamped, we need to project again
				if(Math.abs(cc1-c1) > Math.abs(cc2-c2)) {
					// cc1 is part of the answer, project cc1 to x3-x4
					Point3d	p = new Point3d();
					p.scaleAdd(cc1, x21, x1);
					double	beta = projectToLine(p, x3, x4, x43);
					// Clamp again
					beta = Math.max(Math.min(beta, 1), 0);
					coeffs[0] = cc1;
					coeffs[1] = beta;
				} else {
					// cc2 is part of the answer, project cc2 to x1-x2
					Point3d	p = new Point3d();
					p.scaleAdd(cc2, x43, x3);
					double	beta = projectToLine(p, x1, x2, x21);
					// Clamp again
					beta = Math.max(Math.min(beta, 1), 0);
					coeffs[0] = beta;
					coeffs[1] = cc2;
				}
			} else {
				coeffs[0] = c1;
				coeffs[1] = c2;
			}
		}

		// Compute the distance between the closets points, and test if it is
		// smaller than 2*h (i.e. the distance between two segments is less
		// than sum of their virtual width)
		// TODO: Make this faster
		Vector3d	p1 = new Vector3d(), p2 = new Vector3d();
		p1.scaleAdd(coeffs[0], x21, x1);
		p2.scaleAdd(coeffs[1], x43, x3);
		p1.sub(p2);
		return p1.length();
	}

	/** Velocity filter: edge-edge collision */
	private int applyVFEdge2EdgeCollision (double dt, int number_of_iterations) {

		int	number_of_collisions = 0;

		// TODO: Test
		/*HashSet<SegmentPair>	overlapped_segments = new HashSet<SegmentPair>();
		aabb_tree.intersectSelf(overlapped_segments);
		if(overlapped_segments.size() > 0)
			System.out.println(overlapped_segments.size());*/

		// TODO: Remove
		// Right now test all pairs of segments (of either the same yarn of different yarns)
		/*for(int i=0; i<number_of_segments-1; i++) {
			for(int j=i+1; j<number_of_segments; j++) {*/
		HashSet<SegmentPair>	overlapped_segments = new HashSet<SegmentPair>();
		aabb_tree.intersectSelf(overlapped_segments);
		for(SegmentPair sp : overlapped_segments) {
			final int	i = sp.first, j = sp.second;
			{

				// TODO: Too many object creation
				final int	i1 = segments[i][0],
							i2 = segments[i][1],
							i3 = segments[j][0],
							i4 = segments[j][1];
				
				if(i1 == i3 || i1 == i4 || i2 == i3 || i2 == i4) {
					// If two segments share common particle, skip them
					continue;
				}

				// TODO: Debug
				/*if(i == 24 || j == 24)
					aabb_tree = aabb_tree;*/

				Point3d		x1 = new Point3d(positions[i1*3], positions[i1*3+1], positions[i1*3+2]),
							x2 = new Point3d(positions[i2*3], positions[i2*3+1], positions[i2*3+2]),
							x3 = new Point3d(positions[i3*3], positions[i3*3+1], positions[i3*3+2]),
							x4 = new Point3d(positions[i4*3], positions[i4*3+1], positions[i4*3+2]);
				Vector3d	v1 = new Vector3d(velocities[i1*3], velocities[i1*3+1], velocities[i1*3+2]),
							v2 = new Vector3d(velocities[i2*3], velocities[i2*3+1], velocities[i2*3+2]),
							v3 = new Vector3d(velocities[i3*3], velocities[i3*3+1], velocities[i3*3+2]),
							v4 = new Vector3d(velocities[i4*3], velocities[i4*3+1], velocities[i4*3+2]),
							x21 = new Vector3d(), x31 = new Vector3d(), x41 = new Vector3d(),
							v21 = new Vector3d(), v31 = new Vector3d(), v41 = new Vector3d(),
							x43 = new Vector3d();

				x21.sub(x2, x1);
				x31.sub(x3, x1);
				x41.sub(x4, x1);
				v21.sub(v2, v1);
				v31.sub(v3, v1);
				v41.sub(v4, v1);
				x43.sub(x4, x3);

				// Test if the segments x1-x2 and x3-x4 are coplanar
				// Build the cubic function c0 + c1*t + c2*t^2 + c3*t^3
				double		c0 = crossDotProduct(x21, x31, x41),
							c1 = crossDotProduct(v21, x31, x41) + crossDotProduct(x21, v31, x41) + crossDotProduct(x21, x31, v41),
							c2 = crossDotProduct(v21, v31, x41) + crossDotProduct(v21, x31, v41) + crossDotProduct(x21, v31, v41),
							c3 = crossDotProduct(v21, v31, v41);
				double[]	roots = new double[3];
				int			number_of_roots;
				
				// Normalize so that the largest magnitude of c0, c1, c2, c3 is 1
				double	max_c = Math.max(Math.max(Math.max(Math.abs(c3), Math.abs(c2)), Math.abs(c1)), Math.abs(c0));
				if(max_c == 0)
					continue;	// No collision
				c0 /= max_c;
				c1 /= max_c;
				c2 /= max_c;
				c3 /= max_c;

				// TODO: Debug
				double		c0b = c0, c1b = c1, c2b = c2, c3b = c3;

				// By experiment, if c3 or c2 is very small, the roots may have large error
				final double	param_threshold = 1e-6;
				if(c3 < param_threshold && c3 > -param_threshold)
					c3 = 0;
				if(c2 < param_threshold && c2 > -param_threshold)
					c2 = 0;

				number_of_roots = solveCubicEquation(c3, c2, c1, c0, roots);
				// TODO: Debug
				/*System.out.println("" + c3 + " " + c2 + " " + c1 + " " + c0);
				System.out.println("" + number_of_roots + " " + roots[0] + " " + roots[1] + " " + roots[2]);
				if(roots[0] < 0.1 && roots[0] > 0)
					System.out.println("here");*/
				//System.out.println(String.format("number_of_roots = %d, roots = {%f, %f, %f}", number_of_roots, roots[0], roots[1], roots[2]));

				// TODO: Debug: test if the roots computed are not actual roots
				// Check roots
				for(int k=0; k<number_of_roots; k++) {
					double	r = roots[k],
							error = c3*r*r*r + c2*r*r + c1*r + c0;
					// It seems hard to achieve 1e-6 for all roots (especially for super small or large root)
					// 041610_2.knit can generate cases with large error
					if(Math.abs(error) > 1e-3) {
						System.out.println("Error = " + error);
						System.out.println("" + c3 + " " + c2 + " " + c1 + " " + c0);
						System.out.println("" + c3b + " " + c2b + " " + c1b + " " + c0b);
						System.out.println(r);
						throw new RuntimeException("solveCubicEquation failed");
					}
				}

				if(number_of_roots == 0)
					continue;	// No collision

				// Check for possible collisions
				int			collision_index = -1;
				double[]	coeffs = new double[2];
				double		t_star;
				Point3d		x1_star = new Point3d(),
							x2_star = new Point3d(),
							x3_star = new Point3d(),
							x4_star = new Point3d();
				Vector3d	x21_star = new Vector3d(),
							x31_star = new Vector3d(),
							x41_star = new Vector3d(),
							x43_star = new Vector3d();

				for(int k=0; k<number_of_roots; k++) {
					
					/*if(roots[k] > 0 && roots[k] <= 2*dt)
						System.out.println("number_of_roots = " + number_of_roots + ", roots[k] = " + roots[k]);*/

					t_star = roots[k];
					x1_star.scaleAdd(t_star, v1, x1);
					x2_star.scaleAdd(t_star, v2, x2);
					x3_star.scaleAdd(t_star, v3, x3);
					x4_star.scaleAdd(t_star, v4, x4);
					// TODO: Decide threshold: in this case, x1_star-x2_star and x3_star-x4_star should be coplanar
					// So small distance is expected. However, I'm not sure if 1e-6 is a good idea
					if(roots[k] > 0 && roots[k] <= dt && findClosestPoints(x1_star, x2_star, x3_star, x4_star, coeffs) < 2*1e-6) {
						collision_index = k;
						break;
					}
				}

				if(collision_index == -1) {
					// Check the case that root = dt, in case that the roots we computed is just slightly larger than dt because of the rounding error
					// See [Bridson et al, 2002]. But not sure if this is needed.
					// 4/16/10: Yes this is needed! See 041610.knit and changeset f1582335a5f4.
					// Although this does not help solve hard problem (if there is
					// no minimum vn, it may takes a lot of iterations to solve),
					// it does prevent interpenetration
					t_star = dt;
					x1_star.scaleAdd(t_star, v1, x1);
					x2_star.scaleAdd(t_star, v2, x2);
					x3_star.scaleAdd(t_star, v3, x3);
					x4_star.scaleAdd(t_star, v4, x4);
					x21_star.sub(x2_star, x1_star);
					x31_star.sub(x3_star, x1_star);
					x41_star.sub(x4_star, x1_star);
					x43_star.sub(x4_star, x3_star);
					// TODO: What's the threshold?
					final double	cdp = crossDotProduct(x21_star, x31_star, x41_star);
					if(cdp > 1e-6 || cdp < -1e-6 || findClosestPoints(x1_star, x2_star, x3_star, x4_star, coeffs) > 2*1e-6)
					//if(true)
						continue;	// Ok, now we sure there is no collision
					// TODO: Debug
					else {
						t_star = t_star;
					}
				} else {
					// Update vectors
					x21_star.sub(x2_star, x1_star);
					x31_star.sub(x3_star, x1_star);
					x41_star.sub(x4_star, x1_star);
					x43_star.sub(x4_star, x3_star);
				}

				// Now we have a collision
				number_of_collisions++;
				// TODO: Debug
				//System.out.println(String.format("Segment %d and %d collide!", i, j));

				// TODO: Debug
				//System.out.println(crossDotProduct(x21_star, x31_star, x41_star));
				
				// Calculate collision points
				Point3d		c1_star = new Point3d(),	// The collision point of segement 1 (x1-x2)
							c2_star = new Point3d();	// The collision point of segement 2 (x3-x4)
				Vector3d	x1_star_dot = v1,
							x2_star_dot = v2,
							x3_star_dot = v3,
							x4_star_dot = v4,
							c1_star_dot = new Vector3d(),
							c2_star_dot = new Vector3d(),
							c12_star_dot = new Vector3d();	// Velocity of c1_star relative to c2_star
				double		beta1 = coeffs[0], beta2 = coeffs[1];
				c1_star.scaleAdd(beta1, x21, x1);
				c2_star.scaleAdd(beta2, x43, x3);
				c1_star_dot.scale(1-beta1, x1_star_dot);
				c1_star_dot.scaleAdd(beta1, x2_star_dot, c1_star_dot);
				c2_star_dot.scale(1-beta2, x3_star_dot);
				c2_star_dot.scaleAdd(beta2, x4_star_dot, c2_star_dot);
				c12_star_dot.sub(c1_star_dot, c2_star_dot);

				// Calculate normal
				Vector3d	normal = new Vector3d();
				normal.cross(x21_star, x43_star);
				normal.normalize();

				// Make dot(normal, c12_star_dot) < 0 (i.e., normal is point to the opposite direction that c1_star should go)
				double	vn = normal.dot(c12_star_dot);
				if(vn > 0) {
					normal.negate();
					vn = -vn;
				}

				// TODO: Debug
				/*System.out.println(String.format("normal = (%.2f, %.2f, %.2f), c12_star_dot = (%.2f, %.2f, %.2f)",
						normal.x, normal.y, normal.z, c12_star_dot.x, c12_star_dot.y, c12_star_dot.z));*/

				// TODO: May need to set minimum vn, as in project 2. But this time the test case of t_star = dt solves interpenetration,,
				// so see if we can drop this with penalty force
				// 04/18: no, penalty force does not solve the problem: it still locks
				// It seems that the system locks when 2 "V" shape yarns contact. Maybe sphere penalty force on the particles help.
				// Adaptively enlarge the force seems super helpful
				double vn_th = 0;
				if(number_of_iterations > 1000)
					vn_th = -1e2;
				else if(number_of_iterations > 100)
					vn_th = -1e0;
				else if(number_of_iterations > 10)
					vn_th = -1e-1;
				/*if(vn_th != 0)
					System.err.println(String.format("vn_th = %f, number_of_iterations = %d", vn_th, number_of_iterations));*/
				vn = Math.min(vn, vn_th);

				// TODO: Debug
				/*System.out.println(vn);
				if(vn < -100)
					vn = vn;*/

				// Compute impulse
				double	epsilon = 0.01;
				double	iw1 = is_pinned[i1] ? 0 : 1 / particle_mass[i1],
						iw2 = is_pinned[i2] ? 0 : 1 / particle_mass[i2],
						iw3 = is_pinned[i3] ? 0 : 1 / particle_mass[i3],
						iw4 = is_pinned[i4] ? 0 : 1 / particle_mass[i4];
				double	impulse = (1 + epsilon) * (-vn) / (iw1*(1-beta1)*(1-beta1) + iw2*beta1*beta1 + iw3*(1-beta2)*(1-beta2) + iw4*beta2*beta2);

				// TODO: Debug
				/*System.out.println(String.format("%d-%d and %d-%d: (%.2f, %.2f, %.2f)", i1, i2, i3, i4, normal.x, normal.y, normal.z));
				System.out.println(impulse);*/

				// Calculate each point's new velocity
				if(!is_pinned[i1]) {
					final double	c = (1-beta1) * impulse / particle_mass[i1];
					velocities[3*i1]   += normal.x * c;
					velocities[3*i1+1] += normal.y * c;
					velocities[3*i1+2] += normal.z * c;
				}
				if(!is_pinned[i2]) {
					final double	c = beta1 * impulse / particle_mass[i2];
					velocities[3*i2]   += normal.x * c;
					velocities[3*i2+1] += normal.y * c;
					velocities[3*i2+2] += normal.z * c;
				}
				if(!is_pinned[i3]) {
					final double	c = (1-beta2) * impulse / particle_mass[i3];
					velocities[3*i3]   -= normal.x * c;
					velocities[3*i3+1] -= normal.y * c;
					velocities[3*i3+2] -= normal.z * c;
				}
				if(!is_pinned[i4]) {
					final double	c = beta2 * impulse / particle_mass[i4];
					velocities[3*i4]   -= normal.x * c;
					velocities[3*i4+1] -= normal.y * c;
					velocities[3*i4+2] -= normal.z * c;
				}

				// TODO: Debug
				/*for(int k=0; k<3*N; k++) {
					if(Math.abs(velocities[i]) > 10)
						System.out.println("Velocities[" + i + "] = " + velocities[i]);
				}*/
			}
		}

		return number_of_collisions;
	}

	/** Pinned particles */
	private void applyVFPin () {
		for(int i=0; i<N; i++) {
			if(is_pinned[i]) {
				velocities[3*i] = velocities[3*i+1] = velocities[3*i+2] = 0;
			}
		}
	}

	/** Test if the length constraints are satisfied for the part of a yarn 
	 * start from segement start_segment to end_segment */
	private boolean isConstraintsSatisfied (int start_segment, int end_segment, double strain, double[] pos) {
		for(int k=start_segment; k<=end_segment; k++) {
			final int		i = segments[k][0],
							j = segments[k][1];
			final double	segment_length = segment_lengths[k],
							min_length_square = (segment_length*(1-strain)) * (segment_length*(1-strain)),
							//min_length_square = segment_lengths[k]*segment_lengths[k],
							max_length_square = (segment_length*(1+strain)) * (segment_length*(1+strain)),
							cur_length_square = Utils.squareLength(pos[i*3]-pos[j*3], pos[i*3+1]-pos[j*3+1], pos[i*3+2]-pos[j*3+2]);
			// If both particle i and j are pinned, we have no way to make it satisfy the constraint, so just ignore them
			if((!is_pinned[i] || !is_pinned[j]) && (cur_length_square > max_length_square || cur_length_square < min_length_square))
				return false;
		}
		return true;
	}

	/** Gauss-Seidel method */
	private double[] gaussSeidelMethod (int M, double[] b, GMatrix A) {
		double[]	lambda = new double[M];
		// Initial guess: b (the linear system is A*lambda = b)
		for(int i=0; i<M; i++)
			lambda[i] = b[i];
		for(int it=0; it<Constants.GS_ITERATIONS; it++) {
			for(int i=0; i<M; i++) {
				double	value = 0;
				for(int j=0; j<=i-1; j++)
					value += A.getElement(i, j) * lambda[j];
				for(int j=i+1; j<=M-1; j++)
					value += A.getElement(i, j) * lambda[j];

				// TODO: Not sure if this is right
				/*double	aii = A.getElement(i, i);
				if(aii == 0)
					aii = 1e-7;
				lambda[i] = (b[i] - value) / aii;*/
				if(A.getElement(i, i) != 0)
					lambda[i] = (b[i] - value) / A.getElement(i, i);
				else
					lambda[i] = 0;
			}
		}

		return lambda;
	}

	/** Apply constraints on part of a yarn. start_segment and end_index are
	 * the first and the last segment on that part. The first and last
	 * particles will be pinned, so effectively we try to move the particles
	 * between them to satisfy the constraints */
	private int applyPartConstraints (int start_segment, int end_segment, double[] pos, double strain, double dt, boolean enable_length) {
		
		int				number_of_iterations = 0;
		final int		Ml = enable_length ? end_segment - start_segment + 1 : 0,	// Number of length constraints
						start_particle = segments[start_segment][0],
						end_particle = segments[end_segment][1],
						part_N = end_particle - start_particle + 1;					// Number of particles
		
		// Find the glue constraints that are in this part of yarn. Note that currently glue particles from different parts is unsupported
		ArrayList<Glue>	involved_glues = new ArrayList<Glue>();
		for(int i=0; i<glues.length; i++) {
			if(glues[i].getParticle1() >= start_particle && glues[i].getParticle1() <= end_particle &&
					glues[i].getParticle2() >= start_particle && glues[i].getParticle2() <= end_particle)
				involved_glues.add(glues[i]);
		}
		final int		Mg = involved_glues.size(),
						M = Ml + Mg;

		JMatrix			J = new JMatrix(Ml, Mg, part_N, start_particle);
		GMatrix			A = new GMatrix(M, M);
		double[]		b,
						delta_x = new double[part_N*3];
		boolean			start_pinned = is_pinned[start_particle],
						end_pinned = is_pinned[end_particle];

		// TODO: Test
		// Pin the endpoints of the part of the yarn
		//is_pinned[start_particle] = is_pinned[end_particle] = true;
		//is_pinned[start_particle] = true;

		// TODO: Test
		//while(!isConstraintsSatisfied(start_segment, end_segment, strain, pos)) {
		while(true) {

			// TODO: Test
			/*if(number_of_iterations > 100)
				break;*/

			// TODO: Debug
			/*if(number_of_iterations > 10)
				System.out.println(number_of_iterations);*/

			J.clear();

			// Initialize matrix J and vector b
			// Length constraints
			if(enable_length) {
				for(int k=0; k<Ml; k++) {
					final int		i = segments[k+start_segment][0],
									j = segments[k+start_segment][1];
					final double	segment_length = segment_lengths[k+start_segment];
					J.setLengthConstraint(i, j, pos, segment_length);
					// TODO: Remove
					//b[k] = (Utils.squareLength(pos[i*3]-pos[j*3], pos[i*3+1]-pos[j*3+1], pos[i*3+2]-pos[j*3+2]) / segment_length - segment_length) / (dt*dt);
				}
			}

			// Glue constarints
			for(Glue g : involved_glues) {
				J.setGlueConstraint(g.getParticle1(), g.getParticle2(), pos);
			}

			// Get vector b
			b = J.get_b();

			// Check if all constraints are satisfied
			boolean	all_satisfied = true;
			for(int k=0; k<M; k++) {
				if(b[k] > strain || b[k] < -strain) {
					// TODO: Debug
					System.out.println(String.format("b[%d] = %f", k, b[k]));
					all_satisfied = false;
					break;
				}
			}
			if(all_satisfied)
				break;

			number_of_iterations++;

			// Compute the matrix A of the linear system
			J.computeA(A, particle_mass, is_pinned);

			// Gauss-Seidel method
			double[]	delta_lambda = gaussSeidelMethod(M, b, A);

			GMatrix	lambda = new GMatrix(M, 1, delta_lambda),
					bp = new GMatrix(M, 1);
			bp.mul(A, lambda);

			// TODO: Debug
			// Compute square error
			double	err = 0;
			for(int i=0; i<M; i++)
				err += (bp.getElement(i, 0)-b[i]) * (bp.getElement(i, 0)-b[i]);
			if(err > 1e-3)
				System.out.println("Error too large: " + err);

			// TODO: Debug
			/*for(int i=0; i<delta_lambda.length; i++)
				if(Double.isNaN(delta_lambda[i]))
					delta_lambda[i] = delta_lambda[i];*/

			// Update positions
			// Note: only the particles on this part of the yarn need to be updated
			// Also, the endpoints will not be updated since they are pinned
			J.compute_delta_x(delta_x, delta_lambda, particle_mass, is_pinned, dt);
			for(int i=0; i<part_N; i++) {
				pos[3*(i+start_particle)] += delta_x[3*i];
				pos[3*(i+start_particle)+1] += delta_x[3*i+1];
				pos[3*(i+start_particle)+2] += delta_x[3*i+2];
			}
		}

		// Restore to original status
		is_pinned[start_particle] = start_pinned;
		is_pinned[end_particle] = end_pinned;

		return number_of_iterations;
	}

	/** Implement [Goldenthal et al., 2007] algorithm 1 */
	private int applyConstraints (double dt) {

		// TODO: Test: Need to think of a way to handle glue, since it may involves particles that are far away on the yarn
		final int		max_size = Integer.MAX_VALUE;
		//final int		max_size = 10;
		final double	strain = 0.00001;
		int				number_of_iterations = 0;
		double[]		pos = positions.clone();

		// Initialize the pos to the positions with unconstrained velocities
		for(int i=0; i<N*3; i++)
			pos[i] += velocities[i] * dt;

		// Solve each yarn individually, and solve each part of the yarn individually
		int	start_segment = 0;
		for(int y=0; y<number_of_yarns; y++) {

			Yarn			yarn = yarns[y];
			final int		number_of_segments = yarn.getNumberOfSegments(),
							number_of_constraints = number_of_segments + glues.length,
							last_segment = start_segment + number_of_segments - 1;
			
			while(start_segment <= last_segment) {
				final int	end_segment = Math.min(last_segment, start_segment+max_size-1);
				// If this part contains only one segment, skip it (since after endpoints are pinned, there is no way to satisfy the constraint)
				if(end_segment - start_segment > 0)
					number_of_iterations += applyPartConstraints(start_segment, end_segment, pos, strain, dt, true);
				start_segment = end_segment + 1;
			}

			// TODO: Debug: Compute total length
			/*double	total_length = 0;
			for(int i=yarn.getFirstParticle(); i<yarn.getLastParticle(); i++) {
				final int	j = i+1;
				total_length += Utils.length(pos[i*3]-pos[j*3], pos[i*3+1]-pos[j*3+1], pos[i*3+2]-pos[j*3+2]);
			}
			System.out.println(total_length);*/
		}

		// All constrained is satisfied by current pos, so we compute the velocities
		// needed to achieve these positions
		for(int i=0; i<N; i++) {
			if(!is_pinned[i]) {
				velocities[i*3]   = (pos[i*3]-positions[i*3]) / dt;
				velocities[i*3+1] = (pos[i*3+1]-positions[i*3+1]) / dt;
				velocities[i*3+2] = (pos[i*3+2]-positions[i*3+2]) / dt;
			}
		}

		return number_of_iterations;
	}

	/** Apply velocity filters */
	public IterationInfo applyVelocityFilters (double dt) {

		// TODO: Debug
		//System.out.println("applyVelocityFilters");

		// Update AABB Tree (This is neede since the shape and the velocities of the yarn will change
		aabb_tree.buildTree(dt);

		//int	number_of_collisions = 0,
		int		collision_iterations = 0;
		boolean	is_modified;

		// Length constraints
		// TODO: How to decide if we need more iterations or not?
		//int	constraints_iterations = applyConstraints(dt);
		int	constraints_iterations = 0;
		/*if(constraints_iterations > 4)
			System.out.println(constraints_iterations);*/
		//is_modified |= constraints_iterations > 0;

		/*for(int i=0; i<3*N; i++)
			if(Double.isNaN(velocities[i]))
				velocities[i] = velocities[i];*/

		do {
			collision_iterations++;
			is_modified = false;

			// Pinned particles
			applyVFPin();
			
			// The floor (at y=0)
			is_modified |= applyVFFloor(dt) > 0;

			// Edge-edge collisions
			is_modified |= applyVFEdge2EdgeCollision(dt, collision_iterations) > 0;

			// TODO: Debug
			//System.out.println("Collision: " + collision_iterations);

		} while(is_modified);

		// TODO: Debug: Show the total lengths of yarns
		/*for(int y=0; y<number_of_yarns; y++) {
			Yarn	yarn = yarns[y];
			double	total_length = 0;
			for(int i=yarn.getFirstParticle(); i<yarn.getLastParticle(); i++) {
				final int	j = i+1;
				total_length += Utils.length(
						positions[i*3]-positions[j*3],
						positions[i*3+1]-positions[j*3+1],
						positions[i*3+2]-positions[j*3+2]);
			}
			System.out.println("Yarn " + y + ": " + total_length);
		}*/

		return new IterationInfo(constraints_iterations, collision_iterations);
	}

	/**
     * Get nearest particle based on given ray
     */
    public ClothPick getNearestParticle (Point3d x, Vector3d n) {

		double	min_d = Double.POSITIVE_INFINITY;
		int		min_i = -1;

		// For every particle, calculate its distance to the ray
		// NOTE: n should be UNIT SIZE
		for(int i=0; i<N; i++) {
			final double	px = positions[i*3],
							py = positions[i*3+1],
							pz = positions[i*3+2];
			Vector3d		dv = new Vector3d();
			dv.cross(new Vector3d(px-x.x, py-x.y, pz-x.z), n);
			final double	d = dv.length();

			if(d < min_d) {
				min_i = i;
				min_d = d;
			}
		}

		if(min_i != -1)
			return new ClothPick(this, min_i, min_d);
		else
			return null;
	}

}

