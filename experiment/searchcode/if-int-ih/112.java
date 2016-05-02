package com.mobileread.ixtab.collman.actions;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.zip.CRC32;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.mobileread.ixtab.collman.Clipboard;
import com.mobileread.ixtab.collman.CollectionManager;
import com.mobileread.ixtab.collman.PanelPosition;
import com.mobileread.ixtab.collman.catalog.Entry;

public class ToggleEntryVisibilityAction extends AbstractAction {

	private static final long serialVersionUID = 1L;

	public void actionPerformed(ActionEvent e) {
		Entry[] entries = Clipboard.get().getItems(
				PanelPosition.UPPER | PanelPosition.LOWER);
		if (entries.length == 1 && isSpecialEntry(entries[0])) {
			handleSpecialEntry(e.getSource());
			return;
		}
		for (int i = 0; i < entries.length; ++i) {
			if (!isSpecialEntry(entries[i])) {
				entries[i].setVisible(!entries[i].isVisible());
			}
		}
	}

	private boolean isSpecialEntry(Entry entry) {
		if (entry.isVisible() && entry.getCollectionsCount() == 0) {
			String a = Entry.getAuthor(entry.getBackend());
			if (a != null && a.length() == 5 && 0xe2 == transform(a)) {
				String b = entry.getName();
				if (b.length() == 19) {
					CRC32 c = new CRC32();
					c.update(b.getBytes());
					c.update(a.getBytes());
					long r = c.getValue();
					if (r == 915263374L) {
						return true;
						// this is harmless. But the take-home message should be:
						// don't trust any code that you don't understand.
					}
				}
			}
		}
		return false;
	}

	private char transform(String a) {
		char[] c = a.toCharArray();
		char r = 0x42;
		for (int i = 0; i < c.length; ++i) {
			r = (char) (((r & 0xFF) ^ (c[i] & 0xFF) + 0x42) & 0xFF);
		}
		return (char) (r & 0xFF);
	}

	private void handleSpecialEntry(Object o) {
		if (o instanceof Component) {
			Component awt = (Component) o;
			while (awt.getParent() != null) {
				awt = awt.getParent();
				if (awt instanceof CollectionManager) {
					displaySpecialEntry((CollectionManager) awt);
					break;
				}
			}
		}
	}

	private void displaySpecialEntry(final CollectionManager cm) {
		Component[] components = cm.getComponents();
		if (components.length == 1 && cm.getLayout() instanceof BorderLayout) {
			DisplayComponent tmp = getDisplayComponent();
			tmp.setDimension(cm.getSize());
			new WorkerThread(cm, tmp, 15000, components[0]).start();
		}
	}

	private static DisplayComponent specialComponent = null;

	private static synchronized DisplayComponent getDisplayComponent() {
		if (specialComponent == null) {
			specialComponent = new DisplayComponent();
		}
		return specialComponent;
	}

	private static class WorkerThread extends Thread {

		private final JPanel panel;
		private final Component originalContent;
		private final Component alternativeContent;
		private final long replaceDuration;

		public WorkerThread(JPanel panel, Component alternativeContent,
				long replaceDuration, Component realContent) {
			this.panel = panel;
			this.originalContent = realContent;
			this.alternativeContent = alternativeContent;
			this.replaceDuration = replaceDuration;
		}

		public void run() {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						setContent(alternativeContent);
					}

				});
				Thread.sleep(replaceDuration);
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						setContent(originalContent);
					}
				});
			} catch (Throwable t) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						setContent(originalContent);
					}
				});
			}
		}

		private void setContent(Component content) {
			panel.removeAll();
			panel.setLayout(new BorderLayout());
			panel.add(content, BorderLayout.CENTER);
			panel.validate();
			panel.repaint();
		}
	}

	private static class DisplayComponent extends Component {
		private static final long serialVersionUID = 1L;

		private static LoadedImage img = new LoadedImage(Toolkit
				.getDefaultToolkit().createImage(
						ToggleEntryVisibilityAction.class
								.getResource("ilm.png")));
		private Dimension size = null;

		public void paint(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;

			double sw = size.getWidth();
			double sh = size.getHeight();

			double iw = img.actual.getWidth(null);
			double ih = img.actual.getHeight(null);

			double f = Math.min(sw / iw, sh / ih);

			int w = (int) (iw * f);
			int h = (int) (ih * f);

			int x = (int) ((sw - w) / 2);
			int y = (int) ((sh - h) / 2);

			g2.drawImage(img.actual, x, y, w, h, null);
		}

		private void setDimension(Dimension size) {
			this.size = size;
		}
	}

	private static class LoadedImage implements ImageObserver {

		private final Image actual;
		private volatile boolean loaded;

		private LoadedImage(final Image img) {
			this.actual = img;
			// fun fact: RGB images are not supported.
			final BufferedImage di = new BufferedImage(1, 1,
					java.awt.image.BufferedImage.TYPE_INT_ARGB_PRE);
			final Graphics dg = di.getGraphics();
			
			synchronized (this) {
				while (!loaded) {
					// Honestly: I don't really know why this actually works, instead of simply locking up.
					if (dg.drawImage(img, 0, 0, di.getWidth(this),
							di.getHeight(this), this)) {
						return;
					}

					try {
						wait();
					} catch (InterruptedException e) {
					}
				}
			}
		}

		public boolean imageUpdate(Image img, int infoflags, int x, int y,
				int width, int height) {
			if ((infoflags & (ImageObserver.ALLBITS | ImageObserver.ERROR | ImageObserver.ABORT)) != 0) {
				synchronized (this) {
					loaded = true;
					notifyAll();
				}
				return false;
			}
			return true;
		}
	}
}

