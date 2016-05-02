package Old.C2;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JPanel;

import Old.C2.collection.Tilemap;
import Old.C2.model.Tile;

/**
 * TileGrid is a visual component which displays a tilemap and also provides
 * required actions to edit it.
 * @author Jari Saaranen
 *
 */
public class TileGrid extends JPanel implements MouseListener, 
		MouseMotionListener, KeyListener, MouseWheelListener {
	private static final long serialVersionUID = 1L;
	
	private byte currentLayer;
	private int gridSize;
	private Point offset;
	private Point mouse;
	public Tile activeTile;
	
	public Tilemap<Tile> tilemap;
	
	private boolean left, right, middle;
	
	//position where the dragging of the mouse starts
	private Point dragStart;
	
	private boolean showGrid;
	private boolean showOnlyCurrentLayer;
	private boolean showHud;
	
	/**
	 * default constructor
	 */
	public TileGrid() {
		gridSize = 48;
		offset = new Point();
		dragStart = new Point();
		mouse = new Point();
		
		this.showGrid = true;
		this.showOnlyCurrentLayer = false;
		this.showHud = true;
		
		tilemap = new Tilemap<Tile>();
		
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addKeyListener(this);
		this.addMouseWheelListener(this);
		
		this.setBackground(new Color(0.9f, 0.9f, 0.9f));
	}
	
	public TileGrid(int gridSize) {
		this();
		this.gridSize = gridSize;
	}

	public TileGrid(int gridSize, Tile activeTile) {
		this(gridSize);
		this.activeTile = activeTile;
	}

	public void paintComponent(Graphics g) {
		//call parents method first
		super.paintComponent(g);
		
		//start using Graphics2D
		Graphics2D g2 = (Graphics2D)g;
		
		this.drawTiles(g2);
		
		//draw the overlaying grid
		this.drawGrid(g2);
		
		if(this.showHud)
			this.drawOverlay(g2);
	}
	
	private void drawOverlay(Graphics2D g) {		
		int baseY = 15;
		String[] layers = {"background", "background overlay", "middle", "middle overlay", "overlay", "collision"};

		for(int i = 5; i >= 0; i--) {
			g.setColor(Color.black);
			if(this.showOnlyCurrentLayer)
				g.setColor(Color.gray);
			
			if((byte)i == this.currentLayer)
				g.setColor(Color.red);
			
			g.drawString(layers[i], 15 , baseY + ((5-i)*15));
		}
		
	}

	/**
	 * Draw all tiles from collection to screen
	 * @param g Graphics2D
	 */
	private void drawTiles(Graphics2D g) {
		
		for(byte layer = 0; layer < 6; layer++) {
			if(this.showOnlyCurrentLayer && layer != this.currentLayer)
				continue;
			
			for(Tile t : tilemap) {
				if(t.z != layer) continue;
				
				if(t.image == null) {
					g.setColor(new Color(t.color, t.color, t.color));
					g.fillRect(
							t.x + this.offset.x, 
							t.y + this.offset.y, 
							C2Tools.gridSize, 
							C2Tools.gridSize);
				}
				else
					g.drawImage(t.image, t.x + this.offset.x, t.y + this.offset.y, null);
			}
		}
	}

	public void drawGrid(Graphics2D g) {
		//color for grid lines
		g.setColor(Color.black);
		
		//stroketype for grid lines
		float[] dash1 = { 2f, 0f, 2f };
		BasicStroke bs1 = new BasicStroke(1, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND, 1.0f, dash1, 2f );

		//activate just created stoketype
		g.setStroke(bs1);
		
		//temporary point to tell the offset in gridlevel
		Point tempOffset = this.calculateGridOffset();
		
		if(this.showGrid) {
			//vertical lines
			for(int y = 0; y < this.getHeight(); y++) {
				if(y % this.gridSize == 0)
					g.drawLine(0, y + tempOffset.y, this.getWidth(), y + tempOffset.y);
			}
			
			//horizontal lines
			for(int x = 0; x < this.getWidth(); x++) {
				if(x % this.gridSize == 0)
					g.drawLine(x + tempOffset.x, 0, x + tempOffset.x, this.getHeight());
			}
		}
		
		//display the origin. this may be removed later
		g.drawString("origin", offset.x, offset.y);
		
		//draw current grid cursor location
		g.setColor(Color.red);
		g.drawRect(mouse.x + tempOffset.x, mouse.y + tempOffset.y, gridSize, gridSize);
		
		//set drawing to half-transparent and draw current user-selected tile
		//to mouse position
		int rule = AlphaComposite.SRC_OVER;
        Composite comp = AlphaComposite.getInstance(rule , 0.5f);
        g.setComposite(comp);
        
        g.drawImage(
        		this.activeTile.image, 
        		mouse.x + tempOffset.x, 
        		mouse.y + tempOffset.y, 
        		null);
        
        g.setComposite(AlphaComposite.SrcOver);
	}

	/**
	 * Force offset to specific point
	 * 
	 * Used to set origin to the center of the window
	 * @param point
	 */
	public void setOffset(Point point) {
		this.offset = point;
	}
	
	/**
	 * Calculates gridbased offset
	 * @return Point
	 */
	public Point calculateGridOffset() {
		Point tempOffset = (Point)this.offset.clone();
		
		while(tempOffset.x > this.gridSize || tempOffset.y > this.gridSize) {
			if(tempOffset.x > this.gridSize)
				tempOffset.x -= this.gridSize;
			
			if(tempOffset.y > this.gridSize)
				tempOffset.y -= this.gridSize;
		}
		
		while(tempOffset.x < 0 || tempOffset.y < 0) {
			if(tempOffset.x < 0)
				tempOffset.x += this.gridSize;
			
			if(tempOffset.y < 0)
				tempOffset.y += this.gridSize;
		}
		
		return tempOffset;
	}
	
	public void reset() {
		this.offset = new Point(this.getWidth()/2, this.getHeight()/2);
		this.tilemap.clear();
		
		this.repaint();
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {
		this.dragStart = e.getPoint();
		
		if(e.getButton() == 1)
			this.left = true;
		
		if(e.getButton() == 2)
			this.middle = true;
		
		if(e.getButton() == 3)
			this.right = true;
		
		//calculate new position for tile
		int mouseX = e.getX() - this.offset.x;
		int mouseY = e.getY() - this.offset.y;
		
		if(mouseX < 0) mouseX -= this.gridSize;
		if(mouseY < 0) mouseY -= this.gridSize;
		
		mouseX -= mouseX % this.gridSize;
		mouseY -= mouseY % this.gridSize;
		
		//plot tile
		if(this.left) {
			//was previous tile from same position replaced?
			boolean replaced = false;
			
			Tile temp = new Tile(this.activeTile);
			temp.x = mouseX;
			temp.y = mouseY;
			temp.z = this.currentLayer;
			
			//loop through all tiles
			for(Tile t: this.tilemap) {
				//it tile matches the cursor location...
				if(t.x == mouseX && t.y == mouseY && t.z == this.currentLayer) {
					//remove the old one and..
					this.tilemap.remove(t);
					
					//place new to the same position
					tilemap.add(new Tile(temp));
					
					//tile replaced
					replaced = true;
					break;
				}
			}
			
			//if not replaced, just create a new tile
			if(!replaced)
				tilemap.add(new Tile(temp));
		}
		
		//remove tile
		if(this.right && !e.isShiftDown()) {
			//loop through all tiles
			for(Tile t: this.tilemap) {
				//if tile matches the cursors location...
				if(t.x == mouseX && t.y == mouseY && t.z == this.currentLayer) {
					//remove it
					this.tilemap.remove(t);
					break;
				}
			}
		}
		
		//select new active tile from map
		if(this.middle) {
			for(Tile t: this.tilemap) {
				//it tile matches the cursor location...
				if(t.x == mouseX && t.y == mouseY && t.z == this.currentLayer) {
					this.activeTile.color = t.color;
					this.activeTile.image = t.image;
					this.activeTile.id = t.id;
					this.activeTile.isFloor = t.isFloor;
					this.activeTile.isWall = t.isWall;
				}
			}
		}
		
		this.repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if(e.getButton() == 1)
			this.left = false;
		
		if(e.getButton() == 2)
			this.middle = false;
		
		if(e.getButton() == 3)
			this.right = false;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		this.setFocusable(true);
		this.requestFocusInWindow();
	}

	@Override
	public void mouseExited(MouseEvent e) {
		this.setFocusable(false);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if(e.isShiftDown() && this.right) {
			this.offset.x -= this.dragStart.x - e.getX();
			this.offset.y -= this.dragStart.y - e.getY();
			this.repaint();
		
			this.dragStart = e.getPoint();
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		this.repaint();
		mouse = e.getPoint();
		Point tempOffset = this.calculateGridOffset();
		
		mouse.x -= tempOffset.x;
		mouse.y -= tempOffset.y;
		
		mouse.x -= mouse.x % gridSize;
		mouse.y -= mouse.y % gridSize;
	}

	@Override
	public void keyTyped(KeyEvent e) {
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_G) {
			this.showGrid = !this.showGrid;
			this.repaint();
		}
		
		if(e.getKeyCode() == KeyEvent.VK_C) {
			this.showOnlyCurrentLayer = !this.showOnlyCurrentLayer;
			this.repaint();
		}
		
		if(e.getKeyCode() == KeyEvent.VK_H) {
			this.showHud = !this.showHud;
			this.repaint();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	//TODO scrolling through layers
	public void mouseWheelMoved(MouseWheelEvent e) {
		if(e.getWheelRotation() < 0){
			//Wheel scrolled up
			this.currentLayer++;
			if(this.currentLayer > 5) this.currentLayer = 0;
		}else{
			//Wheel scrolled down
			this.currentLayer--;
			if(this.currentLayer < 0) this.currentLayer = 5;
		}
		
		this.repaint();
	}

}

