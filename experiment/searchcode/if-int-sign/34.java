package plia.plugin.fbx.fileio;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;


import plia.math.Vector3;
import plia.plugin.fbx.core.FbxObject;
import plia.plugin.fbx.core.base.FbxTimeMode;
import plia.plugin.fbx.core.base.FbxTimeSpan;
import plia.plugin.fbx.scene.FbxScene;
import plia.plugin.fbx.scene.animation.FbxAnimCurve;
import plia.plugin.fbx.scene.animation.FbxAnimCurveNode;
import plia.plugin.fbx.scene.geometry.FbxCluster;
import plia.plugin.fbx.scene.geometry.FbxDeformer;
import plia.plugin.fbx.scene.geometry.FbxGeometry;
import plia.plugin.fbx.scene.geometry.FbxMesh;
import plia.plugin.fbx.scene.geometry.FbxNode;
import plia.plugin.fbx.scene.geometry.FbxNodeAttribute;
import plia.plugin.fbx.scene.geometry.FbxSkeleton;
import plia.plugin.fbx.scene.geometry.FbxSkin;
import plia.plugin.fbx.scene.shading.FbxFileTexture;
import plia.plugin.fbx.scene.shading.FbxSurfaceMaterial;
import plia.plugin.fbx.scene.shading.FbxSurfacePhong;
import plia.plugin.fbx.scene.shading.FbxTexture;
import plia.util.Convert;

import android.util.Log;

public class FbxImporter
{
	// Temp
	private static int index;
	private static StringBuilder sb = new StringBuilder();
	
