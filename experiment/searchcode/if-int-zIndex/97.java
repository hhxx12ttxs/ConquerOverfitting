package se.geoproject.atlas.ui.maprender;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import se.geoproject.atlas.R;
import se.geoproject.atlas.map.data.MapItem;
import se.geoproject.atlas.map.data.MapPresets;
import se.geoproject.atlas.map.data.MapRect;
import android.content.Context;
import android.opengl.GLSurfaceView;
/**
 * MapRenderer class, heavily based (one could say stolen) from OSMAndroid
 * http://code.google.com/p/osm-android/source/browse/trunk/OSMandroid/src/com/google/code/osmandroid/graphics/OsmMapRenderer.java
 * 
 * @author viktor
 */
public class MapRenderer implements GLSurfaceView.Renderer {
	
	public static MapTransformation transformation;
	
	private static HashMap<Integer, Integer> zLayout;
    private static final int MAX_NODES = 2000;
    private static final int MAX_TEXTURES = 5;
    private static int[] openGlBugWorkaround = new int [MAX_NODES * 2];
	
    private static void setColor(GL10 gl, int itemType) {
    	switch (itemType) {
	        
	        case MapPresets.BOUNDARY_NATION:
	                gl.glColor4x(59110, 55255, 57054, 0);
	                break;
	                
	        case MapPresets.STREET_PRIMARY:
	                gl.glColor4x(60395, 27756, 38850, 0);
	                break;
	                
	        case MapPresets.STREET_SECONDARY:
	                gl.glColor4x(65021, 46260, 42148, 0);
	                break;
	
	        case MapPresets.STREET_TERTIARY:
	                gl.glColor4x(65536, 55000, 42662, 0);
	                break;
	        
	        case MapPresets.STREET_RESIDENTIAL:
	                gl.glColor4x(43652, 43652, 43652, 0);
	                break;
	                
	        case MapPresets.STREET_UNCLASSIFIED:    
	        case MapPresets.STREET_SERVICE:
	                gl.glColor4x(52652, 54250, 52250, 0);
	                break;
	
	        case MapPresets.STREET_MOTORWAY:
	        case MapPresets.STREET_MOTORWAY_LINK:
	                gl.glColor4x(0, 20000, 30000, 0);
	                break;
	
	        case MapPresets.STREET_TRUNK:
	        case MapPresets.STREET_TRUNK_LINK:
	                gl.glColor4x(43176, 56026, 43176, 0);
	                break;
	                
	        case MapPresets.STREET_FOOTWAY:
	        case MapPresets.STREET_CYCLEWAY:
	        case MapPresets.STREET_STAIRS:
	        		gl.glColor4x(52428, 29321, 29321, 0);
	        		break;
	                
	        case MapPresets.NATURAL_WATER:
	        case MapPresets.NATURAL_COASTLINE:
	                gl.glColor4x(46517, 53456, 53456, 0);
	                break;  
	                
	        case MapPresets.WATER_WAY:
	        		gl.glColor4x(0, 0, 0, 0);
	        		break;
	                
	        case MapPresets.LANDUSE_INDUSTRIAL:
	                gl.glColor4x(57054, 53713, 54741, 0);
	                break;
	
	        case MapPresets.LANDUSE_CEMETERY:
	                gl.glColor4x(43433, 51914, 44718, 0);
	                break;
	                
	        case MapPresets.LEISURE_PARK:
	                gl.glColor4x(46774, 64764, 46774, 0);
	                break;
	                
	        case MapPresets.LEISURE_STADIUM:
	                gl.glColor4x(13107, 52428, 39321, 0);
	                break;
	
	        case MapPresets.PLACE_CITY:
	                gl.glColor4x(52428, 39321, 39321, 0);
	                break;
	                
	        case MapPresets.BUILDING_YES:
	                gl.glColor4x(52428, 39321, 39321, 0);
	                break;
	
	        case MapPresets.ROUTE:
	        	gl.glColor4x(0, 0, 65536, 0);
	        case MapPresets.ROUTE_SUBITEM:
	                gl.glColor4x(0, 0, 65536, 0);
	                break;
	                
	        case MapPresets.PARKING_SPACE:
	        		gl.glColor4x(63744, 56832, 11264, 0);
	        		break;
	        		
	        case MapPresets.RAIL:
	        case MapPresets.RAIL_TRAM:
	        		gl.glColor4x(0, 0, 0, 0);
	        		break;
	                
	        default:
	                gl.glColor4x(0, 0, 0, 0);
    	}
    }
	private static void initZLayout() {
		int i = 1;

		zLayout.put(MapPresets.NATURAL_WATER,             i++);
		zLayout.put(MapPresets.NATURAL_COASTLINE, zLayout.get(MapPresets.NATURAL_WATER));
		zLayout.put(MapPresets.WATER_WAY, 				  i++);
		zLayout.put(MapPresets.STREET_UNCLASSIFIED,   	  i++);
		zLayout.put(MapPresets.STREET_SERVICE,            i++);
		zLayout.put(MapPresets.STREET_CYCLEWAY,			  i++);
		zLayout.put(MapPresets.STREET_FOOTWAY, 			  i++);
		zLayout.put(MapPresets.STREET_STAIRS, 			  i++);
		zLayout.put(MapPresets.STREET_RESIDENTIAL,        i++);
		zLayout.put(MapPresets.LEISURE_PARK,              i++);
		zLayout.put(MapPresets.LEISURE_STADIUM,           i++);
		zLayout.put(MapPresets.LANDUSE_CEMETERY,          i++);
		zLayout.put(MapPresets.LANDUSE_INDUSTRIAL,        i++);
		zLayout.put(MapPresets.STREET_TERTIARY,           i++);
		zLayout.put(MapPresets.STREET_SECONDARY,          i++);
		zLayout.put(MapPresets.STREET_PRIMARY,            i++);
		zLayout.put(MapPresets.STREET_TRUNK_LINK,         i++);
		zLayout.put(MapPresets.STREET_TRUNK,              i++);
		zLayout.put(MapPresets.STREET_MOTORWAY_LINK,  	  i++);
		zLayout.put(MapPresets.STREET_MOTORWAY,           i++);
		zLayout.put(MapPresets.RAIL, i++);
		zLayout.put(MapPresets.RAIL_TRAM, i++);
		zLayout.put(MapPresets.ROUTE,                     i++);
		zLayout.put(MapPresets.ROUTE_SUBITEM,             i++);
		zLayout.put(MapPresets.PLACE_VILLAGE,             i++);
		zLayout.put(MapPresets.PLACE_TOWN,                i++);  
		zLayout.put(MapPresets.PLACE_CITY,                i++);
	}
	
