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
public class NxSoftBodyDesc {
  private long swigCPtr;
  boolean swigCMemOwn;
  
  /**
   * Default java constructor.
   * @param constructionMarker marks that this is java constructor. Clashing with C++ constructors is unlikely.
   */
  NxSoftBodyDesc(Boolean constructionMarker){}

  NxSoftBodyDesc(long cPtr, boolean cMemoryOwn) {
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
  public static long getCPtr(NxSoftBodyDesc obj) {
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
      JPhysXAdapterJNI.delete_NxSoftBodyDesc(swigCPtr);
    }

    swigCPtr = 0;
  }

  public boolean equalCPtr(NxSoftBodyDesc obj) {
    return obj.swigCPtr == this.swigCPtr;
  }


  public void setSoftBodyMesh(NxSoftBodyMesh value) {
    JPhysXAdapterJNI.NxSoftBodyDesc_softBodyMesh_set(swigCPtr, this, NxSoftBodyMesh.getCPtr(value), value);
  }


  public NxSoftBodyMesh getSoftBodyMesh() {
    long cPtr = JPhysXAdapterJNI.NxSoftBodyDesc_softBodyMesh_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxSoftBodyMesh(cPtr, false);
  }
  public NxSoftBodyMesh getSoftBodyMesh_inplace(NxSoftBodyMesh result) {
    long cPtr = JPhysXAdapterJNI.NxSoftBodyDesc_softBodyMesh_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setGlobalPose(NxMat34 value) {
    JPhysXAdapterJNI.NxSoftBodyDesc_globalPose_set(swigCPtr, this, NxMat34.getCPtr(value), value);
  }


  public NxMat34 getGlobalPose() {
    long cPtr = JPhysXAdapterJNI.NxSoftBodyDesc_globalPose_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxMat34(cPtr, false);
  }
  public NxMat34 getGlobalPose_inplace(NxMat34 result) {
    long cPtr = JPhysXAdapterJNI.NxSoftBodyDesc_globalPose_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setParticleRadius(float value) {
    JPhysXAdapterJNI.NxSoftBodyDesc_particleRadius_set(swigCPtr, this, value);
  }


  public float getParticleRadius() {
    return JPhysXAdapterJNI.NxSoftBodyDesc_particleRadius_get(swigCPtr, this);
  }


  public void setDensity(float value) {
    JPhysXAdapterJNI.NxSoftBodyDesc_density_set(swigCPtr, this, value);
  }


  public float getDensity() {
    return JPhysXAdapterJNI.NxSoftBodyDesc_density_get(swigCPtr, this);
  }


  public void setVolumeStiffness(float value) {
    JPhysXAdapterJNI.NxSoftBodyDesc_volumeStiffness_set(swigCPtr, this, value);
  }


  public float getVolumeStiffness() {
    return JPhysXAdapterJNI.NxSoftBodyDesc_volumeStiffness_get(swigCPtr, this);
  }


  public void setStretchingStiffness(float value) {
    JPhysXAdapterJNI.NxSoftBodyDesc_stretchingStiffness_set(swigCPtr, this, value);
  }


  public float getStretchingStiffness() {
    return JPhysXAdapterJNI.NxSoftBodyDesc_stretchingStiffness_get(swigCPtr, this);
  }


  public void setDampingCoefficient(float value) {
    JPhysXAdapterJNI.NxSoftBodyDesc_dampingCoefficient_set(swigCPtr, this, value);
  }


  public float getDampingCoefficient() {
    return JPhysXAdapterJNI.NxSoftBodyDesc_dampingCoefficient_get(swigCPtr, this);
  }


  public void setFriction(float value) {
    JPhysXAdapterJNI.NxSoftBodyDesc_friction_set(swigCPtr, this, value);
  }


  public float getFriction() {
    return JPhysXAdapterJNI.NxSoftBodyDesc_friction_get(swigCPtr, this);
  }


  public void setTearFactor(float value) {
    JPhysXAdapterJNI.NxSoftBodyDesc_tearFactor_set(swigCPtr, this, value);
  }


  public float getTearFactor() {
    return JPhysXAdapterJNI.NxSoftBodyDesc_tearFactor_get(swigCPtr, this);
  }


  public void setCollisionResponseCoefficient(float value) {
    JPhysXAdapterJNI.NxSoftBodyDesc_collisionResponseCoefficient_set(swigCPtr, this, value);
  }


  public float getCollisionResponseCoefficient() {
    return JPhysXAdapterJNI.NxSoftBodyDesc_collisionResponseCoefficient_get(swigCPtr, this);
  }


  public void setAttachmentResponseCoefficient(float value) {
    JPhysXAdapterJNI.NxSoftBodyDesc_attachmentResponseCoefficient_set(swigCPtr, this, value);
  }


  public float getAttachmentResponseCoefficient() {
    return JPhysXAdapterJNI.NxSoftBodyDesc_attachmentResponseCoefficient_get(swigCPtr, this);
  }


  public void setAttachmentTearFactor(float value) {
    JPhysXAdapterJNI.NxSoftBodyDesc_attachmentTearFactor_set(swigCPtr, this, value);
  }


  public float getAttachmentTearFactor() {
    return JPhysXAdapterJNI.NxSoftBodyDesc_attachmentTearFactor_get(swigCPtr, this);
  }


  public void setToFluidResponseCoefficient(float value) {
    JPhysXAdapterJNI.NxSoftBodyDesc_toFluidResponseCoefficient_set(swigCPtr, this, value);
  }


  public float getToFluidResponseCoefficient() {
    return JPhysXAdapterJNI.NxSoftBodyDesc_toFluidResponseCoefficient_get(swigCPtr, this);
  }


  public void setFromFluidResponseCoefficient(float value) {
    JPhysXAdapterJNI.NxSoftBodyDesc_fromFluidResponseCoefficient_set(swigCPtr, this, value);
  }


  public float getFromFluidResponseCoefficient() {
    return JPhysXAdapterJNI.NxSoftBodyDesc_fromFluidResponseCoefficient_get(swigCPtr, this);
  }


  public void setMinAdhereVelocity(float value) {
    JPhysXAdapterJNI.NxSoftBodyDesc_minAdhereVelocity_set(swigCPtr, this, value);
  }


  public float getMinAdhereVelocity() {
    return JPhysXAdapterJNI.NxSoftBodyDesc_minAdhereVelocity_get(swigCPtr, this);
  }


  public void setSolverIterations(long value) {
    JPhysXAdapterJNI.NxSoftBodyDesc_solverIterations_set(swigCPtr, this, value);
  }


  public long getSolverIterations() {
    return JPhysXAdapterJNI.NxSoftBodyDesc_solverIterations_get(swigCPtr, this);
  }


  public void setExternalAcceleration(NxVec3 value) {
    JPhysXAdapterJNI.NxSoftBodyDesc_externalAcceleration_set(swigCPtr, this, NxVec3.getCPtr(value), value);
  }


  public NxVec3 getExternalAcceleration() {
    long cPtr = JPhysXAdapterJNI.NxSoftBodyDesc_externalAcceleration_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxVec3(cPtr, false);
  }
  public NxVec3 getExternalAcceleration_inplace(NxVec3 result) {
    long cPtr = JPhysXAdapterJNI.NxSoftBodyDesc_externalAcceleration_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setWakeUpCounter(float value) {
    JPhysXAdapterJNI.NxSoftBodyDesc_wakeUpCounter_set(swigCPtr, this, value);
  }


  public float getWakeUpCounter() {
    return JPhysXAdapterJNI.NxSoftBodyDesc_wakeUpCounter_get(swigCPtr, this);
  }


  public void setSleepLinearVelocity(float value) {
    JPhysXAdapterJNI.NxSoftBodyDesc_sleepLinearVelocity_set(swigCPtr, this, value);
  }


  public float getSleepLinearVelocity() {
    return JPhysXAdapterJNI.NxSoftBodyDesc_sleepLinearVelocity_get(swigCPtr, this);
  }


  public void setMeshData(NxMeshData value) {
    JPhysXAdapterJNI.NxSoftBodyDesc_meshData_set(swigCPtr, this, NxMeshData.getCPtr(value), value);
  }


  public NxMeshData getMeshData() {
    long cPtr = JPhysXAdapterJNI.NxSoftBodyDesc_meshData_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxMeshData(cPtr, false);
  }
  public NxMeshData getMeshData_inplace(NxMeshData result) {
    long cPtr = JPhysXAdapterJNI.NxSoftBodyDesc_meshData_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setSplitPairData(NxSoftBodySplitPairData value) {
    JPhysXAdapterJNI.NxSoftBodyDesc_splitPairData_set(swigCPtr, this, NxSoftBodySplitPairData.getCPtr(value), value);
  }


  public NxSoftBodySplitPairData getSplitPairData() {
    long cPtr = JPhysXAdapterJNI.NxSoftBodyDesc_splitPairData_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxSoftBodySplitPairData(cPtr, false);
  }
  public NxSoftBodySplitPairData getSplitPairData_inplace(NxSoftBodySplitPairData result) {
    long cPtr = JPhysXAdapterJNI.NxSoftBodyDesc_splitPairData_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setCollisionGroup(int value) {
    JPhysXAdapterJNI.NxSoftBodyDesc_collisionGroup_set(swigCPtr, this, value);
  }


  public int getCollisionGroup() {
    return JPhysXAdapterJNI.NxSoftBodyDesc_collisionGroup_get(swigCPtr, this);
  }


  public void setGroupsMask(NxGroupsMask value) {
    JPhysXAdapterJNI.NxSoftBodyDesc_groupsMask_set(swigCPtr, this, NxGroupsMask.getCPtr(value), value);
  }


  public NxGroupsMask getGroupsMask() {
    long cPtr = JPhysXAdapterJNI.NxSoftBodyDesc_groupsMask_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxGroupsMask(cPtr, false);
  }
  public NxGroupsMask getGroupsMask_inplace(NxGroupsMask result) {
    long cPtr = JPhysXAdapterJNI.NxSoftBodyDesc_groupsMask_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setForceFieldMaterial(int value) {
    JPhysXAdapterJNI.NxSoftBodyDesc_forceFieldMaterial_set(swigCPtr, this, value);
  }


  public int getForceFieldMaterial() {
    return JPhysXAdapterJNI.NxSoftBodyDesc_forceFieldMaterial_get(swigCPtr, this);
  }


  public void setValidBounds(NxBounds3 value) {
    JPhysXAdapterJNI.NxSoftBodyDesc_validBounds_set(swigCPtr, this, NxBounds3.getCPtr(value), value);
  }


  public NxBounds3 getValidBounds() {
    long cPtr = JPhysXAdapterJNI.NxSoftBodyDesc_validBounds_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxBounds3(cPtr, false);
  }
  public NxBounds3 getValidBounds_inplace(NxBounds3 result) {
    long cPtr = JPhysXAdapterJNI.NxSoftBodyDesc_validBounds_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setRelativeGridSpacing(float value) {
    JPhysXAdapterJNI.NxSoftBodyDesc_relativeGridSpacing_set(swigCPtr, this, value);
  }


  public float getRelativeGridSpacing() {
    return JPhysXAdapterJNI.NxSoftBodyDesc_relativeGridSpacing_get(swigCPtr, this);
  }


  public void setFlags(long value) {
    JPhysXAdapterJNI.NxSoftBodyDesc_flags_set(swigCPtr, this, value);
  }


  public long getFlags() {
    return JPhysXAdapterJNI.NxSoftBodyDesc_flags_get(swigCPtr, this);
  }


  public void setUserData(SWIGTYPE_p_void value) {
    JPhysXAdapterJNI.NxSoftBodyDesc_userData_set(swigCPtr, this, SWIGTYPE_p_void.getCPtr(value));
  }


  public SWIGTYPE_p_void getUserData() {
    long cPtr = JPhysXAdapterJNI.NxSoftBodyDesc_userData_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }


  public void setName(String value) {
    JPhysXAdapterJNI.NxSoftBodyDesc_name_set(swigCPtr, this, value);
  }


  public String getName() {
    return JPhysXAdapterJNI.NxSoftBodyDesc_name_get(swigCPtr, this);
  }


  public void setCompartment(NxCompartment value) {
    JPhysXAdapterJNI.NxSoftBodyDesc_compartment_set(swigCPtr, this, NxCompartment.getCPtr(value), value);
  }


  public NxCompartment getCompartment() {
    long cPtr = JPhysXAdapterJNI.NxSoftBodyDesc_compartment_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxCompartment(cPtr, false);
  }
  public NxCompartment getCompartment_inplace(NxCompartment result) {
    long cPtr = JPhysXAdapterJNI.NxSoftBodyDesc_compartment_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public NxSoftBodyDesc() {
    this(JPhysXAdapterJNI.new_NxSoftBodyDesc(), true);
  }


  public void setToDefault() {
    JPhysXAdapterJNI.NxSoftBodyDesc_setToDefault(swigCPtr, this);
  }


  public boolean isValid() {
    return JPhysXAdapterJNI.NxSoftBodyDesc_isValid(swigCPtr, this);
  }


}

