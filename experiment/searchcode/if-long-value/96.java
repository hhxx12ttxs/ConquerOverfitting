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
public class NxMeshData {
  private long swigCPtr;
  boolean swigCMemOwn;
  
  /**
   * Default java constructor.
   * @param constructionMarker marks that this is java constructor. Clashing with C++ constructors is unlikely.
   */
  NxMeshData(Boolean constructionMarker){}

  NxMeshData(long cPtr, boolean cMemoryOwn) {
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
  public static long getCPtr(NxMeshData obj) {
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
      JPhysXAdapterJNI.delete_NxMeshData(swigCPtr);
    }

    swigCPtr = 0;
  }

  public boolean equalCPtr(NxMeshData obj) {
    return obj.swigCPtr == this.swigCPtr;
  }


  public void setVerticesPosBegin(SWIGTYPE_p_void value) {
    JPhysXAdapterJNI.NxMeshData_verticesPosBegin_set(swigCPtr, this, SWIGTYPE_p_void.getCPtr(value));
  }


  public SWIGTYPE_p_void getVerticesPosBegin() {
    long cPtr = JPhysXAdapterJNI.NxMeshData_verticesPosBegin_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }


  public void setVerticesNormalBegin(SWIGTYPE_p_void value) {
    JPhysXAdapterJNI.NxMeshData_verticesNormalBegin_set(swigCPtr, this, SWIGTYPE_p_void.getCPtr(value));
  }


  public SWIGTYPE_p_void getVerticesNormalBegin() {
    long cPtr = JPhysXAdapterJNI.NxMeshData_verticesNormalBegin_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }


  public void setVerticesPosByteStride(int value) {
    JPhysXAdapterJNI.NxMeshData_verticesPosByteStride_set(swigCPtr, this, value);
  }


  public int getVerticesPosByteStride() {
    return JPhysXAdapterJNI.NxMeshData_verticesPosByteStride_get(swigCPtr, this);
  }


  public void setVerticesNormalByteStride(int value) {
    JPhysXAdapterJNI.NxMeshData_verticesNormalByteStride_set(swigCPtr, this, value);
  }


  public int getVerticesNormalByteStride() {
    return JPhysXAdapterJNI.NxMeshData_verticesNormalByteStride_get(swigCPtr, this);
  }


  public void setMaxVertices(long value) {
    JPhysXAdapterJNI.NxMeshData_maxVertices_set(swigCPtr, this, value);
  }


  public long getMaxVertices() {
    return JPhysXAdapterJNI.NxMeshData_maxVertices_get(swigCPtr, this);
  }


  public void setNumVerticesPtr(SWIGTYPE_p_unsigned_int value) {
    JPhysXAdapterJNI.NxMeshData_numVerticesPtr_set(swigCPtr, this, SWIGTYPE_p_unsigned_int.getCPtr(value));
  }


  public SWIGTYPE_p_unsigned_int getNumVerticesPtr() {
    long cPtr = JPhysXAdapterJNI.NxMeshData_numVerticesPtr_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_unsigned_int(cPtr, false);
  }


  public void setIndicesBegin(SWIGTYPE_p_void value) {
    JPhysXAdapterJNI.NxMeshData_indicesBegin_set(swigCPtr, this, SWIGTYPE_p_void.getCPtr(value));
  }


  public SWIGTYPE_p_void getIndicesBegin() {
    long cPtr = JPhysXAdapterJNI.NxMeshData_indicesBegin_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }


  public void setIndicesByteStride(int value) {
    JPhysXAdapterJNI.NxMeshData_indicesByteStride_set(swigCPtr, this, value);
  }


  public int getIndicesByteStride() {
    return JPhysXAdapterJNI.NxMeshData_indicesByteStride_get(swigCPtr, this);
  }


  public void setMaxIndices(long value) {
    JPhysXAdapterJNI.NxMeshData_maxIndices_set(swigCPtr, this, value);
  }


  public long getMaxIndices() {
    return JPhysXAdapterJNI.NxMeshData_maxIndices_get(swigCPtr, this);
  }


  public void setNumIndicesPtr(SWIGTYPE_p_unsigned_int value) {
    JPhysXAdapterJNI.NxMeshData_numIndicesPtr_set(swigCPtr, this, SWIGTYPE_p_unsigned_int.getCPtr(value));
  }


  public SWIGTYPE_p_unsigned_int getNumIndicesPtr() {
    long cPtr = JPhysXAdapterJNI.NxMeshData_numIndicesPtr_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_unsigned_int(cPtr, false);
  }


  public void setParentIndicesBegin(SWIGTYPE_p_void value) {
    JPhysXAdapterJNI.NxMeshData_parentIndicesBegin_set(swigCPtr, this, SWIGTYPE_p_void.getCPtr(value));
  }


  public SWIGTYPE_p_void getParentIndicesBegin() {
    long cPtr = JPhysXAdapterJNI.NxMeshData_parentIndicesBegin_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }


  public void setParentIndicesByteStride(int value) {
    JPhysXAdapterJNI.NxMeshData_parentIndicesByteStride_set(swigCPtr, this, value);
  }


  public int getParentIndicesByteStride() {
    return JPhysXAdapterJNI.NxMeshData_parentIndicesByteStride_get(swigCPtr, this);
  }


  public void setMaxParentIndices(long value) {
    JPhysXAdapterJNI.NxMeshData_maxParentIndices_set(swigCPtr, this, value);
  }


  public long getMaxParentIndices() {
    return JPhysXAdapterJNI.NxMeshData_maxParentIndices_get(swigCPtr, this);
  }


  public void setNumParentIndicesPtr(SWIGTYPE_p_unsigned_int value) {
    JPhysXAdapterJNI.NxMeshData_numParentIndicesPtr_set(swigCPtr, this, SWIGTYPE_p_unsigned_int.getCPtr(value));
  }


  public SWIGTYPE_p_unsigned_int getNumParentIndicesPtr() {
    long cPtr = JPhysXAdapterJNI.NxMeshData_numParentIndicesPtr_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_unsigned_int(cPtr, false);
  }


  public void setDirtyBufferFlagsPtr(SWIGTYPE_p_unsigned_int value) {
    JPhysXAdapterJNI.NxMeshData_dirtyBufferFlagsPtr_set(swigCPtr, this, SWIGTYPE_p_unsigned_int.getCPtr(value));
  }


  public SWIGTYPE_p_unsigned_int getDirtyBufferFlagsPtr() {
    long cPtr = JPhysXAdapterJNI.NxMeshData_dirtyBufferFlagsPtr_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_unsigned_int(cPtr, false);
  }


  public void setFlags(long value) {
    JPhysXAdapterJNI.NxMeshData_flags_set(swigCPtr, this, value);
  }


  public long getFlags() {
    return JPhysXAdapterJNI.NxMeshData_flags_get(swigCPtr, this);
  }


  public void setName(String value) {
    JPhysXAdapterJNI.NxMeshData_name_set(swigCPtr, this, value);
  }


  public String getName() {
    return JPhysXAdapterJNI.NxMeshData_name_get(swigCPtr, this);
  }


  public void setToDefault() {
    JPhysXAdapterJNI.NxMeshData_setToDefault(swigCPtr, this);
  }


  public boolean isValid() {
    return JPhysXAdapterJNI.NxMeshData_isValid(swigCPtr, this);
  }


  public NxMeshData() {
    this(JPhysXAdapterJNI.new_NxMeshData(), true);
  }


}

