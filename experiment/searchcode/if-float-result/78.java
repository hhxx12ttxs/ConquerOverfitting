
import java.awt.Color;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;
import processing.core.*;


public class CircleLayer extends PApplet implements MouseWheelListener{
	
	PFont font;
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
	
	float zoom = .50f;

	boolean     classicMethod = false;
	
	int timer;
	boolean pause;
	int numLayer;
	int numParticle, numParticleTotal;
	Vector<Float> LayerRadius;
	Layer[] LayerArray;
	int numGate0;
	Vector<Gate> gates0;
	Vector<Gate> gatesEXT;
	
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

	  	timer=0;
	  	pause = false;
	  	
	  	numParticleTotal = 1000;
	  	numParticle = numParticleTotal;
	  	numLayer = 3;
	  	
	  	
	  	//premier ensemble de gates
	  	numGate0 = 5;
	  	gates0 = new Vector<Gate>();
	  	gatesEXT = new Vector<Gate>();
	  	Gate gate0;
	  	for(int i=0;i<numGate0;i++){
	  		gate0 = new Gate((float)i*2.0f * (float)Math.PI / (float)numGate0, (float)Math.random()*.01f, 0,null);
	  		gates0.add(gate0);
	  	}
	  	
	  	LayerArray = new Layer[numLayer];
	  	
	  	LayerArray[0] = new Layer(200.0f, 200.0f, 0, gates0, new Vector<Particle>(), new wbTest(0, 4.0f, 20.0f));
	  	LayerArray[1] = new Layer(400.0f,300.0f, 1, new Vector<Gate>(), new Vector<Particle>(), new wbTest(1, 2.0f, 60.0f));
	  	LayerArray[2] = new Layer(700.0f,150.0f, 2, new Vector<Gate>(), new Vector<Particle>(), new wbTest(2, 1.0f, 10.0f));

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
		
		
		System.out.println("timer "+timer+"    numParticle "+numParticle);
		
		
		//draw layer circles
		for(int i=numLayer-1;i>-1;i--){
			float bloc = 255.0f/(float)numLayer;
			fill(bloc*(float)(i+1),bloc*(float)(i+1),bloc*(float)(i+1));
			//System.out.println(i+"  ieue  "+bloc*(float)(i+1));
			pushMatrix(); 
			translate(0,0, -i);
			ellipse(.0f,.0f, 2.0f*(LayerArray[i].radius+LayerArray[i].depth), 2.0f*(LayerArray[i].radius+LayerArray[i].depth) );
			popMatrix();
		}
		//draw empty center
		fill(0,0,0);
		pushMatrix(); 
		translate(0,0, 1);
		ellipse(.0f,.0f, 2.0f*(LayerArray[0].radius), 2.0f*(LayerArray[0].radius));
		popMatrix();
		
		//draw gate
		for(int i=0;i<numLayer;i++){
			
			float radiusLayer = LayerArray[i].radius;
			//System.out.println(i+"   reasuite  "+LayerArray[i].inputGates.size()+"   "+radiusLayer);
			
			for(int j=0;j<LayerArray[i].inputGates.size();j++){
				
				if( LayerArray[i].inputGates.elementAt(j).isEmpty() ){
					fill(0,120,120);
					//System.out.println(j+"    vide");
				}
				else{
					fill(0,0,255);
					//System.out.println(j+"    full");
				}
				
				float thetaGate = LayerArray[i].inputGates.elementAt(j).theta;
				float dr = LayerArray[i].inputGates.elementAt(j).dr;
				
				float gatex = (radiusLayer + dr) * (float)Math.cos(thetaGate);
				float gatey = (radiusLayer + dr) * (float)Math.sin(thetaGate);
				
				pushMatrix(); 
				translate(0,0, 2);
				ellipse( gatex , gatey, 14,14);
				popMatrix();
			}
			
		}
		//draw last gate
		float radiusLayer = LayerArray[numLayer-1].radius + LayerArray[numLayer-1].depth;
		
		for(int j=0;j<gatesEXT.size();j++){
			
			if( gatesEXT.elementAt(j).isEmpty() ){
				fill(0,120,120);
				//System.out.println(j+"    vide");
			}
			else{
				fill(0,0,255);
				//System.out.println(j+"    full");
			}
			
			float thetaGate = gatesEXT.elementAt(j).theta;
			float dr = gatesEXT.elementAt(j).dr;
			
			float gatex = (radiusLayer + dr) * (float)Math.cos(thetaGate);
			float gatey = (radiusLayer + dr) * (float)Math.sin(thetaGate);
			
			pushMatrix(); 
			translate(0,0, 2);
			ellipse( gatex , gatey, 14,14);
			popMatrix();
		}
		
		
		//main loop
		for(int i=0;i<numLayer;i++){
			LayerArray[i].wordBehavior.tick();
		}
		
		
		
		//ajoute des particles aux portes de la couche 0
		if(numParticle>0){
			int gateID = (int)( (float)LayerArray[0].inputGates.size() * Math.random() );
			if(LayerArray[0].inputGates.elementAt(gateID).isEmpty()){
				LayerArray[0].inputGates.elementAt(gateID).attachedPart = new Particle(.0f,.0f,.0f,.0f,.0f);
				numParticle--;
			}
		}
		
