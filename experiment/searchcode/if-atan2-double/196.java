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
public class NxMath {
  private long swigCPtr;
  boolean swigCMemOwn;
  
  /**
   * Default java constructor.
   * @param constructionMarker marks that this is java constructor. Clashing with C++ constructors is unlikely.
   */
  NxMath(Boolean constructionMarker){}

  NxMath(long cPtr, boolean cMemoryOwn) {
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
  public static long getCPtr(NxMath obj) {
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
      JPhysXAdapterJNI.delete_NxMath(swigCPtr);
    }

    swigCPtr = 0;
  }

  public boolean equalCPtr(NxMath obj) {
    return obj.swigCPtr == this.swigCPtr;
  }


  public static boolean equals(float arg0, float arg1, float eps) {
    return JPhysXAdapterJNI.NxMath_equals__SWIG_0(arg0, arg1, eps);
  }


  public static boolean equals(double arg0, double arg1, double eps) {
    return JPhysXAdapterJNI.NxMath_equals__SWIG_1(arg0, arg1, eps);
  }


  public static float floor(float arg0) {
    return JPhysXAdapterJNI.NxMath_floor__SWIG_0(arg0);
  }


  public static double floor(double arg0) {
    return JPhysXAdapterJNI.NxMath_floor__SWIG_1(arg0);
  }


  public static float ceil(float arg0) {
    return JPhysXAdapterJNI.NxMath_ceil__SWIG_0(arg0);
  }


  public static double ceil(double arg0) {
    return JPhysXAdapterJNI.NxMath_ceil__SWIG_1(arg0);
  }


  public static int trunc(float arg0) {
    return JPhysXAdapterJNI.NxMath_trunc__SWIG_0(arg0);
  }


  public static int trunc(double arg0) {
    return JPhysXAdapterJNI.NxMath_trunc__SWIG_1(arg0);
  }


  public static float abs(float arg0) {
    return JPhysXAdapterJNI.NxMath_abs__SWIG_0(arg0);
  }


  public static double abs(double arg0) {
    return JPhysXAdapterJNI.NxMath_abs__SWIG_1(arg0);
  }


  public static int abs(int arg0) {
    return JPhysXAdapterJNI.NxMath_abs__SWIG_2(arg0);
  }


  public static float sign(float arg0) {
    return JPhysXAdapterJNI.NxMath_sign__SWIG_0(arg0);
  }


  public static double sign(double arg0) {
    return JPhysXAdapterJNI.NxMath_sign__SWIG_1(arg0);
  }


  public static int sign(int arg0) {
    return JPhysXAdapterJNI.NxMath_sign__SWIG_2(arg0);
  }


  public static float max(float arg0, float arg1) {
    return JPhysXAdapterJNI.NxMath_max__SWIG_0(arg0, arg1);
  }


  public static double max(double arg0, double arg1) {
    return JPhysXAdapterJNI.NxMath_max__SWIG_1(arg0, arg1);
  }


  public static int max(int arg0, int arg1) {
    return JPhysXAdapterJNI.NxMath_max__SWIG_2(arg0, arg1);
  }


  public static long max(long arg0, long arg1) {
    return JPhysXAdapterJNI.NxMath_max__SWIG_3(arg0, arg1);
  }


  public static float min(float arg0, float arg1) {
    return JPhysXAdapterJNI.NxMath_min__SWIG_0(arg0, arg1);
  }


  public static double min(double arg0, double arg1) {
    return JPhysXAdapterJNI.NxMath_min__SWIG_1(arg0, arg1);
  }


  public static int min(int arg0, int arg1) {
    return JPhysXAdapterJNI.NxMath_min__SWIG_2(arg0, arg1);
  }


  public static long min(long arg0, long arg1) {
    return JPhysXAdapterJNI.NxMath_min__SWIG_3(arg0, arg1);
  }


  public static float mod(float x, float y) {
    return JPhysXAdapterJNI.NxMath_mod__SWIG_0(x, y);
  }


  public static double mod(double x, double y) {
    return JPhysXAdapterJNI.NxMath_mod__SWIG_1(x, y);
  }


  public static float clamp(float v, float hi, float low) {
    return JPhysXAdapterJNI.NxMath_clamp__SWIG_0(v, hi, low);
  }


  public static double clamp(double v, double hi, double low) {
    return JPhysXAdapterJNI.NxMath_clamp__SWIG_1(v, hi, low);
  }


  public static long clamp(long v, long hi, long low) {
    return JPhysXAdapterJNI.NxMath_clamp__SWIG_2(v, hi, low);
  }


  public static int clamp(int v, int hi, int low) {
    return JPhysXAdapterJNI.NxMath_clamp__SWIG_3(v, hi, low);
  }


  public static float sqrt(float arg0) {
    return JPhysXAdapterJNI.NxMath_sqrt__SWIG_0(arg0);
  }


  public static double sqrt(double arg0) {
    return JPhysXAdapterJNI.NxMath_sqrt__SWIG_1(arg0);
  }


  public static float recipSqrt(float arg0) {
    return JPhysXAdapterJNI.NxMath_recipSqrt__SWIG_0(arg0);
  }


  public static double recipSqrt(double arg0) {
    return JPhysXAdapterJNI.NxMath_recipSqrt__SWIG_1(arg0);
  }


