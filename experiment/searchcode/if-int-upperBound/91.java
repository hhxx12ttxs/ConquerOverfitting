package eaadapter.ea2ecore;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;

import eaadapter.EAAttribute;
import eaadapter.EAConnector;
import eaadapter.EAElement;
import eaadapter.EAPackage;
import eaadapter.EARepository;
import eaadapter.abstracthierachy.EANamedElement;
import eaadapter.abstracthierachy.EAStereotypedElement;
import eaadapter.abstracthierachy.EATaggedElement;
import eaadapter.datatypes.T_ConnectorAggregation;

/**
 * <copyright>
 * 
 * Copyright (c) Continental AG and others.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 
 * which accompanies this distribution, and is
 * available at http://www.eclipse.org/org/documents/epl-v10.php
 * 
 * Contributors: 
 *     Continental AG - Initial API and implementation
 * 
 * </copyright>
 */


public class EcoreMapperImpl extends EcoreMapperTemplate {

	public EcoreMapperImpl(EARepository r) {
		super(r);
	}
	
	public EPackage execute() throws Exception {
		createEcore();
		return model;
	}

	/**
	 * <code>createEcore</code> orchestrates the Ecore generation.
	 * At the beginnen, an <code>EPackage</code> is created which acts as root package
	 * for the subsequent mapping.
	 * @throws Exception
	 */
	private void createEcore() throws Exception {
		model = ecoreFactory.createEPackage();
		model.setName("Generated Ecore");
		model.setNsPrefix("Generated Ecore");
		model.setNsURI("http://generated-ecore");
		
		createPackages();
		
		createClassifier();
		
		createTypedElements();		
	}

	private void createPackages() {
		createSubpackagesForPackage(model, repository.getModels());
	}
	
	private void createSubpackagesForPackage(EPackage model, EList<EAPackage> eaPackages) {
		for (EAPackage pkg : eaPackages) {
			EPackage p = createEPackageFromEAPackage(pkg);						
			model.getESubpackages().add(p);
			createSubpackagesForPackage(p, pkg.getPackages());
		}		
	}	
	
	private EPackage createEPackageFromEAPackage(EAPackage eaPackage) {
		EPackage pkg = ecoreFactory.createEPackage();
		pkg.setName(eaPackage.getName());
		pkg.setNsPrefix(eaPackage.getName());
		pkg.setNsURI(createNsURI(eaPackage));
		pkg.getEAnnotations().addAll(createAnnotationsForEANamedElement(eaPackage));
		return pkg;
	}	
		
	private void createTypedElements() throws Exception {
		createAttributes();
		createReferences();		
	}
	
	/**
	 * General: An <code>EAConnector</code> specifies an reference between two <code>EAElements</code>:
	 * <i>client</i> (source) and <i>supplier</i> (target).<br><br>
	 * Thus, an EAConnector has an client and a supplier. The 
	 * direction of an association is (usually) from source to target.
	 * The type of association is defined by the <code>type</code>-attribute.
	 * @throws Exception
	 */
	
	private void createReferences() throws Exception {
		for (TreeIterator<EANamedElement> iter = EcoreUtil.getAllContents(repository.getModels()); iter.hasNext();) {
			EANamedElement element = (EANamedElement) iter.next();
			if (element instanceof EAConnector) {
				EAConnector eaConnector = (EAConnector) element;
								
				if (eaConnector.getType().equals("Association"))
					createAssociation(eaConnector);
				else if (eaConnector.getType().equals("Generalization"))
					createGeneralization(eaConnector);
				else if (eaConnector.getType().equals("Aggregation"))
					createAggregation(eaConnector);
				else if (eaConnector.getType().equals("Dependency"))
					System.out.println("Skipping irrelevant connector of type 'Dependency'");
				else if (eaConnector.getType().equals("NoteLink")) {
					System.out.println("Skipping irrelevant connector of type 'NoteLink'");
				}
				else 
					System.out.println("Warnung: connector type '" + eaConnector.getType() + "' is not implemented.");
			}
		}			
	}

