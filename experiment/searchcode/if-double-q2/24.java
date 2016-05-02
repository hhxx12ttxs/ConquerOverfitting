package org.gvsig.jts.voronoi.dt;
 
/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2007, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *    
 *    @author      Josef Bezdek
 *	  @version     %I%, %G%
 *    @since JDK1.3 
 */


import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;


import java.util.Iterator;
import java.util.LinkedList;

import org.gvsig.spatialindex.b2dtree.KDTree;


public class IncrementalDT{
		
 
		private class TestingTriangles{
			TriangleDT trian;
			TestingTriangles next; 
			
			
			TestingTriangles (){
			}
			
			TestingTriangles (TriangleDT A){
				this.trian=A;
			}
		}
		
		
		private LinearRing newConvexGeometry;							//body konvexniho obalu
		private boolean duplicitePoint;									//true - kdyz nove zadany bod je jiy obsazen v triangulaci
		private Coordinate [] convex;									//body convexniho obalu + body lezici na hrane konvexu
		public KDTree triangles=new KDTree(2);							//vyvazeny dvourozmerny AVL strom, datova struktura trojuhelniku 
		private int number_Points=0;									//pocet bodu vlozenych do triangulace
		public int number_Triangles=0;									//pocet vytvorenych trojuhelniku
		private LinkedList firstPointsXY=new LinkedList();		//souradnice bodu prvniho trojuhelnika
		private boolean firstTriangle = false; 
		public int max=0;												//pocet kroku pro nalezeni vnitrniho trojuhelnika, ktery obsahuje novy bod P
		public int maxK=0;												//pocet kroku pro nalezeni trojuhelnika na kraji konvex obalu, ktery obsahuje novy bod P
	
	
		
		
		/***
		   * The method for imputing a new points to triangulation
		   *
		   * @param Coordinate of point
		   *
	       */		
		
		public void insertPoint(Coordinate XY){ 
			PointDT P=new PointDT(XY);
			duplicitePoint=false;
				number_Points++;
				if (!firstTriangle){								//vstup prvnich bodu, az mohou vytvorit trojuhelnik
					Iterator it = firstPointsXY.iterator();
					while (it.hasNext()) 			//kontrola duplicity
						if (P.compare((PointDT)it.next()))
							duplicitePoint=true;
					if (!duplicitePoint)
						firstPointsXY.addFirst(P);
					else
						number_Points--;
						
					if (number_Points>=3){
						createFirstTriangle();	//vytvoreni prvniho trojuhelnika
					}	
				}
				else{												//vstup pro body do jiz existujici triangulace
					Coordinate[] newPoint={P}; 
					CoordinateArraySequence newPoints=new CoordinateArraySequence(newPoint);
					Point newP=new Point(newPoints,new GeometryFactory());
					
					if (newP.coveredBy(newConvexGeometry.convexHull())){			//vnitrni bod
						divideTriangle(P,newConvexGeometry.contains(newP));
						if (newConvexGeometry.contains(newP)&&!duplicitePoint)		//bod na hrane konvex obalu
							convex=addPointToConvexGeometry(newP,convex);
					}	
					else
						createConvexPolygon(P);										//bod vne konvex obalu
				}
		}
		
		/***
		   * The method for creating a first triangle
		   *
		   * @param Coordinate[] array of first three point's coordinate
		   *
	     */		

		
		private void createFirstTriangle(){
			Iterator it = firstPointsXY.iterator();
			TriangleDT T=new TriangleDT((PointDT)it.next(),
										(PointDT)it.next(),
										(PointDT)it.next());
			
			if (T.isTriangle()){
				try{ 										//vstup do datove struktury
					triangles.insertToTree(T, T.key);
				}
				catch (Exception e){ 
			    	System.err.println(e);
				}
				number_Triangles++;
				convex=new Coordinate[4];
				convex[0]=T.A; 
				convex[1]=T.B;
				convex[2]=T.C;
				convex[3]=T.A;
															//vytvoreni geometrie konvex obalu
				CoordinateArraySequence pointsWhichGenerateConvex=new CoordinateArraySequence(convex); 
				newConvexGeometry=new LinearRing(pointsWhichGenerateConvex,new GeometryFactory());
				firstTriangle = true;
			
				if (firstPointsXY.size()>3){ // doplneni triangulace o prvni body, ktere byly v primce
					it = firstPointsXY.iterator();
					it.next();it.next();it.next();
					
					while(it.hasNext()){
						insertPoint((PointDT)it.next());
					}
				}
			}
		}


		
				
		
		
		/***
		   * The method for searching a tringle which contains new point P, inside convex hull
		   *
		   * @param PointDT - coordinate of new point
		   *
		   * @return TriangleDT - founded triangle which contains new point P
		   *    
	     */		

	
		
