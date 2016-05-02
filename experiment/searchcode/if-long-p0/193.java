package com.jphysx;

/**
 * Copyright (c) 2007-2008, Yuri Kravchik and AGEIA
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of the Yuri Kravchik nor the names of its contributors
 *   may be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public class NxPlane {
  private long swigCPtr;
  boolean swigCMemOwn;
  
  /**
   * Default java constructor.
   * @param constructionMarker marks that this is java constructor. Clashing with C++ constructors is unlikely.
   */
  NxPlane(Boolean constructionMarker){}

  NxPlane(long cPtr, boolean cMemoryOwn) {
      setCPart(cPtr, cMemoryOwn);
  }


  /**
   * Returns c-pointer of the native object associated with java object.
   * <p>
   * Static because pointers to the parent and child of the same object could be different in C++. Not like in Java.
   * <p>
   * Usable for any reason - statistics, research, comparison. 
   * @param obj java-object
   * @return c-pointer to the native object.
   */
  public static long getCPtr(NxPlane obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  
  /**
   * C-object ownership.
   * @return true - java object is in charge to delete corresponding c-object
   */
  public boolean isCMemoryOwner() {
	return swigCMemOwn;
  }


  /**
   * Replaces native object associated with this java object with another one.
   * Handles deletion of native object if needed.
   * @param cPtr c-pointer to the native object
   * @param cMemoryOwn is native object needs to be deleted when java object is deleted?
   */
  void replaceCPart(long cPtr, boolean cMemoryOwn) {
      exceptionlessDelete();
      setCPart(cPtr, cMemoryOwn);
  }


  /**
   * Contains functionality of object construction.
   * @param cPtr c-pointer to the native object
   * @param cMemoryOwn is native object needs to be deleted when java object is deleted?
   */
  void setCPart(long cPtr, boolean cMemoryOwn) {
      swigCMemOwn = cMemoryOwn;
      swigCPtr = cPtr;
  }


  /**
   * Deletes corresponding c-object if this java object is owner of it. Do nothing in other case.
   */
  void exceptionlessDelete(){
      if(swigCPtr != 0 && swigCMemOwn){
          delete();
      }
  }

  protected void finalize() {
    delete();
  }


  public synchronized void delete() {
    if(swigCPtr != 0 && swigCMemOwn) {
      swigCMemOwn = false;
      JPhysXAdapterJNI.delete_NxPlane(swigCPtr);
    }

    swigCPtr = 0;
  }

  public boolean equalCPtr(NxPlane obj) {
    return obj.swigCPtr == this.swigCPtr;
  }


  public NxPlane() {
    this(JPhysXAdapterJNI.new_NxPlane__SWIG_0(), true);
  }


  public NxPlane(float nx, float ny, float nz, float _d) {
    this(JPhysXAdapterJNI.new_NxPlane__SWIG_1(nx, ny, nz, _d), true);
  }


  public NxPlane(NxVec3 p, NxVec3 n) {
    this(JPhysXAdapterJNI.new_NxPlane__SWIG_2(NxVec3.getCPtr(p), p, NxVec3.getCPtr(n), n), true);
  }


  public NxPlane(NxVec3 p0, NxVec3 p1, NxVec3 p2) {
    this(JPhysXAdapterJNI.new_NxPlane__SWIG_3(NxVec3.getCPtr(p0), p0, NxVec3.getCPtr(p1), p1, NxVec3.getCPtr(p2), p2), true);
  }


  public NxPlane(NxVec3 _n, float _d) {
    this(JPhysXAdapterJNI.new_NxPlane__SWIG_4(NxVec3.getCPtr(_n), _n, _d), true);
  }


  public NxPlane(NxPlane plane) {
    this(JPhysXAdapterJNI.new_NxPlane__SWIG_5(NxPlane.getCPtr(plane), plane), true);
  }


  public NxPlane zero() {
    return new NxPlane(JPhysXAdapterJNI.NxPlane_zero(swigCPtr, this), false);
  }
  public NxPlane zero_inplace(NxPlane result) {
    long cPtr = JPhysXAdapterJNI.NxPlane_zero(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public NxPlane set(float nx, float ny, float nz, float _d) {
    return new NxPlane(JPhysXAdapterJNI.NxPlane_set__SWIG_0(swigCPtr, this, nx, ny, nz, _d), false);
  }
  public NxPlane set_inplace(NxPlane result, float nx, float ny, float nz, float _d) {
    long cPtr = JPhysXAdapterJNI.NxPlane_set__SWIG_0(swigCPtr, this, nx, ny, nz, _d);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public NxPlane set(NxVec3 _normal, float _d) {
    return new NxPlane(JPhysXAdapterJNI.NxPlane_set__SWIG_1(swigCPtr, this, NxVec3.getCPtr(_normal), _normal, _d), false);
  }
  public NxPlane set_inplace(NxPlane result, NxVec3 _normal, float _d) {
    long cPtr = JPhysXAdapterJNI.NxPlane_set__SWIG_1(swigCPtr, this, NxVec3.getCPtr(_normal), _normal, _d);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public NxPlane set(NxVec3 p, NxVec3 _n) {
    return new NxPlane(JPhysXAdapterJNI.NxPlane_set__SWIG_2(swigCPtr, this, NxVec3.getCPtr(p), p, NxVec3.getCPtr(_n), _n), false);
  }
  public NxPlane set_inplace(NxPlane result, NxVec3 p, NxVec3 _n) {
    long cPtr = JPhysXAdapterJNI.NxPlane_set__SWIG_2(swigCPtr, this, NxVec3.getCPtr(p), p, NxVec3.getCPtr(_n), _n);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public NxPlane set(NxVec3 p0, NxVec3 p1, NxVec3 p2) {
    return new NxPlane(JPhysXAdapterJNI.NxPlane_set__SWIG_3(swigCPtr, this, NxVec3.getCPtr(p0), p0, NxVec3.getCPtr(p1), p1, NxVec3.getCPtr(p2), p2), false);
  }
  public NxPlane set_inplace(NxPlane result, NxVec3 p0, NxVec3 p1, NxVec3 p2) {
    long cPtr = JPhysXAdapterJNI.NxPlane_set__SWIG_3(swigCPtr, this, NxVec3.getCPtr(p0), p0, NxVec3.getCPtr(p1), p1, NxVec3.getCPtr(p2), p2);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public float distance(NxVec3 p) {
    return JPhysXAdapterJNI.NxPlane_distance(swigCPtr, this, NxVec3.getCPtr(p), p);
  }


  public boolean belongs(NxVec3 p) {
    return JPhysXAdapterJNI.NxPlane_belongs(swigCPtr, this, NxVec3.getCPtr(p), p);
  }


  public NxVec3 project(NxVec3 p) {
    return new NxVec3(JPhysXAdapterJNI.NxPlane_project(swigCPtr, this, NxVec3.getCPtr(p), p), true);
  }
  public NxVec3 project_inplace(NxVec3 result, NxVec3 p) {
    long cPtr = JPhysXAdapterJNI.NxPlane_project(swigCPtr, this, NxVec3.getCPtr(p), p);
        if(cPtr != 0){
            result.replaceCPart(cPtr, true);
            return result;
        }
        return null;
  }



  public NxVec3 pointInPlane() {
    return new NxVec3(JPhysXAdapterJNI.NxPlane_pointInPlane(swigCPtr, this), true);
  }
  public NxVec3 pointInPlane_inplace(NxVec3 result) {
    long cPtr = JPhysXAdapterJNI.NxPlane_pointInPlane(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, true);
            return result;
        }
        return null;
  }



  public void normalize() {
    JPhysXAdapterJNI.NxPlane_normalize(swigCPtr, this);
  }


  public void transform(NxMat34 transform, NxPlane transformed) {
    JPhysXAdapterJNI.NxPlane_transform(swigCPtr, this, NxMat34.getCPtr(transform), transform, NxPlane.getCPtr(transformed), transformed);
  }


  public void inverseTransform(NxMat34 transform, NxPlane transformed) {
    JPhysXAdapterJNI.NxPlane_inverseTransform(swigCPtr, this, NxMat34.getCPtr(transform), transform, NxPlane.getCPtr(transformed), transformed);
  }


  public void setNormal(NxVec3 value) {
    JPhysXAdapterJNI.NxPlane_normal_set(swigCPtr, this, NxVec3.getCPtr(value), value);
  }


  public NxVec3 getNormal() {
    long cPtr = JPhysXAdapterJNI.NxPlane_normal_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxVec3(cPtr, false);
  }
  public NxVec3 getNormal_inplace(NxVec3 result) {
    long cPtr = JPhysXAdapterJNI.NxPlane_normal_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setD(float value) {
    JPhysXAdapterJNI.NxPlane_d_set(swigCPtr, this, value);
  }


  public float getD() {
    return JPhysXAdapterJNI.NxPlane_d_get(swigCPtr, this);
  }


}

