package segment7;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class SwappableImagePanel extends JPanel {

	private static final long serialVersionUID = -8502544668656068234L;
	private BufferedImage original;
	private BufferedImage swapped;
	private boolean swap;
	private Image lastOriginalToDraw;
	private Image lastSwappedToDraw;
	private int lastSwappedWidth;
	private int lastSwappedHeight;
	private int lastOriginalWidth;
	private int lastOriginalHeight;
	
	public SwappableImagePanel(String src) throws IOException {
		original = ImageIO.read(ClassLoader.getSystemResource(src));
		swapped = original;
		swap = false;
		this.setSize(original.getWidth(), original.getHeight());
	}
	
	@Override
	public void update(Graphics g){
		paint(g);
	}
	
	@Override
	public void paint(Graphics g){
		Image toDraw;
		if (swap) {
			if (lastSwappedWidth == getWidth() && lastSwappedHeight == getHeight()) {
				toDraw = lastSwappedToDraw;
			} else {
				toDraw = swapped.getScaledInstance(getWidth(), getHeight(), BufferedImage.SCALE_SMOOTH);
				lastSwappedToDraw = toDraw;
				lastSwappedWidth = getWidth();
				lastSwappedHeight = getHeight();
			}
		} else {
			if (lastOriginalWidth == getWidth() && lastOriginalHeight == getHeight()) {
				toDraw = lastOriginalToDraw;
			} else {
				toDraw = original.getScaledInstance(getWidth(), getHeight(), BufferedImage.SCALE_SMOOTH);
				lastOriginalToDraw = toDraw;
				lastOriginalWidth = getWidth();
				lastOriginalHeight = getHeight();
			}
		}
		g.drawImage(toDraw, 0, 0, this);
	}
	
	public void relativeSwap() {
		swap = !swap;
		repaint();
	}

	public void generateTintSwapped(double tr, double tg, double tb) {
		
		swapped = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_ARGB); 
		
		int[] rgbs = new int[swapped.getWidth() * swapped.getHeight()];
		
		original.getRGB(0, 0, original.getWidth(), original.getHeight(), rgbs, 0, original.getWidth());
		
		for (int i=0; i<rgbs.length; ++i) {
			
			int r = (rgbs[i] & 0x00FF0000) / (256*256);
			int g = (rgbs[i] & 0x0000FF00) / 256;
			int b = (rgbs[i] & 0x000000FF);
			
			r = Math.max(r-((int) (255*(1-tr))), 0);
			g = Math.max(g-((int) (255*(1-tg))), 0);
			b = Math.max(b-((int) (255*(1-tb))), 0);
			
			rgbs[i] = (rgbs[i] & 0xFF000000) + (256*256*r + 256*g + b);
		}
		
		swapped.setRGB(0, 0, original.getWidth(), original.getHeight(), rgbs, 0, original.getWidth());
	}
}

