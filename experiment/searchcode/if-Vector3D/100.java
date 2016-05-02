package brain;
import java.awt.Color;

import perspectives.three_d.Vector3D;



public class Tube 
{
	Vector3D[] segments;
	
	Vector3D[] vertices;
	int[] indeces;
	Vector3D[] normals;
	private boolean isSelected;
	
	public Tube(Vector3D[] segments, int faces, double width)
	{
		this.segments = segments;
		this.isSelected = false;
		
	    //find first normal
	    Vector3D prev_dir = segments[1].minus(segments[0]);
	    Vector3D next_dir = prev_dir;

	    boolean found = false;

	    Vector3D n = null;

	    for(int i=2; i< segments.length; i++)
	    {
	        Vector3D dir = segments[i].minus(segments[i-1]);
	        dir.normalize();
	        n = next_dir.cross(dir);

	         if (n.length() > 0.001f)
	         {
	             found = true;
	             n.normalize();
	             break;
	         }
	    }
	    if (!found)
	    {
	        Vector3D x = new Vector3D(1, 0, 0);
	        n = next_dir.cross(x);
	        n.normalize();
	    }//n is the first normal

	   vertices = new Vector3D[segments.length * faces];
	   int cnt = 0;	  

	    for (int j=0; j<segments.length-1; j++)
	    {
	        prev_dir = null;
	        next_dir = null;
	        if (j == 0)
	        {
	            next_dir = segments[1].minus(segments[0]);
	            prev_dir = next_dir;
	        }
	        else
	        {
	            prev_dir = segments[j].minus(segments[j-1]);
	            prev_dir.normalize();
	            next_dir = segments[j+1].minus(segments[j]);
	            next_dir.normalize();

	            Vector3D new_n = betweenPointsNormal(prev_dir, next_dir, n);
	            if (new_n != null) n = new_n;

	            prev_dir = segments[j].minus(segments[j-1]);
	            next_dir = segments[j+1].minus(segments[j]);
	        }

	        //for each segment we need to construct a circle
	        //construct a coordinate system with the z-axis alligned with the segment
	        Vector3D a = (prev_dir.plus(next_dir)).times(0.5f);
	        a.normalize();

	        Vector3D v = a.cross(n);
	        v.normalize();

	        Vector3D u = v.cross(a);
	        u.normalize();

	        //compute the vertices
	        double theta = (2.0*Math.PI)/faces;
	        for (int k=0; k<faces; k++)
	        {
	        	float f1 = (float)(Math.cos( (double) k * theta ));
	        	float f2 = (float)(Math.sin( (double) k * theta ));
	        	
	             Vector3D nn = v.times(f1).plus(u.times(f2));
	             nn.normalize();
	             
	             Vector3D p = segments[j].plus(nn.times((float)width));

	             vertices[cnt++] = p;
	        }
	        
	        for (int k=0; j==segments.length-2 && k<faces; k++)
	        {
	        	float f1 = (float)(Math.cos( (double) k * theta ));
	        	float f2 = (float)(Math.sin( (double) k * theta ));
	        	
	        	Vector3D nn = v.times(f1).plus(u.times(f2));
	             nn.normalize();
	             
	             Vector3D p = segments[j].plus(nn.times((float)width));
	             vertices[cnt++] = p.plus(segments[j+1].minus(segments[j]));
	        }
	    }

	    
	    //compute the triangle indices
	    indeces = new int[(segments.length-1) * faces * 2 * 3];
	    cnt = 0;

	    //now the cylinders
	    for (int j=0; j<segments.length-1; j++)
	    {
	    	for (int k=0; k<faces; k++)
	        {
	            //triangle 1 (out of the 2 that make up a face)
	            if (k != faces-1)
	            {
	                indeces[cnt++] = j*faces+k;
	                indeces[cnt++] = (j+1)*faces+k;
	                indeces[cnt++] = (j+1)*faces+k+1;
	            }
	            else
	            {
	            	indeces[cnt++] = j*faces+k;
	            	indeces[cnt++] = (j+1)*faces+k;
	            	indeces[cnt++] = (j+1)*faces;
	            }

	            //triangle 2
	            if (k != faces-1)
	            {
	            	indeces[cnt++] = j*faces+k;
	            	indeces[cnt++] = (j+1)*faces+k+1;
	            	indeces[cnt++] = j*faces+k+1;
	            }
	            else
	            {
	            	indeces[cnt++] = j*faces+k;
	            	indeces[cnt++] = (j+1)*faces;
	            	indeces[cnt++] = j*faces;
	            }
	        }
	    }
	    
	    
	    //compute normals
	    normals = new Vector3D[vertices.length];
	    cnt++;
	    
	    //go through all triangles	   
	    int[] vcounts = new int[vertices.length];
	    
	    for (int i=0; i<vertices.length; i++)
	    {
	    	vcounts[i] = 0;
	    	normals[i] = new Vector3D(0,0,0);
	    }
	    for (int i=0; i<indeces.length; i+=3)
	    {
		      Vector3D v1 = vertices[indeces[i]].minus(vertices[indeces[i+1]]); v1.normalize();
		      Vector3D v2 = vertices[indeces[i]].minus(vertices[indeces[i+2]]); v2.normalize();		      
		      Vector3D normal = v2.cross(v1);
		      normal.normalize();
		      
		      normals[indeces[i]] = normals[indeces[i]].plus(normal);
		      normals[indeces[i+1]] = normals[indeces[i+1]].plus(normal);
		      normals[indeces[i+2]] = normals[indeces[i+2]].plus(normal);
		      
		      vcounts[indeces[i]]++;
		      vcounts[indeces[i+1]]++;
		      vcounts[indeces[i+2]]++;     
	    }
	    
	    for (int i=0; i<normals.length; i++)
	    	normals[i] = normals[i].div(vcounts[i]);
	    
	}
	
	private Vector3D betweenPointsNormal(   Vector3D prev_dir,   Vector3D next_dir,   Vector3D prev_n)
	{

		  boolean flip = false;
	
		  //check if the rotation from
		  //prev_dir to next_dir is greater than pi/2
		  if( (prev_dir.dot(next_dir)) < 0.0f ) flip = true;
	
	
		  Vector3D up = prev_dir.cross(next_dir);
	
		  //if |up| == 0 then use the previous normal
		  if(up.length() == 0) return null;
	
		  up.normalize();
	
		  Vector3D sum = prev_dir.plus(next_dir);
		  sum.normalize();
	
		  double x = up.dot(prev_n);
	
		  double y =  1.0f - (x * x);
	
		  if(y > 0.0f) {
		    y = Math.sqrt(y);
		  } else {
		    y = 0.0f;
		  }
	
		  //build a coordinate system
		  Vector3D v = sum.cross(up);
		  sum = prev_dir.cross(up);
		  if( ( (prev_n.dot(sum)) * (v.dot(sum)) ) < 0.0f ) {
		    y = -1.0f * y;
		  }
	
		 Vector3D new_n = ((up.times((float)x)).plus((v.times((float)y))));
		
		  new_n.normalize();
		  
		  return new_n;
	}
	
	public double getDistance(Tube anotherTube)
	{
		return 1;
	}
	
	public static double getDistance(Vector3D[] tube1, Vector3D[] tube2)
	{
		return 1;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
}

