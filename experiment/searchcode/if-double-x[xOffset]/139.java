package ui;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;

import util.GS;
import util.Util;

import board.Board;
import board.LineMark;
import board.NodeLabel;
import board.NodeMark;
import board.SimpleMark;

import java.awt.geom.*;
import java.applet.*;

/**
 * This class draws the goban.
 * @author TKington
 *
 */
public class GobanPanel extends JPanel
{
	/** The number of white stone images with different grain. */
    private static final int NUM_WHITE_IMAGES = 12;
    /** The minimum stone image size. */
    private static final int MIN_TILE_SIZE = 20;
    /** The maximum stone image size. */
    private static final int MAX_TILE_SIZE = 48;
    
    /** The hoshi locations for a 9x9 board. */
    private static final Point [] HOSHI9 = new Point [] {new Point(2, 2), new Point(2, 6), new Point(6, 2), new Point(6, 6)};
    /** The hoshi locations for a 13x13 board. */
    private static final Point [] HOSHI13 = new Point [] {new Point(3, 3), new Point(3, 9), new Point(9,  3),
                                                  new Point(9, 9), new Point(6, 6)};
    /** The hoshi locations for a 19x19 board. */
    private static final Point [] HOSHI19 = new Point [] {new Point(3, 3), new Point(3, 9), new Point(3, 15),
                                                  new Point(9, 3), new Point(9, 9), new Point(9, 15),
                                                  new Point(15, 3), new Point(15, 9), new Point(15, 15)};

    /** Black stone images for all sizes. */
    public static Image blacks[];
    /** White stone images in all sizes and grains. */
    public static Image whites[][];
    /** Black ghost images of all sizes. */
    private static Image blackGhosts[];
    /** White ghost images of all sizes. */
    private static Image whiteGhosts[];
    /** Board background image. */
    private static Image kaya;
    
    /** Click sound. */
    static AudioClip pok;
    
    /** Black stone image for current size. */
    private Image black;
    /** White stone images for current size. */
    private Image white[];
    /** Black ghost image for current size. */
    private Image blackGhost;
    /** White ghost image for current size. */
    private Image whiteGhost;
    
    /** The Board. */
    private Board board;
    /** The listener that will recieve click events. */
    private IGobanListener listener;
    /** The font that will be used to derive fonts for labels. */
    private Font font;
    
    /** Size of a square on the board. */
    private int tileSize;
    
    /** x coordinate of the ghost stone. */
    private int ghostX = -1;
    /** y coordinate of the ghost stone. */
    private int ghostY = -1;
    /** Current color of the ghost stone. */
    private int ghostColor = 1;
    /** Hide the ghost? */
    private boolean hideGhost = false;
    
    /** If true, good/bad moves shown. */
    private boolean navMode;
    
    /** Which grain to use for each point on the board. */
    private int[][] whichWhite;
    /** List of hoshi points for current board size. */
    private Point [] hoshi;
    
    /** Area of the board to be shown in board units. */
    private Rectangle boardBounds;
    /** Dimensions of the board on screen. */
    private Dimension winBounds;
    /** Non-board space to the left of the board. */
    private int xOffset;
    /** Non-board space above the board. */
    private int yOffset;
    /** 
     * Distance from edge of tile to edge of stone.  Zero unless the
     * board is too big for the biggest stone images.
     */
    private int stoneOff;
    
