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
public class NxShapeDesc {
  private long swigCPtr;
  boolean swigCMemOwn;
  
  /**
   * Default java constructor.
   * @param constructionMarker marks that this is java constructor. Clashing with C++ constructors is unlikely.
   */
  NxShapeDesc(Boolean constructionMarker){}

  NxShapeDesc(long cPtr, boolean cMemoryOwn) {
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
  public static long getCPtr(NxShapeDesc obj) {
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
      JPhysXAdapterJNI.delete_NxShapeDesc(swigCPtr);
    }

    swigCPtr = 0;
  }

  public boolean equalCPtr(NxShapeDesc obj) {
    return obj.swigCPtr == this.swigCPtr;
  }


  public void setLocalPose(NxMat34 value) {
    JPhysXAdapterJNI.NxShapeDesc_localPose_set(swigCPtr, this, NxMat34.getCPtr(value), value);
  }


  public NxMat34 getLocalPose() {
    long cPtr = JPhysXAdapterJNI.NxShapeDesc_localPose_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxMat34(cPtr, false);
  }
  public NxMat34 getLocalPose_inplace(NxMat34 result) {
    long cPtr = JPhysXAdapterJNI.NxShapeDesc_localPose_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setShapeFlags(long value) {
    JPhysXAdapterJNI.NxShapeDesc_shapeFlags_set(swigCPtr, this, value);
  }


  public long getShapeFlags() {
    return JPhysXAdapterJNI.NxShapeDesc_shapeFlags_get(swigCPtr, this);
  }


  public void setGroup(int value) {
    JPhysXAdapterJNI.NxShapeDesc_group_set(swigCPtr, this, value);
  }


  public int getGroup() {
    return JPhysXAdapterJNI.NxShapeDesc_group_get(swigCPtr, this);
  }


  public void setMaterialIndex(int value) {
    JPhysXAdapterJNI.NxShapeDesc_materialIndex_set(swigCPtr, this, value);
  }


  public int getMaterialIndex() {
    return JPhysXAdapterJNI.NxShapeDesc_materialIndex_get(swigCPtr, this);
  }


  public void setCcdSkeleton(NxCCDSkeleton value) {
    JPhysXAdapterJNI.NxShapeDesc_ccdSkeleton_set(swigCPtr, this, NxCCDSkeleton.getCPtr(value), value);
  }


  public NxCCDSkeleton getCcdSkeleton() {
    long cPtr = JPhysXAdapterJNI.NxShapeDesc_ccdSkeleton_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxCCDSkeleton(cPtr, false);
  }
  public NxCCDSkeleton getCcdSkeleton_inplace(NxCCDSkeleton result) {
    long cPtr = JPhysXAdapterJNI.NxShapeDesc_ccdSkeleton_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setDensity(float value) {
    JPhysXAdapterJNI.NxShapeDesc_density_set(swigCPtr, this, value);
  }


  public float getDensity() {
    return JPhysXAdapterJNI.NxShapeDesc_density_get(swigCPtr, this);
  }


  public void setMass(float value) {
    JPhysXAdapterJNI.NxShapeDesc_mass_set(swigCPtr, this, value);
  }


  public float getMass() {
    return JPhysXAdapterJNI.NxShapeDesc_mass_get(swigCPtr, this);
  }


  public void setSkinWidth(float value) {
    JPhysXAdapterJNI.NxShapeDesc_skinWidth_set(swigCPtr, this, value);
  }


  public float getSkinWidth() {
    return JPhysXAdapterJNI.NxShapeDesc_skinWidth_get(swigCPtr, this);
  }


  public void setUserData(SWIGTYPE_p_void value) {
    JPhysXAdapterJNI.NxShapeDesc_userData_set(swigCPtr, this, SWIGTYPE_p_void.getCPtr(value));
  }


  public SWIGTYPE_p_void getUserData() {
    long cPtr = JPhysXAdapterJNI.NxShapeDesc_userData_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }


  public void setName(String value) {
    JPhysXAdapterJNI.NxShapeDesc_name_set(swigCPtr, this, value);
  }


  public String getName() {
    return JPhysXAdapterJNI.NxShapeDesc_name_get(swigCPtr, this);
  }


  public void setGroupsMask(NxGroupsMask value) {
    JPhysXAdapterJNI.NxShapeDesc_groupsMask_set(swigCPtr, this, NxGroupsMask.getCPtr(value), value);
  }


  public NxGroupsMask getGroupsMask() {
    long cPtr = JPhysXAdapterJNI.NxShapeDesc_groupsMask_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxGroupsMask(cPtr, false);
  }
  public NxGroupsMask getGroupsMask_inplace(NxGroupsMask result) {
    long cPtr = JPhysXAdapterJNI.NxShapeDesc_groupsMask_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setNonInteractingCompartmentTypes(long value) {
    JPhysXAdapterJNI.NxShapeDesc_nonInteractingCompartmentTypes_set(swigCPtr, this, value);
  }


  public long getNonInteractingCompartmentTypes() {
    return JPhysXAdapterJNI.NxShapeDesc_nonInteractingCompartmentTypes_get(swigCPtr, this);
  }


  public void setToDefault() {
    JPhysXAdapterJNI.NxShapeDesc_setToDefault(swigCPtr, this);
  }


  public boolean isValid() {
    return JPhysXAdapterJNI.NxShapeDesc_isValid(swigCPtr, this);
  }


  public int getType() {
    return JPhysXAdapterJNI.NxShapeDesc_getType(swigCPtr, this);
  }


}

