import javax.swing.JFrame;


import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import javax.swing.JButton;

import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;
import javax.swing.BoxLayout;

import org.apache.commons.math.geometry.Vector3D;

/** Begins the Calibration process for the pendaphone.
 *  Currently contains the main class for the whole set up 
 *  (so the name should probably be changed)
 * @author erin
 *
 */
public class CalibrationPanel extends JPanel
{
	PendaphoneGestures gest;
	
	// dimensions of calibration box
	int height;
	int width;
	int xPos = -1;
	int yPos = -1;
	
	// buffers for pendaphone motion (want to compute these on the fly)
	public static double move_buffer = 0.1;
	public static double click_buffer = -0.009; //Sets the string length at which the click occurs
	
	//modes of callibration
	public static final int POSITIONING_MODE = 0;
	public static final int CALIBRATION_1 = 1;
	public static final int CALIBRATION_2 = 2;
	public static final int CALIBRATION_3 = 3;
	
	// messages related to the calibration
	public static final String[] CALIBRATION_MESSAGES = 
		{"Is this dialog in a place you can reach?\nDrag it somewhere you can reach, then hit ok.",
		"Move the pendaphone to the bottom right corner\nof this dialog box.",
		"Move the pendaphone to the bottom left corner\nof this dialog box.",
		"Move the pendaphone to the top left corner\nof this dialog box."
		};


	/** Constructor. Each calibration panel needs an assorted set of pendaphone gestures. 
	*/
	public CalibrationPanel(PendaphoneGestures pg)
	{
		gest = pg;
	}
	
	//todo - could move text somewhere else
	private JDialog showDialog(int mode)
	{
		  JOptionPane pane = new JOptionPane(CALIBRATION_MESSAGES[mode]);
		  JDialog dialog = pane.createDialog(this, "Calibration");
		  	  
		  // need to get the position of the dialog
		  // bug... sometimes the next dialog shows up in the wrong position
		  if (mode == POSITIONING_MODE)  
		  	  dialog.addComponentListener(new CalibrationListener(dialog, this));
		  else
			  dialog.setModal(false); //can't be modal to read pendaphone position
		  
		  dialog.setSize(400,150); // this size is aribtrary
		  
		  // sets the location to the location of the previous dialog
		  if (xPos >= 0 && yPos >= 0) 
			  dialog.setLocation(xPos, yPos);
		  
		  dialog.setVisible(true);
		  return dialog;
		
	}
	
	/** Sets the location of the calibration dialog. */
	public void setCalibrationCoordinates(int x, int y, int width, int height)
	{
		this.xPos = x;
		this.yPos = y;
		this.width = width;
		this.height = height;
	}
	
	/** A listener to read the coordinates of the first calibration dialog. */
	private class CalibrationListener implements ComponentListener
	{
		JDialog dialog;
		CalibrationPanel panel;
		
		public CalibrationListener(JDialog dialog, CalibrationPanel panel)
		{
			this.dialog = dialog;
			this.panel = panel;
		}
		
		public void componentHidden(ComponentEvent e) {
			panel.setCalibrationCoordinates(dialog.getX(), dialog.getY(), dialog.getWidth(), dialog.getHeight());
	    }

	    public void componentMoved(ComponentEvent e) {
			
	    }

	    public void componentResized(ComponentEvent e) {
	    }

	    public void componentShown(ComponentEvent e) {
	    }
	}
	
	/** Calibrates the Pendaphone coordinates. */
	public DisplayPlane startCalibration()
	{
		Vector3D firstLocation = gest.getRightLocation();
		gest.setFL(firstLocation);
		
		showDialog(POSITIONING_MODE);
		
		JDialog dialog = showDialog(CALIBRATION_1);
		Vector3D bottomRight = calibrateEndpoint(1, firstLocation);
		dialog.setVisible(false);
		
		dialog = showDialog(CALIBRATION_2);
		Vector3D bottomLeft = calibrateEndpoint(2, firstLocation);
		dialog.setVisible(false);
		dialog = showDialog(CALIBRATION_3);
		Vector3D topLeft = calibrateEndpoint(3, firstLocation);
		dialog.setVisible(false);
		
		System.out.println("BottomRight location: " + bottomRight.toString());
		System.out.println("BottomLeft location: " + bottomLeft.toString());
		System.out.println("TopLeft location: " + topLeft.toString());
		
		DisplayPlane dp = new DisplayPlane(bottomRight, bottomLeft, topLeft, height, width, xPos, yPos); 
		//double closestDistance = (dp.onPlane(bottomLeft));
		//move_buffer = closestDistance/3;
		//click_buffer = dp.onPlane(bottomLeft)*50;
		//System.out.println(firstLocation.getNorm());

		//System.out.println(closestDistance + " - " + move_buffer + " " + click_buffer);
		
		
		return dp;
	}
	
	/** Process to get each calibration point.
	 * 
	 * @param i - the calibration phase, for debugging
	 * @param firstLocation - the initial location of the vector
	 * @return
	 */
	public Vector3D calibrateEndpoint(int i, Vector3D firstLocation)
	{
		Vector3D oldLocation, newLocation;
		do
		{
			oldLocation = gest.getRightLocation();
			System.out.println("Phase 1 - " + i + " - " + oldLocation.getNorm() + " - " + firstLocation.getNorm());

		}
		while ( oldLocation.getNorm() < firstLocation.getNorm() + move_buffer);
		//while ( oldLocation.getNorm() < 0.2);
		
		newLocation = gest.getRightLocation();
		//while (oldLocation.getNorm() <= newLocation.getNorm())
		while (oldLocation.getNorm() <= newLocation.getNorm())
		{
			oldLocation = newLocation;
			newLocation = gest.getRightLocation();
			System.out.println("Phase 2 - " + i + " - " + oldLocation.getNorm() + firstLocation.getNorm());
		}
		//while (oldLocation.getNorm() > 0.2)
		while (oldLocation.getNorm()  > firstLocation.getNorm() + move_buffer)
		{
			oldLocation = gest.getRightLocation();
			System.out.println("Phase 3 - " + i + " - " + oldLocation.getNorm() + " - " + firstLocation.getNorm());
		}
		return newLocation;
		
	}
	
	public static void main(String[] args)
	{
		JFrame frame = new JFrame();
		PendaphoneGestures pg = new PendaphoneGestures();
		/*while (pg.getRightLocation().getNorm() == 1.0)
			System.out.println(pg.getRightLocation().getNorm());*/
		CalibrationPanel cp = new CalibrationPanel(pg);
		frame.add(cp);
		DisplayPlane dp = cp.startCalibration();
		pg.setDisplayPlane(dp);
		
		//System.out.println(Toolkit.getDefaultToolkit().getScreenSize());
	}
	
}

