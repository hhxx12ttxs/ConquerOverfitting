package edu.berkeley.cs169.client;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.RequestTimeoutException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DisclosureEvent;
import com.google.gwt.user.client.ui.DisclosureHandler;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MouseWheelListener;
import com.google.gwt.user.client.ui.MouseWheelVelocity;
import com.google.gwt.user.client.ui.PopupListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.mapitz.gwt.googleMaps.client.GControl;
import com.mapitz.gwt.googleMaps.client.GCopyright;
import com.mapitz.gwt.googleMaps.client.GCopyrightCollection;
import com.mapitz.gwt.googleMaps.client.GLatLng;
import com.mapitz.gwt.googleMaps.client.GLatLngBounds;
import com.mapitz.gwt.googleMaps.client.GMap2;
import com.mapitz.gwt.googleMaps.client.GMap2EventClickListener;
import com.mapitz.gwt.googleMaps.client.GMap2EventManager;
import com.mapitz.gwt.googleMaps.client.GMap2Widget;
import com.mapitz.gwt.googleMaps.client.GMapOptions;
import com.mapitz.gwt.googleMaps.client.GMapType;
import com.mapitz.gwt.googleMaps.client.GMarker;
import com.mapitz.gwt.googleMaps.client.GMarkerEventClickListener;
import com.mapitz.gwt.googleMaps.client.GMarkerEventDragListener;
import com.mapitz.gwt.googleMaps.client.GMarkerEventManager;
import com.mapitz.gwt.googleMaps.client.GMarkerEventMouseListener;
import com.mapitz.gwt.googleMaps.client.GMarkerOptions;
import com.mapitz.gwt.googleMaps.client.GMercatorProjection;
import com.mapitz.gwt.googleMaps.client.GOverlay;
import com.mapitz.gwt.googleMaps.client.GOverlayCollection;
import com.mapitz.gwt.googleMaps.client.GPolyline;
import com.mapitz.gwt.googleMaps.client.GTileLayer;
import com.mapitz.gwt.googleMaps.client.JSObject;

import edu.berkeley.cs169.client.rpc.UpdateEdgeCallback;
import edu.berkeley.cs169.client.rpc.UpdateNodeCallback;
import edu.berkeley.cs169.server.graph.Edge;
import edu.berkeley.cs169.server.graph.Node;

/**
 * This panel holds the Google Maps API work. Access the GMap from here,
 * and tell it to do things, too. The Map Modification stuff is all here
 * as well.
 * 
 * @author Han,Simon,Brian
 */
public class MapPanel extends Composite {

    // The Google Map, and the widget that holds it, and the panel that holds IT
    public GMap2 theMap;
    private GMap2Widget mapWidget;
    private FocusPanel mapP;
    
    // Our custom overlay of the campus
    private GMapType customMap;
    
    // Managers that are always instantiated when entering MapModMode
    private GMap2EventManager mapEventMan;   
    private GMarkerEventManager markerEventMan;
    
    public Map allMarkers = new HashMap(); // Markers => Nodes
    public Map allPolylines = new HashMap(); // Edges => Polylines

    // Collection of avgNodes (one for each non-construction Location) that is used in map-clicking
    public Collection avgNodes;
    
    // Used in MapModMode, but also needed everywhere
    public AttributeEditBox attributeEditBox;
    
    // State variables
    private GMarker currentMarker;
    private Node currentNode;
    private GOverlayCollection currentNodeColl;
    private boolean attrEditBoxInit;

    // Like it says, the exact center of campus.
    private static final GLatLng CAMPUS_CENTER = new GLatLng(37.87178125576512, -122.2595340013504);
    private static final int DEFAULT_ZOOM = 16;

    // Set some constant colors for use around the place
    private static final String BLUE = "#0000FF";        // blue
    private static final String SHADER = "#00000F";      // close to black

    /**
     * The constructor to build our mapPanel.
     */
    public MapPanel() {
        mapP = setupMap();
        mapP.addMouseWheelListener(new MapWheelZoom());
        initWidget(mapP);
        this.setStyleName("mapPanel");
    }

    /**
     * Sets up an absolute panel to be used for the map
     * @return
     */
    private FocusPanel setupMap() {
        // Basically, we need a FocusPanel to capture the mouse wheel events
        FocusPanel p = new FocusPanel();
        // AND an AbsolutePanel in order to position the home icon
        AbsolutePanel ap = new AbsolutePanel();
        // Unfortunately, FocusPanels are SimplePanels, while AbsolutePanels are ComplexPanels

        // Our options set - you can add to this to configure the map
        GMapOptions ourOptions = new GMapOptions();
        ourOptions.setLogoPassive(true);

        // Create the widget with upper left corner at CAMPUS_CENTER
        mapWidget = new GMap2Widget("560","600",CAMPUS_CENTER,DEFAULT_ZOOM,ourOptions);

        // Add the map to our layout
        ap.add(mapWidget);
        // Add the home icon to the upper left corner of the map
        ap.add(buildHomeIcon(), 35, 6);

        // Retrieve the GMap2 object and start manipulating your map
        theMap = mapWidget.getGmap();
        theMap.addControl(GControl.GSmallZoomControl());
        theMap.addControl(GControl.GMapTypeControl());
        theMap.enableDoubleClickZoom();
        theMap.enableContinuousZoom();
        theMap.setMapType(GMapType.G_HYBRID_MAP());

        setUpCustomOverlay();
        
        // Get the managers ready
        markerEventMan = GMarkerEventManager.getInstance();
        mapEventMan = GMap2EventManager.getInstance();
        mapEventMan.addOnClickListener(theMap, new MapClickListener());
        
        p.setWidget(ap);
        return p;
    }

    /**
     * Does the work to set up our Campus custom overlay
     */
    private void setUpCustomOverlay() {
        GCopyrightCollection copyCollection = new GCopyrightCollection("UC Berkeley");
        // Set the rectangular latlng bounds for the custom map
        GCopyright copyright = new GCopyright(1, new GLatLngBounds(new GLatLng(27.872659929888, -132.25953936577), new GLatLng(47.872659929888, -112.25953936577)), 0, "http://www.berkeley.edu/map/");
        copyCollection.addCopyright(copyright);
        GTileLayer layer = new GTileLayer(copyCollection, 17, 17);
        configureLayer(layer.getJSObject(), this);
        GTileLayer[] layerarray = {layer};
        customMap = new GMapType(layerarray, new GMercatorProjection(19), "Campus");
        theMap.addMapType(customMap);
    }
    
