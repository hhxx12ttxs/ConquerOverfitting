/* gvSIG. Sistema de Informaci?n Geogr?fica de la Generalitat Valenciana
 *
 * Copyright (C) 2004 IVER T.I. and Generalitat Valenciana.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 *
 * For more information, contact:
 *
 *  Generalitat Valenciana
 *   Conselleria d'Infraestructures i Transport
 *   Av. Blasco Ib??ez, 50
 *   46010 VALENCIA
 *   SPAIN
 *
 *      +34 963862235
 *   gvsig@gva.es
 *      www.gvsig.gva.es
 *
 *    or
 *
 *   IVER T.I. S.A
 *   Salamanca 50
 *   46005 Valencia
 *   Spain
 *
 *   +34 963163400
 *   dac@iver.es
 */
package com.iver.cit.gvsig.fmap.layers;

import java.awt.Image;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.cresques.cts.ICoordTrans;
import org.cresques.cts.IProjection;
import org.gvsig.exceptions.BaseException;

import com.hardcode.gdbms.engine.data.driver.DriverException;
import com.iver.cit.gvsig.exceptions.layers.LoadLayerException;
import com.iver.cit.gvsig.exceptions.layers.ReloadLayerException;
import com.iver.cit.gvsig.exceptions.layers.StartEditionLayerException;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.crs.CRSFactory;
import com.iver.cit.gvsig.fmap.layers.layerOperations.ComposedLayer;
import com.iver.cit.gvsig.fmap.operations.strategies.Strategy;
import com.iver.cit.gvsig.fmap.rendering.LegendListener;
import com.iver.utiles.IPersistence;
import com.iver.utiles.XMLEntity;

/**
 * <p>Implementation of the common characteristics of all layers: visibility, activation, name, ...</p>
 *
 * <p>Represents the definition of a basic layer, implementing {@link FLayer FLayer}, and new functionality:
 * <ul>
 *  <li>Supports transparency.
 *  <li>Notification of evens produced using this layer.
 *  <li>Can have internal virtual layers.
 *  <li>Can have a text layer.
 *  <li>Supports an strategy for visit its geometries.
 *  <li>Can have an image in the <i>TOC (table of contents)</i> associated to the state of this layer.
 * </ul>
 * </p>
 *
 * <p>Each graphical layer will inherit from this class and adapt to its particular logic and model according
 *  its nature.</p>
 *
 * @see FLayer
 * @see FLayerStatus
 */
public abstract class FLyrDefault implements FLayer, LayerListener {
	// private PropertyChangeSupport lnkPropertyChangeSupport;
	/**
	 * Useful for debug the problems during the implementation.
	 */
	private static Logger logger = Logger.getLogger(FLyrDefault.class);
	private LayerChangeSupport layerChangeSupport = new LayerChangeSupport();

	/**
	 * Path to the upper layer which this layer belongs.
	 *
	 * @see #getParentLayer()
	 * @see #setParentLayer(FLayers)
	 */
	private FLayers parentLayer = null;

	/**
	 * A node in the tree of layers. Isn't used.
	 *
	 * @see #getVirtualLayers()
	 * @see #setVirtualLayers(FLayers)
	 */
	private FLayers virtualLayers = null;

	/**
	 * Name for this layer, this also will be a property in the XML entity that represents this layer.
	 *
	 * @see #getName()
	 * @see #setName(String)
	 */
	private String name;

	/**
	 * Projection for this layer.
	 *
	 * @see #getProjection()
	 * @see #setProjection(IProjection)
	 */
	private IProjection projection;

	/**
	 * Transparency level of this layer in the range 0-255. By default 255.
	 * 0   --> Transparent
	 * 255 --> Opaque
	 *
	 * @see #getTransparency()
	 * @see #setTransparency(int)
	 */
	private int transparency = 255;

	/**
	 * Coordinate transformation.
	 *
	 * @see #getCoordTrans()
	 * @see #setCoordTrans(ICoordTrans)
	 */
	private ICoordTrans ct;

