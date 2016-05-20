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
public class NxForceFieldDesc {
  private long swigCPtr;
  boolean swigCMemOwn;
  
  /**
   * Default java constructor.
   * @param constructionMarker marks that this is java constructor. Clashing with C++ constructors is unlikely.
   */
  NxForceFieldDesc(Boolean constructionMarker){}

  NxForceFieldDesc(long cPtr, boolean cMemoryOwn) {
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
  public static long getCPtr(NxForceFieldDesc obj) {
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
      JPhysXAdapterJNI.delete_NxForceFieldDesc(swigCPtr);
    }

    swigCPtr = 0;
  }

  public boolean equalCPtr(NxForceFieldDesc obj) {
    return obj.swigCPtr == this.swigCPtr;
  }


  public void setPose(NxMat34 value) {
    JPhysXAdapterJNI.NxForceFieldDesc_pose_set(swigCPtr, this, NxMat34.getCPtr(value), value);
  }


  public NxMat34 getPose() {
    long cPtr = JPhysXAdapterJNI.NxForceFieldDesc_pose_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxMat34(cPtr, false);
  }
  public NxMat34 getPose_inplace(NxMat34 result) {
    long cPtr = JPhysXAdapterJNI.NxForceFieldDesc_pose_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setActor(NxActor value) {
    JPhysXAdapterJNI.NxForceFieldDesc_actor_set(swigCPtr, this, NxActor.getCPtr(value), value);
  }


  public NxActor getActor() {
    long cPtr = JPhysXAdapterJNI.NxForceFieldDesc_actor_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxActor(cPtr, false);
  }
  public NxActor getActor_inplace(NxActor result) {
    long cPtr = JPhysXAdapterJNI.NxForceFieldDesc_actor_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setCoordinates(int value) {
    JPhysXAdapterJNI.NxForceFieldDesc_coordinates_set(swigCPtr, this, value);
  }


  public int getCoordinates() {
    return JPhysXAdapterJNI.NxForceFieldDesc_coordinates_get(swigCPtr, this);
  }


  public void setIncludeGroupShapes(SWIGTYPE_p_NxArrayTNxForceFieldShapeDesc_p_NxAllocatorDefault_t value) {
    JPhysXAdapterJNI.NxForceFieldDesc_includeGroupShapes_set(swigCPtr, this, SWIGTYPE_p_NxArrayTNxForceFieldShapeDesc_p_NxAllocatorDefault_t.getCPtr(value));
  }


  public SWIGTYPE_p_NxArrayTNxForceFieldShapeDesc_p_NxAllocatorDefault_t getIncludeGroupShapes() {
    long cPtr = JPhysXAdapterJNI.NxForceFieldDesc_includeGroupShapes_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_NxArrayTNxForceFieldShapeDesc_p_NxAllocatorDefault_t(cPtr, false);
  }


  public void setShapeGroups(SWIGTYPE_p_NxArrayTNxForceFieldShapeGroup_p_NxAllocatorDefault_t value) {
    JPhysXAdapterJNI.NxForceFieldDesc_shapeGroups_set(swigCPtr, this, SWIGTYPE_p_NxArrayTNxForceFieldShapeGroup_p_NxAllocatorDefault_t.getCPtr(value));
  }


  public SWIGTYPE_p_NxArrayTNxForceFieldShapeGroup_p_NxAllocatorDefault_t getShapeGroups() {
    long cPtr = JPhysXAdapterJNI.NxForceFieldDesc_shapeGroups_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_NxArrayTNxForceFieldShapeGroup_p_NxAllocatorDefault_t(cPtr, false);
  }


  public void setGroup(int value) {
    JPhysXAdapterJNI.NxForceFieldDesc_group_set(swigCPtr, this, value);
  }


  public int getGroup() {
    return JPhysXAdapterJNI.NxForceFieldDesc_group_get(swigCPtr, this);
  }


  public void setGroupsMask(NxGroupsMask value) {
    JPhysXAdapterJNI.NxForceFieldDesc_groupsMask_set(swigCPtr, this, NxGroupsMask.getCPtr(value), value);
  }


  public NxGroupsMask getGroupsMask() {
    long cPtr = JPhysXAdapterJNI.NxForceFieldDesc_groupsMask_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxGroupsMask(cPtr, false);
  }
  public NxGroupsMask getGroupsMask_inplace(NxGroupsMask result) {
    long cPtr = JPhysXAdapterJNI.NxForceFieldDesc_groupsMask_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setKernel(SWIGTYPE_p_NxForceFieldKernel value) {
    JPhysXAdapterJNI.NxForceFieldDesc_kernel_set(swigCPtr, this, SWIGTYPE_p_NxForceFieldKernel.getCPtr(value));
  }


  public SWIGTYPE_p_NxForceFieldKernel getKernel() {
    long cPtr = JPhysXAdapterJNI.NxForceFieldDesc_kernel_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_NxForceFieldKernel(cPtr, false);
  }


  public void setForceFieldVariety(int value) {
    JPhysXAdapterJNI.NxForceFieldDesc_forceFieldVariety_set(swigCPtr, this, value);
  }


  public int getForceFieldVariety() {
    return JPhysXAdapterJNI.NxForceFieldDesc_forceFieldVariety_get(swigCPtr, this);
  }


  public void setFluidType(int value) {
    JPhysXAdapterJNI.NxForceFieldDesc_fluidType_set(swigCPtr, this, value);
  }


  public int getFluidType() {
    return JPhysXAdapterJNI.NxForceFieldDesc_fluidType_get(swigCPtr, this);
  }


  public void setClothType(int value) {
    JPhysXAdapterJNI.NxForceFieldDesc_clothType_set(swigCPtr, this, value);
  }


  public int getClothType() {
    return JPhysXAdapterJNI.NxForceFieldDesc_clothType_get(swigCPtr, this);
  }


  public void setSoftBodyType(int value) {
    JPhysXAdapterJNI.NxForceFieldDesc_softBodyType_set(swigCPtr, this, value);
  }


  public int getSoftBodyType() {
    return JPhysXAdapterJNI.NxForceFieldDesc_softBodyType_get(swigCPtr, this);
  }


  public void setRigidBodyType(int value) {
    JPhysXAdapterJNI.NxForceFieldDesc_rigidBodyType_set(swigCPtr, this, value);
  }


  public int getRigidBodyType() {
    return JPhysXAdapterJNI.NxForceFieldDesc_rigidBodyType_get(swigCPtr, this);
  }


  public void setFlags(long value) {
    JPhysXAdapterJNI.NxForceFieldDesc_flags_set(swigCPtr, this, value);
  }


  public long getFlags() {
    return JPhysXAdapterJNI.NxForceFieldDesc_flags_get(swigCPtr, this);
  }


  public void setName(String value) {
    JPhysXAdapterJNI.NxForceFieldDesc_name_set(swigCPtr, this, value);
  }


  public String getName() {
    return JPhysXAdapterJNI.NxForceFieldDesc_name_get(swigCPtr, this);
  }


  public void setUserData(SWIGTYPE_p_void value) {
    JPhysXAdapterJNI.NxForceFieldDesc_userData_set(swigCPtr, this, SWIGTYPE_p_void.getCPtr(value));
  }


  public SWIGTYPE_p_void getUserData() {
    long cPtr = JPhysXAdapterJNI.NxForceFieldDesc_userData_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }


  public NxForceFieldDesc() {
    this(JPhysXAdapterJNI.new_NxForceFieldDesc(), true);
  }


  public void setToDefault() {
    JPhysXAdapterJNI.NxForceFieldDesc_setToDefault(swigCPtr, this);
  }


  public boolean isValid() {
    return JPhysXAdapterJNI.NxForceFieldDesc_isValid(swigCPtr, this);
  }


}

