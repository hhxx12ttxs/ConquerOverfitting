
public class RayTracer {
	Scene s;
	
	public RayTracer(Scene scene){
	     s = scene;
	}
	
	
	public int[] rayTrace(Ray ray, int depth){
		
		
		
		double t = Double.MAX_VALUE;
		RenderObject res = null;
		for (int i = 0; i < s.o.objectsArray.size(); i++){
			RenderObject curr =  s.o.objectsArray.get(i);
			double triceratops = getT(ray, curr);
			if (triceratops != -1 && triceratops < t){
				t = triceratops;
				res = curr;
			}
			
		}
		if(res == null){
			System.out.println("test");
			return s.ac;
		}
		Coord Trex = ray.p0C.add(ray.p0C, ray.uC.multiply(ray.uC, t));
		Coord norm = res.getNormalAt(Trex);
//		int[] color = new int[3];
//		int materID = s.o.get_obj_by_id(res.id).matId;
//		Material w = s.m.materialArray.get(materID);
//		for (int j = 0; j < 3; j++){
//			color[j] = (int)(w.rgb[j] * 255);
//		}
		return rayShade(res, ray, Trex, norm, depth);

//		
//		
//		
		//return color;
	}
	
	
	public int[] rayShade(RenderObject o, Ray r, Coord p, Coord n, int depth){
		int[] color = new int[3];
		for (int i = 0; i < 3; i++){
			color[i] = 0;
		}
		int materID = s.o.get_obj_by_id(o.id).matId;
		Material w = s.m.materialArray.get(materID);
		
//		Ray reflect;
//		Ray refract;
		Ray shadow;
		Ray light;
		Ray viewer;
		Coord sdir;
		Coord ldir;
		for (int i = 0; i <  s.l.lightArray.size(); i++){
			Light curr = s.l.lightArray.get(i);
			if(curr.getType() == "point"){
				sdir = p.subtract(curr.getPos(), p);
				ldir = p.subtract(p, curr.getPos());
				ldir.normalize();
				sdir.normalize();
				light = new Ray(curr.getPos(), ldir);
				shadow = new Ray(p, sdir);
			}
			else if(curr.getType() == "infinite"){
				sdir = curr.getDir();
				ldir = curr.getDir();
				sdir.multiply(-1);
				light = new Ray(curr.getPos(), ldir);
				shadow = new Ray(p, sdir);
			}
			else{
				return null;
			}
			double incident = n.dotProduct(shadow.uC);
			
			if(incident > 0){
				boolean isShade = false;
				for (int j = 0; j < s.o.objectsArray.size(); j++){
					if(j != o.id){
						RenderObject currO =  s.o.objectsArray.get(j);
						double shade = getT(shadow, currO);
						if(shade > 0){
							isShade = true;
						}
					}
				}
				
			   if(!isShade){
				   
				   float diffuse = w.Kd * (float)(n.dotProduct(shadow.uC));
				   float specular = 1f;
				   float coefficient = diffuse * specular;
				   
					for (int j = 0; j < 3; j++){
						color[j] += (int)(w.rgb[j] * coefficient * 255f);
					}
			   }
			}
		}
		return color;
		
	}
	
	
	
	public double getT(Ray r, RenderObject o){
			
			
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

