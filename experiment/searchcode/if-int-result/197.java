package uk.ac.lkl.migen.woztools.intervention;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.net.MalformedURLException;

import javax.swing.*;

import org.apache.xmlrpc.*;

/* This is old Java 1.4 code and there are many unchecked generics. Fix that before you use this class again */
@SuppressWarnings("unchecked")
public class InterventionFireClient extends JPanel {

    /**
     * The message container, where messages are displayed. 
     * 
     * Messages are shown in a JLabel associated to a JButton, see
     * method addMessage(String). 
     */
    private JPanel msgContainer = new JPanel();

    private JRadioButton suggestionButton = new JRadioButton("Suggestion");
    private JRadioButton modalButton = new JRadioButton("Modal");
    private JRadioButton responseButton = new JRadioButton("Response");
    private JRadioButton lowButton = new JRadioButton("Low");

    /**
     * The variable panel, where vars are got, and their value can 
     * be changed...
     */
    private JPanel varPanel = new JPanel();
    
    /**
     *    
     */
    XmlRpcClient rpcClient = null; 

    public InterventionFireClient() {
	super();

	this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	msgContainer.setLayout(new BoxLayout(msgContainer, BoxLayout.PAGE_AXIS));
	this.add(msgContainer);
	// Add buttons
	addRadioButtons(this);
	buildVarPanel();
	this.add(varPanel);
	addOtherActionButtons(this);
    }