	public static FbxScene importScene(InputStream inputStream)
	{
		String[] list = null;
		try
		{
			InputStreamReader isr = new InputStreamReader(inputStream);
			char[] buffer = new char[inputStream.available()];
			isr.read(buffer);

			list = new String(buffer).split("\n");
			
			isr.close();
			isr = null;
		} 
		catch (IOException e)
		{
			Log.e("Error", e.getMessage());
		}

		String line = "";
		index = 0;
		// Count
		int model_count = 0;
		int geometry_count = 0;
		int node_attribute_count = 0;
		int anim_curve_count = 0;
		int anim_curve_node_count = 0;
		int deformer_count = 0;
		int material_count = 0;
		int texture_count = 0;
		int video_count = 0;
		
		int startFrame = 0, endFrame = 0;
		int keyframeLength = 0;
		
		FbxTimeSpan timeSpan = new FbxTimeSpan();
		FbxTimeMode timeMode = null;
		int upAxis = 1;
		int upAxisSign = 1;
		int frontAxis = 1;
		int frontAxisSign = 1;
		
		// Global Setting
		while(!line.startsWith("; Object definitions"))
		{
			line = list[index++].trim();
			
			if(line.startsWith("P: \"TimeMode"))
			{
				timeMode = new FbxTimeMode( getLastCommaAsInt(line) );
			}
			else if(line.startsWith("P: \"TimeSpanStart"))
			{
				timeSpan.setStart( getLastCommaAsLong(line) );
			}
			else if(line.startsWith("P: \"TimeSpanStop"))
			{
				timeSpan.setStop( getLastCommaAsLong(line) );
			}
			else if(line.startsWith("P: \"UpAxis\""))
			{
				upAxis = getLastCommaAsInt(line);
			}
			else if(line.startsWith("P: \"UpAxisSign"))
			{
				upAxisSign = getLastCommaAsInt(line);
			}
			else if(line.startsWith("P: \"FrontAxis\""))
			{
				frontAxis = getLastCommaAsInt(line);
			}
			else if(line.startsWith("P: \"FrontAxisSign"))
			{
				frontAxisSign = getLastCommaAsInt(line);
			}
		}
		
		// Object definitions
		while(!line.startsWith("; Object properties"))
		{
			line = list[index++].trim();
			
			if(line.startsWith("ObjectType:"))
			{
				line = line.split("\"")[1];
				String attribute = list[index++];
				int count = Convert.toInt(attribute.split(": ")[1].trim());
				if(line.equals("Model"))
				{
					model_count = count;
				}
				else if(line.equals("Geometry"))
				{
					geometry_count = count;
				}
				else if(line.equals("NodeAttribute"))
				{
					node_attribute_count = count;
				}
				else if(line.equals("AnimationCurve"))
				{
					anim_curve_count = count;
				}
				else if(line.equals("AnimationCurveNode"))
				{
					anim_curve_node_count = count;
				}
				else if(line.equals("Deformer"))
				{
					deformer_count = count;
				}
				else if(line.equals("Material"))
				{
					material_count = count;
				}
				else if(line.equals("Texture"))
				{
					texture_count = count;
				}
				else if(line.equals("Video"))
				{
					video_count = count;
				}
			}
		}
		
		HashMap<Long, FbxObject> maps = new HashMap<Long, FbxObject>();

		// Object properties
		int current_geometry_count = 0;
		int current_node_attribute_count = 0;
		int current_node_count = 0;
		int current_deformer_count = 0;
		int current_anim_curve_node_count = 0;
		int current_anim_curve_count = 0;
		
		int current_material_count = 0;
		int current_texture_count = 0;
		int current_video_count = 0;
		
		while(!line.startsWith("; Object connections"))
		{
			line = list[index++].trim();
			
			// Load Geometries
			if(current_geometry_count < geometry_count)
			{
				if(line.startsWith("Geometry:"))
				{
					current_geometry_count += 1;

					Object[] geoItems = getID_NameAndType(line.substring(10));
					long geometry_id = (Long) geoItems[0];

					FbxMesh mesh = new FbxMesh(geometry_id);
					
					while(true)
					{
						line = list[index++].trim();
						
						if (line.startsWith("Vert"))
						{
							int count = Convert.toInt(line.substring(11).split(" ")[0]);
							mesh.setVertices(readFloatAttribute(list, count));
						} 
						else if (line.startsWith("Poly"))
						{
							int count = Convert.toInt(line.substring(21).split(" ")[0]);
							mesh.setIndices(readIndicesAttribute(list, count));
						} 
						else if (line.startsWith("Normals"))
						{
							int count = Convert.toInt(line.substring(10).split(" ")[0]);

							mesh.setNormals(readFloatAttribute(list, count));
						} 
						else if (line.startsWith("UV:"))
						{
							int count = Convert.toInt(line.substring(5).split(" ")[0]);
							mesh.setUV(readFloatAttribute(list, count));
						} 
						else if (line.startsWith("UVIndex"))
						{
							int count = Convert.toInt(line.substring(10).split(" ")[0]);
							mesh.setUVIndices(readUVIndicesAttribute(list, count));
							break;
						}
					}
					
					maps.put(geometry_id, mesh);
				}
			}
			
			// Load Nodes Attributes
			if(current_node_attribute_count < node_attribute_count)
			{
				if(line.startsWith("NodeAttribute: "))
				{
					current_node_attribute_count += 1;
					
					Object[] items = getID_NameAndType(line.substring(15));
					long node_attribute_id = (Long) items[0];
					
					int attributeType;
					FbxNodeAttribute nodeAttribute = null;

					if(items[2].equals("Null"))
					{
						attributeType = FbxNodeAttribute.Null;
						nodeAttribute = new FbxNodeAttribute(node_attribute_id, attributeType);
					}
					else if(items[2].equals("Mesh"))
					{
						attributeType = FbxNodeAttribute.Mesh;
						nodeAttribute = new FbxNodeAttribute(node_attribute_id, attributeType);
					}
					else
					{
						int skeletonType = 0;
						
						if(line.contains("Limb")) // Limb  or LimbNode
						{
							skeletonType = FbxSkeleton.LimbNode;
						}
						else if(line.contains("Root") || line.contains("Effector"))
						{
							skeletonType = FbxSkeleton.Root;
						}

						nodeAttribute = new FbxSkeleton(node_attribute_id, skeletonType);
					}

					maps.put(node_attribute_id, nodeAttribute);
				}
			}
			// Load Nodes
			if(current_node_count < model_count)
			{
				if(line.startsWith("Model: "))
				{
					current_node_count += 1;
					
					StringBuilder[] item = new StringBuilder[2];
					item[0] = new StringBuilder();
					item[1] = new StringBuilder();
					int inner_index = 0;
					boolean begin = false;
					char temp = 0;

					for (int i = 0; i < line.length(); i++)
					{
						char c = line.charAt(i);

						if (temp == ':')
						{
							if (c == ' ')
							{
								begin = true;
								inner_index = 0;
								continue;
							} 
							else if (c == ':')
							{
								begin = true;
								inner_index = 1;
								continue;
							}
						}

						if (c == '"' || c == ',')
						{
							begin = false;

							if (inner_index == 1)
							{
								break;
							}
						}

						if (begin)
						{
							item[inner_index].append(c);
						}

						temp = c;
					}
					
					long nodeID = Convert.toLong(item[0].toString().trim());
					String nodeName = item[1].toString();
					
					FbxNode node = new FbxNode(nodeID);
					node.setName(nodeName);

					Vector3 lclT = new Vector3();
					Vector3 lclR = new Vector3();
					Vector3 lclS = new Vector3(1,1,1);
					
					while(!(line = list[index++]).contains("}"))
					{
						if (line.contains("Lcl Translation"))
						{
							lclT = new Vector3(splitCommaToFloatArray(line, 3));
						} else if (line.contains("Lcl Rotation"))
						{
							lclR = new Vector3(splitCommaToFloatArray(line, 3));
						} else if (line.contains("Lcl Scaling"))
						{
							lclS = new Vector3(splitCommaToFloatArray(line, 3));
						}
					}

					node.setLclTranslation(lclT);
					node.setLclRotation(lclR);
					node.setLclScaling(lclS);

					maps.put(nodeID, node);
				}
			}
			// Load Deformers
			if(current_deformer_count < deformer_count)
			{
				if (line.startsWith("Deformer: "))
				{
					current_deformer_count += 1;
					
					Object[] items = getID_NameAndType(line.substring(10));
					long deformer_id = (Long) items[0];

					if (items[2].equals("Skin"))
					{
						FbxSkin skin = new FbxSkin(deformer_id);
						maps.put(deformer_id, skin);
					}
					else
					{
						FbxCluster cluster = new FbxCluster(deformer_id);
						
						while(true)
						{
							line = list[index++].trim();
							if (line.startsWith("Indexes"))
							{
								int count = Convert.toInt(line.substring(10).split(" ")[0]);
								cluster.setIndices(readIntAttribute(list, count));
							} 
							else if (line.startsWith("Weights"))
							{
								int count = Convert.toInt(line.substring(10).split(" ")[0]);
								cluster.setWeights(readFloatAttribute(list, count));
							} 
							else if (line.startsWith("Transform:"))
							{
								int count = Convert.toInt(line.substring(12).split(" ")[0]);
								cluster.setTransform(readFloatAttribute(list, count));
							}
							else if (line.startsWith("TransformLink"))
							{
								int count = Convert.toInt(line.substring(16).split(" ")[0]);
								cluster.setTransformLink(readFloatAttribute(list, count));
								break;
							}
						}

						maps.put(deformer_id, cluster);
					}
				}
			}
			
			// Load Materials
			if(current_material_count < material_count)
			{
				if (line.startsWith("Material:"))
				{
					current_material_count += 1;
					
					Object[] items = getID_NameAndType(line.substring(10));

					long material_id = (Long) items[0];
					String material_name = ((String)items[1]).substring(10);
					
					FbxSurfacePhong material = new FbxSurfacePhong(material_id);
					material.setName(material_name);
					
					// Read Properties
					while(!line.contains("}"))
					{
						line = list[index++].trim();

						if(line.startsWith("P: \"Emissive\""))
						{
							material.getEmissive().set(new Vector3(splitCommaToFloatArray(line, 3)));
						}
						else if(line.startsWith("P: \"Ambient\""))
						{
							material.getAmbient().set(new Vector3(splitCommaToFloatArray(line, 3)));
						}
						else if(line.startsWith("P: \"Diffuse\""))
						{
							material.getDiffuse().set(new Vector3(splitCommaToFloatArray(line, 3)));
						}
						else if(line.startsWith("P: \"NormalMap\""))
						{
							material.getNormalMap().set(new Vector3(splitCommaToFloatArray(line, 3)));
						}
						else if(line.startsWith("P: \"Bump\""))
						{
							material.getBump().set(new Vector3(splitCommaToFloatArray(line, 3)));
						}
						else if(line.startsWith("P: \"TransparentColor\""))
						{
							material.getTransparentColor().set(new Vector3(splitCommaToFloatArray(line, 3)));
						}
						else if(line.startsWith("P: \"DisplacementColor\""))
						{
							material.getDisplacementColor().set(new Vector3(splitCommaToFloatArray(line, 3)));
						}
						else if(line.startsWith("P: \"VectorDisplacementColor\""))
						{
							material.getVectorDisplacementColor().set(new Vector3(splitCommaToFloatArray(line, 3)));
						}
						else if(line.startsWith("P: \"Specular\""))
						{
							material.getSpecular().set(new Vector3(splitCommaToFloatArray(line, 3)));
						}
						else if(line.startsWith("P: \"Reflection\""))
						{
							material.getReflection().set(new Vector3(splitCommaToFloatArray(line, 3)));
						}
						else if(line.startsWith("P: \"Shinniness\""))
						{
							material.setShinniness( splitCommaToFloat(line) );
						}
						else if(line.startsWith("P: \"EmissiveFactor"))
						{
							material.setEmissiveFactor( splitCommaToFloat(line) );
						}
						else if(line.startsWith("P: \"AmbientFactor"))
						{
							material.setAmbientFactor( splitCommaToFloat(line) );
						}
						else if(line.startsWith("P: \"DiffuseFactor"))
						{
							material.setDiffuseFactor( splitCommaToFloat(line) );
						}
						else if(line.startsWith("P: \"BumpFactor"))
						{
							material.setBumpFactor( splitCommaToFloat(line) );
						}
						else if(line.startsWith("P: \"TransparencyFactor"))
						{
							material.setTransparencyFactor( splitCommaToFloat(line) );
						}
						else if(line.startsWith("P: \"DisplacementFactor"))
						{
							material.setDisplacementFactor( splitCommaToFloat(line) );
						}
						else if(line.startsWith("P: \"VectorDisplacementFactor"))
						{
							material.setVectorDisplacementFactor( splitCommaToFloat(line) );
						}
						else if(line.startsWith("P: \"SpecularFactor"))
						{
							material.setSpecularFactor( splitCommaToFloat(line) );
						}
						else if(line.startsWith("P: \"ReflectionFactor"))
						{
							material.setReflectionFactor( splitCommaToFloat(line) );
						}
						
//						P: "EmissiveFactor", "double", "Number", "",0
//						P: "AmbientColor", "ColorRGB", "Color", "",0.317647069692612,0.317647069692612,0.317647069692612
//						P: "DiffuseColor", "ColorRGB", "Color", "",0.317647069692612,0.317647069692612,0.317647069692612
//						P: "TransparentColor", "ColorRGB", "Color", "",1,1,1
//						P: "SpecularColor", "ColorRGB", "Color", "",0.899999976158142,0.899999976158142,0.899999976158142
//						P: "SpecularFactor", "double", "Number", "",0
//						P: "ShininessExponent", "double", "Number", "",1.99999991737042
//						P: "Emissive", "Vector3D", "Vector", "",0,0,0
//						P: "Ambient", "Vector3D", "Vector", "",0.317647069692612,0.317647069692612,0.317647069692612
//						P: "Diffuse", "Vector3D", "Vector", "",0.317647069692612,0.317647069692612,0.317647069692612
//						P: "Specular", "Vector3D", "Vector", "",0,0,0
//						P: "Shininess", "double", "Number", "",1.99999991737042
//						P: "Opacity", "double", "Number", "",1
//						P: "Reflectivity", "double", "Number", "",0
					}

					maps.put(material_id, material);
				}
			}
			
			// Load Textures
			if(current_texture_count < texture_count)
			{
				if (line.startsWith("Texture:"))
				{
					current_texture_count += 1;
					String[] t = line.substring(9).split(",");
					long texture_id = Convert.toLong(t[0]);
					String texture_name = t[1].split("\"")[1].substring(9);
					
					FbxFileTexture texture = new FbxFileTexture(texture_id);
					texture.setName(texture_name);
					
					while(!line.contains("}"))
					{
						line = list[index++].trim();
					}
					line = list[index++].trim();
					// Read Properties
					while(!line.contains("}"))
					{
						line = list[index++].trim();
						String[] tmp = line.split("\"");

						if(line.startsWith("FileName:"))
						{
							String tt = tmp[tmp.length-1];
							String fileName = tt;
							
							int indexOfSlash = tt.lastIndexOf("\\");
							
							if(indexOfSlash > -1)
							{
								fileName = tt.substring(indexOfSlash+1);
							}
							
							texture.setFileName(fileName);
						}
						else if(line.startsWith("RelativeFilename:"))
						{
							String tt = tmp[tmp.length-1];
							texture.setRelativeFileName(tt);
						}
						else if(line.startsWith("Media:"))
						{
							
						}
					}
					
					maps.put(texture_id, texture);
				}
			}
			
			// Load Video
			if(current_video_count < video_count)
			{
				if (line.startsWith("Video:"))
				{
					current_video_count += 1;
//					String[] t = line.substring(7).split(",");
//					long video_id = Convert.toLong(t[0]);
//					String video_name = t[1].split("\"")[1].substring(7);
				}
			}
			
			// Load Animation Curve Nodes
			if(current_anim_curve_node_count < anim_curve_node_count)
			{
				if (line.startsWith("AnimationCurveNode:"))
				{
					current_anim_curve_node_count += 1;
					long anim_curve_node_id = Convert.toLong(line.substring(20).split(",")[0]);
					FbxAnimCurveNode animCurveNode = new FbxAnimCurveNode(anim_curve_node_id);
					
					maps.put(anim_curve_node_id, animCurveNode);
				}
			}
			// Load Animation Curves
			if(current_anim_curve_count < anim_curve_count)
			{
				if	(line.startsWith("AnimationCurve:"))
				{
					current_anim_curve_count += 1;
					long anim_curve_id = Convert.toLong(line.substring(16).split(",")[0]);
					FbxAnimCurve animCurve = new FbxAnimCurve(anim_curve_id);
					
					long[] times = null;
					float[] values = null;
					
					while(true)
					{
						line = list[index++].trim();
						if (line.startsWith("KeyTime:"))
						{
							int count = Convert.toInt(line.substring(10).split(" ")[0]);
							times = readLongAttribute(list, count);
							if(times.length > 0)
							{
								int length = (int) ( (times[times.length - 1] - times[0])  / 1539538600L) + 1;
								if(length > keyframeLength)
								{
									keyframeLength = length;
									startFrame = (int) (times[0] / 1539538600L);
									endFrame = (int) (times[times.length - 1] / 1539538600L);
								}
							}
						}
						else if (line.startsWith("KeyValueFloat:"))
						{
							int count = Convert.toInt(line.substring(16).split(" ")[0]);
							values = readFloatAttribute(list, count);
							break;
						}
					}
					
					animCurve.set(times, values);
					maps.put(anim_curve_id, animCurve);
				}
			}
			
		}
		
		ArrayList<FbxNode> rootnodes = new ArrayList<FbxNode>();
		
		// Object Connections
		while(!line.startsWith(";Takes section") && index < list.length)
		{
			line = list[index++].trim();

			if(line.endsWith("RootNode"))
			{
				line = list[index++].trim();
				String attribute = line.substring(8);
				
				long root_node_id = Convert.toLong(attribute.substring(0, attribute.length()-2));
				
				FbxNode node = (FbxNode) maps.get(root_node_id);

				rootnodes.add(node);
			}
			else if (line.endsWith(", SubDeformer::"))
			{
				line = list[index++].trim();
				String[] attribute = line.substring(8).split(",");
				
				long node_id = Convert.toLong(attribute[0]);
				long sub_deformer_id = Convert.toLong(attribute[1]);

				FbxNode associateModel = (FbxNode) maps.get(node_id);
				FbxCluster cluster = (FbxCluster) maps.get(sub_deformer_id);

				cluster.setAssociateModel(associateModel);
			}
			else if (line.startsWith(";Model"))
			{
				if(line.split(",")[1].startsWith(" Model:"))
				{
					line = list[index++].trim();
					String[] attribute = line.substring(8).split(",");
					
					long child_node_id = Convert.toLong(attribute[0]);
					long parent_node_id = Convert.toLong(attribute[1]);

					FbxNode child_node = (FbxNode) maps.get(child_node_id);
					FbxNode parent_node = (FbxNode) maps.get(parent_node_id);

					parent_node.addChild(child_node);
					child_node.setParent(parent_node);
				}
			}
			else if (line.startsWith(";Geometry"))
			{
				line = list[index++].trim();
				String[] attribute = line.substring(8).split(",");
				
				long geometry_id = Convert.toLong(attribute[0]);
				long node_id = Convert.toLong(attribute[1]);

				FbxNode node = (FbxNode) maps.get(node_id);
				FbxGeometry geometry = (FbxGeometry) maps.get(geometry_id);
				node.setNodeAttribute(geometry);
			}
			else if (line.startsWith(";AnimCurveNode:"))
			{
				String[] tmp0 = line.split(",");
				if(tmp0[1].startsWith(" Model:"))
				{
					line = list[index++].trim();
					
					String[] attribute = line.substring(8).split(",");
					
					long anim_curve_node_id = Convert.toLong(attribute[0]);
					long node_id = Convert.toLong(attribute[1]);

					FbxNode node = (FbxNode) maps.get(node_id);
					FbxAnimCurveNode animCurveNode = (FbxAnimCurveNode) maps.get(anim_curve_node_id);

					char type = tmp0[0].charAt(tmp0[0].length()-1);
					if(type == 'T')
					{
						node.setLclTranslation(animCurveNode);
					}
					else if(type == 'R')
					{
						node.setLclRotation(animCurveNode);
					}
					else
					{
						node.setLclScaling(animCurveNode);
					}
				}
			}
			else if (line.startsWith(";NodeAtt"))
			{
				line = list[index++].trim();
				String[] attribute = line.substring(8).split(",");
				
				long node_attribute_id = Convert.toLong(attribute[0]);
				long node_id = Convert.toLong(attribute[1]);

				FbxNode node = (FbxNode) maps.get(node_id);
				FbxNodeAttribute nodeAttribute = (FbxNodeAttribute) maps.get(node_attribute_id);
				
				node.setNodeAttribute(nodeAttribute);
			}
			else if (line.startsWith(";Deform"))
			{
				line = list[index++].trim();
				String[] attribute = line.substring(8).split(",");
				
				long deformer_id = Convert.toLong(attribute[0]);
				long geometry_id = Convert.toLong(attribute[1]);

				FbxDeformer deformer = (FbxDeformer) maps.get(deformer_id);
				FbxGeometry geometry = (FbxGeometry) maps.get(geometry_id);

				geometry.addDeformer(deformer);
			}
			else if (line.startsWith(";AnimCurve:"))
			{
				line = list[index++].trim();
				String[] first_line = line.substring(8).split(",");
				index += 2;
				line = list[index++].trim();
				String[] second_line = line.substring(8).split(",");
				index += 2;
				line = list[index++].trim();
				String[] third_line = line.substring(8).split(",");

				long anim_curve_node_id = Convert.toLong(first_line[1]);
				long x_id = Convert.toLong(first_line[0]);
				long y_id = Convert.toLong(second_line[0]);
				long z_id = Convert.toLong(third_line[0]);

				FbxAnimCurveNode animCurveNode = (FbxAnimCurveNode) maps.get(anim_curve_node_id);
				FbxAnimCurve animCurveX = (FbxAnimCurve) maps.get(x_id);
				FbxAnimCurve animCurveY = (FbxAnimCurve) maps.get(y_id);
				FbxAnimCurve animCurveZ = (FbxAnimCurve) maps.get(z_id);

				animCurveNode.set(animCurveX, animCurveY, animCurveZ);
			}
			else if (line.startsWith(";SubDef"))
			{
				line = list[index++].trim();
				String[] attribute = line.substring(8).split(",");
				
				long sub_deformer_id = Convert.toLong(attribute[0]);
				long deformer_id = Convert.toLong(attribute[1]);

				FbxCluster cluster = (FbxCluster) maps.get(sub_deformer_id);
				FbxSkin deformer = (FbxSkin) maps.get(deformer_id);

				deformer.addCluster(cluster);
			}
			else if(line.startsWith(";Material"))
			{
				line = list[index++].trim();
				String[] attribute = line.substring(8).split(",");
				
				long material_id = Convert.toLong(attribute[0]);
				long node_id = Convert.toLong(attribute[1]);

				FbxSurfaceMaterial material = (FbxSurfaceMaterial) maps.get(material_id);
				FbxNode node = (FbxNode) maps.get(node_id);
				
				node.setMaterial(material);
			}
			else if(line.startsWith(";Texture"))
			{
				line = list[index++].trim();
				String[] attribute = line.substring(8).split(",");
				
				long texture_id = Convert.toLong(attribute[0]);
				long material_id = Convert.toLong(attribute[1]);
				
				String target = attribute[2].trim().split("\"")[1];
				
				FbxTexture texture = (FbxTexture) maps.get(texture_id);
				FbxSurfacePhong material = (FbxSurfacePhong) maps.get(material_id);
				
				if(target.equalsIgnoreCase("DiffuseColor"))
				{
					material.setDiffuseTexture(texture);
				}
				else if(target.equalsIgnoreCase("TransparentColor"))
				{

				}
			}
		}

		FbxScene scene = new FbxScene();

		for (FbxObject fbxObject : maps.values())
		{
			if(fbxObject instanceof FbxGeometry)
			{
				scene.addGeometry((FbxGeometry) fbxObject);
			}
			else if(fbxObject instanceof FbxNode)
			{
				scene.addNode((FbxNode) fbxObject);
			}
			else if(fbxObject instanceof FbxSurfaceMaterial)
			{
				scene.addMaterial((FbxSurfaceMaterial) fbxObject);
			}
		}
		
		FbxNode sceneRootNode = scene.getRootnodes();

		for (FbxNode rootnode : rootnodes)
		{
			sceneRootNode.addChild(rootnode);
		}

		FbxGlobalSetting globalSetting = scene.globalSetting();
		globalSetting.setTimeMode(timeMode);
		globalSetting.setTimeSpan(timeSpan);
		globalSetting.setFrontAxis(frontAxis);
		globalSetting.setFrontAxisSign(frontAxisSign);
		globalSetting.setUpAxis(upAxis);
		globalSetting.setUpAxisSign(upAxisSign);
		
		scene.setObjectDefinitions(model_count, geometry_count, node_attribute_count, anim_curve_count, anim_curve_node_count, deformer_count);
		scene.setKeyframe(startFrame, endFrame, keyframeLength);
		
		
		list = null;

		return scene;
	}