    /**
     * JSNI method to configure our custom map overlay.
     * @param layer
     * @param tileUrlHandler
     */
    private static native void configureLayer(JSObject layer, MapPanel tileUrlHandler)
    /*-{
        layer.getTileUrl = function(a,b)
        {
        	if (b==17 && a.x>=21018 && a.x<=21026 && a.y>=50614 && a.y<= 50621) {
        		return "images/maptiles/Tile_"+(a.x)+"_"+(a.y)+"_"+b+".jpg";
			} else {
          		return "images/maptiles/whitespace.PNG";
                // to return white spaces: return "images/maptiles/whitespace.JPG"; or return "images/maptiles/whitespace.PNG";
                // to return the Google Maps default: return CMF.mPanel.theMap.getMapTypes()[1].getTileLayers()[0].getTileUrl(a,b);
                // original code: return G_NORMAL_MAP.getTileLayers()[0].getTileUrl(a,b);
          	}
        }; 
    }-*/;

    /**
     * Returns a "Home" icon that, when clicked, the map is reset to
     * the default zoom level and is reset to the default center.
     */
    private Image buildHomeIcon() {
        Image homeIcon = new Image("http://maps.google.com/mapfiles/kml/pal3/icon48.png");
        homeIcon.setTitle("Reset the view");
        homeIcon.addClickListener(new ClickListener() { public void onClick(Widget s) { recenter(); } });
        return homeIcon;
    }
    
    /**
     * Dramatically recenters the map.
     */
    protected void recenter() {
        theMap.setZoom(DEFAULT_ZOOM);
        theMap.panTo(CAMPUS_CENTER);
    }
    
    /**
     * Switches the map to our super special custom map overlay
     */

    protected void changeToCustomMode() {
        theMap.setMapType(customMap);
    }
    
    /**
     * What you call when you want to resize the map dynamically.
     * Resizes both the main map widget and the absolutePanel that holds it.
     * @param width in pixels
     * @param height in pixels
     */
    private void resize(int width, int height) {
        mapWidget.setHeight(height + "px");
        mapWidget.setWidth(width + "px");
        mapP.setHeight(height + "px");
        mapP.setWidth(width + "px");
    }
    
    /**
     * JSNI method that tries to intelligently resize the map when asked.
     * Bases its calculations off of the window frame's height and width.
     */
    protected native void jsniResize() /*-{
    // Figure out the true inner dimensions of the window
    var myWidth = 0, myHeight = 0;
    if ( typeof( $wnd.innerWidth ) == 'number' ) {
        // Non-IE
        myWidth = $wnd.innerWidth;
        myHeight = $wnd.innerHeight;
    } else if ( $doc.documentElement && ( $doc.documentElement.clientWidth || $doc.documentElement.clientHeight ) ) {
        // IE 6+ in 'standards compliant mode'
        myWidth = $doc.documentElement.clientWidth;
        myHeight = $doc.documentElement.clientHeight;
    } else if ( $doc.body && ( $doc.body.clientWidth || $doc.body.clientHeight ) ) {
        // IE 4 compatible
        myWidth = $doc.body.clientWidth;
        myHeight = $doc.body.clientHeight;
    }
  
    this.@edu.berkeley.cs169.client.MapPanel::resize(II)(myWidth-330,myHeight-100);
    }-*/;

    /**
     * Without this, only the first entry in MapModMode would work.
     */  
    public void resetMapModMode() {
        attrEditBoxInit = false;
        allMarkers.clear();
        allPolylines.clear();
    }

    public void setupAllMarkers() {
        for (Iterator markersIter = allMarkers.keySet().iterator(); markersIter.hasNext();)
            setupMarker((GMarker) markersIter.next());
    }

    /**
     * Add mouse, click, and drag listeners to markerEventMan for the marker argument
     * @param marker
     */
    private void setupMarker(GMarker marker) {
        NodeMouseListener nodeMouseListener = new NodeMouseListener();
        markerEventMan.addOnClickListener(marker, new NodeClickListener());   
        markerEventMan.addOnMouseOverListener(marker, nodeMouseListener);
        markerEventMan.addOnMouseOutListener(marker, nodeMouseListener);

        NodeDragListener nodeDragListener = new NodeDragListener();
        markerEventMan.addOnDragStartListener(marker, nodeDragListener);
        markerEventMan.addOnDragEndListener(marker, nodeDragListener);
        //marker.enableDragging();
    }

    private void callUpdateNodeService(Node node) {
        CalMap.upNodeService.updateNode(
                node.id.intValue(),
                node.name,
                node.type.i,
                node.x,
                node.y,
                node.elevation,
                new UpdateNodeCallback());	
    }

    private void callUpdateEdgeService(boolean isDeletingEdge) {
        CalMap.upEdgeService.updateEdge(
                attributeEditBox.edgeMap,
                isDeletingEdge,
                new UpdateEdgeCallback());	
    }

    private GMarker makeNewMarker(GLatLng point, Integer id, String name) {
        GMarker marker;
        GMarkerOptions options = new GMarkerOptions();
        options.setDraggable(true);
        options.setTitle(id + ": " + name);

        marker = new GMarker(point, options);	
        setupMarker(marker);

        return marker;
    }

    private class NodeClickListener implements GMarkerEventClickListener {

        /**
         * If a GMarker is mouse-clicked, then the "node settings box" 
         * pops up, allowing for edits to be made to the corresponding 
         * node and surrounding edges.
         */
        public void onClick(GMarker marker) {
            boolean enabled = CMF.mModPanel.clickMapBox.isEnabled();
            boolean checked = CMF.mModPanel.clickMapBox.isChecked();

            if (enabled && checked) {
                currentMarker = marker;
                currentNode = (Node) allMarkers.get(currentMarker);

                // Hide the old box when a new one is about to open
                if (attrEditBoxInit)
                    attributeEditBox.hide();
                attributeEditBox = new AttributeEditBox();
                attributeEditBox.addPopupListener(new AttrPopupListener());
                attributeEditBox.show(); 
                attrEditBoxInit = true;

                // Prepare for highlighting of surrounding edges
                currentNodeColl = new GOverlayCollection();         	            
            }
        }

        public void onDblClick(GMarker marker) {}

        private class AttrPopupListener implements PopupListener {
            public void onPopupClosed(PopupPanel sender, boolean autoClosed) {
                theMap.removeOverlay(currentNodeColl);
            }
        }

    }

