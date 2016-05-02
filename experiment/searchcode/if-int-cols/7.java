/**
 * MineSweeper main program in Java
 * Program developed by Lizhao HOU
 * Email : lizhao.hou@supelec.fr
 * 
 * The description of MineButton index : see MineButton.java
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class MineFrame extends JFrame implements ActionListener
{
	/**
	 * Entrance Function
	 */
	public static void main(String args[])
	{
			System.out.println("Here We Start!\nProgram developed by Lizhao HOU\nEmail : lizhao.hou@supelec.fr");
			new MineFrame(10,9,9);
	}
	/**
	 *	Definition of menu components
	 */
	JMenuBar menuBar=null;
	JMenu menuGame=new JMenu("Game");
		JMenuItem startItem=new JMenuItem("Start");
		JCheckBoxMenuItem beginItem=new JCheckBoxMenuItem("Beginner");
		JCheckBoxMenuItem interItem=new JCheckBoxMenuItem("Intermediate");
		JCheckBoxMenuItem advanItem=new JCheckBoxMenuItem("Advanced");
		JMenuItem exitItem=new JMenuItem("Exit");
	JMenu menuAbout=new JMenu("About");
		JMenuItem aboutItem=new JMenuItem("About Mine Sweeper");

	/**
	 *	define layout components
	 */
	private JButton beginBtn;  						 //Beginning Button
	public static MineButton [][]mineBtn;   		 //Mine Button
	private MineButton bttmp;						 //temporary of Mine Button
	
	private JPanel pane1=new JPanel();               //pane1:details in constructor of MineFrame
	private JPanel pane2=new JPanel();			     //pane2:details in constructor of MineFrame
	private JPanel pane3=new JPanel();				 //pane3:details in constructor of MineFrame
	private JLabel lb1,lb2,lb3;                      //labels:details in constructor of MineFrame

	/**
	 *	define value variables
	 */

	Timer t=new Timer(1000,new timeUsed()); //global game timer

	public static int mineNumber,rows,cols;	//for constructor,mine number, grid size=rows*cols
	private boolean bool=true;				//
	private int finishtime;					//game finish time
	private int difficulty=0;               //level of difficulty
	private Point frameP=new Point();       //coordinates of frame
	
	private int tempmineNumber=0;
	public static int tempx,tempy;
	
	/**
	 *	Constructor
	 */
	public MineFrame(){}
	public MineFrame(int mineNumber,int rows,int cols)
	{
		MineFrame.mineNumber=mineNumber;
		MineFrame.rows=rows;
		MineFrame.cols=cols;
		this.tempmineNumber=mineNumber;

		/**
		 * Window configuration
		 */
		setTitle("Mine Sweeper");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);     //auto clear when close
		setBounds((int)frameP.getX(),(int)frameP.getY(),cols*20-4,rows*20+95);     //set the size of the window
		setResizable(false);
		setLocationRelativeTo(null);          //set window in the middle of screen

		/**
		 * Select the difficulty
		 */
		 if(cols==9 & rows==9 & mineNumber==10)difficulty=1;
		 if(cols==16 & rows==16 & mineNumber==40)difficulty=2;
		 if(cols==30 & rows==16 & mineNumber==99)difficulty=3;

		/**
		 * begin BUTTON initialization  (the smile face above the mines)
		 */
		beginBtn=new JButton(new ImageIcon("res/image/beginBtn.jpg"));
		beginBtn.setFocusPainted(false);
		beginBtn.setBorder(new BevelBorder(BevelBorder.RAISED));
		beginBtn.addMouseListener(new beginBtnFace());

		/**
		 * mine buttons initialization
		 */
		mineBtn=new MineButton[rows][cols];

		/**
		 *  call for methods
		 */
		pane2();
		constructMenu();
		constructMineBtn();
		layMine();
		ShowMineNumber();

		/**
		 *  layout
		 *	lb1 for the number of minesŁŹlb2 for timer,lb3 for the begin button
		 *  pane1 for lb1,lb2,lb3
		 *  pane2 for the area of mines
		 *  pane3 for pane1,pane2
		 */
		lb1=new JLabel();
		lb2=new JLabel("000");  
		lb3=new JLabel();
		if(mineNumber<10)
		{
			lb1.setText("00"+mineNumber);
		}
		else
		{
			lb1.setText("0"+mineNumber);
		}

		lb1.setHorizontalAlignment(SwingConstants.CENTER);
		lb2.setHorizontalAlignment(SwingConstants.CENTER);
		lb1.setPreferredSize(new Dimension(50,23));
		lb2.setPreferredSize(new Dimension(50,23));
		lb1.setOpaque(true);
		lb2.setOpaque(true);
		lb1.setBackground(Color.black);
		lb2.setBackground(Color.black);
		lb1.setForeground(Color.red);
		lb2.setForeground(Color.red);

		lb3.setLayout(new FlowLayout(FlowLayout.CENTER,10,3));
		lb3.add(beginBtn);

		pane1.setLayout(new BorderLayout());
		pane1.setPreferredSize(new Dimension(175 ,30));
		pane1.setBorder(new BevelBorder(BevelBorder.LOWERED));
		pane1.add(lb1,BorderLayout.WEST);
		pane1.add(lb3,BorderLayout.CENTER);
		pane1.add(lb2,BorderLayout.EAST);

		pane3.setBorder(new EmptyBorder(new Insets(0,0,0,0)));
		pane3.setLayout(new BorderLayout());
		pane3.add(pane1,BorderLayout.NORTH);
		pane3.add(pane2,BorderLayout.CENTER);
		
		Container con=getContentPane();
		con.add(pane3,BorderLayout.CENTER);
		setVisible(true);
	}

	public void pane2()
	{
		pane2=new JPanel();
		pane2.setLayout(new GridLayout(rows,cols,1,1));
		pane2.setBorder(new BevelBorder(BevelBorder.LOWERED));
		pane2.setPreferredSize(new Dimension(175,170));
	}

	/**
	 * Methods for buttons initialization 
	 */
	public void constructMineBtn()
	{
		for(int i=0;i<rows;i++)
		{
			for(int j=0;j<cols;j++)
			{
				mineBtn[i][j]=new MineButton();
				mineBtn[i][j].addMouseListener(new MouseE());
				//mineBtn[i][j].addMouseListener(new doub());
				mineBtn[i][j].setBorder(new BevelBorder(BevelBorder.RAISED));
				mineBtn[i][j].setBackground(new Color(220,220,220));
				pane2.add(mineBtn[i][j]);
			}
		}
	}
	
	/**
	 *  lay mines randomly
	 */
	public void layMine()
	{
		for(int i=0;i<mineNumber;i++)
		{
			int a=(int)(Math.random()*rows);
			int b=(int)(Math.random()*cols);
			if(mineBtn[a][b].id==19)
			{
				i--;
			}
			else
			{
				mineBtn[a][b].id=19;
			}
		}
	}

	/**
	 * After lay mines, the index of each button should be recalculated
	 */
	public void ShowMineNumber()
	{
		for(int i=0;i<rows;i++)
		{
			for(int j=0;j<cols;j++)
			{
				int index=10;
				if(mineBtn[i][j].id==10)
				{
					if(i-1>=0 & j-1>=0)
					{
						index=mineBtn[i-1][j-1].id==19?++index:index;
					}
					if(i-1>=0)
					{
						index=mineBtn[i-1][j].id==19?++index:index;
					}
					if(i-1>=0 & j+1<cols)
					{
						index=mineBtn[i-1][j+1].id==19?++index:index;
					}
					if(j-1>=0)
					{
						index=mineBtn[i][j-1].id==19?++index:index;
					}
					if(j+1<cols)
					{
						index=mineBtn[i][j+1].id==19?++index:index;
					}
					if(i+1<rows & j-1>=0)
					{
						index=mineBtn[i+1][j-1].id==19?++index:index;
					}
					if(i+1<rows)
					{
						index=mineBtn[i+1][j].id==19?++index:index;
					}
					if(i+1<rows & j+1<cols)
					{
						index=mineBtn[i+1][j+1].id==19?++index:index;
					}
					mineBtn[i][j].id=index;
				}
			}
		}
	}

	/**
	 *  Construct Menu
	 */
	public void constructMenu()
	{
		menuBar=new JMenuBar();
	
		if(difficulty==1){beginItem.setState(true);}
		if(difficulty==2){interItem.setState(true);}
		if(difficulty==3){advanItem.setState(true);}

		/**
		 * game menu
		 */
		menuGame.add(startItem);
		menuGame.addSeparator();
		menuGame.add(beginItem);
		menuGame.add(interItem);
		menuGame.add(advanItem);
		menuGame.addSeparator();
		menuGame.add(exitItem);
		/**
		 * about menu
		 */
		menuAbout.add(aboutItem);

		startItem.addActionListener(this);
		beginItem.addActionListener(this);
		interItem.addActionListener(this);
		advanItem.addActionListener(this);
		exitItem.addActionListener(this);

		aboutItem.addActionListener(this);
		/**
		 *  add to menuBar
		 */
		menuBar.add(menuGame);
		menuBar.add(menuAbout);
		setJMenuBar(menuBar);
	}
	
	/*********************
	 *  Methods for menu
	 */
	public void actionPerformed(ActionEvent e)
	{
		String choice=((JMenuItem)e.getSource()).getText().trim();
		if(choice.equals("Start"))
		{
			dispose();
			new MineFrame(tempmineNumber,rows,cols);
		}
		if(choice.equals("Beginner"))
		{
			dispose();
			new MineFrame(10,9,9);
		}
		if(choice.equals("Intermediate"))
		{
			dispose();
			new MineFrame(40,16,16);
		}
		if(choice.equals("Advanced"))
		{
			dispose();
			new MineFrame(99,16,30);
		}
		if(choice.equals("Exit"))
		{
			System.exit(0);
		}
		if(choice.equals("About Mine Sweeper"))
		{
			JOptionPane.showMessageDialog(null,
					"Developped by Lizhao HOU\nStudent from Sup¨Ślec France\nEmail:lizhao.hou@supelec.fr",
					"About Mine Sweeper"
					,JOptionPane.INFORMATION_MESSAGE);     
		}
	
	}

    /**************************
     *  class for begin BUTTON (the smile face above mine area)
     */
	class beginBtnFace implements MouseListener
	{
		public void mouseClicked(MouseEvent e)
		{
			dispose();
			//MineFrame.this.remove(pane2);
			new MineFrame(tempmineNumber,rows,cols);
			//constructMineBtn();

		}
		public void mouseEntered(MouseEvent e){}
		public void mouseExited(MouseEvent e){}
		public void mousePressed(MouseEvent e){}
		public void mouseReleased(MouseEvent e){}
	
	}
	
	/**********************
	 *  class for mouse click (left or right click)
	 */
	class MouseE implements MouseListener
	{
		MineButton mbtn;

		public void mouseClicked(MouseEvent e)
		{
			bttmp=mbtn;
			mbtn=(MineButton)e.getSource();
			if(!t.isRunning() && bool)   //by default, bool is true
			{
				t.start();
			}

			if(bool) 
			{
				e.getButton();  

				if(e.getButton()==MouseEvent.BUTTON1 && mbtn.id<20)
				{
					MouseLeft(e);
				}
				else if(e.getButton()==MouseEvent.BUTTON3)
				{
					MouseRight();
				}
			}
		}
		public void mouseEntered(MouseEvent e){}
		public void mouseExited(MouseEvent e){}
		public void mousePressed(MouseEvent e){}
		public void mouseReleased(MouseEvent e){}
	
		/**
		 *  left click
		 */
		public void MouseLeft(MouseEvent e)
		{
			/**
			 *  deduce from id, if not mine, should display its number
			 */
			if(mbtn.id==10){mbtn.setIcon(new ImageIcon("res/image/0.gif"));mbtn.setDisabledIcon(new ImageIcon("res/image/0.gif"));mbtn.id-=10;}
			if(mbtn.id==11){mbtn.setIcon(new ImageIcon("res/image/1.gif"));mbtn.setDisabledIcon(new ImageIcon("res/image/1.gif"));mbtn.id-=10;}
			if(mbtn.id==12){mbtn.setIcon(new ImageIcon("res/image/2.gif"));mbtn.setDisabledIcon(new ImageIcon("res/image/2.gif"));mbtn.id-=10;}
			if(mbtn.id==13){mbtn.setIcon(new ImageIcon("res/image/3.gif"));mbtn.setDisabledIcon(new ImageIcon("res/image/3.gif"));mbtn.id-=10;}
			if(mbtn.id==14){mbtn.setIcon(new ImageIcon("res/image/4.gif"));mbtn.setDisabledIcon(new ImageIcon("res/image/4.gif"));mbtn.id-=10;}
			if(mbtn.id==15){mbtn.setIcon(new ImageIcon("res/image/5.gif"));mbtn.setDisabledIcon(new ImageIcon("res/image/5.gif"));mbtn.id-=10;}
			if(mbtn.id==16){mbtn.setIcon(new ImageIcon("res/image/6.gif"));mbtn.setDisabledIcon(new ImageIcon("res/image/6.gif"));mbtn.id-=10;}
			if(mbtn.id==17){mbtn.setIcon(new ImageIcon("res/image/7.gif"));mbtn.setDisabledIcon(new ImageIcon("res/image/7.gif"));mbtn.id-=10;}
			if(mbtn.id==18){mbtn.setIcon(new ImageIcon("res/image/8.gif"));mbtn.setDisabledIcon(new ImageIcon("res/image/8.gif"));mbtn.id-=10;}
			mbtn.setBorder(new EmptyBorder(new Insets(1,1,1,1)));
			mbtn.setEnabled(false);
			win();             //detect if win
			/**
			 *  if clicked on mine, display the rest of mines, and game over
			 *  also change the begin Button image
			 */
			for(int i=0;i<rows;i++)
			{
				for(int j=0;j<cols;j++)
				{
					if(mbtn.id==19)
					{
						mbtn.setEnabled(false);
						mbtn.setIcon(new ImageIcon("res/image/DiLei_Bomd.gif"));
						mbtn.setDisabledIcon(new ImageIcon("res/image/DiLei_Bomd.gif"));
						if(mineBtn[i][j].id==19)
						{
							mineBtn[i][j].setEnabled(false);
							mineBtn[i][j].setIcon(new ImageIcon("res/image/DiLei.gif"));
							mineBtn[i][j].setDisabledIcon(new ImageIcon("res/image/DiLei.gif"));
							beginBtn.setIcon(new ImageIcon("res/image/88.jpg"));
							e.consume();
							if(t.isRunning())
							{
								t.stop();
								bool=false;
							}
						}
					}
					if(mbtn==mineBtn[i][j])  //if click on empty grid, recursion to find the other empty grids around it
					{
						if(mbtn.id==0)
						{
							recursion(i,j);
							return;
						}
					}
				}
			}
		}
		/**
		 * right click
		 */
		 public void MouseRight()
		 {
			 if(mbtn.isEnabled())
			 {
				for(int i=0;i<rows;i++)
				{
					for(int j=0;j<cols;j++)
					{
						if(mbtn==mineBtn[i][j])
						{
							if(mbtn==mineBtn[i][j] & mineBtn[i][j].id<20)
							{
								mbtn.setIcon(new ImageIcon("res/image/FLAG.GIF")); //Ŕ×ŁŹ˛ĺĆě
								mineBtn[i][j].id+=10;
								mineNumber--;
								if(mineNumber==0)
								{
									win();
								}
								setmineNumberLable();  //set lb1

							}
							else if(mbtn==mineBtn[i][j] & mineBtn[i][j].id>=20)
							{
								mbtn.setIcon(null);
								mineBtn[i][j].id-=10;
								mineNumber++;
								setmineNumberLable();  
							}
						}
					}
				}
			 }
		 }
		/**
		 *the number of mines on the label
		 */
		public void setmineNumberLable()
		{
			if(mineNumber<10)
			{
				lb1.setText("00"+mineNumber);
				if(mineNumber<0)
				{
					lb1.setText("-0"+Math.abs(mineNumber));
					if(mineNumber<=-10)
					{
						lb1.setText(""+mineNumber);
					}
				}
			}
			else
			{
				lb1.setText("0"+mineNumber);
			}
		}
	}

	/**
	 *  determine if win
	 */
	 public void win()
	 {
		int winMineNumber=0;
		int yyy=0;
		for(int k=0;k<rows;k++)
		{
			for(int l=0;l<cols;l++)
			{
				if(mineBtn[k][l].id==29)
				{
					winMineNumber++;
				}
				if(mineBtn[k][l].id<10 |mineBtn[k][l].id>=20)
				{
					yyy++;
				}
			}
		}
		if(winMineNumber==tempmineNumber & yyy==(rows*cols))
		{
			t.stop();
			bool=false;
			beginBtn.setIcon(new ImageIcon("res/image/ok.jpg"));
			finishtime=Integer.parseInt(lb2.getText().trim());
			JOptionPane.showMessageDialog(null,"Finish time:"+finishtime,"You WIN!",JOptionPane.INFORMATION_MESSAGE);
		}
	 }

	/**
	 * IMPORTANT:a click on empty grid will provoke all the empty grids nearby.
	 * Here a recursion algorithm is used.
	 * The description of mineBtn[x][y].id, please see the MineButton.java
	 */
	public void recursion(int x,int y)
	{
		for(int i=-1;i<2;i++)
		{
			if(i+x>=rows)continue;
			if(i+x<0)continue;
			for(int j=-1;j<2;j++)
			{
				if(j+y>=cols)continue;
				if(j+y<0)continue;
				if(i==0 & j==0)continue;
				if(mineBtn[i+x][j+y].id>=10 && mineBtn[i+x][j+y].id<20)
				{
					mineBtn[i+x][j+y].id-=10;
					if(mineBtn[i+x][j+y].id==0)
					{
						mineBtn[i+x][j+y].setIcon(new ImageIcon("res/image/0.gif"));
						mineBtn[i+x][j+y].setDisabledIcon(new ImageIcon("res/image/0.gif"));
						mineBtn[i+x][j+y].setEnabled(false);
						mineBtn[i+x][j+y].setBorder(new EmptyBorder(new Insets(1,1,1,1)));
						recursion(i+x,j+y);
					}
					if(mineBtn[i+x][j+y].id>=1 && mineBtn[i+x][j+y].id<=8)    
					{
						if(mineBtn[i+x][j+y].id==1){mineBtn[i+x][j+y].setIcon(new ImageIcon("res/image/1.gif"));mineBtn[i+x][j+y].setDisabledIcon(new ImageIcon("res/image/1.gif"));}
						if(mineBtn[i+x][j+y].id==2){mineBtn[i+x][j+y].setIcon(new ImageIcon("res/image/2.gif"));mineBtn[i+x][j+y].setDisabledIcon(new ImageIcon("res/image/2.gif"));}
						if(mineBtn[i+x][j+y].id==3){mineBtn[i+x][j+y].setIcon(new ImageIcon("res/image/3.gif"));mineBtn[i+x][j+y].setDisabledIcon(new ImageIcon("res/image/3.gif"));}
						if(mineBtn[i+x][j+y].id==4){mineBtn[i+x][j+y].setIcon(new ImageIcon("res/image/4.gif"));mineBtn[i+x][j+y].setDisabledIcon(new ImageIcon("res/image/4.gif"));}
						if(mineBtn[i+x][j+y].id==5){mineBtn[i+x][j+y].setIcon(new ImageIcon("res/image/5.gif"));mineBtn[i+x][j+y].setDisabledIcon(new ImageIcon("res/image/5.gif"));}
						if(mineBtn[i+x][j+y].id==6){mineBtn[i+x][j+y].setIcon(new ImageIcon("res/image/6.gif"));mineBtn[i+x][j+y].setDisabledIcon(new ImageIcon("res/image/6.gif"));}
						if(mineBtn[i+x][j+y].id==7){mineBtn[i+x][j+y].setIcon(new ImageIcon("res/image/7.gif"));mineBtn[i+x][j+y].setDisabledIcon(new ImageIcon("res/image/7.gif"));}
						if(mineBtn[i+x][j+y].id==8){mineBtn[i+x][j+y].setIcon(new ImageIcon("res/image/8.gif"));mineBtn[i+x][j+y].setDisabledIcon(new ImageIcon("res/image/8.gif"));}
						mineBtn[i+x][j+y].setBorder(new EmptyBorder(new Insets(1,1,1,1)));
						mineBtn[i+x][j+y].setEnabled(false);
					}
					if(mineBtn[i+x][j+y].id==9)
					{
						mineBtn[i+x][j+y].id=19;
					}
				}
			}
		}
	}

	
	
	/*******************
	 *timer : display the time used
	 */
	class timeUsed implements ActionListener
	{
		int time=0;
		public void actionPerformed(ActionEvent e)
		{
			time++;
			if(time<10)lb2.setText("00"+time);
			if(time>=10 & time<=99)lb2.setText("0"+time);
			if(time>=100)lb2.setText(time+"");
		}
	}
}
