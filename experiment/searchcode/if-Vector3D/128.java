/*
 * GravityModel.java
 * 
 * Project: RobSail2012
 * Package: boat.model
 * Last update: 16.04.2012
 * 
 * Contact: Alexander Schlaefer (schlaefer@rob.uni-luebeck.de)
 *          Nikolaus Ammann (ammann@rob.uni-luebeck.de)
 * 
 * Copyright 2012 Institute for Robotics and Cognitive Systems.
 */

package boat.model;

import java.util.Vector;

import javax.vecmath.Vector3d;

/**
 * The Class GravityModel.
 */
public class GravityModel {
    
    /** The capacity. */
    public volatile int capacity = 9;
    
    /** The v model. */
    protected Vector<Vector3d> vModel = null;
    
    /** The offset. */
    protected Vector3d offset = new Vector3d();
    
    /**
     * Creates a new instance of CourseModel.
     * 
     * @param capacity
     *            the capacity
     */
    public GravityModel(int capacity) {
        this.capacity = capacity;
        this.vModel = new Vector<Vector3d>();
    }
    
    /**
     * Instantiates a new gravity model.
     * 
     * @param capacity
     *            the capacity
     * @param offset
     *            the offset
     */
    public GravityModel(int capacity, Vector3d offset) {
        this.capacity = capacity;
        this.offset = offset;
        this.vModel = new Vector<Vector3d>();
    }
    
    /**
     * Adds the.
     * 
     * @param v
     *            the v
     */
    public void add(Vector3d v) {
        this.vModel.add(v);
        while (this.vModel.size() > this.capacity) {
            this.vModel.remove(0);
        }
    }
    
    // only mean of components, not of vector!
    /**
     * Gets the mean gravity.
     * 
     * @return the mean gravity
     */
    public Vector3d getMeanGravity() {
        if (this.vModel.size() < 1)
            return null;
        Vector3d result = new Vector3d();
        for (Vector3d v : this.vModel) {
            result.add(v);
        }
        result.x /= this.vModel.size();
        result.y /= this.vModel.size();
        result.z /= this.vModel.size();
        return result;
    }
    
}