		private TriangleDT ringSearch(PointDT P){
	
			LinkedList tested = new LinkedList();
			double []A = {P.x,P.y};
			TriangleDT T = null;
			try{
				T = (TriangleDT)triangles.searchNearest(A);
			}
			catch(Exception e){}
			TriangleDT TT;
			int i;
			
			if (T.contains(P)){
				return T;
			}	
			tested.add(T);
			for (i=0; i<3; i++){
				if (T.neighbour[i]!=null){
					if (T.neighbour[i].contains(P))
						return T.neighbour[i];
					tested.add(T.neighbour[i]);
					TriangleDT left=null;
					TriangleDT right= null;
					
					for (int j=0; j<3; j++){
						if (T.neighbour[i].neighbour[j]!=null&&T.neighbour[i].neighbour[j]!=T){
							if (left == null){
								left = T.neighbour[i].neighbour[j];
								if (left.contains(P))
									return left;
								tested.add(left);
							}	
							else{
								right = T.neighbour[i].neighbour[j];
								if (right.contains(P))
									return right;
								tested.add(right);
							}
						}
					}
					if (left!=null){
						TT = inSideSearch(T,i,left,P,tested);
						if (TT !=null)
							return TT;
					}
					if (right!=null){
						TT = inSideSearch(T,i,right,P,tested);
						if (TT !=null)
							return TT;
					}
				}
			
			}
			//System.out.println("jsem na konceINSIDE");
			tested = triangles.getLinkedList();
			Iterator iter = tested.iterator();
			while (iter.hasNext()){
				T = (TriangleDT)iter.next();
				if (T.contains(P))
					return T;
			}
			return null;				
			
		}


		/***
		   * The private method for searching around founded tringle
		   *
		   * @param TriangleDT T - founded triangle
		   * @param int i - index in array of neighbour
		   * @param TriangleDT left - neighbour of triangle T
		   * @param PointDT P - new point
		   * @param LinkedList tested - list of tested triangle
		   * 
		   * @return TriangleDT - founded triangle which contains new point P
		   *    
	     */		
			
				
		private TriangleDT inSideSearch(TriangleDT T, int i, TriangleDT left, PointDT P, LinkedList tested){
					
			TriangleDT helpTriangle = new TriangleDT();
			helpTriangle = T.neighbour[i];
			boolean change = true;
			while (change){
				change = false;
				for (int j=0; j<3; j++){
					if ((left.neighbour[j]!=null)&&left.neighbour[j].contains(P))
						return left.neighbour[j];
									
					if (left.neighbour[j]!=null&&
							!wasTested(left.neighbour[j], tested)&&
							left.neighbour[j]!=helpTriangle&&
							left.neighbour[j].containsOneSamePointWith(T)){
						
						tested.add(left.neighbour[j]);
						helpTriangle = left;
						left = left.neighbour[j];
						change = true;
					}
				}
			}
			return null;
		}
		
		/***
		   * The private method for control, if the triangle was tested
		   *
		   * @param TriangleDT T - triangle for testing
		   * @param LinkedList tested - list of tested triangle
		   * 
		   * @return boolean true - the triangle was tested
		   *                 false - the triangle wasn't tested 
 		   */		
				
		private boolean wasTested(TriangleDT T, LinkedList tested){
			Iterator iter = tested.iterator();
			while (iter.hasNext()){
				if ((TriangleDT)iter.next() == T)
					return true;
			}
			return false;
		}
				
				
				
		/***
		   * The method for searching a trinagle contains new point, which isn't inside convex hull
		   *
		   * @param PointDT A - first point on edge of convex hull
		   *		PointDT B - second point on edge of convex hull
		   * 
		   * @return TriangleDT - which contains PointDT A and PointDT B
		   */		

