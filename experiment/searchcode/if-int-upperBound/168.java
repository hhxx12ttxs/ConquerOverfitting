/*******************************************************************************
 * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 ******************************************************************************/
package org.eclipse.emf.emfstore.modelmutator.intern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.emfstore.modelmutator.api.ModelMutatorConfiguration;
import org.eclipse.emf.emfstore.modelmutator.api.ModelMutatorUtil;

/**
 * The base class for generating and changing a model.
 * 
 * @author Eugen Neufeld
 * @author Stephan Köhler
 * @author Philip Achenbach
 */
public abstract class AbstractModelMutator {

	/**
	 * The configuration that is used in the process.
	 */
	private final ModelMutatorConfiguration configuration;

	
	/**
	 * The constructor for the model mutation process.
	 * @param config the configuration that is used
	 */
	public AbstractModelMutator(ModelMutatorConfiguration config) {
		this.configuration = config;
	}

	/**
	 * This function is called before the mutation is applied.
	 */
	public abstract void preMutate();

	/**
	 * This function is called after the mutation is applied.
	 */
	public abstract void postMutate();

	/**
	 * The mutation function.
	 */
	public void mutate() {
		preMutate();
		
		setContaintments();

		setReferences();
		
		postMutate();
	}

	/**
	 * This function generates the Containments of a model.
	 */
	public void setContaintments() {
		Map<Integer, List<EObject>> depthToParentObjects = new LinkedHashMap<Integer, List<EObject>>();
		List<EObject> parentsInThisDepth = new LinkedList<EObject>();
		parentsInThisDepth.add(configuration.getRootEObject());
		int currentDepth = 0;
		depthToParentObjects.put(1, new LinkedList<EObject>());
		
		// We can skip the root level if it is provided by the configuration
		if (configuration.isDoNotGenerateRoot()){
			depthToParentObjects.put(2, new LinkedList<EObject>());
			currentDepth++;
			parentsInThisDepth = new LinkedList<EObject>(configuration.getRootEObject().eContents());
		}
		
		// Use a breadth-first search (BFS) to generate all children/containments
		while (currentDepth < configuration.getDepth()) {
			// for all parent EObjects in this depth
			for (EObject nextParentEObject : parentsInThisDepth) {
				//ModelMutatorUtil.setEObjectAttributes(nextParentEObject, configuration.getRandom(), configuration.getExceptionLog(), configuration.isIgnoreAndLog());
				List<EObject> children = generateChildren(nextParentEObject, currentDepth==0 && configuration.isAllElementsOnRoot());
				// will the just created EObjects have children?
				depthToParentObjects.get(currentDepth + 1).addAll(children);
			}

			// proceed to the next level
			currentDepth++;
			parentsInThisDepth = depthToParentObjects.get(currentDepth);
			depthToParentObjects.put((currentDepth + 1), new LinkedList<EObject>());
		}
	}

	/**
	 * Generates children for a certain parent EObject. Generation includes
	 * setting containment references and attributes. All required references
	 * are set first, thus the specified width might be exceeded.
	 * 
	 * @param parentEObject
	 *            the EObject to generate children for
	 * @param generateAllReferences
	 * 			  Should we generate every EObject on root level
	 * @return all generated children as a list
	 * @see #generateContainments(EObject, EReference, int)
	 */
	public List<EObject> generateChildren(EObject parentEObject, boolean generateAllReferences) {
		Map<EReference, List<EObject>> currentContainments = new HashMap<EReference, List<EObject>>();
		List<EObject> result = new LinkedList<EObject>();
		List<EObject> toDelete=new ArrayList<EObject>();
		//If the current element contains already children, delete them randomly or count them 
		for (EObject curChild : parentEObject.eContents()) {
			if(configuration.getRandom().nextBoolean()){
				toDelete.add(curChild);
				continue;
			}
			if (!currentContainments.containsKey(curChild.eContainmentFeature())) {
				currentContainments.put(curChild.eContainmentFeature(), new LinkedList<EObject>());
			}
			currentContainments.get(curChild.eContainmentFeature()).add(curChild);
			if (configuration.getRandom().nextBoolean()) {
				ModelMutatorUtil.setEObjectAttributes(curChild, configuration.getRandom(), configuration.getExceptionLog(), configuration.isIgnoreAndLog());
			}
			result.add(curChild);
		}
		//delete random selected elements
		for(EObject curChild:toDelete){
			ModelMutatorUtil.removeFullPerCommand(curChild, configuration.getExceptionLog(), configuration.isIgnoreAndLog());

		}

		List<EReference> references = new LinkedList<EReference>();
		//generate the children of the current element so that the lower bound holds or that there is a child of each sort 
		for (EReference reference : parentEObject.eClass().getEAllContainments()) {
			if (configuration.geteStructuralFeaturesToIgnore().contains(reference)
					|| !ModelMutatorUtil.isValid(reference, parentEObject, configuration.getExceptionLog(), configuration.isIgnoreAndLog())) {
				continue;
			}
			references.add(reference);
			int numCurrentContainments = 0;
			if (currentContainments.containsKey(reference)) {
				numCurrentContainments = currentContainments.get(reference).size();
			}
			
			List<EObject> contain=null;
			if (generateAllReferences) {
				contain = generateFullDifferentContainment(parentEObject, reference);
			} else {
				contain = generateMinContainments(parentEObject, reference, reference.getLowerBound() - numCurrentContainments);
			}
			
			if (!currentContainments.containsKey(reference)) {
				currentContainments.put(reference, new LinkedList<EObject>());
			}
			currentContainments.get(reference).addAll(contain);

			result.addAll(contain);
		}
		// fill up the references where more elements are needed
		if (references.size() != 0) {
			for (int i = result.size(); i < configuration.getWidth() && references.size() != 0; i++) {
				Collections.shuffle(references, configuration.getRandom());
				EReference reference = references.get(0);
				int upperBound = Integer.MAX_VALUE;
				if (reference.getUpperBound()!=EReference.UNBOUNDED_MULTIPLICITY && reference.getUpperBound()!=EReference.UNSPECIFIED_MULTIPLICITY) {
					upperBound = reference.getUpperBound();
				}
				if (currentContainments.get(reference).size() < upperBound) {
					List<EObject> contain = generateMinContainments(parentEObject, reference, 1);
					if (!currentContainments.containsKey(reference)) {
						currentContainments.put(reference, new LinkedList<EObject>());
					}
					currentContainments.get(reference).addAll(contain);
					result.addAll(contain);
				} else {
					references.remove(reference);
					i--;
				}
			}
		}
		return result;
	}

