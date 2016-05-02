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
/**
 * Copyright (C) 2009-2013 BIMserver.org
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.bimserver.models.ifc2x3tc1;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Ifc Space Program</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.bimserver.models.ifc2x3tc1.IfcSpaceProgram#getSpaceProgramIdentifier <em>Space Program Identifier</em>}</li>
 *   <li>{@link org.bimserver.models.ifc2x3tc1.IfcSpaceProgram#getMaxRequiredArea <em>Max Required Area</em>}</li>
 *   <li>{@link org.bimserver.models.ifc2x3tc1.IfcSpaceProgram#getMaxRequiredAreaAsString <em>Max Required Area As String</em>}</li>
 *   <li>{@link org.bimserver.models.ifc2x3tc1.IfcSpaceProgram#getMinRequiredArea <em>Min Required Area</em>}</li>
 *   <li>{@link org.bimserver.models.ifc2x3tc1.IfcSpaceProgram#getMinRequiredAreaAsString <em>Min Required Area As String</em>}</li>
 *   <li>{@link org.bimserver.models.ifc2x3tc1.IfcSpaceProgram#getRequestedLocation <em>Requested Location</em>}</li>
 *   <li>{@link org.bimserver.models.ifc2x3tc1.IfcSpaceProgram#getStandardRequiredArea <em>Standard Required Area</em>}</li>
 *   <li>{@link org.bimserver.models.ifc2x3tc1.IfcSpaceProgram#getStandardRequiredAreaAsString <em>Standard Required Area As String</em>}</li>
 *   <li>{@link org.bimserver.models.ifc2x3tc1.IfcSpaceProgram#getHasInteractionReqsFrom <em>Has Interaction Reqs From</em>}</li>
 *   <li>{@link org.bimserver.models.ifc2x3tc1.IfcSpaceProgram#getHasInteractionReqsTo <em>Has Interaction Reqs To</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcSpaceProgram()
 * @model
 * @generated
 */
public interface IfcSpaceProgram extends IfcControl {
	/**
	 * Returns the value of the '<em><b>Space Program Identifier</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Space Program Identifier</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Space Program Identifier</em>' attribute.
	 * @see #setSpaceProgramIdentifier(String)
	 * @see org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcSpaceProgram_SpaceProgramIdentifier()
	 * @model
	 * @generated
	 */
	String getSpaceProgramIdentifier();

	/**
	 * Sets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcSpaceProgram#getSpaceProgramIdentifier <em>Space Program Identifier</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Space Program Identifier</em>' attribute.
	 * @see #getSpaceProgramIdentifier()
	 * @generated
	 */
	void setSpaceProgramIdentifier(String value);

	/**
	 * Returns the value of the '<em><b>Max Required Area</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Max Required Area</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Max Required Area</em>' attribute.
	 * @see #isSetMaxRequiredArea()
	 * @see #unsetMaxRequiredArea()
	 * @see #setMaxRequiredArea(double)
	 * @see org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcSpaceProgram_MaxRequiredArea()
	 * @model unsettable="true"
	 * @generated
	 */
	double getMaxRequiredArea();

	/**
	 * Sets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcSpaceProgram#getMaxRequiredArea <em>Max Required Area</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Max Required Area</em>' attribute.
	 * @see #isSetMaxRequiredArea()
	 * @see #unsetMaxRequiredArea()
	 * @see #getMaxRequiredArea()
	 * @generated
	 */
	void setMaxRequiredArea(double value);

	/**
	 * Unsets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcSpaceProgram#getMaxRequiredArea <em>Max Required Area</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetMaxRequiredArea()
	 * @see #getMaxRequiredArea()
	 * @see #setMaxRequiredArea(double)
	 * @generated
	 */
	void unsetMaxRequiredArea();

	/**
	 * Returns whether the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcSpaceProgram#getMaxRequiredArea <em>Max Required Area</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Max Required Area</em>' attribute is set.
	 * @see #unsetMaxRequiredArea()
	 * @see #getMaxRequiredArea()
	 * @see #setMaxRequiredArea(double)
	 * @generated
	 */
	boolean isSetMaxRequiredArea();

	/**
	 * Returns the value of the '<em><b>Max Required Area As String</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Max Required Area As String</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Max Required Area As String</em>' attribute.
	 * @see #isSetMaxRequiredAreaAsString()
	 * @see #unsetMaxRequiredAreaAsString()
	 * @see #setMaxRequiredAreaAsString(String)
	 * @see org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcSpaceProgram_MaxRequiredAreaAsString()
	 * @model unsettable="true"
	 * @generated
	 */
	String getMaxRequiredAreaAsString();

	/**
	 * Sets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcSpaceProgram#getMaxRequiredAreaAsString <em>Max Required Area As String</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Max Required Area As String</em>' attribute.
	 * @see #isSetMaxRequiredAreaAsString()
	 * @see #unsetMaxRequiredAreaAsString()
	 * @see #getMaxRequiredAreaAsString()
	 * @generated
	 */
	void setMaxRequiredAreaAsString(String value);

