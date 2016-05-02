package cs5643.particles;

import java.awt.Font;
import java.util.*;
import javax.vecmath.*;
import javax.media.opengl.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import com.sun.opengl.util.j2d.*;
import java.awt.Color;
import java.io.IOException;

/**
 * Maintains dynamic lists of Particle and Force objects, and provides
 * access to their state for numerical integration of dynamics.
 * <pre>
 * Symplectic-Euler integrator is hard-coded, with embedded collision
 * processing code.
 * </pre>
 * 
 * @author Doug James, January 2007 (revised Feb 2009)
 */
public class ParticleSystem //implements Serializable
{
	/** Next valid ID for particle */
	private static int	NEXT_PARTICLE_ID = 0;

    /** Current simulation time. */
    double time = 0;

    /** List of Particle objects. */
    ArrayList<Particle>   P = new ArrayList<Particle>();

    /** List of Force objects. */
    ArrayList<Force>      F = new ArrayList<Force>();

	/** Quadratic function solver */
	private QuadraticFuncSolver	quad_solver = new QuadraticFuncSolver();

	/** Uniform subdivision */
	public Grid			grid = new Grid(50, 50);
	public FluidGrid	fgrid = new FluidGrid(50, 50);	// FluidForce needs coaser grid

	/** For debugging: number of iterations in collision test */
	private int	number_of_iterations;

	/** For debugging: To record the collision forces applied on particles */
	/*private HashMap<Particle, Vector2d>	collision_forces = new HashMap<Particle, Vector2d>(),
										repulsion_forces = new HashMap<Particle, Vector2d>(),
										debug_vector = new HashMap<Particle, Vector2d>();*/

	/** For debugging: Render text */
	private TextRenderer textRenderer = new TextRenderer(new Font("Monospaced", Font.BOLD, 32), true, true);

	/** Particle generators */
	ArrayList<ParticleGenerator>	generators = new ArrayList<ParticleGenerator>();

	/** Not used */
    //ArrayList<Filter>		filters = new ArrayList<Filter>();

	/** For debugging */
	//private FileWriter		debug_log_file;
	//private BufferedWriter	debug_log;

    /** Basic constructor. */
    public ParticleSystem() {

		/// BUILD BOX
		double we = 0.0, he = 0.0;
		Particle p00 = createParticle(new Point2d(  we,  he), false);  p00.setPin(true);
		Particle p01 = createParticle(new Point2d(  we,1-he), false);  p01.setPin(true);
		Particle p11 = createParticle(new Point2d(1-we,1-he), false);  p11.setPin(true);
		Particle p10 = createParticle(new Point2d(1-we,  he), false);  p10.setPin(true);
		addForce(new SpringForce2Particle(p00, p01, Constants.STIFFNESS_STRETCH, this));
		addForce(new SpringForce2Particle(p00, p10, Constants.STIFFNESS_STRETCH, this));
		addForce(new SpringForce2Particle(p11, p10, Constants.STIFFNESS_STRETCH, this));
		addForce(new SpringForce2Particle(p11, p01, Constants.STIFFNESS_STRETCH, this));

		addForce(new FluidForce(this));

		//generators.add(new BubbleGenerator(5.0, new Point2d(0.1, 0.9), 0, -90, 20, true, this));

		// For debugging
		/*filters.add(new PlaneCollisionFilter(new Vector2d(0.0, 1.0), new Point2d(0.0, 0.0), this));
		filters.add(new PlaneCollisionFilter(new Vector2d(0.0, -1.0), new Point2d(1.0, 1.0), this));
		filters.add(new PlaneCollisionFilter(new Vector2d(1.0, 0.0), new Point2d(0.0, 0.0), this));
		filters.add(new PlaneCollisionFilter(new Vector2d(-1.0, 0.0), new Point2d(1.0, 1.0), this));*/
	}

    /** Adds a force object (until removed) */
    public synchronized void addForce(Force f) {
	F.add(f);
    }

    /** Useful for removing temporary forces, such as user-interaction
     * spring forces. */
    public synchronized void removeForce(Force f) {
	F.remove(f);
    }