	private static float[] readFloatAttribute(String[] list, int count)
	{
		while(true)
		{
			String line = list[index++].trim();
			if(line.startsWith("}"))
			{
				break;
			}
			sb.append(line);
		}

		float[] items = new float[count];

		double result = 0;
		double p = 1;
		byte sign = 1;
		
		int index = count-1;

		for (int i = sb.length()-1; i >= 0; i--)
		{
			char c = sb.charAt(i);

			if(c >= '0' && c <= '9')
			{
				result += (c - 48) * p;
				p *= 10;
			}
			else if(c == '.')
			{
				result /= p;
				p = 1;
			}
			else if(c == 'e' || c == 'E')
			{
				i = convertToFloatWithE(items, index--, (i-1), result, sign);
				result = 0;
				p = 1;
				sign = 1;
				
				if(index < 0)
				{
					break;
				}
			}
			else if(c == '-')
			{
				sign = -1;
			}
			else
			{
				items[index--] = (float) (result * sign);
				result = 0;
				p = 1;
				sign = 1;

				if(index < 0)
				{
					break;
				}
			}
		}
		
		sb.delete(0, sb.length());

		return items;
	}
	
	private static int convertToFloatWithE(float[] items, int index, int start, double eValue, byte eSign)
	{
		long result = 0;
		long p = 1;
		double lastPow = 1;
		byte sign = 1;
		
		if(eSign == 1)
		{
			lastPow = getMaxPow10(eValue);
		}
		else
		{
			lastPow = getMinPow10(-eValue);
		}

		for (int i = start; i >= 0; i--)
		{
			char c = sb.charAt(i);
			
			if(c >= '0' && c <= '9')
			{
				result += (c - 48) * p;
				p *= 10;
			}
			else if(c == '.')
			{
				result /= p;
				p = 1;
			}
			else if(c == '-')
			{
				sign = -1;
			}
			else
			{
				items[index] = (float) (result * lastPow * sign);
				return i;
			}
		}
		
		return 0;
	}
	
