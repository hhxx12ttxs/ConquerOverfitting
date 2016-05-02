package edu.ucdavis.gwt.gis.client.export;

import java.util.LinkedList;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Element;
import com.google.gwt.http.client.URL;
import com.google.gwt.jsonp.client.JsonpRequestBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;

import edu.ucdavis.cstars.client.Graphic;
import edu.ucdavis.cstars.client.MapWidget;
import edu.ucdavis.cstars.client.geometry.Extent;
import edu.ucdavis.cstars.client.geometry.Geometry;
import edu.ucdavis.cstars.client.geometry.Point;
import edu.ucdavis.cstars.client.geometry.Polygon;
import edu.ucdavis.cstars.client.geometry.Polyline;
import edu.ucdavis.cstars.client.geometry.Geometry.GeometryType;
import edu.ucdavis.cstars.client.layers.FeatureLayer;
import edu.ucdavis.cstars.client.layers.KMLLayer;
import edu.ucdavis.cstars.client.layers.Layer;
import edu.ucdavis.cstars.client.layers.TiledMapServiceLayer;
import edu.ucdavis.cstars.client.symbol.PictureMarkerSymbol;
import edu.ucdavis.cstars.client.symbol.SimpleFillSymbol;
import edu.ucdavis.cstars.client.symbol.SimpleLineSymbol;
import edu.ucdavis.cstars.client.symbol.SimpleMarkerSymbol;
import edu.ucdavis.cstars.client.symbol.Symbol;
import edu.ucdavis.gwt.gis.client.DataManager;
import edu.ucdavis.gwt.gis.client.Debugger;
import edu.ucdavis.gwt.gis.client.canvas.CanvasMap;
import edu.ucdavis.gwt.gis.client.canvas.CanvasPoint;
import edu.ucdavis.gwt.gis.client.canvas.CanvasPolygon;
import edu.ucdavis.gwt.gis.client.canvas.CanvasPolyline;
import edu.ucdavis.gwt.gis.client.canvas.CanvasMap.CanvasMapLoadHandler;
import edu.ucdavis.gwt.gis.client.layers.DataLayer;
import edu.ucdavis.gwt.gis.client.layers.FeatureCollectionDataLayer;
import edu.ucdavis.gwt.gis.client.layers.DataLayer.DataLayerType;

public class ImageExporter {

	public static ImageExporter INSTANCE = new ImageExporter();
	
	private JsonpRequestBuilder xhr = new JsonpRequestBuilder(); 
	private LinkedList<ImageInfo> imageList = new LinkedList<ImageInfo>();
	private MapWidget map;
	private String url = "";

	private int count = 0;
	boolean loading= false;
	
	private LinkedList<String> baseUrls = new LinkedList<String>();
	private LinkedList<MapGraphic> graphics = new LinkedList<MapGraphic>();
	private int imageHeight = 0;
	private int imageWidth = 0;
	
	public interface ImageExportHandler {
		public void onStart();
		public void onFailure(String message);
		public void onComplete(Image img);
	}
	private ImageExportHandler handler = null;
	
	private native boolean instanceOf(Layer l, String type) /*-{
		type = "esri.layers."+type;
		if( l.declaredClass ) {
			if( l.declaredClass == type ) return true; 
		}
		return false;
	}-*/;
	
	private void setup() {
		if( map == null || url == null ) {
			try {
				map = DataManager.INSTANCE.getMap();
				url = DataManager.INSTANCE.getConfig().getExportImageUrl();
			} catch (Exception e) {
				Debugger.INSTANCE.catchException(e, "ImageExporter", "error setting config params");
			}
		}
	}
	
