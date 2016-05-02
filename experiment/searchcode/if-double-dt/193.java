package cs5643.particles;

import java.util.*;
import javax.vecmath.*;
import javax.media.opengl.*;

/**
 *
 * @author Artoo
 */
public class Grid {
	double	x_grid_size, y_grid_size;
	int		x_number, y_number;

	Cell[]	cells;

	public int index (int x, int y) {
		return x + y * x_number;
	}

	public Grid (int x_number, int y_number) {
		this.x_grid_size = 1.0 / (x_number-1);
		this.y_grid_size = 1.0 / (y_number-1);
		this.x_number = x_number;
		this.y_number = y_number;

		cells = new Cell[x_number * y_number];
		for(int x=0; x<x_number; x++) {
			for(int y=0; y<y_number; y++) {
				cells[index(x,y)] = new Cell(x, y);
				cells[index(x,y)].min_pt.set(-x_grid_size/2 + x*x_grid_size, -y_grid_size/2 + y*y_grid_size);
				cells[index(x,y)].max_pt.set(-x_grid_size/2 + (x+1)*x_grid_size, -y_grid_size/2 + (y+1)*y_grid_size);
			}
		}
	}

	public void clear () {
		for(int i=0; i<cells.length; i++) {
			cells[i].particles.clear();
			cells[i].edges.clear();
		}
	}

	public void update (ArrayList<Particle> P, ArrayList<Force> F, double h, double dt) {
		clear();
		for(Particle p : P)
			particleSpaceTimeSpan(p, dt);
		for(Force f : F) {
			if(f instanceof SpringForce2Particle)
				edgeSpaceTimeSpan((SpringForce2Particle)f, h, dt);
		}
	}

	public void edgeSpaceTimeSpan (SpringForce2Particle e, double h, double dt) {
		double	x1 = e.p1.x.x,
				y1 = e.p1.x.y,
				x1p = e.p1.x.x + e.p1.v.x * dt,
				y1p = e.p1.x.y + e.p1.v.y * dt,
				x2 = e.p2.x.x,
				y2 = e.p2.x.y,
				x2p = e.p2.x.x + e.p2.v.x * dt,
				y2p = e.p2.x.y + e.p2.v.y * dt,
				min_x = Math.min(x1, Math.min(x1p, Math.min(x2, x2p))) - h,
				max_x = Math.max(x1, Math.max(x1p, Math.max(x2, x2p))) + h,
				min_y = Math.min(y1, Math.min(y1p, Math.min(y2, y2p))) - h,
				max_y = Math.max(y1, Math.max(y1p, Math.max(y2, y2p))) + h;
		Point2i	i1 = new Point2i(),
				i2 = new Point2i();
		ptInCellIndex(min_x, min_y, i1);
		ptInCellIndex(max_x, max_y, i2);
		// Edge e belongs to [i1.x,i1.y] -> [i2.x,i2.y]
		try {
		for(int x=i1.x; x<=i2.x; x++)
			for(int y=i1.y; y<=i2.y; y++)
				cells[index(x,y)].edges.add(e);
		} catch(Exception exp) {
			exp = exp;
		}
	}

	public void particleSpaceTimeSpan (Particle p, double dt) {
		double	x1 = p.x.x,
				y1 = p.x.y,
				x2 = p.x.x + p.v.x * dt,
				y2 = p.x.y + p.v.y * dt,
				t;
		if(x1 > x2) {
			t = x1;
			x1 = x2;
			x2 = t;
		}
		if(y1 > y2) {
			t = y1;
			y1 = y2;
			y2 = t;
		}

		// Now x1 <= x2, y1 <= y2
		Point2i	i1 = new Point2i(),
				i2 = new Point2i();
		ptInCellIndex(x1, y1, i1);
		ptInCellIndex(x2, y2, i2);

		// Particle p belongs to [i1.x,i1.y] -> [i2.x,i2.y]
		for(int x=i1.x; x<=i2.x; x++)
			for(int y=i1.y; y<=i2.y; y++)
				cells[index(x,y)].particles.add(p);
	}

	public void ptInCellIndex (double x, double y, Point2i index) {
		index.set(
				(int)Math.floor(x / x_grid_size + 0.5),
				(int)Math.floor(y / y_grid_size + 0.5));
	}

	public void getParticleCells (Particle p, double dt, ArrayList<Cell> cells) {
		// This is basically the same code as particleSpaceTimeSpan
		double	x1 = p.x.x,
				y1 = p.x.y,
				x2 = p.x.x + p.v.x * dt,
				y2 = p.x.y + p.v.y * dt,
				t;
		if(x1 > x2) {
			t = x1;
			x1 = x2;
			x2 = t;
		}
		if(y1 > y2) {
			t = y1;
			y1 = y2;
			y2 = t;
		}

		// Now x1 <= x2, y1 <= y2
		Point2i	i1 = new Point2i(),
				i2 = new Point2i();
		ptInCellIndex(x1, y1, i1);
		ptInCellIndex(x2, y2, i2);

		// Particle p belongs to [i1.x,i1.y] -> [i2.x,i2.y]
		cells.clear();
		for(int x=i1.x; x<=i2.x; x++)
			for(int y=i1.y; y<=i2.y; y++)
				cells.add(this.cells[index(x,y)]);
	}

	public void display (GL gl) {
		float[]	old_lw = new float[1];
		gl.glGetFloatv(GL.GL_LINE_WIDTH, old_lw, 0);
		gl.glLineWidth(1.0f);

		for(int i=0; i<cells.length; i++) {
			Cell	cell = cells[i];
			gl.glBegin(GL.GL_LINE_STRIP);
			gl.glColor3f(cell.particles.size()/5.0f, cell.edges.size()/5.0f, 0.0f);
			gl.glVertex2d(cell.min_pt.x, cell.min_pt.y);
			gl.glVertex2d(cell.max_pt.x, cell.min_pt.y);
			gl.glVertex2d(cell.max_pt.x, cell.max_pt.y);
			gl.glVertex2d(cell.min_pt.x, cell.max_pt.y);
			gl.glVertex2d(cell.min_pt.x, cell.min_pt.y);
			gl.glEnd();
		}

		gl.glLineWidth(old_lw[0]);
	}

	public class Cell {
		Point2d				min_pt = new Point2d(),
							max_pt = new Point2d();
		int					x, y;
		ArrayList<Particle>				particles = new ArrayList<Particle>();
		ArrayList<SpringForce2Particle>	edges = new ArrayList<SpringForce2Particle>();

		Cell (int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
}
