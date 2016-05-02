/*
 * Licensed Material - Property of IBM 
 * (C) Copyright IBM Corp. 2002 - All Rights Reserved. 
 */

package action;

import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.uml2.uml.Component;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.Stereotype;

/**
 * Wizard page shown when the user has chosen virtual connector 
 * 
 */

public class ConnectorPage extends WizardPage implements Listener 
{
	public static final String copyright = "(c) Copyright NICK Corporation 2002.";
	
	// widgets on this page
	Combo callerChoice,callerPortChoice;
	Combo receiverChoice,receiverPortChoice;
	List componentList,portList,componentList1,portList1;
	final static String[] yesno ={ "Yes", "No" };
	org.eclipse.uml2.uml.Package myPackage;
		
	/**
	 * Constructor for callback page.
	 */
	protected ConnectorPage(String arg0,org.eclipse.uml2.uml.Package myPackage) {
		super(arg0);
		setTitle("Connector");
		setDescription("Select connector options");
		this.myPackage=myPackage;
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {

	    // create the composite to hold the widgets
		GridData gd;
		Composite composite = new Composite(parent, SWT.NONE);

	    // create the desired layout for this wizard page
		GridLayout gl = new GridLayout();
		int ncol = 2;
		gl.numColumns = ncol;
		composite.setLayout(gl);

		new Label (composite, SWT.NONE).setText("New Caller Component?:");
		callerChoice = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		callerChoice.setLayoutData(gd);
		callerChoice.setItems(yesno);
		callerChoice.addListener(SWT.Selection, this);
		//callerChoice.setText(travelDate.getItem(dayOfMonth -1)); // 0 based indexes
		
		new Label (composite, SWT.NONE).setText("Choose caller component if required:");
		componentList = new List(composite, SWT.BORDER | SWT.READ_ONLY  | SWT.V_SCROLL | SWT.H_SCROLL );
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 100;
		
		gd.horizontalSpan =ncol;
		componentList.setLayoutData(gd);
		componentList.addListener(SWT.Selection, this);
		EList<Element> elements = myPackage.getOwnedElements();
		
		for (Element el: elements) {
			if(el instanceof Component) {
				Component comp = (Component) el;
				componentList.add(comp.getName());
			}
		}
		new Label (composite, SWT.NONE).setText("New Caller Port?:");
		callerPortChoice = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		callerPortChoice.setLayoutData(gd);
		callerPortChoice.setItems(yesno);
		callerPortChoice.addListener(SWT.Selection, this);
		
		new Label (composite, SWT.NONE).setText("Choose port if required:");
		portList = new List(composite, SWT.BORDER | SWT.READ_ONLY  | SWT.V_SCROLL | SWT.H_SCROLL );
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 100;
		
		gd.horizontalSpan =ncol;
		portList.setLayoutData(gd);
		portList.addListener(SWT.Selection, this);
		
		new Label (composite, SWT.NONE).setText("New Receiver component?:");
		receiverChoice = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		receiverChoice.setLayoutData(gd);
		receiverChoice.setItems(yesno);
		receiverChoice.addListener(SWT.Selection, this);
		//callerChoice.setText(travelDate.getItem(dayOfMonth -1)); // 0 based indexes
		
		new Label (composite, SWT.NONE).setText("Choose receiver component if required:");
		componentList1 = new List(composite, SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 100;
		gd.horizontalSpan =ncol;
		componentList1.setLayoutData(gd);
		componentList1.addListener(SWT.Selection, this);
		EList<Element> elements1 = myPackage.getOwnedElements();
		
		for (Element el: elements1) {
			if(el instanceof Component) {
				Component comp = (Component) el;
				componentList1.add(comp.getName());
			}
		}
	    // set the composite as the control for this page
		setControl(composite);		
		//setPageComplete(true);
	}

	public boolean canFlipToNextPage()
	{
		// no next page for this path through the wizard
		return false;
	}
	
    /*
     * Process the events: 
     * when the user has entered all information
     * the wizard can be finished
     */
	public void handleEvent(Event e)
	{
		//MessageDialog.openInformation(this.getShell(),"", "Finished");
		int index=0;
		int size=0;
		if(portList.getSelectionCount()>0) {
			index = portList.getSelectionIndex();
			size = portList.getItemCount();
		}
		portList.removeAll();
		if(componentList.getSelectionCount()>0) {
			Component selectedComp =(Component) myPackage.getOwnedType(componentList.getSelection()[0]);
		
			EList<Port> port = selectedComp.getOwnedPorts();
			
			for (Port p: port) {
				portList.add(p.getName());
			}
		}
		if(portList.getSelectionCount()==size) {
			portList.setSelection(index);
		}
		getWizard().getContainer().updateButtons();
		setPageComplete(isPageComplete());
	}
	
	/*
	 * Sets the completed field on the wizard class when all the information 
	 * is entered and the wizard can be completed
	 */	 
	public boolean isPageComplete()
	{
		if((callerChoice.getText().equals("Yes") || (callerChoice.getText().equals("No") && componentList.getSelectionCount()>0)) && (receiverChoice.getText().equals("Yes") || (receiverChoice.getText().equals("No") && componentList1.getSelectionCount()>0))) {
			saveDataToModel();
			AddPrimitiveWizard wizard = (AddPrimitiveWizard)getWizard();
			wizard.canFinish=true;
			return true;
		} else {
			return false;
		}
	}
	
	private void saveDataToModel()
	{
		AddPrimitiveWizard wizard = (AddPrimitiveWizard)getWizard();
		PrimitiveModel model = wizard.model;
		if(callerChoice.getText().equals("Yes")) {
			model.callerConnectorNew=true;
		} else {
			model.callerConnectorNew=false;
			model.callerConnector=(Component) myPackage.getOwnedType(componentList.getSelection()[0]);
		}
		if(receiverChoice.getText().equals("Yes")) {
			model.receiverConnectorNew=true;
		} else {
			model.receiverConnectorNew=false;
			model.receiverConnector=(Component) myPackage.getOwnedType(componentList1.getSelection()[0]);
		}
	}	

	void onEnterPage()
	{
	    // Gets the model
		AddPrimitiveWizard wizard = (AddPrimitiveWizard)getWizard();
		PrimitiveModel model = wizard.model;
	}
}

