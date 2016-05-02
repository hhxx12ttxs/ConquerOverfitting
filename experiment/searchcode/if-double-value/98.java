<<<<<<< HEAD
/*
 * Copyright 2009 - 2010 JogAmp Community. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * 
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 * 
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY JogAmp Community ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JogAmp Community OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of JogAmp Community.
 */

package com.jogamp.opencl;

import com.jogamp.opencl.util.CLUtil;
import com.jogamp.common.nio.Buffers;
import com.jogamp.common.nio.NativeSizeBuffer;
import com.jogamp.opencl.llb.CLKernelBinding;
import java.nio.Buffer;
import java.nio.ByteBuffer;

import static com.jogamp.opencl.CLException.*;
import static com.jogamp.opencl.llb.CL.*;
import static com.jogamp.common.os.Platform.*;

/**
 * High level abstraction for an OpenCL Kernel.
 * A kernel is a function declared in a program. A kernel is identified by the <code>kernel</code> qualifier
 * applied to any function in a program. A kernel object encapsulates the specific <code>kernel</code>
 * function declared in a program and the argument values to be used when executing this
 * <code>kernel</code> function.
 * <p>
 * Example:
 * <pre>
 * CLKernel addKernel = program.createCLKernel("add");
 * addKernel.setArgs(clBufferA, clBufferB);
 * ...
 * queue.putEnqueue1DKernel(addKernel, 0, clBufferA.getSize(), 0);
 * </pre>
 * CLKernel provides utility methods for setting vector types (float4, int2...) with up to 4 elements. Larger
 * vectors like float16 can be set using {@link #setArg(int, java.nio.Buffer)}.
 *
 * Arguments pointing to {@link CLBuffer}s or {@link CLImage}s can be set using {@link #setArg(int, com.jogamp.opencl.CLMemory) }
 * or its relative putArg(..) methods.
 * </p>
 * <p>
 * CLKernel is not threadsafe. However it is perfectly safe to create a new instance of a CLKernel for every
 * involved Thread.
 * </p>
 * @see CLProgram#createCLKernel(java.lang.String)
 * @see CLProgram#createCLKernels()
 * @author Michael Bien
 */
public class CLKernel extends CLObjectResource implements Cloneable {

    public final String name;
    public final int numArgs;

    private final CLProgram program;
    private final CLKernelBinding binding;

    private final ByteBuffer buffer;

    private int argIndex;
    private boolean force32BitArgs;

    CLKernel(CLProgram program, long id) {
        this(program, null, id);
    }

    CLKernel(CLProgram program, String name, long id) {
        super(program.getContext(), id);

        this.program = program;
        this.buffer = Buffers.newDirectByteBuffer(8*4);

        binding = program.getPlatform().getKernelBinding();

        if(name == null) {
            // get function name
            NativeSizeBuffer size = NativeSizeBuffer.wrap(buffer);
            int ret = binding.clGetKernelInfo(ID, CL_KERNEL_FUNCTION_NAME, 0, null, size);
            checkForError(ret, "error while asking for kernel function name");

            ByteBuffer bb = Buffers.newDirectByteBuffer((int)size.get(0));

            ret = binding.clGetKernelInfo(ID, CL_KERNEL_FUNCTION_NAME, bb.capacity(), bb, null);
            checkForError(ret, "error while asking for kernel function name");
            
            this.name = CLUtil.clString2JavaString(bb, bb.capacity());
        }else{
            this.name = name;
        }

        // get number of arguments
        int ret = binding.clGetKernelInfo(ID, CL_KERNEL_NUM_ARGS, buffer.capacity(), buffer, null);
        checkForError(ret, "error while asking for number of function arguments.");

        numArgs = buffer.getInt(0);

    }

    public CLKernel putArg(Buffer value) {
        setArg(argIndex++, value);
        return this;
    }
    
    public CLKernel putArg(CLMemory<?> value) {
        setArg(argIndex, value);
        argIndex++;
        return this;
    }

    public CLKernel putArg(short value) {
        setArg(argIndex, value);
        argIndex++;
        return this;
    }

    public CLKernel putArg(short x, short y) {
        setArg(argIndex, x, y);
        argIndex++;
        return this;
    }

    public CLKernel putArg(short x, short y, short z) {
        setArg(argIndex, x, y, z);
        argIndex++;
        return this;
    }

    public CLKernel putArg(short x, short y, short z, short w) {
        setArg(argIndex, x, y, z, w);
        argIndex++;
        return this;
    }

    public CLKernel putArg(int value) {
        setArg(argIndex, value);
        argIndex++;
        return this;
    }

    public CLKernel putArg(int x, int y) {
        setArg(argIndex, x, y);
        argIndex++;
        return this;
    }

    public CLKernel putArg(int x, int y, int z) {
        setArg(argIndex, x, y, z);
        argIndex++;
        return this;
    }

    public CLKernel putArg(int x, int y, int z, int w) {
        setArg(argIndex, x, y, z, w);
        argIndex++;
        return this;
    }

    public CLKernel putArg(long value) {
        setArg(argIndex, value);
        argIndex++;
        return this;
    }

    public CLKernel putArg(long x, long y) {
        setArg(argIndex, x, y);
        argIndex++;
        return this;
    }

    public CLKernel putArg(long x, long y, long z) {
        setArg(argIndex, x, y, z);
        argIndex++;
        return this;
    }

    public CLKernel putArg(long x, long y, long z, long w) {
        setArg(argIndex, x, y, z, w);
        argIndex++;
        return this;
    }

    public CLKernel putArg(float value) {
        setArg(argIndex, value);
        argIndex++;
        return this;
    }

