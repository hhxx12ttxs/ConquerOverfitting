
public class Ray {
	Coord p0C;
	double[][] p03;
	double[][] p04;
	
	Coord uC;
	double[][] u3;
	double[][] u4;
	
	public Ray(Coord p0, Coord u){
		p0C = p0;
		uC = u;
		p03 = p0.toArray3D();
		p04 = p0.toArray();
		u3 = u.toArray3D();
		u4 = u.toArray();
	}
	
	public Ray(double[][] p0, double[][] u){
		if(p0.length == 3){
			p03 = p0;
			u3 = u;
			
			p0C = new Coord(); 
			p0C.fromArray(p03);
			
			uC = new Coord();
			uC.fromArray(u3);
			
			p04 = p0C.toArray();
			u4 = uC.toArray();
			
		}
		else{
			p04 = p0;
			u4 = u;
			
			p0C = new Coord();
			p0C.fromArray(p0);
			
			uC = new Coord();
			uC.fromArray(u);
			
			p03 = p0C.toArray3D();
			u3 = uC.toArray3D();
			
		}
		
	}
}

