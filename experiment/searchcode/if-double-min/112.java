<<<<<<< HEAD
package com.google.code.gwt.threejs.client.renderers;

import java.util.ArrayList;
import java.util.List;

import com.google.code.gwt.threejs.client.cameras.Camera;
import com.google.code.gwt.threejs.client.core.Color;
import com.google.code.gwt.threejs.client.core.Matrix4;
import com.google.code.gwt.threejs.client.core.Rectangle;
import com.google.code.gwt.threejs.client.core.UV;
import com.google.code.gwt.threejs.client.core.Vector2;
import com.google.code.gwt.threejs.client.core.Vector3;
import com.google.code.gwt.threejs.client.core.Vector4;
import com.google.code.gwt.threejs.client.lights.AmbientLight;
import com.google.code.gwt.threejs.client.lights.DirectionalLight;
import com.google.code.gwt.threejs.client.lights.Light;
import com.google.code.gwt.threejs.client.lights.PointLight;
import com.google.code.gwt.threejs.client.materials.HasMaterialEnvMap;
import com.google.code.gwt.threejs.client.materials.HasMaterialMap;
import com.google.code.gwt.threejs.client.materials.LineBasicMaterial;
import com.google.code.gwt.threejs.client.materials.Material;
import com.google.code.gwt.threejs.client.materials.MeshBasicMaterial;
import com.google.code.gwt.threejs.client.materials.MeshDepthMaterial;
import com.google.code.gwt.threejs.client.materials.MeshFaceMaterial;
import com.google.code.gwt.threejs.client.materials.MeshLambertMaterial;
import com.google.code.gwt.threejs.client.materials.MeshNormalMaterial;
import com.google.code.gwt.threejs.client.materials.ParticleBasicMaterial;
import com.google.code.gwt.threejs.client.materials.ParticleCanvasMaterial;
import com.google.code.gwt.threejs.client.materials.enums.Blending;
import com.google.code.gwt.threejs.client.materials.enums.Mapping;
import com.google.code.gwt.threejs.client.materials.enums.Shading;
import com.google.code.gwt.threejs.client.renderers.renderables.Renderable;
import com.google.code.gwt.threejs.client.renderers.renderables.RenderableFace3;
import com.google.code.gwt.threejs.client.renderers.renderables.RenderableFace4;
import com.google.code.gwt.threejs.client.renderers.renderables.RenderableLine;
import com.google.code.gwt.threejs.client.renderers.renderables.RenderableParticle;
import com.google.code.gwt.threejs.client.renderers.renderables.RenderableVertex;
import com.google.code.gwt.threejs.client.scenes.Scene;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.CanvasPixelArray;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.Context2d.Composite;
import com.google.gwt.canvas.dom.client.Context2d.LineCap;
import com.google.gwt.canvas.dom.client.Context2d.LineJoin;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.user.client.ui.Image;

public final class CanvasRenderer {
	
	public static class CanvasData {
		public int vertices, faces;
	}
	
	Projector projector;
	Canvas canvas, pixelMap, gradientMap;
	Context2d context, pixelMapContext, gradientMapContext;
	ImageData pixelMapImage;	
	CanvasPixelArray pixelMapData;
	int gradientMapQuality;
	int canvasWidth, canvasHeight, canvasWidthHalf, canvasHeightHalf;
	Integer contextStrokeStyle, contextFillStyle, contextLineWidth;
	LineCap contextLineCap;
	LineJoin contextLineJoin;
	
	Color clearColor;
	double clearOpacity;
	double contextGlobalAlpha;
	Blending contextGlobalCompositeOperation;
	
	Color color0, color1, color2, color3, color4;
	double near;
	double far;
	Rectangle clipRect;
	Rectangle clearRect;
	Rectangle bboxRect;
	Boolean enableLighting;
	Color light, ambientLight, directionalLights, pointLights;
	static double pi2 = Math.PI*2;
	Vector3 vector3;
	Boolean autoClear;
	Boolean sortObjects;
	Boolean sortElements;
	List<Renderable> renderList;
	
	CanvasData data;
	
	public CanvasRenderer(Canvas canvas){
		this.data = new CanvasData();
		this.projector = new Projector();
		this.canvas = canvas;
		this.context = canvas.getContext2d();
		this.clearColor = new Color(0x000000);
		this.clearOpacity = 0;
		this.contextGlobalAlpha = 1;
		this.contextGlobalCompositeOperation = Blending.NormalBlending;
		
		this.color0 = new Color(0x000000);
		this.color1 = new Color(0x000000);
		this.color2 = new Color(0x000000);
		this.color3 = new Color(0x000000);
		this.color4 = new Color(0x000000);
		
		this.clipRect = new Rectangle();
		this.clearRect = new Rectangle();
		this.bboxRect = new Rectangle();
		
		this.enableLighting = false;
		this.light = new Color(0x000000);
		this.ambientLight = new Color(0x000000);
		this.directionalLights = new Color(0x000000);
		this.pointLights = new Color(0x000000);
		this.vector3 = new Vector3(); // Needed for PointLight
		
		this.pixelMap = Canvas.createIfSupported();
		this.pixelMap.setPixelSize(2, 2);
		this.pixelMapContext = pixelMap.getContext2d();
		this.pixelMapContext.setFillStyle("rgba(0,0,0,1)");
		this.pixelMapContext.fillRect(0, 0, 2, 2);
		this.pixelMapImage = pixelMapContext.getImageData(0, 0, 2, 2);
		this.pixelMapData = pixelMapImage.getData();
		
		this.gradientMapQuality = 16;
		this.gradientMap = Canvas.createIfSupported();
		this.gradientMap.setPixelSize(gradientMapQuality, gradientMapQuality);
		this.gradientMapContext = gradientMap.getContext2d();
		this.gradientMapContext.translate(-gradientMapQuality/2, -gradientMapQuality/2);
		this.gradientMapContext.scale(gradientMapQuality, gradientMapQuality);
		this.gradientMapQuality--; // Fix UVs
		
		this.autoClear = true;
		this.sortObjects = true;
		this.sortElements = true;
	}

	// Properties
	public void setAutoClear(Boolean autoClear){
		this.autoClear = autoClear ;
	};
	public Boolean getAutoClear(){
		return this.autoClear;
	};
	public void setSortObjects(Boolean sortObjects){
		this.sortObjects = sortObjects;
	};
	public Boolean getSortObjects(){
		return this.sortObjects;
	};
	public void setSortElements(Boolean sortElements){
		this.sortElements = sortElements;
	};
	public Boolean getSortElements(){
		return this.sortElements;
	};
	public Canvas getDomElement(){
		return this.canvas;
	};
	
	// Methods
	
	public void setSize(int width, int height){
		this.canvasWidth = width;
		this.canvasHeight = height;
		this.canvasWidthHalf = canvasWidth / 2;
		this.canvasHeightHalf = canvasHeight / 2;

		this.canvas.getCanvasElement().setWidth(this.canvasWidth);
		this.canvas.getCanvasElement().setHeight(this.canvasHeight);
		this.clipRect.set(-this.canvasWidthHalf, -this.canvasHeightHalf, this.canvasWidthHalf, this.canvasHeightHalf);

		this.contextGlobalAlpha = 1;
		this.contextGlobalCompositeOperation = Blending.NormalBlending;
		this.contextStrokeStyle = null;
		this.contextFillStyle = null;
		this.contextLineWidth = null;
		this.contextLineCap = null;
	};
	
	public void setClearColor(Color color, double opacity){
		this.clearColor = color;
		this.clearOpacity = opacity;
	};

	public void setClearColorHex(int hex, double opacity){
		this.clearColor.setHex(hex);
		this.clearOpacity = opacity;
	};
	
	public void clear(){
		this.context.setTransform(1,0,0,-1,this.canvasWidthHalf,this.canvasHeightHalf);

		if (!this.clearRect.isEmpty()) {

			this.clearRect.inflate(1);
			this.clearRect.minSelf(this.clipRect);

			if (this.clearColor.getHex() == 0x000000 && this.clearOpacity == 0) {
				this.context.clearRect(
					this.clearRect.getLeft(),
					this.clearRect.getTop(),
					this.clearRect.getWidth(),
					this.clearRect.getHeight()
				);
			} else {
				setBlending(Blending.NormalBlending);
				setOpacity(1);
				double r = Math.floor(this.clearColor.getR()*255),
				g = Math.floor(this.clearColor.getG()*255),
				b = Math.floor(this.clearColor.getB()*255);

				this.context.setFillStyle("rgba("+r+","+g+","+b+","+this.clearOpacity+")");
				this.context.fillRect(
					this.clearRect.getLeft(),
					this.clearRect.getTop(),
					this.clearRect.getWidth(),
					this.clearRect.getHeight()
				);
			}
			this.clearRect.empty();

		}
	};
	