    private class NodeDragListener implements GMarkerEventDragListener {

        private GLatLng begin;

        public void onDragStart(GMarker marker) {
            begin = marker.getPoint();				
        }

        public void onDragEnd(GMarker marker) {
            currentMarker = marker;
            currentNode = (Node) allMarkers.get(marker);

            if (CMF.mModPanel.clickMapBox.isChecked()) {
                if (attrEditBoxInit)
                    attributeEditBox.hide();

                MoveNodeConfirmBox moveNodeConfirmBox = new MoveNodeConfirmBox();
                moveNodeConfirmBox.show();

            } else
                currentMarker.setPoint(begin);                   
        }


        /**
         * Dialog box that pops up to confirm whether or not you
         * want to move a new node/GMarker when you drag it to
         * new spot on the map.  Clicking "Yes" will save the 
         * changes locally and on server.
         * 
         * @author Han
         *
         */
        private class MoveNodeConfirmBox extends DialogBox {

            private final Button yes = new Button("Yes");
            private final Button no = new Button("No");

            public MoveNodeConfirmBox() {
                super(false,true); // Modal
                
                VerticalPanel mainPanel = new VerticalPanel();

                this.setText("Node Placement Alert");

                Label label = new Label("Are you sure you want to move this node?\n ");
                FlowPanel buttons = new FlowPanel();

                yes.addClickListener(new YesListener());
                no.addClickListener(new NoListener());

                buttons.add(yes);
                buttons.add(no);				

                mainPanel.add(label);
                mainPanel.add(buttons);

                this.setWidget(mainPanel);
                setPopupPosition(320, 90);
            }

            private class YesListener implements ClickListener {
                public void onClick(Widget sender) {
                    GLatLng point = currentMarker.getPoint();
                    currentNode.x = point.lat();
                    currentNode.y = point.lng();

                    callUpdateNodeService(currentNode);              
                    changeOverlayLocs(false);  	
                    MoveNodeConfirmBox.this.hide();
                }
            }

            private class NoListener implements ClickListener {	
                public void onClick(Widget sender) {
                    currentMarker.setPoint(begin);
                    MoveNodeConfirmBox.this.hide();
                }			
            }

        }

    }

    /**
     * When mousing over a marker on the map, if
     * "Display all edges" is off, mousing over a marker
     * will highlight the node's surrounding edges.  If all
     * edges are already displayed, then the surrounding ones
     * will change color.
     * 
     * @author Han
     *
     */
    private class NodeMouseListener implements GMarkerEventMouseListener {

        GOverlayCollection mouseOverColl;

        public void onMouseOver(GMarker marker) {
            String color = (CMF.mModPanel.allEdgesBox.isChecked()) ? SHADER : BLUE;

            if (CMF.mModPanel.clickMapBox.isChecked()) {
                Node node = (Node) allMarkers.get(marker);
                Edge tmpEdge;
                GPolyline tmpLine;
                mouseOverColl = new GOverlayCollection();

                for (Iterator edgesIter = node.surroundingEdges.iterator(); edgesIter.hasNext() ; ) {
                    tmpEdge = (Edge) edgesIter.next();
                    tmpLine = new GPolyline(new GLatLng[]
                                                        { new GLatLng(tmpEdge.begin.x,tmpEdge.begin.y), new GLatLng(tmpEdge.end.x,tmpEdge.end.y) },
                                                        color);
                    mouseOverColl.add(tmpLine);
                }

                theMap.addOverlay(mouseOverColl);		              
            }
        }

        public void onMouseOut(GMarker marker) {
            if (CMF.mModPanel.clickMapBox.isChecked())
                theMap.removeOverlay(mouseOverColl);		
        }

        public void onMouseDown(GMarker marker) {}
        public void onMouseUp(GMarker marker) {}

    }


    /**
     * When the coordinates of a node are changed, changeOverlayLocs
     * (1) updates the allMarkers and allPolylines arrays to reflect the changes
     * and (2) redraws the node in question and its surrounding edges. 
     */
    private void changeOverlayLocs(boolean updateMarker) {
        boolean edgeBoxChecked = CMF.mModPanel.allEdgesBox.isChecked();
        boolean nodeBoxChecked = CMF.mModPanel.allNodesBox.isChecked();

        Edge surrEdge;
        GPolyline currentPolyline;
        GLatLng begin, end;

        // Iterate through the surrounding edges
        for (Iterator surrEdgesIter = currentNode.surroundingEdges.iterator(); surrEdgesIter.hasNext() ; ) {
            surrEdge = (Edge) surrEdgesIter.next();
            currentPolyline = (GPolyline) allPolylines.get(surrEdge);            

            // Remove old polyline/edge and its records
            theMap.removeOverlay(currentPolyline);
            allPolylines.remove(surrEdge);
            //// currentNode.surroundingEdges.remove(surrEdge);

            // Update the edge depending on whether the node is at begin or end
            if (surrEdge.begin.id == currentNode.id) {
                begin = new GLatLng(currentNode.x, currentNode.y);
                end = new GLatLng(surrEdge.end.x, surrEdge.end.y);
                surrEdge.begin = currentNode;
            } else {
                begin = new GLatLng(surrEdge.begin.x, surrEdge.begin.y);
                end = new GLatLng(currentNode.x, currentNode.y);
                surrEdge.end = currentNode;
            }

            // Add new polyline/edge to records and display it			
            currentPolyline = new GPolyline(new GLatLng[]{begin, end});
            //// currentNode.surroundingEdges.add(surrEdge);
            allPolylines.put(surrEdge, currentPolyline);
            if (edgeBoxChecked)
                theMap.addOverlay(currentPolyline);			
        }

        if (updateMarker) {
            // Remove old marker and its records
            theMap.removeOverlay(currentMarker);
            allMarkers.remove(currentMarker);

            // Add new marker to records and display it
            currentMarker.setPoint(new GLatLng(currentNode.x, currentNode.y));
            allMarkers.put(currentMarker, currentNode);
            if (nodeBoxChecked)
                theMap.addOverlay(currentMarker);
        }
    }

    /**
     * Sets up map clicklistener for Map Mod Mode
     *
     */
    /*	public void setupMapClickListener() {
		mapEventMan.addOnClickListener(theMap, CMF.mPanel.new MapClickListener());

	}
     */    
    private class MapClickListener implements GMap2EventClickListener {
        
        private GMarker tmpMarker;