    public CLKernel putArg(float x, float y) {
        setArg(argIndex, x, y);
        argIndex++;
        return this;
    }

    public CLKernel putArg(float x, float y, float z) {
        setArg(argIndex, x, y, z);
        argIndex++;
        return this;
    }

    public CLKernel putArg(float x, float y, float z, float w) {
        setArg(argIndex, x, y, z, w);
        argIndex++;
        return this;
    }

    public CLKernel putArg(double value) {
        setArg(argIndex, value);
        argIndex++;
        return this;
    }

    public CLKernel putArg(double x, double y) {
        setArg(argIndex, x, y);
        argIndex++;
        return this;
    }

    public CLKernel putArg(double x, double y, double z) {
        setArg(argIndex, x, y, z);
        argIndex++;
        return this;
    }

    public CLKernel putArg(double x, double y, double z, double w) {
        setArg(argIndex, x, y, z, w);
        argIndex++;
        return this;
    }

    /**
     * Sets the size of a <i>local</i> kernel argument.
     */
    public CLKernel putArgSize(int size) {
        setArgSize(argIndex, size);
        argIndex++;
        return this;
    }

    public CLKernel putArgs(CLMemory<?>... values) {
        setArgs(argIndex, values);
        argIndex += values.length;
        return this;
    }

    /**
     * Resets the argument index to 0.
     */
    public CLKernel rewind() {
        argIndex = 0;
        return this;
    }

    /**
     * Returns the argument index used in the relative putArt(...) methods.
     */
    public int position() {
        return argIndex;
    }

    public CLKernel setArg(int argumentIndex, Buffer value) {
        if(!value.isDirect()) {
            throw new IllegalArgumentException("buffer must be direct.");
        }
        setArgument(argumentIndex, Buffers.sizeOfBufferElem(value)*value.remaining(), value);
        return this;
    }

    public CLKernel setArg(int argumentIndex, CLMemory<?> value) {
        setArgument(argumentIndex, is32Bit()?4:8, wrap(value.ID));
        return this;
    }

    public CLKernel setArg(int argumentIndex, short value) {
        setArgument(argumentIndex, 2, wrap(value));
        return this;
    }

    public CLKernel setArg(int argumentIndex, short x, short y) {
        setArgument(argumentIndex, 2*2, wrap(x, y));
        return this;
    }

    public CLKernel setArg(int argumentIndex, short x, short y, short z) {
        setArgument(argumentIndex, 2*3, wrap(x, y, z));
        return this;
    }

    public CLKernel setArg(int argumentIndex, short x, short y, short z, short w) {
        setArgument(argumentIndex, 2*4, wrap(x, y, z, w));
        return this;
    }

    public CLKernel setArg(int argumentIndex, int value) {
        setArgument(argumentIndex, 4, wrap(value));
        return this;
    }

    public CLKernel setArg(int argumentIndex, int x, int y) {
        setArgument(argumentIndex, 4*2, wrap(x, y));
        return this;
    }

    public CLKernel setArg(int argumentIndex, int x, int y, int z) {
        setArgument(argumentIndex, 4*3, wrap(x, y, z));
        return this;
    }

    public CLKernel setArg(int argumentIndex, int x, int y, int z, int w) {
        setArgument(argumentIndex, 4*4, wrap(x, y, z, w));
        return this;
    }

    public CLKernel setArg(int argumentIndex, long value) {
        if(force32BitArgs) {
            setArgument(argumentIndex, 4, wrap((int)value));
        }else{
            setArgument(argumentIndex, 8, wrap(value));
        }
        return this;
    }

    public CLKernel setArg(int argumentIndex, long x, long y) {
        if(force32BitArgs) {
            setArgument(argumentIndex, 4*2, wrap((int)x, (int)y));
        }else{
            setArgument(argumentIndex, 8*2, wrap(x, y));
        }
        return this;
    }

    public CLKernel setArg(int argumentIndex, long x, long y, long z) {
        if(force32BitArgs) {
            setArgument(argumentIndex, 4*3, wrap((int)x, (int)y, (int)z));
        }else{
            setArgument(argumentIndex, 8*3, wrap(x, y, z));
        }
        return this;
    }

    public CLKernel setArg(int argumentIndex, long x, long y, long z, long w) {
        if(force32BitArgs) {
            setArgument(argumentIndex, 4*4, wrap((int)x, (int)y, (int)z, (int)w));
        }else{
            setArgument(argumentIndex, 8*4, wrap(x, y, z, w));
        }
        return this;
    }

    public CLKernel setArg(int argumentIndex, float value) {
        setArgument(argumentIndex, 4, wrap(value));
        return this;
    }

    public CLKernel setArg(int argumentIndex, float x, float y) {
        setArgument(argumentIndex, 4*2, wrap(x, y));
        return this;
    }

    public CLKernel setArg(int argumentIndex, float x, float y, float z) {
        setArgument(argumentIndex, 4*3, wrap(x, y, z));
        return this;
    }

    public CLKernel setArg(int argumentIndex, float x, float y, float z, float w) {
        setArgument(argumentIndex, 4*4, wrap(x, y, z, w));
        return this;
    }

    public CLKernel setArg(int argumentIndex, double value) {
        if(force32BitArgs) {
            setArgument(argumentIndex, 4, wrap((float)value));
        }else{
            setArgument(argumentIndex, 8, wrap(value));
        }
        return this;
    }

    public CLKernel setArg(int argumentIndex, double x, double y) {
        if(force32BitArgs) {
            setArgument(argumentIndex, 4*2, wrap((float)x, (float)y));
        }else{
            setArgument(argumentIndex, 8*2, wrap(x, y));
        }
        return this;
    }