	public void render(Scene scene, Camera camera){
		//var e, el, element, m, ml, fm, fml, material;

		if (this.autoClear) {
			this.clear();
		} else {
			this.context.setTransform(1,0,0,-1, this.canvasWidthHalf, this.canvasHeightHalf);
		}

		this.data.vertices = 0;
		this.data.faces = 0;

		this.renderList = this.projector.projectScene(scene, camera, this.sortElements);

		/* DEBUG
		this.context.setLineWidth(1);
		this.context.setFillStyle("rgba( 0, 255, 255, 0.5 )");
		this.context.fillRect(this.clipRect.getLeft(), this.clipRect.getTop(), this.clipRect.getWidth(), this.clipRect.getHeight());
		*/
		
		this.enableLighting = scene.getLights().size() > 0;

		if (this.enableLighting) {
			 this.calculateLights(scene);
		}

		for (int e = 0, el = this.renderList.size(); e < el; e++ ) {
			Renderable element = this.renderList.get(e);

			this.bboxRect.empty();

			if (element.getClass() == RenderableParticle.class) {

				RenderableParticle v1 = (RenderableParticle)element;
				v1.setX(v1.getX() * this.canvasWidthHalf);
				v1.setY(v1.getY() * this.canvasHeightHalf);

				int m = 0, ml = v1.getMaterials().size();

				while(m < ml) {
					Material material = v1.getMaterials().get(m++);
					if (material.getOpacity() != 0){
						renderParticle(v1, v1, material, scene);
					}
				}

			} else if (element.getClass() == RenderableLine.class) {

				RenderableLine line = (RenderableLine)element;
				RenderableVertex v1 = line.getV1();
				RenderableVertex v2 = line.getV2();

				Vector4 v1ps = v1.getPositionScreen(),
						v2ps = v2.getPositionScreen();
				
				v1ps.setX(v1ps.getX() * this.canvasWidthHalf);
				v1ps.setY(v1ps.getY() * this.canvasHeightHalf);
				v2ps.setX(v2ps.getX() * this.canvasWidthHalf);
				v2ps.setY(v2ps.getY() * this.canvasHeightHalf);

				this.bboxRect.addPoint(v1ps.getX(), v1ps.getY());
				this.bboxRect.addPoint(v2ps.getX(), v2ps.getY());

				if (this.clipRect.intersects(this.bboxRect)) {

					int m = 0, ml = line.getMaterials().size();

					while (m < ml){

						Material material = line.getMaterials().get(m++);
						if (material.getOpacity() != 0){
							renderLine(v1,v2, element, material, scene);
						}
					}

				}


			} else if (element.getClass() == RenderableFace3.class) {

				RenderableFace3 rf3 = (RenderableFace3)element;
				RenderableVertex v1 = rf3.getV1(),
				v2 = rf3.getV2(),
				v3 = rf3.getV3();
				
				Vector4 v1ps = v1.getPositionScreen(),
				v2ps = v2.getPositionScreen(),
				v3ps = v3.getPositionScreen();

				v1ps.setX(v1ps.getX() * this.canvasWidthHalf);
				v1ps.setY(v1ps.getY() * this.canvasHeightHalf);
				v2ps.setX(v2ps.getX() * this.canvasWidthHalf);
				v2ps.setY(v2ps.getY() * this.canvasHeightHalf);
				v3ps.setX(v3ps.getX() * this.canvasWidthHalf);
				v3ps.setY(v3ps.getY() * this.canvasHeightHalf);

				if (rf3.isOverdraw()) {
					expand(v1ps, v2ps);
					expand(v2ps, v3ps);
					expand(v3ps, v1ps);
				}

				this.bboxRect.add3Points(v1ps.getX(), v1ps.getY(), v2ps.getX(), v2ps.getY(), v3ps.getX(), v3ps.getY());

				if (this.clipRect.intersects(this.bboxRect)) {
					int m = 0, ml = rf3.getMeshMaterials().size();
					while (m < ml) {
						Material material = rf3.getMeshMaterials().get(m++);
						if (material.getClass() == MeshFaceMaterial.class) {
							int fm = 0, fml = rf3.getFaceMaterials().size();
							while (fm < fml) {
								material = rf3.getFaceMaterials().get(fm++);
								if (material.getOpacity() != 0){
									renderFace3(camera, v1, v2, v3, 0, 1, 2, rf3, material, scene);
								}
							}
							continue;
						}
						if (material.getOpacity() != 0){
							renderFace3(camera, v1, v2, v3, 0, 1, 2, rf3, material, scene);
						}
					}
				}

			} else if (element.getClass() == RenderableFace4.class) {
				RenderableFace4 rf4 = (RenderableFace4)element;

				RenderableVertex v1 = rf4.getV1(),
				v2 = rf4.getV2(),
				v3 = rf4.getV3(),
				v4 = rf4.getV4(),
				v5 = new RenderableVertex(),
				v6 = new RenderableVertex();

				Vector4 v1ps = v1.getPositionScreen(),
				v2ps = v2.getPositionScreen(),
				v3ps = v3.getPositionScreen(),
				v4ps = v4.getPositionScreen(),
				v5ps = v5.getPositionScreen(),
				v6ps = v6.getPositionScreen();
				
				v1ps.setX(v1ps.getX() * this.canvasWidthHalf);
				v1ps.setY(v1ps.getY() * this.canvasHeightHalf);
				v2ps.setX(v2ps.getX() * this.canvasWidthHalf);
				v2ps.setY(v2ps.getY() * this.canvasHeightHalf);
				v3ps.setX(v3ps.getX() * this.canvasWidthHalf);
				v3ps.setY(v3ps.getY() * this.canvasHeightHalf);
				v4ps.setX(v4ps.getX() * this.canvasWidthHalf);
				v4ps.setY(v4ps.getY() * this.canvasHeightHalf);

				v5.getPositionScreen().copy(v2ps);
				v6.getPositionScreen().copy(v4ps);

				if (rf4.isOverdraw()) {
					expand(v1ps, v2ps);
					expand(v2ps, v4ps);
					expand(v4ps, v1ps);

					expand(v3ps, v5ps);
					expand(v3ps, v6ps);
				}

				this.bboxRect.addPoint(v1ps.getX(), v1ps.getY());
				this.bboxRect.addPoint(v2ps.getX(), v2ps.getY());
				this.bboxRect.addPoint(v3ps.getX(), v3ps.getY());
				this.bboxRect.addPoint(v4ps.getX(), v4ps.getY());
				
				//GWT.log("bboxRect [b, height, l, r, t, width]: "+this.bboxRect.getBottom()+","+this.bboxRect.getHeight()+","+this.bboxRect.getLeft()+","+this.bboxRect.getRight()+","+this.bboxRect.getTop()+","+this.bboxRect.getWidth());
				//GWT.log("clipRect [b, height, l, r, t, width]: "+this.clipRect.getBottom()+","+this.clipRect.getHeight()+","+this.clipRect.getLeft()+","+this.clipRect.getRight()+","+this.clipRect.getTop()+","+this.clipRect.getWidth());
				
				if (this.clipRect.intersects(this.bboxRect)) {
					int m = 0, ml = rf4.getMeshMaterials().size();
					while (m < ml) {
						Material material = rf4.getMeshMaterials().get(m++);

						if (material.getClass() == MeshFaceMaterial.class) {

							int fm = 0, fml = rf4.getFaceMaterials().size();

							while (fm < fml) {

								material = rf4.getFaceMaterials().get(fm++);
								if (material != null && material.getOpacity() != 0){
									renderFace4(camera, rf4, v1, v2, v3, v4, v5, v6, rf4, material, scene);
								}
							}
							continue;
						}

						if (material.getOpacity() != 0){
							renderFace4(camera, rf4, v1, v2, v3, v4, v5, v6, rf4, material, scene);
						}
					}

				}

			}

			/* DEBUG
			this.context.setLineWidth(1);
			this.context.setStrokeStyle("rgba(0, 255, 0, 0.5)");
			this.context.strokeRect(this.bboxRect.getLeft(), this.bboxRect.getTop(), this.bboxRect.getWidth(), this.bboxRect.getHeight());
			*/

			this.clearRect.addRectangle(this.bboxRect);


		}

		/* DEBUG
		GWT.log("Clear Rect is: "+this.clearRect.getLeft()+", "+this.clearRect.getTop()+", "+this.clearRect.getWidth()+", "+this.clearRect.getHeight());
		this.context.setLineWidth(1);
		this.context.setStrokeStyle("rgba( 255, 0, 0, 0.5 )");
		this.context.strokeRect(this.clearRect.getLeft(), this.clearRect.getTop(), this.clearRect.getWidth(), this.clearRect.getHeight());
		*/
		
		this.context.setTransform(1, 0, 0, 1, 0, 0);
	};
	