	private static int[] readIndicesAttribute(String[] list, int count)
	{
		while(true)
		{
			String line = list[index++].trim();
			if(line.startsWith("}"))
			{
				break;
			}

			sb.append(line);
		}

		int[] items = new int[count];

		int result = 0;
		int p = 1;
		byte sign = 1;
		
		int index = count-1;
		
		for (int i = sb.length()-1; i >= 0; i--)
		{
			char c = sb.charAt(i);
			
			if(c >= '0' && c <= '9')
			{
				result += (c - 48) * p;
				p *= 10;
			}
			else if(c == '-')
			{
				sign = -1;
//				result = ((result * -1) - 1);
			}
			else
			{
				items[index--] = result * sign;
				result = 0;
				p = 1;
				sign = 1;
				
				if(index < 0)
				{
					break;
				}
			}
		}
		
		sb.delete(0, sb.length());

		return items;
	}
	
	private static int[] readUVIndicesAttribute(String[] list, int count)
	{
		while(true)
		{
			String line = list[index++].trim();
			if(line.startsWith("}"))
			{
				break;
			}

			sb.append(line);
		}

		int[] items = new int[count];

		int result = 0;
		int p = 1;
		byte sign = 1;
		
		int index = count-1;

		for (int i = sb.length()-1; i >= 0; i--)
		{
			char c = sb.charAt(i);
			
			if(c >= '0' && c <= '9')
			{
				result += (c - 48) * p;
				p *= 10;
			}
			else if(c == '-')
			{
				sign = -1;
			}
			else
			{
				items[index--] = result * sign;
				result = 0;
				p = 1;
				sign = 1;
				
				if(index < 0)
				{
					break;
				}
			}
		}
		
		sb.delete(0, sb.length());

		return items;
	}
	
