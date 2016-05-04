package game.core;

import game.entities.Bullet;
import game.entities.GameMap;
import game.entities.Player;
import game.entities.Terrain;
import game.entities.Terrain.TerrainType;
import game.entities.Weapon.WeaponType;
import game.misc.BulletCombo;
import game.misc.Command;
import game.misc.GameSettings;
import game.misc.GameUtils;
import game.misc.LevelMaker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;


public class GameClient {
	private JFrame frame;
	private GamePanel gamePanel;
	private RoomPanel roomPanel;
	private IntroPanel introPanel;
	private InstructionPanel instructPanel;
	private ArrayList<Player> players;
	private CopyOnWriteArrayList<Bullet> bullets;
	private HashMap<Integer,Player> ids;
	private Player yourPlayer;
	private Socket masterConnector;
	private LoginStatus status;
	private BufferedWriter roomWriter, masterWriter;
	private BufferedReader roomReader, masterReader;
	private int frameWidth, frameHeight, mapWidth, mapHeight;
	private volatile boolean sendUpdates = false, inGame = false,
	endListening = false, endUpdating = false;
	private static final InetSocketAddress MASTER_ADDRESS = 
		new InetSocketAddress(GameSettings.MASTER_SERVER, 
				GameSettings.MASTER_CLIENT_PORT);
	
	
	private Logger CLILOG = Logger.getLogger("Client Logger");
	
	public static void main(String args[]) {
		//assert GameSettings.THREAD_IO_DELAY >= GameSettings.GAME_DELAY;
		if(args.length == 2) {
			GameSettings.THREAD_IO_DELAY = Integer.parseInt(args[0]);
			GameSettings.GAME_DELAY = Integer.parseInt(args[1]);
		} 
		
		new GameClient().startGame();	
	}
	
