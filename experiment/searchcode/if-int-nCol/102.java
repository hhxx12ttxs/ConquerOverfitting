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

public class OldPatternMainPage extends WizardPage implements Listener
{

	public static final String copyright = "(c) Copyright NICK Corporation 2002.";	
	
	IWorkbench workbench;
	IStructuredSelection selection;
	org.eclipse.uml2.uml.Package myPackage;
	
	Combo patternCombo;
	Text details;
	List componentlist;
	final static String[] patterns ={ "pipe-filter","client-server","layers"};
	String[] comboitems;
	ArrayList componentitems;
	EList<Element> elements;
	ArrayList current;
	
	/**
	 * Constructor for HolidayMainPage.
	 */
	//public PrimitiveMainPage(IWorkbench workbench, IStructuredSelection selection) {
	public OldPatternMainPage(org.eclipse.uml2.uml.Package myPackage) {
		super("Select Component Page");
		setTitle("objects");
		setDescription("Select the object that you would like to change");
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
	    	    
		
		new Label (composite, SWT.NONE).setText("select object:");						
		patternCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		//componentlist = new List(SWT.BORDER | SWT.READ_ONLY);
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		patternCombo.setLayoutData(gd);
		new Label (composite, SWT.NONE).setText("details:");
		details = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
	    details.setLayoutData(new GridData(GridData.FILL_BOTH));
		getElementsOnScreen();
		patternCombo.setItems((String []) componentitems.toArray (new String [componentitems.size ()]));
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
		System.out.println("does it work?");
		try
		{
		   System.out.println("recource? ");
		   System.out.println("recource: "+myPackage);
		   //EList<Namespace> name = myPackage.allNamespaces();
		   
		   comboitems = new String[elements.size()];
		   componentitems = new ArrayList(1);
		   current = new ArrayList(1);
		   String StringFillValue = "empty";
		   Arrays.fill(comboitems, StringFillValue);
		   for (int i = 0; i<elements.size();i++) 
		   {
			   EObject temp = elements.get(i);
				     if (temp instanceof NamedElement)
				     {
				    	current.add(">"+temp);
				    	 NamedElement asNamed = (NamedElement) temp;
				     System.out.println(asNamed.allOwnedElements());
				     //  + " : " + asNamed.eClass().getName());
				     //comboitems[i] = asNamed.getName() + " : " + asNamed.eClass().getName();
				       //System.out.println("loop: "+current);
				       if(asNamed.getName()==null)
				       {
				    	   componentitems.add(""+asNamed.eClass().getName());
				       }
				       else
				       {
				    	   componentitems.add(asNamed.getName() + " : " + asNamed.eClass().getName());
				       }
				     }
			}
		  /* for (
		     TreeIterator<EObject> i = resource.getAllContents();
		     i .hasNext();
		   ) {
		     EObject current = i.next();
		     //if (!(current instanceof NamedElement))
		       i.prune();
		     //NamedElement asNamed = (NamedElement) current;
		     //System.out.println(asNamed.getQualifiedName()
		     //  + " : " + asNamed.eClass().getName());
		       System.out.println("loop: "+current);
		   } */
		   //System.out.println("did it reach here?");
		   //return comboitems;
		}
		catch(Exception e) {e.printStackTrace();}
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
		showDetails(patternCombo.getSelectionIndex());
	    // Show the most serious error
		getWizard().getContainer().updateButtons();
	}
	
	

	/*
	 * Returns the next page.
	 * Saves the values from this page in the model associated 
	 * with the wizard. Initializes the widgets on the next page.
	 */
	
	/*public IWizardPage getNextPage()
	{    		
		saveDataToModel();
		if (patternCombo.getText().equals("Callback")) {
			CallbackPage page = ((AddPrimitiveWizard)getWizard()).callbackPage;
			page.onEnterPage();
			return page;
		} else if (patternCombo.getText().equals("Indirection")) {
			IndirectionPage page = ((AddPrimitiveWizard)getWizard()).indirectionPage;
			page.onEnterPage();
			return page;
		} else if (patternCombo.getText().equals("Grouping")) {
			GroupingPage page = ((AddPrimitiveWizard)getWizard()).groupingPage;
			page.onEnterPage();
			return page;
		} else if (patternCombo.getText().equals("Layers")) {
			LayersPage page = ((AddPrimitiveWizard)getWizard()).layersPage;
			page.onEnterPage();
			return page;
		} else if (patternCombo.getText().equals("Aggregation Cascade")) {
			AggregationCascadePage page = ((AddPrimitiveWizard)getWizard()).aggregationCascadePage;
			page.onEnterPage();
			return page;
		}else if (patternCombo.getText().equals("Composition Cascade")) {
			CompositionCascadePage page = ((AddPrimitiveWizard)getWizard()).compositionCascadePage;
			page.onEnterPage();
			return page;
		}else if (patternCombo.getText().equals("Shield")) {
			ShieldPage page = ((AddPrimitiveWizard)getWizard()).shieldPage;
			page.onEnterPage();
			return page;
		}else if (patternCombo.getText().equals("Typing")) {
			TypingPage page = ((AddPrimitiveWizard)getWizard()).typingPage;
			page.onEnterPage();
			return page;
		}else if (patternCombo.getText().equals("Virtual Connector")) {
			VirtualConnectorPage page = ((AddPrimitiveWizard)getWizard()).virtualConnectorPage;
			page.onEnterPage();
			return page;
		}else if (patternCombo.getText().equals("Connector")) {
			ConnectorPage page = ((AddPrimitiveWizard)getWizard()).connectorPage;
			page.onEnterPage();
			return page;
		}else {
			CallbackPage page = ((AddPrimitiveWizard)getWizard()).callbackPage;
			page.onEnterPage();
			return page;
		}

	}*/

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
		AddPrimitiveWizard wizard = (AddPrimitiveWizard)getWizard();
		PrimitiveModel model = wizard.model;

		model.primitiveChoice=patternCombo.getText();
	}


}