	/*
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
	
	public class Gate {
		
		float theta;
		float dr;
		int layer;
		Particle attachedPart;
		
		public Gate(float theta, float dr, int layer, Particle attachedPart) {
			super();
			this.theta = theta;
			this.dr = dr;
			this.layer = layer;
			this.attachedPart = attachedPart;
		}



		public boolean isEmpty(){
			return (this.attachedPart==null);
		}
	}
	
	
	public class Layer {
		
		float radius;
		float depth;
		int rank;
		Vector<Gate> inputGates;
		Vector<Particle> particles;
		WorldBehavior wordBehavior;
		
		public Layer(float radius, float depth, int rank,
				Vector<Gate> inputGates, Vector<Particle> particles,
				WorldBehavior wordBehavior) {
			super();
			this.radius = radius;
			this.depth = depth;
			this.rank = rank;
			this.inputGates = inputGates;
			this.particles = particles;
			this.wordBehavior = wordBehavior;
		}
	}
	
	
	public class Particle {
		
		float x;
		float y;
		float dx;
		float dy;
		float paramf;
		
		public Particle(float x, float y, float dx, float dy, float paramf) {
			super();
			this.x = x;
			this.y = y;
			this.dx = dx;
			this.dy = dy;
			this.paramf = paramf;
		}
	}
	
	public class WorldBehavior {
		
		int layerRank;
		float ratioGate;
		
		public WorldBehavior(int layerRank, float ratioGate) {
			super();
			this.layerRank = layerRank;
			this.ratioGate = ratioGate;
		}

		public void tick(){
			computeDisplacment();
			
			//si la couche suivante n a pas assez de porte d entree, on en cr�e
			if( layerRank < numLayer-1 ){
				if( (float)LayerArray[layerRank+1].inputGates.size() 
							< this.ratioGate * LayerArray[layerRank].inputGates.size() ){
					
					//System.out.println("create gate");
					createGate( LayerArray[layerRank+1].inputGates );
				}
			}
			else{
				//si derniere couche on remplit la liste gateEXTERNAL
				if( (float)gatesEXT.size() 
						< this.ratioGate * LayerArray[layerRank].inputGates.size() ){
					createGate( gatesEXT );
				}
			}
			
			updatePosition();
			addParticle();
			
			
			if( layerRank < numLayer-1 ){
				transfertParticle( LayerArray[layerRank+1].inputGates );
			}
			else{
				//transfertParticle( gatesEXT );
			}

			render();
		}
		
		public void computeDisplacment() {}
		
		public void createGate(Vector<Gate> gates) {}
		
		public void updatePosition() {}
		
		public void addParticle() {}
		
		public void transfertParticle(Vector<Gate> gates) {}
		
		public void render() {}
	
	}
	
	public class wbTest extends WorldBehavior {

		float speed;
		
		public wbTest(int layerRank, float ratioGate, float speed) {
			super(layerRank, ratioGate);
			// TODO Auto-generated constructor stub
			this.speed = speed;
		}

		public void computeDisplacment() {
			
			//System.out.println(layerRank+" computeDisplacment");
			Particle parttemp;
			float randtheta,randX;
			
			for(int i=0;i<LayerArray[layerRank].particles.size();i++){
				
				parttemp = LayerArray[layerRank].particles.elementAt(i);
				
				randtheta = ((float)Math.random()-.5f) * .5f;
				randX = this.speed*(float)Math.random();
				
				parttemp.dx = randX * (float)Math.cos(randtheta + parttemp.paramf);
				parttemp.dy = randX * (float)Math.sin(randtheta + parttemp.paramf);
				
				LayerArray[layerRank].particles.set(i, parttemp);
				
			}
		
		}
		
		public void createGate(Vector<Gate> gates) {
			
			//System.out.println(layerRank+" createGate");
			Particle parttemp;
			float tempx, tempy;
			float radius, radiusMax = LayerArray[layerRank].radius + LayerArray[layerRank].depth;
			
			for(int i=0;i<LayerArray[layerRank].particles.size();i++){
				
				parttemp = LayerArray[layerRank].particles.elementAt(i);
				
				tempx = parttemp.x + parttemp.dx;
				tempy = parttemp.y + parttemp.dy;
				
				radius = (float)Math.sqrt(tempx*tempx+tempy*tempy);
				
				if(radius>radiusMax){
					
					float thetaGate = (float) Math.atan2(parttemp.y, parttemp.x);
					gates.add(new Gate( thetaGate , .0f, layerRank+1,null));
				}
				
			}
		}
		
		public void updatePosition() {
			
			//System.out.println(layerRank+" updatePosition");
			Particle parttemp;
			float radius;
			float radiusMin = LayerArray[layerRank].radius;
			float radiusMax = LayerArray[layerRank].radius + LayerArray[layerRank].depth;
			
			for(int i=0;i<LayerArray[layerRank].particles.size();i++){
				
				parttemp = LayerArray[layerRank].particles.elementAt(i);
				
				parttemp.x += parttemp.dx;
				parttemp.y += parttemp.dy;
				
				radius = (float)Math.sqrt(parttemp.x*parttemp.x+parttemp.y*parttemp.y);
				
				//System.out.println(i+"   "+radius+"    "+ radiusMin+"    "+radiusMax);
				
				//si on rentre dans un mur on est renvoyé
				if(radius>radiusMax || radius<radiusMin){
					parttemp.x -= parttemp.dx;
					parttemp.y -= parttemp.dy;
					
					float vecx = (float)Math.cos(parttemp.paramf);
					float vecy = (float)Math.sin(parttemp.paramf);
					
					float a = parttemp.x / radius;
					float b = parttemp.y / radius;
					
					float projN = vecx * a + vecy * b;
					float projT = - vecx * b + vecy * a;
					
					vecx = - projN * a - projT * b;
					vecy = - projN * b + projT * a;
					
					parttemp.paramf = (float)Math.atan2(vecy, vecx);
					
					//parttemp.paramf += (float)Math.PI;
					//System.out.println("virage");
				}
				
				LayerArray[layerRank].particles.set(i, parttemp);
				
			}
		}
		
		public void addParticle() {
			
			//System.out.println(layerRank+" addParticle");
			
			for(int i=0;i<LayerArray[layerRank].inputGates.size();i++){
				
				if(!LayerArray[layerRank].inputGates.elementAt(i).isEmpty()){
					
					//on recupere la particle
					Particle temppart = LayerArray[layerRank].inputGates.elementAt(i).attachedPart;
					
					//on lui attribue de nouvelles coordonn�e a cot� de la porte
					float thetaGate = LayerArray[layerRank].inputGates.elementAt(i).theta;
					float dr = LayerArray[layerRank].inputGates.elementAt(i).dr;
					float radiusLayer = LayerArray[layerRank].radius;
					
					float randtheta = thetaGate + ((float)Math.random()-.5f) * (float)Math.PI;
					float distGate = 10.0f;
					
					temppart.x = (radiusLayer + dr) * (float)Math.cos(thetaGate) + distGate * (float)Math.cos(randtheta);
					temppart.y = (radiusLayer + dr) * (float)Math.sin(thetaGate) + distGate * (float)Math.sin(randtheta);
					temppart.paramf = randtheta;
					
					//on ajoute la particle en attente a la porte dans la liste des particules de la couche
					LayerArray[layerRank].particles.add( temppart );
					
					//on vide la porte
					LayerArray[layerRank].inputGates.elementAt(i).attachedPart = null;	
				}
			}
			
		}
		
		public void transfertParticle(Vector<Gate> gates) {
			
			//System.out.println(layerRank+" transfertParticle");
			Particle parttemp;
			float dist, gatex,gatey, dr, thetaGate;
			float radiusEXT = LayerArray[layerRank].radius + LayerArray[layerRank].depth;

			
			for(int i=0;i<LayerArray[layerRank].particles.size();i++){
				
				parttemp = LayerArray[layerRank].particles.elementAt(i);
				
				for(int j=0;j<gates.size();j++){
					
					if(gates.elementAt(j).isEmpty()){	
					
						thetaGate = gates.elementAt(j).theta;
						dr = gates.elementAt(j).dr;
						
						gatex = (radiusEXT + dr) * (float)Math.cos(thetaGate);
						gatey = (radiusEXT + dr) * (float)Math.sin(thetaGate);
						
						dist = (gatex - parttemp.x) * (gatex - parttemp.x)
								+ (gatey - parttemp.y) * (gatey - parttemp.y);
						
						if(dist < 300.0f && gates.elementAt(j).isEmpty()){						
							gates.elementAt(j).attachedPart = parttemp;
							
							System.out.println("world   "+layerRank+"   layersize "+LayerArray[layerRank].particles.size()+"     ipart "+i+"   gate "+j);
							LayerArray[layerRank].particles.remove(i);
							
							//dont test othergate
							j = 999999999;
							
							//car sinon on saute la part apres celle enlev�
							//i--;
						}
					}		
					
				}
				
			}
			
			
		}
		
		public void render() {
			
			//System.out.println(layerRank+" render");
			
			fill(255, 0, 0);
			float colorratio;
			
			for(int i=0;i<LayerArray[layerRank].particles.size();i++){
				
				//float colorratio = (LayerArray[layerRank].particles.elementAt(i).paramf/(2.0f*(float)Math.PI) + .5f);
				
				colorratio = 1.0f;//(float)i / (float)numParticleTotal;
				fill(255f * colorratio, 255f * (float)Math.sin(Math.PI * colorratio), 255f * (1f-colorratio) );
				
				pushMatrix(); 
				translate(LayerArray[layerRank].particles.elementAt(i).x,LayerArray[layerRank].particles.elementAt(i).y, 0);
				box(10);
				popMatrix();
				
			}
		
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
	 if (key == 'l')
	 {
		
	 }

	 
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
