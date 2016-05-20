
import java.awt.Color;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;
import javax.vecmath.Point3d;

import processing.core.*;



public class CodeFluxArnaud extends PApplet implements MouseWheelListener{
	
// variables
	
	PFont font;

	int timer;
	
	boolean restart,pause;
	
	Vector<Attractor> AttractorList; // liste d'attracteur, peut la remplir au fur et  mesure
	Vector<Particle> ParticleList;
	
	
	//Arcball
	//------------------------------------------------------------
	// globals
	//------------------------------------------------------------


	float       DEGTORAD=PI/180;

	float       startDragX, startDragY;
	float       rX, rY;
	vector3     circlePointStart, circlePointEnd, spherePointStart, spherePointEnd;
	Quat  cubeQuat, cubeQuatStart;
	Quat  mouseQuat;
	float[]     cubeMatrix;
	ArcBall_     myArcBall;
	
	float zoom =3.4f; // initialise taille du zoom (f pour declaration en float)

	boolean     classicMethod = false;
	
	//fin arcball
	
	public void setup() {
				
		size(1200, 700, OPENGL);

	  	colorMode(RGB, 255, 255, 255,100);
	  	noStroke();
	  	background(0);

	  	//zoom
	  	this.addMouseWheelListener(this);
	  	
	  	//ArcBall
	    // ArcBall
	    myArcBall = new ArcBall_();
	    
	    // Points when user clicks on the screen
	    circlePointStart = new vector3();
	    spherePointStart = new vector3();
	    
	    // Points when user drags the mouse pointer
	    circlePointEnd = new vector3();
	    spherePointEnd = new vector3();
	    
	    // Rotation of Cube (Quat for Arcball, matrix to hold its appropriate transform matrix for Processing)
	    cubeQuat         = new Quat();
	    cubeMatrix             = new float[9];
	      // Its start Quat (when mouse clicked)
	    cubeQuatStart    = new Quat();
	    
	    // Amount of Rotation (cubeQuat = mouseQuat * cubeQuatStart)
	    mouseQuat = new Quat();
	    //fin arcball
	    
	   // font = loadFont("CourierNew36.vlw"); 
	    
	  	// intialise les variables
	    timer=0;
	  	restart = false;
	  	pause = false;
	  	
	  	AttractorList = new Vector<Attractor>(); // () car vide
	  	
	  	Attractor attra1 = new Attractor (0,0,1);
	  	Attractor attra2 = new Attractor (60,-40,1);
	  	AttractorList.add( attra1 );
	  	AttractorList.add( attra2 );
	  	
	  	ParticleList = new Vector<Particle>(); // () car vide
	  	
	  	Particle parti1 = new Particle (1,1,0,0,0);
	  	ParticleList.add( parti1 );
	  	
	  	
	  	
	}

