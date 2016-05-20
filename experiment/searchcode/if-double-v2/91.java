package ca.sandstorm.luminance.math;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

public class Colliders
{
    public static final float EPSILON = 1e-8f;
    
    public static Vector3f UP = new Vector3f(0, 1, 0);
    public static Vector3f DOWN = new Vector3f(0, -1, 0);
    public static Vector3f LEFT = new Vector3f(-1, 0, 0);
    public static Vector3f RIGHT = new Vector3f(1, 0, 0);
    
    private static Vector3f _tmpCollisionPoint = new Vector3f();
    private static Vector3f _tmpCrossProduct = new Vector3f();
        
    public static void Transform(Vector3f position, Matrix3f matrix, Vector3f result)
    {   

        result.x = (position.x * matrix.m00) + (position.y * matrix.m10) + (position.z * matrix.m20);// + matrix.m41;
        result.y = (position.x * matrix.m01) + (position.y * matrix.m11) + (position.z * matrix.m21);// + matrix.m42;
        result.z = (position.x * matrix.m02) + (position.y * matrix.m12) + (position.z * matrix.m22);// + matrix.m43;
    }
    
    
    public static Matrix3f getMatrixRotationY(float rad)
    {
	Matrix3f mat = new Matrix3f();
	mat.setIdentity();
	mat.rotY(rad);	
	
	return mat;
    }
    
    
    public static double dotProduct(Vector3f v1, Vector3f v2)
    {
        return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
    }    
    
    
    public static double distance(Vector3f v1, Vector3f v2)
    {
	return Math.sqrt( (v2.x - v1.x) * (v2.x - v1.x) + 
	                  (v2.y - v1.y) * (v2.y - v1.y) + 
	                  (v2.z - v1.z) * (v2.z - v1.z) );
    }
    
    
    public static Vector3f crossProduct(Vector3f v1, Vector3f v2)
    {
	_tmpCrossProduct.cross(v1, v2);
	
	return _tmpCrossProduct;
    }
    
    
    public static double intersect(Ray r, Plane p)
    {
        // Calc D for the plane (this is usually pre-calculated
        // and stored with the plane)
        double D = dotProduct(p.getPosition(), p.getNormal());

        // Determine the distance from rOrigin to the point of
        // intersection on the plane

        double numer = dotProduct(p.getNormal(), r.getPosition()) + D;
        double denom = dotProduct(p.getNormal(), r.getDirection());
        return -(numer / denom);
    }
     
    
    public static Vector3f collide(Ray r, Plane p)
    {
        // Get the distance to the collision point

        float time = (float)intersect(r, p);

        // Calculate the collision point
        
        _tmpCollisionPoint.x = r.getPosition().x + r.getDirection().x * time;
        _tmpCollisionPoint.y = r.getPosition().y + r.getDirection().y * time;
        _tmpCollisionPoint.z = r.getPosition().z + r.getDirection().z * time;

        // Here it is...

        return _tmpCollisionPoint;
    }
    
            
    /**
     * Calculate the time of collision between a sphere and a ray.
     * Remember this is not time as in 'real time' it is time as in the
     * mathematical time along the rays path it hits the sphere.
     * @param sphere The sphere
     * @param ray The ray
     * @return < 0 means no collision, >= 0 means a possible collision
     */
    public static float intersect(Sphere sphere, Ray ray)
    {
	Vector3f origin = ray.getPosition();
	Vector3f dir = ray.getDirection();
	Vector3f center = sphere.getCenter();
	float radius = sphere.getRadius();
	float t = 0.0f;
	
        //Compute A, B and C coefficients
        float a = (float)dotProduct(ray.getDirection(), ray.getDirection());
        float b = 2.0f * ( dir.x * (origin.x - center.x) + dir.y * (origin.y - center.y) + dir.z * (origin.z - center.z) );
        float c = 	((origin.x - center.x) * (origin.x - center.x)) + 
        		((origin.y - center.y) * (origin.y - center.y)) + 
        		((origin.z - center.z) * (origin.z - center.z)) - 
        		(radius * radius);

        //Find discriminant
        float disc = b * b - 4.0f * a * c;
        
        // if discriminant is negative there are no real roots, so return 
        // false as ray misses sphere
        if (disc < 0)
            return -1;

        // compute q as described above
        float distSqrt = (float)Math.sqrt(disc);
        float q;
        if (b < 0)
            q = (-b - distSqrt)/2.0f;
        else
            q = (-b + distSqrt)/2.0f;

        // compute t0 and t1
        float t0 = q / a;
        float t1 = c / q;

        // make sure t0 is smaller than t1
        if (t0 > t1)
        {
            // if t0 is bigger than t1 swap them around
            float temp = t0;
            t0 = t1;
            t1 = temp;
        }

        // if t1 is less than zero, the object is in the ray's negative direction
        // and consequently the ray misses the sphere
        if (t1 < 0)
            return -1;

        // if t0 is less than zero, the intersection point is at t1
        if (t0 < 0)
        {
            t = t1;
            return t;
        }
        // else the intersection point is at t0
        else
        {
            t = t0;
            return t;
        }
    }
    
    
    /**
     * Finds the exact collision point between a sphere and a ray if one exists.
     * @param s The sphere
     * @param r The ray
     * @return The collision point if one occurs, null for no collision.
     */
    public static Vector3f collide(Sphere s, Ray r)
    {
	// find time of intersection
	float t = intersect(s, r);
	
	// if a time exists, find collision point
	if (t >= 0.0f)
	{
	    _tmpCollisionPoint.x = r.getPosition().x + (r.getDirection().x * t);
	    _tmpCollisionPoint.y = r.getPosition().y + (r.getDirection().y * t);
	    _tmpCollisionPoint.z = r.getPosition().z + (r.getDirection().z * t);
	    
	    return _tmpCollisionPoint;
	}
	
	// no collision
	return null;
    }
    
    
    /**
     * Fast Intersection Function between ray/plane
     * @unused in Luminance
     */
    public static boolean IntersionPlane(Plane plane, Vector3f position, Vector3f direction,
                                       double[] lamda, Vector3f pNormal) {

        double dotProduct = direction.dot(plane.getNormal());
        double l2;

        //determine if ray paralle to plane
        if ((dotProduct < EPSILON) && (dotProduct > -EPSILON))
            return false;

        Vector3f substract = new Vector3f(plane.getPosition());
        substract.sub(position);
        l2 = (plane.getNormal().dot(substract)) / dotProduct;

        if (l2 < -EPSILON)
            return false;

        pNormal.set(plane.getNormal());
        lamda[0] = l2;
        return true;
    }
}