	private static int[] readIntAttribute(String[] list, int count)
	{
		while(true)
		{
			String line = list[index++].trim();
			if(line.startsWith("}"))
			{
				break;
			}

			sb.append(line);
		}

		int[] items = new int[count];

		int result = 0;
		int p = 1;
		byte sign = 1;
		
		int index = count-1;

		for (int i = sb.length()-1; i >= 0; i--)
		{
			char c = sb.charAt(i);
			
			if(c >= '0' && c <= '9')
			{
				result += (c - 48) * p;
				p *= 10;
			}
			else if(c == '-')
			{
				sign = -1;
			}
			else
			{
				items[index--] = result * sign;
				result = 0;
				p = 1;
				sign = 1;
				
				if(index < 0)
				{
					break;
				}
			}
		}
		
		sb.delete(0, sb.length());

		return items;
	}
	
	private static long[] readLongAttribute(String[] list, int count)
	{
		while(true)
		{
			String line = list[index++].trim();
			if(line.startsWith("}"))
			{
				break;
			}

			sb.append(line);
		}

		long[] items = new long[count];

		long result = 0;
		long p = 1;
		byte sign = 1;
		
		int index = count-1;

		for (int i = sb.length()-1; i >= 0; i--)
		{
			char c = sb.charAt(i);
			
			if(c >= '0' && c <= '9')
			{
				result += (c - 48) * p;
				p *= 10;
			}
			else if(c == '-')
			{
				sign = -1;
			}
			else
			{
				items[index--] = result * sign;
				result = 0;
				p = 1;
				sign = 1;
				
				if(index < 0)
				{
					break;
				}
			}
		}
		
		sb.delete(0, sb.length());

		return items;
	}