    public CLKernel setArg(int argumentIndex, double x, double y, double z) {
        if(force32BitArgs) {
            setArgument(argumentIndex, 4*3, wrap((float)x, (float)y, (float)z));
        }else{
            setArgument(argumentIndex, 8*3, wrap(x, y, z));
        }
        return this;
    }

    public CLKernel setArg(int argumentIndex, double x, double y, double z, double w) {
        if(force32BitArgs) {
            setArgument(argumentIndex, 4*4, wrap((float)x, (float)y, (float)z, (float)w));
        }else{
            setArgument(argumentIndex, 8*4, wrap(x, y, z, w));
        }
        return this;
    }

    /**
     * Sets the size of a <i>local</i> kernel argument at the specified index.
     */
    public CLKernel setArgSize(int argumentIndex, int size) {
        setArgument(argumentIndex, size, null);
        return this;
    }

    public CLKernel setArgs(CLMemory<?>... values) {
        setArgs(0, values);
        return this;
    }

    public CLKernel setArgs(Object... values) {
        if(values == null || values.length == 0) {
            throw new IllegalArgumentException("values array was empty or null.");
        }
        for (int i = 0; i < values.length; i++) {
            Object value = values[i];
            if(value instanceof CLMemory<?>) {
                setArg(i, (CLMemory<?>)value);
            }else if(value instanceof Short) {
                setArg(i, (Short)value);
            }else if(value instanceof Integer) {
                setArg(i, (Integer)value);
            }else if(value instanceof Long) {
                setArg(i, (Long)value);
            }else if(value instanceof Float) {
                setArg(i, (Float)value);
            }else if(value instanceof Double) {
                setArg(i, (Double)value);
            }else if(value instanceof Buffer) {
                setArg(i, (Buffer)value);
            }else{
                throw new IllegalArgumentException(value + " is not a valid argument.");
            }
        }
        return this;
    }

    private void setArgs(int startIndex, CLMemory<?>... values) {
        for (int i = 0; i < values.length; i++) {
            setArg(i+startIndex, values[i]);
        }
    }

    private void setArgument(int argumentIndex, int size, Buffer value) {
        if(argumentIndex >= numArgs || argumentIndex < 0) {
            throw new IndexOutOfBoundsException("kernel "+ this +" has "+numArgs+
                    " arguments, can not set argument with index "+argumentIndex);
        }
        if(!program.isExecutable()) {
            throw new IllegalStateException("can not set program" +
                    " arguments for a not executable program. "+program);
        }

        int ret = binding.clSetKernelArg(ID, argumentIndex, size, value);
        if(ret != CL_SUCCESS) {
            throw newException(ret, "error setting arg "+argumentIndex+" to value "+value+" of size "+size+" of "+this);
        }
    }

    /**
     * Forces double and long arguments to be passed as float and int to the OpenCL kernel.
     * This can be used in applications which want to mix kernels with different floating point precision.
     */
    public CLKernel setForce32BitArgs(boolean force) {
        this.force32BitArgs = force;
        return this;
    }
    
    public CLProgram getProgram() {
        return program;
    }

    /**
     * @see #setForce32BitArgs(boolean) 
     */
    public boolean isForce32BitArgsEnabled() {
        return force32BitArgs;
    }

    private Buffer wrap(float value) {
        return buffer.putFloat(0, value);
    }

    private Buffer wrap(float a, float b) {
        return buffer.putFloat(0, a).putFloat(4, b);
    }

    private Buffer wrap(float a, float b, float c) {
        return buffer.putFloat(0, a).putFloat(4, b).putFloat(8, c);
    }

    private Buffer wrap(float a, float b, float c, float d) {
        return buffer.putFloat(0, a).putFloat(4, b).putFloat(8, c).putFloat(12, d);
    }

    private Buffer wrap(double value) {
        return buffer.putDouble(0, value);
    }

    private Buffer wrap(double a, double b) {
        return buffer.putDouble(0, a).putDouble(8, b);
    }

    private Buffer wrap(double a, double b, double c) {
        return buffer.putDouble(0, a).putDouble(8, b).putDouble(16, c);
    }

    private Buffer wrap(double a, double b, double c, double d) {
        return buffer.putDouble(0, a).putDouble(8, b).putDouble(16, c).putDouble(24, d);
    }

    private Buffer wrap(short value) {
        return buffer.putShort(0, value);
    }

    private Buffer wrap(short a, short b) {
        return buffer.putShort(0, a).putShort(2, b);
    }

    private Buffer wrap(short a, short b, short c) {
        return buffer.putShort(0, a).putShort(2, b).putShort(4, c);
    }

    private Buffer wrap(short a, short b, short c, short d) {
        return buffer.putShort(0, a).putShort(2, b).putShort(4, c).putShort(6, d);
    }

    private Buffer wrap(int value) {
        return buffer.putInt(0, value);
    }

    private Buffer wrap(int a, int b) {
        return buffer.putInt(0, a).putInt(4, b);
    }

    private Buffer wrap(int a, int b, int c) {
        return buffer.putInt(0, a).putInt(4, b).putInt(8, c);
    }

    private Buffer wrap(int a, int b, int c, int d) {
        return buffer.putInt(0, a).putInt(4, b).putInt(8, c).putInt(12, d);
    }

    private Buffer wrap(long value) {
        return buffer.putLong(0, value);
    }

    private Buffer wrap(long a, long b) {
        return buffer.putLong(0, a).putLong(8, b);
    }

    private Buffer wrap(long a, long b, long c) {
        return buffer.putLong(0, a).putLong(8, b).putLong(16, c);
    }