    /** Creates particle and adds it to the particle system. 
     * @param	p0		Undeformed/material position.
	 * @param	fluid	Whether this particle is fluid
     * @return	Reference to new Particle.
     */
    public synchronized Particle createParticle(Point2d p0, boolean fluid) {
		return createParticle(p0, fluid, new Vector2d(0, 0), Constants.PARTICLE_MASS);
	}

	/** Creates particle and adds it to the particle system.
     * @param	p0			Undeformed/material position.
	 * @param	fluid		Whether this particle is fluid
	 * @param	velocity	The initial velocity of the new particle
     * @return	Reference to new Particle.
     */
	public synchronized Particle createParticle(Point2d p0, boolean fluid, Vector2d velocity) {
		return createParticle(p0, fluid, velocity, Constants.PARTICLE_MASS);
	}

	/** Creates particle and adds it to the particle system.
     * @param	p0			Undeformed/material position.
	 * @param	fluid		Whether this particle is fluid
	 * @param	velocity	The initial velocity of the new particle
	 * @param	mass		The mass of the new particle
     * @return	Reference to new Particle.
     */
	public synchronized Particle createParticle(Point2d p0, boolean fluid, Vector2d velocity, double mass) {
		Particle newP = new Particle(p0, NEXT_PARTICLE_ID++, fluid, velocity, mass);
		P.add(newP);
		return newP;
    }


    /** Removes particle and any attached forces from the ParticleSystem.
     * @param p Particle
     */
    public void removeParticle(Particle p) 
    {
	P.remove(p);
	
	ArrayList<Force> removalList = new ArrayList<Force>();
	for(Force f : F) {/// REMOVE f IF p IS USED IN FORCE
	    if(f.contains(p))  removalList.add(f);
	}

	F.removeAll(removalList);
    }

    /** 
     * Helper-function that computes nearest particle to the specified
     * (deformed) position.
     * @return Nearest particle, or null if no particles. 
     */
    public synchronized Particle getNearestParticle(Point2d x)
    {
	Particle minP      = null;
	double   minDistSq = Double.MAX_VALUE;
	for(Particle particle : P) {
	    double distSq = x.distanceSquared(particle.x);
	    if(distSq < minDistSq) {
		minDistSq = distSq;
		minP = particle;
	    }
	}
	return minP;
    }

    /** 
     * Helper-function that computes nearest particle to the specified
     * (deformed) position.
     * @return Nearest particle, or null if no particles. 
     * @param pinned If true, returns pinned particles, and if false, returns unpinned
     */
    public synchronized Particle getNearestPinnedParticle(Point2d x, boolean pinned)
    {
	Particle minP      = null;
	double   minDistSq = Double.MAX_VALUE;
	for(Particle particle : P) {
	    if(particle.isPinned() == pinned) {
		double distSq = x.distanceSquared(particle.x);
		if(distSq < minDistSq) {
		    minDistSq = distSq;
		    minP = particle;
		}
	    }
	}
	return minP;
    }

    /** Moves all particles to undeformed/materials positions, and
     * sets all velocities to zero. Synchronized to avoid problems
     * with simultaneous calls to advanceTime(). */
    public synchronized void reset()
    {
	for(Particle p : P)  {
	    p.x.set(p.x0);
	    p.v.set(p.v0);
	    p.f.set(0,0);
	    p.setHighlight(false);
	}

	/// WORKAROUND FOR DANGLING MOUSE-SPRING FORCES AFTER PS-INTERNAL RESETS:
	ArrayList<Force> removeF = new ArrayList<Force>();
	for(Force f : F) {
	    if(f instanceof SpringForce1Particle) removeF.add(f);
	}
	F.removeAll(removeF);

	time = 0;
    }

    /**
     * TESTS FOR SPRING-SPRING OVERLAP. 
     * <pre>
     * ################# 
     * DO NOT MODIFY!
     * ################# 
     * </pre>
     * @return True if there are overlaps between any unique
     * not-both-pinned springs without shared particles. 
     */
    public boolean hasOverlappingSprings()
    {
	for(Force f1 : F) { 
	    if(f1 instanceof SpringForce2Particle) {
		SpringForce2Particle s1 = (SpringForce2Particle)f1;

		for(Force f2 : F) { 
		    if(f2 instanceof SpringForce2Particle) {
			SpringForce2Particle s2 = (SpringForce2Particle)f2;

			if(s1.overlaps(s2)) {
			    if(s1.p1.isPinned() && s1.p2.isPinned() && 
			       s1.p1.isPinned() && s1.p2.isPinned()) {
				/// IGNORE
			    }
			    else {
					// TODO: Debug
					System.err.println(String.format("Edge (%d,%d) and (%d,%d) are overlapped", s1.p1.id, s1.p2.id, s2.p1.id, s2.p2.id));
					return true;
				}
			}
		    }
		}
	    }
	}
	return false;
    }

