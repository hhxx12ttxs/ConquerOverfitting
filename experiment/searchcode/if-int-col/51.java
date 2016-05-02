package game.misc;


import game.entities.Terrain.TerrainType;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


public class LevelMaker {
	TerrainType[][] currLevel;
	char[][] origLevel;
	int BLOCK_SIZE, xAlign, yAlign;
	static int arrayRows, arrayCols;
	String levelName = "map1.txt";
	int levelWidth, levelHeight;
	TerrainType currentBlock;
	JFrame frame, parent;
	Rectangle borderRect, levelRect;
	DrawPanel dPanel;
	TilePanel tPanel;
	boolean saved = true;
	static BufferedImage[] images = new BufferedImage[TerrainType.values().length];
	private boolean pngMode = true;
		
	public static void main(String args[]) {
		new LevelMaker(null);
		arrayRows = Integer.parseInt(args[0]);
		arrayCols = Integer.parseInt(args[1]);
	}
	
	public LevelMaker(JFrame parent) {
		this.parent = parent;
		levelName = JOptionPane.showInputDialog("Enter Full Name of Level File to Edit:");
		
		frame = new JFrame("Level Maker");
		//loadImages();
		dPanel = new DrawPanel(80);
		tPanel = new TilePanel(20);
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), 
													   BoxLayout.Y_AXIS));
		frame.getContentPane().add(dPanel);
		frame.getContentPane().add(tPanel);
		currLevel = new TerrainType[24][32];
		for(int row = 0; row < currLevel.length; row++) {
			for(int col = 0; col < currLevel[row].length; col++) {
				currLevel[row][col] = TerrainType.ERROR;
			}
		}
		currentBlock = TerrainType.DIRT;
		
		BLOCK_SIZE = 50;

		loadLevel();
		borderRect = new Rectangle(0, 0, BLOCK_SIZE, BLOCK_SIZE);
		levelRect = new Rectangle(0, 0, 
				levelWidth*BLOCK_SIZE, 
				levelHeight*BLOCK_SIZE);
		frame.setResizable(false);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
		frame.setUndecorated(true);
		frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
		
	public void loadLevel() {
		try {
			origLevel = new char[24][32];
	        int colLength = currLevel[0].length;
	        //System.out.println(height + " " +  width);
	        Scanner levelReader = new Scanner(new FileReader(
	        		new File(this.getClass().getResource("/data/" + levelName)
	        				.toURI())));
			int row = 0;
			int col = 0;
			while(levelReader.hasNext()) {
				char block = (char)levelReader.next().codePointAt(0);
				if(block != ' ') {
					origLevel[row][col] = block;
					col++;
				}
				if(col == colLength) { 
					col = 0;
					row++;
				}
			}
	} catch( Exception e) {e.printStackTrace();}
	
	//System.exit(0);
		for( int row = 0; row < origLevel.length; row++ ) {
			for( int col = 0; col < origLevel[row].length; col++ ) {
				currLevel[row][col] = 
					TerrainType.getType(origLevel[row][col]);		
			}
		}
		levelWidth = currLevel[0].length;
		levelHeight = currLevel.length;

}
	
	private void loadImages() {
		try {
			if(!pngMode ) {
				images[1] = ImageIO.read(new File("SOLID.gif"));
				images[2] = ImageIO.read(new File("BOUNCY.gif"));
				images[3] = ImageIO.read(new File("TEMPORARY.gif"));
				images[4] = ImageIO.read(new File("main_right.gif"));
			} else {
				images[1] = ImageIO.read(new File("SOLID.png"));
				images[2] = ImageIO.read(new File("BOUNCY.png"));
				images[3] = ImageIO.read(new File("TEMPORARY.png"));
				images[4] = ImageIO.read(new File("main_right.png"));
			}			

		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	class DrawPanel extends JPanel implements MouseListener, MouseMotionListener, KeyListener {
		int percent;

		public DrawPanel(int percent) {
			this.percent = percent;
			setFocusable(true);
			addKeyListener(this);
			addMouseListener(this);
			addMouseMotionListener(this);
			setBackground(Color.BLACK);
		}
		
		public void paintComponent(Graphics gr) {
			super.paintComponent(gr);
			Graphics2D g = (Graphics2D) gr;
			requestFocusInWindow();
			
			g.setColor(Color.BLACK);
			for(int row = 0; row < currLevel.length; row++) {
				for(int col = 0; col < currLevel[row].length; col++) {
					g.setColor(currLevel[row][col].color);
					g.fillRect(col*BLOCK_SIZE-xAlign, 
						    row*BLOCK_SIZE+yAlign, BLOCK_SIZE, BLOCK_SIZE);
					/*g.drawImage(currLevel[row][col].image, col*BLOCK_SIZE-xAlign, 
						    row*BLOCK_SIZE+yAlign, BLOCK_SIZE, BLOCK_SIZE, this);*/
				}
			}
			g.setColor(Color.WHITE);
			g.draw(borderRect);
			g.setColor(Color.GREEN);
			g.draw(levelRect);
			
		}
		
		public Dimension getPreferredSize() {
			Dimension d = getParent().getSize();
			int w = d.width * percent / 100;
			int h = d.height * percent / 100;
			return new Dimension(w,h);
		}

		@Override
		public void mouseDragged(MouseEvent evt) {
			saved = false;
			int x = ((evt.getX()+BLOCK_SIZE/2)/BLOCK_SIZE)*BLOCK_SIZE;
			int y = ((evt.getY()+BLOCK_SIZE/2)/BLOCK_SIZE)*BLOCK_SIZE;
			int width = BLOCK_SIZE;
			int height = BLOCK_SIZE;
			int arrayRow, arrayCol;
			borderRect.x = x;
			borderRect.y = y;
			
			arrayRow = ((y+height-yAlign)/BLOCK_SIZE)-1;
			arrayCol = ((x+width+xAlign)/BLOCK_SIZE)-1;
			//System.out.println(arrayRow + " " + arrayCol);
			currLevel[arrayRow][arrayCol] = currentBlock;
				
			
			repaint();
		}

		@Override
		public void mouseMoved(MouseEvent evt) {
		
			int x = ((evt.getX()+BLOCK_SIZE/2)/BLOCK_SIZE)*BLOCK_SIZE;
			int y = ((evt.getY()+BLOCK_SIZE/2)/BLOCK_SIZE)*BLOCK_SIZE;
			int width = BLOCK_SIZE;
			int height = BLOCK_SIZE;
			borderRect.x = x;
			borderRect.y = y;
			
			repaint();
		}

		@Override
		public void keyPressed(KeyEvent evt) {
			
			System.out.println("D: " + evt.getKeyChar()+"");
			currentBlock = TerrainType.getType(evt.getKeyChar());

			switch(evt.getKeyCode()) {
				case KeyEvent.VK_RIGHT:
					xAlign+=BLOCK_SIZE;
					break;
				case KeyEvent.VK_LEFT:
					xAlign-=BLOCK_SIZE;
					break;
				case KeyEvent.VK_UP:
					yAlign+=BLOCK_SIZE;
					break;
				case KeyEvent.VK_DOWN:
					yAlign-=BLOCK_SIZE;
					break;
				case KeyEvent.VK_ESCAPE:
					if(!saved) {
						switch(JOptionPane.showConfirmDialog(null, "You haven't saved yet. " +
								"Would you like to return and save?")) {
							case JOptionPane.YES_OPTION:
								// Do nothing and return to level maker
								break;
							case JOptionPane.NO_OPTION:
								if(parent != null) {
									frame.setVisible(false);
									frame.repaint();
									frame.validate();
									frame.dispose();
									dPanel = null;
									tPanel = null;
									currLevel = null;
									origLevel = null;
								} else {
									System.exit(0);
								}
								break;
							case JOptionPane.CANCEL_OPTION:
								// Do nothing and return to level maker
								break;
						}
					} else {
						if(parent != null) {
							frame.setVisible(false);
							frame.repaint();
							frame.validate();
							frame.dispose();
							dPanel = null;
							tPanel = null;
							currLevel = null;
							origLevel = null;
						} else {
							System.exit(0);
						}
					}
					break;
				case KeyEvent.VK_F12:
					for(int row = 0; row < currLevel.length; row++) {
						for(int col = 0; col < currLevel[row].length; col++) {
							try {
								currLevel[row][col] = TerrainType.DIRT;
							} catch(Exception e) {}
						}
					}
					break;
				case KeyEvent.VK_EQUALS:
					if(BLOCK_SIZE < 50) {
						BLOCK_SIZE+=25;
						System.out.println(BLOCK_SIZE);
						
						borderRect.setBounds(borderRect.x+25, 
								borderRect.y+25, BLOCK_SIZE, BLOCK_SIZE);

						if(BLOCK_SIZE == 50) loadImages();
					}
					Toolkit.getDefaultToolkit().sync();
					break;
				case KeyEvent.VK_MINUS:
					if(BLOCK_SIZE > 0) {
					BLOCK_SIZE-=25;
					borderRect.setBounds(borderRect.x-25, 
							borderRect.y-25, BLOCK_SIZE, BLOCK_SIZE);
					Toolkit.getDefaultToolkit().sync();
					break;
			}
		}
			
			
		levelRect.setBounds(0-xAlign, 0+yAlign, 
				levelWidth*BLOCK_SIZE, 
				levelHeight*BLOCK_SIZE);

		repaint();
		}

		BufferedImage createResizedCopy(Image originalImage, 
                int scaledWidth, int scaledHeight, 
                boolean preserveAlpha) {
				        System.out.println("resizing...");
				        int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
				        BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
				        Graphics2D g = scaledBI.createGraphics();
				        g.setColor(Color.WHITE);
				        g.fillRect(0, 0, scaledWidth, scaledHeight);
				        if (preserveAlpha) {
				                g.setComposite(AlphaComposite.Src);
				        }
				        g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null); 
				        g.dispose();
				        return scaledBI;
    }
		
		@Override
		public void keyReleased(KeyEvent evt) {

		}

		@Override
		public void keyTyped(KeyEvent evt) {
			System.out.println(evt.getKeyChar());
			for(TerrainType t: TerrainType.values()) {
				if(evt.getKeyChar() == t.code ||
						("" + evt.getKeyChar())
						.equalsIgnoreCase("" + t.code)) {
					currentBlock = t;
				}
			}
		}

		@Override
		public void mouseClicked(MouseEvent evt) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseEntered(MouseEvent evt) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent evt) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mousePressed(MouseEvent evt) {
			saved = false;
			int x = ((evt.getX()+BLOCK_SIZE/2)/BLOCK_SIZE)*BLOCK_SIZE;
			int y = ((evt.getY()+BLOCK_SIZE/2)/BLOCK_SIZE)*BLOCK_SIZE;
			int width = BLOCK_SIZE;
			int height = BLOCK_SIZE;
			int arrayRow, arrayCol;
			borderRect.setBounds(x, y, BLOCK_SIZE, BLOCK_SIZE);
			arrayRow = ((y+height-yAlign)/BLOCK_SIZE)-1;
			arrayCol = ((x+width+xAlign)/BLOCK_SIZE)-1;
			//System.out.println(arrayRow + " " + arrayCol);
			currLevel[arrayRow][arrayCol] = currentBlock;
				
			repaint();

		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}
	}
		
	class TilePanel extends JPanel implements ActionListener {
		int percent;
		JButton saveButton;
		TerrainType type[];
		public TilePanel(int percent) {
			this.percent = percent;
			type = TerrainType.values();
			saveButton = new JButton("SAVE");
			saveButton.addActionListener(this);
			
			add(saveButton, BorderLayout.EAST);
			setBackground(Color.green);
		}
		
		public void paintComponent(Graphics g) {
			//System.out.println("DDDDDD");
			super.paintComponent(g);
			for (int i = 0; i < type.length; i++) {
				g.drawString(""+type[i].code + " " 
						+ type[i].toString(), 60*i+10, 45);
				g.setColor(type[i].color);
				g.fillRect(60*i+10, 50, BLOCK_SIZE, BLOCK_SIZE);
				//System.out.println(TerrainType[i]);
			}
		}
		
		public Dimension getPreferredSize() {
			Dimension d = getParent().getSize();
			int w = d.width * percent / 100;
			int h = d.height * percent / 100;
			return new Dimension(w,h);
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			String dateName;
			DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH~mm~ss");
	        Date date = new Date();
	        dateName = dateFormat.format(date);
	        System.out.println(dateName);
			File outputFile = null;
			try {
				outputFile = new File(this.getClass().getResource("/data/" + levelName)
						.toURI());
			} catch (URISyntaxException e1) {
				e1.printStackTrace();
			}
			PrintWriter writer = null;
			try {
				writer = new PrintWriter(outputFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			char tileID = '\0';
			for(int row = 0; row < currLevel.length; row++) {
				for(int col = 0; col < currLevel[row].length; col++) {
					tileID = currLevel[row][col].code;
					if(col == currLevel[row].length-1) {
						writer.append(tileID);
						writer.println();
					} else {
						writer.append(tileID + " ");
					}
					writer.flush();
				}
			}
			writer.close();
			dPanel.requestFocusInWindow();
			saved = true;
		}
	}
	
}
