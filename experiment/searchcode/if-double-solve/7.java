import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

public class StdJPanel extends JPanel{

	public StdJPanel()
	{
		super();
		
		this.setLayout(layout);
		this.addKeyListener(key);
		addResult();
		addPad();
	}
		
	/*
	 * add the pad
	 */
	private void addPad()
	{
		key= new KeyCmdAction();
		
		this.addKeyListener(key);
		
		InputMap imap = this.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		ActionMap amap = this.getActionMap();		
		
		backButton = new JButton("\u2190");// \u2190 is 'Ąű'
		Action backAction = new CmdAction();
		backButton.addActionListener(backAction);
		backButton.addKeyListener(key);
		this.add(backButton, new GBC(0, 1, 1, 1).setInsets(2, 10, 3, 2).setIpad(2, 0));
		
		ceButton = new JButton("CE");
		ceButton.addActionListener(new CmdAction());
		ceButton.addKeyListener(key);
		this.add(ceButton, new GBC(1,1,1,1).setInsets(2, 3, 3, 2).setIpad(1, 0));
		
		cButton = new JButton("C");
		cButton.addActionListener(new CmdAction());
		cButton.addKeyListener(key);
		this.add(cButton, new GBC(2, 1, 1, 1).setInsets(2, 3, 3, 2).setIpad(7, 0));
		
		absButton = new JButton("\u00B1");
		absButton.addActionListener(new CmdAction());
		absButton.addKeyListener(key);
		this.add(absButton, new GBC(3, 1, 1, 1).setInsets(2, 3, 3, 2).setIpad(7, 0));
		
		sqrtButton = new JButton("\u221A");
		sqrtButton.addActionListener(new CmdAction());
		sqrtButton.addKeyListener(key);
		this.add(sqrtButton, new GBC(4, 1, GBC.REMAINDER, 1).setInsets(2, 3, 3, 10).setIpad(3, 0));
		
		num7Button = new JButton("7");
		Action num7Action = new NumAction();
		num7Button.addActionListener(num7Action);
		num7Button.addKeyListener(key);
		imap.put(KeyStroke.getKeyStroke("7"), "7");
		amap.put("7", num7Action);
		this.add(num7Button, new GBC(0, 2, 1, 1).setInsets(2, 10, 3, 2).setIpad(8, 0));
		
		num8Button = new JButton("8");
		Action num8Action = new NumAction();
		num8Button.addActionListener(num8Action);
		num8Button.addKeyListener(key);
		imap.put(KeyStroke.getKeyStroke("8"), "8");
		amap.put("8", num8Action);
		this.add(num8Button, new GBC(1, 2, 1, 1).setInsets(2, 3, 3, 2).setIpad(9, 0));
		
		num9Button = new JButton("9");
		Action num9Action = new NumAction();
		num9Button.addActionListener(num9Action);
		num9Button.addKeyListener(key);
		imap.put(KeyStroke.getKeyStroke("9"), "9");
		amap.put("9", num9Action);
		this.add(num9Button, new GBC(2, 2, 1, 1).setInsets(2, 3, 3, 2).setIpad(8, 0));
		
		divButton = new JButton("/");
		divButton.addActionListener(new CmdAction());
		divButton.addKeyListener(key);
		this.add(divButton, new GBC(3, 2, 1, 1).setInsets(2, 3, 3, 2).setIpad(11, 0));
		
		div100Button = new JButton("%");
		div100Button.addActionListener(new CmdAction());
		div100Button.addKeyListener(key);
		this.add(div100Button, new GBC(4, 2, GBC.REMAINDER, 1).setInsets(2, 3, 3, 10).setIpad(8, 0));
		
		num4Button = new JButton("4");
		Action num4Action = new NumAction();
		num4Button.addActionListener(num4Action);
		num4Button.addKeyListener(key);
		imap.put(KeyStroke.getKeyStroke("4"), "4");
		amap.put("4", num4Action);
		this.add(num4Button, new GBC(0, 3, 1, 1).setInsets(2, 10, 3, 2).setIpad(8, 0));
		
		num5Button = new JButton("5");
		Action num5Action = new NumAction();
		num5Button.addActionListener(num5Action);
		num5Button.addKeyListener(key);
		imap.put(KeyStroke.getKeyStroke("5"), "5");
		amap.put("5", num5Action);
		this.add(num5Button, new GBC(1, 3, 1, 1).setInsets(2, 3, 3, 2).setIpad(9, 0));
		
		num6Button = new JButton("6");
		Action num6Action = new NumAction();
		num6Button.addActionListener(num6Action);
		num6Button.addKeyListener(key);
		imap.put(KeyStroke.getKeyStroke("6"), "6");
		amap.put("6", num6Action);
		this.add(num6Button, new GBC(2, 3, 1, 1).setInsets(2, 3, 3, 2).setIpad(8, 0));
		
		multiButton = new JButton("*");
		multiButton.addActionListener(new CmdAction());
		multiButton.addKeyListener(key);
		this.add(multiButton, new GBC(3, 3, 1, 1).setInsets(2, 3, 3, 2).setIpad(10, 0));
		
		reButton = new JButton("1/x");
		reButton.addActionListener(new CmdAction());
		reButton.addKeyListener(key);
		this.add(reButton, new GBC(4, 3, GBC.REMAINDER, 1).setInsets(2, 3, 3, 10).setIpad(0, 0));
		
		num1Button = new JButton("1");
		Action num1Action = new NumAction();
		num1Button.addActionListener(num1Action);
		num1Button.addKeyListener(key);
		imap.put(KeyStroke.getKeyStroke("1"), "1");
		amap.put("1", num1Action);
		this.add(num1Button, new GBC(0, 4, 1, 1).setInsets(2, 10, 3, 2).setIpad(8, 0));
		
		num2Button = new JButton("2");
		Action num2Action = new NumAction();
		num2Button.addActionListener(num2Action);
		num2Button.addKeyListener(key);
		imap.put(KeyStroke.getKeyStroke("2"), "2");
		amap.put("2", num2Action);
		this.add(num2Button, new GBC(1, 4, 1, 1).setInsets(2, 3, 3, 2).setIpad(9, 0));
		
		num3Button = new JButton("3");
		Action num3Action = new NumAction();
		num3Button.addActionListener(num3Action);
		num3Button.addKeyListener(key);
		imap.put(KeyStroke.getKeyStroke("3"), "3");
		amap.put("3", num3Action);
		this.add(num3Button, new GBC(2, 4, 1, 1).setInsets(2, 3, 3, 2).setIpad(8, 0));
		
		minusButton = new JButton("-");
		minusButton.addActionListener(new CmdAction());
		minusButton.addKeyListener(key);
		this.add(minusButton, new GBC(3, 4, 1, 1).setInsets(2, 3, 3, 2).setIpad(11, 0));
		
		equalButton = new JButton("=");
		equalButton.addActionListener(new CmdAction());
		equalButton.addKeyListener(key);
		this.add(equalButton, new GBC(4, 4, GBC.REMAINDER, GBC.REMAINDER).setFill(GBC.VERTICAL).setInsets(2, 3, 10, 10).setIpad(10, 0));
		
		num0Button = new JButton("0");
		Action num0Action = new NumAction();
		num0Button.addActionListener(num0Action);
		num0Button.addKeyListener(key);
		imap.put(KeyStroke.getKeyStroke("0"), "0");
		amap.put("0", num0Action);
		this.add(num0Button, new GBC(0, 5, 2, 1).setFill(GBC.HORIZONTAL).setInsets(2, 10, 10, 2));
		
		pointButton = new JButton(".");
		pointButton.addActionListener(new CmdAction());
		pointButton.addKeyListener(key);
		this.add(pointButton, new GBC(2, 5, 1, 1).setIpad(12, 0).setInsets(2, 3, 10, 2));
		
		addButton = new JButton("+");
		addButton.addActionListener(new CmdAction());
		pointButton.addKeyListener(key);
		this.add(addButton, new GBC(3, 5, 1, 1).setInsets(2, 3, 10, 2).setIpad(8, 0));
		
	}

