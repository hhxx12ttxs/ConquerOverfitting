package com.bdcorps.triangleMadness;

/**Purpose:
 * The Starter class contains the main method to solve the triangle and displays GUI. 
 * The menu screen to the user is also shown from this class.
 * Contains error checking methods to check if same option was input more than once.
 * 
 * Part Of: GUI Based Triangle Solving Program
 * @author Sukhpal S. Saini and Vasu Kamra
 * Last Modified: 05-04-2014 at 2:20 PM
 */

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.bdcorps.triangleSource.TriangleUnit;

/**
 * @author Sukhpal S. Saini and Vasu Kamra
 * 
 */

public class Starter extends JFrame implements ActionListener {
	public static final int CANVAS_WIDTH = 640;
	public static final int CANVAS_HEIGHT = 480;

	public int[] triangleX = { 0, 100, 200 };
	public int[] triangleY = { 0, 100, 200 };
	boolean dragging0 = false, dragging1 = false, dragging2 = false;
	Circle c0, c1, c2;

	private DrawCanvas canvas;
	TriangleUnit t; //holds the triangle
	JPanel mainPanel;//JPanel that holds the values the user will enter
	JPanel btnPanel;//JPanel that holds "Calculate" button
	JPanel solutions1Panel;// JPanel that holds solution 1
	JPanel solutions2Panel;//JPanel that holds solution 2
	JPanel solutionsPanel;//JPanel to hold both solutions
	double i1 = 30, i2 = 30, i3 = 30;
	double i4 = 60, i5 = 60, i6 = 60;
	int multiplier = 10;//scale multiplier for the triangle 
	int round = 100;//round values

	int drawSolution = 1;

	JTextField j1, j2, j3, j4, j5, j6;
	JTextField sol1_a, sol1_b, sol1_c, sol1_A, sol1_B, sol1_C, sol1_Peri,
			sol1_Area;
	JTextField sol2_a, sol2_b, sol2_c, sol2_A, sol2_B, sol2_C, sol2_Peri,
			sol2_Area;

	public Starter() {
		t = new TriangleUnit();

		//Create panels and set layout
		mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(2, 6, 20, 5));
		btnPanel = new JPanel();
		solutions1Panel = new JPanel();
		solutionsPanel = new JPanel(new BorderLayout());
		solutions1Panel.setLayout(new BoxLayout(solutions1Panel,
				BoxLayout.PAGE_AXIS));
		solutions2Panel = new JPanel();
		solutions2Panel.setLayout(new BoxLayout(solutions2Panel,
				BoxLayout.PAGE_AXIS));

		//Contents of the panels
		JLabel label_src_a = new JLabel("a:");
		JLabel label_src_b = new JLabel("b:");
		JLabel label_src_c = new JLabel("c:");
		JLabel label_src_A = new JLabel("A:");
		JLabel label_src_B = new JLabel("B:");
		JLabel label_src_C = new JLabel("C:");

		JLabel sol1_Label = new JLabel("Solution 1:");
		JLabel sol1_Label_a = new JLabel("a:");
		JLabel sol1_Label_b = new JLabel("b:");
		JLabel sol1_Label_c = new JLabel("c:");
		JLabel sol1_Label_A = new JLabel("A:");
		JLabel sol1_Label_B = new JLabel("B:");
		JLabel sol1_Label_C = new JLabel("C:");
		JLabel sol1_Label_Peri = new JLabel("Perimeter:");
		JLabel sol1_Label_Area = new JLabel("Area:");

		JLabel sol2_Label = new JLabel("Solution 2:");
		JLabel sol2_Label_a = new JLabel("a:");
		JLabel sol2_Label_b = new JLabel("b:");
		JLabel sol2_Label_c = new JLabel("c:");
		JLabel sol2_Label_A = new JLabel("A:");
		JLabel sol2_Label_B = new JLabel("B:");
		JLabel sol2_Label_C = new JLabel("C:");
		JLabel sol2_Label_Peri = new JLabel("Perimeter:");
		JLabel sol2_Label_Area = new JLabel("Area:");

