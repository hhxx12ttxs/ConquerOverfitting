import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.KeyStroke;


public class ProJPanel extends JPanel{

	public ProJPanel()
	{
		super();		
		this.setLayout(layout);		
		this.addKeyListener(key);
		addResult();
		addPad();
		addSpecial();
		setEnableButton("10");
	}
	
	private long toDec(String num)
	{
		long n = 0;
		long c = 0;
		if(preSec.equals("Hex"))
		{
			for(int i = 0; i <num.length(); i++)
			{
				switch(num.charAt(num.length()-i-1))
				{
				case 'A' : c = 10;break;
				case 'B' : c = 11;break;
				case 'C' : c = 12;break;
				case 'D' : c = 13;break;
				case 'E' : c = 14;break;
				case 'F' : c = 15;break;
				default : c = num.charAt(num.length()-i-1) - '0';break;
				}
				n += Math.pow(16 , i) * c;
			}
		}else
		if(preSec.equals("Dec"))
		{
			n = Long.parseLong(num);
		}else
		if(preSec.equals("Oct"))
		{
			for(int i = 0; i <num.length(); i++)
			{
				switch(num.charAt(num.length()-i-1))
				{
				case 'A' : c = 10;break;
				case 'B' : c = 11;break;
				case 'C' : c = 12;break;
				case 'D' : c = 13;break;
				case 'E' : c = 14;break;
				case 'F' : c = 15;break;
				default : c = num.charAt(num.length()-i-1) - '0';break;
				}
				n += Math.pow(8 , i) * c;
			}
		}else
		if(preSec.equals("Bin"))
		{
			for(int i = 0; i <num.length(); i++)
			{
				switch(num.charAt(num.length()-i-1))
				{
				case 'A' : c = 10;break;
				case 'B' : c = 11;break;
				case 'C' : c = 12;break;
				case 'D' : c = 13;break;
				case 'E' : c = 14;break;
				case 'F' : c = 15;break;
				default : c = num.charAt(num.length()-i-1) - '0';break;
				}
				n += Math.pow(2 , i) * c;
			}
		}
		return n;
	}
	
