<<<<<<< HEAD
package retina.model.database.io;

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import retina.model.database.molecule.category.Channel;
import retina.model.database.molecule.category.Complex;
import retina.model.database.molecule.category.MoleculeCategory;
import retina.model.database.molecule.type.Molecule;

/**
 * Converts an XML document containing information on a molecule to a
 * corresponding Molecule Bean. 
 * 
 * Currently VERY unsafe - category parsing / getMoleculesUnderTags() / getMolecularValuesUnderTags() need to be improved.
 * 
 * @author dylanross
 *
 */
public class XMLToMolecule extends XMLToBeanConverter
{
	public static Molecule convert(String filepath)
	{
		Molecule molecule = new Molecule();

		Element root = getXMLRootElement(filepath);

		// parse names
		NodeList namesList = root.getElementsByTagName("names").item(0).getChildNodes();
		try
		{
			molecule.setNames(getNodeListTextContent(namesList));
		}
		catch (Exception e) { System.out.println("Error parsing names : " + e); }

		// parse description
		molecule.setDescription(root.getElementsByTagName("description").item(0).getTextContent());

		// parse formula
		molecule.setFormula(root.getElementsByTagName("formula").item(0).getTextContent());

		// parse molecular weight
		String molecularWeightString = root.getElementsByTagName("molecular_weight").item(0).getTextContent();
		try
		{
			molecule.setMolecularWeight(Double.parseDouble(molecularWeightString));
		}
		catch (Exception e) { System.out.println("Error parsing molecular weight : " + e); }

		// parse charge
		String chargeString = root.getElementsByTagName("charge").item(0).getTextContent();
		try
		{
			molecule.setCharge(Double.parseDouble(chargeString));
		}
		catch (Exception e) { System.out.println("Error parsing charge : " + e); }

		/**
		 * Parse molecule categories.
		 */

		NodeList categoryList = root.getElementsByTagName("type_information").item(0).getChildNodes(); 			// get NodeList of category information
		int numberCategories = categoryList.getLength();																										// get number of categories

		if (numberCategories != 0)
		{
			ArrayList<MoleculeCategory> categories = new ArrayList<MoleculeCategory>(numberCategories);				// form categories array large enough to hold all categories

			/**
			 * Complex category information.
			 */

			if (root.getElementsByTagName("complex").getLength() != 0) 												// if complex information exists
			{
				Complex complex = new Complex(); 																								// form new complex
				NodeList constituentsList = root.getElementsByTagName("con_molecule");					// find constituent molecules
				try
				{
					complex.setConstituentsStringReference(getNodeListTextContent(constituentsList));			// form String array from XML and set as reference
				}
				catch (Exception e) { System.out.println("Error parsing constituents references : " + e); }
				
				categories.add(complex);
			}

			/**
			 * Channel category information.
			 */

			if (root.getElementsByTagName("channel").getLength() != 0) 																			// if channel information exists
			{
				Channel channel = new Channel(); 																															// form new channel

				NodeList substratesList = root.getElementsByTagName("sub_molecule"); 													// get list of substrate molecules
				channel.setSubstratesStringReference(getNodeListTextContent(substratesList)); 								// form String array from XML and set as reference

				NodeList activatorsList = root.getElementsByTagName("activator");															// get list of activator molecules
				try
				{
					channel.setActivators(getNodeListMolecularValueContent(activatorsList)); 										// convert list to MolecularValue[] and set channel's activators
				}
				catch (Exception e) { System.out.println("Error parsing activators : " + e); }

				NodeList inhibitorsList = root.getElementsByTagName("inhibitor"); 														// get list of inhibitor molecules
				try
				{
					channel.setInhibitors(getNodeListMolecularValueContent(inhibitorsList)); 										// convert list to MolecularValue[] and set channel's inhibitors
				}
				catch(Exception e) { System.out.println("Error parsing inhibitors : " + e); }

				String activationTimeConstantString = root.getElementsByTagName("act_t_const").item(0).getTextContent(); 			// get activation time constant from XML
				try
				{
					channel.setActivationTimeConstant(Double.parseDouble(activationTimeConstantString)); 												// attempt to parse activationTimeConstantString
				}
				catch (Exception e) { System.out.println("Error parsing activation time constant : " + e); }

				String inactivationTimeConstantString = root.getElementsByTagName("inact_t_const").item(0).getTextContent(); 	// get inactivation time constant from XML
				try 
				{
					channel.setInactivationTimeConstant(Double.parseDouble(inactivationTimeConstantString));									  // attempt to parse inactivationTimeConstantString
				}
				catch (Exception e) { System.out.println("Error parsing inactivation time constant : " + e); }

				categories.add(channel);
			}

			molecule.setCategories(categories);
		}

		System.out.println("Molecule parsed : " + molecule);

		return molecule;
	}
=======
package com.barsoft.example.lab2;

public class Complex {
	protected double x;
	protected double y;

	public Complex(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public static Complex Sum(Complex a, Complex b) {
		return new Complex(a.x + b.x, a.y + b.y);
	}

	public static Complex Mul(Complex a, Complex b) {
		return new Complex(a.x * b.x - a.y * b.y, a.x * b.y + a.y * b.x);
	}

	public void Sum(Complex b) {
		Complex c = new Complex(this.x + b.x, this.y + b.y);
		this.x = c.x;
		this.y = c.y;
	}

	public void Mul(Complex b) {
		Complex c = new Complex(this.x * b.x - this.y * b.y, this.x * b.y
				+ this.y * b.x);
		this.x = c.x;
		this.y = c.y;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null) {
			if (obj instanceof Complex) {
				Complex cmp = (Complex) obj;
				if (this.x == cmp.x && this.y == cmp.x) {
					return true;
				}
			} else if (obj instanceof Double) {
				Double dbl = (Double) obj;
				if (this.x == dbl && this.y==0){
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return x + "," + y + "i";
	}

>>>>>>> 76aa07461566a5976980e6696204781271955163
}