	/**
	 * 
	 * At "aggregation" references, the direction <code>client</code> --> <code>supplier</code> is sometimes
	 * swapped, e.g. the black-diamond (indicates an aggregation) which should be attached to the <code>client</code> is attachted to the <code>supplier</code>.<br>	  
	 *  Therefore, we have to determine on which <code>connectorEnd</code> the black diamond
	 * <code>(Aggregation == COMPOSITE)</code> is.<br><br> The end on which the black-diamond is,
	 * will be treated as new <code>client</code>, the other side as <code>supplier</code>.
	 * @param eaConnector
	 * @throws Exception
	 */
	private void createAggregation(EAConnector eaConnector) throws Exception {
		EClass client = findEClassByID(eaConnector.getClientID());
		EClass supplier = findEClassByID(eaConnector.getSupplierID());
		
		if(client == null){
			System.out.println("Warning: Client is null - aggregation not created.");
		}
		if(supplier == null){
			System.out.println("Warning: Supplier is null - aggregation not created");
		}
	
		if (client == null || supplier == null)
		{
			return;
		}
				
		/**
		 * Case 1: Direction is okay. Black diamond is attached to the client!
		 */
		
		if (eaConnector.getClientEnd().getAggregation() == T_ConnectorAggregation.COMPOSITE) {
			EReference reference = ecoreFactory.createEReference();
			reference.setContainment(true);
			reference.setName(eaConnector.getSupplierEnd().getRole());
			reference.setDerived(eaConnector.isDerived());
			int lowerBound = createLowerBound(eaConnector.getSupplierEnd().getCardinality());
			int upperBound = createUpperBound(eaConnector.getSupplierEnd().getCardinality());
			reference.setLowerBound(lowerBound);
			reference.setUpperBound(upperBound);
			reference.setEType(supplier);
			reference.getEAnnotations().addAll(createAnnotationsForEAConnector(eaConnector));		
			client.getEStructuralFeatures().add(reference);		
			
			/**
			 * create opposite Reference only if a cardinality for the opposite direction
			 * is specified
			 */
			if (eaConnector.getClientEnd().getCardinality().length() > 0) {
				EReference opposite = ecoreFactory.createEReference();
				String oppositeName;
				if (eaConnector.getClientEnd().getRole().length() > 0)
					oppositeName = eaConnector.getClientEnd().getRole();
				else
					oppositeName = client.getName() + "_" + reference.getName();
				
				opposite.setName(oppositeName);
				opposite.setDerived(eaConnector.isDerived());
				opposite.setLowerBound(createLowerBound(eaConnector.getClientEnd().getCardinality()));
				opposite.setUpperBound(createUpperBound(eaConnector.getClientEnd().getCardinality()));
				opposite.setEType(client);
				supplier.getEStructuralFeatures().add(opposite);
				
				opposite.setEOpposite(reference);
				reference.setEOpposite(opposite);
			}
			
		} else if (eaConnector.getSupplierEnd().getAggregation() == T_ConnectorAggregation.COMPOSITE) {			
			/**
			 * Case 2: Black diamond is attached to the supplier! 
			 * We have to swap client and supplier.
			 */
			EReference ref = ecoreFactory.createEReference();
			ref.setContainment(true);
			ref.setDerived(eaConnector.isDerived());
			ref.setName(eaConnector.getClientEnd().getRole());
			int lowerBound = createLowerBound(eaConnector.getClientEnd().getCardinality());
			int upperBound = createUpperBound(eaConnector.getClientEnd().getCardinality());
			ref.setLowerBound(lowerBound);
			ref.setUpperBound(upperBound);
			ref.setEType(client);
			ref.getEAnnotations().addAll(createAnnotationsForEAConnector(eaConnector));									
			supplier.getEStructuralFeatures().add(ref);	
			
			/**
			 * create opposite Reference only if a cardinality for the opposite direction
			 * is specified
			 */
			if (eaConnector.getSupplierEnd().getCardinality().length() > 0) {
				EReference opposite = ecoreFactory.createEReference();
				String oppositeName;
				if (eaConnector.getSupplierEnd().getRole().length() > 0)
					oppositeName = eaConnector.getSupplierEnd().getRole();
				else
					oppositeName = supplier.getName() + "_" + ref.getName();
				
				opposite.setName(oppositeName);
				opposite.setDerived(eaConnector.isDerived());
				opposite.setLowerBound(createLowerBound(eaConnector.getSupplierEnd().getCardinality()));
				opposite.setUpperBound(createUpperBound(eaConnector.getSupplierEnd().getCardinality()));
				opposite.setEType(supplier);
				client.getEStructuralFeatures().add(opposite);
				
				opposite.setEOpposite(ref);
				ref.setEOpposite(opposite);	
			}
		}
		
	}	
		

