/*
 * $RCSfile: GeometryClipper.java,v $
 *
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 *
 * $Revision: 1.2 $
 * $Date: 2007/02/09 17:17:01 $
 * $State: Exp $
 */

package org.jdesktop.j3d.utils.geometry;

import java.util.Vector;
import java.util.Enumeration;

import javax.media.j3d.BoundingBox;

import javax.vecmath.Point3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import com.sun.j3d.utils.geometry.Stripifier;


/** 
 * Given a GeometryInfo structure clip the geometry so that it fits
 * completely inside a BoundingBox volume.
 *
 * @author  paulby
 * @version 1.5, 01/18/02
 */
public class GeometryClipper extends Object {

  private BoundingBox boundingBox;
  private Point3d upperCorner;
  private Point3d lowerCorner;
  private Vector newTriangles;
  private NormalGenerator normalGenerator;
  private Stripifier stripifier;
  
  /** Creates new GeometryClipper */
  public GeometryClipper() {
    upperCorner = new Point3d();
    lowerCorner = new Point3d();
    newTriangles = new Vector();
    
    normalGenerator = new NormalGenerator();
    stripifier = new Stripifier();
  }
  
  /**
    * Clip the geometry so that it fits inside the BoundingBox. Any
    * triangles which overlap the boundary will have their sides truncated
    * at the boundary
    */
  public void clip( GeometryInfo geom, BoundingBox boundingBox ) {
    
    boundingBox.getUpper( upperCorner );
    boundingBox.getLower( lowerCorner );
    newTriangles.clear();
    this.boundingBox = boundingBox;
    geom.convertToIndexedTriangles();
    processTriangleArray( geom );
    
  }
  /**
    * Create the smallest boundingBox that encloses the
    * geometry in Shape
    */
  public BoundingBox createBounds( javax.media.j3d.Shape3D shape ) {
    if (!(shape.getGeometry() instanceof javax.media.j3d.GeometryArray))
      throw new RuntimeException( "Geomtery type not supported "+
                        shape.getGeometry().getClass().getName() );
    
    javax.media.j3d.GeometryArray geom = (javax.media.j3d.GeometryArray)shape.getGeometry();
    double[] coords = new double[ geom.getVertexCount()*3 ];
    geom.getCoordinates( 0, coords );
    
    if (coords.length<4)    // Handle Shape with a single point
      return new BoundingBox( new Point3d( coords[0], coords[1], coords[2] ),
                              new Point3d( coords[0], coords[1], coords[2] ));
    
    double lowerX = coords[0];
    double upperX = coords[0];
    double lowerY = coords[1];
    double upperY = coords[1];
    double lowerZ = coords[2];
    double upperZ = coords[2];
    
    for(int i=3; i<coords.length; i+=3) {
      lowerX = Math.min( lowerX, coords[i] );
      upperX = Math.max( upperX, coords[i] );
      lowerY = Math.min( lowerY, coords[i+1] );
      upperY = Math.max( upperY, coords[i+1] );
      lowerZ = Math.min( lowerZ, coords[i+2] );
      upperZ = Math.max( upperZ, coords[i+2] );
    }
    
    return new BoundingBox( new Point3d( lowerX, lowerY, lowerZ ),
                            new Point3d( upperX, upperY, upperZ ));

  }  
  private void processTriangleArray( GeometryInfo geom ) {
    geom.indexify();
    
    Point3f[] coords = geom.getCoordinates();
    Object[] colors = geom.getColors();
    Vector3f[] normals = geom.getNormals();
    int[] coordIndices = geom.getCoordinateIndices();
    int[] colorIndices = geom.getColorIndices();
    
    int numPointsDeleted = 0;
    
    //printGeom( geom );
    
   for(int i=0; i<coordIndices.length; i+=3) {
      // clip Triangle marks deleted vertex indices and add's
      // new Triangles to the newTriangle structure.
      numPointsDeleted += clipTriangle( coords, coordIndices,
                                        colors, colorIndices,
                                        i );
    }
    
    // Remove the indices marked as deleted
    removeDeletedVertex( geom, coordIndices, colorIndices, numPointsDeleted );

    // Add the newTriangles to the geometry
    addNewTriangles( geom );
    
    //printGeom( geom );
    geom.compact();
    geom.setNormals( (Vector3f[])null );
    geom.setNormalIndices( null );
    normalGenerator.generateNormals( geom );
    stripifier.stripify( geom );
    
    //System.out.println("Index Size "+geom.getCoordinateIndices().length );
    //System.out.println("Coords Size "+geom.getCoordinates().length );

  }
  
