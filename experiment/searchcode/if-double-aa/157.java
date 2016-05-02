
public class RayCaster {
	Scene s;
	
	public RayCaster(Scene scene){
		this.s = scene;
	}
	
	
	public int[] castRay(int x, int y){
		RayTracer rayTracer = new RayTracer(s);
		Coord rayP = new Coord();
		Coord rayD = new Coord();
		
		double du, dv;
		du = (x -((s.c.viewportWidth)/2)+1)*s.c.xRes();
		dv = (-y+((s.c.viewportHeight)/2)+1)*s.c.yRes();
		
		
		
		rayP.x = s.c.cCop.x + (s.c.n.x * s.c.getFocal()) + (du*s.c.u.x) + (dv*s.c.v.x);
		rayP.y = s.c.cCop.y + (s.c.n.y * s.c.getFocal()) + (du*s.c.u.y) + (dv*s.c.v.y);
		rayP.z = s.c.cCop.z + (s.c.n.z * s.c.getFocal()) + (du*s.c.u.z) + (dv*s.c.v.z);
		
		
		if(s.c.orthographic()){
			rayD.copy(s.c.n);
		}
		
		Ray ray = new Ray (rayP, rayD);
		
		//if(rayTracer != null){
		return rayTracer.rayTrace(ray, 0);
		//}
		//else{
//			double t = Double.MAX_VALUE;
//			RenderObject res = null;
//			for (int i = 0; i < s.o.objectsArray.size(); i++){
//				RenderObject curr =  s.o.objectsArray.get(i);
//				double triceratops = getT(ray, curr);
//				if (triceratops != -1 && triceratops < t){
//					t = triceratops;
//					res = curr;
//				}
//				
//			}
//			//System.out.println(res.t);
//			int[] color = new int[3];
//			if(res == null){
//				color[0]= 0;
//				color[1]= 0;
//				color[2]= 0;
//				return color;
//			}
//			
//			int materID = s.o.get_obj_by_id(res.id).matId;
//			Material w = s.m.materialArray.get(materID);
//			for (int i = 0; i < 3; i++){
//				color[i] = (int)(w.rgb[i] * 255);
//			}
//			
//			
//			
//			return color;
//		}
	}
	
	double getT(Ray r, RenderObject o){
		
		
		Matrix m = new Matrix();
		double[][] u = r.u3;
		double[][] ut = m.tMatrix(u);
		double[][] p = r.p0C.subtract(r.p0C, o.getOrigin()).toArray3D();
		double[][] pt = m.tMatrix(p);
		
		
		
		double[][] aa = m.multiplyMatrix(ut, u);
		double a = aa[0][0];		  
		
		double bb[][] = m.multiplyMatrix(ut, p);
		double b = bb[0][0] * 2;
		
		
		double cc[][] = m.multiplyMatrix(pt, p);
		double cee = cc[0][0] - (o.getRadius() * o.getRadius());
		double existance = (b*b)-(4*a*cee);
		if(existance < 0){
			return -1;
		}
		else{
			double t1 = ((-1 * b) + Math.sqrt(existance))/ (2*a);
			double t2 = ((-1 * b) - Math.sqrt(existance))/ (2*a);
			if(t1 < 0 && t2 < 0){
				return -1;
			}
			else if (t1 < 0){
				return t2;
			}
			else if (t2 < 0){
				return t1;
			}
			else{
				return Math.min(t1, t2);
			}
		}
		
	}
	
	
	
}