		j1 = new JTextField("");
		j2 = new JTextField("");
		j3 = new JTextField("");
		j4 = new JTextField("");
		j5 = new JTextField("");
		j6 = new JTextField("");

		sol1_a = new JTextField("	", 5);
		sol1_b = new JTextField("	", 5);
		sol1_c = new JTextField("	", 5);
		sol1_A = new JTextField("	", 5);
		sol1_B = new JTextField("	", 5);
		sol1_C = new JTextField("	", 5);
		sol1_Peri = new JTextField("	", 5);
		sol1_Area = new JTextField("	", 5);

		sol2_a = new JTextField("	", 5);
		sol2_b = new JTextField("	", 5);
		sol2_c = new JTextField("	", 5);
		sol2_A = new JTextField("	", 5);
		sol2_B = new JTextField("	", 5);
		sol2_C = new JTextField("	", 5);
		sol2_Peri = new JTextField("	", 5);
		sol2_Area = new JTextField("	", 5);

		//Add contents to panels
		mainPanel.add(label_src_a);
		mainPanel.add(label_src_b);

		mainPanel.add(label_src_c);
		mainPanel.add(label_src_A);

		mainPanel.add(label_src_B);
		mainPanel.add(label_src_C);

		sol1_Label_a.setLabelFor(sol1_a);
		sol1_Label_b.setLabelFor(sol1_b);
		sol1_Label_c.setLabelFor(sol1_c);
		sol1_Label_A.setLabelFor(sol1_A);
		sol1_Label_B.setLabelFor(sol1_B);
		sol1_Label_C.setLabelFor(sol1_C);

		mainPanel.add(j1);
		mainPanel.add(j2);

		mainPanel.add(j3);
		mainPanel.add(j4);

		mainPanel.add(j5);
		mainPanel.add(j6);

		solutions1Panel.add(new JSeparator(SwingConstants.HORIZONTAL));
		JPanel p_sol1_1 = new JPanel();
		solutions1Panel.add(sol1_Label);
		p_sol1_1.add(sol1_Label_a);
		p_sol1_1.add(sol1_a);

		solutions1Panel.add(p_sol1_1);

		JPanel p_sol1_2 = new JPanel();
		p_sol1_2.add(sol1_Label_b);
		p_sol1_2.add(sol1_b);

		solutions1Panel.add(p_sol1_2);

		JPanel p_sol1_3 = new JPanel();
		p_sol1_3.add(sol1_Label_c);
		p_sol1_3.add(sol1_c);

		solutions1Panel.add(p_sol1_3);

		JPanel p_sol1_4 = new JPanel();
		p_sol1_4.add(sol1_Label_A);
		p_sol1_4.add(sol1_A);

		solutions1Panel.add(p_sol1_4);

		JPanel p_sol1_5 = new JPanel();
		p_sol1_5.add(sol1_Label_B);
		p_sol1_5.add(sol1_B);

		solutions1Panel.add(p_sol1_5);

		JPanel p_sol1_6 = new JPanel();
		p_sol1_6.add(sol1_Label_C);
		p_sol1_6.add(sol1_C);

		solutions1Panel.add(p_sol1_6);

		JPanel p_sol1_7 = new JPanel();
		p_sol1_7.add(sol1_Label_Peri);
		p_sol1_7.add(sol1_Peri);

		solutions1Panel.add(p_sol1_7);

		JPanel p_sol1_8 = new JPanel();
		p_sol1_8.add(sol1_Label_Area);
		p_sol1_8.add(sol1_Area);

		solutions1Panel.add(p_sol1_8);

		solutionsPanel.add(new JSeparator(SwingConstants.HORIZONTAL));

		JPanel p_sol2_1 = new JPanel();
		solutions2Panel.add(sol2_Label);
		p_sol2_1.add(sol2_Label_a);
		p_sol2_1.add(sol2_a);

