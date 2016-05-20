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
package com.ngt.jopenmetaverse.shared.sim.rendering;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import com.ngt.jopenmetaverse.shared.sim.imaging.IBitmap;
import com.ngt.jopenmetaverse.shared.types.Vector2;
import com.ngt.jopenmetaverse.shared.types.Vector3;
import com.ngt.jopenmetaverse.shared.types.Vector4;


public class RHelp
{
	public final static Vector3 InvalidPosition = new Vector3(99999f, 99999f, 99999f);
	static float t1 = 0.075f;
	static float t2 = t1 / 5.7f;

	public static Vector3 Smoothed1stOrder(Vector3 curPos, Vector3 targetPos, float lastFrameTime)
	{
		int numIterations = (int)(lastFrameTime * 100);
		do
		{
			//			curPos += (targetPos - curPos)*t1;
			curPos = Vector3.subtract(targetPos, curPos).multiply(t1).add(curPos);
			numIterations--;
		}
		while (numIterations > 0);
		if (Vector3.distanceSquared(curPos, targetPos) < 0.00001f)
		{
			curPos = targetPos;
		}
		return curPos;
	}

	/*
	 * @param 
	 * 
	 * 
	 * @return Vector3: Current Position
	 * @return Return accelration by updating accel[0] element
	 */
	public static Vector3 Smoothed2ndOrder(Vector3 curPos, Vector3 targetPos, Vector3[] accel, float lastFrameTime)
	{
		int numIterations = (int)(lastFrameTime * 100);
		do
		{
			//			                accel += (targetPos - accel - curPos) * t1;
			accel[0] = Vector3.subtract(targetPos, curPos).subtract(accel[0]).multiply(t1).subtract(accel[0]);
			//			                curPos += accel * t2;
			curPos = Vector3.multiply(accel[0], t2).add(curPos);
			numIterations--;
		}
		while (numIterations > 0);
		if (Vector3.distanceSquared(curPos, targetPos) < 0.00001f)
		{
			curPos = targetPos;
		}
		return curPos;
	}

	public static org.lwjgl.util.vector.Vector2f TKVector3(Vector2 v)
	{
		return new org.lwjgl.util.vector.Vector2f(v.X, v.Y);
	}

	public static org.lwjgl.util.vector.Vector3f TKVector3(Vector3 v)
	{
		return new org.lwjgl.util.vector.Vector3f(v.X, v.Y, v.Z);
	}

	public static org.lwjgl.util.vector.Vector4f TKVector3(Vector4 v)
	{
		return new org.lwjgl.util.vector.Vector4f(v.X, v.Y, v.Z, v.W);
	}

	public static Vector2 OMVVector2(org.lwjgl.util.vector.Vector2f v)
	{
		return new Vector2(v.x, v.y);
	}

	public static Vector3 OMVVector3(org.lwjgl.util.vector.Vector3f v)
	{
		return new Vector3(v.x, v.y, v.z);
	}

	public static Vector4 OMVVector4(org.lwjgl.util.vector.Vector4f v)
	{
		return new Vector4(v.x, v.y, v.z, v.w);
	}

	//			        public static Color WinColor(OpenTK.Graphics.Color4f color)
	//			        {
	//			            return Color.FromArgb((int)(color.A * 255), (int)(color.R * 255), (int)(color.G * 255), (int)(color.B * 255));
	//			        }
	//
	//			        public static Color WinColor(Color4 color)
	//			        {
	//			            return Color.FromArgb((int)(color.getA() * 255), (int)(color.getR() * 255), (int)(color.getG() * 255), (int)(color.getB() * 255));
	//			        }

