package vivace.view;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.sound.midi.MidiEvent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import vivace.helper.GridHelper;
import vivace.model.Project;

public class EditDialog extends JDialog implements ActionListener, PropertyChangeListener {

	private static final long serialVersionUID = -5465445970465019688L;
	private String typedText = null;
	private JTextField textField;
	private Project model;
	private JOptionPane optionPane;
	private String button1String = "Ok";
	private String button2String = "Cancel";
	private String mode;
	private MidiEvent event;
	
	private int position;
	private int numerator;
	private int denominator;
	
	/**
	 * Returns null if the typed string was invalid;
	 * otherwise, returns the string as the user entered it.
	 */
	public String getValidatedText() {
		return typedText;
	}

	/** 
	 * Creates the reusable dialog. 
	 */
	public EditDialog(Frame frame, Project model, MidiEvent event, String text, String mode, int position) {
		
		super(frame, true);
		
		//Set up the variables
		this.mode = mode;
		this.model = model;
		this.event = event;
		this.position = position;

		//The title of the dialog box
		setTitle("Timesignature Edit");

		//The text that will show up in the edit box when visible
		textField = new JTextField(10);

		textField.setText(text);

		//Create an array of the text and components to be displayed.
		String msgString1 = "Enter a new timesignature";

		Object[] array = {msgString1, textField};

		//Create an array specifying the number of dialog buttons
		//and their text.
		Object[] options = {button1String, button2String};

		//Create the JOptionPane.
		optionPane = new JOptionPane(array,
				JOptionPane.QUESTION_MESSAGE,
				JOptionPane.YES_NO_OPTION,
				null,
				options,
				options[0]);

		//Make this dialog display it.
		setContentPane(optionPane);

		//Handle window closing correctly.
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent event) {

				/*
				 * Instead of directly closing the window,
				 * the JOptionPane's value property is changed
				 */
				optionPane.setValue(new Integer( JOptionPane.CLOSED_OPTION));
			}
		});

		//Ensure the text field always gets the first focus.
		addComponentListener(new ComponentAdapter() {

			public void componentShown(ComponentEvent event) {

				textField.requestFocusInWindow();
			}
		});

		//Register an event handler that puts the text into the option pane.
		textField.addActionListener(this);

		//Register an event handler that reacts to option pane state changes.
		optionPane.addPropertyChangeListener(this);
	}


	/** 
	 * This method handles events for the text field. 
	 */
	public void actionPerformed(ActionEvent e) {

		optionPane.setValue(button1String);
	}


	/** 
	 * This method reacts to state changes in the option pane. 
	 */
	public void propertyChange(PropertyChangeEvent e) {

		String prop = e.getPropertyName();

		// If the dialog is visible AND
		// the source event is the optionpane (with the text) AND
		// the property changed was the text in the optionpane
		if (isVisible() && e.getSource() == optionPane && (JOptionPane.VALUE_PROPERTY.equals(prop) || JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {

			//Get the value in the optionpane
			Object value = optionPane.getValue();

			//The user has still not selected an input
			if (value == JOptionPane.UNINITIALIZED_VALUE) {
				//ignore reset
				return;
			}

			//Reset the JOptionPane's value.
			//This is needed for the property change event
			//to be fired next time the same button is pressed
			optionPane.setValue(

					JOptionPane.UNINITIALIZED_VALUE);

			if (button1String.equals(value)) {

				typedText = textField.getText();

				String ucText = typedText.toUpperCase();

				
				/*
				 * Start the checking of correct input
				 */
				//The input string is 3 characters long and the middle is a "/"  (ex. 4/4)
				if( ucText.length() == 3 && ucText.charAt(1) == '/' &&
						singleValidNomerator(ucText.charAt(0)) && singleValidDenominator(ucText.charAt(2)) ){

					if(mode.equals("EDIT")){
						
						//The input was correct, edit the label and close!
						model.editTimeSignatureEventTrack0(event, numerator, denominator);
					
					} else if (mode.equals("ADD")){
						
						//TODO: Gör n?gra uträkningar!
						
						int ticks = GridHelper.xPositionToTick(GridHelper.getClosestMainBarPosition(position));
						
						model.addTimesignatureEventTrack0(numerator, denominator, ticks);
						
					}
					//TODO: Edit the label
					clearAndHide();
				}

				//The input string is 4 characters long and the second character is a "/"  (ex. 4/16)
				else if( ucText.length() == 4 && ucText.charAt(1) == '/' && 
						singleValidNomerator(ucText.charAt(0)) && multiValidDenominator(ucText.substring(2, 4))){

					if(mode.equals("EDIT")){
						
						//The input was correct, edit the label and close!
						model.editTimeSignatureEventTrack0(event, numerator, denominator);
					
					} else if (mode.equals("ADD")){
						
						//TODO: Gör n?gra uträkningar!
						
						int ticks = GridHelper.xPositionToTick(GridHelper.getClosestMainBarPosition(position));
						
						model.addTimesignatureEventTrack0(numerator, denominator, ticks);
						
					}

					//TODO: Edit the label
					clearAndHide();

				//The input string is 4 characters long and the third character is a "/"  (ex. 16/8)
				} else if( ucText.length() == 4 && ucText.charAt(2) == '/' &&
						multiValidNomerator(ucText.substring(0, 2)) && singleValidDenominator(ucText.charAt(3))){

					if(mode.equals("EDIT")){
						
						//The input was correct, edit the label and close!
						model.editTimeSignatureEventTrack0(event, numerator, denominator);
					
					} else if (mode.equals("ADD")){
						
						//TODO: Gör n?gra uträkningar!
						
						int ticks = GridHelper.xPositionToTick(GridHelper.getClosestMainBarPosition(position));
						
						model.addTimesignatureEventTrack0(numerator, denominator, ticks);
						
					}

					//TODO: Edit the label
					clearAndHide();


				//The input string is 5 characters long and the third character is a "/"  (ex. 16/16)
				} else if( ucText.length() == 5 && ucText.charAt(2) == '/' && 
						multiValidNomerator(ucText.substring(0, 2)) && multiValidDenominator(ucText.substring(3, 5))){

					if(mode.equals("EDIT")){
						
						//The input was correct, edit the label and close!
						model.editTimeSignatureEventTrack0(event, numerator, denominator);
					
					} else if (mode.equals("ADD")){
						
						//TODO: Gör n?gra uträkningar!
				
						int ticks = GridHelper.xPositionToTick(GridHelper.getClosestMainBarPosition(position));
						
						model.addTimesignatureEventTrack0(numerator, denominator, ticks);
						
					}

					//TODO: Edit the label
					clearAndHide();

				} else {

					//Select all text (this look nice!)
					textField.selectAll();

					// TODO: Use CHAMP for this!
					JOptionPane.showMessageDialog(
							EditDialog.this,
							"Sorry, \"" + typedText + "\" "
							+ "isn't a valid timesignature.\n",
							"Try again",
							JOptionPane.ERROR_MESSAGE);
					typedText = null;
					textField.requestFocusInWindow();
				}

			//User closed dialog or clicked cancel
			} else { 

				//DO SOMETHING COOL or just HIDE
				typedText = null;
				clearAndHide();
			}
		}
	}

	/**
	 * Checks if the input is a valid Numerator
	 * Input is a char
	 */
	public boolean singleValidNomerator(char cnemo){

		int nemo = cnemo;

		//Check if input is between 1 and 9
		if( 49 <= nemo &&  nemo <= 59  ){
			
			numerator = Math.abs(48-nemo);
			
			return true;

		} else {
			return false;
		}
	}


	/**
	 * Checks if the input is a valid Denominator
	 * Input is a char
	 */
	public boolean singleValidDenominator(char cdeno){

		int deno = cdeno;

		//Check if input is either 2,4 or 8
		if( deno == 50 || deno == 52 || deno == 56  ){
			
			denominator = Math.abs(48-deno);
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Checks if the input is a valid Nomerator
	 * Input is a string
	 */
	public boolean multiValidNomerator(String snemo){

		int nemo = -1;
		
		try{
			nemo = Integer.parseInt(snemo);
			
		} catch ( NumberFormatException e) {
			
			return false;
		}
		
		//Check if the string i 2 characters long and the value is between 1 and 64
		if( nemo != -1 && snemo.length() == 2 && 1 <= nemo && nemo <= 64  ){
			
			numerator = nemo;
			
			return true;

		}
		
		return false;
		
	}
	
	
	/**
	 * Checks if the input is a valid Denominator
	 * Input is a string
	 */
	public boolean multiValidDenominator(String cdeno){
		
		int deno = -1;
		
		try{
			deno = Integer.parseInt(cdeno);
			
		} catch ( NumberFormatException e) {
			return false;
		}
			
			
		//Check if the string i 2 characters long and the value is between 1 and 64
		if( deno != -1 && cdeno.length() == 2 && 
				(deno == 2 || deno == 4 || deno == 8 || deno == 16 || deno == 32 || deno == 64)){
			
			denominator = deno;
			
			return true;

		}
		
		return false;
	}

	/** This method clears the dialog and hides it. */
	public void clearAndHide() {
		textField.setText(null);
		setVisible(false);
	}
}