	private void createAssociation(EAConnector eaConnector) throws Exception {
		EClass client = findEClassByID(eaConnector.getClientID());
		EClass supplier = findEClassByID(eaConnector.getSupplierID());
		
		if(client == null){
			System.out.println("Warning: Client is null - association not created.");
		}
		if(supplier == null){
			System.out.println("Warning: Supplier is null - association not created");
		}
		
		if (client == null || supplier == null)
		{
			return;
		}
				
		EReference reference = ecoreFactory.createEReference();
		reference.setName(eaConnector.getSupplierEnd().getRole());
		reference.setDerived(eaConnector.isDerived());
		int lowerBound = createLowerBound(eaConnector.getSupplierEnd().getCardinality());
		int upperBound = createUpperBound(eaConnector.getSupplierEnd().getCardinality());
		reference.setLowerBound(lowerBound);
		reference.setUpperBound(upperBound);
		reference.setEType(supplier);
		reference.getEAnnotations().addAll(createAnnotationsForEAConnector(eaConnector));		
		client.getEStructuralFeatures().add(reference);
		
		/**
		 * Create Opposite Reference if Multiplicity is specified
		 */
		if (eaConnector.getClientEnd().getCardinality().length() > 0) {
			EReference opposite = ecoreFactory.createEReference();
			String oppositeName = "";
			if (eaConnector.getClientEnd().getRole().length() > 0) {
				oppositeName = eaConnector.getClientEnd().getRole();
			} else {
				oppositeName = client.getName() + "_" + reference.getName();
			}
			opposite.setName(oppositeName);
			int lowerBoundOpposite = createLowerBound(eaConnector.getClientEnd().getCardinality());
			int upperBoundOpposite = createUpperBound(eaConnector.getClientEnd().getCardinality());
			opposite.setLowerBound(lowerBoundOpposite);
			opposite.setUpperBound(upperBoundOpposite);
			opposite.setEType(client);
			supplier.getEStructuralFeatures().add(opposite);
			
			opposite.setEOpposite(reference);
			reference.setEOpposite(opposite);			
		}
	}
	
	
	private void createGeneralization(EAConnector eaConnector) throws Exception {
		EClass client = findEClassByID(eaConnector.getClientID());
		EClass supplier = findEClassByID(eaConnector.getSupplierID());

		if(client == null){
			System.out.println("Warning: Client is null - generalization not created.");
		}
		if(supplier == null){
			System.out.println("Warning: Supplier is null - generalization not created");
		}
		
		if (client == null || supplier == null)
		{
			return;
		}

		
		client.getESuperTypes().add(supplier);
	}	
		
	private void createAttributes() throws Exception {
		for (TreeIterator<EObject> iter = EcoreUtil.getAllContents(model.getESubpackages()); iter.hasNext();) {
			EObject element = (EObject) iter.next();
			if (element instanceof EClass) {
				EClass eClass = (EClass) element;
				EList<EAttribute> attributes = createAttributesForEClass(eClass);
				eClass.getEStructuralFeatures().addAll(attributes);
			}
		}		
	}
	
	private EList<EAttribute> createAttributesForEClass(EClass eClass) throws Exception {
		EAElement eaElement = findEAElementForEClass(eClass);
		EList<EAttribute> attributes = new BasicEList<EAttribute>();
		for (EAAttribute eaAttribute : eaElement.getAttributes()) {
			try {
				EAttribute newEAttribute = createEAttributeForEAAttribute(eaAttribute);
				if ( newEAttribute != null) {
					attributes.add(newEAttribute);
				}
			} catch (Exception e) {
				logger.error(e.getMessage());
				logger.error("Attribute: " + eaAttribute.getName());
				logger.error("Type: " + eaAttribute.getType());
				logger.error("Class: " + eClass.getName());
				logger.error("Package: " + createPathOfEAPackage(eaElement.getPackage()) + eaElement.getPackage().getName());
				throw e;
			}
		}
		return attributes;
	}

	private EAttribute createEAttributeForEAAttribute(EAAttribute eaAttribute) throws Exception {
			EAttribute attribute = ecoreFactory.createEAttribute();
			attribute.setName(eaAttribute.getName());
			EClassifier eType = findEClassifier(eaAttribute.getClassifierID(), eaAttribute.getType());
//			If a classifier is not set, the attribute is not created as it is useless.
			if (eType == null) {
				System.out.println("Warning: the Classifier '" + eaAttribute.getType() + "' for attribute '"+eaAttribute.getName()+"' (id: " + eaAttribute.getClassifierID() + ") not set!");
				return null;
			}
			attribute.setEType(eType);
			attribute.setOrdered(eaAttribute.getOrdered());
			attribute.setLowerBound(createLowerBound(eaAttribute.getLowerBound()));
			attribute.setUpperBound(createUpperBound(eaAttribute.getUpperBound()));
			attribute.getEAnnotations().addAll(createAnnotationsForEAttribute(eaAttribute));
			return attribute;
	}

