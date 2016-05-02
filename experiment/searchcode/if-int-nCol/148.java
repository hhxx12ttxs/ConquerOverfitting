/*
 * Licensed Material - Property of NICK 
 * (C) Copyright NICK Corp. 2002 - All Rights Reserved. 
 */
 
package action;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;

/**
 * Class representing the first page of the wizard
 */

public class PrimitiveMainPage extends WizardPage implements Listener
{

	public static final String copyright = "(c) Copyright NICK Corporation 2002.";	
	
	IWorkbench workbench;
	IStructuredSelection selection;
	
	Combo primitiveCombo;
	final static String[] primitives ={ "Callback", "Indirection", "Grouping", "Layers", "Aggregation Cascade", "Composition Cascade", "Shield", "Typing", "Virtual Connector","Connector"};
	
	/**
	 * Constructor for HolidayMainPage.
	 */
	//public PrimitiveMainPage(IWorkbench workbench, IStructuredSelection selection) {
	public PrimitiveMainPage() {
		super("Select Primitive Page");
		setTitle("Select Primitive");
		setDescription("Select the primitive that you would like to create");
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {

	    // create the composite to hold the widgets
		GridData gd;
		Composite composite =  new Composite(parent, SWT.NULL);

	    // create the desired layout for this wizard page
		GridLayout gl = new GridLayout();
		int ncol = 4;
		gl.numColumns = ncol;
		composite.setLayout(gl);
		
	    // create the widgets. If the appearance of the widget is different from the default, 
	    // create a GridData for it to set the alignment and define how much space it will occupy		
	    	    
		
		new Label (composite, SWT.NONE).setText("Primitive to create:");						
		primitiveCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		primitiveCombo.setLayoutData(gd);
		primitiveCombo.setItems(primitives);		
	    // set the composite as the control for this page
		setControl(composite);		
		addListeners();
		
	}
	
	private void addListeners()
	{
		primitiveCombo.addListener(SWT.Selection, this);
	}

	
	/**
	 * @see Listener#handleEvent(Event)
	 */
	public void handleEvent(Event event) {
	    // Initialize a variable with the no error status

	    // Show the most serious error
		getWizard().getContainer().updateButtons();
	}

	/*
	 * Returns the next page.
	 * Saves the values from this page in the model associated 
	 * with the wizard. Initializes the widgets on the next page.
	 */
	
	public IWizardPage getNextPage()
	{    		
		saveDataToModel();
		if (primitiveCombo.getText().equals("Callback")) {
			CallbackPage page = ((AddPrimitiveWizard)getWizard()).callbackPage;
			page.onEnterPage();
			return page;
		} else if (primitiveCombo.getText().equals("Indirection")) {
			IndirectionPage page = ((AddPrimitiveWizard)getWizard()).indirectionPage;
			page.onEnterPage();
			return page;
		} else if (primitiveCombo.getText().equals("Grouping")) {
			GroupingPage page = ((AddPrimitiveWizard)getWizard()).groupingPage;
			page.onEnterPage();
			return page;
		} else if (primitiveCombo.getText().equals("Layers")) {
			LayersPage page = ((AddPrimitiveWizard)getWizard()).layersPage;
			page.onEnterPage();
			return page;
		} else if (primitiveCombo.getText().equals("Aggregation Cascade")) {
			AggregationCascadePage page = ((AddPrimitiveWizard)getWizard()).aggregationCascadePage;
			page.onEnterPage();
			return page;
		}else if (primitiveCombo.getText().equals("Composition Cascade")) {
			CompositionCascadePage page = ((AddPrimitiveWizard)getWizard()).compositionCascadePage;
			page.onEnterPage();
			return page;
		}else if (primitiveCombo.getText().equals("Shield")) {
			ShieldPage page = ((AddPrimitiveWizard)getWizard()).shieldPage;
			page.onEnterPage();
			return page;
		}else if (primitiveCombo.getText().equals("Typing")) {
			TypingPage page = ((AddPrimitiveWizard)getWizard()).typingPage;
			page.onEnterPage();
			return page;
		}else if (primitiveCombo.getText().equals("Virtual Connector")) {
			VirtualConnectorPage page = ((AddPrimitiveWizard)getWizard()).virtualConnectorPage;
			page.onEnterPage();
			return page;
		}else if (primitiveCombo.getText().equals("Connector")) {
			ConnectorPage page = ((AddPrimitiveWizard)getWizard()).connectorPage;
			page.onEnterPage();
			return page;
		}else {
			CallbackPage page = ((AddPrimitiveWizard)getWizard()).callbackPage;
			page.onEnterPage();
			return page;
		}

	}

	/**
	 * @see IWizardPage#canFlipToNextPage()
	 */
	public boolean canFlipToNextPage()
	{
		if(!primitiveCombo.getText().equals("")) {
			return true;
		} else {
			return false;
		}
	}
	
	/*
	 * Saves the uses choices from this page to the model.
	 * Called on exit of the page
	 */
	private void saveDataToModel()
	{
	    // Gets the model
		AddPrimitiveWizard wizard = (AddPrimitiveWizard)getWizard();
		PrimitiveModel model = wizard.model;

		model.primitiveChoice=primitiveCombo.getText();
	}


}


