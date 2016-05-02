package octree.culling;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

public class GeometricViewFrustum {

	// corner points of the view frustum
	// point numbering from right bottom counter clockwise
	private Vector3f base_near1;
	private Vector3f base_near2;
	private Vector3f base_near3;
	private Vector3f base_near4;
	
	private Vector3f base_far1;
	private Vector3f base_far2;
	private Vector3f base_far3;
	private Vector3f base_far4;
	
	private Vector3f near1;
	private Vector3f near2;
	private Vector3f near3;
	private Vector3f near4;
	
	private Vector3f far1;
	private Vector3f far2;
	private Vector3f far3;
	private Vector3f far4;
	
	// other variables
	private float viewing_angle;
	private float aspect_ratio;  // width / height
	private float z_near;
	private float z_far;
	private Vector3f position;
	private Vector3f orientation;
	private Vector3f up;
	private Vector3f right;
	
	public GeometricViewFrustum(float viewing_angle, float z_near, float z_far, float aspect_ratio,  Vector3f position, Vector3f orientation, Vector3f up, Vector3f right) {
		// set variables
		this.viewing_angle = viewing_angle;
		this.z_near = z_near;
		this.z_far = z_far;
		this.aspect_ratio = aspect_ratio;
		this.position = position;
		orientation.normalize();
		this.orientation = orientation;
		up.normalize();
		this.up = up;
		right.normalize();
		this.right = right;
		
		calculateBounds();
		
		near1 = base_near1;
		near2 = base_near2;
		near3 = base_near3;
		near4 = base_near4;
		
		far1 = base_far1;
		far2 = base_far2;
		far3 = base_far3;
		far4 = base_far4;
	}
	
	
	private void calculateBounds() {
		// center of far and near plane
		Vector3f nc = new Vector3f(this.orientation);
		nc.scale(this.z_near);
		nc.add(this.position);
		
		Vector3f fc = new Vector3f(orientation);
		fc.scale(this.z_far);
		fc.add(this.position);
		
		double va_tan = Math.tan(this.viewing_angle);
				
		// half widths
		float hn = (float) (this.z_near * va_tan);
		float hf = (float) (this.z_far * va_tan);
		
		float wn = hn * this.aspect_ratio;
		float wf = hf * this.aspect_ratio;
		
		// finally calculate corner points
		
		// near plane
		Vector3f hn_up = new Vector3f(this.up);
		hn_up.scale(hn);
		Vector3f wn_right = new Vector3f(this.right);
		wn_right.scale(wn);
		
		base_near1 = new Vector3f(nc);
		base_near1.sub(hn_up);
		base_near1.add(wn_right);
		
		base_near2 = new Vector3f(nc);
		base_near2.add(hn_up);
		base_near2.add(wn_right);
		
		base_near3 = new Vector3f(nc);
		base_near3.add(hn_up);
		base_near3.sub(wn_right);
		
		base_near4 = new Vector3f(nc);
		base_near4.sub(hn_up);
		base_near4.sub(wn_right);
		
		// far plane
		Vector3f hf_up = new Vector3f(this.up);
		hf_up.scale(hf);
		Vector3f wf_right = new Vector3f(this.right);
		wf_right.scale(wf);
		
		base_far1 = new Vector3f(fc);
		base_far1.sub(hf_up);
		base_far1.add(wf_right);
		
		base_far1 = new Vector3f(fc);
		base_far1.add(hf_up);
		base_far1.add(wf_right);
		
		base_far1 = new Vector3f(fc);
		base_far1.add(hf_up);
		base_far1.sub(wf_right);
		
		base_far1 = new Vector3f(fc);
		base_far1.sub(hf_up);
		base_far1.sub(wf_right);
	}
	
	
	public void update(Matrix4f transformation) {
		
		near1 = new Vector3f(base_near1);
		transformation.transform(near1);
		
		near2 = new Vector3f(base_near2);
		transformation.transform(near2);
		
		near3 = new Vector3f(base_near3);
		transformation.transform(near3);
		
		near4 = new Vector3f(base_near4);
		transformation.transform(near4);
		
		far1 = new Vector3f(base_far1);
		transformation.transform(far1);
		
		far2 = new Vector3f(base_far2);
		transformation.transform(far2);
		
		far3 = new Vector3f(base_far3);
		transformation.transform(far3);
		
		far4 = new Vector3f(base_far4);
		transformation.transform(far4);
	}
	
	private boolean pointInFrustum(Vector3f point) {
		return ()
	}
	
	public int isVisible(OctreeCell cell) {
		// test if
		if ()
	}
}

