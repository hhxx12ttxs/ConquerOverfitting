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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.vecmath.Tuple3d;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3d;


/**
 * <pre>
  {
      "metadata": {
          "formatVersion":3
          "generatedBy" : "Blender 2.60 Exporter",
          "vertices" : 1226,
          "faces" : 1008,
          "normals" : 0,
          "colors" : 0,
          "uvs" : 0,
          "materials" : 1,
          "morphTargets" : 31
      },
      "scale":1,
      "materials":[],
      "vertices":[5,25,5,5,25,-5,5,-25,5,5,-25,-5,-5,25,-5,-5,25,5,-5,-25,-5,-5,-25,5],
      "morphTargets":[],
      "morphColors":[],
      "normals":[1,0,0,-1,0,0,0,1,0,0,-1,0,0,0,1,0,0,-1],
      "colors":[],
      "uvs":[[]],
      "faces":[0,0,2,3,0,4,6,7,0,4,5,0,0,7,6,3,0,5,7,2,0,1,3,6]

  }
 </pre>
 *
 * @author  cz
 */
public class GeometryBuilder {
    private static Log log = LogFactory.getLog( GeometryBuilder.class );
    private static final DecimalFormat DEC3 = new DecimalFormat( "0.0##",
            DecimalFormatSymbols.getInstance( Locale.ENGLISH ) );
    private static final double MIN = 1e-5;
    private List< Integer > faces = new ArrayList< Integer >();
    private String generatedBy = "GeometryBuilder";
    private List< Tuple3d > normals = new ArrayList< Tuple3d >();
    private List< Tuple3d > vertices = new ArrayList< Tuple3d >();

    public static void format( PrintWriter out, Tuple3d p ) {
        format( out, p.x );
        out.print( "," );
        format( out, p.y );
        out.print( "," );
        format( out, p.z );
    }

        public static void format( PrintWriter out, double d ) {

            if ( Math.abs(d) > MIN ) {
                out.print( DEC3.format( d ) );
            } else {
                out.print( "0.0" );
            }
        }

        public static void format( PrintWriter out, Tuple3f p ) {
            format( out, p.x );
            out.print( "," );
            format( out, p.y );
            out.print( "," );
            format( out, p.z );

        }

    /**
     * Add a single triangle without normal or colour
     *
     * @param  p1  the first vertex
     * @param  p2  the second vertex
     * @param  p3  the third vertex
     */
    public void addTriangle( Tuple3f v1, Tuple3f v2, Tuple3f v3 ) {

        addTriangle( new Vector3d( v1 ), new Vector3d( v2 ), new Vector3d( v3 ) );
    }

    /**
     * Add a single triangle without normal or colour
     *
     * @param  p1  the first vertex
     * @param  p2  the second vertex
     * @param  p3  the third vertex
     */
    public void addTriangle( Tuple3d v1, Tuple3d v2, Tuple3d v3 ) {

        if ( ( v1 == null ) || ( v2 == null ) || ( v3 == null ) ) {
            throw new IllegalArgumentException( "expect non null input for v1 ... v3, got v1 " +
                v1 + ", v2 " + v2 + ", v3 " + v3 );

        }

        //log.info( "addTriangle " + v1 + ", " + v2 + ", " + v3 );

//        // Calculate normal
//        Vector3d t1 = new Vector3d();
//        t1.sub( v2, v1);
//        Vector3d t2 = new Vector3d();
//        t2.sub( v3, v1);
//
//        Vector3d n = new Vector3d();
//        n.cross( t1, t2 );

        int startLength = vertices.size();
        vertices.add( v1 );
        vertices.add( v2 );
        vertices.add( v3 );

        faces.add( startLength );
        faces.add( startLength + 1 );
        faces.add( startLength + 2 );

    }

    public void toJSON( OutputStream os ) throws IOException {
        PrintWriter out = new PrintWriter( os );
        out.println( "{" );

        out.println( "  \"metadata\": {" );
        out.println( "     \"formatVersion\":3," );
        out.println( "     \"generatedBy\":\"GeometryBuilder\"," );
        out.println( "     \"vertices\":" + vertices.size() + "," );
        out.println( "     \"faces\":" + faces.size() + "," );
        out.println( "     \"normals\":0," );
        out.println( "     \"colors\":0," );
        out.println( "     \"uvs\":0," );
        out.println( "     \"materials\":0," );
        out.println( "     \"morphTargets\":0" );
        out.println( "  }," );
        out.println( "  \"scale\":1," );
        out.println( "  \"materials\":[]," );
        out.print( "  \"vertices\":[" );

        //log.info( "vertices " + vertices );

        for ( Iterator< Tuple3d > it = vertices.iterator(); it.hasNext(); ) {
            Tuple3d point3d = it.next();
            format( out, point3d );

            if ( it.hasNext() ) {
                out.print( ", " );
            }
        }

        out.println( "]," );

        out.println( "  \"morphTargets\":[]," );
        out.println( "  \"morphColors\":[]," );
        out.println( "  \"normals\":[]," );
        out.println( "  \"colors\":[]," );
        out.println( "  \"uvs\":[[]]," );
        out.print( "  \"faces\":[" );

        for ( Iterator< Integer > it = faces.iterator(); it.hasNext(); ) {
            out.print( "0, " );
            out.print( it.next() );
            out.print( "," );
            out.print( it.next() );
            out.print( "," );
            out.print( it.next() );

            if ( it.hasNext() ) {
                out.print( ", " );
            }

        }

        out.println( "]" );

        out.println( "}" );
        out.close();
    }
}