        /**
         * Handles on-map clicks depending on whether it is 
         * a GOverlay or a GLatLng that has been clicked on.
         */
        public void onClick(GMap2 map, GOverlay overlay, GLatLng point) {
            boolean inMapMod = CMF.mModPanel.clickMapBox.isVisible();
            boolean enabled = CMF.mModPanel.clickMapBox.isEnabled();
            boolean checked = CMF.mModPanel.clickMapBox.isChecked();

            if (CMF.isMakingServerCall)	{ // If the previous server call had not finished yet
                CMF.message("Sorry, your last map click was not registered.  Please try again.");
                return;
            }

            if (!inMapMod && CMF.oPanel.activeRouting.isChecked()) {

                // Begin active routing!
                CMF.startSpin();

                try { // If a point on the map is clicked
                    Node closestNode = null;
                    Node tmpNode;
                    // Look through all the locations to find the one closest to point
                    for (Iterator avgNodeIter = avgNodes.iterator(); avgNodeIter.hasNext();) {
                        if (closestNode == null)
                            closestNode = (Node) avgNodeIter.next();
                        else {
                            tmpNode = (Node) avgNodeIter.next();

                            // If this tmpNode is closer to the clicked point than the previous closest node,
                            // use tmpNode as the new closest node
                            if (isCloserTo(point, new GLatLng(tmpNode.x, tmpNode.y), new GLatLng(closestNode.x, closestNode.y))) 
                                closestNode = tmpNode;
                        }      			
                    }

                    // Add the location to locationPanel
                    CMF.locPanel.addLocation(closestNode.name, false);

                } catch (NullPointerException n) {	// If a marker is clicked
                    CMF.isMakingServerCall = false;
                } finally {
                    CMF.stopSpin();
                }

                return;
            }

            if (enabled && checked) {	// In Map Mod with clicking on the map enabled
                try { // If a point on the map is clicked, add a GMarker to that point	            	
                    tmpMarker = new GMarker(point);
                    theMap.addOverlay(tmpMarker);

                    if (attrEditBoxInit)
                        attributeEditBox.hide();

                    NewNodeConfirmBox newNodeConfirmBox = new NewNodeConfirmBox();
                    newNodeConfirmBox.show();

                } catch (NullPointerException n) {}
                // Clicking on a GMarker, which is handled in onClick(final GMarker marker)  
            }

        }

        /**
         * Returns true if p1 is closer to p than p2, and false otherwise.
         * @param p 		(GLatLng)
         * @param p1		(GLatLng)
         * @param p2		(GLatLng)
         * @return boolean
         */
        private boolean isCloserTo(GLatLng p, GLatLng p1, GLatLng p2) {
            double dist1 = (p1.lat()-p.lat())*(p1.lat()-p.lat()) + (p1.lng()-p.lng())*(p1.lng()-p.lng());
            double dist2 = (p2.lat()-p.lat())*(p2.lat()-p.lat()) + (p2.lng()-p.lng())*(p2.lng()-p.lng());

            return (dist1 < dist2);
        }


        /**
         * Dialog box that pops up to confirm whether or not you
         * want to add a new node/GMarker when you click on the map.
         * 
         * @author Han
         *
         */
        private class NewNodeConfirmBox extends DialogBox {

            private final Button yes = new Button("Yes");
            private final Button no = new Button("No");

            private GMarker marker;

            public NewNodeConfirmBox() {	
                super(false,true); // Modal
                marker = tmpMarker;

                this.setText("New Node Alert");

                VerticalPanel mainPanel = new VerticalPanel();

                Label label = new Label("Do you want to add a node here?\n ");

                FlowPanel buttons = new FlowPanel();

                yes.addClickListener(new YesListener());
                no.addClickListener(new NoListener());

                buttons.add(yes);
                buttons.add(no);

                mainPanel.add(label);
                mainPanel.add(buttons);

                this.setWidget(mainPanel);
                setPopupPosition(320, 90);
            }

            private class YesListener implements ClickListener {

                public void onClick(Widget sender) {    	
                    GLatLng point = marker.getPoint();
                    Integer id = currentNodeId();
                    currentNode = new Node
                    (id + "\t" + "node" + "\t" + 0 + "\t" + point.lat() + "\t" + point.lng() + "\t" + 0);

                    // Remove the temporary newMarker and replace it with a marker that has the correct tooltip
                    theMap.removeOverlay(marker);
                    marker = makeNewMarker(point, currentNode.id, currentNode.name);  	
                    theMap.addOverlay(marker); 

                    allMarkers.put(marker, currentNode);        	
                    callUpdateNodeService(currentNode);

                    NewNodeConfirmBox.this.hide();
                }

                /**
                 * 
                 * @return the smallest unused nodeId
                 */
                private Integer currentNodeId() {
                    Integer tmpId;
                    Integer candidateId;
                    List idList = new Vector();

                    // Build a list of ids so that we can choose an unused id
                    for (Iterator nodesIter = allMarkers.keySet().iterator(); nodesIter.hasNext() ;) {            
                        tmpId = ((Node) allMarkers.get(nodesIter.next())).id;
                        idList.add(tmpId);
                    }

                    // Loop until a valid id is found; then return it
                    for (int i = 0; i > -1; i++) {
                        candidateId = new Integer(i);
                        if (!idList.contains(candidateId))
                            return candidateId;
                    }

                    return null;
                }

            }

            private class NoListener implements ClickListener {
                public void onClick(Widget sender) {
                    theMap.removeOverlay(marker);		
                    NewNodeConfirmBox.this.hide();
                }		
            }

        }

    }


    public void showNodes() {
        GOverlayCollection markerColl = new GOverlayCollection();
        for (Iterator markersIter = allMarkers.keySet().iterator(); markersIter.hasNext() ; )
            markerColl.add((GMarker) markersIter.next());
        theMap.addOverlay(markerColl);
    }

    public void showEdges() {
        GOverlayCollection polylineColl = new GOverlayCollection();
        for (Iterator polylinesIter = allPolylines.values().iterator(); polylinesIter.hasNext() ; )
            polylineColl.add((GPolyline) polylinesIter.next());
        theMap.addOverlay(polylineColl);
    }

    protected void hideNodes() {
        for (Iterator markersIter = allMarkers.keySet().iterator(); markersIter.hasNext() ; )
            theMap.removeOverlay((GMarker) markersIter.next());	
    }

