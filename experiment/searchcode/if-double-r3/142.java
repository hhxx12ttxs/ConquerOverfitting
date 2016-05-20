import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

class box
{
	public int x;
	public int height;
	public Color color;
	
	public String toString()
	{
		return "X:"+x+" Height:"+height+" "+color;
	}
}


public class GUI
{	
	public final int LEFT =1;
	public final int RIGHT =0;
	
	JFrame frame;
	image im;
	oneD oned;
	JLabel label;
	int eye=150;
	int direction = LEFT;	
	LinkedList<box> seg_box = new LinkedList<box>();
	BSPTree bsp=null;
	

	public GUI()
	{
		frame = new JFrame("BSPDemo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.setResizable(false);
		im = new image();
		oned = new oneD();
		JPanel buttonPane = new JPanel();
		
		JButton reverse = new JButton("Reverse Eye");
		JButton addRandom = new JButton("Add Random Slab");
		label = new JLabel("Now looking: LEFT");
		buttonPane.add(reverse);
		buttonPane.add(addRandom);
		buttonPane.add(label);
		frame.getContentPane().add(im,BorderLayout.WEST);
		frame.getContentPane().add(buttonPane,BorderLayout.CENTER);
		frame.getContentPane().add(oned,BorderLayout.EAST);
		frame.setSize(800,300);
		
		bsp=new BSPTree(this,eye);
		
		reverse.addActionListener(new 
				ActionListener()
				{
					public void actionPerformed(ActionEvent a)
					{
						toggleDirection();
						//Make change to viz
					}
				});
		addRandom.addActionListener(new 
				ActionListener() {				
					@Override
					public void actionPerformed(ActionEvent e) {
						Random r1 = new Random();
						Random r2 = new Random();
						Random r3 = new Random();
						float r = r2.nextFloat();
						float g = r2.nextFloat();
						float b = r2.nextFloat();						
						box bx = new box();
						bx.height = r1.nextInt(5)+1;
						bx.color = new Color(r,g,b);
						
						boolean param = true;
						while(param )
						{
							param=false;
							bx.x = r3.nextInt(300);
							Iterator<box> temp_b = seg_box.iterator();
							while(temp_b.hasNext())
							{
								box tt = temp_b.next();
								if(tt.x == bx.x){
									param = true;
									break;
								}
							}
						}
						seg_box.add(bx);
						//renderPoint(bx);
						//System.out.println("Done");
						oned.repaint();
						bsp.build(seg_box);
						bsp.renderTree();
					}
					
		});
		
		
		frame.setVisible(true);
	}
	public void imageClear()
	{
		im.clear();
	}
	public void renderPoint(box temp)
	{
		System.out.println(temp);
		if(direction == LEFT && temp.x <eye)
		{
			im.paintBox(temp);
		}
		else if(direction == RIGHT && temp.x > eye)
			im.paintBox(temp);
	}
	
	private void toggleDirection()
	{
		direction = 1- direction;
		String text =(direction == 1? "LEFT":"RIGHT");
		label.setText("Now looking at: "+text);
		bsp.build(seg_box);
		bsp.renderTree();
	}
	
	private void setEyePosition(int x)
	{
		eye = x;
		bsp.obj_x = x;
	}
	
	public static void main(String[] args)
	{
		new GUI();
	}
	
	class image extends JPanel
	{
		BufferedImage buff;
		int centerx=150;
		int centery=150;
		public image()
		{
			setPreferredSize(new Dimension(300,300));
			buff=null;
		}
		public void paintComponent(Graphics g)
		{
			Graphics2D g2 = (Graphics2D)g;
			
			if(buff == null)
			{
				int h = getHeight();
				int w = getWidth();			
				buff = (BufferedImage)createImage(w, h);
				Graphics2D gc = buff.createGraphics();
				gc.setColor(Color.WHITE);
				gc.fillRect(0, 0, w, h);			
				gc.drawString("The view will appear here",10, 10);
			}			
			g2.drawImage(buff, null, 0, 0);
			
		}
		public void clear()
		{
			buff=null;
			int h = getHeight();
			int w = getWidth();			
			buff = (BufferedImage)createImage(w, h);
			Graphics2D gc = buff.createGraphics();
			gc.setColor(Color.WHITE);
			gc.fillRect(0, 0, w, h);
			gc.drawString("The view will appear here",10, 15);
		}
		public void paintBox(box b)
		{
			Graphics2D gc = buff.createGraphics();
			Color tc = gc.getColor();
			gc.setColor(b.color);
			gc.fillRect(centerx-(b.height*10),centery-(b.height*10),b.height*20,b.height*20);
			gc.setColor(tc);
			repaint();
		}
	}
	class oneD extends JPanel implements MouseListener
	{
		
		BufferedImage buff;
		int prev=-1;
		boolean moving = false;
		public oneD()
		{
			setPreferredSize(new Dimension(300,300));
			buff=null;
			addMouseListener(this);
		}
		
		public void paintComponent(Graphics g)
		{
			Graphics2D g2 = (Graphics2D)g;
			
			if(buff == null)
			{
				int h = getHeight();
				int w = getWidth();			
				buff = (BufferedImage)createImage(w, h);
				Graphics2D gc = buff.createGraphics();
				gc.setColor(Color.WHITE);
				gc.fillRect(0, 0, w, h);
				gc.setColor(Color.black);
				gc.drawLine(0, 150, 299, 150);
				gc.drawString("Drag the red dot to change eye position", 10, 10);
			}
			Graphics2D gc = buff.createGraphics();
			if(moving == true)
				gc.setColor(Color.GREEN);
			else
			{
				if(prev != -1)
				{
					gc.setColor(Color.WHITE);
					gc.fillOval(prev-4,150-4,8,8);
					prev=-1;
				}
				gc.setColor(Color.black);
				gc.drawLine(0, 150, 299, 150);
				drawShapes(gc);
				gc.setColor(Color.RED);
			}
			
			gc.fillOval(eye-4,150-4,8,8);
			g2.drawImage(buff, null, 0, 0);
			
		}
		private void drawShapes(Graphics2D g)
		{
			Color c = g.getColor();
			BasicStroke base =(BasicStroke) g.getStroke();
			g.setStroke(new BasicStroke(4));
			Iterator<box> i = seg_box.iterator();
			while(i.hasNext())
			{
				box temp = i.next();
				//System.out.println(temp.x);
				g.setColor(temp.color);
				g.drawLine(temp.x, 150-(temp.height*5+10), temp.x, 150+(temp.height*5+10));
			}
			g.setColor(c);
			g.setStroke(base);
		}
		
		@Override
		public void mouseClicked(MouseEvent arg0) {
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {}
		@Override
		public void mouseExited(MouseEvent arg0) {}
		@Override
		public void mousePressed(MouseEvent e) {
			
			if(getDistance(e.getPoint(),new Point(eye,150)) < 8)
				moving = true;
			repaint();
		}
		@Override
		public void mouseReleased(MouseEvent e) {
			if(moving == true)
			{
				prev = eye;
				setEyePosition(e.getPoint().x);
				moving = false;
			}
			repaint();
			bsp.build(seg_box);
			bsp.renderTree();
		}
		
		private double getDistance(Point a,Point b)
		{
			double sqr_x = a.x - b.x;
			sqr_x *= sqr_x;
			double sqr_y = a.y - b.y;
			sqr_y *= sqr_y;
			
			return Math.sqrt(sqr_x+sqr_y);
		}
		
	}
	
	
	
}
