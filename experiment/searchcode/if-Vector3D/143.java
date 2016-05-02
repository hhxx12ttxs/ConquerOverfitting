package graphics;

import util.Color;
import util.Vector3D;

public class Light {
	/* light type */
	public static int AMBIENT = 1;
	public static int SPOT_LIGHT_ALL_DIRECTION = 2;
	public static int SPOT_LIGHT_ONE_DIRECTION = 3;
	public static int SPOT_LIGHT_PYRAMID_DIRECTION = 4;
	
	//http://zhidao.baidu.com/question/303610291.html
	public static final double WAVELENGTH_RED = 700;
	public static final double WAVELENGTH_GREEN = 550;
	public static final double WAVELENGTH_BLUE = 420;
	
	Color ambient;
	Color diffuse;
	Color specular;
	Vector3D pos;
	Vector3D direction;
	double tmin;
	double tmax;

	/*decay = 1 / (kc + kd*d + kq*d*d)
	 * decay = 1 by default
	 */
	double kc; 
	double kd;
	double kq;
	
	int type;
	
	public Light() {
		// TODO Auto-generated constructor stub
		kc = 1.0;
		kd = 0.0;
		kq = 0.0;
		tmin = 0.1;
		tmax = 100000;
	}

	public Light Clone(){
		Light clone = new Light();
		clone.setType(type);
		//clone.setAmbient(ambient.Clone());
		clone.setDiffuse(diffuse.Clone());
		clone.setSpecular(specular.Clone());
		clone.setPos(pos.Clone());
		if(direction != null)
			clone.setDirection(direction.Clone());
		clone.setTmax(tmax);
		clone.setTmin(tmin);
		return clone;
	}
	
	
	public Color getAmbient() {
		return ambient;
	}

	public void setAmbient(Color ambient) {
		this.ambient = ambient;
	}

	public Color getDiffuse() {
		return diffuse;
	}

	public void setDiffuse(Color diffuse) {
		this.diffuse = diffuse;
	}

	public Color getSpecular() {
		return specular;
	}

	public void setSpecular(Color specular) {
		this.specular = specular;
	}

	public Vector3D getPos() {
		return pos;
	}

	public void setPos(Vector3D pos) {
		this.pos = pos;
	}

	public Vector3D getDirection() {
		return direction;
	}

	public void setDirection(Vector3D direction) {
		this.direction = direction;
	}

	public double getTmin() {
		return tmin;
	}

	public void setTmin(double tmin) {
		this.tmin = tmin;
	}

	public double getTmax() {
		return tmax;
	}

	public void setTmax(double tmax) {
		this.tmax = tmax;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	
	
}