	/** Check if edge p1-p2 overlaps any spring */
	public boolean isOverlappingSprings (Point2d p1, Point2d p2) {
		for(Force f : F) {
			if(f instanceof SpringForce2Particle) {
				SpringForce2Particle s = (SpringForce2Particle)f;
				if(SpringForce2Particle.overlaps(s.p1.x, s.p2.x, p1, p2))
					return true;
			}
		}
		return false;
	}

    /**
     * MAIN FUNCTION TO IMPLEMENT YOUR ROBUST COLLISION PROCESSING ALGORITHM.
     */
    public synchronized void advanceTime(double dt)
    {
		long t0 = -System.nanoTime();

		// Update FluidGrid for FluidForce
		fgrid.clear();
		fgrid.update(P);
	
		{/// GATHER BASIC FORCES (NO NEED TO MODIFY):
	
		    /// CLEAR FORCE ACCUMULATORS:
		    for(Particle p : P)  p.f.set(0,0);
	
		    /// APPLY FORCES:
			for(Force force : F)
				force.applyForce();
	
	 	    // GRAVITY:
	 	    for(Particle p : P)   p.f.y -= p.m * 10.f;
	
		    // ADD SOME MASS-PROPORTIONAL DAMPING (DEFAULT IS ZERO)
		    for(Particle p : P) 
			Utils.acc(p.f,  -Constants.DAMPING_MASS * p.m, p.v);
		}
	
		/// PENALTY FORCES (LAST!):
		{
		    // Penalty force is applied after the velocity is calculated
		}

		// Generates particles
		// Do generating here to let filters filter them before computing any force
		for(ParticleGenerator g : generators)
			g.generateParticle(dt);
	
		///////////////////////////////////////////////
		/// SYMPLECTIC-EULER TIME-STEP w/ COLLISIONS:
		///////////////////////////////////////////////
		///////////////////////////////////////////////
		/// 1. UPDATE PREDICTOR VELOCITY WITH FORCES
		///////////////////////////////////////////////
		for(Particle p : P) {
		    /// APPLY PIN CONSTRAINTS (set p=p0, and zero out v):
		    if(p.isPinned()) {
			p.v.set(0,0);
		    }
		    else {
			p.v.scaleAdd(dt/p.m, p.f, p.v); // v += dt * f/m;
		    }
	
		    /// CLEAR FORCE ACCUMULATOR
		    p.f.set(0,0);
		}

		// For debugging
		/*for(Filter f : filters)
			f.apply(dt);*/
	
		//////////////////////////////////////////////////
		/// 2. RESOLVE REMAINING COLLISIONS USING IMPULSES
		//////////////////////////////////////////////////
		// Thickness of spaghetti
		final double			h = 0.01, mu = 0.5;
		ArrayList<Grid.Cell>	cells = new ArrayList<Grid.Cell>();
		// We need this because each edge may occupy multiple cells, but should be checked only once
		HashSet<SpringForce2Particle>	used_edges = new HashSet<SpringForce2Particle>();
		// Update Grid
		grid.clear();
		grid.update(P, F, h, dt);
		// (1) Apply penalty forces
		{
			// For debugging
			// Clear repulsion forces acting on particles
			/*repulsion_forces.clear();
			for(Particle p : P) {
				repulsion_forces.put(p, new Vector2d(0, 0));
			}*/

			for(Particle p : P) {
				used_edges.clear();
				grid.getParticleCells(p, dt, cells);
				for(Grid.Cell cell : cells) {
					for(SpringForce2Particle edge : cell.edges) {
						// If the particle p is on the edge(force), or
						// this edge is already tested, skip the edge
						if(edge.contains(p) || used_edges.contains(edge))
							continue;

						used_edges.add(edge);

						Particle	q = edge.p1, r = edge.p2;

						// Calculate the normal (let it point to p)
						Vector2d	n = new Vector2d(q.x.y-r.x.y, r.x.x-q.x.x),
									p_sub_q = new Vector2d(p.x.x-q.x.x, p.x.y-q.x.y);
						n.normalize();
						if(n.dot(p_sub_q) < 0)
							n.negate();

						// Calculate the penetration depth
						double	d = h - n.dot(p_sub_q);

						if(d > 0) {
							// Calculate the projection
							Vector2d		r_sub_q = new Vector2d(r.x.x-q.x.x, r.x.y-q.x.y);
							double			beta = r_sub_q.dot(p_sub_q)/r_sub_q.lengthSquared(),
											beta_bar = 1 - beta;

							if(beta >= 0 && beta <= 1) {
								Vector2d		c_dot = new Vector2d(beta_bar*q.v.x+beta*r.v.x, beta_bar*q.v.y+beta*r.v.y),
												v = new Vector2d(p.v.x-c_dot.x, p.v.y-c_dot.y);

								// Normal speed: If vn >= 0, they are separating; otherwise, they're moving closer
								Vector2d	v_n = new Vector2d(),
											v_t = new Vector2d(),
											v_t_normalized = new Vector2d();
								v_n.scale(v.dot(n), n);
								v_t.sub(v, v_n);
								v_t_normalized.normalize(v_t);
								double	vn = v_n.length(),
										vt = v_t.length(),
										v0 = 0.1 * d / dt;

								// Apply penalty force if the separating speed is not enough
								if(vn < v0) {
									double	impulse = Math.min(dt * Constants.STIFFNESS_STRETCH * d, p.m * (v0 - vn));

									// Calculate each point's new velocity
									if (!p.isPinned()) {
										p.v.scaleAdd(impulse / p.m, n, p.v);
										// Calculate friction if there is tangential velocity
										// When particle is fluid, assume no friction
										if(vt > 0 && !p.isFluid())
											p.v.scaleAdd(Math.max(1-mu*(impulse / p.m)/vt, 0)-1, v_t, p.v);

										// For debugging
										//repulsion_forces.get(p).scaleAdd(impulse/p.m, n, repulsion_forces.get(p));
									}
									if (!q.isPinned()) {
										q.v.scaleAdd(-beta_bar * impulse / q.m, n, q.v);
										// Calculate friction if there is tangential velocity
										if(vt > 0)
											q.v.scaleAdd(1-Math.max(1-mu*(beta_bar * impulse / q.m)/vt, 0), v_t, q.v);

										// For debugging
										//repulsion_forces.get(q).scaleAdd(-beta_bar * impulse / q.m, n, repulsion_forces.get(q));
									}
									if (!r.isPinned()) {
										r.v.scaleAdd(-beta * impulse / r.m, n, r.v);
										// Calculate friction if there is tangential velocity
										if(vt > 0)
											r.v.scaleAdd(1-Math.max(1-mu*(beta * impulse / r.m)/vt, 0), v_t, r.v);

										// For debugging
										//repulsion_forces.get(r).scaleAdd(-beta * impulse / r.m, n, repulsion_forces.get(r));
									}
								} // if
							} // if
						} // if
					} // for edge
				} // for cell
			} // for particle
		}
		// (2) Resolve collision
		{
			boolean	has_collision;
			number_of_iterations = 0;

			// For debugging
			// Clear collision forces acting on particles
			/*collision_forces.clear();
			for(Particle p : P) {
				collision_forces.put(p, new Vector2d(0, 0));
			}*/

			do {
				has_collision = false;
				number_of_iterations++;
				
				// Update Grid before each iteration, since repulsion forces and collisions change velocities
				grid.clear();
				grid.update(P, F, h, dt);

				for(Particle p : P) {
					used_edges.clear();
					grid.getParticleCells(p, dt, cells);
					for(Grid.Cell cell : cells) {
						for (SpringForce2Particle edge : cell.edges) {
							// If the particle p is on the edge(force), or
							// this edge is already tested, skip the edge
							if (edge.contains(p) || used_edges.contains(edge))
								continue;

							used_edges.add(edge);

							Particle q = edge.p1, r = edge.p2;

							// Simple boundary test
							if(!spaceTimeBoundaryTest(p, q, r, dt))
								continue;

							Vector2d r_sub_q = new Vector2d(r.x),
									r_sub_q_dot = new Vector2d(r.v),
									p_sub_q = new Vector2d(p.x),
									p_sub_q_dot = new Vector2d(p.v);
							r_sub_q.sub(q.x);
							p_sub_q.sub(q.x);
							r_sub_q_dot.sub(q.v);
							p_sub_q_dot.sub(q.v);
							double A = determinant(r_sub_q_dot, p_sub_q_dot),
									B = determinant(r_sub_q_dot, p_sub_q) + determinant(r_sub_q, p_sub_q_dot),
									C = determinant(r_sub_q, p_sub_q);
							boolean has_root = quad_solver.solve(A, B, C);

							if (has_root) {
								double t_star;	// The collision time
								double[] roots = quad_solver.getRoots();
								double alpha = -1;
								int index = -1;
								Point2d q_star = new Point2d(), // The position of points when colliding
										r_star = new Point2d(),
										p_star = new Point2d();

								for (int i = 0; i < 2; i++) {
									t_star = roots[i];
									q_star.scaleAdd(t_star, q.v, q.x);
									r_star.scaleAdd(t_star, r.v, r.x);
									p_star.scaleAdd(t_star, p.v, p.x);

									alpha = lineTest(p_star, q_star, r_star);
									// TODO: Remove this
									final double threshold = 0;
									if (t_star > 0 && t_star <= dt && alpha >= -threshold && alpha <= 1 + threshold) {
										index = i;
										break;
									}
								}

								if (index != -1) {
									// Calculate normal
									double		beta = alpha,
												beta_bar = 1 - beta;
									Vector2d	q_star_dot = q.v, // The velocity is not changed during (0,t_star]
												r_star_dot = r.v,
												p_star_dot = p.v;

									Point2d		c = p_star;
									Vector2d	c_dot = new Vector2d(beta_bar * q_star_dot.x + beta * r_star_dot.x, beta_bar * q_star_dot.y + beta * r_star_dot.y),
												v = new Vector2d();
									v.sub(p_star_dot, c_dot);

									// Note: normal n needs to be normalized
									Vector2d n = new Vector2d(q_star.y - r_star.y, r_star.x - q_star.x);
									n.normalize();
									double vn = v.dot(n);
									// Make vn < 0
									if (vn > 0) {
										n.negate();
										vn = -vn;
									}

									// The magnitude of vn can't be too small, or the collision force will be nearly zero
									// and we'll need a lot of iterations to split them apart
									final double vn_th = -1e-1;
									vn = Math.min(vn, vn_th);

									has_collision = true;

									double epsilon = 0.01;
									double iw_p = p.isPinned() ? 0 : 1 / p.m,
											iw_q = q.isPinned() ? 0 : 1 / q.m,
											iw_r = r.isPinned() ? 0 : 1 / r.m;
									double impulse = (1 + epsilon) * (-vn) / (iw_p + beta_bar * beta_bar * iw_q + beta * beta * iw_r);

									// Calculate each point's new velocity
									if (!p.isPinned()) {
										p.v.scaleAdd(impulse / p.m, n, p.v);
										// For debugging
										//collision_forces.get(p).scaleAdd(impulse / p.m, n, collision_forces.get(p));
									}
									if (!q.isPinned()) {
										q.v.scaleAdd(-beta_bar * impulse / q.m, n, q.v);
										// For debugging
										//collision_forces.get(q).scaleAdd(-beta_bar * impulse / q.m, n, collision_forces.get(q));
									}
									if (!r.isPinned()) {
										r.v.scaleAdd(-beta * impulse / r.m, n, r.v);
										// For debugging
										//collision_forces.get(r).scaleAdd(-beta * impulse / r.m, n, collision_forces.get(r));
									}
								} // if
							} // if
						} // for edge
					} // for cell
				} // for particle
			} while(has_collision);	// Repeat until no collision
		}

		//////////////////////////////////////////////////////////
		/// 3. ADVANCE POSITIONS USING COLLISION-FEASIBLE VELOCITY
		//////////////////////////////////////////////////////////
		for(Particle p : P) {
		    p.x.scaleAdd(dt, p.v, p.x); //p.x += dt * p.v;
		}
	
		time += dt;
	
		t0 += System.nanoTime();
    }