	private List<EObject> generateFullDifferentContainment(EObject parentEObject, EReference reference) {
		List<EClass> allEClasses = new LinkedList<EClass>();
		allEClasses.addAll(ModelMutatorUtil.getAllEContainments(reference));

		// only allow EClasses that appear in the specified EPackage
		allEClasses.retainAll(ModelMutatorUtil.getAllEClasses(configuration.getModelPackage()));
		// don't allow any EClass or sub class of all EClasses specified in ignoredClasses
		for (EClass eClass : configuration.geteClassesToIgnore()) {
			allEClasses.remove(eClass);
			allEClasses.removeAll(ModelMutatorUtil.getAllSubEClasses(eClass));
		}
		
		List<EObject> result = new LinkedList<EObject>();
		for (EClass eClass : allEClasses){
			EObject newChild = generateElement(parentEObject,eClass,reference);
			// was creating the child successful?
			if (newChild != null) {
				result.add(newChild);
			}
		}
		// Fill with random objects to get to the lowerBound
		int numToFillMin = reference.getLowerBound() - result.size();
		if (numToFillMin > 0) {
			result.addAll(generateMinContainments(parentEObject, reference, numToFillMin));
		}
		return result;
	}

	/**
	 * Creates valid instances of children for <code>parentEObject</code> using
	 * the information in the <code>reference</code>. They are set as a child of
	 * <code>parentEObject</code> with AddCommand/SetCommand.
	 * 
	 * @param parentEObject
	 *            the EObject that shall contain the new instances of children
	 * @param reference
	 *            the containment reference
	 * @param width
	 *            the amount of children to create
	 * @return a list containing the instances of children or an empty list if
	 *         the operation failed
	 * 
	 * @see ModelGeneratorUtil#addPerCommand(EObject, EStructuralFeature,
	 *      Object, Set, boolean)
	 * @see ModelGeneratorUtil#setPerCommand(EObject, EStructuralFeature,
	 *      Object, Set, boolean)
	 */
	public List<EObject> generateMinContainments(EObject parentEObject, EReference reference, int width) {
		List<EObject> result = new LinkedList<EObject>();
		for (int i = 0; i < width; i++) {
			EClass eClass = getValidEClass(reference);
			if (eClass != null) {
				EObject newChild = generateElement(parentEObject, eClass, reference);
				// was creating the child successful?
				if (newChild != null) {
					result.add(newChild);
				}
			}
		}
		return result;
	}

	public EObject generateElement(EObject parentEObject, EClass eClass, EReference reference) {
		// create child and add it to parentEObject
		// Old version which used another method:
		//EObject newChild = setContainment(parentEObject, eClass, reference);
		EObject newChild = null;
		// create and set attributes
		EObject newEObject = EcoreUtil.create(eClass);
		ModelMutatorUtil.setEObjectAttributes(newEObject, configuration.getRandom(), configuration.getExceptionLog(), configuration.isIgnoreAndLog());
		// reference created EObject to the parent
		if (reference.isMany()) {
			newChild = ModelMutatorUtil.addPerCommand(parentEObject, reference, newEObject, configuration.getExceptionLog(), configuration.isIgnoreAndLog());
		} else {
			newChild = ModelMutatorUtil.setPerCommand(parentEObject, reference, newEObject, configuration.getExceptionLog(), configuration.isIgnoreAndLog());
		}
		return newChild;
	}

