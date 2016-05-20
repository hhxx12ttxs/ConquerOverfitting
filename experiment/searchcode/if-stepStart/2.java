package gui;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.undo.UndoManager;

import view.OutputModule;
import view.Virtual;
import view.virtualdriver;

import exceptions.IdentifierAlreadyInUseException;
import exceptions.InvalidOutputModuleException;
import exceptions.InvalidStatementException;

import builder.DancerProgram;
import builder.InstructorProgram;

/**
 * @author - Kashif Qureshi
 */

public class GUI extends JFrame implements ActionListener {
	private ArrayList nested;
	private static int x = 0;
	private boolean abc = true;
	private static String[] colors2;
	private static boolean helper = false;
	private static int stepC;
	private String stepper;
	private static Container w2;
	private static JFrame ok;
	private static JTextArea masterBox;
	private static JTextArea dancerBox;
	private static JTextArea lines;
	private static JTextArea lines2;
	private static Container window;
	private static String cmd;
	private static String virtual;
	private JFileChooser fc = new JFileChooser();
	private UndoManager undoManager = new UndoManager();
	private static ArrayList masterTree = new ArrayList();
	private static ArrayList dancerTree = new ArrayList();
	private static ArrayList colors = new ArrayList();
	private JButton undoB;
	private JButton redoB;
	private JButton loadWorld;
	private static JTextField masterLines;
	private static JTextField dancerLines;
	private static JComboBox master;
	private static JComboBox dancer;
	private static DancerProgram deepee;
	private static InstructorProgram eyepee;
	private String[] masterCommands = {" instructor", " black     ", " blue      ", " cyan      ", " dark gray ", " gray      ", " light gray", " green     ", " magenta   ", " orange    ", " pink      ", " red       ", " white     ", " yellow    ", " delete line", " clear"};
	private String[] dancerCommands = {" dancer", " define                     ", " repeat x times             ", " if                         ", " while                      ", " set                        ", " perform                    ", " go_backward                ", " go_forward                 ", " rotate_left                ", " rotate_right               ", " delete line", " clear"};
	private static String[] functions = new String[100];
	private static int functionCounter = 0;
	private static String assistance;
	private static int loc;
	private static int aLine;
	private static int staticMTL;
	private static int staticDTL;
	private static int lastLine; 
	private int mTotalLines = 0;
	private static int dTotalLines = 0; 
	private boolean Master = true;
	private boolean undo = false;
	private boolean redo = false;
	private boolean isCommand = false;
	private boolean masterNum;
	private boolean dancerNum;
	private boolean stepStart = true;
	private boolean success = false;
	private static boolean assist = false;
	private static String micro;
	final static boolean shouldFill = true;
    final static boolean shouldWeightX = true;
    final static boolean RIGHT_TO_LEFT = false;

