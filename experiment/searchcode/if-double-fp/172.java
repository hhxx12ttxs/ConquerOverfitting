package fp.s100502514.metroArmy;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.GregorianCalendar;

import javax.swing.ImageIcon;

public class Scenery extends Drawable{
	int image, id; //which image for the scenery; id is not-in-use currently.
	double origin; //between 0 and 1; 0 for top, and 1 for bottom, as the center of scaling
	/*double x, z, height;*/ //In meter
	public Scenery(double x, double z, double height, int img_index, double origin){
		this.x = x;
		this.z = z;
		this.height = height;
		this.image = img_index;
		this.origin = origin;
		/*if(RaceCar.sceneryLoopUnit/2-Math.abs(x-RaceCar.sceneryLoopUnit/2)<150){
			this.z-=3.5;
		}*/
	}
	public Scenery(double x, double z, double height, int img_index){
		this(x, z, height, img_index, 1.0);
	}
	public Scenery(double height, int img_index){
		this(Math.random()*(RaceCar.sceneryLoopUnit),
				Math.random()*(RaceCar.maxDistantFromRoad-5)+5,
				height, img_index);
	}
	
	/** private variables for saving the attributes of this while painting,
	 *  and for hitTest() use. */
	private int tmp_img_x = 0, tmp_img_y = 0, tmp_img_w = 0, tmp_img_h = 0;
	
	@Override
	public void drawOn(RaceCar car, Graphics g) {
		int centerX = car.getWidth()/2, centerY = car.getHeight()/2;
		Scenery sc = this;
		double x_3d = sc.x-car.getCurrentPosition(), y_3d = -car.getCameraElavation(),
				z_3d = sc.z;
		ImageIcon ico = RaceCar.sceneries[sc.image];
		Image img = ico.getImage();
		
		//2D-transformed position and size
		double scaley = 1;/*RaceCar.scaleRatio;/* *
				((double)ico.getIconHeight()/(double)ico.getIconWidth());*/
		double scalex = 1/ /*RaceCar.scaleRatio/ */
				((double)ico.getIconHeight()/(double)ico.getIconWidth());
		double px = x_3d*(1/RaceCar.scaleRatio)/z_3d*car.getCameraSize(),
				py = -(y_3d*scaley)/z_3d*car.getCameraSize();
		double ph = sc.height*car.getCameraSize()
				*(1/* /RaceCar.scaleRatio*/*scaley)/z_3d; //Private member scaleRatio
		double pw = sc.height*car.getCameraSize()
				*(1/* /RaceCar.scaleRatio*/*scalex)/z_3d;
		if(!destroyed){
			g.drawImage(img,
					tmp_img_x = (int)(px+centerX),
					tmp_img_y = (int)(py+centerY-(ph*origin)),
					tmp_img_w = (int)(pw),
					tmp_img_h = (int)(ph),
					car);
		}else if(!Double.isNaN(getAnimationPosition())){
			double star_rate = (1-getAnimationPosition());
			int num_stars = (int)(star_rate*tmp_img_w*tmp_img_h*(6.0e-4));
			if(num_stars>40)num_stars=40;
			else if(num_stars<1)num_stars=1;
			Image star_img;
			for(int i=0; i<num_stars; i++){
				star_img = RaceCar.stars[(int)(Math.random()*
					RaceCar.stars.length)].getImage();
				double x=Math.random(), y=Math.random();
				BufferedImage tmp_img = new BufferedImage(
						g.getClipBounds().width, g.getClipBounds().height,
						BufferedImage.TYPE_4BYTE_ABGR);
				Graphics2D g2d = tmp_img.createGraphics();
				g2d.drawImage(img,
						tmp_img_x = (int)(px+centerX),
						tmp_img_y = (int)(py+centerY-(ph*0.95)),
						tmp_img_w = (int)(pw),
						tmp_img_h = (int)(ph),
						car);
				int sx = tmp_img_x+(int)((tmp_img_w)*x);
				int sy = tmp_img_y+(int)((tmp_img_h)*y);
				//System.err.println("# "+tmp_img.getWidth()+" "+tmp_img.getHeight());
				//System.err.println("  "+sx+" "+sy);
				try{
					if(((tmp_img.getRGB(sx, sy)>>>24)&0xff)>0){
						g.drawImage(star_img, sx-25, sy-25, 50, 50, car);
						//System.out.print("D");
					}
				}catch(Exception err){
					/* Do not draw the stars out-of-bounds, which may cause errors. */
				}
			}
		}
	}
	@Override
	public double getZ(){
		//ImageIcon ico = RaceCar.sceneries[image];
		return z*RaceCar.scaleRatio;
		// /((double)ico.getIconHeight()/(double)ico.getIconWidth());
	}
	
	/** Determine if the mouse overlaps the rectangle sensor-zone of this object. */
	@Override
	public boolean hitTest(Point mouse) {
		if(destroyed)return false;
		if(mouse==null)return false;
		if(mouse.x<tmp_img_x+tmp_img_w*0.0 || mouse.x>tmp_img_x+tmp_img_w*1.0){
			return false;
		}else if(mouse.y<tmp_img_y+tmp_img_h*0.03 || mouse.y>tmp_img_y+tmp_img_h*1.0){
			return false;
		}
		return true;
	}

	/** Scenery is always destroyable EXCEPT those already destroyed. */
	private boolean destroyed = false;
	@Override
	public boolean isDestroyable() {
		return !destroyed;
	}
	@Override
	public double destroyStuff() {
		if(destroyed){
			return 0;
		}else{
			visible_changed_timestamp = new GregorianCalendar().getTimeInMillis();
			destroyed = true;
			return (tmp_img_w*tmp_img_h)*(1.0e-5);
		}
	}
	
	//unix timestamp im ms
	private long visible_changed_timestamp = 0;
	private static final int disable_animation_period = 3000;
	//Returning range: 0.0(Begin) ~ 1.0(End), or NaN for Not-moving.
	protected double getAnimationPosition(){
		long time = new GregorianCalendar().getTimeInMillis();
		double result = (time - visible_changed_timestamp);
		result /= disable_animation_period;
		if(result>=0 && result<1)return result;
		else return Double.NaN;
	}
}