	/**
	 * Minimum scale, >= 0 or -1 if not defined. By default -1.
	 *
	 * @see #getMinScale()
	 * @see #setMinScale(double)
	 */
	private double minScale = -1; // -1 indica que no se usa

	/**
	 * Maximum scale, >= 0 or -1 if not defined. By default -1.
	 *
	 * @see #getMaxScale()
	 * @see #setMaxScale(double)
	 */
	private double maxScale = -1;
//	private boolean isInTOC = true;

	/**
	 * Array list with all listeners registered to this layer.
	 *
	 * @see #getLayerListeners()
	 * @see #setLayerText(FLyrText)
	 * @see #removeLayerListener(LayerListener)
	 * @see #callEditionChanged(LayerEvent)
	 */
	protected ArrayList layerListeners = new ArrayList();

	/**
	 * Strategy of drawing and processing for this layer.
	 *
	 * @see #getStrategy()
	 * @see #setStrategy(Strategy)
	 */
	private Strategy privateStrategy = null;

	/**
	 * Hash table with the extended properties of this layer.
	 *
	 * @see #getProperty(Object)
	 * @see #setProperty(Object, Object)
	 * @see #getExtendedProperties()
	 */
	private Hashtable properties = new Hashtable();

	//by default, all is active, visible and avalaible
	/**
	 * Status of this layer.
	 *
	 * @see #getFLayerStatus()
	 * @see #setFLayerStatus(FLayerStatus)
	 * @see #isActive()
	 * @see #setActive(boolean)
	 * @see #isVisible()
	 * @see #setVisible(boolean)
	 * @see #visibleRequired()
	 * @see #isEditing()
	 * @see #setEditing(boolean)
	 * @see #isInTOC()
	 * @see #isCachingDrawnLayers()
	 * @see #setCachingDrawnLayers(boolean)
	 * @see #isDirty()
	 * @see #setDirty(boolean)
	 * @see #isAvailable()
	 * @see #setAvailable(boolean)
	 * @see #isOk()
	 * @see #isWritable()
	 * @see #getNumErrors()
	 * @see #getError(int)
	 * @see #getErrors()
	 * @see #addError(DriverException)
	 */
	private FLayerStatus status = new FLayerStatus();
	/**
	 * Image drawn shown in the TOC according the status of this layer.
	 *
	 * @see #getTocStatusImage()
	 * @see #setTocStatusImage(Image)
	 */
	private Image tocStatusImage;


