package Viewer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;
import javax.swing.event.MouseInputListener;

import Annotation.CrossAnnotation;
import Annotation.FlagAnnotation;
import Annotation.Note;
import Annotation.NoteAnnotation;
import Annotation.NoteDialog;
import Annotation.RectangleAnnotation;
import Experiment.PointClass;
import PointCounting.PointCounting;

/**
 * 
 */

/**
 * @author tcornis3
 *
 */
public class ViewPort extends JLabel implements MouseInputListener,MouseWheelListener {	
	public int selectionX;
	public int selectionY;
	public int selectionW;
	public int selectionH;
	public Rectangle selectionRect;
	public boolean showSelection;
	public BufferedImage image;
	public BufferedImage outputImage;
	public Rectangle viewPortRect;
	private double zoom = 1;
	private int pointerX;
	private int pointerY;
	private int pointerXvp;
	private int pointerYvp;
	private Point lastPos = new Point(0,0);
	private Slide slide;
	private boolean clearPoints = true;
	
	private JPopupMenu popup;
	private JPopupMenu notePopup;
	private Point notePopupPoint;
	private JMenuItem menuItem;	
	private NoteDialog noteDialog;
	
	public ViewPort() {
		this.addMouseListener(this);
		this.addMouseMotionListener(this);			
		this.addMouseWheelListener(this);

		updateNotePopup();		
		popup = new JPopupMenu();
		for (PointClass pc : PointCounting.experiment.getPointClasses()) { 
			menuItem = new JMenuItem(pc.getName());
			menuItem.addActionListener(new popupListener());				
			popup.add(menuItem);
		}
		popup.addSeparator();
		menuItem = new JMenuItem("Add flag");
		menuItem.addActionListener(new popupListener());
		popup.add(menuItem);
		menuItem = new JMenuItem("Remove flag");
		menuItem.addActionListener(new popupListener());
		popup.add(menuItem);
	}

	public void setSlide(Slide s) {
		this.slide = s;
	}
	
	public void setZoom(double z) {
		this.zoom = z;	
	}

	public double getZoom() {
		return this.zoom;	
	}
	
	public void updateCounts() {
		slide.getGrid().updateCounts();
		PointCounting.updateCountsPane();
	}
	
	public void drawGrid() {
		Graphics2D g = (Graphics2D) outputImage.getGraphics();			
		// find grid points in this viewport
		for (GridPoint gp : slide.getGrid().getPoints()) {
			if (viewPortRect.contains(gp.getCenter())) {
				drawPoint(gp,g);
			}
		}
	}
	
	public void drawNotes() {
		Graphics2D g = (Graphics2D) outputImage.getGraphics();			
		// find notes in this viewport
		for (Note note : slide.getGrid().getNotes()) {
			if (viewPortRect.contains(note.getAnchor())) {
				drawNote(note,g);
			}
		}
	}
	
	public void selectPoints() {
		if (clearPoints) slide.getGrid().unselectAll();
		slide.getGrid().selectPoints(selectionRect);
	}
	
	public Rectangle scaleRect(Rectangle r, double scale) {
		int x = (int)Math.round(r.x * scale);
		int y = (int)Math.round(r.y * scale);
		int w = (int)Math.round(r.width * scale);
		int h = (int)Math.round(r.height * scale);
		Rectangle scaledRect = new Rectangle(x,y,w,h);
		return scaledRect;
	}
	
