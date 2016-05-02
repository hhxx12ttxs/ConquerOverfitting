package btMath;


public class Vector3D{

	private float[] vals = new float[4];
	private int Thetaquad;
	public static final Vector3D Origin = new Vector3D(0,0,0); 
	
	/**************Constructors****************/
	public Vector3D(){
		vals[0]=0;
		vals[1]=0;
		vals[2]=0;
		vals[3]=1;
	}//Origin Constructor
	
	public Vector3D(float x, float y, float z){
		vals[0]=x;
		vals[1]=y;
		vals[2]=z;
		vals[3]=1;
		Round();
		findThetaquad();
	}//float Constructor
	
	public Vector3D(double x, double y, double z){
		vals[0]=(float)x;
		vals[1]=(float)y;
		vals[2]=(float)z;
		vals[3]=1;
		Round();
		findThetaquad();
	}//double Constructor
	
	public Vector3D(int x, int y, int z){
		vals[0]=x;
		vals[1]=y;
		vals[2]=z;
		vals[3]=1;
		findThetaquad();
	}//int Constructor
	
	public Vector3D(Vector3D pt){
		vals[0]=pt.getX();
		vals[1]=pt.getY();
		vals[2]=pt.getZ();
		vals[3]=1;
		Thetaquad=pt.getTquad();
	}
	
	public Vector3D(Vector3D pt1, Vector3D pt2){
		vals[0]=pt2.getX()-pt1.getX();
		vals[1]=pt2.getY()-pt1.getY();
		vals[2]=pt2.getZ()-pt1.getZ();
		vals[3]=1;
		findThetaquad();
	}
	
	public Vector3D(float[] v){
		vals[0]=v[0];
		vals[1]=v[1];
		vals[2]=v[2];
		vals[3]=1;
		findThetaquad();
	}
	
	public Vector3D(float mag, double theta, double Phi){
		vals[0]=(float) (mag*Math.sin(Phi)*Math.cos(theta));
		vals[1]=(float) (mag*Math.cos(Phi));
		vals[2]=(float) (mag*Math.sin(Phi)*Math.sin(theta));
		vals[3]=1;
		Round();
		findThetaquad();
		
	}

	/******************Accessors***************************/
	public float getX(){return vals[0];}
	public float getY(){return vals[1];}
	public float getZ(){return vals[2];}
	public int getTquad(){return Thetaquad;}
	public float[] getVals(){return vals;}
	
	//fix these
	public double getThetaDir(){return  Math.acos( (vals[0]/getMag())/Math.sin(getPhiDir()) ); }
	
	public double getPhiDir(){return Math.acos(vals[1]/getMag());}
	
	public float getMag(){return (float) Math.sqrt( (vals[0]*vals[0]) + (vals[1]*vals[1]) + (vals[2]*vals[2]) );}
	
	/**********************Mutators*******************************/
	public void chgX(float x){vals[0]=x;}
	public void chgY(float y){vals[1]=y;}
	public void chgZ(float z){vals[2]=z;}
	public void chgX(int x){vals[0]=x;}
	public void chgY(int y){vals[1]=y;}
	public void chgZ(int z){vals[2]=z;}
	public void chgX(double x){vals[0]=(float)x;}
	public void chgY(double y){vals[1]=(float)y;}
	public void chgZ(double z){vals[2]=(float)z;}
	public void multX(float s){vals[0]*=s;}	
	public void multX(int s){vals[0]*=s;}
	public void multX(double s){vals[0]*=s;}
	public void multY(float s){vals[1]*=s;}	
	public void multY(int s){vals[1]*=s;}
	public void multY(double s){vals[1]*=s;}
	public void multZ(float s){vals[2]*=s;}	
	public void multZ(int s){vals[2]*=s;}
	public void multZ(double s){vals[2]*=s;}
	public void addX(float s){vals[0]+=s;}	
	public void addX(int s){vals[0]+=s;}
	public void addX(double s){vals[0]+=s;}
	public void addY(float s){vals[1]+=s;}	
	public void addY(int s){vals[1]+=s;}
	public void addY(double s){vals[1]+=s;}
	public void addZ(float s){vals[2]+=s;}	
	public void addZ(int s){vals[2]+=s;}
	public void addZ(double s){vals[2]+=s;}
	public void  chgVec(Vector3D vec){
		vals[0]=vec.getX();
		vals[1]=vec.getY();
		vals[2]=vec.getZ();
		Thetaquad=vec.getTquad();
	}
	
	public void findThetaquad(){
		if( vals[0]==0 ){
			if(vals[2]>=0)
				Thetaquad=1;
			else
				Thetaquad=3;
		}
		else if( vals[0]>0 ){
			if(vals[2]>=0)
				Thetaquad=0;
			else
				Thetaquad=3;
		}
		else{
			if(vals[2]>0)
				Thetaquad=1;
			else 
				Thetaquad=2;

		}
	}//end findQuad

	
	
	public void Round(){
		
		if( Math.abs(((int)(vals[0]) - vals[0])) <= .001 )
			vals[0]= (int)(vals[0]);
		if( Math.abs(((int)(vals[1]) - vals[1])) <= .001 )
			vals[1]= (int)(vals[1]);
		if( Math.abs(((int)(vals[2]) - vals[2])) <= .001 )
			vals[2]= (int)(vals[2]);

	}
	
	
	public String toString(){
		return "Mag: "+getMag()+"  <"+vals[0]+" , "+vals[1]+" , "+vals[2]+"> Tdir: "+getThetaDir()+"  Pdir: "+getPhiDir()+"  XZQuadrant: "+(Thetaquad+1);
	}
	
	public boolean equals(Vector3D other){
		return (vals[0] == other.getX()) && (vals[1] == other.getY()) && (vals[2] == other.getZ());
	}
	
}//end Vector3D