	private void createClassifier() {
		for (EPackage pkg : model.getESubpackages()) {
			createClassifierForEPackage(pkg);
		}
	}
	
	
	/**
	 * creates the classifiers for the specified <code>EPackage</code><br><br>
	 * 
	 * We have to consider 3 cases, based on the <code>EAPackage</code>'s <code>stereotype</code>:
	 * <ul>
	 * <li><code>enumeration</code> --> create an <code>EEnum</code></li>
	 * <li><code>primitive</code> --> create an <code>EDataType</code>
	 * <li><code>EClass</code> otherwise 
	 * </ul>
	 *  
	 * @param pkg
	 */
	private void createClassifierForEPackage(EPackage pkg) {
		EAPackage correspondingEAPackage = findEAPackageForEPackage(pkg);
		
		for (EAElement eaElement : correspondingEAPackage.getElements()) {
			/* 
			 * Skip elements with MetaType "Boundary", "Text" and "Note"
			 * because they are useless in the Ecore class model
			 */
			if (eaElement.getMetaType().equals("Boundary") || eaElement.getMetaType().equals("Text") || eaElement.getMetaType().equals("Note")) {
				continue;
			}			
			if (eaElement.getStereotype().equals("enumeration")) {
				EEnum eEEnum = createEEnumFromEAElement(eaElement);
				pkg.getEClassifiers().add(eEEnum);
			} else if (eaElement.getStereotype().equals("primitive")) {
				EDataType eDatatype = createEDataTypeFromEAElement(eaElement);
				pkg.getEClassifiers().add(eDatatype);
			} else {
				EClass eClass = createEClassFromEAElement(eaElement);
				pkg.getEClassifiers().add(eClass);
			}
		}
		
		//recursive call for all packages
		for (EPackage ePackage : pkg.getESubpackages())
			createClassifierForEPackage(ePackage);
	}
	
	
	/**
	 * Creates an <code>EEnum</code> for the <code>EAElement</code><br>
	 * the <code>EAElement</code>'s attributes are treated as their literals.<br><br>
	 * the literal's values are determined by a counter, starting at 0 and increments by 1.
	 * <br>
	 * E.g.
	 * <ul>
	 *  <li>literal 1 has value <code>0</code></li>
	 *  <li>literal 2 has value <code>1</code></li>
	 *  <li>literal 3 has value <code>2</code></li>
	 *  <li>...</li>
	 * </ul>
	 * @param eaElement
	 * @return
	 */
	private EEnum createEEnumFromEAElement(EAElement eaElement) {
		EEnum eEEnum = ecoreFactory.createEEnum();
		eEEnum.setName(eaElement.getName());
		int literalValue = 0;
		for (EAAttribute eaLiteral : eaElement.getAttributes()) {
			EEnumLiteral literal = ecoreFactory.createEEnumLiteral();
			literal.setName(eaLiteral.getName());
			literal.setLiteral(eaLiteral.getName());
			literal.setValue(literalValue++);
			eEEnum.getELiterals().add(literal);
		}
				
		EList<EAnnotation> annotations = createAnnotationsForEAElement(eaElement);
		eEEnum.getEAnnotations().addAll(annotations);		
		return eEEnum;
	}
	


	private EClass createEClassFromEAElement(EAElement eaElement) {
		EClass eClass = ecoreFactory.createEClass();
		eClass.setAbstract(eaElement.isIsAbstract());
		eClass.setName(eaElement.getName());
		EList<EAnnotation> annotations = createAnnotationsForEAElement(eaElement);
		eClass.getEAnnotations().addAll(annotations);
		return eClass;
	}

	private EDataType createEDataTypeFromEAElement(EAElement eaElement) {
		EDataType eDataType = ecoreFactory.createEDataType();
		eDataType.setName(eaElement.getName());
		eDataType.setInstanceTypeName(eaElement.getName());
		EList<EAnnotation> annotations = createAnnotationsForEAElement(eaElement);
		eDataType.getEAnnotations().addAll(annotations);
		return eDataType;
	}	

	public EPackage getGeneratedEcore() {
		return model;
	}
	
