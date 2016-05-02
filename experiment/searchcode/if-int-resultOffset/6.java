/**
 * A library to interact with Virtual Worlds such as OpenSim
 * Copyright (C) 2012  Jitendra Chauhan, Email: jitendra.chauhan@gmail.com
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.ngt.jopenmetaverse.shared.sim.rendering.math;

import com.ngt.jopenmetaverse.shared.sim.rendering.mesh.InterleavedVBO;
import com.ngt.jopenmetaverse.shared.util.Utils;


/**
 * Class that holds a mesh's data 
 * @author S??nke Sothmann
 */
public class Mesh {
	/**
	 * float array containing the vertices float values
	 */
	protected float[] verticesArray;
	/**
	 * float array containing the tex coords float values
	 */
	protected float[] texCoordsArray;
	/**
	 * float array containing the vertex normals float values
	 */
	protected float[] vertexNormalsArray;
	
	/**
	 * Default constructor, does nothing.
	 */
	public Mesh() {
	}
	
	/**
	 * Initializes the Mesh's data arrays with the given sizes
	 * @param numVerticesElements number of vertices floats
	 * @param numTexCoordsElements number of tex coords floats
	 * @param numVertexNormalsElements number of vertex normals floats
	 */
	public Mesh(int numVerticesElements, int numTexCoordsElements, int numVertexNormalsElements) {
		this.verticesArray = new float[numVerticesElements];
		this.texCoordsArray = new float[numTexCoordsElements];
		this.vertexNormalsArray = new float[numVertexNormalsElements];
	}

	/**
	 * Returns the vertex coordinates for this cube.
	 * 
	 * @return the vertex coordinates
	 */
	public float[] getVertices() {
		return verticesArray;
	}

	/**
	 * Returns the texture coordinates for this cube.
	 * 
	 * @return the texture coordinates
	 */
	public float[] getTexCoords() {
		return texCoordsArray;
	}

	/**
	 * Returns the vertex normals for this cube.
	 * 
	 * @return the vertex normals
	 */
	public float[] getVertexNormals() {
		return vertexNormalsArray;
	}
	
	/**
	 * Adds a Mesh to this Mesh, resulting in a Mesh consisting of all the data of the two Meshes
	 * @param other Mesh to add
	 * @return Mesh consisting of all the data of the two Meshes
	 */
	public Mesh add(Mesh other){
		Mesh result = new Mesh(this.verticesArray.length + other.verticesArray.length, this.texCoordsArray.length + other.texCoordsArray.length, this.vertexNormalsArray.length + other.vertexNormalsArray.length);
		
		// merge vertices
		for(int i=0; i<this.verticesArray.length; i++){
			result.verticesArray[i] = this.verticesArray[i];
		}
		for(int i=0; i<other.verticesArray.length; i++){
			result.verticesArray[this.verticesArray.length+i] = other.verticesArray[i];
		}
		
		// merge tex coords
		for(int i=0; i<this.texCoordsArray.length; i++){
			result.texCoordsArray[i] = this.texCoordsArray[i];
		}
		for(int i=0; i<other.texCoordsArray.length; i++){
			result.texCoordsArray[this.texCoordsArray.length+i] = other.texCoordsArray[i];
		}
		
		// merge vertex normals
		for(int i=0; i<this.vertexNormalsArray.length; i++){
			result.vertexNormalsArray[i] = this.vertexNormalsArray[i];
		}
		for(int i=0; i<other.vertexNormalsArray.length; i++){
			result.vertexNormalsArray[this.vertexNormalsArray.length+i] = other.vertexNormalsArray[i];
		}
		
		return result;
	}
	
	public InterleavedVBO createInterleavedVBO()
	{
		final int vertexSize = 4*3;
		final int normalSize = 4*3;
		final int texSize = 4*2;
		final int vertexOffet = 0;
		final int normalOffset = vertexSize;
		final int texOffset = vertexSize + normalSize; 
		final int stride = vertexSize + normalSize + texSize;  
		final byte[] data = new byte[verticesArray.length* 4
		                     + vertexNormalsArray.length*4 + texCoordsArray.length*4];
		
		
		byte[] verticesBytesArray = ConversionUtils.floatArrayToByteArray(verticesArray);
		byte[] vertexNormalsBytes = ConversionUtils.floatArrayToByteArray(vertexNormalsArray);
		byte[] texCoordsBytes = ConversionUtils.floatArrayToByteArray(texCoordsArray);
		
		// merge vertices
		int vertexNextOffset = 0;
		int normalNextOffset = 0;
		int texNextOffset = 0;
		int resultOffset = 0;
		for(int i=0; i<verticesBytesArray.length/vertexSize; i++)
		{
			Utils.arraycopy(verticesBytesArray, vertexNextOffset, data, resultOffset, vertexSize);
			resultOffset += vertexSize;
			Utils.arraycopy(vertexNormalsBytes, normalNextOffset, data, resultOffset, normalSize);
			resultOffset += normalSize;
			Utils.arraycopy(texCoordsBytes, texNextOffset, data, resultOffset, texSize);
			resultOffset +=  texSize;
			
			texNextOffset+= texSize;
			vertexNextOffset += vertexSize;
			normalNextOffset += normalSize;
		}
		
		return new InterleavedVBO(data, stride, vertexSize/4, texSize/4, 
				normalSize/4, vertexOffet, normalOffset, texOffset);
	}
	
	public void print()
	{
		for(int i = 0; i < verticesArray.length/3; i++)
		{
			System.out.println(String.format("Index %d: V<%f, %f %f> N<%f, %f, %f> T<%f, %f>", 
					i, verticesArray[i*3], verticesArray[i*3+1], verticesArray[i*3+2],
					vertexNormalsArray[i*3], vertexNormalsArray[i*3+1], vertexNormalsArray[i*3+2],
					texCoordsArray[i*2], texCoordsArray[i*2+1]));
		}
	}
	
}

