package graphic.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.media.opengl.GL;

import math.Matrix;
import math.Vektor;
import math.MatrixStack;

public class RenderObject {
	private Mesh mesh;
	private List<Mesh> meshset = new ArrayList<Mesh>();
	private Matrix transformation;
	private Matrix oldtransform = new Matrix();
	private HashMap<Matrix, Matrix> transformationmap;
	private HashMap<Matrix, UniformsAndMatrix> transformationUniformsetMap;
	private boolean saveOldTransformation;
	private List<List<Texture>> texturesetarray = new ArrayList<List<Texture>>();
	private List<Texture> textures;
	private List<Uniform> uniforms = new ArrayList<Uniform>();
	private ShaderProgram program;
	private List<RenderObject> renderObjects = new ArrayList<RenderObject>();
	private Vektor minboundingbox = new Vektor();
	private Vektor maxboundingbox = new Vektor();
	private boolean frustrumcullable;
	private boolean frustrumculling;
	private boolean blending;
	private boolean temporarilydisableblending;
	private boolean zbuffertest;
	private boolean zbufferwrite;
	private boolean visible;

	private boolean cull(Matrix modelViewMatrix,Matrix projectionMatrix) {
		float deltax = this.maxboundingbox.elements[0] - this.minboundingbox.elements[0];
		float deltay = this.maxboundingbox.elements[1] - this.minboundingbox.elements[1];
		float deltaz = this.maxboundingbox.elements[2] - this.minboundingbox.elements[2];

		Vektor minpos = new Vektor();
		Vektor maxpos = new Vektor();
		boolean initminmax = true;

		for(int x=0 ; x<2 ; ++x) {
			for(int y=0 ; y<2 ; ++y) {
				for(int z=0 ; z<2 ; ++z) {
					Vektor pos = new Vektor();
					pos.copy(this.minboundingbox);
					pos.elements[0] += deltax * ((float)x);
					pos.elements[1] += deltay * ((float)y);
					pos.elements[2] += deltaz * ((float)z);
					pos.leftmult(modelViewMatrix);
					if(!initminmax) {
						for(int i=0 ; i<3 ; ++i) {
							minpos.elements[i] = (float)Math.min(minpos.elements[i],pos.elements[i]);
							maxpos.elements[i] = (float)Math.max(maxpos.elements[i],pos.elements[i]);
						}
					} else {
						for(int i=0 ; i<3 ; ++i) {
							minpos.elements[i] = maxpos.elements[i] = pos.elements[i];
						}
						initminmax = false;
					}
				}
			}
		}
		if(minpos.elements[2] > 0 ) {
			return true;
		}
		Vektor min_maxparallel = new Vektor();
		Vektor max_minparallel = new Vektor();
		min_maxparallel.copy(minpos);
		min_maxparallel.elements[2] = maxpos.elements[2];
		max_minparallel.copy(maxpos);
		max_minparallel.elements[2]= minpos.elements[2];
		minpos.leftmult(projectionMatrix);
		min_maxparallel.leftmult(projectionMatrix);
		maxpos.leftmult(projectionMatrix);
		max_minparallel.leftmult(projectionMatrix);
		if(minpos.elements[3] == 0.0 || maxpos.elements[3] == 0.0 || min_maxparallel.elements[3] == 0.0 ||max_minparallel.elements[3] == 0.0) {
			return false;
		}
		for(int i=0 ; i<2 ; ++i) {
			minpos.elements[i] /= minpos.elements[3];
			maxpos.elements[i] /= maxpos.elements[3];
			min_maxparallel.elements[i] /= min_maxparallel.elements[3];
			max_minparallel.elements[i] /= max_minparallel.elements[3];
		}
		boolean snwe_minpos[] = new boolean[4];
		boolean snwe_max_minparallel[] = new boolean[4];
		boolean snwe_min_maxparallel[] = new boolean[4];
		boolean snwe_maxpos[] = new boolean[4];

		boolean max_cullcandidate = false;

		snwe_min_maxparallel[0] = min_maxparallel.elements[0] < -1;
		snwe_min_maxparallel[1] = min_maxparallel.elements[0] > 1;
		snwe_min_maxparallel[2] = min_maxparallel.elements[1] < -1;
		snwe_min_maxparallel[3] = min_maxparallel.elements[1] > 1;
		snwe_maxpos[0] = maxpos.elements[0] < -1;
		snwe_maxpos[1] = maxpos.elements[0] > 1;
		snwe_maxpos[2] = maxpos.elements[1] < -1;
		snwe_maxpos[3] = maxpos.elements[1] > 1;
		for(int i=0 ; i<4 ; ++i) {
			max_cullcandidate = max_cullcandidate || (snwe_min_maxparallel[i] && snwe_maxpos[i]);
		}

		boolean min_cullcandidate = false;

		snwe_minpos[0] = minpos.elements[0] < -1;
		snwe_minpos[1] = minpos.elements[0] > 1;
		snwe_minpos[2] = minpos.elements[1] < -1;
		snwe_minpos[3] = minpos.elements[1] > 1;
		snwe_max_minparallel[0] = max_minparallel.elements[0] < -1;
		snwe_max_minparallel[1] = max_minparallel.elements[0] > 1;
		snwe_max_minparallel[2] = max_minparallel.elements[1] < -1;
		snwe_max_minparallel[3] = max_minparallel.elements[1] > 1;
		for(int i=0 ; i<4 ; ++i) {
			min_cullcandidate = min_cullcandidate || (snwe_minpos[i] && snwe_max_minparallel[i]);
		}

		return max_cullcandidate && min_cullcandidate;
	}
	
