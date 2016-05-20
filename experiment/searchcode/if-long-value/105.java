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
public class NxSphericalJointDesc extends NxJointDesc {
  private long swigCPtr;

  /**
   * Default java constructor.
   * @param constructionMarker marks that this is java constructor. Clashing with C++ constructors is unlikely.
   */
  NxSphericalJointDesc(Boolean constructionMarker){
	super(constructionMarker);
  }

  NxSphericalJointDesc(long cPtr, boolean cMemoryOwn) {
	super(Boolean.TRUE);
    setCPart(cPtr, cMemoryOwn);
  }


  /**
   * Contains functionality of object construction.
   * @param cPtr c-pointer to the native object
   * @param cMemoryOwn is native object needs to be deleted when java object is deleted?
   */
  void setCPart(long cPtr, boolean cMemoryOwn) {
      super.setCPart(JPhysXAdapterJNI.SWIGNxSphericalJointDescUpcast(cPtr), cMemoryOwn);
      swigCPtr = cPtr;
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
  public static long getCPtr(NxSphericalJointDesc obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }


  protected void finalize() {
    delete();
  }


  public synchronized void delete() {
    if(swigCPtr != 0 && swigCMemOwn) {
      swigCMemOwn = false;
      JPhysXAdapterJNI.delete_NxSphericalJointDesc(swigCPtr);
    }

    swigCPtr = 0;
    super.delete();
  }

  public boolean equalCPtr(NxSphericalJointDesc obj) {
    return obj.swigCPtr == this.swigCPtr;
  }


  public void setSwingAxis(NxVec3 value) {
    JPhysXAdapterJNI.NxSphericalJointDesc_swingAxis_set(swigCPtr, this, NxVec3.getCPtr(value), value);
  }


  public NxVec3 getSwingAxis() {
    long cPtr = JPhysXAdapterJNI.NxSphericalJointDesc_swingAxis_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxVec3(cPtr, false);
  }
  public NxVec3 getSwingAxis_inplace(NxVec3 result) {
    long cPtr = JPhysXAdapterJNI.NxSphericalJointDesc_swingAxis_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setProjectionDistance(float value) {
    JPhysXAdapterJNI.NxSphericalJointDesc_projectionDistance_set(swigCPtr, this, value);
  }


  public float getProjectionDistance() {
    return JPhysXAdapterJNI.NxSphericalJointDesc_projectionDistance_get(swigCPtr, this);
  }


  public void setTwistLimit(NxJointLimitPairDesc value) {
    JPhysXAdapterJNI.NxSphericalJointDesc_twistLimit_set(swigCPtr, this, NxJointLimitPairDesc.getCPtr(value), value);
  }


  public NxJointLimitPairDesc getTwistLimit() {
    long cPtr = JPhysXAdapterJNI.NxSphericalJointDesc_twistLimit_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxJointLimitPairDesc(cPtr, false);
  }
  public NxJointLimitPairDesc getTwistLimit_inplace(NxJointLimitPairDesc result) {
    long cPtr = JPhysXAdapterJNI.NxSphericalJointDesc_twistLimit_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setSwingLimit(NxJointLimitDesc value) {
    JPhysXAdapterJNI.NxSphericalJointDesc_swingLimit_set(swigCPtr, this, NxJointLimitDesc.getCPtr(value), value);
  }


  public NxJointLimitDesc getSwingLimit() {
    long cPtr = JPhysXAdapterJNI.NxSphericalJointDesc_swingLimit_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxJointLimitDesc(cPtr, false);
  }
  public NxJointLimitDesc getSwingLimit_inplace(NxJointLimitDesc result) {
    long cPtr = JPhysXAdapterJNI.NxSphericalJointDesc_swingLimit_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setTwistSpring(NxSpringDesc value) {
    JPhysXAdapterJNI.NxSphericalJointDesc_twistSpring_set(swigCPtr, this, NxSpringDesc.getCPtr(value), value);
  }


  public NxSpringDesc getTwistSpring() {
    long cPtr = JPhysXAdapterJNI.NxSphericalJointDesc_twistSpring_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxSpringDesc(cPtr, false);
  }
  public NxSpringDesc getTwistSpring_inplace(NxSpringDesc result) {
    long cPtr = JPhysXAdapterJNI.NxSphericalJointDesc_twistSpring_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setSwingSpring(NxSpringDesc value) {
    JPhysXAdapterJNI.NxSphericalJointDesc_swingSpring_set(swigCPtr, this, NxSpringDesc.getCPtr(value), value);
  }


  public NxSpringDesc getSwingSpring() {
    long cPtr = JPhysXAdapterJNI.NxSphericalJointDesc_swingSpring_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxSpringDesc(cPtr, false);
  }
  public NxSpringDesc getSwingSpring_inplace(NxSpringDesc result) {
    long cPtr = JPhysXAdapterJNI.NxSphericalJointDesc_swingSpring_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setJointSpring(NxSpringDesc value) {
    JPhysXAdapterJNI.NxSphericalJointDesc_jointSpring_set(swigCPtr, this, NxSpringDesc.getCPtr(value), value);
  }


  public NxSpringDesc getJointSpring() {
    long cPtr = JPhysXAdapterJNI.NxSphericalJointDesc_jointSpring_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxSpringDesc(cPtr, false);
  }
  public NxSpringDesc getJointSpring_inplace(NxSpringDesc result) {
    long cPtr = JPhysXAdapterJNI.NxSphericalJointDesc_jointSpring_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setFlags(long value) {
    JPhysXAdapterJNI.NxSphericalJointDesc_flags_set(swigCPtr, this, value);
  }


  public long getFlags() {
    return JPhysXAdapterJNI.NxSphericalJointDesc_flags_get(swigCPtr, this);
  }


  public void setProjectionMode(int value) {
    JPhysXAdapterJNI.NxSphericalJointDesc_projectionMode_set(swigCPtr, this, value);
  }


  public int getProjectionMode() {
    return JPhysXAdapterJNI.NxSphericalJointDesc_projectionMode_get(swigCPtr, this);
  }


  public NxSphericalJointDesc() {
    this(JPhysXAdapterJNI.new_NxSphericalJointDesc(), true);
  }


  public void setToDefault() {
    JPhysXAdapterJNI.NxSphericalJointDesc_setToDefault(swigCPtr, this);
  }


  public boolean isValid() {
    return JPhysXAdapterJNI.NxSphericalJointDesc_isValid(swigCPtr, this);
  }


}

