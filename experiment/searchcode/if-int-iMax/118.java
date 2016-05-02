package util;

import com.jogamp.common.nio.Buffers;
import common.GLSLAttrib;
import common.VertexBufferObject;
import common.math.Mat4;
import common.math.Point2;
import common.math.Point4;
import common.math.Vec2;
import common.math.Vec4;
import common.math.VectorMath;
import common.shaders.Program;
import io.Logger;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import javax.media.opengl.GL3;
import scene.Terrain;
import scene.TerrainProperties;


/**
 * Generate Terrains and associated things (Heightmaps, VertexBufferObjects) in all sorts of manners.
 *
 * @author jeroen
 */
public class TerrainGenerator
{
	/* An immense amount of options for different kinds of noise generation algorithms that can be used */
	public static enum noiseType
	{
		SIMPLEX
	};
	private static float[][] heightmap;
	private static Point4[] points;
	private static Point2[] texCoords;
	private static Vec4[] normals;


	/**
	 * Generate a terrain to be drawn using program, moved by modelMatrix, spanning from from to to, made up of squares
	 * squareSize by squareSize, the amount of which is determined by the contents of the file pointed to by filename.
	 * This file should consist of characters that can be found quite easily in generatePropertiesArrayFromFile().
	 *
	 * @param gl
	 * @param program
	 * @param filename
	 * @param modelMatrix
	 * @param from
	 * @param to
	 * @param squareSize
	 * @param textureLength	The length in meters that any ground texture should be stretched to
	 * @param textureWidth	The width in meters that any ground texture should be stretched to
	 *
	 * @return A lot of terrains that can be nicely concatenated. They follow the layout given in the file
	 *
	 * @throws IOException
	 */
	public static Terrain[][] generateTerrainArray(GL3 gl, Program program, String filename, Mat4 modelMatrix, Vec2 from,
			Vec2 to, int squareSize, float textureLength, float textureWidth) throws IOException
	{
		TerrainProperties[][] map = generatePropertiesArrayFromFile(filename);
		Terrain[][] result = new Terrain[map.length - 1][map[0].length - 1];

		Vec2 subTerrainLength = new Vec2(to.get(0) - from.get(0), 0).mulV((float)1 / (map.length - 1));
		Vec2 subTerrainWidth = new Vec2(0, to.get(1) - from.get(1)).mulV((float)1 / (map[0].length - 1));

		for(int j = 0; j < map[0].length - 1; j++)
		{
			for(int i = 0; i < map.length - 1; i++)
			{
				TerrainProperties upperLeft, upperRight, lowerLeft, lowerRight;
				upperLeft = map[i][j];
				upperRight = map[i + 1][j];
				lowerLeft = map[i][j + 1];
				lowerRight = map[i + 1][j + 1];

				/* If any of these is null, no terrain will be generated here */
				if(upperLeft == null || upperRight == null || lowerLeft == null || lowerRight == null) continue;

				TerrainProperties[] props = new TerrainProperties[] {upperLeft, upperRight, lowerLeft, lowerRight};
				result[i][j] = generateTerrain(gl, program, modelMatrix,
						VectorMath.add(from, VectorMath.mul(subTerrainLength, i)).addV(VectorMath.mul(subTerrainWidth, j)),
						VectorMath.add(from, VectorMath.mul(subTerrainLength, i + 1)).addV(VectorMath.mul(subTerrainWidth, j + 1)),
						squareSize, squareSize,	props, textureLength, textureWidth);
			}
		}

		return result;
	}


	/**
	 * Read a file and convert characters into TerrainProperties, putting them into an array aligned like the file
	 *
	 * @param filename
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private static TerrainProperties[][] generatePropertiesArrayFromFile(String filename) throws IOException, FileNotFoundException
	{
		ArrayList<ArrayList<TerrainProperties>> map = new ArrayList<ArrayList<TerrainProperties>>();

		/* Read the file */
		File input = new File(filename);
		BufferedReader in = new BufferedReader(new FileReader(input));

