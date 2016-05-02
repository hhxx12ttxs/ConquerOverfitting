
package shapes.shapes;

import Jama.Matrix;
import canva.CanvaGraphics;
import shapes.abstractshapes.AbstractGraphicsObject;
import shapes.types.IAffineTransformed;
import shapes.types.IPerspective;
import shapes.utils.Coordinate;
import shapes.utils.Coordinates;
import shapes.utils.Face;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Gomon Sergey, ZOR
 */
public class Cube extends AbstractGraphicsObject implements IAffineTransformed, IPerspective {

	private static final int Z_BEGIN = 50;
    private Point centerP;
	private Matrix perspectiveMatrix;
	Coordinate perspectiveLoockPoint;
    private List<Face> faces = new ArrayList<Face>();

	private Coordinates vertexes;
	private boolean complete;
	private boolean removeInvis;
	private boolean perspective;

	public Cube() {
		this.vertexes = null;
		this.complete = false;
		this.perspectiveMatrix = null;
		this.perspectiveLoockPoint = new Coordinate(null,0,0,0);
		this.removeInvis = false;
	}

	@Override
	public boolean processMousePress(int x, int y) {
		if(!complete) {
			vertexes = new Coordinates();
			vertexes.addPoint(new Coordinate(null, x, y, Z_BEGIN));
			calc();
			return true;
		}
		return false;
	}

	@Override
	public boolean processMouseRelease(int x, int y) {
		if(!complete) {
			complete = true;
			calc();
		}
		return false;
	}

	@Override
	public boolean processMouseMove(int x, int y) {
		if(!complete) {
			Coordinate first = vertexes.get(0);
			int dx = x - vertexes.get(0).get(0);
			int dy = y - vertexes.get(0).get(1);
			int dl = 0;
			if(Math.abs(dx) > Math.abs(dy)) {
				dl = Math.abs(dy);
			} else {
				dl = Math.abs(dx);
			}
			vertexes.clear();
			vertexes.addPoint(first);
			vertexes.addPoint(new Coordinate(null,
					first.get(0),
					first.get(1) + dl*Math.signum(dy),
					Z_BEGIN));
			vertexes.addPoint(new Coordinate(null,
					first.get(0) + dl*Math.signum(dx),
					first.get(1) + dl*Math.signum(dy),
					Z_BEGIN));
			vertexes.addPoint(new Coordinate(null,
					first.get(0) + dl*Math.signum(dx),
					first.get(1),
					Z_BEGIN));
			vertexes.addPoint(new Coordinate(null,
					first.get(0),
					first.get(1),
					Z_BEGIN+dl));
			vertexes.addPoint(new Coordinate(null,
					first.get(0),
					first.get(1) + dl*Math.signum(dy),
					Z_BEGIN+dl));
			vertexes.addPoint(new Coordinate(null,
					first.get(0) + dl*Math.signum(dx),
					first.get(1) + dl*Math.signum(dy),
					Z_BEGIN+dl));
			vertexes.addPoint(new Coordinate(null,
					first.get(0) + dl*Math.signum(dx),
					first.get(1),
					Z_BEGIN+dl));
			calc();
			return true;
		}
		return false;
	}

	@Override
	public boolean processMouseDoubleClick(int x, int y) {
		return false;
	}

	@Override
	public boolean isComplete() {
		return complete;
	}

