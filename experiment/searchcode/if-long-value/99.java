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
public class NxSceneDesc {
  private long swigCPtr;
  boolean swigCMemOwn;
  
  /**
   * Default java constructor.
   * @param constructionMarker marks that this is java constructor. Clashing with C++ constructors is unlikely.
   */
  NxSceneDesc(Boolean constructionMarker){}

  NxSceneDesc(long cPtr, boolean cMemoryOwn) {
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
  public static long getCPtr(NxSceneDesc obj) {
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
      JPhysXAdapterJNI.delete_NxSceneDesc(swigCPtr);
    }

    swigCPtr = 0;
  }

  public boolean equalCPtr(NxSceneDesc obj) {
    return obj.swigCPtr == this.swigCPtr;
  }


  public void setGravity(NxVec3 value) {
    JPhysXAdapterJNI.NxSceneDesc_gravity_set(swigCPtr, this, NxVec3.getCPtr(value), value);
  }


  public NxVec3 getGravity() {
    long cPtr = JPhysXAdapterJNI.NxSceneDesc_gravity_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxVec3(cPtr, false);
  }
  public NxVec3 getGravity_inplace(NxVec3 result) {
    long cPtr = JPhysXAdapterJNI.NxSceneDesc_gravity_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setUserNotify(NxUserNotify value) {
    JPhysXAdapterJNI.NxSceneDesc_userNotify_set(swigCPtr, this, NxUserNotify.getCPtr(value), value);
  }


  public NxUserNotify getUserNotify() {
    long cPtr = JPhysXAdapterJNI.NxSceneDesc_userNotify_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxUserNotify(cPtr, false);
  }
  public NxUserNotify getUserNotify_inplace(NxUserNotify result) {
    long cPtr = JPhysXAdapterJNI.NxSceneDesc_userNotify_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setFluidUserNotify(SWIGTYPE_p_NxFluidUserNotify value) {
    JPhysXAdapterJNI.NxSceneDesc_fluidUserNotify_set(swigCPtr, this, SWIGTYPE_p_NxFluidUserNotify.getCPtr(value));
  }


  public SWIGTYPE_p_NxFluidUserNotify getFluidUserNotify() {
    long cPtr = JPhysXAdapterJNI.NxSceneDesc_fluidUserNotify_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_NxFluidUserNotify(cPtr, false);
  }


  public void setClothUserNotify(SWIGTYPE_p_NxClothUserNotify value) {
    JPhysXAdapterJNI.NxSceneDesc_clothUserNotify_set(swigCPtr, this, SWIGTYPE_p_NxClothUserNotify.getCPtr(value));
  }


  public SWIGTYPE_p_NxClothUserNotify getClothUserNotify() {
    long cPtr = JPhysXAdapterJNI.NxSceneDesc_clothUserNotify_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_NxClothUserNotify(cPtr, false);
  }


  public void setSoftBodyUserNotify(SWIGTYPE_p_NxSoftBodyUserNotify value) {
    JPhysXAdapterJNI.NxSceneDesc_softBodyUserNotify_set(swigCPtr, this, SWIGTYPE_p_NxSoftBodyUserNotify.getCPtr(value));
  }


  public SWIGTYPE_p_NxSoftBodyUserNotify getSoftBodyUserNotify() {
    long cPtr = JPhysXAdapterJNI.NxSceneDesc_softBodyUserNotify_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_NxSoftBodyUserNotify(cPtr, false);
  }


  public void setUserContactModify(NxUserContactModify value) {
    JPhysXAdapterJNI.NxSceneDesc_userContactModify_set(swigCPtr, this, NxUserContactModify.getCPtr(value), value);
  }


  public NxUserContactModify getUserContactModify() {
    long cPtr = JPhysXAdapterJNI.NxSceneDesc_userContactModify_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxUserContactModify(cPtr, false);
  }
  public NxUserContactModify getUserContactModify_inplace(NxUserContactModify result) {
    long cPtr = JPhysXAdapterJNI.NxSceneDesc_userContactModify_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setUserTriggerReport(NxUserTriggerReport value) {
    JPhysXAdapterJNI.NxSceneDesc_userTriggerReport_set(swigCPtr, this, NxUserTriggerReport.getCPtr(value), value);
  }


  public NxUserTriggerReport getUserTriggerReport() {
    long cPtr = JPhysXAdapterJNI.NxSceneDesc_userTriggerReport_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxUserTriggerReport(cPtr, false);
  }
  public NxUserTriggerReport getUserTriggerReport_inplace(NxUserTriggerReport result) {
    long cPtr = JPhysXAdapterJNI.NxSceneDesc_userTriggerReport_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setUserContactReport(NxUserContactReport value) {
    JPhysXAdapterJNI.NxSceneDesc_userContactReport_set(swigCPtr, this, NxUserContactReport.getCPtr(value), value);
  }


  public NxUserContactReport getUserContactReport() {
    long cPtr = JPhysXAdapterJNI.NxSceneDesc_userContactReport_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxUserContactReport(cPtr, false);
  }
  public NxUserContactReport getUserContactReport_inplace(NxUserContactReport result) {
    long cPtr = JPhysXAdapterJNI.NxSceneDesc_userContactReport_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setUserActorPairFiltering(NxUserActorPairFiltering value) {
    JPhysXAdapterJNI.NxSceneDesc_userActorPairFiltering_set(swigCPtr, this, NxUserActorPairFiltering.getCPtr(value), value);
  }


  public NxUserActorPairFiltering getUserActorPairFiltering() {
    long cPtr = JPhysXAdapterJNI.NxSceneDesc_userActorPairFiltering_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxUserActorPairFiltering(cPtr, false);
  }
  public NxUserActorPairFiltering getUserActorPairFiltering_inplace(NxUserActorPairFiltering result) {
    long cPtr = JPhysXAdapterJNI.NxSceneDesc_userActorPairFiltering_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setMaxTimestep(float value) {
    JPhysXAdapterJNI.NxSceneDesc_maxTimestep_set(swigCPtr, this, value);
  }


  public float getMaxTimestep() {
    return JPhysXAdapterJNI.NxSceneDesc_maxTimestep_get(swigCPtr, this);
  }


  public void setMaxIter(long value) {
    JPhysXAdapterJNI.NxSceneDesc_maxIter_set(swigCPtr, this, value);
  }


  public long getMaxIter() {
    return JPhysXAdapterJNI.NxSceneDesc_maxIter_get(swigCPtr, this);
  }


  public void setTimeStepMethod(int value) {
    JPhysXAdapterJNI.NxSceneDesc_timeStepMethod_set(swigCPtr, this, value);
  }


  public int getTimeStepMethod() {
    return JPhysXAdapterJNI.NxSceneDesc_timeStepMethod_get(swigCPtr, this);
  }


  public void setMaxBounds(NxBounds3 value) {
    JPhysXAdapterJNI.NxSceneDesc_maxBounds_set(swigCPtr, this, NxBounds3.getCPtr(value), value);
  }


  public NxBounds3 getMaxBounds() {
    long cPtr = JPhysXAdapterJNI.NxSceneDesc_maxBounds_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxBounds3(cPtr, false);
  }
  public NxBounds3 getMaxBounds_inplace(NxBounds3 result) {
    long cPtr = JPhysXAdapterJNI.NxSceneDesc_maxBounds_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setLimits(NxSceneLimits value) {
    JPhysXAdapterJNI.NxSceneDesc_limits_set(swigCPtr, this, NxSceneLimits.getCPtr(value), value);
  }


  public NxSceneLimits getLimits() {
    long cPtr = JPhysXAdapterJNI.NxSceneDesc_limits_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxSceneLimits(cPtr, false);
  }
  public NxSceneLimits getLimits_inplace(NxSceneLimits result) {
    long cPtr = JPhysXAdapterJNI.NxSceneDesc_limits_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setSimType(int value) {
    JPhysXAdapterJNI.NxSceneDesc_simType_set(swigCPtr, this, value);
  }


  public int getSimType() {
    return JPhysXAdapterJNI.NxSceneDesc_simType_get(swigCPtr, this);
  }


  public void setGroundPlane(int value) {
    JPhysXAdapterJNI.NxSceneDesc_groundPlane_set(swigCPtr, this, value);
  }


  public int getGroundPlane() {
    return JPhysXAdapterJNI.NxSceneDesc_groundPlane_get(swigCPtr, this);
  }


  public void setBoundsPlanes(int value) {
    JPhysXAdapterJNI.NxSceneDesc_boundsPlanes_set(swigCPtr, this, value);
  }


  public int getBoundsPlanes() {
    return JPhysXAdapterJNI.NxSceneDesc_boundsPlanes_get(swigCPtr, this);
  }


  public void setFlags(long value) {
    JPhysXAdapterJNI.NxSceneDesc_flags_set(swigCPtr, this, value);
  }


  public long getFlags() {
    return JPhysXAdapterJNI.NxSceneDesc_flags_get(swigCPtr, this);
  }


  public void setCustomScheduler(SWIGTYPE_p_NxUserScheduler value) {
    JPhysXAdapterJNI.NxSceneDesc_customScheduler_set(swigCPtr, this, SWIGTYPE_p_NxUserScheduler.getCPtr(value));
  }


  public SWIGTYPE_p_NxUserScheduler getCustomScheduler() {
    long cPtr = JPhysXAdapterJNI.NxSceneDesc_customScheduler_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_NxUserScheduler(cPtr, false);
  }


  public void setSimThreadStackSize(long value) {
    JPhysXAdapterJNI.NxSceneDesc_simThreadStackSize_set(swigCPtr, this, value);
  }


  public long getSimThreadStackSize() {
    return JPhysXAdapterJNI.NxSceneDesc_simThreadStackSize_get(swigCPtr, this);
  }


  public void setSimThreadPriority(int value) {
    JPhysXAdapterJNI.NxSceneDesc_simThreadPriority_set(swigCPtr, this, value);
  }


  public int getSimThreadPriority() {
    return JPhysXAdapterJNI.NxSceneDesc_simThreadPriority_get(swigCPtr, this);
  }


  public void setSimThreadMask(long value) {
    JPhysXAdapterJNI.NxSceneDesc_simThreadMask_set(swigCPtr, this, value);
  }


  public long getSimThreadMask() {
    return JPhysXAdapterJNI.NxSceneDesc_simThreadMask_get(swigCPtr, this);
  }


  public void setInternalThreadCount(long value) {
    JPhysXAdapterJNI.NxSceneDesc_internalThreadCount_set(swigCPtr, this, value);
  }


  public long getInternalThreadCount() {
    return JPhysXAdapterJNI.NxSceneDesc_internalThreadCount_get(swigCPtr, this);
  }


  public void setWorkerThreadStackSize(long value) {
    JPhysXAdapterJNI.NxSceneDesc_workerThreadStackSize_set(swigCPtr, this, value);
  }


  public long getWorkerThreadStackSize() {
    return JPhysXAdapterJNI.NxSceneDesc_workerThreadStackSize_get(swigCPtr, this);
  }


  public void setWorkerThreadPriority(int value) {
    JPhysXAdapterJNI.NxSceneDesc_workerThreadPriority_set(swigCPtr, this, value);
  }


  public int getWorkerThreadPriority() {
    return JPhysXAdapterJNI.NxSceneDesc_workerThreadPriority_get(swigCPtr, this);
  }


  public void setThreadMask(long value) {
    JPhysXAdapterJNI.NxSceneDesc_threadMask_set(swigCPtr, this, value);
  }


  public long getThreadMask() {
    return JPhysXAdapterJNI.NxSceneDesc_threadMask_get(swigCPtr, this);
  }


  public void setBackgroundThreadCount(long value) {
    JPhysXAdapterJNI.NxSceneDesc_backgroundThreadCount_set(swigCPtr, this, value);
  }


  public long getBackgroundThreadCount() {
    return JPhysXAdapterJNI.NxSceneDesc_backgroundThreadCount_get(swigCPtr, this);
  }


  public void setBackgroundThreadMask(long value) {
    JPhysXAdapterJNI.NxSceneDesc_backgroundThreadMask_set(swigCPtr, this, value);
  }


  public long getBackgroundThreadMask() {
    return JPhysXAdapterJNI.NxSceneDesc_backgroundThreadMask_get(swigCPtr, this);
  }


  public void setUpAxis(long value) {
    JPhysXAdapterJNI.NxSceneDesc_upAxis_set(swigCPtr, this, value);
  }


  public long getUpAxis() {
    return JPhysXAdapterJNI.NxSceneDesc_upAxis_get(swigCPtr, this);
  }


  public void setSubdivisionLevel(long value) {
    JPhysXAdapterJNI.NxSceneDesc_subdivisionLevel_set(swigCPtr, this, value);
  }


  public long getSubdivisionLevel() {
    return JPhysXAdapterJNI.NxSceneDesc_subdivisionLevel_get(swigCPtr, this);
  }


  public void setStaticStructure(int value) {
    JPhysXAdapterJNI.NxSceneDesc_staticStructure_set(swigCPtr, this, value);
  }


  public int getStaticStructure() {
    return JPhysXAdapterJNI.NxSceneDesc_staticStructure_get(swigCPtr, this);
  }


  public void setDynamicStructure(int value) {
    JPhysXAdapterJNI.NxSceneDesc_dynamicStructure_set(swigCPtr, this, value);
  }


  public int getDynamicStructure() {
    return JPhysXAdapterJNI.NxSceneDesc_dynamicStructure_get(swigCPtr, this);
  }


  public void setUserData(SWIGTYPE_p_void value) {
    JPhysXAdapterJNI.NxSceneDesc_userData_set(swigCPtr, this, SWIGTYPE_p_void.getCPtr(value));
  }


  public SWIGTYPE_p_void getUserData() {
    long cPtr = JPhysXAdapterJNI.NxSceneDesc_userData_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }


  public void setBpType(int value) {
    JPhysXAdapterJNI.NxSceneDesc_bpType_set(swigCPtr, this, value);
  }


  public int getBpType() {
    return JPhysXAdapterJNI.NxSceneDesc_bpType_get(swigCPtr, this);
  }


  public void setNbGridCellsX(long value) {
    JPhysXAdapterJNI.NxSceneDesc_nbGridCellsX_set(swigCPtr, this, value);
  }


  public long getNbGridCellsX() {
    return JPhysXAdapterJNI.NxSceneDesc_nbGridCellsX_get(swigCPtr, this);
  }


  public void setNbGridCellsY(long value) {
    JPhysXAdapterJNI.NxSceneDesc_nbGridCellsY_set(swigCPtr, this, value);
  }


  public long getNbGridCellsY() {
    return JPhysXAdapterJNI.NxSceneDesc_nbGridCellsY_get(swigCPtr, this);
  }


  public void setSolverBatchSize(long value) {
    JPhysXAdapterJNI.NxSceneDesc_solverBatchSize_set(swigCPtr, this, value);
  }


  public long getSolverBatchSize() {
    return JPhysXAdapterJNI.NxSceneDesc_solverBatchSize_get(swigCPtr, this);
  }


  public NxSceneDesc() {
    this(JPhysXAdapterJNI.new_NxSceneDesc(), true);
  }


  public void setToDefault() {
    JPhysXAdapterJNI.NxSceneDesc_setToDefault(swigCPtr, this);
  }


  public boolean isValid() {
    return JPhysXAdapterJNI.NxSceneDesc_isValid(swigCPtr, this);
  }


}

