package main;

import graphics.HitRecord;
import graphics.Light;
import graphics.Material;
import graphics.Triangle;

import java.util.List;

import com.sun.org.apache.xerces.internal.impl.dtd.models.DFAContentModel;

import sun.org.mozilla.javascript.internal.InterfaceAdapter;
import util.Color;
import util.Vector3D;

public class RayTracer {

	public static final int MAX_DEPTH = 2;
	private Scene scene;

	public RayTracer() {
		scene = new Scene();
		scene.SetupScene();
	}
	
	/* reverseLightIfNeed --> for refraction, lightOutSphere -> lightIntoTheSphere*/
	public Color Trace(Vector3D start, Vector3D direction, double tmin,
			double tmax, Vector3D eyePt, int depth) {
		direction = Vector3D.Normalize(direction);
		Color color = scene.getBackgroundColor();
		// is hit
		HitRecord hitRecord = getHitRecord(start, direction, tmin, tmax);
		if (hitRecord != null) {
			color = Color.DotMultiply(scene.getAmbientLight().getAmbient(),
					Scene.testMaterial.getAmbient());
			Triangle surface = (Triangle) hitRecord.getSurface();

			for (Light light : scene.getLightList()) {
				Light interfereLight = light.Clone();
				Color interfereColor = getInterfereLight(hitRecord.getHitPt(), light, surface.getNormal(), Scene.testMaterial, hitRecord);
				interfereLight.setDiffuse(interfereColor);
				interfereLight.setSpecular(interfereColor);
				Color diffuse = getDiffuseColor(hitRecord.getHitPt(), interfereLight,
						surface.getNormal(), Scene.testMaterial);
				if (diffuse != null) {
					color = Color.Add(color, diffuse);
				}
				Color specular = getSpecularColor(hitRecord.getHitPt(), interfereLight,
						surface.getNormal(), Scene.testMaterial, eyePt);
				if (specular != null) {
					color = Color.Add(color, specular);
				}

			}

			/* recursive part */
			if(false){
			//if (depth != MAX_DEPTH) {
				
				/* color += reflect * R + refract * (1-R) */
				double cos = Math.abs(Vector3D.cos(surface.getNormal(), direction));
				double R0 = Scene.testMaterial.getR0();
				double R = R0 + (1 - R0) * Math.pow(1 - cos, 5);
				System.out.println("\t\t\t\t\tR = " + R);
				
				// reflective specular
				double nd = Vector3D.DotProduct(surface.getNormal(), direction);
				Vector3D reflectRay = Vector3D.Add(direction,
						Vector3D.Scale(surface.getNormal(), nd * 2));
				Color reflectLight = Trace(hitRecord.getHitPt(), reflectRay, tmin, tmax,
						eyePt, depth + 1);
				Color relectiveSpecular = Color.DotMultiply(
						Scene.testMaterial.getSpecular(),reflectLight);
				relectiveSpecular.Scale(R);
				
				color = Color.Add(color, relectiveSpecular);
			
				//refraction
				Vector3D d = Vector3D.Normalize(direction);
				Vector3D n = Vector3D.Normalize(surface.getNormal());
				double nt = Scene.testMaterial.getRfrcIdx();
				Vector3D left = Vector3D.Add(d, Vector3D.Scale(n, cos));//here cos > 0
				Vector3D scaledLeft = Vector3D.Scale(left, 1 / nt);
				double rightScale = Math.sqrt(1 - (1 - cos*cos) / (nt*nt));
				Vector3D t = Vector3D.Substract(scaledLeft, Vector3D.Scale(n, rightScale));
				double thickness = 0.1;
				t.Print("\t\t\t\t\t\t t ");
				Vector3D outPt = Vector3D.Add(hitRecord.getHitPt(), Vector3D.Scale(t, thickness));
				Color refractLight = Trace(outPt, direction, tmin, tmax,
						eyePt, depth + 1);
				//Color refractLight = Trace(hitRecord.getHitPt(), direction, 0.1, tmax,
				//		eyePt, depth + 1);
				if(refractLight != null){System.out.println("dd");}
				//refractLight.Scale(1 - R);
				color.Print("before refract");
				refractLight.Print("refractLight");
				color = Color.Add(color, refractLight);
			}
			color.ToColor255().Print("final");
		}
		return color;
	}