	public static int NextPow2(int start)
	{
		int pow = 1;
		while (pow < start) pow *= 2;
		return pow;
	}

//	/*
//	 * use AssetCache LoadCompressedImage method  
//	 */
//	@Deprecated
//	public static LoadCachedImageResult LoadCachedImage(AssetCache assetCache, UUID textureID) throws IOException
//	{
//		LoadCachedImageResult r = null;
//		if(!assetCache.hasAsset(textureID))
//		{
//			return null;
//		}
//		else
//		{
//			int i = 0;
//			r = new LoadCachedImageResult();
//			byte[] cachedData = assetCache.getCachedAssetBytes(textureID);
//			//Check if the file is actually compressed texture image
//			byte[] header = new byte[36];
//			Utils.arraycopy(cachedData, 0, header, 0, header.length);
//			if (!COMPRESSED_IMAGE_MAGIC_HEADER.equals(Utils.bytesToString(header, 0, COMPRESSED_IMAGE_MAGIC_HEADER.length())))
//			{
//				return null;
//			}
//			
//			i += COMPRESSED_IMAGE_MAGIC_HEADER.length();
//
//			if (header[i++] != 1) // check version
//			{
//				return null;
//			}
//			
//			r.hasAlpha = header[i++] == 1;
//			r.fullAlpha = header[i++] == 1;
//			r.isMask = header[i++] == 1;
//			
//			int uncompressedSize = Utils.bytesToInt(header, i);
//			i += 4;
//
//			textureID = new UUID(header, i);
//			i += 16;
//
//			r.tgaData = new byte[uncompressedSize];
//			ByteArrayInputStream bis = new ByteArrayInputStream(cachedData, i, uncompressedSize); 
//			DeflaterInputStream compressed = new DeflaterInputStream(bis);
//			{
//				int read = 0;
//				while ((read = compressed.read(r.tgaData, read, uncompressedSize - read)) > 0) ;
//			}
//			compressed.close();
//			bis.close();
//		}
//		r.success = true;
//		return r;
//		}
		
//		try
//		{
//			String fname = FileUtils.combineFilePath(settings.ASSET_CACHE_DIR, String.format("%s.rzi", textureID));
//			//string fname = System.IO.Path.Combine(".", string.Format("{0}.rzi", textureID));
//
//			File f = new File(fname);
//			FileInputStream fis = new FileInputStream(f);			
//			{
//				byte[] header = new byte[36];
//				int i = 0;
//				fis.read(header, 0, header.length);
//
//				// check if the file is starting with magic string
//				if (!RAD_IMG_MAGIC.equals(Utils.bytesToString(header, 0, RAD_IMG_MAGIC.length())))
//				{
//					r.success = false;
//					return r;
//				}
//				
//				i += RAD_IMG_MAGIC.length();
//
//				if (header[i++] != 1) // check version
//				{
//					r.success = false;
//					return r;
//				}
//
//				r.hasAlpha = header[i++] == 1;
//				r.fullAlpha = header[i++] == 1;
//				r.isMask = header[i++] == 1;
//
//				int uncompressedSize = Utils.bytesToInt(header, i);
//				i += 4;
//
//				textureID = new UUID(header, i);
//				i += 16;
//
//				r.tgaData = new byte[uncompressedSize];
//				DeflaterInputStream compressed = new DeflaterInputStream(fis);
//				{
//					int read = 0;
//					while ((read = compressed.read(r.tgaData, read, uncompressedSize - read)) > 0) ;
//				}
//			}
//			r.success = true;
//			return r;
//		}
//		catch (FileNotFoundException e) { }
//		catch (Exception ex)
//		{
//			JLogger.debug(String.format("Failed to load radegast cache file %s: %s", textureID, Utils.getExceptionStackTraceAsString(ex)));
//		}
//		r.success = false;
//		return r;
//	}

//	public static boolean SaveCachedImage(AssetCache assetCache, byte[] tgaData, UUID textureID, boolean hasAlpha, boolean fullAlpha, boolean isMask) throws IOException
//	{
//		
//		ByteArrayOutputStream fis = new ByteArrayOutputStream(); 
//		int i = 0;
//		// magic header
//		fis.write(Utils.stringToBytes(COMPRESSED_IMAGE_MAGIC_HEADER), 0, COMPRESSED_IMAGE_MAGIC_HEADER.length());
//		i += COMPRESSED_IMAGE_MAGIC_HEADER.length();
//
//		// version
//		fis.write((byte)1);
//		i++;
//
//		// texture info
//		fis.write(hasAlpha ? (byte)1 : (byte)0);
//		fis.write(fullAlpha ? (byte)1 : (byte)0);
//		fis.write(isMask ? (byte)1 : (byte)0);
//		i += 3;
//
//		// texture size
//		byte[] uncompressedSize = Utils.intToBytes(tgaData.length);
//		fis.write(uncompressedSize, 0, uncompressedSize.length);
//		i += uncompressedSize.length;
//
//		// texture id
//		byte[] id = new byte[16];
//		textureID.ToBytes(id, 0);
//		fis.write(id, 0, 16);
//		i += 16;
//
//		// compressed texture data
//		DeflaterOutputStream compressed = new DeflaterOutputStream(fis);
//		{
//			compressed.write(tgaData, 0, tgaData.length);
//		}
//		
//		assetCache.saveAssetToCache(textureID, fis.toByteArray());
//		compressed.close();
//		fis.close();
//		return true;
//	}
		
//		try
//		{
//			String fname = FileUtils.combineFilePath(settings.ASSET_CACHE_DIR, String.format("%s.rzi", textureID));
//			//string fname = System.IO.Path.Combine(".", string.Format("{0}.rzi", textureID));
//
//			File f = new File(fname);
//			FileOutputStream fis = new FileOutputStream(f);				
//			{
//				int i = 0;
//				// magic header
//				fis.write(Utils.stringToBytes(COMPRESSED_IMAGE_MAGIC_HEADER), 0, COMPRESSED_IMAGE_MAGIC_HEADER.length());
//				i += COMPRESSED_IMAGE_MAGIC_HEADER.length();
//
//				// version
//				fis.write((byte)1);
//				i++;
//
//				// texture info
//				fis.write(hasAlpha ? (byte)1 : (byte)0);
//				fis.write(fullAlpha ? (byte)1 : (byte)0);
//				fis.write(isMask ? (byte)1 : (byte)0);
//				i += 3;
//
//				// texture size
//				byte[] uncompressedSize = Utils.intToBytes(tgaData.length);
//				fis.write(uncompressedSize, 0, uncompressedSize.length);
//				i += uncompressedSize.length;
//
//				// texture id
//				byte[] id = new byte[16];
//				textureID.ToBytes(id, 0);
//				fis.write(id, 0, 16);
//				i += 16;
//
//				// compressed texture data
//				DeflaterOutputStream compressed = new DeflaterOutputStream(fis);
//				{
//					compressed.write(tgaData, 0, tgaData.length);
//				}
//			}
//			return true;
//		}
//		catch (Exception ex)
//		{
//			JLogger.debug(String.format("Failed to save radegast cache file {0}: {1}", textureID, Utils.getExceptionStackTraceAsString(ex)));
//			return false;
//		}
//	}
	//endregion Cached image save and load

