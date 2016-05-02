package main;

import graphics.HitRecord;
import graphics.Light;
import graphics.Material;
import graphics.Sphere;
import graphics.Surface;
import graphics.Triangle;
import util.Color;
import util.Vector3D;

public class BubbleRayTracer {
	public Scene scene;
	private final int MAX_DEPTH = 3;

	public BubbleRayTracer() {
		scene = new Scene();
		scene.SetupScene();
	}
	
	public Color Trace(Vector3D start, Vector3D direction, double tmin,
			double tmax, double perlinRandom, int depth){
		direction = Vector3D.Normalize(direction);
		Color color;
		
		HitRecord hitRecord = getHitSpherePt(start, direction, tmin, tmax);
		if(hitRecord != null){ //trace to bubble
			Vector3D hitPt = hitRecord.getHitPt();
			Sphere surface = (Sphere)hitRecord.getSurface();
			Vector3D normal = surface.getNormal(hitPt);
			//reflection
			double nd = Vector3D.DotProduct(normal, direction);
			Vector3D reflectDirection = Vector3D.Add(direction,
					Vector3D.Scale(normal, nd * -2));
			Color reflectColor;// = new Color(0.5,0.5,0.5);
			if(depth != MAX_DEPTH){
				reflectColor = Trace(hitPt, reflectDirection, tmin, tmax, perlinRandom, depth + 1);
			} 
			else {
				reflectColor = Scene.skyBox.HitColor(hitPt, reflectDirection);
				//reflectColor = new Color(0.5,0.5,0.5);
			} 
			
			//interference
			double thickness = surface.getThickness(hitPt, perlinRandom);
			reflectColor = getInterfereLight2(reflectDirection, normal, reflectColor, thickness, scene.testMaterial);
			color = reflectColor;
			
			//refraction
			Color refractColor;
			if(depth != MAX_DEPTH){
				 refractColor = Trace(hitPt, direction, tmin, tmax, perlinRandom, depth+1);
			}
			else {
				refractColor = Scene.skyBox.HitColor(surface.getCenter(), direction);
				//refractColor = new Color(0.5,0.5,0.5);
			}
			
			//mix
			double cos = Math.abs(Vector3D.cos(normal, direction));
			double R0 = Scene.testMaterial.getR0();
			double R = R0 + (1 - R0) * Math.pow(1 - cos, 5);
			//R += 0.05;
			reflectColor.Scale(R);
			refractColor.Scale(1-R);
			
			color = Color.Add(reflectColor, refractColor); 
			
			
		}else{ //trace to skybox
			color = scene.skyBox.HitColor(start, direction);
		}
		
		
		return color;
	}
	
	private HitRecord getHitSpherePt(Vector3D start, Vector3D direction, double tmin,
			double tmax){
		HitRecord record = null;
		double distance = tmax;
		for(Sphere bubble : scene.getBubbleSphList()){
			HitRecord hit = bubble.hitNearestPoint(start, direction, tmin, tmax);
			if(hit != null && hit.getDistance() < distance){
				record = hit;
				distance = hit.getDistance();
			}
		}
		return record;
	}
	
	private Color getInterfereLight(Vector3D lightDirection, Vector3D normal, Color lightColor, double thickness, Material material){
		Vector3D toLightDirection = lightDirection;
		double cos = Math.abs(Vector3D.cos(normal, toLightDirection));
		double d = 2 * thickness * material.getRfrcIdx() * cos;
		//R
		double eoplR = d + Light.WAVELENGTH_RED / 2;
		double modR = eoplR % Light.WAVELENGTH_RED; 
		double scaleR = Math.abs(modR - Light.WAVELENGTH_RED / 2) / Light.WAVELENGTH_RED * 4;
		double interfereR = lightColor.r * scaleR;
		//G
		double eoplG = d + Light.WAVELENGTH_GREEN / 2;
		double modG = eoplG % Light.WAVELENGTH_GREEN; 
		double scaleG = Math.abs(modG - Light.WAVELENGTH_GREEN / 2) / Light.WAVELENGTH_GREEN * 4;
		System.out.println("scale g = " + scaleG);
		double interfereG = lightColor.g * scaleG;
		//B
		double eoplB = d + Light.WAVELENGTH_BLUE / 2;
		double modB = eoplB % Light.WAVELENGTH_BLUE; 
		double scaleB = Math.abs(modB - Light.WAVELENGTH_BLUE / 2) / Light.WAVELENGTH_BLUE * 4;
		double interfereB = lightColor.b * scaleB;
		//
		if(interfereR > 1) interfereR = 1;
		if(interfereG > 1) interfereG = 1;
		if(interfereB > 1) interfereB = 1;
		return new Color(interfereR, interfereG, interfereB);
	}
	
	private Color getInterfereLight2(Vector3D lightDirection, Vector3D normal, Color lightColor, double thickness, Material material){
		Vector3D toLightDirection = lightDirection;
		double cos = Math.abs(Vector3D.cos(normal, toLightDirection));
		
		double d = 2 * thickness * material.getRfrcIdx() * cos;
		//R
		double eoplR = d / Light.WAVELENGTH_RED;
		double sinR = Math.sin(eoplR);
		double scaleR = 4 * sinR * sinR;
		double interfereR = lightColor.r * scaleR;
		
		//G
		double eoplG = d / Light.WAVELENGTH_GREEN;
		double sinG = Math.sin(eoplG);
		double scaleG = 4 * sinG * sinG;
		double interfereG = lightColor.g * scaleG;
		//B
		double eoplB = d / Light.WAVELENGTH_BLUE;
		double sinB = Math.sin(eoplB);
		double scaleB = 4 * sinB * sinB;
		double interfereB = lightColor.b * scaleB;
		
		if(interfereR > 1) interfereR = 1;
		if(interfereG > 1) interfereG = 1;
		if(interfereB > 1) interfereB = 1;
		return new Color(interfereR, interfereG, interfereB);
	}
}