    protected void hideEdges() {
        for (Iterator polylinesIter = allPolylines.values().iterator(); polylinesIter.hasNext() ; )
            theMap.removeOverlay((GPolyline) polylinesIter.next());	
    }



    /**
     * The dialog box that pops up when you click on a GMarker on the map,
     * displaying and allowing for changes to node and edge attributes.
     * Clicking "Yes" will save the changes locally and on server.
     * 
     * @author Han
     *
     */
    public class AttributeEditBox extends DialogBox {
        
        // For passing edges through UpdateEdgeService to be saved on server.  
        // Saving one edge per service call in rapid succession can cause timing/concurrency issues in file writing
        // {edge id => Edge}
        private Map edgeMap;

        // Class and saveButton being public is a result of problems with clicking saveButton quickly in succession 
        public final Button saveButton = new Button("Save changes");
        private final Button saveCloseButton = new Button("Save changes & close");
        private final Button closeButton = new Button("Close without saving");

        private NodePanel nodePanel;
        private EdgePanel edgePanel;   

        private final Label errorLabel = new Label("\n");

        public AttributeEditBox() {
            super(false,true);
            setText("Attributes Editor for Node " + currentNode.id);

            closeButton.addClickListener(new CloseListener());
            saveCloseButton.addClickListener(new SaveCloseListener());
            saveButton.addClickListener(new SaveCloseListener()); 

            closeButton.setWidth("15em");
            saveCloseButton.setWidth("15em");
            saveButton.setWidth("15em");

            VerticalPanel saveClosePanel = new VerticalPanel();
            saveClosePanel.add(errorLabel);
            saveClosePanel.add(saveButton);
            saveClosePanel.add(saveCloseButton);
            saveClosePanel.add(closeButton);

            VerticalPanel mainPanel = new VerticalPanel();
            TabPanel tPanel = new TabPanel();
            nodePanel = new NodePanel();
            edgePanel = new EdgePanel();

            tPanel.add(nodePanel, "Node");
            tPanel.add(edgePanel, "Edges");

            tPanel.selectTab(0);

            mainPanel.add(tPanel);
            mainPanel.add(saveClosePanel); 

            setPopupPosition(100, 100);
            setWidget(mainPanel);
        }

        private class CloseListener implements ClickListener {
            public void onClick(Widget sender) {
                AttributeEditBox.this.hide();
            }
        }

        private class SaveCloseListener implements ClickListener {
            public void onClick(Widget sender) {
                // Disable saveButton until a result is returned from server to UpdateEdgeCallback 
                if (sender == saveButton)
                    saveButton.setEnabled(false);

                errorLabel.setText("\n");
                try {
                    // Continue with saving and closing only if the node is saved successfully
                    if (nodePanel.saveNodePanelChanges()) {
                        edgePanel.saveEdgePanelChanges();
                        if (sender == saveCloseButton)
                            AttributeEditBox.this.hide();
                    } else {
                        errorLabel.setText("Changes were not saved! Node name cannot include & or /");
                        saveButton.setEnabled(true);	// because saveEdgePanelChanges() never was called
                    }
                }
                catch (NumberFormatException n) {
                    errorLabel.setText("Changes were not saved! Bad numerical input");
                }
            }
        }

        private class NodePanel extends Composite {

            private TextBox nameBox = new TextBox();
            private ListBox typeBox = new ListBox();  
            private TextBox latBox = new TextBox();
            private TextBox lngBox = new TextBox();
            private TextBox elevationBox = new TextBox();

            private Button getElevButton = new Button("Get elevation for the above coordinates");

            private final Label elevErrorLabel = new Label();  

            public NodePanel () {				
                nameBox.setText(currentNode.name);
                latBox.setText(Double.toString(currentNode.x));
                lngBox.setText(Double.toString(currentNode.y));
                elevationBox.setText(Double.toString(currentNode.elevation));

                typeBox.addItem("None");
                typeBox.addItem("Building");
                typeBox.addItem("Construction");
                typeBox.addItem("Place to Eat");
                typeBox.addItem("Computing Facility");
                typeBox.addItem("Library");
                typeBox.setVisibleItemCount(1);		    
                typeBox.setSelectedIndex(currentNode.type.i);

                Label nameLabel = new Label("Name:");
                Label typeLabel = new Label("Type:");
                Label latLabel = new Label("Latitude:");
                Label lngLabel = new Label("Longitude:");
                Label elevationLabel = new Label("Elevation:");

                elevErrorLabel.setVisible(false);

                getElevButton.setWidth("25em");

                getElevButton.addClickListener(new GetElevationListener());

                Grid nodeGrid = new Grid(7, 2);
                nodeGrid.setWidget(0, 0, nameLabel);
                nodeGrid.setWidget(0, 1, nameBox);
                nodeGrid.setWidget(1, 0, typeLabel);
                nodeGrid.setWidget(1, 1, typeBox); 
                nodeGrid.setWidget(2, 0, latLabel);
                nodeGrid.setWidget(2, 1, latBox);
                nodeGrid.setWidget(3, 0, lngLabel);
                nodeGrid.setWidget(3, 1, lngBox);
                nodeGrid.setWidget(4, 0, elevationLabel);
                nodeGrid.setWidget(4, 1, elevationBox);
                nodeGrid.setWidget(5, 1, getElevButton);
                nodeGrid.setWidget(6, 1, elevErrorLabel);

                initWidget(nodeGrid);
            }

            private class GetElevationListener implements ClickListener {
                private final int STATUS_CODE_OK = 200;

                public void onClick(Widget sender) {
                    elevErrorLabel.setVisible(false);

                    String elevURL;

                    elevURL = "http://gisdata.usgs.net/XMLWebServices/TNM_Elevation_Service.asmx/getElevation?X_Value="
                        + lngBox.getText().trim()
                        + "&Y_Value=" + latBox.getText().trim()
                        + "&Source_Layer=-1&Elevation_Units=FEET&Elevation_Only=true";

                    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, elevURL);

                    try {
                        builder.sendRequest(null, new ElevRequestCallback());
                    } catch (RequestException e) {
                        GWT.log("Failed to send request: " + e.getMessage(), new Throwable());
                        elevErrorLabel.setText("ERROR: Failed to send elevation request!");
                        elevErrorLabel.setVisible(true);
                    }
                }

                private class ElevRequestCallback implements RequestCallback {

