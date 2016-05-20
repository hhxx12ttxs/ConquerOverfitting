package fp.s100502514.metroArmy;

import java.awt.*;
import java.awt.image.*;
import java.util.GregorianCalendar;

import javax.swing.*;

public class Person extends Drawable{
	int image, id; //which image for the person
	/*double x, z, height;*/ //In meter
	public Person(double x, double z, double height, int id, int img_index){
		this.x = x;
		this.z = z;
		this.height = height;
		this.image = img_index;
		this.id = id;
		if(RaceCar.sceneryLoopUnit/2-Math.abs(x-RaceCar.sceneryLoopUnit/2)<150){
			this.z-=3.5;
		}
	}
	public Person(double x, double z, double height, int id){
		this(x, z, height, id, (int)(Math.random()*RaceCar.persons.length));
	}
	public Person(int id, Station st){
		this(Math.random()*(st.width)+st.x,
				Math.random()*(st.depth+5)+5,
				Math.pow((Math.random()-0.45)/0.55, 9)*0.9+1.65, id);
	}
	public Person(int id){
		this(Math.random()*(RaceCar.sceneryLoopUnit),
				Math.random()*(RaceCar.maxDistantFromRoad-5)+5,
				Math.pow((Math.random()-0.45)/0.55, 9)*0.9+1.65, id);
	}
	
	/** private variables for saving the attributes of this while painting,
	 *  and for hitTest() use. */
	private int tmp_img_x = 0, tmp_img_y = 0, tmp_img_w = 0, tmp_img_h = 0;
	
	
	/** Draw-on method, called by paintComponent(g). */
	@Override
	public void drawOn(RaceCar car, Graphics g) {
		int centerX = car.getWidth()/2, centerY = car.getHeight()/2;
		Person sb = this;
		double x_3d = sb.x-car.getCurrentPosition(), y_3d = -car.getCameraElavation(),
				z_3d = getZ();//sb.z;
		ImageIcon ico = RaceCar.persons[sb.image];
		Image img = ico.getImage();
		//2D-transformed position and size
		double scale = (double)ico.getIconHeight()/(double)ico.getIconWidth();
		double px = x_3d/z_3d*car.getCameraSize(),
				py = -(y_3d*RaceCar.scaleRatio)/z_3d*car.getCameraSize();
		double ph = sb.height*car.getCameraSize()
				*RaceCar.scaleRatio/z_3d; //Private member scaleRatio
		double pw = sb.height*car.getCameraSize()
				*(RaceCar.scaleRatio/scale)/z_3d;
		
		if(isVisibleOnGUI()){
			g.drawImage(img,
					tmp_img_x = (int)(px+centerX),
					tmp_img_y = (int)(py+centerY-(ph*0.95)),
					tmp_img_w = (int)(pw),
					tmp_img_h = (int)(ph),
					car);
		}else if(!Double.isNaN(getAnimationPosition())){
			double star_rate = visible ?
					getAnimationPosition() : (1-getAnimationPosition());
			int num_stars = (int)(star_rate*60);
			Image star_img = RaceCar.stars[0].getImage();
			for(int i=0; i<num_stars; i++){
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
						g.drawImage(star_img, sx-16, sy-16, 32, 32, car);
						//System.out.print("D");
					}
				}catch(Exception err){
					/* Do not draw the stars out-of-bounds, which may cause errors. */
				}
			}
			//System.out.print("\n");
			/*Graphics2D g2d = (Graphics2D)g;
			g2d.drawImage(g, op, x, y);*/
		}
		
	}
	@Override
	public double getZ(){
		return z;
	}
	
	/** Determine if the person is visible. (for GUI) */
	private boolean visible = true;
	public boolean isVisibleOnGUI(){
		return visible && Double.isNaN(getAnimationPosition());
	}
	/** Enable the person, returns true for success. */
	public boolean enable(){
		if(!visible && Double.isNaN(getAnimationPosition())){
			visible = true;
			visible_changed_timestamp = new GregorianCalendar().getTimeInMillis();
			return true;
		}
		return false;
	}
	/** Disable the person, returns true for success. */
	public boolean disable(){
		if(visible && Double.isNaN(getAnimationPosition())){
			visible = false;
			visible_changed_timestamp = new GregorianCalendar().getTimeInMillis();
			return true;
		}
		return false;
	}
	
	//unix timestamp im ms
	private long visible_changed_timestamp = 0;
	private static final int enable_animation_period = 400;
	private static final int disable_animation_period = 700;
	//Returning range: 0.0(Begin) ~ 1.0(End), or NaN for Not-moving.
	protected double getAnimationPosition(){
		long time = new GregorianCalendar().getTimeInMillis();
		double result = (time - visible_changed_timestamp);
		if(visible){
			result /= enable_animation_period;
		}else{
			result /= disable_animation_period;
		}
		if(result>=0 && result<1)return result;
		else return Double.NaN;
	}
	
	
	/** Determine if the mouse overlaps the rectangle sensor-zone of this object. */
	@Override
	public boolean hitTest(Point mouse) {
		if(!isVisibleOnGUI())return false;
		if(mouse==null)return false;
		if(mouse.x<tmp_img_x+tmp_img_w*0.2 || mouse.x>tmp_img_x+tmp_img_w*0.8){
			return false;
		}else if(mouse.y<tmp_img_y+tmp_img_h*0.03 || mouse.y>tmp_img_y+tmp_img_h*0.88){
			return false;
		}
		return true;
	}
	
	/** Person is not destroyable. */
	@Override
	public boolean isDestroyable() {
		return false;
	}
	@Override
	public double destroyStuff() {
		return 0;
	}
}
