/*
 * Licensed Material - Property of NICK 
 * (C) Copyright NICK Corp. 2002 - All Rights Reserved. 
 */
 
package action;

import java.awt.List;
import java.awt.TextArea;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.*;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
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
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.uml2.uml.Component;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Namespace;

/**
 * Class representing the first page of the wizard
 */

public class PatternMainPage extends WizardPage implements Listener
{

	public static final String copyright = "(c) Copyright NICK Corporation 2002.";	
	
	IWorkbench workbench;
	IStructuredSelection selection;
	org.eclipse.uml2.uml.Package myPackage;
	
	Combo patternCombo;
	Text details;
	List componentlist;
	final static String[] patterns ={ "Model-View-Control","pipe-filter","client-server","layers"};
	String[] comboitems;
	ArrayList componentitems;
	EList<Element> elements;
	ArrayList current;
	
	/**
	 * Constructor for HolidayMainPage.
	 */
	//public PrimitiveMainPage(IWorkbench workbench, IStructuredSelection selection) {
	public PatternMainPage(org.eclipse.uml2.uml.Package myPackage) {
		super("Select Pattern Page");
		setTitle("select primitive");
		setDescription("Select the patterm you want to create");
		this.myPackage=myPackage;
		elements = myPackage.allOwnedElements();
		//this.comboitems=comboitems;
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
		int ncol = 2;
		gl.numColumns = ncol;
		composite.setLayout(gl);
		
	    // create the widgets. If the appearance of the widget is different from the default, 
	    // create a GridData for it to set the alignment and define how much space it will occupy		
	    	    
		
		new Label (composite, SWT.NONE).setText("select pattern:");						
		patternCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		//componentlist = new List(SWT.BORDER | SWT.READ_ONLY);
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		patternCombo.setLayoutData(gd);
		new Label (composite, SWT.NONE).setText("pattern:");
		details = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
	    details.setLayoutData(new GridData(GridData.FILL_BOTH));
		getElementsOnScreen();
		String[] profiles ={ "callback", "shield", "virtual connector" };
		patternCombo.setItems(patterns);
	    // set the composite as the control for this page
		setControl(composite);		
		addListeners();
		
	}
	
	private void addListeners()
	{
		patternCombo.addListener(SWT.Selection, this);
	}

	public void getElementsOnScreen()
	{
		
		comboitems = new String[elements.size()];
		  
	}
	public void showDetails(int index) 
	{
		details.setText("");
		String cur = (String)current.get(index);
		details.setText(cur);
	}
	/**
	 * @see Listener#handleEvent(Event)
	 */
	public void handleEvent(Event event) 
	{
	    // Initialize a variable with the no error status
		//System.out.println("did an event occur?! >> "+event);
		//showDetails(patternCombo.getSelectionIndex());
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
		//saveDataToModel();
		if (patternCombo.getText().equals("Model-View-Control")) {
			MVCProfilePage page = ((AddPatternWizard)getWizard()).MVCprofilepage;
			page.onEnterPage();
			return page;
		}
		else if (patternCombo.getText().equals("")) {
			MVCProfilePage page = ((AddPatternWizard)getWizard()).MVCprofilepage;
			page.onEnterPage();
			return page;
		}
		else
		{
			MVCProfilePage page = ((AddPatternWizard)getWizard()).MVCprofilepage;
			page.onEnterPage();
			return page;
		}

	}

	/**
	 * @see IWizardPage#canFlipToNextPage()
	 */
	public boolean canFlipToNextPage()
	{
		if(!patternCombo.getText().equals("")) {
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
		AddPatternWizard wizard = (AddPatternWizard)getWizard();
		PrimitiveModel model = wizard.model;

		model.primitiveChoice=patternCombo.getText();
	}


}


