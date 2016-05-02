package graphics;


import com.sun.opengl.impl.mipmap.Image;

import sun.net.www.content.audio.x_aiff;
import util.Color;
import util.Vector3D;

public class SkyBox {
	public static final int SKYBOX_FRONT = 0;
	public static final int SKYBOX_BACK = 1;
	public static final int SKYBOX_LEFT = 2;
	public static final int SKYBOX_RIGHT = 3;
	public static final int SKYBOX_TOP = 4;
	public static final int SKYBOX_BOTTOM = 5;
	
	private Vector3D minPt;
	private Vector3D maxPt;
	
	private util.Image[] images;
	private Vector3D[][] imgToWorldPos;
	private final String[] imgNames = {"front", "back", "left", "right", "top", "bottom"};
	public SkyBox(Vector3D minPt, Vector3D maxPt) {
		this.minPt = minPt;
		this.maxPt = maxPt;
	}
	public void Load(String path, String postfix){
		images = new util.Image[6];
		for(int i = 0; i < 6; i++){
			String imgPath = path + "\\" + imgNames[i] + "." + postfix;
			util.Image img = new util.Image();
			img.OpenImg(imgPath);
			images[i] = img;
		}
		SetupRelation();
	}
	
	private void SetupRelation(){
		imgToWorldPos = new Vector3D[6][2];
		double leftX = minPt.x;
		double rightX = maxPt.x;
		double frontZ = minPt.z;
		double backZ = maxPt.z;
		double bottomY = minPt.y;
		double topY = maxPt.y;
		imgToWorldPos[SKYBOX_FRONT][0] = new Vector3D(leftX, topY, frontZ);
		imgToWorldPos[SKYBOX_FRONT][1] = new Vector3D(rightX, bottomY, frontZ);
		imgToWorldPos[SKYBOX_BACK][0] = new Vector3D(rightX, topY, backZ);
		imgToWorldPos[SKYBOX_BACK][1] = new Vector3D(leftX, bottomY, backZ);
		
		imgToWorldPos[SKYBOX_LEFT][0] = new Vector3D(leftX, topY, backZ);
		imgToWorldPos[SKYBOX_LEFT][1] = new Vector3D(leftX, bottomY, frontZ);
		imgToWorldPos[SKYBOX_RIGHT][0] = new Vector3D(rightX, topY, frontZ);
		imgToWorldPos[SKYBOX_RIGHT][1] = new Vector3D(rightX, bottomY, backZ);
		
		imgToWorldPos[SKYBOX_TOP][0] = new Vector3D(leftX, topY, backZ);
		imgToWorldPos[SKYBOX_TOP][1] = new Vector3D(rightX, topY, frontZ);
		imgToWorldPos[SKYBOX_BOTTOM][0] = new Vector3D(leftX, bottomY, frontZ);//minPt
		imgToWorldPos[SKYBOX_BOTTOM][1] = new Vector3D(rightX, bottomY, backZ);
		
	
	}
	
	public Color HitColor(Vector3D start, Vector3D direction){
		Color color = null;
		Vector3D minP, maxP;
		int type;
		//left and right
		if(direction.x != 0){
			if(direction.x < 0){
				type = SKYBOX_LEFT;
			}else{
				type = SKYBOX_RIGHT;
			}
			minP = imgToWorldPos[type][0];
			maxP = imgToWorldPos[type][1];
			double y = (minP.x - start.x) / direction.x * direction.y + start.y;
			double z = (minP.x - start.x) / direction.x * direction.z + start.z;
			if(isInBetween(y, minP.y, maxP.y)&& isInBetween(z, minP.z, maxP.z)){
				double imgRatioX = (z - minP.z) / (maxP.z - minP.z);
				double imgRatioY = (y - minP.y) / (maxP.y - minP.y);
				color = images[type].getPixelColorRelPos(imgRatioX, imgRatioY);
				return color;
			}
		}
		//front and back
		if(direction.z != 0){
			if(direction.z < 0) type = SKYBOX_FRONT;
			else type = SKYBOX_BACK;
			minP = imgToWorldPos[type][0];
			maxP = imgToWorldPos[type][1];
			double x = (minP.z - start.z) / direction.z * direction.x + start.x;
			double y = (minP.z - start.z) / direction.z * direction.y + start.y;
			if(isInBetween(x, minP.x, maxP.x) && isInBetween(y, minP.y, maxP.y)){
				double imgRatioX = (x - minP.x) / (maxP.x - minP.x);
				double imgRatioY = (y - minP.y) / (maxP.y - minP.y);
				color = images[type].getPixelColorRelPos(imgRatioX, imgRatioY);
				return color;
			}
		}
		
		//top and bottom
		if(direction.y != 0){
			if(direction.y < 0) type = SKYBOX_BOTTOM;
			else type = SKYBOX_TOP;
			minP = imgToWorldPos[type][0];
			maxP = imgToWorldPos[type][1];
			double x = (minP.y - start.y) / direction.y * direction.x + start.x;
			double z = (minP.y - start.y) / direction.y * direction.z + start.z;
			if(isInBetween(x, minP.x, maxP.x) && isInBetween(z, minP.z, maxP.z)){
				double imgRatioX = (x - minP.x) / (maxP.x - minP.x);
				double imgRatioY = (z - minP.z) / (maxP.z - minP.z);
				color = images[type].getPixelColorRelPos(imgRatioX, imgRatioY);
				return color;
			}
		}
		
		return color;
	}
	
	
	private boolean isInBetween(double x, double a, double b){
		
		double product = (x - a) * (x - b);
		if(product <= 0) return true;
		else if(Math.abs(x - a) < 1 || Math.abs(x - b) < 1) return true;
		else return false;
	}
}