	/*
	 * add the result label
	 */
	private void addResult()
	{
		titleLabel = new JLabel(" ");
		titleLabel.setFont(new Font("Serif", Font.PLAIN, 12));
		titleLabel.setHorizontalAlignment(JLabel.RIGHT);
		
		resultLabel = new JLabel("0");
		resultLabel.setFont(new Font("Serif", Font.BOLD, 18));
		resultLabel.setHorizontalAlignment(JLabel.RIGHT);
		
		JButton headButton = new JButton();
		headButton.setEnabled(false);
		headButton.setLayout(new BorderLayout());
		
		headButton.add(titleLabel, BorderLayout.NORTH);
		headButton.add(resultLabel);
		
		this.add(headButton,new GBC(0, 0, GBC.REMAINDER, 1).setInsets(10, 10, 3, 11).setFill(GBC.HORIZONTAL).setIpad(0, 30));
	}
	
	
	/*
	 * add the Numbers Listener
	 */
	private class NumAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent event)
		{
			String num = event.getActionCommand();
			String str = resultLabel.getText();
			solvenum(num, str);				
		}
	}

	/*
	 * add the cmd Listener
	 */
	private class CmdAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent event)
		{
			String cmd = event.getActionCommand();
			String str = resultLabel.getText();
			solve(cmd, str);
			
		}
	}
	private class KeyCmdAction extends KeyAdapter
	{
		public void keyPressed(KeyEvent event)
		{
			String cmd = "+";
			//System.out.println(event.getKeyCode());
			
			if(event.isShiftDown()) 
			{
				switch(event.getKeyCode())
				{
				case 56 : cmd = "*";break;
				case 61 : cmd = "+";break;
				case 53 : cmd = "%";break;
				default : return ;
				}
			}else
			switch(event.getKeyCode())
			{
			case KeyEvent.VK_BACK_SPACE : cmd = "\u2190";break;
			case KeyEvent.VK_ESCAPE : cmd = "CE";break;
			case KeyEvent.VK_DIVIDE : cmd = "/";break;
			case 47 : cmd = "/";break;
			case KeyEvent.VK_MULTIPLY : cmd = "*";break;
			case KeyEvent.VK_MINUS : cmd = "-";break;
			case KeyEvent.VK_ADD : cmd = "+";break;
			case KeyEvent.VK_COMMA : cmd = ".";break;
			case KeyEvent.VK_ENTER : cmd = "=";break;
			default : return;
			}
			String str = resultLabel.getText();
			solve(cmd, str);
		}
	}
	
	
	private void solvenum(String num, String str)
	{
		if(finish)
		{
			titleLabel.setText(" ");
			clear();
		}
		if(str.equals("0") || finish )
			{
				str = "";
				finish = false;
			}
		if(str.length() < 16 || (isDouble && str.length() < 17)) str += num;
		resultLabel.setText(str);
	}
	
	private void solve(String cmd, String str)
	{
		if(cmd.equals("\u2190"))
		{
			if(str.equals("0")) 
			{
				ring("ding.wav");
				return;
			}
			if(str.charAt(str.length()-1) == '.')
				isDouble = false;
			str = str.substring(0, str.length()-1);
			if(str.length() == 0)
				str = "0";
		}
		if(cmd.equals("CE"))
		{
			clear();
			finish = true;
			titleLabel.setText(" ");
			str = "0";
		}
		if(cmd.equals("C"))
		{
			if(str.equals("0")) 
			{
				ring("ding.wav");
				return;
			}
			str = "0";
		}
		if(cmd.equals("\u00B1"))
		{
			if(str.equals("0")) 
			{
			//	ring();
				return;
			}
			if(str.charAt(0) != '-')
			{
				String tmp = "-";
				str = tmp + str;
			}else
			{
				str = str.substring(1, str.length());
			}
		}
		if(cmd.equals("\u221A"))
		{
			if(str.equals("0")) 
			{
				ring("ding.wav");
				return;
			}
			double res = Double.parseDouble(str);
			titleLabel.setText("sqrt(" + res + ")");
			res = Math.sqrt(res);
			if(Double.isNaN(res) || Double.isInfinite(res)) ring("ding.wav");
			str = Double.toString(res);
			finish = true;
			clear();
		}
		if(cmd.equals("/"))
		{
			num1 = Double.parseDouble(str);
			titleLabel.setText(str + "/");
			lastCmd = "/";
			str = "0";
			
		}
		if(cmd.equals("%"))
		{
			num1 = Double.parseDouble(str);
			titleLabel.setText(str);
			result = num1/100;
			str = Double.toString(result);
			finish = true;
			clear();
		}
		if(cmd.equals("*"))
		{
			finish = false;
			num1 = Double.parseDouble(str);
			titleLabel.setText(str + "*");
			lastCmd = "*";
			str = "0";
		}
		if(cmd.equals("1/x"))
		{
			num1 = Double.parseDouble(str);
			titleLabel.setText("1/"+num1);
			num1 = 1 / num1;
			str = Double.toString(num1);
			finish = true;
			clear();
		}
		if(cmd.equals("-"))
		{
			finish = false;
			num1 = Double.parseDouble(str);
			titleLabel.setText(str + "-");
			lastCmd = "-";
			str = "0";
		}
		if(cmd.equals("="))
		{	
			if(!finish)
			{
				num2 = Double.parseDouble(str);
			}else
			{
				num1 = Double.parseDouble(str);
			}
			finish = true;
			titleLabel.setText(" ");
			
			if(lastCmd.length() == 0) return ;
			switch (lastCmd.charAt(0))
			{
			case '/': result = num1 / num2;break;
			case '*': result = num1 * num2;break;
			case '-': result = num1 - num2;break;
			case '+': result = num1 + num2;break;
			}
			if(isDouble || (num1 % num2 != 0 && lastCmd.charAt(0) == '/'))
			{
				if(Double.isNaN(result) || Double.isInfinite(result)) ring("ding.wav");
				str = result+"";
			}else
			{
				long tmp = (long) result;
				str = tmp + "";
			}
			
		}
		if(cmd.equals("+"))
		{
			finish = false;
			num1 = Double.parseDouble(str);
			titleLabel.setText(str + "+");
			lastCmd = "+";
			str = "0";
		}
		if(cmd.equals("."))
		{
			finish = false;
			if(isDouble)
			{
				ring("ding.wav");
				return;
			}
			isDouble = true;
			str += ".";
			
		}
		
		resultLabel.setText(str);
			
	}
	
	private void clear()
	{
		isDouble = false;
		lastCmd = "";
		num1 = num2 = result = 0.0;
	}
	
	private void ring(String media_path)
	{
		try{
			//media_path = Main.class.getResource("ring.wav");
			//File media_file=new File(media_path);
			URL media_url=Main.class.getResource("ding.wav");
    		AudioClip media=Applet.newAudioClip(media_url);
    		media.play();
    	}catch(Exception e){ 
           System.out.println("error");
    	}
		return;
	}
	
	public void init()
	{

		num1 = num2 = result = 0;
		titleLabel.setText(" ");
		resultLabel.setText("0");
		lastCmd = "";
		isDouble = finish = false;
		
	}	
	public String echo()
	{
		return resultLabel.getText();
	}
	
	private KeyCmdAction key;
	private double num1, num2,result;
	private GridBagLayout layout = new GridBagLayout();
	private JLabel titleLabel, resultLabel;
	private String lastCmd = "";
	private boolean isDouble = false , finish = false;
	private JButton backButton, ceButton, cButton, absButton, sqrtButton, num7Button, num8Button, num9Button, num4Button, num5Button, num6Button, num1Button, num2Button, num3Button, num0Button, divButton, div100Button, multiButton, reButton, minusButton, equalButton, pointButton, addButton;
}



