/*
 * Vector3D.java
 * 
 * Created on 26/10/2007
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package demos.nehe.lesson40;

/**
 * Java implementation of Erkin Tunca's "Vector3D" class from "Physics1.h" by Scott Rains.<br/>
 *
 * @author Scott Rains (sid.rains[at]gmail.com)
 *         Date: 26 Oct 2007
 */
class Vector3D {

    public float x; // the x value of this Vector3D
    public float y; // the y value of this Vector3D
    public float z; // the z value of this Vector3D

    public Vector3D() {
        x = 0;
        y = 0;
        z = 0;
    }

    public Vector3D(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3D(Vector3D vector) {
        x = vector.x;
        y = vector.y;
        z = vector.z;
    }

    /**
     * operator= equlivant, sets values of v to this Vector3D. example: v1 = v2 means that values of v2 are set onto v1
     */
    public void set(Vector3D vector) {
        x = vector.x;
        y = vector.y;
        z = vector.z;
    }

    public void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * operator+ equlivant, is used to add two Vector3D's. operator+ returns a new Vector3D
     */
    public Vector3D add(Vector3D v) {
        return new Vector3D(x + v.x, y + v.y, z + v.z);
    }

    /**
     * operator- equlivant, is used to take difference of two Vector3D's. operator- returns a new Vector3D
     */
    public Vector3D subtract(Vector3D v) {
        return new Vector3D(x - v.x, y - v.y, z - v.z);
    }

    public static Vector3D subtract(Vector3D v1, Vector3D v2) {
        return new Vector3D(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z);
    }

    /**
     * operator* equlivant, is used to scale a Vector3D by a value. This value multiplies the Vector3D's x, y and z.
     */
    public Vector3D multiply(float value) {
        return new Vector3D(x * value, y * value, z * value);
    }

    /**
     * perator/ equlivant, is used to scale a Vector3D by a value. This value divides the Vector3D's x, y and z.
     */
    public Vector3D divide(float value) {
        return new Vector3D(x / value, y / value, z / value);
    }

    /**
     * operator+= equlivant, is used to add another Vector3D to this Vector3D.
     */
    public Vector3D addTo(Vector3D v) {
        x += v.x;
        y += v.y;
        z += v.z;
        return this;
    }

    /**
     * operator-= equlivant, is used to subtract another Vector3D from this Vector3D.
     */
    public Vector3D subtractTo(Vector3D v) {
        x -= v.x;
        y -= v.y;
        z -= v.z;
        return this;
    }

    /**
     * operator*= equlivant, is used to scale this Vector3D by a value.
     */
    public Vector3D multiplyTo(float v) {
        x *= v;
        y *= v;
        z *= v;
        return this;
    }

    /**
     * operator/= equlivant, is used to scale this Vector3D by a value.
     */
    public Vector3D divideTo(float v) {
        x /= v;
        y /= v;
        z /= v;
        return this;
    }

    /**
     * operator- equlivant, is used to set this Vector3D's x, y, and z to the negative of them.
     */
    public Vector3D negative() {
        return new Vector3D(-x, -y, -z);
    }

    /**
     * length() returns the length of this Vector3D
     */
    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * Normalizes this Vector3D that its direction remains the same but its length is 1.
     */
    public void normalize() {
        float length = length();

        if (length == 0) {
            return;
        }
        x /= length;
        y /= length;
        z /= length;
    }

    public String toString() {
        return x + ", " + y + ", " + z;
    }
}