	public void drawPoint(GridPoint gp, Graphics2D g2d) {
		int gx = (int)Math.round((gp.getCenter().x - viewPortRect.x) * this.zoom);
		int gy = (int)Math.round((gp.getCenter().y - viewPortRect.y) * this.zoom);
		int r = (int)Math.round(slide.getGrid().getRadius() * this.zoom);
		CrossAnnotation ca = new CrossAnnotation(new Point(gx,gy),r);
		if (gp.isSelected()) {
			ca.setColor(Color.BLACK);
			ca.setStroke(new BasicStroke(7f));
			ca.paint(g2d);
		}
		if (gp.isFlagged()) {
			FlagAnnotation fa = new FlagAnnotation(new Point(gx,gy),(int)Math.round(this.slide.getGrid().getPointDistancePx()/18.0));
			fa.setColor(Color.RED);
			fa.setStroke(new BasicStroke(2f));
			fa.paint(g2d);
		}
		PointClass pc = PointCounting.experiment.getPointClass(gp.getPointClass());
		ca.setColor(pc.getColor());
		ca.setStroke(new BasicStroke(pc.getWeight()));
		ca.paint(g2d);
	}
	
	public void drawNote(Note note, Graphics2D g2d) {
		int gx = (int)Math.round((note.getAnchor().x - viewPortRect.x) * this.zoom);
		int gy = (int)Math.round((note.getAnchor().y - viewPortRect.y) * this.zoom);
		NoteAnnotation na = new NoteAnnotation(new Point(gx,gy),note.getMessage());
		na.setColor(Color.BLACK);
		na.setStroke(new BasicStroke(2f));
		na.paint(g2d);			
	}
	
	public void setImage(BufferedImage bi, Rectangle rect) {
		this.viewPortRect = rect;
		AffineTransform tx = new AffineTransform();
		tx.scale(this.zoom, this.zoom);
		AffineTransformOp op = new AffineTransformOp(tx,AffineTransformOp.TYPE_BILINEAR);
		bi = op.filter(bi, null);		
		this.image = bi;
	}
	
	public Grid getGrid() {
		return this.slide.getGrid();
	}
	
	public void update() {
		outputImage = duplicate(image);
		drawGrid();
		drawNotes();
		drawSelection();
		updateCounts();
		updateNotePopup();
		PointCounting.updateFlaggedFields();
		this.setIcon(new ImageIcon(outputImage));
	}
	
	public void drawSelection() {
		Graphics2D g = (Graphics2D) outputImage.getGraphics();
		if (showSelection) {
			Rectangle r = (Rectangle)selectionRect.clone();
			r.translate(-1*viewPortRect.x,-1*viewPortRect.y);
			r = scaleRect(r,this.zoom);
			RectangleAnnotation ra = new RectangleAnnotation(r);
			ra.setColor(Color.YELLOW);
			ra.setStroke(new BasicStroke(2f));
			ra.paint(g);
			selectPoints();
		}
	}
		
	public BufferedImage duplicate(BufferedImage bi) {
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		//WritableRaster raster = bi.copyData(null);
		return new BufferedImage(cm, bi.copyData(null), isAlphaPremultiplied, null);
	}
	
	public void mouseMoved(MouseEvent e) {
		pointerX = (int)Math.round(e.getX() * 1/zoom) + viewPortRect.x;
		pointerY = (int)Math.round(e.getY() * 1/zoom) + viewPortRect.y;
		pointerXvp = e.getX();
		pointerYvp = e.getY();
		//System.out.println(pointerXvp + "," + pointerYvp + " : " + pointerX + "," + pointerY);
		//update the scrollpane viewport pos
		JComponent c = (JComponent)e.getComponent();
		JViewport vp = (JViewport)c.getParent();
		lastPos.setLocation(vp.getMousePosition());
	}
	
	public void mousePressed(MouseEvent e) {
		if (e.isControlDown()){
		   clearPoints = false;
		} else {
		   clearPoints = true;	
		}
		selectionX = pointerX;
		selectionY = pointerY;
	}
	
	public void scrollTo(Point newPos) {
		JViewport vp = (JViewport)this.getParent();
		if (newPos.x > (slide.getFieldWidth()/2)) {
			newPos.x = (slide.getFieldWidth()/2);
		}
		if (newPos.x < 0) {
			newPos.x = 0;
		}
		if (newPos.y > (slide.getFieldHeight()/2)) {
			newPos.y = (slide.getFieldHeight()/2);
		}
		if (newPos.y < 0) {
			newPos.y = 0;
		}
		vp.setViewPosition(newPos);
		lastPos.setLocation(newPos);
	}
	