	private MapRect mapRect;
	private short zoomLevel;
	private boolean renderLabels;
	private LabelsTexture labelsTexture;
	private GLTextures textures;
	
	public MapRenderer(Context context) {
		this.renderLabels = true;
		zLayout = new HashMap<Integer, Integer> ();
		initZLayout();
		labelsTexture = new LabelsTexture();
		textures = new GLTextures(context, MAX_TEXTURES);
		this.textures.add(R.drawable.geonew); 		// Texture for geocache, that is not located
		this.textures.add(R.drawable.geolocated);   // Texture for located geocache
		this.textures.add(R.drawable.usernew);
		this.textures.add(R.drawable.waypoint);
		this.textures.add(R.drawable.parking);
		transformation = new MapTransformation();
		
	}
	
	public void setMap(MapRect mapRect, short zoomLevel, boolean renderLabels) {
		this.mapRect = mapRect;
		this.zoomLevel = zoomLevel;
		this.renderLabels = renderLabels;
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		
		ArrayList<MapItem> mapItemsLocal = mapRect.getMapItems();

        int minX = mapRect.minX;
        int maxX = mapRect.maxX;
        int minY = mapRect.minY;
        int maxY = mapRect.maxY;
        
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrthox(0, maxX - minX, 0, maxY - minY, 655360, -655360);
        
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glTranslatex(0, 0, -65536);
        
        labelsTexture.clearTexture(gl);
        
        
        for(MapItem item: mapItemsLocal) {
        	if(item.type == MapPresets.ICON) {
        		drawImage(gl, item.minX, item.maxX, item.minY, item.maxY, item, item.flags);
        		
        		continue;
        	}
        	
        	if(item.nodes == null) {
        		continue;
        	}
        	
        	int minLength = (5 - (zoomLevel - 6)) * 65536;
        	if(item.numNodes != 1 && (item.maxX - item.minX < minLength) &&
        				(item.maxY - item.minY < minLength)) {		// Item will not be seen
        		continue;
        	}
        	
        	int numItemNodes = item.numNodes > MAX_NODES ? MAX_NODES : item.numNodes; 
        	int count = numItemNodes * 2;
        	
        	ByteBuffer bb = ByteBuffer.allocateDirect(count * 4);
        	bb.order(ByteOrder.nativeOrder());
        	IntBuffer ib = bb.asIntBuffer();
        	for(int i = 0; i < count; i += 2) {
        		ib.put(item.nodes[i] - minX);
        		ib.put(item.nodes[i + 1] - minY);
        		openGlBugWorkaround[i] = item.nodes[i] - minX;
        		openGlBugWorkaround[i + 1] = item.nodes[i + 1] - minY;
        	}
        	ib.position(0);
        	
        	if(item.type == MapPresets.ROUTE || item.type == MapPresets.ROUTE_SUBITEM) {
        		gl.glLineWidthx(196608);
        	}
        	else {
        		int lineWidth = (zoomLevel - item.type / 1000 + 1);
        		gl.glLineWidthx(lineWidth < 8 ? lineWidth << 16 : 8 << 16);
        	}
        	
        	setColor(gl, item.type);
        	
        	int zIndex = 0;
        	Integer v = zLayout.get(item.type);
        	if(v != null) {
        		zIndex = v.intValue();
        	}
        	
        	gl.glEnable(GL10.GL_DEPTH_TEST);
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glDepthMask(true);
            gl.glDepthFunc(GL10.GL_LEQUAL); 
            gl.glPushMatrix();
            
            gl.glTranslatex(0, 0, -zIndex << 10);
            
            if(item.getShape() == MapItem.SHAPE_LINE) {
            	gl.glVertexPointer(2, GL10.GL_FIXED, 0, ib);
            	gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, numItemNodes);
            }
            else if (item.getShape() == MapItem.SHAPE_POLYGON) {
                gl.glVertexPointer(2, GL10.GL_FIXED, 0, ib);
                gl.glDrawArrays(GL10.GL_TRIANGLES, 0, numItemNodes);
            }
            
            gl.glPopMatrix();
            
            if(item.type == MapPresets.PARKING_SPACE) {
            	drawImage(gl, item.getCenter().x - 100, item.getCenter().x + 100, item.getCenter().y - 100,
            			  item.getCenter().y + 100, item, R.drawable.parking);
            }
            
            if(renderLabels) {
            	renderLabel(gl, item, mapRect, zoomLevel);
            }
        }
        