	@Override
	protected void calc() {
		if(vertexes == null) {
			return;
		}
		clearCoordinates();

		if(vertexes.size() == 1) {
			addPoint(vertexes.get(0).get(0), vertexes.get(0).get(1));
		} else {

			Coordinates localVertexes = vertexes.clone();

			if(perspectiveMatrix != null) {
				moveToPerspectiveLookPoint(localVertexes);
				localVertexes.applyPerspective(perspectiveMatrix);
				restorePosition(localVertexes);
			}

			//Проресовка куба не отрывая пера, глупо, но мне можно (=
			//Можно сделать через 4 полигона

			/*System.out.println(localVertexes.getNoClone(0).get(0) + " " + localVertexes.getNoClone(0).get(1));
			System.out.println(localVertexes.getNoClone(1).get(0) + " " + localVertexes.getNoClone(1).get(1));
			System.out.println(localVertexes.getNoClone(2).get(0) + " " + localVertexes.getNoClone(2).get(1));
			System.out.println(localVertexes.getNoClone(3).get(0) + " " + localVertexes.getNoClone(3).get(1));
			System.out.println(localVertexes.getNoClone(4).get(0) + " " + localVertexes.getNoClone(4).get(1));
			System.out.println(localVertexes.getNoClone(5).get(0) + " " + localVertexes.getNoClone(5).get(1));
			System.out.println(localVertexes.getNoClone(6).get(0) + " " + localVertexes.getNoClone(6).get(1));
			System.out.println(localVertexes.getNoClone(7).get(0) + " " + localVertexes.getNoClone(7).get(1));*/

			addPoint(localVertexes.getNoClone(0).get(0), localVertexes.getNoClone(0).get(1));
			addPoint(localVertexes.getNoClone(1).get(0), localVertexes.getNoClone(1).get(1));
			addPoint(localVertexes.getNoClone(2).get(0), localVertexes.getNoClone(2).get(1));
			addPoint(localVertexes.getNoClone(3).get(0), localVertexes.getNoClone(3).get(1));

			addPoint(localVertexes.getNoClone(0).get(0), localVertexes.getNoClone(0).get(1));
			addPoint(localVertexes.getNoClone(4).get(0), localVertexes.getNoClone(4).get(1));
			addPoint(localVertexes.getNoClone(7).get(0), localVertexes.getNoClone(7).get(1));
			addPoint(localVertexes.getNoClone(3).get(0), localVertexes.getNoClone(3).get(1));

			addPoint(localVertexes.getNoClone(2).get(0), localVertexes.getNoClone(2).get(1));
			addPoint(localVertexes.getNoClone(6).get(0), localVertexes.getNoClone(6).get(1));
			addPoint(localVertexes.getNoClone(7).get(0), localVertexes.getNoClone(7).get(1));
			addPoint(localVertexes.getNoClone(6).get(0), localVertexes.getNoClone(6).get(1));

			addPoint(localVertexes.getNoClone(5).get(0), localVertexes.getNoClone(5).get(1));
			addPoint(localVertexes.getNoClone(4).get(0), localVertexes.getNoClone(4).get(1));
			addPoint(localVertexes.getNoClone(5).get(0), localVertexes.getNoClone(5).get(1));
			addPoint(localVertexes.getNoClone(1).get(0), localVertexes.getNoClone(1).get(1));
			addPoint(localVertexes.getNoClone(0).get(0), localVertexes.getNoClone(0).get(1));


        }
	}

	@Override
	public Coordinates getPoints() {
		return vertexes.clone();
	}

	@Override
	public Matrix applyAffine(Matrix transformation) {
		vertexes.applyAffine(transformation);
		calc();
		return null;
	}

	public void perspective(boolean perspective) {
		this.perspective = perspective;
		calc();
	}
	
    private Point pp(int i) {
		Coordinates localVertexes = vertexes.clone();
		if(perspectiveMatrix != null && !removeInvis && perspective) {
			moveToPerspectiveLookPoint(localVertexes);
			localVertexes.applyPerspective(perspectiveMatrix);
			restorePosition(localVertexes);
		}		
        return new Point(localVertexes.coord.get(i).get(0), localVertexes.coord.get(i).get(1), localVertexes.coord.get(i).get(2));
    }

    private boolean checkSide(Point a, Point b, Point c, double[][] vector) {

        double[][] normal = getNormal(a, b, c);
        double[][] cp = new double[][]{{centerP.x, centerP.y, centerP.z, 1}};
      //  double[][] p = new double[][]{normal};
        Matrix pm = new Matrix(normal);
        Matrix check = new Matrix(cp);
        Matrix vectorm = new Matrix(vector);
        double[][] result = vectorm.times(pm).getArray();
        double[][] control = check.times(pm).getArray();
        double res = result[0][0];
        if(control[0][0]>0)
            res *= -1;
        if (res > 0) {
            return true;
        }
        return false;
    }