	public void draw() {
		
		timer++;
		
		//noStroke();

		background(0);
		//lights();
		//ambientLight(0, 0, 0, 0, 0, 0);
		//directionalLight(126, 126, 126, 0, 0, -1);
		//directionalLight(126, 126, 126, 0, -1, 0);

		//arcball
		// Center on Screen
		translate(width/2,height/2);
		//translate((float) (.5*(width - 2 * (WorldDimension-2)*rectlength - diminter-dimextra)),height/2);
		
		// Arcball 
		if (!classicMethod)
		{
		  cubeQuat = multiply(mouseQuat, cubeQuatStart);
		  cubeQuat.normalize();
		  cubeMatrix = cubeQuat.toMatrix3();
		  applyMatrix
		  (
		  cubeMatrix[0], cubeMatrix[3], cubeMatrix[6], 0.0f,
		  cubeMatrix[1], cubeMatrix[4], cubeMatrix[7], 0.0f,
		  cubeMatrix[2], cubeMatrix[5], cubeMatrix[8], 0.0f,
		  0.0f,           0.0f,           0.0f,     1.0f);
		}
		else
		{
		  rotateX ( (float) ((circlePointEnd.y - circlePointStart.y)*360.0/height * DEGTORAD));
		  rotateY ( (float) ((circlePointEnd.x - circlePointStart.x)*360.0/width * DEGTORAD));
		}
		scale(zoom);
		
		//dessine un disque
		fill(255,255,255,255); // remplit tout ce qui suit la couleur defini
		ellipse(0,0,200,200);
		
		double vx, vy , x0 , y0, intensity, norm;
		
		for (double x =-100; x<100; x+=5){
			for (double y =-100; y<100; y+=5){
				
				vx=0;
				vy=0;
				
				for(int i=0; i<AttractorList.size(); i+=1){ // calcul vecteur en un point pour chaque attracteur
					
					x0 = AttractorList.elementAt(i).x; // le x de l'attracteur
					y0 = AttractorList.elementAt(i).y;
					intensity = AttractorList.elementAt(i).intensity;
					
					// vx+= intensity * - (y-y0) / Math.sqrt((x-x0)*(x-x0)+(y-y0)*(y-y0));//somme sur tout les attracteurs
					// vy+= intensity * (x-x0) / Math.sqrt((x-x0)*(x-x0)+(y-y0)*(y-y0));
					
					vx += AttractorList.elementAt(i).force(x,y).x;
					vy += AttractorList.elementAt(i).force(x,y).y;
			
					
				}
				
				norm =  Math.sqrt(vx*vx+vy*vy);
				vx = vx / norm;
				vy = vy / norm;
				
				stroke (255,0,0); //couleur ligne
				line ((float)x,(float)y, (float)(x+5*vx), (float)(y+5*vy));// dessine une ligne, represente le vecteur
				
			}
			
		}
		
		for (int i=0; i<ParticleList.size();i++){
			ParticleList.elementAt(i).computeForce(AttractorList);
			ParticleList.elementAt(i).updatePosition(1, AttractorList);
		
		
			pushMatrix(); // pour que chaqu boite soit projet depuis milieu
			translate((float)ParticleList.elementAt(i).x,(float)ParticleList.elementAt(i).y, 0); // car positionne boite au milieu par defaut
			box(5); 
			popMatrix();//affiche une boite
			
		}
		
		
		
	    
	}
	
// objet qu'on cre
	public class Attractor {
		
		double x;
		double y;
		double intensity;
		
		public Attractor(double x, double y, double intensity) {
			super();
			this.x = x;
			this.y = y;
			this.intensity = intensity;
		}
		
		public Point2D force(double x, double y){ // fonction c'est force et renvoie un type: le Point2D
			
			Point2D p = new Point2D(0,0); 
			
			p.x= intensity * - (y-this.y) / Math.sqrt((x-this.x)*(x-this.x)+(y-this.y)*(y-this.y));
			p.y= intensity * (x-this.x) / Math.sqrt((x-this.x)*(x-this.x)+(y-this.y)*(y-this.y));
			
			return p;
			
		} 
		
		
	}
	
	
	
public class Particle {
		
		double x;
		double y;
		int type;
		double fx;
		double fy;
		
		
		public Particle(double x, double y, int type, double fx, double fy) {
			super();
			this.x = x;
			this.y = y;
			this.type = type;
			this.fx = fx;
			this.fy = fy;
		}



		public void computeForce( Vector<Attractor> attrList){ 
			
			this.fx = 0 ;
			this.fy = 0 ;
			Point2D forcetemp;
			
			for (int i=0; i<attrList.size(); i++){
				
				forcetemp = attrList.elementAt(i).force(this.x,this.y);
				this.fx +=forcetemp.x;
				this.fy +=forcetemp.y;
				
			}
			
			double norm = Math.sqrt(this.fx*this.fx+this.fy*this.fy);
			this.fy /= norm;
			this.fx /= norm;
			
		}
				
							
				
			public void updatePosition(double dt, Vector<Attractor> attrList){ // fonction qui ne renvoie rien
			
			this.x += dt*this.fx;
			this.y += dt*this.fy;
			
			
		} 
		
		
	}


public class Point2D{
	
	double x;
	double y;
	
