package a3.s982003034;

import javax.swing.JOptionPane;

public class A32 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		LinearEquation eq;
		double [] var = new double [6]; // for storing variables before eq is initialized
		double x, y;
		String temp;
		
		// get user input
		for (int i = 0; i < 6; i ++) {
			                                         // convert i to ASCII's a,b,c,d,e,f
			temp = JOptionPane.showInputDialog("Input variable " + (char)(i+97) + ": ");
			var[i] = Double.parseDouble(temp);
		}
		
		eq = new LinearEquation (var[0], var[1], var[2], var[3], var[4], var[5]);
		
		// solve the linear equation
		if (eq.isSolvable()) {
			x = eq.getX();
			y = eq.getY();
			// set up the output message
			temp =
					eq.geta() + "x + " + eq.getb() + "y = " + eq.gete() + "\n" +
					eq.getc() + "x + " + eq.getd() + "y = " + eq.getf() + "\n" +
					"x= " + x + "\n" + "y= " + y + "\n";
			JOptionPane.showMessageDialog(null, temp);
		}
		else JOptionPane.showMessageDialog(null, "The equation is insolvable!");			
	}

}

