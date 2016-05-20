/*
 *      cResistor_4Pita.java
 *      
 *      Copyright 2012 Kelompok Pemrograman Lanjut
 * 		11 S1TI 10
 * 		STMIK Amikom Yogyakarta
 *      
 */
//begin of code
package View;

//import ResistorGUI_4Pita;

import Model.*;
import Control.*;
import Control.Resistor_Main;
import java.awt.Color;
import javax.print.attribute.standard.Compression;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class ResistorGUI_4Pita extends JFrame implements GUI_MethodRule {
							
	double p1 = 0 ;//nilai default untuk pita tanpa warna
	double p2 = 0 ;//nilai default untuk pita tanpa warna
	double p3 = 0.01;//nilai default untuk pita tanpa warna
	double p4 = 0.2 ;//nilai default untuk pita tanpa warna
	
//PANEL
    private JPanel pnlBase;
    private JPanel pnlHeader;
    private JPanel pnlAbout;
    private JPanel pnlHelp;
    private JPanel pnlLine1;
  	private JPanel pnlLine2;
  	private JPanel pnlLine3;
  	private JPanel pnlLine4;
    private JPanel pnlLeft; 
    private JPanel pnlPallete;
    private JPanel pnlContent;
    private JPanel pnlResistor;
    private JPanel pnlResultRMin;
    private JPanel pnlResultRMax;
    private JPanel pnlFunction;
    private JPanel pnlPita1;
 	private JPanel pnlPita2;
 	private JPanel pnlPita3;
 	private JPanel pnlPita4;
 	private JPanel pnlSwitch4;
    private JPanel pnlSwitch5;
    private JPanel pnlCalculate;
    private JPanel pnlReset;
    private JPanel pnlFooter;
    private JPanel pnlExit;
    
//BUTTON

 //initialize button 
    //button warna
    //pita 1
    private JButton btnAbout;
    private JButton btnHelp;
    private JButton btnPita1Black,btnPita1Brown,btnPita1Red,btnPita1Orange,btnPita1Yellow,btnPita1Green,
    				btnPita1Blue, btnPita1Purple,btnPita1Grey, btnPita1White;
    private JButton btnPita2Black,btnPita2Brown,btnPita2Red,btnPita2Orange,btnPita2Yellow,btnPita2Green,
					btnPita2Blue, btnPita2Purple,btnPita2Grey, btnPita2White;
    private JButton btnPita3Black,btnPita3Brown,btnPita3Red,btnPita3Orange,btnPita3Yellow,btnPita3Green,
					btnPita3Blue, btnPita3Purple,btnPita3Grey, btnPita3White, btnPita3Gold, btnPita3Silver;
    private JButton btnPita4Black,btnPita4Brown,btnPita4Red,btnPita4Orange,btnPita4Yellow,btnPita4Green,
					btnPita4Blue, btnPita4Purple,btnPita4Grey, btnPita4White, btnPita4Gold, btnPita4Silver;
    //button Resistor 4 pita; 
    private JButton btnPita1,
    				btnPita2,
    				btnPita3,
    				btnPita4;
    private JButton btnSwitch4;
    private JButton btnSwitch5;	
    private JButton btnCalculate;
    private JButton btnReset;
    private JButton btnExit;
    
//LABEL
	private JLabel lblTitle;
	private JLabel lblHeader;
	private JLabel lblLeft;
	private JLabel lblContent;
	private JLabel lblResistor; //lbl resistor
	private JLabel lblMin;
	private JLabel lblMax;
	private JLabel lblOhmin;
	private JLabel lblOhmax;
	private JLabel lblFunction;
	private JLabel lblCalculate;
	private JLabel lblFooter;
	private JLabel lblWarnaPita1, lblWarnaPita2, lblWarnaPita3, lblWarnaPita4; 
	
//TEXTFIELD
	private JTextField txtResultRMin;
	private JTextField txtResultRMax;
	
//FONT
	private Font font;
	 	
 	private ImageIcon image;
   
    public ResistorGUI_4Pita() {
        initComponent();
        this.setLocationRelativeTo(this);
       
    }

    public void initComponent() {
    	//PANEL
        pnlBase = new JPanel();
        pnlHeader = new JPanel();
        pnlAbout = new JPanel();
        pnlHelp = new JPanel();
        pnlLine1 = new JPanel();
        pnlLine2 = new JPanel();
        pnlLine3 = new JPanel();
        pnlLine4 = new JPanel();
        pnlLeft = new JPanel();
        pnlPallete = new JPanel();
        pnlContent = new JPanel();
        pnlResultRMin = new JPanel();
        pnlResultRMax = new JPanel();
        pnlResistor = new JPanel();
        pnlPita1 = new JPanel();
        pnlPita2 = new JPanel();
        pnlPita3 = new JPanel();
        pnlPita4 = new JPanel();
        pnlFunction = new JPanel();
        pnlSwitch4 = new JPanel();
        pnlSwitch5 = new JPanel();
        pnlCalculate = new JPanel();
        pnlReset = new JPanel();
        pnlFooter = new JPanel();
        pnlExit = new JPanel();
        
        //BUTTON
        btnAbout = new JButton();
        btnHelp = new JButton();
    	btnPita1= new JButton();
        btnPita2 = new JButton();
        btnPita3 = new JButton();
        btnPita4 = new JButton();
        btnSwitch4 = new JButton();
        btnSwitch5 = new JButton();
        btnReset = new JButton();
        btnCalculate = new JButton();
        btnExit = new JButton();
        
        //LABEL 
 		lblTitle = new JLabel();
	 	lblHeader = new JLabel();
		lblLeft = new JLabel();
		lblContent = new JLabel();
		lblResistor = new JLabel(); //label resistor
		lblMin = new JLabel();
		lblMax = new JLabel();
		lblOhmin = new JLabel();
		lblOhmax = new JLabel();
		lblFunction = new JLabel();	
		lblCalculate = new JLabel();
		lblFooter = new JLabel();
		
		txtResultRMin = new JTextField();
		txtResultRMax = new JTextField();
		
		font = new Font("Calibri", 1, 20);
		
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.setTitle("Calculator Resistor");
        this.getContentPane().setLayout(null);
        this.setResizable(false);
        this.setSize(805,581);
        this.add(pnlBase);
        
//BASIC PANEL
        pnlBase.setLayout(null);
        pnlBase.setBackground(new Color(243, 238, 226));
        pnlBase.setOpaque(true);
        pnlBase.setBounds(0,0, 800, 630);
        
        lblHeader.setOpaque(false);
        pnlHeader.add(lblHeader);
        lblHeader.setBounds(0, 0, 730, 75);

        pnlHeader.setLayout(null);
        pnlHeader.setBackground(new Color(241,236,224));
        pnlHeader.setOpaque(true);
        pnlBase.add(pnlHeader);
        pnlHeader.setBounds(0, 0, 800, 75);
        
        //HELP
        pnlHelp.setLayout(null);
        pnlHelp.setBackground(new Color(0, 0, 0));
        pnlHelp.setOpaque(true);
        pnlHeader.add(pnlHelp);
        pnlHelp.setBounds(765, 5, 30, 30);
        
        //btnHelp.setLayout(null);
        btnHelp.setToolTipText("Help");
        btnHelp.setBorderPainted(true);
        btnHelp.setBorder(null);
        btnHelp.setEnabled(true);
        btnHelp.setContentAreaFilled(true);
        btnHelp.setBackground(new Color(0, 0, 0));
        btnHelp.setOpaque(true);
        pnlHelp.add(btnHelp);
        btnHelp.setBounds(0, 0, 30, 30); 
        	
        	
        //ABOUT
        pnlAbout.setLayout(null);
        pnlAbout.setBackground(new Color(0, 0, 0));
        pnlAbout.setOpaque(true);
        pnlHeader.add(pnlAbout);
        pnlAbout.setBounds(730, 5, 30, 30);
        
        //btnAbout.setLayout(null);
        btnAbout.setToolTipText("About");
        btnAbout.setBorderPainted(true);
        btnAbout.setBorder(null);
        btnAbout.setEnabled(true);
        btnAbout.setContentAreaFilled(true);
        btnAbout.setBackground(new Color(0, 0, 0));
        btnAbout.setOpaque(true);
        pnlAbout.add(btnAbout);
        btnAbout.setBounds(0, 0, 30, 30); 
                
      	pnlLine1.setLayout(null);
        pnlLine1.setBackground(new Color(245, 134, 52));
        pnlLine1.setOpaque(true);
        pnlHeader.add(pnlLine1);
        pnlLine1.setBounds(20, 65, 155, 10);
        
        pnlLine2.setLayout(null);
        pnlLine2.setBackground(new Color(82, 113, 141)); 
        pnlLine2.setOpaque(true);
        pnlHeader.add(pnlLine2);
        pnlLine2.setBounds(220, 65, 155, 10);
        
        pnlLine3.setLayout(null);
        pnlLine3.setBackground(new Color(245, 134, 52)); 
        pnlLine3.setOpaque(true);
        pnlHeader.add(pnlLine3);
        pnlLine3.setBounds(420, 65, 155, 10);
        
        pnlLine4.setLayout(null);
        pnlLine4.setBackground(new Color(82, 113, 141)); 
        pnlLine4.setOpaque(true);
        pnlHeader.add(pnlLine4);
        pnlLine4.setBounds(620, 65, 155, 10);
        
//PANEL LEFT
        pnlLeft.setLayout(null);
        pnlLeft.setBackground(new Color(241, 235, 223)); 
        pnlLeft.setOpaque(true);
        pnlBase.add(pnlLeft);
        pnlLeft.setBounds(0,75,200,475);
        
        pnlPallete.setLayout(null);
        pnlPallete.setBackground(new Color(82, 113, 141)); 
        pnlPallete.setOpaque(true);
        pnlLeft.add(pnlPallete);
        pnlPallete.setBounds(10,10,180,460);
        
    
	//COLOR
        //warna pita 1
        lblWarnaPita1 =new JLabel();
		btnPita1Black = new JButton();
		btnPita1Brown = new JButton();
		btnPita1Red = new JButton();
		btnPita1Orange = new JButton();
		btnPita1Yellow = new JButton();
		btnPita1Green = new JButton();
		btnPita1Blue = new JButton();
		btnPita1Purple = new JButton();
		btnPita1Grey = new JButton();
		btnPita1White = new JButton();
		
		//warna pita 2
		lblWarnaPita2 =new JLabel();
		btnPita2Black = new JButton();
		btnPita2Brown = new JButton();
		btnPita2Red = new JButton();
		btnPita2Orange = new JButton();
		btnPita2Yellow = new JButton();
		btnPita2Green = new JButton();
		btnPita2Blue = new JButton();
		btnPita2Purple = new JButton();
		btnPita2Grey = new JButton();
		btnPita2White = new JButton();
	
		//warna pita 3
		lblWarnaPita3 =new JLabel();
		btnPita3Black = new JButton();
		btnPita3Brown = new JButton();
		btnPita3Red = new JButton();
		btnPita3Orange = new JButton();
		btnPita3Yellow = new JButton();
		btnPita3Green = new JButton();
		btnPita3Blue = new JButton();
		btnPita3Purple = new JButton();
		btnPita3Grey = new JButton();
		btnPita3White = new JButton();
		btnPita3Gold = new JButton();
		btnPita3Silver = new JButton();
		
        //warna pita 4
		lblWarnaPita4 =new JLabel();
		btnPita4Black = new JButton();
		btnPita4Brown = new JButton();
		btnPita4Red = new JButton();
		btnPita4Orange = new JButton();
		btnPita4Yellow = new JButton();
		btnPita4Green = new JButton();
		btnPita4Blue = new JButton();
		btnPita4Purple = new JButton();
		btnPita4Grey = new JButton();
		btnPita4White = new JButton();
		btnPita4Gold = new JButton();
		btnPita4Silver = new JButton();
		/////////////////////////
        
        
		//Btn warna pita1
	        lblWarnaPita1.setLayout(null);
	        lblWarnaPita1.setForeground(Color.white);
	        lblWarnaPita1.setText("Tape 1 Colors");
	        pnlPallete.add(lblWarnaPita1);
	        lblWarnaPita1.setBounds(45,10,180,28);
	        
	        btnPita1Black.setLayout(null);
	        btnPita1Black.setBackground(new Color(0,0,0));
	        pnlPallete.add(btnPita1Black);
	        btnPita1Black.setBounds(20,45,20,20);
	        
	        btnPita1Brown.setLayout(null);
	        btnPita1Brown.setBackground(new Color(96, 57, 19));
	        pnlPallete.add(btnPita1Brown);
	        btnPita1Brown.setBounds(44,45,20,20);
	        
	        btnPita1Red.setLayout(null);
	        btnPita1Red.setBackground(new Color(158, 11, 15));
	        pnlPallete.add(btnPita1Red);
	        btnPita1Red.setBounds(68,45,20,20);
	        
	        btnPita1Orange.setLayout(null);
	        btnPita1Orange.setBackground(new Color(242, 101, 34));
	        pnlPallete.add(btnPita1Orange);
	        btnPita1Orange.setBounds(92,45,20,20);
	        
	        btnPita1Yellow.setLayout(null);
	        btnPita1Yellow.setBackground(new Color(255, 242, 0));
	        pnlPallete.add(btnPita1Yellow);
	        btnPita1Yellow.setBounds(116, 45, 20, 20);
	        
	        btnPita1Green.setLayout(null);
	        btnPita1Green.setBackground(new Color(0, 114, 54));
	        pnlPallete.add(btnPita1Green);
	        btnPita1Green.setBounds(140, 45, 20, 20);
	        
	        btnPita1Blue.setLayout(null);
	        btnPita1Blue.setBackground(new Color(0, 174, 239));
	        pnlPallete.add(btnPita1Blue);
	        btnPita1Blue.setBounds(20, 70, 20, 20);
	        
	        btnPita1Purple.setLayout(null);
	        btnPita1Purple.setBackground(new Color(146, 39, 143));
	        pnlPallete.add(btnPita1Purple);
	        btnPita1Purple.setBounds(44, 70, 20, 20);
	        
	        btnPita1Grey.setLayout(null);
	        btnPita1Grey.setBackground(new Color(125, 125, 125));
	        pnlPallete.add(btnPita1Grey);
	        btnPita1Grey.setBounds(68, 70, 20, 20);
	        
	        btnPita1White.setLayout(null);
	        btnPita1White.setBackground(new Color(255,255,255));
	        pnlPallete.add(btnPita1White);
	        btnPita1White.setBounds(92, 70, 20, 20);
	        
	       
      //Btn warna Pita2
	     	lblWarnaPita2.setLayout(null);
	        lblWarnaPita2.setForeground(Color.white);
	        lblWarnaPita2.setText("Tape 2 Colors");
	        pnlPallete.add(lblWarnaPita2);
	        lblWarnaPita2.setBounds(45,98,180,28);
	        
	        btnPita2Black.setLayout(null);
	        btnPita2Black.setBackground(new Color(0,0,0));
	        pnlPallete.add(btnPita2Black);
	        btnPita2Black.setBounds(20,133,20,20);
	        
	        btnPita2Brown.setLayout(null);
	        btnPita2Brown.setBackground(new Color(96, 57, 19));
	        pnlPallete.add(btnPita2Brown);
	        btnPita2Brown.setBounds(44,133,20,20);
	        
	        btnPita2Red.setLayout(null);
	        btnPita2Red.setBackground(new Color(158, 11, 15));
	        pnlPallete.add(btnPita2Red);
	        btnPita2Red.setBounds(68,133,20,20);
	        
	        btnPita2Orange.setLayout(null);
	        btnPita2Orange.setBackground(new Color(242, 101, 34));
	        pnlPallete.add(btnPita2Orange);
	        btnPita2Orange.setBounds(92,133,20,20);
	        
	        btnPita2Yellow.setLayout(null);
	        btnPita2Yellow.setBackground(new Color(255, 242, 0));
	        pnlPallete.add(btnPita2Yellow);
	        btnPita2Yellow.setBounds(116, 133, 20, 20);
	        
	        btnPita2Green.setLayout(null);
	        btnPita2Green.setBackground(new Color(0, 114, 54));
	        pnlPallete.add(btnPita2Green);
	        btnPita2Green.setBounds(140, 133, 20, 20);
	        
	        btnPita2Blue.setLayout(null);
	        btnPita2Blue.setBackground(new Color(0, 174, 239));
	        pnlPallete.add(btnPita2Blue);
	        btnPita2Blue.setBounds(20, 158, 20, 20);
	        
	        btnPita2Purple.setLayout(null);
	        btnPita2Purple.setBackground(new Color(146, 39, 143));
	        pnlPallete.add(btnPita2Purple);
	        btnPita2Purple.setBounds(44, 158, 20, 20);
	        
	        btnPita2Grey.setLayout(null);
	        btnPita2Grey.setBackground(new Color(125, 125, 125));
	        pnlPallete.add(btnPita2Grey);
	        btnPita2Grey.setBounds(68, 158, 20, 20);
	        
	        btnPita2White.setLayout(null);
	        btnPita2White.setBackground(new Color(255,255,255));
	        pnlPallete.add(btnPita2White);
	        btnPita2White.setBounds(92, 158, 20, 20);
	        
        
      //Btn warna Pita3
	      	lblWarnaPita3.setLayout(null);
	        lblWarnaPita3.setForeground(Color.white);
	        lblWarnaPita3.setText("Tape 3 Colors");
	        pnlPallete.add(lblWarnaPita3);
	        lblWarnaPita3.setBounds(45,186,180,28);
	        
	        btnPita3Black.setLayout(null);
	        btnPita3Black.setBackground(new Color(0,0,0));
	        pnlPallete.add(btnPita3Black);
	        btnPita3Black.setBounds(20, 221, 20, 20);
	        
	        btnPita3Brown.setLayout(null);
	        btnPita3Brown.setBackground(new Color(96, 57, 19));
	        pnlPallete.add(btnPita3Brown);
	        btnPita3Brown.setBounds(44, 221, 20, 20);
	        
	        btnPita3Red.setLayout(null);
	        btnPita3Red.setBackground(new Color(158, 11, 15));
	        pnlPallete.add(btnPita3Red);
	        btnPita3Red.setBounds(68, 221, 20, 20);
	        
	        btnPita3Orange.setLayout(null);
	        btnPita3Orange.setBackground(new Color(242, 101, 34));
	        pnlPallete.add(btnPita3Orange);
	        btnPita3Orange.setBounds(92, 221, 20, 20);
	        
	        btnPita3Yellow.setLayout(null);
	        btnPita3Yellow.setBackground(new Color(255, 242, 0));
	        pnlPallete.add(btnPita3Yellow);
	        btnPita3Yellow.setBounds(116, 221, 20, 20);
	        
	        btnPita3Green.setLayout(null);
	        btnPita3Green.setBackground(new Color(0, 114, 54));
	        pnlPallete.add(btnPita3Green);
	        btnPita3Green.setBounds(140, 221, 20, 20);
	        
	        btnPita3Blue.setLayout(null);
	        btnPita3Blue.setBackground(new Color(0, 174, 239));
	        pnlPallete.add(btnPita3Blue);
	        btnPita3Blue.setBounds(20, 246, 20, 20);
	        
	        btnPita3Purple.setLayout(null);
	        btnPita3Purple.setBackground(new Color(146, 39, 143));
	        pnlPallete.add(btnPita3Purple);
	        btnPita3Purple.setBounds(44, 246, 20, 20);
	        
	        btnPita3Grey.setLayout(null);
	        btnPita3Grey.setBackground(new Color(125, 125, 125));
	        pnlPallete.add(btnPita3Grey);
	        btnPita3Grey.setBounds(68, 246, 20, 20);
	        
	        btnPita3White.setLayout(null);
	        btnPita3White.setBackground(new Color(255,255,255));
	        pnlPallete.add(btnPita3White);
	        btnPita3White.setBounds(92, 246, 20, 20);
	        
	        btnPita3Gold.setLayout(null);
	        btnPita3Gold.setBackground(new Color(228, 184, 62));
	        pnlPallete.add(btnPita3Gold);
	        btnPita3Gold.setBounds(116, 246, 20, 20);
        
        	btnPita3Silver.setLayout(null);
	        btnPita3Silver.setBackground(new Color(194, 194, 194));
	        pnlPallete.add(btnPita3Silver);
	        btnPita3Silver.setBounds(140, 246, 20, 20);
	        
      //Btn warna Pita4
	    	lblWarnaPita4.setLayout(null);
	        lblWarnaPita4.setForeground(Color.white);
	        lblWarnaPita4.setText("Tape 4 Colors");
	        pnlPallete.add(lblWarnaPita4);
	        lblWarnaPita4.setBounds(45,274,180,28);
	        
	        btnPita4Black.setLayout(null);
	        btnPita4Black.setBackground(new Color(0,0,0));
	        pnlPallete.add(btnPita4Black);
	        btnPita4Black.setBounds(20, 309, 20, 20);
	        
	        btnPita4Brown.setLayout(null);
	        btnPita4Brown.setBackground(new Color(96, 57, 19));
	        pnlPallete.add(btnPita4Brown);
	        btnPita4Brown.setBounds(44,309,20,20);
	        
	        btnPita4Red.setLayout(null);
	        btnPita4Red.setBackground(new Color(158, 11, 15));
	        pnlPallete.add(btnPita4Red);
	        btnPita4Red.setBounds(68,309,20,20);
	        
	        btnPita4Orange.setLayout(null);
	        btnPita4Orange.setBackground(new Color(242, 101, 34));
	        pnlPallete.add(btnPita4Orange);
	        btnPita4Orange.setBounds(92,309,20,20);
	        
	        btnPita4Yellow.setLayout(null);
	        btnPita4Yellow.setBackground(new Color(255, 242, 0));
	        pnlPallete.add(btnPita4Yellow);
	        btnPita4Yellow.setBounds(116, 309, 20, 20);
	        
	        btnPita4Green.setLayout(null);
	        btnPita4Green.setBackground(new Color(0, 114, 54));
	        pnlPallete.add(btnPita4Green);
	        btnPita4Green.setBounds(140, 309, 20, 20);
	        
	        btnPita4Blue.setLayout(null);
	        btnPita4Blue.setBackground(new Color(0, 174, 239));
	        pnlPallete.add(btnPita4Blue);
	        btnPita4Blue.setBounds(20, 334, 20, 20);
	        
	        btnPita4Purple.setLayout(null);
	        btnPita4Purple.setBackground(new Color(146, 39, 143));
	        pnlPallete.add(btnPita4Purple);
	        btnPita4Purple.setBounds(44, 334, 20, 20);
	        
	        btnPita4Grey.setLayout(null);
	        btnPita4Grey.setBackground(new Color(125, 125, 125));
	        pnlPallete.add(btnPita4Grey);
	        btnPita4Grey.setBounds(68, 334, 20, 20);
	        
	        btnPita4White.setLayout(null);
	        btnPita4White.setBackground(new Color(255,255,255));
	        pnlPallete.add(btnPita4White);
	        btnPita4White.setBounds(92, 334, 20, 20);
	        
	        btnPita4Gold.setLayout(null);
	        btnPita4Gold.setBackground(new Color(228, 184, 62));
	        pnlPallete.add(btnPita4Gold);
	        btnPita4Gold.setBounds(116, 334, 20, 20);
        
        	btnPita4Silver.setLayout(null);
	        btnPita4Silver.setBackground(new Color(194, 194, 194));
	        pnlPallete.add(btnPita4Silver);
	        btnPita4Silver.setBounds(140, 334, 20, 20);
	        
		
//PANEL CONTENT
        pnlContent.setLayout(null);
        pnlContent.setBackground(new Color(241, 235, 223));
        pnlContent.setOpaque(true);
        pnlBase.add(pnlContent);
        pnlContent.setBounds(200,75, 600, 410);
        
        //RESISTOR
        pnlResistor.setLayout(null);
        pnlResistor.setBackground(new Color(241, 235, 223));
        pnlResistor.setOpaque(true);
        pnlContent.add(pnlResistor);
        pnlResistor.setBounds(60,95, 470, 160);
        
        lblResistor.setOpaque(false); //lbl resistor
        pnlResistor.add(lblResistor);
        lblResistor.setBounds(0, 0, 470, 160);
        
      
     //BUTTON PITA 
        
      		btnPita1.setLayout(null);
      		btnPita1.setBackground(new Color(229, 213, 190)); 
      		btnPita1.setBorder(null);
      		lblResistor.add(btnPita1);
      		btnPita1.setBounds(45,12,30,135);
      		
      		btnPita2.setLayout(null);
      		btnPita2.setBackground(new Color(229, 213, 190));
      		btnPita2.setBorder(null);
      		lblResistor.add(btnPita2);
      		btnPita2.setBounds(154,30,30,100);
      		
      		btnPita3.setLayout(null);
      		btnPita3.setBackground(new Color(229, 213, 190)); 
      		btnPita3.setBorder(null);
      		lblResistor.add(btnPita3);
      		btnPita3.setBounds(219,30,30,100);
      		
      		btnPita4.setLayout(null);
      		btnPita4.setBackground(new Color(229, 213, 190));
      		btnPita4.setBorder(null);
      		lblResistor.add(btnPita4);
      		btnPita4.setBounds(284,30,30,100);
      		
        
        ////////////////////////////////////////////// 
        
//PANEL RMIN & RMAX
		//MIN
		pnlResultRMin.setLayout(null);
        pnlResultRMin.setBackground(new Color(82, 113, 141));
        pnlResultRMin.setOpaque(true);
        pnlResultRMin.setVisible(true);
        pnlContent.add(pnlResultRMin);
        pnlResultRMin.setBounds(0, 340, 290, 60);
        
        lblMin.setText("RMin =");
        lblMin.setFont(new Font("Calibri", 1, 20));
        lblMin.setForeground(Color.WHITE);
        lblMin.setHorizontalAlignment(JLabel.LEFT);
        lblMin.setVerticalAlignment(JLabel.CENTER);
        lblMin.setOpaque(false);
        pnlResultRMin.add(lblMin);
        lblMin.setBounds(10, 20, 70, 20);
        
        txtResultRMin.setHorizontalAlignment(JTextField.CENTER);
        txtResultRMin.setFont(font);
        txtResultRMin.setText("0.0");
        txtResultRMin.setOpaque(true);
        txtResultRMin.setBorder(null);
        pnlResultRMin.add(txtResultRMin);
        txtResultRMin.setBounds(90, 15, 130, 30);
        
        lblOhmin.setText("ohm");
        lblOhmin.setFont(new Font("Calibri", 1, 20));
        lblOhmin.setForeground(Color.WHITE);
        lblOhmin.setHorizontalAlignment(JLabel.CENTER);
        lblOhmin.setVerticalAlignment(JLabel.CENTER);
        lblOhmin.setOpaque(false);
        pnlResultRMin.add(lblOhmin);
        lblOhmin.setBounds(230, 15, 50, 30);
        
        //MAX
        
        pnlResultRMax.setLayout(null);
        pnlResultRMax.setBackground(new Color(82, 113, 141));
        pnlResultRMax.setOpaque(true);
        pnlResultRMax.setVisible(true);
        pnlContent.add(pnlResultRMax);
        pnlResultRMax.setBounds(300, 340, 290, 60);

		lblMax.setText("RMax =");
        lblMax.setFont(new Font("Calibri", 1, 20));
        lblMax.setForeground(Color.WHITE);
        lblMax.setHorizontalAlignment(JLabel.LEFT);
        lblMax.setVerticalAlignment(JLabel.CENTER);
        lblMax.setOpaque(false);
        pnlResultRMax.add(lblMax);
        lblMax.setBounds(10, 20, 70, 20);
        
        txtResultRMax.setHorizontalAlignment(JTextField.CENTER);
        txtResultRMax.setFont(font);
        txtResultRMax.setText("0.0");
        txtResultRMax.setOpaque(true);
        txtResultRMax.setBorder(null);
        pnlResultRMax.add(txtResultRMax);
        txtResultRMax.setBounds(90, 15, 130, 30);
        
        lblOhmax.setText("ohm");
        lblOhmax.setFont(new Font("Calibri", 1, 20));
        lblOhmax.setForeground(Color.WHITE);
        lblOhmax.setHorizontalAlignment(JLabel.CENTER);
        lblOhmax.setVerticalAlignment(JLabel.CENTER);
        lblOhmax.setOpaque(false);
        pnlResultRMax.add(lblOhmax);
        lblOhmax.setBounds(230, 15, 50, 30);
		
//PANEL FUNCTION
		pnlFunction.setLayout(null);
        pnlFunction.setBackground(new Color(82, 113, 141)); 
        pnlFunction.setOpaque(true);
        pnlBase.add(pnlFunction);
        pnlFunction.setBounds(200,485,590,60);
        
	//PANEL dan BUTTON FUNCTION AREA
	
		//SWITCH PITA 4
        pnlSwitch4.setLayout(null);
        pnlSwitch4.setBackground(new Color(158, 11, 15)); 
        pnlSwitch4.setOpaque(true);
        pnlFunction.add(pnlSwitch4);
        pnlSwitch4.setBounds(100, 10, 90, 40);
        
        btnSwitch4.setOpaque(true);
        btnSwitch4.setBorderPainted(true);
        btnSwitch4.setContentAreaFilled(true);
        btnSwitch4.setBorder(null);
        btnSwitch4.setEnabled(true);
        pnlSwitch4.add(btnSwitch4);
        btnSwitch4.setBounds(0, 0, 90, 40);
        
        //SWITCH PITA 5
        pnlSwitch5.setLayout(null);
        pnlSwitch5.setBackground(new Color(158, 11, 15)); 
        pnlSwitch5.setOpaque(true);
        pnlFunction.add(pnlSwitch5);
        pnlSwitch5.setBounds(200, 10, 90, 40);
        
        btnSwitch5.setOpaque(true);
        btnSwitch5.setBorderPainted(true);
        btnSwitch5.setContentAreaFilled(true);
        btnSwitch5.setBorder(null);
        btnSwitch5.setEnabled(true);
        pnlSwitch5.add(btnSwitch5);
        btnSwitch5.setBounds(0, 0, 90, 40);
        
        //CALCULATE
        pnlCalculate.setLayout(null);
        pnlCalculate.setBackground(new Color(245,134,52)); 
        pnlCalculate.setOpaque(true);
        pnlFunction.add(pnlCalculate);
        pnlCalculate.setBounds(300, 10, 90, 40);
        
     	btnCalculate.setOpaque(true);
        btnCalculate.setBorderPainted(true);
        btnCalculate.setContentAreaFilled(true);
        btnCalculate.setBorder(null);
        btnCalculate.setEnabled(true);
        pnlCalculate.add(btnCalculate);
        btnCalculate.setBounds(0, 0, 90, 40);
        
        
        //RESET       
        pnlReset.setLayout(null);
        pnlReset.setBackground(new Color(255,255,255));
        pnlReset.setOpaque(true);
        pnlFunction.add(pnlReset);
        pnlReset.setBounds(400, 10, 90, 40);
        
        btnReset.setOpaque(true);
        btnReset.setBorderPainted(true);
        btnReset.setContentAreaFilled(true);
        btnReset.setBorder(null);
        btnReset.setEnabled(true);
        pnlReset.add(btnReset);
        btnReset.setBounds(0, 0, 90, 40);
        
        
//FOOTER
        pnlFooter.setLayout(null);
        pnlFooter.setBackground(new Color(241, 235, 223)); 
        pnlFooter.setOpaque(true);
        pnlBase.add(pnlFooter);
        pnlFooter.setBounds(0,545,800,70);
 
	//PANEL dan BUTTON EXIT       
        pnlExit.setLayout(null);
        pnlExit.setBackground(new Color(245,134,52)); 
        pnlExit.setOpaque(true);
        pnlFooter.add(pnlExit);
        pnlExit.setBounds(680, 10, 110, 40);

	 	btnExit.setOpaque(false);
        btnExit.setBorderPainted(true);
        btnExit.setContentAreaFilled(true);
        btnExit.setBorder(null);
        btnExit.setEnabled(true);
        pnlExit.add(btnExit);
        btnExit.setBounds(0, 0, 110, 40);
        
        eventControl();
        imageControl();
       
    }
   
    
    public void eventControl() { //action listener pada buton warna
    	
    	this.addWindowListener(winAdapter);
    	//warna untuk pita1
    	btnPita1Black.addActionListener(ActColor);
    	btnPita1Brown.addActionListener(ActColor);
    	btnPita1Blue.addActionListener(ActColor);
    	btnPita1Green.addActionListener(ActColor);
    	btnPita1Grey.addActionListener(ActColor);
    	btnPita1Orange.addActionListener(ActColor);
    	btnPita1Purple.addActionListener(ActColor);
    	btnPita1Red.addActionListener(ActColor);
    	btnPita1White.addActionListener(ActColor);
    	btnPita1Yellow.addActionListener(ActColor);
    	btnSwitch4.addActionListener(ActColor); //button untuk link ke pita 4
    	btnSwitch5.addActionListener(ActColor); //button untuk link ke pita 5
    	btnSwitch4.addMouseListener(mosAdapter); //percobaan 1
    	btnSwitch5.addMouseListener(mosAdapter);
    	btnCalculate.addMouseListener(mosAdapter);
    	btnHelp.addActionListener(ActColor);
    	btnReset.addMouseListener(mosAdapter);
    	btnExit.addMouseListener(mosAdapter);
    	
    	
    	//warna untuk pita2
    	btnPita2Black.addActionListener(ActColor);
    	btnPita2Brown.addActionListener(ActColor);
    	btnPita2Blue.addActionListener(ActColor);
    	btnPita2Green.addActionListener(ActColor);
    	btnPita2Grey.addActionListener(ActColor);
    	btnPita2Orange.addActionListener(ActColor);
    	btnPita2Purple.addActionListener(ActColor);
    	btnPita2Red.addActionListener(ActColor);
    	btnPita2White.addActionListener(ActColor);
    	btnPita2Yellow.addActionListener(ActColor);
    	
    	//warna untuk pita3
    	btnPita3Black.addActionListener(ActColor);
    	btnPita3Brown.addActionListener(ActColor);
    	btnPita3Blue.addActionListener(ActColor);
    	btnPita3Green.addActionListener(ActColor);
    	btnPita3Grey.addActionListener(ActColor);
    	btnPita3Orange.addActionListener(ActColor);
    	btnPita3Purple.addActionListener(ActColor);
    	btnPita3Red.addActionListener(ActColor);
    	btnPita3White.addActionListener(ActColor);
    	btnPita3Yellow.addActionListener(ActColor);
    	btnPita3Gold.addActionListener(ActColor);
    	btnPita3Silver.addActionListener(ActColor);
    	
    	//warna untuk pita4
    	btnPita4Black.addActionListener(ActColor);
    	btnPita4Brown.addActionListener(ActColor);
    	btnPita4Blue.addActionListener(ActColor);
    	btnPita4Green.addActionListener(ActColor);
    	btnPita4Grey.addActionListener(ActColor);
    	btnPita4Orange.addActionListener(ActColor);
    	btnPita4Purple.addActionListener(ActColor);
    	btnPita4Red.addActionListener(ActColor);
    	btnPita4White.addActionListener(ActColor);
    	btnPita4Yellow.addActionListener(ActColor);
 		btnPita4Gold.addActionListener(ActColor);
    	btnPita4Silver.addActionListener(ActColor);  
    	
    	btnCalculate.addActionListener(ActColor);
    	btnAbout.addActionListener(ActColor);
    	btnAbout.addMouseListener(mosAdapter);
    	
    	btnReset.addActionListener(ActColor); //action listener untuk button reset   
    	//btnReset.addActionListener(evt); //action listener untuk button reset  	
    	}

public void imageControl(){
	image = new ImageIcon(getClass().getResource("/Images/resistor.png"));
	lblResistor.setIcon(image);
	image = new ImageIcon(getClass().getResource("/Images/cal1.png"));
	btnCalculate.setIcon(image);
	image = new ImageIcon(getClass().getResource("/Images/res1.png"));
	btnReset.setIcon(image);
	image = new ImageIcon(getClass().getResource("/Images/4pita1.png"));
	btnSwitch4.setIcon(image);
	image = new ImageIcon(getClass().getResource("/Images/5pita1.png"));
	btnSwitch5.setIcon(image);
	image = new ImageIcon(getClass().getResource("/Images/help1.png"));
	btnHelp.setIcon(image);
	image = new ImageIcon(getClass().getResource("/Images/exit1.png"));
	btnExit.setIcon(image);
	image = new ImageIcon(getClass().getResource("/Images/about1.png"));
	btnAbout.setIcon(image);
	image = new ImageIcon(getClass().getResource("/Images/header.png")); //image untuk header
	lblHeader.setIcon(image);
	} 
    
//action listener untuk panel pita
    private ActionListener ActColor = new ActionListener() { //action event untuk button pita
    	 public void actionPerformed(ActionEvent ActC) { //action peformed untuk button pita
    		
    		if(ActC.getSource().equals(btnPita1Black)){ ///fungsi untuk pita 1//
    			  btnPita1.setBackground(new Color(0,0,0)); 
    			  p1 = 0;
    		} else if(ActC.getSource().equals(btnPita1Brown)){
	  			  btnPita1.setBackground(new Color(96, 57, 19));
	  			  p1 = 1; 
    		} else if(ActC.getSource().equals(btnPita1Red)){
    			  btnPita1.setBackground(new Color(158, 11, 15));
    			  p1 = 2;
    		} else if(ActC.getSource().equals(btnPita1Orange)){
	  			  btnPita1.setBackground(new Color(242, 101, 34));
	  			  p1 = 3;
    		} else if(ActC.getSource().equals(btnPita1Yellow)){
    			  btnPita1.setBackground(new Color(255, 242, 0));
    			  p1 = 4;
        	} else if(ActC.getSource().equals(btnPita1Green)){
	  			  btnPita1.setBackground(new Color(0, 114, 54)); 
	  			  p1 = 5;
        	} else if(ActC.getSource().equals(btnPita1Blue)){
    			  btnPita1.setBackground(new Color(0, 174, 239)); 
      			  p1 = 6;
            } else if(ActC.getSource().equals(btnPita1Purple)){
	  			  btnPita1.setBackground(new Color(146, 39, 143)); 
	  			  p1 = 7;
            } else if(ActC.getSource().equals(btnPita1Grey)){
	  			  btnPita1.setBackground(new Color(125, 125, 125)); 
	  			  p1 = 8;
            } else if(ActC.getSource().equals(btnPita1White)){
	  			  btnPita1.setBackground(new Color(255,255,255)); 
	  			  p1 = 9;
            } else if(ActC.getSource().equals(btnPita2Black)){ ///fungsi untuk pita 2//
	  			  btnPita2.setBackground(new Color(0,0,0)); 
	  			  p2 = 0;
	  		} else if(ActC.getSource().equals(btnPita2Brown)){
	  			btnPita2.setBackground(new Color(96, 57, 19));
		  		  p2 = 1; 
	  		} else if(ActC.getSource().equals(btnPita2Red)){
	  			  btnPita2.setBackground(new Color(158, 11, 15));
	  			  p2 = 2;
	  		} else if(ActC.getSource().equals(btnPita2Orange)){
		  		  btnPita2.setBackground(new Color(242, 101, 34));
		  		  p2 = 3;
	  		} else if(ActC.getSource().equals(btnPita2Yellow)){
	  			  btnPita2.setBackground(new Color(255, 242, 0));
	  			  p2 = 4;
	      	} else if(ActC.getSource().equals(btnPita2Green)){
		  		  btnPita2.setBackground(new Color(0, 114, 54)); 
		  		  p2 = 5;
	      	} else if(ActC.getSource().equals(btnPita2Blue)){
	  			  btnPita2.setBackground(new Color(0, 174, 239)); 
    			  p2 = 6;
	        } else if(ActC.getSource().equals(btnPita2Purple)){
		  		  btnPita2.setBackground(new Color(146,39,143)); 
		  		  p2 = 7;
	        } else if(ActC.getSource().equals(btnPita2Grey)){
		  		  btnPita2.setBackground(new Color(125, 125, 125)); 
		  		  p2 = 8;
	        } else if(ActC.getSource().equals(btnPita2White)){
		  		  btnPita2.setBackground(new Color(255,255,255)); 
		  		  p2 = 9;
	        } else if(ActC.getSource().equals(btnPita3Black)){ ///fungsi untuk pita 3//
	  			  btnPita3.setBackground(new Color(0,0,0)); 
	  			  p3 = 1;
	  		} else if(ActC.getSource().equals(btnPita3Brown)){
	  			btnPita3.setBackground(new Color(96,57,18));
		  		  p3 = 10; 
	  		} else if(ActC.getSource().equals(btnPita3Red)){
	  			  btnPita3.setBackground(new Color(158, 11, 15));
	  			  p3 = 100;
	  		} else if(ActC.getSource().equals(btnPita3Orange)){
		  		  btnPita3.setBackground(new Color(242, 101, 34));
		  		  p3 = 1000;
	  		} else if(ActC.getSource().equals(btnPita3Yellow)){
	  			  btnPita3.setBackground(new Color(255, 242, 0));
	  			  p3 = 10000;
	      	} else if(ActC.getSource().equals(btnPita3Green)){
		  		  btnPita3.setBackground(new Color(0, 114, 54)); 
		  		  p3 = 100000;
	      	} else if(ActC.getSource().equals(btnPita3Blue)){
	  			  btnPita3.setBackground(new Color(0, 174, 239)); 
	  			  p3 = 1000000;
	        } else if(ActC.getSource().equals(btnPita3Purple)){
		  		  btnPita3.setBackground(new Color(146,39,143)); 
		  		  p3 = 10000000;
	        } else if(ActC.getSource().equals(btnPita3Grey)){
		  		  btnPita3.setBackground(new Color(125, 125, 125)); 
		  		  p3 = 100000000;
	        } else if(ActC.getSource().equals(btnPita3White)){
		  		  btnPita3.setBackground(new Color(255,255,255)); 
		  		  p3 = 1000000000;
		  	} else if(ActC.getSource().equals(btnPita3Gold)){
		  		  btnPita3.setBackground(new Color(228, 184, 62)); 
		  		  p3 = 1;
		  	} else if(ActC.getSource().equals(btnPita3Silver)){
		  		  btnPita3.setBackground(new Color(194, 194, 194)); 
		  		  p3 = 0.1;	  
	        } else if(ActC.getSource().equals(btnPita4Black)){ ///fungsi untuk pita 4//
	  			  btnPita4.setBackground(new Color(0,0,0)); 
	  			  p4 = 0;
	  		} else if(ActC.getSource().equals(btnPita4Brown)){
	  			btnPita4.setBackground(new Color(96,57,18));
		  		  p4 = 0.01; 
	  		} else if(ActC.getSource().equals(btnPita4Red)){
	  			  btnPita4.setBackground(new Color(158, 11, 15));
	  			  p4 = 0.02;
	  		} else if(ActC.getSource().equals(btnPita4Orange)){
		  		  btnPita4.setBackground(new Color(242, 101, 34));
		  		  p4 = 0;
	  		} else if(ActC.getSource().equals(btnPita4Yellow)){
	  			  btnPita4.setBackground(new Color(255, 242, 0));
	  			  p4= 0;
	      	} else if(ActC.getSource().equals(btnPita4Green)){
		  		  btnPita4.setBackground(new Color(0, 114, 54)); 
		  		  p4 =0.005;
	      	} else if(ActC.getSource().equals(btnPita4Blue)){
	  			  btnPita4.setBackground(new Color(0, 174, 239)); 
	  			  p4 = 0.0025;
	        } else if(ActC.getSource().equals(btnPita4Purple)){
		  		  btnPita4.setBackground(new Color(146,39,143)); 
		  		  p4 = 0.001;
	        } else if(ActC.getSource().equals(btnPita4Grey)){
		  		  btnPita4.setBackground(new Color(125, 125, 125)); 
		  		  p4 = 0.0005;
	        } else if(ActC.getSource().equals(btnPita4White)){
		  		  btnPita4.setBackground(new Color(255,255,255)); 
		  		  p4 = 0;
		  	} else if(ActC.getSource().equals(btnPita4Gold)){
		  		  btnPita4.setBackground(new Color(228, 184, 62)); 
		  		  p4 = 0.05;
		  	} else if(ActC.getSource().equals(btnPita4Silver)){
		  		  btnPita4.setBackground(new Color(194, 194, 194)); 
		  		  p4 = 0.1;	  
	        } else if(ActC.getSource().equals(btnCalculate)){
	        	Calculate();
	        } else if(ActC.getSource().equals(btnReset)){  //jika button reset di tekan
	        	p1 = 0; //mengembalikan nilai pita ke default
	        	p2 = 0; //mengembalikan nilai pita ke default
	        	p3 = 0.01; //mengembalikan nilai pita ke default
	        	p4 = 0.2; //mengembalikan nilai pita ke default
	        	txtResultRMax.setText("0.0"); //txtRmax kembali ke 0.0
	        	txtResultRMin.setText("0.0"); //txtRmax kembali ke 0.0
	        	btnPita1.setBackground(new Color(229, 213, 190)); //mengembalikan warna pita ke background semula
	        	btnPita2.setBackground(new Color(229, 213, 190)); //mengembalikan warna pita ke background semula
	        	btnPita3.setBackground(new Color(229, 213, 190)); //mengembalikan warna pita ke background semula
	        	btnPita4.setBackground(new Color(229, 213, 190)); //mengembalikan warna pita ke background semula   	
	        }else if(ActC.getSource().equals(btnSwitch5)){ //fungsi untuk berpindah ke pita 5
	        	ResistorGUI_5Pita me = new ResistorGUI_5Pita();
        		me.setVisible(true);
        		setVisible(false);
	        }else if(ActC.getSource().equals(btnHelp)){ //fungsi untuk help
	        	HelpResistor help = new HelpResistor();
       			help.setVisible(true);
       		}else if(ActC.getSource().equals(btnAbout)){ //fungsi untuk help
	        	About ab = new About();
       			ab.setVisible(true);
	        	
	        }
    			
    	 }
    	
    	 
    };
    
    
    

    
  //  @Override
    public void Calculate() {
    	cResistor_4Pita us = new cResistor_4Pita(0, 0, p1, p2, p3, p4, 0, 0, 0);
    	us.resultpita12();
    	us.resultRMax();
    	us.resultRMin();
		txtResultRMax.setText(String.valueOf(us.resultRMax()));
		txtResultRMin.setText(String.valueOf(us.resultRMin()));
    }
  //close  
  private WindowAdapter winAdapter = new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
            Resistor_Main mEvt = new Resistor_Main();
            mEvt.closingScene();
        }
    };  
    
  private MouseAdapter mosAdapter = new MouseAdapter() {
  	
@Override
        public void mousePressed(MouseEvent evt) {
            if(evt.getSource().equals(btnCalculate)) {
                btnCalculate.setIcon(new ImageIcon(getClass().getResource("/Images/cal3.png")));
            } else if(evt.getSource().equals(btnReset)) {
                btnReset.setIcon(new ImageIcon(getClass().getResource("/Images/res3.png")));            
            } else if(evt.getSource().equals(btnHelp)) {
                btnHelp.setIcon(new ImageIcon(getClass().getResource("/Images/help3.png")));
            } else if(evt.getSource().equals(btnSwitch4)) {
                btnSwitch4.setIcon(new ImageIcon(getClass().getResource("/Images/4pita3.png")));
            } else if(evt.getSource().equals(btnSwitch5)) {
                btnSwitch5.setIcon(new ImageIcon(getClass().getResource("/Images/5pita3.png")));
            } else if(evt.getSource().equals(btnExit)) {
                btnExit.setIcon(new ImageIcon(getClass().getResource("/Images/exit3.png")));
            } else if(evt.getSource().equals(btnAbout)) {
                btnAbout.setIcon(new ImageIcon(getClass().getResource("/Images/about3.png")));
            }
        }

 @Override
        public void mouseReleased(MouseEvent evt) {
            if(evt.getSource().equals(btnHelp)) {
                btnHelp.setIcon(new ImageIcon(getClass().getResource("/Images/help1.png")));
            } else if(evt.getSource().equals(btnSwitch4)) {
                btnSwitch4.setIcon(new ImageIcon(getClass().getResource("/Images/4pita1.png")));
            } else if(evt.getSource().equals(btnSwitch5)) {
                btnSwitch5.setIcon(new ImageIcon(getClass().getResource("/Images/5pita1.png")));
            } else if(evt.getSource().equals(btnCalculate)) {
                btnCalculate.setIcon(new ImageIcon(getClass().getResource("/Images/cal1.png")));
            } else if(evt.getSource().equals(btnReset)) {
                btnReset.setIcon(new ImageIcon(getClass().getResource("/Images/res1.png")));
            } else if(evt.getSource().equals(btnExit)) {
                btnExit.setIcon(new ImageIcon(getClass().getResource("/Images/exit1.png")));
            } else if(evt.getSource().equals(btnAbout)) {
                btnAbout.setIcon(new ImageIcon(getClass().getResource("/Images/about1.png")));
            }
        } 
};
    public static void main(String[] args) {
        ResistorGUI_4Pita me = new ResistorGUI_4Pita();
        me.setVisible(true);
    }
}

//end of code