	public Point2D(double x, double y) {
		super();
		this.x = x;
		this.y = y;
		
	}
	
	
	
	
}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	 public void mouseWheelMoved(MouseWheelEvent e) {
		 int notches = e.getWheelRotation();
	     zoom -= zoom * notches/50;
	 }


	
	public void mousePressed()
	{		
		
	    	circlePointStart.x = (float) ((mouseX - 0.5*width) / (0.5*width));
			circlePointStart.y = (float) ((mouseY - 0.5*height) / (0.5*width));
			spherePointStart = myArcBall.getPointSphere(circlePointStart);
			cubeQuatStart.set(cubeQuat);
			mouseQuat.setIdentity();
	

	}

	public void mouseDragged()
	{
	  if (!classicMethod)
	  {
	    circlePointEnd.x = (float) ((mouseX - 0.5*width) / (0.5*width));
	    circlePointEnd.y = (float) ((mouseY - 0.5*height) / (0.5*width));
	    spherePointEnd = myArcBall.getPointSphere(circlePointEnd);
	    mouseQuat = myArcBall.getQuat(spherePointStart, spherePointEnd);

	   }
	  else
	  {
	    circlePointEnd.x = mouseX;
	    circlePointEnd.y = mouseY;
	  }
	}

	public void keyPressed()
	{
 
	 if (key == ' ')
	 {
		 pause=!pause;
	 }
	 if (key == 'm')
	 {

	 }
	 if (key == 'r')
	 {
		 restart = true;
	 }
	 /*
	 if (key == 'v')
	 {
		 neighborhood=!neighborhood;
	 }
	 if (key == 's')
	 {
		 select=!select;
	 }*/
	 
	}

	 void initVar()
	 {

	     mouseQuat.setIdentity();
	     cubeQuatStart.setIdentity();
	     cubeQuat.setIdentity();
	     circlePointStart.zero();
	     circlePointEnd.zero();
	       
	}



	//------------------------------------------------------------
	// vector3
	//------------------------------------------------------------

	class vector3
	{
	  float x,y,z;
	  
	  vector3()
	  {
	    this.x = (float) 0.0;
	    this.y = 0.0f;
	    this.z = 0.0f;
	  }
	  
	   vector3(float x, float y, float z)
	  {
	    x = 0.0f;
	    y = 0.0f;
	    z = 0.0f;
	  }
	  
	 void norm()
	  {
	    float n = sqrt(x*x + y*y + z*z);
	    this.x /= n;
	    this.y /= n;
	    this.z /= n;
	  }
	  
	 void zero()
	  {
	    x = 0.0f;
	    y = 0.0f;
	    z = 0.0f; 
	  }
	}

	vector3 cross(vector3 a, vector3 b)
	  {
	        vector3 result = new vector3();
		result.x = a.y*b.z - a.z*b.y;
		result.y = a.z*b.x - a.x*b.z;
		result.z = a.x*b.y - a.y*b.x; 

	      return result;
	  }

	  



	//------------------------------------------------------------
	// Quat class
	//------------------------------------------------------------

	class Quat
	{

	  float r,x,y,z;
	  
	 
	  
	  Quat()
	  {
	    setIdentity();
	  }
	  
	  Quat(float r, float x, float y, float z)
	  {
	    set(r,x,y,z);
	  }
	  
	  void setIdentity()
	  {
	    this.r = 1.0f;
	    this.x = 0.0f;
	    this.y = 0.0f;
	    this.z = 0.0f;
	  }
	  
	  void set(float r,float x,float y,float z)
	  {
	    this.r = r;
	    this.x = x;
	    this.y = y;
	    this.z = z;  
	  }
	 
	  void set(float r, vector3 v)
	  {
	      this.r = r;
	      this.x = v.x;
	      this.y = v.y;
	      this.z = v.z;
	  }

	  void set(Quat q)
	  {
	      this.r = q.r;
	      this.x = q.x;
	      this.y = q.y;
	      this.z = q.z;
	  
	  
	  }

	  void normalize()
	  {
	    float norm;
	    norm = sqrt(r*r + x*x + y*y + z*z);

	    this.r /= norm;
	    this.x /= norm;
	    this.y /= norm;
	    this.z /= norm;
	  }  
	  
	  boolean isUnit()
	  {
	    return (r*r + x*x + y*y + z*z > 0.99);
	  }  
	  