    private Buffer wrap(long a, long b, long c, long d) {
        return buffer.putLong(0, a).putLong(8, b).putLong(16, c).putLong(24, d);
    }

    /**
     * Returns the amount of local memory in bytes being used by a kernel.
     * This includes local memory that may be needed by an implementation to execute the kernel,
     * variables declared inside the kernel with the <code>__local</code> address qualifier and local memory
     * to be allocated for arguments to the kernel declared as pointers with the <code>__local</code> address
     * qualifier and whose size is specified with clSetKernelArg.
     * If the local memory size, for any pointer argument to the kernel declared with
     * the <code>__local</code> address qualifier, is not specified, its size is assumed to be 0.
     */
    public long getLocalMemorySize(CLDevice device) {
        return getWorkGroupInfo(device, CL_KERNEL_LOCAL_MEM_SIZE);
    }

    /**
     * Returns the work group size for this kernel on the given device.
     * This provides a mechanism for the application to query the work-group size
     * that can be used to execute a kernel on a specific device given by device.
     * The OpenCL implementation uses the resource requirements of the kernel
     * (register usage etc.) to determine what this work-group size should be. 
     */
    public long getWorkGroupSize(CLDevice device) {
        return getWorkGroupInfo(device, CL_KERNEL_WORK_GROUP_SIZE);
    }

    /**
     * Returns the preferred multiple of workgroup size for launch on the supplied device.
     * This is a performance hint.
     * @since OpenCL 1.1
     */
    public long getPreferredWorkGroupSizeMultiple(CLDevice device) {
        return getWorkGroupInfo(device, CL_KERNEL_PREFERRED_WORK_GROUP_SIZE_MULTIPLE);
    }

    /**
     * Returns the work-group size specified by the <code>__attribute__((reqd_work_group_size(X, Y, Z)))</code> qualifier in kernel sources.
     * If the work-group size is not specified using the above attribute qualifier <code>new long[]{(0, 0, 0)}</code> is returned.
     * The returned array has always three elements.
     */
    public long[] getCompileWorkGroupSize(CLDevice device) {
        int ret = binding.clGetKernelWorkGroupInfo(ID, device.ID, CL_KERNEL_COMPILE_WORK_GROUP_SIZE, (is32Bit()?4:8)*3, buffer, null);
        if(ret != CL_SUCCESS) {
            throw newException(ret, "error while asking for CL_KERNEL_COMPILE_WORK_GROUP_SIZE of "+this+" on "+device);
        }

        if(is32Bit()) {
            return new long[] { buffer.getInt(0), buffer.getInt(4), buffer.getInt(8) };
        }else {
            return new long[] { buffer.getLong(0), buffer.getLong(8), buffer.getLong(16) };
        }
    }

    private long getWorkGroupInfo(CLDevice device, int flag) {
        int ret = binding.clGetKernelWorkGroupInfo(ID, device.ID, flag, 8, buffer, null);
        if(ret != CL_SUCCESS) {
            throw newException(ret, "error while asking for clGetKernelWorkGroupInfo of "+this+" on "+device);
        }
        return buffer.getLong(0);
    }

    /**
     * Releases all resources of this kernel from its context.
     */
    @Override
    public synchronized void release() {
        super.release();
        int ret = binding.clReleaseKernel(ID);
        program.onKernelReleased(this);
        if(ret != CL_SUCCESS) {
            throw newException(ret, "can not release "+this);
        }
    }

    @Override
    public String toString() {
        return "CLKernel [id: " + ID
                      + " name: " + name+"]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CLKernel other = (CLKernel) obj;
        if (this.ID != other.ID) {
            return false;
        }
        if (!this.program.equals(other.program)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + (int) (this.ID ^ (this.ID >>> 32));
        hash = 43 * hash + (this.program != null ? this.program.hashCode() : 0);
        return hash;
    }

    /**
     * Returns a new instance of this kernel with uninitialized arguments.
     */
    @Override
    public CLKernel clone() {
        return program.createCLKernel(name).setForce32BitArgs(force32BitArgs);
    }

}

=======
/*
 * Copyright 2004-2010 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.slim3.datastore;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slim3.datastore.json.JsonArrayReader;
import org.slim3.datastore.json.JsonReader;
import org.slim3.datastore.json.JsonRootReader;
import org.slim3.datastore.json.JsonWriter;
import org.slim3.datastore.json.ModelReader;
import org.slim3.datastore.json.ModelWriter;
import org.slim3.util.BeanDesc;
import org.slim3.util.BeanUtil;
import org.slim3.util.ByteUtil;
import org.slim3.util.Cipher;
import org.slim3.util.CipherFactory;

import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.ShortBlob;
import com.google.appengine.api.datastore.Text;

/**
 * A meta data of model.
 * 
 * @author higa
 * @param <M>
 *            the model type
 * @since 1.0.0
 * 
 */
public abstract class ModelMeta<M> {

    /**
     * The kind of entity.
     */
    protected String kind;

    /**
     * The model class.
     */
    protected Class<M> modelClass;

    /**
     * The list of class hierarchies. If you create polymorphic models such as A
     * -> B -> C, the list of A is empty, the list of B is [B], the list of c
     * is[B, C].
     */
    protected List<String> classHierarchyList;

    /**
     * The bean descriptor.
     */
    protected BeanDesc beanDesc;

    /**
     * Constructor.
     * 
     * @param kind
     *            the kind of entity
     * @param modelClass
     *            the model class
     * @throws NullPointerException
     *             if the modelClass parameter is null
     */
    public ModelMeta(String kind, Class<M> modelClass)
            throws NullPointerException {
        this(kind, modelClass, null);
    }

