package org.geojme.generators.terrain;

import java.awt.image.DataBufferFloat;
import java.awt.image.RenderedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.jai.Interpolation;

import org.geojme.entities.StandaloneEtopo;
import org.geojme.interfaces.ISceneNode;

import org.geojme.interfaces.ISceneView;
import org.geojme.interfaces.ITerrain;
import org.geojme.jme.JMENode;
import org.geojme.jme.camera.FlyByCamAboveTerrain;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.processing.Operations;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.geomipmap.lodcalc.DistanceLodCalculator;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import java.util.concurrent.Callable;
import org.geojme.geodetic.GeodeticManager;
import org.geojme.geodetic.SpatialClassConverter;
import org.geojme.interfaces.GeodeticEntity;
import org.geojme.interfaces.ISceneNodeGenerator;
import org.geojme.jme.ISceneViewJME;
import org.geojme.jme.MultiSpatial;

public class ETOPOTerrainGenerator implements ISceneNodeGenerator {

	private final AssetManager _manager;
	private final SimpleApplication _application;
	private final ISceneView _sceneView;
	private StandaloneEtopo terrainEntity;
	private TerrainCamFrustumControl tcc;
	// TestMode
	private static final boolean ADD_TEST_TOOLS = true;

	public ETOPOTerrainGenerator(SimpleApplication application,
			AssetManager manager, ISceneView sceneView) {
		_manager = manager;
		_application = application;
		_sceneView = sceneView;
	}

	@Override
	public boolean canCreateSceneNode(Object entity) {
		return entity instanceof StandaloneEtopo;
	}

	@Override
	public ISceneNode createSceneNode(Object entity, LevelOfDetail dLevel) {

		if (_manager == null) {
			return null;
		}

		GeodeticManager geodeticManager = _sceneView.getGeodeticManager();
		terrainEntity = (StandaloneEtopo) entity;
		GridCoverageFactory factory = new GridCoverageFactory();

		String name = "GridCoverage";
		float[][] matrix = terrainEntity.getHeightMapMatrix();
		org.geotools.geometry.GeneralEnvelope env = new org.geotools.geometry.GeneralEnvelope(
				geodeticManager.getEntityCRS((GeodeticEntity) entity));
		createGridCoverage2DEnvelope(terrainEntity.getEnvelope(), env);
		GridCoverage2D coverage = factory.create(name, matrix, env);

		int patchSize = 65;
		int width = terrainEntity.getWidth();
		int height = terrainEntity.getHeight();

		/*
		 * double max = (double) Math.max(height, width); double totalSize =
		 * (double) Math.ceil(Math.log(max) / Math.log(2)); totalSize =
		 * (Math.pow(2, totalSize))*4;
		 */

		double totalSize = 512;
		float fillOutScaleX = (float) totalSize / (float) width;
		float fillOutScaleY = (float) totalSize / (float) height;

		GridCoverage2D finalCoverage = null;
		try {
			// Scale terrain to match GridCoverage2D dimensions to the totalSize
			// (a number in the power of 2).
			coverage = (GridCoverage2D) Operations.DEFAULT.scale(coverage,
					fillOutScaleX, fillOutScaleY, 0, 0);
			// Create projected GridCoverage2D
			finalCoverage = process(coverage, geodeticManager.getWorldCRS());
		} catch (Exception ex) {
			Logger.getLogger(ETOPOTerrainGenerator.class.getName()).log(
					Level.SEVERE, null, ex);
		}

		org.geotools.geometry.Envelope2D finalEnvelope = finalCoverage
				.getEnvelope2D();

		/*
		float scaleX = (float) finalEnvelope.getWidth();
		float scaleY = (float) finalEnvelope.getHeight();
		scaleX /= totalSize;
		scaleY /= totalSize;
*/
		Vector3f scale = new Vector3f( (float) (finalEnvelope.getWidth()/totalSize) , 1 , (float) (finalEnvelope.getHeight()/totalSize) );
		RenderedImage ri = finalCoverage.getRenderedImage();

		float[] data = ((DataBufferFloat) ri.getData().getDataBuffer())
				.getData();
		data = reverse(data);

		TerrainQuad terrain = (TerrainQuad) new TerrainQuad("ETOPO Terrain",
				patchSize, (int) totalSize + 1, data);
		Vector3f distortionScale = ((ISceneViewJME) _sceneView)
				.getGeodeticManagerJME().getDistortionScaling();		
		System.out.println(this+" Terrain distortion scale:"+distortionScale);
		distortionScale = distortionScale.mult(scale);
		terrain.setLocalScale(distortionScale);
/*
		TerrainLodControl control = new TerrainLodControl(terrain,
				_application.getCamera());

		// ok, create the terrain at the correct level of detail
		switch (dLevel) {
		case HIGH_DETAIL:
			control.setLodCalculator(new DistanceLodCalculator(patchSize,
					100000000));
			break;
		case MEDIUM_DETAIL:
			control.setLodCalculator(new DistanceLodCalculator(patchSize,
					80000000));
			break;
		case LOW_DETAIL:
			control.setLodCalculator(new DistanceLodCalculator(patchSize,
					60000000));
			break;
		}
		terrain.addControl(control);
*/
		// some local formatting
		if (ADD_TEST_TOOLS) {
			terrain.setMaterial(getTestMaterial());
		} else {
			terrain.setMaterial(getMaterial(-10000, -150, -10, 1000, 2000,
					(int) totalSize + 1));
		}

		terrain.addControl(((FlyByCamAboveTerrain) _application
				.getFlyByCamera()));

		tcc = new TerrainCamFrustumControl(_application.getCamera());

		terrain.addControl(tcc);
		JMENode res = new JMENode(terrain, true);
		res.setOriginalValue(
				SpatialClassConverter.Vector3fToCoordinate(scale, true, false), 0);
		return res;
	}