	public RenderObject() {
		this.saveOldTransformation = false;
		this.mesh = null;
		this.transformation = null;
		this.program = null;
		this.blending = false;
		this.temporarilydisableblending = false;
		this.zbuffertest = true;
		this.zbufferwrite = true;
		this.frustrumcullable = false;
		this.frustrumculling = true;
		this.visible = true;
		this.textures = new ArrayList<Texture>();
		this.texturesetarray.add(this.textures);
		this.transformationmap = new HashMap<Matrix,Matrix>();
		this.transformationUniformsetMap = new HashMap<Matrix,UniformsAndMatrix>();
	}
	
	public void setMesh(int meshnumber) {
		if(meshnumber >= 0 && meshnumber < this.meshset.size()) {
			this.mesh = this.meshset.get(meshnumber);
		}
	}
	
	public void setMesh(Mesh mesh) {
		this.mesh = mesh;
		this.meshset.add(mesh);
	}
	
	public Mesh getMesh() {
		return this.mesh;
	}
	
	public void setTransformation(Matrix transformation) {
		this.transformation = transformation;
	}
	
	public Matrix getTransformation() {
		return this.transformation;
	}
	
	public HashMap<Matrix,Matrix> getTransformationMap() {
		return this.transformationmap;
	}
	
	public HashMap<Matrix,UniformsAndMatrix> getTransformationUniformsetMap() {
		return this.transformationUniformsetMap;
	}
	
	public void addTexture(Texture texture) {
		textures.add(texture);
	}
	
	public void setCurrentTextureSet(int textureset) {
		if(textureset >= 0 && textureset < this.texturesetarray.size())
		{
			this.textures = this.texturesetarray.get(textureset);
		}
	}
	
	public void addTextureSet() {
		List<Texture> textureset = new ArrayList<Texture>();
		this.texturesetarray.add(textureset);
	}
	
	public int textureSetArraySize() {
		return this.texturesetarray.size();
	}
	
	public void addUniformFloat(String name,float value[]) {
		Uniform uniform = new UniformFloat(name,value);
		uniforms.add(uniform);
	}
	
	public void addUniformVektor(String name,Vektor value) {
		Uniform uniform = new UniformVektor(name,value);
		uniforms.add(uniform);
	}
	
	public void addUniformMatrix3x3(String name,Matrix value) {
		Uniform uniform = new UniformMatrix3x3(name,value);
		uniforms.add(uniform);
	}
	
	public void addUniformMatrix4x4(String name,Matrix value) {
		Uniform uniform = new UniformMatrix4x4(name,value);
		uniforms.add(uniform);
	}
	
	public void removeUniforms() {
		uniforms.clear();
	}
	
	public void setShaderProgram(ShaderProgram program) {
		this.program = program;
	}
	
	public ShaderProgram getShaderProgram() {
		return this.program;
	}
	
	public void addRenderObject(RenderObject renderObject) {
		this.renderObjects.add(renderObject);
	}
	