                    public void onError(Request request, Throwable exception) {
                        if (exception instanceof RequestTimeoutException) {
                            // handle a request timeout
                            GWT.log("Elevation request timed out.", new Throwable());
                            elevErrorLabel.setText("ERROR: Elevation request timed out!");
                        } else {
                            // handle other request errors
                            GWT.log("Elevation request failed.", new Throwable());
                            elevErrorLabel.setText("ERROR: Elevation request failed!");
                        }					    
                        elevErrorLabel.setVisible(true);
                    }

                    public void onResponseReceived(Request request, Response response) {
                        if (STATUS_CODE_OK == response.getStatusCode()) {
                            // handle OK response from the server
                            // parse the response for the elevation
                            elevationBox.setText(response.getText().split(">")[2].split("<")[0]);
                        } else {
                            // handle non-OK response from the server
                            GWT.log("Bad response from server.", new Throwable());
                            elevErrorLabel.setText("ERROR: Bad response from the requested server!");
                            elevErrorLabel.setVisible(true);
                        }
                    }				    

                }

            }

            protected boolean saveNodePanelChanges() {
                elevErrorLabel.setVisible(false);

                double oldLat = currentNode.x;
                double oldLng = currentNode.y;
                String oldName = currentNode.name;

                if (invalidNodeName(nameBox.getText())) 
                    return false;

                currentNode.name = nameBox.getText();
                currentNode.type.i = typeBox.getSelectedIndex();
                currentNode.x = Double.valueOf(latBox.getText().trim()).doubleValue();
                currentNode.y = Double.valueOf(lngBox.getText().trim()).doubleValue();
                currentNode.elevation = Double.valueOf(elevationBox.getText().trim()).doubleValue();

                callUpdateNodeService(currentNode);

                // If the node name changes, change the tooltip (by making a new marker)
                if (!currentNode.name.equals(oldName)) {
                    allMarkers.remove(currentMarker);
                    theMap.removeOverlay(currentMarker);

                    currentMarker = makeNewMarker(currentMarker.getPoint(), currentNode.id, currentNode.name);      	
                    theMap.addOverlay(currentMarker);			
                }

                // If the coordinates change, move the marker and the surrounding polylines
                if (currentNode.x != oldLat || currentNode.y != oldLng) {
                    changeOverlayLocs(true);
                } else 
                    allMarkers.put(currentMarker, currentNode);

                return true;
            }

            /**
             * 
             * @param nodeName String
             * @return true if the nodeName contains either "&" or "/", and false otherwise
             */
            private boolean invalidNodeName(String nodeName) {
                return (nodeName.indexOf("&") != -1 || nodeName.indexOf("/") != -1);
            }

        }


        private class EdgePanel extends Composite {

            private final Button addEdgeButton = new Button("Add new edge to this node:");
            private final TextBox addEdgeTextBox = new TextBox(); 				
            private VerticalPanel edgeStack;
            private EdgeEntry currentEdge;
            private final Label addEdgeErrorLabel = new Label("Bad node id input. Please try again.");
            private boolean introText = true;

            public EdgePanel() {			
                Label edgesLabel = new Label("Edges that surround this node:");

                addEdgeTextBox.setText("Enter the node id here");
                AddEdgeListener addEdgeListener = new AddEdgeListener();
                addEdgeTextBox.addClickListener(addEdgeListener);
                addEdgeTextBox.addKeyboardListener(addEdgeListener);
                addEdgeButton.addClickListener(addEdgeListener);

                addEdgeErrorLabel.setVisible(false);

                VerticalPanel mainPanel = new VerticalPanel();
                edgeStack = new VerticalPanel();

                for (Iterator surrEdgeIter = currentNode.surroundingEdges.iterator(); surrEdgeIter.hasNext(); ) {       	
                    // Create a widget for displaying and modifying the contents of each edge 
                    addEdgeToEdgePanel((Edge) surrEdgeIter.next());        	
                }

                mainPanel.add(edgesLabel);
                mainPanel.add(edgeStack);  
                mainPanel.add(addEdgeButton);
                mainPanel.add(addEdgeTextBox);
                mainPanel.add(addEdgeErrorLabel);       

                initWidget(mainPanel);
            }

            private void addEdgeToEdgePanel(Edge edge) {
                EdgeEntry edgeEntry = new EdgeEntry(edge);

                HorizontalPanel edgeLine = new HorizontalPanel();
                Button deleteButton = new Button("Delete", new DeleteEdgeListener());
                edgeLine.add(edgeEntry);
                edgeLine.add(deleteButton);

                edgeStack.add(edgeLine);
            }

            public void saveEdgePanelChanges() {
                int numEdges = edgeStack.getWidgetCount();

                edgeMap = new HashMap();

                for (int i = 0; i < numEdges; i++) {
                    currentEdge = (EdgeEntry) ((HorizontalPanel) edgeStack.getWidget(i)).getWidget(0);
                    currentEdge.saveEdgeEntryChanges();
                }

                callUpdateEdgeService(false);
            }

            private class DeleteEdgeListener implements ClickListener {

                public void onClick(Widget sender) {
                    HorizontalPanel tmpHPanel = (HorizontalPanel) ((Button) sender).getParent();
                    currentEdge = (EdgeEntry) tmpHPanel.getWidget(0);
                    if (allPolylines.get(currentEdge.edge) == null)
                    {
                        // EdgeEntry to be deleted has not yet been saved
                        VerticalPanel tmpEdgeStack = (VerticalPanel) tmpHPanel.getParent();
                        tmpEdgeStack.remove(tmpHPanel);
                    } else {
                        // EdgeEntry to be deleted has been saved
                        DeleteEdgeConfirmBox delEdgeConfirmBox = new DeleteEdgeConfirmBox();
                        delEdgeConfirmBox.show();
                    }
                }

                private class DeleteEdgeConfirmBox extends DialogBox {

                    private final Button yes = new Button("Yes");
                    private final Button no = new Button("No");

                    DeleteEdgeConfirmBox() {		
                        super(false,true); // Modal
                        setText("Delete Edge Alert");

                        VerticalPanel mainPanel = new VerticalPanel();

                        Label label = new Label("Are you sure you want to delete this edge?\n" +
                        "Changes will be saved immediately if you click 'Yes'\n ");

                        FlowPanel buttons = new FlowPanel();

                        yes.addClickListener(new YesListener());
                        no.addClickListener(new NoListener());

                        buttons.add(yes);
                        buttons.add(no);

                        mainPanel.add(label);
                        mainPanel.add(buttons);

                        add(mainPanel);
                    }