        labelsTexture.drawLabels(mapRect, zoomLevel);
        labelsTexture.draw(gl);
	}
	/**
	 * @param gl
	 * @param minX
	 * @param maxX
	 * @param minY
	 * @param maxY
	 * @param item
	 */
	private void drawImage(GL10 gl, int minX, int maxX, int minY, int maxY, MapItem item, int textureID) {
		if(textures.setTexture(gl, textureID)) {
			gl.glDisable(GL10.GL_DEPTH_TEST);
			gl.glEnable(GL10.GL_BLEND);
		    gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		    gl.glColor4x(0x10000, 0x10000, 0x10000, 0x10000);   
		    gl.glEnable(GL10.GL_TEXTURE_2D);

		    int itemMinX = transformation.worldToScreenX(mapRect.minX, mapRect.maxX, minX);
		    int itemMinY = transformation.worldToScreenY(mapRect.minY, mapRect.maxY, minY);
		    int itemMaxX = transformation.worldToScreenX(mapRect.minX, mapRect.maxX, maxX);
			int itemMaxY = transformation.worldToScreenY(mapRect.minY, mapRect.maxY, maxY);

			int[] crop = new int[4];
			crop[0] = 0;
			crop[1] = textures.getTextureSize().get(textureID).getTextureHeight();
			crop[2] = textures.getTextureSize().get(textureID).getTextureWidth();
			crop[3] = -(textures.getTextureSize().get(textureID).getTextureHeight());

			GL11 gl11 = (GL11) gl;
			
			gl11.glTexParameteriv(GL11.GL_TEXTURE_2D,
								  GL11Ext.GL_TEXTURE_CROP_RECT_OES, crop, 0);
			
			GL11Ext gl11Ext = (GL11Ext) gl;
			
			gl11Ext.glDrawTexiOES(itemMinX, itemMinY, 0, itemMaxX - itemMinX, itemMaxY - itemMinY);
			
			gl.glDisable(GL10.GL_BLEND);
			gl.glDisable(GL10.GL_TEXTURE_2D);
			gl.glEnable(GL10.GL_DEPTH_TEST);
		}
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		gl.glViewport(0, 0, width, height);
		transformation.windowHeight = height;
		transformation.windowWidth = width;
		labelsTexture.setDimensions(width, height);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glClearColorx(61937, 61166, 59624, 0);
		gl.glShadeModel(GL10.GL_SMOOTH);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		
		textures.loadTextures(gl);
	}
	
	public void renderLabel(GL10 gl, MapItem item, MapRect rect, int zoomLevel) {
		if(item.name == null || item.type == MapPresets.ROUTE) {
			return;
		}
		
		int weight = Math.round(getLabelWeight(zoomLevel, item.type) * 32768);
		if(Math.abs(item.maxX - item.minX) < weight && Math.abs(item.maxY - item.minY) < weight) {
			return;
		}
		
		labelsTexture.addItem(item);
	}
	
	public static float getLabelWeight(int zoomLevel, int itemType) {
		float weight = 100 << 16;
		switch(itemType){
        
	        case MapPresets.STREET_PRIMARY:
	                if (zoomLevel > 12) {
	                        weight = (float) (1.6 - (zoomLevel - 13) * 0.5);
	                }
	                break;
	                
	        case MapPresets.STREET_SECONDARY:
	                if (zoomLevel > 13) {
	                        weight = (float) (1 - (zoomLevel - 14) * 0.2);  
	                }
	                break;
	                
	        case MapPresets.STREET_TERTIARY:
	                if (zoomLevel > 14) {
	                        weight = (float) (1 - (zoomLevel - 15) * 0.1);
	                }
	                break;
	                
	        case MapPresets.STREET_RESIDENTIAL:
	                if (zoomLevel > 14) {
	                        weight = (float) (0.2 - (zoomLevel - 16) * 0.1);
	                }
	                break;
	                
	        case MapPresets.STREET_UNCLASSIFIED:
	        		if(zoomLevel > 14) {
	        			weight = (float) (0.2 - (zoomLevel - 16) * 0.1);
	        		}
	        		break;
	        		
	        case MapPresets.STREET_FOOTWAY:
	        		if(zoomLevel > 15) {
	        			weight = (float) (0.2 - (zoomLevel - 17) * 0.1);
	        		}
	        		break;
	                
	        case MapPresets.BUILDING_YES:
	        		if(zoomLevel > 14) {
	        			weight = 0;
	        		}
	        		break;
	                
	        case MapPresets.PLACE_CITY:
	                if (zoomLevel > 5 && zoomLevel < 12) {
	                        weight = 0;
	                }
	                break;
	        
	        case MapPresets.PLACE_TOWN:
	                if (zoomLevel > 9 && zoomLevel < 13) {
	                        weight = 0;
	                }
	                break;
	                
	        case MapPresets.PLACE_VILLAGE:
	                if (zoomLevel > 10 && zoomLevel < 14) {
	                        weight = 0;
	                }
	                break;
	                
	        case MapPresets.NATURAL_WATER:
	                if (zoomLevel >= 12 ) {
	                        weight = (float) (4 - (zoomLevel - 12) * 0.4);
	                }
	                break;
	        case MapPresets.LEISURE_PARK:
	        case MapPresets.LEISURE_STADIUM:
	                if (zoomLevel > 14 ) {
	                        weight = 0;
	                }
	                break;
	                
	        case MapPresets.WATER_WAY:
	        		if(zoomLevel > 13) {
	        			weight = 0;
	        		}
	        		break;
	                
	        default:
	                break;
		}
        
        return weight;
	}

}

