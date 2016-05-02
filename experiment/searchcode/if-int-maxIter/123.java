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
public class NxCompartment {
  private long swigCPtr;
  boolean swigCMemOwn;
  
  /**
   * Default java constructor.
   * @param constructionMarker marks that this is java constructor. Clashing with C++ constructors is unlikely.
   */
  NxCompartment(Boolean constructionMarker){}

  NxCompartment(long cPtr, boolean cMemoryOwn) {
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
  public static long getCPtr(NxCompartment obj) {
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

  public synchronized void delete() {
    if(swigCPtr != 0 && swigCMemOwn) {
      swigCMemOwn = false;
      throw new UnsupportedOperationException("C++ destructor does not have public access");
    }

    swigCPtr = 0;
  }

  public boolean equalCPtr(NxCompartment obj) {
    return obj.swigCPtr == this.swigCPtr;
  }


  public int getType() {
    return JPhysXAdapterJNI.NxCompartment_getType(swigCPtr, this);
  }


  public long getDeviceCode() {
    return JPhysXAdapterJNI.NxCompartment_getDeviceCode(swigCPtr, this);
  }


  public float getGridHashCellSize() {
    return JPhysXAdapterJNI.NxCompartment_getGridHashCellSize(swigCPtr, this);
  }


  public long gridHashTablePower() {
    return JPhysXAdapterJNI.NxCompartment_gridHashTablePower(swigCPtr, this);
  }


  public void setTimeScale(float arg0) {
    JPhysXAdapterJNI.NxCompartment_setTimeScale(swigCPtr, this, arg0);
  }


  public float getTimeScale() {
    return JPhysXAdapterJNI.NxCompartment_getTimeScale(swigCPtr, this);
  }


  public void setTiming(float maxTimestep, long maxIter, int method) {
    JPhysXAdapterJNI.NxCompartment_setTiming__SWIG_0(swigCPtr, this, maxTimestep, maxIter, method);
  }


  public void setTiming(float maxTimestep, long maxIter) {
    JPhysXAdapterJNI.NxCompartment_setTiming__SWIG_1(swigCPtr, this, maxTimestep, maxIter);
  }


  public void setTiming(float maxTimestep) {
    JPhysXAdapterJNI.NxCompartment_setTiming__SWIG_2(swigCPtr, this, maxTimestep);
  }


  public void setTiming() {
    JPhysXAdapterJNI.NxCompartment_setTiming__SWIG_3(swigCPtr, this);
  }


  public void getTiming(SWIGTYPE_p_float maxTimestep, SWIGTYPE_p_unsigned_int maxIter, SWIGTYPE_p_NxTimeStepMethod method, SWIGTYPE_p_unsigned_int numSubSteps) {
    JPhysXAdapterJNI.NxCompartment_getTiming__SWIG_0(swigCPtr, this, SWIGTYPE_p_float.getCPtr(maxTimestep), SWIGTYPE_p_unsigned_int.getCPtr(maxIter), SWIGTYPE_p_NxTimeStepMethod.getCPtr(method), SWIGTYPE_p_unsigned_int.getCPtr(numSubSteps));
  }


  public void getTiming(SWIGTYPE_p_float maxTimestep, SWIGTYPE_p_unsigned_int maxIter, SWIGTYPE_p_NxTimeStepMethod method) {
    JPhysXAdapterJNI.NxCompartment_getTiming__SWIG_1(swigCPtr, this, SWIGTYPE_p_float.getCPtr(maxTimestep), SWIGTYPE_p_unsigned_int.getCPtr(maxIter), SWIGTYPE_p_NxTimeStepMethod.getCPtr(method));
  }


  public boolean checkResults(boolean block) {
    return JPhysXAdapterJNI.NxCompartment_checkResults__SWIG_0(swigCPtr, this, block);
  }


  public boolean checkResults() {
    return JPhysXAdapterJNI.NxCompartment_checkResults__SWIG_1(swigCPtr, this);
  }


  public boolean fetchResults(boolean block) {
    return JPhysXAdapterJNI.NxCompartment_fetchResults__SWIG_0(swigCPtr, this, block);
  }


  public boolean fetchResults() {
    return JPhysXAdapterJNI.NxCompartment_fetchResults__SWIG_1(swigCPtr, this);
  }


  public boolean saveToDesc(NxCompartmentDesc desc) {
    return JPhysXAdapterJNI.NxCompartment_saveToDesc(swigCPtr, this, NxCompartmentDesc.getCPtr(desc), desc);
  }


  public void setFlags(long flags) {
    JPhysXAdapterJNI.NxCompartment_setFlags(swigCPtr, this, flags);
  }


  public long getFlags() {
    return JPhysXAdapterJNI.NxCompartment_getFlags(swigCPtr, this);
  }


}