    // FIXME: @Hack(who="MM",why="WOZ",issues =0)
    private void addOtherActionButtons(JPanel container) {
    	JButton beepButton = new JButton("Send Beep");
    	beepButton.addActionListener(new ActionListener() {
    		   public void actionPerformed(ActionEvent e) {
    				try {
    				   System.out.println("BEEP");
    				   //have to double check if that's the way to do async calls 
    				   //of methods that do not have params
    				   rpcClient.executeAsync("woz.beep", new Vector(),null);
    				} catch (Exception ex) {
    				    ex.printStackTrace();
    				}
    		   }
    		});
    	container.add(beepButton);
    	
    	
    	JButton enableColouringButton = new JButton("Enable colouring");
    	enableColouringButton.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			enableColouring();
    		}
    	});
    	container.add(enableColouringButton);
    	
    	JButton lockDownButton = new JButton("Lockdown");
    	lockDownButton.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			lockDown();
    		}
    	});
    	container.add(lockDownButton);
    	
    	JButton freedomButton = new JButton("Freedom");
    	freedomButton.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			freedom();
    		}
    	});
    	container.add(freedomButton);
    }
	
    /**
     * Adds the relevant radio buttons to 
     * the WOZ tool (Interventions Fire client).
     *  
     * @param container The container that will allocate the buttons.
     */
    private void addRadioButtons(JPanel container) {
	ButtonGroup interventionType = new ButtonGroup();
	interventionType.add(suggestionButton);
	interventionType.add(modalButton);
	interventionType.add(responseButton);
	interventionType.add(lowButton);
	suggestionButton.setSelected(true);
	container.add(suggestionButton);
	container.add(modalButton);
	container.add(responseButton);
	container.add(lowButton);
    }
    
    private void buildVarPanel() {
	varPanel.setLayout(new BorderLayout());
	varPanel.add(getChangeVarPanel(), BorderLayout.NORTH);
	varPanel.add(getShowVarsPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel getChangeVarPanel() {
	JPanel result = new JPanel();
	result.setLayout(new FlowLayout());
	final JTextField modelIndexText = new JTextField(2);
	final JTextField varIndexText = new JTextField(2);
	final JTextField varValueText = new JTextField(4);
	result.add(new Label("Tab Idx"));
	result.add(modelIndexText);
	result.add(new Label("Var Idx"));
	result.add(varIndexText);
	result.add(new Label("Value"));
	result.add(varValueText);
	
	JButton changeVarButton = new JButton("Change selected variable");
	changeVarButton.addActionListener(new ActionListener() {
	   public void actionPerformed(ActionEvent e) {
	       String modelIdxString = modelIndexText.getText();
	       int modelIdx = indexHuman2Machine(modelIdxString);
	       String varIdxString = varIndexText.getText();
	       int varIdx = indexHuman2Machine(varIdxString);
	       String varValueString = varValueText.getText();
	       int varValue = indexHuman2Machine(varValueString) +1; // FIXME: Not an index, ergo no need to substract one
	       changeVariable(modelIdx, varIdx, varValue);
	   }
	});
	result.add(changeVarButton);
	return result;
    }

    /** 
     * Converts an index entered by a human to something (more) useful 
     * from a computer's point of view. The thing is as trivial as converting
     * from String to int, and then substracting 1 (humans think from 1, but
     * arrays are numbered from 0...) 
     * 
     * @param index The index as a string (minimum 1).
     * 
     * @return the index (minus 1) as an integer, or 0 if there is 
     * any problem with the conversion.
     */
    private int indexHuman2Machine(String index) {
	int result = 0;
	try {
	    result = Integer.parseInt(index) - 1; 
	} catch (NumberFormatException ex) {
	    System.out.println("Bad index (" + index + "). Using 0 instead...");
	    result = 0;
	}
	return result;
    }
    
    private JPanel getShowVarsPanel() {
	JPanel result = new JPanel();
	result.setLayout(new FlowLayout());
	final JTextField modelIndexSelector = new JTextField(2);
	JButton showVarsButton = new JButton("Show vars");
	showVarsButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		String modelIdx = modelIndexSelector.getText();
		printVariables(indexHuman2Machine(modelIdx)); 
	    }
	});
	result.add(new Label("Tab"));
	result.add(modelIndexSelector);
	result.add(showVarsButton);
	result.add(new Label("             "));
	return result;
    }
    
    /** 
     * Adds a message to be sent to the student, including the 
     * "Send" button. 
     * 
     * @param msg
     */
    public void addMessage(String msg) {
	final JTextArea label = getTextArea();
	label.setText(msg);
	JButton button = new JButton("Send");
	button.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		sendIntervention(label.getText());
	    }});
	Panel panel = new Panel();
	panel.setLayout(new FlowLayout());
	panel.add(button);
	panel.add(label);
	msgContainer.add(panel);
    }
    
    private static JTextArea getTextArea() {
	return getTextArea(null);
    }

    private static JTextArea getTextArea(Dimension dimension) {
	JTextArea result = new JTextArea();
	if (dimension != null) 
	    result.setPreferredSize(dimension);
	else 
	    result.setPreferredSize(new Dimension(300,50)); // default
	result.setCursor(null);  
	result.setLineWrap(true);
	result.setWrapStyleWord(true);
	result.setOpaque(true);  
	result.setFocusable(true);  
	result.setEditable(true);  
	return result; 
    }

    private Vector<Integer> getVariableValues(int modelIdx) {
	if (rpcClient == null) {
	    System.out.println("Debug: no remote client to get vars from... :-(");
	    return null;
	}

	Object result;
	try {
	    Vector<Integer> param = new Vector<Integer>();
	    param.add(modelIdx);
	    result = rpcClient.execute("woz.getVariableValuesList", param);
	    if (result instanceof Vector<?>)
		return (Vector<Integer>) result;
	    else 
		return null;
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }
    
    private Vector<String> getVariableNames(int modelIdx) {
	if (rpcClient == null) {
	    System.out.println("Debug: no remote client to get vars from... :-(");
	    return null;
	}

	Object result;
	try {
	    Vector<Integer> param = new Vector<Integer>();
	    param.add(modelIdx);
	    result = rpcClient.execute("woz.getVariableNamesList", param);
	    if (result instanceof Vector<?>)
		return (Vector<String>) result;
	    else 
		return null;
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }
    
    
    private void enableColouring() {
    	if (rpcClient == null) {
    	    System.out.println("Debug: no remote client to enable colouring");
    	    return;
    	}

    	try {
    	    Vector param = new Vector();
    	    rpcClient.execute("woz.enableColouring", param);
    	} catch (Exception e) {
    	    e.printStackTrace();
    	}	
    }

    

    private void lockDown() {
    	if (rpcClient == null) {
    	    System.out.println("Debug: no remote client to lockdown");
    	    return;
    	}
    	try {
    	    Vector param = new Vector();
    	    rpcClient.execute("woz.lockDown", param);
    	} catch (Exception e) {
    	    e.printStackTrace();
    	}	
    
    }

    private void freedom() {
    	if (rpcClient == null) {
    	    System.out.println("Debug: no remote client to lockdown");
    	    return;
    	}
    	try {
    	    Vector param = new Vector();
    	    rpcClient.execute("woz.freedom", param);
    	} catch (Exception e) {
    	    e.printStackTrace();
    	}	
    
    }
    
    private void changeVariable(int modelIdx, int varIdx, int varValue) {
	if (rpcClient == null) {
	    System.out.println("Debug: no remote client to set vars to " + varValue);
	    return;
	}

	try {
	    Vector param = new Vector();
	    param.add(modelIdx);
	    param.add(varIdx);
	    param.add(varValue);
	    rpcClient.execute("woz.setVariableValue", param);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
    
    private void printVariables(int modelIdx) {
	Vector<Integer> varValues = getVariableValues(modelIdx);
	if (varValues == null || varValues.size() == 0) {
	    System.out.println("No variables in the given tab (or tab does not exist).\n");
	    return;
	}
	
	Vector<String> varNames = getVariableNames(modelIdx);
	if (varNames == null || varNames.size() == 0 || varValues.size() != varValues.size()) {
	    System.out.println("No variables in this tab (or tab does not exist)!\n");
	    return;
	}
	
	System.out.println("Index \tName \tValue");
	for (int i = 0; i < varValues.size(); i++) {
	    String name = varNames.get(i);
	    int value = varValues.get(i).intValue();
	    System.out.println((i+1) + "\t " + name + "\t  " + value);
	}
    }
    
    private void sendIntervention(String msg) {
	if (rpcClient == null) {
	    System.out.println("Debug: no remote client to send '" + msg + "'");
	    return;
	}

	int interruptionLevel = InterventionConstants.INTERRUPTION_MID;
	int interactivityLevel = InterventionConstants.INTERACTION_LOW;
	if (modalButton.isSelected()) {
	    interruptionLevel = InterventionConstants.INTERRUPTION_HIGH;
	    interactivityLevel = InterventionConstants.INTERACTION_LOW;
	} else if (responseButton.isSelected()) {
	    interruptionLevel = InterventionConstants.INTERRUPTION_HIGH;
	    interactivityLevel = InterventionConstants.INTERACTION_MID;	    
	} else if (lowButton.isSelected()) {
	    interruptionLevel = InterventionConstants.INTERRUPTION_MID;
	    interactivityLevel = InterventionConstants.INTERACTION_LOW;	    
	} else if (suggestionButton.isSelected()) {
	    interruptionLevel = InterventionConstants.INTERRUPTION_LOW;
	    interactivityLevel = InterventionConstants.INTERACTION_LOW;	    	    
	}
	
	try {
	    Vector param = new Vector();
	    param.add(interruptionLevel); 
	    param.add(interactivityLevel);
	    param.add(msg);
	    rpcClient.execute("woz.showIntervention", param);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private void setUrl(String url) throws MalformedURLException {
	rpcClient = new XmlRpcClient(url);
    }

    public static void main(String args[]) {
	if (args.length < 1) {
	    System.out.println("USAGE: InterventionFireClient <server_url>");
	    System.exit(0);
	}

	JFrame frame = new JFrame();
	frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	InterventionFireClient client = new InterventionFireClient();
	frame.add(client);

	String url = args[0];
	try {
	    client.setUrl(url);
	} catch (MalformedURLException e) {
	    System.out.println("ERROR: Invalid URL (" + url + ").");
	    System.exit(1);
	}

	client.addMessage("First message");
	client.addMessage("Second message");
	client.addMessage("Third messade.");
	frame.pack();
	frame.setVisible(true);
    }
}

