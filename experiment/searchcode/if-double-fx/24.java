package view;



import game.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class Canvas extends Component implements ActionListener {
	private static final long serialVersionUID = -4485002365030385085L;
	private Dimension boundingRect;
	private Map<Class<?>, Painter> painters;
	private Map<String, BufferedImage> images;
	
	public Canvas(){
		super();
		boundingRect = new Dimension(600,600);

		//-----------------Loading images----------------
		images = new TreeMap<String, BufferedImage>();
		images.put("anteater_0",Window.loadImageFromFile("anteater_0"));
		images.put("anteater_1",Window.loadImageFromFile("anteater_1"));
		images.put("anteater_2",Window.loadImageFromFile("anteater_2"));
		images.put("anteater_3",Window.loadImageFromFile("anteater_3"));
		images.put("anteater_4",Window.loadImageFromFile("anteater_4"));
		images.put("anteater_5",Window.loadImageFromFile("anteater_5"));
		images.put("ant_0",Window.loadImageFromFile("ant_0"));
		images.put("ant_1",Window.loadImageFromFile("ant_1"));
		images.put("ant_2",Window.loadImageFromFile("ant_2"));
		images.put("ant_3",Window.loadImageFromFile("ant_3"));
		images.put("ant_4",Window.loadImageFromFile("ant_4"));
		images.put("ant_5",Window.loadImageFromFile("ant_5"));
		images.put("antfarm",Window.loadImageFromFile("antfarm"));
		images.put("antwatcher",Window.loadImageFromFile("antwatcher"));
		images.put("field",Window.loadImageFromFile("field"));
		images.put("food",Window.loadImageFromFile("food"));
		images.put("lake",Window.loadImageFromFile("lake"));
		images.put("poison",Window.loadImageFromFile("poison"));
		images.put("stone",Window.loadImageFromFile("stone"));
		images.put("antsmell",Window.loadImageFromFile("antsmell"));

		//---------------Mapping painters to classes---------
		painters = new HashMap<Class<?>, Painter>();
		painters.put(Ant.class, new AntPainter());
		painters.put(AntEater.class, new AntEaterPainter());
		painters.put(EmptyField.class, new EmptyFieldPainter());
		painters.put(Anthill.class, new AnthillPainter());
		painters.put(Antwatcher.class, new AntwatcherPainter());
		painters.put(Obstacle.class, new ObstaclePainter());
		painters.put(Food.class, new FoodPainter());
		painters.put(Stone.class, new StonePainter());
		painters.put(Poison.class, new PoisonPainter());
		painters.put(AntSmell.class, new AntSmellPainter());
		this.addMouseListener(new MouseListener() {
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mousePressed(MouseEvent e) {
				Point canvas = e.getComponent().getLocationOnScreen();
				Point click = e.getLocationOnScreen();
				double clickX = click.x - canvas.x;
				double clickY = click.y - canvas.y;
				List<Field> fl = Window.GetController().GetEngine().GetMap();
				Field selected = null;
				double minD = Float.MAX_VALUE;
				for(Field f : fl)
				{
					Dimension d = calculatePosition(f.GetId());
					double fx = d.width+23;
					double fy = d.height+20;
					double dist = Math.sqrt((fx-clickX)*(fx-clickX)+(fy-clickY)*(fy-clickY));
					if(dist < minD) { minD = dist;	selected = f; }
				}
				if(e.getButton() == MouseEvent.BUTTON1){
					
					Window.GetController().sprayPoison(selected);
					Window.getbutton1().setText("Poison: "+Integer.toString(Window.GetController().GetEngine().getpoison()));	//Spray figyelok bovitve
				}
				
				else if(e.getButton() == MouseEvent.BUTTON3){
					
					Window.GetController().sprayDeo(selected);
					Window.getbutton2().setText("Deo: "+Integer.toString(Window.GetController().GetEngine().getdeo()));
				}
				
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		setSize(boundingRect);
		this.setPreferredSize(boundingRect);
		validate();
	}
	Map<Class<?>, Painter> GetPainters() { return painters; }
	Map<String, BufferedImage> GetImages() { return images; }
	
	public void mapPaint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		List<Field> fl = Window.GetController().GetEngine().GetMap();
		
		for(int i = 0; i < fl.size(); i++)
		{
			 Dimension pos = calculatePosition(fl.get(i).GetId());
             Graphics g3 = g2.create();
             
             g3.translate(pos.width, pos.height);
             painters.get(fl.get(i).getClass()).paint(g3,fl.get(i));
             if(fl.get(i).GetFood() != null) {
            	 painters.get(fl.get(i).GetFood().getClass()).paint(g3,fl.get(i).GetFood());
             }
             else if(fl.get(i).GetStone() != null) {
            	 painters.get(fl.get(i).GetStone().getClass()).paint(g3,fl.get(i).GetStone());
             }
             else if(fl.get(i).GetPoison() != null) {
            	 painters.get(fl.get(i).GetPoison().getClass()).paint(g3,fl.get(i).GetPoison());
             }
             else if(fl.get(i).GetAntSmell() != null) {
            	 painters.get(fl.get(i).GetAntSmell().getClass()).paint(g3,fl.get(i).GetAntSmell());
             }
		}
	}
	
	@Override
	public void paint(Graphics g) {
		mapPaint(g);
		//Common
		Graphics2D g2 = (Graphics2D) g;
		//Game Item Specific
		synchronized(Window.GetController()){
			if(Window.GetController() == null)
				return;
			else{
				if(Window.GetController().GetEngine().IsRunning() == false)
					{
						g2.drawString("Click 'New Game' on the menubar to start a new game.",0,boundingRect.height/2);
					}
				else {
					List<IMovable> fl = Window.GetController().GetEngine().GetMovers();
					for(int i = 0; i < fl.size(); i++)
					{						
						 Dimension pos = calculatePosition(fl.get(i).GetId());
	                     Graphics g3 = g2.create();
	                     g3.translate(pos.width, pos.height);
	                     painters.get(fl.get(i).getClass()).paint(g3,fl.get(i));	                   	                     
					}
				}
			}
		}
	}
	private Dimension calculatePosition(long placeId) {
		 long fx = placeId % Window.GetController().GetEngine().GetMapWidth();
		 long fy = placeId / Window.GetController().GetEngine().GetMapWidth();
		 Dimension res = null;
		 if(fy % 2 == 0){
			 if(fx %2 == 0)
			 {
		         res = new Dimension((int)(fx/2)*69,(int)(fy)*40);
			 }
			 else{
				 res = new Dimension((int)(34+(fx/2)*69),(int)(20+(fy)*40));
			 }
		 }
		 else{
			 if(fx %2 == 1)
			 {
		         res = new Dimension((int)(34+(fx/2)*69),(int)(20+((fy*40))));
			 }
			 else{
				 res = new Dimension((int)(fx/2)*69,(int)((fy)*40)); 
			 }
		 }
		 return res;
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
}

