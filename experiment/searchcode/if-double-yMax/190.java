package m2if;

public class Rectangle {

	private double Xmin;
	private double Xmax;
	private double Ymin;
	private double Ymax;
	
	/**
	 * construit le plus petit rectangle contenant le segment donnĂŠ en argument.
	 * @param points
	 */
	public Rectangle(Point[] points){
		double e = 0.000001;
		Xmin = points[0].getX()+e;
		Xmax = points[0].getX()-e;
		Ymin = points[0].getY()+e;
		Ymax = points[0].getY()-e;
		for(Point p : points){
			if(p.getX()>=Xmax){
				Xmax = p.getX();
			}
			if(p.getX()<=Xmin){
				Xmin = p.getX();
			}
			if(p.getY()>=Ymax){
				Ymax = p.getY();
			}
			if(p.getY()<=Ymin){
				Ymin = p.getY();
			}
		}
	}
	
	public boolean containsCoord(double x, double y){
		
		return x<Xmax && x>Xmin && y<Ymax && y>Ymin;
	}

	public double getXmin() {
		return Xmin;
	}

	public double getXmax() {
		return Xmax;
	}

	public double getYmin() {
		return Ymin;
	}

	public double getYmax() {
		return Ymax;
	}
	
}

