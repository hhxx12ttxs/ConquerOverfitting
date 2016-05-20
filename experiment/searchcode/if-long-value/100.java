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
public class NxFluidEmitterDesc {
  private long swigCPtr;
  boolean swigCMemOwn;
  
  /**
   * Default java constructor.
   * @param constructionMarker marks that this is java constructor. Clashing with C++ constructors is unlikely.
   */
  NxFluidEmitterDesc(Boolean constructionMarker){}

  NxFluidEmitterDesc(long cPtr, boolean cMemoryOwn) {
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
  public static long getCPtr(NxFluidEmitterDesc obj) {
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
      JPhysXAdapterJNI.delete_NxFluidEmitterDesc(swigCPtr);
    }

    swigCPtr = 0;
  }

  public boolean equalCPtr(NxFluidEmitterDesc obj) {
    return obj.swigCPtr == this.swigCPtr;
  }


  public void setRelPose(NxMat34 value) {
    JPhysXAdapterJNI.NxFluidEmitterDesc_relPose_set(swigCPtr, this, NxMat34.getCPtr(value), value);
  }


  public NxMat34 getRelPose() {
    long cPtr = JPhysXAdapterJNI.NxFluidEmitterDesc_relPose_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxMat34(cPtr, false);
  }
  public NxMat34 getRelPose_inplace(NxMat34 result) {
    long cPtr = JPhysXAdapterJNI.NxFluidEmitterDesc_relPose_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setFrameShape(NxShape value) {
    JPhysXAdapterJNI.NxFluidEmitterDesc_frameShape_set(swigCPtr, this, NxShape.getCPtr(value), value);
  }


  public NxShape getFrameShape() {
    long cPtr = JPhysXAdapterJNI.NxFluidEmitterDesc_frameShape_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxShape(cPtr, false);
  }
  public NxShape getFrameShape_inplace(NxShape result) {
    long cPtr = JPhysXAdapterJNI.NxFluidEmitterDesc_frameShape_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setType(long value) {
    JPhysXAdapterJNI.NxFluidEmitterDesc_type_set(swigCPtr, this, value);
  }


  public long getType() {
    return JPhysXAdapterJNI.NxFluidEmitterDesc_type_get(swigCPtr, this);
  }


  public void setMaxParticles(long value) {
    JPhysXAdapterJNI.NxFluidEmitterDesc_maxParticles_set(swigCPtr, this, value);
  }


  public long getMaxParticles() {
    return JPhysXAdapterJNI.NxFluidEmitterDesc_maxParticles_get(swigCPtr, this);
  }


  public void setShape(long value) {
    JPhysXAdapterJNI.NxFluidEmitterDesc_shape_set(swigCPtr, this, value);
  }


  public long getShape() {
    return JPhysXAdapterJNI.NxFluidEmitterDesc_shape_get(swigCPtr, this);
  }


  public void setDimensionX(float value) {
    JPhysXAdapterJNI.NxFluidEmitterDesc_dimensionX_set(swigCPtr, this, value);
  }


  public float getDimensionX() {
    return JPhysXAdapterJNI.NxFluidEmitterDesc_dimensionX_get(swigCPtr, this);
  }


  public void setDimensionY(float value) {
    JPhysXAdapterJNI.NxFluidEmitterDesc_dimensionY_set(swigCPtr, this, value);
  }


  public float getDimensionY() {
    return JPhysXAdapterJNI.NxFluidEmitterDesc_dimensionY_get(swigCPtr, this);
  }


  public void setRandomPos(NxVec3 value) {
    JPhysXAdapterJNI.NxFluidEmitterDesc_randomPos_set(swigCPtr, this, NxVec3.getCPtr(value), value);
  }


  public NxVec3 getRandomPos() {
    long cPtr = JPhysXAdapterJNI.NxFluidEmitterDesc_randomPos_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxVec3(cPtr, false);
  }
  public NxVec3 getRandomPos_inplace(NxVec3 result) {
    long cPtr = JPhysXAdapterJNI.NxFluidEmitterDesc_randomPos_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setRandomAngle(float value) {
    JPhysXAdapterJNI.NxFluidEmitterDesc_randomAngle_set(swigCPtr, this, value);
  }


  public float getRandomAngle() {
    return JPhysXAdapterJNI.NxFluidEmitterDesc_randomAngle_get(swigCPtr, this);
  }


  public void setFluidVelocityMagnitude(float value) {
    JPhysXAdapterJNI.NxFluidEmitterDesc_fluidVelocityMagnitude_set(swigCPtr, this, value);
  }


  public float getFluidVelocityMagnitude() {
    return JPhysXAdapterJNI.NxFluidEmitterDesc_fluidVelocityMagnitude_get(swigCPtr, this);
  }


  public void setRate(float value) {
    JPhysXAdapterJNI.NxFluidEmitterDesc_rate_set(swigCPtr, this, value);
  }


  public float getRate() {
    return JPhysXAdapterJNI.NxFluidEmitterDesc_rate_get(swigCPtr, this);
  }


  public void setParticleLifetime(float value) {
    JPhysXAdapterJNI.NxFluidEmitterDesc_particleLifetime_set(swigCPtr, this, value);
  }


  public float getParticleLifetime() {
    return JPhysXAdapterJNI.NxFluidEmitterDesc_particleLifetime_get(swigCPtr, this);
  }


  public void setRepulsionCoefficient(float value) {
    JPhysXAdapterJNI.NxFluidEmitterDesc_repulsionCoefficient_set(swigCPtr, this, value);
  }


  public float getRepulsionCoefficient() {
    return JPhysXAdapterJNI.NxFluidEmitterDesc_repulsionCoefficient_get(swigCPtr, this);
  }


  public void setFlags(long value) {
    JPhysXAdapterJNI.NxFluidEmitterDesc_flags_set(swigCPtr, this, value);
  }


  public long getFlags() {
    return JPhysXAdapterJNI.NxFluidEmitterDesc_flags_get(swigCPtr, this);
  }


  public void setUserData(SWIGTYPE_p_void value) {
    JPhysXAdapterJNI.NxFluidEmitterDesc_userData_set(swigCPtr, this, SWIGTYPE_p_void.getCPtr(value));
  }


  public SWIGTYPE_p_void getUserData() {
    long cPtr = JPhysXAdapterJNI.NxFluidEmitterDesc_userData_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }


  public void setName(String value) {
    JPhysXAdapterJNI.NxFluidEmitterDesc_name_set(swigCPtr, this, value);
  }


  public String getName() {
    return JPhysXAdapterJNI.NxFluidEmitterDesc_name_get(swigCPtr, this);
  }


  public void setToDefault() {
    JPhysXAdapterJNI.NxFluidEmitterDesc_setToDefault(swigCPtr, this);
  }


  public boolean isValid() {
    return JPhysXAdapterJNI.NxFluidEmitterDesc_isValid(swigCPtr, this);
  }


  public NxFluidEmitterDesc() {
    this(JPhysXAdapterJNI.new_NxFluidEmitterDesc(), true);
  }


}