		private TriangleDT ringSearchConvexTriangle(PointDT A, PointDT B){
			PointDT P =new PointDT((A.x + B.x)/2, (A.y + B.y)/2, 0);
			double []X = {P.x,P.y};
			LinkedList tested = new LinkedList();
				
			TriangleDT T = null;
			try{
				T =(TriangleDT)triangles.searchNearest(X);
			}
			catch(Exception e){}
			TriangleDT TT;
			int i;
							
			if (T.containsTwoPoints(A, B))
				return T;
			tested.add(T);
			for (i=0; i<3; i++){
			if (T.neighbour[i]!=null){
				if (T.neighbour[i].containsTwoPoints(A, B))
					return T.neighbour[i];
					tested.add(T.neighbour[i]);
					TriangleDT left=null;
					TriangleDT right= null;
							
					for (int j=0; j<3; j++){
						if (T.neighbour[i].neighbour[j]!=null&&T.neighbour[i].neighbour[j]!=T){
							if (left == null){
								left = T.neighbour[i].neighbour[j];
								if (left.containsTwoPoints(A, B))
									return left;
								tested.add(left);
							}	
							else{
								right = T.neighbour[i].neighbour[j];
								if (right.containsTwoPoints(A, B))
									return right;
								tested.add(right);
							}
						}
					}
					if (left!=null){
						TT = inSideConvexSearch(T,i,left,A,B,tested);
						if (TT !=null)
							return TT;
					}
					if (right!=null){
						TT = inSideConvexSearch(T,i,right,A,B,tested);
						if (TT !=null)
							return TT;
					}
				}
							
			}
			//System.out.println("jsem na konce");
			tested = triangles.getLinkedList();
			Iterator iter = tested.iterator();
			while (iter.hasNext()){
				T = (TriangleDT)iter.next();
				if (T.containsTwoPoints(A, B))
					return T;
			}
			return null;				
						
	}
				

	/***
	   * The private method for searching around founded triangle
	   *
	   * @param TriangleDT T - founded triangle
	   * @param int i - index in array of neighbour
	   * @param TriangleDT left - neighbour of triangle T
	   * @param PointDT A - the point, which must be vertex
	   * @param PointDT B - the point, which must be vertex
	   * @param LinkedList tested - list of tested triangle
	   * 
	   * @return TriangleDT - founded triangle which contains new point P
	   *    
       */		
				
				
			
	private TriangleDT inSideConvexSearch(TriangleDT T, int i, TriangleDT left, PointDT A, PointDT B, LinkedList tested){
		TriangleDT helpTriangle = new TriangleDT();
		helpTriangle = T.neighbour[i];
		boolean change = true;
		while (change){
			change = false;
			for (int j=0; j<3; j++){
				if ((left.neighbour[j]!=null)&&left.neighbour[j].containsTwoPoints(A, B))
					return left.neighbour[j];
				if (left.neighbour[j]!=null&&
						!wasTested(left.neighbour[j], tested)&&
						left.neighbour[j]!=helpTriangle&&
						left.neighbour[j].containsOneSamePointWith(T)){
						
					tested.add(left.neighbour[j]);
					helpTriangle = left;
					left = left.neighbour[j];
					change = true;
				}
			}
		}
		return null;
	}
						
	
	
	/***
	   * The method for dividing a trinagle which contains new point
	   *
	   * @param PointDT A - new point
	   *		boolean pointIsInConvex - true - if point is on edge of the TriangleDT
	   * 								- false - if point is inside the TriangleDT
	   */		
		
	private void divideTriangle(PointDT A, boolean pointIsInConvex){
		TriangleDT T = ringSearch(A);
		duplicitePoint=T.containsPointAsVertex(A);
		if (!duplicitePoint){																	//test duplicity
			TestingTriangles newTriangles=new TestingTriangles();
		
			newTriangles.trian=new TriangleDT(T.A,T.B,A);										//vytvoreni tri novych troj.
			newTriangles.next=new TestingTriangles();
			newTriangles.next.trian=new TriangleDT(T.B,T.C,A);
			newTriangles.next.next=new TestingTriangles();
			newTriangles.next.next.trian=new TriangleDT(T.A,A,T.C);
			
			if (!pointIsInConvex){																// pro body uvnitr trojuhelnika
				saveTriangleNeighbour(newTriangles.trian,newTriangles.next.trian);
				saveTriangleNeighbour(newTriangles.next.trian,newTriangles.next.next.trian);	// ulozeni sousedu mezi novymi troj.
				saveTriangleNeighbour(newTriangles.trian,newTriangles.next.next.trian);
			
				if (T.neighbour[0]!=null)														// ulozeni sousedu z byvaleho troj.
					saveTriangleNeighbour(T.neighbour[0],newTriangles.trian);
			
				if (T.neighbour[1]!=null)
				saveTriangleNeighbour(T.neighbour[1],newTriangles.next.trian);
			
				if (T.neighbour[2]!=null)
					saveTriangleNeighbour(T.neighbour[2],newTriangles.next.next.trian);
				try {																			//vstup do dat. struktury
					triangles.delete(T.key);
					triangles.insertToTree(newTriangles.trian, newTriangles.trian.key);
					triangles.insertToTree(newTriangles.next.trian, newTriangles.next.trian.key);
					triangles.insertToTree(newTriangles.next.next.trian, newTriangles.next.next.trian.key);
				}
				catch (Exception e) {
				}
				
				
				number_Triangles+=2;
				testNewTriangles(newTriangles,newTriangles.next.next);
			}
			else{																				//vstup pro body lezici na hrane troj.
				if (!newTriangles.trian.isTriangle())
					newTriangles=newTriangles.next;
				if (!newTriangles.next.trian.isTriangle())
					newTriangles.next=newTriangles.next.next;
				saveTriangleNeighbour(newTriangles.trian,newTriangles.next.trian);
				for (int i=0;i<3;i++)
					if (T.neighbour[i]!=null){
						saveTriangleNeighbour(T.neighbour[i],newTriangles.next.trian);
						saveTriangleNeighbour(T.neighbour[i],newTriangles.trian);
					}
					
				try {
					triangles.delete(T.key);
					triangles.insertToTree(newTriangles.trian, newTriangles.trian.key);
					triangles.insertToTree(newTriangles.next.trian, newTriangles.next.trian.key);
				}
				catch (Exception e) {
				}
				number_Triangles++;
				testNewTriangles(newTriangles,newTriangles.next);
			}
		}
	}
	
