package plia.core;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import plia.core.event.TouchEvent;
import plia.core.scene.Button;
import plia.core.scene.Camera;
import plia.core.scene.Group;
import plia.core.scene.Light;
//import plia.core.scene.MeshTerrain;
import plia.core.scene.PlaneCollider;
import plia.core.scene.Scene;
import plia.core.scene.SkyBox;
import plia.core.scene.SkyDome;
import plia.core.scene.SphereCollider;
import plia.core.scene.Sprite;
import plia.core.scene.Terrain;
//import plia.core.scene.geometry.Geometry;
import plia.core.scene.shading.Color3;
import plia.core.scene.shading.Color4;
import plia.core.scene.shading.Shader;
import plia.core.scene.shading.Texture2D;
import plia.math.Vector2;
import plia.math.Vector3;
import plia.math.Vector4;

import android.app.Activity;
//import android.graphics.Bitmap;
//import android.graphics.Canvas;
//import android.graphics.Paint;
//import android.graphics.Rect;
//import android.graphics.Typeface;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
//import android.opengl.GLUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

public abstract class Game extends Activity implements IFramework
{
	private GameObjectManager gameObjectManager;
	private AnimationPlayer animationPlayer;
	private TouchEventManager touchEventManager;
	private GameTime gameTime;
	
//	private Debug debug;
	
	private GLSurfaceView glSurfaceView;

	private boolean isResumed = false;
	private boolean isStarted = false;
	
//	private boolean isFirst = true;
	
	private static Game mainGame = null;
	
	public static boolean enabledDebug = false;

	@Override
	protected final void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		instance = this;
		
		this.gameObjectManager = GameObjectManager.getInstance();
		this.animationPlayer = AnimationPlayer.getInstance();
		
		this.touchEventManager = TouchEventManager.getInstance();

		this.gameTime = GameTime.getInstance();
		
		if(mainGame == null)
		{
			Scene.setMainCamera(new Camera("Main Camera"));
			
			Screen.w = getWindowManager().getDefaultDisplay().getWidth();
			Screen.h = getWindowManager().getDefaultDisplay().getHeight();
			
			
			this.gameObjectManager.setContext(this);
			this.gameObjectManager.initialize();

			mainGame = this;
//			this.debug = Debug.getInstance();
		}
		
		System.gc();