	/**
	 * Returns a valid EClasses randomly for the given reference.
	 * @param eReference
	 * 			the eReference the EClass is searched for
	 * @return
	 * 			a valid eClass for the eReference
	 */
	public EClass getValidEClass(EReference eReference) {
		List<EClass> allEClasses = new LinkedList<EClass>();
		allEClasses.addAll(ModelMutatorUtil.getAllEContainments(eReference));

		// only allow EClasses that appear in the specified EPackage
		allEClasses.retainAll(ModelMutatorUtil.getAllEClasses(configuration.getModelPackage()));
		// don't allow any EClass or sub class of all EClasses specified in
		// ignoredClasses
		for (EClass eClass : configuration.geteClassesToIgnore()) {
			allEClasses.remove(eClass);
			allEClasses.removeAll(ModelMutatorUtil.getAllSubEClasses(eClass));
		}
		if (allEClasses.isEmpty()) {
			// no valid EClass left
			return null;
		}
		// random seed all the time
		Collections.shuffle(allEClasses, configuration.getRandom());
		return allEClasses.get(0);
	}
	
	/**
	 * Sets all references for every child (direct and indirect)
	 * of <code>root</code>.
	 * 
	 * @see #changeEObjectAttributes(EObject)
	 * @see #changeEObjectReferences(EObject, Map)
	 */
	public void setReferences() {
		EObject rootObject = configuration.getRootEObject();
		Map<EClass, List<EObject>> allObjectsByEClass = ModelMutatorUtil.getAllClassesAndObjects(rootObject);
		for (EClass eClass : allObjectsByEClass.keySet()) {
			for (EObject eObject : allObjectsByEClass.get(eClass)) {
				generateReferences(eObject, allObjectsByEClass);
			}
		}
	}
	
	/**
	 * Generates references (no containment references) for an EObject. All
	 * valid references are set with EObjects generated during the generation
	 * process.
	 * 
	 * @param eObject
	 *            the EObject to set references for
	 * @param allObjectsByEClass
	 *            all possible EObjects that can be referenced, mapped to their
	 *            EClass
	 * @see ModelGeneratorHelper#setReference(EObject, EClass, EReference, Map)
	 */
	public void generateReferences(EObject eObject, Map<EClass, List<EObject>> allObjectsByEClass) {
		for (EReference reference : ModelMutatorUtil.getValidReferences(eObject, configuration.getExceptionLog(), configuration.isIgnoreAndLog())) {
			for (EClass nextReferenceClass : ModelMutatorUtil.getReferenceClasses(reference, allObjectsByEClass.keySet())) {
				setEObjectReference(eObject, nextReferenceClass, reference, allObjectsByEClass);
			}
		}
	}
	
	/**
	 * Sets a reference, if the upper bound allows it, using
	 * {@link ModelGeneratorUtil#setReference}.
	 * 
	 * @param eObject
	 *            the EObject to set the reference for
	 * @param referenceClass
	 *            the EClass of EObjects that shall be referenced
	 * @param reference
	 *            the EReference that shall be set
	 * @param allEObjects
	 *            all possible EObjects that can be referenced
	 * @see ModelGeneratorUtil#setReference(EObject, EClass, EReference, Random,
	 *      Set, boolean, Map)
	 */
	public void setEObjectReference(EObject eObject, EClass referenceClass, EReference reference,
		Map<EClass, List<EObject>> allEObjects) {
		
		// Delete already set references (only applies when changing a model)
		if (eObject.eIsSet(reference)) {
			//check whether to delete or not
			if(configuration.getRandom().nextBoolean()){
				//do different stuff, depending on reference type
				if(reference.isMany()){
					List<EObject> toDelte=new ArrayList<EObject>();
					//check whether to delete references randomly or all at once 
					if(configuration.getRandom().nextBoolean()){
						for(EObject refObj:(EList<EObject>)eObject.eGet(reference)){
							//check whether to delete this reference
							if(configuration.getRandom().nextBoolean()){
								toDelte.add(refObj);
							}
						}
					}
					else{
						toDelte.addAll((EList<EObject>)eObject.eGet(reference));
					}
					ModelMutatorUtil.removePerCommand(eObject, reference, toDelte, configuration.getExceptionLog(), configuration.isIgnoreAndLog());	
				}
				else
					eObject.eUnset(reference);
			}
			else{
				//nothing was deleted so no references need to be set
				return;
			}
		}
		// check if the upper bound is reached
		if (!ModelMutatorUtil.isValid(reference, eObject, configuration.getExceptionLog(), configuration.isIgnoreAndLog()) ||
				(!reference.isMany() && eObject.eIsSet(reference))) {
			return;
		}
		
		ModelMutatorUtil.setReference(eObject, referenceClass, reference, configuration.getRandom(),
			configuration.getExceptionLog(), configuration.isIgnoreAndLog(), allEObjects);
	}
}