	private void renderFace3(Camera camera, RenderableVertex v1, RenderableVertex v2, RenderableVertex v3, int uv1, int uv2, int uv3, RenderableFace3 element, Material material, Scene scene){
		this.data.vertices += 3;
		this.data.faces++;

		setOpacity(material.getOpacity());
		setBlending(material.getBlending());

		double v1x = v1.getPositionScreen().getX(),
		v1y = v1.getPositionScreen().getY(),
		v2x = v2.getPositionScreen().getX(),
		v2y = v2.getPositionScreen().getY(),
		v3x = v3.getPositionScreen().getX(),
		v3y = v3.getPositionScreen().getY();

		drawTriangle(v1x,v1y,v2x,v2y,v3x,v3y);

		if (material.getClass() == MeshBasicMaterial.class) {

			MeshBasicMaterial meshMaterial = (MeshBasicMaterial)material;
			if (meshMaterial.getMap() != null/* && !material.wireframe*/) {

				if (meshMaterial.getMap().getMapping() == Mapping.UVMapping) {

					List<UV> uvs = element.getUvs().get(0);
					
					UV uv1el = uvs.get(uv1),
					uv2el = uvs.get(uv2),
					uv3el = uvs.get(uv3);
					texturePath(v1x,v1y,v2x,v2y,v3x,v3y, meshMaterial.getMap().getImage(), uv1el.getU(), uv1el.getV(), uv2el.getU(), uv2el.getV(), uv3el.getU(), uv3el.getV());
				}


			} else if (meshMaterial.getEnvMap() != null) {

				if (meshMaterial.getEnvMap().getMapping() == Mapping.SphericalReflectionMapping) {

					Matrix4 cameraMatrix = camera.getMatrixWorldInverse();

					this.vector3.copy(element.getVertexNormalsWorld().get(0));
					double uv1x = (this.vector3.getX() * cameraMatrix.getN11() + this.vector3.getY() * cameraMatrix.getN12() + this.vector3.getZ() * cameraMatrix.getN13()) * 0.5 + 0.5;
					double uv1y = - (this.vector3.getX() * cameraMatrix.getN21() + this.vector3.getY() * cameraMatrix.getN22() + this.vector3.getZ() * cameraMatrix.getN23()) * 0.5 + 0.5;

					this.vector3.copy(element.getVertexNormalsWorld().get(1));
					double uv2x = (this.vector3.getX() * cameraMatrix.getN11() + this.vector3.getY() * cameraMatrix.getN12() + this.vector3.getZ() * cameraMatrix.getN13()) * 0.5 + 0.5;
					double uv2y = - (this.vector3.getX() * cameraMatrix.getN21() + this.vector3.getY() * cameraMatrix.getN22() + this.vector3.getZ() * cameraMatrix.getN23()) * 0.5 + 0.5;

					this.vector3.copy(element.getVertexNormalsWorld().get(2));
					double uv3x = (this.vector3.getX() * cameraMatrix.getN11() + this.vector3.getY() * cameraMatrix.getN12() + this.vector3.getZ() * cameraMatrix.getN13()) * 0.5 + 0.5;
					double uv3y = - (this.vector3.getX() * cameraMatrix.getN21() + this.vector3.getY() * cameraMatrix.getN22() + this.vector3.getZ() * cameraMatrix.getN23()) * 0.5 + 0.5;

					texturePath(v1x,v1y,v2x,v2y,v3x,v3y, meshMaterial.getEnvMap().getImage(),uv1x,uv1y,uv2x,uv2y,uv3x,uv3y);

				}/* else if ( material.envMap.mapping == THREE.SphericalRefractionMapping ) {
				}*/

			} else {

				if (meshMaterial.getWireframe()){
					strokePath(meshMaterial.getColor(), meshMaterial.getWireframeLinewidth(), meshMaterial.getWireframeLinecap(), meshMaterial.getWireframeLinejoin());
				} else {
					fillPath(meshMaterial.getColor());
				}
			}

		} else if (material.getClass() == MeshLambertMaterial.class) {

			MeshLambertMaterial mlMaterial = (MeshLambertMaterial)material;
			if (mlMaterial.getMap() != null && !mlMaterial.isWireframe()) {

				if (mlMaterial.getMap().getMapping() == Mapping.UVMapping) {

					List<UV> uvs = element.getUvs().get(0);
					UV uv1el = uvs.get(uv1),
					uv2el = uvs.get(uv2),
					uv3el = uvs.get(uv3);
					
					texturePath(v1x,v1y,v2x,v2y,v3x,v3y, mlMaterial.getMap().getImage(), uv1el.getU(), uv1el.getV(), uv2el.getU(), uv2el.getV(), uv3el.getU(), uv3el.getV());

				}
				setBlending(Blending.SubtractiveBlending);
			}

			if (this.enableLighting) {

				if (!mlMaterial.isWireframe() && mlMaterial.getShading() == Shading.SmoothShading && element.getVertexNormalsWorld().size() == 3 ) {

					int r = this.ambientLight.getR();
					int g = this.ambientLight.getG();
					int b = this.ambientLight.getB();
					
					this.color1.setR(r);
					this.color2.setR(r);
					this.color3.setR(r);

					this.color1.setG(g);
					this.color2.setG(g);
					this.color3.setG(g);

					this.color1.setB(b);
					this.color2.setB(b);
					this.color3.setB(b);

					calculateLight(scene, element.getV1().getPositionWorld(), element.getVertexNormalsWorld().get(0), this.color1);
					calculateLight(scene, element.getV2().getPositionWorld(), element.getVertexNormalsWorld().get(1), this.color2);
					calculateLight(scene, element.getV3().getPositionWorld(), element.getVertexNormalsWorld().get(2), this.color3);

					this.color4.setR((int)((this.color2.getR() + this.color3.getR()) * 0.5));
					this.color4.setG((int)((this.color2.getG() + this.color3.getG()) * 0.5));
					this.color4.setB((int)((this.color2.getB() + this.color3.getB()) * 0.5));

					Canvas bitmap = getGradientTexture(this.color1, this.color2, this.color3, this.color4);

					texturePath(v1x,v1y,v2x,v2y,v3x,v3y, bitmap, 0, 0, 1, 0, 0, 1);

				} else {

					this.light.setR(this.ambientLight.getR());
					this.light.setG(this.ambientLight.getG());
					this.light.setB(this.ambientLight.getB());

					calculateLight( scene, element.getCentroidWorld(), element.getNormalWorld(), this.light);

					this.color0.setR(Math.max(0, Math.min(mlMaterial.getColor().getR() * this.light.getR(), 1)));
					this.color0.setG(Math.max(0, Math.min(mlMaterial.getColor().getG() * this.light.getG(), 1)));
					this.color0.setB(Math.max(0, Math.min(mlMaterial.getColor().getB() * this.light.getB(), 1)));
					this.color0.updateHex();
					
					if (mlMaterial.isWireframe()){
						strokePath(this.color0, mlMaterial.getWireframeLinewidth(), mlMaterial.getWireframeLinecap(), mlMaterial.getWireframeLinejoin());
					} else {
						fillPath(this.color0);
					}
				}

			} else {
				if (mlMaterial.isWireframe()){
					strokePath(mlMaterial.getColor(), mlMaterial.getWireframeLinewidth(), mlMaterial.getWireframeLinecap(), mlMaterial.getWireframeLinejoin());
				} else {
					fillPath(mlMaterial.getColor());
				}

			}

		} else if (material.getClass() == MeshDepthMaterial.class) {

			this.near = camera.getNear();
			this.far = camera.getFar();
			
			int cv1 = 1 - smoothstep(v1.getPositionScreen().getZ(), this.near, this.far),
				cv2 = 1 - smoothstep( v2.getPositionScreen().getZ(), this.near, this.far),
				cv3 = 1 - smoothstep( v3.getPositionScreen().getZ(), this.near, this.far);

			this.color1.setR(cv1);
			this.color1.setG(cv1);
			this.color1.setB(cv1);
			
			this.color2.setR(cv2);
			this.color2.setG(cv2);
			this.color2.setB(cv2);
			
			this.color3.setR(cv3);
			this.color3.setG(cv3);
			this.color3.setB(cv3);
			
			this.color4.setR((int)((this.color2.getR() + this.color3.getR()) * 0.5));
			this.color4.setG((int)((this.color2.getG() + this.color3.getG()) * 0.5));
			this.color4.setB((int)((this.color2.getB() + this.color3.getB()) * 0.5));

			Canvas bitmap = getGradientTexture(this.color1, this.color2, this.color3, this.color4);

			texturePath(v1x,v1y,v2x,v2y,v3x,v3y, bitmap, 0, 0, 1, 0, 0, 1);

		} else if (material.getClass() == MeshNormalMaterial.class) {

			MeshNormalMaterial mnMaterial = (MeshNormalMaterial)material;
			this.color0.setR(normalToComponent(element.getNormalWorld().getX()));
			this.color0.setG(normalToComponent(element.getNormalWorld().getY()));
			this.color0.setB(normalToComponent(element.getNormalWorld().getZ()));
			this.color0.updateHex();
			
			if (mnMaterial.isWireframe()){
				strokePath(this.color0, mnMaterial.getWireframeLinewidth(), mnMaterial.getWireframeLinecap(), mnMaterial.getWireframeLinejoin());
			} else {
				fillPath(this.color0);
			}
		}
	}
	