    /**
     * Constructor.
     * 
     * @param kind
     *            the kind of entity
     * @param modelClass
     *            the model class
     * @param classHierarchyList
     *            the list of class hierarchies
     * @throws NullPointerException
     *             if the modelClass parameter is null
     */
    @SuppressWarnings("unchecked")
    public ModelMeta(String kind, Class<M> modelClass,
            List<String> classHierarchyList) throws NullPointerException {
        if (kind == null) {
            throw new NullPointerException("The kind parameter is null.");
        }
        if (modelClass == null) {
            throw new NullPointerException("The modelClass parameter is null.");
        }
        this.kind = kind;
        this.modelClass = modelClass;
        if (classHierarchyList == null) {
            this.classHierarchyList = Collections.EMPTY_LIST;
        } else {
            this.classHierarchyList =
                Collections.unmodifiableList(classHierarchyList);
        }
    }

    /**
     * Constructor.
     */
    protected ModelMeta() {
    }

    /**
     * Returns the kind of entity.
     * 
     * @return the kind of entity
     */
    public String getKind() {
        return kind;
    }

    /**
     * Returns the model class.
     * 
     * @return the model class
     */
    public Class<M> getModelClass() {
        return modelClass;
    }

    /**
     * Returns the list of class hierarchies.
     * 
     * @return the list of class hierarchies
     */
    public List<String> getClassHierarchyList() {
        return classHierarchyList;
    }

    /**
     * Returns the AND criterion.
     * 
     * @param criteria
     *            the filter criteria
     * @return the AND criterion
     */
    public FilterCriterion and(FilterCriterion... criteria) {
        return new CompositeCriterion(
            this,
            CompositeFilterOperator.AND,
            criteria);
    }

    /**
     * Returns the OR criterion.
     * 
     * @param criteria
     *            the filter criteria
     * @return the OR criterion
     */
    public FilterCriterion or(FilterCriterion... criteria) {
        return new CompositeCriterion(
            this,
            CompositeFilterOperator.OR,
            criteria);
    }

    /**
     * Returns the schemaVersion property name.
     * 
     * @return the schemaVersion property name
     */
    public abstract String getSchemaVersionName();

    /**
     * Returns the classHierarchyList property name.
     * 
     * @return the classHierarchyList property name
     */
    public abstract String getClassHierarchyListName();

    /**
     * Converts the entity to a model.
     * 
     * @param entity
     *            the entity
     * @return a model
     */
    public abstract M entityToModel(Entity entity);

    /**
     * Converts the model to an entity.
     * 
     * @param model
     *            the model
     * @return an entity
     */
    public abstract Entity modelToEntity(Object model);

    /**
     * Converts the model to JSON string assuming maxDepth is 0.
     * 
     * @param model
     *            the model
     * 
     * @return JSON string
     */
    public String modelToJson(Object model) {
        return modelToJson(model, 0);
    }

    /**
     * Converts the model to JSON string.
     * 
     * @param model
     *            the model
     * 
     * @param maxDepth
     *            the max depth of ModelRef expanding
     * 
     * @return JSON string
     */
    public String modelToJson(final Object model, int maxDepth) {
        StringBuilder b = new StringBuilder();
        JsonWriter w = new JsonWriter(b, new ModelWriter() {
            @Override
            public void write(JsonWriter writer, Object model, int maxDepth,
                    int currentDepth) {
                invokeModelToJson(
                    Datastore.getModelMeta(model.getClass()),
                    writer,
                    model,
                    maxDepth,
                    currentDepth + 1);
            }
        });
        modelToJson(w, model, maxDepth, 0);
        return b.toString();
    }

    /**
     * Converts the models to JSON string.
     * 
     * @param models
     *            models
     * 
     * @return JSON string
     */
    public String modelsToJson(final Object[] models) {
        return modelsToJson(models, 0);
    }

    /**
     * Converts the models to JSON string.
     * 
     * @param models
     *            models
     * 
     * @param maxDepth
     *            the max depth of ModelRef expanding
     * 
     * @return JSON string
     */
    public String modelsToJson(final Object[] models, int maxDepth) {
        int n = models.length;
        if (n == 0)
            return "[]";
        StringBuilder b = new StringBuilder();
        JsonWriter w = new JsonWriter(b, new ModelWriter() {
            @Override
            public void write(JsonWriter writer, Object model, int maxDepth,
                    int currentDepth) {
                invokeModelToJson(
                    Datastore.getModelMeta(model.getClass()),
                    writer,
                    model,
                    maxDepth,
                    currentDepth + 1);
            }
        });
        b.append("[");
        modelToJson(w, models[0], maxDepth, 0);
        for (int i = 1; i < n; i++) {
            b.append(",");
            modelToJson(w, models[i], maxDepth, 0);
        }
        b.append("]");
        return b.toString();
    }

    /**
     * Converts the models to JSON string.
     * 
     * @param models
     *            models
     * 
     * @return JSON string
     */
    public String modelsToJson(Iterable<?> models) {
        return modelsToJson(models, 0);
    }

    /**
     * Converts the models to JSON string.
     * 
     * @param models
     *            models
     * 
     * @param maxDepth
     *            the max depth of ModelRef expanding
     * 
     * @return JSON string
     */
    public String modelsToJson(final Iterable<?> models, int maxDepth) {
        StringBuilder b = new StringBuilder();
        JsonWriter w = new JsonWriter(b, new ModelWriter() {
            @Override
            public void write(JsonWriter writer, Object model, int maxDepth,
                    int currentDepth) {
                invokeModelToJson(
                    Datastore.getModelMeta(model.getClass()),
                    writer,
                    model,
                    maxDepth,
                    currentDepth + 1);
            }
        });
        b.append("[");
        boolean first = true;
        for (Object o : models) {
            if (first) {
                first = false;
            } else {
                b.append(",");
            }
            modelToJson(w, o, maxDepth, 0);
        }
        b.append("]");
        return b.toString();
    }

