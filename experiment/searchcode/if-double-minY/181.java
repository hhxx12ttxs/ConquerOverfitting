package se.geoproject.atlas.activities;

import java.util.ArrayList;
import java.util.HashMap;

import se.geoproject.atlas.R;
import se.geoproject.atlas.client.ServerConnection;
import se.geoproject.atlas.database.DBAdapter;
import se.geoproject.atlas.location.Geocache;
import se.geoproject.atlas.location.ShowDistance;
import se.geoproject.atlas.map.data.BoundingBox;
import se.geoproject.atlas.map.data.MapItem;
import se.geoproject.atlas.map.data.MapPresets;
import se.geoproject.atlas.map.data.Node;
import se.geoproject.atlas.map.data.Tile;
import se.geoproject.atlas.ui.maprender.MapViewGL;
import se.geoproject.atlas.utils.OnItemClickedListener;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

/**
 * 
 * @author anna, eva, nahid, peter, retta, viktor
 *
 */
public class MapActivity extends LocationAwareActivity {
	
	private static final int GEOCACHE_IMAGE_SIZE = 200;
	private static final int USER_IMAGE_SIZE = 300;
	private static final int WAYPOINT_IMAGE_SIZE = 120;
	private static final int REQUEST_GEOCACHES = 1;
	private static final int RESULT_CALCULATE_ROUTE = 2;
	private static final int LOCATE_GEOCACHE = 3;

	public static int width;
	public static int height;
	private MapViewGL mvgl;
	private MapItem user;
	
	private Location lastRouteUpdateLoc = null;
	private Location currentLocation = null;
	private MapItem currentRouteTo = null;
	
	private MapItem currentlySelected;
	private DBAdapter db;

	private Thread dataFetcher = null;
	private double nextStartY = Double.NaN;
	private double nextEndY = Double.NaN;
	private double nextStartX = Double.NaN;
	private double nextEndX = Double.NaN;
	private ArrayList<PathFindingPair> routesLeftToFetch = new ArrayList<PathFindingPair> ();
	private boolean pathIsUpdating = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		FrameLayout r = (FrameLayout) this.getLayoutInflater().inflate(R.layout.map, null);

		setContentView(r);
		
		mvgl = (MapViewGL) findViewById(R.id.mapgl);
		
		// Gets bounding box for current area from SplashScreen
		int[] box = getIntent().getIntArrayExtra("box"); 
		BoundingBox bb = new BoundingBox();
		bb.minY = box[0];
		bb.minX = box[1];
		bb.maxY = box[2];
		bb.maxX = box[3];
		
		// Sets received map area
		mvgl.setMapArea(bb, 15);
		
		// Gets list of tiles for current area from SplashScreen
		ArrayList<Tile> tileList = getIntent().getParcelableArrayListExtra("tiles");
		
		// Sets list of tiles received from SplashScreen to map view
		mvgl.setTiles(tileList);
				
		mvgl.setListener(new OnItemClickedListener () {
			@Override
			public void onClick(MapItem item) {
				if (item.name != null && !item.name.equals("")) {
					currentlySelected = item;
					MapActivity.this.openContextMenu(mvgl);
				}
				
			}

			@Override
			public void onClick(int x, int y) {
				MapItem item = new MapItem();
				item.name = "Lat:" + Double.toString(y / 1000000.0) + " Lon:" + Double.toString(x / 1000000.0);
				item.maxX = x + 5;
				item.maxY = y + 5;
				item.minX = x - 5;
				item.minY = y - 5;
				item.numNodes = 1;
				item.nodes = new int[2];
				item.nodes[0] = x;
				item.nodes[1] = y;
				
				currentlySelected = item;
				MapActivity.this.openContextMenu(mvgl);
			}			
		});
		
		this.registerForContextMenu(mvgl);

		user = new MapItem();
		user.type = MapPresets.ICON;
		user.flags = R.drawable.usernew;
		
		mvgl.getIconList().clear();
		mvgl.getIconList().add(user);		
		
		// Draws geocaches on the map
		showGeocache();