	/**
	 * Unsets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcSpaceProgram#getMaxRequiredAreaAsString <em>Max Required Area As String</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetMaxRequiredAreaAsString()
	 * @see #getMaxRequiredAreaAsString()
	 * @see #setMaxRequiredAreaAsString(String)
	 * @generated
	 */
	void unsetMaxRequiredAreaAsString();

	/**
	 * Returns whether the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcSpaceProgram#getMaxRequiredAreaAsString <em>Max Required Area As String</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Max Required Area As String</em>' attribute is set.
	 * @see #unsetMaxRequiredAreaAsString()
	 * @see #getMaxRequiredAreaAsString()
	 * @see #setMaxRequiredAreaAsString(String)
	 * @generated
	 */
	boolean isSetMaxRequiredAreaAsString();

	/**
	 * Returns the value of the '<em><b>Min Required Area</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Min Required Area</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Min Required Area</em>' attribute.
	 * @see #isSetMinRequiredArea()
	 * @see #unsetMinRequiredArea()
	 * @see #setMinRequiredArea(double)
	 * @see org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcSpaceProgram_MinRequiredArea()
	 * @model unsettable="true"
	 * @generated
	 */
	double getMinRequiredArea();

	/**
	 * Sets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcSpaceProgram#getMinRequiredArea <em>Min Required Area</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Min Required Area</em>' attribute.
	 * @see #isSetMinRequiredArea()
	 * @see #unsetMinRequiredArea()
	 * @see #getMinRequiredArea()
	 * @generated
	 */
	void setMinRequiredArea(double value);

	/**
	 * Unsets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcSpaceProgram#getMinRequiredArea <em>Min Required Area</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetMinRequiredArea()
	 * @see #getMinRequiredArea()
	 * @see #setMinRequiredArea(double)
	 * @generated
	 */
	void unsetMinRequiredArea();

	/**
	 * Returns whether the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcSpaceProgram#getMinRequiredArea <em>Min Required Area</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Min Required Area</em>' attribute is set.
	 * @see #unsetMinRequiredArea()
	 * @see #getMinRequiredArea()
	 * @see #setMinRequiredArea(double)
	 * @generated
	 */
	boolean isSetMinRequiredArea();

	/**
	 * Returns the value of the '<em><b>Min Required Area As String</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Min Required Area As String</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Min Required Area As String</em>' attribute.
	 * @see #isSetMinRequiredAreaAsString()
	 * @see #unsetMinRequiredAreaAsString()
	 * @see #setMinRequiredAreaAsString(String)
	 * @see org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcSpaceProgram_MinRequiredAreaAsString()
	 * @model unsettable="true"
	 * @generated
	 */
	String getMinRequiredAreaAsString();

	/**
	 * Sets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcSpaceProgram#getMinRequiredAreaAsString <em>Min Required Area As String</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Min Required Area As String</em>' attribute.
	 * @see #isSetMinRequiredAreaAsString()
	 * @see #unsetMinRequiredAreaAsString()
	 * @see #getMinRequiredAreaAsString()
	 * @generated
	 */
	void setMinRequiredAreaAsString(String value);

	/**
	 * Unsets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcSpaceProgram#getMinRequiredAreaAsString <em>Min Required Area As String</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetMinRequiredAreaAsString()
	 * @see #getMinRequiredAreaAsString()
	 * @see #setMinRequiredAreaAsString(String)
	 * @generated
	 */
	void unsetMinRequiredAreaAsString();

	/**
	 * Returns whether the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcSpaceProgram#getMinRequiredAreaAsString <em>Min Required Area As String</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Min Required Area As String</em>' attribute is set.
	 * @see #unsetMinRequiredAreaAsString()
	 * @see #getMinRequiredAreaAsString()
	 * @see #setMinRequiredAreaAsString(String)
	 * @generated
	 */
	boolean isSetMinRequiredAreaAsString();

	/**
	 * Returns the value of the '<em><b>Requested Location</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Requested Location</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Requested Location</em>' reference.
	 * @see #isSetRequestedLocation()
	 * @see #unsetRequestedLocation()
	 * @see #setRequestedLocation(IfcSpatialStructureElement)
	 * @see org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcSpaceProgram_RequestedLocation()
	 * @model unsettable="true"
	 * @generated
	 */
	IfcSpatialStructureElement getRequestedLocation();

	/**
	 * Sets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcSpaceProgram#getRequestedLocation <em>Requested Location</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Requested Location</em>' reference.
	 * @see #isSetRequestedLocation()
	 * @see #unsetRequestedLocation()
	 * @see #getRequestedLocation()
	 * @generated
	 */
	void setRequestedLocation(IfcSpatialStructureElement value);

	/**
	 * Unsets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcSpaceProgram#getRequestedLocation <em>Requested Location</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetRequestedLocation()
	 * @see #getRequestedLocation()
	 * @see #setRequestedLocation(IfcSpatialStructureElement)
	 * @generated
	 */
	void unsetRequestedLocation();