	private void renderFace4(Camera camera, RenderableFace4 element, RenderableVertex v1, RenderableVertex v2, RenderableVertex v3, RenderableVertex v4, RenderableVertex v5, RenderableVertex v6, RenderableFace4 rf4, Material material, Scene scene){
		this.data.vertices += 4;
		this.data.faces++;

		this.setOpacity(material.getOpacity());
		this.setBlending(material.getBlending());

		if (((material instanceof HasMaterialMap) || (material instanceof HasMaterialEnvMap))) {
			// Let renderFace3() handle this
			renderFace3(camera, v1, v2, v4, 0, 1, 3, element, material, scene);
			renderFace3(camera, v5, v3, v6, 1, 2, 3, element, material, scene);
			return;
		}

		double v1x = v1.getPositionScreen().getX(),
		v1y = v1.getPositionScreen().getY(),
		v2x = v2.getPositionScreen().getX(),
		v2y = v2.getPositionScreen().getY(),
		v3x = v3.getPositionScreen().getX(),
		v3y = v3.getPositionScreen().getY(),
		v4x = v4.getPositionScreen().getX(),
		v4y = v4.getPositionScreen().getY(),
		v5x = v5.getPositionScreen().getX(),
		v5y = v5.getPositionScreen().getY(),
		v6x = v6.getPositionScreen().getX(),
		v6y = v6.getPositionScreen().getY();

		if (material.getClass() == MeshBasicMaterial.class) {

			MeshBasicMaterial mbMaterial = (MeshBasicMaterial)material;
			drawQuad(v1x, v1y, v2x, v2y, v3x, v3y, v4x, v4y);

			if (mbMaterial.getWireframe()){
				strokePath(mbMaterial.getColor(), mbMaterial.getWireframeLinewidth(), mbMaterial.getWireframeLinecap(), mbMaterial.getWireframeLinejoin());
			} else {
				fillPath(mbMaterial.getColor());
			}

		} else if (material.getClass() == MeshLambertMaterial.class) {

			MeshLambertMaterial mlMaterial = (MeshLambertMaterial)material;
			if (this.enableLighting) {

				if (!mlMaterial.isWireframe() && mlMaterial.getShading() == Shading.SmoothShading && element.getVertexNormalsWorld().size() == 4) {

					int r = this.ambientLight.getR(),
						g = this.ambientLight.getG(),
						b = this.ambientLight.getB();
					
					this.color1.setR(r);
					this.color2.setR(r);
					this.color3.setR(r);
					this.color4.setR(r);

					this.color1.setG(g);
					this.color2.setG(g);
					this.color3.setG(g);
					this.color4.setG(g);

					this.color1.setB(b);
					this.color2.setB(b);
					this.color3.setB(b);
					this.color4.setB(b);

					calculateLight(scene, element.getV1().getPositionWorld(), element.getVertexNormalsWorld().get(0), this.color1);
					calculateLight(scene, element.getV2().getPositionWorld(), element.getVertexNormalsWorld().get(1), this.color2);
					calculateLight(scene, element.getV4().getPositionWorld(), element.getVertexNormalsWorld().get(3), this.color3);
					calculateLight(scene, element.getV3().getPositionWorld(), element.getVertexNormalsWorld().get(2), this.color4);

					Canvas bitmap = getGradientTexture(this.color1, this.color2, this.color3, this.color4);

					// TODO: UVs are incorrect, v4->v3?

					drawTriangle(v1x,v1y,v2x,v2y,v4x,v4y);
					texturePath(v1x,v1y,v2x,v2y,v4x,v4y,bitmap, 0, 0, 1, 0, 0, 1);

					drawTriangle(v5x,v5y,v3x,v3y,v6x,v6y);
					texturePath(v5x,v5y,v3x,v3y,v6x,v6y,bitmap, 1, 0, 1, 1, 0, 1);

				} else {

					this.light.setR(this.ambientLight.getR());
					this.light.setG(this.ambientLight.getG());
					this.light.setB(this.ambientLight.getB());

					calculateLight(scene, element.getCentroidWorld(), element.getNormalWorld(), this.light);

					this.color0.setR(Math.max(0, Math.min(mlMaterial.getColor().getR() * this.light.getR(), 1)));
					this.color0.setG(Math.max(0, Math.min(mlMaterial.getColor().getG() * this.light.getG(), 1)));
					this.color0.setB(Math.max(0, Math.min(mlMaterial.getColor().getB() * this.light.getB(), 1)));
					this.color0.updateHex();

					drawQuad(v1x,v1y,v2x,v2y,v3x,v3y,v4x,v4y);
					
					if (mlMaterial.isWireframe()){
						strokePath(this.color0, mlMaterial.getWireframeLinewidth(), mlMaterial.getWireframeLinecap(), mlMaterial.getWireframeLinejoin());
					} else {
						fillPath(this.color0);
					}
				}

			} else {
				drawQuad(v1x,v1y,v2x,v2y,v3x,v3y,v4x,v4y);
				if (mlMaterial.isWireframe()) {
					strokePath(mlMaterial.getColor(), mlMaterial.getWireframeLinewidth(), mlMaterial.getWireframeLinecap(), mlMaterial.getWireframeLinejoin());
				} else {
					fillPath(mlMaterial.getColor());
				}
			}

		} else if (material.getClass() == MeshNormalMaterial.class) {

			MeshNormalMaterial mnMaterial = (MeshNormalMaterial)material;
			this.color0.setR(normalToComponent(element.getNormalWorld().getX()));
			this.color0.setG(normalToComponent(element.getNormalWorld().getY()));
			this.color0.setB(normalToComponent(element.getNormalWorld().getZ()));
			this.color0.updateHex();

			drawQuad(v1x,v1y,v2x,v2y,v3x,v3y,v4x,v4y);
			
			if (mnMaterial.isWireframe()){
				strokePath(this.color0, mnMaterial.getWireframeLinewidth(), mnMaterial.getWireframeLinecap(), mnMaterial.getWireframeLinejoin());
			} else {
				fillPath(this.color0);
			}
		} else if ( material.getClass() == MeshDepthMaterial.class) {

			this.near = camera.getNear();
			this.far = camera.getFar();
			
			int cv1 = 1 - smoothstep(v1.getPositionScreen().getZ(), this.near, this.far),
				cv2 = 1 - smoothstep(v2.getPositionScreen().getZ(), this.near, this.far),
				cv3 = 1 - smoothstep(v3.getPositionScreen().getZ(), this.near, this.far),
				cv4 = 1 - smoothstep(v4.getPositionScreen().getZ(), this.near, this.far);

			this.color1.setR(cv1);
			this.color1.setG(cv1);
			this.color1.setB(cv1);
			
			this.color2.setR(cv2);
			this.color2.setG(cv2);
			this.color2.setB(cv2);
			
			this.color3.setR(cv4);
			this.color3.setG(cv4);
			this.color3.setB(cv4);
			
			this.color4.setR(cv3);
			this.color4.setG(cv3);
			this.color4.setB(cv3);
			
			Canvas bitmap = getGradientTexture(this.color1,this.color2,this.color3,this.color4);

			// TODO: UVs are incorrect, v4->v3?

			drawTriangle(v1x,v1y,v2x,v2y,v4x,v4y);
			texturePath(v1x,v1y,v2x,v2y,v4x,v4y, bitmap, 0, 0, 1, 0, 0, 1);

			drawTriangle(v5x,v5y,v3x,v3y,v6x,v6y);
			texturePath(v5x,v5y,v3x,v3y,v6x,v6y, bitmap, 1, 0, 1, 1, 0, 1);
		}
	}
	
	private void renderLine(RenderableVertex v1, RenderableVertex v2, Renderable element, Material material, Scene scene){
		setOpacity(material.getOpacity());
		setBlending(material.getBlending());

		this.context.beginPath();
		this.context.moveTo(v1.getPositionScreen().getX(), v1.getPositionScreen().getY());
		this.context.lineTo(v2.getPositionScreen().getX(), v2.getPositionScreen().getY());
		this.context.closePath();

		if (material.getClass() == LineBasicMaterial.class) {

			LineBasicMaterial lbMaterial = (LineBasicMaterial)material;
			setLineWidth(lbMaterial.getLinewidth());
			setLineCap(lbMaterial.getLinecap());
			setLineJoin(lbMaterial.getLinejoin());
			setStrokeStyle(lbMaterial.getColor());

			this.context.stroke();
			this.bboxRect.inflate(lbMaterial.getLinewidth() * 2);
		}
	}
	
	private void renderParticle(RenderableParticle v1, RenderableParticle element, Material material, Scene scene){
		setOpacity(material.getOpacity());
		setBlending(material.getBlending());

		double width, height, bitmapWidth, bitmapHeight;
		double scaleX, scaleY;
		Image bitmap;

		if (material.getClass() == ParticleBasicMaterial.class) {

			ParticleBasicMaterial pbMaterial = (ParticleBasicMaterial)material;
			if (pbMaterial.getMap() != null) {

				bitmap = pbMaterial.getMap().getImage();
				bitmapWidth = bitmap.getWidth() >> 1;
				bitmapHeight = bitmap.getHeight() >> 1;

				scaleX = element.getScale().getX() * this.canvasWidthHalf;
				scaleY = element.getScale().getY() * this.canvasHeightHalf;

				width = scaleX * bitmapWidth;
				height = scaleY * bitmapHeight;

				// TODO: Rotations break this...

				this.bboxRect.set(v1.getX().intValue()-width, v1.getY().intValue()-height, v1.getX().intValue()+width, v1.getY().intValue()+height);

				if (!this.clipRect.intersects(this.bboxRect)) {
					return;
				}

				this.context.save();
				this.context.translate(v1.getX(), v1.getY());
				this.context.rotate(-element.getRotation());
				this.context.scale(scaleX, -scaleY);

				this.context.translate( - bitmapWidth, - bitmapHeight );
				this.context.drawImage(bitmap.getElement().<ImageElement>cast(), 0, 0);

				this.context.restore();

			}

			/* DEBUG
			_context.beginPath();
			_context.moveTo( v1.x - 10, v1.y );
			_context.lineTo( v1.x + 10, v1.y );
			_context.moveTo( v1.x, v1.y - 10 );
			_context.lineTo( v1.x, v1.y + 10 );
			_context.closePath();
			_context.strokeStyle = 'rgb(255,255,0)';
			_context.stroke();
			*/

		} else if (material.getClass() == ParticleCanvasMaterial.class) {

			ParticleCanvasMaterial pcMaterial = (ParticleCanvasMaterial)material;
			width =  (element.getScale().getX() * this.canvasWidthHalf);
			height =  (element.getScale().getY() * this.canvasHeightHalf);

			this.bboxRect.set(v1.getX().intValue()-width, v1.getY().intValue()-height, v1.getX().intValue()+width, v1.getY().intValue()+height);

			if (!this.clipRect.intersects(this.bboxRect)) {
				return;
			}

			setStrokeStyle(pcMaterial.getColor());
			setFillStyle(pcMaterial.getColor());

			this.context.save();
			this.context.translate( v1.getX(), v1.getY() );
			this.context.rotate(-element.getRotation() );
			this.context.scale(width, height);

			pcMaterial.getProgram().run(this.context, null);

			this.context.restore();
		}
	}
	