	public void makePowerOf2() {

	}

	private Material getMaterial(float h0, float h1, float h2, float h3,
			float h4, int terrainSize) {
		float grassScale = 16f;// 16;
		float dirtScale = 16f;// 16;
		float rockScale = 64f;// 64;

		Material mat_terrain = new Material(_manager,
				"Common/MatDefs/Terrain/HeightBasedTerrain.j3md");

		// DIRT texture
		Texture rock = _manager.loadTexture("Textures/Terrain/Rock2/rock.jpg");
		rock.setWrap(WrapMode.Repeat);

		Texture rock2 = _manager.loadTexture("Textures/Terrain/snow2.jpg");
		rock2.setWrap(WrapMode.Repeat);

		mat_terrain.setTexture("region1ColorMap", rock);
		mat_terrain.setVector3("region1", new Vector3f(h0, h1, rockScale));

		// ROCK texture
		Texture dirt = _manager.loadTexture("Textures/Terrain/splat/dirt.jpg");
		dirt.setWrap(WrapMode.Repeat);
		mat_terrain.setTexture("region2ColorMap", dirt);
		mat_terrain.setVector3("region2", new Vector3f(h1, h2, dirtScale));

		Texture grass = _manager
				.loadTexture("Textures/Terrain/splat/grass.jpg");
		grass.setWrap(WrapMode.Repeat);
		mat_terrain.setTexture("region3ColorMap", grass);
		mat_terrain.setVector3("region3", new Vector3f(h2, h3, grassScale));

		mat_terrain.setTexture("region4ColorMap", rock2);
		mat_terrain.setVector3("region4", new Vector3f(h3, h4, rockScale));

		mat_terrain.setTexture("slopeColorMap", rock);
		mat_terrain.setFloat("slopeTileFactor", .7f);

		mat_terrain.setFloat("terrainSize", (terrainSize - 1));
		return mat_terrain;
	}

	public Material getTestMaterial() {
		Material mat_terrain = new Material(_manager,
				"Common/MatDefs/Terrain/Terrain.j3md");
		// mat_terrain.setBoolean("useTriPlanarMapping", false);

		// GRASS texture
		Texture dirt = _manager.loadTexture("Textures/Terrain/splat/dirt.jpg");
		dirt.setWrap(WrapMode.Repeat);
		mat_terrain.setTexture("Tex1", dirt);
		mat_terrain.setFloat("Tex1Scale", 65);

		return mat_terrain;
	}

	@Override
	public void entityMoved(Object entity, ISceneNode object) {
		// ignore, terrain doesn't move!
		// TODO: this method may actually prove useful to trigger a load of new
		// terrain after a large pan/zoom
	}

	@Override
	public void entityStyled(Object entity, ISceneNode object) {
		// ignore, no action

		ITerrain terrainEntity = (ITerrain) entity;
		@SuppressWarnings("unused")
		boolean isVis = terrainEntity.getVisible();

		JMENode node = (JMENode) object;
		node.setVisible(isVis);
	}

	@Override
	public Coordinate getLocation(Object entity) {
		// operation not applicable to this entity type
		return null;
	}

	private int pInterpolation = 1;

	private GridCoverage2D process(GridCoverage2D inRaster,
			CoordinateReferenceSystem targetCrs) throws Exception {

		Interpolation interpolationType = null;
		if (pInterpolation == 1) {
			interpolationType = Interpolation
					.getInstance(Interpolation.INTERP_BILINEAR);
		} else if (pInterpolation == 2) {
			interpolationType = Interpolation
					.getInstance(Interpolation.INTERP_BICUBIC);
		} else if (pInterpolation == 3) {
			interpolationType = Interpolation
					.getInstance(Interpolation.INTERP_BICUBIC_2);
		} else {
			// default to nearest neighbour
			interpolationType = Interpolation
					.getInstance(Interpolation.INTERP_NEAREST);
		}
		GridCoverage2D outRaster = (GridCoverage2D) Operations.DEFAULT
				.resample(inRaster, targetCrs, null, interpolationType);
		return outRaster;
	}

	private void createGridCoverage2DEnvelope(Envelope source,
			org.geotools.geometry.GeneralEnvelope target) {
		target.setRange(0, source.getMinX(), source.getMaxX());
		target.setRange(1, source.getMinY(), source.getMaxY());
	}

	public static float[] reverse(float[] arr) {
		float[] tempArr = new float[arr.length];
		for (int i = 0; i < arr.length; i++) {
			tempArr[i] = arr[arr.length - i - 1];
		}
		return tempArr;
	}

	public void entityScaled(Object entity, final ISceneNode object) {
		_application.enqueue(new Callable<Object>() {
			public Object call() throws Exception {
				MultiSpatial ms = object.resolve(MultiSpatial.class);
				Vector3f originalScale = SpatialClassConverter
						.CoordinateToVector3f(object.getOriginalValue(0), true,
								false);
				Vector3f newScale = ((ISceneViewJME) _sceneView)
						.getGeodeticManagerJME().getUserScaling();
				
				Vector3f distortionScale = ((ISceneViewJME) _sceneView)
						.getGeodeticManagerJME().getDistortionScaling();
				System.out.println("Original scale:" + originalScale);
				System.out.println("New user set scale:" + newScale);
				System.out.println("Distortion scale:" + distortionScale);
				ms.get(0).setLocalScale(
						newScale.mult(distortionScale).mult(originalScale));
				return null;
			}
		});
	}

	public void entityReprojected(Object entity, ISceneNode object) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void entityBoundsAltered(Object entity, ISceneNode object) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
