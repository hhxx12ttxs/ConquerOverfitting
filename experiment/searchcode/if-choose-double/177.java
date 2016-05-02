/**
 * CE1002-100502514 FlashTeens Chiang
 * [The Following Rules are from ce1002 website]
 * Assignment 4-2
	
	1. Define the Triangle2D class that contains:
	  *	The points of the original triangle, which are declared with class MyPoint,
		are (0,0), (10, 15) and (17, 6). (Hint: Exercise 10.4)
	  *	A constructor that creates a new triangle with the specified points p1, p2 and p3,
		which are declared with class MyPoint.
	  *	There are several methods in the class as follows:
		+getArea(): returns the area of the new triangle.
		+getPerimeter(): returns the perimeter of the new triangle.
		+contains(double x, double y): returns true if the specified point p is inside the original triangle.
		+contains(Triangle2D t): returns true if the specified triangle is inside the original triangle.
		 (Official Hint: Return true if triangles APB+BPC+CPA=ABC [May cause a bit errors.])
		 (My own way: Return true if vectors AP=nAB+mAC, n>=0 ,m>=0, n+m<=1;
		  solving n,m with Kramer's Formula, borrowed from Assignment 3-2)

	2. Write a test program, including the following functionality:
	  * Get the Area of the new triangle.
	  * Get the Perimeter of the new triangle.
	  * Check whether the new triangle is in the original triangle or not.
	  * Enter a point and check the point is in the original triangle or not.
	  * a list and infinite loop

	3. (Some GUI skills as Extra Points)
	
	See ce1002 website for more informations.
	
	[Notice]
	# The term "Original Triangle" here is called "Initial Triangle" instead.
	# To simply the GUI interface, few of the class structures might violate some detail requirements,
	  but no influences for runtime requirements.
	  (I think that a single Triangle2D object should not contain 6 points for edges,
	   thus I declare 2 Triangle2D(3 points for each) instead.)
	
 */
