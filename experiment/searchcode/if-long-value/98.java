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
public class NxClothMeshDesc extends NxSimpleTriangleMesh {
  private long swigCPtr;

  /**
   * Default java constructor.
   * @param constructionMarker marks that this is java constructor. Clashing with C++ constructors is unlikely.
   */
  NxClothMeshDesc(Boolean constructionMarker){
	super(constructionMarker);
  }

  NxClothMeshDesc(long cPtr, boolean cMemoryOwn) {
	super(Boolean.TRUE);
    setCPart(cPtr, cMemoryOwn);
  }


  /**
   * Contains functionality of object construction.
   * @param cPtr c-pointer to the native object
   * @param cMemoryOwn is native object needs to be deleted when java object is deleted?
   */
  void setCPart(long cPtr, boolean cMemoryOwn) {
      super.setCPart(JPhysXAdapterJNI.SWIGNxClothMeshDescUpcast(cPtr), cMemoryOwn);
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
  public static long getCPtr(NxClothMeshDesc obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }


  protected void finalize() {
    delete();
  }


  public synchronized void delete() {
    if(swigCPtr != 0 && swigCMemOwn) {
      swigCMemOwn = false;
      JPhysXAdapterJNI.delete_NxClothMeshDesc(swigCPtr);
    }

    swigCPtr = 0;
    super.delete();
  }

  public boolean equalCPtr(NxClothMeshDesc obj) {
    return obj.swigCPtr == this.swigCPtr;
  }


  public void setVertexMassStrideBytes(long value) {
    JPhysXAdapterJNI.NxClothMeshDesc_vertexMassStrideBytes_set(swigCPtr, this, value);
  }


  public long getVertexMassStrideBytes() {
    return JPhysXAdapterJNI.NxClothMeshDesc_vertexMassStrideBytes_get(swigCPtr, this);
  }


  public void setVertexFlagStrideBytes(long value) {
    JPhysXAdapterJNI.NxClothMeshDesc_vertexFlagStrideBytes_set(swigCPtr, this, value);
  }


  public long getVertexFlagStrideBytes() {
    return JPhysXAdapterJNI.NxClothMeshDesc_vertexFlagStrideBytes_get(swigCPtr, this);
  }


  public void setVertexMasses(SWIGTYPE_p_void value) {
    JPhysXAdapterJNI.NxClothMeshDesc_vertexMasses_set(swigCPtr, this, SWIGTYPE_p_void.getCPtr(value));
  }


  public SWIGTYPE_p_void getVertexMasses() {
    long cPtr = JPhysXAdapterJNI.NxClothMeshDesc_vertexMasses_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }


  public void setVertexFlags(SWIGTYPE_p_void value) {
    JPhysXAdapterJNI.NxClothMeshDesc_vertexFlags_set(swigCPtr, this, SWIGTYPE_p_void.getCPtr(value));
  }


  public SWIGTYPE_p_void getVertexFlags() {
    long cPtr = JPhysXAdapterJNI.NxClothMeshDesc_vertexFlags_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }


  public void setWeldingDistance(float value) {
    JPhysXAdapterJNI.NxClothMeshDesc_weldingDistance_set(swigCPtr, this, value);
  }


  public float getWeldingDistance() {
    return JPhysXAdapterJNI.NxClothMeshDesc_weldingDistance_get(swigCPtr, this);
  }


  public NxClothMeshDesc() {
    this(JPhysXAdapterJNI.new_NxClothMeshDesc(), true);
  }


  public void setToDefault() {
    JPhysXAdapterJNI.NxClothMeshDesc_setToDefault(swigCPtr, this);
  }


  public boolean isValid() {
    return JPhysXAdapterJNI.NxClothMeshDesc_isValid(swigCPtr, this);
  }


}

