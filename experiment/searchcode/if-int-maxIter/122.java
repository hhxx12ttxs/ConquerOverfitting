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
public class NxScene {
  private long swigCPtr;
  boolean swigCMemOwn;
  
  /**
   * Default java constructor.
   * @param constructionMarker marks that this is java constructor. Clashing with C++ constructors is unlikely.
   */
  NxScene(Boolean constructionMarker){}

  NxScene(long cPtr, boolean cMemoryOwn) {
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
  public static long getCPtr(NxScene obj) {
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

  public boolean equalCPtr(NxScene obj) {
    return obj.swigCPtr == this.swigCPtr;
  }


  public boolean saveToDesc(NxSceneDesc desc) {
    return JPhysXAdapterJNI.NxScene_saveToDesc(swigCPtr, this, NxSceneDesc.getCPtr(desc), desc);
  }


  public long getFlags() {
    return JPhysXAdapterJNI.NxScene_getFlags(swigCPtr, this);
  }


  public int getSimType() {
    return JPhysXAdapterJNI.NxScene_getSimType(swigCPtr, this);
  }


  public SWIGTYPE_p_void getInternal() {
    long cPtr = JPhysXAdapterJNI.NxScene_getInternal(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }


  public void setGravity(NxVec3 vec) {
    JPhysXAdapterJNI.NxScene_setGravity(swigCPtr, this, NxVec3.getCPtr(vec), vec);
  }


  public void getGravity(NxVec3 vec) {
    JPhysXAdapterJNI.NxScene_getGravity(swigCPtr, this, NxVec3.getCPtr(vec), vec);
  }


  public NxActor createActor(NxActorDescBase desc) {
    long cPtr = JPhysXAdapterJNI.NxScene_createActor(swigCPtr, this, NxActorDescBase.getCPtr(desc), desc);
    return (cPtr == 0) ? null : new NxActor(cPtr, false);
  }
  public NxActor createActor_inplace(NxActor result, NxActorDescBase desc) {
    long cPtr = JPhysXAdapterJNI.NxScene_createActor(swigCPtr, this, NxActorDescBase.getCPtr(desc), desc);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void releaseActor(NxActor actor) {
    JPhysXAdapterJNI.NxScene_releaseActor(swigCPtr, this, NxActor.getCPtr(actor), actor);
  }


  
  /**
   * To down-cast the result, you can't use the
   * <p><code>
   * NxD6Joint d6Joint = (NxD6Joint)scene.createJoint(d6JointDesc);//class cast exception
   * </code><p>
   * Instead you can use the follow:
   * <p><code>
   * NxJoint joint = scene.createJoint(jointDesc);<br>
   * NxD6Joint d6Joint = joint.{@link NxJoint#isD6Joint() isD6Joint}();
   * </code><p>(and other is... methods).
   * @param jointDesc description of the joint
   * @return constructed joint
   */
  public NxJoint createJoint(NxJointDesc jointDesc) {
    long cPtr = JPhysXAdapterJNI.NxScene_createJoint(swigCPtr, this, NxJointDesc.getCPtr(jointDesc), jointDesc);
    return (cPtr == 0) ? null : new NxJoint(cPtr, false);
  }
  public NxJoint createJoint_inplace(NxJoint result, NxJointDesc jointDesc) {
    long cPtr = JPhysXAdapterJNI.NxScene_createJoint(swigCPtr, this, NxJointDesc.getCPtr(jointDesc), jointDesc);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void releaseJoint(NxJoint joint) {
    JPhysXAdapterJNI.NxScene_releaseJoint(swigCPtr, this, NxJoint.getCPtr(joint), joint);
  }


  public SWIGTYPE_p_NxSpringAndDamperEffector createSpringAndDamperEffector(SWIGTYPE_p_NxSpringAndDamperEffectorDesc springDesc) {
    long cPtr = JPhysXAdapterJNI.NxScene_createSpringAndDamperEffector(swigCPtr, this, SWIGTYPE_p_NxSpringAndDamperEffectorDesc.getCPtr(springDesc));
    return (cPtr == 0) ? null : new SWIGTYPE_p_NxSpringAndDamperEffector(cPtr, false);
  }


  public NxEffector createEffector(NxEffectorDesc desc) {
    long cPtr = JPhysXAdapterJNI.NxScene_createEffector(swigCPtr, this, NxEffectorDesc.getCPtr(desc), desc);
    return (cPtr == 0) ? null : new NxEffector(cPtr, false);
  }
  public NxEffector createEffector_inplace(NxEffector result, NxEffectorDesc desc) {
    long cPtr = JPhysXAdapterJNI.NxScene_createEffector(swigCPtr, this, NxEffectorDesc.getCPtr(desc), desc);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void releaseEffector(NxEffector effector) {
    JPhysXAdapterJNI.NxScene_releaseEffector(swigCPtr, this, NxEffector.getCPtr(effector), effector);
  }


  public NxForceField createForceField(NxForceFieldDesc forceFieldDesc) {
    long cPtr = JPhysXAdapterJNI.NxScene_createForceField(swigCPtr, this, NxForceFieldDesc.getCPtr(forceFieldDesc), forceFieldDesc);
    return (cPtr == 0) ? null : new NxForceField(cPtr, false);
  }
  public NxForceField createForceField_inplace(NxForceField result, NxForceFieldDesc forceFieldDesc) {
    long cPtr = JPhysXAdapterJNI.NxScene_createForceField(swigCPtr, this, NxForceFieldDesc.getCPtr(forceFieldDesc), forceFieldDesc);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void releaseForceField(NxForceField forceField) {
    JPhysXAdapterJNI.NxScene_releaseForceField(swigCPtr, this, NxForceField.getCPtr(forceField), forceField);
  }


  public long getNbForceFields() {
    return JPhysXAdapterJNI.NxScene_getNbForceFields(swigCPtr, this);
  }


  public SWIGTYPE_p_p_NxForceField getForceFields() {
    long cPtr = JPhysXAdapterJNI.NxScene_getForceFields(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_p_NxForceField(cPtr, false);
  }


  public SWIGTYPE_p_NxForceFieldLinearKernel createForceFieldLinearKernel(SWIGTYPE_p_NxForceFieldLinearKernelDesc kernelDesc) {
    long cPtr = JPhysXAdapterJNI.NxScene_createForceFieldLinearKernel(swigCPtr, this, SWIGTYPE_p_NxForceFieldLinearKernelDesc.getCPtr(kernelDesc));
    return (cPtr == 0) ? null : new SWIGTYPE_p_NxForceFieldLinearKernel(cPtr, false);
  }


  public void releaseForceFieldLinearKernel(SWIGTYPE_p_NxForceFieldLinearKernel kernel) {
    JPhysXAdapterJNI.NxScene_releaseForceFieldLinearKernel(swigCPtr, this, SWIGTYPE_p_NxForceFieldLinearKernel.getCPtr(kernel));
  }


  public long getNbForceFieldLinearKernels() {
    return JPhysXAdapterJNI.NxScene_getNbForceFieldLinearKernels(swigCPtr, this);
  }


  public void resetForceFieldLinearKernelsIterator() {
    JPhysXAdapterJNI.NxScene_resetForceFieldLinearKernelsIterator(swigCPtr, this);
  }


  public SWIGTYPE_p_NxForceFieldLinearKernel getNextForceFieldLinearKernel() {
    long cPtr = JPhysXAdapterJNI.NxScene_getNextForceFieldLinearKernel(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_NxForceFieldLinearKernel(cPtr, false);
  }


  public SWIGTYPE_p_NxForceFieldShapeGroup createForceFieldShapeGroup(SWIGTYPE_p_NxForceFieldShapeGroupDesc desc) {
    long cPtr = JPhysXAdapterJNI.NxScene_createForceFieldShapeGroup(swigCPtr, this, SWIGTYPE_p_NxForceFieldShapeGroupDesc.getCPtr(desc));
    return (cPtr == 0) ? null : new SWIGTYPE_p_NxForceFieldShapeGroup(cPtr, false);
  }


  public void releaseForceFieldShapeGroup(SWIGTYPE_p_NxForceFieldShapeGroup group) {
    JPhysXAdapterJNI.NxScene_releaseForceFieldShapeGroup(swigCPtr, this, SWIGTYPE_p_NxForceFieldShapeGroup.getCPtr(group));
  }


  public long getNbForceFieldShapeGroups() {
    return JPhysXAdapterJNI.NxScene_getNbForceFieldShapeGroups(swigCPtr, this);
  }


  public void resetForceFieldShapeGroupsIterator() {
    JPhysXAdapterJNI.NxScene_resetForceFieldShapeGroupsIterator(swigCPtr, this);
  }


  public SWIGTYPE_p_NxForceFieldShapeGroup getNextForceFieldShapeGroup() {
    long cPtr = JPhysXAdapterJNI.NxScene_getNextForceFieldShapeGroup(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_NxForceFieldShapeGroup(cPtr, false);
  }


  public int createForceFieldVariety() {
    return JPhysXAdapterJNI.NxScene_createForceFieldVariety(swigCPtr, this);
  }


  public int getHighestForceFieldVariety() {
    return JPhysXAdapterJNI.NxScene_getHighestForceFieldVariety(swigCPtr, this);
  }


  public void releaseForceFieldVariety(int var) {
    JPhysXAdapterJNI.NxScene_releaseForceFieldVariety(swigCPtr, this, var);
  }


  public int createForceFieldMaterial() {
    return JPhysXAdapterJNI.NxScene_createForceFieldMaterial(swigCPtr, this);
  }


  public int getHighestForceFieldMaterial() {
    return JPhysXAdapterJNI.NxScene_getHighestForceFieldMaterial(swigCPtr, this);
  }


  public void releaseForceFieldMaterial(int mat) {
    JPhysXAdapterJNI.NxScene_releaseForceFieldMaterial(swigCPtr, this, mat);
  }


  public float getForceFieldScale(int var, int mat) {
    return JPhysXAdapterJNI.NxScene_getForceFieldScale(swigCPtr, this, var, mat);
  }


  public void setForceFieldScale(int var, int mat, float val) {
    JPhysXAdapterJNI.NxScene_setForceFieldScale(swigCPtr, this, var, mat, val);
  }


  public NxMaterial createMaterial(NxMaterialDesc matDesc) {
    long cPtr = JPhysXAdapterJNI.NxScene_createMaterial(swigCPtr, this, NxMaterialDesc.getCPtr(matDesc), matDesc);
    return (cPtr == 0) ? null : new NxMaterial(cPtr, false);
  }
  public NxMaterial createMaterial_inplace(NxMaterial result, NxMaterialDesc matDesc) {
    long cPtr = JPhysXAdapterJNI.NxScene_createMaterial(swigCPtr, this, NxMaterialDesc.getCPtr(matDesc), matDesc);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void releaseMaterial(NxMaterial material) {
    JPhysXAdapterJNI.NxScene_releaseMaterial(swigCPtr, this, NxMaterial.getCPtr(material), material);
  }


  public NxCompartment createCompartment(NxCompartmentDesc compDesc) {
    long cPtr = JPhysXAdapterJNI.NxScene_createCompartment(swigCPtr, this, NxCompartmentDesc.getCPtr(compDesc), compDesc);
    return (cPtr == 0) ? null : new NxCompartment(cPtr, false);
  }
  public NxCompartment createCompartment_inplace(NxCompartment result, NxCompartmentDesc compDesc) {
    long cPtr = JPhysXAdapterJNI.NxScene_createCompartment(swigCPtr, this, NxCompartmentDesc.getCPtr(compDesc), compDesc);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public long getNbCompartments() {
    return JPhysXAdapterJNI.NxScene_getNbCompartments(swigCPtr, this);
  }


  public long getCompartmentArray(SWIGTYPE_p_p_NxCompartment userBuffer, long bufferSize, SWIGTYPE_p_unsigned_int usersIterator) {
    return JPhysXAdapterJNI.NxScene_getCompartmentArray(swigCPtr, this, SWIGTYPE_p_p_NxCompartment.getCPtr(userBuffer), bufferSize, SWIGTYPE_p_unsigned_int.getCPtr(usersIterator));
  }


  public void setActorPairFlags(NxActor actorA, NxActor actorB, long nxContactPairFlag) {
    JPhysXAdapterJNI.NxScene_setActorPairFlags(swigCPtr, this, NxActor.getCPtr(actorA), actorA, NxActor.getCPtr(actorB), actorB, nxContactPairFlag);
  }


  public long getActorPairFlags(NxActor actorA, NxActor actorB) {
    return JPhysXAdapterJNI.NxScene_getActorPairFlags(swigCPtr, this, NxActor.getCPtr(actorA), actorA, NxActor.getCPtr(actorB), actorB);
  }


  public void setShapePairFlags(NxShape shapeA, NxShape shapeB, long nxContactPairFlag) {
    JPhysXAdapterJNI.NxScene_setShapePairFlags(swigCPtr, this, NxShape.getCPtr(shapeA), shapeA, NxShape.getCPtr(shapeB), shapeB, nxContactPairFlag);
  }


  public long getShapePairFlags(NxShape shapeA, NxShape shapeB) {
    return JPhysXAdapterJNI.NxScene_getShapePairFlags(swigCPtr, this, NxShape.getCPtr(shapeA), shapeA, NxShape.getCPtr(shapeB), shapeB);
  }


  public long getNbPairs() {
    return JPhysXAdapterJNI.NxScene_getNbPairs(swigCPtr, this);
  }


  public long getPairFlagArray(NxPairFlag userArray, long numPairs) {
    return JPhysXAdapterJNI.NxScene_getPairFlagArray(swigCPtr, this, NxPairFlag.getCPtr(userArray), userArray, numPairs);
  }


  public void setGroupCollisionFlag(int group1, int group2, boolean enable) {
    JPhysXAdapterJNI.NxScene_setGroupCollisionFlag(swigCPtr, this, group1, group2, enable);
  }


  public boolean getGroupCollisionFlag(int group1, int group2) {
    return JPhysXAdapterJNI.NxScene_getGroupCollisionFlag(swigCPtr, this, group1, group2);
  }


  public void setDominanceGroupPair(int group1, int group2, NxConstraintDominance dominance) {
    JPhysXAdapterJNI.NxScene_setDominanceGroupPair(swigCPtr, this, group1, group2, NxConstraintDominance.getCPtr(dominance), dominance);
  }


  public NxConstraintDominance getDominanceGroupPair(int group1, int group2) {
    return new NxConstraintDominance(JPhysXAdapterJNI.NxScene_getDominanceGroupPair(swigCPtr, this, group1, group2), true);
  }
  public NxConstraintDominance getDominanceGroupPair_inplace(NxConstraintDominance result, int group1, int group2) {
    long cPtr = JPhysXAdapterJNI.NxScene_getDominanceGroupPair(swigCPtr, this, group1, group2);
        if(cPtr != 0){
            result.replaceCPart(cPtr, true);
            return result;
        }
        return null;
  }



  public void setActorGroupPairFlags(int group1, int group2, long flags) {
    JPhysXAdapterJNI.NxScene_setActorGroupPairFlags(swigCPtr, this, group1, group2, flags);
  }


  public long getActorGroupPairFlags(int group1, int group2) {
    return JPhysXAdapterJNI.NxScene_getActorGroupPairFlags(swigCPtr, this, group1, group2);
  }


  public long getNbActorGroupPairs() {
    return JPhysXAdapterJNI.NxScene_getNbActorGroupPairs(swigCPtr, this);
  }


  public long getActorGroupPairArray(NxActorGroupPair userBuffer, long bufferSize, SWIGTYPE_p_unsigned_int userIterator) {
    return JPhysXAdapterJNI.NxScene_getActorGroupPairArray(swigCPtr, this, NxActorGroupPair.getCPtr(userBuffer), userBuffer, bufferSize, SWIGTYPE_p_unsigned_int.getCPtr(userIterator));
  }


  public void setFilterOps(int op0, int op1, int op2) {
    JPhysXAdapterJNI.NxScene_setFilterOps(swigCPtr, this, op0, op1, op2);
  }


  public void setFilterBool(boolean flag) {
    JPhysXAdapterJNI.NxScene_setFilterBool(swigCPtr, this, flag);
  }


  public void setFilterConstant0(NxGroupsMask mask) {
    JPhysXAdapterJNI.NxScene_setFilterConstant0(swigCPtr, this, NxGroupsMask.getCPtr(mask), mask);
  }


  public void setFilterConstant1(NxGroupsMask mask) {
    JPhysXAdapterJNI.NxScene_setFilterConstant1(swigCPtr, this, NxGroupsMask.getCPtr(mask), mask);
  }


  public void getFilterOps(SWIGTYPE_p_NxFilterOp op0, SWIGTYPE_p_NxFilterOp op1, SWIGTYPE_p_NxFilterOp op2) {
    JPhysXAdapterJNI.NxScene_getFilterOps(swigCPtr, this, SWIGTYPE_p_NxFilterOp.getCPtr(op0), SWIGTYPE_p_NxFilterOp.getCPtr(op1), SWIGTYPE_p_NxFilterOp.getCPtr(op2));
  }


  public boolean getFilterBool() {
    return JPhysXAdapterJNI.NxScene_getFilterBool(swigCPtr, this);
  }


  public NxGroupsMask getFilterConstant0() {
    return new NxGroupsMask(JPhysXAdapterJNI.NxScene_getFilterConstant0(swigCPtr, this), true);
  }
  public NxGroupsMask getFilterConstant0_inplace(NxGroupsMask result) {
    long cPtr = JPhysXAdapterJNI.NxScene_getFilterConstant0(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, true);
            return result;
        }
        return null;
  }



  public NxGroupsMask getFilterConstant1() {
    return new NxGroupsMask(JPhysXAdapterJNI.NxScene_getFilterConstant1(swigCPtr, this), true);
  }
  public NxGroupsMask getFilterConstant1_inplace(NxGroupsMask result) {
    long cPtr = JPhysXAdapterJNI.NxScene_getFilterConstant1(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, true);
            return result;
        }
        return null;
  }



  public long getNbActors() {
    return JPhysXAdapterJNI.NxScene_getNbActors(swigCPtr, this);
  }


  public SWIGTYPE_p_p_NxActor getActors() {
    long cPtr = JPhysXAdapterJNI.NxScene_getActors(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_p_NxActor(cPtr, false);
  }


  public NxActiveTransform getActiveTransforms(SWIGTYPE_p_unsigned_int nbTransformsOut) {
    long cPtr = JPhysXAdapterJNI.NxScene_getActiveTransforms(swigCPtr, this, SWIGTYPE_p_unsigned_int.getCPtr(nbTransformsOut));
    return (cPtr == 0) ? null : new NxActiveTransform(cPtr, false);
  }
  public NxActiveTransform getActiveTransforms_inplace(NxActiveTransform result, SWIGTYPE_p_unsigned_int nbTransformsOut) {
    long cPtr = JPhysXAdapterJNI.NxScene_getActiveTransforms(swigCPtr, this, SWIGTYPE_p_unsigned_int.getCPtr(nbTransformsOut));
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public long getNbStaticShapes() {
    return JPhysXAdapterJNI.NxScene_getNbStaticShapes(swigCPtr, this);
  }


  public long getNbDynamicShapes() {
    return JPhysXAdapterJNI.NxScene_getNbDynamicShapes(swigCPtr, this);
  }


  public long getTotalNbShapes() {
    return JPhysXAdapterJNI.NxScene_getTotalNbShapes(swigCPtr, this);
  }


  public long getNbJoints() {
    return JPhysXAdapterJNI.NxScene_getNbJoints(swigCPtr, this);
  }


  public void resetJointIterator() {
    JPhysXAdapterJNI.NxScene_resetJointIterator(swigCPtr, this);
  }


  public NxJoint getNextJoint() {
    long cPtr = JPhysXAdapterJNI.NxScene_getNextJoint(swigCPtr, this);
    return (cPtr == 0) ? null : new NxJoint(cPtr, false);
  }
  public NxJoint getNextJoint_inplace(NxJoint result) {
    long cPtr = JPhysXAdapterJNI.NxScene_getNextJoint(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public long getNbEffectors() {
    return JPhysXAdapterJNI.NxScene_getNbEffectors(swigCPtr, this);
  }


  public void resetEffectorIterator() {
    JPhysXAdapterJNI.NxScene_resetEffectorIterator(swigCPtr, this);
  }


  public NxEffector getNextEffector() {
    long cPtr = JPhysXAdapterJNI.NxScene_getNextEffector(swigCPtr, this);
    return (cPtr == 0) ? null : new NxEffector(cPtr, false);
  }
  public NxEffector getNextEffector_inplace(NxEffector result) {
    long cPtr = JPhysXAdapterJNI.NxScene_getNextEffector(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public long getBoundForIslandSize(NxActor actor) {
    return JPhysXAdapterJNI.NxScene_getBoundForIslandSize(swigCPtr, this, NxActor.getCPtr(actor), actor);
  }


  public long getIslandArrayFromActor(NxActor actor, SWIGTYPE_p_p_NxActor userBuffer, long bufferSize, SWIGTYPE_p_unsigned_int userIterator) {
    return JPhysXAdapterJNI.NxScene_getIslandArrayFromActor(swigCPtr, this, NxActor.getCPtr(actor), actor, SWIGTYPE_p_p_NxActor.getCPtr(userBuffer), bufferSize, SWIGTYPE_p_unsigned_int.getCPtr(userIterator));
  }


  public long getNbMaterials() {
    return JPhysXAdapterJNI.NxScene_getNbMaterials(swigCPtr, this);
  }


  public long getMaterialArray(SWIGTYPE_p_p_NxMaterial userBuffer, long bufferSize, SWIGTYPE_p_unsigned_int usersIterator) {
    return JPhysXAdapterJNI.NxScene_getMaterialArray(swigCPtr, this, SWIGTYPE_p_p_NxMaterial.getCPtr(userBuffer), bufferSize, SWIGTYPE_p_unsigned_int.getCPtr(usersIterator));
  }


  public int getHighestMaterialIndex() {
    return JPhysXAdapterJNI.NxScene_getHighestMaterialIndex(swigCPtr, this);
  }


  public NxMaterial getMaterialFromIndex(int matIndex) {
    long cPtr = JPhysXAdapterJNI.NxScene_getMaterialFromIndex(swigCPtr, this, matIndex);
    return (cPtr == 0) ? null : new NxMaterial(cPtr, false);
  }
  public NxMaterial getMaterialFromIndex_inplace(NxMaterial result, int matIndex) {
    long cPtr = JPhysXAdapterJNI.NxScene_getMaterialFromIndex(swigCPtr, this, matIndex);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void flushStream() {
    JPhysXAdapterJNI.NxScene_flushStream(swigCPtr, this);
  }


  public void setTiming(float maxTimestep, long maxIter, int method) {
    JPhysXAdapterJNI.NxScene_setTiming__SWIG_0(swigCPtr, this, maxTimestep, maxIter, method);
  }


  public void setTiming(float maxTimestep, long maxIter) {
    JPhysXAdapterJNI.NxScene_setTiming__SWIG_1(swigCPtr, this, maxTimestep, maxIter);
  }


  public void setTiming(float maxTimestep) {
    JPhysXAdapterJNI.NxScene_setTiming__SWIG_2(swigCPtr, this, maxTimestep);
  }


  public void setTiming() {
    JPhysXAdapterJNI.NxScene_setTiming__SWIG_3(swigCPtr, this);
  }


  public void getTiming(SWIGTYPE_p_float maxTimestep, SWIGTYPE_p_unsigned_int maxIter, SWIGTYPE_p_NxTimeStepMethod method, SWIGTYPE_p_unsigned_int numSubSteps) {
    JPhysXAdapterJNI.NxScene_getTiming__SWIG_0(swigCPtr, this, SWIGTYPE_p_float.getCPtr(maxTimestep), SWIGTYPE_p_unsigned_int.getCPtr(maxIter), SWIGTYPE_p_NxTimeStepMethod.getCPtr(method), SWIGTYPE_p_unsigned_int.getCPtr(numSubSteps));
  }


  public void getTiming(SWIGTYPE_p_float maxTimestep, SWIGTYPE_p_unsigned_int maxIter, SWIGTYPE_p_NxTimeStepMethod method) {
    JPhysXAdapterJNI.NxScene_getTiming__SWIG_1(swigCPtr, this, SWIGTYPE_p_float.getCPtr(maxTimestep), SWIGTYPE_p_unsigned_int.getCPtr(maxIter), SWIGTYPE_p_NxTimeStepMethod.getCPtr(method));
  }


  public NxDebugRenderable getDebugRenderable() {
    long cPtr = JPhysXAdapterJNI.NxScene_getDebugRenderable(swigCPtr, this);
    return (cPtr == 0) ? null : new NxDebugRenderable(cPtr, false);
  }
  public NxDebugRenderable getDebugRenderable_inplace(NxDebugRenderable result) {
    long cPtr = JPhysXAdapterJNI.NxScene_getDebugRenderable(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public NxPhysicsSDK getPhysicsSDK() {
    return new NxPhysicsSDK(JPhysXAdapterJNI.NxScene_getPhysicsSDK(swigCPtr, this), false);
  }
  public NxPhysicsSDK getPhysicsSDK_inplace(NxPhysicsSDK result) {
    long cPtr = JPhysXAdapterJNI.NxScene_getPhysicsSDK(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void getStats(NxSceneStats stats) {
    JPhysXAdapterJNI.NxScene_getStats(swigCPtr, this, NxSceneStats.getCPtr(stats), stats);
  }


  public NxSceneStats2 getStats2() {
    long cPtr = JPhysXAdapterJNI.NxScene_getStats2(swigCPtr, this);
    return (cPtr == 0) ? null : new NxSceneStats2(cPtr, false);
  }
  public NxSceneStats2 getStats2_inplace(NxSceneStats2 result) {
    long cPtr = JPhysXAdapterJNI.NxScene_getStats2(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void getLimits(NxSceneLimits limits) {
    JPhysXAdapterJNI.NxScene_getLimits(swigCPtr, this, NxSceneLimits.getCPtr(limits), limits);
  }


  public void setMaxCPUForLoadBalancing(float cpuFraction) {
    JPhysXAdapterJNI.NxScene_setMaxCPUForLoadBalancing(swigCPtr, this, cpuFraction);
  }


  public float getMaxCPUForLoadBalancing() {
    return JPhysXAdapterJNI.NxScene_getMaxCPUForLoadBalancing(swigCPtr, this);
  }


  public void setUserNotify(NxUserNotify callback) {
    JPhysXAdapterJNI.NxScene_setUserNotify(swigCPtr, this, NxUserNotify.getCPtr(callback), callback);
  }


  public NxUserNotify getUserNotify() {
    long cPtr = JPhysXAdapterJNI.NxScene_getUserNotify(swigCPtr, this);
    return (cPtr == 0) ? null : new NxUserNotify(cPtr, false);
  }
  public NxUserNotify getUserNotify_inplace(NxUserNotify result) {
    long cPtr = JPhysXAdapterJNI.NxScene_getUserNotify(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setFluidUserNotify(SWIGTYPE_p_NxFluidUserNotify callback) {
    JPhysXAdapterJNI.NxScene_setFluidUserNotify(swigCPtr, this, SWIGTYPE_p_NxFluidUserNotify.getCPtr(callback));
  }


  public SWIGTYPE_p_NxFluidUserNotify getFluidUserNotify() {
    long cPtr = JPhysXAdapterJNI.NxScene_getFluidUserNotify(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_NxFluidUserNotify(cPtr, false);
  }


  public void setClothUserNotify(SWIGTYPE_p_NxClothUserNotify callback) {
    JPhysXAdapterJNI.NxScene_setClothUserNotify(swigCPtr, this, SWIGTYPE_p_NxClothUserNotify.getCPtr(callback));
  }


  public SWIGTYPE_p_NxClothUserNotify getClothUserNotify() {
    long cPtr = JPhysXAdapterJNI.NxScene_getClothUserNotify(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_NxClothUserNotify(cPtr, false);
  }


  public void setSoftBodyUserNotify(SWIGTYPE_p_NxSoftBodyUserNotify callback) {
    JPhysXAdapterJNI.NxScene_setSoftBodyUserNotify(swigCPtr, this, SWIGTYPE_p_NxSoftBodyUserNotify.getCPtr(callback));
  }


  public SWIGTYPE_p_NxSoftBodyUserNotify getSoftBodyUserNotify() {
    long cPtr = JPhysXAdapterJNI.NxScene_getSoftBodyUserNotify(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_NxSoftBodyUserNotify(cPtr, false);
  }


  public void setUserContactModify(NxUserContactModify callback) {
    JPhysXAdapterJNI.NxScene_setUserContactModify(swigCPtr, this, NxUserContactModify.getCPtr(callback), callback);
  }


  public NxUserContactModify getUserContactModify() {
    long cPtr = JPhysXAdapterJNI.NxScene_getUserContactModify(swigCPtr, this);
    return (cPtr == 0) ? null : new NxUserContactModify(cPtr, false);
  }
  public NxUserContactModify getUserContactModify_inplace(NxUserContactModify result) {
    long cPtr = JPhysXAdapterJNI.NxScene_getUserContactModify(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setUserTriggerReport(NxUserTriggerReport callback) {
    JPhysXAdapterJNI.NxScene_setUserTriggerReport(swigCPtr, this, NxUserTriggerReport.getCPtr(callback), callback);
  }


  public NxUserTriggerReport getUserTriggerReport() {
    long cPtr = JPhysXAdapterJNI.NxScene_getUserTriggerReport(swigCPtr, this);
    return (cPtr == 0) ? null : new NxUserTriggerReport(cPtr, false);
  }
  public NxUserTriggerReport getUserTriggerReport_inplace(NxUserTriggerReport result) {
    long cPtr = JPhysXAdapterJNI.NxScene_getUserTriggerReport(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setUserContactReport(NxUserContactReport callback) {
    JPhysXAdapterJNI.NxScene_setUserContactReport(swigCPtr, this, NxUserContactReport.getCPtr(callback), callback);
  }


  public NxUserContactReport getUserContactReport() {
    long cPtr = JPhysXAdapterJNI.NxScene_getUserContactReport(swigCPtr, this);
    return (cPtr == 0) ? null : new NxUserContactReport(cPtr, false);
  }
  public NxUserContactReport getUserContactReport_inplace(NxUserContactReport result) {
    long cPtr = JPhysXAdapterJNI.NxScene_getUserContactReport(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void setUserActorPairFiltering(NxUserActorPairFiltering callback) {
    JPhysXAdapterJNI.NxScene_setUserActorPairFiltering(swigCPtr, this, NxUserActorPairFiltering.getCPtr(callback), callback);
  }


  public NxUserActorPairFiltering getUserActorPairFiltering() {
    long cPtr = JPhysXAdapterJNI.NxScene_getUserActorPairFiltering(swigCPtr, this);
    return (cPtr == 0) ? null : new NxUserActorPairFiltering(cPtr, false);
  }
  public NxUserActorPairFiltering getUserActorPairFiltering_inplace(NxUserActorPairFiltering result) {
    long cPtr = JPhysXAdapterJNI.NxScene_getUserActorPairFiltering(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public boolean raycastAnyBounds(NxRay worldRay, int shapesType, long groups, float maxDist, NxGroupsMask groupsMask) {
    return JPhysXAdapterJNI.NxScene_raycastAnyBounds__SWIG_0(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, shapesType, groups, maxDist, NxGroupsMask.getCPtr(groupsMask), groupsMask);
  }


  public boolean raycastAnyBounds(NxRay worldRay, int shapesType, long groups, float maxDist) {
    return JPhysXAdapterJNI.NxScene_raycastAnyBounds__SWIG_1(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, shapesType, groups, maxDist);
  }


  public boolean raycastAnyBounds(NxRay worldRay, int shapesType, long groups) {
    return JPhysXAdapterJNI.NxScene_raycastAnyBounds__SWIG_2(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, shapesType, groups);
  }


  public boolean raycastAnyBounds(NxRay worldRay, int shapesType) {
    return JPhysXAdapterJNI.NxScene_raycastAnyBounds__SWIG_3(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, shapesType);
  }


  public boolean raycastAnyShape(NxRay worldRay, int shapesType, long groups, float maxDist, NxGroupsMask groupsMask, SWIGTYPE_p_p_NxShape cache) {
    return JPhysXAdapterJNI.NxScene_raycastAnyShape__SWIG_0(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, shapesType, groups, maxDist, NxGroupsMask.getCPtr(groupsMask), groupsMask, SWIGTYPE_p_p_NxShape.getCPtr(cache));
  }


  public boolean raycastAnyShape(NxRay worldRay, int shapesType, long groups, float maxDist, NxGroupsMask groupsMask) {
    return JPhysXAdapterJNI.NxScene_raycastAnyShape__SWIG_1(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, shapesType, groups, maxDist, NxGroupsMask.getCPtr(groupsMask), groupsMask);
  }


  public boolean raycastAnyShape(NxRay worldRay, int shapesType, long groups, float maxDist) {
    return JPhysXAdapterJNI.NxScene_raycastAnyShape__SWIG_2(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, shapesType, groups, maxDist);
  }


  public boolean raycastAnyShape(NxRay worldRay, int shapesType, long groups) {
    return JPhysXAdapterJNI.NxScene_raycastAnyShape__SWIG_3(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, shapesType, groups);
  }


  public boolean raycastAnyShape(NxRay worldRay, int shapesType) {
    return JPhysXAdapterJNI.NxScene_raycastAnyShape__SWIG_4(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, shapesType);
  }


  public long raycastAllBounds(NxRay worldRay, NxUserRaycastReport report, int shapesType, long groups, float maxDist, long hintFlags, NxGroupsMask groupsMask) {
    return JPhysXAdapterJNI.NxScene_raycastAllBounds__SWIG_0(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, NxUserRaycastReport.getCPtr(report), report, shapesType, groups, maxDist, hintFlags, NxGroupsMask.getCPtr(groupsMask), groupsMask);
  }


  public long raycastAllBounds(NxRay worldRay, NxUserRaycastReport report, int shapesType, long groups, float maxDist, long hintFlags) {
    return JPhysXAdapterJNI.NxScene_raycastAllBounds__SWIG_1(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, NxUserRaycastReport.getCPtr(report), report, shapesType, groups, maxDist, hintFlags);
  }


  public long raycastAllBounds(NxRay worldRay, NxUserRaycastReport report, int shapesType, long groups, float maxDist) {
    return JPhysXAdapterJNI.NxScene_raycastAllBounds__SWIG_2(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, NxUserRaycastReport.getCPtr(report), report, shapesType, groups, maxDist);
  }


  public long raycastAllBounds(NxRay worldRay, NxUserRaycastReport report, int shapesType, long groups) {
    return JPhysXAdapterJNI.NxScene_raycastAllBounds__SWIG_3(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, NxUserRaycastReport.getCPtr(report), report, shapesType, groups);
  }


  public long raycastAllBounds(NxRay worldRay, NxUserRaycastReport report, int shapesType) {
    return JPhysXAdapterJNI.NxScene_raycastAllBounds__SWIG_4(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, NxUserRaycastReport.getCPtr(report), report, shapesType);
  }


  public long raycastAllShapes(NxRay worldRay, NxUserRaycastReport report, int shapesType, long groups, float maxDist, long hintFlags, NxGroupsMask groupsMask) {
    return JPhysXAdapterJNI.NxScene_raycastAllShapes__SWIG_0(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, NxUserRaycastReport.getCPtr(report), report, shapesType, groups, maxDist, hintFlags, NxGroupsMask.getCPtr(groupsMask), groupsMask);
  }


  public long raycastAllShapes(NxRay worldRay, NxUserRaycastReport report, int shapesType, long groups, float maxDist, long hintFlags) {
    return JPhysXAdapterJNI.NxScene_raycastAllShapes__SWIG_1(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, NxUserRaycastReport.getCPtr(report), report, shapesType, groups, maxDist, hintFlags);
  }


  public long raycastAllShapes(NxRay worldRay, NxUserRaycastReport report, int shapesType, long groups, float maxDist) {
    return JPhysXAdapterJNI.NxScene_raycastAllShapes__SWIG_2(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, NxUserRaycastReport.getCPtr(report), report, shapesType, groups, maxDist);
  }


  public long raycastAllShapes(NxRay worldRay, NxUserRaycastReport report, int shapesType, long groups) {
    return JPhysXAdapterJNI.NxScene_raycastAllShapes__SWIG_3(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, NxUserRaycastReport.getCPtr(report), report, shapesType, groups);
  }


  public long raycastAllShapes(NxRay worldRay, NxUserRaycastReport report, int shapesType) {
    return JPhysXAdapterJNI.NxScene_raycastAllShapes__SWIG_4(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, NxUserRaycastReport.getCPtr(report), report, shapesType);
  }


  public NxShape raycastClosestBounds(NxRay worldRay, int shapeType, NxRaycastHit hit, long groups, float maxDist, long hintFlags, NxGroupsMask groupsMask) {
    long cPtr = JPhysXAdapterJNI.NxScene_raycastClosestBounds__SWIG_0(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, shapeType, NxRaycastHit.getCPtr(hit), hit, groups, maxDist, hintFlags, NxGroupsMask.getCPtr(groupsMask), groupsMask);
    return (cPtr == 0) ? null : new NxShape(cPtr, false);
  }
  public NxShape raycastClosestBounds_inplace(NxShape result, NxRay worldRay, int shapeType, NxRaycastHit hit, long groups, float maxDist, long hintFlags, NxGroupsMask groupsMask) {
    long cPtr = JPhysXAdapterJNI.NxScene_raycastClosestBounds__SWIG_0(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, shapeType, NxRaycastHit.getCPtr(hit), hit, groups, maxDist, hintFlags, NxGroupsMask.getCPtr(groupsMask), groupsMask);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public NxShape raycastClosestBounds(NxRay worldRay, int shapeType, NxRaycastHit hit, long groups, float maxDist, long hintFlags) {
    long cPtr = JPhysXAdapterJNI.NxScene_raycastClosestBounds__SWIG_1(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, shapeType, NxRaycastHit.getCPtr(hit), hit, groups, maxDist, hintFlags);
    return (cPtr == 0) ? null : new NxShape(cPtr, false);
  }
  public NxShape raycastClosestBounds_inplace(NxShape result, NxRay worldRay, int shapeType, NxRaycastHit hit, long groups, float maxDist, long hintFlags) {
    long cPtr = JPhysXAdapterJNI.NxScene_raycastClosestBounds__SWIG_1(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, shapeType, NxRaycastHit.getCPtr(hit), hit, groups, maxDist, hintFlags);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public NxShape raycastClosestBounds(NxRay worldRay, int shapeType, NxRaycastHit hit, long groups, float maxDist) {
    long cPtr = JPhysXAdapterJNI.NxScene_raycastClosestBounds__SWIG_2(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, shapeType, NxRaycastHit.getCPtr(hit), hit, groups, maxDist);
    return (cPtr == 0) ? null : new NxShape(cPtr, false);
  }
  public NxShape raycastClosestBounds_inplace(NxShape result, NxRay worldRay, int shapeType, NxRaycastHit hit, long groups, float maxDist) {
    long cPtr = JPhysXAdapterJNI.NxScene_raycastClosestBounds__SWIG_2(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, shapeType, NxRaycastHit.getCPtr(hit), hit, groups, maxDist);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public NxShape raycastClosestBounds(NxRay worldRay, int shapeType, NxRaycastHit hit, long groups) {
    long cPtr = JPhysXAdapterJNI.NxScene_raycastClosestBounds__SWIG_3(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, shapeType, NxRaycastHit.getCPtr(hit), hit, groups);
    return (cPtr == 0) ? null : new NxShape(cPtr, false);
  }
  public NxShape raycastClosestBounds_inplace(NxShape result, NxRay worldRay, int shapeType, NxRaycastHit hit, long groups) {
    long cPtr = JPhysXAdapterJNI.NxScene_raycastClosestBounds__SWIG_3(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, shapeType, NxRaycastHit.getCPtr(hit), hit, groups);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public NxShape raycastClosestBounds(NxRay worldRay, int shapeType, NxRaycastHit hit) {
    long cPtr = JPhysXAdapterJNI.NxScene_raycastClosestBounds__SWIG_4(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, shapeType, NxRaycastHit.getCPtr(hit), hit);
    return (cPtr == 0) ? null : new NxShape(cPtr, false);
  }
  public NxShape raycastClosestBounds_inplace(NxShape result, NxRay worldRay, int shapeType, NxRaycastHit hit) {
    long cPtr = JPhysXAdapterJNI.NxScene_raycastClosestBounds__SWIG_4(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, shapeType, NxRaycastHit.getCPtr(hit), hit);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public NxShape raycastClosestShape(NxRay worldRay, int shapeType, NxRaycastHit hit, long groups, float maxDist, long hintFlags, NxGroupsMask groupsMask, SWIGTYPE_p_p_NxShape cache) {
    long cPtr = JPhysXAdapterJNI.NxScene_raycastClosestShape__SWIG_0(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, shapeType, NxRaycastHit.getCPtr(hit), hit, groups, maxDist, hintFlags, NxGroupsMask.getCPtr(groupsMask), groupsMask, SWIGTYPE_p_p_NxShape.getCPtr(cache));
    return (cPtr == 0) ? null : new NxShape(cPtr, false);
  }
  public NxShape raycastClosestShape_inplace(NxShape result, NxRay worldRay, int shapeType, NxRaycastHit hit, long groups, float maxDist, long hintFlags, NxGroupsMask groupsMask, SWIGTYPE_p_p_NxShape cache) {
    long cPtr = JPhysXAdapterJNI.NxScene_raycastClosestShape__SWIG_0(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, shapeType, NxRaycastHit.getCPtr(hit), hit, groups, maxDist, hintFlags, NxGroupsMask.getCPtr(groupsMask), groupsMask, SWIGTYPE_p_p_NxShape.getCPtr(cache));
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public NxShape raycastClosestShape(NxRay worldRay, int shapeType, NxRaycastHit hit, long groups, float maxDist, long hintFlags, NxGroupsMask groupsMask) {
    long cPtr = JPhysXAdapterJNI.NxScene_raycastClosestShape__SWIG_1(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, shapeType, NxRaycastHit.getCPtr(hit), hit, groups, maxDist, hintFlags, NxGroupsMask.getCPtr(groupsMask), groupsMask);
    return (cPtr == 0) ? null : new NxShape(cPtr, false);
  }
  public NxShape raycastClosestShape_inplace(NxShape result, NxRay worldRay, int shapeType, NxRaycastHit hit, long groups, float maxDist, long hintFlags, NxGroupsMask groupsMask) {
    long cPtr = JPhysXAdapterJNI.NxScene_raycastClosestShape__SWIG_1(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, shapeType, NxRaycastHit.getCPtr(hit), hit, groups, maxDist, hintFlags, NxGroupsMask.getCPtr(groupsMask), groupsMask);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public NxShape raycastClosestShape(NxRay worldRay, int shapeType, NxRaycastHit hit, long groups, float maxDist, long hintFlags) {
    long cPtr = JPhysXAdapterJNI.NxScene_raycastClosestShape__SWIG_2(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, shapeType, NxRaycastHit.getCPtr(hit), hit, groups, maxDist, hintFlags);
    return (cPtr == 0) ? null : new NxShape(cPtr, false);
  }
  public NxShape raycastClosestShape_inplace(NxShape result, NxRay worldRay, int shapeType, NxRaycastHit hit, long groups, float maxDist, long hintFlags) {
    long cPtr = JPhysXAdapterJNI.NxScene_raycastClosestShape__SWIG_2(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, shapeType, NxRaycastHit.getCPtr(hit), hit, groups, maxDist, hintFlags);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public NxShape raycastClosestShape(NxRay worldRay, int shapeType, NxRaycastHit hit, long groups, float maxDist) {
    long cPtr = JPhysXAdapterJNI.NxScene_raycastClosestShape__SWIG_3(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, shapeType, NxRaycastHit.getCPtr(hit), hit, groups, maxDist);
    return (cPtr == 0) ? null : new NxShape(cPtr, false);
  }
  public NxShape raycastClosestShape_inplace(NxShape result, NxRay worldRay, int shapeType, NxRaycastHit hit, long groups, float maxDist) {
    long cPtr = JPhysXAdapterJNI.NxScene_raycastClosestShape__SWIG_3(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, shapeType, NxRaycastHit.getCPtr(hit), hit, groups, maxDist);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public NxShape raycastClosestShape(NxRay worldRay, int shapeType, NxRaycastHit hit, long groups) {
    long cPtr = JPhysXAdapterJNI.NxScene_raycastClosestShape__SWIG_4(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, shapeType, NxRaycastHit.getCPtr(hit), hit, groups);
    return (cPtr == 0) ? null : new NxShape(cPtr, false);
  }
  public NxShape raycastClosestShape_inplace(NxShape result, NxRay worldRay, int shapeType, NxRaycastHit hit, long groups) {
    long cPtr = JPhysXAdapterJNI.NxScene_raycastClosestShape__SWIG_4(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, shapeType, NxRaycastHit.getCPtr(hit), hit, groups);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public NxShape raycastClosestShape(NxRay worldRay, int shapeType, NxRaycastHit hit) {
    long cPtr = JPhysXAdapterJNI.NxScene_raycastClosestShape__SWIG_5(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, shapeType, NxRaycastHit.getCPtr(hit), hit);
    return (cPtr == 0) ? null : new NxShape(cPtr, false);
  }
  public NxShape raycastClosestShape_inplace(NxShape result, NxRay worldRay, int shapeType, NxRaycastHit hit) {
    long cPtr = JPhysXAdapterJNI.NxScene_raycastClosestShape__SWIG_5(swigCPtr, this, NxRay.getCPtr(worldRay), worldRay, shapeType, NxRaycastHit.getCPtr(hit), hit);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public long overlapSphereShapes(NxSphere worldSphere, int shapeType, long nbShapes, SWIGTYPE_p_p_NxShape shapes, SWIGTYPE_p_NxUserEntityReportTNxShape_p_t callback, long activeGroups, NxGroupsMask groupsMask, boolean accurateCollision) {
    return JPhysXAdapterJNI.NxScene_overlapSphereShapes__SWIG_0(swigCPtr, this, NxSphere.getCPtr(worldSphere), worldSphere, shapeType, nbShapes, SWIGTYPE_p_p_NxShape.getCPtr(shapes), SWIGTYPE_p_NxUserEntityReportTNxShape_p_t.getCPtr(callback), activeGroups, NxGroupsMask.getCPtr(groupsMask), groupsMask, accurateCollision);
  }


  public long overlapSphereShapes(NxSphere worldSphere, int shapeType, long nbShapes, SWIGTYPE_p_p_NxShape shapes, SWIGTYPE_p_NxUserEntityReportTNxShape_p_t callback, long activeGroups, NxGroupsMask groupsMask) {
    return JPhysXAdapterJNI.NxScene_overlapSphereShapes__SWIG_1(swigCPtr, this, NxSphere.getCPtr(worldSphere), worldSphere, shapeType, nbShapes, SWIGTYPE_p_p_NxShape.getCPtr(shapes), SWIGTYPE_p_NxUserEntityReportTNxShape_p_t.getCPtr(callback), activeGroups, NxGroupsMask.getCPtr(groupsMask), groupsMask);
  }


  public long overlapSphereShapes(NxSphere worldSphere, int shapeType, long nbShapes, SWIGTYPE_p_p_NxShape shapes, SWIGTYPE_p_NxUserEntityReportTNxShape_p_t callback, long activeGroups) {
    return JPhysXAdapterJNI.NxScene_overlapSphereShapes__SWIG_2(swigCPtr, this, NxSphere.getCPtr(worldSphere), worldSphere, shapeType, nbShapes, SWIGTYPE_p_p_NxShape.getCPtr(shapes), SWIGTYPE_p_NxUserEntityReportTNxShape_p_t.getCPtr(callback), activeGroups);
  }


  public long overlapSphereShapes(NxSphere worldSphere, int shapeType, long nbShapes, SWIGTYPE_p_p_NxShape shapes, SWIGTYPE_p_NxUserEntityReportTNxShape_p_t callback) {
    return JPhysXAdapterJNI.NxScene_overlapSphereShapes__SWIG_3(swigCPtr, this, NxSphere.getCPtr(worldSphere), worldSphere, shapeType, nbShapes, SWIGTYPE_p_p_NxShape.getCPtr(shapes), SWIGTYPE_p_NxUserEntityReportTNxShape_p_t.getCPtr(callback));
  }


  public long overlapAABBShapes(NxBounds3 worldBounds, int shapeType, long nbShapes, SWIGTYPE_p_p_NxShape shapes, SWIGTYPE_p_NxUserEntityReportTNxShape_p_t callback, long activeGroups, NxGroupsMask groupsMask, boolean accurateCollision) {
    return JPhysXAdapterJNI.NxScene_overlapAABBShapes__SWIG_0(swigCPtr, this, NxBounds3.getCPtr(worldBounds), worldBounds, shapeType, nbShapes, SWIGTYPE_p_p_NxShape.getCPtr(shapes), SWIGTYPE_p_NxUserEntityReportTNxShape_p_t.getCPtr(callback), activeGroups, NxGroupsMask.getCPtr(groupsMask), groupsMask, accurateCollision);
  }


  public long overlapAABBShapes(NxBounds3 worldBounds, int shapeType, long nbShapes, SWIGTYPE_p_p_NxShape shapes, SWIGTYPE_p_NxUserEntityReportTNxShape_p_t callback, long activeGroups, NxGroupsMask groupsMask) {
    return JPhysXAdapterJNI.NxScene_overlapAABBShapes__SWIG_1(swigCPtr, this, NxBounds3.getCPtr(worldBounds), worldBounds, shapeType, nbShapes, SWIGTYPE_p_p_NxShape.getCPtr(shapes), SWIGTYPE_p_NxUserEntityReportTNxShape_p_t.getCPtr(callback), activeGroups, NxGroupsMask.getCPtr(groupsMask), groupsMask);
  }


  public long overlapAABBShapes(NxBounds3 worldBounds, int shapeType, long nbShapes, SWIGTYPE_p_p_NxShape shapes, SWIGTYPE_p_NxUserEntityReportTNxShape_p_t callback, long activeGroups) {
    return JPhysXAdapterJNI.NxScene_overlapAABBShapes__SWIG_2(swigCPtr, this, NxBounds3.getCPtr(worldBounds), worldBounds, shapeType, nbShapes, SWIGTYPE_p_p_NxShape.getCPtr(shapes), SWIGTYPE_p_NxUserEntityReportTNxShape_p_t.getCPtr(callback), activeGroups);
  }


  public long overlapAABBShapes(NxBounds3 worldBounds, int shapeType, long nbShapes, SWIGTYPE_p_p_NxShape shapes, SWIGTYPE_p_NxUserEntityReportTNxShape_p_t callback) {
    return JPhysXAdapterJNI.NxScene_overlapAABBShapes__SWIG_3(swigCPtr, this, NxBounds3.getCPtr(worldBounds), worldBounds, shapeType, nbShapes, SWIGTYPE_p_p_NxShape.getCPtr(shapes), SWIGTYPE_p_NxUserEntityReportTNxShape_p_t.getCPtr(callback));
  }


  public long overlapOBBShapes(NxBox worldBox, int shapeType, long nbShapes, SWIGTYPE_p_p_NxShape shapes, SWIGTYPE_p_NxUserEntityReportTNxShape_p_t callback, long activeGroups, NxGroupsMask groupsMask, boolean accurateCollision) {
    return JPhysXAdapterJNI.NxScene_overlapOBBShapes__SWIG_0(swigCPtr, this, NxBox.getCPtr(worldBox), worldBox, shapeType, nbShapes, SWIGTYPE_p_p_NxShape.getCPtr(shapes), SWIGTYPE_p_NxUserEntityReportTNxShape_p_t.getCPtr(callback), activeGroups, NxGroupsMask.getCPtr(groupsMask), groupsMask, accurateCollision);
  }


  public long overlapOBBShapes(NxBox worldBox, int shapeType, long nbShapes, SWIGTYPE_p_p_NxShape shapes, SWIGTYPE_p_NxUserEntityReportTNxShape_p_t callback, long activeGroups, NxGroupsMask groupsMask) {
    return JPhysXAdapterJNI.NxScene_overlapOBBShapes__SWIG_1(swigCPtr, this, NxBox.getCPtr(worldBox), worldBox, shapeType, nbShapes, SWIGTYPE_p_p_NxShape.getCPtr(shapes), SWIGTYPE_p_NxUserEntityReportTNxShape_p_t.getCPtr(callback), activeGroups, NxGroupsMask.getCPtr(groupsMask), groupsMask);
  }


  public long overlapOBBShapes(NxBox worldBox, int shapeType, long nbShapes, SWIGTYPE_p_p_NxShape shapes, SWIGTYPE_p_NxUserEntityReportTNxShape_p_t callback, long activeGroups) {
    return JPhysXAdapterJNI.NxScene_overlapOBBShapes__SWIG_2(swigCPtr, this, NxBox.getCPtr(worldBox), worldBox, shapeType, nbShapes, SWIGTYPE_p_p_NxShape.getCPtr(shapes), SWIGTYPE_p_NxUserEntityReportTNxShape_p_t.getCPtr(callback), activeGroups);
  }


  public long overlapOBBShapes(NxBox worldBox, int shapeType, long nbShapes, SWIGTYPE_p_p_NxShape shapes, SWIGTYPE_p_NxUserEntityReportTNxShape_p_t callback) {
    return JPhysXAdapterJNI.NxScene_overlapOBBShapes__SWIG_3(swigCPtr, this, NxBox.getCPtr(worldBox), worldBox, shapeType, nbShapes, SWIGTYPE_p_p_NxShape.getCPtr(shapes), SWIGTYPE_p_NxUserEntityReportTNxShape_p_t.getCPtr(callback));
  }


  public long overlapCapsuleShapes(NxCapsule worldCapsule, int shapeType, long nbShapes, SWIGTYPE_p_p_NxShape shapes, SWIGTYPE_p_NxUserEntityReportTNxShape_p_t callback, long activeGroups, NxGroupsMask groupsMask, boolean accurateCollision) {
    return JPhysXAdapterJNI.NxScene_overlapCapsuleShapes__SWIG_0(swigCPtr, this, NxCapsule.getCPtr(worldCapsule), worldCapsule, shapeType, nbShapes, SWIGTYPE_p_p_NxShape.getCPtr(shapes), SWIGTYPE_p_NxUserEntityReportTNxShape_p_t.getCPtr(callback), activeGroups, NxGroupsMask.getCPtr(groupsMask), groupsMask, accurateCollision);
  }


  public long overlapCapsuleShapes(NxCapsule worldCapsule, int shapeType, long nbShapes, SWIGTYPE_p_p_NxShape shapes, SWIGTYPE_p_NxUserEntityReportTNxShape_p_t callback, long activeGroups, NxGroupsMask groupsMask) {
    return JPhysXAdapterJNI.NxScene_overlapCapsuleShapes__SWIG_1(swigCPtr, this, NxCapsule.getCPtr(worldCapsule), worldCapsule, shapeType, nbShapes, SWIGTYPE_p_p_NxShape.getCPtr(shapes), SWIGTYPE_p_NxUserEntityReportTNxShape_p_t.getCPtr(callback), activeGroups, NxGroupsMask.getCPtr(groupsMask), groupsMask);
  }


  public long overlapCapsuleShapes(NxCapsule worldCapsule, int shapeType, long nbShapes, SWIGTYPE_p_p_NxShape shapes, SWIGTYPE_p_NxUserEntityReportTNxShape_p_t callback, long activeGroups) {
    return JPhysXAdapterJNI.NxScene_overlapCapsuleShapes__SWIG_2(swigCPtr, this, NxCapsule.getCPtr(worldCapsule), worldCapsule, shapeType, nbShapes, SWIGTYPE_p_p_NxShape.getCPtr(shapes), SWIGTYPE_p_NxUserEntityReportTNxShape_p_t.getCPtr(callback), activeGroups);
  }


  public long overlapCapsuleShapes(NxCapsule worldCapsule, int shapeType, long nbShapes, SWIGTYPE_p_p_NxShape shapes, SWIGTYPE_p_NxUserEntityReportTNxShape_p_t callback) {
    return JPhysXAdapterJNI.NxScene_overlapCapsuleShapes__SWIG_3(swigCPtr, this, NxCapsule.getCPtr(worldCapsule), worldCapsule, shapeType, nbShapes, SWIGTYPE_p_p_NxShape.getCPtr(shapes), SWIGTYPE_p_NxUserEntityReportTNxShape_p_t.getCPtr(callback));
  }


  public NxSweepCache createSweepCache() {
    long cPtr = JPhysXAdapterJNI.NxScene_createSweepCache(swigCPtr, this);
    return (cPtr == 0) ? null : new NxSweepCache(cPtr, false);
  }
  public NxSweepCache createSweepCache_inplace(NxSweepCache result) {
    long cPtr = JPhysXAdapterJNI.NxScene_createSweepCache(swigCPtr, this);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void releaseSweepCache(NxSweepCache cache) {
    JPhysXAdapterJNI.NxScene_releaseSweepCache(swigCPtr, this, NxSweepCache.getCPtr(cache), cache);
  }


  public long linearOBBSweep(NxBox worldBox, NxVec3 motion, long flags, SWIGTYPE_p_void userData, long nbShapes, NxSweepQueryHit shapes, SWIGTYPE_p_NxUserEntityReportTNxSweepQueryHit_t callback, long activeGroups, NxGroupsMask groupsMask) {
    return JPhysXAdapterJNI.NxScene_linearOBBSweep__SWIG_0(swigCPtr, this, NxBox.getCPtr(worldBox), worldBox, NxVec3.getCPtr(motion), motion, flags, SWIGTYPE_p_void.getCPtr(userData), nbShapes, NxSweepQueryHit.getCPtr(shapes), shapes, SWIGTYPE_p_NxUserEntityReportTNxSweepQueryHit_t.getCPtr(callback), activeGroups, NxGroupsMask.getCPtr(groupsMask), groupsMask);
  }


  public long linearOBBSweep(NxBox worldBox, NxVec3 motion, long flags, SWIGTYPE_p_void userData, long nbShapes, NxSweepQueryHit shapes, SWIGTYPE_p_NxUserEntityReportTNxSweepQueryHit_t callback, long activeGroups) {
    return JPhysXAdapterJNI.NxScene_linearOBBSweep__SWIG_1(swigCPtr, this, NxBox.getCPtr(worldBox), worldBox, NxVec3.getCPtr(motion), motion, flags, SWIGTYPE_p_void.getCPtr(userData), nbShapes, NxSweepQueryHit.getCPtr(shapes), shapes, SWIGTYPE_p_NxUserEntityReportTNxSweepQueryHit_t.getCPtr(callback), activeGroups);
  }


  public long linearOBBSweep(NxBox worldBox, NxVec3 motion, long flags, SWIGTYPE_p_void userData, long nbShapes, NxSweepQueryHit shapes, SWIGTYPE_p_NxUserEntityReportTNxSweepQueryHit_t callback) {
    return JPhysXAdapterJNI.NxScene_linearOBBSweep__SWIG_2(swigCPtr, this, NxBox.getCPtr(worldBox), worldBox, NxVec3.getCPtr(motion), motion, flags, SWIGTYPE_p_void.getCPtr(userData), nbShapes, NxSweepQueryHit.getCPtr(shapes), shapes, SWIGTYPE_p_NxUserEntityReportTNxSweepQueryHit_t.getCPtr(callback));
  }


  public long linearCapsuleSweep(NxCapsule worldCapsule, NxVec3 motion, long flags, SWIGTYPE_p_void userData, long nbShapes, NxSweepQueryHit shapes, SWIGTYPE_p_NxUserEntityReportTNxSweepQueryHit_t callback, long activeGroups, NxGroupsMask groupsMask) {
    return JPhysXAdapterJNI.NxScene_linearCapsuleSweep__SWIG_0(swigCPtr, this, NxCapsule.getCPtr(worldCapsule), worldCapsule, NxVec3.getCPtr(motion), motion, flags, SWIGTYPE_p_void.getCPtr(userData), nbShapes, NxSweepQueryHit.getCPtr(shapes), shapes, SWIGTYPE_p_NxUserEntityReportTNxSweepQueryHit_t.getCPtr(callback), activeGroups, NxGroupsMask.getCPtr(groupsMask), groupsMask);
  }


  public long linearCapsuleSweep(NxCapsule worldCapsule, NxVec3 motion, long flags, SWIGTYPE_p_void userData, long nbShapes, NxSweepQueryHit shapes, SWIGTYPE_p_NxUserEntityReportTNxSweepQueryHit_t callback, long activeGroups) {
    return JPhysXAdapterJNI.NxScene_linearCapsuleSweep__SWIG_1(swigCPtr, this, NxCapsule.getCPtr(worldCapsule), worldCapsule, NxVec3.getCPtr(motion), motion, flags, SWIGTYPE_p_void.getCPtr(userData), nbShapes, NxSweepQueryHit.getCPtr(shapes), shapes, SWIGTYPE_p_NxUserEntityReportTNxSweepQueryHit_t.getCPtr(callback), activeGroups);
  }


  public long linearCapsuleSweep(NxCapsule worldCapsule, NxVec3 motion, long flags, SWIGTYPE_p_void userData, long nbShapes, NxSweepQueryHit shapes, SWIGTYPE_p_NxUserEntityReportTNxSweepQueryHit_t callback) {
    return JPhysXAdapterJNI.NxScene_linearCapsuleSweep__SWIG_2(swigCPtr, this, NxCapsule.getCPtr(worldCapsule), worldCapsule, NxVec3.getCPtr(motion), motion, flags, SWIGTYPE_p_void.getCPtr(userData), nbShapes, NxSweepQueryHit.getCPtr(shapes), shapes, SWIGTYPE_p_NxUserEntityReportTNxSweepQueryHit_t.getCPtr(callback));
  }


  public long cullShapes(long nbPlanes, NxPlane worldPlanes, int shapeType, long nbShapes, SWIGTYPE_p_p_NxShape shapes, SWIGTYPE_p_NxUserEntityReportTNxShape_p_t callback, long activeGroups, NxGroupsMask groupsMask) {
    return JPhysXAdapterJNI.NxScene_cullShapes__SWIG_0(swigCPtr, this, nbPlanes, NxPlane.getCPtr(worldPlanes), worldPlanes, shapeType, nbShapes, SWIGTYPE_p_p_NxShape.getCPtr(shapes), SWIGTYPE_p_NxUserEntityReportTNxShape_p_t.getCPtr(callback), activeGroups, NxGroupsMask.getCPtr(groupsMask), groupsMask);
  }


  public long cullShapes(long nbPlanes, NxPlane worldPlanes, int shapeType, long nbShapes, SWIGTYPE_p_p_NxShape shapes, SWIGTYPE_p_NxUserEntityReportTNxShape_p_t callback, long activeGroups) {
    return JPhysXAdapterJNI.NxScene_cullShapes__SWIG_1(swigCPtr, this, nbPlanes, NxPlane.getCPtr(worldPlanes), worldPlanes, shapeType, nbShapes, SWIGTYPE_p_p_NxShape.getCPtr(shapes), SWIGTYPE_p_NxUserEntityReportTNxShape_p_t.getCPtr(callback), activeGroups);
  }


  public long cullShapes(long nbPlanes, NxPlane worldPlanes, int shapeType, long nbShapes, SWIGTYPE_p_p_NxShape shapes, SWIGTYPE_p_NxUserEntityReportTNxShape_p_t callback) {
    return JPhysXAdapterJNI.NxScene_cullShapes__SWIG_2(swigCPtr, this, nbPlanes, NxPlane.getCPtr(worldPlanes), worldPlanes, shapeType, nbShapes, SWIGTYPE_p_p_NxShape.getCPtr(shapes), SWIGTYPE_p_NxUserEntityReportTNxShape_p_t.getCPtr(callback));
  }


  public boolean checkOverlapSphere(NxSphere worldSphere, int shapeType, long activeGroups, NxGroupsMask groupsMask) {
    return JPhysXAdapterJNI.NxScene_checkOverlapSphere__SWIG_0(swigCPtr, this, NxSphere.getCPtr(worldSphere), worldSphere, shapeType, activeGroups, NxGroupsMask.getCPtr(groupsMask), groupsMask);
  }


  public boolean checkOverlapSphere(NxSphere worldSphere, int shapeType, long activeGroups) {
    return JPhysXAdapterJNI.NxScene_checkOverlapSphere__SWIG_1(swigCPtr, this, NxSphere.getCPtr(worldSphere), worldSphere, shapeType, activeGroups);
  }


  public boolean checkOverlapSphere(NxSphere worldSphere, int shapeType) {
    return JPhysXAdapterJNI.NxScene_checkOverlapSphere__SWIG_2(swigCPtr, this, NxSphere.getCPtr(worldSphere), worldSphere, shapeType);
  }


  public boolean checkOverlapSphere(NxSphere worldSphere) {
    return JPhysXAdapterJNI.NxScene_checkOverlapSphere__SWIG_3(swigCPtr, this, NxSphere.getCPtr(worldSphere), worldSphere);
  }


  public boolean checkOverlapAABB(NxBounds3 worldBounds, int shapeType, long activeGroups, NxGroupsMask groupsMask) {
    return JPhysXAdapterJNI.NxScene_checkOverlapAABB__SWIG_0(swigCPtr, this, NxBounds3.getCPtr(worldBounds), worldBounds, shapeType, activeGroups, NxGroupsMask.getCPtr(groupsMask), groupsMask);
  }


  public boolean checkOverlapAABB(NxBounds3 worldBounds, int shapeType, long activeGroups) {
    return JPhysXAdapterJNI.NxScene_checkOverlapAABB__SWIG_1(swigCPtr, this, NxBounds3.getCPtr(worldBounds), worldBounds, shapeType, activeGroups);
  }


  public boolean checkOverlapAABB(NxBounds3 worldBounds, int shapeType) {
    return JPhysXAdapterJNI.NxScene_checkOverlapAABB__SWIG_2(swigCPtr, this, NxBounds3.getCPtr(worldBounds), worldBounds, shapeType);
  }


  public boolean checkOverlapAABB(NxBounds3 worldBounds) {
    return JPhysXAdapterJNI.NxScene_checkOverlapAABB__SWIG_3(swigCPtr, this, NxBounds3.getCPtr(worldBounds), worldBounds);
  }


  public boolean checkOverlapOBB(NxBox worldBox, int shapeType, long activeGroups, NxGroupsMask groupsMask) {
    return JPhysXAdapterJNI.NxScene_checkOverlapOBB__SWIG_0(swigCPtr, this, NxBox.getCPtr(worldBox), worldBox, shapeType, activeGroups, NxGroupsMask.getCPtr(groupsMask), groupsMask);
  }


  public boolean checkOverlapOBB(NxBox worldBox, int shapeType, long activeGroups) {
    return JPhysXAdapterJNI.NxScene_checkOverlapOBB__SWIG_1(swigCPtr, this, NxBox.getCPtr(worldBox), worldBox, shapeType, activeGroups);
  }


  public boolean checkOverlapOBB(NxBox worldBox, int shapeType) {
    return JPhysXAdapterJNI.NxScene_checkOverlapOBB__SWIG_2(swigCPtr, this, NxBox.getCPtr(worldBox), worldBox, shapeType);
  }


  public boolean checkOverlapOBB(NxBox worldBox) {
    return JPhysXAdapterJNI.NxScene_checkOverlapOBB__SWIG_3(swigCPtr, this, NxBox.getCPtr(worldBox), worldBox);
  }


  public boolean checkOverlapCapsule(NxCapsule worldCapsule, int shapeType, long activeGroups, NxGroupsMask groupsMask) {
    return JPhysXAdapterJNI.NxScene_checkOverlapCapsule__SWIG_0(swigCPtr, this, NxCapsule.getCPtr(worldCapsule), worldCapsule, shapeType, activeGroups, NxGroupsMask.getCPtr(groupsMask), groupsMask);
  }


  public boolean checkOverlapCapsule(NxCapsule worldCapsule, int shapeType, long activeGroups) {
    return JPhysXAdapterJNI.NxScene_checkOverlapCapsule__SWIG_1(swigCPtr, this, NxCapsule.getCPtr(worldCapsule), worldCapsule, shapeType, activeGroups);
  }


  public boolean checkOverlapCapsule(NxCapsule worldCapsule, int shapeType) {
    return JPhysXAdapterJNI.NxScene_checkOverlapCapsule__SWIG_2(swigCPtr, this, NxCapsule.getCPtr(worldCapsule), worldCapsule, shapeType);
  }


  public boolean checkOverlapCapsule(NxCapsule worldCapsule) {
    return JPhysXAdapterJNI.NxScene_checkOverlapCapsule__SWIG_3(swigCPtr, this, NxCapsule.getCPtr(worldCapsule), worldCapsule);
  }


  public NxFluid createFluid(NxFluidDescBase fluidDesc) {
    long cPtr = JPhysXAdapterJNI.NxScene_createFluid(swigCPtr, this, NxFluidDescBase.getCPtr(fluidDesc), fluidDesc);
    return (cPtr == 0) ? null : new NxFluid(cPtr, false);
  }
  public NxFluid createFluid_inplace(NxFluid result, NxFluidDescBase fluidDesc) {
    long cPtr = JPhysXAdapterJNI.NxScene_createFluid(swigCPtr, this, NxFluidDescBase.getCPtr(fluidDesc), fluidDesc);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void releaseFluid(NxFluid fluid) {
    JPhysXAdapterJNI.NxScene_releaseFluid(swigCPtr, this, NxFluid.getCPtr(fluid), fluid);
  }


  public long getNbFluids() {
    return JPhysXAdapterJNI.NxScene_getNbFluids(swigCPtr, this);
  }


  public SWIGTYPE_p_p_NxFluid getFluids() {
    long cPtr = JPhysXAdapterJNI.NxScene_getFluids(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_p_NxFluid(cPtr, false);
  }


  public boolean cookFluidMeshHotspot(NxBounds3 bounds, long packetSizeMultiplier, float restParticlesPerMeter, float kernelRadiusMultiplier, float motionLimitMultiplier, float collisionDistanceMultiplier, NxCompartment compartment, boolean forceStrictCookingFormat) {
    return JPhysXAdapterJNI.NxScene_cookFluidMeshHotspot__SWIG_0(swigCPtr, this, NxBounds3.getCPtr(bounds), bounds, packetSizeMultiplier, restParticlesPerMeter, kernelRadiusMultiplier, motionLimitMultiplier, collisionDistanceMultiplier, NxCompartment.getCPtr(compartment), compartment, forceStrictCookingFormat);
  }


  public boolean cookFluidMeshHotspot(NxBounds3 bounds, long packetSizeMultiplier, float restParticlesPerMeter, float kernelRadiusMultiplier, float motionLimitMultiplier, float collisionDistanceMultiplier, NxCompartment compartment) {
    return JPhysXAdapterJNI.NxScene_cookFluidMeshHotspot__SWIG_1(swigCPtr, this, NxBounds3.getCPtr(bounds), bounds, packetSizeMultiplier, restParticlesPerMeter, kernelRadiusMultiplier, motionLimitMultiplier, collisionDistanceMultiplier, NxCompartment.getCPtr(compartment), compartment);
  }


  public boolean cookFluidMeshHotspot(NxBounds3 bounds, long packetSizeMultiplier, float restParticlesPerMeter, float kernelRadiusMultiplier, float motionLimitMultiplier, float collisionDistanceMultiplier) {
    return JPhysXAdapterJNI.NxScene_cookFluidMeshHotspot__SWIG_2(swigCPtr, this, NxBounds3.getCPtr(bounds), bounds, packetSizeMultiplier, restParticlesPerMeter, kernelRadiusMultiplier, motionLimitMultiplier, collisionDistanceMultiplier);
  }


  public NxCloth createCloth(NxClothDesc clothDesc) {
    long cPtr = JPhysXAdapterJNI.NxScene_createCloth(swigCPtr, this, NxClothDesc.getCPtr(clothDesc), clothDesc);
    return (cPtr == 0) ? null : new NxCloth(cPtr, false);
  }
  public NxCloth createCloth_inplace(NxCloth result, NxClothDesc clothDesc) {
    long cPtr = JPhysXAdapterJNI.NxScene_createCloth(swigCPtr, this, NxClothDesc.getCPtr(clothDesc), clothDesc);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void releaseCloth(NxCloth cloth) {
    JPhysXAdapterJNI.NxScene_releaseCloth(swigCPtr, this, NxCloth.getCPtr(cloth), cloth);
  }


  public long getNbCloths() {
    return JPhysXAdapterJNI.NxScene_getNbCloths(swigCPtr, this);
  }


  public SWIGTYPE_p_p_NxCloth getCloths() {
    long cPtr = JPhysXAdapterJNI.NxScene_getCloths(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_p_NxCloth(cPtr, false);
  }


  public NxSoftBody createSoftBody(NxSoftBodyDesc softBodyDesc) {
    long cPtr = JPhysXAdapterJNI.NxScene_createSoftBody(swigCPtr, this, NxSoftBodyDesc.getCPtr(softBodyDesc), softBodyDesc);
    return (cPtr == 0) ? null : new NxSoftBody(cPtr, false);
  }
  public NxSoftBody createSoftBody_inplace(NxSoftBody result, NxSoftBodyDesc softBodyDesc) {
    long cPtr = JPhysXAdapterJNI.NxScene_createSoftBody(swigCPtr, this, NxSoftBodyDesc.getCPtr(softBodyDesc), softBodyDesc);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public void releaseSoftBody(NxSoftBody softBody) {
    JPhysXAdapterJNI.NxScene_releaseSoftBody(swigCPtr, this, NxSoftBody.getCPtr(softBody), softBody);
  }


  public long getNbSoftBodies() {
    return JPhysXAdapterJNI.NxScene_getNbSoftBodies(swigCPtr, this);
  }


  public SWIGTYPE_p_p_NxSoftBody getSoftBodies() {
    long cPtr = JPhysXAdapterJNI.NxScene_getSoftBodies(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_p_NxSoftBody(cPtr, false);
  }


  public boolean isWritable() {
    return JPhysXAdapterJNI.NxScene_isWritable(swigCPtr, this);
  }


  public void simulate(float elapsedTime) {
    JPhysXAdapterJNI.NxScene_simulate(swigCPtr, this, elapsedTime);
  }


  public boolean checkResults(int status, boolean block) {
    return JPhysXAdapterJNI.NxScene_checkResults__SWIG_0(swigCPtr, this, status, block);
  }


  public boolean checkResults(int status) {
    return JPhysXAdapterJNI.NxScene_checkResults__SWIG_1(swigCPtr, this, status);
  }


  public boolean fetchResults(int status, boolean block, SWIGTYPE_p_unsigned_int errorState) {
    return JPhysXAdapterJNI.NxScene_fetchResults__SWIG_0(swigCPtr, this, status, block, SWIGTYPE_p_unsigned_int.getCPtr(errorState));
  }


  public boolean fetchResults(int status, boolean block) {
    return JPhysXAdapterJNI.NxScene_fetchResults__SWIG_1(swigCPtr, this, status, block);
  }


  public boolean fetchResults(int status) {
    return JPhysXAdapterJNI.NxScene_fetchResults__SWIG_2(swigCPtr, this, status);
  }


  public void flushCaches() {
    JPhysXAdapterJNI.NxScene_flushCaches(swigCPtr, this);
  }


  public NxProfileData readProfileData(boolean clearData) {
    long cPtr = JPhysXAdapterJNI.NxScene_readProfileData(swigCPtr, this, clearData);
    return (cPtr == 0) ? null : new NxProfileData(cPtr, false);
  }
  public NxProfileData readProfileData_inplace(NxProfileData result, boolean clearData) {
    long cPtr = JPhysXAdapterJNI.NxScene_readProfileData(swigCPtr, this, clearData);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  public int pollForWork(int waitType) {
    return JPhysXAdapterJNI.NxScene_pollForWork(swigCPtr, this, waitType);
  }


  public void resetPollForWork() {
    JPhysXAdapterJNI.NxScene_resetPollForWork(swigCPtr, this);
  }


  public int pollForBackgroundWork(int waitType) {
    return JPhysXAdapterJNI.NxScene_pollForBackgroundWork(swigCPtr, this, waitType);
  }


  public void shutdownWorkerThreads() {
    JPhysXAdapterJNI.NxScene_shutdownWorkerThreads(swigCPtr, this);
  }


  public void lockQueries() {
    JPhysXAdapterJNI.NxScene_lockQueries(swigCPtr, this);
  }


  public void unlockQueries() {
    JPhysXAdapterJNI.NxScene_unlockQueries(swigCPtr, this);
  }


  public SWIGTYPE_p_NxSceneQuery createSceneQuery(SWIGTYPE_p_NxSceneQueryDesc desc) {
    long cPtr = JPhysXAdapterJNI.NxScene_createSceneQuery(swigCPtr, this, SWIGTYPE_p_NxSceneQueryDesc.getCPtr(desc));
    return (cPtr == 0) ? null : new SWIGTYPE_p_NxSceneQuery(cPtr, false);
  }


  public boolean releaseSceneQuery(SWIGTYPE_p_NxSceneQuery query) {
    return JPhysXAdapterJNI.NxScene_releaseSceneQuery(swigCPtr, this, SWIGTYPE_p_NxSceneQuery.getCPtr(query));
  }


  public void setSolverBatchSize(long solverBatchSize) {
    JPhysXAdapterJNI.NxScene_setSolverBatchSize(swigCPtr, this, solverBatchSize);
  }


  public long getSolverBatchSize() {
    return JPhysXAdapterJNI.NxScene_getSolverBatchSize(swigCPtr, this);
  }


  public void setUserData(SWIGTYPE_p_void value) {
    JPhysXAdapterJNI.NxScene_userData_set(swigCPtr, this, SWIGTYPE_p_void.getCPtr(value));
  }


  public SWIGTYPE_p_void getUserData() {
    long cPtr = JPhysXAdapterJNI.NxScene_userData_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }


  public void setExtLink(SWIGTYPE_p_void value) {
    JPhysXAdapterJNI.NxScene_extLink_set(swigCPtr, this, SWIGTYPE_p_void.getCPtr(value));
  }


  public SWIGTYPE_p_void getExtLink() {
    long cPtr = JPhysXAdapterJNI.NxScene_extLink_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }


  
  /**
    * <b>Java extension method</b><p>
    * Gives access to the array of NxActor. Actors number can be obtained by getNbActors() call.
    * @param number actor index
    * @return {@link NxActor} entry
    */
  public NxActor getActor(int number) {
    long cPtr = JPhysXAdapterJNI.NxScene_getActor(swigCPtr, this, number);
    return (cPtr == 0) ? null : new NxActor(cPtr, false);
  }
  public NxActor getActor_inplace(NxActor result, int number) {
    long cPtr = JPhysXAdapterJNI.NxScene_getActor(swigCPtr, this, number);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  
  /**
    * <b>Java extension method</b><p>
    * Gives access to the array of NxScene. Cloths number can be obtained by getNbCloths() call.
    * @param number Cloth index
    * @return {@link NxCloth} entry
    */
  public NxCloth getCloth(int number) {
    long cPtr = JPhysXAdapterJNI.NxScene_getCloth(swigCPtr, this, number);
    return (cPtr == 0) ? null : new NxCloth(cPtr, false);
  }
  public NxCloth getCloth_inplace(NxCloth result, int number) {
    long cPtr = JPhysXAdapterJNI.NxScene_getCloth(swigCPtr, this, number);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  
  /**
    * <b>Java extension method</b><p>
    * Gives access to the array of NxScene. Fluids number can be obtained by getNbFluids() call.
    * @param number Fluid index
    * @return {@link NxFluid} entry
    */
  public NxFluid getFluid(int number) {
    long cPtr = JPhysXAdapterJNI.NxScene_getFluid(swigCPtr, this, number);
    return (cPtr == 0) ? null : new NxFluid(cPtr, false);
  }
  public NxFluid getFluid_inplace(NxFluid result, int number) {
    long cPtr = JPhysXAdapterJNI.NxScene_getFluid(swigCPtr, this, number);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  
  /**
    * <b>Java extension method</b><p>
    * Gives access to the array of NxScene. ForceFields number can be obtained by getNbForceFields() call.
    * @param number ForceField index
    * @return {@link NxForceField} entry
    */
  public NxForceField getForceField(int number) {
    long cPtr = JPhysXAdapterJNI.NxScene_getForceField(swigCPtr, this, number);
    return (cPtr == 0) ? null : new NxForceField(cPtr, false);
  }
  public NxForceField getForceField_inplace(NxForceField result, int number) {
    long cPtr = JPhysXAdapterJNI.NxScene_getForceField(swigCPtr, this, number);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



  
  /**
    * <b>Java extension method</b><p>
    * Gives access to the array of NxScene. SoftBodies number can be obtained by getNbSoftBodies() call.
    * @param number SoftBody index
    * @return {@link NxSoftBody} entry
    */
  public NxSoftBody getSoftBody(int number) {
    long cPtr = JPhysXAdapterJNI.NxScene_getSoftBody(swigCPtr, this, number);
    return (cPtr == 0) ? null : new NxSoftBody(cPtr, false);
  }
  public NxSoftBody getSoftBody_inplace(NxSoftBody result, int number) {
    long cPtr = JPhysXAdapterJNI.NxScene_getSoftBody(swigCPtr, this, number);
        if(cPtr != 0){
            result.replaceCPart(cPtr, false);
            return result;
        }
        return null;
  }



}

