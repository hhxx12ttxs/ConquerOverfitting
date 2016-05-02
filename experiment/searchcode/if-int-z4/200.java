package a2.s995002204;

import java.util.Scanner;
import javax.swing.JOptionPane;

public class A21 {
	public static void main(String args[])
	{
		int onearray[];		//???? ??????
		onearray = new int[10];
		boolean flag = true; 	//????
		
		String z1 = JOptionPane.showInputDialog("????????:");		//????????
		String z2 = JOptionPane.showInputDialog("????????:");
		String z3 = JOptionPane.showInputDialog("????????:");
		String z4 = JOptionPane.showInputDialog("????????:");
		String z5 = JOptionPane.showInputDialog("????????:");
		String z6 = JOptionPane.showInputDialog("????????:");
		String z7 = JOptionPane.showInputDialog("????????:");
		String z8 = JOptionPane.showInputDialog("????????:");
		String z9 = JOptionPane.showInputDialog("????????:");
		String z10 = JOptionPane.showInputDialog("????????:");
		
		onearray[0] = Integer.parseInt(z1);		//???????????
		onearray[1] = Integer.parseInt(z2);
		onearray[2] = Integer.parseInt(z3);
		onearray[3] = Integer.parseInt(z4);
		onearray[4] = Integer.parseInt(z5);
		onearray[5] = Integer.parseInt(z6);
		onearray[6] = Integer.parseInt(z7);
		onearray[7] = Integer.parseInt(z8);
		onearray[8] = Integer.parseInt(z9);
		onearray[9] = Integer.parseInt(z10);
		
		
		
		for(int i=0;i<onearray.length&& flag;i++)		//????????
		{
			flag = false;								
			System.out.println("?"+(i+1)+"???");
			for(int k=0;k<onearray.length;k++)
				System.out.printf("%d  ",onearray[k]);
			System.out.println();
			for(int j=0;j<onearray.length-1;j++)
			{
				 if ( onearray[j] > onearray[j+1] ) 
				 {
					 Swap(j , j + 1 , onearray);
					 flag = true; 
				 }
			}
		}
	}
	public static void Swap(int a, int b, int[] onearray)		//????
	{
		int temp;
		temp = onearray[b];
		onearray[b] = onearray[a];
		onearray[a] = temp;
	}
}

