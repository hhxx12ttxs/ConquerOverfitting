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
public class NxParticleData {
  private long swigCPtr;
  boolean swigCMemOwn;
  
  /**
   * Default java constructor.
   * @param constructionMarker marks that this is java constructor. Clashing with C++ constructors is unlikely.
   */
  NxParticleData(Boolean constructionMarker){}

  NxParticleData(long cPtr, boolean cMemoryOwn) {
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
  public static long getCPtr(NxParticleData obj) {
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
      JPhysXAdapterJNI.delete_NxParticleData(swigCPtr);
    }

    swigCPtr = 0;
  }

  public boolean equalCPtr(NxParticleData obj) {
    return obj.swigCPtr == this.swigCPtr;
  }


  public void setNumParticlesPtr(SWIGTYPE_p_unsigned_int value) {
    JPhysXAdapterJNI.NxParticleData_numParticlesPtr_set(swigCPtr, this, SWIGTYPE_p_unsigned_int.getCPtr(value));
  }


  public SWIGTYPE_p_unsigned_int getNumParticlesPtr() {
    long cPtr = JPhysXAdapterJNI.NxParticleData_numParticlesPtr_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_unsigned_int(cPtr, false);
  }


  public void setBufferPos(SWIGTYPE_p_float value) {
    JPhysXAdapterJNI.NxParticleData_bufferPos_set(swigCPtr, this, SWIGTYPE_p_float.getCPtr(value));
  }


  public SWIGTYPE_p_float getBufferPos() {
    long cPtr = JPhysXAdapterJNI.NxParticleData_bufferPos_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_float(cPtr, false);
  }


  public void setBufferVel(SWIGTYPE_p_float value) {
    JPhysXAdapterJNI.NxParticleData_bufferVel_set(swigCPtr, this, SWIGTYPE_p_float.getCPtr(value));
  }


  public SWIGTYPE_p_float getBufferVel() {
    long cPtr = JPhysXAdapterJNI.NxParticleData_bufferVel_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_float(cPtr, false);
  }


  public void setBufferLife(SWIGTYPE_p_float value) {
    JPhysXAdapterJNI.NxParticleData_bufferLife_set(swigCPtr, this, SWIGTYPE_p_float.getCPtr(value));
  }


  public SWIGTYPE_p_float getBufferLife() {
    long cPtr = JPhysXAdapterJNI.NxParticleData_bufferLife_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_float(cPtr, false);
  }


  public void setBufferDensity(SWIGTYPE_p_float value) {
    JPhysXAdapterJNI.NxParticleData_bufferDensity_set(swigCPtr, this, SWIGTYPE_p_float.getCPtr(value));
  }


  public SWIGTYPE_p_float getBufferDensity() {
    long cPtr = JPhysXAdapterJNI.NxParticleData_bufferDensity_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_float(cPtr, false);
  }


  public void setBufferId(SWIGTYPE_p_unsigned_int value) {
    JPhysXAdapterJNI.NxParticleData_bufferId_set(swigCPtr, this, SWIGTYPE_p_unsigned_int.getCPtr(value));
  }


  public SWIGTYPE_p_unsigned_int getBufferId() {
    long cPtr = JPhysXAdapterJNI.NxParticleData_bufferId_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_unsigned_int(cPtr, false);
  }


  public void setBufferFlag(SWIGTYPE_p_unsigned_int value) {
    JPhysXAdapterJNI.NxParticleData_bufferFlag_set(swigCPtr, this, SWIGTYPE_p_unsigned_int.getCPtr(value));
  }


  public SWIGTYPE_p_unsigned_int getBufferFlag() {
    long cPtr = JPhysXAdapterJNI.NxParticleData_bufferFlag_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_unsigned_int(cPtr, false);
  }


  public void setBufferCollisionNormal(SWIGTYPE_p_float value) {
    JPhysXAdapterJNI.NxParticleData_bufferCollisionNormal_set(swigCPtr, this, SWIGTYPE_p_float.getCPtr(value));
  }


  public SWIGTYPE_p_float getBufferCollisionNormal() {
    long cPtr = JPhysXAdapterJNI.NxParticleData_bufferCollisionNormal_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_float(cPtr, false);
  }


  public void setBufferPosByteStride(long value) {
    JPhysXAdapterJNI.NxParticleData_bufferPosByteStride_set(swigCPtr, this, value);
  }


  public long getBufferPosByteStride() {
    return JPhysXAdapterJNI.NxParticleData_bufferPosByteStride_get(swigCPtr, this);
  }


  public void setBufferVelByteStride(long value) {
    JPhysXAdapterJNI.NxParticleData_bufferVelByteStride_set(swigCPtr, this, value);
  }


  public long getBufferVelByteStride() {
    return JPhysXAdapterJNI.NxParticleData_bufferVelByteStride_get(swigCPtr, this);
  }


  public void setBufferLifeByteStride(long value) {
    JPhysXAdapterJNI.NxParticleData_bufferLifeByteStride_set(swigCPtr, this, value);
  }


  public long getBufferLifeByteStride() {
    return JPhysXAdapterJNI.NxParticleData_bufferLifeByteStride_get(swigCPtr, this);
  }


  public void setBufferDensityByteStride(long value) {
    JPhysXAdapterJNI.NxParticleData_bufferDensityByteStride_set(swigCPtr, this, value);
  }


  public long getBufferDensityByteStride() {
    return JPhysXAdapterJNI.NxParticleData_bufferDensityByteStride_get(swigCPtr, this);
  }


  public void setBufferIdByteStride(long value) {
    JPhysXAdapterJNI.NxParticleData_bufferIdByteStride_set(swigCPtr, this, value);
  }


  public long getBufferIdByteStride() {
    return JPhysXAdapterJNI.NxParticleData_bufferIdByteStride_get(swigCPtr, this);
  }


  public void setBufferFlagByteStride(long value) {
    JPhysXAdapterJNI.NxParticleData_bufferFlagByteStride_set(swigCPtr, this, value);
  }


  public long getBufferFlagByteStride() {
    return JPhysXAdapterJNI.NxParticleData_bufferFlagByteStride_get(swigCPtr, this);
  }


  public void setBufferCollisionNormalByteStride(long value) {
    JPhysXAdapterJNI.NxParticleData_bufferCollisionNormalByteStride_set(swigCPtr, this, value);
  }


  public long getBufferCollisionNormalByteStride() {
    return JPhysXAdapterJNI.NxParticleData_bufferCollisionNormalByteStride_get(swigCPtr, this);
  }


  public void setName(String value) {
    JPhysXAdapterJNI.NxParticleData_name_set(swigCPtr, this, value);
  }


  public String getName() {
    return JPhysXAdapterJNI.NxParticleData_name_get(swigCPtr, this);
  }


  public void setToDefault() {
    JPhysXAdapterJNI.NxParticleData_setToDefault(swigCPtr, this);
  }


  public boolean isValid() {
    return JPhysXAdapterJNI.NxParticleData_isValid(swigCPtr, this);
  }


  public NxParticleData() {
    this(JPhysXAdapterJNI.new_NxParticleData(), true);
  }


}