		solutions2Panel.add(p_sol2_1);

		JPanel p_sol2_2 = new JPanel();
		p_sol2_2.add(sol2_Label_b);
		p_sol2_2.add(sol2_b);

		solutions2Panel.add(p_sol2_2);

		JPanel p_sol2_3 = new JPanel();
		p_sol2_3.add(sol2_Label_c);
		p_sol2_3.add(sol2_c);

		solutions2Panel.add(p_sol2_3);

		JPanel p_sol2_4 = new JPanel();
		p_sol2_4.add(sol2_Label_A);
		p_sol2_4.add(sol2_A);

		solutions2Panel.add(p_sol2_4);

		JPanel p_sol2_5 = new JPanel();
		p_sol2_5.add(sol2_Label_B);
		p_sol2_5.add(sol2_B);

		solutions2Panel.add(p_sol2_5);

		JPanel p_sol2_6 = new JPanel();
		p_sol2_6.add(sol2_Label_C);
		p_sol2_6.add(sol2_C);

		solutions2Panel.add(p_sol2_6);

		JPanel p_sol2_7 = new JPanel();
		p_sol2_7.add(sol2_Label_Peri);
		p_sol2_7.add(sol2_Peri);

		solutions2Panel.add(p_sol2_7);

		JPanel p_sol2_8 = new JPanel();
		p_sol2_8.add(sol2_Label_Area);
		p_sol2_8.add(sol2_Area);

		solutions2Panel.add(p_sol2_8);
		
		solutionsPanel.add(solutions1Panel, BorderLayout.PAGE_START);

		JButton calcButton = new JButton("Calculate");
		btnPanel.add(calcButton);
		calcButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				t = new TriangleUnit();
				int sideCount = 0, angleCount = 0;
//Error checking for entered values
				if (isValidSide(j1.getText())) {
					i1 = Double.parseDouble(j1.getText());
					sideCount++;
					t.puta1(i1);
				} else {
					j1.setText("");
				}

				if (isValidSide(j2.getText())) {
					i2 = Double.parseDouble(j2.getText());
					sideCount++;
					t.putb1(i2);
				} else {
					j2.setText("");
				}

				if (isValidSide(j3.getText())) {
					i3 = Double.parseDouble(j3.getText());
					sideCount++;
					t.putc1(i3);
				} else {
					j3.setText("");
				}

				if (isValidAngle(j4.getText())) {
					i4 =  Double.parseDouble(j4.getText());
					angleCount++;
					t.putA1(i4);
				} else {
					j4.setText("");
				}

				if (isValidAngle(j5.getText())) {
					i5 =  Double.parseDouble(j5.getText());
					angleCount++;
					t.putB1(i5);
				} else {
					j5.setText("");
				}

				if (isValidAngle(j6.getText())) {
					i6 = Double.parseDouble(j6.getText());
					angleCount++;
					t.putC1(i6);
				} else {
					j6.setText("");
				}

				if ((sideCount + angleCount) >= 3) {
					canvas.setFirstLaunch(false);
					if (t.getSolutions() == 1) {
						solutionsPanel.remove(solutions2Panel);
						validate();
					} else if (t.getSolutions() == 2) {
						solutionsPanel.add(solutions2Panel);
						validate();
					}
				}

				if (t.getSolutions() == 0) {
					canvas.setFirstLaunch(true);//Draws intial panel 
				} else {
					
					//Grabs and shows solved values of the triangle
					sol1_a.setText(roundIt(t.geta1()));
					sol1_b.setText(roundIt(t.getb1()));
					sol1_c.setText(roundIt(t.getc1()));
					sol1_A.setText(roundIt(t.getA1()));
					sol1_B.setText(roundIt(t.getB1()));
					sol1_C.setText(roundIt(t.getC1()));
					sol1_Peri.setText(roundIt(t.getPerimeter1()));
					sol1_Area.setText(roundIt(t.getArea1()));

					sol1_a.setText(roundIt(t.geta1()));
					sol2_a.setText(roundIt(t.geta2()));
					sol2_b.setText(roundIt(t.getb2()));
					sol2_c.setText(roundIt(t.getc2()));
					sol2_A.setText(roundIt(t.getA2()));
					sol2_B.setText(roundIt(t.getB2()));
					sol2_C.setText(roundIt(t.getC2()));
					sol2_Peri.setText(roundIt(t.getPerimeter2()));
					sol2_Area.setText(roundIt(t.getArea2()));

				}

