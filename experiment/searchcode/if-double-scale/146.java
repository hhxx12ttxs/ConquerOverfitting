package aiingames.samplesim.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

import aiingames.samplesim.Config;
import aiingames.samplesim.agents.Agent;
import aiingames.samplesim.simulation.Environment;
import aiingames.samplesim.spatial.Sensor;
import aiingames.samplesim.spatial.SensorPoint;

public class Gui {
	private JFrame f;
	private DrawArea area;

	private long lastUpdate = 0;
	private double scale;
	private double minX;
	private double minY;
	private double maxX;
	private double maxY;

	public Gui() {

		this.f = new JFrame("AI - Ăbung 1");
		this.f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.area = new DrawArea();
		this.f.add(area);
		this.f.setSize(250,250);
		this.f.setVisible(true);
	}

	public void setEnvironmentSize(double minX, double minY, double maxX, double maxY) {
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
		resize();
	}

	private void resize() {
		Dimension screenSize = new Dimension(Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT);
		double width = this.maxX - this.minX;
		double height = this.maxY - this.minY;

		this.scale = screenSize.getHeight() / height;
		if (width * scale > screenSize.width) {
			scale = screenSize.width / width;
		}

		this.f.setSize((int)(width*scale), (int)(height * scale));
		this.area.setTransformation(scale, minX, minY);

	}

	public void update(Collection<Agent> agents,Environment e) {
		for (Agent agent : agents) {
			this.area.updateAgent(agent,e);
		}
        long current = System.currentTimeMillis();
        long diff = current - this.lastUpdate;
        long wait = (long) (Config.getSimStepSize() * 1000) -diff;
        if (wait > 0) {
                try {
                        Thread.sleep(wait);
                } catch (InterruptedException ee) {
                       ee.printStackTrace();
                }
        }
        this.lastUpdate = System.currentTimeMillis();
        this.area.repaint();
	}


	private static class DrawArea extends JPanel {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private Map<Agent,DrawAgent> agents = new HashMap<Agent,DrawAgent>();
		private double scale;
		private double minX;
		private double minY;
		
		public DrawArea(){
			setBorder(BorderFactory.createLineBorder(Color.black));
		}

		public void setTransformation(double scale, double minX, double minY) {
			this.scale = scale;
			this.minX = minX;
			this.minY = minY;
		}
		
		public void updateAgent(Agent agent, Environment e) {

			DrawAgent da = this.agents.get(agent);
			if (da == null) {
				da = new DrawAgent(Color.BLUE,agent);
				da.setRadius((int)(0.5+.5*this.scale));
				this.agents.put(agent, da);
			}
            int x = (int) (0.5+(e.getAgentLocation(agent.getId()).getX() - this.minX) * this.scale);
            int y = (int) (0.5+(e.getAgentLocation(agent.getId()).getY() - this.minY) * this.scale);
            da.setLocation(x,y);
            
            double norm = 2 *Math.sqrt(Math.pow(e.getVx(agent.getId()), 2)+ Math.pow(e.getVy(agent.getId()), 2));
            da.setDirection((int)(e.getVx(agent.getId())*this.scale/norm+0.5),(int) (e.getVy(agent.getId())*this.scale/norm+0.5));
            
            final Object obj = new Object(); 
            
            synchronized ( obj ) 
            { 
             // System.out.println( Thread.holdsLock(obj) );   // true 
              agent.setDirection( (int)(e.getVx(agent.getId())*this.scale/norm+0.5),(int) (e.getVy(agent.getId())*this.scale/norm+0.5)  );
              agent.setLocation(x, y);
            }
            
          
		}
		
        @Override
        public void paint(Graphics arg0) {
                super.paint(arg0);

                for (DrawAgent agent : this.agents.values()) {
                        agent.paintAgent(arg0);
                }
        }


	}

	private static class DrawAgent {
		private int radius = 0;
		private Color color;
		private int y;
		private int x;
		private int dx;
		private int dy;
        
        //
        private Agent agent;
        //

		public DrawAgent(Color color,Agent agent) {
			this.color = color;
            this.agent = agent;
		}


		public void paintAgent(Graphics g)
		{
            agent.paint( g, this.radius, this.x, this.y, this.dx, this.dy );
            
		}




		public void setRadius(int d) {
			this.radius = d;
		}


		public void setLocation(int x, int y) {
			this.x = x;
			this.y = y;

		}

		public void setDirection(int dx, int dy) {
			this.dx = dx;
			this.dy = dy;
		}
	}

}

