package gif;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;

public class PairGenerator implements ActionListener{
	public HashMap<String, Color> colors=new HashMap<String, Color>();
	public int max=0;
	public int modenum=0;
	public int modetwo=0;
	public HashMap<Integer, Integer> mode= new HashMap<Integer, Integer>();
	public HashMap<String, Integer> counter= new HashMap<String, Integer>();
	public String a;
	public String lastpair;
	public HashMap<Character, String> basetobase= new HashMap<Character, String>();
	public JScrollPane scroll;
	JFrame panel;
	double width;
	double height;
	int squareheight;
	int squarewidth;
	int vert=0;
	int sqrt;
	int numchars;
	JTextField xsize;
	JTextField ysize;
	ArrayList<ColorCounter> list;
	Dimension dim;
	int drawn=0;
	JFrame frame;
	JTextField acolor;
	JTextField ccolor;
	JTextField tcolor;
	JTextField gcolor;
	int kone=0;
	int ktwo=0;
	int kthree=1;
	int kfour=2;
	int kfive=3;
	int ksix=4;
	File saver;
	static HashMap<String, Color> colorstat= new HashMap<String, Color>();
    static {
		colorstat.put("aa", new Color(Integer.parseInt("000000", 16)));
		colorstat.put("ac", new Color(Integer.parseInt("ff3333", 16)));
		colorstat.put("at", new Color(Integer.parseInt("bb5500", 16)));
		colorstat.put("ag", new Color(Integer.parseInt("999900", 16)));
		colorstat.put("cg", new Color(Integer.parseInt("827800", 16)));
		colorstat.put("ca", new Color(Integer.parseInt("990066", 16)));
		colorstat.put("ct", new Color(Integer.parseInt("009900", 16)));
		colorstat.put("cc", new Color(Integer.parseInt("009999", 16)));
		colorstat.put("gg", new Color(Integer.parseInt("cc9966", 16)));
		colorstat.put("ga", new Color(Integer.parseInt("666666", 16)));
		colorstat.put("gt", new Color(Integer.parseInt("ff6600", 16)));
		colorstat.put("gc", new Color(Integer.parseInt("000ff", 16)));
		colorstat.put("tt", new Color(Integer.parseInt("33ff33", 16)));
		colorstat.put("ta", new Color(Integer.parseInt("ffff33", 16)));
		colorstat.put("tc", new Color(Integer.parseInt("99ccff", 16)));
		colorstat.put("tg", Color.white);
    }
    static HashMap<Integer, String> nameconverter= new HashMap<Integer, String>();
    static{
    	nameconverter.put(1, "aa");
    	nameconverter.put(2, "ac");
    	nameconverter.put(3, "at");
    	nameconverter.put(4, "ag");
    	nameconverter.put(5, "cg");
    	nameconverter.put(6, "ca");
    	nameconverter.put(7, "ct");
    	nameconverter.put(8, "cc");
    	nameconverter.put(9, "gg");
    	nameconverter.put(10, "ga");
    	nameconverter.put(11, "gt");
    	nameconverter.put(12, "gc");
    	nameconverter.put(13, "tt");
    	nameconverter.put(14, "ta");
    	nameconverter.put(15, "tc");
    	nameconverter.put(16, "tg");
    }
    static HashMap<String, Integer> stringconverter= new HashMap<String, Integer>();
    static{
    	stringconverter.put("aa", 1);
    	stringconverter.put("ac", 2);
    	stringconverter.put("at", 3);
    	stringconverter.put("ag", 4);
    	stringconverter.put("cg", 5);
    	stringconverter.put("ca", 6);
    	stringconverter.put("ct", 7);
    	stringconverter.put("cc", 8);
    	stringconverter.put("gg", 9);
    	stringconverter.put("ga", 10);
    	stringconverter.put("gt", 11);
    	stringconverter.put("gc", 12);
    	stringconverter.put("tt", 13);
    	stringconverter.put("ta", 14);
    	stringconverter.put("tc", 15);
    	stringconverter.put("tg", 16);
    	
    }
	