	 void conjuguate()
	  {
	    this.x = -this.x;
	    this.y = -this.y;
	    this.z = -this.z;  
	  }

	 void multiplyBy(Quat q2)
	{
		Quat qTemp=new Quat();
		qTemp.r = this.r;
		qTemp.x = this.x;
		qTemp.y = this.y;
		qTemp.z = this.z;

		this.r = qTemp.r * q2.r - (qTemp.x * q2.x + qTemp.y * q2.y + qTemp.z * q2.z);
		this.x = qTemp.r * q2.x +  q2.r * qTemp.x + qTemp.y * q2.z - qTemp.z * q2.y; 
		this.y = qTemp.r * q2.y -  q2.z * qTemp.x + qTemp.y * q2.r + qTemp.z * q2.x;
		this.z = qTemp.r * q2.z +  q2.y * qTemp.x - qTemp.y * q2.x + qTemp.z * q2.r;

	}

	void multiplyBy(float a)
	{
		this.r *= a;
		this.x *= a;
		this.y *= a;
		this.z *= a;
	}


	void vectorTransform(vector3 v)
	{
	   this.r = 0;
	   this.x = v.x;
	   this.y = v.y;
	   this.z = v.z;

	}


	float[] toMatrix3()
	{
		float rx, ry, rz, xx, yy, yz, xy, xz, zz, x2, y2, z2; 
	        float[] matrix = new float[9];
	        
		x2 = this.x + this.x; 
		y2 = this.y + this.y; 
		z2 = this.z + this.z;
		
		xx = this.x * x2; xy = this.x * y2; xz = this.x * z2;
		yy = this.y * y2; yz = this.y * z2; zz = this.z * z2;
		rx = this.r * x2; ry = this.r * y2; rz = this.r * z2;


		matrix[0] = (float) (1.0 - (yy + zz)); 
		matrix[1] = xy + rz; 
		matrix[2] = xz - ry; 

		matrix[3] = xy - rz;
		matrix[4] = (float) (1.0 - (xx + zz));
		matrix[5] = yz + rx;

		matrix[6] = xz + ry; 
		matrix[7] = yz - rx; 
		matrix[8] = (float) (1.0 - (xx + yy)); 

	        return matrix;

	}
	}





	// -------------------------------------------------------------
	// Multiply()
	// q = q1*q2
	// -------------------------------------------------------------

	Quat multiply(Quat q1, Quat q2)
	  {
	  
	    Quat result = new Quat();
	    result.r = q1.r * q2.r - (q1.x * q2.x + q1.y * q2.y + q1.z * q2.z);
	    result.x = q1.r * q2.x +  q2.r * q1.x + q1.y * q2.z - q1.z * q2.y; 
	    result.y = q1.r * q2.y -  q2.z * q1.x + q1.y * q2.r + q1.z * q2.x;
	    result.z = q1.r * q2.z +  q2.y * q1.x - q1.y * q2.x + q1.z * q2.r;
	    
	    return result;
	  }


	Quat multiply(Quat q, float a)
	{
	  Quat result = new Quat();
	  result.r = q.r * a;
	  result.x = q.x * a;
	  result.y = q.y * a;
	  result.z = q.z * a;
	  return result;
	}



	// -------------------------------------------------------------
	// vectorRotate()
	// Rotates a vector3 , rotation given by a Quat
	// result = q.v.q^-1
	// -------------------------------------------------------------

	vector3 vectorRotate(vector3 v, Quat rot)
	{
	      vector3 result   = new vector3();
	      Quat invr  = new Quat();
	      Quat qv    = new Quat();
	      Quat tmp   = new Quat();
		
	      invr.r =  rot.r;
	      invr.x = -rot.x;
	      invr.z = -rot.y;
	      invr.z = -rot.z;
	      
	      qv.vectorTransform(v);
		
	      tmp = multiply(qv, invr);
	      invr = multiply(rot, tmp); 
		
		result.x = invr.x;
		result.y = invr.y;
		result.z = invr.z;	

	      return result;
	}