		this.onInitialize(savedInstanceState);
		this.glSurfaceView = new GLSurfaceView(this);
		this.glSurfaceView.setEGLContextClientVersion(2);
		this.glSurfaceView.setRenderer(new GLRenderer());
		this.setContentView(glSurfaceView);
	}
	
	private void checkStart()
	{
		if(isStarted)
		{
			Log.e("Game", "On Start");
			Shader.warmUpAllShader();
			gameObjectManager.start();
			isStarted = false;
		}
	}

	private void checkResume()
	{
		if(isResumed)
		{
			Log.e("Game", "On Resume");
			if(currentScene != null)
			{
				currentScene.initialize();
			}
			gameObjectManager.resume();
			isResumed = false;
		}
	}

	private void update()
	{
		gameObjectManager.update();
		touchEventManager.update();
		animationPlayer.update();
		gameTime.update();
		
		if(currentScene != null)
		{
			currentScene.update();
		}
		
		onUpdate();
	}
	
	private void draw()
	{
		if(currentScene != null)
		{
			currentScene.drawScene();
		}
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		this.gameTime.start();
		this.isStarted = true;
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		this.glSurfaceView.onPause();
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		this.glSurfaceView.onResume();
		this.isResumed = true;
	}
	
	@Override
	protected void onStop()
	{
		super.onStop();
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if(currentScene != null)
		{
			currentScene.onDestroy();
		}
		
		if(mainGame == this)
		{
			mainGame = null;
			isExited = false;
			this.gameObjectManager.destroy();
			this.touchEventManager.removeAll();
			
			this.touchEventManager.destroy();
			this.animationPlayer.destroy();
			
			this.animationPlayer = null;
			this.gameObjectManager = null;
			this.touchEventManager = null;
		}
		
		instance = null;
		System.gc();
	}
	
	@Override
	public final boolean onTouchEvent(MotionEvent event)
	{
		int action = TouchEvent.ACTION_NONE;
		
		switch(event.getAction())
		{
			case MotionEvent.ACTION_DOWN : action = TouchEvent.ACTION_DOWN; break;
			case MotionEvent.ACTION_MOVE : action = TouchEvent.ACTION_DRAG; break;
			case MotionEvent.ACTION_UP : action = TouchEvent.ACTION_UP; break;
		}
		
		float x = event.getX() / Screen.getWidth();
		float y = event.getY() / Screen.getHeight();
		
		onTouchEvent(action, x, y);
		
		if(currentScene != null)
		{
			currentScene.onTouchEvent(action, x, y);
		}
		
		touchEventManager.onTouchEvent(action, x, y);

		return true;
	}
	
	public void onTouchEvent(int action, float x, float y)
	{
		
	}
	
	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		Log.e("State", "Back");
	}
	
	private class GLRenderer implements Renderer
	{
		public void onDrawFrame(GL10 gl)
		{
//			Log.e("FPS", (1000f / GameTime.getElapsedGameTime().getMilliseconds())+"");

			checkStart();
//			checkInit();
			checkResume();

			if(!isResumed)
			{
				if(!isExited)
				{
					GLES20.glViewport(0, 0, Screen.getWidth(), Screen.getHeight());
					GLES20.glClearColor(0.3f, 0.6f, 0.9f, 1);
					GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
					
					update();
					
					draw();
				}
				else
				{
					onBackPressed();
				}
			}
		}

		public void onSurfaceChanged(GL10 gl, int width, int height)
		{
			Screen.w = width;
			Screen.h = height;
			
			Log.e("Screen", Screen.getWidth()+", "+Screen.getHeight());
			
			Scene.onSurfaceChanged();
		}

		public void onSurfaceCreated(GL10 gl, EGLConfig config)
		{

		}
		
	}

	private Scene currentScene;
	
	private static Game instance;
	static Game getInstance()
	{
		return instance;
	}

	public void setScene(Scene scene)
	{
		if(currentScene != scene)
		{
			currentScene = scene;
			isResumed = true;
			isStarted = true;
			
//			this.glSurfaceView = new GLSurfaceView(this);
//			this.glSurfaceView.setEGLContextClientVersion(2);
//			this.glSurfaceView.setRenderer(new GLRenderer());
//			this.setContentView(glSurfaceView);
//			
//			
		}
	}
	
	public Scene getScene()
	{
		return currentScene;
	}
	
	///
	public void log(Object value)
	{
		Log.d("Plia Framework", value + "");
	}
	
	public void err(Object value)
	{
		Log.e("Error", value + "");
	}
	
	public void print(Object value)
	{
		Log.println(Log.ASSERT, "", value + "");
	}
	
	private static boolean isExited = false;
	
	public static final void exit()
	{
		isExited = true;
	}
	
	///
	public static final Group model(String fbx_path)
	{
		return GameObjectManager.loadModel(fbx_path);
	}
	
	public static final Terrain terrain(String heightmapSrc, int maxHeight, int scale)
	{
		return GameObjectManager.createTerrain(heightmapSrc, maxHeight, scale);
	}
	
	public static final Terrain terrain(String heightmapSrc, String diffusemapSrc, int maxHeight, int scale)
	{
		Terrain t = GameObjectManager.createTerrain(heightmapSrc, maxHeight, scale);
		t.setBaseTexture(GameObjectManager.loadTexture2D(diffusemapSrc));
		
		return t;
	}
	
	public static final Terrain terrain(String heightmapSrc, String diffusemapSrc, int maxHeight, int scale, int segment)
	{
		Terrain t = GameObjectManager.createStaticTerrain(heightmapSrc, maxHeight, scale, segment);
		t.setBaseTexture(GameObjectManager.loadTexture2D(diffusemapSrc));
		
		return t;
	}