	private EList<EAnnotation> createAnnotationsForEANamedElement(EANamedElement element) {
		EList<EAnnotation> annotations = new BasicEList<EAnnotation>();
		
		EAnnotation metaData = ecoreFactory.createEAnnotation();
		metaData.setSource("MetaData");
		metaData.getDetails().put("guid", element.getGuid());
		metaData.getDetails().put("id", String.valueOf(element.getId()));
		metaData.getDetails().put("EA name", element.getName());
		annotations.add(metaData);
		
		if (element.getNotes() != null && element.getNotes().length() > 0) {
			EAnnotation genmodel = ecoreFactory.createEAnnotation();
			genmodel.setSource("http://www.eclipse.org/emf/2002/GenModel");
			genmodel.getDetails().put("documentation", element.getNotes());
			annotations.add(genmodel);
		}
		
		return annotations;
	}
	
	private EList<EAnnotation> createAnnotationsForEAStereoTypedElement(EAStereotypedElement element) {
		EList<EAnnotation> annotations = createAnnotationsForEANamedElement(element);
		if (element.getStereotype() != null && element.getStereotype().length() > 0) {
			EAnnotation stereotype = ecoreFactory.createEAnnotation();
			stereotype.setSource("Stereotype");
			
			String stereotypeValue = element.getStereotype();
			if (element.getStereotypeEx().length() > element.getStereotype().length())
				stereotypeValue = element.getStereotypeEx();
			
			stereotype.getDetails().put("Stereotype", stereotypeValue);			

			annotations.add(stereotype);
		}		
		return annotations;
	}
	
	private EList<EAnnotation> createAnnotationsForEAElement(EAElement element) {
		EList<EAnnotation> annotations = createAnnotationsForEAStereoTypedElement(element);
		if (element.getTaggedValues().size() > 0)
			annotations.add(createAnnotationForEATaggedElements(element.getTaggedValues()));
		
		return annotations;
	}
	
	private EList<EAnnotation> createAnnotationsForEAttribute(EAAttribute element) {
		EList<EAnnotation> annotations = createAnnotationsForEAStereoTypedElement(element);
		
		if (element.getTaggedValues().size() > 0)
			annotations.add(createAnnotationForEATaggedElements(element.getTaggedValues()));

		return annotations;
	}	
	
	private EList<EAnnotation> createAnnotationsForEAConnector(EAConnector element) {
		EList<EAnnotation> annotations = createAnnotationsForEAStereoTypedElement(element);
		
		if (element.getTaggedValues().size() > 0)
			annotations.add(createAnnotationForEATaggedElements(element.getTaggedValues()));
		
		return annotations;
	}	
		
	private EAnnotation createAnnotationForEATaggedElements(EList<? extends EATaggedElement> taggedElement) {
		EAnnotation taggedAnnotations = ecoreFactory.createEAnnotation();
		taggedAnnotations.setSource("TaggedValues");
		for (EATaggedElement tv : taggedElement) {
			taggedAnnotations.getDetails().put(tv.getName() , tv.getValue());
		}
		return taggedAnnotations;
	}	

	private String createNsURI(EAPackage pkg) {
		String path = createPathOfEAPackage(pkg);
		return "http://" + path + pkg.getName();
	}
	
	/**
	 * The method <code>findEClassifier</code> returns the Classifier identified
	 * by the <code>classifierID</code> and <code>type</code>.
	 * <br><br>
	 * In Enterprise Architect the classifier is identified by either
	 * <code>classifierID</code> or <code>type</code>.
	 * <br><br>
	 * If type is a simple type like <code>String</code> or <code>Boolean</code>
	 * the <code>classifierID</cod> is 0. If the requested classifier
	 * is not a simple type, <code>classifierID</code> is > 0 and we have to search it.
	 * 
	 * @param classifierID
	 * @param type
	 * @return
	 * @throws Exception 
	 */
		private EClassifier findEClassifier(int classifierID, String type) throws Exception {
			//if (classifierID == 0) { /** TODO: this fixes Problem 1) */
			if (type.equals("String"))
				return EcorePackage.Literals.ESTRING;
			if (type.equals("Boolean"))
				return EcorePackage.Literals.EBOOLEAN;
			if (type.equals("Float"))
				return EcorePackage.Literals.EFLOAT;
			if (type.equals("Double"))
				return EcorePackage.Literals.EDOUBLE;
			if (type.equals("Char"))
				return EcorePackage.Literals.ECHAR;
			if (type.equals("Integer"))
				return EcorePackage.Literals.EINT;
		//}		
		
		for (TreeIterator<EObject> iter = EcoreUtil.getAllContents(model.getESubpackages()); iter.hasNext();) {
			EObject element = (EObject) iter.next();
			if (element instanceof EClassifier) {
				EClassifier classifier = (EClassifier) element;
				int id = Integer.valueOf(classifier.getEAnnotation("MetaData").getDetails().get("id"));
				if (classifierID == id)
					return classifier;
			}
		}	
		
		/*
		 * If we haven't found a Classifier yet, try to find it by name
		 */
		for (TreeIterator<EObject> iter = EcoreUtil.getAllContents(model.getESubpackages()); iter.hasNext();) {
			EObject element = (EObject) iter.next();
			if (element instanceof EClassifier) {
				EClassifier classifier = (EClassifier) element;
				if (classifier.getName().equalsIgnoreCase(type))
					return classifier;
			}
		}
		
		
		return null;
	}
	