				if (drawSolution == 2) {
					if (t.getSolutions() == 1) {
						drawSolution = 1;
					}
				}

				if (drawSolution == 1) {
					canvas.UpdateTriangle(t.geta1() * multiplier, t.getb1()
							* multiplier, t.getc1() * multiplier);//draws the first solution
				} else if (drawSolution == 2) {
					canvas.UpdateTriangle(t.geta2() * multiplier, t.getb2()
							* multiplier, t.getc2() * multiplier);//draws the second solution
				}
				requestFocus(); // change the focus to JFrame to receive
								// KeyEvent
			}
		});

		// Create the menu bar.
		JMenuBar menuBar = new JMenuBar();

		// Build the first menu.
		JMenu menu = new JMenu("Options");

		menuBar.add(menu);

		JMenu submenu1 = new JMenu("Rounding Places");
		menu.add(submenu1);
		JMenuItem item_round10 = new JMenuItem("10");
		submenu1.add(item_round10);

		item_round10.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				round = 10;
			}
		});

		JMenuItem item_round100 = new JMenuItem("100");
		submenu1.add(item_round100);

		item_round100.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				round = 100;
			}
		});

		JMenuItem item_round1000 = new JMenuItem("1000");
		submenu1.add(item_round1000);

		item_round1000.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				round = 1000;
			}
		});

		JMenu submenu2 = new JMenu("Solution to draw");
		menu.add(submenu2);
		JMenuItem item_drawSolution1 = new JMenuItem("Solution 1");

		item_drawSolution1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				drawSolution = 1;
			}
		});
		submenu2.add(item_drawSolution1);

		JMenuItem item_drawSolution2 = new JMenuItem("Solution 2");
		item_drawSolution2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				drawSolution = 2;
			}
		});
		submenu2.add(item_drawSolution2);

		setJMenuBar(menuBar);

		canvas = new DrawCanvas();
		canvas.setFirstLaunch(true);
		canvas.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));

		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());

		cp.add(solutionsPanel, BorderLayout.EAST);

		cp.add(mainPanel, BorderLayout.PAGE_START);
		cp.add(canvas, BorderLayout.CENTER);
		cp.add(btnPanel, BorderLayout.PAGE_END);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Handle the CLOSE
														// button
		setTitle("Triangle Solver");
		pack(); // pack all the components in the JFrame
		setVisible(true); // show it

		requestFocus();
	}

	/**Checks if the value is a valid side value or not
	 * @param str
	 * @return True, if value is +ve; False, if value is -ve
	 */
	public static boolean isValidSide(String str) {
		try {
			double d = Double.parseDouble(str);
			if (d < 0)
				return false;
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	/**Checks if the value is a valid angle value or not
	 * @param str
	 * @return True, if value is valid; False, if value is invalid
	 */
	public static boolean isValidAngle(String str) {
		try {
			double d = Double.parseDouble(str);
			if (d < 0 || d > 180)
				return false;
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public static void main(String[] args) {
		// Run the GUI codes on the Event-Dispatching thread for thread safety
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new Starter(); // Let the constructor do the job
			}
		});
	}

	/**Rounds the value x to places, determined by the menu item
	 * @param x
	 * @return Rounded value
	 */
	public String roundIt(double x) {
			x = (double)Math.round(x * round) / round; 
		return String.valueOf(x);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub

	}
}
