//??????????
//????panel0?=?0??????????????????


package ce1002.A7.s101502022;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class A7  extends JFrame implements ActionListener{

	double n1=0.0,n2,n3;
	JButton button1[]=new JButton[10];
	private static JButton cn,ad,sub,mul,div,amo;
	private static Panel pn1=new Panel(new GridLayout(4,3,5,5));
	private static Panel pn2=new Panel(new GridLayout(4,1,5,5));
	private static long num;//????
	private static byte op;//?????
	static long x;
	String error="error";
	long result;//??????????
	boolean point;
	char operator;
	 double number;
	JTextField numberfield;
    String numberstring="";
	
	public A7(){
        numberfield=new JTextField("0.0",14);
        numberfield.setHorizontalAlignment(JTextField.RIGHT);
		JPanel p1= new JPanel();
		p1.setLayout(new GridLayout(4,4,5,10));//????row,??column
		for(int i=7;i<=9;i++){
			button1[i]=new JButton(Integer.toString(i));
			pn1.add(button1[i]);
			button1[i].addActionListener(this);
			}
		for(int i=4;i<=6;i++){

			button1[i]=new JButton(Integer.toString(i));
			pn1.add(button1[i]);
			button1[i].addActionListener(this);
			}
		for(int i=1;i<=3;i++){

			button1[i]=new JButton(Integer.toString(i));
			pn1.add(button1[i]);
			button1[i].addActionListener(this);
			}
		button1[0]=new JButton(Integer.toString(0));
		pn1.add(button1[0]);
		button1[0].addActionListener(this);
		
		
		cn = new JButton("C");//??
		pn1.add(cn);
		cn.addActionListener(this);
		

		sub=new JButton("-");//??
		pn2.add(sub);
		sub.addActionListener(this);
		
		ad=new JButton("+");//??
		pn2.add(ad);
		ad.addActionListener(this);
		
		amo=new JButton("=");
		amo.setSize(30,100);
		pn2.add(amo);
		amo.addActionListener(this);
		
		JPanel p3= new JPanel();
		p3.add(numberfield,BorderLayout.NORTH);
		p3.add(pn1,BorderLayout.WEST);
		p3.add(pn2,BorderLayout.EAST);
		add(p3);
		
		
	}
	
	
	public void actionPerformed(ActionEvent event) {
		if(((JButton)event.getSource()).getText().charAt(0)=='C'){
			numberfield.setText("0");
        }else if(((JButton)event.getSource()).getText().charAt(0)<='9'&&
                 ((JButton)event.getSource()).getText().charAt(0)>='0'){
            numberstring+=((JButton)event.getSource()).getText().charAt(0)+"";
             numberfield.setText(numberstring);
        }else{
            point=false;
            numberstring="";
            numberfield.setText(operation(number,
                    Double.parseDouble(numberfield.getText()),operator)+"");
            operator=((JButton)event.getSource()).getActionCommand().charAt(0);
            if(number>999999999)
    			numberfield.setText("error");
    		
            
            if(((JButton)event.getSource()).getText().charAt(0)!='=')                  
                number=Double.parseDouble(numberfield.getText());               
        } 
	}
	
	public String operation(double number1,double number2,char operator)
    {      
        switch(operator)
        {        
	        case '+':
	        	if(number1+number2>999999999)
	        		return error;
	        	else
	        		return ""+(number1+number2);
	        case '-':
	        	if(number1+number2>999999999)
	        		return error;
	        	else
	        		return ""+(number1-number2);
	        default:
	        	if(number1+number2>999999999)
	        		return error;
	        	else
	        		return ""+(number2);
        }
    }
	
	public static void main(String[] args){
		A7 frame=new A7();
		frame.setTitle("???");
		frame.setSize(250,250);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
		
	}
}