	private static long getLastCommaAsLong(String line)
	{
		long result = 0;
		long p = 1;
		byte sign = 1;
		
		for (int i = line.length()-1; i >= 0; i--)
		{
			char c = line.charAt(i);
			
			if(c >= '0' && c <= '9')
			{
				result += (c - 48) * p;
				p *= 10;
			}
			else if(c == ',' || i == 0)
			{
				return result * sign;
			}
			else if(c == '-')
			{
				sign = -1;
			}
		}
		
		return 0;
	}
	
	private static int getLastCommaAsInt(String line)
	{
		int result = 0;
		int p = 1;
		int sign = 1;
		
		for (int i = line.length()-1; i >= 0; i--)
		{
			char c = line.charAt(i);
			
			if(c >= '0' && c <= '9')
			{
				result += (c - 48) * p;
				p *= 10;
			}
			else if(c == ',' || i == 0)
			{
				return result * sign;
			}
			else if(c == '-')
			{
				sign = -1;
			}
		}
		
		return 0;
	}

	private static float[] splitCommaToFloatArray(String line, int count)
	{
		float[] items = new float[count];

		float result = 0;
		float p = 1;
		float lastPow = 1;
		int sign = 1;
		
		int index = count-1;

		for (int i = line.length()-1; i >= 0; i--)
		{
			char c = line.charAt(i);
			
			if(c == '-')
			{
				sign = -1;
			}
			else if(c == ',')
			{
				items[index--] = result * lastPow * sign;
				result = 0;
				p = 1;
				lastPow = 1;
				sign = 1;
				
				if(index < 0)
				{
					break;
				}
			}
			else if(c == '.')
			{
				result /= p;
				p = 1;
			}
			else if(c == 'e' || c == 'E')
			{
				if(sign == 1)
				{
					lastPow = (float) getMaxPow10(result);
				}
				else
				{
					lastPow = (float) getMinPow10(-result);
					sign = 1;
				}
				
				result = 0;
				p = 1;
			}
			else if(c >= '0' && c <= '9')
			{
				result += (c - 48) * p;
				p *= 10;
			}
		}

		return items;
	}
	
