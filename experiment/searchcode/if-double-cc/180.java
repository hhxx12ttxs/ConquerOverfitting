import java.util.*;
import javax.media.opengl.GL;
import com.sun.opengl.util.GLUT;

public class Sphere extends RenderObject
{
  private double x, y, z, radius; // center and radius of sphere
  private GLUT glut;
  Matrix m = new Matrix();
  double[][] Qtilde;

  public Sphere( StringTokenizer stok, int id_ )
  {
    glut = new GLUT();

    id = id_;

    stok.nextToken(); // strip away the token "mat"
    stok.nextToken(); // strip away the token "ID"
    	matId = Integer.parseInt( stok.nextToken() );

    x  = Double.parseDouble( stok.nextToken() );
    y  = Double.parseDouble( stok.nextToken() );
    z  = Double.parseDouble( stok.nextToken() );
    radius  = Double.parseDouble( stok.nextToken() );
    Qtilde = Qtilde();
  }

  public double[][] getQ(){
	  return Qtilde;
  }
  public Coord getOrigin(){
	  return new Coord(x, y, z);
  }
  
  public double getRadius(){
  	return radius;
  }
  
  public void render( GL gl, MaterialCollection materials )
  {
    gl.glPushMatrix();
    gl.glTranslated(x,y,z);
    materials.setMaterial(matId, gl);
    glut.glutSolidSphere(radius,40,40);
	gl.glPopMatrix();
  }
  
  public Coord getNormalAt(Coord pt){
	 Coord norm = pt.subtract(pt, getOrigin());
	 norm.normalize();
	 return norm;
  }
  
  public Depth getIntersect(double[][] u, double[][]p){
	 
	  
	  double[][] ut = m.tMatrix(u);
	  double[][] pt = m.tMatrix(p);
	  
	  double[][] q = Qtilde;
	  
//	  for (int i = 0; i < ut.length; i++){
//		  for (int j = 0; j < ut[0].length; j++){
//			  System.out.print(q[i][j]+ " ");
//		  }
//		  System.out.println();
//	  }
	  
	  double[][] aa = m.multiplyMatrix(ut, q);
	  
	  aa = m.multiplyMatrix(aa, u);
	  System.out.println(aa[0][0]);
	  
	  double[][] bb = m.multiplyMatrix(ut, q);
	  bb = m.multiplyMatrix(bb, p);
	  bb[0][0] *= 2;
	  
	  double[][] cc = m.multiplyMatrix(pt, q);
	  cc = m.multiplyMatrix(cc, p);
	  
	  double a = aa[0][0];
	  double b = bb[0][0];
	  double c = cc[0][0];
	  //System.out.println(a + " " + b + " " + c);
	  
	  double existance = (b*b) - (4*a*c);
	  
	  //System.out.println(existance);
	  if (existance < 0){
		  return new Depth(-1, id);
	  }
	  else{
		  double t1 = ((-1 * b) + Math.sqrt(existance))/ (2*a);
		  double t2 = ((-1 * b) - Math.sqrt(existance))/ (2*a);
		  //System.out.println(t1+" " +t2);
		  if(t1 < 0 && t2 < 0){
			  return new Depth(-1,id);
		  }
		  else if (t1 < 0){
			  return new Depth(t2, id);
		  }
		  else if (t2 < 0){
			  return new Depth(t1, id);
		  }
		  else{
			 double t0 = Math.min(t1, t2);
			 return new Depth(t0, id);
		  }
		}
		  
	 }
	  
  
  
  
  
  
  public double[][] Qtilde(){
	  print();
	  
	  double[][] q = m.initMatrix(4, 4);
	  q[0][0] = (1/ (radius * radius));
	  q[1][1] = (1/ (radius * radius));
	  q[2][2] = (1/ (radius * radius));
	  q[3][3] = -1;
	  
	  
	  double[][] t = m.initMatrix(4, 4);
	  
	  t[0][0]= 1;
	  t[1][1] = 1;
	  t[2][2] = 1;
	  t[0][3] = (this.x) * -1;
	  t[1][3] = (this.y) * -1;
	  t[2][3] = (this.z) * -1;
	  t[3][3] = 1;
	  
	  double[][] tt = m.tMatrix(t);
	  
	  double[][] result = m.multiplyMatrix(tt, q);
	  result = m.multiplyMatrix(result, t);
	  for (int i = 0; i < result.length; i++){
		  for (int j = 0; j < result[0].length; j++){
			  System.out.print(result[i][j]+ " ");
		  }
		  System.out.println();
	  }
	  
	  return result;
  }

  public void print()
  {
    System.out.print( "Sphere ID:" + id + " mat ID:" + matId );
    System.out.print( " x:" + x + " y:" + y + " z:" + z);
    System.out.print( " radius:" + radius );
    System.out.println();

  }
}


