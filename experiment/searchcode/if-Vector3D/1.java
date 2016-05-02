package bubble;

import graphics.Triangle;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import com.sun.xml.internal.bind.v2.util.EditDistance;


import javax.media.opengl.GL;

import util.Pair;
import util.Vector3D;
//http://dspace.xmu.edu.cn/dspace/bitstream/2288/6469/1/Loop%E7%BB%86%E5%88%86%E6%9B%B2%E9%9D%A2%E7%B2%BE%E7%A1%AE%E6%B1%82%E5%80%BC%E6%96%B0%E5%85%AC%E5%BC%8F.pdf
public class SubdivisionSurface {//Loop
   private List<Vector3D> verticles;
   private List<SubTriangle> triangles;
   
   private static final double X = 0.5257311;
   private static final double Z = 0.8506508;
   
   public SubdivisionSurface(){}
   
   public SubdivisionSurface(int radius, int divisionDepth){//
	   InitRegPoly20(radius);
	   for(int i = 0; i < divisionDepth; i++){
		   LoopDivide();
	   }
   }
   
   private void InitRegPoly20(int radius){
	   double x = radius * X;
	   double z = radius * Z;
	   Vector3D[] vec = {new Vector3D(-x, 0, z), new Vector3D(x, 0, z), new Vector3D(-x, 0, -z), new Vector3D(x,0,-z),
			   new Vector3D(0,z,x), new Vector3D(0,z,-x), new Vector3D(0,-z,x), new Vector3D(0,-z,-x),
			   new Vector3D(z,x,0), new Vector3D(-z,x,0), new Vector3D(z,-x,0), new Vector3D(-z,-x,0)};
	   verticles = Arrays.asList(vec);
	   
	   SubTriangle[] tri = {new SubTriangle(1,4,0), new SubTriangle(4,9,0), new SubTriangle(4,5,9),new SubTriangle(8,5,4), new SubTriangle(1,8,4),
			   new SubTriangle(1,10,8), new SubTriangle(10,3,8), new SubTriangle(8,3,5), new SubTriangle(3,2,5), new SubTriangle(3,7,2),
			   new SubTriangle(3,10,7), new SubTriangle(10,6,7), new SubTriangle(6,11,7), new SubTriangle(6,0,11), new SubTriangle(6,1,0),
			   new SubTriangle(10,1,6), new SubTriangle(11,0,9), new SubTriangle(2,11,9), new SubTriangle(5,2,9), new SubTriangle(11,2,7)};
	   triangles = Arrays.asList(tri);
   }
   
   private void LoopDivide(){
	   
	   //边点
	   List<Vector3D> newVerticles = new ArrayList<Vector3D>();
	   newVerticles.addAll(verticles);
	   Map<Pair<Integer, Integer>, Integer> midPtMap = new HashMap<Pair<Integer, Integer>, Integer>();
	   
	   for(SubTriangle triangle: triangles){
		   for(int i = 0; i < 3; i++){
			   int pt1 = triangle.index[i];
			   int pt2 = triangle.index[(i+1)%3];
			   if(midPtMap.get(new Pair<Integer, Integer>(pt1, pt2)) != null) continue;
			   SubTriangle[] triWithBorder = getTriangleByBorder(pt1, pt2);
			   Integer[] diagonalPts = new Integer[2];
			   diagonalPts[0] = triWithBorder[0].getOtherPt(pt1, pt2);
			   diagonalPts[1] = triWithBorder[1].getOtherPt(pt1, pt2);
			   Vector3D[] pts = {verticles.get(pt1), verticles.get(pt2), verticles.get(diagonalPts[0]), verticles.get(diagonalPts[1])};
			   double[] coes = {3.0/8, 3.0/8, 1.0/8, 1.0/8};
			   Vector3D newMidPt = Vector3D.LinearSum(pts, coes);
			   newVerticles.add(newMidPt);
			   midPtMap.put(new Pair<Integer, Integer>(pt1, pt2), newVerticles.size()-1);
			   midPtMap.put(new Pair<Integer, Integer>(pt2, pt1), newVerticles.size()-1);
		   }
	   }
	   
	   //顶点
	   for(SubTriangle triangle:triangles){
		   for(int i = 0; i < 3; i++){
			   int index = triangle.index[i];
			   if(newVerticles.get(index).equals(verticles.get(index)) == false) continue; //already updated
			   Set<Integer> neighbourPts = new HashSet<Integer>();
			   for(SubTriangle tri:triangles){
				   if(tri.hasPt(index) == true){
					   neighbourPts.add(tri.index[0]);
					   neighbourPts.add(tri.index[1]);
					   neighbourPts.add(tri.index[2]);
				   }
			   }
			   neighbourPts.remove(index);
			   Integer[] neighbourIndex = neighbourPts.toArray(new Integer[0]);
			   int size = neighbourPts.size();
			   Vector3D[] pts = new Vector3D[size+1];
			   double[] coes = new double[size+1];
			   double beta = (5.0/8 - Math.pow(3.0/8 - 0.25*Math.cos(2*3.1415/size), 2)) / size;
			   for(int j = 0; j < size; j++){
				   pts[j] = verticles.get(neighbourIndex[j]);
				   coes[j] = beta;
			   }
			   pts[size] = verticles.get(index);
			   coes[size] = 1 - size*beta;
			   Vector3D newPt = Vector3D.LinearSum(pts, coes);
			   newVerticles.set(index, newPt);
		   }
	   }
	   
	   //连接三角形
	   List<SubTriangle> newTriangles = new ArrayList<SubTriangle>();
	   for(SubTriangle triangle:triangles){
		   for(int i = 0; i < 3; i++){
			   int pt1 = triangle.index[i];
			   int pt2 = triangle.index[(i+1)%3];
			   int pt3 = triangle.index[(i+2)%3];
			   newTriangles.add(new SubTriangle(pt1, 
					   midPtMap.get(new Pair<Integer, Integer>(pt1, pt2)), 
					   midPtMap.get(new Pair<Integer, Integer>(pt1, pt3))));
			   
		   }
		   int pt1 = triangle.index[0];
		   int pt2 = triangle.index[1];
		   int pt3 = triangle.index[2];
		   newTriangles.add(new SubTriangle(midPtMap.get(new Pair<Integer, Integer>(pt1, pt2)),
				   midPtMap.get(new Pair<Integer, Integer>(pt2, pt3)), 
				   midPtMap.get(new Pair<Integer, Integer>(pt3, pt1))));
	   }
	   
	   verticles = newVerticles;
	   triangles = newTriangles;
   }
   