package a4.s100502514;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class A42 extends MyBaseWindow {
	
	/** Declare data for new triangle 1 */
	Triangle2D triangle_new1 = new Triangle2D(
			new MyPoint(1,1), new MyPoint(1,2), new MyPoint(2,1)
			);
	/** Declare data for new triangle 2 */
	Triangle2D triangle_new2 = null;
	
	
	/** Declare a drawing pane that shows circle and coordinate */
	A42_EquationPane drawingPane = new A42_EquationPane();
	
	/** Constructor to Initialize Window */
	public A42(){
		/** Initializing Window */
		initialize("Triangle Checker by 100502514 on Mar 16 2012", 500, 400);
		
		/** Initializing Input Area */
		JPanel inputArea = new JPanel(new GridLayout(1,4));
		//Add Buttons
		for(int i=0;i<3;i++){
			JButton btn = new JButton("Set Point "+(char)(i+'A'));
			btn.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					int point_index=-1;
					for(int n=0;n<3;n++){
						if(((JButton)(e.getSource())).getText().indexOf(" "+(char)(n+'A'))>=0){
							point_index=n;
							break;
						} 
					}
					showChangePositionDialog(point_index);
				}
			});
			JPanel flow = new JPanel(new FlowLayout());
			flow.add(btn);
			inputArea.add(flow);
		}
		
		//Add "Input" button
		JButton input_btn = new JButton("Select & Calculate");
		input_btn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				onClickInput();
			}
		});
		JPanel myFlowPanel = new JPanel(new FlowLayout());
		myFlowPanel.add(input_btn);
		inputArea.add(myFlowPanel);
		
		//Finally add Input area to window
		JPanel inputAndLabelArea = new JPanel(new GridLayout(2,1));
		inputAndLabelArea.add(inputArea);
		inputAndLabelArea.add(new JLabel("Initial(Black) Triangle Points:\n(0, 0)  (10, 15)  (17, 6)"));
		add(inputAndLabelArea, BorderLayout.NORTH);
		
		//Then add an area for painting circle and point
		add(drawingPane, BorderLayout.CENTER);
		
		//Initialize the display of drawingPane
		drawingPane.initialize(triangle_new1, triangle_new2, null);
		setVisible(true);
	}
	
	/** Show Position-changing Dialog */
	private MyPoint showChangePositionDialog(int point_index){
		try{
			/** Show Input dialog for changing position of a point
			 	The way to input is described as what the message shows. */
			String current_point_info = "";
			if(point_index>=0){
				current_point_info =
						"The current position of Point "+(char)(point_index+'A')+" is " +
						triangle_new1.getMyPoint(point_index).toString()+"\n" +
						"Please input a new position for Point "+(char)(point_index+'A')+":\n";
			}else{
				current_point_info = "Please enter a point and check if it is\n" +
						"in the original triangle:";
			}
			String[] temp = JOptionPane.showInputDialog(
					current_point_info +
					"You can input in the form like:\n" +
					"* (1.2, 3.4)  -- with parentheses and comma\n" +
					"* 1.2 3.4  -- with only a separater in the middle like space" +
					(point_index>=0?"\n\nYou may also press \"Cancel\" to show the point state only.":"")
					).split("[^0123456789e\\.\\-]");
			if(point_index>=0){
				double[] nums = getSplitArrData(temp);
				triangle_new1.getMyPoint(point_index).setPosition(nums[0], nums[1]);
				drawingPane.initialize(triangle_new1, triangle_new2, null);
				if(JOptionPane.showConfirmDialog(null,
						"Position is successfully changed!!\n" +
						"Would you like to show if this point is\n" +
						"inside or outside the initial triangle?")==JOptionPane.YES_OPTION){
					showPointIsInsideInitialTriangle(triangle_new1.getMyPoint(point_index));
				}
				return triangle_new1.getMyPoint(point_index);
			}else{
				MyPoint newPt = new MyPoint();
				double[] nums = getSplitArrData(temp);
				newPt.setPosition(nums[0], nums[1]);
				drawingPane.initialize(triangle_new1, triangle_new2, newPt);
				showPointIsInsideInitialTriangle(newPt);
				return newPt;
			}
		}catch(NumberFormatException err){
			/** Show this message if the input format is invalid. */
			JOptionPane.showMessageDialog(null,
					"Invalid Value!!\n" +
					"Please check if your number is valid.\n");
		}catch(ArrayIndexOutOfBoundsException err){
			/** Show this message if the input format is invalid. */
			JOptionPane.showMessageDialog(null,
					"Invalid Value!!\n" +
					"You have to input 2 numbers separated with non-digit character(s).");
		}catch(Exception err){
			/** Show the result of inside/outside message
				if user closes the input dialog or press "cancel". */
			if(point_index>=0){
				//Still display the point state if necessary
				showPointIsInsideInitialTriangle(triangle_new1.getMyPoint(point_index));
			}
		}
		return null;
	}
	
	/** Get 2 unempty values for array of doubles */
	private double[] getSplitArrData(String[] splitted){
		double[] arr = new double[2];
		int p=0;
		for(int ch=0; ch<splitted.length&&p<2; ch++){
			if(!(splitted[ch].isEmpty()))arr[p++]=Double.parseDouble(splitted[ch]);
			if(p>=2)return arr;
		}
		//Return an empty array if invalid
		return new double[0];
	}
	
	/** Show the result if a point is inside or outside the initial triangle */
	private void showPointIsInsideInitialTriangle(MyPoint p){
		if(triangle_new1.getOriginalTriangle().contains(p)){
			JOptionPane.showMessageDialog(null, "Point "+p.toString()+" is INSIDE the initial triangle!!");
		}else{
			JOptionPane.showMessageDialog(null, "Point "+p.toString()+" is OUTSIDE the initial triangle!!");
		}
	}
	
	/** This method will run when clicked on "Select & Calculate" button */
	public void onClickInput(){
		String button_labels[] = {"Area","Perimeter",
				"New Triangle State",
				"New Point State"};
		int choice = JOptionPane.showOptionDialog(this,
				"Please choose which function to use:\n",
				"Assignment 4-2 by 100502514",
				JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
				button_labels, null);
		
		switch(choice){
		case 0: /** area */
			JOptionPane.showMessageDialog(null, "The AREA of Triangle ABC is "+
					triangle_new1.getArea());
			break;
		case 1: /** perimeter */
			JOptionPane.showMessageDialog(null, "The PERIMETER of Triangle ABC is "+
					triangle_new1.getPerimeter());
			break;
		case 2: /** check 2nd new triangle */
			triangle_new2 = new Triangle2D(new MyPoint(0,0), new MyPoint(0,0), new MyPoint(0,0));
			try{
				for(int count=0;count<3;count++){
					String[] temp = JOptionPane.showInputDialog(
						"Please input Point "+(char)('D'+count)+" in Triangle DEF:\n" +
						"You can input in the form like:\n" +
						"* (1.2, 3.4)  -- with parentheses and comma\n" +
						"* 1.2 3.4  -- with only a separater in the middle like space"
						).split("[^0123456789e\\.\\-]");
					double[] pt = getSplitArrData(temp);
					triangle_new2.getMyPoint(count).setPosition(pt[0],pt[1]);
				}
				JOptionPane.showMessageDialog(null, "Triangle DEF is "+
					(triangle_new1.getOriginalTriangle().contains(triangle_new2)?"INSIDE":"OUTSIDE")+
					" the Original Triangle.");
				drawingPane.initialize(triangle_new1, triangle_new2, null);
			}catch(NumberFormatException err){
				/** Show this message if the input format is invalid. */
				JOptionPane.showMessageDialog(null,
						"Invalid Value!!\n" +
						"Please check if your number is valid.\n");
			}catch(ArrayIndexOutOfBoundsException err){
				/** Show this message if the input format is invalid. */
				JOptionPane.showMessageDialog(null,
						"Invalid Value!!\n" +
						"You have to input 2 numbers separated with non-digit character(s).");
			}catch(Exception err){
				/** Do nothing when user clicks "cancel" */
			}
			
			/** Clear the 2nd new triangle */
			triangle_new2 = null;
			break;
		case 3: /** check new point */
			//enter a new point and show the result.
			showChangePositionDialog(-1);
			break;
		}
		
	}
	
	public static void main(String[] args){
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				A42 myWindow = new A42();
				//Show the application window
				myWindow.setVisible(true);
			}
		});
	}
}