	// -------------------------------------------------------------
	// eulerTransform
	// Takes 3 angles, and returns a Quat
	// -------------------------------------------------------------

	Quat eulerTransform(float alpha, float beta, float gamma)
	{
	        Quat result = new Quat();
		float cr, cp, cy, sr, sp, sy, cpcy, spsy;

		alpha *= DEGTORAD;
		beta  *= DEGTORAD;
		gamma *= DEGTORAD;

		cr = cos(alpha/2);

		cp = cos(beta/2);
		cy = cos(gamma/2);


		sr = sin(alpha/2);
		sp = sin(beta/2);
		sy = sin(gamma/2);
		
		cpcy = cp * cy;
		spsy = sp * sy;


		result.r = cr * cpcy + sr * spsy;
		result.x = sr * cpcy - cr * spsy;
		result.y = cr * sp * cy + sr * cp * sy;
		result.z = cr * cp * sy - sr * sp * cy;

	        return result;
	}


	// -------------------------------------------------------------
	// SLERP: Spherical Linear Interpolation
	// Step from q1 to q2, 0=<t=<1
	// SLERP(q1,q2,0) = q1
	// SLERP(q1,q2,1) = q2
	// -------------------------------------------------------------

	Quat SLERP(Quat q1, Quat q2, float t)
	{
	        Quat result = new Quat();
	        float[]     to1 = new float[4];
	        float       omega, cos_omega, sin_omega, scale0, scale1;


	        // calc cosine
	        cos_omega = q1.r*q2.r + q1.x*q2.x + q1.y*q2.y + q1.z*q2.z;


	        // adjust signs (if necessary)
	        if ( cos_omega <0.0 ){ 
	        	cos_omega = -cos_omega; 
	        	to1[0] = - q2.r;
	        	to1[1] = - q2.x;
			to1[2] = - q2.y;
			to1[3] = - q2.z;
			
	        } else  {
	        	to1[0] =   q2.r;
	        	to1[1] =   q2.x;
			to1[2] =   q2.y;
			to1[3] =   q2.z;
	        }


	        // calculate coefficients


	       if ( (1.0 - cos_omega) > 0.01 ) {
	                // standard case (slerp)
	                omega = (float)Math.acos(cos_omega);
	                sin_omega = sin(omega);
	                scale0 = sin((float) ((1.0 - t) * omega)) / sin_omega;
	                scale1 = sin(t * omega) / sin_omega;


	        } else {        
	    	    // "from" and "to" Quats are very close 
		    //  ... so we can do a linear interpolation
	                scale0 = (float) (1.0 - t);
	                scale1 = t;
	        }
		// calculate final values
		result.r = scale0 * q1.r + scale1 * to1[0];
		result.x = scale0 * q1.x + scale1 * to1[1];
		result.y = scale0 * q1.y + scale1 * to1[2];
		result.z = scale0 * q1.z + scale1 * to1[3];

	        return result;
	}

	// -------------------------------------------------------------
	// ArcBall
	// -------------------------------------------------------------

	class ArcBall_
	{

	  ArcBall_()
	  {
	  
	  }
	  
	  vector3 getPointSphere(vector3 P)
	  {
		float r = P.x*P.x + P.y*P.y;
	        vector3 result = new vector3();
	    
	        result.x = P.x;
	        result.y = P.y;
	        result.z = P.z;    
	        
		if(r>1.0)
		{	float sr = sqrt(r);
			result.x /= sr;
			result.y /= sr;	
			result.z = 0.0f;
		}
		else
			result.z = sqrt( (float) (1.0-r));  

	    

	    return result;
	  
	  }
	  Quat getQuat(vector3 pStartPoint, vector3 pEndPoint)
	  {
	      Quat  result       = new Quat();
	      vector3     crossResult  = new vector3();
	      float       dotResult;
	        
	 
		crossResult = cross(pStartPoint,pEndPoint);
	        dotResult   = pStartPoint.x*pEndPoint.x + pStartPoint.y*pEndPoint.y + pStartPoint.z*pEndPoint.z;
	        
		result.set(dotResult, crossResult);  
	      
	      return result;
	  }
	  
	}
	
	
}