	public void export(ImageExportHandler h) {
		if(loading) {
			h.onFailure("Busy");
			return;
		}
		setup();
		handler = h;
		
		baseUrls.clear();
		graphics.clear();
		
		imageList.clear();
		
		LinkedList<Graphic> mapGraphics = new LinkedList<Graphic>();
		JsArray<Graphic> layerGraphics = map.getGraphics().getGraphics();
		for( int i = 0; i < layerGraphics.length(); i++ ) {
			mapGraphics.add(layerGraphics.get(i));
		}
		
		LinkedList<DataLayer> datalayers = DataManager.INSTANCE.getDataLayers();
		for( DataLayer layer: datalayers ) {
			if( layer.getType() == DataLayerType.FeatureCollection ) {
				LinkedList<Graphic> graphicsList = ((FeatureCollectionDataLayer) layer).getGraphics();
				for( Graphic g: graphicsList) {
					mapGraphics.add(g);
				}
			}
		}
		
		
		Extent ext = map.getExtent();
		int width = map.getWidth();
		int height = map.getHeight();
		
		
		JsArrayString ids = map.getLayerIds();
		for( int i = 0; i < ids.length(); i++ ){
			Layer layer = map.getLayer(ids.get(i));
			if( layer.isVisible() ){
				if( instanceOf(layer, "TiledMapServiceLayer") ){
					parseTiledLayer((TiledMapServiceLayer) layer, i);
					addBaseUrl(layer.getUrl());
				} else if( instanceOf( layer, "OpenStreetMapLayer") ) {
					parseTiledLayer((TiledMapServiceLayer) layer, i);
					addBaseUrl(layer.getUrl());
				} else if( instanceOf(layer, "ArcGISTiledMapServiceLayer") ){
					parseTiledLayer((TiledMapServiceLayer) layer, i);
					addBaseUrl(layer.getUrl());
				} else if( instanceOf(layer, "ArcGISDynamicMapServiceLayer") ) {
					parseDynamicLayer(layer, i);
					addBaseUrl(layer.getUrl());
				} else if ( instanceOf(layer, "KMLLayer") ) {
					JsArray<Layer> layers = ((KMLLayer) layer).getLayers();
					for( int j = 0; j < layers.length(); j++ ) {
						if( instanceOf(layers.get(j), "FeatureLayer") ) {
							addFeatureLayerGraphics((FeatureLayer) layers.get(j), ext, width, height);
						}
					}
				} else if ( instanceOf(layer, "FeatureLayer") ) {
					addFeatureLayerGraphics((FeatureLayer) layer, ext, width, height);
				}
				
			}
		}
		
		for( int i = 0; i < mapGraphics.size(); i++ ) {
			Graphic g = mapGraphics.get(i);
			
			Symbol s = null;
			if( g.getSymbol() != null ) {
				s = g.getSymbol();
			} else if( map.getGraphics().getRenderer() != null ) {
				s = map.getGraphics().getRenderer().getSymbol(g);
			}
			
			if( s != null ) {
				Geometry geo = Geometry.toScreenGeometry(ext, width, height, g.getGeometry());
				graphics.add(new MapGraphic(geo, s));
			}
		}
		
		// sort by z-index
		LinkedList<ImageInfo> tmpList = new LinkedList<ImageInfo>();
		for( ImageInfo img: imageList ){
			boolean found = false;
			for( int i = 0; i < tmpList.size(); i++ ){
				if( tmpList.get(i).zindex > img.zindex ) {
					tmpList.add(i, img);
					found = true;
					break;
				}
			}
			if( !found ) tmpList.add(img);
		}
		imageList = tmpList;
		
		//if( image != null ){
		//	image.removeFromParent();
		//	image = null;
		//}

		String url = this.url+"?";
		count++;
		
		imageHeight = map.getHeight();
		imageWidth = map.getWidth();
		url += "ih="+imageHeight;
		url += "&iw="+imageWidth;
		url += "&ni="+imageList.size();
	
		for( int i = 0; i < imageList.size(); i++ ) {
			
			ImageInfo info =  imageList.get(i);
			
			String urlTemp = info.url;
			urlTemp = urlTemp.replace("http://", "");
			
			int urlid = -1;
			String urlc = "";
			
			for( int j = 0; j < baseUrls.size(); j++ ){
				if( urlTemp.contains(baseUrls.get(j)) ){
					urlid = j;
					urlc = urlTemp.replace(baseUrls.get(j), "");
				}
			}
			// some services now return multiple urls.  So if it's not found and it into the list
			if( urlid == -1 ) {
				if( urlTemp.contains("MapServer") ) {
					String newUrl = urlTemp.split("MapServer")[0]+"MapServer";
					baseUrls.add(newUrl);
					urlid = baseUrls.size()-1;
					urlc = urlTemp.replace(newUrl, "");
				}
			}
			
			
			url += "&uid"+i+"="+urlid;
			url += "&uc"+i+"="+URL.encodeQueryString(urlc);
			url += "&h"+i+"="+info.height;
			url += "&w"+i+"="+info.width;
			url += "&o"+i+"="+info.opacity;
			
			if( info.left < 0 || info.top < 0 ) {
				int sx = 0;
				int sy = 0;
				int dx = 0;
				int dy = 0;
				
				if( info.left < 0 ){
					sx = Math.abs(info.left);
				} else {
					dx = info.left;
				}
				
				if( info.top < 0 ){
					sy = Math.abs(info.top);
				} else {
					dy = info.top;
				}
				
				url += "&t"+i+"="+dy;
				url += "&l"+i+"="+dx;
				url += "&sx"+i+"="+sx;
				url += "&sy"+i+"="+sy;
				
			} else {
				
				url += "&t"+i+"="+info.top;
				url += "&l"+i+"="+info.left;
				url += "&sx"+i+"=0";
				url += "&sy"+i+"=0";
				
			}
		}
		
		for( int i = 0; i < baseUrls.size(); i++ ){
			url += "&urlb"+i+"="+URL.encodePathSegment(baseUrls.get(i));
		}
		
		loading = true;
		handler.onStart();
		
		xhr.requestString(url, new AsyncCallback<String>() {
			@Override
			public void onFailure(Throwable caught) {
				loading = false;
				handler.onFailure("Failed to generate image.");
			}
			@Override
			public void onSuccess(String result) {
				makeCanvasImage(result);
			}
		});
	}
	