    /**
     * Converts the model to JSON string.
     * 
     * @param writer
     *            the writer
     * 
     * @param model
     *            the model
     * 
     * @param maxDepth
     *            the max depth
     * 
     * @param currentDepth
     *            the current depth
     */
    protected abstract void modelToJson(JsonWriter writer, Object model,
            int maxDepth, int currentDepth);

    /**
     * Invoke the modelToJson method.
     * 
     * @param meta
     *            the meta
     * 
     * @param writer
     *            the writer
     * 
     * @param model
     *            the model
     * 
     * @param maxDepth
     *            the max depth
     * 
     * @param currentDepth
     *            the current depth
     */
    protected void invokeModelToJson(ModelMeta<?> meta, JsonWriter writer,
            Object model, int maxDepth, int currentDepth) {
        meta.modelToJson(writer, model, maxDepth, currentDepth);
    }

    /**
     * Converts the JSON string to model.
     * 
     * @param json
     *            the JSON string
     * 
     * @return model
     */
    public M jsonToModel(String json) {
        return jsonToModel(json, 0);
    }

    /**
     * Converts the JSON string to model.
     * 
     * @param json
     *            the JSON string
     * 
     * @param maxDepth
     *            the max depth
     * 
     * @return model
     */
    public M jsonToModel(String json, int maxDepth) {
        return jsonToModel(json, maxDepth, 0);
    }

    /**
     * Converts the JSON string to model array.
     * 
     * @param json
     *            the JSON string
     * 
     * @return model array
     */
    public M[] jsonToModels(String json) {
        return jsonToModels(json, 0);
    }

    /**
     * Converts the JSON string to model array.
     * 
     * @param json
     *            the JSON string
     * 
     * @param maxDepth
     *            the max depth
     * 
     * @return model array
     */
    @SuppressWarnings("unchecked")
    public M[] jsonToModels(String json, int maxDepth) {
        JsonArrayReader ar = new JsonArrayReader(json, new ModelReader() {
            @Override
            public <T> T read(JsonReader reader, Class<T> modelClass,
                    int maxDepth, int currentDepth) {
                return invokeJsonToModel(
                    Datastore.getModelMeta(modelClass),
                    reader,
                    maxDepth,
                    currentDepth + 1);
            }
        });
        M[] ret = (M[]) Array.newInstance(this.getModelClass(), ar.length());
        for (int i = 0; i < ar.length(); i++) {
            ar.setIndex(i);
            ret[i] = jsonToModel(ar.newRootReader(), maxDepth, 0);
        }
        return ret;
    }

    /**
     * Converts the JSON string to model.
     * 
     * @param json
     *            the JSON string
     * 
     * @param maxDepth
     *            the max depth
     * 
     * @param currentDepth
     *            the current depth
     * 
     * @return model
     */
    protected M jsonToModel(String json, int maxDepth, int currentDepth) {
        return jsonToModel(new JsonRootReader(json, new ModelReader() {
            @Override
            public <T> T read(JsonReader reader, Class<T> modelClass,
                    int maxDepth, int currentDepth) {
                return invokeJsonToModel(
                    Datastore.getModelMeta(modelClass),
                    reader,
                    maxDepth,
                    currentDepth + 1);
            }
        }),
            maxDepth,
            currentDepth);
    }

    /**
     * Converts the JSON string to model.
     * 
     * @param reader
     *            the JSON reader
     * 
     * @param maxDepth
     *            the max depth
     * 
     * @param currentDepth
     *            the current depth
     * 
     * @return model
     */
    protected abstract M jsonToModel(JsonRootReader reader, int maxDepth,
            int currentDepth);

    /**
     * Converts the JSON string to model.
     * 
     * @param <T>
     *            the type of model
     * 
     * @param meta
     *            the model meta
     * 
     * @param reader
     *            the JSON reader
     * 
     * @param maxDepth
     *            the max depth
     * 
     * @param currentDepth
     *            the current depth
     * 
     * @return model
     */
    protected <T> T invokeJsonToModel(ModelMeta<T> meta, JsonReader reader,
            int maxDepth, int currentDepth) {
        return meta.jsonToModel(reader.read(), maxDepth, currentDepth);
    }

    /**
     * Returns version property value of the model.
     * 
     * @param model
     *            the model
     * @return a version property value of the model
     */
    protected abstract long getVersion(Object model);

    /**
     * Increments the version property value.
     * 
     * @param model
     *            the model
     */
    protected abstract void incrementVersion(Object model);

    /**
     * This method is called before a model is put to datastore.
     * 
     * @param model
     *            the model
     */
    protected abstract void prePut(Object model);

    /**
     * This method is called after a model is get from datastore.
     * 
     * @param model
     *            the model
     */
    protected abstract void postGet(Object model);

    /**
     * Returns a key of the model.
     * 
     * @param model
     *            the model
     * @return a key of the model
     */
    protected abstract Key getKey(Object model);

    /**
     * Sets the key to the model.
     * 
     * @param model
     *            the model
     * @param key
     *            the key
     */
    protected abstract void setKey(Object model, Key key);