	public void setSaveOldTransform(boolean save) {
		this.saveOldTransformation = save;
	}
	
	public void setFrustrumCullable(Vektor minboundingbox,Vektor maxboundingbox) {
		this.minboundingbox.copy(minboundingbox);
		this.maxboundingbox.copy(maxboundingbox);
		this.frustrumcullable = true;
	}
	
	public void setFrustrumCulling(boolean frustrumculling) {
		this.frustrumculling = frustrumculling;
	}
	
	public void setBlending(boolean blending) {
		this.blending = blending;
	}
	
	public void temporarilyDisableBlending(boolean disable) {
		this.temporarilydisableblending = disable;
	}
	
	public void setZBufferTest(boolean zbuffertest) {
		this.zbuffertest = zbuffertest;
	}
	
	public void setZBufferWrite(boolean zbufferwrite) {
		this.zbufferwrite = zbufferwrite;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	public void setWireframe(boolean wireframe) {
		if(this.mesh != null) {
			if(wireframe) {
				this.mesh.setPolygonMode(Mesh.POLYGONMODE_LINE);
			} else {
				this.mesh.setPolygonMode(Mesh.POLYGONMODE_FILL);
			}
			for(RenderObject renderObject : renderObjects) {
				renderObject.setWireframe(wireframe);
			}
		}
	}
	
	public void setTextureFilter(int filter) {
		for(List<Texture> textureset : this.texturesetarray)
		{
			for(Texture tex : textureset)
			{
				tex.setFilter(filter);
			}
		}
	}
	
	public void setMipmappingFilter(int filter) {
		for(List<Texture> textureset : this.texturesetarray) {
			for(Texture tex : textureset) {
				tex.setMipmappingFilter(filter);
			}
		}
	}
	
	public void useDisplayList(boolean use) {
		if(this.mesh != null) {
			this.mesh.useDisplayList(use);
			for(RenderObject renderObject : renderObjects) {
				renderObject.useDisplayList(use);
			}
		}
	}
	
	public void render(MatrixStack modelStack,Matrix viewMatrix, Matrix viewNormalMatrix,Matrix projectionMatrix,List<Light> lights,List<Texture> globalTextures , List<Uniform> globalUniforms) {
		if(!visible) {
			return;
		}
		if(!zbuffertest) {
			GraphicHardwareManager.getInstance().getGL2().glDisable(GL.GL_DEPTH_TEST);
		}
		if(!zbufferwrite) {
			GraphicHardwareManager.getInstance().getGL2().glDepthMask(false);
		}
		if(blending && (!temporarilydisableblending)) {
			GraphicHardwareManager.getInstance().getGL2().glEnable(GL.GL_BLEND);
			GraphicHardwareManager.getInstance().getGL2().glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		}
		if(transformation != null) {
			modelStack.push();
			modelStack.getTopMatrix().rightmult(transformation);
		}
		if(mesh != null) {
			for(Texture texture : this.textures) {
				texture.use();
			}
			for(Texture texture : globalTextures) {
				texture.use();
			}
			if(program != null) {
				program.use();

				for(Uniform uniform : globalUniforms) {
					uniform.use(this.program);
				}

				for(Uniform uniform : uniforms) {
					uniform.use(this.program);
				}
			}
			if(this.transformationmap.size() == 0 && this.transformationUniformsetMap.size() == 0) {
				Matrix modelNormalMatrix = new Matrix();
				Matrix modelViewMatrix = new Matrix();
				Matrix modelViewNormalMatrix = new Matrix();
				Matrix viewProjectionMatrix = new Matrix();
				program.useUniformMatrix4x4("core_ModelMatrix",modelStack.getTopMatrix());
				if(this.saveOldTransformation) {
					program.useUniformMatrix4x4("old_ModelMatrix",this.oldtransform);
				}
				modelViewMatrix.copy(modelStack.getTopMatrix()).leftmult(viewMatrix);
				boolean donotcull = true;
				
				if(this.frustrumcullable && this.frustrumculling)
				{
					donotcull = !this.cull(modelViewMatrix,projectionMatrix);
				}

				if(donotcull) {
					Matrix modelViewProjectionMatrix = new Matrix();
					modelViewProjectionMatrix.copy(modelViewMatrix).leftmult(projectionMatrix);
					viewProjectionMatrix.copy(projectionMatrix).rightmult(viewMatrix);
					modelNormalMatrix.copy(modelStack.getTopMatrix()).setNormalMatrix();
					modelViewNormalMatrix.copy(modelNormalMatrix).leftmult(viewNormalMatrix);
					program.useUniformMatrix3x3("core_ModelNormalMatrix",modelNormalMatrix);
					program.useUniformMatrix4x4("core_ViewMatrix",viewMatrix);
					program.useUniformMatrix3x3("core_ViewNormalMatrix",viewNormalMatrix);
					program.useUniformMatrix4x4("core_ProjectionMatrix",projectionMatrix);
					program.useUniformMatrix4x4("core_ModelViewMatrix",modelViewMatrix);
					program.useUniformMatrix3x3("core_ModelViewNormalMatrix",modelViewNormalMatrix);
					program.useUniformMatrix4x4("core_ModelViewProjectionMatrix",modelViewProjectionMatrix);
					program.useUniformMatrix4x4("core_ViewProjectionMatrix",viewProjectionMatrix);

					for(Light light : lights) {
						if(light.getName() == null || light.getPosition() == null) {
							continue;
						}
						String name = light.getName();
						Vektor transformedPosition = new Vektor();
						transformedPosition.copy(light.getPosition());
						transformedPosition.leftmult(viewMatrix);
						program.useUniformVector(name,transformedPosition);
						if(light.getDirection() != null) {
							program.useUniformVector(name + "Direction",light.getDirection());
						}
						if(light.getAmbient() != null) {
							program.useUniformVector(name + "Ambient",light.getAmbient());
						}
						if(light.getDiffuse() != null) {
							program.useUniformVector(name + "Diffuse",light.getDiffuse());
						}
						if(light.getSpecular() != null) {
							program.useUniformVector(name + "Specular",light.getSpecular());
						}
					}

					mesh.render();

					if(this.saveOldTransformation) {
						this.oldtransform.copy(modelStack.getTopMatrix());
					}
				}
			} else {
				Matrix viewProjectionMatrix = new Matrix();
				viewProjectionMatrix.copy(projectionMatrix).rightmult(viewMatrix);
				program.useUniformMatrix4x4("core_ViewMatrix",viewMatrix);
				program.useUniformMatrix3x3("core_ViewNormalMatrix",viewNormalMatrix);
				program.useUniformMatrix4x4("core_ProjectionMatrix",projectionMatrix);
				program.useUniformMatrix4x4("core_ViewProjectionMatrix",viewProjectionMatrix);

				for(Matrix currentTransformation : transformationmap.keySet()) {

					Matrix modelViewMatrix = new Matrix();
					modelViewMatrix.copy(currentTransformation).leftmult(viewMatrix);

					boolean donotcull = true;

					if(this.frustrumcullable && this.frustrumculling) {
						donotcull = !this.cull(modelViewMatrix,projectionMatrix);
					}

					if(donotcull) {
						Matrix modelNormalMatrix = new Matrix();
						Matrix modelViewNormalMatrix = new Matrix();
						Matrix modelViewProjectionMatrix = new Matrix();
						modelViewProjectionMatrix.copy(modelViewMatrix).leftmult(projectionMatrix);
						modelNormalMatrix.copy(currentTransformation).setNormalMatrix();
						modelViewNormalMatrix.copy(modelNormalMatrix).leftmult(viewNormalMatrix);
						program.useUniformMatrix4x4("core_ModelMatrix",currentTransformation);
						if(this.saveOldTransformation) {
							program.useUniformMatrix4x4("old_ModelMatrix",transformationmap.get(currentTransformation));
						}
						program.useUniformMatrix3x3("core_ModelNormalMatrix",modelNormalMatrix);
						program.useUniformMatrix4x4("core_ModelViewMatrix",modelViewMatrix);
						program.useUniformMatrix3x3("core_ModelViewNormalMatrix",modelViewNormalMatrix);
						program.useUniformMatrix4x4("core_ModelViewProjectionMatrix",modelViewProjectionMatrix);

						for(Light light : lights) {
							if(light.getName() == null || light.getPosition() == null) {
								continue;
							}
							String name = light.getName();
							Vektor transformedPosition = new Vektor();
							transformedPosition.copy(light.getPosition());
							transformedPosition.leftmult(viewMatrix);
							program.useUniformVector(name,transformedPosition);
							if(light.getDirection() != null) {
								program.useUniformVector(name + "Direction",light.getDirection());
							}
							if(light.getAmbient() != null) {
								program.useUniformVector(name + "Ambient",light.getAmbient());
							}
							if(light.getDiffuse() != null) {
								program.useUniformVector(name + "Diffuse",light.getDiffuse());
							}
							if(light.getSpecular() != null) {
								program.useUniformVector(name + "Specular",light.getSpecular());
							}
						}

						mesh.render();

						if(this.saveOldTransformation) {
							transformationmap.get(currentTransformation).copy(currentTransformation);
						}
					}
				}
				for(Matrix currentTransformation : transformationUniformsetMap.keySet()) {
					UniformsAndMatrix currentUnformAndMatrix = transformationUniformsetMap.get(currentTransformation);
					if(program != null) {
						for(Uniform uniform : currentUnformAndMatrix.getUniforms()) {
							uniform.use(this.program);
						}
					}

					Matrix modelViewMatrix = new Matrix();
					modelViewMatrix.copy(currentTransformation).leftmult(viewMatrix);

					boolean donotcull = true;

					if(this.frustrumcullable && this.frustrumculling) {
						donotcull = !this.cull(modelViewMatrix,projectionMatrix);
					}

					if(donotcull) {
						Matrix modelNormalMatrix = new Matrix();
						Matrix modelViewNormalMatrix = new Matrix();
						Matrix modelViewProjectionMatrix = new Matrix();
						modelViewProjectionMatrix.copy(modelViewMatrix).leftmult(projectionMatrix);
						modelNormalMatrix.copy(currentTransformation).setNormalMatrix();
						modelViewNormalMatrix.copy(modelNormalMatrix).leftmult(viewNormalMatrix);
						program.useUniformMatrix4x4("core_ModelMatrix",currentTransformation);
						if(this.saveOldTransformation) {
							program.useUniformMatrix4x4("old_ModelMatrix",currentUnformAndMatrix.getMatrix());
						}
						program.useUniformMatrix3x3("core_ModelNormalMatrix",modelNormalMatrix);
						program.useUniformMatrix4x4("core_ModelViewMatrix",modelViewMatrix);
						program.useUniformMatrix3x3("core_ModelViewNormalMatrix",modelViewNormalMatrix);
						program.useUniformMatrix4x4("core_ModelViewProjectionMatrix",modelViewProjectionMatrix);

						for(Light light : lights) {
							if(light.getName() == null || light.getPosition() == null) {
								continue;
							}
							String name = light.getName();
							Vektor transformedPosition = new Vektor();
							transformedPosition.copy(light.getPosition());
							transformedPosition.leftmult(viewMatrix);
							program.useUniformVector(name,transformedPosition);
							if(light.getDirection() != null) {
								program.useUniformVector(name + "Direction",light.getDirection());
							}
							if(light.getAmbient() != null) {
								program.useUniformVector(name + "Ambient",light.getAmbient());
							}
							if(light.getDiffuse() != null) {
								program.useUniformVector(name + "Diffuse",light.getDiffuse());
							}
							if(light.getSpecular() != null) {
								program.useUniformVector(name + "Specular",light.getSpecular());
							}
						}

						mesh.render();

						if(this.saveOldTransformation) {
							currentUnformAndMatrix.getMatrix().copy(currentTransformation);
						}
					}
				}
			}
			if(!zbuffertest) {
				GraphicHardwareManager.getInstance().getGL2().glEnable(GL.GL_DEPTH_TEST);
			}
			if(!zbufferwrite) {
				GraphicHardwareManager.getInstance().getGL2().glDepthMask(true);
			}
			if(blending && (!temporarilydisableblending)) {
				GraphicHardwareManager.getInstance().getGL2().glDisable(GL.GL_BLEND);
			}
		}

		for(RenderObject renderObject : renderObjects) {
			renderObject.render(modelStack,viewMatrix,viewNormalMatrix,projectionMatrix,lights,globalTextures,globalUniforms);
		}

		if(transformation != null) {
			modelStack.pop();
		}
	}
}
