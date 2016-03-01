/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package example;

//import iceworld.given.IcetizenLook;
//import iceworld.given.MyIcetizen;

//import java.awt.Dimension;
//import java.awt.Toolkit;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import iceworld.given.ICEWorldImmigration;
import controller.KeyAdapter;
import controller.MenuButtonListener;
import view.EngineWindow;
import view.menu.MenuGroup;
import view.menu.MenuItem;

/**
 *
 * @author Wouter
 */
public class TestEngineWindow extends EngineWindow {

	
	
	//Prinn start

		private		JPanel		panel1;
		private		JPanel		panel2;
		private		JPanel		panel3;
		private		JPanel		panel4;
		private		JPanel		panel5;
		private		JPanel		panel6;
		private		JFrame		frame1;
		private JLabel author_image;
		private JDialog helpJDialog;
		private JDialog HTMLJDialog;
		private JEditorPane jep;
		Font font = new Font("TimesRoman",Font.BOLD,18);
		//Prinn end
		BufferedImage sky=null;
		ImageIcon skyIcon;
	static JMenuBar menuBar;
	static JMenu file,about;
	static JMenuItem exitItem,helpItem,aboutItem,customizationItem;
	/**
	 * 
	 */
	Toolkit toolkit = Toolkit.getDefaultToolkit(); // get screen size
	Dimension screensize = toolkit.getScreenSize(); // get screen size
	private static final long serialVersionUID = 1L;
	TestEngineWindow engineWindow;
	/**
	 * Main Menu Group with Menu Items.
	 */
	MenuGroup mainMenuGroup;
	MenuItem newGame;
	MenuItem options;
	MenuItem quitGame;
	/**
	 * Options Menu Group with Menu Items.
	 */
	MenuGroup optionsGroup;
	MenuItem videoOptions = new MenuItem();
	MenuItem soundOptions = new MenuItem();
	MenuItem optionsBack = new MenuItem();
	/**
	 * Ingame Menu Group with Menu Items.
	 */
	MenuGroup ingameMenuGroup;
	MenuItem ingameResumeGame = new MenuItem();
	MenuItem ingameQuitGame = new MenuItem();

	Myicetz me = new Myicetz();
	int playerUID;
	ICEWorldImmigration immigration;

	public LaunchGame lg;

