package main;

import header.Header;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import pages.contents.ContentsPage;
import pages.recipes.RecipiesPage;
import pages.settings.SettingsPage;
import pages.shoppinglist.ShoppingListPage;
import pages.status.StatusPage;

import navBar.NavBar;

import utilities.Settings;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("serial")
public class MainPanel extends JPanel implements AWTEventListener {
	private NavBar nb = null;
	private JPanel idleScreen = null;
	private JPanel cardPanel = null;
	public final static String IDLESCREEN = "Idle Screen";
	public final static String CONTENTPAGE = "Content Page";
	public final static String SHOPPINGLISTPAGE = "Shopping List Page";
	public final static String RECIPESPAGE = "Recipes Page";
	public final static String STATUSPAGE = "Status Page";
	public final static String SETTINGSPAGE = "Settings Page";
	private Image image;

	static private AtomicInteger countDown = new AtomicInteger(-1);
	private final static int TIMEOUT = 60;

	public MainPanel() {
		super();
		initialize();
	}

	/**
	 * Initialize the main gui component.
	 */
	private void initialize() {
		setBackground(Settings.instance().background);
		ImageIcon icon = new ImageIcon(getClass().getClassLoader().getResource(
				"Icons/ice-cubes.jpg"));
		image = icon.getImage();
		setLayout(new BorderLayout());

		Header h = new Header();
		h.setPreferredSize(new Dimension(1000, 100));
		add(h, BorderLayout.NORTH);

		nb = new NavBar(this);
		nb.setPreferredSize(new Dimension(100, 1000));
		add(nb, BorderLayout.WEST);

		// Create idle screen.
		idleScreen = new JPanel(new GridLayout());
		JLabel label = new JLabel("Idle Screen");
		label.setFont(new Font("Roboto", Font.BOLD, 70));
		label.setHorizontalAlignment(SwingConstants.CENTER);
		idleScreen.add(label);
		idleScreen.setOpaque(false);

		// Create Shopping List Panel
		ShoppingListPage slp = new ShoppingListPage();
		slp.setPreferredSize(new Dimension(1000, 1000));

		// Create Contents Panel
		ContentsPage cp = new ContentsPage(this, slp.getActionListener());
		cp.setPreferredSize(new Dimension(1000, 1000));

		// Create Recipes Panel
		RecipiesPage rp = new RecipiesPage();
		rp.setPreferredSize(new Dimension(1000, 1000));

		// Create Status Panel
		StatusPage stp = new StatusPage();
		stp.setPreferredSize(new Dimension(1000, 1000));

		// Create Settings Panel
		SettingsPage sep = new SettingsPage();
		sep.setPreferredSize(new Dimension(1000, 1000));

		cardPanel = new JPanel(new CardLayout());
		cardPanel.setOpaque(false);
		cardPanel.add(idleScreen, IDLESCREEN);
		cardPanel.add(cp, CONTENTPAGE);
		cardPanel.add(slp, SHOPPINGLISTPAGE);
		cardPanel.add(rp, RECIPESPAGE);
		cardPanel.add(stp, STATUSPAGE);
		cardPanel.add(sep, SETTINGSPAGE);

		add(cardPanel, BorderLayout.CENTER);
	}

	public synchronized void setPage(String page) {
		CardLayout cl = (CardLayout) (cardPanel.getLayout());
		cl.show(cardPanel, page);
		nb.setSelected(page);
		this.eventDispatched(null);
	}

	/**
	 * Tile the background with the image. If the image is not null, the image
	 * is painted over the background.
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (image != null) {
			int iw = image.getWidth(this);
			int ih = image.getHeight(this);
			if (iw > 0 && ih > 0) {
				for (int x = 0; x < getWidth(); x += iw) {
					for (int y = 0; y < getHeight(); y += ih) {
						g.drawImage(image, x, y, iw, ih, this);
					}
				}
			}
		}
	}

	/**
	 * Reset the count if If the mouse is moved, or the user touches a part of
	 * the screen, reset the count down timer.
	 */
	@Override
	public void eventDispatched(AWTEvent arg0) {
		if (!idleScreen.isVisible()) {
			countDown.getAndSet(TIMEOUT);
		}
	}

	public static void main(String[] args) {
		MainPanel m = new MainPanel();

		Toolkit toolkit = Toolkit.getDefaultToolkit();
		toolkit.addAWTEventListener(m, AWTEvent.MOUSE_MOTION_EVENT_MASK);

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1024, 800);
		// frame.setSize(toolkit.getScreenSize());
		// frame.setUndecorated(true);
		frame.setLayout(new GridLayout());
		frame.add(m);
		frame.setVisible(true);
		try {
			Thread u = new UpdaterThread(m);
			u.start();
			while (true) {
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static class UpdaterThread extends Thread {
		private final int TIMEOUT = 1000;
		private MainPanel mainPanel;

		public UpdaterThread(MainPanel mp) {
			mainPanel = mp;
		}

		public void run() {
			while (true) {
				try {
					if (countDown.get() > 0) {
						Thread.sleep(TIMEOUT);
						countDown.getAndDecrement();
					} else if (countDown.get() == 0) {
						countDown.getAndDecrement();
						mainPanel.setPage(IDLESCREEN);
					}
				} catch (InterruptedException ex) {
				}
			}
		}
	}
}

