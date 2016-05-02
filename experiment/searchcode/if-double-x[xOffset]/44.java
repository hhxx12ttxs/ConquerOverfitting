package cncHost;

import javax.swing.JFrame;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.BasicStroke;  
import java.awt.GradientPaint;
import java.awt.TexturePaint; 
import java.awt.Rectangle;
import java.awt.Graphics2D;           
import java.awt.geom.Ellipse2D;       
import java.awt.geom.Rectangle2D;     
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.Arc2D;           
import java.awt.geom.Line2D; 
import java.awt.geom.Ellipse2D; 
import java.awt.image.BufferedImage;  
import javax.swing.JPanel;

public class Visualize{
	public void visualizePaths(Paths paths){
		Configuration c = Configuration.getInstance();
		JFrame frame = new JFrame( "Visualize Paths" );
		VisualizeJPanel panel = new VisualizeJPanel(paths, "paths");
		frame.add( panel );
		frame.setSize( c.width, c.height );
		frame.setVisible( true );
	}
	public void visualizeSuperPoints(Paths paths){
		Configuration c = Configuration.getInstance();
		JFrame frame = new JFrame( "Visualize Points" );
		VisualizeJPanel panel = new VisualizeJPanel(paths, "superPoints");
		frame.add( panel );
		frame.setSize( c.width, c.height );
		frame.setVisible( true );
	}
}
class VisualizeJPanel extends JPanel {
	Paths paths;
	Configuration c = Configuration.getInstance();
	double t, xOffset, yOffset;
	String type;
	public VisualizeJPanel(Paths dPaths, String dType){
		this.paths = dPaths;
		this.type = dType;
		this.t =  Math.min(
				(c.width-(c.buffer*2))/(this.paths.maxX - this.paths.minX), 
				(c.height-(c.buffer*2))/(this.paths.maxY - this.paths.minY));
		xOffset = 0 - this.paths.minX;
		yOffset = 0 - this.paths.minY;
	}
	public void paintComponent( Graphics g ){
		super.paintComponent( g );
		Graphics2D g2d = ( Graphics2D ) g;
                                
		
		if(this.type.equals("paths")){
			g2d.setStroke( new BasicStroke( 2.0f ) );
			g2d.setPaint( Color.GREEN );
			
			for(int i=0;i<this.paths.paths.size();i++){
				for(int j = 1;j<this.paths.paths.get(i).points.size();j++){
					if(j%2 == 0){
						g2d.setPaint( Color.GREEN );
					}	
					if(j%2 == 1){
						g2d.setPaint( Color.BLUE );
					}
					g2d.draw( new Line2D.Double( 
							this.paths.paths.get(i).points.get(j-1).x*t + xOffset*t + c.buffer, 
							(t*this.paths.maxY) 
								- (this.paths.paths.get(i).points.get(j-1).y*t + yOffset*t) 
								+ c.buffer, 
							this.paths.paths.get(i).points.get(j).x*t + xOffset*t + c.buffer, 
							(t*this.paths.maxY) 
								- (this.paths.paths.get(i).points.get(j).y*t + yOffset*t) 
								+ c.buffer));
				}	
			}
		}
		else if(this.type.equals("superPoints")){
			
			g2d.setStroke( new BasicStroke( 2.5f ) );
			g2d.setPaint( Color.GREEN ); 

			int i,j;

			for(i=0;i<this.paths.paths.size();i++){
				for(j = 0;j<this.paths.paths.get(i).points.size();j++){
					g2d.draw( new Line2D.Double( 
							this.paths.paths.get(i).points.get(j).x*t + xOffset*t + c.buffer, 
							(t*this.paths.maxY)
								- (this.paths.paths.get(i).points.get(j).y*t + yOffset*t)
								+ c.buffer, 
							this.paths.paths.get(i).points.get(j).x*t + xOffset*t + c.buffer, 
							(t*this.paths.maxY)
								- (this.paths.paths.get(i).points.get(j).y*t + yOffset*t)
								+ c.buffer) );
				}	
			}

			g2d.setStroke( new BasicStroke( 1.5f ) );
			g2d.setPaint( Color.RED ); 

			for(i=0; i<this.paths.superPoints.size();i++){
				Point s = this.paths.superPoints.get(i).primaryPoint;
				g2d.draw( new Ellipse2D.Double( 
							s.x*t + xOffset*t + c.buffer - (c.superPointDrawingRadius), 
							(t*this.paths.maxY)
								- s.y*t + yOffset*t
								+ c.buffer
								- (c.superPointDrawingRadius*2), 
							c.superPointDrawingRadius*2, 
							c.superPointDrawingRadius*2 ));
			}

			g2d.setPaint( Color.BLUE ); 

			for(i=1; i<this.paths.superPoints.size();i++){
				Point s1 = this.paths.superPoints.get(i-1).primaryPoint;
				Point s2 = this.paths.superPoints.get(i).primaryPoint;
				g2d.draw( new Line2D.Double( 
							s1.x*t + xOffset*t + c.buffer, 
							(t*this.paths.maxY)
								- s1.y*t + yOffset*t
								+ c.buffer, 
							s2.x*t + xOffset*t + c.buffer, 
							(t*this.paths.maxY)
								- s2.y*t + yOffset*t
								+ c.buffer) );
			}

		}
	}
}