	/*
  public TestEngineWindow(Myicetz il, int u) {
    start();
    me=il;
    playerUID = u;
    printLook();
  }
	 */
	public TestEngineWindow() {
		setJMenuBar(createMenuBar());
		addListeners();
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				start();
			}
		});

	}
	public void setImmigration(ICEWorldImmigration immi){
		immigration = immi;
	}
	public void setIcetizen(Myicetz il){
		me=il;
	}
	public void setPlayerUID(int i){
		playerUID=i;
		System.out.println("*+-*+-*+*+-*"+playerUID+"*+-*+-*+*+-");
	}
	/*
  private void printLook(){
	  System.out.println(me.getIcetizenLook().gidB);
	  System.out.println(me.getIcetizenLook().gidH);
	  System.out.println(me.getIcetizenLook().gidS);
	  System.out.println(me.getIcetizenLook().gidW);
  }
	 */
	private void start() {
		engineWindow = this;
		// Semi-singleton
		EngineWindow.setInstance(this);
		initialise();
		showMainMenu();
		bypassMenu();
	}

	private void bypassMenu() {
		// TODO Auto-generated method stub
		System.out.println("Starting new game..");
		// Instantise a new game. ------> with all the iceworld stuff.
		lg = new LaunchGame(new UpdateGame(60));
		lg.setIcetizen(me);
		lg.setPlayerUID(playerUID);
		lg.setImmigration(immigration);
		lg.setTEW(this);
		System.out.println("============"+playerUID+"===========");
		EngineWindow.getInstance().removeAllMenus();
		//---------------loading
		
		try {
			   sky = ImageIO.read(new File("SplashScreen2.jpg"));
			  } catch (IOException e1) {
			   // TODO Auto-generated catch block
			   e1.printStackTrace();
			  }

			  sky = resize(sky,(int) screensize.getWidth(),(int) screensize.getHeight());
			  skyIcon = new ImageIcon(sky);
			  setContentPane(new JLabel(skyIcon));
		
		//---------------loading
			  //setContentPane(EngineWindow.getInstance().getCanvas());
		EngineWindow.getInstance().setGame(lg);
		//EngineWindow.getInstance().getGame().getPlayfieldGrid().setCurrentZoomLevel(101);
		// Starts the game
		new Thread(EngineWindow.getInstance().getGame()).start();
		EngineWindow.getInstance().getCanvas().addZoomButtons();
		EngineWindow.getInstance().getCanvas().setIcetz(me);
		EngineWindow.getInstance().getCanvas().setImmigration(immigration);
		//tew.getInstance().getGame().getPlayfieldGrid().setCurrentZoomLevel(101);
		//this.getGame().getPlayfieldGrid().setCurrentZoomLevel(101);

		//lg.testParsing("hello");
		this.getCanvas().validate();
		this.getCanvas().repaint();
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		try {
			javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
		//TestEngineWindow tew = new TestEngineWindow();
	}

	protected JMenuBar createMenuBar() {
		menuBar = new JMenuBar();
		//menuBar.setBackground(Color.BLACK);
		file = new JMenu("File");
		//file.setBackground(Color.black);
		about = new JMenu("Help");
		//about.setBackground(Color.BLACK);
		exitItem = new JMenuItem("Exit");
		helpItem = new JMenuItem("View Help");
		aboutItem = new JMenuItem("About");
		//customizationItem = new JMenuItem("IceTizen Customization");

		file.add(exitItem);
		about.add(helpItem);
		about.add(aboutItem);
		menuBar.add(file);
		menuBar.add(about);
		//about.add(customizationItem);
		add(menuBar);
		return menuBar;
	}

	private void addListeners()
	{ exitItem.addActionListener(new ActionListener()
	{
		public void actionPerformed(ActionEvent e)
		{
			int confirmed = JOptionPane.showConfirmDialog(null,
					"Are you sure you want to exit?", "User Confirmation",
					JOptionPane.YES_NO_OPTION);
			if (confirmed == JOptionPane.YES_OPTION)
				immigration.logout();
				System.exit(0);

		}
	});

	/*exitItem.addActionListener(new ActionListener()
	  {
	  public void actionPerformed(ActionEvent e)
	  {
	  System.exit(0);
	  }
	  });*/
	helpItem.setMnemonic(KeyEvent.VK_F1);
	helpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1,0));
	helpItem.addActionListener(new ActionListener()
	{
		public void actionPerformed(ActionEvent e)
		{
			JFrame helpJFrame = new JFrame("Help");
			helpJDialog = new JDialog(helpJFrame,"Help");
			//exit when press escape key
			/*
	  helpJDialog.addKeyListener(new KeyAdapter(){
	  public void keyPressed(KeyEvent ke) {
	  int key = ke.getKeyCode();
	  if(key == KeyEvent.VK_ESCAPE){
	  //System.out.println("escape pressed on about");
	  helpJDialog.setVisible(false);
	  helpJDialog.dispose();
	  }
	  }
	  });
			 */
			JTabbedPane helpTab = new JTabbedPane();
			JPanel helpPanel = new JPanel();
			//help.add(helpPanel, BorderLayout.CENTER);
			//helpPanel.setBackground(Color.BLUE);
			//helpPanel.add(helpTab);
			helpJDialog.setLayout(new BorderLayout());
			helpPanel.add(helpTab, BorderLayout.CENTER);
			//helpTab.addTab(title, component)
			// Create the tab pages
			
			createPage1();
			createPage2();
			createPage3();
			createPage4();
			createPage5();
			createPage6();
			
			// Create a tabbed pane

			helpTab.addTab( "Logging In/Out", panel1 );
			helpTab.addTab( "The look of things", panel2 );
			helpTab.addTab( "Customization", panel3 );
			helpTab.addTab( "Sounds", panel4 );
			helpTab.addTab( "File Transferring", panel5 );
			helpTab.addTab( "Don't know SHIT!", panel6 );

			/*
	  JPanel helpPanel = new JPanel();
	  help.add(helpPanel, BorderLayout.CENTER);
	  JTabbedPane helpTab = new JTabbedPane();
	  JPanel helpPage1 = new JPanel();
	  JPanel helpPage2 = new JPanel();
	  JPanel helpPage3 = new JPanel();
	  ImageIcon icon = new ImageIcon("mog.JPG");
	  helpPage1.setBackground(Color.BLACK);
	  helpTab.addTab("page1", icon, helpPage1, "Tab1" );
	  helpPanel.add(helpTab, BorderLayout.CENTER);
	  helpTab.addTab( "Page 1", helpPage1 );
	  helpTab.addTab( "Page 2", helpPage2 );
	  helpTab.addTab( "Page 3", helpPage3 );
	  //JPanel panel = new JPanel();
	  //JLabel help_text = new JLabel();
	  //help_text.setFont(font);
	  //help_text.setText("I can't help u");
	  help.setLayout(new BorderLayout());
	  //panel.add(help_text);
	  //help_text.setAlignmentX(JComponent.CENTER_ALIGNMENT);
	  //help.add(panel);
			 */
			//helpJDialog.setModal(true);
			helpJDialog.add(helpPanel,BorderLayout.CENTER);
			helpJDialog.setPreferredSize(new Dimension(1000,600));
			helpJDialog.pack();
			helpJDialog.setVisible(true);
			helpJDialog.setModal(false);
		}
	});
	aboutItem.setMnemonic(KeyEvent.VK_F2);
	aboutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2,0));
	aboutItem.addActionListener(new ActionListener()
	{
		public void actionPerformed(ActionEvent e)
		{
			JFrame aboutJFrame = new JFrame("About");
			/*
			aboutJFrame.addKeyListener(new KeyAdapter(){
				public void keyPressed(KeyEvent ke) {
					int key = ke.getKeyCode();
					if(key == KeyEvent.VK_ESCAPE){
						//System.out.println("escape pressed on about");
						aboutJFrame.setVisible(false);
						aboutJFrame.dispose();
					}
				}
			});
			 */
			BufferedImage myImage = null;
			try {
				myImage = ImageIO.read(new File("Pian.jpg"));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				//System.out.println("Cannot find image");
			}
			myImage = resize(myImage,500,700);

			ImageIcon imageIcon = new ImageIcon(myImage);

			//resizedIMage = imageIcon.getImage();

			JPanel imagePanel = new JPanel(new GridLayout(1,1,10,10));
			//image = ImageIO.read(new File("D:\\Project pictures\\Ja.jpg"));
			author_image = new JLabel(imageIcon);
			aboutJFrame.setLayout(new BorderLayout());
			imagePanel.add(author_image);
			//author_image.setAlignmentX(JComponent.CENTER_ALIGNMENT);
			//author_image.setFont(font);
			aboutJFrame.add(imagePanel);
			aboutJFrame.setPreferredSize(new Dimension(500,700));
			aboutJFrame.pack();
			aboutJFrame.setVisible(true);
		} 
	}
			);
	//customizationItem.setMnemonic(KeyEvent.VK_F3);
	//customizationItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3,0));
	//customizationItem.addActionListener(new ActionListener());
	}
	public static BufferedImage resize(BufferedImage image, int width, int height) {
		int type = image.getType() == 0? BufferedImage.TYPE_INT_ARGB : image.getType();
		BufferedImage resizedImage = new BufferedImage(width, height, type);
		Graphics2D g = resizedImage.createGraphics();
		g.setComposite(AlphaComposite.Src);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.drawImage(image, 0, 0, width, height, null);
		g.dispose();
		return resizedImage;
	} 
	@Override
	public void showMainMenu() {
		mainMenuGroup = new MenuGroup(this);
		// Height is 0 because it is managed by the Menu Group
		mainMenuGroup.setBounds((this.getWidth() / 2) - 200, (this.getHeight() / 2) - 125,
				400, 0);

		mainMenuGroup.setVisible(true);

		newGame = new MenuItem();
		newGame.setText("Start Simulation");
		newGame.addActionListener(new MenuButtonListener(1));

		options = new MenuItem();
		options.setText("Options");
		options.addActionListener(new MenuButtonListener(2));

		quitGame = new MenuItem();
		quitGame.setText("Quit Simulation");
		quitGame.addActionListener(new MenuButtonListener(3));

		mainMenuGroup.addMenuItem(newGame);
		mainMenuGroup.addMenuItem(options);
		mainMenuGroup.addMenuItem(quitGame);

		this.getCanvas().add(mainMenuGroup);
	}

	/**
	 * Re-creates and shows the options menu.
	 */
	public void showOptionsMenu() {
		optionsGroup = new MenuGroup(this);
		// Height is 0 because it is managed by the Menu Group
		optionsGroup.setBounds((this.getWidth() / 2) - 200, (this.getHeight() / 2) - 125,
				400, 0);

		optionsGroup.setVisible(true);

		videoOptions = new MenuItem();
		videoOptions.setText("Video Options");
		videoOptions.addActionListener(new MenuButtonListener(4));
		optionsGroup.addMenuItem(videoOptions);

		soundOptions = new MenuItem();
		soundOptions.setText("Sound Options");
		soundOptions.addActionListener(new MenuButtonListener(5));
		optionsGroup.addMenuItem(soundOptions);

		optionsBack = new MenuItem();
		optionsBack.setText("Back");
		optionsBack.addActionListener(new MenuButtonListener(6));
		optionsGroup.addMenuItem(optionsBack);


		this.getCanvas().add(optionsGroup);
		this.getCanvas().validate();
		this.getCanvas().repaint();
	}

	@Override
	public void showIngameMenu() {
		ingameMenuGroup = new MenuGroup(this);
		// Height is 0 because it is managed by the Menu Group
		ingameMenuGroup.setBounds((this.getWidth() / 2) - 200, (this.getHeight() / 2) - 125,
				400, 0);

		ingameMenuGroup.setVisible(true);

		ingameResumeGame = new MenuItem();
		ingameResumeGame.setText("Resume Simulation");
		ingameResumeGame.addActionListener(new MenuButtonListener(7));
		ingameMenuGroup.addMenuItem(ingameResumeGame);

		ingameQuitGame = new MenuItem();
		ingameQuitGame.setText("Quit Simulation");
		ingameQuitGame.addActionListener(new MenuButtonListener(3));
		ingameMenuGroup.addMenuItem(ingameQuitGame);


		this.getCanvas().add(ingameMenuGroup);
		this.getCanvas().validate();
		this.getCanvas().repaint();
	}
	
	
	public void createPage1()
	{

		panel1 = new JPanel();
		panel1.setLayout(null);
		panel1.setBackground(Color.CYAN);
		JLabel link = new JLabel("<html><u>Logging in/out</u></html>");
		link.setBounds(40,30,125,25);
		panel1.add(link);
		panel1.setPreferredSize(new Dimension(900,500));
		link.setForeground(Color.BLUE);
		link.setFont(font);
		
		BufferedImage charImage = null;
		try {
			charImage = ImageIO.read(new File("1.jpg"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			//System.out.println("Cannot find image");
		}
		charImage = resize(charImage,200,300);

		ImageIcon nattIcon = new ImageIcon(charImage);
		JLabel natt =new JLabel(nattIcon);
		natt.setBounds(480,100,200,300);
		panel1.add(natt);
		
		link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		link.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {

				jep = new JEditorPane();
				jep.setEditable(false);
				HTMLJDialog = new JDialog(frame1,"Help HTML5");
				try {
					jep.setPage(new File("1Logging InOut.html").toURI().toURL());
					jep.addHyperlinkListener(new HyperlinkListener(){
						public void hyperlinkUpdate(HyperlinkEvent event) {
							if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
								try {
									jep.setPage(event.getURL());
									//urlField.setText(event.getURL().toExternalForm());
								} catch(IOException ioe) {
									jep.setContentType("text/html");
									jep.setText("<html>Could not load</html>");
								}
							}	
						}

					});
				}catch (IOException e1) {
					jep.setContentType("text/html");
					jep.setText("<html>Could not load</html>");
				} 
				JScrollPane scrollPane = new JScrollPane(jep); 
				HTMLJDialog.getContentPane().add(scrollPane);
				HTMLJDialog.setPreferredSize(new Dimension(800,400));
				HTMLJDialog.pack();
				HTMLJDialog.setVisible(true);
				HTMLJDialog.setModal(false);
			}
		});


	}

	public void createPage2()
	{
		panel2 = new JPanel();
		panel2.setLayout(null);
		panel2.setBackground(Color.CYAN);
		JLabel link = new JLabel("<html><u>The look of things</u></html>");
		link.setBounds(40,30,160,25);
		panel2.add(link);
		panel2.setPreferredSize(new Dimension(900,500));
		link.setForeground(Color.BLUE);
		link.setFont(font);
		
		BufferedImage charImage = null;
		try {
			charImage = ImageIO.read(new File("2.jpg"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			//System.out.println("Cannot find image");
		}
		charImage = resize(charImage,200,300);

		ImageIcon nattIcon = new ImageIcon(charImage);
		JLabel natt =new JLabel(nattIcon);
		natt.setBounds(480,100,200,300);
		panel2.add(natt);
		
		link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		link.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {

				jep = new JEditorPane();
				jep.setEditable(false);
				HTMLJDialog = new JDialog(frame1,"Help HTML5");
				try {
					jep.setPage(new File("2The look of things.html").toURI().toURL());
					jep.addHyperlinkListener(new HyperlinkListener(){
						public void hyperlinkUpdate(HyperlinkEvent event) {
							if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
								try {
									jep.setPage(event.getURL());
									//urlField.setText(event.getURL().toExternalForm());
								} catch(IOException ioe) {
									jep.setContentType("text/html");
									jep.setText("<html>Could not load</html>");
								}
							}	
						}

					});
				}catch (IOException e1) {
					jep.setContentType("text/html");
					jep.setText("<html>Could not load</html>");
				} 
				JScrollPane scrollPane = new JScrollPane(jep);     
				HTMLJDialog.getContentPane().add(scrollPane);
				HTMLJDialog.setPreferredSize(new Dimension(800,400));
				HTMLJDialog.pack();
				HTMLJDialog.setVisible(true);
				HTMLJDialog.setModal(false);
			}
		});


	}

	public void createPage3()
	{
		panel3 = new JPanel();
		panel3.setLayout(null);
		panel3.setBackground(Color.CYAN);
		JLabel link = new JLabel("<html><u>Customization</u></html>");
		link.setBounds(40,30,125,25);
		panel3.add(link);
		panel3.setPreferredSize(new Dimension(900,500));
		link.setForeground(Color.BLUE);
		link.setFont(font);
		
		BufferedImage charImage = null;
		try {
			charImage = ImageIO.read(new File("3.jpg"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			//System.out.println("Cannot find image");
		}
		charImage = resize(charImage,200,300);

		ImageIcon nattIcon = new ImageIcon(charImage);
		JLabel natt =new JLabel(nattIcon);
		natt.setBounds(480,100,200,300);
		panel3.add(natt);
		
		link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		link.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {

				jep = new JEditorPane();
				jep.setEditable(false);
				HTMLJDialog = new JDialog(frame1,"Help HTML5");
				try {
					jep.setPage(new File("3Customization.html").toURI().toURL());
					jep.addHyperlinkListener(new HyperlinkListener(){
						public void hyperlinkUpdate(HyperlinkEvent event) {
							if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
								try {
									jep.setPage(event.getURL());
									//urlField.setText(event.getURL().toExternalForm());
								} catch(IOException ioe) {
									jep.setContentType("text/html");
									jep.setText("<html>Could not load</html>");
								}
							}	
						}

					});
				}catch (IOException e1) {
					jep.setContentType("text/html");
					jep.setText("<html>Could not load</html>");
				} 
				JScrollPane scrollPane = new JScrollPane(jep);     
				HTMLJDialog.getContentPane().add(scrollPane);
				HTMLJDialog.setPreferredSize(new Dimension(800,400));
				HTMLJDialog.pack();
				HTMLJDialog.setVisible(true);
				HTMLJDialog.setModal(false);
			}
		});

	}
	public void createPage4()
	{
		panel4 = new JPanel();
		panel4.setLayout(null);
		panel4.setBackground(Color.CYAN);
		JLabel link = new JLabel("<html><u>Sounds</u></html>");
		link.setBounds(40,30,75,25);
		panel4.add(link);
		panel4.setPreferredSize(new Dimension(900,500));
		link.setForeground(Color.BLUE);
		link.setFont(font);
		
		BufferedImage charImage = null;
		try {
			charImage = ImageIO.read(new File("4.jpg"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			//System.out.println("Cannot find image");
		}
		charImage = resize(charImage,200,300);

		ImageIcon nattIcon = new ImageIcon(charImage);
		JLabel natt =new JLabel(nattIcon);
		natt.setBounds(480,100,200,300);
		panel4.add(natt);
		
		link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		link.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {

				jep = new JEditorPane();
				jep.setEditable(false);
				HTMLJDialog = new JDialog(frame1,"Help HTML5");
				try {
					jep.setPage(new File("4Sounds.html").toURI().toURL());
					jep.addHyperlinkListener(new HyperlinkListener(){
						public void hyperlinkUpdate(HyperlinkEvent event) {
							if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
								try {
									jep.setPage(event.getURL());
									//urlField.setText(event.getURL().toExternalForm());
								} catch(IOException ioe) {
									jep.setContentType("text/html");
									jep.setText("<html>Could not load</html>");
								}
							}	
						}

					});
				}catch (IOException e1) {
					jep.setContentType("text/html");
					jep.setText("<html>Could not load</html>");
				} 
				JScrollPane scrollPane = new JScrollPane(jep);     
				HTMLJDialog.getContentPane().add(scrollPane);
				HTMLJDialog.setPreferredSize(new Dimension(800,400));
				HTMLJDialog.pack();
				HTMLJDialog.setVisible(true);
				HTMLJDialog.setModal(false);
			}
		});


	}
	public void createPage5()
	{
		panel5 = new JPanel();
		panel5.setLayout(null);
		panel5.setBackground(Color.CYAN);
		JLabel link = new JLabel("<html><u>File Transferring</u></html>");
		link.setBounds(40,30,150,25);
		panel5.add(link);
		panel5.setPreferredSize(new Dimension(900,500));
		link.setForeground(Color.BLUE);
		link.setFont(font);
		
		BufferedImage charImage = null;
		try {
			charImage = ImageIO.read(new File("5.jpg"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			//System.out.println("Cannot find image");
		}
		charImage = resize(charImage,200,300);

		ImageIcon nattIcon = new ImageIcon(charImage);
		JLabel natt =new JLabel(nattIcon);
		natt.setBounds(480,100,200,300);
		panel5.add(natt);
		
		link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		link.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {

				jep = new JEditorPane();
				jep.setEditable(false);
				HTMLJDialog = new JDialog(frame1,"Help HTML5");
				try {
					jep.setPage(new File("5File Transferring.html").toURI().toURL());
					jep.addHyperlinkListener(new HyperlinkListener(){
						public void hyperlinkUpdate(HyperlinkEvent event) {
							if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
								try {
									jep.setPage(event.getURL());
									//urlField.setText(event.getURL().toExternalForm());
								} catch(IOException ioe) {
									jep.setContentType("text/html");
									jep.setText("<html>Could not load</html>");
								}
							}	
						}

					});
				}catch (IOException e1) {
					jep.setContentType("text/html");
					jep.setText("<html>Could not load</html>");
				} 
				JScrollPane scrollPane = new JScrollPane(jep);     
				HTMLJDialog.getContentPane().add(scrollPane);
				HTMLJDialog.setPreferredSize(new Dimension(800,400));
				HTMLJDialog.pack();
				HTMLJDialog.setVisible(true);
				HTMLJDialog.setModal(false);
			}
		});
	}
	public void createPage6()
	{
		panel6 = new JPanel();
		panel6.setLayout(null);
		panel6.setBackground(Color.CYAN);
		
		JLabel link = new JLabel("<html><u>Don't know SHIT!</u></html>");
		link.setBounds(40,30,150,25);
		panel6.add(link);
		link.setForeground(Color.BLUE);
		link.setFont(font);
		
		JLabel gog = new JLabel("<html><u>Prevents you from failing</u></html>");
		gog.setBounds(40,200,225,25);
		panel6.add(gog);
		gog.setForeground(Color.BLUE);
		gog.setFont(font);
		
		JLabel nattw = new JLabel("<html><u>Contact the GOD Directly!</u></html>");
		nattw.setBounds(480,360,225,25);
		panel6.add(nattw);
		nattw.setForeground(Color.BLUE);
		nattw.setFont(font);
		
		
		
		BufferedImage nattImage = null;
		try {
			nattImage = ImageIO.read(new File("Natt.jpg"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			//System.out.println("Cannot find image");
		}
		nattImage = resize(nattImage,200,300);

		ImageIcon nattIcon = new ImageIcon(nattImage);
		JLabel natt =new JLabel(nattIcon);
		natt.setBounds(480,30,200,300);
		panel6.add(natt);
		JLabel nattLabel = new JLabel("The image of our GOD");
		nattLabel.setBounds(520,200,200,300);
		panel6.add(nattLabel);
		
		gog.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		gog.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				Desktop desktop = Desktop.getDesktop();
				try {
					desktop.browse(new URI("https://www.google.com"));
				} catch (IOException | URISyntaxException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		nattw.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		nattw.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				Desktop desktop = Desktop.getDesktop();
				try {
					desktop.browse(new URI("https://www.facebook.com/executor.natt.wara"));
				} catch (IOException | URISyntaxException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		panel6.setPreferredSize(new Dimension(900,500));
		
		link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		link.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {

				jep = new JEditorPane();
				jep.setEditable(false);

				HTMLJDialog = new JDialog(frame1,"Help HTML5");
				try {
					jep.setPage(new File("6Don't know woi.html").toURI().toURL());
					jep.addHyperlinkListener(new HyperlinkListener(){
						public void hyperlinkUpdate(HyperlinkEvent event) {
							if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
								try {
									jep.setPage(event.getURL());
									//urlField.setText(event.getURL().toExternalForm());
								} catch(IOException ioe) {
									jep.setContentType("text/html");
									jep.setText("<html>Could not load</html>");
								}
							}	
						}

					});
				}catch (IOException e1) {
					jep.setContentType("text/html");
					jep.setText("<html>Could not load</html>");
				}
				JScrollPane scrollPane = new JScrollPane(jep); 
				
				HTMLJDialog.getContentPane().add(scrollPane);
				HTMLJDialog.setPreferredSize(new Dimension(800,400));
				HTMLJDialog.pack();
				HTMLJDialog.setVisible(true);
				HTMLJDialog.setModal(false);
			}
		});

	}
	@Override
	public void setRI(int i) {
		lg.uloop.setRI(i);
		System.out.println("setting refresh interval to "+i);
	}
	
	@Override
	public void talk(String talktext) {
		new Timer2(talktext,lg.icePlayer.getDrawPixelLocation().x,lg.icePlayer.getDrawPixelLocation().y).start();
	}
	/*
  public void addKeyBinding(){
	 // this.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,ActionEvent.CTRL_MASK));
	  this.getCanvas().getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),"plus");
  }
	 */
}
/*
class ZoomHandler implements ActionListener{
	@Override
	public void actionPerformed(ActionEvent e) {
		this.getCanvas().
	}
}
 */