    /**
     * Assigns a key to {@link ModelRef} if necessary.
     * 
     * @param ds
     *            the asynchronous datastore service
     * @param model
     *            the model
     * @throws NullPointerException
     *             if the ds parameter is null or if the model parameter is null
     */
    protected abstract void assignKeyToModelRefIfNecessary(
            AsyncDatastoreService ds, Object model) throws NullPointerException;

    /**
     * Validates the kind of the key.
     * 
     * @param key
     *            the key
     * @throws IllegalArgumentException
     *             if the kind of the key is different from the kind of this
     *             model
     */
    protected void validateKey(Key key) throws IllegalArgumentException {
        if (key != null && !key.getKind().equals(kind)) {
            throw new IllegalArgumentException("The kind("
                + key.getKind()
                + ") of the key("
                + key
                + ") must be "
                + kind
                + ".");
        }
    }

    /**
     * Converts the long to a primitive short.
     * 
     * @param value
     *            the long
     * @return a primitive short
     */
    protected short longToPrimitiveShort(Long value) {
        return value != null ? value.shortValue() : 0;
    }

    /**
     * Converts the long to a short.
     * 
     * @param value
     *            the long
     * @return a short
     */
    protected Short longToShort(Long value) {
        return value != null ? value.shortValue() : null;
    }

    /**
     * Converts the long to a primitive int.
     * 
     * @param value
     *            the long
     * @return a primitive int
     */
    protected int longToPrimitiveInt(Long value) {
        return value != null ? value.intValue() : 0;
    }

    /**
     * Converts the long to an integer.
     * 
     * @param value
     *            the long
     * @return an integer
     */
    protected Integer longToInteger(Long value) {
        return value != null ? value.intValue() : null;
    }

    /**
     * Converts the long to a primitive long.
     * 
     * @param value
     *            the long
     * @return a primitive long
     */
    protected long longToPrimitiveLong(Long value) {
        return value != null ? value : 0;
    }

    /**
     * Converts the double to a primitive float.
     * 
     * @param value
     *            the double
     * @return a primitive float
     */
    protected float doubleToPrimitiveFloat(Double value) {
        return value != null ? value.floatValue() : 0;
    }

    /**
     * Converts the double to a float.
     * 
     * @param value
     *            the double
     * @return a float
     */
    protected Float doubleToFloat(Double value) {
        return value != null ? value.floatValue() : null;
    }

    /**
     * Converts the double to a primitive double.
     * 
     * @param value
     *            the double
     * @return a primitive double
     */
    protected double doubleToPrimitiveDouble(Double value) {
        return value != null ? value : 0;
    }

    /**
     * Converts the boolean to a primitive boolean.
     * 
     * @param value
     *            the boolean
     * @return a primitive boolean
     */
    protected boolean booleanToPrimitiveBoolean(Boolean value) {
        return value != null ? value : false;
    }

    /**
     * Converts the {@link Enum} to a string representation.
     * 
     * @param value
     *            the {@link Enum}
     * @return a string representation
     */
    protected String enumToString(Enum<?> value) {
        return value != null ? value.name() : null;
    }

    /**
     * Converts the string to an {@link Enum}.
     * 
     * @param <T>
     *            the enum type
     * @param clazz
     *            the enum class
     * @param value
     *            the String
     * @return an {@link Enum}
     */
    protected <T extends Enum<T>> T stringToEnum(Class<T> clazz, String value) {
        return value != null ? Enum.valueOf(clazz, value) : null;
    }

    /**
     * Converts the text to a string
     * 
     * @param value
     *            the text
     * @return a string
     */
    protected String textToString(Text value) {
        return value != null ? value.getValue() : null;
    }

    /**
     * Converts the string to a text
     * 
     * @param value
     *            the string
     * @return a text
     */
    protected Text stringToText(String value) {
        return value != null ? new Text(value) : null;
    }

    /**
     * Converts the short blob to an array of bytes.
     * 
     * @param value
     *            the short blob
     * @return an array of bytes
     */
    protected byte[] shortBlobToBytes(ShortBlob value) {
        return value != null ? value.getBytes() : null;
    }

    /**
     * Converts the array of bytes to a short blob.
     * 
     * @param value
     *            the array of bytes
     * @return a short blob
     */
    protected ShortBlob bytesToShortBlob(byte[] value) {
        return value != null ? new ShortBlob(value) : null;
    }

    /**
     * Converts the blob to an array of bytes.
     * 
     * @param value
     *            the blob
     * @return an array of bytes
     */
    protected byte[] blobToBytes(Blob value) {
        return value != null ? value.getBytes() : null;
    }

    /**
     * Converts the array of bytes to a blob.
     * 
     * @param value
     *            the array of bytes
     * @return a blob
     */
    protected Blob bytesToBlob(byte[] value) {
        return value != null ? new Blob(value) : null;
    }

    /**
     * Converts the short blob to a serializable object.
     * 
     * @param <T>
     *            the type
     * @param value
     *            the short blob
     * @return a serializable object
     */
    @SuppressWarnings("unchecked")
    protected <T> T shortBlobToSerializable(ShortBlob value) {
        return value != null ? (T) ByteUtil.toObject(value.getBytes()) : null;
    }

    /**
     * Converts the serializable object to a short blob.
     * 
     * @param value
     *            the serializable object
     * @return a short blob
     */
    protected ShortBlob serializableToShortBlob(Object value) {
        return value != null
            ? new ShortBlob(ByteUtil.toByteArray(value))
            : null;
    }

    /**
     * Converts the blob to a serializable object.
     * 
     * @param <T>
     *            the type
     * @param value
     *            the blob
     * @return a serializable object
     */
    @SuppressWarnings("unchecked")
    protected <T> T blobToSerializable(Blob value) {
        return value != null ? (T) ByteUtil.toObject(value.getBytes()) : null;
    }

