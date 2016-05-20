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
public class NxFluidDescBase {
  private long swigCPtr;
  boolean swigCMemOwn;
  
  /**
   * Default java constructor.
   * @param constructionMarker marks that this is java constructor. Clashing with C++ constructors is unlikely.
   */
  NxFluidDescBase(Boolean constructionMarker){}

  NxFluidDescBase(long cPtr, boolean cMemoryOwn) {
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
  public static long getCPtr(NxFluidDescBase obj) {
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
      JPhysXAdapterJNI.delete_NxFluidDescBase(swigCPtr);
    }

    swigCPtr = 0;
  }

  public boolean equalCPtr(NxFluidDescBase obj) {
    return obj.swigCPtr == this.swigCPtr;
  }


  public void setInitialParticleData(NxParticleData value) {
    JPhysXAdapterJNI.NxFluidDescBase_initialParticleData_set(swigCPtr, this, NxParticleData.getCPtr(value), value);
  }


  public NxParticleData getInitialParticleData() {
    long cPtr = JPhysXAdapterJNI.NxFluidDescBase_initialParticleData_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxParticleData(cPtr, false);
  }
  public NxParticleData getInitialParticleData_inplace(NxParticleData result) {
    long cPtr = JPhysXAdapterJNI.NxFluidDescBase_initialParticleData_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setMaxParticles(long value) {
    JPhysXAdapterJNI.NxFluidDescBase_maxParticles_set(swigCPtr, this, value);
  }


  public long getMaxParticles() {
    return JPhysXAdapterJNI.NxFluidDescBase_maxParticles_get(swigCPtr, this);
  }


  public void setNumReserveParticles(long value) {
    JPhysXAdapterJNI.NxFluidDescBase_numReserveParticles_set(swigCPtr, this, value);
  }


  public long getNumReserveParticles() {
    return JPhysXAdapterJNI.NxFluidDescBase_numReserveParticles_get(swigCPtr, this);
  }


  public void setRestParticlesPerMeter(float value) {
    JPhysXAdapterJNI.NxFluidDescBase_restParticlesPerMeter_set(swigCPtr, this, value);
  }


  public float getRestParticlesPerMeter() {
    return JPhysXAdapterJNI.NxFluidDescBase_restParticlesPerMeter_get(swigCPtr, this);
  }


  public void setRestDensity(float value) {
    JPhysXAdapterJNI.NxFluidDescBase_restDensity_set(swigCPtr, this, value);
  }


  public float getRestDensity() {
    return JPhysXAdapterJNI.NxFluidDescBase_restDensity_get(swigCPtr, this);
  }


  public void setKernelRadiusMultiplier(float value) {
    JPhysXAdapterJNI.NxFluidDescBase_kernelRadiusMultiplier_set(swigCPtr, this, value);
  }


  public float getKernelRadiusMultiplier() {
    return JPhysXAdapterJNI.NxFluidDescBase_kernelRadiusMultiplier_get(swigCPtr, this);
  }


  public void setMotionLimitMultiplier(float value) {
    JPhysXAdapterJNI.NxFluidDescBase_motionLimitMultiplier_set(swigCPtr, this, value);
  }


  public float getMotionLimitMultiplier() {
    return JPhysXAdapterJNI.NxFluidDescBase_motionLimitMultiplier_get(swigCPtr, this);
  }


  public void setCollisionDistanceMultiplier(float value) {
    JPhysXAdapterJNI.NxFluidDescBase_collisionDistanceMultiplier_set(swigCPtr, this, value);
  }


  public float getCollisionDistanceMultiplier() {
    return JPhysXAdapterJNI.NxFluidDescBase_collisionDistanceMultiplier_get(swigCPtr, this);
  }


  public void setPacketSizeMultiplier(long value) {
    JPhysXAdapterJNI.NxFluidDescBase_packetSizeMultiplier_set(swigCPtr, this, value);
  }


  public long getPacketSizeMultiplier() {
    return JPhysXAdapterJNI.NxFluidDescBase_packetSizeMultiplier_get(swigCPtr, this);
  }


  public void setStiffness(float value) {
    JPhysXAdapterJNI.NxFluidDescBase_stiffness_set(swigCPtr, this, value);
  }


  public float getStiffness() {
    return JPhysXAdapterJNI.NxFluidDescBase_stiffness_get(swigCPtr, this);
  }


  public void setViscosity(float value) {
    JPhysXAdapterJNI.NxFluidDescBase_viscosity_set(swigCPtr, this, value);
  }


  public float getViscosity() {
    return JPhysXAdapterJNI.NxFluidDescBase_viscosity_get(swigCPtr, this);
  }


  public void setSurfaceTension(float value) {
    JPhysXAdapterJNI.NxFluidDescBase_surfaceTension_set(swigCPtr, this, value);
  }


  public float getSurfaceTension() {
    return JPhysXAdapterJNI.NxFluidDescBase_surfaceTension_get(swigCPtr, this);
  }


  public void setDamping(float value) {
    JPhysXAdapterJNI.NxFluidDescBase_damping_set(swigCPtr, this, value);
  }


  public float getDamping() {
    return JPhysXAdapterJNI.NxFluidDescBase_damping_get(swigCPtr, this);
  }


  public void setFadeInTime(float value) {
    JPhysXAdapterJNI.NxFluidDescBase_fadeInTime_set(swigCPtr, this, value);
  }


  public float getFadeInTime() {
    return JPhysXAdapterJNI.NxFluidDescBase_fadeInTime_get(swigCPtr, this);
  }


  public void setExternalAcceleration(NxVec3 value) {
    JPhysXAdapterJNI.NxFluidDescBase_externalAcceleration_set(swigCPtr, this, NxVec3.getCPtr(value), value);
  }


  public NxVec3 getExternalAcceleration() {
    long cPtr = JPhysXAdapterJNI.NxFluidDescBase_externalAcceleration_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxVec3(cPtr, false);
  }
  public NxVec3 getExternalAcceleration_inplace(NxVec3 result) {
    long cPtr = JPhysXAdapterJNI.NxFluidDescBase_externalAcceleration_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setProjectionPlane(NxPlane value) {
    JPhysXAdapterJNI.NxFluidDescBase_projectionPlane_set(swigCPtr, this, NxPlane.getCPtr(value), value);
  }


  public NxPlane getProjectionPlane() {
    long cPtr = JPhysXAdapterJNI.NxFluidDescBase_projectionPlane_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxPlane(cPtr, false);
  }
  public NxPlane getProjectionPlane_inplace(NxPlane result) {
    long cPtr = JPhysXAdapterJNI.NxFluidDescBase_projectionPlane_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setRestitutionForStaticShapes(float value) {
    JPhysXAdapterJNI.NxFluidDescBase_restitutionForStaticShapes_set(swigCPtr, this, value);
  }


  public float getRestitutionForStaticShapes() {
    return JPhysXAdapterJNI.NxFluidDescBase_restitutionForStaticShapes_get(swigCPtr, this);
  }


  public void setDynamicFrictionForStaticShapes(float value) {
    JPhysXAdapterJNI.NxFluidDescBase_dynamicFrictionForStaticShapes_set(swigCPtr, this, value);
  }


  public float getDynamicFrictionForStaticShapes() {
    return JPhysXAdapterJNI.NxFluidDescBase_dynamicFrictionForStaticShapes_get(swigCPtr, this);
  }


  public void setStaticFrictionForStaticShapes(float value) {
    JPhysXAdapterJNI.NxFluidDescBase_staticFrictionForStaticShapes_set(swigCPtr, this, value);
  }


  public float getStaticFrictionForStaticShapes() {
    return JPhysXAdapterJNI.NxFluidDescBase_staticFrictionForStaticShapes_get(swigCPtr, this);
  }


  public void setAttractionForStaticShapes(float value) {
    JPhysXAdapterJNI.NxFluidDescBase_attractionForStaticShapes_set(swigCPtr, this, value);
  }


  public float getAttractionForStaticShapes() {
    return JPhysXAdapterJNI.NxFluidDescBase_attractionForStaticShapes_get(swigCPtr, this);
  }


  public void setRestitutionForDynamicShapes(float value) {
    JPhysXAdapterJNI.NxFluidDescBase_restitutionForDynamicShapes_set(swigCPtr, this, value);
  }


  public float getRestitutionForDynamicShapes() {
    return JPhysXAdapterJNI.NxFluidDescBase_restitutionForDynamicShapes_get(swigCPtr, this);
  }


  public void setDynamicFrictionForDynamicShapes(float value) {
    JPhysXAdapterJNI.NxFluidDescBase_dynamicFrictionForDynamicShapes_set(swigCPtr, this, value);
  }


  public float getDynamicFrictionForDynamicShapes() {
    return JPhysXAdapterJNI.NxFluidDescBase_dynamicFrictionForDynamicShapes_get(swigCPtr, this);
  }


  public void setStaticFrictionForDynamicShapes(float value) {
    JPhysXAdapterJNI.NxFluidDescBase_staticFrictionForDynamicShapes_set(swigCPtr, this, value);
  }


  public float getStaticFrictionForDynamicShapes() {
    return JPhysXAdapterJNI.NxFluidDescBase_staticFrictionForDynamicShapes_get(swigCPtr, this);
  }


  public void setAttractionForDynamicShapes(float value) {
    JPhysXAdapterJNI.NxFluidDescBase_attractionForDynamicShapes_set(swigCPtr, this, value);
  }


  public float getAttractionForDynamicShapes() {
    return JPhysXAdapterJNI.NxFluidDescBase_attractionForDynamicShapes_get(swigCPtr, this);
  }


  public void setCollisionResponseCoefficient(float value) {
    JPhysXAdapterJNI.NxFluidDescBase_collisionResponseCoefficient_set(swigCPtr, this, value);
  }


  public float getCollisionResponseCoefficient() {
    return JPhysXAdapterJNI.NxFluidDescBase_collisionResponseCoefficient_get(swigCPtr, this);
  }


  public void setSimulationMethod(long value) {
    JPhysXAdapterJNI.NxFluidDescBase_simulationMethod_set(swigCPtr, this, value);
  }


  public long getSimulationMethod() {
    return JPhysXAdapterJNI.NxFluidDescBase_simulationMethod_get(swigCPtr, this);
  }


  public void setCollisionMethod(long value) {
    JPhysXAdapterJNI.NxFluidDescBase_collisionMethod_set(swigCPtr, this, value);
  }


  public long getCollisionMethod() {
    return JPhysXAdapterJNI.NxFluidDescBase_collisionMethod_get(swigCPtr, this);
  }


  public void setCollisionGroup(int value) {
    JPhysXAdapterJNI.NxFluidDescBase_collisionGroup_set(swigCPtr, this, value);
  }


  public int getCollisionGroup() {
    return JPhysXAdapterJNI.NxFluidDescBase_collisionGroup_get(swigCPtr, this);
  }


  public void setGroupsMask(NxGroupsMask value) {
    JPhysXAdapterJNI.NxFluidDescBase_groupsMask_set(swigCPtr, this, NxGroupsMask.getCPtr(value), value);
  }


  public NxGroupsMask getGroupsMask() {
    long cPtr = JPhysXAdapterJNI.NxFluidDescBase_groupsMask_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxGroupsMask(cPtr, false);
  }
  public NxGroupsMask getGroupsMask_inplace(NxGroupsMask result) {
    long cPtr = JPhysXAdapterJNI.NxFluidDescBase_groupsMask_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setForceFieldMaterial(int value) {
    JPhysXAdapterJNI.NxFluidDescBase_forceFieldMaterial_set(swigCPtr, this, value);
  }


  public int getForceFieldMaterial() {
    return JPhysXAdapterJNI.NxFluidDescBase_forceFieldMaterial_get(swigCPtr, this);
  }


  public void setParticlesWriteData(NxParticleData value) {
    JPhysXAdapterJNI.NxFluidDescBase_particlesWriteData_set(swigCPtr, this, NxParticleData.getCPtr(value), value);
  }


  public NxParticleData getParticlesWriteData() {
    long cPtr = JPhysXAdapterJNI.NxFluidDescBase_particlesWriteData_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxParticleData(cPtr, false);
  }
  public NxParticleData getParticlesWriteData_inplace(NxParticleData result) {
    long cPtr = JPhysXAdapterJNI.NxFluidDescBase_particlesWriteData_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setParticleDeletionIdWriteData(NxParticleIdData value) {
    JPhysXAdapterJNI.NxFluidDescBase_particleDeletionIdWriteData_set(swigCPtr, this, NxParticleIdData.getCPtr(value), value);
  }


  public NxParticleIdData getParticleDeletionIdWriteData() {
    long cPtr = JPhysXAdapterJNI.NxFluidDescBase_particleDeletionIdWriteData_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxParticleIdData(cPtr, false);
  }
  public NxParticleIdData getParticleDeletionIdWriteData_inplace(NxParticleIdData result) {
    long cPtr = JPhysXAdapterJNI.NxFluidDescBase_particleDeletionIdWriteData_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setParticleCreationIdWriteData(NxParticleIdData value) {
    JPhysXAdapterJNI.NxFluidDescBase_particleCreationIdWriteData_set(swigCPtr, this, NxParticleIdData.getCPtr(value), value);
  }


  public NxParticleIdData getParticleCreationIdWriteData() {
    long cPtr = JPhysXAdapterJNI.NxFluidDescBase_particleCreationIdWriteData_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxParticleIdData(cPtr, false);
  }
  public NxParticleIdData getParticleCreationIdWriteData_inplace(NxParticleIdData result) {
    long cPtr = JPhysXAdapterJNI.NxFluidDescBase_particleCreationIdWriteData_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setFluidPacketData(NxFluidPacketData value) {
    JPhysXAdapterJNI.NxFluidDescBase_fluidPacketData_set(swigCPtr, this, NxFluidPacketData.getCPtr(value), value);
  }


  public NxFluidPacketData getFluidPacketData() {
    long cPtr = JPhysXAdapterJNI.NxFluidDescBase_fluidPacketData_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxFluidPacketData(cPtr, false);
  }
  public NxFluidPacketData getFluidPacketData_inplace(NxFluidPacketData result) {
    long cPtr = JPhysXAdapterJNI.NxFluidDescBase_fluidPacketData_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setFlags(long value) {
    JPhysXAdapterJNI.NxFluidDescBase_flags_set(swigCPtr, this, value);
  }


  public long getFlags() {
    return JPhysXAdapterJNI.NxFluidDescBase_flags_get(swigCPtr, this);
  }


  public void setUserData(SWIGTYPE_p_void value) {
    JPhysXAdapterJNI.NxFluidDescBase_userData_set(swigCPtr, this, SWIGTYPE_p_void.getCPtr(value));
  }


  public SWIGTYPE_p_void getUserData() {
    long cPtr = JPhysXAdapterJNI.NxFluidDescBase_userData_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }


  public void setName(String value) {
    JPhysXAdapterJNI.NxFluidDescBase_name_set(swigCPtr, this, value);
  }


  public String getName() {
    return JPhysXAdapterJNI.NxFluidDescBase_name_get(swigCPtr, this);
  }


  public void setCompartment(NxCompartment value) {
    JPhysXAdapterJNI.NxFluidDescBase_compartment_set(swigCPtr, this, NxCompartment.getCPtr(value), value);
  }


  public NxCompartment getCompartment() {
    long cPtr = JPhysXAdapterJNI.NxFluidDescBase_compartment_get(swigCPtr, this);
    return (cPtr == 0) ? null : new NxCompartment(cPtr, false);
  }
  public NxCompartment getCompartment_inplace(NxCompartment result) {
    long cPtr = JPhysXAdapterJNI.NxFluidDescBase_compartment_get(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public NxFluidDescBase() {
    this(JPhysXAdapterJNI.new_NxFluidDescBase(), true);
  }


  public void setToDefault() {
    JPhysXAdapterJNI.NxFluidDescBase_setToDefault(swigCPtr, this);
  }


  public boolean isValid() {
    return JPhysXAdapterJNI.NxFluidDescBase_isValid(swigCPtr, this);
  }


  public int getType() {
    return JPhysXAdapterJNI.NxFluidDescBase_getType(swigCPtr, this);
  }


}

