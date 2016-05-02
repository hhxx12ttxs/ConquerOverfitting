/**
 * 
 */
package com.smokebox.lib.utils;

/**
 * @author Harald Floor Wilhelmsen
 *
 */
public class Vector2 {

	public float x;
	public float y;
	
	/**
	 * Creates and empty 2D-vector.
	 */
	public Vector2() {
		x = 0;
		y = 0;
	}
	
	/**
	 * Creates a copy of the given vector.
	 * @param v	The vector to copy
	 */
	public Vector2(Vector2 v) {
		x = v.x;
		y = v.y;
	}
	
	/**
	 * Constructs a normal vector from given angle
	 * @param angle	
	 */
	public Vector2(float angle) {
		x = (float)Math.sin(angle);
		y = (float)Math.cos(angle);
	}
	
	/**
	 * Constructs a normal vector from given angle
	 * @param angle	
	 */
	public Vector2(double angle) {
		x = (float)Math.sin(angle);
		y = (float)Math.cos(angle);
	}
	
	/**
	 * Creates a vector from the two given components 
	 * @param x	The x-component
	 * @param y The y-component
	 */
	public Vector2(float x, float y) {
		this.x = x;
		this.y = y;	
	}
	
	/**
	 * Sets this vector equal to the one given
	 * @param v	The vector to mimic
	 */
	public Vector2 set(Vector2 v) {
		this.x = v.x;
		this.y = v.y;
		return this;
	}
	
	/**
	 * Sets this vector to the two given component
	 * @param x	The x-component
	 * @param y	The y-component
	 */
	public Vector2 set(float x, float y) {
		this.x = x;
		this.y = y;
		return this;
	}
	
	/**
	 * Returns the magnitude of this vector
	 * @return	float for the magnitude
	 */
	public float getMag() {
		return (float) Math.sqrt(x*x + y*y);
	}
	
	/**
	 * Returns the squared magnitude of the vector.
	 * Use this to spare resources if comparing the length of two vectors.
	 * @return	float for magnitude squared
	 */
	public float getMag2() {
		return x*x + y*y;
	}
	
	/**
	 * Normalizes this vector
	 * @return	This vector for chaining
	 */
	public Vector2 nor() {
		float mag = getMag();
		if(mag != 0) {
			x /= mag;
			y /= mag;
		}
		return this;
	}
	
	/**
	 * Adds the given vector to this vector
	 * @param v	The vector to add by
	 * @return	This vector for chaining
	 */
	public Vector2 add(Vector2 v) {
		x += v.x;
		y += v.y;
		return this;
	}
	
	/**
	 * Adds the given x and y to this vector
	 * @param x
	 * @param y
	 * @return This vector for chaining
	 */
	public Vector2 add(float x, float y) {
		this.x += x;
		this.y += y;
		return this;
	}
	
	/**
	 * Subtracts the given vector from this vector
	 * @param v	The vector to subtract by
	 * @return	This vector for chaining
	 */
	public Vector2 sub(Vector2 v) {
		x -= v.x;
		y -= v.y;
		return this;
	}
	
	/**
	 * Subtracts the given x and y from this vector
	 * @param x	The x to subtract by
	 * @param y	The y to subtract by
	 * @return	This vector for chaining
	 */
	public Vector2 sub(float x, float y) {
		this.x -= x;
		this.y -= y;
		return this;
	}
	
	/**
	 * Scales this vector by given scalar
	 * @param scalar	Float to scale by
	 * @return	This vector for chaining
	 */
	public Vector2 scl(float scalar) {
		x *= scalar;
		y *= scalar;
		return this;
	}
	
	/**
	 * Scales this vector by given scalar
	 * @param scalar	Double to scale by
	 * @return	This vector for chaining
	 */
	public Vector2 scl(double scalar) {
		x *= scalar;
		y *= scalar;
		return this;
	}
	
	/**
	 * Flips both components of this vector
	 * @return	This vector for chaining
	 */
	public Vector2 flip() {
		x = -x;
		y = -y;
		return this;
	}

	/**
	 * Flips the x-component of this vector
	 * @return 	This vector for chaining
	 */
	public Vector2 flipX() {
		x = -x;
		return this;
	}
	
	/**
	 * Flips the y-component of this vector
	 * @return 	This vector for chaining
	 */
	public Vector2 flipY() {
		y = -y;
		return this;
	}
	