		String line;
		while((line = in.readLine()) != null)
		{
			ArrayList<TerrainProperties> mapLine = new ArrayList<TerrainProperties>();

			for(int i = 0; i < line.length(); i++)
			{
				/* Determine the character */
				char c = line.charAt(i);

				/* Turn it into properties */
				TerrainProperties p = null;
				switch(c)
				{
					case 'X':
						p = TerrainProperties.MOUNTAIN;
						break;
					case 'x':
						p = TerrainProperties.LOW_MOUNTAIN;
						break;
					case '!':
						p = TerrainProperties.ALMOST_FLAT_0;
						break;
					case '@':
						p = TerrainProperties.ALMOST_FLAT_5;
						break;
					case '#':
						p = TerrainProperties.ALMOST_FLAT_10;
						break;
					case '$':
						p = TerrainProperties.ALMOST_FLAT_15;
						break;
					case '%':
						p = TerrainProperties.ALMOST_FLAT_20;
						break;
					case '^':
						p = TerrainProperties.ALMOST_FLAT_25;
						break;
					case '&':
						p = TerrainProperties.ALMOST_FLAT_30;
						break;
					case '1':
						p = TerrainProperties.FLAT_0;
						break;
					case '2':
						p = TerrainProperties.FLAT_5;
						break;
					case '3':
						p = TerrainProperties.FLAT_10;
						break;
					case '4':
						p = TerrainProperties.FLAT_15;
						break;
					case '5':
						p = TerrainProperties.FLAT_20;
						break;
					case '6':
						p = TerrainProperties.FLAT_25;
						break;
					case '7':
						p = TerrainProperties.FLAT_30;
						break;
					case ' ':
						p = null;
						break;
					default:
						p = null;
						break;
				}

				mapLine.add(p);
			}

			map.add(mapLine);
		}