    /**
     * Converts the serializable object to a blob.
     * 
     * @param value
     *            the serializable object
     * @return a blob
     */
    protected Blob serializableToBlob(Object value) {
        return value != null ? new Blob(ByteUtil.toByteArray(value)) : null;
    }

    /**
     * Converts the list to an array list.
     * 
     * @param <T>
     *            the type
     * @param clazz
     *            the class
     * @param value
     *            the list
     * @return an array list
     */
    @SuppressWarnings("unchecked")
    protected <T> ArrayList<T> toList(Class<T> clazz, Object value) {
        if (value == null) {
            return new ArrayList<T>();
        }
        return (ArrayList<T>) value;
    }

    /**
     * Converts the list of long to a list of short.
     * 
     * @param value
     *            the list of long
     * @return a list of short
     */
    @SuppressWarnings("unchecked")
    protected ArrayList<Short> longListToShortList(Object value) {
        List<Long> v = (List<Long>) value;
        if (v == null) {
            return new ArrayList<Short>();
        }
        ArrayList<Short> collection = new ArrayList<Short>(v.size());
        int size = v.size();
        for (int i = 0; i < size; i++) {
            Long l = v.get(i);
            collection.add(l != null ? l.shortValue() : null);
        }
        return collection;
    }

    /**
     * Converts the list of long to a list of integer.
     * 
     * @param value
     *            the list of long
     * @return a list of integer
     */
    @SuppressWarnings("unchecked")
    protected ArrayList<Integer> longListToIntegerList(Object value) {
        List<Long> v = (List<Long>) value;
        if (v == null) {
            return new ArrayList<Integer>();
        }
        ArrayList<Integer> collection = new ArrayList<Integer>(v.size());
        int size = v.size();
        for (int i = 0; i < size; i++) {
            Long l = v.get(i);
            collection.add(l != null ? l.intValue() : null);
        }
        return collection;
    }

    /**
     * Converts the list of double to a list of float.
     * 
     * @param value
     *            the list of double
     * @return a list of float
     */
    @SuppressWarnings("unchecked")
    protected ArrayList<Float> doubleListToFloatList(Object value) {
        List<Double> v = (List<Double>) value;
        if (v == null) {
            return new ArrayList<Float>();
        }
        ArrayList<Float> collection = new ArrayList<Float>(v.size());
        int size = v.size();
        for (int i = 0; i < size; i++) {
            Double d = v.get(i);
            collection.add(d != null ? d.floatValue() : null);
        }
        return collection;
    }

    /**
     * Converts the list of {@link Enum}s to a list of strings.
     * 
     * @param value
     *            the list of {@link Enum}
     * @return a list of strings
     */
    @SuppressWarnings("unchecked")
    protected List<String> enumListToStringList(Object value) {
        List<Enum<?>> v = (List<Enum<?>>) value;
        if (v == null) {
            return new ArrayList<String>();
        }
        List<String> list = new ArrayList<String>(v.size());
        for (Enum<?> e : v) {
            list.add(e.name());
        }
        return list;
    }

    /**
     * Converts the list of strings to a list of {@link Enum}s.
     * 
     * @param <T>
     *            the enum type
     * @param clazz
     *            the enum class
     * @param value
     *            the list of strings
     * @return a list of {@link Enum}s
     */
    @SuppressWarnings("unchecked")
    protected <T extends Enum<T>> List<T> stringListToEnumList(Class<T> clazz,
            Object value) {
        List<String> v = (List<String>) value;
        if (v == null) {
            return new ArrayList<T>();
        }
        List<T> list = new ArrayList<T>(v.size());
        for (String s : v) {
            list.add(s != null ? Enum.valueOf(clazz, s) : null);
        }
        return list;
    }

    /**
     * Returns the bean descriptor.
     * 
     * @return the bean descriptor
     */
    protected BeanDesc getBeanDesc() {
        if (beanDesc != null) {
            return beanDesc;
        }
        beanDesc = BeanUtil.getBeanDesc(modelClass);
        return beanDesc;
    }

    /**
     * Determines if the property is cipher.
     * 
     * @param propertyName
     *            the property name
     * @return whether property is cipher
     * @since 1.0.6
     */
    protected boolean isCipherProperty(String propertyName) {
        return false;
    }

    /**
     * Encrypt the text.
     * 
     * @param text
     *            the text
     * @return the encrypted text
     * @since 1.0.6
     */
    protected String encrypt(String text) {
        Cipher c = CipherFactory.getFactory().createCipher();
        return c.encrypt(text);
    }

    /**
     * Encrypt the text.
     * 
     * @param text
     *            the text
     * @return the encrypted text
     * @since 1.0.6
     */
    protected Text encrypt(Text text) {
        if (text == null)
            return null;
        return new Text(encrypt(text.getValue()));
    }

    /**
     * Decrypt the encrypted text.
     * 
     * @param encryptedText
     *            the encrypted text
     * @return the decrypted text
     * @since 1.0.6
     */
    protected String decrypt(String encryptedText) {
        Cipher c = CipherFactory.getFactory().createCipher();
        return c.decrypt(encryptedText);
    }

    /**
     * Decrypt the encrypted text.
     * 
     * @param encryptedText
     *            the encrypted text
     * @return the decrypted text
     * @since 1.0.6
     */
    protected Text decrypt(Text encryptedText) {
        if (encryptedText == null)
            return null;
        return new Text(decrypt(encryptedText.getValue()));
    }
}
>>>>>>> 76aa07461566a5976980e6696204781271955163
