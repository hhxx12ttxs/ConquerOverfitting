/* MACHINE GENERATED FILE, DO NOT EDIT */

package org.lwjgl.opengl;

import org.lwjgl.*;
import java.nio.*;

public final class EXTDirectStateAccess {

	/**
	 *  Accepted by the &lt;pname&gt; parameter of GetBooleanIndexedvEXT,
	 *  GetIntegerIndexedvEXT, GetFloatIndexedvEXT, GetDoubleIndexedvEXT:
	 *  GetBooleani_v, GetIntegeri_v, GetFloati_vEXT, GetDoublei_vEXT:
	 */
	public static final int GL_PROGRAM_MATRIX_EXT = 0x8E2D,
		GL_TRANSPOSE_PROGRAM_MATRIX_EXT = 0x8E2E,
		GL_PROGRAM_MATRIX_STACK_DEPTH_EXT = 0x8E2F;

	private EXTDirectStateAccess() {}

	public static void glClientAttribDefaultEXT(int mask) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glClientAttribDefaultEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		nglClientAttribDefaultEXT(mask, function_pointer);
	}
	static native void nglClientAttribDefaultEXT(int mask, long function_pointer);

	public static void glPushClientAttribDefaultEXT(int mask) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glPushClientAttribDefaultEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		nglPushClientAttribDefaultEXT(mask, function_pointer);
	}
	static native void nglPushClientAttribDefaultEXT(int mask, long function_pointer);

	public static void glMatrixLoadEXT(int matrixMode, FloatBuffer m) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMatrixLoadfEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		BufferChecks.checkBuffer(m, 16);
		nglMatrixLoadfEXT(matrixMode, MemoryUtil.getAddress(m), function_pointer);
	}
	static native void nglMatrixLoadfEXT(int matrixMode, long m, long function_pointer);

	public static void glMatrixLoadEXT(int matrixMode, DoubleBuffer m) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMatrixLoaddEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		BufferChecks.checkBuffer(m, 16);
		nglMatrixLoaddEXT(matrixMode, MemoryUtil.getAddress(m), function_pointer);
	}
	static native void nglMatrixLoaddEXT(int matrixMode, long m, long function_pointer);

	public static void glMatrixMultEXT(int matrixMode, FloatBuffer m) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMatrixMultfEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		BufferChecks.checkBuffer(m, 16);
		nglMatrixMultfEXT(matrixMode, MemoryUtil.getAddress(m), function_pointer);
	}
	static native void nglMatrixMultfEXT(int matrixMode, long m, long function_pointer);

	public static void glMatrixMultEXT(int matrixMode, DoubleBuffer m) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMatrixMultdEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		BufferChecks.checkBuffer(m, 16);
		nglMatrixMultdEXT(matrixMode, MemoryUtil.getAddress(m), function_pointer);
	}
	static native void nglMatrixMultdEXT(int matrixMode, long m, long function_pointer);

	public static void glMatrixLoadIdentityEXT(int matrixMode) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMatrixLoadIdentityEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		nglMatrixLoadIdentityEXT(matrixMode, function_pointer);
	}
	static native void nglMatrixLoadIdentityEXT(int matrixMode, long function_pointer);

	public static void glMatrixRotatefEXT(int matrixMode, float angle, float x, float y, float z) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMatrixRotatefEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		nglMatrixRotatefEXT(matrixMode, angle, x, y, z, function_pointer);
	}
	static native void nglMatrixRotatefEXT(int matrixMode, float angle, float x, float y, float z, long function_pointer);

	public static void glMatrixRotatedEXT(int matrixMode, double angle, double x, double y, double z) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMatrixRotatedEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		nglMatrixRotatedEXT(matrixMode, angle, x, y, z, function_pointer);
	}
	static native void nglMatrixRotatedEXT(int matrixMode, double angle, double x, double y, double z, long function_pointer);

	public static void glMatrixScalefEXT(int matrixMode, float x, float y, float z) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMatrixScalefEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		nglMatrixScalefEXT(matrixMode, x, y, z, function_pointer);
	}
	static native void nglMatrixScalefEXT(int matrixMode, float x, float y, float z, long function_pointer);

	public static void glMatrixScaledEXT(int matrixMode, double x, double y, double z) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMatrixScaledEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		nglMatrixScaledEXT(matrixMode, x, y, z, function_pointer);
	}
	static native void nglMatrixScaledEXT(int matrixMode, double x, double y, double z, long function_pointer);

	public static void glMatrixTranslatefEXT(int matrixMode, float x, float y, float z) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMatrixTranslatefEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		nglMatrixTranslatefEXT(matrixMode, x, y, z, function_pointer);
	}
	static native void nglMatrixTranslatefEXT(int matrixMode, float x, float y, float z, long function_pointer);

	public static void glMatrixTranslatedEXT(int matrixMode, double x, double y, double z) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMatrixTranslatedEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		nglMatrixTranslatedEXT(matrixMode, x, y, z, function_pointer);
	}
	static native void nglMatrixTranslatedEXT(int matrixMode, double x, double y, double z, long function_pointer);

	public static void glMatrixOrthoEXT(int matrixMode, double l, double r, double b, double t, double n, double f) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMatrixOrthoEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		nglMatrixOrthoEXT(matrixMode, l, r, b, t, n, f, function_pointer);
	}
	static native void nglMatrixOrthoEXT(int matrixMode, double l, double r, double b, double t, double n, double f, long function_pointer);

	public static void glMatrixFrustumEXT(int matrixMode, double l, double r, double b, double t, double n, double f) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMatrixFrustumEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		nglMatrixFrustumEXT(matrixMode, l, r, b, t, n, f, function_pointer);
	}
	static native void nglMatrixFrustumEXT(int matrixMode, double l, double r, double b, double t, double n, double f, long function_pointer);

	public static void glMatrixPushEXT(int matrixMode) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMatrixPushEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		nglMatrixPushEXT(matrixMode, function_pointer);
	}
	static native void nglMatrixPushEXT(int matrixMode, long function_pointer);

	public static void glMatrixPopEXT(int matrixMode) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMatrixPopEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		nglMatrixPopEXT(matrixMode, function_pointer);
	}
	static native void nglMatrixPopEXT(int matrixMode, long function_pointer);

	public static void glTextureParameteriEXT(int texture, int target, int pname, int param) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glTextureParameteriEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		nglTextureParameteriEXT(texture, target, pname, param, function_pointer);
	}
	static native void nglTextureParameteriEXT(int texture, int target, int pname, int param, long function_pointer);

	public static void glTextureParameterEXT(int texture, int target, int pname, IntBuffer param) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glTextureParameterivEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		BufferChecks.checkBuffer(param, 4);
		nglTextureParameterivEXT(texture, target, pname, MemoryUtil.getAddress(param), function_pointer);
	}
	static native void nglTextureParameterivEXT(int texture, int target, int pname, long param, long function_pointer);

	public static void glTextureParameterfEXT(int texture, int target, int pname, float param) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glTextureParameterfEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		nglTextureParameterfEXT(texture, target, pname, param, function_pointer);
	}
	static native void nglTextureParameterfEXT(int texture, int target, int pname, float param, long function_pointer);

	public static void glTextureParameterEXT(int texture, int target, int pname, FloatBuffer param) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glTextureParameterfvEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		BufferChecks.checkBuffer(param, 4);
		nglTextureParameterfvEXT(texture, target, pname, MemoryUtil.getAddress(param), function_pointer);
	}
	static native void nglTextureParameterfvEXT(int texture, int target, int pname, long param, long function_pointer);

	public static void glTextureImage1DEXT(int texture, int target, int level, int internalformat, int width, int border, int format, int type, ByteBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glTextureImage1DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOdisabled(caps);
		if (pixels != null)
			BufferChecks.checkBuffer(pixels, GLChecks.calculateTexImage1DStorage(pixels, format, type, width));
		nglTextureImage1DEXT(texture, target, level, internalformat, width, border, format, type, MemoryUtil.getAddressSafe(pixels), function_pointer);
	}
	public static void glTextureImage1DEXT(int texture, int target, int level, int internalformat, int width, int border, int format, int type, DoubleBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glTextureImage1DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOdisabled(caps);
		if (pixels != null)
			BufferChecks.checkBuffer(pixels, GLChecks.calculateTexImage1DStorage(pixels, format, type, width));
		nglTextureImage1DEXT(texture, target, level, internalformat, width, border, format, type, MemoryUtil.getAddressSafe(pixels), function_pointer);
	}
	public static void glTextureImage1DEXT(int texture, int target, int level, int internalformat, int width, int border, int format, int type, FloatBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glTextureImage1DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOdisabled(caps);
		if (pixels != null)
			BufferChecks.checkBuffer(pixels, GLChecks.calculateTexImage1DStorage(pixels, format, type, width));
		nglTextureImage1DEXT(texture, target, level, internalformat, width, border, format, type, MemoryUtil.getAddressSafe(pixels), function_pointer);
	}
	public static void glTextureImage1DEXT(int texture, int target, int level, int internalformat, int width, int border, int format, int type, IntBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glTextureImage1DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOdisabled(caps);
		if (pixels != null)
			BufferChecks.checkBuffer(pixels, GLChecks.calculateTexImage1DStorage(pixels, format, type, width));
		nglTextureImage1DEXT(texture, target, level, internalformat, width, border, format, type, MemoryUtil.getAddressSafe(pixels), function_pointer);
	}
	public static void glTextureImage1DEXT(int texture, int target, int level, int internalformat, int width, int border, int format, int type, ShortBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glTextureImage1DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOdisabled(caps);
		if (pixels != null)
			BufferChecks.checkBuffer(pixels, GLChecks.calculateTexImage1DStorage(pixels, format, type, width));
		nglTextureImage1DEXT(texture, target, level, internalformat, width, border, format, type, MemoryUtil.getAddressSafe(pixels), function_pointer);
	}
	static native void nglTextureImage1DEXT(int texture, int target, int level, int internalformat, int width, int border, int format, int type, long pixels, long function_pointer);
	public static void glTextureImage1DEXT(int texture, int target, int level, int internalformat, int width, int border, int format, int type, long pixels_buffer_offset) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glTextureImage1DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOenabled(caps);
		nglTextureImage1DEXTBO(texture, target, level, internalformat, width, border, format, type, pixels_buffer_offset, function_pointer);
	}
	static native void nglTextureImage1DEXTBO(int texture, int target, int level, int internalformat, int width, int border, int format, int type, long pixels_buffer_offset, long function_pointer);

	public static void glTextureImage2DEXT(int texture, int target, int level, int internalformat, int width, int height, int border, int format, int type, ByteBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glTextureImage2DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOdisabled(caps);
		if (pixels != null)
			BufferChecks.checkBuffer(pixels, GLChecks.calculateTexImage2DStorage(pixels, format, type, width, height));
		nglTextureImage2DEXT(texture, target, level, internalformat, width, height, border, format, type, MemoryUtil.getAddressSafe(pixels), function_pointer);
	}
	public static void glTextureImage2DEXT(int texture, int target, int level, int internalformat, int width, int height, int border, int format, int type, DoubleBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glTextureImage2DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOdisabled(caps);
		if (pixels != null)
			BufferChecks.checkBuffer(pixels, GLChecks.calculateTexImage2DStorage(pixels, format, type, width, height));
		nglTextureImage2DEXT(texture, target, level, internalformat, width, height, border, format, type, MemoryUtil.getAddressSafe(pixels), function_pointer);
	}
	public static void glTextureImage2DEXT(int texture, int target, int level, int internalformat, int width, int height, int border, int format, int type, FloatBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glTextureImage2DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOdisabled(caps);
		if (pixels != null)
			BufferChecks.checkBuffer(pixels, GLChecks.calculateTexImage2DStorage(pixels, format, type, width, height));
		nglTextureImage2DEXT(texture, target, level, internalformat, width, height, border, format, type, MemoryUtil.getAddressSafe(pixels), function_pointer);
	}
	public static void glTextureImage2DEXT(int texture, int target, int level, int internalformat, int width, int height, int border, int format, int type, IntBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glTextureImage2DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOdisabled(caps);
		if (pixels != null)
			BufferChecks.checkBuffer(pixels, GLChecks.calculateTexImage2DStorage(pixels, format, type, width, height));
		nglTextureImage2DEXT(texture, target, level, internalformat, width, height, border, format, type, MemoryUtil.getAddressSafe(pixels), function_pointer);
	}
	public static void glTextureImage2DEXT(int texture, int target, int level, int internalformat, int width, int height, int border, int format, int type, ShortBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glTextureImage2DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOdisabled(caps);
		if (pixels != null)
			BufferChecks.checkBuffer(pixels, GLChecks.calculateTexImage2DStorage(pixels, format, type, width, height));
		nglTextureImage2DEXT(texture, target, level, internalformat, width, height, border, format, type, MemoryUtil.getAddressSafe(pixels), function_pointer);
	}
	static native void nglTextureImage2DEXT(int texture, int target, int level, int internalformat, int width, int height, int border, int format, int type, long pixels, long function_pointer);
	public static void glTextureImage2DEXT(int texture, int target, int level, int internalformat, int width, int height, int border, int format, int type, long pixels_buffer_offset) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glTextureImage2DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOenabled(caps);
		nglTextureImage2DEXTBO(texture, target, level, internalformat, width, height, border, format, type, pixels_buffer_offset, function_pointer);
	}
	static native void nglTextureImage2DEXTBO(int texture, int target, int level, int internalformat, int width, int height, int border, int format, int type, long pixels_buffer_offset, long function_pointer);

	public static void glTextureSubImage1DEXT(int texture, int target, int level, int xoffset, int width, int format, int type, ByteBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glTextureSubImage1DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOdisabled(caps);
		BufferChecks.checkBuffer(pixels, GLChecks.calculateImageStorage(pixels, format, type, width, 1, 1));
		nglTextureSubImage1DEXT(texture, target, level, xoffset, width, format, type, MemoryUtil.getAddress(pixels), function_pointer);
	}
	public static void glTextureSubImage1DEXT(int texture, int target, int level, int xoffset, int width, int format, int type, DoubleBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glTextureSubImage1DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOdisabled(caps);
		BufferChecks.checkBuffer(pixels, GLChecks.calculateImageStorage(pixels, format, type, width, 1, 1));
		nglTextureSubImage1DEXT(texture, target, level, xoffset, width, format, type, MemoryUtil.getAddress(pixels), function_pointer);
	}
	public static void glTextureSubImage1DEXT(int texture, int target, int level, int xoffset, int width, int format, int type, FloatBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glTextureSubImage1DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOdisabled(caps);
		BufferChecks.checkBuffer(pixels, GLChecks.calculateImageStorage(pixels, format, type, width, 1, 1));
		nglTextureSubImage1DEXT(texture, target, level, xoffset, width, format, type, MemoryUtil.getAddress(pixels), function_pointer);
	}
	public static void glTextureSubImage1DEXT(int texture, int target, int level, int xoffset, int width, int format, int type, IntBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glTextureSubImage1DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOdisabled(caps);
		BufferChecks.checkBuffer(pixels, GLChecks.calculateImageStorage(pixels, format, type, width, 1, 1));
		nglTextureSubImage1DEXT(texture, target, level, xoffset, width, format, type, MemoryUtil.getAddress(pixels), function_pointer);
	}
	public static void glTextureSubImage1DEXT(int texture, int target, int level, int xoffset, int width, int format, int type, ShortBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glTextureSubImage1DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOdisabled(caps);
		BufferChecks.checkBuffer(pixels, GLChecks.calculateImageStorage(pixels, format, type, width, 1, 1));
		nglTextureSubImage1DEXT(texture, target, level, xoffset, width, format, type, MemoryUtil.getAddress(pixels), function_pointer);
	}
	static native void nglTextureSubImage1DEXT(int texture, int target, int level, int xoffset, int width, int format, int type, long pixels, long function_pointer);
	public static void glTextureSubImage1DEXT(int texture, int target, int level, int xoffset, int width, int format, int type, long pixels_buffer_offset) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glTextureSubImage1DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOenabled(caps);
		nglTextureSubImage1DEXTBO(texture, target, level, xoffset, width, format, type, pixels_buffer_offset, function_pointer);
	}
	static native void nglTextureSubImage1DEXTBO(int texture, int target, int level, int xoffset, int width, int format, int type, long pixels_buffer_offset, long function_pointer);

	public static void glTextureSubImage2DEXT(int texture, int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, ByteBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glTextureSubImage2DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOdisabled(caps);
		BufferChecks.checkBuffer(pixels, GLChecks.calculateImageStorage(pixels, format, type, width, height, 1));
		nglTextureSubImage2DEXT(texture, target, level, xoffset, yoffset, width, height, format, type, MemoryUtil.getAddress(pixels), function_pointer);
	}
	public static void glTextureSubImage2DEXT(int texture, int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, DoubleBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glTextureSubImage2DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOdisabled(caps);
		BufferChecks.checkBuffer(pixels, GLChecks.calculateImageStorage(pixels, format, type, width, height, 1));
		nglTextureSubImage2DEXT(texture, target, level, xoffset, yoffset, width, height, format, type, MemoryUtil.getAddress(pixels), function_pointer);
	}
	public static void glTextureSubImage2DEXT(int texture, int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, FloatBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glTextureSubImage2DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOdisabled(caps);
		BufferChecks.checkBuffer(pixels, GLChecks.calculateImageStorage(pixels, format, type, width, height, 1));
		nglTextureSubImage2DEXT(texture, target, level, xoffset, yoffset, width, height, format, type, MemoryUtil.getAddress(pixels), function_pointer);
	}
	public static void glTextureSubImage2DEXT(int texture, int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, IntBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glTextureSubImage2DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOdisabled(caps);
		BufferChecks.checkBuffer(pixels, GLChecks.calculateImageStorage(pixels, format, type, width, height, 1));
		nglTextureSubImage2DEXT(texture, target, level, xoffset, yoffset, width, height, format, type, MemoryUtil.getAddress(pixels), function_pointer);
	}
	public static void glTextureSubImage2DEXT(int texture, int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, ShortBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glTextureSubImage2DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOdisabled(caps);
		BufferChecks.checkBuffer(pixels, GLChecks.calculateImageStorage(pixels, format, type, width, height, 1));
		nglTextureSubImage2DEXT(texture, target, level, xoffset, yoffset, width, height, format, type, MemoryUtil.getAddress(pixels), function_pointer);
	}
	static native void nglTextureSubImage2DEXT(int texture, int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, long pixels, long function_pointer);
	public static void glTextureSubImage2DEXT(int texture, int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, long pixels_buffer_offset) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glTextureSubImage2DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOenabled(caps);
		nglTextureSubImage2DEXTBO(texture, target, level, xoffset, yoffset, width, height, format, type, pixels_buffer_offset, function_pointer);
	}
	static native void nglTextureSubImage2DEXTBO(int texture, int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, long pixels_buffer_offset, long function_pointer);

	public static void glCopyTextureImage1DEXT(int texture, int target, int level, int internalformat, int x, int y, int width, int border) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glCopyTextureImage1DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		nglCopyTextureImage1DEXT(texture, target, level, internalformat, x, y, width, border, function_pointer);
	}
	static native void nglCopyTextureImage1DEXT(int texture, int target, int level, int internalformat, int x, int y, int width, int border, long function_pointer);

	public static void glCopyTextureImage2DEXT(int texture, int target, int level, int internalformat, int x, int y, int width, int height, int border) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glCopyTextureImage2DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		nglCopyTextureImage2DEXT(texture, target, level, internalformat, x, y, width, height, border, function_pointer);
	}
	static native void nglCopyTextureImage2DEXT(int texture, int target, int level, int internalformat, int x, int y, int width, int height, int border, long function_pointer);

	public static void glCopyTextureSubImage1DEXT(int texture, int target, int level, int xoffset, int x, int y, int width) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glCopyTextureSubImage1DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		nglCopyTextureSubImage1DEXT(texture, target, level, xoffset, x, y, width, function_pointer);
	}
	static native void nglCopyTextureSubImage1DEXT(int texture, int target, int level, int xoffset, int x, int y, int width, long function_pointer);

	public static void glCopyTextureSubImage2DEXT(int texture, int target, int level, int xoffset, int yoffset, int x, int y, int width, int height) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glCopyTextureSubImage2DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		nglCopyTextureSubImage2DEXT(texture, target, level, xoffset, yoffset, x, y, width, height, function_pointer);
	}
	static native void nglCopyTextureSubImage2DEXT(int texture, int target, int level, int xoffset, int yoffset, int x, int y, int width, int height, long function_pointer);

	public static void glGetTextureImageEXT(int texture, int target, int level, int format, int type, ByteBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glGetTextureImageEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensurePackPBOdisabled(caps);
		BufferChecks.checkBuffer(pixels, GLChecks.calculateImageStorage(pixels, format, type, 1, 1, 1));
		nglGetTextureImageEXT(texture, target, level, format, type, MemoryUtil.getAddress(pixels), function_pointer);
	}
	public static void glGetTextureImageEXT(int texture, int target, int level, int format, int type, DoubleBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glGetTextureImageEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensurePackPBOdisabled(caps);
		BufferChecks.checkBuffer(pixels, GLChecks.calculateImageStorage(pixels, format, type, 1, 1, 1));
		nglGetTextureImageEXT(texture, target, level, format, type, MemoryUtil.getAddress(pixels), function_pointer);
	}
	public static void glGetTextureImageEXT(int texture, int target, int level, int format, int type, FloatBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glGetTextureImageEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensurePackPBOdisabled(caps);
		BufferChecks.checkBuffer(pixels, GLChecks.calculateImageStorage(pixels, format, type, 1, 1, 1));
		nglGetTextureImageEXT(texture, target, level, format, type, MemoryUtil.getAddress(pixels), function_pointer);
	}
	public static void glGetTextureImageEXT(int texture, int target, int level, int format, int type, IntBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glGetTextureImageEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensurePackPBOdisabled(caps);
		BufferChecks.checkBuffer(pixels, GLChecks.calculateImageStorage(pixels, format, type, 1, 1, 1));
		nglGetTextureImageEXT(texture, target, level, format, type, MemoryUtil.getAddress(pixels), function_pointer);
	}
	public static void glGetTextureImageEXT(int texture, int target, int level, int format, int type, ShortBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glGetTextureImageEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensurePackPBOdisabled(caps);
		BufferChecks.checkBuffer(pixels, GLChecks.calculateImageStorage(pixels, format, type, 1, 1, 1));
		nglGetTextureImageEXT(texture, target, level, format, type, MemoryUtil.getAddress(pixels), function_pointer);
	}
	static native void nglGetTextureImageEXT(int texture, int target, int level, int format, int type, long pixels, long function_pointer);
	public static void glGetTextureImageEXT(int texture, int target, int level, int format, int type, long pixels_buffer_offset) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glGetTextureImageEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensurePackPBOenabled(caps);
		nglGetTextureImageEXTBO(texture, target, level, format, type, pixels_buffer_offset, function_pointer);
	}
	static native void nglGetTextureImageEXTBO(int texture, int target, int level, int format, int type, long pixels_buffer_offset, long function_pointer);

	public static void glGetTextureParameterEXT(int texture, int target, int pname, FloatBuffer params) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glGetTextureParameterfvEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		BufferChecks.checkBuffer(params, 4);
		nglGetTextureParameterfvEXT(texture, target, pname, MemoryUtil.getAddress(params), function_pointer);
	}
	static native void nglGetTextureParameterfvEXT(int texture, int target, int pname, long params, long function_pointer);

	/** Overloads glGetTextureParameterfvEXT. */
	public static float glGetTextureParameterfEXT(int texture, int target, int pname) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glGetTextureParameterfvEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		FloatBuffer params = APIUtil.getBufferFloat(caps);
		nglGetTextureParameterfvEXT(texture, target, pname, MemoryUtil.getAddress(params), function_pointer);
		return params.get(0);
	}

	public static void glGetTextureParameterEXT(int texture, int target, int pname, IntBuffer params) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glGetTextureParameterivEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		BufferChecks.checkBuffer(params, 4);
		nglGetTextureParameterivEXT(texture, target, pname, MemoryUtil.getAddress(params), function_pointer);
	}
	static native void nglGetTextureParameterivEXT(int texture, int target, int pname, long params, long function_pointer);

	/** Overloads glGetTextureParameterivEXT. */
	public static int glGetTextureParameteriEXT(int texture, int target, int pname) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glGetTextureParameterivEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		IntBuffer params = APIUtil.getBufferInt(caps);
		nglGetTextureParameterivEXT(texture, target, pname, MemoryUtil.getAddress(params), function_pointer);
		return params.get(0);
	}

	public static void glGetTextureLevelParameterEXT(int texture, int target, int level, int pname, FloatBuffer params) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glGetTextureLevelParameterfvEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		BufferChecks.checkBuffer(params, 4);
		nglGetTextureLevelParameterfvEXT(texture, target, level, pname, MemoryUtil.getAddress(params), function_pointer);
	}
	static native void nglGetTextureLevelParameterfvEXT(int texture, int target, int level, int pname, long params, long function_pointer);

	/** Overloads glGetTextureLevelParameterfvEXT. */
	public static float glGetTextureLevelParameterfEXT(int texture, int target, int level, int pname) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glGetTextureLevelParameterfvEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		FloatBuffer params = APIUtil.getBufferFloat(caps);
		nglGetTextureLevelParameterfvEXT(texture, target, level, pname, MemoryUtil.getAddress(params), function_pointer);
		return params.get(0);
	}

	public static void glGetTextureLevelParameterEXT(int texture, int target, int level, int pname, IntBuffer params) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glGetTextureLevelParameterivEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		BufferChecks.checkBuffer(params, 4);
		nglGetTextureLevelParameterivEXT(texture, target, level, pname, MemoryUtil.getAddress(params), function_pointer);
	}
	static native void nglGetTextureLevelParameterivEXT(int texture, int target, int level, int pname, long params, long function_pointer);

	/** Overloads glGetTextureLevelParameterivEXT. */
	public static int glGetTextureLevelParameteriEXT(int texture, int target, int level, int pname) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glGetTextureLevelParameterivEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		IntBuffer params = APIUtil.getBufferInt(caps);
		nglGetTextureLevelParameterivEXT(texture, target, level, pname, MemoryUtil.getAddress(params), function_pointer);
		return params.get(0);
	}

	public static void glTextureImage3DEXT(int texture, int target, int level, int internalformat, int width, int height, int depth, int border, int format, int type, ByteBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glTextureImage3DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOdisabled(caps);
		if (pixels != null)
			BufferChecks.checkBuffer(pixels, GLChecks.calculateTexImage3DStorage(pixels, format, type, width, height, depth));
		nglTextureImage3DEXT(texture, target, level, internalformat, width, height, depth, border, format, type, MemoryUtil.getAddressSafe(pixels), function_pointer);
	}
	public static void glTextureImage3DEXT(int texture, int target, int level, int internalformat, int width, int height, int depth, int border, int format, int type, DoubleBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glTextureImage3DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOdisabled(caps);
		if (pixels != null)
			BufferChecks.checkBuffer(pixels, GLChecks.calculateTexImage3DStorage(pixels, format, type, width, height, depth));
		nglTextureImage3DEXT(texture, target, level, internalformat, width, height, depth, border, format, type, MemoryUtil.getAddressSafe(pixels), function_pointer);
	}
	public static void glTextureImage3DEXT(int texture, int target, int level, int internalformat, int width, int height, int depth, int border, int format, int type, FloatBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glTextureImage3DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOdisabled(caps);
		if (pixels != null)
			BufferChecks.checkBuffer(pixels, GLChecks.calculateTexImage3DStorage(pixels, format, type, width, height, depth));
		nglTextureImage3DEXT(texture, target, level, internalformat, width, height, depth, border, format, type, MemoryUtil.getAddressSafe(pixels), function_pointer);
	}
	public static void glTextureImage3DEXT(int texture, int target, int level, int internalformat, int width, int height, int depth, int border, int format, int type, IntBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glTextureImage3DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOdisabled(caps);
		if (pixels != null)
			BufferChecks.checkBuffer(pixels, GLChecks.calculateTexImage3DStorage(pixels, format, type, width, height, depth));
		nglTextureImage3DEXT(texture, target, level, internalformat, width, height, depth, border, format, type, MemoryUtil.getAddressSafe(pixels), function_pointer);
	}
	public static void glTextureImage3DEXT(int texture, int target, int level, int internalformat, int width, int height, int depth, int border, int format, int type, ShortBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glTextureImage3DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOdisabled(caps);
		if (pixels != null)
			BufferChecks.checkBuffer(pixels, GLChecks.calculateTexImage3DStorage(pixels, format, type, width, height, depth));
		nglTextureImage3DEXT(texture, target, level, internalformat, width, height, depth, border, format, type, MemoryUtil.getAddressSafe(pixels), function_pointer);
	}
	static native void nglTextureImage3DEXT(int texture, int target, int level, int internalformat, int width, int height, int depth, int border, int format, int type, long pixels, long function_pointer);
	public static void glTextureImage3DEXT(int texture, int target, int level, int internalformat, int width, int height, int depth, int border, int format, int type, long pixels_buffer_offset) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glTextureImage3DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOenabled(caps);
		nglTextureImage3DEXTBO(texture, target, level, internalformat, width, height, depth, border, format, type, pixels_buffer_offset, function_pointer);
	}
	static native void nglTextureImage3DEXTBO(int texture, int target, int level, int internalformat, int width, int height, int depth, int border, int format, int type, long pixels_buffer_offset, long function_pointer);

	public static void glTextureSubImage3DEXT(int texture, int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, ByteBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glTextureSubImage3DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOdisabled(caps);
		BufferChecks.checkBuffer(pixels, GLChecks.calculateImageStorage(pixels, format, type, width, height, depth));
		nglTextureSubImage3DEXT(texture, target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, MemoryUtil.getAddress(pixels), function_pointer);
	}
	public static void glTextureSubImage3DEXT(int texture, int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, DoubleBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glTextureSubImage3DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOdisabled(caps);
		BufferChecks.checkBuffer(pixels, GLChecks.calculateImageStorage(pixels, format, type, width, height, depth));
		nglTextureSubImage3DEXT(texture, target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, MemoryUtil.getAddress(pixels), function_pointer);
	}
	public static void glTextureSubImage3DEXT(int texture, int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, FloatBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glTextureSubImage3DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOdisabled(caps);
		BufferChecks.checkBuffer(pixels, GLChecks.calculateImageStorage(pixels, format, type, width, height, depth));
		nglTextureSubImage3DEXT(texture, target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, MemoryUtil.getAddress(pixels), function_pointer);
	}
	public static void glTextureSubImage3DEXT(int texture, int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, IntBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glTextureSubImage3DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOdisabled(caps);
		BufferChecks.checkBuffer(pixels, GLChecks.calculateImageStorage(pixels, format, type, width, height, depth));
		nglTextureSubImage3DEXT(texture, target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, MemoryUtil.getAddress(pixels), function_pointer);
	}
	public static void glTextureSubImage3DEXT(int texture, int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, ShortBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glTextureSubImage3DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOdisabled(caps);
		BufferChecks.checkBuffer(pixels, GLChecks.calculateImageStorage(pixels, format, type, width, height, depth));
		nglTextureSubImage3DEXT(texture, target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, MemoryUtil.getAddress(pixels), function_pointer);
	}
	static native void nglTextureSubImage3DEXT(int texture, int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, long pixels, long function_pointer);
	public static void glTextureSubImage3DEXT(int texture, int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, long pixels_buffer_offset) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glTextureSubImage3DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOenabled(caps);
		nglTextureSubImage3DEXTBO(texture, target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels_buffer_offset, function_pointer);
	}
	static native void nglTextureSubImage3DEXTBO(int texture, int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, long pixels_buffer_offset, long function_pointer);

	public static void glCopyTextureSubImage3DEXT(int texture, int target, int level, int xoffset, int yoffset, int zoffset, int x, int y, int width, int height) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glCopyTextureSubImage3DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		nglCopyTextureSubImage3DEXT(texture, target, level, xoffset, yoffset, zoffset, x, y, width, height, function_pointer);
	}
	static native void nglCopyTextureSubImage3DEXT(int texture, int target, int level, int xoffset, int yoffset, int zoffset, int x, int y, int width, int height, long function_pointer);

	public static void glBindMultiTextureEXT(int texunit, int target, int texture) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glBindMultiTextureEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		nglBindMultiTextureEXT(texunit, target, texture, function_pointer);
	}
	static native void nglBindMultiTextureEXT(int texunit, int target, int texture, long function_pointer);

	public static void glMultiTexCoordPointerEXT(int texunit, int size, int stride, DoubleBuffer pointer) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMultiTexCoordPointerEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureArrayVBOdisabled(caps);
		BufferChecks.checkDirect(pointer);
		nglMultiTexCoordPointerEXT(texunit, size, GL11.GL_DOUBLE, stride, MemoryUtil.getAddress(pointer), function_pointer);
	}
	public static void glMultiTexCoordPointerEXT(int texunit, int size, int stride, FloatBuffer pointer) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMultiTexCoordPointerEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureArrayVBOdisabled(caps);
		BufferChecks.checkDirect(pointer);
		nglMultiTexCoordPointerEXT(texunit, size, GL11.GL_FLOAT, stride, MemoryUtil.getAddress(pointer), function_pointer);
	}
	static native void nglMultiTexCoordPointerEXT(int texunit, int size, int type, int stride, long pointer, long function_pointer);
	public static void glMultiTexCoordPointerEXT(int texunit, int size, int type, int stride, long pointer_buffer_offset) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMultiTexCoordPointerEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureArrayVBOenabled(caps);
		nglMultiTexCoordPointerEXTBO(texunit, size, type, stride, pointer_buffer_offset, function_pointer);
	}
	static native void nglMultiTexCoordPointerEXTBO(int texunit, int size, int type, int stride, long pointer_buffer_offset, long function_pointer);

	public static void glMultiTexEnvfEXT(int texunit, int target, int pname, float param) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMultiTexEnvfEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		nglMultiTexEnvfEXT(texunit, target, pname, param, function_pointer);
	}
	static native void nglMultiTexEnvfEXT(int texunit, int target, int pname, float param, long function_pointer);

	public static void glMultiTexEnvEXT(int texunit, int target, int pname, FloatBuffer params) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMultiTexEnvfvEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		BufferChecks.checkBuffer(params, 4);
		nglMultiTexEnvfvEXT(texunit, target, pname, MemoryUtil.getAddress(params), function_pointer);
	}
	static native void nglMultiTexEnvfvEXT(int texunit, int target, int pname, long params, long function_pointer);

	public static void glMultiTexEnviEXT(int texunit, int target, int pname, int param) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMultiTexEnviEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		nglMultiTexEnviEXT(texunit, target, pname, param, function_pointer);
	}
	static native void nglMultiTexEnviEXT(int texunit, int target, int pname, int param, long function_pointer);

	public static void glMultiTexEnvEXT(int texunit, int target, int pname, IntBuffer params) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMultiTexEnvivEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		BufferChecks.checkBuffer(params, 4);
		nglMultiTexEnvivEXT(texunit, target, pname, MemoryUtil.getAddress(params), function_pointer);
	}
	static native void nglMultiTexEnvivEXT(int texunit, int target, int pname, long params, long function_pointer);

	public static void glMultiTexGendEXT(int texunit, int coord, int pname, double param) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMultiTexGendEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		nglMultiTexGendEXT(texunit, coord, pname, param, function_pointer);
	}
	static native void nglMultiTexGendEXT(int texunit, int coord, int pname, double param, long function_pointer);

	public static void glMultiTexGenEXT(int texunit, int coord, int pname, DoubleBuffer params) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMultiTexGendvEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		BufferChecks.checkBuffer(params, 4);
		nglMultiTexGendvEXT(texunit, coord, pname, MemoryUtil.getAddress(params), function_pointer);
	}
	static native void nglMultiTexGendvEXT(int texunit, int coord, int pname, long params, long function_pointer);

	public static void glMultiTexGenfEXT(int texunit, int coord, int pname, float param) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMultiTexGenfEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		nglMultiTexGenfEXT(texunit, coord, pname, param, function_pointer);
	}
	static native void nglMultiTexGenfEXT(int texunit, int coord, int pname, float param, long function_pointer);

	public static void glMultiTexGenEXT(int texunit, int coord, int pname, FloatBuffer params) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMultiTexGenfvEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		BufferChecks.checkBuffer(params, 4);
		nglMultiTexGenfvEXT(texunit, coord, pname, MemoryUtil.getAddress(params), function_pointer);
	}
	static native void nglMultiTexGenfvEXT(int texunit, int coord, int pname, long params, long function_pointer);

	public static void glMultiTexGeniEXT(int texunit, int coord, int pname, int param) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMultiTexGeniEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		nglMultiTexGeniEXT(texunit, coord, pname, param, function_pointer);
	}
	static native void nglMultiTexGeniEXT(int texunit, int coord, int pname, int param, long function_pointer);

	public static void glMultiTexGenEXT(int texunit, int coord, int pname, IntBuffer params) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMultiTexGenivEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		BufferChecks.checkBuffer(params, 4);
		nglMultiTexGenivEXT(texunit, coord, pname, MemoryUtil.getAddress(params), function_pointer);
	}
	static native void nglMultiTexGenivEXT(int texunit, int coord, int pname, long params, long function_pointer);

	public static void glGetMultiTexEnvEXT(int texunit, int target, int pname, FloatBuffer params) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glGetMultiTexEnvfvEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		BufferChecks.checkBuffer(params, 4);
		nglGetMultiTexEnvfvEXT(texunit, target, pname, MemoryUtil.getAddress(params), function_pointer);
	}
	static native void nglGetMultiTexEnvfvEXT(int texunit, int target, int pname, long params, long function_pointer);

	public static void glGetMultiTexEnvEXT(int texunit, int target, int pname, IntBuffer params) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glGetMultiTexEnvivEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		BufferChecks.checkBuffer(params, 4);
		nglGetMultiTexEnvivEXT(texunit, target, pname, MemoryUtil.getAddress(params), function_pointer);
	}
	static native void nglGetMultiTexEnvivEXT(int texunit, int target, int pname, long params, long function_pointer);

	public static void glGetMultiTexGenEXT(int texunit, int coord, int pname, DoubleBuffer params) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glGetMultiTexGendvEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		BufferChecks.checkBuffer(params, 4);
		nglGetMultiTexGendvEXT(texunit, coord, pname, MemoryUtil.getAddress(params), function_pointer);
	}
	static native void nglGetMultiTexGendvEXT(int texunit, int coord, int pname, long params, long function_pointer);

	public static void glGetMultiTexGenEXT(int texunit, int coord, int pname, FloatBuffer params) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glGetMultiTexGenfvEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		BufferChecks.checkBuffer(params, 4);
		nglGetMultiTexGenfvEXT(texunit, coord, pname, MemoryUtil.getAddress(params), function_pointer);
	}
	static native void nglGetMultiTexGenfvEXT(int texunit, int coord, int pname, long params, long function_pointer);

	public static void glGetMultiTexGenEXT(int texunit, int coord, int pname, IntBuffer params) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glGetMultiTexGenivEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		BufferChecks.checkBuffer(params, 4);
		nglGetMultiTexGenivEXT(texunit, coord, pname, MemoryUtil.getAddress(params), function_pointer);
	}
	static native void nglGetMultiTexGenivEXT(int texunit, int coord, int pname, long params, long function_pointer);

	public static void glMultiTexParameteriEXT(int texunit, int target, int pname, int param) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMultiTexParameteriEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		nglMultiTexParameteriEXT(texunit, target, pname, param, function_pointer);
	}
	static native void nglMultiTexParameteriEXT(int texunit, int target, int pname, int param, long function_pointer);

	public static void glMultiTexParameterEXT(int texunit, int target, int pname, IntBuffer param) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMultiTexParameterivEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		BufferChecks.checkBuffer(param, 4);
		nglMultiTexParameterivEXT(texunit, target, pname, MemoryUtil.getAddress(param), function_pointer);
	}
	static native void nglMultiTexParameterivEXT(int texunit, int target, int pname, long param, long function_pointer);

	public static void glMultiTexParameterfEXT(int texunit, int target, int pname, float param) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMultiTexParameterfEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		nglMultiTexParameterfEXT(texunit, target, pname, param, function_pointer);
	}
	static native void nglMultiTexParameterfEXT(int texunit, int target, int pname, float param, long function_pointer);

	public static void glMultiTexParameterEXT(int texunit, int target, int pname, FloatBuffer param) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMultiTexParameterfvEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		BufferChecks.checkBuffer(param, 4);
		nglMultiTexParameterfvEXT(texunit, target, pname, MemoryUtil.getAddress(param), function_pointer);
	}
	static native void nglMultiTexParameterfvEXT(int texunit, int target, int pname, long param, long function_pointer);

	public static void glMultiTexImage1DEXT(int texunit, int target, int level, int internalformat, int width, int border, int format, int type, ByteBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMultiTexImage1DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOdisabled(caps);
		if (pixels != null)
			BufferChecks.checkBuffer(pixels, GLChecks.calculateTexImage1DStorage(pixels, format, type, width));
		nglMultiTexImage1DEXT(texunit, target, level, internalformat, width, border, format, type, MemoryUtil.getAddressSafe(pixels), function_pointer);
	}
	public static void glMultiTexImage1DEXT(int texunit, int target, int level, int internalformat, int width, int border, int format, int type, DoubleBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMultiTexImage1DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOdisabled(caps);
		if (pixels != null)
			BufferChecks.checkBuffer(pixels, GLChecks.calculateTexImage1DStorage(pixels, format, type, width));
		nglMultiTexImage1DEXT(texunit, target, level, internalformat, width, border, format, type, MemoryUtil.getAddressSafe(pixels), function_pointer);
	}
	public static void glMultiTexImage1DEXT(int texunit, int target, int level, int internalformat, int width, int border, int format, int type, FloatBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMultiTexImage1DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOdisabled(caps);
		if (pixels != null)
			BufferChecks.checkBuffer(pixels, GLChecks.calculateTexImage1DStorage(pixels, format, type, width));
		nglMultiTexImage1DEXT(texunit, target, level, internalformat, width, border, format, type, MemoryUtil.getAddressSafe(pixels), function_pointer);
	}
	public static void glMultiTexImage1DEXT(int texunit, int target, int level, int internalformat, int width, int border, int format, int type, IntBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMultiTexImage1DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOdisabled(caps);
		if (pixels != null)
			BufferChecks.checkBuffer(pixels, GLChecks.calculateTexImage1DStorage(pixels, format, type, width));
		nglMultiTexImage1DEXT(texunit, target, level, internalformat, width, border, format, type, MemoryUtil.getAddressSafe(pixels), function_pointer);
	}
	public static void glMultiTexImage1DEXT(int texunit, int target, int level, int internalformat, int width, int border, int format, int type, ShortBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMultiTexImage1DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOdisabled(caps);
		if (pixels != null)
			BufferChecks.checkBuffer(pixels, GLChecks.calculateTexImage1DStorage(pixels, format, type, width));
		nglMultiTexImage1DEXT(texunit, target, level, internalformat, width, border, format, type, MemoryUtil.getAddressSafe(pixels), function_pointer);
	}
	static native void nglMultiTexImage1DEXT(int texunit, int target, int level, int internalformat, int width, int border, int format, int type, long pixels, long function_pointer);
	public static void glMultiTexImage1DEXT(int texunit, int target, int level, int internalformat, int width, int border, int format, int type, long pixels_buffer_offset) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMultiTexImage1DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOenabled(caps);
		nglMultiTexImage1DEXTBO(texunit, target, level, internalformat, width, border, format, type, pixels_buffer_offset, function_pointer);
	}
	static native void nglMultiTexImage1DEXTBO(int texunit, int target, int level, int internalformat, int width, int border, int format, int type, long pixels_buffer_offset, long function_pointer);

	public static void glMultiTexImage2DEXT(int texunit, int target, int level, int internalformat, int width, int height, int border, int format, int type, ByteBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMultiTexImage2DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOdisabled(caps);
		if (pixels != null)
			BufferChecks.checkBuffer(pixels, GLChecks.calculateTexImage2DStorage(pixels, format, type, width, height));
		nglMultiTexImage2DEXT(texunit, target, level, internalformat, width, height, border, format, type, MemoryUtil.getAddressSafe(pixels), function_pointer);
	}
	public static void glMultiTexImage2DEXT(int texunit, int target, int level, int internalformat, int width, int height, int border, int format, int type, DoubleBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMultiTexImage2DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOdisabled(caps);
		if (pixels != null)
			BufferChecks.checkBuffer(pixels, GLChecks.calculateTexImage2DStorage(pixels, format, type, width, height));
		nglMultiTexImage2DEXT(texunit, target, level, internalformat, width, height, border, format, type, MemoryUtil.getAddressSafe(pixels), function_pointer);
	}
	public static void glMultiTexImage2DEXT(int texunit, int target, int level, int internalformat, int width, int height, int border, int format, int type, FloatBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMultiTexImage2DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOdisabled(caps);
		if (pixels != null)
			BufferChecks.checkBuffer(pixels, GLChecks.calculateTexImage2DStorage(pixels, format, type, width, height));
		nglMultiTexImage2DEXT(texunit, target, level, internalformat, width, height, border, format, type, MemoryUtil.getAddressSafe(pixels), function_pointer);
	}
	public static void glMultiTexImage2DEXT(int texunit, int target, int level, int internalformat, int width, int height, int border, int format, int type, IntBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMultiTexImage2DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOdisabled(caps);
		if (pixels != null)
			BufferChecks.checkBuffer(pixels, GLChecks.calculateTexImage2DStorage(pixels, format, type, width, height));
		nglMultiTexImage2DEXT(texunit, target, level, internalformat, width, height, border, format, type, MemoryUtil.getAddressSafe(pixels), function_pointer);
	}
	public static void glMultiTexImage2DEXT(int texunit, int target, int level, int internalformat, int width, int height, int border, int format, int type, ShortBuffer pixels) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMultiTexImage2DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOdisabled(caps);
		if (pixels != null)
			BufferChecks.checkBuffer(pixels, GLChecks.calculateTexImage2DStorage(pixels, format, type, width, height));
		nglMultiTexImage2DEXT(texunit, target, level, internalformat, width, height, border, format, type, MemoryUtil.getAddressSafe(pixels), function_pointer);
	}
	static native void nglMultiTexImage2DEXT(int texunit, int target, int level, int internalformat, int width, int height, int border, int format, int type, long pixels, long function_pointer);
	public static void glMultiTexImage2DEXT(int texunit, int target, int level, int internalformat, int width, int height, int border, int format, int type, long pixels_buffer_offset) {
		ContextCapabilities caps = GLContext.getCapabilities();
		long function_pointer = caps.glMultiTexImage2DEXT;
		BufferChecks.checkFunctionAddress(function_pointer);
		GLChecks.ensureUnpackPBOenabled(caps);
		nglMultiTexImage2DEXTBO(texunit, target, level, internalformat, width, height, border, format, type, pixels_buffer_offset, function_pointer);
	}
	static native void nglMultiTexImage2DEXTBO(int texunit, int target, int level, int internalformat, int width, int height, int border, int format, int type, long pixels_buffer_offset, long function_pointer);

	public static void glMultiTexSubImage1DEXT(int texunit, int target, int level, int xoffset, int width, int format, i