	private void drawTriangle(double x0, double y0, double x1, double y1, double x2, double y2){
		this.context.beginPath();
		this.context.moveTo(x0, y0);
		this.context.lineTo(x1, y1);
		this.context.lineTo(x2, y2);
		this.context.lineTo(x0, y0);
		this.context.closePath();
	}
	
	private void drawQuad(double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3){
		this.context.beginPath();
		this.context.moveTo(x0, y0);
		this.context.lineTo(x1, y1);
		this.context.lineTo(x2, y2);
		this.context.lineTo(x3, y3);
		this.context.lineTo(x0, y0);
		this.context.closePath();
	}
	
	private void strokePath(Color color, int lineWidth, LineCap lineCap, LineJoin lineJoin){
		this.setLineWidth(lineWidth);
		this.setLineCap(lineCap);
		this.setLineJoin(lineJoin);
		this.setStrokeStyle(color);

		this.context.stroke();

		this.bboxRect.inflate(lineWidth*2);
	}
	
	private void fillPath(Color color){
		this.setFillStyle(color);
		this.context.fill();
	}
	
	private static interface Drawable<T> {
		void draw(Context2d context, T element);
	}
	
	private static Drawable<CanvasElement> canvasDrawer = new Drawable<CanvasElement>() {

		@Override
		public void draw(Context2d context, CanvasElement element) {
			context.drawImage(element, 0, 0 );
		}
	};
	
	private static Drawable<ImageElement> imageDrawer = new Drawable<ImageElement>() {

		@Override
		public void draw(Context2d context, ImageElement element) {
			context.drawImage(element, 0, 0);
		}
	};

	private void texturePath(double x0, double y0, double x1, double y1, double x2, double y2, Canvas map, double u0, double v0, double u1, double v1, double u2, double v2){
		CanvasElement bitmapElement = map.getCanvasElement();
		int width = bitmapElement.getWidth() - 1,
		height = bitmapElement.getHeight() - 1;

		this.texturePath(x0, y0, x1, y1, x2, y2, bitmapElement, canvasDrawer, width, height, u0, v0, u1, v1, u2, v2);
	}
	
	private void texturePath(double x0, double y0, double x1, double y1, double x2, double y2, Image image, double u0, double v0, double u1, double v1, double u2, double v2){
		ImageElement img = image.getElement().<ImageElement>cast();
		int width = img.getWidth() - 1,
		height = img.getHeight() - 1;

		this.texturePath(x0, y0, x1, y1, x2, y2, img, imageDrawer, width, height, u0, v0, u1, v1, u2, v2);
	}
	
	private <T> void texturePath(double x0, double y0, double x1, double y1, double x2, double y2, T element, Drawable<T> drawer, double width, double height, double u0, double v0, double u1, double v1, double u2, double v2){
		// http://extremelysatisfactorytotalitarianism.com/blog/?p=2120

		double a,b,c,d,e,f;
		double idet;
		double det;

		u0 *= width; v0 *= height;
		u1 *= width; v1 *= height;
		u2 *= width; v2 *= height;

		x1 -= x0; y1 -= y0;
		x2 -= x0; y2 -= y0;

		u1 -= u0; v1 -= v0;
		u2 -= u0; v2 -= v0;

		det = u1 * v2 - u2 * v1;

		if (det != 0){
			idet = 1 / det;
	
			a = (v2*x1 - v1*x2)*idet;
			b = (v2*y1 - v1*y2)*idet;
			c = (u1*x2 - u2*x1)*idet;
			d = (u1*y2 - u2*y1)*idet; // true
	
			e = x0 - a * u0 - c * v0;
			f = y0 - b * u0 - d * v0; // true
			
			this.context.save();
			this.context.transform(a, b, c, d, e, f);
			this.context.clip();
			drawer.draw(this.context, element);
			this.context.restore();
		}
	}
	
	private Canvas getGradientTexture(Color color1, Color color2, Color color3, Color color4){
		// http://mrdoob.com/blog/post/710

		int c1r = ~~(color1.getR() * 255), c1g = ~~(color1.getG() * 255), c1b = ~~(color1.getB() * 255),
		c2r = ~~(color2.getR() * 255), c2g = ~~(color2.getG() * 255), c2b = ~~(color2.getB() * 255),
		c3r = ~~(color3.getR() * 255), c3g = ~~(color3.getG() * 255), c3b = ~~(color3.getB() * 255),
		c4r = ~~(color4.getR() * 255), c4g = ~~(color4.getG() * 255), c4b = ~~(color4.getB() * 255);

		this.pixelMapData.set(0, c1r < 0 ? 0 : c1r > 255 ? 255 : c1r);
		this.pixelMapData.set(1, c1g < 0 ? 0 : c1g > 255 ? 255 : c1g);
		this.pixelMapData.set(2, c1b < 0 ? 0 : c1b > 255 ? 255 : c1b);

		this.pixelMapData.set(4, c2r < 0 ? 0 : c2r > 255 ? 255 : c2r);
		this.pixelMapData.set(5, c2g < 0 ? 0 : c2g > 255 ? 255 : c2g);
		this.pixelMapData.set(6, c2b < 0 ? 0 : c2b > 255 ? 255 : c2b);

		this.pixelMapData.set(8, c3r < 0 ? 0 : c3r > 255 ? 255 : c3r);
		this.pixelMapData.set(9, c3g < 0 ? 0 : c3g > 255 ? 255 : c3g);
		this.pixelMapData.set(10, c3b < 0 ? 0 : c3b > 255 ? 255 : c3b);

		this.pixelMapData.set(12, c4r < 0 ? 0 : c4r > 255 ? 255 : c4r);
		this.pixelMapData.set(13, c4g < 0 ? 0 : c4g > 255 ? 255 : c4g);
		this.pixelMapData.set(14, c4b < 0 ? 0 : c4b > 255 ? 255 : c4b);

		this.pixelMapContext.putImageData(this.pixelMapImage, 0, 0);
		this.gradientMapContext.drawImage(this.pixelMap.getCanvasElement(), 0, 0);

		return this.gradientMap;
	}
	
	private int smoothstep(double value, double min, double max){
		double x = (value - min)/(max - min);
		return (int)(x*x*(3 - 2*x));
	}
	
	private int normalToComponent(double normal){
		double component = (normal+1)*0.5;
		return (int)(component < 0 ? 0 : (component > 1 ? 1 : component));
	}
	
	// Hide anti-alias gaps
	private void expand(Vector2 v1, Vector2 v2){
		double x = v2.getX() - v1.getX(), y =  v2.getY() - v1.getY(),
		unit = 1 / Math.sqrt(x*x + y*y);

		x *= unit; y *= unit;

		v2.setX(v2.getX()+x);
		v2.setY(v2.getY()+y);
		v1.setX(v1.getX()-x);
		v1.setY(v1.getY()-y);
	}
	
	private void setOpacity(double value){
		if (this.contextGlobalAlpha != value) {
			this.context.setGlobalAlpha(this.contextGlobalAlpha = value);
		}
	}
	
	private void setBlending(Blending value){
		if (this.contextGlobalCompositeOperation != value){
			switch (value) {
				case NormalBlending:
					this.context.setGlobalCompositeOperation(Composite.SOURCE_OVER);
					break;
				case AdditiveBlending:
					this.context.setGlobalCompositeOperation(Composite.LIGHTER);
					break;
				case SubtractiveBlending:
					this.context.setGlobalCompositeOperation("darker");
					break;
			}

			this.contextGlobalCompositeOperation = value;
		}
	}
	
	private void setLineWidth(int value){
		if (this.contextLineWidth != value) {
			this.context.setLineWidth(this.contextLineWidth = value);
		}
	}
	
	private void setLineCap(LineCap value){
		// "butt", "round", "square"
		if (this.contextLineCap != value) {
			this.context.setLineCap(this.contextLineCap = value);
		}
	}
	
	private void setLineJoin(LineJoin value){
		// "round", "bevel", "miter"
		if (this.contextLineJoin != value) {
			this.context.setLineJoin(this.contextLineJoin = value);
		}
	}
	