	/***
	   * The method for saving triangle's neighbours between two triangles
	   *
	   * @param TriangleDT T - first triangle
	   * @param	TriangleDT TT - second triangle
	   */		
		
	private void saveTriangleNeighbour(TriangleDT T, TriangleDT TT){
		if ((T.A.compare(TT.A)&&T.B.compare(TT.B))||(T.A.compare(TT.B)&&T.B.compare(TT.A))){
			T.neighbour[0]=TT;
			TT.neighbour[0]=T;
		}
		else
			if ((T.A.compare(TT.A)&&T.B.compare(TT.C))||(T.A.compare(TT.C)&&T.B.compare(TT.A))){
				T.neighbour[0]=TT;
				TT.neighbour[2]=T;
			}
			else
				if ((T.A.compare(TT.B)&&T.B.compare(TT.C))||(T.A.compare(TT.C)&&T.B.compare(TT.B))){
					T.neighbour[0]=TT;
					TT.neighbour[1]=T;
				}
				else
					if ((T.A.compare(TT.A)&&T.C.compare(TT.B))||(T.A.compare(TT.B)&&T.C.compare(TT.A))){
						T.neighbour[2]=TT;
						TT.neighbour[0]=T;
					}
					else
						if ((T.A.compare(TT.A)&&T.C.compare(TT.C))||(T.A.compare(TT.C)&&T.C.compare(TT.A))){
							T.neighbour[2]=TT;
							TT.neighbour[2]=T;
						}
						else
							if ((T.A.compare(TT.B)&&T.C.compare(TT.C))||(T.A.compare(TT.C)&&T.C.compare(TT.B))){
								T.neighbour[2]=TT;
								TT.neighbour[1]=T;
							}
							else
								if ((T.B.compare(TT.A)&&T.C.compare(TT.B))||(T.B.compare(TT.B)&&T.C.compare(TT.A))){
									T.neighbour[1]=TT;
									TT.neighbour[0]=T;
								}
								else
									if ((T.B.compare(TT.A)&&T.C.compare(TT.C))||(T.B.compare(TT.C)&&T.C.compare(TT.A))){
										T.neighbour[1]=TT;
										TT.neighbour[2]=T;
									}
									else
										if ((T.B.compare(TT.B)&&T.C.compare(TT.C))||(T.B.compare(TT.C)&&T.C.compare(TT.B))){
											T.neighbour[1]=TT;
											TT.neighbour[1]=T;
										}
		
	}

	/***
	   * The method for testing new inserting triangles into triangulation
	   *
	   * @param TestingTriangles - private class of the incrementalDT, is it AbstractDateType "spojovy seznam TriangleDT"
	   * 						   newTriangles - is pointer which pointed on start of ADT
	   * 						   endOfListNewTriangles - is pointer which pointed on end of ADT		
	   */		
	