	private void makeCanvasImage(String data) {
		CanvasMap canvasMap = new CanvasMap(imageWidth, imageHeight);
		canvasMap.addGeometry(new CanvasPoint(0, 0, "data:image/png;base64,"+data));
				
		// draw graphics
		for( MapGraphic graphic: graphics) {
			drawGraphicLayer(graphic, canvasMap);
		}
		
		canvasMap.setLoadHandler(new CanvasMapLoadHandler(){
			@Override
			public void onLoad(CanvasMap canvasMap) {
				Image image = new Image(canvasMap.getImageData());
				handler.onComplete(image);
				loading = false;
			}
		});
		canvasMap.redraw();
	}
	
	private void addFeatureLayerGraphics(FeatureLayer layer, Extent ext, int width, int height) {
		for( int i = 0; i < layer.getGraphics().length(); i++ ) {
			Graphic g = layer.getGraphics().get(i);
			Symbol s = layer.getRenderer().getSymbol(g);
			Geometry geo = Geometry.toScreenGeometry(ext, width, height, g.getGeometry());
			graphics.add(new MapGraphic(geo, s));
		}
	}
	
	private void drawGraphicLayer(MapGraphic graphic, CanvasMap canvasMap) {
		
		// DRAW ICON
		if( graphic.getSymbol().getType().contentEquals("picturemarkersymbol") ) {
			// make sure geometry is a point
			if( graphic.getXyGeometry().getType() != GeometryType.POINT ) return;
			
			PictureMarkerSymbol pmc = ((PictureMarkerSymbol) graphic.getSymbol());
			if( pmc.getUrl().startsWith("data") ) {

				int h = (int) Math.ceil(pmc.getHeight() / 2);
				int w = (int) Math.ceil(pmc.getWidth() / 2);
				Point p = (Point) graphic.getXyGeometry();
				canvasMap.addGeometry(new CanvasPoint(p.getX()-w, p.getY()-h, pmc.getWidth(), pmc.getHeight(), pmc.getUrl()));

			} else {
				// we need to proxy the icon url into b64 data
			}
			
		// DRAW LINE
		} else if( graphic.getSymbol().getType().contentEquals("simplelinesymbol") ) {
			// make sure geometry is a polyline
			if( graphic.getXyGeometry().getType() != GeometryType.POLYLINE ) return;
			
			SimpleLineSymbol sls = (SimpleLineSymbol) graphic.getSymbol();
			
			String stroke = sls.getColor().toCss(true);
	
			Polyline p = (Polyline) graphic.getXyGeometry();
			CanvasPolyline line = new CanvasPolyline(p, stroke);
			line.setLineWidth(sls.getWidth());
			canvasMap.addGeometry(line);
			
		// DRAW POLYGON
		} else if ( graphic.getSymbol().getType().contentEquals("simplefillsymbol") ) {
			// make sure geometry is a polygon
			if( graphic.getXyGeometry().getType() != GeometryType.POLYGON ) return;
			
			SimpleFillSymbol sfs = (SimpleFillSymbol) graphic.getSymbol();
			
			String fill = null;
			String stroke = null;
			double lineWidth = 1;
			if( sfs.getColor() != null ) fill = sfs.getColor().toCss(true);
			if( sfs.getOutline() != null ) {
				stroke = sfs.getOutline().getColor().toCss(true);
				lineWidth = sfs.getOutline().getWidth();
			}
	
			Polygon p = (Polygon) graphic.getXyGeometry();
			CanvasPolygon cPoly = new CanvasPolygon(p, stroke, fill);
			cPoly.setLineWidth(lineWidth);
			canvasMap.addGeometry(cPoly);
			
		// DEFAULT MAP MARKERS
		} else if ( graphic.getSymbol().getType().contentEquals("simplemarkersymbol") ) {
			// make sure geometry is a point
			if( graphic.getXyGeometry().getType() != GeometryType.POINT ) return;
			
			SimpleMarkerSymbol sms = (SimpleMarkerSymbol) graphic.getSymbol();
			Point p = (Point) graphic.getXyGeometry();
			FeatureCollectionDataLayer.createMarkerSymbolOnCanvas(sms, canvasMap, p.getY(), p.getX());
		}
		
	}


