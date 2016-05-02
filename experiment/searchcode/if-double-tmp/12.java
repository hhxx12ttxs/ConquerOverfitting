package bbb.common.managers.movement;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

import robocode.*;

import bbb.common.*;
import bbb.common.data.*;
import bbb.common.managers.*;
import bbb.common.util.*;

public class AntiGravity extends Manager {

  /*private final String WALL_LEFT = "Wall: Left";
	private final String WALL_RIGHT = "Wall: Right";
	private final String WALL_UP = "Wall: Up";  
	private final String WALL_BOTTOM = "Wall: Bottom";  
	private final String MOD_UP = " Up";
	private final String MOD_MIDDLE = " Middle";
	private final String MOD_DOWN = " Down";
	private final String MOD_RIGHT = " Right";
	private final String MOD_LEFT = " Left";
	*/
  protected Hashtable gPoints;
  Point2D total;

  private class GPoint extends Point2D.Double {
	protected Shape zone;
	protected double zoneMagnitude;
	protected double magnitude;

	public GPoint(Point2D p, double mag) {
	  super(p.getX(),p.getY());
	  magnitude = mag;
	  zone=null;
	  zoneMagnitude=0;
	}


	public GPoint(Point2D p, double mag, double zoneMagnitude, Shape zone) {
	  this(p,mag);
	  this.zoneMagnitude=zoneMagnitude;
	  this.zone = zone;
	  if (zone != null) {
		if (!zone.contains(this)) {
		  Logger.error("zone doesn't contain p");
		}
	  }
	}
	public double getMag(){
	  return magnitude;
	}

	public Point2D get(Point2D p) {
	  double absoluteMagnitude = 0;
	  double d = distance(p);

	  if (zone != null) 
		if (zone.contains(p)) 
		  absoluteMagnitude = zoneMagnitude;
		else
		  absoluteMagnitude=magnitude;
	  else 
		absoluteMagnitude = magnitude;

	  if(d!=0)
		absoluteMagnitude /= (d*d);
	  else
		absoluteMagnitude*=java.lang.Double.POSITIVE_INFINITY;
	  return new Point2D.Double((x - p.getX()) * absoluteMagnitude, (y - p.getY()) * absoluteMagnitude);
	}
  }

  public AntiGravity(AbstractBot me, BotRepository b) {
	super("AntiGravity", me,b);
	gPoints = new Hashtable();
	int h = (int)getMe().getBattleFieldHeight();
	int w = (int)getMe().getBattleFieldWidth();

	/*		add(WALL_UP + MOD_LEFT, new Point2D.Double(0, h-1), -1,-50, new Rectangle(0, h-100, w, 100));    
			add(WALL_UP + MOD_MIDDLE, new Point2D.Double(w / 2, h-1), -1,-50, new Rectangle(0, h-100, w, 100));    
			add(WALL_UP + MOD_RIGHT, new Point2D.Double(w-1, h-1), -1, -50,new Rectangle(0, h-100, w, 100));    

			add(WALL_BOTTOM + MOD_LEFT, new Point2D.Double(0, 0), -1, -50,new Rectangle(0, 0, w, 100));    
			add(WALL_BOTTOM + MOD_MIDDLE, new Point2D.Double(w / 2, 0),-1, -50, new Rectangle(0, 0, w, 100));
			add(WALL_BOTTOM + MOD_RIGHT, new Point2D.Double(w-1, 0), -1, -50, new Rectangle(0, 0, w, 100));

			add(WALL_LEFT + MOD_UP, new Point2D.Double(0, h-1), -1, -50, new Rectangle(0, 0, 100, h));    
			add(WALL_LEFT + MOD_MIDDLE, new Point2D.Double(0 , h/2),-1, -50, new Rectangle(0, 0, 100, h));
			add(WALL_LEFT + MOD_DOWN, new Point2D.Double(0, 0), -1, -50, new Rectangle(0, 0, 100, h));

			add(WALL_RIGHT + MOD_UP, new Point2D.Double(w-1,h-1), -1, -50, new Rectangle(w-100, 0, 100, h));    
			add(WALL_RIGHT + MOD_MIDDLE, new Point2D.Double(w-1,h/2),-1, -50, new Rectangle(w-100, 0, 100, h));
			add(WALL_RIGHT + MOD_DOWN, new Point2D.Double( w-1,0), -1, -50, new Rectangle(w-100,0, 100, h));
			*/
	double loop;
	/*   for(loop=0;loop<w;loop++)	{
		 add(WALL_UP +loop, new Point2D.Double(loop,h), -1);    
		 add(WALL_BOTTOM +loop, new Point2D.Double(loop,0), -1);    

	}
	for(loop=0;loop<h;loop++)	{
	add(WALL_LEFT +loop, new Point2D.Double(0,loop), -1);    
	add(WALL_RIGHT +loop, new Point2D.Double(w,loop), -1);    

	}
	*/  

	/*		add(WALL_UP + MOD_MIDDLE, new Point2D.Double(w / 2, h-1), -1);    
			add(WALL_UP + MOD_RIGHT, new Point2D.Double(w-1, h-1), -1);    

			add(WALL_BOTTOM + MOD_LEFT, new Point2D.Double(0, 0), -1);    
			add(WALL_BOTTOM + MOD_MIDDLE, new Point2D.Double(w / 2, 0),-1);
			add(WALL_BOTTOM + MOD_RIGHT, new Point2D.Double(w-1, 0), -1);
			*/ /*
				  add(WALL_LEFT + MOD_UP, new Point2D.Double(0, h-1), -1);    
				  add(WALL_LEFT + MOD_MIDDLE, new Point2D.Double(0 , h/2),-1);
				  add(WALL_LEFT + MOD_DOWN, new Point2D.Double(0, 0), -1);

				  add(WALL_RIGHT + MOD_UP, new Point2D.Double(w-1,h-1), -1);    
				  add(WALL_RIGHT + MOD_MIDDLE, new Point2D.Double(w-1,h/2),-1);
				  add(WALL_RIGHT + MOD_DOWN, new Point2D.Double( w-1,0), -1);
				  add(MOD_MIDDLE, new Point2D.Double(w/2,h/2),-1);*/

	/*	

		add(WALL_UP + MOD_LEFT, new Point2D.Double(0, h-1),-10);
		add(WALL_UP + MOD_MIDDLE, new Point2D.Double(w / 2, h-1),-10);
		add(WALL_UP + MOD_RIGHT, new Point2D.Double(w-1, h-1), -10);

		add(WALL_BOTTOM + MOD_LEFT, new Point2D.Double(0, 0), -10);
		add(WALL_BOTTOM + MOD_MIDDLE, new Point2D.Double(w / 2, 0),-10);
		add(WALL_BOTTOM + MOD_RIGHT, new Point2D.Double(w-1, 0), -10);

		add(WALL_LEFT + MOD_UP, new Point2D.Double(0, h-1), -10);
		add(WALL_LEFT + MOD_MIDDLE, new Point2D.Double(0 , h/2),-10);
		add(WALL_LEFT + MOD_DOWN, new Point2D.Double(0, 0),-10);

		add(WALL_RIGHT + MOD_UP, new Point2D.Double(w-1,h-1),-10);  
		add(WALL_RIGHT + MOD_MIDDLE, new Point2D.Double(w-1,h/2), -10);
		add(WALL_RIGHT + MOD_DOWN, new Point2D.Double( w-1,0),  -10);
		*/

	//		add(MOD_MIDDLE, new Point2D.Double(w/2,h/2),10,-1,new Ellipse2D.Double(w/2-w/8,h/2-h/8,w/4,h/4));
	//  add("queroIr",new Point2D.Double(100,100),50);
  }