//	
//	public static final MeshTerrain terrain(String fbx_path, String heightmapSrc, String normalmapSrc)
//	{
//		Group mdl = model(fbx_path);
//		
//		Texture2D heightMap = tex2D(heightmapSrc);
//		Texture2D normalMap = tex2D(normalmapSrc);
//		
//		Geometry geo = mdl.asModel().getGeometry();
//		
//		float maxHeight = geo.getMax().z;
//
//		Vector3 min = geo.getMin();
//		Vector3 max = geo.getMax();
//		
//		Vector3 size = Vector3.subtract(max, min);
//		
//		MeshTerrain terr = new MeshTerrain(mdl, heightMap, normalMap, (int)maxHeight, (int)size.y);
////		terr.getTerrainModel().asModel().getMaterial().setBaseTexture(normalMap);
//		return terr;
//	}
	
	public static final Light directionalLight(float intensity)
	{
		Light light = new Light();
		light.setIntensity(intensity);
		return light;
	}
	
	public static final Light directionalLight(float forwardX, float forwardY, float forwardZ)
	{
		Light light = new Light();
		light.setForward(forwardX, forwardY, forwardZ);

		return light;
	}
	
	public static final Light directionalLight(float forwardX, float forwardY, float forwardZ, float intensity)
	{
		Light light = new Light();
		light.setForward(forwardX, forwardY, forwardZ);
		light.setIntensity(intensity);
		
		return light;
	}
	
	public static final Light directionalLight(float forwardX, float forwardY, float forwardZ, float red, float green, float blue)
	{
		Light light = new Light();
		light.setForward(forwardX, forwardY, forwardZ);
		light.setColor(red, green, blue);

		return light;
	}
	
	public static final Light directionalLight(float forwardX, float forwardY, float forwardZ, float intensity, float red, float green, float blue)
	{
		Light light = new Light();
		light.setForward(forwardX, forwardY, forwardZ);
		light.setColor(red, green, blue);
		light.setIntensity(intensity);
		
		return light;
	}
	
	public static final Light pointLight(float posX, float posY, float posZ, float range)
	{
		Light light = new Light();
		light.setLightType(Light.POINT_LIGHT);
		light.setRange(range);
		light.setPosition(posX, posY, posZ);

		return light;
	}
	
	public static final Light pointLight(float posX, float posY, float posZ, float range, float intensity)
	{
		Light light = new Light();
		light.setLightType(Light.POINT_LIGHT);
		light.setRange(range);
		light.setPosition(posX, posY, posZ);
		light.setIntensity(intensity);
		
		return light;
	}
	
	public static final Light pointLight(float posX, float posY, float posZ, float range, float red, float green, float blue)
	{
		Light light = new Light();
		light.setLightType(Light.POINT_LIGHT);
		light.setRange(range);
		light.setPosition(posX, posY, posZ);
		light.setColor(red, green, blue);

		return light;
	}
	
	public static final Light pointLight(float posX, float posY, float posZ, float range, float intensity, float red, float green, float blue)
	{
		Light light = new Light();
		light.setLightType(Light.POINT_LIGHT);
		light.setRange(range);
		light.setPosition(posX, posY, posZ);
		light.setColor(red, green, blue);
		light.setIntensity(intensity);
		
		return light;
	}
	
	public static final Camera camera(int projectionType)
	{
		Camera camera = new Camera();
		camera.setProjectionType(projectionType);
		
		return camera;
	}
	
	public static final Camera camera(int projectionType, float range)
	{
		Camera camera = new Camera();
		camera.setProjectionType(projectionType);
		camera.setRange(range);
		
		return camera;
	}
	
	public static final Camera camera(int projectionType, float posX, float posY, float posZ, float range)
	{
		Camera camera = new Camera();
		camera.setProjectionType(projectionType);
		camera.setPosition(posX, posY, posZ);
		camera.setRange(range);
		
		return camera;
	}
	
	public static final Camera camera(int projectionType, float posX, float posY, float posZ, float targetX, float targetY, float targetZ, float range)
	{
		Camera camera = new Camera();
		camera.setProjectionType(projectionType);
		camera.setPosition(posX, posY, posZ);
		camera.setLookAt(new Vector3(targetX, targetY, targetZ));
		camera.setRange(range);
		
		return camera;
	}
	
	public static final SkyDome skydome(String textureSrc)
	{
		SkyDome dome = new SkyDome();
		dome.setTexture(tex2D(textureSrc));
		
		return dome;
	}
	
	public static final SkyBox skybox(String src)
	{
		int indexOfDot = src.lastIndexOf(".");
		
		String s1 = src.substring(0, indexOfDot);
		String s2 = src.substring(indexOfDot, src.length());
		
		String frontSrc  = s1+"_front"+s2;
		String backSrc 	 = s1+"_back"+s2;
		String leftSrc 	 = s1+"_left"+s2;
		String rightSrc  = s1+"_right"+s2;
		String topSrc 	 = s1+"_top"+s2;
		String bottomSrc = s1+"_bottom"+s2;
		
		Log.e("", frontSrc+", "+backSrc+", "+leftSrc+", "+rightSrc+", "+topSrc+", "+bottomSrc);
		
		
		return new SkyBox(tex2D(frontSrc), tex2D(backSrc), tex2D(leftSrc), tex2D(rightSrc), tex2D(topSrc), tex2D(bottomSrc));
	}
	
	public static final SkyBox skyBox(String frontSrc, String backSrc, String leftSrc, String rightSrc, String topSrc, String bottomSrc)
	{
		return new SkyBox(tex2D(frontSrc), tex2D(backSrc), tex2D(leftSrc), tex2D(rightSrc), tex2D(topSrc), tex2D(bottomSrc));
	}
	
	public static final Sprite sprite(String imgSrc)
	{
		return GameObjectManager.createSprite(imgSrc);
	}
	
	public static final Sprite sprite(String imgSrc, int frame)
	{
		Sprite sprite = new Sprite();
		sprite.setImageSrc(GameObjectManager.loadTexture2D(imgSrc), frame);
		
		return sprite;
	}
	
	public static final Button button()
	{
		return new Button();
	}
	
	public static final Button button(String imgSrc)
	{
		return GameObjectManager.createButton(imgSrc);
	}
	
	public static final Button button(String imgSrc, int frame)
	{
		Button sprite = new Button();
		sprite.setImageSrc(GameObjectManager.loadTexture2D(imgSrc), frame);
		
		return sprite;
	}
	
	public static final Texture2D tex2D(String path)
	{
		return GameObjectManager.loadTexture2D(path);
	}
	
	public static final Texture2D text(String text, float textSize, int textColor)
	{
		return GameObjectManager.createText(text, textSize, textColor);
		
//		Paint paint = new Paint();
//	    paint.setTextSize(textSize);
//	    paint.setColor(textColor);
//	    paint.setTextAlign(Paint.Align.CENTER);
//	    
//	    Rect bounds = new Rect();
//	    paint.getTextBounds("  "+text+"  ", 0, text.length()+4, bounds);
//	    
//	    int width = bounds.width();
//	    int height = bounds.height();
//	    
//	    int x = (width / 2);
//	    int y = Math.abs(bounds.top);
//
//	    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//	    Canvas canvas = new Canvas(bitmap);
//	    canvas.drawText(text, x, y, paint);
//
//		int[] tex = new int[1];
//		
//		GLES20.glGenTextures(1, tex, 0);
//		
//		// generate color texture
//		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex[0]);
//
//		// parameters
//		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
//		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
//		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
//		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
//
//		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
//		GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
//		
//		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
//		
//		int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
//	    bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
//	    
//	    Texture2D texture = new Texture2D(text, tex[0], pixels, bitmap.getWidth(), bitmap.getHeight());
//	    
//	    return texture;
	}
	
	public static final PlaneCollider collider(float upX, float upY, float upZ, float scaleX, float scaleY, float posX, float posY, float posZ)
	{
		PlaneCollider boundingPlane = new PlaneCollider();
		boundingPlane.setScale(scaleX, scaleY, 0);
		boundingPlane.setUp(upX, upY, upZ);
		boundingPlane.setPosition(posX, posY, posZ);
		return boundingPlane;
	}
	
	public static final PlaneCollider collider(float upX, float upY, float upZ, float scaleX, float scaleY)
	{
		PlaneCollider boundingPlane = new PlaneCollider();
		boundingPlane.setScale(scaleX, scaleY, 0);
		boundingPlane.setUp(upX, upY, upZ);
		return boundingPlane;
	}
	
	public static final PlaneCollider collider(Vector3 up, Vector2 scale)
	{
		PlaneCollider boundingPlane = new PlaneCollider();
		boundingPlane.setScale(scale.x, scale.y, 0);
		boundingPlane.setUp(up);
		return boundingPlane;
	}
	
	public static final SphereCollider collider(float radius)
	{
		return new SphereCollider(radius);
	}
	
	//
	public static final Vector2 vec2()
	{
		return new Vector2();
	}
	
	public static final Vector2 vec2(float value)
	{
		return new Vector2(value, value);
	}
	
	public static final Vector2 vec2(float x, float y)
	{
		return new Vector2(x, y);
	}
	
	public static final Vector3 vec3()
	{
		return new Vector3();
	}
	
	public static final Vector3 vec3(float value)
	{
		return new Vector3(value, value, value);
	}
	
	public static final Vector3 vec3(float x, float y, float z)
	{
		return new Vector3(x, y, z);
	}
	
	public static final Vector4 vec4()
	{
		return new Vector4();
	}
	
	public static final Vector4 vec4(float value)
	{
		return new Vector4(value, value, value, value);
	}
	
	public static final Vector4 vec4(float x, float y, float z, float w)
	{
		return new Vector4(x, y, z, w);
	}
	
	public static final Color3 color(float r, float g, float b)
	{
		return new Color3(r, g, b);
	}
	
	public static final Color4 color(float r, float g, float b, float a)
	{
		return new Color4(r, g, b, a);
	}
}

interface IFramework
{
	void onInitialize(Bundle savedInstanceState);
	void onUpdate();
}