	/**
	 * Draw version of the context. It's used for know when de componend has
	 * changed any visualization property
	 *
	 *  @see getDrawVersion
	 *  @see updateDrawVersion
	 */
	private long drawVersion= 0L;

	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#getProperty(java.lang.Object)
	 */
	public Object getProperty(Object key) {
		return properties.get(key);
	}
	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#setProperty(java.lang.Object, java.lang.Object)
	 */
	public void setProperty(Object key, Object val) {
		properties.put(key, val);
	}
	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#getExtendedProperties()
	 */
	public Map getExtendedProperties() {
		return properties;
	}
	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#setActive(boolean)
	 */
	public void setActive(boolean selected) {
		//active = selected;
		status.active = selected;
		callActivationChanged(LayerEvent.createActivationChangedEvent(this,
		"active"));
	}

	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#isActive()
	 */
	public boolean isActive() {
//		return active;
		return status.active;
	}

	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
		callNameChanged(LayerEvent.createNameChangedEvent(this, "name"));
	}

	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#load()
	 */
	public void load() throws LoadLayerException {
	}

	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#setVisible(boolean)
	 */
	public void setVisible(boolean visibility) {
		if (status.visible != visibility){
			status.visible = visibility;
			this.updateDrawVersion();

//			if (this.getMapContext() != null){
//				this.getMapContext().clearAllCachingImageDrawnLayers();
//			}
			callVisibilityChanged(LayerEvent.createVisibilityChangedEvent(this,
			"visible"));

		}
	}


	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#isVisible()
	 */
	public boolean isVisible() {
//		return visible && this.available;
		return status.visible && status.available;
	}

	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#getParentLayer()
	 */
	public FLayers getParentLayer() {
		return parentLayer;
	}


	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#setParentLayer(com.iver.cit.gvsig.fmap.layers.FLayers)
	 */
	public void setParentLayer(FLayers root) {
		if (this.parentLayer != root){
			this.parentLayer = root;
			this.updateDrawVersion();
		}
	}

	/**
	 * <p>Inserts the projection to this layer.</p>
	 *
	 * @param proj information about the new projection
	 *
	 * @see #isReprojectable()
	 * @see #reProject(MapControl)
	 */
	public void setProjection(IProjection proj) {
		if (this.projection == proj)
			return;
		if (this.projection != null && this.projection.equals(proj)){
			return;
		}
		projection = proj;
		this.updateDrawVersion();

		// Comprobar que la proyecci?n es la misma que la de FMap
		// Si no lo es, es una capa que est? reproyectada al vuelo
		if ((proj != null) && (getMapContext() != null))
			if (proj != getMapContext().getProjection()) {
				ICoordTrans ct = proj.getCT(getMapContext().getProjection());
				setCoordTrans(ct);
				logger.debug("Cambio proyecci?n: FMap con "
						+ getMapContext().getProjection().getAbrev() + " y capa "
						+ getName() + " con " + proj.getAbrev());
			}
	}

	/*
	 * (non-Javadoc)
	 * @see org.cresques.geo.Projected#getProjection()
	 */
	public IProjection getProjection() {
		return projection;
	}

	/**
	 * <p>Changes the projection of this layer.</p>
	 * <p>This method will be overloaded in each kind of layer, according its specific nature.</p>
	 *
	 * @param mapC <code>MapControl</code> instance that will reproject this layer
	 *
	 * @return <code>true<code> if the layer has been created calling {@link FLayers#addLayer(FLayer) FLayers#addLayer}. But returns <code>false</code>
	 *  if the load control logic of this layer is in the reprojection method
	 *
	 * @see #isReprojectable()
	 * @see #setProjection(IProjection)
	 */
	public void reProject(ICoordTrans arg0) {
	}

	/**
	 * Returns the transparency level of this layer, in the range 0-255 .
	 *
	 * @return the transparency level
	 *
	 * @see #setTransparency(int)
	 */
	public int getTransparency() {
		return transparency;
	}

	/**
	 * Inserts the transparency level for this layer, the range allowed is 0-255 .
	 *
	 * @param trans the transparency level
	 *
	 * @see #getTransparency()
	 */
	public void setTransparency(int trans) {
		if (this.transparency != trans){
			transparency = trans;
			this.updateDrawVersion();
		}
	}
	/**
	 * <p>Returns an entity that represents this layer.</p>
	 *
	 * <p>This XML entity has elements (properties) that represent and store information about this layer.</p>
	 *
	 * <p>There are two kinds of information: default properties of this layer, and extended properties (they added that weren't by default)</p>
	 *
	 * <p> <b>Default properties:</b>
	 *  <ul>
	 *   <li> className : name of this class
	 *   <li> active : if this layer is active or not
	 *   <li> name : name of this layer
	 *   <li> minScale : minimum scale of this layer
	 *   <li> maxScale : maximum scale of this layer
	 *   <li> visible : if this layer is visible or not
	 *   <li> proj : the projection of this layer (only if it's defined)
	 *   <li> transparency : transparency level of this layer
	 *   <li> isInTOC : if this layer is in the TOC or not
	 *  </ul>
	 * </p>
	 *
	 * <p> <b>Extended properties:</b> are stored as children of the tree-node returned. There are two kinds of information for a child,
	 *  according if it's an instance of an <code>String</code> or of an object that implements the interface <code>IPersistance</code>.
	 *
	 *  <ul>
	 *   <li> <i>Instance of <code>String</code>:</i>
	 *   <ul>
	 *    <li> className : name of the class of the object that it's the property
	 *    <li> value : value of the property
	 *    <li> layerPropertyName : name of the extended property of the layer
	 *   </ul>
	 *   <li> <i>Implements <code>IPersistance</code>:</i>
	 *   <ul>
	 *    <li> Information returned by the implementation of the method <code>getXMLEntity</code> of that object
	 *    <li> className : name of the class of the object (this information could be with the information returned by
	 *     the method <code>getXMLEntity</code> of that object
	 *    <li> layerPropertyName : name of the extended property of the layer
	 *   </ul>
	 *  <ul>
	 * </p>
	 *
	 * @return an XML entity with information to the current layer
	 * @throws com.iver.cit.gvsig.fmap.layers.XMLException if there is an error obtaining the object.
	 *
	 * @see #setXMLEntity(XMLEntity)
	 * @see #setXMLEntity03(XMLEntity)
	 */
	public XMLEntity getXMLEntity() throws XMLException {
		XMLEntity xml = new XMLEntity();
		xml.putProperty("className", this.getClass().getName());

		if (this instanceof FLayers) {
		}

//		xml.putProperty("active", active);
		xml.putProperty("active", status.active);
		xml.putProperty("name", name);
		xml.putProperty("minScale", minScale);
		xml.putProperty("maxScale", maxScale);

		// TODO xml.addChild(parentLayer.getXMLEntity());
//		xml.putProperty("visible", visible);
		xml.putProperty("visible", status.visible);
		if (projection != null) {
			xml.putProperty("proj", projection.getFullCode());
		}
		xml.putProperty("transparency", transparency);
//		xml.putProperty("isInTOC", isInTOC);
		xml.putProperty("isInTOC", status.inTOC);


		// persist Properties hashTable


		Set keyset = properties.keySet();



		Iterator keyitr = keyset.iterator();
		XMLEntity xmlProperties = new XMLEntity();
		xmlProperties.putProperty("childName","properties");
		while (keyitr.hasNext()) {
			String propName = (String)keyitr.next();
			Object obj = properties.get(propName);
			if (obj instanceof IPersistence)
			{
				IPersistence persistObj = (IPersistence)obj;
				XMLEntity xmlPropObj = persistObj.getXMLEntity();
				// make sure the node contains the class name
				if (!xmlPropObj.contains("className")) {
					try {
						String propClassName = persistObj.getClassName();
						System.out.println("PROP CLASS NAME "+propClassName);
						xmlPropObj.putProperty("className", propClassName);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				xmlPropObj.putProperty("layerPropertyName", propName);
				xmlProperties.addChild(xmlPropObj);
			} else if (obj instanceof String) {
				XMLEntity xmlPropObj = new XMLEntity();
				xmlPropObj.putProperty("className", String.class.getName());
				xmlPropObj.putProperty("value",(String)obj);
				xmlPropObj.putProperty("layerPropertyName", propName);
				xmlProperties.addChild(xmlPropObj);

			}
		}
		if (xmlProperties.getChildrenCount() > 0) {
			xml.addChild(xmlProperties);
		}
		return xml;
	}

	/**
	 * <p>Inserts information to this layer.</p>
	 *
	 * <p>This XML entity has elements that represent and store information about this layer.</p>
	 *
	 * <p>The properties are the same as the described in <code>getXMLEntity()</code>. And the properties
	 *  <i>proj</i>,  <i>transparency</i>, <i>isInTOC</i> are optional.</p>
	 *
	 * <p>The property <i>numProperties</i> is optional, and only used in old projects.</p>
	 *
	 * @see FLyrDefault#getXMLEntity()
	 *
	 * @param xml an <code>XMLEntity</code> with the information
	 *
	 * @throws com.iver.cit.gvsig.fmap.layers.XMLException if there is an error setting the object.
	 *
	 * @see #getXMLEntity()
	 */
	public void setXMLEntity(XMLEntity xml) throws XMLException {
//		active = xml.getBooleanProperty("active");
		status.active = xml.getBooleanProperty("active");
		name = xml.getStringProperty("name");
		minScale = xml.getDoubleProperty("minScale");
		maxScale = xml.getDoubleProperty("maxScale");
//		visible = xml.getBooleanProperty("visible");
		status.visible = xml.getBooleanProperty("visible");
		if (xml.contains("proj")) {
			setProjection(CRSFactory.getCRS(xml.getStringProperty("proj")));
		}
		if (xml.contains("transparency"))
			transparency = xml.getIntProperty("transparency");
		if (xml.contains("isInTOC"))
//			isInTOC = xml.getBooleanProperty("isInTOC");
			status.inTOC = xml.getBooleanProperty("isInTOC");



		// recreate Properties hashTable

		if (xml.contains("numProperties")) { // for older projects
			int numProps = xml.getIntProperty("numProperties");
			Object obj= null;
			IPersistence objPersist;
			for (int iProp=0; iProp<numProps; iProp++) {
				XMLEntity xmlProp = xml.getChild(0);
				try {
					String className = xmlProp.getStringProperty("className");
					if (className.equals(String.class.getName())) {
						obj = xmlProp.getStringProperty("value");
					} else {
						Class classProp = Class.forName(className);
						obj = classProp.newInstance();
						objPersist = (IPersistence)obj;
						objPersist.setXMLEntity(xmlProp);
					}
					String propName = xmlProp.getStringProperty("layerPropertyName");
					properties.put(propName, obj);
				} catch (Exception e) {
					continue;
				}
				// remove Properties children to avoid breaking layers' XML reading logic
				xml.removeChild(0);
			}
		}          // newer projects store properties under a node
		else {
			int xmlPropertiesPos = xml.firstIndexOfChild("childName","properties");
			XMLEntity xmlProperties =null;
			if (xmlPropertiesPos > -1)
				xmlProperties = xml.getChild(xmlPropertiesPos);

			if (xmlProperties != null) {

				int numProps = xmlProperties.getChildrenCount();
				Object obj;
				String className;
				Class classProp;
				IPersistence objPersist;
				for (int iProp=0; iProp<numProps; iProp++) {
					XMLEntity xmlProp = xmlProperties.getChild(iProp);
					try {
						className = xmlProp.getStringProperty("className");
						if (className.equals(String.class.getName())) {
							obj = xmlProp.getStringProperty("value");
						} else {
							classProp = Class.forName(className);
							obj = classProp.newInstance();
							objPersist = (IPersistence)obj;
							objPersist.setXMLEntity(xmlProp);

						}
						String propName = xmlProp.getStringProperty("layerPropertyName");
						properties.put(propName, obj);
					} catch (Exception e) {
						//FIXME: OJO !!!!!
						continue;
					}
				}
				// remove Properties children to avoid breaking layers' XML reading logic
				xml.removeChild(xmlPropertiesPos);
			}
		}
		this.updateDrawVersion();
	}

	/**
	 * <p>Inserts some default properties to the this layer.</p>
	 *
	 * <p> <b>Properties:</b>
	 *  <ul>
	 *   <li> active : if this layer is active or not
	 *   <li> name : name of this layer
	 *   <li> minScale : minimum scale of this layer
	 *   <li> maxScale : maximum scale of this layer
	 *   <li> visible : if this layer is visible or not
	 *   <li> proj : the projection of this layer (only if it's defined)
	 *   <li> transparency : transparency level of this layer (only if it's defined)
	 *  </ul>
	 * </p>
	 *
	 * @see FLyrDefault#getXMLEntity()
	 *
	 * @param xml an <code>XMLEntity</code> with the information
	 *
	 * @throws com.iver.cit.gvsig.fmap.layers.XMLException if there is an error obtaining the object.
	 *
	 * @see #getXMLEntity()
	 * @see #setXMLEntity(XMLEntity)
	 */
	public void setXMLEntity03(XMLEntity xml) throws XMLException {
//		active = xml.getBooleanProperty("active");
		status.active = xml.getBooleanProperty("active");
		name = xml.getStringProperty("name");
		minScale = xml.getDoubleProperty("minScale");
		maxScale = xml.getDoubleProperty("maxScale");
//		visible = xml.getBooleanProperty("visible");
		status.visible = xml.getBooleanProperty("visible");
		if (xml.contains("proj")) {
			setProjection(CRSFactory.getCRS(xml.getStringProperty("proj")));
		}
		if (xml.contains("transparency"))
			transparency = xml.getIntProperty("transparency");
	}

	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#getMapContext()
	 */
	public MapContext getMapContext() {
		if (getParentLayer() != null) {
			return getParentLayer().getMapContext();
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#addLayerListener(com.iver.cit.gvsig.fmap.layers.LayerListener)
	 */
	public boolean addLayerListener(LayerListener o) {
		if (layerListeners.contains(o))
			return false;
		return layerListeners.add(o);
	}
	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#getLayerListeners()
	 */
	public LayerListener[] getLayerListeners() {
		return (LayerListener[])layerListeners.toArray(new LayerListener[0]);
	}
	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#removeLayerListener(com.iver.cit.gvsig.fmap.layers.LayerListener)
	 */
	public boolean removeLayerListener(LayerListener o) {
		return layerListeners.remove(o);
	}
	/**
	 *
	 */
	private void callDrawValueChanged(LayerEvent e) {
		for (Iterator iter = layerListeners.iterator(); iter.hasNext();) {
			LayerListener listener = (LayerListener) iter.next();

			listener.drawValueChanged(e);
		}
	}
	/**
	 * Called by the method {@linkplain #setName(String)}. Notifies all listeners associated to this layer,
	 *  that its name has changed.
	 *
	 * @param e a layer event with the name of the property that has changed
	 *
	 * @see #setName(String)
	 */
	private void callNameChanged(LayerEvent e) {
		for (Iterator iter = layerListeners.iterator(); iter.hasNext();) {
			LayerListener listener = (LayerListener) iter.next();

			listener.nameChanged(e);
		}
	}

	/**
	 * Called by the method {@linkplain #setVisible(boolean)}. Notifies all listeners associated to this layer,
	 *  that its visibility has changed.
	 *
	 * @param e a layer event with the name of the property that has changed
	 *
	 * @see #setVisible(boolean)
	 */
	private void callVisibilityChanged(LayerEvent e) {
		for (Iterator iter = layerListeners.iterator(); iter.hasNext();) {
			LayerListener listener = (LayerListener) iter.next();

			listener.visibilityChanged(e);
		}
	}

	/**
	 * Called by the method {@linkplain #setActive(boolean)}. Notifies all listeners associated to this layer,
	 *  that its active state has changed.
	 *
	 * @param e a layer event with the name of the property that has changed
	 *
	 * @see #setActive(boolean)
	 */
	private void callActivationChanged(LayerEvent e) {
		for (Iterator iter = layerListeners.iterator(); iter.hasNext();) {
			LayerListener listener = (LayerListener) iter.next();

			listener.activationChanged(e);
		}
	}

	/**
	 * Returns the virtual layers associated to this layer.
	 *
	 * @return a node with the layers
	 *
	 * @see #setVirtualLayers(FLayers)
	 */
	public FLayers getVirtualLayers() {
		return virtualLayers;
	}

	/**
	 * Inserts virtual layers to this layer.
	 *
	 * @param virtualLayers a node with the layers
	 *
	 * @see #getVirtualLayers()
	 */
	public void setVirtualLayers(FLayers virtualLayers) {
		if (this.virtualLayers != virtualLayers){
			if (this.virtualLayers != null){
				this.virtualLayers.removeLayerListener(this);
			}
			this.virtualLayers = virtualLayers;
			if (this.virtualLayers != null){
				this.virtualLayers.addLayerListener(this);
			}
		}
	}

	/**
	 * Sets transformation coordinates for this layer.
	 *
	 * @param ct an object that implements the <code>ICoordTrans</code> interface, and with the transformation coordinates
	 *
	 * @see #getCoordTrans()
	 */
	public void setCoordTrans(ICoordTrans ct) {
		if (this.ct == ct){
			return;
		}
		if (this.ct != null && this.ct.equals(ct)){
			return;
		}
		this.ct = ct;
		this.updateDrawVersion();
	}

	/**
	 * Returns the transformation coordinates of this layer.
	 *
	 * @return an object that implements the <code>ICoordTrans</code> interface, and with the transformation coordinates
	 *
	 * @see #setCoordTrans(ICoordTrans)
	 */
	public ICoordTrans getCoordTrans() {
		return ct;
	}

	/**
	 * <p>Method called by {@link FLayers FLayers} to notify this layer that is going to be added.
	 *  This previous notification is useful for the layers that need do something before being added. For
	 *  example, the raster needs reopen a file that could have been closed recently.</p>
	 */
	public void wakeUp() throws LoadLayerException {
	}
	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#getMinScale()
	 */
	public double getMinScale() {
		return minScale;
	}

	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#getMaxScale()
	 */
	public double getMaxScale() {
		return maxScale;
	}
	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#setMinScale(double)
	 */
	public void setMinScale(double minScale) {
		if (this.minScale != minScale){
			this.minScale = minScale;
			this.updateDrawVersion();
		}
	}
	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#setMaxScale(double)
	 */
	public void setMaxScale(double maxScale) {
		if (this.maxScale != maxScale){
			this.maxScale = maxScale;
			this.updateDrawVersion();
		}
	}
	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#isWithinScale(double)
	 */
	public boolean isWithinScale(double scale) {

		boolean bVisible = true;
		if (getMinScale() != -1) {
			if (scale < getMinScale())
				bVisible = false;
		}
		if (getMaxScale() != -1) {
			if (scale > getMaxScale())
				bVisible = false;
		}

		return bVisible;
	}
	/**
	 * Returns the strategy of drawing and processing for this layer.
	 *
	 * @return an object that implements the <code>Strategy</code> interface.
	 *
	 * @see #setStrategy(Strategy)
	 */
	public Strategy getStrategy() {
		return privateStrategy;
	}
	/**
	 * Inserts the strategy of drawing and processing this layer.
	 *
	 * @param s an object that implements the <code>Strategy</code> interface.
	 *
	 * @see #getStrategy()
	 */
	public void setStrategy(Strategy s) {
		privateStrategy = s;
	}
	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#setEditing(boolean)
	 */
	public void setEditing(boolean b) throws StartEditionLayerException {
//		isediting = b;
		status.editing = b;
	}
	/**
	 * Called by some version of the method {@linkplain #setEditing(boolean)} overwritten. Notifies
	 *  all listeners associated to this layer, that its edition state has changed.
	 *
	 * @param e a layer event with the name of the property that has changed
	 *
	 * @see #setEditing(boolean)
	 */
	protected void callEditionChanged(LayerEvent e) {
		for (Iterator iter = layerListeners.iterator(); iter.hasNext();) {
			LayerListener listener = (LayerListener) iter.next();

			listener.editionChanged(e);
		}
	}
	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#isEditing()
	 */
	public boolean isEditing() {
//		return isediting;
		return status.editing;
	}
	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#getTocImageIcon()
	 */
	public ImageIcon getTocImageIcon() {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#isInTOC()
	 */
	public boolean isInTOC() {
//		return isInTOC;
		return status.inTOC;
	}
	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#setInTOC(boolean)
	 */
	public void setInTOC(boolean b) {
		status.inTOC=b;
	}
	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#isAvailable()
	 */
	public boolean isAvailable() {
		return status.available;
	}
	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#setAvailable(boolean)
	 */
	public void setAvailable(boolean available) {
		if (status.available != available){
			status.available = available;
			this.updateDrawVersion();
		}
	}
	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#reload()
	 */
	public void reload() throws ReloadLayerException {
		this.setAvailable(true);
	}

	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#getFLayerStatus()
	 */
	public FLayerStatus getFLayerStatus(){
		return status.cloneStatus();
	}
	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#setFLayerStatus(com.iver.cit.gvsig.fmap.layers.FLayerStatus)
	 */
	public void setFLayerStatus(FLayerStatus status){
		if (!this.status.equals(status)){
			this.status = status;
			this.updateDrawVersion();
		}

	}

	/*
	 * This stuff is to save error's info that causes
	 * unavailable status.
	 * */

	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#isOk()
	 */

	public boolean isOk(){
		return status.isOk();
	}
	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#getNumErrors()
	 */
	public int getNumErrors(){
		return status.getNumErrors();
	}

	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#getError(int)
	 */
	public BaseException getError(int i){
		return status.getError(i);
	}
	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#getErrors()
	 */
	public List getErrors(){
		return status.getErrors();
	}

	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#addError(com.iver.cit.gvsig.fmap.DriverException)
	 */
	public void addError(BaseException exception){
		status.addLayerError(exception);
	}
	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#visibleRequired()
	 */
	public boolean visibleRequired() {
		return status.visible;
	}
	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#getInfoString()
	 */
	public String getInfoString() {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#isWritable()
	 */
	public boolean isWritable() {
		return status.writable;
	}

	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#cloneLayer()
	 */
	public FLayer cloneLayer() throws Exception {
		return this;
	}
	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#getTocStatusImage()
	 */
	public Image getTocStatusImage() {
		return tocStatusImage;
	}

	/**
	 * Inserts the image icon that will be shown in the TOC next to this layer, according its status.
	 *
	 * @param tocStatusImage the image
	 *
	 * @see #getTocStatusImage()
	 */
	public void setTocStatusImage(Image tocStatusImage) {
		this.tocStatusImage = tocStatusImage;
		logger.debug("setTocStatusImage " + tocStatusImage + " sobre capa " + this.getName());
	}
	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#newComposedLayer()
	 */
	public ComposedLayer newComposedLayer() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#allowLinks()
	 */
	public boolean allowLinks()
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#getLinkProperties()
	 */
	public AbstractLinkProperties getLinkProperties()
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#getLink(java.awt.geom.Point2D, double)
	 */
	public URI[] getLink(Point2D point, double tolerance)
	{
		//return linkProperties.getLink(this)
		return null;
	}

	/**
	 * @see LayerChangeSupport#addLayerListener(LegendListener)
	 */
	public void addLegendListener(LegendListener listener) {
		layerChangeSupport.addLayerListener(listener);
	}

	/**
	 * @see LayerChangeSupport#callLegendChanged(LegendChangedEvent)
	 */
	protected void callLegendChanged(LegendChangedEvent e) {
		layerChangeSupport.callLegendChanged(e);
		if(parentLayer != null)
			parentLayer.callLegendChanged(e);
	}

	/**
	 * @see LayerChangeSupport#removeLayerListener(LegendListener)
	 */
	public void removeLegendListener(LegendListener listener) {
		layerChangeSupport.removeLayerListener(listener);
	}


	public long getDrawVersion() {
		return this.drawVersion;
	}

	protected void updateDrawVersion(){
		this.drawVersion++;
		this.callDrawValueChanged(LayerEvent.createDrawValuesChangedEvent(this, ""));
		if (this.parentLayer != null){
			this.parentLayer.updateDrawVersion();
		}
	}

	public boolean hasChangedForDrawing(long value){
		return this.drawVersion > value;
	}

	public void activationChanged(LayerEvent e) {
	}

	public void drawValueChanged(LayerEvent e) {
		this.updateDrawVersion();
	}

	public void editionChanged(LayerEvent e) {

	}

	public void nameChanged(LayerEvent e) {

	}

	public void visibilityChanged(LayerEvent e) {

	}


}