                    private class YesListener implements ClickListener {
                        public void onClick(Widget sender) {
                            Edge tmpEdge = currentEdge.edge;

                            // Remove the entry from the attributes editor
                            edgeStack.remove((HorizontalPanel) currentEdge.getParent()); 

                            // Remove the overlay and the hashmap entry
                            theMap.removeOverlay((GPolyline) allPolylines.get(tmpEdge));
                            allPolylines.remove(tmpEdge);

                            // Remove from surroudingEdge sets of begin and end nodes
                            tmpEdge.begin.surroundingEdges.remove(tmpEdge);
                            tmpEdge.end.surroundingEdges.remove(tmpEdge);

                            // Remove from server edge file
                            edgeMap = new HashMap();
                            edgeMap.put(tmpEdge.id, tmpEdge);
                            callUpdateEdgeService(true);

                            DeleteEdgeConfirmBox.this.hide();
                        }
                    }

                    private class NoListener implements ClickListener {
                        public void onClick(Widget sender) {							
                            DeleteEdgeConfirmBox.this.hide();
                        }				
                    }

                }

            }

            private class AddEdgeListener implements ClickListener, KeyboardListener {

                public void onClick(Widget sender) {
                    if (sender == addEdgeButton)
                        addEdgeFromTextBox();
                    else if (sender == addEdgeTextBox)
                        if (introText) {
                            addEdgeTextBox.setText("");
                            introText = false;
                        }
                }

                public void onKeyDown(Widget sender, char keyCode, int modifiers) {
                    if ((keyCode == KeyboardListener.KEY_ENTER)) 
                        addEdgeFromTextBox();				
                }

                private void addEdgeFromTextBox() {
                    addEdgeErrorLabel.setVisible(false);
                    try {
                        Integer endNodeId =  new Integer(addEdgeTextBox.getText().trim()); 
                        Node endNode = getNodeForId(endNodeId);

                        if (!isValidEndNode(endNode)) {
                            addEdgeErrorLabel.setVisible(true);
                        } else {
                            String optionsLine = newEdgeId() + "\t" + currentNode.id + "\t" + endNode.id + 
                            "\tT\tF\tT\tF\t" + 0 + "\t" + 0;

                            addEdgeToEdgePanel(new Edge(currentNode, endNode, optionsLine));

                            addEdgeTextBox.setText("");
                        }
                    } catch (NullPointerException n) {
                        addEdgeErrorLabel.setVisible(true);
                    } catch (NumberFormatException n) {
                        addEdgeErrorLabel.setVisible(true);
                    }			
                }

                /**
                 * Checks to see if any of the following cases is true:
                 * 		* endNode matches the beginNode
                 * 		* an edge already exists from beginNode to endNode either...
                 * 			* on client- and server-side, or
                 * 			* only having just been created and not yet saved
                 * If any of these cases is true, the endNode is deemed to be invalid,
                 * and we return false. Otherwise, return true.
                 * 		
                 * @param endNode
                 * @return
                 */
                private boolean isValidEndNode(Node endNode) {
                    Edge tmpEdge;
                    if (currentNode.id == endNode.id)
                        return false;
                    for (Iterator surrEdges = currentNode.surroundingEdges.iterator(); surrEdges.hasNext();) {
                        tmpEdge = (Edge) surrEdges.next();
                        if (tmpEdge.begin.id == endNode.id || tmpEdge.end.id == endNode.id)
                            return false;
                    }
                    for (int i = 0; i < edgeStack.getWidgetCount(); i++) {
                        tmpEdge = ((EdgeEntry) ((HorizontalPanel) edgeStack.getWidget(i)).getWidget(0)).edge;
                        if (tmpEdge.begin.id == endNode.id || tmpEdge.end.id == endNode.id)
                            return false;
                    }

                    return true;
                }

                /**
                 * Loop through all nodes to find the node with the id in question
                 * @param id
                 * @return node with node.id==id
                 */
                private Node getNodeForId(Integer id) {
                    Node tmpNode;

                    for (Iterator nodesIter = allMarkers.values().iterator(); nodesIter.hasNext() ; ) {            
                        tmpNode = (Node) nodesIter.next();
                        if (tmpNode.id.equals(id))
                            return tmpNode;
                    }
                    return null;
                }

                /**
                 * 
                 * @return the smallest unused edgeId
                 */
                private Integer newEdgeId() {
                    Integer tmpId;
                    Integer candidateId;
                    List idList = new Vector();

                    // Build a list of ids so that we can choose an unused id
                    for (Iterator edgesIter = allPolylines.keySet().iterator(); edgesIter.hasNext() ;) {            
                        tmpId = ((Edge) edgesIter.next()).id;
                        idList.add(tmpId);
                    }

                    // Add just-added (but not yet saved) edges to the list
                    for (int i = 0; i < edgeStack.getWidgetCount(); i++)
                        idList.add(((EdgeEntry) ((HorizontalPanel) edgeStack.getWidget(i)).getWidget(0)).edge.id);


                    // Loop until a valid id is found; then return it
                    for (int i = 0; i > -1; i++) {
                        candidateId = new Integer(i);
                        if (!idList.contains(candidateId))
                            return candidateId;
                    }

                    return null;
                }

                public void onKeyPress(Widget sender, char keyCode, int modifiers) {}
                public void onKeyUp(Widget sender, char keyCode, int modifiers) {}

            }    



            private class EdgeEntry extends Composite {

                private TextBox beginIdBox = new TextBox();
                private TextBox endIdBox = new TextBox();
                private ListBox crowdRatingBox = new ListBox();
                private ListBox scenicRatingBox = new ListBox();

                private CheckBox isMainPathBox;
                private CheckBox hasStairsBox;
                private CheckBox bikesAllowedBox;
                private CheckBox thruBuildingBox;

                private Edge edge;

                private DisclosurePanel ddPanel;

