package be.ibiiztera.competition.billard;

import be.ibiiztera.md.pmatrix.pushmatrix.Point3D;

public class Force {
	public static final double FRACTOBST = 0.01;
	public static final double DISTMIN = 2.5;
	private Bille[] courant;
	public double amortissement = 0.0/1000000;
	public double intensiteAttraction = 1.0/10;
	public double intensiteRepulsion = -1.0/100000;
	public double FPS = 25;
	private double dt = 1.0 / FPS;
	public double limiteNonCollision = 0;
	private Point3D cm;
	private double cmd;
	
	public void configurer(Bille[] courant) {
		this.courant = courant;
		
		
		
	}
	public Point3D centreMasse()
	{
		cm = Point3D.O0;
		cmd = 0.0; 
		if(courant!=null && courant.length > 1)
		{
			for(int i=0; i<courant.length; i++)
			{
				cm = cm.plus(courant[i].position.mult(courant[i].masse));
		
				cmd += courant[i].masse;
			}
			cm = cm.mult(1.0/cmd);
		}
		
		return cm;
	}

	public Point3D attractionRepulsion(Bille p, Bille other) {
		if (p != other)
		{
			double r = other.position.moins(p.position).norme();
			Point3D vu = other.position
					.moins(p.position)
					.norme1(); 
			return vu.mult(intensiteRepulsion
					* p.masse * other.masse
							/ r*r*r) 
									
									
							.plus(
									
									
				   vu.mult(intensiteAttraction
						   * p.masse * other.masse
							/ r*r)
					);
		}
return Point3D.O0;

	}

	public Point3D frottement(Bille p) {
		Point3D fvp =  p.vitesse.mult(p.amortissement * amortissement * -1);
		
		return fvp;
	}

	public Point3D force(Bille[] b, Bille p) {
		Point3D f = Point3D.O0;
		for (int i = 0; i < b.length; i++)
			if(b[i]==p)
				;
			else
				f = f.plus(attractionRepulsion(p, b[i]));
		
		if(p.vitesse.norme()>=1.0)
			f = f.plus(frottement(p));
		
		return f;
	}

	public Point3D acc(Bille[] b, Bille p, Point3D force) {
		return force(b, p).mult(1 / p.masse);
	}

	public void position(Bille b, Point3D acc, double dt) {
		Point3D v2 = b.vitesse.plus(acc.mult(dt));
		
		Point3D p2 = testCollision(b, b.position, b.vitesse.mult(dt));
		
		for(int i=0; i<courant.length; i++)
		{
			if(courant[i]!=b)
			{
				if(Point3D.distance(courant[i].position, b.position)>limiteNonCollision)
				{
					b.vitesse = v2;
					b.position = p2;
				}
			}
		}
	}

	private Point3D testCollision(Bille b, Point3D position, Point3D direction) {
		if(!obstacle(b, position, position.plus(direction)))
			return position.plus(direction);
		else
		{
			return position;
		}
	}

	private boolean obstacle(Bille bille, Point3D a, Point3D b) {
		for(int i=0; i<courant.length; i++)
			for(double a1=0; a1<1.0; a1+=FRACTOBST)
				if(courant[i]!=bille &&
					Point3D.distance(a.plus(b.moins(a).mult(a1)), courant[i].position)<DISTMIN)
					return false;
		return false;
	}

	public void calculer() {
		Bille[] next = new Bille[courant.length];

		for (int i = 0; i < courant.length; i++) {
			next[i] = courant[i];

			position(next[i],
					acc(courant, courant[i], force(courant, courant[i])), dt);
		}

		//System.err.println("+" + courant.length);
	}
}