  private void printGeom( GeometryInfo geom ) {
    Point3f[] coords = geom.getCoordinates();
    /*
    for(int i=0; i<coords.length; i++)
      System.out.println( i+"    "+ coords[i] );
    */
    
    int[] indices = geom.getCoordinateIndices();
    for(int i=0; i<indices.length; i++)
      System.out.println( indices[i] + "  "+coords[indices[i]] );
  }
  
  /**
    * Remove the deleted Vertex and add any new ones
    */
  private void removeDeletedVertex( GeometryInfo geom, int[] coordIndices,
                             int[] colorIndices, int numDeleted ) {
    int[] normalIndices = geom.getNormalIndices();
    int[] texCoordIndices = geom.getTextureCoordinateIndices(0);
        
    int indexSize = coordIndices.length - numDeleted;
    
    int[] newNormalIndices = null;
    int[] newColorIndices = null;
    int[] newTexCoordIndices = null;
    int[] newCoordIndices = new int[ indexSize ];
    
    if (normalIndices!=null)
      newNormalIndices = new int[ indexSize ];
    
    if (colorIndices!=null)
      newColorIndices = new int[ indexSize ];
    
    if (texCoordIndices!=null)
      newTexCoordIndices = new int[ indexSize ];
    
    int j=0;
    for(int i=0; i<coordIndices.length; i++) {
      if (coordIndices[i]!=-1) {
        newCoordIndices[j] = coordIndices[i];
        if (newNormalIndices!=null)
          newNormalIndices[j] = normalIndices[i];
        if (newColorIndices!=null)
          newColorIndices[j] = colorIndices[i];
        if (newTexCoordIndices!=null)
          newTexCoordIndices[j] = texCoordIndices[i];
        j++;
      }
    }
       
    geom.setCoordinateIndices( newCoordIndices );
    geom.setNormalIndices( newNormalIndices );
    geom.setColorIndices( newColorIndices );
    geom.setTextureCoordinateIndices( 0, newTexCoordIndices );
  }
  
