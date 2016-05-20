import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class PositionTab extends JPanel implements ActionListener, ILogger {
	private static final long serialVersionUID = -2990824653605100370L;

	private IRobot robot;
	private Iterator<Point3d> currentIterator;

	private ArenaPanel arena;
	private TextArea infoText;
	private Image background;
	private BufferedImage buffer;
	private Graphics2D bufferGraphics;
	private JButton matchButton;
	private IRobotFactory robotFactory;

	class ArenaPanel extends ArenaRepresentation implements MouseListener {
		private static final long serialVersionUID = -5243504663310818093L;

		private static final int imageWidth = 800;
		private final static double scaleFactor = (double) imageWidth / 3000.0;

		private final static int rayon = (int) (20 * scaleFactor);

		private Point3d lastPoint = null;

		private Point currPoint = null;
		int numClick = 1;

		private static final int largeur = 300;
		private static final int longueur = 180;
		private Point[][] robotShape = new Point[][] {
				new Point[] { new Point(-20, largeur / 2),
						new Point(-20, -largeur / 2),
						new Point(longueur, -largeur / 2),
						new Point(longueur, largeur / 2) },
				new Point[] { new Point(longueur, 50),
						new Point(longueur + 80, 50),
						new Point(longueur + 80, -50), new Point(longueur, -50) } };

		private Point convertRobotPoint(Point p, int x, int y, double angle) {
			return new Point(x
					+ (int) (scaleFactor * (p.x * Math.cos(angle) - p.y
							* Math.sin(angle))), y
					+ (int) (scaleFactor * (-p.x * Math.sin(angle) - p.y
							* Math.cos(angle))));
		}

		private void drawRobot(Graphics myGraphics, int x, int y, double angle) {
			for (Point[] polygone : robotShape) {
				Point last = convertRobotPoint(polygone[polygone.length - 1],
						x, y, angle);
				for (Point p : polygone) {
					Point current = convertRobotPoint(p, x, y, angle);
					myGraphics.drawLine(last.x, last.y, current.x, current.y);
					last = current;
				}
			}
		}

		public void paint(Graphics myGraphics) {
			if (currentIterator != null) {
				Point3d myP = currentIterator.next();
				bufferGraphics.setColor(Color.RED);

				for (; (myP != null || currentIterator.hasNext()); myP = currentIterator
						.next()) {
					Point3d p = convertToImage(myP);
					if ((p != null) && (lastPoint != null)) {
						bufferGraphics.drawLine(lastPoint.x, lastPoint.y, p.x,
								p.y);
					}
					lastPoint = p;
				}
			}

			// Background with positions
			myGraphics.drawImage(buffer, 0, 0, null);

			// Add robots and their path
			if (currentIterator != null) {
				myGraphics.setColor(Color.RED);

				if (lastPoint != null) {
					drawRobot(myGraphics, lastPoint.x, lastPoint.y,
							lastPoint.angle);
				}

				List<Point> path = robot.getPath();
				if (path != null) {
					for (Point p : path) {
						p = convertToImage(new Point3d(0, p.x, p.y, 0.0f, 0));
						myGraphics.fillOval(p.x - 5, p.y - 5, 10, 10);
					}
				}

				myGraphics.setColor(Color.BLACK);
				path = robot.getAvoidPath();
				if (path != null) {
					for (Point p : path) {
						p = convertToImage(new Point3d(0, p.x, p.y, 0.0f, 0));
						myGraphics.fillOval(p.x - 5, p.y - 5, 10, 10);
					}
				}
			}

			// Highlight selected point
			myGraphics.setColor(Color.GREEN);
			if (currPoint != null) {
				int x = (int) ((double) currPoint.y * scaleFactor);
				int y = (int) ((double) currPoint.x * scaleFactor);

				myGraphics.drawOval(x - rayon / 2, y - rayon / 2, rayon, rayon);
			}

		}

		public ArenaPanel() {
			BufferedImage initialBackground = null;

			try {
				initialBackground = ImageIO.read(new File("table.png"));
			} catch (IOException e) {
				System.out.println("Unable to load 'table.png'");
			}

			int height = imageWidth * initialBackground.getHeight()
					/ initialBackground.getWidth();

			background = initialBackground.getScaledInstance(imageWidth,
					height, Image.SCALE_SMOOTH);

			buffer = new BufferedImage(imageWidth, height,
					BufferedImage.TYPE_INT_RGB);
			bufferGraphics = buffer.createGraphics();
			bufferGraphics.setBackground(Color.WHITE);

			bufferGraphics
					.drawImage(background, 0, 0, imageWidth, height, null);
			// Log.posIts=Log.pos.iterator();

			this.addMouseListener(this);
			this.setPreferredSize(new Dimension(imageWidth, height));
			// this.repaint();
			this.setVisible(true);

		}

		@Override
		public Point3d convertFromImage(Point3d p) {
			if (p == null) {
				return null;
			}
			int x = (int) ((double) p.x / scaleFactor);
			int y = 2100 - (int) ((double) p.y / scaleFactor);
			double angle = (double) (p.angle * 180.0 / Math.PI);
			return new Point3d(p.num, x, y, (float) angle, p.timeStamp);
		}

		@Override
		public Point3d convertToImage(Point3d p) {
			if (p == null) {
				return null;
			}
			int x = (int) ((double) p.x * scaleFactor);
			int y = (int) ((double) (2100 - p.y) * scaleFactor);
			double angle = (double) p.angle * Math.PI / 180.0;
			return new Point3d(p.num, x, y, (float) angle, p.timeStamp);
		}

		@Override
		public void mouseClicked(MouseEvent arg0) {
			if (!arg0.isShiftDown()) {
				return;
			}

			// recherche du point le plus proche, en ajoutant un malus pour les
			// points r?cents.

			Point posClick = arg0.getPoint();
			Point3d closer = null;
			double minDist = -1;
			int DistSel = 10;
			System.out.println("cliqu? en " + posClick);
			Positions pos = robot.getPositions();
			for (Point3d myP : pos) {
				if (myP == null) {
					continue;
				}
				int x = (int) ((double) myP.y * scaleFactor);
				int y = (int) ((double) myP.x * scaleFactor);
				Point myPP = new Point(x, y);

				double di1 = myPP.distance(posClick);
				double di = di1;
				if (minDist == -1 || di < minDist && di1 < DistSel) {
					minDist = di;
					closer = myP;
				}
			}
			if (closer != null) {
				numClick++;
				currPoint = closer;
				PositionTab.this.repaint();
				infoText.append("" + closer);
				infoText.repaint();
			}

		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
			// Don't care
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			// Don't care
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			// Don't care
		}

		public void mouseReleased(MouseEvent arg0) {
			// Don't care
		}
	}

	public PositionTab(IRobotFactory robotFactory) {
		this.robotFactory = robotFactory;
		
		arena = new ArenaPanel();

		infoText = new TextArea();
		infoText.setEditable(false);

		JScrollPane infoPanel;
		infoPanel = new JScrollPane(infoText);
		infoPanel.setVisible(true);

		JPanel buttonsPanel;
		buttonsPanel = new JPanel();
		matchButton = new JButton("Connexion");
		matchButton.addActionListener(this);

		buttonsPanel.add(matchButton);

		JPanel config = new JPanel(new BorderLayout());
		config.add(robotFactory.configurationPanel(), BorderLayout.WEST);
		config.add(buttonsPanel, BorderLayout.EAST);

		JPanel left = new JPanel(new BorderLayout());
		left.add(arena, BorderLayout.NORTH);
		left.add(config, BorderLayout.CENTER);

		this.setLayout(new BorderLayout());
		this.add(left, BorderLayout.WEST);
		this.add(infoPanel, BorderLayout.EAST);

		this.repaint();
		this.setVisible(true);
	}

	public void setRobots(IRobot robot) {
		final PositionTab tab = this;

		this.robot = robot;

		Positions pos = robot.getPositions();
		this.currentIterator = pos.iterator();

		pos.addCallback(new NewPositionCallback() {
			public void onNewPosition(Point3d p) {
				tab.repaint();
			}
		});
	}

	public void actionPerformed(ActionEvent myEvent) {
		Object source = myEvent.getSource();

		if (source == matchButton) {
			robot = robotFactory.getRobot(arena);
			robot.prepareRobot();
			this.setRobots(robot);
		}

	}

	public void log(String message) {
		infoText.append(message + "\n");
		infoText.repaint();
	}

	public ArenaRepresentation getPanel() {
		return arena;
	}

	public static void main(String[] args) {
		JFrame myFrame = new JFrame();
		PositionTab posTab = new PositionTab(new NetworkRobot.Factory());
		myFrame.add(posTab);

		// myFrame.setPreferredSize(new Dimension(1024, 768));
		myFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		myFrame.pack();
		myFrame.setVisible(true);
	}

}

