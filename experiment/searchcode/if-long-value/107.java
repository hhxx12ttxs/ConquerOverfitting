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
public class NxWheelShapeDesc extends NxShapeDesc {
  private long swigCPtr;

  /**
   * Default java constructor.
   * @param constructionMarker marks that this is java constructor. Clashing with C++ constructors is unlikely.
   */
  NxWheelShapeDesc(Boolean constructionMarker){
	super(constructionMarker);
  }

  NxWheelShapeDesc(long cPtr, boolean cMemoryOwn) {
	super(Boolean.TRUE);
    setCPart(cPtr, cMemoryOwn);
  }


  /**
   * Contains functionality of object construction.
   * @param cPtr c-pointer to the native object
   * @param cMemoryOwn is native object needs to be deleted when java object is deleted?
   */
  void setCPart(long cPtr, boolean cMemoryOwn) {
      super.setCPart(JPhysXAdapterJNI.SWIGNxWheelShapeDescUpcast(cPtr), cMemoryOwn);
      swigCPtr = cPtr;
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
  public static long getCPtr(NxWheelShapeDesc obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }


  protected void finalize() {
    delete();
  }


  public synchronized void delete() {
    if(swigCPtr != 0 && swigCMemOwn) {
      swigCMemOwn = false;
      JPhysXAdapterJNI.delete_NxWheelShapeDesc(swigCPtr);
    }

    swigCPtr = 0;
    super.delete();
  }

  public boolean equalCPtr(NxWheelShapeDesc obj) {
    return obj.swigCPtr == this.swigCPtr;
  }


  public void setRadius(float value) {
    JPhysXAdapterJNI.NxWheelShapeDesc_radius_set(swigCPtr, this, value);
  }


  public float getRadius() {
    return JPhysXAdapterJNI.NxWheelShapeDesc_radius_get(swigCPtr, this);
  }


  public void setSuspensionTravel(float value) {
    JPhysXAdapterJNI.NxWheelShapeDesc_suspensionTravel_set(swigCPtr, this, value);
  }


  public float getSuspensionTravel() {
    return JPhysXAdapterJNI.NxWheelShapeDesc_suspensionTravel_get(swigCPtr, this);
  }


  public void setSuspension(NxSpringDesc value) {
    JPhysXAdapterJNI.NxWheelShapeDesc_suspension_set(swigCPtr, this, NxSpringDesc.getCPtr(value), value);
  }


  public NxSpringDesc getSuspension() {
    long cPtr = JPhysXAdapterJNI.NxWheelShapeDesc_suspension_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxSpringDesc(cPtr, false);
  }
  public NxSpringDesc getSuspension_inplace(NxSpringDesc result) {
    long cPtr = JPhysXAdapterJNI.NxWheelShapeDesc_suspension_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setLongitudalTireForceFunction(NxTireFunctionDesc value) {
    JPhysXAdapterJNI.NxWheelShapeDesc_longitudalTireForceFunction_set(swigCPtr, this, NxTireFunctionDesc.getCPtr(value), value);
  }


  public NxTireFunctionDesc getLongitudalTireForceFunction() {
    long cPtr = JPhysXAdapterJNI.NxWheelShapeDesc_longitudalTireForceFunction_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxTireFunctionDesc(cPtr, false);
  }
  public NxTireFunctionDesc getLongitudalTireForceFunction_inplace(NxTireFunctionDesc result) {
    long cPtr = JPhysXAdapterJNI.NxWheelShapeDesc_longitudalTireForceFunction_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setLateralTireForceFunction(NxTireFunctionDesc value) {
    JPhysXAdapterJNI.NxWheelShapeDesc_lateralTireForceFunction_set(swigCPtr, this, NxTireFunctionDesc.getCPtr(value), value);
  }


  public NxTireFunctionDesc getLateralTireForceFunction() {
    long cPtr = JPhysXAdapterJNI.NxWheelShapeDesc_lateralTireForceFunction_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxTireFunctionDesc(cPtr, false);
  }
  public NxTireFunctionDesc getLateralTireForceFunction_inplace(NxTireFunctionDesc result) {
    long cPtr = JPhysXAdapterJNI.NxWheelShapeDesc_lateralTireForceFunction_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setInverseWheelMass(float value) {
    JPhysXAdapterJNI.NxWheelShapeDesc_inverseWheelMass_set(swigCPtr, this, value);
  }


  public float getInverseWheelMass() {
    return JPhysXAdapterJNI.NxWheelShapeDesc_inverseWheelMass_get(swigCPtr, this);
  }


  public void setWheelFlags(long value) {
    JPhysXAdapterJNI.NxWheelShapeDesc_wheelFlags_set(swigCPtr, this, value);
  }


  public long getWheelFlags() {
    return JPhysXAdapterJNI.NxWheelShapeDesc_wheelFlags_get(swigCPtr, this);
  }


  public void setMotorTorque(float value) {
    JPhysXAdapterJNI.NxWheelShapeDesc_motorTorque_set(swigCPtr, this, value);
  }


  public float getMotorTorque() {
    return JPhysXAdapterJNI.NxWheelShapeDesc_motorTorque_get(swigCPtr, this);
  }


  public void setBrakeTorque(float value) {
    JPhysXAdapterJNI.NxWheelShapeDesc_brakeTorque_set(swigCPtr, this, value);
  }


  public float getBrakeTorque() {
    return JPhysXAdapterJNI.NxWheelShapeDesc_brakeTorque_get(swigCPtr, this);
  }


  public void setSteerAngle(float value) {
    JPhysXAdapterJNI.NxWheelShapeDesc_steerAngle_set(swigCPtr, this, value);
  }


  public float getSteerAngle() {
    return JPhysXAdapterJNI.NxWheelShapeDesc_steerAngle_get(swigCPtr, this);
  }


  public void setWheelContactModify(NxUserWheelContactModify value) {
    JPhysXAdapterJNI.NxWheelShapeDesc_wheelContactModify_set(swigCPtr, this, NxUserWheelContactModify.getCPtr(value), value);
  }


  public NxUserWheelContactModify getWheelContactModify() {
    long cPtr = JPhysXAdapterJNI.NxWheelShapeDesc_wheelContactModify_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxUserWheelContactModify(cPtr, false);
  }
  public NxUserWheelContactModify getWheelContactModify_inplace(NxUserWheelContactModify result) {
    long cPtr = JPhysXAdapterJNI.NxWheelShapeDesc_wheelContactModify_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public NxWheelShapeDesc() {
    this(JPhysXAdapterJNI.new_NxWheelShapeDesc(), true);
  }


  public void setToDefault(boolean fromCtor) {
    JPhysXAdapterJNI.NxWheelShapeDesc_setToDefault__SWIG_0(swigCPtr, this, fromCtor);
  }


  public void setToDefault() {
    JPhysXAdapterJNI.NxWheelShapeDesc_setToDefault__SWIG_1(swigCPtr, this);
  }


  public boolean isValid() {
    return JPhysXAdapterJNI.NxWheelShapeDesc_isValid(swigCPtr, this);
  }


}

