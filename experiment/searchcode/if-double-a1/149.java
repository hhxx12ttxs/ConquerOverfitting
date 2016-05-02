import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.*;



public class ATester extends JFrame{

	JPanel jp1 = new JPanel();
	JPanel jp2 = new JPanel();
	JPanel jp3 = new JPanel();
	JPanel jp4 = new JPanel();

	JButton jb = new JButton("Calculate");

	JLabel jl1 = new JLabel();
	JLabel jl2 = new JLabel();
	JTextField bx1 = new JTextField("Enter Test Score",20);
	JTextField bx2 = new JTextField("Enter Weight",20);
	JTextField bx3 = new JTextField("Enter Test Score",20);
	JTextField bx4 = new JTextField("Enter Weight",20);
	JTextField bx5 = new JTextField("Enter Test Score",20);
	JTextField bx6 = new JTextField("Enter Weight",20);
	JTextField bx7 = new JTextField("Enter Test Score",20);
	JTextField bx8 = new JTextField("Enter Weight",20);

	
	public static double getMaxValue(double[] array){  
	      double maxValue = array[0];  
	      for(int i=1;i < array.length;i++){  
	      if(array[i] > maxValue){  
	      maxValue = array[i];  

	         } }return maxValue; 
	     }  
	public ATester()
	{
		setLayout(new FlowLayout());

		setSize(200,200);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		jp1.add(bx1);
		jp1.add(bx2);
		jp2.add(bx3);
		jp2.add(bx4);
		jp3.add(bx5);
		jp3.add(bx6);
		jp4.add(bx7);	
		jp4.add(bx8);	
		
		add(jp1);
		add(jp2);
		add(jp3);
		add(jp4);
		add(jb, BorderLayout.SOUTH);
		jb.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String g1 = bx1.getText();
				String w1 = bx2.getText();
				String g2 = bx3.getText();
				String w2 = bx4.getText();
				String g3 = bx5.getText();
				String w3 = bx6.getText();
				String g4 = bx7.getText();
				String w4 = bx8.getText();

				int grade1 = Integer.parseInt(g1);
				int grade2 = Integer.parseInt(g2);
				int grade3 = Integer.parseInt(g3);
				int grade4 = Integer.parseInt(g4);

				double weight1 = Double.parseDouble(w1);
				double weight2 = Double.parseDouble(w2);
				double weight3 = Double.parseDouble(w3);
				double weight4 = Double.parseDouble(w4);
				
				double [] a1 = {grade1, grade2, grade3, grade4};

				double sum = (grade1*weight1)+(grade2*weight2)+(grade3*weight3)+(grade4*weight4);
				System.out.println("Weighted average is: "+sum);
				System.out.println("The highest test grade was: " + getMaxValue(a1));
				
				
				
			}
		});
		
		
	}
	public static void main(String[] args) 
	{
		new ATester();
	}
}