	private void setStrokeStyle(Color color){
		if (this.contextStrokeStyle != color.getHex()) {
			this.contextStrokeStyle = color.getHex();
			this.context.setStrokeStyle("#" + pad(this.contextStrokeStyle.toString()));
		}
	}
	
	private void setFillStyle(Color color){
		if (this.contextFillStyle != color.getHex()){
			this.contextFillStyle = color.getHex();
			this.context.setFillStyle("#" + pad(this.contextFillStyle.toString()));
		}
	}
	
	private String pad(String str){
		while (str.length() < 6) str = "0" + str;
		return str;
	}
	
	// Helper âRenderâ Methods
	
	private void calculateLight(Scene scene, Vector3 position, Vector3 normal, Color color){
		Light light;
		Color lightColor;
		double amount;
		List<Light> lights = scene.getLights();

		for (int l = 0, ll = lights.size(); l < ll; l++) {

			light = lights.get(l);
			lightColor = light.getColor();

			if (light.getClass() == DirectionalLight.class) {

				DirectionalLight directionalLight = (DirectionalLight)light;
				amount = normal.dot(directionalLight.getPosition());

				if ( amount <= 0 ) continue;

				amount *= directionalLight.getIntensity();

				color.setR((int)(color.getR() + lightColor.getR() * amount));
				color.setG((int)(color.getG() + lightColor.getG() * amount));
				color.setB((int)(color.getB() + lightColor.getB() * amount));

			} else if (light.getClass() == PointLight.class) {

				PointLight plight = (PointLight)light;
				amount = normal.dot(this.vector3.sub(plight.getPosition(), position).normalize());

				if (amount <= 0) continue;

				amount *= plight.getDistance() == 0 ? 1 : 1 - Math.min(position.distanceTo(plight.getPosition()) / plight.getDistance(), 1);

				if (amount == 0) continue;

				amount *= plight.getIntensity();

				color.setR((int)(color.getR() + lightColor.getR() * amount));
				color.setG((int)(color.getG() + lightColor.getG() * amount));
				color.setB((int)(color.getB() + lightColor.getB() * amount));

			}

		}
	}
	
	private void calculateLights(Scene scene){
		Light light;
		Color lightColor;
		ArrayList<Light> lights = scene.getLights();

		this.ambientLight.setRGB(0,0,0);
		this.directionalLights.setRGB(0,0,0);
		this.pointLights.setRGB(0,0,0);

		for (int l = 0, ll = lights.size(); l < ll; l++){

			light = lights.get(l);
			lightColor = light.getColor();

			if (light.getClass() == AmbientLight.class) {

				this.ambientLight.setR(this.ambientLight.getR() + lightColor.getR());
				this.ambientLight.setG(this.ambientLight.getG() + lightColor.getG());
				this.ambientLight.setB(this.ambientLight.getB() + lightColor.getB());

			} else if (light.getClass() == DirectionalLight.class) {

				// for particles

				this.directionalLights.setR(this.directionalLights.getR() + lightColor.getR());
				this.directionalLights.setG(this.directionalLights.getG() + lightColor.getG());
				this.directionalLights.setB(this.directionalLights.getB() + lightColor.getB());

			} else if (light.getClass() == PointLight.class) {

				// for particles

				this.pointLights.setR(this.pointLights.getR() + lightColor.getR());
				this.pointLights.setG(this.pointLights.getG() + lightColor.getG());
				this.pointLights.setB(this.pointLights.getB() + lightColor.getB());

			}

		}
	}
}

=======
package VEW.Scenario2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import VEW.Common.MessageBox;
import VEW.Common.StringTools;
import VEW.Common.VEWUtilities;
import VEW.Common.XML.XMLTag;
import VEW.Controller2.VEWController2;

public class InitialConditionsPanel extends JPanel  {
  private static VEWController2 vc2;
  private DepthTableModel depthValTableModel = new DepthTableModel();
  private JTable depthValTable = new JTable(depthValTableModel);
  private JButton fillBetween = new JButton("Interpolate");
  private DepthGraphPanel dgp = new DepthGraphPanel();
  private DefaultListModel depthEntriesModel = new DefaultListModel();  
  private JList depthEntryList = new JList(depthEntriesModel);
  private DefaultListModel importModel = new DefaultListModel();  
  private JList importList = new JList(importModel);
  private JToggleButton previewData = new JToggleButton("Preview");
  private JButton useData = new JButton("Use Data");
  private JTextField columnDepth = new JTextField("500");

  private JComboBox availableDatasets = new JComboBox();
  private XMLTag theModel;
  private EventHandler eh = new EventHandler();
  private int lockEvents = 0;
  
  public void addVoidFieldTag(String name) {
    XMLTag fieldTag = new XMLTag("field");
    fieldTag.setAttribute("name", name);
    fieldTag.setAttribute("source","Zero");
    int colSize=Integer.parseInt(theModel.getTag("initialconditions").getAttribute("size"));
    StringBuffer data = new StringBuffer();
    for (int i=0; i<colSize-1; i++) data.append("0,");
    data.append("0");
    fieldTag.setValue(data.toString());
    theModel.getTag("initialconditions").addTag(fieldTag);
  }
  
  public boolean checkTag(String name, boolean physics, boolean fix, StringBuffer errorString) {
    boolean ok = true;
    if (theModel.getTag("initialconditions").getTagWhere("field","@name",name)==null) {
      if (fix) {
        addVoidFieldTag(name);
        errorString.append("Created Zero "+name+" Profile\n");
      } else ok = false;
    }
    return ok;
  }
  
  public boolean greenLight(boolean fix) {
    lockEvents++;
    StringBuffer errorString = new StringBuffer();
    boolean ok = true;
    if (theModel.getTag("initialconditions")==null) {
      if (fix) {
        theModel.addTag(new XMLTag("initialconditions"));
        theModel.getTag("initialconditions").setAttribute("size","500");
        errorString.append("Creating Initial Conditions\n");
      } else ok = false;
    }
    if (ok) ok = checkTag("Density",true,fix,errorString);
    if (ok) ok = checkTag("Salinity",true,fix,errorString);
    if (ok) ok = checkTag("Temperature",true,fix,errorString);
    XMLTag[] chems = theModel.getTags("chemical");
    for (int i=0; i<chems.length; i++) {
      if (ok) ok = checkTag(chems[i].getValue("name"),false,fix,errorString);
    }
    if (ok) {
      if (theModel.getTag("initialconditions").getTag("turbocline")==null) {
        if (fix) {
          theModel.getTag("initialconditions").addTag(new XMLTag("turbocline","0.0"));
          errorString.append("Created Zero Initial Turbocline\n");
        }
      }
    }
    if (ok) {
      XMLTag[] fieldChems = theModel.getTag("initialconditions").getTags("field");
      for (int i=0; i<fieldChems.length; i++) {
        String chem = fieldChems[i].getAttribute("name");
        if (!((chem.equals("Density")) || (chem.equals("Salinity")) || (chem.equals("Temperature")))) {
          XMLTag findChem = theModel.getTagWhere("chemical","name",chem);
          if (findChem==null) {
            if (fix) {
              fieldChems[i].removeFromParent();
              errorString.append("Removed profile - "+chem+" not found in model.\n");
            } else {
              ok=false;
              i=fieldChems.length;
            }
          }
        }
      }
    }
    if (fix) {
      if (errorString.length()>5) {
        MessageBox.showMessage(errorString.toString(),this);
        vc2.unsaved(true);
      }
      populateDepthList();
      initialiseAvailableImports();
      depthEntryList.setSelectedIndex(0);
      initialiseTable();
      columnDepth.setText(theModel.getTag("initialconditions").getAttribute("size"));
      updateImportList();
    }
    lockEvents--;
    return ok;
  }
  
  public void initialiseAvailableImports() {
    availableDatasets.removeAllItems();
    availableDatasets.addItem("Levitus");
  }
  
  public void updateImportList() {
    lockEvents++;
    importModel.clear();
    String s = depthEntryList.getSelectedValue().toString();
    if ((s.equals("Temperature")) || (s.equals("Density")) || (s.equals("Salinity"))) {
      importModel.addElement("Density");
      importModel.addElement("Salinity");
      importModel.addElement("Temperature");
    } else if (theModel.getTagWhere("chemical","name",s)!=null) {
      importModel.addElement("Nitrate");
      importModel.addElement("Phosphate");
      importModel.addElement("Silicate");
    } else if (s.equals("Turbocline")) {
      importModel.addElement("Turbocline");
    }
    StringTools.setListItem(importList,s);
    updateButtons();
    lockEvents--;
  }
  
  
  public void populateDepthList() {
    depthEntriesModel.clear();
    ArrayList a = new ArrayList();
    a.add("Turbocline");
    a.add("Temperature");
    a.add("Salinity");
    a.add("Density");
    XMLTag[] chems = theModel.getTags("chemical");
    for (int i=0; i<chems.length; i++) {
      a.add(chems[i].getValue("name"));
    }
    while (a.size()>0) {
      String s = StringTools.nonSpace(a.get(0).toString()).toUpperCase();
      int best = 0;
      for (int i=1; i<a.size(); i++) {
        if (StringTools.nonSpace(a.get(i).toString()).toUpperCase().compareTo(s)<0) {
          s = StringTools.nonSpace(a.get(i).toString()).toUpperCase();
          best = i;
        }
      }
      depthEntriesModel.addElement(a.get(best).toString());
      a.remove(best);
    }
    a = null;
  }
  