		/* Convert to array */
		int iMax = 0, jMax = 0;
		for(ArrayList<TerrainProperties> mapLine : map)
		{
			jMax++;
			iMax = Math.max(iMax, mapLine.size());
		}
		TerrainProperties[][] result = new TerrainProperties[iMax][jMax];
		for(int j = 0; j < map.size(); j++)
		{
			ArrayList<TerrainProperties> mapLine = map.get(j);
			for(int i = 0; i < mapLine.size(); i++)
			{
				result[i][j] = mapLine.get(i);
			}
		}
		return result;
	}


	/**
	 * Generate a terrain to be drawn using program and moved by modelMatrix from from to to of length squares in
	 * x-direction and width squares in z-direction. Textures will be stretched to textureLength by textureWidth meters.
	 * properties can contain 1 value, in which case this is the value for the complete terrain, or four values in which
	 * case they are interpolated ({upper left, upper right, lower left, lower right}).
	 *
	 * @param gl
	 * @param program
	 * @param modelMatrix
	 * @param from
	 * @param to
	 * @param length	Power of two
	 * @param width		Power of two
	 * @param properties
	 * @param textureLength
	 * @param textureWidth
	 * @return
	 */
	public static Terrain generateTerrain(GL3 gl, Program program, Mat4 modelMatrix, Vec2 from, Vec2 to, int length,
			int width, TerrainProperties[] properties, float textureLength,	float textureWidth)
	{
		Heightmap map = generateHeightmap(from, to, length, width, properties);
		VertexBufferObject vbo = generateVBO(gl, map, textureLength, textureWidth);
		return new Terrain(program, vbo, modelMatrix, map);
	}


	/**
	 * Generates a heightmap spanning along the x and z axes between two points. The terrain is made up of length by
	 * width tiles. These two numbers have to be powers of two. The terrain is also between height extremes. The
	 * inclusion of these heights is not guaranteed.
	 * properties can contain 1 value, in which case this is the value for the complete terrain, or four values in which
	 * case they are interpolated ({upper left, upper right, lower left, lower right}).
	 *
	 * @param gl
	 * @param from		One edge of the terrain rectangle
	 * @param to		The other edge of the terrain rectangle
	 * @param width		The amount of tiles in z-direction
	 * @param length	The amount of tiles in x-direction
	 * @param minHeight The minimum y-value of the terrain
	 * @param maxHeight	The maximum y-value of the terrain
	 * @param noise		The noise type to use
	 *
	 * @return A complete VertexBufferObject with the terrain in it, ready to go.
	 */
	//TODO support other noise functions
	//TODO support different precisions of the algorithm (not just precision of the interpolation like now)
	public static Heightmap generateHeightmap(Vec2 from, Vec2 to, int length, int width, TerrainProperties[] properties)
	{
		if(!(MathHelper.isPowerOfTwo(width) && MathHelper.isPowerOfTwo(length)))
		{
			Logger.printError("Terrains can only be generated using a width and height that are a power of two");
			return null;
		}

		/* Get the upper left and lower right points describing the rectangle as seen from the positive y-axis */
		Vec2 upperLeft = MathHelper.upperLeft(from, to);
		Vec2 lowerRight = MathHelper.lowerRight(from, to);

		/* In case the requested terrain has more tiles in one direction than in the other, create multiple square
		 * terrains and concatenate them */
		if(width == length)	heightmap = generateSquareHeightmap(upperLeft, lowerRight, width, properties);
		else
		{
			heightmap = new float[length + 1][width + 1];
			/* If the area is bigger in z-direction (width > length), we need to check length by length squares, and
			 * move the points down every iteration. Likewise for length > width. */
			Vec2 shift;
			if(width > length) shift = new Vec2(0, (lowerRight.get(1) - upperLeft.get(1)) / (width / length));
			else shift = new Vec2((lowerRight.get(0) - upperLeft.get(0)) / (length / width), 0);
			/* Get the lower right corner of the first square */
			Vec2 lowerRightSquare = new Vec2(shift.length(), shift.length()).addV(upperLeft);
			/* Save bigger and smaller of the two (width and length) */
			int bigger = width > length ? width : length;
			int smaller = width > length ? length : width;

			for(int i = 0; i < bigger / smaller; i++)
			{
				Vec2 shiftI = VectorMath.mul(shift, i);
				float[][] squareHeightmap = generateSquareHeightmap(VectorMath.add(upperLeft, shiftI),
						VectorMath.add(lowerRightSquare, shiftI), smaller, properties);
				//TODO take out this step for efficiency
				for(int z = 0; z <= smaller; z++)
				{
					for(int x = 0; x <= smaller; x++)
					{
						heightmap[width > length ? x : x + i * width][width > length ? z + i * length : z] = squareHeightmap[x][z];
					}
				}
			}
		}

		return new Heightmap(heightmap, upperLeft, lowerRight);
	}


	/**
	 * Turn a HeightMap into a VertexBufferObject
	 *
	 * @param gl
	 * @param map
	 * @param textureLength
	 * @param textureWidth
	 * @return
	 */
	public static VertexBufferObject generateVBO(GL3 gl, Heightmap map, float textureLength, float textureWidth)
	{
		heightmap = map.getData();
		fillArraysTriangleStrip(heightmap.length - 1, heightmap[0].length - 1, map.getUpperLeft(), map.getLowerRight(), textureLength, textureWidth);

		FloatBuffer pointsBuffer = VectorMath.toBuffer(points);
		FloatBuffer normalsBuffer = VectorMath.toBuffer(normals);
		FloatBuffer texCoordsBuffer = VectorMath.toBuffer(texCoords);

		pointsBuffer.rewind();
		normalsBuffer.rewind();
		texCoordsBuffer.rewind();

		GLSLAttrib vertices = new GLSLAttrib(pointsBuffer, Buffers.SIZEOF_FLOAT, "vPosition", 4);
		GLSLAttrib normalsAttrib = new GLSLAttrib(normalsBuffer, Buffers.SIZEOF_FLOAT, "vNormal", 4);
		GLSLAttrib textureCoords = new GLSLAttrib(texCoordsBuffer, Buffers.SIZEOF_FLOAT, "texCoord", 2);

		return new VertexBufferObject(gl, GL3.GL_TRIANGLE_STRIP, points, vertices, normalsAttrib, textureCoords);
	}


	/**
	 * Fill the points, normals and texCoords arrays with the correct values of the vertices to draw heightmap using
	 * triangle strips if it's stretched from upperLeft to lowerRight. Textures will be stretched in x-direction by
	 * textureLength, similar for z with textureWidth.
	 *
	 * @param length
	 * @param width
	 * @param upperLeft
	 * @param lowerRight
	 * @param textureLength
	 * @param textureWidth
	 */
	public static void fillArraysTriangleStrip(int length, int width, Vec2 upperLeft, Vec2 lowerRight, float textureLength, float textureWidth)
	{
		/* Every square will be made up of two triangles given by six vertices */
		//int numPoints = width * ((length - 1) * 2 + 2 * 3); simplified:
		int numPoints = width * (length + 2) * 2;
		points = new Point4[numPoints];
		normals = new Vec4[numPoints];
		texCoords = new Point2[numPoints];

		float xIncrement = (lowerRight.get(0) - upperLeft.get(0)) / length;
		float zIncrement = (lowerRight.get(1) - upperLeft.get(1)) / width;
		int index = 0;

		/* The high (low z-value) and low (high z-value) points of the previous, current and next step in this
		 * horizontal triangle strip */
		Point4 previousHigh, previousLow, currentHigh = null, currentLow = null, nextHigh = null, nextLow = null;
		float nextX = 0, currentX = 0, highZ = 0, lowZ = 0;
		for(int j = 0; j < width; j++)
		{
			for(int i = 0; i <= length; i++)
			{
				if(i == 0)
				{
					/* Special case for the left-most two points */
					currentX = upperLeft.get(0);
					nextX = currentX + xIncrement;
					highZ = upperLeft.get(1) + j * zIncrement;
					lowZ = highZ + zIncrement;
					currentHigh = new Point4(currentX, heightmap[i][j], highZ);
					currentLow = new Point4(currentX, heightmap[i][j + 1], lowZ);
					nextHigh = new Point4(nextX, heightmap[i + 1][j], highZ);
					nextLow = new Point4(nextX, heightmap[i + 1][j + 1], lowZ);

					/* Fill the arrays for the current two points, adding the first one twice */
					points[index] = currentHigh;
					texCoords[index] = new Point2(currentX / textureLength, highZ / textureWidth);
					normals[index] = VectorMath.cross(VectorMath.sub(currentLow, currentHigh), VectorMath.sub(nextHigh, currentHigh)).normalizeV();
					index++;
					points[index] = currentHigh;
					texCoords[index] = new Point2(currentX / textureLength, highZ / textureWidth);
					normals[index] = VectorMath.cross(VectorMath.sub(currentLow, currentHigh), VectorMath.sub(nextHigh, currentHigh)).normalizeV();
					index++;
					points[index] = currentLow;
					texCoords[index] = new Point2(currentX / textureLength, lowZ / textureWidth);
					normals[index] = VectorMath.cross(VectorMath.sub(nextHigh, currentLow), VectorMath.sub(currentHigh, currentLow)).normalizeV().addV(
							VectorMath.cross(VectorMath.sub(nextLow, currentLow), VectorMath.sub(nextHigh, currentLow)).normalizeV());
					index++;
				}
				else if(i == length)
				{
					/* Special case for the right-most two points */
					currentX += xIncrement;
					nextX += xIncrement;
					previousHigh = currentHigh;
					previousLow = currentLow;
					currentHigh = nextHigh;
					currentLow = nextLow;

					/* Fill the arrays for the current two points, adding the lsat one twice */
					points[index] = currentHigh;
					texCoords[index] = new Point2(currentX / textureLength, highZ / textureWidth);
					normals[index] = VectorMath.cross(VectorMath.sub(previousHigh, currentHigh), VectorMath.sub(previousLow, currentHigh)).normalizeV().addV(
							VectorMath.cross(VectorMath.sub(previousLow, currentHigh), VectorMath.sub(currentLow, currentHigh)).normalizeV());
					index++;
					points[index] = currentLow;
					texCoords[index] = new Point2(currentX / textureLength, lowZ / textureWidth);
					normals[index] = VectorMath.cross(VectorMath.sub(currentHigh, currentLow), VectorMath.sub(previousLow, currentLow)).normalizeV();
					index++;
					points[index] = currentLow;
					texCoords[index] = new Point2(currentX / textureLength, lowZ / textureWidth);
					normals[index] = VectorMath.cross(VectorMath.sub(currentHigh, currentLow), VectorMath.sub(previousLow, currentLow)).normalizeV();
					index++;
				}
				else
				{
					/* Most often occurring situation in the middle of the strip */
					currentX += xIncrement;
					nextX += xIncrement;
					previousHigh = currentHigh;
					previousLow = currentLow;
					currentHigh = nextHigh;
					currentLow = nextLow;
					nextHigh = new Point4(nextX, heightmap[i + 1][j], highZ);
					nextLow = new Point4(nextX, heightmap[i + 1][j + 1], lowZ);

					/* Fill the arrays for the current two points */
					points[index] = currentHigh;
					texCoords[index] = new Point2(currentX / textureLength, highZ / textureWidth);
					normals[index] = VectorMath.cross(VectorMath.sub(previousHigh, currentHigh), VectorMath.sub(previousLow, currentHigh)).normalizeV().addV(
							VectorMath.cross(VectorMath.sub(previousLow, currentHigh), VectorMath.sub(currentLow, currentHigh)).normalizeV()).addV(
							VectorMath.cross(VectorMath.sub(currentLow, currentHigh), VectorMath.sub(nextHigh, currentHigh)).normalizeV());
					index++;
					points[index] = currentLow;
					texCoords[index] = new Point2(currentX / textureLength, lowZ / textureWidth);
					normals[index] = VectorMath.cross(VectorMath.sub(currentHigh, currentLow), VectorMath.sub(previousLow, currentLow)).normalizeV().addV(
							VectorMath.cross(VectorMath.sub(nextHigh, currentLow), VectorMath.sub(currentHigh, currentLow)).normalizeV()).addV(
							VectorMath.cross(VectorMath.sub(nextLow, currentLow), VectorMath.sub(nextHigh, currentLow)).normalizeV());
					index++;
				}
			}
		}
	}


	/**
	 * Fill the points, normals and texCoords arrays with the correct values of the vertices to draw heightmap using
	 * triangles if it's stretched from upperLeft to lowerRight. Textures will be stretched in x-direction by
	 * textureLength, similar for z with textureWidth.
	 *
	 * @param length
	 * @param width
	 * @param upperLeft
	 * @param lowerRight
	 * @param textureLength
	 * @param textureWidth
	 */
	public static void fillArraysTriangles(int length, int width, Vec2 upperLeft, Vec2 lowerRight, float textureLength, float textureWidth)
	{
		/* Every square will be made up of two triangles given by six vertices */
		points = new Point4[width * length * 6];
		normals = new Vec4[width * length * 6];
		texCoords = new Point2[width * length * 6];

		float xIncrement = (lowerRight.get(0) - upperLeft.get(0)) / length;
		float zIncrement = (lowerRight.get(1) - upperLeft.get(1)) / width;
		int index = 0;
		Point4 upperLeftPoint, upperRightPoint, lowerLeftPoint, lowerRightPoint;
		Point2 upperLeftTexCoord, upperRightTexCoord, lowerLeftTexCoord, lowerRightTexCoord;
		Vec4 upperLeftTriangleNormal, lowerRightTriangleNormal;
		for(int j = 0; j < width; j++)
		{
			for(int i = 0; i < length; i++)
			{
				float x = upperLeft.get(0) + i * xIncrement;
				float z = upperLeft.get(1) + j * zIncrement;

				/* Create the vertices */
				upperLeftPoint = new Point4(x, heightmap[i][j], z);
				upperRightPoint = new Point4(x + xIncrement, heightmap[i + 1][j], z);
				lowerLeftPoint = new Point4(x, heightmap[i][j + 1], z + zIncrement);
				lowerRightPoint = new Point4(x + xIncrement, heightmap[i + 1][j + 1], z + zIncrement);

				/* Set the texture coordinates so that the texture will get the prescribed size, and so that later
				 * attached terrains will not result in texture shifts */
				upperLeftTexCoord = new Point2(x / textureLength, z / textureWidth);
				upperRightTexCoord = new Point2((x + xIncrement) / textureLength, z / textureWidth);
				lowerLeftTexCoord = new Point2(x / textureLength, (z + zIncrement) / textureWidth);
				lowerRightTexCoord = new Point2((x + xIncrement) / textureLength, (z + zIncrement) / textureWidth);

				/* Create the normals for the two triangles */
				upperLeftTriangleNormal = VectorMath.cross(VectorMath.sub(lowerLeftPoint, upperLeftPoint), VectorMath.sub(upperRightPoint, upperLeftPoint));
				lowerRightTriangleNormal = VectorMath.cross(VectorMath.sub(upperRightPoint, lowerRightPoint), VectorMath.sub(lowerLeftPoint, lowerRightPoint));

				/* Fill the arrays for the upper left triangle */
				points[index] = upperLeftPoint;
				texCoords[index] = upperLeftTexCoord;
				normals[index] = upperLeftTriangleNormal;
				index++;
				points[index] = lowerLeftPoint;
				texCoords[index] = lowerLeftTexCoord;
				normals[index] = upperLeftTriangleNormal;
				index++;
				points[index] = upperRightPoint;
				texCoords[index] = upperRightTexCoord;
				normals[index] = upperLeftTriangleNormal;
				index++;

				/* Fill the arrays for the lower right triangle */
				points[index] = lowerLeftPoint;
				texCoords[index] = lowerLeftTexCoord;
				normals[index] = lowerRightTriangleNormal;
				index++;
				points[index] = lowerRightPoint;
				texCoords[index] = lowerRightTexCoord;
				normals[index] = lowerRightTriangleNormal;
				index++;
				points[index] = upperRightPoint;
				texCoords[index] = upperRightTexCoord;
				normals[index] = lowerRightTriangleNormal;
				index++;
			}
		}
	}


	/**
	 * Generate a square heightmap of size size by size from upperLeft to lowerRight.
	 * properties can contain 1 value, in which case this is the value for the complete terrain, or four values in which
	 * case they are interpolated ({upper left, upper right, lower left, lower right}).
	 *
	 * @param upperLeft
	 * @param lowerRight
	 * @param size
	 * @param properties
	 * @return
	 */
	private static float[][] generateSquareHeightmap(Vec2 upperLeft, Vec2 lowerRight, int size, TerrainProperties[] properties)
	{
		float[][] squareHeightmap = new float[size + 1][size + 1];

		for(int z = 0; z <= size; z++)
		{
			for(int x = 0; x <= size; x++)
			{
				squareHeightmap[x][z] = 0;
			}
		}

		int iterations = (int)Math.round(Math.log(size) / Math.log(2));

		for(int i = 0; i < iterations; i++)
		{
			// The amount of tiles (in one direction) for this iteration
			int tiles = (int)Math.pow(2, i);
			int tileSize = size / tiles;
			// How far is jumping one tile in the real coordinates? Divide by tileSize because jumps in the loop are
			// += tileSize
			float xIncrement = (lowerRight.get(0) - upperLeft.get(0)) / tiles / tileSize;
			float zIncrement = (lowerRight.get(1) - upperLeft.get(1)) / tiles / tileSize;

			float[][] noisemap;
			if(properties.length == 1)
			{
				noisemap = generateNoisemap(size, tileSize, upperLeft, xIncrement, zIncrement,
						properties[0].maxHeight - properties[0].minHeight, properties[0].roughness, i);
			}
			else
			{
				noisemap = generateNoisemap(size, tileSize, upperLeft, xIncrement, zIncrement, properties, i);
			}

			/* Interpolate on all points between the tile points, and add all points to the height map*/
			for(int z = 0; z <= size - tileSize; z += tileSize)
			{
				for(int x = 0; x <= size - tileSize; x += tileSize)
				{
					for(int zTile = 0; zTile < tileSize; zTile++)
					{
						for(int xTile = 0; xTile < tileSize; xTile++)
						{
							// The relative location within this tile
							float dx = (float)xTile / tileSize;
							float dz = (float)zTile / tileSize;

							squareHeightmap[x + xTile][z + zTile] += noisemap[x][z] * (1 - dx) * (1 - dz) +
									noisemap[x + tileSize][z] * (dx) * (1 - dz) +
									noisemap[x][z + tileSize] * (1 - dx) * (dz) +
									noisemap[x + tileSize][z + tileSize] * (dx) * (dz);
						}
					}
				}
			}
			/* Interpolate the edge values */
			squareHeightmap[size][size] += noisemap[size][size];
			for(int x = 0; x <= size - tileSize; x += tileSize)
			{
				for(int xTile = 0; xTile < tileSize; xTile++)
				{
					float dx = (float)xTile / tileSize;
					squareHeightmap[x + xTile][size] += noisemap[x][size] * (1 - dx) + noisemap[x + tileSize][size] * dx;
				}
			}
			for(int z = 0; z <= size - tileSize; z += tileSize)
			{
				for(int zTile = 0; zTile < tileSize; zTile++)
				{
					float dz = (float)zTile / tileSize;
					squareHeightmap[size][z + zTile] += noisemap[size][z] * (1 - dz) + noisemap[size][z + tileSize] * dz;
				}
			}
		}

		if(properties.length == 1) raiseToMinimumHeight(size, squareHeightmap, properties[0]);
		else raiseToMinimumHeight(size, squareHeightmap, properties);
		return squareHeightmap;
	}


	/**
	 * Generate a noise map, size by size tiles, skipping over tileSize tiles every time, with the upper left point at
	 * upperLeft, and in world coordinates jupming xIncrement in positive x-direction and zIncrement in positive
	 * z-direction with every tile, of heightsbetween -heightSpan/2 and heightSpan/2. This should be the iterationth
	 * iteration of a bigger loop filling the complete height map. The complete noisemap will be filled by interpolation
	 * every call to this function.
	 *
	 *
	 * @param size
	 * @param tileSize
	 * @param upperLeft
	 * @param xIncrement
	 * @param zIncrement
	 * @param heightSpan
	 * @param roughness
	 * @param iteration
	 * @return
	 */
	private static float[][] generateNoisemap(int size, int tileSize, Vec2 upperLeft, float xIncrement,
			float zIncrement, float heightSpan, float roughness, int iteration)
	{
		float[][] noisemap = new float[size + 1][size + 1];

		/* Get random values at the tile points, which may be far apart */
		float quarterHeightSpan = heightSpan / 4;
		for(int z = 0; z <= size; z += tileSize)
		{
			for(int x = 0; x <= size; x += tileSize)
			{
				/* The maximum height is a little tricky. There are many iterations, and every iteration adds to
				 * the last one. The final values need to span a height of the maximum height difference (it will
				 * later be raised to the minumum height). If the maximum height difference is 40, that means values
				 * should range from -20 to 20 here. They come out from -1 to 1. To make sure that after adding all
				 * iterations together they fulfill this requirement, the height that comes out in the first
				 * iteration is adjusted to range from -10 to 10, the second from -5 to 5, etc. */
				noisemap[x][z] = (SimplexNoise.noise(upperLeft.get(0) + x * xIncrement, upperLeft.get(1)
						+ z * zIncrement) * quarterHeightSpan) / (float)Math.pow(roughness, iteration);
			}
		}

		return noisemap;
	}


	/**
	 * Like generateNoisemap above, but interpolating the terrain properties over the map.
	 * @param size
	 * @param tileSize
	 * @param upperLeft
	 * @param xIncrement
	 * @param zIncrement
	 * @param properties
	 * @param iteration
	 * @return
	 */
	private static float[][] generateNoisemap(int size, int tileSize, Vec2 upperLeft, float xIncrement,
			float zIncrement, TerrainProperties[] properties, int iteration)
	{
		float[][] noisemap = new float[size + 1][size + 1];
		/* Extract the properties, upper and lower left and right */
		TerrainProperties ULP, URP, LLP, LRP;
		if(properties.length == 1) ULP = URP = LLP = LRP = properties[0];
		else
		{
			ULP = properties[0];
			URP = properties[1];
			LLP = properties[2];
			LRP = properties[3];
		}
		/* Get the differences in the values along the edges */
		// Top, from left to right
		float maxHeightDiffTop = URP.maxHeight - ULP.maxHeight;
		float minHeightDiffTop = URP.minHeight - ULP.minHeight;
		float roughnessDiffTop = URP.roughness - ULP.roughness;
		// Left, from top to bottom
		float maxHeightDiffLeft = LLP.maxHeight - ULP.maxHeight;
		float minHeightDiffLeft = LLP.minHeight - ULP.minHeight;
		float roughnessDiffLeft = LLP.roughness - ULP.roughness;
		// Right, from bottom to top
		float maxHeightDiffRight = URP.maxHeight - LRP.maxHeight;
		float minHeightDiffRight = URP.minHeight - LRP.minHeight;
		float roughnessDiffRight = URP.roughness - LRP.roughness;
		// Bottom, from left to right
		float maxHeightDiffBottom = LRP.maxHeight - LLP.maxHeight;
		float minHeightDiffBottom = LRP.minHeight - LLP.minHeight;
		float roughnessDiffBottom = LRP.roughness - LLP.roughness;

		/* Get random values at the tile points, which may be far apart */
		for(int z = 0; z <= size; z += tileSize)
		{
			float dz = (float)z / size;
			for(int x = 0; x <= size; x += tileSize)
			{
				float dx = (float)x / size;
				float minHeight;
				float maxHeight;
				float roughness;
				/* Find out which triangle of the heightmap we're in */
				if(dx + dz > 1)
				{
					/* Lower right triangle */
					minHeight = LLP.minHeight + minHeightDiffBottom * dx + minHeightDiffRight * (1 - dz);
					maxHeight = LLP.maxHeight + maxHeightDiffBottom * dx + maxHeightDiffRight * (1 - dz);
					roughness = LLP.roughness + roughnessDiffBottom * dx + roughnessDiffRight * (1 - dz);
				}
				else
				{
					/* Upper left triangle */
					minHeight = ULP.minHeight + minHeightDiffTop * dx + minHeightDiffLeft * dz;
					maxHeight = ULP.maxHeight + maxHeightDiffTop * dx + maxHeightDiffLeft * dz;
					roughness = ULP.roughness + roughnessDiffTop * dx + roughnessDiffLeft * dz;
				}
				noisemap[x][z] = (SimplexNoise.noise(upperLeft.get(0) + x * xIncrement, upperLeft.get(1)
						+ z * zIncrement) * (maxHeight - minHeight) / 4) / (float)Math.pow(roughness, iteration);
			}
		}

		return noisemap;
	}


	/**
	 * Raise the complete heightmap from being around 0 to the minimum being at the minimum height of the properties.
	 *
	 * @param size
	 * @param squareHeightmap
	 * @param properties
	 */
	private static void raiseToMinimumHeight(int size, float[][] squareHeightmap, TerrainProperties properties)
	{
		/* Move the whole height map so it aligns with the minimum value */
		//(a - b) / 2 + b = (a + b) / 2
		float raise = (properties.maxHeight + properties.minHeight) / 2;
		for(int z = 0; z <= size; z++)
		{
			for(int x = 0; x <= size; x++)
			{
				squareHeightmap[x][z] += raise;
			}
		}
	}


	/**
	 * Raise the complete heightmap from being around 0 to the minimum being at the minimum height of the properties.
	 *
	 * @param size
	 * @param squareHeightmap
	 * @param properties Four TerrainProperties
	 */
	private static void raiseToMinimumHeight(int size, float[][] squareHeightmap, TerrainProperties[] properties)
	{
		/* Extract the properties, upper and lower left and right */
		TerrainProperties ULP, URP, LLP, LRP;
		if(properties.length == 1) ULP = URP = LLP = LRP = properties[0];
		else
		{
			ULP = properties[0];
			URP = properties[1];
			LLP = properties[2];
			LRP = properties[3];
		}
		/* Get the differences in the values along the edges */
		// Top, from left to right
		float maxHeightDiffTop = URP.maxHeight - ULP.maxHeight;
		float minHeightDiffTop = URP.minHeight - ULP.minHeight;
		// Left, from top to bottom
		float maxHeightDiffLeft = LLP.maxHeight - ULP.maxHeight;
		float minHeightDiffLeft = LLP.minHeight - ULP.minHeight;
		// Right, from bottom to top
		float maxHeightDiffRight = URP.maxHeight - LRP.maxHeight;
		float minHeightDiffRight = URP.minHeight - LRP.minHeight;
		// Bottom, from left to right
		float maxHeightDiffBottom = LRP.maxHeight - LLP.maxHeight;
		float minHeightDiffBottom = LRP.minHeight - LLP.minHeight;

		for(int z = 0; z <= size; z ++)
		{
			float dz = (float)z / size;
			for(int x = 0; x <= size; x ++)
			{
				float dx = (float)x / size;
				float minHeight;
				float maxHeight;
				/* Find out which triangle of the heightmap we're in */
				if(dx + dz > 1)
				{
					/* Lower right triangle */
					minHeight = LLP.minHeight + minHeightDiffBottom * dx + minHeightDiffRight * (1 - dz);
					maxHeight = LLP.maxHeight + maxHeightDiffBottom * dx + maxHeightDiffRight * (1 - dz);
				}
				else
				{
					/* Upper left triangle */
					minHeight = ULP.minHeight + minHeightDiffTop * dx + minHeightDiffLeft * dz;
					maxHeight = ULP.maxHeight + maxHeightDiffTop * dx + maxHeightDiffLeft * dz;
				}
				squareHeightmap[x][z] += (maxHeight + minHeight) / 2;
			}
		}
	}
}