	//region Static vertices and indices for a cube (used for bounding box drawing)
	/**********************************************
			          5 --- 4
			         /|    /|
			        1 --- 0 |
			        | 6 --| 7
			        |/    |/
			        2 --- 3
	 ***********************************************/
	public final static float[] CubeVertices = new float[]
			{
		0.5f,  0.5f,  0.5f, // 0
		-0.5f,  0.5f,  0.5f, // 1
		-0.5f, -0.5f,  0.5f, // 2
		0.5f, -0.5f,  0.5f, // 3
		0.5f,  0.5f, -0.5f, // 4
		-0.5f,  0.5f, -0.5f, // 5
		-0.5f, -0.5f, -0.5f, // 6
		0.5f, -0.5f, -0.5f  // 7
			};

	public final static short[] CubeIndices = new short[]
			{
		0, 1, 2, 3,     // Front Face
		4, 5, 6, 7,     // Back Face
		1, 2, 6, 5,     // Left Face
		0, 3, 7, 4,     // Right Face
		0, 1, 5, 4,     // Top Face
		2, 3, 7, 6      // Bottom Face
			};
	//endregion Static vertices and indices for a cube (used for bounding box drawing)

	public static int GLLoadImage(IBitmap bitmap, boolean hasAlpha) throws Exception
	{
		return GLLoadImage(bitmap, hasAlpha, true);
	}