    /**
     * Creates a new GobanPanel.
     * @param b The Board object being represented
     */
    public GobanPanel(Board b)
    {
        setBoard(b);
        
        font = new Font("SansSerif", Font.BOLD, 24); //$NON-NLS-1$
        
        whichWhite = new int[19][19];
        for(int i = 0; i < 19; i++) {
            for(int j = 0; j < 19; j++) {
                whichWhite[i][j] = Main.rand.nextInt(NUM_WHITE_IMAGES);
            }
        }
        
        try {
            loadImages();
        }
        catch(IOException e) {
            JOptionPane.showMessageDialog(null, "Error loading images. See viewer.log for details.");
            Util.logSilent(e);
            System.exit(-1);
        }
        
        setLayout(null);
        setMinimumSize(new Dimension(MIN_TILE_SIZE * 19, MIN_TILE_SIZE * 19));
        setPreferredSize(new Dimension(MIN_TILE_SIZE * 19, MIN_TILE_SIZE * 19));
        
        setSize(new Rectangle(0, 0, 19, 19), new Dimension(517, 517));
        
        addMouseListener(new MouseAdapter()
        {
            @Override
			public void mouseReleased(MouseEvent e)
            {
                onClick(e);
            }
            
            @Override
			public void mouseExited(MouseEvent e) {
				int x = (ghostX - boardBounds.x) * tileSize + xOffset;
				int y = (ghostY - boardBounds.y) * tileSize + yOffset;
		
				ghostX = ghostY = -1;

                repaint(0, x, y, tileSize, tileSize);
            }
        });
        
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
			public void mouseMoved(MouseEvent e) {
                onMouseMoved(e);
            }
        });
        
        addMouseWheelListener(new MouseWheelListener() {
        	public void mouseWheelMoved(MouseWheelEvent e) {
        		onMouseWheelMoved(e);
        	}
        });
        
        addComponentListener(new ComponentAdapter() {
            @Override
			public void componentResized(ComponentEvent e) {
                setSize(null, getSize());
            }
        });
    }
    
    /**
     * Sets the Board being displayed by the GobanPanel.
     * @param b the Board
     */
    public void setBoard(Board b) {
        board = b;
        switch(board.getSize()) {
            case 9: hoshi = HOSHI9; break;
            case 13: hoshi = HOSHI13; break;
            case 19: hoshi = HOSHI19; break;
            default: hoshi = null; break;
        }
    }
    
    /**
     * Sets the size of the GobanPanel, and causes the GobanPanel to recalculate
     * what parts of the board are visible, and the sizes of the stones.
     * @param bBounds the "interesting" area of the board
     * @param wBounds the dimensions of the panel itself
     */
    public void setSize(Rectangle bBounds, Dimension wBounds) {
        if(wBounds == null)
            wBounds = winBounds;
        
        if(bBounds == null)
            bBounds = boardBounds;
        else {
            //  Leave one row of space around problem
            if(bBounds.x > 0) {
                bBounds.x--;
                bBounds.width++;
            }
            if(bBounds.width < board.getSize() - bBounds.x)
                bBounds.width++;
            if(bBounds.y > 0) {
                bBounds.y--;
                bBounds.height++;
            }
            if(bBounds.height < board.getSize() - bBounds.y)
                bBounds.height++;

            //  If stones are near the edge, show the edge
            if(bBounds.x < 4) {
            	bBounds.width += bBounds.x;
            	bBounds.x = 0;
            }
            if(bBounds.x + bBounds.width > board.getSize() - 5)
            	bBounds.width = board.getSize() - bBounds.x;
            if(bBounds.y < 4) {
            	bBounds.height += bBounds.y;
            	bBounds.y = 0;
            }
            if(bBounds.y + bBounds.height > board.getSize() - 5)
            	bBounds.height = board.getSize() - bBounds.y;
        }
        
        int bSize = Math.max(bBounds.height, bBounds.width);
        
        int pixSize = Math.min(wBounds.width, wBounds.height);
        bSize = Math.min(board.getSize(), Math.max(bSize, pixSize / MAX_TILE_SIZE));
        
        //  Adjust bounds if bSize overridden
        if(bBounds.width < bSize) {
            bBounds.x = Math.max(0, bBounds.x + bBounds.width / 2 - bSize / 2);
            bBounds.width = bSize;
            bBounds.x = Math.min(bBounds.x, board.getSize() - bBounds.width);
        }
        if(bBounds.height < bSize) {
            bBounds.y = Math.max(0, bBounds.y + bBounds.height / 2 - bSize / 2);
            bBounds.height = bSize;
            bBounds.y = Math.min(bBounds.y, board.getSize() - bBounds.height);
        }
        
        boardBounds = bBounds;
        winBounds = wBounds;
        
        tileSize = Math.min(MAX_TILE_SIZE + 5, pixSize / bSize);
        
        int w = boardBounds.width * tileSize;
        int h = boardBounds.height * tileSize;
        xOffset = (wBounds.width - w) / 2;
        yOffset = (wBounds.height - h) / 2;
        
        stoneOff = Math.max(0, (tileSize - MAX_TILE_SIZE) / 2);
        
        float fontSize;
        if(tileSize > 30)
            fontSize = 24.0f;
        else if(tileSize > 25)
            fontSize = 18.0f;
        else fontSize = 14.0f;
        if(font.getSize() != fontSize)
            font = font.deriveFont(fontSize);
        
        int tileNum = Math.min(Math.max(tileSize - MIN_TILE_SIZE, 0), blacks.length - 1);
        black = blacks[tileNum];
        white = whites[tileNum];
        blackGhost = blackGhosts[tileNum];
        whiteGhost = whiteGhosts[tileNum];
        
        repaint();
    }
    
    @Override
	public void paint(Graphics g)
    {
        Graphics2D g2d = (Graphics2D)g;
        g2d.addRenderingHints( new RenderingHints( RenderingHints.KEY_ANTIALIASING ,
                                              RenderingHints.VALUE_ANTIALIAS_ON ));
        
        g.setFont(font);
        
        int halfSize = tileSize / 2;
        int w = boardBounds.width * tileSize;
        int h = boardBounds.height * tileSize;
        Dimension panelSize = getSize();
        
        g.setColor(Main.BGCOLOR);
        g.fillRect(0, 0, panelSize.width, yOffset);
        g.fillRect(0, yOffset, xOffset, panelSize.height - yOffset);
        g.fillRect(xOffset, yOffset + h, panelSize.width - xOffset, panelSize.height - h - yOffset);
        g.fillRect(xOffset + w, yOffset, panelSize.width - w - xOffset, h);
        g.setColor(Color.black);
        
        g.translate(xOffset, yOffset);
        
        g.drawImage(kaya, 0, 0, w, h, 0, 0, w, h, null);
        g.drawRect(0, 0, w, h);
                
        //  Grey squares go on before anything else
        ArrayList marks = board.getMarks();
        for(int i = 0; i < marks.size(); i++) {
            NodeMark m = (NodeMark)marks.get(i);
            if(m.getType() != NodeMark.GREYSQ)
                continue;

            Point p2 = ((SimpleMark)m).getPoint();

            int x = p2.x - boardBounds.x;
            int y = p2.y - boardBounds.y;
            g.setColor(Color.lightGray);
            g.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
        }

        drawGrid(g);
        
        for(int i = 0; i < marks.size(); i++) {
            NodeMark m = (NodeMark)marks.get(i);
            if(m.getType() == NodeMark.LINE) {
                LineMark line = (LineMark)m;
                Point p1 = line.getP1();
                Point p2 = line.getP2();
                int x1 = p1.x - boardBounds.x;
                int y1 = p1.y - boardBounds.y;
                int x2 = p2.x - boardBounds.x;
                int y2 = p2.y - boardBounds.y;
                g.setColor(line.getColor());
                g2d.setStroke(line.getStroke());
                g.drawLine(x1 * tileSize + halfSize, y1 * tileSize + halfSize,
                           x2 * tileSize + halfSize, y2 * tileSize + halfSize);
            }
        }
        
        for(int i = 0; i < boardBounds.width; i++)
        {
            for(int j = 0; j < boardBounds.height; j++)
            {
                int x = i + boardBounds.x;
                int y = j + boardBounds.y;
                if(board.getAt(x,y) == 1)
                    g.drawImage(black, i * tileSize + stoneOff, j * tileSize + stoneOff, null);
                else if(board.getAt(x,y) == -1)
                    g.drawImage(white[whichWhite[i][j]], i * tileSize + stoneOff, j * tileSize + stoneOff, null);
            }
        }
        
        Point p = new Point();
        Color oldColor;
        g2d.setStroke(new BasicStroke(tileSize < 25 ? 2 : 3));
        for(int i = 0; i < marks.size(); i++) {
            NodeMark mk = (NodeMark)marks.get(i);
            if(mk instanceof SimpleMark) {
                SimpleMark m = (SimpleMark)mk;
                Point p2 = m.getPoint();

                if(board.getAt(p2.x, p2.y) == 1)
                    g.setColor(Color.WHITE);
                else g.setColor(Color.BLACK);

                p.x = p2.x - boardBounds.x;
                p.y = p2.y - boardBounds.y;

                int x, y, size;
                switch(m.getType()) {
                    case NodeMark.CIR:
                    case NodeMark.GREENCIR:
                    case NodeMark.REDCIR:
                        oldColor = g.getColor();
                        if(m.getType() == NodeMark.GREENCIR)
                            g.setColor(Color.green);
                        else if(m.getType() == NodeMark.REDCIR)
                            g.setColor(Color.red);

                        x = (int)((p.x + 0.22) * tileSize);
                        y = (int)((p.y + 0.18) * tileSize);
                        size = (int)(tileSize * 0.6);
                        g.drawOval(x, y, size, size);

                        g.setColor(oldColor);
                        break;
                    case NodeMark.SQU:
                        Rectangle2D.Double r = new Rectangle2D.Double((p.x + 0.22) * tileSize,
                                                                       (p.y + 0.22) * tileSize,
                                                                       tileSize * 0.56, tileSize * 0.56);
                        g2d.draw(r);
                        break;
                    case NodeMark.X:
                        Line2D.Double line = new Line2D.Double((p.x + 0.22) * tileSize,
                                                               (p.y + 0.22) * tileSize,
                                                               (p.x + 0.78) * tileSize,
                                                               (p.y + 0.78) * tileSize);
                        g2d.draw(line);
                        line.setLine((p.x + 0.22) * tileSize, (p.y + 0.78) * tileSize,
                                     (p.x + 0.78) * tileSize, (p.y + 0.22) * tileSize);
                        g2d.draw(line);
                        break;
                    case NodeMark.TRI:
                    case NodeMark.GREENTRI:
                    case NodeMark.REDTRI:
                        oldColor = g.getColor();
                        if(m.getType() == NodeMark.GREENTRI)
                            g.setColor(Color.green);
                        else if(m.getType() == NodeMark.REDTRI)
                            g.setColor(Color.red);

                        int x1 = p.x * tileSize + halfSize;
                        int y1 = (int)((p.y + 0.07) * tileSize);
                        int x2 = (int)((p.x + 0.15) * tileSize);
                        int y2 = (int)((p.y + 0.72) * tileSize);
                        int x3 = (int)((p.x + 0.85) * tileSize);
                        g.drawLine(x1, y1, x2, y2);
                        g.drawLine(x2, y2, x3, y2);
                        g.drawLine(x3, y2, x1, y1);

                        g.setColor(oldColor);
                        break;
                    case NodeMark.TERRW:
                        g.setColor(Color.white);
                    case NodeMark.TERRB:
                        x = p.x * tileSize + halfSize;
                        y = p.y * tileSize + halfSize;
                        if(tileSize < 27)
                            g.fillOval(x - 3, y - 3, 7, 7);
                        else g.fillOval(x - 5, y - 5, 11, 11);
                        break;
                    case NodeMark.GREYSTONE:
                        oldColor = g.getColor();
                        g.setColor(Color.gray);
                        x = p.x * tileSize;
                        y = p.y * tileSize;
                        g.fillOval(x + 2, y + 2, tileSize - 4, tileSize - 4);
                        g.setColor(oldColor);
                        break;
                }
            }
        }
        
        g.setColor(Color.black);
        FontMetrics met = g.getFontMetrics();
        int dy = halfSize + met.getAscent() / 2 - (2 * tileSize / 16);
        for(int i = 0; i < marks.size(); i++) {
            NodeMark m = (NodeMark)marks.get(i);
            if(m.getType() == NodeMark.LABEL) {
                NodeLabel lab = (NodeLabel)m;
                Point p2 = lab.getPoint();

                if(board.getAt(p2.x, p2.y) == 1)
                    g.setColor(Color.WHITE);
                else g.setColor(Color.BLACK);

                p.x = p2.x - boardBounds.x;
                p.y = p2.y - boardBounds.y;
                int dx = halfSize - met.stringWidth(lab.getText()) / 2;
                g.drawString(lab.getText(), p.x * tileSize + dx, p.y * tileSize + dy);
            }
        }
        
        int right = boardBounds.x + boardBounds.width;
        int bottom = boardBounds.y + boardBounds.height;
        if(ghostX >= boardBounds.x && ghostX < right &&
                ghostY >= boardBounds.y && ghostY < bottom &&
                board.getAt(ghostX,ghostY) == 0)
            g.drawImage((ghostColor == 1) ? blackGhost : whiteGhost,
                        (ghostX - boardBounds.x) * tileSize,
                        (ghostY - boardBounds.y) * tileSize,
                        null);
        
        if(navMode) {
            ArrayList good = board.getGoodMoves();
            for(int i = 0; i < good.size(); i++) {
                p.x = ((Point)good.get(i)).x - boardBounds.x;
                p.y = ((Point)good.get(i)).y - boardBounds.y;

                int x = (int)((p.x + 0.5) * tileSize - 3);
                int y = (int)((p.y + 0.5) * tileSize - 3);
                g.setColor(Color.green);
                g.fillOval(x, y, 7, 7);  
            }

            ArrayList bad = board.getBadMoves();
            for(int i = 0; i < bad.size(); i++) {
                p.x = ((Point)bad.get(i)).x - boardBounds.x;
                p.y = ((Point)bad.get(i)).y - boardBounds.y;

                int x = (int)((p.x + 0.5) * tileSize - 3);
                int y = (int)((p.y + 0.5) * tileSize - 3);
                g.setColor(Color.red);
                g.fillOval(x, y, 7, 7);  
            }
        }
    }
    
    /**
     * Draws the grid on the board.
     * @param g the Graphics context
     */
    private void drawGrid(Graphics g) {
    	int halfSize = tileSize / 2;
    	
    	g.setColor(Color.black);
        for(int i = 0; i < boardBounds.width; i++) {
            int left = i * tileSize;
            int right = left + tileSize - 1;

            for(int j = 0; j < boardBounds.height; j++) {
                int bi = i + boardBounds.x;
                int bj = j + boardBounds.y;

                if(board.getAt(bi, bj) == 0 &&
                        board.hasLabelAt(i + boardBounds.x, j + boardBounds.y))
                    continue;

                int x = i * tileSize + halfSize;
                int y = j * tileSize + halfSize;
                int top = j * tileSize;
                int bottom = top + tileSize - 1;

                if(bi > 0)
                    g.drawLine(left, y, x, y);
                if(bi < board.getSize() - 1)
                    g.drawLine(x, y, right, y);
                if(bj > 0)
                    g.drawLine(x, top, x, y);
                if(bj < board.getSize() - 1)
                    g.drawLine(x, y, x, bottom);
            }
        }

        if(hoshi != null) {
            for(int i = 0; i < hoshi.length; i++) {
                Point p = hoshi[i];
                int x = p.x - boardBounds.x;
                int y = p.y - boardBounds.y;
                if(x >= 0 && x < boardBounds.width && y >= 0 && y < boardBounds.height) {
                    if(board.hasLabelAt(p.x, p.y))
                        continue;

                    int left = x * tileSize + halfSize - 2;
                    int top = y * tileSize + halfSize - 2;
                    g.fillRect(left, top, 5, 5);
                }
            }
        }
    }
    
    /**
     * Translates click events to board coordinates, and passes
     * them on to listeners.
     * @param e the click event
     */
    private void onClick(MouseEvent e)
    {
        int x = e.getX();
        int y = e.getY();
        
        if(x < 0 || x >= board.getSize() * tileSize + xOffset ||
           y < 0 || y >= board.getSize() * tileSize + yOffset )
            return;
        
        x = (x - xOffset) / tileSize + boardBounds.x;
        y = (y - yOffset) / tileSize + boardBounds.y;
        
        if(listener != null)
        	listener.mouseClicked(x, y, e.getModifiers());
    }
    
    /**
     * Handles repainting the ghost stone when the mouse is moved.
     * @param e the MouseEvent
     */
    private void onMouseMoved(MouseEvent e) {
        if(hideGhost || !GS.getShowGhost())
            return;
        
        int x = e.getX();
        int y = e.getY();
        
        if(ghostX != -1 &&
            (x < 0 || x >= board.getSize() * tileSize + xOffset ||
             y < 0 || y >= board.getSize() * tileSize + yOffset)) {
		    //  Clear ghost 
		    int oldGhostX = (ghostX - boardBounds.x) * tileSize + xOffset;
		    int oldGhostY = (ghostY - boardBounds.y) * tileSize + yOffset;
		    
		    ghostX = ghostY = -1;
	            
            repaint(0, oldGhostX, oldGhostY, tileSize, tileSize);

            return;
		}
        
        x = (x - xOffset) / tileSize + boardBounds.x;
        y = (y - yOffset) / tileSize + boardBounds.y;
        
        if(x != ghostX || y != ghostY) {

            int oldGhostX = (ghostX - boardBounds.x) * tileSize + xOffset;
            int oldGhostY = (ghostY - boardBounds.y) * tileSize + yOffset;

            ghostX = x;
            ghostY = y;

            //  Clear old stone
            repaint(0, oldGhostX, oldGhostY, tileSize, tileSize);

            //  Repaint new ghost stone
            repaint(0, (ghostX - boardBounds.x) * tileSize + xOffset, 
                       (ghostY - boardBounds.y) * tileSize + yOffset, 
                        tileSize, tileSize);
        }
    }
    
    /**
     * Notifies listeners when the mouse wheel is moved.
     * @param e the MouseWheelEvent
     */
    private void onMouseWheelMoved(MouseWheelEvent e) {
    	if(listener != null)
    		listener.mouseWheelMoved(e.getWheelRotation());
    }
    
    /**
     * Loads an image.
     * @param imagePath path to the image
     * @return the new Image object
     */
    private Image loadImage(String imagePath)
    {
        ClassLoader cl = this.getClass().getClassLoader();
        URL url = cl.getResource(imagePath);
        if (url != null)  {
            ImageIcon icon = new ImageIcon(url);
            return icon.getImage();
        }
        
        return null;
    }
    
    /**
     * Loads all of the images needed to draw the board.
     * @throws IOException if the images can't be loaded
     */
    private void loadImages() throws IOException
    {
        //  Only do this the first time a GobanPanel is created
        if(blacks != null)
            return;
        
        MediaTracker tracker = new MediaTracker(this);
        
        int id = 0;
        
        kaya = loadImage("images/Kaya.png"); //$NON-NLS-1$
        tracker.addImage(kaya, id++);
        
        int numTiles = MAX_TILE_SIZE - MIN_TILE_SIZE + 1;
        blacks = new Image[numTiles];
        whites = new Image[numTiles][NUM_WHITE_IMAGES];
        blackGhosts = new Image[numTiles];
        whiteGhosts = new Image[numTiles];
        
        for(int i = MIN_TILE_SIZE; i <= MAX_TILE_SIZE; i++) {
            int num = i - MIN_TILE_SIZE;
            blacks[num] = loadImage("images/BlackStone" + i + ".png"); //$NON-NLS-1$ //$NON-NLS-2$
            tracker.addImage(blacks[num], id++);

            for(int j = 0; j < NUM_WHITE_IMAGES; j++) {
                whites[num][j] = loadImage("images/WhiteStone" + i + "-" + j + ".png"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                tracker.addImage(whites[num][j], id++);
            }

            blackGhosts[num] = createImage(new FilteredImageSource(blacks[num].getSource(), new GhostFilter()));
            tracker.addImage(blackGhosts[num], id++);

            whiteGhosts[num] = createImage(new FilteredImageSource(whites[num][0].getSource(), new GhostFilter()));
            tracker.addImage(whiteGhosts[num], id++);
        }
        
        try
        {
            tracker.waitForAll();
        }
        catch(InterruptedException e)
        {
            throw new IOException("Error loading images");
        }
    }
    
    /** Filter used to create ghost image. */
    class GhostFilter extends RGBImageFilter {
        @Override
		public int filterRGB(int x, int y, int rgb) {
            if (((x + y) & 1) == 1)
                rgb &= 0x00ffffff;
            return rgb;
        }
    }
    
    /**
     * Causes the location x,y on the board to be repainted.
     * @param x the x location
     * @param y the y location
     */
    public void repaint(int x, int y) {
        int a = (x - boardBounds.x) * tileSize + xOffset;
        int b = (y - boardBounds.y) * tileSize + yOffset;
        repaint(a, b, tileSize, tileSize);
    }
    
    /**
     * Sets the GobanListener.
     * @param l the listener
     */
    public void setGobanListener(IGobanListener l) { listener = l; }
    
    /**
     * Sets whether the good and bad next moves are displayed. 
     * @param m true to display the good and bad moves
     */
    public void setNavMode(boolean m) { navMode = m; repaint(); }
    
    /**
     * Sets the color of the ghost stone.
     * @param c 1 for black, -1 for white
     */
    public void setGhostColor(int c) { ghostColor = c; }
    
    /**
     * Hides the ghost momentarily.
     *
     */
    public void hideGhost() { ghostX = ghostY = -1; }
    
    /**
     * Turns off the ghost stone.
     *
     */
    public void turnOffGhost() { hideGhost(); hideGhost = true; }    
}