    /**
     * Displays Particle and Force objects.
     */
    public synchronized void display(GL gl) 
    {
		for(Force force : F) {
			force.display(gl);
		}

		for(Particle particle : P) {
			particle.display(gl);
		}

		// Generators
		for(ParticleGenerator g : generators)
			g.display(gl);

		// Following code are for debugging
		// Show Grid
		//grid.display(gl);

		// Show collision forces
		/*gl.glColor3f(1.0f, 0.0f, 0.0f);
		for(Particle p : collision_forces.keySet()) {
			Vector2d	acc = collision_forces.get(p);
			Vector2d	tip = new Vector2d(p.x);
			tip.scaleAdd(0.2, acc, tip);

			gl.glBegin(GL.GL_LINES);
			gl.glVertex2d(p.x.x, p.x.y);
			gl.glVertex2d(tip.x, tip.y);
			gl.glEnd();

			textRenderer.begin3DRendering();
			textRenderer.setColor(1.0f, 0.0f, 0.0f, 1.0f);
			textRenderer.draw3D(String.format("%d", p.id), (float)tip.x, (float)tip.y, 0.0f, 0.001f);
			textRenderer.end3DRendering();
		}*/
		/*gl.glColor3f(1.0f, 0.0f, 0.0f);
		for(Particle p : repulsion_forces.keySet()) {
			Vector2d	acc = repulsion_forces.get(p);
			Vector2d	tip = new Vector2d(p.x);
			tip.scaleAdd(0.2, acc, tip);

			gl.glBegin(GL.GL_LINES);
			gl.glVertex2d(p.x.x, p.x.y);
			gl.glVertex2d(tip.x, tip.y);
			gl.glEnd();

			textRenderer.begin3DRendering();
			textRenderer.setColor(0.0f, 1.0f, 0.0f, 1.0f);
			textRenderer.draw3D(String.format("%d", p.id), (float)tip.x, (float)tip.y, 0.0f, 0.001f);
			textRenderer.end3DRendering();
		}*/
    }