	public PairGenerator(JTextField x, JTextField y){
		colors.put("aa", new Color(Integer.parseInt("000000", 16)));
		colors.put("ac", new Color(Integer.parseInt("ff3333", 16)));
		colors.put("at", new Color(Integer.parseInt("bb5500", 16)));
		colors.put("ag", new Color(Integer.parseInt("999900", 16)));
		colors.put("cg", new Color(Integer.parseInt("827800", 16)));
		colors.put("ca", new Color(Integer.parseInt("990066", 16)));
		colors.put("ct", new Color(Integer.parseInt("009900", 16)));
		colors.put("cc", new Color(Integer.parseInt("009999", 16)));
		colors.put("gg", new Color(Integer.parseInt("cc9966", 16)));
		colors.put("ga", new Color(Integer.parseInt("666666", 16)));
		colors.put("gt", new Color(Integer.parseInt("ff6600", 16)));
		colors.put("gc", new Color(Integer.parseInt("000ff", 16)));
		colors.put("tt", new Color(Integer.parseInt("33ff33", 16)));
		colors.put("ta", new Color(Integer.parseInt("ffff33", 16)));
		colors.put("tc", new Color(Integer.parseInt("99ccff", 16)));
		colors.put("tg", Color.white);
		basetobase.put('a', "Adenine");
		basetobase.put('g', "Guanine");
		basetobase.put('t', "Thymine");
		basetobase.put('c', "Cytosine");
		xsize=x;
		ysize=y;
		panel=new JFrame();
		File f=new File("C:/Users/"+System.getProperty("user.name")+"/Desktop/NewGeneImage.gif");
		int i=0;
		while (f.exists()){
			f=new File("C:/Users/"+System.getProperty("user.name")+"/Desktop/NewGeneImage"+i+".gif");
			i++;
		}
		saver=f;
	}
	public void readList(File f){
		try{
			saver.createNewFile();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		int n=0;
		int c='@';
		char ch='@';
		int total=0;
		int counter=0;
		int max=0;
		StringBuilder s= new StringBuilder();
		boolean lastodd=false;
		list= new ArrayList<ColorCounter>();
		boolean lastzero=false;
		try{
			FileReader ist = new FileReader(f);
			BufferedReader in = new BufferedReader(ist);
			boolean t=true;
			while(t==true){
				c=in.read();
				counter++;
				//if (counter==100){
				//	t=false;
				//}
				if ((char)c=='n'){
					while((char)c=='n'||(char)c=='0'){
						c=in.read();
					}
				}
				if (ch=='z'){
					t=false;
				}
				else if (c==-1){
					t=false;
				}
				else if ((char)c=='z'){
					n=Integer.parseInt(s.toString().replace(ch, '1'), 2);
					if (lastodd){
						n--;
					}
					if (n!=0&&n!=1){
						//System.out.println(s);
						list.add(new ColorCounter(n/2, stringconverter.get(""+ch+ch), total));
						//System.out.println(""+ch+ch+" "+n/2);
						if(mode.containsKey(n/2)){
							mode.put(n/2, mode.get(n/2)+1);
						}
						else{
							mode.put(n/2, 1);
						}
						if(n/2>max){
							max=n/2;
						}
						total+=n/2;
					}
					t=false;
				}
				else if ((char)c!=ch&&(char)c!='0'&&ch!='@'&&ch!='n'){
					//System.out.println(counter);
					n=Integer.parseInt(s.toString().replace(ch, '1'), 2);
					if (lastodd){
						n--;
					}
					if (n%2==1 && n!=1&& n!=0){
						list.add(new ColorCounter(n/2, stringconverter.get(""+ch+ch), total));
						//System.out.println(""+ch+ch);
						if(mode.containsKey(n/2)){
							mode.put(n/2, mode.get(n/2)+1);
						}
						else{
							mode.put(n/2, 1);
						}
						if(n/2>max){
							max=n/2;
						}
						total+=n/2;
						lastodd=true;
						list.add(new ColorCounter(1, stringconverter.get(""+ch+(char)c), total));
						//System.out.println(""+ch+(char)c);
						if(mode.containsKey(1)){
							mode.put(1, mode.get(1)+1);
						}
						else{
							mode.put(1, 1);
						}
						total++;
						lastzero=false;
					}
					else if (n==1){
						lastodd=true;
						list.add(new ColorCounter(1, stringconverter.get(""+ch+(char)c), total));
						//System.out.println(""+ch+(char)c);
						if(mode.containsKey(1)){
							mode.put(1, mode.get(1)+1);
						}
						else{
							mode.put(1, 1);
						}
						total++;
						lastzero=false;
					}
					else if (n!=0){
						lastodd=false;
						list.add(new ColorCounter(n/2, stringconverter.get(""+ch+ch), total));
						//System.out.println(""+ch+ch);
						total+=n/2;
						lastzero=false;
					}
					else if (n==0){
						lastodd=false;
						lastzero=true;
					}
					ch=(char)c;
					s=new StringBuilder();
					s.append((char)c);
				}
				else if ((char)c!=ch&&(char)c!='0'){
					s= new StringBuilder();
					ch=(char)c;
					s.append((char)c);
				}
				else{
					s.append((char)c);
				}
				if (counter%5000000==0){
					list.trimToSize();
				}
			}
			in.close();
			ist.close();
			list.trimToSize();
		}
		catch(Exception e){
			System.out.println(counter+": "+ch+ch);
			System.out.println(n);
			System.out.println(counter+": "+ch+(char)c);
			e.printStackTrace();
		}
	}
	public void makeList(File f, int t){
		long startTime = System.currentTimeMillis();
		readList(f);
		long stopTime = System.currentTimeMillis();
		System.out.println("Elapsed time was " + (stopTime - startTime) + " miliseconds.");
		//s=s.toLowerCase();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		width = screenSize.getWidth();
		height = screenSize.getHeight();
		lastpair="";
		numchars=0;
	/*	for (int i=s.indexOf("\n"); i<s.length();i+=2){
			if ((s.charAt(i)=='a'||s.charAt(i)=='g'||s.charAt(i)=='c'||s.charAt(i)=='t')&&(s.charAt(i-1)=='a'||s.charAt(i-1)=='g'||s.charAt(i-1)=='c'||s.charAt(i-1)=='t')){
				//System.out.println(i);
				numchars+=2;
				thispair=""+s.charAt(i-1)+s.charAt(i);
				if (counter.containsKey(thispair)){
					counter.put(thispair, counter.get(thispair)+1);
				}
				else{
					counter.put(thispair, 1);
				}
				if (thispair.equals(lastpair)){
					//System.out.println("First if");
					count++;
				}
				else{
					//System.out.println("First else");
					if (!lastpair.equals("")){
						list.add(new ColorCounter(count, lastpair, i-(count*2)));
						if(mode.containsKey(count)){
							mode.put(count, mode.get(count)+1);
						}
						else{
							mode.put(count, 1);
						}
					}
					lastpair=""+s.charAt(i-1)+s.charAt(i);
					if (count>max){
						max=count;
					}
					count=1;
				}
			}
		}
		list.add(new ColorCounter(count, lastpair,s.length()-(count*2) ));
		if(mode.containsKey(count)){
			mode.put(count, mode.get(count)+1);
		}
		else{
			mode.put(count, 1);
		}
		if (count>max){
			max=count;
		}
		*/
		int modehelper=0;
		int modehelpertwo=0;
		Object[] ar=mode.keySet().toArray();
		for(int i=0;i<mode.size();i++){
			if(mode.get((Integer)ar[i])>modehelper){
				modetwo=modenum;
				modenum=(Integer)ar[i];
				modehelpertwo=modehelper;
				modehelper=mode.get((Integer)ar[i]);
			}
			else if(mode.get((Integer)ar[i])>modehelpertwo){
				modetwo=(Integer)ar[i];
				modehelpertwo=mode.get((Integer)ar[i]);
			}
		}
		if (modenum==0){
			modenum=modetwo;
		}
		else if (modenum==1){
			modenum=modetwo;
		}
		//System.out.println(list.get(0).getNam());
		if (t==1){
			vert=0;
			makeImage();
		}
		else if (t==-1){
			vert=-1;
			makeSnakeImage();
		}
		else{
			vert=1;
			makeVertImage();
		}
	}
	public void makeImage(){
		sqrt=(int) Math.sqrt(list.size());
		//System.out.println(list.toString());
		squarewidth=(int)(width/sqrt);
		sqrt=(int)(sqrt+((width-(squarewidth*sqrt))/squarewidth));
		squareheight=(int)(height/Math.ceil(list.size() / (double)sqrt));
		/*System.out.println(height);
		System.out.println(width);
		System.out.println(squarewidth);
		System.out.println(squareheight);
		System.out.println(sqrt);
		System.out.println(list.size());
		System.out.println(list.size()%sqrt);*/
		if (!xsize.getText().equals("")&&!ysize.getText().equals("")){
			boolean cat=false;
			try{
				squarewidth=Integer.parseInt(xsize.getText());
				squareheight=Integer.parseInt(ysize.getText());
				sqrt=(int) Math.sqrt(list.size());
			}
			catch(Exception e){
				cat=true;
			}
			if (cat){
				squarewidth=(int)(width/sqrt);
				sqrt=(int)(sqrt+((width-(squarewidth*sqrt))/squarewidth));
				squareheight=(int)(height/Math.ceil(list.size() / (double)sqrt));
			}
		}
		if (((int)Math.ceil(list.size() / sqrt)+1)*squareheight>height){
			sqrt=(int)Math.floor(width/squarewidth);
		}
		BufferedImage image = new BufferedImage(sqrt*squarewidth, (int)(squareheight*Math.ceil(list.size()/(double)sqrt)), BufferedImage.TYPE_INT_RGB);
		dim= new Dimension(image.getWidth(), image.getHeight());
		Color c;
		//System.out.println(modenum);
		Graphics g = image.getGraphics(); 
		int k;
		for (int i=0; i<(int)Math.ceil(list.size() / sqrt);i++){
			if (i+1==Math.ceil(list.size()/sqrt)){
				for (int n=0; n<list.size() % sqrt; n++){
					//System.out.println((i*sqrt)+n);
					//System.out.println(Math.ceil(list.size()/20.0));
					c= colors.get(nameconverter.get(list.get((i*sqrt)+n).getName()));
					//System.out.println(c);
					//System.out.println(list.get((i*sqrt)+n).getNam());
					
					if (list.get((i*sqrt)+n).getNumber()==max){
						//System.out.println(max+" "+i+" "+n);
						k=kone;
					}
					else if (list.get((i*sqrt)+n).getNumber()>modenum){
						k=ktwo;
					}
					else if (list.get((i*sqrt)+n).getNumber()==modenum){
						k=kthree;
					}
					else if (list.get((i*sqrt)+n).getNumber()==1){
						k=kfive;
					}
					else{
						k=kfour;
					}
					for (int p=0; p<k;p++){
						c=c.darker();
					}
					//System.out.println(c);
					g.setColor(c);
					g.fillRect((n*squarewidth), i*squareheight, squarewidth, squareheight);
					drawn++;
					g.setColor(Color.black);
					if (squarewidth>1){
						g.fillRect((n*squarewidth), i*squareheight, 1, squareheight);
					}
					if(n+1==list.size()%sqrt){
						c= Color.white;
						g.setColor(c);
						g.fillRect(i*squarewidth, (n+1*squareheight), squarewidth*(sqrt-(list.size()%sqrt)), squareheight);
					}
				}
			}
			else{
			for (int n=0; n<sqrt+1; n++){
				//System.out.println(Math.ceil(list.size()/20.0));
				c= colors.get(nameconverter.get(list.get((i*sqrt)+n).getName()));
				//System.out.println(list.get((i*20)+n).getNam());
				//System.out.println(colors.get(list.get((i*20)+n).getNam()));
				if (list.get((i*sqrt)+n).getNumber()==max){
					k=kone;
				}
				else if (list.get((i*sqrt)+n).getNumber()>((max-modenum)/2)+modenum){
					k=ktwo;
				}
				else if(list.get((i*sqrt)+n).getNumber()>modenum){
					k=kthree;
				}
				else if (list.get((i*sqrt)+n).getNumber()==modenum){
					k=kfour;
				}
				else if (list.get((i*sqrt)+n).getNumber()==1){
					k=ksix;
				}
				else{
					k=kfive;
				}
				for (int p=0; p<k;p++){
					c=c.darker();
				}
				//System.out.println(c);
				g.setColor(c);
				g.fillRect((n*squarewidth), i*squareheight, squarewidth, squareheight);
				drawn++;
				g.setColor(Color.black);
				if (squarewidth>1){
					g.fillRect((n*squarewidth), i*squareheight, 1, squareheight);
				}
				if (squareheight>1){
					g.fillRect(0, i*squareheight, (int)width, 1);
				}
			}
			}
		}
		//System.out.println(list.size());
		g.setColor(Color.black);
		g.fillRect((int)width-1, 0, 1, (int)height);
		panel.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		scroll = new JScrollPane (new JLabel(new ImageIcon(image)), 
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.addMouseListener(new MouseWatcher(this));
		panel.getContentPane().add(scroll);
		panel.add(new JLabel("Number of bases represented: "+numchars+"  Number of squares on screen: "+list.size()), BorderLayout.PAGE_END);
		panel.pack();
		panel.setExtendedState(JFrame.MAXIMIZED_BOTH);
		panel.setVisible(true);
		try {  
			ImageIO.write(image, "gif", new File("C:/Users/"+System.getProperty("user.name")+"/Desktop/NewGeneImage.gif")); 
		} catch (IOException e) {  
		 e.printStackTrace(); 
		}
		//System.out.println(drawn);
	}
	public void makeVertImage(){
		sqrt=(int) Math.sqrt(list.size());
		//System.out.println(list.toString());		
		squareheight=(int)(height/sqrt);
		sqrt=(int)(sqrt+((height-(squareheight*sqrt))/(double)squareheight));
		squarewidth=(int)(width/Math.ceil(list.size() / (double)sqrt));
		/*System.out.println(height);
		System.out.println(width);
		System.out.println(squareheight);
		System.out.println(squarewidth);
		System.out.println(list.size());
		System.out.println(sqrt);
		System.out.println(Math.ceil(list.size()/sqrt));
		System.out.println(list.size()%sqrt);*/
		if (!xsize.getText().equals("")&&!ysize.getText().equals("")){
			boolean cat=false;
			try{
				squarewidth=Integer.parseInt(xsize.getText());
				squareheight=Integer.parseInt(ysize.getText());
				sqrt=(int) Math.sqrt(list.size());
			}
			catch(Exception e){
				cat=true;
			}
			if (cat){
				squarewidth=(int)(width/sqrt);
				sqrt=(int)(sqrt+((width-(squarewidth*sqrt))/squarewidth));
				squareheight=(int)(height/Math.ceil(list.size() / (double)sqrt));
			}
		}
		if (((int)Math.ceil(list.size() / sqrt)+1)*squareheight>height){
			sqrt=(int)Math.floor(width/squarewidth);
		}
		BufferedImage image = new BufferedImage((int)(squarewidth*Math.ceil(list.size()/(double)sqrt)), sqrt*squareheight, BufferedImage.TYPE_INT_RGB);
		dim= new Dimension(image.getWidth(), image.getHeight());
		Color c;
		Graphics g = image.getGraphics(); 
		int k;
		for (int i=0; i<(int)Math.ceil(list.size() / sqrt)+1;i++){
			if (i==Math.ceil(list.size()/sqrt)){
				for (int n=0; n<list.size()%sqrt; n++){
					/*System.out.println("number: "+(i*sqrt)+n);
					System.out.println("i: "+i);
					System.out.println("n: "+n);
					System.out.println(height);
					System.out.println("width: "+width);
					System.out.println(squareheight);
					System.out.println(squarewidth);*/
					//System.out.println(Math.ceil(list.size()/20.0));
					c= colors.get(nameconverter.get(list.get((i*sqrt)+n).getName()));
					//System.out.println(c);
					//System.out.println(list.get((i*sqrt)+n).getNam());
					if (list.get((i*sqrt)+n).getNumber()==max){
						k=kone;
					}
					else if (list.get((i*sqrt)+n).getNumber()>((max-modenum)/2)+modenum){
						k=ktwo;
					}
					else if(list.get((i*sqrt)+n).getNumber()>modenum){
						k=kthree;
					}
					else if (list.get((i*sqrt)+n).getNumber()==modenum){
						k=kfour;
					}
					else if (list.get((i*sqrt)+n).getNumber()==1){
						k=ksix;
					}
					else{
						k=kfive;
					}
					//System.out.println(c);
					g.setColor(c);
					g.fillRect(i*squarewidth, (n*squareheight), squarewidth, squareheight);
					g.setColor(Color.black);
					if (squarewidth>1){
						g.fillRect(i*squarewidth, (n*squareheight), 1, squareheight);
					}
					if(n+1==list.size()%sqrt){
						c= Color.white;
						g.setColor(c);
						g.fillRect(i*squarewidth, (n+1*squareheight), squarewidth, squareheight*(sqrt-(list.size()%sqrt)));
					}
				}
			}
			else{
			for (int n=0; n<sqrt; n++){
				//System.out.println((i*20)+n);
				//System.out.println(Math.ceil(list.size()/20.0));
				c= colors.get(nameconverter.get(list.get((i*sqrt)+n).getName()));
				//System.out.println(c);
				//System.out.println(list.get((i*20)+n).getNam());
				if (list.get((i*sqrt)+n).getNumber()==max){
					k=kone;
				}
				else if (list.get((i*sqrt)+n).getNumber()>((max-modenum)/2)+modenum){
					k=ktwo;
				}
				else if(list.get((i*sqrt)+n).getNumber()>modenum){
					k=kthree;
				}
				else if (list.get((i*sqrt)+n).getNumber()==modenum){
					k=kfour;
				}
				else if (list.get((i*sqrt)+n).getNumber()==1){
					k=ksix;
				}
				else{
					k=kfive;
				}
				//System.out.println(c);
				g.setColor(c);
				g.fillRect(i*squarewidth, (n*squareheight), squarewidth, squareheight);
				g.setColor(Color.black);
				if (squarewidth>1){
					g.fillRect(i*squarewidth, (n*squareheight), 1, squareheight);
				}
				if (squareheight>1){
					g.fillRect(0, i*squareheight, (int)width, 1);
				}
			}
			}
		}
		g.setColor(Color.black);
		g.fillRect((int)width-1, 0, 1, (int)height);
		panel.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        scroll = new JScrollPane (new JLabel(new ImageIcon(image)), 
      		   JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.addMouseListener(new MouseWatcher(this));
		panel.getContentPane().add(scroll);
 		panel.add(new JLabel("Number of bases represented: "+numchars+"  Number of squares on screen: "+list.size()), BorderLayout.PAGE_END);
 		panel.pack();
		panel.setExtendedState(JFrame.MAXIMIZED_BOTH);
 		panel.setVisible(true);
		try {  
			ImageIO.write(image, "gif", new File("C:/Users/"+System.getProperty("user.name")+"/Desktop/NewGeneImage.gif")); 
		} catch (IOException e) {  
		 e.printStackTrace(); 
		}
	}
	public void makeSnakeImage(){
		//System.out.println(xsize.getText());
		//System.out.println(ysize.getText());
		sqrt=(int) Math.sqrt(list.size());
		//System.out.println(list.toString());
		squarewidth=(int)(width/sqrt);
		sqrt=(int)(sqrt+((width-(squarewidth*sqrt))/squarewidth));
		squareheight=(int)(height/Math.ceil(list.size() / (double)sqrt));
		/*System.out.println(height);
		System.out.println(width);
		System.out.println(squarewidth);
		System.out.println(squareheight);
		System.out.println(sqrt);
		System.out.println(list.size());
		System.out.println(list.size()%sqrt);*/
		if (!xsize.getText().equals("")&&!ysize.getText().equals("")){
			boolean cat=false;
			try{
				squarewidth=Integer.parseInt(xsize.getText());
				squareheight=Integer.parseInt(ysize.getText());
				sqrt=(int) Math.sqrt(list.size());
			}
			catch(Exception e){
				cat=true;
			}
			if (cat){
				squarewidth=(int)(width/sqrt);
				sqrt=(int)(sqrt+((width-(squarewidth*sqrt))/squarewidth));
				squareheight=(int)(height/Math.ceil(list.size() / (double)sqrt));
			}
		}
		if (((int)Math.ceil(list.size() / sqrt)+1)*squareheight>height){
			sqrt=(int)Math.floor(width/squarewidth);
		}
		BufferedImage image = new BufferedImage(sqrt*squarewidth, (int)(squareheight*Math.ceil(list.size()/(double)sqrt)), BufferedImage.TYPE_INT_RGB);
		dim= new Dimension(image.getWidth(), image.getHeight());
		Color c;
		Graphics g = image.getGraphics(); 
		int k;
		for (int i=0; i<(int)Math.ceil(list.size() / sqrt)+1;i++){
			if (i==Math.ceil(list.size()/sqrt)&&Math.ceil(list.size()/sqrt)%2==0){
				for (int n=0; n<list.size() % sqrt; n++){
					//System.out.println((i*sqrt)+n);
					//System.out.println(Math.ceil(list.size()/20.0));
					c= colors.get(nameconverter.get(list.get((i*sqrt)+n).getName()));
					//System.out.println(c);
					//System.out.println(list.get((i*sqrt)+n).getNam());
					if (list.get((i*sqrt)+n).getNumber()==max){
						k=kone;
					}
					else if (list.get((i*sqrt)+n).getNumber()>((max-modenum)/2)+modenum){
						k=ktwo;
					}
					else if(list.get((i*sqrt)+n).getNumber()>modenum){
						k=kthree;
					}
					else if (list.get((i*sqrt)+n).getNumber()==modenum){
						k=kfour;
					}
					else if (list.get((i*sqrt)+n).getNumber()==1){
						k=ksix;
					}
					else{
						k=kfive;
					}
					for (int p=0; p<k;p++){
						c=c.darker();
					}
					//System.out.println(c);
					g.setColor(c);
					g.fillRect((n*squarewidth), i*squareheight, squarewidth, squareheight);
					g.setColor(Color.black);
					if (squarewidth>1){
						g.fillRect((n*squarewidth), i*squareheight, 1, squareheight);
					}
					if (n+1==list.size()%sqrt){
						c= Color.white;
						g.setColor(c);
						g.fillRect(((n+1)*squarewidth), i*squareheight, squarewidth*(sqrt-(list.size()%sqrt)), squareheight);
					}
				}
			}
			else if (i==Math.ceil(list.size()/sqrt)&&Math.ceil(list.size()/sqrt)%2==1){
				for (int n=(list.size()%sqrt)-1; n>-1; n--){
					//System.out.println((i*sqrt)+((list.size()%sqrt)-1)-n);
					//System.out.println(list.get((i*sqrt)+((list.size()%sqrt)-1)-n).getNam());
					//System.out.println((i*sqrt)+n);
					//System.out.println(Math.ceil(list.size()/20.0));
					c= colors.get(nameconverter.get(list.get((i*sqrt)+((list.size()%sqrt)-1)-n).getName()));
					//System.out.println(c);
					//System.out.println(list.get((i*sqrt)+n).getNam());
					if (list.get((i*sqrt)+n).getNumber()==max){
						k=kone;
					}
					else if (list.get((i*sqrt)+n).getNumber()>((max-modenum)/2)+modenum){
						k=ktwo;
					}
					else if (list.get((i*sqrt)+n).getNumber()>modenum){
						k=kthree;
					}
					else if (list.get((i*sqrt)+n).getNumber()==modenum){
						k=kfour;
					}
					else if (list.get((i*sqrt)+n).getNumber()==1){
						k=ksix;
					}
					else{
						k=kfive;
					}
					for (int p=0; p<k;p++){
						c=c.darker();
					}
					//System.out.println(c);
					g.setColor(c);
					g.fillRect((n*squarewidth), i*squareheight, squarewidth, squareheight);
					g.setColor(Color.black);
					if (squarewidth>1){
						g.fillRect((n*squarewidth), i*squareheight, 1, squareheight);
					}
					if (n==(list.size()%sqrt)-1){
						c= Color.white;
						g.setColor(c);
						g.fillRect(((n+1)*squarewidth), i*squareheight, squarewidth*((sqrt+1)*(sqrt-(list.size()%sqrt))), squareheight);
					}
				}
			}
			else if(i%2==0){
			//System.out.println(i);
			for (int n=0; n<sqrt+1; n++){
				//System.out.println(Math.ceil(list.size()/20.0));
				c= colors.get(nameconverter.get(list.get((i*sqrt)+n).getName()));
				//System.out.println(list.get((i*20)+n).getNam());
				if (list.get((i*sqrt)+n).getNumber()==max){
					k=kone;
				}
				else if (list.get((i*sqrt)+n).getNumber()>((max-modenum)/2)+modenum){
					k=ktwo;
				}
				else if (list.get((i*sqrt)+n).getNumber()>modenum){
					k=kthree;
				}
				else if (list.get((i*sqrt)+n).getNumber()==modenum){
					k=kfour;
				}
				else if (list.get((i*sqrt)+n).getNumber()==1){
					k=ksix;
				}
				else{
					k=kfive;
				}
				for (int p=0; p<k;p++){
					c=c.darker();
				}
				//System.out.println(c);
				g.setColor(c);
				g.fillRect((n*squarewidth), i*squareheight, squarewidth, squareheight);
				g.setColor(Color.black);
				if (squarewidth>1){
					g.fillRect((n*squarewidth), i*squareheight, 1, squareheight);
				}
				if (squareheight>1){
					g.fillRect(0, i*squareheight, (int)width, 1);
				}
			}
			}
			else{
				//System.out.println(i);
				for (int n=sqrt-1; n>-1; n--){
					//System.out.println(Math.ceil(list.size()/20.0));
					c= colors.get(nameconverter.get(list.get((i*sqrt)+(sqrt-1)-n).getName()));
					//System.out.println(list.get((i*20)+n).getNam());
					if (list.get((i*sqrt)+n).getNumber()==max){
						k=kone;
					}
					else if (list.get((i*sqrt)+n).getNumber()>((max-modenum)/2)+modenum){
						k=ktwo;
					}
					else if (list.get((i*sqrt)+n).getNumber()>modenum){
						k=kthree;
					}
					else if (list.get((i*sqrt)+n).getNumber()==modenum){
						k=kfour;
					}
					else if (list.get((i*sqrt)+n).getNumber()==1){
						k=ksix;
					}
					else{
						k=kfive;
					}
					for (int p=0; p<k;p++){
						c=c.darker();
					}
					//System.out.println(c);
					g.setColor(c);
					g.fillRect((n*squarewidth), i*squareheight, squarewidth, squareheight);
					g.setColor(Color.black);
					if (squarewidth>1){
						g.fillRect((n*squarewidth), i*squareheight, 1, squareheight);
					}
					if (squareheight>1){
						g.fillRect(0, i*squareheight, (int)width, 1);
					}
				}
			}
		}
		//System.out.println(list.size());
		g.setColor(Color.black);
		g.fillRect((int)width-1, 0, 1, (int)height);
		panel.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		scroll = new JScrollPane (new JLabel(new ImageIcon(image)), 
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.addMouseListener(new MouseWatcher(this));
		g.dispose();
		//System.out.println(scroll.getSize());
		panel.getContentPane().add(scroll);
		JLabel label=new JLabel("Number of bases represented: "+numchars+"  Number of squares on screen: "+list.size()+"   Compression ratio: "+Generator.round((((float)numchars)/list.size()),2)+":1");
		panel.add(label, BorderLayout.PAGE_END);
		panel.pack();
		panel.setExtendedState(JFrame.MAXIMIZED_BOTH);
		//System.out.println(scroll.getSize());
		//System.out.println(label.getSize());
		//System.out.println(panel.getContentPane().getSize());
		panel.setVisible(true);
		for (int i=0; i<list.size();i++){
			if (list.get(i).getNumber()!=1){
				//System.out.println(i+":  "+list.get(i).getNumber());
			}
		}
		try {  
			ImageIO.write(image, "gif", new File("C:/Users/"+System.getProperty("user.name")+"/Desktop/NewGeneImage.gif")); 
		} catch (IOException e) {  
		 e.printStackTrace(); 
		}
	}
	public void mouseClicked(MouseEvent e){
		//System.out.println("Local Clicked")
		JPopupMenu shower=new JPopupMenu();
		JViewport viewport = scroll.getViewport();
		int xdif=viewport.getSize().width-dim.width;
		int ydif=viewport.getSize().height-dim.height;
		xdif=xdif/2;
		ydif=ydif/2;
		//System.out.println("xdif: "+xdif);
		//System.out.println("ydif: "+ydif);
		if (xdif<0){
			xdif=0;
		}
		if (ydif<0){
			ydif=0;
		}
        Point h=viewport.getViewPosition();
		int x=e.getX()+h.x-xdif;
		int y=e.getY()+h.y-ydif;
		int yrow=(int) Math.floor(y/(double)squareheight);
		int xcol=(int) Math.floor(x/(double)squarewidth);
		System.out.println("x: "+x+"   "+e.getX());
		System.out.println("y: "+y+"   "+e.getY());
		System.out.println(squarewidth);
		System.out.println(squareheight);
		System.out.println(xcol);
		System.out.println(yrow);
		//System.out.println(y/(double)squareheight);
		if (yrow<0||xcol<0){
			return;
		}
		//System.out.println("vert: "+vert);
		if (vert==0){
			if (yrow*sqrt+xcol<list.size()){
				System.out.println(yrow*sqrt+xcol);
				shower.add(new JLabel("Base pair is "+basetobase.get(nameconverter.get(list.get(yrow*sqrt+xcol).getName()).charAt(0))+"-"+basetobase.get(nameconverter.get(list.get(yrow*sqrt+xcol).getName()).charAt(1))));
				shower.add(new JLabel("Amount represented: "+list.get(yrow*sqrt+xcol).getNumber()));
				shower.show(e.getComponent(), x+xdif-h.x, y+ydif-h.y);
			}
		}
		else if (vert==-1){
			//System.out.println("snake");
			//System.out.println(list.get(yrow*sqrt+(sqrt-xcol)).getNam());
			System.out.println(sqrt+"   "+(sqrt-xcol));
			System.out.println(("Floor: "+(int)Math.floor(dim.height/(double)squareheight)));
			if (yrow*sqrt+xcol<list.size()){
				if (Math.ceil(list.size() / sqrt)==yrow && yrow%2==1){
					shower.add(new JLabel("Base pair is "+basetobase.get(nameconverter.get(list.get((yrow*list.size()%sqrt+(list.size()%sqrt-xcol))-1).getName()).charAt(0))+"-"+basetobase.get(nameconverter.get(list.get((yrow*list.size()%sqrt+(list.size()%sqrt-xcol))-1).getName()).charAt(1))));
					shower.add(new JLabel("Amount represented: "+list.get((yrow*list.size()%sqrt+(list.size()%sqrt-xcol))-1).getNumber()));
					shower.show(e.getComponent(), x+xdif-h.x, y+ydif-h.y);
				}
				else if (yrow%2==1){
					System.out.println((yrow*sqrt+(sqrt-xcol))-1);
					shower.add(new JLabel("Base pair is "+basetobase.get(nameconverter.get(list.get((yrow*sqrt+(sqrt-xcol))-1).getName()).charAt(0))+"-"+basetobase.get(nameconverter.get(list.get((yrow*sqrt+(sqrt-xcol))-1).getName()).charAt(1))));
					shower.add(new JLabel("Amount represented: "+list.get((yrow*sqrt+(sqrt-xcol))-1).getNumber()));
					shower.show(e.getComponent(), x+xdif-h.x, y+ydif-h.y);
				}
				else{
					System.out.println(yrow*sqrt+xcol);
					shower.add(new JLabel("Base pair is "+basetobase.get(nameconverter.get(list.get(yrow*sqrt+xcol).getName()).charAt(0))+"-"+basetobase.get(nameconverter.get(list.get(yrow*sqrt+xcol).getName()).charAt(1))));
					shower.add(new JLabel("Amount represented: "+list.get(yrow*sqrt+xcol).getNumber()));
					shower.show(e.getComponent(), x+xdif-h.x, y+ydif-h.y);
				}
			}
		}
		else{
			if (xcol*sqrt+yrow<list.size()){
				shower.add(new JLabel("Base pair is "+basetobase.get(nameconverter.get(list.get(yrow*sqrt+xcol).getName()).charAt(0))+"-"+basetobase.get(nameconverter.get(list.get(yrow*sqrt+xcol).getName()).charAt(1))));
				shower.add(new JLabel("Amount represented: "+list.get(xcol*sqrt+yrow).getNumber()));
				shower.show(e.getComponent(), x+xdif-h.x, y+ydif-h.y);
			}
		}
		
	}
	public void pickColors(){
		frame=new JFrame();
		frame.setLayout(new GridBagLayout());
		GridBagConstraints c= new GridBagConstraints();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		acolor= new JTextField(Integer.toHexString(colors.get('a').getRGB()).substring(2));
		ccolor= new JTextField(Integer.toHexString(colors.get('c').getRGB()).substring(2));
		tcolor= new JTextField(Integer.toHexString(colors.get('t').getRGB()).substring(2));
		gcolor= new JTextField(Integer.toHexString(colors.get('g').getRGB()).substring(2));
		JButton updater= new JButton("See selected colors");
		updater.setActionCommand("showchange");
		updater.addActionListener(this);
		JButton submitter= new JButton("Submit changes");
		submitter.setActionCommand("submitcolor");
		submitter.addActionListener(this);
		BufferedImage aai = new BufferedImage(40, 25, BufferedImage.TYPE_INT_RGB);
		BufferedImage aci = new BufferedImage(40, 25, BufferedImage.TYPE_INT_RGB);
		BufferedImage agi = new BufferedImage(40, 25, BufferedImage.TYPE_INT_RGB);
		BufferedImage ati = new BufferedImage(40, 25, BufferedImage.TYPE_INT_RGB);
		BufferedImage cai = new BufferedImage(40, 25, BufferedImage.TYPE_INT_RGB);
		BufferedImage cti = new BufferedImage(40, 25, BufferedImage.TYPE_INT_RGB);
		BufferedImage cgi = new BufferedImage(40, 25, BufferedImage.TYPE_INT_RGB);
		BufferedImage cci = new BufferedImage(40, 25, BufferedImage.TYPE_INT_RGB);
		BufferedImage ggi = new BufferedImage(40, 25, BufferedImage.TYPE_INT_RGB);
		BufferedImage gci = new BufferedImage(40, 25, BufferedImage.TYPE_INT_RGB);
		BufferedImage gti = new BufferedImage(40, 25, BufferedImage.TYPE_INT_RGB);
		BufferedImage gai = new BufferedImage(40, 25, BufferedImage.TYPE_INT_RGB);
		BufferedImage tti = new BufferedImage(40, 25, BufferedImage.TYPE_INT_RGB);
		BufferedImage tai = new BufferedImage(40, 25, BufferedImage.TYPE_INT_RGB);
		BufferedImage tgi = new BufferedImage(40, 25, BufferedImage.TYPE_INT_RGB);
		BufferedImage tci = new BufferedImage(40, 25, BufferedImage.TYPE_INT_RGB);
		Graphics ggg = ggi.getGraphics();
		ggg.setColor(colors.get("gg"));
		ggg.fillRect(0, 0, 40, 25);
		Graphics gtg = gti.getGraphics();
		gtg.setColor(colors.get("gt"));
		gtg.fillRect(0, 0, 40, 25);
		Graphics gag = gai.getGraphics();
		gag.setColor(colors.get("ga"));
		gag.fillRect(0, 0, 40, 25);
		Graphics gcg = gci.getGraphics();
		gcg.setColor(colors.get("gc"));
		gcg.fillRect(0, 0, 40, 25);
		Graphics agg = agi.getGraphics();
		agg.setColor(colors.get("ag"));
		agg.fillRect(0, 0, 40, 25);
		Graphics atg = ati.getGraphics();
		atg.setColor(colors.get("at"));
		atg.fillRect(0, 0, 40, 25);
		Graphics aag = aai.getGraphics();
		aag.setColor(colors.get("aa"));
		aag.fillRect(0, 0, 40, 25);
		Graphics acg = aci.getGraphics();
		acg.setColor(colors.get("ac"));
		acg.fillRect(0, 0, 40, 25);
		Graphics cgg = cgi.getGraphics();
		cgg.setColor(colors.get("cg"));
		cgg.fillRect(0, 0, 40, 25);
		Graphics ctg = cti.getGraphics();
		ctg.setColor(colors.get("ct"));
		ctg.fillRect(0, 0, 40, 25);
		Graphics cag = cai.getGraphics();
		cag.setColor(colors.get("ca"));
		cag.fillRect(0, 0, 40, 25);
		Graphics ccg = cci.getGraphics();
		ccg.setColor(colors.get("cc"));
		ccg.fillRect(0, 0, 40, 25);
		Graphics tgg = tgi.getGraphics();
		tgg.setColor(colors.get("tg"));
		tgg.fillRect(0, 0, 40, 25);
		Graphics tag = tai.getGraphics();
		tag.setColor(colors.get("ta"));
		tag.fillRect(0, 0, 40, 25);
		Graphics ttg = tti.getGraphics();
		ttg.setColor(colors.get("tt"));
		ttg.fillRect(0, 0, 40, 25);
		Graphics tcg = tci.getGraphics();
		tcg.setColor(colors.get("tc"));
		tcg.fillRect(0, 0, 40, 25);
		for (int i=0; i<4; i++){
			for (int n=0; n<4; n++){
				if (i==0){
					
				}
			}
		}
		c.gridx=1;
		c.gridy=0;
		frame.add(new JLabel("A"), c);
		c.gridx--;
		c.gridy++;
		frame.add(acolor, c);
		c.gridx+=2;
		frame.add(new JLabel(new ImageIcon(aai)), c);
		c.gridx=1;
		c.gridy=2;
		frame.add(new JLabel("C"), c);
		c.gridx--;
		c.gridy++;
		frame.add(ccolor, c);
		c.gridx+=2;
		frame.add(new JLabel(new ImageIcon(cci)), c);
		c.gridx=6;
		c.gridy=0;
		frame.add(new JLabel("G"), c);
		c.gridx--;
		c.gridy++;
		frame.add(gcolor, c);
		c.gridx+=2;
		frame.add(new JLabel(new ImageIcon(ggi)), c);
		c.gridx=6;
		c.gridy=2;
		frame.add(new JLabel("T"), c);
		c.gridx--;
		c.gridy++;
		frame.add(tcolor, c);
		c.gridx+=2;
		frame.add(new JLabel(new ImageIcon(tti)), c);
		c.gridy=5;
		c.gridx=3;
		frame.add(updater, c);
		c.gridx++;
		frame.add(submitter, c);
		frame.setSize(600, 300);
		frame.setVisible(true);
		aag.dispose();
		ccg.dispose();
		ttg.dispose();
		ggg.dispose();
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("submitcolor")){
			colors=new HashMap<String, Color>();
			boolean error=false;
			try{
				colors.put("aa", new Color(Integer.parseInt(acolor.getText(), 16)));
				colors.put("cc", new Color(Integer.parseInt(ccolor.getText(), 16)));
				colors.put("gg", new Color(Integer.parseInt(gcolor.getText(), 16)));
				colors.put("tt", new Color(Integer.parseInt(tcolor.getText(), 16)));
			}
			catch(Exception ex){
				error=true;
				JOptionPane.showMessageDialog(frame, "Invalid color code");
				colors=colorstat;
			}
			if (!error){
				frame.dispose();
			}
		}
		else if(e.getActionCommand().equals("showchange")){
			colors=new HashMap<String, Color>();
			boolean error=false;
			try{
				colors.put("aa", new Color(Integer.parseInt(acolor.getText(), 16)));
				colors.put("cc", new Color(Integer.parseInt(ccolor.getText(), 16)));
				colors.put("gg", new Color(Integer.parseInt(gcolor.getText(), 16)));
				colors.put("tt", new Color(Integer.parseInt(tcolor.getText(), 16)));
			}
			catch(Exception ex){
				error=true;
				JOptionPane.showMessageDialog(frame, "Invalid color code");
				colors=colorstat;
			}
			if (!error){
				frame.dispose();
				pickColors();
			}
		}
		
	}
}

