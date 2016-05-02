/*
 * Licensed Material - Property of IBM 
 * (C) Copyright IBM Corp. 2002 - All Rights Reserved. 
 */

package action;

import java.awt.TextField;

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
 * Wizard page shown when the user has chosen layers 
 * 
 */

public class LayersPage extends WizardPage implements Listener 
{
	public static final String copyright = "(c) Copyright NICK Corporation 2002.";
	
	// widgets on this page
	Combo newPackageChoice,layerNumberChoice;
	List packageList;
	final static String[] layernumbers = {"0","1","2","3","4","5","6","7","8","9","10"};
	final static String[] yesno ={ "Yes", "No" };
	org.eclipse.uml2.uml.Package myPackage;
		
	/**
	 * Constructor for grouping page.
	 */
	protected LayersPage(String arg0,org.eclipse.uml2.uml.Package myPackage) {
		super(arg0);
		setTitle("Layers");
		setDescription("Select Layers options");
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

		new Label (composite, SWT.NONE).setText("New Package for Layer?:");
		newPackageChoice = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		newPackageChoice.setLayoutData(gd);
		newPackageChoice.setItems(yesno);
		newPackageChoice.addListener(SWT.Selection, this);
		//callerChoice.setText(travelDate.getItem(dayOfMonth -1)); // 0 based indexes
		
		new Label (composite, SWT.NONE).setText("Choose package if required:");
		packageList = new List(composite, SWT.BORDER | SWT.READ_ONLY  | SWT.V_SCROLL | SWT.H_SCROLL );
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 100;
		gd.horizontalSpan =ncol;
		packageList.setLayoutData(gd);
		packageList.addListener(SWT.Selection, this);
		EList<org.eclipse.uml2.uml.Package> elements = myPackage.getNestedPackages();
		
		for (Element el: elements) {
			if(el instanceof org.eclipse.uml2.uml.Package) {
				org.eclipse.uml2.uml.Package mypackage = (org.eclipse.uml2.uml.Package) el;
				packageList.add(mypackage.getName());
			}
		}
		
		new Label (composite, SWT.NONE).setText("Layer number of new layer?:");
		layerNumberChoice = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		layerNumberChoice.setLayoutData(gd);
		layerNumberChoice.setItems(layernumbers);
		layerNumberChoice.addListener(SWT.Selection, this);
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
		if((newPackageChoice.getText().equals("Yes") || (newPackageChoice.getText().equals("No") && packageList.getSelectionCount()>0)) ) {
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
		if(newPackageChoice.getText().equals("Yes")) {
			model.layerPackageNew=true;
			model.layerNumber = Integer.valueOf(layerNumberChoice.getText());
		} else {
			model.layerPackageNew=false;
			model.layerPackage=(org.eclipse.uml2.uml.Package) myPackage.getNestedPackage(packageList.getSelection()[0]);
			model.layerNumber = Integer.valueOf(layerNumberChoice.getText());
		}
	}	

	void onEnterPage()
	{
	    // Gets the model
		AddPrimitiveWizard wizard = (AddPrimitiveWizard)getWizard();
		PrimitiveModel model = wizard.model;
	}
}