  /** 
    * Add the new Triangles to this geometry. 
    */
  private void addNewTriangles( GeometryInfo geom ) {
    Enumeration e = newTriangles.elements();
    GeometryInfo newGeom;
    int additionalIndices=0;
    int additionalCoords=0;
    int additionalColors=0;
    
    // Calculate the length of the new arrays
    while( e.hasMoreElements() ) {
      newGeom = (GeometryInfo)e.nextElement();
      additionalIndices += newGeom.getCoordinateIndices().length;
      additionalCoords += newGeom.getCoordinates().length;
      
      if (geom.getColors()!=null)
        additionalColors += newGeom.getColors().length;
    }
    
    Point3f[] newCoords = new Point3f[ geom.getCoordinates().length +
                                       additionalCoords ];
    System.arraycopy( geom.getCoordinates(), 0, newCoords, 0,
                          geom.getCoordinates().length );
    
    int[] newCoordIndices = new int[ geom.getCoordinateIndices().length +
                                     additionalIndices ];
    System.arraycopy( geom.getCoordinateIndices(), 0, newCoordIndices, 0, 
                      geom.getCoordinateIndices().length );
    
    int[] newColorIndices = null;
    Object[] newColors = null;
    int currentColor = 0;
    int currentColorIndex =0;
    int currentCoord = geom.getCoordinates().length;
    int currentCoordIndex = geom.getCoordinateIndices().length;
    
    if (geom.getColors()!=null) {
      newColorIndices = new int[ newCoordIndices.length ];
      System.arraycopy( geom.getColorIndices(), 0, newColorIndices, 0, 
                      geom.getColorIndices().length );
    
      if (geom.getColors()[0] instanceof javax.vecmath.Color3f)
        newColors = new javax.vecmath.Color3f[ geom.getColors().length +
                                     additionalColors ];
      else if (geom.getColors()[0] instanceof javax.vecmath.Color4f)
        newColors = new javax.vecmath.Color4f[ geom.getColors().length +
                                     additionalColors ];
      else 
        throw new RuntimeException("Unsupported Color in Geometry ");
      
      System.arraycopy( geom.getColors(), 0, newColors, 0, 
                      geom.getColors().length );
      currentColor = geom.getColors().length;
      currentColorIndex = geom.getColorIndices().length;
    }
  
    
    e = newTriangles.elements();
    int length;
    
    int coordIndicesOffset = geom.getCoordinates().length;
    int colorIndicesOffset = 0;
    
    if (newColors!=null)
      colorIndicesOffset = geom.getColors().length;
    
    while( e.hasMoreElements() ) {
      newGeom = (GeometryInfo)e.nextElement();
      
      length = newGeom.getCoordinates().length;
      System.arraycopy( newGeom.getCoordinates(),0, newCoords, currentCoord, length );
      currentCoord += length;
      
      int[] newGeomCoordIndices = newGeom.getCoordinateIndices();
      for(int i=0; i<newGeomCoordIndices.length; i++)
        newCoordIndices[ currentCoordIndex+i ] = newGeomCoordIndices[i]+coordIndicesOffset;
      coordIndicesOffset += newGeom.getCoordinates().length;     
      currentCoordIndex += newGeomCoordIndices.length;
      
      if (newColors!=null) {
        length = newGeom.getColors().length;
        System.arraycopy( newGeom.getColors(), 0, newColors, currentColor, length );
        currentColor += length;
        
        int[] newGeomColorIndices = newGeom.getColorIndices();
        for(int i=0; i<newGeomColorIndices.length; i++)
          newColorIndices[ currentColorIndex+i ] = newGeomColorIndices[i]+colorIndicesOffset;
        colorIndicesOffset += newGeom.getColors().length;     
        currentColorIndex += newGeomColorIndices.length;
      }
      
    }
    
    geom.setCoordinates( newCoords );
    geom.setCoordinateIndices( newCoordIndices );
    
    if (newColors!=null) {
      if (newColors[0] instanceof javax.vecmath.Color3f)
        geom.setColors( (javax.vecmath.Color3f[])newColors );
      else
        geom.setColors( (javax.vecmath.Color4f[])newColors );
      
      geom.setColorIndices( newColorIndices );
    }
  }
     