	private void addSpecial()
	{
		//add the radioListener
		ActionListener radioListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				String str = resultLabel.getText();
				long num = toDec(str);
				//System.out.println(preSec + " "+num);
				if(hexButton.isSelected())
				{
					str = Long.toHexString(num);
					setEnableButton("16");
					preSec = "Hex";
				}else
				if(decButton.isSelected())
				{
					str = Long.toString(num);
					setEnableButton("10");
					preSec = "Dec";
				}else
				if(octButton.isSelected())
				{
					str = Long.toOctalString(num);
					setEnableButton("8");
					preSec = "Oct";
				}else
				if(binButton.isSelected())
				{
					str = Long.toBinaryString(num);
					setEnableButton("2");
					preSec = "Bin";
				}
				if(str.length() > 20) str = "Too Large!";
				resultLabel.setText(str.toUpperCase());
			}
		};		
		
		numsysPanel = new JPanel();
		
		numsysGroup = new ButtonGroup();
		
		hexButton = new JRadioButton("Hex", false);
		hexButton.addKeyListener(key);
		hexButton.addActionListener(radioListener);
		numsysGroup.add(hexButton);
		numsysPanel.add(hexButton);
		
		decButton = new JRadioButton("Dec", true);
		decButton.addKeyListener(key);
		decButton.addActionListener(radioListener);
		numsysGroup.add(decButton);
		numsysPanel.add(decButton);
		
		octButton = new JRadioButton("Oct", false);
		octButton.addKeyListener(key);
		octButton.addActionListener(radioListener);
		numsysGroup.add(octButton);
		numsysPanel.add(octButton);
		
		binButton = new JRadioButton("Bin", false);
		binButton.addKeyListener(key);
		binButton.addActionListener(radioListener);
		numsysGroup.add(binButton);
		numsysPanel.add(binButton);
		
		numsysPanel.setBorder(BorderFactory.createLineBorder(Color.gray, 1));
		numsysPanel.setLayout(new BoxLayout(numsysPanel, BoxLayout.Y_AXIS));
		
		
		
		this.add(numsysPanel, new GBC(0, 1, 1, 4).setFill(GBC.VERTICAL).setInsets(2, 10, 3, 2).setIpad(2, 0));
		
		//add the hex pad
		numAButton = new JButton("A");
		Action numAAction = new NumAction();
		numAButton.addActionListener(numAAction);
		numAButton.addKeyListener(key);
		imap.put(KeyStroke.getKeyStroke("a"), "A");
		amap.put("A", numAAction);
		this.add(numAButton, new GBC(1, 1, 1, 1).setInsets(2, 3, 3, 2).setIpad(11, 0));
		
		numBButton = new JButton("B");
		Action numBAction = new NumAction();
		numBButton.addActionListener(numBAction);
		numBButton.addKeyListener(key);
		imap.put(KeyStroke.getKeyStroke("b"), "B");
		amap.put("B", numAAction);
		this.add(numBButton, new GBC(1, 2, 1, 1).setInsets(2, 3, 3, 2).setIpad(11, 0));
		
		numCButton = new JButton("C");
		Action numCAction = new NumAction();
		numCButton.addActionListener(numCAction);
		numCButton.addKeyListener(key);
		imap.put(KeyStroke.getKeyStroke("c"), "C");
		amap.put("C", numCAction);
		this.add(numCButton, new GBC(1, 3, 1, 1).setInsets(2, 3, 3, 2).setIpad(11, 0));
		
		numDButton = new JButton("D");
		Action numDAction = new NumAction();
		numDButton.addActionListener(numDAction);
		numDButton.addKeyListener(key);
		imap.put(KeyStroke.getKeyStroke("d"), "D");
		amap.put("D", numDAction);
		this.add(numDButton, new GBC(1, 4, 1, 1).setInsets(2, 3, 3, 2).setIpad(11, 0));
		
		numEButton = new JButton("E");
		Action numEAction = new NumAction();
		numEButton.addActionListener(numEAction);
		numEButton.addKeyListener(key);
		imap.put(KeyStroke.getKeyStroke("e"), "E");
		amap.put("E", numEAction);
		this.add(numEButton, new GBC(1, 5, 1, 1).setInsets(2, 3, 3, 2).setIpad(11, 0));
		
		numFButton = new JButton("F");
		Action numFAction = new NumAction();
		numFButton.addActionListener(numFAction);
		numFButton.addKeyListener(key);
		imap.put(KeyStroke.getKeyStroke("f"), "F");
		amap.put("F", numFAction);
		this.add(numFButton, new GBC(1, 6, 1, 1).setInsets(2, 3, 10, 2).setIpad(11, 0));
		
		
	}
	
	private void setEnableButton(String num)
	{
		int start = Integer.parseInt(num);
		if(start > 0) num0Button.setEnabled(true); else num0Button.setEnabled(false);//to keep the uniform style.
		if(start > 1) num1Button.setEnabled(true); else num1Button.setEnabled(false);//to keep the uniform style.
		if(start > 2) num2Button.setEnabled(true); else num2Button.setEnabled(false);
		if(start > 3) num3Button.setEnabled(true); else num3Button.setEnabled(false);
		if(start > 4) num4Button.setEnabled(true); else num4Button.setEnabled(false);
		if(start > 5) num5Button.setEnabled(true); else num5Button.setEnabled(false);
		if(start > 6) num6Button.setEnabled(true); else num6Button.setEnabled(false);
		if(start > 7) num7Button.setEnabled(true); else num7Button.setEnabled(false);
		if(start > 8) num8Button.setEnabled(true); else num8Button.setEnabled(false);
		if(start > 9) num9Button.setEnabled(true); else num9Button.setEnabled(false);
		if(start > 10) numAButton.setEnabled(true); else numAButton.setEnabled(false);
		if(start > 11) numBButton.setEnabled(true); else numBButton.setEnabled(false);
		if(start > 12) numCButton.setEnabled(true); else numCButton.setEnabled(false);
		if(start > 13) numDButton.setEnabled(true); else numDButton.setEnabled(false);
		if(start > 14) numEButton.setEnabled(true); else numEButton.setEnabled(false);
		if(start > 15) numFButton.setEnabled(true); else numFButton.setEnabled(false);
		if(start > 16) pointButton.setEnabled(true); else pointButton.setEnabled(false);//specially the pointButton should be disable in each case.
	}

	/*
	 * add the pad
	 */
	protected void addPad()
	{
		key= new KeyCmdAction();
		
		this.addKeyListener(key);
		
		imap = this.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		amap = this.getActionMap();		
		
		backButton = new JButton("\u2190");// \u2190 is 'Ąű'
		Action backAction = new CmdAction();
		backButton.addActionListener(backAction);
		backButton.addKeyListener(key);
		this.add(backButton, new GBC(2, 2, 1, 1).setInsets(2, 3, 3, 2).setIpad(5, 0));
		
		ceButton = new JButton("CE");
		ceButton.addActionListener(new CmdAction());
		ceButton.addKeyListener(key);
		this.add(ceButton, new GBC(3,2,1,1).setInsets(2, 3, 3, 2).setIpad(3, 0));
		
		cButton = new JButton("C");
		cButton.addActionListener(new CmdAction());
		cButton.addKeyListener(key);
		this.add(cButton, new GBC(4, 2, 1, 1).setInsets(2, 3, 3, 2).setIpad(10, 0));
		
		absButton = new JButton("\u00B1");
		absButton.addActionListener(new CmdAction());
		absButton.addKeyListener(key);
		this.add(absButton, new GBC(5, 2, 1, 1).setInsets(2, 3, 3, 2).setIpad(10, 0));
		
		sqrtButton = new JButton("\u221A");
		sqrtButton.setEnabled(false);
		sqrtButton.addActionListener(new CmdAction());
		sqrtButton.addKeyListener(key);
		this.add(sqrtButton, new GBC(6, 2, GBC.REMAINDER, 1).setInsets(2, 3, 3, 10).setIpad(3, 0));
		
		num7Button = new JButton("7");
		Action num7Action = new NumAction();
		num7Button.addActionListener(num7Action);
		num7Button.addKeyListener(key);
		imap.put(KeyStroke.getKeyStroke("7"), "7");
		amap.put("7", num7Action);
		this.add(num7Button, new GBC(2, 3, 1, 1).setInsets(2, 3, 3, 2).setIpad(11, 0));
		
		num8Button = new JButton("8");
		Action num8Action = new NumAction();
		num8Button.addActionListener(num8Action);
		num8Button.addKeyListener(key);
		imap.put(KeyStroke.getKeyStroke("8"), "8");
		amap.put("8", num8Action);
		this.add(num8Button, new GBC(3, 3, 1, 1).setInsets(2, 3, 3, 2).setIpad(11, 0));
		
		num9Button = new JButton("9");
		Action num9Action = new NumAction();
		num9Button.addActionListener(num9Action);
		num9Button.addKeyListener(key);
		imap.put(KeyStroke.getKeyStroke("9"), "9");
		amap.put("9", num9Action);
		this.add(num9Button, new GBC(4, 3, 1, 1).setInsets(2, 3, 3, 2).setIpad(11, 0));
		
		divButton = new JButton("/");
		divButton.addActionListener(new CmdAction());
		divButton.addKeyListener(key);
		this.add(divButton, new GBC(5, 3, 1, 1).setInsets(2, 3, 3, 2).setIpad(14, 0));
		
		div100Button = new JButton("%");
		div100Button.setEnabled(false);
		div100Button.addActionListener(new CmdAction());
		div100Button.addKeyListener(key);
		this.add(div100Button, new GBC(6, 3, GBC.REMAINDER, 1).setInsets(2, 3, 3, 10).setIpad(7, 0));
		
		num4Button = new JButton("4");
		Action num4Action = new NumAction();
		num4Button.addActionListener(num4Action);
		num4Button.addKeyListener(key);
		imap.put(KeyStroke.getKeyStroke("4"), "4");
		amap.put("4", num4Action);
		this.add(num4Button, new GBC(2, 4, 1, 1).setInsets(2, 3, 3, 2).setIpad(11, 0));
		
		num5Button = new JButton("5");
		Action num5Action = new NumAction();
		num5Button.addActionListener(num5Action);
		num5Button.addKeyListener(key);
		imap.put(KeyStroke.getKeyStroke("5"), "5");
		amap.put("5", num5Action);
		this.add(num5Button, new GBC(3, 4, 1, 1).setInsets(2, 3, 3, 2).setIpad(11, 0));
		
		num6Button = new JButton("6");
		Action num6Action = new NumAction();
		num6Button.addActionListener(num6Action);
		num6Button.addKeyListener(key);
		imap.put(KeyStroke.getKeyStroke("6"), "6");
		amap.put("6", num6Action);
		this.add(num6Button, new GBC(4, 4, 1, 1).setInsets(2, 3, 3, 2).setIpad(11, 0));
		
		multiButton = new JButton("*");
		multiButton.addActionListener(new CmdAction());
		multiButton.addKeyListener(key);
		this.add(multiButton, new GBC(5, 4, 1, 1).setInsets(2, 3, 3, 2).setIpad(12, 0));
		
		reButton = new JButton("1/x");
		reButton.setEnabled(false);
		reButton.addActionListener(new CmdAction());
		reButton.addKeyListener(key);
		this.add(reButton, new GBC(6, 4, GBC.REMAINDER, 1).setInsets(2, 3, 3, 10).setIpad(0, 0));
		
		num1Button = new JButton("1");
		Action num1Action = new NumAction();
		num1Button.addActionListener(num1Action);
		num1Button.addKeyListener(key);
		imap.put(KeyStroke.getKeyStroke("1"), "1");
		amap.put("1", num1Action);
		this.add(num1Button, new GBC(2, 5, 1, 1).setInsets(2, 3, 3, 2).setIpad(8, 0));
		
		num2Button = new JButton("2");
		Action num2Action = new NumAction();
		num2Button.addActionListener(num2Action);
		num2Button.addKeyListener(key);
		imap.put(KeyStroke.getKeyStroke("2"), "2");
		amap.put("2", num2Action);
		this.add(num2Button, new GBC(3, 5, 1, 1).setInsets(2, 3, 3, 2).setIpad(9, 0));
		
		num3Button = new JButton("3");
		Action num3Action = new NumAction();
		num3Button.addActionListener(num3Action);
		num3Button.addKeyListener(key);
		imap.put(KeyStroke.getKeyStroke("3"), "3");
		amap.put("3", num3Action);
		this.add(num3Button, new GBC(4, 5, 1, 1).setInsets(2, 3, 3, 2).setIpad(8, 0));
		
		minusButton = new JButton("-");
		minusButton.addActionListener(new CmdAction());
		minusButton.addKeyListener(key);
		this.add(minusButton, new GBC(5, 5, 1, 1).setInsets(2, 3, 3, 2).setIpad(11, 0));
		
		equalButton = new JButton("=");
		equalButton.addActionListener(new CmdAction());
		equalButton.addKeyListener(key);
		this.add(equalButton, new GBC(6, 5, GBC.REMAINDER, GBC.REMAINDER).setFill(GBC.VERTICAL).setInsets(2, 3, 10, 10).setIpad(10, 0));
		
		num0Button = new JButton("0");
		Action num0Action = new NumAction();
		num0Button.addActionListener(num0Action);
		num0Button.addKeyListener(key);
		imap.put(KeyStroke.getKeyStroke("0"), "0");
		amap.put("0", num0Action);
		this.add(num0Button, new GBC(2, 6, 2, 1).setFill(GBC.HORIZONTAL).setInsets(2, 3, 10, 2));
		
		pointButton = new JButton(".");
		pointButton.addActionListener(new CmdAction());
		pointButton.addKeyListener(key);
		this.add(pointButton, new GBC(4, 6, 1, 1).setIpad(12, 0).setInsets(2, 3, 10, 2));
		
		addButton = new JButton("+");
		addButton.addActionListener(new CmdAction());
		pointButton.addKeyListener(key);
		this.add(addButton, new GBC(5, 6, 1, 1).setInsets(2, 3, 10, 2).setIpad(8, 0));
		
		//add the MS ...
		JButton mcButton = new JButton("MC");
		mcButton.setEnabled(false);
		this.add(mcButton, new GBC(2, 1, 1, 1).setInsets(2, 3, 3, 2).setIpad(0, 0));
		
		JButton mrButton = new JButton("MR");
		mrButton.setEnabled(false);
		this.add(mrButton, new GBC(3,1,1,1).setInsets(2, 3, 3, 2).setIpad(0, 0));
		
		JButton msButton = new JButton("MS");
		msButton.setEnabled(false);
		this.add(msButton, new GBC(4, 1, 1, 1).setInsets(2, 3, 3, 2).setIpad(0, 0));
		
		JButton maddButton = new JButton("M+");
		maddButton.setEnabled(false);
		this.add(maddButton, new GBC(5, 1, 1, 1).setInsets(2, 3, 3, 2).setIpad(0, 0));
		
		JButton mminusButton = new JButton("M-");
		mminusButton.setEnabled(false);
		this.add(mminusButton, new GBC(6, 1, GBC.REMAINDER, 1).setInsets(2, 3, 3, 10).setIpad(1, 0));
		
	}
	
	/*
	 * add the result label
	 */
	protected void addResult()
	{
		//although the titleLabel is initialization, the titleLabel not use anywhere in the ProJPanel.
		titleLabel = new JLabel(" ");
		titleLabel.setFont(new Font("Serif", Font.PLAIN, 12));
		titleLabel.setHorizontalAlignment(JLabel.RIGHT);
		
		resultLabel = new JLabel("0");
		resultLabel.setFont(new Font("Serif", Font.BOLD, 18));
		resultLabel.setHorizontalAlignment(JLabel.RIGHT);
		
		JButton headButton = new JButton();
		headButton.setEnabled(false);
		headButton.setLayout(new BorderLayout());
		
		//headButton.add(titleLabel, BorderLayout.NORTH);
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
			num1 = toDec(str);
			titleLabel.setText(str + "/");
			lastCmd = "/";
			str = "0";
			
		}
		if(cmd.equals("%"))
		{
			num1 = toDec(str);
			titleLabel.setText(str);
			result = num1/100;
			str = Double.toString(result);
			finish = true;
			clear();
		}
		if(cmd.equals("*"))
		{
			finish = false;
			num1 = toDec(str);
			titleLabel.setText(str + "*");
			lastCmd = "*";
			str = "0";
		}
		if(cmd.equals("1/x"))
		{
			num1 = toDec(str);
			titleLabel.setText("1/"+num1);
			num1 = 1 / num1;
			str = Double.toString(num1);
			finish = true;
			clear();
		}
		if(cmd.equals("-"))
		{
			finish = false;
			num1 = toDec(str);
			titleLabel.setText(str + "-");
			lastCmd = "-";
			str = "0";
		}
		if(cmd.equals("="))
		{	
			if(!finish)
			{
				num2 = toDec(str);
			}else
			{
				num1 = toDec(str);
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
				if(hexButton.isSelected()) str = Long.toHexString(tmp);
				if(decButton.isSelected()) str = tmp + "";
				if(octButton.isSelected()) str = Long.toOctalString(tmp);
				if(binButton.isSelected()) str = Long.toBinaryString(tmp);
			}
			
		}
		if(cmd.equals("+"))
		{
			finish = false;
			num1 = toDec(str);
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
			str += ".";
			
		}
		
		resultLabel.setText(str);
			
	}
	
	private void clear()
	{
		lastCmd = "";
		num1 = num2 = result = 0;
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
		decButton.setSelected(true);
		
		titleLabel.setText(" ");
		resultLabel.setText("0");
		lastCmd = "";
		finish = false;
		
	}	
	
	public String echo()
	{
		return resultLabel.getText();
	}
	
	private String preSec = "Dec";
	private KeyCmdAction key;
	private InputMap imap;
	private ActionMap amap;
	private long num1, num2,result;
	private GridBagLayout layout = new GridBagLayout();
	private JLabel titleLabel, resultLabel;
	private String lastCmd = "";
	private boolean finish = false;
	private JRadioButton hexButton, decButton, octButton, binButton;
	private ButtonGroup numsysGroup;
	private JPanel numsysPanel;
	private final boolean isDouble = false;
	private JButton numAButton, numBButton, numCButton, numDButton, numEButton, numFButton;
	private JButton backButton, ceButton, cButton, absButton, sqrtButton, num7Button, num8Button, num9Button, num4Button, num5Button, num6Button, num1Button, num2Button, num3Button, num0Button, divButton, div100Button, multiButton, reButton, minusButton, equalButton, pointButton, addButton;
	

}
