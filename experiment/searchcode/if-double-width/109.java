// Name:        Brian Golden
// Section:     001
// Date:        11/03/11
// Program:     Target shooting game front-end
// Description: Front-end interaction for a target shooting game

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class FrontEnd extends JApplet implements ActionListener, ItemListener, ChangeListener
{
	private JButton fireBtn;				// button for user choice to fire the projectile
	private JButton resetBtn;				// button to reset score and revive all Targets
	private JTextField angleField;			// text field for user-input angle, in degrees
	private JTextField velocityField;		// text field for user-input velocity, in meters per second
	private JComboBox colorDropDown;		// pull-down box for options for color of  
											//  the projectile and its trajectory 
	private JPanel componentPanel;			// panel to contain fireBtn, resetBtn, text fields
											//  and colorDropDown
	private JPanel sliderPanel;				// panel to contain angleSlider and velocitySlider
	private JSlider angleSlider;			// slider for setting angle
	private JSlider velocitySlider;			// slider for setting velocity
	private JPanel bottomPanel;				// panel to contain componentPanel and sliderPanel
	private double width;					// current width of the applet, in pixels
	private double height;					// current height of the applet, in pixels
	private double angle;					// user-input launch angle, in degrees
	private double velocity;				// user-input magnitude of the projectile's velocity, 
											//  in meters per second 
	private double curEndTime;				// current ending time for a Projectile
	private int score;						// player's score in the game
	private int targetKilledIndex;			// index of Target object killed in targets
	private int projectileIndex;			// index of Image in projectiles
	private int numPoints;					// number of points computed in final trajectory
	private int curEndPoints;				// number of points to compute in a trajectory
	private int maxPointNum;				// number computed point of max height
	private int prevMaxPointNum;			// max height of the previous trajectory for a RedBird
	private boolean isFired;				// true if user chose to fire the projectile
	private boolean isDrawn;				// true if the projectile has been drawn for the first time
	private boolean isAnimated;				// true if projectile has been animated
	private boolean targetWasKilled;		// true if a target was killed by the projectile
	private boolean isAtStart;				// true if the projectile is at its starting point
	private boolean hasBounced;				// true if a YellowBird has bounced one time
	private boolean hasReachedMax;			// true if a RedBird has already reached its max height
	private Trajectory curTraj;				// calculator for determining the position of the projectile
	private Trajectory prevTraj;			// trajectory object for YellowBird object, 
											//  used if the YellowBird bounced
	private Point startingPoint;			// point of the projectile's starting location
	private Point maxPoint;					// point at which a projectile has reached its max height
	private Target[] targets;				// Target objects in the game
	private Projectile[] projectiles;		// Projectile objects that may be launched
	private Color trajColor;				// current color of the trajectory and projectile
	private Font font;						// font for displaying score
	private FontMetrics metrics;			// metrics of font
	
	@Override
	public void init()										// set up the GUI
	{
		FlowLayout componentsLayout;						// layout of componentPanel
		
		setLayout(new BorderLayout());						// set up applet layout
		
		componentsLayout = new FlowLayout();				// initialize GUI components
		componentsLayout.setAlignment(FlowLayout.LEFT);
		componentPanel = new JPanel();
		componentPanel.setLayout(componentsLayout);		
		fireBtn = new JButton("FIRE!");
		resetBtn = new JButton("Reset");
		angleField = new JTextField(5);
		velocityField = new JTextField(5);  
		colorDropDown = new JComboBox(Constants.COLOR_NAMES);
		
		sliderPanel = new JPanel();
		sliderPanel.setLayout(componentsLayout);		
		
		angleSlider = new JSlider(SwingConstants.HORIZONTAL, 
								  0, Constants.MAX_ANGLE, 0);
		angleSlider.setMajorTickSpacing(Constants.ANGLE_MJ_TICK);
		angleSlider.setMinorTickSpacing(Constants.ANGLE_MN_TICK);
		angleSlider.setPaintTicks(true);
		velocitySlider = new JSlider(SwingConstants.HORIZONTAL, 
									 0, Constants.MAX_VEL, 0);
		velocitySlider.setMajorTickSpacing(Constants.VEL_MJ_TICK);
		velocitySlider.setMinorTickSpacing(Constants.VEL_MN_TICK);
		velocitySlider.setPaintTicks(true);
		
		bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		
		curEndTime = 0;										// initialize state variables
		score = 0;
		projectileIndex = (int)(Math.random()*
				Constants.NUM_PROJECTILES);
		numPoints = 0;
		curEndPoints = 0;
		maxPointNum = 0;
		prevMaxPointNum = 0;
		
		isFired = false;
		isDrawn = false;
		isAnimated = false;
		targetWasKilled = false;
		isAtStart = true;
		hasBounced = false;
		hasReachedMax = false;
		
		curTraj = new Trajectory();
		prevTraj = new Trajectory();
		maxPoint = new Point();
		
		targets = new Target[]
				  {new Target(Color.PINK), 
				   new Target(Color.YELLOW), 
				   new Target(Color.WHITE)};
		
		projectiles = new Projectile[]
				{new OrangeBird(new Point()),
				new YellowBird(new Point()),
				new BlueBird(new Point()), 
				new RedBird(new Point())};
		
		trajColor = Constants.COLORS[0];
		font = new Font("SansSerif", Font.BOLD, 
						Constants.FONT_SIZE);
		
		// width, height, startingPoint, and messageWidth are initialized in paint because paint will 
		//  be called immediately after init or window is resized and are not needed until the 
		//  background and slingshot are drawn to the screen
		// metrics is initialized in paint because it needs a graphics context to do so
		// angle and velocity are initialized in actionPerformed because they provide information not 
		//  needed until the user chooses to fire the projectile
		// targetKilledIndex is initialized in CalculateEndTime because 
		//  no target can be killed until the projectile has been launched
		
		componentPanel.add(new JLabel("Launch Angle: "));	  // add GUI components to componentPanel
		componentPanel.add(angleField);
		componentPanel.add(new JLabel("deg  "));
		componentPanel.add(new JLabel("Launch Velocity: "));
		componentPanel.add(velocityField);
		componentPanel.add(new JLabel("m/s"));
		componentPanel.add(new JLabel("Path color: "));
		componentPanel.add(colorDropDown);
		componentPanel.add(fireBtn);
		componentPanel.add(resetBtn);
		
		sliderPanel.add(angleSlider);						  // add sliders to sliderPanel
		sliderPanel.add(velocitySlider);
		
		bottomPanel.add(componentPanel, BorderLayout.NORTH);  // add all GUI components to bottomPanel
		bottomPanel.add(sliderPanel, BorderLayout.SOUTH);
		
		add(bottomPanel, BorderLayout.SOUTH);				  // add componentPanel to the applet window
		
		fireBtn.addActionListener(this);					  // add listeners to GUI components
		resetBtn.addActionListener(this);
		colorDropDown.addItemListener(this);
		angleSlider.addChangeListener(this);
		velocitySlider.addChangeListener(this);
	}
	
	@Override
	public void paint(Graphics g)				// draw background, targets and trajectory 
	{
		String scoreMessage;										// message display for current score
		int messageWidth;											// width of scoreMessage, in pixels
		
		width = getWidth();											// get current width of the applet
		height = getHeight();										// get current height of the applet
		startingPoint = new Point(Constants.PROJ_X_SCALE, 			// starting point of the projectile 
								  Constants.PROJ_Y_SCALE);			//  scaled based on scaling 
																	//  constants, width and height
		metrics = g.getFontMetrics();
		
		scoreMessage = "Score is " + score;
		messageWidth = metrics.stringWidth(scoreMessage);			// get width of messageWidth 
		
		super.paint(g);
		
		DrawBackground(g);											// draw sky, ground and slingshot
		//bottomPanel.repaint();										// redraw the GUI components 
																	//  over the background
		
		g.setColor(Constants.SCORE_COLOR);
		g.setFont(font);
		g.drawString(scoreMessage, 									// print score at top-center 
					(int)(width/2 - messageWidth/2), 				//  of the applet
					Constants.MESSAGE_Y);
		
		for (int j = 0; j < Constants.NUM_TARGETS; j++)				// draw the three targets; 
		{															// a Target will only be 
																	// drawn if it is alive
			targets[j].DrawTarget(g, width, height);
		}
		
		FireProjectile(g);											// fire the projectile, or draw at 
																	//  starting point if user has not 
																	//  yet chosen to fire
	}
	
	public void FireProjectile(Graphics g)
	// PRE: g is the current graphics context
	// POST: draws and/or animates the current projectile to the screen and 
	//		 animates it if user has chosen to fire
	{
		if (isFired && !isAnimated)									// if user has chosen to 
		{														 	//  fire the projectile
			maxPoint = new Point(1, 1);								// set so that any new point will
																	//  be higher, such to get 
																	//  first max point
			CalculateEndTime(curTraj);
			
			g.setColor(trajColor);
			
			if (projectiles[projectileIndex] instanceof YellowBird)	// if projectile is a YellowBird
			{
				FireYellowBird(g);
			}
			
			else if(projectiles[projectileIndex] 					// if projectile is a RedBird
					instanceof RedBird)
			{
				FireRedBird(g);
			}
			
			else													// if a OrangeBird or BlueBird
			{
				AnimateTrajectory(g, curTraj);
				
				if (curEndPoints < numPoints)						// if the projectile has not 
				{													//  reached its ending point
					repaint();										// repaint to continue animation
				}
				else												// otherwise animation is finished
				{
					isAnimated = true;
					
					if (targetWasKilled)							// if a target was hit
					{				
						targets[targetKilledIndex].Kill();			// kill that target
						score += Constants.SCORE_ADD;				// add to score
					}
					else											// otherwise no targets were hit
					{
						score -= 1;									// subtract from score
					}
					
					repaint();
				}
			}
			
		}
		else if (isFired && isAnimated)								// if projectile has been 
		{															//  fired and animated			
			if (hasReachedMax)										// if RedBird reached its max height
			{
				g.setColor(trajColor);
				DrawHalfTrajectoryPath(g, prevTraj);				// redraw previous trajectory
			}
			
			if (hasBounced)											// if YellowBird has bounced
			{
				g.setColor(trajColor);
				DrawTrajectoryPath(g, prevTraj);					// redraw previous trajectory
			}
			
			g.setColor(trajColor);
			DrawTrajectory(g, curTraj);								// redraw trajectory
		}
		
		else														// otherwise draw the projectile  								
		{															// at its starting position	
			projectiles[projectileIndex].SetPosition(startingPoint);
			
			projectiles[projectileIndex].DrawProjectile(g, width, height);
		}
	}
	
	public void FireYellowBird(Graphics g)
	// PRE: g is the current graphics context
	// POST: A YellowBird has been animated and/or drawn appropriately to the applet
	{
		Point prevEndPoint;										// point at the end of first trajectory,
																//  i.e. before the YellowBird bounces
		Point temp;												// new start point for YellowBird, 
																//  slightly above end point of 
																//  previous trajectory
		
		if (curEndPoints < numPoints)							// if the projectile has not 
		{														//  reached its ending point
			g.setColor(trajColor);
			
			if (hasBounced)										// if YellowBird has already bounced
			{
				DrawTrajectoryPath(g, prevTraj);				// redraw previous trajectory
				AnimateTrajectory(g, curTraj);
			}
			else												// otherwise YellowBird has not bounced
			{
				AnimateTrajectory(g, curTraj);					// continue animation
			}
			
			repaint();								
		}
		else													// otherwise YellowBird hit the ground
		{
			if(!hasBounced)										// if YellowBird object has not 
			{													//  bounced yet
				prevEndPoint = curTraj.GetPoint(curEndTime);

				temp = new Point(prevEndPoint.GetXCoordinate(), // new starting point is slightly 
								 prevEndPoint.GetYCoordinate() 	//  above the ground
								 - Constants.BOUNCE_SCALE);
				
				prevTraj = curTraj;								// prevTraj is Trajectory 
																//  before the bounce
				curTraj = new Trajectory(angle, velocity/2,  	// curTraj is new Trajectory starting at 
										 temp, width, height);	//  the point where YellowBird hit the 
																//  ground with velocity halved
				
				isFired = true;									// set so a trajectory will be drawn
				isDrawn = false;
				isAnimated = false;
				hasBounced = true;
				curEndTime = 0;
				numPoints = 0;
				curEndPoints = 0;
				
				repaint();										// repaint to continue animation
			}
			else												// else YellowBird has already bounced
			{
				isAnimated = true;
				
				if (targetWasKilled)							// if a target was hit
				{	
					targets[targetKilledIndex].Kill();			// kill that Target
					score += Constants.SCORE_ADD;				// add to score
				}
				else											// otherwise no targets were hit
				{
					score -= 1;									// subtract from score
				}
				
				repaint();
			}
			
		}
	}
	
	public void FireRedBird(Graphics g)
	// PRE: g is the current graphics context
	// POST: A RedBird has been animated and/or drawn appropriately to the applet
	{
		Point prevEndPoint;									// point at the end of first trajectory,
															//  i.e. before RedBird reaches max height
		Point temp;											// new start point for RedBird, 
															//  slightly above end point of 
															//  previous trajectory
		
		if(curEndPoints == maxPointNum && !hasReachedMax)	// if RedBird has reached its max height
		{
			hasReachedMax = true;
			
			prevEndPoint = curTraj.GetPoint(curEndTime);	// point where RedBird reached max height

			temp = new Point(prevEndPoint.GetXCoordinate(), // new start point is RedBird's max height
							 prevEndPoint.GetYCoordinate());
			
			prevTraj = curTraj;								// prevTraj is Trajectory before max height
			curTraj = new Trajectory(Constants.DOWN_ANGLE, 
									 velocity/2, temp,  
									 width, height);
			
			isFired = true;									// set so a trajectory will be drawn
			isDrawn = false;
			isAnimated = false;
			hasBounced = false;
			targetWasKilled = false;
			curEndTime = 0;
			numPoints = 0;
			curEndPoints = 0;
			prevMaxPointNum = maxPointNum;
			maxPointNum = 0;
			
			repaint();										// repaint to continue animation
		}
		
		else if (curEndPoints < numPoints)					// if the RedBird has not 
		{													//  reached its ending point
			g.setColor(trajColor);
			
			if (hasReachedMax)								// if RedBird has already reached max height
			{
				DrawHalfTrajectoryPath(g, prevTraj);		// redraw previous trajectory
				AnimateTrajectory(g, curTraj);
			}
			else											// otherwise RedBird has not reached max
			{
				AnimateTrajectory(g, curTraj);
			}
			
			repaint();
		}
				
		else												// otherwise animation is finished
		{
				isAnimated = true;
				
				if (targetWasKilled)						// if a target was hit
				{
					targets[targetKilledIndex].Kill();		// kill that Target
					score += Constants.SCORE_ADD;			// add to score
				}
				else										// otherwise no targets were hit
				{
					score -= 1;								// subtract from score
				}
				
				repaint();
		}
	}
	
	public void CalculateEndTime(Trajectory traj)
	// POST: calculates the endTime of the projectile
	{
		double curTime;				// current time during projectile's motion, in seconds
		double timeIntvl;			// time interval over which to compute location of the projectile
		Point curPoint;				// current location of the projectile
		boolean hasHit;				// true when the projectile has hit a target
		int counter;				// number of points computed thus far
		double projSideLength;		// length of each side of current projectile
		
		curTime = 0;
		timeIntvl = Constants.TIME_INTERVAL;
		counter = 0;
		projSideLength = projectiles[projectileIndex].
						 GetSideLength();
		
		curPoint = traj.GetPoint(timeIntvl);			// set current location initially to location 
														//  at the first time unit in the future
		hasHit = false;
		
		if(!isDrawn)											// if projectile is drawn for the first
		{														//  time, then calculate new endTime
			
			while (!hasHit)											// while projectile has 
			{														//  not hit anything
				
				curPoint = traj.GetPoint(curTime);					// curPoint is projectile's current 
																	//  position at time		
				
				if (curPoint.GetYCoordinate() + 
						projSideLength >= 							// if projectile has
						1 - Constants.GROUND_SCALE)					//  reached the ground
				{
					hasHit = true;
				}
				else												// otherwise check if projectile 
				{													//  has hit a target
					for (int i = 0; i < Constants.NUM_TARGETS; i++)	// check each Target individually
					{
						if (targets[i].isHit(curPoint, 				// if targets[i] has been hit
											 width, height, 
											 projectiles[projectileIndex]))	
						{
							targetKilledIndex = i;
							targetWasKilled = true;
							hasHit = true;
						}
					}
				}
				
				counter++;
				curTime = counter*timeIntvl;						// check next time
				numPoints++;
				
				if (curPoint.GetYCoordinate() < 					// if curPoint is 
						maxPoint.GetYCoordinate())					//  higher than maxPoint
					{
						maxPoint = curPoint;						// curPoint is new maxPoint
						maxPointNum = counter;
					}
			}
			
			isDrawn = true;
			traj.SetNumPoints(counter);
		}
	}
	
	public void AnimateTrajectory(Graphics g, Trajectory traj)
	// PRE: g is the current graphics context
	// POST: the trajectory of the projectile has been drawn
	{		
		try
		{			
			DrawTrajectory(g, traj);					// draw trajectory until curEndTime
			
														// draw next step of trajectory
			curEndPoints++;
			
			Thread.sleep(Constants.SLEEP_TIME);
		}
		catch(InterruptedException ie) {}
	}
	
	public void DrawTrajectory(Graphics g, Trajectory traj)
	// PRE: g is the current graphics context
	// POST: the trajectory of the projectile has been drawn with projectile at the end
	{		
		double timeIntvl;			// time interval over which to compute location of the projectile
		double curTime;				// current time during the projectile's flight
		Point curPoint;				// current location of the projectile
		Point prevPoint;			// location of the projectile one time unit previous
		
		timeIntvl = Constants.TIME_INTERVAL;
		prevPoint = traj.GetStartingPoint();			// set previous location to startingPoint
		curPoint = traj.GetPoint(timeIntvl);			// set current location initially to location 
														//  at the first time unit in the future
		curTime = timeIntvl;
		
		for (int i = 1; i < curEndPoints; i++)			// draw a point for each time interval for the
														// projectile; continue while projectile has not
														// hit anything (the ground or a Target object)
		{
			curPoint = traj.GetPoint(curTime);
			
			g.drawLine((int)(prevPoint.GetXCoordinate()*width),    // draw line between 
					(int)(prevPoint.GetYCoordinate()*height),      //  prevPoint and curPoint
					(int)(curPoint.GetXCoordinate()*width), 
					(int)(curPoint.GetYCoordinate()*height));			

			prevPoint = curPoint;								   // for next time value, prevPoint is 
																   //  the current location
			
			curTime = timeIntvl + i*timeIntvl;
			
			curEndTime = curTime;
		}
		
		projectiles[projectileIndex].SetPosition(curPoint);		   // draw the projectile at 
																   //  its current point
		
		projectiles[projectileIndex].DrawProjectile(g, width, height);
	}
	
	public void DrawTrajectoryPath(Graphics g, Trajectory traj)
	// PRE: g is the current graphics context
	// POST: the trajectory of the projectile has been drawn without projectile at end of path
	{		
		double timeIntvl;			// time interval over which to compute location of the projectile
		double curTime;				// current time during the projectile's flight
		Point curPoint;				// current location of the projectile
		Point prevPoint;			// location of the projectile one time unit previous
		
		timeIntvl = Constants.TIME_INTERVAL;
		prevPoint = traj.GetStartingPoint();			// set previous location to startingPoint
		curPoint = traj.GetPoint(timeIntvl);			// set current location initially to location 
														//  at the first time unit in the future
		curTime = timeIntvl;
		
		for (int i = 1; i < traj.GetNumPoints(); i++)	// draw a point for each time interval for the
														// projectile; continue while projectile has not
														// hit anything (the ground or a Target object)
		{
			curPoint = traj.GetPoint(curTime);
			
			g.drawLine((int)(prevPoint.GetXCoordinate()*width),    // draw line between 
					(int)(prevPoint.GetYCoordinate()*height),      //  prevPoint and curPoint
					(int)(curPoint.GetXCoordinate()*width), 
					(int)(curPoint.GetYCoordinate()*height));			

			prevPoint = curPoint;								   // for next time value, prevPoint is 
																   //  the current location
			
			curTime = timeIntvl + i*timeIntvl;
			
			curEndTime = curTime;
		}
	}
	
	public void DrawHalfTrajectoryPath(Graphics g, Trajectory traj)
	// PRE: g is the current graphics context
	// POST: the trajectory of the projectile has been drawn up to its max height
	{		
		double timeIntvl;			// time interval over which to compute location of the projectile
		double curTime;				// current time during the projectile's flight
		Point curPoint;				// current location of the projectile
		Point prevPoint;			// location of the projectile one time unit previous
		
		timeIntvl = Constants.TIME_INTERVAL;
		prevPoint = traj.GetStartingPoint();			// set previous location to startingPoint
		curPoint = traj.GetPoint(timeIntvl);			// set current location initially to location 
														//  at the first time unit in the future
		curTime = timeIntvl;
		
		// maxPointNum
		
		for (int i = 1; i < prevMaxPointNum; i++)		// draw a point for each time interval for the
														// projectile; continue while projectile has not
														// hit anything (the ground or a Target object)
		{
			curPoint = traj.GetPoint(curTime);
			
			g.drawLine((int)(prevPoint.GetXCoordinate()*width),    // draw line between 
					(int)(prevPoint.GetYCoordinate()*height),      //  prevPoint and curPoint
					(int)(curPoint.GetXCoordinate()*width), 
					(int)(curPoint.GetYCoordinate()*height));			

			prevPoint = curPoint;								   // for next time value, prevPoint is 
																   //  the current location
			
			curTime = timeIntvl + i*timeIntvl;
		}
	}
	
	public void DrawBackground(Graphics g)
	// PRE: g is the current graphics context
	// POST: the sky, ground and slingshot have been drawn to the screen
	{
		g.setColor(Color.CYAN);
		g.fillRect(0, 0, (int)width, (int)(height-Constants.GUI_HEIGHT));  // draw the sky
		
		g.setColor(Color.GREEN);
		g.fillRect(0, (int)(height*(1-Constants.GROUND_SCALE)), 		   // draw the ground
				  (int) width, (int)height);   
		
		g.setColor(Color.BLACK);
		g.drawLine((int)(startingPoint.GetXCoordinate()*width), 		   // draw the slingshot base
				   (int)(startingPoint.GetYCoordinate()*height), 
				   (int)(startingPoint.GetXCoordinate()*width), 
				   (int)(height*(1-Constants.GROUND_SCALE)));
		
		g.drawLine((int)(startingPoint.GetXCoordinate()*width),			   // draw first slingshot arm
				   (int)(startingPoint.GetYCoordinate()*height), 
				   (int)(startingPoint.GetXCoordinate()*width + 
				   width*Constants.SLINGSHOT_X_SCALE), 
				   (int)(startingPoint.GetYCoordinate()*height -
				   height*Constants.SLINGSHOT_Y_SCALE));
		
		g.drawLine((int)(startingPoint.GetXCoordinate()*width),			   // draw second slingshot arm
				   (int)(startingPoint.GetYCoordinate()*height), 
				   (int)(startingPoint.GetXCoordinate()*width - 
				   width*Constants.SLINGSHOT_X_SCALE), 
				   (int)(startingPoint.GetYCoordinate()*height -
				   height*Constants.SLINGSHOT_Y_SCALE));
	}
	
	@Override
	public void actionPerformed(ActionEvent e)					// Handle actions on fireBtn & resetBtn
	{
		if (e.getSource() == fireBtn)							// if fireBtn was pressed
		{
			if (GetVelocity(velocityField.getText()) && 		// if user entered valid values for 
					GetAngle(angleField.getText()))				// both velocity and angle
			{
				curTraj = new Trajectory(angle, velocity, 		// create new trajectory based on angle, 
						startingPoint, width, height);			//  velocity and startingPoint
				
				isFired = true;									// set so a trajectory will be drawn
				isDrawn = false;
				isAnimated = false;
				targetWasKilled = false;
				hasBounced = false;
				hasReachedMax = false;
				
				curEndTime = 0;
				numPoints = 0;
				curEndPoints = 0;
				
				if (!isAtStart)									// if projectile is not at start point
				{
					projectileIndex = (int)(Math.random()*		// choose new random projectile
					          Constants.NUM_PROJECTILES);
				}
				
				isAtStart = false;
			}
			else												// otherwise do not fire the projectile
			{
				isFired = false;
			}
		}
		
		if (e.getSource() == resetBtn)							// if resetBtn was pressed
		{
			score = 0;											// reset score to 0
			isFired = false;									// clear trajectory if one is drawn
			isDrawn = false;
			isAnimated = false;
			isAtStart = true;
			projectileIndex = (int)(Math.random()*
			          Constants.NUM_PROJECTILES);
			
			for (int i = 0; i < Constants.NUM_TARGETS; i++)		// revive all Target objects in targets
			{
				targets[i].Revive();							// revive targets[i]
			}
		}	
		
		repaint();
	}
	
	@Override
	public void itemStateChanged(ItemEvent e)		// Handle actions on colorDropdown 
	{
		trajColor = Constants.COLORS[colorDropDown.getSelectedIndex()];
		
		repaint();
	}
	
	@Override
	public void stateChanged(ChangeEvent e) 		// handle changes to angleSlider and velocitySlider
	{
		String value;
		
		if (e.getSource() == angleSlider)						// if angleSlider thumb was moved
		{
			value = Integer.toString(angleSlider.getValue());
			
			angleField.setText(value);							// print current value in angleField
		}
		else if (e.getSource() == velocitySlider)				// if velocitySlider thumb was moved
		{
			value = Integer.toString(velocitySlider.getValue());
			
			velocityField.setText(value);						// print current value in velcocityField
		}
	}
	
	private boolean GetVelocity(String velocityText)
	// PRE: velocityText is initialized
	// POST: returns true if velocityText is a valid value for velocity,
	//		 velocity is set to parsed value from velocityText
	{
		boolean isValidFormat;										  // true if velocityText is a 
																	  // valid velocity value
		try
		{
			velocity = Double.parseDouble(velocityField.getText());	  // get velocity from velocityField
			
			if(velocity > 0)										  // valid if velocity is 
			{														  //  a positive value
				isValidFormat = true;
			}
			else													  // otherwise velocity is 		
			{														  // an unallowed value
				JOptionPane.showMessageDialog(null, 
						"Entered unallowed velocity value." +
						"Enter a positive velocity value");
				isValidFormat = false;
			}
		}
		catch(NumberFormatException nfe)							  // catch exception if user enters 
		{															  // something other than a number
			JOptionPane.showMessageDialog(null, 
					"Entered invalid number format for velocity");
			isValidFormat = false;
		}
		
		return isValidFormat;
	}
	
	private boolean GetAngle(String angleText)
	// PRE: angleText is initialized
	// POST: returns true if angleText is a valid value for angle, 
	// 		 angle is set to parsed value from angleText
	{
		boolean isValidFormat;									// true if angleText is a 
																// valid angle value
		try
		{
			angle = Double.parseDouble(angleField.getText());	// get angle from angleField
			isValidFormat = true;
		}
		catch(NumberFormatException nfe)						// Catch exception if user enters 
		{														// something other than a number
			JOptionPane.showMessageDialog(null, 
					"Entered invalid number format for angle");
			isValidFormat = false;
		}
		
		return isValidFormat;
	}
}
