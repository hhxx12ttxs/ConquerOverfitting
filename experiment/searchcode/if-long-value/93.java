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
public class NxSoftBodyMeshDesc {
  private long swigCPtr;
  boolean swigCMemOwn;
  
  /**
   * Default java constructor.
   * @param constructionMarker marks that this is java constructor. Clashing with C++ constructors is unlikely.
   */
  NxSoftBodyMeshDesc(Boolean constructionMarker){}

  NxSoftBodyMeshDesc(long cPtr, boolean cMemoryOwn) {
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
  public static long getCPtr(NxSoftBodyMeshDesc obj) {
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
      JPhysXAdapterJNI.delete_NxSoftBodyMeshDesc(swigCPtr);
    }

    swigCPtr = 0;
  }

  public boolean equalCPtr(NxSoftBodyMeshDesc obj) {
    return obj.swigCPtr == this.swigCPtr;
  }


  public void setNumVertices(long value) {
    JPhysXAdapterJNI.NxSoftBodyMeshDesc_numVertices_set(swigCPtr, this, value);
  }


  public long getNumVertices() {
    return JPhysXAdapterJNI.NxSoftBodyMeshDesc_numVertices_get(swigCPtr, this);
  }


  public void setNumTetrahedra(long value) {
    JPhysXAdapterJNI.NxSoftBodyMeshDesc_numTetrahedra_set(swigCPtr, this, value);
  }


  public long getNumTetrahedra() {
    return JPhysXAdapterJNI.NxSoftBodyMeshDesc_numTetrahedra_get(swigCPtr, this);
  }


  public void setVertexStrideBytes(long value) {
    JPhysXAdapterJNI.NxSoftBodyMeshDesc_vertexStrideBytes_set(swigCPtr, this, value);
  }


  public long getVertexStrideBytes() {
    return JPhysXAdapterJNI.NxSoftBodyMeshDesc_vertexStrideBytes_get(swigCPtr, this);
  }


  public void setTetrahedronStrideBytes(long value) {
    JPhysXAdapterJNI.NxSoftBodyMeshDesc_tetrahedronStrideBytes_set(swigCPtr, this, value);
  }


  public long getTetrahedronStrideBytes() {
    return JPhysXAdapterJNI.NxSoftBodyMeshDesc_tetrahedronStrideBytes_get(swigCPtr, this);
  }


  public void setVertices(SWIGTYPE_p_void value) {
    JPhysXAdapterJNI.NxSoftBodyMeshDesc_vertices_set(swigCPtr, this, SWIGTYPE_p_void.getCPtr(value));
  }


  public SWIGTYPE_p_void getVertices() {
    long cPtr = JPhysXAdapterJNI.NxSoftBodyMeshDesc_vertices_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }


  public void setTetrahedra(SWIGTYPE_p_void value) {
    JPhysXAdapterJNI.NxSoftBodyMeshDesc_tetrahedra_set(swigCPtr, this, SWIGTYPE_p_void.getCPtr(value));
  }


  public SWIGTYPE_p_void getTetrahedra() {
    long cPtr = JPhysXAdapterJNI.NxSoftBodyMeshDesc_tetrahedra_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }


  public void setFlags(long value) {
    JPhysXAdapterJNI.NxSoftBodyMeshDesc_flags_set(swigCPtr, this, value);
  }


  public long getFlags() {
    return JPhysXAdapterJNI.NxSoftBodyMeshDesc_flags_get(swigCPtr, this);
  }


  public void setVertexMassStrideBytes(long value) {
    JPhysXAdapterJNI.NxSoftBodyMeshDesc_vertexMassStrideBytes_set(swigCPtr, this, value);
  }


  public long getVertexMassStrideBytes() {
    return JPhysXAdapterJNI.NxSoftBodyMeshDesc_vertexMassStrideBytes_get(swigCPtr, this);
  }


  public void setVertexFlagStrideBytes(long value) {
    JPhysXAdapterJNI.NxSoftBodyMeshDesc_vertexFlagStrideBytes_set(swigCPtr, this, value);
  }


  public long getVertexFlagStrideBytes() {
    return JPhysXAdapterJNI.NxSoftBodyMeshDesc_vertexFlagStrideBytes_get(swigCPtr, this);
  }


  public void setVertexMasses(SWIGTYPE_p_void value) {
    JPhysXAdapterJNI.NxSoftBodyMeshDesc_vertexMasses_set(swigCPtr, this, SWIGTYPE_p_void.getCPtr(value));
  }


  public SWIGTYPE_p_void getVertexMasses() {
    long cPtr = JPhysXAdapterJNI.NxSoftBodyMeshDesc_vertexMasses_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }


  public void setVertexFlags(SWIGTYPE_p_void value) {
    JPhysXAdapterJNI.NxSoftBodyMeshDesc_vertexFlags_set(swigCPtr, this, SWIGTYPE_p_void.getCPtr(value));
  }


  public SWIGTYPE_p_void getVertexFlags() {
    long cPtr = JPhysXAdapterJNI.NxSoftBodyMeshDesc_vertexFlags_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }


  public NxSoftBodyMeshDesc() {
    this(JPhysXAdapterJNI.new_NxSoftBodyMeshDesc(), true);
  }


  public void setToDefault() {
    JPhysXAdapterJNI.NxSoftBodyMeshDesc_setToDefault(swigCPtr, this);
  }


  public boolean isValid() {
    return JPhysXAdapterJNI.NxSoftBodyMeshDesc_isValid(swigCPtr, this);
  }


}

