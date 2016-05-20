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
public class NxSceneStats {
  private long swigCPtr;
  boolean swigCMemOwn;
  
  /**
   * Default java constructor.
   * @param constructionMarker marks that this is java constructor. Clashing with C++ constructors is unlikely.
   */
  NxSceneStats(Boolean constructionMarker){}

  NxSceneStats(long cPtr, boolean cMemoryOwn) {
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
  public static long getCPtr(NxSceneStats obj) {
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
      JPhysXAdapterJNI.delete_NxSceneStats(swigCPtr);
    }

    swigCPtr = 0;
  }

  public boolean equalCPtr(NxSceneStats obj) {
    return obj.swigCPtr == this.swigCPtr;
  }


  public void setNumContacts(long value) {
    JPhysXAdapterJNI.NxSceneStats_numContacts_set(swigCPtr, this, value);
  }


  public long getNumContacts() {
    return JPhysXAdapterJNI.NxSceneStats_numContacts_get(swigCPtr, this);
  }


  public void setMaxContacts(long value) {
    JPhysXAdapterJNI.NxSceneStats_maxContacts_set(swigCPtr, this, value);
  }


  public long getMaxContacts() {
    return JPhysXAdapterJNI.NxSceneStats_maxContacts_get(swigCPtr, this);
  }


  public void setNumPairs(long value) {
    JPhysXAdapterJNI.NxSceneStats_numPairs_set(swigCPtr, this, value);
  }


  public long getNumPairs() {
    return JPhysXAdapterJNI.NxSceneStats_numPairs_get(swigCPtr, this);
  }


  public void setMaxPairs(long value) {
    JPhysXAdapterJNI.NxSceneStats_maxPairs_set(swigCPtr, this, value);
  }


  public long getMaxPairs() {
    return JPhysXAdapterJNI.NxSceneStats_maxPairs_get(swigCPtr, this);
  }


  public void setNumDynamicActorsInAwakeGroups(long value) {
    JPhysXAdapterJNI.NxSceneStats_numDynamicActorsInAwakeGroups_set(swigCPtr, this, value);
  }


  public long getNumDynamicActorsInAwakeGroups() {
    return JPhysXAdapterJNI.NxSceneStats_numDynamicActorsInAwakeGroups_get(swigCPtr, this);
  }


  public void setMaxDynamicActorsInAwakeGroups(long value) {
    JPhysXAdapterJNI.NxSceneStats_maxDynamicActorsInAwakeGroups_set(swigCPtr, this, value);
  }


  public long getMaxDynamicActorsInAwakeGroups() {
    return JPhysXAdapterJNI.NxSceneStats_maxDynamicActorsInAwakeGroups_get(swigCPtr, this);
  }


  public void setNumAxisConstraints(long value) {
    JPhysXAdapterJNI.NxSceneStats_numAxisConstraints_set(swigCPtr, this, value);
  }


  public long getNumAxisConstraints() {
    return JPhysXAdapterJNI.NxSceneStats_numAxisConstraints_get(swigCPtr, this);
  }


  public void setMaxAxisConstraints(long value) {
    JPhysXAdapterJNI.NxSceneStats_maxAxisConstraints_set(swigCPtr, this, value);
  }


  public long getMaxAxisConstraints() {
    return JPhysXAdapterJNI.NxSceneStats_maxAxisConstraints_get(swigCPtr, this);
  }


  public void setNumSolverBodies(long value) {
    JPhysXAdapterJNI.NxSceneStats_numSolverBodies_set(swigCPtr, this, value);
  }


  public long getNumSolverBodies() {
    return JPhysXAdapterJNI.NxSceneStats_numSolverBodies_get(swigCPtr, this);
  }


  public void setMaxSolverBodies(long value) {
    JPhysXAdapterJNI.NxSceneStats_maxSolverBodies_set(swigCPtr, this, value);
  }


  public long getMaxSolverBodies() {
    return JPhysXAdapterJNI.NxSceneStats_maxSolverBodies_get(swigCPtr, this);
  }


  public void setNumActors(long value) {
    JPhysXAdapterJNI.NxSceneStats_numActors_set(swigCPtr, this, value);
  }


  public long getNumActors() {
    return JPhysXAdapterJNI.NxSceneStats_numActors_get(swigCPtr, this);
  }


  public void setMaxActors(long value) {
    JPhysXAdapterJNI.NxSceneStats_maxActors_set(swigCPtr, this, value);
  }


  public long getMaxActors() {
    return JPhysXAdapterJNI.NxSceneStats_maxActors_get(swigCPtr, this);
  }


  public void setNumDynamicActors(long value) {
    JPhysXAdapterJNI.NxSceneStats_numDynamicActors_set(swigCPtr, this, value);
  }


  public long getNumDynamicActors() {
    return JPhysXAdapterJNI.NxSceneStats_numDynamicActors_get(swigCPtr, this);
  }


  public void setMaxDynamicActors(long value) {
    JPhysXAdapterJNI.NxSceneStats_maxDynamicActors_set(swigCPtr, this, value);
  }


  public long getMaxDynamicActors() {
    return JPhysXAdapterJNI.NxSceneStats_maxDynamicActors_get(swigCPtr, this);
  }


  public void setNumStaticShapes(long value) {
    JPhysXAdapterJNI.NxSceneStats_numStaticShapes_set(swigCPtr, this, value);
  }


  public long getNumStaticShapes() {
    return JPhysXAdapterJNI.NxSceneStats_numStaticShapes_get(swigCPtr, this);
  }


  public void setMaxStaticShapes(long value) {
    JPhysXAdapterJNI.NxSceneStats_maxStaticShapes_set(swigCPtr, this, value);
  }


  public long getMaxStaticShapes() {
    return JPhysXAdapterJNI.NxSceneStats_maxStaticShapes_get(swigCPtr, this);
  }


  public void setNumDynamicShapes(long value) {
    JPhysXAdapterJNI.NxSceneStats_numDynamicShapes_set(swigCPtr, this, value);
  }


  public long getNumDynamicShapes() {
    return JPhysXAdapterJNI.NxSceneStats_numDynamicShapes_get(swigCPtr, this);
  }


  public void setMaxDynamicShapes(long value) {
    JPhysXAdapterJNI.NxSceneStats_maxDynamicShapes_set(swigCPtr, this, value);
  }


  public long getMaxDynamicShapes() {
    return JPhysXAdapterJNI.NxSceneStats_maxDynamicShapes_get(swigCPtr, this);
  }


  public void setNumJoints(long value) {
    JPhysXAdapterJNI.NxSceneStats_numJoints_set(swigCPtr, this, value);
  }


  public long getNumJoints() {
    return JPhysXAdapterJNI.NxSceneStats_numJoints_get(swigCPtr, this);
  }


  public void setMaxJoints(long value) {
    JPhysXAdapterJNI.NxSceneStats_maxJoints_set(swigCPtr, this, value);
  }


  public long getMaxJoints() {
    return JPhysXAdapterJNI.NxSceneStats_maxJoints_get(swigCPtr, this);
  }


  public NxSceneStats() {
    this(JPhysXAdapterJNI.new_NxSceneStats(), true);
  }


  public void reset() {
    JPhysXAdapterJNI.NxSceneStats_reset(swigCPtr, this);
  }


}

