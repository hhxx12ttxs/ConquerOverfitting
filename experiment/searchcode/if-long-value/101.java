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
public class NxBodyDesc {
  private long swigCPtr;
  boolean swigCMemOwn;
  
  /**
   * Default java constructor.
   * @param constructionMarker marks that this is java constructor. Clashing with C++ constructors is unlikely.
   */
  NxBodyDesc(Boolean constructionMarker){}

  NxBodyDesc(long cPtr, boolean cMemoryOwn) {
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
  public static long getCPtr(NxBodyDesc obj) {
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
      JPhysXAdapterJNI.delete_NxBodyDesc(swigCPtr);
    }

    swigCPtr = 0;
  }

  public boolean equalCPtr(NxBodyDesc obj) {
    return obj.swigCPtr == this.swigCPtr;
  }


  public void setMassLocalPose(NxMat34 value) {
    JPhysXAdapterJNI.NxBodyDesc_massLocalPose_set(swigCPtr, this, NxMat34.getCPtr(value), value);
  }


  public NxMat34 getMassLocalPose() {
    long cPtr = JPhysXAdapterJNI.NxBodyDesc_massLocalPose_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxMat34(cPtr, false);
  }
  public NxMat34 getMassLocalPose_inplace(NxMat34 result) {
    long cPtr = JPhysXAdapterJNI.NxBodyDesc_massLocalPose_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setMassSpaceInertia(NxVec3 value) {
    JPhysXAdapterJNI.NxBodyDesc_massSpaceInertia_set(swigCPtr, this, NxVec3.getCPtr(value), value);
  }


  public NxVec3 getMassSpaceInertia() {
    long cPtr = JPhysXAdapterJNI.NxBodyDesc_massSpaceInertia_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxVec3(cPtr, false);
  }
  public NxVec3 getMassSpaceInertia_inplace(NxVec3 result) {
    long cPtr = JPhysXAdapterJNI.NxBodyDesc_massSpaceInertia_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setMass(float value) {
    JPhysXAdapterJNI.NxBodyDesc_mass_set(swigCPtr, this, value);
  }


  public float getMass() {
    return JPhysXAdapterJNI.NxBodyDesc_mass_get(swigCPtr, this);
  }


  public void setLinearVelocity(NxVec3 value) {
    JPhysXAdapterJNI.NxBodyDesc_linearVelocity_set(swigCPtr, this, NxVec3.getCPtr(value), value);
  }


  public NxVec3 getLinearVelocity() {
    long cPtr = JPhysXAdapterJNI.NxBodyDesc_linearVelocity_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxVec3(cPtr, false);
  }
  public NxVec3 getLinearVelocity_inplace(NxVec3 result) {
    long cPtr = JPhysXAdapterJNI.NxBodyDesc_linearVelocity_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setAngularVelocity(NxVec3 value) {
    JPhysXAdapterJNI.NxBodyDesc_angularVelocity_set(swigCPtr, this, NxVec3.getCPtr(value), value);
  }


  public NxVec3 getAngularVelocity() {
    long cPtr = JPhysXAdapterJNI.NxBodyDesc_angularVelocity_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxVec3(cPtr, false);
  }
  public NxVec3 getAngularVelocity_inplace(NxVec3 result) {
    long cPtr = JPhysXAdapterJNI.NxBodyDesc_angularVelocity_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setWakeUpCounter(float value) {
    JPhysXAdapterJNI.NxBodyDesc_wakeUpCounter_set(swigCPtr, this, value);
  }


  public float getWakeUpCounter() {
    return JPhysXAdapterJNI.NxBodyDesc_wakeUpCounter_get(swigCPtr, this);
  }


  public void setLinearDamping(float value) {
    JPhysXAdapterJNI.NxBodyDesc_linearDamping_set(swigCPtr, this, value);
  }


  public float getLinearDamping() {
    return JPhysXAdapterJNI.NxBodyDesc_linearDamping_get(swigCPtr, this);
  }


  public void setAngularDamping(float value) {
    JPhysXAdapterJNI.NxBodyDesc_angularDamping_set(swigCPtr, this, value);
  }


  public float getAngularDamping() {
    return JPhysXAdapterJNI.NxBodyDesc_angularDamping_get(swigCPtr, this);
  }


  public void setMaxAngularVelocity(float value) {
    JPhysXAdapterJNI.NxBodyDesc_maxAngularVelocity_set(swigCPtr, this, value);
  }


  public float getMaxAngularVelocity() {
    return JPhysXAdapterJNI.NxBodyDesc_maxAngularVelocity_get(swigCPtr, this);
  }


  public void setCCDMotionThreshold(float value) {
    JPhysXAdapterJNI.NxBodyDesc_CCDMotionThreshold_set(swigCPtr, this, value);
  }


  public float getCCDMotionThreshold() {
    return JPhysXAdapterJNI.NxBodyDesc_CCDMotionThreshold_get(swigCPtr, this);
  }


  public void setFlags(long value) {
    JPhysXAdapterJNI.NxBodyDesc_flags_set(swigCPtr, this, value);
  }


  public long getFlags() {
    return JPhysXAdapterJNI.NxBodyDesc_flags_get(swigCPtr, this);
  }


  public void setSleepLinearVelocity(float value) {
    JPhysXAdapterJNI.NxBodyDesc_sleepLinearVelocity_set(swigCPtr, this, value);
  }


  public float getSleepLinearVelocity() {
    return JPhysXAdapterJNI.NxBodyDesc_sleepLinearVelocity_get(swigCPtr, this);
  }


  public void setSleepAngularVelocity(float value) {
    JPhysXAdapterJNI.NxBodyDesc_sleepAngularVelocity_set(swigCPtr, this, value);
  }


  public float getSleepAngularVelocity() {
    return JPhysXAdapterJNI.NxBodyDesc_sleepAngularVelocity_get(swigCPtr, this);
  }


  public void setSolverIterationCount(long value) {
    JPhysXAdapterJNI.NxBodyDesc_solverIterationCount_set(swigCPtr, this, value);
  }


  public long getSolverIterationCount() {
    return JPhysXAdapterJNI.NxBodyDesc_solverIterationCount_get(swigCPtr, this);
  }


  public void setSleepEnergyThreshold(float value) {
    JPhysXAdapterJNI.NxBodyDesc_sleepEnergyThreshold_set(swigCPtr, this, value);
  }


  public float getSleepEnergyThreshold() {
    return JPhysXAdapterJNI.NxBodyDesc_sleepEnergyThreshold_get(swigCPtr, this);
  }


  public void setSleepDamping(float value) {
    JPhysXAdapterJNI.NxBodyDesc_sleepDamping_set(swigCPtr, this, value);
  }


  public float getSleepDamping() {
    return JPhysXAdapterJNI.NxBodyDesc_sleepDamping_get(swigCPtr, this);
  }


  public void setContactReportThreshold(float value) {
    JPhysXAdapterJNI.NxBodyDesc_contactReportThreshold_set(swigCPtr, this, value);
  }


  public float getContactReportThreshold() {
    return JPhysXAdapterJNI.NxBodyDesc_contactReportThreshold_get(swigCPtr, this);
  }


  public NxBodyDesc() {
    this(JPhysXAdapterJNI.new_NxBodyDesc(), true);
  }


  public void setToDefault() {
    JPhysXAdapterJNI.NxBodyDesc_setToDefault(swigCPtr, this);
  }


  public boolean isValid() {
    return JPhysXAdapterJNI.NxBodyDesc_isValid(swigCPtr, this);
  }


}