	public void mouseDragged(MouseEvent e) {
		int x,y;
		pointerX = (int)Math.round(e.getX() * 1/zoom) + viewPortRect.x;
		pointerY = (int)Math.round(e.getY() * 1/zoom) + viewPortRect.y;
		pointerXvp = e.getX();
		pointerYvp = e.getY();
		//System.out.println(pointerX + "," + pointerY);
		switch(PointCounting.currentTool) {
            case POINTER:
				if (pointerX > selectionX) {
					x = selectionX;
				} else {
					x = pointerX;
				}
				if (pointerY > selectionY) {
					y = selectionY;
				} else {
					y = pointerY;
				}
				selectionW = Math.abs(pointerX - selectionX);
				selectionH = Math.abs(pointerY - selectionY);
				this.selectionRect = new Rectangle(x,y,selectionW,selectionH);
				showSelection = true;
				update();
				break;
			case DRAG:
				if (zoom == 1) {
					JComponent c = (JComponent)e.getComponent();
					JViewport vp = (JViewport)c.getParent();
					Point currPos = vp.getMousePosition();
					Point viewPos = getVisibleRect().getLocation(); 
					viewPos.translate(lastPos.x-currPos.x,lastPos.y-currPos.y);
					scrollTo(viewPos);
					lastPos.setLocation(currPos); //if we don't do this, something weird happens
				}
			break;
		}
	}

	public void mouseReleased(MouseEvent e) {
		//System.out.println("  "+selectionX+","+selectionY+" : "+selectionW+","+selectionH);
		this.selectionRect = new Rectangle(0,0,0,0);
		showSelection = false;
		update();
	}
	