	private EClass findEClassByID(int id) throws Exception {
		EClassifier result = findEClassifier(id, "");
		if (result instanceof EClass)
			return (EClass) result;
		return null;
	}
	
	private EClass findEClassByID(long id) throws Exception {
		return findEClassByID((int) id);
	}
	
	/**
	 * Finds the corresponding <code>EAPackage</code> for an <code>EPackage</code>
	 * @param pkg
	 * @return
	 */
	private EAPackage findEAPackageForEPackage(EPackage pkg) {
		String ePackageGUID = pkg.getEAnnotation("MetaData").getDetails().get("guid");
		
		EANamedElement res = findEANamedElementByGUID(ePackageGUID);
		if (res instanceof EAPackage)
			return (EAPackage) res;
		return null;
	}
	
	/**
	 * Finds the corresponding <code>EAElement</code> for an <code>EClass</code>
	 * @param eClass
	 * @return
	 */
	private EAElement findEAElementForEClass(EClass eClass) {
		String eClassGUID = eClass.getEAnnotation("MetaData").getDetails().get("guid");
		
		EANamedElement res = findEANamedElementByGUID(eClassGUID);
		if (res instanceof EAElement)
			return (EAElement) res;
		return null;
	}	
	
	private EANamedElement findEANamedElementByGUID(String guid) {		
		for (TreeIterator<EObject> iter = EcoreUtil.getAllContents(repository.getModels()); iter.hasNext();) {
			EANamedElement element = (EANamedElement) iter.next();
				if (element.getGuid() != null && element.getGuid().equals(guid))
					return element;
		}
		return null;
	}		
	
	
	private static int createLowerBound(String cardinality) {	
		return createBounds(cardinality)[0];
	}
	
	private static int createUpperBound(String cardinality) {		
		return createBounds(cardinality)[1];
	}	
	
	/**
	 * Calculates lower and upper bound for <code>cardinality</code>
	 * @param cardinality (e.g.: 0, '0..*', '*', '1', '2..4',...)
	 * @return two integers:<br>
	 * <code>int[0]</code> --> lowerBound<br>
	 * <code>int[1]</code> --> upperBound<br>
	 */
	private static int[] createBounds(String cardinality) {		
		int[] bounds = new int[2];
		if (cardinality.equals("*")) {
			bounds[0] = 0;
			bounds[1] = -1;
		} else if (cardinality.equals("")) {
			bounds[0] = 1;
			bounds[1] = 1;			
		} else if (cardinality.contains("..")) {
			String[] res = cardinality.split("\\.\\.");
			bounds[0] = Integer.valueOf(res[0]);
			if (res[1].equals("*"))
				bounds[1] = -1;
			else
				bounds[1] = Integer.valueOf(res[1]);
		} else {
			bounds[0] = Integer.valueOf(cardinality);
			bounds[1] = Integer.valueOf(cardinality);
		}		
		return bounds;
	}	
	



	/**
	 * creates the path of an <code>EAPackage</code><br>
	 * E.g. <code>http://package1/package2/package3</code>
	 * @param pkg
	 * @return
	 */
	public static String createPathOfEAPackage(EAPackage pkg) {
		List<EAPackage> superPackages = new LinkedList<EAPackage>();
		EAPackage p = pkg;
		while (p.getSuperPackage() != null) {
			superPackages.add(0, p.getSuperPackage()); //insert to first place
			p = p.getSuperPackage();
		}
		
		String path = "";
		for (EAPackage p2 : superPackages) {
			path = path + p2.getName() + "/";
		}
		return path;		
	}
			

}