	/**
	 * Clear all particles and forces
	 */
	public synchronized void clear () {
		P.clear();
		F.clear();
		time = 0;
		NEXT_PARTICLE_ID = 0;
    }

	private double lineTest (Tuple2d p, Tuple2d q, Tuple2d r) {
		Vector2d	r_sub_q = new Vector2d(r),
					p_sub_q = new Vector2d(p);
		r_sub_q.sub(q);
		p_sub_q.sub(q);
		double	alpha = (r_sub_q.dot(p_sub_q) / r_sub_q.lengthSquared());

		return alpha;
	}

	private double determinant (Tuple2d a, Tuple2d b) {
		return a.x * b.y - a.y * b.x;
	}

	public synchronized void saveConfiguration (String filename) {
		FileWriter	outputStream = null;

		try {
			try {
				outputStream = new FileWriter(filename);

				outputStream.write(String.format("%d\r\n", P.size()));
				for(Particle particle : P) {
					outputStream.write(String.format("%d %f %f %f %b %b\r\n",
							particle.id,
							particle.x.x, particle.x.y,
							particle.m,
							particle.isPinned(),
							particle.isFluid()));
				}
				outputStream.write(String.format("%d\r\n", F.size()));
				for(Force force : F) {
					outputStream.write(force.outputToText());
					outputStream.write("\r\n");
				}
			} finally {
				if(outputStream != null)
					outputStream.close();
			}
		} catch (IOException e) {
			System.out.println("Save configuration file failed: " + e.toString());
		}
	}