		Button zoomIn = (Button) findViewById(R.id.zoom_in);
		zoomIn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mvgl.zoomIn();
			}
		});
		
		Button zoomOut = (Button) findViewById(R.id.zoom_out);
		zoomOut.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mvgl.zoomOut();
			}
		});
	}

	@Override
	public void onLocationChanged(Location location) {
		
		int mercX = (int) (location.getLongitude() * 1000000);
		int mercY = (int) (location.getLatitude() * 1000000);
		
		user.maxX = mercX + USER_IMAGE_SIZE; 
		user.minX = mercX - USER_IMAGE_SIZE;
		user.maxY = mercY + USER_IMAGE_SIZE;
		user.minY = mercY - USER_IMAGE_SIZE;
		currentLocation = location;
		
		if(lastRouteUpdateLoc != null) {
			if(ShowDistance.calculateDistance(location.getLatitude(), lastRouteUpdateLoc.getLatitude(),
											  location.getLongitude(), lastRouteUpdateLoc.getLongitude()) > 0.02) {
				pathIsUpdating = true;
				showRoute(user, currentRouteTo);
				lastRouteUpdateLoc = location;
			}
		}		
		mvgl.renderScene();
	}

	private MapItem addGeocache(Geocache geocache) {
		
		// Latitude and longitude for each geocache
		int mercX = (int) (geocache.getLongitude() * 1000000);
		int mercY = (int) (geocache.getLatitude() * 1000000);
		// Nodes for MapItem
		int[] nodes = new int[2];
		nodes[0] = mercX;
		nodes[1] = mercY;
		// Creates new MapItem, sets type and nodes
		MapItem icon = new MapItem();
		icon.type = MapPresets.ICON;
		icon.nodes = nodes;
		// Checks if geocache is already found or not
		if (geocache.isFound() == 1){
			icon.flags = R.drawable.geolocated; // Icon of located geocache
		}else{
			icon.flags = R.drawable.geonew;		// Icon of geocache
		}
		// Sets bounding box
		icon.maxX = mercX + GEOCACHE_IMAGE_SIZE;
		icon.minX = mercX - GEOCACHE_IMAGE_SIZE;
		icon.maxY = mercY + GEOCACHE_IMAGE_SIZE;
		icon.minY = mercY - GEOCACHE_IMAGE_SIZE;
		// Sets name
		icon.name = geocache.getName();
		// Adds geocache to icon list
		mvgl.getIconList().add(icon);
		return icon;
	}
	
	// Gets all geocaches from database, adds them to the map
	private void showGeocache() {

		db = new DBAdapter(this);
		ArrayList<Geocache> gl = db.getAllGeocache();

		for(int i = 0; i < gl.size(); i++) {
			addGeocache(gl.get(i));
		}
		mvgl.renderScene();
	}
	
	private void showRoute(ArrayList<Node> routeNodes) {
		if(pathIsUpdating) {
			clearRoute();
			pathIsUpdating = false;
		}
		int[] nodes = new int[routeNodes.size() * 2];
		int maxX = 0;
		int minX = Integer.MAX_VALUE;
		int maxY = 0;
		int minY = Integer.MAX_VALUE;

		for (int j = 0; j < routeNodes.size(); j++) {
			
			int mercX = (int) (routeNodes.get(j).getLon() * 1000000);
			int mercY = (int) (routeNodes.get(j).getLat() * 1000000);

			nodes[j * 2] = mercX;
			nodes[j * 2 + 1] = mercY;
			
			MapItem wayPoint = new MapItem();
			wayPoint.type = MapPresets.ICON;
			wayPoint.numNodes = 1;
			wayPoint.nodes = nodes;
			wayPoint.flags = R.drawable.waypoint;
			wayPoint.maxX = mercX + WAYPOINT_IMAGE_SIZE;
			wayPoint.minX = mercX - WAYPOINT_IMAGE_SIZE;
			wayPoint.maxY = mercY + WAYPOINT_IMAGE_SIZE;
			wayPoint.minY = mercY - WAYPOINT_IMAGE_SIZE;
			mvgl.getIconList().add(wayPoint);
			
			if(mercX > maxX) {
				maxX = mercX;
			}
			if(mercY > maxY) {
				maxY = mercY;
			}
			if(mercY < minY) {
				minY = mercY;
			}
			if(mercX < minX) {
				minX = mercX;
			}
		}
		
		MapItem route = new MapItem();
		route.type = MapPresets.ROUTE;
		route.numNodes = routeNodes.size();
		route.nodes = nodes;
		route.flags = route.flags | MapItem.SHAPE_LINE;
		route.maxX = maxX + 6;
		route.minX = minX - 6;
		route.maxY = maxY + 5;
		route.minY = minY - 5;

		mvgl.getIconList().add(route);
		
		if(routesLeftToFetch.size() == 0) {
			mvgl.renderScene();
			
			if(!Double.isNaN(nextEndX)) {
				dataFetcher = new Thread(new PathFetcher(nextStartY, nextStartX, nextEndY, nextEndX));
				dataFetcher.start();
				
				nextEndX = Double.NaN;
			}
			
		}
		else {
			
			dataFetcher = new Thread(new PathFetcher(routesLeftToFetch.get(0).start.getCenter().y / 1000000.0,
													 routesLeftToFetch.get(0).start.getCenter().x / 1000000.0,
													 routesLeftToFetch.get(0).end.getCenter().y / 1000000.0, 
													 routesLeftToFetch.get(0).end.getCenter().x / 1000000.0));
			routesLeftToFetch.remove(0);
			dataFetcher.start();
		}
	}

	private void clearRoute() {

		for(int i = 0; i < mvgl.getIconList().size(); i++) {
			if(mvgl.getIconList().get(i).type == MapPresets.ROUTE) {

				//				mvgl.getIconList().remove(i);

				int j = mvgl.getIconList().get(i).numNodes + 1;
				for(; j > 0; j--, i--) {
					mvgl.getIconList().remove(i);
				}
			}
		}
	}

	private void showRoute(MapItem start, MapItem finish) {

		int finishX = (finish.maxX - finish.minX) / 2 + finish.minX;
		int finishY = (finish.maxY - finish.minY) / 2 + finish.minY;

		int startX = (start.maxX - start.minX) / 2 + start.minX;
		int startY = (start.maxY - start.minY) / 2 + start.minY;

		if (dataFetcher == null || !dataFetcher.isAlive()) {
			dataFetcher = new Thread(new PathFetcher(startY / 1000000.0, startX / 1000000.0, finishY / 1000000.0,
					finishX / 1000000.0));
			dataFetcher.start();
		}
		else {
			nextStartY = startY / 1000000.0;
			nextStartX = startX / 1000000.0;
			nextEndY = finishY / 1000000.0;
			nextEndX = finishX / 1000000.0;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.map_activity_menu, menu);
	    
	    for(MapItem mi: mvgl.getIconList()) {
	    	if(mi.flags == R.drawable.geonew &&
	    			ShowDistance.calculateDistance((mi.getCenter().y) / 1000000.0, (user.getCenter().y) / 1000000.0,
	    										   (mi.getCenter().x) / 1000000.0, (user.getCenter().x) / 1000000.0) < 0.02) {
	    		currentlySelected = mi;
	    	    menu.add(R.string.registerFoundGeocacheMenuTitle);	
	    	}
	    }
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.search_geocaches:
			Intent myIntent = new Intent(MapActivity.this, LocList.class);
			startActivityForResult(myIntent, REQUEST_GEOCACHES);
			return true;

		case R.id.settings_menu:
			Intent settingsIntent = new Intent(MapActivity.this, SettingsActivity.class);
			startActivity(settingsIntent);
			return true;

		case R.id.about:
			// Opens activity, which provides information about application
			Intent aboutIntent = new Intent(MapActivity.this, AboutActivity.class);
			startActivity(aboutIntent);
			return true;

		case 0:
			// Starts activity to register found geocaches
			if (item.getTitle()
					.toString()
					.equals(this
							.getString(R.string.registerFoundGeocacheMenuTitle))) {
				if (currentlySelected != null) {
					startLocateGeocacheActivity();
					return true;
				}
			}
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void startLocateGeocacheActivity() {
		// Creates new intent
		Intent registerIntent = new Intent(MapActivity.this, LocateGeocache.class);
		// Creates array with user position coordinates
		double[] userPosition = new double[2];
		userPosition[0] = user.getCenter().x;
		userPosition[1] = user.getCenter().y;
		// Puts coordinates into intent
		registerIntent.putExtra("userP", userPosition);
		// Starts LocateGeocache activity
		startActivityForResult(registerIntent, LOCATE_GEOCACHE);		
		currentlySelected = null;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == Activity.RESULT_CANCELED) {
			return;
		}
		switch (requestCode) {
		case REQUEST_GEOCACHES:
			ArrayList<String> geocaches = data.getStringArrayListExtra("caches");

			if(resultCode == RESULT_OK){
				mvgl.getIconList().clear();
				mvgl.getIconList().add(user);
				for(Geocache g: db.getGeocachesFromName(geocaches)) {
					addGeocache(g);
				}
			}
			else if(resultCode == RESULT_CALCULATE_ROUTE){
				mvgl.getIconList().clear();
				mvgl.getIconList().add(user);
				ArrayList<MapItem> geocacheItem = new ArrayList<MapItem>();
				ArrayList<Geocache> caches = db.getGeocachesFromName(geocaches);
				for(Geocache g: caches) {
					MapItem m = addGeocache(g);
					geocacheItem.add(m);
				}
				for(Geocache g: db.getAllGeocache()) {
					if(!caches.contains(g)) {
						addGeocache(g);
					}
				}

				clearRoute();
				
				for(int j = 1; j < geocacheItem.size(); j++) {
					routesLeftToFetch.add(new PathFindingPair(geocacheItem.get(j - 1), geocacheItem.get(j)));
				}

				if(geocacheItem.size() > 0) {
					showRoute(user, geocacheItem.get(0));
				}
			}
			break;
			
		case LOCATE_GEOCACHE: // Result of LocateGeocache activity
			// Deletes all icons from map
			mvgl.getIconList().clear(); 
			// Adds user
			mvgl.getIconList().add(user);
			// Adds geocaches
			showGeocache();
			break;

		default:
			break;	
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if(currentlySelected != null) {
			menu.add("Show route to " + currentlySelected.name);
			
			if(ShowDistance.calculateDistance((currentlySelected.getCenter().y) / 1000000.0, (user.getCenter().y) / 1000000.0,
		    								 (currentlySelected.getCenter().x) / 1000000.0, (user.getCenter().x) / 1000000.0) < 0.02) {
				menu.add(R.string.registerFoundGeocacheMenuTitle);
			}
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if(currentlySelected != null) {
			if (item.getTitle().toString().equals("Show route to " + currentlySelected.name)) {
//				routesLeftToFetch = 1;
				clearRoute();
				showRoute(user, currentlySelected);
				currentRouteTo = currentlySelected;
				lastRouteUpdateLoc = currentLocation;
			}
			else if(item.getTitle().toString().equals(this.getString(R.string.registerFoundGeocacheMenuTitle))) {
				startLocateGeocacheActivity();
			}
		}

		currentlySelected = null;

		return true;
	}
	
	
	private class PathFetcher implements Runnable {
		
		ServerConnection con;
		HashMap<String,String> params;
		double latitudeCurrent;
		double latitudeGc;
		double longitudeCurrent;
		double longitudeGc;
		
		public PathFetcher(double latStart, double lonStart, double latEnd, double lonEnd) {
			params = new HashMap<String, String>();
			params.put("latitudeCurrent", Double.toString(latStart));
			latitudeCurrent = latStart;
			params.put("longitudeCurrent", Double.toString(lonStart));
			longitudeCurrent = lonStart;
			params.put("latitudeGc", Double.toString(latEnd));
			latitudeGc = latEnd;
			params.put("longitudeGc", Double.toString(lonEnd));
			longitudeGc = lonEnd;
			con = new ServerConnection(new Handler() {
				public void handleMessage(Message msg) {
					switch(msg.what) {
						case 0:
							Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_LONG).show();
							break;
							
						default:
							break;
					}
				}
			});
		}

		@Override
		public void run() {
			con.buildRequest("getShortestPath", params);
			@SuppressWarnings("unchecked")
			ArrayList<Node> routeNodes = (ArrayList<Node>) con.executeQuery();
			
			if(routeNodes == null) {
				routeNodes = new ArrayList<Node> ();
			}

			routeNodes.add(0, new Node(12345, latitudeCurrent, longitudeCurrent));
			routeNodes.add(new Node(123456, latitudeGc, longitudeGc));
			
			showRoute(routeNodes);
		}

	}
	
	private class PathFindingPair {
		public MapItem start;
		public MapItem end;
		
		public PathFindingPair(MapItem start, MapItem end) {
			this.start = start;
			this.end = end;
		}
	}

}
