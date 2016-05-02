package alpha2;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageFilter;
import java.io.*;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.MouseInputListener;
public class ShowImage extends JFrame implements ActionListener,MouseInputListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static BufferedImage image1,image2;
	static int flag = 0;
	static JFrame frame;
	static Panel panel ,panel1,panel2;
	static JComboBox combo ;
	static JTextField text;
	static JTextArea area;
	static File file = null;
	static String path=null;
	static JButton selectBtn, prfmBtn, browseBtn;
	static Container cont;
	static int select=0;
	public static Stack<Point> storeCoordinates= new Stack<Point>();
	static int xCoordinate =0;
	static int yCoordinate =0;
	static String threshold =null;
	static int noOfClick =0;
	static JPopupMenu Pmenu;
	static JMenuItem menuItem;
	static String getOutputImage=null;
	

	ShowImage()
	{
		frame = this;
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Display Image");


		Container cont = getContentPane();
		cont.setLayout(new BorderLayout());
		

		panel = new Panel();
		panel1 = new Panel();
		panel2 = new Panel();
		area= new JTextArea();
		area.setColumns(20);
		area.setRows(120);
		selectBtn = new JButton("select"); 
		prfmBtn = new JButton("Perform");
		browseBtn = new JButton("Browse");
		text = new JTextField(20);
		String course[] = {"","Image thresholding","Region growing",""};
		combo = new JComboBox(course);
		combo.setBackground(Color.gray);
		combo.setForeground(Color.red);
		JLabel label2=new JLabel("Info about seed value:");
		

		panel1.add(area);
		cont.add(panel1,BorderLayout.EAST);
		
		
		//panel.add(selectBtn);
		panel.add(prfmBtn);
		panel.add(browseBtn);
		panel.add(combo);
		panel.add(text);
		//
		frame.addMouseListener((MouseListener) frame);
		cont.add(panel,BorderLayout.SOUTH);
		area.setEditable(false);
		browseBtn.addActionListener(this);
		prfmBtn.addActionListener(this);
		combo.addActionListener(this);
		selectBtn.addActionListener(this);

		Toolkit toolkit =  Toolkit.getDefaultToolkit ();
		Dimension dim = toolkit.getScreenSize();
		frame.setMinimumSize(dim);
		area.setText("Seed point values are :");

		pack();
		setVisible(true);

	}

	static public void main(String args[]) throws Exception 
	{
		ShowImage showImage = new ShowImage();

	}

	public void paint(Graphics g)
	{
		super.paint(g);
		int height=0;
		int width=0;

		Toolkit toolkit =  Toolkit.getDefaultToolkit ();
		Dimension dim = toolkit.getScreenSize();

		if(image1!=null)
		{
			height = image1.getHeight();
			width = image1.getWidth();
		}

		if (flag==0)

			g.drawImage(image1,14,46,width,height, null);
		else{
			g.drawImage(image1,14,46,width,height, null);
			File input = new File(getOutputImage);
			try {
				image2 = ImageIO.read(input);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}	
			
			if(width>(dim.width/3))
			{
				g.drawImage(image2,14,(46+height),width,height, null);
			}
			else
			{
				g.drawImage(image2,(10+dim.width/3),46,width,height, null);
			}
			
			
				//g.drawImage(image2,(10+dim.width/3),46,width,height, null);
			
			//g.drawImage(image,((2*dim.width)/3),0,width,height, null);	
			flag=0;

		}


	}

	@Override
	public void actionPerformed(ActionEvent e) {
		System.out.println("This"+e.getID()+" "+e.getActionCommand());
		if(e.getSource()== browseBtn)
		{

			JFileChooser chooser = new JFileChooser();
			/*chooser.showOpenDialog(null);*/
			int returnVal = chooser.showOpenDialog(null);

			if(returnVal == JFileChooser.APPROVE_OPTION) {
				file = chooser.getSelectedFile();
				path=file.getPath();

				File input = new File(path);
				try {
					image1 = ImageIO.read(input);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}	
				frame.repaint();
				frame.validate();
			}
		}

		else
			if(e.getSource()==prfmBtn)
			{
				Caller clr = new Caller();
				//
				threshold=text.getText();
				String[] results = threshold.split(","); 
				int threshold[] = new int [300];

				for(int i =0;i<results.length;i++)
				{
					String temp=results[i];
					threshold[i]=Integer.parseInt(temp);
				}		
				 getOutputImage=clr.getPerformed(path, storeCoordinates,select,threshold);
				 
				 flag=1;
				 frame.repaint();
				 frame.validate(); 
			}
			else
				if(e.getSource()==selectBtn)
				{

					
				}

				else
					if(e.getSource()==combo)
					{
						select =combo.getSelectedIndex();
						System.out.println("Select "+select);
						System.out.println(combo.getSelectedIndex());
					}



	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		System.out.println(arg0.getX());
		System.out.println(arg0.getY());
		xCoordinate=arg0.getX();
		yCoordinate=arg0.getY();
		storeCoordinates.add(new Point(xCoordinate-14,yCoordinate-46));
		String x = String.valueOf(xCoordinate);
		String y = String.valueOf(yCoordinate);
		
		Toolkit toolkit =  Toolkit.getDefaultToolkit ();
		Dimension dim = toolkit.getScreenSize();
		
		
		int height =0, width=0;
		if(image1!=null)
		{
			 height = image1.getHeight();
			 width = image1.getWidth();
		}
		
		int storePixelRed[][]=new int [height][width];
		int storePixelGreen[][]=new int [height][width];
		int storePixelBlue[][]=new int [height][width];
		int c=0;
		for(int h = 0; h<height; h++)
		{
			for(int w = 0; w<width ; w++)
			{
				c = image1.getRGB(w,h);
				
					int clr = (c&0x00ff0000)>>16;
					storePixelRed[h][w]=clr;
					clr = (c&0x0000ff00)>>8;
					storePixelGreen[h][w]=clr;					
					clr = (c&0x000000ff);
					storePixelBlue[h][w]=clr;
									
			}
		}
		
		area.setText("Seed point values are :\n "+"Red "+(storePixelRed[yCoordinate-46][xCoordinate-14])+" \n"+"Green "+(storePixelGreen[yCoordinate-46][xCoordinate-14])+"\n"+"Blue "+(storePixelBlue[yCoordinate-46][xCoordinate-14]));
		
		
		
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}
}