  public void initialiseTable() {
    while (depthValTableModel.getRowCount()>0) depthValTableModel.removeRow(0);
    String s = depthEntryList.getSelectedValue().toString();
    if ((s.equals("Density")) || (s.equals("Temperature")) || (s.equals("Salinity")) || 
         (theModel.getTagWhere("chemical","name",s)!=null)) {
      StringBuffer sb = new StringBuffer(theModel.getTag("initialconditions").getTagWhere("field","@name",s).getValue());
      sb.append(",");
      int colSize = Integer.parseInt(theModel.getTag("initialconditions").getAttribute("size"));
      for (int i=0; i<colSize; i++) {
        double d = Double.parseDouble(sb.substring(0,sb.indexOf(",")));
        sb.delete(0,sb.indexOf(",")+1);
        depthValTableModel.addRow(new String[] {i+".0",String.valueOf(d)});
      }
            
    } else if (s.equals("Turbocline")) { 
      double d = Double.parseDouble(theModel.getValue("initialconditions/turbocline"));  
      depthValTableModel.addRow(new String[] {"Depth /m",String.valueOf(d)});
      
    }
  }
  
  public void showPreview() {
    lockEvents++;
    String pathName = "Levitus"+File.separator;
    //String dataSet = availableDatasets.getSelectedItem().toString();
    String importName = importList.getSelectedValue().toString();
    // Find date, longitude, latitude.
    
    GregorianCalendar gc = new GregorianCalendar();
    gc.setTimeInMillis(Long.parseLong(theModel.getValue("track/start")));
    int StartMonth = gc.get(GregorianCalendar.MONTH);
    int StartDayOfYear = gc.get(GregorianCalendar.DAY_OF_YEAR);
    double longitude = Double.parseDouble(theModel.getValue("track/longitude"));
    double latitude = Double.parseDouble(theModel.getValue("track/latitude"));
    double[] ProfileData = new double[0];
    boolean physics = ((importName.equals("Density")) || (importName.equals("Salinity")) || 
                       (importName.equals("Temperature")));
    boolean turbocline = (importName.equals("Turbocline"));
    if (turbocline) importName = "MixedLayerDepth";
    boolean chemistry = (importName.equals("Nitrate")) || (importName.equals("Phosphate")) ||
                        (importName.equals("Silicate"));
    if ((physics) || (turbocline)) {
      int SecondMonth = StartMonth;
      double[] Weights = new double[2];
      if ((StartDayOfYear * 2) > VEWUtilities.StandardMonthLength[StartMonth]) { // If StartDay is in 2nd half of month
        SecondMonth = (SecondMonth + 1) % 12; // Get the following month 
        Weights[0] = StartDayOfYear - (VEWUtilities.StandardMonthMiddle[StartMonth] - VEWUtilities.StandardMonthStart[StartMonth]);
        int MiddleGap = VEWUtilities.StandardMonthMiddle[SecondMonth] - VEWUtilities.StandardMonthMiddle[StartMonth];
        if (MiddleGap < 0) MiddleGap += 365;
        Weights[0] /= MiddleGap;
        Weights[1] = 1 - Weights[0];
      } else { // StartDay is in first half of month
        StartMonth = (StartMonth + 11) % 12; // Get previous month number
        Weights[1] = (VEWUtilities.StandardMonthMiddle[SecondMonth] - VEWUtilities.StandardMonthStart[SecondMonth]) - StartDayOfYear;
        int MiddleGap = VEWUtilities.StandardMonthMiddle[SecondMonth] - VEWUtilities.StandardMonthMiddle[StartMonth];
        if (MiddleGap < 0) MiddleGap += 365;
        Weights[1] /= MiddleGap;
        Weights[0] = 1 - Weights[1];
      }

      int layers=1;
      if (physics) layers=14;
      String theText = importName+ VEWUtilities.LongMonthName[StartMonth]+".vsi";
      ProfileData = FileIO.LoadProfile(latitude, longitude, 1, 1, layers, layers, pathName+theText);
      theText = importName+VEWUtilities.LongMonthName[SecondMonth]+".vsi";
      double[] ProfileData2 = FileIO.LoadProfile(latitude,longitude,1,1,layers,layers,pathName+theText);
      for (int i = 0; i < ProfileData.length; i++)
        ProfileData[i] = (ProfileData[i] * Weights[1]) + (ProfileData2[i] * Weights[0]);
    
    } else if (chemistry) { // Assume the annual variant.
      ProfileData = FileIO.LoadProfile(latitude,longitude, 1,1,14, 14, pathName+importName+".vsi");
    
    } 
    
    if ((chemistry) || (physics)) {
      int[] standardDepths = new int[] {0,10,20,30,50,75,100,125,150,200,250,300,400,500};
      for (int i=0; i<13; i++) {
        int depth1 = standardDepths[i];
        int depth2 = standardDepths[i+1];
        double value1 = ProfileData[i];
        double value2 = ProfileData[i+1];
        for (double j=depth1; j<depth2; j+=1.0) {
          double value = value1+((value2-value1)*((j-depth1)/(depth2-depth1)));
          depthValTableModel.setValueAt(String.valueOf(value),(int)j,1);
        }
      }
      dgp.paint(dgp.getGraphics());
    } else if (turbocline) {
      depthValTableModel.setValueAt(String.valueOf(ProfileData[0]),0,1);
    }
    lockEvents--;
    
  }
  
  public InitialConditionsPanel(VEWController2 parent, XMLTag model) {
    vc2 = parent;
    theModel=model;
    setLayout(new BorderLayout());
    JPanel tablePanel = new JPanel(new BorderLayout());
      JPanel graphTablePanel = new JPanel(new FlowLayout());
      JScrollPane dgpScroller = new JScrollPane(dgp);
        dgp.setPreferredSize(new Dimension(170,550));
        graphTablePanel.add(dgpScroller);
        JScrollPane depthValTableScroller = new JScrollPane(depthValTable);
        depthValTableScroller.setPreferredSize(new Dimension(200,550));
        graphTablePanel.add(depthValTableScroller);
        tablePanel.add(graphTablePanel,BorderLayout.CENTER);
      JPanel buttonPanel = new JPanel(new FlowLayout());
        
        buttonPanel.add(new JLabel("Size of column: "));
        buttonPanel.add(columnDepth);
        columnDepth.setPreferredSize(new Dimension(80,25));
        buttonPanel.add(new JLabel("metres"));
        buttonPanel.add(new JLabel("   "));
        buttonPanel.add(fillBetween);

        tablePanel.add(buttonPanel,BorderLayout.SOUTH);
    JPanel dataChoicePanel = new JPanel(new BorderLayout());
    dataChoicePanel.setPreferredSize(new Dimension(300,550));
      JPanel itemsToSet = new JPanel(new BorderLayout());
        itemsToSet.add(new JLabel("Items that require initialisation:-",JLabel.CENTER),BorderLayout.NORTH);
          JScrollPane depthItemsScroller = new JScrollPane(depthEntryList);
          depthItemsScroller.setPreferredSize(new Dimension(150,200));
          itemsToSet.add(depthItemsScroller,BorderLayout.CENTER);
      dataChoicePanel.add(itemsToSet,BorderLayout.NORTH);
      JPanel availableData = new JPanel(new BorderLayout());
        availableData.setPreferredSize(new Dimension(300,550));
        availableData.add(new JLabel("Data sets available for import:-",JLabel.CENTER),BorderLayout.NORTH);
        JPanel importBits = new JPanel(new BorderLayout());
        importBits.add(availableDatasets,BorderLayout.NORTH);
        JScrollPane importScroller = new JScrollPane(importList);
        importBits.add(importScroller,BorderLayout.CENTER);
        availableData.add(importBits,BorderLayout.CENTER);
        JPanel importButtonHolder = new JPanel(new BorderLayout());
          JPanel importButtons = new JPanel(new GridLayout(2,1));
            importButtons.add(previewData,BorderLayout.SOUTH);
            importButtons.add(useData,BorderLayout.NORTH);
            importButtonHolder.add(importButtons,BorderLayout.SOUTH);
          availableData.add(importButtonHolder,BorderLayout.EAST);
      dataChoicePanel.add(itemsToSet,BorderLayout.NORTH);
      dataChoicePanel.add(availableData,BorderLayout.WEST);
    JPanel spacer = new JPanel();
    spacer.setPreferredSize(new Dimension(10,550));
    add(spacer,BorderLayout.CENTER);
    add(dataChoicePanel,BorderLayout.WEST);
    add(tablePanel,BorderLayout.EAST);
    depthValTableModel.setColumnCount(2);
    depthValTableModel.setColumnIdentifiers(new String[] {"Depth /m","Value"});
    depthEntryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    importList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);    
    