	public static int GLLoadImage(IBitmap bitmap, boolean hasAlpha, boolean useMipmap) throws Exception
	{
		int textureId = -1;
		useMipmap = useMipmap && RenderSettings.HasMipmap;
		IntBuffer textureIDBuffer = BufferUtils.createIntBuffer(1);
		GL11.glGenTextures(textureIDBuffer);
		textureId = textureIDBuffer.get(0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
		textureIDBuffer.clear();
		
		
		byte[] data = bitmap.exportRAW();
		ByteBuffer imageBuffer = ByteBuffer.allocateDirect(data.length);
		imageBuffer.order(ByteOrder.nativeOrder());
		imageBuffer.put(data, 0, data.length);
		imageBuffer.position(0);
		
		GL11.glTexImage2D(
				GL11.GL_TEXTURE_2D,
				0,
				hasAlpha ? GL11.GL_RGBA : GL11.GL_RGB,
						bitmap.getWidth(),
						bitmap.getHeight(),
						0,
						hasAlpha ?  GL11.GL_RGBA : GL11.GL_RGB,
								GL11.GL_UNSIGNED_BYTE,
								imageBuffer);

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S , GL11.GL_REPEAT );
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T , GL11.GL_REPEAT);
		if (useMipmap)
		{
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER , GL11.GL_LINEAR_MIPMAP_LINEAR );
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL14.GL_GENERATE_MIPMAP, 1);
			GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
		}
		else
		{
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		}	 
		return textureId;
	}

	//	public static void Draw2DBox(float x, float y, float width, float height, float depth)
	//	{
	//		GL.Begin(BeginMode.Quads);
	//		{
	//			GL.TexCoord2(0, 1);
	//			GL.Vertex3(x, y, depth);
	//			GL.TexCoord2(1, 1);
	//			GL.Vertex3(x + width, y, depth);
	//			GL.TexCoord2(1, 0);
	//			GL.Vertex3(x + width, y + height, depth);
	//			GL.TexCoord2(0, 0);
	//			GL.Vertex3(x, y + height, depth);
	//		}
	//		GL.End();
	//	}
	
	private static FloatBuffer ambientDataBuffer = createFloatBuffer(new float[] { 0.2f, 0.2f, 0.2f, 1.0f });
	private static FloatBuffer diffusionDataBuffer = createFloatBuffer(new float[] { 0.8f, 0.8f, 0.8f, 1.0f });
	private static FloatBuffer specularDataBuffer = createFloatBuffer(new float[] { 0f, 0f, 0f, 1.0f });
	private static FloatBuffer emissionDataBuffer = createFloatBuffer(new float[] { 0f, 0f, 0f, 1.0f });
	
	
	private static FloatBuffer createFloatBuffer(float[] data)
	{
		FloatBuffer ambientDataBuffer = ByteBuffer
         	    .allocateDirect(data.length*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		 ambientDataBuffer.put(data);
		 ambientDataBuffer.position(0);
		 return ambientDataBuffer;
	}
	
		public static void ResetMaterial()
		{
			
			GL11.glMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT, ambientDataBuffer);
			GL11.glMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_DIFFUSE, diffusionDataBuffer);
			GL11.glMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_SPECULAR, specularDataBuffer);
			GL11.glMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_EMISSION, emissionDataBuffer);
			GL11.glMaterialf(GL11.GL_FRONT_AND_BACK, GL11.GL_SHININESS, 0f);
			//TODO why ShaderProgram need static stop()