	private void testNewTriangles(TestingTriangles newTriangles,TestingTriangles endOfListNewTriangles){
		PointDT freePointA,freePointB;
		
		while (newTriangles!=null){													//prochazi vsechny nove trojuhelniky
			for (int i=0;i<3;i++){	// delaunay test vsech tri sousedu trojuhelnika
				if (newTriangles.trian.neighbour[i]!=null){
					freePointB=searchFreePointOfTwoTriangles(newTriangles.trian,newTriangles.trian.neighbour[i]);
					freePointA=searchFreePointOfTwoTriangles(newTriangles.trian.neighbour[i],newTriangles.trian);
					if (delaunay(newTriangles.trian.neighbour[i],freePointB)){		//vznik novych trojuhelniku, kdyz delaunay test neprosel
						
						endOfListNewTriangles.next=new TestingTriangles();
						endOfListNewTriangles=endOfListNewTriangles.next;
						endOfListNewTriangles.next=new TestingTriangles();
						if (i==0){													//nove dva trojuhelniky
							endOfListNewTriangles.trian=new TriangleDT(newTriangles.trian.A,newTriangles.trian.C,freePointA);
							endOfListNewTriangles.next.trian=new TriangleDT(newTriangles.trian.B,newTriangles.trian.C,freePointA);
						}
						else{
							if (i==1){
								endOfListNewTriangles.trian=new TriangleDT(newTriangles.trian.A,newTriangles.trian.B,freePointA);
								endOfListNewTriangles.next.trian=new TriangleDT(newTriangles.trian.A,newTriangles.trian.C,freePointA);
							}			
							else{
								endOfListNewTriangles.trian=new TriangleDT(newTriangles.trian.B,newTriangles.trian.A,freePointA);
								endOfListNewTriangles.next.trian=new TriangleDT(newTriangles.trian.B,newTriangles.trian.C,freePointA);
							}		
						}						
						for (int j=0;j<3;j++){				// ulozeni sousedu nove vyniklych trojuhelniku
							if ((newTriangles.trian.neighbour[j]!=null)&&(newTriangles.trian.neighbour[j]!=newTriangles.trian.neighbour[i])){
								saveTriangleNeighbour(endOfListNewTriangles.next.trian,newTriangles.trian.neighbour[j]);
								saveTriangleNeighbour(endOfListNewTriangles.trian,newTriangles.trian.neighbour[j]);
							}
						}
						for (int j=0;j<3;j++){
							if ((newTriangles.trian.neighbour[i].neighbour[j]!=null)&&
								(newTriangles.trian.neighbour[i].neighbour[j]!=newTriangles.trian)){
								saveTriangleNeighbour(endOfListNewTriangles.next.trian,newTriangles.trian.neighbour[i].neighbour[j]);
								saveTriangleNeighbour(endOfListNewTriangles.trian,newTriangles.trian.neighbour[i].neighbour[j]);
							}
						}
						saveTriangleNeighbour(endOfListNewTriangles.trian,endOfListNewTriangles.next.trian);
						
						try{				//vstup do datove struktury
							triangles.delete(newTriangles.trian.key);
							triangles.delete(newTriangles.trian.neighbour[i].key);
							triangles.insertToTree(endOfListNewTriangles.trian,endOfListNewTriangles.trian.key);
							triangles.insertToTree(endOfListNewTriangles.next.trian,endOfListNewTriangles.next.trian.key);
						}
						catch (Exception e) {
						}
						endOfListNewTriangles=endOfListNewTriangles.next;
						i=3;
							
					}
				}
			}	
			newTriangles=newTriangles.next;
		}
	}
	
	
	/***
	   * The method for searching a free point from two triangles, which have two same points
	   *
	   * @param TriangleDT - the first triangle
	   * 		TriangleDT - the second triangle
	   */
	
	private PointDT searchFreePointOfTwoTriangles(TriangleDT firstTriangle,TriangleDT secondTriangle){
		if(firstTriangle.neighbour[0]==secondTriangle)//najde volny bod, dvou sousedicich trojuhelniku
			return firstTriangle.C;
		if (firstTriangle.neighbour[1]==secondTriangle)
			return firstTriangle.A;
		else
			return firstTriangle.B;
	}
		
		
	/***
	   * The method for calculation delaunay's test
	   *
	   * @param TriangleDT T - testing triangle
	   *		PointDT A - free point point
	   * 
	   * @return boolean - true - if the new point A is inside circle of triangles T
	   */		


	private boolean delaunay(TriangleDT T, PointDT A){
		double N=((Math.pow(T.C.x,2)-Math.pow(T.A.x,2)+Math.pow(T.C.y,2)-Math.pow(T.A.y,2))*(T.B.x-T.A.x)-(Math.pow(T.B.x,2)-Math.pow(T.A.x,2)+Math.pow(T.B.y,2)-Math.pow(T.A.y,2))*(T.C.x-T.A.x))/
		 (-2*((T.A.y-T.C.y)*(T.B.x-T.A.x)-(T.A.y-T.B.y)*(T.C.x-T.A.x)));
		double M=(Math.pow(T.B.x,2)-Math.pow(T.A.x,2)-Math.pow((T.A.y-N),2)+Math.pow((T.B.y-N),2))/(2*(T.B.x-T.A.x));
		double R=Math.sqrt(Math.pow((T.A.x-M),2)+Math.pow((T.A.y-N),2)); 			//vypocet polomeru
		if ((R-0.0001)>(Math.sqrt(Math.pow((A.x-M),2)+Math.pow((A.y-N),2))))		//tolerance pro trojuhelniku, ktere jsou soumerne podle osy, kterou prochazi spolecna hrana
			return true;
		else{
			if (R>0){
				return false;
			}
			else{
				return delaunay(new TriangleDT(T.B,T.C,T.A), A);
			}	
		}
	}

	
	/***
	   * The method for imputing new point into tiangulation. The point isn't inside convex hull
	   *
	   * @param PointDT P - new point
	   */		

