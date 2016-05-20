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
public class NxD6JointDesc extends NxJointDesc {
  private long swigCPtr;

  /**
   * Default java constructor.
   * @param constructionMarker marks that this is java constructor. Clashing with C++ constructors is unlikely.
   */
  NxD6JointDesc(Boolean constructionMarker){
	super(constructionMarker);
  }

  NxD6JointDesc(long cPtr, boolean cMemoryOwn) {
	super(Boolean.TRUE);
    setCPart(cPtr, cMemoryOwn);
  }


  /**
   * Contains functionality of object construction.
   * @param cPtr c-pointer to the native object
   * @param cMemoryOwn is native object needs to be deleted when java object is deleted?
   */
  void setCPart(long cPtr, boolean cMemoryOwn) {
      super.setCPart(JPhysXAdapterJNI.SWIGNxD6JointDescUpcast(cPtr), cMemoryOwn);
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
  public static long getCPtr(NxD6JointDesc obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }


  protected void finalize() {
    delete();
  }


  public synchronized void delete() {
    if(swigCPtr != 0 && swigCMemOwn) {
      swigCMemOwn = false;
      JPhysXAdapterJNI.delete_NxD6JointDesc(swigCPtr);
    }

    swigCPtr = 0;
    super.delete();
  }

  public boolean equalCPtr(NxD6JointDesc obj) {
    return obj.swigCPtr == this.swigCPtr;
  }


  public void setXMotion(int value) {
    JPhysXAdapterJNI.NxD6JointDesc_xMotion_set(swigCPtr, this, value);
  }


  public int getXMotion() {
    return JPhysXAdapterJNI.NxD6JointDesc_xMotion_get(swigCPtr, this);
  }


  public void setYMotion(int value) {
    JPhysXAdapterJNI.NxD6JointDesc_yMotion_set(swigCPtr, this, value);
  }


  public int getYMotion() {
    return JPhysXAdapterJNI.NxD6JointDesc_yMotion_get(swigCPtr, this);
  }


  public void setZMotion(int value) {
    JPhysXAdapterJNI.NxD6JointDesc_zMotion_set(swigCPtr, this, value);
  }


  public int getZMotion() {
    return JPhysXAdapterJNI.NxD6JointDesc_zMotion_get(swigCPtr, this);
  }


  public void setSwing1Motion(int value) {
    JPhysXAdapterJNI.NxD6JointDesc_swing1Motion_set(swigCPtr, this, value);
  }


  public int getSwing1Motion() {
    return JPhysXAdapterJNI.NxD6JointDesc_swing1Motion_get(swigCPtr, this);
  }


  public void setSwing2Motion(int value) {
    JPhysXAdapterJNI.NxD6JointDesc_swing2Motion_set(swigCPtr, this, value);
  }


  public int getSwing2Motion() {
    return JPhysXAdapterJNI.NxD6JointDesc_swing2Motion_get(swigCPtr, this);
  }


  public void setTwistMotion(int value) {
    JPhysXAdapterJNI.NxD6JointDesc_twistMotion_set(swigCPtr, this, value);
  }


  public int getTwistMotion() {
    return JPhysXAdapterJNI.NxD6JointDesc_twistMotion_get(swigCPtr, this);
  }


  public void setLinearLimit(NxJointLimitSoftDesc value) {
    JPhysXAdapterJNI.NxD6JointDesc_linearLimit_set(swigCPtr, this, NxJointLimitSoftDesc.getCPtr(value), value);
  }


  public NxJointLimitSoftDesc getLinearLimit() {
    long cPtr = JPhysXAdapterJNI.NxD6JointDesc_linearLimit_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxJointLimitSoftDesc(cPtr, false);
  }
  public NxJointLimitSoftDesc getLinearLimit_inplace(NxJointLimitSoftDesc result) {
    long cPtr = JPhysXAdapterJNI.NxD6JointDesc_linearLimit_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setSwing1Limit(NxJointLimitSoftDesc value) {
    JPhysXAdapterJNI.NxD6JointDesc_swing1Limit_set(swigCPtr, this, NxJointLimitSoftDesc.getCPtr(value), value);
  }


  public NxJointLimitSoftDesc getSwing1Limit() {
    long cPtr = JPhysXAdapterJNI.NxD6JointDesc_swing1Limit_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxJointLimitSoftDesc(cPtr, false);
  }
  public NxJointLimitSoftDesc getSwing1Limit_inplace(NxJointLimitSoftDesc result) {
    long cPtr = JPhysXAdapterJNI.NxD6JointDesc_swing1Limit_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setSwing2Limit(NxJointLimitSoftDesc value) {
    JPhysXAdapterJNI.NxD6JointDesc_swing2Limit_set(swigCPtr, this, NxJointLimitSoftDesc.getCPtr(value), value);
  }


  public NxJointLimitSoftDesc getSwing2Limit() {
    long cPtr = JPhysXAdapterJNI.NxD6JointDesc_swing2Limit_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxJointLimitSoftDesc(cPtr, false);
  }
  public NxJointLimitSoftDesc getSwing2Limit_inplace(NxJointLimitSoftDesc result) {
    long cPtr = JPhysXAdapterJNI.NxD6JointDesc_swing2Limit_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setTwistLimit(NxJointLimitSoftPairDesc value) {
    JPhysXAdapterJNI.NxD6JointDesc_twistLimit_set(swigCPtr, this, NxJointLimitSoftPairDesc.getCPtr(value), value);
  }


  public NxJointLimitSoftPairDesc getTwistLimit() {
    long cPtr = JPhysXAdapterJNI.NxD6JointDesc_twistLimit_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxJointLimitSoftPairDesc(cPtr, false);
  }
  public NxJointLimitSoftPairDesc getTwistLimit_inplace(NxJointLimitSoftPairDesc result) {
    long cPtr = JPhysXAdapterJNI.NxD6JointDesc_twistLimit_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setXDrive(NxJointDriveDesc value) {
    JPhysXAdapterJNI.NxD6JointDesc_xDrive_set(swigCPtr, this, NxJointDriveDesc.getCPtr(value), value);
  }


  public NxJointDriveDesc getXDrive() {
    long cPtr = JPhysXAdapterJNI.NxD6JointDesc_xDrive_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxJointDriveDesc(cPtr, false);
  }
  public NxJointDriveDesc getXDrive_inplace(NxJointDriveDesc result) {
    long cPtr = JPhysXAdapterJNI.NxD6JointDesc_xDrive_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setYDrive(NxJointDriveDesc value) {
    JPhysXAdapterJNI.NxD6JointDesc_yDrive_set(swigCPtr, this, NxJointDriveDesc.getCPtr(value), value);
  }


  public NxJointDriveDesc getYDrive() {
    long cPtr = JPhysXAdapterJNI.NxD6JointDesc_yDrive_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxJointDriveDesc(cPtr, false);
  }
  public NxJointDriveDesc getYDrive_inplace(NxJointDriveDesc result) {
    long cPtr = JPhysXAdapterJNI.NxD6JointDesc_yDrive_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setZDrive(NxJointDriveDesc value) {
    JPhysXAdapterJNI.NxD6JointDesc_zDrive_set(swigCPtr, this, NxJointDriveDesc.getCPtr(value), value);
  }


  public NxJointDriveDesc getZDrive() {
    long cPtr = JPhysXAdapterJNI.NxD6JointDesc_zDrive_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxJointDriveDesc(cPtr, false);
  }
  public NxJointDriveDesc getZDrive_inplace(NxJointDriveDesc result) {
    long cPtr = JPhysXAdapterJNI.NxD6JointDesc_zDrive_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setSwingDrive(NxJointDriveDesc value) {
    JPhysXAdapterJNI.NxD6JointDesc_swingDrive_set(swigCPtr, this, NxJointDriveDesc.getCPtr(value), value);
  }


  public NxJointDriveDesc getSwingDrive() {
    long cPtr = JPhysXAdapterJNI.NxD6JointDesc_swingDrive_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxJointDriveDesc(cPtr, false);
  }
  public NxJointDriveDesc getSwingDrive_inplace(NxJointDriveDesc result) {
    long cPtr = JPhysXAdapterJNI.NxD6JointDesc_swingDrive_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setTwistDrive(NxJointDriveDesc value) {
    JPhysXAdapterJNI.NxD6JointDesc_twistDrive_set(swigCPtr, this, NxJointDriveDesc.getCPtr(value), value);
  }


  public NxJointDriveDesc getTwistDrive() {
    long cPtr = JPhysXAdapterJNI.NxD6JointDesc_twistDrive_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxJointDriveDesc(cPtr, false);
  }
  public NxJointDriveDesc getTwistDrive_inplace(NxJointDriveDesc result) {
    long cPtr = JPhysXAdapterJNI.NxD6JointDesc_twistDrive_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setSlerpDrive(NxJointDriveDesc value) {
    JPhysXAdapterJNI.NxD6JointDesc_slerpDrive_set(swigCPtr, this, NxJointDriveDesc.getCPtr(value), value);
  }


  public NxJointDriveDesc getSlerpDrive() {
    long cPtr = JPhysXAdapterJNI.NxD6JointDesc_slerpDrive_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxJointDriveDesc(cPtr, false);
  }
  public NxJointDriveDesc getSlerpDrive_inplace(NxJointDriveDesc result) {
    long cPtr = JPhysXAdapterJNI.NxD6JointDesc_slerpDrive_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setDrivePosition(NxVec3 value) {
    JPhysXAdapterJNI.NxD6JointDesc_drivePosition_set(swigCPtr, this, NxVec3.getCPtr(value), value);
  }


  public NxVec3 getDrivePosition() {
    long cPtr = JPhysXAdapterJNI.NxD6JointDesc_drivePosition_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxVec3(cPtr, false);
  }
  public NxVec3 getDrivePosition_inplace(NxVec3 result) {
    long cPtr = JPhysXAdapterJNI.NxD6JointDesc_drivePosition_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setDriveOrientation(NxQuat value) {
    JPhysXAdapterJNI.NxD6JointDesc_driveOrientation_set(swigCPtr, this, NxQuat.getCPtr(value), value);
  }


  public NxQuat getDriveOrientation() {
    long cPtr = JPhysXAdapterJNI.NxD6JointDesc_driveOrientation_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxQuat(cPtr, false);
  }
  public NxQuat getDriveOrientation_inplace(NxQuat result) {
    long cPtr = JPhysXAdapterJNI.NxD6JointDesc_driveOrientation_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setDriveLinearVelocity(NxVec3 value) {
    JPhysXAdapterJNI.NxD6JointDesc_driveLinearVelocity_set(swigCPtr, this, NxVec3.getCPtr(value), value);
  }


  public NxVec3 getDriveLinearVelocity() {
    long cPtr = JPhysXAdapterJNI.NxD6JointDesc_driveLinearVelocity_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxVec3(cPtr, false);
  }
  public NxVec3 getDriveLinearVelocity_inplace(NxVec3 result) {
    long cPtr = JPhysXAdapterJNI.NxD6JointDesc_driveLinearVelocity_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setDriveAngularVelocity(NxVec3 value) {
    JPhysXAdapterJNI.NxD6JointDesc_driveAngularVelocity_set(swigCPtr, this, NxVec3.getCPtr(value), value);
  }


  public NxVec3 getDriveAngularVelocity() {
    long cPtr = JPhysXAdapterJNI.NxD6JointDesc_driveAngularVelocity_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxVec3(cPtr, false);
  }
  public NxVec3 getDriveAngularVelocity_inplace(NxVec3 result) {
    long cPtr = JPhysXAdapterJNI.NxD6JointDesc_driveAngularVelocity_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setProjectionMode(int value) {
    JPhysXAdapterJNI.NxD6JointDesc_projectionMode_set(swigCPtr, this, value);
  }


  public int getProjectionMode() {
    return JPhysXAdapterJNI.NxD6JointDesc_projectionMode_get(swigCPtr, this);
  }


  public void setProjectionDistance(float value) {
    JPhysXAdapterJNI.NxD6JointDesc_projectionDistance_set(swigCPtr, this, value);
  }


  public float getProjectionDistance() {
    return JPhysXAdapterJNI.NxD6JointDesc_projectionDistance_get(swigCPtr, this);
  }


  public void setProjectionAngle(float value) {
    JPhysXAdapterJNI.NxD6JointDesc_projectionAngle_set(swigCPtr, this, value);
  }


  public float getProjectionAngle() {
    return JPhysXAdapterJNI.NxD6JointDesc_projectionAngle_get(swigCPtr, this);
  }


  public void setGearRatio(float value) {
    JPhysXAdapterJNI.NxD6JointDesc_gearRatio_set(swigCPtr, this, value);
  }


  public float getGearRatio() {
    return JPhysXAdapterJNI.NxD6JointDesc_gearRatio_get(swigCPtr, this);
  }


  public void setFlags(long value) {
    JPhysXAdapterJNI.NxD6JointDesc_flags_set(swigCPtr, this, value);
  }


  public long getFlags() {
    return JPhysXAdapterJNI.NxD6JointDesc_flags_get(swigCPtr, this);
  }


  public NxD6JointDesc() {
    this(JPhysXAdapterJNI.new_NxD6JointDesc(), true);
  }


  public void setToDefault() {
    JPhysXAdapterJNI.NxD6JointDesc_setToDefault(swigCPtr, this);
  }


  public boolean isValid() {
    return JPhysXAdapterJNI.NxD6JointDesc_isValid(swigCPtr, this);
  }


}