//			ShaderProgram.Stop();
		}
	
	//
	//
	//
	//			    public static class MeshToOBJ
	//			    {
	//			        public static boolean MeshesToOBJ(Dictionary<uint, FacetedMesh> meshes, string filename)
	//			        {
	//			            StringBuilder obj = new StringBuilder();
	//			            StringBuilder mtl = new StringBuilder();
	//
	//			            FileInfo objFileInfo = new FileInfo(filename);
	//
	//			            string mtlFilename = objFileInfo.FullName.Substring(objFileInfo.DirectoryName.Length + 1,
	//			                objFileInfo.FullName.Length - (objFileInfo.DirectoryName.Length + 1) - 4) + ".mtl";
	//
	//			            obj.AppendLine("# Created by libprimrender");
	//			            obj.AppendLine("mtllib ./" + mtlFilename);
	//			            obj.AppendLine();
	//
	//			            mtl.AppendLine("# Created by libprimrender");
	//			            mtl.AppendLine();
	//
	//			            int primNr = 0;
	//			            foreach (FacetedMesh mesh in meshes.Values)
	//			            {
	//			                for (int j = 0; j < mesh.Faces.Count; j++)
	//			                {
	//			                    Face face = mesh.Faces[j];
	//
	//			                    if (face.Vertices.Count > 2)
	//			                    {
	//			                        string mtlName = String.Format("material{0}-{1}", primNr, face.ID);
	//			                        Primitive.TextureEntryFace tex = face.TextureFace;
	//			                        string texName = tex.TextureID.ToString() + ".tga";
	//
	//			                        // FIXME: Convert the source to TGA (if needed) and copy to the destination
	//
	//			                        float shiny = 0.00f;
	//			                        switch (tex.Shiny)
	//			                        {
	//			                            case Shininess.High:
	//			                                shiny = 1.00f;
	//			                                break;
	//			                            case Shininess.Medium:
	//			                                shiny = 0.66f;
	//			                                break;
	//			                            case Shininess.Low:
	//			                                shiny = 0.33f;
	//			                                break;
	//			                        }
	//
	//			                        obj.AppendFormat("g face{0}-{1}{2}", primNr, face.ID, Environment.NewLine);
	//
	//			                        mtl.AppendLine("newmtl " + mtlName);
	//			                        mtl.AppendFormat("Ka {0} {1} {2}{3}", tex.RGBA.R, tex.RGBA.G, tex.RGBA.B, Environment.NewLine);
	//			                        mtl.AppendFormat("Kd {0} {1} {2}{3}", tex.RGBA.R, tex.RGBA.G, tex.RGBA.B, Environment.NewLine);
	//			                        //mtl.AppendFormat("Ks {0} {1} {2}{3}");
	//			                        mtl.AppendLine("Tr " + tex.RGBA.A);
	//			                        mtl.AppendLine("Ns " + shiny);
	//			                        mtl.AppendLine("illum 1");
	//			                        if (tex.TextureID != UUID.Zero && tex.TextureID != Primitive.TextureEntry.WHITE_TEXTURE)
	//			                            mtl.AppendLine("map_Kd ./" + texName);
	//			                        mtl.AppendLine();
	//
	//			                        // Write the vertices, texture coordinates, and vertex normals for this side
	//			                        for (int k = 0; k < face.Vertices.Count; k++)
	//			                        {
	//			                            Vertex vertex = face.Vertices[k];
	//
	//			                            //region Vertex
	//
	//			                            Vector3 pos = vertex.Position;
	//
	//			                            // Apply scaling
	//			                            pos *= mesh.Prim.Scale;
	//
	//			                            // Apply rotation
	//			                            pos *= mesh.Prim.Rotation;
	//
	//			                            // The root prim position is sim-relative, while child prim positions are
	//			                            // parent-relative. We want to apply parent-relative translations but not
	//			                            // sim-relative ones
	//			                            if (mesh.Prim.ParentID != 0)
	//			                                pos += mesh.Prim.Position;
	//
	//			                            obj.AppendFormat("v {0} {1} {2}{3}", pos.X, pos.Y, pos.Z, Environment.NewLine);
	//
	//			                            //endregion Vertex
	//
	//			                            //region Texture Coord
	//
	//			                            obj.AppendFormat("vt {0} {1}{2}", vertex.TexCoord.X, vertex.TexCoord.Y,
	//			                                Environment.NewLine);
	//
	//			                            //endregion Texture Coord
	//
	//			                            //region Vertex Normal
	//
	//			                            // HACK: Sometimes normals are getting set to <NaN,NaN,NaN>
	//			                            if (!Single.IsNaN(vertex.Normal.X) && !Single.IsNaN(vertex.Normal.Y) && !Single.IsNaN(vertex.Normal.Z))
	//			                                obj.AppendFormat("vn {0} {1} {2}{3}", vertex.Normal.X, vertex.Normal.Y, vertex.Normal.Z,
	//			                                    Environment.NewLine);
	//			                            else
	//			                                obj.AppendLine("vn 0.0 1.0 0.0");
	//
	//			                            //endregion Vertex Normal
	//			                        }
	//
	//			                        obj.AppendFormat("# {0} vertices{1}", face.Vertices.Count, Environment.NewLine);
	//			                        obj.AppendLine();
	//			                        obj.AppendLine("usemtl " + mtlName);
	//
	//			                        //region Elements
	//
	//			                        // Write all of the faces (triangles) for this side
	//			                        for (int k = 0; k < face.Indices.Count / 3; k++)
	//			                        {
	//			                            obj.AppendFormat("f -{0}/-{0}/-{0} -{1}/-{1}/-{1} -{2}/-{2}/-{2}{3}",
	//			                                face.Vertices.Count - face.Indices[k * 3 + 0],
	//			                                face.Vertices.Count - face.Indices[k * 3 + 1],
	//			                                face.Vertices.Count - face.Indices[k * 3 + 2],
	//			                                Environment.NewLine);
	//			                        }
	//
	//			                        obj.AppendFormat("# {0} elements{1}", face.Indices.Count / 3, Environment.NewLine);
	//			                        obj.AppendLine();
	//
	//			                        //endregion Elements
	//			                    }
	//			                }
	//			                primNr++;
	//			            }
	//
	//			            try
	//			            {
	//			                File.WriteAllText(filename, obj.ToString());
	//			                File.WriteAllText(mtlFilename, mtl.ToString());
	//			            }
	//			            catch (Exception)
	//			            {
	//			                return false;
	//			            }
	//
	//			            return true;
	//			        }
	//			    }
	//
	//	
	//			    /*
	//			     *  Helper classs for reading the static VFS file, call 
	//			     *  staticVFS.readVFSheaders() with the path to the static_data.db2 and static_index.db2 files
	//			     *  and it will pass and dump in to openmetaverse_data for you
	//			     *  This should only be needed to be used if LL update the static VFS in order to refresh our data
	//			     */
	//
	//			    class VFSblock
	//			    {
	//			        public int mLocation;
	//			        public int mLength;
	//			        public int mAccessTime;
	//			        public UUID mFileID;
	//			        public int mSize;
	//			        public AssetType mAssetType;
	//
	//			        public int readblock(byte[] blockdata, int offset)
	//			        {
	//
	//			            BitPack input = new BitPack(blockdata, offset);
	//			            mLocation = input.UnpackInt();
	//			            mLength = input.UnpackInt();
	//			            mAccessTime = input.UnpackInt();
	//			            mFileID = input.UnpackUUID();
	//			            int filetype = input.UnpackShort();
	//			            mAssetType = (AssetType)filetype;
	//			            mSize = input.UnpackInt();
	//			            offset += 34;
	//
	//			            Logger.Log(String.Format("Found header for {0} type {1} length {2} at {3}", mFileID, mAssetType, mSize, mLocation), Helpers.LogLevel.Info);
	//
	//			            return offset;
	//			        }
	//
	//			    }
	//
	//			    public static class StaticVFS
	//			    {
	//			        public static void readVFSheaders(String datafile, string indexfile)
	//			        {
	//			            FileStream datastream;
	//			            FileStream indexstream;
	//
	//			            datastream = File.Open(datafile, FileMode.Open);
	//			            indexstream = File.Open(indexfile, FileMode.Open);
	//
	//			            int offset = 0;
	//
	//			            byte[] blockdata = new byte[indexstream.Length];
	//			            indexstream.Read(blockdata, 0, (int)indexstream.Length);
	//
	//			            while (offset < indexstream.Length)
	//			            {
	//			                VFSblock block = new VFSblock();
	//			                offset = block.readblock(blockdata, offset);
	//
	//			                FileStream writer = File.Open(OpenMetaverse.Settings.RESOURCE_DIR + System.IO.Path.DirectorySeparatorChar + block.mFileID.ToString(), FileMode.Create);
	//			                byte[] data = new byte[block.mSize];
	//			                datastream.Seek(block.mLocation, SeekOrigin.Begin);
	//			                datastream.Read(data, 0, block.mSize);
	//			                writer.Write(data, 0, block.mSize);
	//			                writer.Close();
	//			            }
	//
	//			        }
}	