	private void createConvexPolygon(PointDT P){
		double nextPointDistance=0;							//distance between two point of convex hull
		Coordinate[] newConvex;								//body convexniho obalu
		Coordinate[] vertexsNewTriangles;					//vrcholy nove vyniklych trojuhelniku
		int i,j;
		PointDT X;
		boolean directArrayPoints=true;						//smer pruchodu po konvexnim obalu


		newConvex=new Coordinate[convex.length+1];			//pridani noveho bodu do konvexniho obalu
		for (i=0;i<convex.length-1;i++)
			newConvex[i]=convex[i];
		newConvex[newConvex.length-1]=convex[0];
		newConvex[newConvex.length-2]=P;					
															//vypocet convexniho obalu
		CoordinateArraySequence pointsWhichGenerateConvex=new CoordinateArraySequence(newConvex); 
		newConvexGeometry=new LinearRing(pointsWhichGenerateConvex,new GeometryFactory());
		newConvex=newConvexGeometry.convexHull().getCoordinates();
															//vytvoreni nove geometrie convex obalu
		pointsWhichGenerateConvex=new CoordinateArraySequence(newConvex); 
		newConvexGeometry=new LinearRing(pointsWhichGenerateConvex,new GeometryFactory());
		
		if (P.compare(newConvex[0])){						// nalezeni pretrzeni convex obalu
			i=1;
			j=newConvex.length-2;
		}
		else{
			for (i=1;i<newConvex.length;i++)
				if (P.compare(newConvex[i]))
					break;
		
			j=i+1;
			i--;
		}
	
			boolean foundedI=false;
			boolean foundedJ=false;
			for (int w=0;w<convex.length-1;w++){
		
				X=new PointDT(convex[w]);
				if (!foundedI&&X.compare(newConvex[i])){
					i=w;
					foundedI=true;
				}	
				if (!foundedJ&&X.compare(newConvex[j])){
					j=w;
					foundedJ=true;
				}	
				if (X.compare(P)){
					duplicitePoint=true;
					break;
				}
			}											//int i a j udavaji index v poli convex[] kde doslo k roztrzeni convex vstupem noveho bodu

			if (!duplicitePoint){						//kontrola duplicity
				double k=(convex[i].y-convex[j].y)/(convex[i].x-convex[j].x);
				double q1=convex[i].y-k*convex[i].x;									//zjisteni smeru pretrzeni (z i do j nebo obracene
				double minDistance=Math.abs((-k*P.x+P.y-q1)/Math.sqrt(Math.pow(k, 2)+1));
				if (convex[i].x==convex[j].x){
					if (convex[i].x==0){
						if ((convex[i+1].x-P.x)<(convex[j+1].x-P.x))
							directArrayPoints= false;
						
					}
					else{
						minDistance=Math.abs(convex[j].x-P.x);
						if (new PointDT(convex[i+1]).compare(convex[j])){
							nextPointDistance=Math.abs(convex[j+1].x-P.x);
							if (new TriangleDT(convex[i],convex[j],convex[j+1]).isTriangle()){
								if (minDistance>nextPointDistance)
									directArrayPoints = false;
							}
							else{
								nextPointDistance=Math.abs(convex[i+1].x-P.x);
								if (minDistance<=nextPointDistance)// tyafa obracene
									directArrayPoints = false;
									
							}
						}
						else{
							nextPointDistance=Math.abs(convex[i+1].x-P.x);
							if (new TriangleDT(convex[i],convex[j],convex[i+1]).isTriangle()){
								if (minDistance<=nextPointDistance)// tyafa obracene
									directArrayPoints = false;
							}
							else{
								nextPointDistance=Math.abs(convex[j+1].x-P.x);
								if (minDistance>nextPointDistance)
									directArrayPoints = false;
							}
						}
					}	
				}
				else {
					double q2;
					if (new PointDT(convex[i+1]).compare(convex[j])){
						
						q2=convex[j+1].y-k*convex[j+1].x;
						nextPointDistance=Math.abs((-k*P.x+P.y-q2)/Math.sqrt(Math.pow(k, 2)+1));
						if (new TriangleDT(convex[i],convex[j],convex[j+1]).isTriangle()){
							if (minDistance>nextPointDistance)
								directArrayPoints = false;
						}
						else{
							q2=convex[i+1].y-k*convex[i+1].x;
							nextPointDistance=Math.abs((-k*P.x+P.y-q2)/Math.sqrt(Math.pow(k, 2)+1));
							if (minDistance<nextPointDistance)
								directArrayPoints = false;	
						}
					}
					else{
						q2=convex[i+1].y-k*convex[i+1].x;
						nextPointDistance=Math.abs((-k*P.x+P.y-q2)/Math.sqrt(Math.pow(k, 2)+1));
						if (new TriangleDT(convex[i],convex[j],convex[i+1]).isTriangle()){
							if (minDistance<nextPointDistance)
								directArrayPoints = false;	
						}
						else{
							q2=convex[j+1].y-k*convex[j+1].x;
							nextPointDistance=Math.abs((-k*P.x+P.y-q2)/Math.sqrt(Math.pow(k, 2)+1));
							if (minDistance>nextPointDistance)
								directArrayPoints = false;
						}
							
					}
				}
			int index;
			
			if (!directArrayPoints){
				index=i;
				i=j;			//vytvoreni pole vrcholu nove vzniklych trojuhelniku
				j=index;
			}
			index=0;
			
			if (i<j){
				vertexsNewTriangles=new Coordinate[j-i+1];
				for (int w=i;w<=j;w++){
					vertexsNewTriangles[index]=convex[w];
					index++;
				}
			}
			else{
				vertexsNewTriangles=new Coordinate[convex.length-i+j];
				for (int w=i;w<convex.length-1;w++){
					vertexsNewTriangles[index]=convex[w];
					index++;
				}
				for (int w=0;w<=j;w++){
					vertexsNewTriangles[index]=convex[w];
					index++;
				}
			}
			

			rebuildConvex(newConvex);
			createConvexTriangles(vertexsNewTriangles, P);
			convex=newConvex;
		}		
	}
	
