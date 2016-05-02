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
import org.eclipse.swt.widgets.Text;
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

public class ShieldPage extends WizardPage implements Listener 
{
	public static final String copyright = "(c) Copyright NICK Corporation 2002.";
	
	// widgets on this page
	Combo clientChoice,accesspointChoice,protectedActive,protectedChoice,groupChoice;
	List componentList,componentList1,componentList2,packageList;
	final static String[] yesno ={ "Yes", "No" };
	org.eclipse.uml2.uml.Package myPackage;
	Text shieldGroupName;	
	/**
	 * Constructor for indirection page.
	 */
	protected ShieldPage(String arg0,org.eclipse.uml2.uml.Package myPackage) {
		super(arg0);
		setTitle("Shield");
		setDescription("Select shield options");
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

		new Label (composite, SWT.NONE).setText("New Client Component?:");
		clientChoice = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		clientChoice.setLayoutData(gd);
		clientChoice.setItems(yesno);
		clientChoice.addListener(SWT.Selection, this);
		
		//callerChoice.setText(travelDate.getItem(dayOfMonth -1)); // 0 based indexes
		
		new Label (composite, SWT.NONE).setText("Choose client component if required:");
		//new Label (composite, SWT.NONE).setText("Press shift or ctrl to select multiple components");
		componentList = new List(composite,  SWT.BORDER | SWT.READ_ONLY  | SWT.V_SCROLL | SWT.H_SCROLL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan =ncol;
		gd.heightHint = 50;
		componentList.setLayoutData(gd);
		componentList.addListener(SWT.Selection, this);
		EList<Element> elements = myPackage.getOwnedElements();
		
		for (Element el: elements) {
			if(el instanceof Component) {
				Component comp = (Component) el;
				componentList.add(comp.getName());
			}
		}
		
		new Label (composite, SWT.NONE).setText("New Access Point component?:");
		accesspointChoice = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		accesspointChoice.setLayoutData(gd);
		accesspointChoice.setItems(yesno);
		accesspointChoice.addListener(SWT.Selection, this);
		
		//callerChoice.setText(travelDate.getItem(dayOfMonth -1)); // 0 based indexes
		
		new Label (composite, SWT.NONE).setText("Choose access point component if required:");
		componentList1 = new List(composite, SWT.BORDER | SWT.READ_ONLY  | SWT.V_SCROLL | SWT.H_SCROLL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan =ncol;
		gd.heightHint = 50;
		componentList1.setLayoutData(gd);
		componentList1.addListener(SWT.Selection, this);
		EList<Element> elements1 = myPackage.getOwnedElements();
		
		for (Element el: elements1) {
			if(el instanceof Component) {
				Component comp = (Component) el;
				componentList1.add(comp.getName());
			}
		}
		new Label (composite, SWT.NONE).setText("Add Protected component?:");
		protectedActive = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		protectedActive.setLayoutData(gd);
		protectedActive.setItems(yesno);
		protectedActive.addListener(SWT.Selection, this);
		
		new Label (composite, SWT.NONE).setText("New Protected component if required?:");
		protectedChoice = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		protectedChoice.setLayoutData(gd);
		protectedChoice.setItems(yesno);
		protectedChoice.addListener(SWT.Selection, this);

		//callerChoice.setText(travelDate.getItem(dayOfMonth -1)); // 0 based indexes
		
		new Label (composite, SWT.NONE).setText("Choose protected component if required:");
		//new Label (composite, SWT.NONE).setText("Press shift or ctrl to select multiple components");
		componentList2 = new List(composite, SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL );
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan =ncol;
		gd.heightHint = 50;
		componentList2.setLayoutData(gd);
		componentList2.addListener(SWT.Selection, this);
		EList<Element> elements2 = myPackage.getOwnedElements();
		
		for (Element el: elements2) {
			if(el instanceof Component) {
				Component comp = (Component) el;
				componentList2.add(comp.getName());
			}
		}
		new Label (composite, SWT.NONE).setText("New Group Package?:");
		groupChoice = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		groupChoice.setLayoutData(gd);
		groupChoice.setItems(yesno);
		groupChoice.addListener(SWT.Selection, this);

		//callerChoice.setText(travelDate.getItem(dayOfMonth -1)); // 0 based indexes
		
		new Label (composite, SWT.NONE).setText("Choose package if required:");
		//new Label (composite, SWT.NONE).setText("Press shift or ctrl to select multiple components");
		packageList = new List(composite, SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL );
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan =ncol;
		gd.heightHint = 50;
		packageList.setLayoutData(gd);
		packageList.addListener(SWT.Selection, this);
		EList<org.eclipse.uml2.uml.Package> elements3 = myPackage.getNestedPackages();
		
		for (Element el: elements3) {
			if(el instanceof org.eclipse.uml2.uml.Package) {
				org.eclipse.uml2.uml.Package pack= (org.eclipse.uml2.uml.Package) el;
				packageList.add(pack.getName());
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
		if((protectedActive.getText().equals("Yes") || (protectedActive.getText().equals("No"))) && (clientChoice.getText().equals("Yes") || (clientChoice.getText().equals("No") && componentList.getSelectionCount()>0)) && (accesspointChoice.getText().equals("Yes") || (accesspointChoice.getText().equals("No") && componentList1.getSelectionCount()>0)) && ((protectedChoice.getText().equals("Yes") || (protectedChoice.getText().equals("No") && componentList2.getSelectionCount()>0)) || (protectedActive.getText().equals("No"))) && (groupChoice.getText().equals("Yes") || (groupChoice.getText().equals("No") && packageList.getSelectionCount()>0))) {
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
		if(clientChoice.getText().equals("Yes")) {
			model.clietShieldNew=true;
		} else {
			model.clietShieldNew=false;
			model.clientShield = (Component) myPackage.getOwnedType(componentList.getSelection()[0]);
		}
		if(accesspointChoice.getText().equals("Yes")) {
			model.accesspointShieldNew=true;
		} else {
			model.accesspointShieldNew=false;
			model.accessPointShield=(Component) myPackage.getOwnedType(componentList1.getSelection()[0]);
		}
		if(protectedActive.getText().equals("Yes")) {
			model.protectedActive=true;
			if(protectedChoice.getText().equals("Yes")) {
				model.protectedShieldNew=true;
			} else {
				model.protectedShieldNew=false;
				model.protectedShield = (Component)myPackage.getOwnedType(componentList2.getSelection()[0]);
			}
		} else {
			model.protectedActive=false;
		}
		if(groupChoice.getText().equals("Yes")) {
			model.groupShieldNew=true;
		} else {
			model.groupShieldNew=false;
			model.groupShield=(org.eclipse.uml2.uml.Package) myPackage.getNestedPackage(packageList.getSelection()[0]);
		}
	}	

	void onEnterPage()
	{
	    // Gets the model
		AddPrimitiveWizard wizard = (AddPrimitiveWizard)getWizard();
		PrimitiveModel model = wizard.model;
	}
}

