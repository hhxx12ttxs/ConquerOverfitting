/*******************************************************************************
 * Copyright 2014 Felix Angell freefouran@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package io.root.gfx.glutils;

import io.root.RootException;
import io.root.util.Logger;
import io.root.util.ShaderUtil;
import io.root.util.math.Matrix3f;
import io.root.util.math.Matrix4f;
import io.root.util.math.Vector2f;
import io.root.util.math.Vector3f;
import io.root.util.math.Vector4f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.BufferUtils;

public class Shader {

	// recycling buffers
	private static FloatBuffer floatBuffer;
	private static IntBuffer intBuffer;

	// types of shaders
	public static final int VERT_SHADER = GL.GL_VERTEX_SHADER;
	public static final int FRAG_SHADER = GL.GL_FRAGMENT_SHADER;
	public static final int GEOM_SHADER = GL.GL_FRAGMENT_SHADER;

	// strict shader
	private static boolean strict = false;

	/** the shaders handle */
	protected int shaderProgram;

	/** log for the shader */
	protected String shaderLog = "";

	/** shaders uniforms */
	protected HashMap<String, Integer> shaderUniforms = new HashMap<String, Integer>();

	/** shader attributes */
	protected Attribute[] attributes;

	/** source for the vertex shader */
	protected String vertShaderSource;

	/** source for the fragment shader */
	protected String fragShaderSource;

	/** handle for the vertex shader */
	protected int vertHandle;

	/** handle for the fragment shader */
	protected int fragHandle;

	/**
	 * Creates a shader from the given vertex, and fragment shaders with the
	 * given attribute locations
	 * 
	 * @param vertexShaderSource
	 * @param fragShaderSource
	 * @param attribLocations
	 */
	public Shader(String vertexShaderSource, String fragShaderSource, List<VertexAttribute> attribLocations) {
		if (vertexShaderSource == null || fragShaderSource == null)
			throw new RootException("shader source must be non-null");

		if (!ShaderUtil.shadersSupported())
			throw new RootException("no shader support found; shaders require OpenGL 2.0");

		this.vertShaderSource = vertexShaderSource;
		this.fragShaderSource = fragShaderSource;

		vertHandle = compileShader(VERT_SHADER, vertexShaderSource);
		fragHandle = compileShader(FRAG_SHADER, fragShaderSource);
		shaderProgram = createProgram();

		try {
			linkProgram(attribLocations);
		} catch (RootException e) {
			dispose();
			throw new RootException("could not link program:\n" + e.getMessage());
		}

		if (getLog().length() != 0)
			Logger.warning(getLog());
	}

	/**
	 * Create a Shader Program
	 * 
	 * @return the programs handle
	 * @throws RootException
	 */
	protected int createProgram() throws RootException {
		int program = GL.glCreateProgram();
		if (program == 0)
			throw new RootException("could not create shader program");
		return program;
	}

	/**
	 * @param type
	 *            the type of shader
	 * @return the type of the shader according to the given id
	 */
	private String getShaderTypeAccordingToId(int type) {
		if (type == FRAG_SHADER)
			return "FRAGMENT_SHADER";
		else if (type == VERT_SHADER)
			return "VERTEX_SHADER";
		else if (type == GEOM_SHADER)
			return "GEOMETRY_SHADER";
		else
			return "shader";
	}

	/**
	 * Utility method for compiling a shader
	 * 
	 * @param type
	 *            shader type
	 * @param source
	 *            shader source
	 * @return the shaders handle
	 */
	protected int compileShader(int type, String source) {
		int shader = GL.glCreateShader(type);
		if (shader == 0)
			throw new RootException("could not create shader program");
		GL.glShaderSource(shader, source);
		GL.glCompileShader(shader);

		int comp = GL.glGetShaderi(shader, GL.GL_COMPILE_STATUS);
		int len = GL.glGetShaderi(shader, GL.GL_INFO_LOG_LENGTH);
		String t = getShaderTypeAccordingToId(type);
		String err = GL.glGetShaderInfoLog(shader, len);
		if (err != null && err.length() != 0)
			shaderLog += t + " compile log:\n" + err + "\n";
		if (comp == GL.GL_FALSE)
			throw new RootException(shaderLog.length() != 0 ? shaderLog : "unable to compile " + getShaderTypeAccordingToId(type));
		return shader;
	}

	/**
	 * Attaches the shaders to the program handle
	 */
	protected void attachShaders() {
		GL.glAttachShader(getProgramId(), vertHandle);
		GL.glAttachShader(getProgramId(), fragHandle);
	}

	/**
	 * Link the attributes to the program
	 * 
	 * @param attribLocations
	 */
	protected void linkProgram(List<VertexAttribute> attribLocations) {
		if (!valid())
			throw new RootException("cannot link released program");

		shaderUniforms.clear();

		if (attribLocations != null) {
			for (VertexAttribute a : attribLocations) {
				if (a != null)
					GL.glBindAttribLocation(shaderProgram, a.location, a.name);
			}
		}

		attachShaders();
		GL.glLinkProgram(shaderProgram);
		int comp = GL.glGetProgrami(shaderProgram, GL.GL_LINK_STATUS);
		int len = GL.glGetProgrami(shaderProgram, GL.GL_INFO_LOG_LENGTH);
		String err = GL.glGetProgramInfoLog(shaderProgram, len);
		if (err != null && err.length() != 0)
			shaderLog = err + "\n" + shaderLog;
		if (shaderLog != null)
			shaderLog = shaderLog.trim();
		if (comp == GL.GL_FALSE)
			throw new RootException(shaderLog.length() != 0 ? shaderLog : "unable to link program");

		fetchUniforms();
		fetchAttributes();
	}

	/**
	 * @return the shaders log
	 */
	public String getLog() {
		return shaderLog;
	}

	/**
	 * Use the program
	 */
	public void use() {
		if (!valid())
			throw new IllegalStateException("cannot use invalid program");
		GL.glUseProgram(shaderProgram);
	}

	/**
	 * Release shader program
	 */
	public void release() {
		GL.glUseProgram(0);
	}

	/**
	 * Disposes the shaders
	 */
	public void disposeShaders() {
		if (vertHandle != 0) {
			Logger.notice("Disposing vertex shader #" + vertHandle);
			GL.glDetachShader(getProgramId(), vertHandle);
			GL.glDeleteShader(vertHandle);
			vertHandle = 0;
		}
		if (fragHandle != 0) {
			Logger.notice("Disposing fragment shader #" + fragHandle);
			GL.glDetachShader(getProgramId(), fragHandle);
			GL.glDeleteShader(fragHandle);
			fragHandle = 0;
		}
	}

	/**
	 * Dispose the program itself
	 */
	public void dispose() {
		if (shaderProgram != 0) {
			Logger.notice("Disposing shader program #" + shaderProgram);

			disposeShaders();
			GL.glDeleteProgram(shaderProgram);
			shaderProgram = 0;
		}
	}

	/** @return the vertex shaders id */
	public int getVertexShaderID() {
		return vertHandle;
	}

	/** @return the fragment shaders id */
	public int getFragmentShaderID() {
		return fragHandle;
	}

	/** @return the source of the vert shader */
	public String getVertexShaderSource() {
		return vertShaderSource;
	}

	/** @return the source of the frag shader */
	public String getFragmentShaderSource() {
		return fragShaderSource;
	}

	/** @return the program id */
	public int getProgramId() {
		return shaderProgram;
	}

	/** @return if we are using a program */
	public boolean valid() {
		return shaderProgram != 0;
	}

	/**
	 * Fetches the uniforms from the shader program
	 */
	private void fetchUniforms() {
		int len = GL.glGetProgrami(shaderProgram, GL.GL_ACTIVE_UNIFORMS);
		int strLen = GL.glGetProgrami(shaderProgram, GL.GL_ACTIVE_UNIFORM_MAX_LENGTH);

		for (int i = 0; i < len; i++) {
			String name = GL.glGetActiveUniform(shaderProgram, i, strLen);
			int id = GL.glGetUniformLocation(shaderProgram, name);
			shaderUniforms.put(name, id);
		}
	}

	/**
	 * Fetches the attributes from the shader program
	 */
	private void fetchAttributes() {
		int len = GL.glGetProgrami(shaderProgram, GL.GL_ACTIVE_ATTRIBUTES);
		int strLen = GL.glGetProgrami(shaderProgram, GL.GL_ACTIVE_ATTRIBUTE_MAX_LENGTH);

		attributes = new Attribute[len];
		for (int i = 0; i < len; i++) {
			Attribute a = new Attribute();
			a.name = GL.glGetActiveAttrib(shaderProgram, i, strLen);
			a.size = GL.glGetActiveAttribSize(shaderProgram, i);
			a.type = GL.glGetActiveAttribType(shaderProgram, i);
			a.location = GL.glGetAttribLocation(shaderProgram, a.name);
			attributes[i] = a;
		}
	}

	/**
	 * Gets the unfiroms location from the shader
	 * 
	 * @param name
	 */
	public int getUniformLocation(String name) {
		int location = -1;
		Integer locI = shaderUniforms.get(name);
		if (locI == null) { // maybe it's not yet cached?
			location = GL.glGetUniformLocation(shaderProgram, name);
			shaderUniforms.put(name, location);
		} else
			location = locI.intValue();
		if (location == -1 && strict)
			throw new IllegalArgumentException("no active uniform by name '" + name + "' " + "(disable strict compiling to suppress warnings)");
		return location;
	}

	/**
	 * Lookup an attribute by it's name
	 * 
	 * @param name
	 *            the name to search
	 * @return the attribute with the specified name or null
	 */
	public Attribute findAttrib(String name) {
		for (int i = 0; i < attributes.length; i++) {
			if (name.equals(attributes[i].name))
				return attributes[i];
		}
		if (strict)
			throw new IllegalArgumentException("no active attribute by name '" + name + "' " + "(disable strict compiling to suppress warnings)");
		return null;
	}
	
	public int getAttributeLocation(String name) {
		Attribute a = findAttrib(name);
		return a != null ? a.location : -1;
	}

	public int getAttributeType(String name) {
		Attribute a = findAttrib(name);
		return a != null ? a.type : -1;
	}

	public int getAttributeSize(String name) {
		Attribute a = findAttrib(name);
		return a != null ? a.size : -1;
	}

	public String[] getAttributeNames() {
		String[] s = new String[attributes.length];
		for (int i = 0; i < attributes.length; i++) {
			s[i] = attributes[i].name;
		}
		return s;
	}

	public String[] getUniformNames() {
		return shaderUniforms.keySet().toArray(new String[shaderUniforms.size()]);
	}

	public boolean hasUniform(String name) {
		return shaderUniforms.containsKey(name);
	}

	public boolean hasAttribute(String name) {
		for (int i = 0; i < attributes.length; i++)
			if (name.equals(attributes[i].name))
				return true;
		return false;
	}

	private FloatBuffer uniformf(int loc) {
		if (floatBuffer == null)
			floatBuffer = BufferUtils.createFloatBuffer(16);
		floatBuffer.clear();
		if (loc == -1)
			return floatBuffer;
		getUniform(loc, floatBuffer);
		return floatBuffer;
	}

	private IntBuffer uniformi(int loc) {
		if (intBuffer == null)
			intBuffer = BufferUtils.createIntBuffer(4);
		intBuffer.clear();
		if (loc == -1)
			return intBuffer;
		getUniform(loc, intBuffer);
		return intBuffer;
	}

	public void getUniform(int loc, FloatBuffer buf) {
		GL.glGetUniform(shaderProgram, loc, buf);
	}

	public void getUniform(int loc, IntBuffer buf) {
		GL.glGetUniform(shaderProgram, loc, buf);
	}

	public boolean getUniform(String name, FloatBuffer buf) {
		int id = getUniformLocation(name);
		if (id == -1)
			return false;
		getUniform(id, buf);
		return true;
	}

	public boolean getUniform(String name, IntBuffer buf) {
		int id = getUniformLocation(name);
		if (id == -1)
			return false;
		getUniform(id, buf);
		return true;
	}

	public int getUniform1i(int loc) {
		return uniformi(loc).get(0);
	}

	public int getUniform1i(String name) {
		return getUniform1i(getUniformLocation(name));
	}

	public int[] getUniform2i(int loc) {
		IntBuffer buf = uniformi(loc);
		return new int[] { buf.get(0), buf.get(1) };
	}

	public int[] getUniform2i(String name) {
		return getUniform2i(getUniformLocation(name));
	}

	public int[] getUniform3i(int loc) {
		IntBuffer buf = uniformi(loc);
		return new int[] { buf.get(0), buf.get(1), buf.get(2) };
	}

	public int[] getUniform3i(String name) {
		return getUniform3i(getUniformLocation(name));
	}

	public int[] getUniform4i(int loc) {
		IntBuffer buf = uniformi(loc);
		return new int[] { buf.get(0), buf.get(1), buf.get(2), buf.get(3) };
	}

	public int[] getUniform4i(String name) {
		return getUniform4i(getUniformLocation(name));
	}

	public float getUniform1f(int loc) {
		return uniformf(loc).get(0);
	}

	public float getUniform1f(String name) {
		return getUniform1f(getUniformLocation(name));
	}

	public float[] getUniform2f(int loc) {
		FloatBuffer buf = uniformf(loc);
		return new float[] { buf.get(0), buf.get(1) };
	}

	public float[] getUniform2f(String name) {
		return getUniform2f(getUniformLocation(name));
	}

	public float[] getUniform3f(int loc) {
		FloatBuffer buf = uniformf(loc);
		return new float[] { buf.get(0), buf.get(1), buf.get(2) };
	}

	public float[] getUniform3f(String name) {
		return getUniform3f(getUniformLocation(name));
	}

	public float[] getUniform4f(int loc) {
		FloatBuffer buf = uniformf(loc);
		return new float[] { buf.get(0), buf.get(1), buf.get(2), buf.get(3) };
	}

	public float[] getUniform4f(String name) {
		return getUniform4f(getUniformLocation(name));
	}

	public void setUniformf(int loc, float f) {
		if (loc == -1)
			return;
		GL.glUniform1f(loc, f);
	}

	public void setUniformf(int loc, float a, float b) {
		if (loc == -1)
			return;
		GL.glUniform2f(loc, a, b);
	}

	public void setUniformf(int loc, float a, float b, float c) {
		if (loc == -1)
			return;
		GL.glUniform3f(loc, a, b, c);
	}

	public void setUniformf(int loc, float a, float b, float c, float d) {
		if (loc == -1)
			return;
		GL.glUniform4f(loc, a, b, c, d);
	}

	public void setUniformi(int loc, int i) {
		if (loc == -1)
			return;
		GL.glUniform1i(loc, i);
	}

	public void setUniformi(int loc, int a, int b) {
		if (loc == -1)
			return;
		GL.glUniform2i(loc, a, b);
	}

	public void setUniformi(int loc, int a, int b, int c) {
		if (loc == -1)
			return;
		GL.glUniform3i(loc, a, b, c);
	}

	public void setUniformi(int loc, int a, int b, int c, int d) {
		if (loc == -1)
			return;
		GL.glUniform4i(loc, a, b, c, d);
	}

	public void setUniformf(String name, float f) {
		setUniformf(getUniformLocation(name), f);
	}

	public void setUniformf(String name, float a, float b) {
		setUniformf(getUniformLocation(name), a, b);
	}

	public void setUniformf(String name, float a, float b, float c) {
		setUniformf(getUniformLocation(name), a, b, c);
	}

	public void setUniformf(String name, float a, float b, float c, float d) {
		setUniformf(getUniformLocation(name), a, b, c, d);
	}

	public void setUniformi(String name, int i) {
		setUniformi(getUniformLocation(name), i);
	}

	public void setUniformi(String name, int a, int b) {
		setUniformi(getUniformLocation(name), a, b);
	}

	public void setUniformi(String name, int a, int b, int c) {
		setUniformi(getUniformLocation(name), a, b, c);
	}

	public void setUniformi(String name, int a, int b, int c, int d) {
		setUniformi(getUniformLocation(name), a, b, c, d);
	}

	public void setUniformMatrix(String name, boolean transpose, Matrix3f m) {
		setUniformMatrix(getUniformLocation(name), transpose, m);
	}

	public void setUniformMatrix(String name, boolean transpose, Matrix4f m) {
		setUniformMatrix(getUniformLocation(name), transpose, m);
	}

	public void setUniformMatrix(int loc, boolean transpose, Matrix3f m) {
		if (loc == -1)
			return;
		if (floatBuffer == null)
			floatBuffer = BufferUtils.createFloatBuffer(16);
		floatBuffer.clear();
		m.store(floatBuffer);
		floatBuffer.flip();
		GL.glUniformMatrix3(loc, transpose, floatBuffer);
	}

	public void setUniformMatrix(int loc, boolean transpose, Matrix4f m) {
		if (loc == -1)
			return;
		if (floatBuffer == null)
			floatBuffer = BufferUtils.createFloatBuffer(16);
		floatBuffer.clear();
		m.store(floatBuffer);
		floatBuffer.flip();
		GL.glUniformMatrix4(loc, transpose, floatBuffer);
	}

	public void setUniformf(String name, Vector2f v) {
		setUniformf(getUniformLocation(name), v);
	}

	public void setUniformf(String name, Vector3f v) {
		setUniformf(getUniformLocation(name), v);
	}

	public void setUniformf(String name, Vector4f v) {
		setUniformf(getUniformLocation(name), v);
	}

	public void setUniformf(int loc, Vector2f v) {
		if (loc == -1)
			return;
		setUniformf(loc, v.x, v.y);
	}

	public void setUniformf(int loc, Vector3f v) {
		if (loc == -1)
			return;
		setUniformf(loc, v.x, v.y, v.z);
	}

	public void setUniformf(int loc, Vector4f v) {
		if (loc == -1)
			return;
		setUniformf(loc, v.x, v.y, v.z, v.w);
	}

	/**
	 * Set the strict mode of the shader
	 * 
	 * @param enabled
	 */
	public static void setStrictMode(boolean strict) {
		Shader.strict = strict;
	}

	/**
	 * @return if the shader is in strict mode
	 */
	public static boolean isStrictMode() {
		return strict;
	}

	// "struct" for attribute
	protected static class Attribute {
		/** name of attribute */
		String name = null;
		
		/** attribute type, -1 is default */
		int type = -1;
		
		/** size of attribute */
		int size = 0;
		
		/** location of attribute */
		int location = -1;
	}

}