  private int clipTriangle( Point3f[] coords, int[] coordIndices, 
                            Object[] colors, int[] colorIndices,
                            int index ) {
    Point3f p1 = coords[ coordIndices[ index ] ];
    Point3f p2 = coords[ coordIndices[ index+1 ] ];
    Point3f p3 = coords[ coordIndices[ index+2 ] ];
    
    Vector3f n1 = null;
    Vector3f n2 = null;
    Vector3f n3 = null;
    
    Object color1 = null;
    Object color2 = null;
    Object color3 = null;
    
    if (colors!=null) {
      color1 = colors[ colorIndices[ index ] ];
      color2 = colors[ colorIndices[ index+1 ] ];
      color3 = colors[ colorIndices[ index+2 ] ];
    }
    
    boolean c1 = contains( p1 );
    boolean c2 = contains( p2 );
    boolean c3 = contains( p3 );
    
    if (c1 && c2 && c3) {    // All points in bounds, nothing to do
      return 0 ;
    }
    
    if (!(c1 || c2 || c3)) {    // All points outside bounds, delete triangle
      coordIndices[index] = -1;
      coordIndices[index+1] = -1;
      coordIndices[index+2] = -1;
      return 3;
    }
    
    
    // Need to actually clip this triangle
    
    // if one corner of the triangle is outside the bounds then we need to
    // remove the triangle and replace it with a quad
    // if two corners of the triangle are outside the bounds then we
    // can just create a new triangle
    Point3f[] newPoints = null;
    Object[] newColors = null;    
    
    if (!c1 && !c2 || !c2 && !c3 || !c3 && !c1) {
            // Clip the triangle
      coordIndices[index] = -1;
      coordIndices[index+1] = -1;
      coordIndices[index+2] = -1;
      
      newPoints = new Point3f[3];
      if (!c1 && !c2) {
        newPoints[0]  = clipLine( p3, p1 );
        newPoints[1] = clipLine( p3, p2 );
        newPoints[2] = p3;
      } else if ( !c2 && !c3 )  {
        newPoints[0] = p1;
        newPoints[1] = clipLine( p1, p2 );
        newPoints[2] = clipLine( p1, p3 );
      } else if ( !c3 && !c1 ) {
        newPoints[0]  = clipLine( p2, p1 );
        newPoints[1] = p2;
        newPoints[2] = clipLine( p2, p3 );
      }
      
      if (colors!=null) {
        if (colors[0] instanceof javax.vecmath.Color3f)
          newColors = new javax.vecmath.Color3f[4];
       else if (colors[0] instanceof javax.vecmath.Color4f)
          newColors = new javax.vecmath.Color4f[4];
      
        newColors[0] = color1;
        newColors[1] = color2;
        newColors[2] = color3;
      }

    } else {
    
      // Delete the old triangle and create two new triangles (Quad) to be
      // added later

      coordIndices[index] = -1;
      coordIndices[index+1] = -1;
      coordIndices[index+2] = -1;

      newPoints = new Point3f[4];

      if (colors!=null) {
        if (colors[0] instanceof javax.vecmath.Color3f)
          newColors = new javax.vecmath.Color3f[4];
        else if (colors[0] instanceof javax.vecmath.Color4f)
          newColors = new javax.vecmath.Color4f[4];
      }


      int p=0;

      if (c1 && !c2) {
        if (colors!=null) {
          newColors[p] = color1;
          newColors[p+1] = color2;
        }
        newPoints[p++] = p1;
        newPoints[p++] = clipLine( p1, p2 );
      } else if (c2 && !c1) {
        if (colors!=null) {
          newColors[p] = color1;
          newColors[p+1] = color2;
        }
        newPoints[p++] = clipLine( p2, p1 );
        newPoints[p++] = p2;
      }

      if (c2 && !c3) {
        if (colors!=null) {
          newColors[p] = color2;
          newColors[p+1] = color3;
        }
        newPoints[p++] = p2;
        newPoints[p++] = clipLine( p2, p3 );
      } else if (c3 && !c2) {
        if (colors!=null) {
          newColors[p] = color2;
          newColors[p+1] = color3;
        }
        newPoints[p++] = clipLine( p3, p2 );
        newPoints[p++] = p3;
      }

      if (c1 && !c3) {
        if (colors!=null) {
          newColors[p] = color3;
          newColors[p+1] = color1;
        }
        newPoints[p++] = clipLine( p1, p3 );
        newPoints[p++] = p1;
      } else if (c3 && !c1) {
        if (colors!=null) {
          newColors[p] = color3;
          newColors[p+1] = color1;
        }
        newPoints[p++] = p3;
        newPoints[p++] = clipLine( p3, p1 );
      }

      if (p!=4)
        throw new RuntimeException("Triangle Clip fault");
    }

    GeometryInfo newGeom = null;

    if (newPoints.length==4)
      newGeom = new GeometryInfo( GeometryInfo.QUAD_ARRAY );
    else 
      newGeom = new GeometryInfo( GeometryInfo.TRIANGLE_ARRAY );

    newGeom.setCoordinates( newPoints );
    if (colors!=null) {
      if (newColors[0] instanceof javax.vecmath.Color3f )
        newGeom.setColors( (javax.vecmath.Color3f[])newColors );
      else if (newColors[0] instanceof javax.vecmath.Color4f )
        newGeom.setColors( (javax.vecmath.Color4f[])newColors );
    }
    
    newGeom.convertToIndexedTriangles();
    
    /*
    System.out.println("Clipped Triangle");
    System.out.println( p1 +" "+p2+" "+p3 );
    System.out.println( c1+"  "+c2+"  "+c3 );
    printGeom( newGeom );
    System.out.println();
    */
   
    newTriangles.add( newGeom );
        
    return 3;    
  }
  