    columnDepth.addKeyListener(eh);
    columnDepth.addFocusListener(eh);
    depthEntryList.addListSelectionListener(eh);
    importList.addListSelectionListener(eh);
    depthValTableModel.addTableModelListener(eh);
    depthValTable.addMouseListener(eh);
    fillBetween.addActionListener(eh);
    availableDatasets.addActionListener(eh);
    previewData.addActionListener(eh);
    useData.addActionListener(eh);
    fillBetween.setEnabled(false);
    previewData.setEnabled(false);
    
  }
  
  public void updateButtons() {
    fillBetween.setEnabled(depthValTable.getSelectedRows().length>1);
    previewData.setEnabled(importList.getSelectedIndices().length>0);
  }
  
  public void updateData() {
    String name = depthEntryList.getSelectedValue().toString();
    if (name.equals("Turbocline"))
      theModel.getTag("initialconditions").setValue("turbocline",depthValTable.getValueAt(0,1).toString());
      
    else {
      XMLTag theField = theModel.getTag("initialconditions").getTagWhere("field","@name",name);
      StringBuffer data = new StringBuffer();
      int colSize = Integer.parseInt(theModel.getTag("initialconditions").getAttribute("size"));
      for (int i=0; i<colSize-1; i++) {
        data.append(depthValTable.getValueAt(i,1).toString());
        data.append(',');
      }
      data.append(depthValTable.getValueAt(499,1).toString());
      theField.setValue(data.toString());
    }
    vc2.unsaved(false);    
  }
    
  public void updateColDepth() {
    int colSize=500;
    try { 
      colSize = Integer.parseInt(columnDepth.getText());
    } catch (Exception ex) {}
    if (colSize<500) {
      colSize=500;
      columnDepth.setText("500");
    }
    theModel.getTag("initialconditions").setAttribute("size",String.valueOf(colSize));
    int i = depthValTable.getRowCount();
    while (colSize>i) {
      depthValTableModel.addRow(new String[] {String.valueOf(i)+".0",depthValTable.getValueAt(i-1,1).toString()});
      i++;
    }
    while (i>colSize) {
      depthValTableModel.removeRow(i-1);
      i--;
    }
    XMLTag[] fields = theModel.getTag("initialconditions").getTags("field");
    for (i=0; i<fields.length; i++) {
      StringBuffer data = new StringBuffer(fields[i].getValue());
      double[] vals = new double[colSize];
      int j=0;
      while (data.length()>0) {
        if (data.indexOf(",")>=0) {
          if (j<vals.length) vals[j] = Double.parseDouble(data.substring(0,data.indexOf(",")));
          data.delete(0,data.indexOf(",")+1);
        } else {
          if (j<vals.length) vals[j] = Double.parseDouble(data.toString());
          data.delete(0,data.length());
        }
        j++;
      }
      for (int k=j; k<vals.length; k++) vals[k]=vals[j-1];
      for (int k=0; k<vals.length; k++) {
        if (k<vals.length-1) data.append(String.valueOf(vals[k])+",");
        else data.append(String.valueOf(vals[k]));
      }
      fields[i].setValue(data.toString());
    }
    updateData();
    if (dgp.getGraphics()!=null) dgp.paint(dgp.getGraphics());
    vc2.unsaved(true);
  }
  
  
  
  class EventHandler implements ActionListener, ListSelectionListener, TableModelListener, MouseListener, FocusListener, KeyListener {
    public EventHandler() {}

    public void actionPerformed(ActionEvent e) {
      if (e.getSource()==fillBetween) {
        lockEvents++;
        int[] selection = depthValTable.getSelectedRows();
        int max = selection[0];
        int min = selection[0];
        for (int i=0; i<selection.length; i++) {
          if (selection[i]>max) max = selection[i];
          if (selection[i]<min) min = selection[i];
        }
        double val1 = Double.parseDouble(depthValTableModel.getValueAt(min,1).toString());
        double val2 = Double.parseDouble(depthValTableModel.getValueAt(max,1).toString());        
        double depth1 = Double.parseDouble(depthValTableModel.getValueAt(min,0).toString());
        double depth2 = Double.parseDouble(depthValTableModel.getValueAt(max,0).toString());
        for (int i=min+1; i<=max-1; i++) {
          double instdepth = Double.parseDouble(depthValTableModel.getValueAt(i,0).toString());
          double newVal = val1+(((instdepth-depth1)/(depth2-depth1))*(val2-val1));
          depthValTableModel.setValueAt(String.valueOf(newVal),i,1);
        }
        lockEvents--;
        updateData();
        dgp.paint(dgp.getGraphics());
      
      } else if ((e.getSource()==availableDatasets) && (lockEvents==0)) {
        lockEvents++;
        updateImportList();
        lockEvents--;
        
      } else if ((e.getSource()==previewData) && (lockEvents==0)) {
        lockEvents++;
        boolean previewing = previewData.isSelected();
        depthValTable.setEnabled(!previewing);
        if (previewing) showPreview();
        else {
          initialiseTable();
          dgp.paint(dgp.getGraphics());
        }
        lockEvents--;
     
      } else if (e.getSource()==useData) {
        lockEvents++;
        showPreview();
        updateData();
        previewData.setSelected(false);
        depthValTable.setEnabled(true);
        lockEvents--;
        
      }
    }
    
    public void valueChanged(ListSelectionEvent e) {
      if ((e.getSource()==depthEntryList) && (lockEvents==0)) {
        lockEvents++;
        // Check for unsaved changes
        initialiseTable();
        updateImportList();
        previewData.setSelected(false);
        lockEvents--;
        dgp.paint(dgp.getGraphics());
      
      } else if ((e.getSource()==importList) && (lockEvents==0)) {
        lockEvents++;
        if (previewData.isSelected()) {
          showPreview();
        }
        updateButtons();
        lockEvents--;
      }
    }

    public void tableChanged(TableModelEvent e) {
      if ((e.getSource()==depthValTableModel) && (lockEvents==0)) {
        lockEvents++;
        updateData();
        dgp.paint(dgp.getGraphics());
        lockEvents--;
      }
    }

    public void mouseClicked(MouseEvent e) {
      if (e.getSource()==depthValTable) updateButtons();
    }

    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}

    public void focusGained(FocusEvent e) {}

    public void focusLost(FocusEvent e) {
      if ((e.getSource()==columnDepth) && (lockEvents==0)) {
        lockEvents++;
        updateColDepth();
        lockEvents--;
      }
    }

    public void keyPressed(KeyEvent e) {}

    public void keyReleased(KeyEvent e) {
      if (e.getKeyCode()==KeyEvent.VK_ENTER) {
        lockEvents++;
        updateColDepth();
        lockEvents--;
      }

      
    }

    public void keyTyped(KeyEvent e) {}
  }
  
  class DepthGraphPanel extends JPanel {
    Font smallFont = new Font("Arial",Font.PLAIN,9);
    public void paint(Graphics g) {
      
      g.setColor(getBackground());
      g.fillRect(0,0,181,551);
      g.setColor(Color.WHITE);
      g.fillRect(30,30,120,500);
      g.setColor(Color.black);
      g.drawLine(20,30,20,530);
      g.setFont(smallFont);
      int colSize = Integer.parseInt(theModel.getTag("initialconditions").getAttribute("size"));
      float step = colSize/20f; 
      for (float i=0; i<=colSize; i+=step) {
        int pix = (int) ((i/colSize*1.0f)*500);
        g.drawLine(18,30+pix,22,30+pix);
        int width = g.getFontMetrics(smallFont).stringWidth(String.valueOf((int)i));
        g.drawString(String.valueOf((int)i),16-width,33+pix);
      }
      g.setColor(new Color(128,0,0));
      if (depthValTableModel.getRowCount()>0) {      
        if (!depthValTableModel.getValueAt(0,0).toString().equals("Depth /m")) {
          int rows = depthValTableModel.getRowCount();
          double depth, val;
          double max=0;
          double min=0;
          int lastX=0;
          int lastY=0;
          int newX=0;
          int newY=0;
          for (int i=0; i<rows; i++) {
            val = Double.parseDouble(depthValTableModel.getValueAt(i,1).toString());
            if (i==0) { max=val; min = val; }
            else {
              if (val>max) max=val;
              if (val<min) min=val;
            }
          }
          for (int i=0; i<rows; i++) {
            depth = Double.parseDouble(depthValTableModel.getValueAt(i,0).toString());
            val = Double.parseDouble(depthValTableModel.getValueAt(i,1).toString());
            if (i==0) {
              lastX= (int) (((val-min)/(max-min))*120);
              lastY = (int) (500*((depth)/(1.0f*colSize)));
            } else {
              newX= (int) (((val-min)/(max-min))*120);
              newY = (int) (500*((depth)/(1.0f*colSize)));
              g.drawLine(30+lastX,30+lastY,30+newX,30+newY);
              lastX=newX;
              lastY=newY;
            }
          }
        } else {
          double turboDepth = Double.parseDouble(depthValTableModel.getValueAt(0,1).toString());
          turboDepth = (int) (500*(turboDepth/(1.0f*colSize)));
          g.drawLine(30,30+(int)turboDepth,149,30+(int)turboDepth);
        }
      }
    }
    
    public DepthGraphPanel() {
      super();
    }
  }
  
  class DepthTableModel extends DefaultTableModel {
    public boolean isCellEditable(int row, int col) { return (col==1); }
  }
  
}
>>>>>>> 76aa07461566a5976980e6696204781271955163