                public EdgeEntry(final Edge edge) {
                    this.edge = edge;

                    Integer beginId = edge.begin.id;
                    Integer endId = edge.end.id;   	
                    boolean isMainPath = edge.isMainPath;
                    boolean hasStairs = edge.hasStairs;
                    boolean bikesAllowed = edge.bikesAllowed;
                    boolean thruBuilding = edge.thruBuilding;
                    int crowdRating = edge.crowdRating;
                    int scenicRating = edge.scenicRating;

                    beginIdBox.setText(beginId.toString());
                    endIdBox.setText(endId.toString());

                    crowdRatingBox.addItem("0");
                    crowdRatingBox.addItem("1");
                    crowdRatingBox.addItem("2");
                    crowdRatingBox.addItem("3");
                    crowdRatingBox.addItem("4");
                    crowdRatingBox.addItem("5");
                    crowdRatingBox.setVisibleItemCount(1);		    
                    crowdRatingBox.setSelectedIndex(crowdRating);

                    scenicRatingBox.addItem("0");
                    scenicRatingBox.addItem("1");
                    scenicRatingBox.addItem("2");
                    scenicRatingBox.addItem("3");
                    scenicRatingBox.addItem("4");
                    scenicRatingBox.addItem("5");
                    scenicRatingBox.setVisibleItemCount(1);		    
                    scenicRatingBox.setSelectedIndex(scenicRating);

                    beginIdBox.setEnabled(false);
                    endIdBox.setEnabled(false);

                    Label edgeLabel = new Label("Edit this edge:");      
                    Label beginIdLabel = new Label("Begin node:");
                    Label endIdLabel = new Label("End node:");
                    Label isMainPathLabel = new Label("isMainPath:");
                    Label hasStairsLabel = new Label("hasOutdoorStairs:");
                    Label bikesAllowedLabel = new Label("bikesAllowed:");
                    Label thruBuildingLabel = new Label("thruBuilding:");
                    Label crowdRatingLabel = new Label("crowdRating:");
                    Label scenicRatingLabel = new Label("scenicRating:");

                    isMainPathBox = new CheckBox();
                    hasStairsBox = new CheckBox();
                    bikesAllowedBox = new CheckBox();
                    thruBuildingBox = new CheckBox();

                    isMainPathBox.setChecked(isMainPath);     
                    hasStairsBox.setChecked(hasStairs);     
                    bikesAllowedBox.setChecked(bikesAllowed);
                    thruBuildingBox.setChecked(thruBuilding);

                    String begin = edge.begin.id + ": " + edge.begin.name;
                    String end = edge.end.id + ": " + edge.end.name;
                    Label header = new Label("Edge " + edge.id + " from " + begin + " to " + end);

                    ddPanel = new DisclosurePanel();

                    Grid edgeGrid = new Grid(9, 2);
                    edgeGrid.setWidget(0, 0, edgeLabel);
                    edgeGrid.setWidget(1, 0, beginIdLabel);
                    edgeGrid.setWidget(1, 1, beginIdBox);
                    edgeGrid.setWidget(2, 0, endIdLabel);
                    edgeGrid.setWidget(2, 1, endIdBox);
                    edgeGrid.setWidget(3, 0, isMainPathLabel);
                    edgeGrid.setWidget(3, 1, isMainPathBox);
                    edgeGrid.setWidget(4, 0, hasStairsLabel);
                    edgeGrid.setWidget(4, 1, hasStairsBox);
                    edgeGrid.setWidget(5, 0, bikesAllowedLabel);
                    edgeGrid.setWidget(5, 1, bikesAllowedBox);
                    edgeGrid.setWidget(6, 0, thruBuildingLabel);
                    edgeGrid.setWidget(6, 1, thruBuildingBox);
                    edgeGrid.setWidget(7, 0, crowdRatingLabel);
                    edgeGrid.setWidget(7, 1, crowdRatingBox);
                    edgeGrid.setWidget(8, 0, scenicRatingLabel);
                    edgeGrid.setWidget(8, 1, scenicRatingBox);

                    ddPanel.add(edgeGrid);
                    ddPanel.setHeader(header);
                    ddPanel.addEventHandler(new EdgeDropDownListener());

                    initWidget(ddPanel);
                }

                protected void saveEdgeEntryChanges() {
                    GPolyline currentLine = (GPolyline) allPolylines.get(edge);
                    allPolylines.remove(edge);

                    edge.isMainPath = isMainPathBox.isChecked();
                    edge.hasStairs = hasStairsBox.isChecked();
                    edge.bikesAllowed = bikesAllowedBox.isChecked();
                    edge.thruBuilding = thruBuildingBox.isChecked();
                    edge.crowdRating = crowdRatingBox.getSelectedIndex();
                    edge.scenicRating = scenicRatingBox.getSelectedIndex();								

                    // If there is no polyline for this edge yet(that is, if this edge is newly created), add a polyline
                    if (currentLine == null) {
                        currentLine = new GPolyline(new GLatLng[]{new GLatLng(edge.begin.x, edge.begin.y), new GLatLng(edge.end.x, edge.end.y)});
                        if (CMF.mModPanel.allEdgesBox.isChecked())
                            theMap.addOverlay(currentLine);
                        edge.begin.surroundingEdges.add(edge);
                        edge.end.surroundingEdges.add(edge);
                    }

                    allPolylines.put(edge, currentLine);				
                    edgeMap.put(edge.id, edge);			
                }

                /**
                 * When an edge entry is dropped down, the edge
                 * is highlighted on the map.
                 *
                 */
                private class EdgeDropDownListener implements DisclosureHandler {

                    GPolyline line;

                    public void onClose(DisclosureEvent sender) {
                        theMap.removeOverlay(line);
                    }

                    public void onOpen(DisclosureEvent sender) {
                        for (int i = 0; i < edgeStack.getWidgetCount(); i++) {
                            EdgeEntry tmpEntry = (EdgeEntry) ((HorizontalPanel) edgeStack.getWidget(i)).getWidget(0);
                            if (tmpEntry.edge.id != edge.id)
                                tmpEntry.ddPanel.setOpen(false);
                        }

                        String color = (CMF.mModPanel.allEdgesBox.isChecked()) ? SHADER : BLUE;
                        line = new GPolyline(new GLatLng[]
                                                         { new GLatLng(edge.begin.x,edge.begin.y), new GLatLng(edge.end.x,edge.end.y) },
                                                         color);
                        theMap.addOverlay(line);
                        currentNodeColl.add(line);
                    }

                }

            }

        }

    }

    /**
     * Mouse Wheel Listener, for Jerjou
     */
    private class MapWheelZoom implements MouseWheelListener {
        public void onMouseWheel(Widget sender, MouseWheelVelocity velocity) {
            // Don't do anything if we're in MapMod mode
            if (CMF.mModPanel.clickMapBox.isVisible()) return;

            if (velocity.isNorth()) {
                theMap.setZoom(theMap.getZoom()+1);
            }
            else if (velocity.isSouth()) {
                theMap.setZoom(theMap.getZoom()-1);
            }
        }
    }
}
