/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bitbucket.fredgrott.activewallpaper.log;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL;




import android.opengl.GLException;



/**
 * Implement an error checking wrapper. The wrapper will automatically call
     * glError after each GL operation, and throw a GLException if an error
     * occurs. (By design, calling glError itself will not cause an exception
     * to be thrown.) Enabling error checking is an alternative to manually
     *  calling glError after every GL operation.
 */
class GLErrorWrapper extends GLWrapperBase {
    
    /** The m check error. */
    boolean mCheckError;
    
    /** The m check thread. */
    boolean mCheckThread;
    
    /** The m our thread. */
    Thread mOurThread;

    /**
     * Instantiates a new gL error wrapper.
     *
     * @param gl the gl
     * @param configFlags the config flags
     */
    public GLErrorWrapper(GL gl, int configFlags) {
        super(gl);
        mCheckError = (configFlags & GLDebugHelper.CONFIG_CHECK_GL_ERROR) != 0;
        mCheckThread = (configFlags & GLDebugHelper.CONFIG_CHECK_THREAD) != 0;
    }

    /**
     * Check thread.
     */
    private void checkThread() {
        if (mCheckThread) {
            Thread currentThread = Thread.currentThread();
            if (mOurThread == null) {
                mOurThread = currentThread;
            } else {
                if (!mOurThread.equals(currentThread)) {
                    throw new GLException(GLDebugHelper.ERROR_WRONG_THREAD,
                            "OpenGL method called from wrong thread.");
                }
            }
        }
    }

    /**
     * Check error.
     */
    private void checkError() {
        if (mCheckError) {
            int glError;
            if ((glError = mgl.glGetError()) != 0) {
                throw new GLException(glError);
            }
        }
    }

