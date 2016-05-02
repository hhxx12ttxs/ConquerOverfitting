/*
 * CADOculus, 3D in the Web 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT Licens as published at
 * http://opensource.org/licenses/mit-license.php
 * 
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the MIT Licens
 * along with this program.  If not, see <* http://opensource.org/licenses/mit-license.php>
 *
 */
package de.cadoculus.conversion.threejs;

import static de.cadoculus.conversion.threejs.ThreeJsScene.*;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;


/**
 * This class represents a node in a scenegraph.
 *
 * <p>By default the node is null positioned without rotation or scaling and has a 'displayName'
 * property.
 *
 * @author  cz
 */
public class ThreeJsNode {
    private static final String DISPLAY_NAME = "displayName";

    private List< ThreeJsNode > children = new ArrayList< ThreeJsNode >();
    private ThreeJsGeometry geometry;
    private final String id = UUID.randomUUID().toString();
    private Point3d position = new Point3d();
    private Properties properties = new Properties();
    private Vector3d rotation = new Vector3d();
    private Vector3d scale = new Vector3d( 1, 1, 1 );
    private final ThreeJsScene scene;
    private boolean visible = true;

    /**
     * Create a new Node with the given displayName
     *
     * @param  name
     */
    ThreeJsNode( ThreeJsScene scene, String name ) {
        this.properties.setProperty( DISPLAY_NAME, name );
        this.scene = scene;
    }

    @Override public boolean equals( Object obj ) {

        if ( obj instanceof ThreeJsNode ) {
            return ( ( ThreeJsNode ) obj ).getId().equals( getId() );
        }

        return false;

    }

    @Override public int hashCode() {
        return 2105 + getId().hashCode();
    }

    @Override public String toString() {
        return properties.containsKey( DISPLAY_NAME ) ? properties.getProperty( DISPLAY_NAME )
                                                      : getId();
    }

    /**
     * Get the list of children Nodes
     *
     * @return  the children
     */
    public List< ThreeJsNode > getChildren() {
        return children;
    }

    /**
     * @return  the geometry
     */
    public ThreeJsGeometry getGeometry() {
        return geometry;
    }

    /**
     * Get the identifier for the node
     *
     * @return  the id
     */
    public String getId() {
        return id;
    }

    /**
     * Get the position of the node
     *
     * @return  the position
     */
    public Point3d getPosition() {
        return position;
    }

    /**
     * Get the nodes Properties
     *
     * @return  the properties
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Get the nodes rotation vector
     *
     * @return  the rotation
     */
    public Vector3d getRotation() {
        return rotation;
    }

    /**
     * Get the nodes scale vector
     *
     * @return  the scale
     */
    public Vector3d getScale() {
        return scale;
    }

    /**
     * Get the value of the visible flag
     *
     * @return  the visible
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Set a new children Nodes list
     *
     * @param  children  the children to set
     */
    public void setChildren( List< ThreeJsNode > children ) {

        if ( children == null ) {
            throw new IllegalArgumentException( "expect non null children list" );
        }

        this.children = children;
    }

    /**
     * @param  geometry  the geometry to set
     */
    public void setGeometry( ThreeJsGeometry geometry ) {
        this.geometry = geometry;
    }

    /**
     * Set a new position
     *
     * @param  position  the position to set
     */
    public void setPosition( Point3d position ) {

        if ( position == null ) {
            throw new IllegalArgumentException( "expect non null position" );
        }

        this.position = position;
    }

    /**
     * Set a new Properties map
     *
     * @param  properties  the properties to set
     */
    public void setProperties( Properties properties ) {

        if ( position == null ) {
            throw new IllegalArgumentException( "expect non null position" );
        }

        this.properties = properties;
    }

    /**
     * Set the nodes rotation vector
     *
     * @param  rotation  the rotation to set
     */
    public void setRotation( Vector3d rotation ) {

        if ( rotation == null ) {
            throw new IllegalArgumentException( "expect non null rotation" );
        }

        this.rotation = rotation;
    }

    /**
     * Set the nodes scale vector
     *
     * @param  scale  the scale to set
     */
    public void setScale( Vector3d scale ) {

        if ( scale == null ) {
            throw new IllegalArgumentException( "expect non null scale" );
        }

        this.scale = scale;
    }

    /**
     * Set the nodes scale
     *
     * @param  scale  the scale to set on all 3 axes
     */
    public void setScale( double scale ) {

        if ( scale < 1e-6 ) {
            throw new IllegalArgumentException( "expect a scale > 1e-6" );
        }

        this.scale.x = scale;
        this.scale.y = scale;
        this.scale.z = scale;
    }

    /**
     * Set the nodes visibility flag
     *
     * @param  visible  the visible to set
     */
    public void setVisible( boolean visible ) {
        this.visible = visible;
    }

    void toJSON( PrintWriter pw, int level ) {
        String indent = ThreeJsScene.getIndent( level );

        // "rootNode" : {
        pw.print( indent );
        pw.print( "\"" );
        pw.print( getId() );
        pw.println( "\" : {" );

        // "geometry" : "colorcube",
        if ( geometry != null ) {
            pw.print( indent );
            pw.print( "  \"geometry\" : \"" );
            pw.print( geometry.getId() );
            pw.println( "\"," );
        }

        // "position" : [ 0, 0, 0 ],
        pw.print( indent );
        pw.print( "  \"position\" : " );
        format( pw, position );
        pw.println( "," );

        // "rotation" : [ 0, 0, 0 ],
        pw.print( indent );
        pw.print( "  \"rotation\" : " );
        format( pw, rotation );
        pw.println( "," );

        //    "scale" : [ 1, 1, 1 ],
        pw.print( indent );
        pw.print( "  \"scale\" : " );
        format( pw, scale );
        pw.println( "," );

        //    "visible"  : true,
        pw.print( indent );
        pw.println( visible ? "  \"visible\" : true," : "  \"visible\" : false," );

        // "properties" : {
        if ( !properties.containsKey( DISPLAY_NAME ) ) {
            properties.put( DISPLAY_NAME, getId() );
        }

        pw.print( indent );
        pw.println( "  \"properties\" : {" );

        for ( Iterator< Object > it = properties.keySet().iterator(); it.hasNext(); ) {
            String key = ( String ) it.next();
            pw.print( indent );
            pw.print( "    \"" );
            pw.print( key );
            pw.print( "\" : \"" );
            pw.print( properties.getProperty( key ) );

            pw.println( it.hasNext() ? "\"," : "\"" );
        }

        pw.print( indent );
        pw.println( "    }," );

        //  "children" : {
        if ( children.isEmpty() ) {
            pw.print( indent );
            pw.println( "  \"children\" : { }" );
        } else {
            pw.print( indent );
            pw.println( "  \"children\" : {" );

            for ( Iterator< ThreeJsNode > it = children.iterator(); it.hasNext(); ) {
                ThreeJsNode node = it.next();
                node.toJSON( pw, level + 1 );

                pw.println( it.hasNext() ? "," : "" );
            }

            pw.print( indent );
            pw.println( "    }" );

        }

        pw.print( indent );
        pw.print( "  }" );
        pw.flush();

    }

}

