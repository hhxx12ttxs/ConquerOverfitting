package engine.executives.managers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class SwappableClickableImagePanel extends JPanel {

	private static final long serialVersionUID = -8502544668656068234L;
	private BufferedImage on;
	private BufferedImage off;
	private boolean swap;
	private Image lastOriginalToDraw;
	private Image lastSwappedToDraw;
	private int lastSwappedWidth;
	private int lastSwappedHeight;
	private int lastOriginalWidth;
	private int lastOriginalHeight;
	
	public SwappableClickableImagePanel(String src, MouseListener listener) throws IOException {
		on = ImageIO.read(ClassLoader.getSystemResource(src));

		off = on;
		swap = true;
		
		generateTintSwapped(0.0, 1.0, 0.0);
		
		this.setPreferredSize(new Dimension(50,50));
		this.addMouseListener(listener);
		this.setBackground(Color.white);
	}
	
	@Override
	public void update(Graphics g){
		paint(g);
	}
	
	@Override
	public void paint(Graphics g){
		Image toDraw;
		
		g.setColor(Color.white);
		
		g.fillRect(0, 0, getWidth(), getHeight());

		if (swap) {
			if (lastSwappedWidth == getWidth() && lastSwappedHeight == getHeight()) {
				toDraw = lastSwappedToDraw;
			} else {
				toDraw = off;
				lastSwappedToDraw = toDraw;
				lastSwappedWidth = getWidth();
				lastSwappedHeight = getHeight();
			}
		} else {
			if (lastOriginalWidth == getWidth() && lastOriginalHeight == getHeight()) {
				toDraw = lastOriginalToDraw;
			} else {
				toDraw = on;
				lastOriginalToDraw = toDraw;
				lastOriginalWidth = getWidth();
				lastOriginalHeight = getHeight();
			}
		}
		g.drawImage(toDraw, (getWidth()-toDraw.getWidth(this))/2, (getHeight()-toDraw.getHeight(this))/2, this);
	}
	
	public void generateTintSwapped(double tr, double tg, double tb) {
		
		off = new BufferedImage(on.getWidth(), on.getHeight(),  BufferedImage.TYPE_INT_ARGB); 
		
		int[] rgbs = new int[off.getWidth() * off.getHeight()];
		
		on.getRGB(0, 0, on.getWidth(), on.getHeight(), rgbs, 0, on.getWidth());
		
		for (int i=0; i<rgbs.length; ++i) {
			
			int r = (rgbs[i] & 0x00FF0000) / (256*256);
			int g = (rgbs[i] & 0x0000FF00) / 256;
			int b = (rgbs[i] & 0x000000FF);
			
			r = Math.max(r-((int) (255*(1-tr))), 0);
			g = Math.max(g-((int) (255*(1-tg))), 0);
			b = Math.max(b-((int) (255*(1-tb))), 0);
			
			rgbs[i] = (rgbs[i] & 0xFF000000) + (256*256*r + 256*g + b);
		}
		
		off.setRGB(0, 0, on.getWidth(), on.getHeight(), rgbs, 0, on.getWidth());
	}

	
	public void relativeSwap() {
		swap = !swap;
		repaint();
	}
	
	public void setState(boolean state) {
		swap = state;
		repaint();
	}
	
	

}