    // ---------------------------------------------------------------------
    // GL10 methods:

   
    /**
     * Gl active texture.
     *
     * @param texture the texture
     * @see{@link  javax.microedition.khronos.opengles.GL10#glActiveTexture(int)}
     */
    public void glActiveTexture(int texture) {
        checkThread();
        mgl.glActiveTexture(texture);
        checkError();
    }

    
    /**
     * Gl alpha func.
     *
     * @param func the func
     * @param ref the ref
     * @see{@link javax.microedition.khronos.opengles.GL10#glAlphaFunc(int,float)}
     */
    public void glAlphaFunc(int func, float ref) {
        checkThread();
        mgl.glAlphaFunc(func, ref);
        checkError();
    }

    
    /**
     * Gl alpha funcx.
     *
     * @param func the func
     * @param ref the ref
     * @see{@link  javax.microedition.khronos.opengles.GL10#glAlphaFuncx(int, int)}
     */
    public void glAlphaFuncx(int func, int ref) {
        checkThread();
        mgl.glAlphaFuncx(func, ref);
        checkError();
    }

    
    /**
     * Gl bind texture.
     *
     * @param target the target
     * @param texture the texture
     * @see{@link javax.microedition.khronos.opengles.GL10#glBindTexture(int, int)}
     */
    public void glBindTexture(int target, int texture) {
        checkThread();
        mgl.glBindTexture(target, texture);
        checkError();
    }

   
    /**
     * Gl blend func.
     *
     * @param sfactor the sfactor
     * @param dfactor the dfactor
     * @see{@link  javax.microedition.khronos.opengles.GL10#glBlendFunc(int, int)}
     */
    public void glBlendFunc(int sfactor, int dfactor) {
        checkThread();
        mgl.glBlendFunc(sfactor, dfactor);
        checkError();
    }

    
    /**
     * Gl clear.
     *
     * @param mask the mask
     * @see{@link javax.microedition.khronos.opengles.GL10#glClear(int)}
     */
    public void glClear(int mask) {
        checkThread();
        mgl.glClear(mask);
        checkError();
    }

   
    /**
     * Gl clear color.
     *
     * @param red the red
     * @param green the green
     * @param blue the blue
     * @param alpha the alpha
     * @see{@link javax.microedition.khronos.opengles.GL10#glClearColor(float, float, float, float)}
     */
    public void glClearColor(float red, float green, float blue, float alpha) {
        checkThread();
        mgl.glClearColor(red, green, blue, alpha);
        checkError();
    }

    
    /**
     * Gl clear colorx.
     *
     * @param red the red
     * @param green the green
     * @param blue the blue
     * @param alpha the alpha
     * @see{@link javax.microedition.khronos.opengles.GL10#glClearColorx(int, int, int, int)}
     */
    public void glClearColorx(int red, int green, int blue, int alpha) {
        checkThread();
        mgl.glClearColorx(red, green, blue, alpha);
        checkError();
    }

    
    /**
     * Gl clear depthf.
     *
     * @param depth the depth
     * @see{@link javax.microedition.khronos.opengles.GL10#glClearDepthf(float)}
     */
    public void glClearDepthf(float depth) {
        checkThread();
        mgl.glClearDepthf(depth);
        checkError();
    }

    
    /**
     * Gl clear depthx.
     *
     * @param depth the depth
     * @see{@link  javax.microedition.khronos.opengles.GL10#glClearDepthx(int)}
     */
    public void glClearDepthx(int depth) {
        checkThread();
        mgl.glClearDepthx(depth);
        checkError();
    }

    
    /**
     * Gl clear stencil.
     *
     * @param s the s
     * @see{@link  javax.microedition.khronos.opengles.GL10#glClearStencil(int)}
     */
    public void glClearStencil(int s) {
        checkThread();
        mgl.glClearStencil(s);
        checkError();
    }

    
    /**
     * Gl client active texture.
     *
     * @param texture the texture
     * @see{@link javax.microedition.khronos.opengles.GL10#glClientActiveTexture(int)}
     */
    public void glClientActiveTexture(int texture) {
        checkThread();
        mgl.glClientActiveTexture(texture);
        checkError();
    }

   
    /**
     * Gl color4f.
     *
     * @param red the red
     * @param green the green
     * @param blue the blue
     * @param alpha the alpha
     * @see{@link javax.microedition.khronos.opengles.GL10#glClolor4f(float, float, float,float)}
     */
    public void glColor4f(float red, float green, float blue, float alpha) {
        checkThread();
        mgl.glColor4f(red, green, blue, alpha);
        checkError();
    }

    
    /**
     * Gl color4x.
     *
     * @param red the red
     * @param green the green
     * @param blue the blue
     * @param alpha the alpha
     * @see{@link javax.microedition.khronos.opengles.GL10#glColor4x(int, int, int, int)}
     */
    public void glColor4x(int red, int green, int blue, int alpha) {
        checkThread();
        mgl.glColor4x(red, green, blue, alpha);
        checkError();
    }

    
    /**
     * Gl color mask.
     *
     * @param red the red
     * @param green the green
     * @param blue the blue
     * @param alpha the alpha
     * @see{@link  javax.microedition.khronos.opengles.GL10#glColorMask(boolean, boolean, boolean, boolean)}
     */
    public void glColorMask(boolean red, boolean green, boolean blue,
            boolean alpha) {
        checkThread();
        mgl.glColorMask(red, green, blue, alpha);
        checkError();
    }

   
    /**
     * Gl color pointer.
     *
     * @param size the size
     * @param type the type
     * @param stride the stride
     * @param pointer the pointer
     * @see{@link  javax.microedition.khronos.opengles.GL10#glColorPointer(int, int, int, Buffer)}
     */
    public void glColorPointer(int size, int type, int stride, Buffer pointer) {
        checkThread();
        mgl.glColorPointer(size, type, stride, pointer);
        checkError();
    }

   
    /**
     * Gl compressed tex image2 d.
     *
     * @param target the target
     * @param level the level
     * @param internalformat the internalformat
     * @param width the width
     * @param height the height
     * @param border the border
     * @param imageSize the image size
     * @param data the data
     * @see{@link  javax.microedition.khronos.opengles.GL10#glCompressedTexImage2D(int, int, int, int, int, int, int, Buffer)}
     */
    public void glCompressedTexImage2D(int target, int level,
            int internalformat, int width, int height, int border,
            int imageSize, Buffer data) {
        checkThread();
        mgl.glCompressedTexImage2D(target, level, internalformat, width,
                height, border, imageSize, data);
        checkError();
    }

   
    /**
     * Gl compressed tex sub image2 d.
     *
     * @param target the target
     * @param level the level
     * @param xoffset the xoffset
     * @param yoffset the yoffset
     * @param width the width
     * @param height the height
     * @param format the format
     * @param imageSize the image size
     * @param data the data
     * @see{@link javax.microedition.khronos.opengles.GL10#glCompressedTexSubImage2D(int, int,int, int, int,int, int,int, java.nio.Buffer)}
     */
    public void glCompressedTexSubImage2D(int target, int level, int xoffset,
            int yoffset, int width, int height, int format, int imageSize,
            Buffer data) {
        checkThread();
        mgl.glCompressedTexSubImage2D(target, level, xoffset, yoffset, width,
                height, format, imageSize, data);
        checkError();
    }

   
    /**
     * Gl copy tex image2 d.
     *
     * @param target the target
     * @param level the level
     * @param internalformat the internalformat
     * @param x the x
     * @param y the y
     * @param width the width
     * @param height the height
     * @param border the border
     * @see{@link  javax.microedition.khronos.opengles.GL10#glCopyTexImage2D(int, int, int, int, int, int, int, int)}
     */
    public void glCopyTexImage2D(int target, int level, int internalformat,
            int x, int y, int width, int height, int border) {
        checkThread();
        mgl.glCopyTexImage2D(target, level, internalformat, x, y, width,
                height, border);
        checkError();
    }

   
    /**
     * Gl copy tex sub image2 d.
     *
     * @param target the target
     * @param level the level
     * @param xoffset the xoffset
     * @param yoffset the yoffset
     * @param x the x
     * @param y the y
     * @param width the width
     * @param height the height
     * @see{@link  javax.microedition.khronos.opengles.GL10#glCopyTexSubImage2D(int, int, int, int, int, int, int, int)}
     */
    public void glCopyTexSubImage2D(int target, int level, int xoffset,
            int yoffset, int x, int y, int width, int height) {
        checkThread();
        mgl.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width,
                height);
        checkError();
    }

   
    /**
     * Gl cull face.
     *
     * @param mode the mode
     * @see{@link  javax.microedition.khronos.opengles.GL10#glCullFace(int)}
     */
    public void glCullFace(int mode) {
        checkThread();
        mgl.glCullFace(mode);
        checkError();
    }

    
    /**
     * Gl delete textures.
     *
     * @param n the n
     * @param textures the textures
     * @param offset the offset
     * @see{@link {@link javax.microedition.khronos.opengles.GL10#glDeleteTextures(int, int[], int)}
     */
    public void glDeleteTextures(int n, int[] textures, int offset) {
        checkThread();
        mgl.glDeleteTextures(n, textures, offset);
        checkError();
    }

    
    /**
     * Gl delete textures.
     *
     * @param n the n
     * @param textures the textures
     * @see{@link javax.microedition.khronos.opengles.GL10#glDeleteTextures(int, java.nio.IntBuffer)}
     */
    public void glDeleteTextures(int n, IntBuffer textures) {
        checkThread();
        mgl.glDeleteTextures(n, textures);
        checkError();
    }

    
    /**
     * Gl depth func.
     *
     * @param func the func
     * @see{@link  javax.microedition.khronos.opengles.GL10#glDepthFunc(int)}
     */
    public void glDepthFunc(int func) {
        checkThread();
        mgl.glDepthFunc(func);
        checkError();
    }

   
    /**
     * Gl depth mask.
     *
     * @param flag the flag
     * @see{@link  javax.microedition.khronos.opengles.GL10#glDepthMask(boolean)}
     */
    public void glDepthMask(boolean flag) {
        checkThread();
        mgl.glDepthMask(flag);
        checkError();
    }

    
    /**
     * Gl depth rangef.
     *
     * @param near the near
     * @param far the far
     * @see{@link javax.microedition.khronos.opengles.GL10#glDepthRangef(float, float)}
     */
    public void glDepthRangef(float near, float far) {
        checkThread();
        mgl.glDepthRangef(near, far);
        checkError();
    }

    
    /**
     * Gl depth rangex.
     *
     * @param near the near
     * @param far the far
     * @see{@link  javax.microedition.khronos.opengles.GL10#glDepthRangex(int, int)}
     */
    public void glDepthRangex(int near, int far) {
        checkThread();
        mgl.glDepthRangex(near, far);
        checkError();
    }

    
    /**
     * Gl disable.
     *
     * @param cap the cap
     * @see{@link javax.microedition.khronos.opengles.GL10#glDisable(int)}
     */
    public void glDisable(int cap) {
        checkThread();
        mgl.glDisable(cap);
        checkError();
    }

   
    /**
     * Gl disable client state.
     *
     * @param array the array
     * @see{@link  javax.microedition.khronos.opengles.GL10#glDisableClientState(int)}
     */
    public void glDisableClientState(int array) {
        checkThread();
        mgl.glDisableClientState(array);
        checkError();
    }

    
    /**
     * Gl draw arrays.
     *
     * @param mode the mode
     * @param first the first
     * @param count the count
     * @see{@link  javax.microedition.khronos.opengles.GL10#glDrawArrays(int, int, int)}
     */
    public void glDrawArrays(int mode, int first, int count) {
        checkThread();
        mgl.glDrawArrays(mode, first, count);
        checkError();
    }

   
    /**
     * Gl draw elements.
     *
     * @param mode the mode
     * @param count the count
     * @param type the type
     * @param indices the indices
     * @see{@link  javax.microedition.khronos.opengles.GL10#glDrawElements(int, int, int, Buffer)}
     */
    public void glDrawElements(int mode, int count, int type, Buffer indices) {
        checkThread();
        mgl.glDrawElements(mode, count, type, indices);
        checkError();
    }

   
    /**
     * Gl enable.
     *
     * @param cap the cap
     * @see{@link  javax.microedition.khronos.opengles.GL10#glEnable(int)}
     */
    public void glEnable(int cap) {
        checkThread();
        mgl.glEnable(cap);
        checkError();
    }

   
    /**
     * Gl enable client state.
     *
     * @param array the array
     * @see{@link  javax.microedition.khronos.opengles.GL10#glEnableClientState(int)}
     */
    public void glEnableClientState(int array) {
        checkThread();
        mgl.glEnableClientState(array);
        checkError();
    }

   
    /**
     * Gl finish.
     *
     * @see{@link  javax.microedition.khronos.opengles.GL10#glFinish()}
     */
    public void glFinish() {
        checkThread();
        mgl.glFinish();
        checkError();
    }

    
    /**
     * Gl flush.
     *
     * @see{@link  javax.microedition.khronos.opengles.GL10#glFlush()}
     */
    public void glFlush() {
        checkThread();
        mgl.glFlush();
        checkError();
    }

   
    /**
     * Gl fogf.
     *
     * @param pname the pname
     * @param param the param
     * @see{@link  javax.microedition.khronos.opengles.GL10#glFogf(int, float)}
     */
    public void glFogf(int pname, float param) {
        checkThread();
        mgl.glFogf(pname, param);
        checkError();
    }

   
    /**
     * Gl fogfv.
     *
     * @param pname the pname
     * @param params the params
     * @param offset the offset
     * @see{@link  javax.microedition.khronos.opengles.GL10#glFogfv(int, FloatBufferfloat[],int)}
     */
    public void glFogfv(int pname, float[] params, int offset) {
        checkThread();
        mgl.glFogfv(pname, params, offset);
        checkError();
    }

   
    /**
     * Gl fogfv.
     *
     * @param pname the pname
     * @param params the params
     * @see{@link  javax.microedition.khronos.opengles.GL10#glFogfv(int, FloatBuffer)}
     */
    public void glFogfv(int pname, FloatBuffer params) {
        checkThread();
        mgl.glFogfv(pname, params);
        checkError();
    }

    
    /**
     * Gl fogx.
     *
     * @param pname the pname
     * @param param the param
     * @see{@link  javax.microedition.khronos.opengles.GL10#glFogx(int, int)}
     */
    public void glFogx(int pname, int param) {
        checkThread();
        mgl.glFogx(pname, param);
        checkError();
    }

   
    /**
     * Gl fogxv.
     *
     * @param pname the pname
     * @param params the params
     * @param offset the offset
     * @see{@link  javax.microedition.khronos.opengles.GL10#glFogxv(int, IntBufferint[], int)}
     */
    public void glFogxv(int pname, int[] params, int offset) {
        checkThread();
        mgl.glFogxv(pname, params, offset);
        checkError();
    }

    
    /**
     * Gl fogxv.
     *
     * @param pname the pname
     * @param params the params
     * @see{@link  javax.microedition.khronos.opengles.GL10#glFogxv(int, IntBuffer)}
     */
    public void glFogxv(int pname, IntBuffer params) {
        checkThread();
        mgl.glFogxv(pname, params);
        checkError();
    }

   
    /**
     * Gl front face.
     *
     * @param mode the mode
     * @see{@link  javax.microedition.khronos.opengles.GL10#glFrontFace(int)}
     */
    public void glFrontFace(int mode) {
        checkThread();
        mgl.glFrontFace(mode);
        checkError();
    }

    
    /**
     * Gl frustumf.
     *
     * @param left the left
     * @param right the right
     * @param bottom the bottom
     * @param top the top
     * @param near the near
     * @param far the far
     * @see{@link javax.microedition.khronos.opengles.GL10#glFrustumf(float, float, float, float, float, float)}
     */
    public void glFrustumf(float left, float right, float bottom, float top,
            float near, float far) {
        checkThread();
        mgl.glFrustumf(left, right, bottom, top, near, far);
        checkError();
    }

    
    /**
     * Gl frustumx.
     *
     * @param left the left
     * @param right the right
     * @param bottom the bottom
     * @param top the top
     * @param near the near
     * @param far the far
     * @see{@link  javax.microedition.khronos.opengles.GL10#glFrustumx(int, int, int, int, int, int)}
     */
    public void glFrustumx(int left, int right, int bottom, int top, int near,
            int far) {
        checkThread();
        mgl.glFrustumx(left, right, bottom, top, near, far);
        checkError();
    }

    
    /**
     * Gl gen textures.
     *
     * @param n the n
     * @param textures the textures
     * @param offset the offset
     * @see{@link  javax.microedition.khronos.opengles.GL10#glGenTextures(int, int[], int)}
     */
    public void glGenTextures(int n, int[] textures, int offset) {
        checkThread();
        mgl.glGenTextures(n, textures, offset);
        checkError();
    }

    
    /**
     * Gl gen textures.
     *
     * @param n the n
     * @param textures the textures
     * @see{@link javax.microedition.khronos.opengles.GL10#glGenTextures(int, IntBuffer)}
     */
    public void glGenTextures(int n, IntBuffer textures) {
        checkThread();
        mgl.glGenTextures(n, textures);
        checkError();
    }

    
    /**
     * Gl get error.
     *
     * @return the int
     * @see{@link javax.microedition.khronos.opengles.Gl10#glGetError()}
     */
    public int glGetError() {
        checkThread();
        int result = mgl.glGetError();
        return result;
    }

    
    /**
     * Gl get integerv.
     *
     * @param pname the pname
     * @param params the params
     * @param offset the offset
     * @see{@link  javax.microedition.khronos.opengles.GL10#glGetIntegerv(int, int[], int)}
     */
    public void glGetIntegerv(int pname, int[] params, int offset) {
        checkThread();
        mgl.glGetIntegerv(pname, params, offset);
        checkError();
    }

   
    /**
     * Gl get integerv.
     *
     * @param pname the pname
     * @param params the params
     * @see{@link javax.microedition.khronos.opengles.GL10#glGetIntegerv(int, IntBuffer)}
     */
    public void glGetIntegerv(int pname, IntBuffer params) {
        checkThread();
        mgl.glGetIntegerv(pname, params);
        checkError();
    }

    
    /**
     * Gl get string.
     *
     * @param name the name
     * @return the string
     * @see{@link  javax.microedition.khronos.opengles.GL10#glGetString(int)}
     */
    public String glGetString(int name) {
        checkThread();
        String result = mgl.glGetString(name);
        checkError();
        return result;
    }

   
    /**
     * Gl hint.
     *
     * @param target the target
     * @param mode the mode
     * @see{@link  javax.microedition.khronos.opengles.GL10#glHint(int, int)}
     */
    public void glHint(int target, int mode) {
        checkThread();
        mgl.glHint(target, mode);
        checkError();
    }

   
    /**
     * Gl light modelf.
     *
     * @param pname the pname
     * @param param the param
     * @see{@link  javax.microedition.khronos.opengles.GL10#glLightModelf(int, float)}
     */
    public void glLightModelf(int pname, float param) {
        checkThread();
        mgl.glLightModelf(pname, param);
        checkError();
    }

   
    /**
     * Gl light modelfv.
     *
     * @param pname the pname
     * @param params the params
     * @param offset the offset
     * @see{@link  javax.microedition.khronos.opengles.GL10#glLightModelfv(int,float[], int)}
     */
    public void glLightModelfv(int pname, float[] params, int offset) {
        checkThread();
        mgl.glLightModelfv(pname, params, offset);
        checkError();
    }

   
    /**
     * Gl light modelfv.
     *
     * @param pname the pname
     * @param params the params
     * @see{@link  javax.microedition.khronos.opengles.GL10#glLightModelfv(int, FloatBuffer)}
     */
    public void glLightModelfv(int pname, FloatBuffer params) {
        checkThread();
        mgl.glLightModelfv(pname, params);
        checkError();
    }

   
    /**
     * Gl light modelx.
     *
     * @param pname the pname
     * @param param the param
     * @see{@link  javax.microedition.khronos.opengles.GL10#glLightModelx(int, int)}
     */
    public void glLightModelx(int pname, int param) {
        checkThread();
        mgl.glLightModelx(pname, param);
        checkError();
    }

    
    /**
     * Gl light modelxv.
     *
     * @param pname the pname
     * @param params the params
     * @param offset the offset
     * @see{@link  javax.microedition.khronos.opengles.GL10#glLightModelxv(int, int[], int)}
     */
    public void glLightModelxv(int pname, int[] params, int offset) {
        checkThread();
        mgl.glLightModelxv(pname, params, offset);
        checkError();
    }

   
    /**
     * Gl light modelxv.
     *
     * @param pname the pname
     * @param params the params
     * @see{@link javax.microedition.khronos.opengles.GL10#glLightModelxv(int, IntBuffer)}
     */
    public void glLightModelxv(int pname, IntBuffer params) {
        checkThread();
        mgl.glLightModelxv(pname, params);
        checkError();
    }

    
    /**
     * Gl lightf.
     *
     * @param light the light
     * @param pname the pname
     * @param param the param
     * @see{@link javax.microedition.khronos.opengles.GL10#glLightf(int, int, float)}
     */
    public void glLightf(int light, int pname, float param) {
        checkThread();
        mgl.glLightf(light, pname, param);
        checkError();
    }

    
    /**
     * Gl lightfv.
     *
     * @param light the light
     * @param pname the pname
     * @param params the params
     * @param offset the offset
     * @see{@link javax.microedition.khronos.opengles.GL10#glLightfv(int, int, float[], int)}
     */
    public void glLightfv(int light, int pname, float[] params, int offset) {
        checkThread();
        mgl.glLightfv(light, pname, params, offset);
        checkError();
    }

   
    /**
     * Gl lightfv.
     *
     * @param light the light
     * @param pname the pname
     * @param params the params
     * @see{@link javax,microedition.khronos.opengles.Gl10#glLightfv(int, int, FloatBuffer)}
     */
    public void glLightfv(int light, int pname, FloatBuffer params) {
        checkThread();
        mgl.glLightfv(light, pname, params);
        checkError();
    }

    
    /**
     * Gl lightx.
     *
     * @param light the light
     * @param pname the pname
     * @param param the param
     * @see{@link  javax.microedition.khronos.opengles.GL10#glLightx(int, int, int)}
     */
    public void glLightx(int light, int pname, int param) {
        checkThread();
        mgl.glLightx(light, pname, param);
        checkError();
    }

   
    /**
     * Gl lightxv.
     *
     * @param light the light
     * @param pname the pname
     * @param params the params
     * @param offset the offset
     * @see{@link javax.microedition.khronos.opengles.GL10#glLigthxv(int,int,int[], int)}
     */
    public void glLightxv(int light, int pname, int[] params, int offset) {
        checkThread();
        mgl.glLightxv(light, pname, params, offset);
        checkError();
    }

    
    /**
     * Gl lightxv.
     *
     * @param light the light
     * @param pname the pname
     * @param params the params
     * @see{@link javax.microedition.khronos.opengles.GL10#glLightxv(int,int, IntBuffer)}
     */
    public void glLightxv(int light, int pname, IntBuffer params) {
        checkThread();
        mgl.glLightxv(light, pname, params);
        checkError();
    }

    
    /**
     * Gl line width.
     *
     * @param width the width
     * @see{@link javax.microedition.khronos.opengles.GL10#glLineWidth(float)}
     */
    public void glLineWidth(float width) {
        checkThread();
        mgl.glLineWidth(width);
        checkError();
    }

    
    /**
     * Gl line widthx.
     *
     * @param width the width
     * @see{@link javax.microedition.khronos.opengles.GL10#glLineWidthx(int)}
     */
    public void glLineWidthx(int width) {
        checkThread();
        mgl.glLineWidthx(width);
        checkError();
    }

    
    /**
     * Gl load identity.
     *
     * @see{@link javax.microedition.khronos.opengles.GL10#glLoadIdenity()}
     */
    public void glLoadIdentity() {
        checkThread();
        mgl.glLoadIdentity();
        checkError();
    }

    
    /**
     * Gl load matrixf.
     *
     * @param m the m
     * @param offset the offset
     * @see{@link  javax.microedition.khronos.opengles.GL10#glLoadMatrixf(float[], int)}
     */
    public void glLoadMatrixf(float[] m, int offset) {
        checkThread();
        mgl.glLoadMatrixf(m, offset);
        checkError();
    }

   
    /**
     * Gl load matrixf.
     *
     * @param m the m
     * @see{@link  javax.microedition.khronos.opengles.GL10#glLoadMatrixf(FloatBuffer)}
     */
    public void glLoadMatrixf(FloatBuffer m) {
        checkThread();
        mgl.glLoadMatrixf(m);
        checkError();
    }

    
    /**
     * Gl load matrixx.
     *
     * @param m the m
     * @param offset the offset
     * @see{@link javax.microedition.khronos.opengles.GL10#glLoadMatrixx(int[], int)}
     */
    public void glLoadMatrixx(int[] m, int offset) {
        checkThread();
        mgl.glLoadMatrixx(m, offset);
        checkError();
    }

    
    /**
     * Gl load matrixx.
     *
     * @param m the m
     * @see{@link  javax.microedition.khronos.opengles.GL10#glLoadMatrixx(IntBuffer)}
     */
    public void glLoadMatrixx(IntBuffer m) {
        checkThread();
        mgl.glLoadMatrixx(m);
        checkError();
    }

    
    /**
     * Gl logic op.
     *
     * @param opcode the opcode
     * @see{@link  javax.microedition.khronos.opengles.GL10#glLogicOp(int)}
     */
    public void glLogicOp(int opcode) {
        checkThread();
        mgl.glLogicOp(opcode);
        checkError();
    }

   
    /**
     * Gl materialf.
     *
     * @param face the face
     * @param pname the pname
     * @param param the param
     * @see{@link  javax.microedition.khronos.opengles.GL10#glMaterialf(int, int, float)}
     */
    public void glMaterialf(int face, int pname, float param) {
        checkThread();
        mgl.glMaterialf(face, pname, param);
        checkError();
    }

   
    /**
     * Gl materialfv.
     *
     * @param face the face
     * @param pname the pname
     * @param params the params
     * @param offset the offset
     * @see{@link javax.microedition.khronos.opengles.GL10#glMaterialfv(int, int,float[], int)}
     */
    public void glMaterialfv(int face, int pname, float[] params, int offset) {
        checkThread();
        mgl.glMaterialfv(face, pname, params, offset);
        checkError();
    }

   
    /**
     * Gl materialfv.
     *
     * @param face the face
     * @param pname the pname
     * @param params the params
     * @see{@link javax.microedition.khronos.opengles.GL10#glMaterialfv(int, int, FloatBuffer)}
     */
    public void glMaterialfv(int face, int pname, FloatBuffer params) {
        checkThread();
        mgl.glMaterialfv(face, pname, params);
        checkError();
    }

   
    /**
     * Gl materialx.
     *
     * @param face the face
     * @param pname the pname
     * @param param the param
     * @see{@link  javax.microedition.khronos.opengles.GL10#glMaterialx(int, int, int)}
     */
    public void glMaterialx(int face, int pname, int param) {
        checkThread();
        mgl.glMaterialx(face, pname, param);
        checkError();
    }

   
    /**
     * Gl materialxv.
     *
     * @param face the face
     * @param pname the pname
     * @param params the params
     * @param offset the offset
     * @see{@link javax.microedition.khronos.opengles.GL10#glMaterialxv(int, int, int[], int)}
     */
    public void glMaterialxv(int face, int pname, int[] params, int offset) {
        checkThread();
        mgl.glMaterialxv(face, pname, params, offset);
        checkError();
    }

   
    /**
     * Gl materialxv.
     *
     * @param face the face
     * @param pname the pname
     * @param params the params
     * @see{@link  javax.microedition.khronos.opengles.GL10#glMaterialxv(int, int, IntBuffer)}
     */
    public void glMaterialxv(int face, int pname, IntBuffer params) {
        checkThread();
        mgl.glMaterialxv(face, pname, params);
        checkError();
    }

   
    /**
     * Gl matrix mode.
     *
     * @param mode the mode
     * @see{@link  javax.microedition.khronos.opengles.GL10#glMatrixMode(int)}
     */
    public void glMatrixMode(int mode) {
        checkThread();
        mgl.glMatrixMode(mode);
        checkError();
    }

    
    /**
     * Gl mult matrixf.
     *
     * @param m the m
     * @param offset the offset
     * @see{@link javax.microedition.khronos.opengles.GL10#glMultMatrixf(float[], int)}
     */
    public void glMultMatrixf(float[] m, int offset) {
        checkThread();
        mgl.glMultMatrixf(m, offset);
        checkError();
    }

   
    /**
     * Gl mult matrixf.
     *
     * @param m the m
     * @see{@link javax.microedition.khronos.opengles.GL10#glMultMatrixf(FloatBuffer)}
     */
    public void glMultMatrixf(FloatBuffer m) {
        checkThread();
        mgl.glMultMatrixf(m);
        checkError();
    }

   
    /**
     * Gl mult matrixx.
     *
     * @param m the m
     * @param offset the offset
     * @see{@link  javax.microedition.khronos.opengles.GL10#glMultMatrixx(int[], int)}
     */
    public void glMultMatrixx(int[] m, int offset) {
        checkThread();
        mgl.glMultMatrixx(m, offset);
        checkError();
    }

    
    /**
     * Gl mult matrixx.
     *
     * @param m the m
     * @see{@link  javax.microedition.khronos.opengles.GL10#glMultMatrixx(IntBuffer)}
     */
    public void glMultMatrixx(IntBuffer m) {
        checkThread();
        mgl.glMultMatrixx(m);
        checkError();
    }

    
    /**
     * Gl multi tex coord4f.
     *
     * @param target the target
     * @param s the s
     * @param t the t
     * @param r the r
     * @param q the q
     * @see{@link  javax.microedition.khronos.opengles.GL10#glMultiTexCoord4f(int, float, float, float, float)}
     */
    public void glMultiTexCoord4f(int target,
            float s, float t, float r, float q) {
        checkThread();
        mgl.glMultiTexCoord4f(target, s, t, r, q);
        checkError();
    }

   
    /**
     * Gl multi tex coord4x.
     *
     * @param target the target
     * @param s the s
     * @param t the t
     * @param r the r
     * @param q the q
     * @see{@link  javax.microedition.khronos.opengles.GL10#glMultiTexCoord4x(int, int, int, int, int)}
     */
    public void glMultiTexCoord4x(int target, int s, int t, int r, int q) {
        checkThread();
        mgl.glMultiTexCoord4x(target, s, t, r, q);
        checkError();
    }

    
    /**
     * Gl normal3f.
     *
     * @param nx the nx
     * @param ny the ny
     * @param nz the nz
     * @see{@link  javax.microedition.khronos.opengles.GL10#glNormal3f(float, float, float)}
     */
    public void glNormal3f(float nx, float ny, float nz) {
        checkThread();
        mgl.glNormal3f(nx, ny, nz);
        checkError();
    }

   
    /**
     * Gl normal3x.
     *
     * @param nx the nx
     * @param ny the ny
     * @param nz the nz
     * @see{@link  javax.microedition.khronos.opengles.GL10#glNormal3x(int, int, int)}
     */
    public void glNormal3x(int nx, int ny, int nz) {
        checkThread();
        mgl.glNormal3x(nx, ny, nz);
        checkError();
    }

   
    /**
     * Gl normal pointer.
     *
     * @param type the type
     * @param stride the stride
     * @param pointer the pointer
     * @see{@link  javax.microedition.khronos.opengles.GL10#glNormalPointer(int, int, Buffer)}
     */
    public void glNormalPointer(int type, int stride, Buffer pointer) {
        checkThread();
        mgl.glNormalPointer(type, stride, pointer);
        checkError();
    }

   
    /**
     * Gl orthof.
     *
     * @param left the left
     * @param right the right
     * @param bottom the bottom
     * @param top the top
     * @param near the near
     * @param far the far
     * @see{@link  javax.microedition.khronos.opengles.GL10#glOrthof(float, float, float, float, float, float)}
     */
    public void glOrthof(float left, float right, float bottom, float top,
            float near, float far) {
        checkThread();
        mgl.glOrthof(left, right, bottom, top, near, far);
        checkError();
    }

   
    /**
     * Gl orthox.
     *
     * @param left the left
     * @param right the right
     * @param bottom the bottom
     * @param top the top
     * @param near the near
     * @param far the far
     * @see{@link javax.microedition.khronos.opengles.GL10#glOrthox(int, int, int, int, int, int)}
     */
    public void glOrthox(int left, int right, int bottom, int top, int near,
            int far) {
        checkThread();
        mgl.glOrthox(left, right, bottom, top, near, far);
        checkError();
    }

   
    /**
     * Gl pixel storei.
     *
     * @param pname the pname
     * @param param the param
     * @see{@link  javax.microedition.khronos.opengles.GL10#glPixelStorei(int, int)}
     */
    public void glPixelStorei(int pname, int param) {
        checkThread();
        mgl.glPixelStorei(pname, param);
        checkError();
    }

   
    /**
     * Gl point size.
     *
     * @param size the size
     * @see{@link  javax.microedition.khronos.opengles.GL10#glPointSize(float)}
     */
    public void glPointSize(float size) {
        checkThread();
        mgl.glPointSize(size);
        checkError();
    }

    
    /**
     * Gl point sizex.
     *
     * @param size the size
     * @see{@link  javax.microedition.khronos.opengles.GL10#glPointSizex(int)}
     */
    public void glPointSizex(int size) {
        checkThread();
        mgl.glPointSizex(size);
        checkError();
    }

    
    /**
     * Gl polygon offset.
     *
     * @param factor the factor
     * @param units the units
     * @see{@link javax.microedition.khronos.opengles.GL10#glPolygonOffset(float, float)}
     */
    public void glPolygonOffset(float factor, float units) {
        checkThread();
        mgl.glPolygonOffset(factor, units);
        checkError();
    }

    
    /**
     * Gl polygon offsetx.
     *
     * @param factor the factor
     * @param units the units
     * @see{@link  javax.microedition.khronos.opengles.GL10#glPolygonOffsetx(int, int)}
     */
    public void glPolygonOffsetx(int factor, int units) {
        checkThread();
        mgl.glPolygonOffsetx(factor, units);
        checkError();
    }

   
    /**
     * Gl pop matrix.
     *
     * @see{@link  javax.microedition.khronos.opengles.GL10#glPopMatrix()}
     */
    public void glPopMatrix() {
        checkThread();
        mgl.glPopMatrix();
        checkError();
    }

    
    /**
     * Gl push matrix.
     *
     * @see{@link  javax.microedition.khronos.opengles.GL10#glPushMatrix()}
     */
    public void glPushMatrix() {
        checkThread();
        mgl.glPushMatrix();
        checkError();
    }

   
    /**
     * Gl read pixels.
     *
     * @param x the x
     * @param y the y
     * @param width the width
     * @param height the height
     * @param format the format
     * @param type the type
     * @param pixels the pixels
     * @see{@link  javax.microedition.khronos.opengles.GL10#glReadPixels(int, int, int, int, int, int, Buffer)}
     */
    public void glReadPixels(int x, int y, int width, int height, int format,
            int type, Buffer pixels) {
        checkThread();
        mgl.glReadPixels(x, y, width, height, format, type, pixels);
        checkError();
    }

    
    /**
     * Gl rotatef.
     *
     * @param angle the angle
     * @param x the x
     * @param y the y
     * @param z the z
     * @see{@link  javax.microedition.khronos.opengles.GL10#glRotatef(float, float, float, float)}
     */
    public void glRotatef(float angle, float x, float y, float z) {
        checkThread();
        mgl.glRotatef(angle, x, y, z);
        checkError();
    }

   
    /**
     * Gl rotatex.
     *
     * @param angle the angle
     * @param x the x
     * @param y the y
     * @param z the z
     * @see{@link javax.microedition.khronos.opengles.GL10#glRotatex(int, int, int, int)}
     */
    public void glRotatex(int angle, int x, int y, int z) {
        checkThread();
        mgl.glRotatex(angle, x, y, z);
        checkError();
    }

    
    /**
     * Gl sample coverage.
     *
     * @param value the value
     * @param invert the invert
     * @see{@link javax.microedition.khronos.opengles.GL10#glSampleCoverage(float, boolean)}
     */
    public void glSampleCoverage(float value, boolean invert) {
        checkThread();
        mgl.glSampleCoverage(value, invert);
        checkError();
    }

    
    /**
     * Gl sample coveragex.
     *
     * @param value the value
     * @param invert the invert
     * @see{@link javax.microedition.khronos.opengles.GL10#glSampleCoveragex(int, boolean)}
     */
    public void glSampleCoveragex(int value, boolean invert) {
        checkThread();
        mgl.glSampleCoveragex(value, invert);
        checkError();
    }

    
    /**
     * Gl scalef.
     *
     * @param x the x
     * @param y the y
     * @param z the z
     * @see{@link  javax.microedition.khronos.opengles.GL10#glScalef(float, float, float)}
     */
    public void glScalef(float x, float y, float z) {
        checkThread();
        mgl.glScalef(x, y, z);
        checkError();
    }

    
    /**
     * Gl scalex.
     *
     * @param x the x
     * @param y the y
     * @param z the z
     * @see{@link  javax.microedition.khronos.opengles.GL10#glScalex(int, int, int)}
     */
    public void glScalex(int x, int y, int z) {
        checkThread();
        mgl.glScalex(x, y, z);
        checkError();
    }

    
    /**
     * Gl scissor.
     *
     * @param x the x
     * @param y the y
     * @param width the width
     * @param height the height
     * @see{@link  javax.microedition.khronos.opengles.GL10#glScissor(int, int, int, int)}
     */
    public void glScissor(int x, int y, int width, int height) {
        checkThread();
        mgl.glScissor(x, y, width, height);
        checkError();
    }

    
    /**
     * Gl shade model.
     *
     * @param mode the mode
     * @see{@link  javax.microedition.khronos.opengles.GL10#glShadeModel(int)}
     */
    public void glShadeModel(int mode) {
        checkThread();
        mgl.glShadeModel(mode);
        checkError();
    }

   
    /**
     * Gl stencil func.
     *
     * @param func the func
     * @param ref the ref
     * @param mask the mask
     * @see{@link  javax.microedition.khronos.opengles.GL10#glStencilFunc(int, int, int)}
     */
    public void glStencilFunc(int func, int ref, int mask) {
        checkThread();
        mgl.glStencilFunc(func, ref, mask);
        checkError();
    }

    
    /**
     * Gl stencil mask.
     *
     * @param mask the mask
     * @see{@link javax.microedition.khronos.opengles.GL10#glStencilMask(int)}
     */
    public void glStencilMask(int mask) {
        checkThread();
        mgl.glStencilMask(mask);
        checkError();
    }

   
    /**
     * Gl stencil op.
     *
     * @param fail the fail
     * @param zfail the zfail
     * @param zpass the zpass
     * @see{@link javax.microedition.khronos.opengles.GL10#glStencilOp(int, int, int)}
     */
    public void glStencilOp(int fail, int zfail, int zpass) {
        checkThread();
        mgl.glStencilOp(fail, zfail, zpass);
        checkError();
    }

    
    /**
     * Gl tex coord pointer.
     *
     * @param size the size
     * @param type the type
     * @param stride the stride
     * @param pointer the pointer
     * @see{@link javax.microedition.khronos.opengles.GL10#glTexCoordPointer(int, int, int, Buffer)}
     */
    public void glTexCoordPointer(int size, int type,
            int stride, Buffer pointer) {
        checkThread();
        mgl.glTexCoordPointer(size, type, stride, pointer);
        checkError();
    }

    
    /**
     * Gl tex envf.
     *
     * @param target the target
     * @param pname the pname
     * @param param the param
     * @see{@link  javax.microedition.khronos.opengles.GL10#glTexEnvf(int, int, float)}
     */
    public void glTexEnvf(int target, int pname, float param) {
        checkThread();
        mgl.glTexEnvf(target, pname, param);
        checkError();
    }

    
    /**
     * Gl tex envfv.
     *
     * @param target the target
     * @param pname the pname
     * @param params the params
     * @param offset the offset
     * @see{@link  javax.microedition.khronos.opengles.GL10#glTexEnvfv(int, int, float[], int)}
     */
    public void glTexEnvfv(int target, int pname, float[] params, int offset) {
        checkThread();
        mgl.glTexEnvfv(target, pname, params, offset);
        checkError();
    }

    
    /**
     * Gl tex envfv.
     *
     * @param target the target
     * @param pname the pname
     * @param params the params
     * @see{@link  javax.microedition.khronos.opengles.GL10#glTexEnvfv(int, int, FloatBuffer)}
     */
    public void glTexEnvfv(int target, int pname, FloatBuffer params) {
        checkThread();
        mgl.glTexEnvfv(target, pname, params);
        checkError();
    }

    
    /**
     * Gl tex envx.
     *
     * @param target the target
     * @param pname the pname
     * @param param the param
     * @see{@link  javax.microedition.khronos.opengles.GL10#glTexEnvx(int, int, int)}
     */
    public void glTexEnvx(int target, int pname, int param) {
        checkThread();
        mgl.glTexEnvx(target, pname, param);
        checkError();
    }

   
    /**
     * Gl tex envxv.
     *
     * @param target the target
     * @param pname the pname
     * @param params the params
     * @param offset the offset
     * @see{@link javax.microedition.khronos.opengles.GL10#glTexEnvxv(int, int, int[], int)}
     */
    public void glTexEnvxv(int target, int pname, int[] params, int offset) {
        checkThread();
        mgl.glTexEnvxv(target, pname, params, offset);
        checkError();
    }

    
    /**
     * Gl tex envxv.
     *
     * @param target the target
     * @param pname the pname
     * @param params the params
     * @see{@link  javax.microedition.khronos.opengles.GL10#glTexEnvxv(int, int, IntBuffer)}
     */
    public void glTexEnvxv(int target, int pname, IntBuffer params) {
        checkThread();
        mgl.glTexEnvxv(target, pname, params);
        checkError();
    }

    
    /**
     * Gl tex image2 d.
     *
     * @param target the target
     * @param level the level
     * @param internalformat the internalformat
     * @param width the width
     * @param height the height
     * @param border the border
     * @param format the format
     * @param type the type
     * @param pixels the pixels
     * @see{@link  javax.microedition.khronos.opengles.GL10#glTexImage2D(int, int, int, int, int, int, int, int, Buffer)}
     */
    public void glTexImage2D(int target, int level, int internalformat,
            int width, int height, int border, int format, int type,
            Buffer pixels) {
        checkThread();
        mgl.glTexImage2D(target, level, internalformat, width, height, border,
                format, type, pixels);
        checkError();
    }

    
    /**
     * Gl tex parameterf.
     *
     * @param target the target
     * @param pname the pname
     * @param param the param
     * @see{@link  javax.microedition.khronos.opengles.GL10#glTexParameterf(int, int, float)}
     */
    public void glTexParameterf(int target, int pname, float param) {
        checkThread();
        mgl.glTexParameterf(target, pname, param);
        checkError();
    }

    
    /**
     * Gl tex parameterx.
     *
     * @param target the target
     * @param pname the pname
     * @param param the param
     * @see{@link javax.microedition.khronos.opengles.GL10#glTexParameterx(int, int, int)}
     */
    public void glTexParameterx(int target, int pname, int param) {
        checkThread();
        mgl.glTexParameterx(target, pname, param);
        checkError();
    }

    
    /**
     * Gl tex parameteriv.
     *
     * @param target the target
     * @param pname the pname
     * @param params the params
     * @param offset the offset
     * @see{@link {@link javax.microedition.khronos.opengles.GL11#glTexParameteriv(int, int, int[], int)}
     */
    public void glTexParameteriv(int target, int pname, int[] params, int offset) {
        checkThread();
        mgl11.glTexParameteriv(target, pname, params, offset);
        checkError();
    }

    
    /**
     * Gl tex parameteriv.
     *
     * @param target the target
     * @param pname the pname
     * @param params the params
     * @see{@link  javax.microedition.khronos.opengles.GL11#glTexParameteriv(int, int, IntBuffer)}
     */
    public void glTexParameteriv(int target, int pname, IntBuffer params) {
        checkThread();
        mgl11.glTexParameteriv(target, pname, params);
        checkError();
    }

   
    /**
     * Gl tex sub image2 d.
     *
     * @param target the target
     * @param level the level
     * @param xoffset the xoffset
     * @param yoffset the yoffset
     * @param width the width
     * @param height the height
     * @param format the format
     * @param type the type
     * @param pixels the pixels
     * @see{@link  javax.microedition.khronos.opengles.GL10#glTexSubImage2D(int, int, int, int, int, int, int, int, Buffer)}
     */
    public void glTexSubImage2D(int target, int level, int xoffset,
            int yoffset, int width, int height, int format, int type,
            Buffer pixels) {
        checkThread();
        mgl.glTexSubImage2D(target, level, xoffset, yoffset, width, height,
                format, type, pixels);
        checkError();
    }

    
    /**
     * Gl translatef.
     *
     * @param x the x
     * @param y the y
     * @param z the z
     * @see{@link  javax.microedition.khronos.opengles.GL10#glTranslatef(float, float, float)}
     */
    public void glTranslatef(float x, float y, float z) {
        checkThread();
        mgl.glTranslatef(x, y, z);
        checkError();
    }

    
    /**
     * Gl translatex.
     *
     * @param x the x
     * @param y the y
     * @param z the z
     * @see{@link  javax.microedition.khronos.opengles.GL10#glTranslatex(int, int, int)}
     */
    public void glTranslatex(int x, int y, int z) {
        checkThread();
        mgl.glTranslatex(x, y, z);
        checkError();
    }

   
    /**
     * Gl vertex pointer.
     *
     * @param size the size
     * @param type the type
     * @param stride the stride
     * @param pointer the pointer
     * @see{@link javax.microedition.khronos.opengles.GL10#glVertexPointer(int, int, int, Buffer)}
     */
    public void glVertexPointer(int size, int type,
            int stride, Buffer pointer) {
        checkThread();
        mgl.glVertexPointer(size, type, stride, pointer);
        checkError();
    }

   
    /**
     * Gl viewport.
     *
     * @param x the x
     * @param y the y
     * @param width the width
     * @param height the height
     * @see{@link  javax.microedition.khronos.opengles.GL10#glViewport(int, int, int, int)}
     */
    public void glViewport(int x, int y, int width, int height) {
        checkThread();
        mgl.glViewport(x, y, width, height);
        checkError();
    }

   
    /**
     * Gl clip planef.
     *
     * @param plane the plane
     * @param equation the equation
     * @param offset the offset
     * @see{@link  javax.microedition.khronos.opengles.GL11#glClipPlanef(int, float[], int)}
     */
    public void glClipPlanef(int plane, float[] equation, int offset) {
        checkThread();
        mgl11.glClipPlanef(plane, equation, offset);
        checkError();
    }

   
    /**
     * Gl clip planef.
     *
     * @param plane the plane
     * @param equation the equation
     * @see{@link  javax.microedition.khronos.opengles.GL11#glClipPlanef(int, FloatBuffer)}
     */
    public void glClipPlanef(int plane, FloatBuffer equation) {
        checkThread();
        mgl11.glClipPlanef(plane, equation);
        checkError();
    }

   
    /**
     * Gl clip planex.
     *
     * @param plane the plane
     * @param equation the equation
     * @param offset the offset
     * @see{@link  javax.microedition.khronos.opengles.GL11#glClipPlanex(int, int[], int)}
     */
    public void glClipPlanex(int plane, int[] equation, int offset) {
        checkThread();
        mgl11.glClipPlanex(plane, equation, offset);
        checkError();
    }

    
    /**
     * Gl clip planex.
     *
     * @param plane the plane
     * @param equation the equation
     * @see{@link  javax.microedition.khronos.opengles.GL11#glClipPlanex(int, IntBuffer)}
     */
    public void glClipPlanex(int plane, IntBuffer equation) {
        checkThread();
        mgl11.glClipPlanex(plane, equation);
        checkError();
    }

    // Draw Texture Extension

    
    /**
     * Gl draw texf oes.
     *
     * @param x the x
     * @param y the y
     * @param z the z
     * @param width the width
     * @param height the height
     * @see{@link  javax.microedition.khronos.opengles.GL11Ext#glDrawTexfOES(float, float, float, float, float)}
     */
    public void glDrawTexfOES(float x, float y, float z,
        float width, float height) {
        checkThread();
        mgl11Ext.glDrawTexfOES(x, y, z, width, height);
        checkError();
    }

    
    /**
     * Gl draw texfv oes.
     *
     * @param coords the coords
     * @param offset the offset
     * @see{@link  javax.microedition.khronos.opengles.GL11Ext#glDrawTexfvOES(float[], int)}
     */
    public void glDrawTexfvOES(float[] coords, int offset) {
        checkThread();
        mgl11Ext.glDrawTexfvOES(coords, offset);
        checkError();
    }

   
    /**
     * Gl draw texfv oes.
     *
     * @param coords the coords
     * @see{@link  javax.microedition.khronos.opengles.GL11Ext#glDrawTexfvOES(FloatBuffer)}
     */
    public void glDrawTexfvOES(FloatBuffer coords) {
        checkThread();
        mgl11Ext.glDrawTexfvOES(coords);
        checkError();
    }

   
    /**
     * Gl draw texi oes.
     *
     * @param x the x
     * @param y the y
     * @param z the z
     * @param width the width
     * @param height the height
     * @see{@link javax.microedition.khronos.opengles.GL11Ext#glDrawTexiOES(int, int, int, int, int)}
     */
    public void glDrawTexiOES(int x, int y, int z, int width, int height) {
        checkThread();
        mgl11Ext.glDrawTexiOES(x, y, z, width, height);
        checkError();
    }

    
    /**
     * Gl draw texiv oes.
     *
     * @param coords the coords
     * @param offset the offset
     * @see{@link javax.microedition.khronos.opengles.GL11Ext#glDrawTexivOES(int[], int)}
     */
    public void glDrawTexivOES(int[] coords, int offset) {
        checkThread();
        mgl11Ext.glDrawTexivOES(coords, offset);
        checkError();
    }

   
    /**
     * Gl draw texiv oes.
     *
     * @param coords the coords
     * @see{@link  javax.microedition.khronos.opengles.GL11Ext#glDrawTexivOES(IntBuffer)}
     */
    public void glDrawTexivOES(IntBuffer coords) {
        checkThread();
        mgl11Ext.glDrawTexivOES(coords);
        checkError();
    }

    
    /**
     * Gl draw texs oes.
     *
     * @param x the x
     * @param y the y
     * @param z the z
     * @param width the width
     * @param height the height
     * @see{@link javax.microedition.khronos.opengles.GL11Ext#glDrawTexsOES(short, short, short, short, short)}
     */
    public void glDrawTexsOES(short x, short y, short z,
        short width, short height) {
        checkThread();
        mgl11Ext.glDrawTexsOES(x, y, z, width, height);
        checkError();
    }

    
    /**
     * Gl draw texsv oes.
     *
     * @param coords the coords
     * @param offset the offset
     * @see{@link javax.microedition.khronos.opengles.GL11Ext#glDrawTexsvOES(short[],int)}
     */
    public void glDrawTexsvOES(short[] coords, int offset) {
        checkThread();
        mgl11Ext.glDrawTexsvOES(coords, offset);
        checkError();
    }

   
    /**
     * Gl draw texsv oes.
     *
     * @param coords the coords
     * @see{@link  javax.microedition.khronos.opengles.GL11Ext#glDrawTexsvOES(ShortBuffer)}
     */
    public void glDrawTexsvOES(ShortBuffer coords) {
        checkThread();
        mgl11Ext.glDrawTexsvOES(coords);
        checkError();
    }

    
    /**
     * Gl draw texx oes.
     *
     * @param x the x
     * @param y the y
     * @param z the z
     * @param width the width
     * @param height the height
     * @see{@link  javax.microedition.khronos.opengles.GL11Ext#glDrawTexxOES(int, int, int, int, int)}
     */
    public void glDrawTexxOES(int x, int y, int z, int width, int height) {
        checkThread();
        mgl11Ext.glDrawTexxOES(x, y, z, width, height);
        checkError();
    }

    
    /**
     * Gl draw texxv oes.
     *
     * @param coords the coords
     * @param offset the offset
     * @see{@link  javax.microedition.khronos.opengles.GL11Ext#glDrawTexxvOES(int[], int)}
     */
    public void glDrawTexxvOES(int[] coords, int offset) {
        checkThread();
        mgl11Ext.glDrawTexxvOES(coords, offset);
        checkError();
    }

    
    /**
     * Gl draw texxv oes.
     *
     * @param coords the coords
     * @see{@link  javax.microedition.khronos.opengles.GL11Ext#glDrawTexxvOES(IntBuffer)}
     */
    public void glDrawTexxvOES(IntBuffer coords) {
        checkThread();
        mgl11Ext.glDrawTexxvOES(coords);
        checkError();
    }

   
    /**
     * Gl query matrixx oes.
     *
     * @param mantissa the mantissa
     * @param mantissaOffset the mantissa offset
     * @param exponent the exponent
     * @param exponentOffset the exponent offset
     * @return the int
     * @see{@link javax.microedition.khronos.opengles.GL10Ext#glQueryMatrixxOES(int[], int, int[], int)}
     */
    public int glQueryMatrixxOES(int[] mantissa, int mantissaOffset,
        int[] exponent, int exponentOffset) {
        checkThread();
        int valid = mgl10Ext.glQueryMatrixxOES(mantissa, mantissaOffset,
            exponent, exponentOffset);
        checkError();
        return valid;
    }

   
    /**
     * Gl query matrixx oes.
     *
     * @param mantissa the mantissa
     * @param exponent the exponent
     * @return the int
     * @see{@link javax.microedition.khronos.opengles.GL10Ext#glQueryMatrixxOES(IntBuffer, IntBuffer)}
     */
    public int glQueryMatrixxOES(IntBuffer mantissa, IntBuffer exponent) {
        checkThread();
        int valid = mgl10Ext.glQueryMatrixxOES(mantissa, exponent);
        checkError();
        return valid;
    }

    
    /**
     * Gl bind buffer.
     *
     * @param target the target
     * @param buffer the buffer
     * @see{@link  javax.microedition.khronos.opengles.GL11#glBindBuffer(int, int)}
     */
    public void glBindBuffer(int target, int buffer) {
        checkThread();
        mgl11.glBindBuffer(target, buffer);
        checkError();
    }

    
    /**
     * Gl buffer data.
     *
     * @param target the target
     * @param size the size
     * @param data the data
     * @param usage the usage
     * @see{@link  javax.microedition.khronos.opengles.GL11#glBufferData(int, int, Buffer, int)}
     */
    public void glBufferData(int target, int size, Buffer data, int usage) {
        checkThread();
        mgl11.glBufferData(target, size, data, usage);
        checkError();
    }

    
    /**
     * Gl buffer sub data.
     *
     * @param target the target
     * @param offset the offset
     * @param size the size
     * @param data the data
     * @see{@link javax.microedition.khronos.opengles.GL11#glBufferSubData(int, int, int, Buffer)}
     */
    public void glBufferSubData(int target, int offset, int size, Buffer data) {
        checkThread();
        mgl11.glBufferSubData(target, offset, size, data);
        checkError();
    }

   
    /**
     * Gl color4ub.
     *
     * @param red the red
     * @param green the green
     * @param blue the blue
     * @param alpha the alpha
     * @see{@link  javax.microedition.khronos.opengles.GL11#glColor4ub(byte, byte, byte, byte)}
     */
    public void glColor4ub(byte red, byte green, byte blue, byte alpha) {
        checkThread();
        mgl11.glColor4ub(red, green, blue, alpha);
        checkError();    }

   
    /**
     * Gl color pointer.
     *
     * @param size the size
     * @param type the type
     * @param stride the stride
     * @param offset the offset
     * @see{@link javax.microedition.khronos.opengles.GL11#glColorPointer(int, int, int, Buffer)}
     */
    public void glColorPointer(int size, int type, int stride, int offset) {
        checkThread();
        mgl11.glColorPointer(size, type, stride, offset);
        checkError();
    }

    
    /**
     * Gl delete buffers.
     *
     * @param n the n
     * @param buffers the buffers
     * @param offset the offset
     * @see{@link javax.microedition.khronos.opengles.GL11#glDeleteBuffers(int, int[], int)}
     */
    public void glDeleteBuffers(int n, int[] buffers, int offset) {
        checkThread();
        mgl11.glDeleteBuffers(n, buffers, offset);
        checkError();
    }

    
    /**
     * Gl delete buffers.
     *
     * @param n the n
     * @param buffers the buffers
     * @see{@link  javax.microedition.khronos.opengles.GL11#glDeleteBuffers(int, IntBuffer)}
     */
    public void glDeleteBuffers(int n, IntBuffer buffers) {
        checkThread();
        mgl11.glDeleteBuffers(n, buffers);
        checkError();
    }

    
    /**
     * Gl draw elements.
     *
     * @param mode the mode
     * @param count the count
     * @param type the type
     * @param offset the offset
     * @see{@link  javax.microedition.khronos.opengles.GL11#glDrawElements(int, int, int, int)}
     */
    public void glDrawElements(int mode, int count, int type, int offset) {
        checkThread();
        mgl11.glDrawElements(mode, count, type, offset);
        checkError();
    }

   
    /**
     * Gl gen buffers.
     *
     * @param n the n
     * @param buffers the buffers
     * @param offset the offset
     * @see{@link  javax.microedition.khronos.opengles.GL11#glGenBuffers(int, int[], int)}
     */
    public void glGenBuffers(int n, int[] buffers, int offset) {
        checkThread();
        mgl11.glGenBuffers(n, buffers, offset);
        checkError();
    }

    
    /**
     * Gl gen buffers.
     *
     * @param n the n
     * @param buffers the buffers
     * @see{@link  javax.microedition.khronos.opengles.GL11#glGenBuffers(int, IntBuffer)}
     */
    public void glGenBuffers(int n, IntBuffer buffers) {
        checkThread();
        mgl11.glGenBuffers(n, buffers);
        checkError();
    }

    
    /**
     * Gl get booleanv.
     *
     * @param pname the pname
     * @param params the params
     * @param offset the offset
     * @see{@link javax.microedition.khronos.opengles.GL11#glGetBooleanv(int, boolean[], int)}
     */
    public void glGetBooleanv(int pname, boolean[] params, int offset) {
        checkThread();
        mgl11.glGetBooleanv(pname, params, offset);
        checkError();
    }

   
    /**
     * Gl get booleanv.
     *
     * @param pname the pname
     * @param params the params
     * @see{@link  javax.microedition.khronos.opengles.GL11#glGetBooleanv(int, IntBuffer)}
     */
    public void glGetBooleanv(int pname, IntBuffer params) {
        checkThread();
        mgl11.glGetBooleanv(pname, params);
        checkError();
    }

    
    /**
     * Gl get buffer parameteriv.
     *
     * @param target the target
     * @param pname the pname
     * @param params the params
     * @param offset the offset
     * @see{@link javax.microedition.khronos.opengles.GL11#glGetBufferParameteriv(int, int, int[], int)}
     */
    public void glGetBufferParameteriv(int target, int pname, int[] params,
            int offset) {
        checkThread();
        mgl11.glGetBufferParameteriv(target, pname, params, offset);
        checkError();
    }

    
    /**
     * Gl get buffer parameteriv.
     *
     * @param target the target
     * @param pname the pname
     * @param params the params
     * @see{@link javax.microedition.khronos.opengles.GL11#glGetBufferParameteriv(int, int, IntBuffer)}
     */
    public void glGetBufferParameteriv(int target, int pname, IntBuffer params) {
        checkThread();
        mgl11.glGetBufferParameteriv(target, pname, params);
        checkError();
    }

    
    /**
     * Gl get clip planef.
     *
     * @param pname the pname
     * @param eqn the eqn
     * @param offset the offset
     * @see{@link javax.microedition.khronos.opengles.GL11#glClipPlanef(int, float[], int)}
     */
    public void glGetClipPlanef(int pname, float[] eqn, int offset) {
        checkThread();
        mgl11.glGetClipPlanef(pname, eqn, offset);
        checkError();
    }

    /**
     * Gl get clip planef.
     *
     * @param pname the pname
     * @param eqn the eqn
     * @see{@link  javax.microedition.khronos.opengles.GL11#glGetClipPlanef(int, FloatBuffer)}
     */
    public void glGetClipPlanef(int pname, FloatBuffer eqn) {
        checkThread();
        mgl11.glGetClipPlanef(pname, eqn);
        checkError();
    }

  
    /**
     * Gl get clip planex.
     *
     * @param pname the pname
     * @param eqn the eqn
     * @param offset the offset
     * @see{@link  javax.microedition.khronos.opengles.GL11#glGetClipPlanex(int, int[], int)}
     */
    public void glGetClipPlanex(int pname, int[] eqn, int offset) {
        checkThread();
        mgl11.glGetClipPlanex(pname, eqn, offset);
        checkError();
    }

   
    /**
     * Gl get clip planex.
     *
     * @param pname the pname
     * @param eqn the eqn
     * @see{@link javax.microedition.khronos.opengles.GL11#glGetClipPlanex(int, Buffer)}
     */
    public void glGetClipPlanex(int pname, IntBuffer eqn) {
        checkThread();
        mgl11.glGetClipPlanex(pname, eqn);
        checkError();
    }

   
    /**
     * Gl get fixedv.
     *
     * @param pname the pname
     * @param params the params
     * @param offset the offset
     * @see{@link javax.microedition.khronos.opengles.Gl11#glGetFixedv(int, int[], int)
     */
    public void glGetFixedv(int pname, int[] params, int offset) {
        checkThread();
        mgl11.glGetFixedv(pname, params, offset);
        checkError();
    }

    
    /**
     * Gl get fixedv.
     *
     * @param pname the pname
     * @param params the params
     * @see{@link javax.microedition.khronos.opengles.GL11#glGetFixedv(int, IntBuffer)}
     */
    public void glGetFixedv(int pname, IntBuffer params) {
        checkThread();
        mgl11.glGetFixedv(pname, params);
        checkError();
    }

    
    /**
     * Gl get floatv.
     *
     * @param pname the pname
     * @param params the params
     * @param offset the offset
     * @see{@link javax.microedition.khronos.opengles.GL11#glGetFloatv(int, float[],int)}
     */
    public void glGetFloatv(int pname, float[] params, int offset) {
        checkThread();
        mgl11.glGetFloatv(pname, params, offset);
        checkError();
    }

    
    /**
     * Gl get floatv.
     *
     * @param pname the pname
     * @param params the params
     * @see{@link javax.microedition.khronos.opengles.GL11#glGetFloatv(int, FloatBuffe)}
     */
    public void glGetFloatv(int pname, FloatBuffer params) {
        checkThread();
        mgl11.glGetFloatv(pname, params);
        checkError();
    }

    
    /**
     * Gl get lightfv.
     *
     * @param light the light
     * @param pname the pname
     * @param params the params
     * @param offset the offset
     * @see{@link  javax.microedition.khronos.opengles.GL11#glGetLightfv(int, int, float[], int)}
     */
    public void glGetLightfv(int light, int pname, float[] params, int offset) {
        checkThread();
        mgl11.glGetLightfv(light, pname, params, offset);
        checkError();
    }

    
    /**
     * Gl get lightfv.
     *
     * @param light the light
     * @param pname the pname
     * @param params the params
     * @see{@link javax.microedition.khronos.opengles.GL11#glGetLightfv(int, int, FloatBuffer)}
     */
    public void glGetLightfv(int light, int pname, FloatBuffer params) {
        checkThread();
        mgl11.glGetLightfv(light, pname, params);
        checkError();
    }

    
    /**
     * Gl get lightxv.
     *
     * @param light the light
     * @param pname the pname
     * @param params the params
     * @param offset the offset
     * @see{@link javax.microedition.khronos.opengles.GL11#glGetLightxv(int, int, int[], int)}
     */
    public void glGetLightxv(int light, int pname, int[] params, int offset) {
        checkThread();
        mgl11.glGetLightxv(light, pname, params, offset);
        checkError();
    }

   
    /**
     * Gl get lightxv.
     *
     * @param light the light
     * @param pname the pname
     * @param params the params
     * @see{@link javax.microedition.khronos.opengles.GL11#glGetLightxv(int, int, IntBuffer)}
     */
    public void glGetLightxv(int light, int pname, IntBuffer params) {
        checkThread();
        mgl11.glGetLightxv(light, pname, params);
        checkError();
    }

    
    /**
     * Gl get materialfv.
     *
     * @param face the face
     * @param pname the pname
     * @param params the params
     * @param offset the offset
     * @see{@link javax.microedition.khronos.opengles.GL11#glGetMaterialfv(int, int, float[], int)}
     */
    public void glGetMaterialfv(int face, int pname, float[] params, int offset) {
        checkThread();
        mgl11.glGetMaterialfv(face, pname, params, offset);
        checkError();
    }

    
    /**
     * Gl get materialfv.
     *
     * @param face the face
     * @param pname the pname
     * @param params the params
     * @see{@link javax.microedition.khronos.opengles.Gl11#glGetMaterialfv(int, int,FloatBuffer)}
     */
    public void glGetMaterialfv(int face, int pname, FloatBuffer params) {
        checkThread();
        mgl11.glGetMaterialfv(face, pname, params);
        checkError();
    }

    
    /**
     * Gl get materialxv.
     *
     * @param face the face
     * @param pname the pname
     * @param params the params
     * @param offset the offset
     * @see{@link javax.microedition.khronos.opengles.GL11#glGetMaterialxv(int, int, int[], int)}
     */
    public void glGetMaterialxv(int face, int pname, int[] params, int offset) {
        checkThread();
        mgl11.glGetMaterialxv(face, pname, params, offset);
        checkError();
    }

    
    /**
     * Gl get materialxv.
     *
     * @param face the face
     * @param pname the pname
     * @param params the params
     * @see{@link javax.microedition.khronos.opengles.GL11#glGetMaterialxv(int, int, IntBuffer)}
     */
    public void glGetMaterialxv(int face, int pname, IntBuffer params) {
        checkThread();
        mgl11.glGetMaterialxv(face, pname, params);
        checkError();
    }

    
    /**
     * Gl get pointerv.
     *
     * @param pname the pname
     * @param params the params
     * @see{@link  javax.microedition.khronos.opengles.GL11#glGetPointerv(int, Buffer[])}
     */
    public void glGetPointerv(int pname, Buffer[] params) {
        checkThread();
        mgl11.glGetPointerv(pname, params);
        checkError();
    }

    
    /**
     * Gl get tex enviv.
     *
     * @param env the env
     * @param pname the pname
     * @param params the params
     * @param offset the offset
     * @see{@link javax.microedition.khronos.opegles.GL11#glGetTexenviv(int, int, int[], int)}
     */
    public void glGetTexEnviv(int env, int pname, int[] params, int offset) {
        checkThread();
        mgl11.glGetTexEnviv(env, pname, params, offset);
        checkError();
    }

    
    /**
     * Gl get tex enviv.
     *
     * @param env the env
     * @param pname the pname
     * @param params the params
     * @see{@link javax.microedition.khronos.opoengles.GL11#glGetTexEnviv(int, int, IntBuffer)}
     */
    public void glGetTexEnviv(int env, int pname, IntBuffer params) {
        checkThread();
        mgl11.glGetTexEnviv(env, pname, params);
        checkError();
    }

    
    /**
     * Gl get tex envxv.
     *
     * @param env the env
     * @param pname the pname
     * @param params the params
     * @param offset the offset
     * @see{@link javax.microedition.khronos.opengles.Gl11#glGetTexEnvxv(int, int, int[], int)}
     */
    public void glGetTexEnvxv(int env, int pname, int[] params, int offset) {
        checkThread();
        mgl11.glGetTexEnvxv(env, pname, params, offset);
        checkError();
    }

    /**
     * Gl get tex envxv.
     *
     * @param env the env
     * @param pname the pname
     * @param params the params
     * @see{@link javax.microedition.khronos.opengles.GL11#glGetTexEnvxv(int, int, IntBuffer)}
     */
    public void glGetTexEnvxv(int env, int pname, IntBuffer params) {
        checkThread();
        mgl11.glGetTexEnvxv(env, pname, params);
        checkError();
    }

    /**
     * Gl get tex parameterfv.
     *
     * @param target the target
     * @param pname the pname
     * @param params the params
     * @param offset the offset
     * @see{@link javax.microedition.khronos.opengles.GL11#glGetTexParameterfv(int, int, float[], int)}
     */
    public void glGetTexParameterfv(int target, int pname, float[] params,
            int offset) {
        checkThread();
        mgl11.glGetTexParameterfv(target, pname, params, offset);
        checkError();
    }

    
    /**
     * Gl get tex parameterfv.
     *
     * @param target the target
     * @param pname the pname
     * @param params the params
     * @see{@link  javax.microedition.khronos.opengles.GL11#glGetTexParameterfv(int, int, FloatBuffer)}
     */
    public void glGetTexParameterfv(int target, int pname, FloatBuffer params) {
        checkThread();
        mgl11.glGetTexParameterfv(target, pname, params);
        checkError();
    }

    
    /**
     * Gl get tex parameteriv.
     *
     * @param target the target
     * @param pname the pname
     * @param params the params
     * @param offset the offset
     * @see{@link javax.microedition.khronos.opengles.GL11#glGetTexParameteriv(int, int, int[], int)}
     */
    public void glGetTexParameteriv(int target, int pname, int[] params,
            int offset) {
        checkThread();
        mgl11.glGetTexParameteriv(target, pname, params, offset);
        checkError();
    }

   
    /**
     * Gl get tex parameteriv.
     *
     * @param target the target
     * @param pname the pname
     * @param params the params
     * @see{@link  javax.microedition.khronos.opengles.GL11#glGetTexParameteriv(int, int, IntBuffer)}
     */
    public void glGetTexParameteriv(int target, int pname, IntBuffer params) {
        checkThread();
        mgl11.glGetTexParameteriv(target, pname, params);
        checkError();
    }

   
    /**
     * Gl get tex parameterxv.
     *
     * @param target the target
     * @param pname the pname
     * @param params the params
     * @param offset the offset
     * @see{@link javax.microedition.khronos.opengles.GL11#glGetTexParameterxv(int, int, int[], int)}
     */
    public void glGetTexParameterxv(int target, int pname, int[] params,
            int offset) {
        checkThread();
        mgl11.glGetTexParameterxv(target, pname, params, offset);
        checkError();
    }

    
    /**
     * Gl get tex parameterxv.
     *
     * @param target the target
     * @param pname the pname
     * @param params the params
     * @see{@link javax.microedition.khronos.opengles.GL11#glGetTexParameterxv(int, int, IntBuffer)}
     */
    public void glGetTexParameterxv(int target, int pname, IntBuffer params) {
        checkThread();
        mgl11.glGetTexParameterxv(target, pname, params);
        checkError();
    }

   
    /**
     * Gl is buffer.
     *
     * @param buffer the buffer
     * @return true, if successful
     * @see{@link  javax.microedition.khronos.opengles.GL11#glIsBuffer(int)}
     */
    public boolean glIsBuffer(int buffer) {
        checkThread();
        boolean valid = mgl11.glIsBuffer(buffer);
        checkError();
        return valid;
    }

    
    /**
     * Gl is enabled.
     *
     * @param cap the cap
     * @return true, if successful
     * @see{@link  javax.microedition.khronos.opengles.GL11#glIsEnabled(int)}
     */
    public boolean glIsEnabled(int cap) {
        checkThread();
        boolean valid = mgl11.glIsEnabled(cap);
        checkError();
        return valid;
    }

    
    /**
     * Gl is texture.
     *
     * @param texture the texture
     * @return true, if successful
     * @see{@link  javax.microedition.khronos.opengles.GL11#glIsTexture(int)}
     */
    public boolean glIsTexture(int texture) {
        checkThread();
        boolean valid = mgl11.glIsTexture(texture);
        checkError();
        return valid;
    }

    
    /**
     * Gl normal pointer.
     *
     * @param type the type
     * @param stride the stride
     * @param offset the offset
     * @see{@link  javax.microedition.khronos.opengles.GL11#glNormalPointer(int, int, int)}
     */
    public void glNormalPointer(int type, int stride, int offset) {
        checkThread();
        mgl11.glNormalPointer(type, stride, offset);
        checkError();
    }

    
    /**
     * Gl point parameterf.
     *
     * @param pname the pname
     * @param param the param
     * @see{@link  javax.microedition.khronos.opengles.GL11#glPointParameterf(int, float)}
     */
    public void glPointParameterf(int pname, float param) {
        checkThread();
        mgl11.glPointParameterf(pname, param);
        checkError();
    }

    /**
     * Gl point parameterfv.
     *
     * @param pname the pname
     * @param params the params
     * @param offset the offset
     * @see{@link javax.microedition.khronos.opengles.GL11#glPointParameterfv(int, float[], int)}
     */
    public void glPointParameterfv(int pname, float[] params, int offset) {
        checkThread();
        mgl11.glPointParameterfv(pname, params, offset);
        checkError();
    }

    
    /**
     * Gl point parameterfv.
     *
     * @param pname the pname
     * @param params the params
     * @see{@link javax.microedition.khronos.opengles.Gl11#glPointParameterfv(int, FloatBuffer)}
     */
    public void glPointParameterfv(int pname, FloatBuffer params) {
        checkThread();
        mgl11.glPointParameterfv(pname, params);
        checkError();
    }

   
    /**
     * Gl point parameterx.
     *
     * @param pname the pname
     * @param param the param
     * @see{@link javax.microedition.khronos.opengles.GL11#glPointParamterx(int, int)}
     */
    public void glPointParameterx(int pname, int param) {
        checkThread();
        mgl11.glPointParameterx(pname, param);
        checkError();
    }

   
    /**
     * Gl point parameterxv.
     *
     * @param pname the pname
     * @param params the params
     * @param offset the offset
     * @see{@link javax.microedition.khronos.opengles.GL11#glPointParameterxv(int, int[], int)}
     */
    public void glPointParameterxv(int pname, int[] params, int offset) {
        checkThread();
        mgl11.glPointParameterxv(pname, params, offset);
        checkError();
    }

    
    /**
     * Gl point parameterxv.
     *
     * @param pname the pname
     * @param params the params
     * @see{@link javax.microedition.khronos.opengles.Gl11#glPointParameterxv(int, IntBuffer)}
     */
    public void glPointParameterxv(int pname, IntBuffer params) {
        checkThread();
        mgl11.glPointParameterxv(pname, params);
        checkError();
    }

    
    /**
     * Gl point size pointer oes.
     *
     * @param type the type
     * @param stride the stride
     * @param pointer the pointer
     * @see{@link javax.microedition.khronos.opengler.Gl11#glPointSizePointerOES(int, int, Buffer)}
     */
    public void glPointSizePointerOES(int type, int stride, Buffer pointer) {
        checkThread();
        mgl11.glPointSizePointerOES(type, stride, pointer);
        checkError();
    }

    
    /**
     * Gl tex coord pointer.
     *
     * @param size the size
     * @param type the type
     * @param stride the stride
     * @param offset the offset
     * @see{@link {@link javax.microedition.khronos.opengles.GL11#glTexCoordPointer(int, int, int,int)}
     */
    public void glTexCoordPointer(int size, int type, int stride, int offset) {
        checkThread();
        mgl11.glTexCoordPointer(size, type, stride, offset);
        checkError();
    }

   
    /**
     * Gl tex envi.
     *
     * @param target the target
     * @param pname the pname
     * @param param the param
     * @see{@link javax.microedition.khronos.opengles.GL11#glTexEnvi(int, int, int)}
     */
    public void glTexEnvi(int target, int pname, int param) {
        checkThread();
        mgl11.glTexEnvi(target, pname, param);
        checkError();
    }

   
    /**
     * Gl tex enviv.
     *
     * @param target the target
     * @param pname the pname
     * @param params the params
     * @param offset the offset
     * @see{@link javax.microedition.khronos.opengles.GL11#glTeEnviv(int, int, int[], int)}
     */
    public void glTexEnviv(int target, int pname, int[] params, int offset) {
        checkThread();
        mgl11.glTexEnviv(target, pname, params, offset);
        checkError();
    }

   
    /**
     * Gl tex enviv.
     *
     * @param target the target
     * @param pname the pname
     * @param params the params
     * @see{@link javax.microedition.khronos.opengles.GL11#glTexenviv(int, int, IntBuffer)}
     */
    public void glTexEnviv(int target, int pname, IntBuffer params) {
        checkThread();
        mgl11.glTexEnviv(target, pname, params);
        checkError();
    }

   
    /**
     * Gl tex parameterfv.
     *
     * @param target the target
     * @param pname the pname
     * @param params the params
     * @param offset the offset
     * @see{@link javax.microedition.khronos.opengles.GL11#glTexParameterfv(int, int, float[], int)}
     */
    public void glTexParameterfv(int target, int pname, float[] params,
            int offset) {
        checkThread();
        mgl11.glTexParameterfv(target, pname, params, offset);
        checkError();
    }

   
    /**
     * Gl tex parameterfv.
     *
     * @param target the target
     * @param pname the pname
     * @param params the params
     * @see{@link javax.microedition.khronos.opengles.GL11#glTexParameterfv(int, int, FloatBuffer)}
     */
    public void glTexParameterfv(int target, int pname, FloatBuffer params) {
        checkThread();
        mgl11.glTexParameterfv(target, pname, params);
        checkError();
    }

    
    /**
     * Gl tex parameteri.
     *
     * @param target the target
     * @param pname the pname
     * @param param the param
     * @see{@link javax.microedition.khronos.opengles.GL11glTexParamteri(int, int, int)}
     */
    public void glTexParameteri(int target, int pname, int param) {
        checkThread();
        mgl11.glTexParameteri(target, pname, param);
        checkError();
    }

   
    /**
     * Gl tex parameterxv.
     *
     * @param target the target
     * @param pname the pname
     * @param params the params
     * @param offset the offset
     * @see{@link  javax.microedition.khronos.opengles.GL11#glTexParameterxv(int, int, int[], int)}
     */
    public void glTexParameterxv(int target, int pname, int[] params, int offset) {
        checkThread();
        mgl11.glTexParameterxv(target, pname, params, offset);
        checkError();
    }

    
    /**
     * *.
     *
     * @param target the target
     * @param pname the pname
     * @param params the params
     * @see{@link javax.microedition.khronos.opengles.GL11#glTexParamterxv(int, int, java.nio.IntBuffer)}
     */
    public void glTexParameterxv(int target, int pname, IntBuffer params) {
        checkThread();
        mgl11.glTexParameterxv(target, pname, params);
        checkError();
    }

    
    /**
     * Gl vertex pointer.
     *
     * @param size the size
     * @param type the type
     * @param stride the stride
     * @param offset the offset
     * @see{@link  javax.microedition.khronos.opengles.GL11#glVertexPointer(int, int, int, int)}
     */
    public void glVertexPointer(int size, int type, int stride, int offset) {
        checkThread();
        mgl11.glVertexPointer(size, type, stride, offset);
        checkError();
    }

   
    /**
     * Gl current palette matrix oes.
     *
     * @param matrixpaletteindex the matrixpaletteindex
     * @see{@link javax.microedition.khronos.opengles.GL11Ext#glCurrentPaletteMatrixOES(int)}
     */
    public void glCurrentPaletteMatrixOES(int matrixpaletteindex) {
        checkThread();
        mgl11Ext.glCurrentPaletteMatrixOES(matrixpaletteindex);
        checkError();
    }

    
    /**
     * Gl load palette from model view matrix oes.
     *
     * @see{@link javax.microedition.khronos.opengles.GL11Ext#glLoadPaletteFromModelViewMatrixOES()}
     */
    public void glLoadPaletteFromModelViewMatrixOES() {
        checkThread();
        mgl11Ext.glLoadPaletteFromModelViewMatrixOES();
        checkError();
    }

    
    /**
     * Gl matrix index pointer oes.
     *
     * @param size the size
     * @param type the type
     * @param stride the stride
     * @param pointer the pointer
     * @see{@link javax.microedition.khronos.opengles.Gl11Ext#glMatrixIndexPointerOES(int, int, int, Buffer)}
     */
    public void glMatrixIndexPointerOES(int size, int type, int stride,
            Buffer pointer) {
        checkThread();
        mgl11Ext.glMatrixIndexPointerOES(size, type, stride, pointer);
        checkError();
    }

   
    /**
     * Gl matrix index pointer oes.
     *
     * @param size the size
     * @param type the type
     * @param stride the stride
     * @param offset the offset
     * @see{@link  javax.microedition.khronos.opengles.GL11Ext#glMatrixIndexPointerOES(int, int, int, Buffer)}
     */
    public void glMatrixIndexPointerOES(int size, int type, int stride,
            int offset) {
        checkThread();
        mgl11Ext.glMatrixIndexPointerOES(size, type, stride, offset);
        checkError();
    }

   
    /**
     * Gl weight pointer oes.
     *
     * @param size the size
     * @param type the type
     * @param stride the stride
     * @param pointer the pointer
     * @see{@link  javax.microedition.khronos.opengles.GL11Ext#glWeightPointerOES(int, int, int, Buffer)}
     */
    public void glWeightPointerOES(int size, int type, int stride,
            Buffer pointer) {
        checkThread();
        mgl11Ext.glWeightPointerOES(size, type, stride, pointer);
        checkError();
    }

    /**
     * Gl weight pointer oes.
     *
     * @param size the size
     * @param type the type
     * @param stride the stride
     * @param offset the offset
     * @see{@link  javax.microedition.khronos.opengles.GL11Ext#glWeightPointerOES(int, int, int, int)}
     */
    public void glWeightPointerOES(int size, int type, int stride, int offset) {
        checkThread();
        mgl11Ext.glWeightPointerOES(size, type, stride, offset);
        checkError();
    }

   
    /**
     * Gl bind framebuffer oes.
     *
     * @param target the target
     * @param framebuffer the framebuffer
     * @see{@link javax.microedition.khronos.opengles.GL11ExtensionPack#glBindFramebufferOES(int, int)}
     */
    @Override
    public void glBindFramebufferOES(int target, int framebuffer) {
        checkThread();
        mgl11ExtensionPack.glBindFramebufferOES(target, framebuffer);
        checkError();
    }

    
    /**
     * Gl bind renderbuffer oes.
     *
     * @param target the target
     * @param renderbuffer the renderbuffer
     * @see{@link javax.microedition.khronos.opengles.GL11ExtensionPack#glBlendRenderbufferOES(int, int)}
     */
    @Override
    public void glBindRenderbufferOES(int target, int renderbuffer) {
        checkThread();
        mgl11ExtensionPack.glBindRenderbufferOES(target, renderbuffer);
        checkError();
    }

   
    /**
     * Gl blend equation.
     *
     * @param mode the mode
     * @see{@link javax.microedition.khronos.opengles.GL11ExtensionPack#glBlendEquation(int)}
     */
    @Override
    public void glBlendEquation(int mode) {
        checkThread();
        mgl11ExtensionPack.glBlendEquation(mode);
        checkError();
    }

    
    /**
     * Gl blend equation separate.
     *
     * @param modeRGB the mode rgb
     * @param modeAlpha the mode alpha
     * @see{@link java.xmicroedition.khronos.opengles.GL11ExtensionPack#glBlendEquationSeparate(int, int)}
     */
    @Override
    public void glBlendEquationSeparate(int modeRGB, int modeAlpha) {
        checkThread();
        mgl11ExtensionPack.glBlendEquationSeparate(modeRGB, modeAlpha);
        checkError();
    }

    
    /**
     * Gl blend func separate.
     *
     * @param srcRGB the src rgb
     * @param dstRGB the dst rgb
     * @param srcAlpha the src alpha
     * @param dstAlpha the dst alpha
     * @see{@link javax.microedition.khronos.opengles.GL11ExtensionPack#glBlendfuncSeparate(int, int, int, int)}
     */
    @Override
    public void glBlendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha,
            int dstAlpha) {
        checkThread();
        mgl11ExtensionPack.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
        checkError();
    }

    
    /**
     * Gl check framebuffer status oes.
     *
     * @param target the target
     * @return the int
     * @see{@link javax.microedition.khronos.opengles.GL11ExtensionPack#glCheckFramebufferStatusOES(int)}
     */
    @Override
    public int glCheckFramebufferStatusOES(int target) {
        checkThread();
        int result = mgl11ExtensionPack.glCheckFramebufferStatusOES(target);
        checkError();
        return result;
    }

    
    /**
     * Gl delete framebuffers oes.
     *
     * @param n the n
     * @param framebuffers the framebuffers
     * @param offset the offset
     * @see{@link javax.microedition.khronos.opengles.GL11ExtensionPack#glDeleteFramebuffersOES(int, int[], int)}
     */
    @Override
    public void glDeleteFramebuffersOES(int n, int[] framebuffers, int offset) {
        checkThread();
        mgl11ExtensionPack.glDeleteFramebuffersOES(n, framebuffers, offset);
        checkError();
    }

    
    /**
     * Gl delete framebuffers oes.
     *
     * @param n the n
     * @param framebuffers the framebuffers
     * @see{@link javax.microedition.khronos.opengles.GL11ExtensionPack#glDeleteFramebuffersOES(int, java.nio.IntBuffer)}
     */
    @Override
    public void glDeleteFramebuffersOES(int n, IntBuffer framebuffers) {
        checkThread();
        mgl11ExtensionPack.glDeleteFramebuffersOES(n, framebuffers);
        checkError();
    }

    
    /**
     * Gl delete renderbuffers oes.
     *
     * @param n the n
     * @param renderbuffers the renderbuffers
     * @param offset the offset
     * @see{@link javax.microedition.khronos.opengles.GL11extensionPack#glDeleteRenderbuffersOES(int, int[], int)}
     */
    @Override
    public void glDeleteRenderbuffersOES(int n, int[] renderbuffers, int offset) {
        checkThread();
        mgl11ExtensionPack.glDeleteRenderbuffersOES(n, renderbuffers, offset);
        checkError();
    }

    
    /**
     * Gl delete renderbuffers oes.
     *
     * @param n the n
     * @param renderbuffers the renderbuffers
     * @see{@link javax.microedition.khronos.opengles.GL11ExtensionPack#glDeleteRenderbuffersOES(int, java.nio.IntBuffer)}
     */
    @Override
    public void glDeleteRenderbuffersOES(int n, IntBuffer renderbuffers) {
        checkThread();
        mgl11ExtensionPack.glDeleteRenderbuffersOES(n, renderbuffers);
        checkError();
    }

    
    /**
     * Gl framebuffer renderbuffer oes.
     *
     * @param target the target
     * @param attachment the attachment
     * @param renderbuffertarget the renderbuffertarget
     * @param renderbuffer the renderbuffer
     * @see{@link javax.microedition.khronos.opengles.GL11ExtensionPack#glFramebufferRenderbufferOES(int, int,int,int)}
     */
    @Override
    public void glFramebufferRenderbufferOES(int target, int attachment,
            int renderbuffertarget, int renderbuffer) {
        checkThread();
        mgl11ExtensionPack.glFramebufferRenderbufferOES(target, attachment, renderbuffertarget, renderbuffer);
        checkError();
    }

    
    /**
     * Gl framebuffer texture2 does.
     *
     * @param target the target
     * @param attachment the attachment
     * @param textarget the textarget
     * @param texture the texture
     * @param level the level
     * @see{@link javax.microedition.khronos.opengles.GL11ExtensionPack#glFramebufferTexture2DOES(int, int, int, int, int)}
     */
    @Override
    public void glFramebufferTexture2DOES(int target, int attachment,
            int textarget, int texture, int level) {
        checkThread();
        mgl11ExtensionPack.glFramebufferTexture2DOES(target, attachment, textarget, texture, level);
        checkError();
    }

    
    /**
     * Gl generate mipmap oes.
     *
     * @param target the target
     * @see{@link javax.microedition.khronos.opengles.GL11ExtensionPack#glGenerateMipmapOES(int)}
     */
    @Override
    public void glGenerateMipmapOES(int target) {
        checkThread();
        mgl11ExtensionPack.glGenerateMipmapOES(target);
        checkError();
    }

    
    /**
     * Gl gen framebuffers oes.
     *
     * @param n the n
     * @param framebuffers the framebuffers
     * @param offset the offset
     * @see{@link javax.microedition.khronos.opengles.GL11ExtensionPack#glGenFramebuffersOES(int, int[], int)}
     */
    @Override
    public void glGenFramebuffersOES(int n, int[] framebuffers, int offset) {
        checkThread();
        mgl11ExtensionPack.glGenFramebuffersOES(n, framebuffers, offset);
        checkError();
    }

   
    /**
     * Gl gen framebuffers oes.
     *
     * @param n the n
     * @param framebuffers the framebuffers
     * @see{@link javax.microedition.khronos.opengles.GL11ExtensionPack#glGenFramebuffersOES(int, java.nio.IntBuffer)}
     */
    @Override
    public void glGenFramebuffersOES(int n, IntBuffer framebuffers) {
        checkThread();
        mgl11ExtensionPack.glGenFramebuffersOES(n, framebuffers);
        checkError();
    }

    
    /**
     * Gl gen renderbuffers oes.
     *
     * @param n the n
     * @param renderbuffers the renderbuffers
     * @param offset the offset
     * @see{@link javax.microedition.khronos.opengles.GL11ExtensionPack#glGenRenderbuffersOES(int, int[], int)}
     */
    @Override
    public void glGenRenderbuffersOES(int n, int[] renderbuffers, int offset) {
        checkThread();
        mgl11ExtensionPack.glGenRenderbuffersOES(n, renderbuffers, offset);
        checkError();
    }

    
    /**
     * Gl gen renderbuffers oes.
     *
     * @param n the n
     * @param renderbuffers the renderbuffers
     * @see{@link javax.microedition.khronos.opengles.GL11ExtensionPack#glGenRenderbuffersOES(int, java.nio.IntBuffer)}
     */
    @Override
    public void glGenRenderbuffersOES(int n, IntBuffer renderbuffers) {
        checkThread();
        mgl11ExtensionPack.glGenRenderbuffersOES(n, renderbuffers);
        checkError();
    }

    
    /**
     * Gl get framebuffer attachment parameteriv oes.
     *
     * @param target the target
     * @param attachment the attachment
     * @param pname the pname
     * @param params the params
     * @param offset the offset
     * @see{@link javax.microedition.khronos.opengles.GL11ExtensionPack#glGetFramebufferAttachmentParameterivOES(int,int, int, int[],int)}
     */
    @Override
    public void glGetFramebufferAttachmentParameterivOES(int target,
            int attachment, int pname, int[] params, int offset) {
        checkThread();
        mgl11ExtensionPack.glGetFramebufferAttachmentParameterivOES(target, attachment, pname, params, offset);
        checkError();
    }

    
    /**
     * Gl get framebuffer attachment parameteriv oes.
     *
     * @param target the target
     * @param attachment the attachment
     * @param pname the pname
     * @param params the params
     * @see{@link javax.microedition.khronos.opengles.GL11ExtensionPack#glGetFramebufferAttachmentParameterivOES(int, int, int, java.nio.IntBuffer)}
     */
    @Override
    public void glGetFramebufferAttachmentParameterivOES(int target,
            int attachment, int pname, IntBuffer params) {
        checkThread();
        mgl11ExtensionPack.glGetFramebufferAttachmentParameterivOES(target, attachment, pname, params);
        checkError();
    }

    
    /**
     * Gl get renderbuffer parameteriv oes.
     *
     * @param target the target
     * @param pname the pname
     * @param params the params
     * @param offset the offset
     * @see{@link javax.microedtion.khronos.opengles.GL11ExtensionPack#glGetRenderbufferParameterivOES(int, int, int[], int)}
     */
    @Override
    public void glGetRenderbufferParameterivOES(int target, int pname,
            int[] params, int offset) {
        checkThread();
        mgl11ExtensionPack.glGetRenderbufferParameterivOES(target, pname, params, offset);
        checkError();
    }

   
    /**
     * Gl get renderbuffer parameteriv oes.
     *
     * @param target the target
     * @param pname the pname
     * @param params the params
     * @see{@link javax.microedition.khronos.opengles.GL11ExtensionPack#glGetRenderbufferParameterivOES(int, int, java.nio.IntBuffer)}
     */
    @Override
    public void glGetRenderbufferParameterivOES(int target, int pname,
            IntBuffer params) {
        checkThread();
        mgl11ExtensionPack.glGetRenderbufferParameterivOES(target, pname, params);
        checkError();
    }

    
    /**
     * Gl get tex genfv.
     *
     * @param coord the coord
     * @param pname the pname
     * @param params the params
     * @param offset the offset
     * @see{@link javax.microedition.khronos.opengles.GL11extensionPack#glGetTexGenfv(int, int, float[], int)}
     */
    @Override
    public void glGetTexGenfv(int coord, int pname, float[] params, int offset) {
        checkThread();
        mgl11ExtensionPack.glGetTexGenfv(coord, pname, params, offset);
        checkError();
    }

    
    /**
     * Gl get tex genfv.
     *
     * @param coord the coord
     * @param pname the pname
     * @param params the params
     * @see{@link javax.microedition.khronos.opengles.GL11ExtensionPack#glGetTexGenfv(int, int, java.nio.FloatBuffer)}
     */
    @Override
    public void glGetTexGenfv(int coord, int pname, FloatBuffer params) {
        checkThread();
        mgl11ExtensionPack.glGetTexGenfv(coord, pname, params);
        checkError();
    }

    
   /**
    * Gl get tex geniv.
    *
    * @param coord the coord
    * @param pname the pname
    * @param params the params
    * @param offset the offset
    * @see{@link javax.microedition.khronos.opengles.GL11ExtensionPack#glGetTexGeniv(int, int, int[], int)}
    */
    @Override
    public void glGetTexGeniv(int coord, int pname, int[] params, int offset) {
        checkThread();
        mgl11ExtensionPack.glGetTexGeniv(coord, pname, params, offset);
        checkError();
    }

   
    /**
     * Gl get tex geniv.
     *
     * @param coord the coord
     * @param pname the pname
     * @param params the params
     * @see{@link javax.microedition.khronosh.opengles.GL11ExtensionPack#glGetTexGeniv(int, int, java.nio.IntBuffer)}
     */
    @Override
    public void glGetTexGeniv(int coord, int pname, IntBuffer params) {
        checkThread();
        mgl11ExtensionPack.glGetTexGeniv(coord, pname, params);
        checkError();
    }

   
    /**
     * Gl get tex genxv.
     *
     * @param coord the coord
     * @param pname the pname
     * @param params the params
     * @param offset the offset
     * @see{@link javax.microedition.khronos.opengles.GL11ExtensionPack#glGetTexGenxv(int, int, int[], int)}
     */
    @Override
    public void glGetTexGenxv(int coord, int pname, int[] params, int offset) {
        checkThread();
        mgl11ExtensionPack.glGetTexGenxv(coord, pname, params, offset);
        checkError();
    }

   
    /**
     * Gl get tex genxv.
     *
     * @param coord the coord
     * @param pname the pname
     * @param params the params
     * @see{@link javax.microedition.khronos.opengles.GL11ExtensionPack#glGetTexGenxy(int, int, java.nio.IntBuffer)}
     */
    @Override
    public void glGetTexGenxv(int coord, int pname, IntBuffer params) {
        checkThread();
        mgl11ExtensionPack.glGetTexGenxv(coord, pname, params);
        checkError();
    }

   
    /**
     * Gl is framebuffer oes.
     *
     * @param framebuffer the framebuffer
     * @return true, if gl is framebuffer oes
     * @see{@link javax.microedition.khronos.opengles.GL11ExtensionPack#gllsFramebufferOES(int)}
     */
    @Override
    public boolean glIsFramebufferOES(int framebuffer) {
        checkThread();
        boolean result = mgl11ExtensionPack.glIsFramebufferOES(framebuffer);
        checkError();
        return result;
    }

   
    /**
     * Gl is renderbuffer oes.
     *
     * @param renderbuffer the renderbuffer
     * @return true, if gl is renderbuffer oes
     * @see{@link javax.microedition.khronos.opengles.GL11ExtensionPack#gllsRenderbufferOES(int)}
     */
    @Override
    public boolean glIsRenderbufferOES(int renderbuffer) {
        checkThread();
        mgl11ExtensionPack.glIsRenderbufferOES(renderbuffer);
        checkError();
        return false;
    }

    
    /**
     * Gl renderbuffer storage oes.
     *
     * @param target the target
     * @param internalformat the internalformat
     * @param width the width
     * @param height the height
     * @see{@link javax.microedition.khronos.opengles.GL11ExtensionPack#glRenderbufferStorageOES(int, int, int, int)}
     */
    @Override
    public void glRenderbufferStorageOES(int target, int internalformat,
            int width, int height) {
        checkThread();
        mgl11ExtensionPack.glRenderbufferStorageOES(target, internalformat, width, height);
        checkError();
    }

    
    /**
     * Gl tex genf.
     *
     * @param coord the coord
     * @param pname the pname
     * @param param the param
     * @see{@link javax.microedition.khronos.opengles.GL11ExtensionPack#glTexGenf(int, int, float)}
     */
    @Override
    public void glTexGenf(int coord, int pname, float param) {
        checkThread();
        mgl11ExtensionPack.glTexGenf(coord, pname, param);
        checkError();
    }

    
    /**
     * Gl tex genfv.
     *
     * @param coord the coord
     * @param pname the pname
     * @param params the params
     * @param offset the offset
     * @see{@link javax.microedition.khronosh.opengles.GL11ExtensionPack#glTexGenfy(int, int, float[], int)}
     */
    @Override
    public void glTexGenfv(int coord, int pname, float[] params, int offset) {
        checkThread();
        mgl11ExtensionPack.glTexGenfv(coord, pname, params, offset);
        checkError();
    }

   
    /**
     * Gl tex genfv.
     *
     * @param coord the coord
     * @param pname the pname
     * @param params the params
     * @see{@link javax.microedition.khronos.opengles.GL11ExtensionPack#glTexGenfv(int, int, java.nio.FloatBuffer)}
     */
    @Override
    public void glTexGenfv(int coord, int pname, FloatBuffer params) {
        checkThread();
        mgl11ExtensionPack.glTexGenfv(coord, pname, params);
        checkError();
    }

    
    /**
     * Gl tex geni.
     *
     * @param coord the coord
     * @param pname the pname
     * @param param the param
     * @see{@link javax.microedition.khronos.opengles.GL11ExtensionPack#glTexGeni(int, int, int)}
     */
    @Override
    public void glTexGeni(int coord, int pname, int param) {
        checkThread();
        mgl11ExtensionPack.glTexGeni(coord, pname, param);
        checkError();
    }

   
    /**
     * Gl tex geniv.
     *
     * @param coord the coord
     * @param pname the pname
     * @param params the params
     * @param offset the offset
     * @see{@link javax.microedition.khronos.opengles.GL11ExtensionPack#glTexGeniv(int, int[], int)}
     */
    @Override
    public void glTexGeniv(int coord, int pname, int[] params, int offset) {
        checkThread();
        mgl11ExtensionPack.glTexGeniv(coord, pname, params, offset);
        checkError();
    }

    
    /**
     * Gl tex geniv.
     *
     * @param coord the coord
     * @param pname the pname
     * @param params the params
     * @see{@link javax.microedition.khronos.opengles.GL11ExtensionPack#glTexGeniv(int, int, java.nio.IntBuffer)}
     */
    @Override
    public void glTexGeniv(int coord, int pname, IntBuffer params) {
        checkThread();
        mgl11ExtensionPack.glTexGeniv(coord, pname, params);
        checkError();
    }

    
    /**
     * Gl tex genx.
     *
     * @param coord the coord
     * @param pname the pname
     * @param param the param
     * @see{@link javax.microedition.khronos.opengles.GL11ExtensionPack#glTexGenx(int, int, int)}
     */
    @Override
    public void glTexGenx(int coord, int pname, int param) {
        checkThread();
        mgl11ExtensionPack.glTexGenx(coord, pname, param);
        checkError();
    }

    
    /**
     * Gl tex genxv.
     *
     * @param coord the coord
     * @param pname the pname
     * @param params the params
     * @param offset the offset
     * @see{@link javax.microedition.khronos.opengles.GL11ExtensionPack#glTexGenxv(int, int, int[], int)}
     */
    @Override
    public void glTexGenxv(int coord, int pname, int[] params, int offset) {
        checkThread();
        mgl11ExtensionPack.glTexGenxv(coord, pname, params, offset);
        checkError();
    }

    
    /**
     * Gl tex genxv.
     *
     * @param coord the coord
     * @param pname the pname
     * @param params the params
     * @see{@link javax.microedition.khronos.opengles.GL11ExtensionPack#glTexGenxv(int, int, java.nio.intBuffer)}
     */
    @Override
    public void glTexGenxv(int coord, int pname, IntBuffer params) {
        checkThread();
        mgl11ExtensionPack.glTexGenxv(coord, pname, params);
        checkError();
    }
}