   private SubTriangle[] getTriangleByBorder(int pt1, int pt2){
	   SubTriangle[] neighbour = new SubTriangle[2];
	   int index = 0;
	   for(SubTriangle triangle : triangles){
		   if(triangle.hasPt(pt1) == true && triangle.hasPt(pt2) == true){
			   neighbour[index] = triangle;
			   index++;
			   if(index == 2) break;
		   }
	   }
	   return neighbour;
   }
   
   
   public List<Triangle> exportToTraigles(){
	   Vector3D central = getCentralPoint();
	   List<Triangle> exportTriangles = new ArrayList<Triangle>();
	   for(SubTriangle triangle : triangles){
		   //TODO:
		   Triangle newTriangle = new Triangle(verticles.get(triangle.index[0]), verticles.get(triangle.index[1]), verticles.get(triangle.index[2]));
		   //compute normal
		   Vector3D ab = Vector3D.Substract(newTriangle.getVerticles()[0], newTriangle.getVerticles()[1]);
		   Vector3D bc = Vector3D.Substract(newTriangle.getVerticles()[1], newTriangle.getVerticles()[2]);
		   Vector3D normal = Vector3D.CrossProduct(ab, bc);
		   Vector3D relaA = Vector3D.Substract(newTriangle.getVerticles()[0], central);
		   if(Vector3D.DotProduct(normal, relaA) > 0){
			   newTriangle.setNormal(normal);
		   }else{
			   newTriangle.setNormal(Vector3D.Multiply(normal, -1));
		   }
		   exportTriangles.add(newTriangle);
	   }
	   return exportTriangles;
   }
   
   public Vector3D getCentralPoint(){
	   Vector3D central = new Vector3D();
	   for(Vector3D vertical : verticles){
		   central.x += vertical.x;
		   central.y += vertical.y;
		   central.z += vertical.z;
	   }
	   return Vector3D.Multiply(central, 1.0 / verticles.size());
   }
   
   public double getRadius(){
	   Vector3D central = getCentralPoint();
	   Vector3D relaA = Vector3D.Substract(verticles.get(0), central);
	   return relaA.Length();
   }
   
   public void Draw(GL gl){
	   gl.glBegin(GL.GL_TRIANGLES);
	   /*color info here*/
	   //gl.glColor3f(0.9f, 0.5f, 0.2f);
	   gl.glColor3f(0.0f, 0.0f, 0.0f);
	   for(SubTriangle triangle : triangles){
		   gl.glNormal3d(verticles.get(triangle.index[0]).x, verticles.get(triangle.index[0]).y, verticles.get(triangle.index[0]).z);
		   gl.glVertex3d(verticles.get(triangle.index[0]).x, verticles.get(triangle.index[0]).y, verticles.get(triangle.index[0]).z);
		   gl.glNormal3d(verticles.get(triangle.index[1]).x, verticles.get(triangle.index[1]).y, verticles.get(triangle.index[1]).z);
		   gl.glVertex3d(verticles.get(triangle.index[1]).x, verticles.get(triangle.index[1]).y, verticles.get(triangle.index[1]).z);
		   gl.glNormal3d(verticles.get(triangle.index[2]).x, verticles.get(triangle.index[2]).y, verticles.get(triangle.index[2]).z);
		   gl.glVertex3d(verticles.get(triangle.index[2]).x, verticles.get(triangle.index[2]).y, verticles.get(triangle.index[2]).z);
		   
	   }
	   gl.glEnd();
   }
   
   private class SubTriangle{
	   public int[] index = new int[3];//verticle index
	   
	   public SubTriangle(){}
	   
	   public SubTriangle(int x, int y, int z){//index
		   index[0] = x;
		   index[1] = y;
		   index[2] = z;
	   }
	   
	   public boolean hasPt(int ptIndex){
		   if(index[0] == ptIndex || index[1] == ptIndex || index[2] == ptIndex){
			   return true;
		   }else{
			   return false;
		   }
	   }
	   
	   public Integer getOtherPt(int pt1, int pt2){
		   for(int i = 0; i < 3; i++){
			   if(index[i] != pt1 && index[i] != pt2)
				   return index[i];
		   }
		   return -1;
	   }
   }
   
   
}

	 
 
 

