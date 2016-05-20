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
public class NxClothDesc {
  private long swigCPtr;
  boolean swigCMemOwn;
  
  /**
   * Default java constructor.
   * @param constructionMarker marks that this is java constructor. Clashing with C++ constructors is unlikely.
   */
  NxClothDesc(Boolean constructionMarker){}

  NxClothDesc(long cPtr, boolean cMemoryOwn) {
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
  public static long getCPtr(NxClothDesc obj) {
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
      JPhysXAdapterJNI.delete_NxClothDesc(swigCPtr);
    }

    swigCPtr = 0;
  }

  public boolean equalCPtr(NxClothDesc obj) {
    return obj.swigCPtr == this.swigCPtr;
  }


  public void setClothMesh(NxClothMesh value) {
    JPhysXAdapterJNI.NxClothDesc_clothMesh_set(swigCPtr, this, NxClothMesh.getCPtr(value), value);
  }


  public NxClothMesh getClothMesh() {
    long cPtr = JPhysXAdapterJNI.NxClothDesc_clothMesh_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxClothMesh(cPtr, false);
  }
  public NxClothMesh getClothMesh_inplace(NxClothMesh result) {
    long cPtr = JPhysXAdapterJNI.NxClothDesc_clothMesh_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setGlobalPose(NxMat34 value) {
    JPhysXAdapterJNI.NxClothDesc_globalPose_set(swigCPtr, this, NxMat34.getCPtr(value), value);
  }


  public NxMat34 getGlobalPose() {
    long cPtr = JPhysXAdapterJNI.NxClothDesc_globalPose_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxMat34(cPtr, false);
  }
  public NxMat34 getGlobalPose_inplace(NxMat34 result) {
    long cPtr = JPhysXAdapterJNI.NxClothDesc_globalPose_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setThickness(float value) {
    JPhysXAdapterJNI.NxClothDesc_thickness_set(swigCPtr, this, value);
  }


  public float getThickness() {
    return JPhysXAdapterJNI.NxClothDesc_thickness_get(swigCPtr, this);
  }


  public void setDensity(float value) {
    JPhysXAdapterJNI.NxClothDesc_density_set(swigCPtr, this, value);
  }


  public float getDensity() {
    return JPhysXAdapterJNI.NxClothDesc_density_get(swigCPtr, this);
  }


  public void setBendingStiffness(float value) {
    JPhysXAdapterJNI.NxClothDesc_bendingStiffness_set(swigCPtr, this, value);
  }


  public float getBendingStiffness() {
    return JPhysXAdapterJNI.NxClothDesc_bendingStiffness_get(swigCPtr, this);
  }


  public void setStretchingStiffness(float value) {
    JPhysXAdapterJNI.NxClothDesc_stretchingStiffness_set(swigCPtr, this, value);
  }


  public float getStretchingStiffness() {
    return JPhysXAdapterJNI.NxClothDesc_stretchingStiffness_get(swigCPtr, this);
  }


  public void setDampingCoefficient(float value) {
    JPhysXAdapterJNI.NxClothDesc_dampingCoefficient_set(swigCPtr, this, value);
  }


  public float getDampingCoefficient() {
    return JPhysXAdapterJNI.NxClothDesc_dampingCoefficient_get(swigCPtr, this);
  }


  public void setFriction(float value) {
    JPhysXAdapterJNI.NxClothDesc_friction_set(swigCPtr, this, value);
  }


  public float getFriction() {
    return JPhysXAdapterJNI.NxClothDesc_friction_get(swigCPtr, this);
  }


  public void setPressure(float value) {
    JPhysXAdapterJNI.NxClothDesc_pressure_set(swigCPtr, this, value);
  }


  public float getPressure() {
    return JPhysXAdapterJNI.NxClothDesc_pressure_get(swigCPtr, this);
  }


  public void setTearFactor(float value) {
    JPhysXAdapterJNI.NxClothDesc_tearFactor_set(swigCPtr, this, value);
  }


  public float getTearFactor() {
    return JPhysXAdapterJNI.NxClothDesc_tearFactor_get(swigCPtr, this);
  }


  public void setCollisionResponseCoefficient(float value) {
    JPhysXAdapterJNI.NxClothDesc_collisionResponseCoefficient_set(swigCPtr, this, value);
  }


  public float getCollisionResponseCoefficient() {
    return JPhysXAdapterJNI.NxClothDesc_collisionResponseCoefficient_get(swigCPtr, this);
  }


  public void setAttachmentResponseCoefficient(float value) {
    JPhysXAdapterJNI.NxClothDesc_attachmentResponseCoefficient_set(swigCPtr, this, value);
  }


  public float getAttachmentResponseCoefficient() {
    return JPhysXAdapterJNI.NxClothDesc_attachmentResponseCoefficient_get(swigCPtr, this);
  }


  public void setAttachmentTearFactor(float value) {
    JPhysXAdapterJNI.NxClothDesc_attachmentTearFactor_set(swigCPtr, this, value);
  }


  public float getAttachmentTearFactor() {
    return JPhysXAdapterJNI.NxClothDesc_attachmentTearFactor_get(swigCPtr, this);
  }


  public void setToFluidResponseCoefficient(float value) {
    JPhysXAdapterJNI.NxClothDesc_toFluidResponseCoefficient_set(swigCPtr, this, value);
  }


  public float getToFluidResponseCoefficient() {
    return JPhysXAdapterJNI.NxClothDesc_toFluidResponseCoefficient_get(swigCPtr, this);
  }


  public void setFromFluidResponseCoefficient(float value) {
    JPhysXAdapterJNI.NxClothDesc_fromFluidResponseCoefficient_set(swigCPtr, this, value);
  }


  public float getFromFluidResponseCoefficient() {
    return JPhysXAdapterJNI.NxClothDesc_fromFluidResponseCoefficient_get(swigCPtr, this);
  }


  public void setMinAdhereVelocity(float value) {
    JPhysXAdapterJNI.NxClothDesc_minAdhereVelocity_set(swigCPtr, this, value);
  }


  public float getMinAdhereVelocity() {
    return JPhysXAdapterJNI.NxClothDesc_minAdhereVelocity_get(swigCPtr, this);
  }


  public void setSolverIterations(long value) {
    JPhysXAdapterJNI.NxClothDesc_solverIterations_set(swigCPtr, this, value);
  }


  public long getSolverIterations() {
    return JPhysXAdapterJNI.NxClothDesc_solverIterations_get(swigCPtr, this);
  }


  public void setExternalAcceleration(NxVec3 value) {
    JPhysXAdapterJNI.NxClothDesc_externalAcceleration_set(swigCPtr, this, NxVec3.getCPtr(value), value);
  }


  public NxVec3 getExternalAcceleration() {
    long cPtr = JPhysXAdapterJNI.NxClothDesc_externalAcceleration_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxVec3(cPtr, false);
  }
  public NxVec3 getExternalAcceleration_inplace(NxVec3 result) {
    long cPtr = JPhysXAdapterJNI.NxClothDesc_externalAcceleration_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setWindAcceleration(NxVec3 value) {
    JPhysXAdapterJNI.NxClothDesc_windAcceleration_set(swigCPtr, this, NxVec3.getCPtr(value), value);
  }


  public NxVec3 getWindAcceleration() {
    long cPtr = JPhysXAdapterJNI.NxClothDesc_windAcceleration_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxVec3(cPtr, false);
  }
  public NxVec3 getWindAcceleration_inplace(NxVec3 result) {
    long cPtr = JPhysXAdapterJNI.NxClothDesc_windAcceleration_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setWakeUpCounter(float value) {
    JPhysXAdapterJNI.NxClothDesc_wakeUpCounter_set(swigCPtr, this, value);
  }


  public float getWakeUpCounter() {
    return JPhysXAdapterJNI.NxClothDesc_wakeUpCounter_get(swigCPtr, this);
  }


  public void setSleepLinearVelocity(float value) {
    JPhysXAdapterJNI.NxClothDesc_sleepLinearVelocity_set(swigCPtr, this, value);
  }


  public float getSleepLinearVelocity() {
    return JPhysXAdapterJNI.NxClothDesc_sleepLinearVelocity_get(swigCPtr, this);
  }


  public void setMeshData(NxMeshData value) {
    JPhysXAdapterJNI.NxClothDesc_meshData_set(swigCPtr, this, NxMeshData.getCPtr(value), value);
  }


  public NxMeshData getMeshData() {
    long cPtr = JPhysXAdapterJNI.NxClothDesc_meshData_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxMeshData(cPtr, false);
  }
  public NxMeshData getMeshData_inplace(NxMeshData result) {
    long cPtr = JPhysXAdapterJNI.NxClothDesc_meshData_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setCollisionGroup(int value) {
    JPhysXAdapterJNI.NxClothDesc_collisionGroup_set(swigCPtr, this, value);
  }


  public int getCollisionGroup() {
    return JPhysXAdapterJNI.NxClothDesc_collisionGroup_get(swigCPtr, this);
  }


  public void setGroupsMask(NxGroupsMask value) {
    JPhysXAdapterJNI.NxClothDesc_groupsMask_set(swigCPtr, this, NxGroupsMask.getCPtr(value), value);
  }


  public NxGroupsMask getGroupsMask() {
    long cPtr = JPhysXAdapterJNI.NxClothDesc_groupsMask_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxGroupsMask(cPtr, false);
  }
  public NxGroupsMask getGroupsMask_inplace(NxGroupsMask result) {
    long cPtr = JPhysXAdapterJNI.NxClothDesc_groupsMask_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setForceFieldMaterial(int value) {
    JPhysXAdapterJNI.NxClothDesc_forceFieldMaterial_set(swigCPtr, this, value);
  }


  public int getForceFieldMaterial() {
    return JPhysXAdapterJNI.NxClothDesc_forceFieldMaterial_get(swigCPtr, this);
  }


  public void setValidBounds(NxBounds3 value) {
    JPhysXAdapterJNI.NxClothDesc_validBounds_set(swigCPtr, this, NxBounds3.getCPtr(value), value);
  }


  public NxBounds3 getValidBounds() {
    long cPtr = JPhysXAdapterJNI.NxClothDesc_validBounds_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxBounds3(cPtr, false);
  }
  public NxBounds3 getValidBounds_inplace(NxBounds3 result) {
    long cPtr = JPhysXAdapterJNI.NxClothDesc_validBounds_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setRelativeGridSpacing(float value) {
    JPhysXAdapterJNI.NxClothDesc_relativeGridSpacing_set(swigCPtr, this, value);
  }


  public float getRelativeGridSpacing() {
    return JPhysXAdapterJNI.NxClothDesc_relativeGridSpacing_get(swigCPtr, this);
  }


  public void setFlags(long value) {
    JPhysXAdapterJNI.NxClothDesc_flags_set(swigCPtr, this, value);
  }


  public long getFlags() {
    return JPhysXAdapterJNI.NxClothDesc_flags_get(swigCPtr, this);
  }


  public void setUserData(SWIGTYPE_p_void value) {
    JPhysXAdapterJNI.NxClothDesc_userData_set(swigCPtr, this, SWIGTYPE_p_void.getCPtr(value));
  }


  public SWIGTYPE_p_void getUserData() {
    long cPtr = JPhysXAdapterJNI.NxClothDesc_userData_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }


  public void setName(String value) {
    JPhysXAdapterJNI.NxClothDesc_name_set(swigCPtr, this, value);
  }


  public String getName() {
    return JPhysXAdapterJNI.NxClothDesc_name_get(swigCPtr, this);
  }


  public void setCompartment(NxCompartment value) {
    JPhysXAdapterJNI.NxClothDesc_compartment_set(swigCPtr, this, NxCompartment.getCPtr(value), value);
  }


  public NxCompartment getCompartment() {
    long cPtr = JPhysXAdapterJNI.NxClothDesc_compartment_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxCompartment(cPtr, false);
  }
  public NxCompartment getCompartment_inplace(NxCompartment result) {
    long cPtr = JPhysXAdapterJNI.NxClothDesc_compartment_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public NxClothDesc() {
    this(JPhysXAdapterJNI.new_NxClothDesc(), true);
  }


  public void setToDefault() {
    JPhysXAdapterJNI.NxClothDesc_setToDefault(swigCPtr, this);
  }


  public boolean isValid() {
    return JPhysXAdapterJNI.NxClothDesc_isValid(swigCPtr, this);
  }


}

