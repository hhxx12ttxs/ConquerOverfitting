package a2.s100502033;
import java.lang.Math;
import javax.swing.JOptionPane;
public class A22 
{
	public static double number(double x , double y)
	{
		double r2 = Math.sqrt( x*x + y*y); //??????R
		return r2;
	}
	public static void main(String[] args)
	{
		for(int i = 0 ; i < 3 ; i--)
		{
			String r = JOptionPane.showInputDialog("?????R??");
			if ( r == null) //??????
			{
				JOptionPane.showMessageDialog(null, "Thank you\nyou play");
				break;
			}
			double r1 = Double.parseDouble(r); //?STRING???DOUBLE
			if (r1 <= 0)//??????
			{
				JOptionPane.showMessageDialog(null,"??????????");
				continue; 
			}
			String y = JOptionPane.showInputDialog("???Y???");
			if ( y == null)//??????
			{
				JOptionPane.showMessageDialog(null, "Thank you\nyou play");
				break;
			}
			String x = JOptionPane.showInputDialog("???X???");
			if ( x == null)//??????
			{
				JOptionPane.showMessageDialog(null, "Thank you\nyou play");
				break;
			}
			double x1 = Double.parseDouble(x);//?STRING???DOUBLE
			double y1 = Double.parseDouble(y);//?STRING???DOUBLE
			if(number(x1,y1) < r1)
			{
				JOptionPane.showMessageDialog(null, "??????");
			}
			else if(number(x1,y1) > r1)
			{
				JOptionPane.showMessageDialog(null, "??????");
			}
			else 
			{
				JOptionPane.showMessageDialog(null, "??????");
			}
		}
	}
}