	/**
	 * Returns whether the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcSpaceProgram#getRequestedLocation <em>Requested Location</em>}' reference is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Requested Location</em>' reference is set.
	 * @see #unsetRequestedLocation()
	 * @see #getRequestedLocation()
	 * @see #setRequestedLocation(IfcSpatialStructureElement)
	 * @generated
	 */
	boolean isSetRequestedLocation();

	/**
	 * Returns the value of the '<em><b>Standard Required Area</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Standard Required Area</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Standard Required Area</em>' attribute.
	 * @see #setStandardRequiredArea(double)
	 * @see org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcSpaceProgram_StandardRequiredArea()
	 * @model
	 * @generated
	 */
	double getStandardRequiredArea();

	/**
	 * Sets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcSpaceProgram#getStandardRequiredArea <em>Standard Required Area</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Standard Required Area</em>' attribute.
	 * @see #getStandardRequiredArea()
	 * @generated
	 */
	void setStandardRequiredArea(double value);

	/**
	 * Returns the value of the '<em><b>Standard Required Area As String</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Standard Required Area As String</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Standard Required Area As String</em>' attribute.
	 * @see #setStandardRequiredAreaAsString(String)
	 * @see org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcSpaceProgram_StandardRequiredAreaAsString()
	 * @model
	 * @generated
	 */
	String getStandardRequiredAreaAsString();

	/**
	 * Sets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcSpaceProgram#getStandardRequiredAreaAsString <em>Standard Required Area As String</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Standard Required Area As String</em>' attribute.
	 * @see #getStandardRequiredAreaAsString()
	 * @generated
	 */
	void setStandardRequiredAreaAsString(String value);

	/**
	 * Returns the value of the '<em><b>Has Interaction Reqs From</b></em>' reference list.
	 * The list contents are of type {@link org.bimserver.models.ifc2x3tc1.IfcRelInteractionRequirements}.
	 * It is bidirectional and its opposite is '{@link org.bimserver.models.ifc2x3tc1.IfcRelInteractionRequirements#getRelatedSpaceProgram <em>Related Space Program</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Has Interaction Reqs From</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Has Interaction Reqs From</em>' reference list.
	 * @see #isSetHasInteractionReqsFrom()
	 * @see #unsetHasInteractionReqsFrom()
	 * @see org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcSpaceProgram_HasInteractionReqsFrom()
	 * @see org.bimserver.models.ifc2x3tc1.IfcRelInteractionRequirements#getRelatedSpaceProgram
	 * @model opposite="RelatedSpaceProgram" unsettable="true"
	 * @generated
	 */
	EList<IfcRelInteractionRequirements> getHasInteractionReqsFrom();

	/**
	 * Unsets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcSpaceProgram#getHasInteractionReqsFrom <em>Has Interaction Reqs From</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetHasInteractionReqsFrom()
	 * @see #getHasInteractionReqsFrom()
	 * @generated
	 */
	void unsetHasInteractionReqsFrom();

	/**
	 * Returns whether the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcSpaceProgram#getHasInteractionReqsFrom <em>Has Interaction Reqs From</em>}' reference list is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Has Interaction Reqs From</em>' reference list is set.
	 * @see #unsetHasInteractionReqsFrom()
	 * @see #getHasInteractionReqsFrom()
	 * @generated
	 */
	boolean isSetHasInteractionReqsFrom();

	/**
	 * Returns the value of the '<em><b>Has Interaction Reqs To</b></em>' reference list.
	 * The list contents are of type {@link org.bimserver.models.ifc2x3tc1.IfcRelInteractionRequirements}.
	 * It is bidirectional and its opposite is '{@link org.bimserver.models.ifc2x3tc1.IfcRelInteractionRequirements#getRelatingSpaceProgram <em>Relating Space Program</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Has Interaction Reqs To</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Has Interaction Reqs To</em>' reference list.
	 * @see #isSetHasInteractionReqsTo()
	 * @see #unsetHasInteractionReqsTo()
	 * @see org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcSpaceProgram_HasInteractionReqsTo()
	 * @see org.bimserver.models.ifc2x3tc1.IfcRelInteractionRequirements#getRelatingSpaceProgram
	 * @model opposite="RelatingSpaceProgram" unsettable="true"
	 * @generated
	 */
	EList<IfcRelInteractionRequirements> getHasInteractionReqsTo();

	/**
	 * Unsets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcSpaceProgram#getHasInteractionReqsTo <em>Has Interaction Reqs To</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetHasInteractionReqsTo()
	 * @see #getHasInteractionReqsTo()
	 * @generated
	 */
	void unsetHasInteractionReqsTo();

	/**
	 * Returns whether the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcSpaceProgram#getHasInteractionReqsTo <em>Has Interaction Reqs To</em>}' reference list is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Has Interaction Reqs To</em>' reference list is set.
	 * @see #unsetHasInteractionReqsTo()
	 * @see #getHasInteractionReqsTo()
	 * @generated
	 */
	boolean isSetHasInteractionReqsTo();

} // IfcSpaceProgram
>>>>>>> 76aa07461566a5976980e6696204781271955163

