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
public class NxRaycastHit {
  private long swigCPtr;
  boolean swigCMemOwn;
  
  /**
   * Default java constructor.
   * @param constructionMarker marks that this is java constructor. Clashing with C++ constructors is unlikely.
   */
  NxRaycastHit(Boolean constructionMarker){}

  NxRaycastHit(long cPtr, boolean cMemoryOwn) {
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
  public static long getCPtr(NxRaycastHit obj) {
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
      JPhysXAdapterJNI.delete_NxRaycastHit(swigCPtr);
    }

    swigCPtr = 0;
  }

  public boolean equalCPtr(NxRaycastHit obj) {
    return obj.swigCPtr == this.swigCPtr;
  }


  public void setShape(NxShape value) {
    JPhysXAdapterJNI.NxRaycastHit_shape_set(swigCPtr, this, NxShape.getCPtr(value), value);
  }


  public NxShape getShape() {
    long cPtr = JPhysXAdapterJNI.NxRaycastHit_shape_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxShape(cPtr, false);
  }
  public NxShape getShape_inplace(NxShape result) {
    long cPtr = JPhysXAdapterJNI.NxRaycastHit_shape_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setWorldImpact(NxVec3 value) {
    JPhysXAdapterJNI.NxRaycastHit_worldImpact_set(swigCPtr, this, NxVec3.getCPtr(value), value);
  }


  public NxVec3 getWorldImpact() {
    long cPtr = JPhysXAdapterJNI.NxRaycastHit_worldImpact_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxVec3(cPtr, false);
  }
  public NxVec3 getWorldImpact_inplace(NxVec3 result) {
    long cPtr = JPhysXAdapterJNI.NxRaycastHit_worldImpact_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setWorldNormal(NxVec3 value) {
    JPhysXAdapterJNI.NxRaycastHit_worldNormal_set(swigCPtr, this, NxVec3.getCPtr(value), value);
  }


  public NxVec3 getWorldNormal() {
    long cPtr = JPhysXAdapterJNI.NxRaycastHit_worldNormal_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxVec3(cPtr, false);
  }
  public NxVec3 getWorldNormal_inplace(NxVec3 result) {
    long cPtr = JPhysXAdapterJNI.NxRaycastHit_worldNormal_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setFaceID(long value) {
    JPhysXAdapterJNI.NxRaycastHit_faceID_set(swigCPtr, this, value);
  }


  public long getFaceID() {
    return JPhysXAdapterJNI.NxRaycastHit_faceID_get(swigCPtr, this);
  }


  public void setInternalFaceID(long value) {
    JPhysXAdapterJNI.NxRaycastHit_internalFaceID_set(swigCPtr, this, value);
  }


  public long getInternalFaceID() {
    return JPhysXAdapterJNI.NxRaycastHit_internalFaceID_get(swigCPtr, this);
  }


  public void setDistance(float value) {
    JPhysXAdapterJNI.NxRaycastHit_distance_set(swigCPtr, this, value);
  }


  public float getDistance() {
    return JPhysXAdapterJNI.NxRaycastHit_distance_get(swigCPtr, this);
  }


  public void setU(float value) {
    JPhysXAdapterJNI.NxRaycastHit_u_set(swigCPtr, this, value);
  }


  public float getU() {
    return JPhysXAdapterJNI.NxRaycastHit_u_get(swigCPtr, this);
  }


  public void setV(float value) {
    JPhysXAdapterJNI.NxRaycastHit_v_set(swigCPtr, this, value);
  }


  public float getV() {
    return JPhysXAdapterJNI.NxRaycastHit_v_get(swigCPtr, this);
  }


  public void setMaterialIndex(int value) {
    JPhysXAdapterJNI.NxRaycastHit_materialIndex_set(swigCPtr, this, value);
  }


  public int getMaterialIndex() {
    return JPhysXAdapterJNI.NxRaycastHit_materialIndex_get(swigCPtr, this);
  }


  public void setFlags(long value) {
    JPhysXAdapterJNI.NxRaycastHit_flags_set(swigCPtr, this, value);
  }


  public long getFlags() {
    return JPhysXAdapterJNI.NxRaycastHit_flags_get(swigCPtr, this);
  }


  public NxRaycastHit() {
    this(JPhysXAdapterJNI.new_NxRaycastHit(), true);
  }


}