    public void actionPerformed(ActionEvent e)
    {
    	String event = e.getActionCommand();
    	
    	if(event.compareTo("Load Virtual World") == 0)
    	{	
    		int returnVal = fc.showOpenDialog(window);
    		if(returnVal == JFileChooser.APPROVE_OPTION)
    		{
    			File file = fc.getSelectedFile();
    			String fn = file.getName(); 
    			String dir = file.getPath();
    			System.out.println("file " + file);
    			virtual = fn;
    			Virtual v = new Virtual(fn);
    		}
    	}
    	
    	if(event.compareTo("Save instructor") == 0)
    	{
    		String masterFile = masterBox.getText();           
            int returnVal = fc.showSaveDialog(window);
            if (returnVal == JFileChooser.APPROVE_OPTION) 
            {
                File file = fc.getSelectedFile();
                try 
                {
					Writer out = new OutputStreamWriter(new FileOutputStream(file));
					String temp = ".master";
					temp = temp.concat(masterFile);
					out.write(temp);
					out.close();
				} catch (FileNotFoundException e1) 
				{
					System.out.println("ffff");
					e1.printStackTrace();
				} catch (IOException a) 
				{
					a.printStackTrace();
				}
                System.out.println("Saving: " + file.getName() + ". \n" );
            }
    	}
    	
    	if(event.compareTo("Save dancer") == 0)
    	{
            int returnVal = fc.showSaveDialog(window);
            String dancerFile = dancerBox.getText();
            if (returnVal == JFileChooser.APPROVE_OPTION) 
            {
                File file = fc.getSelectedFile();
                try 
                {
					Writer out = new OutputStreamWriter(new FileOutputStream(file));
					String temp = new String(".dancer");
					temp = temp.concat(dancerFile);
					System.out.println("temp \n" + temp);
					out.write(temp);
					out.close();
				} catch (FileNotFoundException e1) 
				{
					System.out.println("ffff");
					e1.printStackTrace();
				} catch (IOException a) 
				{
					a.printStackTrace();
				}
                System.out.println("Saving: " + file.getName() + ". \n" );
            } 
    	}
    	
    	if(event.compareTo("Load dancer") == 0)
    	{
    		int returnVal = fc.showOpenDialog(window);
    		if(returnVal == JFileChooser.APPROVE_OPTION)
    		{
    			File file = fc.getSelectedFile();
    			String fn = file.getName();
    			String ext = fn.substring(fn.length()-4, fn.length());
    			System.out.println("Extension: " + ext);
    			if(ext.compareTo(".txt") != 0)
    			{
    				String[] err = {"Only text files allowed"};
    				ErrorMsg.main(err);
    			}
    			else
    			{
    				String cmd;
    				String macro;
    				int macros = 0;
    				try 
    				{
						Scanner sc = new Scanner(new FileInputStream(file));
						int counter = 0;
						String lines = new String("");
						while(sc.hasNextLine())
						{
							String text = sc.nextLine();
							if(counter == 0)
							{
								String checkFileType = text.substring(0, 7);
								if(checkFileType.compareTo(".dancer") != 0)
								{
									success = false;
									break;
								}
								success = true;
								lines = text.substring(7, text.length());
								cmd = text.substring(7, 14);
								if(cmd.compareTo(" define") == 0)
								{
									macro = text.substring(14, text.length());
									macro = macro.trim();
									functions[macros] = macro;
									macros++;
								}
								
								lines = lines.concat("\n");
							}
							else
							{
								System.out.println("text " + text);
								cmd = text.substring(0, 4);
								if(cmd.compareTo(" def") == 0)
								{
									macro = text.substring(8, text.length());
									macro = macro.trim();
									functions[macros] = macro;
									macros++;
								}
								lines = lines.concat(text);
								lines = lines.concat("\n");
							}
							
							counter++;
						}
						if(success)
						{
							dTotalLines = counter;
							staticDTL = counter;
							Integer lineNum = new Integer(dTotalLines + 1);
							dancerLines.setText(lineNum.toString());
							dancerBox.setText(lines);
						}
						if(!success)
						{
							String[] err = {"Not a valid dancer file"};
							ErrorMsg.main(err);
						}
						
					} catch (FileNotFoundException e1) 
					{
						e1.printStackTrace();
					}
    			}
    		}
    	}
    	
    	if(event.compareTo("Load instructor") == 0)
    	{
    		System.out.println("Load instructor");
    		int returnVal = fc.showOpenDialog(window);
    		if(returnVal == JFileChooser.APPROVE_OPTION)
    		{
    			File file = fc.getSelectedFile();
    			String fn = file.getName();
    			String ext = fn.substring(fn.length()-4, fn.length());
    			System.out.println("Extension: " + ext);
    			if(ext.compareTo(".txt") != 0)
    			{
    				String[] err = {"Only text files allowed"};
    				ErrorMsg.main(err);
    			}
    			else
    			{
    				try 
    				{
						Scanner sc = new Scanner(new FileInputStream(file));
						int counter = 0;
						String lines = new String("");
						while(sc.hasNextLine())
						{
							String text = sc.nextLine();
							if(counter == 0)
							{
								String checkFileType = text.substring(0, 7);
								if(checkFileType.compareTo(".master") != 0)
								{
									success = false;
									break;
								}
								success = true;
								lines = text.substring(7, text.length());
								lines = lines.concat("\n");
							}
							else
							{
								lines = lines.concat(text);
								lines = lines.concat("\n");
							}
							
							counter++;
						}
						
						if(success)
						{
							System.out.println("Test C " + lines);
							mTotalLines = counter;
							staticMTL = counter;
							Integer lineNum = new Integer(mTotalLines+1);
							masterLines.setText(lineNum.toString());
							masterBox.setText(lines);
						}
						
						if(!success)
						{
							String[] err = {"Not a valid instructor file"};
							ErrorMsg.main(err);
						}
						
					} catch (FileNotFoundException e1) 
					{
						System.out.println("File not found");
						e1.printStackTrace();
					}
    			}
    		}
    	}
    	
    	if(event.compareTo("Undo") == 0)
    	{
    		redo = true;
    		redoB.setEnabled(true);
    		undoB.setEnabled(false);
    		System.out.println("Undo");
    	}
    	
    	if(event.compareTo("Redo") == 0)
    	{
    		undo = true;
    		undoB.setEnabled(true);
    		redoB.setEnabled(false);
    		System.out.println("Redo");
    	}
    	
    	if(event.compareTo("Step") == 0)
    	{
    		
    		System.out.println("Step");
    		
			Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(new Color(250, 218, 94));
			Object highlight;
			

			if(stepStart)
			{
				GUI.makeList();
			//	if(dancerTree.)
	    		int totalLines = masterBox.getLineCount();
	    		int counter = 0;
	    		int startOffset;
	    		
	    	    final DancerProgram dp = new DancerProgram();
	    	    final InstructorProgram ip = new InstructorProgram();
	    	    ArrayList<String> dancerProgram = new ArrayList<String>(dancerTree);
	    	    ArrayList<String> instructorProgram = new ArrayList<String>(masterTree);
	    	    deepee = dp;
	    	    eyepee = ip;
	    	    
	    	    try {
	    	      dp.setProgram(dancerProgram);
	    	    } catch (IdentifierAlreadyInUseException doh) {
	    	  //    System.out.println(doh.getMessage());
	    	    } catch (InvalidStatementException doh) {
	    	  //    System.out.println(doh.getMessage());
	    	    }
	    	    System.out.println("Program set");
	    		
	    	    OutputModule temp = null;
				try {
					temp = ip.outputMethod("Virtual", "/ilab/users/kashifq/workspace/DLSPEB/src/view/map.txt", "run");
				} catch (FileNotFoundException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				} catch (InvalidOutputModuleException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
	    	    try {
					dp.outputMethod(temp);
				} catch (InvalidOutputModuleException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				dp.world = ip.world;
				Thread t = new Thread();
				
		          new Thread
		          (
		            new Runnable()
		            {
		              public void run()
		              {
		                boolean k = dp.step();
		                
		              }
		            }
		          ).start();
			}
			else
			{
				
		          boolean m = deepee.step();
			}

    	    
			if(stepStart)
			{
				stepStart = false;
			String text = new String("");
			try {
				text = dancerBox.getText(0, 28);
			} catch (BadLocationException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			text = text.trim();
			
			try {
				dancerBox.getHighlighter().removeAllHighlights();
				highlight = dancerBox.getHighlighter().addHighlight(0, text.length()+1, painter);
			} catch (BadLocationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			}
			
			else
			{
				String text = new String("");
				System.out.println("Helper " + helper);
				if(helper)
				{
					System.out.println("Testing123");
					try {
						text = dancerBox.getText(29*stepC, 29);
						text = text.trim();
					} catch (BadLocationException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					try {
						System.out.println("text length " + text.length());
						System.out.println("text " + text);
						dancerBox.getHighlighter().removeAllHighlights();
						highlight = dancerBox.getHighlighter().addHighlight(29*stepC, 29*stepC+text.length()+1, painter);
					} catch (BadLocationException e1) 
					{
						System.out.println("Bad highlight");
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
			stepC++;
			
    	}
    	
    	if(event.compareTo("Run") == 0)
    	{
    		int totalLines = masterBox.getLineCount();
    		int counter = 0;
    		int startOffset;
    		while (counter <= totalLines)
    		{
    			try
    			{
    				startOffset = masterBox.getLineStartOffset(counter);
    				String temp = masterBox.getText(startOffset, 11);
    				temp = temp.trim();
    				masterTree.add(counter, temp);
    			}
    			catch(BadLocationException ugh)
    			{
    				
    			}
    			counter++;
    		}
    		
    		counter = 0;
    		while(counter <= masterTree.size() -1)
    		{
    			System.out.println("Master tree element " + counter + " is " + masterTree.get(counter));
    			counter++;
    		}
    		
    		totalLines = dancerBox.getLineCount();
    		counter = 0;
    		while(counter <= totalLines)
    		{
    			try
    			{
    				startOffset = dancerBox.getLineStartOffset(counter);
    				String temp = dancerBox.getText(startOffset, 28);
    				temp = temp.trim();
    				dancerTree.add(counter, temp);
    			}
    			catch(BadLocationException ok)
    			{
    				System.out.println("aok");
    			}
    			counter++;
    		}
    		counter = 0;
    		while(counter <= dancerTree.size() - 1)
    		{
    			System.out.println("Dancer arraylist element " + counter + " is " + dancerTree.get(counter));
    			counter++;
    		}
    		
    	    final DancerProgram dp = new DancerProgram();
    	    final InstructorProgram ip = new InstructorProgram();
    	    ArrayList<String> dancerProgram = new ArrayList<String>(dancerTree);
    	    ArrayList<String> instructorProgram = new ArrayList<String>(masterTree);
    	    
    	    try {
    	      dp.setProgram(dancerProgram);
    	    } catch (IdentifierAlreadyInUseException doh) {
    	  //    System.out.println(doh.getMessage());
    	    } catch (InvalidStatementException doh) {
    	  //    System.out.println(doh.getMessage());
    	    }
    	    System.out.println("Program set");
    		
    	    OutputModule temp = null;
			try {
				temp = ip.outputMethod("Virtual", "/ilab/users/kashifq/workspace/DLSPEB/src/view/map.txt", "run");
			} catch (FileNotFoundException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} catch (InvalidOutputModuleException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
    	    try {
				dp.outputMethod(temp);
			} catch (InvalidOutputModuleException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
          dp.world = ip.world;
    	    
          new Thread
          (
            new Runnable()
            {
              public void run()
              {
                dp.run(ip);
              }
            }
          ).start();
    	    System.out.println("Done with run.");
    		masterTree.clear();
    		dancerTree.clear();
    	}

    	
    	if(event.compareTo("Stop") == 0)
    	{
    		System.out.println("Stop");
    	}
    		
    	if(event.compareTo("comboBoxChanged") == 0)
    	{
    	//undo = true;
    	//undoB.setEnabled(true);
    	System.out.println(event);
		JComboBox selection = (JComboBox)e.getSource();
		String command = (String)selection.getSelectedItem();
		
		System.out.println(dancerLines.getText());
		System.out.println(dTotalLines);
		System.out.println(command + " " + contains(masterCommands, command));
		Integer num = new Integer(0);
		Integer num2 = new Integer(0);
		
		int location = 0;
		try
		{
			num = Integer.parseInt(masterLines.getText());
			num = num - 1;
			masterNum = true;
		}
		catch(NumberFormatException NFE)
		{
			masterLines.setText("Only numbers are valid line numbers");
			masterNum = false;
		}
		
		try
		{
			num2 = Integer.parseInt(dancerLines.getText());
			num2 = num2 - 1;
			dancerNum = true;
		}
		catch(NumberFormatException NFe)
		{
			dancerLines.setText("Only numbers are valid line numbers");
			dancerNum = false;
		}
		
		int index = selection.getSelectedIndex();
		
		// Master command only
		if(contains(masterCommands, command) && masterNum && !contains(dancerCommands, command) && command.compareTo(" instructor") != 0)
		{
			Master = true;
			try{
				location = masterBox.getLineStartOffset(num.intValue());
				}
				catch(BadLocationException be)
				{
					System.out.println("wassup");
				}
			System.out.println("A " + command);
			Integer lineNum = new Integer(masterLines.getText());
			
			if(command.compareTo( "clear") == 0)
			{
				System.out.println("Sanity check on clear");
				mTotalLines = 0;
				staticMTL = 0;
				masterBox.setText("");
				masterLines.setText("1");
				master.setSelectedIndex(0);
			}
			
			else if(lineNum <= mTotalLines) // Insert to line in middle of code
			{
				if(command.compareTo(" delete line") == 0)
				{
					if(lineNum == 1)
					{
						String temp = masterBox.getText();
						System.out.println("deleting first line " + temp);
						masterBox.setText("");
						masterBox.append(temp);
					}
					
					try
					{
						String temp = masterBox.getText(0, location);
						masterBox.setText("");
						masterBox.append(temp);
					}
					catch (BadLocationException ble)
					{
						
					}
					
					mTotalLines--;
					staticMTL--;
					
				}
				
				else
				{
				System.out.println("D");
				if(lineNum == 1)
				{
					String temp = masterBox.getText();
					masterBox.setText("");
					masterBox.append(command + "\n");
					masterBox.append(temp);
				}
				else 
				{
					masterBox.insert("\n", location);
					masterBox.insert(command, location);
				}
			}}
			
			else // Insert to line at end of code
			{
				masterBox.append(command);
				masterBox.append("\n");
			}
			
			lineNum = lineNum - 1;
			mTotalLines++;
			staticMTL++; 
			lineNum = mTotalLines + 1;
			masterLines.setText(lineNum.toString());		
			master.setSelectedIndex(0);
			System.out.println("MTL " + mTotalLines);
		}
		else if(contains(masterCommands, command) && masterNum && command.compareTo(" instructor") != 0 && masterCommands.length > index && command.equals(masterCommands[index])) // Master command that dancer also has
		{
			Master = true;
			System.out.println("A master?");
			try{
				location = masterBox.getLineStartOffset(num.intValue());
				}
				catch(BadLocationException be)
				{
					System.out.println("wassup");
				}
				
				
			if(command.compareTo(" clear") == 0)
			{
				masterBox.setText("");
				masterLines.setText("1");
				mTotalLines = 0;
				staticMTL = 0;
				master.setSelectedIndex(0);
			}
			else if(masterCommands.length > index && command.equals(masterCommands[index]))
			{
				System.out.println("B " + command);
				Integer lineNum = new Integer(masterLines.getText());
				System.out.println("Obtained line number " + lineNum);
				System.out.println("mTotalLines " + mTotalLines);
				
				if(lineNum <= mTotalLines)
				{
					if(command.compareTo(" delete line") == 0)
					{
						int offset = 0;
						int offset2 = 0;
						String temp = "";
						System.out.println("SC4");
						if(lineNum == 1)
						{
							try{
								offset = masterBox.getLineEndOffset(0);
								System.out.println("1");
								int totalLines = masterBox.getLineCount();
								totalLines = totalLines - 2;
								System.out.println(totalLines);
								temp = masterBox.getText(offset, (11*totalLines)+totalLines);
								System.out.println("2");
							}
							catch(BadLocationException ble)
							{
								
							}

							//temp = masterBox.getText();
							System.out.println("Temp? " + temp);
							masterBox.setText("");
							masterBox.append(temp);
							//masterBox.append("\n");
							mTotalLines--;
							staticMTL = staticMTL - 1;
						}
						
						
						else if(lineNum == mTotalLines)
						{
							try
							{
								System.out.println("sc10");
								int aNum = lineNum - 1;
								offset = masterBox.getLineStartOffset(aNum);
								temp = masterBox.getText(0, offset);
								masterBox.setText("");
								masterBox.append(temp);
								mTotalLines--;
								staticMTL--;
								System.out.println("mTotalLines " + mTotalLines);
							}
							catch(BadLocationException ble)
							{
								
							}
						}
						
						else if(lineNum < mTotalLines)
						{
							try
							{
								int aNum = lineNum - 1;
								offset = masterBox.getLineStartOffset(aNum);
								temp = masterBox.getText(0, offset);
								System.out.println("temp" + temp);
								int count = masterBox.getLineOfOffset(masterBox.getLineStartOffset(aNum));
								offset2 = masterBox.getLineEndOffset(aNum);
								int totalLines = masterBox.getLineCount();
								System.out.println("Total lines " + totalLines);
								System.out.println("aNum " + aNum);
								System.out.println("Count " + count);
								if(totalLines >= 14)
								{
									System.out.println("Reached here");
									aNum = aNum - 1;
								}
								System.out.println("offset2 line " + masterBox.getLineOfOffset(offset2));
								
								int remainingLines = totalLines - 1 - (count+1);
								System.out.println("remaining lines " + remainingLines);
								
								int counter3 = count+1;
								String afterLine = new String("");
								while(counter3 < totalLines - 1)
								{
								//	System.out.println("Line number " + (counter3+1) + " " + masterBox.getText(offset2, 11));
									afterLine = afterLine.concat(masterBox.getText(offset2, 12));
									offset2 = offset2 + 12;
									counter3++;
								}
								
								String full = temp.concat(afterLine);
								System.out.println("full " + full);
								
								masterBox.setText("");
								masterBox.append(full);
								mTotalLines = mTotalLines - 1;
								staticMTL = staticMTL - 1;
								System.out.println("Delete in middle mtl " + mTotalLines);
								
							}
							catch (BadLocationException ble)
							{
								System.out.println("Bad location 1");
							}
							
						}
						
					}
								
					else{
					if(lineNum < 1)
					{
						System.out.println("Don't be an idiot");
					}
					System.out.println("C");
					if(lineNum == 1)
					{
						String temp = masterBox.getText();
						masterBox.setText("");
						masterBox.append(command + "\n");
						masterBox.append(temp);
					}
					else
					{
						System.out.println("flag");
						masterBox.insert("\n", location);
						masterBox.insert(command, location);
					}
				}}
				
				else
				{
					if(command.compareTo(" delete line") == 0)
					{
					//	mTotalLines--;
					//	staticMTL--;
					}
					else
					{
						masterBox.append(command);
						masterBox.append("\n");
					}
				}	
				lineNum = mTotalLines;
				if(command.compareTo(" delete line") != 0)
				{
					mTotalLines++;
					staticMTL++;
				}
				lineNum = mTotalLines + 1;
				masterLines.setText(lineNum.toString());
				master.setSelectedIndex(0);
			}}
			else if(contains(dancerCommands, command) && dancerNum && !contains(masterCommands, command) && command.compareTo(" dancer") != 0) // Dancer command 
			{
				Master = false;
				System.out.println("Here?");
				try{
					location = dancerBox.getLineStartOffset(num2.intValue());
					}
					catch(BadLocationException be)
					{
						System.out.println("wassup");
					}
				System.out.println("Z " + command);
				Integer lineNum = new Integer(dancerLines.getText());
				loc = location;
				
				if(command.compareTo(" repeat x times             ") == 0)
				{
					aLine = lineNum;
					System.out.println("SC");
					window.setVisible(false);
					TextFieldListener.main(masterCommands);
					dTotalLines = dTotalLines + 2;
					
				}
				
				else if(command.compareTo(" set                        ") == 0)
				{
					aLine = lineNum;
					window.setVisible(false);
					//TODO: right here
					Set.main(functions);
					dTotalLines = dTotalLines + 2;
				}
				
				else if(command.compareTo(" if                         ") == 0)
				{
					aLine = lineNum;
					window.setVisible(false);
					conditional.main(masterCommands);
					dTotalLines = dTotalLines + 4;
				}
				
				else if(command.compareTo(" define                     ") == 0)
				{
					aLine = lineNum;
					window.setVisible(false);
					define.main(functions);
					dTotalLines = dTotalLines + 2;
				}
				
				else if(command.compareTo(" perform                    ") == 0)
				{
					aLine = lineNum;
					window.setVisible(false);
					Perform.main(functions);
					dTotalLines = dTotalLines++;
				}
				
				else if(command.compareTo(" while                      ") == 0)
				{
					aLine = lineNum;
					window.setVisible(false);
					//TODO: right here
					loop.main(masterCommands);
					dTotalLines = dTotalLines + 2;
				}
				
				else if(command.compareTo(" clear") == 0)
				{
					System.out.println("Testing123");
					dTotalLines = 1;
					staticDTL = 1;
					String[] temp = new String[100];
					functions = temp;
					dancerBox.setText("");
					dancerLines.setText("1");
					dancer.setSelectedIndex(0);
				}
					
				else{
				
				if(lineNum <= dTotalLines) // Insert to line in middle of code
				{
					if(command.compareTo(" delete line") == 0)
					{
						int offset = 0;
						int offset2 = 0;
						String temp = "";
						System.out.println("CS4 " + lineNum);
						if(lineNum == 1)
						{
							try{
								offset = dancerBox.getLineEndOffset(0);
								int totalLines = dancerBox.getLineCount();
								totalLines = totalLines - 2;
								System.out.println(totalLines);
								temp = dancerBox.getText(offset, (11*totalLines));
								System.out.println("2");
							}
							catch(BadLocationException ble)
							{
								System.out.println("ok...");
							}

							System.out.println("Temp? " + temp);
							dancerBox.setText("");
							dancerBox.append(temp);
							dancerBox.append("\n");
							dTotalLines--;
							dTotalLines--;
							staticDTL = staticDTL -2;
						}
						
						
						else if(lineNum == dTotalLines)
						{
							try
							{
								int aNum = lineNum - 1;
								offset = dancerBox.getLineStartOffset(aNum);
								temp = dancerBox.getText(0, offset);
								dancerBox.setText("");
								dancerBox.append(temp);
								dTotalLines--;
								dTotalLines--;
								staticDTL = staticDTL - 2;
							}
							catch(BadLocationException ble)
							{
								
							}
						}
						
						else if(lineNum < dTotalLines)
						{
							try
							{
								int aNum = lineNum - 1;
								offset = dancerBox.getLineStartOffset(aNum);
								temp = dancerBox.getText(0, offset);
								
								System.out.println(temp);
								
								offset2 = dancerBox.getLineEndOffset(aNum);
								int totalLines = dancerBox.getLineCount();
								totalLines = totalLines - 2;
								String temp2 = dancerBox.getText(offset2, (11*(totalLines-aNum)));
								System.out.println(temp2);
								dancerBox.setText("");
								dancerBox.append(temp);
								dancerBox.append(temp2);
								dancerBox.append("\n");
								dTotalLines = dTotalLines - 2;
								staticDTL = staticDTL - 2;
							}
							catch (BadLocationException ble)
							{
								System.out.println("Bad location 2");
							}
						}
						
					}
					// delete end
					
					System.out.println("W");
					if(lineNum == 1)
					{
						String temp = dancerBox.getText();
						dancerBox.setText("");
						dancerBox.append(command + "\n");
						dancerBox.append(temp);
					}
					else 
					{
						dancerBox.insert("\n", location);
						dancerBox.insert(command, location);
					}
				}
				
				else // Insert to line at end of code
				{
					if(command.compareTo(" delete line") == 0)
					{
						dTotalLines--;
						staticDTL--;
					}
					else
					{
						dancerBox.append(command);
						dancerBox.append("\n");
					}
				}
				
				lineNum = lineNum - 1;
				dTotalLines++;
				staticDTL++;
				lineNum = dTotalLines + 1;
				dancerLines.setText(lineNum.toString());		
				dancer.setSelectedIndex(0);
				}}
			
			else if(dancerNum && command.compareTo(" dancer") != 0) // Dancer command that master also has
			{
				Master = false;
				try{
					location = dancerBox.getLineStartOffset(num2.intValue());
					}
					catch(BadLocationException be)
					{
						System.out.println("wassup");
					}
				if(dancerCommands.length >= index && command.equals(dancerCommands[index]))
				{
					System.out.println("Y " + command);
					Integer lineNum = new Integer(dancerLines.getText());
					System.out.println("lineNum " + lineNum + " dtl " + dTotalLines);
					
					if(command.compareTo(" clear") == 0)
					{
						System.out.println("Testing123");
						dTotalLines = 0;
						staticDTL = 0;
						String[] temp = new String[100];
						functions = temp;
						dancerBox.setText("");
						dancerLines.setText("1");
						dancer.setSelectedIndex(0);
					}
					
					else if(lineNum <= dTotalLines)
					{
						// start here
						if(command.compareTo(" delete line") == 0)
						{
							int offset = 0;
							int offset2 = 0;
							String temp = "";
							System.out.println("ABC4");
							if(lineNum == 1)
							{
								try{
									offset = dancerBox.getLineEndOffset(0);
									System.out.println("1");
									int totalLines = dancerBox.getLineCount();
									totalLines = totalLines - 2;
									System.out.println(totalLines);
									temp = dancerBox.getText(offset, (28*totalLines)+totalLines);
									System.out.println("2" + (28*totalLines)+totalLines);
								}
								catch(BadLocationException ble)
								{
									
								}

								//temp = masterBox.getText();
								System.out.println("Temp? " + temp);
								dancerBox.setText("");
								dancerBox.append(temp);
								//masterBox.append("\n");
								dTotalLines--;
								staticDTL = staticDTL - 1;
							}
							
							
							else if(lineNum == dTotalLines)
							{
								try
								{
									System.out.println("cs10");
									int aNum = lineNum - 1;
									offset = dancerBox.getLineStartOffset(aNum);
									temp = dancerBox.getText(0, offset);
									dancerBox.setText("");
									dancerBox.append(temp);
									dTotalLines--;
									staticDTL--;
									System.out.println("dTotalLines " + dTotalLines);
								}
								catch(BadLocationException ble)
								{
									
								}
							}
							
							else if(lineNum < dTotalLines)
							{
								try
								{
									int aNum = lineNum - 1;
									offset = dancerBox.getLineStartOffset(aNum);
									temp = dancerBox.getText(0, offset);
									System.out.println("temp" + temp);
									int count = dancerBox.getLineOfOffset(dancerBox.getLineStartOffset(aNum));
									offset2 = dancerBox.getLineEndOffset(aNum);
									int totalLines = dancerBox.getLineCount();
									System.out.println("Total lines " + totalLines);
									System.out.println("aNum " + aNum);
									System.out.println("Count " + count);
									if(totalLines >= 14)
									{
										System.out.println("Reached here");
										aNum = aNum - 1;
									}
									System.out.println("offset2 line " + dancerBox.getLineOfOffset(offset2));
									
									int remainingLines = totalLines - 1 - (count+1);
									System.out.println("remaining lines " + remainingLines);
									
									int counter3 = count+1;
									String afterLine = new String("");
									while(counter3 < totalLines - 1)
									{
									//	System.out.println("Line number " + (counter3+1) + " " + masterBox.getText(offset2, 11));
										afterLine = afterLine.concat(dancerBox.getText(offset2, 29));
										offset2 = offset2 + 29;
										counter3++;
									}
									
									String full = temp.concat(afterLine);
									System.out.println("full " + full);
									
									dancerBox.setText("");
									dancerBox.append(full);
									dancerBox.append("\n");
									dTotalLines = dTotalLines - 1;
									staticDTL = staticDTL - 1;
									System.out.println("Delete in middle dtl " + dTotalLines);
									
								}
								catch (BadLocationException ble)
								{
									System.out.println("Bad location 1");
								}
								
							}
							
						}
						
						// end here
						if(lineNum < 1 || lineNum > dTotalLines)
						{
							System.out.println("Don't be an idiot");
						}
						System.out.println("X");
						if(lineNum == 1 && command.compareTo(" delete line") != 0)
						{
							String temp = dancerBox.getText();
							dancerBox.setText("");
							dancerBox.append(command + "\n");
							dancerBox.append(temp);
						}
						else if(command.compareTo(" delete line") != 0)
						{
							System.out.println("Dancer flag");
							dancerBox.insert("\n", location);
							dancerBox.insert(command, location);
						}
					}
					
					else if(command.compareTo(" delete line") != 0 && command.compareTo(" clear") != 0)
					{
						dancerBox.append(command);
						dancerBox.append("\n");
					}
					
					if(command.compareTo(" delete line") == 0 || command.compareTo(" clear") == 0)
					{
						System.out.println("t3");
						dTotalLines--;
						staticDTL--;
					}
					
					lineNum = lineNum - 1;
					dTotalLines++;
					staticDTL++;
					lineNum = dTotalLines + 1;
					dancerLines.setText(lineNum.toString());	
					dancer.setSelectedIndex(0);
				}				
			}
		
		}   	
    }
    
    public GUI(Container pane) {
        if (RIGHT_TO_LEFT) {
            pane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }

        JButton button;
	pane.setLayout(new GridBagLayout());
	GridBagConstraints c = new GridBagConstraints();
	if (shouldFill) {
	//natural height, maximum width
	c.fill = GridBagConstraints.HORIZONTAL;
	}

	JScrollPane jsp = new JScrollPane();
	masterBox = new JTextArea(35, 35);
	masterBox.setEditable(false);

	lines = new JTextArea("1");
		lines.setBackground(Color.LIGHT_GRAY);
	lines.setEditable(false);

	masterBox.getDocument().addDocumentListener(new DocumentListener(){
		public String getText(){
			int caretPosition = masterBox.getDocument().getLength();
			Element root = masterBox.getDocument().getDefaultRootElement();
			String text = "1" + System.getProperty("line.separator");
			for(int i = 2; i < root.getElementIndex( caretPosition ) + 2; i++){
				text += i + System.getProperty("line.separator");
			}
			return text;
		}
		
		public void changedUpdate(DocumentEvent de) {
			lines.setText(getText());
		}

		
		public void insertUpdate(DocumentEvent de) {
			lines.setText(getText());
		}

		
		public void removeUpdate(DocumentEvent de) {
			lines.setText(getText());
		}

	});

	masterBox.getDocument().addUndoableEditListener(
	        new UndoableEditListener() {
	            public void undoableEditHappened(UndoableEditEvent e) {
	              undoManager.addEdit(e.getEdit());
	           //   updateButtons();
	            }
	          });
	
	jsp.getViewport().add(masterBox);
	jsp.setRowHeaderView(lines);
	jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	if (shouldWeightX) {
	c.weightx = 0.5;
	}
	c.fill = GridBagConstraints.HORIZONTAL;
	c.gridx = 0;
	c.gridy = 3;
	pane.add(jsp, c);

	//
		JScrollPane jsp2 = new JScrollPane();
		dancerBox = new JTextArea(35, 35);
		dancerBox.setEditable(false);

		lines2 = new JTextArea("1");
 		lines2.setBackground(Color.LIGHT_GRAY);
		lines2.setEditable(false);
 
		dancerBox.getDocument().addDocumentListener(new DocumentListener(){
			public String getText(){
				int caretPosition = dancerBox.getDocument().getLength();
				Element root = dancerBox.getDocument().getDefaultRootElement();
				String text = "1" + System.getProperty("line.separator");
				for(int i = 2; i < root.getElementIndex( caretPosition ) + 2; i++){
					text += i + System.getProperty("line.separator");
				}
				return text;
			}
			
			public void changedUpdate(DocumentEvent de) {
				lines2.setText(getText());
			}
 
			
			public void insertUpdate(DocumentEvent de) {
				lines2.setText(getText());
			}
 
			
			public void removeUpdate(DocumentEvent de) {
				lines2.setText(getText());
			}
 
		});
 
	jsp2.getViewport().add(dancerBox);
	jsp2.setRowHeaderView(lines2);
	jsp2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	c.gridx = 2;
	c.gridy = 3;
	pane.add(jsp2, c);
	
	JButton saveI = new JButton("Save instructor");
	saveI.setBackground(new Color(200, 150, 200));
    saveI.setVerticalTextPosition(AbstractButton.CENTER);
    saveI.setHorizontalTextPosition(AbstractButton.LEADING); 
    saveI.setMnemonic(KeyEvent.VK_I);
    saveI.addActionListener(this);
    c.gridx = 0;
    c.gridy = 0;
    pane.add(saveI, c);
    /**
    undoB = new JButton("Undo");
	undoB.setBackground(new Color(255, 255, 0));
	undoB.setVerticalTextPosition(AbstractButton.CENTER);
	undoB.setHorizontalTextPosition(AbstractButton.LEADING); 
	undoB.setMnemonic(KeyEvent.VK_U);
	undoB.addActionListener(this);
	undoB.setEnabled(false);
	c.gridx = 3;
	pane.add(undoB, c);
	*/
	loadWorld = new JButton("Load Virtual World");
	loadWorld.setBackground(new Color(73, 121, 107));
	loadWorld.setVerticalTextPosition(AbstractButton.CENTER);
	loadWorld.setHorizontalTextPosition(AbstractButton.LEADING);
	loadWorld.setMnemonic(KeyEvent.VK_W);
	loadWorld.addActionListener(this);
	c.gridx = 1;
	c.gridy = 1;
	pane.add(loadWorld, c);
	
    JButton step2 = new JButton("Step");
    step2.setBackground(new Color(0, 150, 150));
    step2.setVerticalTextPosition(AbstractButton.CENTER);
    step2.setHorizontalTextPosition(AbstractButton.LEADING); 
    step2.setMnemonic(KeyEvent.VK_S);
    step2.addActionListener(this);
	c.gridx = 0;
	c.gridy = 5;
	pane.add(step2, c);
	
    JButton run2 = new JButton("Run");
    run2.setBackground(Color.green);
    run2.setVerticalTextPosition(AbstractButton.CENTER);
    run2.setHorizontalTextPosition(AbstractButton.LEADING); 
    run2.setMnemonic(KeyEvent.VK_T);
    run2.addActionListener(this);
	c.gridx = 2;
	c.gridy = 5;
	pane.add(run2, c);
	
    JButton saveD = new JButton("Save dancer");
    saveD.setBackground(new Color(200, 150, 200));
    saveD.setVerticalTextPosition(AbstractButton.CENTER);
    saveD.setHorizontalAlignment(AbstractButton.CENTER);
    saveD.setMnemonic(KeyEvent.VK_D);
    saveD.addActionListener(this);
    c.gridx = 2;
    c.gridy = 0;
    pane.add(saveD, c);

    JButton loadD = new JButton("Load dancer");
    loadD.setBackground(new Color(0, 206, 209));
    loadD.setVerticalTextPosition(AbstractButton.CENTER);
    loadD.setHorizontalAlignment(AbstractButton.CENTER);
    loadD.setMnemonic(KeyEvent.VK_O);
    loadD.addActionListener(this);
    c.gridy = 1;
    c.gridx = 2;
    pane.add(loadD, c);
    
    JButton loadI = new JButton("Load instructor");
    loadI.setBackground(new Color(0, 206, 209));
    loadI.setVerticalTextPosition(AbstractButton.CENTER);
    loadI.setHorizontalAlignment(AbstractButton.CENTER);
    loadI.setMnemonic(KeyEvent.VK_A);
    loadI.addActionListener(this);
    c.gridx = 0;
    pane.add(loadI, c);
    /**
    redoB = new JButton("Redo");
    redoB.setBackground(new Color(255, 255, 0));
    redoB.setVerticalTextPosition(AbstractButton.CENTER);
    redoB.setHorizontalTextPosition(AbstractButton.LEADING); 
    redoB.setMnemonic(KeyEvent.VK_R);
    redoB.addActionListener(this);
	c.gridx = 3;
	redoB.setEnabled(false);
	pane.add(redoB, c);
	*/
    
    
	master = new JComboBox(masterCommands);
	master.setSelectedIndex(0);
	master.addActionListener(this);
	c.gridx = 0;
	c.gridy = 6;
	pane.add(master, c);

	dancer = new JComboBox(dancerCommands);
	dancer.setSelectedIndex(0);
	dancer.addActionListener(this);
	c.gridx = 2;
	c.gridy = 6;
	pane.add(dancer, c);

	JLabel m = new JLabel("Instructor");
	c.gridx = 0;
	c.gridy = 2;
	pane.add(m, c);
	
	JLabel d = new JLabel("Dancer");
	c.gridx = 2;
	pane.add(d, c);
	
	masterLines = new JTextField("1");
	masterLines.addActionListener(this);
	c.gridx = 0;       
	c.gridy = 7;     
	pane.add(masterLines, c);
	
	dancerLines = new JTextField("1");
	dancerLines.addActionListener(this);
	c.gridx = 2;
	pane.add(dancerLines, c);
    window = pane;
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("Dance Learning Simulation Programming Environment");
        ok = frame;
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Set up the content pane.
        GUI newContentPane = new GUI(frame.getContentPane());

        //Display the window.
        frame.setSize(1600, 1600);
        frame.setVisible(true);
    }

    protected static boolean contains(String[] array, String key)
    {
    	int counter = 0;
    	while(counter < array.length)
    	{
    		if(array[counter].compareTo(key) == 0)
    		{
    			return true;
    		}
    		counter++;
    	}
    	return false;
    }
    
    public static void define(String thanks, boolean condition)
    {
    	assist = condition;
    	if(condition)
    	{
    		colors2[x] = thanks;
    		x++;
    		functions[functionCounter] = thanks;
    		functionCounter++;
    		System.out.println("aLine " + aLine + " staticDTL " + staticDTL);
			int counter = thanks.length() + 8;
			while(counter < 28)
			{
				thanks = thanks.concat(" ");
				counter++;
				System.out.println("define length " + thanks.length());
			}
    		if(aLine <= staticDTL) // Insert to line in middle of code
    		{
    			System.out.println("D");
    			if(aLine == 1)
    			{
    				String temp = dancerBox.getText();
    				dancerBox.setText("");
    				dancerBox.append(" define " + thanks + "\n" + " endDefine                  " + "\n");
    				dancerBox.append(temp);
    			}
    			else 
    			{
    				dancerBox.insert("\n", loc);
    				dancerBox.insert(" define " + thanks + "\n" + " endDefine                  ", loc);
    			}
    		}
		
    		else // Insert to line at end of code
    		{
    			dancerBox.append(" define " + thanks + "\n" + " endDefine                  ");
    			dancerBox.append("\n");
    		}
		
     		if(aLine == 1)
     			aLine = aLine - 1;
    		staticDTL = staticDTL + 2;	
    		aLine = staticDTL + 1;
    		Integer t = new Integer(aLine);
    		System.out.println("Integer t " + t);
    		String temp = new String(t.toString());
    		dancerLines.setText(temp);		
    		dancer.setSelectedIndex(0);
    		window.setVisible(true);		
    		assistance = thanks;
    		System.out.println("Thanks");
    	}
    }
    
    public static void perform(String thanks, boolean condition)
    {
    	assist = condition;
    	if(condition)
    	{
    		System.out.println("aLine " + aLine + " staticDTL " + staticDTL);
			int counter = thanks.length() + 7;
			System.out.println("perform length " + thanks.length());
			while(counter < 28)
			{
				thanks = thanks.concat(" ");
				counter++;
			}
			
    		if(aLine <= staticDTL) // Insert to line in middle of code
    		{
    			System.out.println("D");
    			if(aLine == 1)
    			{
    				String temp = dancerBox.getText();
    				dancerBox.setText("");
    				dancerBox.append(" perform " + thanks + "\n");
    				dancerBox.append(temp);
    			}
    			else 
    			{
    				dancerBox.insert("\n", loc);
    				dancerBox.insert(" perform " + thanks, loc);
    			}
    		}
		
    		else // Insert to line at end of code
    		{
    			dancerBox.append(" perform " + thanks);
    			dancerBox.append("\n");
    		}
		
     		if(aLine == 1)
     			aLine = aLine - 1;
    		staticDTL = staticDTL + 1;	
    		aLine = staticDTL + 1;
    		Integer t = new Integer(aLine);
    		System.out.println("Integer t " + t);
    		String temp = new String(t.toString());
    		dancerLines.setText(temp);		
    		dancer.setSelectedIndex(0);
    		window.setVisible(true);		
    		assistance = thanks;
    	}
    }
    
    public static void conditional(String condition, boolean cond)
    {
    	assist = cond;
    	if(cond)
    	{
    		System.out.println("aLine " + aLine + " staticDTL " + staticDTL);
     		if(aLine <= staticDTL) // Insert to line in middle of code
    		{
    			System.out.println("D");
    			if(aLine == 1)
    			{
    				String temp = dancerBox.getText();
    				dancerBox.setText("");
    				dancerBox.append(" if " + condition + "\n" + " else                       " + "\n" + " endElse                    " + "\n" + " endIf                       " + "\n");
    				dancerBox.append(temp);
    			}
    			else 
    			{
    				dancerBox.insert("\n", loc);
    				dancerBox.insert(" if " + condition + "\n" + " else                       " + "\n" + " endElse                    " + "\n" + " endIf                       ", loc);
    			}
    		}
    		
    		else // Insert to line at end of code
    		{
    			dancerBox.append(" if " + condition + "\n" + " else                       " + "\n" + " endElse                    " + "\n" + " endIf                       ");
    			dancerBox.append("\n");
    		}
    		
     		if(aLine == 1)
     			aLine = aLine - 1;
    		staticDTL = staticDTL + 3;	
    		aLine = staticDTL + 1;
    		Integer t = new Integer(aLine);
    		System.out.println("Integer t " + t);
    		String temp = new String(t.toString());
    		dancerLines.setText(temp);		
    		dancer.setSelectedIndex(0);
        	window.setVisible(true);   	
        	assistance = condition;   		
    	}
    }
    
    public static void wile(String condition, boolean cond)
    {
    	assist = cond;
    	if(cond)
    	{
    		System.out.println("aLine " + aLine + " staticDTL " + staticDTL);
     		if(aLine <= staticDTL) // Insert to line in middle of code
    		{
    			System.out.println("D");
    			if(aLine == 1)
    			{
    				String temp = dancerBox.getText();
    				dancerBox.setText("");
    				dancerBox.append(" while " + condition + "\n" + " endWhile                         " + "\n");
    				dancerBox.append(temp);
    			}
    			else 
    			{
    				dancerBox.insert("\n", loc);
    				dancerBox.insert(" while " + condition + "\n" + " endWhile                    ", loc);
    			}
    		}
    		
    		else // Insert to line at end of code
    		{
    			dancerBox.append(" while " + condition + "\n" + " endWhile                    ");
    			dancerBox.append("\n");
    		}
    		
     		if(aLine == 1)
     			aLine = aLine - 1;
    		staticDTL = staticDTL + 2;	
    		aLine = staticDTL + 1;
    		Integer t = new Integer(aLine);
    		System.out.println("Integer t " + t);
    		String temp = new String(t.toString());
    		dancerLines.setText(temp);		
    		dancer.setSelectedIndex(0);
        	assistance = condition;   	
    	}
    	window.setVisible(true);
    }
    
    public static void set(String thx, boolean macro, boolean condition)
    {
    	assist = condition;
    	System.out.println("What is macro: " + macro);
    	if(macro)
    	{
    		int c = thx.length();
    		while(c <= 28)
    		{
    			micro = micro.concat(" ");
    			System.out.println("c " + c);
    			c++;
    		}
    	}
    	if(condition)
    	{
    	System.out.println("aLine " + aLine + " staticDTL " + staticDTL);
 		if(aLine <= staticDTL) // Insert to line in middle of code
		{
			System.out.println("D");
			if(aLine == 1)
			{
				String temp = dancerBox.getText();
				dancerBox.setText("");
				if(macro)
				{
					System.out.println("Flag 1");
					dancerBox.append(" set " + thx + " to                " + "\n" + " " + micro + "\n" + " endSet                      " + "\n");
					micro = null;
				}
				else
				{
					dancerBox.append(" set " + thx + " to                " + "\n" + " endSet                      " + "\n");
					dancerBox.append(temp);
				}
			}
			else 
			{
				dancerBox.insert("\n", loc);
				if(macro)
				{
					System.out.println("Sanity check");
					dancerBox.insert(" set " + thx + " to                " + "\n" + " " + micro + "\n" + " endSet                      ", loc);
					micro = null;
				}
				else
					dancerBox.insert(" set " + thx + " to                " + "\n" + " endSet                      ", loc);
			}
		}
		
		else // Insert to line at end of code
		{
			if(macro)
			{
				System.out.println("Test A");
				dancerBox.append(" set " + thx + " to                " + "\n" + " " + micro + "\n" + " endSet                      ");
				dancerBox.append("\n");
			}
			else
			{
				System.out.println("Test B");
				dancerBox.append(" set " + thx + " to                " + "\n" + " endSet                      ");
				dancerBox.append("\n");
			}
		}
		
 		if(aLine == 1)
 			aLine = aLine - 1;
		staticDTL = staticDTL + 2;	
		aLine = staticDTL + 1;
		if(macro)
		{
			staticDTL++;
			aLine++;
		}
		Integer t = new Integer(aLine);
		System.out.println("Integer t " + t);    		          
		String temp = new String(t.toString());
		dancerLines.setText(temp);		
		dancer.setSelectedIndex(0);
    	window.setVisible(true);   	
    	assistance = thx;
    	}   	
    }
    
    public static void assist(String thanks, boolean condition)
    {
    	assist = condition;
    	if(condition)
    	{
    	System.out.println("aLine " + aLine + " staticDTL " + staticDTL);
 		if(aLine <= staticDTL) // Insert to line in middle of code
		{
			System.out.println("D");
			if(aLine == 1)
			{
				String temp = dancerBox.getText();
				dancerBox.setText("");
				dancerBox.append(" repeat " + thanks + " times             " + "\n" + " endRepeat                   " + "\n");
				dancerBox.append(temp);
			}
			else 
			{
				dancerBox.insert("\n", loc);
				dancerBox.insert(" repeat " + thanks + " times             " + "\n" + " endRepeat                   ", loc);
			}
		}
		
		else // Insert to line at end of code
		{
			dancerBox.append(" repeat " + thanks + " times             " + "\n" + " endRepeat                   ");
			dancerBox.append("\n");
		}
		
 		if(aLine == 1)
 			aLine = aLine - 1;
		staticDTL = staticDTL + 2;	
		aLine = staticDTL + 1;
		Integer t = new Integer(aLine);
		System.out.println("Integer t " + t);
		String temp = new String(t.toString());
		dancerLines.setText(temp);		
		dancer.setSelectedIndex(0);
    	window.setVisible(true);   	
    	assistance = thanks;
    	}
    }
    
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
    	
    	
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
        
    	
    	    /** 
      
      InstructorProgram ip = new InstructorProgram();
      DancerProgram dp = new DancerProgram();
      OutputModule om = null;
      try {
        om = ip.outputMethod("MarkVirtual", "Hi", "run");
        dp.outputMethod(om);
      } catch (FileNotFoundException e) {
      } catch (InvalidOutputModuleException e) {
      }
      
      ArrayList<String> colors = new ArrayList<String>();
      colors.add("green");
      colors.add("blue");
      colors.add("red");
      ip.setProgram(colors);
      
      ArrayList<String> program = new ArrayList<String>();
 //     program.add("define hi");
 //     program.add("if is_obstacle_on_left");
 //     program.add("rotate_left");
 //     program.add("rotate_right");
      program.add("repeat 15 times");
      program.add("go_backward");
      program.add("endRepeat");
   //   program.add("endif");
   //   program.add("enddefine");
  //    program.add("go_forward");
  //    program.add("set blue to");
  //    program.add("hi");
  //    program.add("endset");
 //     program.add("perform blue");
      try
      {
        dp.setProgram(program);
      } catch (IdentifierAlreadyInUseException e) {
      }
      catch (InvalidStatementException e) {
      }
      dp.run(ip);
    */
    }

    public static void helper(boolean whatever)
    {
    	helper = whatever;
    }
    
    private static void makeList()
    {
		int totalLines = masterBox.getLineCount();
		int counter = 0;
		int startOffset;
		while (counter <= totalLines)
		{
			try
			{
				startOffset = masterBox.getLineStartOffset(counter);
				String temp = masterBox.getText(startOffset, 11);
				temp = temp.trim();
				masterTree.add(counter, temp);
			}
			catch(BadLocationException ugh)
			{
				
			}
			counter++;
		}
		
		counter = 0;
		while(counter <= masterTree.size() -1)
		{
			System.out.println("Master tree element " + counter + " is " + masterTree.get(counter));
			counter++;
		}
		
		totalLines = dancerBox.getLineCount();
		counter = 0;
		while(counter <= totalLines)
		{
			try
			{
				startOffset = dancerBox.getLineStartOffset(counter);
				String temp = dancerBox.getText(startOffset, 28);
				temp = temp.trim();
				dancerTree.add(counter, temp);
			}
			catch(BadLocationException ok)
			{
				
			}
			counter++;
		}
		counter = 0;
		while(counter <= dancerTree.size() - 1)
		{
			System.out.println("Dancer arraylist element " + counter + " is " + dancerTree.get(counter));
			counter++;
		}
		
		  	
    }
    
	protected static void macroSet(String condition) 
	{
		dTotalLines++;
		micro = condition;
	}
	
	protected static void hideWindow()
	{
		window.setVisible(false);
	}
	
	protected void rp()
	{
		repaint();
	}
	
	public static void windowClosed()
	{
		window.setVisible(true);
	}
}
