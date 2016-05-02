/*
 * Licensed Material - Property of IBM 
 * (C) Copyright IBM Corp. 2002 - All Rights Reserved. 
 */

package action;

import java.util.ArrayList;
import java.util.Iterator;

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
import org.eclipse.uml2.uml.Stereotype;

/**
 * Wizard page shown when the user has chosen indirection 
 * 
 */

public class IndirectionPage extends WizardPage implements Listener 
{
	public static final String copyright = "(c) Copyright NICK Corporation 2002.";
	
	// widgets on this page
	Combo clientChoice,clientAppendChoice;
	Combo indirectorChoice,clientActive;
	Combo targetChoice,targetAppendChoice;
	List componentList,componentList1,componentList2;
	final static String[] yesno ={ "Yes", "No" };
	org.eclipse.uml2.uml.Package myPackage;
		
	/**
	 * Constructor for indirection page.
	 */
	protected IndirectionPage(String arg0,org.eclipse.uml2.uml.Package myPackage) {
		super(arg0);
		setTitle("Indirection");
		setDescription("Select indirection options");
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

		new Label (composite, SWT.NONE).setText("Add Client component?:");
		clientActive = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		clientActive.setLayoutData(gd);
		clientActive.setItems(yesno);
		clientActive.addListener(SWT.Selection, this);
		
		new Label (composite, SWT.NONE).setText("New Client Component?:");
		clientChoice = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		clientChoice.setLayoutData(gd);
		clientChoice.setItems(yesno);
		clientChoice.addListener(SWT.Selection, this);
		
		//callerChoice.setText(travelDate.getItem(dayOfMonth -1)); // 0 based indexes
		
		new Label (composite, SWT.NONE).setText("Choose Client component if required:");
		//new Label (composite, SWT.NONE).setText("Press shift or ctrl to select multiple components");
		componentList = new List(composite,  SWT.BORDER | SWT.READ_ONLY  | SWT.V_SCROLL | SWT.H_SCROLL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan =ncol;
		gd.heightHint = 70;
		componentList.setLayoutData(gd);
		componentList.addListener(SWT.Selection, this);
		EList<Element> elements = myPackage.getOwnedElements();
		
		for (Element el: elements) {
			if(el instanceof Component) {
				Component comp = (Component) el;
				componentList.add(comp.getName());
			}
		}
		
		new Label (composite, SWT.NONE).setText("New Indirector/Proxy component?:");
		indirectorChoice = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		indirectorChoice.setLayoutData(gd);
		indirectorChoice.setItems(yesno);
		indirectorChoice.addListener(SWT.Selection, this);
		
		//callerChoice.setText(travelDate.getItem(dayOfMonth -1)); // 0 based indexes
		
		new Label (composite, SWT.NONE).setText("Choose Indirector/Proxy component if required:");
		componentList1 = new List(composite, SWT.BORDER | SWT.READ_ONLY  | SWT.V_SCROLL | SWT.H_SCROLL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan =ncol;
		gd.heightHint = 70;
		componentList1.setLayoutData(gd);
		componentList1.addListener(SWT.Selection, this);
		EList<Element> elements1 = myPackage.getOwnedElements();
		
		for (Element el: elements1) {
			if(el instanceof Component) {
				Component comp = (Component) el;
				componentList1.add(comp.getName());
			}
		}
		
		new Label (composite, SWT.NONE).setText("New Target component?:");
		targetChoice = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		targetChoice.setLayoutData(gd);
		targetChoice.setItems(yesno);
		targetChoice.addListener(SWT.Selection, this);

		//callerChoice.setText(travelDate.getItem(dayOfMonth -1)); // 0 based indexes
		
		new Label (composite, SWT.NONE).setText("Choose Target component if required:");
		//new Label (composite, SWT.NONE).setText("Press shift or ctrl to select multiple components");
		componentList2 = new List(composite, SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL );
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan =ncol;
		gd.heightHint = 70;
		componentList2.setLayoutData(gd);
		componentList2.addListener(SWT.Selection, this);
		EList<Element> elements2 = myPackage.getOwnedElements();
		
		for (Element el: elements2) {
			if(el instanceof Component) {
				Component comp = (Component) el;
				componentList2.add(comp.getName());
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
		getWizard().getContainer().updateButtons();
		setPageComplete(isPageComplete());
	}
	
	/*
	 * Sets the completed field on the wizard class when all the information 
	 * is entered and the wizard can be completed
	 */	 
	public boolean isPageComplete()
	{
		if(((clientActive.getText().equals("Yes") && (clientChoice.getText().equals("Yes") || (clientChoice.getText().equals("No") && componentList.getSelectionCount()>0))) || clientActive.getText().equals("No")) && (indirectorChoice.getText().equals("Yes") || (indirectorChoice.getText().equals("No") && componentList1.getSelectionCount()>0)) && (targetChoice.getText().equals("Yes") || (targetChoice.getText().equals("No") && componentList2.getSelectionCount()>0))) {
			AddPrimitiveWizard wizard = (AddPrimitiveWizard)getWizard();
			wizard.canFinish=true;
			saveDataToModel();
			
			return true;
		} else {
			return false;
		}
	}
	
	private void saveDataToModel()
	{
		AddPrimitiveWizard wizard = (AddPrimitiveWizard)getWizard();
		PrimitiveModel model = wizard.model;
		if(clientActive.getText().equals("Yes")) {
			model.clientActive=true;
			if(clientChoice.getText().equals("Yes")) {
				model.clientNew=true;
			} else {
				model.clientNew=false;
				model.client = (Component) myPackage.getOwnedType(componentList.getSelection()[0]);
			}
		} else {
			model.clientActive=false;
		}
		if(indirectorChoice.getText().equals("Yes")) {
			model.indirectorNew=true;
		} else {
			model.indirectorNew=false;
			model.indirector=(Component) myPackage.getOwnedType(componentList1.getSelection()[0]);
		}
		if(targetChoice.getText().equals("Yes")) {
			model.targetNew=true;
		} else {
			model.targetNew=false;
			//for (int i=0;i<componentList2.getSelectionCount();i++) {
			//	model.targets.add((Component) myPackage.getOwnedType(componentList2.getSelection()[i]));
			//}
			model.target = (Component)myPackage.getOwnedType(componentList2.getSelection()[0]);
		}
	}	

	void onEnterPage()
	{
	    // Gets the model
		AddPrimitiveWizard wizard = (AddPrimitiveWizard)getWizard();
		PrimitiveModel model = wizard.model;
	}
}