	/***
	   * The method for creating new convex triangles
	   *
	   *  @param Coordinate[] - array of convex points, which generate new convex triangles
	   *  @param PointDT P - new point
	   */		
		
	private void createConvexTriangles(Coordinate[] vertexsNewTriangles, PointDT P){
		int i=0;
		int j=vertexsNewTriangles.length-1;
		
		TestingTriangles newTriangles=new TestingTriangles();
		TestingTriangles topOfNewTriangles=new TestingTriangles();
		
		while (!new TriangleDT(P,new PointDT(vertexsNewTriangles[i]),new PointDT(vertexsNewTriangles[i+1])).isTriangle())
			i++;
		
		//ulozeni novych trojuhelniku do ADT spojovy seznam	
		newTriangles.trian=new TriangleDT(P,new PointDT(vertexsNewTriangles[i]),new PointDT(vertexsNewTriangles[i+1]));
		//System.out.println("s");
		
		topOfNewTriangles=newTriangles;
		
		TriangleDT pomocnyS = ringSearchConvexTriangle(new PointDT(vertexsNewTriangles[i]),new PointDT(vertexsNewTriangles[i+1]));
		
		
		saveTriangleNeighbour(newTriangles.trian,pomocnyS);
		i++;
		//newTriangles.trian.printTriangle();
		while (i!=j){
			pomocnyS=new TriangleDT(P,new PointDT(vertexsNewTriangles[i]),new PointDT(vertexsNewTriangles[i+1]));
			if (pomocnyS.isTriangle()){
				
				newTriangles.next=new TestingTriangles();
				newTriangles.next.trian=pomocnyS;
				pomocnyS = ringSearchConvexTriangle(new PointDT(vertexsNewTriangles[i]),new PointDT(vertexsNewTriangles[i+1]));
				saveTriangleNeighbour(newTriangles.next.trian,pomocnyS);
				saveTriangleNeighbour(newTriangles.trian,newTriangles.next.trian);
				//newTriangles.next.trian.printTriangle();
				newTriangles=newTriangles.next;
			}
			i++;
		}
		
		newTriangles=topOfNewTriangles;
		try{//ulozeni do dat.struktury
			while (true){
				triangles.insertToTree(newTriangles.trian, newTriangles.trian.key);
				number_Triangles++;
				if (newTriangles.next==null){
					break;
				}
				newTriangles=newTriangles.next;
				
			}
		}
		catch (Exception e){
		}
			
		testNewTriangles(topOfNewTriangles,newTriangles);
	}
	
	/***
	   * The method for creating new convex triangles
	   *
	   *  @param Coordinate[] - array of new convex points, which must be getting full
	   * 
	   */		
	
	
	private void rebuildConvex(Coordinate[] newConvex){
		boolean isSame; 			//body newConvex,jsou body nejmensiho convexniho obalu,
		for (int w=0;w<convex.length-1;w++){//zde se doplni body ktere lezi na hrane convexu
			isSame=false;
			for (int v=0;v<newConvex.length-1;v++)
				if (new PointDT(convex[w]).compare(newConvex[v]))
					isSame=true;
			if (!isSame){
				Coordinate[] oldPoint={convex[w]}; 
				CoordinateArraySequence oldPoints=new CoordinateArraySequence(oldPoint);
				Point oldP=new Point(oldPoints,new GeometryFactory());
				if (newConvexGeometry.contains(oldP))
					newConvex=addPointToConvexGeometry(oldP,newConvex);
			}		
		}
	}
	