  public void add(String name, Point2D p, double mag) { 
	gPoints.put(name, new GPoint(p, mag));
  }

  public void add(String name, Point2D p, double mag, double zoneMagnitude,Shape zone) { 
	gPoints.put(name, new GPoint(p, mag,zoneMagnitude, zone));  
  }

  int dir=1;

  public void run() {
	total=new Point2D.Double();
	Point2D tmp;
	Point2D pos=new Point2D.Double(getMe().getX(),me.getY());
	Enumeration e;

	e=gPoints.elements();
	while(e.hasMoreElements()){
	  tmp=((GPoint)e.nextElement()).get(pos);
	  total.setLocation(total.getX()+tmp.getX(),total.getY()+tmp.getY());
	}
	if(total.getX()!=0||total.getY()!=0){
	  double turn=Trig.normalRelativeAngle(Trig.getAngle(total,getMe().getHeadingRadians(),true));
	  Logger.log(""+turn);
	  if(Math.abs(turn)>Math.PI/2){
		Logger.log("--------");
		Logger.log(""+turn);
		dir=-1;
		turn=Trig.normalRelativeAngle(-turn+Math.PI);
		// (turn>0)?Math.PI/4:-Math.PI/4;
		Logger.log(""+turn);
	  }else dir=1;
	  getMe().setTurnRightRadians(turn);

	}
	getMe().setAhead(100000*dir);

  }



  public void onScannedRobot(ScannedRobotEvent e){}


  public void onPaint(java.awt.Graphics2D g) {
	Enumeration e=gPoints.elements();
	GPoint tmp;
	g.setColor(Color.red);
	/*while(e.hasMoreElements()){
	  tmp=((Point2D)e.nextElement());
	  tmp=(Point2D)tmp.clone();
	  tmp.setLocation(tmp.getX(),-tmp.getY());
	  g.draw(new Rectangle((Point)tmp,new Dimension(10,10)));  
	  }*/

	g.setColor(Color.red);
	g.translate(0,getMe().getBattleFieldHeight());
	g.draw(new Line2D.Double(getMe().getX(),-me.getY(),me.getX()+(total.getX()*300),-(me.getY()+(total.getY()*300))));
	while(e.hasMoreElements()){
	  tmp=((GPoint)e.nextElement());
	  tmp=(GPoint)tmp.clone();
	  tmp.setLocation(tmp.getX(),-tmp.getY());
	  if(tmp.getMag()>0){
		g.setColor(Color.green);
		g.draw(new Rectangle2D.Double(tmp.getX()-25*tmp.getMag(),tmp.getY()-25*tmp.getMag(),50*tmp.getMag(),50*tmp.getMag()));  
	  }
	  else{
		g.setColor(Color.red);
		g.draw(new Rectangle2D.Double(tmp.getX()-25*-tmp.getMag(),tmp.getY()-25*-tmp.getMag(),50*-tmp.getMag(),50*-tmp.getMag()));  
	  }
	}

	g.translate(0,-getMe().getBattleFieldHeight());
  }                                                                                                              

}

