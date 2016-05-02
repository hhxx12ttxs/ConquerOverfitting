package cs5643.finalproj;

import cs5643.finalproj.knitted.*;
import java.util.*;
import javax.vecmath.*;
import javax.media.opengl.*;

/**
 *
 * @author Artoo
 */
public class Grid {
	/** The min point and max point of the grid */
	private Point3d	grid_min_pt, grid_max_pt;

	/** The size of the grid */
	private double	x_size, y_size, z_size;

	/** The size of each cell */
	private double	x_cell_size, y_cell_size, z_cell_size;

	/** The number of cells in each dimension
	 * ?_size = ?_cell_size * ?_number */
	private int		x_number, y_number, z_number;

	/** Cells to store the segments inside the grid */
	Cell[]	cells;
	
	/** Cell to store the segments outside the grid */
	Cell	outside;

	public int index (int x, int y, int z) {
		return x + y * x_number + z * x_number * y_number;
	}

	public Grid (Point3d min_pt, Point3d max_pt, Tuple3i number_of_cells) {
		this.grid_min_pt	= min_pt;
		this.grid_max_pt	= max_pt;
		this.x_size			= max_pt.x - min_pt.x;
		this.y_size			= max_pt.y - min_pt.y;
		this.z_size			= max_pt.z - min_pt.z;
		this.x_number		= number_of_cells.x;
		this.y_number		= number_of_cells.y;
		this.z_number		= number_of_cells.z;
		this.x_cell_size	= this.x_size / this.x_number;
		this.y_cell_size	= this.y_size / this.y_number;
		this.z_cell_size	= this.z_size / this.z_number;

		cells = new Cell[x_number * y_number * z_number];
		for(int x=0; x<x_number; x++) {
			for(int y=0; y<y_number; y++) {
				for(int z=0; z<z_number; z++) {
					cells[index(x,y,z)] = new Cell(x, y, z,
							new Point3d(min_pt.x + x*x_cell_size, min_pt.y + y*y_cell_size, min_pt.z + z*z_cell_size),
							new Point3d(min_pt.x + (x+1)*x_cell_size, min_pt.y + (y+1)*y_cell_size, min_pt.z + (z+1)*z_cell_size));
				}
			}
		}
		outside = new Cell(-1, -1, -1, null, null);
	}

	public void clear () {
		for(int i=0; i<cells.length; i++) {
			cells[i].segments.clear();
		}
	}

	public void update (Cloth cloth, double h, double dt) {
		clear();

		int			number_of_segments = cloth.getNumberOfSegments();
		int[][]		segments = cloth.getSegments();
		double[]	positions = cloth.getPositions(),
					velocities = cloth.getVelocities(),
					segment_radii = cloth.getSegmentRadii();
		Point3d		xi = new Point3d(),
					xj = new Point3d();
		Vector3d	vi = new Vector3d(),
					vj = new Vector3d();

		for(int k=0; k<number_of_segments; k++) {
			final int	i = segments[k][0],
						j = segments[k][1];
			xi.set(positions[i*3], positions[i*3+1], positions[i*3+2]);
			xj.set(positions[j*3], positions[j*3+1], positions[j*3+2]);
			vi.set(velocities[i*3], velocities[i*3+1], velocities[i*3+2]);
			vj.set(velocities[j*3], velocities[j*3+1], velocities[j*3+2]);
			edgeSpaceTimeSpan(k, xi, xj, vi, vj, segment_radii[k], dt);
		}
	}

	// TODO: Remove
	/*private double min6 (double v1, double v2, double v3, double v4, double v5, double v6) {
		return Math.min(v1, Math.min(v2, Math.min(v3, Math.min(v4, Math.min(v5, v6)))));
	}

	private double max6 (double v1, double v2, double v3, double v4, double v5, double v6) {
		return Math.max(v1, Math.max(v2, Math.max(v3, Math.max(v4, Math.max(v5, v6)))));
	}*/

	public void edgeSpaceTimeSpan (int id, Point3d p1, Point3d p2, Vector3d v1, Vector3d v2, double h, double dt) {
		double	x1 = p1.x,
				y1 = p1.y,
				z1 = p1.z,
				x1p = x1 + v1.x * dt,
				y1p = y1 + v1.y * dt,
				z1p = z1 + v1.z * dt,
				x2 = p2.x,
				y2 = p2.y,
				z2 = p2.z,
				x2p = x2 + v2.x * dt,
				y2p = y2 + v2.y * dt,
				z2p = z2 + v2.z * dt,
				min_x = Math.min(x1, Math.min(x1p, Math.min(x2, x2p))) - h,
				max_x = Math.max(x1, Math.max(x1p, Math.max(x2, x2p))) + h,
				min_y = Math.min(y1, Math.min(y1p, Math.min(y2, y2p))) - h,
				max_y = Math.max(y1, Math.max(y1p, Math.max(y2, y2p))) + h,
				min_z = Math.min(z1, Math.min(z1p, Math.min(z2, z2p))) - h,
				max_z = Math.max(z1, Math.max(z1p, Math.max(z2, z2p))) + h;
		Point3i	min_i = new Point3i(),
				max_i = new Point3i();
		ptInCellIndex(min_x, min_y, min_z, min_i);
		ptInCellIndex(max_x, max_y, max_z, max_i);

		// Edge e belongs to [min_i.x,min_i.y,min_i.z] -> [max_i.x,max_i.y,max_i.z]
		
		// If the index is outside of the grid, put the segment to outside cell
		if(min_i.x < 0 || min_i.y < 0 || min_i.z < 0 || max_i.x >= x_number || max_i.y >= y_number || max_i.z >= z_number) {
			outside.segments.add(id);
			min_i.x = Math.min(Math.max(min_i.x, 0), x_number-1);
			min_i.y = Math.min(Math.max(min_i.y, 0), y_number-1);
			min_i.z = Math.min(Math.max(min_i.z, 0), z_number-1);
			max_i.x = Math.min(Math.max(max_i.x, 0), x_number-1);
			max_i.y = Math.min(Math.max(max_i.y, 0), y_number-1);
			max_i.z = Math.min(Math.max(max_i.z, 0), z_number-1);
		}

		// TODO: Remove
		try {
		for(int x=min_i.x; x<=max_i.x; x++)
			for(int y=min_i.y; y<=max_i.y; y++)
				for(int z=min_i.z; z<=max_i.z; z++)
					cells[index(x,y,z)].segments.add(id);
		} catch(Exception exp) {
			exp = exp;
		}
	}

	// TODO: Remove
	/*public void particleSpaceTimeSpan (Particle p, double dt) {
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
	}*/

	public void ptInCellIndex (double x, double y, double z, Point3i index) {
		index.set(
				(int)Math.floor((x-grid_min_pt.x) / x_cell_size),
				(int)Math.floor((y-grid_min_pt.y) / y_cell_size),
				(int)Math.floor((z-grid_min_pt.z) / z_cell_size));
	}

	// TODO: Not implemented after this line
	/*
	public void getSegmentCells (Cloth cloth, int segment_id, double dt, ArrayList<Cell> cells) {
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
	}*/

	public class Cell {
		/** Note: this should not be used as a accurate hit test boundary */
		Point3d				min_pt = new Point3d(),
							max_pt = new Point3d();

		int					x, y, z;
		ArrayList<Integer>	segments = new ArrayList<Integer>();

		Cell (int x, int y, int z, Point3d min_pt, Point3d max_pt) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.min_pt = min_pt;
			this.max_pt = max_pt;
		}
	}
}
