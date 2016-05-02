package org.gwtopenmaps.openlayers.client.layer;

import org.gwtopenmaps.openlayers.client.util.JSObject;


/**
 *
 * @author Erdem Gunay
 *         Amr Alam - Refractions Research
 *         Edwin Commandeur - Atlis EJS
 *
 * @author Nazzareno Sileno - CNR IMAA geoSDI Group - @email nazzareno.sileno@geosdi.org
 */
public interface LayerImpl
{
	LayerImpl IMPL = new LayerImplJFX();
	
    public  JSObject clone(JSObject obj) /*-{
        return obj.clone();
    }-*/;
    
    public  boolean redraw(JSObject layer, boolean force) /*-{
        return layer.redraw(force);
    }-*/;

    public  void setIsBaseLayer(boolean isBaseLayer, JSObject layer) /*-{
        layer.setIsBaseLayer(isBaseLayer);
    }-*/;

    public  boolean isBaseLayer(JSObject layer) /*-{
        return layer.isBaseLayer;
    }-*/;

    public  String getId(JSObject layer) /*-{
        return layer.id;
    }-*/;

    public  float getOpacity(JSObject layer) /*-{
        if(layer.opacity){ return layer.opacity }else{ return 1.0 };
    }-*/;

    public  void setOpacity(float opacity, JSObject layer) /*-{
        layer.setOpacity(opacity);
    }-*/;

    public  boolean displayInLayerSwitcher(JSObject layer) /*-{
        return layer.displayInLayerSwitcher;
    }-*/;

    public  void setDisplayInLayerSwitcher(boolean display, JSObject layer) /*-{
        layer.displayInLayerSwitcher = display;
    }-*/;

    public  String getName(JSObject layer) /*-{
        if(layer.name){ return layer.name }else{ return "" };
    }-*/;

    public  void setName(String name, JSObject layer) /*-{
        layer.setName(name);
    }-*/;

    public  boolean isVisible(JSObject layer) /*-{
        return layer.getVisibility();
    }-*/;

    public  void setIsVisible(boolean isVisible, JSObject layer) /*-{
        layer.setVisibility(isVisible);
    }-*/;

    public  void setZIndex(JSObject layer, int zIndex) /*-{
        layer.setZIndex(zIndex);
    }-*/;

    public  Object getZIndex(JSObject layer) /*-{
        return layer.getZIndex();
    }-*/;

    public  String getUnits(JSObject layer) /*-{
        if(layer.units){ return layer.units }else{ return ""};
    }-*/;

    public  void addOptions(JSObject layer, JSObject layerOptions) /*-{
        layer.addOptions(layerOptions);
    }-*/;

    public  void destroy(JSObject layer, boolean setNewBaseLayer) /*-{
        layer.destroy(setNewBaseLayer);
    }-*/;
    
    public  double getResolutionForZoom(JSObject layer, double zoom) /*-{
        return layer.getResolutionForZoom(zoom);
    }-*/;
    
    public  JSObject getProjection(JSObject layer) /*-{
       return layer.projection;
    }-*/;
    
    public  JSObject getOptions(JSObject layer) /*-{
       if( layer.options ) 		
            return layer.options;
       else 
            return {};
    }-*/;
}