	/**
	 * Adds a scaled vector to this vector
	 * @param v		The vector to add
	 * @param scl	The scale to scale the add-vector with
	 * @return		This vector for chaining
	 */
	public Vector2 addScaledVector(Vector2 v, float scl) {
		x += v.x*scl;
		y += v.y*scl;
		return this;
	}
	
	/**
	 * Rounds the components with java's Math.round()
	 * @return This vector for chaining
	 */
	public Vector2 round() {
		x = Math.round(x);
		y = Math.round(y);
		return this;
	}
	
	/**
	 * Sets vector to random values.
	 * @param xNeg	Can x be negative
	 * @param xPos	Can x be positive
	 * @param yNeg	Can y be negative
	 * @param yPos	Can y be positive
	 * @return	This vector for chaining
	 */
	public Vector2 setRandom(boolean xNeg, boolean xPos, boolean yNeg, boolean yPos) {
		if(!xNeg && !xPos && !yNeg && !yPos) return this;
		
		x = (float) (Math.random()*(1 + MathUtils.BoolToInt(xPos)) - 1*MathUtils.BoolToInt(xNeg));
		y = (float) (Math.random()*(1 + MathUtils.BoolToInt(yPos)) - 1*MathUtils.BoolToInt(yNeg));
		
		return this;
	}
	
	public static Vector2 getRandom() {
		return new Vector2((float)Math.random()*2 - 1, (float)Math.random()*2 - 1);
	}
	
	public static Vector2 getRandomDirection() {
		return new Vector2(Math.random()*Math.PI*2);
	}
	
	/**
	 * Sets this vector to random normalized
	 * @return	This vector for chaining
	 */
	public Vector2 setRandomNormalized() {
		x = (float)Math.random()*2 - 1;
		y = (float)Math.random()*2 - 1;
		
		return this.nor();
	}
	
	/**
	 * Sets vector to random values. If xlow or yhigh i higher than
	 * counterpart, vector will remain as before.
	 * @param xll	X lower limit
	 * @param xtl	X top limit
	 * @param yll	Y lower limit
	 * @param ytl	Y top limit
	 * @return This vector for chaining, after values have been set.
	 */
	public Vector2 setRandom(float xll, float xtl, float yll, float ytl) {
		if(xtl < xll || ytl < yll) return this;
		
		x = (float) (Math.random()*(xtl - xll) + xll);
		y = (float) (Math.random()*(ytl - yll) + yll);
		
		return this;
	}

	/**
	 * Returns the angle of this vector in radians
	 * @return The angle of this vector in radians
	 */
	public float getAngleAsRadians() {
		float f = (float)Math.atan2(y, x);
		if(f < 0) f += 2f*Math.PI;
		return f;
	}
	
	/**
	 * Returns the angle between this vector and the one given
	 * @param v	The vector to compare angle with
	 * @return	The angle between this vector and the one given
	 */
	public float getAngleTo(Vector2 v) {
		return v.getAngleAsRadians() - this.getAngleAsRadians();
	}
	
	/**
	 * Returns the dot-product of this vector and the given vector
	 * @param v	Vector to get dot-product with
	 * @return	The dot-product of this vector and the given vector
	 */
	public float getDotProduct(Vector2 v) {
		return x*v.x + y*v.y;
	}
	
	/**
	 * Sets this vectors components to zero
	 * @return	This vector for chaining
	 */
	public Vector2 clear() {
		this.x = 0;
		this.y = 0;
		return this;
	}
	
	/**
	 * Returns the magnitude of the difference between the vectors
	 * @param v	Vector to get difference to
	 * @return	A float for magnitude of the difference
	 */
	public float getDifferenceTo(Vector2 v) {
		float xD = x - v.x;
		float yD = y - v.y;
		return (float)Math.sqrt(xD*xD + yD*yD);
	}
	
	/**
	 * Return the true distance to the given coordinates
	 * @param x	The destination-x
	 * @param y	The destination-y
	 * @return	The true difference between this vector and two coordinates given
	 */
	
	public float getDifferenceTo(float x, float y) {
		float xD = this.x - x;
		float yD = this.y - y;
		return (float)Math.sqrt(xD*xD + yD*yD);
	}
	
	/**
	 * Return the squared distance to the given coordinates
	 * @param x	The destination-x
	 * @param y	The destination-y
	 * @return	The squared difference between this vector and two coordinates given
	 */
	public float getDifferenceTo2(float x, float y) {
		float xD = this.x - x;
		float yD = this.y - y;
		return xD*xD + yD*yD;
	}
	