	public void mouseClicked(MouseEvent e) {
		pointerX = (int)Math.round(e.getX() * 1/zoom) + viewPortRect.x;
		pointerY = (int)Math.round(e.getY() * 1/zoom) + viewPortRect.y;
		if (e.getButton() == MouseEvent.NOBUTTON) {
		  System.out.println("No button clicked...");
		} else if (e.getButton() == MouseEvent.BUTTON1) {
			switch(PointCounting.currentTool) {
				case POINTER:
					int x = pointerX - slide.getGrid().getRadius();
					int y = pointerY - slide.getGrid().getRadius();
					int wh = slide.getGrid().getRadius()*2;
					selectionRect = new Rectangle(x,y,wh,wh);
					showSelection = true;
					update();
					showSelection = false;
					update();
				break;
			}			
		} else if ((e.getButton() == MouseEvent.BUTTON2) || (e.getButton() == MouseEvent.BUTTON3)){
			if (slide.getGrid().getSelectedCount() > 0) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			} else {
				notePopupPoint = new Point(pointerX,pointerY);
				notePopup.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}
	
	public void mouseExited(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		int notches = e.getWheelRotation();
        if (notches > 0) {
			slide.previousField();
			slide.getGrid().unselectAll();
			PointCounting.showField();
        } else {
			slide.nextField();
			slide.getGrid().unselectAll();
			PointCounting.showField();			
		}
    }

	public void updateNotePopup() {
		notePopup = new JPopupMenu();
		if (zoom == 1) {
			menuItem = new JMenuItem("10X");
			menuItem.addActionListener(new popupListener());
			notePopup.add(menuItem);
		} else {
			menuItem = new JMenuItem("20X");
			menuItem.addActionListener(new popupListener());
			notePopup.add(menuItem);
		}
		notePopup.addSeparator();
		switch(PointCounting.currentTool) {
            case POINTER:
				menuItem = new JMenuItem("Drag tool");
				menuItem.addActionListener(new popupListener());
				notePopup.add(menuItem);
				break;
			case DRAG:
				menuItem = new JMenuItem("Pointer");
				menuItem.addActionListener(new popupListener());
				notePopup.add(menuItem);
				break;
		}
		notePopup.addSeparator();
		menuItem = new JMenuItem("Add note");
		menuItem.addActionListener(new popupListener());
		notePopup.add(menuItem);
		menuItem = new JMenuItem("Remove note");
		menuItem.addActionListener(new popupListener());
		notePopup.add(menuItem);
	}
	
	//Listen to the popups
	public class popupListener implements ActionListener {
		public void actionPerformed(ActionEvent e){
			for (int i = 0; i < PointCounting.experiment.countPointClasses(); i++) {
				if (e.getActionCommand() == PointCounting.experiment.getPointClasses().get(i).getName()) {
					for (GridPoint gp : slide.getGrid().getPoints()) {
						if (gp.isSelected()) gp.setClass(i);
					}
				}
				slide.getGrid().updateCounts();
			}
			if (e.getActionCommand() == "Add flag") {
				for (GridPoint gp : slide.getGrid().getPoints()) {
					if (gp.isSelected()) gp.flag();
				}
			}
			if (e.getActionCommand() == "Remove flag") {
				for (GridPoint gp : slide.getGrid().getPoints()) {
					if (gp.isSelected()) gp.unflag();
				}
			}
			if (e.getActionCommand() == "20X") {
				int x = pointerXvp;
				int y = pointerYvp;
				x = 2 * x - PointCounting.viewPortW/2;
				y = 2 * y - PointCounting.viewPortH/2;
				PointCounting.zoomButton.setIcon(new ImageIcon("icons\\level1_icon&32.png"));
				PointCounting.currentZoom = 1;
				PointCounting.updateImageInfo();
				PointCounting.showField();
				scrollTo(new Point(x,y));
			}
			if (e.getActionCommand() == "10X") {
				PointCounting.zoomButton.setIcon(new ImageIcon("icons\\level0_icon&32.png"));
				PointCounting.currentZoom = 0.5;
				PointCounting.pointerButton.setSelected(true);
				PointCounting.currentTool = PointCounting.Tool.POINTER;
				PointCounting.frame.setCursor(PointCounting.pointerCursor);
				PointCounting.updateImageInfo();
				PointCounting.showField();
			}
			if (e.getActionCommand() == "Pointer") {
				PointCounting.pointerButton.setSelected(true);
				PointCounting.currentTool = PointCounting.Tool.POINTER;
				PointCounting.frame.setCursor(PointCounting.pointerCursor);
			}
			if (e.getActionCommand() == "Drag tool") {
				PointCounting.dragButton.setSelected(true);
				PointCounting.currentTool = PointCounting.Tool.DRAG;
				PointCounting.frame.setCursor(PointCounting.dragCursor);
			}
			if (e.getActionCommand() == "Add note") {
				int x = notePopupPoint.x;
				int y = notePopupPoint.y;
				Point anchor = new Point(x,y);
				noteDialog = new NoteDialog(PointCounting.frame);
				noteDialog.show();
				System.out.println(noteDialog.getMessage());
				noteDialog.getMessageLines();
				slide.getGrid().addNote(anchor,noteDialog.getMessage(),(Graphics2D)outputImage.getGraphics());
				update();
			}
			if (e.getActionCommand() == "Remove note") {
				int x = notePopupPoint.x;
				int y = notePopupPoint.y;
				for (int i = 0; i < slide.getGrid().getNotes().size();i++) {
					Note note = slide.getGrid().getNotes().get(i);
					Rectangle boundsRect = note.getBoundsRect((Graphics2D)outputImage.getGraphics());	
					boundsRect.width = (int)Math.round(boundsRect.width * 1/zoom); 
					boundsRect.height = (int)Math.round(boundsRect.height * 1/zoom); 
					if (boundsRect.contains(new Point(x,y))) {
						slide.getGrid().removeNote(i);
					}	
				}
				update();	
			}

			slide.getGrid().unselectAll();
			update();
		}
	}
}