	private Color getDiffuseColor(Vector3D pt, Light light, Vector3D normal,
			Material material) {
		Color diffuse = null;
		if (light.getType() == Light.SPOT_LIGHT_ALL_DIRECTION) {
			Vector3D toLightDirection = Vector3D.Substract(light.getPos(), pt);
			double cos = Vector3D.cos(toLightDirection, normal);

			if (cos > 0) {
				diffuse = Color.DotMultiply(light.getDiffuse(),
						material.getDiffuse());
				diffuse.Scale(cos);
				System.out.println("cos = " + cos);
			}
		}
		return diffuse;
	}

	private Color getSpecularColor(Vector3D pt, Light light, Vector3D normal,
			Material material, Vector3D eyePt) {
		Color specular = null;
		if (light.getType() == Light.SPOT_LIGHT_ALL_DIRECTION) {
			Vector3D toLightDirection = Vector3D.Substract(light.getPos(), pt);
			Vector3D toEyeDirection = Vector3D.Substract(eyePt, pt);
			Vector3D H = Vector3D.Add(toLightDirection, toEyeDirection);
			double cos = Vector3D.cos(normal, H);
			if (cos > 0) {
				System.out.println("\t\t\t\tcos = "
						+ Math.pow(cos, material.getShineness()));
				specular = Color.DotMultiply(light.getSpecular(),
						material.getSpecular());
				specular.Scale(Math.pow(cos, material.getShineness()));
			}
		}
		return specular;
	}

	private HitRecord getHitRecord(Vector3D start, Vector3D direction,
			double tmin, double tmax) {
		HitRecord record = null;
		for (Triangle triangle : scene.getTriangleList()) {
			if (triangle.hit(start, direction, tmin, tmax) == true) {
				HitRecord hitRecord = triangle.hitNearestPoint(start,
						direction, tmin, tmax);
				if (record == null)
					record = hitRecord;
				else if (hitRecord.getDistance() < record.getDistance())
					record = hitRecord;
			}
		}
		return record;
	}
	
	private Color getInterfereLight(Vector3D pt, Light light, Vector3D normal, Material material, HitRecord record){
		Triangle triangle = (Triangle)record.getSurface();
		double thickness = triangle.getBubble().getThickness(pt);
		Vector3D toLightDirection = Vector3D.Substract(light.getPos(), pt);
		double cos = Vector3D.cos(normal, toLightDirection);
		double d = 2 * thickness * material.getRfrcIdx() * cos;
		
		//R
		double eoplR = d + Light.WAVELENGTH_RED / 2;
		double modR = eoplR % Light.WAVELENGTH_RED; 
		double scaleR = Math.abs(modR - Light.WAVELENGTH_RED / 2) / Light.WAVELENGTH_RED * 2;
		double interfereR = light.getDiffuse().r * scaleR;
		//G
		double eoplG = d + Light.WAVELENGTH_GREEN / 2;
		double modG = eoplG % Light.WAVELENGTH_GREEN; 
		double scaleG = Math.abs(modG - Light.WAVELENGTH_GREEN / 2) / Light.WAVELENGTH_GREEN * 2;
		double interfereG = light.getDiffuse().g * scaleG;
		//B
		double eoplB = d + Light.WAVELENGTH_BLUE / 2;
		double modB = eoplB % Light.WAVELENGTH_BLUE; 
		double scaleB = Math.abs(modB - Light.WAVELENGTH_BLUE / 2) / Light.WAVELENGTH_BLUE * 2;
		double interfereB = light.getDiffuse().b * scaleB;
		//
		if(interfereR > 1) interfereR = 1;
		if(interfereG > 1) interfereG = 1;
		if(interfereB > 1) interfereB = 1;
		return new Color(interfereR, interfereG, interfereB);
	}
}