	public synchronized void loadConfiguration (String filename) {
		Scanner	inputStream = null;

		try {
			try {
				inputStream = new Scanner(new BufferedReader(new FileReader(filename)));

				// Clear particles and forces
				clear();

				HashMap	particle_map = new HashMap();
				long	number_of_particles = inputStream.nextLong();
				long	max_id = -1;
				for(long i=0; i<number_of_particles; i++) {
					long	id = inputStream.nextLong();
					double	x0 = inputStream.nextDouble(), y0 = inputStream.nextDouble(), m = inputStream.nextDouble();
					boolean	is_pinned = inputStream.nextBoolean(),
							is_fluid = inputStream.nextBoolean();
					Particle	particle = createParticle(new Point2d(x0, y0), is_fluid, new Vector2d(0, 0), m);
					particle.id = (int)id;
					if(id > max_id)
						max_id = id;
					particle.setPin(is_pinned);
					particle_map.put(id, particle);
				}

				long	number_of_forces = inputStream.nextLong();
				for(long i=0; i<number_of_forces; i++) {
					String	force_name = inputStream.next("\\S+");
					if(force_name.equals("SpringForce2Particle")) {
						long	id1 = inputStream.nextLong(), id2 = inputStream.nextLong();
						double	stiffness = inputStream.nextDouble();
						SpringForce2Particle	newForce = new SpringForce2Particle((Particle)particle_map.get(id1), (Particle)particle_map.get(id2), stiffness, this);
						addForce(newForce);
					} else if(force_name.equals("SpringForceBending")) {
						long	id0 = inputStream.nextLong(), id1 = inputStream.nextLong(), id2 = inputStream.nextLong();
						double	stiffness = inputStream.nextDouble();
						SpringForceBending	newForce = new SpringForceBending((Particle)particle_map.get(id0), (Particle)particle_map.get(id1), (Particle)particle_map.get(id2), stiffness, this);
						addForce(newForce);
					} else if(force_name.equals("FluidForce")) {
						addForce(new FluidForce(this));
					} else
						System.out.println("Unrecognized force: " + force_name);
				}
				
				NEXT_PARTICLE_ID = (int)max_id + 1;

			} finally {
				if(inputStream != null)
					inputStream.close();
			}
		} catch (IOException e) {
			System.out.println("Load configuration file failed: " + e.toString());
		}
	}

