package MFS;

import java.util.ArrayList;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrix;

import etc.Functions;

public class MFS {
	
	private String fileName_;
	private String name_;
	
	private ArrayList<BoundaryPoint> boundaryPointList_ = new ArrayList<BoundaryPoint>();
	private Point2D[] surfacePoints_;
	
	private double E_, nu_;
	
	private double minX_, maxX_, minY_, maxY_;
	
	private Matrix mfsK;
	
	public MFS(String fileName){
		this.fileName_ = fileName;
		
		int cutter =  fileName.lastIndexOf("\\") +1 ;
		this.name_ = fileName.substring(cutter);
		
		this.maxX_ = Double.NEGATIVE_INFINITY;
		this.minX_ = Double.POSITIVE_INFINITY;
		
		this.maxY_ = Double.NEGATIVE_INFINITY;
		this.minY_ = Double.POSITIVE_INFINITY;
	}
	
	public void addBoundaryPoint(BoundaryPoint boundaryPoint){
		boundaryPointList_.add(boundaryPoint);
	}
	
	public void setE(double E){
		this.E_ = E;
	}
	
	public void setNu(double nu){
		this.nu_ = nu;
	}
	
	public double getE(){
		return this.E_;
	}
	
	public double getNu(){
		return this.nu_;
	}
	
	public double getG(){
		return this.getE()/(2*(1+this.getNu()));
	}
	
	public void setSurfacePoints(){
		
		if( this.maxX_ == Double.NEGATIVE_INFINITY ){
			this.setBorder();
		}
		
		this.surfacePoints_ = new Point2D[this.boundaryPointList_.size()];
		
		double xLength = this.maxX_ - this.minX_;
		double yLength = this.maxY_ - this.minY_;
		
//		double radius = xLength > yLength ? xLength : yLength;
		double radius = 5;
		
		double radian = 2*Math.PI / this.surfacePoints_.length;
		
		for(int i=0; i<this.surfacePoints_.length; i++){
			double surfaceX = this.getCenter().getXCoord() + radius * Math.cos( radian*i );
			double surfaceY = this.getCenter().getYCoord() + radius * Math.sin( radian*i );
			
			this.surfacePoints_[i] = new Point2D((i+1), surfaceX, surfaceY);
		}
	}
	
	private void setBorder(){

		for( BoundaryPoint boundaryPoint : this.getBoundaryPoints() ){
			if( boundaryPoint.getXCoord() > maxX_ ){
				maxX_ = boundaryPoint.getXCoord();
			}
			else if( boundaryPoint.getXCoord() < minX_ ){
				minX_ = boundaryPoint.getXCoord();
			}
			
			if( boundaryPoint.getYCoord() > maxY_ ){
				maxY_ = boundaryPoint.getYCoord();
			}
			else if( boundaryPoint.getYCoord() < minY_){
				minY_ = boundaryPoint.getYCoord();
			}
		}
	}
	
	public Point2D getCenter(){
		if( this.maxX_ == Double.NEGATIVE_INFINITY ){
			this.setBorder();
		}
		return new Point2D(-1, (maxX_ + minX_)/2, (minY_ + maxY_)/2 );
	}
	
	public BoundaryPoint getBoundaryPoint(int no){
		for(BoundaryPoint boundaryPoint : this.getBoundaryPoints() ){
			if( no == boundaryPoint.getNo() ){
				return boundaryPoint;
			}
		}
		
		return null;
	}
	
	public ArrayList<BoundaryPoint> getBoundaryPoints(){
		return this.boundaryPointList_;
	}
	
	public Point2D[] getSurfacePoints(){
		return this.surfacePoints_;
	}
	
	public Point2D getSurfacePoint(int pointNo){
		for(Point2D surfacePoint : this.getSurfacePoints()){
			if(pointNo == surfacePoint.getNo()){
				return surfacePoint;
			}
		}
		
		return null;
	}
	
	private double lnrj(Point2D x, int j){
		Point2D surfacePoint = this.getSurfacePoint(j);
		return Math.log( x.distance(surfacePoint) );
	}
	
	private double differentialR(Point2D x, Point2D surfacePoint, int i) throws Exception{
		double R = x.distance(surfacePoint);
		if( i==1 ){
			return (x.getXCoord() - surfacePoint.getXCoord() ) / R;
		}
		else if( i== 2){
			return (x.getYCoord() - surfacePoint.getYCoord() ) / R;
		}
		else{
			throw new Exception("Invalid index");
		}
	}
	
	private double differentialR(Point2D x, int j, int m) throws Exception{
		Point2D surfacePoint = this.getSurfacePoint(j);
		return this.differentialR(x, surfacePoint, m);
	}
	
	private double drdn(int boundaryPointNo, int surfacePointNo){
		Point2D surfacePoint = this.getSurfacePoint(surfacePointNo);
		BoundaryPoint boundaryPoint = this.getBoundaryPoint(boundaryPointNo);
		return this.drdn(boundaryPoint, surfacePoint);
	}
	