	private static float splitCommaToFloat(String line)
	{
		float result = 0;
		float p = 1;
		float lastPow = 1;
		int sign = 1;

		for (int i = line.length()-1; i >= 0; i--)
		{
			char c = line.charAt(i);
			
			if(c == '-')
			{
				sign = -1;
			}
			else if(c == ',')
			{
				return result * lastPow * sign;
			}
			else if(c == '.')
			{
				result /= p;
				p = 1;
			}
			else if(c == 'e' || c == 'E')
			{
				if(sign == 1)
				{
					lastPow = (float) getMaxPow10(result);
				}
				else
				{
					lastPow = (float) getMinPow10(-result);
					sign = 1;
				}
				
				result = 0;
				p = 1;
			}
			else if(c >= '0' && c <= '9')
			{
				result += (c - 48) * p;
				p *= 10;
			}
		}

		return 0;
	}
	
	private static Object[] getID_NameAndType(String line)
	{
		Object[] items = new Object[3];
		
		int indexOfComma = -1;
		int[] indexOfQuote = new int[4];
		int index = 0;
		
		for (int i = 0; i < line.length(); i++)
		{
			char c = line.charAt(i);
			
			if(c == ',' && indexOfComma == -1)
			{
				indexOfComma = i;
			}
			else if(c == '\"')
			{
				indexOfQuote[index++] = i;
				
				if(index > 3)
				{
					break;
				}
			}
		}

		items[0] = Convert.toLong(line.substring(0, indexOfComma));
		items[1] = line.substring(indexOfQuote[0]+1, indexOfQuote[1]);
		items[2] = line.substring(indexOfQuote[2]+1, indexOfQuote[3]);
		
		return items;
	}

	private static double getMaxPow10(double positiveY)
	{
		int cast = (int) positiveY;
		double decimal = Math.exp((positiveY - cast) * 2.302585092994046);
		if(cast >= maxPowD.length)
		{
			cast = maxPowD.length-1;
		}

		return maxPowD[cast] * decimal;
	}
	
	private static double getMinPow10(double negativeY)
	{
		int cast = (int) -negativeY;
		double decimal = Math.exp((negativeY + cast) * 2.302585092994046);
		if(cast >= minPowD.length)
		{
			cast = minPowD.length-1;
		}
		
		return minPowD[cast] * decimal;
	}

	private static double[] maxPowD = new double[309];
	private static double[] minPowD = new double[325];
	
	static
	{

		double pd = 1;
		for (int i = 0; i < maxPowD.length; i++)
		{
			maxPowD[i] = pd;
			pd *= 10d;
		}
		
		pd = 1;
		for (int i = 0; i < minPowD.length; i++)
		{
			minPowD[i] = pd;
			pd /= 10d;
		}
	}
}