	/** Return the number of particles */
	public int size () {
		return P.size();
	}

	/** Return the number of iteration of collision test in last advanceTime() call */
	public int getNumberOfIterations () {
		return number_of_iterations;
	}

	/** Space-time boundary test */
	private boolean spaceTimeBoundaryTest (Particle p, Particle q, Particle r, double dt) {
		if(Math.max(p.x.x, p.x.x+p.v.x*dt) < Math.min(Math.min(Math.min(q.x.x, q.x.x+q.v.x*dt), r.x.x), r.x.x+r.v.x*dt) ||
				Math.min(p.x.x, p.x.x+p.v.x*dt) > Math.max(Math.max(Math.max(q.x.x, q.x.x+q.v.x*dt), r.x.x), r.x.x+r.v.x*dt) ||
				Math.max(p.x.y, p.x.y+p.v.y*dt) < Math.min(Math.min(Math.min(q.x.y, q.x.y+q.v.y*dt), r.x.y), r.x.y+r.v.y*dt) ||
				Math.min(p.x.y, p.x.y+p.v.y*dt) > Math.max(Math.max(Math.max(q.x.y, q.x.y+q.v.y*dt), r.x.y), r.x.y+r.v.y*dt))
			return false;
		return true;
	}

    /**
     * Solve quadratic function 
     */
	private class QuadraticFuncSolver {
		// roots[0] <= roots[1]
		private double[]	roots = new double[2];

		public boolean solve (double A, double B, double C) {
			// Normalize A,B,C so that the largest coefficient is 1
			// (see [Heath, p26])
			double	max = Math.max(Math.max(A, B), C);
			if(max == 0)
				return false;	// No root
			A /= max;
			B /= max;
			C /= max;

			double	D = B*B - 4*A*C;
			// TODO: triggered when A is small?
			if(A == 0) {
				if(B == 0)
					return false;
				else
					roots[0] = roots[1] = -C/B;
			} else if(B == 0) {
				double	double_root = -C/A;
				if(double_root < 0)
					return false;
				else {
					double_root = Math.sqrt(double_root);
					roots[0] = -double_root;
					roots[1] = double_root;
				}
			} else if(D < 0) {
				return false;
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
			}
			return true;
		}

		public double getMinRoot () { return roots[0]; }
		public double getMaxRoot () { return roots[1]; }
		public double[] getRoots () { return roots; }
	}
    
}