    private double[][] getNormal(Point p1, Point p2, Point p3) {
        double x1 = p1.getX();
        double y1 = p1.getY();
        double z1 = p1.getZ();
        double x2 = p2.getX();
        double y2 = p2.getY();
        double z2 = p2.getZ();
        double x3 = p3.getX();
        double y3 = p3.getY();
        double z3 = p3.getZ();
        double[][] normal = new double[4][1];
        normal[0][0] = y1 * (z2 - z3) + y2 * (z3 - z1) + y3 * (z1 - z2);
        normal[1][0] = z1 * (x2 - x3) + z2 * (x3 - x1) + z3 * (x1 - x2);
        normal[2][0] = x1 * (y2 - y3) + x2 * (y3 - y1) + x3 * (y1 - y2);
        normal[3][0] = -(normal[0][0] * x1 + normal[1][0] * y1 + normal[2][0] * z1);
        return normal;
    }
    private List<Face> getFaces() {
        faces.clear();
        faces.add(new Face(pp(0), pp(1), pp(2), pp(3)));
        faces.add(new Face(pp(4), pp(5), pp(6), pp(7)));
        faces.add(new Face(pp(0), pp(1), pp(5), pp(4)));
        faces.add(new Face(pp(2), pp(3), pp(7), pp(6)));
        faces.add(new Face(pp(1), pp(2), pp(6), pp(5)));
        faces.add(new Face(pp(0), pp(3), pp(7), pp(4)));
        return faces;
    }
    private void refrescenterPoint(){
        centerP = new Point();
        centerP.x = pp(2).x + pp(4).x;
        centerP.y = pp(2).y + pp(4).y;
        centerP.z = pp(2).z + pp(4).z ;
        centerP.x/=2;
        centerP.y/=2;
        centerP.z/=2;
    }
	
	public void removeInves(boolean removeInvis) {
		this.removeInvis = removeInvis;
	}
	
    @Override
    public void draw(CanvaGraphics g) {
        if (complete) {
            refrescenterPoint();
            double[][] vector = new double[][]{{0, 0, 1, 0}};
            for (Face f : getFaces()) {
                if (checkSide(f.p1, f.p2, f.p3, vector) || !removeInvis) {
                    Polygon p = new Polygon();
					p.setAntialiasing(getAntialiasing());
                    p.setPointsFromFace(f);
                    p.draw(g);
                }
            }
        } else {
            super.draw(g);
        }

    }

    @Override
	public void applyPerspective(int dx, int dy, int dz) {

		if(dx == 0 && dy == 0 && dz == 0) {
			perspectiveMatrix = null;
		}

		double dxn = 1;
		double dyn = 1;
		double dzn = 1;

		if(dx == 0) {
			dxn = 0;
			dx = 1;
		}

		if(dy == 0) {
			dyn = 0;
			dy = 1;
		}

		if(dz == 0) {
			dzn = 0;
			dz = 1;
		}

		perspectiveMatrix = new Matrix(new double[][]{
			{1, 0, 0, dxn/dx},
			{0, 1, 0, dyn/dy},
			{0, 0, 1, dzn/dz},
			{0, 0, 0, 0}}, 4, 4);
	}

	@Override
	public Coordinate getPerspectiveLoockPoint() {
		return perspectiveLoockPoint;
	}

	@Override
	public void setPerspectiveLoockPoint(Coordinate perspectiveLoockPoint) {
		this.perspectiveLoockPoint = perspectiveLoockPoint;
	}

	private void moveToPerspectiveLookPoint(Coordinates coord) {
		int x = perspectiveLoockPoint.get(0);
		int y = perspectiveLoockPoint.get(1);
		int z = perspectiveLoockPoint.get(2);
		Matrix transfirmMatrix = new Matrix(new double[][] {
			 {1,0,0,0},
			 {0,1,0,0},
			 {0,0,1,0},
			 {-x,-y,-z,1}}, 4,4);
		coord.applyAffine(transfirmMatrix);
	}

	private void restorePosition(Coordinates coord) {
		int x = perspectiveLoockPoint.get(0);
		int y = perspectiveLoockPoint.get(1);
		int z = perspectiveLoockPoint.get(2);
		Matrix transfirmMatrix = new Matrix(new double[][] {
			 {1,0,0,0},
			 {0,1,0,0},
			 {0,0,1,0},
			 {x,y,z,1}}, 4,4);
		coord.applyAffine(transfirmMatrix);
	}
}

