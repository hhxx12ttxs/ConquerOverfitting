package com.game30.javagl.programs;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL43;

import com.game30.javagl.GLMagicInteger;
import com.game30.javagl.GLMagicIntegerException;

/**
 * Defines the programmable stages of the OpenGL rendering pipeline.   Each of these stages represents a particular type
 * of programmable processing.  Each stage has a set of inputs and outputs, which are passed from prior stages and on to
 * subsequent stages (whether programmable or not).
 *
 * <p>While shader stages do use the same language, each stage has a separate set of inputs and outputs, as well as
 * built-in variables. As such, shader objects are built for a specific shader stage.  So while program objects can
 * contain multiple stages, shader objects only contain code for a single stage.
 *
 * @author Brian Norman
 * @version 1.0.0-SNAPSHOT
 * @see <a href="http://www.opengl.org/wiki/Shader#Stages">Shader Stages</a> on the OpenGL wiki
 * @since 1.0.0
 */
public enum GLShaderStage implements GLMagicInteger {

    /**
     * Handles the processing of individual vertices. Vertex shaders are fed Vertex Attribute data, as specified from a
     * vertex array object by a rendering command.  A vertex shader receives a single vertex from the vertex stream and
     * generates a single vertex to the output vertex stream.  There must be a 1:1 mapping from input vertices to output
     * vertices.
     *
     * @see <a href="http://www.opengl.org/wiki/Vertex_Shader">Vertex Shader</a> on the OpenGL wiki
     */
    Vertex(GL20.GL_VERTEX_SHADER),

    /**
     * A user-supplied program that, when executed, will process a Fragment from the rasterization process into a set of
     * colors and a single depth value.
     *
     * @see <a href="http://www.opengl.org/wiki/Fragment_Shader">Fragment Shader</a> on the OpenGL wiki
     */
    Fragment(GL20.GL_FRAGMENT_SHADER),

    /**
     * A Shader program written in GLSL that governs the processing of Primitives.  Geometry shaders reside between the
     * Vertex Shaders (or the optional Tessellation stage) and the fixed-function Vertex Post-Processing stage.
     *
     * @see <a href="http://www.opengl.org/wiki/Geometry_Shader">Geometry Shader</a> on the OpenGL wiki
     */
    Geometry(GL32.GL_GEOMETRY_SHADER),

    /**
     * a Shader program written in GLSL. It sits between the Vertex Shader and the Tessellation Evaluation Shader.  The
     * TCS controls how much tessellation a particular patch gets; it also defines the size of a patch, thus allowing it
     * to augment data.  It can also filter vertex data taken from the vertex shader.  The main purpose of the TCS is to
     * feed the tessellation levels to the Tessellation primitive generator stage, as well as to feed patch data (as its
     * output values) to the Tessellation Evaluation Shader stage.
     *
     * @see <a href="http://www.opengl.org/wiki/Tessellation_Control_Shader">Tessellation Control Shader</a> on the
     * OpenGL wiki
     */
    TessellationControl(GL40.GL_TESS_CONTROL_SHADER),

    /**
     * A Shader program written in GLSL that takes the results of a Tessellation operation and computes the interpolated
     * positions and other per-vertex data from them.  These values are passed on to the next stage in the pipeline.
     *
     * @see <a href="http://www.opengl.org/wiki/Tessellation_Evaluation_Shader">Tessellation Evaluation Shader</a> on
     * the OpenGL wiki
     */
    TessellationEvaluation(GL40.GL_TESS_EVALUATION_SHADER),

    /**
     * Used entirely for computing arbitrary information.  While it can do rendering, it is generally used for tasks not
     * directly related to drawing triangles and pixels.
     *
     * @see <a href="http://www.opengl.org/wiki/Compute_Shader">Geometry Shader</a> on the OpenGL wiki
     */
    Compute(GL43.GL_COMPUTE_SHADER),

    // End of enumeration
    ;

    /**
     * Returns the GLShaderStage that corresponds to the specified OpenGL magic integer.
     *
     * @param glInt the stage magic integer.
     * @return the corresponding GLShaderStage.
     * @throws GLMagicIntegerException if the specified magic integer does not match a enumeration instance.
     */
    public static GLShaderStage fromGLInt(int glInt) throws GLMagicIntegerException {
        // While it may not be the most elegant, switch is definitely the fastest.
        switch (glInt) {
            case GL20.GL_VERTEX_SHADER:
                return GLShaderStage.Vertex;
            case GL20.GL_FRAGMENT_SHADER:
                return GLShaderStage.Fragment;
            case GL32.GL_GEOMETRY_SHADER:
                return GLShaderStage.Geometry;
            case GL40.GL_TESS_CONTROL_SHADER:
                return GLShaderStage.TessellationControl;
            case GL40.GL_TESS_EVALUATION_SHADER:
                return GLShaderStage.TessellationEvaluation;
            case GL43.GL_COMPUTE_SHADER:
                return GLShaderStage.Compute;
            default:
                throw new GLMagicIntegerException("Could not match to a GLShaderStage.");
        }
    }

    /** The OpenGL magic integer of the stage. */
    private final int glInt;

    /**
     * Creates a new shader stage with the specified magic integer.
     *
     * @param glInt the stage magic integer.
     */
    GLShaderStage(int glInt) {
        this.glInt = glInt;
    }

    @Override
    public int glInt() {
        return glInt;
    }
}