	private void addBaseUrl(String url){
		if( url == null ) return;
		if( !url.contains("http") ) url = Window.Location.getHost()+url;
		url = url.replace("http://", "").replaceAll("\\?.*", "");
		if( !baseUrls.contains(url) ){
			baseUrls.add(url);
		}
	}
	
	private void parseDynamicLayer(Layer layer, int index) {
		Element child =  getDynamicElement(layer.getId());
		ImageInfo info = new ImageInfo();

		info.top = 0;
		info.left = 0;
		
		info.width = Integer.parseInt(child.getStyle().getWidth().replaceAll("px", ""));
		info.height = Integer.parseInt(child.getStyle().getHeight().replaceAll("px", ""));
		
		info.opacity = layer.getOpacity();
		
		//info.zindex = Integer.parseInt(child.getStyle().getZIndex());
		info.zindex = index;
		
		info.url = child.getAttribute("src");
		if( !info.url.contains("http") ) info.url = Window.Location.getHost()+info.url;
		
		imageList.add(info);
	}
	
	
	private void parseTiledLayer(TiledMapServiceLayer layer, int index) {
		String mode = DataManager.INSTANCE.getMap().getNavigationMode();
		if( mode == null ) parseTiledLayerNormal(layer, index);
		if( mode.contentEquals("css-transforms") ) {
			parseTiledLayerFX(layer, index);
		} else {
			parseTiledLayerNormal(layer, index);
		}
	}
	
	// for parsing map when it's in navigation mode = 'css-transform';
	private void parseTiledLayerFX(TiledMapServiceLayer layer, int index) {
		Element root = getLayerElement(layer.getId());
		JsArray<Element> children = getChildrenFx(layer.getId());
		
		int offsetTop = 0;
		int offsetLeft = 0;
		
		int[] translate = parseTranslateProperty(root);
		offsetLeft = translate[0];
		offsetTop = translate[1];

		int zindex = index;
		
		
		for( int i = 0; i < children.length(); i++ ){
			Element child = children.get(i);
			ImageInfo info = new ImageInfo();
			
			int top = 0;
			int left = 0;
			

			translate = parseTranslateProperty(child);
			left = translate[0];
			top = translate[1];
			
			top = offsetTop + top;
			left = offsetLeft + left;
			
			info.top = top;
			info.left = left;
			
			info.opacity = layer.getOpacity();
			
			info.width = Integer.parseInt(child.getStyle().getWidth().replaceAll("px", ""));
			info.height = Integer.parseInt(child.getStyle().getHeight().replaceAll("px", ""));
			
			info.zindex = zindex;
			
			info.url = child.getAttribute("src");
			if( !info.url.contains("http") ) info.url = Window.Location.getHost()+info.url;
			
			// TODO: HACK
			// The http://a.tile.openstreetmap.org seems to reject the python script requests
			// So change all the a.tile servers to b's
			if( instanceOf(layer, "OpenStreetMapLayer") ) {
				info.url = info.url.replace("a.tile.openstreetmap.org", "b.tile.openstreetmap.org");
				if( info.url.contains("tile.openstreetmap.org") ){
					addBaseUrl("http://"+info.url.replace("http://","").split("/")[0]);
				}
			}
			
			imageList.add(info);
		}
	}
	