	/***
	   * The method for adding point, which is on edge of convex hull, but isn't in points which generate convex hull
	   *
	   * @param PointDT P - new point
	   * 		Coordinate[] - coordinate of convex hull's points
	   *		 
	   * @return Coordinate[] - coordinate of convex hull's points
	   */				
	
	private Coordinate[] addPointToConvexGeometry(Point P, Coordinate[] pointsOfConvex){
		Coordinate[] edgeOfConvex=new Coordinate[2];
		
		for (int i=0;i< pointsOfConvex.length-1;i++){
			edgeOfConvex[0]= pointsOfConvex[i];
			edgeOfConvex[1]= pointsOfConvex[i+1];
			CoordinateArraySequence q=new CoordinateArraySequence(edgeOfConvex); 
			LineString L=new LineString(q,new GeometryFactory());
			
			if (L.contains(P)){
				int j;
				Coordinate[] tempArray=new Coordinate[ pointsOfConvex.length+1];
				for (j=0;j<=i;j++)
					tempArray[j]= pointsOfConvex[j];
				tempArray[j++]=P.getCoordinate();
				for (;j<tempArray.length;j++)
					tempArray[j]= pointsOfConvex[j-1];

				return tempArray;
			}
		}
		return null;
	}

		
		/***
		   * The method for printing all triangles
		   */		

	/*	public void printAllTriangles(){
			double[] key=new double[2];
			TriangleDT T;
			int i=0;
			key[0]=50;
			key[1]=50;
			System.out.println("pocet TROJUHELNIKU JE"+number_Triangles);
			try{
				do{
//					T=(TriangleDT) triangles.nearest(key);
					System.out.print(i+" triangle: ");
					T.printTriangle();
					i++;
					key=T.key;
					triangles.delete(key);
				}while (T!=null);
			}
			catch(Exception e){
				System.out.println(e);
			}
		}
		
		public void getShapeFile() {
	try{
    	String URI="c:/temp/triangles_100.shp";
    	AttributeType geom = AttributeTypeFactory.newAttributeType("the_geom",
    			LinearRing.class);
   
    	FeatureType ftRoad = FeatureTypeBuilder.newFeatureType(
    			new AttributeType[] { geom}, "triangle");

    	// feature : instance creation 
    	WKTReader wktReader = new WKTReader();
    	LineString geometry = (LineString) wktReader
    			.read("LINEARRING (0 0, 10 10, 20 30,12 23, 0 0)");
    	LineString geometry1 = (LineString) wktReader
		.read("LINEARRING (0 0, 30 10, 50 30,125 23, 0 0)");

    	Float width = new Float(10);
    	Feature theRoad = ftRoad.create(new Object[] { geometry },
    			"Triangles");
    	CoordinateReferenceSystem crs = CRS.decode("EPSG:2065");

    	// datastore creation
    	URL anURL = (new File(URI)).toURL();
    	ShapefileDataStore datastore = new ShapefileDataStore(anURL);
    	datastore.forceSchemaCRS( crs );
    	
    	datastore.createSchema(ftRoad);

    	
    	// file saving
    	FeatureWriter aWriter = datastore.getFeatureWriter("triangle",
    			((FeatureStore) datastore.getFeatureSource("triangle"))
    					.getTransaction());
    	
    	double[] key=new double[2];
		TriangleDT T;
		int i=0;
		key[0]=50;
		key[1]=50;
		System.out.println("pocet TROJUHELNIKU JE"+number_Triangles);
		
			do{
	//			T=(TriangleDT) triangles.nearest(key);
			   	Feature aNewFeature = aWriter.next();
		
			   	geometry = (LinearRing) wktReader
    			.read("LINEARRING ("+T.A.x+" "+T.A.y+","+T.B.x+" "+T.B.y+","+T.C.x+" "+T.C.y+","+T.A.x+" "+T.A.y+")");
    		   	
		    	aNewFeature.setAttribute(0,geometry);
		    				
				i++;
				key=T.key;
				triangles.delete(key);
			}while (i<number_Triangles);
	
    	aWriter.write();
    	aWriter.close();
    	datastore.forceSchemaCRS( crs );
        	

			
			
			
			}
			catch(Exception e){
				System.out.println(e);
			}
		
			
			
		}*/
		
	}
		
		