  public static float pow(float x, float y) {
    return JPhysXAdapterJNI.NxMath_pow__SWIG_0(x, y);
  }


  public static double pow(double x, double y) {
    return JPhysXAdapterJNI.NxMath_pow__SWIG_1(x, y);
  }


  public static float exp(float arg0) {
    return JPhysXAdapterJNI.NxMath_exp__SWIG_0(arg0);
  }


  public static double exp(double arg0) {
    return JPhysXAdapterJNI.NxMath_exp__SWIG_1(arg0);
  }


  public static float logE(float arg0) {
    return JPhysXAdapterJNI.NxMath_logE__SWIG_0(arg0);
  }


  public static double logE(double arg0) {
    return JPhysXAdapterJNI.NxMath_logE__SWIG_1(arg0);
  }


  public static float log2(float arg0) {
    return JPhysXAdapterJNI.NxMath_log2__SWIG_0(arg0);
  }


  public static double log2(double arg0) {
    return JPhysXAdapterJNI.NxMath_log2__SWIG_1(arg0);
  }


  public static float log10(float arg0) {
    return JPhysXAdapterJNI.NxMath_log10__SWIG_0(arg0);
  }


  public static double log10(double arg0) {
    return JPhysXAdapterJNI.NxMath_log10__SWIG_1(arg0);
  }


  public static float degToRad(float arg0) {
    return JPhysXAdapterJNI.NxMath_degToRad__SWIG_0(arg0);
  }


  public static double degToRad(double arg0) {
    return JPhysXAdapterJNI.NxMath_degToRad__SWIG_1(arg0);
  }


  public static float radToDeg(float arg0) {
    return JPhysXAdapterJNI.NxMath_radToDeg__SWIG_0(arg0);
  }


  public static double radToDeg(double arg0) {
    return JPhysXAdapterJNI.NxMath_radToDeg__SWIG_1(arg0);
  }


  public static float sin(float arg0) {
    return JPhysXAdapterJNI.NxMath_sin__SWIG_0(arg0);
  }


  public static double sin(double arg0) {
    return JPhysXAdapterJNI.NxMath_sin__SWIG_1(arg0);
  }


  public static float cos(float arg0) {
    return JPhysXAdapterJNI.NxMath_cos__SWIG_0(arg0);
  }


  public static double cos(double arg0) {
    return JPhysXAdapterJNI.NxMath_cos__SWIG_1(arg0);
  }


  public static void sinCos(float arg0, SWIGTYPE_p_float sin, SWIGTYPE_p_float cos) {
    JPhysXAdapterJNI.NxMath_sinCos__SWIG_0(arg0, SWIGTYPE_p_float.getCPtr(sin), SWIGTYPE_p_float.getCPtr(cos));
  }


  public static void sinCos(double arg0, SWIGTYPE_p_double sin, SWIGTYPE_p_double cos) {
    JPhysXAdapterJNI.NxMath_sinCos__SWIG_1(arg0, SWIGTYPE_p_double.getCPtr(sin), SWIGTYPE_p_double.getCPtr(cos));
  }


  public static float tan(float arg0) {
    return JPhysXAdapterJNI.NxMath_tan__SWIG_0(arg0);
  }


  public static double tan(double arg0) {
    return JPhysXAdapterJNI.NxMath_tan__SWIG_1(arg0);
  }


  public static float asin(float arg0) {
    return JPhysXAdapterJNI.NxMath_asin__SWIG_0(arg0);
  }


  public static double asin(double arg0) {
    return JPhysXAdapterJNI.NxMath_asin__SWIG_1(arg0);
  }


  public static float acos(float arg0) {
    return JPhysXAdapterJNI.NxMath_acos__SWIG_0(arg0);
  }


  public static double acos(double arg0) {
    return JPhysXAdapterJNI.NxMath_acos__SWIG_1(arg0);
  }


  public static float atan(float arg0) {
    return JPhysXAdapterJNI.NxMath_atan__SWIG_0(arg0);
  }


  public static double atan(double arg0) {
    return JPhysXAdapterJNI.NxMath_atan__SWIG_1(arg0);
  }


  public static float atan2(float x, float y) {
    return JPhysXAdapterJNI.NxMath_atan2__SWIG_0(x, y);
  }


  public static double atan2(double x, double y) {
    return JPhysXAdapterJNI.NxMath_atan2__SWIG_1(x, y);
  }


  public static float rand(float a, float b) {
    return JPhysXAdapterJNI.NxMath_rand__SWIG_0(a, b);
  }


  public static int rand(int a, int b) {
    return JPhysXAdapterJNI.NxMath_rand__SWIG_1(a, b);
  }


  public static long hash(SWIGTYPE_p_unsigned_int array, long n) {
    return JPhysXAdapterJNI.NxMath_hash(SWIGTYPE_p_unsigned_int.getCPtr(array), n);
  }


  public static int hash32(int arg0) {
    return JPhysXAdapterJNI.NxMath_hash32(arg0);
  }


  public static boolean isFinite(float x) {
    return JPhysXAdapterJNI.NxMath_isFinite__SWIG_0(x);
  }


  public static boolean isFinite(double x) {
    return JPhysXAdapterJNI.NxMath_isFinite__SWIG_1(x);
  }


  public NxMath() {
    this(JPhysXAdapterJNI.new_NxMath(), true);
  }


}