	//differential R to normal vector direction
	private double drdn(BoundaryPoint boundaryPoint, Point2D surfacePoint){
		double delta = 1e-100;
		
		Point2D x2 = new Point2D(-2, boundaryPoint.getXCoord() + delta * boundaryPoint.getNormalVector().getX(),
									boundaryPoint.getYCoord() + delta * boundaryPoint.getNormalVector().getY());
		Point2D x1 = new Point2D(-1, boundaryPoint.getXCoord() - delta * boundaryPoint.getNormalVector().getX(),
									boundaryPoint.getYCoord() - delta * boundaryPoint.getNormalVector().getY());
		
		double r2 = x2.distance(surfacePoint);
		double r1 = x1.distance(surfacePoint);
		
		return ( r2 - r1 ) / ( 2 * delta);
	}
	

	
//	private double differentialR2(Point2D x, Point2D d, int m) throws Exception{
//		double delta = 1e-10;
//		
//		Point2D x2, x1;
//		
//		if( m == 1){
//			x2 = new Point2D(-2, x.getXCoord() + delta, x.getYCoord());
//			x1 = new Point2D(-1, x.getXCoord() - delta, x.getYCoord());
//		}
//		else if( m== 2){
//			x2 = new Point2D(-2, x.getXCoord() , x.getYCoord() + delta);
//			x1 = new Point2D(-1, x.getXCoord() , x.getYCoord() - delta);
//		}
//		else{
//			throw new Exception("invalid index! index must be 1 or 2");			
//		}
//		
//		double rjx2 = x2.distance(d);
//		double rjx1 = x1.distance(d);
//		
//		return ( rjx2 - rjx1 ) / (2*delta);
//	}
//	
//	private double differentialR2(Point2D x, int j, int m) throws Exception{
//		double delta = 1e-200;
//		
//		Point2D x2, x1;
//		
//		if( m == 1){
//			x2 = new Point2D(-2, x.getXCoord() + delta, x.getYCoord());
//			x1 = new Point2D(-1, x.getXCoord() - delta, x.getYCoord());
//		}
//		else if( m== 2){
//			x2 = new Point2D(-2, x.getXCoord() , x.getYCoord() + delta);
//			x1 = new Point2D(-1, x.getXCoord() , x.getYCoord() - delta);
//		}
//		else{
//			throw new Exception("invalid index! index must be 1 or 2");			
//		}
//		
//		double rjx2 = x2.distance(this.getSurfacePoint(j));
//		double rjx1 = x1.distance(this.getSurfacePoint(j));
//		
//		return ( rjx2 - rjx1 ) / (2*delta);
//		
//	}
	
//	private double drdn(BoundaryPoint ){
//		
//	}
	
	private double ujlm(Point2D x, int j, int l, int m) throws Exception{
		double lnrj = lnrj(x, j );
		double ujlm = -1.0 / ( 8*Math.PI*(1-this.getNu())*this.getG()) 
				       * ( (3-4*this.getNu())*lnrj*Functions.kroneckerDelta(l, m) 
				    		   - this.differentialR(x, j, m) * this.differentialR(x, j, l) ) ;
		
		return ujlm;
	}
	
	private double pjlm(Point2D x, int i, int j, int l, int m) throws Exception{
		double rjx = x.distance( this.getSurfacePoint(j) );
		double drdn = this.drdn(i, j);
		BoundaryPoint boundaryPoint = this.getBoundaryPoint(i);
		double pjlm = -1.0 / ( 4*Math.PI * (1- this.getNu()) * rjx )
					* ( ( (1-2*this.getNu() )*Functions.kroneckerDelta(l, m) 
							+ 2 * differentialR(x, j, l) * differentialR(x, j, m)  ) * drdn 
						- (1-2*this.getNu())*(differentialR(x, j, l) * boundaryPoint.getNormalVector().getXY(m) -differentialR(x, j, m)*boundaryPoint.getNormalVector().getXY(l) )) ; 
		
		return pjlm;
	}
	
	public void setMfsKmatrix() throws Exception{
		this.mfsK = new DenseMatrix( 2*this.getBoundaryPoints().size(), 2 * 2*this.getBoundaryPoints().size());
		
		for(int i=0; i < 2*this.getBoundaryPoints().size() ; i++){
			for( int j=0; j< 2*this.getBoundaryPoints().size(); j++){
				BoundaryPoint xi = this.getBoundaryPoints().get(i);
				int m = i%2+1;
				
				double uj1m = this.ujlm(xi, j, 1, m);
				double uj2m = this.ujlm(xi, j, 2, m);
				
//				this.mfsK.set(i*2 + 1*(i%2), , arg2)
			}
		}
		
	}
	
	
	
//	public static void main(String[] arg0){
//		Point2D x = new Point2D(1, 10, 10);
//		Point2D d = new Point2D(2, 30, 40);
//		
//		MFS mfs = new MFS("test");
//		
//		try {
//			double result1 = mfs.differentialR(x,d, 2);
//			System.out.println( result1);
//			
//			double result2 = mfs.differentialR2(x, d, 2);
//			System.out.println( result2 );
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	}
	
	
	public String getName(){
		return this.name_;
	}

}