  /**
    * Clip the line between p1 and p2 against the BoundingBox
    * return the intersection point
    * 
    * Assumes p1 is inside box and p2 is outside
    *
    * Assumes BoundingBox is axis aligned
    */
  private Point3f clipLine( Point3f p1, Point3f p2 ) {
    Vector3f line = new Vector3f();
    line.sub( p2, p1 );
    Point3f ret = new Point3f();
    double lambda;
    
    if (line.x != 0.0) {
      if (line.x < 0.0) {
        lambda = (lowerCorner.x - p1.x) / line.x;
        ret.x = (float)(p1.x + line.x*lambda);
        ret.y = (float)(p1.y + line.y*lambda);
        ret.z = (float)(p1.z + line.z*lambda);
        
        if (ret.y >= lowerCorner.y && ret.y<=upperCorner.y &&
            ret.z >= lowerCorner.z && ret.z<=upperCorner.z)
          return ret;       
      } else {
        lambda = (upperCorner.x - p1.x) / line.x;
        ret.x = (float)(p1.x + line.x*lambda);
        ret.y = (float)(p1.y + line.y*lambda);
        ret.z = (float)(p1.z + line.z*lambda);
        
        if (ret.y >= lowerCorner.y && ret.y<=upperCorner.y &&
            ret.z >= lowerCorner.z && ret.z<=upperCorner.z)
          return ret;       
      }
    }
 
    if (line.y != 0.0) {
      if (line.y < 0.0) {
        lambda = (lowerCorner.y - p1.y) / line.y;
        ret.x = (float)(p1.x + line.x*lambda);
        ret.y = (float)(p1.y + line.y*lambda);
        ret.z = (float)(p1.z + line.z*lambda);
        
        if (ret.x >= lowerCorner.x && ret.x<=upperCorner.x &&
            ret.z >= lowerCorner.z && ret.z<=upperCorner.z)
          return ret;       
      } else {
        lambda = (upperCorner.y - p1.y) / line.y;
        ret.x = (float)(p1.x + line.x*lambda);
        ret.y = (float)(p1.y + line.y*lambda);
        ret.z = (float)(p1.z + line.z*lambda);
        
        if (ret.x >= lowerCorner.x && ret.x<=upperCorner.x &&
            ret.z >= lowerCorner.z && ret.z<=upperCorner.z)
          return ret;       
      }
    }

    if (line.z != 0.0) {
      if (line.z < 0.0) {
        lambda = (lowerCorner.z - p1.z) / line.z;
        ret.x = (float)(p1.x + line.x*lambda);
        ret.y = (float)(p1.y + line.y*lambda);
        ret.z = (float)(p1.z + line.z*lambda);
        
        if (ret.x >= lowerCorner.x && ret.x<=upperCorner.x &&
            ret.y >= lowerCorner.y && ret.y<=upperCorner.y)
          return ret;       
      } else {
        lambda = (upperCorner.z - p1.z) / line.z;
        ret.x = (float)(p1.x + line.x*lambda);
        ret.y = (float)(p1.y + line.y*lambda);
        ret.z = (float)(p1.z + line.z*lambda);
        
        if (ret.x >= lowerCorner.x && ret.x<=upperCorner.x &&
            ret.y >= lowerCorner.y && ret.y<=upperCorner.y)
          return ret;       
      }
    }
    
    throw new RuntimeException( "Intersection not found" );
  }
  
  /**
    * Return true if box contains point
    */
  private boolean contains( Point3f point ) {
    
    //System.out.println( point );
    
    if (point.x<lowerCorner.x || point.x > upperCorner.x)
      return false;
    
    
    if (point.y<lowerCorner.y || point.y > upperCorner.y)
      return false;
    
    //System.out.println(point.z+"  "+lowerCorner.z+"  "+upperCorner.z);
    
    if (point.z<lowerCorner.z || point.z > upperCorner.z)
      return false;
    
    return true;    
  }
}


