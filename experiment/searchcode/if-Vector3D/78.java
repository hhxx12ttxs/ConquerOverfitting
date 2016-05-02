package graphics;

import bubble.Bubble;
import main.Scene;
import Jama.Matrix;
import util.MyMatrix;
import util.Vector3D;

public class Triangle extends Surface {
	protected Vector3D[] verticles = new Vector3D[3];
	protected Vector3D normal;
	protected Material material = Scene.testMaterial;
	
	protected Bubble bubble;
	
	public Triangle() {
	}

	public Triangle(Vector3D a, Vector3D b, Vector3D c) {
		verticles[0] = a;
		verticles[1] = b;
		verticles[2] = c;
	}

	public Triangle(Vector3D[] verticles) {
		if (verticles.length >= 3) {
			this.verticles[0] = verticles[0];
			this.verticles[1] = verticles[1];
			this.verticles[2] = verticles[2];
		}
	}

	public boolean hit(Vector3D start, Vector3D direction, double mint,
			double maxt) {
		Vector3D solution = getHitSolution(start, direction);
		if (solution != null && solution.x > 0 && solution.y > 0
				&& solution.x + solution.y < 1 && solution.z > mint
				&& solution.z < maxt) {
			return true;
		} else {
			return false;
		}

	}

	public HitRecord hitNearestPoint(Vector3D start, Vector3D direction,
			double mint, double maxt) {
		Vector3D solution = getHitSolution(start, direction);
		if (solution != null && solution.x > 0 && solution.y > 0
				&& solution.x + solution.y < 1 && solution.z > mint
				&& solution.z < maxt) {
			Vector3D ab = Vector3D.Substract(verticles[1], verticles[0]);
			Vector3D ac = Vector3D.Substract(verticles[2], verticles[0]);
			Vector3D[] vecs = {ab, ac, verticles[0]};
			double[] coes = {solution.x, solution.y, 1};
			Vector3D hitPt = Vector3D.LinearSum(vecs, coes);
			HitRecord record = new HitRecord(hitPt, this, solution.z);
			return record;
		} else {
			return null;
		}
	}

	private Vector3D getHitSolution(Vector3D start, Vector3D direction) {
		double[][] leftArray = {
				{ verticles[0].x - verticles[1].x,
						verticles[0].x - verticles[2].x, direction.x },
				{ verticles[0].y - verticles[1].y,
						verticles[0].y - verticles[2].y, direction.y },
				{ verticles[0].z - verticles[1].z,
						verticles[0].z - verticles[2].z, direction.z } };
		Matrix left = new Matrix(leftArray, 3, 3);
		Vector3D right = new Vector3D(verticles[0].x - start.x, verticles[0].y
				- start.y, verticles[0].z - start.z);
		Vector3D solution = MyMatrix.SolveEquation(left, right);
		return solution;
	}

	public Vector3D[] getVerticles() {
		return verticles;
	}

	public void setVerticles(Vector3D[] verticles) {
		this.verticles = verticles;
	}

	public Vector3D getNormal() {
		return normal;
	}

	public void setNormal(Vector3D normal) {
		this.normal = normal;
	}

	public Material getMaterial() {
		return material;
	}

	public void setMaterial(Material material) {
		this.material = material;
	}

	public Bubble getBubble() {
		return bubble;
	}

	public void setBubble(Bubble bubble) {
		this.bubble = bubble;
	}

}

