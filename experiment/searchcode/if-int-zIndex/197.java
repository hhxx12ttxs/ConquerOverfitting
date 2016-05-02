package org.gwtopenmaps.openlayers.client.layer;

import org.gwtopenmaps.openlayers.client.util.JSObject;
import org.gwtopenmaps.openlayers.client.util.JSObjectJFX;


/**
 *
 * @author Erdem Gunay
 *         Amr Alam - Refractions Research
 *         Edwin Commandeur - Atlis EJS
 *
 * @author Nazzareno Sileno - CNR IMAA geoSDI Group - @email nazzareno.sileno@geosdi.org
 */
class LayerImplJFX implements LayerImpl
{

    public  JSObject clone(JSObject obj) {
//        return obj.clone();
    	
    	throw new UnsupportedOperationException();
    };
    
    public  boolean redraw(JSObject layer, boolean force) {
//        return layer.redraw(force);
    	
    	throw new UnsupportedOperationException();
    };

    public  void setIsBaseLayer(boolean isBaseLayer, JSObject layer) {
        ((JSObjectJFX) layer).getJFXObject().call("setIsBaseLayer", isBaseLayer);
    };

    public  boolean isBaseLayer(JSObject layer) {
//        return layer.isBaseLayer;
    	
    	throw new UnsupportedOperationException();
    };

    public  String getId(JSObject layer) {
//        return layer.id;
    	
    	throw new UnsupportedOperationException();
    };

    public  float getOpacity(JSObject layer) {
//        if(layer.opacity){ return layer.opacity }else{ return 1.0 };
    	
    	throw new UnsupportedOperationException();
    };

    public  void setOpacity(float opacity, JSObject layer) {
//        layer.setOpacity(opacity);
    	
    	throw new UnsupportedOperationException();
    };

    public  boolean displayInLayerSwitcher(JSObject layer) {
//        return layer.displayInLayerSwitcher;
    	
    	throw new UnsupportedOperationException();
    };

    public  void setDisplayInLayerSwitcher(boolean display, JSObject layer) {
//        layer.displayInLayerSwitcher = display;
    	
    	throw new UnsupportedOperationException();
    };

    public  String getName(JSObject layer) {
//        if(layer.name){ return layer.name }else{ return "" };
    	
    	throw new UnsupportedOperationException();
    };

    public  void setName(String name, JSObject layer) {
//        layer.setName(name);
    	
    	throw new UnsupportedOperationException();
    };

    public  boolean isVisible(JSObject layer) {
//        return layer.getVisibility();
    	
    	throw new UnsupportedOperationException();
    };

    public  void setIsVisible(boolean isVisible, JSObject layer) {
//        layer.setVisibility(isVisible);
    	
    	throw new UnsupportedOperationException();
    };

    public  void setZIndex(JSObject layer, int zIndex) {
//        layer.setZIndex(zIndex);
    	
    	throw new UnsupportedOperationException();
    };

    public  Object getZIndex(JSObject layer) {
//        return layer.getZIndex();
    	
    	throw new UnsupportedOperationException();
    };

    public  String getUnits(JSObject layer) {
//        if(layer.units){ return layer.units }else{ return ""};
    	
    	throw new UnsupportedOperationException();
    };

    public  void addOptions(JSObject layer, JSObject layerOptions) {
//        layer.addOptions(layerOptions);
    	
    	throw new UnsupportedOperationException();
    };

    public  void destroy(JSObject layer, boolean setNewBaseLayer) {
//        layer.destroy(setNewBaseLayer);
    	
    	throw new UnsupportedOperationException();
    };
    
    public  double getResolutionForZoom(JSObject layer, double zoom) {
//        return layer.getResolutionForZoom(zoom);
    	
    	throw new UnsupportedOperationException();
    };
    
    public  JSObject getProjection(JSObject layer) {
//       return layer.projection;
    	
    	throw new UnsupportedOperationException();
    };
    
    public  JSObject getOptions(JSObject layer) {
//       if( layer.options ) 		
//            return layer.options;
//       else 
//            return {};
    	
    	throw new UnsupportedOperationException();
    };
}