	public GameClient() {
		masterConnector = new Socket();
		status = new LoginStatus();
		frame = new JFrame("Game Client");
		frame.setSize(800, 630);
		frame.setResizable(false);
		frameWidth = frame.getWidth();
		frameHeight = frame.getHeight();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowClosing(WindowEvent e) {
				if(inGame) {
					requestExit();
					closeRoom();
				} else {
					closeConnToMaster();
					frame.dispose();
					System.exit(0);
				}
			}
			
			@Override
			public void windowClosed(WindowEvent e) {
				
			}
			
			@Override
			public void windowActivated(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		frame.setVisible(true);
	}
	
	private void startGame() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JFrame.setDefaultLookAndFeelDecorated(true);
				try {
					//UIManager.setLookAndFeel("com.jtattoo.plaf.aluminium.AluminiumLookAndFeel");
				
					UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
				} catch (Exception ex) {
					System.out.println("Failed loading L&F: ");
					System.out.println(ex);
				}
				connectToMaster();
				introPanel = new IntroPanel();
				instructPanel = new InstructionPanel();
				roomPanel = new RoomPanel();
				addToFrame(introPanel, frame);
			}
		});	
	}
	
	private void addToFrame(JComponent p, JFrame thisFrame) {
		thisFrame.getContentPane().add(p);
		thisFrame.getContentPane().validate();
		p.requestFocusInWindow();
		thisFrame.getContentPane().repaint();
	}
	
	private void removeFromFrame(JComponent p, JFrame thisFrame) {
		thisFrame.getContentPane().remove(p);
		thisFrame.getContentPane().validate();
		thisFrame.getContentPane().repaint();
	}
	
	class IntroPanel extends JPanel {
		JLabel imageLabel;
		Timer imgTimer;
		JButton play, instructions, quit, register, levelMaker, forceConnect;
		
		public IntroPanel() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			play = new JButton("Play");
			instructions = new JButton("Instructions");
			quit = new JButton("Quit");
			levelMaker = new JButton("Level Maker");
			register = new JButton("New Account");
			forceConnect = new JButton("Connect to Master Server");
			setUpButtons(play);
			setUpButtons(instructions);
			setUpButtons(levelMaker);
			setUpButtons(register);
			setUpButtons(forceConnect);
			setUpButtons(quit);
			
			play.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(!masterConnector.isConnected()) {
							connectToMaster();
						}
						if(GameSettings.DEBUG_MODE) {
							removeFromFrame(introPanel, frame);
							addToFrame(roomPanel, frame);
							return;
						}
						if(!status.isLoginSuccess()) {
							if(sendCredentials()) {
								removeFromFrame(introPanel, frame);
								addToFrame(roomPanel, frame);
							}
						}
					}
				});
			
			instructions.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						frame.getContentPane().remove(introPanel);
						frame.getContentPane().add(instructPanel);
						frame.getContentPane().validate();
						frame.getContentPane().repaint();
					}
				});
			
			quit.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						frame.dispose();
						System.exit(0);
					}
				});

			levelMaker.addActionListener(new ActionListener() {						
						@Override
						public void actionPerformed(ActionEvent arg0) {
							new LevelMaker(frame);		
						}
				});
			
			register.addActionListener(new ActionListener() {						
				@Override
				public void actionPerformed(ActionEvent evt) {
					
				}
			});
			
			forceConnect.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					if(!masterConnector.isConnected()) {
						connectToMaster();
					} else {
						JOptionPane.showMessageDialog(frame,
								"Already connected to Master Server");
					}
				}
			});
		}
		
		public void setUpButtons(JButton button) {
			// method for adding buttons and making them uniform
			button.setForeground(Color.LIGHT_GRAY);
			button.setBackground(Color.BLACK);
			Dimension minSize = new Dimension(5, 20);
			Dimension prefSize = new Dimension(5, 20);
			Dimension maxSize = new Dimension(Short.MAX_VALUE, 20);
			
			
			button.setAlignmentX(Component.CENTER_ALIGNMENT);
			add(new Box.Filler(minSize, prefSize, maxSize));
			add(button);
			
		}
		
		public void paintComponent(Graphics g) {
			// changes as user slides JSlider over choices
			super.paintComponent(g);
		}
	}
	
	class InstructionPanel extends JPanel {
		JButton back;
		JScrollPane scroller;
		JTextArea area;
		Font instructFont;
		public InstructionPanel() {
			// initialize a scrollable JTextArea for the instructions
			setLayout(new BorderLayout());
			back = new JButton("Back");
			back.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					removeFromFrame(instructPanel, frame);
					addToFrame(introPanel, frame);
				}
			});
			instructFont = new Font("Sans Serif", Font.PLAIN, 36);
			area = new JTextArea(20, 20);
			area.setLineWrap(true);
			area.setWrapStyleWord(true);
			area.setEditable(false);
			area.setBackground(Color.lightGray);
			area.setForeground(Color.BLACK);
			setAreaText();
			scroller = new JScrollPane(area);
			add(back, BorderLayout.NORTH);
			add(scroller, BorderLayout.CENTER);
			setBackground(Color.LIGHT_GRAY);
		}
		
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
		}
		
		public void setAreaText() {
			// load the text instructions from a file
			BufferedReader fileReader = null;
			String text = null;
			try {	
				fileReader = new BufferedReader(new FileReader("instructions.txt"));
				text = fileReader.readLine();
			} catch(Exception e) {e.printStackTrace();}
			area.setFont(instructFont);
			area.setText(text);
		}
	}
	
	class RoomPanel extends JPanel {
		JTable gameList;
		GameOptions options;
		String[] cols = {"Room ID#","Room IP","Room Port","Room Name","Map"};
		TableModel model;
		DefaultTableModel dModel;
		int selectionID, selectionPort;
		String selectionAddr;
		
		public RoomPanel() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			dModel = new DefaultTableModel(0, cols.length) ;
			dModel.setColumnIdentifiers(cols);
			gameList = new JTable(dModel) {
				public boolean isCellEditable(int row, int column) {
					return false;
				};
			};
			model = gameList.getModel();
			//gameList.setEnabled(false);
			options = new GameOptions();
			JScrollPane tableScroller = new JScrollPane(gameList);
			add(tableScroller);
			add(options);
		}		
		
		class GameOptions extends JPanel {
			JButton startGame, refreshList, connectToMaster, createRoom, back;
			public GameOptions() {
				startGame = new JButton("Enter Room");
				refreshList = new JButton("Get Room List");
				createRoom = new JButton("Create room");
				back = new JButton("Back to Main Menu");
				add(back);
				add(createRoom);
				add(refreshList);
				add(startGame);
				
				back.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						removeFromFrame(roomPanel, frame);
						addToFrame(introPanel, frame);
					}
				});
				
				startGame.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent evt) {
						boolean selected = false;
						int row = gameList.getSelectedRow();
						if(row != -1) {
							String data[] = new String[model.getColumnCount()];
							for(int col = 0; col < cols.length; col++) {
								data[col] = (String) model.getValueAt(row, col);
							}
							selectionID = Integer.parseInt(data[0]);
							selectionAddr = data[1];
							selectionPort = Integer.parseInt(data[2]);
							gamePanel = new GamePanel(
									data[3], 
									data[4]);
							removeFromFrame(roomPanel, frame);
							addToFrame(gamePanel, frame);
							SwingUtilities.invokeLater(new Runnable() {		
								@Override
								public void run() {
									initGame(selectionAddr, selectionPort);		
									new Thread(new ServerListener()).start();
									new Thread(new ServerUpdater()).start();
									inGame = true;
								}
							});
						} else JOptionPane.showMessageDialog(frame, "No room selected");
					}
				});
				
				createRoom.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent evt) {
	
					}
				});
				
				refreshList.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						
						GameUtils.writeLine(masterWriter, Command.REQ_ROOMS);
						
						DefaultTableModel model = (DefaultTableModel) gameList.getModel();
						for(int i = 0; i < model.getRowCount(); i++) 
							model.removeRow(i);
						String reply = null;
						String[] data;			
						try {
							for(int row = 0; 
							(reply = masterReader.readLine()) != null;
							row++) {
								System.out.println(reply);
								if(reply.startsWith(Command.IN_LIST)) {
									data = reply.split("!")[2].split(",");
									model.insertRow(row, 
											new String[] {data[0],data[1],data[2]
											              ,data[3],data[4]});
								} else if(reply.startsWith(Command.END_OF_LIST)) {
									System.out.println("NUMBER OF ROOMS: " + row);
									System.out.println("--END LIST--");
									break;
								}
							}
							//gameList = new JTable(model);
						} catch (IOException e1) {
							
							e1.printStackTrace();
						}
					}
				});
			}
			
			class NewRoomDialog extends JDialog {
				
			}
		}
	}
	
	class GamePanel extends JPanel {
		char[][] currLevel;
		Terrain[][] map;
		float ACTUAL_TIME_ELAPSED;
		long numFrames, startTime, currTime;
		
		public GamePanel(String roomName, String mapName) {
			setFocusable(true);
			setBackground(Color.BLUE);
			addKeyListener(new KeyListener() {	
				boolean wasPressed;
				@Override
				public void keyTyped(KeyEvent evt) {
				}
				
				@Override
				public void keyReleased(KeyEvent evt) {
					switch(evt.getKeyCode()) {
					case KeyEvent.VK_RIGHT:
					case KeyEvent.VK_LEFT:
						yourPlayer.movLeft = false;
						yourPlayer.movRight = false;
						break;
					case KeyEvent.VK_UP: 
					case KeyEvent.VK_DOWN: 
						yourPlayer.movUp = false;
						yourPlayer.movDown = false;	
						break;
					}
					sendPlayerInfo(null);
					wasPressed = false;
				}
				
				@Override
				public void keyPressed(KeyEvent evt) {
					if(!wasPressed) {
						switch(evt.getKeyCode()) {
							case KeyEvent.VK_RIGHT:
								yourPlayer.movRight = true;
								yourPlayer.movLeft = false;
								break;
							case KeyEvent.VK_LEFT:
								yourPlayer.movLeft = true;
								yourPlayer.movRight = false;
								break;
							case KeyEvent.VK_UP:
								yourPlayer.movUp = true;
								yourPlayer.movDown = false;
								break;
							case KeyEvent.VK_DOWN:
								yourPlayer.movUp = false;
								yourPlayer.movDown = true;
								break;
							case KeyEvent.VK_ESCAPE:
								if(inGame)
									requestExit();
								break;
						}
						sendPlayerInfo(null);
					}	
				}
			});
			addMouseListener(new MouseListener() {
				@Override
				public void mouseReleased(MouseEvent evt) {
					
				}
				
				@Override
				public void mousePressed(MouseEvent evt) {
					//int x = evt.getX();
					//int y = evt.getY();
					fireWeapon(evt.getPoint());	
				}
				
				@Override
				public void mouseExited(MouseEvent evt) {
					
				}
				
				@Override
				public void mouseEntered(MouseEvent evt) {
					
				}
				
				@Override
				public void mouseClicked(MouseEvent evt) {
					
				}
			});

			players = new ArrayList<Player>();
			bullets = new CopyOnWriteArrayList<Bullet>();
			ids = new HashMap<Integer, Player>();
			yourPlayer = new Player((int)(Math.random()*730)+40, 
					(int)(Math.random()*530)+40, GameSettings.PLAYER_SIZE, 
					GameSettings.PLAYER_SIZE, -1, 100, 3, Color.GREEN);
			yourPlayer.onGround = true;	
			frame.setTitle("Game Client: " + yourPlayer.getPlayerID() +
					" in room " + roomName + " playing on " + mapName);
			players.add(yourPlayer);		
			loadLevel(this.getClass()
					.getResource("/data/map1.txt")	);
			
			new Thread(new GameLoop()).start();
		}
		
		class GameLoop implements Runnable {
			@Override
			public void run() {
				int[] cenRowAndCol = new int[2];
				int[] bulletRowCol = new int[2];
				Terrain[][] surroundings;
				startTime = System.currentTimeMillis();
				while(true) {	
						yourPlayer.setPoints();
						cenRowAndCol = getCenterTileRowColVeryImproved(yourPlayer.center);
						surroundings = calcSurroundings(cenRowAndCol);
						//System.out.println("ROW/COL: " + cenRowAndCol[0] + ", " + cenRowAndCol[1]);
						if(yourPlayer.movRight) {
							if(!(surroundings[1][2].getType() == TerrainType.WALL &&
									(surroundings[1][2].contains(yourPlayer.topRight) ||
									surroundings[1][2].contains(yourPlayer.bottomRight)))) {
								yourPlayer.x+=yourPlayer.speed;
								yourPlayer.movRightBlocked = false;
							} else yourPlayer.movRightBlocked = true;
							
						} else if(yourPlayer.movLeft) {
							if(!(surroundings[1][0].getType() == TerrainType.WALL &&
									(surroundings[1][0].contains(yourPlayer.topLeft) ||
									surroundings[1][0].contains(yourPlayer.bottomLeft)))) {
								yourPlayer.x-=yourPlayer.speed;
								yourPlayer.movLeftBlocked = false;
							} else yourPlayer.movLeftBlocked = true;
						} 
						if(yourPlayer.movUp) {
							if(!(surroundings[0][1].getType() == TerrainType.WALL &&
									(surroundings[0][1].contains(yourPlayer.topRight) ||
									surroundings[0][1].contains(yourPlayer.topLeft)))) {
								yourPlayer.y-=yourPlayer.speed;
								yourPlayer.movUpBlocked = false;
							} else yourPlayer.movUpBlocked = true;
						} else if(yourPlayer.movDown) {
							if(!(surroundings[2][1].getType() == TerrainType.WALL &&
									(surroundings[2][1].contains(yourPlayer.bottomRight) ||
									surroundings[2][1].contains(yourPlayer.bottomLeft)))) {
								yourPlayer.y+=yourPlayer.speed;
								yourPlayer.movDownBlocked = false;
							} else yourPlayer.movDownBlocked = true;
						}
						/*System.out.println("---------");
						System.out.println("\n movright: " + yourPlayer.movRight);
						System.out.println(" movleft: " + yourPlayer.movLeft);
						System.out.println(" movup: " + yourPlayer.movUp);
						System.out.println(" movdown: " + yourPlayer.movDown);
						System.out.println(" movrightblo: " + yourPlayer.movRightBlocked);
						System.out.println(" movleftblo: " + yourPlayer.movLeftBlocked);
						System.out.println(" movupblo: " + yourPlayer.movUpBlocked);
						System.out.println(" movdownblo: " + yourPlayer.movDownBlocked);*/
						
						if(!players.isEmpty())
						for(Player otherPlayer: players) {
							if(yourPlayer.equals(otherPlayer)) continue;
							if(otherPlayer.movRight && !otherPlayer.movRightBlocked) {
								otherPlayer.x+=otherPlayer.speed;
							} 
							if(otherPlayer.movLeft && !otherPlayer.movLeftBlocked) {
								otherPlayer.x-=otherPlayer.speed;
							}
							if(otherPlayer.movUp && !otherPlayer.movUpBlocked) {
								otherPlayer.y-=otherPlayer.speed;
							}
							if(otherPlayer.movDown && !otherPlayer.movDownBlocked) {
								otherPlayer.y+=otherPlayer.speed;
							}
						}
						
						if(!bullets.isEmpty())
							for(Bullet currBull: bullets) {
								currBull.move();
								bulletRowCol 
									 = getCenterTileRowColVeryImproved(currBull.center);
								if(!inMapRange(currBull.center, bulletRowCol) ||
										map[bulletRowCol[0]][bulletRowCol[1]].getType() 
										== TerrainType.WALL) {
									bullets.remove(currBull);
									continue;
								}
								
								if(currBull.intersects(yourPlayer) &&
										currBull.getParent() != yourPlayer) {
									yourPlayer.health-=currBull.damage;
									bullets.remove(currBull);
									sendPlayerInfo(null);
								} else if(currBull.bullet_life <= 0) {
									bullets.remove(currBull);
								}
							}
						
						if(yourPlayer.movUp || 
								yourPlayer.movDown || 
								yourPlayer.movRight || 
								yourPlayer.movLeft) sendUpdates = true;
						//else moving = false;
						currTime = System.currentTimeMillis();
						ACTUAL_TIME_ELAPSED = 
							1.0f/(((float)numFrames/(currTime-startTime))*1000);
							if(Float.isInfinite(ACTUAL_TIME_ELAPSED) || 
									Float.isNaN(ACTUAL_TIME_ELAPSED))
								ACTUAL_TIME_ELAPSED = 0.016f;
							
							if(numFrames < Long.MAX_VALUE)
								numFrames++;
							else numFrames = 0;
						repaint();	
					try {
						Thread.sleep(GameSettings.GAME_DELAY);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		public void paintComponent(Graphics gr) {
			super.paintComponent(gr);
			Graphics2D g = (Graphics2D) gr;
			drawMap(g);
			drawPlayers(g);
			drawBullets(g);
			drawStats(g);
		}
		
		public void loadLevel(URL level) {
				try {
						Scanner levelReader = null;
						BufferedReader fileChecker = new BufferedReader(
								new FileReader(new File(level.toURI())));
						int width = 0, height = 0;
				        String line = "";
				        while ((line = fileChecker.readLine()) != null) {
				                    if (width == 0)
				                    {
				                        String[] str = line.split(" ");
				                        width = str.length;
				                    }
				            height++;
				        }
				        System.out.println(height + " " + width);
				        mapHeight = height;
				        mapWidth = width;
				        currLevel = new char[height][width];
				        map = new Terrain[height][width];
				        int colLength = currLevel[0].length;
				        
				        //System.out.println(height + " " +  width);
				        levelReader = new Scanner(new File(level.toURI()));
						int row = 0;
						int col = 0;
						while(levelReader.hasNext()) {
							char block = (char)levelReader.next().codePointAt(0);
							if(block != ' ') {
								currLevel[row][col] = block;
								col++;
							}
							if(col == colLength) { 
								col = 0;
								row++;
							}
						}
				} catch( Exception e) {e.printStackTrace();}

					for( int row = 0; row < currLevel.length; row++ ) {
						for( int col = 0; col < currLevel[row].length; col++ ) {
							map[row][col] = new Terrain(
								col*GameSettings.BLOCK_SIZE,
								row*GameSettings.BLOCK_SIZE,
								GameSettings.BLOCK_SIZE, 
								GameSettings.BLOCK_SIZE, 
								TerrainType.getType(currLevel[row][col]));
						}
					}		
		}
		
		public Terrain[][] calcSurroundings(int[] rowAndCol) {
			Terrain[][] surround= new Terrain[3][3];
			int cenRow = 0;
			int cenCol = 0;
			cenRow = rowAndCol[0];
			cenCol = rowAndCol[1];
			surround[0][0] = map[cenRow-1][cenCol-1];
			surround[0][1] = map[cenRow-1][cenCol];
			surround[0][2] = map[cenRow-1][cenCol+1];
			surround[1][0] = map[cenRow][cenCol-1];
			surround[1][1] = map[cenRow][cenCol];
			surround[1][2] = map[cenRow][cenCol+1];
			surround[2][0] = map[cenRow+1][cenCol-1];
			surround[2][1] = map[cenRow+1][cenCol];
			surround[2][2] = map[cenRow+1][cenCol+1];
				/*surround[0][0] = levelTiles[cenRow-2][cenCol-2];
				surround[0][1] = levelTiles[cenRow-2][cenCol-1];
				surround[0][2] = levelTiles[cenRow-2][cenCol];
				surround[0][3] = levelTiles[cenRow-2][cenCol+1];
				surround[0][4] = levelTiles[cenRow-2][cenCol+2];
				surround[1][0] = levelTiles[cenRow-1][cenCol-2];
				surround[1][1] = levelTiles[cenRow-1][cenCol-1];
				surround[1][2] = levelTiles[cenRow-1][cenCol];
				surround[1][3] = levelTiles[cenRow-1][cenCol+1];
				surround[1][4] = levelTiles[cenRow-1][cenCol+2];
				surround[2][0] = levelTiles[cenRow][cenCol-2];
				surround[2][1] = levelTiles[cenRow][cenCol-1];
				surround[2][2] = levelTiles[cenRow][cenCol];
				surround[2][3] = levelTiles[cenRow][cenCol+1];
				surround[2][4] = levelTiles[cenRow][cenCol+2];
				surround[3][0] = levelTiles[cenRow+1][cenCol-2];
				surround[3][1] = levelTiles[cenRow+1][cenCol-1];
				surround[3][2] = levelTiles[cenRow+1][cenCol];
				surround[3][3] = levelTiles[cenRow+1][cenCol+1];
				surround[3][4] = levelTiles[cenRow+1][cenCol+2];
				surround[4][0] = levelTiles[cenRow+2][cenCol-2];
				surround[4][1] = levelTiles[cenRow+2][cenCol-1];
				surround[4][2] = levelTiles[cenRow+2][cenCol];
				surround[4][3] = levelTiles[cenRow+2][cenCol+1];
				surround[4][4] = levelTiles[cenRow+2][cenCol+2];*/

			return surround;
		}
		
		public int[] getCenterTileRowColVeryImproved(Point p) {
			int x = p.x;
			int y = p.y;
			int upperLeftX = map[0][0].x;
			int upperLeftY = map[0][0].y;
			int[] rowAndCol = new int[2];
			rowAndCol[0] = (Math.abs(upperLeftY-y)/GameSettings.BLOCK_SIZE);
			rowAndCol[1] = (Math.abs(upperLeftX-x)/GameSettings.BLOCK_SIZE);
			
			return rowAndCol;
		}
		
		public void drawMap(Graphics2D g) {
			Terrain t;
			for( int row = 0; row < map.length; row++ ) {
				for( int col = 0; col < map[row].length; col++ ) {
					t = map[row][col];
					g.setColor(t.color);
					g.fillRect(col*GameSettings.BLOCK_SIZE, 
							row*GameSettings.BLOCK_SIZE, 
							GameSettings.BLOCK_SIZE,
							GameSettings.BLOCK_SIZE);
					if(GameSettings.DEBUG_MODE ) {
						g.setColor(Color.RED);
						g.drawLine(t.center.x, t.center.y, t.center.x, t.center.y);
					}
				}
			}	
		}
		
		public void drawPlayers(Graphics2D g) {
			for(Player player: players) {
				g.setColor(player.getColor());
				g.fill(player);
				g.setColor(Color.RED);
				if(GameSettings.DEBUG_MODE) {
					g.fillRect(yourPlayer.bottomLeft.x, 
						yourPlayer.bottomLeft.y,
						2,2);	
					g.fillRect(yourPlayer.bottomRight.x, 
						yourPlayer.bottomRight.y,
						2,2);
					g.fillRect(yourPlayer.topRight.x, 
						yourPlayer.topRight.y,
						2,2);
					g.fillRect(yourPlayer.topLeft.x, 
						yourPlayer.topLeft.y,
						2,2);
				}
				if(player == yourPlayer) continue;
				g.setColor(Color.GREEN);
				g.drawString("" + player.health, 
						player.x-4, player.y-8);
					
				/*g.fillRect(player.x-2, player.y-2, 
						(int)(player.health/5), 3);*/
			}
		}
		
		public void drawBullets(Graphics2D g) {
			for(Bullet bullet: bullets) {
				g.setColor(bullet.color);
				g.fill(bullet);
			}
		}
	
		public void drawStats(Graphics2D g) {
			g.setColor(Color.GREEN);
			g.fillRect(54, 4, (int)(yourPlayer.health*1.5), 16);
			g.setColor(Color.RED);
			g.drawString("Health: ", 10, 16);
			g.setColor(Color.BLACK);
			g.drawString("" + yourPlayer.health, 85, 16);
		}
	
		public boolean inMapRange(Point p, int[] rowCol) {
			if(rowCol[0] <= 0 || rowCol[0] >= mapHeight ||
					rowCol[1] <= 0 || rowCol[1] >= mapWidth) {
				return false;
			}
			return true;
		}
	}
	
	public void initGame(String roomAddress, int roomPort) {
		
		try {
			masterConnector = new Socket(roomAddress, roomPort);
			if(masterConnector.isConnected()) {
				roomWriter = new BufferedWriter(
							new OutputStreamWriter(masterConnector.getOutputStream()));
				roomReader = new BufferedReader(
							new InputStreamReader(masterConnector.getInputStream()));
				requestEntry();		
				if(yourPlayer.getPlayerID() != -1) System.out.println("ACCEPTED INTO GAME");
			} else System.err.println("NOT CONNECTED TO SERVER");
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	public void registerPlayer(Player p) {
		players.add(p);
		ids.put(p.getPlayerID(), p);
	}
	
	class ServerListener implements Runnable {
		String incoming;
		@Override
		public void run() {
			try {
				while(true) {
					if((incoming = roomReader.readLine()) != null) {
						parseCommand(incoming);
					}
				}
			} catch (IOException e) {
				if(endListening) {
					System.out.println("END LISTENING");
					return;
				}
				closeRoom();
				JOptionPane.showMessageDialog(frame, "Room closed");
				CLILOG.log(Level.INFO, incoming);
				e.printStackTrace();	
			}
		}
	}
	
	class ServerUpdater implements Runnable {
		public ServerUpdater() {
			try {			
				System.out.println("Connected to Server on " +
						masterConnector.getLocalPort());
			} catch (Exception e) {
				e.printStackTrace();
			}		
		}
		@Override
		public void run() {
			String info = null;
			while(true) {
				try {
					//sendPlayerInfoSync(info);
					if(sendUpdates) {
						sendPlayerInfo(info);
					}
					Thread.sleep(GameSettings.THREAD_IO_DELAY);
				} catch (Exception e) {
					closeRoom();
					JOptionPane.showMessageDialog(frame, "Room closed");
					e.printStackTrace();
				}	
				if(endUpdating) break;
			}
			System.out.println("END UPDATING");
			return;
		}	
	}

	public void parseCommand(String command) {
		if(command.trim().isEmpty()) return;
		String[] cmd = command.split("!");
		String data = null;
		try {
			data = cmd[2];
		} catch(Exception e) {
			System.out.println("ERROR ON COMMAND: " + command);
		}
		
		if(cmd[0].equals(Command.ROOM_ENTERED)) {
			yourPlayer.setPlayerID(Integer.parseInt(data));
			ids.put(yourPlayer.getPlayerID(), yourPlayer);
			
		} else if(cmd[0].equals(Command.PLAYER_EXITED)) {
			Player toRemove = ids.remove(Integer.parseInt(data));
			System.out.println(toRemove.getPlayerID() + " HAS EXITED.");
			players.remove(toRemove);
		} else {
			if(yourPlayer.getPlayerID() != -1) {
				int whichPlayer = Integer.parseInt(cmd[1]);
				if(yourPlayer.getPlayerID() == whichPlayer) return;
				String[] info = data.split(",");
				if(cmd[0].equals(Command.NEWCOMER)) {
					registerPlayer(new Player(
							Integer.parseInt(info[0]), Integer.parseInt(info[1]), 
							GameSettings.PLAYER_SIZE, GameSettings.PLAYER_SIZE,
							whichPlayer, Integer.parseInt(info[2]), Integer.parseInt(info[3]), Color.RED));
				} else if(cmd[0].equals(Command.INFO)) {
					Player pToMove = null;
					pToMove = ids.get(whichPlayer);
					pToMove.movRight = Boolean.parseBoolean(info[0]);
					pToMove.movLeft = Boolean.parseBoolean(info[1]);
					pToMove.movUp = Boolean.parseBoolean(info[2]);
					pToMove.movDown = Boolean.parseBoolean(info[3]);
					pToMove.movRightBlocked = Boolean.parseBoolean(info[4]);
					pToMove.movLeftBlocked = Boolean.parseBoolean(info[5]);
					pToMove.movUpBlocked = Boolean.parseBoolean(info[6]);
					pToMove.movDownBlocked = Boolean.parseBoolean(info[7]);
					/*pToMove.setLocation(Integer.parseInt(info[0]), 
							Integer.parseInt(info[1]));*/
					pToMove.health = Integer.parseInt(info[8]);
				} else if(cmd[0].equals(Command.SYNC_INFO)) {
					Player pToMove = null;
					pToMove = ids.get(whichPlayer);
					pToMove.setLocation(Integer.parseInt(info[0]), 
							Integer.parseInt(info[1]));
					//pToMove.health = Integer.parseInt(info[10]);
				} else if(cmd[0].equals(Command.EXISTING)) {
					registerPlayer(new Player(
							Integer.parseInt(info[0]), 
							Integer.parseInt(info[1]),  GameSettings.PLAYER_SIZE, 
							GameSettings.PLAYER_SIZE, whichPlayer, 
							Integer.parseInt(info[2]), Integer.parseInt(info[3]),
							Color.RED));
				} else if(cmd[0].equals(Command.WEP_FIRE)) {
					Player firingPlayer = ids.get(whichPlayer);
					WeaponType firingWep = 
						WeaponType.valueOf(info[0]);
	
					BulletCombo.spawnShot(firingWep, 
							bullets, firingPlayer, new Point(
									Integer.parseInt(info[1]), 
									Integer.parseInt(info[2])));
				}  else if(cmd[0].equals(Command.SERV_LIM_REACHED)) {
					System.out.println("SERVER IS AT MAX CAPACITY: " + data);
					requestExit();
				}
			}
		}
	}
	
	public void connectToMaster() {
		if(!masterConnector.isConnected()) {
			try {
				masterConnector = new Socket(MASTER_ADDRESS.getAddress(),
											MASTER_ADDRESS.getPort());
				//masterConnector.connect(MASTER_ADDRESS);
				masterWriter = new BufferedWriter(
						new OutputStreamWriter(masterConnector.getOutputStream()));
				masterReader = new BufferedReader(
						new InputStreamReader(masterConnector.getInputStream()));
			} catch (IOException e) {
				JOptionPane.showMessageDialog(frame, 
				"Couldn't connect to Master Server");
				e.printStackTrace();
			}	
		}
	}
	
	public boolean getCredentials() {
		JLabel label = new JLabel("Please enter your username:");
		JTextField jf = new JTextField(28);
		int option;
		do {
			option = JOptionPane.showConfirmDialog(frame,
					new Object[]{label, jf}, "Username:",
					JOptionPane.OK_CANCEL_OPTION);
			
			if(option == JOptionPane.OK_OPTION) {
				status.setUsername(jf.getText());
			} else  {
				return false;	
			}
		}  while(!(jf.getText().trim().length() > 0));
		
		label = new JLabel("Please enter your password:");
		JPasswordField jpf = new JPasswordField();
		do {
			option = JOptionPane.showConfirmDialog(frame,
					new Object[]{label, jpf}, "Password:",
					JOptionPane.OK_CANCEL_OPTION);
			
			if(option == JOptionPane.OK_OPTION) {
				status.setPass(new String(jpf.getPassword()));
				return true;
			}
			 else  {
				return false;	
			 }
		} while(!(new String(jpf.getPassword()).trim().length() > 0));
	}
	
	public boolean sendCredentials() {
		if(!getCredentials()) return false;
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
					//connectToServer();
					GameUtils.writeLine(masterWriter, Command.LOGIN_CRED 
							+ "!!" + status.getUsername() 
							+ "," + status.getPass());
						String reply;
						try {
							if((reply = masterReader.readLine()) != null) {
								if(reply.equals(Command.LOGIN_SUCC)) {
									JOptionPane.showMessageDialog(frame, 
											"Login success");
									status.setLoginSuccess(true);
								} else if(reply.equals(Command.LOGIN_FAIL)){
									JOptionPane.showMessageDialog(frame, 
											"Login fail");
									status.setLoginSuccess(false);
								}
							}
						} catch (HeadlessException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}		
			}
		});
		return status.isLoginSuccess();
	}
	
	public void sendPlayerInfo(String data) {
		data = "";
		data = Command.INFO + "!" + yourPlayer.getPlayerID() + "!"
			+ yourPlayer.movRight + "," 
			+ yourPlayer.movLeft + ","
			+ yourPlayer.movUp + ","
			+ yourPlayer.movDown + ","
			+ yourPlayer.movRightBlocked + "," 
			+ yourPlayer.movLeftBlocked + ","
			+ yourPlayer.movUpBlocked + ","
			+ yourPlayer.movDownBlocked + ","
			+ yourPlayer.health;
		synchronized(this) {
			GameUtils.writeLine(roomWriter, data);
		}
	}
	
	public void sendPlayerInfoSync(String data) {
		data = "";
		data = Command.SYNC_INFO+ "!" + yourPlayer.getPlayerID() + "!"
			+ yourPlayer.x + "," + yourPlayer.y;
		synchronized(this) {
			GameUtils.writeLine(roomWriter, data);
		}
	}
	
	public void requestEntry() {
		String entryRequest = Command.REQ_ENTRY + "!" 
			+ yourPlayer.getPlayerID() + "!" + yourPlayer.x + "," 
			+ yourPlayer.y + "," + yourPlayer.health
			+ "," + yourPlayer.speed;
		GameUtils
			.writeLine(roomWriter, entryRequest);
	}
	
	public void requestExit() {
		sendUpdates = false;
		endListening = true;
		endUpdating = true;
		
		String exitRequest = Command.REQ_EXIT + "!!" 
					+ yourPlayer.getPlayerID();
		System.out.println(exitRequest);
		GameUtils.writeLine(roomWriter, exitRequest);
		closeRoom();
			
	}
	
	public void fireWeapon(Point target) {
		String weaponFired = Command.WEP_FIRE + "!" + yourPlayer.getPlayerID()
						+ "!" + yourPlayer.getCurrWeapon() + "," +
						target.x + "," + target.y;
		GameUtils.writeLine(roomWriter, weaponFired);	
		/*bullets.add(new Bullet(yourPlayer.x, yourPlayer.y,
				10, 10, Color.CYAN, target.x, target.y, yourPlayer));*/
		BulletCombo.spawnShot(yourPlayer.getCurrWeapon(), 
				bullets, yourPlayer, target);
		System.out.println(weaponFired);
	}

	public void closeConnToMaster() {
		try {
			masterConnector.close();
			masterWriter.close();
			masterReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	public void closeRoom() {
		try {
			roomWriter.close();
			masterConnector.close();
			roomReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				removeFromFrame(gamePanel, frame);
				gamePanel = null;
				addToFrame(roomPanel, frame);
				inGame = false;
			}
		});
	}
}