	/**
	 * Returns true of this vector is zero
	 * @return	True of the this vector is zero
	 */
	public boolean isZero() {
		return x == 0 && y == 0;
	}
	
	/**
	 * Raises x- and y-component to the given power
	 * @param power	The power to raise by
	 * @return	This vector for chaining
	 */
	public Vector2 pow(float power) {
		x = (float)Math.pow(x, power);
		y = (float)Math.pow(y, power);
		return this;
	}
	
	/**
	 * Sets the x-component of this vector to the given value
	 * @param x	The value to set x to
	 * @return This vector for chaining
	 */
	public Vector2 setX(float x) {
		this.x = x;
		return this;
	}
	
	/**
	 * Sets the y-component of this vector to the given value
	 * @param y The value to set y to
	 * @return This vector for chaining
	 */
	public Vector2 setY(float y) {
		this.y = y;
		return this;
	}
	
	/**
	 * Truncates(limits) this vector to the given length
	 * @param max	The maximum value
	 * @return	This vector for chaining
	 */
	public Vector2 truncate(float max) {
		if(getMag2() > max*max) this.nor().scl(max);
		return this;
	}
	
	/**
	 * Applies linear interpolation to the vector
	 * @param end	The Vector to interpolate towards
	 * @param fraction	The step in which to do it by. (0-1). Higher numbers result in faster interpolation
	 * @return	This vector for chaining
	 */
	public Vector2 lerp(Vector2 end, float fraction) {
		x = MathUtils.lerp(this.x, end.x, fraction);
		y = MathUtils.lerp(this.y, end.y, fraction);
		return this;
	}
	
	/**
	 * A normalized lerp, much more accurate compared to how effective it is.
	 * @param end	Vector2 to interpolate towards
	 * @param fraction	The step in which to do it by. (0-1). Higher numbers result in faster interpolation
	 * @return	This vector for chaining
	 */
	public Vector2 nlerp(Vector2 end, float fraction) {
		return lerp(end, fraction).nor();
	}
	
	/** Does not work. FIXME
	 * Applies circular interpolation to the vector. Less accurate
	 * but more effective than lerp.
	 * @param end	Vector to interpolate towards
	 * @param fraction	The step in which to do it by. (0-1). Higher numbers result in faster interpolation
	 * @return	This vector for chaining
	 */
	@Deprecated
	public Vector2 clerp(Vector2 end, float fraction) {
		float dot = this.getDotProduct(end);
		MathUtils.clamp(dot, -1f, 1f);
		float theta = (float)Math.acos(dot)*fraction;
		Vector2 relVec = new Vector2(end).sub(new Vector2(this).scl(dot)).nor();
		return new Vector2(this).scl( (float)Math.cos(theta) ).add( relVec.scl( (float)Math.sin(theta) ) );
	}
	
	public static Vector2 projectAOntoB(Vector2 a, Vector2 b) {
		Vector2 p = new Vector2(a).scl(a.getDotProduct(b)/a.getMag2());
		return p;
	}

	/**
	 * Returns the given vector projected onto this one 
	 * @param b	The vector to project from
	 * @return	This vector for chaining
	 */
	public Vector2 limitByProjection(Vector2 b) {
		this.scl(this.getDotProduct(b)/this.getMag2());
		
		return this;
	}
	
	public Vector2 projectOnto(Vector2 a) {
		this.set(new Vector2(a).scl(a.getDotProduct(this)/a.getMag2()));
		
		return this;
	}
	
	/**
	 * Return vector with components inverted (1/x, 1/y)
	 * @return This vector for chaining
	 */
	public Vector2 invert() {
		x = 1/x;
		y = 1/y;
		return this;
	}
	
	/**
	 * Returns a normal vector for this vector
	 * @return	Not normalized normal vector
	 */
	public Vector2 getNormal() {
		return new Vector2(-y, x);
	}
	
	/**
	 * Returns a normal vector for this vector
	 * This is flipped compared to getNormal()
	 * @return	Not normalized normal vector
	 */
	public Vector2 getNormal2() {
		return new Vector2(y, -x);
	}
	
	/**
	 * Use to hinder unwanted changes when passing as
	 * 	parameter and using in calculations and the likes.
	 * @return	A copy of this vector
	 */
	public Vector2 cp() {
		return new Vector2(x, y);
	}
}