	private void parseTiledLayerNormal(TiledMapServiceLayer layer, int index){
		Element root = getLayerElement(layer.getId());
		JsArray<Element> children = getChildren(layer.getId());
		
		int offsetTop = 0;
		int offsetLeft = 0;
		

		offsetTop = Integer.parseInt(root.getStyle().getTop().replaceAll("px", ""));
		offsetLeft = Integer.parseInt(root.getStyle().getLeft().replaceAll("px", ""));
		
		int zindex = index;
		
		for( int i = 0; i < children.length(); i++ ){
			Element child = children.get(i);
			ImageInfo info = new ImageInfo();
			
			int top = 0;
			int left = 0;
			
			top = Integer.parseInt(child.getStyle().getTop().replaceAll("px", ""));
			left = Integer.parseInt(child.getStyle().getLeft().replaceAll("px", ""));
			
			top = offsetTop + top;
			left = offsetLeft + left;
			
			info.top = top;
			info.left = left;
			
			info.opacity = layer.getOpacity();
			
			info.width = Integer.parseInt(child.getStyle().getWidth().replaceAll("px", ""));
			info.height = Integer.parseInt(child.getStyle().getHeight().replaceAll("px", ""));
			
			info.zindex = zindex;
			
			info.url = child.getAttribute("src");
			if( !info.url.contains("http") ) info.url = Window.Location.getHost()+info.url;
			
			// TODO: HACK
			// The http://a.tile.openstreetmap.org seems to reject the python script requests
			// So change all the a.tile servers to b's
			if( instanceOf(layer, "OpenStreetMapLayer") ) {
				info.url = info.url.replace("a.tile.openstreetmap.org", "b.tile.openstreetmap.org");
				if( info.url.contains("tile.openstreetmap.org") ){
					addBaseUrl("http://"+info.url.replace("http://","").split("/")[0]);
				}
			}
			
			imageList.add(info);
		}
	}
	
	private int[] parseTranslateProperty(Element ele) {
		String transform = getTransform(ele);
		if( transform.isEmpty() ) return new int[] {0, 0};
		
		if( transform.contains("translate3d") ) {
			String translate = transform.replaceAll("translate3d\\(", "").replaceAll("\\)", "").replaceAll("px", "");
			String[] parts = translate.split(", ");
			if( parts.length > 1 ) {
				try {
					return new int[] { 
							Integer.parseInt(parts[0]),
							Integer.parseInt(parts[1])
						};
				} catch (Exception e) {}
			}
		} else {
			String translate = transform.replaceAll("translate\\(", "").replaceAll("\\)", "").replaceAll("px", "");
			String[] parts = translate.split(", ");
			if( parts.length > 1 ) {
				try {
					return new int[] { 
							Integer.parseInt(parts[0]),
							Integer.parseInt(parts[1])
						};
				} catch (Exception e) {}
			}
		}
		
		return new int[] {0, 0};
	}
	
	private native String getTransform(Element ele) /*-{
		if( ele.style['MozTransform'] ) return ele.style['MozTransform'];
		if( ele.style['WebkitTransform'] ) return ele.style['WebkitTransform'];
		if( ele.style['transform'] ) return ele.style['transform'];
		if( ele.style['msTransform'] ) return ele.style['msTransform'];
		if( ele.style['OTransform'] ) return ele.style['OTransform'];
		return "";
	}-*/;
	
	private native Element getDynamicElement(String id) /*-{
		return $wnd.document.getElementById("map_0_"+id).children[0];
	}-*/;
	
	private native Element getLayerElement(String id) /*-{
		return $wnd.document.getElementById("map_0_"+id);
	}-*/;
	
	private native JsArray<Element> getChildren(String id) /*-{
		return $wnd.document.getElementById("map_0_"+id).children;
	}-*/;
	
	private native JsArray<Element> getChildrenFx(String id) /*-{
		return $wnd.document.getElementById("map_0_"+id).children[0].children;
	}-*/;
	
	public class MapGraphic {
		private Geometry xyGeometry = null;
		private Symbol symbol = null;
		public MapGraphic(Geometry g, Symbol s) {
			xyGeometry = g;
			symbol = s;
		}
		public Geometry getXyGeometry() {
			return xyGeometry;
		}
		public Symbol getSymbol() {
			return symbol;
		}
	}
	
	public class ImageInfo {
		public String url = "";
		public int top = 0;
		public int left = 0;
		public int width = 0;
		public int height = 0;
		public double opacity = 0;
		public int zindex = 0;
	}
	

	
}

